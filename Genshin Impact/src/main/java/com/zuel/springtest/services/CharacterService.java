package com.zuel.springtest.services;

import com.zuel.springtest.common.BusinessException;
import com.zuel.springtest.common.ResultCode;
import com.zuel.springtest.entity.Character;
import com.zuel.springtest.mapper.CharacterMapper;
import com.zuel.springtest.vo.CharacterVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 原神角色业务
 */
@Service
@RequiredArgsConstructor
public class CharacterService {

    private final CharacterMapper characterMapper;

    /**
     * 角色列表
     *
     * @param element 元素筛选，为空表示全部
     * @param keyword 名称/副标题模糊搜索，为空表示不搜索
     */
    public List<CharacterVO> listCharacters(String element, String keyword) {
        List<Character> characters;
        if (StringUtils.hasText(keyword)) {
            characters = characterMapper.search(keyword.trim());
        } else if (StringUtils.hasText(element)) {
            characters = characterMapper.selectByElement(element.trim());
        } else {
            characters = characterMapper.selectAll();
        }
        return characters.stream().map(CharacterVO::from).toList();
    }

    public CharacterVO getById(Integer id) {
        return CharacterVO.from(characterMapper.selectById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "角色不存在")));
    }

    public CharacterVO getByName(String name) {
        return CharacterVO.from(characterMapper.selectByName(name)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "角色不存在")));
    }

    /**
     * 随机推荐角色
     */
    public List<CharacterVO> listRandom(int limit) {
        if (limit < 1 || limit > 20) {
            limit = 6;
        }
        return characterMapper.selectRandom(limit).stream().map(CharacterVO::from).toList();
    }

    /**
     * 角色统计：总数 + 各元素分布
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total", characterMapper.countAll());
        statistics.put("byElement", characterMapper.countByElement());
        return statistics;
    }

    public int countAll() {
        return characterMapper.countAll();
    }
}
