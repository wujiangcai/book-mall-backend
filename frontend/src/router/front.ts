import type { RouteRecordRaw } from 'vue-router'

export const frontRoutes: RouteRecordRaw[] = [
  { path: '/', name: 'FrontHome', component: () => import('../views/front/Home.vue') },
  { path: '/books', name: 'BookList', component: () => import('../views/front/BookList.vue') },
  { path: '/book/:id', name: 'BookDetail', component: () => import('../views/front/BookDetail.vue') },
  { path: '/cart', name: 'Cart', component: () => import('../views/front/Cart.vue'), meta: { requiresAuth: true } },
  { path: '/orders', name: 'OrderList', component: () => import('../views/front/OrderList.vue'), meta: { requiresAuth: true } },
  { path: '/order/:id', name: 'OrderDetail', component: () => import('../views/front/OrderDetail.vue'), meta: { requiresAuth: true } },
  { path: '/login', name: 'FrontLogin', component: () => import('../views/front/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/front/Register.vue') },
  { path: '/user', name: 'UserCenter', component: () => import('../views/front/UserCenter.vue'), meta: { requiresAuth: true } },
  { path: '/address', name: 'AddressManage', component: () => import('../views/front/AddressManage.vue'), meta: { requiresAuth: true } },
]
