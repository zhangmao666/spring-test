<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-header__info">
        <h2 class="page-header__title">课程管理</h2>
        <p class="page-header__desc">管理和维护系统中的所有课程信息</p>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增课程
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <el-card class="filter-card">
      <div class="filter-form">
        <div class="filter-item">
          <el-input 
            v-model="searchForm.keyword" 
            placeholder="搜索课程名称..." 
            clearable
            class="search-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        <div class="filter-actions">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
      </div>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table 
        :data="tableData" 
        v-loading="loading" 
        row-key="id"
        class="modern-table"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="课程名称" min-width="180">
          <template #default="{ row }">
            <div class="cell-course">
              <div class="cell-course__icon">
                <el-icon><Reading /></el-icon>
              </div>
              <div class="cell-course__info">
                <span class="cell-course__name">{{ row.name }}</span>
                <span class="cell-course__desc">{{ row.description || '暂无描述' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="teacherName" label="教师" width="120">
          <template #default="{ row }">
            <div class="cell-teacher">
              <el-avatar :size="28" class="cell-teacher__avatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <span>{{ row.teacherName || '未分配' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag 
              :class="['status-tag', `status-tag--${row.status?.toLowerCase()}`]"
              effect="light"
            >
              <span class="status-dot"></span>
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-tooltip content="编辑" placement="top">
                <el-button type="primary" link @click="handleEdit(row)">
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip v-if="row.status === 'DRAFT'" content="发布" placement="top">
                <el-button type="success" link @click="handlePublish(row)">
                  <el-icon><CircleCheck /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip v-if="row.status === 'PUBLISHED'" content="下架" placement="top">
                <el-button type="warning" link @click="handleUnpublish(row)">
                  <el-icon><RemoveFilled /></el-icon>
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
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="600px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="4"
            placeholder="请输入课程描述" 
          />
        </el-form-item>
        <el-form-item label="教师ID" prop="teacherId">
          <el-input-number v-model="form.teacherId" :min="1" placeholder="请输入教师ID" />
        </el-form-item>
        <el-form-item label="最大人数" prop="maxStudents">
          <el-input-number v-model="form.maxStudents" :min="1" :max="1000" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getCourseList, 
  createCourse, 
  updateCourse, 
  deleteCourse,
  publishCourse,
  unpublishCourse,
  searchCourses
} from '@/api/course'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增课程')
const formRef = ref(null)
const tableData = ref([])

const searchForm = reactive({
  keyword: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  name: '',
  description: '',
  teacherId: null,
  maxStudents: 50
})

const rules = {
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  teacherId: [{ required: true, message: '请选择教师', trigger: 'blur' }]
}

const getStatusType = (status) => {
  const types = {
    'DRAFT': 'info',
    'PUBLISHED': 'success',
    'UNPUBLISHED': 'warning'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    'DRAFT': '草稿',
    'PUBLISHED': '已发布',
    'UNPUBLISHED': '已下架'
  }
  return texts[status] || status
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCourseList()
    tableData.value = res.data || []
    pagination.total = tableData.value.length
  } catch (error) {
    console.error('加载课程列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  if (searchForm.keyword) {
    loading.value = true
    try {
      const res = await searchCourses(searchForm.keyword)
      tableData.value = res.data || []
      pagination.total = tableData.value.length
    } catch (error) {
      console.error('搜索失败:', error)
    } finally {
      loading.value = false
    }
  } else {
    loadData()
  }
}

const resetSearch = () => {
  searchForm.keyword = ''
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增课程'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑课程'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm('确定要发布此课程吗？', '提示', { type: 'warning' })
    await publishCourse(row.id)
    ElMessage.success('发布成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布失败:', error)
    }
  }
}

const handleUnpublish = async (row) => {
  try {
    await ElMessageBox.confirm('确定要下架此课程吗？', '提示', { type: 'warning' })
    await unpublishCourse(row.id)
    ElMessage.success('下架成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('下架失败:', error)
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除此课程吗？', '提示', { type: 'warning' })
    await deleteCourse(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    if (form.id) {
      await updateCourse(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createCourse(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    name: '',
    description: '',
    teacherId: null,
    maxStudents: 50
  })
  formRef.value?.resetFields()
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

.page-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 0;

  &__info {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  &__title {
    font-size: 24px;
    font-weight: 700;
    color: #1e293b;
    margin: 0;
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
  justify-content: space-between;
  gap: 16px;

  .filter-item {
    flex: 1;
    max-width: 400px;
  }

  .search-input {
    :deep(.el-input__wrapper) {
      border-radius: 8px;
    }
  }

  .filter-actions {
    display: flex;
    gap: 8px;
  }
}

.table-card {
  :deep(.el-card__body) {
    padding: 20px !important;
  }
}

.modern-table {
  :deep(.el-table__header th) {
    background: #f8fafc !important;
    font-weight: 600;
  }

  :deep(.el-table__row) {
    transition: background 0.2s ease;

    &:hover > td {
      background: #f8fafc !important;
    }
  }
}

.cell-course {
  display: flex;
  align-items: center;
  gap: 12px;

  &__icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    background: rgba($primary, 0.1);
    color: $primary;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &__info {
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
  }

  &__name {
    font-weight: 600;
    color: #1e293b;
  }

  &__desc {
    font-size: 12px;
    color: #94a3b8;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.cell-teacher {
  display: flex;
  align-items: center;
  gap: 8px;

  &__avatar {
    background: linear-gradient(135deg, $primary, #818cf8);
    color: #fff;
    font-size: 12px;
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

  &--draft {
    background: rgba(#64748b, 0.1);
    color: #64748b;
    .status-dot { background: #64748b; }
  }

  &--published {
    background: rgba($success, 0.1);
    color: $success;
    .status-dot { background: $success; }
  }

  &--unpublished {
    background: rgba($warning, 0.1);
    color: $warning;
    .status-dot { background: $warning; }
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
