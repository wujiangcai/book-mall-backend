import request from '../request'
import type { AdminUserDetail, AdminUserListItem, PageResult } from '../../types/api'

export type AdminUserListParams = {
  page?: number
  pageSize?: number
  keyword?: string
}

export type UserStatusRequest = {
  status: number
}

export const adminUserApi = {
  list: (params?: AdminUserListParams) => request.get<PageResult<AdminUserListItem>>('/api/admin/users', { params }),
  detail: (id: number | string) => request.get<AdminUserDetail>(`/api/admin/users/${id}`),
  updateStatus: (id: number | string, payload: UserStatusRequest) =>
    request.put<void>(`/api/admin/users/${id}/status`, payload),
  changePassword: (payload: { oldPassword: string; newPassword: string }) =>
    request.put<void>('/api/admin/users/password', payload),
}

export default adminUserApi
