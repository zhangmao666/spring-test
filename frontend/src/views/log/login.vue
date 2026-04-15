<template>
  <div class="app-page">
    <section class="app-page__hero">
      <div>
        <span class="app-page__eyebrow">LOGIN AUDIT</span>
        <h2 class="app-page__title">登录日志</h2>
        <p class="app-page__desc">集中查看账号登录记录、失败原因与访问环境，快速定位异常登录行为。</p>
      </div>

      <div class="app-page__actions">
        <el-button type="danger" :disabled="tableData.length === 0" @click="handleClear">
          <el-icon><Delete /></el-icon>
          清空日志
        </el-button>
      </div>
    </section>

    <section class="app-metric-grid metrics-grid--three">
      <article class="app-metric-card">
        <span class="app-metric-card__label">当前页记录</span>
        <strong class="app-metric-card__value">{{ tableData.length }}</strong>
      </article>
      <article class="app-metric-card">
        <span class="app-metric-card__label">登录成功</span>
        <strong class="app-metric-card__value">{{ successCount }}</strong>
      </article>
      <article class="app-metric-card">
        <span class="app-metric-card__label">登录失败</span>
        <strong class="app-metric-card__value">{{ failedCount }}</strong>
      </article>
    </section>

    <el-card class="filter-card" shadow="never">
      <div class="app-toolbar">
        <div class="app-toolbar__fields">
          <el-input
            v-model="searchForm.username"
            placeholder="按用户名搜索"
            clearable
            class="filter-input filter-input--wide"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>

          <el-select v-model="searchForm.status" placeholder="登录状态" clearable class="filter-select">
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
        <span>当前共加载 <strong>{{ pagination.total }}</strong> 条登录日志</span>
        <span>建议优先关注失败记录、重复异常 IP 和短时间频繁登录行为。</span>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table
        :data="tableData"
        v-loading="loading"
        class="log-table"
        empty-text="暂无符合条件的登录日志"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />

        <el-table-column prop="username" label="用户信息" min-width="180">
          <template #default="{ row }">
            <div class="cell-user">
              <el-avatar :size="38" class="cell-user__avatar">
                <el-icon :size="18"><User /></el-icon>
              </el-avatar>
              <div class="cell-user__info">
                <span class="cell-user__name">{{ row.username || '未知用户' }}</span>
                <span class="cell-user__meta">{{ row.ipAddress || '未记录 IP' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="browser" label="浏览器" min-width="140" show-overflow-tooltip />
        <el-table-column prop="os" label="操作系统" min-width="140" show-overflow-tooltip />

        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <span :class="['app-status-pill', row.status === 1 ? 'app-status-pill--success' : 'app-status-pill--danger']">
              {{ row.status === 1 ? '成功' : '失败' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="msg" label="结果说明" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="cell-message">{{ row.msg || '无额外说明' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="loginTime" label="登录时间" min-width="180">
          <template #default="{ row }">
            <div class="cell-time">
              <el-icon><Clock /></el-icon>
              <span>{{ formatTime(row.loginTime) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="app-table-footer">
        <div v-if="selectedIds.length > 0" class="table-batch">
          <el-button type="danger" plain @click="handleBatchDelete">
            批量删除（{{ selectedIds.length }}）
          </el-button>
          <span class="app-inline-note">删除前请确认这些日志不再用于排查登录问题。</span>
        </div>
        <span v-else class="app-inline-note">支持按用户、时间段和结果状态联合筛选登录记录。</span>

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
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Delete, Search, User } from '@element-plus/icons-vue'
import { batchDeleteLoginLog, clearLoginLog, deleteLoginLog, getLoginLogList } from '@/api/log'

const loading = ref(false)
const tableData = ref([])
const selectedIds = ref([])

const searchForm = reactive({
  username: '',
  status: null,
  dateRange: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const successCount = computed(() => tableData.value.filter((item) => item.status === 1).length)
const failedCount = computed(() => tableData.value.filter((item) => item.status === 0).length)

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      username: searchForm.username || undefined,
      status: searchForm.status
    }

    if (searchForm.dateRange?.length === 2) {
      params.startTime = searchForm.dateRange[0]
      params.endTime = searchForm.dateRange[1]
    }

    const res = await getLoginLogList(params)
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载登录日志失败，请稍后重试。')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const resetSearch = () => {
  searchForm.username = ''
  searchForm.status = null
  searchForm.dateRange = null
  pagination.page = 1
  loadData()
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map((item) => item.id)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除用户“${row.username || '未知用户'}”的这条登录日志吗？`, '确认删除', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await deleteLoginLog(row.id)
    ElMessage.success('登录日志已删除')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除登录日志失败。')
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 条登录日志吗？`, '批量删除确认', {
      type: 'warning',
      confirmButtonText: '批量删除',
      cancelButtonText: '取消'
    })
    await batchDeleteLoginLog(selectedIds.value)
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
    await ElMessageBox.confirm('确认清空所有登录日志吗？此操作不可恢复。', '清空登录日志', {
      type: 'warning',
      confirmButtonText: '清空日志',
      cancelButtonText: '取消'
    })
    await clearLoginLog()
    ElMessage.success('登录日志已清空')
    selectedIds.value = []
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空登录日志失败。')
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

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.metrics-grid--three {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.filter-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-input {
  width: 220px;
}

.filter-input--wide {
  width: 280px;
}

.filter-select {
  width: 140px;
}

.filter-date {
  width: 360px;
}

.cell-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cell-user__avatar {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-strong));
  color: #fff;
}

.cell-user__info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.cell-user__name {
  color: var(--text-primary);
  font-weight: 700;
}

.cell-user__meta,
.cell-message {
  color: var(--text-muted);
  font-size: 0.88rem;
}

.cell-time {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-muted);
  font-size: 0.84rem;
}

.table-batch {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

@media (max-width: 1100px) {
  .metrics-grid--three {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .filter-input,
  .filter-input--wide,
  .filter-select,
  .filter-date {
    width: 100%;
  }
}
</style>
