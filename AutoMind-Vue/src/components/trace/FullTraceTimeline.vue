<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  trace: {
    type: Object,
    default: () => ({ events: [] }),
  },
})

const activeEvent = ref(null)
const drawerVisible = ref(false)

const events = computed(() => props.trace?.events || [])
const summary = computed(() => props.trace?.summary || {})

function tagType(status) {
  const value = String(status || '').toUpperCase()
  if (['COMPLETED', 'SUCCESS', 'OK', 'APPROVED'].includes(value)) return 'success'
  if (['PENDING', 'AWAIT_APPROVAL', 'PERMISSION'].includes(value)) return 'warning'
  if (['FAILED', 'BREACH', 'REJECTED', 'TIMEOUT'].includes(value) || value.startsWith('MCP_')) return 'danger'
  return 'info'
}

function timelineType(event) {
  const type = tagType(event?.status)
  if (type === 'danger') return 'danger'
  if (type === 'warning') return 'warning'
  if (type === 'success') return 'success'
  return 'primary'
}

function formatPayload(value) {
  if (value == null || value === '') return '-'
  try {
    const data = JSON.parse(JSON.stringify(value))
    if (data && typeof data === 'object') {
      const scrub = (obj) => {
        if (Array.isArray(obj)) {
          obj.forEach(scrub)
        } else if (obj !== null) {
          delete obj.traceId
          delete obj.trace_id
          Object.keys(obj).forEach(k => {
            if (typeof obj[k] === 'object') scrub(obj[k])
          })
        }
      }
      scrub(data)
    }
    return JSON.stringify(data, null, 2)
  } catch (e) {
    return JSON.stringify(value, null, 2)
  }
}

function displayValue(value) {
  if (value == null || value === '') return '-'
  if (Array.isArray(value)) return value.length ? value.map((item) => (typeof item === 'object' ? JSON.stringify(item) : item)).join('；') : '无'
  if (typeof value === 'object') return JSON.stringify(value)
  return value
}

function eventItems(event) {
  const payload = event?.payload || {}
  if (event?.eventType === 'AGENT_TASK') {
    return payload.decisionSummary?.items || []
  }
  if (event?.eventType === 'MCP_CALL') {
    return [
      { label: '工具', value: payload.toolName },
      { label: '响应码', value: payload.responseCode },
      { label: '错误类型', value: payload.errorType },
      { label: '耗时', value: payload.latencyMs, unit: 'ms' },
      { label: '高风险', value: payload.isHighRisk ? '是' : '否' },
    ]
  }
  if (event?.eventType === 'A2A_MESSAGE') {
    return [
      { label: '源域', value: payload.sourceDomainId },
      { label: '目标域', value: payload.targetDomainId },
      { label: '消息类型', value: payload.messageType },
      { label: '耗时', value: payload.latencyMs, unit: 'ms' },
    ]
  }
  if (event?.eventType === 'APPROVAL') {
    return [
      { label: '审批类型', value: payload.approvalType },
      { label: '风险等级', value: payload.riskLevel },
      { label: '审批状态', value: payload.status },
      { label: '审批意见', value: payload.decisionReason },
    ]
  }
  if (event?.eventType === 'SLA_RECORD') {
    return [
      { label: '链路', value: payload.linkId },
      { label: '指标', value: payload.metricType },
      { label: '目标值', value: payload.targetValue },
      { label: '观测值', value: payload.observedValue },
      { label: '是否违约', value: payload.breachFlag ? '是' : '否' },
    ]
  }
  return []
}

function openEvent(event) {
  activeEvent.value = event
  drawerVisible.value = true
}
</script>

<template>
  <ElEmpty v-if="!events.length" description="输入 intentId 或关联标识后查看 Agent 执行时间线" />
  <div v-else class="trace-wrap">
    <ElDescriptions class="trace-overview" :column="4" border>
      <ElDescriptionsItem label="intentId">{{ trace.intentId }}</ElDescriptionsItem>
      <ElDescriptionsItem label="tenantId">{{ trace.tenantId }}</ElDescriptionsItem>
      <ElDescriptionsItem label="状态">
        <ElTag :type="tagType(trace.status)">{{ trace.status }}</ElTag>
      </ElDescriptionsItem>
      <ElDescriptionsItem label="事件数">{{ events.length }}</ElDescriptionsItem>
      <ElDescriptionsItem label="创建时间" :span="4">{{ trace.createTime }}</ElDescriptionsItem>
      <ElDescriptionsItem label="用户意图" :span="4">{{ trace.inputText }}</ElDescriptionsItem>
    </ElDescriptions>

    <div class="metric-row">
      <ElCard shadow="never">
        <div class="metric-value">{{ summary.agentTaskCount || 0 }}</div>
        <div class="metric-label">Agent 任务</div>
      </ElCard>
      <ElCard shadow="never">
        <div class="metric-value">{{ summary.mcpCallCount || 0 }}</div>
        <div class="metric-label">MCP 调用</div>
      </ElCard>
      <ElCard shadow="never">
        <div class="metric-value">{{ summary.a2aMessageCount || 0 }}</div>
        <div class="metric-label">A2A 消息</div>
      </ElCard>
      <ElCard shadow="never">
        <div class="metric-value">{{ summary.approvalCount || 0 }}</div>
        <div class="metric-label">审批记录</div>
      </ElCard>
      <ElCard shadow="never">
        <div class="metric-value">{{ summary.slaBreaches || 0 }}</div>
        <div class="metric-label">SLA 违约</div>
      </ElCard>
    </div>

    <ElTimeline>
      <ElTimelineItem
        v-for="event in events"
        :key="`${event.eventType}-${event.payloadId}-${event.eventTime}`"
        :timestamp="event.eventTime"
        :type="timelineType(event)"
        placement="top"
      >
        <ElCard shadow="never" class="event-card" @click="openEvent(event)">
          <div class="event-head">
            <div>
              <div class="event-title">{{ event.title || event.eventType }}</div>
              <div class="event-summary">{{ event.summary }}</div>
            </div>
            <div class="event-tags">
              <ElTag size="small">{{ event.eventType }}</ElTag>
              <ElTag size="small" :type="tagType(event.status)">{{ event.status }}</ElTag>
            </div>
          </div>
          <div v-if="eventItems(event).length" class="item-grid">
            <div v-for="item in eventItems(event).slice(0, 6)" :key="item.label" class="item-cell">
              <span>{{ item.label }}</span>
              <strong>{{ displayValue(item.value) }}{{ item.unit || '' }}</strong>
            </div>
          </div>
        </ElCard>
      </ElTimelineItem>
    </ElTimeline>

    <ElDrawer v-model="drawerVisible" title="事件详情" size="520px">
      <template v-if="activeEvent">
        <ElDescriptions :column="1" border>
          <ElDescriptionsItem label="事件类型">{{ activeEvent.eventType }}</ElDescriptionsItem>
          <ElDescriptionsItem label="阶段">{{ activeEvent.stage }}</ElDescriptionsItem>
          <ElDescriptionsItem label="状态">
            <ElTag :type="tagType(activeEvent.status)">{{ activeEvent.status }}</ElTag>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="时间">{{ activeEvent.eventTime }}</ElDescriptionsItem>
          <ElDescriptionsItem label="摘要">{{ activeEvent.summary }}</ElDescriptionsItem>
        </ElDescriptions>

        <ElDivider />
        <div class="drawer-section-title">关键字段</div>
        <ElDescriptions :column="1" border>
          <ElDescriptionsItem v-for="item in eventItems(activeEvent)" :key="item.label" :label="item.label">
            {{ displayValue(item.value) }}{{ item.unit || '' }}
          </ElDescriptionsItem>
        </ElDescriptions>

        <ElCollapse class="raw-collapse">
          <ElCollapseItem title="原始 Payload" name="payload">
            <pre>{{ formatPayload(activeEvent.payload) }}</pre>
          </ElCollapseItem>
        </ElCollapse>
      </template>
    </ElDrawer>
  </div>
</template>

<style scoped>
.trace-wrap {
  padding: 16px;
  border: 1px solid var(--am-border);
  border-radius: 8px;
  background: #fff;
}

.trace-overview {
  margin-bottom: 14px;
}

.metric-row {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 18px;
}

.metric-value {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
}

.metric-label {
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
}

.event-card {
  cursor: pointer;
}

.event-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.event-title {
  font-weight: 700;
  color: #1f2937;
}

.event-summary {
  margin-top: 6px;
  color: #64748b;
}

.event-tags {
  display: flex;
  flex-shrink: 0;
  gap: 8px;
}

.item-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  margin-top: 12px;
}

.item-cell {
  padding: 8px 10px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #f8fafc;
}

.item-cell span {
  display: block;
  color: #64748b;
  font-size: 12px;
}

.item-cell strong {
  display: block;
  margin-top: 4px;
  color: #111827;
  word-break: break-all;
}

.drawer-section-title {
  margin-bottom: 10px;
  font-weight: 700;
}

.raw-collapse {
  margin-top: 14px;
}

pre {
  max-height: 420px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 1100px) {
  .metric-row,
  .item-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
