package com.zuel.springtest.vo;

import com.zuel.springtest.entity.Board;
import lombok.Data;

/**
 * 板块视图对象
 */
@Data
public class BoardVO {

    private Integer id;
    private Integer communityId;
    private String name;
    private String icon;
    private String description;
    private Integer sort;
    /** 今日该板块发帖数 */
    private Integer todayPostCount;

    public static BoardVO from(Board board) {
        if (board == null) {
            return null;
        }
        BoardVO vo = new BoardVO();
        vo.setId(board.getId());
        vo.setCommunityId(board.getCommunityId());
        vo.setName(board.getName());
        vo.setIcon(board.getIcon());
        vo.setDescription(board.getDescription());
        vo.setSort(board.getSort());
        return vo;
    }
}
