<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ApiError } from '@/api/http'
import { useAuth } from '@/stores/auth'
import {
  deleteContent,
  listContents,
  offlineContent,
  publishContent,
  type ContentView,
} from '@/api/content'

const route = useRoute()
const router = useRouter()

// 编辑角色看不到发布、下线入口；服务端对越权调用仍返回 403
const { current } = useAuth()
const canPublish = computed(() => current.value?.role === 'ADMIN')

const contentType = computed(() => String(route.meta.contentType ?? 'PRODUCT'))
const slug = computed(() => String(route.meta.slug ?? 'products'))

const loading = ref(false)
const rows = ref<ContentView[]>([])
const total = ref(0)
const query = reactive({ keyword: '', status: '', unpublishedOnly: false, page: 1, pageSize: 10 })

const statusMeta: Record<string, { text: string; type: 'success' | 'info' | 'warning' | 'danger' }> = {
  PUBLISHED: { text: '已发布', type: 'success' },
  UNPUBLISHED: { text: '草稿', type: 'info' },
  OFFLINE: { text: '已下线', type: 'warning' },
}

/** 后端统一输出 UTC，列表按本地时区显示到分钟 */
function formatTime(value: string | null) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '—'
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

async function load() {
  loading.value = true
  try {
    const data = await listContents({
      contentType: contentType.value,
      keyword: query.keyword || undefined,
      publicationStatus: query.status || undefined,
      hasUnpublishedChanges: query.unpublishedOnly || undefined,
      page: query.page,
      pageSize: query.pageSize,
    })
    rows.value = data.items
    total.value = data.total
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '内容加载失败')
  } finally {
    loading.value = false
  }
}

function reloadFromFirstPage() {
  query.page = 1
  load()
}

function openPreview(row: ContentView) {
  const { href } = router.resolve({ path: `/preview/${slug.value}/${row.id}` })
  window.open(href, '_blank', 'noopener')
}

async function publish(row: ContentView) {
  try {
    await ElMessageBox.confirm(
      `发布后官网立即生效，当前草稿版本 v${row.draftVersion}。确认发布「${row.title}」？`,
      '确认发布',
      { confirmButtonText: '发布', cancelButtonText: '取消' },
    )
    await publishContent(row.id, row.lockVersion, row.draftVersion)
    ElMessage.success('已发布')
    await load()
  } catch (error) {
    if (error instanceof ApiError) ElMessage.error(error.message)
  }
}

async function offline(row: ContentView) {
  try {
    await ElMessageBox.confirm(`下线后官网立即不可访问，快照仍保留，可再次发布。`, '确认下线', {
      confirmButtonText: '下线',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await offlineContent(row.id, row.lockVersion)
    ElMessage.success('已下线')
    await load()
  } catch (error) {
    if (error instanceof ApiError) ElMessage.error(error.message)
  }
}

async function remove(row: ContentView) {
  try {
    await ElMessageBox.confirm(`删除后不可恢复，且已被其他内容引用时无法删除。`, '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteContent(row.id, row.lockVersion)
    ElMessage.success('已删除')
    await load()
  } catch (error) {
    if (error instanceof ApiError) ElMessage.error(error.message)
  }
}

watch(() => route.meta.contentType, reloadFromFirstPage)
onMounted(load)
</script>

<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div class="head">
          <span>{{ route.meta.title }}</span>
          <div class="actions">
            <el-input
              v-model="query.keyword"
              placeholder="搜索标题"
              clearable
              style="width: 180px"
              @keyup.enter="reloadFromFirstPage"
            />
            <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px" @change="reloadFromFirstPage">
              <el-option label="已发布" value="PUBLISHED" />
              <el-option label="草稿" value="UNPUBLISHED" />
              <el-option label="已下线" value="OFFLINE" />
            </el-select>
            <el-checkbox v-model="query.unpublishedOnly" @change="reloadFromFirstPage">仅看有未发布修改</el-checkbox>
            <el-button @click="reloadFromFirstPage">查询</el-button>
            <el-button type="primary" @click="router.push(`/contents/${slug}/new`)">新建</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="rows" border>
        <el-table-column label="封面" width="76">
          <template #default="{ row }">
            <img
              v-if="row.coverMediaId"
              :src="`/api/admin/media/${row.coverMediaId}/file`"
              class="thumb"
              alt=""
            />
            <div v-else class="thumb thumb--empty" aria-hidden="true"></div>
          </template>
        </el-table-column>
        <el-table-column label="标题" min-width="260">
          <template #default="{ row }">
            <div class="title-cell">
              <span>{{ row.title || '（未命名）' }}</span>
              <el-tag v-if="row.hasUnpublishedChanges" size="small" type="warning">有未发布修改</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="130">
          <template #default="{ row }">{{ row.categoryName || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="statusMeta[row.status]?.type ?? 'info'">
              {{ statusMeta[row.status]?.text ?? row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="版本" width="120">
          <template #default="{ row }">
            草稿 v{{ row.draftVersion }} / 上线 v{{ row.publishedVersion ?? '—' }}
          </template>
        </el-table-column>
        <el-table-column label="最后修改" width="150">
          <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/contents/${slug}/${row.id}/edit`)">编辑</el-button>
            <el-button link @click="openPreview(row)">预览</el-button>
            <el-button v-if="canPublish && row.hasUnpublishedChanges" link type="success" @click="publish(row)">发布</el-button>
            <el-button v-if="canPublish && row.status === 'PUBLISHED'" link type="warning" @click="offline(row)">下线</el-button>
            <el-button v-if="canPublish" link type="danger" @click="remove(row)">删除</el-button>
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
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.thumb {
  width: 52px;
  height: 32px;
  border-radius: 3px;
  object-fit: cover;
  background: var(--a-surface);
  display: block;
}

.thumb--empty {
  border: 1px dashed var(--a-border);
}

.pager {
  margin-top: 14px;
  justify-content: flex-end;
}
</style>
