<script setup>
import { onMounted, ref } from 'vue'
import { listHealing } from '@/api/healing'

const recentRecords = ref([])
const loading = ref(false)

async function fetchRecent() {
  loading.value = true
  try {
    const res = await listHealing({ pageNo: 1, pageSize: 5 })
    recentRecords.value = res?.list || res?.records || []
  } catch (error) {
    console.error('Failed to fetch recent healing records:', error)
  } finally {
    loading.value = false
  }
}

function getStatusType(result) {
  switch (result) {
    case 'SUCCESS': return 'success'
    case 'HANDOFF_HUMAN': return 'danger'
    default: return 'warning'
  }
}

onMounted(fetchRecent)
</script>

<template>
  <div class="healing-chain-card" v-loading="loading">
    <div class="card-header">
      <span class="title">最新自愈动态 (最近 5 次)</span>
      <ElButton type="primary" link size="small" @click="fetchRecent">
        刷新
      </ElButton>
    </div>
    <div class="card-body">
      <ElTimeline v-if="recentRecords.length > 0">
        <ElTimelineItem
          v-for="record in recentRecords"
          :key="record.id"
          :timestamp="record.createdAt"
          :type="getStatusType(record.result)"
          hollow
        >
          <div class="timeline-content">
            <div class="timeline-header">
              <span class="intent-id">意图 #{{ record.intentId }}</span>
              <ElTag size="small" :type="getStatusType(record.result)" class="result-tag">
                {{ record.result }}
              </ElTag>
              <span class="trigger-src">触发源: {{ record.triggerSource }}</span>
            </div>
            <div class="cause" v-if="record.rootCause">
              <strong>故障原因:</strong> {{ record.rootCause }}
            </div>
            <div class="strategy-info">
              <span>自愈策略: <strong>{{ record.strategy }}</strong></span>
              <span class="separator">|</span>
              <span>重试次数: <strong>{{ record.attemptNo }} / {{ record.maxAttempts }}</strong></span>
            </div>
          </div>
        </ElTimelineItem>
      </ElTimeline>
      <ElEmpty v-else description="暂无最近自愈记录" :image-size="60" />
    </div>
  </div>
</template>

<style scoped>
.healing-chain-card {
  margin-bottom: 20px;
  padding: 20px;
  border: 1px solid var(--am-border, #e2e8f0);
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  border-bottom: 1px solid #f1f5f9;
  padding-bottom: 8px;
}

.card-header .title {
  font-weight: 600;
  font-size: 15px;
  color: #1e293b;
}

.card-body {
  padding-top: 8px;
}

.timeline-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
  background: #f8fafc;
  padding: 12px 16px;
  border-radius: 6px;
  border-left: 3px solid #cbd5e1;
}

.timeline-header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.intent-id {
  font-weight: 600;
  color: var(--el-color-primary, #409eff);
}

.result-tag {
  font-weight: bold;
}

.trigger-src {
  font-size: 12px;
  color: #64748b;
  background: #e2e8f0;
  padding: 2px 6px;
  border-radius: 4px;
}

.cause {
  font-size: 13px;
  color: #475569;
  line-height: 1.4;
}

.strategy-info {
  font-size: 12px;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 8px;
}

.separator {
  color: #cbd5e1;
}
</style>
