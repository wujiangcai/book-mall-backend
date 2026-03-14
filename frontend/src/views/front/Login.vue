<template>
  <div class="login-layout">
    <div class="login-visual card-glass">
      <div class="visual-title">书城 · 现代文艺书店</div>
      <div class="visual-sub">与书相遇，和自己相逢</div>
      <img :src="heroImage" alt="books" class="visual-image" />
    </div>
    <a-card title="前台登录" :bordered="false" class="auth-card card-glass">
      <a-form :model="form" layout="vertical" @submit="handleLogin">
        <a-form-item label="用户名">
          <a-input v-model="form.username" placeholder="用户名" autocomplete="username" />
        </a-form-item>
        <a-form-item label="密码">
          <a-input-password v-model="form.password" placeholder="密码" autocomplete="current-password" />
        </a-form-item>
        <a-button type="primary" html-type="submit" :loading="loading" long>登录</a-button>
      </a-form>
      <a-space direction="vertical" fill class="social-block">
        <a-divider>社交登录</a-divider>
        <a-space>
          <a-button>微信</a-button>
          <a-button>QQ</a-button>
          <a-button>微博</a-button>
        </a-space>
        <a-link @click="goRegister">没有账号？去注册</a-link>
      </a-space>
      <a-alert v-if="error" type="error" :title="error" class="error" />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import frontAuthApi from '../../api/front/auth'
import { useAuthStore } from '../../store/auth'
import { setRole, setToken, setUserId, setUsername } from '../../utils/auth'
import heroImage from '../../assets/hero.png'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const error = ref('')
const authStore = useAuthStore()

const form = reactive({
  username: '',
  password: '',
})

const goRegister = () => {
  router.push('/register')
}

const handleLogin = async () => {
  error.value = ''
  loading.value = true
  try {
    const data = (await frontAuthApi.login({
      username: form.username,
      password: form.password,
    })) as any
    if (data?.token) {
      setToken(data.token)
      setRole(data.role ?? 0)
      if (typeof data.userId === 'number') {
        setUserId(data.userId)
      }
      if (typeof data.username === 'string') {
        setUsername(data.username)
      }
      authStore.setAuth({
        token: data.token,
        role: data.role ?? 0,
        userId: data.userId,
        username: data.username,
      })
    }

    await authStore.fetchUserInfo().catch(() => null)

    const redirect = (route.query.redirect as string) || '/'
    await router.replace(redirect)
  } catch (err: any) {
    error.value = err?.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  align-items: center;
  margin-top: 32px;
}

.login-visual {
  padding: 32px;
  border-radius: 20px;
}

.visual-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--brand-primary);
}

.visual-sub {
  margin-top: 8px;
  color: var(--brand-muted);
}

.visual-image {
  width: 100%;
  margin-top: 20px;
  border-radius: 16px;
  object-fit: cover;
}

.auth-card {
  width: 100%;
}

.social-block {
  margin-top: 16px;
}

.error {
  margin-top: 12px;
}

@media (max-width: 992px) {
  .login-layout {
    grid-template-columns: 1fr;
  }
}
</style>
