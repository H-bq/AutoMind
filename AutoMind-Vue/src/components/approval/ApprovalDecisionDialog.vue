<script setup>
import { computed, reactive, ref } from 'vue'
import { approve, reject, rejectWithModification } from '@/api/approval'
import { useNotificationStore } from '@/store/notification'

const props = defineProps({
  visible: Boolean,
  approvalId: {
    type: [String, Number],
    default: '',
  },
  mode: {
    type: String,
    default: 'approve',
  },
})
const emit = defineEmits(['decided', 'update:visible'])
const notificationStore = useNotificationStore()
const loading = ref(false)
const form = reactive({
  reason: '',
  modifiedPayload: '{}',
})

const title = computed(() => {
  const map = {
    approve: '批准审批',
    reject: '拒绝审批',
    'reject-with-modification': '拒绝并要求修改',
  }
  return map[props.mode] || '审批决策'
})

async function submit() {
  loading.value = true
  try {
    let result
    if (props.mode === 'approve') result = await approve(props.approvalId, { reason: form.reason })
    if (props.mode === 'reject') result = await reject(props.approvalId, { reason: form.reason })
    if (props.mode === 'reject-with-modification') {
      result = await rejectWithModification(props.approvalId, {
        reason: form.reason,
        modifiedPayload: JSON.parse(form.modifiedPayload || '{}'),
      })
    }
    notificationStore.success('审批操作已提交')
    emit('decided', result)
    emit('update:visible', false)
  } catch (error) {
    notificationStore.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <ElDialog :model-value="visible" :title="title" width="560px" @update:model-value="$emit('update:visible', $event)">
    <ElForm label-position="top">
      <ElFormItem label="处理原因">
        <ElInput v-model="form.reason" type="textarea" :rows="4" />
      </ElFormItem>
      <ElFormItem v-if="mode === 'reject-with-modification'" label="修改后的 payload">
        <ElInput v-model="form.modifiedPayload" type="textarea" :rows="8" />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="$emit('update:visible', false)">取消</ElButton>
      <ElButton type="primary" :loading="loading" @click="submit">确认</ElButton>
    </template>
  </ElDialog>
</template>
