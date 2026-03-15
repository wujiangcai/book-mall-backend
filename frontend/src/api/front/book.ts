import request from '../request'
import type { BookDetail, BookListItem, PageResult } from '../../types/api'

export type BookListParams = {
  page?: number
  pageSize?: number
  categoryId?: number
  keyword?: string
  minPrice?: number
  maxPrice?: number
}

export const frontBookApi = {
  list: (params?: BookListParams) => request.get<PageResult<BookListItem>>('/api/front/books', { params }),
  detail: (id: number | string) => request.get<BookDetail>(`/api/front/books/${id}`),
}

export default frontBookApi
