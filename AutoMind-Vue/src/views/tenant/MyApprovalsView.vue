<script setup>
import { useRouter } from 'vue-router'
import { listMyProgress } from '@/api/approval'
import DataTable from '@/components/common/DataTable.vue'
import PageContainer from '@/components/common/PageContainer.vue'
import { formatDateTime } from '@/utils/format'

const router = useRouter()

const columns = [
  { prop: 'intentId', label: '意图 ID', width: 100 },
  { prop: 'inputText', label: '提交内容', minWidth: 260 },
  { prop: 'approvalStatus', label: '当前状态', width: 120 },
  { prop: 'decisionReason', label: '原因', minWidth: 220 },
  { prop: 'approvalType', label: '审批类型', width: 150 },
  { prop: 'riskLevel', label: '风险等级', width: 110 },
  { prop: 'intentSubmittedAt', label: '提交时间', width: 180 },
  { prop: 'decidedAt', label: '处理时间', width: 180 },
]

const fetcher = async (query) => listMyProgress(query)

const statusMap = {
  PENDING: { label: '待受理', type: 'warning' },
  APPROVED: { label: '已通过', type: 'success' },
  REJECTED: { label: '拒绝通过', type: 'danger' },
  AUTO_TIMEOUT: { label: '已超时', type: 'info' },
}

function getStatus(row) {
  const status = row.approvalStatus || row.intentStatus
  if (statusMap[status]) return statusMap[status]
  if (status === 'COMPLETED') return { label: '已通过', type: 'success' }
  if (status === 'REJECTED') return { label: '拒绝通过', type: 'danger' }
  return { label: '待受理', type: 'warning' }
}

function displayReason(row) {
  const reason = row.decisionReason
  if (reason) return reason
  return row.approvalStatus === 'APPROVED' ? '审批通过' : '暂无处理原因'
}

function openIntentDetail(intentId) {
  if (!intentId) return
  router.push(`/tenant/intents/${intentId}`)
}
</script>

<template>
  <PageContainer title="我的审批进度" description="查看本人提交意图的受理结果、审批状态与处理原因。">
    <DataTable :columns="columns" :fetcher="fetcher">
      <template #intentId="{ row }">
        <ElButton link type="primary" @click="openIntentDetail(row.intentId)">#{{ row.intentId }}</ElButton>
      </template>
      <template #approvalStatus="{ row }">
        <ElTag :type="getStatus(row).type" effect="plain">
          {{ getStatus(row).label }}
        </ElTag>
      </template>
      <template #decisionReason="{ row }">
        <span class="reason-text">{{ displayReason(row) }}</span>
      </template>
      <template #approvalType="{ row }">
        <ElTag v-if="row.approvalType" type="info" effect="plain">{{ row.approvalType }}</ElTag>
        <span v-else>-</span>
      </template>
      <template #riskLevel="{ row }">
        <ElTag v-if="row.riskLevel" :type="row.riskLevel === 'HIGH' ? 'danger' : row.riskLevel === 'MEDIUM' ? 'warning' : 'success'" effect="plain">
          {{ row.riskLevel }}
        </ElTag>
        <span v-else>-</span>
      </template>
      <template #intentSubmittedAt="{ row }">
        {{ formatDateTime(row.intentSubmittedAt) }}
      </template>
      <template #decidedAt="{ row }">
        {{ formatDateTime(row.decidedAt) }}
      </template>
    </DataTable>
  </PageContainer>
</template>

<style scoped>
.reason-text {
  color: #475569;
  line-height: 1.5;
}
</style>
