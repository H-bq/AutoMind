<template>
  <ElResult icon="warning" title="403" sub-title="当前账号无权访问该页面">
    <template #extra>
      <RouterLink to="/">
        <ElButton type="primary">返回首页</ElButton>
      </RouterLink>
    </template>
  </ElResult>
</template>
