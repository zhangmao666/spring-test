<template>
  <div class="page-container fund-analysis-view">
    <!-- 顶部标题区 -->
    <div class="header-section">
      <el-page-header @back="goBack">
        <template #content>
          <div class="header-content">
            <h1 class="text-gradient">🤖 AI 走势智能分析</h1>
            <el-tag v-if="fundCode" type="primary" effect="dark" round>{{ fundCode }}</el-tag>
          </div>
        </template>
      </el-page-header>
    </div>

    <!-- 基金代码输入区 -->
    <el-card class="search-card glass-container" v-if="!fundCode">
      <div class="search-area">
        <div class="search-icon">🔍</div>
        <h2>输入基金代码开始分析</h2>
        <p class="search-desc">AI 将根据历史走势数据为您提供深度分析报告</p>
        <div class="search-input-group">
          <el-input
            v-model="inputCode"
            placeholder="请输入基金代码，如 005827"
            size="large"
            clearable
            @keyup.enter="startAnalysis"
          >
            <template #prefix>
              <el-icon><TrendCharts /></el-icon>
            </template>
          </el-input>
          <el-select v-model="selectedDays" size="large" style="width: 140px">
            <el-option :value="30" label="近1个月" />
            <el-option :value="90" label="近3个月" />
            <el-option :value="180" label="近半年" />
            <el-option :value="365" label="近1年" />
          </el-select>
          <el-button type="primary" size="large" @click="startAnalysis" :loading="loadingSummary">
            <el-icon><MagicStick /></el-icon> 开始分析
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 分析结果区 -->
    <template v-if="fundCode">
      <!-- 走势摘要卡片组 -->
      <div class="summary-section" v-loading="loadingSummary">
        <el-row :gutter="16">
          <el-col :span="6">
            <div class="metric-card glass-container">
              <div class="metric-icon" style="background: linear-gradient(135deg, #6366f1, #8b5cf6)">
                <el-icon :size="22"><TrendCharts /></el-icon>
              </div>
              <div class="metric-info">
                <div class="metric-label">最新净值</div>
                <div class="metric-value">{{ summary?.trendSummary?.latestNetValue?.toFixed(4) || '--' }}</div>
                <div class="metric-change" :class="(summary?.trendSummary?.latestChange || 0) >= 0 ? 'up' : 'down'">
                  {{ (summary?.trendSummary?.latestChange || 0) >= 0 ? '+' : '' }}{{ (summary?.trendSummary?.latestChange || 0).toFixed(2) }}%
                </div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="metric-card glass-container">
              <div class="metric-icon" style="background: linear-gradient(135deg, #10b981, #059669)">
                <el-icon :size="22"><DataLine /></el-icon>
              </div>
              <div class="metric-info">
                <div class="metric-label">区间收益</div>
                <div class="metric-value" :class="(summary?.trendSummary?.totalReturn || 0) >= 0 ? 'up' : 'down'">
                  {{ (summary?.trendSummary?.totalReturn || 0) >= 0 ? '+' : '' }}{{ (summary?.trendSummary?.totalReturn || 0).toFixed(2) }}%
                </div>
                <div class="metric-sub">{{ selectedDays }}天</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="metric-card glass-container">
              <div class="metric-icon" style="background: linear-gradient(135deg, #f59e0b, #d97706)">
                <el-icon :size="22"><Warning /></el-icon>
              </div>
              <div class="metric-info">
                <div class="metric-label">振幅 / 波动</div>
                <div class="metric-value">{{ (summary?.trendSummary?.volatility || 0).toFixed(2) }}%</div>
                <div class="metric-sub">{{ summary?.trendSummary?.dataPoints || 0 }}个交易日</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="metric-card glass-container">
              <div class="metric-icon" style="background: linear-gradient(135deg, #ef4444, #dc2626)">
                <el-icon :size="22"><Histogram /></el-icon>
              </div>
              <div class="metric-info">
                <div class="metric-label">净值区间</div>
                <div class="metric-value">{{ (summary?.trendSummary?.minValue || 0).toFixed(2) }} ~ {{ (summary?.trendSummary?.maxValue || 0).toFixed(2) }}</div>
                <div class="metric-sub">日均涨跌 {{ (summary?.trendSummary?.avgChange || 0).toFixed(4) }}%</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 迷你走势图 + 操作区 -->
      <el-card class="chart-control-card glass-container">
        <div class="chart-control-row">
          <div class="mini-chart-area">
            <div class="mini-chart-header">
              <span class="mini-chart-title">{{ summary?.fundName || '基金' }} 近期走势</span>
              <el-tag v-if="summary?.fundName" type="" effect="plain" size="small">{{ summary?.fundCode }}</el-tag>
            </div>
            <div class="mini-sparkline-row">
              <div v-for="(point, idx) in (summary?.trendSummary?.recentPoints || [])" :key="idx" class="spark-bar-wrapper">
                <div
                  class="spark-bar"
                  :class="point.changePercent >= 0 ? 'up' : 'down'"
                  :style="{ height: getBarHeight(point.changePercent) + 'px' }"
                ></div>
                <span class="spark-label">{{ point.date }}</span>
              </div>
            </div>
          </div>
          <div class="control-area">
            <el-select v-model="selectedDays" size="default" style="width: 120px" @change="refreshSummary">
              <el-option :value="30" label="近1个月" />
              <el-option :value="90" label="近3个月" />
              <el-option :value="180" label="近半年" />
              <el-option :value="365" label="近1年" />
            </el-select>
            <el-button type="primary" @click="startAiAnalysis" :loading="analyzing" :disabled="analyzing">
              <el-icon><MagicStick /></el-icon>
              {{ analyzing ? 'AI 分析中...' : '开始 AI 分析' }}
            </el-button>
            <el-button @click="resetAnalysis" :icon="RefreshRight">
              重新分析
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- AI 分析面板 -->
      <el-card class="analysis-card glass-container" id="analysis-panel">
        <template #header>
          <div class="analysis-header">
            <div class="analysis-title">
              <span class="ai-badge">
                <span class="ai-dot" :class="{ 'active': analyzing }"></span>
                AI
              </span>
              <span>深度走势分析报告</span>
            </div>
            <div class="analysis-meta" v-if="analysisContent">
              <el-tag type="info" effect="plain" size="small">
                <el-icon><Clock /></el-icon> {{ analysisDuration }}s
              </el-tag>
            </div>
          </div>
        </template>

        <!-- 分析中的动画 -->
        <div v-if="analyzing && !analysisContent" class="analysis-loading">
          <div class="loading-brain">
            <div class="brain-pulse"></div>
            <span class="brain-icon">🧠</span>
          </div>
          <p class="loading-text">AI 正在分析 <strong>{{ summary?.fundName }}</strong> 的走势数据...</p>
          <p class="loading-hint">正在提取 {{ summary?.trendSummary?.dataPoints || 0 }} 个交易日的净值数据进行深度分析</p>
        </div>

        <!-- AI 分析内容（流式输出） -->
        <div v-if="analysisContent" class="analysis-body">
          <div class="markdown-content" v-html="renderedMarkdown"></div>
          <div v-if="analyzing" class="typing-cursor"></div>
        </div>

        <!-- 空状态 -->
        <div v-if="!analyzing && !analysisContent" class="analysis-empty">
          <div class="empty-icon">📊</div>
          <h3>准备就绪</h3>
          <p>点击上方「开始 AI 分析」按钮，获取基于 {{ selectedDays }} 天历史数据的深度走势分析报告</p>
          <el-button type="primary" size="large" @click="startAiAnalysis">
            <el-icon><MagicStick /></el-icon> 立即分析
          </el-button>
        </div>

        <!-- 分析完成提示 -->
        <div v-if="analysisComplete && !analyzing" class="analysis-complete-badge">
          <el-icon><CircleCheck /></el-icon> 分析报告已生成完毕
        </div>
      </el-card>

      <!-- 风险提示底栏 -->
      <div class="disclaimer">
        <el-alert type="warning" :closable="false" center>
          <template #title>
            ⚠️ 重要提示：以上分析由AI生成，仅供参考，不构成任何投资建议。投资有风险，入市需谨慎。过往业绩不代表未来表现。
          </template>
        </el-alert>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  TrendCharts, MagicStick, DataLine, Warning, Histogram,
  RefreshRight, Clock, CircleCheck
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

// 基础状态
const fundCode = ref(route.params.fundCode || '')
const inputCode = ref('')
const selectedDays = ref(90)
const loadingSummary = ref(false)
const analyzing = ref(false)
const analysisContent = ref('')
const analysisComplete = ref(false)
const summary = ref(null)

// 分析时长追踪
const analysisStartTime = ref(0)
const analysisDuration = ref(0)
let durationTimer = null

// SSE 连接引用
let eventSource = null

// 计算渲染后的 Markdown 内容
const renderedMarkdown = computed(() => {
  return renderMarkdown(analysisContent.value)
})

// 简单的 Markdown 渲染函数
function renderMarkdown(text) {
  if (!text) return ''
  let html = text
    // 转义 HTML
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    // 标题
    .replace(/^### (.+)$/gm, '<h3>$1</h3>')
    .replace(/^## (.+)$/gm, '<h2>$1</h2>')
    .replace(/^# (.+)$/gm, '<h1>$1</h1>')
    // 粗体和斜体
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    // 列表
    .replace(/^- (.+)$/gm, '<li>$1</li>')
    .replace(/^(\d+)\. (.+)$/gm, '<li>$2</li>')
    // 代码
    .replace(/`(.+?)`/g, '<code>$1</code>')
    // 分割线
    .replace(/^---$/gm, '<hr/>')
    // 换行
    .replace(/\n\n/g, '</p><p>')
    .replace(/\n/g, '<br/>')

  // 包裹 li 标签
  html = html.replace(/((<li>.*?<\/li>\s*)+)/g, '<ul>$1</ul>')

  return `<p>${html}</p>`
}

// 计算柱状图高度
function getBarHeight(changePercent) {
  const absVal = Math.abs(changePercent || 0)
  return Math.min(Math.max(absVal * 15, 4), 60)
}

// 返回
const goBack = () => router.back()

// 开始分析（从搜索入口）
const startAnalysis = () => {
  if (!inputCode.value.trim()) {
    ElMessage.warning('请输入基金代码')
    return
  }
  fundCode.value = inputCode.value.trim()
  loadSummary()
}

// 加载走势摘要
const loadSummary = async () => {
  loadingSummary.value = true
  analysisContent.value = ''
  analysisComplete.value = false
  try {
    const response = await fetch(`/api/fund/analysis/summary/${fundCode.value}?days=${selectedDays.value}`)
    const res = await response.json()
    if (res.code === 200) {
      summary.value = res.data
      if (!res.data.trendSummary) {
        ElMessage.warning('暂无该基金的历史数据，正在尝试获取...')
      }
    } else {
      ElMessage.error(res.message || '获取摘要失败')
    }
  } catch (error) {
    ElMessage.error('无法连接到服务器')
  } finally {
    loadingSummary.value = false
  }
}

// 刷新摘要（切换时间范围时触发）
const refreshSummary = () => {
  if (fundCode.value) {
    loadSummary()
  }
}

// 开始 AI 流式分析
const startAiAnalysis = () => {
  if (!fundCode.value) return
  if (analyzing.value) return

  // 重置状态
  analysisContent.value = ''
  analysisComplete.value = false
  analyzing.value = true
  analysisStartTime.value = Date.now()
  analysisDuration.value = 0

  // 启动时长计时器
  durationTimer = setInterval(() => {
    analysisDuration.value = ((Date.now() - analysisStartTime.value) / 1000).toFixed(1)
  }, 100)

  // 建立 SSE 连接
  const url = `/api/fund/analysis/stream/${fundCode.value}?days=${selectedDays.value}`
  eventSource = new EventSource(url)

  eventSource.addEventListener('start', (event) => {
    console.log('AI 分析开始:', event.data)
  })

  eventSource.addEventListener('message', (event) => {
    try {
      const data = JSON.parse(event.data)
      if (data.content) {
        analysisContent.value += data.content
        // 自动滚动到底部
        nextTick(() => {
          const panel = document.getElementById('analysis-panel')
          if (panel) {
            panel.scrollIntoView({ behavior: 'smooth', block: 'end' })
          }
        })
      }
    } catch (e) {
      console.error('解析SSE消息失败:', e)
    }
  })

  eventSource.addEventListener('done', (event) => {
    console.log('AI 分析完成:', event.data)
    analyzing.value = false
    analysisComplete.value = true
    clearInterval(durationTimer)
    analysisDuration.value = ((Date.now() - analysisStartTime.value) / 1000).toFixed(1)
    eventSource.close()
    eventSource = null
  })

  eventSource.addEventListener('error', (event) => {
    console.error('SSE 错误:', event)
    analyzing.value = false
    clearInterval(durationTimer)
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
    if (!analysisContent.value) {
      ElMessage.error('AI 分析失败，请稍后重试')
    }
  })
}

// 重新分析
const resetAnalysis = () => {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
  analyzing.value = false
  analysisContent.value = ''
  analysisComplete.value = false
  clearInterval(durationTimer)
  analysisDuration.value = 0
}

// 组件挂载时
onMounted(() => {
  if (fundCode.value) {
    loadSummary()
  }
})

// 组件卸载时清理
onUnmounted(() => {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
  if (durationTimer) {
    clearInterval(durationTimer)
  }
})
</script>

<style lang="scss" scoped>
.fund-analysis-view {
  .header-section {
    margin-bottom: 24px;
    .header-content {
      display: flex;
      align-items: center;
      gap: 12px;
      h1 { font-size: 1.8rem; margin: 0; }
    }
  }

  // 搜索区
  .search-card {
    .search-area {
      text-align: center;
      padding: 40px 0;
      .search-icon {
        font-size: 3rem;
        margin-bottom: 16px;
        animation: float 3s ease-in-out infinite;
      }
      h2 {
        font-size: 1.5rem;
        font-weight: 700;
        margin-bottom: 8px;
        color: var(--text-primary);
      }
      .search-desc {
        color: var(--text-secondary);
        margin-bottom: 32px;
        font-size: 0.95rem;
      }
      .search-input-group {
        display: flex;
        gap: 12px;
        justify-content: center;
        max-width: 600px;
        margin: 0 auto;
      }
    }
  }

  // 摘要指标卡片
  .summary-section {
    margin-bottom: 20px;
    .metric-card {
      padding: 20px;
      border-radius: 16px;
      display: flex;
      align-items: center;
      gap: 16px;
      transition: all 0.3s ease;
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
      }
      .metric-icon {
        width: 48px;
        height: 48px;
        border-radius: 14px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        flex-shrink: 0;
      }
      .metric-info {
        flex: 1;
        .metric-label {
          font-size: 0.8rem;
          color: var(--text-secondary);
          margin-bottom: 4px;
          font-weight: 500;
        }
        .metric-value {
          font-size: 1.4rem;
          font-weight: 800;
          font-family: 'Inter', monospace;
          &.up { color: #ef4444; }
          &.down { color: #10b981; }
        }
        .metric-change {
          font-size: 0.8rem;
          font-weight: 700;
          font-family: monospace;
          &.up { color: #ef4444; }
          &.down { color: #10b981; }
        }
        .metric-sub {
          font-size: 0.75rem;
          color: var(--text-secondary);
          opacity: 0.7;
        }
      }
    }
  }

  // 图表控制区
  .chart-control-card {
    margin-bottom: 20px;
    .chart-control-row {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
      gap: 24px;
    }
    .mini-chart-area {
      flex: 1;
      .mini-chart-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 16px;
        .mini-chart-title {
          font-size: 1rem;
          font-weight: 700;
          color: var(--text-primary);
        }
      }
      .mini-sparkline-row {
        display: flex;
        align-items: flex-end;
        gap: 6px;
        height: 80px;
        padding-bottom: 20px;
        position: relative;
        .spark-bar-wrapper {
          display: flex;
          flex-direction: column;
          align-items: center;
          flex: 1;
          .spark-bar {
            width: 100%;
            max-width: 32px;
            border-radius: 4px 4px 0 0;
            transition: height 0.5s ease;
            &.up { background: linear-gradient(180deg, #ef4444, rgba(239, 68, 68, 0.3)); }
            &.down { background: linear-gradient(180deg, #10b981, rgba(16, 185, 129, 0.3)); }
          }
          .spark-label {
            font-size: 0.6rem;
            color: var(--text-secondary);
            margin-top: 4px;
            transform: rotate(-30deg);
            white-space: nowrap;
          }
        }
      }
    }
    .control-area {
      display: flex;
      gap: 12px;
      align-items: center;
      flex-shrink: 0;
    }
  }

  // AI 分析面板
  .analysis-card {
    margin-bottom: 20px;
    .analysis-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      .analysis-title {
        display: flex;
        align-items: center;
        gap: 10px;
        font-size: 1.1rem;
        font-weight: 700;
        .ai-badge {
          display: inline-flex;
          align-items: center;
          gap: 6px;
          background: linear-gradient(135deg, #6366f1, #8b5cf6);
          color: #fff;
          padding: 4px 12px;
          border-radius: 20px;
          font-size: 0.8rem;
          font-weight: 800;
          .ai-dot {
            width: 6px;
            height: 6px;
            background: #fff;
            border-radius: 50%;
            &.active {
              animation: pulse 1s infinite;
            }
          }
        }
      }
    }

    // 加载动画
    .analysis-loading {
      text-align: center;
      padding: 60px 0;
      .loading-brain {
        position: relative;
        display: inline-block;
        margin-bottom: 24px;
        .brain-icon {
          font-size: 3rem;
          position: relative;
          z-index: 1;
          animation: float 2s ease-in-out infinite;
        }
        .brain-pulse {
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          width: 80px;
          height: 80px;
          background: radial-gradient(circle, rgba(99, 102, 241, 0.3), transparent);
          border-radius: 50%;
          animation: pulseGrow 2s ease-in-out infinite;
        }
      }
      .loading-text {
        font-size: 1.1rem;
        font-weight: 600;
        color: var(--text-primary);
        margin-bottom: 8px;
      }
      .loading-hint {
        font-size: 0.85rem;
        color: var(--text-secondary);
      }
    }

    // 分析内容
    .analysis-body {
      position: relative;
      .markdown-content {
        line-height: 1.9;
        color: var(--text-primary);
        font-size: 0.95rem;

        :deep(h1) {
          font-size: 1.5rem;
          font-weight: 800;
          margin: 28px 0 16px;
          color: var(--text-primary);
          border-bottom: 2px solid rgba(99, 102, 241, 0.2);
          padding-bottom: 8px;
        }
        :deep(h2) {
          font-size: 1.25rem;
          font-weight: 700;
          margin: 24px 0 12px;
          color: var(--text-primary);
          display: flex;
          align-items: center;
          gap: 8px;
        }
        :deep(h3) {
          font-size: 1.1rem;
          font-weight: 700;
          margin: 20px 0 10px;
          color: var(--el-color-primary);
        }
        :deep(strong) {
          color: var(--el-color-primary);
          font-weight: 700;
        }
        :deep(ul) {
          padding-left: 20px;
          margin: 8px 0;
        }
        :deep(li) {
          margin: 6px 0;
          list-style: none;
          position: relative;
          padding-left: 16px;
          &::before {
            content: '▸';
            position: absolute;
            left: 0;
            color: var(--el-color-primary);
            font-weight: bold;
          }
        }
        :deep(code) {
          background: rgba(99, 102, 241, 0.1);
          color: var(--el-color-primary);
          padding: 2px 6px;
          border-radius: 4px;
          font-size: 0.85em;
          font-family: 'Inter', monospace;
        }
        :deep(hr) {
          border: none;
          border-top: 1px solid rgba(0, 0, 0, 0.06);
          margin: 24px 0;
        }
        :deep(p) {
          margin: 4px 0;
        }
      }

      .typing-cursor {
        display: inline-block;
        width: 2px;
        height: 18px;
        background: var(--el-color-primary);
        margin-left: 2px;
        animation: blink 0.8s infinite;
        vertical-align: text-bottom;
      }
    }

    // 空状态
    .analysis-empty {
      text-align: center;
      padding: 60px 0;
      .empty-icon {
        font-size: 3rem;
        margin-bottom: 16px;
        opacity: 0.5;
      }
      h3 {
        font-size: 1.2rem;
        font-weight: 700;
        color: var(--text-primary);
        margin-bottom: 8px;
      }
      p {
        color: var(--text-secondary);
        margin-bottom: 24px;
        font-size: 0.9rem;
      }
    }

    // 完成标记
    .analysis-complete-badge {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      margin-top: 24px;
      padding: 12px;
      background: rgba(16, 185, 129, 0.08);
      border-radius: 12px;
      color: #10b981;
      font-weight: 600;
      font-size: 0.9rem;
    }
  }

  // 风险提示
  .disclaimer {
    margin-bottom: 20px;
  }
}

// 动画
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

@keyframes pulseGrow {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.5; }
  50% { transform: translate(-50%, -50%) scale(1.5); opacity: 0; }
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}
</style>
