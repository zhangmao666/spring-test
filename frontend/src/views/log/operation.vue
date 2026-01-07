<template>
  <div class="log-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-header__info">
        <h2 class="page-header__title">操作日志</h2>
        <p class="page-header__desc">查看系统操作记录和审计信息</p>
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
          v-model="searchForm.title" 
          placeholder="搜索模块名称..." 
          clearable
          class="search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-input 
          v-model="searchForm.operatorName" 
          placeholder="搜索操作人..." 
          clearable
          class="operator-input"
        />
        <el-select v-model="searchForm.businessType" placeholder="业务类型" clearable class="type-select">
          <el-option label="其它" :value="0" />
          <el-option label="新增" :value="1" />
          <el-option label="修改" :value="2" />
          <el-option label="删除" :value="3" />
          <el-option label="查询" :value="4" />
          <el-option label="导出" :value="5" />
          <el-option label="导入" :value="6" />
        </el-select>
        <el-select v-model="searchForm.status" placeholder="操作状态" clearable class="status-select">
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
        <el-table-column prop="title" label="模块" width="120" />
        <el-table-column prop="businessType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="getBusinessTypeTag(row.businessType)" size="small">
              {{ getBusinessTypeText(row.businessType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="requestMethod" label="请求方式" width="90">
          <template #default="{ row }">
            <el-tag :type="getMethodTag(row.requestMethod)" size="small" effect="plain">
              {{ row.requestMethod }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestUrl" label="请求URL" min-width="180" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP地址" width="130" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时" width="90">
          <template #default="{ row }">
            <span :class="['cost-time', getCostTimeClass(row.costTime)]">
              {{ row.costTime }}ms
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="operationTime" label="操作时间" width="170">
          <template #default="{ row }">
            <div class="cell-time">
              <el-icon><Clock /></el-icon>
              <span>{{ row.operationTime }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-tooltip content="详情" placement="top">
              <el-button type="primary" link @click="handleDetail(row)">
                <el-icon><View /></el-icon>
              </el-button>
            </el-tooltip>
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

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="操作日志详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="模块名称">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">
          <el-tag :type="getBusinessTypeTag(detailData.businessType)" size="small">
            {{ getBusinessTypeText(detailData.businessType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作人员">{{ detailData.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="操作状态">
          <el-tag :type="detailData.status === 1 ? 'success' : 'danger'" size="small">
            {{ detailData.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ detailData.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detailData.costTime }}ms</el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">{{ detailData.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ detailData.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ detailData.operationTime }}</el-descriptions-item>
        <el-descriptions-item label="方法名称" :span="2">
          <code class="method-name">{{ detailData.method }}</code>
        </el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre class="json-content">{{ formatJson(detailData.requestParam) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="返回结果" :span="2" v-if="detailData.jsonResult">
          <pre class="json-content">{{ formatJson(detailData.jsonResult) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="detailData.errorMsg">
          <span class="error-msg">{{ detailData.errorMsg }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOperationLogList, getOperationLogDetail, deleteOperationLog, batchDeleteOperationLog, clearOperationLog } from '@/api/log'

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

const getBusinessTypeText = (type) => businessTypeMap[type] || '其它'

const getBusinessTypeTag = (type) => {
  const tags = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger', 4: '', 5: 'warning', 6: 'success' }
  return tags[type] || 'info'
}

const getMethodTag = (method) => {
  const tags = { GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger' }
  return tags[method] || 'info'
}

const getCostTimeClass = (time) => {
  if (time < 100) return 'fast'
  if (time < 500) return 'normal'
  return 'slow'
}

const formatJson = (str) => {
  if (!str) return ''
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

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
    
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startTime = searchForm.dateRange[0]
      params.endTime = searchForm.dateRange[1]
    }
    
    const res = await getOperationLogList(params)
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error('加载操作日志失败:', error)
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
  selectedIds.value = selection.map(item => item.id)
}

const handleDetail = async (row) => {
  try {
    const res = await getOperationLogDetail(row.id)
    detailData.value = res.data || row
    detailVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    detailData.value = row
    detailVisible.value = true
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条操作日志吗？', '提示', { type: 'warning' })
    await deleteOperationLog(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') console.error('删除失败:', error)
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条日志吗？`, '提示', { type: 'warning' })
    await batchDeleteOperationLog(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    loadData()
  } catch (error) {
    if (error !== 'cancel') console.error('批量删除失败:', error)
  }
}

const handleClear = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有操作日志吗？此操作不可恢复！', '警告', { 
      type: 'warning',
      confirmButtonText: '确定清空',
      confirmButtonClass: 'el-button--danger'
    })
    await clearOperationLog()
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
$warning: #f59e0b;
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

  .search-input, .operator-input {
    width: 160px;
  }

  .type-select, .status-select {
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

.cost-time {
  font-weight: 500;
  
  &.fast {
    color: $success;
  }
  
  &.normal {
    color: $warning;
  }
  
  &.slow {
    color: $danger;
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

.method-name {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  background: #f1f5f9;
  padding: 4px 8px;
  border-radius: 4px;
  word-break: break-all;
}

.json-content {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  background: #f8fafc;
  padding: 12px;
  border-radius: 6px;
  margin: 0;
  max-height: 200px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
}

.error-msg {
  color: $danger;
}
</style>
