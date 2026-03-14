import request from '../request'

export const frontBookApi = {
  list: (params?: Record<string, unknown>) => request.get('/api/front/books', { params }),
  detail: (id: number | string) => request.get(`/api/front/books/${id}`),
}

export default frontBookApi
