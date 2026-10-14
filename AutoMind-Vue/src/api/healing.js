import http from './http'

export const listHealing = (query) => http.get('/healing-records', { params: query })
export const getHealing = (id) => http.get(`/healing-records/${id}`)
export const getHealingByIntent = (intentId) => http.get(`/healing-records/by-intent/${intentId}`)
