<template>
  <div class="auth-page">
    <a-card title="前台登录" :bordered="false" class="auth-card">
      <a-form :model="form" layout="vertical" @submit.prevent="handleLogin">
        <a-form-item label="用户名">
          <a-input v-model="form.username" placeholder="用户名" autocomplete="username" />
        </a-form-item>
        <a-form-item label="密码">
          <a-input-password v-model="form.password" placeholder="密码" autocomplete="current-password" />
        </a-form-item>
        <a-button type="primary" html-type="submit" :loading="loading" long>登录</a-button>
      </a-form>
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

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const error = ref('')
const authStore = useAuthStore()

const form = reactive({
  username: '',
  password: '',
})

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
.auth-page {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

.auth-card {
  width: 360px;
}

.error {
  margin-top: 12px;
}
</style>
