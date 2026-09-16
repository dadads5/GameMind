package com.zuel.springtest.controller;

import com.zuel.springtest.common.Result;
import com.zuel.springtest.dto.ai.AskRequest;
import com.zuel.springtest.security.CurrentUser;
import com.zuel.springtest.security.LoginUser;
import com.zuel.springtest.services.DeepSeekService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
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
        String answer = deepSeekService.ask(request.getQuestion(), request.getHistory());
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

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

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

        CompletableFuture.runAsync(() -> {
            try {
                deepSeekService.streamAsk(
                        request.getQuestion(),
                        request.getHistory(),
                        token -> {
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
