import request from '../request'
import type { UserInfoResponse } from '../../types/api'

export type UpdateUserRequest = {
  nickname?: string
  email?: string
  phone?: string
}

export const frontUserApi = {
  getInfo: () => request.get<UserInfoResponse>('/api/front/user/info'),
  updateInfo: (payload: UpdateUserRequest) => request.put<void>('/api/front/user/info', payload),
}

export default frontUserApi
