<template>
  <div class="password-page">
    <section class="password-hero">
      <div class="password-hero__copy">
        <div class="hero-kicker">SECURE TOOLBOX</div>
        <h2 class="hero-title">密码生成器</h2>
        <p class="hero-desc">
          在浏览器本地生成高强度密码，支持长度调节、字符集组合、排除易混淆字符和批量生成。
        </p>

        <div class="hero-tags">
          <el-tag effect="dark" type="primary">本地生成</el-tag>
          <el-tag effect="plain">安全随机源</el-tag>
          <el-tag effect="plain">无需后端接口</el-tag>
        </div>
      </div>

      <div class="password-hero__preview">
        <div class="preview-shell">
          <div class="preview-shell__label">最新生成</div>
          <div class="preview-shell__value">{{ primaryPassword?.value || '点击下方按钮立即生成' }}</div>
          <div class="preview-shell__meta">
            <span>{{ strengthMeta.label }}</span>
            <span>{{ entropyLabel }}</span>
          </div>
        </div>
      </div>
    </section>

    <div class="password-grid">
      <section class="panel panel--controls">
        <div class="panel-head">
          <div>
            <div class="panel-eyebrow">配置区</div>
            <h3>生成规则</h3>
          </div>
          <el-tag type="info" effect="plain">字符池 {{ poolSize }}</el-tag>
        </div>

        <div class="control-card">
          <div class="control-row">
            <div>
              <div class="control-label">密码长度</div>
              <div class="control-hint">建议至少 12 位，重要账户建议 16 位以上。</div>
            </div>
            <strong class="control-value">{{ length }}</strong>
          </div>

          <el-slider v-model="length" :min="6" :max="64" :step="1" />

          <div class="quick-lengths">
            <button
              v-for="size in quickLengths"
              :key="size"
              :class="['quick-length', { active: length === size }]"
              type="button"
              @click="length = size"
            >
              {{ size }}
            </button>
          </div>
        </div>

        <div class="option-grid">
          <label class="option-card">
            <div class="option-card__copy">
              <span class="option-card__title">小写字母</span>
              <span class="option-card__desc">包含 `a-z`</span>
            </div>
            <el-switch v-model="includeLowercase" />
          </label>

          <label class="option-card">
            <div class="option-card__copy">
              <span class="option-card__title">大写字母</span>
              <span class="option-card__desc">包含 `A-Z`</span>
            </div>
            <el-switch v-model="includeUppercase" />
          </label>

          <label class="option-card">
            <div class="option-card__copy">
              <span class="option-card__title">数字</span>
              <span class="option-card__desc">包含 `0-9`</span>
            </div>
            <el-switch v-model="includeNumbers" />
          </label>

          <label class="option-card">
            <div class="option-card__copy">
              <span class="option-card__title">符号</span>
              <span class="option-card__desc">包含常见安全符号</span>
            </div>
            <el-switch v-model="includeSymbols" />
          </label>
        </div>

        <div class="advanced-list">
          <label class="advanced-item">
            <div>
              <div class="advanced-item__title">排除易混淆字符</div>
              <div class="advanced-item__desc">过滤 `0`、`O`、`I`、`l`、`1` 等容易看错的字符。</div>
            </div>
            <el-switch v-model="excludeAmbiguous" />
          </label>

          <label class="advanced-item">
            <div>
              <div class="advanced-item__title">每类至少出现一次</div>
              <div class="advanced-item__desc">如果已选择多个字符类型，会保证每一类至少出现一次。</div>
            </div>
            <el-switch v-model="ensureEveryType" />
          </label>
        </div>

        <div class="control-card">
          <div class="control-row">
            <div>
              <div class="control-label">批量生成</div>
              <div class="control-hint">一次生成多组，便于挑选或备用。</div>
            </div>
            <el-select v-model="batchCount" style="width: 120px">
              <el-option v-for="count in [1, 3, 5, 8]" :key="count" :label="`${count} 组`" :value="count" />
            </el-select>
          </div>
        </div>

        <div class="pool-card">
          <div class="pool-card__head">
            <span>当前字符集</span>
            <span>{{ selectedCategoryLabels }}</span>
          </div>
          <div class="pool-card__value">{{ poolPreview || '请至少选择一种字符类型' }}</div>
        </div>

        <el-alert
          v-if="!canGenerate"
          title="请至少保留一种字符类型，否则无法生成密码。"
          type="warning"
          :closable="false"
          show-icon
        />

        <div class="action-row">
          <el-button type="primary" :disabled="!canGenerate" @click="generatePasswords">
            <el-icon><RefreshRight /></el-icon>
            立即生成
          </el-button>
          <el-button plain :disabled="!primaryPassword" @click="copyPassword(primaryPassword?.value)">
            <el-icon><CopyDocument /></el-icon>
            复制当前
          </el-button>
          <el-button plain :disabled="generatedPasswords.length === 0" @click="copyAllPasswords">
            <el-icon><DocumentCopy /></el-icon>
            复制全部
          </el-button>
        </div>
      </section>

      <section class="panel panel--result">
        <div class="panel-head">
          <div>
            <div class="panel-eyebrow">结果区</div>
            <h3>生成结果</h3>
          </div>
          <el-tag :color="strengthMeta.color" effect="dark">{{ strengthMeta.label }}</el-tag>
        </div>

        <div class="result-hero" :style="{ '--strength-color': strengthMeta.color }">
          <div class="result-hero__label">主密码</div>
          <div class="result-hero__password">{{ primaryPassword?.value || '等待生成' }}</div>

          <div class="result-hero__metrics">
            <div class="metric-pill">
              <span>长度</span>
              <strong>{{ length }}</strong>
            </div>
            <div class="metric-pill">
              <span>熵估算</span>
              <strong>{{ entropyLabel }}</strong>
            </div>
            <div class="metric-pill">
              <span>模式</span>
              <strong>{{ generationModeLabel }}</strong>
            </div>
          </div>

          <div class="strength-track">
            <div class="strength-track__bar" :style="{ width: `${strengthMeta.percent}%` }" />
          </div>

          <p class="result-hero__hint">{{ strengthMeta.hint }}</p>
        </div>

        <div class="summary-grid">
          <article class="summary-card">
            <span class="summary-card__label">字符池大小</span>
            <strong class="summary-card__value">{{ poolSize }}</strong>
            <span class="summary-card__desc">当前选项合并后的可选字符数</span>
          </article>
          <article class="summary-card">
            <span class="summary-card__label">已选类别</span>
            <strong class="summary-card__value">{{ selectedCategoryCount }}</strong>
            <span class="summary-card__desc">小写、大写、数字、符号的启用数量</span>
          </article>
          <article class="summary-card">
            <span class="summary-card__label">生成方式</span>
            <strong class="summary-card__value">{{ batchCount }} 组</strong>
            <span class="summary-card__desc">一次输出多组候选密码</span>
          </article>
        </div>

        <div class="result-list">
          <div class="result-list__head">
            <span>候选密码</span>
            <span>{{ generatedPasswords.length }} / {{ batchCount }}</span>
          </div>

          <button
            v-for="(item, index) in generatedPasswords"
            :key="item.id"
            class="password-item"
            type="button"
            @click="copyPassword(item.value)"
          >
            <div class="password-item__index">#{{ index + 1 }}</div>
            <div class="password-item__value">{{ item.value }}</div>
            <div class="password-item__action">
              <el-icon><CopyDocument /></el-icon>
            </div>
          </button>

          <div v-if="generatedPasswords.length === 0" class="empty-state">
            <el-icon :size="40"><Lock /></el-icon>
            <p>还没有生成结果，先在左侧配置规则并点击“立即生成”。</p>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  CopyDocument,
  DocumentCopy,
  Lock,
  RefreshRight
} from '@element-plus/icons-vue'

const LOWERCASE = 'abcdefghijklmnopqrstuvwxyz'
const UPPERCASE = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
const NUMBERS = '0123456789'
const SYMBOLS = '!@#$%^&*()-_=+[]{}?/~'
const AMBIGUOUS = new Set(['0', 'O', 'o', 'I', 'l', '1'])
const quickLengths = [12, 16, 20, 24, 32]

const length = ref(18)
const batchCount = ref(5)
const includeLowercase = ref(true)
const includeUppercase = ref(true)
const includeNumbers = ref(true)
const includeSymbols = ref(true)
const excludeAmbiguous = ref(true)
const ensureEveryType = ref(true)
const generatedPasswords = ref([])

const normalizeCharset = (value) => {
  if (!excludeAmbiguous.value) return value
  return [...value].filter(char => !AMBIGUOUS.has(char)).join('')
}

const selectedCategories = computed(() => {
  const categories = []

  if (includeLowercase.value) {
    categories.push({ key: 'lowercase', label: '小写', chars: normalizeCharset(LOWERCASE) })
  }
  if (includeUppercase.value) {
    categories.push({ key: 'uppercase', label: '大写', chars: normalizeCharset(UPPERCASE) })
  }
  if (includeNumbers.value) {
    categories.push({ key: 'numbers', label: '数字', chars: normalizeCharset(NUMBERS) })
  }
  if (includeSymbols.value) {
    categories.push({ key: 'symbols', label: '符号', chars: SYMBOLS })
  }

  return categories.filter(item => item.chars.length > 0)
})

const selectedCategoryCount = computed(() => selectedCategories.value.length)
const canGenerate = computed(() => selectedCategoryCount.value > 0)
const selectedCategoryLabels = computed(() => selectedCategories.value.map(item => item.label).join(' / ') || '未选择')
const pool = computed(() => selectedCategories.value.map(item => item.chars).join(''))
const poolSize = computed(() => pool.value.length)
const poolPreview = computed(() => pool.value.slice(0, 80))
const entropyEstimate = computed(() => {
  if (!poolSize.value || !length.value) return 0
  return length.value * Math.log2(poolSize.value)
})
const entropyLabel = computed(() => `${entropyEstimate.value.toFixed(1)} bits`)
const primaryPassword = computed(() => generatedPasswords.value[0] || null)
const generationModeLabel = computed(() => ensureEveryType.value ? '均衡覆盖' : '纯随机池')

const strengthMeta = computed(() => {
  const entropy = entropyEstimate.value

  if (!primaryPassword.value) {
    return {
      label: '待生成',
      color: '#64748b',
      percent: 0,
      hint: '生成后会根据当前长度和字符池给出强度参考。'
    }
  }

  if (entropy < 45) {
    return {
      label: '较弱',
      color: '#ef4444',
      percent: 28,
      hint: '长度或字符种类偏少，适合临时用途，不建议用于重要账户。'
    }
  }

  if (entropy < 65) {
    return {
      label: '中等',
      color: '#f59e0b',
      percent: 56,
      hint: '已经具备一定强度，若用于核心账号，建议继续增加长度。'
    }
  }

  if (entropy < 90) {
    return {
      label: '强',
      color: '#0f9f6e',
      percent: 82,
      hint: '长度和字符复杂度比较均衡，适合大多数正式账户使用。'
    }
  }

  return {
    label: '极强',
    color: '#2563eb',
    percent: 100,
    hint: '已经达到很高的随机性，适合高敏感场景或密码管理器保存。'
  }
})

const getCryptoApi = () => {
  if (!window.crypto?.getRandomValues) {
    throw new Error('当前浏览器不支持安全随机数生成。')
  }
  return window.crypto
}

const getRandomInt = (max) => {
  if (max <= 0) return 0

  const cryptoApi = getCryptoApi()
  const array = new Uint32Array(1)
  const maxUint32 = 0x100000000
  const limit = maxUint32 - (maxUint32 % max)

  // Rejection sampling avoids modulo bias when the range does not evenly divide 2^32.
  do {
    cryptoApi.getRandomValues(array)
  } while (array[0] >= limit)

  return array[0] % max
}

const pickChar = (source) => source[getRandomInt(source.length)]

const shuffleChars = (chars) => {
  const result = [...chars]
  for (let index = result.length - 1; index > 0; index -= 1) {
    const swapIndex = getRandomInt(index + 1)
    ;[result[index], result[swapIndex]] = [result[swapIndex], result[index]]
  }
  return result
}

const buildPassword = () => {
  if (!canGenerate.value) {
    throw new Error('请至少选择一种字符类型。')
  }

  if (ensureEveryType.value && length.value < selectedCategoryCount.value) {
    throw new Error(`当前长度至少需要 ${selectedCategoryCount.value} 位，才能覆盖所有已选字符类型。`)
  }

  const chars = []

  if (ensureEveryType.value) {
    selectedCategories.value.forEach(category => {
      chars.push(pickChar(category.chars))
    })
  }

  while (chars.length < length.value) {
    chars.push(pickChar(pool.value))
  }

  return shuffleChars(chars).join('')
}

const generatePasswords = () => {
  try {
    const timestamp = Date.now()
    generatedPasswords.value = Array.from({ length: batchCount.value }, (_, index) => ({
      id: `${timestamp}-${index}`,
      value: buildPassword()
    }))
    ElMessage.success(`已生成 ${batchCount.value} 组密码`)
  } catch (error) {
    ElMessage.error(error.message || '生成失败，请稍后重试')
  }
}

const copyPassword = async (password) => {
  if (!password) return

  try {
    await navigator.clipboard.writeText(password)
    ElMessage.success('密码已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败，请检查浏览器剪贴板权限')
  }
}

const copyAllPasswords = async () => {
  if (!generatedPasswords.value.length) return

  try {
    await navigator.clipboard.writeText(generatedPasswords.value.map(item => item.value).join('\n'))
    ElMessage.success('所有密码已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败，请稍后重试')
  }
}

watch([selectedCategoryCount, ensureEveryType], () => {
  if (ensureEveryType.value && length.value < selectedCategoryCount.value) {
    length.value = selectedCategoryCount.value
  }
})

onMounted(() => {
  generatePasswords()
})
</script>

<style lang="scss" scoped>
.password-page {
  display: flex;
  flex-direction: column;
  gap: 22px;
  min-height: 100%;
}

.password-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(280px, 0.8fr);
  gap: 20px;
  padding: 24px 28px;
  border-radius: var(--radius-lg);
  background:
    radial-gradient(circle at top left, rgba(20, 184, 166, 0.18), transparent 28%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(240, 249, 255, 0.96));
  border: 1px solid rgba(37, 99, 235, 0.12);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08);
}

.hero-kicker,
.panel-eyebrow {
  color: #0f766e;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.hero-title {
  margin: 10px 0 0;
  color: #0f172a;
  font-size: 2rem;
  font-weight: 700;
  font-family: var(--font-display);
  letter-spacing: -0.04em;
}

.hero-desc {
  margin: 12px 0 0;
  max-width: 620px;
  color: #475569;
  line-height: 1.8;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

.password-hero__preview {
  display: flex;
  align-items: stretch;
}

.preview-shell {
  width: 100%;
  padding: 22px;
  border-radius: var(--radius-lg);
  background:
    linear-gradient(145deg, rgba(15, 23, 42, 0.96), rgba(30, 41, 59, 0.94)),
    radial-gradient(circle at top right, rgba(45, 212, 191, 0.2), transparent 30%);
  color: #e2e8f0;
  box-shadow: 0 22px 36px rgba(15, 23, 42, 0.18);
}

.preview-shell__label {
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: rgba(226, 232, 240, 0.72);
}

.preview-shell__value {
  margin-top: 12px;
  font-family: var(--font-mono);
  font-size: 1.2rem;
  line-height: 1.6;
  word-break: break-all;
}

.preview-shell__meta {
  display: flex;
  gap: 14px;
  margin-top: 18px;
  color: #93c5fd;
  font-size: 13px;
}

.password-grid {
  display: grid;
  grid-template-columns: minmax(0, 0.95fr) minmax(0, 1.05fr);
  gap: 20px;
  min-height: 0;
}

.panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 22px;
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-sm);
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;

  h3 {
    margin: 6px 0 0;
    color: #0f172a;
    font-size: 1.35rem;
  }
}

.control-card,
.pool-card {
  padding: 18px;
  border-radius: var(--radius-md);
  background:
    linear-gradient(180deg, rgba(248, 250, 252, 0.96), rgba(255, 255, 255, 0.94));
  border: 1px solid rgba(148, 163, 184, 0.14);
}

.control-row,
.pool-card__head,
.result-list__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.control-label {
  color: #0f172a;
  font-weight: 800;
}

.control-hint,
.pool-card__head,
.summary-card__desc,
.result-hero__hint {
  color: #64748b;
  font-size: 13px;
  line-height: 1.7;
}

.control-value {
  color: #0f766e;
  font-size: 1.8rem;
  font-weight: 900;
  font-family: var(--font-mono);
}

.quick-lengths {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.quick-length {
  border: 1px solid rgba(15, 118, 110, 0.16);
  border-radius: 999px;
  background: #fff;
  color: #0f172a;
  font-size: 13px;
  font-weight: 700;
  padding: 8px 14px;
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}

.quick-length:hover,
.quick-length.active {
  transform: translateY(-1px);
  border-color: rgba(15, 118, 110, 0.42);
  background: rgba(204, 251, 241, 0.68);
  color: #0f766e;
}

.option-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.option-card,
.advanced-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 16px 18px;
  border-radius: var(--radius-md);
  border: 1px solid rgba(148, 163, 184, 0.14);
  background: rgba(255, 255, 255, 0.94);
}

.option-card__copy,
.advanced-item > div {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.option-card__title,
.advanced-item__title {
  color: #0f172a;
  font-weight: 800;
}

.option-card__desc,
.advanced-item__desc {
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.advanced-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pool-card__value {
  margin-top: 14px;
  padding: 14px 16px;
  border-radius: var(--radius-sm);
  background: rgba(15, 23, 42, 0.94);
  color: #cbd5e1;
  font-family: var(--font-mono);
  line-height: 1.75;
  word-break: break-all;
}

.action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.result-hero {
  padding: 22px;
  border-radius: var(--radius-lg);
  background:
    radial-gradient(circle at top right, rgba(255, 255, 255, 0.18), transparent 28%),
    linear-gradient(135deg, color-mix(in srgb, var(--strength-color) 86%, #0f172a), #0f172a 78%);
  color: #f8fafc;
  box-shadow: 0 18px 34px color-mix(in srgb, var(--strength-color) 28%, transparent);
}

.result-hero__label {
  color: rgba(248, 250, 252, 0.72);
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.result-hero__password {
  margin-top: 12px;
  font-family: var(--font-mono);
  font-size: 1.42rem;
  font-weight: 700;
  line-height: 1.7;
  word-break: break-all;
}

.result-hero__metrics {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

.metric-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  color: rgba(248, 250, 252, 0.92);
  font-size: 13px;

  strong {
    font-size: 13px;
  }
}

.strength-track {
  width: 100%;
  height: 10px;
  margin-top: 18px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  overflow: hidden;
}

.strength-track__bar {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.72), #ffffff);
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.summary-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px;
  border-radius: var(--radius-md);
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.95), rgba(255, 255, 255, 0.96));
  border: 1px solid rgba(148, 163, 184, 0.14);
}

.summary-card__label {
  color: #64748b;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.summary-card__value {
  color: #0f172a;
  font-size: 1.38rem;
  font-weight: 900;
  font-family: var(--font-mono);
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.password-item {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 14px;
  width: 100%;
  padding: 16px 18px;
  border: 1px solid rgba(148, 163, 184, 0.14);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.96);
  text-align: left;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.password-item:hover {
  transform: translateY(-2px);
  border-color: rgba(37, 99, 235, 0.22);
  box-shadow: 0 14px 24px rgba(15, 23, 42, 0.06);
}

.password-item__index {
  color: #0f766e;
  font-weight: 800;
}

.password-item__value {
  font-family: var(--font-mono);
  color: #0f172a;
  line-height: 1.7;
  word-break: break-all;
}

.password-item__action {
  color: #64748b;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-height: 220px;
  border-radius: var(--radius-lg);
  border: 1px dashed rgba(148, 163, 184, 0.24);
  color: #94a3b8;
  text-align: center;

  p {
    max-width: 260px;
    margin: 0;
    line-height: 1.7;
  }
}

@media (max-width: 1180px) {
  .password-hero,
  .password-grid {
    grid-template-columns: 1fr;
  }

  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .password-hero,
  .panel {
    padding: 18px;
  }

  .option-grid,
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .control-row,
  .pool-card__head,
  .result-list__head,
  .panel-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
