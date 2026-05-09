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


@media (max-width: 1200px) {
  .stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
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

}
</style>
