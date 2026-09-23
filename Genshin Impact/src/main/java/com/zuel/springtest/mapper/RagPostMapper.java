package com.zuel.springtest.mapper;

import com.zuel.springtest.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * RAG 建索引时只读查询帖子正文（避免与 PostMapper 的方法耦合）
 */
@Mapper
public interface RagPostMapper {

    @Select("SELECT p.id, p.title, p.content, b.community_id AS community_id " +
            "FROM post p LEFT JOIN board b ON p.board_id = b.id " +
            "WHERE p.content IS NOT NULL AND p.content <> ''")
    List<Post> selectAllPosts();

    @Select("SELECT p.id, p.title, p.content, b.community_id AS community_id " +
            "FROM post p LEFT JOIN board b ON p.board_id = b.id " +
            "WHERE p.id = #{id}")
    Post selectPostById(Long id);
}
