import AppLayout from '@/components/layout/AppLayout.vue'
import { ROLE } from '@/utils/constants'

const viewModules = import.meta.glob('@/views/**/*.vue')
const view = (path) => viewModules[`/src/views/${path}.vue`]

export const tenantMenus = [
  { path: '/dashboard', title: '总览首页', icon: 'DataBoard' },
  { path: '/tenant/intent/submit', title: '提交意图', icon: 'EditPen' },
  { path: '/tenant/intents', title: '我的意图列表', icon: 'Tickets' },
  { path: '/tenant/topology', title: '我的拓扑', icon: 'Share' },
  { path: '/tenant/approvals', title: '我的审批进度', icon: 'Checked' },
]

export const adminMenus = [
  { path: '/dashboard', title: '总览首页', icon: 'DataBoard' },
  { path: '/admin/approvals/pending', title: '待审批队列', icon: 'Bell' },
  { path: '/admin/approvals/history', title: '审批历史', icon: 'Clock' },
  { path: '/admin/topology', title: '全局拓扑', icon: 'Share' },
  { path: '/admin/sla', title: '全局 SLA 仪表盘', icon: 'TrendCharts' },
  { path: '/admin/agent-tasks', title: 'Agent 任务监控', icon: 'Cpu' },
  { path: '/admin/mcp-audit', title: 'MCP 调用审计', icon: 'Operation' },
  { path: '/admin/a2a-audit', title: 'A2A 跨域审计', icon: 'Connection' },
  { path: '/admin/healing', title: '自愈记录', icon: 'RefreshRight' },
  // { path: '/admin/trace', title: '全链路追踪', icon: 'Guide' },
  // { path: '/admin/audit-logs', title: '审计日志', icon: 'DocumentChecked' },
]

export const routes = [
  {
    path: '/login',
    name: 'Login',
    component: view('auth/LoginView'),
    meta: { role: ROLE.PUBLIC, title: '登录' },
  },
  {
    path: '/',
    component: AppLayout,
    meta: { role: ROLE.AUTH },
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', name: 'Dashboard', component: view('shared/DashboardView'), meta: { role: ROLE.AUTH, title: '总览首页' } },
      { path: '403', name: 'Forbidden', component: view('shared/ForbiddenView'), meta: { role: ROLE.AUTH, title: '权限不足' } },
      { path: 'topology/link/:linkId', name: 'LinkDetail', component: view('shared/LinkDetailView'), meta: { role: ROLE.AUTH, title: '链路详情' } },
      { path: 'tenant/intent/submit', name: 'TenantIntentSubmit', component: view('tenant/IntentSubmitView'), meta: { role: ROLE.TENANT, title: '提交意图' } },
      { path: 'tenant/intents', name: 'TenantIntents', component: view('tenant/MyIntentsView'), meta: { role: ROLE.TENANT, title: '我的意图列表' } },
      { path: 'tenant/intents/:intentId', name: 'TenantIntentDetail', component: view('tenant/MyIntentDetailView'), meta: { role: ROLE.TENANT, title: '意图详情' } },
      { path: 'tenant/topology', name: 'TenantTopology', component: view('tenant/MyTopologyView'), meta: { role: ROLE.TENANT, title: '我的拓扑' } },
      { path: 'tenant/sla', name: 'TenantSla', component: view('tenant/MySlaView'), meta: { role: ROLE.TENANT, title: '我的 SLA 仪表盘' } },
      { path: 'tenant/approvals', name: 'TenantApprovals', component: view('tenant/MyApprovalsView'), meta: { role: ROLE.TENANT, title: '我的审批进度' } },
      { path: 'tenant/healing', name: 'TenantHealing', component: view('tenant/MyHealingView'), meta: { role: ROLE.TENANT, title: '我的自愈记录' } },
      { path: 'admin/approvals/pending', name: 'AdminApprovalPending', component: view('admin/ApprovalQueueView'), meta: { role: ROLE.ADMIN, title: '待审批队列' } },
      { path: 'admin/approvals/history', name: 'AdminApprovalHistory', component: view('admin/ApprovalHistoryView'), meta: { role: ROLE.ADMIN, title: '审批历史' } },
      { path: 'admin/approvals/:id', name: 'AdminApprovalDetail', component: view('admin/ApprovalDetailView'), meta: { role: ROLE.ADMIN, title: '审批详情' } },
      { path: 'admin/topology', name: 'AdminTopology', component: view('admin/GlobalTopologyView'), meta: { role: ROLE.ADMIN, title: '全局拓扑' } },
      { path: 'admin/sla', name: 'AdminSla', component: view('admin/GlobalSlaView'), meta: { role: ROLE.ADMIN, title: '全局 SLA 仪表盘' } },
      { path: 'admin/agent-tasks', name: 'AdminAgentTasks', component: view('admin/AgentTasksView'), meta: { role: ROLE.ADMIN, title: 'Agent 任务监控' } },
      { path: 'admin/mcp-audit', name: 'AdminMcpAudit', component: view('admin/McpAuditView'), meta: { role: ROLE.ADMIN, title: 'MCP 调用审计' } },
      { path: 'admin/a2a-audit', name: 'AdminA2aAudit', component: view('admin/A2aAuditView'), meta: { role: ROLE.ADMIN, title: 'A2A 跨域审计' } },
      { path: 'admin/healing', name: 'AdminHealing', component: view('admin/HealingRecordsView'), meta: { role: ROLE.ADMIN, title: '自愈记录' } },
      { path: 'admin/trace/:intentId', name: 'AdminTraceByIntent', component: view('admin/FullTraceView'), meta: { role: ROLE.ADMIN, title: '全链路追踪' } },
      { path: 'admin/trace', name: 'AdminTrace', component: view('admin/FullTraceView'), meta: { role: ROLE.ADMIN, title: '全链路追踪' } },
      // { path: 'admin/audit-logs', name: 'AdminAuditLogs', component: view('admin/AuditLogsView'), meta: { role: ROLE.ADMIN, title: '审计日志' } },
      { path: ':catchAll(.*)', name: 'NotFound', component: view('shared/NotFoundView'), meta: { role: ROLE.AUTH, title: '页面不存在' } },
    ],
  },
]
