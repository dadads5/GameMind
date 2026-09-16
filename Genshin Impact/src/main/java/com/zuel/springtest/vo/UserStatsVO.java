package com.zuel.springtest.vo;

import lombok.Data;

/**
 * 用户活跃度统计
 *
 * <p>用于前端计算用户等级与徽章，不含敏感信息。
 */
@Data
public class UserStatsVO {

    /** 发帖总数 */
    private Integer totalPosts;
    /** 回复总数 */
    private Integer totalReplies;
    /** 帖子获赞总数 */
    private Integer totalLikes;
    /** 主页被点赞总数 */
    private Integer homeLikeCount;

    public static UserStatsVO of(int totalPosts, int totalReplies, int totalLikes, int homeLikeCount) {
        UserStatsVO vo = new UserStatsVO();
        vo.setTotalPosts(totalPosts);
        vo.setTotalReplies(totalReplies);
        vo.setTotalLikes(totalLikes);
        vo.setHomeLikeCount(homeLikeCount);
        return vo;
    }
}
