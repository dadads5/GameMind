package com.zuel.springtest.mapper;

import com.zuel.springtest.entity.AiMessage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AI 会话消息数据访问
 */
@Mapper
@Repository
public interface AiMessageMapper {

    @Insert("INSERT INTO ai_message(conversation_id, role, content, created_at) " +
            "VALUES(#{conversationId}, #{role}, #{content}, NOW())")
    int insert(AiMessage message);

    /** 按时间正序取回，便于直接作为上下文拼装 */
    @Select("SELECT id, conversation_id, role, content, created_at FROM ai_message " +
            "WHERE conversation_id = #{conversationId} ORDER BY id ASC")
    List<AiMessage> selectByConversation(@Param("conversationId") Long conversationId);

    @Delete("DELETE FROM ai_message WHERE conversation_id = #{conversationId}")
    int deleteByConversation(@Param("conversationId") Long conversationId);
}
