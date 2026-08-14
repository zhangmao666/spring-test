<template>
  <div class="user-page tone-system">
    <PageHero
      title="用户管理"
      subtitle="统一查看账号状态、角色分布和安全操作，避免误改用户权限。"
      eyebrow="USER OPERATIONS"
      tone="system"
      :icon="UserFilled"
    >
      <template #actions>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
      </template>
    </PageHero>

    <section class="stat-strip">
      <StatCard label="用户总数" :value="summary.total" :icon="UserFilled" tone="system" hint="平台注册账号" :delay="0">
        <template #chart><Sparkline :data="trend.total" color="var(--accent-system)" /></template>
      </StatCard>
      <StatCard label="启用中" :value="summary.active" :icon="CircleCheckFilled" tone="tools" hint="当前活跃账号" :delay="80">
        <template #chart><Sparkline :data="trend.active" color="var(--accent-tools)" /></template>
      </StatCard>
      <StatCard label="禁用中" :value="summary.disabled" :icon="CircleClose" tone="news" hint="已停用账号" :delay="160" />
      <StatCard label="管理员" :value="summary.admin" :icon="Star" tone="ai" hint="拥有最高权限" :delay="240" />
    </section>

    <el-card class="filter-card" shadow="never">
      <div class="filter-toolbar">
        <div class="filter-toolbar__fields">
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

          <el-input
            v-model="searchForm.email"
            placeholder="按邮箱搜索"
            clearable
            class="filter-input"
            @keyup.enter="handleSearch"
          />

          <el-select
            v-model="searchForm.status"
            placeholder="用户状态"
            clearable
            class="filter-select"
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </div>

        <div class="filter-toolbar__actions">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
      </div>

<!--      <div class="filter-result">-->
<!--        <span>当前共找到 <strong>{{ pagination.total }}</strong> 位用户</span>-->
<!--        <span>默认按列表结果进行状态管理与安全操作</span>-->
<!--      </div>-->
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" class="user-table" empty-text="暂无符合条件的用户">
        <el-table-column prop="username" label="用户信息" min-width="240">
          <template #default="{ row }">
            <div class="cell-user">
              <el-avatar :size="42" class="cell-user__avatar">
                <el-icon :size="20"><UserFilled /></el-icon>
              </el-avatar>
              <div class="cell-user__info">
                <span class="cell-user__name">{{ row.username }}</span>
                <span class="cell-user__email">{{ row.email || '未填写邮箱' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="phone" label="手机号" min-width="150">
          <template #default="{ row }">
            <span class="cell-phone">{{ row.phone || '未填写' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="role" label="角色" width="140">
          <template #default="{ row }">
            <span :class="['role-tag', `role-tag--${String(row.role || '').toLowerCase()}`]">
              {{ getRoleText(row.role) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="190">
          <template #default="{ row }">
            <div class="status-cell">
              <span :class="['status-badge', row.status === 1 ? 'status-badge--active' : 'status-badge--inactive']">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </span>
              <el-switch
                v-model="row.status"
                :active-value="1"
                :inactive-value="0"
                @change="handleStatusChange(row)"
              />
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="创建时间" min-width="180">
          <template #default="{ row }">
            <div class="cell-time">
              <el-icon><Clock /></el-icon>
              <span>{{ formatTime(row.createdAt) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" min-width="250" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button link type="warning" @click="handleResetPwd(row)">重置密码</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <span class="pagination-tip">每次操作前，请先确认用户角色和状态是否正确。</span>
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="loadData"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱地址" />
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
          <el-input v-model="form.password" type="password" placeholder="请输入初始密码" show-password />
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
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { CircleCheckFilled, CircleClose, Clock, Plus, Search, Star, UserFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createUser,
  deleteUser,
  getUserList,
  resetPassword,
  updateUser,
  updateUserStatus
} from '@/api/user'
import PageHero from '@/components/PageHero.vue'
import StatCard from '@/components/StatCard.vue'
import Sparkline from '@/components/Sparkline.vue'

const trend = {
  total: [12, 15, 14, 18, 22, 24, 28],
  active: [10, 12, 13, 16, 20, 22, 26]
}

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
    { min: 3, max: 20, message: '用户名长度需在 3 到 20 个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }]
}

const summary = computed(() => ({
  total: pagination.total,
  active: tableData.value.filter((item) => item.status === 1).length,
  disabled: tableData.value.filter((item) => item.status === 0).length,
  admin: tableData.value.filter((item) => item.role === 'ADMIN').length
}))

const getRoleText = (role) => {
  const texts = { ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生' }
  return texts[role] || role || '未分配角色'
}

const formatTime = (time) => {
  if (!time) return '未记录'
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return time
  const pad = (value) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
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
    ElMessage.error('加载用户列表失败，请稍后重试。')
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

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增用户'
  Object.assign(form, {
    id: null,
    username: '',
    email: '',
    phone: '',
    role: 'STUDENT',
    password: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  Object.assign(form, { ...row, password: '' })
  dialogVisible.value = true
}

const handleStatusChange = async (row) => {
  const nextStatus = row.status
  const previousStatus = nextStatus === 1 ? 0 : 1

  try {
    await updateUserStatus(row.id, nextStatus)
    ElMessage.success(`用户已${nextStatus === 1 ? '启用' : '禁用'}`)
  } catch (error) {
    row.status = previousStatus
    ElMessage.error('状态更新失败，请稍后重试。')
  }
}

const handleResetPwd = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认要将用户“${row.username}”的密码重置为默认密码吗？此操作会影响该用户的后续登录。`,
      '确认重置密码',
      { type: 'warning', confirmButtonText: '确认重置', cancelButtonText: '取消' }
    )
    await resetPassword(row.id)
    ElMessage.success('密码已重置为默认值：123456')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重置密码失败，请稍后重试。')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认删除用户“${row.username}”吗？删除后该账号将无法继续登录后台。`,
      '确认删除用户',
      { type: 'warning', confirmButtonText: '删除用户', cancelButtonText: '取消' }
    )
    await deleteUser(row.id)
    ElMessage.success('用户删除成功')
    if (tableData.value.length === 1 && pagination.page > 1) {
      pagination.page -= 1
    }
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除用户失败，请稍后重试。')
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (form.id) {
      await updateUser(form.id, form)
      ElMessage.success('用户信息已更新')
    } else {
      await createUser(form)
      ElMessage.success('用户创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    if (error?.message) return
    ElMessage.error('保存失败，请检查表单后重试。')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.user-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-hero {
  display: none;
}

.page-hero__eyebrow,
.page-hero__title,
.page-hero__desc,
.metric-chip,
.metric-chip__label,
.metric-chip__value { display: none; }

.stat-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.metric-strip {
  display: none;
}

.filter-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.filter-toolbar__fields,
.filter-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-toolbar__fields {
  flex: 1;
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

.filter-result {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  color: #64748b;
  font-size: 0.88rem;
}

.filter-result strong {
  color: #0f172a;
}

.cell-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cell-user__avatar {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
  flex-shrink: 0;
}

.cell-user__info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.cell-user__name {
  color: #0f172a;
  font-weight: 700;
}

.cell-user__email,
.cell-phone {
  color: #64748b;
  font-size: 0.88rem;
}

.role-tag,
.status-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 700;
}

.role-tag--admin {
  background: rgba(239, 68, 68, 0.12);
  color: #dc2626;
}

.role-tag--teacher {
  background: rgba(245, 158, 11, 0.12);
  color: #d97706;
}

.role-tag--student {
  background: rgba(37, 99, 235, 0.12);
  color: #2563eb;
}

.status-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-badge--active {
  background: rgba(16, 185, 129, 0.12);
  color: #059669;
}

.status-badge--inactive {
  background: rgba(239, 68, 68, 0.12);
  color: #dc2626;
}

.cell-time {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
  font-size: 0.84rem;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.pagination-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}

.pagination-tip {
  color: #94a3b8;
  font-size: 0.82rem;
}

@media (max-width: 1200px) {
  .stat-strip { grid-template-columns: repeat(2, minmax(0, 1fr)); }

  .filter-toolbar,
  .filter-result,
  .pagination-container {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .stat-strip { grid-template-columns: 1fr; }

  .filter-input,
  .filter-input--wide,
  .filter-select {
    width: 100%;
  }
}
</style>
