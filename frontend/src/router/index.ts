import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { frontRoutes } from './front'
import { adminRoutes } from './admin'

const routes: RouteRecordRaw[] = [
  ...frontRoutes,
  ...adminRoutes,
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(() => {
  // TODO: integrate token/role checks and 401/403 handling
  return true
})

export default router
