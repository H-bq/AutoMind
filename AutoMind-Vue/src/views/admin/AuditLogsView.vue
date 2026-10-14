<script setup>
import { listAuditLogs } from '@/api/audit'
import DataTable from '@/components/common/DataTable.vue'
import PageContainer from '@/components/common/PageContainer.vue'

const columns = [
  { prop: 'id', label: 'id', width: 100 },
  { prop: 'tenantId', label: 'tenantId', minWidth: 140 },
  { prop: 'actorId', label: 'actorId', width: 120 },
  { prop: 'actorRole', label: 'actorRole', width: 120 },
  { prop: 'action', label: 'action', minWidth: 180 },
  { prop: 'createdAt', label: 'createdAt', width: 180 },
]

const fetcher = (query) => listAuditLogs(query)
</script>

<template>
  <PageContainer title="审计日志" description="查看当前账号权限范围内的审计日志。">
    <DataTable :columns="columns" :fetcher="fetcher" />
  </PageContainer>
</template>
