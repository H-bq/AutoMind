<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getGlobalTopology } from '@/api/topology'
import PageContainer from '@/components/common/PageContainer.vue'
import TopologyGraph from '@/components/topology/TopologyGraph.vue'

const router = useRouter()
const loading = ref(false)
const topology = ref({
  domains: [],
  nodes: [],
  links: [],
  linkResources: [],
  dataIssues: [],
})

const fetchTopology = async () => {
  loading.value = true
  try {
    topology.value = await getGlobalTopology()
  } finally {
    loading.value = false
  }
}

const openLink = (link) => {
  if (link?.id) router.push(`/topology/link/${link.id}`)
}

onMounted(fetchTopology)
</script>

<template>
  <PageContainer title="我的拓扑" description="展示当前租户可见的拓扑数据。">
    <template #actions>
      <ElButton :loading="loading" @click="fetchTopology">刷新</ElButton>
    </template>
    <TopologyGraph
      v-loading="loading"
      :domains="topology?.domains || []"
      :nodes="topology?.nodes || []"
      :links="topology?.links || []"
      :link-resources="topology?.linkResources || []"
      @link-click="openLink"
    />
    <ElAlert
      v-if="topology?.dataIssues?.length"
      type="warning"
      :closable="false"
      show-icon
      title="拓扑数据存在完整性提示"
    >
      <ul class="issue-list">
        <li v-for="issue in topology.dataIssues" :key="`${issue.issueType}-${issue.entityId}`">
          {{ issue.severity }} / {{ issue.issueType }}：{{ issue.message }}
        </li>
      </ul>
    </ElAlert>
  </PageContainer>
</template>

<style scoped>
.issue-list {
  margin: 6px 0 0;
  padding-left: 18px;
}
</style>
