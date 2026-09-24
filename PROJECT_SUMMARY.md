# 项目总结 · 游戏主题社区论坛平台（Total_ai）

> 说明：本项目由本人**独立负责**，从需求分析、架构设计、数据库设计、后端开发、前端开发到联调上线全流程完成。本文件用于撰写简历「项目经历」与面试准备，前几节为项目深度分析，最后一节为可直接裁剪进简历的精简版。

---

## 1. 项目定位与背景分析

本项目是一个面向游戏爱好者（覆盖原神、火影、卡牌对战、英雄对战等多个题材）的**多模块社区论坛平台**，是集内容社区、用户成长体系、实时互动、AI 辅助、游戏工具于一体的全栈 Web 应用。

从产品视角，它要同时解决三类需求：
1. **内容沉淀**：玩家发帖、讨论、分享攻略（论坛基础能力）。
2. **社区氛围**：点赞/收藏/关注、等级徽章、个人主页、管理员治理（社区运营能力）。
3. **差异化体验**：AI 智能问答 / 润写 / 回复、实时在线、游戏专属工具（伤害计算/抽卡/配装），形成与通用论坛的区隔。

技术选型上在「常规 CRUD 论坛的可维护性」与「实时 / AI 等特性的性能与扩展性」之间取得平衡——这是本项目架构的核心取舍点。

---

## 2. 技术栈分析（含选型理由）

### 前端
| 技术 | 作用 | 选型理由 |
|---|---|---|
| Vue 3 + `<script setup>` | 视图层 | Composition API 逻辑复用清晰，SFC 适合中型项目快速迭代 |
| TypeScript | 类型安全 | vue-tsc 全量类型检查，降低大型 SPA 维护成本 |
| Vite 8 | 构建 | 极速 HMR，原生 ESM，产物小 |
| Element Plus 2 | 组件库 | 表单/弹窗/消息开箱即用，缩短后台周期 |
| Tailwind CSS 4 | 原子化样式 | 无上下文切换，适合在 Element Plus 之上做个性化视觉 |
| Pinia 3 | 状态管理 | 比 Vuex 更轻、TS 友好，管用户态/通知未读数 |
| Vue Router 5 | 路由 | `meta.requiresAuth` 登录守卫、动态重定向 |
| Axios（封装 `request`） | HTTP | 统一拦截响应，约定 `res.success / res.data` |

### 后端
| 技术 | 作用 | 选型理由 |
|---|---|---|
| Spring Boot 4.0.3 / Java 17 | 框架 | 约定优于配置，生态成熟 |
| MyBatis + MySQL | 持久层 | SQL 可控，便于复杂统计/联表；PageHelper 分页 |
| Redis | 缓存 + 实时计数 | 统计缓存、在线 ZSet、单设备 Token、AI 限流 |
| JWT（jjwt 0.12.6） | 认证 | 无状态令牌，配合拦截器做登录校验 |
| Spring Security Crypto（BCrypt） | 密码 | 自适应哈希，抵御彩虹表 |
| PageHelper | 分页 | 物理分页，避免全表加载 |
| Validation | 参数校验 | `@Valid` 前置防御 |
| Lombok | 样板代码 | 减少 getter/setter/Builder 冗余 |
| HttpClient + SSE | AI 调用 | 串/流式调用 DeepSeek，流式用 `SseEmitter` 推送 |
| RAG 检索增强（`EmbeddingService` + `KnowledgeService`） | 站内攻略检索 | 硅基流动 bge-m3 向量化帖子切片，余弦相似度 Top-K 召回注入 prompt，可溯源，未配置自动降级 |

**关键取舍**：认证用「JWT 拦截器」而非 Spring Security 全量过滤器链，是因为纯前后端分离 SPA + 轻量鉴权，自建拦截器更轻、对白名单（`AntPathMatcher`）控制更直接；密码用 BCrypt 而非明文/MD5，是安全底线。

---

## 3. 系统架构分析

### 3.1 分层架构
```
Vue 3 SPA（浏览器）
   │  Axios，请求头携带 JWT（Bearer）
   ▼
Spring Boot Controller（REST 端点，@RestController）
   │  JwtAuthenticationInterceptor：白名单放行 + 登录用户注入 + 异步刷新在线
   ▼
Service 层（事务边界、业务规则）
   ├─ MyBatis Mapper ──► MySQL（12 张表）
   ├─ Redis（统计缓存 / 在线 ZSet / 单设备 Token / AI 限流）
   └─ DeepSeekService（SSE 流式对话）
```

### 3.2 前后端协作契约
- **统一响应**：`Result<T>` 包装 `{ success, code, message, data }`，前端 `request` 统一解包，业务只关心 `res.data`。
- **错误体系**：后端 `BusinessException` + `ResultCode` 枚举，前端 `notify` 统一提示，避免散落 `alert/confirm`。
- **鉴权流**：登录返回 `token`；前端存 `localStorage`；路由守卫 `requiresAuth` 拦截未登录跳 `/login?redirect=`；拦截器校验 Token 并注入 `LoginUser`。

### 3.3 安全设计
- **单设备登录**：Redis 存 `user:token:{id} = 当前有效 token`，异地登录使旧 Token 失效。
- **在线状态**：登录即在 `online:users` ZSet 写入（member=userId，score=最后活跃时间戳）；拦截器对每个已登录请求**异步**刷新时间戳。
- **参数校验**：`@Valid` 校验注册/登录/发帖等关键入参；敏感操作（删帖、改角色）均要求 `@CurrentUser` 且后端二次校验权限。

---

## 4. 数据库设计分析（12 张核心表）

| 表 | 职责 | 关键关系 |
|---|---|---|
| `user` | 用户（含 role/vip/like_count/密码哈希） | 1:N 帖子、评论、点赞 |
| `user_like` | 用户主页互赞 | 记录谁给谁主页点赞（每日一次） |
| `community` | 社区/板块容器 | 1:N 帖子 |
| `board` | 子版块 | 隶属 community |
| `post` | 帖子（标题/内容/作者/浏览量/置顶/精华） | N:1 user、community |
| `post_image` | 帖子配图 | N:1 post |
| `post_like` | 帖子点赞 | 用户对帖子的点赞关系 |
| `comment` | 评论 | N:1 post、user |
| `comment_like` | 评论点赞 | 用户对评论的点赞 |
| `notification` | 站内通知 | 评论/点赞等事件触发 |
| `genshin_character` | 原神角色（元素/属性） | 支撑伤害计算/配装 |
| `wzry_hero` | 王者荣耀英雄 | 支撑英雄模块 |

设计要点：点赞/收藏独立成关系表（`post_like`/`comment_like`/`user_like`），便于统计与去重；浏览量走 Redis 增量而非每次写库；`post` 含置顶/精华标记支撑运营与首页排序。

---

## 5. 核心功能模块（非 AI 部分）

- **多模块社区**（naruto / module2 / module3 / module4）：各自独立的首页、板块、列表、详情、发帖；路由按模块拆分、按需懒加载（`() => import()`）减小首包；首页聚合「全局热门」+ 各社区最新。
- **用户体系与成长**：注册（用户名预校验）、登录、改密、改资料、头像上传；等级/徽章系统（`utils/userRating.ts` 纯函数，前后端共用）——活跃度积分 = `发帖×2 + 回复×1 + 获赞×0.5 + 主页获赞×2`，阈值 Lv1–Lv10（Lv3 后每级 ×10 陡增），自动发放管理员/VIP/老用户/活跃/人气王等徽章。
- **内容互动**：发帖（配图）、评论、点赞/取消、收藏、浏览量自增；热门/最新双排序。
- **管理员后台**：仪表盘、跨社区帖子搜索、置顶/精华、删帖删评、用户启停、角色/VIP、板块与社区 CRUD；删除类操作前端统一 `notifyConfirm` 二次确认。
- **站内通知**：评论/点赞等事件写入 `notification`，前端未读数由全局轮询，实时感知。
- **统计与实时**：统计总览 Redis 缓存 60s、写入失败回退 DB；轻量 `GET /statistics/online` 只算在线人数（Redis ZSet + 5 分钟滑动窗口），不随总览缓存过期。
- **游戏工具**：原神伤害计算器、抽卡模拟器、角色配装、游戏资讯（数据来自 `genshin_character` / `wzry_hero`）。

---

## 6. AI 特色功能体系（独立大节）

本项目把大模型能力深度嵌入社区场景，形成「问答 + 创作辅助 + 互动辅助」三层 AI 能力。所有 AI 能力统一由 `DeepSeekService`（DeepSeek `deepseek-chat`）提供，通过 `AiController` 暴露，前端 `api/ai.ts` 封装 `askAI`（同步）与 `streamAI`（流式）两种调用方式。

### 6.1 AI 智能问答（多轮 + 流式）
- **入口**：独立 `/ai-chat` 聊天页（需登录），调用 `POST /api/ai/ask`（同步）与 `POST /api/ai/ask/stream`（SSE 流式）。
- **多轮上下文**：请求携带 `history: ChatMessage[]`，后端将历史拼接进 DeepSeek messages，实现连续对话记忆。
- **流式渲染**：后端用 `SseEmitter` 逐段推送（`event: token` / `event: done` / `event: error`）；前端用**原生 `fetch` + `ReadableStream` reader** 解析 `text/event-stream`，逐 token 调用 `onToken` 回调，实现打字机效果（这点比直接用 EventSource 更可控，可带自定义鉴权头与错误回调）。
- **工程化防护**（高并发长连接场景）：
  - 流式专用**有界线程池**（核心 4 / 最大 16 / 队列 64，弃用早期无界 `newCachedThreadPool`，防止并发长连接无限建线程）；
  - **并发信号量** `SSE_CONCURRENCY = 32`，超限直接拒绝，避免长连接堆积拖垮后端；
  - **全局共享心跳调度器**（替代每连接单线程），每 15s 发 SSE 注释行防 nginx 等中间代理因空闲断开；
  - 单连接 **5 分钟超时兜底** + `AtomicBoolean` 保证 `cleanup` 只执行一次（释放信号量/取消心跳）；
  - `data` 行兼容「JSON 字符串」与「纯文本」两种格式，避免 token 被静默丢弃。
- **限流**：每用户每分钟最多 10 次（`Redis` 计数，失效即放行），防止额度被刷。

### 6.2 AI 润写（创作辅助，发帖场景）
- **入口**：所有发帖页（`CreatePostView` 等）内嵌 `AiPolish` 组件，正文框右下方「✨ AI 润写」按钮。
- **五种风格模式**：`智能润色 / 更简洁 / 更专业 / 更生动 / 纠错`，分别对应不同 system 提示词（如纠错仅修错别字语病、不大幅改写；生动要求有感染力）。
- **交互**：弹出「原文 vs 润色后」双栏对照，润色结果可**手动微调**，确认后「替换原文」一键写回 `v-model:content`，不打断发帖流程。
- **实现**：调用同步 `askAI('/ai/ask')`，非流式（整段返回即可，无需逐字），prompt = 模式指令 + 正文。

### 6.3 AI 智能回复（互动辅助，帖子详情场景）
- **入口**：帖子详情页（`PostDetailView` / `CommunityPostDetailView` 等）内嵌 `AiReply` 组件，按钮「✨ AI 智能回复」。
- **能力**：把当前帖子「标题 + 正文」拼为 prompt（`请对以下帖子内容进行智能回复`），调用流式 `streamAI` 生成一段可参考的回复，逐字渲染到结果框，供用户借鉴或直接复制后回复。
- **价值**：降低社区互动门槛，尤其对新用户或「想回但不知怎么开口」的场景，提升评论活跃度。
- **实现**：`getAiReply()` 内 `aiReply.value += text` 增量拼接，支持 `light/dark` 双主题适配不同帖子页。

### 6.4 AI 能力设计小结
三层能力对应三类用户意图：**有问题问 AI（问答）**、**不会写让 AI 改（润写）**、**想回但不懂让 AI 起头（智能回复）**，均围绕「降低 UGC 生产门槛、提升社区活跃」这一目标。后端统一限流 + 并发保护 + 异常降级，保证 AI 特性不会成为系统稳定性短板。

### 6.5 RAG 检索增强（已实现）
在三层 AI 能力之上引入检索增强生成，让回答「基于站内真实内容、可溯源」：
- **向量化**：`EmbeddingService` 对接硅基流动 SiliconFlow（OpenAI 兼容），默认 `BAAI/bge-m3`（1024 维），Key 经 `EMBEDDING_API_KEY` 注入。
- **索引**：`KnowledgeService` 将帖子切片向量化存入 `PostChunk`（MySQL，embedding 以逗号分隔浮点串），启动自动建索引 + 每 30 分钟定时重建 + 发帖时增量 `indexPost`。
- **召回**：查询向量与全量切片做余弦相似度 Top-K 召回（阈值 0.20），命中片段作为参考素材注入 prompt。
- **降级**：`EmbeddingService` 未配置时自动跳过 RAG，问答降级为纯模型知识，不影响主流程。
- 工具调用智能体（Function Calling / AgentService）仍规划中，方案见 `AGENT_UPGRADE_PLAN.md`。

---

## 7. 关键技术难点与解决方案（面试可展开）

1. **Redis 抖动拖垮全站**：在线状态原同步写 Redis，Redis 不可用时每请求阻塞到超时、并发占满 Tomcat 线程。改为拦截器内独立线程池（核心 1/最大 2/队列 + `DiscardPolicy`）**异步刷新在线时间戳**，队列满丢弃——宁可少统计一次，不阻塞业务。
2. **实时在线统计精准度与膨胀**：Redis `ZSet`（score=时间戳）+ 5 分钟滑动窗口 + 定时清理；在线人数实时计算，不读 60s 统计缓存。
3. **SSE 长连接资源泄漏**：早期无界线程池 + 每连接独立心跳线程。改为有界线程池 + 全局心跳调度器 + 信号量并发上限(32) + 超时兜底 + `AtomicBoolean` 幂等 cleanup。
4. **AI 限流与降级**：每用户每分钟 10 次 Redis 限流；DeepSeek 未配置/网络/超时/空响应分级降级；错误明细仅记服务端日志、前端收脱敏文案。
5. **统计接口性能**：总览 Redis 缓存 60s；在线人数拆独立轻量端点，互不拖累，首屏只取必要数据。
6. **多页面数据竞态**：公开主页 `onMounted` 未 `await loadProfile()` 致 `loadRating()` 读 `profile=null`、统计恒为 0。改为 `onMounted`/`watch` 先 `await loadProfile()` 再加载统计与等级。
7. **统一交互体验**：封装 `notify`（`ElMessage` + `ElMessageBox.confirm`）替代散落 `alert/confirm`。

---

## 8. 工程化与质量保障
- 前端：`vue-tsc` 类型检查、`run-p` 并行构建、按需懒加载路由、统一 `notify` 与 `request` 封装。
- 后端：Lombok 减样板、统一 `Result`/`BusinessException`、Validation 前置校验、结构化日志（slf4j）。
- 安全：BCrypt 密码、JWT 无状态、单设备登录、敏感操作权限二次校验、AI 接口登录 + 限流。

---

## 9. 简历可直接使用版本（精简、可粘贴）

**游戏社区论坛平台（独立全栈项目）** ｜ Vue3 / TypeScript / Spring Boot / MySQL / Redis

- 独立负责需求分析、架构设计、数据库设计、前后端开发与联调上线，构建面向游戏爱好者的多模块社区论坛（原神/火影/卡牌/英雄），含发帖评论、点赞收藏、等级徽章、个人主页、管理后台、站内通知、游戏工具。
- **AI 特色**：基于 DeepSeek 构建三层 AI 能力——①多轮流式智能问答（SSE + 原生 fetch 逐 token 渲染）；②发帖页 AI 润写（智能润色/简洁/专业/生动/纠错 5 模式，原文对照一键替换）；③帖子详情页 AI 智能回复（按标题+正文流式生成可参考回复）。后端做有界线程池 + 信号量并发上限(32) + 全局心跳 + 限流(每用户 10 次/分) + 异常降级，保障稳定性。
- 前端 Vue3 + TS + Pinia + Element Plus + Tailwind 实现 SPA，封装统一消息/确认组件与路由登录守卫；后端 Spring Boot + MyBatis，RESTful 接口 70+。
- 用 Redis ZSet + 5 分钟滑动窗口实现实时在线统计，独立线程池异步刷新避免 Redis 抖动阻塞主流程；统计总览 Redis 缓存 60s，在线人数独立轻量端点降 DB 压力。
- 引入 RAG 检索增强：硅基流动 bge-m3 向量化站内帖子、余弦相似度 Top-K 召回注入 prompt，让 AI 回答基于站内真实内容且可溯源；独立负责阿里云 ECS 单机部署上线（Nginx 反代 + Let's Encrypt HTTPS + 每日自动备份）。
- 设计活跃度积分驱动的用户等级（Lv1–Lv10）与自动徽章体系，前后端共用计算逻辑；JWT + BCrypt 实现无状态认证与单设备登录；修复多页面数据加载竞态保障数据准确。

**技能关键词**：Vue3 · TypeScript · Vite · Pinia · Element Plus · Tailwind · Spring Boot · MyBatis · MySQL · Redis · JWT · BCrypt · SSE 流式 · DeepSeek API · 大模型应用 · RESTful · 高并发缓存设计 · 前后端分离
