<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import ConfigPanel from '@/components/ConfigPanel.vue'
import ContentPicker from '@/components/ContentPicker.vue'
import LinkEditor, { type LinkValue } from '@/components/LinkEditor.vue'
import MediaPicker from '@/components/MediaPicker.vue'
import { useConfig } from '@/composables/useConfig'
import { get } from '@/api/http'
import { getMedia, type MediaItem } from '@/api/content'

interface HeroItem {
  key?: string
  enabled: boolean
  sortOrder: number
  title: string
  subtitle: string
  desktopMediaId?: number
  mobileMediaId?: number
  imageAlt: string
  buttonText: string
  link: LinkValue | null
}

interface MetricItem {
  key?: string
  sortOrder: number
  displayValue: string
  unit: string
  label: string
  description: string
}

interface ServiceGroup {
  key?: string
  sortOrder: number
  title: string
  description: string
  imageMediaId?: number
  contentType: string
  contentIds: number[]
  moreLink: LinkValue | null
}

interface BlockShape {
  enabled: boolean
  title: string
  subtitle: string
}

interface HomeDraft {
  hero: { items: HeroItem[] }
  metrics: BlockShape & { items: MetricItem[] }
  services: BlockShape & { groups: ServiceGroup[] }
  cases: BlockShape & { contentIds: number[] }
  customers: BlockShape & { contentIds: number[] }
  news: BlockShape & { mode: string; limit: number; contentIds: number[] }
  consultation: { title: string; subtitle: string; buttonText: string; link: LinkValue | null }
}

const { view, loading, saving, publishing, loadError, load, save, publish } =
  useConfig<HomeDraft & Record<string, unknown>>('HOME')

/** 预览用后台会话，新窗口打开；首屏与区块取草稿，关联内容取线上版本 */
function openPreview() {
  window.open(`${import.meta.env.BASE_URL}preview/home`, '_blank', 'noopener')
}

function emptyDraft(): HomeDraft {
  return {
    hero: { items: [] },
    metrics: { enabled: true, title: '企业实力', subtitle: '', items: [] },
    services: { enabled: true, title: '产品与解决方案', subtitle: '', groups: [] },
    cases: { enabled: true, title: '客户案例', subtitle: '', contentIds: [] },
    customers: { enabled: true, title: '客户标识', subtitle: '', contentIds: [] },
    news: { enabled: true, title: '新闻动态', subtitle: '', mode: 'SELECTED', limit: 3, contentIds: [] },
    consultation: { title: '了解适合您的服务', subtitle: '', buttonText: '立即咨询', link: { type: 'CONSULT' } },
  }
}

const form = reactive<HomeDraft>(emptyDraft())

// 已发布内容索引：用来发现草稿里指向「已下线/已删除」内容的引用
const publishedIndex = ref<Map<string, string>>(new Map())

const missingRefs = computed(() => {
  const missing: string[] = []
  const check = (ids: number[] | undefined, label: string) => {
    for (const id of ids ?? []) {
      if (!publishedIndex.value.has(String(id))) {
        missing.push(`${label} #${id}`)
      }
    }
  }
  for (const group of form.services.groups) {
    check(group.contentIds, `产品服务/${group.title || '未命名分组'}`)
  }
  check(form.cases.contentIds, '客户案例')
  check(form.customers.contentIds, '客户标识')
  if (form.news.mode === 'SELECTED') {
    check(form.news.contentIds, '新闻')
  }
  return missing
})

async function loadPublishedIndex() {
  try {
    const data = await get<{ items: Array<{ id: string; title: string }> }>(
      '/api/admin/contents?publicationStatus=PUBLISHED&page=1&pageSize=50',
    )
    publishedIndex.value = new Map(data.items.map((item) => [String(item.id), item.title]))
  } catch {
    // 索引加载失败不阻断编辑，失效提示会退化为「全部标红」，因此这里静默
    publishedIndex.value = new Map()
  }
}

function blockOf(source: unknown, fallback: BlockShape): BlockShape {
  const value = (source ?? {}) as Partial<BlockShape>
  return {
    enabled: value.enabled ?? fallback.enabled,
    title: value.title ?? '',
    subtitle: value.subtitle ?? '',
  }
}

function fill() {
  const draft = view.value?.draft as Partial<HomeDraft> | undefined
  if (!draft) {
    Object.assign(form, emptyDraft())
    return
  }
  form.hero.items = (draft.hero?.items ?? []).map((item) => ({
    key: item.key,
    enabled: item.enabled ?? true,
    sortOrder: item.sortOrder ?? 100,
    title: item.title ?? '',
    subtitle: item.subtitle ?? '',
    desktopMediaId: item.desktopMediaId,
    mobileMediaId: item.mobileMediaId,
    imageAlt: item.imageAlt ?? '',
    buttonText: item.buttonText ?? '',
    link: (item.link as LinkValue | null) ?? { type: 'NONE' },
  }))

  const metrics = blockOf(draft.metrics, { enabled: true, title: '', subtitle: '' })
  form.metrics.enabled = metrics.enabled
  form.metrics.title = metrics.title
  form.metrics.subtitle = metrics.subtitle
  form.metrics.items = (draft.metrics?.items ?? []).map((item) => ({
    key: item.key,
    sortOrder: item.sortOrder ?? 100,
    displayValue: item.displayValue ?? '',
    unit: item.unit ?? '',
    label: item.label ?? '',
    description: item.description ?? '',
  }))

  const services = blockOf(draft.services, { enabled: true, title: '', subtitle: '' })
  form.services.enabled = services.enabled
  form.services.title = services.title
  form.services.subtitle = services.subtitle
  form.services.groups = (draft.services?.groups ?? []).map((group) => ({
    key: group.key,
    sortOrder: group.sortOrder ?? 100,
    title: group.title ?? '',
    description: group.description ?? '',
    imageMediaId: group.imageMediaId,
    contentType: group.contentType ?? 'PRODUCT',
    contentIds: (group.contentIds ?? []).map(Number),
    moreLink: (group.moreLink as LinkValue | null) ?? null,
  }))

  const cases = blockOf(draft.cases, { enabled: true, title: '', subtitle: '' })
  form.cases.enabled = cases.enabled
  form.cases.title = cases.title
  form.cases.subtitle = cases.subtitle
  form.cases.contentIds = (draft.cases?.contentIds ?? []).map(Number)

  const customers = blockOf(draft.customers, { enabled: true, title: '', subtitle: '' })
  form.customers.enabled = customers.enabled
  form.customers.title = customers.title
  form.customers.subtitle = customers.subtitle
  form.customers.contentIds = (draft.customers?.contentIds ?? []).map(Number)

  const news = blockOf(draft.news, { enabled: true, title: '', subtitle: '' })
  form.news.enabled = news.enabled
  form.news.title = news.title
  form.news.subtitle = news.subtitle
  form.news.mode = draft.news?.mode ?? 'SELECTED'
  form.news.limit = draft.news?.limit ?? 3
  form.news.contentIds = (draft.news?.contentIds ?? []).map(Number)

  form.consultation.title = draft.consultation?.title ?? ''
  form.consultation.subtitle = draft.consultation?.subtitle ?? ''
  form.consultation.buttonText = draft.consultation?.buttonText ?? ''
  form.consultation.link = (draft.consultation?.link as LinkValue | null) ?? { type: 'CONSULT' }
  syncMedia()
}

const mediaCache = reactive<Record<number, MediaItem>>({})

async function syncMedia() {
  const ids = form.hero.items
    .flatMap((item) => [item.desktopMediaId, item.mobileMediaId])
    .filter((id): id is number => Boolean(id && !mediaCache[id]))
  await Promise.all(ids.map(async (id) => {
    try {
      mediaCache[id] = await getMedia(String(id))
    } catch {
      // 素材被删除时保留空预览，不阻断表单
    }
  }))
}

function isVideoMedia(id?: number): boolean {
  return Boolean(id && mediaCache[id]?.mediaType === 'VIDEO')
}

watch(
  () => form.hero.items.map((item) => [item.desktopMediaId, item.mobileMediaId]),
  () => syncMedia(),
  { deep: true },
)

async function onSave() {
  const payload = JSON.parse(JSON.stringify(form)) as HomeDraft
  if (await save(payload as HomeDraft & Record<string, unknown>)) fill()
}

async function onPublish() {
  if (await publish()) fill()
}

// ---- 首屏 ----
function addHero() {
  if (form.hero.items.length >= 5) {
    ElMessage.warning('首屏最多 5 条')
    return
  }
  form.hero.items.push({
    enabled: true,
    sortOrder: (form.hero.items.length + 1) * 10,
    title: '',
    subtitle: '',
    desktopMediaId: undefined,
    mobileMediaId: undefined,
    imageAlt: '',
    buttonText: '',
    link: { type: 'NONE' },
  })
}

// ---- 指标 ----
function addMetric() {
  if (form.metrics.items.length >= 8) {
    ElMessage.warning('指标最多 8 条')
    return
  }
  form.metrics.items.push({
    sortOrder: (form.metrics.items.length + 1) * 10,
    displayValue: '',
    unit: '',
    label: '',
    description: '',
  })
}

// ---- 服务分组 ----
function addGroup() {
  if (form.services.groups.length >= 3) {
    ElMessage.warning('服务分组最多 3 组')
    return
  }
  form.services.groups.push({
    sortOrder: (form.services.groups.length + 1) * 10,
    title: '',
    description: '',
    contentType: 'PRODUCT',
    contentIds: [],
    moreLink: { type: 'PAGE', pageCode: 'PRODUCTS' },
  })
}

// ---- 图片选择 ----
type PickerTarget =
  | { kind: 'heroDesktop'; index: number }
  | { kind: 'heroMobile'; index: number }
  | { kind: 'group'; index: number }

const pickerOpen = ref(false)
const pickerTarget = ref<PickerTarget | null>(null)
const pickerKind = computed<'IMAGE' | 'ALL'>(() =>
  pickerTarget.value?.kind === 'group' ? 'IMAGE' : 'ALL',
)

function openPicker(target: PickerTarget) {
  pickerTarget.value = target
  pickerOpen.value = true
}

function onPick(media: MediaItem) {
  const target = pickerTarget.value
  if (!target) return
  if (target.kind === 'heroDesktop') {
    form.hero.items[target.index].desktopMediaId = Number(media.id)
  } else if (target.kind === 'heroMobile') {
    form.hero.items[target.index].mobileMediaId = Number(media.id)
  } else {
    form.services.groups[target.index].imageMediaId = Number(media.id)
  }
  pickerOpen.value = false
}

function thumb(id?: number) {
  return id ? `/api/admin/media/${id}/file` : ''
}

function move<T extends { sortOrder: number }>(list: T[], index: number, delta: number) {
  const target = index + delta
  if (target < 0 || target >= list.length) return
  const current = list[index].sortOrder
  list[index].sortOrder = list[target].sortOrder
  list[target].sortOrder = current
  const [item] = list.splice(index, 1)
  list.splice(target, 0, item)
}

onMounted(async () => {
  await load()
  fill()
  loadPublishedIndex()
})
</script>

<template>
  <ConfigPanel
    title="首页管理"
    :status="view?.status"
    :draft-version="view?.draftVersion"
    :published-version="view?.publishedVersion"
    :has-unpublished-changes="view?.hasUnpublishedChanges"
    :loading="loading"
    :saving="saving"
    :publishing="publishing"
    @save="onSave"
    @publish="onPublish"
  >
    <template #header-actions>
      <el-button size="small" @click="openPreview">预览草稿</el-button>
    </template>
    <el-alert v-if="loadError" type="error" :closable="false" :title="loadError" class="err" />
    <el-alert
      type="info"
      :closable="false"
      class="err"
      title="首页顺序固定：首屏 → 指标 → 产品服务 → 客户案例 → 客户标识 → 新闻 → 咨询区。区块开关与文案都要发布后才生效。"
    />
    <el-alert
      v-if="missingRefs.length"
      type="warning"
      :closable="false"
      class="err"
      title="以下引用指向的内容已下线或删除，发布前请移除或替换："
      :description="missingRefs.join('、')"
      show-icon
    />

    <!-- 首屏 -->
    <el-card shadow="never" class="block">
      <template #header>
        <div class="block__head">
          <span>首屏（必显，不可整块关闭）</span>
          <el-button size="small" @click="addHero">添加一条</el-button>
        </div>
      </template>
      <p class="hint">最多 5 条，启用中的条目正式上线时必须填写标题与桌面图片；2 条以上前台提供手动切换。</p>

      <div v-for="(item, index) in form.hero.items" :key="item.key ?? `hero-${index}`" class="sub">
        <div class="sub__head">
          <el-switch v-model="item.enabled" active-text="启用" />
          <span class="sub__index">第 {{ index + 1 }} 条</span>
          <div class="sub__actions">
            <el-button link :disabled="index === 0" @click="move(form.hero.items, index, -1)">上移</el-button>
            <el-button link :disabled="index === form.hero.items.length - 1" @click="move(form.hero.items, index, 1)">下移</el-button>
            <el-button link type="danger" @click="form.hero.items.splice(index, 1)">删除</el-button>
          </div>
        </div>

        <el-form label-width="96px" size="small">
          <el-form-item label="标题" required>
            <el-input v-model="item.title" maxlength="40" show-word-limit placeholder="独立文字层，可换行" />
          </el-form-item>
          <el-form-item label="副标题">
            <el-input v-model="item.subtitle" maxlength="100" show-word-limit />
          </el-form-item>
          <el-form-item label="桌面媒体（图片或视频）" required>
            <div class="media">
              <video
                v-if="item.desktopMediaId && isVideoMedia(item.desktopMediaId)"
                :src="thumb(item.desktopMediaId)"
                class="media__preview"
                muted
                playsinline
                preload="metadata"
              ></video>
              <img
                v-else-if="item.desktopMediaId"
                :src="thumb(item.desktopMediaId)"
                alt="桌面首屏素材"
                class="media__preview"
              />
              <span v-else class="media__empty">未选择（建议 1920×900，视频 16:9）</span>
              <el-button size="small" @click="openPicker({ kind: 'heroDesktop', index })">选择</el-button>
              <el-button v-if="item.desktopMediaId" size="small" link type="danger" @click="item.desktopMediaId = undefined">清除</el-button>
            </div>
          </el-form-item>
          <el-form-item label="手机媒体">
            <div class="media">
              <video
                v-if="item.mobileMediaId && isVideoMedia(item.mobileMediaId)"
                :src="thumb(item.mobileMediaId)"
                class="media__preview"
                muted
                playsinline
                preload="metadata"
              ></video>
              <img
                v-else-if="item.mobileMediaId"
                :src="thumb(item.mobileMediaId)"
                alt="手机首屏素材"
                class="media__preview"
              />
              <span v-else class="media__empty">未选择时前台回退桌面素材（建议 750×1000）</span>
              <el-button size="small" @click="openPicker({ kind: 'heroMobile', index })">选择</el-button>
              <el-button v-if="item.mobileMediaId" size="small" link type="danger" @click="item.mobileMediaId = undefined">清除</el-button>
            </div>
          </el-form-item>
          <el-form-item label="图片替代文字">
            <el-input v-model="item.imageAlt" maxlength="120" placeholder="不填时使用首屏标题" />
          </el-form-item>
          <el-form-item label="按钮文案">
            <el-input v-model="item.buttonText" maxlength="12" placeholder="不配置按钮则留空" style="width: 200px" />
          </el-form-item>
          <el-form-item label="按钮链接">
            <LinkEditor v-model="item.link" :allow-none="true" />
          </el-form-item>
        </el-form>
      </div>

      <el-empty v-if="form.hero.items.length === 0" description="还没有首屏，发布前至少需要 1 条" />
    </el-card>

    <!-- 企业指标 -->
    <el-card shadow="never" class="block">
      <template #header>
        <div class="block__head">
          <span>
            <el-switch v-model="form.metrics.enabled" class="switch" />
            企业指标
          </span>
          <el-button size="small" @click="addMetric">添加指标</el-button>
        </div>
      </template>
      <p class="hint">最多 8 条。数值按字符串保存，支持「1000」「10万」「99.9%」，前台不参与计算。</p>
      <el-form label-width="96px" size="small">
        <el-form-item label="区块标题"><el-input v-model="form.metrics.title" maxlength="40" /></el-form-item>
        <el-form-item label="区块说明"><el-input v-model="form.metrics.subtitle" maxlength="200" /></el-form-item>
      </el-form>

      <div v-for="(item, index) in form.metrics.items" :key="item.key ?? `metric-${index}`" class="inline">
        <el-input v-model="item.displayValue" maxlength="16" placeholder="1000" style="width: 110px" />
        <el-input v-model="item.unit" maxlength="8" placeholder="+" style="width: 70px" />
        <el-input v-model="item.label" maxlength="20" placeholder="服务客户" style="width: 140px" />
        <el-input v-model="item.description" maxlength="80" placeholder="说明（可选）" style="width: 220px" />
        <el-button link :disabled="index === 0" @click="move(form.metrics.items, index, -1)">上移</el-button>
        <el-button link :disabled="index === form.metrics.items.length - 1" @click="move(form.metrics.items, index, 1)">下移</el-button>
        <el-button link type="danger" @click="form.metrics.items.splice(index, 1)">删除</el-button>
      </div>
    </el-card>

    <!-- 产品服务 -->
    <el-card shadow="never" class="block">
      <template #header>
        <div class="block__head">
          <span>
            <el-switch v-model="form.services.enabled" class="switch" />
            产品服务
          </span>
          <el-button size="small" @click="addGroup">添加分组</el-button>
        </div>
      </template>
      <p class="hint">
        最多 3 组，每组只能全部是产品或全部是解决方案，只能选择已发布内容。卡片标题、摘要与封面取自内容最新发布版，这里不重复录入。
      </p>
      <el-form label-width="96px" size="small">
        <el-form-item label="区块标题"><el-input v-model="form.services.title" maxlength="40" /></el-form-item>
        <el-form-item label="区块说明"><el-input v-model="form.services.subtitle" maxlength="200" /></el-form-item>
      </el-form>

      <div v-for="(group, index) in form.services.groups" :key="group.key ?? `group-${index}`" class="sub">
        <div class="sub__head">
          <span class="sub__index">分组 {{ index + 1 }}</span>
          <div class="sub__actions">
            <el-button link :disabled="index === 0" @click="move(form.services.groups, index, -1)">上移</el-button>
            <el-button link :disabled="index === form.services.groups.length - 1" @click="move(form.services.groups, index, 1)">下移</el-button>
            <el-button link type="danger" @click="form.services.groups.splice(index, 1)">删除</el-button>
          </div>
        </div>
        <el-form label-width="96px" size="small">
          <el-form-item label="分组标题" required>
            <el-input v-model="group.title" maxlength="30" />
          </el-form-item>
          <el-form-item label="分组说明">
            <el-input v-model="group.description" type="textarea" :rows="2" maxlength="200" show-word-limit />
          </el-form-item>
          <el-form-item label="内容类型">
            <el-select v-model="group.contentType" style="width: 160px" @change="group.contentIds = []">
              <el-option label="产品" value="PRODUCT" />
              <el-option label="解决方案" value="SOLUTION" />
            </el-select>
          </el-form-item>
          <el-form-item label="分组配图">
            <div class="media">
              <img v-if="group.imageMediaId" :src="thumb(group.imageMediaId)" alt="分组配图" />
              <span v-else class="media__empty">可选</span>
              <el-button size="small" @click="openPicker({ kind: 'group', index })">选择</el-button>
              <el-button v-if="group.imageMediaId" size="small" link type="danger" @click="group.imageMediaId = undefined">清除</el-button>
            </div>
          </el-form-item>
          <el-form-item label="组内内容">
            <ContentPicker v-model="group.contentIds" :content-type="group.contentType" :max="6" />
          </el-form-item>
          <el-form-item label="更多链接">
            <LinkEditor v-model="group.moreLink" :allow-none="true" />
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 客户案例 -->
    <el-card shadow="never" class="block">
      <template #header>
        <div class="block__head">
          <span>
            <el-switch v-model="form.cases.enabled" class="switch" />
            客户案例
          </span>
        </div>
      </template>
      <el-form label-width="96px" size="small">
        <el-form-item label="区块标题"><el-input v-model="form.cases.title" maxlength="40" /></el-form-item>
        <el-form-item label="区块说明"><el-input v-model="form.cases.subtitle" maxlength="200" /></el-form-item>
        <el-form-item label="推荐案例">
          <ContentPicker v-model="form.cases.contentIds" content-type="CASE" :max="6" />
        </el-form-item>
      </el-form>
      <p class="hint">最多 6 个，按所选顺序展示；「更多案例」固定指向 /cases。</p>
    </el-card>

    <!-- 客户标识 -->
    <el-card shadow="never" class="block">
      <template #header>
        <div class="block__head">
          <span>
            <el-switch v-model="form.customers.enabled" class="switch" />
            客户标识
          </span>
        </div>
      </template>
      <el-form label-width="96px" size="small">
        <el-form-item label="区块标题"><el-input v-model="form.customers.title" maxlength="40" /></el-form-item>
        <el-form-item label="区块说明"><el-input v-model="form.customers.subtitle" maxlength="200" /></el-form-item>
        <el-form-item label="客户">
          <ContentPicker v-model="form.customers.contentIds" content-type="CUSTOMER" :max="30" />
        </el-form-item>
      </el-form>
      <p class="hint">最多 30 个，按所选顺序显示；有合法客户网址时才可点击。</p>
    </el-card>

    <!-- 新闻 -->
    <el-card shadow="never" class="block">
      <template #header>
        <div class="block__head">
          <span>
            <el-switch v-model="form.news.enabled" class="switch" />
            新闻动态
          </span>
        </div>
      </template>
      <el-form label-width="96px" size="small">
        <el-form-item label="区块标题"><el-input v-model="form.news.title" maxlength="40" /></el-form-item>
        <el-form-item label="区块说明"><el-input v-model="form.news.subtitle" maxlength="200" /></el-form-item>
        <el-form-item label="取数模式">
          <el-radio-group v-model="form.news.mode">
            <el-radio value="SELECTED">手选（按所选顺序）</el-radio>
            <el-radio value="LATEST">最新（按展示时间倒序）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="显示条数">
          <el-input-number v-model="form.news.limit" :min="1" :max="6" />
        </el-form-item>
        <el-form-item v-if="form.news.mode === 'SELECTED'" label="手选新闻">
          <ContentPicker v-model="form.news.contentIds" content-type="NEWS" :max="6" />
        </el-form-item>
      </el-form>
      <p class="hint">
        手选模式按所选顺序取前 {{ form.news.limit }} 条，不足时不自动补位；最新模式忽略手选，按展示日期倒序。
      </p>
    </el-card>

    <!-- 咨询区 -->
    <el-card shadow="never" class="block">
      <template #header>咨询区</template>
      <el-form label-width="96px" size="small">
        <el-form-item label="标题" required><el-input v-model="form.consultation.title" maxlength="40" /></el-form-item>
        <el-form-item label="副标题"><el-input v-model="form.consultation.subtitle" maxlength="200" /></el-form-item>
        <el-form-item label="按钮文案" required><el-input v-model="form.consultation.buttonText" maxlength="12" /></el-form-item>
        <el-form-item label="按钮动作">
          <LinkEditor v-model="form.consultation.link" :allow-none="false" :allow-consult="true" />
        </el-form-item>
      </el-form>
      <p class="hint">只允许咨询动作或联系页；联系页表单关闭时，前台自动退到「联系我们」入口。</p>
    </el-card>

    <MediaPicker v-model="pickerOpen" :selected-id="null" :kind="pickerKind" @select="onPick" />

    <template #tips>
      <p>首页引用的内容必须是已发布状态。内容改名后无需重发首页，前台始终显示最新发布版。</p>
      <p>内容下线后首页会自动剔除该卡片；整组为空时该组隐藏。</p>
    </template>
  </ConfigPanel>
</template>

<style scoped>
.err {
  margin-bottom: 12px;
}

.block {
  margin-bottom: 16px;
}

.block__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.switch {
  margin-right: 8px;
}

.hint {
  margin: 0 0 14px;
  font-size: 12px;
  color: var(--a-text-muted);
  line-height: 1.7;
}

.sub {
  border: 1px solid var(--a-border);
  border-radius: 8px;
  padding: 14px;
  margin-bottom: 14px;
  background: #fbfcfe;
}

.sub__head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.sub__index {
  font-size: 13px;
  font-weight: 500;
}

.sub__actions {
  margin-left: auto;
}

.inline {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding: 6px 0;
}

.media {
  display: flex;
  align-items: center;
  gap: 10px;
}

.media img,
.media__preview {
  width: 96px;
  height: 54px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid var(--a-border);
  background: #0b0b10;
}

.media__empty {
  font-size: 12px;
  color: var(--a-text-muted);
}
</style>
