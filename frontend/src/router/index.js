import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', description: '登录 AI-world，进入基于 AI 的世界' }
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
        meta: { title: '工作台', icon: 'HomeFilled', description: '查看系统概览与常用模块入口' }
      },
      {
        path: 'prompt',
        name: 'PromptTemplate',
        component: () => import('@/views/prompt/index.vue'),
        meta: { title: '提示工程', icon: 'EditPen', description: '管理提示词模板与渲染配置' }
      },
      {
        path: 'dict',
        name: 'Dict',
        component: () => import('@/views/dict/index.vue'),
        meta: { title: '字典管理', icon: 'Collection', description: '维护系统字典及字典项配置' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', icon: 'User', description: '查看账户状态、权限和安全操作' }
      },
      {
        path: 'log/login',
        name: 'LoginLog',
        component: () => import('@/views/log/login.vue'),
        meta: { title: '登录日志', icon: 'Tickets', description: '追踪账户登录行为与异常' }
      },
      {
        path: 'log/operation',
        name: 'OperationLog',
        component: () => import('@/views/log/operation.vue'),
        meta: { title: '操作日志', icon: 'Document', description: '审计后台操作记录与变更' }
      },
      {
        path: 'ai/chat',
        name: 'AiChat',
        component: () => import('@/views/ai/ChatRoom.vue'),
        meta: { title: 'AI-world 聊天室', icon: 'Service', description: '进行 AI 对话与历史会话管理' }
      },
      {
        path: 'ai/resume',
        name: 'ResumeAI',
        component: () => import('@/views/ai/ResumeAI.vue'),
        meta: { title: 'AI 简历助手', icon: 'DocumentChecked', description: '生成和优化简历内容' }
      },
      {
        path: 'ai/models',
        name: 'AiModelManage',
        component: () => import('@/views/ai/ModelManage.vue'),
        meta: { title: '模型管理', icon: 'Cpu', description: '配置 AI 模型能力与默认选项' }
      },
      {
        path: 'news/hot',
        name: 'HotNews',
        component: () => import('@/views/news/HotNews.vue'),
        meta: { title: '热点新闻', icon: 'Notification', description: '查看多平台实时热点与趋势' }
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
