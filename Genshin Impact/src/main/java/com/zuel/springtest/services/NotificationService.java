package com.zuel.springtest.services;

import com.zuel.springtest.entity.Comment;
import com.zuel.springtest.entity.Notification;
import com.zuel.springtest.entity.Post;
import com.zuel.springtest.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 站内消息通知业务
 *
 * <p>触发点：帖子被赞、评论被赞、帖子被评论。自己操作自己的内容不产生通知；
 * 取消点赞会撤回对应通知，避免消息误导。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    /** 帖子被赞 */
    public static final String TYPE_LIKE_POST = "LIKE_POST";
    /** 评论被赞 */
    public static final String TYPE_LIKE_COMMENT = "LIKE_COMMENT";
    /** 帖子被评论 */
    public static final String TYPE_COMMENT_POST = "COMMENT_POST";

    private static final String TARGET_POST = "POST";
    private static final String TARGET_COMMENT = "COMMENT";

    private final NotificationMapper notificationMapper;

    /** 帖子点赞：liked=true 生成/刷新通知，liked=false 撤回通知 */
    public void notifyLikePost(Post post, Long actorId, boolean liked) {
        if (post == null || post.getUserId() == null) {
            return;
        }
        handleLike(post.getUserId(), actorId, TYPE_LIKE_POST, TARGET_POST, post.getId(), liked, "赞了你的帖子");
    }

    /**
     * 评论点赞：liked=true 生成/刷新通知，liked=false 撤回通知
     *
     * <p>target 统一记录为评论所属帖子（而非评论本身），这样三类通知点击后都能跳转到帖子详情，
     * 前端不需要再按类型区分跳转逻辑。
     */
    public void notifyLikeComment(Comment comment, Long actorId, boolean liked) {
        if (comment == null || comment.getUserId() == null || comment.getPostId() == null) {
            return;
        }
        handleLike(comment.getUserId(), actorId, TYPE_LIKE_COMMENT, TARGET_POST, comment.getPostId(), liked, "赞了你的评论");
    }

    /** 帖子被评论 */
    public void notifyCommentPost(Post post, Long actorId, String content) {
        if (post == null || post.getUserId() == null || post.getUserId().equals(actorId)) {
            return;
        }
        Notification notification = new Notification();
        notification.setUserId(post.getUserId());
        notification.setActorId(actorId);
        notification.setType(TYPE_COMMENT_POST);
        notification.setTargetType(TARGET_POST);
        notification.setTargetId(post.getId());
        notification.setContent("评论了你的帖子：" + abbreviate(content, 40));
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(notification);
    }

    /** 消息列表 */
    public List<Notification> list(Long userId, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);
        int offset = (safePage - 1) * safeSize;
        return notificationMapper.selectByUser(userId, safeSize, offset);
    }

    public int countByUser(Long userId) {
        return notificationMapper.countByUser(userId);
    }

    public int countUnread(Long userId) {
        return notificationMapper.countUnread(userId);
    }

    public void markRead(Long userId, Long id) {
        notificationMapper.markRead(id, userId);
    }

    public void markAllRead(Long userId) {
        notificationMapper.markAllRead(userId);
    }

    public void delete(Long userId, Long id) {
        notificationMapper.deleteById(id, userId);
    }

    // ------------------------------------------------------------------
    // 私有方法
    // ------------------------------------------------------------------

    private void handleLike(Long receiverId, Long actorId, String type, String targetType,
                            Long targetId, boolean liked, String summary) {
        if (receiverId == null || receiverId.equals(actorId)) {
            return;
        }
        // 取消点赞：撤回此前的通知
        if (!liked) {
            notificationMapper.deleteOne(receiverId, actorId, type, targetType, targetId);
            return;
        }
        // 先删后插，保证唯一键不冲突且始终是最新的点赞
        notificationMapper.deleteOne(receiverId, actorId, type, targetType, targetId);
        Notification notification = new Notification();
        notification.setUserId(receiverId);
        notification.setActorId(actorId);
        notification.setType(type);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);
        notification.setContent(summary);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(notification);
    }

    private String abbreviate(String text, int max) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max) + "…";
    }
}
