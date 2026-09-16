package com.zuel.springtest.services;

import com.zuel.springtest.common.BusinessException;
import com.zuel.springtest.common.ResultCode;
import com.zuel.springtest.dto.auth.ChangePasswordRequest;
import com.zuel.springtest.dto.auth.LoginRequest;
import com.zuel.springtest.dto.auth.RegisterRequest;
import com.zuel.springtest.dto.auth.UpdateProfileRequest;
import com.zuel.springtest.entity.User;
import com.zuel.springtest.utils.JwtUtils;
import com.zuel.springtest.vo.AuthVO;
import com.zuel.springtest.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * 认证业务：注册、登录、资料维护
 *
 * <p>Token 签发集中在此处，Controller 只负责参数校验与结果封装。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    /** 勾选「记住我」后的 Token 有效期：7 天 */
    private static final long REMEMBER_ME_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;

    /** 用户当前有效 Token（单设备登录）：value=token 字符串，TTL 跟随 token 有效期 */
    private static final String USER_TOKEN_KEY = "user:token:";
    /** 在线用户 ZSet：member=userId，score=最近活跃时间戳（毫秒） */
    private static final String ONLINE_KEY = "online:users";
    /** 离线判定阈值：超过 5 分钟无请求视为离线 */
    private static final long ONLINE_TIMEOUT_MS = 5 * 60 * 1000L;

    /** 登录失败计数 key 前缀 */
    private static final String LOGIN_FAIL_KEY = "login:fail:";
    /** 登录失败次数上限，达到后暂时锁定 */
    private static final int LOGIN_FAIL_LIMIT = 5;
    /** 登录失败计数窗口 */
    private static final Duration LOGIN_FAIL_WINDOW = Duration.ofMinutes(15);

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate stringRedis;

    /**
     * 注册并直接签发 Token
     */
    @Transactional
    public AuthVO register(RegisterRequest request) {
        User user = userService.register(request.getUsername(), request.getPassword(), request.getEmail());
        return buildAndStore(user, jwtUtils.getExpirationTime());
    }

    /**
     * 登录
     *
     * <p>带失败次数限制：同一用户名在 15 分钟内连续失败 5 次后暂时锁定，
     * 防止对登录接口做暴力破解 / 撞库。
     */
    public AuthVO login(LoginRequest request) {
        String username = request.getUsername();
        assertNotLocked(username);

        try {
            User user = userService.authenticate(username, request.getPassword());
            clearLoginFailCount(username);
            long expiration = Boolean.TRUE.equals(request.getRememberMe())
                    ? REMEMBER_ME_EXPIRATION
                    : jwtUtils.getExpirationTime();
            return buildAndStore(user, expiration);
        } catch (BusinessException e) {
            // 仅「认证失败」（用户不存在 / 密码错误）计入失败次数，其他业务异常不计数
            recordLoginFail(username);
            throw e;
        }
    }

    /**
     * 登录失败限流检查：达到上限时抛出 429
     *
     * <p>Redis 不可用时降级为不限流，保证登录功能本身可用，仅记录告警。
     */
    private void assertNotLocked(String username) {
        try {
            String value = stringRedis.opsForValue().get(LOGIN_FAIL_KEY + username);
            if (value != null && Integer.parseInt(value) >= LOGIN_FAIL_LIMIT) {
                throw new BusinessException(ResultCode.TOO_MANY_REQUESTS,
                        "登录失败次数过多，请 15 分钟后再试");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("登录限流检查失败，降级放行 user={}", username, e);
        }
    }

    /** 累加登录失败次数，首次失败时设置计数窗口 TTL */
    private void recordLoginFail(String username) {
        try {
            String key = LOGIN_FAIL_KEY + username;
            Long count = stringRedis.opsForValue().increment(key);
            if (count != null && count == 1) {
                stringRedis.expire(key, LOGIN_FAIL_WINDOW);
            }
        } catch (Exception e) {
            log.warn("记录登录失败次数异常 user={}", username, e);
        }
    }

    /** 登录成功后清零失败计数 */
    private void clearLoginFailCount(String username) {
        try {
            stringRedis.delete(LOGIN_FAIL_KEY + username);
        } catch (Exception e) {
            log.warn("清理登录失败次数异常 user={}", username, e);
        }
    }

    /**
     * 获取当前用户资料
     */
    public UserVO getProfile(Long userId) {
        return UserVO.from(userService.getById(userId));
    }

    /**
     * 修改资料
     */
    @Transactional
    public UserVO updateProfile(Long userId, UpdateProfileRequest request) {
        return UserVO.from(userService.updateProfile(userId, request));
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
    }

    private AuthVO buildAndStore(User user, long expirationMs) {
        String token = jwtUtils.generateToken(user.getUsername(), expirationMs);
        storeUserToken(user.getId(), token, expirationMs);
        recordOnline(user.getId());
        return AuthVO.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expirationMs / 1000)
                .user(UserVO.from(user))
                .build();
    }

    /**
     * 单设备登录：记录用户当前有效 Token，旧 Token 因不相等而自动失效
     */
    private void storeUserToken(Long userId, String token, long expirationMs) {
        try {
            stringRedis.opsForValue().set(USER_TOKEN_KEY + userId, token, Duration.ofMillis(expirationMs));
        } catch (Exception e) {
            log.warn("记录用户 Token 失败 user={}", userId, e);
        }
    }

    /**
     * 登录即标记为在线
     */
    private void recordOnline(Long userId) {
        try {
            stringRedis.opsForZSet().add(ONLINE_KEY, userId.toString(), System.currentTimeMillis());
        } catch (Exception e) {
            log.warn("记录在线状态失败 user={}", userId, e);
        }
    }

    /**
     * 退出登录：删除当前有效 Token，使该用户所有 Token 立即失效
     */
    public void logout(Long userId) {
        try {
            stringRedis.delete(USER_TOKEN_KEY + userId);
        } catch (Exception e) {
            log.warn("退出登录清理 Token 失败 user={}", userId, e);
        }
    }

    /**
     * 每分钟清理离线用户（超过阈值无活跃心跳）
     */
    @Scheduled(fixedDelay = 60 * 1000)
    public void cleanOffline() {
        try {
            long threshold = System.currentTimeMillis() - ONLINE_TIMEOUT_MS;
            stringRedis.opsForZSet().removeRangeByScore(ONLINE_KEY, 0, threshold);
        } catch (Exception e) {
            log.warn("清理离线用户失败", e);
        }
    }
}
