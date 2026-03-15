import request from '../request'
import type { Banner, PageResult } from '../../types/api'

export type BannerAdminListParams = {
  page?: number
  pageSize?: number
}

export type BannerCreateRequest = {
  imageUrl: string
  linkUrl?: string
  sortOrder?: number
  status?: number
}

export type BannerUpdateRequest = BannerCreateRequest

export type BannerSortRequest = {
  sortOrder: number
}

export const adminBannerApi = {
  list: (params?: BannerAdminListParams) => request.get<PageResult<Banner>>('/api/admin/banners', { params }),
  create: (payload: BannerCreateRequest) => request.post<void>('/api/admin/banners', payload),
  update: (id: number | string, payload: BannerUpdateRequest) => request.put<void>(`/api/admin/banners/${id}`, payload),
  remove: (id: number | string) => request.delete<void>(`/api/admin/banners/${id}`),
  updateSort: (id: number | string, payload: BannerSortRequest) =>
    request.put<void>(`/api/admin/banners/${id}/sort`, payload),
}

export default adminBannerApi
