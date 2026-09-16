package com.zuel.springtest.vo;

/**
 * 点赞操作结果
 *
 * @param liked     操作后当前用户是否已点赞
 * @param likeCount 操作后的点赞总数
 */
public record LikeResult(boolean liked, int likeCount) {
}
