import { defineStore } from 'pinia'

export const useTraceStore = defineStore('trace', {
  state: () => ({
    lastTraceId: '',
  }),
  actions: {
    setTraceId(traceId) {
      if (traceId) this.lastTraceId = traceId
    },
  },
})
