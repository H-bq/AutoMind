<script setup>
import { onMounted, reactive, ref } from 'vue'
import TraceIdTag from './TraceIdTag.vue'
import { useTraceStore } from '@/store/trace'

const props = defineProps({
  columns: {
    type: Array,
    required: true,
  },
  fetcher: {
    type: Function,
    required: true,
  },
  autoLoad: {
    type: Boolean,
    default: true,
  },
  rowClickable: {
    type: Boolean,
    default: false,
  },
})
const emit = defineEmits(['row-click'])

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const traceStore = useTraceStore()
const query = reactive({
  pageNo: 1,
  pageSize: 10,
})

async function refresh(extraQuery = {}) {
  loading.value = true
  try {
    const result = await props.fetcher({ ...query, ...extraQuery })
    const list = Array.isArray(result) ? result : result?.list || result?.records || []
    rows.value = list
    total.value = Array.isArray(result) ? list.length : result?.total || list.length
  } finally {
    loading.value = false
  }
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

function handleRowClick(row) {
  emit('row-click', row)
}

defineExpose({ refresh })

onMounted(() => {
  if (props.autoLoad) refresh()
})
</script>

<template>
  <div class="data-table">
    <ElTable
      v-loading="loading"
      :data="rows"
      :row-class-name="rowClickable ? 'is-clickable' : ''"
      border
      stripe
      @row-click="handleRowClick"
    >
      <ElTableColumn
        v-for="column in columns"
        :key="column.prop || column.label"
        :prop="column.prop"
        :label="column.label"
        :width="column.width"
        :min-width="column.minWidth"
        :fixed="column.fixed"
        show-overflow-tooltip
      >
        <template v-if="$slots[column.prop]" #default="scope">
          <slot :name="column.prop" v-bind="scope" />
        </template>
      </ElTableColumn>
      <template #empty>
        <ElEmpty description="数据暂时为空" />
      </template>
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
</template>

<style scoped>
.data-table {
  padding: 16px;
  border: 1px solid var(--am-border);
  border-radius: 8px;
  background: #fff;
}

.table-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14px;
}

:deep(.is-clickable) {
  cursor: pointer;
}
</style>
