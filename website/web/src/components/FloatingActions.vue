<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { openConsult } from '@/composables/consult'
import type { SiteConfig } from '@/data/types'

defineProps<{
  site?: SiteConfig | null
}>()

const showBackTop = ref(false)

function onScroll() {
  showBackTop.value = window.scrollY > 360
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function handleConsult() {
  openConsult({ sourcePosition: 'FLOATING' })
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
  <aside class="floating" aria-label="快捷服务与在线咨询">
    <!-- 智能咨询入口 -->
    <button
      type="button"
      class="floating__btn floating__btn--consult"
      aria-label="打开在线咨询"
      @click="handleConsult"
    >
      <div class="floating__icon-wrap">
        <svg class="floating__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
        </svg>
        <span class="floating__pulse" aria-hidden="true"></span>
      </div>
      <span class="floating__text">在线咨询</span>
    </button>

    <!-- 电话快捷呼叫（若有配置） -->
    <a
      v-if="site?.contact.phone"
      :href="`tel:${site.contact.phone.replace(/[\s-]/g, '')}`"
      class="floating__btn floating__btn--phone"
      :aria-label="`致电咨询：${site.contact.phone}`"
      :title="`致电咨询：${site.contact.phone}`"
    >
      <svg class="floating__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
        <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
      </svg>
    </a>

    <!-- 回到顶部 -->
    <Transition name="fade">
      <button
        v-if="showBackTop"
        type="button"
        class="floating__btn floating__btn--top"
        aria-label="回到顶部"
        @click="scrollToTop"
      >
        <svg class="floating__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <polyline points="18 15 12 9 6 15"></polyline>
        </svg>
      </button>
    </Transition>
  </aside>
</template>

<style scoped>
.floating {
  position: fixed;
  right: 24px;
  bottom: 28px;
  z-index: 35;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

.floating__btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  font-family: inherit;
  transition: transform 0.25s var(--ease-out), box-shadow 0.25s ease, background-color 0.25s ease;
}

.floating__btn:hover {
  transform: translateY(-3px);
}

.floating__btn--consult {
  padding: 10px 18px 10px 14px;
  background: var(--c-brand, #5b4be8);
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.02em;
  box-shadow: 0 12px 28px rgba(91, 75, 232, 0.38), 0 4px 10px rgba(10, 12, 20, 0.12);
}

.floating__btn--consult:hover {
  background: var(--c-brand-dark, #4636d6);
  box-shadow: 0 16px 36px rgba(91, 75, 232, 0.46), 0 6px 14px rgba(10, 12, 20, 0.16);
}

.floating__icon-wrap {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.floating__icon {
  width: 18px;
  height: 18px;
}

.floating__pulse {
  position: absolute;
  inset: -4px;
  border-radius: 50%;
  border: 1.5px solid rgba(255, 255, 255, 0.6);
  animation: pulseAura 2s cubic-bezier(0.24, 0, 0.38, 1) infinite;
  pointer-events: none;
}

@keyframes pulseAura {
  0% {
    transform: scale(0.8);
    opacity: 0.8;
  }
  70% {
    transform: scale(1.6);
    opacity: 0;
  }
  100% {
    transform: scale(1.6);
    opacity: 0;
  }
}

.floating__text {
  line-height: 1;
}

.floating__btn--phone,
.floating__btn--top {
  width: 42px;
  height: 42px;
  background: #ffffff;
  color: var(--c-text, #101014);
  border: 1px solid var(--c-border, rgba(16, 16, 20, 0.08));
  box-shadow: 0 8px 24px rgba(10, 12, 20, 0.08);
}

.floating__btn--phone:hover,
.floating__btn--top:hover {
  background: var(--c-surface, #f5f7fa);
  color: var(--c-brand, #5b4be8);
  border-color: var(--c-brand-soft, #eeebff);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

@media (max-width: 768px) {
  .floating {
    right: 16px;
    bottom: 20px;
    gap: 8px;
  }
  .floating__text {
    display: none;
  }
  .floating__btn--consult {
    width: 44px;
    height: 44px;
    padding: 0;
  }
}
</style>
