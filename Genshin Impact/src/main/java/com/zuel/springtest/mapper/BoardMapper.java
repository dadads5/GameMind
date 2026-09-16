package com.zuel.springtest.mapper;

import com.zuel.springtest.entity.Board;
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
 * 板块数据访问
 */
@Mapper
@Repository
public interface BoardMapper {

    @Select("SELECT * FROM board ORDER BY sort, id")
    List<Board> selectAll();

    @Select("SELECT * FROM board WHERE id = #{id}")
    Optional<Board> selectById(Integer id);

    @Select("SELECT * FROM board WHERE community_id = #{communityId} ORDER BY sort, id")
    List<Board> selectByCommunityId(@Param("communityId") Integer communityId);

    @Select("SELECT COUNT(*) FROM board")
    int countAll();

    @Select("SELECT COALESCE(MAX(id), 0) FROM board")
    int selectMaxId();

    // ---------------- 管理端 ----------------

    // board 表 id 非自增，需手动分配；故 INSERT 显式带上 id 列
    @Insert("INSERT INTO board(id, community_id, name, icon, description, sort) " +
            "VALUES(#{id}, #{communityId}, #{name}, #{icon}, #{description}, #{sort})")
    int insert(Board board);

    @Update("UPDATE board SET community_id = #{communityId}, name = #{name}, icon = #{icon}, " +
            "description = #{description}, sort = #{sort} WHERE id = #{id}")
    int update(Board board);

    @Delete("DELETE FROM board WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);

    @Delete("DELETE FROM board WHERE community_id = #{communityId}")
    int deleteByCommunityId(@Param("communityId") Integer communityId);
}
