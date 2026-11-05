<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ApiError, upload } from '@/api/http'
import { listMedia, type MediaItem } from '@/api/content'

const props = defineProps<{
  modelValue: boolean
  selectedId?: string | null
  kind?: 'IMAGE' | 'VIDEO' | 'ALL'
}>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  select: [media: MediaItem]
}>()

const loading = ref(false)
const items = ref<MediaItem[]>([])
const keyword = ref('')
const page = ref(1)
const pageSize = 24
const total = ref(0)
const subFilter = ref<'' | 'IMAGE' | 'VIDEO'>('')
const uploading = ref(false)
const dragOver = ref(false)
const fileInput = ref<HTMLInputElement>()

const filterType = computed<'' | 'IMAGE' | 'VIDEO' | undefined>(() => {
  if (props.kind === 'VIDEO') return 'VIDEO'
  if (props.kind === 'IMAGE') return 'IMAGE'
  return subFilter.value || undefined
})

const accept = computed(() => {
  if (props.kind === 'VIDEO') return 'video/mp4,video/webm'
  if (props.kind === 'IMAGE') return 'image/png,image/jpeg,image/webp'
  return 'image/png,image/jpeg,image/webp,video/mp4,video/webm'
})

const dialogTitle = computed(() =>
  props.kind === 'VIDEO' ? '选择视频' : props.kind === 'ALL' ? '选择素材（图片/视频）' : '选择图片',
)
const uploadLabel = computed(() =>
  props.kind === 'VIDEO' ? '上传新视频' : props.kind === 'ALL' ? '上传新素材' : '上传新图片',
)
const hint = computed(() =>
  props.kind === 'VIDEO'
    ? '支持 MP4、WebM 视频，单个不超过 50 MB。视频将静音循环播放。'
    : props.kind === 'ALL'
    ? '支持直接拖拽文件至此上传：JPG、PNG、WebP 图片（≤5 MB）与 MP4、WebM 视频（≤50 MB）。'
    : '支持直接拖拽图片至此上传：JPG、PNG、WebP，单张不超过 5 MB。缩略图为后台鉴权地址，公开地址在发布后生效。',
)

async function load() {
  loading.value = true
  try {
    const data = await listMedia({
      keyword: keyword.value.trim() || undefined,
      type: filterType.value,
      page: page.value,
      pageSize,
    })
    items.value = data.items
    total.value = data.total
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '加载素材失败')
  } finally {
    loading.value = false
  }
}

watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      keyword.value = ''
      page.value = 1
      subFilter.value = ''
      load()
    }
  },
)

async function uploadSingleFile(file: File) {
  uploading.value = true
  try {
    const form = new FormData()
    form.append('file', file)
    const media = await upload<MediaItem>('/api/admin/media', form)
    ElMessage.success('上传成功')
    page.value = 1
    await load()
    emit('select', media)
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '上传失败')
  } finally {
    uploading.value = false
  }
}

async function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  await uploadSingleFile(file)
  input.value = ''
}

async function onDrop(event: DragEvent) {
  dragOver.value = false
  const file = event.dataTransfer?.files?.[0]
  if (!file) return
  await uploadSingleFile(file)
}

function choose(media: MediaItem) {
  emit('select', media)
}

function close() {
  emit('update:modelValue', false)
}

function thumbUrl(media: MediaItem) {
  return `/api/admin/media/${media.id}/file`
}

function formatSize(bytes: number) {
  if (!bytes) return ''
  if (bytes < 1024 * 1024) return `${Math.round(bytes / 1024)}KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)}MB`
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    :title="dialogTitle"
    width="860px"
    top="5vh"
    @update:model-value="(v: boolean) => emit('update:modelValue', v)"
  >
    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="按原始文件名搜索..."
        clearable
        style="width: 240px"
        @keyup.enter="page = 1; load()"
      />
      <el-select
        v-if="kind === 'ALL'"
        v-model="subFilter"
        style="width: 120px"
        placeholder="全部类型"
        @change="page = 1; load()"
      >
        <el-option label="全部类型" value="" />
        <el-option label="仅图片" value="IMAGE" />
        <el-option label="仅视频" value="VIDEO" />
      </el-select>
      <el-button @click="page = 1; load()">查询</el-button>
      <div class="spacer"></div>
      <input ref="fileInput" type="file" :accept="accept" hidden @change="onFileChange" />
      <el-button type="primary" :loading="uploading" @click="fileInput?.click()">
        + {{ uploadLabel }}
      </el-button>
    </div>

    <div
      class="dropzone"
      :class="{ 'is-dragover': dragOver }"
      @dragover.prevent="dragOver = true"
      @dragleave.prevent="dragOver = false"
      @drop.prevent="onDrop"
    >
      <span>{{ hint }}</span>
    </div>

    <div v-loading="loading" class="grid">
      <button
        v-for="media in items"
        :key="media.id"
        type="button"
        class="item"
        :class="{ 'is-selected': String(media.id) === String(selectedId) }"
        :title="`${media.originalName} (${formatSize(media.byteSize)})`"
        @click="choose(media)"
      >
        <video
          v-if="media.mediaType === 'VIDEO'"
          :src="thumbUrl(media)"
          class="item__media"
          muted
          playsinline
          preload="metadata"
        ></video>
        <img v-else :src="thumbUrl(media)" :alt="media.originalName" class="item__media" loading="lazy" />
        <span class="item__name">{{ media.originalName }}</span>
        <span class="item__meta">
          {{ media.mediaType === 'VIDEO' ? '▶ 视频' : `${media.width}×${media.height}` }}
        </span>
      </button>
      <el-empty v-if="!loading && items.length === 0" description="还没有匹配的素材，可点击右上角或拖拽文件上传" />
    </div>

    <div class="pager">
      <span class="pager__total">共 {{ total }} 个素材</span>
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="load"
      />
    </div>

    <template #footer>
      <el-button @click="close">取消</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.spacer {
  flex: 1;
}

.dropzone {
  margin: 12px 0 14px;
  padding: 10px 14px;
  border-radius: 8px;
  border: 1px dashed var(--a-border);
  background: #fafbfe;
  font-size: 12px;
  color: var(--a-text-muted);
  transition: all 0.2s ease;
}

.dropzone.is-dragover {
  border-color: var(--el-color-primary);
  background: #eef5fd;
  color: var(--el-color-primary);
}

.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  max-height: 420px;
  overflow-y: auto;
  min-height: 160px;
}

.item {
  position: relative;
  border: 2px solid var(--a-border);
  border-radius: 8px;
  padding: 0;
  background: #0f172a;
  cursor: pointer;
  overflow: hidden;
  aspect-ratio: 16 / 10;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item:hover {
  border-color: var(--el-color-primary);
}

.item.is-selected {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 0 2px var(--el-color-primary-light-8);
}

.item__media {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.item__name {
  position: absolute;
  left: 6px;
  top: 6px;
  max-width: 75%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
}

.item__meta {
  position: absolute;
  right: 6px;
  bottom: 6px;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.65);
  color: #fff;
}

.pager {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pager__total {
  font-size: 12px;
  color: var(--a-text-muted);
}
</style>
