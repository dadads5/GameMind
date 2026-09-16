package com.zuel.springtest.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录/注册响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthVO {

    private String token;
    /** 固定为 Bearer */
    @Builder.Default
    private String tokenType = "Bearer";
    /** 有效期（秒） */
    private long expiresIn;
    private UserVO user;
}
