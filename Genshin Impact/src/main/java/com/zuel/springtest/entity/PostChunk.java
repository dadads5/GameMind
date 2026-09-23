package com.zuel.springtest.entity;

import lombok.Data;

/**
 * 帖子知识片段（RAG 索引单元）
 *
 * <p>一条帖子可切分为多个片段，目前按「一帖一片」存储，chunkIndex 预留以支持后续细粒度切分。
 * embedding 以逗号分隔的浮点串存储（维度由 {@link com.zuel.springtest.config.EmbeddingProperties} 决定）。
 */
@Data
public class PostChunk {
    private Long id;
    private Long postId;
    private String title;
    private int chunkIndex;
    private String content;
    private String embedding;
}
