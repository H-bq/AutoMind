<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getIntent, getIntentTrace } from '@/api/intent'
import PageContainer from '@/components/common/PageContainer.vue'
import PevTimeline from '@/components/trace/PevTimeline.vue'

const route = useRoute()
const router = useRouter()
const intent = ref(null)
const tasks = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const intentId = route.params.intentId
    const [intentResult, traceResult] = await Promise.all([getIntent(intentId), getIntentTrace(intentId)])
    intent.value = intentResult
    tasks.value = traceResult?.tasks || []
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <PageContainer title="意图详情" :description="`intentId: ${route.params.intentId}`">
    <template #actions>
      <ElButton @click="router.back()">
        <ElIcon style="margin-right: 4px;"><ArrowLeft /></ElIcon>返回
      </ElButton>
    </template>
    <ElCard v-loading="loading" shadow="never">
      <ElDescriptions v-if="intent" border :column="2">
        <ElDescriptionsItem label="ID">{{ intent.id }}</ElDescriptionsItem>
        <ElDescriptionsItem label="租户 ID">{{ intent.tenantId }}</ElDescriptionsItem>
        <ElDescriptionsItem label="用户 ID">{{ intent.userId }}</ElDescriptionsItem>
        <ElDescriptionsItem label="优先级">{{ intent.priority }}</ElDescriptionsItem>
        <ElDescriptionsItem label="创建时间">{{ intent.createTime }}</ElDescriptionsItem>
        <ElDescriptionsItem label="输入文本" :span="2">{{ intent.inputText }}</ElDescriptionsItem>
        <ElDescriptionsItem label="解析意图" :span="2">{{ intent.parsedIntent || '-' }}</ElDescriptionsItem>
      </ElDescriptions>
      <ElEmpty v-else description="数据暂时为空" />
    </ElCard>
    <ElCard class="trace-card" shadow="never">
      <PevTimeline :agent-tasks="tasks" />
    </ElCard>
  </PageContainer>
</template>

<style scoped>
.trace-card {
  margin-top: 16px;
}
</style>
