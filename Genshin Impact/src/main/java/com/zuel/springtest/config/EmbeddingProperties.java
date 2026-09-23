package com.zuel.springtest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Embedding 配置：{@code embedding.*}
 *
 * <p>DeepSeek 只提供对话能力、不提供向量化，RAG 需要一个独立的 Embedding 服务。
 * 这里对接国内大模型厂商的 embedding 接口（如智谱 glm-embedding / 通义 text-embedding）。
 * API Key 通过环境变量注入，不写死在仓库里。
 */
@Data
@Component
@ConfigurationProperties(prefix = "embedding")
public class EmbeddingProperties {

    private Api api = new Api();

    @Data
    public static class Api {
        /** embedding 接口地址（硅基流动 SiliconFlow OpenAI 兼容模式） */
        private String url = "https://api.siliconflow.cn/v1/embeddings";
        /** API Key，务必通过环境变量 EMBEDDING_API_KEY 注入 */
        private String key;
        /** 模型名称（BAAI/bge-m3，1024 维检索专用；或 Qwen/Qwen3-Embedding-0.6B） */
        private String model = "BAAI/bge-m3";
        /** 向量维度，默认 1024（bge-m3 / Qwen3-Embedding 默认输出维度） */
        private int dimensions = 1024;
    }
}
