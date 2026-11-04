<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { openConsult } from '@/composables/consult'
import { openVideo } from '@/composables/videoModal'
import type { NavItem } from '@/data/types'

const props = defineProps<{
  siteName: string
  nav: NavItem[]
  logoUrl?: string | null
  logoAlt?: string
}>()

const route = useRoute()
const open = ref(false)
const openMenu = ref<string | null>(null)
const openMobile = ref<string | null>(null)
const scrolled = ref(false)

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

interface MegaSubItem {
  label: string
  path?: string
  tag?: string
  isVideo?: boolean
}

interface MegaColumn {
  title: string
  items: MegaSubItem[]
}

const defaultMegaColumns: MegaColumn[] = [
  {
    title: 'AI产品',
    items: [
      { label: '紫宝盒', path: '/products/1', tag: '首款', isVideo: false }
    ]
  },
  {
    title: 'IoT服务',
    items: [
      { label: '安全管家', path: '/products?category=安全', isVideo: false },
      { label: '冷链管家', path: '/products?category=冷链', isVideo: false },
      { label: '流向管家', path: '/products?category=流向', isVideo: false },
      { label: '追货管家', path: '/products?category=追货', isVideo: false }
    ]
  },
  {
    title: '交易服务',
    items: [
      { label: '财运通', path: '/solutions?category=交易', isVideo: false },
      { label: '数字能源', path: '/solutions?category=能源', isVideo: false }
    ]
  },
  {
    title: '解决方案',
    items: [
      { label: '大宗物流数字化', path: '/solutions/1', isVideo: false },
      { label: '连锁零售数字化', path: '/solutions/2', isVideo: false },
      { label: '饲料防偷盗 AI 治理', path: '/solutions/3', isVideo: false }
    ]
  },
  {
    title: '产品演示',
    items: [
      { label: '产品操作视频', path: '', isVideo: true }
    ]
  }
]

function handleMegaAction(subItem: MegaSubItem) {
  openMenu.value = null
  if (subItem.isVideo) {
    openVideo({
      title: '紫宝盒 | 物流行业首款全场景AI工具产品演示',
      url: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4'
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
                {{ item.label }}
                <span class="caret" aria-hidden="true"></span>
              </button>
              <Transition name="menu">
                <div
                  v-if="openMenu === item.label"
                  class="mega"
                  :class="{ 'mega--columns': item.label.includes('产品') || item.label.includes('方案') }"
                >
                  <!-- 对齐视频 5 列业务全景下拉菜单 -->
                  <template v-if="item.label.includes('产品') || item.label.includes('方案')">
                    <div class="mega-cols">
                      <div v-for="col in defaultMegaColumns" :key="col.title" class="mega-col">
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
                          <span class="mega__text">{{ child.label }}</span>
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
                          <span class="mega__text">{{ child.label }}</span>
                          <span class="mega__arrow" aria-hidden="true">→</span>
                        </a>
                        <RouterLink v-else :to="child.path" class="mega__link" @click="openMenu = null">
                          <span class="mega__bullet" aria-hidden="true"></span>
                          <span class="mega__text">{{ child.label }}</span>
                          <span class="mega__arrow" aria-hidden="true">→</span>
                        </RouterLink>
                      </li>
                    </ul>
                  </template>

                  <div class="mega__foot">
                    <span>需要专业场景建议？</span>
                    <button type="button" class="mega__foot-cta" @click="consult('NAVIGATION')">
                      联系技术专家 <span aria-hidden="true">→</span>
                    </button>
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
              {{ item.label }}
            </button>
            <a
              v-else-if="item.url"
              :href="item.url"
              class="nav__link"
              :target="item.newWindow ? '_blank' : undefined"
              :rel="item.newWindow ? 'noopener noreferrer' : undefined"
            >
              {{ item.label }}
            </a>
            <RouterLink
              v-else
              :to="item.path"
              class="nav__link"
              :class="{ 'is-active': isActive(item) }"
            >
              {{ item.label }}
            </RouterLink>
          </li>
        </ul>
      </nav>

      <div class="actions">
        <span class="lang" aria-hidden="true">English</span>
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
            <span class="assistant__sub">您好，我是您的</span>
            <span class="assistant__name">数字物流专家 小七</span>
          </span>
        </button>
        <button type="button" class="actions__cta" @click="consult('OTHER_CTA')">产品试用</button>
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
                {{ item.label }}
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
                    {{ child.label }}
                  </button>
                  <a
                    v-else-if="child.url"
                    :href="child.url"
                    class="mobile__sublink"
                    :target="child.newWindow ? '_blank' : undefined"
                    :rel="child.newWindow ? 'noopener noreferrer' : undefined"
                    @click="close"
                  >
                    {{ child.label }}
                  </a>
                  <RouterLink v-else :to="child.path" class="mobile__sublink" @click="close">
                    {{ child.label }}
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
              {{ item.label }}
            </button>
            <a
              v-else-if="item.url"
              :href="item.url"
              class="mobile__link"
              :target="item.newWindow ? '_blank' : undefined"
              :rel="item.newWindow ? 'noopener noreferrer' : undefined"
              @click="close"
            >
              {{ item.label }}
            </a>
            <RouterLink
              v-else
              :to="item.path"
              class="mobile__link"
              :class="{ 'is-active': isActive(item) }"
              @click="close"
            >
              {{ item.label }}
            </RouterLink>
          </li>
        </ul>
        <button type="button" class="btn btn--primary mobile__cta" @click="consult('OTHER_CTA')">
          预约演示
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
  gap: 36px;
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
  gap: 32px;
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
  width: 440px;
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

.mega {
  width: 520px;
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
  gap: 18px;
  flex: none;
}

.lang {
  font-size: 13px;
  color: var(--c-text-muted);
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

@media (max-width: 992px) {
  .actions__assistant {
    display: none;
  }
}

.actions__cta {
  min-height: 42px;
  padding: 0 20px;
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
  padding: 8px 20px 24px;
  max-height: calc(100dvh - var(--header-height));
  overflow-y: auto;
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

.mobile-enter-active,
.mobile-leave-active {
  transition: opacity 0.2s ease, transform 0.3s var(--ease-out);
}

.mobile-enter-from,
.mobile-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@media (max-width: 1080px) {
  .nav__list {
    gap: 22px;
  }
  .lang {
    display: none;
  }
}

@media (max-width: 900px) {
  .nav {
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
  .header__inner {
    gap: 16px;
  }
}

@media (max-width: 520px) {
  .actions__cta {
    display: none;
  }
  .brand__name {
    font-size: 16px;
  }
}
</style>
