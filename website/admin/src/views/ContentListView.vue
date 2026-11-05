<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ApiError } from '@/api/http'
import {
  cloneContent,
  deleteContent,
  listCategories,
  listContents,
  offlineContent,
  publishContent,
  type CategoryView,
  type ContentView,
} from '@/api/content'
import { useAuth } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const { current } = useAuth()
const isAdmin = computed(() => current.value?.role === 'ADMIN')

const contentType = computed(() => String(route.meta.contentType || 'PRODUCT'))
const slug = computed(() => String(route.meta.slug || 'products'))
const pageTitle = computed(() => String(route.meta.title || '内容管理'))
const hasCategory = computed(() => contentType.value !== 'CUSTOMER')

const loading = ref(false)
const batchRunning = ref(false)
const rows = ref<ContentView[]>([])
const total = ref(0)
const categories = ref<CategoryView[]>([])
const selectedRows = ref<ContentView[]>([])

const query = reactive({
  keyword: '',
  categoryId: '',
  publicationStatus: '',
  hasUnpublishedChanges: false,
  page: 1,
  pageSize: 20,
})

async function loadCategories() {
  if (!hasCategory.value) {
    categories.value = []
    return
  }
  try {
    const res = await listCategories(contentType.value)
    categories.value = res.items
  } catch {
    categories.value = []
  }
}

async function load() {
  loading.value = true
  try {
    const data = await listContents({
      contentType: contentType.value,
      keyword: query.keyword.trim() || undefined,
      categoryId: query.categoryId || undefined,
      publicationStatus: query.publicationStatus || undefined,
      hasUnpublishedChanges: query.hasUnpublishedChanges || undefined,
      page: query.page,
      pageSize: query.pageSize,
    })
    rows.value = data.items
    total.value = data.total
    selectedRows.value = []
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '内容列表加载失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.keyword = ''
  query.categoryId = ''
  query.publicationStatus = ''
  query.hasUnpublishedChanges = false
  query.page = 1
  load()
}

function onSelectionChange(val: ContentView[]) {
  selectedRows.value = val
}

function goCreate() {
  router.push(`/contents/${slug.value}/create`)
}

function goEdit(row: ContentView) {
  router.push(`/contents/${slug.value}/${row.id}/edit`)
}

function goPreview(row: ContentView) {
  router.push(`/preview/${slug.value}/${row.id}`)
}

async function handleClone(row: ContentView) {
  try {
    const created = await cloneContent(row.id)
    ElMessage.success(`已克隆生成新草稿：#${created.content.id}`)
    await load()
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '克隆失败')
  }
}

async function handlePublish(row: ContentView) {
  try {
    await ElMessageBox.confirm(`确认将「${row.title}」(v${row.draftVersion}) 正式发布到线上官网？`, '确认发布', {
      confirmButtonText: '立即发布',
      cancelButtonText: '取消',
    })
    await publishContent(row.id, row.lockVersion, row.draftVersion)
    ElMessage.success('发布成功')
    await load()
  } catch (error) {
    if (error instanceof ApiError) ElMessage.error(error.message)
  }
}

async function handleOffline(row: ContentView) {
  try {
    await ElMessageBox.confirm(`下线后「${row.title}」将从公开列表中移除，确认下线？`, '确认下线', {
      type: 'warning',
      confirmButtonText: '确认下线',
      cancelButtonText: '取消',
    })
    await offlineContent(row.id, row.lockVersion, row.draftVersion)
    ElMessage.success('已下线')
    await load()
  } catch (error) {
    if (error instanceof ApiError) ElMessage.error(error.message)
  }
}

async function handleDelete(row: ContentView) {
  try {
    await ElMessageBox.confirm(`删除后不可恢复（已被首页或其他内容引用的条目无法删除），确认删除「${row.title}」？`, '确认删除', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deleteContent(row.id, row.lockVersion)
    ElMessage.success('已删除')
    await load()
  } catch (error) {
    if (error instanceof ApiError) ElMessage.error(error.message)
  }
}

async function batchPublish() {
  if (!selectedRows.value.length) return
  try {
    await ElMessageBox.confirm(`确认批量发布已勾选的 ${selectedRows.value.length} 条内容？`, '批量发布', {
      confirmButtonText: '批量发布',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  batchRunning.value = true
  let ok = 0
  let fail = 0
  for (const row of selectedRows.value) {
    try {
      await publishContent(row.id, row.lockVersion, row.draftVersion)
      ok++
    } catch {
      fail++
    }
  }
  batchRunning.value = false
  ElMessage.success(`批量发布完成：成功 ${ok} 条${fail ? `，失败 ${fail} 条（请检查必填项）` : ''}`)
  await load()
}

async function batchOffline() {
  const pubRows = selectedRows.value.filter((r) => r.status === 'PUBLISHED')
  if (!pubRows.length) {
    ElMessage.warning('所选条目中没有处于「已发布」状态的内容')
    return
  }
  try {
    await ElMessageBox.confirm(`确认批量下线已勾选的 ${pubRows.length} 条已发布内容？`, '批量下线', {
      type: 'warning',
      confirmButtonText: '批量下线',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  batchRunning.value = true
  let ok = 0
  for (const row of pubRows) {
    try {
      await offlineContent(row.id, row.lockVersion, row.draftVersion)
      ok++
    } catch {
      // ignore single failure
    }
  }
  batchRunning.value = false
  ElMessage.success(`已批量下线 ${ok} 条内容`)
  await load()
}

function formatTime(value: string | null) {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  return d.toLocaleString('zh-CN', { hour12: false })
}

watch(
  () => route.path,
  () => {
    query.keyword = ''
    query.categoryId = ''
    query.publicationStatus = ''
    query.hasUnpublishedChanges = false
    query.page = 1
    loadCategories()
    load()
  },
)

onMounted(() => {
  loadCategories()
  load()
})
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <div class="head">
        <div>
          <span class="head__title">{{ pageTitle }}</span>
          <span class="head__sub">草稿修改与线上发布快照双版本隔离保护</span>
        </div>
        <div class="actions">
          <el-button @click="load">刷新</el-button>
          <el-button type="primary" @click="goCreate">+ 新建内容</el-button>
        </div>
      </div>
    </template>

    <!-- 筛选栏 -->
    <div class="filters">
      <el-input
        v-model="query.keyword"
        placeholder="按标题搜索..."
        clearable
        style="width: 220px"
        @keyup.enter="query.page = 1; load()"
      />
      <el-select
        v-if="hasCategory"
        v-model="query.categoryId"
        placeholder="全部分类"
        clearable
        style="width: 160px"
        @change="query.page = 1; load()"
      >
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-select
        v-model="query.publicationStatus"
        placeholder="全部状态"
        clearable
        style="width: 140px"
        @change="query.page = 1; load()"
      >
        <el-option label="草稿 (UNPUBLISHED)" value="UNPUBLISHED" />
        <el-option label="已发布 (PUBLISHED)" value="PUBLISHED" />
        <el-option label="已下线 (OFFLINE)" value="OFFLINE" />
      </el-select>
      <el-checkbox v-model="query.hasUnpublishedChanges" @change="query.page = 1; load()">
        仅看有未发布修改
      </el-checkbox>
      <el-button type="primary" plain @click="query.page = 1; load()">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <!-- 批量操作浮动条 -->
    <div v-if="selectedRows.length > 0 && isAdmin" class="batch-bar">
      <span>已选择 <strong>{{ selectedRows.length }}</strong> 项</span>
      <div class="batch-bar__btns">
        <el-button size="small" type="success" :loading="batchRunning" @click="batchPublish">
          批量发布选中项
        </el-button>
        <el-button size="small" type="warning" :loading="batchRunning" @click="batchOffline">
          批量下线选中项
        </el-button>
      </div>
    </div>

    <el-table
      v-loading="loading"
      :data="rows"
      border
      stripe
      @selection-change="onSelectionChange"
    >
      <el-table-column v-if="isAdmin" type="selection" width="46" />
      <el-table-column prop="id" label="ID" width="76" />
      <el-table-column label="标题" min-width="240">
        <template #default="{ row }">
          <div class="title-cell">
            <span class="title-cell__text">{{ row.title }}</span>
            <el-tag v-if="row.hasUnpublishedChanges" size="small" type="warning">有未发布修改</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column v-if="hasCategory" label="分类" width="130">
        <template #default="{ row }">{{ row.categoryName || '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag
            size="small"
            :type="row.status === 'PUBLISHED' ? 'success' : row.status === 'OFFLINE' ? 'warning' : 'info'"
          >
            {{ row.status === 'PUBLISHED' ? '已发布' : row.status === 'OFFLINE' ? '已下线' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="版本 (草稿/线上)" width="130" align="center">
        <template #default="{ row }">
          v{{ row.draftVersion }} / {{ row.publishedVersion ? `v${row.publishedVersion}` : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="85" align="center" />
      <el-table-column label="最后更新" width="168">
        <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="290" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="goEdit(row)">编辑</el-button>
          <el-button v-if="contentType !== 'CUSTOMER'" link type="info" @click="goPreview(row)">预览</el-button>
          <el-button link @click="handleClone(row)">克隆副本</el-button>
          <template v-if="isAdmin">
            <el-button
              v-if="row.hasUnpublishedChanges || row.status !== 'PUBLISHED'"
              link
              type="success"
              @click="handlePublish(row)"
            >
              发布
            </el-button>
            <el-button v-if="row.status === 'PUBLISHED'" link type="warning" @click="handleOffline(row)">
              下线
            </el-button>
            <el-button v-if="row.status !== 'PUBLISHED'" link type="danger" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
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

.actions {
  display: flex;
  gap: 8px;
}

.filters {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.batch-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  margin-bottom: 12px;
  background: #f5f3ff;
  border: 1px solid #c4b5fd;
  border-radius: 8px;
  font-size: 13px;
  color: #3730a3;
}

.batch-bar__btns {
  display: flex;
  gap: 8px;
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.title-cell__text {
  font-weight: 600;
}

.pager {
  margin-top: 14px;
  justify-content: flex-end;
}
</style>
