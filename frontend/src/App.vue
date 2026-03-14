<template>
  <a-layout class="app-layout">
    <a-layout-header class="app-header">
      <div class="logo" @click="go('/')">书城</div>
      <a-space>
        <a-link @click="go('/books')">图书</a-link>
        <a-link @click="go('/cart')">购物车</a-link>
        <a-link @click="go('/orders')">订单</a-link>
        <a-link @click="go('/address')">地址</a-link>
        <a-link @click="go('/user')">用户中心</a-link>
        <a-link @click="go('/login')">登录</a-link>
      </a-space>
    </a-layout-header>
    <a-layout-content class="app-content">
      <router-view />
    </a-layout-content>
  </a-layout>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const go = (path: string) => {
  router.push(path)
}
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.logo {
  font-weight: 600;
  cursor: pointer;
}

.app-content {
  padding: 24px;
}
</style>
