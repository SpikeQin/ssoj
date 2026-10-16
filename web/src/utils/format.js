/** 日期时间格式化：后端返回 ISO 字符串（2026-10-16T11:44:46） */

function pad(value) {
  return String(value).padStart(2, '0')
}

function parse(value) {
  if (!value) {
    return null
  }
  if (value instanceof Date) {
    return value
  }
  return new Date(String(value).replace(' ', 'T'))
}

/** 2026-10-16 11:44:46 */
export function formatDateTime(value) {
  const date = parse(value)
  if (!date || Number.isNaN(date.getTime())) {
    return value || '—'
  }
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ` +
    `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

/** 10-16 11:44 */
export function formatShortDateTime(value) {
  const date = parse(value)
  if (!date || Number.isNaN(date.getTime())) {
    return value || '—'
  }
  return `${pad(date.getMonth() + 1)}-${pad(date.getDate())} ` +
    `${pad(date.getHours())}:${pad(date.getMinutes())}`
}

/**
 * 竞赛时间范围：同一天写作 2026-10-18 13:30:00 ~ 17:30:00，
 * 跨天时结束时间带上日期，避免只剩时分秒看不出是哪天结束
 */
export function formatTimeRange(start, end) {
  if (!start || !end) {
    return '—'
  }
  const from = formatDateTime(start)
  const to = formatDateTime(end)
  const sameDay = from.slice(0, 10) === to.slice(0, 10)
  return `${from} ~ ${sameDay ? to.slice(11) : to}`
}
