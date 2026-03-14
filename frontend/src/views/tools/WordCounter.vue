<template>
  <div class="wc-page">

    <!-- 页头 -->
    <div class="wc-header">
      <div class="wc-header__left">
        <div class="wc-icon-badge">
          <el-icon :size="22"><EditPen /></el-icon>
        </div>
        <div>
          <h2 class="wc-title">字数统计器</h2>
          <p class="wc-desc">实时分析文本 · 多维度统计 · 阅读时间估算 · 关键词提取</p>
        </div>
      </div>
      <div class="wc-header__actions">
        <el-tooltip content="复制统计结果" placement="bottom">
          <el-button circle @click="copyStats" :disabled="!text">
            <el-icon><CopyDocument /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="粘贴剪贴板" placement="bottom">
          <el-button circle type="primary" plain @click="pasteFromClipboard">
            <el-icon><DocumentAdd /></el-icon>
          </el-button>
        </el-tooltip>
        <el-button type="danger" plain @click="clearText" :disabled="!text">
          <el-icon><Delete /></el-icon>清空
        </el-button>
      </div>
    </div>

    <!-- 主体：左侧编辑 / 右侧统计 -->
    <div class="wc-body">

      <!-- 编辑区 -->
      <div class="wc-editor-col">
        <div class="wc-editor-wrap">
          <div class="wc-editor-toolbar">
            <span class="toolbar-label">文本输入</span>
            <div class="toolbar-right">
              <el-tag size="small" type="info">{{ totalChars }} 字符</el-tag>
              <el-select v-model="targetLen" size="small" style="width:120px">
                <el-option label="无目标" :value="0" />
                <el-option label="目标 100 字" :value="100" />
                <el-option label="目标 300 字" :value="300" />
                <el-option label="目标 500 字" :value="500" />
                <el-option label="目标 1000 字" :value="1000" />
                <el-option label="目标 3000 字" :value="3000" />
              </el-select>
            </div>
          </div>

          <!-- 目标进度条 -->
          <div v-if="targetLen > 0" class="wc-progress">
            <el-progress
              :percentage="Math.min(100, Math.round(nonWhitespaceChars / targetLen * 100))"
              :status="nonWhitespaceChars >= targetLen ? 'success' : ''"
              :stroke-width="6"
              :color="progressColor"
            />
            <span class="progress-hint">
              {{ nonWhitespaceChars >= targetLen
                ? `已达目标，超出 ${nonWhitespaceChars - targetLen} 字`
                : `还差 ${targetLen - nonWhitespaceChars} 字` }}
            </span>
          </div>

          <el-input
            v-model="text"
            type="textarea"
            :rows="18"
            resize="none"
            placeholder="在此输入或粘贴文本，所有统计将实时更新..."
            class="wc-textarea"
          />

          <!-- 搜索高亮 -->
          <div class="wc-search-row">
            <el-input
              v-model="searchKeyword"
              placeholder="输入关键词高亮统计..."
              clearable
              size="small"
              prefix-icon="Search"
              class="search-input"
            />
            <el-tag v-if="searchKeyword && searchCount > 0" type="warning" size="small">
              共出现 {{ searchCount }} 次
            </el-tag>
            <el-tag v-else-if="searchKeyword && searchCount === 0" type="danger" size="small">
              未找到
            </el-tag>
          </div>
        </div>
      </div>

      <!-- 统计面板 -->
      <div class="wc-stats-col">

        <!-- 核心指标 -->
        <div class="stats-section">
          <div class="stats-section__title">核心指标</div>
          <div class="core-grid">
            <div
              v-for="item in coreStats"
              :key="item.label"
              class="core-item"
              :style="{ '--accent': item.color }"
            >
              <div class="core-item__icon">
                <el-icon :size="18"><component :is="item.icon" /></el-icon>
              </div>
              <div class="core-item__data">
                <div class="core-item__value">{{ item.value }}</div>
                <div class="core-item__label">{{ item.label }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 字符构成 -->
        <div class="stats-section">
          <div class="stats-section__title">字符构成</div>
          <div class="compose-list">
            <div v-for="item in composeStats" :key="item.label" class="compose-item">
              <div class="compose-item__head">
                <span class="compose-item__dot" :style="{ background: item.color }"></span>
                <span class="compose-item__label">{{ item.label }}</span>
                <span class="compose-item__value">{{ item.value }}</span>
                <span class="compose-item__pct">{{ item.pct }}%</span>
              </div>
              <el-progress
                :percentage="Number(item.pct)"
                :show-text="false"
                :stroke-width="5"
                :color="item.color"
              />
            </div>
          </div>
        </div>

        <!-- 阅读信息 -->
        <div class="stats-section">
          <div class="stats-section__title">阅读 & 说话</div>
          <div class="read-grid">
            <div class="read-item">
              <el-icon color="#6366f1"><Clock /></el-icon>
              <div>
                <div class="read-item__val">{{ readingTime }}</div>
                <div class="read-item__lbl">默读时长</div>
              </div>
            </div>
            <div class="read-item">
              <el-icon color="#10b981"><Mic /></el-icon>
              <div>
                <div class="read-item__val">{{ speakingTime }}</div>
                <div class="read-item__lbl">朗读时长</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Top 关键词 -->
        <div class="stats-section" v-if="topWords.length > 0">
          <div class="stats-section__title">高频词 Top 10</div>
          <div class="keyword-list">
            <div v-for="(item, idx) in topWords" :key="item.word" class="keyword-item">
              <span class="keyword-rank" :class="{ top3: idx < 3 }">{{ idx + 1 }}</span>
              <span class="keyword-word">{{ item.word }}</span>
              <el-progress
                class="keyword-bar"
                :percentage="Math.round(item.count / topWords[0].count * 100)"
                :show-text="false"
                :stroke-width="4"
                color="#6366f1"
              />
              <span class="keyword-count">{{ item.count }}</span>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="!text" class="empty-hint">
          <el-icon :size="40" color="#cbd5e1"><EditPen /></el-icon>
          <p>开始输入文本，统计结果将在此实时显示</p>
        </div>

      </div>
    </div>

  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  EditPen, CopyDocument, DocumentAdd, Delete, Clock
} from '@element-plus/icons-vue'

// ── 图标占位（Element Plus 无 Mic，用 Microphone 代替或手写 svg inline）
// 用一个 inline functional 组件模拟麦克风图标
const Mic = { template: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/><line x1="12" y1="19" x2="12" y2="23"/><line x1="8" y1="23" x2="16" y2="23"/></svg>` }

const text = ref('')
const targetLen = ref(0)
const searchKeyword = ref('')

// ── 基础统计
const totalChars = computed(() => text.value.length)
const nonWhitespaceChars = computed(() => text.value.replace(/\s/g, '').length)
const chineseChars = computed(() => (text.value.match(/[\u4e00-\u9fa5]/g) || []).length)
const englishLetters = computed(() => (text.value.match(/[A-Za-z]/g) || []).length)
const digits = computed(() => (text.value.match(/\d/g) || []).length)
const punctuation = computed(() => (text.value.match(/[^\w\s\u4e00-\u9fa5]/g) || []).length)
const spaces = computed(() => (text.value.match(/\s/g) || []).length)
const words = computed(() => (text.value.match(/[A-Za-z]+(?:[-'][A-Za-z]+)*/g) || []).length)
const sentences = computed(() => {
  if (!text.value.trim()) return 0
  return (text.value.match(/[。！？…!?]+|[.]+(?:\s|$)/g) || []).length || (text.value.trim() ? 1 : 0)
})
const lines = computed(() => text.value ? text.value.split(/\r?\n/).length : 0)
const paragraphs = computed(() => {
  const c = text.value.trim()
  if (!c) return 0
  return c.split(/\n\s*\n/).filter(Boolean).length
})

// 阅读时间（中文 400字/分钟，英文 200词/分钟混合估算）
const readingTime = computed(() => {
  const totalWords = chineseChars.value + words.value
  if (totalWords === 0) return '0 秒'
  const secs = Math.round((chineseChars.value / 400 + words.value / 200) * 60)
  if (secs < 60) return `${secs} 秒`
  return `${Math.floor(secs / 60)} 分 ${secs % 60} 秒`
})
const speakingTime = computed(() => {
  const totalWords = chineseChars.value + words.value
  if (totalWords === 0) return '0 秒'
  const secs = Math.round((chineseChars.value / 200 + words.value / 120) * 60)
  if (secs < 60) return `${secs} 秒`
  return `${Math.floor(secs / 60)} 分 ${secs % 60} 秒`
})

// 进度条颜色
const progressColor = computed(() => {
  const pct = nonWhitespaceChars.value / targetLen.value
  if (pct < 0.5) return '#ef4444'
  if (pct < 0.8) return '#f59e0b'
  if (pct < 1) return '#3b82f6'
  return '#10b981'
})

// 关键词搜索
const searchCount = computed(() => {
  if (!searchKeyword.value || !text.value) return 0
  const escaped = searchKeyword.value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  return (text.value.match(new RegExp(escaped, 'gi')) || []).length
})

// 字符构成
const composeStats = computed(() => {
  const total = totalChars.value || 1
  return [
    { label: '中文', value: chineseChars.value, color: '#6366f1', pct: ((chineseChars.value / total) * 100).toFixed(1) },
    { label: '英文字母', value: englishLetters.value, color: '#3b82f6', pct: ((englishLetters.value / total) * 100).toFixed(1) },
    { label: '数字', value: digits.value, color: '#10b981', pct: ((digits.value / total) * 100).toFixed(1) },
    { label: '标点', value: punctuation.value, color: '#f59e0b', pct: ((punctuation.value / total) * 100).toFixed(1) },
    { label: '空白', value: spaces.value, color: '#94a3b8', pct: ((spaces.value / total) * 100).toFixed(1) },
  ]
})

// 核心指标
const coreStats = computed(() => [
  { label: '总字符', value: totalChars.value, icon: 'Document', color: '#6366f1' },
  { label: '有效字数', value: nonWhitespaceChars.value, icon: 'EditPen', color: '#3b82f6' },
  { label: '英文单词', value: words.value, icon: 'Collection', color: '#10b981' },
  { label: '句子数', value: sentences.value, icon: 'ChatDotRound', color: '#f59e0b' },
  { label: '段落数', value: paragraphs.value, icon: 'Menu', color: '#8b5cf6' },
  { label: '行数', value: lines.value, icon: 'Grid', color: '#ec4899' },
])

// Top 10 高频词（2字及以上中文词汇 + 英文单词，过滤停用词）
const STOPWORDS = new Set(['the','a','an','is','in','on','of','to','and','or','it','at','by','be','as','we','he','she','they','this','that','with','for','from','are','was','were','has','have','had','not','but','you','your','my','我','的','了','在','是','和','有','这','个','中','大','为','上','与','也','就','不','他','她','它'])
const topWords = computed(() => {
  if (!text.value.trim()) return []
  const wordMap = new Map()
  // 中文：简单按2-4字切分（模拟词频）
  const zhTokens = text.value.match(/[\u4e00-\u9fa5]{2,4}/g) || []
  for (const w of zhTokens) {
    if (!STOPWORDS.has(w)) wordMap.set(w, (wordMap.get(w) || 0) + 1)
  }
  // 英文单词
  const enTokens = text.value.toLowerCase().match(/[a-z]{2,}/g) || []
  for (const w of enTokens) {
    if (!STOPWORDS.has(w)) wordMap.set(w, (wordMap.get(w) || 0) + 1)
  }
  return [...wordMap.entries()]
    .sort((a, b) => b[1] - a[1])
    .slice(0, 10)
    .map(([word, count]) => ({ word, count }))
})

// ── 操作
const clearText = () => {
  text.value = ''
  searchKeyword.value = ''
}

const pasteFromClipboard = async () => {
  try {
    text.value = (await navigator.clipboard.readText()) || ''
    ElMessage.success('已从剪贴板粘贴')
  } catch {
    ElMessage.error('无法读取剪贴板，请手动粘贴（Ctrl+V）')
  }
}

const copyStats = () => {
  const lines = [
    '【字数统计报告】',
    `总字符（含空格）：${totalChars.value}`,
    `有效字数（不含空格）：${nonWhitespaceChars.value}`,
    `中文字数：${chineseChars.value}`,
    `英文字母：${englishLetters.value}`,
    `数字：${digits.value}`,
    `标点：${punctuation.value}`,
    `英文单词数：${words.value}`,
    `句子数：${sentences.value}`,
    `段落数：${paragraphs.value}`,
    `行数：${lines.value}`,
    `预计阅读时间：${readingTime.value}`,
    `预计朗读时间：${speakingTime.value}`,
  ]
  navigator.clipboard.writeText(lines.join('\n'))
    .then(() => ElMessage.success('统计结果已复制到剪贴板'))
    .catch(() => ElMessage.error('复制失败'))
}
</script>

<style lang="scss" scoped>
$primary: #6366f1;
$blue: #3b82f6;
$green: #10b981;
$yellow: #f59e0b;
$red: #ef4444;
$purple: #8b5cf6;
$pink: #ec4899;
$gray: #64748b;
$border: #e2e8f0;
$radius: 14px;

.wc-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: 100%;
}

/* ─── 页头 ─── */
.wc-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;

  &__left {
    display: flex;
    align-items: center;
    gap: 14px;
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.wc-icon-badge {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, $primary 0%, $purple 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.3);
}

.wc-title {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.4px;
}

.wc-desc {
  margin: 3px 0 0;
  font-size: 13px;
  color: $gray;
}

/* ─── 主体布局 ─── */
.wc-body {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 20px;
  min-height: 0;
  flex: 1;
}

/* ─── 编辑区 ─── */
.wc-editor-col {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.wc-editor-wrap {
  background: #fff;
  border-radius: $radius;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.wc-editor-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid $border;
  background: #fafbfc;
  gap: 12px;

  .toolbar-label {
    font-size: 13px;
    font-weight: 600;
    color: #334155;
  }

  .toolbar-right {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.wc-progress {
  padding: 10px 16px 8px;
  border-bottom: 1px solid $border;
  display: flex;
  align-items: center;
  gap: 12px;

  :deep(.el-progress) { flex: 1; }

  .progress-hint {
    font-size: 12px;
    color: $gray;
    white-space: nowrap;
  }
}

.wc-textarea {
  flex: 1;

  :deep(.el-textarea__inner) {
    border: none !important;
    border-radius: 0;
    font-size: 14px;
    line-height: 1.75;
    font-family: Inter, 'PingFang SC', 'Microsoft YaHei', sans-serif;
    padding: 16px;
    resize: none;
    height: 100%;
    min-height: 300px;
    box-shadow: none !important;
  }
}

.wc-search-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-top: 1px solid $border;
  background: #fafbfc;

  .search-input {
    flex: 1;

    :deep(.el-input__inner) {
      font-size: 13px;
    }
  }
}

/* ─── 统计右侧面板 ─── */
.wc-stats-col {
  display: flex;
  flex-direction: column;
  gap: 14px;
  overflow-y: auto;

  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb { background: $border; border-radius: 4px; }
}

.stats-section {
  background: #fff;
  border-radius: $radius;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);

  &__title {
    font-size: 12px;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.08em;
    color: #94a3b8;
    margin-bottom: 12px;
  }
}

/* 核心指标 2×3 网格 */
.core-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.core-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  background: color-mix(in srgb, var(--accent) 8%, transparent);
  border: 1px solid color-mix(in srgb, var(--accent) 15%, transparent);

  &__icon {
    width: 34px;
    height: 34px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--accent);
    background: color-mix(in srgb, var(--accent) 12%, #fff);
    flex-shrink: 0;
  }

  &__value {
    font-size: 20px;
    font-weight: 800;
    color: #0f172a;
    line-height: 1;
  }

  &__label {
    font-size: 11px;
    color: $gray;
    margin-top: 3px;
  }
}

/* 字符构成 */
.compose-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.compose-item {
  &__head {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 5px;
  }

  &__dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    flex-shrink: 0;
  }

  &__label {
    flex: 1;
    font-size: 13px;
    color: #334155;
  }

  &__value {
    font-size: 13px;
    font-weight: 600;
    color: #0f172a;
    min-width: 32px;
    text-align: right;
  }

  &__pct {
    font-size: 11px;
    color: $gray;
    min-width: 36px;
    text-align: right;
  }
}

/* 阅读 & 说话 */
.read-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.read-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 10px;

  .el-icon {
    font-size: 20px;
    flex-shrink: 0;
  }

  &__val {
    font-size: 16px;
    font-weight: 700;
    color: #0f172a;
    line-height: 1;
  }

  &__lbl {
    font-size: 11px;
    color: $gray;
    margin-top: 4px;
  }
}

/* 高频词 */
.keyword-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.keyword-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.keyword-rank {
  width: 20px;
  height: 20px;
  border-radius: 6px;
  background: #f1f5f9;
  font-size: 11px;
  font-weight: 700;
  color: $gray;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.top3 {
    background: linear-gradient(135deg, $primary, $purple);
    color: #fff;
  }
}

.keyword-word {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
  min-width: 60px;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.keyword-bar {
  flex: 1;
}

.keyword-count {
  font-size: 12px;
  color: $gray;
  min-width: 24px;
  text-align: right;
}

/* 空状态 */
.empty-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 32px 16px;
  background: #fff;
  border-radius: $radius;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  color: #94a3b8;
  font-size: 13px;
  text-align: center;

  p { margin: 0; }
}

@media (max-width: 1100px) {
  .wc-body {
    grid-template-columns: 1fr;
  }

  .wc-stats-col {
    display: grid;
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 680px) {
  .wc-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .wc-stats-col {
    grid-template-columns: 1fr;
  }

  .core-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
