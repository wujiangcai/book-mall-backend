import request from '../request'
import type { AdminDashboard } from '../../types/api'

export const adminDashboardApi = {
  get: () => request.get<AdminDashboard>('/api/admin/dashboard'),
}

export default adminDashboardApi
