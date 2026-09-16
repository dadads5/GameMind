package com.zuel.springtest.services;

import com.zuel.springtest.common.BusinessException;
import com.zuel.springtest.common.ResultCode;
import com.zuel.springtest.entity.WzHero;
import com.zuel.springtest.mapper.WzHeroMapper;
import com.zuel.springtest.vo.WzHeroVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 王者荣耀英雄业务
 */
@Service
@RequiredArgsConstructor
public class WzHeroService {

    private final WzHeroMapper wzHeroMapper;

    /**
     * 英雄列表
     *
     * @param role    职业筛选（坦克/战士/刺客/法师/射手/辅助），为空表示全部
     * @param lane    分路筛选（对抗路/打野/中路/发育路/游走），为空表示全部
     * @param keyword 英雄名/称号模糊搜索，为空表示不搜索
     */
    public List<WzHeroVO> listHeroes(String role, String lane, String keyword) {
        List<WzHero> heroes;
        if (StringUtils.hasText(keyword)) {
            heroes = wzHeroMapper.search(keyword.trim());
        } else if (StringUtils.hasText(role)) {
            heroes = wzHeroMapper.selectByRole(role.trim());
        } else if (StringUtils.hasText(lane)) {
            heroes = wzHeroMapper.selectByLane(lane.trim());
        } else {
            heroes = wzHeroMapper.selectAll();
        }
        return heroes.stream().map(WzHeroVO::from).toList();
    }

    public WzHeroVO getById(Integer id) {
        return WzHeroVO.from(wzHeroMapper.selectById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "英雄不存在")));
    }

    public WzHeroVO getByName(String name) {
        return WzHeroVO.from(wzHeroMapper.selectByName(name)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "英雄不存在")));
    }

    /** 随机推荐英雄 */
    public List<WzHeroVO> listRandom(int limit) {
        if (limit < 1 || limit > 20) {
            limit = 6;
        }
        return wzHeroMapper.selectRandom(limit).stream().map(WzHeroVO::from).toList();
    }

    /** 英雄统计：总数 + 职业分布 + 分路分布 */
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total", wzHeroMapper.countAll());
        statistics.put("byRole", wzHeroMapper.countByRole());
        statistics.put("byLane", wzHeroMapper.countByLane());
        return statistics;
    }

    public int countAll() {
        return wzHeroMapper.countAll();
    }
}
