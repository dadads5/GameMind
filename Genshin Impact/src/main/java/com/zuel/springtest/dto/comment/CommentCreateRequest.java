package com.zuel.springtest.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表评论请求
 */
@Data
public class CommentCreateRequest {

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 5000, message = "评论内容过长")
    private String content;
}
