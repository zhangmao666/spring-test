<template>
  <el-container class="layout-shell">
    <el-aside :width="isCollapse ? '88px' : '252px'" class="sidebar-shell">
      <div class="sidebar-panel">
        <div class="brand-block">
          <div class="brand-mark">
            <el-icon :size="22"><TrendCharts /></el-icon>
          </div>
          <transition name="fade-text">
            <div v-show="!isCollapse" class="brand-copy">
              <span class="brand-title">AI-world</span>
<!--              <span class="brand-subtitle">把模型、内容与管理流程接到同一个工作台</span>-->
            </div>
          </transition>
        </div>

        <div class="sidebar-nav">
          <div class="menu-section">
            <span v-show="!isCollapse" class="menu-section__label">工作台</span>
            <el-menu
              :default-active="activeMenu"
              :default-openeds="defaultOpeneds"
              :collapse="isCollapse"
              :collapse-transition="false"
              router
            >
              <el-menu-item index="/dashboard">
                <el-icon><Monitor /></el-icon>
                <template #title>工作台</template>
              </el-menu-item>
            </el-menu>
          </div>

          <div class="menu-section">
            <span v-show="!isCollapse" class="menu-section__label">智能内容</span>
            <el-menu
              :default-active="activeMenu"
              :default-openeds="defaultOpeneds"
              :collapse="isCollapse"
              :collapse-transition="false"
              router
            >
              <el-menu-item index="/news/hot">
                <el-icon><Bell /></el-icon>
                <template #title>热点新闻</template>
              </el-menu-item>
              <el-sub-menu index="content-ai">
                <template #title>
                  <el-icon><Lightning /></el-icon>
                  <span>AI 工作区</span>
                </template>
                <el-menu-item index="/ai/chat">AI 聊天</el-menu-item>
                <el-menu-item index="/ai/models">模型管理</el-menu-item>
              </el-sub-menu>
            </el-menu>
          </div>

          <div class="menu-section">
            <span v-show="!isCollapse" class="menu-section__label">系统管理</span>
            <el-menu
              :default-active="activeMenu"
              :default-openeds="defaultOpeneds"
              :collapse="isCollapse"
              :collapse-transition="false"
              router
            >
              <el-menu-item index="/user">
                <el-icon><UserFilled /></el-icon>
                <template #title>用户管理</template>
              </el-menu-item>
              <el-menu-item index="/dict">
                <el-icon><FolderOpened /></el-icon>
                <template #title>字典管理</template>
              </el-menu-item>
              <el-sub-menu index="system-log">
                <template #title>
                  <el-icon><Tickets /></el-icon>
                  <span>日志审计</span>
                </template>
                <el-menu-item index="/log/login">登录日志</el-menu-item>
                <el-menu-item index="/log/operation">操作日志</el-menu-item>
              </el-sub-menu>
            </el-menu>
          </div>

          <div class="menu-section">
            <span v-show="!isCollapse" class="menu-section__label">工具中心</span>
            <el-menu
              :default-active="activeMenu"
              :default-openeds="defaultOpeneds"
              :collapse="isCollapse"
              :collapse-transition="false"
              router
            >
              <el-sub-menu index="toolbox">
                <template #title>
                  <el-icon><MagicStick /></el-icon>
                  <span>实用工具</span>
                </template>
                <el-menu-item index="/tools/password">密码生成器</el-menu-item>
                <el-menu-item index="/tools/word-counter">字数统计器</el-menu-item>
                <el-menu-item index="/tools/pomodoro">番茄钟</el-menu-item>
              </el-sub-menu>
            </el-menu>
          </div>
        </div>

        <div class="sidebar-footer">
          <button class="collapse-button" type="button" @click="toggleCollapse">
            <el-icon :size="18">
              <Expand v-if="isCollapse" />
              <Fold v-else />
            </el-icon>
            <span v-show="!isCollapse">收起导航</span>
          </button>
        </div>
      </div>
    </el-aside>

    <el-container :class="['workspace-shell', { 'workspace-shell--chat': isAiChatRoute }]" :data-tone="currentTone">
      <el-header v-if="!isAiChatRoute" class="workspace-header">
        <div class="workspace-header__left">
          <div class="page-badge">
            <span class="page-badge__dot"></span>
            <span>{{ toneLabel }}</span>
          </div>
          <h1 class="page-title">{{ pageTitle }}</h1>
          <p class="page-description">{{ pageDescription }}</p>

<!--          <el-breadcrumb separator="/" class="page-breadcrumb">-->
<!--            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">-->
<!--              {{ item.title }}-->
<!--            </el-breadcrumb-item>-->
<!--          </el-breadcrumb>-->
        </div>

        <div class="workspace-header__right">
          <router-link class="header-shortcut" to="/user">
            <el-icon><UserFilled /></el-icon>
            <span>用户中心</span>
          </router-link>

          <el-dropdown trigger="click" @command="handleCommand">
            <button class="user-chip" type="button">
              <el-avatar :size="34" class="user-chip__avatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <span class="user-chip__name">{{ username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  账户信息
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  安全退出
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main :class="['workspace-main', { 'workspace-main--chat': isAiChatRoute }]">
        <router-view v-slot="{ Component, route: r }">
          <component
            :is="Component"
            :key="r.path"
            v-motion
            :initial="{ opacity: 0, y: 12 }"
            :enter="{ opacity: 1, y: 0, transition: { duration: 260, ease: [0.19, 1, 0.22, 1] } }"
          />
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowDown,
  Bell,
  Expand,
  Fold,
  FolderOpened,
  Lightning,
  MagicStick,
  Monitor,
  SwitchButton,
  Tickets,
  TrendCharts,
  User,
  UserFilled
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)

const activeMenu = computed(() => route.path)
const username = computed(() => localStorage.getItem('username') || '访客')
const pageTitle = computed(() => route.meta.title || '工作台')
const pageDescription = computed(() => route.meta.description || '')
const defaultOpeneds = ['content-ai', 'toolbox', 'system-log']
const isAiChatRoute = computed(() => route.name === 'AiChat')

const currentTone = computed(() => {
  const p = route.path
  if (p.startsWith('/ai')) return 'ai'
  if (p.startsWith('/news')) return 'news'
  if (p.startsWith('/tools')) return 'tools'
  if (p.startsWith('/user') || p.startsWith('/dict') || p.startsWith('/log')) return 'system'
  return 'primary'
})

const toneLabel = computed(() => {
  const map = { ai: 'AI 工作区', news: '内容情报', tools: '工具中心', system: '系统管理', primary: '工作台' }
  return map[currentTone.value] || '工作台'
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = async (command) => {
  if (command === 'profile') {
    ElMessage.info('账户详情入口可继续接入个人中心页面。')
    return
  }

  if (command === 'logout') {
    try {
      await ElMessageBox.confirm(
        '退出后将清除本地登录状态，需要重新输入账号密码才能继续操作。',
        '确认退出登录',
        {
          confirmButtonText: '退出登录',
          cancelButtonText: '再看看',
          type: 'warning'
        }
      )

      localStorage.removeItem('token')
      localStorage.removeItem('username')
      router.push('/login')
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('退出登录失败，请稍后重试。')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.layout-shell {
  height: 100dvh;
  min-height: 100dvh;
  padding: 18px;
  gap: 18px;
  overflow: hidden;
  background:
    radial-gradient(circle at top right, rgba(47, 91, 234, 0.12), transparent 24%),
    radial-gradient(circle at left 30%, rgba(15, 159, 110, 0.08), transparent 18%);
}

.sidebar-shell,
.workspace-shell {
  min-height: 0;
}

.sidebar-panel,
.workspace-header,
.workspace-main {
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-sm);
  backdrop-filter: blur(18px);
}

.sidebar-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 14px;
  border-radius: 24px;
  overflow: hidden;
}

.sidebar-panel::before {
  content: '';
  position: absolute;
  top: -90px;
  right: -30px;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(47, 91, 234, 0.14), transparent 68%);
  pointer-events: none;
}

.sidebar-nav {
  position: relative;
  z-index: 1;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 4px;
}

.sidebar-nav::-webkit-scrollbar {
  width: 6px;
}

.sidebar-nav::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.4);
  border-radius: 999px;
}

.sidebar-nav::-webkit-scrollbar-track {
  background: transparent;
}

.brand-block {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 12px 18px;
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 46px;
  height: 46px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-strong));
  color: #fff;
  box-shadow: 0 12px 24px rgba(47, 91, 234, 0.24);
}

.brand-copy {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.brand-title {
  color: var(--text-primary);
  font-size: 1rem;
  font-weight: 800;
}

.brand-subtitle {
  margin-top: 3px;
  color: var(--text-muted);
  font-size: 0.8rem;
}

.menu-section {
  margin-bottom: 12px;
}

.menu-section__label {
  display: block;
  margin: 0 12px 8px;
  color: var(--text-disabled);
  font-size: 0.74rem;
  font-weight: 800;
  letter-spacing: 0.08em;
}

.menu-section :deep(.el-menu) {
  border: none;
  background: transparent;
}

.menu-section :deep(.el-menu-item),
.menu-section :deep(.el-sub-menu__title) {
  height: 48px;
  margin-bottom: 4px;
  border-radius: 14px;
  color: var(--text-secondary);
  font-weight: 600;
}

.menu-section :deep(.el-sub-menu) {
  margin-bottom: 4px;
  border-radius: 18px;
  overflow: hidden;
}

.menu-section :deep(.el-sub-menu.is-opened) {
  background: linear-gradient(180deg, rgba(244, 247, 255, 0.96), rgba(237, 242, 255, 0.88));
  box-shadow: inset 0 0 0 1px rgba(116, 143, 252, 0.12);
}

.menu-section :deep(.el-menu-item:hover),
.menu-section :deep(.el-sub-menu__title:hover) {
  background: var(--surface-emphasis);
  color: var(--color-primary);
}

.menu-section :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-strong));
  color: #fff;
  box-shadow: 0 12px 24px rgba(47, 91, 234, 0.22);
}

.menu-section :deep(.el-menu-item .el-icon),
.menu-section :deep(.el-sub-menu__title .el-icon) {
  margin-right: 12px;
  font-size: 18px;
}

.menu-section :deep(.el-sub-menu .el-menu) {
  background: transparent;
}

.menu-section :deep(.el-menu--inline) {
  padding: 2px 8px 8px;
  overflow: hidden;
}

.menu-section :deep(.el-menu--inline .el-menu-item) {
  height: 42px;
  margin: 4px 0 0;
  padding-left: 52px !important;
  width: 100%;
  max-width: 100%;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.58);
  color: var(--text-secondary);
  box-shadow: inset 0 0 0 1px rgba(148, 163, 184, 0.08);
  box-sizing: border-box;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.menu-section :deep(.el-menu--inline .el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.9);
  color: var(--color-primary);
}

.menu-section :deep(.el-menu--inline .el-menu-item.is-active) {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-strong));
  color: #fff;
  box-shadow: 0 10px 20px rgba(47, 91, 234, 0.18);
}

.sidebar-footer {
  position: relative;
  z-index: 1;
  margin-top: auto;
  padding-top: 10px;
}

.collapse-button {
  width: 100%;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border: 1px solid var(--border-subtle);
  border-radius: 14px;
  background: var(--surface-muted);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.collapse-button:hover {
  border-color: var(--color-primary-border);
  color: var(--color-primary);
  background: var(--surface-emphasis);
}

.workspace-shell {
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.workspace-shell::before {
  content: '';
  position: absolute;
  top: 24px;
  right: 24px;
  width: 240px;
  height: 240px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(47, 91, 234, 0.08), transparent 68%);
  pointer-events: none;
}

.workspace-shell--chat {
  gap: 0;
}

.workspace-header {
  position: relative;
  overflow: hidden;
  height: auto !important;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 22px 24px;
  border-radius: 24px;
  gap: 20px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(248, 250, 252, 0.88));
}

.workspace-header::after {
  content: '';
  position: absolute;
  inset: auto 24px 0 24px;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--accent-border), transparent);
  pointer-events: none;
}

.workspace-header__left {
  position: relative;
  z-index: 1;
  min-width: 0;
}

.page-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 999px;
  background: var(--accent-soft);
  color: var(--accent-strong);
  font-size: 0.74rem;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.page-badge__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--accent);
  box-shadow: 0 0 0 4px var(--accent-soft);
  animation: badgePulse 2.4s ease-in-out infinite;
}

@keyframes badgePulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(0.85); }
}

.page-kicker {
  display: inline-block;
  color: var(--text-disabled);
  font-size: 0.76rem;
  font-weight: 800;
  letter-spacing: 0.08em;
}

.page-title {
  margin: 10px 0 0;
  color: var(--text-primary);
  font-size: 1.85rem;
  font-weight: 800;
  line-height: 1.12;
  background: var(--accent-gradient);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.page-description {
  margin: 10px 0 0;
  color: var(--text-muted);
  font-size: 0.95rem;
  line-height: 1.7;
}

.page-breadcrumb {
  margin-top: 12px;
}

.page-breadcrumb :deep(.el-breadcrumb__item) {
  font-size: 0.82rem;
}

.page-breadcrumb :deep(.el-breadcrumb__inner) {
  color: var(--text-muted);
  font-weight: 500;
}

.page-breadcrumb :deep(.el-breadcrumb__inner.is-link:hover) {
  color: var(--color-primary);
}

.workspace-header__right {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.header-shortcut,
.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  height: 42px;
  padding: 0 16px;
  border: 1px solid var(--border-subtle);
  border-radius: 999px;
  background: #fff;
  color: var(--text-secondary);
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.header-shortcut:hover,
.user-chip:hover {
  border-color: var(--color-primary-border);
  color: var(--color-primary);
  background: var(--surface-emphasis);
}

.user-chip__avatar {
  background: linear-gradient(135deg, var(--color-primary-soft), rgba(47, 91, 234, 0.18));
  color: var(--color-primary);
}

.user-chip__name {
  font-weight: 700;
}

.workspace-main {
  position: relative;
  flex: 1;
  min-height: 0;
  padding: 24px;
  border-radius: 24px;
  overflow: auto;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.9), rgba(244, 247, 252, 0.84));
}

.workspace-main--chat {
  padding: 12px;
}

.fade-text-enter-active,
.fade-text-leave-active {
  transition: opacity 0.2s ease;
}

.fade-text-enter-from,
.fade-text-leave-to {
  opacity: 0;
}

.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.page-fade-enter-from,
.page-fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

@media (max-width: 1180px) {
  .layout-shell {
    padding: 12px;
    gap: 12px;
  }

  .workspace-header {
    flex-direction: column;
  }
}

@media (max-width: 960px) {
  .layout-shell {
    height: auto;
    min-height: 100dvh;
    flex-direction: column;
    overflow: visible;
  }

  .sidebar-shell {
    width: 100% !important;
  }

  .sidebar-panel {
    height: auto;
  }

  .workspace-main {
    padding: 18px;
  }

  .workspace-main--chat {
    padding: 12px;
  }
}
</style>
