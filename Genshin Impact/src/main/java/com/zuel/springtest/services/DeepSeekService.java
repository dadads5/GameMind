package com.zuel.springtest.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zuel.springtest.common.BusinessException;
import com.zuel.springtest.common.ResultCode;
import com.zuel.springtest.config.DeepSeekProperties;
import com.zuel.springtest.dto.ai.ChatMessage;
import com.zuel.springtest.dto.ai.SourceChunk;
import com.zuel.springtest.dto.deepseek.ChatCompletionRequest;
import com.zuel.springtest.dto.deepseek.ChatCompletionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * DeepSeek 对话服务（含 RAG 检索增强）
 *
 * <p>配置由 {@link DeepSeekProperties} 类型安全绑定，API Key 通过环境变量注入。
 * 每次问答前会先经 {@link KnowledgeService} 检索站内相关攻略，作为参考素材注入 prompt，
 * 实现「基于站内真实内容回答 + 可溯源」。Embedding 未配置时自动降级为纯模型知识。
 */
@Slf4j
@Service
public class DeepSeekService {

    private static final String DEFAULT_SYSTEM_PROMPT = """
            你是一个专业的游戏助手，专门回答游戏相关的问题。
            请用简洁、专业、友好的方式回答用户的问题。
            如果问题与游戏无关，请礼貌地引导用户回到游戏话题。
            """;

    private final DeepSeekProperties properties;
    private final KnowledgeService knowledgeService;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DeepSeekService(DeepSeekProperties properties, KnowledgeService knowledgeService) {
        this.properties = properties;
        this.knowledgeService = knowledgeService;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * AI 是否已正确配置（用于健康检查）
     */
    public boolean isConfigured() {
        return properties.getApi() != null && StringUtils.hasText(properties.getApi().getKey());
    }

    // ------------------------------------------------------------------
    // 同步问答
    // ------------------------------------------------------------------

    public String ask(String question, List<ChatMessage> history) {
        return ask(question, history, knowledgeService.retrieve(question));
    }

    public String ask(String question, List<ChatMessage> history, List<SourceChunk> sources) {
        if (!isConfigured()) {
            log.warn("DeepSeek API Key 未配置，请在环境变量 DEEPSEEK_API_KEY 中设置");
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "AI 服务未配置，请联系管理员");
        }
        ChatCompletionRequest request = buildRequest(question, history, sources, false);
        try {
            String requestBody = objectMapper.writeValueAsString(request);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getApi().getUrl()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + properties.getApi().getKey())
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("DeepSeek 返回错误，状态码：{}，响应：{}", response.statusCode(), response.body());
                throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "AI 服务暂时不可用，请稍后重试");
            }
            ChatCompletionResponse completion = objectMapper.readValue(response.body(), ChatCompletionResponse.class);
            if (completion.getChoices() == null || completion.getChoices().isEmpty()
                    || completion.getChoices().get(0).getMessage() == null) {
                log.error("DeepSeek 响应格式异常：{}", response.body());
                throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "AI 返回内容为空，请重试");
            }
            String answer = completion.getChoices().get(0).getMessage().getContent();
            log.debug("DeepSeek 回答长度：{} 字符", answer == null ? 0 : answer.length());
            return answer;
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.error("调用 DeepSeek API 发生网络错误", e);
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "AI 服务连接失败，请稍后重试");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("调用 DeepSeek API 被中断", e);
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "AI 服务请求超时，请稍后重试");
        } catch (Exception e) {
            log.error("调用 DeepSeek API 发生未知错误", e);
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "AI 服务异常，请稍后重试");
        }
    }

    // ------------------------------------------------------------------
    // 流式问答（SSE）
    // ------------------------------------------------------------------

    public void streamAsk(String question, List<ChatMessage> history,
                           Consumer<String> onToken, Runnable onComplete) {
        streamAsk(question, history, knowledgeService.retrieve(question), onToken, onComplete);
    }

    public void streamAsk(String question, List<ChatMessage> history, List<SourceChunk> sources,
                          Consumer<String> onToken, Runnable onComplete) {
        if (!isConfigured()) {
            log.warn("DeepSeek API Key 未配置，请在环境变量 DEEPSEEK_API_KEY 中设置");
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "AI 服务未配置，请联系管理员");
        }
        ChatCompletionRequest request = buildRequest(question, history, sources, true);
        try {
            String requestBody = objectMapper.writeValueAsString(request);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getApi().getUrl()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + properties.getApi().getKey())
                    .timeout(Duration.ofSeconds(120))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // ofLines 让响应体按行流式到达，避免一次性缓冲
            HttpResponse<Stream<String>> response =
                    httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofLines());

            if (response.statusCode() != 200) {
                StringBuilder err = new StringBuilder();
                response.body().limit(5).forEach(err::append);
                log.error("DeepSeek 流式返回错误，状态码：{}，响应：{}", response.statusCode(), err);
                throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "AI 服务暂时不可用，请稍后重试");
            }

            response.body().forEach(line -> {
                if (line == null || line.isBlank()) {
                    return;
                }
                String data = line.startsWith("data:") ? line.substring(5).trim() : line.trim();
                if (data.isEmpty() || data.equals("[DONE]")) {
                    return;
                }
                try {
                    JsonNode node = objectMapper.readTree(data);
                    JsonNode choices = node.get("choices");
                    if (choices == null || choices.isEmpty()) {
                        return;
                    }
                    JsonNode delta = choices.get(0).get("delta");
                    if (delta == null) {
                        return;
                    }
                    JsonNode content = delta.get("content");
                    if (content != null && !content.isNull()) {
                        String text = content.asText();
                        if (!text.isEmpty()) {
                            onToken.accept(text);
                        }
                    }
                } catch (Exception e) {
                    log.debug("忽略无法解析的流式片段：{}", line);
                }
            });

            onComplete.run();
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.error("调用 DeepSeek 流式 API 发生网络错误", e);
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "AI 服务连接失败，请稍后重试");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("调用 DeepSeek 流式 API 被中断", e);
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "AI 服务请求超时，请稍后重试");
        } catch (Exception e) {
            log.error("调用 DeepSeek 流式 API 发生未知错误", e);
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "AI 服务异常，请稍后重试");
        }
    }

    // ------------------------------------------------------------------
    // 共用：组装请求（含 RAG 上下文注入）
    // ------------------------------------------------------------------

    private ChatCompletionRequest buildRequest(String question, List<ChatMessage> history,
                                               List<SourceChunk> sources, boolean stream) {
        DeepSeekProperties.Api api = properties.getApi();
        DeepSeekProperties.Chat chat = properties.getChat();
        List<ChatCompletionRequest.Message> messages = buildMessages(question, history, sources);
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(api.getModel());
        request.setMessages(messages);
        request.setTemperature(chat.getTemperature());
        // 适当压低上限，避免模型无意义地 long generating，整体更快结束
        request.setMaxTokens(Math.min(chat.getMaxTokens(), 800));
        request.setStream(stream);
        return request;
    }

    private List<ChatCompletionRequest.Message> buildMessages(String question, List<ChatMessage> history,
                                                              List<SourceChunk> sources) {
        List<ChatCompletionRequest.Message> messages = new ArrayList<>();
        messages.add(new ChatCompletionRequest.Message("system", resolveSystemPrompt()));
        if (sources != null && !sources.isEmpty()) {
            messages.add(new ChatCompletionRequest.Message("system", buildRagInstruction(sources)));
        }
        for (ChatMessage message : limitHistory(history)) {
            if (message == null || !StringUtils.hasText(message.getContent())) {
                continue;
            }
            messages.add(new ChatCompletionRequest.Message(message.getRole(), message.getContent()));
        }
        messages.add(new ChatCompletionRequest.Message("user", question));
        return messages;
    }

    /**
     * 将检索到的站内攻略拼装成 system 级参考约束：要求模型优先依据素材、文末标注来源、不足时如实说明。
     */
    private String buildRagInstruction(List<SourceChunk> sources) {
        StringBuilder sb = new StringBuilder();
        sb.append("以下是来自本站内的相关游戏攻略素材（已按相关度排序）。请遵循以下要求：\n");
        sb.append("1. 优先依据这些素材回答用户问题，保证信息准确、有出处；\n");
        sb.append("2. 若素材不足以回答，请如实说明「未找到相关站内攻略」，不要编造；\n");
        sb.append("3. 回答结尾用「来源：<帖子标题>」标注引用了哪些素材（可多个）；\n");
        sb.append("4. 不要输出「根据资料显示」等无意义前缀，直接作答。\n\n");
        for (int i = 0; i < sources.size(); i++) {
            SourceChunk s = sources.get(i);
            sb.append("【素材 ").append(i + 1).append("】标题：")
                    .append(s.getTitle() == null ? "" : s.getTitle()).append("\n");
            String content = s.getContent() == null ? "" : s.getContent();
            if (content.length() > 600) {
                content = content.substring(0, 600);
            }
            sb.append(content).append("\n\n");
        }
        return sb.toString();
    }

    /**
     * 截断多轮对话历史，避免把整段长对话都发给模型导致首字延迟（TTFB）随聊天变长而变慢。
     */
    private List<ChatMessage> limitHistory(List<ChatMessage> history) {
        if (history == null || history.isEmpty()) {
            return List.of();
        }
        final int MAX_HISTORY = 10;
        if (history.size() <= MAX_HISTORY) {
            return history;
        }
        return history.subList(history.size() - MAX_HISTORY, history.size());
    }

    private String resolveSystemPrompt() {
        DeepSeekProperties.Chat chat = properties.getChat();
        if (chat != null && StringUtils.hasText(chat.getSystemPrompt())) {
            return chat.getSystemPrompt();
        }
        return DEFAULT_SYSTEM_PROMPT;
    }
}
