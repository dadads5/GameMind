package com.zuel.springtest.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求
 */
@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /** 记住我：勾选后 Token 有效期延长至 7 天 */
    private Boolean rememberMe = false;
}
