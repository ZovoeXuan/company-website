# 本次专属增量更新说明与源码对照 (`apply_latest_update.py`)

> **更新说明**：本份更新仅包含您本次专门指出的 **视觉与交互重构项**（共涉及 **11 个核心文件**），之前已提交的代码完全保留不受影响。

## 一、本次增量更新覆盖的文件清单

| 序号 | 模块端 | 文件路径 | 变更说明 |
| :--- | :--- | :--- | :--- |
| 1 | 前台动效 | `website/web/src/directives/reveal.ts` | 强化滚动流动式上浮弹出动效 (`v-reveal`) |
| 2 | 前台类型 | `website/web/src/data/types.ts` | 扩展 AboutPage 字段（使命愿景价值观、办公网点等），清除 ROI 类型 |
| 3 | 前台逻辑 | `website/web/src/composables/consult.ts` | 彻底移除所有 ROI 测算逻辑，保留纯净咨询弹窗 |
| 4 | 前台导航 | `website/web/src/components/SiteHeader.vue` | 1:1 对齐截图：全宽浅灰蓝通栏 5 列下拉大菜单，右侧 English/小七/产品试用 |
| 5 | 前台悬浮 | `website/web/src/components/FloatingActions.vue` | 移除 ROI 测算按钮，保留在线咨询、热线气泡与返回顶部 |
| 6 | 前台布局 | `website/web/src/layouts/SiteLayout.vue` | 移除 RoiCalculatorModal 引用与挂载 |
| 7 | 前台首页 | `website/web/src/views/HomeView.vue` | 移除首屏 90 秒与标签、移除底部 01/02 文字条；核心产品去掉侧边配图框；全页流动弹出动效 |
| 8 | 关于我们 | `website/web/src/views/AboutView.vue` | 1:1 像素级复刻 7 张截图：深空流光Banner、5页签导航、企业简介左文右图、使命愿景价值观逐行弹出、发展历程横轴轮播、新闻4卡轮播、加入我们海报、联系我们地图照片 |
| 9 | 后台首页 | `website/admin/src/views/HomeManageView.vue` | 移除核心产品分组配图上传项，保持表单与前台同步 |
| 10 | 实景预演 | `website/admin/src/views/PreviewHomeView.vue` | 移除核心产品右侧配图占位框，恢复三列清爽卡片展示 |
| 11 | Java 后端 | `website/server/src/main/java/com/company/cms/site/SiteConfigValidator.java` | 扩展关于我们 (ABOUT) 配置草稿校验，支持愿景、价值观、办公网点等新字段持久化 |

## 二、本地一键应用步骤

1. 在右侧面板下载 **`apply_latest_update.py`**，放入本地仓库根目录（例如 `D:\中转站\公司官网\apply_latest_update.py`）。
2. 在终端运行：
   ```bash
   python apply_latest_update.py
   ```
   *脚本会自动备份即将被更新的 11 个文件，并自动清理旧的 `RoiCalculatorModal.vue`。*
3. 提交推送到 GitHub：
   ```bash
   git add .
   git commit -m "feat: 优化首屏动效、重构产品分组与关于我们页面"
   git push origin master
   ```

## 三、增量文件完整源码参考

### `website/web/src/directives/reveal.ts`

```typescript
import type { Directive } from 'vue'

let observer: IntersectionObserver | null = null

function getObserver(): IntersectionObserver | null {
  if (typeof IntersectionObserver === 'undefined') return null
  if (!observer) {
    observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            entry.target.classList.add('is-revealed')
          }
        }
      },
      { threshold: 0.1, rootMargin: '0px 0px -6% 0px' },
    )
  }
  return observer
}

/** v-reveal：滑到哪里缓慢浮现弹出，可选数字表示延迟毫秒。 */
export const vReveal: Directive<HTMLElement, number | undefined> = {
  mounted(el, binding) {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      el.classList.add('is-revealed')
      return
    }
    el.setAttribute('data-reveal', '')
    if (binding.value) {
      el.style.setProperty('--reveal-delay', `${binding.value}ms`)
    }
    getObserver()?.observe(el)
  },
  unmounted(el) {
    observer?.unobserve(el)
  },
}
```

### `website/web/src/data/types.ts`

```typescript
/**
 * 与后端公开 DTO 对齐的展示类型。
 */

export interface MediaRef {
  id: string
  url: string
  alt: string
  width: number
  height: number
  type?: 'IMAGE' | 'VIDEO'
  durationMs?: number | null
}

export interface PageResult<T> {
  items: T[]
  page: number
  pageSize: number
  total: number
}

export interface NavItem {
  label: string
  path: string
  url?: string | null
  newWindow?: boolean
  linkType?: string | null
  children?: NavItem[]
}

export interface MegaSubItem {
  label: string
  path?: string
  tag?: string
  isVideo?: boolean
  videoUrl?: string
  videoTitle?: string
}

export interface MegaColumn {
  title: string
  items: MegaSubItem[]
}

export interface QrCodeItem {
  label: string
  imageUrl?: string | null
}

export interface SiteConfig {
  siteName: string
  slogan: string
  logoUrl?: string | null
  logoAlt?: string
  faviconUrl?: string | null
  defaultSeo?: { title: string; description: string }
  nav: NavItem[]
  megaColumns?: MegaColumn[]
  megaCta?: { text: string; linkText: string } | null
  contact: {
    phone: string
    email: string
    address: string
    hours: string
  }
  footer: {
    copyright: string
    icp: string
    icpUrl?: string
    policeText?: string
    policeUrl?: string
    groups: Array<{ title: string; links: NavItem[] }>
    links: NavItem[]
    qrCodes?: QrCodeItem[]
  }
  formEnabled?: boolean
  privacyVersion?: number | null
}

export interface HomeLink {
  type: string
  url?: string
  newWindow?: boolean
  contentType?: string
  contentId?: string
}

export interface HomeBlock {
  title: string
  subtitle: string
}

export interface HomeHeroItem {
  key: string
  title: string
  subtitle: string
  desktopImage: MediaRef | null
  mobileImage: MediaRef | null
  buttonText: string
  link: HomeLink | null
}

export interface HomeMetric {
  key: string
  displayValue: string
  unit: string
  label: string
  description: string
}

export interface HomeCard {
  id: string
  contentType?: string
  title: string
  summary: string
  cover: MediaRef | null
  url: string
  customerName?: string
  industry?: string
  outcomes?: Array<{ displayValue: string; label: string }>
}

export interface HomeServiceGroup {
  key: string
  title: string
  description: string
  image: MediaRef | null
  moreLink: HomeLink | null
  items: HomeCard[]
}

export interface HomeCustomer {
  id: string
  name: string
  logo: MediaRef
  url: string | null
}

export interface HomeNewsItem {
  id: string
  title: string
  summary: string
  displayTime: string
  url: string
  cover?: MediaRef | null
}

export interface HomeView {
  version: number | null
  publishedAt: string | null
  sectionOrder?: string[]
  hero: { items: HomeHeroItem[] }
  metrics: (HomeBlock & { items: HomeMetric[] }) | null
  services: (HomeBlock & { groups: HomeServiceGroup[] }) | null
  cases: (HomeBlock & { items: HomeCard[]; moreUrl: string }) | null
  customers: (HomeBlock & { items: HomeCustomer[] }) | null
  news: (HomeBlock & { mode: string; items: HomeNewsItem[]; moreUrl: string }) | null
  consultation: { title: string; subtitle: string; buttonText: string; link: HomeLink }
}

export interface Category {
  id: string
  name: string
}

export interface SeoMeta {
  title?: string
  description?: string
}

export interface TitledItem {
  title: string
  description: string
  icon?: MediaRef | null
}

export interface OutcomeMetric {
  displayValue: string
  label: string
}

export interface ContentSummary {
  id: string
  title: string
  summary: string
  cover: MediaRef | null
  coverAlt?: string
  categoryId: string | null
  categoryName: string | null
  industry?: string
  customerName?: string
  outcomes?: OutcomeMetric[]
  displayTime?: string
}

export interface ProductSummary extends ContentSummary {}

export interface ProductDetail extends ProductSummary {
  bodyHtml?: string
  seo?: SeoMeta
  hero?: MediaRef | null
  features?: TitledItem[]
  relatedCases?: CaseSummary[]
}

export interface SolutionSummary extends ContentSummary {}

export interface SolutionDetail extends SolutionSummary {
  bodyHtml?: string
  seo?: SeoMeta
  hero?: MediaRef | null
  pains?: TitledItem[]
  benefits?: TitledItem[]
  relatedProducts?: ProductSummary[]
}

export interface CaseSummary extends ContentSummary {}

export interface CaseDetail extends CaseSummary {
  bodyHtml?: string
  seo?: SeoMeta
  hero?: MediaRef | null
  quoteText?: string
  quoteAuthor?: string
  relatedProducts?: ProductSummary[]
  relatedSolutions?: SolutionSummary[]
}

export interface NewsSummary extends ContentSummary {}

export interface NewsDetail extends NewsSummary {
  bodyHtml?: string
  seo?: SeoMeta
  author?: string
  sourceName?: string
  sourceUrl?: string
  prev?: { id: string; title: string } | null
  next?: { id: string; title: string } | null
}

export interface AboutValueItem {
  title: string
  lines: string[]
}

export interface AboutOfficeItem {
  city: string
  phone: string
  address: string
  image?: string
  mapX?: number
  mapY?: number
}

export interface AboutPage {
  heroTitle?: string
  introTitle?: string
  intro: string
  introImage?: string
  mission: string
  vision?: string
  values?: AboutValueItem[]
  milestones: Array<{ year: string; title: string; description: string }>
  qualifications: Array<{ name: string; description: string }>
  joinTitle?: string
  joinButtonText?: string
  offices?: AboutOfficeItem[]
}

export interface PrivacyPage {
  updatedAt: string
  bodyHtml: string
}

export interface ContactPage {
  title: string
  description: string
  successText: string
  formEnabled: boolean
  phone?: string
  email?: string
  address?: string
  hours?: string
}

export interface LeadSubmitPayload {
  name: string
  phone: string
  companyName: string
  demand: string
  sourcePath: string
  sourcePosition: string
  contentType?: string
  contentId?: number
  consent: boolean
  consentVersion: number
}

export interface LeadSubmitResult {
  accepted: boolean
  receiptNo: string
}
```

### `website/web/src/composables/consult.ts`

```typescript
import { reactive } from 'vue'
import { getContactPage, getSite } from '@/api/public'

export interface ConsultOpenOptions {
  sourcePosition: string
  contentType?: 'PRODUCT' | 'SOLUTION' | 'CASE'
  contentId?: string | number
  title?: string
  initialDemand?: string
}

const dialog = reactive({
  open: false,
  sourcePosition: 'NAVIGATION',
  contentType: undefined as 'PRODUCT' | 'SOLUTION' | 'CASE' | undefined,
  contentId: undefined as number | undefined,
  title: '',
  initialDemand: '',
})

export function openConsult(options: ConsultOpenOptions) {
  dialog.sourcePosition = options.sourcePosition
  dialog.contentType = options.contentType
  dialog.contentId = options.contentId != null ? Number(options.contentId) : undefined
  dialog.title = options.title ?? ''
  dialog.initialDemand = options.initialDemand ?? ''
  dialog.open = true
}

export function closeConsult() {
  dialog.open = false
}

export function useConsultDialog() {
  return { dialog, openConsult, closeConsult }
}

export interface ConsultContext {
  formEnabled: boolean
  privacyVersion: number | null
  successText: string
  phone: string
  email: string
}

/** 打开表单时实时拉取当前发布态，不把 formEnabled 缓存在 localStorage。 */
export async function loadConsultContext(): Promise<ConsultContext> {
  const [site, contact] = await Promise.all([
    getSite(),
    getContactPage().catch(() => null),
  ])
  return {
    formEnabled: Boolean(site.formEnabled ?? contact?.formEnabled),
    privacyVersion: site.privacyVersion ?? null,
    successText: contact?.successText || '感谢您的咨询，我们的顾问将在 1 个工作日内与您联系。',
    phone: contact?.phone || site.contact?.phone || '',
    email: contact?.email || site.contact?.email || '',
  }
}
```

### `website/web/src/components/SiteHeader.vue`

```vue
<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { openConsult } from '@/composables/consult'
import { openVideo } from '@/composables/videoModal'
import type { MegaColumn, MegaSubItem, NavItem } from '@/data/types'

const props = defineProps<{
  siteName: string
  nav: NavItem[]
  logoUrl?: string | null
  logoAlt?: string
  megaColumns?: MegaColumn[]
  megaCta?: { text: string; linkText: string } | null
}>()

const route = useRoute()
const open = ref(false)
const openMenu = ref<string | null>(null)
const openMobile = ref<string | null>(null)
const scrolled = ref(false)
const lang = ref<'zh' | 'en'>('zh')

const enLabelMap: Record<string, string> = {
  首页: 'Home',
  产品: 'Products',
  产品中心: 'Products',
  产品与方案: 'Products & Solutions',
  产品与解决方案: 'Products & Solutions',
  解决方案: 'Solutions',
  客户案例: 'Customer Cases',
  新闻: 'News',
  新闻中心: 'News Center',
  新闻动态: 'News',
  关于我们: 'About Us',
  联系我们: 'Contact',
}

function displayNavLabel(label: string) {
  if (lang.value === 'zh') return label
  return enLabelMap[label] || label
}

function toggleLang() {
  lang.value = lang.value === 'zh' ? 'en' : 'zh'
  document.documentElement.lang = lang.value === 'en' ? 'en' : 'zh-CN'
}

function close() {
  open.value = false
  openMobile.value = null
  openMenu.value = null
}

function onScroll() {
  scrolled.value = window.scrollY > 24
}

function onKeydown(event: KeyboardEvent) {
  if (event.key !== 'Escape') return
  if (openMenu.value) {
    openMenu.value = null
    return
  }
  if (open.value) close()
}

let leaveTimer: number | null = null

function isMegaItem(item: NavItem) {
  return Boolean(item.children?.length) || item.label.includes('产品') || item.label.includes('方案')
}

function handleMouseEnter(item: NavItem) {
  if (!isMegaItem(item)) {
    openMenu.value = null
    return
  }
  if (leaveTimer) {
    clearTimeout(leaveTimer)
    leaveTimer = null
  }
  openMenu.value = item.label
}

function handleMouseLeave() {
  if (leaveTimer) clearTimeout(leaveTimer)
  leaveTimer = window.setTimeout(() => {
    openMenu.value = null
  }, 180)
}

function toggleMenu(item: NavItem) {
  openMenu.value = openMenu.value === item.label ? null : item.label
}

const defaultMegaColumns: MegaColumn[] = [
  {
    title: 'AI产品',
    items: [{ label: '紫宝盒', path: '/products/1', isVideo: false }],
  },
  {
    title: 'IoT服务',
    items: [
      { label: '安全管家', path: '/products?category=安全', isVideo: false },
      { label: '冷链管家', path: '/products?category=冷链', isVideo: false },
      { label: '流向管家', path: '/products?category=流向', isVideo: false },
      { label: '追货管家', path: '/products?category=追货', isVideo: false },
    ],
  },
  {
    title: '交易服务',
    items: [
      { label: '财运通', path: '/solutions?category=交易', isVideo: false },
      { label: '数字能源', path: '/solutions?category=能源', isVideo: false },
    ],
  },
  {
    title: '解决方案',
    items: [
      { label: '大宗物流数字化', path: '/solutions/1', isVideo: false },
      { label: '连锁零售数字化', path: '/solutions/2', isVideo: false },
      { label: '饲料防偷盗 AI 治理', path: '/solutions/3', isVideo: false },
    ],
  },
  {
    title: '产品演示',
    items: [{ label: '产品操作视频', path: '', isVideo: true }],
  },
]

const resolvedMegaColumns = computed<MegaColumn[]>(() => {
  if (props.megaColumns && props.megaColumns.length > 0) {
    return props.megaColumns
  }
  return defaultMegaColumns
})

function isTwoColGrid(col: MegaColumn): boolean {
  return col.title.includes('IoT') || col.title.includes('交易') || col.items.length >= 4
}

function handleMegaAction(subItem: MegaSubItem) {
  openMenu.value = null
  if (subItem.isVideo) {
    openVideo({
      title: subItem.videoTitle || '产品操作视频演示',
      url: subItem.videoUrl || 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
    })
  }
}

function consult(position = 'NAVIGATION') {
  openMenu.value = null
  openMobile.value = null
  openConsult({ sourcePosition: position })
}

function isActive(item: NavItem): boolean {
  if (!item.path) return false
  if (item.path === '/') return route.path === '/'
  return route.path === item.path || route.path.startsWith(`${item.path}/`)
}

onMounted(() => {
  document.addEventListener('keydown', onKeydown)
  window.addEventListener('scroll', onScroll, { passive: true })
  onScroll()
})
onBeforeUnmount(() => {
  document.removeEventListener('keydown', onKeydown)
  window.removeEventListener('scroll', onScroll)
})
</script>

<template>
  <header class="header" :class="{ 'is-scrolled': scrolled }">
    <div class="header__inner container container--wide">
      <RouterLink to="/" class="brand" :aria-label="`${props.siteName} 首页`">
        <img v-if="logoUrl" :src="logoUrl" :alt="logoAlt || siteName" class="brand__logo" />
        <template v-else>
          <span class="brand__g7" aria-hidden="true">G7</span>
          <span class="brand__name">{{ siteName }}</span>
        </template>
      </RouterLink>

      <nav class="nav" aria-label="主导航">
        <ul class="nav__list">
          <li
            v-for="item in nav"
            :key="item.label"
            class="nav__item"
            @mouseenter="handleMouseEnter(item)"
            @mouseleave="handleMouseLeave"
          >
            <template v-if="isMegaItem(item)">
              <button
                type="button"
                class="nav__link nav__button"
                :class="{ 'is-active': isActive(item) || openMenu === item.label, 'is-open': openMenu === item.label }"
                :aria-expanded="openMenu === item.label"
                @click="toggleMenu(item)"
              >
                {{ displayNavLabel(item.label) }}
                <span class="caret" aria-hidden="true"></span>
              </button>

              <!-- 1:1 还原截图 image_1249a6.png：全宽浅灰蓝通栏 5 列下拉大菜单 -->
              <Transition name="mega-band">
                <div
                  v-if="openMenu === item.label"
                  class="mega-band"
                  @mouseenter="handleMouseEnter(item)"
                  @mouseleave="handleMouseLeave"
                >
                  <div class="container container--wide">
                    <template v-if="item.label.includes('产品') || item.label.includes('方案')">
                      <div class="mega-cols">
                        <div v-for="col in resolvedMegaColumns" :key="col.title" class="mega-col">
                          <div class="mega-col__title">{{ col.title }}</div>
                          <ul
                            class="mega-col__list"
                            :class="{ 'mega-col__list--2col': isTwoColGrid(col) }"
                          >
                            <li v-for="sub in col.items" :key="sub.label">
                              <button
                                v-if="sub.isVideo"
                                type="button"
                                class="mega-col__link"
                                @click="handleMegaAction(sub)"
                              >
                                {{ sub.label }}
                              </button>
                              <RouterLink
                                v-else-if="sub.path"
                                :to="sub.path"
                                class="mega-col__link"
                                @click="openMenu = null"
                              >
                                {{ sub.label }}
                              </RouterLink>
                              <span v-else class="mega-col__link">{{ sub.label }}</span>
                            </li>
                          </ul>
                        </div>
                      </div>
                    </template>

                    <!-- 常规多子项通栏 -->
                    <template v-else>
                      <ul class="mega-simple-list">
                        <li v-for="child in item.children" :key="child.label">
                          <button
                            v-if="child.linkType === 'CONSULT'"
                            type="button"
                            class="mega-col__link"
                            @click="consult()"
                          >
                            {{ displayNavLabel(child.label) }}
                          </button>
                          <a
                            v-else-if="child.url"
                            :href="child.url"
                            class="mega-col__link"
                            :target="child.newWindow ? '_blank' : undefined"
                            :rel="child.newWindow ? 'noopener noreferrer' : undefined"
                            @click="openMenu = null"
                          >
                            {{ displayNavLabel(child.label) }}
                          </a>
                          <RouterLink v-else :to="child.path" class="mega-col__link" @click="openMenu = null">
                            {{ displayNavLabel(child.label) }}
                          </RouterLink>
                        </li>
                      </ul>
                    </template>
                  </div>
                </div>
              </Transition>
            </template>

            <button
              v-else-if="item.linkType === 'CONSULT'"
              type="button"
              class="nav__link"
              @click="consult()"
            >
              {{ displayNavLabel(item.label) }}
            </button>
            <a
              v-else-if="item.url"
              :href="item.url"
              class="nav__link"
              :target="item.newWindow ? '_blank' : undefined"
              :rel="item.newWindow ? 'noopener noreferrer' : undefined"
            >
              {{ displayNavLabel(item.label) }}
            </a>
            <RouterLink
              v-else
              :to="item.path"
              class="nav__link"
              :class="{ 'is-active': isActive(item) }"
            >
              {{ displayNavLabel(item.label) }}
            </RouterLink>
          </li>
        </ul>
      </nav>

      <!-- 右侧区域：1:1 对齐截图 image_1249a6.png -->
      <div class="actions">
        <button type="button" class="lang" @click="toggleLang">
          {{ lang === 'zh' ? 'English' : '中文' }}
        </button>

        <button
          type="button"
          class="actions__assistant"
          title="点击在线交流"
          @click="consult('NAVIGATION')"
        >
          <span class="assistant__meta">
            <span class="assistant__sub">您好, 我是您的</span>
            <span class="assistant__name">数字物流专家 小七</span>
          </span>
          <span class="assistant__pin" aria-hidden="true">
            <span class="assistant__head">
              <span class="assistant__eye"></span>
              <span class="assistant__eye"></span>
            </span>
            <span class="assistant__shadow"></span>
          </span>
        </button>

        <button type="button" class="actions__cta" @click="consult('OTHER_CTA')">
          {{ lang === 'en' ? 'Free Trial' : '产品试用' }}
        </button>

        <button
          type="button"
          class="toggle"
          :aria-expanded="open"
          aria-controls="mobile-nav"
          @click="open = !open"
        >
          <span class="visually-hidden">{{ open ? '关闭菜单' : '打开菜单' }}</span>
          <span class="toggle__line" :class="{ 'is-open': open }"></span>
        </button>
      </div>
    </div>

    <Transition name="mobile">
      <nav v-if="open" id="mobile-nav" class="mobile" aria-label="移动端导航">
        <ul class="mobile__list">
          <li v-for="item in nav" :key="item.label">
            <template v-if="item.children?.length">
              <button
                type="button"
                class="mobile__link mobile__button"
                :aria-expanded="openMobile === item.label"
                @click="openMobile = openMobile === item.label ? null : item.label"
              >
                {{ displayNavLabel(item.label) }}
                <span class="caret" aria-hidden="true"></span>
              </button>
              <ul v-if="openMobile === item.label" class="mobile__sub">
                <li v-for="child in item.children" :key="child.label">
                  <button
                    v-if="child.linkType === 'CONSULT'"
                    type="button"
                    class="mobile__sublink mobile__sublink--button"
                    @click="consult()"
                  >
                    {{ displayNavLabel(child.label) }}
                  </button>
                  <a
                    v-else-if="child.url"
                    :href="child.url"
                    class="mobile__sublink"
                    :target="child.newWindow ? '_blank' : undefined"
                    :rel="child.newWindow ? 'noopener noreferrer' : undefined"
                    @click="close"
                  >
                    {{ displayNavLabel(child.label) }}
                  </a>
                  <RouterLink v-else :to="child.path" class="mobile__sublink" @click="close">
                    {{ displayNavLabel(child.label) }}
                  </RouterLink>
                </li>
              </ul>
            </template>
            <button
              v-else-if="item.linkType === 'CONSULT'"
              type="button"
              class="mobile__link"
              @click="consult()"
            >
              {{ displayNavLabel(item.label) }}
            </button>
            <a
              v-else-if="item.url"
              :href="item.url"
              class="mobile__link"
              :target="item.newWindow ? '_blank' : undefined"
              :rel="item.newWindow ? 'noopener noreferrer' : undefined"
              @click="close"
            >
              {{ displayNavLabel(item.label) }}
            </a>
            <RouterLink
              v-else
              :to="item.path"
              class="mobile__link"
              :class="{ 'is-active': isActive(item) }"
              @click="close"
            >
              {{ displayNavLabel(item.label) }}
            </RouterLink>
          </li>
        </ul>
        <button type="button" class="btn btn--primary mobile__cta" @click="consult('OTHER_CTA')">
          产品试用
        </button>
      </nav>
    </Transition>
  </header>
</template>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 40;
  background: #ffffff;
  border-bottom: 1px solid rgba(16, 16, 20, 0.05);
  transition: box-shadow 0.3s ease;
}

.header.is-scrolled {
  box-shadow: 0 4px 20px rgba(10, 12, 20, 0.05);
}

.header__inner {
  display: flex;
  align-items: center;
  gap: 44px;
  height: var(--header-height, 76px);
}

.brand {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: none;
}

.brand__logo {
  max-height: 32px;
  max-width: 160px;
  object-fit: contain;
}

.brand__g7 {
  font-size: 22px;
  font-weight: 900;
  font-style: italic;
  color: #4f2de2;
  letter-spacing: -0.04em;
}

.brand__name {
  font-size: 18px;
  font-weight: 800;
  color: #4f2de2;
  letter-spacing: -0.02em;
}

.nav {
  flex: 1;
  min-width: 0;
}

.nav__list {
  display: flex;
  align-items: center;
  gap: 38px;
  list-style: none;
  margin: 0;
  padding: 0;
}

.nav__item {
  position: static;
}

.nav__link {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 24px 0;
  font-size: 15px;
  color: #2b2f38;
  background: none;
  border: none;
  cursor: pointer;
  font-family: inherit;
  transition: color 0.2s ease;
}

.nav__link:hover,
.nav__link.is-active {
  color: #5b4be8;
}

.caret {
  width: 6px;
  height: 6px;
  border-right: 1.5px solid currentColor;
  border-bottom: 1.5px solid currentColor;
  transform: rotate(45deg) translateY(-2px);
  transition: transform 0.25s ease;
}

.nav__button.is-open .caret {
  transform: rotate(225deg) translateY(-2px);
}

/* 全宽浅灰蓝通栏下拉大菜单（1:1 对齐 image_1249a6.png） */
.mega-band {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  width: 100%;
  background: #eaedf2;
  padding: 40px 0 56px;
  border-top: 1px solid rgba(16, 16, 20, 0.05);
  box-shadow: 0 20px 40px rgba(10, 12, 20, 0.06);
  z-index: 35;
}

.mega-cols {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 36px;
  max-width: 1120px;
  margin: 0 auto;
}

.mega-col__title {
  font-size: 15px;
  font-weight: 500;
  color: #1f2329;
  padding-bottom: 14px;
  margin-bottom: 18px;
  border-bottom: 1px solid #d4d8e1;
}

.mega-col__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.mega-col__list--2col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px 20px;
}

.mega-col__link {
  display: inline-block;
  border: none;
  background: none;
  padding: 0;
  font-family: inherit;
  font-size: 13.5px;
  color: #5f6470;
  text-align: left;
  cursor: pointer;
  transition: color 0.18s ease, transform 0.18s ease;
}

.mega-col__link:hover {
  color: #5b4be8;
}

.mega-simple-list {
  list-style: none;
  margin: 0 auto;
  padding: 0;
  max-width: 1120px;
  display: flex;
  gap: 36px;
  flex-wrap: wrap;
}

.mega-band-enter-active,
.mega-band-leave-active {
  transition: opacity 0.24s ease, transform 0.24s cubic-bezier(0.22, 1, 0.36, 1);
}

.mega-band-enter-from,
.mega-band-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* 右侧操作区（1:1 还原截图） */
.actions {
  display: flex;
  align-items: center;
  gap: 24px;
  flex: none;
}

.lang {
  border: none;
  background: none;
  font-size: 13.5px;
  color: #2b2f38;
  cursor: pointer;
  padding: 4px 6px;
}

.lang:hover {
  color: #5b4be8;
}

.actions__assistant {
  display: flex;
  align-items: center;
  gap: 10px;
  background: none;
  border: none;
  padding: 4px 6px;
  cursor: pointer;
  font-family: inherit;
}

.assistant__meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  text-align: right;
  line-height: 1.3;
}

.assistant__sub {
  font-size: 11px;
  color: #4f7cff;
  font-weight: 500;
}

.assistant__name {
  font-size: 11.5px;
  font-weight: 600;
  color: #5b4be8;
}

.assistant__pin {
  position: relative;
  width: 36px;
  height: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.assistant__head {
  width: 32px;
  height: 32px;
  border-radius: 50% 50% 50% 15%;
  transform: rotate(-45deg);
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 55%, #3b82f6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  box-shadow: 0 6px 14px rgba(79, 70, 229, 0.38);
}

.assistant__eye {
  width: 4px;
  height: 6px;
  border-radius: 999px;
  background: #38bdf8;
  transform: rotate(45deg);
  box-shadow: 0 0 6px #38bdf8;
}

.assistant__shadow {
  margin-top: 3px;
  width: 22px;
  height: 4px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(79, 70, 229, 0.45) 0%, transparent 75%);
}

.actions__cta {
  height: 40px;
  padding: 0 24px;
  border: none;
  border-radius: 2px;
  background: #0a0b0e;
  color: #ffffff;
  font-size: 13.5px;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.actions__cta:hover {
  background: #5b4be8;
}

.toggle {
  display: none;
  width: 40px;
  height: 40px;
  align-items: center;
  justify-content: center;
  background: #fff;
  border: 1px solid var(--c-border-strong);
  border-radius: 10px;
  cursor: pointer;
}

.toggle__line,
.toggle__line::before,
.toggle__line::after {
  display: block;
  width: 18px;
  height: 1.5px;
  background: var(--c-text);
  transition: transform 0.25s ease, opacity 0.2s ease;
}

.toggle__line {
  position: relative;
}

.toggle__line::before,
.toggle__line::after {
  content: '';
  position: absolute;
  left: 0;
}

.toggle__line::before {
  top: -6px;
}

.toggle__line::after {
  top: 6px;
}

.toggle__line.is-open {
  background: transparent;
}

.toggle__line.is-open::before {
  top: 0;
  transform: rotate(45deg);
}

.toggle__line.is-open::after {
  top: 0;
  transform: rotate(-45deg);
}

.mobile {
  display: none;
  border-top: 1px solid var(--c-border);
  background: #fff;
  padding: 12px 20px 24px;
}

.mobile__list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.mobile__link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 15px 4px;
  font-size: 16px;
  color: var(--c-text);
  border: none;
  border-bottom: 1px solid var(--c-border);
  background: none;
  text-align: left;
}

.mobile__sub {
  list-style: none;
  margin: 0;
  padding: 6px 0 10px 14px;
}

.mobile__sublink {
  display: block;
  padding: 10px 4px;
  font-size: 14px;
  color: var(--c-text-muted);
}

.mobile__cta {
  margin-top: 16px;
  width: 100%;
}

@media (max-width: 980px) {
  .nav,
  .actions__assistant {
    display: none;
  }
  .toggle {
    display: inline-flex;
  }
  .mobile {
    display: block;
  }
  .actions {
    margin-left: auto;
    gap: 12px;
  }
}
</style>
```

### `website/web/src/components/FloatingActions.vue`

```vue
<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { openConsult } from '@/composables/consult'
import type { SiteConfig } from '@/data/types'

const props = defineProps<{
  site?: SiteConfig | null
}>()

const showPhoneCard = ref(false)
const showTopBtn = ref(false)

const phone = computed(() => props.site?.contact?.phone || '400-888-0000')
const email = computed(() => props.site?.contact?.email || 'connect@g7.com.cn')
const hours = computed(() => props.site?.contact?.hours || '周一至周日 08:30 - 20:30')

function onScroll() {
  const top = window.scrollY || document.documentElement.scrollTop
  showTopBtn.value = top > 300
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true })
  onScroll()
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<template>
  <aside class="float-dock" aria-label="快捷联系与返回顶部">
    <button
      type="button"
      class="dock-btn dock-btn--ai"
      title="在线咨询"
      @click="openConsult({ sourcePosition: 'FLOATING' })"
    >
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
      </svg>
      <span class="dock-btn__label">在线咨询</span>
    </button>

    <div
      class="dock-popover-wrap"
      @mouseenter="showPhoneCard = true"
      @mouseleave="showPhoneCard = false"
    >
      <button
        type="button"
        class="dock-btn"
        aria-label="查看联系电话"
        @click="showPhoneCard = !showPhoneCard"
      >
        <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"></path>
        </svg>
        <span class="dock-btn__label">热线电话</span>
      </button>

      <Transition name="pop">
        <div v-if="showPhoneCard" class="dock-card">
          <p class="dock-card__title">服务热线</p>
          <a :href="`tel:${phone}`" class="dock-card__phone">{{ phone }}</a>
          <p class="dock-card__meta">服务时间：{{ hours }}</p>
          <p class="dock-card__meta">联系邮箱：{{ email }}</p>
        </div>
      </Transition>
    </div>

    <Transition name="pop">
      <button
        v-if="showTopBtn"
        type="button"
        class="dock-btn"
        title="回到顶部"
        @click="scrollToTop"
      >
        <span class="dock-btn__top-arrow">↑</span>
        <span class="dock-btn__label">顶部</span>
      </button>
    </Transition>
  </aside>
</template>

<style scoped>
.float-dock {
  position: fixed;
  right: 20px;
  bottom: 28px;
  z-index: 45;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.dock-btn {
  width: 54px;
  height: 54px;
  border-radius: 14px;
  border: 1px solid rgba(16, 16, 20, 0.1);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  color: #101014;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  cursor: pointer;
  box-shadow: 0 8px 24px rgba(10, 12, 20, 0.08);
  transition: transform 0.2s ease, border-color 0.2s ease, color 0.2s ease;
}

.dock-btn:hover {
  transform: translateY(-2px);
  border-color: #5b4be8;
  color: #5b4be8;
}

.dock-btn--ai {
  background: #5b4be8;
  color: #ffffff;
  border-color: transparent;
}

.dock-btn--ai:hover {
  background: #4636d6;
  color: #ffffff;
}

.dock-btn__label {
  font-size: 10px;
  font-weight: 500;
  line-height: 1;
}

.dock-btn__top-arrow {
  font-size: 16px;
  font-weight: 700;
  line-height: 1;
}

.dock-popover-wrap {
  position: relative;
}

.dock-card {
  position: absolute;
  right: calc(100% + 12px);
  bottom: 0;
  width: 240px;
  background: #ffffff;
  border: 1px solid rgba(16, 16, 20, 0.1);
  border-radius: 14px;
  padding: 16px 18px;
  box-shadow: 0 18px 40px rgba(10, 12, 20, 0.14);
}

.dock-card__title {
  font-size: 12px;
  color: #8b90a0;
  margin: 0 0 4px;
}

.dock-card__phone {
  display: block;
  font-size: 19px;
  font-weight: 700;
  color: #5b4be8;
  margin-bottom: 6px;
}

.dock-card__meta {
  font-size: 12px;
  color: #5f6470;
  margin: 3px 0;
}

.pop-enter-active,
.pop-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.pop-enter-from,
.pop-leave-to {
  opacity: 0;
  transform: translateY(6px);
}
</style>
```

### `website/web/src/layouts/SiteLayout.vue`

```vue
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import ConsultDialog from '@/components/ConsultDialog.vue'
import FloatingActions from '@/components/FloatingActions.vue'
import VideoModal from '@/components/VideoModal.vue'
import { ApiError } from '@/api/http'
import { getSite } from '@/api/public'
import type { SiteConfig } from '@/data/types'

const route = useRoute()
const config = ref<SiteConfig | null>(null)
const notice = ref('')

async function loadSite() {
  notice.value = ''
  try {
    config.value = await getSite()
  } catch (error) {
    notice.value =
      error instanceof ApiError && error.code === 'NOT_FOUND'
        ? '站点内容尚未发布，请管理员在后台完成「全站设置」并发布。'
        : '站点信息加载失败，请稍后刷新重试。'
  }
}

onMounted(loadSite)
</script>

<template>
  <div class="site">
    <a class="skip-link" href="#main">跳到主要内容</a>

    <SiteHeader
      v-if="config"
      :site-name="config.siteName"
      :nav="config.nav"
      :logo-url="config.logoUrl"
      :logo-alt="config.logoAlt"
      :mega-columns="config.megaColumns"
      :mega-cta="config.megaCta"
    />

    <main id="main" class="site__main" tabindex="-1">
      <div v-if="notice" class="container notice" role="status">
        <span>{{ notice }}</span>
        <button type="button" class="notice__retry" @click="loadSite">重试</button>
      </div>
      <RouterView v-slot="{ Component }">
        <Transition name="page" mode="out-in">
          <component :is="Component" :key="route.fullPath" />
        </Transition>
      </RouterView>
    </main>

    <SiteFooter v-if="config" :config="config" />

    <FloatingActions :site="config" />

    <ConsultDialog />
    <VideoModal />
  </div>
</template>

<style scoped>
.site {
  display: flex;
  flex-direction: column;
  min-height: 100dvh;
}

.site__main {
  flex: 1;
  outline: none;
}

.notice {
  margin-top: 16px;
  padding: 12px 16px;
  border: 1px solid #f0e0bd;
  background: #fdf6e3;
  border-radius: 8px;
  color: #8a6d1f;
  font-size: 14px;
  line-height: 1.7;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.notice__retry {
  padding: 4px 12px;
  border-radius: 6px;
  border: 1px solid #d8be82;
  background: #fff;
  color: #8a6d1f;
  font-size: 12.5px;
  cursor: pointer;
}
</style>
```

### `website/web/src/views/HomeView.vue`

```vue
<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ApiError } from '@/api/http'
import { getHome } from '@/api/public'
import { openConsult } from '@/composables/consult'
import { usePageMeta } from '@/composables/usePageMeta'
import { vReveal } from '@/directives/reveal'
import EmptyState from '@/components/EmptyState.vue'
import type { HomeLink, HomeView } from '@/data/types'

const router = useRouter()
const data = ref<HomeView | null>(null)
const loading = ref(true)
const error = ref('')
const heroIndex = ref(0)
let heroTimer: number | null = null

usePageMeta(() => ({
  title: '首页',
  description: '致力于以数字化与智能技术助力企业高效运营与持续增长。',
}))

const DEFAULT_SECTION_ORDER = ['metrics', 'services', 'cases', 'customers', 'news', 'consultation']

const orderedSections = computed<string[]>(() => {
  const custom = data.value?.sectionOrder
  if (!custom || !Array.isArray(custom) || custom.length === 0) {
    return DEFAULT_SECTION_ORDER
  }
  const merged = [...custom.filter((k) => DEFAULT_SECTION_ORDER.includes(k))]
  for (const defKey of DEFAULT_SECTION_ORDER) {
    if (!merged.includes(defKey)) merged.push(defKey)
  }
  return merged
})

const heroes = computed(() => data.value?.hero?.items ?? [])
const currentHero = computed(() => heroes.value[heroIndex.value] ?? null)

function startHeroRotation() {
  stopHeroRotation()
  if (heroes.value.length <= 1) return
  heroTimer = window.setInterval(() => {
    heroIndex.value = (heroIndex.value + 1) % heroes.value.length
  }, 6500)
}

function stopHeroRotation() {
  if (heroTimer) {
    clearInterval(heroTimer)
    heroTimer = null
  }
}

function handleAction(link: HomeLink | null | undefined, sourcePosition = 'HOME_HERO') {
  if (!link) return
  if (link.type === 'CONSULT') {
    openConsult({
      sourcePosition,
      contentType: link.contentType as 'PRODUCT' | 'SOLUTION' | 'CASE' | undefined,
      contentId: link.contentId,
    })
    return
  }
  if (link.type === 'ANCHOR' && link.url) {
    const targetId = link.url.replace(/^#/, '')
    const el = document.getElementById(targetId)
    if (el) {
      el.scrollIntoView({ behavior: 'smooth' })
      return
    }
  }
  if (link.url) {
    if (link.url.startsWith('http://') || link.url.startsWith('https://')) {
      if (link.newWindow) {
        window.open(link.url, '_blank', 'noopener,noreferrer')
      } else {
        window.location.href = link.url
      }
    } else {
      router.push(link.url)
    }
  }
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    data.value = await getHome()
    heroIndex.value = 0
    startHeroRotation()
  } catch (err) {
    data.value = null
    error.value =
      err instanceof ApiError && err.code === 'NOT_FOUND'
        ? '首页内容尚未发布，请管理员在后台「首页管理」或「实景交互预演台」完成配置并发布。'
        : err instanceof ApiError
        ? err.message
        : '首页数据加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

onMounted(load)
onBeforeUnmount(stopHeroRotation)
</script>

<template>
  <div class="home-page">
    <div v-if="loading" class="container state">
      <div class="skeleton"></div>
    </div>

    <div v-else-if="error" class="container state">
      <EmptyState title="首页暂不可用" :description="error">
        <button type="button" class="btn btn--primary" @click="load">重新加载</button>
      </EmptyState>
    </div>

    <template v-else-if="data">
      <!-- 1. 首屏 Hero（干净大气：去除顶部胶囊与观看90秒按钮、去除底部 01/02 文字条，滑入缓慢浮现） -->
      <section v-if="currentHero" class="hero">
        <div class="hero__media">
          <video
            v-if="currentHero.desktopImage?.type === 'VIDEO'"
            :key="currentHero.desktopImage.url"
            :src="currentHero.desktopImage.url"
            class="hero__bg"
            autoplay
            muted
            loop
            playsinline
          ></video>
          <picture v-else-if="currentHero.desktopImage">
            <source
              v-if="currentHero.mobileImage?.url"
              media="(max-width: 768px)"
              :srcset="currentHero.mobileImage.url"
            />
            <img
              :src="currentHero.desktopImage.url"
              :alt="currentHero.desktopImage.alt || currentHero.title"
              class="hero__bg"
              fetchpriority="high"
            />
          </picture>
          <div class="hero__overlay"></div>
        </div>

        <div class="container container--wide hero__content">
          <h1 v-reveal class="hero__title">{{ currentHero.title }}</h1>
          <p v-if="currentHero.subtitle" v-reveal="100" class="hero__subtitle">
            {{ currentHero.subtitle }}
          </p>

          <div v-if="currentHero.buttonText && currentHero.link" v-reveal="180" class="hero__actions">
            <button
              type="button"
              class="btn btn--outline-light hero__btn"
              @click="handleAction(currentHero.link, 'HOME_HERO')"
            >
              <span>{{ currentHero.buttonText }}</span>
              <span aria-hidden="true">→</span>
            </button>
          </div>
        </div>
      </section>

      <!-- 按后台编排的 sectionOrder 动态渲染各大区块，每块与内部卡片均配流动式 v-reveal -->
      <template v-for="secKey in orderedSections" :key="secKey">
        <!-- A. 企业关键指标 (metrics) -->
        <section
          v-if="secKey === 'metrics' && data.metrics?.items?.length"
          id="metrics"
          class="section section--tight metrics-section"
        >
          <div class="container container--wide">
            <div v-if="data.metrics.title || data.metrics.subtitle" v-reveal class="section-head">
              <div>
                <h2 v-if="data.metrics.title" class="section-title">{{ data.metrics.title }}</h2>
                <p v-if="data.metrics.subtitle" class="section-sub">{{ data.metrics.subtitle }}</p>
              </div>
            </div>

            <ul class="metrics-grid">
              <li
                v-for="(item, mIdx) in data.metrics.items"
                :key="item.key"
                v-reveal="mIdx * 90"
                class="metric-item"
              >
                <div class="metric-item__val-row">
                  <span class="metric-item__value">{{ item.displayValue }}</span>
                  <span v-if="item.unit" class="metric-item__unit">{{ item.unit }}</span>
                </div>
                <p class="metric-item__label">{{ item.label }}</p>
                <p v-if="item.description" class="metric-item__desc">{{ item.description }}</p>
              </li>
            </ul>
          </div>
        </section>

        <!-- B. 核心产品与解决方案 (services) — 回归清爽经典结构：去除分组旁边丑陋的大配图框，直接展示分组标题与三列卡片 -->
        <section
          v-else-if="secKey === 'services' && data.services?.groups?.length"
          id="services"
          class="section services-section"
        >
          <div class="container container--wide">
            <div v-reveal class="section-head">
              <div>
                <span class="kicker">PRODUCTS & SOLUTIONS</span>
                <h2 class="section-title">{{ data.services.title || '核心产品与解决方案' }}</h2>
                <p v-if="data.services.subtitle" class="section-sub">{{ data.services.subtitle }}</p>
              </div>
              <RouterLink to="/products" class="link-more section-head__link">查看全部</RouterLink>
            </div>

            <div class="service-groups">
              <div
                v-for="(group, gIdx) in data.services.groups"
                :key="group.key"
                v-reveal="gIdx * 80"
                class="service-group"
              >
                <div class="service-group__header">
                  <div>
                    <span class="service-group__index">0{{ gIdx + 1 }}</span>
                    <h3 class="service-group__title">{{ group.title }}</h3>
                    <p v-if="group.description" class="service-group__desc">{{ group.description }}</p>
                  </div>
                  <button
                    v-if="group.moreLink"
                    type="button"
                    class="btn btn--pill service-group__more"
                    @click="handleAction(group.moreLink, 'OTHER_CTA')"
                  >
                    了解更多 →
                  </button>
                </div>

                <div class="grid grid--3 service-group__cards">
                  <RouterLink
                    v-for="(card, cIdx) in group.items"
                    :key="card.id"
                    v-reveal="cIdx * 90"
                    :to="card.url || `/products/${card.id}`"
                    class="service-card"
                  >
                    <div v-if="card.cover" class="service-card__media media-frame">
                      <video
                        v-if="card.cover.type === 'VIDEO'"
                        :src="card.cover.url"
                        autoplay
                        muted
                        loop
                        playsinline
                      ></video>
                      <img v-else :src="card.cover.url" :alt="card.cover.alt || card.title" loading="lazy" />
                    </div>
                    <div class="service-card__body">
                      <h4 class="service-card__title">{{ card.title }}</h4>
                      <p v-if="card.summary" class="service-card__summary">{{ card.summary }}</p>
                      <span class="service-card__link">了解详情 →</span>
                    </div>
                  </RouterLink>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- C. 客户案例 (cases) -->
        <section
          v-else-if="secKey === 'cases' && data.cases?.items?.length"
          id="cases"
          class="section section--surface cases-section"
        >
          <div class="container container--wide">
            <div v-reveal class="section-head">
              <div>
                <span class="kicker">CUSTOMER CASES</span>
                <h2 class="section-title">{{ data.cases.title || '客户案例' }}</h2>
                <p v-if="data.cases.subtitle" class="section-sub">{{ data.cases.subtitle }}</p>
              </div>
              <RouterLink :to="data.cases.moreUrl || '/cases'" class="link-more section-head__link">
                查看全部案例
              </RouterLink>
            </div>

            <div class="grid grid--3">
              <RouterLink
                v-for="(item, idx) in data.cases.items"
                :key="item.id"
                v-reveal="idx * 90"
                :to="item.url || `/cases/${item.id}`"
                class="case-card"
              >
                <div class="case-card__media media-frame">
                  <img
                    v-if="item.cover"
                    :src="item.cover.url"
                    :alt="item.cover.alt || item.title"
                    loading="lazy"
                  />
                  <span v-if="item.industry" class="case-card__badge">{{ item.industry }}</span>
                </div>
                <div class="case-card__body">
                  <p v-if="item.customerName" class="case-card__customer">{{ item.customerName }}</p>
                  <h3 class="case-card__title">{{ item.title }}</h3>
                  <p v-if="item.summary" class="case-card__summary">{{ item.summary }}</p>

                  <dl v-if="item.outcomes?.length" class="case-outcomes">
                    <div v-for="out in item.outcomes" :key="out.label" class="case-outcomes__item">
                      <dd class="case-outcomes__val">{{ out.displayValue }}</dd>
                      <dt class="case-outcomes__lbl">{{ out.label }}</dt>
                    </div>
                  </dl>
                </div>
              </RouterLink>
            </div>
          </div>
        </section>

        <!-- D. 合作客户标识 (customers) -->
        <section
          v-else-if="secKey === 'customers' && data.customers?.items?.length"
          id="customers"
          class="section section--tight customers-section"
        >
          <div class="container container--wide">
            <div v-reveal class="section-head">
              <div>
                <span class="kicker">CUSTOMERS</span>
                <h2 class="section-title">{{ data.customers.title || '合作客户' }}</h2>
                <p v-if="data.customers.subtitle" class="section-sub">{{ data.customers.subtitle }}</p>
              </div>
            </div>

            <ul class="logo-grid">
              <li
                v-for="(cust, idx) in data.customers.items"
                :key="cust.id"
                v-reveal="(idx % 6) * 60"
                class="logo-grid__item"
              >
                <a
                  v-if="cust.url"
                  :href="cust.url"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="logo-grid__link"
                >
                  <img :src="cust.logo.url" :alt="cust.logo.alt || cust.name" loading="lazy" />
                </a>
                <div v-else class="logo-grid__link">
                  <img :src="cust.logo.url" :alt="cust.logo.alt || cust.name" loading="lazy" />
                </div>
              </li>
            </ul>
          </div>
        </section>

        <!-- E. 最新动态 (news) -->
        <section
          v-else-if="secKey === 'news' && data.news?.items?.length"
          id="news"
          class="section section--surface news-section"
        >
          <div class="container container--wide">
            <div v-reveal class="section-head">
              <div>
                <span class="kicker">NEWS CENTER</span>
                <h2 class="section-title">{{ data.news.title || '最新动态' }}</h2>
                <p v-if="data.news.subtitle" class="section-sub">{{ data.news.subtitle }}</p>
              </div>
              <RouterLink :to="data.news.moreUrl || '/news'" class="link-more section-head__link">
                查看更多
              </RouterLink>
            </div>

            <div class="grid grid--3">
              <RouterLink
                v-for="(item, idx) in data.news.items"
                :key="item.id"
                v-reveal="idx * 90"
                :to="item.url || `/news/${item.id}`"
                class="news-card"
              >
                <div v-if="item.cover" class="news-card__media media-frame">
                  <img :src="item.cover.url" :alt="item.cover.alt || item.title" loading="lazy" />
                </div>
                <div class="news-card__body">
                  <time class="news-card__date">{{ item.displayTime }}</time>
                  <h3 class="news-card__title">{{ item.title }}</h3>
                  <p v-if="item.summary" class="news-card__summary">{{ item.summary }}</p>
                  <span class="link-more news-card__more">阅读全文</span>
                </div>
              </RouterLink>
            </div>
          </div>
        </section>

        <!-- F. 底部咨询行动区 (consultation) — 已彻底移除 ROI 测算卡 -->
        <section
          v-else-if="secKey === 'consultation' && data.consultation"
          id="consultation"
          class="section section--dark consult-cta-section"
        >
          <div class="container container--wide">
            <div v-reveal class="consult-cta-box">
              <div>
                <h2 class="consult-cta-box__title">{{ data.consultation.title || '与我们取得联系' }}</h2>
                <p v-if="data.consultation.subtitle" class="consult-cta-box__sub">
                  {{ data.consultation.subtitle }}
                </p>
              </div>
              <div class="consult-cta-box__actions">
                <button
                  type="button"
                  class="btn btn--accent consult-cta-box__btn"
                  @click="handleAction(data.consultation.link, 'HOME_BOTTOM')"
                >
                  {{ data.consultation.buttonText || '立即咨询' }}
                </button>
              </div>
            </div>
          </div>
        </section>
      </template>
    </template>
  </div>
</template>

<style scoped>
.hero {
  position: relative;
  min-height: clamp(540px, 76vh, 720px);
  display: flex;
  align-items: center;
  background: #08080c;
  color: #ffffff;
  overflow: hidden;
}

.hero__media {
  position: absolute;
  inset: 0;
}

.hero__bg {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero__overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    95deg,
    rgba(6, 8, 16, 0.88) 0%,
    rgba(6, 8, 16, 0.68) 48%,
    rgba(6, 8, 16, 0.22) 100%
  );
}

.hero__content {
  position: relative;
  z-index: 2;
  padding-top: 88px;
  padding-bottom: 72px;
}

.hero__title {
  max-width: 720px;
  font-size: clamp(36px, 4.8vw, 60px);
  line-height: 1.15;
  font-weight: 700;
  color: #ffffff;
  white-space: pre-line;
  letter-spacing: -0.03em;
}

.hero__subtitle {
  margin-top: 20px;
  max-width: 560px;
  font-size: clamp(16px, 1.5vw, 18.5px);
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.82);
}

.hero__actions {
  margin-top: 34px;
}

/* 企业关键指标 */
.metrics-section {
  border-bottom: 1px solid var(--c-border);
}

.metrics-grid {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 32px;
}

.metric-item {
  padding-left: 24px;
  border-left: 1px solid var(--c-border-strong);
}

.metric-item:first-child {
  padding-left: 0;
  border-left: none;
}

.metric-item__val-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.metric-item__value {
  font-size: clamp(38px, 4.2vw, 52px);
  font-weight: 700;
  letter-spacing: -0.03em;
  line-height: 1.05;
  color: var(--c-text);
}

.metric-item__unit {
  font-size: 22px;
  font-weight: 600;
  color: var(--c-brand);
}

.metric-item__label {
  margin-top: 10px;
  font-size: 15px;
  font-weight: 600;
  color: var(--c-text);
}

.metric-item__desc {
  margin-top: 4px;
  font-size: 13px;
  color: var(--c-text-muted);
}

/* 核心产品与方案分组（清爽标题行 + 三列卡片，无右侧多余配图框） */
.service-groups {
  display: flex;
  flex-direction: column;
  gap: 64px;
}

.service-group {
  padding-top: 32px;
  border-top: 1px solid var(--c-border);
}

.service-group:first-child {
  padding-top: 0;
  border-top: none;
}

.service-group__header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 28px;
  flex-wrap: wrap;
}

.service-group__index {
  font-size: 12px;
  font-weight: 700;
  color: var(--c-brand);
  letter-spacing: 0.14em;
}

.service-group__title {
  margin-top: 6px;
  font-size: clamp(22px, 2.6vw, 30px);
}

.service-group__desc {
  margin-top: 8px;
  max-width: 640px;
  font-size: 15px;
  color: var(--c-text-muted);
  line-height: 1.75;
}

.service-card {
  background: #ffffff;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.3s var(--ease-out), box-shadow 0.3s var(--ease-out), border-color 0.25s ease;
}

.service-card:hover {
  transform: translateY(-6px);
  border-color: rgba(91, 75, 232, 0.32);
  box-shadow: 0 20px 44px rgba(10, 12, 24, 0.08);
}

.service-card__media {
  aspect-ratio: 16 / 9;
  border-radius: 0;
}

.service-card__body {
  padding: 24px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.service-card__title {
  font-size: 19px;
  font-weight: 600;
}

.service-card__summary {
  margin-top: 10px;
  font-size: 14px;
  color: var(--c-text-muted);
  line-height: 1.75;
  flex: 1;
}

.service-card__link {
  margin-top: 18px;
  font-size: 13.5px;
  font-weight: 500;
  color: var(--c-brand);
}

/* 客户案例 */
.case-card {
  background: #ffffff;
  border-radius: var(--radius);
  border: 1px solid var(--c-border);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.3s var(--ease-out), box-shadow 0.3s var(--ease-out);
}

.case-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 22px 48px rgba(10, 12, 24, 0.09);
}

.case-card__media {
  aspect-ratio: 16 / 10;
  border-radius: 0;
}

.case-card__badge {
  position: absolute;
  top: 14px;
  left: 14px;
  padding: 4px 12px;
  border-radius: 999px;
  background: rgba(8, 8, 12, 0.72);
  backdrop-filter: blur(6px);
  color: #ffffff;
  font-size: 12px;
}

.case-card__body {
  padding: 24px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.case-card__customer {
  font-size: 12.5px;
  font-weight: 600;
  color: var(--c-brand);
  margin-bottom: 6px;
}

.case-card__title {
  font-size: 19px;
  line-height: 1.45;
}

.case-card__summary {
  margin-top: 8px;
  font-size: 14px;
  color: var(--c-text-muted);
  line-height: 1.75;
}

.case-outcomes {
  margin: auto 0 0;
  padding-top: 18px;
  border-top: 1px solid var(--c-border);
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.case-outcomes__val {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--c-brand);
}

.case-outcomes__lbl {
  font-size: 12px;
  color: var(--c-text-muted);
}

/* 客户墙 */
.logo-grid {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.logo-grid__link {
  height: 92px;
  border: 1px solid var(--c-border);
  border-radius: 14px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.logo-grid__link:hover {
  border-color: var(--c-brand);
  transform: translateY(-2px);
}

.logo-grid__link img {
  max-width: 136px;
  max-height: 44px;
  object-fit: contain;
  filter: grayscale(1);
  opacity: 0.82;
  transition: filter 0.25s ease, opacity 0.25s ease;
}

.logo-grid__link:hover img {
  filter: none;
  opacity: 1;
}

/* 新闻卡片 */
.news-card {
  background: #ffffff;
  border-radius: var(--radius);
  border: 1px solid var(--c-border);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.28s var(--ease-out), box-shadow 0.28s var(--ease-out);
}

.news-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 18px 40px rgba(10, 12, 24, 0.08);
}

.news-card__media {
  aspect-ratio: 16 / 10;
  border-radius: 0;
}

.news-card__body {
  padding: 22px 24px 26px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.news-card__date {
  font-size: 12.5px;
  color: var(--c-text-subtle);
}

.news-card__title {
  margin-top: 8px;
  font-size: 18px;
  line-height: 1.5;
}

.news-card__summary {
  margin-top: 8px;
  font-size: 14px;
  color: var(--c-text-muted);
  line-height: 1.75;
  flex: 1;
}

.news-card__more {
  margin-top: 16px;
}

/* 底部咨询区 */
.consult-cta-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
  flex-wrap: wrap;
}

.consult-cta-box__title {
  font-size: clamp(28px, 3.6vw, 44px);
  color: #ffffff;
}

.consult-cta-box__sub {
  margin-top: 12px;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.74);
  max-width: 600px;
}
</style>
```

### `website/web/src/views/AboutView.vue`

```vue
<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { getAbout, getNews, getSite } from '@/api/public'
import { openConsult } from '@/composables/consult'
import { usePageMeta } from '@/composables/usePageMeta'
import { vReveal } from '@/directives/reveal'
import type { AboutOfficeItem, AboutPage, AboutValueItem, NewsSummary } from '@/data/types'

const data = ref<AboutPage | null>(null)
const siteName = ref('G7易流')
const newsList = ref<NewsSummary[]>([])
const activeTab = ref('intro')
const milestoneIndex = ref(1)
const newsStartIndex = ref(0)
const activeOfficeIndex = ref(0)

usePageMeta(() => ({
  title: '关于我们',
  description: data.value?.intro?.slice(0, 120) || '了解企业简介、使命愿景、发展历程、新闻中心与全国办公网点。',
}))

const TABS = [
  { key: 'intro', label: '企业简介', targetId: 'about-intro' },
  { key: 'culture', label: '管理团队', targetId: 'about-culture' },
  { key: 'media', label: '媒体中心', targetId: 'about-media' },
  { key: 'milestones', label: '资料共享', targetId: 'about-milestones' },
  { key: 'join', label: '加入我们', targetId: 'about-join' },
]

const heroTitle = computed(() => data.value?.heroTitle || '以物联网技术和软件服务改变公路货运')

const introTitle = computed(() => data.value?.introTitle || `关于${siteName.value}`)

const introParagraphs = computed(() => {
  const raw = (data.value?.intro || '').trim()
  if (raw) {
    return raw.split(/\n+/).filter(Boolean)
  }
  return [
    `北京汇通天下物联科技有限公司（${siteName.value}）是一家领先的公路货运行业物联网科技和软件服务公司。为货运经营者提供从车队管理、安全管理、货物管理到智能调度、自动结算的一体化操作平台，连接货运全流程并以数据和AI驱动运营，助力货运经营者降本增效。`,
    `${siteName.value}的客户覆盖超80%上游头部货主以及36000+下游中小物流企业，服务网络覆盖全国核心物流节点与产业园区。`,
  ]
})

const missionText = computed(
  () => data.value?.mission || '以科技创新和服务升级物流生产力，让产业参与者共享美好改变',
)

const visionText = computed(() => data.value?.vision || '物流产业最受尊重的SaaS公司')

const DEFAULT_VALUES: AboutValueItem[] = [
  {
    title: '客户成功',
    lines: [
      '我们存在的唯一目的就是为客户带来价值',
      '客户的需求是公司发展、产品创新的源泉:深入场景，洞察客户需求，解决客户痛点问题',
      '以可信赖的产品和有温度的服务，满足甚至超越客户的合理期望，做客户的长期朋友',
    ],
  },
  {
    title: '极限',
    lines: [
      '目标远大，不甘平庸，不断挑战认知边界，把不可能变为可能',
      '敢想、敢试、敢突破，致力于打造有号召力的产品',
      '把事做透，用工匠精神实现卓越成果',
    ],
  },
  {
    title: '信任',
    lines: [
      '以身作则，说到做到',
      '敢于信任，从我做起',
      '主动寻找有共同理想的产业变革者，善于建立信任，打造有格局的朋友圈',
    ],
  },
  {
    title: '坚韧',
    lines: [
      '坚持长期主义积极面对挑战和不确定性，敢于在泥泞中前行',
      '坚定目标，锲而不舍，迎难而上，确保结果',
      '充分发挥主观能动性，多找思路和方法，拒绝借口',
    ],
  },
]

const valuesList = computed<AboutValueItem[]>(() => {
  if (data.value?.values && data.value.values.length > 0) {
    return data.value.values
  }
  return DEFAULT_VALUES
})

interface NormalizedMilestone {
  year: string
  bullets: string[]
}

const DEFAULT_MILESTONES: NormalizedMilestone[] = [
  {
    year: '2018',
    bullets: [
      '发布智能挂车与安全管家托管服务',
      '连接平台货运车辆突破 80 万台',
      '完成由多家国际顶级机构联合领投的战略融资',
    ],
  },
  {
    year: '2019',
    bullets: [
      '平台卡车连接数突破140万',
      '面向煤炭、钢铁、水泥、汽车、快递、快运等多个行业提供综合性数字化解决方案',
      '与中国铝业、普洛斯、丸红等设立合资公司，服务生态扩张',
    ],
  },
  {
    year: '2022',
    bullets: [
      '平台卡车连接数突破 200万',
      '宣布完成新一轮2亿美元融资',
      '嬴彻科技自动驾驶卡车量产交付',
    ],
  },
  {
    year: '2010',
    bullets: [
      '向物流运输公司提供基于北斗和GPS位置信息的车队管理 SaaS 服务',
      '平台连接车辆2000台',
    ],
  },
  {
    year: '2014',
    bullets: [
      '推出智能管车与车队金融结算服务体系',
      '全国设立七大区域运营中心，服务超万家物流企业',
    ],
  },
]

const milestonesList = computed<NormalizedMilestone[]>(() => {
  const raw = data.value?.milestones ?? []
  if (raw.length > 0) {
    return raw.map((m) => {
      const text = [m.title, m.description].filter(Boolean).join('；')
      const bullets = text
        .split(/[\n；;]+/)
        .map((s) => s.trim())
        .filter(Boolean)
      return {
        year: m.year,
        bullets: bullets.length ? bullets : [m.title || '关键发展节点'],
      }
    })
  }
  return DEFAULT_MILESTONES
})

// 顶部显示的 5 个年份窗口（以当前选中项为中心）
const visibleYearWindow = computed(() => {
  const list = milestonesList.value
  const total = list.length
  if (total === 0) return []
  const count = Math.min(5, total)
  const half = Math.floor(count / 2)
  const result: Array<{ index: number; year: string; isCenter: boolean }> = []
  for (let offset = -half; offset <= half; offset++) {
    if (result.length >= count) break
    const idx = (milestoneIndex.value + offset + total) % total
    result.push({
      index: idx,
      year: list[idx].year,
      isCenter: idx === milestoneIndex.value,
    })
  }
  return result
})

// 下方 3 张卡片（左 / 中高亮 / 右）
const visibleMilestoneCards = computed(() => {
  const list = milestonesList.value
  const total = list.length
  if (total === 0) return []
  if (total === 1) return [{ index: 0, item: list[0], isCenter: true }]
  if (total === 2) {
    const nextIdx = (milestoneIndex.value + 1) % total
    return [
      { index: milestoneIndex.value, item: list[milestoneIndex.value], isCenter: true },
      { index: nextIdx, item: list[nextIdx], isCenter: false },
    ]
  }
  const prevIdx = (milestoneIndex.value - 1 + total) % total
  const nextIdx = (milestoneIndex.value + 1) % total
  return [
    { index: prevIdx, item: list[prevIdx], isCenter: false },
    { index: milestoneIndex.value, item: list[milestoneIndex.value], isCenter: true },
    { index: nextIdx, item: list[nextIdx], isCenter: false },
  ]
})

function prevMilestone() {
  const total = milestonesList.value.length
  if (!total) return
  milestoneIndex.value = (milestoneIndex.value - 1 + total) % total
}

function nextMilestone() {
  const total = milestonesList.value.length
  if (!total) return
  milestoneIndex.value = (milestoneIndex.value + 1) % total
}

const DEFAULT_NEWS: NewsSummary[] = [
  {
    id: '1',
    title: 'G7与E6宣布完成合并',
    summary: '双方正式完成战略合并，共同打造公路货运行业领先的物联网与软件服务平台。',
    cover: null,
    categoryId: null,
    categoryName: '企业动态',
    displayTime: '2022/06/07',
  },
  {
    id: '2',
    title: '让你的物流AI开工！2026数字物流大会精彩日程抢先看',
    summary: '聚焦物流全场景 AI 智能体与 IoT 深度融合实践。',
    cover: null,
    categoryId: null,
    categoryName: '行业峰会',
    displayTime: '2026/10/16',
  },
  {
    id: '3',
    title: '走G7「新」路 马上出发！——老翟新能源车队一线探访',
    summary: '深入干线与大宗运输现场，探访新能源重卡车队真实运营效益。',
    cover: null,
    categoryId: null,
    categoryName: '前沿洞察',
    displayTime: '2026/10/13',
  },
  {
    id: '4',
    title: '2026物流小巨人启动——「新能源&AI」双榜单全新招募！',
    summary: '寻找物流产业最具技术领导力与增长韧性的标杆企业。',
    cover: null,
    categoryId: null,
    categoryName: '品牌活动',
    displayTime: '2026/09/23',
  },
]

const resolvedNews = computed<NewsSummary[]>(() => {
  return newsList.value.length > 0 ? newsList.value : DEFAULT_NEWS
})

const visibleNewsCards = computed(() => {
  const list = resolvedNews.value
  if (list.length <= 4) return list
  const out: NewsSummary[] = []
  for (let i = 0; i < 4; i++) {
    out.push(list[(newsStartIndex.value + i) % list.length])
  }
  return out
})

function prevNews() {
  const len = resolvedNews.value.length
  if (len <= 4) return
  newsStartIndex.value = (newsStartIndex.value - 1 + len) % len
}

function nextNews() {
  const len = resolvedNews.value.length
  if (len <= 4) return
  newsStartIndex.value = (newsStartIndex.value + 1) % len
}

const DEFAULT_OFFICES: AboutOfficeItem[] = [
  {
    city: '北京（总部）',
    phone: '010-62667878',
    address: '北京市 海淀区 建材城中路 27 号金隅智造工场 N7 号楼 2 层',
    mapX: 68,
    mapY: 42,
  },
  {
    city: '深圳',
    phone: '0755-26944168',
    address: '深圳市南山区滨海大道 3369 号有线信息传输大厦 9 层',
    mapX: 67,
    mapY: 75,
  },
  {
    city: '成都',
    phone: '028-83358692',
    address: '成都市 天府新区 兴隆湖畔路西段 99 号天府新经济产业园 D 区 B6 栋 28 楼',
    mapX: 54,
    mapY: 62,
  },
  {
    city: '广州',
    phone: '020-38889000',
    address: '广州市天河区珠江新城花城大道 85 号高德置地广场春广场',
    mapX: 66,
    mapY: 71,
  },
]

const officesList = computed<AboutOfficeItem[]>(() => {
  if (data.value?.offices && data.value.offices.length > 0) {
    return data.value.offices
  }
  return DEFAULT_OFFICES
})

const currentOffice = computed(() => officesList.value[activeOfficeIndex.value] ?? officesList.value[0])

function scrollToSection(tabKey: string, targetId: string) {
  activeTab.value = tabKey
  const el = document.getElementById(targetId)
  if (el) {
    const y = el.getBoundingClientRect().top + window.scrollY - 90
    window.scrollTo({ top: y, behavior: 'smooth' })
  }
}

function onScroll() {
  for (const tab of TABS) {
    const el = document.getElementById(tab.targetId)
    if (el) {
      const rect = el.getBoundingClientRect()
      if (rect.top <= 180 && rect.bottom >= 180) {
        activeTab.value = tab.key
        break
      }
    }
  }
}

onMounted(async () => {
  window.addEventListener('scroll', onScroll, { passive: true })
  const [aboutRes, siteRes, newsRes] = await Promise.all([
    getAbout().catch(() => null),
    getSite().catch(() => null),
    getNews({ page: 1, pageSize: 8 }).catch(() => ({ items: [] })),
  ])
  if (aboutRes) {
    data.value = aboutRes
    if (milestonesList.value.length >= 3) {
      milestoneIndex.value = Math.min(2, milestonesList.value.length - 1)
    } else {
      milestoneIndex.value = 0
    }
  }
  if (siteRes?.siteName) {
    siteName.value = siteRes.siteName
  }
  if (newsRes?.items?.length) {
    newsList.value = newsRes.items
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<template>
  <div class="about-g7">
    <!-- 1. 顶部深空流光透视首屏 Banner（1:1 对齐 image_1254cc.jpg） -->
    <section class="about-hero">
      <div class="about-hero__trails" aria-hidden="true"></div>
      <div class="container about-hero__inner">
        <div v-reveal class="about-hero__logo-mark" aria-hidden="true">G</div>
        <h1 v-reveal="100" class="about-hero__title">{{ heroTitle }}</h1>
      </div>
    </section>

    <!-- 2. 悬浮白底五页签导航条（1:1 对齐 image_1254cc.jpg / image_1250ec.jpg） -->
    <div class="container about-tabs-wrap">
      <nav v-reveal="140" class="about-tabs" aria-label="关于我们页内导航">
        <button
          v-for="tab in TABS"
          :key="tab.key"
          type="button"
          class="about-tabs__btn"
          :class="{ 'is-active': activeTab === tab.key }"
          @click="scrollToSection(tab.key, tab.targetId)"
        >
          {{ tab.label }}
        </button>
      </nav>
    </div>

    <!-- 3. 企业简介：左文右图（1:1 对齐 image_1254cc.jpg） -->
    <section id="about-intro" class="about-intro-sec">
      <div class="container about-intro-grid">
        <div v-reveal class="about-intro__text">
          <h2 class="about-sec-title">{{ introTitle }}</h2>
          <p v-for="(para, pIdx) in introParagraphs" :key="pIdx" class="about-intro__para">
            {{ para }}
          </p>
        </div>

        <div v-reveal="140" class="about-intro__visual">
          <div class="about-intro__photo">
            <div class="about-intro__radar" aria-hidden="true"></div>
            <div class="about-intro__truck-scene" aria-hidden="true">
              <span class="scene-tag">AI + IoT 全链路实时感知</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 4. 使命 · 愿景 · 价值观（1:1 对齐 image_1249bf.png & image_1249a8.jpg，每一行随滚动缓慢弹出） -->
    <section id="about-culture" class="about-culture-sec">
      <div class="container">
        <!-- 使命 -->
        <div v-reveal class="culture-row">
          <div class="culture-row__label">使命</div>
          <div class="culture-row__content">
            <h3 class="culture-row__headline">{{ missionText }}</h3>
          </div>
        </div>

        <!-- 愿景 -->
        <div v-reveal="80" class="culture-row">
          <div class="culture-row__label">愿景</div>
          <div class="culture-row__content">
            <h3 class="culture-row__headline">{{ visionText }}</h3>
          </div>
        </div>

        <!-- 价值观 -->
        <div class="culture-row culture-row--values">
          <div v-reveal class="culture-row__label">价值观</div>
          <div class="culture-row__content culture-values-list">
            <div
              v-for="(valItem, vIdx) in valuesList"
              :key="valItem.title"
              v-reveal="vIdx * 90"
              class="value-block"
            >
              <h4 class="value-block__title">{{ valItem.title }}</h4>
              <p v-for="(line, lIdx) in valItem.lines" :key="lIdx" class="value-block__line">
                {{ line }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 5. 发展历程横向时间轴轮播（1:1 对齐 image_1254a8.png） -->
    <section id="about-milestones" class="about-history-sec">
      <div class="container">
        <h2 v-reveal class="about-center-title">发展历程</h2>

        <!-- 顶部年份轴 + 左右切换箭头 -->
        <div v-reveal="100" class="history-axis">
          <button
            type="button"
            class="history-arrow"
            aria-label="上一年份"
            @click="prevMilestone"
          >
            ‹
          </button>

          <div class="history-years">
            <button
              v-for="yr in visibleYearWindow"
              :key="`${yr.index}-${yr.year}`"
              type="button"
              class="history-year-btn"
              :class="{ 'is-center': yr.isCenter }"
              @click="milestoneIndex = yr.index"
            >
              {{ yr.year }}
            </button>
          </div>

          <button
            type="button"
            class="history-arrow"
            aria-label="下一年份"
            @click="nextMilestone"
          >
            ›
          </button>
        </div>

        <!-- 下方三列历程卡片（中间卡片白底悬浮阴影放大 + 紫色圆环靶心节点） -->
        <div v-reveal="160" class="history-stage">
          <div
            v-for="card in visibleMilestoneCards"
            :key="`card-${card.index}`"
            class="history-card"
            :class="{ 'is-center': card.isCenter }"
            @click="milestoneIndex = card.index"
          >
            <div class="history-card__year">{{ card.item.year }}</div>
            <div class="history-card__line">
              <span v-if="card.isCenter" class="history-card__target" aria-hidden="true"></span>
              <span v-else class="history-card__dot" aria-hidden="true"></span>
            </div>
            <ul class="history-card__bullets">
              <li v-for="(b, bIdx) in card.item.bullets" :key="bIdx">{{ b }}</li>
            </ul>
          </div>
        </div>
      </div>
    </section>

    <!-- 6. 媒体中心 / 新闻中心 4 卡横向轮播（1:1 对齐 image_1250ec.jpg） -->
    <section id="about-media" class="about-news-sec">
      <div class="container">
        <div v-reveal class="about-news__head">
          <h2 class="about-sec-title">新闻中心</h2>
          <RouterLink to="/news" class="about-news__more-btn">查看更多</RouterLink>
        </div>

        <div v-reveal="120" class="about-news-slider">
          <button
            type="button"
            class="slider-arrow slider-arrow--left"
            aria-label="上一组新闻"
            @click="prevNews"
          >
            ‹
          </button>

          <div class="about-news-grid">
            <RouterLink
              v-for="(item, nIdx) in visibleNewsCards"
              :key="`${item.id}-${nIdx}`"
              :to="`/news/${item.id}`"
              class="about-news-card"
            >
              <div class="about-news-card__poster" :class="`poster-theme--${nIdx % 4}`">
                <img
                  v-if="item.cover?.url"
                  :src="item.cover.url"
                  :alt="item.cover.alt || item.title"
                  loading="lazy"
                />
                <div v-else class="poster-fallback">
                  <span class="poster-fallback__brand">{{ siteName }}</span>
                  <strong class="poster-fallback__text">{{ item.title.slice(0, 14) }}</strong>
                </div>
              </div>
              <div class="about-news-card__body">
                <h3 class="about-news-card__title">{{ item.title }}</h3>
                <div class="about-news-card__time">
                  <span aria-hidden="true">🕒</span>
                  <span>{{ item.displayTime || '2026/10/16' }}</span>
                </div>
              </div>
            </RouterLink>
          </div>

          <button
            type="button"
            class="slider-arrow slider-arrow--right"
            aria-label="下一组新闻"
            @click="nextNews"
          >
            ›
          </button>
        </div>
      </div>
    </section>

    <!-- 7. 加入我们横幅海报（1:1 对齐 image_1249c3.jpg） -->
    <section id="about-join" class="about-join-sec">
      <div class="container">
        <div v-reveal class="join-banner">
          <div class="join-banner__content">
            <h2 class="join-banner__title">{{ data?.joinTitle || `${siteName}路上见` }}</h2>
            <button
              type="button"
              class="join-banner__btn"
              @click="openConsult({ sourcePosition: 'OTHER_CTA', title: '人才招聘与职位咨询' })"
            >
              {{ data?.joinButtonText || '查看职位' }}
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 8. 联系我们：多城市左侧切换列表 + 右侧点阵地图与实景照片气泡（1:1 对齐 image_1254a5.jpg） -->
    <section id="about-contact" class="about-offices-sec">
      <div class="container">
        <h2 v-reveal class="about-center-title">联系我们</h2>

        <div class="offices-layout">
          <!-- 左侧城市列表 -->
          <div v-reveal="80" class="offices-list">
            <div
              v-for="(off, oIdx) in officesList"
              :key="off.city"
              class="office-item"
              :class="{ 'is-active': activeOfficeIndex === oIdx }"
              @mouseenter="activeOfficeIndex = oIdx"
              @click="activeOfficeIndex = oIdx"
            >
              <h3 class="office-item__city">{{ off.city }}</h3>
              <template v-if="off.phone || off.address">
                <p v-if="off.phone" class="office-item__line">
                  <span>电话：</span>{{ off.phone }}
                </p>
                <p v-if="off.address" class="office-item__line">
                  <span>地址：</span>{{ off.address }}
                </p>
              </template>
            </div>
          </div>

          <!-- 右侧点阵地图与实景园区悬浮气泡 -->
          <div v-reveal="160" class="offices-map">
            <div class="offices-map__canvas">
              <!-- 悬浮园区照片框 -->
              <div class="map-photo-popup">
                <div class="map-photo-popup__img">
                  <div class="building-illustration">
                    <span class="building-illustration__tag">{{ currentOffice?.city }}</span>
                  </div>
                </div>
                <span class="map-photo-popup__arrow" aria-hidden="true"></span>
              </div>

              <!-- 城市坐标点 -->
              <div class="map-pins">
                <button
                  v-for="(off, oIdx) in officesList"
                  :key="off.city"
                  type="button"
                  class="map-pin"
                  :class="{ 'is-active': activeOfficeIndex === oIdx }"
                  :style="{ left: `${off.mapX || 65}%`, top: `${off.mapY || 45 + oIdx * 10}%` }"
                  @click="activeOfficeIndex = oIdx"
                >
                  <span class="map-pin__dot"></span>
                  <span class="map-pin__name">{{ off.city }}</span>
                </button>
                <span class="map-pin map-pin--static" style="left: 70%; top: 47%">
                  <span class="map-pin__dot"></span>
                  <span class="map-pin__name">天津</span>
                </span>
                <span class="map-pin map-pin--static" style="left: 58%; top: 53%">
                  <span class="map-pin__dot"></span>
                  <span class="map-pin__name">西安</span>
                </span>
                <span class="map-pin map-pin--static" style="left: 74%; top: 58%">
                  <span class="map-pin__dot"></span>
                  <span class="map-pin__name">上海</span>
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.about-g7 {
  background: #ffffff;
  color: #1f2329;
}

/* 1. 顶部深空流光 Banner */
.about-hero {
  position: relative;
  height: 400px;
  background: radial-gradient(ellipse at 50% 85%, #2b1d52 0%, #100e20 55%, #07070c 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  overflow: hidden;
}

.about-hero__trails {
  position: absolute;
  inset: 0;
  background:
    repeating-linear-gradient(
      115deg,
      transparent 0px,
      transparent 38px,
      rgba(245, 158, 11, 0.08) 39px,
      transparent 41px
    ),
    repeating-linear-gradient(
      65deg,
      transparent 0px,
      transparent 44px,
      rgba(99, 102, 241, 0.12) 45px,
      transparent 48px
    );
  opacity: 0.85;
}

.about-hero__inner {
  position: relative;
  z-index: 2;
  padding-bottom: 28px;
}

.about-hero__logo-mark {
  font-size: 56px;
  font-weight: 900;
  font-style: italic;
  color: #ffffff;
  line-height: 1;
  margin-bottom: 14px;
  letter-spacing: -0.05em;
}

.about-hero__title {
  font-size: clamp(26px, 3.6vw, 40px);
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 0.02em;
}

/* 2. 悬浮白底五页签导航条 */
.about-tabs-wrap {
  position: relative;
  z-index: 10;
  margin-top: -42px;
}

.about-tabs {
  max-width: 980px;
  height: 82px;
  margin: 0 auto;
  background: #ffffff;
  box-shadow: 0 12px 36px rgba(16, 24, 40, 0.06);
  display: flex;
  align-items: center;
  padding: 0 48px;
  gap: 56px;
}

.about-tabs__btn {
  position: relative;
  height: 100%;
  border: none;
  background: none;
  font-size: 15px;
  color: #2b2f38;
  cursor: pointer;
  padding: 0 4px;
  transition: color 0.2s ease;
}

.about-tabs__btn:hover,
.about-tabs__btn.is-active {
  color: #5b4be8;
  font-weight: 600;
}

.about-tabs__btn.is-active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 14px;
  height: 3px;
  background: #5b4be8;
  border-radius: 2px;
}

/* 3. 企业简介 */
.about-intro-sec {
  padding: 96px 0 80px;
}

.about-intro-grid {
  max-width: 1040px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 64px;
  align-items: center;
}

.about-sec-title {
  font-size: 34px;
  font-weight: 700;
  color: #1f2329;
  margin-bottom: 28px;
}

.about-intro__para {
  font-size: 14px;
  line-height: 2;
  color: #6e7380;
  margin-bottom: 16px;
}

.about-intro__photo {
  position: relative;
  aspect-ratio: 4 / 3;
  background: linear-gradient(140deg, #1b2432 0%, #0f1722 60%, #1e1b4b 100%);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.about-intro__radar {
  width: 240px;
  height: 240px;
  border-radius: 50%;
  background: conic-gradient(
    from 210deg,
    rgba(124, 58, 237, 0.55) 0deg,
    rgba(56, 189, 248, 0.35) 75deg,
    transparent 120deg
  );
  filter: blur(2px);
}

.scene-tag {
  position: absolute;
  bottom: 20px;
  right: 20px;
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.22);
  color: #ffffff;
  font-size: 12.5px;
  border-radius: 4px;
}

/* 4. 使命 / 愿景 / 价值观 */
.about-culture-sec {
  padding: 40px 0 96px;
}

.culture-row {
  max-width: 1040px;
  margin: 0 auto;
  padding: 64px 0;
  border-bottom: 1px solid #eceef2;
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 32px;
  align-items: baseline;
}

.culture-row--values {
  border-bottom: none;
}

.culture-row__label {
  font-size: 20px;
  color: #5f6470;
}

.culture-row__headline {
  font-size: clamp(24px, 2.8vw, 32px);
  font-weight: 700;
  color: #2b2f38;
  line-height: 1.4;
}

.culture-values-list {
  display: flex;
  flex-direction: column;
  gap: 52px;
}

.value-block__title {
  font-size: 26px;
  font-weight: 700;
  color: #2b2f38;
  margin-bottom: 16px;
}

.value-block__line {
  font-size: 14px;
  line-height: 2;
  color: #6e7380;
  margin: 2px 0;
}

/* 5. 发展历程横向时间轴 */
.about-history-sec {
  padding: 88px 0 110px;
  background: #fcfcfd;
}

.about-center-title {
  text-align: center;
  font-size: 36px;
  font-weight: 700;
  color: #2b2f38;
  margin-bottom: 52px;
}

.history-axis {
  max-width: 920px;
  margin: 0 auto 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.history-arrow {
  width: 36px;
  height: 36px;
  border: none;
  background: none;
  font-size: 34px;
  color: #8b90a0;
  cursor: pointer;
  line-height: 1;
}

.history-arrow:hover {
  color: #5b4be8;
}

.history-years {
  flex: 1;
  display: flex;
  align-items: baseline;
  justify-content: space-around;
  padding-bottom: 20px;
  border-bottom: 1px solid #e5e7eb;
}

.history-year-btn {
  border: none;
  background: none;
  font-size: 16px;
  font-weight: 700;
  color: #2b2f38;
  cursor: pointer;
  transition: transform 0.25s ease, font-size 0.25s ease, color 0.25s ease;
}

.history-year-btn.is-center {
  font-size: 28px;
  color: #1f2329;
  transform: translateY(-2px);
}

.history-stage {
  max-width: 1040px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 28px;
  align-items: stretch;
}

.history-card {
  padding: 40px 32px;
  border-radius: 4px;
  background: transparent;
  cursor: pointer;
  transition: all 0.3s ease;
}

.history-card.is-center {
  background: #ffffff;
  box-shadow: 0 20px 50px rgba(16, 24, 40, 0.08);
}

.history-card__year {
  font-size: 28px;
  font-weight: 700;
  color: #3a3f4a;
  margin-bottom: 20px;
}

.history-card__line {
  position: relative;
  height: 20px;
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}

.history-card__line::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  top: 50%;
  border-bottom: 1px dashed #d0d5dd;
}

.history-card__dot {
  position: relative;
  z-index: 2;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #98a2b3;
}

.history-card__target {
  position: relative;
  z-index: 2;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid #5b4be8;
  background: #ffffff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.history-card__target::after {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #5b4be8;
}

.history-card__bullets {
  list-style: disc;
  padding-left: 18px;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  font-size: 13.5px;
  line-height: 1.8;
  color: #5f6470;
}

/* 6. 新闻中心 4 卡横向轮播 */
.about-news-sec {
  padding: 88px 0 96px;
  background: #f5f7fb;
}

.about-news__head {
  max-width: 1080px;
  margin: 0 auto 36px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.about-news__head .about-sec-title {
  margin-bottom: 0;
}

.about-news__more-btn {
  padding: 9px 28px;
  border: 1px solid #1f2329;
  font-size: 13.5px;
  color: #1f2329;
  background: transparent;
  transition: all 0.2s ease;
}

.about-news__more-btn:hover {
  background: #1f2329;
  color: #ffffff;
}

.about-news-slider {
  max-width: 1180px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 16px;
}

.slider-arrow {
  width: 36px;
  height: 36px;
  border: none;
  background: none;
  font-size: 34px;
  color: #98a2b3;
  cursor: pointer;
  flex: none;
}

.slider-arrow:hover {
  color: #5b4be8;
}

.about-news-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.about-news-card {
  background: #ffffff;
  display: flex;
  flex-direction: column;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.about-news-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 16px 36px rgba(16, 24, 40, 0.08);
}

.about-news-card__poster {
  aspect-ratio: 1 / 1;
  overflow: hidden;
  position: relative;
}

.about-news-card__poster img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.poster-fallback {
  width: 100%;
  height: 100%;
  padding: 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: #ffffff;
}

.poster-theme--0 .poster-fallback {
  background: linear-gradient(145deg, #2563eb 0%, #1e3a8a 60%, #0f172a 100%);
}

.poster-theme--1 .poster-fallback {
  background: linear-gradient(145deg, #4f46e5 0%, #311042 65%, #09090f 100%);
}

.poster-theme--2 .poster-fallback {
  background: linear-gradient(145deg, #0284c7 0%, #1e40af 65%, #0f172a 100%);
}

.poster-theme--3 .poster-fallback {
  background: linear-gradient(145deg, #78350f 0%, #271709 65%, #09090b 100%);
}

.poster-fallback__brand {
  font-size: 13px;
  font-weight: 700;
  opacity: 0.85;
}

.poster-fallback__text {
  font-size: 20px;
  line-height: 1.35;
}

.about-news-card__body {
  padding: 20px 18px 22px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 126px;
}

.about-news-card__title {
  font-size: 14.5px;
  font-weight: 500;
  color: #2b2f38;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.about-news-card__time {
  margin-top: 14px;
  font-size: 12px;
  color: #98a2b3;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 7. 加入我们 Banner */
.about-join-sec {
  padding: 80px 0;
}

.join-banner {
  max-width: 1040px;
  margin: 0 auto;
  height: 320px;
  background:
    linear-gradient(90deg, rgba(16, 18, 28, 0.72) 0%, rgba(16, 18, 28, 0.25) 65%),
    radial-gradient(circle at 80% 30%, #4b5563 0%, #1f2937 100%);
  display: flex;
  align-items: center;
  padding: 0 72px;
}

.join-banner__title {
  font-size: 36px;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 24px;
}

.join-banner__btn {
  padding: 10px 32px;
  border: 1px solid rgba(255, 255, 255, 0.85);
  background: transparent;
  color: #ffffff;
  font-size: 13.5px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.join-banner__btn:hover {
  background: #ffffff;
  color: #101014;
}

/* 8. 联系我们：多城市列表 + 点阵地图 */
.about-offices-sec {
  padding: 88px 0 110px;
  background: #f2f4f8;
}

.offices-layout {
  max-width: 1040px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 440px 1fr;
  gap: 56px;
  align-items: center;
}

.offices-list {
  position: relative;
  border-right: 2px solid #e2e6ee;
  padding-right: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.office-item {
  position: relative;
  padding: 18px 24px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.office-item.is-active {
  background: #ffffff;
  box-shadow: 0 10px 28px rgba(16, 24, 40, 0.05);
}

.office-item.is-active::after {
  content: '';
  position: absolute;
  right: -26px;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #4b5563;
}

.office-item__city {
  font-size: 17px;
  font-weight: 700;
  color: #2b2f38;
  margin-bottom: 8px;
}

.office-item__line {
  font-size: 13px;
  line-height: 1.75;
  color: #5f6470;
  margin: 2px 0;
}

.office-item__line span {
  color: #98a2b3;
}

.offices-map__canvas {
  position: relative;
  height: 400px;
  background-image: radial-gradient(#d0d5dd 1.25px, transparent 1.25px);
  background-size: 12px 12px;
  border-radius: 12px;
}

.map-photo-popup {
  position: absolute;
  top: 24px;
  left: 50%;
  transform: translateX(-50%);
  width: 280px;
  height: 156px;
  border: 2px solid #2b2f38;
  border-radius: 6px;
  background: #ffffff;
  overflow: visible;
  box-shadow: 0 16px 36px rgba(16, 24, 40, 0.14);
}

.map-photo-popup__img {
  width: 100%;
  height: 100%;
  overflow: hidden;
  border-radius: 4px;
}

.building-illustration {
  width: 100%;
  height: 100%;
  background: linear-gradient(160deg, #3b82f6 0%, #1d4ed8 45%, #1e293b 100%);
  display: flex;
  align-items: flex-end;
  padding: 14px;
}

.building-illustration__tag {
  padding: 4px 10px;
  border-radius: 4px;
  background: rgba(15, 23, 42, 0.75);
  color: #ffffff;
  font-size: 12px;
  font-weight: 600;
}

.map-photo-popup__arrow {
  position: absolute;
  bottom: -9px;
  left: 62%;
  width: 14px;
  height: 14px;
  background: #2b2f38;
  transform: rotate(45deg);
}

.map-pin {
  position: absolute;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border: none;
  background: none;
  font-size: 12px;
  color: #4b5563;
  cursor: pointer;
  padding: 2px 4px;
}

.map-pin__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  border: 2px solid #98a2b3;
  background: #ffffff;
}

.map-pin.is-active {
  font-weight: 700;
  color: #101014;
}

.map-pin.is-active .map-pin__dot {
  width: 10px;
  height: 10px;
  border-color: #101014;
  background: #ffffff;
}

.map-pin--static {
  pointer-events: none;
  opacity: 0.75;
}

@media (max-width: 920px) {
  .about-tabs {
    padding: 0 20px;
    gap: 20px;
    overflow-x: auto;
  }
  .about-intro-grid,
  .culture-row,
  .history-stage,
  .offices-layout {
    grid-template-columns: 1fr;
  }
  .about-news-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
```

### `website/admin/src/views/HomeManageView.vue`

```vue
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ConfigPanel from '@/components/ConfigPanel.vue'
import ContentPicker from '@/components/ContentPicker.vue'
import LinkEditor, { type LinkValue } from '@/components/LinkEditor.vue'
import MediaPicker from '@/components/MediaPicker.vue'
import { useConfig } from '@/composables/useConfig'
import type { MediaItem } from '@/api/content'

interface HeroItem {
  key?: string
  enabled: boolean
  sortOrder: number
  title: string
  subtitle: string
  desktopMediaId: number | null
  mobileMediaId: number | null
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
  imageMediaId: number | null
  contentType: 'PRODUCT' | 'SOLUTION'
  contentIds: number[]
  moreLink: LinkValue | null
}

interface HomeDraft {
  sectionOrder?: string[]
  hero: { items: HeroItem[] }
  metrics: { enabled: boolean; title: string; subtitle: string; items: MetricItem[] }
  services: { enabled: boolean; title: string; subtitle: string; groups: ServiceGroup[] }
  cases: { enabled: boolean; title: string; subtitle: string; contentIds: number[] }
  customers: { enabled: boolean; title: string; subtitle: string; contentIds: number[] }
  news: {
    enabled: boolean
    title: string
    subtitle: string
    mode: 'SELECTED' | 'LATEST'
    limit: number
    contentIds: number[]
  }
  consultation: {
    title: string
    subtitle: string
    buttonText: string
    link: LinkValue | null
  }
}

const SECTION_LABELS: Record<string, { name: string; desc: string }> = {
  metrics: { name: '企业关键指标 (metrics)', desc: '四列大数字规模与实力背书' },
  services: { name: '产品与解决方案分组 (services)', desc: '左右交错核心产品与方案推荐卡' },
  cases: { name: '标杆客户案例 (cases)', desc: '行业头部客户实践与成果数据' },
  customers: { name: '合作客户标识墙 (customers)', desc: '合作伙伴与客户 Logo 墙/跑马灯' },
  news: { name: '最新动态与洞察 (news)', desc: '手选或自动按最新日期展示企业动态' },
  consultation: { name: '底部咨询与 ROI 行动区 (consultation)', desc: '全站转化收口与专家预约引导' },
}

const DEFAULT_ORDER = ['metrics', 'services', 'cases', 'customers', 'news', 'consultation']

const router = useRouter()
const activeTab = ref('order')
const pickerOpen = ref(false)
const pickerKind = ref<'IMAGE' | 'VIDEO' | 'ALL'>('ALL')
let pickerCallback: ((media: MediaItem) => void) | null = null

const { view, loading, saving, publishing, loadError, load, save, publish } =
  useConfig<HomeDraft & Record<string, unknown>>('HOME')

const form = reactive<HomeDraft>({
  sectionOrder: [...DEFAULT_ORDER],
  hero: { items: [] },
  metrics: { enabled: true, title: '数字生产力规模背书', subtitle: '', items: [] },
  services: { enabled: true, title: '核心产品与行业方案', subtitle: '', groups: [] },
  cases: { enabled: true, title: '行业领军企业的共同选择', subtitle: '', contentIds: [] },
  customers: { enabled: true, title: '生态伙伴与标杆客户', subtitle: '', contentIds: [] },
  news: { enabled: true, title: '最新动态与前沿洞察', subtitle: '', mode: 'LATEST', limit: 3, contentIds: [] },
  consultation: {
    title: '准备好开启企业数字化生产力跃迁了吗？',
    subtitle: '留下您的业务痛点，我们的行业架构师将在 1 个工作日内为您提供定制方案与演示。',
    buttonText: '立即预约咨询',
    link: { type: 'CONSULT' },
  },
})

function fill() {
  const draft = view.value?.draft
  if (!draft) return

  const rawOrder = Array.isArray(draft.sectionOrder) ? draft.sectionOrder : []
  const mergedOrder = rawOrder.filter((k) => DEFAULT_ORDER.includes(k))
  for (const defKey of DEFAULT_ORDER) {
    if (!mergedOrder.includes(defKey)) mergedOrder.push(defKey)
  }
  form.sectionOrder = mergedOrder

  form.hero.items = (draft.hero?.items ?? []).map((item, idx) => ({
    key: item.key,
    enabled: item.enabled ?? true,
    sortOrder: item.sortOrder ?? (idx + 1) * 10,
    title: item.title ?? '',
    subtitle: item.subtitle ?? '',
    desktopMediaId: item.desktopMediaId != null ? Number(item.desktopMediaId) : null,
    mobileMediaId: item.mobileMediaId != null ? Number(item.mobileMediaId) : null,
    imageAlt: item.imageAlt ?? '',
    buttonText: item.buttonText ?? '了解更多',
    link: (item.link as LinkValue | null) ?? { type: 'CONSULT' },
  }))

  form.metrics = {
    enabled: draft.metrics?.enabled ?? true,
    title: draft.metrics?.title ?? '企业实力与服务规模',
    subtitle: draft.metrics?.subtitle ?? '',
    items: (draft.metrics?.items ?? []).map((m, idx) => ({
      key: m.key,
      sortOrder: m.sortOrder ?? (idx + 1) * 10,
      displayValue: m.displayValue ?? '',
      unit: m.unit ?? '',
      label: m.label ?? '',
      description: m.description ?? '',
    })),
  }

  form.services = {
    enabled: draft.services?.enabled ?? true,
    title: draft.services?.title ?? '核心产品与行业方案',
    subtitle: draft.services?.subtitle ?? '',
    groups: (draft.services?.groups ?? []).map((g, idx) => ({
      key: g.key,
      sortOrder: g.sortOrder ?? (idx + 1) * 10,
      title: g.title ?? '',
      description: g.description ?? '',
      imageMediaId: g.imageMediaId != null ? Number(g.imageMediaId) : null,
      contentType: g.contentType === 'SOLUTION' ? 'SOLUTION' : 'PRODUCT',
      contentIds: (g.contentIds ?? []).map((id) => Number(id)),
      moreLink: (g.moreLink as LinkValue | null) ?? { type: 'PAGE', pageCode: 'PRODUCTS' },
    })),
  }

  form.cases = {
    enabled: draft.cases?.enabled ?? true,
    title: draft.cases?.title ?? '客户案例',
    subtitle: draft.cases?.subtitle ?? '',
    contentIds: (draft.cases?.contentIds ?? []).map((id) => Number(id)),
  }

  form.customers = {
    enabled: draft.customers?.enabled ?? true,
    title: draft.customers?.title ?? '合作客户',
    subtitle: draft.customers?.subtitle ?? '',
    contentIds: (draft.customers?.contentIds ?? []).map((id) => Number(id)),
  }

  form.news = {
    enabled: draft.news?.enabled ?? true,
    title: draft.news?.title ?? '最新动态',
    subtitle: draft.news?.subtitle ?? '',
    mode: draft.news?.mode === 'LATEST' ? 'LATEST' : 'SELECTED',
    limit: draft.news?.limit ?? 3,
    contentIds: (draft.news?.contentIds ?? []).map((id) => Number(id)),
  }

  form.consultation = {
    title: draft.consultation?.title ?? '了解适合您的数字化服务',
    subtitle: draft.consultation?.subtitle ?? '',
    buttonText: draft.consultation?.buttonText ?? '立即咨询',
    link: (draft.consultation?.link as LinkValue | null) ?? { type: 'CONSULT' },
  }
}

function moveSection(idx: number, delta: number) {
  if (!form.sectionOrder) return
  const target = idx + delta
  if (target < 0 || target >= form.sectionOrder.length) return
  const [moved] = form.sectionOrder.splice(idx, 1)
  form.sectionOrder.splice(target, 0, moved)
}

function isSectionEnabled(secKey: string): boolean {
  if (secKey === 'consultation') return true
  const block = (form as Record<string, any>)[secKey]
  return Boolean(block?.enabled)
}

function toggleSectionEnabled(secKey: string, val: boolean) {
  if (secKey === 'consultation') return
  const block = (form as Record<string, any>)[secKey]
  if (block) block.enabled = val
}

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

function addHero() {
  if (form.hero.items.length >= 5) {
    ElMessage.warning('首屏轮播最多 5 条')
    return
  }
  form.hero.items.push({
    enabled: true,
    sortOrder: (form.hero.items.length + 1) * 10,
    title: '让物联网与 AI 成为企业增长引擎',
    subtitle: '全场景实时感知、智能风控与数字化协同平台',
    desktopMediaId: null,
    mobileMediaId: null,
    imageAlt: '',
    buttonText: '预约演示',
    link: { type: 'CONSULT' },
  })
}

function addMetric() {
  if (form.metrics.items.length >= 8) {
    ElMessage.warning('关键指标最多 8 条')
    return
  }
  form.metrics.items.push({
    sortOrder: (form.metrics.items.length + 1) * 10,
    displayValue: '99.9',
    unit: '%',
    label: '平台可用率',
    description: '金融级高可用云原生架构保障',
  })
}

function addServiceGroup() {
  if (form.services.groups.length >= 3) {
    ElMessage.warning('服务分组最多 3 组')
    return
  }
  form.services.groups.push({
    sortOrder: (form.services.groups.length + 1) * 10,
    title: '智能硬件与 SaaS 平台',
    description: '软硬一体全栈自研，开箱即用赋能业务现场。',
    imageMediaId: null,
    contentType: 'PRODUCT',
    contentIds: [],
    moreLink: { type: 'PAGE', pageCode: 'PRODUCTS' },
  })
}

function applyPresetMetrics() {
  form.metrics.enabled = true
  form.metrics.title = '值得信赖的行业数字化基座'
  form.metrics.subtitle = '用真实在线运行规模验证每一份交付承诺'
  form.metrics.items = [
    { sortOrder: 10, displayValue: '320', unit: '万+', label: '在线物联网终端', description: '7×24h 毫秒级状态感知与采集' },
    { sortOrder: 20, displayValue: '7000', unit: '+', label: '头部标杆企业客户', description: '覆盖能源、冷链、危化、快消制造' },
    { sortOrder: 30, displayValue: '68', unit: '%', label: '高危事故率平均下降', description: 'AI 双目防碰撞与主动安全托管' },
    { sortOrder: 40, displayValue: '99.95', unit: '%', label: '云平台服务可用性', description: '多活容灾与国密级数据安全认证' },
  ]
  ElMessage.success('已填入行业标杆指标模板，保存草稿后生效')
}

async function onSave() {
  const payload = JSON.parse(JSON.stringify(form)) as HomeDraft & Record<string, unknown>
  if (await save(payload)) fill()
}

async function onPublish() {
  if (await publish()) fill()
}

onMounted(async () => {
  await load()
  fill()
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
    <div class="studio-banner">
      <div>
        <strong class="studio-banner__title">🎨 想要更直观地边看边改？试试「全站实景交互预演工作台」</strong>
        <p class="studio-banner__desc">
          支持在真实官网画布上直接拖拽调整模块顺序、双击修改文字、点击替换图片/视频、绑定链接，修改结果与当前表单 100% 双向实时互通。
        </p>
      </div>
      <el-button type="primary" @click="router.push('/preview/home')">
        进入实景交互预演台 →
      </el-button>
    </div>

    <el-alert v-if="loadError" type="error" :closable="false" :title="loadError" class="err" />

    <el-tabs v-model="activeTab" class="home-tabs">
      <!-- Tab 0: 模块摆放顺序与总览 -->
      <el-tab-pane label="0. 模块摆放顺序与开关" name="order">
        <p class="tab-hint">
          首屏 Hero 固定置顶展示；下方 6 大业务模块可自由上下移动调整在前台首页的展示顺序，也可一键开启或隐藏。
        </p>
        <div class="order-list">
          <div v-for="(secKey, idx) in form.sectionOrder" :key="secKey" class="order-item">
            <span class="order-item__num">0{{ idx + 1 }}</span>
            <div class="order-item__info">
              <strong>{{ SECTION_LABELS[secKey]?.name || secKey }}</strong>
              <span>{{ SECTION_LABELS[secKey]?.desc }}</span>
            </div>
            <el-switch
              v-if="secKey !== 'consultation'"
              :model-value="isSectionEnabled(secKey)"
              active-text="启用"
              inactive-text="隐藏"
              @update:model-value="(v: boolean) => toggleSectionEnabled(secKey, v)"
            />
            <el-tag v-else size="small" type="info">常驻收口区</el-tag>
            <div class="order-item__btns">
              <el-button size="small" :disabled="idx === 0" @click="moveSection(idx, -1)">↑ 上移</el-button>
              <el-button
                size="small"
                :disabled="idx === (form.sectionOrder?.length ?? 0) - 1"
                @click="moveSection(idx, 1)"
              >
                ↓ 下移
              </el-button>
              <el-button size="small" type="primary" link @click="activeTab = secKey">编辑内容</el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- Tab 1: 首屏 Hero -->
      <el-tab-pane label="1. 首屏轮播 (Hero)" name="hero">
        <p class="tab-hint">最多 5 条首屏，支持桌面大图/视频与移动端竖图自适应。正式发布至少需 1 条有效首屏。</p>
        <div v-for="(slide, idx) in form.hero.items" :key="slide.key ?? `hero-${idx}`" class="block-card">
          <div class="block-card__head">
            <strong>首屏 #{{ idx + 1 }}</strong>
            <el-switch v-model="slide.enabled" active-text="启用" />
            <el-button link type="danger" @click="form.hero.items.splice(idx, 1)">删除</el-button>
          </div>
          <el-form label-width="110px">
            <el-form-item label="主标题" required>
              <el-input v-model="slide.title" maxlength="40" show-word-limit placeholder="最多 40 字" />
            </el-form-item>
            <el-form-item label="副标题">
              <el-input v-model="slide.subtitle" type="textarea" :rows="2" maxlength="100" show-word-limit />
            </el-form-item>
            <el-form-item label="桌面图片/视频" required>
              <div class="media-pick-row">
                <el-tag v-if="slide.desktopMediaId" type="success">素材 ID: #{{ slide.desktopMediaId }}</el-tag>
                <span v-else class="muted">未选择</span>
                <el-button size="small" @click="openMedia('ALL', (m) => (slide.desktopMediaId = Number(m.id)))">
                  选择素材
                </el-button>
                <el-button v-if="slide.desktopMediaId" size="small" link type="danger" @click="slide.desktopMediaId = null">
                  清除
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="手机端配图">
              <div class="media-pick-row">
                <el-tag v-if="slide.mobileMediaId" type="info">素材 ID: #{{ slide.mobileMediaId }}</el-tag>
                <span v-else class="muted">可选，未选时自动回退桌面素材</span>
                <el-button size="small" @click="openMedia('IMAGE', (m) => (slide.mobileMediaId = Number(m.id)))">
                  选择图片
                </el-button>
                <el-button v-if="slide.mobileMediaId" size="small" link type="danger" @click="slide.mobileMediaId = null">
                  清除
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="主按钮文案">
              <el-input v-model="slide.buttonText" maxlength="12" style="width: 220px" placeholder="例如：预约演示" />
            </el-form-item>
            <el-form-item label="按钮跳转动作">
              <LinkEditor v-model="slide.link" :allow-consult="true" />
            </el-form-item>
          </el-form>
        </div>
        <el-button @click="addHero">+ 添加首屏轮播项</el-button>
      </el-tab-pane>

      <!-- Tab 2: 企业指标 -->
      <el-tab-pane label="2. 企业指标 (Metrics)" name="metrics">
        <div class="tab-toolbar">
          <el-switch v-model="form.metrics.enabled" active-text="在前台启用该模块" />
          <el-button size="small" type="primary" plain @click="applyPresetMetrics">
            ✨ 一键填入行业标杆指标模板
          </el-button>
        </div>
        <el-form label-width="110px">
          <el-form-item label="区块标题">
            <el-input v-model="form.metrics.title" maxlength="40" show-word-limit />
          </el-form-item>
          <el-form-item label="区块副标题">
            <el-input v-model="form.metrics.subtitle" maxlength="200" show-word-limit />
          </el-form-item>
        </el-form>
        <div v-for="(m, idx) in form.metrics.items" :key="m.key ?? `m-${idx}`" class="metric-row">
          <el-input v-model="m.displayValue" maxlength="16" placeholder="数值(如 320)" style="width: 130px" />
          <el-input v-model="m.unit" maxlength="8" placeholder="单位(万+)" style="width: 90px" />
          <el-input v-model="m.label" maxlength="20" placeholder="指标名称" style="width: 180px" />
          <el-input v-model="m.description" maxlength="80" placeholder="补充说明（选填）" style="flex: 1" />
          <el-button link type="danger" @click="form.metrics.items.splice(idx, 1)">删除</el-button>
        </div>
        <el-button @click="addMetric">+ 添加企业指标</el-button>
      </el-tab-pane>

      <!-- Tab 3: 产品与解决方案分组 -->
      <el-tab-pane label="3. 产品与服务 (Services)" name="services">
        <div class="tab-toolbar">
          <el-switch v-model="form.services.enabled" active-text="在前台启用该模块" />
        </div>
        <el-form label-width="110px">
          <el-form-item label="模块主标题">
            <el-input v-model="form.services.title" maxlength="40" show-word-limit />
          </el-form-item>
          <el-form-item label="模块副标题">
            <el-input v-model="form.services.subtitle" maxlength="200" show-word-limit />
          </el-form-item>
        </el-form>

        <div v-for="(grp, gIdx) in form.services.groups" :key="grp.key ?? `grp-${gIdx}`" class="block-card">
          <div class="block-card__head">
            <strong>服务分组 #{{ gIdx + 1 }}</strong>
            <el-button link type="danger" @click="form.services.groups.splice(gIdx, 1)">删除分组</el-button>
          </div>
          <el-form label-width="110px">
            <el-form-item label="分组标题" required>
              <el-input v-model="grp.title" maxlength="30" show-word-limit />
            </el-form-item>
            <el-form-item label="分组说明">
              <el-input v-model="grp.description" type="textarea" :rows="2" maxlength="200" show-word-limit />
            </el-form-item>

            <el-form-item label="关联类型">
              <el-radio-group v-model="grp.contentType" @change="grp.contentIds = []">
                <el-radio value="PRODUCT">产品 (PRODUCT)</el-radio>
                <el-radio value="SOLUTION">解决方案 (SOLUTION)</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="选择推荐内容">
              <ContentPicker v-model="grp.contentIds" :content-type="grp.contentType" :max="6" />
            </el-form-item>
            <el-form-item label="更多链接">
              <LinkEditor v-model="grp.moreLink" :allow-consult="true" />
            </el-form-item>
          </el-form>
        </div>
        <el-button @click="addServiceGroup">+ 添加服务分组（最多 3 组）</el-button>
      </el-tab-pane>

      <!-- Tab 4: 客户案例 -->
      <el-tab-pane label="4. 客户案例 (Cases)" name="cases">
        <div class="tab-toolbar">
          <el-switch v-model="form.cases.enabled" active-text="在前台启用该模块" />
        </div>
        <el-form label-width="110px">
          <el-form-item label="模块标题">
            <el-input v-model="form.cases.title" maxlength="40" show-word-limit />
          </el-form-item>
          <el-form-item label="模块副标题">
            <el-input v-model="form.cases.subtitle" maxlength="200" show-word-limit />
          </el-form-item>
          <el-form-item label="精选案例">
            <ContentPicker v-model="form.cases.contentIds" content-type="CASE" :max="6" />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- Tab 5: 合作客户标识 -->
      <el-tab-pane label="5. 客户标识墙 (Customers)" name="customers">
        <div class="tab-toolbar">
          <el-switch v-model="form.customers.enabled" active-text="在前台启用该模块" />
        </div>
        <el-form label-width="110px">
          <el-form-item label="模块标题">
            <el-input v-model="form.customers.title" maxlength="40" show-word-limit />
          </el-form-item>
          <el-form-item label="模块副标题">
            <el-input v-model="form.customers.subtitle" maxlength="200" show-word-limit />
          </el-form-item>
          <el-form-item label="选择客户标识">
            <ContentPicker v-model="form.customers.contentIds" content-type="CUSTOMER" :max="30" />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- Tab 6: 新闻动态 -->
      <el-tab-pane label="6. 新闻动态 (News)" name="news">
        <div class="tab-toolbar">
          <el-switch v-model="form.news.enabled" active-text="在前台启用该模块" />
        </div>
        <el-form label-width="110px">
          <el-form-item label="模块标题">
            <el-input v-model="form.news.title" maxlength="40" show-word-limit />
          </el-form-item>
          <el-form-item label="展示模式">
            <el-radio-group v-model="form.news.mode">
              <el-radio value="LATEST">按最新发布日期自动取前 N 条 (LATEST)</el-radio>
              <el-radio value="SELECTED">人工手选指定新闻 (SELECTED)</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="展示条数">
            <el-input-number v-model="form.news.limit" :min="1" :max="6" />
          </el-form-item>
          <el-form-item v-if="form.news.mode === 'SELECTED'" label="手选新闻">
            <ContentPicker v-model="form.news.contentIds" content-type="NEWS" :max="6" />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- Tab 7: 底部咨询区 -->
      <el-tab-pane label="7. 底部咨询区 (CTA)" name="consultation">
        <el-form label-width="110px">
          <el-form-item label="主标题" required>
            <el-input v-model="form.consultation.title" maxlength="40" show-word-limit />
          </el-form-item>
          <el-form-item label="副标题说明">
            <el-input v-model="form.consultation.subtitle" type="textarea" :rows="2" maxlength="200" show-word-limit />
          </el-form-item>
          <el-form-item label="按钮文字" required>
            <el-input v-model="form.consultation.buttonText" maxlength="12" style="width: 220px" />
          </el-form-item>
          <el-form-item label="按钮动作">
            <LinkEditor v-model="form.consultation.link" :allow-consult="true" :allow-none="false" />
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <MediaPicker v-model="pickerOpen" :kind="pickerKind" @select="onMediaPicked" />

    <template #tips>
      <p>首页模块摆放顺序与各区块内容均已与「实景交互预演工作台 (/preview/home)」双向实时打通。</p>
      <p>关联的产品、方案、案例、新闻与客户标识自动读取目标内容的最新线上发布版本，无需在首页重复录入标题和封面。</p>
    </template>
  </ConfigPanel>
</template>

<style scoped>
.studio-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  margin-bottom: 18px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f5f3ff 0%, #ede9fe 100%);
  border: 1px solid #c4b5fd;
  flex-wrap: wrap;
}

.studio-banner__title {
  font-size: 14.5px;
  color: #3730a3;
}

.studio-banner__desc {
  margin: 4px 0 0;
  font-size: 12.5px;
  color: #4f46e5;
}

.err {
  margin-bottom: 12px;
}

.tab-hint {
  font-size: 12.5px;
  color: var(--a-text-muted);
  margin: 0 0 14px;
}

.tab-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 18px;
  border: 1px solid var(--a-border);
  border-radius: 10px;
  background: #fafbfe;
}

.order-item__num {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: #eeebff;
  color: #5b4be8;
  font-weight: 700;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.order-item__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.order-item__info strong {
  font-size: 14px;
}

.order-item__info span {
  font-size: 12px;
  color: var(--a-text-muted);
}

.order-item__btns {
  display: flex;
  align-items: center;
  gap: 8px;
}

.block-card {
  border: 1px solid var(--a-border);
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 14px;
  background: #fafbfe;
}

.block-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.media-pick-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.muted {
  font-size: 12px;
  color: var(--a-text-muted);
}

.metric-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}
</style>
```

### `website/admin/src/views/PreviewHomeView.vue`

```vue
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
const showRightInspector = ref(true)
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
                <span class="assistant-pill">🤖 数字物流专家 小七</span>
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
                  <textarea
                    v-model="currentHero.title"
                    class="inline-textarea inline-textarea--hero-title"
                    rows="2"
                    maxlength="40"
                    placeholder="在此直接输入首屏大标题（支持换行）"
                    @change="recordSnapshot('修改首屏标题')"
                  ></textarea>
                  <textarea
                    v-model="currentHero.subtitle"
                    class="inline-textarea inline-textarea--hero-sub"
                    rows="2"
                    maxlength="100"
                    placeholder="在此输入首屏副标题说明..."
                    @change="recordSnapshot('修改首屏副标题')"
                  ></textarea>
                  <div class="canvas-hero__btn-edit">
                    <input
                      v-model="currentHero.buttonText"
                      class="inline-input inline-input--hero-btn"
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
                  <h1 class="canvas-hero__title">{{ currentHero.title }}</h1>
                  <p v-if="currentHero.subtitle" class="canvas-hero__sub">{{ currentHero.subtitle }}</p>
                  <span v-if="currentHero.buttonText" class="canvas-hero__cta">
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
```

### `website/server/src/main/java/com/company/cms/site/SiteConfigValidator.java`

```java
package com.company.cms.site;

import com.company.cms.common.ApiException;
import com.company.cms.common.ErrorCode;
import com.company.cms.common.FieldErrorItem;
import com.company.cms.common.HtmlSanitizer;
import com.company.cms.content.ContentRepository;
import com.company.cms.content.ContentRow;
import com.company.cms.media.MediaService;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 配置草稿校验与归一化（文档 6.1～6.4）。
 *
 * 保存草稿只校验结构、长度、数组上限、资源存在与链接安全；必填与「目标已发布」
 * 放到发布校验，保证运营可以分多次填完。
 */
@Component
public class SiteConfigValidator {

    private static final Set<String> PAGE_CODES = Set.of(
            "HOME", "PRODUCTS", "SOLUTIONS", "CASES", "NEWS", "ABOUT", "CONTACT", "PRIVACY");

    private final ObjectMapper objectMapper;
    private final HtmlSanitizer htmlSanitizer;
    private final LinkResolver links;
    private final MediaService mediaService;
    private final ContentRepository contents;

    public SiteConfigValidator(ObjectMapper objectMapper, HtmlSanitizer htmlSanitizer,
                                LinkResolver links, MediaService mediaService,
                                ContentRepository contents) {
        this.objectMapper = objectMapper;
        this.htmlSanitizer = htmlSanitizer;
        this.links = links;
        this.mediaService = mediaService;
        this.contents = contents;
    }

    public JsonNode validate(SiteConfigKey key, JsonNode draft) {
        if (draft == null || !draft.isObject()) {
            throw ApiException.validation("draft", "配置内容必须是 JSON 对象");
        }
        List<FieldErrorItem> errors = new ArrayList<>();
        ObjectNode normalized = switch (key) {
            case SITE -> site(draft, errors);
            case NAVIGATION -> navigation(draft, errors);
            case ABOUT -> about(draft, errors);
            case CONTACT -> contact(draft, errors);
            case PRIVACY -> privacy(draft, errors);
            case HOME -> home(draft, errors);
        };
        if (!errors.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "请检查配置内容", errors);
        }
        return normalized;
    }

    // -------------------------------------------------- SITE

    private ObjectNode site(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("siteName", limit(text(draft, "siteName"), 60, "siteName", "站点名称", errors));
        node.put("slogan", limit(text(draft, "slogan"), 100, "slogan", "标语", errors));
        // 旧字段 copyright 仍写入，公开接口取 copyrightText 优先
        String copyright = limit(text(draft, "copyrightText"), 120, "copyrightText", "版权文案", errors);
        if (copyright.isEmpty()) {
            copyright = limit(text(draft, "copyright"), 120, "copyrightText", "版权文案", errors);
        }
        node.put("copyrightText", copyright);
        node.put("icpText", limit(text(draft, "icpText"), 80, "icpText", "备案号", errors));
        String icpUrl = externalOrEmpty(draft, "icpUrl", "icpUrl", "备案链接", errors);
        node.put("icpUrl", icpUrl);
        node.put("policeText", limit(text(draft, "policeText"), 80, "policeText", "公安备案号", errors));
        node.put("policeUrl", externalOrEmpty(draft, "policeUrl", "policeUrl", "公安备案链接", errors));

        Long logoId = mediaId(draft.get("logoMediaId"), "logoMediaId", "Logo", errors);
        if (logoId != null) {
            node.put("logoMediaId", logoId);
        }
        node.put("logoAlt", limit(text(draft, "logoAlt"), 120, "logoAlt", "Logo 替代文字", errors));
        Long faviconId = mediaId(draft.get("faviconMediaId"), "faviconMediaId", "站点图标", errors);
        if (faviconId != null) {
            node.put("faviconMediaId", faviconId);
        }

        JsonNode seo = draft.get("defaultSeo");
        ObjectNode seoNode = objectMapper.createObjectNode();
        seoNode.put("title", limit(text(seo, "title"), 80, "defaultSeo.title", "默认标题", errors));
        seoNode.put("description", limit(text(seo, "description"), 200, "defaultSeo.description", "默认描述", errors));
        node.set("defaultSeo", seoNode);

        ArrayNode groups = objectMapper.createArrayNode();
        JsonNode footerGroups = draft.get("footerGroups");
        if (footerGroups != null && footerGroups.isArray()) {
            if (footerGroups.size() > 4) {
                errors.add(new FieldErrorItem("footerGroups", "页脚分组最多 4 组"));
            }
            for (int i = 0; i < Math.min(footerGroups.size(), 4); i++) {
                JsonNode group = footerGroups.get(i);
                String path = "footerGroups[" + i + "]";
                ObjectNode item = objectMapper.createObjectNode();
                String title = limit(text(group, "title"), 20, path + ".title", "分组标题", errors);
                if (title.isEmpty()) {
                    errors.add(new FieldErrorItem(path + ".title", "请填写分组标题"));
                }
                item.put("title", title);
                item.set("links", navLinks(group.get("links"), path + ".links", 10, errors));
                groups.add(item);
            }
        }
        node.set("footerGroups", groups);

        ArrayNode qrCodes = objectMapper.createArrayNode();
        JsonNode rawQrs = draft.get("qrCodes");
        if (rawQrs != null && rawQrs.isArray()) {
            for (int i = 0; i < Math.min(rawQrs.size(), 4); i++) {
                JsonNode q = rawQrs.get(i);
                if (q == null || !q.isObject()) continue;
                ObjectNode qNode = objectMapper.createObjectNode();
                qNode.put("label", limit(text(q, "label"), 20, "qrCodes[" + i + "].label", "二维码名称", errors));
                Long qMediaId = mediaId(q.get("mediaId"), "qrCodes[" + i + "].mediaId", "二维码图片", errors);
                if (qMediaId != null) {
                    qNode.put("mediaId", qMediaId);
                    qNode.put("imageUrl", "/api/public/media/" + qMediaId);
                } else {
                    String imgUrl = text(q, "imageUrl");
                    if (!imgUrl.isEmpty()) qNode.put("imageUrl", imgUrl);
                }
                qrCodes.add(qNode);
            }
        }
        node.set("qrCodes", qrCodes);

        // 联系方式统一在 CONTACT 维护，这里不再接受重复录入
        return node;
    }

    // -------------------------------------------------- NAVIGATION

    private ObjectNode navigation(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        JsonNode items = draft.get("items");
        ArrayNode result = objectMapper.createArrayNode();
        if (items == null || !items.isArray() || items.isEmpty()) {
            errors.add(new FieldErrorItem("items", "请至少配置一项导航"));
        } else {
            if (items.size() > 8) {
                errors.add(new FieldErrorItem("items", "顶级导航最多 8 项"));
            }
            Set<String> labels = new HashSet<>();
            Set<String> keys = new HashSet<>();
            for (int i = 0; i < Math.min(items.size(), 8); i++) {
                JsonNode item = items.get(i);
                String path = "items[" + i + "]";
                if (item == null || !item.isObject()) {
                    errors.add(new FieldErrorItem(path, "格式不正确"));
                    continue;
                }
                ObjectNode nav = objectMapper.createObjectNode();
                String key = stableKey(item, "nav", "key-" + i, keys, path, errors);
                nav.put("key", key);
                String label = limit(text(item, "label"), 20, path + ".label", "导航名称", errors);
                if (label.isEmpty()) {
                    errors.add(new FieldErrorItem(path + ".label", "请填写导航名称"));
                }
                if (!labels.add(label)) {
                    errors.add(new FieldErrorItem(path + ".label", "同级导航名称不能重复"));
                }
                nav.put("label", label);
                nav.put("enabled", bool(item, "enabled", true));
                nav.put("sortOrder", sortOrder(item, path, errors));
                nav.set("link", linkOrNull(item.get("link"), path + ".link", "导航链接", errors));

                JsonNode children = item.get("children");
                ArrayNode childNodes = objectMapper.createArrayNode();
                if (children != null && children.isArray() && !children.isEmpty()) {
                    if (children.size() > 10) {
                        errors.add(new FieldErrorItem(path + ".children", "子菜单最多 10 项"));
                    }
                    Set<String> childLabels = new HashSet<>();
                    Set<String> childKeys = new HashSet<>();
                    for (int j = 0; j < Math.min(children.size(), 10); j++) {
                        JsonNode child = children.get(j);
                        String childPath = path + ".children[" + j + "]";
                        if (child == null || !child.isObject()) {
                            errors.add(new FieldErrorItem(childPath, "格式不正确"));
                            continue;
                        }
                        ObjectNode childNode = objectMapper.createObjectNode();
                        childNode.put("key", stableKey(child, "nav", "key-c" + i + "-" + j,
                                childKeys, childPath, errors));
                        String childLabel = limit(text(child, "label"), 20,
                                childPath + ".label", "子菜单名称", errors);
                        if (childLabel.isEmpty()) {
                            errors.add(new FieldErrorItem(childPath + ".label", "请填写子菜单名称"));
                        }
                        if (!childLabels.add(childLabel)) {
                            errors.add(new FieldErrorItem(childPath + ".label", "同级导航名称不能重复"));
                        }
                        childNode.put("label", childLabel);
                        childNode.put("enabled", bool(child, "enabled", true));
                        childNode.put("sortOrder", sortOrder(child, childPath, errors));
                        childNode.set("link", linkOrNull(child.get("link"),
                                childPath + ".link", "子菜单链接", errors));
                        childNodes.add(childNode);
                    }
                }
                nav.set("children", childNodes);
                result.add(nav);
            }
        }
        node.set("items", result);

        // 产品/解决方案 5列全景下拉大菜单配置
        ArrayNode megaResult = objectMapper.createArrayNode();
        JsonNode mega = draft.get("megaColumns");
        if (mega != null && mega.isArray()) {
            if (mega.size() > 8) {
                errors.add(new FieldErrorItem("megaColumns", "大菜单列数最多 8 列"));
            }
            for (int i = 0; i < Math.min(mega.size(), 8); i++) {
                JsonNode col = mega.get(i);
                String path = "megaColumns[" + i + "]";
                if (col == null || !col.isObject()) continue;
                ObjectNode colNode = objectMapper.createObjectNode();
                String colTitle = limit(text(col, "title"), 30, path + ".title", "大菜单列标题", errors);
                colNode.put("title", colTitle);

                ArrayNode colItems = objectMapper.createArrayNode();
                JsonNode subItems = col.get("items");
                if (subItems != null && subItems.isArray()) {
                    if (subItems.size() > 15) {
                        errors.add(new FieldErrorItem(path + ".items", "每列最多 15 个项目"));
                    }
                    for (int j = 0; j < Math.min(subItems.size(), 15); j++) {
                        JsonNode sub = subItems.get(j);
                        String subPath = path + ".items[" + j + "]";
                        if (sub == null || !sub.isObject()) continue;
                        ObjectNode subNode = objectMapper.createObjectNode();
                        String label = limit(text(sub, "label"), 40, subPath + ".label", "项目名称", errors);
                        if (label.isEmpty()) {
                            errors.add(new FieldErrorItem(subPath + ".label", "请填写项目名称"));
                        }
                        subNode.put("label", label);
                        subNode.put("path", limit(text(sub, "path"), 255, subPath + ".path", "链接路径", errors));
                        subNode.put("tag", limit(text(sub, "tag"), 10, subPath + ".tag", "角标文案", errors));
                        subNode.put("isVideo", bool(sub, "isVideo", false));
                        subNode.put("videoUrl", limit(text(sub, "videoUrl"), 500, subPath + ".videoUrl", "视频链接", errors));
                        subNode.put("videoTitle", limit(text(sub, "videoTitle"), 100, subPath + ".videoTitle", "视频标题", errors));
                        colItems.add(subNode);
                    }
                }
                colNode.set("items", colItems);
                megaResult.add(colNode);
            }
        }
        node.set("megaColumns", megaResult);

        // 底部 CTA 引导条
        JsonNode cta = draft.get("megaCta");
        if (cta != null && cta.isObject()) {
            ObjectNode ctaNode = objectMapper.createObjectNode();
            ctaNode.put("text", limit(text(cta, "text"), 100, "megaCta.text", "底部提示语", errors));
            ctaNode.put("linkText", limit(text(cta, "linkText"), 50, "megaCta.linkText", "底部链接文字", errors));
            node.set("megaCta", ctaNode);
        }

        return node;
    }

    /** 页脚链接沿用导航项结构，但不允许二级。 */
    private ArrayNode navLinks(JsonNode source, String field, int max, List<FieldErrorItem> errors) {
        ArrayNode result = objectMapper.createArrayNode();
        if (source == null || !source.isArray()) {
            return result;
        }
        if (source.size() > max) {
            errors.add(new FieldErrorItem(field, "链接最多 " + max + " 项"));
        }
        Set<String> keys = new HashSet<>();
        for (int i = 0; i < Math.min(source.size(), max); i++) {
            JsonNode item = source.get(i);
            String path = field + "[" + i + "]";
            if (item == null || !item.isObject()) {
                errors.add(new FieldErrorItem(path, "格式不正确"));
                continue;
            }
            ObjectNode node = objectMapper.createObjectNode();
            node.put("key", stableKey(item, "lnk", "key-l" + i, keys, path, errors));
            String label = limit(text(item, "label"), 20, path + ".label", "链接名称", errors);
            if (label.isEmpty()) {
                errors.add(new FieldErrorItem(path + ".label", "请填写链接名称"));
            }
            node.put("label", label);
            node.put("enabled", bool(item, "enabled", true));
            node.put("sortOrder", sortOrder(item, path, errors));
            node.set("link", linkOrNull(item.get("link"), path + ".link", "链接", errors));
            result.add(node);
        }
        return result;
    }

    // -------------------------------------------------- ABOUT

    private ObjectNode about(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("heroTitle", limit(text(draft, "heroTitle"), 100, "heroTitle", "首屏标题", errors));
        node.put("introTitle", limit(text(draft, "introTitle"), 60, "introTitle", "简介标题", errors));
        node.put("intro", limit(text(draft, "intro"), 2000, "intro", "公司简介", errors));
        node.put("mission", limit(text(draft, "mission"), 2000, "mission", "使命", errors));
        node.put("vision", limit(text(draft, "vision"), 500, "vision", "愿景", errors));
        node.put("joinTitle", limit(text(draft, "joinTitle"), 60, "joinTitle", "招聘横幅标题", errors));
        node.put("joinButtonText", limit(text(draft, "joinButtonText"), 20, "joinButtonText", "招聘按钮文字", errors));
        if (draft.get("values") != null && draft.get("values").isArray()) {
            node.set("values", draft.get("values"));
        }
        if (draft.get("offices") != null && draft.get("offices").isArray()) {
            node.set("offices", draft.get("offices"));
        }

        ArrayNode milestones = objectMapper.createArrayNode();
        JsonNode source = draft.get("milestones");
        if (source != null && source.isArray()) {
            if (source.size() > 20) {
                errors.add(new FieldErrorItem("milestones", "发展历程最多 20 条"));
            }
            for (int i = 0; i < Math.min(source.size(), 20); i++) {
                JsonNode item = source.get(i);
                String path = "milestones[" + i + "]";
                String year = text(item, "year");
                if (!year.matches("^\\d{4}$")) {
                    errors.add(new FieldErrorItem(path + ".year", "年份需为 4 位数字"));
                }
                ObjectNode milestone = objectMapper.createObjectNode();
                milestone.put("year", year);
                milestone.put("title", limit(text(item, "title"), 50, path + ".title", "标题", errors));
                milestone.put("description",
                        limit(text(item, "description"), 300, path + ".description", "说明", errors));
                milestones.add(milestone);
            }
        }
        node.set("milestones", milestones);

        ArrayNode qualifications = objectMapper.createArrayNode();
        JsonNode quals = draft.get("qualifications");
        if (quals != null && quals.isArray()) {
            if (quals.size() > 20) {
                errors.add(new FieldErrorItem("qualifications", "资质最多 20 条"));
            }
            for (int i = 0; i < Math.min(quals.size(), 20); i++) {
                JsonNode item = quals.get(i);
                String path = "qualifications[" + i + "]";
                ObjectNode qual = objectMapper.createObjectNode();
                qual.put("name", limit(text(item, "name"), 60, path + ".name", "名称", errors));
                qual.put("description",
                        limit(text(item, "description"), 300, path + ".description", "说明", errors));
                qualifications.add(qual);
            }
        }
        node.set("qualifications", qualifications);
        return node;
    }

    // -------------------------------------------------- CONTACT

    private ObjectNode contact(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        String title = limit(text(draft, "title"), 50, "title", "页面标题", errors);
        node.put("title", title.isEmpty() ? "联系我们" : title);
        node.put("description", limit(text(draft, "description"), 300, "description", "页面描述", errors));
        node.put("successText", limit(text(draft, "successText"), 200, "successText", "提交成功提示", errors));
        node.put("formEnabled", bool(draft, "formEnabled", false));
        node.put("phone", limit(text(draft, "phone"), 40, "phone", "联系电话", errors));
        String email = limit(text(draft, "email"), 100, "email", "邮箱", errors);
        if (!email.isEmpty() && !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            errors.add(new FieldErrorItem("email", "邮箱格式不正确"));
        }
        node.put("email", email);
        node.put("address", limit(text(draft, "address"), 120, "address", "地址", errors));
        node.put("hours", limit(text(draft, "hours"), 60, "hours", "服务时间", errors));
        return node;
    }

    // -------------------------------------------------- PRIVACY

    private ObjectNode privacy(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        String updatedAt = text(draft, "updatedAt");
        try {
            node.put("updatedAt", LocalDate.parse(updatedAt).toString());
        } catch (DateTimeParseException ex) {
            errors.add(new FieldErrorItem("updatedAt", "更新日期格式需为 yyyy-MM-dd"));
            node.put("updatedAt", LocalDate.now().toString());
        }
        String body = htmlSanitizer.sanitize(text(draft, "bodyHtml"));
        if (body.isBlank()) {
            errors.add(new FieldErrorItem("bodyHtml", "隐私说明正文不能为空"));
        }
        node.put("bodyHtml", body);
        return node;
    }

    // -------------------------------------------------- HOME

    private static final List<String> DEFAULT_HOME_SECTIONS = List.of(
            "metrics", "services", "cases", "customers", "news", "consultation");

    private ObjectNode home(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        node.set("sectionOrder", sectionOrder(draft.get("sectionOrder")));
        node.set("hero", hero(draft.get("hero"), errors));
        node.set("metrics", metrics(draft.get("metrics"), errors));
        node.set("services", services(draft.get("services"), errors));
        node.set("cases", caseBlock(draft.get("cases"), errors));
        node.set("customers", customers(draft.get("customers"), errors));
        node.set("news", news(draft.get("news"), errors));
        node.set("consultation", consultation(draft.get("consultation"), errors));
        return node;
    }

    private ArrayNode sectionOrder(JsonNode source) {
        ArrayNode arr = objectMapper.createArrayNode();
        Set<String> added = new HashSet<>();
        if (source != null && source.isArray()) {
            for (JsonNode item : source) {
                String key = item != null && item.isString() ? item.asString().trim() : "";
                if (DEFAULT_HOME_SECTIONS.contains(key) && added.add(key)) {
                    arr.add(key);
                }
            }
        }
        for (String defKey : DEFAULT_HOME_SECTIONS) {
            if (added.add(defKey)) {
                arr.add(defKey);
            }
        }
        return arr;
    }

    private ObjectNode hero(JsonNode source, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        ArrayNode items = objectMapper.createArrayNode();
        JsonNode list = source == null ? null : source.get("items");
        if (list != null && list.isArray()) {
            if (list.size() > 5) {
                errors.add(new FieldErrorItem("hero.items", "首屏最多 5 条"));
            }
            Set<String> keys = new HashSet<>();
            for (int i = 0; i < Math.min(list.size(), 5); i++) {
                JsonNode item = list.get(i);
                String path = "hero.items[" + i + "]";
                if (item == null || !item.isObject()) {
                    errors.add(new FieldErrorItem(path, "格式不正确"));
                    continue;
                }
                ObjectNode heroItem = objectMapper.createObjectNode();
                heroItem.put("key", stableKey(item, "hero", "key-h" + i, keys, path, errors));
                heroItem.put("enabled", bool(item, "enabled", true));
                heroItem.put("sortOrder", sortOrder(item, path, errors));
                heroItem.put("title", limit(text(item, "title"), 40, path + ".title", "首屏标题", errors));
                heroItem.put("subtitle",
                        limit(text(item, "subtitle"), 100, path + ".subtitle", "首屏副标题", errors));
                Long desktop = mediaId(item.get("desktopMediaId"), path + ".desktopMediaId", "桌面图片", errors);
                if (desktop != null) {
                    heroItem.put("desktopMediaId", desktop);
                }
                Long mobile = mediaId(item.get("mobileMediaId"), path + ".mobileMediaId", "手机图片", errors);
                if (mobile != null) {
                    heroItem.put("mobileMediaId", mobile);
                }
                heroItem.put("imageAlt", limit(text(item, "imageAlt"), 120, path + ".imageAlt", "图片替代文字", errors));
                String buttonText = limit(text(item, "buttonText"), 12, path + ".buttonText", "按钮文案", errors);
                ObjectNode link = linkOrNull(item.get("link"), path + ".link", "按钮链接", errors);
                if (!buttonText.isEmpty() && link == null) {
                    errors.add(new FieldErrorItem(path + ".link", "填写按钮文案时必须配置链接"));
                }
                if (buttonText.isEmpty() && link != null && !"NONE".equals(text(link, "type"))) {
                    errors.add(new FieldErrorItem(path + ".buttonText", "配置链接时必须填写按钮文案"));
                }
                heroItem.put("buttonText", buttonText);
                heroItem.set("link", link);
                items.add(heroItem);
            }
        }
        node.set("items", items);
        return node;
    }

    private ObjectNode metrics(JsonNode source, List<FieldErrorItem> errors) {
        ObjectNode node = block(source, "metrics", "企业指标", errors);
        ArrayNode items = objectMapper.createArrayNode();
        JsonNode list = source == null ? null : source.get("items");
        if (list != null && list.isArray()) {
            if (list.size() > 8) {
                errors.add(new FieldErrorItem("metrics.items", "指标最多 8 条"));
            }
            Set<String> keys = new HashSet<>();
            for (int i = 0; i < Math.min(list.size(), 8); i++) {
                JsonNode item = list.get(i);
                String path = "metrics.items[" + i + "]";
                if (item == null || !item.isObject()) {
                    errors.add(new FieldErrorItem(path, "格式不正确"));
                    continue;
                }
                ObjectNode metric = objectMapper.createObjectNode();
                metric.put("key", stableKey(item, "metric", "key-m" + i, keys, path, errors));
                metric.put("sortOrder", sortOrder(item, path, errors));
                metric.put("displayValue",
                        limit(text(item, "displayValue"), 16, path + ".displayValue", "展示值", errors));
                metric.put("unit", limit(text(item, "unit"), 8, path + ".unit", "单位", errors));
                metric.put("label", limit(text(item, "label"), 20, path + ".label", "指标名称", errors));
                metric.put("description",
                        limit(text(item, "description"), 80, path + ".description", "指标说明", errors));
                items.add(metric);
            }
        }
        node.set("items", items);
        return node;
    }

    private ObjectNode services(JsonNode source, List<FieldErrorItem> errors) {
        ObjectNode node = block(source, "services", "产品服务", errors);
        ArrayNode groups = objectMapper.createArrayNode();
        JsonNode list = source == null ? null : source.get("groups");
        if (list != null && list.isArray()) {
            if (list.size() > 3) {
                errors.add(new FieldErrorItem("services.groups", "服务分组最多 3 组"));
            }
            Set<String> keys = new HashSet<>();
            for (int i = 0; i < Math.min(list.size(), 3); i++) {
                JsonNode group = list.get(i);
                String path = "services.groups[" + i + "]";
                if (group == null || !group.isObject()) {
                    errors.add(new FieldErrorItem(path, "格式不正确"));
                    continue;
                }
                ObjectNode item = objectMapper.createObjectNode();
                item.put("key", stableKey(group, "group", "key-g" + i, keys, path, errors));
                item.put("sortOrder", sortOrder(group, path, errors));
                item.put("title", limit(text(group, "title"), 30, path + ".title", "分组标题", errors));
                item.put("description",
                        limit(text(group, "description"), 200, path + ".description", "分组说明", errors));
                Long image = mediaId(group.get("imageMediaId"), path + ".imageMediaId", "分组配图", errors);
                if (image != null) {
                    item.put("imageMediaId", image);
                }
                String contentType = text(group, "contentType");
                if (!Set.of("PRODUCT", "SOLUTION").contains(contentType)) {
                    errors.add(new FieldErrorItem(path + ".contentType", "分组内容类型只能是产品或解决方案"));
                }
                item.put("contentType", contentType);
                ArrayNode ids = idArray(group.get("contentIds"), path + ".contentIds", 6, errors);
                if (ids.isEmpty()) {
                    errors.add(new FieldErrorItem(path + ".contentIds", "请选择 1 到 6 个内容"));
                }
                item.set("contentIds", ids);
                item.set("moreLink", linkOrNull(group.get("moreLink"), path + ".moreLink", "更多链接", errors));
                groups.add(item);
            }
        }
        node.set("groups", groups);
        return node;
    }

    private ObjectNode caseBlock(JsonNode source, List<FieldErrorItem> errors) {
        ObjectNode node = block(source, "cases", "客户案例", errors);
        node.set("contentIds", idArray(source == null ? null : source.get("contentIds"),
                "cases.contentIds", 6, errors));
        return node;
    }

    private ObjectNode customers(JsonNode source, List<FieldErrorItem> errors) {
        ObjectNode node = block(source, "customers", "客户标识", errors);
        node.set("contentIds", idArray(source == null ? null : source.get("contentIds"),
                "customers.contentIds", 30, errors));
        return node;
    }

    private ObjectNode news(JsonNode source, List<FieldErrorItem> errors) {
        ObjectNode node = block(source, "news", "新闻动态", errors);
        String mode = text(source, "mode");
        if (mode.isEmpty()) {
            mode = "SELECTED";
        }
        if (!Set.of("SELECTED", "LATEST").contains(mode)) {
            errors.add(new FieldErrorItem("news.mode", "新闻模式只能是 SELECTED 或 LATEST"));
            mode = "SELECTED";
        }
        node.put("mode", mode);
        int limit = intValue(source, "limit", 3);
        if (limit < 1 || limit > 6) {
            errors.add(new FieldErrorItem("news.limit", "条数需在 1 到 6 之间"));
            limit = Math.max(1, Math.min(6, limit));
        }
        node.put("limit", limit);
        node.set("contentIds", idArray(source == null ? null : source.get("contentIds"),
                "news.contentIds", 6, errors));
        return node;
    }

    private ObjectNode consultation(JsonNode source, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("title", limit(text(source, "title"), 40, "consultation.title", "咨询区标题", errors));
        node.put("subtitle",
                limit(text(source, "subtitle"), 200, "consultation.subtitle", "咨询区副标题", errors));
        node.put("buttonText",
                limit(text(source, "buttonText"), 12, "consultation.buttonText", "按钮文案", errors));
        node.set("link", linkOrNull(source == null ? null : source.get("link"),
                "consultation.link", "咨询动作", errors));
        return node;
    }

    /** 区块公共字段：enabled、title、subtitle。 */
    private ObjectNode block(JsonNode source, String field, String label, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("enabled", bool(source, "enabled", true));
        node.put("title", limit(text(source, "title"), 40, field + ".title", label + "标题", errors));
        node.put("subtitle", limit(text(source, "subtitle"), 200, field + ".subtitle", label + "副标题", errors));
        return node;
    }

    // -------------------------------------------------- 发布校验

    /**
     * 发布前的完整校验：必填、目标存在且已发布、类型匹配。
     * 与保存校验分开，避免运营填一半就被拦住。
     */
    public void validateForPublish(SiteConfigKey key, JsonNode draft) {
        List<FieldErrorItem> errors = new ArrayList<>();
        switch (key) {
            case HOME -> publishHome(draft, errors);
            case NAVIGATION -> publishNavigation(draft, errors);
            case SITE -> publishSite(draft, errors);
            case CONTACT -> publishContact(draft, errors);
            default -> {
            }
        }
        if (!errors.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "发布前请先补全以下内容", errors);
        }
    }

    private void publishSite(JsonNode draft, List<FieldErrorItem> errors) {
        if (text(draft, "siteName").isBlank()) {
            errors.add(new FieldErrorItem("siteName", "站点名称不能为空"));
        }
        if (text(draft, "copyrightText").isBlank()) {
            errors.add(new FieldErrorItem("copyrightText", "版权文案不能为空"));
        }
        JsonNode seo = draft.get("defaultSeo");
        if (text(seo, "title").isBlank()) {
            errors.add(new FieldErrorItem("defaultSeo.title", "默认标题不能为空"));
        }
        if (text(seo, "description").isBlank()) {
            errors.add(new FieldErrorItem("defaultSeo.description", "默认描述不能为空"));
        }
        String icpText = text(draft, "icpText");
        String icpUrl = text(draft, "icpUrl");
        if (!icpText.isEmpty() && icpUrl.isEmpty()) {
            errors.add(new FieldErrorItem("icpUrl", "填写备案号时需同时填写备案链接"));
        }
        if (icpText.isEmpty() && !icpUrl.isEmpty()) {
            errors.add(new FieldErrorItem("icpText", "填写备案链接时需同时填写备案号"));
        }
    }

    private void publishContact(JsonNode draft, List<FieldErrorItem> errors) {
        if (text(draft, "phone").isBlank() && text(draft, "email").isBlank()) {
            errors.add(new FieldErrorItem("phone", "联系电话与邮箱至少填写一项"));
        }
    }

    private void publishNavigation(JsonNode draft, List<FieldErrorItem> errors) {
        JsonNode items = draft.get("items");
        if (items == null || !items.isArray() || items.isEmpty()) {
            errors.add(new FieldErrorItem("items", "请至少配置一项导航"));
            return;
        }
        java.util.Map<String, Boolean> rendered = new java.util.HashMap<>();
        for (int i = 0; i < items.size(); i++) {
            JsonNode item = items.get(i);
            String path = "items[" + i + "]";
            boolean enabled = item.get("enabled") == null || item.get("enabled").asBoolean(true);
            if (!enabled) {
                continue;
            }
            JsonNode children = item.get("children");
            boolean hasChild = false;
            if (children != null && children.isArray()) {
                for (int j = 0; j < children.size(); j++) {
                    JsonNode child = children.get(j);
                    boolean childEnabled = child.get("enabled") == null || child.get("enabled").asBoolean(true);
                    if (!childEnabled) {
                        continue;
                    }
                    if (!links.publishable(child.get("link"), null)) {
                        errors.add(new FieldErrorItem(path + ".children[" + j + "].link",
                                "子菜单目标不存在或未发布"));
                    } else {
                        hasChild = true;
                    }
                }
            }
            String type = text(item.get("link"), "type");
            if (!hasChild) {
                if (!links.publishable(item.get("link"), null) || "NONE".equals(type)) {
                    errors.add(new FieldErrorItem(path + ".link", "导航项必须有有效链接或有效子菜单"));
                }
            }
            rendered.put(path, hasChild || !"NONE".equals(type));
        }
        if (rendered.values().stream().noneMatch(Boolean::booleanValue)) {
            errors.add(new FieldErrorItem("items", "没有可渲染的导航项"));
        }
    }

    private void publishHome(JsonNode draft, List<FieldErrorItem> errors) {
        JsonNode hero = draft.get("hero");
        JsonNode items = hero == null ? null : hero.get("items");
        int validHero = 0;
        if (items != null && items.isArray()) {
            for (int i = 0; i < items.size(); i++) {
                JsonNode item = items.get(i);
                boolean enabled = item.get("enabled") == null || item.get("enabled").asBoolean(true);
                if (!enabled) {
                    continue;
                }
                String path = "hero.items[" + i + "]";
                if (text(item, "title").isBlank()) {
                    errors.add(new FieldErrorItem(path + ".title", "首屏标题不能为空"));
                }
                if (item.get("desktopMediaId") == null) {
                    errors.add(new FieldErrorItem(path + ".desktopMediaId", "启用中的首屏必须有桌面图片"));
                } else if (!mediaExists(item.get("desktopMediaId").asLong())) {
                    errors.add(new FieldErrorItem(path + ".desktopMediaId", "桌面图片不存在"));
                }
                if (!links.publishable(item.get("link"), null)) {
                    errors.add(new FieldErrorItem(path + ".link", "按钮目标不存在或未发布"));
                }
                validHero++;
            }
        }
        if (validHero == 0) {
            errors.add(new FieldErrorItem("hero.items", "正式上线至少需要 1 条有效首屏"));
        }

        // metrics 无引用
        JsonNode services = draft.get("services");
        if (services != null && services.get("groups") != null && services.get("groups").isArray()) {
            for (int i = 0; i < services.get("groups").size(); i++) {
                JsonNode group = services.get("groups").get(i);
                String path = "services.groups[" + i + "]";
                if (text(group, "title").isBlank()) {
                    errors.add(new FieldErrorItem(path + ".title", "分组标题不能为空"));
                }
                String contentType = text(group, "contentType");
                JsonNode ids = group.get("contentIds");
                if (ids == null || ids.isEmpty()) {
                    errors.add(new FieldErrorItem(path + ".contentIds", "分组至少选择 1 个内容"));
                } else {
                    for (int j = 0; j < ids.size(); j++) {
                        long id = ids.get(j).asLong();
                        var row = contents.findPublishedById(id);
                        if (row.isEmpty() || !row.get().contentType().name().equals(contentType)) {
                            errors.add(new FieldErrorItem(path + ".contentIds[" + j + "]",
                                    "内容不存在、未发布或类型不匹配"));
                        }
                    }
                }
                if (!links.publishable(group.get("moreLink"), null)) {
                    errors.add(new FieldErrorItem(path + ".moreLink", "更多链接目标不存在或未发布"));
                }
            }
        }

        publishIdBlock(draft, "cases", "CASE", 6, errors);
        publishIdBlock(draft, "customers", "CUSTOMER", 30, errors);
        JsonNode news = draft.get("news");
        if (news != null && "SELECTED".equals(text(news, "mode"))) {
            publishIdBlock(draft, "news", "NEWS", 6, errors);
        }

        JsonNode consultation = draft.get("consultation");
        if (consultation != null) {
            if (text(consultation, "title").isBlank()) {
                errors.add(new FieldErrorItem("consultation.title", "咨询区标题不能为空"));
            }
            if (text(consultation, "buttonText").isBlank()) {
                errors.add(new FieldErrorItem("consultation.buttonText", "咨询按钮文案不能为空"));
            }
            links.publishable(consultation.get("link"), null);
            String type = text(consultation.get("link"), "type");
        }
    }

    private void publishIdBlock(JsonNode draft, String key, String typeName, int max,
                                List<FieldErrorItem> errors) {
        JsonNode block = draft.get(key);
        if (block == null) {
            return;
        }
        JsonNode ids = block.get("contentIds");
        if (ids != null && ids.isArray()) {
            for (int i = 0; i < ids.size(); i++) {
                long id = ids.get(i).asLong();
                var row = contents.findPublishedById(id);
                if (row.isEmpty() || !row.get().contentType().name().equals(typeName)) {
                    errors.add(new FieldErrorItem(key + ".contentIds[" + i + "]", "内容不存在、未发布或类型不匹配"));
                }
            }
        }
    }

    // -------------------------------------------------- 校验辅助工具

    private ObjectNode linkOrNull(JsonNode linkNode, String path, String label, List<FieldErrorItem> errors) {
        if (linkNode == null || linkNode.isNull() || !linkNode.isObject()) {
            return null;
        }
        String error = links.validateDraft(linkNode);
        if (error != null) {
            errors.add(new FieldErrorItem(path, label + error));
            return null;
        }
        return (ObjectNode) linkNode;
    }

    private Long mediaId(JsonNode node, String path, String label, List<FieldErrorItem> errors) {
        if (node == null || node.isNull()) {
            return null;
        }
        Long id = node.isNumber() ? node.asLong() : parseLong(node.asString());
        if (id == null || id <= 0) {
            errors.add(new FieldErrorItem(path, label + " ID 不合法"));
            return null;
        }
        if (!mediaExists(id)) {
            errors.add(new FieldErrorItem(path, label + "不存在，请重新选择"));
            return null;
        }
        return id;
    }

    private boolean mediaExists(long id) {
        try {
            mediaService.require(id);
            return true;
        } catch (ApiException ex) {
            return false;
        }
    }

    private ArrayNode idArray(JsonNode source, String path, int max, List<FieldErrorItem> errors) {
        ArrayNode result = objectMapper.createArrayNode();
        if (source == null || !source.isArray()) {
            return result;
        }
        if (source.size() > max) {
            errors.add(new FieldErrorItem(path, "最多选择 " + max + " 项"));
        }
        Set<Long> seen = new HashSet<>();
        for (int i = 0; i < Math.min(source.size(), max); i++) {
            JsonNode node = source.get(i);
            Long id = node.isNumber() ? node.asLong() : parseLong(node.asString());
            if (id == null || id <= 0) {
                errors.add(new FieldErrorItem(path + "[" + i + "]", "内容 ID 不合法"));
                continue;
            }
            if (!seen.add(id)) {
                errors.add(new FieldErrorItem(path + "[" + i + "]", "同一内容不能重复选择"));
                continue;
            }
            result.add(id);
        }
        return result;
    }

    private Long parseLong(String raw) {
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String stableKey(JsonNode item, String prefix, String fallback,
                            Set<String> used, String path, List<FieldErrorItem> errors) {
        String key = text(item, "key");
        if (key.isEmpty() || !key.matches("^[a-z0-9-]{4,40}$") || used.contains(key)) {
            key = fallback + "-" + Integer.toHexString(ThreadLocalRandom.current().nextInt(0x10000, 0xfffff));
            while (used.contains(key)) {
                key = prefix + "-" + Integer.toHexString(ThreadLocalRandom.current().nextInt(0x10000, 0xfffff));
            }
        }
        used.add(key);
        return key;
    }

    private int sortOrder(JsonNode item, String path, List<FieldErrorItem> errors) {
        int value = intValue(item, "sortOrder", 100);
        if (value < 0 || value > 9999) {
            errors.add(new FieldErrorItem(path + ".sortOrder", "排序需在 0 到 9999 之间"));
            return 100;
        }
        return value;
    }

    private int intValue(JsonNode node, String field, int fallback) {
        if (node == null) {
            return fallback;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return fallback;
        }
        return value.isNumber() ? value.asInt() : fallback;
    }

    private boolean bool(JsonNode node, String field, boolean fallback) {
        if (node == null) {
            return fallback;
        }
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? fallback : value.asBoolean(fallback);
    }

    private String externalOrEmpty(JsonNode node, String field, String path, String label,
                                    List<FieldErrorItem> errors) {
        String url = text(node, field).trim();
        if (url.isEmpty()) {
            return "";
        }
        String lower = url.toLowerCase();
        if (!lower.startsWith("http://") && !lower.startsWith("https://") || url.length() > 500) {
            errors.add(new FieldErrorItem(path, label + "必须是 http/https 完整地址"));
            return "";
        }
        return url;
    }

    private String text(JsonNode node, String field) {
        if (node == null) {
            return "";
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return "";
        }
        return value.isString() ? value.asString().trim() : value.asString();
    }

    private String limit(String value, int max, String path, String label, List<FieldErrorItem> errors) {
        if (value.length() > max) {
            errors.add(new FieldErrorItem(path, label + "不能超过 " + max + " 字"));
        }
        return value;
    }

}
```
