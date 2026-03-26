<template>
  <div class="dashboard">
    <!-- 天气详情模块 -->
    <div class="weather-section" v-loading="weatherLoading">
      <div class="weather-card-new" v-if="weather">
        <!-- 左侧主要信息 -->
        <div class="weather-primary">
          <div class="weather-location">
            <el-icon :size="16"><Location /></el-icon>
            <span>{{ weather.city }}, {{ weather.country }}</span>
          </div>
          <div class="weather-temp-wrap">
            <span class="weather-temp-value">{{ Math.round(weather.temperature) }}</span>
            <span class="weather-temp-unit">°C</span>
            <el-icon :size="36" class="weather-float-icon"><Sunny /></el-icon>
          </div>
          <div class="weather-condition">{{ weather.description }}</div>
          <div class="weather-feels">体感 {{ Math.round(weather.feelsLike) }}°C</div>
        </div>
        
        <!-- 右侧详细指标 -->
        <div class="weather-metrics">
          <div class="metric-item">
            <div class="metric-icon humidity">
              <el-icon :size="18"><Drizzling /></el-icon>
            </div>
            <div class="metric-data">
              <span class="metric-value">{{ weather.humidity }}%</span>
              <span class="metric-label">湿度</span>
            </div>
          </div>
          <div class="metric-item">
            <div class="metric-icon wind">
              <el-icon :size="18"><WindPower /></el-icon>
            </div>
            <div class="metric-data">
              <span class="metric-value">{{ weather.windSpeed }}</span>
              <span class="metric-label">风速 m/s</span>
            </div>
          </div>
          <div class="metric-item">
            <div class="metric-icon pressure">
              <el-icon :size="18"><Odometer /></el-icon>
            </div>
            <div class="metric-data">
              <span class="metric-value">{{ weather.pressure }}</span>
              <span class="metric-label">气压 hPa</span>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="weather-loading">
        <span>加载天气中...</span>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card stat-card--success animate-fade-up" style="animation-delay: 0ms">
        <div class="stat-card__icon">
          <el-icon :size="24"><UserFilled /></el-icon>
        </div>
        <div class="stat-card__info">
          <div class="stat-card__value">{{ displayUsers }}</div>
          <div class="stat-card__label">用户总数</div>
        </div>
        <div class="stat-card__trend trend--up">
          <el-icon><Top /></el-icon>
          <span>8%</span>
        </div>
        <div ref="sparkline1Ref" class="stat-card__sparkline"></div>
      </div>

      <div class="stat-card stat-card--danger animate-fade-up" style="animation-delay: 120ms">
        <div class="stat-card__icon">
          <el-icon :size="24"><Files /></el-icon>
        </div>
        <div class="stat-card__info">
          <div class="stat-card__value">{{ displayDicts }}</div>
          <div class="stat-card__label">字典总数</div>
        </div>
        <div class="stat-card__trend trend--up">
          <el-icon><Top /></el-icon>
          <span>5%</span>
        </div>
        <div ref="sparkline2Ref" class="stat-card__sparkline"></div>
      </div>
    </div>

    <div class="content-row">
      <!-- 快捷操作 -->
      <div class="quick-section">
        <el-card>
          <template #header>
            <div class="section-header">
              <div class="section-title">
                <el-icon><Grid /></el-icon>
                <span>快捷入口</span>
              </div>
            </div>
          </template>
          <div class="quick-grid">
            <div class="quick-item" @click="$router.push('/dict')">
              <div class="quick-item__icon quick-item__icon--success">
                <el-icon :size="24"><Files /></el-icon>
              </div>
              <div class="quick-item__text">字典管理</div>
            </div>
            <div class="quick-item" @click="$router.push('/user')">
              <div class="quick-item__icon quick-item__icon--info">
                <el-icon :size="24"><UserFilled /></el-icon>
              </div>
              <div class="quick-item__text">用户管理</div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 系统信息 -->
      <div class="info-section">
        <el-card>
          <template #header>
            <div class="section-header">
              <div class="section-title">
                <el-icon><InfoFilled /></el-icon>
                <span>系统信息</span>
              </div>
            </div>
          </template>
          <div class="info-list">
            <div class="info-item">
              <span class="info-item__label">系统名称</span>
              <span class="info-item__value">Admin Pro 管理系统</span>
            </div>
            <div class="info-item">
              <span class="info-item__label">系统版本</span>
              <span class="info-item__value">
                <el-tag size="small" type="primary">v1.0.0</el-tag>
              </span>
            </div>
            <div class="info-item">
              <span class="info-item__label">前端框架</span>
              <span class="info-item__value">Vue 3 + Element Plus</span>
            </div>
            <div class="info-item">
              <span class="info-item__label">后端框架</span>
              <span class="info-item__value">Spring Boot 3</span>
            </div>
            <div class="info-item">
              <span class="info-item__label">当前用户</span>
              <span class="info-item__value">
                <el-tag size="small">{{ username }}</el-tag>
              </span>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 最近活动 -->
    <el-card class="activity-card">
      <template #header>
        <div class="section-header">
          <div class="section-title">
            <el-icon><Clock /></el-icon>
            <span>最近活动</span>
          </div>
          <el-button type="primary" link>查看全部</el-button>
        </div>
      </template>
      <div class="activity-list">
        <transition-group name="activity-list">
          <div
            v-for="(activity, index) in activities"
            :key="index"
            class="activity-item animate-fade-up"
            :style="{ animationDelay: `${index * 80}ms` }"
          >
            <div :class="['activity-dot', `activity-dot--${activity.type}`]"></div>
            <div class="activity-content">
              <div class="activity-text">{{ activity.content }}</div>
              <div class="activity-time">{{ activity.time }}</div>
            </div>
          </div>
        </transition-group>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { getWeatherByCity } from '@/api/weather'

const username = computed(() => localStorage.getItem('username') || '管理员')

const weather = ref(null)
const weatherLoading = ref(false)

const stats = ref({ users: 0, dicts: 0 })
const displayUsers = ref(0)
const displayDicts = ref(0)

const sparkline1Ref = ref(null)
const sparkline2Ref = ref(null)
let chart1 = null
let chart2 = null

const activities = ref([
  { content: '系统已成功启动', time: '刚刚', type: 'success' },
  { content: '用户登录系统', time: '5 分钟前', type: 'primary' },
  { content: '数据库连接正常', time: '10 分钟前', type: 'success' },
  { content: '缓存服务已启动', time: '15 分钟前', type: 'info' },
  { content: '定时任务执行完成', time: '30 分钟前', type: 'warning' }
])

function easeOutQuad(t) {
  return t * (2 - t)
}

function countUp(target, setter, duration = 1200) {
  const startTime = performance.now()
  function update(currentTime) {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
    setter(Math.floor(target * easeOutQuad(progress)))
    if (progress < 1) requestAnimationFrame(update)
  }
  requestAnimationFrame(update)
}

function initSparkline(el, data, color) {
  if (!el) return null
  const chart = echarts.init(el)
  chart.setOption({
    animation: true,
    animationDuration: 1000,
    grid: { top: 4, bottom: 4, left: 4, right: 4 },
    xAxis: { type: 'category', show: false, data: data.map((_, i) => i) },
    yAxis: { type: 'value', show: false, min: Math.min(...data) * 0.95 },
    series: [{
      type: 'line',
      data,
      smooth: true,
      symbol: 'none',
      lineStyle: { color, width: 2 },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: color + '60' },
            { offset: 1, color: color + '00' }
          ]
        }
      }
    }]
  })
  return chart
}

const loadWeather = async () => {
  weatherLoading.value = true
  try {
    const res = await getWeatherByCity('成都')
    weather.value = res.data
  } catch (error) {
    console.error('获取天气失败:', error)
  } finally {
    weatherLoading.value = false
  }
}

onMounted(() => {
  loadWeather()

  setTimeout(() => {
    stats.value = { users: 156, dicts: 8 }
    countUp(156, v => { displayUsers.value = v })
    countUp(8, v => { displayDicts.value = v })

    setTimeout(() => {
      chart1 = initSparkline(sparkline1Ref.value, [120, 132, 135, 138, 140, 148, 156], '#10b981')
      chart2 = initSparkline(sparkline2Ref.value, [5, 5, 6, 6, 7, 7, 8], '#ef4444')
    }, 100)
  }, 300)
})

onBeforeUnmount(() => {
  chart1?.dispose()
  chart2?.dispose()
})
</script>

<style lang="scss" scoped>
$primary: #6366f1;
$success: #10b981;
$warning: #f59e0b;
$danger: #ef4444;
$info: #3b82f6;

.dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.weather-section {
  margin-bottom: 0;
}

.weather-card-new {
  display: flex;
  align-items: stretch;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

  .weather-primary {
    flex: 0 0 280px;
    background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
    padding: 28px 32px;
    color: #fff;
    display: flex;
    flex-direction: column;

    .weather-location {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      opacity: 0.9;
      margin-bottom: 12px;
    }

    .weather-temp-wrap {
      display: flex;
      align-items: flex-start;
      margin-bottom: 8px;

      .weather-temp-value {
        font-size: 64px;
        font-weight: 300;
        line-height: 1;
        letter-spacing: -2px;
      }

      .weather-temp-unit {
        font-size: 24px;
        font-weight: 300;
        margin-top: 8px;
      }

      .weather-float-icon {
        margin-left: 12px;
        margin-top: 8px;
        opacity: 0.85;
        animation: float 3s ease-in-out infinite;
        color: #fde68a;
      }
    }

    .weather-condition {
      font-size: 18px;
      font-weight: 500;
      margin-bottom: 4px;
    }

    .weather-feels {
      font-size: 13px;
      opacity: 0.8;
    }
  }

  .weather-metrics {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-around;
    padding: 24px 40px;
    background: #fff;

    .metric-item {
      display: flex;
      align-items: center;
      gap: 14px;

      .metric-icon {
        width: 44px;
        height: 44px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;

        &.humidity {
          background: rgba(59, 130, 246, 0.1);
          color: #3b82f6;
        }

        &.wind {
          background: rgba(16, 185, 129, 0.1);
          color: #10b981;
        }

        &.pressure {
          background: rgba(245, 158, 11, 0.1);
          color: #f59e0b;
        }
      }

      .metric-data {
        display: flex;
        flex-direction: column;
        gap: 2px;

        .metric-value {
          font-size: 20px;
          font-weight: 600;
          color: #1e293b;
        }

        .metric-label {
          font-size: 12px;
          color: #94a3b8;
        }
      }
    }
  }
}

.weather-loading {
  background: #fff;
  border-radius: 16px;
  padding: 48px;
  text-align: center;
  color: #94a3b8;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
  }

  &::before {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    width: 120px;
    height: 120px;
    border-radius: 50%;
    transform: translate(30%, -30%);
    opacity: 0.1;
  }

  &--primary {
    &::before { background: $primary; }
    .stat-card__icon { background: rgba($primary, 0.1); color: $primary; }
  }

  &--success {
    &::before { background: $success; }
    .stat-card__icon { background: rgba($success, 0.1); color: $success; }
  }

  &--warning {
    &::before { background: $warning; }
    .stat-card__icon { background: rgba($warning, 0.1); color: $warning; }
  }

  &--danger {
    &::before { background: $danger; }
    .stat-card__icon { background: rgba($danger, 0.1); color: $danger; }
  }

  &__icon {
    width: 56px;
    height: 56px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &__info {
    flex: 1;
  }

  &__value {
    font-size: 28px;
    font-weight: 700;
    color: #1e293b;
    line-height: 1.2;
  }

  &__label {
    font-size: 14px;
    color: #64748b;
    margin-top: 4px;
  }

  &__trend {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    font-weight: 500;
    padding: 4px 8px;
    border-radius: 6px;

    &.trend--up {
      background: rgba($success, 0.1);
      color: $success;
    }

    &.trend--down {
      background: rgba($danger, 0.1);
      color: $danger;
    }
  }

  &__sparkline {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 52px;
    pointer-events: none;
    opacity: 0.7;
  }
}

.content-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #1e293b;

    .el-icon {
      color: $primary;
    }
  }
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px 16px;
  background: #f8fafc;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: #f1f5f9;
    transform: translateY(-2px);

    .quick-item__icon {
      transform: scale(1.1);
    }
  }

  &__icon {
    width: 56px;
    height: 56px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: transform 0.2s ease;

    &--primary { background: rgba($primary, 0.1); color: $primary; }
    &--success { background: rgba($success, 0.1); color: $success; }
    &--warning { background: rgba($warning, 0.1); color: $warning; }
    &--info { background: rgba($info, 0.1); color: $info; }
  }

  &__text {
    font-size: 14px;
    font-weight: 500;
    color: #475569;
  }
}

.info-list {
  display: flex;
  flex-direction: column;
}

.info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 0;
  border-bottom: 1px solid #f1f5f9;

  &:last-child {
    border-bottom: none;
  }

  &__label {
    font-size: 14px;
    color: #64748b;
  }

  &__value {
    font-size: 14px;
    font-weight: 500;
    color: #1e293b;
  }
}

.activity-card {
  :deep(.el-card__body) {
    padding: 0 !important;
  }
}

.activity-list {
  max-height: 300px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px 24px;
  transition: background 0.2s ease;

  &:hover {
    background: #f8fafc;
  }

  &:not(:last-child) {
    border-bottom: 1px solid #f1f5f9;
  }
}

.activity-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;

  &--primary { background: $primary; }
  &--success { background: $success; }
  &--warning { background: $warning; }
  &--danger { background: $danger; }
  &--info { background: $info; }
}

.activity-content {
  flex: 1;
}

.activity-text {
  font-size: 14px;
  color: #1e293b;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #94a3b8;
}

// 活动列表过渡动画
.activity-list-enter-active {
  transition: all 0.4s ease;
}
.activity-list-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

// 浮动动画（天气图标）
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50%       { transform: translateY(-6px); }
}

@media (max-width: 1200px) {
  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .content-row {
    grid-template-columns: 1fr;
  }
}
</style>
