package com.zuel.springtest.controller;

import com.zuel.springtest.common.Result;
import com.zuel.springtest.security.CurrentUser;
import com.zuel.springtest.security.LoginUser;
import com.zuel.springtest.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 站内消息通知接口（需登录）
 *
 * <p>前端 baseURL 为 /api，实际路径 /api/notifications。
 * 第一期采用轮询方式：进入页面拉取列表，定时拉取未读数刷新红点。
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 消息列表（同时返回总数与未读数，便于前端一次渲染）
     */
    @GetMapping
    public Result<Map<String, Object>> list(@CurrentUser LoginUser loginUser,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        Long userId = loginUser.getId();
        List<?> items = notificationService.list(userId, page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("list", items);
        data.put("total", notificationService.countByUser(userId));
        data.put("unread", notificationService.countUnread(userId));
        return Result.success(data);
    }

    /** 未读数量（红点用，轮询目标） */
    @GetMapping("/unread-count")
    public Result<Integer> unreadCount(@CurrentUser LoginUser loginUser) {
        return Result.success(notificationService.countUnread(loginUser.getId()));
    }

    /** 单条标记已读 */
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id, @CurrentUser LoginUser loginUser) {
        notificationService.markRead(loginUser.getId(), id);
        return Result.success("已读", null);
    }

    /** 全部标记已读 */
    @PutMapping("/read-all")
    public Result<Void> markAllRead(@CurrentUser LoginUser loginUser) {
        notificationService.markAllRead(loginUser.getId());
        return Result.success("已全部标记已读", null);
    }

    /** 删除单条消息 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @CurrentUser LoginUser loginUser) {
        notificationService.delete(loginUser.getId(), id);
        return Result.success("已删除", null);
    }
}
