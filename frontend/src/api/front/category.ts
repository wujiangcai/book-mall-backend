import request from '../request'
import type { CategoryTreeItem } from '../../types/api'

export const frontCategoryApi = {
  list: () => request.get<CategoryTreeItem[]>('/api/front/categories'),
}

export default frontCategoryApi
