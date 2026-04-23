import request from '../request'
import type { AdminAddress, AdminUserDetail, AdminUserListItem, PageResult } from '../../types/api'

export type AdminUserCreateRequest = {
  username: string
  password: string
  phone?: string
  nickname?: string
  email?: string
  role: number
  status: number
}

export type AdminUserUpdateRequest = {
  username: string
  phone?: string
  nickname?: string
  email?: string
  role: number
  status: number
}

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
  addresses: (id: number | string) => request.get<AdminAddress[]>(`/api/admin/users/${id}/addresses`),
  create: (payload: AdminUserCreateRequest) => request.post<void>('/api/admin/users', payload),
  update: (id: number | string, payload: AdminUserUpdateRequest) => request.put<void>(`/api/admin/users/${id}`, payload),
  updateStatus: (id: number | string, payload: UserStatusRequest) =>
    request.put<void>(`/api/admin/users/${id}/status`, payload),
  resetPassword: (id: number | string) => request.put<void>(`/api/admin/users/${id}/reset-password`),
  changePassword: (payload: { oldPassword: string; newPassword: string }) =>
    request.put<void>('/api/admin/users/password', payload),
}

export default adminUserApi
