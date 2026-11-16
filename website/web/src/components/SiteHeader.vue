<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getSite } from '@/api/public'
import { openConsult } from '@/composables/consult'
import { openVideo } from '@/composables/videoModal'
import { useRealtimeSync } from '@/composables/useRealtimeSync'
import { resolvePublicPhone } from '@/utils/siteDisplay'
import type { MegaColumn, MegaSubItem, NavItem, SiteConfig } from '@/data/types'

const route = useRoute()
const site = ref<SiteConfig | null>(null)
const mobileOpen = ref(false)
const scrolled = ref(false)
const megaOpen = ref(false)
let megaTimer: ReturnType<typeof setTimeout> | null = null

const isHomeTop = computed(() => route.path === '/' && !scrolled.value && !megaOpen.value)

const defaultNavItems: NavItem[] = [
  { label: '首页', path: '/' },
  { label: '产品中心', path: '/products' },
  { label: '解决方案', path: '/solutions' },
  { label: '客户案例', path: '/cases' },
  { label: '新闻中心', path: '/news' },
  { label: '关于我们', path: '/about' },
  { label: '联系我们', path: '/contact' },
]

const navItems = computed<NavItem[]>(() => {
  const items = site.value?.nav
  if (items && items.length > 0) return items
  return defaultNavItems
})

const megaColumns = computed<MegaColumn[]>(() => {
  const configured = site.value?.megaColumns
  if (configured && configured.length > 0) return configured
  const prodItem = site.value?.nav?.find(
    (item) => item.path === '/products' || item.label.includes('产品'),
  )
  if (prodItem?.children && prodItem.children.length > 0) {
    return [
      {
        title: prodItem.label || '产品与方案',
        items: prodItem.children.map((ch) => ({
          label: ch.label,
          path: ch.path || ch.url || '/products',
        })),
      },
    ]
  }
  return []
})

const hasMegaMenu = computed(() => megaColumns.value.length > 0)

function isMegaTrigger(item: NavItem) {
  return (
    hasMegaMenu.value &&
    (item.path === '/products' || item.label.includes('产品') || Boolean(item.children?.length))
  )
}

function openMega() {
  if (!hasMegaMenu.value) return
  if (megaTimer) clearTimeout(megaTimer)
  megaOpen.value = true
}

function closeMegaDelayed() {
  if (megaTimer) clearTimeout(megaTimer)
  megaTimer = setTimeout(() => {
    megaOpen.value = false
  }, 150)
}

function handleSubItemClick(sub: MegaSubItem) {
  megaOpen.value = false
  if (sub.isVideo && sub.videoUrl) {
    openVideo({ title: sub.videoTitle || sub.label, url: sub.videoUrl })
  }
}

function isExternal(item: NavItem) {
  const targetUrl = item.url || item.path || ''
  return item.linkType === 'URL' || /^https?:\/\//i.test(targetUrl)
}

function isActive(path: string) {
  if (path === '/') return route.path === '/'
  return route.path === path || route.path.startsWith(`${path}/`)
}

function handleCta() {
  mobileOpen.value = false
  megaOpen.value = false
  openConsult({
    sourcePosition: 'HEADER',
    title: '预约方案咨询',
  })
}

const displayPhone = computed(() =>
  resolvePublicPhone(site.value?.contact?.phone, null),
)

function onScroll() {
  scrolled.value = window.scrollY > 24
}

watch(
  () => route.fullPath,
  () => {
    mobileOpen.value = false
    megaOpen.value = false
  },
)

async function reloadHeaderConfig() {
  const siteData = await getSite().catch(() => null)
  if (siteData) site.value = siteData
}

useRealtimeSync(['siteConfigVersion'], () => {
  void reloadHeaderConfig()
})

onMounted(async () => {
  onScroll()
  window.addEventListener('scroll', onScroll, { passive: true })
  await reloadHeaderConfig()
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
  if (megaTimer) clearTimeout(megaTimer)
})
</script>

<template>
  <header
    class="site-header"
    :class="{
      'is-home-top': isHomeTop,
      'is-scrolled': scrolled || megaOpen || !isHomeTop,
    }"
  >
    <div class="site-header__bar">
      <!-- 左侧：品牌标识 + 主导航 -->
      <div class="site-header__left">
        <RouterLink to="/" class="brand" aria-label="返回首页">
          <img
            v-if="site?.logoUrl"
            :src="site.logoUrl"
            :alt="site.logoAlt || site.siteName || '企业官网'"
            class="brand__img"
          />
          <span v-else class="brand__text">
            {{ site?.siteName || '企业官网' }}
          </span>
        </RouterLink>

        <nav class="primary-nav" aria-label="主导航">
          <ul class="primary-nav__list">
            <li
              v-for="item in navItems"
              :key="item.label + item.path"
              class="primary-nav__item"
              @mouseenter="isMegaTrigger(item) ? openMega() : (megaOpen = false)"
              @mouseleave="isMegaTrigger(item) ? closeMegaDelayed() : undefined"
            >
              <a
                v-if="isExternal(item)"
                :href="item.url || item.path"
                :target="item.newWindow ? '_blank' : undefined"
                :rel="item.newWindow ? 'noopener noreferrer' : undefined"
                class="primary-nav__link"
              >
                <span>{{ item.label }}</span>
              </a>
              <RouterLink
                v-else
                :to="item.path"
                class="primary-nav__link"
                :class="{
                  'is-active': isActive(item.path) || (isMegaTrigger(item) && megaOpen),
                }"
              >
                <span>{{ item.label }}</span>
                <svg
                  v-if="isMegaTrigger(item)"
                  class="primary-nav__caret"
                  :class="{ 'is-open': megaOpen }"
                  width="10"
                  height="6"
                  viewBox="0 0 10 6"
                  fill="none"
                  aria-hidden="true"
                >
                  <path
                    d="M1 1L5 5L9 1"
                    stroke="currentColor"
                    stroke-width="1.5"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
              </RouterLink>
            </li>
          </ul>
        </nav>
      </div>

      <!-- 右侧：真实配置热线 + 咨询按钮（无伪双语切换） -->
      <div class="site-header__right">
        <a
          v-if="displayPhone"
          :href="`tel:${displayPhone.replace(/[\s-]/g, '')}`"
          class="header-hotline"
        >
          <span class="header-hotline__label">咨询热线：</span>
          <span class="header-hotline__num">{{ displayPhone }}</span>
        </a>

        <button type="button" class="header-cta" @click="handleCta">
          立即咨询
        </button>

        <button
          type="button"
          class="burger"
          :aria-expanded="mobileOpen"
          aria-controls="mobile-drawer"
          aria-label="展开或收起菜单"
          @click="mobileOpen = !mobileOpen"
        >
          <span></span>
          <span></span>
          <span></span>
        </button>
      </div>
    </div>

    <!-- 多列下拉面板：仅当后台配置了 megaColumns 或子菜单时渲染 -->
    <Transition name="mega-fade">
      <div
        v-if="megaOpen && hasMegaMenu"
        class="mega-panel"
        @mouseenter="openMega"
        @mouseleave="closeMegaDelayed"
      >
        <div class="mega-panel__inner">
          <div v-for="col in megaColumns" :key="col.title" class="mega-col">
            <h3 class="mega-col__title">{{ col.title }}</h3>
            <div class="mega-col__divider"></div>
            <ul class="mega-col__list">
              <li v-for="sub in col.items" :key="sub.label" class="mega-col__item">
                <a
                  v-if="sub.isVideo && sub.videoUrl"
                  href="javascript:void(0)"
                  class="mega-link"
                  @click.prevent="handleSubItemClick(sub)"
                >
                  <div class="mega-link__title-row">
                    <span class="mega-link__name">{{ sub.label }}</span>
                    <span v-if="sub.tag" class="mega-link__badge">{{ sub.tag }}</span>
                  </div>
                </a>
                <RouterLink
                  v-else
                  :to="sub.path || '/products'"
                  class="mega-link"
                  @click="megaOpen = false"
                >
                  <div class="mega-link__title-row">
                    <span class="mega-link__name">{{ sub.label }}</span>
                    <span v-if="sub.tag" class="mega-link__badge">{{ sub.tag }}</span>
                  </div>
                </RouterLink>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 移动端抽屉 -->
    <div v-if="mobileOpen" id="mobile-drawer" class="mobile-drawer">
      <ul class="mobile-drawer__list">
        <li v-for="item in navItems" :key="item.label + item.path">
          <RouterLink :to="item.path" class="mobile-drawer__link">
            {{ item.label }}
          </RouterLink>
        </li>
      </ul>
      <div class="mobile-drawer__foot">
        <a
          v-if="displayPhone"
          :href="`tel:${displayPhone.replace(/[\s-]/g, '')}`"
          class="mobile-drawer__phone"
        >
          咨询热线：{{ displayPhone }}
        </a>
        <button type="button" class="header-cta header-cta--full" @click="handleCta">
          立即咨询
        </button>
      </div>
    </div>
  </header>
</template>

<style scoped>
.site-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 60;
  height: 68px;
  transition: background-color 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid #e2e8f0;
  color: #0f172a;
}

.site-header.is-home-top {
  background: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(8px);
  border-bottom-color: rgba(255, 255, 255, 0.1);
  color: #ffffff;
}

.site-header.is-scrolled {
  background: rgba(255, 255, 255, 0.98);
  color: #0f172a;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.05);
}

.site-header__bar {
  max-width: 1380px;
  height: 68px;
  margin: 0 auto;
  padding: 0 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.site-header__left {
  display: flex;
  align-items: center;
  gap: 36px;
  height: 100%;
}

.brand {
  display: inline-flex;
  align-items: center;
  text-decoration: none;
  flex-shrink: 0;
}

.brand__img {
  height: 32px;
  width: auto;
}

.brand__text {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--c-brand);
}

.site-header.is-home-top .brand__text {
  color: #ffffff;
}

.primary-nav {
  height: 100%;
}

.primary-nav__list {
  display: flex;
  align-items: center;
  gap: 4px;
  height: 100%;
  list-style: none;
  margin: 0;
  padding: 0;
}

.primary-nav__item {
  height: 100%;
  display: flex;
  align-items: center;
}

.primary-nav__link {
  position: relative;
  height: 100%;
  padding: 0 14px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  color: inherit;
  transition: color 0.18s ease;
}

.primary-nav__link::after {
  content: '';
  position: absolute;
  left: 14px;
  right: 14px;
  bottom: 0;
  height: 2px;
  background: var(--c-brand);
  transform: scaleX(0);
  transition: transform 0.2s ease;
}

.site-header.is-home-top .primary-nav__link::after {
  background: #ffffff;
}

.primary-nav__link:hover::after,
.primary-nav__link.is-active::after {
  transform: scaleX(1);
}

.primary-nav__link.is-active {
  font-weight: 600;
  color: var(--c-brand);
}

.site-header.is-home-top .primary-nav__link.is-active {
  color: #ffffff;
}

.primary-nav__caret {
  transition: transform 0.2s ease;
  opacity: 0.75;
}

.primary-nav__caret.is-open {
  transform: rotate(180deg);
}

.site-header__right {
  display: flex;
  align-items: center;
  gap: 18px;
}

.header-hotline {
  display: inline-flex;
  align-items: center;
  font-size: 13.5px;
  color: inherit;
  text-decoration: none;
  white-space: nowrap;
}

.header-hotline__label {
  opacity: 0.75;
}

.header-hotline__num {
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.header-cta {
  height: 36px;
  padding: 0 18px;
  border: none;
  border-radius: 6px;
  background: var(--c-brand);
  color: #ffffff;
  font-size: 13.5px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.18s ease;
  white-space: nowrap;
}

.header-cta:hover {
  background: var(--c-brand-dark);
}

.header-cta--full {
  width: 100%;
  height: 42px;
}

.mega-panel {
  position: absolute;
  top: 68px;
  left: 0;
  right: 0;
  background: #ffffff;
  color: #0f172a;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 16px 36px rgba(15, 23, 42, 0.08);
  padding: 28px 0 32px;
}

.mega-panel__inner {
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 32px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 32px;
}

.mega-col__title {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 10px;
}

.mega-col__divider {
  height: 1px;
  background: #e2e8f0;
  margin-bottom: 12px;
}

.mega-col__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.mega-link {
  display: block;
  padding: 6px 8px;
  border-radius: 6px;
  text-decoration: none;
  transition: background-color 0.15s ease;
}

.mega-link:hover {
  background: #f8fafc;
}

.mega-link__title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mega-link__name {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.mega-link:hover .mega-link__name {
  color: var(--c-brand);
}

.mega-link__badge {
  font-size: 11px;
  font-weight: 600;
  color: var(--c-brand);
  background: var(--c-brand-soft);
  padding: 1px 6px;
  border-radius: 4px;
}

.mega-fade-enter-active,
.mega-fade-leave-active {
  transition: opacity 0.16s ease, transform 0.16s ease;
}

.mega-fade-enter-from,
.mega-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.burger {
  display: none;
  width: 38px;
  height: 38px;
  border: 1px solid currentColor;
  border-radius: 6px;
  background: transparent;
  color: inherit;
  padding: 9px 8px;
  cursor: pointer;
}

.burger span {
  display: block;
  height: 2px;
  background: currentColor;
  margin: 4px 0;
}

.mobile-drawer {
  background: #ffffff;
  color: #0f172a;
  border-bottom: 1px solid #e2e8f0;
  padding: 16px 24px 24px;
}

.mobile-drawer__list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.mobile-drawer__link {
  display: block;
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
  font-size: 15px;
  font-weight: 500;
  color: #0f172a;
}

.mobile-drawer__foot {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mobile-drawer__phone {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

@media (max-width: 1080px) {
  .primary-nav,
  .header-hotline {
    display: none;
  }
  .burger {
    display: inline-block;
  }
  .site-header__bar {
    padding: 0 20px;
  }
}
</style>
