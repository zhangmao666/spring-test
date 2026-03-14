<template>
  <div class="resume-ai-page">

    <!-- 页头 -->
    <div class="ra-header">
      <div class="ra-header__left">
        <div class="ra-icon-badge">
          <el-icon :size="22"><DocumentChecked /></el-icon>
        </div>
        <div>
          <h2 class="ra-title">AI 简历助手</h2>
          <p class="ra-desc">智能优化现有简历 · AI 从零建立生成专业简历</p>
        </div>
      </div>
    </div>

    <!-- Tab 切换 -->
    <div class="ra-tabs">
      <div
        class="ra-tab"
        :class="{ active: activeTab === 'optimize' }"
        @click="activeTab = 'optimize'"
      >
        <el-icon><MagicStick /></el-icon>
        <span>简历优化</span>
      </div>
      <div
        class="ra-tab"
        :class="{ active: activeTab === 'generate' }"
        @click="activeTab = 'generate'"
      >
        <el-icon><Promotion /></el-icon>
        <span>AI 建立生成</span>
      </div>
    </div>

    <!-- ══════════ Tab1：简历优化 ══════════ -->
    <div v-show="activeTab === 'optimize'" class="ra-body">
      <div class="ra-form-col">
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <el-icon color="#6366f1"><Edit /></el-icon>
              <span>填写优化信息</span>
            </div>
          </template>

          <el-form :model="optimizeForm" label-position="top" class="ra-form">
            <el-form-item label="目标岗位" required>
              <el-input
                v-model="optimizeForm.targetPosition"
                placeholder="如：前端工程师、产品经理、Java开发..."
                clearable
              />
            </el-form-item>

            <el-form-item label="目标行业">
              <el-select v-model="optimizeForm.targetIndustry" placeholder="选择或输入行业" clearable filterable allow-create>
                <el-option v-for="ind in industries" :key="ind" :label="ind" :value="ind" />
              </el-select>
            </el-form-item>

            <el-form-item label="优化方向">
              <el-checkbox-group v-model="optimizeForm.optimizeDirections">
                <el-checkbox label="表达优化">表达优化</el-checkbox>
                <el-checkbox label="结构调整">结构调整</el-checkbox>
                <el-checkbox label="量化成果">量化成果</el-checkbox>
                <el-checkbox label="突出亮点">突出亮点</el-checkbox>
                <el-checkbox label="关键词匹配">关键词匹配</el-checkbox>
              </el-checkbox-group>
            </el-form-item>

            <el-form-item label="附加要求">
              <el-input
                v-model="optimizeForm.additionalRequirements"
                type="textarea"
                :rows="2"
                placeholder="如：突出技术深度、弱化管理经验、强调海外背景..."
                maxlength="500"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="原始简历内容" required>
              <el-input
                v-model="optimizeForm.resumeContent"
                type="textarea"
                :rows="12"
                placeholder="粘贴你的简历文本内容，支持纯文本格式..."
                maxlength="6000"
                show-word-limit
                resize="none"
              />
            </el-form-item>

            <el-form-item>
              <div class="form-footer">
                <div class="form-footer__opts">
                  <el-switch v-model="optimizeForm.useDeepThinking" />
                  <span class="switch-label">深度思考模式（更慢但质量更高）</span>
                </div>
                <el-button
                  type="primary"
                  :loading="optimizeLoading"
                  :disabled="!optimizeForm.resumeContent || !optimizeForm.targetPosition"
                  @click="handleOptimize"
                  size="large"
                >
                  <el-icon><MagicStick /></el-icon>
                  {{ optimizeLoading ? '优化中...' : '开始优化' }}
                </el-button>
              </div>
            </el-form-item>
          </el-form>
        </el-card>
      </div>

      <!-- 优化结果 -->
      <div class="ra-result-col">
        <div v-if="!optimizeResult && !optimizeLoading" class="ra-empty">
          <div class="ra-empty__icon">
            <el-icon :size="48" color="#cbd5e1"><DocumentChecked /></el-icon>
          </div>
          <p class="ra-empty__title">优化结果将在这里展示</p>
          <p class="ra-empty__desc">填写左侧信息后点击"开始优化"</p>
        </div>

        <div v-if="optimizeLoading" class="ra-loading">
          <div class="loading-spinner">
            <div class="spinner-ring"></div>
            <div class="spinner-ring spinner-ring--delay"></div>
          </div>
          <p class="loading-text">AI 正在分析并优化您的简历...</p>
          <p class="loading-sub">预计需要 15-60 秒，请耐心等待</p>
        </div>

        <template v-if="optimizeResult && !optimizeLoading">
          <!-- 评分卡 -->
          <div class="score-banner">
            <div class="score-banner__left">
              <div class="score-circle" :style="{ '--score-color': scoreColor(optimizeResult.matchScore) }">
                <span class="score-num">{{ optimizeResult.matchScore ?? '--' }}</span>
                <span class="score-unit">分</span>
              </div>
              <div>
                <div class="score-label">岗位匹配度</div>
                <div class="score-position">{{ optimizeResult.targetPosition }}</div>
              </div>
            </div>
            <div class="score-banner__right">
              <el-button size="small" @click="copyOptimized">
                <el-icon><CopyDocument /></el-icon> 复制优化简历
              </el-button>
              <el-button size="small" type="primary" plain @click="downloadOptimized">
                <el-icon><Download /></el-icon> 下载
              </el-button>
            </div>
          </div>

          <!-- 标签栏 -->
          <div class="result-tabs">
            <span
              v-for="t in resultTabs"
              :key="t.key"
              :class="['result-tab', { active: resultTab === t.key }]"
              @click="resultTab = t.key"
            >{{ t.label }}</span>
          </div>

          <!-- 优化后简历 -->
          <el-card v-show="resultTab === 'resume'" class="result-card">
            <div class="resume-preview" v-html="renderMarkdown(optimizeResult.optimizedResume)"></div>
          </el-card>

          <!-- 优化摘要 -->
          <el-card v-show="resultTab === 'summary'" class="result-card">
            <div class="tag-section">
              <div class="tag-section__title">优化摘要</div>
              <div class="tag-list">
                <el-tag v-for="(s, i) in optimizeResult.optimizeSummary" :key="i" type="primary" effect="plain">
                  {{ s }}
                </el-tag>
              </div>
            </div>
            <div class="tag-section">
              <div class="tag-section__title">核心亮点</div>
              <div class="highlight-list">
                <div v-for="(h, i) in optimizeResult.highlights" :key="i" class="highlight-item">
                  <span class="highlight-num">{{ i + 1 }}</span>
                  <span>{{ h }}</span>
                </div>
              </div>
            </div>
            <div class="tag-section">
              <div class="tag-section__title">改进建议</div>
              <div class="suggestion-list">
                <div v-for="(s, i) in optimizeResult.suggestions" :key="i" class="suggestion-item">
                  <el-icon color="#f59e0b"><Warning /></el-icon>
                  <span>{{ s }}</span>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 元数据 -->
          <div class="meta-bar">
            <span>模型：{{ optimizeResult.model || '-' }}</span>
            <span>耗时：{{ optimizeResult.responseTime ? (optimizeResult.responseTime / 1000).toFixed(1) + 's' : '-' }}</span>
            <span>Token：{{ optimizeResult.tokensUsed ?? '-' }}</span>
          </div>
        </template>
      </div>
    </div>

    <!-- ══════════ Tab2：AI 建立生成 ══════════ -->
    <div v-show="activeTab === 'generate'" class="ra-body">
      <div class="ra-form-col">
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <el-icon color="#10b981"><Promotion /></el-icon>
              <span>填写个人信息</span>
            </div>
          </template>

          <el-form :model="generateForm" label-position="top" class="ra-form">
            <div class="form-row-2">
              <el-form-item label="姓名" required>
                <el-input v-model="generateForm.name" placeholder="请输入您的姓名" clearable />
              </el-form-item>
              <el-form-item label="目标岗位" required>
                <el-input v-model="generateForm.targetPosition" placeholder="如：Java后端工程师" clearable />
              </el-form-item>
            </div>

            <div class="form-row-2">
              <el-form-item label="目标行业">
                <el-select v-model="generateForm.targetIndustry" placeholder="选择行业" clearable filterable allow-create>
                  <el-option v-for="ind in industries" :key="ind" :label="ind" :value="ind" />
                </el-select>
              </el-form-item>
              <el-form-item label="工作年限" required>
                <el-select v-model="generateForm.workYears" placeholder="选择工作年限">
                  <el-option label="应届毕业生" value="应届" />
                  <el-option label="1-3 年" value="1-3年" />
                  <el-option label="3-5 年" value="3-5年" />
                  <el-option label="5-10 年" value="5-10年" />
                  <el-option label="10 年以上" value="10年以上" />
                </el-select>
              </el-form-item>
            </div>

            <div class="form-row-3">
              <el-form-item label="最高学历">
                <el-select v-model="generateForm.education" placeholder="学历">
                  <el-option label="大专" value="大专" />
                  <el-option label="本科" value="本科" />
                  <el-option label="硕士" value="硕士" />
                  <el-option label="博士" value="博士" />
                </el-select>
              </el-form-item>
              <el-form-item label="毕业院校">
                <el-input v-model="generateForm.school" placeholder="院校名称" clearable />
              </el-form-item>
              <el-form-item label="专业">
                <el-input v-model="generateForm.major" placeholder="专业名称" clearable />
              </el-form-item>
            </div>

            <el-form-item label="核心技能">
              <el-input
                v-model="generateForm.coreSkills"
                placeholder="如：Java、Spring Boot、MySQL、Redis、Vue3（逗号分隔）"
                clearable
              />
            </el-form-item>

            <el-form-item label="工作经历">
              <el-input
                v-model="generateForm.workExperience"
                type="textarea"
                :rows="5"
                placeholder="简要描述工作经历，如：&#10;- 2021-2023 XXX公司 后端开发工程师，负责用户中台开发，日活100w&#10;- 2023-今 YYY公司 高级工程师，主导微服务架构改造..."
                maxlength="2000"
                show-word-limit
                resize="none"
              />
            </el-form-item>

            <el-form-item label="项目经历">
              <el-input
                v-model="generateForm.projectExperience"
                type="textarea"
                :rows="4"
                placeholder="描述主要项目，如：&#10;- 电商平台重构：使用 Spring Cloud 完成微服务拆分，TPS 提升 3 倍&#10;- 实时数据看板：基于 Flink + Kafka 构建..."
                maxlength="2000"
                show-word-limit
                resize="none"
              />
            </el-form-item>

            <el-form-item label="个人优势">
              <el-input
                v-model="generateForm.personalSummary"
                type="textarea"
                :rows="2"
                placeholder="如：擅长高并发场景设计，热爱技术分享，有开源项目贡献..."
                maxlength="500"
                show-word-limit
                resize="none"
              />
            </el-form-item>

            <el-form-item label="简历风格">
              <el-radio-group v-model="generateForm.style">
                <el-radio-button label="concise">简洁版</el-radio-button>
                <el-radio-button label="detailed">详细版</el-radio-button>
                <el-radio-button label="technical">技术向</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item>
              <div class="form-footer">
                <div class="form-footer__opts">
                  <el-switch v-model="generateForm.useDeepThinking" />
                  <span class="switch-label">深度思考模式</span>
                </div>
                <el-button
                  type="success"
                  :loading="generateLoading"
                  :disabled="!generateForm.name || !generateForm.targetPosition || !generateForm.workYears"
                  @click="handleGenerate"
                  size="large"
                >
                  <el-icon><Promotion /></el-icon>
                  {{ generateLoading ? '生成中...' : '立即生成简历' }}
                </el-button>
              </div>
            </el-form-item>
          </el-form>
        </el-card>
      </div>

      <!-- 生成结果 -->
      <div class="ra-result-col">
        <div v-if="!generateResult && !generateLoading" class="ra-empty">
          <div class="ra-empty__icon">
            <el-icon :size="48" color="#cbd5e1"><Promotion /></el-icon>
          </div>
          <p class="ra-empty__title">生成的简历将在这里展示</p>
          <p class="ra-empty__desc">填写左侧信息后点击"立即生成简历"</p>
        </div>

        <div v-if="generateLoading" class="ra-loading">
          <div class="loading-spinner">
            <div class="spinner-ring spinner-ring--green"></div>
            <div class="spinner-ring spinner-ring--green spinner-ring--delay"></div>
          </div>
          <p class="loading-text">AI 正在为您量身定制简历...</p>
          <p class="loading-sub">预计需要 20-60 秒，请耐心等待</p>
        </div>

        <template v-if="generateResult && !generateLoading">
          <!-- 简历头部信息栏 -->
          <div class="gen-banner">
            <div class="gen-banner__info">
              <div class="gen-name">{{ generateResult.name }}</div>
              <div class="gen-position">
                <el-tag type="success" size="small">{{ generateResult.targetPosition }}</el-tag>
              </div>
            </div>
            <div class="gen-banner__actions">
              <el-button size="small" @click="copyGenerated">
                <el-icon><CopyDocument /></el-icon> 复制简历
              </el-button>
              <el-button size="small" type="success" plain @click="downloadGenerated">
                <el-icon><Download /></el-icon> 下载
              </el-button>
            </div>
          </div>

          <!-- 结果标签 -->
          <div class="result-tabs">
            <span
              v-for="t in genResultTabs"
              :key="t.key"
              :class="['result-tab', { active: genResultTab === t.key }]"
              @click="genResultTab = t.key"
            >{{ t.label }}</span>
          </div>

          <el-card v-show="genResultTab === 'resume'" class="result-card">
            <div class="resume-preview" v-html="renderMarkdown(generateResult.resumeContent)"></div>
          </el-card>

          <el-card v-show="genResultTab === 'tips'" class="result-card">
            <div class="tips-section">
              <div class="tips-section__title">
                <el-icon color="#6366f1"><Opportunity /></el-icon>
                求职建议
              </div>
              <div class="tips-content" v-html="renderMarkdown(generateResult.writingTips || '暂无建议')"></div>
            </div>
          </el-card>

          <div class="meta-bar">
            <span>模型：{{ generateResult.model || '-' }}</span>
            <span>耗时：{{ generateResult.responseTime ? (generateResult.responseTime / 1000).toFixed(1) + 's' : '-' }}</span>
            <span>Token：{{ generateResult.tokensUsed ?? '-' }}</span>
          </div>
        </template>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import {
  DocumentChecked, MagicStick, Promotion, Edit, CopyDocument,
  Download, Warning, Opportunity
} from '@element-plus/icons-vue'
import { optimizeResume, generateResume } from '@/api/resume'

// ── Tab ──────────────────────────────────────────────────────
const activeTab = ref('optimize')

// ── 行业列表 ─────────────────────────────────────────────────
const industries = [
  '互联网/软件', '金融/银行', '电商/零售', '教育培训', '医疗健康',
  '游戏/娱乐', '人工智能', '云计算/大数据', '新能源/汽车', '制造业',
  '咨询/投资', '政府/事业单位'
]

// ── 优化 Tab 数据 ─────────────────────────────────────────────
const optimizeForm = reactive({
  resumeContent: '',
  targetPosition: '',
  targetIndustry: '',
  optimizeDirections: ['表达优化', '量化成果'],
  additionalRequirements: '',
  useDeepThinking: false
})

const optimizeLoading = ref(false)
const optimizeResult = ref(null)
const resultTab = ref('resume')
const resultTabs = [
  { key: 'resume', label: '优化后简历' },
  { key: 'summary', label: '优化报告' }
]

// ── 生成 Tab 数据 ─────────────────────────────────────────────
const generateForm = reactive({
  name: '',
  targetPosition: '',
  targetIndustry: '',
  workYears: '',
  education: '本科',
  school: '',
  major: '',
  coreSkills: '',
  workExperience: '',
  projectExperience: '',
  personalSummary: '',
  style: 'detailed',
  useDeepThinking: false
})

const generateLoading = ref(false)
const generateResult = ref(null)
const genResultTab = ref('resume')
const genResultTabs = [
  { key: 'resume', label: '生成的简历' },
  { key: 'tips', label: '求职建议' }
]

// ── 操作 ─────────────────────────────────────────────────────
const handleOptimize = async () => {
  if (!optimizeForm.resumeContent.trim()) {
    ElMessage.warning('请输入简历内容')
    return
  }
  if (!optimizeForm.targetPosition.trim()) {
    ElMessage.warning('请填写目标岗位')
    return
  }
  optimizeLoading.value = true
  optimizeResult.value = null
  try {
    const payload = {
      resumeContent: optimizeForm.resumeContent,
      targetPosition: optimizeForm.targetPosition,
      targetIndustry: optimizeForm.targetIndustry || undefined,
      optimizeDirection: optimizeForm.optimizeDirections.join('、') || undefined,
      additionalRequirements: optimizeForm.additionalRequirements || undefined,
      useDeepThinking: optimizeForm.useDeepThinking
    }
    const res = await optimizeResume(payload)
    optimizeResult.value = res.data
    resultTab.value = 'resume'
    ElMessage.success('简历优化完成！')
  } catch (err) {
    ElMessage.error('优化失败，请稍后重试')
    console.error(err)
  } finally {
    optimizeLoading.value = false
  }
}

const handleGenerate = async () => {
  if (!generateForm.name.trim() || !generateForm.targetPosition.trim()) {
    ElMessage.warning('请填写姓名和目标岗位')
    return
  }
  generateLoading.value = true
  generateResult.value = null
  try {
    const payload = {
      name: generateForm.name,
      targetPosition: generateForm.targetPosition,
      targetIndustry: generateForm.targetIndustry || undefined,
      workYears: generateForm.workYears,
      education: generateForm.education || undefined,
      school: generateForm.school || undefined,
      major: generateForm.major || undefined,
      coreSkills: generateForm.coreSkills || undefined,
      workExperience: generateForm.workExperience || undefined,
      projectExperience: generateForm.projectExperience || undefined,
      personalSummary: generateForm.personalSummary || undefined,
      style: generateForm.style,
      useDeepThinking: generateForm.useDeepThinking
    }
    const res = await generateResume(payload)
    generateResult.value = res.data
    genResultTab.value = 'resume'
    ElMessage.success('简历生成成功！')
  } catch (err) {
    ElMessage.error('生成失败，请稍后重试')
    console.error(err)
  } finally {
    generateLoading.value = false
  }
}

// ── 复制 & 下载 ───────────────────────────────────────────────
const copyOptimized = () => {
  if (!optimizeResult.value?.optimizedResume) return
  navigator.clipboard.writeText(optimizeResult.value.optimizedResume)
    .then(() => ElMessage.success('已复制到剪贴板'))
    .catch(() => ElMessage.error('复制失败'))
}

const copyGenerated = () => {
  if (!generateResult.value?.resumeContent) return
  navigator.clipboard.writeText(generateResult.value.resumeContent)
    .then(() => ElMessage.success('已复制到剪贴板'))
    .catch(() => ElMessage.error('复制失败'))
}

const downloadFile = (content, filename) => {
  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = filename
  a.click()
  URL.revokeObjectURL(a.href)
}

const downloadOptimized = () => {
  if (!optimizeResult.value?.optimizedResume) return
  downloadFile(optimizeResult.value.optimizedResume,
    `${optimizeResult.value.targetPosition}_优化简历.md`)
}

const downloadGenerated = () => {
  if (!generateResult.value?.resumeContent) return
  downloadFile(generateResult.value.resumeContent,
    `${generateResult.value.name}_${generateResult.value.targetPosition}_简历.md`)
}

// ── 工具 ─────────────────────────────────────────────────────
const scoreColor = (score) => {
  if (!score) return '#94a3b8'
  if (score >= 85) return '#10b981'
  if (score >= 70) return '#3b82f6'
  if (score >= 50) return '#f59e0b'
  return '#ef4444'
}

/** 极简 Markdown 渲染（h1-h3 / bold / ul / 换行） */
const renderMarkdown = (text) => {
  if (!text) return ''
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/^### (.+)$/gm, '<h3>$1</h3>')
    .replace(/^## (.+)$/gm, '<h2>$1</h2>')
    .replace(/^# (.+)$/gm, '<h1>$1</h1>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    .replace(/^[-*] (.+)$/gm, '<li>$1</li>')
    .replace(/(<li>.*<\/li>(\n|$))+/g, (m) => `<ul>${m}</ul>`)
    .replace(/\n{2,}/g, '</p><p>')
    .replace(/\n/g, '<br>')
    .replace(/^(?!<[hul])(.+)$/gm, (m) => m.startsWith('<') ? m : `<p>${m}</p>`)
}
</script>

<style lang="scss" scoped>
$primary: #6366f1;
$green: #10b981;
$blue: #3b82f6;
$yellow: #f59e0b;
$red: #ef4444;
$gray: #64748b;
$border: #e2e8f0;
$radius: 14px;
$bg: #f8fafc;

.resume-ai-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 0;
}

/* ─── 页头 ─── */
.ra-header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  &__left {
    display: flex;
    align-items: center;
    gap: 14px;
  }
}

.ra-icon-badge {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, $primary 0%, #8b5cf6 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.3);
}

.ra-title {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.4px;
}

.ra-desc {
  margin: 3px 0 0;
  font-size: 13px;
  color: $gray;
}

/* ─── Tab 切换 ─── */
.ra-tabs {
  display: flex;
  gap: 8px;
  background: #fff;
  padding: 6px;
  border-radius: 14px;
  width: fit-content;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}

.ra-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 24px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  color: $gray;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;

  .el-icon { font-size: 16px; }

  &:hover { color: $primary; background: rgba($primary, 0.06); }

  &.active {
    background: $primary;
    color: #fff;
    box-shadow: 0 4px 12px rgba($primary, 0.3);
  }

  &:last-child.active {
    background: $green;
    box-shadow: 0 4px 12px rgba($green, 0.3);
  }
}

/* ─── 主体布局 ─── */
.ra-body {
  display: grid;
  grid-template-columns: 480px 1fr;
  gap: 20px;
  min-height: 0;
  align-items: start;
}

/* ─── 表单区 ─── */
.ra-form-col { min-height: 0; }

.form-card {
  :deep(.el-card__header) {
    padding: 16px 20px;
    border-bottom: 1px solid $border;
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
}

.ra-form {
  :deep(.el-form-item__label) {
    font-size: 13px;
    font-weight: 600;
    color: #334155;
    padding-bottom: 4px;
  }

  :deep(.el-textarea__inner),
  :deep(.el-input__inner) {
    font-size: 13px;
  }
}

.form-row-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.form-row-3 {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 12px;
}

.form-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding-top: 4px;

  &__opts {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .switch-label {
    font-size: 13px;
    color: $gray;
  }
}

/* ─── 结果区 ─── */
.ra-result-col {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.ra-empty {
  background: #fff;
  border-radius: $radius;
  padding: 64px 24px;
  text-align: center;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);

  &__icon { margin-bottom: 16px; }

  &__title {
    font-size: 16px;
    font-weight: 600;
    color: #475569;
    margin: 0 0 8px;
  }

  &__desc {
    font-size: 13px;
    color: #94a3b8;
    margin: 0;
  }
}

/* ─── 加载动画 ─── */
.ra-loading {
  background: #fff;
  border-radius: $radius;
  padding: 64px 24px;
  text-align: center;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);

  .loading-text {
    font-size: 15px;
    font-weight: 600;
    color: #334155;
    margin: 24px 0 6px;
  }

  .loading-sub {
    font-size: 13px;
    color: #94a3b8;
    margin: 0;
  }
}

.loading-spinner {
  position: relative;
  width: 56px;
  height: 56px;
  margin: 0 auto;
}

.spinner-ring {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 3px solid transparent;
  border-top-color: $primary;
  animation: spin 1s linear infinite;

  &--green { border-top-color: $green; }

  &--delay {
    animation-delay: -0.4s;
    inset: 8px;
    border-width: 2px;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ─── 评分横幅 ─── */
.score-banner {
  background: #fff;
  border-radius: $radius;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);

  &__left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  &__right {
    display: flex;
    gap: 8px;
  }
}

.score-circle {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: 3px solid var(--score-color, #94a3b8);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: color-mix(in srgb, var(--score-color, #94a3b8) 8%, #fff);
}

.score-num {
  font-size: 22px;
  font-weight: 800;
  color: var(--score-color, #94a3b8);
  line-height: 1;
}

.score-unit { font-size: 11px; color: $gray; }

.score-label {
  font-size: 12px;
  color: $gray;
  margin-bottom: 4px;
}

.score-position {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
}

/* ─── 生成结果头部 ─── */
.gen-banner {
  background: linear-gradient(135deg, rgba($green, 0.06) 0%, rgba($blue, 0.06) 100%);
  border: 1px solid rgba($green, 0.2);
  border-radius: $radius;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;

  &__actions {
    display: flex;
    gap: 8px;
  }
}

.gen-name {
  font-size: 20px;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 6px;
}

.gen-position { display: flex; gap: 8px; }

/* ─── 结果 Tab ─── */
.result-tabs {
  display: flex;
  gap: 4px;
  background: #fff;
  padding: 4px;
  border-radius: 10px;
  width: fit-content;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.result-tab {
  padding: 7px 18px;
  border-radius: 7px;
  font-size: 13px;
  font-weight: 600;
  color: $gray;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;

  &:hover { color: $primary; }

  &.active {
    background: $primary;
    color: #fff;
  }
}

/* ─── 简历预览 ─── */
.result-card {
  :deep(.el-card__body) { padding: 24px; }
}

.resume-preview {
  font-size: 14px;
  line-height: 1.75;
  color: #1e293b;

  :deep(h1) {
    font-size: 20px;
    font-weight: 800;
    color: #0f172a;
    margin: 0 0 16px;
    padding-bottom: 10px;
    border-bottom: 2px solid $primary;
  }

  :deep(h2) {
    font-size: 15px;
    font-weight: 700;
    color: $primary;
    margin: 20px 0 8px;
    padding-left: 10px;
    border-left: 3px solid $primary;
  }

  :deep(h3) {
    font-size: 14px;
    font-weight: 600;
    color: #334155;
    margin: 12px 0 6px;
  }

  :deep(ul) {
    margin: 6px 0;
    padding-left: 20px;
  }

  :deep(li) {
    margin-bottom: 4px;
    color: #475569;
  }

  :deep(strong) { color: #0f172a; }

  :deep(p) {
    margin: 8px 0;
    color: #475569;
  }
}

/* ─── 优化报告 ─── */
.tag-section {
  margin-bottom: 24px;

  &__title {
    font-size: 13px;
    font-weight: 700;
    color: #334155;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    margin-bottom: 12px;
  }
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.highlight-list { display: flex; flex-direction: column; gap: 8px; }

.highlight-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 14px;
  background: rgba($primary, 0.04);
  border-radius: 8px;
  font-size: 13px;
  color: #334155;
}

.highlight-num {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: $primary;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.suggestion-list { display: flex; flex-direction: column; gap: 8px; }

.suggestion-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 13px;
  color: #475569;
  padding: 8px 0;
  border-bottom: 1px solid $border;

  &:last-child { border-bottom: none; }

  .el-icon { flex-shrink: 0; margin-top: 1px; }
}

/* ─── 建议 ─── */
.tips-section {
  &__title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 700;
    color: #1e293b;
    margin-bottom: 16px;

    .el-icon { font-size: 18px; }
  }
}

.tips-content {
  font-size: 14px;
  line-height: 1.8;
  color: #475569;

  :deep(h2), :deep(h3) {
    font-size: 14px;
    font-weight: 700;
    color: $primary;
    margin: 14px 0 6px;
  }

  :deep(ul) { padding-left: 20px; }
  :deep(li) { margin-bottom: 4px; }
  :deep(strong) { color: #334155; }
}

/* ─── 元数据 ─── */
.meta-bar {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: #94a3b8;
  padding: 8px 4px;
}

@media (max-width: 1200px) {
  .ra-body {
    grid-template-columns: 1fr;
  }

  .form-row-3 {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 768px) {
  .ra-tabs { flex-wrap: wrap; }

  .form-row-2,
  .form-row-3 {
    grid-template-columns: 1fr;
  }
}
</style>
