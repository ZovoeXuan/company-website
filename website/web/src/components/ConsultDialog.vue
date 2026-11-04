<script setup lang="ts">
import { nextTick, onBeforeUnmount, ref, watch } from 'vue'
import ConsultForm from '@/components/ConsultForm.vue'
import { useConsultDialog } from '@/composables/consult'

const { dialog, closeConsult } = useConsultDialog()
const panel = ref<HTMLElement | null>(null)

function onKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape' && dialog.open) {
    closeConsult()
  }
}

watch(
  () => dialog.open,
  async (open) => {
    if (open) {
      document.addEventListener('keydown', onKeydown)
      document.body.style.overflow = 'hidden'
      await nextTick()
      panel.value?.focus()
    } else {
      document.removeEventListener('keydown', onKeydown)
      document.body.style.overflow = ''
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
    <div v-if="dialog.open" class="overlay" @click.self="closeConsult">
      <Transition name="drawer" appear>
        <section
          ref="panel"
          class="panel"
          role="dialog"
          aria-modal="true"
          aria-labelledby="consult-dialog-title"
          tabindex="-1"
        >
          <header class="panel__head">
            <div class="panel__assistant-badge">
              <div class="assistant-avatar" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="assistant-avatar__icon">
                  <rect x="3" y="11" width="18" height="10" rx="2"></rect>
                  <circle cx="12" cy="5" r="2"></circle>
                  <path d="M12 7v4"></path>
                  <line x1="8" y1="16" x2="8.01" y2="16"></line>
                  <line x1="16" y1="16" x2="16.01" y2="16"></line>
                </svg>
              </div>
              <div>
                <h2 id="consult-dialog-title" class="panel__title">你好呀，我是小七</h2>
                <p class="panel__sub">数字物流专家 · 让我们开始交流吧</p>
              </div>
            </div>
            <button type="button" class="panel__close" aria-label="关闭咨询窗口" @click="closeConsult">
              <span>关闭窗口</span>
              <span class="panel__close-x" aria-hidden="true">×</span>
            </button>
          </header>

          <div class="panel__body">
            <ConsultForm
              :source-position="dialog.target.sourcePosition"
              :content-type="dialog.target.contentType"
              :content-id="dialog.target.contentId"
            />
          </div>
        </section>
      </Transition>
    </div>
  </Teleport>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  z-index: 60;
  background: rgba(10, 12, 20, 0.45);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: flex-end;
  padding: 0;
}

.panel {
  width: min(480px, 100vw);
  height: 100dvh;
  max-height: 100dvh;
  overflow-y: auto;
  background: #ffffff;
  padding: 28px 32px 36px;
  box-shadow: -16px 0 48px rgba(10, 12, 20, 0.16);
  outline: none;
  display: flex;
  flex-direction: column;
}

.panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--c-border);
}

.panel__assistant-badge {
  display: flex;
  align-items: center;
  gap: 12px;
}

.assistant-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: linear-gradient(135deg, #7c3aed, #4f46e5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.35);
  flex: none;
}

.assistant-avatar__icon {
  width: 20px;
  height: 20px;
}

.panel__title {
  font-size: 17px;
  font-weight: 600;
  color: var(--c-text);
  margin: 0;
}

.panel__sub {
  margin: 3px 0 0;
  font-size: 12px;
  color: var(--c-text-muted);
}

.panel__close {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: none;
  background: none;
  color: var(--c-text-subtle);
  font-size: 13px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 6px;
  transition: color 0.2s ease, background-color 0.2s ease;
}

.panel__close:hover {
  color: var(--c-text);
  background: var(--c-surface);
}

.panel__close-x {
  font-size: 18px;
  line-height: 1;
}

.panel__body {
  flex: 1;
}

.drawer-enter-active,
.drawer-leave-active {
  transition: transform 0.35s cubic-bezier(0.22, 1, 0.36, 1);
}

.drawer-enter-from,
.drawer-leave-to {
  transform: translateX(100%);
}

@media (max-width: 520px) {
  .panel {
    padding: 20px 20px 28px;
  }
}
</style>
