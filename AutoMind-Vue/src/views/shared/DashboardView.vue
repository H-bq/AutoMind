<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getTopologyOverview } from '@/api/topology'
import PageContainer from '@/components/common/PageContainer.vue'
import SlaGauge from '@/components/sla/SlaGauge.vue'
import SlaTrendChart from '@/components/sla/SlaTrendChart.vue'
import TopologyGraph from '@/components/topology/TopologyGraph.vue'
import { formatPercent } from '@/utils/format'

const router = useRouter()
const loading = ref(false)
const overview = ref({
  domainCount: 0,
  nodeCount: 0,
  linkCount: 0,
  activeLinkCount: 0,
  anomalyLinkCount: 0,
  totalBandwidthMbps: 0,
  reservedBandwidthTotalMbps: 0,
  bandwidthReservationRate: 0,
  topology: {
    domains: [],
    nodes: [],
    links: [],
    linkResources: [],
    dataIssues: [],
  },
  sla: {
    overallComplianceRate: null,
    metricCompliance: [],
    breachTrend: [],
  },
  anomalyLinks: [],
})

const topology = computed(() => overview.value?.topology || {})
const sla = computed(() => overview.value?.sla || {})
const issueCount = computed(() => topology.value?.dataIssues?.length || 0)
const resourcesByLink = computed(() => {
  const map = new Map()
  ;(topology.value?.linkResources || []).forEach((resource) => {
    if (resource?.linkId) map.set(resource.linkId, resource)
  })
  return map
})

const linkRows = computed(() => (topology.value?.links || []).map((link) => {
  const resource = resourcesByLink.value.get(link.id)
  return {
    ...link,
    resource,
    displayName: link.linkName || link.linkCode || link.id,
    currentLatencyMs: resource?.currentLatencyMs,
    currentPacketLossRate: resource?.currentPacketLossRate,
    currentJitterMs: resource?.currentJitterMs,
    isAnomaly: Boolean(resource?.isAnomaly),
  }
}))

const metricCards = computed(() => [
  { label: '网络域', value: overview.value.domainCount, tone: 'primary' },
  { label: '节点', value: overview.value.nodeCount, tone: 'success' },
  { label: '链路', value: overview.value.linkCount, tone: 'primary' },
  { label: '异常链路', value: overview.value.anomalyLinkCount, tone: overview.value.anomalyLinkCount ? 'danger' : 'success' },
  { label: '运行链路', value: overview.value.activeLinkCount, tone: 'success' },
  { label: '数据问题', value: issueCount.value, tone: issueCount.value ? 'warning' : 'success' },
])

const fetchOverview = async () => {
  loading.value = true
  try {
    overview.value = await getTopologyOverview()
  } finally {
    loading.value = false
  }
}

const openLink = (link) => {
  const linkId = link?.id || link?.linkId
  if (linkId) router.push(`/topology/link/${linkId}`)
}

const linkStatusType = (row) => {
  if (row?.isAnomaly) return 'warning'
  const status = String(row?.status || '').toUpperCase()
  if (status.includes('DOWN') || status.includes('FAIL') || status.includes('ERROR')) return 'danger'
  if (status.includes('WARN') || status.includes('DEGRADED') || status.includes('ANOMALY')) return 'warning'
  return 'success'
}

onMounted(fetchOverview)
</script>

<template>
  <PageContainer title="总览首页" description="聚合当前权限范围内的拓扑、链路健康、SLA 与数据完整性状态。">
    <template #actions>
      <ElButton :loading="loading" @click="fetchOverview">刷新</ElButton>
    </template>

    <div v-loading="loading" class="overview">
      <section class="metric-grid">
        <div v-for="item in metricCards" :key="item.label" class="metric-tile" :class="`metric-${item.tone}`">
          <span>{{ item.label }}</span>
          <strong>{{ item.value ?? 0 }}</strong>
        </div>
      </section>

      <section class="main-grid">
        <div class="panel topology-panel">
          <div class="panel-head">
            <h2>拓扑概览</h2>
            <ElTag v-if="issueCount" type="warning">{{ issueCount }} 个数据问题</ElTag>
          </div>
          <TopologyGraph
            :domains="topology.domains || []"
            :nodes="topology.nodes || []"
            :links="topology.links || []"
            :link-resources="topology.linkResources || []"
            :height="650"
            @link-click="openLink"
          />
        </div>
      </section>

      <section class="split-grid">
        <div class="panel">
          <div class="panel-head">
            <h2>链路数据</h2>
            <ElTag :type="overview.anomalyLinkCount ? 'warning' : 'success'">
              {{ linkRows.length }} 条链路
            </ElTag>
          </div>
          <ElTable class="link-table" :data="linkRows" empty-text="暂无链路数据" @row-click="openLink">
            <ElTableColumn prop="displayName" label="链路" min-width="220" show-overflow-tooltip>
              <template #default="{ row }">
                <strong class="link-name">{{ row.displayName }}</strong>
                <span class="link-code">{{ row.linkCode || '-' }}</span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="linkType" label="类型" width="130" show-overflow-tooltip />
            <ElTableColumn prop="status" label="状态" width="110">
              <template #default="{ row }">
                <ElTag :type="linkStatusType(row)" size="small">
                  {{ row.isAnomaly ? 'ANOMALY' : row.status || '-' }}
                </ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="currentLatencyMs" label="时延 ms" width="105">
              <template #default="{ row }">{{ row.currentLatencyMs ?? '-' }}</template>
            </ElTableColumn>
            <ElTableColumn prop="currentPacketLossRate" label="丢包率" width="110">
              <template #default="{ row }">{{ formatPercent(row.currentPacketLossRate) }}</template>
            </ElTableColumn>
            <ElTableColumn label="带宽 Mbps" width="140">
              <template #default="{ row }">
                {{ row.reservedBandwidthMbps ?? 0 }} / {{ row.totalBandwidthMbps ?? 0 }}
              </template>
            </ElTableColumn>
            <ElTableColumn label="操作" width="96" fixed="right">
              <template #default="{ row }">
                <ElButton link type="primary" @click.stop="openLink(row)">详情</ElButton>
              </template>
            </ElTableColumn>
          </ElTable>
        </div>

        <div class="panel sla-panel">
          <div class="panel-head">
            <h2>SLA 近 24 小时</h2>
            <strong>{{ formatPercent(sla.overallComplianceRate) }}</strong>
          </div>
          <SlaGauge :value="sla.overallComplianceRate" />
          <SlaTrendChart :points="sla.breachTrend || []" />
        </div>
      </section>
    </div>
  </PageContainer>
</template>

<style scoped>
.overview {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(120px, 1fr));
  gap: 12px;
}

.metric-tile,
.panel {
  border: 1px solid var(--am-border);
  border-radius: 8px;
  background: #fff;
}

.metric-tile {
  display: flex;
  min-height: 92px;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
  padding: 16px;
}

.metric-tile span {
  color: var(--am-muted);
}

.metric-tile strong {
  font-size: 30px;
  line-height: 1;
}

.metric-primary strong {
  color: var(--am-primary);
}

.metric-success strong {
  color: var(--am-success);
}

.metric-warning strong {
  color: var(--am-warning);
}

.metric-danger strong {
  color: var(--am-danger);
}

.main-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 16px;
}

.split-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.panel {
  min-width: 0;
  padding: 16px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.panel-head h2 {
  margin: 0;
  font-size: 16px;
}

.topology-panel :deep(.topology-graph) {
  border: 0;
  padding: 0;
}

.link-table :deep(.el-table__row) {
  cursor: pointer;
}

.link-name,
.link-code {
  display: block;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.link-code {
  margin-top: 4px;
  color: var(--am-muted);
  font-size: 12px;
}

.sla-panel {
  display: flex;
  min-height: 420px;
  flex-direction: column;
}

@media (max-width: 1180px) {
  .metric-grid {
    grid-template-columns: repeat(3, minmax(120px, 1fr));
  }

  .main-grid,
  .split-grid {
    grid-template-columns: 1fr;
  }
}
</style>
