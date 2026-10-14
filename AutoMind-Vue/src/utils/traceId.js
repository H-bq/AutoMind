export function getTraceHeaderName() {
  return import.meta.env.VITE_TRACE_HEADER || 'X-Trace-Id'
}

export function extractTraceId(headers = {}) {
  const headerName = getTraceHeaderName().toLowerCase()
  return headers[headerName] || headers['x-trace-id'] || ''
}
