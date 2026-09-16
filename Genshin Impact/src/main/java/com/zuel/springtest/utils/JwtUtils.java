package com.zuel.springtest.utils;

import com.zuel.springtest.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 *
 * <p>签名算法为 HS512，要求密钥长度不低于 64 字节；长度不足时在启动阶段即失败，
 * 避免运行期因弱密钥产生的兼容处理。
 */
@Slf4j
@Component
public class JwtUtils {

    /** HS512 要求的最小密钥字节数 */
    private static final int MIN_SECRET_LENGTH = 64;

    private final long jwtExpirationMs;
    private final SecretKey signingKey;

    public JwtUtils(JwtProperties properties) {
        String configuredSecret = properties.getSecret();
        byte[] keyBytes;

        if (configuredSecret == null || configuredSecret.isBlank()) {
            // 未配置密钥时绝不回落到仓库里的固定密钥（那样任何人都能伪造任意用户 Token），
            // 而是生成一次性随机密钥：服务可用，代价是重启后已签发 Token 失效。
            keyBytes = Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded();
            log.warn("==================================================================");
            log.warn("app.jwt.secret 未配置（环境变量 JWT_SECRET 缺失），已生成一次性随机密钥。");
            log.warn("影响：服务重启后所有已签发 Token 将失效，用户需要重新登录。");
            log.warn("生产环境必须通过环境变量 JWT_SECRET 配置固定密钥（>= {} 字节）。", MIN_SECRET_LENGTH);
            log.warn("==================================================================");
        } else {
            keyBytes = configuredSecret.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length < MIN_SECRET_LENGTH) {
                throw new IllegalArgumentException(
                        "app.jwt.secret 长度不足：HS512 要求至少 " + MIN_SECRET_LENGTH
                                + " 字节，当前为 " + keyBytes.length + " 字节");
            }
        }

        this.jwtExpirationMs = properties.getExpiration();
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT 初始化完成，Token 有效期：{} 毫秒", jwtExpirationMs);
    }

    /**
     * 生成 Token，使用配置文件中的默认有效期
     */
    public String generateToken(String username) {
        return generateToken(username, jwtExpirationMs);
    }

    /**
     * 生成 Token，使用自定义有效期
     *
     * @param expirationTime 有效期（毫秒）
     */
    public String generateToken(String username, long expirationTime) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationTime))
                .signWith(signingKey)
                .compact();
    }

    /**
     * 从 Token 中解析用户名
     */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 校验 Token 是否有效（签名正确且未过期）
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 判断 Token 是否即将过期
     *
     * @param minutes 剩余时间阈值（分钟）
     */
    public boolean isTokenExpiringSoon(String token, int minutes) {
        try {
            Date expiration = parseClaims(token).getExpiration();
            long timeLeft = expiration.getTime() - System.currentTimeMillis();
            return timeLeft <= (long) minutes * 60 * 1000;
        } catch (JwtException | IllegalArgumentException e) {
            // 解析失败一律视为已过期
            return true;
        }
    }

    /**
     * 配置中的默认有效期（毫秒）
     */
    public long getExpirationTime() {
        return jwtExpirationMs;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
