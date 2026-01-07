<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <!-- 左侧品牌区域 -->
    <div class="brand-section">
      <div class="brand-content">
        <div class="brand-logo">
          <el-icon :size="48"><DataAnalysis /></el-icon>
        </div>
        <h1 class="brand-title">Admin Pro</h1>
        <p class="brand-subtitle">现代化企业级后台管理系统</p>
        <div class="brand-features">
          <div class="feature-item">
            <el-icon><CircleCheck /></el-icon>
            <span>安全可靠的权限管理</span>
          </div>
          <div class="feature-item">
            <el-icon><CircleCheck /></el-icon>
            <span>灵活高效的数据管理</span>
          </div>
          <div class="feature-item">
            <el-icon><CircleCheck /></el-icon>
            <span>现代化的用户体验</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录区域 -->
    <div class="login-section">
      <div class="login-box">
        <div class="login-header">
          <h2>欢迎回来</h2>
          <p>请登录您的账户继续使用</p>
        </div>
        
        <el-form 
          ref="formRef" 
          :model="loginForm" 
          :rules="rules" 
          class="login-form"
          size="large"
        >
          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="请输入用户名"
            >
              <template #prefix>
                <el-icon class="input-icon"><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="请输入密码"
              show-password
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon class="input-icon"><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item class="form-options">
            <div class="options-row">
              <el-checkbox v-model="rememberMe">记住登录状态</el-checkbox>
              <el-button type="primary" link class="forgot-link">忘记密码？</el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              :loading="loading"
              class="login-btn"
              @click="handleLogin"
            >
              <span v-if="!loading">登 录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <span class="footer-text">还没有账号？</span>
          <el-button type="primary" link @click="showRegister = true">立即注册</el-button>
        </div>

        <el-divider>
          <span class="divider-text">其他登录方式</span>
        </el-divider>

        <div class="social-login">
          <div class="social-btn">
            <el-icon :size="20"><ChatDotRound /></el-icon>
          </div>
          <div class="social-btn">
            <el-icon :size="20"><Message /></el-icon>
          </div>
          <div class="social-btn">
            <el-icon :size="20"><Link /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 注册对话框 -->
    <el-dialog 
      v-model="showRegister" 
      title="创建新账户" 
      width="420px"
      :close-on-click-modal="false"
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
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
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

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    const res = await login(loginForm)
    // 后端返回的是 accessToken 字段
    const token = res.data?.accessToken || res.data?.token
    if (token) {
      localStorage.setItem('token', token)
      localStorage.setItem('username', res.data?.username || loginForm.username)
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error('登录失败：未获取到token')
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('登录失败，请检查用户名和密码')
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
    ElMessage.success('注册成功，请登录')
    showRegister.value = false
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    registerLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
$primary: #6366f1;
$primary-light: #818cf8;
$primary-dark: #4f46e5;

.login-container {
  height: 100vh;
  display: flex;
  position: relative;
  overflow: hidden;
  background: #f8fafc;
}

.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;

  .circle {
    position: absolute;
    border-radius: 50%;
    background: linear-gradient(135deg, rgba($primary, 0.1), rgba($primary-light, 0.05));
  }

  .circle-1 {
    width: 600px;
    height: 600px;
    top: -200px;
    right: -100px;
  }

  .circle-2 {
    width: 400px;
    height: 400px;
    bottom: -100px;
    left: -100px;
  }

  .circle-3 {
    width: 200px;
    height: 200px;
    top: 50%;
    left: 40%;
  }
}

.brand-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, $primary 0%, $primary-dark 100%);
  padding: 60px;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
    opacity: 0.5;
  }

  .brand-content {
    position: relative;
    z-index: 1;
    color: #fff;
    max-width: 400px;

    .brand-logo {
      width: 80px;
      height: 80px;
      background: rgba(255, 255, 255, 0.15);
      backdrop-filter: blur(10px);
      border-radius: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 32px;
    }

    .brand-title {
      font-size: 42px;
      font-weight: 700;
      margin-bottom: 12px;
      letter-spacing: -1px;
    }

    .brand-subtitle {
      font-size: 18px;
      opacity: 0.9;
      margin-bottom: 48px;
      line-height: 1.6;
    }

    .brand-features {
      display: flex;
      flex-direction: column;
      gap: 16px;

      .feature-item {
        display: flex;
        align-items: center;
        gap: 12px;
        font-size: 15px;
        opacity: 0.9;

        .el-icon {
          font-size: 20px;
          color: #a5f3fc;
        }
      }
    }
  }
}

.login-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  z-index: 1;
}

.login-box {
  width: 100%;
  max-width: 400px;

  .login-header {
    margin-bottom: 40px;

    h2 {
      font-size: 28px;
      font-weight: 700;
      color: #1e293b;
      margin-bottom: 8px;
    }

    p {
      font-size: 15px;
      color: #64748b;
    }
  }

  .login-form {
    .el-form-item {
      margin-bottom: 20px;
    }

    .input-icon {
      color: #94a3b8;
    }

    :deep(.el-input__wrapper) {
      padding: 4px 16px;
      height: 48px;
      border-radius: 10px;
      box-shadow: 0 0 0 1px #e2e8f0 inset;
      transition: all 0.2s ease;

      &:hover {
        box-shadow: 0 0 0 1px $primary-light inset;
      }

      &.is-focus {
        box-shadow: 0 0 0 2px $primary inset;
      }
    }

    .form-options {
      margin-bottom: 24px;

      .options-row {
        width: 100%;
        display: flex;
        align-items: center;
        justify-content: space-between;
      }

      .forgot-link {
        font-size: 14px;
      }
    }

    .login-btn {
      width: 100%;
      height: 48px;
      font-size: 16px;
      font-weight: 600;
      border-radius: 10px;
      background: linear-gradient(135deg, $primary 0%, $primary-dark 100%);
      border: none;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba($primary, 0.35);
      }

      &:active {
        transform: translateY(0);
      }
    }
  }

  .login-footer {
    text-align: center;
    margin: 24px 0;

    .footer-text {
      color: #64748b;
      font-size: 14px;
    }
  }

  :deep(.el-divider) {
    margin: 32px 0;

    .el-divider__text {
      background: #f8fafc;
      padding: 0 16px;
    }

    .divider-text {
      font-size: 13px;
      color: #94a3b8;
    }
  }

  .social-login {
    display: flex;
    justify-content: center;
    gap: 16px;

    .social-btn {
      width: 48px;
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 12px;
      background: #fff;
      border: 1px solid #e2e8f0;
      color: #64748b;
      cursor: pointer;
      transition: all 0.2s ease;

      &:hover {
        border-color: $primary;
        color: $primary;
        transform: translateY(-2px);
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 900px) {
  .brand-section {
    display: none;
  }

  .login-section {
    padding: 24px;
  }
}
</style>
