<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { ElMessageBox, ElNotification } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import { createSlaEventSource, getDashboard, listSlaRecords } from '@/api/sla'
import { generateAnomaly } from '@/api/simulator'
import { listLinks } from '@/api/topology'
import http from '@/api/http'
import DataTable from '@/components/common/DataTable.vue'
import PageContainer from '@/components/common/PageContainer.vue'
import SlaGauge from '@/components/sla/SlaGauge.vue'
import SlaTrendChart from '@/components/sla/SlaTrendChart.vue'
import { useUserStore } from '@/store/user'

const dashboard = ref({
  overallComplianceRate: null,
  metricCompliance: [],
  breachTrend: [],
})

const columns = [
  { prop: 'position', label: '位置', minWidth: 140 },
  { prop: 'metricType', label: '指标类型', width: 140 },
  { prop: 'observedValue', label: '观测值', width: 150 },
  { prop: 'targetValue', label: '目标值', width: 140 },
  { prop: 'breachFlag', label: '是否违约', width: 120 },
  { prop: 'evaluatedAt', label: '评估时间', width: 180 },
]

const loadedRecords = ref([])

const fetcher = async (query) => {
  const res = await listSlaRecords(query)
  if (res && typeof res === 'object') {
    const list = Array.isArray(res) ? res : res.list || res.records || []
    loadedRecords.value = list
    return {
      ...res,
      total: res.total ? parseInt(res.total, 10) : 0,
      pages: res.pages ? parseInt(res.pages, 10) : 0,
    }
  }
  if (Array.isArray(res)) {
    loadedRecords.value = res
  }
  return res
}

const tableRef = ref(null)
const isSilentRefreshing = ref(false)
const realtimeStatus = ref('disconnected')
const lastRealtimeEvent = ref(null)
const userStore = useUserStore()
let eventSource = null
let refreshTimer = null
let refreshInFlight = false
let refreshQueued = false

const isGenerating = ref(false)
const showAnomalyDialog = ref(false)
const selectTenant = ref('')
const allTenants = ref([])
const linksList = ref([])

const openAnomalyDialog = async () => {
  try {
    const [linksRes, personsRes] = await Promise.all([
      listLinks(),
      http.get('/person/list')
    ])

    if (linksRes && Array.isArray(linksRes)) {
      linksList.value = linksRes
    }

    if (personsRes && Array.isArray(personsRes)) {
      const tenantMap = {}
      personsRes.forEach(p => {
        if (p.tenantId && p.tenantId !== 'admin' && p.position) {
          if (!tenantMap[p.tenantId]) {
            tenantMap[p.tenantId] = p.position
          }
        }
      })

      const uniqueTenants = Object.keys(tenantMap)
      allTenants.value = uniqueTenants.map(tenantId => ({
        id: tenantId,
        label: tenantMap[tenantId]
      }))

      if (allTenants.value.length > 0) {
        selectTenant.value = allTenants.value[0].id
      }
    }
    showAnomalyDialog.value = true
  } catch (err) {
    console.error('Failed to fetch data for dialog:', err)
    ElNotification({
      title: '错误',
      message: '获取数据失败，无法生成异常数据',
      type: 'error'
    })
  }
}

const submitAnomalyGeneration = async () => {
  if (!selectTenant.value) {
    ElNotification({
      title: '提示',
      message: '请选择租户',
      type: 'warning',
    })
    return
  }

  isGenerating.value = true
  try {
    const targetLink = linksList.value.find(link => link.tenantId === selectTenant.value)
    if (!targetLink) {
      ElNotification({
        title: '错误',
        message: '未找到该租户对应的网络链路',
        type: 'error',
      })
      return
    }

    await generateAnomaly({ linkId: targetLink.id })
    const selectedTenantObj = allTenants.value.find(t => t.id === selectTenant.value)
    const tenantLabel = selectedTenantObj ? selectedTenantObj.label : selectTenant.value
    
    ElNotification({
      title: '成功',
      message: `已成功为租户 [${tenantLabel}] 生成模拟异常数据`,
      type: 'success',
    })
    showAnomalyDialog.value = false
    await refreshAllData()
  } catch (error) {
    console.error('Failed to generate anomaly:', error)
  } finally {
    isGenerating.value = false
  }
}

const statusMeta = computed(() => {
  const map = {
    connected: { label: 'SSE 已连接', type: 'success' },
    connecting: { label: 'SSE 连接中', type: 'warning' },
    reconnecting: { label: 'SSE 重连中', type: 'warning' },
    disconnected: { label: 'SSE 已断开', type: 'info' },
  }
  return map[realtimeStatus.value] || map.disconnected
})

const lastEventSummary = computed(() => {
  const event = lastRealtimeEvent.value
  if (!event) return ''
  const location = event.position || event.linkCode || event.linkId || '-'
  const metric = event.metricType || '-'
  const observed = event.observedValue ?? '-'
  const target = event.targetValue ?? '-'
  const result = event.breachFlag ? '违约' : '达标'
  return `${location} / ${metric}: ${observed} / ${target}，${result}`
})

const fetchDashboardData = async () => {
  try {
    dashboard.value = await getDashboard()
  } catch (error) {
    console.error('Failed to fetch SLA dashboard:', error)
  }
}

const refreshAllData = async () => {
  if (refreshInFlight) {
    refreshQueued = true
    return
  }

  refreshInFlight = true
  isSilentRefreshing.value = true
  try {
    await fetchDashboardData()
    if (tableRef.value) {
      await tableRef.value.refresh()
    }
  } catch (error) {
    console.error('Failed to refresh SLA data:', error)
  } finally {
    isSilentRefreshing.value = false
    refreshInFlight = false
    if (refreshQueued) {
      refreshQueued = false
      scheduleRealtimeRefresh(0)
    }
  }
}

const scheduleRealtimeRefresh = (delay = 600) => {
  if (refreshTimer) {
    clearTimeout(refreshTimer)
  }
  refreshTimer = setTimeout(() => {
    refreshTimer = null
    refreshAllData()
  }, delay)
}

const parseSlaEvent = (data) => {
  try {
    return JSON.parse(data)
  } catch (error) {
    console.warn('Failed to parse SLA realtime event:', error)
    return null
  }
}

const openRealtimeStream = () => {
  if (eventSource || !userStore.token) return

  realtimeStatus.value = 'connecting'
  eventSource = createSlaEventSource(userStore.token)
  if (!eventSource) {
    realtimeStatus.value = 'disconnected'
    return
  }

  eventSource.addEventListener('connected', () => {
    realtimeStatus.value = 'connected'
  })

  eventSource.addEventListener('sla-event', (message) => {
    realtimeStatus.value = 'connected'
    const event = parseSlaEvent(message.data)
    if (event) {
      lastRealtimeEvent.value = event
      if (event.breachFlag) {
        ElNotification({
          title: 'SLA 链路违约报警',
          message: `监测到链路异常违约！
位置: ${event.position || '-'}
指标: ${event.metricType || '-'}
观测值: ${event.observedValue ?? '-'} (目标: ${event.targetValue ?? '-'})`,
          type: 'error',
          position: 'top-right',
          duration: 6000,
        })
      }
    }
    scheduleRealtimeRefresh()
  })

  eventSource.onerror = () => {
    realtimeStatus.value = eventSource?.readyState === EventSource.CLOSED ? 'disconnected' : 'reconnecting'
  }
}

const closeRealtimeStream = () => {
  if (refreshTimer) {
    clearTimeout(refreshTimer)
    refreshTimer = null
  }
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
  realtimeStatus.value = 'disconnected'
}

onMounted(async () => {
  await fetchDashboardData()
  openRealtimeStream()
})

onUnmounted(() => {
  closeRealtimeStream()
})
</script>

<template>
  <PageContainer title="全局 SLA 仪表盘" description="展示 SLA 达标率、违约趋势 and 评估记录。">
    <div :class="{ 'silent-refresh': isSilentRefreshing }">
      <div class="sla-toolbar">
        <div class="toolbar-actions">
          <ElButton
            type="warning"
            :loading="isGenerating"
            class="generate-btn"
            @click="openAnomalyDialog"
          >
            <ElIcon class="btn-icon"><Warning /></ElIcon>
            生成异常数据
          </ElButton>
        </div>
        <div class="realtime-state">
          <ElTag :type="statusMeta.type" effect="plain" round>{{ statusMeta.label }}</ElTag>
          <span v-if="lastEventSummary" class="last-event">{{ lastEventSummary }}</span>
        </div>
      </div>
      <div class="sla-layout">
        <SlaGauge :value="dashboard?.overallComplianceRate" />
        <SlaTrendChart :points="dashboard?.breachTrend || []" />
      </div>
      <DataTable ref="tableRef" :columns="columns" :fetcher="fetcher" />
    </div>

    <!-- 异常数据生成对话框 -->
    <ElDialog
      v-model="showAnomalyDialog"
      title="生成模拟异常网络数据"
      width="460px"
      destroy-on-close
    >
      <ElForm label-width="100px" style="padding-top: 10px;">
        <ElFormItem label="选择租户">
          <ElSelect v-model="selectTenant" placeholder="请选择租户" style="width: 100%;">
            <ElOption
              v-for="tenant in allTenants"
              :key="tenant.id"
              :label="tenant.label"
              :value="tenant.id"
            />
          </ElSelect>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <span class="dialog-footer">
          <ElButton @click="showAnomalyDialog = false">取消</ElButton>
          <ElButton type="primary" :loading="isGenerating" @click="submitAnomalyGeneration">
            确定生成
          </ElButton>
        </span>
      </template>
    </ElDialog>
  </PageContainer>
</template>

<style scoped>
.sla-layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.sla-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.generate-btn {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%) !important;
  border: none !important;
  color: #fff !important;
  font-weight: 600 !important;
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3) !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  border-radius: 6px !important;
}

.generate-btn:hover {
  transform: translateY(-2px) scale(1.02) !important;
  box-shadow: 0 6px 16px rgba(245, 158, 11, 0.4) !important;
}

.generate-btn:active {
  transform: translateY(0) scale(0.98) !important;
}

.btn-icon {
  margin-right: 6px;
  font-size: 16px;
}

.realtime-state {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 10px;
}

.last-event {
  max-width: min(560px, 62vw);
  overflow: hidden;
  color: var(--am-text-secondary);
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.silent-refresh .el-loading-mask) {
  display: none !important;
}

@media (max-width: 768px) {
  .sla-layout {
    grid-template-columns: 1fr;
  }

  .sla-toolbar {
    justify-content: flex-start;
  }

  .realtime-state {
    align-items: flex-start;
    flex-direction: column;
  }

  .last-event {
    max-width: 100%;
    white-space: normal;
  }
}
</style>
