package com.zuel.springtest.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户
 *
 * <p>密码字段存储 BCrypt 哈希值，永不明文存储。
 */
@Data
public class User {

    private Long id;
    private String username;
    /** BCrypt 哈希后的密码 */
    private String password;
    private String email;
    private String nickname;
    private String avatar;
    /** 个人简介 */
    private String bio;
    /** 账号状态：1 正常，0 禁用 */
    private Integer status;
    /** 角色：0 普通用户，1 管理员 */
    private Integer role;
    /** 是否 VIP 用户：0 否，1 是（由管理员在后台设置） */
    private Integer vip;
    /** 主页被点赞总数 */
    private Integer likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
