import type { RouteRecordRaw } from 'vue-router'

export const adminRoutes: RouteRecordRaw[] = [
  { path: '/admin/login', name: 'AdminLogin', component: () => import('../views/admin/AdminLogin.vue') },
  { path: '/admin/dashboard', name: 'Dashboard', component: () => import('../views/admin/Dashboard.vue') },
  { path: '/admin/books', name: 'BookManage', component: () => import('../views/admin/BookManage.vue') },
  { path: '/admin/categories', name: 'CategoryManage', component: () => import('../views/admin/CategoryManage.vue') },
  { path: '/admin/orders', name: 'OrderManage', component: () => import('../views/admin/OrderManage.vue') },
  { path: '/admin/users', name: 'UserManage', component: () => import('../views/admin/UserManage.vue') },
  { path: '/admin/banners', name: 'BannerManage', component: () => import('../views/admin/BannerManage.vue') },
]
