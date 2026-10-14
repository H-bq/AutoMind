import http from './http'

export const getDashboard = () => http.get('/sla/dashboard')
export const evaluate = (payload) => http.post('/sla/evaluate', payload)
export const listSlaRecords = (query) => http.get('/sla/records', { params: query })

export const createSlaEventSource = (token) => {
  if (!token) return null

  const apiBase = import.meta.env.VITE_API_BASE || '/api'
  const normalizedBase = apiBase.endsWith('/') ? apiBase.slice(0, -1) : apiBase
  const url = `${normalizedBase}/realtime/sla/stream?token=${encodeURIComponent(token)}`

  return new EventSource(url)
}
