import axios from 'axios'
import { Message } from '@arco-design/web-vue'

// Axios 二次封装：统一 baseURL、自动带 Token、统一处理后端 Result 结构与错误提示。
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8003',
  timeout: 10000,
})

request.interceptors.request.use((config) => {
  // 本项目把 token 存在 localStorage 中，每次请求自动带到 Authorization 头里。
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = token
  }
  return config
})

request.interceptors.response.use(
  (response: any) => {
    // 支付接口会直接返回 HTML 表单，所以这里对 text/html 做特殊兼容。
    const contentType = response?.headers?.['content-type'] || ''
    if (contentType.includes('text/html')) return response.data
    const { code, message, data } = response.data || {}
    if (code === 200) return data
    Message.error(message || '请求失败')
    return Promise.reject(new Error(message || '请求失败'))
  },
  (error: any) => {
    const status = error?.response?.status
    const message = error?.response?.data?.message || error?.message || '请求失败'
    if (status === 401) {
      // Token 失效时清空登录态并跳回对应登录页。
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
