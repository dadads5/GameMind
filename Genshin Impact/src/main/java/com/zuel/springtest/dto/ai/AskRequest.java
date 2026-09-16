package com.zuel.springtest.dto.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * AI 提问请求
 */
@Data
public class AskRequest {

    @NotBlank(message = "问题不能为空")
    @Size(max = 2000, message = "问题内容过长")
    private String question;

    /** 多轮对话历史，最多携带 20 条 */
    @Valid
    @Size(max = 20, message = "对话历史最多 20 条")
    private List<ChatMessage> history;
}
