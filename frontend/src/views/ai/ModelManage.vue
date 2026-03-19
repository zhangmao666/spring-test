<template>
  <div class="model-page">
    <div class="page-header">
      <div class="page-header__info">
        <h2 class="page-header__title">基座模型管理</h2>
        <p class="page-header__desc">统一维护 OpenAI 协议模型配置，并为聊天页提供可切换模型</p>
      </div>
      <div class="page-header__actions">
        <el-button @click="loadModels">刷新</el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增模型
        </el-button>
      </div>
    </div>

    <el-card class="table-card" shadow="never">
      <el-table :data="modelList" v-loading="loading" class="model-table">
        <el-table-column label="模型" min-width="240">
          <template #default="{ row }">
            <div class="model-main">
              <div class="model-name">{{ row.displayName }}</div>
              <div class="model-meta">{{ row.provider }} / {{ row.modelName }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="Base URL" min-width="220">
          <template #default="{ row }">
            <code class="mono">{{ row.baseUrl }}</code>
          </template>
        </el-table-column>

        <el-table-column label="API Key" width="150">
          <template #default="{ row }">
            <span class="mono">{{ row.maskedApiKey || '未配置' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="能力" width="180">
          <template #default="{ row }">
            <div class="ability-tags">
              <el-tag size="small" :type="row.supportsDeepThinking ? 'success' : 'info'" effect="plain">
                深度思考
              </el-tag>
              <el-tag size="small" :type="row.supportsWebSearch ? 'warning' : 'info'" effect="plain">
                联网搜索
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="180">
          <template #default="{ row }">
            <div class="status-cell">
              <el-tag :type="row.enabled ? 'success' : 'info'">
                {{ row.enabled ? '启用' : '停用' }}
              </el-tag>
              <el-tag v-if="row.isDefault" type="primary">默认</el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" plain @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" plain @click="handleTestRow(row)">测试</el-button>
              <el-button size="small" type="primary" plain :disabled="row.isDefault" @click="handleSetDefault(row)">
                设为默认
              </el-button>
              <el-switch
                :model-value="row.enabled"
                inline-prompt
                active-text="开"
                inactive-text="关"
                @change="value => handleToggleStatus(row, value)"
              />
              <el-button size="small" type="danger" plain @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
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
              <el-input v-model="form.modelName" placeholder="如：deepseek-v3.2" />
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
            :placeholder="form.id ? '留空表示保留原密钥' : '请输入 API Key'"
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
          <el-tag :type="connectionVerified ? 'success' : 'info'">
            {{ connectionVerified ? '已通过连接测试' : '尚未测试或配置已变更' }}
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

const dialogTitle = computed(() => (form.id ? '编辑模型' : '新增模型'))

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
  modelName: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
  apiKey: [{
    validator: (_, value, callback) => {
      if (!form.id && !value) {
        callback(new Error('新建模型时必须填写 API Key'))
        return
      }
      callback()
    },
    trigger: 'blur'
  }]
}

watch(
  () => [form.provider, form.baseUrl, form.apiKey, form.modelName].join('|'),
  () => {
    connectionVerified.value = false
    lastTestMessage.value = ''
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
}

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
    lastTestMessage.value = res.data?.message || '连接测试成功'
    ElMessage.success('连接测试通过')
  } catch (error) {
    connectionVerified.value = false
    lastTestMessage.value = ''
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
  if (!connectionVerified.value) {
    ElMessage.warning('请先通过连接测试再保存')
    return
  }

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
  gap: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.page-header__title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
}

.page-header__desc {
  margin: 6px 0 0;
  color: #64748b;
}

.page-header__actions {
  display: flex;
  gap: 12px;
}

.table-card {
  border-radius: 20px;
}

.model-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.model-name {
  font-weight: 700;
  color: #0f172a;
}

.model-meta,
.test-panel__message {
  color: #64748b;
  font-size: 13px;
}

.mono {
  font-family: Consolas, Monaco, monospace;
  font-size: 12px;
}

.ability-tags,
.status-cell,
.table-actions,
.test-panel,
.test-panel__status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.table-actions {
  flex-wrap: wrap;
}

.test-panel {
  justify-content: space-between;
  margin-top: 8px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #f8fafc;
}
</style>
