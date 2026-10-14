<script setup>
import { useRouter } from 'vue-router'
import IntentSubmitForm from '@/components/intent/IntentSubmitForm.vue'
import PageContainer from '@/components/common/PageContainer.vue'

const router = useRouter()

function handleSubmitted(result) {
  if (result?.intentId) router.push(`/tenant/intents/${result.intentId}`)
}
</script>

<template>
  <PageContainer title="提交意图" description="提交业务意图，租户与用户上下文由登录 token 提供。">
    <IntentSubmitForm @submitted="handleSubmitted" />
  </PageContainer>
</template>
