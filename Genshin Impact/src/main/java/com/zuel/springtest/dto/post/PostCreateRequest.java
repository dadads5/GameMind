package com.zuel.springtest.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 发帖请求
 */
@Data
public class PostCreateRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题不能超过 100 个字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 20000, message = "内容过长")
    private String content;

    /** 板块 ID：1蒙德 2璃月 3稻妻 4须弥 5枫丹 6纳塔 7火影忍者 */
    @NotNull(message = "板块不能为空")
    private Integer boardId;

    /** 配图 URL 列表（可选） */
    private List<String> images;
}
