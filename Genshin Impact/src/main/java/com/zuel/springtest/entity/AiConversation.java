package com.zuel.springtest.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 会话（一次连续的多轮对话）
 */
@Data
public class AiConversation {

    private Long id;
    private Long userId;
    /** 会话标题，取首条提问的前若干字符 */
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
