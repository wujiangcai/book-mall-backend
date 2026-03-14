import request from '../request'
import type { AuthResponse } from '../../types/api'

export type AdminLoginRequest = {
  username: string
  password: string
}

export const adminAuthApi = {
  login: (payload: AdminLoginRequest) => request.post<AuthResponse>('/api/admin/auth/login', payload),
}

export default adminAuthApi
