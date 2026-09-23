# Gamemind 游戏社区平台

一个面向游戏爱好者的**多模块社区论坛平台**，采用前后端分离架构。除了完整的论坛能力（发帖、评论、点赞、板块、管理后台、站内通知），还集成了 AI 智能问答、AI 润写、AI 智能回复，以及原神伤害计算、抽卡模拟等游戏工具。

> 前端入口：`community/`（Vue 3 + Vite + TypeScript） ｜ 后端入口：`Genshin Impact/`（Spring Boot 4 + MyBatis）

---

## ✨ 核心功能

### 社区内容
- **多模块社区**：火影、卡牌、原神、英雄等多个题材板块，各自独立的首页 / 板块 / 列表 / 详情 / 发帖
- **内容互动**：发帖（支持配图）、评论、帖子与评论点赞、收藏、浏览量统计
- **热榜与最新**：综合浏览、点赞、评论权重的热榜（定时重建）

### AI 能力（三层场景化应用）
| 能力 | 场景 | 说明 |
|---|---|---|
| 智能问答 | `/ai-chat` | 多轮上下文对话，SSE 流式输出（打字机效果），支持随时中断 |
| AI 润写 | 发帖页 | 智能润色 / 更简洁 / 更专业 / 更生动 / 纠错 五种风格，原文对照、可编辑、一键替换 |
| AI 智能回复 | 帖子详情 | 依据标题与正文流式生成可参考回复，降低互动门槛 |

配套实现了 SSE 并发治理（有界线程池 + 信号量并发上限 + 全局心跳 + 超时兜底 + 幂等清理）与 Redis 限流（10 次/分钟/用户）。

### 用户与成长
- 注册登录（JWT + BCrypt）、单设备登录、登录失败计数防暴力破解
- **等级与徽章**：基于活跃度积分（`发帖×2 + 回复×1 + 获赞×0.5 + 主页获赞×2`）计算 Lv1–Lv10，自动发放管理员 / VIP / 老用户 / 活跃 / 人气王等徽章
- 个人主页、公开用户主页、主页点赞（每日一次）

### 管理与统计
- 管理后台：仪表盘、内容治理（置顶 / 精华 / 删除）、用户治理（启停 / 角色 / VIP）、板块与社区管理
- 站内通知：评论与点赞通知、未读数
- 实时在线人数（Redis ZSet + 5 分钟滑动窗口）、统计总览（Redis 缓存 60s）

### 游戏工具
原神伤害计算器、抽卡模拟器、角色配装、游戏资讯

---

## 🛠 技术栈

**前端**：Vue 3（Composition API）· TypeScript · Vite 8 · Pinia · Vue Router · Element Plus · Tailwind CSS 4 · Axios

**后端**：Spring Boot 4.0.3 · Java 17 · MyBatis · MySQL · Redis · JWT（jjwt）· Spring Security Crypto（BCrypt）· PageHelper · Validation · Lombok

**AI**：DeepSeek Chat Completions API（支持流式 SSE）

---

## 📁 项目结构

```
Total_ai/
├── community/                  # 前端 Vue 3 工程
│   └── src/
│       ├── api/                # 接口封装（request / ai / user 等）
│       ├── views/              # 页面（含 model2/3/4、naruto 等模块）
│       ├── components/         # 复用组件（AiPolish / AiReply 等）
│       ├── router/             # 路由（含登录守卫）
│       ├── stores/             # Pinia 状态
│       └── utils/              # 工具（notify / userRating）
├── Genshin Impact/             # 后端 Spring Boot 工程
│   └── src/main/java/com/zuel/springtest/
│       ├── controller/         # 13 个 REST Controller
│       ├── services/           # 业务逻辑
│       ├── mapper/             # MyBatis Mapper（注解式）
│       ├── entity/ / vo/ / dto/
│       ├── security/           # JWT 认证与拦截器
│       └── common/             # 统一响应与异常体系
├── allsql/                     # 数据库脚本
│   ├── yxy.sql                 # 主库建表脚本
│   └── ai_conversation.sql     # AI 会话表
└── *.md                        # 项目文档
```

---

## 🚀 快速开始

### 环境要求
JDK 17+ · Maven 3.8+ · Node.js 20+ · MySQL 8 · Redis

### 1. 初始化数据库
```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS yxy DEFAULT CHARSET utf8mb4;"
mysql -u root -p yxy < allsql/yxy.sql
mysql -u root -p yxy < allsql/ai_conversation.sql   # AI 会话表（必需）
```

### 2. 启动 Redis
```bash
redis-server
```

### 3. 启动后端（默认端口 8080）

仓库**不包含**本地敏感配置（数据库密码等），请二选一：

**方式 A：本地配置文件（推荐）**

在 `Genshin Impact/src/main/resources/` 下创建 `application-local.yml`（该文件已被 `.gitignore` 忽略，不会提交）：

```yaml
spring:
  datasource:
    username: root
    password: 你的密码
```

然后激活 `local` profile 启动：

```bash
cd "Genshin Impact"
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

> IDE 用户：在 Run Configuration 的 **Active profiles** 填 `local`，设置一次后长期生效。
> 也可以在 `application.yml` 中取消 `spring.profiles.active: local` 的注释，实现零参数启动。

**方式 B：环境变量**

```bash
# Windows PowerShell
$env:DB_PASSWORD = "你的密码"
$env:JWT_SECRET  = "至少64字节的随机字符串"

cd "Genshin Impact"
mvn spring-boot:run
```

### 4. 启动前端（默认端口 5173）
```bash
cd community
npm install
npm run dev
```

浏览器打开 http://localhost:5173 。前端已配置代理：`/api` 与 `/uploads` 转发至 `http://localhost:8080`。

---

## ⚙️ 环境变量

所有敏感配置均通过环境变量注入，**不要写死在配置文件中**。

| 变量 | 默认值 | 说明 |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/yxy` | 数据库连接地址 |
| `DB_USERNAME` | `root` | 数据库账号 |
| `DB_PASSWORD` | 空 | 数据库密码。**本地建议写在 `application-local.yml`（不入库），生产用环境变量注入** |
| `JWT_SECRET` | 空 | **生产必须设置**（≥64 字节）。留空则每次启动生成随机密钥，导致重启后所有 Token 失效 |
| `JWT_EXPIRATION` | `86400000` | Token 有效期（毫秒） |
| `REDIS_HOST` / `REDIS_PORT` | `127.0.0.1` / `6379` | Redis 地址 |
| `REDIS_PASSWORD` | 空 | Redis 密码 |
| `DEEPSEEK_API_KEY` | 空 | **必填**，否则 AI 功能不可用 |
| `DEEPSEEK_MODEL` | `deepseek-chat` | 模型名称 |
| `UPLOAD_DIR` | `./uploads` | 上传文件存储目录 |

前端可选配置见 `community/.env.example`。

---

## 📡 API 概览

基础路径 `/api`，统一响应格式：
```json
{ "success": true, "code": 200, "message": "OK", "data": {} }
```

| 模块 | 主要路径 |
|---|---|
| 认证 | `/auth/register` `/auth/login` `/auth/logout` `/auth/me` |
| 用户 | `/users/{id}` `/users/{id}/stats` `/users/{id}/like` |
| 内容 | `/posts` `/comments` `/communities` `/boards` |
| 统计 | `/statistics/overview` `/statistics/online` |
| AI | `/ai/ask` `/ai/ask/stream` `/ai/conversations` `/ai/health` |
| 管理 | `/admin/dashboard` `/admin/posts` `/admin/users` |
| 上传 | `/upload/avatar` `/upload/image` |

---

## ⚠️ 注意事项

1. **`JWT_SECRET` 务必在生产环境显式设置**，否则每次重启都会签发新的随机密钥，已登录用户会被强制登出。
2. **AI 功能需要配置 `DEEPSEEK_API_KEY`**，未配置时接口会返回友好提示而非报错。
3. `uploads/` 目录存放用户上传的头像与图片，已在 `.gitignore` 中忽略，请勿提交。
4. 浏览量采用 Redis 增量聚合并每 5 分钟批量落库，**压测或验证时需等待约 5 分钟**才能看到数据库计数更新。

---

## 📄 文档

| 文档 | 说明 |
|---|---|
| `AI_OPTIMIZATION_SUMMARY.md` | AI 功能优化总结（含会话持久化） |
| `AGENT_UPGRADE_PLAN.md` | 智能体（工具调用 + RAG）改造方案 |
| `PROJECT_SUMMARY.md` | 项目整体总结（用于简历） |

---

## 📝 License

本项目基于 [MIT License](./LICENSE) 开源。
