<template>
  <div class="app-page">
    <section class="app-page__hero">
      <div>
        <span class="app-page__eyebrow">OPERATION AUDIT</span>
        <h2 class="app-page__title">操作日志</h2>
        <p class="app-page__desc">查看模块操作记录、请求信息与异常结果，辅助排查配置变更和审计风险。</p>
      </div>

      <div class="app-page__actions">
        <el-button type="danger" :disabled="tableData.length === 0" @click="handleClear">
          <el-icon><Delete /></el-icon>
          清空日志
        </el-button>
      </div>
    </section>

    <section class="app-metric-grid metrics-grid--four">
      <article class="app-metric-card">
        <span class="app-metric-card__label">当前页记录</span>
        <strong class="app-metric-card__value">{{ tableData.length }}</strong>
      </article>
      <article class="app-metric-card">
        <span class="app-metric-card__label">成功操作</span>
        <strong class="app-metric-card__value">{{ successCount }}</strong>
      </article>
      <article class="app-metric-card">
        <span class="app-metric-card__label">失败操作</span>
        <strong class="app-metric-card__value">{{ failedCount }}</strong>
      </article>
      <article class="app-metric-card">
        <span class="app-metric-card__label">高耗时请求</span>
        <strong class="app-metric-card__value">{{ slowCount }}</strong>
      </article>
    </section>

    <el-card class="filter-card" shadow="never">
      <div class="app-toolbar">
        <div class="app-toolbar__fields">
          <el-input
            v-model="searchForm.title"
            placeholder="按模块名称搜索"
            clearable
            class="filter-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>

          <el-input
            v-model="searchForm.operatorName"
            placeholder="按操作人搜索"
            clearable
            class="filter-input"
            @keyup.enter="handleSearch"
          />

          <el-select v-model="searchForm.businessType" placeholder="业务类型" clearable class="filter-select">
            <el-option label="其它" :value="0" />
            <el-option label="新增" :value="1" />
            <el-option label="修改" :value="2" />
            <el-option label="删除" :value="3" />
            <el-option label="查询" :value="4" />
            <el-option label="导出" :value="5" />
            <el-option label="导入" :value="6" />
          </el-select>

          <el-select v-model="searchForm.status" placeholder="操作状态" clearable class="filter-select">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>

          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            class="filter-date"
          />
        </div>

        <div class="app-toolbar__actions">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
      </div>

      <div class="app-result-bar">
        <span>当前共加载 <strong>{{ pagination.total }}</strong> 条操作日志</span>
        <span>建议优先查看失败记录和高耗时请求，避免忽略潜在的配置或权限问题。</span>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table
        :data="tableData"
        v-loading="loading"
        class="log-table"
        empty-text="暂无符合条件的操作日志"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" label="模块" min-width="140" />

        <el-table-column prop="businessType" label="类型" width="110">
          <template #default="{ row }">
            <span :class="['app-status-pill', `business-${getBusinessTypeTag(row.businessType)}`]">
              {{ getBusinessTypeText(row.businessType) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="operatorName" label="操作人" min-width="120" />

        <el-table-column prop="requestMethod" label="请求方式" width="110">
          <template #default="{ row }">
            <span class="method-badge">{{ row.requestMethod || 'GET' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="requestUrl" label="请求 URL" min-width="220" show-overflow-tooltip />

        <el-table-column prop="status" label="状态" width="96">
          <template #default="{ row }">
            <span :class="['app-status-pill', row.status === 1 ? 'app-status-pill--success' : 'app-status-pill--danger']">
              {{ row.status === 1 ? '成功' : '失败' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="costTime" label="耗时" width="110">
          <template #default="{ row }">
            <span :class="['cost-time', getCostTimeClass(row.costTime)]">{{ row.costTime }} ms</span>
          </template>
        </el-table-column>

        <el-table-column prop="operationTime" label="操作时间" min-width="180">
          <template #default="{ row }">
            <div class="cell-time">
              <el-icon><Clock /></el-icon>
              <span>{{ formatTime(row.operationTime) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="app-table-footer">
        <div v-if="selectedIds.length > 0" class="table-batch">
          <el-button type="danger" plain @click="handleBatchDelete">
            批量删除（{{ selectedIds.length }}）
          </el-button>
          <span class="app-inline-note">删除前请确认这些日志不再用于审计或问题追踪。</span>
        </div>
        <span v-else class="app-inline-note">详情面板会展示请求参数、返回结果和错误信息，便于快速排查问题。</span>

        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="操作日志详情" width="760px" destroy-on-close>
      <div class="detail-grid">
        <div class="detail-row">
          <span class="detail-label">模块名称</span>
          <strong>{{ detailData.title || '未记录' }}</strong>
        </div>
        <div class="detail-row">
          <span class="detail-label">业务类型</span>
          <span :class="['app-status-pill', `business-${getBusinessTypeTag(detailData.businessType)}`]">
            {{ getBusinessTypeText(detailData.businessType) }}
          </span>
        </div>
        <div class="detail-row">
          <span class="detail-label">操作人员</span>
          <strong>{{ detailData.operatorName || '未记录' }}</strong>
        </div>
        <div class="detail-row">
          <span class="detail-label">操作状态</span>
          <span :class="['app-status-pill', detailData.status === 1 ? 'app-status-pill--success' : 'app-status-pill--danger']">
            {{ detailData.status === 1 ? '成功' : '失败' }}
          </span>
        </div>
        <div class="detail-row">
          <span class="detail-label">请求方式</span>
          <span class="method-badge">{{ detailData.requestMethod || '未记录' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">耗时</span>
          <span :class="['cost-time', getCostTimeClass(detailData.costTime)]">{{ detailData.costTime || 0 }} ms</span>
        </div>
        <div class="detail-row detail-row--full">
          <span class="detail-label">请求 URL</span>
          <code class="detail-code">{{ detailData.requestUrl || '未记录' }}</code>
        </div>
        <div class="detail-row">
          <span class="detail-label">IP 地址</span>
          <strong>{{ detailData.ipAddress || '未记录' }}</strong>
        </div>
        <div class="detail-row">
          <span class="detail-label">操作时间</span>
          <strong>{{ formatTime(detailData.operationTime) }}</strong>
        </div>
        <div class="detail-row detail-row--full">
          <span class="detail-label">方法名称</span>
          <code class="detail-code">{{ detailData.method || '未记录' }}</code>
        </div>
        <div class="detail-block">
          <span class="detail-label">请求参数</span>
          <pre class="json-content">{{ formatJson(detailData.requestParam) }}</pre>
        </div>
        <div v-if="detailData.jsonResult" class="detail-block">
          <span class="detail-label">返回结果</span>
          <pre class="json-content">{{ formatJson(detailData.jsonResult) }}</pre>
        </div>
        <div v-if="detailData.errorMsg" class="detail-block">
          <span class="detail-label">错误信息</span>
          <div class="error-message">{{ detailData.errorMsg }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Delete, Search } from '@element-plus/icons-vue'
import {
  batchDeleteOperationLog,
  clearOperationLog,
  deleteOperationLog,
  getOperationLogDetail,
  getOperationLogList
} from '@/api/log'

const loading = ref(false)
const tableData = ref([])
const selectedIds = ref([])
const detailVisible = ref(false)
const detailData = ref({})

const searchForm = reactive({
  title: '',
  operatorName: '',
  businessType: null,
  status: null,
  dateRange: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const businessTypeMap = {
  0: '其它',
  1: '新增',
  2: '修改',
  3: '删除',
  4: '查询',
  5: '导出',
  6: '导入'
}

const successCount = computed(() => tableData.value.filter((item) => item.status === 1).length)
const failedCount = computed(() => tableData.value.filter((item) => item.status === 0).length)
const slowCount = computed(() => tableData.value.filter((item) => Number(item.costTime) >= 1000).length)

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      title: searchForm.title || undefined,
      operatorName: searchForm.operatorName || undefined,
      businessType: searchForm.businessType,
      status: searchForm.status
    }

    if (searchForm.dateRange?.length === 2) {
      params.startTime = searchForm.dateRange[0]
      params.endTime = searchForm.dateRange[1]
    }

    const res = await getOperationLogList(params)
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载操作日志失败，请稍后重试。')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const resetSearch = () => {
  searchForm.title = ''
  searchForm.operatorName = ''
  searchForm.businessType = null
  searchForm.status = null
  searchForm.dateRange = null
  pagination.page = 1
  loadData()
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map((item) => item.id)
}

const getBusinessTypeText = (type) => businessTypeMap[type] || '其它'

const getBusinessTypeTag = (type) => {
  if (type === 1) return 'success'
  if (type === 2 || type === 4) return 'info'
  if (type === 3) return 'danger'
  if (type === 5 || type === 6) return 'warning'
  return 'neutral'
}

const getCostTimeClass = (costTime) => {
  const value = Number(costTime) || 0
  if (value >= 1000) return 'cost-time--slow'
  if (value >= 300) return 'cost-time--medium'
  return 'cost-time--fast'
}

const handleDetail = async (row) => {
  try {
    const res = await getOperationLogDetail(row.id)
    detailData.value = res.data || {}
    detailVisible.value = true
  } catch (error) {
    ElMessage.error('加载日志详情失败。')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除模块“${row.title || '未命名模块'}”的这条操作日志吗？`, '确认删除', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await deleteOperationLog(row.id)
    ElMessage.success('操作日志已删除')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除操作日志失败。')
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 条操作日志吗？`, '批量删除确认', {
      type: 'warning',
      confirmButtonText: '批量删除',
      cancelButtonText: '取消'
    })
    await batchDeleteOperationLog(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败。')
    }
  }
}

const handleClear = async () => {
  try {
    await ElMessageBox.confirm('确认清空所有操作日志吗？此操作不可恢复。', '清空操作日志', {
      type: 'warning',
      confirmButtonText: '清空日志',
      cancelButtonText: '取消'
    })
    await clearOperationLog()
    ElMessage.success('操作日志已清空')
    selectedIds.value = []
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空操作日志失败。')
    }
  }
}

const formatTime = (time) => {
  if (!time) return '未记录'
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return time
  const pad = (value) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const formatJson = (value) => {
  if (!value) return '无数据'
  try {
    const parsed = typeof value === 'string' ? JSON.parse(value) : value
    return JSON.stringify(parsed, null, 2)
  } catch (error) {
    return String(value)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.metrics-grid--four {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.filter-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-input {
  width: 220px;
}

.filter-select {
  width: 140px;
}

.filter-date {
  width: 360px;
}

.business-success {
  background: var(--color-success-soft);
  color: var(--color-success);
}

.business-info {
  background: var(--color-info-soft);
  color: var(--color-info);
}

.business-warning {
  background: var(--color-warning-soft);
  color: var(--color-warning);
}

.business-danger {
  background: var(--color-danger-soft);
  color: var(--color-danger);
}

.business-neutral {
  background: rgba(100, 116, 139, 0.12);
  color: var(--text-secondary);
}

.method-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 72px;
  padding: 6px 10px;
  border-radius: 999px;
  background: var(--surface-emphasis);
  color: var(--color-primary);
  font-size: 0.78rem;
  font-weight: 700;
}

.cost-time {
  font-weight: 700;
}

.cost-time--fast {
  color: var(--color-success);
}

.cost-time--medium {
  color: var(--color-warning);
}

.cost-time--slow {
  color: var(--color-danger);
}

.cell-time {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-muted);
  font-size: 0.84rem;
}

.row-actions,
.table-batch {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.detail-grid {
  display: grid;
  gap: 16px;
}

.detail-row {
  display: grid;
  grid-template-columns: 120px 1fr;
  align-items: center;
  gap: 14px;
}

.detail-row--full {
  align-items: flex-start;
}

.detail-label {
  color: var(--text-muted);
  font-size: 0.86rem;
  font-weight: 700;
}

.detail-code,
.json-content {
  font-family: "Cascadia Code", "JetBrains Mono", "SFMono-Regular", Consolas, monospace;
}

.detail-code {
  padding: 10px 12px;
  border-radius: 12px;
  background: var(--surface-muted);
  color: var(--text-primary);
  word-break: break-all;
}

.detail-block {
  display: grid;
  gap: 10px;
}

.json-content {
  margin: 0;
  padding: 16px;
  border: 1px solid var(--border-soft);
  border-radius: 16px;
  background: var(--surface-muted);
  color: var(--text-secondary);
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.7;
  max-height: 280px;
  overflow: auto;
}

.error-message {
  padding: 14px 16px;
  border-radius: 16px;
  background: var(--color-danger-soft);
  color: var(--color-danger);
  line-height: 1.7;
}

@media (max-width: 1100px) {
  .metrics-grid--four {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .metrics-grid--four {
    grid-template-columns: 1fr;
  }

  .filter-input,
  .filter-select,
  .filter-date {
    width: 100%;
  }

  .detail-row {
    grid-template-columns: 1fr;
  }
}
</style>
