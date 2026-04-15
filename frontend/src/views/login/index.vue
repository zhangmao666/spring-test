<template>
  <div class="login-page">
    <section class="login-hero">
      <div class="login-hero__panel">
        <span class="login-hero__eyebrow">AI-WORLD</span>
        <h1 class="login-hero__title">进入 AI-world，连接一个基于 AI 的世界。</h1>
        <p class="login-hero__desc">
          在这里统一管理模型、对话、内容与工作流，让 AI 成为整个系统持续运转的核心引擎。
        </p>

        <div class="hero-metrics">
          <article class="hero-metric">
            <span class="hero-metric__label">后台能力</span>
            <strong class="hero-metric__value">8+</strong>
            <span class="hero-metric__hint">覆盖用户、字典、日志、AI 与资讯模块</span>
          </article>
          <article class="hero-metric">
            <span class="hero-metric__label">核心原则</span>
            <strong class="hero-metric__value">清晰优先</strong>
            <span class="hero-metric__hint">更少噪音，更快定位任务与风险点</span>
          </article>
        </div>

        <div class="hero-points">
          <div class="hero-point">
            <el-icon><CircleCheckFilled /></el-icon>
            <span>稳定的账号与权限管理</span>
          </div>
          <div class="hero-point">
            <el-icon><CircleCheckFilled /></el-icon>
            <span>高频配置与日志审计集中处理</span>
          </div>
          <div class="hero-point">
            <el-icon><CircleCheckFilled /></el-icon>
            <span>AI 能力融入后台工作流，而不是独立割裂页面</span>
          </div>
        </div>
      </div>
    </section>

    <section class="login-main">
      <div class="login-card">
        <div class="login-card__header">
          <span class="login-card__eyebrow">账号登录</span>
          <h2>欢迎回来</h2>
          <p>使用你的账户进入 AI-world。</p>
        </div>

        <el-form
          ref="formRef"
          :model="loginForm"
          :rules="rules"
          class="login-form"
          size="large"
        >
          <el-form-item prop="username" label="用户名">
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

          <el-form-item prop="password" label="密码">
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

          <div class="login-form__options">
            <el-checkbox v-model="rememberMe">记住登录状态</el-checkbox>
            <button class="text-action" type="button" @click="handleForgotPassword">忘记密码</button>
          </div>

          <el-button
            type="primary"
            :loading="loading"
            class="login-submit"
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
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled, InfoFilled, Lock, User } from '@element-plus/icons-vue'
import { login, register } from '@/api/auth'

const router = useRouter()
const formRef = ref(null)
const registerFormRef = ref(null)
const loading = ref(false)
const registerLoading = ref(false)
const rememberMe = ref(false)
const showRegister = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

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
  background:
    radial-gradient(circle at top left, rgba(47, 91, 234, 0.09), transparent 28%),
    radial-gradient(circle at bottom right, rgba(15, 159, 110, 0.08), transparent 22%),
    var(--page-bg);
}

.login-hero,
.login-main {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
}

.login-hero {
  background:
    linear-gradient(150deg, rgba(15, 23, 42, 0.96), rgba(30, 64, 175, 0.92)),
    radial-gradient(circle at top left, rgba(255, 255, 255, 0.12), transparent 24%);
  color: #fff;
}

.login-hero__panel {
  width: min(560px, 100%);
}

.login-hero__eyebrow {
  display: inline-block;
  color: rgba(255, 255, 255, 0.74);
  font-size: 0.76rem;
  font-weight: 800;
  letter-spacing: 0.16em;
}

.login-hero__title {
  margin: 16px 0 0;
  font-size: clamp(2.3rem, 4vw, 3.4rem);
  line-height: 1.08;
  letter-spacing: -0.04em;
}

.login-hero__desc {
  margin: 18px 0 0;
  max-width: 520px;
  color: rgba(255, 255, 255, 0.82);
  font-size: 1rem;
  line-height: 1.8;
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 28px;
}

.hero-metric {
  padding: 18px 20px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(12px);
}

.hero-metric__label {
  color: rgba(255, 255, 255, 0.68);
  font-size: 0.8rem;
}

.hero-metric__value {
  display: block;
  margin-top: 10px;
  font-size: 1.6rem;
  font-weight: 800;
}

.hero-metric__hint {
  display: block;
  margin-top: 8px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 0.9rem;
  line-height: 1.6;
}

.hero-points {
  margin-top: 28px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.hero-point {
  display: flex;
  align-items: center;
  gap: 12px;
  color: rgba(255, 255, 255, 0.86);
}

.hero-point .el-icon {
  color: #bfdbfe;
  font-size: 1.1rem;
}

.login-card {
  width: min(460px, 100%);
  padding: 32px;
  border: 1px solid var(--border-subtle);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: var(--shadow-strong);
}

.login-card__header {
  margin-bottom: 26px;
}

.login-card__eyebrow {
  color: var(--text-disabled);
  font-size: 0.76rem;
  font-weight: 800;
  letter-spacing: 0.12em;
}

.login-card__header h2 {
  margin: 10px 0 0;
  color: var(--text-primary);
  font-size: 2rem;
  line-height: 1.1;
}

.login-card__header p {
  margin: 10px 0 0;
  color: var(--text-muted);
  line-height: 1.7;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.input-icon {
  color: var(--text-disabled);
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
  width: 100%;
  height: 46px;
  font-size: 1rem;
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
  .login-page {
    grid-template-columns: 1fr;
  }

  .login-hero {
    padding-bottom: 20px;
  }
}

@media (max-width: 768px) {
  .login-hero,
  .login-main {
    padding: 24px;
  }

  .hero-metrics {
    grid-template-columns: 1fr;
  }

  .login-card {
    padding: 24px;
  }

  .login-form__options,
  .register-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
