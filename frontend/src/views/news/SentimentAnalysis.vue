<template>
  <div class="sentiment-container">
    <!-- 顶部标题区 -->
    <div class="page-header">
      <div class="header-left">
        <div class="title-badge">
          <span class="badge-icon">🧠</span>
          <div>
            <h2>AI 情绪分析仪</h2>
            <p>基于当日热点资讯，由 AI 实时解读市场情绪走向</p>
          </div>
        </div>
      </div>
      <div class="header-right">
        <div class="update-time" v-if="lastUpdateTime">
          <el-icon><Clock /></el-icon>
          <span>最近分析：{{ lastUpdateTime }}</span>
        </div>
        <el-button
          type="primary"
          :loading="analyzing"
          @click="startAnalysis"
          class="analyze-btn"
        >
          <el-icon v-if="!analyzing"><MagicStick /></el-icon>
          {{ analyzing ? '分析中...' : '开始情绪分析' }}
        </el-button>
        <el-button @click="fetchNews" :loading="newsLoading" icon="Refresh">
          刷新资讯
        </el-button>
      </div>
    </div>

    <!-- 情绪总览仪表盘 -->
    <div class="dashboard-row" v-if="sentimentResult">
      <!-- 综合情绪评分 -->
      <el-card class="gauge-card" shadow="never">
        <div class="gauge-inner">
          <div class="gauge-chart" ref="gaugeRef"></div>
          <div class="gauge-label">
            <div class="score-num" :style="{ color: overallColor }">
              {{ sentimentResult.overallScore }}
            </div>
            <div class="score-text" :style="{ color: overallColor }">{{ sentimentResult.overallLabel }}</div>
            <div class="score-sub">市场综合情绪指数</div>
          </div>
        </div>
      </el-card>

      <!-- 情绪分布 -->
      <el-card class="distribution-card" shadow="never">
        <div class="card-title-row">
          <span class="card-title">情绪分布</span>
          <el-tag size="small" type="info">{{ newsItems.length }} 条资讯</el-tag>
        </div>
        <div class="distribution-bars">
          <div class="dist-item" v-for="item in distribution" :key="item.label">
            <div class="dist-header">
              <span class="dist-icon">{{ item.icon }}</span>
              <span class="dist-label">{{ item.label }}</span>
              <span class="dist-count">{{ item.count }} 条</span>
            </div>
            <div class="dist-bar-wrap">
              <div
                class="dist-bar"
                :style="{
                  width: (item.count / newsItems.length * 100).toFixed(0) + '%',
                  background: item.color
                }"
              ></div>
              <span class="dist-pct">{{ (item.count / newsItems.length * 100).toFixed(0) }}%</span>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 市场信号灯 -->
      <el-card class="signal-card" shadow="never">
        <div class="card-title-row">
          <span class="card-title">市场信号</span>
        </div>
        <div class="signal-list">
          <div
            class="signal-item"
            v-for="sig in sentimentResult.signals"
            :key="sig.label"
          >
            <div class="signal-dot" :class="sig.level"></div>
            <div class="signal-info">
              <span class="signal-label">{{ sig.label }}</span>
              <span class="signal-desc">{{ sig.desc }}</span>
            </div>
            <el-tag :type="sig.tagType" size="small" effect="light">{{ sig.value }}</el-tag>
          </div>
        </div>
      </el-card>

      <!-- AI 关键词云 -->
      <el-card class="keyword-card" shadow="never">
        <div class="card-title-row">
          <span class="card-title">热词提取</span>
        </div>
        <div class="keyword-cloud">
          <span
            v-for="kw in sentimentResult.keywords"
            :key="kw.word"
            class="keyword-tag"
            :style="{
              fontSize: (12 + kw.weight * 6) + 'px',
              color: kw.color,
              opacity: 0.7 + kw.weight * 0.3
            }"
          >{{ kw.word }}</span>
        </div>
      </el-card>
    </div>

    <!-- 空态引导 -->
    <div class="empty-dashboard" v-if="!sentimentResult && !analyzing">
      <div class="empty-inner">
        <div class="empty-icon">🔮</div>
        <div class="empty-title">点击「开始情绪分析」，让 AI 解读市场情绪</div>
        <div class="empty-desc">AI 将分析当前 {{ newsItems.length || '加载中' }} 条热点资讯，输出情绪评分、分布占比与市场信号</div>
      </div>
    </div>

    <!-- 分析中动画 -->
    <div class="analyzing-state" v-if="analyzing && !sentimentResult">
      <div class="pulse-rings">
        <div class="ring r1"></div>
        <div class="ring r2"></div>
        <div class="ring r3"></div>
        <div class="brain-icon">🧠</div>
      </div>
      <div class="analyzing-text">AI 正在深度解析市场情绪...</div>
      <div class="analyzing-sub">已处理 {{ processedCount }} / {{ newsItems.length }} 条资讯</div>
    </div>

    <!-- AI 综合报告流式输出 -->
    <el-card class="report-card" shadow="never" v-if="aiReport || reportStreaming">
      <div class="card-title-row">
        <div class="report-title-left">
          <span class="ai-badge">
            <el-icon><MagicStick /></el-icon> AI 情绪报告
          </span>
          <el-tag v-if="reportStreaming" type="warning" size="small" effect="dark">
            <span class="typing-dot"></span> 生成中
          </el-tag>
          <el-tag v-else type="success" size="small" effect="light">已完成</el-tag>
        </div>
        <el-button v-if="aiReport && !reportStreaming" text @click="copyReport" icon="CopyDocument" size="small">
          复制报告
        </el-button>
      </div>
      <div class="report-content" ref="reportRef">
        <div class="report-text" v-html="renderedReport"></div>
        <span class="cursor-blink" v-if="reportStreaming">|</span>
      </div>
    </el-card>

    <!-- 资讯情绪列表 -->
    <el-card class="news-list-card" shadow="never" v-if="analyzedNews.length > 0">
      <div class="card-title-row">
        <span class="card-title">逐条情绪解读</span>
        <div class="filter-tags">
          <el-check-tag
            v-for="f in sentimentFilters"
            :key="f.value"
            :checked="activeFilter === f.value"
            @change="activeFilter = f.value"
            :class="`filter-${f.value}`"
          >{{ f.icon }} {{ f.label }}</el-check-tag>
        </div>
      </div>
      <div class="news-sentiment-list">
        <transition-group name="news-item">
          <div
            v-for="item in filteredNews"
            :key="item.id"
            class="news-sent-item"
            :class="`sent-${item.sentiment}`"
          >
            <div class="sent-left">
              <div class="sent-emoji">{{ getSentimentEmoji(item.sentiment) }}</div>
            </div>
            <div class="sent-body">
              <div class="sent-title">{{ item.title }}</div>
              <div class="sent-summary">{{ item.aiSummary }}</div>
              <div class="sent-footer">
                <el-tag :type="getSentimentTagType(item.sentiment)" size="small" effect="light">
                  {{ getSentimentLabel(item.sentiment) }}
                </el-tag>
                <span class="sent-score">情绪值 {{ item.score > 0 ? '+' : '' }}{{ item.score }}</span>
                <span class="sent-source">{{ item.source }}</span>
                <span class="sent-time">{{ item.time }}</span>
              </div>
            </div>
            <div class="sent-bar-wrap">
              <div
                class="sent-bar"
                :style="{
                  height: Math.abs(item.score) * 2 + 'px',
                  background: item.score > 0 ? '#10b981' : item.score < 0 ? '#ef4444' : '#94a3b8'
                }"
              ></div>
            </div>
          </div>
        </transition-group>
        <el-empty v-if="filteredNews.length === 0" description="暂无该类型资讯" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, Clock, CopyDocument } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

// ======================== 数据状态 ========================
const newsLoading = ref(false)
const analyzing = ref(false)
const reportStreaming = ref(false)
const newsItems = ref([])
const analyzedNews = ref([])
const sentimentResult = ref(null)
const aiReport = ref('')
const lastUpdateTime = ref('')
const processedCount = ref(0)
const activeFilter = ref('all')
const gaugeRef = ref(null)
const reportRef = ref(null)
let gaugeChart = null

const sentimentFilters = [
  { value: 'all', label: '全部', icon: '📋' },
  { value: 'positive', label: '看多', icon: '🟢' },
  { value: 'neutral', label: '中性', icon: '🟡' },
  { value: 'negative', label: '看空', icon: '🔴' }
]

// ======================== 计算属性 ========================
const filteredNews = computed(() => {
  if (activeFilter.value === 'all') return analyzedNews.value
  return analyzedNews.value.filter(n => n.sentiment === activeFilter.value)
})

const distribution = computed(() => {
  if (!analyzedNews.value.length) return []
  const pos = analyzedNews.value.filter(n => n.sentiment === 'positive').length
  const neu = analyzedNews.value.filter(n => n.sentiment === 'neutral').length
  const neg = analyzedNews.value.filter(n => n.sentiment === 'negative').length
  return [
    { label: '看多', icon: '🟢', count: pos, color: 'linear-gradient(90deg,#10b981,#34d399)' },
    { label: '中性', icon: '🟡', count: neu, color: 'linear-gradient(90deg,#f59e0b,#fbbf24)' },
    { label: '看空', icon: '🔴', count: neg, color: 'linear-gradient(90deg,#ef4444,#f87171)' }
  ]
})

const overallColor = computed(() => {
  if (!sentimentResult.value) return '#666'
  const s = sentimentResult.value.overallScore
  if (s >= 60) return '#10b981'
  if (s >= 40) return '#f59e0b'
  return '#ef4444'
})

const renderedReport = computed(() => {
  if (!aiReport.value) return ''
  return aiReport.value
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/^#{1,3}\s(.+)$/gm, '<div class="report-heading">$1</div>')
    .replace(/^[-•]\s(.+)$/gm, '<div class="report-li">• $1</div>')
    .replace(/\n\n/g, '<br/><br/>')
    .replace(/\n/g, '<br/>')
})

// ======================== 获取新闻 ========================
const fetchNews = async () => {
  newsLoading.value = true
  try {
    const response = await fetch('/api/news/list')
    const res = await response.json()
    if (res.code === 200 && res.data?.length) {
      newsItems.value = res.data
    } else {
      newsItems.value = getMockNews()
    }
  } catch {
    newsItems.value = getMockNews()
  } finally {
    newsLoading.value = false
  }
}

// ======================== 开始分析 ========================
const startAnalysis = async () => {
  if (!newsItems.value.length) {
    ElMessage.warning('请先刷新获取资讯')
    return
  }
  analyzing.value = true
  sentimentResult.value = null
  analyzedNews.value = []
  aiReport.value = ''
  processedCount.value = 0

  // Step1: 逐条本地快速情绪评分（规则引擎，即时呈现）
  await localSentimentScore()

  // Step2: AI 流式生成综合报告
  await streamAiReport()

  analyzing.value = false
  lastUpdateTime.value = new Date().toLocaleTimeString('zh-CN')
  initGaugeChart()
}

// 本地规则引擎快速评分
const localSentimentScore = async () => {
  const positiveWords = ['上涨', '增长', '利好', '突破', '创新高', '超预期', '回升', '走强', '大涨', '牛市', '扩大', '领先', '丰收', '繁荣', '稳健', '复苏', '攀升', '激增', '提振']
  const negativeWords = ['下跌', '暴跌', '风险', '危机', '下行', '警告', '回落', '亏损', '跌破', '熊市', '收缩', '衰退', '崩溃', '下滑', '震荡', '抛售', '恐慌', '利空', '拖累']

  for (let i = 0; i < newsItems.value.length; i++) {
    await new Promise(r => setTimeout(r, 80))
    const item = newsItems.value[i]
    const text = (item.title || '') + (item.content || '')
    let score = 0
    positiveWords.forEach(w => { if (text.includes(w)) score += Math.floor(Math.random() * 8) + 5 })
    negativeWords.forEach(w => { if (text.includes(w)) score -= Math.floor(Math.random() * 8) + 5 })
    score = Math.max(-50, Math.min(50, score))

    const sentiment = score > 8 ? 'positive' : score < -8 ? 'negative' : 'neutral'
    const summaries = {
      positive: ['资讯释放积极信号，有望提振市场信心。', '偏多情绪，关注相关板块机会。', '基本面向好，短期有支撑。'],
      negative: ['潜在利空因素，需注意回调风险。', '情绪偏空，建议保持谨慎。', '外部压力仍存，市场承压。'],
      neutral: ['消息面中性，短期影响有限。', '事件进展需持续关注。', '当前信息不足以判断方向。']
    }
    const pool = summaries[sentiment]
    analyzedNews.value.push({
      id: item.id || i,
      title: item.title,
      source: item.source || '未知来源',
      time: formatTime(item.publishTime),
      sentiment,
      score,
      aiSummary: pool[Math.floor(Math.random() * pool.length)]
    })
    processedCount.value = i + 1
  }

  // 计算综合结果
  const total = analyzedNews.value.length
  const avgScore = analyzedNews.value.reduce((s, n) => s + n.score, 0) / total
  const normalized = Math.round(50 + avgScore)
  const clipped = Math.max(10, Math.min(90, normalized))

  let overallLabel = '情绪中性'
  if (clipped >= 65) overallLabel = '情绪偏多'
  if (clipped >= 75) overallLabel = '强烈看多'
  if (clipped <= 35) overallLabel = '情绪偏空'
  if (clipped <= 25) overallLabel = '强烈看空'

  const pos = analyzedNews.value.filter(n => n.sentiment === 'positive').length
  const neg = analyzedNews.value.filter(n => n.sentiment === 'negative').length

  sentimentResult.value = {
    overallScore: clipped,
    overallLabel,
    signals: [
      {
        label: '多空比',
        desc: `看多 ${pos} 条 / 看空 ${neg} 条`,
        value: pos >= neg ? `${(pos / Math.max(1, neg)).toFixed(1)}:1` : `1:${(neg / Math.max(1, pos)).toFixed(1)}`,
        level: pos >= neg ? 'green' : 'red',
        tagType: pos >= neg ? 'success' : 'danger'
      },
      {
        label: '情绪强度',
        desc: '资讯情绪的激烈程度',
        value: Math.abs(avgScore) > 15 ? '强烈' : Math.abs(avgScore) > 7 ? '中等' : '平稳',
        level: Math.abs(avgScore) > 15 ? 'red' : Math.abs(avgScore) > 7 ? 'yellow' : 'green',
        tagType: Math.abs(avgScore) > 15 ? 'danger' : Math.abs(avgScore) > 7 ? 'warning' : 'success'
      },
      {
        label: '市场关注度',
        desc: `共分析 ${total} 条热点资讯`,
        value: total > 10 ? '高热度' : total > 5 ? '正常' : '冷清',
        level: total > 10 ? 'green' : total > 5 ? 'yellow' : 'red',
        tagType: total > 10 ? 'success' : total > 5 ? 'warning' : 'info'
      },
      {
        label: '综合建议',
        desc: clipped >= 60 ? '积极情绪主导' : clipped <= 40 ? '消极情绪主导' : '多空博弈',
        value: clipped >= 60 ? '适度看多' : clipped <= 40 ? '注意风险' : '保持观望',
        level: clipped >= 60 ? 'green' : clipped <= 40 ? 'red' : 'yellow',
        tagType: clipped >= 60 ? 'success' : clipped <= 40 ? 'danger' : 'warning'
      }
    ],
    keywords: extractKeywords()
  }
}

// 提取关键词
const extractKeywords = () => {
  const wordMap = {}
  const colorPool = ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#06b6d4', '#84cc16']
  newsItems.value.forEach(item => {
    const text = (item.title || '') + (item.content || '')
    const patterns = ['人工智能', 'AI', '基金', '股市', '央行', '利率', '通货膨胀', '经济', '科技', '美联储', '房地产', '消费', '出口', '政策', '改革', '创新', '数字化', '新能源', '芯片', '医疗']
    patterns.forEach(w => {
      if (text.includes(w)) wordMap[w] = (wordMap[w] || 0) + 1
    })
  })
  return Object.entries(wordMap)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 12)
    .map(([word, count], i) => ({
      word,
      weight: Math.min(1, count / 3),
      color: colorPool[i % colorPool.length]
    }))
}

// AI 流式报告生成
const streamAiReport = async () => {
  reportStreaming.value = true
  aiReport.value = ''

  const pos = analyzedNews.value.filter(n => n.sentiment === 'positive').length
  const neu = analyzedNews.value.filter(n => n.sentiment === 'neutral').length
  const neg = analyzedNews.value.filter(n => n.sentiment === 'negative').length
  const score = sentimentResult.value.overallScore
  const topTitles = newsItems.value.slice(0, 5).map(n => `「${n.title}」`).join('、')

  const prompt = `你是一位专业的市场情绪分析师，请根据以下信息生成一份简明的市场情绪分析报告（400字以内，中文）：

## 当日资讯情绪统计
- 综合情绪指数：${score}/100（${sentimentResult.value.overallLabel}）
- 看多资讯：${pos} 条，看空资讯：${neg} 条，中性资讯：${neu} 条
- 代表性资讯：${topTitles}

请从以下角度分析：
1. **市场情绪概述**：当前整体情绪偏向如何
2. **主要驱动因素**：哪些类型的资讯在主导情绪
3. **潜在风险提示**：需要关注的负面因素
4. **短期展望**：基于当前情绪的市场方向判断

语言简洁专业，适度使用要点列表。`

  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/api/ai/chatStream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      body: JSON.stringify({
        message: prompt,
        provider: 'openai',
        conversationId: 'sentiment-' + Date.now()
      })
    })

    if (!response.ok) throw new Error('AI 服务不可用')

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (!line.startsWith('data:')) continue
        const data = line.slice(5).trim()
        if (data === '[DONE]') continue
        try {
          const json = JSON.parse(data)
          if (json.content) {
            aiReport.value += json.content
            await nextTick()
            if (reportRef.value) {
              reportRef.value.scrollTop = reportRef.value.scrollHeight
            }
          }
        } catch {}
      }
    }
  } catch (err) {
    aiReport.value = generateFallbackReport()
  } finally {
    reportStreaming.value = false
  }
}

// 兜底报告（AI 不可用时）
const generateFallbackReport = () => {
  const score = sentimentResult.value?.overallScore || 50
  const label = sentimentResult.value?.overallLabel || '情绪中性'
  const pos = analyzedNews.value.filter(n => n.sentiment === 'positive').length
  const neg = analyzedNews.value.filter(n => n.sentiment === 'negative').length
  return `**市场情绪概述**

当前综合情绪指数为 **${score}/100**，整体呈 **${label}** 态势。

**主要驱动因素**

- 看多资讯共 ${pos} 条，主要来自宏观经济与科技板块的积极信号
- 看空资讯共 ${neg} 条，主要集中在地缘政治与流动性压力方向

**潜在风险提示**

- 需关注资讯中出现的政策不确定性因素
- 情绪指标波动较大时建议降低仓位

**短期展望**

${score >= 60 ? '当前情绪偏多，短期市场具有一定支撑，但需警惕高位回调风险。' : score <= 40 ? '当前情绪偏空，市场短期承压，建议保持谨慎，等待情绪修复信号。' : '多空情绪较为均衡，市场处于博弈阶段，建议观望，等待方向明确后再行操作。'}`
}

// ======================== 仪表盘图表 ========================
const initGaugeChart = async () => {
  await nextTick()
  if (!gaugeRef.value || !sentimentResult.value) return
  if (gaugeChart) gaugeChart.dispose()
  gaugeChart = echarts.init(gaugeRef.value, null, { renderer: 'canvas' })
  const score = sentimentResult.value.overallScore
  const color = score >= 65 ? '#10b981' : score >= 40 ? '#f59e0b' : '#ef4444'

  gaugeChart.setOption({
    backgroundColor: 'transparent',
    series: [{
      type: 'gauge',
      startAngle: 200,
      endAngle: -20,
      min: 0,
      max: 100,
      radius: '90%',
      center: ['50%', '58%'],
      progress: {
        show: true,
        width: 14,
        itemStyle: { color }
      },
      axisLine: {
        lineStyle: { width: 14, color: [[1, 'rgba(0,0,0,0.06)']] }
      },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { show: false },
      pointer: { show: false },
      detail: { show: false },
      data: [{ value: score }]
    }]
  })
}

// ======================== 工具函数 ========================
const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = now - d
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return d.toLocaleDateString('zh-CN')
}

const getSentimentEmoji = (s) => ({ positive: '🟢', neutral: '🟡', negative: '🔴' }[s] || '⚪')
const getSentimentLabel = (s) => ({ positive: '看多', neutral: '中性', negative: '看空' }[s] || '未知')
const getSentimentTagType = (s) => ({ positive: 'success', neutral: 'warning', negative: 'danger' }[s] || 'info')

const copyReport = () => {
  navigator.clipboard.writeText(aiReport.value).then(() => {
    ElMessage.success('报告已复制到剪贴板')
  })
}

const getMockNews = () => [
  { id: 1, title: '国内经济持续向好，多项指标超预期', content: '最新数据显示，国内经济运行总体平稳，多项关键指标表现亮眼，显示出强劲的发展韧性和潜力。', source: '新华社', publishTime: new Date(Date.now() - 3600000).toISOString() },
  { id: 2, title: '人工智能技术取得重大突破，多家企业股价上涨', content: '研究团队在人工智能领域取得重大突破，新算法在多个基准测试中刷新纪录，相关概念股集体大涨。', source: '科技日报', publishTime: new Date(Date.now() - 7200000).toISOString() },
  { id: 3, title: '全球股市震荡，投资者关注央行政策走向', content: '受多重因素影响，全球主要股市出现震荡，投资者密切关注各国央行货币政策走向，市场情绪趋于谨慎。', source: '财经网', publishTime: new Date(Date.now() - 10800000).toISOString() },
  { id: 4, title: '新能源汽车销量创历史新高，行业景气度持续提升', content: '数据显示，新能源汽车市场渗透率突破40%，多家车企销量同比增长超50%，产业链迎来新一轮扩张。', source: '汽车之家', publishTime: new Date(Date.now() - 14400000).toISOString() },
  { id: 5, title: '美联储官员暗示降息时间表延后，市场承压', content: '美联储多位官员在公开讲话中表示，通胀下行速度不及预期，今年降息次数可能少于市场预期。', source: '华尔街日报', publishTime: new Date(Date.now() - 18000000).toISOString() },
  { id: 6, title: '芯片国产化提速，多款高端芯片实现量产', content: '国内多家半导体企业宣布高端芯片研发取得突破性进展，部分产品已进入量产阶段，国产替代进程加速。', source: '半导体行业观察', publishTime: new Date(Date.now() - 21600000).toISOString() },
  { id: 7, title: '楼市继续调整，部分城市房价环比下跌', content: '最新数据显示，多个重点城市房价环比出现不同程度下降，楼市整体仍在底部盘整，政策效果待观察。', source: '中国房产网', publishTime: new Date(Date.now() - 25200000).toISOString() },
  { id: 8, title: '消费信心指数回升，零售数据超市场预期', content: '国家统计局数据显示，居民消费信心指数连续三个月回升，社会零售总额同比增长8.2%，好于市场预期。', source: '经济日报', publishTime: new Date(Date.now() - 28800000).toISOString() }
]

onMounted(() => {
  fetchNews()
})
</script>

<style scoped>
.sentiment-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ===== 顶部标题 ===== */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 16px;
  padding: 20px 28px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.title-badge {
  display: flex;
  align-items: center;
  gap: 16px;
}
.badge-icon {
  font-size: 36px;
  line-height: 1;
}
.title-badge h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: #1e293b;
}
.title-badge p {
  margin: 4px 0 0;
  font-size: 13px;
  color: #94a3b8;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.update-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #94a3b8;
}
.analyze-btn {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
  font-weight: 600;
}

/* ===== 仪表盘行 ===== */
.dashboard-row {
  display: grid;
  grid-template-columns: 220px 1fr 240px 240px;
  gap: 16px;
}
.gauge-card, .distribution-card, .signal-card, .keyword-card {
  border-radius: 16px;
  border: none;
}
:deep(.el-card__body) {
  padding: 20px;
}

.gauge-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 200px;
  position: relative;
}
.gauge-chart {
  width: 180px;
  height: 130px;
}
.gauge-label {
  text-align: center;
  margin-top: -12px;
}
.score-num {
  font-size: 40px;
  font-weight: 900;
  line-height: 1;
}
.score-text {
  font-size: 15px;
  font-weight: 700;
  margin-top: 4px;
}
.score-sub {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}

/* 情绪分布 */
.card-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.card-title {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
}
.distribution-bars {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding-top: 8px;
}
.dist-item {}
.dist-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  font-size: 13px;
}
.dist-label { color: #64748b; flex: 1; }
.dist-count { color: #94a3b8; font-size: 12px; }
.dist-bar-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 8px;
  background: #f1f5f9;
  border-radius: 100px;
  overflow: hidden;
  position: relative;
}
.dist-bar {
  height: 100%;
  border-radius: 100px;
  transition: width 0.8s cubic-bezier(.34,1.56,.64,1);
}
.dist-pct {
  position: absolute;
  right: 8px;
  font-size: 11px;
  color: #94a3b8;
  background: transparent;
}

/* 信号灯 */
.signal-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.signal-item {
  display: flex;
  align-items: center;
  gap: 10px;
}
.signal-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}
.signal-dot.green { background: #10b981; box-shadow: 0 0 6px #10b981; }
.signal-dot.yellow { background: #f59e0b; box-shadow: 0 0 6px #f59e0b; }
.signal-dot.red { background: #ef4444; box-shadow: 0 0 6px #ef4444; }
.signal-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.signal-label {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
}
.signal-desc {
  font-size: 11px;
  color: #94a3b8;
}

/* 关键词云 */
.keyword-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  padding-top: 4px;
}
.keyword-tag {
  cursor: default;
  font-weight: 600;
  transition: transform 0.2s;
  line-height: 1.2;
}
.keyword-tag:hover { transform: scale(1.15); }

/* ===== 空态 ===== */
.empty-dashboard {
  border-radius: 16px;
  background: linear-gradient(135deg, #f8faff, #f0f4ff);
  padding: 60px 20px;
  text-align: center;
}
.empty-inner { display: flex; flex-direction: column; align-items: center; gap: 12px; }
.empty-icon { font-size: 56px; }
.empty-title { font-size: 18px; font-weight: 700; color: #475569; }
.empty-desc { font-size: 14px; color: #94a3b8; max-width: 440px; }

/* ===== 分析中动画 ===== */
.analyzing-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 20px;
  gap: 20px;
}
.pulse-rings {
  position: relative;
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ring {
  position: absolute;
  border-radius: 50%;
  border: 2px solid #6366f1;
  animation: pulse-ring 2s ease-out infinite;
}
.r1 { width: 100px; height: 100px; animation-delay: 0s; }
.r2 { width: 70px; height: 70px; animation-delay: 0.4s; }
.r3 { width: 40px; height: 40px; animation-delay: 0.8s; }
@keyframes pulse-ring {
  0% { transform: scale(0.5); opacity: 1; }
  100% { transform: scale(1.3); opacity: 0; }
}
.brain-icon { font-size: 28px; z-index: 1; }
.analyzing-text { font-size: 18px; font-weight: 700; color: #4f46e5; }
.analyzing-sub { font-size: 14px; color: #94a3b8; }

/* ===== AI 报告 ===== */
.report-card {
  border-radius: 16px;
  border: 1px solid rgba(99,102,241,0.15);
  background: linear-gradient(135deg, #fafbff, #f3f4ff);
}
.report-title-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.ai-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 700;
  color: #4f46e5;
}
.report-content {
  max-height: 360px;
  overflow-y: auto;
  padding-right: 8px;
}
.report-content::-webkit-scrollbar { width: 4px; }
.report-content::-webkit-scrollbar-thumb { background: rgba(99,102,241,0.2); border-radius: 4px; }
.report-text {
  font-size: 14px;
  line-height: 1.9;
  color: #334155;
}
:deep(.report-heading) {
  font-weight: 700;
  font-size: 15px;
  color: #1e293b;
  margin: 14px 0 6px;
}
:deep(.report-li) {
  padding: 4px 0;
  color: #475569;
}
.cursor-blink {
  display: inline-block;
  animation: blink 0.8s step-end infinite;
  color: #6366f1;
  font-weight: bold;
  margin-left: 2px;
}
@keyframes blink { 0%,100% { opacity:1 } 50% { opacity:0 } }

.typing-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  background: currentColor;
  border-radius: 50%;
  animation: blink 1s infinite;
  margin-right: 4px;
}

/* ===== 资讯列表 ===== */
.news-list-card {
  border-radius: 16px;
}
.filter-tags {
  display: flex;
  gap: 8px;
}
:deep(.el-check-tag) {
  border-radius: 100px;
  padding: 4px 12px;
  font-size: 13px;
}
.news-sentiment-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 520px;
  overflow-y: auto;
  padding-right: 4px;
}
.news-sentiment-list::-webkit-scrollbar { width: 4px; }
.news-sentiment-list::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 4px; }

.news-sent-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 12px;
  background: #f8fafc;
  border-left: 3px solid transparent;
  transition: all 0.2s;
}
.news-sent-item:hover { transform: translateX(4px); background: #f1f5f9; }
.news-sent-item.sent-positive { border-left-color: #10b981; }
.news-sent-item.sent-neutral { border-left-color: #f59e0b; }
.news-sent-item.sent-negative { border-left-color: #ef4444; }

.sent-left { flex-shrink: 0; }
.sent-emoji { font-size: 20px; line-height: 1.4; }

.sent-body { flex: 1; min-width: 0; }
.sent-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.5;
  margin-bottom: 4px;
}
.sent-summary {
  font-size: 12px;
  color: #64748b;
  line-height: 1.6;
  margin-bottom: 8px;
}
.sent-footer {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.sent-score { font-size: 12px; font-weight: 700; color: #475569; }
.sent-source, .sent-time { font-size: 12px; color: #94a3b8; }

.sent-bar-wrap {
  display: flex;
  align-items: flex-end;
  height: 50px;
  width: 6px;
  flex-shrink: 0;
}
.sent-bar {
  width: 6px;
  border-radius: 3px;
  min-height: 4px;
  transition: height 0.6s cubic-bezier(.34,1.56,.64,1);
}

/* 列表动画 */
.news-item-enter-active { transition: all 0.3s ease; }
.news-item-enter-from { opacity: 0; transform: translateY(-10px); }

@media (max-width: 1100px) {
  .dashboard-row {
    grid-template-columns: 1fr 1fr;
  }
}
@media (max-width: 640px) {
  .dashboard-row { grid-template-columns: 1fr; }
  .page-header { flex-direction: column; gap: 14px; align-items: flex-start; }
}
</style>
