package com.zuel.springtest.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 当前登录用户
 *
 * <p>由 {@link JwtAuthenticationInterceptor} 解析 Token 后放入请求属性，
 * 再通过 {@code @CurrentUser} 注入到 Controller 方法参数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    /** 角色：0 普通用户，1 管理员 */
    private Integer role;
}
