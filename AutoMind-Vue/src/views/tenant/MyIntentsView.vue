<script setup>
import { useRouter } from 'vue-router'
import { listIntents } from '@/api/intent'
import DataTable from '@/components/common/DataTable.vue'
import PageContainer from '@/components/common/PageContainer.vue'

const router = useRouter()

const columns = [
  { prop: 'id', label: 'ID', width: 100 },
  { prop: 'inputText', label: '输入文本', minWidth: 260 },
  { prop: 'parsedIntent', label: '解析意图', minWidth: 220 },
  { prop: 'priority', label: '优先级', width: 100 },
  { prop: 'createTime', label: '创建时间', width: 180 },
]

const fetcher = async () => listIntents()

const handleRowClick = (row) => {
  router.push(`/tenant/intents/${row.id}`)
}
</script>

<template>
  <PageContainer title="我的意图列表" description="展示当前账号可见的意图请求记录。">
    <DataTable :columns="columns" :fetcher="fetcher" row-clickable @row-click="handleRowClick">
      <template #id="{ row }">
        <ElButton link type="primary" @click.stop="router.push(`/tenant/intents/${row.id}`)">{{ row.id }}</ElButton>
      </template>
    </DataTable>
  </PageContainer>
</template>
