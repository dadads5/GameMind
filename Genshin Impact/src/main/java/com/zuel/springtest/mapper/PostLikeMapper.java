package com.zuel.springtest.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 帖子点赞数据访问
 *
 * <p>通过 (post_id, user_id) 唯一键保证同一用户对同一帖子只能点赞一次。
 */
@Mapper
@Repository
public interface PostLikeMapper {

    @Insert("INSERT INTO post_like(post_id, user_id) VALUES(#{postId}, #{userId})")
    int insert(@Param("postId") Long postId, @Param("userId") Long userId);

    @Delete("DELETE FROM post_like WHERE post_id = #{postId} AND user_id = #{userId}")
    int deleteByPostAndUser(@Param("postId") Long postId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) > 0 FROM post_like WHERE post_id = #{postId} AND user_id = #{userId}")
    boolean exists(@Param("postId") Long postId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM post_like WHERE post_id = #{postId}")
    int countByPost(@Param("postId") Long postId);

    @Select("SELECT COUNT(*) FROM post_like WHERE user_id = #{userId}")
    int countByUser(@Param("userId") Long userId);

    /** 批量判断某用户对若干帖子的点赞状态 */
    @Select("<script>" +
            "SELECT post_id FROM post_like WHERE user_id = #{userId} AND post_id IN " +
            "<foreach item='item' collection='postIds' open='(' separator=',' close=')'>#{item}</foreach>" +
            "</script>")
    java.util.List<Long> selectLikedPostIds(@Param("userId") Long userId,
                                            @Param("postIds") java.util.Collection<Long> postIds);

    /** 删除帖子时同步清理点赞记录 */
    @Delete("DELETE FROM post_like WHERE post_id = #{postId}")
    int deleteByPostId(@Param("postId") Long postId);
}
