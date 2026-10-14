<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getAgentTimelineByIntent, getAgentTimelineByTrace } from '@/api/agent'
import FullTraceTimeline from '@/components/trace/FullTraceTimeline.vue'
import PageContainer from '@/components/common/PageContainer.vue'

const loading = ref(false)
const route = useRoute()
const form = reactive({
  keyword: '',
})
const trace = ref({ events: [] })

function searchRouteIntent() {
  const intentId = route.params.intentId
  if (!intentId) return
  form.keyword = String(intentId)
  search()
}

async function search() {
  const keyword = form.keyword.trim()
  if (!keyword) {
    trace.value = { events: [] }
    return
  }

  loading.value = true
  try {
    if (/^\d+$/.test(keyword)) {
      trace.value = await getAgentTimelineByIntent(keyword)
    } else {
      trace.value = await getAgentTimelineByTrace(keyword)
    }
  } finally {
    loading.value = false
  }
}

onMounted(searchRouteIntent)
watch(() => route.params.intentId, searchRouteIntent)
</script>

<template>
  <PageContainer title="Agent 执行时间线" description="输入关联任务标识或 intentId 查询 Agent 决策、审批、MCP、A2A 与 SLA 验证时间线。">
    <template #actions>
      <ElInput v-model="form.keyword" placeholder="intentId 或关联标识" clearable style="width: 320px" @keyup.enter="search" />
      <ElButton type="primary" :loading="loading" @click="search">查询</ElButton>
    </template>
    <FullTraceTimeline v-loading="loading" :trace="trace" />
  </PageContainer>
</template>
