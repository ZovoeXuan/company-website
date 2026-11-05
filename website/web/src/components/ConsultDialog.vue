<script setup lang="ts">
import { nextTick, onBeforeUnmount, ref, watch } from 'vue'
import ConsultForm from '@/components/ConsultForm.vue'
import { useConsultDialog } from '@/composables/consult'

const { dialog, closeConsult } = useConsultDialog()
const panel = ref<HTMLElement | null>(null)
const selectedFaq = ref<number | null>(null)
const presetDemand = ref('')

interface FaqItem {
  q: string
  a: string
  demandPrefill: string
}

const faqs: FaqItem[] = [
  {
    q: '什么是「紫宝盒」全场景 AI 工具？',
    a: '紫宝盒是面向公路货运与车队管理打造的全场景 AI 智能终端与算法平台，覆盖 50+ 高危驾驶与装卸作业场景，兼具高准确率风险识别与车载智能语音交互。',
    demandPrefill: '我希望了解「紫宝盒」全场景 AI 工具的硬件参数、算法覆盖场景及试用方案。',
  },
  {
    q: '在利润与运力承压下，如何管好车辆、司机与能耗成本？',
    a: '通过 IoT 安全管家 + 数字能源 + 智能管车一体化平台，打通「定位、油耗/电耗、安全风控、自动结算」数据闭环，帮助车队平均降低事故率 51%、节省管理成本。',
    demandPrefill: '我们车队希望了解如何通过数字化平台降低车辆能耗、事故率与日常运营成本。',
  },
  {
    q: '新能源车队管理有哪些核心痛点与解决方案？',
    a: '针对新能源重卡续航焦虑、充电调度难与电池寿命监控问题，我们提供智能补能网络规划、能耗精细化核算与电池健康实时监测方案。',
    demandPrefill: '我们正在运营或筹建新能源车队，希望咨询新能源车队精细化运营与补能管理方案。',
  },
  {
    q: '冷链与大宗物流如何实现全流程透明化追货与防偷盗？',
    a: '依托高精温湿度传感器、电子锁与载重/视频融合 AI 算法，实现全程不脱温预警、异常停留报警与水泥/饲料等大宗散货防偷盗治理。',
    demandPrefill: '希望了解冷链全程温控 / 大宗散货防偷盗 AI 治理方案及成功案例。',
  },
]

function selectFaq(index: number) {
  selectedFaq.value = selectedFaq.value === index ? null : index
  if (selectedFaq.value !== null) {
    presetDemand.value = faqs[index].demandPrefill
  }
}

function onKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape' && dialog.open) {
    closeConsult()
  }
}

watch(
  () => dialog.open,
  async (open) => {
    if (open) {
      presetDemand.value = dialog.initialDemand || ''
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
    <Transition name="drawer">
      <div
        v-if="dialog.open"
        class="overlay"
        role="dialog"
        aria-modal="true"
        aria-labelledby="consult-dialog-title"
        @click.self="closeConsult"
      >
        <div ref="panel" class="panel" tabindex="-1">
          <!-- 顶部数字专家助手卡片（对齐参考视频 04:27 小七助手抽屉） -->
          <div class="panel__head">
            <div class="panel__assistant">
              <div class="assistant-avatar" aria-hidden="true">
                <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="11" width="18" height="10" rx="2"></rect>
                  <circle cx="12" cy="5" r="2"></circle>
                  <path d="M12 7v4"></path>
                  <line x1="8" y1="16" x2="8.01" y2="16"></line>
                  <line x1="16" y1="16" x2="16.01" y2="16"></line>
                </svg>
                <span class="assistant-status"></span>
              </div>
              <div>
                <h2 id="consult-dialog-title" class="panel__title">您好，我是数字物流专家 小七</h2>
                <p class="panel__sub">7×24h 实时解答产品方案，为您预约专属行业顾问演示</p>
              </div>
            </div>
            <button type="button" class="panel__close" aria-label="关闭咨询窗口" @click="closeConsult">
              ✕
            </button>
          </div>

          <!-- 猜你想问快捷问答区（对齐参考视频右侧智能问答交互） -->
          <div class="faq-section">
            <div class="faq-section__header">
              <span class="faq-section__badge">💡 猜你想问</span>
              <span class="faq-section__hint">点击问题查看解答并自动带入咨询需求</span>
            </div>
            <ul class="faq-list">
              <li v-for="(item, idx) in faqs" :key="item.q" class="faq-item">
                <button
                  type="button"
                  class="faq-item__q"
                  :class="{ 'is-active': selectedFaq === idx }"
                  @click="selectFaq(idx)"
                >
                  <span>{{ item.q }}</span>
                  <span class="faq-item__arrow" aria-hidden="true">{{ selectedFaq === idx ? '−' : '+' }}</span>
                </button>
                <div v-if="selectedFaq === idx" class="faq-item__a">
                  <p>{{ item.a }}</p>
                </div>
              </li>
            </ul>
          </div>

          <div class="form-divider">
            <span>填写预约信息 · 专属顾问 1 对 1 演示</span>
          </div>

          <ConsultForm
            :source-position="dialog.sourcePosition"
            :content-type="dialog.contentType"
            :content-id="dialog.contentId != null ? String(dialog.contentId) : undefined"
            :initial-demand="presetDemand"
          />
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  z-index: 80;
  background: rgba(8, 8, 12, 0.55);
  backdrop-filter: blur(6px);
  display: flex;
  justify-content: flex-end;
}

.panel {
  width: min(500px, 100vw);
  height: 100dvh;
  overflow-y: auto;
  background: #ffffff;
  padding: 24px 28px 36px;
  box-shadow: -20px 0 60px rgba(8, 8, 12, 0.22);
  display: flex;
  flex-direction: column;
  outline: none;
}

.panel__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 16px;
  border-radius: 16px;
  background: linear-gradient(135deg, #f5f3ff 0%, #eef2ff 100%);
  border: 1px solid rgba(91, 75, 232, 0.14);
  margin-bottom: 18px;
}

.panel__assistant {
  display: flex;
  align-items: center;
  gap: 12px;
}

.assistant-avatar {
  position: relative;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6d5ce8 0%, #4636d6 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 18px rgba(91, 75, 232, 0.35);
  flex: none;
}

.assistant-status {
  position: absolute;
  right: 1px;
  bottom: 1px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #10b981;
  border: 2px solid #ffffff;
}

.panel__title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #101014;
}

.panel__sub {
  margin: 4px 0 0;
  font-size: 12px;
  color: #5f6470;
}

.panel__close {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  background: rgba(255, 255, 255, 0.8);
  color: #5f6470;
  font-size: 14px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.panel__close:hover {
  background: #ffffff;
  color: #101014;
}

.faq-section {
  background: #f8f9fc;
  border: 1px solid rgba(16, 16, 20, 0.07);
  border-radius: 14px;
  padding: 14px 16px;
  margin-bottom: 20px;
}

.faq-section__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.faq-section__badge {
  font-size: 12.5px;
  font-weight: 600;
  color: #4636d6;
}

.faq-section__hint {
  font-size: 11.5px;
  color: #8b90a0;
}

.faq-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.faq-item__q {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 9px 12px;
  border-radius: 8px;
  border: 1px solid rgba(16, 16, 20, 0.06);
  background: #ffffff;
  color: #101014;
  font-size: 13px;
  font-weight: 500;
  text-align: left;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s ease;
}

.faq-item__q:hover,
.faq-item__q.is-active {
  border-color: rgba(91, 75, 232, 0.4);
  color: #5b4be8;
  background: #fcfbff;
}

.faq-item__arrow {
  font-weight: 700;
  color: #5b4be8;
}

.faq-item__a {
  padding: 10px 12px;
  font-size: 12.5px;
  line-height: 1.65;
  color: #5f6470;
  background: rgba(91, 75, 232, 0.04);
  border-radius: 0 0 8px 8px;
  margin-top: -2px;
}

.faq-item__a p {
  margin: 0;
}

.form-divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  font-size: 12px;
  font-weight: 600;
  color: #5f6470;
}

.form-divider::before,
.form-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: rgba(16, 16, 20, 0.08);
}

.drawer-enter-active,
.drawer-leave-active {
  transition: opacity 0.25s ease;
}

.drawer-enter-active .panel,
.drawer-leave-active .panel {
  transition: transform 0.3s cubic-bezier(0.22, 1, 0.36, 1);
}

.drawer-enter-from,
.drawer-leave-to {
  opacity: 0;
}

.drawer-enter-from .panel,
.drawer-leave-to .panel {
  transform: translateX(100%);
}
</style>
