<template>
  <div class="admin-dashboard">
    <h1>后台首页</h1>
    <p v-if="authStore.userInfo">当前管理员：{{ authStore.userInfo.username }}</p>
    <button @click="handleLogout">退出登录</button>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../store/auth'

const router = useRouter()
const authStore = useAuthStore()

const handleLogout = async () => {
  authStore.clear()
  await router.replace('/admin/login')
}
</script>

<style scoped>
.admin-dashboard {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
</style>
