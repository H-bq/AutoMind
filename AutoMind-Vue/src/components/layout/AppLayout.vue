<script setup>
import HeaderBar from './HeaderBar.vue'
import SideMenu from './SideMenu.vue'
</script>

<template>
  <div class="app-layout">
    <SideMenu />
    <div class="workspace">
      <HeaderBar />
      <main class="content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-layout {
  display: flex;
  width: 100%;
  min-height: 100vh;
  background: var(--am-bg);
}

.workspace {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
}

.content {
  flex: 1;
  padding: 20px;
  overflow: auto;
}
</style>
