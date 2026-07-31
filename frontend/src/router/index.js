import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', description: '登录 AI-world，进入统一的 AI 工作台' }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台', icon: 'HomeFilled', description: '查看系统状态、关键数据和高频入口' }
      },
      {
        path: 'dict',
        name: 'Dict',
        component: () => import('@/views/dict/index.vue'),
        meta: { title: '字典管理', icon: 'Collection', description: '维护系统配置和字典项内容' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', icon: 'User', description: '管理账号、状态、权限与安全信息' }
      },
      {
        path: 'log/login',
        name: 'LoginLog',
        component: () => import('@/views/log/login.vue'),
        meta: { title: '登录日志', icon: 'Tickets', description: '查看账户登录记录与异常行为' }
      },
      {
        path: 'log/operation',
        name: 'OperationLog',
        component: () => import('@/views/log/operation.vue'),
        meta: { title: '操作日志', icon: 'Document', description: '追踪后台操作记录与变更' }
      },
      {
        path: 'ai/chat',
        name: 'AiChat',
        component: () => import('@/views/ai/ChatRoom.vue'),
        meta: { title: 'AI-world 聊天室', icon: 'Service', description: '进行 AI 对话并管理历史会话' }
      },
      {
        path: 'ai/models',
        name: 'AiModelManage',
        component: () => import('@/views/ai/ModelManage.vue'),
        meta: { title: '模型管理', icon: 'Cpu', description: '配置模型能力、可用状态与默认选项' }
      },
      {
        path: 'ai/skills',
        name: 'AiSkillManage',
        component: () => import('@/views/ai/SkillManage.vue'),
        meta: { title: '技能管理', icon: 'MagicStick', description: '管理对话可挂载的提示词与工具技能' }
      },
      {
        path: 'news/hot',
        name: 'HotNews',
        component: () => import('@/views/news/HotNews.vue'),
        meta: { title: '热点新闻', icon: 'Notification', description: '查看多平台热点与趋势变化' }
      },
      {
        path: 'tools/password',
        name: 'PasswordGenerator',
        component: () => import('@/views/tools/PasswordGenerator.vue'),
        meta: { title: '密码生成器', icon: 'Lock', description: '本地生成高强度密码并快速复制使用' }
      },
      {
        path: 'tools/word-counter',
        name: 'WordCounter',
        component: () => import('@/views/tools/WordCounter.vue'),
        meta: { title: '字数统计器', icon: 'EditPen', description: '统计文本字数、结构和阅读时长' }
      },
      {
        path: 'tools/pomodoro',
        name: 'PomodoroClock',
        component: () => import('@/views/tools/PomodoroClock.vue'),
        meta: { title: '番茄钟', icon: 'Timer', description: '本地番茄钟 · 轻任务清单 · 自动续算' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - AI-world` : 'AI-world'
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
