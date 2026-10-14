import http from './http'

export const listA2aMessages = (query) => http.get('/a2a-messages', { params: query })
export const getA2aMessage = (id) => http.get(`/a2a-messages/${id}`)
export const getA2aByTrace = (traceId) => http.get(`/a2a-messages/by-trace/${traceId}`)
