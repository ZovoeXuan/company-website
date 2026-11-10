<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { closeXiaoQi, openConsult, useXiaoQiDrawer } from '@/composables/consult'
import { askXiaoqi } from '@/api/public'
import type { SiteConfig, XiaoQiFaqPair } from '@/data/types'

const props = defineProps<{
  site?: SiteConfig | null
}>()

const { xiaoqi } = useXiaoQiDrawer()
const inputVal = ref('')
const deepThinkOn = ref(true)
const batchIndex = ref(0)
const spinning = ref(false)
const showHistory = ref(false)
const scrollBox = ref<HTMLElement | null>(null)

interface ChatMsg {
  id: number
  role: 'user' | 'assistant'
  text: string
  thought?: string
  thinking?: boolean
}

interface SessionItem {
  id: string
  title: string
  time: string
  messages: ChatMsg[]
}

const messages = ref<ChatMsg[]>([])
const sessions = ref<SessionItem[]>([])
let msgCounter = 1
let streamTimer: number | null = null

const cfg = computed(() => props.site?.xiaoqiConfig ?? null)
const botName = computed(() => cfg.value?.name || '小鑫')
const welcomeSub = computed(
  () => cfg.value?.welcomeSubtitle || `${props.site?.siteName || '企业'} 数字化专家`,
)
const certBadge = computed(() => cfg.value?.certBadge || '专业认证')
const deepModel = computed(() => cfg.value?.deepThinkModel || 'DeepSeek R1')
const disclaimer = computed(
  () => cfg.value?.disclaimer || '提醒：AI也可能会犯错误，请考虑核实重要信息',
)

const DEFAULT_CAPABILITIES = [
  '数字化解决方案',
  '新能源场景分析',
  '运价查询',
  '线路智能推荐',
  '物流实时数据',
]

const capabilities = computed(() => {
  if (cfg.value?.capabilities && cfg.value.capabilities.length > 0) {
    return cfg.value.capabilities
  }
  return DEFAULT_CAPABILITIES
})

const DEFAULT_QUESTIONS = [
  '新能源物流车运营痛点解决方案？',
  '如何构建智慧物流园区？',
  '“三晒两抓-城配版”系统中司机的安全分是如何分类的？',
  '物流企业如何控制货损率？',
  '中国“双碳”目标及全球ESG趋势对物流企业提出哪些新要求？',
  '什么是「紫宝盒」全场景AI工具？它能解决哪些车队痛点？',
  '大宗煤炭与钢铁运输如何实现全流程防偷盗AI治理？',
  '冷链运输如何做到全程不脱温预警与自动合规留痕？',
  '车队如何通过财运通与数字能源降低加油与充电成本？',
  '如何申请产品免费试用或预约行业专家上门演示？',
]

const questionPool = computed(() => {
  if (cfg.value?.questions && cfg.value.questions.length > 0) {
    return cfg.value.questions
  }
  return DEFAULT_QUESTIONS
})

const visibleQuestions = computed(() => {
  const pool = questionPool.value
  if (pool.length <= 5) return pool
  const start = (batchIndex.value * 5) % pool.length
  const out: string[] = []
  for (let i = 0; i < 5; i++) {
    out.push(pool[(start + i) % pool.length])
  }
  return out
})

function rotateBatch() {
  spinning.value = true
  batchIndex.value += 1
  window.setTimeout(() => {
    spinning.value = false
  }, 360)
}

const DEFAULT_FAQ_PAIRS: XiaoQiFaqPair[] = [
  {
    q: '新能源物流车运营痛点解决方案？',
    thought:
      '正在检索新能源重卡与城配车队运营数据库：分析续航里程焦虑、充电排班调度、电池SOH衰减监控以及电耗精细化核算四大核心痛点，匹配数字能源与智能管车一体化方案...',
    a: `针对新能源物流车运营，我们提供「智能管车 + 数字能源」一体化解决方案：
1. 智能补能调度：打通全国核心干线与园区充电网络，根据剩余SOC与运单时效智能规划最优充换电站点；
2. 能耗精细核算：按单、按车、按司机自动核算百公里电耗，识别异常高耗能驾驶行为；
3. 电池健康守护：7×24h 实时监测电芯温差与SOH状态，提前预警热失控风险，延长电池全生命周期价值。`,
  },
  {
    q: '如何构建智慧物流园区？',
    thought:
      '正在调取智慧园区与履约可视化架构模型：整合入园预约、道闸车牌/RFID自动识别、月台智能分配、场内安全测速与装卸时长监控模块...',
    a: `构建智慧物流园区核心在于「场内场外协同 + 全流程无人化感知」：
1. 预约与准入联动：在途车辆ETA精准预测，到场自动触发门禁与RFID读头识别；
2. 智能月台调度：根据车型、货品温区自动分配空闲月台，减少场内排队等待时间 40% 以上；
3. 装卸作业透明化：通过AI摄像头与挂车专用主机实时记录装卸进度与安全合规状态。`,
  },
  {
    q: '“三晒两抓-城配版”系统中司机的安全分是如何分类的？',
    thought:
      '正在查询安全管家「三晒两抓-城配版」算法评分规则：提取驾驶行为风险权重、里程归一化折算与红黄绿三档分级标准...',
    a: `在「三晒两抓-城配版」安全管理体系中，系统基于前向防碰撞、疲劳分心识别及急加速/急刹等维度的AI感知数据，将司机安全分按周期动态分为三档：
• 标杆绿档（90~100分）：安全习惯优异，纳入车队正向激励红榜；
• 关注黄档（75~89分）：存在偶发跟车过近或分心行为，由AI语音实时提醒并推送线上微课；
• 高危红档（<75分）：高频触发高风险事件，列为安全官重点“两抓”约谈与精准干预对象。`,
  },
  {
    q: '物流企业如何控制货损率？',
    thought:
      '正在分析冷链、大宗与快消干线货损成因：从温湿度失控、暴力装卸、途中异常开锁/偷盗及急刹颠簸四个维度整理防控闭环...',
    a: `控制货损率需要构建「事前预防 + 事中AI干预 + 事后定责」全链路闭环：
1. 温控与环境监测：冷链管家实时监控厢内多点温湿度，偏离阈值秒级告警；
2. 防偷盗与电子锁：结合AI量方、轴称重与视频监控，对异常停留、私自开锁或重量突变实时取证告警；
3. 平稳驾驶管控：通过安全驾驶系统减少急刹、急转弯造成的货物挤压破损。`,
  },
  {
    q: '中国“双碳”目标及全球ESG趋势对物流企业提出哪些新要求？',
    thought:
      '正在检索物流行业双碳政策与企业ESG碳足迹核算标准（ISO 14083 / GLEC框架）...',
    a: `在“双碳”与全球ESG合规背景下，货主与监管机构对物流企业提出了三大核心要求：
1. 可核查的碳足迹台账：需提供按单、按吨公里的真实碳排放数据，告别粗放估算；
2. 绿色运力替代比例：加快电动重卡、LNG清洁能源车辆及轻量化共享挂车的应用；
3. 满载率与多式联运优化：借助AI智能配载与线路优化降低空驶率，实现经济效益与减碳目标双赢。`,
  },
]

const faqPool = computed(() => {
  if (cfg.value?.faqPairs && cfg.value.faqPairs.length > 0) {
    return [...cfg.value.faqPairs, ...DEFAULT_FAQ_PAIRS]
  }
  return DEFAULT_FAQ_PAIRS
})

function findAnswer(question: string): { thought: string; answer: string } {
  const qClean = question.trim().toLowerCase()
  for (const item of faqPool.value) {
    if (
      item.q.toLowerCase().includes(qClean) ||
      qClean.includes(item.q.slice(0, 6).toLowerCase())
    ) {
      return {
        thought:
          item.thought ||
          `正在为您深度检索「${item.q}」相关的行业实践、产品架构与真实运营数据...`,
        answer: item.a,
      }
    }
  }
  return {
    thought: `正在理解您的问题「${question}」，并检索 ${props.site?.siteName || '企业'} 产品知识库、解决方案矩阵与标杆客户案例...`,
    answer: `关于「${question}」：
我们提供软硬一体化的全场景 AI 与数字化平台方案，可根据您的具体业务规模、管理痛点与系统对接需求进行灵活配置。

您可以点击右上角「产品试用」预约专属顾问演示，或在后台「全站设置 → 小鑫AI配置」中接入您企业专属的大模型 API 与知识库问答库。`,
  }
}

async function scrollToBottom() {
  await nextTick()
  if (scrollBox.value) {
    scrollBox.value.scrollTop = scrollBox.value.scrollHeight
  }
}

async function askQuestion(rawText?: string) {
  const q = (rawText ?? inputVal.value).trim()
  if (!q) return
  inputVal.value = ''
  showHistory.value = false

  if (streamTimer) {
    clearInterval(streamTimer)
    streamTimer = null
  }

  messages.value.push({
    id: msgCounter++,
    role: 'user',
    text: q,
  })

  const replyId = msgCounter++
  const matched = findAnswer(q)

  messages.value.push({
    id: replyId,
    role: 'assistant',
    text: '',
    thought: deepThinkOn.value ? matched.thought : undefined,
    thinking: deepThinkOn.value,
  })
  await scrollToBottom()

  // 服务端代理调用后台配置的大模型；未接入、限流或上游失败时回退本地知识库
  try {
    const history = messages.value
      .filter((m) => m.id !== replyId && m.text && m.id < replyId - 1)
      .slice(-12)
      .map((m) => ({ role: m.role, content: m.text }))
    const { answer } = await askXiaoqi(q, history)
    if (answer) {
      streamTextOut(replyId, answer)
      return
    }
  } catch {
    // 回退本地回答
  }

  window.setTimeout(
    () => {
      streamTextOut(replyId, matched.answer)
    },
    deepThinkOn.value ? 650 : 180,
  )
}

function streamTextOut(msgId: number, fullText: string) {
  const target = messages.value.find((m) => m.id === msgId)
  if (!target) return
  target.thinking = false
  let idx = 0
  streamTimer = window.setInterval(() => {
    idx += 3
    target.text = fullText.slice(0, idx)
    scrollToBottom()
    if (idx >= fullText.length && streamTimer) {
      clearInterval(streamTimer)
      streamTimer = null
    }
  }, 24)
}

function startNewSession() {
  if (messages.value.length > 0) {
    const firstUser = messages.value.find((m) => m.role === 'user')
    sessions.value.unshift({
      id: String(Date.now()),
      title: firstUser?.text.slice(0, 18) || '历史对话',
      time: new Date().toLocaleTimeString().slice(0, 5),
      messages: [...messages.value],
    })
  }
  messages.value = []
  showHistory.value = false
}

function restoreSession(sess: SessionItem) {
  messages.value = [...sess.messages]
  showHistory.value = false
  scrollToBottom()
}

function openTrialFromBot() {
  closeXiaoQi()
  openConsult({ sourcePosition: 'XIAOQI_AI', title: '预约产品展示' })
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape' && xiaoqi.open) {
    closeXiaoQi()
  }
}

watch(
  () => xiaoqi.open,
  (isOpen) => {
    if (isOpen) {
      document.addEventListener('keydown', onKeydown)
      if (xiaoqi.initialQuestion) {
        const q = xiaoqi.initialQuestion
        xiaoqi.initialQuestion = ''
        askQuestion(q)
      }
    } else {
      document.removeEventListener('keydown', onKeydown)
    }
  },
)

onBeforeUnmount(() => {
  document.removeEventListener('keydown', onKeydown)
  if (streamTimer) clearInterval(streamTimer)
})
</script>

<template>
  <Teleport to="body">
    <Transition name="xq-drawer">
      <div v-if="xiaoqi.open" class="xq-backdrop" @click.self="closeXiaoQi">
        <!-- 1:1 还原 首页.mp4 00:34-00:41 右侧小鑫 AI 抽屉 -->
        <aside class="xq-panel" role="dialog" aria-label="小鑫 AI 智能助手">
          <!-- 顶部栏：左侧菜单 ☰ + 右侧 关闭窗口 & 新建会话图标 -->
          <header class="xq-topbar">
            <button
              type="button"
              class="xq-icon-btn"
              title="历史会话记录"
              @click="showHistory = !showHistory"
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="3" y1="6" x2="21" y2="6"></line>
                <line x1="3" y1="12" x2="21" y2="12"></line>
                <line x1="3" y1="18" x2="21" y2="18"></line>
              </svg>
            </button>

            <div class="xq-topbar__right">
              <button type="button" class="xq-close-btn" @click="closeXiaoQi">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                  <polyline points="16 17 21 12 16 7"></polyline>
                  <line x1="21" y1="12" x2="9" y2="12"></line>
                </svg>
                <span>关闭窗口</span>
              </button>
              <button
                type="button"
                class="xq-icon-btn"
                title="开启新对话"
                @click="startNewSession"
              >
                <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"></path>
                </svg>
              </button>
            </div>
          </header>

          <!-- 历史对话下拉抽屉 -->
          <div v-if="showHistory" class="xq-history">
            <div class="xq-history__head">
              <strong>历史对话记录</strong>
              <button type="button" class="xq-history__new" @click="startNewSession">+ 新对话</button>
            </div>
            <p v-if="sessions.length === 0" class="xq-history__empty">暂无历史会话记录</p>
            <ul v-else class="xq-history__list">
              <li v-for="s in sessions" :key="s.id" @click="restoreSession(s)">
                <span>{{ s.title }}</span>
                <time>{{ s.time }}</time>
              </li>
            </ul>
          </div>

          <!-- 主体滚动区 -->
          <div ref="scrollBox" class="xq-body">
            <!-- 1. 顶部小鑫身份卡片 -->
            <div class="xq-hero-card">
              <div class="xq-hero-card__top">
                <div class="xq-avatar" aria-hidden="true">
                  <div class="xq-avatar__pin">
                    <div class="xq-avatar__face">
                      <span></span>
                      <span></span>
                    </div>
                    <i class="xq-avatar__g">G</i>
                  </div>
                  <div class="xq-avatar__glow"></div>
                </div>

                <div class="xq-hero-card__info">
                  <div class="xq-hero-card__title-row">
                    <h3 class="xq-hero-card__title">
                      我是 <em>{{ botName }}</em>
                    </h3>
                    <span class="xq-cert">🛡️ {{ certBadge }}</span>
                  </div>
                  <p class="xq-hero-card__sub">{{ welcomeSub }}</p>
                </div>
              </div>

              <!-- 5 个能力胶囊标签 -->
              <div class="xq-caps">
                <button
                  v-for="cap in capabilities"
                  :key="cap"
                  type="button"
                  class="xq-cap-pill"
                  @click="askQuestion(`请介绍贵司的${cap}能力与核心优势`)"
                >
                  {{ cap }}
                </button>
              </div>
            </div>

            <!-- 2. 换一批 + 5 条预设高频问题列表 -->
            <div class="xq-presets">
              <div class="xq-presets__toolbar">
                <button type="button" class="xq-refresh-btn" @click="rotateBatch">
                  <span class="xq-refresh-icon" :class="{ 'is-spinning': spinning }">↻</span>
                  <span>换一批</span>
                </button>
              </div>

              <ul class="xq-q-list">
                <li v-for="q in visibleQuestions" :key="q">
                  <button type="button" class="xq-q-item" @click="askQuestion(q)">
                    <span class="xq-q-item__text">{{ q }}</span>
                    <span class="xq-q-item__arrow" aria-hidden="true">→</span>
                  </button>
                </li>
              </ul>
            </div>

            <!-- 3. 实时流式对话消息列表 -->
            <div v-if="messages.length > 0" class="xq-chat-list">
              <div
                v-for="msg in messages"
                :key="msg.id"
                class="xq-msg"
                :class="`xq-msg--${msg.role}`"
              >
                <div class="xq-msg__bubble">
                  <div v-if="msg.thought" class="xq-msg__thought">
                    <div class="xq-msg__thought-head">
                      <span>💭 {{ msg.thinking ? '正在深度思考...' : `已深度思考 (${deepModel})` }}</span>
                    </div>
                    <p>{{ msg.thought }}</p>
                  </div>
                  <p class="xq-msg__text">{{ msg.text || (msg.thinking ? '' : '...') }}</p>
                  <div v-if="msg.role === 'assistant' && !msg.thinking" class="xq-msg__actions">
                    <button type="button" class="xq-msg__cta" @click="openTrialFromBot">
                      预约专家演示 →
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 底部输入区：圆角紫框输入 + 深度思考 DeepSeek R1 切换胶囊 + 免责声明 -->
          <footer class="xq-footer">
            <form class="xq-input-box" @submit.prevent="askQuestion()">
              <input
                v-model="inputVal"
                type="text"
                class="xq-input"
                placeholder="输入问题，为您答疑解惑"
                maxlength="300"
              />
              <button
                type="submit"
                class="xq-send-btn"
                :disabled="!inputVal.trim()"
                aria-label="发送问题"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 3L4 21l8-4 8 4L12 3z"></path>
                </svg>
              </button>
            </form>

            <div class="xq-footer__meta">
              <button
                v-if="cfg?.deepThinkEnabled !== false"
                type="button"
                class="xq-deep-pill"
                :class="{ 'is-active': deepThinkOn }"
                @click="deepThinkOn = !deepThinkOn"
              >
                <span>深度思考 {{ deepModel }}</span>
              </button>
            </div>

            <p class="xq-disclaimer">{{ disclaimer }}</p>
          </footer>
        </aside>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.xq-backdrop {
  position: fixed;
  inset: 0;
  z-index: 88;
  background: rgba(10, 12, 20, 0.18);
  display: flex;
  justify-content: flex-end;
}

.xq-panel {
  width: 100%;
  max-width: 416px;
  height: 100dvh;
  background: #f6f7fb;
  box-shadow: -16px 0 48px rgba(10, 12, 24, 0.16);
  display: flex;
  flex-direction: column;
  position: relative;
}

.xq-topbar {
  height: 52px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f6f7fb;
  flex: none;
}

.xq-topbar__right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.xq-icon-btn {
  width: 30px;
  height: 30px;
  border: none;
  background: none;
  color: #2b2f38;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
}

.xq-icon-btn:hover {
  background: rgba(91, 75, 232, 0.08);
  color: #5b4be8;
}

.xq-close-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: none;
  background: none;
  font-size: 12px;
  color: #5f6470;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
}

.xq-close-btn:hover {
  color: #101014;
  background: rgba(16, 16, 20, 0.05);
}

.xq-history {
  position: absolute;
  top: 52px;
  left: 16px;
  right: 16px;
  z-index: 20;
  background: #ffffff;
  border-radius: 10px;
  padding: 14px;
  box-shadow: 0 16px 36px rgba(16, 24, 40, 0.14);
}

.xq-history__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  margin-bottom: 10px;
}

.xq-history__new {
  border: none;
  background: none;
  color: #5b4be8;
  font-size: 12.5px;
  cursor: pointer;
}

.xq-history__empty {
  font-size: 12px;
  color: #98a2b3;
  text-align: center;
  padding: 12px 0;
}

.xq-history__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.xq-history__list li {
  display: flex;
  justify-content: space-between;
  padding: 8px 10px;
  border-radius: 6px;
  font-size: 12.5px;
  background: #f8f9fc;
  cursor: pointer;
}

.xq-history__list li:hover {
  background: #ede9fe;
  color: #5b4be8;
}

.xq-body {
  flex: 1;
  overflow-y: auto;
  padding: 8px 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* 顶部渐变身份卡 */
.xq-hero-card {
  position: relative;
  border-radius: 14px;
  padding: 18px 16px 16px;
  background: linear-gradient(135deg, #e0e7ff 0%, #ede9fe 52%, #e0f2fe 100%);
  box-shadow: 0 6px 20px rgba(79, 70, 229, 0.08);
}

.xq-hero-card__top {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 14px;
}

.xq-avatar {
  position: relative;
  width: 54px;
  height: 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: none;
}

.xq-avatar__pin {
  position: relative;
  width: 46px;
  height: 46px;
  border-radius: 50% 50% 50% 14%;
  transform: rotate(-45deg);
  background: linear-gradient(135deg, #6366f1 0%, #4338ca 60%, #38bdf8 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 18px rgba(67, 56, 202, 0.38);
}

.xq-avatar__face {
  transform: rotate(45deg);
  width: 28px;
  height: 18px;
  border-radius: 10px;
  background: #0f172a;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: -4px;
}

.xq-avatar__face span {
  width: 5px;
  height: 8px;
  border-radius: 999px;
  background: #38bdf8;
  box-shadow: 0 0 8px #38bdf8;
}

.xq-avatar__g {
  position: absolute;
  bottom: 5px;
  right: 7px;
  transform: rotate(45deg);
  font-size: 9px;
  font-weight: 900;
  font-style: italic;
  color: #ffffff;
}

.xq-avatar__glow {
  margin-top: 5px;
  width: 32px;
  height: 6px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(79, 70, 229, 0.55) 0%, transparent 75%);
}

.xq-hero-card__info {
  flex: 1;
  min-width: 0;
}

.xq-hero-card__title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.xq-hero-card__title {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.xq-hero-card__title em {
  font-style: italic;
  font-size: 18px;
  font-weight: 900;
  color: #4f46e5;
  margin-left: 2px;
}

.xq-cert {
  font-size: 11px;
  color: #64748b;
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.xq-hero-card__sub {
  margin: 4px 0 0;
  font-size: 12.5px;
  color: #334155;
  font-weight: 500;
}

.xq-caps {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.xq-cap-pill {
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.85);
  background: rgba(255, 255, 255, 0.82);
  color: #334155;
  font-size: 12px;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.18s ease;
}

.xq-cap-pill:hover {
  background: #ffffff;
  color: #4f46e5;
  border-color: #a5b4fc;
}

/* 换一批与问题列表 */
.xq-presets__toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}

.xq-refresh-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: none;
  background: none;
  font-size: 12px;
  color: #475569;
  cursor: pointer;
  padding: 2px 6px;
}

.xq-refresh-btn:hover {
  color: #4f46e5;
}

.xq-refresh-icon {
  display: inline-block;
  font-size: 13px;
  transition: transform 0.35s ease;
}

.xq-refresh-icon.is-spinning {
  transform: rotate(360deg);
}

.xq-q-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.xq-q-item {
  width: 100%;
  padding: 13px 16px;
  border-radius: 10px;
  border: 1px solid rgba(226, 232, 240, 0.8);
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  text-align: left;
  font-family: inherit;
  font-size: 13px;
  color: #1e293b;
  cursor: pointer;
  transition: border-color 0.18s ease, box-shadow 0.18s ease, color 0.18s ease;
}

.xq-q-item:hover {
  border-color: #a5b4fc;
  color: #4f46e5;
  box-shadow: 0 4px 14px rgba(79, 70, 229, 0.07);
}

.xq-q-item__text {
  line-height: 1.45;
}

.xq-q-item__arrow {
  color: #94a3b8;
  font-size: 13px;
  flex: none;
}

/* 对话气泡 */
.xq-chat-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 4px;
}

.xq-msg {
  display: flex;
}

.xq-msg--user {
  justify-content: flex-end;
}

.xq-msg__bubble {
  max-width: 90%;
  padding: 12px 14px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.68;
}

.xq-msg--user .xq-msg__bubble {
  background: #4f46e5;
  color: #ffffff;
  border-bottom-right-radius: 3px;
}

.xq-msg--assistant .xq-msg__bubble {
  background: #ffffff;
  color: #1e293b;
  border: 1px solid #e2e8f0;
  border-bottom-left-radius: 3px;
}

.xq-msg__thought {
  margin-bottom: 8px;
  padding: 8px 10px;
  border-radius: 8px;
  background: #f8fafc;
  border-left: 3px solid #818cf8;
  font-size: 11.5px;
  color: #64748b;
}

.xq-msg__thought-head {
  font-weight: 600;
  color: #4f46e5;
  margin-bottom: 4px;
}

.xq-msg__text {
  margin: 0;
  white-space: pre-line;
}

.xq-msg__actions {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #e2e8f0;
  display: flex;
  justify-content: flex-end;
}

.xq-msg__cta {
  border: none;
  background: none;
  color: #4f46e5;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}

/* 底部输入栏 */
.xq-footer {
  padding: 10px 16px 12px;
  background: #f6f7fb;
  border-top: 1px solid rgba(226, 232, 240, 0.7);
  flex: none;
}

.xq-input-box {
  height: 44px;
  border-radius: 10px;
  border: 1px solid #a5b4fc;
  background: #ffffff;
  display: flex;
  align-items: center;
  padding: 0 8px 0 14px;
  gap: 8px;
}

.xq-input {
  flex: 1;
  border: none;
  background: none;
  font-size: 13px;
  color: #1e293b;
  font-family: inherit;
  outline: none;
}

.xq-input::placeholder {
  color: #94a3b8;
}

.xq-send-btn {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: none;
  background: #818cf8;
  color: #ffffff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.xq-send-btn:hover:not(:disabled) {
  background: #4f46e5;
}

.xq-send-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.xq-footer__meta {
  margin-top: 8px;
  display: flex;
  align-items: center;
}

.xq-deep-pill {
  padding: 4px 12px;
  border-radius: 6px;
  border: 1px solid transparent;
  background: #e2e8f0;
  color: #64748b;
  font-size: 11.5px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.18s ease;
}

.xq-deep-pill.is-active {
  background: #ede9fe;
  color: #4f46e5;
  border-color: #c4b5fd;
}

.xq-disclaimer {
  margin: 8px 0 0;
  text-align: center;
  font-size: 11px;
  color: #94a3b8;
}

.xq-drawer-enter-active,
.xq-drawer-leave-active {
  transition: opacity 0.25s ease;
}

.xq-drawer-enter-active .xq-panel,
.xq-drawer-leave-active .xq-panel {
  transition: transform 0.28s cubic-bezier(0.22, 1, 0.36, 1);
}

.xq-drawer-enter-from,
.xq-drawer-leave-to {
  opacity: 0;
}

.xq-drawer-enter-from .xq-panel,
.xq-drawer-leave-to .xq-panel {
  transform: translateX(100%);
}
</style>
