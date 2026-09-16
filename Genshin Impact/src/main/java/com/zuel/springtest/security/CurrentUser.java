package com.zuel.springtest.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在 Controller 方法参数上，注入当前登录用户 {@link LoginUser}
 *
 * <p>默认要求已登录（接口不在白名单内，或请求携带有效 Token），未登录时抛出 401。
 * 对于「登录了展示更多信息、未登录也能看」的公开接口，可设置 {@code required = false}，
 * 此时未登录注入 null。
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {

    /**
     * 是否必须登录
     */
    boolean required() default true;
}
