import http from './http'

export const listAgentTasks = (query) => http.get('/agent-tasks', { params: query })
export const getAgentTask = (id) => http.get(`/agent-tasks/${id}`)
export const getByTrace = (traceId) => http.get(`/agent-tasks/by-trace/${traceId}`)
export const getAgentTasksByIntent = (intentId) => http.get(`/agent-tasks/by-intent/${intentId}`)
export const getAgentTimelineByIntent = (intentId) => http.get(`/agent-tasks/timeline/by-intent/${intentId}`)
export const getAgentTimelineByTrace = (traceId) => http.get(`/agent-tasks/timeline/by-trace/${traceId}`)
