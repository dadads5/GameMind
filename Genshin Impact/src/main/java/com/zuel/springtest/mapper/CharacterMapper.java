package com.zuel.springtest.mapper;

import com.zuel.springtest.entity.Character;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 原神角色数据访问
 *
 * <p>字段与 {@code genshin_character} 表完全一致：
 * id / name / icon / subtitle / weapons / artifacts / stats / talents / element / created_at
 */
@Mapper
@Repository
public interface CharacterMapper {

    @Select("SELECT * FROM genshin_character ORDER BY id")
    List<Character> selectAll();

    @Select("SELECT * FROM genshin_character WHERE id = #{id}")
    Optional<Character> selectById(@Param("id") Integer id);

    @Select("SELECT * FROM genshin_character WHERE name = #{name}")
    Optional<Character> selectByName(@Param("name") String name);

    @Select("SELECT * FROM genshin_character WHERE element = #{element} ORDER BY id")
    List<Character> selectByElement(@Param("element") String element);

    /** 按名称或副标题模糊搜索 */
    @Select("SELECT * FROM genshin_character " +
            "WHERE name LIKE CONCAT('%', #{keyword}, '%') " +
            "   OR subtitle LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY id")
    List<Character> search(@Param("keyword") String keyword);

    /** 随机推荐若干角色 */
    @Select("SELECT * FROM genshin_character ORDER BY RAND() LIMIT #{limit}")
    List<Character> selectRandom(@Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM genshin_character")
    int countAll();

    /** 按元素统计角色数量 */
    @Select("SELECT element AS name, COUNT(*) AS count " +
            "FROM genshin_character " +
            "GROUP BY element " +
            "ORDER BY count DESC")
    List<Map<String, Object>> countByElement();
}
