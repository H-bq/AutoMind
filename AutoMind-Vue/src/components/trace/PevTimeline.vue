<script setup>
defineProps({
  agentTasks: {
    type: Array,
    default: () => [],
  },
})
</script>

<template>
  <ElTimeline v-if="agentTasks.length">
    <ElTimelineItem
      v-for="task in agentTasks"
      :key="task.id"
      :timestamp="task.startedAt || task.finishedAt"
      placement="top"
    >
      <ElCard shadow="never">
        <strong>{{ task.agentRole || task.taskType || task.id }}</strong>
        <div>{{ task.pevState || '-' }}</div>
        <div v-if="task.errorCode">errorCode: {{ task.errorCode }}</div>
      </ElCard>
    </ElTimelineItem>
  </ElTimeline>
  <ElEmpty v-else description="数据暂时为空" />
</template>
