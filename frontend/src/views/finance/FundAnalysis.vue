<template>
  <div class="page-container fund-analysis-view">

    <!-- 顶部 Header -->
    <div class="header-section">
      <el-page-header @back="goBack">
        <template #content>
          <div class="header-content">
            <h1 class="text-gradient">AI 走势分析</h1>
            <el-tag v-if="fundCode" type="primary" effect="dark" round>{{ fundCode }}</el-tag>
            <el-tag v-if="summary?.fundName" effect="plain" size="small" style="max-width:200px;overflow:hidden;text-overflow:ellipsis">
              {{ summary.fundName }}
            </el-tag>
          </div>
        </template>
      </el-page-header>
    </div>

    <!-- 搜索入口（无基金代码时显示）-->
    <el-card class="search-card glass-container" v-if="!fundCode">
      <div class="search-area">
        <div class="search-icon">📈</div>
        <h2>输入基金代码开始分析</h2>
        <p class="search-desc">AI 将基于历史净值数据，为您生成深度走势分析报告</p>
        <div class="search-row">
          <el-input
            v-model="inputCode"
            placeholder="例如：005827"
            size="large"
            clearable
            @keyup.enter="startAnalysis"
            style="max-width: 280px"
          >
            <template #prefix><el-icon><TrendCharts /></el-icon></template>
          </el-input>
          <el-select v-model="selectedDays" size="large" style="width: 130px">
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

      <!-- 4 个指标卡片 -->
      <el-row :gutter="16" class="metric-row" v-loading="loadingSummary">
        <el-col :xs="12" :sm="6">
          <div class="metric-card glass-container">
            <div class="metric-icon" style="background: linear-gradient(135deg,#6366f1,#8b5cf6)">
              <el-icon :size="20"><TrendCharts /></el-icon>
            </div>
            <div class="metric-body">
              <div class="metric-label">最新净值</div>
              <div class="metric-value">{{ fmtVal(summary?.trendSummary?.latestNetValue, 4) }}</div>
              <div class="metric-change" :class="pctClass(summary?.trendSummary?.latestChange)">
                {{ fmtPct(summary?.trendSummary?.latestChange) }}
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="metric-card glass-container">
            <div class="metric-icon" style="background: linear-gradient(135deg,#10b981,#059669)">
              <el-icon :size="20"><DataLine /></el-icon>
            </div>
            <div class="metric-body">
              <div class="metric-label">区间收益</div>
              <div class="metric-value" :class="pctClass(summary?.trendSummary?.totalReturn)">
                {{ fmtPct(summary?.trendSummary?.totalReturn) }}
              </div>
              <div class="metric-sub">{{ selectedDays }} 天</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="metric-card glass-container">
            <div class="metric-icon" style="background: linear-gradient(135deg,#f59e0b,#d97706)">
              <el-icon :size="20"><Warning /></el-icon>
            </div>
            <div class="metric-body">
              <div class="metric-label">波动率</div>
              <div class="metric-value">{{ fmtVal(summary?.trendSummary?.volatility, 2) }}%</div>
              <div class="metric-sub">{{ summary?.trendSummary?.dataPoints || 0 }} 个交易日</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="metric-card glass-container">
            <div class="metric-icon" style="background: linear-gradient(135deg,#ef4444,#dc2626)">
              <el-icon :size="20"><Histogram /></el-icon>
            </div>
            <div class="metric-body">
              <div class="metric-label">净值区间</div>
              <div class="metric-value range-val">
                {{ fmtVal(summary?.trendSummary?.minValue, 2) }}
                <span class="range-sep">~</span>
                {{ fmtVal(summary?.trendSummary?.maxValue, 2) }}
              </div>
              <div class="metric-sub">日均 {{ fmtPct(summary?.trendSummary?.avgChange, 4) }}</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 迷你走势 + 操作栏 -->
      <el-card class="control-card glass-container">
        <div class="control-row">
          <!-- 迷你柱状图 -->
          <div class="sparkline-area" v-if="summary?.trendSummary?.recentPoints?.length">
            <div class="sparkline-title">
              近期走势
              <span class="sparkline-hint">（{{ summary.trendSummary.recentPoints.length }} 个交易日）</span>
            </div>
            <div class="sparkline-bars">
              <div
                v-for="(pt, i) in summary.trendSummary.recentPoints"
                :key="i"
                class="bar-item"
                :title="`${pt.date}  ${pt.changePercent >= 0 ? '+' : ''}${pt.changePercent?.toFixed(2)}%`"
              >
                <div
                  class="bar"
                  :class="pt.changePercent >= 0 ? 'up' : 'down'"
                  :style="{ height: barHeight(pt.changePercent) + 'px' }"
                ></div>
              </div>
            </div>
          </div>
          <div class="sparkline-area empty" v-else>
            <span class="no-data-hint">暂无走势数据</span>
          </div>

          <!-- 操作区 -->
          <div class="control-actions">
            <el-select v-model="selectedDays" size="default" style="width:120px" @change="refreshSummary">
              <el-option :value="30" label="近1个月" />
              <el-option :value="90" label="近3个月" />
              <el-option :value="180" label="近半年" />
              <el-option :value="365" label="近1年" />
            </el-select>
            <el-button
              type="primary"
              @click="startAiAnalysis"
              :loading="analyzing"
              :disabled="analyzing"
            >
              <el-icon><MagicStick /></el-icon>
              {{ analyzing ? 'AI 分析中...' : '开始 AI 分析' }}
            </el-button>
            <el-button v-if="analysisContent || analyzing" @click="resetAnalysis" :icon="RefreshRight" plain>
              重置
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- AI 分析面板 -->
      <el-card class="analysis-card glass-container" id="analysis-panel">
        <template #header>
          <div class="analysis-header">
            <div class="analysis-title">
              <span class="ai-badge" :class="{ pulsing: analyzing }">
                <span class="ai-dot"></span>AI
              </span>
              深度走势分析报告
            </div>
            <el-tag v-if="analysisContent && !analyzing" type="success" effect="plain" size="small">
              <el-icon><CircleCheck /></el-icon>
              已完成 · {{ analysisDuration }}s
            </el-tag>
            <el-tag v-else-if="analyzing" type="warning" effect="plain" size="small">
              <el-icon><Loading /></el-icon>
              分析中 {{ analysisDuration }}s
            </el-tag>
          </div>
        </template>

        <!-- 正在分析 -->
        <div v-if="analyzing && !analysisContent" class="analysis-loading">
          <div class="loading-anim">
            <div class="ring r1"></div>
            <div class="ring r2"></div>
            <div class="ring r3"></div>
            <span class="loading-icon">🧠</span>
          </div>
          <p class="loading-text">AI 正在分析 <strong>{{ summary?.fundName || fundCode }}</strong> 的走势数据</p>
          <p class="loading-sub">基于 {{ summary?.trendSummary?.dataPoints || 0 }} 个交易日净值，请稍候...</p>
        </div>

        <!-- 流式输出内容 -->
        <div v-if="analysisContent" class="analysis-body">
          <div class="markdown-content" v-html="renderedMarkdown"></div>
          <span v-if="analyzing" class="typing-cursor"></span>
        </div>

        <!-- 空状态 -->
        <div v-if="!analyzing && !analysisContent" class="analysis-empty">
          <div class="empty-icon">📊</div>
          <p class="empty-title">准备就绪</p>
          <p class="empty-desc">点击「开始 AI 分析」，获取基于 {{ selectedDays }} 天历史数据的深度报告</p>
          <el-button type="primary" @click="startAiAnalysis">
            <el-icon><MagicStick /></el-icon> 立即分析
          </el-button>
        </div>
      </el-card>

      <!-- 风险提示 -->
      <el-alert class="disclaimer" type="warning" :closable="false">
        <template #title>
          ⚠️ 本页分析由 AI 自动生成，仅供学习参考，不构成任何投资建议。投资有风险，入市需谨慎。
        </template>
      </el-alert>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  TrendCharts, MagicStick, DataLine, Warning, Histogram,
  RefreshRight, CircleCheck, Loading
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

// ── 基础状态 ──────────────────────────────────────────────────────
const fundCode = ref(route.params.fundCode || '')
const inputCode = ref('')
const selectedDays = ref(90)
const loadingSummary = ref(false)
const analyzing = ref(false)
const analysisContent = ref('')
const summary = ref(null)

// 分析计时
const analysisStartTime = ref(0)
const analysisDuration = ref(0)
let durationTimer = null
let eventSource = null

// ── 格式化 ────────────────────────────────────────────────────────
const fmtVal = (v, d = 2) => (v == null ? '--' : Number(v).toFixed(d))
const fmtPct = (v, d = 2) => (v == null ? '--' : (v >= 0 ? '+' : '') + Number(v).toFixed(d) + '%')
const pctClass = (v) => (v == null ? '' : v >= 0 ? 'up' : 'down')
const barHeight = (v) => Math.min(Math.max(Math.abs(v || 0) * 14, 3), 56)

// ── Markdown 渲染 ─────────────────────────────────────────────────
const renderedMarkdown = computed(() => renderMarkdown(analysisContent.value))

function renderMarkdown(text) {
  if (!text) return ''
  let html = text
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/^### (.+)$/gm, '<h3>$1</h3>')
    .replace(/^## (.+)$/gm, '<h2>$1</h2>')
    .replace(/^# (.+)$/gm, '<h1>$1</h1>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    .replace(/^- (.+)$/gm, '<li>$1</li>')
    .replace(/^(\d+)\. (.+)$/gm, '<li>$2</li>')
    .replace(/`(.+?)`/g, '<code>$1</code>')
    .replace(/^---$/gm, '<hr/>')
    .replace(/\n\n/g, '</p><p>')
    .replace(/\n/g, '<br/>')
  html = html.replace(/((<li>.*?<\/li>\s*)+)/g, '<ul>$1</ul>')
  return `<p>${html}</p>`
}

// ── 搜索入口 ──────────────────────────────────────────────────────
const startAnalysis = () => {
  if (!inputCode.value.trim()) { ElMessage.warning('请输入基金代码'); return }
  fundCode.value = inputCode.value.trim()
  loadSummary()
}

// ── 加载摘要 ──────────────────────────────────────────────────────
const loadSummary = async () => {
  loadingSummary.value = true
  analysisContent.value = ''
  try {
    const res = await fetch(`/api/fund/analysis/summary/${fundCode.value}?days=${selectedDays.value}`).then(r => r.json())
    if (res.code === 200) {
      summary.value = res.data
      if (!res.data?.trendSummary) ElMessage.warning('暂无历史数据，正在尝试获取...')
    } else {
      ElMessage.error(res.message || '获取摘要失败')
    }
  } catch {
    ElMessage.error('无法连接到服务器')
  } finally {
    loadingSummary.value = false
  }
}

const refreshSummary = () => { if (fundCode.value) loadSummary() }

// ── AI 流式分析 ───────────────────────────────────────────────────
const startAiAnalysis = () => {
  if (!fundCode.value || analyzing.value) return
  analysisContent.value = ''
  analyzing.value = true
  analysisStartTime.value = Date.now()
  analysisDuration.value = 0

  durationTimer = setInterval(() => {
    analysisDuration.value = ((Date.now() - analysisStartTime.value) / 1000).toFixed(1)
  }, 100)

  eventSource = new EventSource(`/api/fund/analysis/stream/${fundCode.value}?days=${selectedDays.value}`)

  eventSource.addEventListener('message', (e) => {
    try {
      const data = JSON.parse(e.data)
      if (data.content) {
        analysisContent.value += data.content
        nextTick(() => {
          const el = document.getElementById('analysis-panel')
          el?.scrollIntoView({ behavior: 'smooth', block: 'end' })
        })
      }
    } catch { /* ignore */ }
  })

  eventSource.addEventListener('done', () => {
    finishAnalysis()
  })

  eventSource.addEventListener('error', () => {
    finishAnalysis(true)
  })
}

const finishAnalysis = (isError = false) => {
  analyzing.value = false
  clearInterval(durationTimer)
  analysisDuration.value = ((Date.now() - analysisStartTime.value) / 1000).toFixed(1)
  if (eventSource) { eventSource.close(); eventSource = null }
  if (isError && !analysisContent.value) ElMessage.error('AI 分析失败，请稍后重试')
}

const resetAnalysis = () => {
  if (eventSource) { eventSource.close(); eventSource = null }
  clearInterval(durationTimer)
  analyzing.value = false
  analysisContent.value = ''
  analysisDuration.value = 0
}

const goBack = () => router.back()

// ── 生命周期 ──────────────────────────────────────────────────────
onMounted(() => { if (fundCode.value) loadSummary() })
onUnmounted(() => {
  if (eventSource) { eventSource.close(); eventSource = null }
  clearInterval(durationTimer)
})
</script>

<style lang="scss" scoped>
.fund-analysis-view {
  // Header
  .header-section {
    margin-bottom: 24px;
    .header-content {
      display: flex; align-items: center; gap: 10px;
      h1 { font-size: 1.75rem; margin: 0; }
    }
  }

  // 搜索区
  .search-card {
    .search-area {
      text-align: center;
      padding: 56px 20px;
      .search-icon { font-size: 3.5rem; margin-bottom: 16px; }
      h2 { font-size: 1.5rem; font-weight: 700; margin-bottom: 8px; }
      .search-desc { color: var(--text-secondary); font-size: 0.92rem; margin-bottom: 32px; }
      .search-row {
        display: flex; gap: 12px; justify-content: center; flex-wrap: wrap;
      }
    }
  }

  // 指标卡片组
  .metric-row {
    margin-bottom: 20px;
    .metric-card {
      display: flex; align-items: center; gap: 14px;
      padding: 18px 20px;
      border-radius: 14px;
      transition: transform 0.25s, box-shadow 0.25s;
      cursor: default;
      &:hover { transform: translateY(-3px); box-shadow: 0 10px 24px rgba(0,0,0,0.1); }

      .metric-icon {
        width: 44px; height: 44px; border-radius: 12px; flex-shrink: 0;
        display: flex; align-items: center; justify-content: center; color: #fff;
      }
      .metric-body {
        flex: 1; min-width: 0;
        .metric-label { font-size: 0.78rem; color: var(--text-secondary); margin-bottom: 4px; }
        .metric-value {
          font-size: 1.35rem; font-weight: 800; font-family: 'Inter', monospace;
          white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
          &.up { color: #ef4444; } &.down { color: #10b981; }
        }
        .range-val { font-size: 1rem; }
        .range-sep { color: var(--text-secondary); font-weight: 400; margin: 0 4px; }
        .metric-change { font-size: 0.8rem; font-weight: 700; font-family: monospace; &.up { color:#ef4444; } &.down { color:#10b981; } }
        .metric-sub { font-size: 0.72rem; color: var(--text-secondary); margin-top: 2px; }
      }
    }
  }

  // 控制栏
  .control-card {
    margin-bottom: 20px;
    .control-row {
      display: flex; align-items: flex-end; gap: 24px; flex-wrap: wrap;
    }
    .sparkline-area {
      flex: 1; min-width: 200px;
      &.empty { display: flex; align-items: center; height: 70px; }
      .sparkline-title {
        font-size: 0.85rem; font-weight: 700; color: var(--text-primary); margin-bottom: 10px;
        .sparkline-hint { font-weight: 400; font-size: 0.75rem; color: var(--text-secondary); margin-left: 4px; }
      }
      .sparkline-bars {
        display: flex; align-items: flex-end; gap: 4px; height: 56px;
        .bar-item {
          flex: 1; max-width: 20px;
          display: flex; align-items: flex-end; cursor: pointer;
          &:hover .bar { opacity: 1; }
          .bar {
            width: 100%; border-radius: 3px 3px 0 0; min-height: 3px;
            opacity: 0.75; transition: height 0.4s ease, opacity 0.2s;
            &.up { background: linear-gradient(180deg, #ef4444, rgba(239,68,68,0.25)); }
            &.down { background: linear-gradient(180deg, #10b981, rgba(16,185,129,0.25)); }
          }
        }
      }
      .no-data-hint { color: var(--text-secondary); font-size: 0.85rem; opacity: 0.5; }
    }
    .control-actions { display: flex; gap: 10px; align-items: center; flex-shrink: 0; }
  }

  // AI 分析卡片
  .analysis-card {
    margin-bottom: 20px;
    .analysis-header {
      display: flex; justify-content: space-between; align-items: center;
      .analysis-title {
        display: flex; align-items: center; gap: 10px;
        font-size: 1.05rem; font-weight: 700;
        .ai-badge {
          display: inline-flex; align-items: center; gap: 5px;
          background: linear-gradient(135deg, #6366f1, #8b5cf6);
          color: #fff; padding: 3px 10px; border-radius: 20px;
          font-size: 0.78rem; font-weight: 800;
          .ai-dot {
            width: 6px; height: 6px; border-radius: 50%; background: #fff;
          }
          &.pulsing .ai-dot { animation: pulse-dot 1s infinite; }
        }
      }
    }

    // 加载动画
    .analysis-loading {
      text-align: center; padding: 64px 0;
      .loading-anim {
        position: relative; display: inline-flex; align-items: center; justify-content: center;
        width: 80px; height: 80px; margin-bottom: 24px;
        .loading-icon { font-size: 2rem; position: relative; z-index: 2; }
        .ring {
          position: absolute; border-radius: 50%; border: 2px solid transparent;
          animation: spin 2s linear infinite;
          &.r1 { width: 80px; height: 80px; border-top-color: #6366f1; }
          &.r2 { width: 60px; height: 60px; border-top-color: #8b5cf6; animation-duration: 1.5s; animation-direction: reverse; }
          &.r3 { width: 40px; height: 40px; border-top-color: #a78bfa; animation-duration: 1s; }
        }
      }
      .loading-text { font-size: 1.05rem; font-weight: 600; margin-bottom: 6px; }
      .loading-sub { font-size: 0.82rem; color: var(--text-secondary); }
    }

    // 分析内容
    .analysis-body {
      position: relative;
      .markdown-content {
        line-height: 1.95; color: var(--text-primary); font-size: 0.95rem;
        :deep(h1) { font-size: 1.4rem; font-weight: 800; margin: 24px 0 12px; border-bottom: 2px solid rgba(99,102,241,0.18); padding-bottom: 8px; }
        :deep(h2) { font-size: 1.2rem; font-weight: 700; margin: 20px 0 10px; color: var(--text-primary); }
        :deep(h3) { font-size: 1.05rem; font-weight: 700; margin: 16px 0 8px; color: var(--el-color-primary); }
        :deep(strong) { color: var(--el-color-primary); font-weight: 700; }
        :deep(ul) { padding-left: 0; margin: 8px 0; list-style: none; }
        :deep(li) {
          position: relative; padding-left: 18px; margin: 5px 0;
          &::before { content: '▸'; position: absolute; left: 0; color: var(--el-color-primary); font-weight: 700; }
        }
        :deep(code) { background: rgba(99,102,241,0.1); color: var(--el-color-primary); padding: 1px 5px; border-radius: 4px; font-size: 0.85em; }
        :deep(hr) { border: none; border-top: 1px solid rgba(0,0,0,0.07); margin: 20px 0; }
        :deep(p) { margin: 4px 0; }
      }
      .typing-cursor {
        display: inline-block; width: 2px; height: 17px;
        background: var(--el-color-primary); margin-left: 2px;
        animation: blink 0.8s step-end infinite; vertical-align: text-bottom;
      }
    }

    // 空状态
    .analysis-empty {
      text-align: center; padding: 64px 0;
      .empty-icon { font-size: 3rem; opacity: 0.4; margin-bottom: 14px; }
      .empty-title { font-size: 1.1rem; font-weight: 700; margin-bottom: 6px; }
      .empty-desc { color: var(--text-secondary); font-size: 0.88rem; margin-bottom: 24px; }
    }
  }

  .disclaimer { margin-bottom: 20px; }
}

// 动画
@keyframes spin { to { transform: rotate(360deg); } }
@keyframes pulse-dot { 0%,100% { opacity:1 } 50% { opacity:0.2 } }
@keyframes blink { 0%,100% { opacity:1 } 50% { opacity:0 } }
</style>
