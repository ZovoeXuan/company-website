<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ApiError } from '@/api/http'
import { useAuth } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const { login } = useAuth()

const form = ref({ username: '', password: '' })
const submitting = ref(false)
const errorText = ref('')

async function submit() {
  if (submitting.value) return
  errorText.value = ''
  if (!form.value.username.trim() || !form.value.password) {
    errorText.value = '请输入账号和密码'
    return
  }
  submitting.value = true
  try {
    await login(form.value.username.trim(), form.value.password)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard'
    await router.replace(redirect)
  } catch (error) {
    const apiError = error instanceof ApiError ? error : null
    errorText.value = apiError?.message ?? '登录失败，请稍后重试'
    if (apiError?.requestId) {
      ElMessage.error(`${errorText.value}（${apiError.requestId}）`)
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="login">
    <div class="panel">
      <div class="brand">
        <span class="logo">示</span>
        <div>
          <p class="brand-name">示例科技</p>
          <p class="brand-sub">内容管理后台</p>
        </div>
      </div>

      <h1>登录</h1>

      <form class="form" @submit.prevent="submit">
        <label class="field">
          <span>账号</span>
          <input v-model="form.username" autocomplete="username" placeholder="请输入账号" />
        </label>
        <label class="field">
          <span>密码</span>
          <input v-model="form.password" type="password" autocomplete="current-password" placeholder="请输入密码" />
        </label>

        <p v-if="errorText" class="error">{{ errorText }}</p>

        <button type="submit" class="submit" :disabled="submitting">
          {{ submitting ? '登录中…' : '登录' }}
        </button>
      </form>

      <p class="hint">账号由管理员创建，不提供自助注册与找回密码。连续失败会被临时限制登录。</p>
    </div>
  </div>
</template>

<style scoped>
.login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f6f8fa;
  padding: 24px;
}

.panel {
  width: 100%;
  max-width: 380px;
  background: #fff;
  border: 1px solid var(--a-border);
  border-radius: 12px;
  padding: 28px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 26px;
}

.logo {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  background: #e6f1fb;
  color: #185fa5;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
}

.brand-name {
  font-size: 14px;
  font-weight: 500;
}

.brand-sub {
  font-size: 12px;
  color: var(--a-text-muted);
  margin-top: 2px;
}

h1 {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 18px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: var(--a-text-muted);
}

.field input {
  height: 38px;
  border: 1px solid var(--a-border);
  border-radius: 8px;
  padding: 0 12px;
  font-size: 14px;
  color: var(--a-text);
  outline: none;
}

.field input:focus {
  border-color: #185fa5;
}

.error {
  font-size: 13px;
  color: #c0392b;
}

.submit {
  height: 38px;
  border: none;
  border-radius: 8px;
  background: #185fa5;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}

.submit:disabled {
  opacity: 0.6;
  cursor: default;
}

.hint {
  margin-top: 18px;
  font-size: 12px;
  color: var(--a-text-muted);
  line-height: 1.7;
}
</style>
