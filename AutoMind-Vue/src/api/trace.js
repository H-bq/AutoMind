import http from './http'

export const fullTraceByIntent = (intentId) => http.get(`/intent-request/${intentId}/full-trace`)
export const fullTrace = (traceId) => http.get(`/trace/${traceId}`)
