<template>
  <div class="user-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-header__info">
        <h2 class="page-header__title">用户管理</h2>
        <p class="page-header__desc">管理系统用户账户和权限配置</p>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增用户
      </el-button>
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
        <el-input 
          v-model="searchForm.email" 
          placeholder="搜索邮箱..." 
          clearable
          class="email-input"
        />
        <el-select v-model="searchForm.status" placeholder="用户状态" clearable class="status-select">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </el-card>

    <!-- 用户列表 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" class="user-table">
        <el-table-column prop="username" label="用户" min-width="200">
          <template #default="{ row }">
            <div class="cell-user">
              <el-avatar :size="40" class="cell-user__avatar">
                <el-icon :size="20"><UserFilled /></el-icon>
              </el-avatar>
              <div class="cell-user__info">
                <span class="cell-user__name">{{ row.username }}</span>
                <span class="cell-user__email">{{ row.email }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140">
          <template #default="{ row }">
            <span class="cell-phone">{{ row.phone || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <span :class="['role-tag', `role-tag--${row.role?.toLowerCase()}`]">
              {{ getRoleText(row.role) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch 
              v-model="row.status" 
              :active-value="1" 
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">
            <div class="cell-time">
              <el-icon><Clock /></el-icon>
              <span>{{ row.createTime }}</span>
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
              <el-tooltip content="重置密码" placement="top">
                <el-button type="warning" link @click="handleResetPwd(row)">
                  <el-icon><Key /></el-icon>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="学生" value="STUDENT" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!form.id" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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
import { getUserList, createUser, updateUser, deleteUser, updateUserStatus, resetPassword } from '@/api/user'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const formRef = ref(null)
const tableData = ref([])

const searchForm = reactive({
  username: '',
  email: '',
  status: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  username: '',
  email: '',
  phone: '',
  role: 'STUDENT',
  password: '',
  status: 1
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const getRoleType = (role) => {
  const types = { 'ADMIN': 'danger', 'TEACHER': 'warning', 'STUDENT': '' }
  return types[role] || ''
}

const getRoleText = (role) => {
  const texts = { 'ADMIN': '管理员', 'TEACHER': '教师', 'STUDENT': '学生' }
  return texts[role] || role
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      page: pagination.page,
      size: pagination.size,
      username: searchForm.username || undefined,
      email: searchForm.email || undefined,
      status: searchForm.status
    })
    tableData.value = res.data?.records || res.data?.list || res.data || []
    pagination.total = res.data?.total || tableData.value.length
  } catch (error) {
    console.error('加载用户列表失败:', error)
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
  searchForm.email = ''
  searchForm.status = null
  pagination.page = 1
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增用户'
  Object.assign(form, { id: null, username: '', email: '', phone: '', role: 'STUDENT', password: '', status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  Object.assign(form, { ...row, password: '' })
  dialogVisible.value = true
}

const handleStatusChange = async (row) => {
  try {
    await updateUserStatus(row.id, row.status)
    const statusText = row.status === 1 ? '启用' : '禁用'
    ElMessage.success(`用户已${statusText}`)
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    console.error('状态更新失败:', error)
  }
}

const handleResetPwd = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要重置用户"${row.username}"的密码吗？`, '提示', { type: 'warning' })
    await resetPassword(row.id)
    ElMessage.success('密码已重置为: 123456')
  } catch (error) {
    if (error !== 'cancel') console.error('重置密码失败:', error)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户"${row.username}"吗？`, '提示', { type: 'warning' })
    await deleteUser(row.id)
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
      await updateUser(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createUser(form)
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

.user-page {
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

  .search-input, .email-input {
    width: 200px;
  }

  .status-select {
    width: 120px;
  }
}

.user-table {
  :deep(.el-table__header th) {
    background: #f8fafc !important;
    font-weight: 600;
  }
}

.cell-user {
  display: flex;
  align-items: center;
  gap: 12px;

  &__avatar {
    background: linear-gradient(135deg, $primary, #818cf8);
    color: #fff;
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

  &__email {
    font-size: 12px;
    color: #94a3b8;
  }
}

.cell-phone {
  color: #64748b;
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

.role-tag {
  display: inline-flex;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;

  &--admin {
    background: rgba($danger, 0.1);
    color: $danger;
  }

  &--teacher {
    background: rgba($warning, 0.1);
    color: $warning;
  }

  &--student {
    background: rgba($primary, 0.1);
    color: $primary;
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
