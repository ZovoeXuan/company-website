<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { listContents, type ContentView } from '@/api/content'

const props = withDefaults(
  defineProps<{
    modelValue: number[]
    contentType: string
    max?: number
    /** 已选内容标题缓存，父组件可传入以在未打开选择器时展示 */
    labels?: Record<string, string>
  }>(),
  { max: 6 },
)

const emit = defineEmits<{ 'update:modelValue': [value: number[]] }>()

const open = ref(false)
const loading = ref(false)
const keyword = ref('')
const page = ref(1)
const pageSize = 20
const total = ref(0)
const items = ref<ContentView[]>([])
const selected = ref<number[]>([])
const titleMap = ref<Record<string, string>>({ ...(props.labels ?? {}) })

async function load() {
  loading.value = true
  try {
    const data = await listContents({
      contentType: props.contentType,
      publicationStatus: 'PUBLISHED',
      keyword: keyword.value.trim() || undefined,
      page: page.value,
      pageSize,
    })
    items.value = data.items
    total.value = data.total
    for (const item of data.items) {
      titleMap.value[item.id] = item.title
    }
  } catch (error) {
    ElMessage.error((error as { message?: string }).message ?? '内容加载失败')
  } finally {
    loading.value = false
  }
}

watch(open, (value) => {
  if (value) {
    selected.value = [...props.modelValue]
    page.value = 1
    keyword.value = ''
    load()
  }
})

/** 已选内容的标题缓存：未打开选择器时也要显示具体标题而不是 #id */
async function ensureTitles() {
  const missing = props.modelValue.filter((id) => !titleMap.value[String(id)])
  if (missing.length === 0) return
  try {
    const data = await listContents({ contentType: props.contentType, page: 1, pageSize: 50 })
    for (const item of data.items) {
      titleMap.value[item.id] = item.title
    }
  } catch {
    // 标题拉取失败时保留 #id 展示，不影响保存
  }
}

watch(() => props.modelValue, ensureTitles, { immediate: true })
onMounted(ensureTitles)

function toggle(id: number) {
  const index = selected.value.indexOf(id)
  if (index >= 0) {
    selected.value.splice(index, 1)
    return
  }
  if (selected.value.length >= props.max) {
    ElMessage.warning(`最多选择 ${props.max} 项`)
    return
  }
  selected.value.push(id)
}

function confirm() {
  emit('update:modelValue', [...selected.value])
  open.value = false
}

function remove(index: number) {
  const next = [...props.modelValue]
  next.splice(index, 1)
  emit('update:modelValue', next)
}

function move(index: number, delta: number) {
  const target = index + delta
  if (target < 0 || target >= props.modelValue.length) return
  const next = [...props.modelValue]
  const [item] = next.splice(index, 1)
  next.splice(target, 0, item)
  emit('update:modelValue', next)
}

function labelOf(id: number) {
  return titleMap.value[String(id)] ?? `#${id}`
}
</script>

<template>
  <div class="picker">
    <ul v-if="modelValue.length" class="chosen">
      <li v-for="(id, index) in modelValue" :key="id" class="chosen__item">
        <span class="chosen__title">{{ labelOf(id) }}</span>
        <span class="chosen__actions">
          <el-button link :disabled="index === 0" @click="move(index, -1)">上移</el-button>
          <el-button link :disabled="index === modelValue.length - 1" @click="move(index, 1)">下移</el-button>
          <el-button link type="danger" @click="remove(index)">移除</el-button>
        </span>
      </li>
    </ul>
    <p v-else class="empty">尚未选择，前台该区块会隐藏</p>
    <el-button size="small" @click="open = true">选择已发布内容</el-button>

    <el-dialog v-model="open" title="选择内容" width="760px" top="8vh">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="按标题搜索" clearable style="width: 240px" @keyup.enter="load" />
        <el-button @click="page = 1; load()">查询</el-button>
        <span class="count">已选 {{ selected.length }} / {{ max }}</span>
      </div>

      <div v-loading="loading" class="list">
        <label v-for="item in items" :key="item.id" class="row" :class="{ 'is-checked': selected.includes(Number(item.id)) }">
          <el-checkbox
            :model-value="selected.includes(Number(item.id))"
            @update:model-value="toggle(Number(item.id))"
          />
          <span class="row__title">{{ item.title || `#${item.id}` }}</span>
          <span class="row__meta">v{{ item.publishedVersion ?? '—' }}</span>
        </label>
        <el-empty v-if="!loading && items.length === 0" description="没有已发布内容" />
      </div>

      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="load"
        />
      </div>

      <template #footer>
        <el-button @click="open = false">取消</el-button>
        <el-button type="primary" @click="confirm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.chosen {
  list-style: none;
  margin: 0 0 10px;
  padding: 0;
  border: 1px solid var(--a-border);
  border-radius: 6px;
  overflow: hidden;
}

.chosen__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 12px;
  font-size: 13px;
  background: #fff;
}

.chosen__item + .chosen__item {
  border-top: 1px solid var(--a-border);
}

.chosen__title {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty {
  margin: 0 0 10px;
  font-size: 12px;
  color: var(--a-text-muted);
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.count {
  margin-left: auto;
  font-size: 12px;
  color: var(--a-text-muted);
}

.list {
  max-height: 400px;
  overflow-y: auto;
  min-height: 120px;
}

.row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 10px;
  border-radius: 6px;
  cursor: pointer;
}

.row:hover {
  background: #f4f7fb;
}

.row.is-checked {
  background: #eef5fd;
}

.row__title {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.row__meta {
  font-size: 12px;
  color: var(--a-text-muted);
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
