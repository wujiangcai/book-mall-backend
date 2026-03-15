import request from '../request'
import type { CategoryAdminItem, PageResult } from '../../types/api'

export type CategoryAdminListParams = {
  page?: number
  pageSize?: number
}

export type CategoryCreateRequest = {
  categoryName: string
  parentId?: number
  sortOrder?: number
}

export type CategoryUpdateRequest = CategoryCreateRequest

export type CategoryStatusRequest = {
  status: number
}

export const adminCategoryApi = {
  list: (params?: CategoryAdminListParams) =>
    request.get<PageResult<CategoryAdminItem>>('/api/admin/categories', { params }),
  create: (payload: CategoryCreateRequest) => request.post<void>('/api/admin/categories', payload),
  update: (id: number | string, payload: CategoryUpdateRequest) =>
    request.put<void>(`/api/admin/categories/${id}`, payload),
  remove: (id: number | string) => request.delete<void>(`/api/admin/categories/${id}`),
  updateStatus: (id: number | string, payload: CategoryStatusRequest) =>
    request.put<void>(`/api/admin/categories/${id}/status`, payload),
}

export default adminCategoryApi
