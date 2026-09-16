package com.zuel.springtest.mapper;

import com.zuel.springtest.entity.Notification;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 站内消息通知数据访问（表 notification）
 *
 * <p>唯一键 (user_id, actor_id, type, target_type, target_id) 保证同一人对同一对象的同类行为
 * 只保留一条通知，避免反复点赞/取消造成消息刷屏。
 */
@Mapper
@Repository
public interface NotificationMapper {

    @Insert("INSERT INTO notification (user_id, actor_id, type, target_type, target_id, content, is_read, created_at) " +
            "VALUES (#{userId}, #{actorId}, #{type}, #{targetType}, #{targetId}, #{content}, #{isRead}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Notification notification);

    /** 消息列表，按时间倒序，JOIN 出触发者昵称与头像 */
    @Select("SELECT n.*, u.username AS actor_name, u.avatar AS actor_avatar " +
            "FROM notification n LEFT JOIN user u ON n.actor_id = u.id " +
            "WHERE n.user_id = #{userId} " +
            "ORDER BY n.created_at DESC " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<Notification> selectByUser(@Param("userId") Long userId,
                                    @Param("limit") int limit,
                                    @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId}")
    int countByUser(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0")
    int countUnread(@Param("userId") Long userId);

    @Update("UPDATE notification SET is_read = 1 WHERE id = #{id} AND user_id = #{userId}")
    int markRead(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE notification SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllRead(@Param("userId") Long userId);

    /** 撤回某条通知（取赞、删除评论等场景） */
    @Delete("DELETE FROM notification " +
            "WHERE user_id = #{userId} AND actor_id = #{actorId} " +
            "  AND type = #{type} AND target_type = #{targetType} AND target_id = #{targetId}")
    int deleteOne(@Param("userId") Long userId,
                  @Param("actorId") Long actorId,
                  @Param("type") String type,
                  @Param("targetType") String targetType,
                  @Param("targetId") Long targetId);

    @Delete("DELETE FROM notification WHERE id = #{id} AND user_id = #{userId}")
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);
}
