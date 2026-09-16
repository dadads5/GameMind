package com.zuel.springtest.vo;

import com.zuel.springtest.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户视图对象
 *
 * <p>不含 password 等敏感字段。
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private String bio;
    /** 角色：0 普通用户，1 管理员 */
    private Integer role;
    /** 是否 VIP 用户：0 否，1 是 */
    private Integer vip;
    /** 主页被点赞总数 */
    private Integer likeCount;
    /** 当前登录用户今天是否已对该用户点赞（未登录为 null） */
    private Boolean todayLiked;
    private LocalDateTime createdAt;

    public static UserVO from(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setBio(user.getBio());
        vo.setRole(user.getRole());
        vo.setVip(user.getVip() != null ? user.getVip() : 0);
        vo.setLikeCount(user.getLikeCount());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
}
