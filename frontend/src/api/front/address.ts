import request from '../request'
import type { Address } from '../../types/api'

export type AddressCreateRequest = {
  receiverName: string
  phone: string
  province?: string
  city?: string
  district?: string
  detailAddress: string
  isDefault?: number
}

export type AddressUpdateRequest = {
  receiverName: string
  phone: string
  province?: string
  city?: string
  district?: string
  detailAddress: string
}

export const frontAddressApi = {
  list: () => request.get<Address[]>('/api/front/user/addresses'),
  create: (payload: AddressCreateRequest) => request.post<void>('/api/front/user/addresses', payload),
  update: (id: number | string, payload: AddressUpdateRequest) =>
    request.put<void>(`/api/front/user/addresses/${id}`, payload),
  remove: (id: number | string) => request.delete<void>(`/api/front/user/addresses/${id}`),
  setDefault: (id: number | string) => request.put<void>(`/api/front/user/addresses/${id}/default`),
}

export default frontAddressApi
