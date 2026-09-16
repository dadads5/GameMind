package com.zuel.springtest;

import com.zuel.springtest.entity.User;
import com.zuel.springtest.mapper.UserMapper;
import com.zuel.springtest.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 存量密码 BCrypt 迁移（一次性任务）
 *
 * <p>历史数据中密码为明文或伪造的 {@code $2a$10$xxx} 字符串，无法还原。
 * 该任务把非 BCrypt 的密码统一重置为初始密码并用 BCrypt 重新加密。
 *
 * <p><b>使用方式</b>：仅在需要时以 migrate profile 启动一次
 * <pre>
 *   mvn spring-boot:run -Dspring-boot.run.profiles=migrate
 *   # 或
 *   java -jar app.jar --spring.profiles.active=migrate
 * </pre>
 *
 * <p>迁移完成后请以正常 profile 重启服务，并通知相关用户修改密码。
 */
@Slf4j
@Component
@Profile("migrate")
@RequiredArgsConstructor
public class PasswordMigrationRunner implements CommandLineRunner {

    private final UserMapper userMapper;
    private final UserService userService;

    /** 迁移后统一的初始密码，可通过配置覆盖 */
    @Value("${app.migrate.default-password:123456}")
    private String defaultPassword;

    @Override
    public void run(String... args) {
        List<User> users = userMapper.selectAll();
        int migrated = 0;

        for (User user : users) {
            boolean changed = userService.migratePasswordToBcrypt(user.getId(), defaultPassword);
            if (changed) {
                migrated++;
                log.warn("用户 [{}]（id={}）的密码已重置为初始密码", user.getUsername(), user.getId());
            }
        }

        log.warn("===== 密码迁移完成：共 {} 个用户，本次迁移 {} 个，初始密码为 [{}] =====",
                users.size(), migrated, defaultPassword);
        log.warn("===== 请通知相关用户尽快修改密码，并以正常 profile 重启服务 =====");
    }
}
