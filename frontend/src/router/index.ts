import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { frontRoutes } from './front'
import { adminRoutes } from './admin'
import { clearAuth, getRole, getToken } from '../utils/auth'

const routes: RouteRecordRaw[] = [
  ...frontRoutes,
  ...adminRoutes,
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const token = getToken()
  const role = getRole()

  if (to.meta.requiresAdmin) {
    if (!token || role !== 1) {
      clearAuth()
      return { path: '/admin/login', query: { redirect: to.fullPath } }
    }
  }

  if (to.meta.requiresAuth) {
    if (!token) {
      clearAuth()
      return { path: '/login', query: { redirect: to.fullPath } }
    }
  }

  return true
})

export default router
