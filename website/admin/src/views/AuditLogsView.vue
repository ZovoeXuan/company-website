<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ApiError, get } from '@/api/http'

interface AuditRow {
  id: string
  actorName?: string
  action: string
  targetType?: string
  targetId?: string
  result: 'SUCCESS' | 'FAILURE'
  summary: string
  createdAt: string
}

const loading = ref(false)
const rows = ref<AuditRow[]>([])
const total = ref(0)
const query = reactive({
  action: '',
  range: [] as string[],
  page: 1,
  pageSize: 20,
})

function formatTime(value: string | null) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function resetQuery() {
  query.action = ''
  query.range = []
  query.page = 1
  load()
}

async function load() {
  loading.value = true
  try {
    const params = new URLSearchParams({
      page: String(query.page),
      pageSize: String(query.pageSize),
    })
    if (query.action.trim()) params.set('action', query.action.trim())
    if (query.range.length === 2) {
      params.set('from', query.range[0])
      params.set('to', query.range[1])
    }
    const data = await get<{ items: AuditRow[]; total: number }>(
      `/api/admin/audit-logs?${params.toString()}`,
    )
    rows.value = data.items
    total.value = data.total
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '日志加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <div class="head">
        <span>操作审计日志</span>
        <div class="head-actions">
          <el-input
            v-model="query.action"
            placeholder="动作关键字，如 LOGIN"
            clearable
            style="width: 200px"
            @keyup.enter="query.page = 1; load()"
          />
          <el-date-picker
            v-model="query.range"
            type="daterange"
            value-format="YYYY-MM-DD"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 250px"
          />
          <el-button type="primary" @click="query.page = 1; load()">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </div>
      </div>
    </template>

    <el-table v-loading="loading" :data="rows" border stripe style="width: 100%">
      <el-table-column label="操作时间" width="180">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作账号" width="140">
        <template #default="{ row }">{{ row.actorName || '系统' }}</template>
      </el-table-column>
      <el-table-column prop="action" label="操作动作" width="180">
        <template #default="{ row }">
          <el-tag size="small" type="info" effect="plain">{{ row.action }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="目标对象" width="140">
        <template #default="{ row }">
          {{ row.targetType ? `${row.targetType} #${row.targetId ?? ''}` : '—' }}
        </template>
      </el-table-column>
      <el-table-column label="执行结果" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="row.result === 'SUCCESS' ? 'success' : 'danger'">
            {{ row.result === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="summary" label="日志详情" min-width="260" show-overflow-tooltip />
    </el-table>

    <el-pagination
      v-model:current-page="query.page"
      v-model:page-size="query.pageSize"
      :page-sizes="[20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      class="pager"
      @current-change="load"
      @size-change="query.page = 1; load()"
    />
  </el-card>
</template>

<style scoped>
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
  flex-wrap: wrap;
}

.pager {
  margin-top: 14px;
  justify-content: flex-end;
}
</style>
