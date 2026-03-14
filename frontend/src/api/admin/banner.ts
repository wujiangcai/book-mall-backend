import request from '../request'

export const adminBannerApi = {
  list: () => request.get('/api/admin/banners'),
  create: (payload: unknown) => request.post('/api/admin/banners', payload),
  update: (id: number | string, payload: unknown) => request.put(`/api/admin/banners/${id}`, payload),
  remove: (id: number | string) => request.delete(`/api/admin/banners/${id}`),
  updateSort: (id: number | string, payload: unknown) => request.put(`/api/admin/banners/${id}/sort`, payload),
}

export default adminBannerApi
