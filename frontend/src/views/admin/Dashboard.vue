<template>
  <a-space direction="vertical" fill size="large">
    <a-card :bordered="false">
      <div class="admin-title">后台首页</div>
      <div class="admin-sub" v-if="authStore.userInfo">当前管理员：{{ authStore.userInfo.username }}</div>
    </a-card>

    <a-card :bordered="false">
      <a-space wrap>
        <a-button type="primary" @click="go('/admin/books')">图书管理</a-button>
        <a-button @click="go('/admin/categories')">分类管理</a-button>
        <a-button @click="go('/admin/orders')">订单管理</a-button>
        <a-button @click="go('/admin/users')">用户管理</a-button>
        <a-button @click="go('/admin/banners')">轮播管理</a-button>
        <a-button status="danger" @click="handleLogout">退出登录</a-button>
      </a-space>
    </a-card>
  </a-space>
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
  await router.replace('/admin/login')
}
</script>

<style scoped>
.admin-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--brand-primary);
}

.admin-sub {
  margin-top: 8px;
  color: var(--brand-muted);
}
</style>
