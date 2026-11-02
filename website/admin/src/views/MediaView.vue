<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ApiError, upload } from '@/api/http'
import { deleteMedia, listMedia, mediaReferences, type MediaItem } from '@/api/content'

const loading = ref(false)
const uploading = ref(false)
const rows = ref<MediaItem[]>([])
const total = ref(0)
const query = reactive({ keyword: '', page: 1, pageSize: 20 })
const fileInput = ref<HTMLInputElement>()

const refDialog = ref(false)
const refLoading = ref(false)
const refRows = ref<Array<Record<string, string>>>([])

async function load() {
  loading.value = true
  try {
    const data = await listMedia({ keyword: query.keyword || undefined, page: query.page, pageSize: query.pageSize })
    rows.value = data.items
    total.value = data.total
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '素材加载失败')
  } finally {
    loading.value = false
  }
}

async function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const form = new FormData()
    form.append('file', file)
    await upload<MediaItem>('/api/admin/media', form)
    ElMessage.success('上传成功')
    query.page = 1
    await load()
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '上传失败')
  } finally {
    uploading.value = false
    input.value = ''
  }
}

async function copyUrl(media: MediaItem) {
  const url = `${location.origin}${media.url}`
  try {
    await navigator.clipboard.writeText(url)
    ElMessage.success('已复制公开地址（内容发布后可用）')
  } catch {
    ElMessage.warning(`复制失败，请手动复制：${url}`)
  }
}

async function showReferences(media: MediaItem) {
  refDialog.value = true
  refLoading.value = true
  refRows.value = []
  try {
    refRows.value = await mediaReferences(media.id)
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '引用查询失败')
  } finally {
    refLoading.value = false
  }
}

async function remove(media: MediaItem) {
  try {
    await ElMessageBox.confirm('删除后不可恢复；仍被内容引用时会被拒绝。', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteMedia(media.id)
    ElMessage.success('已删除')
    await load()
  } catch (error) {
    if (error instanceof ApiError) ElMessage.error(error.message)
  }
}

function formatSize(bytes: number) {
  return bytes >= 1024 * 1024 ? `${(bytes / 1024 / 1024).toFixed(1)} MB` : `${Math.round(bytes / 1024)} KB`
}

/** 接口返回 UTC ISO 字符串，列表按本地时区展示到分钟。 */
function formatTime(value: string | null) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

onMounted(load)
</script>

<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div class="head">
          <span>素材管理</span>
          <div class="actions">
            <el-input
              v-model="query.keyword"
              placeholder="按文件名搜索"
              clearable
              style="width: 200px"
              @keyup.enter="query.page = 1; load()"
            />
            <el-button @click="query.page = 1; load()">查询</el-button>
            <input ref="fileInput" type="file" accept="image/png,image/jpeg,image/webp" hidden @change="onFileChange" />
            <el-button type="primary" :loading="uploading" @click="fileInput?.click()">上传图片</el-button>
          </div>
        </div>
      </template>

      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="支持 JPG、PNG、WebP，单张不超过 5 MB、2000 万像素。公开地址只在内容发布后可访问。"
        class="tip"
      />

      <el-table v-loading="loading" :data="rows" border>
        <el-table-column label="预览" width="120">
          <template #default="{ row }">
            <img :src="`/api/admin/media/${row.id}/file`" :alt="row.originalName" class="thumb" loading="lazy" />
          </template>
        </el-table-column>
        <el-table-column prop="originalName" label="文件名" min-width="200" />
        <el-table-column label="尺寸" width="120">
          <template #default="{ row }">{{ row.width }}×{{ row.height }}</template>
        </el-table-column>
        <el-table-column label="大小" width="100">
          <template #default="{ row }">{{ formatSize(row.byteSize) }}</template>
        </el-table-column>
        <el-table-column label="引用" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.referenced ? 'success' : 'info'">
              {{ row.referenced ? '已引用' : '未引用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="上传时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="copyUrl(row)">复制地址</el-button>
            <el-button link type="primary" @click="showReferences(row)">引用</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
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

    <el-dialog v-model="refDialog" title="引用位置" width="560px">
      <div v-loading="refLoading">
        <el-empty v-if="!refLoading && refRows.length === 0" description="尚未被任何内容引用" />
        <el-table v-else :data="refRows" border size="small">
          <el-table-column prop="stage" label="阶段" width="100" />
          <el-table-column label="来源" min-width="140">
            <template #default="{ row }">{{ row.sourceKind }} #{{ row.sourceId }}</template>
          </el-table-column>
          <el-table-column prop="fieldPath" label="字段" min-width="160" />
        </el-table>
      </div>
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

.thumb {
  width: 96px;
  height: 72px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid var(--a-border);
}

.pager {
  margin-top: 14px;
  justify-content: flex-end;
}
</style>
