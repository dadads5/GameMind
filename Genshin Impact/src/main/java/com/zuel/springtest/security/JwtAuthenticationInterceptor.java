package com.zuel.springtest.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zuel.springtest.common.Result;
import com.zuel.springtest.common.ResultCode;
import com.zuel.springtest.config.SecurityProperties;
import com.zuel.springtest.entity.User;
import com.zuel.springtest.services.UserService;
import com.zuel.springtest.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * JWT 认证拦截器
 *
 * <p>职责：
 * <ol>
 *   <li>解析 {@code Authorization: Bearer xxx} 得到 {@link LoginUser}，写入请求属性</li>
 *   <li>对非白名单接口校验登录状态，未登录直接返回 401 JSON</li>
 * </ol>
 *
 * <p>业务代码只需在方法参数上声明 {@code @CurrentUser LoginUser user} 即可拿到当前用户，
 * 不再重复解析 Token，也杜绝了前端伪造 userId 的可能。
 */
@Slf4j
@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    /** 用户当前有效 Token（单设备登录） */
    private static final String USER_TOKEN_KEY = "user:token:";
    /** 在线用户 ZSet */
    private static final String ONLINE_KEY = "online:users";

    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final SecurityProperties securityProperties;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedis;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 在线状态刷新线程池
     *
     * <p>在线人数属于统计类数据，允许少量丢失。若同步刷新，Redis 不可用时每个请求都会阻塞到连接超时，
     * 几十个并发就足以占满 Tomcat 线程池导致整站不可用；因此这里异步执行，并在队列满时直接丢弃。
     */
    private static final ExecutorService ONLINE_EXECUTOR = new ThreadPoolExecutor(
            1,
            2,
            60L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(2048),
            new ThreadFactory() {
                private final AtomicInteger seq = new AtomicInteger();

                @Override
                public Thread newThread(Runnable runnable) {
                    Thread thread = new Thread(runnable, "online-refresh-" + seq.incrementAndGet());
                    thread.setDaemon(true);
                    return thread;
                }
            },
            // 队列满时丢弃任务：宁可少统计一次在线，也不能拖慢业务请求
            new ThreadPoolExecutor.DiscardPolicy());

    public JwtAuthenticationInterceptor(JwtUtils jwtUtils,
                                        UserService userService,
                                        SecurityProperties securityProperties,
                                        ObjectMapper objectMapper,
                                        StringRedisTemplate stringRedis) {
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.securityProperties = securityProperties;
        this.objectMapper = objectMapper;
        this.stringRedis = stringRedis;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // 非 Controller 方法（静态资源等）直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        // 预检请求直接放行，由 CORS 配置处理
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String path = resolvePath(request);
        LoginUser loginUser = resolveLoginUser(request);

        // 已登录用户刷新在线状态（异步执行：Redis 异常时不应阻塞业务请求）
        if (loginUser != null) {
            refreshOnlineAsync(loginUser.getId());
        }

        // 白名单接口：登录了就注入用户，没登录也放行
        if (isWhitelisted(request.getMethod(), path)) {
            if (loginUser != null) {
                request.setAttribute(CurrentUserArgumentResolver.CURRENT_USER_ATTRIBUTE, loginUser);
            }
            return true;
        }

        // 非白名单：必须登录
        if (loginUser == null) {
            log.debug("未登录或 Token 无效，拦截请求：{} {}", request.getMethod(), path);
            writeUnauthorized(response);
            return false;
        }

        request.setAttribute(CurrentUserArgumentResolver.CURRENT_USER_ATTRIBUTE, loginUser);
        return true;
    }

    /**
     * 从请求头解析 Token 并查询用户
     *
     * @return 当前登录用户，解析失败或用户不存在时返回 null
     */
    private LoginUser resolveLoginUser(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        try {
            if (!jwtUtils.validateToken(token)) {
                return null;
            }
            String username = jwtUtils.getUsernameFromToken(token);
            Optional<User> userOptional = userService.findByUsername(username);
            if (userOptional.isEmpty()) {
                return null;
            }
            User user = userOptional.get();
            // 单设备登录：当前 token 必须仍是用户的有效 token
            if (!isTokenStillValid(user.getId(), token)) {
                return null;
            }
            return LoginUser.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .role(user.getRole())
                    .build();
        } catch (Exception e) {
            log.debug("解析 Token 失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 判断请求是否命中白名单
     *
     * @param method 请求方法，如 GET
     * @param path   去掉 context-path 后的请求路径
     */
    private boolean isWhitelisted(String method, String path) {
        if (securityProperties.getWhitelist() == null) {
            return false;
        }
        for (String item : securityProperties.getWhitelist()) {
            if (item == null || item.isBlank()) {
                continue;
            }
            String pattern = item.trim();
            int separatorIndex = pattern.indexOf(':');
            if (separatorIndex > 0) {
                // 形如 GET:/api/posts/**
                String patternMethod = pattern.substring(0, separatorIndex).trim();
                String patternPath = pattern.substring(separatorIndex + 1).trim();
                if (!patternMethod.equalsIgnoreCase(method)) {
                    continue;
                }
                if (pathMatcher.match(patternPath, path)) {
                    return true;
                }
            } else if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 单设备登录：校验当前 token 是否为用户最新签发的有效 token
     *
     * <p>Redis 中无记录（老 token / 服务重启前签发）视为有效，保证向后兼容；
     * Redis 异常时放行，不阻断主流程。
     */
    private boolean isTokenStillValid(Long userId, String token) {
        try {
            String valid = stringRedis.opsForValue().get(USER_TOKEN_KEY + userId);
            if (valid == null) {
                return true;
            }
            return valid.equals(token);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 异步刷新用户在线时间戳；队列满时丢弃任务，绝不阻塞请求线程
     */
    private void refreshOnlineAsync(Long userId) {
        try {
            ONLINE_EXECUTOR.execute(() -> refreshOnline(userId));
        } catch (RejectedExecutionException ignored) {
            // 队列已满：丢弃本次在线状态刷新
        }
    }

    /** 刷新用户在线时间戳 */
    private void refreshOnline(Long userId) {
        try {
            stringRedis.opsForZSet().add(ONLINE_KEY, userId.toString(), System.currentTimeMillis());
        } catch (Exception e) {
            // 忽略在线状态写入失败
        }
    }

    private String resolvePath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        return path;
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(ResultCode.UNAUTHORIZED)));
    }
}
