<template>
  <div class="auth-page">
    <a-card title="前台注册" :bordered="false" class="auth-card">
      <a-form :model="form" layout="vertical" @submit.prevent="handleRegister">
        <a-form-item label="用户名">
          <a-input v-model="form.username" placeholder="用户名" autocomplete="username" />
        </a-form-item>
        <a-form-item label="密码">
          <a-input-password v-model="form.password" placeholder="密码" autocomplete="new-password" />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input v-model="form.phone" placeholder="手机号" />
        </a-form-item>
        <a-form-item label="昵称">
          <a-input v-model="form.nickname" placeholder="昵称（可选）" />
        </a-form-item>
        <a-form-item label="邮箱">
          <a-input v-model="form.email" placeholder="邮箱（可选）" />
        </a-form-item>
        <a-button type="primary" html-type="submit" :loading="loading" long>注册</a-button>
      </a-form>
      <a-alert v-if="error" type="error" :title="error" class="error" />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import frontAuthApi from '../../api/front/auth'

const router = useRouter()
const loading = ref(false)
const error = ref('')

const form = reactive({
  username: '',
  password: '',
  phone: '',
  nickname: '',
  email: '',
})

const handleRegister = async () => {
  error.value = ''
  loading.value = true
  try {
    await frontAuthApi.register({
      username: form.username,
      password: form.password,
      phone: form.phone,
      nickname: form.nickname || undefined,
      email: form.email || undefined,
    })
    await router.replace('/login')
  } catch (err: any) {
    error.value = err?.message || '注册失败'
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
