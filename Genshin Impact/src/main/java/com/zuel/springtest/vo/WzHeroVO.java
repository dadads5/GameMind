package com.zuel.springtest.vo;

import com.zuel.springtest.entity.WzHero;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 王者荣耀英雄视图对象
 */
@Data
public class WzHeroVO {

    private Integer id;
    private String name;
    private String title;
    private String role;
    private String lane;
    private Integer difficulty;
    private Integer priceGold;
    private Integer priceCoupon;
    private String icon;
    private String avatar;
    private String skills;
    private String builds;
    private String runes;
    private String tips;
    private LocalDateTime createdAt;

    public static WzHeroVO from(WzHero hero) {
        if (hero == null) {
            return null;
        }
        WzHeroVO vo = new WzHeroVO();
        vo.setId(hero.getId());
        vo.setName(hero.getName());
        vo.setTitle(hero.getTitle());
        vo.setRole(hero.getRole());
        vo.setLane(hero.getLane());
        vo.setDifficulty(hero.getDifficulty());
        vo.setPriceGold(hero.getPriceGold());
        vo.setPriceCoupon(hero.getPriceCoupon());
        vo.setIcon(hero.getIcon());
        vo.setAvatar(hero.getAvatar());
        vo.setSkills(hero.getSkills());
        vo.setBuilds(hero.getBuilds());
        vo.setRunes(hero.getRunes());
        vo.setTips(hero.getTips());
        vo.setCreatedAt(hero.getCreatedAt());
        return vo;
    }
}
