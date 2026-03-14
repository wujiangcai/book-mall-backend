import request from '../request'

export const adminAuthApi = {
  login: (payload: unknown) => request.post('/api/admin/auth/login', payload),
}

export default adminAuthApi
