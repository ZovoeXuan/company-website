<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCases, getNews, getProducts, getSolutions } from '@/api/public'
import { openConsult, openRoiCalculator } from '@/composables/consult'
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
const router = useRouter()
const open = ref(false)
const openMenu = ref<string | null>(null)
const openMobile = ref<string | null>(null)
const scrolled = ref(false)
const lang = ref<'zh' | 'en'>('zh')

// ==================== 全站 ⌘K / Ctrl+K 聚光灯搜索 ====================
interface SearchHit {
  id: string
  type: 'PRODUCT' | 'SOLUTION' | 'CASE' | 'NEWS'
  typeLabel: string
  title: string
  summary: string
  path: string
}

const searchOpen = ref(false)
const searchKeyword = ref('')
const searchLoading = ref(false)
const searchResults = ref<SearchHit[]>([])
const activeHitIndex = ref(0)
const searchInputRef = ref<HTMLInputElement | null>(null)
let searchTimer: number | null = null

const enLabelMap: Record<string, string> = {
  首页: 'Home',
  产品: 'Products',
  产品中心: 'Products',
  产品与方案: 'Products & Solutions',
  解决方案: 'Solutions',
  客户案例: 'Cases',
  标杆案例: 'Cases',
  新闻: 'News',
  新闻动态: 'News & Insights',
  洞察专栏: 'Insights',
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

function openSearchModal() {
  searchOpen.value = true
  activeHitIndex.value = 0
  window.setTimeout(() => {
    searchInputRef.value?.focus()
  }, 60)
  if (!searchResults.value.length && !searchKeyword.value.trim()) {
    runSpotlightSearch('')
  }
}

function closeSearchModal() {
  searchOpen.value = false
}

async function runSpotlightSearch(kw: string) {
  searchLoading.value = true
  try {
    const q = kw.trim() || undefined
    const [prods, sols, cases, news] = await Promise.all([
      getProducts({ page: 1, pageSize: 4, keyword: q }).catch(() => ({ items: [] })),
      getSolutions({ page: 1, pageSize: 4, keyword: q }).catch(() => ({ items: [] })),
      getCases({ page: 1, pageSize: 4, keyword: q }).catch(() => ({ items: [] })),
      getNews({ page: 1, pageSize: 4, keyword: q }).catch(() => ({ items: [] })),
    ])
    const hits: SearchHit[] = []
    for (const p of prods.items) {
      hits.push({
        id: `P-${p.id}`,
        type: 'PRODUCT',
        typeLabel: '产品',
        title: p.title,
        summary: p.summary || p.categoryName || '核心产品',
        path: `/products/${p.id}`,
      })
    }
    for (const s of sols.items) {
      hits.push({
        id: `S-${s.id}`,
        type: 'SOLUTION',
        typeLabel: '解决方案',
        title: s.title,
        summary: s.summary || s.industry || '行业解决方案',
        path: `/solutions/${s.id}`,
      })
    }
    for (const c of cases.items) {
      hits.push({
        id: `C-${c.id}`,
        type: 'CASE',
        typeLabel: '客户案例',
        title: c.title,
        summary: c.customerName ? `${c.customerName} · ${c.summary}` : c.summary,
        path: `/cases/${c.id}`,
      })
    }
    for (const n of news.items) {
      hits.push({
        id: `N-${n.id}`,
        type: 'NEWS',
        typeLabel: '新闻动态',
        title: n.title,
        summary: n.displayTime || n.summary,
        path: `/news/${n.id}`,
      })
    }
    searchResults.value = hits
    activeHitIndex.value = 0
  } finally {
    searchLoading.value = false
  }
}

watch(searchKeyword, (val) => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = window.setTimeout(() => {
    runSpotlightSearch(val)
  }, 200)
})

function goHit(hit: SearchHit) {
  searchOpen.value = false
  router.push(hit.path)
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
  if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 'k') {
    event.preventDefault()
    if (searchOpen.value) {
      closeSearchModal()
    } else {
      openSearchModal()
    }
    return
  }
  if (searchOpen.value) {
    if (event.key === 'Escape') {
      closeSearchModal()
      return
    }
    if (event.key === 'ArrowDown' && searchResults.value.length) {
      event.preventDefault()
      activeHitIndex.value = (activeHitIndex.value + 1) % searchResults.value.length
      return
    }
    if (event.key === 'ArrowUp' && searchResults.value.length) {
      event.preventDefault()
      activeHitIndex.value = (activeHitIndex.value - 1 + searchResults.value.length) % searchResults.value.length
      return
    }
    if (event.key === 'Enter' && searchResults.value[activeHitIndex.value]) {
      event.preventDefault()
      goHit(searchResults.value[activeHitIndex.value])
      return
    }
  }
  if (event.key !== 'Escape') return
  if (openMenu.value) {
    openMenu.value = null
    return
  }
  if (open.value) close()
}

let leaveTimer: number | null = null

function handleMouseEnter(item: NavItem) {
  if (!item.children?.length) {
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
    items: [{ label: '紫宝盒', path: '/products/1', tag: '首款', isVideo: false }],
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

const resolvedMegaCta = computed(() => ({
  text: props.megaCta?.text || '需要专业场景建议？',
  linkText: props.megaCta?.linkText || '联系技术专家',
}))

function handleMegaAction(subItem: MegaSubItem) {
  openMenu.value = null
  if (subItem.isVideo) {
    openVideo({
      title: subItem.videoTitle || '紫宝盒 | 物流行业首款全场景AI工具产品演示',
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
          <span class="brand__mark" aria-hidden="true">
            <span>{{ siteName.slice(0, 1) }}</span>
          </span>
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
            <template v-if="item.children?.length">
              <button
                type="button"
                class="nav__link nav__button"
                :class="{ 'is-active': isActive(item), 'is-open': openMenu === item.label }"
                :aria-expanded="openMenu === item.label"
                @click="toggleMenu(item)"
              >
                {{ displayNavLabel(item.label) }}
                <span class="caret" aria-hidden="true"></span>
              </button>
              <Transition name="menu">
                <div
                  v-if="openMenu === item.label"
                  class="mega"
                  :class="{ 'mega--columns': item.label.includes('产品') || item.label.includes('方案') }"
                >
                  <!-- 对齐视频 5 列业务全景下拉菜单（优先使用后台配置的 megaColumns） -->
                  <template v-if="item.label.includes('产品') || item.label.includes('方案')">
                    <div class="mega-cols">
                      <div v-for="col in resolvedMegaColumns" :key="col.title" class="mega-col">
                        <div class="mega-col__title">{{ col.title }}</div>
                        <ul class="mega-col__list">
                          <li v-for="sub in col.items" :key="sub.label">
                            <button
                              v-if="sub.isVideo"
                              type="button"
                              class="mega__link mega__link--video"
                              @click="handleMegaAction(sub)"
                            >
                              <span class="mega__bullet" aria-hidden="true"></span>
                              <span class="mega__text">{{ sub.label }}</span>
                              <span class="mega__video-badge">▶ 演示</span>
                            </button>
                            <RouterLink
                              v-else-if="sub.path"
                              :to="sub.path"
                              class="mega__link"
                              @click="openMenu = null"
                            >
                              <span class="mega__bullet" aria-hidden="true"></span>
                              <span class="mega__text">{{ sub.label }}</span>
                              <span v-if="sub.tag" class="mega__item-tag">{{ sub.tag }}</span>
                              <span class="mega__arrow" aria-hidden="true">→</span>
                            </RouterLink>
                          </li>
                        </ul>
                      </div>
                    </div>
                  </template>

                  <!-- 常规多子级下拉菜单 -->
                  <template v-else>
                    <div class="mega__head">
                      <span class="mega__kicker">{{ item.label }} · 业务全景</span>
                      <span class="mega__note">选择方向，查看对应内容</span>
                    </div>
                    <ul class="mega__list">
                      <li v-for="child in item.children" :key="child.label">
                        <button
                          v-if="child.linkType === 'CONSULT'"
                          type="button"
                          class="mega__link"
                          @click="consult()"
                        >
                          <span class="mega__bullet" aria-hidden="true"></span>
                          <span class="mega__text">{{ displayNavLabel(child.label) }}</span>
                          <span class="mega__arrow" aria-hidden="true">→</span>
                        </button>
                        <a
                          v-else-if="child.url"
                          :href="child.url"
                          class="mega__link"
                          :target="child.newWindow ? '_blank' : undefined"
                          :rel="child.newWindow ? 'noopener noreferrer' : undefined"
                          @click="openMenu = null"
                        >
                          <span class="mega__bullet" aria-hidden="true"></span>
                          <span class="mega__text">{{ displayNavLabel(child.label) }}</span>
                          <span class="mega__arrow" aria-hidden="true">→</span>
                        </a>
                        <RouterLink v-else :to="child.path" class="mega__link" @click="openMenu = null">
                          <span class="mega__bullet" aria-hidden="true"></span>
                          <span class="mega__text">{{ displayNavLabel(child.label) }}</span>
                          <span class="mega__arrow" aria-hidden="true">→</span>
                        </RouterLink>
                      </li>
                    </ul>
                  </template>

                  <div class="mega__foot">
                    <span>{{ resolvedMegaCta.text }}</span>
                    <div class="mega__foot-actions">
                      <button
                        type="button"
                        class="mega__foot-cta mega__foot-cta--roi"
                        @click="openMenu = null; openRoiCalculator()"
                      >
                        🧮 测算降本 ROI
                      </button>
                      <button type="button" class="mega__foot-cta" @click="consult('NAVIGATION')">
                        {{ resolvedMegaCta.linkText }} <span aria-hidden="true">→</span>
                      </button>
                    </div>
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

      <div class="actions">
        <!-- ⌘K 聚光灯搜索入口 -->
        <button
          type="button"
          class="spotlight-trigger"
          title="全站搜索 (Ctrl+K / ⌘K)"
          @click="openSearchModal"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
            <circle cx="11" cy="11" r="8"></circle>
            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
          </svg>
          <span class="spotlight-trigger__text">{{ lang === 'en' ? 'Search' : '搜索' }}</span>
          <kbd class="spotlight-trigger__kbd">⌘K</kbd>
        </button>

        <!-- 中英双语切换 -->
        <button type="button" class="lang-btn" title="切换语言 / Switch Language" @click="toggleLang">
          {{ lang === 'zh' ? 'EN' : '中文' }}
        </button>

        <button
          type="button"
          class="actions__assistant"
          title="点击与数字物流专家在线交流"
          @click="consult('NAVIGATION')"
        >
          <span class="assistant__avatar">
            <svg class="assistant__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <rect x="3" y="11" width="18" height="10" rx="2"></rect>
              <circle cx="12" cy="5" r="2"></circle>
              <path d="M12 7v4"></path>
              <line x1="8" y1="16" x2="8.01" y2="16"></line>
              <line x1="16" y1="16" x2="16.01" y2="16"></line>
            </svg>
          </span>
          <span class="assistant__meta">
            <span class="assistant__sub">{{ lang === 'en' ? 'AI Expert' : '您好，我是您的' }}</span>
            <span class="assistant__name">{{ lang === 'en' ? 'Ask XiaoQi' : '数字物流专家 小七' }}</span>
          </span>
        </button>
        <button type="button" class="actions__cta" @click="consult('OTHER_CTA')">
          {{ lang === 'en' ? 'Book Demo' : '产品试用' }}
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
        <div class="mobile__top-tools">
          <button type="button" class="mobile__search-btn" @click="close(); openSearchModal()">
            🔍 搜索产品、方案、案例或动态...
          </button>
          <button type="button" class="lang-btn" @click="toggleLang">
            {{ lang === 'zh' ? 'English' : '中文' }}
          </button>
        </div>
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
          预约演示
        </button>
      </nav>
    </Transition>

    <!-- 全站 ⌘K / Ctrl+K 聚光灯搜索弹窗 -->
    <Teleport to="body">
      <div v-if="searchOpen" class="spotlight-mask" @click.self="closeSearchModal">
        <div class="spotlight-panel" role="dialog" aria-modal="true" aria-label="全站搜索">
          <div class="spotlight-bar">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
            <input
              ref="searchInputRef"
              v-model="searchKeyword"
              type="text"
              class="spotlight-input"
              placeholder="输入关键词即时检索：产品、解决方案、标杆案例、新闻动态..."
            />
            <button v-if="searchKeyword" type="button" class="spotlight-clear" @click="searchKeyword = ''">
              清空
            </button>
            <button type="button" class="spotlight-esc" @click="closeSearchModal">ESC</button>
          </div>

          <div class="spotlight-quick">
            <span class="spotlight-quick__lbl">快捷直达：</span>
            <button type="button" class="spotlight-chip" @click="closeSearchModal(); openRoiCalculator()">
              🧮 ROI 降本增效测算器
            </button>
            <button type="button" class="spotlight-chip" @click="closeSearchModal(); consult('NAVIGATION')">
              🤖 唤醒数字专家小七
            </button>
            <button type="button" class="spotlight-chip" @click="closeSearchModal(); router.push('/products')">
              📦 全部产品
            </button>
            <button type="button" class="spotlight-chip" @click="closeSearchModal(); router.push('/solutions')">
              💡 行业方案
            </button>
            <button type="button" class="spotlight-chip" @click="closeSearchModal(); router.push('/cases')">
              🏆 客户案例
            </button>
          </div>

          <div class="spotlight-body">
            <div v-if="searchLoading" class="spotlight-state">正在检索全站已发布内容...</div>
            <div v-else-if="searchResults.length === 0" class="spotlight-state">
              未找到与「{{ searchKeyword }}」匹配的内容，可直接唤起小七专家为您解答。
            </div>
            <ul v-else class="spotlight-list">
              <li
                v-for="(hit, idx) in searchResults"
                :key="hit.id"
                class="spotlight-item"
                :class="{ 'is-active': idx === activeHitIndex }"
                @mouseenter="activeHitIndex = idx"
                @click="goHit(hit)"
              >
                <span class="spotlight-item__badge" :class="`badge--${hit.type.toLowerCase()}`">
                  {{ hit.typeLabel }}
                </span>
                <div class="spotlight-item__main">
                  <p class="spotlight-item__title">{{ hit.title }}</p>
                  <p v-if="hit.summary" class="spotlight-item__sub">{{ hit.summary }}</p>
                </div>
                <span class="spotlight-item__enter">↵ 跳转</span>
              </li>
            </ul>
          </div>

          <div class="spotlight-foot">
            <span>↑↓ 键切换结果 · Enter 跳转详情 · ESC 关闭</span>
            <span>仅检索线上已发布内容</span>
          </div>
        </div>
      </div>
    </Teleport>
  </header>
</template>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 40;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: saturate(180%) blur(14px);
  border-bottom: 1px solid transparent;
  transition: background-color 0.3s ease, border-color 0.3s ease, box-shadow 0.3s ease;
}

.header.is-scrolled {
  background: rgba(255, 255, 255, 0.98);
  border-bottom-color: var(--c-border);
  box-shadow: 0 8px 32px rgba(10, 12, 20, 0.05);
}

.header__inner {
  display: flex;
  align-items: center;
  gap: 32px;
  height: var(--header-height);
  transition: height 0.3s var(--ease-out);
}

.header.is-scrolled .header__inner {
  height: 68px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: none;
}

.brand__logo {
  max-height: 34px;
  max-width: 168px;
  object-fit: contain;
}

.brand__mark {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: linear-gradient(135deg, #7a6bff 0%, #5b4be8 60%, #3a2ac4 100%);
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 600;
  box-shadow: 0 6px 18px rgba(91, 75, 232, 0.28);
}

.brand__name {
  font-size: 17px;
  font-weight: 600;
  letter-spacing: -0.02em;
}

.nav {
  flex: 1;
  min-width: 0;
}

.nav__list {
  display: flex;
  gap: 28px;
  list-style: none;
  margin: 0;
  padding: 0;
}

.nav__item {
  position: relative;
}

.nav__link {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 0;
  font-size: 15px;
  color: var(--c-text-muted);
  background: none;
  border: none;
  cursor: pointer;
  font-family: inherit;
  transition: color 0.2s ease;
}

.nav__link::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 2px;
  height: 2px;
  background: var(--c-brand);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.3s var(--ease-out);
}

.nav__link:hover,
.nav__link.is-active {
  color: var(--c-text);
}

.nav__link.is-active::after {
  transform: scaleX(1);
}

.caret {
  width: 6px;
  height: 6px;
  border-right: 1.5px solid currentColor;
  border-bottom: 1.5px solid currentColor;
  transform: rotate(45deg) translateY(-2px);
  transition: transform 0.25s var(--ease-out);
}

.nav__button.is-open .caret {
  transform: rotate(225deg) translateY(-2px);
}

.mega {
  position: absolute;
  top: calc(100% + 10px);
  left: -24px;
  width: 520px;
  padding: 22px 24px 18px;
  background: #fff;
  border: 1px solid var(--c-border);
  border-radius: 18px;
  box-shadow: 0 28px 64px rgba(10, 12, 20, 0.14);
  z-index: 30;
}

.mega__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--c-border);
  margin-bottom: 8px;
}

.mega__kicker {
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--c-brand);
  font-weight: 600;
}

.mega__note {
  font-size: 12px;
  color: var(--c-text-subtle);
}

.mega__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2px 18px;
}

.mega__link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  padding: 11px 10px;
  border: none;
  background: none;
  border-radius: 8px;
  font-family: inherit;
  font-size: 14px;
  color: var(--c-text);
  text-align: left;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.mega__link:hover {
  background: var(--c-brand-soft);
  color: var(--c-brand);
}

.mega--columns {
  width: min(980px, 92vw);
  left: 50%;
  transform: translateX(-40%);
}

.mega-cols {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 20px;
  padding: 10px 0 16px;
}

.mega-col__title {
  font-size: 13px;
  font-weight: 700;
  color: var(--c-text-muted);
  letter-spacing: 0.04em;
  padding-bottom: 12px;
  margin-bottom: 8px;
  border-bottom: 1px solid var(--c-border);
}

.mega-col__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.mega-col__list .mega__link {
  padding: 8px 6px;
  font-size: 13.5px;
}

.mega__video-badge {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(91, 75, 232, 0.1);
  color: var(--c-brand);
  font-weight: 500;
}

.mega__item-tag {
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 3px;
  background: var(--c-accent, #ff4d4f);
  color: #fff;
  font-weight: 600;
  margin-right: auto;
  margin-left: 6px;
}

.mega__bullet {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--c-brand, #5b4be8);
  opacity: 0.4;
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.mega__link:hover .mega__bullet {
  opacity: 1;
  transform: scale(1.3);
}

.mega__text {
  flex: 1;
}

.mega__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid var(--c-border);
  font-size: 13px;
  color: var(--c-text-muted);
}

.mega__foot-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.mega__foot-cta {
  border: none;
  background: none;
  padding: 0;
  font-family: inherit;
  font-size: 13px;
  font-weight: 500;
  color: var(--c-brand);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: transform 0.2s ease;
}

.mega__foot-cta--roi {
  padding: 4px 10px;
  border-radius: 6px;
  background: #eeebff;
}

.mega__foot-cta:hover {
  transform: translateX(3px);
}

.mega__arrow {
  opacity: 0;
  transform: translateX(-6px);
  transition: opacity 0.2s ease, transform 0.25s var(--ease-out);
}

.mega__link:hover .mega__arrow {
  opacity: 1;
  transform: none;
}

.menu-enter-active,
.menu-leave-active {
  transition: opacity 0.2s ease, transform 0.25s var(--ease-out);
}

.menu-enter-from,
.menu-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: none;
}

.spotlight-trigger {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  padding: 0 10px;
  border-radius: 999px;
  border: 1px solid var(--c-border-strong);
  background: #f8f9fc;
  color: var(--c-text-muted);
  font-size: 12.5px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.spotlight-trigger:hover {
  border-color: var(--c-brand);
  color: var(--c-brand);
  background: #ffffff;
}

.spotlight-trigger__kbd {
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 4px;
  background: rgba(16, 16, 20, 0.06);
  font-family: inherit;
}

.lang-btn {
  height: 32px;
  padding: 0 10px;
  border-radius: 6px;
  border: 1px solid var(--c-border);
  background: transparent;
  font-size: 12.5px;
  font-weight: 600;
  color: var(--c-text-muted);
  cursor: pointer;
  transition: all 0.2s ease;
}

.lang-btn:hover {
  color: var(--c-brand);
  border-color: var(--c-brand);
}

.actions__assistant {
  display: flex;
  align-items: center;
  gap: 10px;
  background: none;
  border: none;
  padding: 4px 10px;
  border-radius: 999px;
  cursor: pointer;
  font-family: inherit;
  transition: background-color 0.2s ease;
}

.actions__assistant:hover {
  background: var(--c-surface);
}

.assistant__avatar {
  position: relative;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #7c3aed, #4f46e5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.35);
  flex: none;
}

.assistant__icon {
  width: 17px;
  height: 17px;
}

.assistant__meta {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  text-align: left;
  line-height: 1.25;
}

.assistant__sub {
  font-size: 11px;
  color: var(--c-text-subtle);
}

.assistant__name {
  font-size: 12px;
  font-weight: 600;
  color: var(--c-brand);
}

@media (max-width: 1120px) {
  .actions__assistant {
    display: none;
  }
}

.actions__cta {
  min-height: 40px;
  padding: 0 18px;
  border: none;
  border-radius: 6px;
  background: var(--c-text);
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: background-color 0.2s ease, transform 0.2s var(--ease-out);
}

.actions__cta:hover {
  background: var(--c-brand);
  transform: translateY(-1px);
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
  transition: transform 0.25s var(--ease-out), opacity 0.2s ease;
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
  max-height: calc(100dvh - var(--header-height));
  overflow-y: auto;
}

.mobile__top-tools {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.mobile__search-btn {
  flex: 1;
  height: 36px;
  padding: 0 12px;
  border-radius: 8px;
  border: 1px solid var(--c-border-strong);
  background: #f8f9fc;
  color: var(--c-text-muted);
  font-size: 13px;
  text-align: left;
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
  padding: 16px 4px;
  font-size: 17px;
  color: var(--c-text);
  border: none;
  border-bottom: 1px solid var(--c-border);
  background: none;
  font-family: inherit;
  text-align: left;
  cursor: pointer;
}

.mobile__link.is-active {
  color: var(--c-brand);
  font-weight: 600;
}

.mobile__sub {
  list-style: none;
  margin: 0;
  padding: 6px 0 10px 14px;
}

.mobile__sublink {
  display: block;
  padding: 11px 4px;
  font-size: 14px;
  color: var(--c-text-muted);
}

.mobile__sublink--button {
  width: 100%;
  border: none;
  background: none;
  text-align: left;
  font-family: inherit;
  cursor: pointer;
}

.mobile__cta {
  margin-top: 18px;
  width: 100%;
}

/* 聚光灯搜索弹窗样式 */
.spotlight-mask {
  position: fixed;
  inset: 0;
  z-index: 100;
  background: rgba(10, 12, 22, 0.58);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 10vh 16px 24px;
}

.spotlight-panel {
  width: min(680px, 96vw);
  background: #ffffff;
  border-radius: 18px;
  border: 1px solid rgba(16, 16, 20, 0.1);
  box-shadow: 0 28px 80px rgba(8, 10, 22, 0.28);
  overflow: hidden;
}

.spotlight-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--c-border);
  color: var(--c-brand);
}

.spotlight-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15.5px;
  color: var(--c-text);
  font-family: inherit;
}

.spotlight-clear {
  font-size: 12px;
  color: var(--c-text-muted);
  cursor: pointer;
}

.spotlight-esc {
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 6px;
  background: #f1f3f7;
  color: #5f6470;
  font-weight: 600;
}

.spotlight-quick {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding: 10px 20px;
  background: #fafbfe;
  border-bottom: 1px solid var(--c-border);
}

.spotlight-quick__lbl {
  font-size: 12px;
  color: var(--c-text-subtle);
}

.spotlight-chip {
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid rgba(91, 75, 232, 0.18);
  background: #ffffff;
  color: #4b5563;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.18s ease;
}

.spotlight-chip:hover {
  background: #5b4be8;
  color: #ffffff;
  border-color: #5b4be8;
}

.spotlight-body {
  max-height: 380px;
  overflow-y: auto;
  padding: 10px 12px;
}

.spotlight-state {
  padding: 36px 20px;
  text-align: center;
  font-size: 14px;
  color: var(--c-text-muted);
}

.spotlight-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.spotlight-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.spotlight-item.is-active {
  background: #f5f3ff;
}

.spotlight-item__badge {
  font-size: 11px;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: 6px;
  flex: none;
}

.badge--product {
  background: #eeebff;
  color: #5b4be8;
}

.badge--solution {
  background: #e0f2fe;
  color: #0284c7;
}

.badge--case {
  background: #fef3c7;
  color: #d97706;
}

.badge--news {
  background: #dcfce7;
  color: #15803d;
}

.spotlight-item__main {
  flex: 1;
  min-width: 0;
}

.spotlight-item__title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--c-text);
}

.spotlight-item__sub {
  margin: 2px 0 0;
  font-size: 12px;
  color: var(--c-text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.spotlight-item__enter {
  font-size: 12px;
  color: var(--c-brand);
  font-weight: 500;
}

.spotlight-foot {
  display: flex;
  justify-content: space-between;
  padding: 10px 20px;
  background: #f8f9fc;
  border-top: 1px solid var(--c-border);
  font-size: 11.5px;
  color: var(--c-text-subtle);
}

@media (max-width: 900px) {
  .nav,
  .spotlight-trigger,
  .lang-btn {
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

@media (max-width: 520px) {
  .actions__cta {
    display: none;
  }
}
</style>
