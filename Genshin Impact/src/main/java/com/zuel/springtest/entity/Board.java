package com.zuel.springtest.entity;

import lombok.Data;

/**
 * 论坛板块
 */
@Data
public class Board {

    private Integer id;
    /** 所属社区：1原神 2火影忍者（见 community 表） */
    private Integer communityId;
    private String name;
    private String icon;
    private String description;
    private Integer sort;
}
