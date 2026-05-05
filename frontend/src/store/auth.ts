import { defineStore } from 'pinia'
import frontUserApi from '../api/front/user'
import type { RoleValue } from '../types/enums'
import { UserRole } from '../types/enums'
import {
  clearAuth,
  getRole,
  getToken,
  getUserId,
  getUsername,
  setRole,
  setToken,
  setUserId,
  setUsername,
} from '../utils/auth'

export type UserInfo = {
  id: number
  username: string
  nickname?: string
  phone?: string
  email?: string
  role?: RoleValue
}

// Pinia 登录态仓库：统一管理 token、角色、用户信息，避免组件各自维护一份状态。
export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getToken() as string | null,
    role: getRole() as RoleValue | null,
    userId: getUserId() as number | null,
    username: getUsername() as string | null,
    userInfo: null as UserInfo | null,
    loading: false,
  }),
  getters: {
    isAuthed: (state) => Boolean(state.token),
    isAdmin: (state) => state.role === UserRole.Admin,
  },
  actions: {
    setAuth(payload: { token?: string; role?: RoleValue; userId?: number; username?: string }) {
      if (payload.token) {
        this.token = payload.token
        setToken(payload.token)
      }
      if (payload.role !== undefined) {
        this.role = payload.role
        setRole(payload.role)
      }
      if (payload.userId !== undefined) {
        this.userId = payload.userId
        setUserId(payload.userId)
      }
      if (payload.username !== undefined) {
        this.username = payload.username
        setUsername(payload.username)
      }
    },
    clear() {
      // 主动退出登录或登录过期时，清空内存态和本地缓存。
      this.token = null
      this.role = null
      this.userId = null
      this.username = null
      this.userInfo = null
      clearAuth()
    },
    async fetchUserInfo() {
      // 刷新页面后，前端会根据本地 token 重新请求一次用户资料，恢复登录态展示。
      this.loading = true
      try {
        const data = (await frontUserApi.getInfo()) as any
        this.userInfo = data || null
        return data as UserInfo
      } finally {
        this.loading = false
      }
    },
  },
})
