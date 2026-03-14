<template>
  <a-card title="用户中心" :bordered="false">
    <a-descriptions :column="1" v-if="authStore.userInfo">
      <a-descriptions-item label="用户名">{{ authStore.userInfo.username }}</a-descriptions-item>
      <a-descriptions-item label="昵称">{{ authStore.userInfo.nickname || '-' }}</a-descriptions-item>
      <a-descriptions-item label="手机号">{{ authStore.userInfo.phone || '-' }}</a-descriptions-item>
      <a-descriptions-item label="邮箱">{{ authStore.userInfo.email || '-' }}</a-descriptions-item>
    </a-descriptions>
    <a-space style="margin-top: 16px">
      <a-button @click="go('/address')">管理地址</a-button>
      <a-button @click="go('/orders')">我的订单</a-button>
      <a-button status="danger" @click="handleLogout">退出登录</a-button>
    </a-space>
  </a-card>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../store/auth'

const router = useRouter()
const authStore = useAuthStore()

const go = (path: string) => {
  router.push(path)
}

const handleLogout = async () => {
  authStore.clear()
  await router.replace('/login')
}
</script>

