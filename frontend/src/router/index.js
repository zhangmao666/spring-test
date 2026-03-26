import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
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
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'fund/ranking',
        name: 'FundRanking',
        component: () => import('@/views/finance/FundRanking.vue'),
        meta: { title: '基金排行', icon: 'TrendCharts' }
      },
      {
        path: 'fund/analysis',
        name: 'FundAnalysis',
        component: () => import('@/views/finance/FundAnalysis.vue'),
        meta: { title: 'AI走势分析', icon: 'MagicStick' }
      },
      {
        path: 'fund/analysis/:fundCode',
        name: 'FundAnalysisDetail',
        component: () => import('@/views/finance/FundAnalysis.vue'),
        meta: { title: 'AI走势分析', icon: 'MagicStick' }
      },
      {
        path: 'finance/fund-trend/:fundCode',
        name: 'FundTrend',
        component: () => import('@/views/finance/FundTrend.vue'),
        meta: { title: '基金走势', icon: 'DataLine' }
      },
      {
        path: 'prompt',
        name: 'PromptTemplate',
        component: () => import('@/views/prompt/index.vue'),
        meta: { title: '提示工程', icon: 'EditPen' }
      },
      {
        path: 'dict',
        name: 'Dict',
        component: () => import('@/views/dict/index.vue'),
        meta: { title: '字典管理', icon: 'Collection' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'log/login',
        name: 'LoginLog',
        component: () => import('@/views/log/login.vue'),
        meta: { title: '登录日志', icon: 'Tickets' }
      },
      {
        path: 'log/operation',
        name: 'OperationLog',
        component: () => import('@/views/log/operation.vue'),
        meta: { title: '操作日志', icon: 'Document' }
      },
      {
        path: 'ai/chat',
        name: 'AiChat',
        component: () => import('@/views/ai/ChatRoom.vue'),
        meta: { title: 'AI 聊天室', icon: 'Service' }
      },
      {
        path: 'ai/resume',
        name: 'ResumeAI',
        component: () => import('@/views/ai/ResumeAI.vue'),
        meta: { title: 'AI 简历助手', icon: 'DocumentChecked' }
      },
      {
        path: 'ai/models',
        name: 'AiModelManage',
        component: () => import('@/views/ai/ModelManage.vue'),
        meta: { title: '基座模型管理', icon: 'Cpu' }
      },
      {
        path: 'news/hot',
        name: 'HotNews',
        component: () => import('@/views/news/HotNews.vue'),
        meta: { title: '热点新闻', icon: 'Notification' }
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
  document.title = to.meta.title ? `${to.meta.title} - 后台管理` : '后台管理系统'
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
