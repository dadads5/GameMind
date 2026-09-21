-- AI 会话持久化表（智能问答多轮历史服务端存储）
-- 执行方式：在 yxy 库中执行本文件（如：mysql -u root -p yxy < ai_conversation.sql）

CREATE TABLE IF NOT EXISTS ai_conversation (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT       NOT NULL COMMENT '所属用户',
  title       VARCHAR(200)          COMMENT '会话标题，取首条提问前 20 字',
  created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user (user_id),
  INDEX idx_updated (updated_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'AI 会话';

CREATE TABLE IF NOT EXISTS ai_message (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  conversation_id BIGINT   NOT NULL COMMENT '所属会话',
  role            VARCHAR(20) NOT NULL COMMENT 'user / assistant',
  content         TEXT                 COMMENT '消息内容',
  created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_conv (conversation_id),
  CONSTRAINT fk_ai_msg_conv
    FOREIGN KEY (conversation_id) REFERENCES ai_conversation (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'AI 会话消息';
