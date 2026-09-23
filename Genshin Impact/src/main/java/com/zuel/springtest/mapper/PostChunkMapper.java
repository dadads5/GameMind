package com.zuel.springtest.mapper;

import com.zuel.springtest.entity.PostChunk;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 帖子知识片段的持久化（RAG 索引表）
 */
@Mapper
public interface PostChunkMapper {

    @Insert("INSERT INTO post_chunk(post_id, title, chunk_index, content, embedding) " +
            "VALUES(#{postId}, #{title}, #{chunkIndex}, #{content}, #{embedding}) " +
            "ON DUPLICATE KEY UPDATE title = VALUES(title), content = VALUES(content), " +
            "embedding = VALUES(embedding)")
    int insert(PostChunk chunk);

    @Delete("DELETE FROM post_chunk WHERE post_id = #{postId}")
    int deleteByPostId(Long postId);

    @Select("SELECT id, post_id, title, chunk_index, content, embedding FROM post_chunk")
    List<PostChunk> selectAll();

    @Select("SELECT COUNT(*) FROM post_chunk")
    int count();
}
