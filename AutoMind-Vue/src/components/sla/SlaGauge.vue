<script setup>
import { formatPercent } from '@/utils/format'

defineProps({
  value: {
    type: Number,
    default: null,
  },
})
</script>

<template>
  <div class="sla-gauge">
    <span>达标率</span>
    <strong>{{ formatPercent(value) }}</strong>
  </div>
</template>

<style scoped>
.sla-gauge {
  display: grid;
  width: 220px;
  height: 140px;
  place-items: center;
  border: 1px solid var(--am-border);
  border-radius: 8px;
  background: #fff;
}

.sla-gauge span,
.sla-gauge strong {
  display: block;
}

.sla-gauge strong {
  color: var(--am-primary);
  font-size: 34px;
}
</style>
