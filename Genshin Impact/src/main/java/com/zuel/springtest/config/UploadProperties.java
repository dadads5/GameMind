package com.zuel.springtest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件上传配置：{@code app.upload.*}
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {

    /** 本地存储目录 */
    private String dir = "./uploads";

    /** 对外访问 URL 前缀 */
    private String urlPrefix = "/uploads/";

    /**
     * 前端构建产物（Vue dist）所在目录。
     * <p>当希望由本后端直接托管前端时，把 Vue 的 {@code npm run build} 产物放至此目录
     * （或保留默认值 {@code ../community/dist}），后端会把 /PFP、/heroes、/background、
     * /huo 等静态资源映射到此处。留空则不托管前端资源，避免与 src/main/resources/static 下已有的页面冲突。
     */
    private String frontendDir = "../community/dist";
}
