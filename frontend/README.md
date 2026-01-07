# Spring Boot 后台管理系统 - 前端

基于 Vue 3 + Element Plus 构建的现代化后台管理界面。

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **Vite** - 下一代前端构建工具
- **Element Plus** - 基于 Vue 3 的组件库
- **Vue Router** - Vue.js 官方路由
- **Pinia** - Vue 状态管理
- **Axios** - HTTP 客户端
- **Sass** - CSS 预处理器

## 功能模块

- 🏠 **首页仪表盘** - 数据统计、快捷操作、系统信息
- 📚 **课程管理** - 课程CRUD、发布/下架
- 📖 **字典管理** - 数据字典及字典项管理
- ✅ **任务管理** - 任务创建、分配、状态跟踪
- 👤 **用户管理** - 用户CRUD、角色分配、状态控制

## 快速开始

### 安装依赖

```bash
cd frontend
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:3000

### 构建生产版本

```bash
npm run build
```

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API 接口
│   │   ├── request.js    # Axios 封装
│   │   ├── auth.js       # 认证接口
│   │   ├── course.js     # 课程接口
│   │   └── dict.js       # 字典接口
│   ├── layout/           # 布局组件
│   ├── router/           # 路由配置
│   ├── stores/           # Pinia 状态管理
│   ├── styles/           # 全局样式
│   ├── views/            # 页面视图
│   │   ├── dashboard/    # 首页
│   │   ├── login/        # 登录页
│   │   ├── course/       # 课程管理
│   │   ├── dict/         # 字典管理
│   │   ├── task/         # 任务管理
│   │   └── user/         # 用户管理
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html
├── package.json
└── vite.config.js
```

## API 代理配置

开发环境下，前端请求会自动代理到后端服务：

- 前端地址: `http://localhost:3000`
- 后端地址: `http://localhost:8080`
- API 前缀: `/api` → 代理到后端根路径

## 默认账户

首次使用可以使用任意用户名密码登录（演示模式），或通过注册功能创建真实账户。

## 后端配置

确保 Spring Boot 后端服务运行在 `localhost:8080`，并已配置跨域支持。

## 浏览器支持

- Chrome (推荐)
- Firefox
- Safari
- Edge
