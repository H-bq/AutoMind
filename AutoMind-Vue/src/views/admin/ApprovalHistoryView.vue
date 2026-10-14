<script setup>
import { useRouter } from 'vue-router'
import { listHistory } from '@/api/approval'
import DataTable from '@/components/common/DataTable.vue'
import PageContainer from '@/components/common/PageContainer.vue'

const router = useRouter()

const columns = [
  { prop: 'id', label: 'ID', width: 90 },
  { prop: 'approvalType', label: '审批类型', minWidth: 160 },
  { prop: 'riskLevel', label: '风险等级', width: 110 },
  { prop: 'status', label: '状态', width: 110 },
  { prop: 'tenantId', label: '租户', minWidth: 140 },
  { prop: 'submittedAt', label: '提交时间', width: 180 },
  { prop: 'decidedAt', label: '决策时间', width: 180 },
]

const fetcher = (query) => listHistory(query)

function openDetail(row) {
  if (row?.id) router.push(`/admin/approvals/${row.id}`)
}
</script>

<template>
  <PageContainer title="审批历史" description="展示已处理审批记录，点击行查看完整审批详情。">
    <DataTable :columns="columns" :fetcher="fetcher" row-clickable @row-click="openDetail" />
  </PageContainer>
</template>
