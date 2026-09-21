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

    /** 多轮对话历史，最多携带 20 条（传入 conversationId 时可省略，由服务端取历史） */
    @Valid
    @Size(max = 20, message = "对话历史最多 20 条")
    private List<ChatMessage> history;

    /**
     * 会话 id：传入则沿用该会话（服务端取其历史作为上下文），
     * 不传则自动新建会话。服务端会校验归属，越权 id 会被忽略并新建。
     */
    private Long conversationId;

    /**
     * 是否持久化到会话，默认 true。
     *
     * <p>润写、智能回复等「一次性辅助」应传 false：这类请求不属于连续对话，
     * 若持久化会为每次润写都新建一个会话，污染用户的会话列表。
     */
    private Boolean persist;
}
