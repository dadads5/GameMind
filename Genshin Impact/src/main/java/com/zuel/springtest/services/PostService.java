package com.zuel.springtest.services;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zuel.springtest.common.BusinessException;
import com.zuel.springtest.common.PageResult;
import com.zuel.springtest.common.ResultCode;
import com.zuel.springtest.dto.post.PostCreateRequest;
import com.zuel.springtest.dto.post.PostQuery;
import com.zuel.springtest.dto.post.PostUpdateRequest;
import com.zuel.springtest.entity.Post;
import com.zuel.springtest.entity.PostImage;
import com.zuel.springtest.mapper.BoardMapper;
import com.zuel.springtest.mapper.CommentLikeMapper;
import com.zuel.springtest.mapper.CommentMapper;
import com.zuel.springtest.mapper.PostImageMapper;
import com.zuel.springtest.mapper.PostLikeMapper;
import com.zuel.springtest.mapper.PostMapper;
import com.zuel.springtest.mapper.UserMapper;
import com.zuel.springtest.vo.LikeResult;
import com.zuel.springtest.vo.PostVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 帖子业务
 *
 * <p>所有查询统一带出作者信息；点赞基于 post_like 表实现「一人一赞、可取消」；
 * 配图存储于 post_image 表，由 Service 层批量关联加载；板块名称按 board_id 查 board 表填充。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostImageMapper postImageMapper;
    private final BoardMapper boardMapper;
    private final UserMapper userMapper;
    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final NotificationService notificationService;
    private final StatisticsService statisticsService;
    private final StringRedisTemplate stringRedis;

    /** 浏览量 Redis 增量 key 前缀（值为纯数字，供 INCR 使用） */
    private static final String VIEW_DELTA_PREFIX = "post:view:delta:";
    /** 待落库浏览增量的帖子 id 集合 */
    private static final String VIEW_DIRTY_KEY = "posts:view:dirty";

    /** 热榜 ZSet key（member=postId 字符串，score=热度分） */
    private static final String HOT_RANK_KEY = "posts:hot";
    /** 热榜重建时单批批量写入的条数 */
    private static final int REBUILD_BATCH_SIZE = 500;
    /** 热度权重 */
    private static final double WEIGHT_VIEW = 1.0;
    private static final double WEIGHT_LIKE = 3.0;
    private static final double WEIGHT_COMMENT = 5.0;
    /** 新帖时间加成：14 天内每天 +5 分，越新越高 */
    private static final double TIME_BONUS_PER_DAY = 5.0;
    private static final long TIME_BONUS_WINDOW_DAYS = 14;

    // ------------------------------------------------------------------
    // 查询
    // ------------------------------------------------------------------

    /**
     * 分页查询帖子
     */
    public PageResult<PostVO> listPosts(PostQuery query, Long currentUserId) {
        query.normalize();

        PageHelper.startPage(query.getPage(), query.getSize());
        List<Post> posts = postMapper.selectByCondition(
                query.getBoardId(),
                query.getCommunityId(),
                blankToNull(query.getKeyword()),
                query.getAuthorId(),
                query.getSort());

        PageInfo<Post> pageInfo = new PageInfo<>(posts);
        fillLiked(pageInfo.getList(), currentUserId);
        fillImages(pageInfo.getList());
        fillBoardNames(pageInfo.getList());
        return PageResult.of(pageInfo).map(PostVO::from);
    }

    /**
     * 热门帖子：优先读 Redis 热榜 ZSet，空/异常时回退 MySQL。
     *
     * <p>ZSet 为空（首次启动 / Redis 清库）或读取异常时直接走数据库兜底，由定时任务异步重建，
     * 不在请求线程内触发全量重建，避免冷启动时并发请求同时全表扫描打满线程池。
     * 带 board/community 筛选时在内存按 ZSet 顺序过滤后取前 limit。
     */
    public List<PostVO> listHotPosts(Integer boardId, Integer communityId, int limit, Long currentUserId) {
        if (limit < 1 || limit > 50) {
            limit = 10;
        }
        try {
            Set<String> raw = stringRedis.opsForZSet().reverseRange(HOT_RANK_KEY, 0, 199);
            if (raw != null && !raw.isEmpty()) {
                List<Long> ids = raw.stream().map(Long::valueOf).toList();
                List<Post> posts = postMapper.selectByIds(ids);
                Map<Long, Post> map = posts.stream()
                        .collect(Collectors.toMap(Post::getId, p -> p, (a, b) -> a));
                List<Post> ordered = ids.stream().map(map::get).filter(Objects::nonNull).toList();

                List<Post> filtered = ordered;
                if (boardId != null || communityId != null) {
                    Map<Integer, Integer> boardCommunity = buildBoardCommunityMap(ordered);
                    filtered = ordered.stream()
                            .filter(p -> (boardId == null || boardId.equals(p.getBoardId()))
                                    && (communityId == null
                                        || communityId.equals(boardCommunity.get(p.getBoardId()))))
                            .toList();
                }
                List<Post> top = filtered.stream().limit(limit).toList();
                fillLiked(top, currentUserId);
                fillImages(top);
                fillBoardNames(top);
                return top.stream().map(PostVO::from).toList();
            }
        } catch (Exception e) {
            log.warn("读取热榜缓存失败，回退数据库", e);
        }
        List<Post> posts = postMapper.selectHot(boardId, communityId, limit);
        fillLiked(posts, currentUserId);
        fillImages(posts);
        fillBoardNames(posts);
        return posts.stream().map(PostVO::from).toList();
    }

    /**
     * 帖子详情，同时浏览量 +1
     */
    @Transactional
    public PostVO getPostDetail(Long id, Long currentUserId) {
        Post post = getPostOrThrow(id);

        post.setViewCount(incrementViewCount(id, post.getViewCount()));

        fillLiked(List.of(post), currentUserId);
        fillImages(List.of(post));
        fillBoardNames(List.of(post));
        return PostVO.from(post);
    }

    // ------------------------------------------------------------------
    // 浏览量异步计数
    // ------------------------------------------------------------------

    /**
     * 浏览量 +1：写入 Redis 增量并登记脏 key，由定时任务批量落库。
     * Redis 不可用时降级为直接写库，保证浏览量不丢失。
     */
    private int incrementViewCount(Long id, Integer dbViews) {
        int base = dbViews == null ? 0 : dbViews;
        try {
            Long cur = stringRedis.opsForValue().increment(VIEW_DELTA_PREFIX + id);
            // 兜底 TTL：仅首次写入时设置，长期无人浏览的 key 1 小时后自动过期
            if (cur != null && cur == 1) {
                stringRedis.expire(VIEW_DELTA_PREFIX + id, java.time.Duration.ofHours(1));
            }
            stringRedis.opsForSet().add(VIEW_DIRTY_KEY, id.toString());
            return base + (cur == null ? 0 : cur.intValue());
        } catch (Exception e) {
            log.warn("浏览量计数写入 Redis 失败，降级直接写库 post={}", id, e);
            try {
                postMapper.incrementViewCount(id);
                return base + 1;
            } catch (Exception ex) {
                return base;
            }
        }
    }

    /**
     * 定时把 Redis 中的浏览增量批量落库（每 5 分钟，上一次结束 5 分钟后触发）。
     * 任意异常都被吞掉，绝不阻塞主流程。
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void flushViewCounts() {
        try {
            Set<String> dirtyIds = stringRedis.opsForSet().members(VIEW_DIRTY_KEY);
            if (dirtyIds == null || dirtyIds.isEmpty()) {
                return;
            }
            for (String sid : dirtyIds) {
                try {
                    Long id = Long.valueOf(sid);
                    String dv = stringRedis.opsForValue().get(VIEW_DELTA_PREFIX + id);
                    if (dv == null) {
                        stringRedis.opsForSet().remove(VIEW_DIRTY_KEY, sid);
                        continue;
                    }
                    long delta = Long.parseLong(dv);
                    if (delta > 0) {
                        postMapper.addViewCount(id, delta);
                        // 仅回退已落库部分，保留落库期间新增的浏览
                        stringRedis.opsForValue().increment(VIEW_DELTA_PREFIX + id, -delta);
                        // 浏览增量同步到热榜
                        incrementHotScore(id, delta * WEIGHT_VIEW);
                    }
                    stringRedis.opsForSet().remove(VIEW_DIRTY_KEY, sid);
                } catch (Exception e) {
                    log.warn("浏览量落库失败，跳过 post={}", sid, e);
                }
            }
        } catch (Exception e) {
            log.warn("浏览量落库任务异常", e);
        }
    }

    // ------------------------------------------------------------------
    // 写操作
    // ------------------------------------------------------------------

    /**
     * 发帖
     */
    @Transactional
    public PostVO createPost(Long userId, PostCreateRequest request) {
        validateBoard(request.getBoardId());

        Post post = new Post();
        post.setUserId(userId);
        post.setBoardId(request.getBoardId());
        post.setTitle(request.getTitle().trim());
        post.setContent(request.getContent());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCreatedAt(LocalDateTime.now());
        post.setImages(request.getImages());

        if (postMapper.insert(post) <= 0) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "发帖失败，请稍后重试");
        }
        saveImages(post.getId(), request.getImages());

        userMapper.selectById(userId).ifPresent(user -> {
            post.setAuthorName(user.getUsername());
            post.setAuthorAvatar(user.getAvatar());
        });
        post.setLiked(false);
        log.info("用户 {} 发布帖子：{}（id={}）", userId, post.getTitle(), post.getId());
        recalcHotScore(post.getId());
        statisticsService.evict();
        return PostVO.from(post);
    }

    /**
     * 编辑帖子，仅作者可操作
     */
    @Transactional
    public PostVO updatePost(Long id, Long userId, PostUpdateRequest request) {
        Post post = getPostOrThrow(id);
        ensureOwner(post, userId);
        validateBoard(request.getBoardId());

        if (postMapper.updateContent(id, request.getTitle().trim(), request.getContent(), request.getBoardId()) <= 0) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "更新帖子失败");
        }
        postImageMapper.deleteByPostId(id);
        saveImages(id, request.getImages());

        post.setTitle(request.getTitle().trim());
        post.setContent(request.getContent());
        post.setBoardId(request.getBoardId());
        post.setImages(request.getImages());
        return PostVO.from(post);
    }

    /**
     * 删除帖子，仅作者可操作
     */
    @Transactional
    public void deletePost(Long id, Long userId) {
        Post post = getPostOrThrow(id);
        ensureOwner(post, userId);

        commentLikeMapper.deleteByPostId(id);
        commentMapper.deleteByPostId(id);
        postLikeMapper.deleteByPostId(id);
        postImageMapper.deleteByPostId(id);

        if (postMapper.deleteById(id) <= 0) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "删除帖子失败");
        }
        // 清理 Redis 残留，避免已删除的帖子继续出现在热榜
        evictPostCaches(id);
        log.info("帖子 {} 已被用户 {} 删除", id, userId);
        statisticsService.evict();
    }

    /**
     * 管理员删除帖子：不做作者校验，级联清理范围与作者自删保持一致
     *
     * <p>原先管理员删帖只清了 comment，导致 post_like / post_image / comment_like 留下孤儿数据。
     */
    @Transactional
    public void deletePostByAdmin(Long id) {
        Post post = getPostOrThrow(id);

        commentLikeMapper.deleteByPostId(id);
        commentMapper.deleteByPostId(id);
        postLikeMapper.deleteByPostId(id);
        postImageMapper.deleteByPostId(id);

        if (postMapper.deleteById(id) <= 0) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "删除帖子失败");
        }
        evictPostCaches(id);
        log.info("帖子 {} 已被管理员删除", id);
        statisticsService.evict();
    }

    /**
     * 清理帖子在 Redis 中的残留：热榜成员、浏览增量、待落库脏集合
     *
     * <p>帖子删除后若不清理，其 id 会一直留在热榜 ZSet 中。Redis 操作失败不影响主流程，
     * 热榜本身也支持从数据库重建。
     */
    private void evictPostCaches(Long id) {
        try {
            stringRedis.opsForZSet().remove(HOT_RANK_KEY, id.toString());
            stringRedis.delete(VIEW_DELTA_PREFIX + id);
            stringRedis.opsForSet().remove(VIEW_DIRTY_KEY, id.toString());
        } catch (Exception e) {
            log.warn("清理帖子 Redis 缓存失败 post={}", id, e);
        }
    }

    /**
     * 点赞 / 取消点赞
     */
    @Transactional
    public LikeResult toggleLike(Long postId, Long userId) {
        Post post = getPostOrThrow(postId);

        boolean alreadyLiked = postLikeMapper.exists(postId, userId);
        if (alreadyLiked) {
            postLikeMapper.deleteByPostAndUser(postId, userId);
            postMapper.decrementLikeCount(postId);
            incrementHotScore(postId, -WEIGHT_LIKE);
        } else {
            postLikeMapper.insert(postId, userId);
            postMapper.incrementLikeCount(postId);
            incrementHotScore(postId, WEIGHT_LIKE);
        }

        // 通知帖子作者：点赞生成（或刷新）通知，取消点赞则撤回
        notificationService.notifyLikePost(post, userId, !alreadyLiked);

        return new LikeResult(!alreadyLiked, postMapper.getLikeCount(postId));
    }

    // ------------------------------------------------------------------
    // 统计
    // ------------------------------------------------------------------

    public int countAll() {
        return postMapper.countAll();
    }

    public int sumViewCount() {
        return postMapper.sumViewCount();
    }

    public int countToday() {
        return postMapper.countToday();
    }

    // ------------------------------------------------------------------
    // 私有方法
    // ------------------------------------------------------------------

    private Post getPostOrThrow(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "帖子不存在");
        }
        return post;
    }

    private void ensureOwner(Post post, Long userId) {
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能操作自己发布的帖子");
        }
    }

    private void validateBoard(Integer boardId) {
        // 改为查库校验：支持 forum_optimize 之后动态新增的板块（含火影子板块 8~13）
        if (boardId == null || boardMapper.selectById(boardId).isEmpty()) {
            throw new BusinessException("板块不存在");
        }
    }

    /**
     * 批量填充当前用户的点赞状态
     */
    private void fillLiked(List<Post> posts, Long currentUserId) {
        if (posts == null || posts.isEmpty()) {
            return;
        }
        if (currentUserId == null) {
            posts.forEach(post -> post.setLiked(false));
            return;
        }
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Set<Long> likedIds = new HashSet<>(postLikeMapper.selectLikedPostIds(currentUserId, postIds));
        posts.forEach(post -> post.setLiked(likedIds.contains(post.getId())));
    }

    /**
     * 批量加载帖子配图，按 post_id 分组后写回 Post.images
     */
    private void fillImages(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return;
        }
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        List<PostImage> all = postImageMapper.selectByPostIds(postIds);
        Map<Long, List<String>> imageMap = all.stream()
                .collect(Collectors.groupingBy(PostImage::getPostId,
                        Collectors.mapping(PostImage::getUrl, Collectors.toList())));
        posts.forEach(post -> post.setImages(imageMap.getOrDefault(post.getId(), Collections.emptyList())));
    }

    /**
     * 批量填充板块名称（按 board_id 查 board 表）
     */
    private void fillBoardNames(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return;
        }
        Map<Integer, String> nameMap = new HashMap<>();
        Map<Integer, Integer> communityMap = new HashMap<>();
        posts.stream().map(Post::getBoardId).distinct().forEach(id -> {
            if (id != null) {
                boardMapper.selectById(id).ifPresent(board -> {
                    nameMap.put(board.getId(), board.getName());
                    communityMap.put(board.getId(), board.getCommunityId());
                });
            }
        });
        posts.forEach(post -> {
            post.setBoardName(nameMap.get(post.getBoardId()));
            post.setCommunityId(communityMap.get(post.getBoardId()));
        });
    }

    /**
     * 写入帖子配图（按列表顺序作为 sort）
     */
    private void saveImages(Long postId, List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return;
        }
        int sort = 0;
        for (String url : urls) {
            if (url == null || url.isBlank()) {
                continue;
            }
            PostImage image = new PostImage();
            image.setPostId(postId);
            image.setUrl(url.trim());
            image.setSort(sort++);
            postImageMapper.insert(image);
        }
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    // ------------------------------------------------------------------
    // 热榜（Redis ZSet）
    // ------------------------------------------------------------------

    /**
     * 重建热榜 ZSet（每 10 分钟，避免增量事件遗漏导致排名失真）
     *
     * <p>三个关键点：
     * <ul>
     *   <li>数据侧只取热度 Top 1000（见 PostMapper#selectAllHot），不做全表扫描；</li>
     *   <li>先写临时 key，再用 RENAME 原子替换，避免重建中途失败导致热榜只剩半截数据；</li>
     *   <li>分批批量 ZADD，避免逐条写入产生 N 次网络往返。</li>
     * </ul>
     */
    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void rebuildHotRanking() {
        try {
            List<Post> posts = postMapper.selectAllHot();
            if (posts.isEmpty()) {
                return;
            }

            String tempKey = HOT_RANK_KEY + ":rebuild";
            stringRedis.delete(tempKey);

            for (int i = 0; i < posts.size(); i += REBUILD_BATCH_SIZE) {
                int end = Math.min(i + REBUILD_BATCH_SIZE, posts.size());
                Set<ZSetOperations.TypedTuple<String>> batch = new LinkedHashSet<>();
                for (Post p : posts.subList(i, end)) {
                    batch.add(ZSetOperations.TypedTuple.of(p.getId().toString(), hotScore(p)));
                }
                stringRedis.opsForZSet().add(tempKey, batch);
            }

            // RENAME 是单命令原子操作，不会出现热榜为空的中间状态
            stringRedis.rename(tempKey, HOT_RANK_KEY);
            log.info("热榜重建完成，共 {} 条", posts.size());
        } catch (Exception e) {
            log.warn("热榜重建失败", e);
        }
    }

    /**
     * 事件驱动：热度增量（点赞/评论/浏览实时累加）
     */
    public void incrementHotScore(Long id, double delta) {
        try {
            stringRedis.opsForZSet().incrementScore(HOT_RANK_KEY, id.toString(), delta);
        } catch (Exception e) {
            log.warn("热榜分数更新失败 post={}", id, e);
        }
    }

    /**
     * 发帖时按当前计数重算该帖分数
     */
    private void recalcHotScore(Long id) {
        try {
            Post p = postMapper.selectById(id);
            if (p != null) {
                stringRedis.opsForZSet().add(HOT_RANK_KEY, id.toString(), hotScore(p));
            }
        } catch (Exception e) {
            log.warn("热榜分数重算失败 post={}", id, e);
        }
    }

    /**
     * 热度公式：浏览*1 + 点赞*3 + 评论*5 + 新帖时间加成
     */
    private double hotScore(Post p) {
        int views = p.getViewCount() == null ? 0 : p.getViewCount();
        int likes = p.getLikeCount() == null ? 0 : p.getLikeCount();
        int comments = p.getCommentCount() == null ? 0 : p.getCommentCount();
        double timeBonus = 0;
        if (p.getCreatedAt() != null) {
            long days = java.time.Duration.between(p.getCreatedAt(), LocalDateTime.now()).toDays();
            timeBonus = Math.max(0, TIME_BONUS_WINDOW_DAYS - days) * TIME_BONUS_PER_DAY;
        }
        return views * WEIGHT_VIEW + likes * WEIGHT_LIKE + comments * WEIGHT_COMMENT + timeBonus;
    }

    private Map<Integer, Integer> buildBoardCommunityMap(List<Post> posts) {
        Map<Integer, Integer> m = new HashMap<>();
        posts.stream().map(Post::getBoardId).filter(Objects::nonNull).distinct()
                .forEach(bid -> boardMapper.selectById(bid)
                        .ifPresent(b -> m.put(b.getId(), b.getCommunityId())));
        return m;
    }
}
