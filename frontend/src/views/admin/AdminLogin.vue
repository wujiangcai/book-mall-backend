<template>
  <div class="auth-page">
    <h1>后台登录</h1>
    <form @submit.prevent="handleLogin">
      <label>
        用户名
        <input v-model="form.username" placeholder="用户名" autocomplete="username" />
      </label>
      <label>
        密码
        <input v-model="form.password" type="password" placeholder="密码" autocomplete="current-password" />
      </label>
      <button type="submit" :disabled="loading">登录</button>
    </form>
    <p v-if="error" class="error">{{ error }}</p>
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
.auth-page {
  max-width: 360px;
  margin: 40px auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
label {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.error {
  color: #d93026;
}
</style>
