<!-- 对应源码路径: website/web/src/views/ProductDetailView.vue -->
<script setup lang="ts">
/**
 * 产品详情页（同时满足《开发文档》6.5 与《参考站核对.md》2.5 & D1 的 7 段结构化版式）：
 * 1. 首屏：产品名称 + 一句话价值摘要 + 「立即咨询」按钮 + 首屏大图 (hero / cover)
 * 2. 核心能力 / 硬件感知模块（features[] / hardwareModules[]，如 ADAS / DSM / BSD 图文卡片）
 * 3. 算法与量化效果指标带（effectMetrics[]，如「事故率下降 17%」「风险识别率 99.2%」）
 * 4. 按角色拆分价值 Tab 区（roleTabs[]：司机端 / 安全员 / 车队长 切换）
 * 5. 端侧协同工具矩阵（terminalTools[]：WEB 管理端 + 移动 APP + 微信群实时播报）
 * 6. 富文本详情正文（bodyHtml，通过 ArticleBody 安全渲染）+ 相关标杆案例（relatedCases[] 含 outcomes 数字）
 * 7. 底部咨询转化行动区（自动携带 PRODUCT_DETAIL 与当前产品 ID）
 */
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ArticleBody from '../../../shared/ArticleBody.vue'
import EmptyState from '@/components/EmptyState.vue'
import { ApiError } from '@/api/http'
import { getProduct } from '@/api/public'
import { openConsult } from '@/composables/consult'
import { usePageMeta } from '@/composables/usePageMeta'
import { vReveal } from '@/directives/reveal'
import type {
  OutcomeMetric,
  ProductDetail,
  ProductRoleTab,
  ProductTerminalTool,
  TitledItem,
} from '@/data/types'

const route = useRoute()
const router = useRouter()
const id = computed(() => String(route.params.id || ''))

const data = ref<ProductDetail | null>(null)
const loading = ref(true)
const notFound = ref(false)
const error = ref('')
const activeRoleIdx = ref(0)

usePageMeta(() => ({
  title: data.value?.seo?.title || data.value?.title || '产品详情',
  description: data.value?.seo?.description || data.value?.summary || '',
}))

const heroMedia = computed(() => data.value?.hero || data.value?.cover || null)

const DEFAULT_HARDWARE_MODULES: TitledItem[] = [
  {
    tag: 'ADAS 前向防碰撞',
    title: '智能前向视觉感知终端',
    description: '实时识别前车距离、车道偏离与行人穿行，毫秒级声光预警，降低追尾事故风险。',
  },
  {
    tag: 'DSM 驾驶状态监测',
    title: 'AI 驾驶员行为分析相机',
    description: '精准识别闭眼疲劳、打哈欠、抽烟、接打手机及分心驾驶，联动语音播报主动干预。',
  },
  {
    tag: 'BSD 盲区生命探测',
    title: '右转与倒车盲点监测雷达',
    description: '覆盖大型车辆右转内轮差与车尾盲区，声光报警提醒司机与车外行人双向避让。',
  },
]

const hardwareList = computed<TitledItem[]>(() => {
  if (data.value?.hardwareModules && data.value.hardwareModules.length > 0) {
    return data.value.hardwareModules
  }
  if (data.value?.features && data.value.features.length > 0) {
    return data.value.features
  }
  return DEFAULT_HARDWARE_MODULES
})

const DEFAULT_EFFECT_METRICS: OutcomeMetric[] = [
  { displayValue: '17%↓', label: '车队百公里高危事故率平均下降' },
  { displayValue: '99.2%', label: 'AI 高危风险场景识别准确率' },
  { displayValue: '7×24h', label: '云端安全官全天候主动干预守护' },
  { displayValue: '4倍↑', label: '车队安全管理人效提升' },
]

const effectMetrics = computed<OutcomeMetric[]>(() => {
  if (data.value?.effectMetrics && data.value.effectMetrics.length > 0) {
    return data.value.effectMetrics
  }
  return DEFAULT_EFFECT_METRICS
})

const DEFAULT_ROLE_TABS: ProductRoleTab[] = [
  {
    role: '司机端',
    headline: '实时语音温和提醒，做司机的随车安全副驾',
    bullets: [
      '车距过近、分心疲劳时车内语音即时提醒，避免事后罚款',
      '每日安全分自动生成，安全里程达标可获车队正向红包激励',
      '一键语音播报路况与异常天气预警，行车更安心',
    ],
  },
  {
    role: '安全员',
    headline: 'AI 自动分级过滤 95% 无效告警，只抓高危红档风险',
    bullets: [
      '系统自动按「红、黄、绿」三档对风险事件分级，告别人工盯屏疲劳',
      '高危事件自动截取前后短视频与轨迹证据，一键下发约谈任务',
      '闭环跟踪每次干预后的驾驶行为改善率，合规台账自动归档',
    ],
  },
  {
    role: '车队长 / 经营者',
    headline: '看得见的安全ROI，大幅降低出险率与次年保费成本',
    bullets: [
      '车队整体安全分、百公里报警率、红档司机占比一屏掌控',
      '联动保险风控体系，显著降低重大事故赔付支出与停运损失',
      '支持多分子公司、多车队横向对标排行，数据驱动精细化管理',
    ],
  },
]

const roleTabs = computed<ProductRoleTab[]>(() => {
  if (data.value?.roleTabs && data.value.roleTabs.length > 0) {
    return data.value.roleTabs
  }
  return DEFAULT_ROLE_TABS
})

const currentRoleTab = computed(() => roleTabs.value[activeRoleIdx.value] ?? roleTabs.value[0])

const DEFAULT_TERMINAL_TOOLS: ProductTerminalTool[] = [
  {
    name: 'WEB 数字化工作台',
    badge: 'PC 端',
    description: '全景地图实时监控、风险视频调阅、安全分大盘与合规报表一键导出。',
  },
  {
    name: '移动管车 APP / 小程序',
    badge: '移动端',
    description: '随时随地接收高危告警推送、查看单车轨迹、在线批复安全整改闭环。',
  },
  {
    name: '企业微信 / 微信群机器人',
    badge: '即时协同',
    description: '班前安全播报、红档事件实时推群、周度红黑榜自动晒单，管理零门槛。',
  },
]

const terminalTools = computed<ProductTerminalTool[]>(() => {
  if (data.value?.terminalTools && data.value.terminalTools.length > 0) {
    return data.value.terminalTools
  }
  return DEFAULT_TERMINAL_TOOLS
})

function triggerConsult() {
  openConsult({
    sourcePosition: 'PRODUCT_DETAIL',
    contentType: 'PRODUCT',
    contentId: id.value,
    title: '预约产品展示',
    initialDemand: data.value?.title ? `咨询产品：${data.value.title}` : '',
  })
}

async function load() {
  if (!id.value) return
  loading.value = true
  notFound.value = false
  error.value = ''
  try {
    data.value = await getProduct(id.value)
  } catch (err) {
    data.value = null
    if (err instanceof ApiError && err.status === 404) {
      notFound.value = true
    } else {
      error.value = err instanceof ApiError ? err.message : '产品详情加载失败，请稍后重试'
    }
  } finally {
    loading.value = false
  }
}

watch(id, load)
onMounted(load)
</script>

<template>
  <div class="pdetail-page">
    <div v-if="loading" class="container state-box">
      <div class="skeleton"></div>
    </div>

    <div v-else-if="notFound" class="container state-box">
      <EmptyState
        title="未找到该产品"
        description="该产品可能尚未发布、已下线或链接地址有误。"
      >
        <button type="button" class="btn btn--primary" @click="router.push('/products')">
          返回产品列表
        </button>
      </EmptyState>
    </div>

    <div v-else-if="error" class="container state-box">
      <EmptyState title="加载失败" :description="error">
        <button type="button" class="btn btn--primary" @click="load">重试</button>
      </EmptyState>
    </div>

    <template v-else-if="data">
      <!-- 1. 首屏：产品名 + 一句话价值 + 「立即咨询」 + 首屏视觉 -->
      <section class="pdetail-hero">
        <div class="container pdetail-hero__grid">
          <div v-reveal class="pdetail-hero__text">
            <span v-if="data.categoryName" class="pdetail-hero__cat">{{ data.categoryName }}</span>
            <h1 class="pdetail-hero__title">{{ data.title }}</h1>
            <p v-if="data.summary" class="pdetail-hero__sub">{{ data.summary }}</p>
            <div class="pdetail-hero__actions">
              <button type="button" class="pdetail-cta-btn" @click="triggerConsult">
                立即咨询
              </button>
              <RouterLink to="/products" class="pdetail-back-link">返回产品列表</RouterLink>
            </div>
          </div>

          <div v-if="heroMedia" v-reveal="120" class="pdetail-hero__visual">
            <img :src="heroMedia.url" :alt="heroMedia.alt || data.title" class="pdetail-hero__img" />
          </div>
        </div>
      </section>

      <!-- 2. 硬件与核心能力模块（features[] / hardwareModules[]） -->
      <section v-if="hardwareList.length" class="pdetail-sec">
        <div class="container">
          <h2 v-reveal class="pdetail-sec__title">核心感知与能力模块</h2>
          <div class="pdetail-hw-grid">
            <article
              v-for="(item, idx) in hardwareList"
              :key="item.key || item.title || idx"
              v-reveal="idx * 70"
              class="pdetail-hw-card"
            >
              <span v-if="item.tag" class="pdetail-hw-card__tag">{{ item.tag }}</span>
              <h3 class="pdetail-hw-card__title">{{ item.title }}</h3>
              <p class="pdetail-hw-card__desc">{{ item.description }}</p>
            </article>
          </div>
        </div>
      </section>

      <!-- 3. 算法与量化效果指标带（effectMetrics[]） -->
      <section v-if="effectMetrics.length" class="pdetail-metrics-band">
        <div class="container">
          <h2 v-reveal class="pdetail-sec__title pdetail-sec__title--light">算法赋能与真实运营效果</h2>
          <div class="pdetail-metrics-grid">
            <div
              v-for="(m, mIdx) in effectMetrics"
              :key="m.label + mIdx"
              v-reveal="mIdx * 70"
              class="pdetail-metric-item"
            >
              <strong class="pdetail-metric-item__val">{{ m.displayValue }}</strong>
              <span class="pdetail-metric-item__lbl">{{ m.label }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 4. 按角色拆分价值 Tab 区（司机端 / 安全员 / 车队长） -->
      <section v-if="roleTabs.length" class="pdetail-sec pdetail-sec--alt">
        <div class="container">
          <h2 v-reveal class="pdetail-sec__title">全角色协同增效</h2>

          <div v-reveal="80" class="pdetail-role-tabs" role="tablist">
            <button
              v-for="(rt, rIdx) in roleTabs"
              :key="rt.role"
              type="button"
              role="tab"
              class="pdetail-role-tab"
              :class="{ 'is-active': activeRoleIdx === rIdx }"
              :aria-selected="activeRoleIdx === rIdx"
              @click="activeRoleIdx = rIdx"
            >
              {{ rt.role }}
            </button>
          </div>

          <div v-if="currentRoleTab" v-reveal="120" class="pdetail-role-panel">
            <h3 class="pdetail-role-panel__headline">{{ currentRoleTab.headline }}</h3>
            <ul class="pdetail-role-panel__list">
              <li v-for="(b, bIdx) in currentRoleTab.bullets" :key="bIdx">{{ b }}</li>
            </ul>
          </div>
        </div>
      </section>

      <!-- 5. 端侧工具矩阵（WEB + APP + 微信群） -->
      <section v-if="terminalTools.length" class="pdetail-sec">
        <div class="container">
          <h2 v-reveal class="pdetail-sec__title">多端工具无缝触达</h2>
          <div class="pdetail-hw-grid">
            <div
              v-for="(tool, tIdx) in terminalTools"
              :key="tool.name + tIdx"
              v-reveal="tIdx * 70"
              class="pdetail-tool-card"
            >
              <span v-if="tool.badge" class="pdetail-tool-card__badge">{{ tool.badge }}</span>
              <h3 class="pdetail-tool-card__title">{{ tool.name }}</h3>
              <p class="pdetail-tool-card__desc">{{ tool.description }}</p>
            </div>
          </div>
        </div>
      </section>

      <!-- 6. 详情富文本正文 (bodyHtml) -->
      <section v-if="data.bodyHtml" class="pdetail-sec pdetail-sec--prose">
        <div class="container">
          <ArticleBody :html="data.bodyHtml" :title="data.title" />
        </div>
      </section>

      <!-- 7. 相关标杆案例（relatedCases[]，展示成果数字 outcomes） -->
      <section v-if="data.relatedCases?.length" class="pdetail-sec pdetail-sec--alt">
        <div class="container">
          <h2 v-reveal class="pdetail-sec__title">标杆客户实践案例</h2>
          <div class="pdetail-hw-grid">
            <RouterLink
              v-for="(c, cIdx) in data.relatedCases"
              :key="c.id"
              v-reveal="cIdx * 80"
              :to="`/cases/${c.id}`"
              class="pdetail-case-card"
            >
              <div v-if="c.cover" class="pdetail-case-card__media">
                <img :src="c.cover.url" :alt="c.cover.alt || c.title" loading="lazy" />
              </div>
              <div class="pdetail-case-card__body">
                <span v-if="c.customerName" class="pdetail-case-card__cust">{{ c.customerName }}</span>
                <h3 class="pdetail-case-card__title">{{ c.title }}</h3>
                <p v-if="c.summary" class="pdetail-case-card__sum">{{ c.summary }}</p>
              </div>
            </RouterLink>
          </div>
        </div>
      </section>

      <!-- 8. 底部咨询转化行动区 -->
      <section class="pdetail-bottom-cta">
        <div class="container pdetail-bottom-cta__inner">
          <h2 class="pdetail-bottom-cta__title">立即预约「{{ data.title }}」专属演示与方案报价</h2>
          <button type="button" class="pdetail-cta-btn pdetail-cta-btn--light" @click="triggerConsult">
            立即咨询
          </button>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.pdetail-page {
  background: #ffffff;
  color: #1f2329;
}

.state-box {
  padding: 80px 0;
}

.pdetail-hero {
  background: linear-gradient(135deg, #0d111d 0%, #1e1b4b 55%, #090a0f 100%);
  color: #ffffff;
  padding: 84px 0;
}

.pdetail-hero__grid {
  max-width: 1120px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  gap: 56px;
  align-items: center;
}

.pdetail-hero__cat {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 999px;
  background: rgba(99, 102, 241, 0.25);
  color: #c7d2fe;
  font-size: 12px;
  margin-bottom: 16px;
}

.pdetail-hero__title {
  font-size: clamp(32px, 4vw, 48px);
  font-weight: 700;
  margin: 0 0 18px;
}

.pdetail-hero__sub {
  font-size: 16px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.82);
  margin: 0 0 32px;
}

.pdetail-hero__actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.pdetail-cta-btn {
  height: 44px;
  padding: 0 34px;
  border: none;
  border-radius: 2px;
  background: #5b4be8;
  color: #ffffff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.pdetail-cta-btn:hover {
  background: #4338ca;
}

.pdetail-cta-btn--light {
  background: #ffffff;
  color: #101014;
}

.pdetail-cta-btn--light:hover {
  background: #e0e7ff;
  color: #4338ca;
}

.pdetail-back-link {
  color: rgba(255, 255, 255, 0.75);
  font-size: 13.5px;
  text-decoration: none;
}

.pdetail-back-link:hover {
  color: #ffffff;
}

.pdetail-hero__img {
  width: 100%;
  border-radius: 8px;
  box-shadow: 0 24px 56px rgba(0, 0, 0, 0.4);
}

.pdetail-sec {
  padding: 80px 0;
}

.pdetail-sec--alt {
  background: #f5f7fb;
}

.pdetail-sec__title {
  text-align: center;
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 48px;
}

.pdetail-sec__title--light {
  color: #ffffff;
}

.pdetail-hw-grid {
  max-width: 1100px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 24px;
}

.pdetail-hw-card,
.pdetail-tool-card {
  padding: 32px 28px;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  box-shadow: 0 8px 24px rgba(16, 24, 40, 0.04);
}

.pdetail-hw-card__tag,
.pdetail-tool-card__badge {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  color: #5b4be8;
  background: #ede9fe;
  padding: 3px 10px;
  border-radius: 4px;
  margin-bottom: 14px;
}

.pdetail-hw-card__title,
.pdetail-tool-card__title {
  font-size: 19px;
  font-weight: 700;
  margin: 0 0 12px;
}

.pdetail-hw-card__desc,
.pdetail-tool-card__desc {
  font-size: 14px;
  line-height: 1.75;
  color: #5f6470;
  margin: 0;
}

.pdetail-metrics-band {
  padding: 72px 0;
  background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%);
  color: #ffffff;
}

.pdetail-metrics-grid {
  max-width: 1080px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 28px;
  text-align: center;
}

.pdetail-metric-item__val {
  display: block;
  font-size: 40px;
  font-weight: 800;
  color: #a5b4fc;
  margin-bottom: 8px;
}

.pdetail-metric-item__lbl {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
}

.pdetail-role-tabs {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 32px;
}

.pdetail-role-tab {
  padding: 10px 28px;
  border-radius: 999px;
  border: 1px solid #d0d5dd;
  background: #ffffff;
  font-size: 14.5px;
  font-weight: 600;
  color: #334155;
  cursor: pointer;
  transition: all 0.2s ease;
}

.pdetail-role-tab.is-active {
  background: #5b4be8;
  border-color: #5b4be8;
  color: #ffffff;
}

.pdetail-role-panel {
  max-width: 820px;
  margin: 0 auto;
  background: #ffffff;
  padding: 36px 44px;
  border-radius: 8px;
  box-shadow: 0 12px 32px rgba(16, 24, 40, 0.06);
}

.pdetail-role-panel__headline {
  font-size: 20px;
  font-weight: 700;
  color: #1f2329;
  margin: 0 0 18px;
}

.pdetail-role-panel__list {
  margin: 0;
  padding-left: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  font-size: 14.5px;
  line-height: 1.75;
  color: #4b5563;
}

.pdetail-case-card {
  background: #ffffff;
  border-radius: 6px;
  overflow: hidden;
  text-decoration: none;
  color: inherit;
  box-shadow: 0 8px 24px rgba(16, 24, 40, 0.05);
  display: flex;
  flex-direction: column;
}

.pdetail-case-card__media {
  aspect-ratio: 16 / 10;
  overflow: hidden;
}

.pdetail-case-card__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pdetail-case-card__body {
  padding: 20px;
}

.pdetail-case-card__cust {
  font-size: 12px;
  font-weight: 700;
  color: #5b4be8;
}

.pdetail-case-card__title {
  font-size: 16px;
  margin: 6px 0 8px;
}

.pdetail-case-card__sum {
  font-size: 13px;
  color: #6e7380;
  margin: 0;
}

.pdetail-bottom-cta {
  padding: 68px 0;
  background: #090a0f;
  color: #ffffff;
  text-align: center;
}

.pdetail-bottom-cta__inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.pdetail-bottom-cta__title {
  font-size: 28px;
  margin: 0;
}

@media (max-width: 960px) {
  .pdetail-hero__grid,
  .pdetail-hw-grid,
  .pdetail-metrics-grid {
    grid-template-columns: 1fr;
  }
}
</style>
