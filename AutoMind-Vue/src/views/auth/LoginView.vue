<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login } from '@/api/auth'
import { useNotificationStore } from '@/store/notification'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const notificationStore = useNotificationStore()
const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
})

function fillCredentials(username, password) {
  form.username = username
  form.password = password
}

function redirectByRole() {
  const fallback = userStore.isAdmin ? '/admin/approvals/pending' : '/tenant/intent/submit'
  router.push(route.query.redirect || fallback)
}

async function submit() {
  if (loading.value) return
  loading.value = true
  try {
    const session = await login({
      username: form.username,
      password: form.password,
    })
    userStore.setSession(session)
    redirectByRole()
  } catch (error) {
    notificationStore.error(error, '用户名或密码错误')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-view">
    <section class="login-panel">
      <div class="login-copy">
        <div class="brand-mark">A</div>
        <h1>AutoMind 租户运维平台</h1>
        <p>统一承载意图提交、拓扑、SLA、审计和 Agent 任务追踪。</p>
      </div>
      <ElForm class="login-form" label-position="top" @keyup.enter="submit">
        <ElFormItem label="用户名">
          <ElInput v-model="form.username" placeholder="username" />
        </ElFormItem>
        <ElFormItem label="密码">
          <ElInput v-model="form.password" type="password" show-password placeholder="password" />
        </ElFormItem>
        <div class="demo-accounts">
          <ElButton size="small" type="info" plain @click="fillCredentials('admin', '123456')">管理员账号</ElButton>
          <ElButton size="small" type="info" plain @click="fillCredentials('tenant1_admin', '123456')">示例租户账号</ElButton>
        </div>
        <ElButton type="primary" :loading="loading" class="login-button" @click="submit">登录</ElButton>
      </ElForm>
    </section>
  </main>
</template>

<style scoped>
.login-view {
  display: grid;
  min-height: 100vh;
  place-items: center;
  background: #eef3fb;
}

.login-panel {
  display: grid;
  width: min(920px, calc(100vw - 32px));
  grid-template-columns: 1fr 380px;
  overflow: hidden;
  border: 1px solid var(--am-border);
  border-radius: 8px;
  background: #fff;
}

.login-copy {
  padding: 52px;
  background: #14213d;
  color: #fff;
}

.brand-mark {
  display: grid;
  width: 48px;
  height: 48px;
  place-items: center;
  border-radius: 8px;
  background: #2563eb;
  font-size: 24px;
  font-weight: 700;
}

h1 {
  margin: 36px 0 12px;
  font-size: 32px;
}

p {
  margin: 0;
  color: #c8d4ea;
}

.login-form {
  padding: 52px 40px;
}

.demo-accounts {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.demo-accounts .el-button {
  flex: 1;
  margin-left: 0;
}

.login-button {
  width: 100%;
}

@media (max-width: 760px) {
  .login-panel {
    grid-template-columns: 1fr;
  }
}
</style>
