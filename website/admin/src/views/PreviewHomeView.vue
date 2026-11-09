<script setup lang="ts">
/**
 * 全站实景交互预演与全局可视化编辑台 (Visual Live Studio)
 *
 * 核心能力：
 * 1. 全局跨模块双向同步：同时加载并可视化编辑 SITE（全站设置/页脚）、NAVIGATION（顶部导航与5列全景菜单）、
 *    HOME（首屏、指标、服务分组、案例、客户墙、新闻、咨询区及模块摆放顺序 sectionOrder）、CONTACT（热线/邮箱/地址/表单开关）。
 * 2. 画布级所见即所得直改：文字直接点击修改、图片/视频点击唤起 MediaPicker 即时替换、按钮/导航链接点击唤起 LinkEditor、
 *    关联产品/方案/案例/客户/新闻点击唤起 ContentPicker。
 * 3. 模块自由摆放与增删：支持在画布悬浮条或左侧大纲树中上下移动模块顺序、开关模块显隐、增删改查各模块子条目。
 * 4. 实用增强工具：桌面(100%)/平板(768px)/手机(390px)多视口实时切换、30步 Undo/Redo 撤销重做栈、
 *    「交互编辑态 / 纯净预演态 / 线上已发布版」三态切换、发布前智能合规体检抽屉、一键全局保存草稿与一键全局发布。
 */
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import ContentPicker from '@/components/ContentPicker.vue'
import LinkEditor, { type LinkValue } from '@/components/LinkEditor.vue'
import MediaPicker from '@/components/MediaPicker.vue'
import { getMedia, listContents, type ContentView, type MediaItem } from '@/api/content'
import { ApiError } from '@/api/http'
import { getConfig, publishConfig, saveConfigDraft, type ConfigKey, type ConfigView } from '@/api/site'
import { useAuth } from '@/stores/auth'

const router = useRouter()
const { current } = useAuth()
const isAdmin = computed(() => current.value?.role === 'ADMIN')

// ==================== 类型定义 ====================
interface FooterLink {
  key?: string
  label: string
  enabled: boolean
  sortOrder: number
  link: LinkValue | null
}
interface FooterGroup {
  title: string
  links: FooterLink[]
}
interface SiteDraft {
  siteName: string
  slogan: string
  copyrightText: string
  icpText: string
  icpUrl: string
  policeText: string
  policeUrl: string
  logoMediaId?: number
  logoAlt: string
  faviconMediaId?: number
  defaultSeo: { title: string; description: string }
  footerGroups: FooterGroup[]
}

interface MegaSubItem {
  label: string
  path: string
  tag?: string
  isVideo?: boolean
  videoUrl?: string
  videoTitle?: string
}
interface MegaColumn {
  title: string
  items: MegaSubItem[]
}
interface NavNode {
  key?: string
  label: string
  enabled: boolean
  sortOrder: number
  link: LinkValue | null
  children?: NavNode[]
}
interface NavigationDraft {
  items: NavNode[]
  megaColumns?: MegaColumn[]
  megaCta?: { text: string; linkText: string }
}

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
  titleColor?: string
  subtitleColor?: string
  kickerColor?: string
  buttonBgColor?: string
  buttonTextColor?: string
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
export type HomeSectionKey = 'metrics' | 'services' | 'cases' | 'customers' | 'news' | 'consultation'
interface HomeDraft {
  sectionOrder: HomeSectionKey[]
  hero: { items: HeroItem[] }
  metrics: BlockShape & { items: MetricItem[] }
  services: BlockShape & { groups: ServiceGroup[] }
  cases: BlockShape & { contentIds: number[] }
  customers: BlockShape & { contentIds: number[] }
  news: BlockShape & { mode: string; limit: number; contentIds: number[] }
  consultation: { title: string; subtitle: string; buttonText: string; link: LinkValue | null }
}

interface ContactDraft {
  title: string
  description: string
  successText: string
  formEnabled: boolean
  phone: string
  email: string
  address: string
  hours: string
}

const DEFAULT_SECTION_ORDER: HomeSectionKey[] = [
  'metrics',
  'services',
  'cases',
  'customers',
  'news',
  'consultation',
]

const SECTION_META: Record<HomeSectionKey, { label: string; icon: string; formPath: string }> = {
  metrics: { label: '企业实力指标', icon: '📊', formPath: '/home' },
  services: { label: '产品与解决方案', icon: '📦', formPath: '/home' },
  cases: { label: '标杆客户案例', icon: '🏆', formPath: '/home' },
  customers: { label: '客户标识 Logo 墙', icon: '🤝', formPath: '/home' },
  news: { label: '新闻动态资讯', icon: '📰', formPath: '/home' },
  consultation: { label: '底部咨询行动区', icon: '💬', formPath: '/home' },
}

// ==================== 全局状态与配置视图 ====================
const loading = ref(true)
const savingAll = ref(false)
const publishingAll = ref(false)

const studioMode = ref<'edit' | 'preview' | 'published'>('edit')
const viewport = ref<'desktop' | 'tablet' | 'mobile'>('desktop')
const showLeftOutline = ref(true)
const showDisabledBlocks = ref(true)
const healthDrawerOpen = ref(false)

const heroIndex = ref(0)
const activeServiceGroup = ref(0)
const openMegaPreview = ref(false)
const selectedSection = ref<'header' | 'hero' | HomeSectionKey | 'footer'>('hero')

const configViews = reactive<{
  SITE: ConfigView<SiteDraft> | null
  NAVIGATION: ConfigView<NavigationDraft> | null
  HOME: ConfigView<HomeDraft> | null
  CONTACT: ConfigView<ContactDraft> | null
}>({
  SITE: null,
  NAVIGATION: null,
  HOME: null,
  CONTACT: null,
})

// 实时可编辑草稿副本
const drafts = reactive<{
  SITE: SiteDraft
  NAVIGATION: NavigationDraft
  HOME: HomeDraft
  CONTACT: ContactDraft
}>({
  SITE: {
    siteName: 'G7易流',
    slogan: '领先的物联网与数字化软件服务商',
    copyrightText: '© 2026 示例科技有限公司',
    icpText: '',
    icpUrl: '',
    policeText: '',
    policeUrl: '',
    logoMediaId: undefined,
    logoAlt: '',
    faviconMediaId: undefined,
    defaultSeo: { title: '企业官网', description: '面向企业的物联网与数字化平台' },
    footerGroups: [],
  },
  NAVIGATION: {
    items: [],
    megaColumns: [],
    megaCta: { text: '需要专业场景建议？', linkText: '联系技术专家' },
  },
  HOME: {
    sectionOrder: [...DEFAULT_SECTION_ORDER],
    hero: { items: [] },
    metrics: { enabled: true, title: '企业实力', subtitle: '', items: [] },
    services: { enabled: true, title: '产品与解决方案', subtitle: '', groups: [] },
    cases: { enabled: true, title: '客户案例', subtitle: '', contentIds: [] },
    customers: { enabled: true, title: '合作客户', subtitle: '', contentIds: [] },
    news: { enabled: true, title: '新闻动态', subtitle: '', mode: 'SELECTED', limit: 3, contentIds: [] },
    consultation: { title: '了解适合您的服务', subtitle: '', buttonText: '立即咨询', link: { type: 'CONSULT' } },
  },
  CONTACT: {
    title: '联系我们',
    description: '',
    successText: '已收到您的咨询，我们会尽快与您联系。',
    formEnabled: true,
    phone: '400-888-6688',
    email: 'connect@example.com',
    address: '北京市海淀区中关村南大街1号',
    hours: '周一至周日 09:00 - 18:00',
  },
})

// 初始快照基线（用于判断哪个模块被修改过）
const baselineJson = reactive<Record<ConfigKey, string>>({
  SITE: '',
  NAVIGATION: '',
  HOME: '',
  ABOUT: '',
  CONTACT: '',
  PRIVACY: '',
})

const dirtyKeys = computed<ConfigKey[]>(() => {
  const keys: ConfigKey[] = []
  ;(['SITE', 'NAVIGATION', 'HOME', 'CONTACT'] as ConfigKey[]).forEach((k) => {
    const currentStr = JSON.stringify(drafts[k as 'SITE' | 'NAVIGATION' | 'HOME' | 'CONTACT'])
    if (baselineJson[k] && currentStr !== baselineJson[k]) {
      keys.push(k)
    }
  })
  return keys
})

const unpublishedKeys = computed<ConfigKey[]>(() => {
  const keys: ConfigKey[] = []
  ;(['SITE', 'NAVIGATION', 'HOME', 'CONTACT'] as const).forEach((k) => {
    if (configViews[k]?.hasUnpublishedChanges || dirtyKeys.value.includes(k)) {
      keys.push(k)
    }
  })
  return keys
})

// ==================== Undo / Redo 历史栈 ====================
const historyStack = ref<string[]>([])
const historyPointer = ref(-1)
let suppressHistory = false

function recordSnapshot(label = '') {
  if (suppressHistory) return
  const snap = JSON.stringify(drafts)
  if (historyPointer.value >= 0 && historyStack.value[historyPointer.value] === snap) return
  historyStack.value = historyStack.value.slice(0, historyPointer.value + 1)
  historyStack.value.push(snap)
  if (historyStack.value.length > 30) {
    historyStack.value.shift()
  }
  historyPointer.value = historyStack.value.length - 1
  if (label) {
    // 轻量提示
  }
}

const canUndo = computed(() => historyPointer.value > 0)
const canRedo = computed(() => historyPointer.value < historyStack.value.length - 1)

function undo() {
  if (!canUndo.value) return
  suppressHistory = true
  historyPointer.value -= 1
  const parsed = JSON.parse(historyStack.value[historyPointer.value])
  Object.assign(drafts.SITE, parsed.SITE)
  Object.assign(drafts.NAVIGATION, parsed.NAVIGATION)
  Object.assign(drafts.HOME, parsed.HOME)
  Object.assign(drafts.CONTACT, parsed.CONTACT)
  suppressHistory = false
  ElMessage.info('已撤销上一步修改')
}

function redo() {
  if (!canRedo.value) return
  suppressHistory = true
  historyPointer.value += 1
  const parsed = JSON.parse(historyStack.value[historyPointer.value])
  Object.assign(drafts.SITE, parsed.SITE)
  Object.assign(drafts.NAVIGATION, parsed.NAVIGATION)
  Object.assign(drafts.HOME, parsed.HOME)
  Object.assign(drafts.CONTACT, parsed.CONTACT)
  suppressHistory = false
  ElMessage.info('已重做修改')
}

// ==================== 内容与媒体缓存 ====================
const contentCatalog = reactive<Record<string, ContentView>>({})
const latestNewsList = ref<ContentView[]>([])
const mediaCache = reactive<Record<number, MediaItem>>({})

async function loadCatalogAndMedia() {
  try {
    const types = ['PRODUCT', 'SOLUTION', 'CASE', 'NEWS', 'CUSTOMER']
    await Promise.all(
      types.map(async (t) => {
        const res = await listContents({
          contentType: t,
          publicationStatus: 'PUBLISHED',
          page: 1,
          pageSize: 50,
        })
        for (const item of res.items) {
          contentCatalog[String(item.id)] = item
        }
        if (t === 'NEWS') {
          latestNewsList.value = res.items
        }
      }),
    )
  } catch {
    // 忽略单个列表失败
  }
  await syncMediaCache()
}

async function syncMediaCache() {
  const ids = new Set<number>()
  if (drafts.SITE.logoMediaId) ids.add(Number(drafts.SITE.logoMediaId))
  if (drafts.SITE.faviconMediaId) ids.add(Number(drafts.SITE.faviconMediaId))
  for (const h of drafts.HOME.hero.items) {
    if (h.desktopMediaId) ids.add(Number(h.desktopMediaId))
    if (h.mobileMediaId) ids.add(Number(h.mobileMediaId))
  }
  for (const g of drafts.HOME.services.groups) {
    if (g.imageMediaId) ids.add(Number(g.imageMediaId))
  }
  for (const c of Object.values(contentCatalog)) {
    if (c.coverMediaId) ids.add(Number(c.coverMediaId))
  }
  await Promise.all(
    Array.from(ids)
      .filter((id) => id > 0 && !mediaCache[id])
      .map(async (id) => {
        try {
          mediaCache[id] = await getMedia(id)
        } catch {
          // 忽略已删除素材
        }
      }),
  )
}

function mediaUrl(id?: number | string | null): string {
  if (!id) return ''
  return `/api/admin/media/${id}/file`
}

function isVideoMedia(id?: number | string | null): boolean {
  if (!id) return false
  return mediaCache[Number(id)]?.mediaType === 'VIDEO'
}

// ==================== 数据加载与归一化 ====================
function normalizeSectionOrder(raw?: unknown): HomeSectionKey[] {
  const result: HomeSectionKey[] = []
  if (Array.isArray(raw)) {
    for (const item of raw) {
      const k = String(item) as HomeSectionKey
      if (DEFAULT_SECTION_ORDER.includes(k) && !result.includes(k)) {
        result.push(k)
      }
    }
  }
  for (const k of DEFAULT_SECTION_ORDER) {
    if (!result.includes(k)) result.push(k)
  }
  return result
}

function applySourceData(usePublished = false) {
  const siteSrc = (usePublished ? configViews.SITE?.published : configViews.SITE?.draft) as Partial<SiteDraft> | null
  if (siteSrc) {
    drafts.SITE = {
      siteName: siteSrc.siteName ?? 'G7易流',
      slogan: siteSrc.slogan ?? '领先的物联网与数字化软件服务商',
      copyrightText: siteSrc.copyrightText ?? '© 2026 示例科技有限公司',
      icpText: siteSrc.icpText ?? '',
      icpUrl: siteSrc.icpUrl ?? '',
      policeText: siteSrc.policeText ?? '',
      policeUrl: siteSrc.policeUrl ?? '',
      logoMediaId: siteSrc.logoMediaId ? Number(siteSrc.logoMediaId) : undefined,
      logoAlt: siteSrc.logoAlt ?? '',
      faviconMediaId: siteSrc.faviconMediaId ? Number(siteSrc.faviconMediaId) : undefined,
      defaultSeo: {
        title: siteSrc.defaultSeo?.title ?? '',
        description: siteSrc.defaultSeo?.description ?? '',
      },
      footerGroups: (siteSrc.footerGroups ?? []).map((g) => ({
        title: g.title ?? '',
        links: (g.links ?? []).map((l) => ({
          key: l.key,
          label: l.label ?? '',
          enabled: l.enabled ?? true,
          sortOrder: l.sortOrder ?? 100,
          link: l.link ?? { type: 'PAGE', pageCode: 'HOME' },
        })),
      })),
    }
  }

  const navSrc = (usePublished ? configViews.NAVIGATION?.published : configViews.NAVIGATION?.draft) as Partial<NavigationDraft> | null
  if (navSrc) {
    drafts.NAVIGATION = {
      items: (navSrc.items ?? []).map((n) => ({
        key: n.key,
        label: n.label ?? '',
        enabled: n.enabled ?? true,
        sortOrder: n.sortOrder ?? 100,
        link: n.link ?? { type: 'NONE' },
        children: (n.children ?? []).map((c) => ({
          key: c.key,
          label: c.label ?? '',
          enabled: c.enabled ?? true,
          sortOrder: c.sortOrder ?? 100,
          link: c.link ?? { type: 'PAGE', pageCode: 'HOME' },
        })),
      })),
      megaColumns: (navSrc.megaColumns ?? []).map((col) => ({
        title: col.title ?? '',
        items: (col.items ?? []).map((sub) => ({
          label: sub.label ?? '',
          path: sub.path ?? '/products',
          tag: sub.tag ?? '',
          isVideo: Boolean(sub.isVideo),
          videoUrl: sub.videoUrl ?? '',
          videoTitle: sub.videoTitle ?? '',
        })),
      })),
      megaCta: {
        text: navSrc.megaCta?.text ?? '需要专业场景建议？',
        linkText: navSrc.megaCta?.linkText ?? '联系技术专家',
      },
    }
    if (drafts.NAVIGATION.items.length === 0) {
      drafts.NAVIGATION.items = [
        { label: '首页', enabled: true, sortOrder: 10, link: { type: 'PAGE', pageCode: 'HOME' }, children: [] },
        { label: '产品及解决方案', enabled: true, sortOrder: 20, link: { type: 'PAGE', pageCode: 'PRODUCTS' }, children: [] },
        { label: '客户案例', enabled: true, sortOrder: 30, link: { type: 'PAGE', pageCode: 'CASES' }, children: [] },
        { label: '新闻中心', enabled: true, sortOrder: 40, link: { type: 'PAGE', pageCode: 'NEWS' }, children: [] },
        { label: '关于我们', enabled: true, sortOrder: 50, link: { type: 'PAGE', pageCode: 'ABOUT' }, children: [] },
      ]
    }
  }

  const homeSrc = (usePublished ? configViews.HOME?.published : configViews.HOME?.draft) as Partial<HomeDraft> | null
  if (homeSrc) {
    drafts.HOME = {
      sectionOrder: normalizeSectionOrder((homeSrc as Record<string, unknown>).sectionOrder),
      hero: {
        items: (homeSrc.hero?.items ?? []).map((h) => ({
          key: h.key,
          enabled: h.enabled ?? true,
          sortOrder: h.sortOrder ?? 100,
          title: h.title ?? '',
          subtitle: h.subtitle ?? '',
          desktopMediaId: h.desktopMediaId ? Number(h.desktopMediaId) : undefined,
          mobileMediaId: h.mobileMediaId ? Number(h.mobileMediaId) : undefined,
          imageAlt: h.imageAlt ?? '',
          buttonText: h.buttonText ?? '',
          link: h.link ?? { type: 'NONE' },
          titleColor: h.titleColor,
          subtitleColor: h.subtitleColor,
          kickerColor: h.kickerColor,
          buttonBgColor: h.buttonBgColor,
          buttonTextColor: h.buttonTextColor,
        })),
      },
      metrics: {
        enabled: homeSrc.metrics?.enabled ?? true,
        title: homeSrc.metrics?.title ?? '企业实力',
        subtitle: homeSrc.metrics?.subtitle ?? '',
        items: (homeSrc.metrics?.items ?? []).map((m) => ({
          key: m.key,
          sortOrder: m.sortOrder ?? 100,
          displayValue: m.displayValue ?? '',
          unit: m.unit ?? '',
          label: m.label ?? '',
          description: m.description ?? '',
        })),
      },
      services: {
        enabled: homeSrc.services?.enabled ?? true,
        title: homeSrc.services?.title ?? '产品与解决方案',
        subtitle: homeSrc.services?.subtitle ?? '',
        groups: (homeSrc.services?.groups ?? []).map((g) => ({
          key: g.key,
          sortOrder: g.sortOrder ?? 100,
          title: g.title ?? '',
          description: g.description ?? '',
          imageMediaId: g.imageMediaId ? Number(g.imageMediaId) : undefined,
          contentType: g.contentType ?? 'PRODUCT',
          contentIds: (g.contentIds ?? []).map(Number),
          moreLink: g.moreLink ?? { type: 'PAGE', pageCode: 'PRODUCTS' },
        })),
      },
      cases: {
        enabled: homeSrc.cases?.enabled ?? true,
        title: homeSrc.cases?.title ?? '客户案例',
        subtitle: homeSrc.cases?.subtitle ?? '',
        contentIds: (homeSrc.cases?.contentIds ?? []).map(Number),
      },
      customers: {
        enabled: homeSrc.customers?.enabled ?? true,
        title: homeSrc.customers?.title ?? '合作客户',
        subtitle: homeSrc.customers?.subtitle ?? '',
        contentIds: (homeSrc.customers?.contentIds ?? []).map(Number),
      },
      news: {
        enabled: homeSrc.news?.enabled ?? true,
        title: homeSrc.news?.title ?? '新闻动态',
        subtitle: homeSrc.news?.subtitle ?? '',
        mode: homeSrc.news?.mode ?? 'SELECTED',
        limit: homeSrc.news?.limit ?? 3,
        contentIds: (homeSrc.news?.contentIds ?? []).map(Number),
      },
      consultation: {
        title: homeSrc.consultation?.title ?? '了解适合您的服务',
        subtitle: homeSrc.consultation?.subtitle ?? '',
        buttonText: homeSrc.consultation?.buttonText ?? '立即咨询',
        link: homeSrc.consultation?.link ?? { type: 'CONSULT' },
      },
    }
  }

  const contactSrc = (usePublished ? configViews.CONTACT?.published : configViews.CONTACT?.draft) as Partial<ContactDraft> | null
  if (contactSrc) {
    drafts.CONTACT = {
      title: contactSrc.title || '联系我们',
      description: contactSrc.description ?? '',
      successText: contactSrc.successText ?? '已收到您的咨询，我们会尽快与您联系。',
      formEnabled: contactSrc.formEnabled ?? true,
      phone: contactSrc.phone ?? '',
      email: contactSrc.email ?? '',
      address: contactSrc.address ?? '',
      hours: contactSrc.hours ?? '',
    }
  }
}

async function loadAll() {
  loading.value = true
  try {
    const [site, nav, home, contact] = await Promise.all([
      getConfig<SiteDraft>('SITE'),
      getConfig<NavigationDraft>('NAVIGATION'),
      getConfig<HomeDraft>('HOME'),
      getConfig<ContactDraft>('CONTACT'),
    ])
    configViews.SITE = site
    configViews.NAVIGATION = nav
    configViews.HOME = home
    configViews.CONTACT = contact

    applySourceData(false)

    baselineJson.SITE = JSON.stringify(drafts.SITE)
    baselineJson.NAVIGATION = JSON.stringify(drafts.NAVIGATION)
    baselineJson.HOME = JSON.stringify(drafts.HOME)
    baselineJson.CONTACT = JSON.stringify(drafts.CONTACT)

    historyStack.value = [JSON.stringify(drafts)]
    historyPointer.value = 0

    await loadCatalogAndMedia()
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '加载全局配置失败')
  } finally {
    loading.value = false
  }
}

function switchStudioMode(mode: 'edit' | 'preview' | 'published') {
  if (mode === 'published') {
    applySourceData(true)
  } else if (studioMode.value === 'published') {
    applySourceData(false)
  }
  studioMode.value = mode
}

// ==================== 全局保存与一键发布 ====================
async function saveAllDrafts(silent = false): Promise<boolean> {
  const targets: Array<'SITE' | 'NAVIGATION' | 'HOME' | 'CONTACT'> =
    dirtyKeys.value.length > 0
      ? (dirtyKeys.value as Array<'SITE' | 'NAVIGATION' | 'HOME' | 'CONTACT'>)
      : ['HOME']

  savingAll.value = true
  try {
    for (const key of targets) {
      const view = configViews[key]
      if (!view) continue
      const updated = await saveConfigDraft(
        key,
        JSON.parse(JSON.stringify(drafts[key])),
        view.lockVersion,
      )
      ;(configViews as Record<string, unknown>)[key] = updated
      baselineJson[key] = JSON.stringify(drafts[key])
    }
    if (!silent) {
      ElMessage.success(`已同步保存 ${targets.join(' / ')} 草稿至后台各管理模块！`)
    }
    return true
  } catch (error) {
    if (error instanceof ApiError) {
      const detail = error.fieldErrors?.map((f) => `${f.field}: ${f.message}`).join('；')
      ElMessage.error(detail ? `${error.message}（${detail}）` : error.message)
    } else {
      ElMessage.error('保存草稿失败，请检查输入项')
    }
    return false
  } finally {
    savingAll.value = false
  }
}

async function saveAndPublishAll() {
  if (!isAdmin.value) {
    ElMessage.warning('当前为编辑角色，仅管理员可执行发布')
    return
  }
  if (healthIssues.value.some((i) => i.level === 'error')) {
    healthDrawerOpen.value = true
    ElMessage.warning('存在阻塞发布的必填项缺失，请先在右侧体检面板修正')
    return
  }
  try {
    await ElMessageBox.confirm(
      `即将把当前可视化编排的全局改动（${unpublishedKeys.value.join('、') || 'HOME'}）保存并正式发布到线上官网，确认继续？`,
      '全局保存并发布上线',
      { type: 'warning', confirmButtonText: '立即全局发布', cancelButtonText: '取消' },
    )
  } catch {
    return
  }

  publishingAll.value = true
  try {
    if (dirtyKeys.value.length > 0) {
      const ok = await saveAllDrafts(true)
      if (!ok) return
    }
    const pubTargets: Array<'SITE' | 'NAVIGATION' | 'HOME' | 'CONTACT'> = [
      'SITE',
      'NAVIGATION',
      'CONTACT',
      'HOME',
    ]
    for (const key of pubTargets) {
      const v = configViews[key]
      if (v && v.hasUnpublishedChanges) {
        await publishConfig(key, v.lockVersion, v.draftVersion)
      }
    }
    ElMessage.success('🎉 全局配置已成功发布上线！官网前台已实时更新')
    await loadAll()
  } catch (error) {
    if (error instanceof ApiError) {
      const detail = error.fieldErrors?.map((f) => `${f.field}: ${f.message}`).join('；')
      ElMessage.error(detail ? `${error.message}（${detail}）` : error.message)
    } else {
      ElMessage.error('发布失败')
    }
  } finally {
    publishingAll.value = false
  }
}

// ==================== 模块排序、增删与快捷操作 ====================
function moveSection(key: HomeSectionKey, delta: number) {
  const list = drafts.HOME.sectionOrder
  const idx = list.indexOf(key)
  const target = idx + delta
  if (idx < 0 || target < 0 || target >= list.length) return
  const [item] = list.splice(idx, 1)
  list.splice(target, 0, item)
  recordSnapshot('调整模块顺序')
}

function moveItem<T extends { sortOrder?: number }>(list: T[], index: number, delta: number) {
  const target = index + delta
  if (target < 0 || target >= list.length) return
  if (list[index].sortOrder !== undefined && list[target].sortOrder !== undefined) {
    const tmp = list[index].sortOrder
    list[index].sortOrder = list[target].sortOrder
    list[target].sortOrder = tmp
  }
  const [item] = list.splice(index, 1)
  list.splice(target, 0, item)
  recordSnapshot('调整条目顺序')
}

const currentHero = computed<HeroItem | null>(() => {
  return drafts.HOME.hero.items[heroIndex.value] ?? drafts.HOME.hero.items[0] ?? null
})

function addHeroSlide() {
  if (drafts.HOME.hero.items.length >= 5) {
    ElMessage.warning('首屏轮播最多支持 5 条')
    return
  }
  const firstMediaId = drafts.HOME.hero.items[0]?.desktopMediaId
  drafts.HOME.hero.items.push({
    enabled: true,
    sortOrder: (drafts.HOME.hero.items.length + 1) * 10,
    title: '紫宝盒 · 物流行业全场景 AI 工具',
    subtitle: '聪明看见，有效沟通，覆盖 50+ 高危驾驶与作业场景',
    desktopMediaId: firstMediaId,
    mobileMediaId: undefined,
    imageAlt: '全场景AI工具',
    buttonText: '了解详情',
    link: { type: 'PAGE', pageCode: 'PRODUCTS' },
    titleColor: '#FFFFFF',
    subtitleColor: 'rgba(255, 255, 255, 0.92)',
    buttonBgColor: '#FFFFFF',
    buttonTextColor: '#1F2329',
  })
  heroIndex.value = drafts.HOME.hero.items.length - 1
  selectedSection.value = 'hero'
  recordSnapshot('新增首屏海报')
}

function removeHeroSlide(idx: number) {
  drafts.HOME.hero.items.splice(idx, 1)
  if (heroIndex.value >= drafts.HOME.hero.items.length) {
    heroIndex.value = Math.max(0, drafts.HOME.hero.items.length - 1)
  }
  recordSnapshot('删除首屏海报')
}

function addMetricItem() {
  if (drafts.HOME.metrics.items.length >= 8) {
    ElMessage.warning('企业指标最多配置 8 项')
    return
  }
  drafts.HOME.metrics.items.push({
    sortOrder: (drafts.HOME.metrics.items.length + 1) * 10,
    displayValue: '800W',
    unit: '+',
    label: 'IoT 设备连接',
    description: '实时在线追踪与风控守护',
  })
  selectedSection.value = 'metrics'
  recordSnapshot('新增企业指标')
}

function addServiceGroup() {
  if (drafts.HOME.services.groups.length >= 3) {
    ElMessage.warning('产品服务分组最多 3 组')
    return
  }
  const firstProduct = Object.values(contentCatalog).find((c) => c.contentType === 'PRODUCT')
  drafts.HOME.services.groups.push({
    sortOrder: (drafts.HOME.services.groups.length + 1) * 10,
    title: '新业务服务分组',
    description: '在此输入该业务分组的核心能力描述与适用场景说明。',
    contentType: 'PRODUCT',
    contentIds: firstProduct ? [Number(firstProduct.id)] : [],
    moreLink: { type: 'PAGE', pageCode: 'PRODUCTS' },
  })
  activeServiceGroup.value = drafts.HOME.services.groups.length - 1
  selectedSection.value = 'services'
  recordSnapshot('新增服务分组')
}

function addTopNav() {
  if (drafts.NAVIGATION.items.length >= 8) {
    ElMessage.warning('顶层导航最多 8 项')
    return
  }
  drafts.NAVIGATION.items.push({
    label: `新栏目${drafts.NAVIGATION.items.length + 1}`,
    enabled: true,
    sortOrder: (drafts.NAVIGATION.items.length + 1) * 10,
    link: { type: 'PAGE', pageCode: 'ABOUT' },
    children: [],
  })
  selectedSection.value = 'header'
  recordSnapshot('新增顶部导航')
}

function addFooterGroup() {
  if (drafts.SITE.footerGroups.length >= 4) {
    ElMessage.warning('页脚分组最多 4 组')
    return
  }
  drafts.SITE.footerGroups.push({
    title: `栏目分组${drafts.SITE.footerGroups.length + 1}`,
    links: [
      { label: '产品中心', enabled: true, sortOrder: 10, link: { type: 'PAGE', pageCode: 'PRODUCTS' } },
      { label: '联系我们', enabled: true, sortOrder: 20, link: { type: 'PAGE', pageCode: 'CONTACT' } },
    ],
  })
  selectedSection.value = 'footer'
  recordSnapshot('新增页脚分组')
}

// ==================== 媒体选择器弹窗绑定 ====================
type MediaPickContext =
  | { target: 'siteLogo' }
  | { target: 'heroDesktop'; index: number }
  | { target: 'heroMobile'; index: number }
  | { target: 'serviceGroup'; index: number }

const mediaPickerOpen = ref(false)
const mediaPickCtx = ref<MediaPickContext | null>(null)
const mediaPickerKind = computed<'IMAGE' | 'VIDEO' | 'ALL'>(() => {
  if (!mediaPickCtx.value) return 'ALL'
  if (mediaPickCtx.value.target === 'siteLogo' || mediaPickCtx.value.target === 'serviceGroup') {
    return 'IMAGE'
  }
  return 'ALL'
})

function triggerMediaPick(ctx: MediaPickContext) {
  mediaPickCtx.value = ctx
  mediaPickerOpen.value = true
}

function onMediaPicked(media: MediaItem) {
  mediaCache[Number(media.id)] = media
  const ctx = mediaPickCtx.value
  if (!ctx) return
  if (ctx.target === 'siteLogo') {
    drafts.SITE.logoMediaId = Number(media.id)
  } else if (ctx.target === 'heroDesktop') {
    drafts.HOME.hero.items[ctx.index].desktopMediaId = Number(media.id)
  } else if (ctx.target === 'heroMobile') {
    drafts.HOME.hero.items[ctx.index].mobileMediaId = Number(media.id)
  } else if (ctx.target === 'serviceGroup') {
    drafts.HOME.services.groups[ctx.index].imageMediaId = Number(media.id)
  }
  mediaPickerOpen.value = false
  recordSnapshot('更换图片/视频素材')
}

// ==================== 链接编辑器弹窗绑定 ====================
const linkDialogOpen = ref(false)
const linkDialogTitle = ref('编辑跳转链接')
const linkDialogAllowConsult = ref(false)
const linkDialogAllowNone = ref(true)
const tempLink = ref<LinkValue | null>(null)
let applyLinkCallback: ((val: LinkValue | null) => void) | null = null

function openLinkModal(options: {
  title: string
  current: LinkValue | null
  allowConsult?: boolean
  allowNone?: boolean
  onConfirm: (val: LinkValue | null) => void
}) {
  linkDialogTitle.value = options.title
  tempLink.value = options.current ? JSON.parse(JSON.stringify(options.current)) : { type: 'PAGE', pageCode: 'HOME' }
  linkDialogAllowConsult.value = Boolean(options.allowConsult)
  linkDialogAllowNone.value = options.allowNone ?? true
  applyLinkCallback = options.onConfirm
  linkDialogOpen.value = true
}

function confirmLinkModal() {
  if (applyLinkCallback) {
    applyLinkCallback(tempLink.value ? JSON.parse(JSON.stringify(tempLink.value)) : null)
    recordSnapshot('修改跳转链接')
  }
  linkDialogOpen.value = false
}

function describeLink(link?: LinkValue | null): string {
  if (!link || link.type === 'NONE') return '无链接'
  if (link.type === 'PAGE') return `页面:${link.pageCode || 'HOME'}`
  if (link.type === 'CONTENT') return `${link.contentType}#${link.contentId || '?'}`
  if (link.type === 'EXTERNAL') return `外链:${link.url || ''}`
  if (link.type === 'ANCHOR') return `锚点:#${link.anchorCode || ''}`
  if (link.type === 'CONSULT') return '唤起咨询弹窗'
  return link.type
}

// ==================== 内容卡片解析辅助 ====================
function resolveCards(ids: number[]): ContentView[] {
  return ids.map((id) => contentCatalog[String(id)]).filter((c): c is ContentView => Boolean(c))
}

const resolvedNews = computed<ContentView[]>(() => {
  const limit = drafts.HOME.news.limit || 3
  if (drafts.HOME.news.mode === 'LATEST') {
    return latestNewsList.value.slice(0, limit)
  }
  return resolveCards(drafts.HOME.news.contentIds).slice(0, limit)
})

// ==================== 发布前智能合规体检 ====================
interface HealthIssue {
  level: 'error' | 'warning'
  module: string
  message: string
  section: 'header' | 'hero' | HomeSectionKey | 'footer'
}

const healthIssues = computed<HealthIssue[]>(() => {
  const list: HealthIssue[] = []
  if (!drafts.SITE.siteName.trim()) {
    list.push({ level: 'error', module: '全站设置', message: '站点名称不能为空', section: 'header' })
  }
  if (!drafts.SITE.copyrightText.trim()) {
    list.push({ level: 'error', module: '全站设置', message: '页脚版权文案不能为空', section: 'footer' })
  }
  if (Boolean(drafts.SITE.icpText.trim()) !== Boolean(drafts.SITE.icpUrl.trim())) {
    list.push({ level: 'error', module: '全站设置', message: 'ICP 备案号与备案链接需成对填写', section: 'footer' })
  }
  const enabledHeroes = drafts.HOME.hero.items.filter((h) => h.enabled)
  if (enabledHeroes.length === 0) {
    list.push({ level: 'error', module: '首页首屏', message: '至少需要启用 1 条首屏轮播', section: 'hero' })
  }
  enabledHeroes.forEach((h, idx) => {
    if (!h.title.trim()) {
      list.push({ level: 'error', module: '首页首屏', message: `第 ${idx + 1} 条首屏缺少标题`, section: 'hero' })
    }
    if (!h.desktopMediaId) {
      list.push({ level: 'error', module: '首页首屏', message: `第 ${idx + 1} 条首屏未选择桌面图片或视频`, section: 'hero' })
    }
  })
  drafts.HOME.services.groups.forEach((g, idx) => {
    if (!g.title.trim()) {
      list.push({ level: 'error', module: '产品与服务', message: `分组 ${idx + 1} 缺少标题`, section: 'services' })
    }
    if (!g.contentIds || g.contentIds.length === 0) {
      list.push({ level: 'warning', module: '产品与服务', message: `分组「${g.title || idx + 1}」尚未选择关联内容`, section: 'services' })
    }
  })
  if (!drafts.CONTACT.phone.trim() && !drafts.CONTACT.email.trim()) {
    list.push({ level: 'error', module: '联系我们', message: '联系电话与邮箱至少需填写一项', section: 'footer' })
  }
  return list
})

function focusSection(sec: 'header' | 'hero' | HomeSectionKey | 'footer') {
  selectedSection.value = sec
  document.getElementById(`studio-sec-${sec}`)?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

// 键盘快捷键 Ctrl+Z / Ctrl+Y / Ctrl+S
function handleKeydown(e: KeyboardEvent) {
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'z') {
    if (e.shiftKey) {
      e.preventDefault()
      redo()
    } else {
      e.preventDefault()
      undo()
    }
  } else if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'y') {
    e.preventDefault()
    redo()
  } else if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 's') {
    e.preventDefault()
    saveAllDrafts()
  }
}

onMounted(() => {
  loadAll()
  window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <div v-loading="loading" class="studio">
    <!-- ==================== 顶部全局实景工作台控制栏 ==================== -->
    <header class="studio-bar">
      <div class="studio-bar__left">
        <button type="button" class="studio-back" @click="router.push('/home')">
          ← 返回传统后台
        </button>
        <div class="studio-title">
          <span class="studio-badge">🎨 实景交互编辑台</span>
          <span class="studio-sub">所见即所得 · 全局修改自动同步至后台各板块</span>
        </div>

        <!-- 脏数据模块指示器 -->
        <div class="dirty-pills">
          <span
            v-for="k in (['SITE', 'NAVIGATION', 'HOME', 'CONTACT'] as const)"
            :key="k"
            class="dirty-pill"
            :class="{
              'is-dirty': dirtyKeys.includes(k),
              'is-unpub': configViews[k]?.hasUnpublishedChanges,
            }"
            :title="dirtyKeys.includes(k) ? '有未保存的画布修改' : configViews[k]?.hasUnpublishedChanges ? '草稿已存，待发布上线' : '已与线上同步'"
          >
            {{ k }}
            <i v-if="dirtyKeys.includes(k)" class="dot dot--dirty"></i>
            <i v-else-if="configViews[k]?.hasUnpublishedChanges" class="dot dot--unpub"></i>
          </span>
        </div>
      </div>

      <!-- 中部：模式切换 + 视口切换 + 撤销重做 -->
      <div class="studio-bar__center">
        <div class="seg-group" role="group" aria-label="编辑模式切换">
          <button
            type="button"
            class="seg-btn"
            :class="{ 'is-active': studioMode === 'edit' }"
            @click="switchStudioMode('edit')"
          >
            ✏️ 交互编辑
          </button>
          <button
            type="button"
            class="seg-btn"
            :class="{ 'is-active': studioMode === 'preview' }"
            @click="switchStudioMode('preview')"
          >
            👁️ 纯净预演
          </button>
          <button
            type="button"
            class="seg-btn"
            :class="{ 'is-active': studioMode === 'published' }"
            @click="switchStudioMode('published')"
          >
            🌐 线上已发布版
          </button>
        </div>

        <div class="seg-group" role="group" aria-label="响应式视口切换">
          <button
            type="button"
            class="seg-btn"
            :class="{ 'is-active': viewport === 'desktop' }"
            title="桌面端视口 (100%)"
            @click="viewport = 'desktop'"
          >
            🖥️ 桌面
          </button>
          <button
            type="button"
            class="seg-btn"
            :class="{ 'is-active': viewport === 'tablet' }"
            title="平板视口 (768px)"
            @click="viewport = 'tablet'"
          >
            📟 平板
          </button>
          <button
            type="button"
            class="seg-btn"
            :class="{ 'is-active': viewport === 'mobile' }"
            title="手机端视口 (390px)"
            @click="viewport = 'mobile'"
          >
            📱 手机
          </button>
        </div>

        <div class="seg-group">
          <button type="button" class="seg-btn" :disabled="!canUndo" title="撤销 (Ctrl+Z)" @click="undo">
            ↩ 撤销
          </button>
          <button type="button" class="seg-btn" :disabled="!canRedo" title="重做 (Ctrl+Y)" @click="redo">
            ↪ 重做
          </button>
        </div>
      </div>

      <!-- 右侧：体检、全局保存草稿、全局发布 -->
      <div class="studio-bar__right">
        <button
          type="button"
          class="health-btn"
          :class="{ 'has-error': healthIssues.some((i) => i.level === 'error') }"
          @click="healthDrawerOpen = true"
        >
          🛡️ 体检 ({{ healthIssues.length }})
        </button>
        <el-button
          type="primary"
          size="small"
          :loading="savingAll"
          :disabled="studioMode === 'published'"
          @click="saveAllDrafts(false)"
        >
          💾 保存全局草稿 {{ dirtyKeys.length ? `(${dirtyKeys.length})` : '' }}
        </el-button>
        <el-button
          v-if="isAdmin"
          type="success"
          size="small"
          :loading="publishingAll"
          :disabled="studioMode === 'published'"
          @click="saveAndPublishAll"
        >
          🚀 一键全局发布上线
        </el-button>
      </div>
    </header>

    <!-- ==================== 主体三栏工作区 ==================== -->
    <div class="studio-body">
      <!-- 左侧：全局图层与模块摆放排序大纲树 -->
      <aside v-if="showLeftOutline && studioMode === 'edit'" class="studio-outline">
        <div class="outline-head">
          <span>🗂️ 全局模块与摆放顺序</span>
          <button type="button" class="icon-close" @click="showLeftOutline = false">✕</button>
        </div>
        <p class="outline-tip">点击模块定位画布，使用 ↑↓ 自由调整首页模块上下摆放位置</p>

        <div class="outline-list">
          <!-- 固定顶栏 -->
          <div
            class="outline-item"
            :class="{ 'is-selected': selectedSection === 'header' }"
            @click="focusSection('header')"
          >
            <span class="outline-item__icon">🌐</span>
            <span class="outline-item__label">顶部品牌与导航栏</span>
            <el-tag size="small" type="info">SITE/NAV</el-tag>
          </div>

          <!-- 固定首屏 -->
          <div
            class="outline-item"
            :class="{ 'is-selected': selectedSection === 'hero' }"
            @click="focusSection('hero')"
          >
            <span class="outline-item__icon">🎬</span>
            <span class="outline-item__label">首屏海报/视频 ({{ drafts.HOME.hero.items.length }})</span>
            <el-tag size="small" type="primary">必显</el-tag>
          </div>

          <!-- 可自由排序的 6 大模块 -->
          <div
            v-for="(secKey, sIdx) in drafts.HOME.sectionOrder"
            :key="secKey"
            class="outline-item"
            :class="{
              'is-selected': selectedSection === secKey,
              'is-disabled': secKey !== 'consultation' && !drafts.HOME[secKey].enabled,
            }"
            @click="focusSection(secKey)"
          >
            <span class="outline-item__icon">{{ SECTION_META[secKey].icon }}</span>
            <span class="outline-item__label">{{ SECTION_META[secKey].label }}</span>
            <div class="outline-item__actions" @click.stop>
              <el-switch
                v-if="secKey !== 'consultation'"
                v-model="drafts.HOME[secKey].enabled"
                size="small"
                @change="recordSnapshot('切换模块显隐')"
              />
              <button
                type="button"
                class="mini-btn"
                :disabled="sIdx === 0"
                title="上移模块"
                @click="moveSection(secKey, -1)"
              >
                ↑
              </button>
              <button
                type="button"
                class="mini-btn"
                :disabled="sIdx === drafts.HOME.sectionOrder.length - 1"
                title="下移模块"
                @click="moveSection(secKey, 1)"
              >
                ↓
              </button>
            </div>
          </div>

          <!-- 固定页脚 -->
          <div
            class="outline-item"
            :class="{ 'is-selected': selectedSection === 'footer' }"
            @click="focusSection('footer')"
          >
            <span class="outline-item__icon">📞</span>
            <span class="outline-item__label">页脚与全站联系方式</span>
            <el-tag size="small" type="info">SITE/CONTACT</el-tag>
          </div>
        </div>

        <div class="outline-foot">
          <label class="outline-check">
            <input v-model="showDisabledBlocks" type="checkbox" />
            <span>在编辑态显示已关闭模块（半透明）</span>
          </label>
        </div>
      </aside>

      <!-- 折叠态展开悬浮钮 -->
      <button
        v-if="!showLeftOutline && studioMode === 'edit'"
        type="button"
        class="drawer-toggle drawer-toggle--left"
        @click="showLeftOutline = true"
      >
        🗂️ 模块树
      </button>

      <!-- ==================== 中央：可交互实景画布 ==================== -->
      <main class="studio-canvas-wrap">
        <div class="studio-canvas" :class="[`viewport--${viewport}`, `mode--${studioMode}`]">
          <!-- 1. 顶部品牌与导航实景交互区 (SITE + NAVIGATION) -->
          <header
            id="studio-sec-header"
            class="canvas-header"
            :class="{ 'is-focused': studioMode === 'edit' && selectedSection === 'header' }"
            @click="selectedSection = 'header'"
          >
            <div v-if="studioMode === 'edit'" class="sec-floating-bar">
              <span class="sec-tag">🌐 顶部导航与品牌 (同步至「全站设置」&「导航」)</span>
              <button type="button" class="tb-btn" @click.stop="triggerMediaPick({ target: 'siteLogo' })">
                🖼️ 换 Logo
              </button>
              <button type="button" class="tb-btn" @click.stop="addTopNav">
                ➕ 添加导航项
              </button>
              <button
                type="button"
                class="tb-btn"
                @click.stop="openMegaPreview = !openMegaPreview"
              >
                {{ openMegaPreview ? '收起全景下拉菜单' : '📂 编辑5列全景菜单' }}
              </button>
              <button type="button" class="tb-btn tb-btn--ghost" @click.stop="router.push('/settings/navigation')">
                去传统导航页 ↗
              </button>
            </div>

            <div class="canvas-header__inner">
              <!-- 品牌 Logo 与站点名称直改 -->
              <div class="canvas-brand">
                <div
                  class="canvas-brand__logo-wrap"
                  :title="studioMode === 'edit' ? '点击更换站点 Logo' : ''"
                  @click.stop="studioMode === 'edit' && triggerMediaPick({ target: 'siteLogo' })"
                >
                  <img
                    v-if="drafts.SITE.logoMediaId"
                    :src="mediaUrl(drafts.SITE.logoMediaId)"
                    :alt="drafts.SITE.logoAlt || drafts.SITE.siteName"
                    class="canvas-brand__logo"
                  />
                  <span v-else class="canvas-brand__mark">
                    {{ drafts.SITE.siteName.slice(0, 1) || 'G' }}
                  </span>
                </div>
                <input
                  v-if="studioMode === 'edit'"
                  v-model="drafts.SITE.siteName"
                  class="inline-input inline-input--brand"
                  maxlength="60"
                  placeholder="站点名称"
                  @change="recordSnapshot('修改站点名称')"
                />
                <span v-else class="canvas-brand__name">{{ drafts.SITE.siteName }}</span>
              </div>

              <!-- 导航列表直改 -->
              <nav class="canvas-nav">
                <div
                  v-for="(navItem, nIdx) in drafts.NAVIGATION.items"
                  :key="navItem.key ?? `nav-${nIdx}`"
                  class="canvas-nav__item"
                  :class="{ 'is-disabled': !navItem.enabled }"
                >
                  <template v-if="studioMode === 'edit'">
                    <input
                      v-model="navItem.label"
                      class="inline-input inline-input--nav"
                      maxlength="20"
                      placeholder="导航名"
                      @change="recordSnapshot('修改导航名称')"
                    />
                    <div class="nav-item-pop">
                      <button
                        type="button"
                        title="绑定跳转链接"
                        @click.stop="
                          openLinkModal({
                            title: `导航「${navItem.label}」跳转目标`,
                            current: navItem.link,
                            allowNone: true,
                            allowConsult: true,
                            onConfirm: (val) => (navItem.link = val),
                          })
                        "
                      >
                        🔗 {{ describeLink(navItem.link) }}
                      </button>
                      <button
                        type="button"
                        :disabled="nIdx === 0"
                        title="左移"
                        @click.stop="moveItem(drafts.NAVIGATION.items, nIdx, -1)"
                      >
                        ←
                      </button>
                      <button
                        type="button"
                        :disabled="nIdx === drafts.NAVIGATION.items.length - 1"
                        title="右移"
                        @click.stop="moveItem(drafts.NAVIGATION.items, nIdx, 1)"
                      >
                        →
                      </button>
                      <button
                        type="button"
                        class="danger"
                        title="删除该导航"
                        @click.stop="drafts.NAVIGATION.items.splice(nIdx, 1); recordSnapshot('删除导航项')"
                      >
                        ✕
                      </button>
                    </div>
                  </template>
                  <span v-else class="canvas-nav__text">{{ navItem.label }}</span>
                </div>
              </nav>

              <div class="canvas-header__cta">
                <span class="assistant-pill">🤖 数字物流专家 小鑫</span>
                <span class="cta-pill">产品试用</span>
              </div>
            </div>

            <!-- 5 列全景下拉菜单可视化编辑弹层 -->
            <div v-if="openMegaPreview && studioMode === 'edit'" class="mega-editor" @click.stop>
              <div class="mega-editor__head">
                <strong>📂 产品与解决方案 · 5 列全景下拉菜单可视化编辑 (同步至 NAVIGATION.megaColumns)</strong>
                <div class="mega-editor__actions">
                  <el-button
                    size="small"
                    @click="
                      drafts.NAVIGATION.megaColumns = drafts.NAVIGATION.megaColumns || [];
                      drafts.NAVIGATION.megaColumns.push({
                        title: '新菜单列',
                        items: [{ label: '新菜单项', path: '/products', tag: '' }],
                      });
                      recordSnapshot('新增全景菜单列')
                    "
                  >
                    + 新增菜单列
                  </el-button>
                  <el-button size="small" @click="openMegaPreview = false">收起</el-button>
                </div>
              </div>
              <div class="mega-editor__grid">
                <div
                  v-for="(col, cIdx) in drafts.NAVIGATION.megaColumns"
                  :key="`mcol-${cIdx}`"
                  class="mega-col-box"
                >
                  <div class="mega-col-box__head">
                    <input v-model="col.title" class="inline-input" placeholder="列标题" />
                    <button
                      type="button"
                      class="mini-del"
                      @click="drafts.NAVIGATION.megaColumns?.splice(cIdx, 1)"
                    >
                      删除列
                    </button>
                  </div>
                  <div v-for="(sub, sIdx) in col.items" :key="`msub-${sIdx}`" class="mega-sub-row">
                    <input v-model="sub.label" class="inline-input" placeholder="名称" style="width: 90px" />
                    <input v-model="sub.path" class="inline-input" placeholder="/products/1" style="flex: 1" />
                    <input v-model="sub.tag" class="inline-input" placeholder="角标(如:首款)" style="width: 64px" />
                    <button type="button" class="mini-del" @click="col.items.splice(sIdx, 1)">✕</button>
                  </div>
                  <button
                    type="button"
                    class="mega-add-sub"
                    @click="col.items.push({ label: '新子项', path: '/products', tag: '' })"
                  >
                    + 添加子项
                  </button>
                </div>
              </div>
            </div>
          </header>

          <!-- 2. 首屏海报/视频轮播实景交互区 (HOME.hero) -->
          <section
            id="studio-sec-hero"
            class="canvas-hero"
            :class="{ 'is-focused': studioMode === 'edit' && selectedSection === 'hero' }"
            @click="selectedSection = 'hero'"
          >
            <!-- 首屏悬浮工具条 -->
            <div v-if="studioMode === 'edit'" class="sec-floating-bar">
              <span class="sec-tag">🎬 首屏海报/视频 (共 {{ drafts.HOME.hero.items.length }}/5 屏)</span>
              <button
                v-if="currentHero"
                type="button"
                class="tb-btn"
                @click.stop="triggerMediaPick({ target: 'heroDesktop', index: heroIndex })"
              >
                🖼️ 换桌面背景(图/视频)
              </button>
              <button
                v-if="currentHero"
                type="button"
                class="tb-btn"
                @click.stop="triggerMediaPick({ target: 'heroMobile', index: heroIndex })"
              >
                📱 换手机背景
              </button>
              <button type="button" class="tb-btn" @click.stop="addHeroSlide">
                ➕ 新增一屏
              </button>
              <button
                v-if="drafts.HOME.hero.items.length > 1"
                type="button"
                class="tb-btn"
                :disabled="heroIndex === 0"
                @click.stop="moveItem(drafts.HOME.hero.items, heroIndex, -1); heroIndex = Math.max(0, heroIndex - 1)"
              >
                ⬅ 前移本屏
              </button>
              <button
                v-if="drafts.HOME.hero.items.length > 1"
                type="button"
                class="tb-btn"
                :disabled="heroIndex === drafts.HOME.hero.items.length - 1"
                @click.stop="moveItem(drafts.HOME.hero.items, heroIndex, 1); heroIndex = Math.min(drafts.HOME.hero.items.length - 1, heroIndex + 1)"
              >
                ➡ 后移本屏
              </button>
              <button
                v-if="currentHero && drafts.HOME.hero.items.length > 1"
                type="button"
                class="tb-btn tb-btn--danger"
                @click.stop="removeHeroSlide(heroIndex)"
              >
                🗑️ 删除本屏
              </button>
            </div>

            <template v-if="currentHero">
              <!-- 背景媒体（自动按桌面/手机视口切换并支持 MP4/WebM 视频） -->
              <div class="canvas-hero__media">
                <template v-if="viewport === 'mobile' && currentHero.mobileMediaId">
                  <video
                    v-if="isVideoMedia(currentHero.mobileMediaId)"
                    :key="`mvid-${currentHero.mobileMediaId}`"
                    :src="mediaUrl(currentHero.mobileMediaId)"
                    autoplay
                    muted
                    loop
                    playsinline
                  ></video>
                  <img v-else :src="mediaUrl(currentHero.mobileMediaId)" :alt="currentHero.imageAlt || currentHero.title" />
                </template>
                <template v-else-if="currentHero.desktopMediaId">
                  <video
                    v-if="isVideoMedia(currentHero.desktopMediaId)"
                    :key="`dvid-${currentHero.desktopMediaId}`"
                    :src="mediaUrl(currentHero.desktopMediaId)"
                    autoplay
                    muted
                    loop
                    playsinline
                  ></video>
                  <img v-else :src="mediaUrl(currentHero.desktopMediaId)" :alt="currentHero.imageAlt || currentHero.title" />
                </template>
                <div v-else class="canvas-hero__empty-media">
                  <p>尚未选择首屏背景图片或视频</p>
                  <el-button
                    v-if="studioMode === 'edit'"
                    type="primary"
                    @click.stop="triggerMediaPick({ target: 'heroDesktop', index: heroIndex })"
                  >
                    点击选择首屏图片 / 视频
                  </el-button>
                </div>
                <div class="canvas-hero__mask"></div>
              </div>

              <!-- 首屏前景文案直改区 -->
              <div class="canvas-hero__content">
                <p class="canvas-hero__kicker">INDUSTRIAL IOT · ENERGY INTELLIGENCE</p>
                <template v-if="studioMode === 'edit'">
                  <div class="hero-color-row">
                    <span class="color-tip">标题颜色:</span>
                    <el-color-picker v-model="currentHero.titleColor" size="small" @change="recordSnapshot('修改首屏标题颜色')" />
                    <span class="color-tip" style="margin-left: 10px">副标题:</span>
                    <el-color-picker v-model="currentHero.subtitleColor" size="small" @change="recordSnapshot('修改首屏副标题颜色')" />
                    <span class="color-tip" style="margin-left: 10px">按钮背景:</span>
                    <el-color-picker v-model="currentHero.buttonBgColor" size="small" @change="recordSnapshot('修改按钮背景色')" />
                    <span class="color-tip" style="margin-left: 10px">按钮文字:</span>
                    <el-color-picker v-model="currentHero.buttonTextColor" size="small" @change="recordSnapshot('修改按钮文字颜色')" />
                  </div>
                  <textarea
                    v-model="currentHero.title"
                    class="inline-textarea inline-textarea--hero-title"
                    :style="{ color: currentHero.titleColor || '#ffffff' }"
                    rows="2"
                    maxlength="40"
                    placeholder="在此直接输入首屏大标题（支持换行）"
                    @change="recordSnapshot('修改首屏标题')"
                  ></textarea>
                  <textarea
                    v-model="currentHero.subtitle"
                    class="inline-textarea inline-textarea--hero-sub"
                    :style="{ color: currentHero.subtitleColor || 'rgba(255, 255, 255, 0.88)' }"
                    rows="2"
                    maxlength="100"
                    placeholder="在此输入首屏副标题说明..."
                    @change="recordSnapshot('修改首屏副标题')"
                  ></textarea>
                  <div class="canvas-hero__btn-edit">
                    <input
                      v-model="currentHero.buttonText"
                      class="inline-input inline-input--hero-btn"
                      :style="{
                        backgroundColor: currentHero.buttonBgColor || '#e5e7eb',
                        color: currentHero.buttonTextColor || '#1f2329'
                      }"
                      maxlength="12"
                      placeholder="按钮文字(如:了解详情)"
                      @change="recordSnapshot('修改首屏按钮文字')"
                    />
                    <button
                      type="button"
                      class="link-badge-btn"
                      @click.stop="
                        openLinkModal({
                          title: '首屏主按钮跳转目标',
                          current: currentHero.link,
                          allowNone: true,
                          allowConsult: true,
                          onConfirm: (val) => {
                            if (currentHero) currentHero.link = val
                          },
                        })
                      "
                    >
                      🔗 {{ describeLink(currentHero.link) }}
                    </button>
                  </div>
                </template>
                <template v-else>
                  <h1 class="canvas-hero__title" :style="{ color: currentHero.titleColor || '#ffffff' }">{{ currentHero.title }}</h1>
                  <p v-if="currentHero.subtitle" class="canvas-hero__sub" :style="{ color: currentHero.subtitleColor || 'rgba(255, 255, 255, 0.88)' }">{{ currentHero.subtitle }}</p>
                  <span
                    v-if="currentHero.buttonText"
                    class="canvas-hero__cta"
                    :style="{
                      backgroundColor: currentHero.buttonBgColor || '#e5e7eb',
                      color: currentHero.buttonTextColor || '#1f2329'
                    }"
                  >
                    {{ currentHero.buttonText }} →
                  </span>
                </template>

                <!-- 底部轮播切换条 -->
                <div class="canvas-hero__dots">
                  <button
                    v-for="(hItem, hIdx) in drafts.HOME.hero.items"
                    :key="hItem.key ?? `dot-${hIdx}`"
                    type="button"
                    class="hero-dot"
                    :class="{ 'is-active': hIdx === heroIndex, 'is-off': !hItem.enabled }"
                    @click.stop="heroIndex = hIdx"
                  >
                    <span class="hero-dot__num">0{{ hIdx + 1 }}</span>
                    <span class="hero-dot__name">{{ hItem.title || '未命名首屏' }}</span>
                  </button>
                </div>
              </div>
            </template>
          </section>

          <!-- ==================== 3. 动态可自由排序的 6 大业务模块 ==================== -->
          <div class="canvas-sections">
            <template v-for="(secKey, orderIdx) in drafts.HOME.sectionOrder" :key="secKey">
              <!-- (A) 企业指标模块 metrics -->
              <section
                v-if="secKey === 'metrics' && (drafts.HOME.metrics.enabled || (studioMode === 'edit' && showDisabledBlocks))"
                id="studio-sec-metrics"
                class="canvas-block"
                :class="{
                  'is-focused': studioMode === 'edit' && selectedSection === 'metrics',
                  'is-disabled-block': !drafts.HOME.metrics.enabled,
                }"
                @click="selectedSection = 'metrics'"
              >
                <div v-if="studioMode === 'edit'" class="sec-floating-bar">
                  <span class="sec-tag">📊 企业指标 (排序 #{{ orderIdx + 1 }})</span>
                  <el-switch v-model="drafts.HOME.metrics.enabled" size="small" active-text="显示" />
                  <button type="button" class="tb-btn" :disabled="orderIdx === 0" @click.stop="moveSection('metrics', -1)">
                    ⬆️ 上移模块
                  </button>
                  <button
                    type="button"
                    class="tb-btn"
                    :disabled="orderIdx === drafts.HOME.sectionOrder.length - 1"
                    @click.stop="moveSection('metrics', 1)"
                  >
                    ⬇️ 下移模块
                  </button>
                  <button type="button" class="tb-btn" @click.stop="addMetricItem">
                    ➕ 添加指标
                  </button>
                </div>

                <div class="canvas-wrap">
                  <div class="sec-header-center">
                    <template v-if="studioMode === 'edit'">
                      <input
                        v-model="drafts.HOME.metrics.title"
                        class="inline-input inline-input--sec-title"
                        maxlength="40"
                        placeholder="模块标题（如：大数据）"
                      />
                      <input
                        v-model="drafts.HOME.metrics.subtitle"
                        class="inline-input inline-input--sec-sub"
                        maxlength="200"
                        placeholder="可选副标题说明..."
                      />
                    </template>
                    <template v-else>
                      <h2 class="sec-title">{{ drafts.HOME.metrics.title || '企业实力' }}</h2>
                      <p v-if="drafts.HOME.metrics.subtitle" class="sec-sub">{{ drafts.HOME.metrics.subtitle }}</p>
                    </template>
                  </div>

                  <div class="metrics-grid">
                    <div
                      v-for="(mItem, mIdx) in drafts.HOME.metrics.items"
                      :key="mItem.key ?? `m-${mIdx}`"
                      class="metric-box"
                    >
                      <template v-if="studioMode === 'edit'">
                        <div class="metric-box__num-row">
                          <input
                            v-model="mItem.displayValue"
                            class="inline-input inline-input--metric-val"
                            maxlength="16"
                            placeholder="800W"
                          />
                          <input
                            v-model="mItem.unit"
                            class="inline-input inline-input--metric-unit"
                            maxlength="8"
                            placeholder="+"
                          />
                        </div>
                        <input
                          v-model="mItem.label"
                          class="inline-input inline-input--metric-label"
                          maxlength="20"
                          placeholder="指标名称"
                        />
                        <input
                          v-model="mItem.description"
                          class="inline-input inline-input--metric-desc"
                          maxlength="80"
                          placeholder="补充说明(可选)"
                        />
                        <div class="item-mini-bar">
                          <button type="button" :disabled="mIdx === 0" @click.stop="moveItem(drafts.HOME.metrics.items, mIdx, -1)">←</button>
                          <button type="button" :disabled="mIdx === drafts.HOME.metrics.items.length - 1" @click.stop="moveItem(drafts.HOME.metrics.items, mIdx, 1)">→</button>
                          <button type="button" class="danger" @click.stop="drafts.HOME.metrics.items.splice(mIdx, 1)">删除</button>
                        </div>
                      </template>
                      <template v-else>
                        <p class="metric-box__val">
                          {{ mItem.displayValue }}<span>{{ mItem.unit }}</span>
                        </p>
                        <p class="metric-box__lbl">{{ mItem.label }}</p>
                        <p v-if="mItem.description" class="metric-box__dsc">{{ mItem.description }}</p>
                      </template>
                    </div>
                  </div>
                </div>
              </section>

              <!-- (B) 产品与解决方案模块 services -->
              <section
                v-if="secKey === 'services' && (drafts.HOME.services.enabled || (studioMode === 'edit' && showDisabledBlocks))"
                id="studio-sec-services"
                class="canvas-block canvas-block--alt"
                :class="{
                  'is-focused': studioMode === 'edit' && selectedSection === 'services',
                  'is-disabled-block': !drafts.HOME.services.enabled,
                }"
                @click="selectedSection = 'services'"
              >
                <div v-if="studioMode === 'edit'" class="sec-floating-bar">
                  <span class="sec-tag">📦 产品与解决方案 (排序 #{{ orderIdx + 1 }})</span>
                  <el-switch v-model="drafts.HOME.services.enabled" size="small" active-text="显示" />
                  <button type="button" class="tb-btn" :disabled="orderIdx === 0" @click.stop="moveSection('services', -1)">
                    ⬆️ 上移模块
                  </button>
                  <button
                    type="button"
                    class="tb-btn"
                    :disabled="orderIdx === drafts.HOME.sectionOrder.length - 1"
                    @click.stop="moveSection('services', 1)"
                  >
                    ⬇️ 下移模块
                  </button>
                  <button type="button" class="tb-btn" @click.stop="addServiceGroup">
                    ➕ 新增服务分组
                  </button>
                </div>

                <div class="canvas-wrap">
                  <div class="sec-header-center">
                    <template v-if="studioMode === 'edit'">
                      <input
                        v-model="drafts.HOME.services.title"
                        class="inline-input inline-input--sec-title"
                        maxlength="40"
                        placeholder="模块标题（如：产品与解决方案）"
                      />
                      <input
                        v-model="drafts.HOME.services.subtitle"
                        class="inline-input inline-input--sec-sub"
                        maxlength="200"
                        placeholder="模块副标题..."
                      />
                    </template>
                    <template v-else>
                      <h2 class="sec-title">{{ drafts.HOME.services.title }}</h2>
                      <p v-if="drafts.HOME.services.subtitle" class="sec-sub">{{ drafts.HOME.services.subtitle }}</p>
                    </template>
                  </div>

                  <div
                    v-for="(grp, gIdx) in drafts.HOME.services.groups"
                    :key="grp.key ?? `grp-${gIdx}`"
                    class="service-group-stage"
                  >
                    <div class="service-group-stage__info">
                      <template v-if="studioMode === 'edit'">
                        <div class="grp-toolbar">
                          <el-tag size="small">分组 0{{ gIdx + 1 }}</el-tag>
                          <el-select
                            v-model="grp.contentType"
                            size="small"
                            style="width: 110px"
                            @change="grp.contentIds = []"
                          >
                            <el-option label="关联产品" value="PRODUCT" />
                            <el-option label="关联方案" value="SOLUTION" />
                          </el-select>
                          <button type="button" class="mini-btn" :disabled="gIdx === 0" @click.stop="moveItem(drafts.HOME.services.groups, gIdx, -1)">↑</button>
                          <button type="button" class="mini-btn" :disabled="gIdx === drafts.HOME.services.groups.length - 1" @click.stop="moveItem(drafts.HOME.services.groups, gIdx, 1)">↓</button>
                          <button type="button" class="mini-del" @click.stop="drafts.HOME.services.groups.splice(gIdx, 1)">删除分组</button>
                        </div>
                        <input
                          v-model="grp.title"
                          class="inline-input inline-input--grp-title"
                          maxlength="30"
                          placeholder="分组名称（如：IoT 服务）"
                        />
                        <textarea
                          v-model="grp.description"
                          class="inline-textarea"
                          rows="2"
                          maxlength="200"
                          placeholder="分组业务场景描述..."
                        ></textarea>
                        <div class="grp-picker-wrap" @click.stop>
                          <p class="grp-picker-label">已关联的线上卡片（可上下调整顺序或增删）：</p>
                          <ContentPicker v-model="grp.contentIds" :content-type="grp.contentType" :max="6" />
                        </div>
                        <div class="grp-link-row">
                          <button
                            type="button"
                            class="link-badge-btn"
                            @click.stop="
                              openLinkModal({
                                title: `分组「${grp.title}」更多跳转链接`,
                                current: grp.moreLink,
                                allowNone: true,
                                onConfirm: (val) => (grp.moreLink = val),
                              })
                            "
                          >
                            🔗 更多链接: {{ describeLink(grp.moreLink) }}
                          </button>
                        </div>
                      </template>
                      <template v-else>
                        <h3 class="grp-title">{{ grp.title }}</h3>
                        <p class="grp-desc">{{ grp.description }}</p>
                      </template>

                      <!-- 关联内容卡片实时渲染 -->
                      <div class="cards-grid">
                        <div v-for="card in resolveCards(grp.contentIds)" :key="card.id" class="live-card">
                          <img
                            v-if="card.coverMediaId"
                            :src="mediaUrl(card.coverMediaId)"
                            :alt="card.title"
                            class="live-card__img"
                          />
                          <div class="live-card__body">
                            <h4>{{ card.title }}</h4>
                            <span class="live-card__go">查看详情 →</span>
                          </div>
                        </div>
                      </div>
                    </div>


                  </div>
                </div>
              </section>

              <!-- (C) 客户案例模块 cases -->
              <section
                v-if="secKey === 'cases' && (drafts.HOME.cases.enabled || (studioMode === 'edit' && showDisabledBlocks))"
                id="studio-sec-cases"
                class="canvas-block"
                :class="{
                  'is-focused': studioMode === 'edit' && selectedSection === 'cases',
                  'is-disabled-block': !drafts.HOME.cases.enabled,
                }"
                @click="selectedSection = 'cases'"
              >
                <div v-if="studioMode === 'edit'" class="sec-floating-bar">
                  <span class="sec-tag">🏆 客户案例 (排序 #{{ orderIdx + 1 }})</span>
                  <el-switch v-model="drafts.HOME.cases.enabled" size="small" active-text="显示" />
                  <button type="button" class="tb-btn" :disabled="orderIdx === 0" @click.stop="moveSection('cases', -1)">
                    ⬆️ 上移模块
                  </button>
                  <button
                    type="button"
                    class="tb-btn"
                    :disabled="orderIdx === drafts.HOME.sectionOrder.length - 1"
                    @click.stop="moveSection('cases', 1)"
                  >
                    ⬇️ 下移模块
                  </button>
                  <button type="button" class="tb-btn tb-btn--ghost" @click.stop="router.push('/contents/cases')">
                    管理案例库 ↗
                  </button>
                </div>

                <div class="canvas-wrap">
                  <div class="sec-header-center">
                    <template v-if="studioMode === 'edit'">
                      <input
                        v-model="drafts.HOME.cases.title"
                        class="inline-input inline-input--sec-title"
                        maxlength="40"
                        placeholder="模块标题（如：携手行业标杆客户）"
                      />
                      <input
                        v-model="drafts.HOME.cases.subtitle"
                        class="inline-input inline-input--sec-sub"
                        maxlength="200"
                        placeholder="模块副标题..."
                      />
                    </template>
                    <template v-else>
                      <h2 class="sec-title">{{ drafts.HOME.cases.title }}</h2>
                      <p v-if="drafts.HOME.cases.subtitle" class="sec-sub">{{ drafts.HOME.cases.subtitle }}</p>
                    </template>
                  </div>

                  <div v-if="studioMode === 'edit'" class="inline-picker-panel" @click.stop>
                    <ContentPicker v-model="drafts.HOME.cases.contentIds" content-type="CASE" :max="6" />
                  </div>

                  <div class="cards-grid cards-grid--3">
                    <div v-for="cItem in resolveCards(drafts.HOME.cases.contentIds)" :key="cItem.id" class="live-card">
                      <img
                        v-if="cItem.coverMediaId"
                        :src="mediaUrl(cItem.coverMediaId)"
                        :alt="cItem.title"
                        class="live-card__img"
                      />
                      <div class="live-card__body">
                        <span v-if="cItem.categoryName" class="live-card__tag">{{ cItem.categoryName }}</span>
                        <h4>{{ cItem.title }}</h4>
                      </div>
                    </div>
                  </div>
                </div>
              </section>

              <!-- (D) 客户标识墙模块 customers -->
              <section
                v-if="secKey === 'customers' && (drafts.HOME.customers.enabled || (studioMode === 'edit' && showDisabledBlocks))"
                id="studio-sec-customers"
                class="canvas-block canvas-block--alt"
                :class="{
                  'is-focused': studioMode === 'edit' && selectedSection === 'customers',
                  'is-disabled-block': !drafts.HOME.customers.enabled,
                }"
                @click="selectedSection = 'customers'"
              >
                <div v-if="studioMode === 'edit'" class="sec-floating-bar">
                  <span class="sec-tag">🤝 客户标识墙 (排序 #{{ orderIdx + 1 }})</span>
                  <el-switch v-model="drafts.HOME.customers.enabled" size="small" active-text="显示" />
                  <button type="button" class="tb-btn" :disabled="orderIdx === 0" @click.stop="moveSection('customers', -1)">
                    ⬆️ 上移模块
                  </button>
                  <button
                    type="button"
                    class="tb-btn"
                    :disabled="orderIdx === drafts.HOME.sectionOrder.length - 1"
                    @click.stop="moveSection('customers', 1)"
                  >
                    ⬇️ 下移模块
                  </button>
                  <button type="button" class="tb-btn tb-btn--ghost" @click.stop="router.push('/contents/customers')">
                    管理客户标识库 ↗
                  </button>
                </div>

                <div class="canvas-wrap">
                  <div class="sec-header-center">
                    <template v-if="studioMode === 'edit'">
                      <input
                        v-model="drafts.HOME.customers.title"
                        class="inline-input inline-input--sec-title"
                        maxlength="40"
                        placeholder="模块标题（如：合作客户）"
                      />
                    </template>
                    <template v-else>
                      <h2 class="sec-title">{{ drafts.HOME.customers.title || '合作客户' }}</h2>
                    </template>
                  </div>

                  <div v-if="studioMode === 'edit'" class="inline-picker-panel" @click.stop>
                    <ContentPicker v-model="drafts.HOME.customers.contentIds" content-type="CUSTOMER" :max="30" />
                  </div>

                  <div class="logo-wall">
                    <div
                      v-for="cust in resolveCards(drafts.HOME.customers.contentIds)"
                      :key="cust.id"
                      class="logo-wall__item"
                    >
                      <img v-if="cust.coverMediaId" :src="mediaUrl(cust.coverMediaId)" :alt="cust.title" />
                      <span>{{ cust.title }}</span>
                    </div>
                  </div>
                </div>
              </section>

              <!-- (E) 新闻动态模块 news -->
              <section
                v-if="secKey === 'news' && (drafts.HOME.news.enabled || (studioMode === 'edit' && showDisabledBlocks))"
                id="studio-sec-news"
                class="canvas-block"
                :class="{
                  'is-focused': studioMode === 'edit' && selectedSection === 'news',
                  'is-disabled-block': !drafts.HOME.news.enabled,
                }"
                @click="selectedSection = 'news'"
              >
                <div v-if="studioMode === 'edit'" class="sec-floating-bar">
                  <span class="sec-tag">📰 新闻动态 (排序 #{{ orderIdx + 1 }})</span>
                  <el-switch v-model="drafts.HOME.news.enabled" size="small" active-text="显示" />
                  <el-radio-group v-model="drafts.HOME.news.mode" size="small" @click.stop>
                    <el-radio-button value="SELECTED">手选推荐</el-radio-button>
                    <el-radio-button value="LATEST">最新时间倒序</el-radio-button>
                  </el-radio-group>
                  <el-input-number
                    v-model="drafts.HOME.news.limit"
                    size="small"
                    :min="1"
                    :max="6"
                    style="width: 96px"
                    @click.stop
                  />
                  <button type="button" class="tb-btn" :disabled="orderIdx === 0" @click.stop="moveSection('news', -1)">
                    ⬆️ 上移
                  </button>
                  <button
                    type="button"
                    class="tb-btn"
                    :disabled="orderIdx === drafts.HOME.sectionOrder.length - 1"
                    @click.stop="moveSection('news', 1)"
                  >
                    ⬇️ 下移
                  </button>
                </div>

                <div class="canvas-wrap">
                  <div class="sec-header-center">
                    <template v-if="studioMode === 'edit'">
                      <input
                        v-model="drafts.HOME.news.title"
                        class="inline-input inline-input--sec-title"
                        maxlength="40"
                        placeholder="模块标题（如：最新动态）"
                      />
                    </template>
                    <template v-else>
                      <h2 class="sec-title">{{ drafts.HOME.news.title || '新闻动态' }}</h2>
                    </template>
                  </div>

                  <div
                    v-if="studioMode === 'edit' && drafts.HOME.news.mode === 'SELECTED'"
                    class="inline-picker-panel"
                    @click.stop
                  >
                    <ContentPicker v-model="drafts.HOME.news.contentIds" content-type="NEWS" :max="6" />
                  </div>

                  <div class="news-list-preview">
                    <div v-for="nItem in resolvedNews" :key="nItem.id" class="news-row-preview">
                      <span class="news-row-preview__date">{{ nItem.displayTime?.slice(0, 10) || '最新' }}</span>
                      <strong class="news-row-preview__title">{{ nItem.title }}</strong>
                      <span class="news-row-preview__arrow">阅读全文 →</span>
                    </div>
                  </div>
                </div>
              </section>

              <!-- (F) 底部咨询行动区 consultation -->
              <section
                v-if="secKey === 'consultation'"
                id="studio-sec-consultation"
                class="canvas-cta"
                :class="{ 'is-focused': studioMode === 'edit' && selectedSection === 'consultation' }"
                @click="selectedSection = 'consultation'"
              >
                <div v-if="studioMode === 'edit'" class="sec-floating-bar">
                  <span class="sec-tag">💬 底部咨询区 (排序 #{{ orderIdx + 1 }})</span>
                  <button type="button" class="tb-btn" :disabled="orderIdx === 0" @click.stop="moveSection('consultation', -1)">
                    ⬆️ 上移模块
                  </button>
                  <button
                    type="button"
                    class="tb-btn"
                    :disabled="orderIdx === drafts.HOME.sectionOrder.length - 1"
                    @click.stop="moveSection('consultation', 1)"
                  >
                    ⬇️ 下移模块
                  </button>
                </div>

                <div class="canvas-wrap canvas-cta__inner">
                  <template v-if="studioMode === 'edit'">
                    <input
                      v-model="drafts.HOME.consultation.title"
                      class="inline-input inline-input--cta-title"
                      maxlength="40"
                      placeholder="行动区主标题"
                    />
                    <input
                      v-model="drafts.HOME.consultation.subtitle"
                      class="inline-input inline-input--cta-sub"
                      maxlength="200"
                      placeholder="行动区副标题说明"
                    />
                    <div class="canvas-hero__btn-edit">
                      <input
                        v-model="drafts.HOME.consultation.buttonText"
                        class="inline-input inline-input--hero-btn"
                        maxlength="12"
                        placeholder="立即咨询"
                      />
                      <button
                        type="button"
                        class="link-badge-btn"
                        @click.stop="
                          openLinkModal({
                            title: '底部咨询区按钮动作',
                            current: drafts.HOME.consultation.link,
                            allowNone: false,
                            allowConsult: true,
                            onConfirm: (val) => (drafts.HOME.consultation.link = val),
                          })
                        "
                      >
                        🔗 {{ describeLink(drafts.HOME.consultation.link) }}
                      </button>
                    </div>
                  </template>
                  <template v-else>
                    <h2>{{ drafts.HOME.consultation.title }}</h2>
                    <p v-if="drafts.HOME.consultation.subtitle">{{ drafts.HOME.consultation.subtitle }}</p>
                    <span class="canvas-hero__cta">{{ drafts.HOME.consultation.buttonText || '立即咨询' }}</span>
                  </template>
                </div>
              </section>
            </template>
          </div>

          <!-- ==================== 4. 底部页脚与全站联系方式实景交互区 (SITE + CONTACT) ==================== -->
          <footer
            id="studio-sec-footer"
            class="canvas-footer"
            :class="{ 'is-focused': studioMode === 'edit' && selectedSection === 'footer' }"
            @click="selectedSection = 'footer'"
          >
            <div v-if="studioMode === 'edit'" class="sec-floating-bar">
              <span class="sec-tag">📞 页脚分组与全站联系方式 (同步至「全站设置」&「联系我们」)</span>
              <el-switch v-model="drafts.CONTACT.formEnabled" size="small" active-text="开启全站咨询表单" />
              <button type="button" class="tb-btn" @click.stop="addFooterGroup">
                ➕ 添加页脚分组
              </button>
              <button type="button" class="tb-btn tb-btn--ghost" @click.stop="router.push('/pages/contact')">
                去联系我们页 ↗
              </button>
            </div>

            <div class="canvas-wrap canvas-footer__grid">
              <!-- 品牌与标语 -->
              <div class="footer-col">
                <strong>{{ drafts.SITE.siteName }}</strong>
                <textarea
                  v-if="studioMode === 'edit'"
                  v-model="drafts.SITE.slogan"
                  class="inline-textarea inline-textarea--dark"
                  rows="2"
                  maxlength="100"
                  placeholder="一句话品牌标语..."
                ></textarea>
                <p v-else>{{ drafts.SITE.slogan }}</p>
              </div>

              <!-- 页脚分组链接直改 -->
              <div
                v-for="(fGrp, fgIdx) in drafts.SITE.footerGroups"
                :key="`fg-${fgIdx}`"
                class="footer-col"
              >
                <template v-if="studioMode === 'edit'">
                  <div class="fg-head">
                    <input v-model="fGrp.title" class="inline-input inline-input--dark" maxlength="20" placeholder="分组名" />
                    <button type="button" class="mini-del" @click.stop="drafts.SITE.footerGroups.splice(fgIdx, 1)">✕</button>
                  </div>
                  <div v-for="(fLnk, flIdx) in fGrp.links" :key="fLnk.key ?? `fl-${flIdx}`" class="fg-link-row">
                    <input v-model="fLnk.label" class="inline-input inline-input--dark" maxlength="20" placeholder="链接名" />
                    <button
                      type="button"
                      class="mini-btn"
                      title="修改链接"
                      @click.stop="
                        openLinkModal({
                          title: `页脚链接「${fLnk.label}」`,
                          current: fLnk.link,
                          allowNone: false,
                          allowConsult: true,
                          onConfirm: (val) => (fLnk.link = val),
                        })
                      "
                    >
                      🔗
                    </button>
                    <button type="button" class="mini-del" @click.stop="fGrp.links.splice(flIdx, 1)">✕</button>
                  </div>
                  <button
                    type="button"
                    class="mega-add-sub"
                    @click.stop="
                      fGrp.links.push({
                        label: '新链接',
                        enabled: true,
                        sortOrder: (fGrp.links.length + 1) * 10,
                        link: { type: 'PAGE', pageCode: 'ABOUT' },
                      })
                    "
                  >
                    + 添加链接
                  </button>
                </template>
                <template v-else>
                  <h4>{{ fGrp.title }}</h4>
                  <ul>
                    <li v-for="(fLnk, flIdx) in fGrp.links" :key="`flv-${flIdx}`">{{ fLnk.label }}</li>
                  </ul>
                </template>
              </div>

              <!-- 联系我们直改列 (同步至 CONTACT) -->
              <div class="footer-col footer-col--contact">
                <h4>联系我们 (全站共用)</h4>
                <template v-if="studioMode === 'edit'">
                  <label class="dark-field">
                    <span>全国热线电话</span>
                    <input v-model="drafts.CONTACT.phone" class="inline-input inline-input--dark-lg" placeholder="400-888-6688" />
                  </label>
                  <label class="dark-field">
                    <span>服务时间</span>
                    <input v-model="drafts.CONTACT.hours" class="inline-input inline-input--dark" placeholder="周一至周日 9:00-18:00" />
                  </label>
                  <label class="dark-field">
                    <span>商务邮箱</span>
                    <input v-model="drafts.CONTACT.email" class="inline-input inline-input--dark" placeholder="connect@company.com" />
                  </label>
                  <label class="dark-field">
                    <span>公司地址</span>
                    <input v-model="drafts.CONTACT.address" class="inline-input inline-input--dark" placeholder="详细办公地址" />
                  </label>
                </template>
                <template v-else>
                  <p class="footer-phone">{{ drafts.CONTACT.phone }}</p>
                  <p>{{ drafts.CONTACT.hours }}</p>
                  <p>{{ drafts.CONTACT.email }}</p>
                  <p>{{ drafts.CONTACT.address }}</p>
                </template>
              </div>
            </div>

            <!-- 版权与备案直改条 -->
            <div class="canvas-wrap canvas-footer__bottom">
              <template v-if="studioMode === 'edit'">
                <input v-model="drafts.SITE.copyrightText" class="inline-input inline-input--dark" style="width: 260px" placeholder="版权文案(必填)" />
                <input v-model="drafts.SITE.icpText" class="inline-input inline-input--dark" style="width: 180px" placeholder="ICP备案号" />
                <input v-model="drafts.SITE.icpUrl" class="inline-input inline-input--dark" style="width: 220px" placeholder="https://beian.miit.gov.cn/" />
              </template>
              <template v-else>
                <span>{{ drafts.SITE.copyrightText }}</span>
                <span v-if="drafts.SITE.icpText">{{ drafts.SITE.icpText }}</span>
              </template>
            </div>
          </footer>
        </div>
      </main>
    </div>

    <!-- ==================== 媒体选择器弹窗 ==================== -->
    <MediaPicker
      v-model="mediaPickerOpen"
      :selected-id="null"
      :kind="mediaPickerKind"
      @select="onMediaPicked"
    />

    <!-- ==================== 链接绑定器弹窗 ==================== -->
    <el-dialog v-model="linkDialogOpen" :title="linkDialogTitle" width="620px">
      <p style="margin-bottom: 12px; font-size: 13px; color: #5f6470">
        选择点击该按钮或菜单后的目标动作（支持站内页面、具体内容详情、外部链接、首页锚点或打开咨询弹窗）：
      </p>
      <LinkEditor
        v-model="tempLink"
        :allow-none="linkDialogAllowNone"
        :allow-consult="linkDialogAllowConsult"
      />
      <template #footer>
        <el-button @click="linkDialogOpen = false">取消</el-button>
        <el-button type="primary" @click="confirmLinkModal">确定绑定</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 发布前智能体检抽屉 ==================== -->
    <el-drawer v-model="healthDrawerOpen" title="🛡️ 全局发布前智能合规体检" size="400px">
      <el-alert
        v-if="healthIssues.length === 0"
        type="success"
        :closable="false"
        show-icon
        title="所有模块均已通过合规检查，可以随时安全发布！"
      />
      <div v-else class="health-list">
        <div
          v-for="(iss, idx) in healthIssues"
          :key="idx"
          class="health-item"
          :class="`is-${iss.level}`"
          @click="healthDrawerOpen = false; focusSection(iss.section)"
        >
          <div class="health-item__head">
            <el-tag size="small" :type="iss.level === 'error' ? 'danger' : 'warning'">
              {{ iss.level === 'error' ? '阻塞项' : '建议优化' }}
            </el-tag>
            <strong>{{ iss.module }}</strong>
          </div>
          <p class="health-item__msg">{{ iss.message }}</p>
          <span class="health-item__go">点击定位至画布该模块 →</span>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.studio {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: #0f1117;
  color: #101014;
}

/* 顶部全局控制栏 */
.studio-bar {
  height: 58px;
  background: #161923;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding: 0 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #ffffff;
  flex: none;
  z-index: 50;
}

.studio-bar__left,
.studio-bar__center,
.studio-bar__right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.studio-back {
  padding: 6px 12px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: rgba(255, 255, 255, 0.06);
  color: #ffffff;
  font-size: 12.5px;
  cursor: pointer;
}

.studio-back:hover {
  background: rgba(255, 255, 255, 0.14);
}

.studio-title {
  display: flex;
  flex-direction: column;
}

.studio-badge {
  font-size: 13.5px;
  font-weight: 700;
  color: #ffffff;
}

.studio-sub {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.55);
}

.dirty-pills {
  display: flex;
  gap: 6px;
  margin-left: 6px;
}

.dirty-pill {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 11px;
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.65);
}

.dirty-pill.is-dirty {
  background: rgba(245, 158, 11, 0.2);
  color: #fcd34d;
  border: 1px solid rgba(245, 158, 11, 0.45);
}

.dirty-pill.is-unpub {
  color: #a5b4fc;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.dot--dirty {
  background: #f59e0b;
}

.dot--unpub {
  background: #818cf8;
}

.seg-group {
  display: inline-flex;
  background: rgba(255, 255, 255, 0.07);
  padding: 3px;
  border-radius: 8px;
  gap: 2px;
}

.seg-btn {
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.72);
  font-size: 12px;
  padding: 5px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.seg-btn:hover:not(:disabled) {
  color: #ffffff;
}

.seg-btn.is-active {
  background: #5b4be8;
  color: #ffffff;
  font-weight: 600;
}

.seg-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.health-btn {
  padding: 5px 12px;
  border-radius: 6px;
  border: 1px solid rgba(16, 185, 129, 0.4);
  background: rgba(16, 185, 129, 0.12);
  color: #6ee7b7;
  font-size: 12px;
  cursor: pointer;
}

.health-btn.has-error {
  border-color: rgba(239, 68, 68, 0.5);
  background: rgba(239, 68, 68, 0.18);
  color: #fca5a5;
}

/* 主体布局 */
.studio-body {
  flex: 1;
  display: flex;
  min-height: 0;
  position: relative;
}

.studio-outline {
  width: 280px;
  background: #1b1f2c;
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  color: #ffffff;
  display: flex;
  flex-direction: column;
  flex: none;
}

.outline-head {
  padding: 14px 16px 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13.5px;
  font-weight: 700;
}

.outline-tip {
  padding: 0 16px 12px;
  font-size: 11.5px;
  color: rgba(255, 255, 255, 0.5);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.outline-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.outline-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 10px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid transparent;
  cursor: pointer;
  font-size: 12.5px;
  transition: all 0.15s ease;
}

.outline-item:hover {
  background: rgba(255, 255, 255, 0.08);
}

.outline-item.is-selected {
  border-color: #7c6df2;
  background: rgba(91, 75, 232, 0.22);
}

.outline-item.is-disabled {
  opacity: 0.5;
}

.outline-item__label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.outline-item__actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.outline-foot {
  padding: 12px 14px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.outline-check {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.drawer-toggle--left {
  position: absolute;
  left: 12px;
  top: 12px;
  z-index: 30;
  padding: 8px 12px;
  border-radius: 8px;
  background: #5b4be8;
  color: #fff;
  border: none;
  font-size: 12px;
  cursor: pointer;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
}

/* 中央画布容器 */
.studio-canvas-wrap {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  justify-content: center;
  background: #0d0f14;
}

.studio-canvas {
  width: 100%;
  background: #ffffff;
  border-radius: 12px;
  overflow-x: hidden;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.45);
  transition: max-width 0.3s ease;
  height: fit-content;
}

.studio-canvas.viewport--desktop {
  max-width: 1440px;
}

.studio-canvas.viewport--tablet {
  max-width: 768px;
}

.studio-canvas.viewport--mobile {
  max-width: 390px;
}

/* 区块悬浮工具栏 */
.sec-floating-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding: 8px 14px;
  background: rgba(22, 25, 35, 0.94);
  backdrop-filter: blur(8px);
  color: #ffffff;
  font-size: 12px;
  border-bottom: 1px solid rgba(91, 75, 232, 0.4);
}

.sec-tag {
  font-weight: 700;
  color: #c4b5fd;
  margin-right: 6px;
}

.tb-btn {
  padding: 4px 10px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.08);
  color: #ffffff;
  font-size: 12px;
  cursor: pointer;
}

.tb-btn:hover:not(:disabled) {
  background: #5b4be8;
  border-color: #5b4be8;
}

.tb-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.tb-btn--danger:hover {
  background: #dc2626;
  border-color: #dc2626;
}

.tb-btn--ghost {
  margin-left: auto;
  color: #a5b4fc;
}

/* 顶部导航实景 */
.canvas-header {
  position: relative;
  background: #ffffff;
  border-bottom: 1px solid rgba(16, 16, 20, 0.08);
}

.canvas-header.is-focused,
.canvas-hero.is-focused,
.canvas-block.is-focused,
.canvas-cta.is-focused,
.canvas-footer.is-focused {
  outline: 2px solid #5b4be8;
  outline-offset: -2px;
}

.canvas-header__inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 14px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}

.canvas-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.canvas-brand__logo-wrap {
  cursor: pointer;
}

.canvas-brand__logo {
  max-height: 34px;
  max-width: 140px;
  object-fit: contain;
}

.canvas-brand__mark {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  background: #5b4be8;
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.canvas-nav {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.canvas-nav__item {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 4px 6px;
  border-radius: 6px;
  background: #f8f9fc;
}

.nav-item-pop {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-item-pop button {
  font-size: 11px;
  padding: 2px 5px;
  border-radius: 4px;
  border: 1px solid #e2e8f0;
  background: #fff;
  cursor: pointer;
}

.nav-item-pop button.danger {
  color: #dc2626;
}

.canvas-header__cta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.assistant-pill {
  font-size: 12px;
  padding: 5px 12px;
  border-radius: 999px;
  background: #eeebff;
  color: #4636d6;
  font-weight: 600;
}

.cta-pill {
  font-size: 12.5px;
  padding: 6px 14px;
  border-radius: 6px;
  background: #101014;
  color: #ffffff;
  font-weight: 600;
}

/* 5列大菜单可视化编辑区 */
.mega-editor {
  background: #f8f9fc;
  border-top: 1px solid #e2e8f0;
  padding: 16px 24px;
}

.mega-editor__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 13px;
}

.mega-editor__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

.mega-col-box {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 10px;
}

.mega-col-box__head {
  display: flex;
  justify-content: space-between;
  gap: 6px;
  margin-bottom: 8px;
}

.mega-sub-row {
  display: flex;
  gap: 4px;
  margin-bottom: 6px;
}

.mega-add-sub {
  width: 100%;
  padding: 5px;
  border: 1px dashed #cbd5e1;
  border-radius: 6px;
  background: transparent;
  font-size: 12px;
  color: #5b4be8;
  cursor: pointer;
}

/* 首屏实景 */
.canvas-hero {
  position: relative;
  min-height: 520px;
  background: #08080c;
  color: #ffffff;
  display: flex;
  flex-direction: column;
}

.canvas-hero__media {
  position: relative;
  min-height: 480px;
  flex: 1;
}

.canvas-hero__media img,
.canvas-hero__media video {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.canvas-hero__empty-media {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: rgba(255, 255, 255, 0.65);
}

.canvas-hero__mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(8, 8, 12, 0.88) 0%, rgba(8, 8, 12, 0.45) 55%, rgba(8, 8, 12, 0.2) 100%);
  pointer-events: none;
}

.canvas-hero__content {
  position: relative;
  z-index: 5;
  max-width: 1200px;
  width: 100%;
  margin: -420px auto 0;
  padding: 40px 28px 48px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.hero-color-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
  background: rgba(15, 23, 42, 0.75);
  backdrop-filter: blur(8px);
  padding: 4px 12px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  width: fit-content;
}

.hero-color-row .color-tip {
  font-size: 11px;
  color: #e2e8f0;
}

.canvas-hero__kicker {
  font-size: 12px;
  letter-spacing: 0.16em;
  color: #a5b4fc;
  font-weight: 600;
}

.canvas-hero__title {
  font-size: clamp(32px, 4.5vw, 54px);
  font-weight: 700;
  line-height: 1.15;
  white-space: pre-line;
}

.canvas-hero__sub {
  font-size: 17px;
  color: rgba(255, 255, 255, 0.8);
  max-width: 600px;
}

.canvas-hero__btn-edit {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.link-badge-btn {
  padding: 8px 14px;
  border-radius: 8px;
  border: 1px solid rgba(91, 75, 232, 0.5);
  background: rgba(91, 75, 232, 0.2);
  color: #e0e7ff;
  font-size: 12.5px;
  cursor: pointer;
}

.canvas-hero__dots {
  display: flex;
  gap: 10px;
  margin-top: 28px;
  flex-wrap: wrap;
}

.hero-dot {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.75);
  font-size: 12px;
  cursor: pointer;
}

.hero-dot.is-active {
  background: #5b4be8;
  border-color: #5b4be8;
  color: #ffffff;
  font-weight: 600;
}

/* 通用内联编辑输入框 */
.inline-input,
.inline-textarea {
  font-family: inherit;
  border: 1px dashed rgba(91, 75, 232, 0.45);
  border-radius: 6px;
  padding: 4px 8px;
  background: rgba(255, 255, 255, 0.92);
  color: #101014;
  transition: all 0.15s ease;
}

.inline-input:focus,
.inline-textarea:focus {
  outline: none;
  border-style: solid;
  border-color: #5b4be8;
  box-shadow: 0 0 0 3px rgba(91, 75, 232, 0.18);
}

.inline-input--brand {
  font-size: 16px;
  font-weight: 700;
  width: 120px;
}

.inline-input--nav {
  font-size: 13.5px;
  font-weight: 600;
  width: 105px;
}

.inline-textarea--hero-title {
  font-size: 36px;
  font-weight: 700;
  color: #ffffff;
  background: rgba(16, 16, 24, 0.65);
  border-color: rgba(255, 255, 255, 0.35);
  max-width: 680px;
}

.inline-textarea--hero-sub {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  background: rgba(16, 16, 24, 0.55);
  border-color: rgba(255, 255, 255, 0.25);
  max-width: 640px;
}

.inline-input--hero-btn {
  font-size: 14px;
  font-weight: 600;
  width: 150px;
}

/* 业务模块通用画布样式 */
.canvas-block {
  padding: 0 0 64px;
  background: #ffffff;
}

.canvas-block--alt {
  background: #f5f7fa;
}

.is-disabled-block {
  opacity: 0.48;
}

.canvas-wrap {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px 0;
}

.sec-header-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  margin-bottom: 32px;
  text-align: center;
}

.inline-input--sec-title {
  font-size: 28px;
  font-weight: 700;
  text-align: center;
  width: min(420px, 100%);
}

.inline-input--sec-sub {
  font-size: 14px;
  color: #5f6470;
  text-align: center;
  width: min(560px, 100%);
}

/* 指标网格 */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 20px;
}

.metric-box {
  padding: 20px;
  border-radius: 14px;
  border: 1px solid rgba(16, 16, 20, 0.08);
  background: #ffffff;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.metric-box__num-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.inline-input--metric-val {
  font-size: 30px;
  font-weight: 700;
  color: #5b4be8;
  width: 130px;
}

.inline-input--metric-unit {
  font-size: 18px;
  font-weight: 700;
  width: 52px;
}

.metric-box__val {
  font-size: 40px;
  font-weight: 700;
  color: #101014;
}

.metric-box__val span {
  font-size: 22px;
  color: #5b4be8;
  margin-left: 3px;
}

.item-mini-bar {
  display: flex;
  gap: 6px;
  margin-top: 4px;
}

.item-mini-bar button,
.mini-btn,
.mini-del {
  font-size: 11px;
  padding: 2px 7px;
  border-radius: 4px;
  border: 1px solid #cbd5e1;
  background: #fff;
  color: #334155;
  cursor: pointer;
}

.mini-del,
.item-mini-bar button.danger {
  color: #dc2626;
  border-color: #fecaca;
}

/* 服务分组舞台 */
.service-group-stage {
  display: block;
  padding: 28px;
  border-radius: 18px;
  background: #ffffff;
  border: 1px solid rgba(16, 16, 20, 0.08);
  margin-bottom: 24px;
}

@media (max-width: 860px) {
  .service-group-stage {
    grid-template-columns: 1fr;
  }
}

.service-group-stage__info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.grp-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.inline-input--grp-title {
  font-size: 22px;
  font-weight: 700;
}

.grp-picker-wrap {
  background: #f8f9fc;
  padding: 12px;
  border-radius: 10px;
}

.grp-picker-label {
  font-size: 12px;
  color: #5f6470;
  margin-bottom: 8px;
}



.media-placeholder {
  color: #64748b;
  font-size: 13px;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
  margin-top: 14px;
}

.cards-grid--3 {
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
}

.live-card {
  border-radius: 12px;
  border: 1px solid rgba(16, 16, 20, 0.08);
  background: #ffffff;
  overflow: hidden;
}

.live-card__img {
  width: 100%;
  aspect-ratio: 16 / 9;
  object-fit: cover;
}

.live-card__body {
  padding: 12px;
}

.live-card__tag {
  font-size: 11px;
  color: #5b4be8;
  background: #eeebff;
  padding: 2px 8px;
  border-radius: 999px;
}

.live-card__body h4 {
  margin: 6px 0;
  font-size: 15px;
}

.live-card__go {
  font-size: 12px;
  color: #5b4be8;
}

.inline-picker-panel {
  max-width: 680px;
  margin: 0 auto 24px;
  padding: 14px;
  background: #f8f9fc;
  border-radius: 12px;
  border: 1px dashed rgba(91, 75, 232, 0.35);
}

.logo-wall {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 12px;
}

.logo-wall__item {
  height: 84px;
  border-radius: 10px;
  background: #ffffff;
  border: 1px solid rgba(16, 16, 20, 0.07);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10px;
  gap: 6px;
  font-size: 12px;
}

.logo-wall__item img {
  max-height: 36px;
  max-width: 110px;
  object-fit: contain;
}

.news-list-preview {
  display: flex;
  flex-direction: column;
}

.news-row-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 16px 0;
  border-bottom: 1px solid rgba(16, 16, 20, 0.08);
}

.news-row-preview__date {
  font-size: 13px;
  color: #8b90a0;
}

.news-row-preview__title {
  flex: 1;
  font-size: 16px;
}

.news-row-preview__arrow {
  font-size: 13px;
  color: #5b4be8;
}

/* 底部咨询区与页脚 */
.canvas-cta {
  background: linear-gradient(135deg, #10121d 0%, #1c1738 100%);
  color: #ffffff;
  padding-bottom: 56px;
}

.canvas-cta__inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  text-align: center;
}

.inline-input--cta-title {
  font-size: 28px;
  font-weight: 700;
  text-align: center;
  width: min(480px, 100%);
}

.inline-input--cta-sub {
  font-size: 14px;
  text-align: center;
  width: min(540px, 100%);
}

.canvas-footer {
  background: #090a0f;
  color: #a0a4b8;
  padding-bottom: 36px;
}

.canvas-footer__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 24px;
}

.footer-col {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.footer-col h4,
.footer-col strong {
  color: #ffffff;
  font-size: 15px;
}

.fg-head,
.fg-link-row {
  display: flex;
  align-items: center;
  gap: 4px;
}

.inline-input--dark,
.inline-textarea--dark {
  background: rgba(255, 255, 255, 0.08);
  color: #ffffff;
  border-color: rgba(255, 255, 255, 0.22);
  width: 100%;
}

.inline-input--dark-lg {
  background: rgba(255, 255, 255, 0.1);
  color: #ffffff;
  font-size: 18px;
  font-weight: 700;
}

.dark-field {
  display: flex;
  flex-direction: column;
  gap: 3px;
  font-size: 11px;
}

.footer-phone {
  font-size: 22px;
  font-weight: 700;
  color: #ffffff;
}

.canvas-footer__bottom {
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  font-size: 12px;
}

.health-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.health-item {
  padding: 12px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  cursor: pointer;
}

.health-item.is-error {
  background: #fef2f2;
  border-color: #fecaca;
}

.health-item.is-warning {
  background: #fffbeb;
  border-color: #fde68a;
}

.health-item__head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.health-item__msg {
  font-size: 13px;
  margin-bottom: 6px;
}

.health-item__go {
  font-size: 12px;
  color: #5b4be8;
  font-weight: 600;
}
</style>
