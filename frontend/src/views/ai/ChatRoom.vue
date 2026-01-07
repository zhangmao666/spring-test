<template>
  <div class="chat-wrapper" :class="`theme-${currentTheme}`">
    <!-- 背景装饰点缀 -->
    <div class="chat-bg-glow"></div>

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
          <el-button circle @click="clearHistory">
            <el-icon><Delete /></el-icon>
          </el-button>
          <el-button circle>
            <el-icon><MoreFilled /></el-icon>
          </el-button>
        </div>
      </div>

      <div class="messages-area" ref="messagesContainer">
        <transition-group name="msg-list">
          <div v-if="messages.length === 0" class="empty-state" key="empty">
            <div class="empty-icon glass-container">
              <el-icon :size="40"><ChatDotRound /></el-icon>
            </div>
            <h3>你好! 我是您的 AI 助手</h3>
            <p>我可以帮您进行投资分析、代码编写或日常答疑。</p>
            <div class="suggestion-chips">
              <div v-for="tag in suggestions" :key="tag" class="chip" @click="inputToSent = tag">
                {{ tag }}
              </div>
            </div>
          </div>
          
          <div 
            v-for="(msg, index) in messages" 
            :key="index"
            :class="['message-item', msg.role === 'user' ? 'user-message' : 'ai-message']"
          >
            <div class="avatar-box">
              <el-avatar v-if="msg.role !== 'user'" :size="45" :src="aiAvatar" />
              <div v-else class="user-avatar-placeholder">
                <el-icon><UserFilled /></el-icon>
              </div>
            </div>
            <div class="content-box">
              <div class="bubble glass-container">
                <div class="text" v-if="msg.role === 'user'">{{ msg.content }}</div>
                <div v-else class="ai-content-wrapper">
                  <!-- 深度思考展示区 -->
                  <div v-if="msg.thought" class="thought-container">
                    <div class="thought-header" @click="msg.thoughtExpanded = !msg.thoughtExpanded">
                      <div class="thought-title">
                        <el-icon class="thought-icon"><Cpu /></el-icon>
                        <span>{{ msg.isThinking ? '正在思考...' : `已思考 (用时 ${msg.thinkingTime || 0} 秒)` }}</span>
                      </div>
                      <el-icon :class="['expand-icon', { expanded: msg.thoughtExpanded }]"><ArrowRight /></el-icon>
                    </div>
                    <transition name="expand">
                      <div v-show="msg.thoughtExpanded" class="thought-content-box">
                        <div class="thought-inner markdown-body" v-html="md.render(msg.thought)"></div>
                      </div>
                    </transition>
                  </div>
                  
                  <!-- 回答正文 -->
                  <div class="text markdown-body" v-html="md.render(msg.content)"></div>
                </div>
              </div>
              <div class="message-meta">{{ formatTime(msg.timestamp) }}</div>
            </div>
          </div>
        </transition-group>
        <div v-if="loading" class="typing-indicator ai-message">
          <div class="bubble glass-container">
            <div class="dots"><span></span><span></span><span></span></div>
          </div>
        </div>

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
              <el-button circle class="icon-btn" text>
                <el-icon :size="20"><Link /></el-icon>
              </el-button>
              <el-button 
                type="primary" 
                circle 
                class="send-btn" 
                @click="handleSend" 
                :loading="loading"
                :disabled="!inputToSent.trim()"
              >
                <el-icon><Top /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  ChatDotRound, Delete, Promotion, UserFilled, 
  MoreFilled, Plus, Cpu, Compass, Link, Top, ArrowRight
} from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import aiAvatar from '@/assets/ai_avatar.png'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
const currentTheme = computed(() => themeStore.currentTheme)

const suggestions = ['分析今日热门板块', '写一个快速排序算法', '总结最近的财报热点', '推荐几本金融入门书']

const md = new MarkdownIt({
  html: true,
  linkify: true,
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(str, { language: lang }).value;
      } catch (__) {}
    }
    return '';
  }
})

const messages = ref([])
const inputToSent = ref('')
const loading = ref(false)
const messagesContainer = ref(null)
const conversationId = ref(null)
const useWebSearch = ref(false)
const useDeepThinking = ref(false)

const formatTime = (ts) => {
  const d = new Date(ts)
  return `${d.getHours()}:${d.getMinutes().toString().padStart(2, '0')}`
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTo({
      top: messagesContainer.value.scrollHeight,
      behavior: 'smooth'
    })
  }
}

const clearHistory = () => {
  messages.value = []
  conversationId.value = null
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
  await scrollToBottom()

  const aiIndex = messages.value.length
  messages.value.push({
    role: 'ai',
    thought: '',
    content: '',
    fullText: '',
    isThinking: false,
    thoughtExpanded: true,
    thinkingTime: 0,
    timestamp: Date.now()
  })

  if (!conversationId.value) conversationId.value = crypto.randomUUID()

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
        conversationId: conversationId.value,
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
            return 
          }

          const delta = json.choices?.[0]?.delta
          if (delta) {
            // 1. 优先处理专用的 reasoning_content 字段 (DeepSeek 官方 API)
            if (delta.reasoning_content) {
              targetMsg.thought += delta.reasoning_content
              if (!targetMsg.isThinking) {
                targetMsg.isThinking = true
                targetMsg.thoughtExpanded = true
                if (thoughtTimer) clearInterval(thoughtTimer)
                thoughtTimer = setInterval(() => {
                  targetMsg.thinkingTime++
                }, 1000)
              }
            } 
            
            // 2. 处理正文内容
            if (delta.content) {
              // 如果之前在思考状态，现在收到正文，说明思考结束
              if (targetMsg.isThinking && !targetMsg.fullText.includes('<think>')) {
                targetMsg.isThinking = false
                if (thoughtTimer) clearInterval(thoughtTimer)
              }
              
              targetMsg.fullText += delta.content
              
              // 3. 回退方案：解析内容中的 <think> 标签 (非官方或兼容层)
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
            
            await scrollToBottom()
          }
        } catch (e) {}
      }
    }
    if (thoughtTimer) clearInterval(thoughtTimer)
  } catch (error) {
    ElMessage.error('服务连接失败')
    messages.value[aiIndex].content = '抱歉，系统暂时无法响应您的请求。'
  } finally {
    loading.value = false
  }
}

onMounted(() => scrollToBottom())
</script>

<style lang="scss" scoped>
.chat-wrapper {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  padding: 10px;
}

.chat-bg-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.1) 0%, transparent 70%);
  transform: translate(-50%, -50%);
  z-index: 0;
  pointer-events: none;
}

.chat-container {
  width: 100%;
  max-width: 1200px;
  height: 100%;
  border-radius: $radius-lg;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 1;
}

.chat-header {
  height: 100px;
  padding: 0 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(0,0,0,0.05);
  background: rgba(255, 255, 255, 0.3);

  .header-left {
    display: flex;
    align-items: center;
    gap: 20px;

    .status-indicator {
      position: relative;
      .ai-avatar-main { border: 3px solid #fff; box-shadow: $shadow-md; }
      .status-dot {
        position: absolute;
        bottom: 2px;
        right: 2px;
        width: 14px;
        height: 14px;
        border-radius: 50%;
        border: 2px solid #fff;
        &.online { background: #10B981; }
      }
    }

    .header-info {
      display: flex;
      flex-direction: column;
      .title { font-size: 1.3rem; font-weight: 800; color: var(--text-primary); }
      .subtitle { font-size: 0.85rem; color: var(--text-secondary); opacity: 0.7; }
    }
  }
}

.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 40px;
  display: flex;
  flex-direction: column;
  gap: 30px;
  scroll-behavior: smooth;

  &::-webkit-scrollbar { width: 0; }
}

.empty-state {
  text-align: center;
  margin: auto;
  max-width: 400px;
  
  .empty-icon {
    width: 80px;
    height: 80px;
    margin: 0 auto 30px;
    border-radius: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--primary-color);
  }
  h3 { font-size: 1.6rem; font-weight: 800; color: var(--text-primary); margin-bottom: 15px; }
  p { font-size: 1rem; color: var(--text-secondary); line-height: 1.6; margin-bottom: 30px; }

  .suggestion-chips {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 12px;
    .chip {
      padding: 10px 18px;
      background: rgba(255, 255, 255, 0.6);
      border: 1px solid rgba(0,0,0,0.05);
      border-radius: 100px;
      font-size: 0.9rem;
      color: var(--text-primary);
      cursor: pointer;
      transition: all $transition-fast;
      &:hover { border-color: var(--primary-color); background: #fff; transform: translateY(-2px); }
    }
  }
}

.message-item {
  display: flex;
  gap: 20px;
  max-width: 85%;
  animation: slideIn 0.4s cubic-bezier(0.4, 0, 0.2, 1);

  &.user-message {
    align-self: flex-end;
    flex-direction: row-reverse;
    .bubble {
      background: linear-gradient(135deg, #6366F1 0%, #8B5CF6 100%);
      color: #fff;
      border: none;
      box-shadow: 0 10px 25px rgba(99, 102, 241, 0.3);
    }
    .message-meta { text-align: right; }
  }

  &.ai-message {
    align-self: flex-start;
    .bubble {
      background: rgba(255, 255, 255, 0.8);
      color: var(--text-primary);
    }
  }

  .avatar-box {
    flex-shrink: 0;
    .user-avatar-placeholder {
      width: 45px;
      height: 45px;
      background: #f1f5f9;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.5rem;
      color: #94a3b8;
    }
  }

  .content-box {
    .bubble {
      padding: 18px 24px;
      border-radius: 20px;
      line-height: 1.7;
      font-size: 1rem;
      
      :deep(.markdown-body) {
        font-size: 1rem;
        pre { background: rgba(0,0,0,0.05); border-radius: 10px; margin: 10px 0; }
        code { font-family: 'JetBrains Mono', monospace; }
      }

      .thought-container {
        margin-bottom: 12px;
        border-radius: 12px;
        
        .thought-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 8px 0;
          cursor: pointer;
          color: #6366F1;
          font-size: 0.9rem;
          font-weight: 500;
          opacity: 0.8;
          transition: opacity 0.2s;
          
          &:hover { opacity: 1; }
          
          .thought-title {
            display: flex;
            align-items: center;
            gap: 8px;
            .thought-icon { font-size: 1.1rem; }
          }
          
          .expand-icon {
            transition: transform 0.3s;
            &.expanded { transform: rotate(90deg); }
          }
        }
        
        .thought-content-box {
          border-left: 2px solid #e2e8f0;
          margin-left: 10px;
          padding: 5px 0 10px 15px;
          
          .thought-inner {
            font-size: 0.95rem;
            color: #64748b;
            line-height: 1.6;
            :deep(p) { margin: 0 0 10px 0; }
          }
        }
      }
    }
    .message-meta {
      font-size: 0.75rem;
      color: var(--text-secondary);
      margin-top: 8px;
      opacity: 0.5;
    }
  }
}

.input-area-wrapper {
  padding: 10px 40px 40px;
  background: transparent;
  width: 100%;
  display: flex;
  justify-content: center;
}

.input-card {
  width: 100%;
  max-width: 900px;
  background: #fff;
  border-radius: 24px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  border: 1px solid rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  padding: 15px 20px;
  transition: all 0.3s ease;

  &:focus-within {
    box-shadow: 0 8px 30px rgba(0,0,0,0.12);
    border-color: rgba(99, 102, 241, 0.2);
  }

  .main-input {
    :deep(.el-textarea__inner) {
      box-shadow: none;
      background: transparent;
      padding: 0;
      font-size: 1.05rem;
      color: var(--text-primary);
      margin-bottom: 10px;
      
      &::placeholder { color: #94a3b8; }
    }
  }

  .action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 5px;

    .toggles {
      display: flex;
      gap: 12px;
      
      .toggle-chip {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 6px 14px;
        border-radius: 100px;
        font-size: 0.85rem;
        color: var(--text-secondary);
        border: 1px solid #e2e8f0;
        cursor: pointer;
        transition: all 0.2s;
        
        .el-icon { font-size: 1rem; }

        &:hover { background: #f8fafc; color: var(--text-primary); }
        
        &.active {
          background: rgba(99, 102, 241, 0.1);
          color: var(--primary-color);
          border-color: rgba(99, 102, 241, 0.2);
          font-weight: 500;
        }
      }
    }

    .right-actions {
      display: flex;
      align-items: center;
      gap: 10px;

      .icon-btn {
        color: #94a3b8;
        &:hover { color: var(--text-primary); background: #f1f5f9; }
      }

      .send-btn {
        width: 36px;
        height: 36px;
        font-size: 1.1rem;
        background: var(--primary-color);
        border: none;
        
        &:disabled {
          background: #e2e8f0;
          color: #fff;
        }
      }
    }
  }
}

.typing-indicator {
  .dots {
    display: flex;
    gap: 6px;
    span {
      width: 8px;
      height: 8px;
      background: var(--primary-color);
      border-radius: 50%;
      animation: bounce 1.4s infinite;
      &:nth-child(2) { animation-delay: 0.2s; }
      &:nth-child(3) { animation-delay: 0.4s; }
    }
  }
}

// 展开收起动画
.expand-enter-active, .expand-leave-active {
  transition: all 0.3s ease;
  max-height: 2000px;
  opacity: 1;
  overflow: hidden;
}
.expand-enter-from, .expand-leave-to {
  max-height: 0;
  opacity: 0;
}

@keyframes bounce { 0%, 80%, 100% { transform: scale(0); } 40% { transform: scale(1); } }
@keyframes slideIn { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }

.msg-list-enter-active, .msg-list-leave-active { transition: all 0.5s ease; }
.msg-list-enter-from { opacity: 0; transform: translateY(30px); }
</style>

