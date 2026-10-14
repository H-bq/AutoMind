<script setup>
import { onMounted, ref } from 'vue'
import { getDashboard, listSlaRecords } from '@/api/sla'
import DataTable from '@/components/common/DataTable.vue'
import PageContainer from '@/components/common/PageContainer.vue'
import SlaGauge from '@/components/sla/SlaGauge.vue'
import SlaTrendChart from '@/components/sla/SlaTrendChart.vue'

const dashboard = ref({
  overallComplianceRate: null,
  metricCompliance: [],
  breachTrend: [],
})

const columns = [
  { prop: 'linkId', label: 'linkId', minWidth: 140 },
  { prop: 'metricType', label: 'metricType', width: 140 },
  { prop: 'observedValue', label: 'observedValue', width: 150 },
  { prop: 'targetValue', label: 'targetValue', width: 140 },
  { prop: 'breachFlag', label: 'breachFlag', width: 120 },
  { prop: 'evaluatedAt', label: 'evaluatedAt', width: 180 },
]

const fetcher = (query) => listSlaRecords(query)

onMounted(async () => {
  dashboard.value = await getDashboard()
})
</script>

<template>
  <PageContainer title="我的 SLA 仪表盘" description="展示当前账号可见的 SLA 评估数据。">
    <div class="sla-layout">
      <SlaGauge :value="dashboard?.overallComplianceRate" />
      <SlaTrendChart :points="dashboard?.breachTrend || []" />
    </div>
    <DataTable :columns="columns" :fetcher="fetcher" />
  </PageContainer>
</template>

<style scoped>
.sla-layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 16px;
  margin-bottom: 16px;
}
</style>
