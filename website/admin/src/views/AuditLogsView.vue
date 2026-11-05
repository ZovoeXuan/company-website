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

const QUICK_ACTIONS = [
  { label: '全部操作', value: '' },
  { label: '登录认证', value: 'LOGIN' },
  { label: '配置发布', value: 'CONFIG_PUBLISH' },
  { label: '内容发布', value: 'CONTENT_PUBLISH' },
  { label: '内容下线', value: 'CONTENT_OFFLINE' },
  { label: '线索跟进', value: 'LEAD' },
  { label: '账号变更', value: 'USER' },
]

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
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function selectQuickAction(act: string) {
  query.action = act
  query.page = 1
  load()
}

function resetQuery() {
  query.action = ''
  query.range = []
  query.page = 1
  load()
}

function exportCurrentCsv() {
  if (!rows.value.length) {
    ElMessage.warning('当前页暂无日志可导出')
    return
  }
  const headers = ['ID', '时间', '操作人', '动作', '目标类型', '目标ID', '结果', '摘要']
  const csvRows = [
    headers.join(','),
    ...rows.value.map((r) =>
      [
        r.id,
        formatTime(r.createdAt),
        r.actorName || '-',
        r.action,
        r.targetType || '-',
        r.targetId || '-',
        r.result,
        `"${(r.summary || '').replace(/"/g, '""')}"`,
      ].join(','),
    ),
  ]
  const blob = new Blob(['\uFEFF' + csvRows.join('\n')], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `cms-audit-logs-p${query.page}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('已导出当前页审计日志 CSV')
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
        <div>
          <span class="head__title">操作审计日志</span>
          <span class="head__sub">记录全站登录、账号变更、发布下线与线索查阅痕迹（只读不可篡改）</span>
        </div>
        <div class="filters">
          <el-input
            v-model="query.action"
            placeholder="动作代码（如 LOGIN / PUBLISH）"
            clearable
            style="width: 210px"
            @keyup.enter="query.page = 1; load()"
          />
          <el-date-picker
            v-model="query.range"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 250px"
          />
          <el-button type="primary" @click="query.page = 1; load()">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button plain @click="exportCurrentCsv">导出当前页 CSV</el-button>
        </div>
      </div>
    </template>

    <div class="quick-bar">
      <button
        v-for="item in QUICK_ACTIONS"
        :key="item.label"
        type="button"
        class="quick-chip"
        :class="{ 'is-active': query.action === item.value }"
        @click="selectQuickAction(item.value)"
      >
        {{ item.label }}
      </button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column label="时间" width="175">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column prop="actorName" label="操作人" width="130">
        <template #default="{ row }">{{ row.actorName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="action" label="动作" width="170" />
      <el-table-column label="对象" width="160">
        <template #default="{ row }">
          {{ row.targetType ? `${row.targetType}${row.targetId ? '#' + row.targetId : ''}` : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="结果" width="95" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="row.result === 'SUCCESS' ? 'success' : 'danger'">
            {{ row.result }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="summary" label="摘要说明" min-width="240" />
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

.head__title {
  font-weight: 600;
  font-size: 16px;
}

.head__sub {
  margin-left: 10px;
  font-size: 12px;
  color: var(--a-text-muted);
}

.filters {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.quick-bar {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.quick-chip {
  padding: 5px 12px;
  border-radius: 999px;
  border: 1px solid var(--a-border);
  background: #fafbfe;
  font-size: 12px;
  color: var(--a-text-muted);
  cursor: pointer;
}

.quick-chip.is-active {
  background: #5b4be8;
  border-color: #5b4be8;
  color: #ffffff;
}

.pager {
  margin-top: 14px;
  justify-content: flex-end;
}
</style>
