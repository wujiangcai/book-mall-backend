<template>
  <a-layout class="admin-layout">
    <a-layout-sider
      class="admin-sider"
      :width="220"
      :collapsed="collapsed"
      :collapsible="false"
      breakpoint="lg"
    >
      <div class="sider-logo">
        <span>书城·管理后台</span>
      </div>
      <a-menu
        class="admin-menu"
        :selected-keys="selectedKeys"
        v-model:open-keys="openKeys"
        :collapsed="collapsed"
        @menu-item-click="handleMenuClick"
      >
        <a-sub-menu key="books">
          <template #title>
            <icon-book />
            <span>图书管理</span>
          </template>
          <a-menu-item key="/admin/books">图书列表</a-menu-item>
          <a-menu-item key="/admin/categories">分类管理</a-menu-item>
        </a-sub-menu>
        <a-sub-menu key="orders">
          <template #title>
            <icon-file />
            <span>订单管理</span>
          </template>
          <a-menu-item key="/admin/orders">订单列表</a-menu-item>
        </a-sub-menu>
        <a-sub-menu key="users">
          <template #title>
            <icon-user />
            <span>用户管理</span>
          </template>
          <a-menu-item key="/admin/users">用户列表</a-menu-item>
        </a-sub-menu>
        <a-sub-menu key="marketing">
          <template #title>
            <icon-notification />
            <span>营销中心</span>
          </template>
          <a-menu-item key="/admin/banners">轮播图</a-menu-item>
        </a-sub-menu>
      </a-menu>
    </a-layout-sider>

    <a-drawer
      class="admin-drawer"
      :visible="drawerVisible"
      placement="left"
      :width="240"
      @cancel="drawerVisible = false"
      :footer="false"
    >
      <div class="sider-logo">
        <span>书城·管理后台</span>
      </div>
      <a-menu
        class="admin-menu"
        :selected-keys="selectedKeys"
        v-model:open-keys="openKeys"
        @menu-item-click="handleMenuClick"
      >
        <a-sub-menu key="books">
          <template #title>
            <icon-book />
            <span>图书管理</span>
          </template>
          <a-menu-item key="/admin/books">图书列表</a-menu-item>
          <a-menu-item key="/admin/categories">分类管理</a-menu-item>
        </a-sub-menu>
        <a-sub-menu key="orders">
          <template #title>
            <icon-file />
            <span>订单管理</span>
          </template>
          <a-menu-item key="/admin/orders">订单列表</a-menu-item>
        </a-sub-menu>
        <a-sub-menu key="users">
          <template #title>
            <icon-user />
            <span>用户管理</span>
          </template>
          <a-menu-item key="/admin/users">用户列表</a-menu-item>
        </a-sub-menu>
        <a-sub-menu key="marketing">
          <template #title>
            <icon-notification />
            <span>营销中心</span>
          </template>
          <a-menu-item key="/admin/banners">轮播图</a-menu-item>
        </a-sub-menu>
      </a-menu>
    </a-drawer>

    <a-layout class="admin-main">
      <a-layout-header class="admin-header">
        <div class="header-left">
          <a-button class="collapse-btn" type="text" @click="toggleCollapse">
            <icon-menu-fold v-if="!collapsed" />
            <icon-menu-unfold v-else />
          </a-button>
          <a-button class="mobile-btn" type="text" @click="drawerVisible = true">
            <icon-menu />
          </a-button>
          <a-breadcrumb class="admin-breadcrumb">
            <a-breadcrumb-item>管理后台</a-breadcrumb-item>
            <a-breadcrumb-item>{{ breadcrumb }}</a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        <div class="header-right">
          <a-dropdown @select="handleUserAction">
            <a-space class="admin-user" size="small">
              <a-avatar :size="32">管</a-avatar>
              <span>{{ adminName }}</span>
              <icon-down />
            </a-space>
            <template #content>
              <a-doption value="profile">个人信息</a-doption>
              <a-doption value="logout">退出登录</a-doption>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>
      <a-layout-content class="admin-content">
        <div class="admin-content-inner">
          <router-view />
        </div>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../store/auth'
import {
  IconBook,
  IconFile,
  IconUser,
  IconNotification,
  IconMenuFold,
  IconMenuUnfold,
  IconMenu,
  IconDown,
} from '@arco-design/web-vue/es/icon'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const collapsed = ref(false)
const drawerVisible = ref(false)

const adminName = computed(() => authStore.userInfo?.username || '管理员')
const selectedKeys = computed(() => {
  const base = route.path
  return [base]
})

const openKeys = ref<string[]>([])

const syncOpenKeys = () => {
  if (route.path.startsWith('/admin/books') || route.path.startsWith('/admin/categories')) {
    openKeys.value = ['books']
    return
  }
  if (route.path.startsWith('/admin/orders')) {
    openKeys.value = ['orders']
    return
  }
  if (route.path.startsWith('/admin/users')) {
    openKeys.value = ['users']
    return
  }
  if (route.path.startsWith('/admin/banners')) {
    openKeys.value = ['marketing']
    return
  }
  openKeys.value = []
}

syncOpenKeys()

watch(
  () => route.path,
  () => {
    syncOpenKeys()
  }
)

const breadcrumb = computed(() => {
  const map: Record<string, string> = {
    '/admin/books': '图书列表',
    '/admin/categories': '分类管理',
    '/admin/orders': '订单列表',
    '/admin/users': '用户列表',
    '/admin/banners': '轮播图',
    '/admin/profile': '个人信息',
  }
  return map[route.path] || '管理模块'
})

const toggleCollapse = () => {
  collapsed.value = !collapsed.value
}

const handleMenuClick = (key: string) => {
  drawerVisible.value = false
  if (key.includes('?')) {
    const [path, search] = key.split('?')
    const query = Object.fromEntries(new URLSearchParams(search).entries())
    router.push({ path, query })
    return
  }
  router.push(key)
}

const handleUserAction = async (value: string | number | Record<string, any> | undefined) => {
  if (value === 'logout') {
    authStore.clear()
    await router.replace('/admin/login')
    return
  }
  if (value === 'profile') {
    router.push('/admin/profile')
  }
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background: #0f172a;
  color: #e2e8f0;
}

.admin-sider {
  background: #ffffff;
  color: #1f2937;
  border-right: 1px solid rgba(15, 23, 42, 0.08);
  display: none;
}

.sider-logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: #1e3a5f;
  letter-spacing: 1px;
}

.admin-menu {
  background: transparent;
  color: #1f2937;
}

.admin-menu :deep(.arco-menu-sub-menu-title) {
  color: #1f2937;
  font-weight: 500;
  font-size: 16px;
}

.admin-menu :deep(.arco-menu-sub-menu .arco-menu-item) {
  color: #374151;
  font-weight: 400;
  font-size: 14px;
}

.admin-menu :deep(.arco-menu-item-selected) {
  background: #1e3a5f;
  color: #ffffff;
  font-weight: 600;
}

.admin-menu :deep(.arco-menu-item:hover),
.admin-menu :deep(.arco-menu-sub-menu-title:hover) {
  background: #f3f4f6;
  color: #1f2937;
}

.admin-menu :deep(.arco-menu-icon),
.admin-menu :deep(.arco-menu-icon svg) {
  color: currentColor;
}

.admin-main {
  background: #f1f5f9;
  min-height: 100vh;
}

.admin-header {
  height: 64px;
  background: #ffffff;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-search {
  width: 220px;
}

.admin-user {
  cursor: pointer;
  color: #000000;
}

.admin-content {
  padding: 24px;
}

.admin-content-inner {
  min-height: calc(100vh - 112px);
}

.collapse-btn {
  display: none;
}

.mobile-btn {
  display: inline-flex;
}

.admin-drawer :deep(.arco-drawer-body) {
  padding: 0;
  background: #1e3a5f;
  color: #f8fafc;
}

@media (min-width: 992px) {
  .admin-sider {
    display: block;
  }

  .mobile-btn {
    display: none;
  }

  .collapse-btn {
    display: inline-flex;
  }
}

@media (max-width: 768px) {
  .admin-header {
    padding: 0 16px;
  }

  .admin-search {
    display: none;
  }
}
</style>
