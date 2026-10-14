<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Aim,
  CircleCheckFilled,
  Connection,
  Cpu,
  FullScreen,
  Grid,
  Minus,
  Plus,
  Refresh,
  ScaleToOriginal,
  Share,
  WarningFilled,
} from '@element-plus/icons-vue'

const CANVAS_WIDTH = 1420
const CANVAS_HEIGHT = 680
const NODE_WIDTH = 250
const NODE_HEIGHT = 102
const CENTER_WIDTH = 260
const CENTER_HEIGHT = 112
const DOMAIN_PADDING = 28

const props = defineProps({
  domains: {
    type: Array,
    default: () => [],
  },
  nodes: {
    type: Array,
    default: () => [],
  },
  links: {
    type: Array,
    default: () => [],
  },
  linkResources: {
    type: Array,
    default: () => [],
  },
  height: {
    type: Number,
    default: 640,
  },
})

const emit = defineEmits(['node-click', 'link-click', 'domain-click'])

const zoom = ref(1)
const positions = ref({})
const dragging = ref(null)
const dragMoved = ref(false)
const layoutKey = computed(() => `automind.topology.layout.${props.nodes.map((node) => node.id).sort().join('|')}`)

const resourcesByLink = computed(() => {
  const map = new Map()
  props.linkResources.forEach((resource) => {
    if (resource?.linkId) map.set(resource.linkId, resource)
  })
  return map
})

const nodesById = computed(() => {
  const map = new Map()
  props.nodes.forEach((node) => {
    if (node?.id) map.set(node.id, node)
  })
  return map
})

const domainsById = computed(() => {
  const map = new Map()
  props.domains.forEach((domain) => {
    if (domain?.id) map.set(domain.id, domain)
  })
  return map
})

const visibleDomains = computed(() => props.domains.filter((domain) => !isCenterDomain(domain)))
const centerDomain = computed(() => props.domains.find(isCenterDomain))

const domainGroups = computed(() => visibleDomains.value.map((domain) => ({
  domain,
  nodes: props.nodes.filter((node) => node.domainId === domain.id),
})))

const orphanNodes = computed(() => props.nodes.filter((node) => node.domainId && !domainsById.value.has(node.domainId)))

const centerNode = computed(() => {
  if (centerDomain.value) {
    return {
      id: `domain:${centerDomain.value.id}`,
      type: 'center',
      source: centerDomain.value,
      title: domainTitle(centerDomain.value),
      subtitle: 'GLOBAL VIEW',
      status: centerDomain.value.status || 'ACTIVE',
      width: CENTER_WIDTH,
      height: CENTER_HEIGHT,
    }
  }

  return {
    id: 'domain:global-view',
    type: 'center',
    source: null,
    title: '全局拓扑视图',
    subtitle: 'GLOBAL VIEW',
    status: 'ACTIVE',
    width: CENTER_WIDTH,
    height: CENTER_HEIGHT,
  }
})

const graphNodes = computed(() => [
  centerNode.value,
  ...props.nodes.map((node) => ({
    id: node.id,
    type: 'node',
    source: node,
    title: nodeTitle(node),
    subtitle: `${node.nodeType || '-'} / ${node.location || '-'}`,
    status: node.status,
    width: NODE_WIDTH,
    height: NODE_HEIGHT,
  })),
])

const nodeViews = computed(() => graphNodes.value.map((item) => ({
  ...item,
  x: positions.value[item.id]?.x ?? CANVAS_WIDTH / 2 - item.width / 2,
  y: positions.value[item.id]?.y ?? CANVAS_HEIGHT / 2 - item.height / 2,
})))

const nodeViewById = computed(() => {
  const map = new Map()
  nodeViews.value.forEach((node) => map.set(node.id, node))
  return map
})

const domainViews = computed(() => {
  const views = domainGroups.value.map((group) => {
    const memberViews = group.nodes.map((node) => nodeViewById.value.get(node.id)).filter(Boolean)
    const slot = domainSlot(group.domain)
    if (!memberViews.length) {
      return {
        id: group.domain.id,
        domain: group.domain,
        x: slot.x,
        y: slot.y,
        width: slot.width,
        height: slot.height,
      }
    }

    const minX = Math.min(...memberViews.map((node) => node.x)) - DOMAIN_PADDING
    const minY = Math.min(...memberViews.map((node) => node.y)) - DOMAIN_PADDING - 8
    const maxX = Math.max(...memberViews.map((node) => node.x + node.width)) + DOMAIN_PADDING
    const maxY = Math.max(...memberViews.map((node) => node.y + node.height)) + DOMAIN_PADDING

    return {
      id: group.domain.id,
      domain: group.domain,
      x: Math.max(18, minX),
      y: Math.max(42, minY),
      width: Math.min(CANVAS_WIDTH - Math.max(18, minX) - 18, Math.max(slot.width, maxX - minX)),
      height: Math.max(slot.height, maxY - minY),
    }
  })

  if (orphanNodes.value.length) {
    views.push({
      id: 'orphan',
      domain: { domainName: '未归属节点', status: 'DEGRADED' },
      x: 520,
      y: 520,
      width: 320,
      height: 118,
    })
  }

  return views
})

const linkViews = computed(() => props.links.map((link, index) => {
  const source = nodeViewById.value.get(link.sourceNodeId)
  const target = nodeViewById.value.get(link.targetNodeId)
  if (!source || !target) return null

  const resource = resourcesByLink.value.get(link.id)
  const severity = statusSeverity(link.status, resource?.isAnomaly)
  const route = buildLinkRoute(source, target, index, severity)
  const badge = quadraticPoint(route.sourcePoint, route.control, route.targetPoint, 0.5)

  return {
    link,
    sourcePoint: route.sourcePoint,
    targetPoint: route.targetPoint,
    control: route.control,
    badge,
    severity,
    path: `M ${route.sourcePoint.x} ${route.sourcePoint.y} Q ${route.control.x} ${route.control.y} ${route.targetPoint.x} ${route.targetPoint.y}`,
  }
}).filter(Boolean))

const statusCounts = computed(() => {
  const counts = { normal: 0, warning: 0, danger: 0 }
  props.nodes.forEach((node) => {
    const severity = statusSeverity(node.status)
    if (severity === 'danger') counts.danger += 1
    else if (severity === 'warning') counts.warning += 1
    else counts.normal += 1
  })
  return counts
})

const stageShellStyle = computed(() => ({
  width: `${CANVAS_WIDTH * zoom.value}px`,
  height: `${CANVAS_HEIGHT * zoom.value}px`,
}))

const stageStyle = computed(() => ({
  width: `${CANVAS_WIDTH}px`,
  height: `${CANVAS_HEIGHT}px`,
  transform: `scale(${zoom.value})`,
}))

function isCenterDomain(domain) {
  const code = String(domain?.domainCode || '').toUpperCase()
  const name = String(domain?.domainName || '')
  return code.includes('ADMIN') || name.includes('管理员') || name.includes('全局')
}

function statusSeverity(status, anomaly = false) {
  if (anomaly) return 'warning'
  const normalized = String(status || '').toUpperCase()
  if (normalized.includes('DOWN') || normalized.includes('FAIL') || normalized.includes('ERROR')) return 'danger'
  if (normalized.includes('WARN') || normalized.includes('DEGRADED') || normalized.includes('ANOMALY')) return 'warning'
  return 'success'
}

function statusText(status, anomaly = false) {
  if (anomaly) return 'WARN'
  const normalized = String(status || '').toUpperCase()
  if (normalized.includes('DOWN') || normalized.includes('FAIL') || normalized.includes('ERROR')) return 'DOWN'
  if (normalized.includes('WARN') || normalized.includes('DEGRADED') || normalized.includes('ANOMALY')) return 'WARN'
  return status || 'UP'
}

function domainTitle(domain) {
  return domain?.domainName || domain?.domainCode || domain?.id || '未命名域'
}

function nodeTitle(node) {
  return node?.nodeName || node?.nodeCode || node?.id || '未命名节点'
}

function linkTitle(link) {
  return link.linkName || link.linkCode || link.id
}

function centerPoint(node) {
  return {
    x: (node?.x ?? CANVAS_WIDTH / 2) + (node?.width ?? CENTER_WIDTH) / 2,
    y: (node?.y ?? CANVAS_HEIGHT / 2) + (node?.height ?? CENTER_HEIGHT) / 2,
  }
}

function anchorPoint(from, to) {
  const fromCenter = centerPoint(from)
  const toCenter = centerPoint(to)
  const dx = toCenter.x - fromCenter.x
  const dy = toCenter.y - fromCenter.y

  if (Math.abs(dx) > Math.abs(dy)) {
    return {
      x: fromCenter.x + (dx > 0 ? from.width / 2 : -from.width / 2),
      y: fromCenter.y,
    }
  }

  return {
    x: fromCenter.x,
    y: fromCenter.y + (dy > 0 ? from.height / 2 : -from.height / 2),
  }
}

function buildLinkRoute(source, target, index, severity) {
  const sourcePoint = anchorPoint(source, target)
  const targetPoint = anchorPoint(target, source)
  const center = centerPoint(nodeViewById.value.get(centerNode.value.id))
  const dx = targetPoint.x - sourcePoint.x
  const dy = targetPoint.y - sourcePoint.y
  const distance = Math.max(1, Math.hypot(dx, dy))
  const mid = {
    x: (sourcePoint.x + targetPoint.x) / 2,
    y: (sourcePoint.y + targetPoint.y) / 2,
  }
  const normal = {
    x: -dy / distance,
    y: dx / distance,
  }
  const toCenter = {
    x: center.x - mid.x,
    y: center.y - mid.y,
  }
  const awaySign = normal.x * toCenter.x + normal.y * toCenter.y > 0 ? -1 : 1
  const centerDistance = Math.hypot(toCenter.x, toCenter.y)
  const baseCurve = Math.min(128, Math.max(42, distance * 0.16)) + (centerDistance < 230 ? 48 : 0)
  const laneOffset = linkLaneOffset(index) + (severity === 'warning' ? 16 : 0)
  const curve = awaySign * baseCurve + laneOffset

  return {
    sourcePoint,
    targetPoint,
    control: {
      x: clamp(mid.x + normal.x * curve, 24, CANVAS_WIDTH - 24),
      y: clamp(mid.y + normal.y * curve, 24, CANVAS_HEIGHT - 24),
    },
  }
}

function linkLaneOffset(index) {
  const lanes = [0, -26, 26, -48, 48, -70, 70]
  return lanes[index % lanes.length]
}

function quadraticPoint(start, control, end, t) {
  const oneMinus = 1 - t
  return {
    x: oneMinus * oneMinus * start.x + 2 * oneMinus * t * control.x + t * t * end.x,
    y: oneMinus * oneMinus * start.y + 2 * oneMinus * t * control.y + t * t * end.y,
  }
}

function domainSlot(domain) {
  const code = String(domain?.domainCode || '').toUpperCase()
  if (code.includes('BJ-CAMPUS')) return { x: 42, y: 46, width: 592, height: 138 }
  if (code.includes('SH-POP')) return { x: 786, y: 46, width: 592, height: 138 }
  if (code.includes('HZ')) return { x: 96, y: 238, width: 304, height: 126 }
  if (code.includes('SH-FINANCE')) return { x: 944, y: 238, width: 304, height: 126 }
  if (code.includes('GZ')) return { x: 96, y: 402, width: 304, height: 126 }
  if (code.includes('BJ-EDGE')) return { x: 928, y: 402, width: 304, height: 126 }
  if (code.includes('XA')) return { x: 96, y: 548, width: 304, height: 118 }
  return { x: 518, y: 500, width: 320, height: 126 }
}

function buildAutoLayout() {
  const next = {}
  next[centerNode.value.id] = {
    x: CANVAS_WIDTH / 2 - CENTER_WIDTH / 2,
    y: CANVAS_HEIGHT / 2 - CENTER_HEIGHT / 2 + 36,
  }

  domainGroups.value.forEach((group, index) => {
    const slot = domainSlot(group.domain, index)
    group.nodes.forEach((node, nodeIndex) => {
      const xGap = group.nodes.length > 1 ? Math.max(0, (slot.width - NODE_WIDTH * group.nodes.length) / (group.nodes.length + 1)) : (slot.width - NODE_WIDTH) / 2
      next[node.id] = {
        x: slot.x + xGap + nodeIndex * (NODE_WIDTH + xGap),
        y: slot.y + 44,
      }
    })
  })

  orphanNodes.value.forEach((node, index) => {
    next[node.id] = {
      x: 540 + index * 24,
      y: 552 + index * 16,
    }
  })

  return next
}

function loadSavedLayout() {
  try {
    const saved = JSON.parse(localStorage.getItem(layoutKey.value) || '{}')
    const expectedIds = graphNodes.value.map((node) => node.id)
    if (expectedIds.every((id) => saved[id])) {
      positions.value = saved
      return
    }
  } catch {
    localStorage.removeItem(layoutKey.value)
  }
  autoLayout()
}

function autoLayout() {
  positions.value = buildAutoLayout()
}

function saveLayout() {
  localStorage.setItem(layoutKey.value, JSON.stringify(positions.value))
  ElMessage.success('拓扑布局已保存')
}

function resetView() {
  zoom.value = 1
  autoLayout()
}

function changeZoom(delta) {
  zoom.value = Math.min(1.35, Math.max(0.75, Number((zoom.value + delta).toFixed(2))))
}

function setZoom(value) {
  zoom.value = value
}

function startDrag(event, node) {
  if (event.button !== 0) return
  dragging.value = {
    id: node.id,
    pointerId: event.pointerId,
    startX: event.clientX,
    startY: event.clientY,
    nodeX: node.x,
    nodeY: node.y,
    width: node.width,
    height: node.height,
  }
  dragMoved.value = false
  event.currentTarget.setPointerCapture(event.pointerId)
}

function moveDrag(event) {
  if (!dragging.value || event.pointerId !== dragging.value.pointerId) return
  const dx = (event.clientX - dragging.value.startX) / zoom.value
  const dy = (event.clientY - dragging.value.startY) / zoom.value

  if (Math.abs(dx) > 2 || Math.abs(dy) > 2) dragMoved.value = true

  positions.value = {
    ...positions.value,
    [dragging.value.id]: {
      x: clamp(dragging.value.nodeX + dx, 12, CANVAS_WIDTH - dragging.value.width - 12),
      y: clamp(dragging.value.nodeY + dy, 12, CANVAS_HEIGHT - dragging.value.height - 12),
    },
  }
}

function stopDrag(event) {
  if (!dragging.value || event.pointerId !== dragging.value.pointerId) return
  dragging.value = null
}

function clamp(value, min, max) {
  return Math.min(max, Math.max(min, value))
}

function handleNodeClick(node) {
  if (dragMoved.value) {
    dragMoved.value = false
    return
  }

  if (node.type === 'node') emit('node-click', node.source)
  if (node.type === 'center' && node.source) emit('domain-click', node.source)
}

watch(
  () => [props.domains, props.nodes, props.links],
  loadSavedLayout,
  { immediate: true, deep: true },
)

onBeforeUnmount(() => {
  dragging.value = null
})
</script>

<template>
  <div class="topology-graph" :style="{ minHeight: `${height}px` }">
    <div class="graph-head">
      <div class="graph-summary">
        <div class="summary-chip success">
          <ElIcon><CircleCheckFilled /></ElIcon>
          <span>正常</span>
          <strong>{{ statusCounts.normal }}</strong>
        </div>
        <div class="summary-chip warning">
          <ElIcon><WarningFilled /></ElIcon>
          <span>异常</span>
          <strong>{{ statusCounts.warning }}</strong>
        </div>
        <div class="summary-chip danger">
          <ElIcon><WarningFilled /></ElIcon>
          <span>故障</span>
          <strong>{{ statusCounts.danger }}</strong>
        </div>
        <div class="summary-chip">
          <ElIcon><Grid /></ElIcon>
          <span>域</span>
          <strong>{{ visibleDomains.length }}</strong>
        </div>
        <div class="summary-chip">
          <ElIcon><Cpu /></ElIcon>
          <span>节点</span>
          <strong>{{ nodes.length }}</strong>
        </div>
        <div class="summary-chip">
          <ElIcon><Connection /></ElIcon>
          <span>链路</span>
          <strong>{{ links.length }}</strong>
        </div>
      </div>

      <div class="graph-actions">
        <ElButton @click="autoLayout">
          <ElIcon><Share /></ElIcon>
          自动布局
        </ElButton>
        <ElButton @click="saveLayout">
          <ElIcon><ScaleToOriginal /></ElIcon>
          保存布局
        </ElButton>
        <ElButton @click="resetView">
          <ElIcon><Refresh /></ElIcon>
          重置视图
        </ElButton>
        <ElButtonGroup>
          <ElButton aria-label="缩小拓扑" @click="changeZoom(-0.1)">
            <ElIcon><Minus /></ElIcon>
          </ElButton>
          <ElDropdown trigger="click" @command="setZoom">
            <ElButton class="zoom-button">{{ Math.round(zoom * 100) }}%</ElButton>
            <template #dropdown>
              <ElDropdownMenu>
                <ElDropdownItem :command="0.75">75%</ElDropdownItem>
                <ElDropdownItem :command="1">100%</ElDropdownItem>
                <ElDropdownItem :command="1.25">125%</ElDropdownItem>
              </ElDropdownMenu>
            </template>
          </ElDropdown>
          <ElButton aria-label="放大拓扑" @click="changeZoom(0.1)">
            <ElIcon><Plus /></ElIcon>
          </ElButton>
        </ElButtonGroup>
      </div>
    </div>

    <div class="graph-viewport" :style="{ height: `${height - 74}px` }">
      <div class="stage-shell" :style="stageShellStyle">
        <div class="graph-stage" :style="stageStyle">
          <div class="grid-layer" />

          <button
            v-for="domain in domainViews"
            :key="domain.id"
            type="button"
            class="domain-zone"
            :class="statusSeverity(domain.domain.status)"
            :style="{
              left: `${domain.x}px`,
              top: `${domain.y}px`,
              width: `${domain.width}px`,
              height: `${domain.height}px`,
            }"
            @click="$emit('domain-click', domain.domain)"
          >
            <span class="zone-handle" />
            <span class="zone-title">{{ domainTitle(domain.domain) }}</span>
          </button>

          <svg class="link-layer" :viewBox="`0 0 ${CANVAS_WIDTH} ${CANVAS_HEIGHT}`" aria-hidden="true">
            <g v-for="item in linkViews" :key="item.link.id" class="link-item" :class="item.severity">
              <path class="link-shadow" :d="item.path" />
              <path class="link-line" :d="item.path" />
              <path class="link-hit" :d="item.path" @click="$emit('link-click', item.link)">
                <title>{{ linkTitle(item.link) }}</title>
              </path>
              <circle class="link-badge" :cx="item.badge.x" :cy="item.badge.y" r="8" />
              <text class="link-badge-text" :x="item.badge.x" :y="item.badge.y + 4">
                {{ item.severity === 'success' ? '✓' : '!' }}
              </text>
            </g>
          </svg>

          <button
            v-for="node in nodeViews"
            :key="node.id"
            type="button"
            class="topology-node"
            :class="[node.type, statusSeverity(node.status)]"
            :style="{
              left: `${node.x}px`,
              top: `${node.y}px`,
              width: `${node.width}px`,
              height: `${node.height}px`,
            }"
            @click="handleNodeClick(node)"
            @pointerdown="startDrag($event, node)"
            @pointermove="moveDrag"
            @pointerup="stopDrag"
            @pointercancel="stopDrag"
          >
            <span class="node-icon">
              <ElIcon v-if="node.type === 'center'"><Aim /></ElIcon>
              <ElIcon v-else><Cpu /></ElIcon>
            </span>
            <span class="node-copy">
              <strong>{{ node.title }}</strong>
              <span>{{ node.subtitle }}</span>
            </span>
            <span class="status-pill">
              <i />
              {{ statusText(node.status, statusSeverity(node.status) === 'warning') }}
            </span>
          </button>

          <div class="mini-map">
            <svg :viewBox="`0 0 ${CANVAS_WIDTH} ${CANVAS_HEIGHT}`">
              <path
                v-for="item in linkViews"
                :key="`mini-${item.link.id}`"
                :d="item.path"
                class="mini-link"
                :class="item.severity"
              />
              <rect
                v-for="node in nodeViews"
                :key="`mini-${node.id}`"
                :x="node.x"
                :y="node.y"
                :width="node.width"
                :height="node.height"
                rx="8"
                class="mini-node"
                :class="node.type"
              />
            </svg>
            <ElButtonGroup class="mini-actions">
              <ElButton aria-label="重置视图" @click="resetView">
                <ElIcon><FullScreen /></ElIcon>
              </ElButton>
              <ElButton aria-label="自动布局" @click="autoLayout">
                <ElIcon><Refresh /></ElIcon>
              </ElButton>
            </ElButtonGroup>
          </div>
        </div>
      </div>

      <div class="floating-controls">
        <ElButtonGroup>
          <ElButton aria-label="放大拓扑" @click="changeZoom(0.1)">
            <ElIcon><Plus /></ElIcon>
          </ElButton>
          <ElButton aria-label="缩小拓扑" @click="changeZoom(-0.1)">
            <ElIcon><Minus /></ElIcon>
          </ElButton>
          <ElButton aria-label="重置拓扑" @click="resetView">
            <ElIcon><FullScreen /></ElIcon>
          </ElButton>
        </ElButtonGroup>
      </div>

      <ElEmpty v-if="!domains.length && !nodes.length && !links.length" class="empty" description="数据暂时为空" />
    </div>
  </div>
</template>

<style scoped>
.topology-graph {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 14px;
  border: 1px solid var(--am-border);
  border-radius: 8px;
  background: #fff;
}

.graph-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  padding: 16px 16px 0;
}

.graph-summary,
.graph-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.graph-actions :deep(.el-button) {
  min-height: 36px;
}

.summary-chip {
  display: inline-flex;
  min-width: 98px;
  height: 36px;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid #dbe4f0;
  border-radius: 7px;
  background: linear-gradient(180deg, #ffffff, #f8fbff);
  color: #27405f;
  font-size: 14px;
  box-shadow: 0 4px 12px rgb(31 41 55 / 6%);
}

.summary-chip strong {
  color: #14213d;
  font-size: 16px;
}

.summary-chip.success :deep(.el-icon) {
  color: var(--am-success);
}

.summary-chip.warning :deep(.el-icon) {
  color: var(--am-warning);
}

.summary-chip.danger :deep(.el-icon) {
  color: var(--am-danger);
}

.zoom-button {
  width: 88px;
  font-weight: 700;
}

.graph-viewport {
  position: relative;
  overflow: auto;
  margin: 0 16px 16px;
  border: 1px solid #dbe5f2;
  border-radius: 8px;
  background: #fbfdff;
}

.stage-shell {
  position: relative;
  min-width: 1040px;
  min-height: 560px;
}

.graph-stage {
  position: absolute;
  left: 0;
  top: 0;
  overflow: hidden;
  transform-origin: left top;
}

.grid-layer {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle, rgb(37 99 235 / 14%) 1px, transparent 1px) 0 0 / 16px 16px,
    linear-gradient(180deg, #fcfdff 0%, #f8fbff 100%);
}

.domain-zone {
  position: absolute;
  z-index: 1;
  border: 1px dashed #c9d8eb;
  border-radius: 28px;
  background: rgb(239 245 255 / 48%);
  color: var(--am-primary);
  cursor: pointer;
  text-align: left;
}

.domain-zone.warning {
  border-color: #f0b75c;
  background: rgb(255 247 237 / 56%);
}

.zone-title {
  position: absolute;
  left: 50%;
  top: -28px;
  max-width: calc(100% - 72px);
  transform: translateX(-50%);
  overflow: hidden;
  border-radius: 999px;
  background: #eaf2ff;
  color: #1f66cc;
  font-size: 14px;
  font-weight: 700;
  line-height: 32px;
  padding: 0 18px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.domain-zone.warning .zone-title {
  background: #fff0d9;
  color: #bf6500;
}

.zone-handle,
.zone-handle::before,
.zone-handle::after {
  position: absolute;
  width: 4px;
  height: 4px;
  border-radius: 999px;
  background: #b6c8df;
  box-shadow: 0 8px 0 #b6c8df, 0 16px 0 #b6c8df;
}

.zone-handle {
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
}

.zone-handle::before,
.zone-handle::after {
  content: "";
  top: 0;
}

.zone-handle::before {
  left: -8px;
}

.zone-handle::after {
  left: 8px;
}

.link-layer {
  position: absolute;
  inset: 0;
  z-index: 2;
  overflow: visible;
  pointer-events: none;
}

.link-item {
  color: var(--am-success);
}

.link-item.warning {
  color: var(--am-warning);
}

.link-item.danger {
  color: var(--am-danger);
}

.link-shadow {
  fill: none;
  stroke: #ffffff;
  stroke-linecap: round;
  stroke-width: 7;
}

.link-line {
  fill: none;
  stroke: currentColor;
  stroke-linecap: round;
  stroke-width: 3;
}

.link-hit {
  pointer-events: stroke;
  fill: none;
  stroke: transparent;
  stroke-width: 22;
  cursor: pointer;
}

.link-badge {
  fill: currentColor;
  stroke: #fff;
  stroke-width: 3;
}

.link-badge-text {
  fill: #fff;
  font-size: 14px;
  font-weight: 800;
  pointer-events: none;
  text-anchor: middle;
}

.topology-node {
  position: absolute;
  z-index: 3;
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  grid-template-rows: minmax(42px, 1fr) 28px;
  column-gap: 12px;
  align-items: center;
  border: 1px solid #dfe7f2;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 24px rgb(20 33 61 / 12%);
  color: #14213d;
  cursor: grab;
  padding: 14px;
  text-align: left;
  touch-action: none;
  user-select: none;
}

.topology-node:hover,
.topology-node:focus-visible {
  border-color: #7aa8ff;
  box-shadow: 0 16px 30px rgb(37 99 235 / 20%);
  outline: none;
}

.topology-node.warning {
  border-color: #f59e0b;
  box-shadow: 0 14px 28px rgb(217 119 6 / 14%);
}

.topology-node.danger {
  border-color: #ef4444;
  box-shadow: 0 14px 28px rgb(220 38 38 / 14%);
}

.topology-node.center {
  border-color: #a9c7ff;
  box-shadow: 0 14px 28px rgb(37 99 235 / 22%);
}

.topology-node:active {
  cursor: grabbing;
}

.node-icon {
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  border: 1px solid #cfe0ff;
  border-radius: 8px;
  background: linear-gradient(180deg, #eef5ff, #dceaff);
  color: var(--am-primary);
  font-size: 24px;
}

.center .node-icon {
  color: #fff;
  background: linear-gradient(180deg, #3b82f6, #1d4ed8);
}

.node-copy {
  min-width: 0;
}

.node-copy strong,
.node-copy span {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.node-copy strong {
  font-size: 14px;
}

.node-copy span {
  margin-top: 8px;
  color: var(--am-muted);
  font-size: 12px;
}

.status-pill {
  display: flex;
  grid-column: 1 / -1;
  height: 26px;
  align-items: center;
  gap: 8px;
  border-radius: 6px;
  background: #ecf8eb;
  color: #186b38;
  font-size: 12px;
  font-weight: 700;
  padding: 0 10px;
}

.warning .status-pill {
  background: #fff3df;
  color: #a25100;
}

.danger .status-pill {
  background: #ffe7e7;
  color: #b91c1c;
}

.status-pill i {
  width: 9px;
  height: 9px;
  border-radius: 999px;
  background: currentColor;
}

.mini-map {
  position: absolute;
  right: 18px;
  bottom: 18px;
  z-index: 4;
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid #dfe7f2;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 16px 30px rgb(20 33 61 / 14%);
  padding: 10px;
}

.mini-map svg {
  width: 206px;
  height: 110px;
  border: 2px solid #4e89ff;
  border-radius: 4px;
  background: #f6f9ff;
}

.mini-link {
  fill: none;
  stroke: var(--am-success);
  stroke-linecap: round;
  stroke-width: 8;
}

.mini-link.warning {
  stroke: var(--am-warning);
}

.mini-link.danger {
  stroke: var(--am-danger);
}

.mini-node {
  fill: #b6cae7;
}

.mini-node.center {
  fill: #2f6fd4;
}

.mini-actions {
  display: grid;
  gap: 8px;
}

.mini-actions :deep(.el-button) {
  margin-left: 0;
}

.floating-controls {
  position: sticky;
  left: 12px;
  bottom: 12px;
  z-index: 5;
  width: fit-content;
  margin: -56px 0 12px 12px;
}

.empty {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  background: rgb(255 255 255 / 78%);
}

@media (max-width: 900px) {
  .graph-head {
    display: grid;
  }

  .graph-actions {
    justify-content: flex-start;
  }

  .summary-chip {
    min-width: 92px;
  }
}
</style>
