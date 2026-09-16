package com.zuel.springtest.vo;

import com.zuel.springtest.common.BoardEnum;
import com.zuel.springtest.entity.Post;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 帖子视图对象
 */
@Data
public class PostVO {

    private Long id;
    /** 板块 ID */
    private Integer boardId;
    /** 板块名称 */
    private String boardName;
    private String title;
    private String content;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    /** 当前登录用户是否已点赞（未登录时为 false） */
    private Boolean liked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    /** 配图 URL 列表（由 post_image 表关联加载） */
    private List<String> images;

    // ---------- 作者信息 ----------
    private Long authorId;
    private String authorName;
    private String authorAvatar;
    /** 所属社区 ID（用于跳转社区帖子详情） */
    private Integer communityId;

    public static PostVO from(Post post) {
        if (post == null) {
            return null;
        }
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setBoardId(post.getBoardId());
        // 优先用 Service 层查出的真实板块名，枚举仅作兜底
        vo.setBoardName(post.getBoardName() != null ? post.getBoardName() : BoardEnum.getName(post.getBoardId()));
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setViewCount(post.getViewCount());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setLiked(post.getLiked() != null ? post.getLiked() : Boolean.FALSE);
        vo.setCreatedAt(post.getCreatedAt());
        vo.setUpdatedAt(post.getUpdatedAt());
        vo.setAuthorId(post.getUserId());
        vo.setAuthorName(post.getAuthorName());
        vo.setAuthorAvatar(post.getAuthorAvatar());
        vo.setCommunityId(post.getCommunityId());
        if (post.getImages() != null && !post.getImages().isEmpty()) {
            vo.setImages(post.getImages());
        } else {
            vo.setImages(Collections.emptyList());
        }
        return vo;
    }
}
