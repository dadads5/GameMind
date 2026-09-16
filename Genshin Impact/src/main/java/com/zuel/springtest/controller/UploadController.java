package com.zuel.springtest.controller;

import com.zuel.springtest.common.Result;
import com.zuel.springtest.security.CurrentUser;
import com.zuel.springtest.security.LoginUser;
import com.zuel.springtest.services.FileStorageService;
import com.zuel.springtest.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 文件上传接口
 */
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final FileStorageService fileStorageService;
    private final UserService userService;

    /**
     * 上传头像，上传成功后自动更新当前用户资料
     */
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file,
                                                    @CurrentUser LoginUser loginUser) {
        String url = fileStorageService.store(file, "avatars");
        userService.updateAvatar(loginUser.getId(), url);
        return Result.success("头像已更新", Map.of("url", url));
    }

    /**
     * 上传帖子配图
     */
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file,
                                                   @CurrentUser(required = false) LoginUser loginUser) {
        String url = fileStorageService.store(file, "posts");
        return Result.success("上传成功", Map.of("url", url));
    }
}
