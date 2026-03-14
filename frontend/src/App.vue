<template>
  <a-layout class="app-layout">
    <a-layout-header class="app-header card-glass">
      <div class="app-container header-inner">
        <div class="logo" @click="go('/')">书城 · 文艺书店</div>
        <a-input-search
          class="nav-search"
          v-model="keyword"
          placeholder="搜索图书、作者、ISBN"
          allow-clear
          @search="handleSearch"
        />
        <a-space class="nav-links">
          <a-link @click="go('/books')">图书</a-link>
          <a-badge :count="cartCount" :offset="[6, -2]">
            <a-link @click="go('/cart')">购物车</a-link>
          </a-badge>
          <a-link @click="go('/orders')">订单</a-link>
          <a-link @click="go('/address')">地址</a-link>
          <a-link v-if="isAuthed" @click="go('/user')">用户中心</a-link>
          <a-link v-if="!isAuthed" @click="go('/login')">登录</a-link>
          <a-link v-if="!isAuthed" @click="go('/register')">注册</a-link>
          <a-link v-if="isAuthed" @click="logout">退出</a-link>
        </a-space>
      </div>
    </a-layout-header>

    <a-layout-content class="app-content">
      <div class="app-container">
        <router-view />
      </div>
    </a-layout-content>

    <a-layout-footer class="app-footer">
      <div class="app-container">
        <div class="footer-brand">书城 · 现代文艺书店</div>
        <div class="footer-meta">精选好书 · 安静阅读 · 2026</div>
      </div>
    </a-layout-footer>
    <a-back-top :visible-height="400" />
  </a-layout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './store/auth'
import frontCartApi from './api/front/cart'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const keyword = ref('')
const cartCount = ref(0)

const isAuthed = computed(() => authStore.isAuthed)

const go = (path: string) => {
  router.push(path)
}

const handleSearch = () => {
  if (!keyword.value) return
  router.push({ path: '/books', query: { keyword: keyword.value } })
}

const loadCartCount = async () => {
  if (!authStore.isAuthed) {
    cartCount.value = 0
    return
  }
  const data = (await frontCartApi.list().catch(() => [])) as any
  cartCount.value = data?.length || 0
}

const logout = async () => {
  authStore.clear()
  cartCount.value = 0
  await router.replace('/login')
}

onMounted(() => {
  keyword.value = (route.query.keyword as string) || ''
  loadCartCount()
})
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  background: transparent;
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  padding: 12px 0;
  border-bottom: 1px solid var(--brand-border);
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.logo {
  font-weight: 600;
  color: var(--brand-primary);
  cursor: pointer;
  white-space: nowrap;
}

.nav-search {
  flex: 1;
  max-width: 420px;
}

.nav-links :deep(.arco-link) {
  color: var(--brand-primary);
  font-weight: 500;
}

.app-content {
  padding: 32px 0 48px;
}

.app-footer {
  background: transparent;
  border-top: 1px solid var(--brand-border);
  padding: 24px 0 48px;
  text-align: center;
  color: var(--brand-muted);
}

.footer-brand {
  font-weight: 600;
  color: var(--brand-primary);
}

.footer-meta {
  margin-top: 4px;
}

@media (max-width: 992px) {
  .header-inner {
    flex-wrap: wrap;
  }

  .nav-search {
    order: 3;
    max-width: 100%;
  }
}
</style>
