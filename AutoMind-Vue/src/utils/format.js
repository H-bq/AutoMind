import dayjs from 'dayjs'

export function formatDateTime(value) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-'
}

export function formatPercent(value) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) return '-'
  const numeric = Number(value)
  return `${(Math.abs(numeric) <= 1 ? numeric * 100 : numeric).toFixed(2)}%`
}

export function formatLatency(value) {
  return value === null || value === undefined ? '-' : `${value} ms`
}
