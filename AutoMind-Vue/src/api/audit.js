import http from './http'

export const listAuditLogs = (query) => http.get('/audit-log/list', { params: query })
export const getAuditLog = (id) => http.get(`/audit-log/${id}`)
