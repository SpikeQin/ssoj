/**
 * 判题状态与难度的字典。
 * 后端返回的状态码为数字，这里统一转换成前端使用的短名与展示文案。
 */

const JUDGE_STATUS = {
  0: 'pending',
  1: 'judging',
  2: 'ac',
  3: 'wa',
  4: 'tle',
  5: 're',
  6: 'ce',
  7: 'se',
}

const STATUS_TEXT = {
  pending: 'Pending',
  judging: 'Judging',
  ac: 'Accepted',
  wa: 'Wrong Answer',
  tle: 'Time Limit Exceeded',
  re: 'Runtime Error',
  ce: 'Compile Error',
  se: 'System Error',
}

export const difficultyText = {
  1: '简单',
  2: '中等',
  3: '困难',
}

/** 把状态码转换成短名，已是短名时原样返回 */
export function statusKey(status) {
  if (typeof status === 'string') {
    return status
  }
  return JUDGE_STATUS[status] || 'pending'
}

export function statusText(status) {
  return STATUS_TEXT[statusKey(status)] || '未知状态'
}
