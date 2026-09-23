package com.zuel.springtest.dto.ai;

import lombok.Data;

/**
 * RAG 检索命中的来源片段，回传给前端用于「可溯源」展示
 */
@Data
public class SourceChunk {
    private Long postId;
    private String title;
    private String content;
    private double score;
    /** 帖子所属社区 ID，用于前端跳转到 /community/:communityId/post/:id */
    private Integer communityId;

    public SourceChunk(Long postId, String title, String content, double score) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.score = score;
    }

    public SourceChunk(Long postId, String title, String content, double score, Integer communityId) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.score = score;
        this.communityId = communityId;
    }
}
