<template>
  <div class="chat-room tone-ai">
    <aside :class="['conversation-sidebar', { collapsed: sidebarCollapsed }]">
      <div class="sidebar-header">
        <div v-if="!sidebarCollapsed" class="sidebar-header__title">
          <h3>你的对话</h3>
        </div>
        <div class="sidebar-header__actions">
          <button class="icon-btn sidebar-toggle" type="button" aria-label="切换会话侧栏" @click="sidebarCollapsed = !sidebarCollapsed">
            <el-icon><ArrowLeft v-if="!sidebarCollapsed" /><ArrowRight v-else /></el-icon>
          </button>
        </div>
      </div>
      <div v-if="!sidebarCollapsed" class="sidebar-new-chat">
        <el-button type="primary" class="new-chat-btn" @click="createNewConversation">
          <el-icon><Plus /></el-icon>
          新对话
        </el-button>
      </div>

      <div v-if="!sidebarCollapsed" class="conversation-list">
        <button
          v-for="(conv, index) in conversations"
          :key="conv.conversationId"
          :class="['conversation-item', { active: currentConversationId === conv.conversationId }]"
          :style="{ animationDelay: `${index * 60}ms` }"
          @click="switchConversation(conv.conversationId)"
        >
          <div class="conversation-item__top">
            <span class="conversation-item__title">{{ conv.title || '新对话' }}</span>
            <button class="icon-btn" type="button" aria-label="删除会话" @click.stop="deleteConversation(conv.conversationId)">
              <el-icon><Delete /></el-icon>
            </button>
          </div>
          <div class="conversation-item__meta">
            <span>{{ conv.messageCount || 0 }} 条</span>
            <span class="meta-dot">·</span>
            <span class="conversation-item__model">{{ conv.modelDisplayName || conv.model || '默认' }}</span>
            <span class="meta-dot">·</span>
            <span>{{ formatDate(conv.lastMessageTime) }}</span>
          </div>
        </button>
      </div>
    </aside>

    <section class="chat-main">
      <header class="chat-header">
        <div class="chat-header__left">
          <el-avatar :size="42" :src="aiAvatar" />
          <div>
            <h2>AI-world</h2>
            <p>{{ conversationSummary }}</p>
          </div>
        </div>

        <div class="chat-header__right">
          <el-select
            v-model="selectedModelId"
            class="model-select"
            placeholder="选择模型"
            clearable
            filterable
            :disabled="managedModels.length === 0"
            @change="handleModelChange"
          >
            <el-option
              v-for="item in managedModels"
              :key="item.id"
              :label="item.displayName"
              :value="item.id"
            >
              <div class="model-option">
                <span>{{ item.displayName }}</span>
                <span class="model-option__meta">{{ item.modelName }}</span>
              </div>
            </el-option>
          </el-select>

          <el-tag effect="plain">{{ currentModelLabel }}</el-tag>

          <el-button circle aria-label="清空当前会话" @click="clearCurrentHistory">
            <el-icon><RefreshRight /></el-icon>
          </el-button>
        </div>
      </header>

      <div class="messages-shell">

      <main ref="messagesContainer" class="messages-panel" @scroll="handleScroll">
        <div v-if="messages.length === 0" class="empty-state">
          <EmptyIllustration
            variant="chat"
            title="开始一段新的对话"
            description="选择下方建议话题快速开始，或直接输入你的问题。会话会记录最近一次使用的模型。"
          />
          <div class="suggestion-grid">
            <button
              v-for="(item, index) in suggestionCards"
              :key="index"
              class="suggestion-card"
              v-motion
              :initial="{ opacity: 0, scale: 0.94, y: 8 }"
              :visibleOnce="{ opacity: 1, scale: 1, y: 0, transition: { delay: 200 + index * 80, duration: 260 } }"
              @click="handleSuggestionClick(item.text)"
            >
              <span class="suggestion-card__icon">
                <el-icon><component :is="item.icon" /></el-icon>
              </span>
              <span>{{ item.text }}</span>
            </button>
          </div>
        </div>

        <transition-group name="msg" tag="div" class="messages-inner">
        <div
          v-for="(msg, index) in messages"
          :key="msg.timestamp || index"
          :class="['message-row', msg.role === 'user' ? 'message-row--user' : 'message-row--assistant']"
        >
          <div class="message-avatar">
            <div v-if="msg.role === 'user'" class="user-avatar">
              <el-icon><UserFilled /></el-icon>
            </div>
            <el-avatar v-else :size="38" :src="aiAvatar" />
          </div>

          <div class="message-body">
            <div class="message-bubble">
              <div v-if="msg.role === 'user'" class="message-text">{{ msg.content }}</div>
              <div v-else class="assistant-content">
                <div v-if="msg.isStreaming && !msg.content && !msg.thought" class="streaming-loader">
                  <span></span><span></span><span></span>
                </div>

                <div v-if="msg.agentEvents && msg.agentEvents.length" class="agent-trace-card">
                  <div class="agent-trace-card__header" @click="msg.agentEventsExpanded = !msg.agentEventsExpanded">
                    <div class="agent-trace-card__title">
                      <el-icon><Cpu /></el-icon>
                      <span>执行轨迹 ({{ msg.agentEvents.length }})</span>
                    </div>
                    <el-icon :class="{ 'rotated-down': msg.agentEventsExpanded }"><ArrowRight /></el-icon>
                  </div>
                  <div v-show="msg.agentEventsExpanded" class="agent-trace-card__body">
                    <div v-if="msg.agentTodos && msg.agentTodos.length" class="agent-todo-list">
                      <div
                        v-for="todo in msg.agentTodos"
                        :key="todo.todoId"
                        :class="['agent-todo-item', `is-${String(todo.status || 'PENDING').toLowerCase()}`]"
                      >
                        <span class="agent-todo-item__dot"></span>
                        <span class="agent-todo-item__content">{{ todo.content }}</span>
                        <span class="agent-todo-item__status">{{ todoStatusLabel(todo.status) }}</span>
                      </div>
                    </div>
                    <div
                      v-for="(evt, evtIdx) in msg.agentEvents"
                      :key="`${evt.timestamp}-${evtIdx}`"
                      class="agent-trace-card__item"
                    >
                      <span class="agent-trace-card__type">{{ evt.typeLabel }}</span>
                      <p class="agent-trace-card__text">{{ evt.text }}</p>
                    </div>
                  </div>
                </div>

                <div v-if="msg.thought" class="thought-card">
                  <div class="thought-card__header" @click="msg.thoughtExpanded = !msg.thoughtExpanded">
                    <div class="thought-card__title">
                      <el-icon><Cpu /></el-icon>
                      <span>{{ msg.isThinking ? '正在深度思考...' : `深度思考完成 (${msg.thinkingTime || 0}s)` }}</span>
                    </div>
                    <el-icon :class="{ rotated: msg.thoughtExpanded }"><ArrowRight /></el-icon>
                  </div>
                  <div v-show="msg.thoughtExpanded" class="thought-card__content markdown-body" v-html="msg.renderedThoughtHtml || ''"></div>
                </div>

                <div v-if="shouldShowSearchCard(msg)" class="search-card">
                  <div class="search-card__header">
                    <div class="search-card__title">
                      <el-icon><Compass /></el-icon>
                      <span>联网搜索</span>
                    </div>
                    <el-tag size="small" :type="getSearchStatusTagType(msg.searchStatus)" effect="plain">
                      {{ getSearchStatusLabel(msg.searchStatus) }}
                    </el-tag>
                  </div>
                  <div v-if="msg.searchQuery" class="search-card__query">
                    检索词：{{ msg.searchQuery }}
                  </div>
                  <div v-if="msg.searchStatus === 'SUCCESS' && msg.sources?.length" class="search-source-list">
                    <a
                      v-for="(source, sourceIndex) in msg.sources"
                      :key="`${source.url || source.title}-${sourceIndex}`"
                      class="search-source"
                      :href="source.url"
                      target="_blank"
                      rel="noopener noreferrer"
                    >
                      <div class="search-source__top">
                        <span class="search-source__title">{{ source.title || source.url }}</span>
                        <span class="search-source__domain">{{ source.domain || '来源链接' }}</span>
                      </div>
                      <p class="search-source__snippet">{{ source.snippet }}</p>
                    </a>
                  </div>
                  <div v-else class="search-card__fallback">
                    {{ getSearchFallbackText(msg.searchStatus) }}
                  </div>
                </div>

                <div class="message-text markdown-body" v-html="msg.renderedHtml || ''"></div>
                <span v-if="msg.isStreaming && msg.content" class="stream-cursor">|</span>
              </div>
            </div>

            <div
              v-if="msg.role === 'assistant' && msg.content && !msg.isStreaming && (msg.suggestionsLoading || msg.suggestions?.length)"
              class="message-suggestions"
            >
              <div class="message-suggestions__label">推荐追问</div>
              <div v-if="msg.suggestionsLoading && !msg.suggestions?.length" class="message-suggestions__loading">
                正在生成推荐问题...
              </div>
              <div v-else-if="msg.suggestions?.length" class="message-suggestions__list">
                <button
                  v-for="(suggestion, suggestionIndex) in msg.suggestions"
                  :key="`${msg.timestamp}-${suggestionIndex}`"
                  class="message-suggestion-chip"
                  type="button"
                  @click="handleSuggestionClick(suggestion)"
                >
                  {{ suggestion }}
                </button>
              </div>
            </div>

            <div class="message-actions" v-if="msg.content">
              <button class="icon-btn" type="button" aria-label="复制消息" @click="copyMessage(msg.content)">
                <el-icon><CopyDocument /></el-icon>
              </button>
              <button class="icon-btn" type="button" aria-label="重新生成消息" :disabled="loading" @click="regenerateFromMessage(index)">
                <el-icon><RefreshRight /></el-icon>
              </button>
              <span>{{ formatTime(msg.timestamp) }}</span>
            </div>
          </div>
        </div>
        </transition-group>
      </main>

      <button v-if="userScrolledUp" class="scroll-bottom-btn" type="button" aria-label="scroll to bottom" @click="scrollToBottom(true)">
        <el-icon><ArrowDown /></el-icon>
      </button>
      </div>

      <footer class="chat-input">
        <div v-if="mountedSkills.length" class="mounted-skill-bar">
          <div class="mounted-skill-list">
            <el-tag
              v-for="skill in compactMountedSkills"
              :key="skill.id"
              closable
              effect="plain"
              @close="removeMountedSkill(skill.id)"
            >
              {{ skill.name }}
            </el-tag>
            <el-tag v-if="mountedSkills.length > compactMountedSkills.length" effect="plain">
              +{{ mountedSkills.length - compactMountedSkills.length }}
            </el-tag>
          </div>
          <span v-if="hasToolSkillWithoutAgent" class="skill-mode-hint">工具型技能将在智能体模式下执行，当前仅作为提示词参考</span>
        </div>
        <el-input
          v-model="inputToSent"
          type="textarea"
          :autosize="{ minRows: 1, maxRows: 5 }"
          placeholder="输入消息，按 Enter 发送"
          resize="none"
          @keydown.enter.exact.prevent="handleSend"
        />

        <div class="chat-input__actions">
          <div class="toggle-group">
            <button :class="['toggle-chip', 'toggle-chip--agent', { active: useAgent }]" type="button" @click="toggleAgentMode">
              <el-icon><ChatDotRound /></el-icon>
              智能体模式
            </button>
            <button
              :class="['toggle-chip', { active: useDeepThinking, 'toggle-chip--disabled': useAgent }]"
              type="button"
              :disabled="useAgent"
              :title="useAgent ? '智能体模式暂不支持深度思考模型的多轮工具调用' : '启用深度思考'"
              @click="useDeepThinking = !useDeepThinking"
            >
              <el-icon><Cpu /></el-icon>
              深度思考
            </button>
            <button
              :class="['toggle-chip', { active: useWebSearch, 'toggle-chip--disabled': useAgent || !webSearchAvailable }]"
              type="button"
              :disabled="useAgent || !webSearchAvailable"
              :title="useAgent ? '智能体模式首版暂不接入联网搜索' : (webSearchAvailable ? '使用系统联网搜索' : webSearchUnavailableReason)"
              @click="useWebSearch = !useWebSearch"
            >
              <el-icon><Compass /></el-icon>
              联网搜索
            </button>
            <button :class="['toggle-chip', { active: mountedSkills.length > 0 }]" type="button" @click="openSkillPicker">
              <el-icon><MagicStick /></el-icon>
              技能 {{ mountedSkills.length }}/50
            </button>
          </div>

          <el-button type="primary" :loading="loading" :disabled="!inputToSent.trim()" @click="handleSend">
            <el-icon><ArrowUpBold /></el-icon>
            发送
          </el-button>
        </div>
      </footer>
    </section>

    <el-dialog v-model="skillPickerVisible" width="920px" class="skill-picker-dialog" destroy-on-close>
      <template #header>
        <div class="skill-picker-header">
          <div>
            <h3>编程技能</h3>
            <p>Skills</p>
          </div>
          <span>已选 {{ mountedSkills.length }}/50</span>
        </div>
      </template>
      <div class="skill-picker">
        <aside class="skill-picker-side">
          <el-button type="primary" class="create-skill-btn" @click="goSkillManage">
            <el-icon><Plus /></el-icon>
            创建技能
          </el-button>
          <button :class="['skill-source-tab', { active: skillFilters.originType !== 'MY' }]" @click="setSkillOrigin('')">平台技能</button>
          <button :class="['skill-source-tab', { active: skillFilters.originType === 'MY' }]" @click="setSkillOrigin('MY')">我的技能</button>
          <div class="skill-filter-block">
            <span>按类型筛选</span>
            <el-checkbox-group v-model="skillTypeFilters" @change="loadSkillOptions">
              <el-checkbox label="PROMPT">提示词</el-checkbox>
              <el-checkbox label="TOOL">工具</el-checkbox>
              <el-checkbox label="MIXED">混合</el-checkbox>
            </el-checkbox-group>
          </div>
          <div class="skill-filter-block">
            <span>按使用场景筛选</span>
            <el-checkbox v-model="webScenarioOnly" @change="loadSkillOptions">网页开发</el-checkbox>
          </div>
        </aside>
        <main class="skill-picker-main">
          <div class="selected-skill-row">
            <el-tag
              v-for="skill in mountedSkills"
              :key="skill.id"
              closable
              effect="plain"
              @close="removeMountedSkill(skill.id)"
            >
              {{ skill.name }}
            </el-tag>
          </div>
          <el-input v-model="skillFilters.keyword" clearable placeholder="搜索技能" @keyup.enter="loadSkillOptions" />
          <div v-loading="skillLoading" class="skill-option-list">
            <div v-for="skill in skillOptions" :key="skill.id" class="skill-option">
              <div class="skill-option-icon">{{ skill.name?.slice(0, 1) }}</div>
              <div class="skill-option-body">
                <div class="skill-option-title">
                  <strong>{{ skill.name }}</strong>
                  <el-tag size="small" effect="plain">{{ typeLabel(skill.skillType) }}</el-tag>
                </div>
                <p>{{ skill.description || '暂无描述' }}</p>
              </div>
              <el-button :disabled="isMountedSkill(skill.id)" @click="addMountedSkill(skill)">
                {{ isMountedSkill(skill.id) ? '已添加' : '添加' }}
              </el-button>
            </div>
          </div>
        </main>
      </div>
    </el-dialog>

  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowDown,
  ArrowLeft,
  ArrowRight,
  ArrowUpBold,
  ChatDotRound,
  Compass,
  CopyDocument,
  Cpu,
  DataAnalysis,
  Delete,
  Document,
  MagicStick,
  Plus,
  Reading,
  RefreshRight,
  TrendCharts,
  UserFilled
} from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import axios from 'axios'
import aiAvatar from '@/assets/ai_avatar.png'
import { getAiCapabilities, getAiModelList } from '@/api/ai-model'
import { getConversationSkills, listSkills, saveConversationSkills } from '@/api/ai-skill'
import { useRouter } from 'vue-router'
import EmptyIllustration from '@/components/EmptyIllustration.vue'

const suggestionCards = [
  { icon: DataAnalysis, text: '分析今天热门板块' },
  { icon: Document, text: '写一个快速排序算法' },
  { icon: TrendCharts, text: '总结最近的财报热点' },
  { icon: Reading, text: '推荐几本金融入门书' }
]

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: (str, lang) => {
    const langLabel = lang || 'text'
    let highlighted = ''
    if (lang && hljs.getLanguage(lang)) {
      try {
        highlighted = hljs.highlight(str, { language: lang }).value
      } catch (__) {}
    }
    if (!highlighted) {
      highlighted = md.utils.escapeHtml(str)
    }
    return `<div class="code-block-wrapper"><div class="code-block-header"><span class="code-lang">${langLabel}</span><button class="code-copy-btn" onclick="navigator.clipboard.writeText(this.closest('.code-block-wrapper').querySelector('code').textContent).then(()=>{this.textContent='已复制';setTimeout(()=>{this.textContent='复制'},1500)})">复制</button></div><pre><code class="hljs language-${langLabel}">${highlighted}</code></pre></div>`
  }
})

const defaultRender = md.renderer.rules.link_open || function (tokens, idx, options, env, self) {
  return self.renderToken(tokens, idx, options)
}
md.renderer.rules.link_open = function (tokens, idx, options, env, self) {
  tokens[idx].attrSet('target', '_blank')
  tokens[idx].attrSet('rel', 'noopener noreferrer')
  return defaultRender(tokens, idx, options, env, self)
}

const messages = ref([])
const router = useRouter()
const conversations = ref([])
const managedModels = ref([])
const defaultModelId = ref(null)
const selectedModelId = ref(null)
const inputToSent = ref('')
const loading = ref(false)
const messagesContainer = ref(null)
const currentConversationId = ref(null)
const useAgent = ref(false)
const useWebSearch = ref(false)
const useDeepThinking = ref(false)
const sidebarCollapsed = ref(false)
const userScrolledUp = ref(false)
const agentTimeline = ref([])
const agentStatus = ref('idle')
const agentStatusText = ref('智能体模式已关闭，当前为普通聊天模式。')
const aiCapabilities = ref({
  webSearchEnabled: false,
  webSearchMode: 'system-searxng'
})
const mountedSkills = ref([])
const skillOptions = ref([])
const skillPickerVisible = ref(false)
const skillLoading = ref(false)
const skillFilters = ref({ keyword: '', originType: '', enabledOnly: true })
const skillTypeFilters = ref([])
const webScenarioOnly = ref(false)

const selectedModel = computed(() => managedModels.value.find(item => item.id === selectedModelId.value) || null)
const webSearchAvailable = computed(() => Boolean(aiCapabilities.value?.webSearchEnabled))
const webSearchUnavailableReason = computed(() => webSearchAvailable.value ? '' : '当前系统未配置 SearXNG 联网搜索')
const agentStatusLabel = computed(() => ({
  idle: '未启用',
  running: '执行中',
  completed: '已完成',
  failed: '失败'
}[agentStatus.value] || '处理中'))
const agentStatusTagType = computed(() => ({
  idle: 'info',
  running: 'warning',
  completed: 'success',
  failed: 'danger'
}[agentStatus.value] || 'info'))
const currentModelLabel = computed(() => {
  if (selectedModel.value) return selectedModel.value.displayName
  if (managedModels.value.length === 0) return '配置文件默认模型'
  return '未选择模型'
})
const compactMountedSkills = computed(() => mountedSkills.value.slice(0, 8))
const mountedSkillIds = computed(() => mountedSkills.value.map(item => item.id))
const hasToolSkillWithoutAgent = computed(() => !useAgent.value && mountedSkills.value.some(item => ['TOOL', 'MIXED'].includes(item.skillType)))
const conversationSummary = computed(() => {
  const count = conversations.value.length
  const model = currentModelLabel.value
  return `当前模型：${model} · 已保存 ${count} 个会话`
})

let renderTimer = null
let scrollTimer = null
let requestSequence = 0
let activeRequestId = 0

const copyMessage = (content) => {
  navigator.clipboard.writeText(content).then(() => {
    ElMessage.success({ message: '已复制到剪贴板', duration: 1500 })
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

const loadMountedSkills = async (conversationId = currentConversationId.value) => {
  if (!conversationId) {
    mountedSkills.value = []
    return
  }
  try {
    const { data } = await getConversationSkills(conversationId)
    mountedSkills.value = data?.data || data || []
  } catch (error) {
    mountedSkills.value = []
  }
}

const saveMountedSkills = async () => {
  if (!currentConversationId.value) return
  await saveConversationSkills(currentConversationId.value, mountedSkillIds.value)
}

const loadSkillOptions = async () => {
  skillLoading.value = true
  try {
    const params = {
      ...skillFilters.value,
      skillType: skillTypeFilters.value.length === 1 ? skillTypeFilters.value[0] : '',
      scenario: webScenarioOnly.value ? '网页开发' : ''
    }
    const { data } = await listSkills(params)
    let list = data?.data || data || []
    if (skillTypeFilters.value.length > 1) {
      list = list.filter(item => skillTypeFilters.value.includes(item.skillType))
    }
    skillOptions.value = list
  } catch (error) {
    ElMessage.error('加载技能列表失败')
  } finally {
    skillLoading.value = false
  }
}

const openSkillPicker = async () => {
  if (!currentConversationId.value) {
    currentConversationId.value = crypto.randomUUID()
  }
  skillPickerVisible.value = true
  await loadSkillOptions()
}

const setSkillOrigin = (originType) => {
  skillFilters.value.originType = originType
  loadSkillOptions()
}

const isMountedSkill = (id) => mountedSkills.value.some(item => item.id === id)

const addMountedSkill = async (skill) => {
  if (isMountedSkill(skill.id)) return
  if (mountedSkills.value.length >= 50) {
    ElMessage.warning('最多挂载 50 个技能')
    return
  }
  mountedSkills.value.push(skill)
  await saveMountedSkills()
}

const removeMountedSkill = async (id) => {
  mountedSkills.value = mountedSkills.value.filter(item => item.id !== id)
  await saveMountedSkills()
}

const goSkillManage = () => {
  skillPickerVisible.value = false
  router.push('/ai/skills')
}

const typeLabel = (value) => ({ PROMPT: '提示词', TOOL: '工具', MIXED: '混合' }[value] || value || '-')

const doRender = (msg) => {
  if (msg.content) msg.renderedHtml = md.render(msg.content)
  if (msg.thought) msg.renderedThoughtHtml = md.render(msg.thought)
}

const createAssistantMessage = () => ({
  role: 'assistant',
  thought: '',
  content: '',
  usedWebSearch: useWebSearch.value,
  searchQuery: '',
  searchStatus: useWebSearch.value ? 'SEARCHING' : 'NOT_REQUESTED',
  sources: [],
  fullText: '',
  isThinking: false,
  isStreaming: true,
  suggestions: [],
  suggestionsLoading: false,
  thoughtExpanded: true,
  thinkingTime: 0,
  timestamp: Date.now(),
  renderedHtml: '',
  renderedThoughtHtml: '',
  agentEvents: [],
  agentEventsExpanded: false,
  agentRunId: '',
  agentTodos: []
})

const throttledRender = (msg) => {
  if (renderTimer) return
  renderTimer = setTimeout(() => {
    renderTimer = null
    doRender(msg)
  }, 80)
}

const finalRender = (msg) => {
  if (renderTimer) {
    clearTimeout(renderTimer)
    renderTimer = null
  }
  doRender(msg)
  msg.isStreaming = false
}

const setLoadingForRequest = (requestId, nextLoading) => {
  if (activeRequestId === requestId) {
    loading.value = nextLoading
  }
}

const throttledScroll = () => {
  if (scrollTimer) return
  scrollTimer = setTimeout(async () => {
    scrollTimer = null
    if (userScrolledUp.value) return
    await nextTick()
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  }, 80)
}

const handleScroll = () => {
  if (!messagesContainer.value) return
  const { scrollTop, scrollHeight, clientHeight } = messagesContainer.value
  userScrolledUp.value = scrollTop + clientHeight < scrollHeight - 100
}

const scrollToBottom = async (force = false) => {
  if (userScrolledUp.value && !force) return
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTo({
      top: messagesContainer.value.scrollHeight,
      behavior: 'smooth'
    })
  }
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const d = new Date(timestamp)
  return `${d.getHours()}:${d.getMinutes().toString().padStart(2, '0')}`
}

const formatDate = (datetime) => {
  if (!datetime) return ''
  const d = new Date(datetime)
  const now = new Date()
  const diff = now - d
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return `${d.getMonth() + 1}/${d.getDate()}`
}

const normalizeSources = (sources) => Array.isArray(sources) ? sources : []

const resetAgentRuntime = () => {
  agentTimeline.value = []
  agentStatus.value = useAgent.value ? 'idle' : 'idle'
  agentStatusText.value = useAgent.value
    ? '等待发送消息后启动智能体。'
    : '智能体模式已关闭，当前为普通聊天模式。'
}

const pushAgentTimeline = (type, payload = {}, targetMsg = null) => {
  updateAgentMessageState(type, payload, targetMsg)
  const text = buildAgentTimelineText(type, payload)
  const item = {
    type,
    typeLabel: buildAgentTimelineLabel(type),
    text,
    timestamp: Date.now()
  }
  agentTimeline.value.push(item)
  if (targetMsg) {
    if (!Array.isArray(targetMsg.agentEvents)) {
      targetMsg.agentEvents = []
    }
    targetMsg.agentEvents.push(item)
  }
}

const buildAgentTimelineLabel = (type) => ({
  agent_run: '运行',
  agent_plan: '规划',
  agent_todo: '任务',
  agent_compact: '压缩',
  agent_subagent: '子代理',
  agent_permission: '权限',
  agent_tool_call: '工具调用',
  agent_tool_result: '工具结果',
  agent_status: '状态'
}[type] || '智能体')

const buildAgentTimelineText = (type, payload = {}) => {
  if (type === 'agent_run') {
    return payload.message || `Run ${payload.runId || ''} 已启动`
  }
  if (type === 'agent_plan') {
    return payload.summary || '智能体已生成执行计划。'
  }
  if (type === 'agent_todo') {
    const todos = normalizeAgentTodos(payload)
    return todos.length ? `更新 ${todos.length} 个任务` : '任务状态已更新'
  }
  if (type === 'agent_compact') {
    return payload.reason || payload.snapshot?.compactLevel || '上下文已压缩'
  }
  if (type === 'agent_subagent') {
    const subagent = payload.subagent || {}
    return `${subagent.agentType || 'general-purpose'}：${subagent.task || subagent.resultSummary || '子代理状态更新'}`
  }
  if (type === 'agent_permission') {
    const request = payload.request || {}
    return `${request.toolName || '工具'} 需要授权`
  }
  if (type === 'agent_tool_call') {
    const args = payload.arguments ? JSON.stringify(payload.arguments) : '{}'
    return `${payload.toolName || '未知工具'} ${args}`
  }
  if (type === 'agent_tool_result') {
    return `${payload.toolName || '未知工具'}：${payload.result || '已完成'}`
  }
  if (type === 'agent_status') {
    return payload.message || payload.status || '状态已更新'
  }
  return '智能体事件'
}

const normalizeAgentTodos = (payload = {}) => {
  if (Array.isArray(payload.todos)) return payload.todos
  if (payload.todo) return [payload.todo]
  return []
}

const updateAgentMessageState = (type, payload = {}, targetMsg = null) => {
  if (!targetMsg) return
  if (payload.runId) {
    targetMsg.agentRunId = payload.runId
  }
  if (type === 'agent_run') {
    targetMsg.agentRunId = payload.runId || targetMsg.agentRunId
  }
  if (type === 'agent_todo') {
    const incoming = normalizeAgentTodos(payload)
    if (!Array.isArray(targetMsg.agentTodos)) {
      targetMsg.agentTodos = []
    }
    incoming.forEach((todo) => {
      const index = targetMsg.agentTodos.findIndex(item => item.todoId === todo.todoId)
      if (index >= 0) {
        targetMsg.agentTodos[index] = { ...targetMsg.agentTodos[index], ...todo }
      } else {
        targetMsg.agentTodos.push(todo)
      }
    })
  }
}

const todoStatusLabel = (status) => ({
  PENDING: '待办',
  IN_PROGRESS: '进行中',
  COMPLETED: '完成',
  CANCELLED: '取消'
}[status] || status || '待办')

const shouldShowSearchCard = (msg) => ['SEARCHING', 'SUCCESS', 'FALLBACK_NO_RESULT', 'FALLBACK_ERROR'].includes(msg?.searchStatus)

const getSearchStatusLabel = (status) => ({
  SEARCHING: '搜索中',
  SUCCESS: '已引用来源',
  FALLBACK_NO_RESULT: '未找到结果',
  FALLBACK_ERROR: '搜索失败'
}[status] || '未搜索')

const getSearchStatusTagType = (status) => ({
  SEARCHING: 'info',
  SUCCESS: 'success',
  FALLBACK_NO_RESULT: 'warning',
  FALLBACK_ERROR: 'danger'
}[status] || 'info')

const getSearchFallbackText = (status) => ({
  SEARCHING: '正在联网检索，请稍候...',
  FALLBACK_NO_RESULT: '未检索到有效结果，已自动降级为普通回答。',
  FALLBACK_ERROR: '联网搜索失败，已自动降级为普通回答。'
}[status] || '')

const loadCapabilities = async () => {
  try {
    const res = await getAiCapabilities()
    aiCapabilities.value = res.data || aiCapabilities.value
    if (!aiCapabilities.value.webSearchEnabled) {
      useWebSearch.value = false
    }
  } catch (error) {
    aiCapabilities.value = {
      webSearchEnabled: false,
      webSearchMode: 'system-searxng'
    }
    useWebSearch.value = false
  }
}

const loadManagedModels = async () => {
  try {
    const res = await getAiModelList({ enabledOnly: true })
    managedModels.value = res.data || []
    defaultModelId.value = managedModels.value.find(item => item.isDefault)?.id ?? null
    if (!currentConversationId.value) {
      selectedModelId.value = defaultModelId.value
    }
  } catch (error) {
    managedModels.value = []
    defaultModelId.value = null
  }
}

const loadConversations = async () => {
  try {
    const response = await axios.get('/api/ai/history/conversations')
    conversations.value = response.data.data || []
  } catch (error) {
    console.error('加载会话列表失败', error)
  }
}

const loadConversationMessages = async (conversationId) => {
  if (!conversationId) return []
  try {
    const response = await axios.get(`/api/ai/history/conversations/${conversationId}/messages`)
    return response.data.data || []
  } catch (error) {
    console.error('加载会话消息失败', error?.response?.status, error?.response?.data)
    return []
  }
}

const resolveConversationModel = (conversation, silent = false) => {
  if (!conversation?.modelId) {
    selectedModelId.value = defaultModelId.value
    return
  }
  const exists = managedModels.value.some(item => item.id === conversation.modelId)
  if (exists) {
    selectedModelId.value = conversation.modelId
    return
  }
  selectedModelId.value = defaultModelId.value
  if (!silent) {
    ElMessage.warning('当前会话绑定的模型已不可用，已回退到默认模型')
  }
}

const mapHistoryMessage = (msg) => {
  const item = {
    role: msg.role,
    content: msg.content,
    thought: msg.thought,
    thinkingTime: msg.thinkingTime,
    usedWebSearch: Boolean(msg.usedWebSearch) || ['SUCCESS', 'FALLBACK_NO_RESULT', 'FALLBACK_ERROR'].includes(msg.searchStatus),
    searchQuery: msg.searchQuery || '',
    searchStatus: msg.searchStatus || 'NOT_REQUESTED',
    sources: normalizeSources(msg.sources),
    timestamp: new Date(msg.timestamp).getTime(),
    thoughtExpanded: false,
    isThinking: false,
    isStreaming: false,
    suggestions: [],
    suggestionsLoading: false,
    fullText: '',
    renderedHtml: '',
    renderedThoughtHtml: '',
    agentEvents: [],
    agentEventsExpanded: false
  }
  if (msg.role !== 'user') doRender(item)
  return item
}

const switchConversation = async (conversationId) => {
  currentConversationId.value = conversationId
  resetAgentRuntime()
  resolveConversationModel(conversations.value.find(item => item.conversationId === conversationId))
  try {
    await loadMountedSkills(conversationId)
    const historyMessages = await loadConversationMessages(conversationId)
    messages.value = historyMessages.map(mapHistoryMessage)
    userScrolledUp.value = false
    await scrollToBottom(true)
  } catch (error) {
    console.error('加载会话消息失败', error?.response?.status, error?.response?.data)
    ElMessage.error('加载会话消息失败')
  }
}

const recoverAssistantMessageFromHistory = async (conversationId, targetMsg) => {
  if (!conversationId || !targetMsg) return false
  const historyMessages = await loadConversationMessages(conversationId)
  const latestAssistant = [...historyMessages].reverse().find(item => item.role === 'assistant' && item.content)
  if (!latestAssistant) return false

  targetMsg.content = latestAssistant.content || targetMsg.content
  targetMsg.thought = latestAssistant.thought || targetMsg.thought
  targetMsg.thinkingTime = latestAssistant.thinkingTime || targetMsg.thinkingTime
  targetMsg.usedWebSearch = Boolean(latestAssistant.usedWebSearch) || targetMsg.usedWebSearch
  targetMsg.searchQuery = latestAssistant.searchQuery || targetMsg.searchQuery
  targetMsg.searchStatus = latestAssistant.searchStatus || targetMsg.searchStatus
  targetMsg.sources = normalizeSources(latestAssistant.sources)
  finalRender(targetMsg)
  return true
}

const createNewConversation = () => {
  currentConversationId.value = crypto.randomUUID()
  messages.value = []
  mountedSkills.value = []
  selectedModelId.value = defaultModelId.value
  userScrolledUp.value = false
  resetAgentRuntime()
}

const handleModelChange = () => {
  if (!selectedModel.value && managedModels.value.length > 0) {
    ElMessage.info('当前会话将回退到配置文件默认模型或后端默认模型')
  }
}

const deleteConversation = async (conversationId) => {
  try {
    await ElMessageBox.confirm('确定删除这个会话吗？', '提示', { type: 'warning' })
    await axios.delete(`/api/ai/history/conversations/${conversationId}`)
    ElMessage.success('删除成功')
    if (currentConversationId.value === conversationId) {
      createNewConversation()
    }
    await loadConversations()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const clearCurrentHistory = () => {
  messages.value = []
  currentConversationId.value = null
  mountedSkills.value = []
  selectedModelId.value = defaultModelId.value
  resetAgentRuntime()
}

const normalizeCapabilitiesBeforeSend = () => {
  if (useAgent.value && useDeepThinking.value) {
    useDeepThinking.value = false
    ElMessage.warning('智能体模式暂不支持深度思考模型，已自动关闭深度思考')
  }
  if (useAgent.value && useWebSearch.value) {
    useWebSearch.value = false
    ElMessage.warning('智能体模式首版暂不接入联网搜索，已自动关闭联网搜索')
  }
  if (selectedModel.value && useDeepThinking.value && !selectedModel.value.supportsDeepThinking) {
    useDeepThinking.value = false
    ElMessage.warning('当前模型不支持深度思考，已自动关闭')
  }
  if (useWebSearch.value && !webSearchAvailable.value) {
    useWebSearch.value = false
    ElMessage.warning('当前模型不支持联网搜索，已自动关闭')
  }
}

const regenerateFromMessage = async (index) => {
  if (loading.value) return
  const target = messages.value[index]
  if (!target?.content) return

  let prompt = ''
  if (target.role === 'user') {
    prompt = target.content
  } else {
    for (let i = index - 1; i >= 0; i--) {
      if (messages.value[i].role === 'user' && messages.value[i].content) {
        prompt = messages.value[i].content
        break
      }
    }
  }

  if (!prompt) {
    ElMessage.warning('未找到可重新生成的提问')
    return
  }

  inputToSent.value = prompt
  await handleSend()
}

const handleSuggestionClick = async (prompt) => {
  if (!prompt || loading.value) return
  inputToSent.value = prompt
  await handleSend()
}

const toggleAgentMode = () => {
  useAgent.value = !useAgent.value
  if (useAgent.value && useDeepThinking.value) {
    useDeepThinking.value = false
    ElMessage.info('智能体模式暂不支持深度思考模型，已自动关闭深度思考')
  }
  if (useAgent.value && useWebSearch.value) {
    useWebSearch.value = false
    ElMessage.info('智能体模式首版暂不接入联网搜索，已自动关闭联网搜索')
  }
  resetAgentRuntime()
}

const handleSend = async () => {
  const content = inputToSent.value.trim()
  if (!content || loading.value) return

  normalizeCapabilitiesBeforeSend()

  messages.value.push({
    role: 'user',
    content,
    timestamp: Date.now()
  })

  inputToSent.value = ''
  const requestId = ++requestSequence
  activeRequestId = requestId
  loading.value = true
  userScrolledUp.value = false
  await scrollToBottom(true)

  const aiIndex = messages.value.length
  messages.value.push(createAssistantMessage())

  if (!currentConversationId.value) {
    currentConversationId.value = crypto.randomUUID()
  }

  if (useAgent.value) {
    agentTimeline.value = []
    agentStatus.value = 'running'
    agentStatusText.value = '智能体正在分析问题并按需调用工具。'
    messages.value[aiIndex].agentEventsExpanded = true
  } else {
    resetAgentRuntime()
  }

  await handleClassicSend(content, requestId, aiIndex)
}

const handleClassicSend = async (content, requestId, aiIndex) => {
  const targetMsg = messages.value[aiIndex]
  targetMsg.suggestionsLoading = true

  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/api/ai/chatStream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: token ? `Bearer ${token}` : ''
      },
      body: JSON.stringify({
        message: content,
        provider: selectedModel.value?.provider || 'openai',
        model: selectedModel.value?.modelName,
        modelId: selectedModelId.value,
        conversationId: currentConversationId.value,
        useAgent: useAgent.value,
        useWebSearch: useWebSearch.value,
        useDeepThinking: useDeepThinking.value,
        skillIds: mountedSkillIds.value
      })
    })

    if (!response.ok) throw new Error('Network error')

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    let thoughtTimer = null
    const targetMsg = messages.value[aiIndex]

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      let currentEvent = 'message'
      for (const line of lines) {
        if (line.startsWith('event:')) {
          currentEvent = line.slice(6).trim()
          continue
        }
        if (!line.startsWith('data:')) continue

        const data = line.slice(5).trim()
        if (data === '[DONE]') continue

        try {
          const json = JSON.parse(data)
          if (currentEvent === 'error') {
            ElMessage.error(json.error || 'AI 服务出错')
            targetMsg.content = `[错误] ${json.error || '系统繁忙，请稍后再试'}`
            targetMsg.isThinking = false
            targetMsg.suggestionsLoading = false
            if (useAgent.value) {
              agentStatus.value = 'failed'
              agentStatusText.value = json.error || '智能体执行失败'
              targetMsg.agentEventsExpanded = false
            }
            finalRender(targetMsg)
            setLoadingForRequest(requestId, false)
            return
          }

          if (currentEvent === 'agent_status') {
            agentStatus.value = json.status || 'running'
            agentStatusText.value = json.message || '智能体状态已更新'
            pushAgentTimeline(currentEvent, json, targetMsg)
            throttledScroll()
            continue
          }

          if ([
            'agent_run',
            'agent_plan',
            'agent_todo',
            'agent_compact',
            'agent_subagent',
            'agent_permission',
            'agent_tool_call',
            'agent_tool_result'
          ].includes(currentEvent)) {
            pushAgentTimeline(currentEvent, json, targetMsg)
            throttledScroll()
            continue
          }

          if (currentEvent === 'search') {
            targetMsg.usedWebSearch = Boolean(json.usedWebSearch)
            targetMsg.searchQuery = json.searchQuery || ''
            targetMsg.searchStatus = json.searchStatus || 'NOT_REQUESTED'
            targetMsg.sources = normalizeSources(json.sources)
            throttledScroll()
            continue
          }

          if (currentEvent === 'done') {
            if (json.agentRunId) {
              targetMsg.agentRunId = json.agentRunId
            }
            if (thoughtTimer) clearInterval(thoughtTimer)
            targetMsg.isThinking = false
            targetMsg.suggestionsLoading = Boolean(targetMsg.content)
             if (useAgent.value && agentStatus.value !== 'failed') {
              agentStatus.value = 'completed'
              agentStatusText.value = '智能体已完成本轮任务。'
              targetMsg.agentEventsExpanded = false
            }
            finalRender(targetMsg)
            setLoadingForRequest(requestId, false)
            throttledScroll()
            continue
          }

          if (currentEvent === 'suggestions') {
            targetMsg.suggestions = Array.isArray(json.suggestions)
              ? json.suggestions.filter(item => typeof item === 'string' && item.trim())
              : []
            targetMsg.suggestionsLoading = false
            throttledScroll()
            continue
          }

          const delta = json.choices?.[0]?.delta
          if (!delta) continue

          let changed = false
          if (delta.reasoning_content) {
            targetMsg.thought += delta.reasoning_content
            changed = true
            if (!targetMsg.isThinking) {
              targetMsg.isThinking = true
              targetMsg.thoughtExpanded = true
              if (thoughtTimer) clearInterval(thoughtTimer)
              thoughtTimer = setInterval(() => {
                targetMsg.thinkingTime++
              }, 1000)
            }
          }

          if (delta.content) {
            changed = true
            if (targetMsg.isThinking && !targetMsg.fullText.includes('<think>')) {
              targetMsg.isThinking = false
              if (thoughtTimer) clearInterval(thoughtTimer)
            }

            targetMsg.fullText += delta.content
            if (targetMsg.fullText.includes('<think>')) {
              if (targetMsg.fullText.includes('</think>')) {
                const parts = targetMsg.fullText.split('</think>')
                targetMsg.thought = parts[0].replace('<think>', '').trim()
                targetMsg.content = parts[1].trim()
                if (targetMsg.isThinking) {
                  targetMsg.isThinking = false
                  if (thoughtTimer) clearInterval(thoughtTimer)
                }
              } else {
                targetMsg.thought = targetMsg.fullText.replace('<think>', '').trim()
                if (!targetMsg.isThinking) {
                  targetMsg.isThinking = true
                  targetMsg.thoughtExpanded = true
                  if (thoughtTimer) clearInterval(thoughtTimer)
                  thoughtTimer = setInterval(() => {
                    targetMsg.thinkingTime++
                  }, 1000)
                }
              }
            } else {
              targetMsg.content += delta.content
            }
          }

          if (changed) {
            throttledRender(targetMsg)
            throttledScroll()
          }
        } catch (e) {
          console.error(e)
        }
      }
    }

    if (thoughtTimer) clearInterval(thoughtTimer)
    if (targetMsg.isStreaming) {
      const recovered = useAgent.value && !targetMsg.content
        ? await recoverAssistantMessageFromHistory(currentConversationId.value, targetMsg)
        : false
      if (!recovered) {
        finalRender(targetMsg)
      }
      if (useAgent.value && agentStatus.value === 'running') {
        agentStatus.value = recovered ? 'completed' : 'failed'
        agentStatusText.value = recovered
          ? '智能体已完成本轮任务。'
          : '智能体已结束，但未收到完整的流式收尾事件。'
        targetMsg.agentEventsExpanded = false
      }
    }
    targetMsg.suggestionsLoading = false
    await loadConversations()
    resolveConversationModel(conversations.value.find(item => item.conversationId === currentConversationId.value), true)
  } catch (error) {
    ElMessage.error('服务连接失败')
    const errMsg = messages.value[aiIndex]
    errMsg.content = '抱歉，系统暂时无法响应您的请求。'
    errMsg.suggestionsLoading = false
    if (useAgent.value) {
      agentStatus.value = 'failed'
      agentStatusText.value = '智能体服务连接失败'
      errMsg.agentEventsExpanded = false
    }
    finalRender(errMsg)
  } finally {
    setLoadingForRequest(requestId, false)
  }
}

onMounted(async () => {
  await loadCapabilities()
  await loadManagedModels()
  await loadConversations()
  createNewConversation()
  resetAgentRuntime()
})

onBeforeUnmount(() => {
  if (renderTimer) clearTimeout(renderTimer)
  if (scrollTimer) clearTimeout(scrollTimer)
})
</script>

<style lang="scss" scoped>
.chat-room {
  display: grid;
  grid-template-columns: 232px 1fr;
  gap: 14px;
  height: 100%;
  max-height: 100%;
  min-height: 0;
  overflow: hidden;
  transition: grid-template-columns 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:has(.conversation-sidebar.collapsed) {
    grid-template-columns: 48px 1fr;
  }
}

.conversation-sidebar,
.chat-main {
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 20px;
  backdrop-filter: blur(18px);
  overflow: hidden;
}

.conversation-sidebar {
  display: flex;
  flex-direction: column;
  min-height: 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;

  &.collapsed {
    .sidebar-header {
      padding: 18px 10px;
      justify-content: center;
    }
  }
}

.chat-header,
.chat-input {
  padding: 14px 16px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.18);
}

.sidebar-header {
  padding: 14px 16px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  transition: all 0.3s ease;

  &__title {
    flex: 1;
    min-width: 0;
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
  }
}

.sidebar-toggle {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(99, 102, 241, 0.08);
  color: #6366f1;
  flex-shrink: 0;

  &:hover {
    background: rgba(99, 102, 241, 0.16);
  }
}

.sidebar-header h3,
.chat-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
}

.sidebar-header h3 {
  font-size: 15px;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-header p,
.chat-header p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 12px;
}

.sidebar-new-chat {
  padding: 0 10px 10px;
}

.new-chat-btn {
  width: 100%;
  border-radius: 12px;
}

.conversation-list {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding: 10px;
}

.conversation-item {
  width: 100%;
  border: 1px solid transparent;
  background: #fff;
  border-radius: 14px;
  padding: 8px 10px;
  margin-bottom: 6px;
  text-align: left;
  cursor: pointer;
  transition: 0.2s ease;
  animation: fadeInUp 0.3s ease both;
}

.conversation-item:hover,
.conversation-item.active {
  border-color: rgba(59, 130, 246, 0.32);
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
}

.conversation-item__top,
.conversation-item__meta,
.chat-header,
.chat-header__left,
.chat-header__right,
.chat-input__actions,
.toggle-group,
.message-actions,
.thought-card__header,
.thought-card__title,
.model-option {
  display: flex;
  align-items: center;
  gap: 10px;
}

.conversation-item__top,
.conversation-item__meta,
.chat-input__actions {
  justify-content: space-between;
}

.conversation-item__title {
  font-weight: 700;
  color: #0f172a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-item__meta,
.conversation-item__time,
.model-option__meta,
.message-actions {
  color: #64748b;
  font-size: 11px;
}

.conversation-item__meta {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.conversation-item__model {
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: inline-block;
  vertical-align: bottom;
}

.meta-dot {
  margin: 0 2px;
  color: #94a3b8;
}

.chat-main {
  display: flex;
  flex-direction: column;
  min-height: 0;
  position: relative;
}

.messages-shell {
  position: relative;
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.chat-header {
  justify-content: space-between;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(241, 245, 249, 0.8));
}

.chat-header__right {
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.agent-trace-card {
  border-radius: 16px;
  padding: 10px 12px;
  background: linear-gradient(135deg, rgba(239, 246, 255, 0.92), rgba(248, 250, 252, 0.96));
  border: 1px solid rgba(37, 99, 235, 0.14);
}

.agent-trace-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  user-select: none;
}

.agent-trace-card__title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #1e40af;
}

.agent-trace-card__header .el-icon {
  color: #64748b;
  font-size: 14px;
  transition: transform 0.25s ease;
}

.rotated-down {
  transform: rotate(90deg);
}

.agent-trace-card__body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 10px;
  max-height: 260px;
  overflow: auto;
  padding-right: 4px;
}

.agent-todo-list {
  display: grid;
  gap: 6px;
}

.agent-todo-item {
  display: grid;
  grid-template-columns: 8px minmax(0, 1fr) auto;
  align-items: center;
  gap: 8px;
  min-height: 34px;
  padding: 7px 9px;
  border-radius: 8px;
  background: rgba(248, 250, 252, 0.96);
  border: 1px solid rgba(148, 163, 184, 0.18);
}

.agent-todo-item__dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: #94a3b8;
}

.agent-todo-item.is-in_progress .agent-todo-item__dot {
  background: #f59e0b;
}

.agent-todo-item.is-completed .agent-todo-item__dot {
  background: #16a34a;
}

.agent-todo-item.is-cancelled .agent-todo-item__dot {
  background: #ef4444;
}

.agent-todo-item__content {
  min-width: 0;
  color: #1f2937;
  font-size: 13px;
  line-height: 1.45;
  word-break: break-word;
}

.agent-todo-item__status {
  color: #64748b;
  font-size: 12px;
  white-space: nowrap;
}

.agent-trace-card__item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(148, 163, 184, 0.14);
}

.agent-trace-card__type {
  flex: 0 0 auto;
  min-width: 52px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
  line-height: 1.6;
}

.agent-trace-card__text {
  flex: 1;
  margin: 0;
  color: #334155;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
}

.model-select {
  width: 220px;
}

.messages-panel {
  position: relative;
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding: 16px 18px 20px;
  overscroll-behavior: contain;
  scrollbar-gutter: stable;
  background:
    radial-gradient(circle at top right, rgba(59, 130, 246, 0.08), transparent 24%),
    radial-gradient(circle at bottom left, rgba(16, 185, 129, 0.08), transparent 22%),
    #f8fafc;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100%;
  text-align: center;
  color: #475569;
}

.empty-state h3 {
  margin: 18px 0 8px;
  color: #0f172a;
}

.suggestion-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  width: min(640px, 100%);
  margin-top: 22px;
}

.suggestion-card {
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  background: rgba(255, 255, 255, 0.9);
  border-radius: 18px;
  padding: 14px 16px;
  cursor: pointer;
  transition: 0.2s ease;
}

.suggestion-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 16px 32px rgba(37, 99, 235, 0.12);
  border-color: rgba(37, 99, 235, 0.28);
}

.suggestion-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: rgba(37, 99, 235, 0.1);
  color: #2563eb;
}

.suggestion-card__icon :deep(.el-icon) {
  font-size: 18px;
}

.message-row {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}

.message-row--user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex: 0 0 auto;
}

.user-avatar {
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563eb, #0ea5e9);
  color: #fff;
}

.message-body {
  max-width: min(84%, 1120px);
}

.message-bubble {
  border-radius: 20px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(148, 163, 184, 0.18);
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.05);
}

.message-row--user .message-bubble {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
}

.assistant-content,
.thought-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.thought-card {
  border-radius: 16px;
  padding: 10px 12px;
  background: #eff6ff;
}

.thought-card__header {
  justify-content: space-between;
  cursor: pointer;
}

.thought-card__content {
  color: #334155;
}

.rotated {
  transform: rotate(90deg);
}

.message-text {
  line-height: 1.65;
  word-break: break-word;
}

.message-suggestions {
  margin-top: 10px;
}

.message-suggestions__label {
  margin-bottom: 8px;
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
}

.message-suggestions__loading {
  padding: 10px 12px;
  border-radius: 14px;
  background: rgba(248, 250, 252, 0.95);
  border: 1px dashed rgba(148, 163, 184, 0.4);
  color: #64748b;
  font-size: 13px;
}

.message-suggestions__list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.message-suggestion-chip {
  max-width: 100%;
  border: 1px solid rgba(59, 130, 246, 0.18);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(239, 246, 255, 0.92));
  color: #0f172a;
  border-radius: 999px;
  padding: 10px 14px;
  line-height: 1.5;
  text-align: left;
  white-space: normal;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.message-suggestion-chip:hover {
  transform: translateY(-1px);
  border-color: rgba(37, 99, 235, 0.32);
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.12);
}

.streaming-loader {
  display: inline-flex;
  gap: 6px;
}

.streaming-loader span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #3b82f6;
  animation: thinking-wave 1.2s infinite ease-in-out;
}

.streaming-loader span:nth-child(2) {
  animation-delay: 0.15s;
  background: #6366f1;
}

.streaming-loader span:nth-child(3) {
  animation-delay: 0.3s;
  background: #8b5cf6;
}

@keyframes thinking-wave {
  0%, 60%, 100% { transform: scale(0.8); opacity: 0.4; }
  30%           { transform: scale(1.5); opacity: 1; }
}

// 消息气泡入场动画
.msg-enter-active {
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.msg-enter-from {
  opacity: 0;
  transform: translateY(16px) scale(0.95);
}

// 对话列表项动画
.conversation-item {
  animation: fadeInUp 0.3s ease both;
}

// 流式光标
.stream-cursor {
  display: inline-block;
  margin-left: 2px;
  color: #3b82f6;
  font-weight: bold;
  animation: cursor-blink 0.8s step-end infinite;
}

@keyframes cursor-blink {
  0%, 100% { opacity: 1; }
  50%       { opacity: 0; }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(12px); }
  to   { opacity: 1; transform: translateY(0); }
}

.scroll-bottom-btn,
.icon-btn {
  border: none;
  cursor: pointer;
}

.icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 999px;
  background: rgba(148, 163, 184, 0.12);
  color: #475569;
}

.scroll-bottom-btn {
  position: absolute;
  right: 18px;
  bottom: 18px;
  z-index: 2;
  width: 42px;
  height: 42px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: #0f172a;
  color: #fff;
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.18);
}

.messages-panel > .scroll-bottom-btn {
  display: none;
}

.chat-input {
  border-top: 1px solid rgba(148, 163, 184, 0.18);
  border-bottom: none;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.mounted-skill-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 2px 0;
}

.mounted-skill-list,
.selected-skill-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-width: 0;
}

.skill-mode-hint {
  flex: 0 0 auto;
  font-size: 12px;
  color: #b45309;
}

.chat-input :deep(.el-textarea__inner) {
  padding: 10px 12px;
  line-height: 1.6;
  border-radius: 14px;
}

.toggle-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  background: #fff;
  color: #334155;
  border-radius: 999px;
  padding: 7px 12px;
  cursor: pointer;
}

.toggle-chip.active {
  border-color: rgba(37, 99, 235, 0.4);
  background: rgba(37, 99, 235, 0.1);
  color: #1d4ed8;
}

.toggle-chip--agent {
  border-color: rgba(14, 116, 144, 0.22);
  color: #0f766e;
}

.toggle-chip--agent.active {
  border-color: rgba(13, 148, 136, 0.4);
  background: rgba(20, 184, 166, 0.12);
  color: #0f766e;
}

.toggle-chip--disabled,
.toggle-chip:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.skill-picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.skill-picker-header h3 {
  margin: 0;
  font-size: 18px;
  color: #0f172a;
}

.skill-picker-header p {
  margin: 2px 0 0;
  color: #64748b;
  font-size: 12px;
}

.skill-picker {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 16px;
  min-height: 540px;
}

.skill-picker-side {
  border-right: 1px solid rgba(148, 163, 184, 0.18);
  padding-right: 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.create-skill-btn {
  width: 100%;
}

.skill-source-tab {
  width: 100%;
  min-height: 40px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #334155;
  text-align: left;
  padding: 0 12px;
  cursor: pointer;
  font-weight: 600;
}

.skill-source-tab.active,
.skill-source-tab:hover {
  background: rgba(15, 23, 42, 0.06);
  color: #0f172a;
}

.skill-filter-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-top: 10px;
  border-top: 1px solid rgba(148, 163, 184, 0.14);
}

.skill-filter-block > span {
  color: #64748b;
  font-size: 12px;
}

.skill-picker-main {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.skill-option-list {
  min-height: 0;
  max-height: 430px;
  overflow: auto;
  border-radius: 10px;
  background: #f8fafc;
}

.skill-option {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.14);
}

.skill-option-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(37, 99, 235, 0.1);
  color: #2563eb;
  font-weight: 800;
}

.skill-option-title {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.skill-option-title strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.skill-option-body p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.5;
}

.search-card {
  margin-bottom: 12px;
  padding: 12px;
  border-radius: 16px;
  border: 1px solid rgba(14, 116, 144, 0.14);
  background:
    linear-gradient(135deg, rgba(240, 249, 255, 0.92), rgba(248, 250, 252, 0.96));
}

.search-card__header,
.search-source__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.search-card__title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #0f172a;
}

.search-card__query {
  margin-top: 8px;
  color: #334155;
  font-size: 13px;
}

.search-source-list {
  display: grid;
  gap: 10px;
  margin-top: 12px;
}

.search-source {
  display: block;
  padding: 12px;
  border-radius: 14px;
  text-decoration: none;
  color: inherit;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(148, 163, 184, 0.16);
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.search-source:hover {
  transform: translateY(-1px);
  border-color: rgba(37, 99, 235, 0.28);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
}

.search-source__title {
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
}

.search-source__domain {
  font-size: 12px;
  color: #0f766e;
}

.search-source__snippet,
.search-card__fallback {
  margin: 8px 0 0;
  color: #475569;
  font-size: 13px;
  line-height: 1.6;
}

.markdown-body :deep(pre) {
  overflow-x: auto;
}

@media (max-width: 1100px) {
  .chat-room {
    grid-template-columns: 1fr;
  }

  .conversation-sidebar {
    max-height: 240px;
  }

  .message-body {
    max-width: 88%;
  }

  .suggestion-grid {
    grid-template-columns: 1fr;
  }

  .agent-trace-card__item {
    flex-direction: column;
    gap: 4px;
  }

  .mounted-skill-bar {
    align-items: flex-start;
    flex-direction: column;
  }

  .skill-picker {
    grid-template-columns: 1fr;
  }

  .skill-picker-side {
    border-right: none;
    border-bottom: 1px solid rgba(148, 163, 184, 0.18);
    padding-right: 0;
    padding-bottom: 12px;
  }

  .skill-option {
    grid-template-columns: 40px minmax(0, 1fr);
  }

  .skill-option .el-button {
    grid-column: 1 / -1;
  }
}
</style>
