import http from './http'

export const listMcpCallLogs = (query) => http.get('/mcp-call-logs', { params: query })
export const getMcpCallLog = (id) => http.get(`/mcp-call-logs/${id}`)
export const getMcpByTrace = (traceId) => http.get(`/mcp-call-logs/by-trace/${traceId}`)
