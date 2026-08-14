<template>
  <div class="dashboard-page">
    <section
      class="hero-panel"
      v-motion
      :initial="{ opacity: 0, y: 12 }"
      :enter="{ opacity: 1, y: 0, transition: { duration: 320 } }"
    >
      <div class="hero-panel__aurora hero-panel__aurora--one" />
      <div class="hero-panel__aurora hero-panel__aurora--two" />

      <div class="hero-panel__content">
        <div class="hero-panel__main">
          <div class="hero-panel__heading">
            <div>
              <div class="hero-panel__eyebrow">
                <span class="hero-panel__dot"></span>
                <span>AI-world · 工作台</span>
              </div>
              <h2 class="section-title">你好，{{ username }}</h2>
              <p class="section-subtitle">今日概览 · {{ todayLabel }} · {{ weatherCity }} {{ weatherTemperature }}</p>
            </div>
            <div class="hero-status">
              <span class="hero-status__dot" />
              <span>{{ loading ? '同步中' : '已同步' }}</span>
            </div>
          </div>

          <div class="hero-actions">
            <el-button type="primary" @click="$router.push('/ai/chat')">
              <el-icon><ChatDotRound /></el-icon>
              开启 AI 对话
            </el-button>
            <el-button @click="$router.push('/news/hot')">
              <el-icon><Bell /></el-icon>
              查看热点
            </el-button>
            <el-button @click="$router.push('/user')">
              <el-icon><UserFilled /></el-icon>
              用户管理
            </el-button>
          </div>
        </div>

        <aside class="hero-panel__visual" aria-label="工作台任务处理动画">
          <div class="hero-lottie">
            <dotlottie-wc
              class="hero-lottie__player"
              src="/animations/working.lottie"
              :autoplay="!prefersReducedMotion"
              :loop="!prefersReducedMotion"
              speed="0.82"
            />
          </div>
          <div class="hero-panel__visual-copy">
            <span>运行状态</span>
            <strong>{{ loading ? '正在同步工作台数据' : '工作台准备就绪' }}</strong>
          </div>
        </aside>
      </div>
    </section>

    <section class="stat-strip">
      <StatCard
        label="用户总数"
        :value="stats.userTotal"
        :hint="`${stats.activeUsers} 启用`"
        :icon="UserFilled"
        tone="primary"
        :delay="0"
      >
        <template #chart>
          <Sparkline :data="trend.users" color="var(--accent-ai)" />
        </template>
      </StatCard>
      <StatCard
        label="字典总数"
        :value="stats.dictTotal"
        :hint="`${stats.enabledDicts} 启用`"
        :icon="Collection"
        tone="system"
        :delay="80"
      >
        <template #chart>
          <Sparkline :data="trend.dicts" color="var(--accent-system)" />
        </template>
      </StatCard>
      <StatCard
        label="健康度"
        :value="healthScore"
        format="percent"
        hint="综合启用率"
        :icon="DataAnalysis"
        tone="tools"
        :delay="160"
      >
        <template #chart>
          <Sparkline :data="trend.health" color="var(--accent-tools)" />
        </template>
      </StatCard>
      <StatCard
        :label="weatherCity"
        :value="weatherTemp"
        :unit="'°C'"
        :hint="weatherHeadline"
        :icon="LocationFilled"
        tone="news"
        :delay="240"
        :animate="Number.isFinite(weatherTemp)"
      >
        <template #chart>
          <Sparkline :data="trend.weather" color="var(--accent-news)" />
        </template>
      </StatCard>
    </section>

    <section class="chart-panel">
      <div class="chart-panel__head">
        <div>
          <div class="chart-panel__eyebrow">近 7 日活跃度</div>
          <h3 class="chart-panel__title">工作台流量</h3>
        </div>
        <div class="chart-panel__legend">
          <span class="chart-panel__legend-item"><i class="dot dot--primary"></i>访问</span>
          <span class="chart-panel__legend-item"><i class="dot dot--ai"></i>AI 对话</span>
        </div>
      </div>
      <div ref="chartRef" class="chart-panel__canvas"></div>
    </section>

    <section class="weather-showcase" :class="`weather-showcase--${weatherTheme}`">
      <div class="weather-showcase__copy">
        <div class="weather-heading">
          <div>
            <h3 class="weather-title">{{ weatherCity }}</h3>
            <p class="weather-subtitle">{{ weatherHeadline }}</p>
          </div>
          <div class="weather-badge">
            <el-icon><LocationFilled /></el-icon>
            <span>{{ weatherPayload.city || weatherCity }}</span>
          </div>
        </div>

        <div class="weather-temperature">
          <strong>{{ weatherTemperature }}</strong>
          <span>{{ weatherFeelsLike }}</span>
        </div>

        <div class="weather-metrics">
          <div class="weather-metric">
            <span class="weather-metric__label">湿度</span>
            <strong class="weather-metric__value">{{ weatherHumidity }}</strong>
          </div>
          <div class="weather-metric">
            <span class="weather-metric__label">风速</span>
            <strong class="weather-metric__value">{{ weatherWind }}</strong>
          </div>
          <div class="weather-metric">
            <span class="weather-metric__label">更新</span>
            <strong class="weather-metric__value">{{ weatherUpdatedAt }}</strong>
          </div>
        </div>

        <div class="weather-actions">
          <el-select
            v-model="weatherCity"
            class="weather-actions__select"
            size="large"
            :disabled="weatherLoading"
            @change="loadWeather"
          >
            <el-option v-for="city in weatherCities" :key="city" :label="city" :value="city" />
          </el-select>
          <el-button
            class="weather-actions__button"
            type="primary"
            plain
            :icon="RefreshRight"
            :loading="weatherLoading"
            @click="loadWeather"
          >
            刷新
          </el-button>
        </div>
      </div>
    </section>

    <section class="quick-panel">
      <div class="quick-panel__head">
        <h3>快捷入口</h3>
        <span class="quick-panel__hint">高频功能，一键直达</span>
      </div>
      <div class="quick-grid">
        <button
          v-for="(action, i) in quickActions"
          :key="action.title"
          class="quick-card"
          :class="`tone-${action.tone}`"
          type="button"
          v-motion
          :initial="{ opacity: 0, y: 12 }"
          :visibleOnce="{ opacity: 1, y: 0, transition: { duration: 260, delay: 100 + i * 70 } }"
          @click="$router.push(action.to)"
        >
          <div class="quick-card__icon">
            <el-icon><component :is="action.icon" /></el-icon>
          </div>
          <div class="quick-card__copy">
            <span class="quick-card__title">{{ action.title }}</span>
            <span class="quick-card__desc">{{ action.desc }}</span>
          </div>
          <el-icon class="quick-card__arrow"><ArrowRight /></el-icon>
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, nextTick } from 'vue'
import {
  ArrowRight,
  Bell,
  Collection,
  Cpu,
  ChatDotRound,
  DataAnalysis,
  Document,
  LocationFilled,
  RefreshRight,
  Setting,
  UserFilled
} from '@element-plus/icons-vue'
import axios from 'axios'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import StatCard from '@/components/StatCard.vue'
import Sparkline from '@/components/Sparkline.vue'

echarts.use([LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const loading = ref(false)
const prefersReducedMotion = ref(false)
const stats = ref({
  userTotal: 0,
  activeUsers: 0,
  disabledUsers: 0,
  dictTotal: 0,
  enabledDicts: 0,
  disabledDicts: 0
})

const username = computed(() => localStorage.getItem('username') || '访客')
const todayLabel = computed(() => {
  const d = new Date()
  const week = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][d.getDay()]
  return `${d.getMonth() + 1}月${d.getDate()}日 · ${week}`
})

const weatherCity = ref('成都')
const weatherCities = ['成都', '北京', '上海', '广州', '深圳', '杭州', '南京', '武汉', '重庆', '西安']
const weatherLoading = ref(false)
const weatherPayload = ref({})
const weatherError = ref(false)

const trend = ref({
  users: [12, 18, 14, 22, 26, 24, 30],
  dicts: [8, 9, 12, 11, 14, 16, 15],
  health: [82, 85, 84, 88, 90, 92, 94],
  weather: [16, 18, 22, 20, 24, 21, 23]
})

const healthScore = computed(() => {
  const { userTotal, activeUsers, dictTotal, enabledDicts } = stats.value
  if (!userTotal && !dictTotal) return 100
  const userRate = userTotal ? (activeUsers / userTotal) : 1
  const dictRate = dictTotal ? (enabledDicts / dictTotal) : 1
  return Math.round((userRate * 50 + dictRate * 50))
})

const weatherTheme = computed(() => {
  const desc = (weatherPayload.value.weather || '').toLowerCase()
  if (/雪/.test(desc)) return 'snowy'
  if (/雨|阵雨/.test(desc)) return 'rainy'
  if (/风/.test(desc)) return 'windy'
  if (/云|阴/.test(desc)) return 'cloudy'
  return 'sunny'
})

const weatherTemperature = computed(() => {
  const t = weatherPayload.value.temperature
  return t != null ? `${t}°C` : '--'
})

const weatherTemp = computed(() => {
  const t = weatherPayload.value.temperature
  return t != null ? Number(t) : 0
})

const weatherFeelsLike = computed(() => {
  const t = weatherPayload.value.feelsLike
  return t != null ? `体感 ${t}°C` : ''
})

const weatherHeadline = computed(() => weatherPayload.value.weather || '加载中...')

const weatherHumidity = computed(() => {
  const h = weatherPayload.value.humidity
  return h != null ? `${h}%` : '--'
})

const weatherWind = computed(() => {
  const w = weatherPayload.value.windSpeed
  const d = weatherPayload.value.windDirection || ''
  return w != null ? `${d} ${w}m/s` : '--'
})

const weatherUpdatedAt = computed(() => {
  const t = weatherPayload.value.observeTime
  if (!t) return '--'
  try {
    const d = new Date(t)
    return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
  } catch { return '--' }
})

const quickActions = [
  { title: 'AI 聊天', desc: '和智能助手对话', icon: ChatDotRound, tone: 'ai', to: '/ai/chat' },
  { title: '模型管理', desc: '配置可用 AI 模型', icon: Cpu, tone: 'ai', to: '/ai/models' },
  { title: '热点新闻', desc: '多平台趋势速览', icon: Bell, tone: 'news', to: '/news/hot' },
  { title: '用户管理', desc: '账户、权限、状态', icon: UserFilled, tone: 'system', to: '/user' },
  { title: '字典配置', desc: '维护系统字典项', icon: Collection, tone: 'system', to: '/dict' },
  { title: '操作日志', desc: '追踪后台变更', icon: Document, tone: 'system', to: '/log/operation' }
]

const chartRef = ref(null)
let chart = null
let resizeObs = null
let motionQuery = null

const syncMotionPreference = () => {
  prefersReducedMotion.value = Boolean(motionQuery?.matches)
}

const days = () => {
  const arr = []
  const now = new Date()
  for (let i = 6; i >= 0; i--) {
    const d = new Date(now)
    d.setDate(now.getDate() - i)
    arr.push(`${d.getMonth() + 1}/${d.getDate()}`)
  }
  return arr
}

const resolveVar = (name) => {
  const val = getComputedStyle(document.documentElement).getPropertyValue(name).trim()
  return val || '#2f5bea'
}

const renderChart = () => {
  if (!chart) return
  const primary = resolveVar('--color-primary')
  const ai = resolveVar('--accent-ai')
  chart.setOption({
    grid: { left: 24, right: 24, top: 20, bottom: 30 },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(15, 23, 42, 0.92)',
      borderWidth: 0,
      textStyle: { color: '#fff', fontSize: 12 },
      padding: [8, 12]
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: days(),
      axisLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.3)' } },
      axisLabel: { color: resolveVar('--text-muted'), fontSize: 11 },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.16)', type: 'dashed' } },
      axisLabel: { color: resolveVar('--text-muted'), fontSize: 11 },
      axisLine: { show: false },
      axisTick: { show: false }
    },
    series: [
      {
        name: '访问',
        type: 'line',
        smooth: true,
        showSymbol: false,
        lineStyle: { color: primary, width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: primary },
            { offset: 1, color: 'rgba(255,255,255,0)' }
          ]),
          opacity: 0.28
        },
        data: [128, 152, 143, 176, 188, 210, 232]
      },
      {
        name: 'AI 对话',
        type: 'line',
        smooth: true,
        showSymbol: false,
        lineStyle: { color: ai, width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: ai },
            { offset: 1, color: 'rgba(255,255,255,0)' }
          ]),
          opacity: 0.22
        },
        data: [42, 58, 66, 72, 88, 96, 104]
      }
    ]
  })
}

const loadStats = async () => {
  loading.value = true
  try {
    const [userRes, dictRes] = await Promise.all([
      axios.get('/api/users/stats'),
      axios.get('/api/dicts/stats')
    ])
    const u = userRes.data?.data || userRes.data || {}
    const d = dictRes.data?.data || dictRes.data || {}
    stats.value = {
      userTotal: u.total ?? 0,
      activeUsers: u.activeCount ?? 0,
      disabledUsers: u.disabledCount ?? 0,
      dictTotal: d.total ?? 0,
      enabledDicts: d.enabledCount ?? 0,
      disabledDicts: d.disabledCount ?? 0
    }
  } catch (e) {
    console.error('加载统计失败', e)
  } finally {
    loading.value = false
  }
}

const loadWeather = async () => {
  weatherLoading.value = true
  weatherError.value = false
  try {
    const res = await axios.get('/api/weather', { params: { city: weatherCity.value } })
    weatherPayload.value = res.data?.data || res.data || {}
  } catch (e) {
    console.error('加载天气失败', e)
    weatherError.value = true
    weatherPayload.value = {}
  } finally {
    weatherLoading.value = false
  }
}

onMounted(async () => {
  if (typeof window !== 'undefined' && window.matchMedia) {
    motionQuery = window.matchMedia('(prefers-reduced-motion: reduce)')
    syncMotionPreference()
    motionQuery.addEventListener?.('change', syncMotionPreference)
  }
  loadStats()
  loadWeather()
  await nextTick()
  if (chartRef.value) {
    chart = echarts.init(chartRef.value)
    renderChart()
    resizeObs = new ResizeObserver(() => chart && chart.resize())
    resizeObs.observe(chartRef.value)
  }
})

onBeforeUnmount(() => {
  motionQuery?.removeEventListener?.('change', syncMotionPreference)
  if (resizeObs) resizeObs.disconnect()
  if (chart) chart.dispose()
})
</script>

<style lang="scss" scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.hero-panel {
  position: relative;
  overflow: hidden;
  padding: 32px;
  border: 1px solid rgba(148, 163, 184, 0.16);
  border-radius: 28px;
  background:
    radial-gradient(circle at 82% 20%, rgba(56, 189, 248, 0.32), transparent 40%),
    radial-gradient(circle at 12% 70%, rgba(52, 211, 153, 0.18), transparent 40%),
    linear-gradient(135deg, #0b1024 0%, #101a3f 45%, #1a3260);
  color: #fff;
  box-shadow: 0 30px 70px rgba(15, 23, 42, 0.22);
}

.hero-panel__aurora {
  position: absolute;
  border-radius: 999px;
  filter: blur(24px);
  pointer-events: none;
  opacity: 0.85;
  animation: heroFloat 14s ease-in-out infinite;
}

.hero-panel__aurora--one {
  top: -90px;
  right: 10%;
  width: 260px;
  height: 260px;
  background: radial-gradient(circle, rgba(56, 189, 248, 0.55), transparent 68%);
}

.hero-panel__aurora--two {
  bottom: -140px;
  left: -40px;
  width: 320px;
  height: 320px;
  background: radial-gradient(circle, rgba(52, 211, 153, 0.35), transparent 68%);
  animation-direction: reverse;
  animation-duration: 18s;
}

@keyframes heroFloat {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-16px, 12px); }
}

.hero-panel__content {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(190px, 240px);
  gap: 26px;
  align-items: center;
}

.hero-panel__main {
  display: flex;
  flex-direction: column;
  gap: 22px;
  min-width: 0;
}

.hero-panel__heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.hero-panel__eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 5px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.14);
  color: rgba(255, 255, 255, 0.86);
  font-size: 0.76rem;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.hero-panel__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #7dd3fc;
  box-shadow: 0 0 0 4px rgba(125, 211, 252, 0.24);
}

.section-title {
  margin: 14px 0 0;
  font-size: clamp(1.8rem, 3vw, 2.6rem);
  font-weight: 900;
  line-height: 1.05;
  letter-spacing: -0.02em;
  background: linear-gradient(135deg, #ffffff, #c4d9ff 60%, #7dd3fc);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.section-subtitle {
  margin: 10px 0 0;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.95rem;
}

.hero-status {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.14);
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.84rem;
  white-space: nowrap;
}

.hero-status__dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: #34d399;
  box-shadow: 0 0 0 6px rgba(52, 211, 153, 0.16);
  animation: heroPulse 2.4s ease-in-out infinite;
}

@keyframes heroPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-panel__visual {
  position: relative;
  overflow: hidden;
  align-self: stretch;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 218px;
  padding: 16px;
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.14), rgba(255, 255, 255, 0.06));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.16);
}

.hero-panel__visual::before {
  content: '';
  position: absolute;
  inset: 12px;
  border-radius: 20px;
  background:
    linear-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 24px 24px;
  pointer-events: none;
}

.hero-lottie {
  position: relative;
  z-index: 1;
  display: grid;
  place-items: center;
  width: min(100%, 190px);
  aspect-ratio: 1 / 1;
  margin: 0 auto;
}

.hero-lottie__player {
  display: block;
  width: 100%;
  height: 100%;
}

.hero-panel__visual-copy {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 4px;
  text-align: center;
}

.hero-panel__visual-copy span {
  color: rgba(255, 255, 255, 0.58);
  font-size: 0.76rem;
  font-weight: 700;
}

.hero-panel__visual-copy strong {
  color: rgba(255, 255, 255, 0.92);
  font-size: 0.92rem;
  line-height: 1.4;
}

.hero-actions :deep(.el-button:not(.el-button--primary)) {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.18) !important;
}

.hero-actions :deep(.el-button:not(.el-button--primary):hover) {
  background: rgba(255, 255, 255, 0.18);
  border-color: rgba(255, 255, 255, 0.3) !important;
}

.stat-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.chart-panel {
  padding: 24px;
  border-radius: 24px;
  border: 1px solid var(--border-subtle);
  background: var(--surface-base);
  box-shadow: var(--shadow-sm);
}

.chart-panel__head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.chart-panel__eyebrow {
  color: var(--text-muted);
  font-size: 0.78rem;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.chart-panel__title {
  margin: 6px 0 0;
  color: var(--text-primary);
  font-size: 1.15rem;
  font-weight: 800;
}

.chart-panel__legend {
  display: inline-flex;
  gap: 14px;
  color: var(--text-muted);
  font-size: 0.84rem;
}

.chart-panel__legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.dot--primary { background: var(--color-primary); }
.dot--ai { background: var(--accent-ai); }

.chart-panel__canvas {
  width: 100%;
  height: 260px;
}

.weather-showcase {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 16px;
  overflow: hidden;
  padding: 26px;
  border-radius: var(--radius-lg);
  border: 1px solid rgba(148, 163, 184, 0.18);
  box-shadow: var(--shadow-sm);
}

.weather-showcase--sunny {
  background: radial-gradient(circle at 82% 18%, rgba(255, 213, 79, 0.34), transparent 18%),
    linear-gradient(135deg, #11315f, #2166b5 48%, #61b0ff);
}

.weather-showcase--cloudy {
  background: radial-gradient(circle at 78% 16%, rgba(255, 255, 255, 0.22), transparent 18%),
    linear-gradient(135deg, #1f304f, #58708f 46%, #93a7bf);
}

.weather-showcase--rainy {
  background: radial-gradient(circle at 84% 16%, rgba(146, 197, 255, 0.18), transparent 18%),
    linear-gradient(135deg, #101c33, #26496c 44%, #3f7a9f);
}

.weather-showcase--windy {
  background: radial-gradient(circle at 84% 20%, rgba(255, 255, 255, 0.18), transparent 18%),
    linear-gradient(135deg, #10233f, #225a81 44%, #67b4d6);
}

.weather-showcase--snowy {
  background: radial-gradient(circle at 82% 16%, rgba(255, 255, 255, 0.42), transparent 18%),
    linear-gradient(135deg, #20334f, #5c7599 46%, #d7e2ef);
}

.weather-showcase__copy,
.weather-showcase__art {
  position: relative;
  z-index: 1;
}

.weather-showcase__copy {
  color: #fff;
}

.weather-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.weather-title {
  margin: 0;
  font-family: var(--font-display);
  font-size: clamp(1.6rem, 3vw, 2.2rem);
  font-weight: 700;
  line-height: 1.05;
  letter-spacing: -0.01em;
}

.weather-subtitle {
  margin: 8px 0 0;
  color: rgba(255, 255, 255, 0.72);
  font-size: 0.98rem;
  line-height: 1.7;
}

.weather-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  font-size: 0.86rem;
  font-weight: 600;
  white-space: nowrap;
}

.weather-temperature {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  margin-top: 18px;
}

.weather-temperature strong {
  font-family: var(--font-mono);
  font-size: clamp(2.6rem, 6vw, 4rem);
  font-weight: 600;
  line-height: 0.95;
}

.weather-temperature span {
  padding-bottom: 8px;
  color: rgba(255, 255, 255, 0.78);
  font-size: 1rem;
}

.weather-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 20px;
}

.weather-metric {
  padding: 14px 16px;
  border-radius: var(--radius-sm);
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(8px);
}

.weather-metric__label {
  display: block;
  color: rgba(255, 255, 255, 0.64);
  font-size: 0.76rem;
  letter-spacing: 0.08em;
}

.weather-metric__value {
  display: block;
  margin-top: 6px;
  color: #fff;
  font-size: 1rem;
  font-weight: 800;
}

.weather-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 18px;
}

.weather-actions__select {
  width: 168px;
}

.weather-actions__select :deep(.el-input__wrapper) {
  border-radius: var(--radius-sm);
  box-shadow: none !important;
  background: rgba(255, 255, 255, 0.16);
}

.weather-actions__select :deep(.el-input__inner) {
  color: #fff;
}

.weather-actions__button {
  height: 40px;
  border-radius: var(--radius-sm);
  border-color: rgba(255, 255, 255, 0.22);
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.weather-actions__button:hover {
  border-color: rgba(255, 255, 255, 0.42);
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
}

.quick-panel {
  padding: 22px;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-subtle);
  background: var(--surface-base);
  box-shadow: var(--shadow-sm);
}

.quick-panel__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 16px;
}

.quick-panel__head h3 {
  margin: 0;
  color: var(--text-primary);
  font-size: 1.1rem;
  font-weight: 800;
}

.quick-panel__hint {
  color: var(--text-muted);
  font-size: 0.86rem;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.quick-card {
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  gap: 14px;
  width: 100%;
  padding: 18px;
  border: 1px solid var(--border-subtle);
  border-radius: 18px;
  background: linear-gradient(160deg, var(--accent-soft) 0%, transparent 55%), var(--surface-base);
  text-align: left;
  cursor: pointer;
  transition: transform 180ms ease, box-shadow 180ms ease, border-color 180ms ease;
}

.quick-card::after {
  content: '';
  position: absolute;
  top: -60px;
  right: -60px;
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: radial-gradient(circle, var(--accent-soft), transparent 72%);
  pointer-events: none;
}

.quick-card:hover {
  transform: translateY(-3px);
  border-color: var(--accent-border);
  box-shadow: var(--shadow-strong);
}

.quick-card__icon {
  position: relative;
  z-index: 1;
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: var(--accent-soft);
  color: var(--accent-strong);
  flex-shrink: 0;
  font-size: 1.1rem;
}

.quick-card__copy {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  min-width: 0;
}

.quick-card__title {
  color: var(--text-primary);
  font-weight: 700;
}

.quick-card__desc {
  color: var(--text-muted);
  font-size: 0.82rem;
  line-height: 1.5;
}

.quick-card__arrow {
  position: relative;
  z-index: 1;
  color: var(--accent);
  transition: transform 180ms ease;
}

.quick-card:hover .quick-card__arrow {
  transform: translateX(4px);
}

@keyframes rain-fall {
  0% { transform: translateY(-16px); opacity: 0; }
  15% { opacity: 1; }
  100% { transform: translateY(130px); opacity: 0; }
}

@keyframes snow-fall {
  0% { transform: translate3d(0, -10px, 0); opacity: 0; }
  12% { opacity: 1; }
  100% { transform: translate3d(18px, 132px, 0); opacity: 0; }
}

@keyframes wind-move {
  0%, 100% { transform: translateX(0); opacity: 0.32; }
  50% { transform: translateX(18px); opacity: 1; }
}

@keyframes sparkle {
  0%, 100% { transform: scale(0.6); opacity: 0.35; }
  50% { transform: scale(1.2); opacity: 1; }
}

@media (max-width: 1200px) {
  .stat-strip { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .quick-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .weather-showcase { grid-template-columns: 1fr; }
  .hero-panel__content { grid-template-columns: minmax(0, 1fr) minmax(180px, 210px); }
}

@media (max-width: 768px) {
  .hero-panel__content { grid-template-columns: 1fr; }
  .hero-panel__heading,
  .weather-heading { flex-direction: column; align-items: flex-start; }
  .stat-strip { grid-template-columns: 1fr; }
  .quick-grid { grid-template-columns: 1fr; }
  .weather-showcase { padding: 20px; }
  .hero-panel { padding: 22px; }
  .hero-panel__visual { min-height: 190px; }
  .hero-lottie { width: min(100%, 160px); }
  .weather-scene { width: 100%; max-width: 280px; }
}
</style>
