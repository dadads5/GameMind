package com.zuel.springtest.vo;

import com.zuel.springtest.entity.Character;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 原神角色视图对象
 */
@Data
public class CharacterVO {

    private Integer id;
    /** 角色名 */
    private String name;
    /** 头像用单字 */
    private String icon;
    /** 定位副标题，如「岩系月结晶主C」 */
    private String subtitle;
    /** 推荐武器，多个用顿号分隔 */
    private String weapons;
    /** 推荐圣遗物 */
    private String artifacts;
    /** 面板属性建议 */
    private String stats;
    /** 天赋加点建议 */
    private String talents;
    /** 元素属性：火 / 水 / 风 / 雷 / 草 / 冰 / 岩 */
    private String element;
    private LocalDateTime createdAt;

    public static CharacterVO from(Character character) {
        if (character == null) {
            return null;
        }
        CharacterVO vo = new CharacterVO();
        vo.setId(character.getId());
        vo.setName(character.getName());
        vo.setIcon(character.getIcon());
        vo.setSubtitle(character.getSubtitle());
        vo.setWeapons(character.getWeapons());
        vo.setArtifacts(character.getArtifacts());
        vo.setStats(character.getStats());
        vo.setTalents(character.getTalents());
        vo.setElement(character.getElement());
        vo.setCreatedAt(character.getCreatedAt());
        return vo;
    }
}
