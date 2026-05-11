import request from '../request'
import type { RecommendationBook } from '../../types/api'

export type RecommendationParams = {
  sceneBookId?: number | string
  limit?: number
}

export type RecommendationBehavior = {
  bookId: number | string
  behaviorType: 'view' | 'cart' | 'purchase'
}

export const frontRecommendationApi = {
  list: (params?: RecommendationParams) => request.get<RecommendationBook[]>('/api/front/recommendations', { params }),
  record: (data: RecommendationBehavior) => request.post<void>('/api/front/recommendations/behaviors', data),
}

export default frontRecommendationApi
