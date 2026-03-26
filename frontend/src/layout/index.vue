<template>
  <el-container class="layout-container" :data-theme="currentTheme">
    <!-- 主题装饰效果 -->
    <ThemeDecorations />
    
    <!-- 侧边栏 - 悬浮设计 -->
    <el-aside :width="isCollapse ? '80px' : '260px'" class="sidebar-wrapper">
      <div class="sidebar glass-container">
        <div class="logo">
          <div class="logo-icon">
            <img v-if="currentTheme === 'liuyifei'" src="/themes/liuyifei.jpg" class="theme-logo-img" />
            <el-icon v-else size="24"><TrendCharts /></el-icon>
          </div>
          <transition name="fade-text">
            <span v-show="!isCollapse" class="logo-text">{{ currentTheme === 'liuyifei' ? '亦菲·仙踪' : 'Premium Admin' }}</span>
          </transition>
        </div>
        
        <div class="menu-wrapper">
          <el-menu
            :default-active="activeMenu"
            :collapse="isCollapse"
            :collapse-transition="false"
            active-text-color="#ffffff"
            router
          >
            <el-menu-item index="/dashboard">
              <el-icon><Monitor /></el-icon>
              <template #title>数字化看板</template>
            </el-menu-item>

            <el-sub-menu index="ai">
              <template #title>
                <el-icon><Lightning /></el-icon>
                <span>AI能力</span>
              </template>
              <el-menu-item index="/ai/chat">AI智能对话</el-menu-item>
              <el-menu-item index="/ai/resume">AI简历助手</el-menu-item>
            </el-sub-menu>


            <el-menu-item index="/news/hot">
              <el-icon><Bell /></el-icon>
              <template #title>热点新闻</template>
            </el-menu-item>

            <el-sub-menu index="finance">
              <template #title>
                <el-icon><TrendCharts /></el-icon>
                <span>智能投研</span>
              </template>
              <el-menu-item index="/fund/ranking">基金排行</el-menu-item>
              <el-menu-item index="/fund/analysis">AI走势分析</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="management">
              <template #title>
                <el-icon><FolderOpened /></el-icon>
                <span>系统管理</span>
              </template>
              <el-menu-item index="/prompt">提示工程</el-menu-item>
              <el-menu-item index="/ai/models">基座模型管理</el-menu-item>
              <el-menu-item index="/dict">数据字典</el-menu-item>
              <el-menu-item index="/user">用户管理</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="log">
              <template #title>
                <el-icon><Tickets /></el-icon>
                <span>日志管理</span>
              </template>
              <el-menu-item index="/log/login">登录日志</el-menu-item>
              <el-menu-item index="/log/operation">操作日志</el-menu-item>
            </el-sub-menu>
          </el-menu>
        </div>
        
        <div class="sidebar-footer">
          <div class="collapse-trigger" @click="toggleCollapse">
            <el-icon :size="20">
              <Expand v-if="isCollapse" />
              <Fold v-else />
            </el-icon>
          </div>
        </div>
      </div>
    </el-aside>

    <el-container class="main-wrapper">
      <!-- 顶部导航 - 玻璃特效 -->
      <el-header class="header glass-container">
        <div class="header-left">
          <div class="breadcrumb-custom">
            <span class="active-page">{{ $route.meta.title || '概览' }}</span>
            <div class="breadcrumb-path">
              <span>系统</span>
              <el-icon :size="10"><ArrowRight /></el-icon>
              <span>{{ $route.meta.title }}</span>
            </div>
          </div>
        </div>
        
        <div class="header-right">
          <div class="header-tools">
            <div class="weather-pill" v-if="weather">
              <el-icon><Sunny /></el-icon>
              <span>{{ weather.city }} · {{ Math.round(weather.temperature) }}°C</span>
            </div>
            
            <ThemeSwitcher />
            
            <el-divider direction="vertical" />
            
            <el-dropdown @command="handleCommand" trigger="click">
              <div class="user-pill glass-container">
                <el-avatar :size="32" class="user-avatar" :src="currentTheme === 'liuyifei' ? '/themes/liuyifei.jpg' : ''">
                  <el-icon v-if="currentTheme !== 'liuyifei'"><UserFilled /></el-icon>
                </el-avatar>
                <span class="name-text">{{ username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="premium-dropdown">
                  <el-dropdown-item command="profile"><el-icon><User /></el-icon>账户详情</el-dropdown-item>
                  <el-dropdown-item command="logout" divided><el-icon><SwitchButton /></el-icon>安全退出</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  Monitor, DataLine, Lightning, FolderOpened, Tickets, TrendCharts,
  Expand, Fold, Sunny, UserFilled, ArrowDown, User, SwitchButton,
  ArrowRight, Bell, DocumentChecked
} from '@element-plus/icons-vue'
import { getWeatherByCity } from '@/api/weather'
import ThemeSwitcher from '@/components/ThemeSwitcher.vue'
import ThemeDecorations from '@/components/ThemeDecorations.vue'
import { useThemeStore } from '@/stores/theme'

const route = useRoute()
const router = useRouter()
const themeStore = useThemeStore()
const isCollapse = ref(false)
const weather = ref(null)

const currentTheme = computed(() => themeStore.currentTheme)
const activeMenu = computed(() => route.path)
const username = computed(() => localStorage.getItem('username') || 'Guest')

const loadWeather = async () => {
  try {
    const res = await getWeatherByCity('北京')
    weather.value = res.data
  } catch (error) {
    console.error('Weather sync failed:', error)
  }
}

onMounted(() => {
  loadWeather()
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('准备离开了吗？您的会话将被清除。', '登出账户', {
      confirmButtonText: '确定登出',
      cancelButtonText: '再待一会儿',
      type: 'info',
      confirmButtonClass: 'premium-btn'
    }).then(() => {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      router.push('/login')
    })
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
  display: flex;
  background: var(--content-bg);
  padding: 20px;
  gap: 20px;
  overflow: hidden;
}

.sidebar-wrapper {
  transition: width $transition-base;
  height: 100%;
  
  .sidebar {
    height: 100%;
    border-radius: $radius-lg;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    background: var(--sidebar-bg, rgba(255, 255, 255, 0.8));
  }

  .logo {
    height: 90px;
    padding: 0 24px;
    display: flex;
    align-items: center;
    gap: 16px;
    
    .logo-icon {
      width: 44px;
      height: 44px;
      background: var(--primary-color);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      box-shadow: 0 8px 16px rgba(0,0,0,0.1);
      overflow: hidden;
      
      .theme-logo-img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    .logo-text {
      font-size: 1.2rem;
      font-weight: 800;
      color: var(--text-primary);
      letter-spacing: -0.5px;
    }
  }

  .menu-wrapper {
    flex: 1;
    min-height: 0;
    padding: 12px;
    overflow-y: auto;
    overflow-x: hidden;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(99, 102, 241, 0.18);
      border-radius: 999px;
    }

    &::-webkit-scrollbar-thumb:hover {
      background: rgba(99, 102, 241, 0.32);
    }
    
    :deep(.el-menu) {
      border: none;
      background: transparent;
      
      .el-menu-item, .el-sub-menu__title {
        height: 54px;
        margin-bottom: 6px;
        border-radius: 14px;
        color: var(--text-secondary);
        font-weight: 500;
        
        &:hover {
          background: rgba(99, 102, 241, 0.08) !important;
          color: var(--primary-color);
        }
        
        &.is-active {
          background: var(--primary-color) !important;
          color: #fff !important;
          box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
        }
        
        .el-icon {
          font-size: 20px;
          margin-right: 12px;
        }
      }
    }
  }

  .sidebar-footer {
    padding: 20px;
    .collapse-trigger {
      height: 48px;
      background: rgba(0,0,0,0.03);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      color: var(--text-secondary);
      transition: all $transition-fast;
      
      &:hover {
        background: rgba(99, 102, 241, 0.1);
        color: var(--primary-color);
      }
    }
  }
}

.main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: 100%;
}

.header {
  height: $header-height !important;
  border-radius: $radius-lg;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  background: var(--header-bg, rgba(255,255,255,0.8));

  .breadcrumb-custom {
    display: flex;
    flex-direction: column;
    
    .active-page {
      font-size: 1.4rem;
      font-weight: 800;
      color: var(--text-primary);
    }
    
    .breadcrumb-path {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 0.8rem;
      color: var(--text-secondary);
      opacity: 0.6;
      font-weight: 500;
      margin-top: 2px;
      
      .el-icon {
        margin-top: 1px;
      }
    }
  }

  .header-tools {
    display: flex;
    align-items: center;
    gap: 20px;
    
    .weather-pill {
      background: rgba(0,0,0,0.04);
      padding: 8px 16px;
      border-radius: 100px;
      font-size: 0.9rem;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 8px;
      color: var(--text-secondary);
      
      .el-icon { color: #f59e0b; font-size: 1.1rem; }
    }
    
    .user-pill {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 6px 14px 6px 6px;
      border-radius: 100px;
      cursor: pointer;
      transition: all $transition-fast;
      
      &:hover { transform: scale(1.02); filter: brightness(1.05); }
      
      .user-avatar { border: 2px solid #fff; }
      .name-text { font-weight: 700; font-size: 0.95rem; color: var(--text-primary); }
    }
  }
}

.main-content {
  padding: 0 !important;
  border-radius: $radius-lg;
  overflow-y: auto;
  
  &::-webkit-scrollbar { width: 0; }
}

.page-fade-enter-active, .page-fade-leave-active {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}
.page-fade-enter-from { opacity: 0; transform: scale(0.97) translateY(12px); }
.page-fade-leave-to { opacity: 0; transform: scale(1.01) translateY(-8px); }
</style>
