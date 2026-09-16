package com.zuel.springtest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * DeepSeek 配置：{@code deepseek.*}
 *
 * <p>类型安全地读取配置，取代原先手工拼 key 的 DeepSeekConfig。
 */
@Data
@Component
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekProperties {

    private Api api = new Api();
    private Chat chat = new Chat();

    @Data
    public static class Api {
        /** 接口地址 */
        private String url = "https://api.deepseek.com/v1/chat/completions";
        /** API Key，务必通过环境变量 DEEPSEEK_API_KEY 注入 */
        private String key;
        /** 模型名称 */
        private String model = "deepseek-chat";
    }

    @Data
    public static class Chat {
        private String systemPrompt = "你是一个专业的游戏助手，专门回答游戏相关的问题。";
        private int maxTokens = 800;
        private double temperature = 0.7;
    }
}
