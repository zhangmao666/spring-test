<template>
  <div class="log-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-header__info">
        <h2 class="page-header__title">登录日志</h2>
        <p class="page-header__desc">查看系统用户登录记录</p>
      </div>
      <div class="page-header__actions">
        <el-button type="danger" @click="handleClear" :disabled="tableData.length === 0">
          <el-icon><Delete /></el-icon>
          清空日志
        </el-button>
      </div>
    </div>

    <!-- 筛选区域 -->
    <el-card class="filter-card">
      <div class="filter-form">
        <el-input 
          v-model="searchForm.username" 
          placeholder="搜索用户名..." 
          clearable
          class="search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="searchForm.status" placeholder="登录状态" clearable class="status-select">
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
          class="date-picker"
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </el-card>

    <!-- 日志列表 -->
    <el-card class="table-card">
      <el-table 
        :data="tableData" 
        v-loading="loading" 
        class="log-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="username" label="用户名" width="140">
          <template #default="{ row }">
            <div class="cell-user">
              <el-avatar :size="32" class="cell-user__avatar">
                <el-icon :size="16"><User /></el-icon>
              </el-avatar>
              <span class="cell-user__name">{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="登录IP" width="140" />
        <el-table-column prop="browser" label="浏览器" width="100" />
        <el-table-column prop="os" label="操作系统" width="100" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="msg" label="消息" min-width="150" show-overflow-tooltip />
        <el-table-column prop="loginTime" label="登录时间" width="170">
          <template #default="{ row }">
            <div class="cell-time">
              <el-icon><Clock /></el-icon>
              <span>{{ row.loginTime }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-tooltip content="删除" placement="top">
              <el-button type="danger" link @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <div class="batch-actions" v-if="selectedIds.length > 0">
          <el-button type="danger" size="small" @click="handleBatchDelete">
            批量删除 ({{ selectedIds.length }})
          </el-button>
        </div>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getLoginLogList, deleteLoginLog, batchDeleteLoginLog, clearLoginLog } from '@/api/log'

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

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      username: searchForm.username || undefined,
      status: searchForm.status
    }
    
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startTime = searchForm.dateRange[0]
      params.endTime = searchForm.dateRange[1]
    }
    
    const res = await getLoginLogList(params)
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error('加载登录日志失败:', error)
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
  selectedIds.value = selection.map(item => item.id)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条登录日志吗？', '提示', { type: 'warning' })
    await deleteLoginLog(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') console.error('删除失败:', error)
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条日志吗？`, '提示', { type: 'warning' })
    await batchDeleteLoginLog(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    loadData()
  } catch (error) {
    if (error !== 'cancel') console.error('批量删除失败:', error)
  }
}

const handleClear = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有登录日志吗？此操作不可恢复！', '警告', { 
      type: 'warning',
      confirmButtonText: '确定清空',
      confirmButtonClass: 'el-button--danger'
    })
    await clearLoginLog()
    ElMessage.success('清空成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') console.error('清空失败:', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
$primary: #6366f1;
$success: #10b981;
$danger: #ef4444;

.log-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 0;

  &__title {
    font-size: 24px;
    font-weight: 700;
    color: #1e293b;
    margin: 0 0 4px 0;
  }

  &__desc {
    font-size: 14px;
    color: #64748b;
    margin: 0;
  }
}

.filter-card {
  :deep(.el-card__body) {
    padding: 16px 20px !important;
  }
}

.filter-form {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;

  .search-input {
    width: 180px;
  }

  .status-select {
    width: 120px;
  }

  .date-picker {
    width: 360px;
  }
}

.log-table {
  :deep(.el-table__header th) {
    background: #f8fafc !important;
    font-weight: 600;
  }
}

.cell-user {
  display: flex;
  align-items: center;
  gap: 10px;

  &__avatar {
    background: linear-gradient(135deg, $primary, #818cf8);
    color: #fff;
    flex-shrink: 0;
  }

  &__name {
    font-weight: 500;
    color: #1e293b;
  }
}

.cell-time {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
  font-size: 13px;

  .el-icon {
    font-size: 14px;
  }
}

.table-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;

  .batch-actions {
    display: flex;
    gap: 8px;
  }
}
</style>
