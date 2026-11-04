<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ApiError, upload } from '@/api/http'
import { deleteMedia, listMedia, mediaReferences, type MediaItem } from '@/api/content'

const loading = ref(false)
const uploading = ref(false)
const rows = ref<MediaItem[]>([])
const total = ref(0)
const viewMode = ref<'grid' | 'table'>('grid')
const currentOrigin = ref(typeof window !== 'undefined' ? window.location.origin : '')

const query = reactive({
  keyword: '',
  type: '' as '' | 'IMAGE' | 'VIDEO',
  referencedFilter: '' as '' | 'yes' | 'no',
  page: 1,
  pageSize: 24,
})

const fileInput = ref<HTMLInputElement>()
const dragOver = ref(false)

// 详情/全屏预览对话框
const previewVisible = ref(false)
const activeMedia = ref<MediaItem | null>(null)

// 引用位置弹窗
const refDialog = ref(false)
const refLoading = ref(false)
const refRows = ref<Array<Record<string, string>>>([])
const refMediaName = ref('')

async function load() {
  loading.value = true
  try {
    const data = await listMedia({
      keyword: query.keyword.trim() || undefined,
      type: query.type || undefined,
      referenced: query.referencedFilter === 'yes' ? true : undefined,
      page: query.page,
      pageSize: query.pageSize,
    })
    // 如果筛选未引用（前端过滤后端暂不支持反向），在前端过滤
    if (query.referencedFilter === 'no') {
      rows.value = data.items.filter((item) => !item.referenced)
      total.value = rows.value.length
    } else {
      rows.value = data.items
      total.value = data.total
    }
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '素材加载失败')
  } finally {
    loading.value = false
  }
}

async function handleUploadFiles(files: FileList | File[]) {
  if (!files || files.length === 0) return
  uploading.value = true
  let successCount = 0
  let failCount = 0

  for (let i = 0; i < files.length; i++) {
    const file = files[i]
    try {
      const form = new FormData()
      form.append('file', file)
      await upload<MediaItem>('/api/admin/media', form)
      successCount++
    } catch (error) {
      failCount++
      const msg = error instanceof ApiError ? error.message : `${file.name} 上传失败`
      ElMessage.error(msg)
    }
  }

  if (successCount > 0) {
    ElMessage.success(`成功上传 ${successCount} 个素材${failCount > 0 ? `，${failCount} 个失败` : ''}`)
    query.page = 1
    await load()
  }
  uploading.value = false
}

function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  if (input.files) {
    handleUploadFiles(input.files)
  }
  input.value = ''
}

function onDrop(event: DragEvent) {
  dragOver.value = false
  if (event.dataTransfer?.files) {
    handleUploadFiles(event.dataTransfer.files)
  }
}

function openLightbox(media: MediaItem) {
  activeMedia.value = media
  previewVisible.value = true
}

async function copyUrl(media: MediaItem) {
  const url = `${location.origin}${media.url}`
  try {
    await navigator.clipboard.writeText(url)
    ElMessage.success('已复制公开地址（内容发布后生效）')
  } catch {
    ElMessage.warning(`复制失败，请手动复制：${url}`)
  }
}

async function showReferences(media: MediaItem) {
  refMediaName.value = media.originalName
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
    await ElMessageBox.confirm(`确定要永久删除素材「${media.originalName}」吗？被内容引用的素材无法删除。`, '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteMedia(media.id)
    ElMessage.success('素材已删除')
    if (previewVisible.value && activeMedia.value?.id === media.id) {
      previewVisible.value = false
    }
    await load()
  } catch (error) {
    if (error instanceof ApiError) ElMessage.error(error.message)
  }
}

function formatSize(bytes: number) {
  return bytes >= 1024 * 1024 ? `${(bytes / 1024 / 1024).toFixed(1)} MB` : `${Math.round(bytes / 1024)} KB`
}

function formatDuration(ms: number | null) {
  if (!ms) return '—'
  const total = Math.round(ms / 1000)
  return `${Math.floor(total / 60)}:${String(total % 60).padStart(2, '0')}`
}

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
  <div class="media-page">
    <el-card shadow="never" class="media-card">
      <template #header>
        <div class="media-header">
          <div class="title-wrap">
            <span class="title">素材管理</span>
            <el-tag size="small" type="info">共 {{ total }} 个资源</el-tag>
          </div>

          <div class="header-actions">
            <el-input
              v-model="query.keyword"
              placeholder="按文件名搜索…"
              clearable
              style="width: 180px"
              @keyup.enter="query.page = 1; load()"
              @clear="query.page = 1; load()"
            />
            <el-select v-model="query.type" style="width: 100px" @change="query.page = 1; load()">
              <el-option value="" label="全部类型" />
              <el-option value="IMAGE" label="图片" />
              <el-option value="VIDEO" label="视频" />
            </el-select>
            <el-select v-model="query.referencedFilter" style="width: 110px" @change="query.page = 1; load()">
              <el-option value="" label="引用状态" />
              <el-option value="yes" label="仅已引用" />
              <el-option value="no" label="仅未引用" />
            </el-select>
            <el-button @click="query.page = 1; load()">查询</el-button>

            <el-radio-group v-model="viewMode" size="default" class="view-switch">
              <el-radio-button label="grid">画廊</el-radio-button>
              <el-radio-button label="table">列表</el-radio-button>
            </el-radio-group>

            <input
              ref="fileInput"
              type="file"
              multiple
              accept="image/png,image/jpeg,image/webp,video/mp4,video/webm"
              hidden
              @change="onFileChange"
            />
            <el-button type="primary" :loading="uploading" @click="fileInput?.click()">
              上传素材
            </el-button>
          </div>
        </div>
      </template>

      <!-- 拖拽上传区域 -->
      <div
        class="dropzone"
        :class="{ 'dropzone--active': dragOver, 'dropzone--uploading': uploading }"
        @dragover.prevent="dragOver = true"
        @dragleave.prevent="dragOver = false"
        @drop.prevent="onDrop"
        @click="fileInput?.click()"
      >
        <div class="dropzone-content">
          <svg class="dropzone-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
            <polyline points="17 8 12 3 7 8" />
            <line x1="12" y1="3" x2="12" y2="15" />
          </svg>
          <div class="dropzone-text">
            <span class="dropzone-title">拖拽图片或视频到此处，或点击浏览文件批量上传</span>
            <span class="dropzone-hint">支持 JPG、PNG、WebP（≤5MB）及 MP4、WebM 视频（≤50MB）</span>
          </div>
        </div>
      </div>

      <!-- 画廊视图 -->
      <div v-if="viewMode === 'grid'" v-loading="loading" class="gallery-container">
        <el-empty v-if="!loading && rows.length === 0" description="暂无符合条件的素材" />
        <div v-else class="gallery-grid">
          <div
            v-for="item in rows"
            :key="item.id"
            class="gallery-item"
            :class="{ 'gallery-item--video': item.mediaType === 'VIDEO' }"
          >
            <!-- 缩略图容器 -->
            <div class="thumb-wrap" @click="openLightbox(item)">
              <video
                v-if="item.mediaType === 'VIDEO'"
                :src="`/api/admin/media/${item.id}/file`"
                class="thumb-media"
                muted
                playsinline
                preload="metadata"
              />
              <img
                v-else
                :src="`/api/admin/media/${item.id}/file`"
                :alt="item.originalName"
                class="thumb-media"
                loading="lazy"
              />
              <div class="thumb-overlay">
                <span class="thumb-action">查看大图</span>
              </div>
              <!-- 类型与尺寸标签 -->
              <div class="thumb-badges">
                <span v-if="item.mediaType === 'VIDEO'" class="badge badge-video">
                  ▶ {{ formatDuration(item.durationMs) }}
                </span>
                <span v-else class="badge badge-res">
                  {{ item.width }}×{{ item.height }}
                </span>
                <span class="badge badge-size">{{ formatSize(item.byteSize) }}</span>
              </div>
            </div>

            <!-- 信息区 -->
            <div class="item-body">
              <div class="item-name" :title="item.originalName">{{ item.originalName }}</div>
              <div class="item-meta">
                <el-tag size="small" :type="item.referenced ? 'success' : 'info'" effect="plain">
                  {{ item.referenced ? '已引用' : '未引用' }}
                </el-tag>
                <span class="item-time">{{ formatTime(item.createdAt) }}</span>
              </div>
              <div class="item-actions">
                <el-button link type="primary" size="small" @click="copyUrl(item)">复制地址</el-button>
                <el-button link type="primary" size="small" @click="showReferences(item)">引用 ({{ item.referenced ? '有' : '0' }})</el-button>
                <el-button link type="danger" size="small" @click="remove(item)">删除</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 表格视图 -->
      <div v-else v-loading="loading">
        <el-table :data="rows" border stripe style="width: 100%">
          <el-table-column label="预览" width="110">
            <template #default="{ row }">
              <div class="table-thumb" @click="openLightbox(row)">
                <video
                  v-if="row.mediaType === 'VIDEO'"
                  :src="`/api/admin/media/${row.id}/file`"
                  class="table-thumb-img"
                  muted
                  playsinline
                  preload="metadata"
                />
                <img
                  v-else
                  :src="`/api/admin/media/${row.id}/file`"
                  :alt="row.originalName"
                  class="table-thumb-img"
                  loading="lazy"
                />
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="originalName" label="文件名" min-width="200" show-overflow-tooltip />
          <el-table-column label="类型" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="row.mediaType === 'VIDEO' ? 'warning' : 'info'">
                {{ row.mediaType === 'VIDEO' ? '视频' : '图片' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="尺寸 / 时长" width="130">
            <template #default="{ row }">
              {{ row.mediaType === 'VIDEO' ? formatDuration(row.durationMs) : `${row.width}×${row.height}` }}
            </template>
          </el-table-column>
          <el-table-column label="文件大小" width="100">
            <template #default="{ row }">{{ formatSize(row.byteSize) }}</template>
          </el-table-column>
          <el-table-column label="引用状态" width="100">
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
      </div>

      <!-- 分页栏 -->
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :page-sizes="[12, 24, 48, 96]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="load"
          @size-change="query.page = 1; load()"
        />
      </div>
    </el-card>

    <!-- 素材全屏预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      :title="activeMedia?.originalName ?? '素材详情'"
      width="780px"
      destroy-on-close
      class="media-preview-modal"
    >
      <div v-if="activeMedia" class="preview-layout">
        <div class="preview-stage">
          <video
            v-if="activeMedia.mediaType === 'VIDEO'"
            :src="`/api/admin/media/${activeMedia.id}/file`"
            controls
            autoplay
            class="stage-video"
          />
          <img
            v-else
            :src="`/api/admin/media/${activeMedia.id}/file`"
            :alt="activeMedia.originalName"
            class="stage-image"
          />
        </div>
        <el-descriptions :column="2" border size="small" class="preview-info">
          <el-descriptions-item label="文件名" :span="2">{{ activeMedia.originalName }}</el-descriptions-item>
          <el-descriptions-item label="素材类型">
            {{ activeMedia.mediaType === 'VIDEO' ? 'MP4/WebM 视频' : '图片' }}
          </el-descriptions-item>
          <el-descriptions-item label="MIME 类型">{{ activeMedia.mimeType }}</el-descriptions-item>
          <el-descriptions-item label="分辨率 / 时长">
            {{ activeMedia.mediaType === 'VIDEO' ? formatDuration(activeMedia.durationMs) : `${activeMedia.width} × ${activeMedia.height} px` }}
          </el-descriptions-item>
          <el-descriptions-item label="文件大小">{{ formatSize(activeMedia.byteSize) }}</el-descriptions-item>
          <el-descriptions-item label="上传时间">{{ formatTime(activeMedia.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="引用状态">
            <el-tag size="small" :type="activeMedia.referenced ? 'success' : 'info'">
              {{ activeMedia.referenced ? '已引用' : '未引用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="公开地址" :span="2">
            <div class="url-copy-box">
              <span class="url-text">{{ currentOrigin }}{{ activeMedia.url }}</span>
              <el-button size="small" type="primary" plain @click="copyUrl(activeMedia)">复制</el-button>
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <div class="preview-actions">
          <el-button v-if="activeMedia" @click="showReferences(activeMedia)">查看引用</el-button>
          <el-button v-if="activeMedia" type="danger" plain @click="remove(activeMedia)">删除素材</el-button>
          <el-button type="primary" @click="previewVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 引用位置弹窗 -->
    <el-dialog v-model="refDialog" :title="`「${refMediaName}」的引用位置`" width="600px">
      <div v-loading="refLoading">
        <el-empty v-if="!refLoading && refRows.length === 0" description="尚未被任何内容或配置引用，可安全删除。" />
        <el-table v-else :data="refRows" border size="small">
          <el-table-column prop="stage" label="发布阶段" width="110">
            <template #default="{ row }">
              <el-tag size="small" :type="row.stage === 'PUBLISHED' ? 'success' : 'warning'">
                {{ row.stage === 'PUBLISHED' ? '线上发布版' : '后台草稿' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="来源对象" min-width="140">
            <template #default="{ row }">
              {{ row.sourceKind }} #{{ row.sourceId }}
            </template>
          </el-table-column>
          <el-table-column prop="fieldPath" label="字段路径" min-width="180" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.media-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.media-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: var(--a-text-primary);
}

.header-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.view-switch {
  margin-left: 4px;
}

/* 拖拽上传区 */
.dropzone {
  border: 1.5px dashed var(--a-border);
  border-radius: 10px;
  padding: 18px 24px;
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.02);
  cursor: pointer;
  transition: all 0.2s ease;
}

.dropzone:hover,
.dropzone--active {
  border-color: var(--a-brand);
  background: rgba(142, 68, 243, 0.05);
}

.dropzone--uploading {
  pointer-events: none;
  opacity: 0.6;
}

.dropzone-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
}

.dropzone-icon {
  width: 28px;
  height: 28px;
  color: var(--a-brand);
  flex-shrink: 0;
}

.dropzone-text {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.dropzone-title {
  font-size: 13.5px;
  font-weight: 500;
  color: var(--a-text-primary);
}

.dropzone-hint {
  font-size: 12px;
  color: var(--a-text-subtle);
}

/* 画廊网格 */
.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.gallery-item {
  border: 1px solid var(--a-border);
  border-radius: 10px;
  background: var(--a-card-bg);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
}

.gallery-item:hover {
  transform: translateY(-2px);
  border-color: rgba(142, 68, 243, 0.4);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.25);
}

.thumb-wrap {
  position: relative;
  aspect-ratio: 16 / 10;
  background: #08080c;
  cursor: pointer;
  overflow: hidden;
}

.thumb-media {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.gallery-item:hover .thumb-media {
  transform: scale(1.03);
}

.thumb-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.thumb-wrap:hover .thumb-overlay {
  opacity: 1;
}

.thumb-action {
  font-size: 12px;
  color: #fff;
  padding: 4px 10px;
  background: rgba(142, 68, 243, 0.8);
  border-radius: 4px;
}

.thumb-badges {
  position: absolute;
  bottom: 6px;
  left: 6px;
  right: 6px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  pointer-events: none;
}

.badge {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.65);
  color: #f1f2f6;
  backdrop-filter: blur(4px);
}

.badge-video {
  background: rgba(224, 86, 36, 0.85);
  font-weight: 500;
}

.item-body {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.item-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--a-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
}

.item-time {
  font-size: 11.5px;
  color: var(--a-text-subtle);
}

.item-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
  border-top: 1px solid var(--a-border);
  padding-top: 8px;
  margin-top: auto;
}

/* 表格视图缩略图 */
.table-thumb {
  width: 80px;
  height: 50px;
  border-radius: 4px;
  overflow: hidden;
  background: #08080c;
  cursor: pointer;
}

.table-thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pagination-bar {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 预览弹窗 */
.preview-layout {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.preview-stage {
  max-height: 400px;
  background: #08080c;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 1px solid var(--a-border);
}

.stage-video,
.stage-image {
  max-width: 100%;
  max-height: 380px;
  object-fit: contain;
}

.preview-info {
  margin-top: 4px;
}

.url-copy-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.url-text {
  font-size: 12px;
  color: var(--a-text-muted);
  word-break: break-all;
}

.preview-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
