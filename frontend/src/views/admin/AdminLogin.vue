<template>
  <div class="admin-login">
    <div class="login-background">
      <div class="login-header">
        <div class="login-logo">书城·管理后台</div>
      </div>
      <div class="login-shell">
        <div class="login-illustration">
          <div class="illustration-content">
            <div class="illustration-title">书店管理场景</div>
            <div class="illustration-sub">深靛蓝专业后台 · 实时掌控</div>
            <div class="illustration-cards">
              <div class="illu-card">
                <div class="illu-line" />
                <div class="illu-line short" />
              </div>
              <div class="illu-card">
                <div class="illu-line" />
                <div class="illu-line short" />
              </div>
              <div class="illu-card">
                <div class="illu-line" />
                <div class="illu-line short" />
              </div>
            </div>
          </div>
        </div>
        <div class="login-form-panel">
          <a-card class="login-card" :bordered="false">
            <div class="card-title">管理员登录</div>
            <div class="card-sub">请输入后台账号与密码</div>
            <a-form :model="form" layout="vertical" @submit="handleLogin">
              <a-form-item label="用户名">
                <a-input v-model="form.username" placeholder="请输入用户名" allow-clear />
              </a-form-item>
              <a-form-item label="密码">
                <a-input-password v-model="form.password" placeholder="请输入密码" allow-clear />
              </a-form-item>
              <div class="login-options">
                <a-checkbox v-model="remember">记住我</a-checkbox>
                <a-link>忘记密码?</a-link>
              </div>
              <a-button type="primary" long html-type="submit" :loading="loading">登录</a-button>
            </a-form>
            <a-alert v-if="error" type="error" :title="error" show-icon class="login-error" />
          </a-card>
        </div>
      </div>
      <div class="login-footer">© 2026 书城管理系统</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import adminAuthApi from '../../api/admin/auth'
import { UserRole } from '../../types/enums'
import { useAuthStore } from '../../store/auth'
import { setRole, setToken, setUserId, setUsername } from '../../utils/auth'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const error = ref('')
const remember = ref(true)
const authStore = useAuthStore()

const form = reactive({
  username: '',
  password: '',
})

const handleLogin = async () => {
  error.value = ''
  loading.value = true
  try {
    const data = (await adminAuthApi.login({
      username: form.username,
      password: form.password,
    })) as any
    if (data?.token) {
      setToken(data.token)
      setRole(data.role ?? UserRole.Admin)
      if (typeof data.userId === 'number') {
        setUserId(data.userId)
      }
      if (typeof data.username === 'string') {
        setUsername(data.username)
      }
      authStore.setAuth({
        token: data.token,
        role: data.role ?? UserRole.Admin,
        userId: data.userId,
        username: data.username,
      })
    }

    if (data?.role !== UserRole.Admin) {
      error.value = '当前账号不是管理员'
      return
    }

    await authStore.fetchUserInfo().catch(() => null)

    const redirect = (route.query.redirect as string) || '/admin/dashboard'
    await router.replace(redirect)
  } catch (err: any) {
    error.value = err?.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login {
  min-height: 100vh;
}

.login-background {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f172a, #1e293b);
  color: #e2e8f0;
  display: flex;
  flex-direction: column;
}

.login-header {
  padding: 20px 32px 0;
}

.login-logo {
  font-size: 18px;
  font-weight: 600;
  color: #1e3a5f;
  background: rgba(255, 255, 255, 0.86);
  display: inline-flex;
  padding: 10px 18px;
  border-radius: 999px;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.25);
}

.login-shell {
  flex: 1;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 32px;
  padding: 40px 64px 24px;
}

.login-illustration {
  background: rgba(30, 58, 95, 0.25);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.illustration-content {
  max-width: 360px;
  text-align: left;
  padding: 32px;
}

.illustration-title {
  font-size: 24px;
  font-weight: 600;
  color: #f8fafc;
}

.illustration-sub {
  margin-top: 8px;
  font-size: 14px;
  color: rgba(226, 232, 240, 0.8);
}

.illustration-cards {
  margin-top: 24px;
  display: grid;
  gap: 16px;
}

.illu-card {
  background: rgba(15, 23, 42, 0.6);
  border-radius: 16px;
  padding: 16px;
  border: 1px solid rgba(148, 163, 184, 0.2);
}

.illu-line {
  height: 10px;
  background: rgba(226, 232, 240, 0.4);
  border-radius: 999px;
}

.illu-line.short {
  width: 60%;
  margin-top: 10px;
}

.login-form-panel {
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 100%;
  max-width: 380px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 16px 36px rgba(15, 23, 42, 0.2);
}

.card-title {
  font-size: 20px;
  font-weight: 600;
  color: #0f172a;
}

.card-sub {
  margin: 4px 0 20px;
  color: #64748b;
  font-size: 13px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  color: #475569;
}

.login-error {
  margin-top: 16px;
}

.login-footer {
  text-align: center;
  padding: 20px 0 32px;
  color: rgba(226, 232, 240, 0.7);
  font-size: 12px;
}

@media (max-width: 992px) {
  .login-shell {
    grid-template-columns: 1fr;
    padding: 32px 24px 16px;
  }

  .login-illustration {
    min-height: 240px;
  }
}

@media (max-width: 600px) {
  .login-header {
    padding: 16px 20px 0;
  }

  .login-shell {
    padding: 24px 16px 8px;
  }
}
</style>
