package com.zuel.springtest.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改个人资料请求
 *
 * <p>所有字段均可选，传 null 表示不修改。
 */
@Data
public class UpdateProfileRequest {

    @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "用户名只能包含字母、数字、下划线，长度 3-20 位")
    private String username;

    @Size(max = 50, message = "昵称不能超过 50 个字符")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱不能超过 100 个字符")
    private String email;

    @Size(max = 255, message = "个人简介不能超过 255 个字符")
    private String bio;

    @Size(max = 255, message = "头像地址过长")
    private String avatar;
}
