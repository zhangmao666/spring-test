<template>
  <div class="chat-wrapper-full" :class="`theme-${currentTheme}`">
    <!-- 背景装饰点缀 (保留原有特色) -->
    <div class="chat-bg-glow"></div>

    <div class="main-layout">
      <!-- 左侧会话列表 -->
      <div class="sidebar" :class="{ collapsed: sidebarCollapsed }">
        <div class="sidebar-header">
          <h3 v-if="!sidebarCollapsed">对话历史</h3>
          <el-button
            type="primary"
            :circle="sidebarCollapsed"
            @click="createNewConversation"
            :icon="sidebarCollapsed ? 'Plus' : ''"
            class="new-conversation-btn"
          >
            <template v-if="!sidebarCollapsed">
              <el-icon><Plus /></el-icon> 新对话
            </template>
          </el-button>
        </div>
        
        <div class="conversation-list" v-if="!sidebarCollapsed">
          <div 
            v-for="conv in conversations" 
            :key="conv.conversationId"
            class="conversation-item"
            :class="{ active: currentConversationId === conv.conversationId }"
            @click="switchConversation(conv.conversationId)"
          >
            <div class="conv-title">{{ conv.title }}</div>
            <div class="conv-meta">
              <span class="msg-count">{{ conv.messageCount }} 条消息</span>
              <span class="time">{{ formatDate(conv.lastMessageTime) }}</span>
            </div>
            <el-button 
              circle 
              text 
              class="delete-btn"
              @click.stop="deleteConversation(conv.conversationId)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
        
        <div class="sidebar-toggle" @click="sidebarCollapsed = !sidebarCollapsed">
          <el-icon>
            <component :is="sidebarCollapsed ? 'ArrowRight' : 'ArrowLeft'" />
          </el-icon>
        </div>
      </div>

      <!-- 主聊天区域 -->
      <div class="chat-main">
        <div class="chat-container glass-container">
          <div class="chat-header">
            <div class="header-left">
              <div class="status-indicator">
                <el-avatar :size="50" :src="aiAvatar" class="ai-avatar-main" />
                <div class="status-dot online"></div>
              </div>
              <div class="header-info">
                <span class="title">AI 智能助手</span>
                <span class="subtitle">✨DeepSeek-v3.2✨</span>
              </div>
            </div>
            <div class="header-right">
              <el-tooltip content="清空当前对话" placement="bottom">
                <el-button circle @click="clearCurrentHistory">
                  <el-icon><RefreshRight /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </div>

          <div class="messages-area" ref="messagesContainer" @scroll="handleScroll">
            <transition-group name="msg-list">
              <div v-if="messages.length === 0" class="empty-state" key="empty">
                <div class="empty-hero">
                  <div class="empty-icon-ring">
                    <el-icon :size="36"><ChatDotRound /></el-icon>
                  </div>
                  <h3>你好！我是您的 AI 助手</h3>
                  <p>我可以帮您进行投资分析、代码编写或日常答疑，试试下面的话题吧。</p>
                </div>
                <div class="suggestion-grid">
                  <div v-for="(item, idx) in suggestionCards" :key="idx" class="suggestion-card" @click="inputToSent = item.text">
                    <div class="suggestion-icon">{{ item.icon }}</div>
                    <div class="suggestion-text">{{ item.text }}</div>
                  </div>
                </div>
              </div>
              
              <div 
                v-for="(msg, index) in messages" 
                :key="index"
                :class="['message-item', msg.role === 'user' ? 'user-message' : 'ai-message']"
              >
                <div class="avatar-box">
                  <el-avatar v-if="msg.role !== 'user'" :size="40" :src="aiAvatar" />
                  <div v-else class="user-avatar-placeholder">
                    <el-icon><UserFilled /></el-icon>
                  </div>
                </div>
                <div class="content-box">
                  <div class="bubble glass-container">
                    <div class="text" v-if="msg.role === 'user'">{{ msg.content }}</div>
                    <div v-else class="ai-content-wrapper">
                      <!-- 等待动画：流式输出且无内容时显示 -->
                      <div v-if="msg.isStreaming && !msg.content && !msg.thought" class="streaming-loader">
                        <span></span><span></span><span></span>
                      </div>
                      <!-- 深度思考展示区 -->
                      <div v-if="msg.thought" class="thought-container">
                        <div class="thought-header" @click="msg.thoughtExpanded = !msg.thoughtExpanded">
                          <div class="thought-title">
                            <el-icon class="thought-icon" :class="{ spinning: msg.isThinking }"><Cpu /></el-icon>
                            <span>{{ msg.isThinking ? '正在深度思考...' : `已深度思考 (用时 ${msg.thinkingTime || 0}s)` }}</span>
                          </div>
                          <el-icon :class="['expand-icon', { expanded: msg.thoughtExpanded }]"><ArrowRight /></el-icon>
                        </div>
                        <transition name="expand">
                          <div v-show="msg.thoughtExpanded" class="thought-content-box">
                            <div class="thought-inner markdown-body" v-html="msg.renderedThoughtHtml || ''"></div>
                          </div>
                        </transition>
                      </div>
                      
                      <!-- 回答正文 -->
                      <div class="text markdown-body" v-html="msg.renderedHtml || ''"></div>
                    </div>
                  </div>
                  <div class="message-actions" v-if="msg.content">
                    <button class="action-btn" @click="copyMessage(msg.content)" title="复制">
                      <el-icon :size="14"><CopyDocument /></el-icon>
                    </button>
                    <button class="action-btn" :disabled="loading" @click="regenerateFromMessage(index)" title="重新生成">
                      <el-icon :size="14"><RefreshRight /></el-icon>
                    </button>
                    <span class="message-time">{{ formatTime(msg.timestamp) }}</span>
                  </div>
                  <div v-else class="message-meta">{{ formatTime(msg.timestamp) }}</div>
                </div>
              </div>
            </transition-group>

            <!-- 回到底部按钮 -->
            <transition name="fade-up">
              <div v-if="userScrolledUp" class="scroll-bottom-btn" @click="scrollToBottom(true)">
                <el-icon><ArrowDown /></el-icon>
              </div>
            </transition>
          </div>

          <div class="input-area-wrapper">
            <div class="input-card glass-container">
              <el-input
                v-model="inputToSent"
                type="textarea"
                :autosize="{ minRows: 1, maxRows: 6 }"
                placeholder="给 AI 助手发送消息"
                class="main-input"
                resize="none"
                @keydown.enter.exact.prevent="handleSend"
              />
              
              <div class="action-bar">
                <div class="toggles">
                  <div 
                    class="toggle-chip" 
                    :class="{ active: useDeepThinking }" 
                    @click="useDeepThinking = !useDeepThinking"
                  >
                    <el-icon><Cpu /></el-icon> 深度思考
                  </div>
                  <div 
                    class="toggle-chip" 
                    :class="{ active: useWebSearch }"
                    @click="useWebSearch = !useWebSearch"
                  >
                    <el-icon><Compass /></el-icon> 联网搜索
                  </div>
                </div>

                <div class="right-actions">
                  <el-button 
                    type="primary" 
                    circle 
                    class="send-btn" 
                    @click="handleSend" 
                    :loading="loading"
                    :disabled="!inputToSent.trim()"
                  >
                    <el-icon><ArrowUpBold /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onBeforeUnmount, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ChatDotRound, Delete, UserFilled, Plus, RefreshRight,
  Cpu, Compass, ArrowUpBold, ArrowRight, ArrowLeft, ArrowDown,
  CopyDocument
} from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import aiAvatar from '@/assets/ai_avatar.png'
import { useThemeStore } from '@/stores/theme'
import axios from 'axios'

const themeStore = useThemeStore()
const currentTheme = computed(() => themeStore.currentTheme)

const suggestionCards = [
  { icon: '📊', text: '分析今日热门板块' },
  { icon: '💻', text: '写一个快速排序算法' },
  { icon: '📰', text: '总结最近的财报热点' },
  { icon: '📚', text: '推荐几本金融入门书' }
]

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: function (str, lang) {
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
    return `<div class="code-block-wrapper"><div class="code-block-header"><span class="code-lang">${langLabel}</span><button class="code-copy-btn" onclick="navigator.clipboard.writeText(this.closest('.code-block-wrapper').querySelector('code').textContent).then(()=>{this.textContent='已复制!';setTimeout(()=>{this.textContent='复制'},1500)})">复制</button></div><pre><code class="hljs language-${langLabel}">${highlighted}</code></pre></div>`
  }
})

// 自定义渲染规则 - 让链接在新窗口打开
const defaultRender = md.renderer.rules.link_open || function(tokens, idx, options, env, self) {
  return self.renderToken(tokens, idx, options)
}
md.renderer.rules.link_open = function (tokens, idx, options, env, self) {
  tokens[idx].attrSet('target', '_blank')
  tokens[idx].attrSet('rel', 'noopener noreferrer')
  return defaultRender(tokens, idx, options, env, self)
}

// 复制消息内容
const copyMessage = (content) => {
  navigator.clipboard.writeText(content).then(() => {
    ElMessage.success({ message: '已复制到剪贴板', duration: 1500 })
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

// === 节流渲染工具 ===
let _renderTimer = null
let _scrollTimer = null

const throttledRender = (msg) => {
  if (_renderTimer) return
  _renderTimer = setTimeout(() => {
    _renderTimer = null
    doRender(msg)
  }, 80)
}

const doRender = (msg) => {
  if (msg.content) {
    msg.renderedHtml = md.render(msg.content)
  }
  if (msg.thought) {
    msg.renderedThoughtHtml = md.render(msg.thought)
  }
}

const finalRender = (msg) => {
  if (_renderTimer) { clearTimeout(_renderTimer); _renderTimer = null }
  doRender(msg)
  msg.isStreaming = false
}

const throttledScroll = () => {
  if (_scrollTimer) return
  _scrollTimer = setTimeout(async () => {
    _scrollTimer = null
    if (userScrolledUp.value) return
    await nextTick()
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  }, 80)
}

// === 状态管理 ===
const messages = ref([])
const conversations = ref([])
const sidebarCollapsed = ref(false)
const inputToSent = ref('')
const loading = ref(false)
const messagesContainer = ref(null)
const currentConversationId = ref(null)
const useWebSearch = ref(false)
const useDeepThinking = ref(false)
const userScrolledUp = ref(false)

// === 滚动控制 ===
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

// === 时间格式化 ===
const formatTime = (ts) => {
  if (!ts) return ''
  const d = new Date(ts)
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

// === 会话管理 ===
const loadConversations = async () => {
  try {
    const response = await axios.get('/api/ai/history/conversations')
    conversations.value = response.data.data || []
  } catch (error) {
    console.error('加载会话列表失败', error)
  }
}

const switchConversation = async (conversationId) => {
  currentConversationId.value = conversationId
  try {
    const response = await axios.get(`/api/ai/history/conversations/${conversationId}/messages`)
    const historyMessages = response.data.data || []
    messages.value = historyMessages.map(msg => {
      const item = {
        role: msg.role,
        content: msg.content,
        thought: msg.thought,
        thinkingTime: msg.thinkingTime,
        timestamp: new Date(msg.timestamp).getTime(),
        thoughtExpanded: false,
        isStreaming: false,
        renderedHtml: '',
        renderedThoughtHtml: ''
      }
      if (msg.role !== 'user') {
        if (item.content) item.renderedHtml = md.render(item.content)
        if (item.thought) item.renderedThoughtHtml = md.render(item.thought)
      }
      return item
    })
    userScrolledUp.value = false
    await scrollToBottom(true)
  } catch (error) {
    ElMessage.error('加载会话消息失败')
  }
}

const createNewConversation = () => {
  currentConversationId.value = crypto.randomUUID()
  messages.value = []
  userScrolledUp.value = false
}

const deleteConversation = async (conversationId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个对话吗？', '提示', {
      type: 'warning'
    })
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
}

// === 消息发送 ===
const regenerateFromMessage = async (index) => {
  if (loading.value) return

  const target = messages.value[index]
  if (!target || !target.content) return

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

const handleSend = async () => {
  const content = inputToSent.value.trim()
  if (!content || loading.value) return

  messages.value.push({
    role: 'user',
    content: content,
    timestamp: Date.now()
  })
  
  inputToSent.value = ''
  loading.value = true
  userScrolledUp.value = false // 发送新消息时重置滚动状态
  await scrollToBottom(true)

  const aiIndex = messages.value.length
  messages.value.push({
    role: 'ai',
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
        'Authorization': token ? `Bearer ${token}` : ''
      },
      body: JSON.stringify({
        message: content,
        provider: 'openai',
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
          if (delta) {
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
          }
        } catch (e) {}
      }
    }
    if (thoughtTimer) clearInterval(thoughtTimer)
    finalRender(targetMsg)
    await loadConversations()
  } catch (error) {
    ElMessage.error('服务连接失败')
    const errMsg = messages.value[aiIndex]
    errMsg.content = '抱歉，系统暂时无法响应您的请求。'
    finalRender(errMsg)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadConversations()
  createNewConversation()
})

onBeforeUnmount(() => {
  if (_renderTimer) clearTimeout(_renderTimer)
  if (_scrollTimer) clearTimeout(_scrollTimer)
})
</script>

<style lang="scss" scoped>
$primary: #6366f1;
$primary-light: rgba(99, 102, 241, 0.1);

.chat-wrapper-full {
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  background-color: var(--bg-color);
}

.main-layout {
  display: flex;
  flex: 1;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

// ========== 侧边栏 ==========
.sidebar {
  width: 260px;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
  border-right: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 10;
  
  &.collapsed {
    width: 0;
    border-right: none;
    .sidebar-header, .conversation-list { opacity: 0; pointer-events: none; }
  }
  
  .sidebar-header {
    padding: 24px 20px;
    display: flex;
    align-items: center;
    justify-content: space-between;

    h3 {
      font-size: 1.1rem;
      font-weight: 800;
      color: var(--text-primary);
      margin: 0;
    }

    .new-conversation-btn {
      min-width: 100px;
      padding: 8px 16px !important;
      font-size: 0.9rem;
      font-weight: 600;
      white-space: nowrap;

      .el-icon {
        margin-right: 4px;
      }
    }
  }
  
  .conversation-list {
    flex: 1;
    overflow-y: auto;
    padding: 0 12px 20px;
    
    &::-webkit-scrollbar { width: 4px; }
    &::-webkit-scrollbar-thumb {
      background: rgba(0,0,0,0.1);
      border-radius: 2px;
    }
    
    .conversation-item {
      padding: 12px 16px;
      margin-bottom: 8px;
      border-radius: 12px;
      background: rgba(255, 255, 255, 0.5);
      border: 1px solid transparent;
      cursor: pointer;
      transition: all 0.2s;
      position: relative;
      
      &:hover {
        background: #fff;
        border-color: rgba($primary, 0.2);
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(0,0,0,0.05);
        .delete-btn { opacity: 1; }
      }
      
      &.active {
        background: #fff;
        border-color: var(--primary-color);
        box-shadow: 0 4px 15px rgba($primary, 0.1);
        .conv-title { color: var(--primary-color); }
      }
      
      .conv-title {
        font-size: 0.9rem;
        color: var(--text-primary);
        font-weight: 600;
        margin-bottom: 4px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        padding-right: 20px;
      }
      
      .conv-meta {
        display: flex;
        justify-content: space-between;
        font-size: 0.75rem;
        color: var(--text-secondary);
        opacity: 0.7;
      }
      
      .delete-btn {
        position: absolute;
        top: 10px;
        right: 8px;
        opacity: 0;
        transition: opacity 0.2s;
        color: #94a3b8;
        &:hover { color: var(--el-color-danger); }
      }
    }
  }
  
  .sidebar-toggle {
    position: absolute;
    right: -12px;
    top: 50%;
    transform: translateY(-50%);
    width: 24px;
    height: 48px;
    background: #fff;
    border: 1px solid rgba(0,0,0,0.05);
    border-radius: 0 8px 8px 0;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--primary-color);
    cursor: pointer;
    z-index: 100;
    box-shadow: 4px 0 10px rgba(0,0,0,0.05);
    
    &:hover {
      color: var(--primary-dark);
      background: #f8fafc;
    }
  }
}

// ========== 主聊天区 ==========
.chat-main {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  height: 100%;
}

.chat-bg-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 800px;
  height: 800px;
  background: radial-gradient(circle, rgba($primary, 0.06) 0%, transparent 70%);
  transform: translate(-50%, -50%);
  z-index: 0;
  pointer-events: none;
}

.chat-container {
  width: 100%;
  height: 100%;
  border-radius: 0;
  border: none;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 1;
}

// ========== 顶栏 ==========
.chat-header {
  height: 70px;
  padding: 0 30px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(0,0,0,0.05);
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .status-indicator {
      position: relative;
      .ai-avatar-main { border: 2px solid #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
      .status-dot {
        position: absolute;
        bottom: 2px;
        right: 2px;
        width: 12px;
        height: 12px;
        border-radius: 50%;
        border: 2px solid #fff;
        &.online { background: #10B981; box-shadow: 0 0 6px rgba(16, 185, 129, 0.5); }
      }
    }

    .header-info {
      display: flex;
      flex-direction: column;
      .title { font-size: 1.15rem; font-weight: 800; color: var(--text-primary); }
      .subtitle { font-size: 0.8rem; color: var(--text-secondary); opacity: 0.6; }
    }
  }
}

// ========== 消息区 ==========
.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 40px 10%;
  display: flex;
  flex-direction: column;
  gap: 28px;
  scroll-behavior: smooth;
  position: relative;

  &::-webkit-scrollbar { width: 5px; }
  &::-webkit-scrollbar-thumb {
    background: rgba(0,0,0,0.08);
    border-radius: 10px;
    &:hover { background: rgba(0,0,0,0.15); }
  }
}

// ========== 空状态 ==========
.empty-state {
  text-align: center;
  margin: auto;
  max-width: 520px;
  animation: fadeInUp 0.6s ease;

  .empty-hero {
    margin-bottom: 36px;
  }

  .empty-icon-ring {
    width: 80px;
    height: 80px;
    margin: 0 auto 20px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: $primary;
    background: linear-gradient(135deg, rgba($primary, 0.08), rgba($primary, 0.18));
    box-shadow: 0 8px 30px rgba($primary, 0.12);
    animation: float 3s ease-in-out infinite;
  }

  h3 {
    font-size: 1.6rem;
    font-weight: 800;
    color: var(--text-primary);
    margin-bottom: 10px;
  }

  p {
    font-size: 0.95rem;
    color: var(--text-secondary);
    line-height: 1.7;
    opacity: 0.8;
  }

  .suggestion-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
    text-align: left;
  }

  .suggestion-card {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 16px 18px;
    background: #fff;
    border: 1px solid rgba(0,0,0,0.05);
    border-radius: 14px;
    cursor: pointer;
    transition: all 0.25s ease;

    .suggestion-icon {
      font-size: 1.5rem;
      flex-shrink: 0;
    }

    .suggestion-text {
      font-size: 0.88rem;
      color: var(--text-primary);
      font-weight: 500;
    }

    &:hover {
      border-color: rgba($primary, 0.3);
      background: rgba($primary, 0.03);
      transform: translateY(-3px);
      box-shadow: 0 8px 24px rgba(0,0,0,0.06);
    }
  }
}

// ========== 消息项 ==========
.message-item {
  display: flex;
  gap: 14px;
  max-width: 85%;
  animation: fadeInUp 0.35s ease;
  
  &.user-message {
    align-self: flex-end;
    flex-direction: row-reverse;
    .bubble {
      background: linear-gradient(135deg, $primary 0%, darken($primary, 8%) 100%);
      color: #fff;
      border: none;
      box-shadow: 0 6px 20px rgba($primary, 0.25);
      border-radius: 18px 18px 4px 18px;
    }
    .message-meta, .message-actions { text-align: right; justify-content: flex-end; }
  }

  &.ai-message {
    align-self: flex-start;
    .bubble {
      background: #fff;
      color: var(--text-primary);
      border: 1px solid rgba(0,0,0,0.05);
      box-shadow: 0 2px 12px rgba(0,0,0,0.04);
      border-radius: 18px 18px 18px 4px;
    }
  }

  .avatar-box {
    flex-shrink: 0;
    margin-top: 4px;
    .user-avatar-placeholder {
      width: 40px;
      height: 40px;
      background: linear-gradient(135deg, #e0e7ff, #c7d2fe);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.1rem;
      color: $primary;
    }
    .el-avatar {
      border-radius: 12px;
    }
  }

  .content-box {
    max-width: 100%;
    min-width: 0;

    .bubble {
      padding: 16px 20px;
      border-radius: 18px;
      line-height: 1.7;
      font-size: 0.95rem;
      word-break: break-word;
      overflow-wrap: break-word;
      
      // ===== Markdown 渲染优化 =====
      :deep(.markdown-body) {
        font-size: 0.95rem;
        color: inherit;
        line-height: 1.75;

        > *:first-child { margin-top: 0; }
        > *:last-child { margin-bottom: 0; }

        p { margin: 0.6em 0; }

        h1, h2, h3, h4 {
          margin: 1.2em 0 0.5em;
          font-weight: 700;
          color: var(--text-primary);
          &:first-child { margin-top: 0; }
        }
        h1 { font-size: 1.4em; }
        h2 { font-size: 1.2em; }
        h3 { font-size: 1.05em; }

        strong { font-weight: 700; color: var(--text-primary); }

        a {
          color: $primary;
          text-decoration: none;
          border-bottom: 1px solid rgba($primary, 0.3);
          transition: border-color 0.2s;
          &:hover { border-color: $primary; }
        }

        code {
          background: rgba(0, 0, 0, 0.06);
          padding: 2px 6px;
          border-radius: 4px;
          font-size: 0.88em;
          font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
          color: #c7254e;
        }

        // 代码块整体包装
        .code-block-wrapper {
          margin: 14px 0;
          border-radius: 10px;
          overflow: hidden;
          border: 1px solid rgba(0,0,0,0.08);
          background: #fafbfc;

          .code-block-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 6px 14px;
            background: #f0f1f3;
            border-bottom: 1px solid rgba(0,0,0,0.06);

            .code-lang {
              font-size: 0.72rem;
              font-weight: 600;
              color: #64748b;
              text-transform: uppercase;
              letter-spacing: 0.5px;
            }

            .code-copy-btn {
              padding: 3px 10px;
              font-size: 0.72rem;
              color: #64748b;
              background: rgba(255,255,255,0.7);
              border: 1px solid rgba(0,0,0,0.08);
              border-radius: 5px;
              cursor: pointer;
              transition: all 0.15s;
              &:hover {
                background: #fff;
                color: $primary;
                border-color: rgba($primary, 0.3);
              }
            }
          }

          pre {
            margin: 0 !important;
            padding: 14px 16px !important;
            background: #fafbfc !important;
            border-radius: 0 !important;
            border: none !important;
            overflow-x: auto;

            code {
              background: none !important;
              padding: 0 !important;
              color: inherit !important;
              font-size: 0.85rem;
              line-height: 1.6;
            }
          }
        }

        // 无包装的 pre（fallback）
        > pre {
          background: #fafbfc;
          border-radius: 10px;
          padding: 14px 16px;
          border: 1px solid rgba(0,0,0,0.08);
          overflow-x: auto;
          code {
            background: none !important;
            padding: 0 !important;
            color: inherit !important;
          }
        }

        blockquote {
          margin: 0.8em 0;
          padding: 10px 16px;
          border-left: 3px solid rgba($primary, 0.4);
          background: rgba($primary, 0.03);
          border-radius: 0 8px 8px 0;
          color: var(--text-secondary);
          font-style: italic;
          p { margin: 0.3em 0; }
        }

        ul, ol {
          padding-left: 1.5em;
          margin: 0.6em 0;
          li {
            margin: 0.3em 0;
            &::marker { color: rgba($primary, 0.6); }
          }
        }

        table {
          width: 100%;
          border-collapse: collapse;
          margin: 1em 0;
          font-size: 0.88em;
          border-radius: 8px;
          overflow: hidden;
          border: 1px solid rgba(0,0,0,0.08);
          th {
            background: #f8f9fa;
            font-weight: 600;
            color: var(--text-primary);
            padding: 10px 14px;
            border-bottom: 2px solid rgba(0,0,0,0.08);
          }
          td {
            padding: 8px 14px;
            border-bottom: 1px solid rgba(0,0,0,0.04);
          }
          tr:last-child td { border-bottom: none; }
          tr:hover td { background: rgba($primary, 0.02); }
        }

        hr {
          border: none;
          height: 1px;
          background: rgba(0,0,0,0.08);
          margin: 1.2em 0;
        }

        img {
          max-width: 100%;
          border-radius: 8px;
          margin: 0.5em 0;
        }
      }

      // ===== 深度思考 =====
      .thought-container {
        margin-bottom: 14px;
        background: linear-gradient(135deg, rgba($primary, 0.04), rgba($primary, 0.08));
        border-radius: 12px;
        padding: 0 14px;
        border: 1px solid rgba($primary, 0.08);
        
        .thought-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 10px 0;
          cursor: pointer;
          color: $primary;
          font-size: 0.85rem;
          font-weight: 600;
          user-select: none;
          
          .thought-title {
            display: flex;
            align-items: center;
            gap: 8px;
          }

          .thought-icon.spinning {
            animation: spin 1.5s linear infinite;
          }
          
          .expand-icon {
            transition: transform 0.3s;
            font-size: 0.8rem;
            &.expanded { transform: rotate(90deg); }
          }
        }
        
        .thought-content-box {
          border-top: 1px dashed rgba($primary, 0.15);
          padding: 12px 0;
          
          .thought-inner {
            font-size: 0.85rem;
            color: #64748b;
            line-height: 1.7;
          }
        }
      }
    }

    // ===== 消息操作 =====
    .message-actions {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-top: 6px;
      opacity: 1;
      transition: opacity 0.2s;

      .action-btn {
        width: 28px;
        height: 28px;
        display: flex;
        align-items: center;
        justify-content: center;
        border: none;
        background: rgba(0,0,0,0.04);
        border-radius: 6px;
        color: #94a3b8;
        cursor: pointer;
        transition: all 0.15s;
        &:hover {
          background: rgba($primary, 0.1);
          color: $primary;
        }

        &:disabled {
          cursor: not-allowed;
          opacity: 0.45;
        }
      }

      .message-time {
        font-size: 0.7rem;
        color: var(--text-secondary);
        opacity: 0.5;
      }
    }

    &:hover .message-actions { opacity: 1; }

    .message-meta {
      font-size: 0.7rem;
      color: var(--text-secondary);
      margin-top: 6px;
      opacity: 0.5;
    }
  }
}

// ========== 回到底部按钮 ==========
.scroll-bottom-btn {
  position: sticky;
  bottom: 10px;
  align-self: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #fff;
  border: 1px solid rgba(0,0,0,0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  color: $primary;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
  transition: all 0.2s;
  z-index: 10;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(0,0,0,0.15);
    background: $primary;
    color: #fff;
  }
}

// ========== 输入区 ==========
.input-area-wrapper {
  padding: 0 10% 28px;
  background: transparent;
  width: 100%;
}

.input-card {
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.07);
  border: 1px solid rgba(0,0,0,0.05);
  padding: 12px 16px;
  transition: all 0.3s ease;

  &:focus-within {
    transform: translateY(-2px);
    box-shadow: 0 12px 40px rgba(0,0,0,0.1);
    border-color: rgba($primary, 0.25);
  }

  .main-input {
    :deep(.el-textarea__inner) {
      box-shadow: none;
      background: transparent;
      padding: 4px 8px;
      font-size: 1rem;
      color: var(--text-primary);
      line-height: 1.6;
      &::placeholder { color: #94a3b8; }
    }
  }

  .action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 8px;
    padding: 0 8px;

    .toggles {
      display: flex;
      gap: 8px;
      
      .toggle-chip {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 5px 12px;
        border-radius: 100px;
        font-size: 0.75rem;
        color: #64748b;
        background: #f1f5f9;
        cursor: pointer;
        transition: all 0.2s;
        user-select: none;
        
        &:hover { background: #e2e8f0; }
        
        &.active {
          background: $primary-light;
          color: $primary;
          font-weight: 600;
        }
      }
    }

    .right-actions {
      display: flex;
      gap: 8px;
      .send-btn {
        width: 36px;
        height: 36px;
        background: linear-gradient(135deg, $primary, darken($primary, 8%));
        border: none;
        box-shadow: 0 4px 14px rgba($primary, 0.3);
        transition: all 0.2s;
        &:hover { transform: scale(1.08); box-shadow: 0 6px 18px rgba($primary, 0.4); }
        &:disabled { opacity: 0.5; transform: none; box-shadow: none; }
      }
    }
  }
}

// ========== 气泡内等待动画 ==========
.streaming-loader {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 0;

  span {
    width: 7px;
    height: 7px;
    border-radius: 50%;
    background: $primary;
    opacity: 0.35;
    animation: loader-wave 1.2s ease-in-out infinite;

    &:nth-child(2) { animation-delay: 0.15s; }
    &:nth-child(3) { animation-delay: 0.3s; }
  }
}

// ========== 动画 ==========
@keyframes loader-wave {
  0%, 100% { opacity: 0.3; transform: scale(0.85); }
  50% { opacity: 1; transform: scale(1.15); }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.msg-list-enter-active { animation: fadeInUp 0.35s ease; }

.expand-enter-active, .expand-leave-active {
  transition: all 0.3s ease;
  max-height: 500px;
  overflow: hidden;
}
.expand-enter-from, .expand-leave-to {
  max-height: 0;
  opacity: 0;
}

.fade-up-enter-active, .fade-up-leave-active {
  transition: all 0.3s ease;
}
.fade-up-enter-from, .fade-up-leave-to {
  opacity: 0;
  transform: translateY(10px);
}


</style>
