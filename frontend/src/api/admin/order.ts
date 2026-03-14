import request from '../request'
import type { AdminOrderDetail, AdminOrderListItem, PageResult } from '../../types/api'

export type AdminOrderListParams = {
  page?: number
  pageSize?: number
  status?: number
  orderNo?: string
  userId?: number
}

export const adminOrderApi = {
  list: (params?: AdminOrderListParams) =>
    request.get<PageResult<AdminOrderListItem>>('/api/admin/orders', { params }),
  detail: (id: number | string) => request.get<AdminOrderDetail>(`/api/admin/orders/${id}`),
  ship: (id: number | string) => request.put<void>(`/api/admin/orders/${id}/ship`),
}

export default adminOrderApi
