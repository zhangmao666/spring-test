<template>
  <div class="login-page">
    <section class="login-hero">
      <div class="login-hero__orb login-hero__orb--purple"></div>
      <div class="login-hero__orb login-hero__orb--blue"></div>
      <div class="login-hero__orb login-hero__orb--teal"></div>
      <div class="login-hero__stars">
        <span v-for="s in stars" :key="s.id" :style="s.style"></span>
      </div>
      <div class="login-hero__grid"></div>

      <div
        class="login-hero__panel"
        v-motion
        :initial="{ opacity: 0, y: 20 }"
        :enter="{ opacity: 1, y: 0, transition: { duration: 500 } }"
      >
        <div class="login-hero__brand">
          <div class="login-hero__mark">
            <el-icon :size="24"><Cpu /></el-icon>
          </div>
          <div>
            <span class="login-hero__eyebrow">AI-WORLD</span>
            <p class="login-hero__brand-line">统一 AI 工作台</p>
          </div>
        </div>

        <h1 class="login-hero__title">
          让 AI 真正
          <span class="login-hero__title-accent">融入</span>
          你的日常工作。
        </h1>

        <div class="feature-carousel">
          <transition name="feature-fade" mode="out-in">
            <article :key="activeFeature.id" class="feature-card">
              <div class="feature-card__icon" :class="`tone-${activeFeature.tone}`">
                <el-icon :size="26"><component :is="activeFeature.icon" /></el-icon>
              </div>
              <h3>{{ activeFeature.title }}</h3>
              <p>{{ activeFeature.desc }}</p>
            </article>
          </transition>
          <div class="feature-carousel__dots">
            <button
              v-for="(f, i) in features"
              :key="f.id"
              type="button"
              class="feature-carousel__dot"
              :class="{ 'is-active': i === activeIndex }"
              @click="activeIndex = i"
            ></button>
          </div>
        </div>

        <div class="hero-points">
          <div
            v-for="(p, i) in points"
            :key="p.text"
            class="hero-point"
            v-motion
            :initial="{ opacity: 0, x: -12 }"
            :enter="{ opacity: 1, x: 0, transition: { delay: 400 + i * 100, duration: 320 } }"
          >
            <el-icon><CircleCheckFilled /></el-icon>
            <span>{{ p.text }}</span>
          </div>
        </div>
      </div>
    </section>

    <section class="login-main">
      <div
        class="login-card"
        v-motion
        :initial="{ opacity: 0, y: 16 }"
        :enter="{ opacity: 1, y: 0, transition: { duration: 460, delay: 120 } }"
      >
        <div class="login-card__header">
          <span class="login-card__eyebrow">账号登录</span>
          <h2>欢迎回来</h2>
          <p>登录后继续处理任务、查看状态并使用 AI 能力。</p>
        </div>

        <el-form
          ref="formRef"
          :model="loginForm"
          :rules="rules"
          class="login-form"
          size="large"
        >
          <el-form-item
            prop="username"
            label="用户名"
            v-motion
            :initial="{ opacity: 0, y: 8 }"
            :enter="{ opacity: 1, y: 0, transition: { delay: 260, duration: 300 } }"
          >
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              autocomplete="username"
            >
              <template #prefix>
                <el-icon class="input-icon"><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item
            prop="password"
            label="密码"
            v-motion
            :initial="{ opacity: 0, y: 8 }"
            :enter="{ opacity: 1, y: 0, transition: { delay: 340, duration: 300 } }"
          >
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              autocomplete="current-password"
              show-password
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon class="input-icon"><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <div
            class="login-form__options"
            v-motion
            :initial="{ opacity: 0 }"
            :enter="{ opacity: 1, transition: { delay: 420, duration: 300 } }"
          >
            <el-checkbox v-model="rememberMe">记住登录状态</el-checkbox>
            <button class="text-action" type="button" @click="handleForgotPassword">忘记密码</button>
          </div>

          <el-button
            type="primary"
            :loading="loading"
            class="login-submit"
            v-motion
            :initial="{ opacity: 0, y: 8 }"
            :enter="{ opacity: 1, y: 0, transition: { delay: 500, duration: 320 } }"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录并进入工作台' }}
          </el-button>
        </el-form>

        <div class="login-card__support">
          <div class="support-note">
            <el-icon><InfoFilled /></el-icon>
            <span>当前仅支持账号密码登录。若需要开通或重置账号，请联系系统管理员。</span>
          </div>

          <div class="register-row">
            <span>还没有账号？</span>
            <button class="text-action" type="button" @click="showRegister = true">创建新账户</button>
          </div>
        </div>
      </div>
    </section>

    <el-dialog
      v-model="showRegister"
      title="创建新账户"
      width="440px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-position="top"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱地址" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱地址" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showRegister = false">取消</el-button>
          <el-button type="primary" :loading="registerLoading" @click="handleRegister">
            创建账户
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Bell,
  ChatDotRound,
  CircleCheckFilled,
  Cpu,
  DataAnalysis,
  InfoFilled,
  Lock,
  User
} from '@element-plus/icons-vue'
import { login, register } from '@/api/auth'

const router = useRouter()
const formRef = ref(null)
const registerFormRef = ref(null)
const loading = ref(false)
const registerLoading = ref(false)
const rememberMe = ref(false)
const showRegister = ref(false)

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', email: '', password: '', confirmPassword: '' })

const features = [
  { id: 1, tone: 'ai', icon: ChatDotRound, title: 'AI 聊天', desc: '接入模型对话与提示词管理，让 AI 成为团队的第二双手。' },
  { id: 2, tone: 'news', icon: Bell, title: '热点情报', desc: '多平台热点聚合，实时抓取，让你不错过任何风向。' },
  { id: 3, tone: 'tools', icon: DataAnalysis, title: '效率工具', desc: '密码、字数、番茄钟等本地工具，专注单点、极简可靠。' }
]

const points = [
  { text: '稳定的账号与权限管理' },
  { text: '高频配置与日志审计集中处理' },
  { text: 'AI 能力融入后台工作流，而不是独立割裂页面' }
]

const activeIndex = ref(0)
const activeFeature = computed(() => features[activeIndex.value])
let timer = null

onMounted(() => {
  timer = setInterval(() => {
    activeIndex.value = (activeIndex.value + 1) % features.length
  }, 4200)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})

const stars = Array.from({ length: 40 }, (_, i) => ({
  id: i,
  style: {
    top: `${Math.random() * 100}%`,
    left: `${Math.random() * 100}%`,
    width: `${1 + Math.random() * 2}px`,
    height: `${1 + Math.random() * 2}px`,
    animationDelay: `${Math.random() * 3}s`,
    animationDuration: `${2 + Math.random() * 3}s`
  }
}))

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const validateConfirmPassword = (_, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
    return
  }
  callback()
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度需在 3 到 20 个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleForgotPassword = () => {
  ElMessage.info('请联系系统管理员重置密码。')
}

const handleLogin = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    const res = await login(loginForm)
    const token = res.data?.accessToken || res.data?.token
    if (!token) {
      ElMessage.error('登录失败：未获取到访问令牌')
      return
    }
    localStorage.setItem('token', token)
    localStorage.setItem('username', res.data?.username || loginForm.username)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    ElMessage.error('登录失败，请检查用户名和密码。')
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
    registerLoading.value = true
    await register({
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password
    })
    ElMessage.success('注册成功，请使用新账户登录。')
    showRegister.value = false
  } catch (error) {
    ElMessage.error('注册失败，请检查填写信息。')
  } finally {
    registerLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) minmax(420px, 0.92fr);
  background: var(--page-bg);
}

.login-hero,
.login-main {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  position: relative;
  overflow: hidden;
}

.login-hero {
  background:
    radial-gradient(circle at 20% 20%, rgba(56, 189, 248, 0.32), transparent 40%),
    radial-gradient(circle at 80% 80%, rgba(47, 91, 234, 0.4), transparent 40%),
    linear-gradient(140deg, #08091f 0%, #101a40 45%, #14245c);
  color: #fff;
}

.login-hero__orb {
  position: absolute;
  border-radius: 999px;
  filter: blur(60px);
  pointer-events: none;
  opacity: 0.65;
  animation: orbFloat 16s ease-in-out infinite;
}

.login-hero__orb--purple {
  top: -100px;
  left: 12%;
  width: 320px;
  height: 320px;
  background: radial-gradient(circle, #38bdf8, transparent 70%);
}

.login-hero__orb--blue {
  bottom: -120px;
  right: 8%;
  width: 380px;
  height: 380px;
  background: radial-gradient(circle, #2f5bea, transparent 70%);
  animation-duration: 22s;
  animation-direction: reverse;
}

.login-hero__orb--teal {
  top: 40%;
  right: 40%;
  width: 240px;
  height: 240px;
  background: radial-gradient(circle, #14b8a6, transparent 72%);
  opacity: 0.35;
  animation-duration: 18s;
}

@keyframes orbFloat {
  0%, 100% { transform: translate(0, 0); }
  33% { transform: translate(-24px, 20px); }
  66% { transform: translate(20px, -14px); }
}

.login-hero__grid {
  position: absolute;
  inset: 0;
  pointer-events: none;
  opacity: 0.12;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.1) 1px, transparent 1px);
  background-size: 42px 42px;
  mask-image: radial-gradient(ellipse at center, black 30%, transparent 76%);
}

.login-hero__stars {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.login-hero__stars span {
  position: absolute;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 0 4px rgba(255, 255, 255, 0.8);
  animation: starTwinkle ease-in-out infinite;
}

@keyframes starTwinkle {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}

.login-hero__panel {
  position: relative;
  z-index: 1;
  width: min(560px, 100%);
}

.login-hero__brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 32px;
}

.login-hero__mark {
  display: grid;
  place-items: center;
  width: 52px;
  height: 52px;
  border-radius: 16px;
  background: linear-gradient(135deg, #38bdf8, #2f5bea);
  color: #fff;
  box-shadow: 0 20px 40px rgba(47, 91, 234, 0.4);
}

.login-hero__eyebrow {
  display: inline-block;
  color: rgba(255, 255, 255, 0.72);
  font-size: 0.76rem;
  font-weight: 800;
  letter-spacing: 0.24em;
}

.login-hero__brand-line {
  margin: 4px 0 0;
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.86rem;
}

.login-hero__title {
  margin: 0;
  font-size: clamp(2rem, 3.6vw, 3rem);
  line-height: 1.15;
  letter-spacing: -0.02em;
  font-weight: 800;
}

.login-hero__title-accent {
  background: linear-gradient(135deg, #7dd3fc, #38bdf8);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.feature-carousel {
  position: relative;
  margin-top: 32px;
  padding: 22px 24px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(18px);
  min-height: 160px;
}

.feature-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.feature-card__icon {
  display: grid;
  place-items: center;
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: var(--accent-soft);
  color: var(--accent);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.2);
}

.feature-card h3 {
  margin: 6px 0 0;
  font-size: 1.15rem;
  font-weight: 800;
  color: #fff;
}

.feature-card p {
  margin: 0;
  color: rgba(255, 255, 255, 0.72);
  font-size: 0.92rem;
  line-height: 1.7;
}

.feature-carousel__dots {
  position: absolute;
  right: 24px;
  bottom: 20px;
  display: inline-flex;
  gap: 6px;
}

.feature-carousel__dot {
  width: 24px;
  height: 4px;
  border: none;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.24);
  cursor: pointer;
  transition: background 220ms ease;
}

.feature-carousel__dot.is-active {
  background: linear-gradient(90deg, #7dd3fc, #38bdf8);
}

.feature-fade-enter-active,
.feature-fade-leave-active {
  transition: opacity 300ms ease, transform 300ms ease;
}

.feature-fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.feature-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.hero-points {
  margin-top: 26px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hero-point {
  display: flex;
  align-items: center;
  gap: 10px;
  color: rgba(255, 255, 255, 0.82);
  font-size: 0.94rem;
}

.hero-point .el-icon {
  color: #7dd3fc;
  font-size: 1.05rem;
}

.login-main {
  background:
    radial-gradient(circle at top left, rgba(47, 91, 234, 0.06), transparent 30%),
    radial-gradient(circle at bottom right, rgba(14, 165, 233, 0.06), transparent 30%),
    var(--page-bg);
}

.login-card {
  position: relative;
  z-index: 1;
  width: min(460px, 100%);
  padding: 34px;
  border: 1px solid var(--border-subtle);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: var(--shadow-elevated);
  backdrop-filter: blur(20px);
}

.login-card__header {
  margin-bottom: 26px;
}

.login-card__eyebrow {
  color: var(--color-primary);
  font-size: 0.76rem;
  font-weight: 800;
  letter-spacing: 0.12em;
}

.login-card__header h2 {
  margin: 8px 0 0;
  color: var(--text-primary);
  font-size: 1.9rem;
  line-height: 1.15;
  font-weight: 800;
  background: linear-gradient(135deg, var(--color-primary), #0ea5e9);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.login-card__header p {
  margin: 10px 0 0;
  color: var(--text-muted);
  line-height: 1.7;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.input-icon {
  color: var(--text-disabled);
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(47, 91, 234, 0.22) inset !important;
}

.login-form__options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 22px;
}

.text-action {
  padding: 0;
  border: none;
  background: transparent;
  color: var(--color-primary);
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 600;
}

.text-action:hover {
  color: var(--color-primary-strong);
}

.login-submit {
  position: relative;
  overflow: hidden;
  width: 100%;
  height: 48px;
  font-size: 1rem;
  background: linear-gradient(135deg, var(--color-primary), #0ea5e9) !important;
}

.login-submit::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 60%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.35), transparent);
  transition: left 500ms ease;
}

.login-submit:hover::before {
  left: 120%;
}

.login-card__support {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--border-soft);
}

.support-note {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 14px 16px;
  border-radius: 16px;
  background: var(--surface-muted);
  color: var(--text-muted);
  font-size: 0.88rem;
  line-height: 1.7;
}

.support-note .el-icon {
  margin-top: 2px;
  color: var(--color-primary);
}

.register-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 18px;
  color: var(--text-muted);
  font-size: 0.92rem;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 1080px) {
  .login-page { grid-template-columns: 1fr; }
  .login-hero { padding-bottom: 32px; }
}

@media (max-width: 768px) {
  .login-hero,
  .login-main { padding: 24px; }
  .login-card { padding: 24px; }
  .login-form__options,
  .register-row { flex-direction: column; align-items: flex-start; }
}
</style>
