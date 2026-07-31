<template>
  <div class="hot-news-page tone-news">
    <PageHero
      title="热点新闻"
      subtitle="聚合 5 大主流平台热榜，实时抓取全网风向。切换平台即刻查看不同来源的热点排行。"
      eyebrow="REALTIME HOTBOARD"
      tone="news"
      :icon="Bell"
    >
      <template #actions>
        <el-button
          type="primary"
          class="refresh-button"
          :loading="loading"
          :icon="Refresh"
          @click="fetchNews"
        >
          刷新热榜
        </el-button>
      </template>
    </PageHero>

    <section class="stat-strip">
      <StatCard
        label="覆盖平台"
        :value="platforms.length"
        :icon="Connection"
        tone="news"
        hint="澎湃 · 头条 · 腾讯 · 新浪 · 网易"
        :delay="0"
      />
      <StatCard
        label="当前热点"
        :value="newsList.length"
        :icon="TrendCharts"
        tone="news"
        :hint="`来自 ${currentPlatformName}`"
        :delay="80"
      >
        <template #chart>
          <Sparkline :data="fakeTrend" color="var(--accent-news)" />
        </template>
      </StatCard>
      <StatCard
        label="上次更新"
        :value="formattedUpdateTime === '--' ? 0 : refreshCount"
        :animate="false"
        :icon="Refresh"
        tone="news"
        :hint="formattedUpdateTime"
        :delay="160"
      />
    </section>

    <section class="platform-switcher">
      <button
        v-for="(p, i) in platforms"
        :key="p.id"
        :class="['platform-chip', { active: currentPlatform === p.id }]"
        type="button"
        v-motion
        :initial="{ opacity: 0, y: 10 }"
        :visibleOnce="{ opacity: 1, y: 0, transition: { delay: i * 60, duration: 260 } }"
        @click="handlePlatformChange(p.id)"
      >
        <span class="chip-badge">{{ p.short }}</span>
        <div class="chip-copy">
          <span class="chip-name">{{ p.name }}</span>
          <span class="chip-code">{{ p.id }}</span>
        </div>
      </button>
    </section>

    <section class="board-panel">
      <div class="board-header">
        <div>
          <h2 class="board-title">{{ currentPlatformName }}热榜</h2>
          <p class="board-desc">点击榜单标题可直达原始新闻页面</p>
        </div>
        <span v-if="newsList.length" class="board-count">共 {{ newsList.length }} 条热点</span>
      </div>

      <div v-if="loading" class="loading-state">
        <div v-for="i in 6" :key="i" class="skeleton-row">
          <div class="skeleton-rank" />
          <div class="skeleton-main">
            <el-skeleton-item variant="text" style="width: 72%" />
            <el-skeleton-item variant="text" style="width: 36%; height: 12px" />
          </div>
          <div class="skeleton-hot" />
        </div>
      </div>

      <div v-else-if="newsList.length === 0" class="empty-state">
        <EmptyIllustration
          variant="news"
          title="暂无热点数据"
          description="换个平台试试，或稍后再来刷新"
        />
      </div>

      <ul v-else class="news-list">
        <li
          v-for="(item, i) in newsList"
          :key="item.rank"
          class="news-row"
          v-motion
          :initial="{ opacity: 0, x: -8 }"
          :visibleOnce="{ opacity: 1, x: 0, transition: { delay: Math.min(i * 30, 600), duration: 220 } }"
        >
          <a
            :href="item.url"
            target="_blank"
            rel="noopener noreferrer"
            :class="['news-link', { 'without-heat': !item.hotValue }]"
          >
            <div :class="['rank-badge', rankClass(item.rank)]">
              {{ item.rank }}
            </div>

            <div class="news-main">
              <p class="news-title">{{ item.title }}</p>
              <div class="news-meta">
                <span>{{ item.source || currentPlatformName }}</span>
              </div>
            </div>

            <div v-if="item.hotValue" class="news-heat">
              <span class="heat-value">🔥 {{ formatHot(item.hotValue) }}</span>
            </div>

            <el-icon class="news-arrow"><ArrowRight /></el-icon>
          </a>
        </li>
      </ul>

      <div v-if="newsList.length" class="board-footer">
        <el-icon><Location /></el-icon>
        <span>点击榜单标题可直接跳转到原文页面，查看更多详细内容</span>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  ArrowRight,
  Bell,
  Connection,
  Location,
  Refresh,
  TrendCharts
} from '@element-plus/icons-vue'
import { getHotNews } from '@/api/news'
import PageHero from '@/components/PageHero.vue'
import StatCard from '@/components/StatCard.vue'
import Sparkline from '@/components/Sparkline.vue'
import EmptyIllustration from '@/components/EmptyIllustration.vue'

const platforms = [
  { id: 'thepaper', name: '澎湃新闻', short: '澎湃' },
  { id: 'toutiao', name: '今日头条', short: '头条' },
  { id: 'tencent-news', name: '腾讯新闻', short: '腾讯' },
  { id: 'sina-news', name: '新浪新闻', short: '新浪' },
  { id: 'netease-news', name: '网易新闻', short: '网易' }
]

const currentPlatform = ref('thepaper')
const loading = ref(false)
const newsList = ref([])
const updateTime = ref('')
const refreshCount = ref(0)
const fakeTrend = ref([12, 18, 15, 22, 26, 24, 30])

const currentPlatformName = computed(() => {
  const p = platforms.find(item => item.id === currentPlatform.value)
  return p ? p.name : ''
})

const formattedUpdateTime = computed(() => {
  if (!updateTime.value) return '--'
  try {
    const d = new Date(updateTime.value)
    if (isNaN(d.getTime())) return updateTime.value
    const pad = n => String(n).padStart(2, '0')
    return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  } catch {
    return updateTime.value
  }
})

const rankClass = (rank) => {
  if (rank === 1) return 'top-one'
  if (rank === 2) return 'top-two'
  if (rank === 3) return 'top-three'
  return 'top-normal'
}

const formatHot = (val) => {
  if (!val) return ''
  const num = parseFloat(String(val).replace(/[^\d.]/g, ''))
  if (isNaN(num)) return val
  if (num >= 10000) return `${(num / 10000).toFixed(1)}w`
  return val
}

const fetchNews = async () => {
  loading.value = true
  try {
    const res = await getHotNews({ platform: currentPlatform.value })
    const data = res.data || res
    newsList.value = data.items || []
    updateTime.value = data.updateTime || ''
    refreshCount.value += 1
  } catch (e) {
    console.error('加载热点新闻失败', e)
    newsList.value = []
  } finally {
    loading.value = false
  }
}

const handlePlatformChange = (id) => {
  currentPlatform.value = id
  fetchNews()
}

onMounted(() => {
  fetchNews()
})
</script>

<style lang="scss" scoped>
.hot-news-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.refresh-button {
  height: 42px;
  border-radius: 14px;
  font-weight: 700;
  background: var(--accent-gradient) !important;
  border: none !important;
  box-shadow: 0 12px 24px var(--accent-soft) !important;
}

.stat-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.platform-switcher {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.platform-chip {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 150px;
  flex: 1;
  padding: 14px 16px;
  border: 2px solid var(--border-subtle);
  border-radius: 18px;
  background: var(--surface-base);
  color: var(--text-secondary);
  cursor: pointer;
  transition: transform 220ms ease, border-color 220ms ease, box-shadow 220ms ease;
}

.platform-chip:hover {
  border-color: var(--accent-border);
  box-shadow: 0 8px 24px var(--accent-soft);
  transform: translateY(-3px);
}

.platform-chip.active {
  border-color: var(--accent);
  background: linear-gradient(135deg, var(--accent-soft), transparent 60%), var(--surface-base);
  box-shadow: 0 12px 28px var(--accent-soft);
}

.chip-badge {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: var(--accent-soft);
  color: var(--accent-strong);
  font-size: 0.74rem;
  font-weight: 800;
  flex-shrink: 0;
  transition: all 220ms ease;
}

.platform-chip.active .chip-badge {
  background: var(--accent-gradient);
  color: #fff;
  box-shadow: 0 8px 16px var(--accent-soft);
}

.chip-copy {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.chip-name {
  font-size: 0.92rem;
  font-weight: 700;
  color: var(--text-primary);
}

.chip-code {
  font-size: 0.72rem;
  color: var(--text-disabled);
  letter-spacing: 0.04em;
}

.board-panel {
  border: 1px solid var(--border-subtle);
  border-radius: 24px;
  overflow: hidden;
  background: var(--surface-base);
  box-shadow: var(--shadow-sm);
}

.board-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-soft);
}

.board-title {
  margin: 0;
  color: var(--text-primary);
  font-size: 1.1rem;
  font-weight: 800;
}

.board-desc {
  margin: 4px 0 0;
  color: var(--text-muted);
  font-size: 0.84rem;
}

.board-count {
  display: inline-flex;
  padding: 5px 12px;
  border-radius: 999px;
  background: var(--accent-soft);
  color: var(--accent-strong);
  font-size: 0.8rem;
  font-weight: 700;
  white-space: nowrap;
}

.loading-state,
.empty-state {
  padding: 20px 24px 28px;
}

.skeleton-row {
  display: grid;
  grid-template-columns: 50px minmax(0, 1fr) 80px;
  gap: 16px;
  align-items: center;
  padding: 14px 0;
}

.skeleton-rank,
.skeleton-hot {
  height: 42px;
  border-radius: 14px;
  background: var(--surface-muted);
}

.skeleton-main {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.news-list {
  margin: 0;
  padding: 8px 12px 16px;
  list-style: none;
}

.news-link {
  display: grid;
  grid-template-columns: 50px minmax(0, 1fr) auto 24px;
  gap: 14px;
  align-items: center;
  padding: 14px 12px;
  border-radius: 16px;
  color: inherit;
  text-decoration: none;
  transition: background-color 220ms ease, transform 220ms ease;
}

.news-link.without-heat {
  grid-template-columns: 50px minmax(0, 1fr) 24px;
}

.news-link:hover {
  background: var(--accent-soft);
  transform: translateX(4px);
}

.rank-badge {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border-radius: 14px;
  font-size: 0.95rem;
  font-weight: 800;
}

.top-one {
  background: linear-gradient(135deg, #f97316, #fb923c);
  color: #fff;
  box-shadow: 0 8px 16px rgba(249, 115, 22, 0.32);
}

.top-two {
  background: linear-gradient(135deg, #64748b, #94a3b8);
  color: #fff;
}

.top-three {
  background: linear-gradient(135deg, #d97706, #fbbf24);
  color: #fff;
  box-shadow: 0 8px 16px rgba(245, 158, 11, 0.28);
}

.top-normal {
  background: var(--surface-muted);
  color: var(--text-secondary);
}

.news-main { min-width: 0; }

.news-title {
  margin: 0;
  color: var(--text-primary);
  font-size: 0.98rem;
  font-weight: 600;
  line-height: 1.55;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.news-link:hover .news-title {
  color: var(--accent-strong);
}

.news-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  color: var(--text-muted);
  font-size: 0.78rem;
}

.heat-value {
  color: #f97316;
  font-size: 0.88rem;
  font-weight: 700;
  white-space: nowrap;
}

.news-arrow {
  color: var(--text-disabled);
  font-size: 14px;
}

.news-link:hover .news-arrow {
  color: var(--accent);
}

.board-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 24px;
  border-top: 1px solid var(--border-soft);
  color: var(--text-muted);
  font-size: 0.82rem;
}

@media (max-width: 1100px) {
  .stat-strip { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

@media (max-width: 768px) {
  .stat-strip { grid-template-columns: 1fr; }
  .platform-chip { min-width: calc(50% - 6px); flex: unset; }
  .news-link { grid-template-columns: 40px minmax(0, 1fr) 24px; gap: 12px; }
  .news-heat { grid-column: 2; margin-top: -2px; }
  .rank-badge { width: 36px; height: 36px; border-radius: 12px; font-size: 0.88rem; }
}
</style>
