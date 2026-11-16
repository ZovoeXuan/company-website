<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ApiError, request } from '@/api/http'
import { useAuth } from '@/stores/auth'

const router = useRouter()
const { current, logout } = useAuth()

const form = reactive({ oldPassword: '', newPassword: '', confirm: '' })
const submitting = ref(false)

async function submit() {
  if (form.newPassword.length < 12) {
    ElMessage.warning('新密码长度至少 12 位')
    return
  }
  if (form.newPassword !== form.confirm) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  submitting.value = true
  try {
    await request('/api/admin/auth/password', {
      method: 'PUT',
      body: { oldPassword: form.oldPassword, newPassword: form.newPassword },
    })
    ElMessage.success('密码已修改，请使用新密码重新登录')
    try {
      await logout()
    } catch {
      // 会话已被服务端撤销，退出接口失败不影响结果
    }
    await router.replace('/login')
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '修改失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <el-card shadow="never" class="card">
    <template #header>修改密码</template>
    <p class="who">当前账号：{{ current?.displayName }}（{{ current?.username }}）</p>
    <el-form label-width="100px" class="form">
      <el-form-item label="原密码">
        <el-input v-model="form.oldPassword" type="password" autocomplete="current-password" />
      </el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="form.newPassword" type="password" autocomplete="new-password" placeholder="至少 12 位" />
      </el-form-item>
      <el-form-item label="确认新密码">
        <el-input v-model="form.confirm" type="password" autocomplete="new-password" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="submit">保存并重新登录</el-button>
      </el-form-item>
    </el-form>
    <p class="hint">修改成功后该账号的全部会话会被撤销，包括其他设备上的登录。</p>
  </el-card>
</template>

<style scoped>
.card {
  max-width: 560px;
  border-radius: 10px;
}

.who {
  font-size: 13px;
  color: var(--a-text-muted);
  margin-bottom: 16px;
}

.form {
  margin-bottom: 4px;
}

.hint {
  font-size: 12px;
  color: var(--a-text-muted);
  line-height: 1.7;
}
</style>
