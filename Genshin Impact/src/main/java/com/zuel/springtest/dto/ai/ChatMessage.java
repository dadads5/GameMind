package com.zuel.springtest.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    /** 角色：user 或 assistant */
    @NotBlank(message = "消息角色不能为空")
    @Pattern(regexp = "user|assistant", message = "消息角色只能是 user 或 assistant")
    private String role;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 8000, message = "消息内容过长")
    private String content;
}
