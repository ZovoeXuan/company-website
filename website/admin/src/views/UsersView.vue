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

const rules: FormRules = {
  displayName: [{ required: true, message: '请输入显示名称', trigger: 'blur' }],
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
    id: '', lockVersion: 0, username: '', displayName: '', roleCode: 'EDITOR', enabled: true, initialPassword: '',
  })
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
  formVisible.value = true
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (formMode.value === 'create') {
        await request('/api/admin/users', {
          method: 'POST',
          body: {
            username: editing.username,
            displayName: editing.displayName,
            roleCode: editing.roleCode,
            initialPassword: editing.initialPassword,
          },
        })
        ElMessage.success('账号已创建')
      } else {
        await request(`/api/admin/users/${editing.id}`, {
          method: 'PUT',
          body: {
            expectedLockVersion: editing.lockVersion,
            displayName: editing.displayName,
            roleCode: editing.roleCode,
            enabled: editing.enabled,
          },
        })
        ElMessage.success('账号已更新')
      }
      formVisible.value = false
      await load()
    } catch (error) {
      ElMessage.error(error instanceof ApiError ? error.message : '保存失败')
    } finally {
      submitting.value = false
    }
  })
}

async function resetPassword(row: UserRow) {
  try {
    const { value } = await ElMessageBox.prompt(
      `为 ${row.displayName}（${row.username}）设置新密码，长度至少 12 位。保存后该账号所有会话将被撤销。`,
      '重置密码',
      { inputType: 'password', inputPlaceholder: '请输入新密码', confirmButtonText: '确认重置', cancelButtonText: '取消' },
    )
    if (!value || value.length < 12) {
      ElMessage.warning('密码长度至少 12 位')
      return
    }
    await request(`/api/admin/users/${row.id}/reset-password`, {
      method: 'POST',
      body: { expectedLockVersion: row.lockVersion, newPassword: value },
    })
    ElMessage.success('密码已重置，该账号需重新登录')
    await load()
  } catch (error) {
    if (error instanceof ApiError) {
      ElMessage.error(error.message)
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
          <span>账号管理</span>
          <div class="head-actions">
            <el-input
              v-model="query.keyword"
              placeholder="搜索账号或名称"
              clearable
              style="width: 200px"
              @keyup.enter="query.page = 1; load()"
            />
            <el-button @click="query.page = 1; load()">查询</el-button>
            <el-button type="primary" @click="openCreate">新建账号</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="rows" border>
        <el-table-column prop="username" label="账号" width="150" />
        <el-table-column prop="displayName" label="显示名称" />
        <el-table-column label="角色" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="row.role === 'ADMIN' ? 'danger' : 'info'">{{ row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最后登录" width="190">
          <template #default="{ row }">{{ row.lastLoginAt || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="resetPassword(row)">重置密码</el-button>
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

    <el-dialog v-model="formVisible" :title="formMode === 'create' ? '新建账号' : '编辑账号'" width="460px">
      <el-form ref="formRef" :model="editing" :rules="rules" label-width="90px">
        <el-form-item label="账号">
          <el-input v-model="editing.username" :disabled="formMode === 'edit'" placeholder="4~50 位字母、数字、下划线、连字符" />
        </el-form-item>
        <el-form-item label="显示名称" prop="displayName">
          <el-input v-model="editing.displayName" maxlength="50" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editing.roleCode" style="width: 100%">
            <el-option label="管理员 ADMIN" value="ADMIN" />
            <el-option label="编辑 EDITOR" value="EDITOR" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="formMode === 'create'" label="初始密码">
          <el-input v-model="editing.initialPassword" type="password" placeholder="至少 12 位" />
        </el-form-item>
        <el-form-item v-else label="状态">
          <el-switch v-model="editing.enabled" active-text="启用" inactive-text="禁用" />
        </el-form-item>
        <p v-if="formMode === 'edit' && editing.id === current?.id" class="warn">
          正在编辑当前登录账号，禁用或降级后会立即失去对应用权限。
        </p>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.head-actions {
  display: flex;
  gap: 8px;
}

.pager {
  margin-top: 14px;
  justify-content: flex-end;
}

.warn {
  font-size: 12px;
  color: #b26a00;
  line-height: 1.6;
}
</style>
