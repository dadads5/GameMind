package com.zuel.springtest.entity;

import lombok.Data;

/**
 * 社区 / IP 维度（原神、火影忍者等），板块归属到某个社区
 */
@Data
public class Community {

    private Integer id;
    private String name;
    private String icon;
    private String description;
    private Integer sort;
}
