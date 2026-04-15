# Spring Boot Admin Frontend

基于 `Vue 3 + Vite + Element Plus` 的后台前端项目。

## 当前功能

- 工作台概览
- 用户管理
- 字典管理
- AI 对话与模型配置
- 热点新闻
- 登录日志与操作日志

## 本地开发

```bash
cd frontend
npm install
npm run dev
```

默认开发地址：`http://localhost:3000`

## 生产构建

```bash
npm run build
```

## 目录说明

```text
frontend/
  src/
    api/        # 前端 API 封装
    layout/     # 全局布局
    router/     # 路由配置
    stores/     # Pinia 状态管理
    styles/     # 全局样式
    views/      # 页面视图
  public/
  index.html
  package.json
  vite.config.js
```
