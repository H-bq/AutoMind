<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
  items: {
    type: Array,
    default: () => [],
  },
})
</script>

<template>
  <div class="feature-stub">
    <ElAlert
      :title="`${title} 页面骨架已接入`"
      type="info"
      :closable="false"
      show-icon
      description="当前阶段先完成导航、权限、接口契约与页面容器；具体业务交互可在后端接口稳定后逐步替换占位内容。"
    />
    <div class="stub-grid">
      <div v-for="item in items" :key="item.label" class="stub-item">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </div>
    </div>
    <slot />
  </div>
</template>

<style scoped>
.feature-stub {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stub-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.stub-item {
  padding: 16px;
  border: 1px solid var(--am-border);
  border-radius: 8px;
  background: #fff;
}

.stub-item span {
  display: block;
  color: var(--am-muted);
  font-size: 13px;
}

.stub-item strong {
  display: block;
  margin-top: 8px;
  color: var(--am-text);
  font-size: 22px;
}
</style>
