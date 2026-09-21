package com.zuel.springtest.mapper;

import com.zuel.springtest.entity.AiConversation;
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
 * AI 会话数据访问
 */
@Mapper
@Repository
public interface AiConversationMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO ai_conversation(user_id, title, created_at, updated_at) " +
            "VALUES(#{userId}, #{title}, NOW(), NOW())")
    int insert(AiConversation conversation);

    /** 某用户的会话列表，按最近更新排序 */
    @Select("SELECT id, user_id, title, created_at, updated_at FROM ai_conversation " +
            "WHERE user_id = #{userId} ORDER BY updated_at DESC LIMIT 50")
    List<AiConversation> selectByUser(@Param("userId") Long userId);

    @Select("SELECT id, user_id, title, created_at, updated_at FROM ai_conversation WHERE id = #{id}")
    AiConversation selectById(@Param("id") Long id);

    /** 带归属校验的删除，防止越权删除他人会话 */
    @Delete("DELETE FROM ai_conversation WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    /** 刷新更新时间，用于列表排序 */
    @Update("UPDATE ai_conversation SET updated_at = NOW() WHERE id = #{id}")
    int touch(@Param("id") Long id);

    @Update("UPDATE ai_conversation SET title = #{title} WHERE id = #{id}")
    int updateTitle(@Param("id") Long id, @Param("title") String title);
}
