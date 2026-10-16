import axios from 'axios'

/**
 * 统一请求封装：后端接口统一前缀 /api，响应体格式为 { code, message, data }
 * 开发期由 vite.config.js 代理到 Spring Boot 的 8080 端口
 */
const request = axios.create({
  baseURL: '/api',
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
  (response) => {
    const body = response.data
    if (body.code === 0) {
      return body.data
    }
    return Promise.reject(new Error(body.message || '请求失败'))
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      // 令牌失效：清除本地登录状态并回到登录页
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      if (!window.location.pathname.startsWith('/login')) {
        const redirect = encodeURIComponent(window.location.pathname + window.location.search)
        window.location.href = `/login?redirect=${redirect}`
      }
      return Promise.reject(new Error('登录状态已过期，请重新登录'))
    }
    if (status === 403) {
      return Promise.reject(new Error('没有权限执行该操作'))
    }
    return Promise.reject(new Error(error.response?.data?.message || error.message || '网络异常'))
  },
)

export default request
