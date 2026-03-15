import request from '../request'
import type { AdminOrderDetail, AdminOrderListItem, PageResult } from '../../types/api'

export type AdminOrderListParams = {
  page?: number
  pageSize?: number
  status?: number
  orderNo?: string
  userId?: number
}

export type AdminOrderCreateRequest = {
  userId: number
  addressId: number
  items: Array<{ bookId: number; quantity: number }>
}

export type AdminOrderUpdateRequest = {
  status: number
  addressId: number
}

export const adminOrderApi = {
  list: (params?: AdminOrderListParams) =>
    request.get<PageResult<AdminOrderListItem>>('/api/admin/orders', { params }),
  detail: (id: number | string) => request.get<AdminOrderDetail>(`/api/admin/orders/${id}`),
  create: (payload: AdminOrderCreateRequest) => request.post<void>('/api/admin/orders', payload),
  update: (id: number | string, payload: AdminOrderUpdateRequest) =>
    request.put<void>(`/api/admin/orders/${id}`, payload),
  cancel: (id: number | string) => request.put<void>(`/api/admin/orders/${id}/cancel`),
  ship: (id: number | string) => request.put<void>(`/api/admin/orders/${id}/ship`),
}

export default adminOrderApi
