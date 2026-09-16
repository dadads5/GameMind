package com.zuel.springtest.config;

import com.zuel.springtest.security.CurrentUserArgumentResolver;
import com.zuel.springtest.security.JwtAuthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Spring MVC 全局配置：CORS、JWT 拦截器、参数解析器、上传目录静态映射
 *
 * <p>CORS 在此处统一配置，各 Controller 不再使用 {@code @CrossOrigin}。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthenticationInterceptor jwtAuthenticationInterceptor;
    private final UploadProperties uploadProperties;

    /**
     * 允许跨域的来源，逗号分隔。
     *
     * <p>未配置（或仍为 "*"）时不再放行任意来源，而是回退到本机开发端口白名单，
     * 避免生产环境出现「allowedOriginPatterns("*") + allowCredentials(true)」这一可被任意站点利用的组合。
     */
    @Value("${app.cors.allowed-origins:}")
    private String corsAllowedOrigins;

    /** 未显式配置来源时允许的本地开发地址 */
    private static final List<String> LOCAL_DEV_ORIGINS = List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "http://localhost:5174",
            "http://127.0.0.1:5174",
            "http://localhost:8080",
            "http://127.0.0.1:8080");

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        CorsRegistration reg = registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600);

        String configured = corsAllowedOrigins == null ? "" : corsAllowedOrigins.trim();
        if (configured.isEmpty() || "*".equals(configured)) {
            log.warn("app.cors.allowed-origins 未配置，已回退到仅允许本机开发来源 {}；"
                    + "生产环境必须显式配置为前端域名（逗号分隔）", LOCAL_DEV_ORIGINS);
            reg.allowedOrigins(LOCAL_DEV_ORIGINS.toArray(String[]::new));
        } else {
            reg.allowedOrigins(Arrays.stream(configured.split(","))
                    .map(String::trim)
                    .filter(origin -> !origin.isEmpty())
                    .toArray(String[]::new));
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/api/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentUserArgumentResolver());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String prefix = uploadProperties.getUrlPrefix();
        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        Path dir = Paths.get(uploadProperties.getDir()).toAbsolutePath().normalize();
        registry.addResourceHandler(prefix + "**")
                .addResourceLocations(dir.toUri().toString());
        log.info("上传目录静态映射：{} -> {}", prefix + "**", dir);

        // 前端构建产物（Vue dist）：当后端直接托管前端时，提供图片等静态资源。
        // 映射到独立的 frontendDir，避免覆盖 src/main/resources/static 下已有的页面。
        String frontendDir = uploadProperties.getFrontendDir();
        if (frontendDir != null && !frontendDir.isBlank()) {
            String loc = Paths.get(frontendDir).toAbsolutePath().normalize().toUri().toString();
            String[] assetPrefixes = {"/PFP", "/heroes", "/background", "/huo"};
            for (String p : assetPrefixes) {
                registry.addResourceHandler(p + "/**")
                        .addResourceLocations(loc + p.substring(1) + "/");
            }
            // 根级图片（导航 logo 等）
            registry.addResourceHandler("/mainlogo.png", "/AI.jpg", "/shalu-logo.jpg",
                            "/yslogo.png", "/wzry-logo.jpg")
                    .addResourceLocations(loc + "/");
            // SPA 入口：后端直接托管构建产物时，根路径与 /assets 返回 dist 的 index.html / 静态资源
            registry.addResourceHandler("/", "/index.html")
                    .addResourceLocations(loc + "/index.html");
            registry.addResourceHandler("/assets/**")
                    .addResourceLocations(loc + "/assets/");
            log.info("前端静态资源映射（/PFP,/heroes,/background,/huo,...） -> {}", loc);
        }
    }
}
