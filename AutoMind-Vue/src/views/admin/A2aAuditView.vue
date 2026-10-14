<script setup>
import { onMounted, reactive, ref } from 'vue'
import { listA2aMessages, getA2aMessage } from '@/api/a2a'
import PageContainer from '@/components/common/PageContainer.vue'
import { ElMessage } from 'element-plus'
import {
  Search,
  Refresh,
  InfoFilled,
  Warning,
  CopyDocument,
  Timer,
  CircleCheck,
  CircleClose,
  View,
  User,
  Cpu,
  Aim,
  Clock,
  Document,
  Right,
  Connection
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const listRows = ref([])
const total = ref(0)
const loading = ref(false)

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  traceId: '',
  sourceDomainId: '',
  targetDomainId: '',
  messageType: '',
  status: '',
  startTime: null,
  endTime: null,
})

const timeRange = ref([])

// Drawer state
const drawerVisible = ref(false)
const detailLoading = ref(false)
const currentDetail = ref(null)
const viewMode = ref('visual')

async function refresh() {
  loading.value = true
  // Sync time range
  if (timeRange.value && timeRange.value.length === 2) {
    query.startTime = timeRange.value[0]
    query.endTime = timeRange.value[1]
  } else {
    query.startTime = null
    query.endTime = null
  }
  
  // Clean up params (remove empty/null strings)
  const params = {
    pageNo: query.pageNo,
    pageSize: query.pageSize,
    traceId: query.traceId || undefined,
    sourceDomainId: query.sourceDomainId || undefined,
    targetDomainId: query.targetDomainId || undefined,
    messageType: query.messageType || undefined,
    status: query.status || undefined,
    startTime: query.startTime || undefined,
    endTime: query.endTime || undefined
  }

  try {
    const res = await listA2aMessages(params)
    listRows.value = res?.list || res?.records || []
    total.value = res?.total || 0
  } catch (error) {
    console.error('Failed to load A2A cross-domain messages:', error)
    ElMessage.error('加载 A2A 跨域消息失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNo = 1
  refresh()
}

function handleReset() {
  query.traceId = ''
  query.sourceDomainId = ''
  query.targetDomainId = ''
  query.messageType = ''
  query.status = ''
  query.startTime = null
  query.endTime = null
  timeRange.value = []
  query.pageNo = 1
  refresh()
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

async function showDetail(row) {
  drawerVisible.value = true
  detailLoading.value = true
  currentDetail.value = null
  viewMode.value = 'visual'
  try {
    const res = await getA2aMessage(row.id)
    currentDetail.value = res
  } catch (error) {
    console.error('Failed to load A2A detail:', error)
    ElMessage.error('加载 A2A 消息详情失败')
  } finally {
    detailLoading.value = false
  }
}

const formatJson = (jsonStr) => {
  if (!jsonStr) return '无数据 (No Data)'
  try {
    const parsed = typeof jsonStr === 'string' ? JSON.parse(jsonStr) : jsonStr
    return JSON.stringify(parsed, null, 2)
  } catch (e) {
    return jsonStr
  }
}

const highlightJson = (jsonStr) => {
  const formatted = formatJson(jsonStr)
  if (formatted === '无数据 (No Data)') return '<span class="json-null">无数据 (No Data)</span>'
  return formatted
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+-]?\d+)?)/g, (match) => {
      let cls = 'json-number'
      if (/^"/.test(match)) {
        if (/:$/.test(match)) {
          cls = 'json-key'
        } else {
          cls = 'json-string'
        }
      } else if (/true|false/.test(match)) {
        cls = 'json-boolean'
      } else if (/null/.test(match)) {
        cls = 'json-null'
      }
      return `<span class="${cls}">${match}</span>`
    })
}

const getLineNumbers = (jsonStr) => {
  const formatted = formatJson(jsonStr)
  const lines = formatted.split('\n').length
  return Array.from({ length: lines }, (_, i) => i + 1).join('\n')
}

const keyTranslations = {
  tenantId: '租户 ID (Tenant)',
  sourceDomainId: '源网络域 (Source Domain)',
  targetDomainId: '目标网络域 (Target Domain)',
  sourceAgent: '源代理 (Source Agent)',
  targetAgent: '目标代理 (Target Agent)',
  taskId: '关联任务 ID (Task ID)',
  traceId: '链路追踪 ID (Trace ID)',
  messageType: '消息类型 (Type)',
  status: '状态 (Status)',
  latencyMs: '跨域时延 (Latency)',
  sentAt: '发送时间 (Sent At)',
  receivedAt: '接收时间 (Received At)',
  id: '消息编号 (ID)'
}

const formatFieldKey = (key) => {
  return keyTranslations[key] || key
}

const getParsedData = (jsonStr) => {
  if (!jsonStr) return {}
  try {
    return typeof jsonStr === 'string' ? JSON.parse(jsonStr) : jsonStr
  } catch (e) {
    return { value: jsonStr }
  }
}

const hasValidData = (jsonStr) => {
  if (!jsonStr || jsonStr === '无数据 (No Data)') return false
  try {
    const data = getParsedData(jsonStr)
    return Object.keys(data).length > 0
  } catch (e) {
    return false
  }
}

const copyText = async (text) => {
  if (!text || text === '无数据 (No Data)') return
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch (err) {
    ElMessage.error('复制失败')
  }
}

const getLatencyClass = (latency) => {
  if (!latency) return 'latency-normal'
  if (latency < 300) return 'latency-fast'
  if (latency < 1500) return 'latency-medium'
  return 'latency-slow'
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm:ss')
}

const getStatusType = (status) => {
  if (!status) return 'info'
  const s = status.toUpperCase()
  if (s === 'RESP' || s === 'SUCCESS') return 'success'
  if (s === 'TIMEOUT' || s === 'FAILED') return 'danger'
  if (s === 'REQ') return 'primary'
  return 'warning'
}

const getTypeTagType = (type) => {
  if (!type) return 'info'
  const t = type.toUpperCase()
  if (t === 'REQ') return ''
  if (t === 'RESP') return 'success'
  if (t === 'TIMEOUT') return 'danger'
  return 'warning'
}

onMounted(refresh)
</script>

<template>
  <PageContainer title="A2A 跨域审计" description="监控和分析跨越不同安全/网络域的 A2A 消息传递，审计数据流向、跨域时延与数据负载。">
    <!-- Filter form container -->
    <div class="glass-panel filter-panel">
      <ElForm :model="query" label-position="top" class="filter-form">
        <div class="filter-grid">
          <ElFormItem label="全链路 TraceId">
            <ElInput v-model="query.traceId" placeholder="输入 32 位 TraceId 进行精确搜索" clearable />
          </ElFormItem>
          <ElFormItem label="源网络域">
            <ElInput v-model="query.sourceDomainId" placeholder="源安全/网络域 ID" clearable />
          </ElFormItem>
          <ElFormItem label="目标网络域">
            <ElInput v-model="query.targetDomainId" placeholder="目标安全/网络域 ID" clearable />
          </ElFormItem>
          <ElFormItem label="消息类型">
            <ElSelect v-model="query.messageType" placeholder="全部" clearable style="width: 100%">
              <ElOption label="全部类型" value="" />
              <ElOption label="REQ (请求)" value="REQ" />
              <ElOption label="RESP (响应)" value="RESP" />
              <ElOption label="TIMEOUT (超时)" value="TIMEOUT" />
            </ElSelect>
          </ElFormItem>
          <ElFormItem label="消息状态">
            <ElSelect v-model="query.status" placeholder="全部" clearable style="width: 100%">
              <ElOption label="全部状态" value="" />
              <ElOption label="REQ (已发送)" value="REQ" />
              <ElOption label="RESP (已响应)" value="RESP" />
              <ElOption label="TIMEOUT (超时)" value="TIMEOUT" />
            </ElSelect>
          </ElFormItem>
          <ElFormItem label="发送时间范围" class="time-range-item">
            <ElDatePicker
              v-model="timeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              value-format="YYYY-MM-DDTHH:mm:ss"
              style="width: 100%"
            />
          </ElFormItem>
        </div>
        <div class="filter-actions">
          <ElButton type="primary" class="gradient-btn search-btn" @click="handleSearch">
            <ElIcon><Search /></ElIcon><span>筛选查询</span>
          </ElButton>
          <ElButton class="btn-secondary reset-btn" @click="handleReset">
            <ElIcon><Refresh /></ElIcon><span>重置条件</span>
          </ElButton>
        </div>
      </ElForm>
    </div>

    <!-- Table content container -->
    <div class="glass-panel table-panel">
      <ElTable v-loading="loading" :data="listRows" border stripe class="custom-table">
        <ElTableColumn prop="id" label="ID" width="90" align="center" />
        
        <ElTableColumn prop="traceId" label="全链路 Trace ID" min-width="170">
          <template #default="{ row }">
            <div class="trace-id-container">
              <router-link :to="'/admin/trace/' + row.traceId" class="trace-link font-mono" title="查看全链路执行时间线">
                {{ row.traceId }}
              </router-link>
              <ElButton type="primary" link class="trace-copy-btn" @click.stop="copyText(row.traceId)" title="复制 Trace ID">
                <ElIcon><CopyDocument /></ElIcon>
              </ElButton>
            </div>
          </template>
        </ElTableColumn>

        <ElTableColumn label="跨域数据流向 (Domain Flow)" min-width="220">
          <template #default="{ row }">
            <div class="flow-cell">
              <span class="flow-node source" :title="row.sourceDomainId">{{ row.sourceDomainId }}</span>
              <ElIcon class="flow-arrow"><Right /></ElIcon>
              <span class="flow-node target" :title="row.targetDomainId">{{ row.targetDomainId }}</span>
            </div>
          </template>
        </ElTableColumn>

        <ElTableColumn label="Agent 协同 (Agent Flow)" min-width="200">
          <template #default="{ row }">
            <div class="flow-cell select-none">
              <span class="agent-badge source-agent" :title="row.sourceAgent">{{ row.sourceAgent }}</span>
              <ElIcon class="flow-arrow-small"><Right /></ElIcon>
              <span class="agent-badge target-agent" :title="row.targetAgent">{{ row.targetAgent }}</span>
            </div>
          </template>
        </ElTableColumn>

        <ElTableColumn prop="messageType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <ElTag :type="getTypeTagType(row.messageType)" size="small" effect="dark" class="status-tag">
              {{ row.messageType }}
            </ElTag>
          </template>
        </ElTableColumn>

        <ElTableColumn prop="status" label="状态" width="110" align="center">
          <template #default="{ row }">
            <div class="status-badge-container">
              <ElTag :type="getStatusType(row.status)" size="small" effect="light" class="status-tag">
                <ElIcon class="status-icon">
                  <CircleCheck v-if="row.status === 'RESP' || row.status === 'SUCCESS'" />
                  <CircleClose v-else-if="row.status === 'TIMEOUT' || row.status === 'FAILED'" />
                  <Timer v-else />
                </ElIcon>
                {{ row.status }}
              </ElTag>
            </div>
          </template>
        </ElTableColumn>

        <ElTableColumn prop="latencyMs" label="跨域耗时" width="120" align="center">
          <template #default="{ row }">
            <div v-if="row.latencyMs !== null && row.latencyMs !== undefined" class="latency-cell" :class="getLatencyClass(row.latencyMs)">
              <ElIcon><Timer /></ElIcon>
              <span>{{ row.latencyMs }} ms</span>
            </div>
            <span v-else class="text-muted">-</span>
          </template>
        </ElTableColumn>

        <ElTableColumn prop="sentAt" label="发送时间" width="170" align="center">
          <template #default="{ row }">
            <span class="datetime-text">{{ formatDate(row.sentAt) }}</span>
          </template>
        </ElTableColumn>

        <ElTableColumn label="操作" width="90" align="center" fixed="right">
          <template #default="{ row }">
            <ElButton type="primary" link class="action-link" @click="showDetail(row)">
              <ElIcon class="mr-0.5"><View /></ElIcon>详情
            </ElButton>
          </template>
        </ElTableColumn>
        <template #empty>
          <ElEmpty description="未查询到任何 A2A 跨域消息审计记录" />
        </template>
      </ElTable>

      <div class="table-footer">
        <ElPagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50, 100]"
          :current-page="query.pageNo"
          :page-size="query.pageSize"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- Details Drawer -->
    <ElDrawer
      v-model="drawerVisible"
      title="A2A 跨域消息审计详情"
      size="65%"
      destroy-on-close
      class="a2a-drawer"
    >
      <div v-loading="detailLoading" class="drawer-inner">
        <template v-if="currentDetail">
          <!-- Top summary banner -->
          <div class="drawer-header-summary" :class="{ 'failed-banner': currentDetail.status === 'TIMEOUT' || currentDetail.status === 'FAILED' }">
            <div class="banner-top">
              <span class="banner-title">
                跨域传输 #{{ currentDetail.id }}
                <span class="banner-ver">{{ currentDetail.messageType }}</span>
              </span>
              <ElTag :type="getStatusType(currentDetail.status)" effect="dark" size="large" class="banner-tag">
                {{ currentDetail.status }}
              </ElTag>
            </div>
            <div class="banner-stats">
              <div class="stat-box">
                <span class="stat-lbl">数据方向</span>
                <span class="stat-val font-semibold">{{ currentDetail.sourceDomainId }} &rarr; {{ currentDetail.targetDomainId }}</span>
              </div>
              <div class="stat-box">
                <span class="stat-lbl">传输耗时</span>
                <span class="stat-val font-semibold" :class="getLatencyClass(currentDetail.latencyMs)">
                  {{ currentDetail.latencyMs !== null && currentDetail.latencyMs !== undefined ? currentDetail.latencyMs + ' ms' : '-' }}
                </span>
              </div>
              <div class="stat-box">
                <span class="stat-lbl">源 Agent</span>
                <span class="stat-val">{{ currentDetail.sourceAgent }}</span>
              </div>
              <div class="stat-box">
                <span class="stat-lbl">目标 Agent</span>
                <span class="stat-val">{{ currentDetail.targetAgent }}</span>
              </div>
            </div>
          </div>

          <!-- Split layout grid -->
          <div class="drawer-content-grid">
            <!-- Left Info Panel -->
            <div class="meta-section">
              <div class="meta-card-compact">
                <div class="meta-header-compact">
                  <ElIcon class="section-icon"><InfoFilled /></ElIcon>
                  <span>元数据审计 (Metadata)</span>
                </div>
                <div class="meta-grid-compact">
                  <div class="meta-item-compact">
                    <div class="meta-label-compact">
                      <ElIcon><User /></ElIcon>
                      <span>租户 ID</span>
                    </div>
                    <div class="meta-value-compact">{{ currentDetail.tenantId || '-' }}</div>
                  </div>
                  
                  <div class="meta-item-compact">
                    <div class="meta-label-compact">
                      <ElIcon><Cpu /></ElIcon>
                      <span>任务 ID (Task ID)</span>
                    </div>
                    <div class="meta-value-compact">
                      <span v-if="currentDetail.taskId" class="compact-badge-task">{{ currentDetail.taskId }}</span>
                      <span v-else>-</span>
                    </div>
                  </div>

                  <div class="meta-item-compact">
                    <div class="meta-label-compact">
                      <ElIcon><Connection /></ElIcon>
                      <span>链路追踪 (Trace ID)</span>
                    </div>
                    <div class="meta-value-compact">
                      <router-link :to="'/admin/trace/' + currentDetail.traceId" class="trace-link font-mono" style="font-size: 13px;">
                        {{ currentDetail.traceId }}
                      </router-link>
                    </div>
                  </div>

                  <div class="meta-item-compact">
                    <div class="meta-label-compact">
                      <ElIcon><Clock /></ElIcon>
                      <span>发送时间</span>
                    </div>
                    <div class="meta-value-compact datetime-display">{{ formatDate(currentDetail.sentAt) }}</div>
                  </div>

                  <div class="meta-item-compact">
                    <div class="meta-label-compact">
                      <ElIcon><Clock /></ElIcon>
                      <span>接收时间</span>
                    </div>
                    <div class="meta-value-compact datetime-display">{{ formatDate(currentDetail.receivedAt) }}</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Right Payload Viewer -->
            <div class="payloads-section">
              <div class="payload-box">
                <div class="box-toolbar">
                  <div class="toolbar-left">
                    <ElIcon class="json-icon"><Document /></ElIcon>
                    <span class="toolbar-title">payload.json</span>
                  </div>
                  <div class="toolbar-right">
                    <ElRadioGroup v-model="viewMode" size="small" class="mode-selector">
                      <ElRadioButton label="visual">可视化</ElRadioButton>
                      <ElRadioButton label="code">代码</ElRadioButton>
                    </ElRadioGroup>
                    <ElButton type="primary" size="small" link class="copy-btn" @click="copyText(formatJson(currentDetail.payload))">
                      <ElIcon><CopyDocument /></ElIcon> 复制负载内容
                    </ElButton>
                  </div>
                </div>
                <div class="payload-body">
                  <!-- Visual Mode -->
                  <div v-if="viewMode === 'visual'" class="visual-table-wrapper">
                    <div v-if="hasValidData(currentDetail.payload)" class="visual-property-grid">
                      <div v-for="(val, key) in getParsedData(currentDetail.payload)" :key="key" class="property-row">
                        <div class="property-label">
                          <span class="label-zh">{{ formatFieldKey(key) }}</span>
                          <span class="label-en">{{ key }}</span>
                        </div>
                        <div class="property-value">
                          <ElTag v-if="typeof val === 'boolean'" :type="val ? 'success' : 'danger'" size="default" effect="plain" class="val-tag">
                            {{ val ? '是 (True)' : '否 (False)' }}
                          </ElTag>
                          <span v-else-if="typeof val === 'number'" class="val-number-styled">{{ val }}</span>
                          <span v-else-if="val === null || val === undefined" class="val-null-styled">-</span>
                          <div v-else-if="typeof val === 'object'" class="val-object-styled">
                            <pre><code>{{ JSON.stringify(val, null, 2) }}</code></pre>
                          </div>
                          <span v-else class="val-string-styled">{{ val }}</span>
                        </div>
                      </div>
                    </div>
                    <div v-else class="empty-visual-data">
                      <ElEmpty description="无结构化数据展示" :image-size="60" />
                    </div>
                  </div>
                  <!-- Code Mode -->
                  <div v-else class="code-editor-layout">
                    <pre class="line-numbers-gutter"><code>{{ getLineNumbers(currentDetail.payload) }}</code></pre>
                    <pre class="code-pre"><code v-html="highlightJson(currentDetail.payload)"></code></pre>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
        <el-empty v-else description="无数据展示" />
      </div>
    </ElDrawer>
  </PageContainer>
</template>

<style scoped>
/* Glassmorphism style panels */
.glass-panel {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(225, 235, 245, 0.8);
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.03);
  padding: 24px;
  margin-bottom: 20px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.glass-panel:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.05);
}

/* Filter panel configurations */
.filter-panel {
  padding-bottom: 16px;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px 20px;
}

/* Time range input covers more width */
.time-range-item {
  grid-column: span 2;
}

@media (max-width: 992px) {
  .time-range-item {
    grid-column: span 1;
  }
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
  border-top: 1px dashed rgba(0, 0, 0, 0.06);
  padding-top: 16px;
}

/* Premium Gradient Button styles */
.gradient-btn {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%) !important;
  border: none !important;
  color: #ffffff !important;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.25) !important;
  font-weight: 500 !important;
  transition: all 0.3s ease !important;
}

.gradient-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.35) !important;
}

.gradient-btn:active {
  transform: translateY(0);
}

.btn-secondary {
  border: 1px solid #d9e1ec !important;
  background: #ffffff !important;
  color: #4b5563 !important;
  transition: all 0.3s ease !important;
}

.btn-secondary:hover {
  color: #2563eb !important;
  border-color: #2563eb !important;
  background: rgba(37, 99, 235, 0.04) !important;
  transform: translateY(-2px);
}

/* Custom table styling */
.custom-table {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.01);
}

.custom-table :deep(.el-table__header-wrapper) th {
  background-color: #f8fafc !important;
  color: #374151;
  font-weight: 600;
}

.trace-id-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.trace-link {
  color: #2563eb;
  text-decoration: none;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  margin-right: 4px;
}

.trace-link:hover {
  text-decoration: underline;
  color: #1d4ed8;
}

.trace-copy-btn {
  padding: 2px;
  height: auto;
  font-size: 14px;
  color: #94a3b8 !important;
}

.trace-copy-btn:hover {
  color: #2563eb !important;
}

.flow-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: monospace;
}

.flow-node {
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 600;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.flow-node.source {
  background: #eff6ff;
  color: #1e40af;
  border: 1px solid #bfdbfe;
}

.flow-node.target {
  background: #f0fdf4;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.flow-arrow {
  color: #94a3b8;
  font-size: 16px;
  flex-shrink: 0;
}

.flow-arrow-small {
  color: #cbd5e1;
  font-size: 12px;
  flex-shrink: 0;
}

.agent-badge {
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 12px;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.agent-badge.source-agent {
  background: #f1f5f9;
  color: #475569;
  border: 1px solid #e2e8f0;
}

.agent-badge.target-agent {
  background: #faf5ff;
  color: #6b21a8;
  border: 1px solid #f3e8ff;
}

.status-tag {
  font-weight: 500;
}

.status-icon {
  margin-right: 4px;
  vertical-align: middle;
}

.latency-cell {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-family: monospace;
  font-weight: 600;
}

.latency-fast {
  color: #10b981;
}

.latency-medium {
  color: #f59e0b;
}

.latency-slow {
  color: #ef4444;
}

.datetime-text {
  color: #4b5563;
  font-size: 13px;
}

.action-link {
  font-weight: 600;
  transition: transform 0.2s ease;
}

.action-link:hover {
  transform: scale(1.08);
}

.table-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* Drawer styles */
.drawer-inner {
  padding: 0 4px;
  height: 100%;
}

.drawer-header-summary {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e2e8f0;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
}

.drawer-header-summary.failed-banner {
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
  border-color: #fca5a5;
}

.drawer-header-summary::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: #2563eb;
}

.drawer-header-summary.failed-banner::before {
  background: #ef4444;
}

.banner-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.banner-title {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}

.banner-ver {
  font-size: 13px;
  color: #2563eb;
  font-weight: 600;
  margin-left: 8px;
  background: rgba(37, 99, 235, 0.08);
  padding: 2px 8px;
  border-radius: 4px;
}

.banner-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-box {
  display: flex;
  flex-direction: column;
}

.stat-lbl {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 4px;
}

.stat-val {
  font-size: 15px;
  font-weight: 600;
  color: #334155;
}

/* Grid splits inside drawer */
.drawer-content-grid {
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 24px;
  align-items: start;
}

@media (max-width: 1200px) {
  .drawer-content-grid {
    grid-template-columns: 1fr;
  }
}

/* Compact Metadata Card Style */
.meta-card-compact {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
  overflow: hidden;
  transition: all 0.3s ease;
}

.meta-card-compact:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.04);
  border-color: #cbd5e1;
}

.meta-header-compact {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  padding: 16px;
  font-weight: 700;
  color: #0f172a;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
}

.section-icon {
  color: #2563eb;
  font-size: 18px;
}

.meta-grid-compact {
  padding: 8px 0;
}

.meta-item-compact {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-bottom: 1px solid #f8fafc;
}

.meta-item-compact:last-child {
  border-bottom: none;
}

.meta-label-compact {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
  font-size: 13px;
  font-weight: 500;
}

.meta-label-compact .el-icon {
  font-size: 14px;
  color: #94a3b8;
}

.meta-value-compact {
  font-size: 14px;
  color: #1e293b;
  font-weight: 600;
  word-break: break-all;
}

.datetime-display {
  color: #475569;
  font-weight: 500;
  font-size: 13px;
}

.compact-badge-task {
  background: rgba(37, 99, 235, 0.08);
  color: #2563eb;
  padding: 3px 10px;
  border-radius: 20px;
  font-family: Consolas, Monaco, monospace;
  font-size: 13px;
}

/* Payload content */
.payloads-section {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.payload-box {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

.box-toolbar {
  background: #f8fafc;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e2e8f0;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.json-icon {
  color: #2563eb;
  font-size: 14px;
}

.toolbar-title {
  color: #334155;
  font-size: 13px;
  font-family: Consolas, Monaco, monospace;
  font-weight: 600;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.mode-selector :deep(.el-radio-button__inner) {
  font-weight: 500;
}

.copy-btn {
  font-size: 12px;
  color: #2563eb !important;
  font-weight: 600 !important;
}

.copy-btn:hover {
  color: #1d4ed8 !important;
}

/* Visual Table styles */
.visual-property-grid {
  background: #ffffff;
}

.property-row {
  display: grid;
  grid-template-columns: 200px 1fr;
  border-bottom: 1px solid #f1f5f9;
  align-items: center;
  transition: background-color 0.2s ease;
}

.property-row:hover {
  background-color: #f8fafc;
}

.property-row:last-child {
  border-bottom: none;
}

.property-label {
  padding: 12px 20px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  border-right: 1px solid #f1f5f9;
  align-self: stretch;
  justify-content: center;
}

.label-zh {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
}

.label-en {
  font-size: 11px;
  color: #94a3b8;
  font-family: Consolas, Monaco, monospace;
}

.property-value {
  padding: 12px 20px;
  font-size: 13px;
  color: #334155;
  word-break: break-all;
}

.val-tag {
  font-weight: 600;
}

.val-number-styled {
  font-family: Consolas, Monaco, monospace;
  font-weight: 700;
  color: #ea580c;
  background: #fff7ed;
  padding: 2px 6px;
  border-radius: 4px;
  border: 1px solid #ffedd5;
  display: inline-block;
}

.val-null-styled {
  color: #94a3b8;
  font-style: italic;
}

.val-string-styled {
  font-weight: 500;
  color: #334155;
}

.val-object-styled {
  background: #0f172a;
  border: 1px solid #334155;
  border-radius: 6px;
  padding: 12px;
  max-height: 200px;
  overflow: auto;
}

.val-object-styled pre {
  margin: 0;
}

.val-object-styled code {
  font-family: Consolas, Monaco, monospace;
  font-size: 12px;
  color: #e2e8f0;
}

.empty-visual-data {
  padding: 40px;
  text-align: center;
  background: #ffffff;
}

/* Code Editor Layout */
.code-editor-layout {
  display: flex;
  background: #0f172a;
  font-family: Consolas, Monaco, 'Andale Mono', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.6;
  max-height: 520px;
  overflow: hidden;
}

.line-numbers-gutter {
  padding: 16px 12px;
  background: #1e293b;
  color: #475569;
  text-align: right;
  user-select: none;
  border-right: 1px solid #334155;
  margin: 0;
  min-width: 38px;
  font-family: inherit;
}

.code-pre {
  padding: 16px;
  margin: 0;
  overflow: auto;
  flex: 1;
  background: transparent;
  font-family: inherit;
}

.code-pre code {
  font-family: inherit;
  font-size: inherit;
  color: #e2e8f0;
  white-space: pre;
  word-break: normal;
}

/* Syntax Highlighting Colors */
:deep(.json-key) {
  color: #f87171; /* soft rose red */
  font-weight: 600;
}

:deep(.json-string) {
  color: #34d399; /* vibrant emerald green */
}

:deep(.json-number) {
  color: #fb923c; /* orange */
}

:deep(.json-boolean) {
  color: #818cf8; /* indigo blue */
  font-weight: bold;
}

:deep(.json-null) {
  color: #64748b; /* slate gray */
  font-style: italic;
}
</style>
