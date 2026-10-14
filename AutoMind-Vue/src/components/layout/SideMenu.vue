<script setup>
import * as Icons from '@element-plus/icons-vue'
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { adminMenus, tenantMenus } from '@/router/routes'
import { useUserStore } from '@/store/user'

const route = useRoute()
const userStore = useUserStore()
const menus = computed(() => (userStore.isAdmin ? adminMenus : tenantMenus))
</script>

<template>
  <aside class="side-menu">
    <div class="brand">
      <span class="brand-mark">A</span>
      <div>
        <div class="brand-title">AutoMind</div>
        <div class="brand-subtitle">租户运维平台</div>
      </div>
    </div>

    <ElMenu
      class="menu"
      :default-active="route.path"
      router
      background-color="transparent"
      text-color="#d6deef"
      active-text-color="#ffffff"
    >
      <ElMenuItem v-for="item in menus" :key="item.path" :index="item.path">
        <ElIcon><component :is="Icons[item.icon]" /></ElIcon>
        <span>{{ item.title }}</span>
      </ElMenuItem>
    </ElMenu>
  </aside>
</template>

<style scoped>
.side-menu {
  position: sticky;
  top: 0;
  flex: 0 0 248px;
  width: 248px;
  height: 100vh;
  overflow-y: auto;
  background: var(--am-sidebar);
  color: #fff;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 72px;
  padding: 0 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.brand-mark {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  border-radius: 8px;
  background: #2563eb;
  font-weight: 700;
}

.brand-title {
  font-size: 18px;
  font-weight: 700;
}

.brand-subtitle {
  margin-top: 4px;
  color: #9fb0d0;
  font-size: 12px;
}

.menu {
  border-right: 0;
  padding: 12px;
}

:deep(.el-menu-item) {
  height: 44px;
  border-radius: 6px;
  margin-bottom: 4px;
}

:deep(.el-menu-item.is-active) {
  background: var(--am-sidebar-active);
}
</style>
