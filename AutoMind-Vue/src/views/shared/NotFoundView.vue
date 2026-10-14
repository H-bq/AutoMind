<template>
  <ElResult icon="info" title="404" sub-title="页面不存在或资源不可见">
    <template #extra>
      <RouterLink to="/">
        <ElButton type="primary">返回首页</ElButton>
      </RouterLink>
    </template>
  </ElResult>
</template>
