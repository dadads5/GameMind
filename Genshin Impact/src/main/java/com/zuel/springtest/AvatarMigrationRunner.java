package com.zuel.springtest;

import com.zuel.springtest.entity.User;
import com.zuel.springtest.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 存量用户随机头像分配（一次性任务）
 *
 * <p>将 {@code public/PFP} 下的图片随机分配给尚未配置有效头像的用户，
 * 避免前端显示破图。仅覆盖以下情况，保留用户自行上传的真实头像：
 * <ul>
 *   <li>头像为空（null / 空串）</li>
 *   <li>头像仍是旧 mock 路径（以 {@code /PFP/} 开头但文件不存在）</li>
 * </ul>
 * 以 {@code /uploads/} 或 http(s) 开头的真实头像不会被覆盖。
 *
 * <p><b>使用方式</b>：与密码迁移一同，以 migrate profile 启动一次即可
 * <pre>
 *   mvn spring-boot:run -Dspring-boot.run.profiles=migrate
 *   # 或
 *   java -jar app.jar --spring.profiles.active=migrate
 * </pre>
 */
@Slf4j
@Component
@Profile("migrate")
@RequiredArgsConstructor
public class AvatarMigrationRunner implements CommandLineRunner {

    private final UserMapper userMapper;

    /** public/PFP 下可用的头像图片（对应前端访问路径 /PFP/xxx.jpg） */
    private static final String[] PFP_AVATARS = {
            "/PFP/微信图片_20260428104345_66_110.jpg",
            "/PFP/微信图片_20260428104345_67_110.jpg",
            "/PFP/微信图片_20260428104347_68_110.jpg",
            "/PFP/微信图片_20260428104348_69_110.jpg",
            "/PFP/微信图片_20260428104349_70_110.jpg",
            "/PFP/微信图片_20260428104351_71_110.jpg",
            "/PFP/微信图片_20260428104352_72_110.jpg",
            "/PFP/微信图片_20260428104353_73_110.jpg",
            "/PFP/微信图片_20260428104354_74_110.jpg",
            "/PFP/微信图片_20260428104356_75_110.jpg",
            "/PFP/微信图片_20260428104357_76_110.jpg",
    };

    @Override
    public void run(String... args) {
        List<User> users = userMapper.selectAll();
        int assigned = 0;

        for (User user : users) {
            if (needsAvatar(user.getAvatar())) {
                String avatar = PFP_AVATARS[ThreadLocalRandom.current().nextInt(PFP_AVATARS.length)];
                userMapper.updateAvatar(user.getId(), avatar);
                assigned++;
                log.warn("用户 [{}]（id={}）已分配随机头像：{}", user.getUsername(), user.getId(), avatar);
            }
        }

        log.warn("===== 头像随机分配完成：共 {} 个用户，本次分配 {} 个 =====", users.size(), assigned);
    }

    /** 是否需要分配头像：空值，或以旧 mock 路径 /PFP/ 开头（排除真实的 /uploads/ 与 http 头像） */
    private boolean needsAvatar(String avatar) {
        if (avatar == null || avatar.isBlank()) {
            return true;
        }
        return avatar.startsWith("/PFP/");
    }
}
