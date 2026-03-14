import request from '../request'

export const adminOrderApi = {
  list: (params?: Record<string, unknown>) => request.get('/api/admin/orders', { params }),
  detail: (id: number | string) => request.get(`/api/admin/orders/${id}`),
  ship: (id: number | string) => request.put(`/api/admin/orders/${id}/ship`),
}

export default adminOrderApi
