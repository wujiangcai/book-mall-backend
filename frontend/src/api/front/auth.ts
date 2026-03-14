import request from '../request'

export const frontAuthApi = {
  register: (payload: unknown) => request.post('/api/front/auth/register', payload),
  login: (payload: unknown) => request.post('/api/front/auth/login', payload),
}

export default frontAuthApi
