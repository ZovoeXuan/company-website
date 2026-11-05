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
const showPassword = ref(false)
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
      ElMessage.error(`${errorText.value} (${apiError.requestId})`)
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="login">
    <div class="login__glow" aria-hidden="true"></div>
    <div class="panel">
      <div class="brand">
        <span class="logo">G</span>
        <div>
          <p class="brand-name">官网内容管理与可视化中台</p>
          <p class="brand-sub">Enterprise CMS & Visual Live Studio</p>
        </div>
      </div>

      <h1>欢迎登录管理后台</h1>

      <form class="form" @submit.prevent="submit">
        <label class="field">
          <span>管理员 / 编辑账号</span>
          <input v-model="form.username" autocomplete="username" placeholder="请输入登录账号" />
        </label>
        <label class="field">
          <span>安全密码</span>
          <div class="pwd-wrap">
            <input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              autocomplete="current-password"
              placeholder="请输入登录密码"
            />
            <button type="button" class="pwd-toggle" @click="showPassword = !showPassword">
              {{ showPassword ? '隐藏' : '显示' }}
            </button>
          </div>
        </label>

        <p v-if="errorText" class="error">{{ errorText }}</p>

        <button type="submit" class="submit" :disabled="submitting">
          {{ submitting ? '安全验证登录中...' : '立即登录控制台' }}
        </button>
      </form>

      <p class="hint">
        账号由系统管理员统一分配（ADMIN 管理员 / EDITOR 内容编辑）。连续输错密码将触发防暴力破解保护。
      </p>
    </div>
  </div>
</template>

<style scoped>
.login {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at 20% 20%, #1d1848 0%, #0b0d17 65%);
  padding: 24px;
  overflow: hidden;
}

.login__glow {
  position: absolute;
  width: 520px;
  height: 520px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(91, 75, 232, 0.32) 0%, transparent 70%);
  top: -120px;
  right: -80px;
  pointer-events: none;
}

.panel {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 410px;
  background: rgba(255, 255, 255, 0.98);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 18px;
  padding: 34px 32px;
  box-shadow: 0 28px 72px rgba(0, 0, 0, 0.45);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 26px;
}

.logo {
  width: 40px;
  height: 40px;
  border-radius: 11px;
  background: linear-gradient(135deg, #7c3aed 0%, #5b4be8 100%);
  color: #ffffff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 800;
  box-shadow: 0 8px 20px rgba(91, 75, 232, 0.35);
}

.brand-name {
  font-size: 15px;
  font-weight: 700;
  color: #101014;
  margin: 0;
}

.brand-sub {
  font-size: 12px;
  color: #6b7280;
  margin: 2px 0 0;
}

h1 {
  font-size: 20px;
  font-weight: 700;
  color: #101014;
  margin: 0 0 20px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.field input {
  height: 42px;
  width: 100%;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 0 14px;
  font-size: 14px;
  color: #111827;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.field input:focus {
  border-color: #5b4be8;
  box-shadow: 0 0 0 3px rgba(91, 75, 232, 0.14);
}

.pwd-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.pwd-toggle {
  position: absolute;
  right: 10px;
  border: none;
  background: none;
  font-size: 12px;
  color: #5b4be8;
  cursor: pointer;
}

.error {
  font-size: 13px;
  color: #dc2626;
  background: #fef2f2;
  padding: 8px 12px;
  border-radius: 8px;
  margin: 0;
}

.submit {
  height: 44px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #6366f1 0%, #5b4be8 100%);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 6px;
  box-shadow: 0 10px 24px rgba(91, 75, 232, 0.3);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.submit:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 14px 28px rgba(91, 75, 232, 0.42);
}

.submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.hint {
  margin-top: 22px;
  font-size: 12px;
  color: #6b7280;
  line-height: 1.6;
}
</style>
