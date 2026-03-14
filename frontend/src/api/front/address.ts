import request from '../request'

export const frontAddressApi = {
  list: () => request.get('/api/front/user/addresses'),
  create: (payload: unknown) => request.post('/api/front/user/addresses', payload),
  update: (id: number | string, payload: unknown) => request.put(`/api/front/user/addresses/${id}`, payload),
  remove: (id: number | string) => request.delete(`/api/front/user/addresses/${id}`),
  setDefault: (id: number | string) => request.put(`/api/front/user/addresses/${id}/default`),
}

export default frontAddressApi
