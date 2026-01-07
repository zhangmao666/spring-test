<template>
  <div class="dict-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-header__info">
        <h2 class="page-header__title">字典管理</h2>
        <p class="page-header__desc">维护系统数据字典和字典项配置</p>
      </div>
    </div>

    <div class="dict-layout">
      <!-- 字典列表 -->
      <el-card class="dict-card">
        <template #header>
          <div class="section-header">
            <div class="section-title">
              <el-icon><Files /></el-icon>
              <span>字典列表</span>
            </div>
            <el-button type="primary" size="small" @click="handleAddDict">
              <el-icon><Plus /></el-icon>新增
            </el-button>
          </div>
        </template>

        <el-table 
          :data="dictList" 
          v-loading="dictLoading" 
          highlight-current-row
          @current-change="handleDictSelect"
          class="dict-table"
        >
          <el-table-column prop="dictCode" label="编码" width="120">
            <template #default="{ row }">
              <el-tag effect="plain" size="small">{{ row.dictCode }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="dictName" label="名称">
            <template #default="{ row }">
              <span class="dict-name">{{ row.dictName }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <span :class="['status-badge', row.status === 1 ? 'status-badge--active' : 'status-badge--inactive']">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click.stop="handleEditDict(row)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button type="danger" link size="small" @click.stop="handleDeleteDict(row)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-container">
          <el-pagination
            v-model:current-page="dictPagination.page"
            v-model:page-size="dictPagination.size"
            :total="dictPagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, prev, pager, next"
            small
            background
            @change="loadDictList"
          />
        </div>
      </el-card>

      <!-- 字典项列表 -->
      <el-card class="item-card">
        <template #header>
          <div class="section-header">
            <div class="section-title">
              <el-icon><List /></el-icon>
              <span>字典项</span>
              <el-tag v-if="currentDict" type="primary" size="small" effect="light" class="current-dict-tag">
                {{ currentDict.dictName }}
              </el-tag>
            </div>
            <el-button 
              type="primary" 
              size="small" 
              :disabled="!currentDict"
              @click="handleAddItem"
            >
              <el-icon><Plus /></el-icon>新增
            </el-button>
          </div>
        </template>

        <div v-if="!currentDict" class="empty-placeholder">
          <el-icon :size="48"><Pointer /></el-icon>
          <p>请先选择左侧的字典</p>
        </div>

        <el-table v-else :data="itemList" v-loading="itemLoading" class="item-table">
          <el-table-column prop="itemValue" label="值" width="120">
            <template #default="{ row }">
              <code class="item-value">{{ row.itemValue }}</code>
            </template>
          </el-table-column>
          <el-table-column prop="itemLabel" label="标签">
            <template #default="{ row }">
              <span class="item-label">{{ row.itemLabel }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="sort" label="排序" width="70" align="center">
            <template #default="{ row }">
              <span class="sort-badge">{{ row.sort }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <span :class="['status-badge', row.status === 1 ? 'status-badge--active' : 'status-badge--inactive']">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleEditItem(row)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button type="danger" link size="small" @click="handleDeleteItem(row)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 字典对话框 -->
    <el-dialog v-model="dictDialogVisible" :title="dictDialogTitle" width="500px">
      <el-form ref="dictFormRef" :model="dictForm" :rules="dictRules" label-width="100px">
        <el-form-item label="字典编码" prop="dictCode">
          <el-input v-model="dictForm.dictCode" placeholder="请输入字典编码" />
        </el-form-item>
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="dictForm.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dictForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="dictForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dictDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDictSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 字典项对话框 -->
    <el-dialog v-model="itemDialogVisible" :title="itemDialogTitle" width="500px">
      <el-form ref="itemFormRef" :model="itemForm" :rules="itemRules" label-width="100px">
        <el-form-item label="字典值" prop="itemValue">
          <el-input v-model="itemForm.itemValue" placeholder="请输入字典值" />
        </el-form-item>
        <el-form-item label="字典标签" prop="itemLabel">
          <el-input v-model="itemForm.itemLabel" placeholder="请输入字典标签" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="itemForm.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="itemForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleItemSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getDictList, 
  createDict, 
  updateDict, 
  deleteDict,
  getDictItems,
  createDictItem,
  updateDictItem,
  deleteDictItem
} from '@/api/dict'

const dictLoading = ref(false)
const itemLoading = ref(false)
const dictList = ref([])
const itemList = ref([])
const currentDict = ref(null)

const dictDialogVisible = ref(false)
const dictDialogTitle = ref('新增字典')
const dictFormRef = ref(null)

const itemDialogVisible = ref(false)
const itemDialogTitle = ref('新增字典项')
const itemFormRef = ref(null)

const dictPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const dictForm = reactive({
  id: null,
  dictCode: '',
  dictName: '',
  status: 1,
  remark: ''
})

const itemForm = reactive({
  id: null,
  dictId: null,
  itemValue: '',
  itemLabel: '',
  sort: 0,
  status: 1
})

const dictRules = {
  dictCode: [{ required: true, message: '请输入字典编码', trigger: 'blur' }],
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }]
}

const itemRules = {
  itemValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }],
  itemLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }]
}

const loadDictList = async () => {
  dictLoading.value = true
  try {
    const res = await getDictList({
      page: dictPagination.page - 1,
      size: dictPagination.size
    })
    dictList.value = res.data?.records || []
    dictPagination.total = res.data?.total || 0
  } catch (error) {
    console.error('加载字典列表失败:', error)
  } finally {
    dictLoading.value = false
  }
}

const loadDictItems = async (dictId) => {
  if (!dictId) return
  itemLoading.value = true
  try {
    const res = await getDictItems(dictId)
    itemList.value = res.data || []
  } catch (error) {
    console.error('加载字典项失败:', error)
  } finally {
    itemLoading.value = false
  }
}

const handleDictSelect = (row) => {
  currentDict.value = row
  if (row) {
    loadDictItems(row.id)
  } else {
    itemList.value = []
  }
}

const handleAddDict = () => {
  dictDialogTitle.value = '新增字典'
  Object.assign(dictForm, { id: null, dictCode: '', dictName: '', status: 1, remark: '' })
  dictDialogVisible.value = true
}

const handleEditDict = (row) => {
  dictDialogTitle.value = '编辑字典'
  Object.assign(dictForm, row)
  dictDialogVisible.value = true
}

const handleDeleteDict = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除此字典吗？', '提示', { type: 'warning' })
    await deleteDict(row.id)
    ElMessage.success('删除成功')
    loadDictList()
    if (currentDict.value?.id === row.id) {
      currentDict.value = null
      itemList.value = []
    }
  } catch (error) {
    if (error !== 'cancel') console.error('删除失败:', error)
  }
}

const handleDictSubmit = async () => {
  try {
    await dictFormRef.value.validate()
    if (dictForm.id) {
      await updateDict(dictForm.id, dictForm)
      ElMessage.success('更新成功')
    } else {
      await createDict(dictForm)
      ElMessage.success('创建成功')
    }
    dictDialogVisible.value = false
    loadDictList()
  } catch (error) {
    console.error('提交失败:', error)
  }
}

const handleAddItem = () => {
  itemDialogTitle.value = '新增字典项'
  Object.assign(itemForm, { 
    id: null, 
    dictId: currentDict.value?.id, 
    itemValue: '', 
    itemLabel: '', 
    sort: 0, 
    status: 1 
  })
  itemDialogVisible.value = true
}

const handleEditItem = (row) => {
  itemDialogTitle.value = '编辑字典项'
  Object.assign(itemForm, row)
  itemDialogVisible.value = true
}

const handleDeleteItem = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除此字典项吗？', '提示', { type: 'warning' })
    await deleteDictItem(row.id)
    ElMessage.success('删除成功')
    loadDictItems(currentDict.value?.id)
  } catch (error) {
    if (error !== 'cancel') console.error('删除失败:', error)
  }
}

const handleItemSubmit = async () => {
  try {
    await itemFormRef.value.validate()
    if (itemForm.id) {
      await updateDictItem(itemForm.id, itemForm)
      ElMessage.success('更新成功')
    } else {
      await createDictItem(itemForm)
      ElMessage.success('创建成功')
    }
    itemDialogVisible.value = false
    loadDictItems(currentDict.value?.id)
  } catch (error) {
    console.error('提交失败:', error)
  }
}

onMounted(() => {
  loadDictList()
})
</script>

<style lang="scss" scoped>
$primary: #6366f1;
$success: #10b981;
$danger: #ef4444;

.dict-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
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

.dict-layout {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 20px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
    color: #1e293b;

    .el-icon {
      color: $primary;
    }

    .current-dict-tag {
      margin-left: 4px;
    }
  }
}

.dict-table, .item-table {
  :deep(.el-table__row) {
    cursor: pointer;
    transition: background 0.2s ease;

    &.current-row > td {
      background: rgba($primary, 0.08) !important;
    }
  }
}

.dict-name {
  font-weight: 500;
  color: #1e293b;
}

.item-value {
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: $primary;
}

.item-label {
  font-weight: 500;
  color: #1e293b;
}

.sort-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: #f1f5f9;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;

  &--active {
    background: rgba($success, 0.1);
    color: $success;
  }

  &--inactive {
    background: rgba($danger, 0.1);
    color: $danger;
  }
}

.empty-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #94a3b8;

  .el-icon {
    margin-bottom: 16px;
    opacity: 0.5;
  }

  p {
    margin: 0;
    font-size: 14px;
  }
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
}

@media (max-width: 1000px) {
  .dict-layout {
    grid-template-columns: 1fr;
  }
}
</style>
