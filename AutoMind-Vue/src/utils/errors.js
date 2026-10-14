export class ApiError extends Error {
  constructor(code, message, traceId, details) {
    super(message || '请求失败')
    this.name = 'ApiError'
    this.code = code
    this.traceId = traceId
    this.details = details
  }
}

export function normalizeError(error) {
  if (error instanceof ApiError) return error
  return new ApiError(error?.code || 'UNKNOWN', error?.message || '请求失败', error?.traceId, error)
}
