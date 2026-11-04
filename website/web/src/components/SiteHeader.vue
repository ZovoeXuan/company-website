<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { openConsult } from '@/composables/consult'
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
                <div v-if="openMenu === item.label" class="mega">
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
        <span class="lang" aria-hidden="true">中文</span>
        <button type="button" class="actions__cta" @click="consult('OTHER_CTA')">预约演示</button>
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
