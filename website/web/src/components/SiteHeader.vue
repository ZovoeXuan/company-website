<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import type { NavItem } from '@/data/types'

defineProps<{ siteName: string; nav: NavItem[] }>()

const route = useRoute()
const open = ref(false)

function close() {
  open.value = false
}

function onKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape' && open.value) {
    close()
  }
}

onMounted(() => document.addEventListener('keydown', onKeydown))
onBeforeUnmount(() => document.removeEventListener('keydown', onKeydown))
</script>

<template>
  <header class="header">
    <div class="container header__inner">
      <RouterLink to="/" class="brand" :aria-label="`${siteName} 首页`">
        <span class="brand__mark" aria-hidden="true">示</span>
        <span class="brand__name">{{ siteName }}</span>
      </RouterLink>

      <nav class="nav" aria-label="主导航">
        <ul class="nav__list">
          <li v-for="item in nav" :key="item.path">
            <RouterLink :to="item.path" class="nav__link">{{ item.label }}</RouterLink>
          </li>
        </ul>
      </nav>

      <div class="actions">
        <RouterLink to="/contact" class="btn btn--primary actions__cta">咨询我们</RouterLink>
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
        <li v-for="item in nav" :key="item.path">
          <RouterLink
            :to="item.path"
            class="mobile__link"
            :class="{ 'is-active': route.path === item.path }"
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

.nav__link {
  display: inline-block;
  padding: 8px 0;
  font-size: 15px;
  color: var(--c-text-muted);
  border-bottom: 2px solid transparent;
}

.nav__link:hover {
  color: var(--c-text);
}

.nav__link.router-link-active {
  color: var(--c-text);
  border-bottom-color: var(--c-brand);
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
  display: block;
  padding: 12px 4px;
  font-size: 15px;
  color: var(--c-text);
  border-bottom: 1px solid var(--c-border);
}

.mobile__link.is-active {
  color: var(--c-brand);
  font-weight: 500;
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
