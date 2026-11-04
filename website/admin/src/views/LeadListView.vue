<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ApiError } from '@/api/http'
import { getLead, listLeads, updateLeadStatus, type LeadDetail, type LeadListItem, type LeadStatus } from '@/api/leads'

const loading = ref(false)
const rows = ref<LeadListItem[]>([])
const total = ref(0)
const query = reactive({
  status: '',
  keyword: '',
  range: [] as string[],
  page: 1,
  pageSize: 20,
})

const statusOptions: Array<{ value: LeadStatus | ''; label: string }> = [
  { value: '', label: '全部状态' },
  { value: 'NEW', label: '待处理' },
  { value: 'CONTACTED', label: '已联系' },
  { value: 'CLOSED', label: '已关闭' },
  { value: 'INVALID', label: '无效' },
]

const sourceLabels: Record<string, string> = {
  HOME_HERO: '首页首屏',
  HOME_BOTTOM: '首页底部',
  PRODUCT_DETAIL: '产品详情',
  SOLUTION_DETAIL: '方案详情',
  CASE_DETAIL: '案例详情',
  CONTACT: '联系页',
  FLOATING: '悬浮入口',
  NAVIGATION: '导航',
  FOOTER: '页脚',
  OTHER_CTA: '其他入口',
}

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<LeadDetail | null>(null)
const handling = reactive({ status: '' as LeadStatus | '', note: '' })
const submitting = ref(false)

const nextStatuses = computed<Array<{ value: LeadStatus; label: string }>>(() => {
  switch (detail.value?.status) {
    case 'NEW':
      return [
        { value: 'CONTACTED', label: '标记为已联系' },
        { value: 'INVALID', label: '标记为无效' },
      ]
    case 'CONTACTED':
      return [
        { value: 'CLOSED', label: '关闭线索' },
        { value: 'INVALID', label: '标记为无效' },
      ]
    case 'CLOSED':
    case 'INVALID':
      return [{ value: 'CONTACTED', label: '重新跟进' }]
    default:
      return []
  }
})

const noteRequired = computed(() =>
  detail.value ? detail.value.status === 'CLOSED' || detail.value.status === 'INVALID' : false,
)

async function load() {
  loading.value = true
  try {
    const params: Parameters<typeof listLeads>[0] = {
      status: query.status || undefined,
      keyword: query.keyword.trim() || undefined,
      page: query.page,
      pageSize: query.pageSize,
    }
    if (query.range.length === 2) {
      params.from = query.range[0]
      params.to = query.range[1]
    }
    const data = await listLeads(params)
    rows.value = data.items
    total.value = data.total
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '咨询列表加载失败')
  } finally {
    loading.value = false
  }
}

async function openDetail(row: LeadListItem) {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  handling.note = ''
  try {
    const data = await getLead(row.id)
    detail.value = data
    handling.status = nextStatuses.value[0]?.value ?? ''
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '咨询详情加载失败')
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

async function submitStatus() {
  if (!detail.value || !handling.status) {
    ElMessage.warning('请选择要变更的状态')
    return
  }
  if (noteRequired.value && !handling.note.trim()) {
    ElMessage.warning('重新跟进必须填写原因')
    return
  }
  submitting.value = true
  try {
    await updateLeadStatus(detail.value.id, {
      status: handling.status,
      note: handling.note.trim(),
      expectedLockVersion: detail.value.lockVersion,
    })
    ElMessage.success('处理结果已保存')
    detailVisible.value = false
    await load()
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '处理失败，请重试')
  } finally {
    submitting.value = false
  }
}

function resetQuery() {
  query.status = ''
  query.keyword = ''
  query.range = []
  query.page = 1
  load()
}

function exportCsv() {
  if (rows.value.length === 0) {
    ElMessage.info('当前列表暂无数据可导出')
    return
  }
  const headers = ['回执号', '姓名', '公司', '手机号', '来源位置', '关联内容', '状态', '处理人', '提交时间']
  const csvRows = [headers.join(',')]

  for (const row of rows.value) {
    const line = [
      `"${row.receiptNo ?? ''}"`,
      `"${(row.name ?? '').replace(/"/g, '""')}"`,
      `"${(row.companyName ?? '').replace(/"/g, '""')}"`,
      `"${row.phoneMasked ?? ''}"`,
      `"${sourceLabel(row.sourcePosition)}"`,
      `"${(row.contentTitle ?? '').replace(/"/g, '""')}"`,
      `"${statusLabel(row.status)}"`,
      `"${row.handledByName ?? ''}"`,
      `"${formatTime(row.createdAt)}"`,
    ]
    csvRows.push(line.join(','))
  }

  const blob = new Blob(['\uFEFF' + csvRows.join('\r\n')], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `咨询线索_${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('已导出当前咨询线索数据')
}

async function copyPhone(phone: string) {
  try {
    await navigator.clipboard.writeText(phone)
    ElMessage.success('手机号已复制')
  } catch {
    ElMessage.warning(`复制失败：${phone}`)
  }
}

function statusLabel(status: string) {
  return statusOptions.find((item) => item.value === status)?.label ?? status
}

function statusTagType(status: string) {
  if (status === 'NEW') return 'warning'
  if (status === 'CONTACTED') return 'primary'
  if (status === 'CLOSED') return 'info'
  return 'danger'
}

function sourceLabel(status: string) {
  return sourceLabels[status] ?? status
}

function formatTime(value?: string | null) {
  if (!value) return '—'
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

onMounted(load)
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <div class="head">
        <span>咨询管理</span>
        <div class="head-actions">
          <el-select v-model="query.status" style="width: 130px">
            <el-option v-for="item in statusOptions" :key="item.value" :value="item.value" :label="item.label" />
          </el-select>
          <el-input
            v-model="query.keyword"
            placeholder="姓名或公司关键字"
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
          <el-button type="success" plain @click="exportCsv">导出 CSV</el-button>
        </div>
      </div>
    </template>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="提交时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="companyName" label="公司" min-width="160" show-overflow-tooltip />
      <el-table-column prop="phoneMasked" label="手机" width="120" />
      <el-table-column label="来源" width="120">
        <template #default="{ row }">{{ sourceLabel(row.sourcePosition) }}</template>
      </el-table-column>
      <el-table-column label="关联内容" min-width="140">
        <template #default="{ row }">{{ row.contentTitle || '—' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处理人" width="110">
        <template #default="{ row }">{{ row.handledByName || '—' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pager"
      :total="total"
      v-model:page-size="query.pageSize"
      v-model:current-page="query.page"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @current-change="load"
      @size-change="query.page = 1; load()"
    />
  </el-card>

  <el-dialog v-model="detailVisible" title="咨询详情" width="720px" destroy-on-close>
    <div v-loading="detailLoading">
      <div v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="回执号">{{ detail.receiptNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag size="small" :type="statusTagType(detail.status)">{{ statusLabel(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="姓名">{{ detail.name }}</el-descriptions-item>
          <el-descriptions-item label="公司">{{ detail.companyName }}</el-descriptions-item>
          <el-descriptions-item label="手机号">
            <div style="display: flex; align-items: center; gap: 8px;">
              <span>{{ detail.phone }}</span>
              <el-button size="small" link type="primary" @click="copyPhone(detail.phone)">复制</el-button>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatTime(detail.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="来源位置">{{ sourceLabel(detail.sourcePosition) }}</el-descriptions-item>
          <el-descriptions-item label="来源路径">{{ detail.sourcePath }}</el-descriptions-item>
          <el-descriptions-item label="关联内容">{{ detail.contentTitle || '—' }}</el-descriptions-item>
          <el-descriptions-item label="隐私同意">
            版本 {{ detail.consentVersion }} · {{ formatTime(detail.consentAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="处理人">{{ detail.handledByName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ formatTime(detail.handledAt) }}</el-descriptions-item>
          <el-descriptions-item label="最近备注" :span="2">{{ detail.lastNote || '—' }}</el-descriptions-item>
        </el-descriptions>

        <div class="demand">
          <p class="demand__label">需求描述</p>
          <p class="demand__text">{{ detail.demand }}</p>
        </div>

        <div class="handle">
          <p class="handle__title">处理</p>
          <div class="handle__row">
            <el-select v-model="handling.status" placeholder="选择目标状态" style="width: 200px">
              <el-option
                v-for="item in nextStatuses"
                :key="item.value"
                :value="item.value"
                :label="item.label"
              />
            </el-select>
            <el-input
              v-model="handling.note"
              type="textarea"
              :rows="2"
              maxlength="2000"
              show-word-limit
              :placeholder="noteRequired ? '重新跟进必须填写原因' : '处理备注（可选）'"
            />
          </div>
          <el-button type="primary" :loading="submitting" @click="submitStatus">保存处理结果</el-button>
        </div>
      </div>
    </div>
  </el-dialog>
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

.demand {
  margin-top: 16px;
  padding: 14px 16px;
  background: var(--a-surface, #f7f8fa);
  border-radius: 8px;
}

.demand__label {
  font-size: 12px;
  color: var(--a-text-muted);
  margin-bottom: 6px;
}

.demand__text {
  font-size: 14px;
  line-height: 1.8;
  white-space: pre-wrap;
}

.handle {
  margin-top: 20px;
  padding-top: 18px;
  border-top: 1px solid var(--a-border, #e5e7eb);
}

.handle__title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 12px;
}

.handle__row {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 12px;
}

.handle__row :deep(.el-textarea) {
  flex: 1;
}

@media (max-width: 720px) {
  .handle__row {
    flex-direction: column;
  }
  .handle__row :deep(.el-select) {
    width: 100% !important;
  }
}
</style>
