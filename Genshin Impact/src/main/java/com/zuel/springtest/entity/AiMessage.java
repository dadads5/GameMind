package com.zuel.springtest.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 会话中的一条消息
 *
 * <p>role 取值：user / assistant，与大模型消息的 role 保持一致，便于直接拼装上下文。
 */
@Data
public class AiMessage {

    private Long id;
    private Long conversationId;
    private String role;
    private String content;
    private LocalDateTime createdAt;
}
