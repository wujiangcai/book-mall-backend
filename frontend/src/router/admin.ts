import type { RouteRecordRaw } from 'vue-router'

export const adminRoutes: RouteRecordRaw[] = [
  { path: '/admin/login', name: 'AdminLogin', component: () => import('../views/admin/AdminLogin.vue') },
  {
    path: '/admin',
    name: 'AdminRoot',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { requiresAdmin: true },
    children: [
      { path: 'books', name: 'BookManage', component: () => import('../views/admin/BookManage.vue'), meta: { requiresAdmin: true } },
      { path: 'categories', name: 'CategoryManage', component: () => import('../views/admin/CategoryManage.vue'), meta: { requiresAdmin: true } },
      { path: 'orders', name: 'OrderManage', component: () => import('../views/admin/OrderManage.vue'), meta: { requiresAdmin: true } },
      { path: 'users', name: 'UserManage', component: () => import('../views/admin/UserManage.vue'), meta: { requiresAdmin: true } },
      { path: 'banners', name: 'BannerManage', component: () => import('../views/admin/BannerManage.vue'), meta: { requiresAdmin: true } },
    ],
  },
]
