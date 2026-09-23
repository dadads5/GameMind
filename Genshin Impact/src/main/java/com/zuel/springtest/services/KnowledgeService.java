package com.zuel.springtest.services;

import com.zuel.springtest.dto.ai.SourceChunk;
import com.zuel.springtest.entity.Post;
import com.zuel.springtest.entity.PostChunk;
import com.zuel.springtest.mapper.PostChunkMapper;
import com.zuel.springtest.mapper.RagPostMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * RAG 知识库服务
 *
 * <p>职责：
 * <ol>
 *   <li>建索引：把站内帖子（标题+正文）向量化后写入 {@code post_chunk} 表；</li>
 *   <li>检索：根据用户问题向量，与全量片段做余弦相似度，召回 top-k 作为参考素材；</li>
 *   <li>同步：发帖/改帖时增量更新，删帖时清理。</li>
 * </ol>
 *
 * <p>规模说明：社区帖子量级小（数百条），检索时一次性把向量加载进内存做余弦计算即可，
 * 无需引入专门的向量数据库。若日后数据量增大，可平滑替换为向量库而不影响调用方。
 */
@Slf4j
@Service
public class KnowledgeService {

    private final EmbeddingService embeddingService;
    private final PostChunkMapper chunkMapper;
    private final RagPostMapper ragPostMapper;

    /** 召回片段数 */
    private static final int TOP_K = 3;
    /** 相似度阈值：低于此值视为无相关素材，触发「未找到站内攻略」降级 */
    private static final double SIM_THRESHOLD = 0.20;

    public KnowledgeService(EmbeddingService embeddingService,
                            PostChunkMapper chunkMapper,
                            RagPostMapper ragPostMapper) {
        this.embeddingService = embeddingService;
        this.chunkMapper = chunkMapper;
        this.ragPostMapper = ragPostMapper;
    }

    /**
     * 启动后若索引为空则后台自动建一次（不阻塞启动）
     */
    @PostConstruct
    public void initIndexIfEmpty() {
        if (!embeddingService.isConfigured()) {
            log.warn("Embedding 未配置，跳过 RAG 自动建索引（可在配置后调用重建接口）");
            return;
        }
        if (chunkMapper.count() == 0) {
            Thread t = new Thread(this::rebuildIndex);
            t.setDaemon(true);
            t.setName("rag-index-init");
            t.start();
        }
    }

    /**
     * 定时全量重建（兜底，保证长期一致性）
     */
    @Scheduled(fixedDelay = 30 * 60 * 1000, initialDelay = 30 * 60 * 1000)
    public void rebuildIndex() {
        if (!embeddingService.isConfigured()) {
            return;
        }
        try {
            List<Post> posts = ragPostMapper.selectAllPosts();
            for (Post p : posts) {
                indexPost(p.getId());
            }
            log.info("RAG 索引重建完成，帖子数={}", posts.size());
        } catch (Exception e) {
            log.error("RAG 索引重建失败", e);
        }
    }

    /**
     * 为单个帖子建立/重建索引（增量同步入口）
     */
    public void indexPost(Long postId) {
        if (!embeddingService.isConfigured()) {
            return;
        }
        try {
            Post p = ragPostMapper.selectPostById(postId);
            if (p == null) {
                return;
            }
            chunkMapper.deleteByPostId(postId);
            String text = ((p.getTitle() == null ? "" : p.getTitle()) + "\n"
                    + (p.getContent() == null ? "" : p.getContent())).trim();
            if (text.isEmpty()) {
                return;
            }
            // 单条嵌入，超长截断到 2000 字（embedding 模型有 token 上限）
            String toEmbed = text.length() > 2000 ? text.substring(0, 2000) : text;
            float[] vec = embeddingService.embed(toEmbed);
            PostChunk chunk = new PostChunk();
            chunk.setPostId(postId);
            chunk.setTitle(p.getTitle());
            chunk.setChunkIndex(0);
            chunk.setContent(toEmbed);
            chunk.setEmbedding(serialize(vec));
            chunkMapper.insert(chunk);
        } catch (Exception e) {
            log.error("索引帖子失败 postId={}", postId, e);
        }
    }

    /**
     * 删除帖子的索引
     */
    public void deleteByPostId(Long postId) {
        try {
            chunkMapper.deleteByPostId(postId);
        } catch (Exception e) {
            log.error("删除帖子索引失败 postId={}", postId, e);
        }
    }

    /**
     * 检索与问题最相关的 top-k 片段
     *
     * @return 命中片段列表（已按相似度降序）；未配置或异常时返回空，调用方据此降级
     */
    public List<SourceChunk> retrieve(String query) {
        if (!embeddingService.isConfigured()) {
            return List.of();
        }
        try {
            float[] q = embeddingService.embed(query);
            List<PostChunk> all = chunkMapper.selectAll();
            if (all.isEmpty()) {
                return List.of();
            }
            List<Scored> scored = new ArrayList<>();
            for (PostChunk c : all) {
                float[] v = deserialize(c.getEmbedding());
                if (v == null || v.length != q.length) {
                    continue;
                }
                double sim = cosine(q, v);
                if (sim >= SIM_THRESHOLD) {
                    scored.add(new Scored(c, sim));
                }
            }
            scored.sort((a, b) -> Double.compare(b.sim, a.sim));
            List<SourceChunk> result = new ArrayList<>();
            for (int i = 0; i < Math.min(TOP_K, scored.size()); i++) {
                PostChunk c = scored.get(i).chunk;
                Integer communityId = null;
                try {
                    Post srcPost = ragPostMapper.selectPostById(c.getPostId());
                    if (srcPost != null) {
                        communityId = srcPost.getCommunityId();
                    }
                } catch (Exception ex) {
                    log.warn("查询来源帖子 communityId 失败 postId={}", c.getPostId(), ex);
                }
                result.add(new SourceChunk(c.getPostId(), c.getTitle(), c.getContent(), scored.get(i).sim, communityId));
            }
            return result;
        } catch (Exception e) {
            log.error("RAG 检索失败", e);
            return List.of();
        }
    }

    private static class Scored {
        final PostChunk chunk;
        final double sim;

        Scored(PostChunk chunk, double sim) {
            this.chunk = chunk;
            this.sim = sim;
        }
    }

    private static double cosine(float[] a, float[] b) {
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        if (na == 0 || nb == 0) {
            return 0;
        }
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    private static String serialize(float[] v) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < v.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(v[i]);
        }
        return sb.toString();
    }

    private static float[] deserialize(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        String[] parts = s.split(",");
        float[] v = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                v[i] = Float.parseFloat(parts[i].trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return v;
    }
}
