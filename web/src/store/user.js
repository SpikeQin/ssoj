import { reactive } from 'vue'

/**
 * 简单的登录状态存储：令牌与用户信息保存在 localStorage，
 * 刷新页面后仍然保持登录，后续接入后端时改为登录接口返回即可。
 */
const state = reactive({
  token: localStorage.getItem('token') || '',
  user: JSON.parse(localStorage.getItem('user') || 'null'),
})

export function setLogin(token, user) {
  state.token = token
  state.user = user
  localStorage.setItem('token', token)
  localStorage.setItem('user', JSON.stringify(user))
}

export function clearLogin() {
  state.token = ''
  state.user = null
  localStorage.removeItem('token')
  localStorage.removeItem('user')
}

export function useLoginState() {
  return state
}

export function isAdmin() {
  return state.user?.role === 1
}
