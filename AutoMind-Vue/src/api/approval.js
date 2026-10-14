import http from './http'

export const listPending = (query) => http.get('/approvals/pending', { params: query })
export const listHistory = (query) => http.get('/approvals/history', { params: query })
export const listMyProgress = (query) => http.get('/approvals/my-progress', { params: query })
export const getApproval = (id) => http.get(`/approvals/${id}`)
export const approve = (id, payload) => http.post(`/approvals/${id}/approve`, payload)
export const reject = (id, payload) => http.post(`/approvals/${id}/reject`, payload)
export const rejectWithModification = (id, payload) => http.post(`/approvals/${id}/reject-with-modification`, payload)
