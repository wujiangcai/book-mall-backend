import request from '../request'

export const frontBannerApi = {
  list: () => request.get('/api/front/banners'),
}

export default frontBannerApi
