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
