import axios from 'axios'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8003',
  timeout: 10000,
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response: any) => {
    const { code, message, data } = response.data || {}
    if (code === 200) return data
    return Promise.reject(new Error(message || '请求失败'))
  },
  (error: any) => {
    const status = error?.response?.status
    if (status === 401) {
      localStorage.removeItem('token')
      // TODO: redirect to login
    }
    return Promise.reject(error)
  }
)

export default request
