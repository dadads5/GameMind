package com.zuel.springtest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 接口访问配置：{@code app.security.*}
 *
 * <p>{@code whitelist} 支持两种写法：
 * <ul>
 *   <li>{@code /api/posts/**} —— 任意请求方法均放行</li>
 *   <li>{@code GET:/api/posts/**} —— 仅指定请求方法放行</li>
 * </ul>
 * 未命中白名单的接口一律要求携带有效 Token。
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private List<String> whitelist = new ArrayList<>();
}
