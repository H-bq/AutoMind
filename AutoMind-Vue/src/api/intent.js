import http from './http'

export const submitIntent = ({ inputText, priority }) => http.post('/intent-request/submit', { inputText, priority })
export const listIntents = (query) => http.get('/intent-request/list', { params: query })
export const getIntent = (intentId) => http.get(`/intent-request/${intentId}`)
export const getIntentTrace = (intentId) => http.get(`/intent-request/${intentId}/trace`)
