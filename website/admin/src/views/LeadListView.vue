<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ApiError } from '@/api/http'
import {
  getLead,
  listLeads,
  updateLeadStatus,
  type LeadDetail,
  type LeadListItem,
  type LeadStatus,
} from '@/api/leads'

const STATUS_COLUMNS: Array<{ key: LeadStatus; label: string; tag: 'danger' | 'warning' | 'success' | 'info' }> = [
  { key: 'NEW', label: '待跟进 (NEW)', tag: 'danger' },
  { key: 'CONTACTED', label: '已联系跟进 (CONTACTED)', tag: 'warning' },
  { key: 'CLOSED', label: '已闭环成单 (CLOSED)', tag: 'success' },
  { key: 'INVALID', label: '无效线索 (INVALID)', tag: 'info' },
]

const viewMode = ref<'table' | 'kanban'>('table')
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

const detailOpen = ref(false)
const detailLoading = ref(false)
const currentLead = ref<LeadDetail | null>(null)
const followForm = reactive<{ status: LeadStatus; note: string }>({
  status: 'CONTACTED',
  note: '',
})
const savingFollow = ref(false)

const kanbanGroups = computed(() => {
  const map: Record<LeadStatus, LeadListItem[]> = {
    NEW: [],
    CONTACTED: [],
    CLOSED: [],
    INVALID: [],
  }
  for (const r of rows.value) {
    if (map[r.status]) map[r.status].push(r)
  }
  return map
})

function isOverdueSla(row: LeadListItem): boolean {
  if (row.status !== 'NEW' || !row.createdAt) return false
  const created = new Date(row.createdAt).getTime()
  if (Number.isNaN(created)) return false
  return Date.now() - created > 24 * 3600 * 1000
}

function formatTime(value: string | null | undefined) {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  return d.toLocaleString('zh-CN', { hour12: false })
}

async function load() {
  loading.value = true
  try {
    const res = await listLeads({
      status: query.status || undefined,
      keyword: query.keyword.trim() || undefined,
      from: query.range[0] || undefined,
      to: query.range[1] || undefined,
      page: query.page,
      pageSize: query.pageSize,
    })
    rows.value = res.items
    total.value = res.total
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '咨询线索加载失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.status = ''
  query.keyword = ''
  query.range = []
  query.page = 1
  load()
}

async function openLeadDetail(row: LeadListItem) {
  detailOpen.value = true
  detailLoading.value = true
  try {
    const detail = await getLead(row.id)
    currentLead.value = detail
    followForm.status = detail.status === 'NEW' ? 'CONTACTED' : detail.status
    followForm.note = detail.lastNote ?? ''
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '详情加载失败')
    detailOpen.value = false
  } finally {
    detailLoading.value = false
  }
}

async function saveFollowUp() {
  if (!currentLead.value) return
  if (
    (currentLead.value.status === 'CLOSED' || currentLead.value.status === 'INVALID') &&
    followForm.status === 'CONTACTED' &&
    !followForm.note.trim()
  ) {
    ElMessage.warning('将已关闭或无效线索重新激活为跟进中时，必须填写跟进备注说明原因')
    return
  }
  savingFollow.value = true
  try {
    await updateLeadStatus(currentLead.value.id, {
      status: followForm.status,
      note: followForm.note.trim(),
      expectedLockVersion: currentLead.value.lockVersion,
    })
    ElMessage.success('线索状态与跟进记录已更新')
    detailOpen.value = false
    await load()
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '保存失败')
  } finally {
    savingFollow.value = false
  }
}

function exportLeadsCsv() {
  if (!rows.value.length) {
    ElMessage.warning('当前列表没有可导出的线索')
    return
  }
  const headers = ['单号', '姓名', '公司名称', '脱敏电话', '来源位置', '关联内容', '状态', '处理人', '提交时间']
  const lines = [
    headers.join(','),
    ...rows.value.map((r) =>
      [
        r.receiptNo,
        `"${(r.name || '').replace(/"/g, '""')}"`,
        `"${(r.companyName || '').replace(/"/g, '""')}"`,
        r.phoneMasked,
        r.sourcePosition,
        `"${(r.contentTitle || '-').replace(/"/g, '""')}"`,
        r.status,
        r.handledByName || '-',
        formatTime(r.createdAt),
      ].join(','),
    ),
  ]
  const blob = new Blob(['\uFEFF' + lines.join('\n')], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `leads-p${query.page}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('已导出当前页脱敏线索列表 CSV')
}

onMounted(load)
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <div class="head">
        <div>
          <span class="head__title">客户咨询线索管理</span>
          <span class="head__sub">列表默认展示末四位脱敏手机号；点击「查看并跟进」触发审计并解密完整号码</span>
        </div>
        <div class="head__actions">
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button value="table">📋 表格视图</el-radio-button>
            <el-radio-button value="kanban">🗂️ 看板视图 (Kanban)</el-radio-button>
          </el-radio-group>
          <el-button size="small" plain @click="exportLeadsCsv">导出当前页 CSV</el-button>
          <el-button size="small" @click="load">刷新</el-button>
        </div>
      </div>
    </template>

    <div class="filters">
      <el-select
        v-model="query.status"
        placeholder="全部状态"
        clearable
        style="width: 160px"
        @change="query.page = 1; load()"
      >
        <el-option v-for="col in STATUS_COLUMNS" :key="col.key" :label="col.label" :value="col.key" />
      </el-select>
      <el-input
        v-model="query.keyword"
        placeholder="搜索姓名 / 公司名称..."
        clearable
        style="width: 220px"
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
    </div>

    <!-- 模式 A：表格视图 -->
    <el-table v-if="viewMode === 'table'" v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="receiptNo" label="回执单号" width="135" />
      <el-table-column label="客户姓名" width="140">
        <template #default="{ row }">
          <strong>{{ row.name }}</strong>
          <el-tag v-if="isOverdueSla(row)" size="small" type="danger" style="margin-left: 6px">
            超时未跟进
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="companyName" label="公司名称" min-width="180" />
      <el-table-column prop="phoneMasked" label="联系电话(脱敏)" width="135" />
      <el-table-column label="来源位置 / 关联内容" min-width="180">
        <template #default="{ row }">
          <span>{{ row.sourcePosition }}</span>
          <span v-if="row.contentTitle" class="sub-txt"> · {{ row.contentTitle }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag
            size="small"
            :type="
              row.status === 'NEW'
                ? 'danger'
                : row.status === 'CONTACTED'
                ? 'warning'
                : row.status === 'CLOSED'
                ? 'success'
                : 'info'
            "
          >
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="handledByName" label="跟进人" width="110">
        <template #default="{ row }">{{ row.handledByName || '-' }}</template>
      </el-table-column>
      <el-table-column label="提交时间" width="168">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="130" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openLeadDetail(row)">查看并跟进</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 模式 B：Kanban 看板视图 -->
    <div v-else v-loading="loading" class="kanban">
      <div v-for="col in STATUS_COLUMNS" :key="col.key" class="kanban__col">
        <div class="kanban__head">
          <strong>{{ col.label }}</strong>
          <el-tag size="small" :type="col.tag">{{ kanbanGroups[col.key].length }}</el-tag>
        </div>
        <div class="kanban__list">
          <div
            v-for="item in kanbanGroups[col.key]"
            :key="item.id"
            class="kanban-card"
            :class="{ 'kanban-card--overdue': isOverdueSla(item) }"
            @click="openLeadDetail(item)"
          >
            <div class="kanban-card__top">
              <strong>{{ item.name }}</strong>
              <span class="kanban-card__phone">{{ item.phoneMasked }}</span>
            </div>
            <p class="kanban-card__company">{{ item.companyName }}</p>
            <div class="kanban-card__foot">
              <span>{{ item.sourcePosition }}</span>
              <span>{{ formatTime(item.createdAt) }}</span>
            </div>
          </div>
          <p v-if="kanbanGroups[col.key].length === 0" class="kanban__empty">暂无该状态线索</p>
        </div>
      </div>
    </div>

    <el-pagination
      class="pager"
      layout="total, prev, pager, next"
      :total="total"
      :page-size="query.pageSize"
      :current-page="query.page"
      @current-change="(p: number) => { query.page = p; load() }"
    />

    <!-- 线索详情与状态流转弹窗 -->
    <el-dialog v-model="detailOpen" title="客户咨询详情与跟进记录" width="620px">
      <div v-loading="detailLoading">
        <el-descriptions v-if="currentLead" :column="2" border>
          <el-descriptions-item label="回执单号">{{ currentLead.receiptNo }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag size="small">{{ currentLead.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="客户姓名">{{ currentLead.name }}</el-descriptions-item>
          <el-descriptions-item label="完整联系电话">
            <strong class="phone-full">{{ currentLead.phone }}</strong>
          </el-descriptions-item>
          <el-descriptions-item label="公司名称" :span="2">{{ currentLead.companyName }}</el-descriptions-item>
          <el-descriptions-item label="来源路径">{{ currentLead.sourcePath }}</el-descriptions-item>
          <el-descriptions-item label="来源位置">{{ currentLead.sourcePosition }}</el-descriptions-item>
          <el-descriptions-item label="关联内容" :span="2">
            {{ currentLead.contentTitle ? `${currentLead.contentType} #${currentLead.contentId} - ${currentLead.contentTitle}` : '无特定关联' }}
          </el-descriptions-item>
          <el-descriptions-item label="隐私协议版本">v{{ currentLead.consentVersion }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatTime(currentLead.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="咨询需求描述" :span="2">
            <div class="demand-box">{{ currentLead.demand || '-' }}</div>
          </el-descriptions-item>
        </el-descriptions>

        <el-form v-if="currentLead" label-position="top" class="follow-form">
          <el-form-item label="流转状态">
            <el-radio-group v-model="followForm.status">
              <el-radio value="CONTACTED">已联系跟进 (CONTACTED)</el-radio>
              <el-radio value="CLOSED">已闭环完成 (CLOSED)</el-radio>
              <el-radio value="INVALID">标记无效 (INVALID)</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="内部跟进备注 (最多 2000 字)">
            <el-input
              v-model="followForm.note"
              type="textarea"
              :rows="3"
              maxlength="2000"
              show-word-limit
              placeholder="记录沟通情况、客户预算或下一步安排..."
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="detailOpen = false">关闭</el-button>
        <el-button type="primary" :loading="savingFollow" @click="saveFollowUp">保存跟进结果</el-button>
      </template>
    </el-dialog>
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
  font-size: 16px;
  font-weight: 600;
}

.head__sub {
  margin-left: 10px;
  font-size: 12px;
  color: var(--a-text-muted);
}

.head__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filters {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.sub-txt {
  color: var(--a-text-muted);
  font-size: 12px;
}

/* 看板视图 */
.kanban {
  display: grid;
  grid-template-columns: repeat(4, minmax(220px, 1fr));
  gap: 14px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.kanban__col {
  background: #f6f8fb;
  border: 1px solid var(--a-border);
  border-radius: 10px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  min-height: 360px;
}

.kanban__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 13.5px;
}

.kanban__list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.kanban-card {
  background: #ffffff;
  border: 1px solid var(--a-border);
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.kanban-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.06);
}

.kanban-card--overdue {
  border-left: 3px solid #ef4444;
}

.kanban-card__top {
  display: flex;
  justify-content: space-between;
  font-size: 13.5px;
}

.kanban-card__phone {
  font-size: 12px;
  color: #5b4be8;
  font-family: monospace;
}

.kanban-card__company {
  margin: 6px 0;
  font-size: 12.5px;
  color: var(--a-text-muted);
}

.kanban-card__foot {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #9ca3af;
}

.kanban__empty {
  text-align: center;
  font-size: 12px;
  color: var(--a-text-muted);
  padding: 32px 0;
}

.phone-full {
  color: #5b4be8;
  font-size: 15px;
  font-family: monospace;
}

.demand-box {
  white-space: pre-wrap;
  word-break: break-word;
  background: #f8f9fc;
  padding: 10px 12px;
  border-radius: 6px;
  line-height: 1.6;
}

.follow-form {
  margin-top: 16px;
}

.pager {
  margin-top: 14px;
  justify-content: flex-end;
}
</style>
