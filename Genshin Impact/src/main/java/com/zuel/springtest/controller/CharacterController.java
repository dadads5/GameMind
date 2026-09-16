package com.zuel.springtest.controller;

import com.zuel.springtest.common.Result;
import com.zuel.springtest.services.CharacterService;
import com.zuel.springtest.vo.CharacterVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 原神角色接口
 */
@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
public class CharacterController {

    private final CharacterService characterService;

    /**
     * 角色列表，支持元素筛选与关键词搜索
     *
     * <p>示例：{@code GET /api/characters?element=火&keyword=主C}
     */
    @GetMapping
    public Result<List<CharacterVO>> listCharacters(@RequestParam(required = false) String element,
                                                    @RequestParam(required = false) String keyword) {
        return Result.success(characterService.listCharacters(element, keyword));
    }

    /** 随机推荐角色 */
    @GetMapping("/recommend")
    public Result<List<CharacterVO>> recommend(@RequestParam(defaultValue = "6") int limit) {
        return Result.success(characterService.listRandom(limit));
    }

    /** 统计信息 */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        return Result.success(characterService.getStatistics());
    }

    /** 按 ID 查询 */
    @GetMapping("/{id}")
    public Result<CharacterVO> getById(@PathVariable Integer id) {
        return Result.success(characterService.getById(id));
    }

    /** 按名称查询 */
    @GetMapping("/name/{name}")
    public Result<CharacterVO> getByName(@PathVariable String name) {
        return Result.success(characterService.getByName(name));
    }
}
