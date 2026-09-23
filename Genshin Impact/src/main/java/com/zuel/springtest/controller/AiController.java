package com.zuel.springtest.controller;

import com.zuel.springtest.common.Result;
import com.zuel.springtest.dto.ai.AskRequest;
import com.zuel.springtest.dto.ai.ChatMessage;
import com.zuel.springtest.security.CurrentUser;
import com.zuel.springtest.security.LoginUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zuel.springtest.dto.ai.SourceChunk;
import com.zuel.springtest.entity.AiConversation;
import com.zuel.springtest.entity.AiMessage;
import com.zuel.springtest.services.AiConversationService;
import com.zuel.springtest.services.DeepSeekService;
import com.zuel.springtest.services.KnowledgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI 问答接口
 *
 * <p>需要登录后才可使用，避免 API 额度被滥用。
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final DeepSeekService deepSeekService;
    private final StringRedisTemplate stringRedis;
    private final AiConversationService aiConversationService;
    private final KnowledgeService knowledgeService;

    private static final ObjectMapper OBJ_MAPPER = new ObjectMapper();

    /**
     * 流式请求专用线程池：固定上限 + 有界队列
     *
     * <p>原先使用无界的 newCachedThreadPool，大量并发长连接会无上限地创建线程，
     * 这里改为有界线程池，配合下面的并发信号量使用。
     */
    private static final Executor SSE_EXECUTOR = new ThreadPoolExecutor(
            4,
            16,
            60L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(64),
            new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger();

                @Override
                public Thread newThread(Runnable runnable) {
                    Thread t = new Thread(runnable, "sse-ai-" + counter.incrementAndGet());
                    t.setDaemon(true);
                    return t;
                }
            },
            new ThreadPoolExecutor.AbortPolicy());

    /**
     * 全局共享的心跳调度器
     *
     * <p>原先每个流式请求都会新建一个单线程调度器，线程数随并发连接数线性增长；
     * 改为全局共享一个，各连接在结束时取消自己的心跳任务。
     */
    private static final ScheduledExecutorService HEARTBEAT_EXECUTOR =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "sse-heartbeat");
                t.setDaemon(true);
                return t;
            });

    /** 并发 SSE 连接上限：超出直接拒绝，保护后端不被长连接拖垮 */
    private static final Semaphore SSE_CONCURRENCY = new Semaphore(32);

    /** 单个 SSE 连接的最长存活时间（兜底防泄漏，心跳不为其续期） */
    private static final long SSE_TIMEOUT_MS = 5 * 60 * 1000L;

    /**
     * 提问（支持多轮对话历史）
     */
    @PostMapping("/ask")
    public Result<String> ask(@Valid @RequestBody AskRequest request,
                              @CurrentUser LoginUser loginUser) {
        if (!allowAiCall(loginUser.getId())) {
            return Result.fail("AI 调用过于频繁，请 1 分钟后再试");
        }
        boolean persist = shouldPersist(request);
        Long conversationId = null;
        List<ChatMessage> history = request.getHistory() == null ? List.of() : request.getHistory();
        if (persist) {
            conversationId = aiConversationService.resolve(
                    request.getConversationId(), loginUser.getId(), request.getQuestion());
            // 先取历史（此时本次提问尚未落库），再保存提问，避免与下方 question 在上下文中重复
            history = resolveHistory(request, conversationId, loginUser.getId());
            aiConversationService.saveMessage(conversationId, "user", request.getQuestion());
        }
        String answer = deepSeekService.ask(request.getQuestion(), history);
        if (persist) {
            aiConversationService.saveMessage(conversationId, "assistant", answer);
        }
        return Result.success(answer);
    }

    /**
     * 流式提问（SSE）
     *
     * <p>以 {@code text/event-stream} 逐段返回模型输出：每行 {@code data: "<片段>"}，
     * 结束时发送 {@code event:done}，出错时发送 {@code event:error}。
     * 在服务端另起线程调用 DeepSeek，避免阻塞请求线程。
     */
    @PostMapping(value = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter askStream(@Valid @RequestBody AskRequest request,
                                @CurrentUser LoginUser loginUser) {
        if (!allowAiCall(loginUser.getId())) {
            return errorEmitter("AI 调用过于频繁，请 1 分钟后再试");
        }
        // 并发保护：超出上限直接拒绝，避免长连接堆积耗尽线程
        if (!SSE_CONCURRENCY.tryAcquire()) {
            return errorEmitter("当前使用人数较多，请稍后再试");
        }

        // 会话解析：未传 id 时按首条提问自动新建，并把 id 回传给前端
        boolean persist = shouldPersist(request);
        Long conversationId = null;
        List<ChatMessage> history = request.getHistory() == null ? List.of() : request.getHistory();
        // RAG 检索：先查出与问题最相关的站内攻略，作为参考素材并随答案回传前端溯源
        List<SourceChunk> sources = knowledgeService.retrieve(request.getQuestion());
        if (persist) {
            conversationId = aiConversationService.resolve(
                    request.getConversationId(), loginUser.getId(), request.getQuestion());
            // 先取历史（此时本次提问尚未落库），再保存提问，避免与下方 question 在上下文中重复
            history = resolveHistory(request, conversationId, loginUser.getId());
            aiConversationService.saveMessage(conversationId, "user", request.getQuestion());
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        // 首个事件：把本次会话 id 告知前端，后续多轮可直接沿用
        if (persist) {
            try {
                emitter.send(SseEmitter.event().name("conversation").data(String.valueOf(conversationId)));
            } catch (IOException ignored) {
                // 客户端已断开，后续事件自然失败，由 cleanup 统一收尾
            }
        }

        // RAG 来源事件：检索到的站内攻略，供前端「可溯源」展示（失败不影响主回答）
        if (!sources.isEmpty()) {
            try {
                String srcJson = OBJ_MAPPER.writeValueAsString(
                        sources.stream()
                                .map(s -> {
                                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                                    m.put("postId", s.getPostId());
                                    m.put("title", s.getTitle() == null ? "" : s.getTitle());
                                    m.put("communityId", s.getCommunityId());
                                    return m;
                                })
                                .toList());
                emitter.send(SseEmitter.event().name("sources").data(srcJson));
            } catch (IOException ignored) {
                // 来源展示失败不影响主回答
            }
        }

        // 心跳：每 15s 发一条 SSE 注释行，防止中间代理（nginx 等）因空闲断开连接
        ScheduledFuture<?> heartbeatFuture = HEARTBEAT_EXECUTOR.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().comment("keep-alive"));
            } catch (IOException ignored) {
                // 客户端已断开，由 cleanup 统一收尾
            }
        }, 15, 15, TimeUnit.SECONDS);

        // 连接结束（完成 / 超时 / 出错）时统一释放资源，AtomicBoolean 保证只执行一次
        AtomicBoolean cleaned = new AtomicBoolean(false);
        Runnable cleanup = () -> {
            if (cleaned.compareAndSet(false, true)) {
                heartbeatFuture.cancel(true);
                SSE_CONCURRENCY.release();
            }
        };
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        // 累积完整答案，流结束（含客户端中断）后落库，保证多轮上下文不丢
        StringBuilder answerBuffer = new StringBuilder();
        // lambda 内部只能引用 effectively final 的变量，这里固化其最终值
        final List<ChatMessage> finalHistory = history;
        final List<SourceChunk> finalSources = sources;
        final Long finalConversationId = conversationId;
        final boolean finalPersist = persist;
        CompletableFuture.runAsync(() -> {
            try {
                deepSeekService.streamAsk(
                        request.getQuestion(),
                        finalHistory,
                        finalSources,
                        token -> {
                            answerBuffer.append(token);
                            try {
                                emitter.send(SseEmitter.event().name("token").data(token));
                            } catch (Exception ignored) {
                                // 客户端已断开，忽略
                            }
                        },
                        () -> {
                            try {
                                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                            } catch (Exception ignored) {
                                // 忽略
                            }
                            emitter.complete();
                        });
            } catch (Exception e) {
                // 内部异常明细只记服务端日志，前端统一收到脱敏文案
                log.error("AI 流式问答失败 userId={}", loginUser.getId(), e);
                try {
                    emitter.send(SseEmitter.event().name("error").data("AI 服务异常，请稍后重试"));
                } catch (Exception ignored) {
                    // 忽略
                }
                emitter.complete();
            } finally {
                // 客户端主动中断时也保存已生成内容，保证会话上下文完整
                if (finalPersist && answerBuffer.length() > 0) {
                    aiConversationService.saveMessage(finalConversationId, "assistant", answerBuffer.toString());
                }
                cleanup.run();
            }
        }, SSE_EXECUTOR);
        return emitter;
    }

    /** 返回一个立即结束、只携带错误提示的 SSE 通道 */
    private SseEmitter errorEmitter(String message) {
        SseEmitter emitter = new SseEmitter(0L);
        try {
            emitter.send(SseEmitter.event().name("error").data(message));
        } catch (IOException ignored) {
            // 忽略
        }
        emitter.complete();
        return emitter;
    }

    /**
     * 健康检查，不泄露任何密钥信息
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        return Result.success(Map.of(
                "status", "OK",
                "aiConfigured", deepSeekService.isConfigured()
        ));
    }

    // ------------------------------------------------------------------
    // 会话持久化
    // ------------------------------------------------------------------

    /** 当前用户的会话列表（按最近更新排序） */
    @GetMapping("/conversations")
    public Result<List<AiConversation>> conversations(@CurrentUser LoginUser loginUser) {
        return Result.success(aiConversationService.listByUser(loginUser.getId()));
    }

    /** 新建空会话（通常无需调用：提问时不传 conversationId 会自动创建） */
    @PostMapping("/conversations")
    public Result<Map<String, Object>> createConversation(@CurrentUser LoginUser loginUser) {
        Long id = aiConversationService.create(loginUser.getId(), null);
        return Result.success(Map.of("conversationId", id));
    }

    /** 某会话的历史消息（带归属校验） */
    @GetMapping("/conversations/{id}/messages")
    public Result<List<AiMessage>> conversationMessages(@PathVariable Long id,
                                                        @CurrentUser LoginUser loginUser) {
        return Result.success(aiConversationService.listMessages(id, loginUser.getId()));
    }

    /** 删除会话及其消息（带归属校验） */
    @DeleteMapping("/conversations/{id}")
    public Result<Void> deleteConversation(@PathVariable Long id, @CurrentUser LoginUser loginUser) {
        aiConversationService.delete(id, loginUser.getId());
        return Result.success();
    }

    /**
     * 是否持久化到会话：默认 true；润写 / 智能回复等一次性辅助传 false。
     */
    private boolean shouldPersist(AskRequest request) {
        return request.getPersist() == null || Boolean.TRUE.equals(request.getPersist());
    }

    /**
     * 解析本次请求使用的对话历史：请求自带 history 优先，
     * 否则从服务端会话读取（前端因此可不再携带 20 条历史）。
     */
    private List<ChatMessage> resolveHistory(AskRequest request, Long conversationId, Long userId) {
        if (request.getHistory() != null && !request.getHistory().isEmpty()) {
            return request.getHistory();
        }
        List<AiMessage> stored = aiConversationService.context(conversationId, userId);
        if (stored.isEmpty()) {
            return List.of();
        }
        return stored.stream()
                .map(m -> new ChatMessage(m.getRole(), m.getContent()))
                .toList();
    }

    /**
     * AI 接口限流：每用户每分钟最多 10 次调用。Redis 不可用时放行。
     */
    private boolean allowAiCall(Long userId) {
        String key = "ratelimit:ai:" + userId;
        try {
            Long n = stringRedis.opsForValue().increment(key);
            if (n != null && n == 1) {
                stringRedis.expire(key, Duration.ofMinutes(1));
            }
            return n != null && n <= 10;
        } catch (Exception e) {
            log.warn("AI 限流检查失败，放行 userId={}", userId, e);
            return true;
        }
    }
}
