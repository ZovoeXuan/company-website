<!-- 对应源码路径: website/web/src/components/ConsultDialog.vue -->
<script setup lang="ts">
import { nextTick, onBeforeUnmount, ref, watch } from 'vue'
import ConsultForm from '@/components/ConsultForm.vue'
import { useConsultDialog } from '@/composables/consult'

const { dialog, closeConsult } = useConsultDialog()
const modalRef = ref<HTMLElement | null>(null)
let triggerEl: HTMLElement | null = null

function onKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape' && dialog.open) {
    closeConsult()
  }
}

watch(
  () => dialog.open,
  async (open) => {
    if (open) {
      triggerEl = document.activeElement instanceof HTMLElement ? document.activeElement : null
      document.addEventListener('keydown', onKeydown)
      document.body.style.overflow = 'hidden'
      await nextTick()
      modalRef.value?.focus()
    } else {
      document.removeEventListener('keydown', onKeydown)
      document.body.style.overflow = ''
      await nextTick()
      triggerEl?.focus?.()
      triggerEl = null
    }
  },
)

onBeforeUnmount(() => {
  document.removeEventListener('keydown', onKeydown)
  document.body.style.overflow = ''
})
</script>

<template>
  <Teleport to="body">
    <Transition name="trial-modal">
      <div
        v-if="dialog.open"
        class="trial-backdrop"
        role="dialog"
        aria-modal="true"
        aria-labelledby="trial-modal-title"
        @click.self="closeConsult"
      >
        <!-- 1:1 还原截图 预约产品展示.png + 满足开发文档 11.3 键盘焦点回退 -->
        <div ref="modalRef" class="trial-modal" tabindex="-1">
          <button
            type="button"
            class="trial-modal__close"
            aria-label="关闭窗口"
            @click="closeConsult"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="15" y1="9" x2="9" y2="15"></line>
              <line x1="9" y1="9" x2="15" y2="15"></line>
            </svg>
          </button>

          <h2 id="trial-modal-title" class="trial-modal__title">
            {{ dialog.title || '预约产品展示' }}
          </h2>

          <ConsultForm
            :source-position="dialog.sourcePosition"
            :content-type="dialog.contentType"
            :content-id="dialog.contentId != null ? String(dialog.contentId) : undefined"
            :initial-demand="dialog.initialDemand"
          />
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.trial-backdrop {
  position: fixed;
  inset: 0;
  z-index: 90;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.trial-modal {
  position: relative;
  width: 100%;
  max-width: 472px;
  background: #ffffff;
  border-radius: 6px;
  padding: 36px 40px 38px;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.24);
  outline: none;
}

.trial-modal__close {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 30px;
  height: 30px;
  border: none;
  background: none;
  color: #5f6470;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s ease, transform 0.2s ease;
}

.trial-modal__close:hover {
  color: #101014;
  transform: scale(1.06);
}

.trial-modal__title {
  text-align: center;
  font-size: 24px;
  font-weight: 700;
  color: #1f2329;
  margin: 0 0 24px;
  letter-spacing: 0.01em;
}

.trial-modal-enter-active,
.trial-modal-leave-active {
  transition: opacity 0.22s ease;
}

.trial-modal-enter-active .trial-modal,
.trial-modal-leave-active .trial-modal {
  transition: transform 0.24s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.22s ease;
}

.trial-modal-enter-from,
.trial-modal-leave-to {
  opacity: 0;
}

.trial-modal-enter-from .trial-modal,
.trial-modal-leave-to .trial-modal {
  opacity: 0;
  transform: translateY(12px) scale(0.97);
}
</style>
