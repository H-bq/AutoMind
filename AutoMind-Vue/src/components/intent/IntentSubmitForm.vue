<script setup>
import { reactive, ref } from 'vue'
import { submitIntent } from '@/api/intent'
import { useNotificationStore } from '@/store/notification'

const emit = defineEmits(['submitted'])
const notificationStore = useNotificationStore()
const loading = ref(false)
const form = reactive({
  inputText: '',
  priority: 'P1',
})

async function submit() {
  loading.value = true
  try {
    const result = await submitIntent({
      inputText: form.inputText,
      priority: form.priority,
    })
    notificationStore.success('意图已提交')
    emit('submitted', result)
  } catch (error) {
    notificationStore.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <ElForm class="intent-form" label-position="top">
    <ElFormItem label="业务意图">
      <ElInput
        v-model="form.inputText"
        type="textarea"
        :rows="6"
        placeholder="明天 8-10 点，从北京到上海开 200Mbps 视频会议专线，时延不超过 40ms"
      />
    </ElFormItem>
    <ElFormItem label="优先级">
      <ElSelect v-model="form.priority">
        <ElOption label="P1" value="P1" />
        <ElOption label="P2" value="P2" />
        <ElOption label="P3" value="P3" />
      </ElSelect>
    </ElFormItem>
    <ElButton type="primary" :loading="loading" @click="submit">提交意图</ElButton>
  </ElForm>
</template>

<style scoped>
.intent-form {
  max-width: 760px;
  padding: 20px;
  border: 1px solid var(--am-border);
  border-radius: 8px;
  background: #fff;
}
</style>
