<script setup>
const props = defineProps({
  approval: {
    type: Object,
    default: () => ({}),
  },
})

function formatPayload(value) {
  if (value == null || value === '') return '-'
  if (typeof value === 'object') return JSON.stringify(value, null, 2)
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch {
    return String(value)
  }
}

const fields = [
  ['租户', 'tenantId'],
  ['岗位', 'position'],
  ['优先级', 'priority'],
  ['状态', 'status'],
  ['审批类型', 'approvalType'],
  ['风险等级', 'riskLevel'],
  ['提交时间', 'submittedAt'],
  ['决策时间', 'decidedAt'],
]

const parsedIntentFields = [
  ['意图类型', 'intentType'],
  ['源位置', 'sourceLocation'],
  ['目标位置', 'targetLocation'],
  ['协议', 'protocol'],
  ['带宽 Mbps', 'bandwidthMbps'],
  ['最大时延 ms', 'maxLatencyMs'],
  ['最大丢包率', 'maxLossRate'],
  ['优先级', 'priority'],
  ['开始时间', 'startTime'],
  ['结束时间', 'endTime'],
  ['置信度', 'confidence'],
]
</script>

<template>
  <ElDescriptions border :column="2">
    <ElDescriptionsItem v-for="[label, prop] in fields" :key="prop" :label="label">
      {{ props.approval[prop] || '-' }}
    </ElDescriptionsItem>
    <ElDescriptionsItem label="原始意图" :span="2">
      <pre>{{ props.approval.inputText || '-' }}</pre>
    </ElDescriptionsItem>
    <ElDescriptionsItem label="解析结果" :span="2">
      <div v-if="props.approval.parsedIntent" class="parsed-intent">
        <div v-for="[label, prop] in parsedIntentFields" :key="prop" class="parsed-field">
          <span>{{ label }}</span>
          <strong>{{ props.approval.parsedIntent[prop] ?? '-' }}</strong>
        </div>
        <div class="parsed-field parsed-field-wide">
          <span>缺失字段</span>
          <strong>{{ props.approval.parsedIntent.missingFields?.length ? props.approval.parsedIntent.missingFields.join('、') : '无' }}</strong>
        </div>
      </div>
      <span v-else>-</span>
    </ElDescriptionsItem>
    <ElDescriptionsItem label="决策原因" :span="2">
      {{ props.approval.decisionReason || '-' }}
    </ElDescriptionsItem>
    <ElDescriptionsItem label="AI 推荐" :span="2">
      <pre>{{ formatPayload(props.approval.aiRecommendation) }}</pre>
    </ElDescriptionsItem>
    <ElDescriptionsItem label="提交载荷" :span="2">
      <pre>{{ formatPayload(props.approval.submittedPayload) }}</pre>
    </ElDescriptionsItem>
    <ElDescriptionsItem label="修改后载荷" :span="2">
      <pre>{{ formatPayload(props.approval.modifiedPayload) }}</pre>
    </ElDescriptionsItem>
  </ElDescriptions>
</template>

<style scoped>
pre {
  margin: 0;
  white-space: pre-wrap;
}

.parsed-intent {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 16px;
}

.parsed-field {
  display: grid;
  grid-template-columns: 100px minmax(0, 1fr);
  gap: 8px;
  min-width: 0;
}

.parsed-field span {
  color: #64748b;
}

.parsed-field strong {
  min-width: 0;
  overflow-wrap: anywhere;
  font-weight: 500;
  color: #0f172a;
}

.parsed-field-wide {
  grid-column: 1 / -1;
}

@media (max-width: 760px) {
  .parsed-intent {
    grid-template-columns: 1fr;
  }

  .parsed-field-wide {
    grid-column: auto;
  }
}
</style>
