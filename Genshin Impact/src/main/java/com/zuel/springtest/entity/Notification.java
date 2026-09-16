package com.zuel.springtest.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站内消息通知
 *
 * <p>第一期仅覆盖三类：帖子被赞（LIKE_POST）、评论被赞（LIKE_COMMENT）、帖子被评论（COMMENT_POST）。
 * 采用轮询方式获取，未读数走索引计数，无需引入 Redis。
 */
@Data
public class Notification {

    private Long id;
    /** 接收者 */
    private Long userId;
    /** 触发者（点赞/评论的人） */
    private Long actorId;
    /** LIKE_POST / LIKE_COMMENT / COMMENT_POST */
    private String type;
    /** POST / COMMENT */
    private String targetType;
    /** 被赞或被评论的对象 ID */
    private Long targetId;
    /** 摘要文案 */
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;

    // ---------- 关联字段（JOIN user 表填充，非数据库列） ----------

    /** 触发者昵称 */
    private String actorName;
    /** 触发者头像 */
    private String actorAvatar;
}
