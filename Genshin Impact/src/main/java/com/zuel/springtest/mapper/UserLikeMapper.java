package com.zuel.springtest.mapper;

import com.zuel.springtest.entity.UserLike;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 用户主页点赞记录数据访问
 *
 * <p>user_like 表 id 非自增，需手动分配。
 */
@Mapper
@Repository
public interface UserLikeMapper {

    @Select("SELECT COALESCE(MAX(id), 0) FROM user_like")
    int selectMaxId();

    @Insert("INSERT INTO user_like(id, liker_id, liked_user_id, like_date, created_at) " +
            "VALUES(#{id}, #{likerId}, #{likedUserId}, #{likeDate}, #{createdAt})")
    int insert(UserLike userLike);

    /** 查询某人对某人某天是否已点赞（用于每天一次限制） */
    @Select("SELECT COUNT(*) FROM user_like " +
            "WHERE liker_id = #{likerId} AND liked_user_id = #{likedUserId} AND like_date = #{likeDate}")
    int countByLikerAndDate(@Param("likerId") Long likerId,
                            @Param("likedUserId") Long likedUserId,
                            @Param("likeDate") String likeDate);

    /** 某人被点赞总次数（用于主页展示） */
    @Select("SELECT COUNT(*) FROM user_like WHERE liked_user_id = #{likedUserId}")
    int countByLikedUser(@Param("likedUserId") Long likedUserId);
}
