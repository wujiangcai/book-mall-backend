import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { frontRoutes } from './front'
import { adminRoutes } from './admin'
import { UserRole } from '../types/enums'
import { clearAuth, getRole, getToken } from '../utils/auth'
import { useAuthStore } from '../store/auth'

const routes: RouteRecordRaw[] = [
  ...frontRoutes,
  ...adminRoutes,
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  const token = getToken()
  const role = getRole()

  if (to.meta.requiresAdmin) {
    if (!token || role !== UserRole.Admin) {
      clearAuth()
      return { path: '/admin/login', query: { redirect: to.fullPath } }
    }
    const authStore = useAuthStore()
    if (!authStore.userInfo && authStore.isAuthed) {
      try {
        await authStore.fetchUserInfo()
      } catch (error: any) {
        authStore.clear()
        return { path: '/admin/login', query: { redirect: to.fullPath } }
      }
    }
  }

  if (to.meta.requiresAuth) {
    if (!token) {
      clearAuth()
      return { path: '/login', query: { redirect: to.fullPath } }
    }
    const authStore = useAuthStore()
    if (!authStore.userInfo && authStore.isAuthed) {
      try {
        await authStore.fetchUserInfo()
      } catch (error: any) {
        authStore.clear()
        return { path: '/login', query: { redirect: to.fullPath } }
      }
    }
  }

  return true
})

export default router
