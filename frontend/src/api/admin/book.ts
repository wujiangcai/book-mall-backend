import request from '../request'
import type { AdminBookListItem, PageResult } from '../../types/api'

export type AdminBookListParams = {
  page?: number
  pageSize?: number
  categoryId?: number
  keyword?: string
}

export type BookCreateRequest = {
  bookName: string
  author?: string
  publisher?: string
  isbn?: string
  categoryId: number
  price: number
  stock: number
  coverImage?: string
  description?: string
  status: number
}

export type BookUpdateRequest = BookCreateRequest

export type BookStatusRequest = {
  status: number
}

export const adminBookApi = {
  list: (params?: AdminBookListParams) => request.get<PageResult<AdminBookListItem>>('/api/admin/books', { params }),
  create: (payload: BookCreateRequest) => request.post<void>('/api/admin/books', payload),
  update: (id: number | string, payload: BookUpdateRequest) => request.put<void>(`/api/admin/books/${id}`, payload),
  remove: (id: number | string) => request.delete<void>(`/api/admin/books/${id}`),
  updateStatus: (id: number | string, payload: BookStatusRequest) =>
    request.put<void>(`/api/admin/books/${id}/status`, payload),
  upload: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post<string>('/api/admin/upload/book-cover', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}

export default adminBookApi
