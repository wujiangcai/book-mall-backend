import type { RouteRecordRaw } from 'vue-router'

export const adminRoutes: RouteRecordRaw[] = [
  { path: '/admin/login', name: 'AdminLogin', component: () => import('../views/admin/AdminLogin.vue') },
  {
    path: '/admin',
    name: 'AdminRoot',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { requiresAdmin: true },
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/admin/Dashboard.vue'), meta: { requiresAdmin: true } },
      { path: 'books', name: 'BookManage', component: () => import('../views/admin/BookManage.vue'), meta: { requiresAdmin: true } },
      { path: 'books/stock', name: 'BookStock', component: () => import('../views/admin/BookManage.vue'), meta: { requiresAdmin: true } },
      { path: 'categories', name: 'CategoryManage', component: () => import('../views/admin/CategoryManage.vue'), meta: { requiresAdmin: true } },
      { path: 'orders', name: 'OrderManage', component: () => import('../views/admin/OrderManage.vue'), meta: { requiresAdmin: true } },
      { path: 'orders/refund', name: 'OrderRefund', component: () => import('../views/admin/OrderManage.vue'), meta: { requiresAdmin: true } },
      { path: 'orders/logistics', name: 'OrderLogistics', component: () => import('../views/admin/OrderManage.vue'), meta: { requiresAdmin: true } },
      { path: 'users', name: 'UserManage', component: () => import('../views/admin/UserManage.vue'), meta: { requiresAdmin: true } },
      { path: 'users/roles', name: 'UserRoles', component: () => import('../views/admin/UserManage.vue'), meta: { requiresAdmin: true } },
      { path: 'banners', name: 'BannerManage', component: () => import('../views/admin/BannerManage.vue'), meta: { requiresAdmin: true } },
      { path: 'banners/coupon', name: 'BannerCoupon', component: () => import('../views/admin/BannerManage.vue'), meta: { requiresAdmin: true } },
      { path: 'banners/notice', name: 'BannerNotice', component: () => import('../views/admin/BannerManage.vue'), meta: { requiresAdmin: true } },
      { path: 'settings', name: 'AdminSettings', component: () => import('../views/admin/Settings.vue'), meta: { requiresAdmin: true } },
      { path: 'logs', name: 'AdminLogs', component: () => import('../views/admin/Logs.vue'), meta: { requiresAdmin: true } },
    ],
  },
]
