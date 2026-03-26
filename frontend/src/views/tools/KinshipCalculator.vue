<template>
  <div class="kin-page">
    <div class="kin-header">
      <div class="kin-header__left">
        <div class="kin-badge">
          <el-icon :size="24"><Connection /></el-icon>
        </div>
        <div>
          <h2 class="kin-title">亲戚计算器</h2>
          <p class="kin-desc">
            输入一串中文亲属关系，实时得到称谓、推导过程和关系图谱。
          </p>
        </div>
      </div>

      <div class="kin-header__actions">
        <el-tag effect="dark" type="primary">本地计算</el-tag>
        <el-tag effect="plain">单页面展示</el-tag>
        <el-button plain @click="applySample(sampleExpressions[0])">
          <el-icon><MagicStick /></el-icon>
          试试示例
        </el-button>
      </div>
    </div>

    <div class="kin-hero">
      <section class="kin-panel kin-panel--input">
        <div class="panel-head">
          <div>
            <div class="panel-eyebrow">输入操作区</div>
            <h3>输入亲戚关系</h3>
          </div>
          <el-tag type="info" effect="plain">{{ normalizedPreview }}</el-tag>
        </div>

        <el-input
          v-model="inputText"
          type="textarea"
          :rows="5"
          resize="none"
          placeholder="例如：妈妈的哥哥的女儿"
          class="kin-input"
        />

        <div class="input-toolbar">
          <div class="toolbar-label">常用关系按钮</div>
          <div class="toolbar-tip">点击即可自动插入“的”连接关系</div>
        </div>

        <div class="button-groups">
          <div v-for="group in groupedButtons" :key="group.title" class="button-group">
            <div class="button-group__title">{{ group.title }}</div>
            <div class="button-group__items">
              <button
                v-for="item in group.items"
                :key="item.key"
                class="relation-chip"
                @click="insertRelation(item)"
              >
                {{ item.label }}
              </button>
            </div>
          </div>
        </div>

        <div class="sample-section">
          <div class="sample-section__head">
            <span>示例输入</span>
            <span>点击可一键填入</span>
          </div>
          <div class="sample-list">
            <button
              v-for="sample in sampleExpressions"
              :key="sample"
              class="sample-card"
              @click="applySample(sample)"
            >
              {{ sample }}
            </button>
          </div>
        </div>

        <div class="action-row">
          <el-button type="primary" @click="runAnalysis">
            <el-icon><Promotion /></el-icon>
            开始计算
          </el-button>
          <el-button plain @click="removeLastToken" :disabled="!inputText">
            <el-icon><RefreshRight /></el-icon>
            回退一步
          </el-button>
          <el-button plain @click="copySummary" :disabled="!hasResult">
            <el-icon><DocumentCopy /></el-icon>
            复制结果
          </el-button>
          <el-button type="danger" plain @click="clearAll" :disabled="!inputText && !hasResult">
            <el-icon><Delete /></el-icon>
            清空
          </el-button>
        </div>
      </section>

      <section ref="resultPanelRef" class="kin-panel kin-panel--result">
        <div class="panel-head">
          <div>
            <div class="panel-eyebrow">结果展示区</div>
            <h3>最终答案</h3>
          </div>
          <el-tag v-if="hasResult" :type="result.success ? (result.isAmbiguous ? 'warning' : 'success') : 'info'" effect="dark">
            {{ resultStatusText }}
          </el-tag>
        </div>

        <div v-if="hasResult" class="result-shell">
          <div :class="['result-hero', { 'result-hero--danger': !result.success, 'result-hero--warning': result.isAmbiguous }]">
            <div class="result-hero__label">计算结果</div>
            <div class="result-hero__value">{{ result.finalDisplay }}</div>
            <div class="result-hero__meta">
              <span>关系路径：{{ result.pathText }}</span>
            </div>
          </div>

          <el-alert
            :title="result.message"
            :type="result.success ? (result.isAmbiguous ? 'warning' : 'success') : 'error'"
            show-icon
            :closable="false"
          />

          <div class="token-trail">
            <div class="token-trail__title">关系拆解</div>
            <div class="trail-tags">
              <el-tag v-for="token in result.tokens" :key="`${token.key}-${token.raw}`" effect="plain">
                {{ token.meta.label }}
              </el-tag>
            </div>
          </div>

          <div v-if="result.finalCandidates.length > 1" class="candidate-box">
            <div class="candidate-box__title">可能结果</div>
            <div class="candidate-box__items">
              <span v-for="candidate in result.finalCandidates" :key="candidate" class="candidate-pill">
                {{ candidate }}
              </span>
            </div>
          </div>
        </div>

        <div v-else class="empty-state">
          <el-icon :size="42"><Guide /></el-icon>
          <h4>等待开始计算</h4>
          <p>输入或点选一串亲戚关系后，结果、步骤和图谱会一起出现在这里。</p>
        </div>
      </section>
    </div>

    <div class="kin-details">
      <section class="kin-panel">
        <div class="panel-head">
          <div>
            <div class="panel-eyebrow">分析过程区</div>
            <h3>逐步推导</h3>
          </div>
          <span class="panel-side-note">{{ result.steps.length }} 个步骤</span>
        </div>

        <div v-if="result.steps.length" class="steps-list">
          <div v-for="step in result.steps" :key="step.index" class="step-card">
            <div class="step-card__head">
              <div class="step-index">STEP {{ step.index }}</div>
              <el-tag effect="plain">{{ step.tokenLabel }}</el-tag>
            </div>

            <div class="step-grid">
              <div class="step-item">
                <span class="step-item__label">输入关系词</span>
                <strong>{{ step.tokenLabel }}</strong>
              </div>
              <div class="step-item">
                <span class="step-item__label">当前身份变化</span>
                <strong>{{ step.relationChange }}</strong>
              </div>
              <div class="step-item">
                <span class="step-item__label">关系方向</span>
                <strong>{{ step.direction }}</strong>
              </div>
              <div class="step-item">
                <span class="step-item__label">当前结果</span>
                <strong>{{ step.nextTitle }}</strong>
              </div>
            </div>

            <div class="step-explanation">
              {{ step.explanation }}
            </div>
          </div>
        </div>

        <div v-else class="empty-inline">
          计算后，这里会按步骤解释“每个关系词是怎么推导出下一层关系”的。
        </div>
      </section>

      <section class="kin-panel">
        <div class="panel-head">
          <div>
            <div class="panel-eyebrow">图谱展示区</div>
            <h3>关系图谱</h3>
          </div>
          <span class="panel-side-note">我 -> 中间关系 -> 最终关系</span>
        </div>

        <div v-if="graphNodes.length" class="graph-flow">
          <template v-for="(node, index) in graphNodes" :key="node.id">
            <div :class="['graph-node', `graph-node--${node.tone}`]">
              <div class="graph-node__title">{{ node.title }}</div>
              <div class="graph-node__subtitle">{{ node.subtitle }}</div>
            </div>
            <div v-if="index < graphNodes.length - 1" class="graph-arrow">
              <span></span>
            </div>
          </template>
        </div>

        <div v-else class="empty-inline">
          图谱会根据每一步推导自动生成，帮助你直观看到从“我”到目标称谓的路径。
        </div>
      </section>
    </div>

    <section class="kin-panel kin-panel--guide">
      <div class="panel-head">
        <div>
          <div class="panel-eyebrow">使用说明区</div>
          <h3>规则范围与说明</h3>
        </div>
      </div>

      <div class="guide-grid">
        <div class="guide-card">
          <div class="guide-card__title">当前支持</div>
          <ul>
            <li v-for="note in capabilityNotes" :key="note">{{ note }}</li>
          </ul>
        </div>
        <div class="guide-card">
          <div class="guide-card__title">输入建议</div>
          <ul>
            <li>优先使用“X的Y的Z”这种中文链式表达。</li>
            <li>如果某一步出现多个可能，结果区会展示全部合理候选。</li>
            <li>按钮插入适合逐步拼装关系，适合不会一次性完整输入的用户。</li>
          </ul>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Connection,
  Delete,
  DocumentCopy,
  Guide,
  MagicStick,
  Promotion,
  RefreshRight
} from '@element-plus/icons-vue'
import {
  analyzeKinship,
  capabilityNotes,
  relationButtonGroups,
  relationTokenMap,
  sampleExpressions
} from '@/utils/kinship'

const inputText = ref('')
const result = ref({
  success: false,
  normalizedInput: '',
  tokens: [],
  steps: [],
  finalDisplay: '',
  finalCandidates: [],
  isAmbiguous: false,
  pathText: '',
  graphNodes: [],
  message: ''
})
const resultPanelRef = ref(null)

const groupedButtons = computed(() => relationButtonGroups.map(group => ({
  ...group,
  items: group.keys.map(key => relationTokenMap[key])
})))

const normalizedPreview = computed(() => inputText.value.trim() ? inputText.value : '等待输入')
const hasResult = computed(() => Boolean(result.value.message || result.value.steps.length))
const graphNodes = computed(() => result.value.graphNodes || [])
const resultStatusText = computed(() => {
  if (!result.value.success) return '未完成'
  return result.value.isAmbiguous ? '多结果' : '已锁定'
})

const applySample = (sample) => {
  inputText.value = sample
}

const insertRelation = (relation) => {
  if (!relation?.label) return
  const value = inputText.value.trim()
  inputText.value = value ? `${value}的${relation.label}` : relation.label
}

const removeLastToken = () => {
  const value = inputText.value.trim()
  if (!value) return
  const parts = value.split('的').filter(Boolean)
  parts.pop()
  inputText.value = parts.join('的')
}

const clearAll = () => {
  inputText.value = ''
  result.value = {
    success: false,
    normalizedInput: '',
    tokens: [],
    steps: [],
    finalDisplay: '',
    finalCandidates: [],
    isAmbiguous: false,
    pathText: '',
    graphNodes: [],
    message: ''
  }
}

const scrollToResult = async () => {
  await nextTick()
  resultPanelRef.value?.scrollIntoView({
    behavior: 'smooth',
    block: 'start'
  })
}

const runAnalysis = async () => {
  if (!inputText.value.trim()) {
    ElMessage.warning('请先输入一串亲戚关系。')
    return
  }

  result.value = analyzeKinship(inputText.value)
  await scrollToResult()

  if (result.value.success) {
    ElMessage.success(result.value.isAmbiguous ? '计算完成，结果包含多种可能。' : '计算完成。')
  } else {
    ElMessage.error(result.value.message)
  }
}

const copySummary = async () => {
  if (!hasResult.value) return

  const lines = [
    `输入：${inputText.value}`,
    `规范化：${result.value.normalizedInput || '-'}`,
    `结果：${result.value.finalDisplay || '-'}`,
    `路径：${result.value.pathText || '-'}`,
    `说明：${result.value.message || '-'}`
  ]

  if (result.value.steps.length) {
    lines.push('步骤：')
    result.value.steps.forEach(step => {
      lines.push(`${step.index}. ${step.relationChange}`)
      lines.push(`   ${step.explanation}`)
    })
  }

  try {
    await navigator.clipboard.writeText(lines.join('\n'))
    ElMessage.success('结果已复制到剪贴板。')
  } catch (error) {
    ElMessage.error('复制失败，请稍后重试。')
  }
}
</script>

<style lang="scss" scoped>
.kin-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 100%;
}

.kin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;

  &__left,
  &__actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__actions {
    flex-wrap: wrap;
    justify-content: flex-end;
  }
}

.kin-badge {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  background:
    radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.32), transparent 35%),
    linear-gradient(145deg, #f97316, #ea580c 50%, #c2410c);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 14px 28px rgba(234, 88, 12, 0.24);
}

.kin-title {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  color: #111827;
}

.kin-desc {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 13px;
}

.kin-hero,
.kin-details {
  display: grid;
  gap: 20px;
}

.kin-hero {
  grid-template-columns: minmax(0, 1.15fr) minmax(340px, 0.85fr);
}

.kin-details {
  grid-template-columns: minmax(0, 1fr) minmax(0, 0.95fr);
}

.kin-panel {
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(255, 247, 237, 0.92)),
    linear-gradient(120deg, rgba(251, 146, 60, 0.06), transparent 55%);
  border: 1px solid rgba(251, 146, 60, 0.14);
  border-radius: 24px;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.06);
  padding: 22px;
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;

  h3 {
    margin: 4px 0 0;
    font-size: 20px;
    color: #111827;
  }
}

.panel-eyebrow {
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #f97316;
}

.panel-side-note {
  color: #94a3b8;
  font-size: 12px;
}

.kin-input {
  :deep(.el-textarea__inner) {
    min-height: 132px;
    border-radius: 18px;
    line-height: 1.8;
    padding: 14px 16px;
    font-size: 15px;
  }
}

.input-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 18px 0 12px;
}

.toolbar-label {
  color: #334155;
  font-weight: 700;
}

.toolbar-tip {
  color: #94a3b8;
  font-size: 12px;
}

.button-groups {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.button-group {
  background: rgba(255, 255, 255, 0.66);
  border: 1px solid rgba(148, 163, 184, 0.15);
  border-radius: 18px;
  padding: 14px;

  &__title {
    font-size: 13px;
    font-weight: 700;
    color: #334155;
    margin-bottom: 10px;
  }

  &__items {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
}

.relation-chip {
  border: none;
  border-radius: 999px;
  padding: 8px 14px;
  font-size: 13px;
  font-weight: 600;
  color: #9a3412;
  background: rgba(255, 237, 213, 0.92);
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 18px rgba(251, 146, 60, 0.18);
    background: #fdba74;
    color: #7c2d12;
  }
}

.sample-section {
  margin-top: 18px;
  padding: 16px;
  border-radius: 20px;
  background:
    linear-gradient(135deg, rgba(255, 247, 237, 0.95), rgba(255, 255, 255, 0.9)),
    repeating-linear-gradient(-45deg, rgba(251, 146, 60, 0.06) 0 10px, transparent 10px 20px);
  border: 1px dashed rgba(249, 115, 22, 0.24);

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 12px;
    color: #475569;
    font-size: 13px;
    font-weight: 700;
  }
}

.sample-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.sample-card {
  text-align: left;
  border: 1px solid rgba(251, 146, 60, 0.16);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.92);
  padding: 12px 14px;
  color: #1f2937;
  cursor: pointer;
  transition: transform 0.18s ease, border-color 0.18s ease;

  &:hover {
    transform: translateY(-2px);
    border-color: rgba(249, 115, 22, 0.4);
  }
}

.action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

.result-shell {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.result-hero {
  padding: 18px;
  border-radius: 22px;
  color: #fff;
  background:
    radial-gradient(circle at top right, rgba(255, 255, 255, 0.28), transparent 24%),
    linear-gradient(135deg, #0f766e, #14b8a6 58%, #2dd4bf);
  box-shadow: 0 20px 36px rgba(20, 184, 166, 0.2);

  &--warning {
    background:
      radial-gradient(circle at top right, rgba(255, 255, 255, 0.26), transparent 24%),
      linear-gradient(135deg, #b45309, #f59e0b 58%, #fbbf24);
    box-shadow: 0 20px 36px rgba(245, 158, 11, 0.22);
  }

  &--danger {
    background:
      radial-gradient(circle at top right, rgba(255, 255, 255, 0.24), transparent 24%),
      linear-gradient(135deg, #991b1b, #ef4444 58%, #f87171);
    box-shadow: 0 20px 36px rgba(239, 68, 68, 0.2);
  }

  &__label {
    font-size: 12px;
    letter-spacing: 0.08em;
    text-transform: uppercase;
    opacity: 0.85;
  }

  &__value {
    margin-top: 8px;
    font-size: 32px;
    line-height: 1.2;
    font-weight: 800;
  }

  &__meta {
    margin-top: 10px;
    font-size: 13px;
    opacity: 0.9;
  }
}

.token-trail,
.candidate-box {
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(148, 163, 184, 0.14);
  border-radius: 18px;
  padding: 14px;
}

.token-trail__title,
.candidate-box__title {
  color: #475569;
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 10px;
}

.trail-tags,
.candidate-box__items {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.candidate-pill {
  display: inline-flex;
  align-items: center;
  padding: 7px 12px;
  border-radius: 999px;
  background: rgba(254, 240, 138, 0.52);
  color: #854d0e;
  font-size: 13px;
  font-weight: 700;
}

.empty-state {
  min-height: 320px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 12px;
  color: #94a3b8;
  text-align: center;

  h4 {
    margin: 0;
    color: #334155;
  }

  p {
    margin: 0;
    max-width: 320px;
    line-height: 1.7;
  }
}

.steps-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.step-card {
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(148, 163, 184, 0.14);
  padding: 16px;

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
    margin-bottom: 14px;
  }
}

.step-index {
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.12em;
  color: #f97316;
}

.step-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.step-item {
  padding: 12px;
  border-radius: 16px;
  background: #f8fafc;
  display: flex;
  flex-direction: column;
  gap: 6px;

  strong {
    color: #0f172a;
    line-height: 1.6;
  }

  &__label {
    font-size: 12px;
    color: #64748b;
  }
}

.step-explanation {
  margin-top: 14px;
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(255, 247, 237, 0.76);
  color: #7c2d12;
  line-height: 1.75;
  font-size: 14px;
}

.graph-flow {
  display: flex;
  align-items: center;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 6px;
}

.graph-node {
  min-width: 170px;
  padding: 16px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.18);
  box-shadow: 0 10px 20px rgba(15, 23, 42, 0.05);

  &--start {
    background: linear-gradient(135deg, rgba(251, 191, 36, 0.16), rgba(255, 255, 255, 0.96));
    border-color: rgba(245, 158, 11, 0.28);
  }

  &--end {
    background: linear-gradient(135deg, rgba(20, 184, 166, 0.14), rgba(255, 255, 255, 0.96));
    border-color: rgba(20, 184, 166, 0.28);
  }

  &__title {
    font-size: 18px;
    font-weight: 800;
    color: #111827;
  }

  &__subtitle {
    margin-top: 6px;
    color: #64748b;
    font-size: 13px;
    line-height: 1.6;
  }
}

.graph-arrow {
  flex: 0 0 38px;
  display: flex;
  justify-content: center;

  span {
    position: relative;
    width: 38px;
    height: 2px;
    background: linear-gradient(90deg, #fb923c, #f97316);

    &::after {
      content: '';
      position: absolute;
      right: -2px;
      top: -4px;
      border-top: 5px solid transparent;
      border-bottom: 5px solid transparent;
      border-left: 9px solid #f97316;
    }
  }
}

.kin-panel--guide {
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.95)),
    radial-gradient(circle at top left, rgba(20, 184, 166, 0.08), transparent 28%);
}

.guide-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.guide-card {
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(148, 163, 184, 0.14);

  &__title {
    margin-bottom: 12px;
    font-size: 15px;
    font-weight: 800;
    color: #0f172a;
  }

  ul {
    margin: 0;
    padding-left: 18px;
    color: #475569;
    line-height: 1.8;
  }
}

.empty-inline {
  color: #94a3b8;
  line-height: 1.7;
  padding: 10px 0;
}

@media (max-width: 1180px) {
  .kin-hero,
  .kin-details,
  .guide-grid {
    grid-template-columns: 1fr;
  }

  .sample-list {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .kin-header,
  .kin-header__left {
    flex-direction: column;
    align-items: flex-start;
  }

  .kin-header__actions {
    justify-content: flex-start;
  }

  .input-toolbar,
  .panel-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .step-grid {
    grid-template-columns: 1fr;
  }

  .graph-flow {
    flex-direction: column;
    align-items: stretch;
  }

  .graph-arrow {
    flex: 0 0 auto;
    width: 100%;
    height: 24px;
    align-items: center;

    span {
      width: 2px;
      height: 24px;
      background: linear-gradient(180deg, #fb923c, #f97316);

      &::after {
        right: -4px;
        top: auto;
        bottom: -2px;
        border-left: 5px solid transparent;
        border-right: 5px solid transparent;
        border-top: 9px solid #f97316;
        border-bottom: none;
      }
    }
  }
}
</style>
