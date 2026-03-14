import axios from 'axios'
import { Message } from '@arco-design/web-vue'

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
    Message.error(message || '请求失败')
    return Promise.reject(new Error(message || '请求失败'))
  },
  (error: any) => {
    const status = error?.response?.status
    const message = error?.response?.data?.message || error?.message || '请求失败'
    if (status === 401) {
      localStorage.removeItem('token')
      Message.error('登录已过期，请重新登录')
      const redirect = encodeURIComponent(window.location.pathname + window.location.search)
      const loginPath = window.location.pathname.startsWith('/admin') ? '/admin/login' : '/login'
      window.location.href = `${loginPath}?redirect=${redirect}`
      return Promise.reject(error)
    }
    Message.error(message)
    return Promise.reject(error)
  }
)

export default request
