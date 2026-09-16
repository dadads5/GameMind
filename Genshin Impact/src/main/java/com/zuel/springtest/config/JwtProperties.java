package com.zuel.springtest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置：{@code app.jwt.*}
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** 签名密钥，HS512 要求长度 >= 64 字节 */
    private String secret;

    /** Token 有效期（毫秒） */
    private long expiration = 86_400_000L;
}
