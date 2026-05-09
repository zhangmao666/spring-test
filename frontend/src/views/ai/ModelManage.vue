<template>
  <div class="model-page">
    <div class="page-header">
      <div class="page-header__info">
        <h2 class="page-header__title">模型管理</h2>
        <p class="page-header__desc">
          统一维护模型配置与默认选择。
        </p>
      </div>

      <div class="page-header__actions">
        <el-button @click="loadModels">刷新</el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增模型
        </el-button>
      </div>
    </div>

    <el-card class="list-card" shadow="never">
      <div class="list-card__meta">
        <div>
          <div class="list-card__label">模型列表</div>
          <div class="list-card__title">{{ modelList.length }} 个配置</div>
        </div>
      </div>

      <div class="model-grid" v-loading="loading">
        <el-empty
          v-if="!modelList.length"
          description="还没有模型配置，可以先新增一个可用模型"
        />

        <article v-for="row in modelList" :key="row.id" class="model-card">
          <div class="model-card__top">
            <div class="model-identity">
              <div class="model-identity__badge">{{ getModelInitial(row) }}</div>

              <div class="model-identity__content">
                <div class="model-identity__title-row">
                  <h3 class="model-name">{{ row.displayName }}</h3>
                  <el-tag v-if="row.isDefault" type="primary" effect="dark" round>默认模型</el-tag>
                  <el-tag :type="row.enabled ? 'success' : 'info'" effect="plain" round>
                    {{ row.enabled ? '已启用' : '已停用' }}
                  </el-tag>
                </div>

                <div class="model-meta">{{ formatProvider(row.provider) }} / {{ row.modelName }}</div>
                <p v-if="row.remark" class="model-remark">{{ row.remark }}</p>
              </div>
            </div>

            <div class="provider-pill">{{ formatProvider(row.provider) }}</div>
          </div>

          <div class="model-specs">
            <div class="spec-card">
              <div class="spec-card__label">Base URL</div>
              <code class="mono spec-card__value">{{ row.baseUrl }}</code>
            </div>

            <div class="spec-card">
              <div class="spec-card__label">API Key</div>
              <div class="spec-card__value mono">{{ row.maskedApiKey || '使用配置文件' }}</div>
            </div>

            <div class="spec-card spec-card--capabilities">
              <div class="spec-card__label">能力标签</div>
              <div class="ability-tags">
                <el-tag size="small" :type="row.supportsDeepThinking ? 'success' : 'info'" effect="plain" round>
                  深度思考
                </el-tag>
                <el-tag size="small" :type="row.supportsWebSearch ? 'warning' : 'info'" effect="plain" round>
                  联网搜索
                </el-tag>
              </div>
            </div>
          </div>

          <div class="model-actions">
            <div class="model-actions__group">
              <el-button size="small" plain @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" plain @click="handleTestRow(row)">测试连接</el-button>
            </div>

            <div class="model-actions__group model-actions__group--secondary">
              <el-button
                size="small"
                type="primary"
                plain
                :disabled="row.isDefault"
                @click="handleSetDefault(row)"
              >
                设为默认
              </el-button>

              <div class="status-toggle">
                <span class="status-toggle__label">{{ row.enabled ? '状态：启用' : '状态：停用' }}</span>
                <el-switch
                  :model-value="row.enabled"
                  @change="value => handleToggleStatus(row, value)"
                />
              </div>

              <el-button size="small" type="danger" plain @click="handleDelete(row)">删除</el-button>
            </div>
          </div>
        </article>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="展示名称" prop="displayName">
              <el-input v-model="form.displayName" placeholder="如：DeepSeek V3.2" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="渠道标识" prop="provider">
              <el-input v-model="form.provider" placeholder="如：openai" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="模型名称" prop="modelName">
              <el-input v-model="form.modelName" placeholder="如：gpt-5.2-chat" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Base URL" prop="baseUrl">
              <el-input v-model="form.baseUrl" placeholder="如：https://api.openai.com/v1" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="API Key" prop="apiKey">
          <el-input
            v-model="form.apiKey"
            type="password"
            show-password
            autocomplete="new-password"
            name="model-api-key"
            :placeholder="form.id ? '留空表示保留原密钥或使用配置文件中的 API Key' : '留空表示使用配置文件中的 API Key'"
          />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="启用状态">
              <el-switch v-model="form.enabled" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="默认模型">
              <el-switch v-model="form.isDefault" :disabled="!form.enabled" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="深度思考">
              <el-switch v-model="form.supportsDeepThinking" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="联网搜索">
              <el-switch v-model="form.supportsWebSearch" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="可选备注" />
        </el-form-item>
      </el-form>

      <div class="test-panel">
        <div class="test-panel__status">
          <el-tag :type="testStatusTagType">
            {{ testStatusLabel }}
          </el-tag>
          <span v-if="lastTestMessage" class="test-panel__message">{{ lastTestMessage }}</span>
        </div>
        <el-button :loading="testing" @click="handleTestConnection">测试连接</el-button>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  createAiModel,
  deleteAiModel,
  getAiModelList,
  setDefaultAiModel,
  testAiModel,
  updateAiModel,
  updateAiModelStatus
} from '@/api/ai-model'

const loading = ref(false)
const testing = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const modelList = ref([])
const connectionVerified = ref(false)
const lastTestMessage = ref('')
const testStatus = ref('idle')

const dialogTitle = computed(() => (form.id ? '编辑模型' : '新增模型'))
const testStatusLabel = computed(() => ({
  idle: '尚未测试或配置已发生变更',
  success: '已通过连接测试',
  error: '连接测试失败'
}[testStatus.value] || '尚未测试或配置已发生变更'))
const testStatusTagType = computed(() => ({
  idle: 'info',
  success: 'success',
  error: 'danger'
}[testStatus.value] || 'info'))

const form = reactive({
  id: null,
  provider: 'openai',
  displayName: '',
  baseUrl: '',
  apiKey: '',
  modelName: '',
  enabled: true,
  isDefault: false,
  supportsDeepThinking: false,
  supportsWebSearch: false,
  remark: ''
})

const rules = {
  provider: [{ required: true, message: '请输入渠道标识', trigger: 'blur' }],
  displayName: [{ required: true, message: '请输入展示名称', trigger: 'blur' }],
  baseUrl: [{ required: true, message: '请输入 Base URL', trigger: 'blur' }],
  modelName: [{ required: true, message: '请输入模型名称', trigger: 'blur' }]
}

watch(
  () => [form.provider, form.baseUrl, form.apiKey, form.modelName].join('|'),
  () => {
    connectionVerified.value = false
    lastTestMessage.value = ''
    testStatus.value = 'idle'
  }
)

const resetForm = () => {
  Object.assign(form, {
    id: null,
    provider: 'openai',
    displayName: '',
    baseUrl: '',
    apiKey: '',
    modelName: '',
    enabled: true,
    isDefault: false,
    supportsDeepThinking: false,
    supportsWebSearch: false,
    remark: ''
  })
  connectionVerified.value = false
  lastTestMessage.value = ''
  testStatus.value = 'idle'
}

const getModelInitial = (row) => {
  const source = row.displayName || row.modelName || '?'
  return source.trim().charAt(0).toUpperCase()
}

const formatProvider = (provider) => (provider ? provider.toUpperCase() : 'UNKNOWN')

const loadModels = async () => {
  loading.value = true
  try {
    const res = await getAiModelList()
    modelList.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  Object.assign(form, {
    id: row.id,
    provider: row.provider,
    displayName: row.displayName,
    baseUrl: row.baseUrl,
    apiKey: '',
    modelName: row.modelName,
    enabled: row.enabled,
    isDefault: row.isDefault,
    supportsDeepThinking: row.supportsDeepThinking,
    supportsWebSearch: row.supportsWebSearch,
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

const handleTestConnection = async () => {
  await formRef.value?.validateField(['provider', 'baseUrl', 'modelName', 'apiKey']).catch(() => null)
  testing.value = true
  try {
    const res = await testAiModel({
      id: form.id,
      provider: form.provider,
      baseUrl: form.baseUrl,
      apiKey: form.apiKey,
      modelName: form.modelName
    })
    connectionVerified.value = true
    testStatus.value = 'success'
    lastTestMessage.value = res.data?.message || '连接测试成功'
    ElMessage.success('连接测试通过')
  } catch (error) {
    connectionVerified.value = false
    testStatus.value = 'error'
    lastTestMessage.value = error?.message || error?.response?.data?.message || '连接测试失败，请检查模型配置'
  } finally {
    testing.value = false
  }
}

const handleTestRow = async (row) => {
  try {
    const res = await testAiModel({
      id: row.id,
      provider: row.provider,
      baseUrl: row.baseUrl,
      apiKey: '',
      modelName: row.modelName
    })
    ElMessage.success(res.data?.message || `${row.displayName} 测试成功`)
  } catch (error) {
    // request interceptor already shows the message
  }
}

const handleSubmit = async () => {
  await formRef.value?.validate()

  const payload = {
    provider: form.provider,
    displayName: form.displayName,
    baseUrl: form.baseUrl,
    apiKey: form.apiKey,
    modelName: form.modelName,
    enabled: form.enabled,
    isDefault: form.isDefault,
    supportsDeepThinking: form.supportsDeepThinking,
    supportsWebSearch: form.supportsWebSearch,
    remark: form.remark
  }

  if (form.id) {
    await updateAiModel(form.id, payload)
    ElMessage.success('模型更新成功')
  } else {
    await createAiModel(payload)
    ElMessage.success('模型创建成功')
  }

  dialogVisible.value = false
  await loadModels()
}

const handleSetDefault = async (row) => {
  await setDefaultAiModel(row.id)
  ElMessage.success('默认模型已更新')
  await loadModels()
}

const handleToggleStatus = async (row, enabled) => {
  try {
    await updateAiModelStatus(row.id, enabled)
    ElMessage.success(enabled ? '模型已启用' : '模型已停用')
    await loadModels()
  } catch (error) {
    await loadModels()
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除模型“${row.displayName}”吗？`, '提示', { type: 'warning' })
    await deleteAiModel(row.id)
    ElMessage.success('模型已删除')
    await loadModels()
  } catch (error) {
    if (error !== 'cancel') {
      await loadModels()
    }
  }
}

onMounted(() => {
  loadModels()
})
</script>

<style lang="scss" scoped>
.model-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  flex-wrap: wrap;
  padding: 2px 2px 0;
}

.page-header__title {
  margin: 0;
  color: #0f172a;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.15;
  letter-spacing: -0.03em;
}

.page-header__desc {
  margin: 6px 0 0;
  max-width: 560px;
  color: #64748b;
  font-size: 14px;
  line-height: 1.6;
}

.page-header__actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.list-card {
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.16);
  box-shadow: none;
}

.list-card__meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.14);
  flex-wrap: wrap;
}

.list-card__label {
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
}

.list-card__title {
  margin-top: 4px;
  color: #1e293b;
  font-size: 16px;
  font-weight: 600;
}

.model-grid {
  display: grid;
  gap: 12px;
}

.model-card {
  position: relative;
  padding: 18px 18px 16px;
  border-radius: 16px;
  border: 1px solid rgba(148, 163, 184, 0.16);
  background: #fff;
  box-shadow: none;
}

.model-card__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 14px;
}

.model-identity {
  display: flex;
  gap: 12px;
  min-width: 0;
  flex: 1;
}

.model-identity__badge {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  flex-shrink: 0;
  background: #f1f5f9;
  color: #334155;
  font-size: 16px;
  font-weight: 700;
}

.model-identity__content {
  min-width: 0;
}

.model-identity__title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.model-name {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
  font-weight: 700;
  line-height: 1.2;
}

.model-meta {
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
}

.model-remark {
  margin-top: 6px;
  color: #94a3b8;
  font-size: 13px;
  line-height: 1.55;
}

.provider-pill {
  padding: 6px 10px;
  border-radius: 999px;
  background: #f8fafc;
  color: #475569;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  white-space: nowrap;
  border: 1px solid rgba(148, 163, 184, 0.14);
}

.model-specs {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}

.spec-card {
  min-width: 0;
  padding: 12px 14px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid rgba(148, 163, 184, 0.14);
}

.spec-card__label {
  margin-bottom: 6px;
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
}

.spec-card__value {
  display: block;
  min-width: 0;
  color: #1e293b;
  line-height: 1.55;
  word-break: break-all;
}

.spec-card--capabilities {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.mono {
  font-family: Consolas, Monaco, monospace;
  font-size: 12px;
}

.ability-tags,
.test-panel,
.test-panel__status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ability-tags {
  flex-wrap: wrap;
  align-items: flex-start;
}

.model-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding-top: 14px;
  border-top: 1px solid rgba(148, 163, 184, 0.16);
  flex-wrap: wrap;
}

.model-actions__group {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.model-actions__group--secondary {
  justify-content: flex-end;
}

.status-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  border-radius: 999px;
  background: #f8fafc;
  border: 1px solid rgba(148, 163, 184, 0.14);
}

.status-toggle__label {
  color: #475569;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.model-card :deep(.el-button.el-button--small) {
  height: 30px;
  padding: 0 12px !important;
  border-radius: 999px !important;
}

.model-card :deep(.el-switch) {
  --el-switch-on-color: #0f766e;
  --el-switch-off-color: #cbd5e1;
}

.test-panel {
  justify-content: space-between;
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: 12px;
  background: #f8fafc;
  gap: 12px;
  flex-wrap: wrap;
}

.test-panel__message {
  color: #64748b;
  font-size: 13px;
}

@media (max-width: 1080px) {
  .model-specs {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 768px) {
  .page-header {
    align-items: flex-start;
  }

  .page-header__title {
    font-size: 24px;
  }

  .model-card {
    padding: 16px;
  }

  .model-card__top {
    flex-direction: column;
  }

  .model-name {
    font-size: 18px;
  }

  .model-specs {
    grid-template-columns: 1fr;
  }

  .model-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .model-actions__group,
  .model-actions__group--secondary {
    width: 100%;
    justify-content: flex-start;
  }

  .status-toggle {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
