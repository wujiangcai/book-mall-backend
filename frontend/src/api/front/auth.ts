import request from '../request'
import type { AuthResponse } from '../../types/api'

export type RegisterRequest = {
  username: string
  password: string
  phone: string
  nickname?: string
  email?: string
}

export type LoginRequest = {
  username: string
  password: string
}

export const frontAuthApi = {
  register: (payload: RegisterRequest) => request.post<AuthResponse>('/api/front/auth/register', payload),
  login: (payload: LoginRequest) => request.post<AuthResponse>('/api/front/auth/login', payload),
}

export default frontAuthApi
