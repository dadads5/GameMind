package com.zuel.springtest.entity;

import lombok.Data;

/**
 * 帖子配图（一对多，替代原 post.images 逗号串）
 */
@Data
public class PostImage {

    private Long id;
    private Long postId;
    private String url;
    private Integer sort;
}
