<template>
  <div class="chat-room">
    <aside :class="['conversation-sidebar', { collapsed: sidebarCollapsed }]">
      <div class="sidebar-header">
        <div v-if="!sidebarCollapsed" class="sidebar-header__title">
          <h3>对话历史</h3>
        </div>
        <div class="sidebar-header__actions">
          <el-button v-if="!sidebarCollapsed" type="primary" @click="createNewConversation">
            <el-icon><Plus /></el-icon>
            新对话
          </el-button>
          <button class="icon-btn sidebar-toggle" type="button" aria-label="切换会话侧栏" @click="sidebarCollapsed = !sidebarCollapsed">
            <el-icon><ArrowLeft v-if="!sidebarCollapsed" /><ArrowRight v-else /></el-icon>
          </button>
        </div>
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
          <el-icon :size="42"><ChatDotRound /></el-icon>
          <h3>开始一段新的对话</h3>
          <p>当前支持在聊天中切换不同的基座模型，会话会记录最近一次使用的模型。</p>
          <div class="suggestion-grid">
            <button
              v-for="(item, index) in suggestionCards"
              :key="index"
              class="suggestion-card"
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

                <div class="message-text markdown-body" v-html="msg.renderedHtml || ''"></div>
                <span v-if="msg.isStreaming && msg.content" class="stream-cursor">|</span>
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
            <button :class="['toggle-chip', { active: useDeepThinking }]" type="button" @click="useDeepThinking = !useDeepThinking">
              <el-icon><Cpu /></el-icon>
              深度思考
            </button>
            <button :class="['toggle-chip', { active: useWebSearch }]" type="button" @click="useWebSearch = !useWebSearch">
              <el-icon><Compass /></el-icon>
              联网搜索
            </button>
          </div>

          <el-button type="primary" :loading="loading" :disabled="!inputToSent.trim()" @click="handleSend">
            <el-icon><ArrowUpBold /></el-icon>
            发送
          </el-button>
        </div>
      </footer>
    </section>
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
import { getAiModelList } from '@/api/ai-model'

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
const conversations = ref([])
const managedModels = ref([])
const defaultModelId = ref(null)
const selectedModelId = ref(null)
const inputToSent = ref('')
const loading = ref(false)
const messagesContainer = ref(null)
const currentConversationId = ref(null)
const useWebSearch = ref(false)
const useDeepThinking = ref(false)
const sidebarCollapsed = ref(false)
const userScrolledUp = ref(false)

const selectedModel = computed(() => managedModels.value.find(item => item.id === selectedModelId.value) || null)
const currentModelLabel = computed(() => {
  if (selectedModel.value) return selectedModel.value.displayName
  if (managedModels.value.length === 0) return '配置文件默认模型'
  return '未选择模型'
})
const conversationSummary = computed(() => {
  const count = conversations.value.length
  const model = currentModelLabel.value
  return `当前模型：${model} · 已保存 ${count} 个会话`
})

let renderTimer = null
let scrollTimer = null

const copyMessage = (content) => {
  navigator.clipboard.writeText(content).then(() => {
    ElMessage.success({ message: '已复制到剪贴板', duration: 1500 })
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

const doRender = (msg) => {
  if (msg.content) msg.renderedHtml = md.render(msg.content)
  if (msg.thought) msg.renderedThoughtHtml = md.render(msg.thought)
}

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
    timestamp: new Date(msg.timestamp).getTime(),
    thoughtExpanded: false,
    isThinking: false,
    isStreaming: false,
    fullText: '',
    renderedHtml: '',
    renderedThoughtHtml: ''
  }
  if (msg.role !== 'user') doRender(item)
  return item
}

const switchConversation = async (conversationId) => {
  currentConversationId.value = conversationId
  resolveConversationModel(conversations.value.find(item => item.conversationId === conversationId))
  try {
    const response = await axios.get(`/api/ai/history/conversations/${conversationId}/messages`)
    messages.value = (response.data.data || []).map(mapHistoryMessage)
    userScrolledUp.value = false
    await scrollToBottom(true)
  } catch (error) {
    console.error('加载会话消息失败', error?.response?.status, error?.response?.data)
    ElMessage.error('加载会话消息失败')
  }
}

const createNewConversation = () => {
  currentConversationId.value = crypto.randomUUID()
  messages.value = []
  selectedModelId.value = defaultModelId.value
  userScrolledUp.value = false
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
  selectedModelId.value = defaultModelId.value
}

const normalizeCapabilitiesBeforeSend = () => {
  if (!selectedModel.value) return
  if (useDeepThinking.value && !selectedModel.value.supportsDeepThinking) {
    useDeepThinking.value = false
    ElMessage.warning('当前模型不支持深度思考，已自动关闭')
  }
  if (useWebSearch.value && !selectedModel.value.supportsWebSearch) {
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
  loading.value = true
  userScrolledUp.value = false
  await scrollToBottom(true)

  const aiIndex = messages.value.length
  messages.value.push({
    role: 'assistant',
    thought: '',
    content: '',
    fullText: '',
    isThinking: false,
    isStreaming: true,
    thoughtExpanded: true,
    thinkingTime: 0,
    timestamp: Date.now(),
    renderedHtml: '',
    renderedThoughtHtml: ''
  })

  if (!currentConversationId.value) {
    currentConversationId.value = crypto.randomUUID()
  }

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
        useWebSearch: useWebSearch.value,
        useDeepThinking: useDeepThinking.value
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
            finalRender(targetMsg)
            return
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
    finalRender(targetMsg)
    await loadConversations()
    resolveConversationModel(conversations.value.find(item => item.conversationId === currentConversationId.value), true)
  } catch (error) {
    ElMessage.error('服务连接失败')
    const errMsg = messages.value[aiIndex]
    errMsg.content = '抱歉，系统暂时无法响应您的请求。'
    finalRender(errMsg)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadManagedModels()
  await loadConversations()
  createNewConversation()
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

.sidebar-header,
.chat-header,
.chat-input {
  padding: 14px 16px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.18);
}

.sidebar-header {
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

.model-select {
  width: 220px;
}

.messages-panel {
  position: relative;
  height: 100%;
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
}
</style>
