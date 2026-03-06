<template>
  <div class="news-container">
    <el-card class="header-card">
      <div class="header-content">
        <div class="title-section">
          <h2>📰 热点资讯</h2>
          <p class="subtitle">每日精选国内外热点新闻</p>
        </div>
        <div class="action-section">
          <el-button type="primary" @click="showSubscribeDialog = true" icon="Bell">
            订阅邮件推送
          </el-button>
          <el-button @click="refreshNews" icon="Refresh" :loading="loading">
            刷新
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="filter-card">
      <el-radio-group v-model="selectedCategory" size="small" @change="filterNews">
        <el-radio-button label="全部" />
        <el-radio-button label="国内" />
        <el-radio-button label="国际" />
        <el-radio-button label="科技" />
        <el-radio-button label="财经" />
        <el-radio-button label="体育" />
        <el-radio-button label="娱乐" />
      </el-radio-group>
    </el-card>

    <div v-loading="loading" class="news-list">
      <el-empty v-if="filteredNewsList.length === 0" description="暂无资讯" />

      <el-card
        v-for="news in filteredNewsList"
        :key="news.id"
        class="news-item"
        shadow="hover"
      >
        <div class="news-content">
          <div class="news-header">
            <el-tag :type="getCategoryType(news.category)" size="small">
              {{ news.category }}
            </el-tag>
            <span class="news-time">{{ formatTime(news.publishTime) }}</span>
          </div>

          <h3 class="news-title">{{ news.title }}</h3>

          <p class="news-description" v-if="news.content">
            {{ news.content }}
          </p>

          <div class="news-footer">
            <span class="news-source">
              <el-icon><Document /></el-icon>
              {{ news.source }}
            </span>
            <el-button
              v-if="news.url"
              type="primary"
              link
              @click="openUrl(news.url)"
            >
              查看详情 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 订阅对话框 -->
    <el-dialog
      v-model="showSubscribeDialog"
      title="订阅每日资讯"
      width="500px"
    >
      <el-form :model="subscribeForm" label-width="80px">
        <el-form-item label="邮箱">
          <el-input
            v-model="subscribeForm.email"
            placeholder="请输入邮箱地址"
            type="email"
          />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input
            v-model="subscribeForm.username"
            placeholder="请输入您的姓名"
          />
        </el-form-item>
        <el-alert
          title="订阅后，系统将在每天早上8点推送热点资讯到您的邮箱"
          type="info"
          :closable="false"
          style="margin-bottom: 20px"
        />
      </el-form>
      <template #footer>
        <el-button @click="showSubscribeDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubscribe" :loading="subscribing">
          确认订阅
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, ArrowRight } from '@element-plus/icons-vue'

const loading = ref(false)
const newsList = ref([])
const selectedCategory = ref('全部')
const showSubscribeDialog = ref(false)
const subscribing = ref(false)

const subscribeForm = ref({
  email: '',
  username: ''
})

// 过滤后的新闻列表
const filteredNewsList = computed(() => {
  if (selectedCategory.value === '全部') {
    return newsList.value
  }
  return newsList.value.filter(news => news.category === selectedCategory.value)
})

// 获取分类标签类型
const getCategoryType = (category) => {
  const typeMap = {
    '国内': 'danger',
    '国际': 'warning',
    '科技': 'primary',
    '财经': 'success',
    '体育': 'info',
    '娱乐': ''
  }
  return typeMap[category] || ''
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 3600000) { // 1小时内
    return Math.floor(diff / 60000) + '分钟前'
  } else if (diff < 86400000) { // 24小时内
    return Math.floor(diff / 3600000) + '小时前'
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

// 打开链接
const openUrl = (url) => {
  window.open(url, '_blank')
}

// 获取资讯列表
const fetchNews = async () => {
  loading.value = true
  try {
    // 这里调用后端API获取资讯
    const response = await fetch('/api/news/list')
    const res = await response.json()
    if (res.code === 200) {
      newsList.value = res.data || []
      if (newsList.value.length === 0) {
        ElMessage.warning('暂无资讯，正在尝试从免费新闻源同步，请稍后刷新')
      }
    } else {
      ElMessage.error(res.message || '获取资讯失败')
    }
  } catch (error) {
    ElMessage.error('无法连接到服务器')
    // 模拟数据（开发时使用）
    newsList.value = generateMockData()
  } finally {
    loading.value = false
  }
}

// 刷新资讯
const refreshNews = () => {
  fetchNews()
}

// 过滤资讯
const filterNews = () => {
  // 分类已通过computed自动过滤
}

// 订阅
const handleSubscribe = async () => {
  if (!subscribeForm.value.email) {
    ElMessage.warning('请输入邮箱地址')
    return
  }
  if (!subscribeForm.value.username) {
    ElMessage.warning('请输入姓名')
    return
  }

  subscribing.value = true
  try {
    const response = await fetch(
      `/api/news/subscribe?email=${subscribeForm.value.email}&username=${subscribeForm.value.username}`,
      { method: 'POST' }
    )
    const res = await response.json()
    if (res.code === 200) {
      ElMessage.success('订阅成功！每天早上8点将推送资讯到您的邮箱')
      showSubscribeDialog.value = false
      subscribeForm.value = { email: '', username: '' }
    } else {
      ElMessage.error(res.message || '订阅失败')
    }
  } catch (error) {
    ElMessage.error('订阅失败，请稍后重试')
  } finally {
    subscribing.value = false
  }
}

// 生成模拟数据
const generateMockData = () => {
  return [
    {
      id: 1,
      title: '国内经济持续向好，多项指标超预期',
      content: '最新数据显示，国内经济运行总体平稳，多项关键指标表现亮眼，显示出强劲的发展韧性和潜力。',
      source: '新华社',
      url: 'https://example.com/news1',
      category: '国内',
      publishTime: new Date(Date.now() - 3600000).toISOString()
    },
    {
      id: 2,
      title: '人工智能技术取得重大突破',
      content: '研究团队在人工智能领域取得重大突破，新算法在多个基准测试中刷新纪录。',
      source: '科技日报',
      url: 'https://example.com/news2',
      category: '科技',
      publishTime: new Date(Date.now() - 7200000).toISOString()
    },
    {
      id: 3,
      title: '全球股市震荡，投资者关注央行政策',
      content: '受多重因素影响，全球主要股市出现震荡，投资者密切关注各国央行货币政策走向。',
      source: '财经网',
      url: 'https://example.com/news3',
      category: '财经',
      publishTime: new Date(Date.now() - 10800000).toISOString()
    }
  ]
}

onMounted(() => {
  fetchNews()
})
</script>

<style scoped>
.news-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header-card {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.subtitle {
  margin: 5px 0 0 0;
  color: #999;
  font-size: 14px;
}

.action-section {
  display: flex;
  gap: 10px;
}

.filter-card {
  margin-bottom: 20px;
}

.news-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.news-item {
  transition: transform 0.2s;
}

.news-item:hover {
  transform: translateY(-2px);
}

.news-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.news-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.news-time {
  color: #999;
  font-size: 12px;
}

.news-title {
  margin: 0;
  font-size: 18px;
  color: #333;
  line-height: 1.5;
}

.news-description {
  margin: 0;
  color: #666;
  font-size: 14px;
  line-height: 1.6;
}

.news-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}

.news-source {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #999;
  font-size: 13px;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .action-section {
    width: 100%;
  }

  .action-section .el-button {
    flex: 1;
  }
}
</style>
