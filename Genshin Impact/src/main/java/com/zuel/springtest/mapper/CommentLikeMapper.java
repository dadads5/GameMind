package com.zuel.springtest.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * 评论点赞数据访问
 *
 * <p>通过 (comment_id, user_id) 唯一键保证同一用户对同一评论只能点赞一次。
 */
@Mapper
@Repository
public interface CommentLikeMapper {

    @Insert("INSERT INTO comment_like(comment_id, user_id) VALUES(#{commentId}, #{userId})")
    int insert(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Delete("DELETE FROM comment_like WHERE comment_id = #{commentId} AND user_id = #{userId}")
    int deleteByCommentAndUser(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) > 0 FROM comment_like WHERE comment_id = #{commentId} AND user_id = #{userId}")
    boolean exists(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM comment_like WHERE comment_id = #{commentId}")
    int countByComment(@Param("commentId") Long commentId);

    @Select("<script>" +
            "SELECT comment_id FROM comment_like WHERE user_id = #{userId} AND comment_id IN " +
            "<foreach item='item' collection='commentIds' open='(' separator=',' close=')'>#{item}</foreach>" +
            "</script>")
    List<Long> selectLikedCommentIds(@Param("userId") Long userId,
                                     @Param("commentIds") Collection<Long> commentIds);

    /** 删除评论时同步清理点赞记录 */
    @Delete("DELETE FROM comment_like WHERE comment_id = #{commentId}")
    int deleteByCommentId(@Param("commentId") Long commentId);

    /** 删除帖子时同步清理其下评论的点赞记录 */
    @Delete("DELETE cl FROM comment_like cl " +
            "INNER JOIN comment c ON cl.comment_id = c.id " +
            "WHERE c.post_id = #{postId}")
    int deleteByPostId(@Param("postId") Long postId);
}
