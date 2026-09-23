package com.zuel.springtest.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zuel.springtest.common.BusinessException;
import com.zuel.springtest.common.ResultCode;
import com.zuel.springtest.config.EmbeddingProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Embedding 服务
 *
 * <p>DeepSeek 不提供向量化能力，这里对接国内厂商 embedding 接口（智谱 glm-embedding / 通义等），
 * 将文本转为稠密向量，供 RAG 检索使用。配置见 {@link EmbeddingProperties}。
 */
@Slf4j
@Service
public class EmbeddingService {

    private final EmbeddingProperties properties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EmbeddingService(EmbeddingProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public boolean isConfigured() {
        return properties.getApi() != null && StringUtils.hasText(properties.getApi().getKey());
    }

    /**
     * 将单条文本转为向量
     *
     * @throws BusinessException 未配置或调用失败时抛出（由上层决定降级策略）
     */
    public float[] embed(String text) {
        if (!isConfigured()) {
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "Embedding 服务未配置");
        }
        EmbeddingProperties.Api api = properties.getApi();
        try {
            String body = objectMapper.writeValueAsString(
                    Map.of("model", api.getModel(), "input", text));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(api.getUrl()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + api.getKey())
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("Embedding 返回错误，状态码：{}，响应：{}", response.statusCode(), response.body());
                throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "Embedding 服务异常");
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode data = root.get("data");
            if (data == null || data.isEmpty()) {
                throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "Embedding 返回为空");
            }
            JsonNode embedding = data.get(0).get("embedding");
            float[] vec = new float[embedding.size()];
            for (int i = 0; i < embedding.size(); i++) {
                vec[i] = (float) embedding.get(i).asDouble();
            }
            return vec;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用 Embedding 失败", e);
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "Embedding 调用失败");
        }
    }

    /** 批量向量化（逐条调用，简单稳妥；数据量小可接受） */
    public List<float[]> embedBatch(List<String> texts) {
        return texts.stream().map(this::embed).toList();
    }
}
