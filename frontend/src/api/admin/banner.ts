import request from '../request'
import type { Banner } from '../../types/api'

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
  list: () => request.get<Banner[]>('/api/admin/banners'),
  create: (payload: BannerCreateRequest) => request.post<void>('/api/admin/banners', payload),
  update: (id: number | string, payload: BannerUpdateRequest) => request.put<void>(`/api/admin/banners/${id}`, payload),
  remove: (id: number | string) => request.delete<void>(`/api/admin/banners/${id}`),
  updateSort: (id: number | string, payload: BannerSortRequest) =>
    request.put<void>(`/api/admin/banners/${id}/sort`, payload),
}

export default adminBannerApi
