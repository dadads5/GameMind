# community（前端）

本目录是 **Gamemind 游戏社区平台**的前端工程，基于 Vue 3 + TypeScript + Vite 构建。

> 项目整体介绍、后端启动方式与环境配置请查看 **[根目录 README](../README.md)**。

## 技术栈

Vue 3 · TypeScript · Vite 8 · Pinia · Vue Router · Element Plus · Tailwind CSS 4 · Axios

## 开发

```bash
npm install
npm run dev        # 开发服务器 http://localhost:5173
```

## 构建

```bash
npm run type-check # 类型检查（vue-tsc）
npm run build      # 构建产物输出到 dist/
npm run preview    # 本地预览构建产物
```

## 代理说明

`vite.config.ts` 中已配置代理，开发时无需处理跨域：

| 路径 | 转发目标 |
|---|---|
| `/api` | `http://localhost:8080` |
| `/uploads` | `http://localhost:8080` |

环境变量示例见 `.env.example`。
