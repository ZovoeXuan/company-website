<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ApiError } from '@/api/http'
import {
  createContent,
  getContent,
  listCategories,
  offlineContent,
  publishContent,
  saveDraft,
  type CategoryView,
  type ContentDetail,
} from '@/api/content'
import ContentPicker from '@/components/ContentPicker.vue'
import MediaPicker from '@/components/MediaPicker.vue'

interface TitledItem {
  title: string
  description: string
  iconMediaId?: number
}

interface OutcomeItem {
  displayValue: string
  label: string
}

interface ContentForm {
  title: string
  summary: string
  bodyHtml: string
  categoryId: string
  sortOrder: number
  displayTime: string
  coverId: string
  heroId: string
  logoId: string
  logoAlt: string
  websiteUrl: string
  customerName: string
  industry: string
  author: string
  sourceName: string
  sourceUrl: string
  quoteText: string
  quoteAuthor: string
  seoTitle: string
  seoDescription: string
  features: TitledItem[]
  pains: TitledItem[]
  benefits: TitledItem[]
  outcomes: OutcomeItem[]
  relatedCaseIds: number[]
  relatedProductIds: number[]
  relatedSolutionIds: number[]
}

const route = useRoute()
const router = useRouter()

const contentType = computed(() => String(route.meta.contentType ?? 'PRODUCT'))
const slug = computed(() => String(route.meta.slug ?? 'products'))
const isNew = computed(() => !route.params.id)
const contentId = computed(() => (route.params.id ? String(route.params.id) : ''))

const isProduct = computed(() => contentType.value === 'PRODUCT')
const isSolution = computed(() => contentType.value === 'SOLUTION')
const isCase = computed(() => contentType.value === 'CASE')
const isNews = computed(() => contentType.value === 'NEWS')
const isCustomer = computed(() => contentType.value === 'CUSTOMER')
const hasCategory = computed(() => !isCustomer.value)
const hasBody = computed(() => !isCustomer.value)
const titleLabel = computed(() => (isCustomer.value ? '客户名称' : '标题'))

const loading = ref(false)
const saving = ref(false)
const publishing = ref(false)
const detail = ref<ContentDetail | null>(null)
const categories = ref<CategoryView[]>([])
const pickerOpen = ref(false)

type PickerTarget =
  | { kind: 'cover' }
  | { kind: 'hero' }
  | { kind: 'logo' }
  | { kind: 'featureIcon'; index: number }

const pickerTarget = ref<PickerTarget>({ kind: 'cover' })

const form = ref<ContentForm>(blankForm())

function blankForm(): ContentForm {
  return {
    title: '',
    summary: '',
    bodyHtml: '',
    categoryId: '',
    sortOrder: 100,
    displayTime: '',
    coverId: '',
    heroId: '',
    logoId: '',
    logoAlt: '',
    websiteUrl: '',
    customerName: '',
    industry: '',
    author: '',
    sourceName: '',
    sourceUrl: '',
    quoteText: '',
    quoteAuthor: '',
    seoTitle: '',
    seoDescription: '',
    features: [],
    pains: [],
    benefits: [],
    outcomes: [],
    relatedCaseIds: [],
    relatedProductIds: [],
    relatedSolutionIds: [],
  }
}

function str(value: unknown) {
  return typeof value === 'string' ? value : ''
}

function numStr(value: unknown) {
  return typeof value === 'number' || typeof value === 'string' ? String(value) : ''
}

function titled(value: unknown): TitledItem[] {
  if (!Array.isArray(value)) return []
  return value.map((raw) => {
    const item = (raw ?? {}) as Partial<TitledItem>
    return {
      title: str(item.title),
      description: str(item.description),
      iconMediaId: item.iconMediaId === undefined || item.iconMediaId === null
        ? undefined
        : Number(item.iconMediaId),
    }
  })
}

function outcomeList(value: unknown): OutcomeItem[] {
  if (!Array.isArray(value)) return []
  return value.map((raw) => {
    const item = (raw ?? {}) as Partial<OutcomeItem>
    return { displayValue: str(item.displayValue), label: str(item.label) }
  })
}

function idList(value: unknown): number[] {
  if (!Array.isArray(value)) return []
  return value.map((item) => Number(item)).filter((n) => Number.isFinite(n) && n > 0)
}

function cleanTitled(items: TitledItem[]) {
  return items
    .filter((item) => item.title.trim() || item.description.trim())
    .map((item) => {
      const result: Record<string, unknown> = {
        title: item.title.trim(),
        description: item.description.trim(),
      }
      if (item.iconMediaId) result.iconMediaId = Number(item.iconMediaId)
      return result
    })
}

function fill(data: ContentDetail) {
  const source = (data.draft ?? {}) as Record<string, unknown>
  const seo = (source.seo ?? {}) as Record<string, unknown>
  const values = form.value
  values.title = str(source.title)
  values.summary = str(source.summary)
  values.bodyHtml = str(source.bodyHtml)
  values.categoryId = source.categoryId ? String(source.categoryId) : ''
  values.sortOrder = typeof source.sortOrder === 'number' ? source.sortOrder : 100
  values.displayTime = str(source.displayTime)
  values.coverId = numStr(source.coverMediaId)
  values.heroId = numStr(source.heroMediaId)
  values.logoId = numStr(source.logoMediaId)
  values.logoAlt = str(source.logoAlt)
  values.websiteUrl = str(source.websiteUrl)
  values.customerName = str(source.customerName)
  values.industry = str(source.industry)
  values.author = str(source.author)
  values.sourceName = str(source.sourceName)
  values.sourceUrl = str(source.sourceUrl)
  values.quoteText = str(source.quoteText)
  values.quoteAuthor = str(source.quoteAuthor)
  values.seoTitle = str(seo.title)
  values.seoDescription = str(seo.description)
  values.features = titled(source.features)
  values.pains = titled(source.pains)
  values.benefits = titled(source.benefits)
  values.outcomes = outcomeList(source.outcomes)
  values.relatedCaseIds = idList(source.relatedCaseIds)
  values.relatedProductIds = idList(source.relatedProductIds)
  values.relatedSolutionIds = idList(source.relatedSolutionIds)
}

function reportError(error: unknown) {
  if (error instanceof ApiError) {
    const fields = error.fieldErrors?.map((item) => `${item.field}：${item.message}`).join('；')
    ElMessage.error(fields ? `${error.message}（${fields}）` : error.message)
  } else {
    ElMessage.error('操作失败，请重试')
  }
}

function thumb(id: string | number) {
  return `/api/admin/media/${id}`
}

function openPicker(target: PickerTarget) {
  pickerTarget.value = target
  pickerOpen.value = true
}

function onPick(media: { id: string | number }) {
  const target = pickerTarget.value
  const values = form.value
  if (target.kind === 'cover') values.coverId = String(media.id)
  if (target.kind === 'hero') values.heroId = String(media.id)
  if (target.kind === 'logo') values.logoId = String(media.id)
  if (target.kind === 'featureIcon') {
    const item = values.features[target.index]
    if (item) item.iconMediaId = Number(media.id)
  }
}

function addTitled(list: TitledItem[], max: number) {
  if (list.length >= max) {
    ElMessage.warning('最多 ' + max + ' 条')
    return
  }
  list.push({ title: '', description: '' })
}

function addOutcome() {
  if (form.value.outcomes.length >= 4) {
    ElMessage.warning('最多 4 条')
    return
  }
  form.value.outcomes.push({ displayValue: '', label: '' })
}

function statusText(status: string) {
  if (status === 'PUBLISHED') return '已发布'
  if (status === 'OFFLINE') return '已下线'
  return '未发布'
}

function back() {
  router.push(`/contents/${slug}`)
}

function buildDraft() {
  const values = form.value
  const payload: Record<string, unknown> = {
    title: values.title.trim(),
    sortOrder: values.sortOrder,
  }

  if (isCustomer.value) {
    if (values.logoId) payload.logoMediaId = Number(values.logoId)
    if (values.logoAlt.trim()) payload.logoAlt = values.logoAlt.trim()
    if (values.websiteUrl.trim()) payload.websiteUrl = values.websiteUrl.trim()
    return payload
  }

  payload.summary = values.summary.trim()
  if (values.bodyHtml.trim()) payload.bodyHtml = values.bodyHtml
  if (values.categoryId) payload.categoryId = Number(values.categoryId)
  if (values.coverId) payload.coverMediaId = Number(values.coverId)
  if (values.heroId) payload.heroMediaId = Number(values.heroId)
  if (values.seoTitle.trim() || values.seoDescription.trim()) {
    payload.seo = {
      ...(values.seoTitle.trim() ? { title: values.seoTitle.trim() } : {}),
      ...(values.seoDescription.trim() ? { description: values.seoDescription.trim() } : {}),
  }
  }

  if (isProduct.value) {
    const features = cleanTitled(values.features)
    if (features.length) payload.features = features
    if (values.relatedCaseIds.length) payload.relatedCaseIds = values.relatedCaseIds
  }
  if (isSolution.value) {
    if (values.industry.trim()) payload.industry = values.industry.trim()
    const pains = cleanTitled(values.pains)
    if (pains.length) payload.pains = pains
    const benefits = cleanTitled(values.benefits)
    if (benefits.length) payload.benefits = benefits
    if (values.relatedProductIds.length) payload.relatedProductIds = values.relatedProductIds
  }
  if (isCase.value) {
    if (values.customerName.trim()) payload.customerName = values.customerName.trim()
    if (values.industry.trim()) payload.industry = values.industry.trim()
    const outcomes = values.outcomes
      .filter((item) => item.displayValue.trim() || item.label.trim())
      .map((item) => ({ displayValue: item.displayValue.trim(), label: item.label.trim() }))
    if (outcomes.length) payload.outcomes = outcomes
    if (values.quoteText.trim()) payload.quoteText = values.quoteText.trim()
    if (values.quoteAuthor.trim()) payload.quoteAuthor = values.quoteAuthor.trim()
    if (values.relatedProductIds.length) payload.relatedProductIds = values.relatedProductIds
    if (values.relatedSolutionIds.length) payload.relatedSolutionIds = values.relatedSolutionIds
  }
  if (isNews.value) {
    if (values.displayTime) payload.displayTime = values.displayTime
    if (values.author.trim()) payload.author = values.author.trim()
    if (values.sourceName.trim()) payload.sourceName = values.sourceName.trim()
    if (values.sourceUrl.trim()) payload.sourceUrl = values.sourceUrl.trim()
  }
  return payload
}

async function load() {
  loading.value = true
  try {
    await loadCategories()
    if (!isNew.value) {
      const data = await getContent(contentId.value)
      detail.value = data
      fill(data)
    } else {
      form.value = blankForm()
    }
  } catch (error) {
    reportError(error)
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  if (isCustomer.value) {
    categories.value = []
    return
  }
  try {
    const result = await listCategories({ categoryType: contentType.value, page: 1, pageSize: 50 })
    categories.value = result.items
  } catch (error) {
    reportError(error)
  }
}

async function submit(publish: boolean) {
  const draft = buildDraft()
  if (publish) publishing.value = true
  else saving.value = true
  try {
    let data: ContentDetail
    if (isNew.value) {
      data = await createContent(contentType.value, draft)
    } else {
      data = await saveDraft(contentId.value, draft, detail.value?.content.lockVersion ?? 0)
    }
    if (publish) {
      await publishContent(data.content.id, data.content.lockVersion, data.content.draftVersion)
      data = await getContent(data.content.id)
    }
    detail.value = data
    fill(data)
    ElMessage.success(publish ? '已保存并发布' : '草稿已保存')
    if (isNew.value) await router.replace(`/contents/${slug}/${data.content.id}`)
  } catch (error) {
    reportError(error)
  } finally {
    saving.value = false
    publishing.value = false
  }
}

async function offline() {
  try {
    await ElMessageBox.confirm('下线后官网将不再展示该内容，确认继续？', '下线确认', {
      type: 'warning',
    })
  } catch {
    return
  }
  if (!detail.value) return
  try {
    await offlineContent(contentId.value, detail.value.content.lockVersion)
    const data = await getContent(contentId.value)
    detail.value = data
    fill(data)
    ElMessage.success('已下线')
  } catch (error) {
    reportError(error)
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="editor">
    <el-card shadow="never" class="main">
      <template #header>
        <div class="head">
          <span>{{ isNew ? '新建' : '编辑' }}{{ route.meta.title }}</span>
    <el-button @click="back">返回列表</el-button>
        </div>
      </template>

      <el-form label-width="104px" label-position="right">
        <el-form-item :label="titleLabel" required>
          <el-input
            v-model="form.title"
            :maxlength="isCustomer ? 60 : 120"
            show-word-limit
            :placeholder="isCustomer ? '对外展示的客户名称' : '建议 20 字以内，写清对象与价值'"
          />
        </el-form-item>

        <template v-if="isCustomer">
          <el-form-item label="客户 Logo" required>
            <div class="media">
              <img v-if="form.logoId" :src="thumb(form.logoId)" alt="客户 Logo" />
              <span v-else class="media__empty">未选择</span>
    <el-button size="small" @click="openPicker({ kind: 'logo' })">选择图片</el-button>
              <el-button v-if="form.logoId" size="small" link type="danger" @click="form.logoId = ''">
                清除
              </el-button>
            </div>
            <p class="field-hint">建议 PNG 透明底，前台按 contain 显示，不会拉伸。</p>
          </el-form-item>
          <el-form-item label="Logo 替代文字">
            <el-input v-model="form.logoAlt" maxlength="120" placeholder="不填时使用客户名称" />
          </el-form-item>
          <el-form-item label="客户网址">
            <el-input v-model="form.websiteUrl" maxlength="500" placeholder="https://，选填" />
          </el-form-item>
        </template>

        <template v-if="!isCustomer">
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
              <el-option
                v-for="item in categories"
                :key="item.id"
                :label="item.name + '（' + (item.status === 'PUBLISHED' ? '已发布' : '未发布') + '）'"
                :value="item.id"
              />
            </el-select>
            <RouterLink to="/contents/categories" class="tip-link">管理分类</RouterLink>
          </el-form-item>

          <el-form-item v-if="isNews" label="展示日期" required>
            <el-date-picker
              v-model="form.displayTime"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="不能晚于今天"
            />
          </el-form-item>

          <el-form-item label="封面" required>
            <div class="media">
              <img v-if="form.coverId" :src="thumb(form.coverId)" alt="封面预览" />
              <span v-else class="media__empty">未选择</span>
            <el-button size="small" @click="openPicker({ kind: 'cover' })">选择图片</el-button>
              <el-button v-if="form.coverId" size="small" link type="danger" @click="form.coverId = ''">
                清除
              </el-button>
            </div>
            <p class="field-hint">列表与详情复用同一张封面，建议 16:9。</p>
          </el-form-item>

          <el-form-item v-if="!isNews" label="主图">
            <div class="media">
              <img v-if="form.heroId" :src="thumb(form.heroId)" alt="主图预览" />
            <span v-else class="media__empty">未选择时使用封面</span>
              <el-button size="small" @click="openPicker({ kind: 'hero' })">选择图片</el-button>
              <el-button v-if="form.heroId" size="small" link type="danger" @click="form.heroId = ''">
                清除
              </el-button>
            </div>
          </el-form-item>

          <el-form-item v-if="isSolution || isCase" label="所属行业">
            <el-input v-model="form.industry" maxlength="60" placeholder="例如：汽车零部件" style="width: 260px" />
          </el-form-item>

          <el-form-item v-if="isCase" label="客户名称" required>
            <el-input v-model="form.customerName" maxlength="60" placeholder="可按协议使用简称" style="width: 260px" />
          </el-form-item>
        </template>

        <!-- 产品能力 -->
        <template v-if="isProduct">
          <el-divider content-position="left">核心能力（最多 8 条）</el-divider>
          <div v-for="(item, index) in form.features" :key="'feature-' + index" class="row">
            <el-input v-model="item.title" maxlength="30" placeholder="能力标题" style="width: 180px" />
            <el-input v-model="item.description" maxlength="200" placeholder="一句话说明" style="width: 300px" />
            <el-button size="small" @click="openPicker({ kind: 'featureIcon', index })">图标</el-button>
            <el-button link type="danger" @click="form.features.splice(index, 1)">删除</el-button>
          </div>
          <el-button class="add" @click="addTitled(form.features, 8)">添加能力</el-button>
        </template>

        <!-- 方案痛点与价值 -->
        <template v-if="isSolution">
          <el-divider content-position="left">业务痛点（最多 6 条）</el-divider>
          <div v-for="(item, index) in form.pains" :key="'pain-' + index" class="row">
            <el-input v-model="item.title" maxlength="30" placeholder="痛点标题" style="width: 180px" />
            <el-input v-model="item.description" maxlength="200" placeholder="一句话说明" style="width: 320px" />
            <el-button link type="danger" @click="form.pains.splice(index, 1)">删除</el-button>
          </div>
          <el-button class="add" @click="addTitled(form.pains, 6)">添加痛点</el-button>

            <el-divider content-position="left">方案价值（最多 6 条）</el-divider>
          <div v-for="(item, index) in form.benefits" :key="'benefit-' + index" class="row">
            <el-input v-model="item.title" maxlength="30" placeholder="价值标题" style="width: 180px" />
            <el-button link type="danger" @click="form.benefits.splice(index, 1)">删除</el-button>
          </div>
          <el-button class="add" @click="addTitled(form.benefits, 6)">添加价值</el-button>
        </template>

        <!-- 案例成果与引用 -->
        <template v-if="isCase">
          <el-divider content-position="left">项目成果（最多 4 条）</el-divider>
          <div v-for="(item, index) in form.outcomes" :key="'outcome-' + index" class="row">
            <el-input v-model="item.displayValue" maxlength="20" placeholder="例如 30%" style="width: 140px" />
            <el-input v-model="item.label" maxlength="40" placeholder="例如 非计划停机下降" style="width: 260px" />
            <el-button link type="danger" @click="form.outcomes.splice(index, 1)">删除</el-button>
          </div>
          <el-button class="add" @click="addOutcome">添加成果</el-button>

          <el-divider content-position="left">客户引用（成对填写）</el-divider>
          <el-form-item label="引用语">
            <el-input v-model="form.quoteText" type="textarea" :rows="2" maxlength="300" show-word-limit />
          </el-form-item>
          <el-form-item label="引用人">
            <el-input v-model="form.quoteAuthor" maxlength="60" placeholder="例如：生产总监 王先生" style="width: 260px" />
          </el-form-item>
        </template>

        <!-- 新闻来源 -->
        <template v-if="isNews">
          <el-divider content-position="left">来源信息</el-divider>
          <el-form-item label="作者">
            <el-input v-model="form.author" maxlength="50" style="width: 220px" />
          </el-form-item>
          <el-form-item label="来源名称">
            <el-input v-model="form.sourceName" maxlength="100" style="width: 260px" />
          </el-form-item>
          <el-form-item label="来源链接">
            <el-input v-model="form.sourceUrl" maxlength="500" placeholder="https://，填链接必须填来源名" />
          </el-form-item>
        </template>

        <!-- 关联内容 -->
        <template v-if="isProduct">
          <el-divider content-position="left">相关案例（最多 6 条）</el-divider>
          <el-form-item label="已选案例">
            <ContentPicker v-model="form.relatedCaseIds" content-type="CASE" :max="6" />
          </el-form-item>
        </template>

        <template v-if="isSolution">
          <el-divider content-position="left">用到的产品（最多 6 条）</el-divider>
          <el-form-item label="已选产品">
            <ContentPicker v-model="form.relatedProductIds" content-type="PRODUCT" :max="6" />
          </el-form-item>
        </template>

        <template v-if="isCase">
          <el-divider content-position="left">用到的产品（最多 6 条）</el-divider>
          <el-form-item label="已选产品">
            <ContentPicker v-model="form.relatedProductIds" content-type="PRODUCT" :max="6" />
          </el-form-item>
          <el-form-item label="相关方案">
            <ContentPicker v-model="form.relatedSolutionIds" content-type="SOLUTION" :max="6" />
          </el-form-item>
        </template>

        <!-- 正文 -->
        <el-form-item v-if="hasBody" label="正文" required>
          <el-input
            v-model="form.bodyHtml"
            type="textarea"
            :rows="12"
            placeholder="支持基础 HTML，例如 <h2>解决什么问题</h2><p>…</p>"
          />
          <p class="field-hint">当前为 HTML 源码输入，保存时服务端会做白名单清洗。</p>
        </el-form-item>

        <el-divider content-position="left">SEO（可选）</el-divider>
        <el-form-item label="SEO 标题">
          <el-input v-model="form.seoTitle" maxlength="80" show-word-limit placeholder="不填时使用内容标题" />
        </el-form-item>
        <el-form-item label="SEO 描述">
          <el-input
            v-model="form.seoDescription"
            type="textarea"
            :rows="2"
            maxlength="200"
            show-word-limit
            placeholder="不填时使用摘要"
          />
        </el-form-item>

        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="side">
      <template #header>状态</template>
      <div v-if="detail" class="status">
        <p>
          <span class="status__label">当前状态</span>
          <span class="status__value">{{ statusText(detail.content.status) }}</span>
        </p>
        <p>
          <span class="status__label">草稿版本</span>
          <span class="status__value">v{{ detail.content.draftVersion }}</span>
        </p>
        <p>
          <span class="status__label">线上版本</span>
          <span class="status__value">
            {{ detail.content.publishedVersion ? 'v' + detail.content.publishedVersion : '未发布' }}
          </span>
        </p>
        <p v-if="detail.content.hasUnpublishedChanges" class="warn">有未发布的修改</p>
      </div>
      <p v-else class="muted">保存后显示版本信息</p>

      <div class="actions">
        <el-button :loading="saving" @click="submit(false)">保存草稿</el-button>
        <el-button type="primary" :loading="publishing" @click="submit(true)">保存并发布</el-button>
        <el-button v-if="!isNew" type="warning" plain @click="offline">下线</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="side">
      <template #header>发布规则</template>
      <ul class="rules">
        <li>保存草稿只写草稿，官网不受影响。</li>
        <li>发布会把当前草稿整体变成线上版本。</li>
        <li>发布要求：标题、摘要、正文、封面齐全；所选分类必须已发布。</li>
        <li>新闻必须填写不晚于今天的展示日期。</li>
        <li>已发布的内容不能直接删除，请先下线。</li>
      </ul>
    </el-card>

    <MediaPicker v-model="pickerOpen" :selected-id="null" @select="onPick" />
  </div>
</template>

<style scoped>
.editor {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 16px;
  align-items: start;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.media {
  display: flex;
  align-items: center;
  gap: 10px;
}

.media img {
  width: 96px;
  height: 54px;
  object-fit: cover;
  border: 1px solid var(--a-border);
  border-radius: 6px;
}

.media__empty {
  font-size: 12px;
  color: var(--a-text-subtle);
}

.row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.field-hint {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--a-text-subtle);
}

.add {
  margin-top: 4px;
}

.status p {
  display: flex;
  justify-content: space-between;
  margin: 0 0 8px;
  font-size: 13px;
}

.status__label {
  color: var(--a-text-muted);
}

.warn {
  color: var(--a-warning);
  font-size: 13px;
}

.muted {
  color: var(--a-text-subtle);
  font-size: 13px;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--a-border);
}

.rules {
  margin: 0;
  padding-left: 18px;
  font-size: 12px;
  color: var(--a-text-muted);
  line-height: 1.9;
}

.tip-link {
  margin-left: 8px;
  font-size: 12px;
  color: var(--a-brand);
}

@media (max-width: 1080px) {
  .editor {
    grid-template-columns: 1fr;
  }
}
</style>
