<template>
  <div class="prompt-page">
    <div class="page-header">
      <div class="page-header__info">
        <h2 class="page-header__title">提示工程</h2>
        <p class="page-header__desc">统一维护 Prompt 模板、变量占位与渲染预览</p>
      </div>
      <div class="page-header__actions">
        <el-button type="primary" size="large" @click="handleAdd" class="btn-add">
          <el-icon><Plus /></el-icon> 新增规则
        </el-button>
      </div>
    </div>

    <div class="page-main">
      <el-card class="filter-card" shadow="hover">
        <el-form :inline="true" :model="filters" class="filter-form">
          <el-form-item label="编码">
            <el-input v-model="filters.promptCode" placeholder="如 ai.resume.generate" clearable class="filter-input" />
          </el-form-item>
          <el-form-item label="名称">
            <el-input v-model="filters.promptName" placeholder="请输入模板名称" clearable class="filter-input" />
          </el-form-item>
          <el-form-item label="类型">
            <el-input v-model="filters.promptType" placeholder="如 AI_BUSINESS" clearable class="filter-input" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="filters.status" placeholder="全部" clearable class="filter-select">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item class="filter-actions">
            <el-button type="primary" @click="loadPromptTemplates">查 询</el-button>
            <el-button @click="resetFilters">重 置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="table-card" shadow="hover">
        <el-table
          :data="promptList"
          v-loading="loading"
          class="prompt-table"
          stripe
          table-layout="auto"
          :fit="false"
        >
          <el-table-column prop="promptCode" label="编码" min-width="200">
            <template #default="{ row }">
              <code class="prompt-code">{{ row.promptCode }}</code>
            </template>
          </el-table-column>

          <el-table-column prop="promptName" label="名称" min-width="320">
            <template #default="{ row }">
              <div class="prompt-name-wrap">
                <span class="prompt-name">{{ row.promptName || '-' }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="promptType" label="类型" min-width="140">
            <template #default="{ row }">
              <el-tag effect="light" type="primary" hit round class="custom-tag">{{ row.promptType }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light" round>
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="摘要" min-width="340">
            <template #default="{ row }">
              <div class="summary-preview">
                <div class="summary-line" :title="row.remark || '-'">
                  <el-tag size="small" type="info" effect="plain" class="remark-tag">备注</el-tag>
                  {{ row.remark || '暂无备注' }}
                </div>
                <div class="summary-line summary-line--muted" :title="row.promptContent">
                  {{ row.promptContent }}
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="220" align="center" fixed="right">
            <template #default="{ row }">
              <div class="table-actions-group">
                <el-button link type="primary" @click="handlePreview(row)">查看</el-button>
                <el-divider direction="vertical" />
                <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
                <el-divider direction="vertical" />
                <el-button link type="success" @click="handleRender(row)">渲染</el-button>
                <el-divider direction="vertical" />
                <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @change="loadPromptTemplates"
          />
        </div>
      </el-card>
    </div>

    <!-- Modals -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="760px" class="custom-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="top">
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="模板编码" prop="promptCode">
              <el-input v-model="form.promptCode" placeholder="如 ai.resume.generate" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模板名称" prop="promptName">
              <el-input v-model="form.promptName" placeholder="请输入模板名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="模板类型" prop="promptType">
              <el-input v-model="form.promptType" placeholder="如 AI_BUSINESS" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio-button :value="1">启用</el-radio-button>
                <el-radio-button :value="0">禁用</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="变量定义" prop="variables">
          <el-input
            v-model="form.variables"
            type="textarea"
            :rows="3"
            placeholder='可填 JSON，例如 {"name":"姓名","targetPosition":"目标岗位"}'
          />
        </el-form-item>

        <el-form-item label="提示词内容" prop="promptContent">
          <el-input
            v-model="form.promptContent"
            type="textarea"
            :rows="8"
            placeholder="请输入模板内容，支持 {{variable}} 占位"
          />
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确认保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="renderDialogVisible" title="渲染预览" width="760px" class="custom-dialog">
      <el-form :model="renderForm" label-position="top">
        <el-form-item label="模板编码">
          <el-input v-model="renderForm.promptCode" disabled />
        </el-form-item>
        <el-form-item label="变量 JSON (输入测试数据)">
          <el-input
            v-model="renderVariablesText"
            type="textarea"
            :rows="5"
            placeholder='请输入 JSON，例如 {"name":"张三"}'
          />
        </el-form-item>
      </el-form>

      <div class="render-actions">
        <el-button type="primary" size="large" @click="submitRender">生成渲染结果</el-button>
      </div>

      <el-card v-if="renderResult" class="render-result mt-4" shadow="never">
        <template #header>
          <div class="result-header">
            <span>渲染结果输出</span>
          </div>
        </template>
        <pre class="result-content">{{ renderResult }}</pre>
      </el-card>

      <template #footer>
        <el-button @click="renderDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewDialogVisible" title="模板详情" width="820px" class="custom-dialog">
      <div v-if="previewItem" class="preview-dialog">
        <el-descriptions :column="2" border class="preview-meta" size="large">
          <el-descriptions-item label="模板编码">
            <code class="prompt-code">{{ previewItem.promptCode }}</code>
          </el-descriptions-item>
          <el-descriptions-item label="模板名称">
            <strong>{{ previewItem.promptName }}</strong>
          </el-descriptions-item>
          <el-descriptions-item label="模板类型">
            <el-tag effect="light">{{ previewItem.promptType }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="previewItem.status === 1 ? 'success' : 'info'" effect="light" round>
              {{ previewItem.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ previewItem.remark || '暂无备注' }}</el-descriptions-item>
        </el-descriptions>

        <div class="preview-section">
          <div class="preview-section__title">变量定义</div>
          <pre class="preview-block">{{ previewItem.variables || '{}' }}</pre>
        </div>

        <div class="preview-section">
          <div class="preview-section__title">提示词内容</div>
          <pre class="preview-block preview-block--content">{{ previewItem.promptContent }}</pre>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="previewDialogVisible = false">关闭，我已了解</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createPromptTemplate,
  deletePromptTemplate,
  getPromptTemplateList,
  renderPromptTemplate,
  updatePromptTemplate
} from '@/api/prompt'

const loading = ref(false)
const promptList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增模板')
const formRef = ref(null)

const renderDialogVisible = ref(false)
const renderVariablesText = ref('{}')
const renderResult = ref('')
const renderForm = reactive({
  promptCode: ''
})
const previewDialogVisible = ref(false)
const previewItem = ref(null)

const filters = reactive({
  promptCode: '',
  promptName: '',
  promptType: '',
  status: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  promptCode: '',
  promptName: '',
  promptType: 'AI_BUSINESS',
  promptContent: '',
  variables: '',
  status: 1,
  remark: ''
})

const rules = {
  promptCode: [{ required: true, message: '请输入模板编码', trigger: 'blur' }],
  promptName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  promptType: [{ required: true, message: '请输入模板类型', trigger: 'blur' }],
  promptContent: [{ required: true, message: '请输入提示词内容', trigger: 'blur' }]
}

const loadPromptTemplates = async () => {
  loading.value = true
  try {
    const res = await getPromptTemplateList({
      ...filters,
      page: pagination.page - 1,
      size: pagination.size
    })
    promptList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error('加载 Prompt 模板失败:', error)
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    promptCode: '',
    promptName: '',
    promptType: 'AI_BUSINESS',
    promptContent: '',
    variables: '',
    status: 1,
    remark: ''
  })
}

const handleAdd = () => {
  dialogTitle.value = '新增模板'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑模板'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    const payload = {
      promptCode: form.promptCode,
      promptName: form.promptName,
      promptType: form.promptType,
      promptContent: form.promptContent,
      variables: form.variables,
      status: form.status,
      remark: form.remark
    }

    if (form.id) {
      await updatePromptTemplate(form.id, payload)
      ElMessage.success('模板更新成功')
    } else {
      await createPromptTemplate(payload)
      ElMessage.success('模板创建成功')
    }
    dialogVisible.value = false
    loadPromptTemplates()
  } catch (error) {
    console.error('保存 Prompt 模板失败:', error)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除模板「${row.promptName}」吗？`, '提示', { type: 'warning' })
    await deletePromptTemplate(row.id)
    ElMessage.success('模板删除成功')
    loadPromptTemplates()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除 Prompt 模板失败:', error)
    }
  }
}

const handleRender = (row) => {
  renderForm.promptCode = row.promptCode
  renderVariablesText.value = row.variables || '{}'
  renderResult.value = ''
  renderDialogVisible.value = true
}

const handlePreview = (row) => {
  previewItem.value = { ...row }
  previewDialogVisible.value = true
}

const handleTableCommand = (command, row) => {
  if (command === 'render') {
    handleRender(row)
    return
  }
  if (command === 'edit') {
    handleEdit(row)
    return
  }
  if (command === 'delete') {
    handleDelete(row)
  }
}

const submitRender = async () => {
  try {
    const variables = renderVariablesText.value?.trim()
      ? JSON.parse(renderVariablesText.value)
      : {}
    const res = await renderPromptTemplate({
      promptCode: renderForm.promptCode,
      variables
    })
    renderResult.value = res.data?.renderedContent || ''
  } catch (error) {
    ElMessage.error('渲染失败，请检查 JSON 格式')
    console.error('渲染 Prompt 失败:', error)
  }
}

const resetFilters = () => {
  filters.promptCode = ''
  filters.promptName = ''
  filters.promptType = ''
  filters.status = null
  pagination.page = 1
  loadPromptTemplates()
}

onMounted(() => {
  loadPromptTemplates()
})
</script>

<style lang="scss" scoped>
.prompt-page {
  display: flex;
  flex-direction: column;
  background-color: #f1f5f9;
  min-height: calc(100vh - 84px);
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  &__info {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  &__title {
    margin: 0;
    font-size: 26px;
    font-weight: 800;
    color: #0f172a;
    letter-spacing: -0.5px;
  }

  &__desc {
    margin: 0;
    color: #64748b;
    font-size: 14px;
  }
}

.btn-add {
  border-radius: 8px;
  font-weight: 600;
  box-shadow: 0 4px 6px -1px rgba(67, 56, 202, 0.2);
  transition: all 0.2s;
  
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 8px -2px rgba(67, 56, 202, 0.25);
  }
}

.page-main {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.05);

  :deep(.el-card__body) {
    padding: 20px 24px 2px;
  }
}

.table-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);

  :deep(.el-card__body) {
    padding: 0;
  }
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;

  .filter-input {
    width: 220px;
  }
  
  .filter-select {
    width: 120px;
  }

  .el-form-item {
    margin-bottom: 18px;
    margin-right: 16px;
  }

  .filter-actions {
    margin-left: auto;
    margin-right: 0;
  }
}

.prompt-table {
  width: 100%;
  border-radius: 12px;
  overflow: hidden;

  :deep(.el-table__header th) {
    background-color: #f8fafc;
    color: #475569;
    font-weight: 600;
    font-size: 14px;
    border-bottom: 1px solid #e2e8f0;
    padding: 12px 0;
    white-space: nowrap;
  }

  :deep(.el-table__row) {
    transition: background-color 0.2s;
  }
  
  :deep(.el-scrollbar__bar.is-horizontal) {
    opacity: 1;
    height: 8px;
  }
}

.prompt-code {
  display: inline-block;
  padding: 4px 10px;
  background: #eef2ff;
  border-radius: 6px;
  color: #4338ca;
  font-size: 13px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}

.prompt-name {
  display: inline-block;
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
  line-height: 1.4;
  letter-spacing: 0.01em;
  white-space: nowrap;
}

.prompt-name-wrap {
  display: flex;
  align-items: center;
  min-width: max-content;
  min-height: 28px;
  padding: 2px 0;
}

.custom-tag {
  font-weight: 500;
  letter-spacing: 0.5px;
}

.summary-preview {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 4px 0;
  max-width: 100%;
}

.remark-tag {
  margin-right: 8px;
  vertical-align: middle;
}

.summary-line {
  color: #334155;
  font-size: 13px;
  display: flex;
  align-items: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 100%;
}

.summary-line--muted {
  color: #64748b;
  font-size: 13.5px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-all;
  white-space: normal;
}

.table-actions-group {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;

  :deep(.el-divider) {
    margin: 0 4px;
    height: 12px;
    background-color: #cbd5e1;
  }
  
  :deep(.el-button) {
    font-weight: 500;
    padding: 4px;
    height: auto;
  }
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 16px 24px;
  background-color: #fff;
  border-top: 1px solid #e2e8f0;
  border-bottom-left-radius: 12px;
  border-bottom-right-radius: 12px;
}

.custom-dialog {
  :deep(.el-dialog__header) {
    margin-right: 0;
    border-bottom: 1px solid #e2e8f0;
    padding-bottom: 16px;
  }
  
  :deep(.el-dialog__title) {
    font-weight: 700;
    color: #0f172a;
  }

  :deep(.el-dialog__body) {
    padding: 24px;
  }

  :deep(.el-dialog__footer) {
    border-top: 1px solid #e2e8f0;
    padding-top: 16px;
  }
}

.render-actions {
  display: flex;
  justify-content: flex-end;
  margin: 16px 0;
}

.render-result {
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;

  .result-header {
    font-weight: 600;
    color: #0f172a;
  }

  pre.result-content {
    margin: 0;
    white-space: pre-wrap;
    word-break: break-word;
    color: #334155;
    line-height: 1.6;
    font-family: ui-monospace, SFMono-Regular, Consolas, monospace;
    font-size: 13.5px;
  }
}

.preview-dialog {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.preview-meta {
  :deep(.el-descriptions__label) {
    width: 120px;
    background-color: #f8fafc;
    color: #475569;
    font-weight: 600;
  }
}

.preview-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.preview-section__title {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
  padding-left: 8px;
  border-left: 4px solid #4338ca;
  line-height: 1;
}

.preview-block {
  margin: 0;
  padding: 16px;
  border-radius: 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  white-space: pre-wrap;
  word-break: break-word;
  color: #334155;
  line-height: 1.6;
  font-family: ui-monospace, SFMono-Regular, Consolas, monospace;
  font-size: 13.5px;
  max-height: 220px;
  overflow: auto;
}

.preview-block--content {
  max-height: 360px;
}

.mt-4 {
  margin-top: 16px;
}

@media (max-width: 960px) {
  .prompt-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .filter-form .filter-actions {
    margin-left: 0;
    width: 100%;
    margin-top: 8px;
  }

  .pagination-container {
    justify-content: center;
  }
}
</style>
