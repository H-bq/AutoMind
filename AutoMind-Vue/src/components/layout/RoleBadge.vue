<script setup>
defineProps({
  role: {
    type: String,
    default: 'TENANT',
  },
})
</script>

<template>
  <ElTag :type="role === 'ADMIN' ? 'danger' : 'primary'" effect="light">
    {{ role === 'ADMIN' ? 'admin' : 'tenant' }}
  </ElTag>
</template>
