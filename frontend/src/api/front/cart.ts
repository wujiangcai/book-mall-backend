import request from '../request'
import type { CartItem } from '../../types/api'

export type CartAddRequest = {
  bookId: number
  quantity: number
}

export type CartUpdateRequest = {
  quantity: number
}

export const frontCartApi = {
  list: () => request.get<CartItem[]>('/api/front/cart'),
  add: (payload: CartAddRequest) => request.post<void>('/api/front/cart', payload),
  update: (id: number | string, payload: CartUpdateRequest) =>
    request.put<void>(`/api/front/cart/${id}`, payload),
  remove: (id: number | string) => request.delete<void>(`/api/front/cart/${id}`),
}

export default frontCartApi
