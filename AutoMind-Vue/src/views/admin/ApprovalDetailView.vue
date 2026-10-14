<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getApproval } from '@/api/approval'
import ApprovalCard from '@/components/approval/ApprovalCard.vue'
import ApprovalDecisionDialog from '@/components/approval/ApprovalDecisionDialog.vue'
import PageContainer from '@/components/common/PageContainer.vue'

const route = useRoute()
const approval = ref(null)
const loading = ref(false)
const dialogVisible = ref(false)
const decisionMode = ref('approve')
const isPending = computed(() => approval.value?.status === 'PENDING')

async function loadApproval() {
  loading.value = true
  try {
    approval.value = await getApproval(route.params.id)
  } finally {
    loading.value = false
  }
}

function openDecision(mode) {
  decisionMode.value = mode
  dialogVisible.value = true
}

async function handleDecided() {
  await loadApproval()
}

onMounted(loadApproval)
</script>

<template>
  <PageContainer title="审批详情" :description="`approvalId: ${route.params.id}`">
    <div v-loading="loading" class="approval-detail">
      <ApprovalCard v-if="approval" :approval="approval" />
      <ElEmpty v-else description="数据暂时为空" />

      <div v-if="approval && isPending" class="actions">
        <ElButton type="primary" @click="openDecision('approve')">批准</ElButton>
        <ElButton type="danger" @click="openDecision('reject')">拒绝</ElButton>
      </div>
    </div>

    <ApprovalDecisionDialog
      v-model:visible="dialogVisible"
      :approval-id="route.params.id"
      :mode="decisionMode"
      @decided="handleDecided"
    />
  </PageContainer>
</template>

<style scoped>
.approval-detail {
  padding: 16px;
  border: 1px solid var(--am-border);
  border-radius: 8px;
  background: #fff;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}
</style>
