package com.zuel.springtest.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子点赞记录
 *
 * <p>(post_id, user_id) 唯一，用于实现「同一用户只能赞一次、可取消」。
 */
@Data
public class PostLike {

    private Long id;
    private Long postId;
    private Long userId;
    private LocalDateTime createdAt;
}
