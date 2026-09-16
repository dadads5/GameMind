package com.zuel.springtest.mapper;

import com.zuel.springtest.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问
 *
 * <p>注意：{@code update} 不含 password 字段，密码修改请使用 {@link #updatePassword}，
 * 避免更新资料时把密码覆盖为空。
 */
@Mapper
@Repository
public interface UserMapper {

    // ---------------- 写操作 ----------------

    @Insert("INSERT INTO user(username, password, email, nickname, avatar, bio, status, created_at) " +
            "VALUES(#{username}, #{password}, #{email}, #{nickname}, #{avatar}, #{bio}, #{status}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    /**
     * 更新用户资料（不涉及密码）
     */
    @Update("UPDATE user SET username = #{username}, email = #{email}, nickname = #{nickname}, " +
            "avatar = #{avatar}, bio = #{bio}, status = #{status}, updated_at = NOW() " +
            "WHERE id = #{id}")
    int update(User user);

    @Update("UPDATE user SET password = #{password}, updated_at = NOW() WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Update("UPDATE user SET avatar = #{avatar}, updated_at = NOW() WHERE id = #{id}")
    int updateAvatar(@Param("id") Long id, @Param("avatar") String avatar);

    /** 主页被点赞数 +1 */
    @Update("UPDATE user SET like_count = like_count + 1 WHERE id = #{id}")
    int incrementLikeCount(@Param("id") Long id);

    @Delete("DELETE FROM user WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    // ---------------- 读操作 ----------------

    @Select("SELECT * FROM user WHERE id = #{id}")
    Optional<User> selectById(@Param("id") Long id);

    @Select("SELECT * FROM user WHERE username = #{username}")
    Optional<User> selectByUsername(@Param("username") String username);

    @Select("SELECT * FROM user WHERE email = #{email}")
    Optional<User> selectByEmail(@Param("email") String email);

    @Select("SELECT COUNT(*) > 0 FROM user WHERE username = #{username}")
    boolean existsByUsername(@Param("username") String username);

    @Select("SELECT COUNT(*) > 0 FROM user WHERE email = #{email}")
    boolean existsByEmail(@Param("email") String email);

    @Select("SELECT * FROM user ORDER BY created_at DESC")
    List<User> selectAll();

    // ---------------- 统计 ----------------

    @Select("SELECT COUNT(*) FROM user")
    int countAll();

    @Select("SELECT COUNT(*) FROM user WHERE DATE(created_at) = CURDATE()")
    int countToday();

    // ---------------- 管理端 ----------------

    /** 启停账号：status 1 正常，0 禁用 */
    @Update("UPDATE user SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") int status);

    /** 设置角色：0 普通用户，1 管理员 */
    @Update("UPDATE user SET role = #{role}, updated_at = NOW() WHERE id = #{id}")
    int updateRole(@Param("id") Long id, @Param("role") int role);

    /** 设置 VIP：0 否，1 是 */
    @Update("UPDATE user SET vip = #{vip}, updated_at = NOW() WHERE id = #{id}")
    int updateVip(@Param("id") Long id, @Param("vip") int vip);

    /** 管理端用户列表：支持用户名/昵称/邮箱模糊搜索 */
    @Select("<script>" +
            "SELECT * FROM user " +
            "<where>" +
            "  <if test='keyword != null and keyword.trim() != &quot;&quot;'>" +
            "    AND (username LIKE CONCAT('%', #{keyword}, '%') " +
            "      OR nickname LIKE CONCAT('%', #{keyword}, '%') " +
            "      OR email LIKE CONCAT('%', #{keyword}, '%')) " +
            "  </if>" +
            "</where>" +
            "ORDER BY created_at DESC " +
            "LIMIT #{limit} OFFSET #{offset}" +
            "</script>")
    List<User> selectForAdmin(@Param("keyword") String keyword,
                              @Param("limit") int limit,
                              @Param("offset") int offset);

    @Select("<script>" +
            "SELECT COUNT(*) FROM user " +
            "<where>" +
            "  <if test='keyword != null and keyword.trim() != &quot;&quot;'>" +
            "    AND (username LIKE CONCAT('%', #{keyword}, '%') " +
            "      OR nickname LIKE CONCAT('%', #{keyword}, '%') " +
            "      OR email LIKE CONCAT('%', #{keyword}, '%')) " +
            "  </if>" +
            "</where>" +
            "</script>")
    int countForAdmin(@Param("keyword") String keyword);
}
