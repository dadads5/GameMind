package com.zuel.springtest.mapper;

import com.zuel.springtest.entity.WzHero;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 王者荣耀英雄数据访问（表 wzry_hero）
 */
@Mapper
@Repository
public interface WzHeroMapper {

    @Select("SELECT * FROM wzry_hero ORDER BY id")
    List<WzHero> selectAll();

    @Select("SELECT * FROM wzry_hero WHERE id = #{id}")
    Optional<WzHero> selectById(@Param("id") Integer id);

    @Select("SELECT * FROM wzry_hero WHERE name = #{name}")
    Optional<WzHero> selectByName(@Param("name") String name);

    /** 按职业筛选：坦克 / 战士 / 刺客 / 法师 / 射手 / 辅助 */
    @Select("SELECT * FROM wzry_hero WHERE role = #{role} ORDER BY id")
    List<WzHero> selectByRole(@Param("role") String role);

    /** 按分路筛选：对抗路 / 打野 / 中路 / 发育路 / 游走 */
    @Select("SELECT * FROM wzry_hero WHERE lane = #{lane} ORDER BY id")
    List<WzHero> selectByLane(@Param("lane") String lane);

    /** 按英雄名或称号模糊搜索 */
    @Select("SELECT * FROM wzry_hero " +
            "WHERE name LIKE CONCAT('%', #{keyword}, '%') " +
            "   OR title LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY id")
    List<WzHero> search(@Param("keyword") String keyword);

    /** 随机推荐若干英雄 */
    @Select("SELECT * FROM wzry_hero ORDER BY RAND() LIMIT #{limit}")
    List<WzHero> selectRandom(@Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM wzry_hero")
    int countAll();

    /** 按职业统计英雄数量 */
    @Select("SELECT role AS name, COUNT(*) AS count " +
            "FROM wzry_hero " +
            "GROUP BY role " +
            "ORDER BY count DESC")
    List<Map<String, Object>> countByRole();

    /** 按分路统计英雄数量 */
    @Select("SELECT lane AS name, COUNT(*) AS count " +
            "FROM wzry_hero " +
            "GROUP BY lane " +
            "ORDER BY count DESC")
    List<Map<String, Object>> countByLane();
}
