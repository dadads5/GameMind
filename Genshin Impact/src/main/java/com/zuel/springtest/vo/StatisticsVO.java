package com.zuel.springtest.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 平台统计数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsVO {

    // ---------- 用户 ----------
    private Integer totalUsers;
    private Integer todayNewUsers;

    // ---------- 帖子 ----------
    private Integer totalPosts;
    private Integer todayPosts;
    private Integer totalViews;

    // ---------- 互动 ----------
    private Integer totalComments;
    private Integer todayComments;

    // ---------- 实时 ----------
    /** 当前在线用户数（Redis ZSet 实时统计，Redis 不可用时为 0） */
    private Integer onlineUsers;
}
