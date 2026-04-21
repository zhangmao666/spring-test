<template>
  <div class="dashboard-page">
    <section class="overview-panel">
      <div class="overview-panel__copy">
        <span class="section-kicker">SYSTEM OVERVIEW</span>
        <h2 class="section-title">工作台</h2>
        <p class="section-desc">
          查看系统关键数据、常用入口和最近状态，也能顺手看一眼当前城市的实时天气。
        </p>
      </div>

      <div class="overview-panel__actions">
        <el-button type="primary" @click="$router.push('/user')">
          <el-icon><UserFilled /></el-icon>
          进入用户管理
        </el-button>
        <el-button @click="$router.push('/dict')">
          <el-icon><Collection /></el-icon>
          查看字典配置
        </el-button>
      </div>
    </section>

    <section class="stat-grid">
      <article
        v-for="card in statCards"
        :key="card.key"
        class="stat-card"
        :class="`stat-card--${card.tone}`"
      >
        <div class="stat-card__icon">
          <el-icon :size="20"><component :is="card.icon" /></el-icon>
        </div>
        <div class="stat-card__body">
          <span class="stat-card__label">{{ card.label }}</span>
          <strong class="stat-card__value">{{ card.value }}</strong>
          <span class="stat-card__hint">{{ card.hint }}</span>
        </div>
      </article>
    </section>


    <section class="weather-showcase" :class="`weather-showcase--${weatherTheme}`">
      <div class="weather-showcase__copy">
        <span class="weather-kicker">LIVE WEATHER</span>

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

        <p class="weather-description">{{ weatherDescriptionText }}</p>

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
            <span class="weather-metric__label">更新时间</span>
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
            <el-option
              v-for="city in weatherCities"
              :key="city"
              :label="city"
              :value="city"
            />
          </el-select>

          <el-button
            class="weather-actions__button"
            type="primary"
            plain
            :icon="RefreshRight"
            :loading="weatherLoading"
            @click="loadWeather"
          >
            刷新天气
          </el-button>
        </div>

        <div class="weather-note" :class="{ 'weather-note--error': weatherError }">
          <el-icon><InfoFilled /></el-icon>
          <span>{{ weatherNote }}</span>
        </div>
      </div>

      <div class="weather-showcase__art">
        <div class="weather-scene" :class="`weather-scene--${weatherTheme}`">
          <div class="weather-scene__glow" />
          <div class="weather-scene__sun" />
          <div class="weather-scene__moon" />

          <div class="weather-scene__cloud weather-scene__cloud--main" />
          <div class="weather-scene__cloud weather-scene__cloud--secondary" />
          <div class="weather-scene__cloud weather-scene__cloud--tiny" />

          <div class="weather-scene__rain">
            <span
              v-for="drop in rainDrops"
              :key="`rain-${drop}`"
              :style="{
                '--offset': `${(drop - 1) * 24}px`,
                '--duration': `${1.2 + drop * 0.08}s`,
                '--delay': `${drop * 0.12}s`
              }"
            />
          </div>

          <div class="weather-scene__snow">
            <span
              v-for="flake in snowFlakes"
              :key="`snow-${flake}`"
              :style="{
                '--offset': `${(flake - 1) * 26}px`,
                '--duration': `${2.8 + flake * 0.12}s`,
                '--delay': `${flake * 0.18}s`
              }"
            />
          </div>

          <div class="weather-scene__wind">
            <span
              v-for="trail in windTrails"
              :key="`wind-${trail}`"
              :style="{
                '--delay': `${trail * 0.2}s`
              }"
            />
          </div>

          <div class="weather-scene__sparkles">
            <span v-for="spark in sparkles" :key="`spark-${spark}`" />
          </div>
        </div>
      </div>
    </section>


    <section class="content-grid">
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <div>
              <h3>快捷入口</h3>
              <p>快速进入常用模块。</p>
            </div>
          </div>
        </template>

        <div class="quick-grid">
          <button
            v-for="action in quickActions"
            :key="action.title"
            class="quick-card"
            type="button"
            @click="$router.push(action.to)"
          >
            <div class="quick-card__icon">
              <el-icon><component :is="action.icon" /></el-icon>
            </div>
            <div class="quick-card__copy">
              <span class="quick-card__title">{{ action.title }}</span>
              <span class="quick-card__desc">{{ action.desc }}</span>
            </div>
          </button>
        </div>
      </el-card>

      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <div>
              <h3>当前重点</h3>
              <p>近期需要关注的数据状态。</p>
            </div>
            <span class="summary-timestamp">更新于 {{ lastUpdatedLabel }}</span>
          </div>
        </template>

        <div class="focus-list">
          <div class="focus-item">
            <span class="focus-item__label">启用用户</span>
            <strong class="focus-item__value">{{ stats.activeUsers }}</strong>
            <span class="focus-item__desc">当前可正常使用系统的账号数量。</span>
          </div>
          <div class="focus-item">
            <span class="focus-item__label">启用字典</span>
            <strong class="focus-item__value">{{ stats.enabledDicts }}</strong>
            <span class="focus-item__desc">当前生效中的字典配置数量。</span>
          </div>
          <div class="focus-item">
            <span class="focus-item__label">停用对象</span>
            <strong class="focus-item__value">{{ inactiveSummary }}</strong>
            <span class="focus-item__desc">包含禁用用户和停用字典。</span>
          </div>
        </div>

        <div class="summary-note">
          <el-icon><InfoFilled /></el-icon>
          <span>{{ summaryNote }}</span>
        </div>
      </el-card>
    </section>

    <section class="detail-grid">
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <div>
              <h3>数据概览</h3>
              <p>展示当前主要管理对象的数量。</p>
            </div>
          </div>
        </template>

        <div class="status-metrics">
          <div class="status-metric">
            <span class="status-metric__label">用户总数</span>
            <strong class="status-metric__value">{{ stats.userTotal }}</strong>
          </div>
          <div class="status-metric">
            <span class="status-metric__label">禁用用户</span>
            <strong class="status-metric__value">{{ stats.disabledUsers }}</strong>
          </div>
          <div class="status-metric">
            <span class="status-metric__label">字典总数</span>
            <strong class="status-metric__value">{{ stats.dictTotal }}</strong>
          </div>
          <div class="status-metric">
            <span class="status-metric__label">停用字典</span>
            <strong class="status-metric__value">{{ stats.disabledDicts }}</strong>
          </div>
        </div>
      </el-card>

      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <div>
              <h3>模块摘要</h3>
              <p>当前主要模块的用途概览。</p>
            </div>
          </div>
        </template>

        <div class="tips-list">
          <div class="tip-item">
            <strong>用户管理</strong>
            <span>管理账号状态、角色信息和基础安全操作。</span>
          </div>
          <div class="tip-item">
            <strong>字典管理</strong>
            <span>维护系统基础配置及字典项内容。</span>
          </div>
          <div class="tip-item">
            <strong>AI 与热点</strong>
            <span>用于内容辅助、信息查看和模型能力管理。</span>
          </div>
        </div>
      </el-card>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  Collection,
  Compass,
  Cpu,
  InfoFilled,
  LocationFilled,
  Notification,
  RefreshRight,
  UserFilled
} from '@element-plus/icons-vue'
import { getDictList } from '@/api/dict'
import { getUserList } from '@/api/user'
import { getSupportedCities, getWeatherByCity } from '@/api/weather'

const loading = ref(false)
const weatherLoading = ref(false)
const weatherError = ref('')
const lastUpdatedAt = ref(null)

const weatherCity = ref('上海')
const weatherCities = ref(['上海'])
const weatherPayload = ref({
  city: '上海',
  temperature: 24,
  feelsLike: 25,
  description: '晴朗，适合开始今天的工作节奏。',
  main: 'Clear',
  humidity: 56,
  windSpeed: 3.2,
  queryTime: null
})

const rainDrops = [1, 2, 3, 4, 5, 6, 7]
const snowFlakes = [1, 2, 3, 4, 5, 6]
const windTrails = [1, 2, 3]
const sparkles = [1, 2, 3, 4]

const stats = ref({
  userTotal: 0,
  activeUsers: 0,
  disabledUsers: 0,
  dictTotal: 0,
  enabledDicts: 0,
  disabledDicts: 0
})

const quickActions = [
  { title: '用户管理', desc: '查看账号状态、角色和安全操作', to: '/user', icon: UserFilled },
  { title: '字典管理', desc: '维护系统基础配置和字典项', to: '/dict', icon: Collection },
  { title: 'AI 聊天', desc: '处理文本问答和辅助内容生成', to: '/ai/chat', icon: Cpu },
  { title: '热点新闻', desc: '快速查看多平台实时热点', to: '/news/hot', icon: Notification }
]

const statCards = computed(() => [
  {
    key: 'users',
    label: '用户总数',
    value: stats.value.userTotal,
    hint: `${stats.value.activeUsers} 位启用中`,
    tone: 'primary',
    icon: UserFilled
  },
  {
    key: 'dicts',
    label: '字典总数',
    value: stats.value.dictTotal,
    hint: `${stats.value.enabledDicts} 个启用中`,
    tone: 'success',
    icon: Collection
  },
  {
    key: 'disabledUsers',
    label: '禁用用户',
    value: stats.value.disabledUsers,
    hint: '当前处于禁用状态的账号',
    tone: 'warning',
    icon: Compass
  },
  {
    key: 'disabledDicts',
    label: '停用字典',
    value: stats.value.disabledDicts,
    hint: '当前未启用的字典配置',
    tone: 'neutral',
    icon: InfoFilled
  }
])

const inactiveSummary = computed(() => stats.value.disabledUsers + stats.value.disabledDicts)

const lastUpdatedLabel = computed(() => {
  if (!lastUpdatedAt.value) return '刚刚'

  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(lastUpdatedAt.value)
})

const summaryNote = computed(() => {
  if (inactiveSummary.value > 0) {
    return `当前共有 ${inactiveSummary.value} 个停用对象，建议继续排查对应配置。`
  }

  return '当前系统状态稳定，核心对象均处于可用区间。'
})

const weatherStatusText = computed(() => {
  return [weatherPayload.value.main, weatherPayload.value.description]
    .filter(Boolean)
    .join(' ')
    .toLowerCase()
})

const weatherTheme = computed(() => {
  const text = weatherStatusText.value

  if (text.includes('snow') || text.includes('雪')) return 'snowy'
  if (text.includes('rain') || text.includes('雨') || text.includes('shower')) return 'rainy'
  if (text.includes('wind') || text.includes('风')) return 'windy'
  if (text.includes('cloud') || text.includes('阴') || text.includes('多云')) return 'cloudy'
  return 'sunny'
})

const weatherHeadline = computed(() => {
  const themeMap = {
    sunny: '晴光正好，适合把重要事情推进一点。',
    cloudy: '云层柔和，今天适合稳稳当当地处理任务。',
    rainy: '有雨意，页面上给它加一点动态水滴氛围。',
    windy: '风感明显，适合用流线图案表现空气流动。',
    snowy: '雪意偏轻，用更安静的颗粒感来呈现天气。'
  }

  return themeMap[weatherTheme.value]
})

const weatherTemperature = computed(() => {
  const temperature = weatherPayload.value.temperature
  return Number.isFinite(temperature) ? `${Math.round(temperature)}°` : '--'
})

const weatherFeelsLike = computed(() => {
  const feelsLike = weatherPayload.value.feelsLike
  return Number.isFinite(feelsLike) ? `体感 ${Math.round(feelsLike)}°` : '体感暂未返回'
})

const weatherDescriptionText = computed(() => {
  return weatherPayload.value.description || '天气接口已接通，等待实时数据返回。'
})

const weatherHumidity = computed(() => {
  const humidity = weatherPayload.value.humidity
  return Number.isFinite(humidity) ? `${humidity}%` : '--'
})

const weatherWind = computed(() => {
  const windSpeed = weatherPayload.value.windSpeed
  return Number.isFinite(windSpeed) ? `${windSpeed.toFixed(1)} m/s` : '--'
})

const weatherUpdatedAt = computed(() => {
  const parsed = parseDateLike(weatherPayload.value.queryTime)
  if (!parsed) return '刚刚'

  return new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  }).format(parsed)
})

const weatherNote = computed(() => {
  if (weatherError.value) {
    return weatherError.value
  }

  return '图案会根据天气关键词自动切换为晴天、多云、下雨、刮风或下雪场景。'
})

const extractList = (payload) => {
  if (Array.isArray(payload?.records)) return payload.records
  if (Array.isArray(payload?.list)) return payload.list
  if (Array.isArray(payload?.items)) return payload.items
  if (Array.isArray(payload)) return payload
  return []
}

const extractTotal = (payload, fallback = 0) => {
  return typeof payload?.total === 'number' ? payload.total : fallback
}

const parseDateLike = (value) => {
  if (!value) return null
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? null : date
}

const loadDashboard = async () => {
  loading.value = true

  try {
    const [userResult, dictResult] = await Promise.allSettled([
      getUserList({ page: 1, size: 200 }),
      getDictList({ page: 0, size: 200 })
    ])

    const userPayload = userResult.status === 'fulfilled' ? userResult.value.data : null
    const dictPayload = dictResult.status === 'fulfilled' ? dictResult.value.data : null

    const users = extractList(userPayload)
    const dicts = extractList(dictPayload)

    stats.value = {
      userTotal: extractTotal(userPayload, users.length),
      activeUsers: users.filter((item) => item.status === 1).length,
      disabledUsers: users.filter((item) => item.status !== 1).length,
      dictTotal: extractTotal(dictPayload, dicts.length),
      enabledDicts: dicts.filter((item) => item.status === 1).length,
      disabledDicts: dicts.filter((item) => item.status !== 1).length
    }
  } finally {
    lastUpdatedAt.value = new Date()
    loading.value = false
  }
}

const loadWeatherCities = async () => {
  try {
    const res = await getSupportedCities()
    const cities = Array.isArray(res.data) && res.data.length > 0 ? res.data : ['上海']
    weatherCities.value = cities

    if (!cities.includes(weatherCity.value)) {
      weatherCity.value = cities[0]
    }
  } catch (error) {
    weatherCities.value = ['上海', '北京', '广州', '深圳', '杭州']
  }
}

const loadWeather = async () => {
  weatherLoading.value = true
  weatherError.value = ''

  try {
    const res = await getWeatherByCity(weatherCity.value)
    if (res?.data) {
      weatherPayload.value = {
        ...weatherPayload.value,
        ...res.data
      }
    }
  } catch (error) {
    weatherError.value = '暂时没有拿到实时天气，先展示默认场景图案。'
  } finally {
    weatherLoading.value = false
  }
}

onMounted(async () => {
  await Promise.allSettled([loadDashboard(), loadWeatherCities()])
  await loadWeather()
})
</script>

<style lang="scss" scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.overview-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 26px 28px;
  border-radius: 24px;
  background:
    linear-gradient(135deg, rgba(15, 23, 42, 0.96), rgba(30, 41, 59, 0.92)),
    linear-gradient(135deg, rgba(37, 99, 235, 0.18), rgba(16, 185, 129, 0.14));
  color: #fff;
}

.overview-panel__copy {
  max-width: 720px;
}

.section-kicker {
  display: inline-block;
  color: rgba(255, 255, 255, 0.72);
  font-size: 0.76rem;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.section-title {
  margin: 8px 0 0;
  font-size: clamp(1.6rem, 2.8vw, 2.3rem);
  line-height: 1.15;
}

.section-desc {
  margin: 10px 0 0;
  color: rgba(255, 255, 255, 0.76);
  line-height: 1.7;
}

.overview-panel__actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.overview-panel__actions :deep(.el-button:not(.el-button--primary)) {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.14) !important;
}

.stat-grid,
.content-grid,
.detail-grid {
  display: grid;
  gap: 20px;
}

.stat-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.content-grid,
.detail-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  min-height: 128px;
  padding: 20px;
  border: 1px solid rgba(148, 163, 184, 0.16);
  border-radius: 20px;
  background: #fff;
}

.stat-card__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 16px;
  flex-shrink: 0;
}

.stat-card__body {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-card__label {
  color: #64748b;
  font-size: 0.9rem;
}

.stat-card__value {
  color: #0f172a;
  font-size: 1.8rem;
  line-height: 1;
}

.stat-card__hint {
  color: #94a3b8;
  font-size: 0.82rem;
  line-height: 1.5;
}

.stat-card--primary .stat-card__icon {
  background: rgba(37, 99, 235, 0.12);
  color: #2563eb;
}

.stat-card--success .stat-card__icon {
  background: rgba(16, 185, 129, 0.12);
  color: #059669;
}

.stat-card--warning .stat-card__icon {
  background: rgba(245, 158, 11, 0.12);
  color: #d97706;
}

.stat-card--neutral .stat-card__icon {
  background: rgba(71, 85, 105, 0.12);
  color: #475569;
}


.weather-showcase {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(280px, 0.8fr);
  gap: 22px;
  overflow: hidden;
  padding: 28px;
  border-radius: 30px;
  border: 1px solid rgba(148, 163, 184, 0.14);
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.08);
}

.weather-showcase::before,
.weather-showcase::after {
  content: '';
  position: absolute;
  inset: auto;
  border-radius: 999px;
  pointer-events: none;
}

.weather-showcase::before {
  top: -72px;
  right: -18px;
  width: 220px;
  height: 220px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.5), transparent 68%);
}

.weather-showcase::after {
  left: -120px;
  bottom: -150px;
  width: 280px;
  height: 280px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.18), transparent 70%);
}

.weather-showcase--sunny {
  background:
    radial-gradient(circle at 82% 18%, rgba(255, 213, 79, 0.34), transparent 18%),
    linear-gradient(135deg, #11315f, #2166b5 48%, #61b0ff);
}

.weather-showcase--cloudy {
  background:
    radial-gradient(circle at 78% 16%, rgba(255, 255, 255, 0.22), transparent 18%),
    linear-gradient(135deg, #1f304f, #58708f 46%, #93a7bf);
}

.weather-showcase--rainy {
  background:
    radial-gradient(circle at 84% 16%, rgba(146, 197, 255, 0.18), transparent 18%),
    linear-gradient(135deg, #101c33, #26496c 44%, #3f7a9f);
}

.weather-showcase--windy {
  background:
    radial-gradient(circle at 84% 20%, rgba(255, 255, 255, 0.18), transparent 18%),
    linear-gradient(135deg, #10233f, #225a81 44%, #67b4d6);
}

.weather-showcase--snowy {
  background:
    radial-gradient(circle at 82% 16%, rgba(255, 255, 255, 0.42), transparent 18%),
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

.weather-kicker {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 0.76rem;
  font-weight: 800;
  letter-spacing: 0.18em;
}

.weather-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-top: 14px;
}

.weather-title {
  margin: 0;
  font-size: clamp(2rem, 3.4vw, 3.2rem);
  line-height: 0.96;
}

.weather-subtitle {
  margin: 10px 0 0;
  max-width: 560px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 0.98rem;
  line-height: 1.7;
}

.weather-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  font-size: 0.9rem;
  font-weight: 600;
  white-space: nowrap;
}

.weather-temperature {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  margin-top: 20px;
}

.weather-temperature strong {
  font-size: clamp(3rem, 7vw, 4.8rem);
  line-height: 0.95;
}

.weather-temperature span {
  padding-bottom: 8px;
  color: rgba(255, 255, 255, 0.78);
  font-size: 1rem;
}

.weather-description {
  margin: 16px 0 0;
  max-width: 580px;
  color: rgba(255, 255, 255, 0.84);
  line-height: 1.8;
}

.weather-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 22px;
}

.weather-metric {
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(14px);
}

.weather-metric__label {
  display: block;
  color: rgba(255, 255, 255, 0.64);
  font-size: 0.76rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.weather-metric__value {
  display: block;
  margin-top: 8px;
  color: #fff;
  font-size: 1.05rem;
  font-weight: 800;
}

.weather-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 20px;
}

.weather-actions__select {
  width: 168px;
}

.weather-actions__select :deep(.el-input__wrapper) {
  border-radius: 16px;
  box-shadow: none !important;
  background: rgba(255, 255, 255, 0.16);
}

.weather-actions__select :deep(.el-input__inner) {
  color: #fff;
}

.weather-actions__select :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.5);
}

.weather-actions__button {
  height: 40px;
  border-radius: 16px;
  border-color: rgba(255, 255, 255, 0.22);
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.weather-actions__button:hover {
  border-color: rgba(255, 255, 255, 0.42);
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
}

.weather-note {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-top: 18px;
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.12);
  color: rgba(255, 255, 255, 0.82);
  line-height: 1.6;
}

.weather-note--error {
  background: rgba(248, 113, 113, 0.14);
  color: #ffe5e5;
}

.weather-showcase__art {
  display: flex;
  align-items: center;
  justify-content: center;
}

.weather-scene {
  position: relative;
  width: min(100%, 360px);
  aspect-ratio: 1 / 1;
  border-radius: 34px;
  overflow: hidden;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.18), rgba(255, 255, 255, 0.04));
  border: 1px solid rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(18px);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.18);
}

.weather-scene__glow,
.weather-scene__sun,
.weather-scene__moon,
.weather-scene__cloud,
.weather-scene__rain,
.weather-scene__snow,
.weather-scene__wind,
.weather-scene__sparkles {
  position: absolute;
}

.weather-scene__glow {
  inset: auto;
  top: 18%;
  left: 50%;
  width: 210px;
  height: 210px;
  border-radius: 999px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.28), transparent 68%);
  transform: translateX(-50%);
}

.weather-scene__sun,
.weather-scene__moon {
  top: 18%;
  right: 18%;
  width: 84px;
  height: 84px;
  border-radius: 50%;
}

.weather-scene__sun {
  background: radial-gradient(circle at 35% 35%, #fff7c8, #ffd157 58%, #ff9f2f);
  box-shadow:
    0 0 0 16px rgba(255, 209, 87, 0.14),
    0 0 42px rgba(255, 209, 87, 0.28);
}

.weather-scene__moon {
  background: radial-gradient(circle at 30% 30%, #f8fbff, #d7e4f7 58%, #a7c0df);
  box-shadow:
    -18px 0 0 0 rgba(30, 58, 96, 0.9),
    0 0 32px rgba(215, 228, 247, 0.18);
  opacity: 0;
}

.weather-scene__cloud {
  left: 50%;
  width: 132px;
  height: 48px;
  border-radius: 999px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(228, 237, 247, 0.96));
  box-shadow: 0 18px 30px rgba(15, 23, 42, 0.1);
  transform: translateX(-50%);
}

.weather-scene__cloud::before,
.weather-scene__cloud::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  background: inherit;
}

.weather-scene__cloud::before {
  left: 18px;
  bottom: 18px;
  width: 48px;
  height: 48px;
}

.weather-scene__cloud::after {
  right: 20px;
  bottom: 12px;
  width: 56px;
  height: 56px;
}

.weather-scene__cloud--main {
  top: 34%;
}

.weather-scene__cloud--secondary {
  top: 48%;
  left: 38%;
  width: 112px;
  transform: translateX(-50%) scale(0.86);
  opacity: 0.92;
}

.weather-scene__cloud--tiny {
  top: 24%;
  left: 26%;
  width: 88px;
  transform: translateX(-50%) scale(0.68);
  opacity: 0.72;
}

.weather-scene__rain,
.weather-scene__snow {
  left: 50%;
  top: 52%;
  width: 170px;
  height: 150px;
  transform: translateX(-50%);
  opacity: 0;
}

.weather-scene__rain span,
.weather-scene__snow span {
  position: absolute;
  left: calc(20px + var(--offset));
}

.weather-scene__rain span {
  top: 0;
  width: 3px;
  height: 36px;
  border-radius: 999px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.12), rgba(123, 201, 255, 0.95));
  animation: rain-fall var(--duration) linear infinite;
  animation-delay: var(--delay);
}

.weather-scene__snow span {
  top: 0;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 0 18px rgba(255, 255, 255, 0.32);
  animation: snow-fall var(--duration) linear infinite;
  animation-delay: var(--delay);
}

.weather-scene__wind {
  left: 50%;
  top: 50%;
  width: 220px;
  height: 120px;
  transform: translateX(-50%);
  opacity: 0;
}

.weather-scene__wind span {
  position: absolute;
  left: 0;
  width: 140px;
  height: 14px;
  border-top: 2px solid rgba(255, 255, 255, 0.72);
  border-radius: 999px;
  animation: wind-move 2.2s ease-in-out infinite;
  animation-delay: var(--delay);
}

.weather-scene__wind span:nth-child(1) {
  top: 8px;
}

.weather-scene__wind span:nth-child(2) {
  top: 42px;
  width: 180px;
}

.weather-scene__wind span:nth-child(3) {
  top: 78px;
  width: 120px;
}

.weather-scene__sparkles {
  inset: 0;
}

.weather-scene__sparkles span {
  position: absolute;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.8);
  animation: sparkle 2.8s ease-in-out infinite;
}

.weather-scene__sparkles span:nth-child(1) {
  top: 16%;
  left: 18%;
}

.weather-scene__sparkles span:nth-child(2) {
  top: 26%;
  left: 74%;
  animation-delay: 0.6s;
}

.weather-scene__sparkles span:nth-child(3) {
  top: 38%;
  left: 12%;
  animation-delay: 1.2s;
}

.weather-scene__sparkles span:nth-child(4) {
  top: 62%;
  left: 76%;
  animation-delay: 1.8s;
}

.weather-scene--cloudy .weather-scene__sun,
.weather-scene--rainy .weather-scene__sun,
.weather-scene--windy .weather-scene__sun,
.weather-scene--snowy .weather-scene__sun {
  opacity: 0.35;
  transform: scale(0.88);
}

.weather-scene--cloudy .weather-scene__cloud,
.weather-scene--rainy .weather-scene__cloud,
.weather-scene--snowy .weather-scene__cloud {
  background: linear-gradient(180deg, rgba(243, 247, 252, 0.98), rgba(205, 218, 233, 0.95));
}

.weather-scene--rainy .weather-scene__rain {
  opacity: 1;
}

.weather-scene--windy .weather-scene__wind {
  opacity: 1;
}

.weather-scene--snowy .weather-scene__snow {
  opacity: 1;
}

.weather-scene--snowy .weather-scene__sun {
  opacity: 0;
}

.weather-scene--snowy .weather-scene__moon {
  opacity: 1;
}


.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.card-header h3 {
  margin: 0;
  color: #0f172a;
  font-size: 1.05rem;
  font-weight: 800;
}

.card-header p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 0.88rem;
}

.summary-timestamp {
  color: #94a3b8;
  font-size: 0.8rem;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.quick-card {
  display: flex;
  align-items: center;
  gap: 14px;
  width: 100%;
  padding: 16px;
  border: 1px solid rgba(148, 163, 184, 0.16);
  border-radius: 18px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.quick-card:hover {
  border-color: rgba(37, 99, 235, 0.22);
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.06);
  transform: translateY(-2px);
}

.quick-card__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 14px;
  background: rgba(37, 99, 235, 0.1);
  color: #2563eb;
  flex-shrink: 0;
}

.quick-card__copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.quick-card__title {
  color: #0f172a;
  font-weight: 700;
}

.quick-card__desc {
  color: #64748b;
  font-size: 0.82rem;
  line-height: 1.5;
}

.focus-list,
.status-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.status-metrics {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.focus-item,
.status-metric,
.tip-item {
  padding: 18px;
  border-radius: 18px;
  background: #f8fafc;
}

.focus-item__label,
.status-metric__label {
  color: #64748b;
  font-size: 0.84rem;
}

.focus-item__value,
.status-metric__value {
  display: block;
  margin-top: 10px;
  color: #0f172a;
  font-size: 1.6rem;
  line-height: 1;
}

.focus-item__desc {
  display: block;
  margin-top: 10px;
  color: #94a3b8;
  font-size: 0.82rem;
  line-height: 1.5;
}

.summary-note {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-top: 18px;
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(37, 99, 235, 0.08);
  color: #1e40af;
  line-height: 1.6;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.tip-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tip-item strong {
  color: #0f172a;
  font-size: 0.95rem;
}

.tip-item span {
  color: #64748b;
  font-size: 0.88rem;
  line-height: 1.6;
}


@keyframes rain-fall {
  0% {
    transform: translateY(-16px);
    opacity: 0;
  }

  15% {
    opacity: 1;
  }

  100% {
    transform: translateY(130px);
    opacity: 0;
  }
}

@keyframes snow-fall {
  0% {
    transform: translate3d(0, -10px, 0);
    opacity: 0;
  }

  12% {
    opacity: 1;
  }

  100% {
    transform: translate3d(18px, 132px, 0);
    opacity: 0;
  }
}

@keyframes wind-move {
  0%,
  100% {
    transform: translateX(0);
    opacity: 0.32;
  }

  50% {
    transform: translateX(18px);
    opacity: 1;
  }
}

@keyframes sparkle {
  0%,
  100% {
    transform: scale(0.6);
    opacity: 0.35;
  }

  50% {
    transform: scale(1.2);
    opacity: 1;
  }
}


@media (max-width: 1200px) {
  .stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }


  .weather-showcase {
    grid-template-columns: 1fr;
  }

}

@media (max-width: 960px) {
  .content-grid,
  .detail-grid,
  .quick-grid,
  .focus-list,
  .status-metrics,
  .weather-metrics {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .overview-panel,
  .weather-heading {
    flex-direction: column;
    align-items: flex-start;
  }

  .stat-grid {
    grid-template-columns: 1fr;
  }


  .weather-showcase {
    padding: 20px;
  }

  .weather-scene {
    width: 100%;
    max-width: 320px;
  }

}
</style>
