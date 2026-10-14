import { ROLE } from '@/utils/constants'
import { useUserStore } from '@/store/user'

export function setupGuards(router) {
  router.beforeEach((to) => {
    const userStore = useUserStore()
    const role = to.meta.role || ROLE.AUTH

    if (role === ROLE.PUBLIC) {
      if (userStore.loggedIn && userStore.token && to.path === '/login') {
        return userStore.isAdmin ? '/admin/approvals/pending' : '/tenant/intent/submit'
      }
      return true
    }

    if (!userStore.loggedIn || !userStore.token) {
      userStore.clear()
      return { path: '/login', query: { redirect: to.fullPath } }
    }

    if (role === ROLE.TENANT && userStore.isAdmin) return '/admin/approvals/pending'
    if (role === ROLE.ADMIN && !userStore.isAdmin) return '/403'

    return true
  })
}
