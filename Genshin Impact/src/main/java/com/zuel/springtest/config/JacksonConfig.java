package com.zuel.springtest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 显式声明 Jackson 2 的 {@link ObjectMapper} Bean。
 *
 * <p>Spring Boot 4 默认注入的是 Jackson 3（{@code tools.jackson.databind.ObjectMapper}），
 * 部分代码（如 {@code JwtAuthenticationInterceptor}）仍依赖 Jackson 2 命名空间下的
 * {@code com.fasterxml.jackson.databind.ObjectMapper}，此处手动提供以满足注入。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }
}
