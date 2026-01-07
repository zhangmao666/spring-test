<template>
  <div class="task-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-header__info">
        <h2 class="page-header__title">任务管理</h2>
        <p class="page-header__desc">创建、分配和跟踪任务进度</p>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新建任务
      </el-button>
    </div>

    <!-- 筛选区域 -->
    <el-card class="filter-card">
      <div class="filter-form">
        <el-input 
          v-model="searchForm.title" 
          placeholder="搜索任务标题..." 
          clearable
          class="search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="searchForm.status" placeholder="任务状态" clearable class="status-select">
          <el-option label="待处理" value="PENDING" />
          <el-option label="进行中" value="IN_PROGRESS" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </el-card>

    <!-- 任务列表 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" class="task-table">
        <el-table-column prop="title" label="任务" min-width="240">
          <template #default="{ row }">
            <div class="cell-task">
              <div class="cell-task__main">
                <span class="cell-task__title">{{ row.title }}</span>
                <span class="cell-task__desc">{{ row.description || '暂无描述' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="assignee" label="负责人" width="130">
          <template #default="{ row }">
            <div class="cell-assignee">
              <el-avatar :size="28" class="cell-assignee__avatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <span>{{ row.assignee }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <span :class="['priority-tag', `priority-tag--${row.priority?.toLowerCase()}`]">
              {{ getPriorityText(row.priority) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <span :class="['status-tag', `status-tag--${row.status?.toLowerCase()}`]">
              <span class="status-dot"></span>
              {{ getStatusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="dueDate" label="截止日期" width="120">
          <template #default="{ row }">
            <div class="cell-date">
              <el-icon><Calendar /></el-icon>
              <span>{{ row.dueDate }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-tooltip content="编辑" placement="top">
                <el-button type="primary" link @click="handleEdit(row)">
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip v-if="row.status !== 'COMPLETED'" content="完成" placement="top">
                <el-button type="success" link @click="handleComplete(row)">
                  <el-icon><CircleCheck /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <el-button type="danger" link @click="handleDelete(row)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="任务描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入任务描述" />
        </el-form-item>
        <el-form-item label="负责人" prop="assignee">
          <el-input v-model="form.assignee" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="form.priority" placeholder="请选择优先级">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
            <el-option label="紧急" value="URGENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期" prop="dueDate">
          <el-date-picker 
            v-model="form.dueDate" 
            type="date" 
            placeholder="请选择截止日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTaskList, getMyCreatedTasks, createTask, updateTask, deleteTask, completeTask } from '@/api/task'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增任务')
const formRef = ref(null)
const tableData = ref([])

const searchForm = reactive({
  title: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  title: '',
  description: '',
  assignee: '',
  priority: 'MEDIUM',
  dueDate: ''
})

const rules = {
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }],
  assignee: [{ required: true, message: '请输入负责人', trigger: 'blur' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }]
}

const getPriorityType = (priority) => {
  const types = { 'LOW': 'info', 'MEDIUM': '', 'HIGH': 'warning', 'URGENT': 'danger' }
  return types[priority] || ''
}

const getPriorityText = (priority) => {
  const texts = { 'LOW': '低', 'MEDIUM': '中', 'HIGH': '高', 'URGENT': '紧急' }
  return texts[priority] || priority
}

const getStatusType = (status) => {
  const types = { 'PENDING': 'info', 'IN_PROGRESS': 'warning', 'COMPLETED': 'success', 'CANCELLED': 'danger' }
  return types[status] || ''
}

const getStatusText = (status) => {
  const texts = { 'PENDING': '待处理', 'IN_PROGRESS': '进行中', 'COMPLETED': '已完成', 'CANCELLED': '已取消' }
  return texts[status] || status
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMyCreatedTasks({
      page: pagination.page,
      size: pagination.size,
      title: searchForm.title || undefined,
      status: searchForm.status || undefined
    })
    tableData.value = res.data?.records || res.data?.list || res.data || []
    pagination.total = res.data?.total || tableData.value.length
  } catch (error) {
    console.error('加载任务列表失败:', error)
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
  searchForm.status = ''
  pagination.page = 1
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增任务'
  Object.assign(form, { id: null, title: '', description: '', assignee: '', priority: 'MEDIUM', dueDate: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑任务'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleComplete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要完成此任务吗？', '提示', { type: 'warning' })
    await completeTask(row.id)
    ElMessage.success('任务已完成')
    loadData()
  } catch (error) {
    if (error !== 'cancel') console.error('操作失败:', error)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除此任务吗？', '提示', { type: 'warning' })
    await deleteTask(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') console.error('删除失败:', error)
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (form.id) {
      await updateTask(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createTask(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
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
$info: #3b82f6;

.task-page {
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

  .search-input {
    width: 280px;
  }

  .status-select {
    width: 140px;
  }
}

.task-table {
  :deep(.el-table__header th) {
    background: #f8fafc !important;
    font-weight: 600;
  }
}

.cell-task {
  &__main {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  &__title {
    font-weight: 600;
    color: #1e293b;
  }

  &__desc {
    font-size: 12px;
    color: #94a3b8;
  }
}

.cell-assignee {
  display: flex;
  align-items: center;
  gap: 8px;

  &__avatar {
    background: linear-gradient(135deg, $primary, #818cf8);
    color: #fff;
    font-size: 12px;
  }
}

.cell-date {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
  font-size: 13px;

  .el-icon {
    font-size: 14px;
  }
}

.priority-tag {
  display: inline-flex;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;

  &--low {
    background: rgba(#64748b, 0.1);
    color: #64748b;
  }

  &--medium {
    background: rgba($info, 0.1);
    color: $info;
  }

  &--high {
    background: rgba($warning, 0.1);
    color: $warning;
  }

  &--urgent {
    background: rgba($danger, 0.1);
    color: $danger;
  }
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;

  .status-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
  }

  &--pending {
    background: rgba(#64748b, 0.1);
    color: #64748b;
    .status-dot { background: #64748b; }
  }

  &--in_progress {
    background: rgba($info, 0.1);
    color: $info;
    .status-dot { background: $info; }
  }

  &--completed {
    background: rgba($success, 0.1);
    color: $success;
    .status-dot { background: $success; }
  }

  &--cancelled {
    background: rgba($danger, 0.1);
    color: $danger;
    .status-dot { background: $danger; }
  }
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 4px;

  .el-button {
    padding: 6px;
    
    .el-icon {
      font-size: 16px;
    }
  }
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}
</style>
