import request from '../request'

export const frontCartApi = {
  list: () => request.get('/api/front/cart'),
  add: (payload: unknown) => request.post('/api/front/cart', payload),
  update: (id: number | string, payload: unknown) => request.put(`/api/front/cart/${id}`, payload),
  remove: (id: number | string) => request.delete(`/api/front/cart/${id}`),
}

export default frontCartApi
