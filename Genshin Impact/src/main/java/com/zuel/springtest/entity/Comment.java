package com.zuel.springtest.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论
 */
@Data
public class Comment {

    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    private Integer likeCount = 0;
    private LocalDateTime createdAt;

    // ---------- 关联字段（由 JOIN 查询填充，非数据库列） ----------

    /** 作者用户名 */
    private String authorName;
    /** 作者头像 */
    private String authorAvatar;
    /** 所属帖子标题（仅「我的评论」场景填充） */
    private String postTitle;
    /** 当前登录用户是否已点赞（由 Service 层按情况填充） */
    private Boolean liked;
}
