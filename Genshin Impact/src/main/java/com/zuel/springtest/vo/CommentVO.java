package com.zuel.springtest.vo;

import com.zuel.springtest.entity.Comment;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论视图对象
 */
@Data
public class CommentVO {

    private Long id;
    private Long postId;
    /** 所属帖子标题（仅「我的评论」场景返回） */
    private String postTitle;
    private String content;
    private Integer likeCount;
    /** 当前登录用户是否已点赞（未登录时为 false） */
    private Boolean liked;
    private LocalDateTime createdAt;

    // ---------- 作者信息 ----------
    private Long authorId;
    private String authorName;
    private String authorAvatar;

    public static CommentVO from(Comment comment) {
        if (comment == null) {
            return null;
        }
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setPostId(comment.getPostId());
        vo.setPostTitle(comment.getPostTitle());
        vo.setContent(comment.getContent());
        vo.setLikeCount(comment.getLikeCount());
        vo.setLiked(comment.getLiked() != null ? comment.getLiked() : Boolean.FALSE);
        vo.setCreatedAt(comment.getCreatedAt());
        vo.setAuthorId(comment.getUserId());
        vo.setAuthorName(comment.getAuthorName());
        vo.setAuthorAvatar(comment.getAuthorAvatar());
        return vo;
    }
}
