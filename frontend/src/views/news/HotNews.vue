<template>
  <div class="hot-news-page">
    <section class="hero-panel">
      <div class="hero-copy">
        <p class="eyebrow">Realtime Hotboard</p>
        <h2 class="page-title">{{ copy.title }}</h2>
        <p class="page-subtitle">{{ copy.subtitle }}</p>
      </div>
      <div class="hero-actions">
        <div class="platform-meta">
          <span class="meta-label">{{ copy.currentPlatform }}</span>
          <span class="meta-value">{{ currentPlatformMeta.label }}</span>
        </div>
        <div class="platform-meta">
          <span class="meta-label">{{ copy.lastUpdated }}</span>
          <span class="meta-value">{{ formattedUpdateTime }}</span>
        </div>
        <el-button
          type="primary"
          :icon="Refresh"
          :loading="loading"
          class="refresh-button"
          @click="loadNews"
        >
          {{ copy.refresh }}
        </el-button>
      </div>
    </section>

    <section class="platform-switcher">
      <button
        v-for="platform in platforms"
        :key="platform.value"
        class="platform-chip"
        :class="{ active: currentPlatform === platform.value }"
        @click="switchPlatform(platform.value)"
      >
        <span class="chip-name">{{ platform.label }}</span>
        <span class="chip-code">{{ platform.value }}</span>
      </button>
    </section>

    <section class="board-panel">
      <div class="board-header">
        <div>
          <p class="board-title">{{ `${currentPlatformMeta.label}${copy.boardSuffix}` }}</p>
          <p class="board-desc">{{ copy.boardDesc }}</p>
        </div>
        <div class="board-count">{{ `${newsItems.length} ${copy.boardCount}` }}</div>
      </div>

      <div v-if="loading" class="loading-state">
        <el-skeleton v-for="index in 8" :key="index" animated>
          <template #template>
            <div class="skeleton-row">
              <div class="skeleton-rank" />
              <div class="skeleton-main">
                <el-skeleton-item variant="text" style="width: 72%" />
                <el-skeleton-item variant="text" style="width: 38%" />
              </div>
              <div class="skeleton-hot" />
            </div>
          </template>
        </el-skeleton>
      </div>

      <div v-else-if="newsItems.length === 0" class="empty-state">
        <el-empty :description="copy.empty" />
      </div>

      <ol v-else class="news-list">
        <li v-for="item in newsItems" :key="`${currentPlatform}-${item.rank}-${item.title}`" class="news-row">
          <a
            :href="item.url"
            target="_blank"
            rel="noopener noreferrer"
            class="news-link"
            :class="{ 'without-heat': !item.hotValue }"
          >
            <div class="rank-badge" :class="getRankClass(item.rank)">
              {{ item.rank || '--' }}
            </div>
            <div class="news-main">
              <h3 class="news-title">{{ item.title }}</h3>
              <div class="news-meta">
                <span class="meta-source">{{ item.source || currentPlatformMeta.label }}</span>
                <span class="meta-divider">/</span>
                <span class="meta-platform">{{ currentPlatformMeta.label }}</span>
              </div>
            </div>
            <div v-if="item.hotValue" class="news-heat">
              <span class="heat-label">{{ copy.hotValue }}</span>
              <span class="heat-value">{{ item.hotValue }}</span>
            </div>
          </a>
        </li>
      </ol>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getHotNews } from '@/api/news'

const copy = {
  title: '\u70ed\u70b9\u65b0\u95fb',
  subtitle: '\u805a\u5408 5 \u4e2a\u4e3b\u6d41\u8d44\u8baf\u5e73\u53f0\u70ed\u699c\uff0c\u6309\u5e73\u53f0\u5feb\u901f\u5207\u6362\u67e5\u770b\u6700\u65b0\u70ed\u70b9\u3002',
  currentPlatform: '\u5f53\u524d\u5e73\u53f0',
  lastUpdated: '\u6700\u540e\u66f4\u65b0',
  refresh: '\u5237\u65b0\u70ed\u699c',
  boardSuffix: '\u70ed\u699c',
  boardDesc: '\u70b9\u51fb\u699c\u5355\u6807\u9898\u53ef\u76f4\u8fbe\u539f\u59cb\u65b0\u95fb\u9875\u9762',
  boardCount: '\u6761\u70ed\u70b9',
  empty: '\u5f53\u524d\u5e73\u53f0\u6682\u65e0\u70ed\u70b9\u6570\u636e',
  hotValue: '\u70ed\u5ea6',
  notUpdated: '\u6682\u672a\u66f4\u65b0',
  loadFailed: '\u83b7\u53d6\u70ed\u70b9\u65b0\u95fb\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5'
}

const platforms = [
  { label: '\u6f8e\u6e43\u65b0\u95fb', value: 'thepaper' },
  { label: '\u4eca\u65e5\u5934\u6761', value: 'toutiao' },
  { label: '\u817e\u8baf\u65b0\u95fb', value: 'tencent-news' },
  { label: '\u65b0\u6d6a\u65b0\u95fb', value: 'sina-news' },
  { label: '\u7f51\u6613\u65b0\u95fb', value: 'netease-news' }
]

const loading = ref(false)
const currentPlatform = ref('thepaper')
const newsItems = ref([])
const updateTime = ref('')

const currentPlatformMeta = computed(() => {
  return platforms.find(platform => platform.value === currentPlatform.value) || platforms[0]
})

const formattedUpdateTime = computed(() => {
  if (!updateTime.value) {
    return copy.notUpdated
  }

  const parsedDate = parseUpdateTime(updateTime.value)
  if (!parsedDate) {
    return updateTime.value
  }

  const year = parsedDate.getFullYear()
  const month = `${parsedDate.getMonth() + 1}`.padStart(2, '0')
  const day = `${parsedDate.getDate()}`.padStart(2, '0')
  const hour = `${parsedDate.getHours()}`.padStart(2, '0')
  const minute = `${parsedDate.getMinutes()}`.padStart(2, '0')

  return `${year}-${month}-${day} ${hour}:${minute}`
})

const parseUpdateTime = (value) => {
  if (!value) {
    return null
  }

  if (value.includes('T')) {
    const isoDate = new Date(value)
    return Number.isNaN(isoDate.getTime()) ? null : isoDate
  }

  const localDate = new Date(value.replace(' ', 'T'))
  return Number.isNaN(localDate.getTime()) ? null : localDate
}

const loadNews = async () => {
  loading.value = true
  try {
    const res = await getHotNews({
      platform: currentPlatform.value
    })
    const data = res.data || {}
    newsItems.value = data.items || []
    updateTime.value = data.updateTime || ''
  } catch (error) {
    newsItems.value = []
    updateTime.value = ''
    ElMessage.error(copy.loadFailed)
  } finally {
    loading.value = false
  }
}

const switchPlatform = (platform) => {
  if (platform === currentPlatform.value) {
    return
  }
  currentPlatform.value = platform
  loadNews()
}

const getRankClass = (rank) => {
  if (rank === 1) return 'top-one'
  if (rank === 2) return 'top-two'
  if (rank === 3) return 'top-three'
  return 'top-normal'
}

onMounted(loadNews)
</script>

<style lang="scss" scoped>
.hot-news-page {
  min-height: 100%;
  padding: 24px;
  background:
    radial-gradient(circle at top right, rgba(255, 140, 66, 0.16), transparent 24%),
    radial-gradient(circle at left center, rgba(28, 126, 214, 0.14), transparent 26%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.95), rgba(248, 250, 252, 0.92));
}

.hero-panel {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(280px, 0.7fr);
  gap: 20px;
  padding: 28px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 28px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(247, 250, 252, 0.92)),
    linear-gradient(120deg, rgba(255, 132, 0, 0.08), rgba(0, 122, 204, 0.08));
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.08);
}

.eyebrow {
  margin: 0 0 10px;
  color: #d35400;
  font-size: 0.8rem;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.page-title {
  margin: 0;
  font-size: clamp(1.9rem, 3vw, 2.8rem);
  line-height: 1.1;
  color: #18212f;
}

.page-subtitle {
  margin: 12px 0 0;
  max-width: 640px;
  color: #516074;
  font-size: 1rem;
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  justify-content: center;
  align-items: stretch;
}

.platform-meta {
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(15, 23, 42, 0.04);
  border: 1px solid rgba(15, 23, 42, 0.05);
}

.meta-label {
  display: block;
  margin-bottom: 4px;
  color: #7a8798;
  font-size: 0.78rem;
}

.meta-value {
  color: #1b2636;
  font-size: 1rem;
  font-weight: 700;
}

.refresh-button {
  margin-top: 4px;
  height: 46px;
  border-radius: 16px;
}

.platform-switcher {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin: 22px 0;
}

.platform-chip {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 3px;
  min-width: 140px;
  padding: 14px 16px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.78);
  color: #4c5a6b;
  cursor: pointer;
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
}

.platform-chip:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.08);
  border-color: rgba(211, 84, 0, 0.28);
}

.platform-chip.active {
  border-color: transparent;
  background: linear-gradient(135deg, #18212f, #d35400);
  color: #fff;
  box-shadow: 0 16px 32px rgba(211, 84, 0, 0.22);
}

.chip-name {
  font-size: 0.95rem;
  font-weight: 700;
}

.chip-code {
  font-size: 0.72rem;
  opacity: 0.72;
  letter-spacing: 0.06em;
}

.board-panel {
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 28px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.06);
}

.board-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  padding: 22px 24px;
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.02), rgba(255, 255, 255, 0.3));
}

.board-title {
  margin: 0;
  color: #18212f;
  font-size: 1.15rem;
  font-weight: 800;
}

.board-desc {
  margin: 6px 0 0;
  color: #6b7787;
  font-size: 0.86rem;
}

.board-count {
  color: #d35400;
  font-size: 0.9rem;
  font-weight: 700;
}

.loading-state,
.empty-state {
  padding: 22px 24px 30px;
}

.skeleton-row {
  display: grid;
  grid-template-columns: 56px minmax(0, 1fr) 92px;
  gap: 16px;
  align-items: center;
  padding: 14px 0;
}

.skeleton-rank,
.skeleton-hot {
  height: 42px;
  border-radius: 14px;
  background: rgba(15, 23, 42, 0.08);
}

.skeleton-main {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.news-list {
  margin: 0;
  padding: 8px 0 16px;
  list-style: none;
}

.news-row {
  padding: 0 16px;
}

.news-link {
  display: grid;
  grid-template-columns: 56px minmax(0, 1fr) 96px;
  gap: 16px;
  align-items: center;
  padding: 16px 8px;
  border-radius: 20px;
  color: inherit;
  text-decoration: none;
  transition: background-color 0.2s ease, transform 0.2s ease;
}

.news-link.without-heat {
  grid-template-columns: 56px minmax(0, 1fr);
}

.news-link:hover {
  background: rgba(15, 23, 42, 0.035);
  transform: translateX(4px);
}

.rank-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 16px;
  font-size: 1rem;
  font-weight: 800;
}

.top-one {
  background: linear-gradient(135deg, #ff7a18, #ffb347);
  color: #fff;
}

.top-two {
  background: linear-gradient(135deg, #748cab, #a7b7c9);
  color: #fff;
}

.top-three {
  background: linear-gradient(135deg, #b5651d, #d4a373);
  color: #fff;
}

.top-normal {
  background: rgba(15, 23, 42, 0.06);
  color: #415064;
}

.news-main {
  min-width: 0;
}

.news-title {
  margin: 0;
  color: #17202d;
  font-size: 1rem;
  line-height: 1.55;
}

.news-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  color: #758293;
  font-size: 0.8rem;
}

.meta-divider {
  opacity: 0.45;
}

.news-heat {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.heat-label {
  color: #8995a4;
  font-size: 0.72rem;
}

.heat-value {
  color: #d35400;
  font-size: 0.95rem;
  font-weight: 800;
}

@media (max-width: 960px) {
  .hero-panel {
    grid-template-columns: 1fr;
  }

  .hero-actions {
    align-items: stretch;
  }
}

@media (max-width: 640px) {
  .hot-news-page {
    padding: 16px;
  }

  .hero-panel,
  .board-header,
  .loading-state,
  .empty-state {
    padding-left: 18px;
    padding-right: 18px;
  }

  .platform-chip {
    min-width: calc(50% - 6px);
  }

  .news-row {
    padding: 0 10px;
  }

  .news-link {
    grid-template-columns: 44px minmax(0, 1fr);
    gap: 12px;
  }

  .news-heat {
    grid-column: 2;
    align-items: flex-start;
    margin-top: -2px;
  }

  .rank-badge {
    width: 40px;
    height: 40px;
    border-radius: 14px;
    font-size: 0.92rem;
  }

  .news-title {
    font-size: 0.95rem;
  }
}
</style>
