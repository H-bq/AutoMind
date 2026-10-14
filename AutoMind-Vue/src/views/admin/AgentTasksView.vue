<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getAgentTasksByIntent, listAgentTasks } from '@/api/agent'
import PageContainer from '@/components/common/PageContainer.vue'
import TraceIdTag from '@/components/common/TraceIdTag.vue'
import { useTraceStore } from '@/store/trace'

const groupRows = ref([])
const total = ref(0)
const loading = ref(false)
const taskLoadingIntentIds = ref(new Set())
const traceStore = useTraceStore()
const router = useRouter()
const query = reactive({
  pageNo: 1,
  pageSize: 10,
})

const taskColumns = [
  { prop: 'id', label: '任务ID', width: 90 },
  { prop: 'agentRole', label: 'Agent角色', width: 120 },
  { prop: 'taskType', label: '任务类型', width: 140 },
  { prop: 'pevState', label: '状态', width: 130 },
  { prop: 'confidence', label: '置信度', width: 110 },
  { prop: 'retryCount', label: '重试次数', width: 110 },
  { prop: 'errorCode', label: '错误码', width: 140 },
  { prop: 'startedAt', label: '启动时间', width: 180 },
  { prop: 'finishedAt', label: '完成时间', width: 180 },
]

async function refresh() {
  loading.value = true
  try {
    const result = await listAgentTasks(query)
    groupRows.value = (result?.list || result?.records || []).map((item) => ({
      ...item,
      agentTasks: [],
      tasksLoaded: false,
    }))
    total.value = result?.total || groupRows.value.length
  } finally {
    loading.value = false
  }
}

async function loadTasks(row) {
  if (row.tasksLoaded || taskLoadingIntentIds.value.has(row.intentId)) return

  taskLoadingIntentIds.value = new Set(taskLoadingIntentIds.value).add(row.intentId)
  try {
    row.agentTasks = await getAgentTasksByIntent(row.intentId)
    row.tasksLoaded = true
  } finally {
    const next = new Set(taskLoadingIntentIds.value)
    next.delete(row.intentId)
    taskLoadingIntentIds.value = next
  }
}

function handleExpandChange(row, expandedRows) {
  if (expandedRows.some((item) => item.intentId === row.intentId)) {
    loadTasks(row)
  }
}

function isTaskLoading(intentId) {
  return taskLoadingIntentIds.value.has(intentId)
}

function openTrace(row) {
  if (!row?.intentId) return
  router.push(`/admin/trace/${row.intentId}`)
}

function handleSizeChange(pageSize) {
  query.pageSize = pageSize
  query.pageNo = 1
  refresh()
}

function handleCurrentChange(pageNo) {
  query.pageNo = pageNo
  refresh()
}

onMounted(refresh)
</script>

<template>
  <PageContainer title="Agent 任务监控" description="按 intentId 汇总展示当前账号可见的 Agent 任务。">
    <div class="task-groups">
      <ElTable
        v-loading="loading"
        :data="groupRows"
        border
        stripe
        row-key="intentId"
        @expand-change="handleExpandChange"
      >
        <ElTableColumn type="expand" width="48">
          <template #default="{ row }">
            <div class="task-detail">
              <ElTable
                v-loading="isTaskLoading(row.intentId)"
                :data="row.agentTasks"
                border
                size="small"
                empty-text="该 intentId 下暂无 Agent 任务"
              >
                <ElTableColumn
                  v-for="column in taskColumns"
                  :key="column.prop"
                  :prop="column.prop"
                  :label="column.label"
                  :width="column.width"
                  show-overflow-tooltip
                />
              </ElTable>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="tenantId" label="租户ID" min-width="140" show-overflow-tooltip />
        <ElTableColumn prop="position" label="位置" min-width="160" show-overflow-tooltip />
        <ElTableColumn prop="inputText" label="输入文本" min-width="320" show-overflow-tooltip />
        <ElTableColumn prop="createTime" label="创建时间" width="180" show-overflow-tooltip />
        <ElTableColumn label="链路详情" width="120" fixed="right">
          <template #default="{ row }">
            <ElButton type="primary" link @click.stop="openTrace(row)">查看</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>

      <div class="table-footer">
        <TraceIdTag v-if="traceStore.lastTraceId" :trace-id="traceStore.lastTraceId" />
        <ElPagination
          background
          layout="total, sizes, prev, pager, next"
          :page-sizes="[10, 20, 50]"
          :current-page="query.pageNo"
          :page-size="query.pageSize"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </PageContainer>
</template>

<style scoped>
.task-groups {
  padding: 16px;
  border: 1px solid var(--am-border);
  border-radius: 8px;
  background: #fff;
}

.task-detail {
  padding: 12px 16px 16px 56px;
  background: #f8fafc;
}

.table-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 14px;
}
</style>
