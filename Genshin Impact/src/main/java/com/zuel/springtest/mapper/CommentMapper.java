package com.zuel.springtest.mapper;

import com.zuel.springtest.entity.Comment;
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
 * 评论数据访问
 */
@Mapper
@Repository
public interface CommentMapper {

    // ---------------- 写操作 ----------------

    @Insert("INSERT INTO comment(post_id, user_id, content, like_count, created_at) " +
            "VALUES(#{postId}, #{userId}, #{content}, #{likeCount}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Comment comment);

    @Update("UPDATE comment SET content = #{content} WHERE id = #{id}")
    int updateContent(@Param("id") Long id, @Param("content") String content);

    /** 点赞数 +1 */
    @Update("UPDATE comment SET like_count = like_count + 1 WHERE id = #{commentId}")
    int incrementLikeCount(@Param("commentId") Long commentId);

    /** 点赞数 -1（不小于 0） */
    @Update("UPDATE comment SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{commentId}")
    int decrementLikeCount(@Param("commentId") Long commentId);

    @Select("SELECT like_count FROM comment WHERE id = #{commentId}")
    int getLikeCount(@Param("commentId") Long commentId);

    @Delete("DELETE FROM comment WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Delete("DELETE FROM comment WHERE post_id = #{postId}")
    int deleteByPostId(@Param("postId") Long postId);

    // ---------------- 读操作 ----------------

    /** 帖子下的评论列表，正序排列 */
    @Select("SELECT c.*, u.username AS author_name, u.avatar AS author_avatar " +
            "FROM comment c LEFT JOIN user u ON c.user_id = u.id " +
            "WHERE c.post_id = #{postId} " +
            "ORDER BY c.created_at ASC")
    List<Comment> selectByPostId(@Param("postId") Long postId);

    /** 某用户发表的评论，带出所属帖子标题 */
    @Select("SELECT c.*, p.title AS post_title, u.username AS author_name, u.avatar AS author_avatar " +
            "FROM comment c " +
            "LEFT JOIN post p ON c.post_id = p.id " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "WHERE c.user_id = #{userId} " +
            "ORDER BY c.created_at DESC")
    List<Comment> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM comment WHERE id = #{id}")
    Comment selectById(@Param("id") Long id);

    // ---------------- 统计 ----------------

    @Select("SELECT COUNT(*) FROM comment")
    int countAll();

    @Select("SELECT COUNT(*) FROM comment WHERE DATE(created_at) = CURDATE()")
    int countToday();

    @Select("SELECT COUNT(*) FROM comment WHERE post_id = #{postId}")
    int countByPostId(@Param("postId") Long postId);

    /** 某用户的回复数 */
    @Select("SELECT COUNT(*) FROM comment WHERE user_id = #{userId}")
    int countByAuthor(@Param("userId") Long userId);

    // ---------------- 管理端 ----------------

    /** 管理端评论列表：带作者与所属帖子标题 */
    @Select("<script>" +
            "SELECT c.*, u.username AS author_name, p.title AS post_title " +
            "FROM comment c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "LEFT JOIN post p ON c.post_id = p.id " +
            "<where>" +
            "  <if test='keyword != null and keyword.trim() != &quot;&quot;'>" +
            "    AND c.content LIKE CONCAT('%', #{keyword}, '%') " +
            "  </if>" +
            "</where>" +
            "ORDER BY c.created_at DESC " +
            "LIMIT #{limit} OFFSET #{offset}" +
            "</script>")
    List<Comment> selectForAdmin(@Param("keyword") String keyword,
                                 @Param("limit") int limit,
                                 @Param("offset") int offset);

    @Select("<script>" +
            "SELECT COUNT(*) FROM comment c " +
            "<where>" +
            "  <if test='keyword != null and keyword.trim() != &quot;&quot;'>" +
            "    AND c.content LIKE CONCAT('%', #{keyword}, '%') " +
            "  </if>" +
            "</where>" +
            "</script>")
    int countForAdmin(@Param("keyword") String keyword);
}
