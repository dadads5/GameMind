package com.zuel.springtest.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子
 *
 * <p>{@code boardId} 为所属板块 ID，取值见 {@link com.zuel.springtest.common.BoardEnum}，
 * 对应 {@code board} 字典表（外键 fk_post_board）。
 */
@Data
public class Post {

    private Long id;
    private Long userId;
    /** 所属板块 ID：1蒙德 2璃月 3稻妻 4须弥 5枫丹 6纳塔 7火影忍者（及火影子板块 8~13） */
    private Integer boardId;
    private String title;
    private String content;
    private Integer viewCount = 0;
    private Integer likeCount = 0;
    private Integer commentCount = 0;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 是否置顶（管理端设置） */
    private Boolean isTop;
    /** 是否精华（管理端设置） */
    private Boolean isEssence;

    /** 配图 URL 列表（由 post_image 表关联加载，非数据库列） */
    private List<String> images;

    // ---------- 关联字段（由 JOIN 查询填充，非数据库列） ----------

    /** 作者用户名 */
    private String authorName;
    /** 作者头像 */
    private String authorAvatar;
    /** 板块名称（由 Service 层按 board_id 查 board 表填充） */
    private String boardName;
    /** 所属社区 ID（由 Service 层按 board_id 查 board 表填充，非数据库列） */
    private Integer communityId;
    /** 当前登录用户是否已点赞（由 Service 层按情况填充） */
    private Boolean liked;
}
