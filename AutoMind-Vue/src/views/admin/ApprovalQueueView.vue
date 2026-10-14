<script setup>
import { useRouter } from 'vue-router'
import { listPending } from '@/api/approval'
import DataTable from '@/components/common/DataTable.vue'
import PageContainer from '@/components/common/PageContainer.vue'

const router = useRouter()

const columns = [
  { prop: 'id', label: 'ID', width: 90 },
  { prop: 'approvalType', label: '审批类型', minWidth: 160 },
  { prop: 'riskLevel', label: '风险等级', width: 110 },
  { prop: 'tenantId', label: '租户', minWidth: 140 },
  { prop: 'submittedAt', label: '提交时间', width: 180 },
  { prop: 'expireAt', label: '过期时间', width: 180 },
]

const fetcher = (query) => listPending(query)

function openDetail(row) {
  if (row?.id) router.push(`/admin/approvals/${row.id}`)
}
</script>

<template>
  <PageContainer title="待审批队列" description="展示待处理审批关键字段，点击行进入详情页处理。">
    <DataTable :columns="columns" :fetcher="fetcher" row-clickable @row-click="openDetail" />
  </PageContainer>
</template>
