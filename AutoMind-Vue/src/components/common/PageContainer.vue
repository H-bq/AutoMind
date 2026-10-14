<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    default: '',
  },
})
</script>

<template>
  <section class="page-container">
    <div class="page-head">
      <div>
        <h1>{{ title }}</h1>
        <p v-if="description">{{ description }}</p>
      </div>
      <div class="actions">
        <slot name="actions" />
      </div>
    </div>
    <slot />
  </section>
</template>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

h1 {
  margin: 0;
  font-size: 22px;
  line-height: 1.35;
}

p {
  margin: 6px 0 0;
  color: var(--am-muted);
}

.actions {
  display: flex;
  gap: 8px;
}
</style>
