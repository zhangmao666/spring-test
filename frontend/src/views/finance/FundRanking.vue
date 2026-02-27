<template>
  <div class="page-container fund-ranking-view">
    <!-- 1. 顶部宏观指标区 (Dashboard) -->
    <div class="header-section">
      <div class="title-row">
        <div>
          <h1 class="text-gradient">基金投资决策中心</h1>
          <p class="subtitle">智能筛选优质基金，辅助量化决策</p>
        </div>
        <div class="market-status">
          <el-tag :type="isMarketOpen ? 'success' : 'info'" effect="dark" round>
            {{ isMarketOpen ? '交易中' : '已收盘' }}
          </el-tag>
          <span class="time">{{ currentTime }}</span>
        </div>
      </div>
    </div>

    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <div class="stat-card glass-container">
          <div class="label">市场温度</div>
          <div class="value">65° <span class="desc">中性偏热</span></div>
          <el-progress :percentage="65" :color="customColors" :show-text="false" />
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-container">
          <div class="label">全市场平均收益</div>
          <div class="value up">+1.42%</div>
          <div class="trend up"><el-icon><CaretTop /></el-icon> 较昨日上升 0.2%</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-container">
          <div class="label">沪深300估值</div>
          <div class="value">12.4x <span class="desc">低估</span></div>
          <div class="trend down">处历史 15% 分位</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-container">
          <div class="label">热门赛道</div>
          <div class="value">🤖 人工智能</div>
          <div class="trend up">资金流入 +42.5亿</div>
        </div>
      </el-col>
    </el-row>

    <!-- 2. 超级筛选栏 (Filter Bar) -->
    <el-card class="filter-card glass-container mb-4">
      <div class="filter-row">
        <div class="filter-group">
          <span class="filter-label">基金类型:</span>
          <el-radio-group v-model="filters.type" size="small">
            <el-radio-button label="全部" />
            <el-radio-button label="股票型" />
            <el-radio-button label="混合型" />
            <el-radio-button label="债券型" />
            <el-radio-button label="指数型" />
          </el-radio-group>
        </div>
        <div class="flex-grow"></div>
        <el-input v-model="searchQuery" placeholder="搜索代码/名称..." suffix-icon="Search" size="small" style="width: 200px" />
      </div>
    </el-card>

    <!-- 3. 列表区域 (Data Table with Tabs) -->
    <el-card class="table-card glass-container">
      <template #header>
        <div class="card-header">
          <el-tabs v-model="activeTab" class="ranking-tabs">
            <el-tab-pane label="实时行情" name="realtime" />
            <el-tab-pane label="历史业绩" name="performance" />
            <el-tab-pane label="风险指标" name="risk" />
            <el-tab-pane label="基金经理" name="manager" />
          </el-tabs>
          <div class="actions">
            <el-button-group>
              <el-button size="small" :icon="Refresh" @click="fetchRanking">刷新</el-button>
            </el-button-group>
          </div>
        </div>
      </template>

      <el-table :data="filteredData" v-loading="loading" style="width: 100%">
        <el-table-column width="60" align="center">
          <template #default="scope">
             <div class="rank-num">{{ scope.$index + 1 }}</div>
          </template>
        </el-table-column>
        
        <!-- 基础信息 (始终显示) -->
        <el-table-column label="基金名称/代码" min-width="180">
          <template #default="scope">
            <div class="fund-info">
              <div class="name">
                {{ scope.row.fundName }}
                <el-tag v-if="scope.row.starRating >= 5" size="small" type="warning" effect="dark" class="ml-1">5星</el-tag>
              </div>
              <div class="code-row">
                <span class="code">{{ scope.row.fundCode }}</span>
                <span class="type-tag">{{ scope.row.fundType }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <!-- 动态切换的列 -->
        <template v-if="activeTab === 'realtime'">
          <el-table-column prop="netValue" label="最新净值" width="110" sortable>
            <template #default="scope">{{ scope.row.netValue?.toFixed(4) || '--' }}</template>
          </el-table-column>
          <el-table-column label="日涨跌" width="100" sortable :sort-by="(row) => row.changePercent">
            <template #default="scope">
              <span class="trend-text" :class="(scope.row.changePercent || 0) >= 0 ? 'up' : 'down'">
                {{ (scope.row.changePercent || 0) >= 0 ? '+' : '' }}{{ (scope.row.changePercent || 0).toFixed(2) }}%
              </span>
            </template>
          </el-table-column>
          <el-table-column label="近1周" width="100" sortable :sort-by="(row) => row.oneWeekReturn">
            <template #default="scope">
              <span v-if="scope.row.oneWeekReturn != null" :class="scope.row.oneWeekReturn >= 0 ? 'up' : 'down'">
                {{ scope.row.oneWeekReturn >= 0 ? '+' : '' }}{{ scope.row.oneWeekReturn.toFixed(2) }}%
              </span>
              <span v-else class="no-data">--</span>
            </template>
          </el-table-column>
          <el-table-column label="近1月" width="100" sortable :sort-by="(row) => row.oneMonthReturn">
            <template #default="scope">
              <span v-if="scope.row.oneMonthReturn != null" :class="scope.row.oneMonthReturn >= 0 ? 'up' : 'down'">
                {{ scope.row.oneMonthReturn >= 0 ? '+' : '' }}{{ scope.row.oneMonthReturn.toFixed(2) }}%
              </span>
              <span v-else class="no-data">--</span>
            </template>
          </el-table-column>
        </template>

        <template v-if="activeTab === 'performance'">
          <el-table-column label="近3月" width="100" sortable :sort-by="(row) => row.threeMonthReturn">
            <template #default="scope">
              <span v-if="scope.row.threeMonthReturn != null" :class="scope.row.threeMonthReturn >= 0 ? 'up' : 'down'">{{ scope.row.threeMonthReturn.toFixed(2) }}%</span>
              <span v-else class="no-data">--</span>
            </template>
          </el-table-column>
          <el-table-column label="近6月" width="100" sortable :sort-by="(row) => row.sixMonthReturn">
            <template #default="scope">
              <span v-if="scope.row.sixMonthReturn != null" :class="scope.row.sixMonthReturn >= 0 ? 'up' : 'down'">{{ scope.row.sixMonthReturn.toFixed(2) }}%</span>
              <span v-else class="no-data">--</span>
            </template>
          </el-table-column>
          <el-table-column label="近1年" width="100" sortable :sort-by="(row) => row.oneYearReturn">
            <template #default="scope">
              <span v-if="scope.row.oneYearReturn != null" :class="scope.row.oneYearReturn >= 0 ? 'up' : 'down'">
                <strong>{{ scope.row.oneYearReturn.toFixed(2) }}%</strong>
              </span>
              <span v-else class="no-data">--</span>
            </template>
          </el-table-column>
          <el-table-column label="今年来" width="100" sortable :sort-by="(row) => row.ytdReturn">
            <template #default="scope">
              <span v-if="scope.row.ytdReturn != null" :class="scope.row.ytdReturn >= 0 ? 'up' : 'down'">{{ scope.row.ytdReturn.toFixed(2) }}%</span>
              <span v-else class="no-data">--</span>
            </template>
          </el-table-column>
          <el-table-column label="成立来" width="110" sortable :sort-by="(row) => row.sinceInceptionReturn">
            <template #default="scope">
              <span v-if="scope.row.sinceInceptionReturn != null" :class="scope.row.sinceInceptionReturn >= 0 ? 'up' : 'down'">{{ scope.row.sinceInceptionReturn.toFixed(2) }}%</span>
              <span v-else class="no-data">--</span>
            </template>
          </el-table-column>
        </template>

        <template v-if="activeTab === 'risk'">
          <el-table-column label="日涨跌" width="100">
            <template #default="scope">
              <span :class="(scope.row.changePercent || 0) >= 0 ? 'up' : 'down'">
                {{ (scope.row.changePercent || 0) >= 0 ? '+' : '' }}{{ (scope.row.changePercent || 0).toFixed(2) }}%
              </span>
            </template>
          </el-table-column>
          <el-table-column label="近6月波幅" width="110">
            <template #default="scope">
              <span v-if="scope.row.sixMonthReturn != null">
                {{ Math.abs(scope.row.sixMonthReturn).toFixed(2) }}%
              </span>
              <span v-else class="no-data">--</span>
            </template>
          </el-table-column>
          <el-table-column label="最大回撤" width="150">
            <template #default="scope">
              <template v-if="scope.row.maxDrawdown != null">
                <el-progress :percentage="Math.min(Math.abs(scope.row.maxDrawdown), 100)" status="exception" :show-text="false" :stroke-width="12" />
                <span class="drawdown-val">{{ scope.row.maxDrawdown?.toFixed(2) }}%</span>
              </template>
              <span v-else class="no-data">--</span>
            </template>
          </el-table-column>
          <el-table-column label="夏普比率" width="120">
            <template #default="scope">
              <span v-if="scope.row.sharpeRatio != null">{{ scope.row.sharpeRatio.toFixed(2) }}</span>
              <span v-else class="no-data">--</span>
            </template>
          </el-table-column>
        </template>

        <template v-if="activeTab === 'manager'">
          <el-table-column label="基金经理" width="150">
            <template #default="scope">
              <div class="manager-cell" v-if="scope.row.managerName">
                <el-avatar :size="24" icon="UserFilled" />
                <span class="name">{{ scope.row.managerName }}</span>
              </div>
              <span v-else class="no-data">--</span>
            </template>
          </el-table-column>
          <el-table-column label="累计净值" width="110">
            <template #default="scope">{{ scope.row.accumulatedValue?.toFixed(4) || '--' }}</template>
          </el-table-column>
          <el-table-column label="成立来" width="110">
            <template #default="scope">
              <span v-if="scope.row.sinceInceptionReturn != null" :class="scope.row.sinceInceptionReturn >= 0 ? 'up' : 'down'">{{ scope.row.sinceInceptionReturn.toFixed(2) }}%</span>
              <span v-else class="no-data">--</span>
            </template>
          </el-table-column>
        </template>

        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="scope">
            <div style="display: flex; align-items: center; justify-content: center; gap: 4px; white-space: nowrap;">
              <el-button type="primary" size="small" link @click.stop="viewTrend(scope.row)">
                <el-icon><TrendCharts /></el-icon> 走势
              </el-button>
              <el-button type="success" size="small" link @click.stop="goAnalysis(scope.row)" style="margin-left: 0;">
                <el-icon><DataLine /></el-icon> AI分析
              </el-button>
              <el-button type="primary" size="small" link @click.stop="analyzeFund(scope.row)" style="margin-left: 0;">
                <el-icon><MagicStick /></el-icon> AI诊断
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <el-button text @click="triggerUpdate" :loading="updating">
          <el-icon><RefreshRight /></el-icon> 手动同步
        </el-button>
      </div>
    </el-card>

    <!-- AI 诊断抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="'基金深度诊断: ' + currentFund?.fundName"
      direction="rtl"
      size="450px"
      class="ai-drawer"
    >
      <div v-if="analyzing" class="analysis-loading" v-loading="true" element-loading-text="AI 正在深度解析中...">
        <div class="skeleton-box"></div>
      </div>
      <div v-else class="analysis-content">
        <div class="summary-card">
          <h4>AI 核心观点</h4>
          <p>{{ aiConclusion }}</p>
        </div>
        <div class="detail-grids">
          <div class="grid-item">
            <label>收益打分</label>
            <el-rate v-model="scores.return" disabled show-score />
          </div>
          <div class="grid-item">
            <label>风险控制</label>
            <el-rate v-model="scores.risk" disabled show-score />
          </div>
        </div>
        <div class="advice">
          <h5>💡 投资建议</h5>
          <p>{{ aiAdvice }}</p>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  CaretTop, Refresh, RefreshRight, Search, MagicStick, 
  DataLine, TrendCharts, UserFilled
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const currentTime = ref('')
const isMarketOpen = ref(true)
let timer = null

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', { 
    hour12: false, 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit', 
    hour: '2-digit', 
    minute: '2-digit' 
  }).replace(/\//g, '-')
  
  const hour = now.getHours()
  const minute = now.getMinutes()
  const totalMin = hour * 60 + minute
  // 简单模拟A股交易时间：9:30-11:30, 13:00-15:00
  const isOpen = (totalMin >= 570 && totalMin <= 690) || (totalMin >= 780 && totalMin <= 900)
  isMarketOpen.value = isOpen
}

const loading = ref(false)
const rankingData = ref([])
const activeTab = ref('realtime')
const searchQuery = ref('')

// 筛选器状态
const filters = reactive({
  type: '全部'
})

// 仪表盘颜色配置
const customColors = [
  { color: '#f56c6c', percentage: 20 },
  { color: '#e6a23c', percentage: 40 },
  { color: '#5cb87a', percentage: 60 },
  { color: '#1989fa', percentage: 80 },
  { color: '#6f7ad3', percentage: 100 },
]

// 过滤后的数据
const filteredData = computed(() => {
  return rankingData.value.filter(item => {
    const matchSearch = !searchQuery.value ||
      item.fundName.includes(searchQuery.value) ||
      item.fundCode.includes(searchQuery.value)

    const matchType = filters.type === '全部' || item.fundType === filters.type

    return matchSearch && matchType
  })
})

const fetchRanking = async () => {
  loading.value = true
  try {
    const response = await fetch('/api/fund/ranking')
    const res = await response.json()
    if (res.code === 200) {
      rankingData.value = res.data
    } else {
      ElMessage.error(res.message || '获取排行失败')
    }
  } catch (error) {
    ElMessage.error('无法连接到服务器')
  } finally {
    loading.value = false
  }
}

// AI 诊断逻辑
const drawerVisible = ref(false)
const analyzing = ref(false)
const currentFund = ref(null)
const aiConclusion = ref('')
const aiAdvice = ref('')
const scores = reactive({ return: 0, risk: 0 })

const fundRouter = useRouter()

const viewTrend = (row) => {
  // 跳转到走势页面
  fundRouter.push(`/finance/fund-trend/${row.fundCode}`)
}

const goAnalysis = (row) => {
  // 跳转到AI走势分析页面
  fundRouter.push(`/fund/analysis/${row.fundCode}`)
}

const analyzeFund = (row) => {
  currentFund.value = row
  drawerVisible.value = true
  analyzing.value = true

  // 模拟 AI 异步分析过程
  setTimeout(() => {
    analyzing.value = false
    aiConclusion.value = `该基金（${row.fundCode}）近期在${row.sector || '白马股'}赛道表现强势，近1年回撤控制在${Math.abs(row.maxDrawdown || 10).toFixed(1)}%以内，属于稳健进攻型。`
    aiAdvice.value = "建议结合当前市场估值，在此位置分批建仓，长线持有以获取行业成长红利。"
    scores.return = 4.5
    scores.risk = row.maxDrawdown < -20 ? 3.0 : 4.8
  }, 1500)
}

const updating = ref(false)
const triggerUpdate = async () => {
  updating.value = true
  try {
    const response = await fetch('/api/fund/update', { method: 'POST' })
    const res = await response.json()
    if (res.code === 200) {
      ElMessage.success('昨日数据同步指令已下达，后台正在处理')
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('触发同步失败')
  } finally {
    updating.value = false
  }
}

onMounted(() => {
  fetchRanking()
  updateTime()
  timer = setInterval(updateTime, 60000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style lang="scss" scoped>
.fund-ranking-view {
  .header-section {
    margin-bottom: 24px;
    .title-row {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
    }
    h1 { font-size: 2.2rem; margin-bottom: 6px; letter-spacing: -1px; }
    .subtitle { color: var(--text-secondary); font-size: 1rem; opacity: 0.7; }
    .market-status {
      display: flex;
      align-items: center;
      gap: 12px;
      .time { font-size: 0.85rem; color: var(--text-secondary); font-family: monospace; }
    }
  }

  .stat-cards {
    margin-bottom: 24px;
    .stat-card {
      padding: 20px;
      height: 120px;
      display: flex;
      flex-direction: column;
      justify-content: center;
      .label { font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 8px; }
      .value { 
        font-size: 1.6rem; font-weight: 800; margin-bottom: 6px; 
        &.up { color: #10B981; }
        .desc { font-size: 0.8rem; font-weight: 400; opacity: 0.6; margin-left: 4px; }
      }
      .trend {
        font-size: 0.8rem; display: flex; align-items: center; gap: 4px;
        &.up { color: #f56c6c; }
        &.down { color: #10B981; }
      }
    }
  }

  .filter-card {
    .filter-row {
      display: flex;
      align-items: center;
      gap: 20px;
      flex-wrap: wrap;
      .filter-group {
        display: flex;
        align-items: center;
        gap: 10px;
        .filter-label { font-size: 0.85rem; font-weight: 600; color: var(--text-secondary); }
      }
    }
  }

  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: none;
      padding-bottom: 0;
      .ranking-tabs { border-bottom: none; :deep(.el-tabs__header) { margin-bottom: 0; } }
    }
  }

  .fund-info {
    .name { font-weight: 700; color: var(--text-primary); margin-bottom: 4px; display: flex; align-items: center; }
    .code-row {
      display: flex; gap: 8px; font-size: 0.8rem;
      .code { font-family: monospace; color: var(--text-secondary); }
      .type-tag { background: rgba(0,0,0,0.05); padding: 0 4px; border-radius: 2px; color: var(--text-secondary); }
    }
  }

  .trend-text { font-weight: 800; font-family: monospace; &.up { color: #f56c6c; } &.down { color: #10B981; } }

  .mini-sparkline {
    width: 60px; height: 24px;
    &.up { color: #f56c6c; }
    &.down { color: #10B981; }
    .spark-svg { width: 100%; height: 100%; opacity: 0.6; }
  }

  .drawdown-val { font-size: 0.75rem; color: #EF4444; font-weight: 600; margin-top: 4px; display: block; }
  
  .manager-cell { display: flex; align-items: center; gap: 8px; .name { font-weight: 600; } }

  .table-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; }

  // AI Drawer Styles
  .ai-drawer {
    .analysis-content {
      display: flex;
      flex-direction: column;
      gap: 24px;
      .summary-card {
        background: var(--el-color-primary-light-9); padding: 16px; border-radius: 12px;
        h4 { margin: 0 0 8px 0; color: var(--el-color-primary); }
        p { margin: 0; font-size: 0.95rem; line-height: 1.6; }
      }
      .detail-grids {
        display: grid; grid-template-columns: 1fr 1fr; gap: 16px;
        .grid-item { label { font-size: 0.8rem; color: var(--text-secondary); margin-bottom: 4px; display: block; } }
      }
      .advice {
        h5 { margin: 0 0 8px 0; font-size: 1rem; }
        p { margin: 0; color: var(--text-secondary); font-size: 0.9rem; }
      }
    }
  }
}
</style>
