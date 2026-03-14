import request from '../request'

export const adminCategoryApi = {
  list: () => request.get('/api/admin/categories'),
  create: (payload: unknown) => request.post('/api/admin/categories', payload),
  update: (id: number | string, payload: unknown) => request.put(`/api/admin/categories/${id}`, payload),
  remove: (id: number | string) => request.delete(`/api/admin/categories/${id}`),
  updateStatus: (id: number | string, payload: unknown) => request.put(`/api/admin/categories/${id}/status`, payload),
}

export default adminCategoryApi
