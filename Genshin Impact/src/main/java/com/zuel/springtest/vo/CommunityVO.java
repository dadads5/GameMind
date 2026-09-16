package com.zuel.springtest.vo;

import com.zuel.springtest.entity.Community;
import lombok.Data;

/**
 * 社区视图对象
 */
@Data
public class CommunityVO {

    private Integer id;
    private String name;
    private String icon;
    private String description;
    private Integer sort;

    public static CommunityVO from(Community community) {
        if (community == null) {
            return null;
        }
        CommunityVO vo = new CommunityVO();
        vo.setId(community.getId());
        vo.setName(community.getName());
        vo.setIcon(community.getIcon());
        vo.setDescription(community.getDescription());
        vo.setSort(community.getSort());
        return vo;
    }
}
