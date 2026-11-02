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
        <span>操作日志</span>
        <div class="head-actions">
          <el-input v-model="query.action" placeholder="动作，如 LOGIN_SUCCESS" clearable style="width: 220px" />
          <el-date-picker
            v-model="query.range"
            type="daterange"
            value-format="YYYY-MM-DD"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 260px"
          />
          <el-button @click="query.page = 1; load()">查询</el-button>
        </div>
      </div>
    </template>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="createdAt" label="时间(UTC)" width="210" />
      <el-table-column label="操作人" width="140">
        <template #default="{ row }">{{ row.actorName || '—' }}</template>
      </el-table-column>
      <el-table-column prop="action" label="动作" width="180" />
      <el-table-column label="对象" width="130">
        <template #default="{ row }">
          {{ row.targetType ? `${row.targetType}#${row.targetId ?? ''}` : '—' }}
        </template>
      </el-table-column>
      <el-table-column label="结果" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.result === 'SUCCESS' ? 'success' : 'danger'">
            {{ row.result }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="summary" label="摘要" />
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
