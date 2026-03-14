import request from '../request'

export const frontUserApi = {
  getInfo: () => request.get('/api/front/user/info'),
  updateInfo: (payload: unknown) => request.put('/api/front/user/info', payload),
}

export default frontUserApi
