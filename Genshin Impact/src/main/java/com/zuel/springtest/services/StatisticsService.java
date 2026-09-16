package com.zuel.springtest.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zuel.springtest.mapper.CommentMapper;
import com.zuel.springtest.mapper.PostMapper;
import com.zuel.springtest.mapper.UserMapper;
import com.zuel.springtest.vo.StatisticsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 平台统计业务
 *
 * <p>统计接口原本每次请求都要执行 5 次全表 COUNT，这里用 Redis 缓存 60 秒：
 * 数据变更后由各业务方调用 {@link #evict()} 主动失效，兼顾实时性与数据库压力。
 *
 * <p>Redis 不可用时自动降级为直接查库，不影响主流程。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    /** 统计总览缓存 key */
    private static final String CACHE_KEY = "stats:overview";
    private static final Duration CACHE_TTL = Duration.ofSeconds(60);
    /** 在线用户 ZSet */
    private static final String ONLINE_KEY = "online:users";

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedis;

    /**
     * 获取平台统计总览（优先读缓存）
     */
    public StatisticsVO overview() {
        StatisticsVO statistics;
        try {
            Object cached = redisTemplate.opsForValue().get(CACHE_KEY);
            if (cached != null) {
                statistics = objectMapper.convertValue(cached, StatisticsVO.class);
            } else {
                statistics = queryFromDb();
                try {
                    redisTemplate.opsForValue().set(CACHE_KEY, statistics, CACHE_TTL);
                } catch (Exception e) {
                    log.warn("写入统计缓存失败", e);
                }
            }
        } catch (Exception e) {
            log.warn("读取统计缓存失败，回退数据库查询", e);
            statistics = queryFromDb();
        }
        // 在线人数为实时数据，不随统计缓存（60 秒）过期
        statistics.setOnlineUsers(onlineUsers());
        return statistics;
    }

    /**
     * 统计数据发生变更后清除缓存，使下一次请求重新统计
     */
    public void evict() {
        try {
            redisTemplate.delete(CACHE_KEY);
        } catch (Exception e) {
            log.warn("清除统计缓存失败", e);
        }
    }

    /**
     * 当前在线用户数（实时计算，不读 60 秒统计缓存）
     */
    public int onlineCount() {
        return onlineUsers();
    }

    /** 在线用户活跃窗口：超过该时长未活跃则视为离线 */
    private static final Duration ONLINE_WINDOW = Duration.ofMinutes(5);

    /**
     * 当前在线用户数：统计最近 {@code ONLINE_WINDOW} 内活跃过的登录用户，
     * 并清理过期成员避免 ZSet 无限膨胀。Redis 不可用时为 0。
     */
    private int onlineUsers() {
        try {
            long threshold = System.currentTimeMillis() - ONLINE_WINDOW.toMillis();
            // 先剔除窗口外未活跃的旧成员（score 为最后活跃时间戳）
            stringRedis.opsForZSet().removeRangeByScore(ONLINE_KEY, 0, threshold);
            Long count = stringRedis.opsForZSet().zCard(ONLINE_KEY);
            return count == null ? 0 : count.intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    private StatisticsVO queryFromDb() {
        return StatisticsVO.builder()
                .totalUsers(userMapper.countAll())
                .todayNewUsers(userMapper.countToday())
                .totalPosts(postMapper.countAll())
                .todayPosts(postMapper.countToday())
                .totalViews(postMapper.sumViewCount())
                .totalComments(commentMapper.countAll())
                .todayComments(commentMapper.countToday())
                .build();
    }
}
