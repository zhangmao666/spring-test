<template>
  <div class="page-container fund-trend-view">
    <!-- 顶部 -->
    <div class="header-section">
      <el-page-header @back="goBack">
        <template #content>
          <div class="header-content">
            <h1 class="text-gradient">{{ fundInfo.fundName || '基金走势' }}</h1>
            <el-tag v-if="fundInfo.fundCode" type="primary" effect="dark" round>{{ fundInfo.fundCode }}</el-tag>
          </div>
        </template>
      </el-page-header>
    </div>

    <!-- 走势图表 -->
    <el-card class="chart-card glass-container" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="card-title">净值走势</span>
          <div class="actions">
            <el-radio-group v-model="selectedDays" size="small" @change="loadTrendData">
              <el-radio-button :label="30">近1月</el-radio-button>
              <el-radio-button :label="90">近3月</el-radio-button>
              <el-radio-button :label="180">近半年</el-radio-button>
              <el-radio-button :label="365">近1年</el-radio-button>
            </el-radio-group>
            <el-button size="small" :icon="Refresh" @click="refreshData" :loading="loading">刷新</el-button>
          </div>
        </div>
      </template>

      <div ref="chartContainer" class="chart-container"></div>

      <!-- 空状态 -->
      <el-empty v-if="!loading && !trendData.length" description="暂无走势数据" />
    </el-card>

    <!-- AI 预测 -->
    <el-card class="prediction-card glass-container">
      <template #header>
        <div class="card-header">
          <div class="pred-title">
            <span class="ai-badge">
              <span class="ai-dot" :class="{ pulsing: predicting }"></span> AI
            </span>
            走势预测
          </div>
          <el-button size="small" type="primary" @click="loadPrediction" :loading="predicting">
            <el-icon><MagicStick /></el-icon> 获取预测
          </el-button>
        </div>
      </template>

      <div v-if="prediction" class="prediction-body">
        <!-- 三项核心指标 -->
        <el-row :gutter="16" class="pred-metrics">
          <el-col :span="8">
            <div class="pred-stat">
              <div class="pred-label">预测方向</div>
              <div class="pred-val direction" :class="directionClass">
                {{ directionText }}
                <el-icon v-if="prediction.predictedDirection === 'UP'"><CaretTop /></el-icon>
                <el-icon v-else-if="prediction.predictedDirection === 'DOWN'"><CaretBottom /></el-icon>
                <el-icon v-else><Remove /></el-icon>
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="pred-stat">
              <div class="pred-label">预测涨跌</div>
              <div class="pred-val" :class="prediction.predictedChange >= 0 ? 'up' : 'down'">
                {{ prediction.predictedChange >= 0 ? '+' : '' }}{{ prediction.predictedChange?.toFixed(2) }}%
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="pred-stat">
              <div class="pred-label">置信度</div>
              <div class="pred-val confidence-wrap">
                <el-progress
                  type="circle"
                  :percentage="prediction.confidence"
                  :color="confidenceColor"
                  :width="60"
                  :stroke-width="5"
                />
              </div>
            </div>
          </el-col>
        </el-row>

        <el-divider style="margin: 16px 0" />

        <!-- 分析文字区 -->
        <div class="pred-text-block">
          <div class="pred-section">
            <div class="pred-section-title"><span class="dot blue"></span>趋势分析</div>
            <p>{{ prediction.trendAnalysis }}</p>
          </div>

          <div class="pred-section">
            <div class="pred-section-title"><span class="dot green"></span>投资建议</div>
            <el-alert :type="riskAlertType" :closable="false" class="risk-alert">
              <template #title>
                <span class="risk-level">风险等级：{{ riskLevelText }}</span>
              </template>
              <span>{{ prediction.recommendation }}</span>
            </el-alert>
          </div>

          <div class="pred-section" v-if="prediction.detailedAnalysis">
            <div class="pred-section-title"><span class="dot purple"></span>详细分析</div>
            <p class="detailed-text">{{ prediction.detailedAnalysis }}</p>
          </div>
        </div>
      </div>

      <el-empty v-else description="点击「获取预测」按钮，AI 将分析近期走势趋势" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CaretTop, CaretBottom, Refresh, MagicStick, Remove } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const route = useRoute()
const router = useRouter()

const fundCode = ref(route.params.fundCode || '')
const fundInfo = reactive({ fundCode: '', fundName: '' })
const loading = ref(false)
const predicting = ref(false)
const selectedDays = ref(90)
const trendData = ref([])
const prediction = ref(null)
const chartContainer = ref(null)
let chartInstance = null

// ── 计算属性 ──────────────────────────────────────────────────────
const directionText = computed(() => ({ UP: '看涨', DOWN: '看跌', STABLE: '震荡', UNKNOWN: '待定' }[prediction.value?.predictedDirection] || '待定'))
const directionClass = computed(() => ({ UP: 'up', DOWN: 'down', STABLE: 'stable' }[prediction.value?.predictedDirection] || ''))
const riskLevelText = computed(() => ({ LOW: '低风险', MEDIUM: '中风险', HIGH: '高风险', UNKNOWN: '未知' }[prediction.value?.riskLevel] || '未知'))
const riskAlertType = computed(() => ({ LOW: 'success', MEDIUM: 'warning', HIGH: 'error' }[prediction.value?.riskLevel] || 'info'))
const confidenceColor = computed(() => {
  const c = prediction.value?.confidence || 0
  return c >= 80 ? '#10b981' : c >= 60 ? '#f59e0b' : '#ef4444'
})

// ── 数据加载 ──────────────────────────────────────────────────────
const loadTrendData = async () => {
  loading.value = true
  try {
    const res = await fetch(`/api/fund/trend/${fundCode.value}?days=${selectedDays.value}`).then(r => r.json())
    if (res.code === 200) {
      trendData.value = res.data.trendData || []
      fundInfo.fundCode = res.data.fundCode
      fundInfo.fundName = res.data.fundName
      renderChart()
    } else {
      ElMessage.error(res.message || '获取走势数据失败')
    }
  } catch {
    ElMessage.error('无法连接到服务器')
  } finally {
    loading.value = false
  }
}

const loadPrediction = async () => {
  predicting.value = true
  try {
    const res = await fetch(`/api/fund/predict/${fundCode.value}`).then(r => r.json())
    if (res.code === 200) {
      prediction.value = res.data
      ElMessage.success('AI 预测完成')
    } else {
      ElMessage.error(res.message || '获取预测失败')
    }
  } catch {
    ElMessage.error('无法连接到服务器')
  } finally {
    predicting.value = false
  }
}

const refreshData = async () => {
  loading.value = true
  try {
    const res = await fetch(`/api/fund/refresh/${fundCode.value}?days=${selectedDays.value}`, { method: 'POST' }).then(r => r.json())
    if (res.code === 200) {
      ElMessage.success('数据刷新成功')
      await loadTrendData()
    } else {
      ElMessage.error(res.message || '刷新失败')
    }
  } catch {
    ElMessage.error('刷新失败')
  } finally {
    loading.value = false
  }
}

// ── ECharts 图表 ──────────────────────────────────────────────────
const renderChart = () => {
  if (!chartContainer.value || !trendData.value.length) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartContainer.value)
  }

  const dates = trendData.value.map(d => d.date)
  const values = trendData.value.map(d => d.netValue)
  const first = values[0] || 0
  const last = values[values.length - 1] || 0
  const isUp = last >= first
  const mainColor = isUp ? '#ef4444' : '#10b981'
  const gradStart = isUp ? 'rgba(239,68,68,0.30)' : 'rgba(16,185,129,0.30)'
  const gradEnd   = isUp ? 'rgba(239,68,68,0.02)' : 'rgba(16,185,129,0.02)'

  chartInstance.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(15,15,30,0.88)',
      borderColor: 'rgba(255,255,255,0.08)',
      borderWidth: 1,
      textStyle: { color: '#e2e8f0', fontSize: 12 },
      formatter: (params) => {
        const pt = trendData.value[params[0].dataIndex]
        const chgColor = (pt.changePercent || 0) >= 0 ? '#ef4444' : '#10b981'
        return `
          <div style="padding:6px 2px;line-height:1.8">
            <div style="font-weight:700;margin-bottom:4px;color:#f1f5f9">${pt.date}</div>
            <div>单位净值：<b style="color:#f1f5f9">${pt.netValue?.toFixed(4)}</b></div>
            <div>累计净值：<b style="color:#f1f5f9">${pt.accumulatedValue?.toFixed(4) ?? '--'}</b></div>
            <div>涨跌幅：<b style="color:${chgColor}">${(pt.changePercent || 0) >= 0 ? '+' : ''}${pt.changePercent?.toFixed(2)}%</b></div>
          </div>`
      }
    },
    grid: { left: 16, right: 24, top: 20, bottom: 32, containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false,
      axisLine: { lineStyle: { color: 'rgba(0,0,0,0.1)' } },
      axisTick: { show: false },
      axisLabel: {
        color: '#94a3b8', fontSize: 11,
        formatter: (v) => { const d = new Date(v); return `${d.getMonth() + 1}/${d.getDate()}` }
      },
      splitLine: { show: false }
    },
    yAxis: {
      type: 'value',
      scale: true,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#94a3b8', fontSize: 11, formatter: '{value}' },
      splitLine: { lineStyle: { color: 'rgba(0,0,0,0.06)', type: 'dashed' } }
    },
    series: [{
      name: '净值',
      type: 'line',
      data: values,
      smooth: 0.5,
      symbol: 'none',
      sampling: 'lttb',
      lineStyle: { width: 2.5, color: mainColor },
      itemStyle: { color: mainColor },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: gradStart },
          { offset: 1, color: gradEnd }
        ])
      }
    }]
  }, true)
}

const goBack = () => router.back()

onMounted(() => {
  if (fundCode.value) loadTrendData()
  window.addEventListener('resize', () => chartInstance?.resize())
})

onUnmounted(() => {
  chartInstance?.dispose()
})
</script>

<style lang="scss" scoped>
.fund-trend-view {
  .header-section {
    margin-bottom: 24px;
    .header-content { display: flex; align-items: center; gap: 10px; h1 { font-size: 1.75rem; margin: 0; } }
  }

  // 图表卡片
  .chart-card {
    margin-bottom: 20px;
    .card-header {
      display: flex; justify-content: space-between; align-items: center;
      .card-title { font-size: 1rem; font-weight: 700; }
      .actions { display: flex; gap: 12px; align-items: center; }
    }
    .chart-container { width: 100%; height: 380px; }
  }

  // 预测卡片
  .prediction-card {
    .card-header {
      display: flex; justify-content: space-between; align-items: center;
      .pred-title {
        display: flex; align-items: center; gap: 10px;
        font-size: 1rem; font-weight: 700;
        .ai-badge {
          display: inline-flex; align-items: center; gap: 5px;
          background: linear-gradient(135deg, #6366f1, #8b5cf6);
          color: #fff; padding: 3px 10px; border-radius: 20px;
          font-size: 0.78rem; font-weight: 800;
          .ai-dot {
            width: 6px; height: 6px; border-radius: 50%; background: #fff;
            &.pulsing { animation: pulse-dot 1s infinite; }
          }
        }
      }
    }

    .prediction-body {
      .pred-metrics {
        .pred-stat {
          background: var(--el-fill-color-light);
          border-radius: 12px; padding: 16px;
          text-align: center;
          .pred-label { font-size: 0.8rem; color: var(--text-secondary); margin-bottom: 12px; }
          .pred-val {
            font-size: 1.7rem; font-weight: 800;
            display: flex; align-items: center; justify-content: center; gap: 6px;
            font-family: 'Inter', monospace;
            &.up { color: #ef4444; }
            &.down { color: #10b981; }
            &.stable { color: #94a3b8; }
            &.direction { font-size: 1.5rem; }
            &.confidence-wrap { font-size: 1rem; }
          }
        }
      }

      .pred-text-block { display: flex; flex-direction: column; gap: 20px; }

      .pred-section {
        .pred-section-title {
          display: flex; align-items: center; gap: 8px;
          font-size: 0.9rem; font-weight: 700; margin-bottom: 10px;
          .dot {
            width: 8px; height: 8px; border-radius: 50%;
            &.blue { background: #6366f1; }
            &.green { background: #10b981; }
            &.purple { background: #8b5cf6; }
          }
        }
        p {
          margin: 0; line-height: 1.8; color: var(--text-secondary);
          font-size: 0.9rem;
        }
        .detailed-text { white-space: pre-wrap; }
        .risk-alert { :deep(.el-alert__title) { font-size: 0.85rem; } }
        .risk-level { font-weight: 700; }
      }
    }
  }
}

@keyframes pulse-dot { 0%,100% { opacity:1 } 50% { opacity:0.2 } }
</style>
