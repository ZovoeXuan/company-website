<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { listContents, type ContentView } from '@/api/content'

export interface LinkValue {
  type: string
  pageCode?: string
  contentType?: string
  contentId?: string
  url?: string
  newWindow?: boolean
  anchorCode?: string
}

const props = withDefaults(
  defineProps<{
    modelValue: LinkValue | null
    allowNone?: boolean
    allowConsult?: boolean
    placeholder?: string
  }>(),
  { allowNone: true, allowConsult: false, placeholder: '选择链接' },
)

const emit = defineEmits<{ 'update:modelValue': [value: LinkValue | null] }>()

const PAGES = [
  { value: 'HOME', label: '首页' },
  { value: 'PRODUCTS', label: '产品列表' },
  { value: 'SOLUTIONS', label: '解决方案列表' },
  { value: 'CASES', label: '客户案例列表' },
  { value: 'NEWS', label: '新闻列表' },
  { value: 'ABOUT', label: '关于我们' },
  { value: 'CONTACT', label: '联系我们' },
  { value: 'PRIVACY', label: '隐私说明' },
]

const ANCHORS = [
  { value: 'metrics', label: '企业指标' },
  { value: 'services', label: '产品与解决方案' },
  { value: 'cases', label: '客户案例' },
  { value: 'customers', label: '客户标识' },
  { value: 'news', label: '新闻动态' },
  { value: 'consultation', label: '咨询区' },
]

const CONTENT_TYPES = computed(() => [
  { value: 'PRODUCT', label: '产品' },
  { value: 'SOLUTION', label: '解决方案' },
  { value: 'CASE', label: '客户案例' },
  { value: 'NEWS', label: '新闻' },
])

const CONSULT_CONTENT_TYPES = [
  { value: '', label: '通用咨询（不限内容）' },
  { value: 'PRODUCT', label: '关联具体产品' },
  { value: 'SOLUTION', label: '关联解决方案' },
  { value: 'CASE', label: '关联客户案例' },
]

const typeOptions = computed(() => {
  const base = [
    { value: 'PAGE', label: '站内页面' },
    { value: 'CONTENT', label: '内容详情' },
    { value: 'EXTERNAL', label: '外部链接' },
    { value: 'ANCHOR', label: '首页锚点' },
  ]
  if (props.allowConsult) base.push({ value: 'CONSULT', label: '咨询动作' })
  if (props.allowNone) base.unshift({ value: 'NONE', label: '无链接' })
  return base
})

const type = computed({
  get: () => props.modelValue?.type ?? (props.allowNone ? 'NONE' : 'PAGE'),
  set: (value: string) => {
    if (value === 'NONE') {
      update({ type: 'NONE' })
    } else if (value === 'PAGE') {
      update({ type: 'PAGE', pageCode: props.modelValue?.pageCode ?? 'HOME' })
    } else if (value === 'CONTENT') {
      update({ type: 'CONTENT', contentType: CONTENT_TYPES.value[0].value, contentId: '' })
    } else if (value === 'EXTERNAL') {
      update({ type: 'EXTERNAL', url: props.modelValue?.url ?? '', newWindow: false })
    } else if (value === 'ANCHOR') {
      update({ type: 'ANCHOR', anchorCode: 'services' })
    } else if (value === 'CONSULT') {
      update({ type: 'CONSULT' })
    }
  },
})

const contents = ref<ContentView[]>([])
const loading = ref(false)

async function loadContents(contentType: string) {
  if (!contentType) {
    contents.value = []
    return
  }
  loading.value = true
  try {
    const data = await listContents({ contentType, publicationStatus: 'PUBLISHED', page: 1, pageSize: 50 })
    contents.value = data.items
  } catch {
    contents.value = []
  } finally {
    loading.value = false
  }
}

watch(
  () => [props.modelValue?.type, props.modelValue?.contentType],
  ([currentType, contentType]) => {
    if (currentType === 'CONTENT' || currentType === 'CONSULT') {
      if (contentType) loadContents(String(contentType))
      else contents.value = []
    }
  },
  { immediate: true },
)

function update(next: LinkValue | null) {
  emit('update:modelValue', next)
}

function patch(partial: Partial<LinkValue>) {
  update({ ...(props.modelValue ?? { type: 'NONE' }), ...partial } as LinkValue)
}
</script>

<template>
  <div class="link-editor">
    <el-select :model-value="type" style="width: 130px" @update:model-value="type = $event">
      <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
    </el-select>

    <template v-if="type === 'PAGE'">
      <el-select
        :model-value="modelValue?.pageCode"
        placeholder="选择页面"
        style="width: 180px"
        @update:model-value="(v: string) => patch({ pageCode: v })"
      >
        <el-option v-for="item in PAGES" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
    </template>

    <template v-else-if="type === 'CONTENT'">
      <el-select
        :model-value="modelValue?.contentType"
        style="width: 120px"
        @update:model-value="(v: string) => patch({ contentType: v, contentId: '' })"
      >
        <el-option v-for="item in CONTENT_TYPES" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select
        :model-value="modelValue?.contentId != null ? String(modelValue.contentId) : ''"
        :loading="loading"
        filterable
        placeholder="选择已发布内容"
        style="width: 220px"
        @update:model-value="(v: string) => patch({ contentId: v })"
      >
        <el-option v-for="item in contents" :key="item.id" :label="item.title" :value="String(item.id)" />
      </el-select>
    </template>

    <template v-else-if="type === 'EXTERNAL'">
      <el-input
        :model-value="modelValue?.url"
        placeholder="https://..."
        style="width: 240px"
        @update:model-value="(v: string) => patch({ url: v })"
      />
      <el-checkbox
        :model-value="Boolean(modelValue?.newWindow)"
        @update:model-value="(v: boolean) => patch({ newWindow: v })"
      >
        新窗口
      </el-checkbox>
    </template>

    <template v-else-if="type === 'ANCHOR'">
      <el-select
        :model-value="modelValue?.anchorCode"
        placeholder="选择锚点"
        style="width: 160px"
        @update:model-value="(v: string) => patch({ anchorCode: v })"
      >
        <el-option v-for="item in ANCHORS" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
    </template>

    <template v-else-if="type === 'CONSULT'">
      <el-select
        :model-value="modelValue?.contentType ?? ''"
        placeholder="关联咨询来源（可选）"
        style="width: 170px"
        @update:model-value="(v: string) => patch({ contentType: v || undefined, contentId: undefined })"
      >
        <el-option v-for="item in CONSULT_CONTENT_TYPES" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select
        v-if="modelValue?.contentType"
        :model-value="modelValue?.contentId != null ? String(modelValue.contentId) : ''"
        :loading="loading"
        filterable
        placeholder="选择关联的已发布内容"
        style="width: 210px"
        @update:model-value="(v: string) => patch({ contentId: v || undefined })"
      >
        <el-option v-for="item in contents" :key="item.id" :label="item.title" :value="String(item.id)" />
      </el-select>
    </template>
  </div>
</template>

<style scoped>
.link-editor {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
