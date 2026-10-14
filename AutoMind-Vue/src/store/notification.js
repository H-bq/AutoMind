import { defineStore } from 'pinia'
import { ElMessage, ElNotification } from 'element-plus'

function friendlyErrorMessage(error, fallback) {
  const rawMessage = error?.message || fallback
  const message = String(rawMessage)
    .replace(/\s*traceId\s*[:：]\s*[0-9a-f-]+/gi, '')
    .replace(/\s*trace_id\s*[:：]\s*[0-9a-f-]+/gi, '')
    .trim()

  return message || fallback
}

export const useNotificationStore = defineStore('notification', {
  actions: {
    success(message) {
      ElMessage.success(message)
    },
    info(message) {
      ElMessage.info(message)
    },
    error(error, fallback = '操作失败') {
      const message = friendlyErrorMessage(error, fallback)
      ElNotification.error({
        title: '请求异常',
        message,
        duration: 5000,
      })
    },
  },
})
