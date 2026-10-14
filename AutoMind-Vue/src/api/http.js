import axios from 'axios'
import router from '@/router'
import { useNotificationStore } from '@/store/notification'
import { useTraceStore } from '@/store/trace'
import { useUserStore } from '@/store/user'
import { ApiError } from '@/utils/errors'
import { extractTraceId, getTraceHeaderName } from '@/utils/traceId'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 30000,
})

http.interceptors.request.use((config) => {
  const userStore = useUserStore()
  config.headers = config.headers || {}

  if (userStore.token) config.headers.Authorization = `Bearer ${userStore.token}`
  if (config.traceId) config.headers[getTraceHeaderName()] = config.traceId

  return config
})

http.interceptors.response.use(
  (response) => {
    const traceStore = useTraceStore()
    const traceId = extractTraceId(response.headers) || response.data?.traceId
    traceStore.setTraceId(traceId)

    if (response.status === 202) return response.data?.data ?? response.data

    const body = response.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 200) return body.data
      throw new ApiError(body.code, body.msg || body.message, body.traceId || traceId, body)
    }

    return body
  },
  (error) => {
    const userStore = useUserStore()
    const notificationStore = useNotificationStore()
    const response = error.response
    const traceId = extractTraceId(response?.headers) || response?.data?.traceId
    const code = response?.data?.code || response?.status || 'NETWORK_ERROR'
    const message = response?.data?.msg || response?.data?.message || error.message || '请求失败'
    const apiError = new ApiError(code, message, traceId, response?.data)

    if (response?.status === 401) {
      userStore.clear()
      if (router.currentRoute.value.path !== '/login') router.push('/login')
    } else {
      notificationStore.error(apiError)
    }

    return Promise.reject(apiError)
  },
)

export default http
