<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ApiError, upload } from '@/api/http'
import { listMedia, type MediaItem } from '@/api/content'

const props = defineProps<{ modelValue: boolean; selectedId?: string | null }>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  select: [media: MediaItem]
}>()

const loading = ref(false)
const items = ref<MediaItem[]>([])
const keyword = ref('')
const uploading = ref(false)
const fileInput = ref<HTMLInputElement>()

async function load() {
  loading.value = true
  try {
    const data = await listMedia({ keyword: keyword.value, page: 1, pageSize: 40 })
    items.value = data.items
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '素材加载失败')
  } finally {
    loading.value = false
  }
}

watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      keyword.value = ''
      load()
    }
  },
)

async function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const form = new FormData()
    form.append('file', file)
    const media = await upload<MediaItem>('/api/admin/media', form)
    ElMessage.success('上传成功')
    await load()
    emit('select', media)
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '上传失败')
  } finally {
    uploading.value = false
    input.value = ''
  }
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
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    title="选择图片"
    width="820px"
    top="6vh"
    @update:model-value="(v: boolean) => emit('update:modelValue', v)"
  >
    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="按文件名搜索"
        clearable
        style="width: 240px"
        @keyup.enter="load"
      />
      <el-button @click="load">查询</el-button>
      <div class="spacer"></div>
      <input ref="fileInput" type="file" accept="image/png,image/jpeg,image/webp" hidden @change="onFileChange" />
      <el-button type="primary" :loading="uploading" @click="fileInput?.click()">上传新图片</el-button>
    </div>

    <p class="hint">支持 JPG、PNG、WebP，单张不超过 5 MB。缩略图为后台鉴权地址，前台公开地址在发布后生效。</p>

    <div v-loading="loading" class="grid">
      <button
        v-for="media in items"
        :key="media.id"
        type="button"
        class="item"
        :class="{ 'is-selected': media.id === selectedId }"
        @click="choose(media)"
      >
        <img :src="thumbUrl(media)" :alt="media.originalName" loading="lazy" />
        <span class="item__meta">{{ media.width }}×{{ media.height }}</span>
      </button>
      <el-empty v-if="!loading && items.length === 0" description="还没有素材，先上传一张" />
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
  gap: 8px;
}

.spacer {
  flex: 1;
}

.hint {
  margin: 12px 0 16px;
  font-size: 12px;
  color: var(--a-text-muted);
  line-height: 1.7;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 12px;
  max-height: 420px;
  overflow-y: auto;
  min-height: 160px;
}

.item {
  position: relative;
  padding: 0;
  border: 2px solid var(--a-border);
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  overflow: hidden;
}

.item img {
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
  display: block;
}

.item.is-selected {
  border-color: #185fa5;
}

.item__meta {
  display: block;
  padding: 6px 8px;
  font-size: 12px;
  color: var(--a-text-muted);
  text-align: left;
}
</style>
