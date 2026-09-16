package com.zuel.springtest.security;

import com.zuel.springtest.common.BusinessException;
import com.zuel.springtest.common.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 解析 {@code @CurrentUser LoginUser} 参数
 *
 * <p>用户信息由 {@link JwtAuthenticationInterceptor} 预先写入请求属性。
 */
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    /** 存放当前登录用户的请求属性名 */
    public static final String CURRENT_USER_ATTRIBUTE = "currentUser";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && LoginUser.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Object user = request.getAttribute(CURRENT_USER_ATTRIBUTE);
        if (user == null) {
            CurrentUser annotation = parameter.getParameterAnnotation(CurrentUser.class);
            if (annotation != null && !annotation.required()) {
                return null;
            }
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        return user;
    }
}
