<script setup>
import { ref, onMounted, watch } from 'vue'
import { listSlaRecords } from '@/api/sla'
import { Calendar, ArrowRight, Connection } from '@element-plus/icons-vue'

const props = defineProps({
  points: {
    type: Array,
    default: () => [],
  },
})

const loading = ref(false)
const tenantGroups = ref([])
const dialogVisible = ref(false)
const selectedTenant = ref('')
const selectedBucket = ref('')
const detailedRecords = ref([])

// 实时监测父组件的指标数据变化，重新拉取并按租户进行归类
watch(() => props.points, () => {
  fetchAndGroupRecords()
}, { deep: true })

async function fetchAndGroupRecords() {
  loading.value = true
  try {
    const res = await listSlaRecords({ pageNo: 1, pageSize: 100, breachFlag: true })
    const records = res?.list || res?.records || []

    // 1. 按租户（位置/Position）进行第一层分组
    const groupsMap = {}
    records.forEach(record => {
      const tenantName = record.position || record.tenantId || '未知租户'
      if (!groupsMap[tenantName]) {
        groupsMap[tenantName] = []
      }
      groupsMap[tenantName].push(record)
    })

    // 2. 在每个租户分组内，按分钟时间桶进行第二层聚合
    const resultGroups = []
    for (const [tenantName, tenantRecords] of Object.entries(groupsMap)) {
      const bucketMap = {}
      tenantRecords.forEach(record => {
        const bucketTime = truncateToMinute(record.evaluatedAt)
        if (!bucketMap[bucketTime]) {
          bucketMap[bucketTime] = []
        }
        bucketMap[bucketTime].push(record)
      })

      const points = Object.entries(bucketMap).map(([bucketTime, bucketRecords]) => {
        return {
          bucketTime,
          breachCount: bucketRecords.length,
          records: bucketRecords
        }
      }).sort((a, b) => b.bucketTime.localeCompare(a.bucketTime)) // 按时间降序

      resultGroups.push({
        tenantName,
        points
      })
    }
    // 排序租户名字
    resultGroups.sort((a, b) => a.tenantName.localeCompare(b.tenantName))
    tenantGroups.value = resultGroups
  } catch (error) {
    console.error('Failed to load and group SLA breach records:', error)
  } finally {
    loading.value = false
  }
}

function truncateToMinute(timeStr) {
  if (!timeStr || timeStr.length < 16) return timeStr
  return timeStr.substring(0, 16) + ':00'
}

function showDetails(tenantName, point) {
  selectedTenant.value = tenantName
  selectedBucket.value = point.bucketTime
  detailedRecords.value = point.records
  dialogVisible.value = true
}

function getMetricLabel(type) {
  switch (type) {
    case 'LATENCY': return '时延 (Latency)'
    case 'JITTER': return '抖动 (Jitter)'
    case 'LOSS': return '丢包率 (Packet Loss)'
    case 'BANDWIDTH': return '带宽占用 (Bandwidth)'
    default: return type
  }
}

onMounted(() => {
  fetchAndGroupRecords()
})
</script>

<template>
  <div class="trend-chart" v-loading="loading">
    <div class="chart-header">
      <span class="title">SLA 违约历史趋势</span>
    </div>
    
    <div v-if="tenantGroups.length > 0" class="tenant-groups-container">
      <div v-for="group in tenantGroups" :key="group.tenantName" class="tenant-group-card">
        <div class="tenant-header">
          <ElIcon class="tenant-icon"><Connection /></ElIcon>
          <span class="tenant-name">{{ group.tenantName }}</span>
        </div>
        <div class="tenant-points-list">
          <div
            v-for="point in group.points"
            :key="point.bucketTime"
            class="trend-point clickable-row"
            @click="showDetails(group.tenantName, point)"
          >
            <div class="time-info">
              <ElIcon class="icon-clock"><Calendar /></ElIcon>
              <span>{{ point.bucketTime }}</span>
            </div>
            <div class="breach-info">
              <ElTag type="danger" size="small" effect="dark" class="breach-tag">
                {{ point.breachCount }} 项违约
              </ElTag>
              <ElIcon class="arrow-icon"><ArrowRight /></ElIcon>
            </div>
          </div>
        </div>
      </div>
    </div>
    <ElEmpty v-else description="数据暂时为空" />

    <!-- SLA 违约指标详情弹窗 -->
    <ElDialog
      v-model="dialogVisible"
      :title="`SLA 违约指标详情 (${selectedTenant} - ${selectedBucket})`"
      width="600px"
      destroy-on-close
      append-to-body
    >
      <div class="dialog-content">
        <ElTable :data="detailedRecords" border stripe empty-text="该时间段未找到匹配的违约指标">
          <ElTableColumn prop="metricType" label="违约指标" min-width="180">
            <template #default="{ row }">
              <span class="metric-label">{{ getMetricLabel(row.metricType) }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="observedValue" label="实测观测值" width="130">
            <template #default="{ row }">
              <span class="breach-value">{{ row.observedValue }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="targetValue" label="SLA 目标阈值" width="130">
            <template #default="{ row }">
              <span class="target-value">{{ row.targetValue }}</span>
            </template>
          </ElTableColumn>
        </ElTable>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <ElButton @click="dialogVisible = false">关闭</ElButton>
        </div>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.trend-chart {
  display: flex;
  flex-direction: column;
  min-height: 260px;
  border: 1px solid var(--am-border, #e2e8f0);
  border-radius: 8px;
  background: #fff;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.chart-header {
  margin-bottom: 16px;
  border-bottom: 1px solid #f1f5f9;
  padding-bottom: 8px;
}

.chart-header .title {
  font-weight: 600;
  font-size: 15px;
  color: #1e293b;
}

.tenant-groups-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tenant-group-card {
  border: 1px solid #f1f5f9;
  border-radius: 6px;
  background: #fafbfd;
  padding: 12px;
}

.tenant-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 14px;
  color: #1e293b;
  margin-bottom: 8px;
  padding-bottom: 4px;
  border-bottom: 1px solid #f1f5f9;
}

.tenant-icon {
  color: var(--el-color-primary, #409eff);
}

.tenant-points-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.trend-point {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px dashed #f1f5f9;
  padding: 8px 6px;
  transition: all 0.2s ease-in-out;
  border-radius: 4px;
  background: #fff;
}

.clickable-row {
  cursor: pointer;
}

.clickable-row:hover {
  background-color: #f8fafc;
  transform: translateX(4px);
}

.time-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #475569;
}

.icon-clock {
  color: #94a3b8;
}

.breach-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.breach-tag {
  font-weight: bold;
}

.arrow-icon {
  color: #cbd5e1;
  transition: color 0.2s;
}

.clickable-row:hover .arrow-icon {
  color: #64748b;
}

.dialog-content {
  padding: 10px 0;
}

.metric-label {
  font-weight: 600;
  color: var(--el-color-danger, #f56c6c);
}

.breach-value {
  color: var(--el-color-danger, #f56c6c);
  font-weight: 600;
}

.target-value {
  color: #64748b;
}

.dialog-footer {
  text-align: right;
}
</style>


