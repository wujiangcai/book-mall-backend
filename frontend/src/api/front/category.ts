import request from '../request'

export const frontCategoryApi = {
  list: () => request.get('/api/front/categories'),
}

export default frontCategoryApi
