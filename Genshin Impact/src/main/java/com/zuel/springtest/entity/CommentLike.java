package com.zuel.springtest.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论点赞记录
 *
 * <p>(comment_id, user_id) 唯一，用于实现「同一用户只能赞一次、可取消」。
 */
@Data
public class CommentLike {

    private Long id;
    private Long commentId;
    private Long userId;
    private LocalDateTime createdAt;
}
