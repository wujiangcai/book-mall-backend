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
  // 路由前置守卫：在页面切换前判断当前页面是否需要登录或管理员权限。
  const token = getToken()
  const role = getRole()

  if (to.meta.requiresAdmin) {
    // 后台页面需要管理员身份，角色不对时直接跳后台登录页。
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
    // 前台个人中心、购物车、订单等页面要求用户先登录。
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
