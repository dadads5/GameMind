package com.zuel.springtest.controller;

import com.zuel.springtest.common.Result;
import com.zuel.springtest.services.WzHeroService;
import com.zuel.springtest.vo.WzHeroVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 王者荣耀英雄接口
 *
 * <p>前端 baseURL 为 /api，因此实际路径为 /api/wzry/heroes
 */
@RestController
@RequestMapping("/api/wzry/heroes")
@RequiredArgsConstructor
public class WzHeroController {

    private final WzHeroService wzHeroService;

    /**
     * 英雄列表，支持职业 / 分路筛选与关键词搜索
     *
     * <p>示例：{@code GET /api/wzry/heroes?role=法师&lane=中路&keyword=妲己}
     */
    @GetMapping
    public Result<List<WzHeroVO>> listHeroes(@RequestParam(required = false) String role,
                                             @RequestParam(required = false) String lane,
                                             @RequestParam(required = false) String keyword) {
        return Result.success(wzHeroService.listHeroes(role, lane, keyword));
    }

    /** 随机推荐英雄 */
    @GetMapping("/recommend")
    public Result<List<WzHeroVO>> recommend(@RequestParam(defaultValue = "6") int limit) {
        return Result.success(wzHeroService.listRandom(limit));
    }

    /** 统计信息：总数 / 职业分布 / 分路分布 */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        return Result.success(wzHeroService.getStatistics());
    }

    /** 按 ID 查询 */
    @GetMapping("/{id}")
    public Result<WzHeroVO> getById(@PathVariable Integer id) {
        return Result.success(wzHeroService.getById(id));
    }

    /** 按名称查询 */
    @GetMapping("/name/{name}")
    public Result<WzHeroVO> getByName(@PathVariable String name) {
        return Result.success(wzHeroService.getByName(name));
    }
}
