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
      <section
        ref="panel"
        class="panel"
        role="dialog"
        aria-modal="true"
        aria-labelledby="consult-dialog-title"
        tabindex="-1"
      >
        <header class="panel__head">
          <div>
            <h2 id="consult-dialog-title" class="panel__title">在线咨询</h2>
            <p class="panel__sub">留下联系方式与场景，我们会先判断是否适合，再做沟通。</p>
          </div>
          <button type="button" class="panel__close" aria-label="关闭咨询窗口" @click="closeConsult">
            ×
          </button>
        </header>

        <ConsultForm
          :source-position="dialog.target.sourcePosition"
          :content-type="dialog.target.contentType"
          :content-id="dialog.target.contentId"
        />
      </section>
    </div>
  </Teleport>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  z-index: 60;
  background: rgba(15, 25, 40, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.panel {
  width: min(560px, 100%);
  max-height: calc(100dvh - 48px);
  overflow-y: auto;
  background: #fff;
  border-radius: 14px;
  padding: 28px;
  box-shadow: 0 24px 64px rgba(10, 20, 40, 0.22);
  outline: none;
}

.panel__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
}

.panel__title {
  font-size: 20px;
  font-weight: 500;
}

.panel__sub {
  margin-top: 6px;
  font-size: 13px;
  color: var(--c-text-muted);
  line-height: 1.7;
}

.panel__close {
  flex: none;
  width: 32px;
  height: 32px;
  border: 1px solid var(--c-border);
  border-radius: 50%;
  background: #fff;
  color: var(--c-text-muted);
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
}

.panel__close:hover {
  color: var(--c-text);
  border-color: var(--c-brand);
}

@media (max-width: 520px) {
  .overlay {
    padding: 0;
    align-items: flex-end;
  }
  .panel {
    max-height: 92dvh;
    border-radius: 14px 14px 0 0;
    padding: 22px;
  }
}
</style>
