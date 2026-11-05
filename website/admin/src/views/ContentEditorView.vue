<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import ArticleBody from '@shared/ArticleBody.vue'
import ContentPicker from '@/components/ContentPicker.vue'
import MediaPicker from '@/components/MediaPicker.vue'
import { ApiError } from '@/api/http'
import {
  createContent,
  getContent,
  listCategories,
  offlineContent,
  publishContent,
  saveDraft,
  type CategoryView,
  type ContentView,
  type MediaItem,
} from '@/api/content'
import { useAuth } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const { current } = useAuth()
const isAdmin = computed(() => current.value?.role === 'ADMIN')

const contentType = computed(() => String(route.meta.contentType || 'PRODUCT'))
const slug = computed(() => String(route.meta.slug || 'products'))
const id = computed(() => (typeof route.params.id === 'string' ? route.params.id : ''))
const isEdit = computed(() => Boolean(id.value))

const loading = ref(false)
const saving = ref(false)
const publishing = ref(false)
const meta = ref<ContentView | null>(null)
const rawDraftBackup = ref<Record<string, unknown>>({})
const categories = ref<CategoryView[]>([])

// 编辑器模式：纯源码 / 左右分屏实时预览
const editorMode = ref<'split' | 'code'>('split')
const bodyTextareaRef = ref<HTMLTextAreaElement | null>(null)

// 素材选择器
const pickerOpen = ref(false)
const pickerKind = ref<'IMAGE' | 'VIDEO' | 'ALL'>('IMAGE')
let pickerCallback: ((media: MediaItem) => void) | null = null

const form = ref({
  title: '',
  summary: '',
  coverMediaId: null as number | null,
  coverAlt: '',
  heroMediaId: null as number | null,
  logoMediaId: null as number | null,
  logoAlt: '',
  websiteUrl: '',
  categoryId: '',
  sortOrder: 100,
  industry: '',
  customerName: '',
  quoteText: '',
  quoteAuthor: '',
  author: '',
  displayTime: new Date().toISOString().slice(0, 16).replace('T', ' '),
  sourceName: '',
  sourceUrl: '',
  bodyHtml: '',
  seoTitle: '',
  seoDescription: '',
  features: [] as Array<{ key?: string; title: string; description: string; iconMediaId?: number | null }>,
  pains: [] as Array<{ key?: string; title: string; description: string }>,
  benefits: [] as Array<{ key?: string; title: string; description: string }>,
  outcomes: [] as Array<{ key?: string; displayValue: string; label: string }>,
  relatedCaseIds: [] as number[],
  relatedProductIds: [] as number[],
  relatedSolutionIds: [] as number[],
})

/** 将正文中的 data-media-id="123" 转换为后台鉴权图片预览地址，供右侧实时预览使用 */
const previewHtml = computed(() => {
  if (!form.value.bodyHtml) return ''
  return form.value.bodyHtml.replace(
    /<img([^>]*?)data-media-id=["'](\d+)["']([^>]*?)>/gi,
    '<img$1data-media-id="$2" src="/api/admin/media/$2/file"$3>',
  )
})

function openMedia(kind: 'IMAGE' | 'VIDEO' | 'ALL', cb: (m: MediaItem) => void) {
  pickerKind.value = kind
  pickerCallback = cb
  pickerOpen.value = true
}

function onMediaPicked(media: MediaItem) {
  if (pickerCallback) {
    pickerCallback(media)
    pickerCallback = null
  }
  pickerOpen.value = false
}

function insertHtmlSnippet(snippet: string) {
  const el = bodyTextareaRef.value
  if (!el) {
    form.value.bodyHtml = (form.value.bodyHtml ? form.value.bodyHtml + '\n' : '') + snippet
    return
  }
  const start = el.selectionStart ?? form.value.bodyHtml.length
  const end = el.selectionEnd ?? form.value.bodyHtml.length
  const selected = form.value.bodyHtml.slice(start, end)
  const finalSnippet = selected ? snippet.replace('在此输入内容', selected) : snippet
  form.value.bodyHtml = form.value.bodyHtml.slice(0, start) + finalSnippet + form.value.bodyHtml.slice(end)
}

function insertBodyImage() {
  openMedia('IMAGE', (media) => {
    const tag = `<p><img data-media-id="${media.id}" alt="${media.originalName.replace(/"/g, '')}" /></p>`
    insertHtmlSnippet(tag)
    ElMessage.success(`已插入受控素材图片 #${media.id}`)
  })
}

async function loadCategories() {
  if (contentType.value === 'CUSTOMER') return
  try {
    const res = await listCategories(contentType.value)
    categories.value = res.items
  } catch {
    categories.value = []
  }
}

function fillFromDraft(draft: Record<string, any>) {
  rawDraftBackup.value = { ...draft }
  form.value.title = draft.title ?? ''
  form.value.summary = draft.summary ?? ''
  form.value.coverMediaId = draft.coverMediaId != null ? Number(draft.coverMediaId) : null
  form.value.coverAlt = draft.coverAlt ?? ''
  form.value.heroMediaId = draft.heroMediaId != null ? Number(draft.heroMediaId) : null
  form.value.logoMediaId = draft.logoMediaId != null ? Number(draft.logoMediaId) : null
  form.value.logoAlt = draft.logoAlt ?? ''
  form.value.websiteUrl = draft.websiteUrl ?? ''
  form.value.categoryId = draft.categoryId != null ? String(draft.categoryId) : ''
  form.value.sortOrder = draft.sortOrder ?? 100
  form.value.industry = draft.industry ?? ''
  form.value.customerName = draft.customerName ?? ''
  form.value.quoteText = draft.quoteText ?? ''
  form.value.quoteAuthor = draft.quoteAuthor ?? ''
  form.value.author = draft.author ?? ''
  form.value.displayTime = draft.displayTime ?? new Date().toISOString().slice(0, 16).replace('T', ' ')
  form.value.sourceName = draft.sourceName ?? ''
  form.value.sourceUrl = draft.sourceUrl ?? ''
  form.value.bodyHtml = draft.bodyHtml ?? ''
  form.value.seoTitle = draft.seo?.title ?? ''
  form.value.seoDescription = draft.seo?.description ?? ''
  form.value.features = Array.isArray(draft.features) ? draft.features.map((f: any) => ({ ...f })) : []
  form.value.pains = Array.isArray(draft.pains) ? draft.pains.map((p: any) => ({ ...p })) : []
  form.value.benefits = Array.isArray(draft.benefits) ? draft.benefits.map((b: any) => ({ ...b })) : []
  form.value.outcomes = Array.isArray(draft.outcomes) ? draft.outcomes.map((o: any) => ({ ...o })) : []
  form.value.relatedCaseIds = Array.isArray(draft.relatedCaseIds) ? draft.relatedCaseIds.map(Number) : []
  form.value.relatedProductIds = Array.isArray(draft.relatedProductIds) ? draft.relatedProductIds.map(Number) : []
  form.value.relatedSolutionIds = Array.isArray(draft.relatedSolutionIds) ? draft.relatedSolutionIds.map(Number) : []
}

function buildDraftPayload(): Record<string, unknown> {
  if (contentType.value === 'CUSTOMER') {
    return {
      ...rawDraftBackup.value,
      title: form.value.title.trim(),
      logoMediaId: form.value.logoMediaId,
      logoAlt: form.value.logoAlt.trim() || form.value.title.trim(),
      websiteUrl: form.value.websiteUrl.trim(),
      sortOrder: form.value.sortOrder,
    }
  }

  const base: Record<string, unknown> = {
    ...rawDraftBackup.value,
    title: form.value.title.trim(),
    summary: form.value.summary.trim(),
    coverMediaId: form.value.coverMediaId,
    coverAlt: form.value.coverAlt.trim() || form.value.title.trim(),
    categoryId: form.value.categoryId ? Number(form.value.categoryId) : null,
    sortOrder: form.value.sortOrder,
    bodyHtml: form.value.bodyHtml,
    seo: {
      title: form.value.seoTitle.trim(),
      description: form.value.seoDescription.trim(),
    },
  }

  if (contentType.value === 'PRODUCT') {
    base.heroMediaId = form.value.heroMediaId
    base.features = form.value.features
    base.relatedCaseIds = form.value.relatedCaseIds
  } else if (contentType.value === 'SOLUTION') {
    base.industry = form.value.industry.trim()
    base.heroMediaId = form.value.heroMediaId
    base.pains = form.value.pains
    base.benefits = form.value.benefits
    base.relatedProductIds = form.value.relatedProductIds
  } else if (contentType.value === 'CASE') {
    base.customerName = form.value.customerName.trim()
    base.industry = form.value.industry.trim()
    base.outcomes = form.value.outcomes
    base.quoteText = form.value.quoteText.trim()
    base.quoteAuthor = form.value.quoteAuthor.trim()
    base.relatedProductIds = form.value.relatedProductIds
    base.relatedSolutionIds = form.value.relatedSolutionIds
  } else if (contentType.value === 'NEWS') {
    base.author = form.value.author.trim()
    base.displayTime = form.value.displayTime.trim()
    base.sourceName = form.value.sourceName.trim()
    base.sourceUrl = form.value.sourceUrl.trim()
  }

  return base
}

async function load() {
  if (!isEdit.value) return
  loading.value = true
  try {
    const detail = await getContent(id.value)
    meta.value = detail.content
    fillFromDraft(detail.draft)
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '内容加载失败')
  } finally {
    loading.value = false
  }
}

async function onSaveDraft() {
  if (!form.value.title.trim()) {
    ElMessage.warning('请先填写标题')
    return false
  }
  saving.value = true
  try {
    const payload = buildDraftPayload()
    if (isEdit.value && meta.value) {
      const updated = await saveDraft(id.value, payload, meta.value.lockVersion)
      meta.value = updated.content
      fillFromDraft(updated.draft)
      ElMessage.success(`草稿已保存 (v${updated.content.draftVersion})`)
    } else {
      const created = await createContent(contentType.value, payload)
      meta.value = created.content
      ElMessage.success('草稿已创建')
      await router.replace(`/contents/${slug.value}/${created.content.id}/edit`)
    }
    return true
  } catch (error) {
    if (error instanceof ApiError) {
      const fields = error.fieldErrors?.map((f) => `${f.field}: ${f.message}`).join('; ')
      ElMessage.error(fields ? `${error.message} (${fields})` : error.message)
    } else {
      ElMessage.error('保存草稿失败')
    }
    return false
  } finally {
    saving.value = false
  }
}

async function onPublish() {
  const saved = await onSaveDraft()
  if (!saved || !meta.value) return
  try {
    await ElMessageBox.confirm('发布后前台立即更新为当前版本，确认发布？', '确认发布', {
      confirmButtonText: '立即发布',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  publishing.value = true
  try {
    await publishContent(meta.value.id, meta.value.lockVersion, meta.value.draftVersion)
    ElMessage.success('已正式发布上线')
    await load()
  } catch (error) {
    if (error instanceof ApiError) {
      const fields = error.fieldErrors?.map((f) => `${f.field}: ${f.message}`).join('; ')
      ElMessage.error(fields ? `${error.message} (${fields})` : error.message)
    } else {
      ElMessage.error('发布失败')
    }
  } finally {
    publishing.value = false
  }
}

async function onOffline() {
  if (!meta.value) return
  try {
    await ElMessageBox.confirm('确认下线该内容？', '确认下线', { type: 'warning' })
    await offlineContent(meta.value.id, meta.value.lockVersion, meta.value.draftVersion)
    ElMessage.success('已下线')
    await load()
  } catch (error) {
    if (error instanceof ApiError) ElMessage.error(error.message)
  }
}

function goBack() {
  router.push(`/contents/${slug.value}`)
}

function goPreview() {
  if (!meta.value) return
  router.push(`/preview/${slug.value}/${meta.value.id}`)
}

onMounted(() => {
  loadCategories()
  load()
})
</script>

<template>
  <div v-loading="loading" class="editor-page">
    <!-- 顶部粘性状态与动作栏 -->
    <div class="top-bar">
      <div class="top-bar__left">
        <el-button link @click="goBack">← 返回列表</el-button>
        <strong class="top-bar__title">{{ isEdit ? `编辑 #${id}` : '新建内容' }}</strong>
        <template v-if="meta">
          <el-tag size="small" :type="meta.status === 'PUBLISHED' ? 'success' : 'info'">
            {{ meta.status === 'PUBLISHED' ? `线上 v${meta.publishedVersion}` : meta.status === 'OFFLINE' ? '已下线' : '草稿未发布' }}
          </el-tag>
          <el-tag size="small" type="info">草稿 v{{ meta.draftVersion }}</el-tag>
          <el-tag v-if="meta.hasUnpublishedChanges" size="small" type="warning">有未发布修改</el-tag>
        </template>
      </div>
      <div class="top-bar__actions">
        <el-button v-if="isEdit && contentType !== 'CUSTOMER'" @click="goPreview">预览草稿</el-button>
        <el-button type="primary" :loading="saving" @click="onSaveDraft">保存草稿</el-button>
        <el-button v-if="isAdmin" type="success" :loading="publishing" @click="onPublish">
          保存并发布
        </el-button>
        <el-button
          v-if="isAdmin && meta?.status === 'PUBLISHED'"
          type="warning"
          plain
          @click="onOffline"
        >
          下线
        </el-button>
      </div>
    </div>

    <!-- 客户标识 CUSTOMER 专属极简表单 -->
    <el-card v-if="contentType === 'CUSTOMER'" shadow="never" class="main-card">
      <el-form label-width="110px">
        <el-form-item label="客户名称" required>
          <el-input v-model="form.title" maxlength="60" show-word-limit />
        </el-form-item>
        <el-form-item label="客户 Logo" required>
          <div class="media-pick">
            <img v-if="form.logoMediaId" :src="`/api/admin/media/${form.logoMediaId}/file`" alt="Logo" />
            <span v-else class="media-pick__empty">未选择</span>
            <el-button size="small" @click="openMedia('IMAGE', (m) => (form.logoMediaId = Number(m.id)))">
              选择 Logo 图片
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="客户官网外链">
          <el-input v-model="form.websiteUrl" maxlength="500" placeholder="可选，https://..." />
        </el-form-item>
        <el-form-item label="排序权重">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 产品 / 方案 / 案例 / 新闻 完整编辑器 -->
    <template v-else>
      <el-card shadow="never" class="main-card">
        <el-form label-width="110px">
          <el-form-item label="标题" required>
            <el-input v-model="form.title" maxlength="120" show-word-limit placeholder="请输入标题" />
          </el-form-item>

          <div class="form-grid-2">
            <el-form-item label="所属分类">
              <el-select v-model="form.categoryId" clearable placeholder="选择分类" style="width: 100%">
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="排序权重">
              <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
            </el-form-item>
          </div>

          <el-form-item label="摘要导语">
            <el-input v-model="form.summary" type="textarea" :rows="2" maxlength="300" show-word-limit />
          </el-form-item>

          <div class="form-grid-2">
            <el-form-item label="列表封面图" required>
              <div class="media-pick">
                <img v-if="form.coverMediaId" :src="`/api/admin/media/${form.coverMediaId}/file`" alt="封面" />
                <span v-else class="media-pick__empty">必选</span>
                <el-button size="small" @click="openMedia('IMAGE', (m) => (form.coverMediaId = Number(m.id)))">
                  选择封面
                </el-button>
              </div>
            </el-form-item>
            <el-form-item v-if="contentType !== 'NEWS'" label="详情首屏大图">
              <div class="media-pick">
                <img v-if="form.heroMediaId" :src="`/api/admin/media/${form.heroMediaId}/file`" alt="Hero" />
                <span v-else class="media-pick__empty">可选(默认用封面)</span>
                <el-button size="small" @click="openMedia('ALL', (m) => (form.heroMediaId = Number(m.id)))">
                  选择素材
                </el-button>
                <el-button v-if="form.heroMediaId" size="small" link type="danger" @click="form.heroMediaId = null">
                  清除
                </el-button>
              </div>
            </el-form-item>
          </div>

          <!-- 类型专属结构化字段 -->
          <template v-if="contentType === 'PRODUCT'">
            <el-divider content-position="left">产品核心特性 (features，最多 8 条)</el-divider>
            <div v-for="(feat, fIdx) in form.features" :key="fIdx" class="sub-row">
              <el-input v-model="feat.title" maxlength="30" placeholder="特性标题" style="width: 200px" />
              <el-input v-model="feat.description" maxlength="200" placeholder="特性说明" style="flex: 1" />
              <el-button link type="danger" @click="form.features.splice(fIdx, 1)">删除</el-button>
            </div>
            <el-button size="small" @click="form.features.length < 8 && form.features.push({ title: '', description: '' })">
              + 添加产品特性
            </el-button>

            <el-divider content-position="left">关联标杆案例</el-divider>
            <ContentPicker v-model="form.relatedCaseIds" content-type="CASE" :max="6" />
          </template>

          <template v-else-if="contentType === 'SOLUTION'">
            <el-form-item label="适用行业">
              <el-input v-model="form.industry" maxlength="60" style="width: 280px" placeholder="例如：冷链物流 / 大宗能源" />
            </el-form-item>
            <el-divider content-position="left">行业痛点 (pains，最多 6 条)</el-divider>
            <div v-for="(p, pIdx) in form.pains" :key="pIdx" class="sub-row">
              <el-input v-model="p.title" maxlength="30" placeholder="痛点标题" style="width: 200px" />
              <el-input v-model="p.description" maxlength="200" placeholder="痛点说明" style="flex: 1" />
              <el-button link type="danger" @click="form.pains.splice(pIdx, 1)">删除</el-button>
            </div>
            <el-button size="small" @click="form.pains.length < 6 && form.pains.push({ title: '', description: '' })">
              + 添加行业痛点
            </el-button>

            <el-divider content-position="left">方案核心价值 (benefits，最多 6 条)</el-divider>
            <div v-for="(b, bIdx) in form.benefits" :key="bIdx" class="sub-row">
              <el-input v-model="b.title" maxlength="30" placeholder="价值标题" style="width: 200px" />
              <el-input v-model="b.description" maxlength="200" placeholder="价值说明" style="flex: 1" />
              <el-button link type="danger" @click="form.benefits.splice(bIdx, 1)">删除</el-button>
            </div>
            <el-button size="small" @click="form.benefits.length < 6 && form.benefits.push({ title: '', description: '' })">
              + 添加方案价值
            </el-button>

            <el-divider content-position="left">关联推荐产品</el-divider>
            <ContentPicker v-model="form.relatedProductIds" content-type="PRODUCT" :max="6" />
          </template>

          <template v-else-if="contentType === 'CASE'">
            <div class="form-grid-2">
              <el-form-item label="客户名称" required>
                <el-input v-model="form.customerName" maxlength="60" />
              </el-form-item>
              <el-form-item label="所属行业">
                <el-input v-model="form.industry" maxlength="60" />
              </el-form-item>
            </div>

            <el-divider content-position="left">量化成果指标 (outcomes，最多 4 条)</el-divider>
            <div v-for="(out, oIdx) in form.outcomes" :key="oIdx" class="sub-row">
              <el-input v-model="out.displayValue" maxlength="20" placeholder="数值(如 68%↓)" style="width: 160px" />
              <el-input v-model="out.label" maxlength="40" placeholder="成果说明(如 事故率下降)" style="flex: 1" />
              <el-button link type="danger" @click="form.outcomes.splice(oIdx, 1)">删除</el-button>
            </div>
            <el-button size="small" @click="form.outcomes.length < 4 && form.outcomes.push({ displayValue: '', label: '' })">
              + 添加成果指标
            </el-button>

            <div class="form-grid-2" style="margin-top: 14px">
              <el-form-item label="客户证言">
                <el-input v-model="form.quoteText" maxlength="300" placeholder="选填，与证言人成对填写" />
              </el-form-item>
              <el-form-item label="证言人及职务">
                <el-input v-model="form.quoteAuthor" maxlength="60" />
              </el-form-item>
            </div>
          </template>

          <template v-else-if="contentType === 'NEWS'">
            <div class="form-grid-2">
              <el-form-item label="展示日期" required>
                <el-input v-model="form.displayTime" placeholder="YYYY-MM-DD HH:mm" />
              </el-form-item>
              <el-form-item label="作者">
                <el-input v-model="form.author" maxlength="50" />
              </el-form-item>
            </div>
            <div class="form-grid-2">
              <el-form-item label="来源名称">
                <el-input v-model="form.sourceName" maxlength="100" />
              </el-form-item>
              <el-form-item label="来源外链">
                <el-input v-model="form.sourceUrl" maxlength="500" placeholder="https://..." />
              </el-form-item>
            </div>
          </template>

          <!-- 可视化富文本工具栏 + 左右分屏实时预览 -->
          <el-divider content-position="left">详情正文 (支持快捷排版与实时预览)</el-divider>
          <div class="richtext-toolbar">
            <div class="richtext-toolbar__btns">
              <el-button size="small" @click="insertHtmlSnippet('<h2>在此输入内容</h2>\n')">H2 章节标题</el-button>
              <el-button size="small" @click="insertHtmlSnippet('<h3>在此输入内容</h3>\n')">H3 子标题</el-button>
              <el-button size="small" @click="insertHtmlSnippet('<p><strong>在此输入内容</strong></p>\n')">B 粗体段落</el-button>
              <el-button size="small" @click="insertHtmlSnippet('<blockquote><p>在此输入内容</p></blockquote>\n')">“ 引用高亮</el-button>
              <el-button size="small" @click="insertHtmlSnippet('<ul>\n  <li>要点一</li>\n  <li>要点二</li>\n</ul>\n')">• 列表</el-button>
              <el-button size="small" @click="insertHtmlSnippet('<table><thead><tr><th>指标</th><th>数值</th></tr></thead><tbody><tr><td>效率提升</td><td>45%</td></tr></tbody></table>\n')">
                📊 表格
              </el-button>
              <el-button size="small" type="primary" plain @click="insertBodyImage">
                🖼️ 插入素材库图片 (data-media-id)
              </el-button>
            </div>
            <el-radio-group v-model="editorMode" size="small">
              <el-radio-button value="split">左右分屏实时预览</el-radio-button>
              <el-radio-button value="code">仅编辑源码</el-radio-button>
            </el-radio-group>
          </div>

          <div class="richtext-workbench" :class="{ 'is-split': editorMode === 'split' }">
            <textarea
              ref="bodyTextareaRef"
              v-model="form.bodyHtml"
              class="richtext-source"
              rows="18"
              placeholder="在此输入或通过上方工具栏插入 HTML 正文。正文内图片请点击「插入素材库图片」，自动生成受控的 data-media-id 标签..."
            ></textarea>

            <div v-if="editorMode === 'split'" class="richtext-preview">
              <div class="richtext-preview__head">前台排版实时预览（复用 ArticleBody）</div>
              <div class="richtext-preview__body">
                <ArticleBody :html="previewHtml" :show-toc="false" :show-progress="false" :show-tools="false" />
              </div>
            </div>
          </div>

          <el-divider content-position="left">独立 SEO 设置与搜索预览</el-divider>
          <div class="form-grid-2">
            <el-form-item label="SEO 标题">
              <el-input v-model="form.seoTitle" maxlength="80" placeholder="选填，未填时默认使用内容标题" />
            </el-form-item>
            <el-form-item label="SEO 描述">
              <el-input v-model="form.seoDescription" maxlength="200" placeholder="选填，未填时默认使用摘要" />
            </el-form-item>
          </div>
        </el-form>
      </el-card>
    </template>

    <MediaPicker v-model="pickerOpen" :kind="pickerKind" @select="onMediaPicked" />
  </div>
</template>

<style scoped>
.editor-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.top-bar {
  position: sticky;
  top: 0;
  z-index: 15;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 18px;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(8px);
  border: 1px solid var(--a-border);
  border-radius: 10px;
}

.top-bar__left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.top-bar__title {
  font-size: 15px;
}

.top-bar__actions {
  display: flex;
  gap: 8px;
}

.form-grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

@media (max-width: 900px) {
  .form-grid-2 {
    grid-template-columns: 1fr;
  }
}

.media-pick {
  display: flex;
  align-items: center;
  gap: 10px;
}

.media-pick img,
.media-pick__empty {
  width: 84px;
  height: 52px;
  border-radius: 6px;
  border: 1px solid var(--a-border);
  object-fit: cover;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: var(--a-text-muted);
  background: #f6f8fa;
}

.sub-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.richtext-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  flex-wrap: wrap;
  padding: 10px 12px;
  background: #f8f9fc;
  border: 1px solid var(--a-border);
  border-radius: 8px 8px 0 0;
}

.richtext-toolbar__btns {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.richtext-workbench {
  display: grid;
  grid-template-columns: 1fr;
  border: 1px solid var(--a-border);
  border-top: none;
  border-radius: 0 0 8px 8px;
  overflow: hidden;
}

.richtext-workbench.is-split {
  grid-template-columns: 1fr 1fr;
}

@media (max-width: 960px) {
  .richtext-workbench.is-split {
    grid-template-columns: 1fr;
  }
}

.richtext-source {
  width: 100%;
  border: none;
  padding: 14px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 13.5px;
  line-height: 1.65;
  outline: none;
  resize: vertical;
  background: #0f172a;
  color: #e2e8f0;
}

.richtext-preview {
  background: #ffffff;
  border-left: 1px solid var(--a-border);
  display: flex;
  flex-direction: column;
  max-height: 520px;
}

.richtext-preview__head {
  padding: 8px 14px;
  font-size: 12px;
  font-weight: 600;
  color: #5b4be8;
  background: #f5f3ff;
  border-bottom: 1px solid var(--a-border);
}

.richtext-preview__body {
  padding: 16px 20px;
  overflow-y: auto;
  flex: 1;
}
</style>
