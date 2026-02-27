<template>
  <div class="page-container fund-trend-view">
    <!-- 顶部信息区 -->
    <div class="header-section">
      <el-page-header @back="goBack">
        <template #content>
          <div class="header-content">
            <h1 class="text-gradient">{{ fundInfo.fundName || '基金走势分析' }}</h1>
            <el-tag v-if="fundInfo.fundCode" type="primary" effect="dark">{{ fundInfo.fundCode }}</el-tag>
          </div>
        </template>
      </el-page-header>
    </div>

    <!-- 走势图表区 -->
    <el-card class="chart-card glass-container" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>净值走势</span>
          <div class="actions">
            <el-radio-group v-model="selectedDays" size="small" @change="loadTrendData">
              <el-radio-button :label="30">近1月</el-radio-button>
              <el-radio-button :label="90">近3月</el-radio-button>
              <el-radio-button :label="180">近半年</el-radio-button>
              <el-radio-button :label="365">近1年</el-radio-button>
            </el-radio-group>
            <el-button size="small" :icon="Refresh" @click="refreshData">刷新</el-button>
          </div>
        </div>
      </template>

      <div ref="chartContainer" class="chart-container"></div>
    </el-card>

    <!-- AI预测区 -->
    <el-card class="prediction-card glass-container" v-loading="predicting">
      <template #header>
        <div class="card-header" style="display: flex; align-items: center; justify-content: space-between; width: 100%;">
          <span>🤖 AI走势预测</span>
          <el-button size="small" type="primary" @click="loadPrediction">
            <el-icon><MagicStick /></el-icon> 获取AI预测
          </el-button>
        </div>
      </template>

      <div v-if="prediction" class="prediction-content">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="stat-box">
              <div class="label">预测方向</div>
              <div class="value" :class="directionClass">
                {{ directionText }}
                <el-icon v-if="prediction.predictedDirection === 'UP'"><CaretTop /></el-icon>
                <el-icon v-else-if="prediction.predictedDirection === 'DOWN'"><CaretBottom /></el-icon>
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-box">
              <div class="label">预测涨跌</div>
              <div class="value" :class="prediction.predictedChange >= 0 ? 'up' : 'down'">
                {{ prediction.predictedChange >= 0 ? '+' : '' }}{{ prediction.predictedChange?.toFixed(2) }}%
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-box">
              <div class="label">置信度</div>
              <div class="value">
                <el-progress :percentage="prediction.confidence" :color="confidenceColor" />
              </div>
            </div>
          </el-col>
        </el-row>

        <el-divider />

        <div class="analysis-section">
          <h4>📊 趋势分析</h4>
          <p>{{ prediction.trendAnalysis }}</p>
        </div>

        <div class="analysis-section">
          <h4>💡 投资建议</h4>
          <el-alert :type="riskAlertType" :closable="false">
            <template #title>
              <span class="risk-badge">风险等级: {{ riskLevelText }}</span>
            </template>
            {{ prediction.recommendation }}
          </el-alert>
        </div>

        <div class="analysis-section">
          <h4>📝 详细分析</h4>
          <p class="detailed-text">{{ prediction.detailedAnalysis }}</p>
        </div>
      </div>

      <el-empty v-else description="点击上方按钮获取AI预测" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CaretTop, CaretBottom, Refresh, MagicStick } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const route = useRoute()
const router = useRouter()

const fundCode = ref(route.params.fundCode || '')
const fundInfo = reactive({
  fundCode: '',
  fundName: ''
})

const loading = ref(false)
const predicting = ref(false)
const selectedDays = ref(90)
const trendData = ref([])
const prediction = ref(null)
const chartContainer = ref(null)
let chartInstance = null

// 计算属性
const directionText = computed(() => {
  const map = { UP: '看涨', DOWN: '看跌', STABLE: '震荡', UNKNOWN: '未知' }
  return map[prediction.value?.predictedDirection] || '未知'
})

const directionClass = computed(() => {
  const map = { UP: 'up', DOWN: 'down', STABLE: 'stable' }
  return map[prediction.value?.predictedDirection] || ''
})

const riskLevelText = computed(() => {
  const map = { LOW: '低风险', MEDIUM: '中风险', HIGH: '高风险', UNKNOWN: '未知' }
  return map[prediction.value?.riskLevel] || '未知'
})

const riskAlertType = computed(() => {
  const map = { LOW: 'success', MEDIUM: 'warning', HIGH: 'error' }
  return map[prediction.value?.riskLevel] || 'info'
})

const confidenceColor = computed(() => {
  const confidence = prediction.value?.confidence || 0
  if (confidence >= 80) return '#67C23A'
  if (confidence >= 60) return '#E6A23C'
  return '#F56C6C'
})

// 加载走势数据
const loadTrendData = async () => {
  loading.value = true
  try {
    const response = await fetch(`/api/fund/trend/${fundCode.value}?days=${selectedDays.value}`)
    const res = await response.json()
    if (res.code === 200) {
      trendData.value = res.data.trendData || []
      fundInfo.fundCode = res.data.fundCode
      fundInfo.fundName = res.data.fundName
      renderChart()
    } else {
      ElMessage.error(res.message || '获取走势数据失败')
    }
  } catch (error) {
    ElMessage.error('无法连接到服务器')
  } finally {
    loading.value = false
  }
}

// 加载AI预测
const loadPrediction = async () => {
  predicting.value = true
  try {
    const response = await fetch(`/api/fund/predict/${fundCode.value}`)
    const res = await response.json()
    if (res.code === 200) {
      prediction.value = res.data
      ElMessage.success('AI预测完成')
    } else {
      ElMessage.error(res.message || '获取预测失败')
    }
  } catch (error) {
    ElMessage.error('无法连接到服务器')
  } finally {
    predicting.value = false
  }
}

// 刷新数据
const refreshData = async () => {
  loading.value = true
  try {
    const response = await fetch(`/api/fund/refresh/${fundCode.value}?days=${selectedDays.value}`, {
      method: 'POST'
    })
    const res = await response.json()
    if (res.code === 200) {
      ElMessage.success('数据刷新成功')
      await loadTrendData()
    } else {
      ElMessage.error(res.message || '刷新失败')
    }
  } catch (error) {
    ElMessage.error('刷新失败')
  } finally {
    loading.value = false
  }
}

// 渲染图表
const renderChart = () => {
  if (!chartContainer.value || !trendData.value.length) return

  if (!chartInstance) {
    chartInstance = echarts.init(chartContainer.value)
  }

  const dates = trendData.value.map(item => item.date)
  const values = trendData.value.map(item => item.netValue)

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const point = trendData.value[params[0].dataIndex]
        return `
          <div style="padding: 8px;">
            <div style="font-weight: bold; margin-bottom: 4px;">${point.date}</div>
            <div>单位净值: ${point.netValue?.toFixed(4)}</div>
            <div>累计净值: ${point.accumulatedValue?.toFixed(4)}</div>
            <div style="color: ${point.changePercent >= 0 ? '#f56c6c' : '#10B981'}">
              涨跌幅: ${point.changePercent >= 0 ? '+' : ''}${point.changePercent?.toFixed(2)}%
            </div>
          </div>
        `
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false,
      axisLabel: {
        formatter: (value) => {
          const date = new Date(value)
          return `${date.getMonth() + 1}/${date.getDate()}`
        }
      }
    },
    yAxis: {
      type: 'value',
      scale: true,
      axisLabel: {
        formatter: '{value}'
      }
    },
    series: [
      {
        name: '净值',
        type: 'line',
        data: values,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          width: 2,
          color: '#409EFF'
        },
        itemStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        }
      }
    ]
  }

  chartInstance.setOption(option)
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  if (fundCode.value) {
    loadTrendData()
  }

  window.addEventListener('resize', () => {
    chartInstance?.resize()
  })
})

onUnmounted(() => {
  chartInstance?.dispose()
})
</script>

<style lang="scss" scoped>
.fund-trend-view {
  .header-section {
    margin-bottom: 24px;
    .header-content {
      display: flex;
      align-items: center;
      gap: 12px;
      h1 {
        font-size: 1.8rem;
        margin: 0;
      }
    }
  }

  .chart-card {
    margin-bottom: 24px;
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      .actions {
        display: flex;
        gap: 12px;
      }
    }
    .chart-container {
      width: 100%;
      height: 400px;
    }
  }

  .prediction-card {
    .prediction-content {
      .stat-box {
        text-align: center;
        padding: 20px;
        background: var(--el-fill-color-light);
        border-radius: 8px;
        .label {
          font-size: 0.9rem;
          color: var(--text-secondary);
          margin-bottom: 12px;
        }
        .value {
          font-size: 1.8rem;
          font-weight: 800;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
          &.up {
            color: #f56c6c;
          }
          &.down {
            color: #10B981;
          }
          &.stable {
            color: #909399;
          }
        }
      }

      .analysis-section {
        margin-top: 24px;
        h4 {
          margin: 0 0 12px 0;
          font-size: 1.1rem;
        }
        p {
          margin: 0;
          line-height: 1.8;
          color: var(--text-secondary);
        }
        .detailed-text {
          white-space: pre-wrap;
        }
        .risk-badge {
          font-weight: 600;
        }
      }
    }
  }
}
</style>
