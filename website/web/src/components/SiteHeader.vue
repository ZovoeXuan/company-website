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

function close() {
  open.value = false
  openMobile.value = null
}

function onKeydown(event: KeyboardEvent) {
  if (event.key !== 'Escape') return
  if (openMenu.value) {
    openMenu.value = null
    return
  }
  if (open.value) close()
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

onMounted(() => document.addEventListener('keydown', onKeydown))
onBeforeUnmount(() => document.removeEventListener('keydown', onKeydown))
</script>

<template>
  <header class="header">
    <div class="container header__inner">
      <RouterLink to="/" class="brand" :aria-label="`${props.siteName} 首页`">
        <img v-if="logoUrl" :src="logoUrl" :alt="logoAlt || siteName" class="brand__logo" />
        <template v-else>
          <span class="brand__mark" aria-hidden="true">{{ siteName.slice(0, 1) }}</span>
          <span class="brand__name">{{ siteName }}</span>
        </template>
      </RouterLink>

      <nav class="nav" aria-label="主导航">
        <ul class="nav__list">
          <li v-for="item in nav" :key="item.label" class="nav__item">
            <template v-if="item.children?.length">
              <button
                type="button"
                class="nav__link nav__button"
                :class="{ 'router-link-active': isActive(item) }"
                :aria-expanded="openMenu === item.label"
                @click="toggleMenu(item)"
              >
                {{ item.label }}
                <span class="caret" aria-hidden="true">▾</span>
              </button>
              <ul v-if="openMenu === item.label" class="sub">
                <li v-for="child in item.children" :key="child.label">
                  <button
                    v-if="child.linkType === 'CONSULT'"
                    type="button"
                    class="sub__link sub__button"
                    @click="consult()"
                  >
                    {{ child.label }}
                  </button>
                  <a
                    v-else-if="child.url"
                    :href="child.url"
                    class="sub__link"
                    :target="child.newWindow ? '_blank' : undefined"
                    :rel="child.newWindow ? 'noopener noreferrer' : undefined"
                    @click="openMenu = null"
                  >
                    {{ child.label }}
                  </a>
                  <RouterLink v-else :to="child.path" class="sub__link" @click="openMenu = null">
                    {{ child.label }}
                  </RouterLink>
                </li>
              </ul>
            </template>
            <button
              v-else-if="item.linkType === 'CONSULT'"
              type="button"
              class="nav__link nav__button"
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
            <RouterLink v-else :to="item.path" class="nav__link">{{ item.label }}</RouterLink>
          </li>
        </ul>
      </nav>

      <div class="actions">
        <button type="button" class="btn btn--primary actions__cta" @click="consult('OTHER_CTA')">
          咨询我们
        </button>
        <button
          type="button"
          class="toggle"
          :aria-expanded="open"
          aria-controls="mobile-nav"
          @click="open = !open"
        >
          <span class="visually-hidden">{{ open ? '关闭菜单' : '打开菜单' }}</span>
          <svg width="20" height="20" viewBox="0 0 20 20" aria-hidden="true">
            <path v-if="!open" d="M3 6h14M3 10h14M3 14h14" stroke="currentColor" stroke-width="1.6" />
            <path v-else d="M5 5l10 10M15 5L5 15" stroke="currentColor" stroke-width="1.6" />
          </svg>
        </button>
      </div>
    </div>

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
              <span class="caret" aria-hidden="true">{{ openMobile === item.label ? '▴' : '▾' }}</span>
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
    </nav>
  </header>
</template>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 30;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: saturate(180%) blur(8px);
  border-bottom: 1px solid var(--c-border);
}

.header__inner {
  display: flex;
  align-items: center;
  gap: 24px;
  height: var(--header-height);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: none;
}

.brand__logo {
  max-height: 34px;
  max-width: 160px;
  object-fit: contain;
}

.brand__mark {
  width: 30px;
  height: 30px;
  border-radius: 7px;
  background: var(--c-brand);
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.brand__name {
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 0.01em;
}

.nav {
  flex: 1;
  min-width: 0;
}

.nav__list {
  display: flex;
  gap: 26px;
  list-style: none;
  margin: 0;
  padding: 0;
}

.nav__item {
  position: relative;
}

.nav__link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 8px 0;
  font-size: 15px;
  color: var(--c-text-muted);
  border-bottom: 2px solid transparent;
  background: none;
  border-top: none;
  border-left: none;
  border-right: none;
  cursor: pointer;
  font-family: inherit;
}

.nav__link:hover {
  color: var(--c-text);
}

.nav__link.router-link-active,
.nav__button.router-link-active {
  color: var(--c-text);
  border-bottom-color: var(--c-brand);
}

.caret {
  font-size: 10px;
  line-height: 1;
}

.sub {
  position: absolute;
  top: 100%;
  left: -12px;
  min-width: 160px;
  list-style: none;
  margin: 4px 0 0;
  padding: 6px;
  background: #fff;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  box-shadow: 0 12px 28px rgba(16, 30, 48, 0.08);
  z-index: 20;
}

.sub__link {
  display: block;
  padding: 8px 12px;
  font-size: 14px;
  color: var(--c-text-muted);
  border-radius: 4px;
}

.sub__link:hover {
  background: var(--c-surface);
  color: var(--c-text);
}

.sub__button {
  width: 100%;
  border: none;
  background: none;
  text-align: left;
  font-family: inherit;
  cursor: pointer;
}

.actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: none;
}

.actions__cta {
  min-height: 38px;
  padding: 0 16px;
  font-size: 14px;
}

.toggle {
  display: none;
  width: 40px;
  height: 40px;
  align-items: center;
  justify-content: center;
  background: #fff;
  border: 1px solid var(--c-border-strong);
  border-radius: var(--radius-sm);
  color: var(--c-text);
  cursor: pointer;
}

.mobile {
  border-top: 1px solid var(--c-border);
  background: #fff;
}

.mobile__list {
  list-style: none;
  margin: 0;
  padding: 8px 16px 16px;
}

.mobile__link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 12px 4px;
  font-size: 15px;
  color: var(--c-text);
  border-bottom: 1px solid var(--c-border);
  background: none;
  border-top: none;
  border-left: none;
  border-right: none;
  font-family: inherit;
  text-align: left;
  cursor: pointer;
}

.mobile__link.is-active {
  color: var(--c-brand);
  font-weight: 500;
}

.mobile__sub {
  list-style: none;
  margin: 0;
  padding: 4px 0 8px 12px;
}

.mobile__sublink {
  display: block;
  padding: 10px 4px;
  font-size: 14px;
  color: var(--c-text-muted);
  border-bottom: 1px dashed var(--c-border);
}

.mobile__sublink--button {
  width: 100%;
  border: none;
  border-bottom: 1px dashed var(--c-border);
  background: none;
  text-align: left;
  font-family: inherit;
  cursor: pointer;
}

@media (max-width: 980px) {
  .nav {
    display: none;
  }
  .toggle {
    display: inline-flex;
  }
  .actions {
    margin-left: auto;
  }
}

@media (max-width: 520px) {
  .actions__cta {
    display: none;
  }
}
</style>
