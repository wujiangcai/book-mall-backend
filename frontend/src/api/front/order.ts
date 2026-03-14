import request from '../request'
import type { OrderCreateResponse, OrderDetail, OrderListItem, PageResult } from '../../types/api'

export type OrderCreateRequest = {
  addressId: number
  cartIds: number[]
}

export type OrderListParams = {
  page?: number
  pageSize?: number
}

export const frontOrderApi = {
  create: (payload: OrderCreateRequest) => request.post<OrderCreateResponse>('/api/front/orders', payload),
  pay: (id: number | string) => request.post<void>(`/api/front/orders/${id}/pay`),
  cancel: (id: number | string) => request.post<void>(`/api/front/orders/${id}/cancel`),
  list: (params?: OrderListParams) => request.get<PageResult<OrderListItem>>('/api/front/orders', { params }),
  detail: (id: number | string) => request.get<OrderDetail>(`/api/front/orders/${id}`),
}

export default frontOrderApi
