import request from '../request'

export const frontOrderApi = {
  create: (payload: unknown) => request.post('/api/front/orders', payload),
  pay: (id: number | string) => request.post(`/api/front/orders/${id}/pay`),
  cancel: (id: number | string) => request.post(`/api/front/orders/${id}/cancel`),
  list: (params?: Record<string, unknown>) => request.get('/api/front/orders', { params }),
  detail: (id: number | string) => request.get(`/api/front/orders/${id}`),
}

export default frontOrderApi
