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
