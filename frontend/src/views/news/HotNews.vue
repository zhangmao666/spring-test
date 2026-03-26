<template>
  <div class="hot-news-page">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">热点新闻</h2>
        <span class="page-subtitle">Powered by NewsAPI.org</span>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchQuery"
          placeholder="搜索关键词..."
          :prefix-icon="Search"
          clearable
          style="width: 220px"
          @keyup.enter="loadNews"
          @clear="loadNews"
        />
        <el-button type="primary" :icon="Refresh" :loading="loading" @click="loadNews">刷新</el-button>
      </div>
    </div>

    <div class="category-tabs">
      <div
        v-for="cat in categories"
        :key="cat.value"
        class="tab-item"
        :class="{ active: currentCategory === cat.value }"
        @click="switchCategory(cat.value)"
      >
        {{ cat.label }}
      </div>
    </div>

    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="articles.length === 0" class="empty-state">
      <el-empty description="暂无新闻数据" />
    </div>

    <div v-else class="news-grid">
      <a
        v-for="(article, index) in articles"
        :key="index"
        :href="article.url"
        target="_blank"
        rel="noopener noreferrer"
        class="news-card"
      >
        <div class="card-image" v-if="article.urlToImage">
          <img :src="article.urlToImage" :alt="article.title" @error="handleImgError($event)" />
        </div>
        <div class="card-image placeholder" v-else>
          <el-icon :size="36"><Reading /></el-icon>
        </div>
        <div class="card-body">
          <div class="card-source">
            <span class="source-name">{{ article.source?.name || '未知来源' }}</span>
            <span class="publish-time">{{ formatTime(article.publishedAt) }}</span>
          </div>
          <h3 class="card-title">{{ article.title }}</h3>
          <p class="card-desc">{{ article.description || '暂无摘要' }}</p>
        </div>
      </a>
    </div>

    <div class="pagination-bar" v-if="totalResults > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="Math.min(totalResults, 100)"
        layout="prev, pager, next"
        @current-change="loadNews"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search, Refresh, Reading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getHotNews } from '@/api/news'

const categories = [
  { label: '全部', value: 'general' },
  { label: '科技', value: 'technology' },
  { label: '商业', value: 'business' },
  { label: '娱乐', value: 'entertainment' },
  { label: '体育', value: 'sports' },
  { label: '科学', value: 'science' },
  { label: '健康', value: 'health' }
]

const loading = ref(false)
const articles = ref([])
const totalResults = ref(0)
const currentCategory = ref('general')
const currentPage = ref(1)
const pageSize = ref(20)
const searchQuery = ref('')

const loadNews = async () => {
  loading.value = true
  try {
    const params = {
      category: currentCategory.value,
      pageSize: pageSize.value,
      page: currentPage.value
    }
    if (searchQuery.value) {
      params.q = searchQuery.value
    }
    const res = await getHotNews(params)
    const data = res.data
    articles.value = (data?.articles || []).filter(a => a.title !== '[Removed]')
    totalResults.value = data?.totalResults || 0
  } catch (e) {
    ElMessage.error('获取新闻失败，请稍后重试')
    articles.value = []
  } finally {
    loading.value = false
  }
}

const switchCategory = (category) => {
  currentCategory.value = category
  currentPage.value = 1
  loadNews()
}

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = Math.floor((now - date) / 1000)
  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`
  return `${Math.floor(diff / 86400)}天前`
}

const handleImgError = (e) => {
  e.target.style.display = 'none'
  e.target.parentElement.classList.add('placeholder')
}

onMounted(loadNews)
</script>

<style lang="scss" scoped>
.hot-news-page {
  padding: 24px;
  min-height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 20px;

  .page-title {
    font-size: 1.6rem;
    font-weight: 800;
    color: var(--text-primary);
    margin: 0 0 4px;
  }

  .page-subtitle {
    font-size: 0.8rem;
    color: var(--text-secondary);
    opacity: 0.6;
  }

  .header-right {
    display: flex;
    gap: 12px;
    align-items: center;
  }
}

.category-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  flex-wrap: wrap;

  .tab-item {
    padding: 6px 18px;
    border-radius: 100px;
    font-size: 0.9rem;
    font-weight: 600;
    cursor: pointer;
    background: rgba(0, 0, 0, 0.04);
    color: var(--text-secondary);
    transition: all 0.2s;

    &:hover {
      background: rgba(99, 102, 241, 0.1);
      color: var(--primary-color);
    }

    &.active {
      background: var(--primary-color);
      color: #fff;
      box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
    }
  }
}

.loading-state {
  padding: 20px 0;
}

.empty-state {
  padding: 60px 0;
}

.news-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.news-card {
  display: flex;
  flex-direction: column;
  background: var(--card-bg, #fff);
  border-radius: 16px;
  overflow: hidden;
  text-decoration: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.25s;
  border: 1px solid rgba(0, 0, 0, 0.05);

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
  }

  .card-image {
    height: 180px;
    overflow: hidden;
    background: rgba(99, 102, 241, 0.06);
    display: flex;
    align-items: center;
    justify-content: center;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s;
    }

    &.placeholder {
      color: rgba(99, 102, 241, 0.3);
    }
  }

  &:hover .card-image img {
    transform: scale(1.04);
  }

  .card-body {
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 8px;
    flex: 1;
  }

  .card-source {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .source-name {
      font-size: 0.78rem;
      font-weight: 700;
      color: var(--primary-color);
    }

    .publish-time {
      font-size: 0.75rem;
      color: var(--text-secondary);
      opacity: 0.6;
    }
  }

  .card-title {
    font-size: 0.95rem;
    font-weight: 700;
    color: var(--text-primary);
    margin: 0;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .card-desc {
    font-size: 0.82rem;
    color: var(--text-secondary);
    margin: 0;
    line-height: 1.6;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
    opacity: 0.75;
  }
}

.pagination-bar {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>
