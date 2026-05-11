import request from '../request'
import type { AdminAiAnalysis } from '../../types/api'

export const adminAiAnalysisApi = {
  get: () => request.get<AdminAiAnalysis>('/api/admin/ai-analysis', { timeout: 90000 }),
}

export default adminAiAnalysisApi
