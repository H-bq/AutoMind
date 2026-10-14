import http from './http'

export const login = (payload) => http.post('/user/login', payload)
export const logout = () => http.post('/user/logout')
export const me = () => http.get('/user/me')
