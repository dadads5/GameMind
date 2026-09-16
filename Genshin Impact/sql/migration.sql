-- ============================================================
-- 论坛规范化改造：数据库迁移脚本（idempotent，可重复执行）
-- ============================================================
-- 适用：修复阶段的 4 个数据/安全问题
--   1. comment.like_count 缺失列（原代码直接 SELECT 该列导致报错）
--   2. 新增 post_like / comment_like 点赞表，实现「一人一赞、可取消」
--   3. user.updated_at 缺失列（资料更新使用）
--   4. （可选）将存量明文密码重置为 BCrypt，配合后端 migrate profile 使用
--
-- 用法：
--   mysql -u root -p < migration.sql
--   或在 Navicat / DBeaver 中全选执行。
--
-- 说明：
--   * 脚本默认针对库名 `yxy`（与 application.yml 的 DB_URL 一致）。
--     若你的库名是 `springtest` 或其他，请修改下面的 @schema 变量。
--   * 所有 ALTER / CREATE 均通过 INFORMATION_SCHEMA 判断，已存在则跳过，不会报错。
-- ============================================================

SET @schema = 'yxy';   -- ← 若你的数据库名不同，请改成实际库名

-- 1) comment 增加 like_count 列 -----------------------------------
SET @cnt = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema AND TABLE_NAME = 'comment' AND COLUMN_NAME = 'like_count');
SET @sql = IF(@cnt = 0,
    CONCAT('ALTER TABLE `', @schema, '`.`comment` ADD COLUMN `like_count` INT NOT NULL DEFAULT 0 COMMENT ''点赞数'''),
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2) post_like 表（帖子点赞） -------------------------------------
SET @tbl = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
            WHERE TABLE_SCHEMA = @schema AND TABLE_NAME = 'post_like');
SET @sql = IF(@tbl = 0,
    CONCAT('CREATE TABLE `', @schema, '`.`post_like` (',
           '`id` BIGINT NOT NULL AUTO_INCREMENT,',
           '`post_id` BIGINT NOT NULL,',
           '`user_id` BIGINT NOT NULL,',
           '`created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,',
           'PRIMARY KEY (`id`),',
           'UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),',
           'KEY `idx_post_like_post` (`post_id`)',
           ') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4'),
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3) comment_like 表（评论点赞） ----------------------------------
SET @tbl = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
            WHERE TABLE_SCHEMA = @schema AND TABLE_NAME = 'comment_like');
SET @sql = IF(@tbl = 0,
    CONCAT('CREATE TABLE `', @schema, '`.`comment_like` (',
           '`id` BIGINT NOT NULL AUTO_INCREMENT,',
           '`comment_id` BIGINT NOT NULL,',
           '`user_id` BIGINT NOT NULL,',
           '`created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,',
           'PRIMARY KEY (`id`),',
           'UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),',
           'KEY `idx_comment_like_comment` (`comment_id`)',
           ') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4'),
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4) user 增加 updated_at 列 -------------------------------------
SET @cnt = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema AND TABLE_NAME = 'user' AND COLUMN_NAME = 'updated_at');
SET @sql = IF(@cnt = 0,
    CONCAT('ALTER TABLE `', @schema, '`.`user` ADD COLUMN `updated_at` DATETIME DEFAULT NULL'),
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 5) 外键约束（数据完整性，ON DELETE CASCADE） -------------------
--    若历史数据存在孤儿记录导致创建失败，可忽略本段，服务层已显式清理子表。
SET @fk = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS
           WHERE CONSTRAINT_SCHEMA = @schema AND CONSTRAINT_NAME = 'fk_comment_post');
SET @sql = IF(@fk = 0,
    CONCAT('ALTER TABLE `', @schema, '`.`comment` ',
           'ADD CONSTRAINT `fk_comment_post` FOREIGN KEY (`post_id`) REFERENCES `', @schema, '`.`post` (`id`) ON DELETE CASCADE'),
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @fk = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS
           WHERE CONSTRAINT_SCHEMA = @schema AND CONSTRAINT_NAME = 'fk_post_like_post');
SET @sql = IF(@fk = 0,
    CONCAT('ALTER TABLE `', @schema, '`.`post_like` ',
           'ADD CONSTRAINT `fk_post_like_post` FOREIGN KEY (`post_id`) REFERENCES `', @schema, '`.`post` (`id`) ON DELETE CASCADE'),
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @fk = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS
           WHERE CONSTRAINT_SCHEMA = @schema AND CONSTRAINT_NAME = 'fk_comment_like_comment');
SET @sql = IF(@fk = 0,
    CONCAT('ALTER TABLE `', @schema, '`.`comment_like` ',
           'ADD CONSTRAINT `fk_comment_like_comment` FOREIGN KEY (`comment_id`) REFERENCES `', @schema, '`.`comment` (`id`) ON DELETE CASCADE'),
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT '迁移脚本执行完成 ✅' AS result;
