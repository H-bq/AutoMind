<script setup>
import { listHealing } from '@/api/healing'
import DataTable from '@/components/common/DataTable.vue'
import HealingChainView from '@/components/healing/HealingChainView.vue'
import PageContainer from '@/components/common/PageContainer.vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'intentId', label: '意图 ID', width: 100 },
  { prop: 'triggerSource', label: '触发来源', width: 140 },
  { prop: 'attemptNo', label: '自愈尝试次数', width: 120 },
  { prop: 'strategy', label: '自愈策略', width: 180 },
  { prop: 'result', label: '自愈结果', width: 150 },
  { prop: 'rootCause', label: '故障原因', minWidth: 220 },
  { prop: 'createdAt', label: '触发时间', width: 180 },
  { prop: 'recoveredAt', label: '恢复时间', width: 180 },
]

const fetcher = (query) => listHealing(query)

function openIntentDetail(intentId) {
  if (!intentId) return
  router.push(`/tenant/intents/${intentId}`)
}

function getTriggerSourceType(source) {
  switch (source) {
    case 'MONITOR': return 'warning'
    case 'MCP_ERROR': return 'danger'
    case 'A2A_FAIL': return 'info'
    default: return ''
  }
}

function getResultType(result) {
  switch (result) {
    case 'SUCCESS': return 'success'
    case 'HANDOFF_HUMAN': return 'danger'
    default: return 'warning'
  }
}
</script>

<template>
  <PageContainer title="我的自愈记录" description="查看当前租户内触发的自愈重规划历史与执行结果。">
    <HealingChainView />
    <DataTable :columns="columns" :fetcher="fetcher">
      <template #intentId="{ row }">
        <ElButton type="primary" link @click="openIntentDetail(row.intentId)">
          #{{ row.intentId }}
        </ElButton>
      </template>

      <template #triggerSource="{ row }">
        <ElTag :type="getTriggerSourceType(row.triggerSource)" effect="plain">
          {{ row.triggerSource }}
        </ElTag>
      </template>

      <template #attemptNo="{ row }">
        <span>{{ row.attemptNo }} / {{ row.maxAttempts }}</span>
      </template>

      <template #strategy="{ row }">
        <ElTag type="info" size="small">{{ row.strategy }}</ElTag>
      </template>

      <template #result="{ row }">
        <ElTag :type="getResultType(row.result)">
          {{ row.result }}
        </ElTag>
      </template>
    </DataTable>
  </PageContainer>
</template>

