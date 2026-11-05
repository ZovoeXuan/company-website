<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ApiError, upload } from '@/api/http'
import { deleteMedia, listMedia, mediaReferences, type MediaItem } from '@/api/content'
import { useAuth } from '@/stores/auth'

const { current } = useAuth()
const isAdmin = computed(() => current.value?.role === 'ADMIN')

const loading = ref(false)
const uploading = ref(false)
const dragOver = ref(false)
const items = ref<MediaItem[]>([])
const total = ref(0)
const fileInput = ref<HTMLInputElement>()

const query = reactive({
  keyword: '',
  type: '' as '' | 'IMAGE' | 'VIDEO',
  referencedFilter: 'ALL' as 'ALL' | 'YES' | 'NO',
  page: 1,
  pageSize: 20,
})

const refDrawerOpen = ref(false)
const refLoading = ref(false)
const currentMedia = ref<MediaItem | null>(null)
const refList = ref<Array<Record<string, string>>>([])

async function load() {
  loading.value = true
  try {
    const res = await listMedia({
      keyword: query.keyword.trim() || undefined,
      type: query.type || undefined,
      referenced:
        query.referencedFilter === 'YES'
          ? true
          : query.referencedFilter === 'NO'
          ? false
          : undefined,
      page: query.page,
      pageSize: query.pageSize,
    })
    items.value = res.items
    total.value = res.total
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '素材库加载失败')
  } finally {
    loading.value = false
  }
}

async function handleFiles(fileList: FileList | null | undefined) {
  if (!fileList || fileList.length === 0) return
  uploading.value = true
  let ok = 0
  for (const file of Array.from(fileList)) {
    try {
      const form = new FormData()
      form.append('file', file)
      await upload<MediaItem>('/api/admin/media', form)
      ok++
    } catch (error) {
      ElMessage.error(error instanceof ApiError ? `${file.name}: ${error.message}` : `${file.name} 上传失败`)
    }
  }
  uploading.value = false
  if (ok > 0) {
    ElMessage.success(`成功上传 ${ok} 个素材文件`)
    query.page = 1
    await load()
  }
}

async function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  await handleFiles(input.files)
  input.value = ''
}

async function onDrop(event: DragEvent) {
  dragOver.value = false
  await handleFiles(event.dataTransfer?.files)
}

async function openRefs(item: MediaItem) {
  currentMedia.value = item
  refDrawerOpen.value = true
  refLoading.value = true
  try {
    refList.value = await mediaReferences(item.id)
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '引用查询失败')
    refList.value = []
  } finally {
    refLoading.value = false
  }
}

async function copyMediaId(item: MediaItem) {
  try {
    await navigator.clipboard.writeText(String(item.id))
    ElMessage.success(`已复制素材 ID: #${item.id}`)
  } catch {
    // ignore
  }
}

async function handleDelete(item: MediaItem) {
  if (item.referenced) {
    ElMessage.warning('该素材当前正被草稿或已发布内容引用，请先解除引用后再删除')
    return
  }
  try {
    await ElMessageBox.confirm(`确认删除无引用素材「${item.originalName}」(#${item.id})？`, '确认删除', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deleteMedia(item.id)
    ElMessage.success('已删除')
    await load()
  } catch (error) {
    if (error instanceof ApiError) ElMessage.error(error.message)
  }
}

function formatSize(bytes: number) {
  if (!bytes) return '0 KB'
  if (bytes < 1024 * 1024) return `${Math.max(1, Math.round(bytes / 1024))} KB`
  return `${(bytes / (1024 * 1024)).toFixed(2)} MB`
}

onMounted(load)
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <div class="head">
        <div>
          <span class="head__title">素材资源库</span>
          <span class="head__sub">受控存储图片与视频，支持查看被哪些配置或文章引用，防止误删线上图片</span>
        </div>
        <div class="actions">
          <input
            ref="fileInput"
            type="file"
            multiple
            accept="image/png,image/jpeg,image/webp,video/mp4,video/webm"
            hidden
            @change="onFileChange"
          />
          <el-button @click="load">刷新</el-button>
          <el-button type="primary" :loading="uploading" @click="fileInput?.click()">
            + 上传图片 / 视频
          </el-button>
        </div>
      </div>
    </template>

    <!-- 拖拽上传热区 -->
    <div
      class="drop-banner"
      :class="{ 'is-dragover': dragOver }"
      @dragover.prevent="dragOver = true"
      @dragleave.prevent="dragOver = false"
      @drop.prevent="onDrop"
    >
      <span>📤 将 JPG / PNG / WebP 图片（≤5MB）或 MP4 / WebM 视频（≤50MB）直接拖拽到此处即可批量上传</span>
    </div>

    <div class="filters">
      <el-input
        v-model="query.keyword"
        placeholder="搜索原始文件名..."
        clearable
        style="width: 220px"
        @keyup.enter="query.page = 1; load()"
      />
      <el-select v-model="query.type" style="width: 130px" @change="query.page = 1; load()">
        <el-option label="全部类型" value="" />
        <el-option label="图片 (IMAGE)" value="IMAGE" />
        <el-option label="视频 (VIDEO)" value="VIDEO" />
      </el-select>
      <el-select v-model="query.referencedFilter" style="width: 150px" @change="query.page = 1; load()">
        <el-option label="全部引用状态" value="ALL" />
        <el-option label="已被引用" value="YES" />
        <el-option label="未被引用（可清理）" value="NO" />
      </el-select>
      <el-button type="primary" plain @click="query.page = 1; load()">查询</el-button>
    </div>

    <div v-loading="loading" class="media-grid">
      <div v-for="item in items" :key="item.id" class="media-card">
        <div class="media-card__thumb">
          <video
            v-if="item.mediaType === 'VIDEO'"
            :src="`/api/admin/media/${item.id}/file`"
            muted
            playsinline
            preload="metadata"
          ></video>
          <img v-else :src="`/api/admin/media/${item.id}/file`" :alt="item.originalName" loading="lazy" />
          <span class="media-card__id">#{{ item.id }}</span>
          <el-tag
            size="small"
            :type="item.referenced ? 'success' : 'info'"
            class="media-card__ref-tag"
          >
            {{ item.referenced ? '使用中' : '未引用' }}
          </el-tag>
        </div>

        <div class="media-card__body">
          <p class="media-card__name" :title="item.originalName">{{ item.originalName }}</p>
          <p class="media-card__meta">
            {{ item.mediaType === 'VIDEO' ? '视频' : `${item.width}×${item.height}` }} · {{ formatSize(item.byteSize) }}
          </p>
          <div class="media-card__actions">
            <el-button link size="small" type="primary" @click="openRefs(item)">引用详情</el-button>
            <el-button link size="small" @click="copyMediaId(item)">复制ID</el-button>
            <el-button
              v-if="isAdmin"
              link
              size="small"
              type="danger"
              :disabled="item.referenced"
              @click="handleDelete(item)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && items.length === 0" description="暂无符合条件的素材" />
    </div>

    <el-pagination
      class="pager"
      layout="total, prev, pager, next"
      :total="total"
      :page-size="query.pageSize"
      :current-page="query.page"
      @current-change="(p: number) => { query.page = p; load() }"
    />

    <!-- 引用关系抽屉 -->
    <el-drawer
      v-model="refDrawerOpen"
      :title="`素材引用追踪：${currentMedia?.originalName || ''} (#${currentMedia?.id || ''})`"
      size="460px"
    >
      <div v-loading="refLoading">
        <p class="drawer-tip">
          只要素材存在任何草稿（DRAFT）或线上发布（PUBLISHED）引用，系统将自动锁死删除操作，保障官网不出现裂图。
        </p>
        <el-table v-if="refList.length" :data="refList" border stripe size="small">
          <el-table-column prop="sourceKind" label="来源类型" width="100" />
          <el-table-column prop="sourceId" label="来源标识" width="100" />
          <el-table-column prop="stage" label="阶段" width="95">
            <template #default="{ row }">
              <el-tag size="small" :type="row.stage === 'PUBLISHED' ? 'success' : 'warning'">
                {{ row.stage }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="fieldPath" label="引用字段路径" min-width="130" />
        </el-table>
        <el-empty v-else description="当前没有任何配置或内容引用该素材，管理员可安全删除" />
      </div>
    </el-drawer>
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

.drop-banner {
  margin-bottom: 14px;
  padding: 12px 16px;
  border-radius: 10px;
  border: 1px dashed #a5b4fc;
  background: #f5f3ff;
  color: #4338ca;
  font-size: 13px;
  text-align: center;
  transition: all 0.2s ease;
}

.drop-banner.is-dragover {
  background: #ede9fe;
  border-color: #5b4be8;
  transform: scale(1.005);
}

.filters {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  min-height: 200px;
}

.media-card {
  border: 1px solid var(--a-border);
  border-radius: 10px;
  overflow: hidden;
  background: #ffffff;
  display: flex;
  flex-direction: column;
}

.media-card__thumb {
  position: relative;
  aspect-ratio: 16 / 10;
  background: #0f172a;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.media-card__thumb img,
.media-card__thumb video {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.media-card__id {
  position: absolute;
  left: 8px;
  top: 8px;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.65);
  color: #ffffff;
}

.media-card__ref-tag {
  position: absolute;
  right: 8px;
  top: 8px;
}

.media-card__body {
  padding: 10px 12px;
}

.media-card__name {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.media-card__meta {
  margin: 4px 0 8px;
  font-size: 11.5px;
  color: var(--a-text-muted);
}

.media-card__actions {
  display: flex;
  justify-content: space-between;
  padding-top: 8px;
  border-top: 1px solid var(--a-border);
}

.drawer-tip {
  font-size: 12.5px;
  color: var(--a-text-muted);
  line-height: 1.6;
  margin: 0 0 14px;
}

.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
