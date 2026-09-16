package com.zuel.springtest.mapper;

import com.zuel.springtest.entity.Community;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 社区数据访问
 */
@Mapper
@Repository
public interface CommunityMapper {

    @Select("SELECT * FROM community ORDER BY sort, id")
    List<Community> selectAll();

    @Select("SELECT * FROM community WHERE id = #{id}")
    Community selectById(Integer id);

    @Select("SELECT COALESCE(MAX(id), 0) FROM community")
    int selectMaxId();

    @Insert("INSERT INTO community(id, name, icon, description, sort) " +
            "VALUES(#{id}, #{name}, #{icon}, #{description}, #{sort})")
    int insert(Community community);

    @Update("UPDATE community SET name = #{name}, icon = #{icon}, " +
            "description = #{description}, sort = #{sort} WHERE id = #{id}")
    int update(Community community);

    @Delete("DELETE FROM community WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);
}
