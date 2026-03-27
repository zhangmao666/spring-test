<template>
  <div class="dict-page">
    <div class="page-header">
      <div class="page-header__info">
        <div class="page-header__eyebrow">DATA DICTIONARY</div>
        <h2 class="page-header__title">字典管理</h2>
        <p class="page-header__desc">
          左侧选择字典进入详情，右侧专注维护该字典下的字典项，避免操作挤在同一行里造成换行和重叠。
        </p>
      </div>
    </div>

    <div class="dict-layout">
      <el-card class="dict-card" shadow="never">
        <div class="section-header">
          <div class="section-title">
            <el-icon><Files /></el-icon>
            <span>字典列表</span>
          </div>
          <el-button type="primary" class="header-action" @click="handleAddDict">
            <el-icon><Plus /></el-icon>
            <span>新增字典</span>
          </el-button>
        </div>

        <div class="dict-list" v-loading="dictLoading">
          <el-empty
            v-if="!dictList.length"
            description="暂无字典配置，可以先新增一个字典"
          />

          <article
            v-for="row in dictList"
            :key="row.id"
            :class="['dict-entry', { 'is-active': currentDict?.id === row.id }]"
            @click="handleDictSelect(row)"
          >
            <div class="dict-entry__main">
              <div class="dict-entry__head">
                <div class="dict-entry__title-row">
                  <span class="dict-entry__title" :title="row.dictName">{{ row.dictName }}</span>
                </div>
                <span :class="['status-badge', row.status === 1 ? 'status-badge--active' : 'status-badge--inactive']">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </span>
              </div>

              <p v-if="row.remark" class="dict-entry__remark" :title="row.remark">{{ row.remark }}</p>

            </div>

            <div class="dict-entry__actions" @click.stop>
              <el-button class="inline-action" size="small" plain @click="handleEditDict(row)">
                <el-icon><Edit /></el-icon>
                <span>编辑字典</span>
              </el-button>
              <el-button class="inline-action inline-action--danger" size="small" plain @click="handleDeleteDict(row)">
                <el-icon><Delete /></el-icon>
                <span>删除</span>
              </el-button>
            </div>
          </article>
        </div>

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

      <el-card class="item-card" shadow="never">
        <div class="section-header section-header--detail">
          <div class="section-title">
            <el-icon><List /></el-icon>
            <span>字典项</span>
          </div>

          <div class="section-actions">
            <el-button class="header-action" plain :disabled="!currentDict" @click="handleEditDict(currentDict)">
              <el-icon><Edit /></el-icon>
              <span>编辑当前字典</span>
            </el-button>
            <el-button type="primary" class="header-action" :disabled="!currentDict" @click="handleAddItem">
              <el-icon><Plus /></el-icon>
              <span>新增字典项</span>
            </el-button>
          </div>
        </div>

        <div v-if="!currentDict" class="empty-placeholder">
          <el-icon :size="48"><Pointer /></el-icon>
          <p>点击左侧字典卡片后，在这里维护对应的字典项</p>
        </div>

        <template v-else>
          <div class="dict-detail">
            <div class="dict-detail__eyebrow">CURRENT DICTIONARY</div>
            <div class="dict-detail__title-row">
              <h3 class="dict-detail__title">{{ currentDict.dictName }}</h3>
              <el-tag type="primary" effect="dark" round>{{ currentDict.dictCode }}</el-tag>
              <span :class="['status-badge', currentDict.status === 1 ? 'status-badge--active' : 'status-badge--inactive']">
                {{ currentDict.status === 1 ? '启用' : '禁用' }}
              </span>
            </div>
            <p class="dict-detail__desc">
              当前正在维护该字典下的字典项。新增、编辑和删除都在这个上下文里进行，避免误操作到其他字典。
            </p>
          </div>

          <el-table :data="itemList" v-loading="itemLoading" class="item-table" empty-text="当前字典暂无字典项">
            <el-table-column prop="itemValue" label="值" width="180" show-overflow-tooltip>
              <template #default="{ row }">
                <code class="item-value" :title="row.itemValue">{{ row.itemValue }}</code>
              </template>
            </el-table-column>

            <el-table-column prop="itemLabel" label="标签" min-width="240" show-overflow-tooltip>
              <template #default="{ row }">
                <span class="item-label" :title="row.itemLabel">{{ row.itemLabel }}</span>
              </template>
            </el-table-column>

            <el-table-column prop="sort" label="排序" width="88" align="center">
              <template #default="{ row }">
                <span class="sort-badge">{{ row.sort }}</span>
              </template>
            </el-table-column>

            <el-table-column prop="status" label="状态" width="96">
              <template #default="{ row }">
                <span :class="['status-badge', row.status === 1 ? 'status-badge--active' : 'status-badge--inactive']">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </span>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="220" align="right">
              <template #default="{ row }">
                <div class="item-actions">
                  <el-button class="inline-action" size="small" plain @click="handleEditItem(row)">
                    <el-icon><Edit /></el-icon>
                    <span>编辑</span>
                  </el-button>
                  <el-button class="inline-action inline-action--danger" size="small" plain @click="handleDeleteItem(row)">
                    <el-icon><Delete /></el-icon>
                    <span>删除</span>
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </template>
      </el-card>
    </div>

    <el-dialog v-model="dictDialogVisible" :title="dictDialogTitle" width="560px" destroy-on-close>
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

    <el-dialog v-model="itemDialogVisible" :title="itemDialogTitle" width="560px" destroy-on-close>
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
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Files, List, Plus, Pointer } from '@element-plus/icons-vue'
import {
  createDict,
  createDictItem,
  deleteDict,
  deleteDictItem,
  getDictItems,
  getDictList,
  updateDict,
  updateDictItem
} from '@/api/dict'

const route = useRoute()
const router = useRouter()

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

const syncRouteDictId = async (dictId) => {
  const query = { ...route.query }
  if (dictId) {
    query.dictId = String(dictId)
  } else {
    delete query.dictId
  }
  await router.replace({ path: route.path, query })
}

const clearCurrentDict = async (syncRoute = true) => {
  currentDict.value = null
  itemList.value = []
  if (syncRoute) {
    await syncRouteDictId(null)
  }
}

const loadDictItems = async (dictId) => {
  if (!dictId) {
    itemList.value = []
    return
  }

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

const syncCurrentDict = async () => {
  const routeDictId = route.query.dictId
  const targetId = routeDictId || currentDict.value?.id

  if (!targetId) {
    await clearCurrentDict(false)
    return
  }

  const matched = dictList.value.find(item => String(item.id) === String(targetId))
  if (!matched) {
    await clearCurrentDict(!!routeDictId)
    return
  }

  currentDict.value = matched
  await loadDictItems(matched.id)
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
    await syncCurrentDict()
  } catch (error) {
    console.error('加载字典列表失败:', error)
  } finally {
    dictLoading.value = false
  }
}

const handleDictSelect = async (row) => {
  currentDict.value = row
  await syncRouteDictId(row.id)
  await loadDictItems(row.id)
}

const handleAddDict = () => {
  dictDialogTitle.value = '新增字典'
  Object.assign(dictForm, { id: null, dictCode: '', dictName: '', status: 1, remark: '' })
  dictDialogVisible.value = true
}

const handleEditDict = (row) => {
  if (!row) return

  dictDialogTitle.value = '编辑字典'
  Object.assign(dictForm, {
    id: row.id,
    dictCode: row.dictCode,
    dictName: row.dictName,
    status: row.status,
    remark: row.remark || ''
  })
  dictDialogVisible.value = true
}

const handleDeleteDict = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除字典“${row.dictName}”吗？`, '提示', { type: 'warning' })
    await deleteDict(row.id)
    ElMessage.success('删除成功')

    if (currentDict.value?.id === row.id) {
      await clearCurrentDict()
    }

    await loadDictList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const handleDictSubmit = async () => {
  try {
    await dictFormRef.value?.validate()
    if (dictForm.id) {
      await updateDict(dictForm.id, dictForm)
      ElMessage.success('更新成功')
    } else {
      await createDict(dictForm)
      ElMessage.success('创建成功')
    }

    dictDialogVisible.value = false
    await loadDictList()
  } catch (error) {
    console.error('提交失败:', error)
  }
}

const handleAddItem = () => {
  if (!currentDict.value) return

  itemDialogTitle.value = '新增字典项'
  Object.assign(itemForm, {
    id: null,
    dictId: currentDict.value.id,
    itemValue: '',
    itemLabel: '',
    sort: 0,
    status: 1
  })
  itemDialogVisible.value = true
}

const handleEditItem = (row) => {
  itemDialogTitle.value = '编辑字典项'
  Object.assign(itemForm, {
    id: row.id,
    dictId: row.dictId,
    itemValue: row.itemValue,
    itemLabel: row.itemLabel,
    sort: row.sort,
    status: row.status
  })
  itemDialogVisible.value = true
}

const handleDeleteItem = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除字典项“${row.itemLabel}”吗？`, '提示', { type: 'warning' })
    await deleteDictItem(row.id)
    ElMessage.success('删除成功')
    await loadDictItems(currentDict.value?.id)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const handleItemSubmit = async () => {
  try {
    await itemFormRef.value?.validate()
    if (itemForm.id) {
      await updateDictItem(itemForm.id, itemForm)
      ElMessage.success('更新成功')
    } else {
      await createDictItem(itemForm)
      ElMessage.success('创建成功')
    }

    itemDialogVisible.value = false
    await loadDictItems(currentDict.value?.id)
  } catch (error) {
    console.error('提交失败:', error)
  }
}

onMounted(async () => {
  await loadDictList()
})
</script>

<style lang="scss" scoped>
.dict-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.page-header {
  padding: 6px 2px 2px;
}

.page-header__eyebrow {
  color: #94a3b8;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.18em;
}

.page-header__title {
  margin: 8px 0 0;
  color: #0f172a;
  font-size: 34px;
  font-weight: 800;
  line-height: 1.05;
  letter-spacing: -0.04em;
}

.page-header__desc {
  margin: 12px 0 0;
  max-width: 860px;
  color: #64748b;
  font-size: 16px;
  line-height: 1.75;
}

.dict-layout {
  display: grid;
  grid-template-columns: 420px minmax(0, 1fr);
  gap: 20px;
}

.dict-card,
.item-card {
  border-radius: 28px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.14);
}

.section-header--detail {
  flex-wrap: wrap;
}

.section-title,
.section-actions,
.header-action,
.inline-action,
.dict-entry__title-row,
.dict-entry__actions,
.item-actions,
.dict-detail__title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  white-space: nowrap;
}

.section-title {
  min-width: 0;
  color: #0f172a;
  font-size: 20px;
  font-weight: 800;
}

.section-title .el-icon {
  color: #4f46e5;
}

.section-actions {
  flex-wrap: wrap;
}

.dict-list {
  display: grid;
  gap: 14px;
}

.dict-entry {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 18px;
  border-radius: 22px;
  border: 1px solid rgba(148, 163, 184, 0.16);
  background:
    radial-gradient(circle at top right, rgba(96, 165, 250, 0.1), transparent 32%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.96));
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.06);
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.dict-entry:hover {
  transform: translateY(-2px);
  box-shadow: 0 18px 30px rgba(15, 23, 42, 0.1);
}

.dict-entry.is-active {
  border-color: rgba(79, 70, 229, 0.36);
  box-shadow: 0 20px 34px rgba(79, 70, 229, 0.14);
  background:
    radial-gradient(circle at top right, rgba(99, 102, 241, 0.14), transparent 36%),
    linear-gradient(180deg, rgba(238, 242, 255, 0.82), rgba(255, 255, 255, 0.98));
}

.dict-entry__main {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.dict-entry__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.dict-entry__title-row {
  min-width: 0;
  flex: 1;
}

.dict-entry__title {
  min-width: 0;
  overflow: hidden;
  color: #0f172a;
  font-size: 20px;
  font-weight: 800;
  text-overflow: ellipsis;
}

.dict-entry__remark {
  margin: 0;
  overflow: hidden;
  color: #64748b;
  font-size: 14px;
  line-height: 1.6;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dict-entry__actions {
  justify-content: flex-end;
  flex-wrap: nowrap;
}

.dict-detail {
  margin-bottom: 16px;
  padding: 22px 24px;
  border-radius: 22px;
  border: 1px solid rgba(148, 163, 184, 0.14);
  background:
    radial-gradient(circle at top right, rgba(59, 130, 246, 0.12), transparent 32%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.96));
}

.dict-detail__eyebrow {
  color: #94a3b8;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.16em;
}

.dict-detail__title-row {
  margin-top: 10px;
  gap: 12px;
  flex-wrap: wrap;
}

.dict-detail__title {
  margin: 0;
  color: #0f172a;
  font-size: 28px;
  font-weight: 800;
  line-height: 1.1;
}

.dict-detail__desc {
  margin: 12px 0 0;
  color: #64748b;
  font-size: 14px;
  line-height: 1.7;
}

.item-table {
  width: 100%;
}

.item-value,
.item-label {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
}

.item-value {
  padding: 4px 10px;
  border-radius: 999px;
  background: #eef2ff;
  color: #4f46e5;
  font-size: 13px;
}

.item-label {
  color: #0f172a;
  font-size: 15px;
  font-weight: 600;
}

.sort-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 34px;
  height: 30px;
  padding: 0 10px;
  border-radius: 10px;
  background: #f1f5f9;
  color: #475569;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 56px;
  padding: 5px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
  white-space: nowrap;
}

.status-badge--active {
  background: rgba(16, 185, 129, 0.12);
  color: #059669;
}

.status-badge--inactive {
  background: rgba(239, 68, 68, 0.12);
  color: #dc2626;
}

.header-action,
.inline-action {
  white-space: nowrap;
}

.header-action :deep(span),
.inline-action :deep(span),
.header-action :deep(.el-icon),
.inline-action :deep(.el-icon) {
  white-space: nowrap;
}

.inline-action {
  height: 34px;
  padding: 0 14px !important;
  border-radius: 999px !important;
}

.inline-action--danger {
  border-color: rgba(239, 68, 68, 0.18) !important;
  color: #dc2626 !important;
}

.inline-action--danger:hover {
  background: rgba(239, 68, 68, 0.06) !important;
}

.item-actions {
  justify-content: flex-end;
  flex-wrap: nowrap;
}

.empty-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 420px;
  padding: 40px 20px;
  color: #94a3b8;
  text-align: center;
}

.empty-placeholder .el-icon {
  margin-bottom: 16px;
  opacity: 0.55;
}

.empty-placeholder p {
  margin: 0;
  font-size: 15px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
  padding-top: 14px;
  border-top: 1px solid rgba(148, 163, 184, 0.14);
}

:deep(.item-table .cell) {
  white-space: nowrap;
}

@media (max-width: 1120px) {
  .dict-layout {
    grid-template-columns: 1fr;
  }

  .dict-entry__actions {
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .page-header__title {
    font-size: 28px;
  }

  .section-header {
    align-items: flex-start;
  }

  .section-actions {
    width: 100%;
  }

  .dict-entry__head,
  .dict-entry__actions {
    flex-direction: column;
    align-items: flex-start;
  }

  .dict-detail__title {
    font-size: 24px;
  }

  .pagination-container {
    justify-content: flex-start;
  }

  .item-actions {
    justify-content: flex-start;
  }
}
</style>
