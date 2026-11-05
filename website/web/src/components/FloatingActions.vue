<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { openConsult, openRoiCalculator } from '@/composables/consult'
import type { SiteConfig } from '@/data/types'

const props = defineProps<{
  site?: SiteConfig | null
}>()

const showPhoneCard = ref(false)
const scrollPercent = ref(0)
const showTopBtn = ref(false)

const phone = computed(() => props.site?.contact?.phone || '400-888-0000')
const email = computed(() => props.site?.contact?.email || 'connect@g7.com.cn')
const hours = computed(() => props.site?.contact?.hours || '周一至周日 08:30 - 20:30')

const ringDashoffset = computed(() => {
  const circumference = 2 * Math.PI * 18
  return circumference - (scrollPercent.value / 100) * circumference
})

function onScroll() {
  const doc = document.documentElement
  const top = window.scrollY || doc.scrollTop
  const height = doc.scrollHeight - doc.clientHeight
  showTopBtn.value = top > 260
  scrollPercent.value = height > 0 ? Math.min(100, Math.max(0, Math.round((top / height) * 100))) : 0
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
  <aside class="float-dock" aria-label="快捷工具栏">
    <!-- 1. 小七智能顾问磁吸入口 -->
    <button
      type="button"
      class="dock-btn dock-btn--ai"
      title="唤醒数字物流专家小七"
      @click="openConsult({ sourcePosition: 'FLOATING' })"
    >
      <span class="dock-btn__pulse" aria-hidden="true"></span>
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <rect x="3" y="11" width="18" height="10" rx="2"></rect>
        <circle cx="12" cy="5" r="2"></circle>
        <path d="M12 7v4"></path>
        <line x1="8" y1="16" x2="8.01" y2="16"></line>
        <line x1="16" y1="16" x2="16.01" y2="16"></line>
      </svg>
      <span class="dock-btn__label">小七顾问</span>
    </button>

    <!-- 2. ROI 降本增效测算器 -->
    <button
      type="button"
      class="dock-btn"
      title="测算您的企业数字化 ROI 收益"
      @click="openRoiCalculator()"
    >
      <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <rect x="4" y="2" width="16" height="20" rx="2"></rect>
        <line x1="8" y1="6" x2="16" y2="6"></line>
        <line x1="8" y1="10" x2="10" y2="10"></line>
        <line x1="14" y1="10" x2="16" y2="10"></line>
        <line x1="8" y1="14" x2="10" y2="14"></line>
        <line x1="14" y1="14" x2="16" y2="14"></line>
        <line x1="8" y1="18" x2="16" y2="18"></line>
      </svg>
      <span class="dock-btn__label">ROI测算</span>
    </button>

    <!-- 3. 热线电话与邮箱气泡 -->
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
        <span class="dock-btn__label">热线咨询</span>
      </button>

      <Transition name="pop">
        <div v-if="showPhoneCard" class="dock-card">
          <p class="dock-card__title">全国专家服务热线</p>
          <a :href="`tel:${phone}`" class="dock-card__phone">{{ phone }}</a>
          <p class="dock-card__meta">服务时间：{{ hours }}</p>
          <p class="dock-card__meta">商务合作：{{ email }}</p>
          <button
            type="button"
            class="dock-card__cta"
            @click="showPhoneCard = false; openConsult({ sourcePosition: 'FLOATING' })"
          >
            预约专家回电 →
          </button>
        </div>
      </Transition>
    </div>

    <!-- 4. 带阅读进度环的回到顶部按钮 -->
    <Transition name="pop">
      <button
        v-if="showTopBtn"
        type="button"
        class="dock-btn dock-btn--top"
        :title="`回到顶部 (已浏览 ${scrollPercent}%)`"
        @click="scrollToTop"
      >
        <svg class="progress-ring" width="42" height="42" viewBox="0 0 42 42">
          <circle class="progress-ring__bg" cx="21" cy="21" r="18" fill="none" stroke-width="2.5" />
          <circle
            class="progress-ring__bar"
            cx="21"
            cy="21"
            r="18"
            fill="none"
            stroke-width="2.5"
            stroke-dasharray="113.1"
            :stroke-dashoffset="ringDashoffset"
          />
        </svg>
        <span class="dock-btn__top-arrow">↑</span>
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
  position: relative;
  width: 56px;
  height: 56px;
  border-radius: 16px;
  border: 1px solid rgba(16, 16, 20, 0.1);
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(12px);
  color: #101014;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  cursor: pointer;
  box-shadow: 0 10px 28px rgba(10, 12, 20, 0.1);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease, color 0.2s ease;
}

.dock-btn:hover {
  transform: translateY(-2px);
  border-color: #5b4be8;
  color: #5b4be8;
  box-shadow: 0 14px 32px rgba(91, 75, 232, 0.2);
}

.dock-btn--ai {
  background: linear-gradient(145deg, #6366f1 0%, #5b4be8 60%, #4338ca 100%);
  color: #ffffff;
  border-color: transparent;
  box-shadow: 0 12px 28px rgba(91, 75, 232, 0.38);
}

.dock-btn--ai:hover {
  color: #ffffff;
  box-shadow: 0 16px 36px rgba(91, 75, 232, 0.52);
}

.dock-btn__pulse {
  position: absolute;
  top: 7px;
  right: 7px;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #34d399;
  box-shadow: 0 0 0 3px rgba(52, 211, 153, 0.3);
}

.dock-btn__label {
  font-size: 10.5px;
  font-weight: 600;
  line-height: 1;
}

.dock-popover-wrap {
  position: relative;
}

.dock-card {
  position: absolute;
  right: calc(100% + 12px);
  bottom: 0;
  width: 248px;
  background: #ffffff;
  border: 1px solid rgba(16, 16, 20, 0.1);
  border-radius: 16px;
  padding: 18px;
  box-shadow: 0 20px 48px rgba(10, 12, 20, 0.16);
}

.dock-card__title {
  font-size: 12px;
  color: #8b90a0;
  margin: 0 0 4px;
}

.dock-card__phone {
  display: block;
  font-size: 20px;
  font-weight: 800;
  color: #5b4be8;
  margin-bottom: 8px;
}

.dock-card__meta {
  font-size: 12px;
  color: #5f6470;
  margin: 3px 0;
}

.dock-card__cta {
  margin-top: 12px;
  width: 100%;
  padding: 8px 12px;
  border-radius: 8px;
  border: none;
  background: #eeebff;
  color: #5b4be8;
  font-size: 12.5px;
  font-weight: 600;
  cursor: pointer;
}

.dock-btn--top {
  padding: 0;
}

.progress-ring {
  transform: rotate(-90deg);
}

.progress-ring__bg {
  stroke: rgba(16, 16, 20, 0.08);
}

.progress-ring__bar {
  stroke: #5b4be8;
  stroke-linecap: round;
  transition: stroke-dashoffset 0.15s ease;
}

.dock-btn__top-arrow {
  position: absolute;
  font-size: 15px;
  font-weight: 700;
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

@media (max-width: 640px) {
  .float-dock {
    right: 12px;
    bottom: 18px;
  }
  .dock-btn {
    width: 48px;
    height: 48px;
  }
  .dock-btn__label {
    font-size: 9.5px;
  }
}
</style>
