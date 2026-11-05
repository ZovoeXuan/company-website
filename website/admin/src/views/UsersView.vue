<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { ApiError, get, request } from '@/api/http'
import { useAuth } from '@/stores/auth'

interface UserRow {
  id: string
  username: string
  displayName: string
  role: 'ADMIN' | 'EDITOR'
  enabled: boolean
  lockVersion: number
  lastLoginAt?: string
}

const { current } = useAuth()
const loading = ref(false)
const rows = ref<UserRow[]>([])
const total = ref(0)
const query = reactive({ keyword: '', page: 1, pageSize: 10 })

const formVisible = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const formRef = ref<FormInstance>()
const submitting = ref(false)
const editing = reactive({
  id: '',
  lockVersion: 0,
  username: '',
  displayName: '',
  roleCode: 'EDITOR' as 'ADMIN' | 'EDITOR',
  enabled: true,
  initialPassword: '',
})

const original = reactive({ enabled: true, roleCode: 'EDITOR' as 'ADMIN' | 'EDITOR' })

const rules: FormRules = {
  username: [
    { required: true, message: '请输入登录账号', trigger: 'blur' },
    {
      pattern: /^[A-Za-z0-9_-]{4,50}$/,
      message: '账号需为 4~50 位英文字母、数字、下划线或连字符',
      trigger: 'blur',
    },
  ],
  displayName: [{ required: true, message: '请输入显示名称', trigger: 'blur' }],
  initialPassword: [
    {
      validator: (_rule, value, callback) => {
        if (formMode.value === 'create') {
          if (!value || String(value).length < 12) {
            callback(new Error('新建账号初始密码至少 12 位'))
            return
          }
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
}

function formatTime(value?: string | null) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

async function load() {
  loading.value = true
  try {
    const data = await get<{ items: UserRow[]; total: number }>(
      `/api/admin/users?page=${query.page}&pageSize=${query.pageSize}&keyword=${encodeURIComponent(query.keyword)}`,
    )
    rows.value = data.items
    total.value = data.total
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '账号列表加载失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  formMode.value = 'create'
  Object.assign(editing, {
    id: '',
    lockVersion: 0,
    username: '',
    displayName: '',
    roleCode: 'EDITOR',
    enabled: true,
    initialPassword: '',
  })
  Object.assign(original, { enabled: true, roleCode: 'EDITOR' })
  formVisible.value = true
}

function openEdit(row: UserRow) {
  formMode.value = 'edit'
  Object.assign(editing, {
    id: row.id,
    lockVersion: row.lockVersion,
    username: row.username,
    displayName: row.displayName,
    roleCode: row.role,
    enabled: row.enabled,
    initialPassword: '',
  })
  Object.assign(original, { enabled: row.enabled, roleCode: row.role })
  formVisible.value = true
}

async function confirmSensitiveChange() {
  if (formMode.value !== 'edit') return true
  if (original.enabled && !editing.enabled) {
    try {
      await ElMessageBox.confirm(
        `禁用后「${editing.displayName || editing.username}」将立即退出登录，且无法再进入后台。确认禁用？`,
        '禁用账号确认',
        { type: 'warning', confirmButtonText: '确认禁用', cancelButtonText: '取消' },
      )
    } catch {
      return false
    }
  }
  if (original.roleCode === 'ADMIN' && editing.roleCode === 'EDITOR') {
    try {
      await ElMessageBox.confirm(
        `降级后「${editing.displayName || editing.username}」将失去发布、下线、删除与咨询处理权限。确认降级？`,
        '角色变更确认',
        { type: 'warning', confirmButtonText: '确认降级', cancelButtonText: '取消' },
      )
    } catch {
      return false
    }
  }
  return true
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (!(await confirmSensitiveChange())) return
    submitting.value = true
    try {
      if (formMode.value === 'create') {
        await request('/api/admin/users', {
          method: 'POST',
          body: {
            username: editing.username.trim(),
            displayName: editing.displayName.trim(),
            roleCode: editing.roleCode,
            initialPassword: editing.initialPassword,
          },
        })
        ElMessage.success('账号新建成功')
      } else {
        await request(`/api/admin/users/${editing.id}`, {
          method: 'PUT',
          body: {
            expectedLockVersion: editing.lockVersion,
            displayName: editing.displayName.trim(),
            roleCode: editing.roleCode,
            enabled: editing.enabled,
          },
        })
        ElMessage.success('账号更新成功')
      }
      formVisible.value = false
      await load()
    } catch (error) {
      ElMessage.error(error instanceof ApiError ? error.message : '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

async function resetPassword(row: UserRow) {
  try {
    const { value: newPassword } = await ElMessageBox.prompt(
      `请为「${row.displayName || row.username}」输入新密码（建议至少 12 位），重置后该账号所有在线会话将立即失效：`,
      '重置密码',
      {
        inputType: 'password',
        inputPlaceholder: '请输入至少 12 位新密码',
        inputValidator: (val) => {
          if (!val || val.length < 6) return '密码长度至少 6 位（建议 12 位以上）'
          if (val.length > 128) return '密码长度不能超过 128 位'
          return true
        },
        confirmButtonText: '确认重置',
        cancelButtonText: '取消',
      },
    )
    await request(`/api/admin/users/${row.id}/reset-password`, {
      method: 'POST',
      body: {
        expectedLockVersion: row.lockVersion,
        newPassword,
      },
    })
    ElMessage.success('密码已重置，该账号原有会话已全部撤销')
    await load()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error instanceof ApiError ? error.message : '重置失败')
    }
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div class="head">
          <span>后台账号管理</span>
          <div class="actions">
            <el-input
              v-model="query.keyword"
              placeholder="搜索账号或显示名称"
              clearable
              style="width: 220px"
              @keyup.enter="query.page = 1; load()"
            />
            <el-button @click="query.page = 1; load()">查询</el-button>
            <el-button type="primary" @click="openCreate">新建账号</el-button>
          </div>
        </div>
      </template>

      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="账号不提供物理删除以保留操作审计关联；禁用、降级或重置密码会立即撤销该账号所有登录会话。"
        class="tip"
      />

      <el-table v-loading="loading" :data="rows" border>
        <el-table-column prop="username" label="登录账号" min-width="140" />
        <el-table-column prop="displayName" label="显示名称" min-width="140" />
        <el-table-column label="角色" width="130">
          <template #default="{ row }">
            <el-tag size="small" :type="row.role === 'ADMIN' ? 'danger' : 'primary'">
              {{ row.role === 'ADMIN' ? 'ADMIN 管理员' : 'EDITOR 编辑' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? '启用' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最后登录时间" width="170">
          <template #default="{ row }">{{ formatTime(row.lastLoginAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="resetPassword(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pager"
        layout="total, prev, pager, next"
        :total="total"
        :page-size="query.pageSize"
        :current-page="query.page"
        @current-change="(p: number) => { query.page = p; load() }"
      />
    </el-card>

    <el-dialog v-model="formVisible" :title="formMode === 'create' ? '新建后台账号' : '编辑后台账号'" width="460px">
      <el-form ref="formRef" :model="editing" :rules="rules" label-width="96px">
        <el-form-item label="登录账号" prop="username">
          <el-input
            v-model="editing.username"
            :disabled="formMode === 'edit'"
            maxlength="50"
            placeholder="4~50 位字母、数字、_ 或 -"
          />
        </el-form-item>
        <el-form-item label="显示名称" prop="displayName">
          <el-input v-model="editing.displayName" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="角色权限" required>
          <el-select v-model="editing.roleCode" style="width: 100%">
            <el-option label="EDITOR 内容编辑（保存草稿、上传素材、预览）" value="EDITOR" />
            <el-option label="ADMIN 系统管理员（发布、下线、管理线索与账号）" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="formMode === 'create'" label="初始密码" prop="initialPassword" required>
          <el-input
            v-model="editing.initialPassword"
            type="password"
            show-password
            placeholder="建议至少 12 位，最长 128 位"
          />
        </el-form-item>
        <el-form-item v-if="formMode === 'edit'" label="启用状态">
          <el-switch v-model="editing.enabled" :disabled="editing.id === current?.id" />
          <span v-if="editing.id === current?.id" class="self-hint">不能禁用当前登录账号自身</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.actions {
  display: flex;
  gap: 8px;
}

.tip {
  margin-bottom: 14px;
}

.self-hint {
  margin-left: 10px;
  font-size: 12px;
  color: var(--a-text-muted);
}

.pager {
  margin-top: 14px;
  justify-content: flex-end;
}
</style>
