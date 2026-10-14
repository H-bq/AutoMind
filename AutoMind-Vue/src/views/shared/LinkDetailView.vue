<script setup>
import { LineChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import * as echarts from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getLinkStatus } from '@/api/topology'
import PageContainer from '@/components/common/PageContainer.vue'
import { formatDateTime, formatPercent } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref({
  link: null,
  sourceNode: null,
  targetNode: null,
  sourceDomain: null,
  targetDomain: null,
  latestResource: null,
  latestSlaVerdict: null,
  latestTelemetry: null,
  telemetryTrend: [],
  dataIssues: [],
})
const chartRef = ref(null)
let chart = null

echarts.use([GridComponent, LegendComponent, TooltipComponent, LineChart, CanvasRenderer])

const linkId = computed(() => route.params.linkId)
const link = computed(() => detail.value?.link || {})
const sourceNode = computed(() => detail.value?.sourceNode || {})
const targetNode = computed(() => detail.value?.targetNode || {})
const latestResource = computed(() => detail.value?.latestResource || {})
const latestSla = computed(() => detail.value?.latestSlaVerdict || {})
const telemetryTrend = computed(() => detail.value?.telemetryTrend || [])

const linkTitle = computed(() => link.value.linkName || link.value.linkCode || linkId.value)

const statusType = (status, anomaly = false) => {
  if (anomaly) return 'warning'
  const normalized = String(status || '').toUpperCase()
  if (normalized.includes('DOWN') || normalized.includes('FAIL') || normalized.includes('ERROR')) return 'danger'
  if (normalized.includes('WARN') || normalized.includes('DEGRADED') || normalized.includes('ANOMALY')) return 'warning'
  if (normalized.includes('UP') || normalized.includes('ACTIVE') || normalized.includes('ONLINE') || normalized.includes('NORMAL')) return 'success'
  return 'info'
}

const fetchDetail = async () => {
  loading.value = true
  try {
    detail.value = await getLinkStatus(linkId.value)
  } finally {
    loading.value = false
  }
}

const renderChart = async () => {
  await nextTick()
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)

  const points = telemetryTrend.value || []
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 0, data: ['时延', '抖动', '带宽使用率', '丢包率'] },
    grid: { top: 44, left: 42, right: 18, bottom: 32 },
    xAxis: {
      type: 'category',
      data: points.map((item) => formatDateTime(item.time)),
      axisLabel: { hideOverlap: true },
    },
    yAxis: [
      { type: 'value', name: 'ms / %' },
      { type: 'value', name: 'loss', min: 0 },
    ],
    series: [
      { name: '时延', type: 'line', smooth: true, data: points.map((item) => item.latencyMs ?? null) },
      { name: '抖动', type: 'line', smooth: true, data: points.map((item) => item.jitterMs ?? null) },
      { name: '带宽使用率', type: 'line', smooth: true, data: points.map((item) => item.bandwidthUsage ?? null) },
      { name: '丢包率', type: 'line', smooth: true, yAxisIndex: 1, data: points.map((item) => item.packetLossRate ?? null) },
    ],
  })
}

watch(telemetryTrend, renderChart, { deep: true })

const resizeChart = () => chart?.resize()

onMounted(async () => {
  await fetchDetail()
  await renderChart()
  window.addEventListener('resize', resizeChart)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  chart?.dispose()
  chart = null
})
</script>

<template>
  <PageContainer :title="`链路详情：${linkTitle}`" description="展示链路静态信息、端点节点、实时资源、SLA 结论与近 24 小时遥测趋势。">
    <template #actions>
      <ElButton @click="router.back()">返回</ElButton>
      <ElButton type="primary" :loading="loading" @click="fetchDetail">刷新</ElButton>
    </template>

    <div v-loading="loading" class="detail-layout">
      <section class="summary-grid">
        <div class="summary-tile">
          <span>链路状态</span>
          <ElTag :type="statusType(link.status, latestResource.isAnomaly)">{{ latestResource.isAnomaly ? 'ANOMALY' : link.status || '-' }}</ElTag>
        </div>
        <div class="summary-tile">
          <span>当前时延</span>
          <strong>{{ latestResource.currentLatencyMs ?? detail.latestTelemetry?.latencyMs ?? '-' }} ms</strong>
        </div>
        <div class="summary-tile">
          <span>丢包率</span>
          <strong>{{ formatPercent(latestResource.currentPacketLossRate ?? detail.latestTelemetry?.packetLossRate) }}</strong>
        </div>
        <div class="summary-tile">
          <span>SLA</span>
          <ElTag :type="latestSla.breachFlag ? 'danger' : 'success'">{{ latestSla.breachFlag ? '违约' : '正常' }}</ElTag>
        </div>
      </section>

      <section class="panel">
        <div class="panel-head">
          <h2>基础信息</h2>
        </div>
        <ElDescriptions :column="3" border>
          <ElDescriptionsItem label="链路编码">{{ link.linkCode || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="链路名称">{{ link.linkName || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="链路类型">{{ link.linkType || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="源节点">{{ sourceNode.nodeName || sourceNode.nodeCode || link.sourceNodeId || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="目标节点">{{ targetNode.nodeName || targetNode.nodeCode || link.targetNodeId || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="最大时延">{{ link.maxLatencyMs ?? '-' }} ms</ElDescriptionsItem>
          <ElDescriptionsItem label="总带宽">{{ link.totalBandwidthMbps ?? 0 }} Mbps</ElDescriptionsItem>
          <ElDescriptionsItem label="已预留">{{ link.reservedBandwidthMbps ?? 0 }} Mbps</ElDescriptionsItem>
          <ElDescriptionsItem label="资源采样">{{ formatDateTime(latestResource.sampledAt) }}</ElDescriptionsItem>
        </ElDescriptions>
      </section>

      <section class="main-grid">
        <div class="panel">
          <div class="panel-head">
            <h2>遥测趋势</h2>
            <ElTag>{{ telemetryTrend.length }} 点</ElTag>
          </div>
          <div v-if="telemetryTrend.length" ref="chartRef" class="trend-chart" />
          <ElEmpty v-else description="暂无遥测趋势数据" />
        </div>

        <div class="panel">
          <div class="panel-head">
            <h2>SLA 结论</h2>
            <ElTag :type="latestSla.breachFlag ? 'danger' : 'success'">{{ latestSla.breachFlag ? '违约' : '达标' }}</ElTag>
          </div>
          <div class="kv-row"><span>指标</span><strong>{{ latestSla.metricType || '-' }}</strong></div>
          <div class="kv-row"><span>观测值</span><strong>{{ latestSla.observedValue ?? '-' }}</strong></div>
          <div class="kv-row"><span>目标值</span><strong>{{ latestSla.targetValue ?? '-' }}</strong></div>
          <div class="kv-row"><span>评估窗口</span><strong>{{ latestSla.evaluationWindow || '-' }}</strong></div>
          <div class="kv-row"><span>评估时间</span><strong>{{ formatDateTime(latestSla.evaluatedAt) }}</strong></div>
        </div>
      </section>

      <section v-if="detail.dataIssues?.length" class="panel">
        <div class="panel-head">
          <h2>数据完整性</h2>
          <ElTag type="warning">{{ detail.dataIssues.length }}</ElTag>
        </div>
        <ElTable :data="detail.dataIssues" size="small">
          <ElTableColumn prop="severity" label="级别" width="90" />
          <ElTableColumn prop="issueType" label="类型" width="180" />
          <ElTableColumn prop="entityType" label="实体" width="160" />
          <ElTableColumn prop="message" label="说明" min-width="220" />
        </ElTable>
      </section>
    </div>
  </PageContainer>
</template>

<style scoped>
.detail-layout {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(150px, 1fr));
  gap: 12px;
}

.summary-tile,
.panel {
  border: 1px solid var(--am-border);
  border-radius: 8px;
  background: #fff;
}

.summary-tile {
  display: flex;
  min-height: 88px;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
  padding: 16px;
}

.summary-tile span {
  color: var(--am-muted);
}

.summary-tile strong {
  font-size: 24px;
}

.panel {
  min-width: 0;
  padding: 16px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.panel-head h2 {
  margin: 0;
  font-size: 16px;
}

.main-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 16px;
}

.trend-chart {
  width: 100%;
  height: 360px;
}

.kv-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  border-bottom: 1px solid var(--am-border);
  padding: 12px 0;
}

.kv-row span {
  color: var(--am-muted);
}

@media (max-width: 1080px) {
  .summary-grid,
  .main-grid {
    grid-template-columns: 1fr;
  }
}
</style>
