package com.zuel.springtest.vo;

import com.zuel.springtest.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端用户视图对象
 *
 * <p>与 {@link UserVO} 的区别：包含 status / role 管理字段，且不含 password 等敏感信息。
 */
@Data
public class AdminUserVO {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    /** 账号状态：1 正常，0 禁用 */
    private Integer status;
    /** 角色：0 普通用户，1 管理员 */
    private Integer role;
    /** 是否 VIP 用户：0 否，1 是 */
    private Integer vip;
    private LocalDateTime createdAt;

    public static AdminUserVO from(User user) {
        if (user == null) {
            return null;
        }
        AdminUserVO vo = new AdminUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setRole(user.getRole());
        vo.setVip(user.getVip() != null ? user.getVip() : 0);
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
}
