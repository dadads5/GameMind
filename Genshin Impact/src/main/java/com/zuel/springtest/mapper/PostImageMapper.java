package com.zuel.springtest.mapper;

import com.zuel.springtest.entity.PostImage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 帖子配图数据访问
 */
@Mapper
@Repository
public interface PostImageMapper {

    @Insert("INSERT INTO post_image(post_id, url, sort) VALUES(#{postId}, #{url}, #{sort})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PostImage image);

    @Select("SELECT id, post_id, url, sort FROM post_image WHERE post_id = #{postId} ORDER BY sort ASC")
    List<PostImage> selectByPostId(@Param("postId") Long postId);

    @Select("<script>" +
            "SELECT id, post_id, url, sort FROM post_image " +
            "WHERE post_id IN " +
            "<foreach item='id' collection='postIds' open='(' separator=',' close=')'>#{id}</foreach> " +
            "ORDER BY post_id ASC, sort ASC" +
            "</script>")
    List<PostImage> selectByPostIds(@Param("postIds") List<Long> postIds);

    @Delete("DELETE FROM post_image WHERE post_id = #{postId}")
    int deleteByPostId(@Param("postId") Long postId);
}
