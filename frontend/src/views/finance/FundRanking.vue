<template>
  <div class="page-container fund-ranking-view">
    <!-- 顶部标题栏 -->
    <div class="header-section">
      <div class="title-row">
        <div>
          <h1 class="text-gradient">智能基金排行</h1>
          <p class="subtitle">实时行情 · 历史业绩 · 风险指标 · 多维筛选</p>
        </div>
        <div class="market-status">
          <el-tag :type="isMarketOpen ? 'success' : 'info'" effect="dark" round size="small">
            <span class="status-dot" :class="{ active: isMarketOpen }"></span>
            {{ isMarketOpen ? '交易中' : '已收盘' }}
          </el-tag>
          <span class="time">{{ currentTime }}</span>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <el-card class="filter-card glass-container mb-4">
      <div class="filter-row">
        <div class="filter-group">
          <span class="filter-label">基金类型</span>
          <el-radio-group v-model="filters.type" size="small">
            <el-radio-button label="全部" />
            <el-radio-button label="股票型" />
            <el-radio-button label="混合型" />
            <el-radio-button label="债券型" />
            <el-radio-button label="指数型" />
          </el-radio-group>
        </div>
        <div class="filter-right">
          <el-input
            v-model="searchQuery"
            placeholder="搜索代码 / 名称..."
            :prefix-icon="Search"
            size="small"
            clearable
            style="width: 220px"
          />
          <el-button size="small" :icon="Refresh" @click="fetchRanking" :loading="loading">刷新</el-button>
        </div>
      </div>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card glass-container">
      <template #header>
        <div class="card-header">
          <el-tabs v-model="activeTab" class="ranking-tabs">
            <el-tab-pane label="实时行情" name="realtime" />
            <el-tab-pane label="历史业绩" name="performance" />
            <el-tab-pane label="风险指标" name="risk" />
            <el-tab-pane label="基金经理" name="manager" />
          </el-tabs>
          <div class="header-right">
            <span class="data-count">共 {{ filteredData.length }} 只</span>
            <el-button text size="small" @click="triggerUpdate" :loading="updating">
              <el-icon><RefreshRight /></el-icon> 同步数据
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="filteredData"
        v-loading="loading"
        style="width: 100%"
        row-class-name="table-row"
        :empty-text="loading ? '加载中...' : '暂无数据'"
      >
        <!-- 序号 -->
        <el-table-column width="52" align="center">
          <template #default="scope">
            <div class="rank-badge" :class="scope.$index < 3 ? `top-${scope.$index + 1}` : ''">
              {{ scope.$index + 1 }}
            </div>
          </template>
        </el-table-column>

        <!-- 基金名称/代码 (始终显示) -->
        <el-table-column label="基金" min-width="200">
          <template #default="scope">
            <div class="fund-info">
              <div class="fund-name">
                {{ scope.row.fundName }}
                <el-tag v-if="scope.row.starRating >= 5" size="small" type="warning" effect="dark" class="star-tag">5★</el-tag>
              </div>
              <div class="fund-meta">
                <span class="code">{{ scope.row.fundCode }}</span>
                <span class="type-badge">{{ scope.row.fundType }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <!-- 实时行情 Tab -->
        <template v-if="activeTab === 'realtime'">
          <el-table-column prop="netValue" label="最新净值" width="110" sortable>
            <template #default="scope">
              <span class="mono-val">{{ scope.row.netValue?.toFixed(4) || '--' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="日涨跌" width="105" sortable :sort-by="r => r.changePercent">
            <template #default="scope">
              <span class="pct-val" :class="pctClass(scope.row.changePercent)">
                {{ fmtPct(scope.row.changePercent) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="近1周" width="100" sortable :sort-by="r => r.oneWeekReturn">
            <template #default="scope">
              <ReturnCell :value="scope.row.oneWeekReturn" />
            </template>
          </el-table-column>
          <el-table-column label="近1月" width="100" sortable :sort-by="r => r.oneMonthReturn">
            <template #default="scope">
              <ReturnCell :value="scope.row.oneMonthReturn" />
            </template>
          </el-table-column>
        </template>

        <!-- 历史业绩 Tab -->
        <template v-if="activeTab === 'performance'">
          <el-table-column label="近3月" width="100" sortable :sort-by="r => r.threeMonthReturn">
            <template #default="scope"><ReturnCell :value="scope.row.threeMonthReturn" /></template>
          </el-table-column>
          <el-table-column label="近6月" width="100" sortable :sort-by="r => r.sixMonthReturn">
            <template #default="scope"><ReturnCell :value="scope.row.sixMonthReturn" /></template>
          </el-table-column>
          <el-table-column label="近1年" width="100" sortable :sort-by="r => r.oneYearReturn">
            <template #default="scope"><ReturnCell :value="scope.row.oneYearReturn" bold /></template>
          </el-table-column>
          <el-table-column label="今年来" width="100" sortable :sort-by="r => r.ytdReturn">
            <template #default="scope"><ReturnCell :value="scope.row.ytdReturn" /></template>
          </el-table-column>
          <el-table-column label="成立来" width="110" sortable :sort-by="r => r.sinceInceptionReturn">
            <template #default="scope"><ReturnCell :value="scope.row.sinceInceptionReturn" /></template>
          </el-table-column>
        </template>

        <!-- 风险指标 Tab -->
        <template v-if="activeTab === 'risk'">
          <el-table-column label="日涨跌" width="105">
            <template #default="scope">
              <span class="pct-val" :class="pctClass(scope.row.changePercent)">{{ fmtPct(scope.row.changePercent) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="近6月波幅" width="110">
            <template #default="scope">
              <span v-if="scope.row.sixMonthReturn != null" class="mono-val">
                {{ Math.abs(scope.row.sixMonthReturn).toFixed(2) }}%
              </span>
              <span v-else class="no-data">--</span>
            </template>
          </el-table-column>
          <el-table-column label="最大回撤" width="160">
            <template #default="scope">
              <template v-if="scope.row.maxDrawdown != null">
                <el-progress
                  :percentage="Math.min(Math.abs(scope.row.maxDrawdown), 100)"
                  status="exception"
                  :show-text="false"
                  :stroke-width="8"
                  style="margin-bottom: 4px"
                />
                <span class="drawdown-val">{{ scope.row.maxDrawdown?.toFixed(2) }}%</span>
              </template>
              <span v-else class="no-data">--</span>
            </template>
          </el-table-column>
          <el-table-column label="夏普比率" width="110">
            <template #default="scope">
              <span v-if="scope.row.sharpeRatio != null" class="mono-val">{{ scope.row.sharpeRatio.toFixed(2) }}</span>
              <span v-else class="no-data">--</span>
            </template>
          </el-table-column>
        </template>

        <!-- 基金经理 Tab -->
        <template v-if="activeTab === 'manager'">
          <el-table-column label="基金经理" width="160">
            <template #default="scope">
              <div class="manager-cell" v-if="scope.row.managerName">
                <el-avatar :size="28" :style="{ background: 'linear-gradient(135deg,#6366f1,#8b5cf6)', color:'#fff', fontSize:'12px' }">
                  {{ scope.row.managerName?.charAt(0) }}
                </el-avatar>
                <span class="manager-name">{{ scope.row.managerName }}</span>
              </div>
              <span v-else class="no-data">--</span>
            </template>
          </el-table-column>
          <el-table-column label="累计净值" width="110">
            <template #default="scope">
              <span class="mono-val">{{ scope.row.accumulatedValue?.toFixed(4) || '--' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="成立来收益" width="120">
            <template #default="scope"><ReturnCell :value="scope.row.sinceInceptionReturn" /></template>
          </el-table-column>
        </template>

        <!-- 操作列 -->
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <div class="action-btns">
              <el-button type="primary" size="small" link @click.stop="viewTrend(scope.row)">
                <el-icon><TrendCharts /></el-icon> 走势
              </el-button>
              <el-divider direction="vertical" />
              <el-button type="success" size="small" link @click.stop="goAnalysis(scope.row)">
                <el-icon><DataLine /></el-icon> AI分析
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, defineComponent, h } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh, RefreshRight, Search, DataLine, TrendCharts } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// ── 内联子组件：收益单元格 ────────────────────────────────────────
const ReturnCell = defineComponent({
  props: { value: { type: Number, default: null }, bold: Boolean },
  setup(props) {
    return () => {
      if (props.value == null) return h('span', { class: 'no-data' }, '--')
      const cls = ['pct-val', props.value >= 0 ? 'up' : 'down', props.bold ? 'bold' : ''].join(' ')
      const text = (props.value >= 0 ? '+' : '') + props.value.toFixed(2) + '%'
      return h('span', { class: cls }, text)
    }
  }
})

// ── 时钟 ──────────────────────────────────────────────────────────
const currentTime = ref('')
const isMarketOpen = ref(false)
let clockTimer = null

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    hour12: false, year: 'numeric', month: '2-digit',
    day: '2-digit', hour: '2-digit', minute: '2-digit'
  }).replace(/\//g, '-')
  const t = now.getHours() * 60 + now.getMinutes()
  isMarketOpen.value = (t >= 570 && t <= 690) || (t >= 780 && t <= 900)
}

// ── 数据 ──────────────────────────────────────────────────────────
const loading = ref(false)
const updating = ref(false)
const rankingData = ref([])
const activeTab = ref('realtime')
const searchQuery = ref('')
const filters = reactive({ type: '全部' })

const filteredData = computed(() => rankingData.value.filter(item => {
  const q = searchQuery.value.trim()
  const matchSearch = !q || item.fundName?.includes(q) || item.fundCode?.includes(q)
  const matchType = filters.type === '全部' || item.fundType === filters.type
  return matchSearch && matchType
}))

// ── 格式化工具 ────────────────────────────────────────────────────
const pctClass = (v) => (v == null ? '' : v >= 0 ? 'pct-val up' : 'pct-val down')
const fmtPct = (v) => v == null ? '--' : (v >= 0 ? '+' : '') + Number(v).toFixed(2) + '%'

// ── 接口 ──────────────────────────────────────────────────────────
const fetchRanking = async () => {
  loading.value = true
  try {
    const res = await fetch('/api/fund/ranking').then(r => r.json())
    if (res.code === 200) {
      rankingData.value = res.data
    } else {
      ElMessage.error(res.message || '获取排行失败')
    }
  } catch {
    ElMessage.error('无法连接到服务器')
  } finally {
    loading.value = false
  }
}

const triggerUpdate = async () => {
  updating.value = true
  try {
    const res = await fetch('/api/fund/update', { method: 'POST' }).then(r => r.json())
    if (res.code === 200) {
      ElMessage.success('数据同步指令已下达，后台正在处理')
    } else {
      ElMessage.error(res.message || '触发失败')
    }
  } catch {
    ElMessage.error('触发同步失败')
  } finally {
    updating.value = false
  }
}

// ── 跳转 ──────────────────────────────────────────────────────────
const router = useRouter()
const viewTrend = (row) => router.push(`/finance/fund-trend/${row.fundCode}`)
const goAnalysis = (row) => router.push(`/fund/analysis/${row.fundCode}`)

// ── 生命周期 ──────────────────────────────────────────────────────
onMounted(() => {
  fetchRanking()
  updateTime()
  clockTimer = setInterval(updateTime, 60000)
})

onUnmounted(() => {
  if (clockTimer) clearInterval(clockTimer)
})
</script>

<style lang="scss" scoped>
.fund-ranking-view {
  // 标题区
  .header-section {
    margin-bottom: 24px;
    .title-row {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
    }
    h1 { font-size: 2rem; margin-bottom: 6px; letter-spacing: -0.5px; }
    .subtitle { color: var(--text-secondary); font-size: 0.9rem; }
    .market-status {
      display: flex;
      align-items: center;
      gap: 12px;
      .time { font-size: 0.82rem; color: var(--text-secondary); font-family: monospace; }
      .status-dot {
        display: inline-block;
        width: 7px; height: 7px;
        border-radius: 50%;
        background: currentColor;
        margin-right: 4px;
        &.active { animation: blink-dot 1.5s infinite; }
      }
    }
  }

  // 筛选栏
  .filter-card {
    :deep(.el-card__body) { padding: 14px 20px; }
    .filter-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
      flex-wrap: wrap;
      .filter-group {
        display: flex; align-items: center; gap: 10px;
        .filter-label { font-size: 0.83rem; font-weight: 600; color: var(--text-secondary); white-space: nowrap; }
      }
      .filter-right { display: flex; align-items: center; gap: 10px; }
    }
  }

  // 表格卡片头部
  .table-card {
    :deep(.el-card__header) { padding: 0 20px; }
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      .ranking-tabs {
        :deep(.el-tabs__header) { margin-bottom: 0; }
        :deep(.el-tabs__nav-wrap::after) { display: none; }
      }
      .header-right {
        display: flex; align-items: center; gap: 16px;
        .data-count { font-size: 0.82rem; color: var(--text-secondary); }
      }
    }
  }

  // 序号徽标
  .rank-badge {
    width: 24px; height: 24px;
    border-radius: 50%;
    display: flex; align-items: center; justify-content: center;
    font-size: 0.75rem; font-weight: 700;
    background: var(--el-fill-color);
    color: var(--text-secondary);
    margin: 0 auto;
    &.top-1 { background: linear-gradient(135deg, #f59e0b, #d97706); color: #fff; }
    &.top-2 { background: linear-gradient(135deg, #94a3b8, #64748b); color: #fff; }
    &.top-3 { background: linear-gradient(135deg, #cd7c3a, #a0522d); color: #fff; }
  }

  // 基金信息
  .fund-info {
    .fund-name {
      font-weight: 700;
      font-size: 0.9rem;
      color: var(--text-primary);
      margin-bottom: 4px;
      display: flex;
      align-items: center;
      gap: 6px;
      .star-tag { font-size: 0.7rem; padding: 0 5px; }
    }
    .fund-meta {
      display: flex; gap: 8px; font-size: 0.78rem;
      .code { font-family: monospace; color: var(--text-secondary); }
      .type-badge {
        background: rgba(99, 102, 241, 0.1);
        color: #6366f1;
        padding: 0 5px;
        border-radius: 3px;
        font-size: 0.72rem;
        font-weight: 600;
      }
    }
  }

  // 数值样式
  .pct-val {
    font-weight: 700; font-family: monospace; font-size: 0.9rem;
    &.up { color: #ef4444; }
    &.down { color: #10b981; }
    &.bold { font-size: 1rem; }
  }
  .mono-val { font-family: monospace; font-size: 0.9rem; color: var(--text-primary); }
  .no-data { color: var(--text-secondary); font-size: 0.82rem; opacity: 0.5; }
  .drawdown-val { font-size: 0.75rem; color: #ef4444; font-weight: 600; }

  // 基金经理
  .manager-cell {
    display: flex; align-items: center; gap: 10px;
    .manager-name { font-weight: 600; font-size: 0.88rem; }
  }

  // 操作列
  .action-btns {
    display: flex; align-items: center; justify-content: center;
    :deep(.el-divider--vertical) { margin: 0 4px; }
  }
}

@keyframes blink-dot {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
</style>
