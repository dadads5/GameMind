package com.zuel.springtest.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户主页点赞记录
 *
 * <p>(liker_id, liked_user_id, like_date) 唯一，实现「同一用户对同一人每天只能赞一次」。
 * like_date 为 yyyy-MM-dd 格式。
 */
@Data
public class UserLike {

    private Long id;
    private Long likerId;
    private Long likedUserId;
    private String likeDate;
    private LocalDateTime createdAt;
}
