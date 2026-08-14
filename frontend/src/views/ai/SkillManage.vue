<template>
  <div class="skill-manage tone-ai">
    <PageHero
      title="技能管理"
      subtitle="维护可在对话中挂载的提示词与工具技能，统一同步、导入与启停。"
      eyebrow="SKILL LIBRARY"
      tone="ai"
      :icon="MagicStick"
    >
      <template #actions>
        <el-button @click="handleSync">同步本地</el-button>
        <el-button type="primary" @click="openImport">导入技能包</el-button>
      </template>
    </PageHero>

    <section class="stat-strip stat-strip--three">
      <StatCard label="技能总数" :value="skills.length" :icon="MagicStick" tone="ai" hint="已注册的技能条目" :delay="0" />
      <StatCard label="启用中" :value="enabledSkillCount" :icon="CircleCheckFilled" tone="tools" hint="当前可挂载的技能" :delay="80" />
      <StatCard label="官方技能" :value="officialSkillCount" :icon="Star" tone="news" hint="官方内置技能数量" :delay="160" />
    </section>

    <section class="skill-toolbar">
      <div class="toolbar-actions">
        <el-input v-model="filters.keyword" clearable placeholder="搜索技能" class="search-input" @keyup.enter="loadSkills" />
        <el-select v-model="filters.originType" clearable placeholder="来源" class="filter-select" @change="loadSkills">
          <el-option label="官方技能" value="OFFICIAL" />
          <el-option label="社区技能" value="COMMUNITY" />
          <el-option label="我的技能" value="MY" />
        </el-select>
        <el-select v-model="filters.skillType" clearable placeholder="类型" class="filter-select" @change="loadSkills">
          <el-option label="提示词" value="PROMPT" />
          <el-option label="工具" value="TOOL" />
          <el-option label="混合" value="MIXED" />
        </el-select>
        <el-button @click="loadSkills">查询</el-button>
      </div>
    </section>

    <section class="skill-table-shell">
      <el-table v-loading="loading" :data="skills" height="100%">
        <el-table-column prop="name" label="技能" min-width="220">
          <template #default="{ row }">
            <div class="skill-name-cell">
              <span>{{ row.name }}</span>
              <el-tag size="small" effect="plain">{{ row.skillKey }}</el-tag>
            </div>
            <p class="skill-desc">{{ row.description || '暂无描述' }}</p>
          </template>
        </el-table-column>
        <el-table-column label="来源" width="120">
          <template #default="{ row }">
            <el-tag :type="originTag(row.originType)" effect="plain">{{ originLabel(row.originType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag effect="plain">{{ typeLabel(row.skillType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="130" />
        <el-table-column prop="scenario" label="场景" width="130" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" @change="value => handleStatus(row, value)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="!row.readonly" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-drawer v-model="drawerVisible" :title="drawerTitle" size="560px">
      <el-form :model="form" label-position="top">
        <el-form-item label="技能名称">
          <el-input v-model="form.name" :disabled="detailMode" />
        </el-form-item>
        <el-form-item label="唯一标识">
          <el-input v-model="form.skillKey" :disabled="detailMode || form.readonly" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" :disabled="detailMode" />
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="来源">
            <el-select v-model="form.originType" :disabled="detailMode">
              <el-option label="官方技能" value="OFFICIAL" />
              <el-option label="社区技能" value="COMMUNITY" />
              <el-option label="我的技能" value="MY" />
            </el-select>
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="form.skillType" :disabled="detailMode">
              <el-option label="提示词" value="PROMPT" />
              <el-option label="工具" value="TOOL" />
              <el-option label="混合" value="MIXED" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="分类">
            <el-input v-model="form.category" :disabled="detailMode" />
          </el-form-item>
          <el-form-item label="使用场景">
            <el-input v-model="form.scenario" :disabled="detailMode" />
          </el-form-item>
        </div>
        <el-form-item label="标签">
          <el-input v-model="form.tags" :disabled="detailMode" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        <el-form-item label="技能正文">
          <el-input v-model="form.content" type="textarea" :rows="10" :disabled="detailMode || form.readonly" />
        </el-form-item>
        <el-form-item label="入口脚本">
          <el-input v-model="form.entryCommand" :disabled="detailMode" placeholder="例如 get-weather.bat" />
        </el-form-item>
        <el-form-item label="参数 Schema">
          <el-input v-model="form.parameterSchema" type="textarea" :rows="5" :disabled="detailMode" />
        </el-form-item>
        <el-form-item v-if="form.scripts?.length" label="本地脚本">
          <div class="script-list">
            <el-tag v-for="script in form.scripts" :key="script" effect="plain">{{ script }}</el-tag>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="drawerVisible = false">关闭</el-button>
        <el-button v-if="!detailMode" type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-drawer>

    <el-dialog v-model="importVisible" title="导入技能包" width="520px">
      <el-upload
        ref="uploadRef"
        drag
        accept=".zip"
        :auto-upload="false"
        :limit="1"
        :on-change="handleImportFileChange"
        :on-remove="handleImportFileRemove"
      >
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="upload-title">拖拽 zip 文件到这里，或点击选择</div>
        <div class="upload-hint">技能包内需要包含 SKILL.md，系统会自动读取 name 和 description</div>
      </el-upload>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" :disabled="!importFile" @click="handleImport">
          导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheckFilled, MagicStick, Star, UploadFilled } from '@element-plus/icons-vue'
import PageHero from '@/components/PageHero.vue'
import StatCard from '@/components/StatCard.vue'
import {
  createSkill,
  deleteSkill,
  getSkill,
  importSkillZip,
  listSkills,
  syncLocalSkills,
  updateSkill,
  updateSkillStatus
} from '@/api/ai-skill'

const loading = ref(false)
const saving = ref(false)
const importing = ref(false)
const drawerVisible = ref(false)
const importVisible = ref(false)
const detailMode = ref(false)
const editingId = ref(null)
const uploadRef = ref(null)
const importFile = ref(null)
const skills = ref([])
const filters = reactive({
  keyword: '',
  originType: '',
  skillType: '',
  enabledOnly: false
})

const emptyForm = () => ({
  skillKey: '',
  name: '',
  description: '',
  originType: 'MY',
  skillType: 'PROMPT',
  category: '',
  scenario: '',
  tags: '',
  content: '',
  entryCommand: '',
  parameterSchema: '',
  enabled: true,
  readonly: false,
  scripts: []
})

const form = reactive(emptyForm())
const drawerTitle = computed(() => detailMode.value ? '技能详情' : editingId.value ? '编辑技能' : '创建技能')

const loadSkills = async () => {
  loading.value = true
  try {
    const { data } = await listSkills(filters)
    skills.value = data?.data || data || []
  } catch (error) {
    ElMessage.error('加载技能失败')
  } finally {
    loading.value = false
  }
}

const resetForm = (next = emptyForm()) => {
  Object.assign(form, emptyForm(), next)
}

const openCreate = () => {
  editingId.value = null
  detailMode.value = false
  resetForm()
  drawerVisible.value = true
}

const openImport = () => {
  importFile.value = null
  importVisible.value = true
  uploadRef.value?.clearFiles?.()
}

const openEdit = async (row) => {
  editingId.value = row.id
  detailMode.value = false
  await loadDetail(row.id)
  drawerVisible.value = true
}

const openDetail = async (row) => {
  editingId.value = row.id
  detailMode.value = true
  await loadDetail(row.id)
  drawerVisible.value = true
}

const loadDetail = async (id) => {
  const { data } = await getSkill(id)
  resetForm(data?.data || data)
}

const handleSave = async () => {
  if (!form.name || !form.skillKey) {
    ElMessage.warning('请填写技能名称和唯一标识')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await updateSkill(editingId.value, form)
    } else {
      await createSkill(form)
    }
    ElMessage.success('保存成功')
    drawerVisible.value = false
    await loadSkills()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const handleStatus = async (row, enabled) => {
  try {
    await updateSkillStatus(row.id, enabled)
    ElMessage.success('状态已更新')
  } catch (error) {
    row.enabled = !enabled
    ElMessage.error('状态更新失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除技能「${row.name}」吗？`, '提示', { type: 'warning' })
    await deleteSkill(row.id)
    ElMessage.success('删除成功')
    await loadSkills()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleSync = async () => {
  try {
    const { data } = await syncLocalSkills()
    ElMessage.success(`同步完成：${data?.data ?? data ?? 0} 个本地技能`)
    await loadSkills()
  } catch (error) {
    ElMessage.error('同步失败')
  }
}

const handleImportFileChange = (uploadFile) => {
  const raw = uploadFile?.raw
  if (!raw) {
    importFile.value = null
    return
  }
  if (!raw.name.toLowerCase().endsWith('.zip')) {
    ElMessage.warning('请选择 zip 格式的技能包')
    uploadRef.value?.clearFiles?.()
    importFile.value = null
    return
  }
  importFile.value = raw
}

const handleImportFileRemove = () => {
  importFile.value = null
}

const handleImport = async () => {
  if (!importFile.value) {
    ElMessage.warning('请先选择技能包')
    return
  }
  importing.value = true
  try {
    const { data } = await importSkillZip(importFile.value)
    const skill = data?.data || data
    ElMessage.success(`技能「${skill?.name || importFile.value.name}」导入成功`)
    importVisible.value = false
    uploadRef.value?.clearFiles?.()
    importFile.value = null
    await loadSkills()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '导入失败')
  } finally {
    importing.value = false
  }
}

const originLabel = (value) => ({ OFFICIAL: '官方', COMMUNITY: '社区', MY: '我的' }[value] || value || '-')
const originTag = (value) => ({ OFFICIAL: 'success', COMMUNITY: 'warning', MY: 'info' }[value] || 'info')
const typeLabel = (value) => ({ PROMPT: '提示词', TOOL: '工具', MIXED: '混合' }[value] || value || '-')

onMounted(loadSkills)
</script>

<style lang="scss" scoped>
.skill-manage {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 0;
}

.skill-toolbar,
.skill-table-shell {
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 12px;
}

.skill-toolbar {
  padding: 16px;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.skill-toolbar h2 {
  margin: 0;
  font-size: 20px;
  color: #0f172a;
}

.skill-toolbar p,
.skill-desc {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 13px;
}

.toolbar-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}

.search-input {
  width: 220px;
}

.filter-select {
  width: 130px;
}

.skill-table-shell {
  flex: 1;
  min-height: 0;
  padding: 10px;
}

.skill-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  color: #0f172a;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.script-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.upload-icon {
  margin-top: 10px;
  font-size: 34px;
  color: #2563eb;
}

.upload-title {
  margin-top: 8px;
  color: #0f172a;
  font-weight: 700;
}

.upload-hint {
  margin-top: 6px;
  color: #64748b;
  font-size: 13px;
}

@media (max-width: 900px) {
  .skill-toolbar {
    flex-direction: column;
  }

  .toolbar-actions,
  .search-input,
  .filter-select {
    width: 100%;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
