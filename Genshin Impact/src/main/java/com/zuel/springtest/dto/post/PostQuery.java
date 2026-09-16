package com.zuel.springtest.dto.post;

import lombok.Data;

/**
 * 帖子列表查询条件
 *
 * <p>以 query 参数形式接收：{@code /api/posts?boardId=7&keyword=xx&sort=hot&page=1&size=10}
 */
@Data
public class PostQuery {

    /** 板块 ID，为空表示全部 */
    private Integer boardId;

    /** 社区 ID，为空表示不限；指定时按社区聚合其下所有板块的帖子（如 community_id=2 火影忍者） */
    private Integer communityId;

    /** 标题/内容模糊搜索关键词 */
    private String keyword;

    /** 作者 ID，用于「我的帖子」 */
    private Long authorId;

    /** 排序：hot=热度优先，latest（默认）=最新发布 */
    private String sort = "latest";

    /** 页码，从 1 开始 */
    private Integer page = 1;

    /** 每页条数 */
    private Integer size = 10;

    /**
     * 归一化分页参数，防止非法值导致 SQL 异常
     */
    public void normalize() {
        if (this.page == null || this.page < 1) {
            this.page = 1;
        }
        if (this.size == null || this.size < 1) {
            this.size = 10;
        }
        if (this.size > 100) {
            this.size = 100;
        }
        if (this.sort == null || this.sort.isBlank()) {
            this.sort = "latest";
        }
    }
}
