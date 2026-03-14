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
      this.token = null
      this.role = null
      this.userId = null
      this.username = null
      this.userInfo = null
      clearAuth()
    },
    async fetchUserInfo() {
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
