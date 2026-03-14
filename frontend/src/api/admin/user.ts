import request from '../request'

export const adminUserApi = {
  list: (params?: Record<string, unknown>) => request.get('/api/admin/users', { params }),
  detail: (id: number | string) => request.get(`/api/admin/users/${id}`),
  updateStatus: (id: number | string, payload: unknown) => request.put(`/api/admin/users/${id}/status`, payload),
}

export default adminUserApi
