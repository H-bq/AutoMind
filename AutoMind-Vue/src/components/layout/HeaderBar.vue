<script setup>
import { useRoute, useRouter } from 'vue-router'
import RoleBadge from './RoleBadge.vue'
import TraceIdTag from '@/components/common/TraceIdTag.vue'
import { useTraceStore } from '@/store/trace'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const traceStore = useTraceStore()

function logout() {
  userStore.clear()
  router.push('/login')
}
</script>

<template>
  <header class="header-bar">
    <div class="breadcrumb-container">
      <ElBreadcrumb separator="/">
        <ElBreadcrumbItem :to="{ path: '/' }">AutoMind</ElBreadcrumbItem>
        <ElBreadcrumbItem v-slot:default v-if="route.meta.title">{{ route.meta.title }}</ElBreadcrumbItem>
      </ElBreadcrumb>
    </div>

    <div class="header-actions">
      <TraceIdTag v-if="traceStore.lastTraceId" :trace-id="traceStore.lastTraceId" />
      <ElTag effect="plain">{{ userStore.position || '-' }}</ElTag>
      <RoleBadge :role="userStore.displayRole" />
      <span class="username">{{ userStore.name || userStore.username || userStore.userId }}</span>
      <ElButton size="small" @click="logout">退出</ElButton>
    </div>
  </header>
</template>

<style scoped>
.header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 72px;
  padding: 0 24px;
  border-bottom: 1px solid var(--am-border);
  background: #fff;
}

.breadcrumb-container {
  display: flex;
  align-items: center;
}


.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  color: var(--am-muted);
  font-size: 14px;
}
</style>
