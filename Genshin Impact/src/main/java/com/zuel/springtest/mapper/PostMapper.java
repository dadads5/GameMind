package com.zuel.springtest.mapper;

import com.zuel.springtest.entity.Post;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 帖子数据访问
 *
 * <p>列表与详情查询统一 LEFT JOIN user 带出作者信息，避免出现 authorName 为 null 的问题。
 */
@Mapper
@Repository
public interface PostMapper {

    // ---------------- 写操作 ----------------

    @Insert("INSERT INTO post(user_id, board_id, title, content, view_count, like_count, comment_count, created_at) " +
            "VALUES(#{userId}, #{boardId}, #{title}, #{content}, #{viewCount}, #{likeCount}, #{commentCount}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Post post);

    /**
     * 更新帖子正文（不含统计字段）
     */
    @Update("UPDATE post SET title = #{title}, content = #{content}, board_id = #{boardId}, updated_at = NOW() " +
            "WHERE id = #{id}")
    int updateContent(@Param("id") Long id,
                      @Param("title") String title,
                      @Param("content") String content,
                      @Param("boardId") Integer boardId);

    @Update("UPDATE post SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    /** 浏览量批量累加（定时任务把 Redis 增量落库时使用） */
    @Update("UPDATE post SET view_count = view_count + #{delta} WHERE id = #{id}")
    int addViewCount(@Param("id") Long id, @Param("delta") long delta);

    /** 点赞数 +1 */
    @Update("UPDATE post SET like_count = like_count + 1 WHERE id = #{postId}")
    int incrementLikeCount(@Param("postId") Long postId);

    /** 点赞数 -1（不小于 0） */
    @Update("UPDATE post SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{postId}")
    int decrementLikeCount(@Param("postId") Long postId);

    @Select("SELECT like_count FROM post WHERE id = #{postId}")
    int getLikeCount(@Param("postId") Long postId);

    /** 按 comment 表重算评论数 */
    @Update("UPDATE post SET comment_count = (SELECT COUNT(*) FROM comment WHERE post_id = #{postId}) " +
            "WHERE id = #{postId}")
    int refreshCommentCount(@Param("postId") Long postId);

    @Delete("DELETE FROM post WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    // ---------------- 读操作 ----------------

    @Select("SELECT p.*, u.username AS author_name, u.avatar AS author_avatar " +
            "FROM post p LEFT JOIN user u ON p.user_id = u.id " +
            "WHERE p.id = #{id}")
    Post selectById(@Param("id") Long id);

    /**
     * 条件查询帖子（带作者信息）
     *
     * @param boardId 板块 ID，为空表示不限
     * @param keyword 标题/内容模糊匹配关键词，为空表示不限
     * @param authorId 作者 ID，为空表示不限
     * @param sort   排序方式：hot=热度优先，其余（含 latest）=时间倒序
     */
    @Select("<script>" +
            "SELECT p.*, u.username AS author_name, u.avatar AS author_avatar " +
            "FROM post p LEFT JOIN user u ON p.user_id = u.id " +
            "<where>" +
            "  <if test='boardId != null'>AND p.board_id = #{boardId} </if>" +
            "  <if test='communityId != null'>AND p.board_id IN (SELECT id FROM board WHERE community_id = #{communityId}) </if>" +
            "  <if test='keyword != null and keyword.trim() != &quot;&quot;'>" +
            "    AND (p.title LIKE CONCAT('%', #{keyword}, '%') OR p.content LIKE CONCAT('%', #{keyword}, '%')) " +
            "  </if>" +
            "  <if test='authorId != null'>AND p.user_id = #{authorId} </if>" +
            "</where>" +
            "<choose>" +
            "  <when test=\"sort != null and sort == 'hot'\">" +
            "    ORDER BY p.is_top DESC, (p.view_count + p.like_count * 2) DESC, p.created_at DESC " +
            "  </when>" +
            "  <otherwise>ORDER BY p.is_top DESC, p.created_at DESC </otherwise>" +
            "</choose>" +
            "</script>")
    List<Post> selectByCondition(@Param("boardId") Integer boardId,
                                 @Param("communityId") Integer communityId,
                                 @Param("keyword") String keyword,
                                 @Param("authorId") Long authorId,
                                 @Param("sort") String sort);

    /**
     * 热门帖子：按「浏览量 + 点赞数 * 2」降序
     */
    @Select("<script>" +
            "SELECT p.*, u.username AS author_name, u.avatar AS author_avatar " +
            "FROM post p LEFT JOIN user u ON p.user_id = u.id " +
            "<where>" +
            "  <if test='boardId != null'>AND p.board_id = #{boardId} </if>" +
            "  <if test='communityId != null'>AND p.board_id IN (SELECT id FROM board WHERE community_id = #{communityId}) </if>" +
            "</where>" +
            "ORDER BY p.is_top DESC, (p.view_count + p.like_count * 2) DESC, p.created_at DESC " +
            "LIMIT #{limit}" +
            "</script>")
    List<Post> selectHot(@Param("boardId") Integer boardId,
                         @Param("communityId") Integer communityId,
                         @Param("limit") int limit);

    /**
     * 按 id 列表批量查询（顺序由调用方依据热榜 ZSet 重排）
     */
    @Select("<script>" +
            "SELECT p.*, u.username AS author_name, u.avatar AS author_avatar " +
            "FROM post p LEFT JOIN user u ON p.user_id = u.id " +
            "WHERE p.id IN " +
            "<foreach item='item' collection='ids' open='(' separator=',' close=')'>#{item}</foreach>" +
            "</script>")
    List<Post> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 热榜重建用的帖子热度快照（仅取排序所需字段）
     *
     * <p>按与 PostService#hotScore 一致的权重在数据库侧先排序，只取前 1000 条，
     * 避免重建时全表扫描。
     */
    @Select("SELECT id, board_id, view_count, like_count, comment_count, created_at FROM post "
            + "ORDER BY (view_count + like_count * 3 + comment_count * 5) DESC LIMIT 1000")
    List<Post> selectAllHot();

    // ---------------- 统计 ----------------

    @Select("SELECT COUNT(*) FROM post")
    int countAll();

    @Select("SELECT IFNULL(SUM(view_count), 0) FROM post")
    int sumViewCount();

    @Select("SELECT IFNULL(SUM(comment_count), 0) FROM post")
    int sumCommentCount();

    @Select("SELECT COUNT(*) FROM post WHERE DATE(created_at) = CURDATE()")
    int countToday();

    @Select("<script>" +
            "SELECT COUNT(*) FROM post WHERE board_id IN " +
            "<foreach item='item' collection='boardIds' open='(' separator=',' close=')'>#{item}</foreach>" +
            "</script>")
    int countByBoardIds(@Param("boardIds") List<Integer> boardIds);

    @Select("SELECT COUNT(*) FROM post WHERE board_id = #{boardId} AND DATE(created_at) = CURDATE()")
    int countTodayByBoardId(@Param("boardId") Integer boardId);

    /** 某用户的发帖总数 */
    @Select("SELECT COUNT(*) FROM post WHERE user_id = #{userId}")
    int countByAuthor(@Param("userId") Long userId);

    /** 某用户的帖子获赞总数 */
    @Select("SELECT IFNULL(SUM(like_count), 0) FROM post WHERE user_id = #{userId}")
    int sumLikeCountByAuthor(@Param("userId") Long userId);

    @Select("<script>" +
            "SELECT board_id, COUNT(*) AS cnt FROM post " +
            "WHERE DATE(created_at) = CURDATE() AND board_id IN " +
            "<foreach item='item' collection='boardIds' open='(' separator=',' close=')'>#{item}</foreach>" +
            " GROUP BY board_id" +
            "</script>")
    List<Map<String, Object>> countTodayByBoardIds(@Param("boardIds") List<Integer> boardIds);

    // ---------------- 管理端 ----------------

    /** 管理端帖子列表：带作者与板块名，置顶优先 */
    @Select("<script>" +
            "SELECT p.*, u.username AS author_name, u.avatar AS author_avatar, b.name AS board_name " +
            "FROM post p " +
            "LEFT JOIN user u ON p.user_id = u.id " +
            "LEFT JOIN board b ON p.board_id = b.id " +
            "<where>" +
            "  <if test='keyword != null and keyword.trim() != &quot;&quot;'>" +
            "    AND (p.title LIKE CONCAT('%', #{keyword}, '%') OR p.content LIKE CONCAT('%', #{keyword}, '%')) " +
            "  </if>" +
            "  <if test='boardId != null'>" +
            "    AND p.board_id = #{boardId} " +
            "  </if>" +
            "</where>" +
            "ORDER BY p.is_top DESC, p.created_at DESC " +
            "LIMIT #{limit} OFFSET #{offset}" +
            "</script>")
    List<Post> selectForAdmin(@Param("keyword") String keyword,
                              @Param("boardId") Integer boardId,
                              @Param("limit") int limit,
                              @Param("offset") int offset);

    /** 管理端帖子总数（支持关键词与板块） */
    @Select("<script>" +
            "SELECT COUNT(*) FROM post p " +
            "<where>" +
            "  <if test='keyword != null and keyword.trim() != &quot;&quot;'>" +
            "    AND (p.title LIKE CONCAT('%', #{keyword}, '%') OR p.content LIKE CONCAT('%', #{keyword}, '%')) " +
            "  </if>" +
            "  <if test='boardId != null'>" +
            "    AND p.board_id = #{boardId} " +
            "  </if>" +
            "</where>" +
            "</script>")
    int countForAdmin(@Param("keyword") String keyword, @Param("boardId") Integer boardId);

    @Update("UPDATE post SET is_top = #{isTop} WHERE id = #{id}")
    int updateTop(@Param("id") Long id, @Param("isTop") int isTop);

    @Update("UPDATE post SET is_essence = #{isEssence} WHERE id = #{id}")
    int updateEssence(@Param("id") Long id, @Param("isEssence") int isEssence);
}
