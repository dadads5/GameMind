# AI 功能优化总结（本次更新）

> 范围：智能问答的**体验优化** + **会话持久化**。
> 涉及：后端 8 个文件（3 新增 / 5 修改）、前端 4 个文件（1 重写 / 3 修改）、新增 2 张表。
> 不涉及论文文档（论文相关见 `THESIS_SUMMARY.md`）。

---

## 1. 优化总览

| # | 类别 | 问题 | 方案 | 效果 |
|---|---|---|---|---|
| 1 | 体验 | 生成过程无法中断，最长需等 120s | `AbortSignal` + 「■ 停止」按钮 | 可随时停止，已生成内容保留 |
| 2 | 性能 | 每个 token 都 `find()` 全量遍历消息数组 | 持有消息对象引用 + rAF 节流滚动 | 消除 O(n) 重复查找与高频 DOM 更新 |
| 3 | 体验 | Enter 强制发送，无法换行；输入框固定 1 行 | Enter 发送 / Shift+Enter 换行 + 高度自适应（≤160px） | 支持多行提问，长文本不再被挤压 |
| 4 | 功能 | 对话历史由前端临时携带（≤20 条），刷新即丢 | 服务端会话持久化（2 张表 + 4 个接口） | 刷新/换设备可续聊，请求体更小 |
| 5 | 正确性 | 润写/智能回复会为每次调用新建会话，污染会话列表 | 新增 `persist` 开关，这两类传 `false` | 会话列表只保留真实对话 |
| 6 | 正确性 | 多轮时当前提问在上下文中重复出现两次 | 调整顺序：先取历史 → 再保存本次提问 | 上下文不再重复 |
| 7 | 编译 | lambda 引用被重复赋值的局部变量 | 固化 `final` 副本 | 编译通过 |

---

## 2. 后端改动

### 2.1 新增文件

| 文件 | 说明 |
|---|---|
| `entity/AiConversation.java` | AI 会话实体（id、userId、title、时间） |
| `entity/AiMessage.java` | 会话消息实体（role 取值与模型消息一致，便于直接拼上下文） |
| `mapper/AiConversationMapper.java` | 会话 CRUD（注解式 SQL，删除带归属校验） |
| `mapper/AiMessageMapper.java` | 消息插入 / 按会话查询 / 级联删除 |
| `services/AiConversationService.java` | 会话解析、列表、历史、删除、保存；异常一律降级不影响回答 |
| `allsql/ai_conversation.sql` | 建表 SQL |

### 2.2 修改文件

**`dto/ai/AskRequest.java`** — 新增两个字段：

```java
private Long conversationId;  // 会话 id，不传则自动新建
private Boolean persist;      // 是否持久化，默认 true；润写/智能回复传 false
```

**`controller/AiController.java`**
- 新增 4 个会话接口（见 §3）
- `/ask` 与 `/ask/stream` 支持会话：解析会话 → 取历史 → 保存提问 → 调用模型 → 保存回答
- SSE 新增首个事件 `conversation`，回传服务端分配的会话 id
- 客户端中断时也保存已生成内容（在 `finally` 中落库）
- `persist=false` 时全程不落库、不发 `conversation` 事件

### 2.3 关键设计

**会话解析顺序（避免上下文重复）**
```
resolve(会话) → 取历史 → 保存本次提问 → 调用模型
```
必须**先取历史再保存提问**，否则当前问题会同时出现在「历史」和「本轮 question」中，导致重复。

**归属校验（安全）**
读取消息与删除会话均校验 `user_id`；传入他人 `conversationId` 时不报错，降级为新建会话（保证聊天不中断）或直接拒绝（删除场景）。

**失败降级**
持久化异常只记日志，不影响 AI 回答；读取上下文失败时降级为空历史。

---

## 3. 新增接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/ai/conversations` | 当前用户会话列表（按最近更新排序，最多 50 条） |
| POST | `/api/ai/conversations` | 新建空会话，返回 `{ conversationId }` |
| GET | `/api/ai/conversations/{id}/messages` | 会话历史消息（带归属校验） |
| DELETE | `/api/ai/conversations/{id}` | 删除会话及其消息（带归属校验） |

**SSE 事件约定（`/api/ai/ask/stream`）**

| 事件 | data | 说明 |
|---|---|---|
| `conversation` | 会话 id（数字） | 首个事件，前端据此沿用会话 |
| `token` | 文本片段 | 逐字渲染 |
| `done` | `[DONE]` | 结束 |
| `error` | 脱敏提示 | 出错 |

---

## 4. 前端改动

| 文件 | 改动 |
|---|---|
| `api/ai.ts` | ① `streamAI` 支持 `signal`（中断按 `onDone` 处理，不误报错误）② 解析 `conversation` 事件 ③ `persist` 参数 ④ 新增 4 个会话 API ⑤ `askAI` 第三参改为 `AskOptions` |
| `views/AIChatView.vue` | 重写：停止生成按钮、消息引用优化、rAF 节流滚动、输入框换行与自适应、**会话侧栏**（列表/新建/切换/删除/清空） |
| `components/AiPolish.vue` | 润写调用传 `persist: false` |
| `components/AiReply.vue` | 智能回复调用传 `persist: false` |

### 4.1 中断机制
```ts
controller = new AbortController()
await streamAI(content, [], { ... }, { signal: controller.signal, conversationId })
// 停止：controller.abort()
```
`api/ai.ts` 中对 `AbortError` 做了区分：fetch 阶段中断直接返回，读取流阶段中断调用 `onDone()`，因此**不会弹出"网络错误"**。

### 4.2 渲染性能
```ts
// 之前：每个 token 都 find 整个数组
const aiMessage = messages.value.find((m) => m.id === aiId)

// 现在：push 后直接持有引用
const aiMessage: Message = { ... }
messages.value.push(aiMessage)
onToken: (token) => { aiMessage.content += token; scheduleScroll() }
```
滚动用 `requestAnimationFrame` 节流，避免每个 token 触发一次 `nextTick`。

---

## 5. 数据库变更

```sql
CREATE TABLE ai_conversation (...);  -- 会话，索引 user_id / updated_at
CREATE TABLE ai_message (...);        -- 消息，外键级联删除
```
执行：`mysql -u root -p yxy < allsql/ai_conversation.sql`

---

## 6. 踩坑记录

1. **lambda 引用非 effectively final 变量**（编译报错）
   `conversationId` 与 `history` 先赋初值、后在 `if (persist)` 中重新赋值，又被 `CompletableFuture.runAsync` 的 lambda 引用 → 编译失败。
   解决：在 `runAsync` 前固化 `final` 副本（`finalHistory` / `finalConversationId` / `finalPersist`）。

2. **当前提问在上下文中重复**：见 §2.3 顺序调整。

3. **润写污染会话列表**：润写/智能回复不传 `conversationId`，服务端会每次自动建会话 → 加 `persist=false`。

---

## 7. 验证情况

- 前端：`npm run type-check` 通过，本次改动的 4 个文件**零错误**（输出中的既有报错来自 `GachaSimulatorView` 等历史文件，与本次无关）。
- 后端：本环境 `mvn` 未加入 PATH 无法编译，需在 IDE 中 Build 验证。

**建议验证步骤**
1. 执行 `allsql/ai_conversation.sql` 建表；
2. 启动后端与前端，进入 `/ai-chat`；
3. 连续提问两轮 → 左侧栏应只出现**一个**会话，且第二轮能承接上文；
4. 生成中点击「■ 停止」→ 内容保留、无报错；
5. 刷新页面 → 点击该会话可看到完整历史；
6. 在帖子页使用「AI 润写」→ 会话列表**不会**新增记录。

---

## 8. 后续可做（本次未做）

| 项 | 说明 |
|---|---|
| `/ask` 同步接口并发保护 | 当前在 Tomcat 线程阻塞最长 60s，流式有信号量 32，同步无保护 |
| `DeepSeekService` 去重 | `ask` 与 `streamAsk` 消息构建重复约 15 行 |
| 结果缓存 | 相同问题重复调用，可加 Redis 缓存省额度 |
| 失败重试 | 429 / 5xx / 网络抖动目前直接失败 |
| 复制按钮 | `AiChatView` 气泡与 `AiReply` 结果尚未支持一键复制 |
| 智能体改造 | 工具调用 + RAG，方案见 `AGENT_UPGRADE_PLAN.md` |
