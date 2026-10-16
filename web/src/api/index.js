import request from './request'
import { setLogin } from '@/store/user'
import { statusKey } from '@/utils/dict'

/** 去掉空值参数，避免后端把空字符串绑定成非法值 */
function clean(source = {}) {
  const result = {}
  Object.entries(source).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== '') {
      result[key] = value
    }
  })
  return result
}

const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

/* ---------------------------------------------------------------------------
 * 登录与账号
 * ------------------------------------------------------------------------ */

export async function login(payload) {
  const data = await request.post('/auth/login', payload)
  setLogin(data.token, data.user)
  return data.user
}

export function register(payload) {
  return request.post('/auth/register', payload)
}

export function getProfile() {
  return request.get('/user/profile')
}

export function updateProfile(payload) {
  return request.put('/user/profile', payload)
}

export function changePassword(payload) {
  return request.put('/user/password', payload)
}

/** 个人统计：提交数、通过题数、状态分布与最近提交 */
export function getStatistics() {
  return request.get('/user/statistics')
}

/* ---------------------------------------------------------------------------
 * 题目
 * ------------------------------------------------------------------------ */

export function getProblemList(params = {}) {
  return request.get('/problems', { params: clean(params) })
}

export function getProblemDetail(id) {
  return request.get(`/problems/${id}`)
}

export function saveProblem(payload) {
  const body = {
    title: payload.title,
    description: payload.description,
    difficulty: payload.difficulty,
    tags: payload.tags,
    timeLimit: payload.timeLimit,
    memoryLimit: payload.memoryLimit,
  }
  return payload.id
    ? request.put(`/problems/${payload.id}`, body)
    : request.post('/problems', body)
}

export function setProblemEnabled(id, enabled) {
  return request.put(`/problems/${id}/enabled`, null, { params: { enabled } })
}

export async function getProblemCases(problemId) {
  const list = await request.get(`/problems/${problemId}/cases`)
  return list.map((item) => ({
    caseId: item.id,
    input: item.input,
    output: item.output,
    enabled: item.enabled,
  }))
}

export function saveProblemCases(problemId, cases) {
  const body = cases.map((item) => ({
    input: item.input,
    output: item.output,
    enabled: item.enabled,
  }))
  return request.post(`/problems/${problemId}/cases`, body)
}

/** 竞赛选题使用的题目下拉数据 */
export async function getProblemOptions() {
  const data = await getProblemList({ page: 1, size: 100 })
  return data.records.map((item) => ({
    id: item.id,
    title: item.title,
    difficulty: item.difficulty,
  }))
}

/* ---------------------------------------------------------------------------
 * 提交与判题
 * ------------------------------------------------------------------------ */

export function getSubmissionList(params = {}) {
  return request.get('/submissions', { params: clean(params) })
}

export function getSubmissionDetail(id) {
  return request.get(`/submissions/${id}`)
}

/**
 * 提交代码并等待判题结果。
 *
 * 后端目前没有推送通道，前端用轮询提交详情的方式获取进度；
 * 后端接入 WebSocket 后只需替换这里的实现，页面组件不需要改动。
 *
 * @param payload {{ problemId, language, code, contestId }}
 * @param onEvent 依次收到 status、case、done 三类事件
 */
export async function submitCode(payload, onEvent = () => {}) {
  const submissionId = await request.post('/submissions', clean(payload))
  onEvent({ type: 'status', status: 'pending' })

  const deadline = Date.now() + 120000
  let lastStatus = 'pending'
  while (Date.now() < deadline) {
    await delay(700)
    const detail = await getSubmissionDetail(submissionId)
    const key = statusKey(detail.status)
    if (key !== lastStatus) {
      lastStatus = key
      onEvent({ type: 'status', status: key })
    }
    if (key === 'pending' || key === 'judging') {
      continue
    }

    const cases = (detail.cases || []).map((item) => ({ ...item, status: statusKey(item.status) }))
    cases.forEach((item) => onEvent({ type: 'case', result: item }))
    const result = {
      id: submissionId,
      status: key,
      timeUsed: detail.timeUsed,
      passedCases: cases.filter((item) => item.status === 'ac').length,
      totalCases: cases.length,
      errorMessage: detail.errorMessage,
    }
    onEvent({ type: 'done', result })
    return result
  }
  throw new Error('判题超时，请稍后在提交记录中查看结果')
}

/* ---------------------------------------------------------------------------
 * 竞赛与榜单
 * ------------------------------------------------------------------------ */

export function getContestList() {
  return request.get('/contests')
}

export function getContestDetail(id) {
  return request.get(`/contests/${id}`)
}

export function getContestRank(id) {
  return request.get(`/contests/${id}/rank`)
}

export function saveContest(payload) {
  const body = {
    title: payload.title,
    description: payload.description,
    startTime: payload.startTime,
    endTime: payload.endTime,
    problemIds: payload.problemIds,
    hideProblems: payload.hideProblems,
  }
  return payload.id
    ? request.put(`/contests/${payload.id}`, body)
    : request.post('/contests', body)
}

export function getGlobalRank(limit = 100) {
  return request.get('/ranks/global', { params: { limit } })
}

/* ---------------------------------------------------------------------------
 * 后台管理
 * ------------------------------------------------------------------------ */

export function getUserList(params = {}) {
  return request.get('/users', { params: clean(params) })
}

export function updateUserStatus(id, status) {
  return request.put(`/users/${id}/status`, null, { params: { status } })
}

export function resetUserPassword(id) {
  return request.put(`/users/${id}/password/reset`)
}

export function createUser(payload) {
  return request.post('/users', payload)
}

export function deleteUser(id) {
  return request.delete(`/users/${id}`)
}

export function deleteProblem(id) {
  return request.delete(`/problems/${id}`)
}

export function deleteContest(id) {
  return request.delete(`/contests/${id}`)
}

export function getAnnouncementList() {
  return request.get('/announcements')
}

export function saveAnnouncement(payload) {
  const body = { title: payload.title, content: payload.content }
  return payload.id
    ? request.put(`/announcements/${payload.id}`, body)
    : request.post('/announcements', body)
}

export function deleteAnnouncement(id) {
  return request.delete(`/announcements/${id}`)
}

export function getSystemConfig() {
  return request.get('/system/config/list')
}

export function saveSystemConfig(list) {
  return request.put(
    '/system/config',
    list.map((item) => ({ configKey: item.configKey, configValue: item.configValue })),
  )
}

export function getDashboardSummary() {
  return request.get('/dashboard/summary')
}
