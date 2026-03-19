<template>
  <div class="prompt-page">
    <div class="page-header">
      <div class="page-header__info">
        <h2 class="page-header__title">提示工程</h2>
        <p class="page-header__desc">统一维护 Prompt 模板、变量占位与渲染预览</p>
      </div>
      <div class="page-header__actions">
        <el-button @click="resetFilters">重置筛选</el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增模板
        </el-button>
      </div>
    </div>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="编码">
          <el-input v-model="filters.promptCode" placeholder="如 ai.resume.generate" clearable />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="filters.promptName" placeholder="请输入模板名称" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-input v-model="filters.promptType" placeholder="如 AI_BUSINESS" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadPromptTemplates">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table
        :data="promptList"
        v-loading="loading"
        class="prompt-table"
        table-layout="auto"
        :fit="false"
      >
          <el-table-column prop="promptCode" label="编码" width="240">
            <template #default="{ row }">
              <code class="prompt-code">{{ row.promptCode }}</code>
            </template>
          </el-table-column>

          <el-table-column prop="promptName" label="名称" width="220" />

          <el-table-column prop="promptType" label="类型" width="150">
            <template #default="{ row }">
              <el-tag effect="plain">{{ row.promptType }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="status" label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="摘要" min-width="320">
            <template #default="{ row }">
              <div class="summary-preview">
                <div class="summary-line" :title="row.remark || '-'">
                  {{ row.remark || '暂无备注' }}
                </div>
                <div class="summary-line summary-line--muted" :title="row.promptContent">
                  {{ row.promptContent }}
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="预览" width="100" align="center">
            <template #default="{ row }">
              <el-button size="small" plain @click="handlePreview(row)">查看</el-button>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="100" align="center" fixed="right">
            <template #default="{ row }">
              <el-dropdown trigger="click" @command="command => handleTableCommand(command, row)">
                <el-button size="small" type="primary">
                  操作
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="render">渲染</el-dropdown-item>
                    <el-dropdown-item command="edit">编辑</el-dropdown-item>
                    <el-dropdown-item command="delete">删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          background
          @change="loadPromptTemplates"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="760px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
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

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="模板类型" prop="promptType">
              <el-input v-model="form.promptType" placeholder="如 AI_BUSINESS" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="变量定义" prop="variables">
          <el-input
            v-model="form.variables"
            type="textarea"
            :rows="4"
            placeholder='可填 JSON，例如 {"name":"姓名","targetPosition":"目标岗位"}'
          />
        </el-form-item>

        <el-form-item label="提示词内容" prop="promptContent">
          <el-input
            v-model="form.promptContent"
            type="textarea"
            :rows="12"
            placeholder="请输入模板内容，支持 {{variable}} 占位"
          />
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="可选备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="renderDialogVisible" title="渲染预览" width="760px">
      <el-form :model="renderForm" label-width="100px">
        <el-form-item label="模板编码">
          <el-input v-model="renderForm.promptCode" disabled />
        </el-form-item>
        <el-form-item label="变量 JSON">
          <el-input
            v-model="renderVariablesText"
            type="textarea"
            :rows="6"
            placeholder='请输入 JSON，例如 {"name":"张三"}'
          />
        </el-form-item>
      </el-form>

      <div class="render-actions">
        <el-button type="primary" @click="submitRender">生成预览</el-button>
      </div>

      <el-card v-if="renderResult" class="render-result" shadow="never">
        <template #header>
          <span>渲染结果</span>
        </template>
        <pre>{{ renderResult }}</pre>
      </el-card>

      <template #footer>
        <el-button @click="renderDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewDialogVisible" title="模板详情" width="820px">
      <div v-if="previewItem" class="preview-dialog">
        <el-descriptions :column="2" border class="preview-meta">
          <el-descriptions-item label="模板编码">{{ previewItem.promptCode }}</el-descriptions-item>
          <el-descriptions-item label="模板名称">{{ previewItem.promptName }}</el-descriptions-item>
          <el-descriptions-item label="模板类型">{{ previewItem.promptType }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ previewItem.status === 1 ? '启用' : '禁用' }}</el-descriptions-item>
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
        <el-button @click="previewDialogVisible = false">关闭</el-button>
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
  gap: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;

  &__title {
    margin: 0 0 6px;
    font-size: 24px;
    font-weight: 700;
    color: #1e293b;
  }

  &__desc {
    margin: 0;
    color: #64748b;
  }

  &__actions {
    display: flex;
    gap: 12px;
  }
}

.filter-card,
.table-card {
  border-radius: 18px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 0;
}

.prompt-code {
  display: inline-block;
  padding: 4px 8px;
  background: #eef2ff;
  border-radius: 8px;
  color: #4338ca;
  font-size: 12px;
}

.prompt-table {
  min-width: 1100px;

  :deep(.el-scrollbar__bar.is-horizontal) {
    opacity: 1;
  }
}

.summary-preview {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.summary-line {
  color: #334155;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
  -webkit-line-clamp: 1;
}

.summary-line--muted {
  color: #64748b;
  -webkit-line-clamp: 2;
}

.table-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;

  :deep(.el-button) {
    margin-left: 0;
    min-width: 56px;
  }
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.render-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.render-result {
  background: #f8fafc;

  pre {
    margin: 0;
    white-space: pre-wrap;
    word-break: break-word;
    color: #334155;
    line-height: 1.6;
    font-family: Consolas, 'Courier New', monospace;
  }
}

.preview-dialog {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.preview-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.preview-section__title {
  font-size: 14px;
  font-weight: 700;
  color: #334155;
}

.preview-block {
  margin: 0;
  padding: 16px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  white-space: pre-wrap;
  word-break: break-word;
  color: #334155;
  line-height: 1.6;
  font-family: Consolas, 'Courier New', monospace;
  max-height: 220px;
  overflow: auto;
}

.preview-block--content {
  max-height: 360px;
}

@media (max-width: 960px) {
  .page-header {
    flex-direction: column;
  }

  .page-header__actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .pagination-container {
    justify-content: flex-start;
    overflow-x: auto;
  }

  .prompt-table {
    min-width: 980px;
  }
}
</style>
