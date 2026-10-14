import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    userId: null,
    username: null,
    tenantId: null,
    role: null,
    name: null,
    position: null,
    token: null,
    loggedIn: false,
  }),
  getters: {
    isAdmin: (state) => state.tenantId === 'admin',
    displayRole: (state) => (state.tenantId === 'admin' ? 'ADMIN' : state.role || 'TENANT'),
  },
  actions: {
    setSession(payload) {
      this.userId = payload.userId
      this.username = payload.username
      this.tenantId = payload.tenantId
      this.role = payload.role || (payload.tenantId === 'admin' ? 'ADMIN' : 'TENANT')
      this.name = payload.name || null
      this.position = payload.position || null
      this.token = payload.token || null
      this.loggedIn = Boolean(this.token)
    },
    clear() {
      this.$reset()
    },
  },
  persist: {
    key: 'automind.session',
    storage: localStorage,
  },
})
