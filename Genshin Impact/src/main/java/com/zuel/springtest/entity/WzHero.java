package com.zuel.springtest.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 王者荣耀英雄实体（对应 wzry_hero 表）
 *
 * <p>字段与表完全一致：
 * id / name / title / role / lane / difficulty / price_gold / price_coupon
 * / icon / avatar / skills / builds / runes / tips / created_at
 */
@Data
public class WzHero {

    private Integer id;
    /** 英雄名，如「亚瑟」 */
    private String name;
    /** 称号，如「圣骑士」 */
    private String title;
    /** 职业：坦克 / 战士 / 刺客 / 法师 / 射手 / 辅助 */
    private String role;
    /** 分路：对抗路 / 打野 / 中路 / 发育路 / 游走 */
    private String lane;
    /** 上手难度 1~10 */
    private Integer difficulty;
    /** 金币价格 */
    private Integer priceGold;
    /** 点券价格 */
    private Integer priceCoupon;
    /** 头像用单字 */
    private String icon;
    /** 头像图片地址（可空，前端会回退到首字母头像） */
    private String avatar;
    /** 技能描述（被动 + 三个技能，多行文本） */
    private String skills;
    /** 推荐出装 */
    private String builds;
    /** 推荐铭文 */
    private String runes;
    /** 玩法技巧 */
    private String tips;
    private LocalDateTime createdAt;
}
