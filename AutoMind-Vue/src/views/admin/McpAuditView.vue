<script setup>
import { onMounted, reactive, ref } from 'vue'
import { listMcpCallLogs, getMcpCallLog } from '@/api/mcp'
import PageContainer from '@/components/common/PageContainer.vue'
import TraceIdTag from '@/components/common/TraceIdTag.vue'
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
  Document
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const listRows = ref([])
const total = ref(0)
const loading = ref(false)

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  traceId: '',
  agentTaskId: null,
  toolName: '',
  errorType: '',
  isHighRisk: '',
  startTime: null,
  endTime: null,
})

const timeRange = ref([])

// Drawer state
const drawerVisible = ref(false)
const activeTab = ref('request')
const detailLoading = ref(false)
const currentDetail = ref(null)

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
    agentTaskId: query.agentTaskId || undefined,
    toolName: query.toolName || undefined,
    errorType: query.errorType || undefined,
    isHighRisk: query.isHighRisk !== '' ? query.isHighRisk : undefined,
    startTime: query.startTime || undefined,
    endTime: query.endTime || undefined
  }

  try {
    const res = await listMcpCallLogs(params)
    listRows.value = res?.list || res?.records || []
    total.value = res?.total || 0
  } catch (error) {
    console.error('Failed to load MCP call logs:', error)
    ElMessage.error('加载调用日志失败')
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
  query.agentTaskId = null
  query.toolName = ''
  query.errorType = ''
  query.isHighRisk = ''
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
  activeTab.value = 'request'
  try {
    const res = await getMcpCallLog(row.id)
    currentDetail.value = res
  } catch (error) {
    console.error('Failed to load detail:', error)
    ElMessage.error('加载详情失败')
  } finally {
    detailLoading.value = false
  }
}

const formatJson = (jsonStr) => {
  if (!jsonStr) return '无数据 (No Data)'
  try {
    const parsed = typeof jsonStr === 'string' ? JSON.parse(jsonStr) : jsonStr
    if (parsed && typeof parsed === 'object') {
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
      scrub(parsed)
    }
    return JSON.stringify(parsed, null, 2)
  } catch (e) {
    if (typeof jsonStr === 'string') {
      return jsonStr.replace(/"traceId"\s*:\s*"[^"]*"/g, '"traceId":"<hidden>"').replace(/"trace_id"\s*:\s*"[^"]*"/g, '"trace_id":"<hidden>"')
    }
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

const viewMode = ref('visual')

const keyTranslations = {
  linkId: '链路 ID',
  priority: '优先级',
  reservedBandwidthMbps: '预留带宽 (Mbps)',
  latencyMs: '延迟时长',
  errorType: '错误类型',
  status: '状态码',
  code: '状态码',
  message: '响应消息',
  tenantId: '租户 ID',
  agentTaskId: '任务 ID',
  intentId: '意图 ID',
  toolName: '工具名称',
  toolVersion: '工具版本',
  result: '执行结果',
  success: '是否成功',
  data: '返回数据',
  records: '数据记录',
  list: '列表数据',
  total: '总记录数',
  error: '错误信息',
  msg: '提示消息',
  id: '流水号 (ID)'
}

const formatFieldKey = (key) => {
  return keyTranslations[key] || key
}

const getParsedData = (jsonStr) => {
  if (!jsonStr) return {}
  try {
    const parsed = typeof jsonStr === 'string' ? JSON.parse(jsonStr) : jsonStr
    if (parsed && typeof parsed === 'object') {
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
      scrub(parsed)
    }
    return parsed
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

onMounted(refresh)
</script>

<template>
  <PageContainer title="MCP 调用审计" description="监控和分析 MCP (Model Context Protocol) 服务的调用日志，审计高风险操作和接口性能。">
    <!-- Filter form container -->
    <div class="glass-panel filter-panel">
      <ElForm :model="query" label-position="top" class="filter-form">
        <div class="filter-grid">
          <ElFormItem label="Agent 任务 ID">
            <ElInput v-model.number="query.agentTaskId" placeholder="任务关联 ID" clearable />
          </ElFormItem>
          <ElFormItem label="工具名称">
            <ElInput v-model="query.toolName" placeholder="MCP 工具名" clearable />
          </ElFormItem>
          <ElFormItem label="错误类型">
            <ElInput v-model="query.errorType" placeholder="异常类型，如 TIMEOUT" clearable />
          </ElFormItem>
          <ElFormItem label="安全等级">
            <ElSelect v-model="query.isHighRisk" placeholder="全部" clearable style="width: 100%">
              <ElOption label="全部等级" value="" />
              <ElOption label="高风险工具 (High Risk)" :value="true" />
              <ElOption label="普通工具 (Standard)" :value="false" />
            </ElSelect>
          </ElFormItem>
          <ElFormItem label="调用时间范围" class="time-range-item">
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
        <ElTableColumn prop="agentTaskId" label="任务 ID" width="110" align="center">
          <template #default="{ row }">
            <span v-if="row.agentTaskId" class="task-id-badge">{{ row.agentTaskId }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="toolName" label="MCP 工具详情" min-width="200">
          <template #default="{ row }">
            <div class="tool-info-cell">
              <span class="tool-name">{{ row.toolName }}</span>
              <span v-if="row.toolVersion" class="tool-ver">v{{ row.toolVersion }}</span>
              <span v-if="row.isHighRisk" class="risk-badge animate-pulse">高风险</span>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="attemptNo" label="尝试" width="80" align="center">
          <template #default="{ row }">
            <ElTag size="small" type="info" effect="plain" class="attempt-tag">
              第 {{ row.attemptNo || 1 }} 次
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="responseCode" label="响应状态" width="130" align="center">
          <template #default="{ row }">
            <div class="status-badge-container">
              <ElTag :type="row.responseCode === 200 && !row.errorType ? 'success' : 'danger'" size="small" effect="light" class="status-tag">
                <ElIcon class="status-icon">
                  <CircleCheck v-if="row.responseCode === 200 && !row.errorType" />
                  <CircleClose v-else />
                </ElIcon>
                {{ row.responseCode === 200 && !row.errorType ? 'Success' : 'Error' }}
              </ElTag>
              <div v-if="row.errorType" class="error-label" :title="row.errorType">{{ row.errorType }}</div>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="latencyMs" label="耗时 (Latency)" width="130" align="center">
          <template #default="{ row }">
            <div class="latency-cell" :class="getLatencyClass(row.latencyMs)">
              <ElIcon><Timer /></ElIcon>
              <span>{{ row.latencyMs }} ms</span>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="calledAt" label="调用时间" width="180" align="center">
          <template #default="{ row }">
            <span class="datetime-text">{{ formatDate(row.calledAt) }}</span>
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
          <ElEmpty description="未查询到任何 MCP 调用审计记录" />
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
      title="MCP 调用日志审计详情"
      size="65%"
      destroy-on-close
      class="mcp-drawer"
    >
      <div v-loading="detailLoading" class="drawer-inner">
        <template v-if="currentDetail">
          <!-- Top summary banner -->
          <div class="drawer-header-summary" :class="{ 'failed-banner': currentDetail.errorType || currentDetail.responseCode !== 200 }">
            <div class="banner-top">
              <span class="banner-title">
                {{ currentDetail.toolName }}
                <span class="banner-ver" v-if="currentDetail.toolVersion">v{{ currentDetail.toolVersion }}</span>
              </span>
              <ElTag :type="currentDetail.responseCode === 200 && !currentDetail.errorType ? 'success' : 'danger'" effect="dark" size="large" class="banner-tag">
                {{ currentDetail.responseCode === 200 && !currentDetail.errorType ? 'SUCCESS' : 'FAILED' }}
              </ElTag>
            </div>
            <div class="banner-stats">
              <div class="stat-box">
                <span class="stat-lbl">调用 ID</span>
                <span class="stat-val">#{{ currentDetail.id }}</span>
              </div>
              <div class="stat-box">
                <span class="stat-lbl">执行耗时</span>
                <span class="stat-val font-semibold" :class="getLatencyClass(currentDetail.latencyMs)">{{ currentDetail.latencyMs }} ms</span>
              </div>
              <div class="stat-box">
                <span class="stat-lbl">重试轮次</span>
                <span class="stat-val">第 {{ currentDetail.attemptNo || 1 }} 次</span>
              </div>
              <div class="stat-box">
                <span class="stat-lbl">安全等级</span>
                <span class="stat-val">
                  <span :class="currentDetail.isHighRisk ? 'text-danger' : 'text-success'">
                    {{ currentDetail.isHighRisk ? '高风险工具' : '普通工具' }}
                  </span>
                </span>
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
                  <span>审计元数据 (Metadata)</span>
                </div>
                <div class="meta-grid-compact">
                  <div class="meta-item-compact">
                    <div class="meta-label-compact">
                      <ElIcon><User /></ElIcon>
                      <span>租户 ID (Tenant)</span>
                    </div>
                    <div class="meta-value-compact copyable-text" @click="copyText(currentDetail.tenantId)">
                      {{ currentDetail.tenantId || '-' }}
                    </div>
                  </div>
                  
                  <div class="meta-item-compact">
                    <div class="meta-label-compact">
                      <ElIcon><Cpu /></ElIcon>
                      <span>Agent 任务 ID</span>
                    </div>
                    <div class="meta-value-compact">
                      <span class="compact-badge-task">{{ currentDetail.agentTaskId || '-' }}</span>
                    </div>
                  </div>

                  <div class="meta-item-compact">
                    <div class="meta-label-compact">
                      <ElIcon><Aim /></ElIcon>
                      <span>意图 ID (Intent ID)</span>
                    </div>
                    <div class="meta-value-compact">
                      <span class="compact-badge-intent">{{ currentDetail.intentId || '-' }}</span>
                    </div>
                  </div>

                  <div class="meta-item-compact">
                    <div class="meta-label-compact">
                      <ElIcon>
                        <CircleCheck v-if="currentDetail.responseCode === 200 && !currentDetail.errorType" />
                        <CircleClose v-else />
                      </ElIcon>
                      <span>HTTP 状态码</span>
                    </div>
                    <div class="meta-value-compact">
                      <ElTag :type="currentDetail.responseCode === 200 && !currentDetail.errorType ? 'success' : 'danger'" size="small" effect="dark" class="compact-status-tag">
                        {{ currentDetail.responseCode }}
                      </ElTag>
                    </div>
                  </div>

                  <div class="meta-item-compact" v-if="currentDetail.errorType">
                    <div class="meta-label-compact">
                      <ElIcon><Warning /></ElIcon>
                      <span>错误类型</span>
                    </div>
                    <div class="meta-value-compact error-text font-mono">{{ currentDetail.errorType }}</div>
                  </div>

                  <div class="meta-item-compact">
                    <div class="meta-label-compact">
                      <ElIcon><Clock /></ElIcon>
                      <span>调用时刻</span>
                    </div>
                    <div class="meta-value-compact datetime-display">{{ formatDate(currentDetail.calledAt) }}</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Right Payload Tabs -->
            <div class="payloads-section">
              <ElTabs v-model="activeTab" type="card" class="modern-tabs">
                <ElTabPane name="request">
                  <template #label>
                    <span class="tab-label-inner">
                      <span>请求参数 (Request)</span>
                    </span>
                  </template>
                  <div class="payload-box">
                    <div class="box-toolbar">
                      <div class="toolbar-left">
                        <ElIcon class="json-icon"><Document /></ElIcon>
                        <span class="toolbar-title">request_payload.json</span>
                      </div>
                      <div class="toolbar-right">
                        <ElRadioGroup v-model="viewMode" size="small" class="mode-selector">
                          <ElRadioButton label="visual">可视化</ElRadioButton>
                          <ElRadioButton label="code">代码</ElRadioButton>
                        </ElRadioGroup>
                        <ElButton type="primary" size="small" link class="copy-btn" @click="copyText(formatJson(currentDetail.requestParams))">
                          <ElIcon><CopyDocument /></ElIcon> 复制内容
                        </ElButton>
                      </div>
                    </div>
                    <div class="payload-body">
                      <!-- Visual Mode -->
                      <div v-if="viewMode === 'visual'" class="visual-table-wrapper">
                        <div v-if="hasValidData(currentDetail.requestParams)" class="visual-property-grid">
                          <div v-for="(val, key) in getParsedData(currentDetail.requestParams)" :key="key" class="property-row">
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
                        <pre class="line-numbers-gutter"><code>{{ getLineNumbers(currentDetail.requestParams) }}</code></pre>
                        <pre class="code-pre"><code v-html="highlightJson(currentDetail.requestParams)"></code></pre>
                      </div>
                    </div>
                  </div>
                </ElTabPane>
                <ElTabPane name="response">
                  <template #label>
                    <span class="tab-label-inner">
                      <span>返回响应 (Response)</span>
                    </span>
                  </template>
                  <div class="payload-box">
                    <div class="box-toolbar">
                      <div class="toolbar-left">
                        <ElIcon class="json-icon"><Document /></ElIcon>
                        <span class="toolbar-title">response_payload.json</span>
                      </div>
                      <div class="toolbar-right">
                        <ElRadioGroup v-model="viewMode" size="small" class="mode-selector">
                          <ElRadioButton label="visual">可视化</ElRadioButton>
                          <ElRadioButton label="code">代码</ElRadioButton>
                        </ElRadioGroup>
                        <ElButton type="primary" size="small" link class="copy-btn" @click="copyText(formatJson(currentDetail.responseBody))">
                          <ElIcon><CopyDocument /></ElIcon> 复制内容
                        </ElButton>
                      </div>
                    </div>
                    <div class="payload-body">
                      <!-- Visual Mode -->
                      <div v-if="viewMode === 'visual'" class="visual-table-wrapper">
                        <div v-if="hasValidData(currentDetail.responseBody)" class="visual-property-grid">
                          <div v-for="(val, key) in getParsedData(currentDetail.responseBody)" :key="key" class="property-row">
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
                        <pre class="line-numbers-gutter"><code>{{ getLineNumbers(currentDetail.responseBody) }}</code></pre>
                        <pre class="code-pre"><code v-html="highlightJson(currentDetail.responseBody)"></code></pre>
                      </div>
                    </div>
                  </div>
                </ElTabPane>
              </ElTabs>
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

.tool-info-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.tool-name {
  font-weight: 600;
  color: #1f2937;
}

.tool-ver {
  font-size: 11px;
  color: #9ca3af;
  background: #f3f4f6;
  padding: 1px 4px;
  border-radius: 4px;
}

.risk-badge {
  font-size: 11px;
  font-weight: 600;
  color: #ffffff;
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  padding: 2px 6px;
  border-radius: 4px;
  box-shadow: 0 2px 6px rgba(239, 68, 68, 0.2);
}

/* Pulse animation for high risk indicator */
.animate-pulse {
  animation: pulse-glow 2s infinite alternate;
}

@keyframes pulse-glow {
  0% {
    box-shadow: 0 0 4px rgba(239, 68, 68, 0.4);
    opacity: 0.9;
  }
  100% {
    box-shadow: 0 0 10px rgba(239, 68, 68, 0.8);
    opacity: 1;
  }
}

.task-id-badge {
  background-color: rgba(37, 99, 235, 0.08);
  color: #2563eb;
  padding: 3px 8px;
  border-radius: 4px;
  font-family: monospace;
  font-weight: 600;
}

.status-badge-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.status-tag {
  font-weight: 500;
}

.status-icon {
  margin-right: 4px;
  vertical-align: middle;
}

.error-label {
  font-size: 11px;
  color: #ef4444;
  font-family: monospace;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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

.attempt-tag {
  font-weight: 500;
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
  color: #64748b;
  font-weight: 400;
  margin-left: 6px;
  background: rgba(0, 0, 0, 0.05);
  padding: 2px 6px;
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

.meta-value-compact.copyable-text {
  cursor: pointer;
  color: #2563eb;
  transition: color 0.2s ease;
}

.meta-value-compact.copyable-text:hover {
  color: #1d4ed8;
  text-decoration: underline;
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

.compact-badge-intent {
  background: rgba(16, 185, 129, 0.08);
  color: #10b981;
  padding: 3px 10px;
  border-radius: 20px;
  font-family: Consolas, Monaco, monospace;
  font-size: 13px;
}

.compact-status-tag {
  font-family: Consolas, Monaco, monospace;
  font-weight: 700;
}

.error-text {
  color: #ef4444;
  font-family: Consolas, Monaco, monospace;
  font-weight: 600;
  background-color: #fef2f2;
  padding: 2px 6px;
  border-radius: 4px;
  border: 1px solid #fee2e2;
}

/* Payload content */
.payloads-section {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.modern-tabs :deep(.el-tabs__item) {
  font-weight: 600;
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
