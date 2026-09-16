package com.zuel.springtest.controller;

import com.zuel.springtest.common.Result;
import com.zuel.springtest.dto.auth.ChangePasswordRequest;
import com.zuel.springtest.dto.auth.LoginRequest;
import com.zuel.springtest.dto.auth.RegisterRequest;
import com.zuel.springtest.dto.auth.UpdateProfileRequest;
import com.zuel.springtest.security.CurrentUser;
import com.zuel.springtest.security.LoginUser;
import com.zuel.springtest.services.AuthService;
import com.zuel.springtest.services.UserService;
import com.zuel.springtest.vo.AuthVO;
import com.zuel.springtest.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证接口
 *
 * <p>用户身份一律由 JWT 拦截器解析后通过 {@code @CurrentUser} 注入，
 * 不再信任请求体中传入的 userId。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    /** 注册 */
    @PostMapping("/register")
    public Result<AuthVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success("注册成功", authService.register(request));
    }

    /** 登录 */
    @PostMapping("/login")
    public Result<AuthVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("登录成功", authService.login(request));
    }

    /** 退出登录：使当前 Token 失效（单设备登录），前端清除本地 Token 即可 */
    @PostMapping("/logout")
    public Result<Void> logout(@CurrentUser LoginUser loginUser) {
        authService.logout(loginUser.getId());
        return Result.success("已退出登录", null);
    }

    /** 当前登录用户资料 */
    @GetMapping("/me")
    public Result<UserVO> me(@CurrentUser LoginUser loginUser) {
        return Result.success(authService.getProfile(loginUser.getId()));
    }

    /** 修改资料（用户名 / 昵称 / 邮箱 / 简介 / 头像） */
    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@CurrentUser LoginUser loginUser,
                                        @Valid @RequestBody UpdateProfileRequest request) {
        return Result.success("资料已更新", authService.updateProfile(loginUser.getId(), request));
    }

    /** 修改密码 */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@CurrentUser LoginUser loginUser,
                                       @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(loginUser.getId(), request);
        return Result.success("密码修改成功", null);
    }

    /** 检查用户名是否可用（注册页使用，无需登录） */
    @GetMapping("/check-username")
    public Result<Map<String, Object>> checkUsername(@RequestParam String username) {
        boolean available = !userService.existsByUsername(username);
        return Result.success(Map.of(
                "available", available,
                "username", username
        ));
    }
}
