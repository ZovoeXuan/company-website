<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ApiError } from '@/api/http'
import {
  createContent,
  getContent,
  listCategories,
  publishContent,
  saveDraft,
  type CategoryView,
  type ContentDetail,
} from '@/api/content'
import MediaPicker from '@/components/MediaPicker.vue'

const route = useRoute()
const router = useRouter()

const contentType = computed(() => String(route.meta.contentType ?? 'PRODUCT'))
const slug = computed(() => String(route.meta.slug ?? 'products'))
const isNew = computed(() => !route.params.id)
const contentId = computed(() => (route.params.id ? String(route.params.id) : ''))

const loading = ref(false)
const saving = ref(false)
const publishing = ref(false)
const detail = ref<ContentDetail | null>(null)
const categories = ref<CategoryView[]>([])
const pickerOpen = ref(false)

const form = reactive({
  title: '',
  summary: '',
  bodyHtml: '',
  categoryId: '' as string,
  sortOrder: 9999,
  displayTime: '',
  capabilities: '',
  industry: '',
  coverId: '' as string,
  coverName: '',
})

const advancedJson = ref('{}')
const advancedError = ref('')

/** 类型相关但结构复杂的字段（results/actions/outcome 等）走高级 JSON，避免做四套表单。 */
const CORE_KEYS = [
  'title', 'summary', 'bodyHtml', 'categoryId', 'sortOrder', 'displayTime',
  'capabilities', 'industry', 'coverMediaId', 'coverAlt',
]

const isNews = computed(() => contentType.value === 'NEWS')
const hasCategory = computed(() => contentType.value !== 'CUSTOMER')
const hasCapabilities = computed(() => ['PRODUCT', 'SOLUTION'].includes(contentType.value))
const hasIndustry = computed(() => ['SOLUTION', 'CASE'].includes(contentType.value))

function formatTime(value: unknown) {
  return typeof value === 'string' ? value : ''
}

function fill(detailValue: ContentDetail) {
  const draft = detailValue.draft ?? {}
  form.title = String(draft.title ?? '')
  form.summary = String(draft.summary ?? '')
  form.bodyHtml = String(draft.bodyHtml ?? '')
  form.categoryId = draft.categoryId ? String(draft.categoryId) : ''
  form.sortOrder = typeof draft.sortOrder === 'number' ? draft.sortOrder : 9999
  form.displayTime = formatTime(draft.displayTime)
  form.capabilities = Array.isArray(draft.capabilities) ? (draft.capabilities as string[]).join('，') : ''
  form.industry = String(draft.industry ?? '')
  form.coverId = draft.coverMediaId ? String(draft.coverMediaId) : ''
  form.coverName = draft.coverMediaId ? `图片 #${draft.coverMediaId}` : ''

  const rest: Record<string, unknown> = {}
  for (const [key, value] of Object.entries(draft)) {
    if (!CORE_KEYS.includes(key)) rest[key] = value
  }
  advancedJson.value = Object.keys(rest).length > 0 ? JSON.stringify(rest, null, 2) : '{}'
  advancedError.value = ''
}

async function load() {
  loading.value = true
  try {
    categories.value = (await listCategories({ categoryType: contentType.value, page: 1, pageSize: 50 })).items
    if (!isNew.value) {
      const data = await getContent(contentId.value)
      detail.value = data
      fill(data)
    }
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '内容加载失败')
  } finally {
    loading.value = false
  }
}

function buildDraft(): Record<string, unknown> | null {
  let extra: Record<string, unknown> = {}
  if (advancedJson.value.trim()) {
    try {
      extra = JSON.parse(advancedJson.value)
    } catch {
      advancedError.value = '高级字段不是合法 JSON'
      return null
    }
  }
  advancedError.value = ''

  const draft: Record<string, unknown> = { ...extra, title: form.title.trim(), summary: form.summary.trim() }
  if (form.bodyHtml.trim()) draft.bodyHtml = form.bodyHtml
  if (hasCategory.value && form.categoryId) draft.categoryId = Number(form.categoryId)
  draft.sortOrder = form.sortOrder
  if (isNews.value && form.displayTime) draft.displayTime = form.displayTime
  if (hasCapabilities.value) {
    const list = form.capabilities.split(/[，,]/).map((item) => item.trim()).filter(Boolean)
    if (list.length > 0) draft.capabilities = list
    else delete draft.capabilities
  }
  if (hasIndustry.value && form.industry.trim()) draft.industry = form.industry.trim()
  if (form.coverId) {
    draft.coverMediaId = Number(form.coverId)
    draft.coverAlt = form.title.trim()
  } else {
    delete draft.coverMediaId
    delete draft.coverAlt
  }
  return draft
}

function reportError(error: unknown) {
  if (error instanceof ApiError) {
    const fields = error.fieldErrors?.map((item) => `${item.field}：${item.message}`).join('；')
    ElMessage.error(fields ? `${error.message}（${fields}）` : error.message)
  } else {
    ElMessage.error('操作失败，请重试')
  }
}

async function save(): Promise<ContentDetail | null> {
  if (!form.title.trim()) {
    ElMessage.warning('请填写标题')
    return null
  }
  const draft = buildDraft()
  if (!draft) return null

  saving.value = true
  try {
    const saved = isNew.value
      ? await createContent(contentType.value, draft)
      : await saveDraft(contentId.value, draft, detail.value!.content.lockVersion)

    detail.value = saved
    fill(saved)
    if (isNew.value) {
      ElMessage.success('草稿已创建')
      await router.replace(`/contents/${slug.value}/${saved.content.id}/edit`)
    } else {
      ElMessage.success('草稿已保存')
    }
    return saved
  } catch (error) {
    reportError(error)
    return null
  } finally {
    saving.value = false
  }
}

async function saveAndPublish() {
  const saved = await save()
  if (!saved) return

  try {
    await ElMessageBox.confirm('发布后官网立即生效，确认发布？', '确认发布', {
      confirmButtonText: '发布',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }

  publishing.value = true
  try {
    await publishContent(saved.content.id, saved.content.lockVersion, saved.content.draftVersion)
    ElMessage.success('已发布')
    const refreshed = await getContent(saved.content.id)
    detail.value = refreshed
    fill(refreshed)
  } catch (error) {
    reportError(error)
  } finally {
    publishing.value = false
  }
}

function onSelectCover(media: { id: string; originalName: string }) {
  form.coverId = media.id
  form.coverName = media.originalName
  pickerOpen.value = false
}

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="editor">
    <el-card shadow="never" class="main">
      <template #header>
        <div class="head">
          <span>{{ isNew ? '新建' : '编辑' }}{{ route.meta.title }}</span>
          <el-button @click="router.push(`/contents/${slug}`)">返回列表</el-button>
        </div>
      </template>

      <el-form label-width="96px" label-position="right">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" maxlength="120" show-word-limit placeholder="建议 20 字以内，写清对象与价值" />
        </el-form-item>

        <el-form-item label="摘要">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="2"
            maxlength="300"
            show-word-limit
            placeholder="用于列表页与分享描述，60~80 字为宜"
          />
        </el-form-item>

        <el-form-item v-if="hasCategory" label="分类">
          <el-select v-model="form.categoryId" placeholder="请选择分类" clearable style="width: 260px">
            <el-option v-for="item in categories" :key="item.id" :label="`${item.name}（${item.status === 'PUBLISHED' ? '已发布' : '未发布'}）`" :value="item.id" />
          </el-select>
          <RouterLink to="/contents/categories" class="tip-link">管理分类</RouterLink>
        </el-form-item>

        <el-form-item v-if="isNews" label="展示日期">
          <el-date-picker v-model="form.displayTime" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>

        <el-form-item label="封面">
          <div class="cover">
            <img v-if="form.coverId" :src="`/api/admin/media/${form.coverId}/file`" alt="封面预览" class="cover__img" />
            <div v-else class="cover__empty">未选择</div>
            <div class="cover__actions">
              <el-button size="small" @click="pickerOpen = true">选择图片</el-button>
              <el-button v-if="form.coverId" size="small" link type="danger" @click="form.coverId = ''; form.coverName = ''">
                清除
              </el-button>
              <p class="cover__name">{{ form.coverName || '建议 4:3 或 16:9，宽度 1200px 以上' }}</p>
            </div>
          </div>
        </el-form-item>

        <el-form-item v-if="hasCapabilities" label="能力标签">
          <el-input v-model="form.capabilities" placeholder="用逗号分隔，例如：多协议接入，台账与巡检" />
        </el-form-item>

        <el-form-item v-if="hasIndustry" label="所属行业">
          <el-input v-model="form.industry" placeholder="例如：汽车零部件" />
        </el-form-item>

        <el-form-item label="正文">
          <el-input
            v-model="form.bodyHtml"
            type="textarea"
            :rows="12"
            placeholder="支持基础 HTML，例如 <h2>解决什么问题</h2><p>…</p>"
          />
          <p class="field-hint">当前为 HTML 源码输入，所见即所得编辑器在后续版本接入。</p>
        </el-form-item>

        <el-form-item label="高级字段">
          <el-input v-model="advancedJson" type="textarea" :rows="6" spellcheck="false" />
          <p class="field-hint">
            结构化字段（results、actions、outcome、quote 等）以 JSON 维护，保存时与其他字段合并。
          </p>
          <p v-if="advancedError" class="field-error">{{ advancedError }}</p>
        </el-form-item>

        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
          <span class="field-hint inline">数值越小越靠前</span>
        </el-form-item>
      </el-form>
    </el-card>

    <aside class="side">
      <el-card shadow="never">
        <template #header>状态</template>
        <div v-if="detail" class="status">
          <p class="row"><span>发布状态</span><el-tag size="small">{{ detail.content.status }}</el-tag></p>
          <p class="row"><span>草稿版本</span><span>v{{ detail.content.draftVersion }}</span></p>
          <p class="row"><span>线上版本</span><span>v{{ detail.content.publishedVersion ?? '—' }}</span></p>
          <p v-if="detail.content.hasUnpublishedChanges" class="warn">有未发布的修改</p>
        </div>
        <p v-else class="muted">保存后显示版本信息</p>

        <div class="actions">
          <el-button type="primary" :loading="saving" @click="save">保存草稿</el-button>
          <el-button type="success" :loading="publishing" @click="saveAndPublish">保存并发布</el-button>
        </div>
      </el-card>
    </aside>

    <MediaPicker
      v-model="pickerOpen"
      :selected-id="form.coverId"
      @select="onSelectCover"
    />
  </div>
</template>

<style scoped>
.editor {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 16px;
  align-items: start;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.cover {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.cover__img,
.cover__empty {
  width: 160px;
  height: 110px;
  border-radius: 8px;
  border: 1px solid var(--a-border);
  object-fit: cover;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--a-text-muted);
  font-size: 12px;
  background: #f6f8fa;
}

.cover__actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.cover__name {
  font-size: 12px;
  color: var(--a-text-muted);
}

.tip-link {
  margin-left: 12px;
  font-size: 13px;
  color: #185fa5;
}

.field-hint {
  margin-top: 4px;
  font-size: 12px;
  color: var(--a-text-muted);
  line-height: 1.6;
}

.field-hint.inline {
  margin-left: 10px;
}

.field-error {
  margin-top: 4px;
  font-size: 12px;
  color: #c0392b;
}

.status .row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  padding: 6px 0;
  color: var(--a-text-muted);
}

.warn {
  margin-top: 8px;
  font-size: 12px;
  color: #b26a00;
}

.muted {
  font-size: 13px;
  color: var(--a-text-muted);
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 16px;
}

@media (max-width: 1100px) {
  .editor {
    grid-template-columns: 1fr;
  }
}
</style>
