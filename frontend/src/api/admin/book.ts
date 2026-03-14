import request from '../request'

export const adminBookApi = {
  list: (params?: Record<string, unknown>) => request.get('/api/admin/books', { params }),
  create: (payload: unknown) => request.post('/api/admin/books', payload),
  update: (id: number | string, payload: unknown) => request.put(`/api/admin/books/${id}`, payload),
  remove: (id: number | string) => request.delete(`/api/admin/books/${id}`),
  updateStatus: (id: number | string, payload: unknown) => request.put(`/api/admin/books/${id}/status`, payload),
}

export default adminBookApi
