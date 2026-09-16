package com.zuel.springtest.controller;

import com.zuel.springtest.common.Result;
import com.zuel.springtest.services.StatisticsService;
import com.zuel.springtest.vo.StatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 平台统计接口
 *
 * <p>首页一次请求即可拿到用户 / 帖子 / 互动三维度数据，
 * 取代原先分散在 users、posts、comments 三个接口的实现。
 * 统计结果由 {@link StatisticsService} 缓存，避免每次请求多次全表 COUNT。
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping
    public Result<StatisticsVO> overview() {
        return Result.success(statisticsService.overview());
    }

    /**
     * 在线人数（轻量端点）
     *
     * <p>前端首页仅展示一个数字，无需拉取整份统计总览；
     * 该端点只算在线用户数，开销远低于 {@code /overview}。
     */
    @GetMapping("/online")
    public Result<Map<String, Integer>> online() {
        return Result.success(Map.of("onlineUsers", statisticsService.onlineCount()));
    }
}
