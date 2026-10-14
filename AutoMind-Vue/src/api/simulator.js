import http from './http'

export const generateAnomaly = (data, adminToken) => {
  const headers = {}
  const token = adminToken || import.meta.env.VITE_SIMULATOR_ADMIN_TOKEN || 'automind-simulator-admin-dev-token'
  if (token) {
    headers['X-Simulator-Admin-Token'] = token
  }
  return http.post('/simulator/anomaly/generate', data || {}, { headers })
}
