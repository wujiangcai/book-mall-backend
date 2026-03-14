import request from '../request'
import type { Banner } from '../../types/api'

export const frontBannerApi = {
  list: () => request.get<Banner[]>('/api/front/banners'),
}

export default frontBannerApi
