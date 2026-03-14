import type { RouteRecordRaw } from 'vue-router'

export const adminRoutes: RouteRecordRaw[] = [
  { path: '/admin/login', name: 'AdminLogin', component: () => import('../views/admin/AdminLogin.vue') },
  { path: '/admin/dashboard', name: 'Dashboard', component: () => import('../views/admin/Dashboard.vue'), meta: { requiresAdmin: true } },
  { path: '/admin/books', name: 'BookManage', component: () => import('../views/admin/BookManage.vue'), meta: { requiresAdmin: true } },
  { path: '/admin/categories', name: 'CategoryManage', component: () => import('../views/admin/CategoryManage.vue'), meta: { requiresAdmin: true } },
  { path: '/admin/orders', name: 'OrderManage', component: () => import('../views/admin/OrderManage.vue'), meta: { requiresAdmin: true } },
  { path: '/admin/users', name: 'UserManage', component: () => import('../views/admin/UserManage.vue'), meta: { requiresAdmin: true } },
  { path: '/admin/banners', name: 'BannerManage', component: () => import('../views/admin/BannerManage.vue'), meta: { requiresAdmin: true } },
]
