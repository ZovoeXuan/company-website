<!-- 对应源码路径: website/web/src/views/HomeView.vue -->
<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getHome, getSite } from '@/api/public'
import { openConsult } from '@/composables/consult'
import { usePageMeta } from '@/composables/usePageMeta'
import { vReveal } from '@/directives/reveal'
import type {
  HomeHeroItem,
  HomeHotspotItem,
  HomeLink,
  HomeMetric,
  HomeShowcaseStage,
  HomeView,
} from '@/data/types'

const router = useRouter()
const data = ref<HomeView | null>(null)
const siteName = ref('公司官网')
const heroIndex = ref(0)
const activeMetricIdx = ref(6)
const activeStageIdx = ref(0)
const activeSolutionTab = ref(0)
const radarDistance = ref(153)
const paymentStep = ref<'qr' | 'receipt'>('receipt')

let heroTimer: number | null = null
let metricTimer: number | null = null
let radarTimer: number | null = null
let paymentTimer: number | null = null

usePageMeta(() => ({
  title: '首页',
  description: '领先的产业物联网与全场景 AI 软件服务平台。',
}))

// 默认 3 屏 Hero（1:1 对齐 首页.mp4 00:00-00:08：深色紫宝盒、浅色白皮书、深色戈壁公路）
const DEFAULT_HEROES: HomeHeroItem[] = [
  {
    key: 'hero-1',
    title: '紫宝盒',
    subtitle: '物流行业首款全场景AI工具',
    theme: 'dark',
    watermark: '紫宝盒',
    desktopImage: null,
    mobileImage: null,
    buttonText: '了解详情',
    link: { type: 'PAGE', url: '/products' },
  },
  {
    key: 'hero-2',
    title: '新能源车队管理白皮书',
    subtitle: '迈向数据驱动的「成本、效率、安全」的精细化运营新时代',
    theme: 'light',
    kicker: '徐浪  G7易流安全专家、金牌讲师',
    badgeText: '免费下载',
    desktopImage: null,
    mobileImage: null,
    buttonText: '了解详情',
    link: { type: 'CONSULT' },
  },
  {
    key: 'hero-3',
    title: '以物联网技术和软件服务改变公路货运',
    subtitle: '公路货运行业物联网SaaS公司',
    theme: 'dark',
    desktopImage: null,
    mobileImage: null,
    buttonText: '了解详情',
    link: { type: 'PAGE', url: '/about' },
  },
]

const heroes = computed<HomeHeroItem[]>(() => {
  const configured = data.value?.hero?.items ?? []
  if (configured.length >= 3) {
    return configured.map((item, idx) => ({
      ...DEFAULT_HEROES[idx % DEFAULT_HEROES.length],
      ...item,
      theme: item.theme || DEFAULT_HEROES[idx % DEFAULT_HEROES.length].theme,
    }))
  }
  if (configured.length > 0) {
    const merged = configured.map((item, idx) => ({
      ...DEFAULT_HEROES[idx % DEFAULT_HEROES.length],
      ...item,
    }))
    for (let i = merged.length; i < 3; i++) {
      merged.push(DEFAULT_HEROES[i])
    }
    return merged
  }
  return DEFAULT_HEROES
})

const currentHero = computed(() => heroes.value[heroIndex.value] ?? heroes.value[0])

function selectHero(idx: number) {
  heroIndex.value = idx
  startHeroRotation()
}

function startHeroRotation() {
  if (heroTimer) clearInterval(heroTimer)
  if (heroes.value.length <= 1) return
  heroTimer = window.setInterval(() => {
    heroIndex.value = (heroIndex.value + 1) % heroes.value.length
  }, 6000)
}

// 默认 8 项 G 大数据指标（1:1 对齐 首页.mp4 00:09-00:18）
const DEFAULT_METRICS: HomeMetric[] = [
  { key: 'm1', displayValue: '800W', unit: '+', label: 'IoT设备连接', description: '' },
  { key: 'm2', displayValue: '12000', unit: '+', label: '油站连接', description: '' },
  { key: 'm3', displayValue: '18W', unit: '+', label: '安全设备', description: '' },
  { key: 'm4', displayValue: '14000', unit: '+', label: '数字货舱', description: '' },
  { key: 'm5', displayValue: '35000', unit: '+', label: '货运经营者', description: '' },
  { key: 'm6', displayValue: '1.44亿公里', unit: '+', label: '日均跟踪里程', description: '' },
  { key: 'm7', displayValue: '1亿', unit: '+', label: '日均车辆事件', description: '' },
  { key: 'm8', displayValue: '12T', unit: '+', label: '日均上传数据', description: '' },
]

const metricsList = computed<HomeMetric[]>(() => {
  const raw = data.value?.metrics?.items ?? []
  if (raw.length >= 8) return raw.slice(0, 8)
  if (raw.length > 0) {
    const out = [...raw]
    for (let i = out.length; i < 8; i++) {
      out.push(DEFAULT_METRICS[i])
    }
    return out
  }
  return DEFAULT_METRICS
})

// 11 个车身/产品 90度折线引线热点（点击可直达产品详情界面）
const DEFAULT_HOTSPOTS: HomeHotspotItem[] = [
  { label: '安全驾驶系统', path: '/products/1', x: 525, y: 175, labelX: 390, labelY: 165, align: 'right' },
  { label: 'AI 车挂匹配', path: '/products?category=IoT', x: 585, y: 195, labelX: 405, labelY: 205, align: 'right' },
  { label: '数字加注', path: '/solutions?category=交易', x: 565, y: 245, labelX: 410, labelY: 245, align: 'right' },
  { label: '油耗管理', path: '/products?category=IoT', x: 590, y: 260, labelX: 590, labelY: 75, align: 'left' },
  { label: 'AI 量方', path: '/products/1', x: 645, y: 170, labelX: 645, labelY: 95, align: 'left' },
  { label: '实时温控', path: '/products?category=冷链', x: 695, y: 160, labelX: 695, labelY: 60, align: 'left' },
  { label: '胎温胎压', path: '/products?category=安全', x: 748, y: 255, labelX: 748, labelY: 82, align: 'left' },
  { label: '轴称重', path: '/products?category=IoT', x: 785, y: 255, labelX: 785, labelY: 98, align: 'left' },
  { label: '货舱摄像头', path: '/products?category=安全', x: 842, y: 155, labelX: 842, labelY: 65, align: 'left' },
  { label: '挂车电子制动系统', path: '/products?category=安全', x: 760, y: 268, labelX: 930, labelY: 220, align: 'left' },
  { label: '倒车盲点检测', path: '/products/1', x: 852, y: 265, labelX: 930, labelY: 265, align: 'left' },
]

const hotspotsList = computed<HomeHotspotItem[]>(() => {
  if (data.value?.metrics?.hotspots && data.value.metrics.hotspots.length > 0) {
    return data.value.metrics.hotspots.map((hs, idx) => ({
      ...(DEFAULT_HOTSPOTS[idx] || {}),
      ...hs,
      path: hs.path || DEFAULT_HOTSPOTS[idx]?.path || '/products',
    }))
  }
  return DEFAULT_HOTSPOTS
})

function goToProduct(path?: string) {
  if (!path) {
    router.push('/products')
    return
  }
  if (path.startsWith('http://') || path.startsWith('https://')) {
    window.open(path, '_blank', 'noopener,noreferrer')
  } else {
    router.push(path)
  }
}

const featuredProductShortcuts = [
  {
    tag: '首款核心硬件',
    title: '紫宝盒',
    desc: '全场景智能视觉与感知车载终端，毫秒级声光预警',
    path: '/products/1',
  },
  {
    tag: '安全管车',
    title: '安全管家',
    desc: '三晒两抓主动安全管理闭环，百公里高危事故率大幅下降',
    path: '/products?category=安全',
  },
  {
    tag: '全程冷链',
    title: '冷链管家',
    desc: '厢内多温区多点实时温湿度监测，异常秒级告警',
    path: '/products?category=冷链',
  },
  {
    tag: '数字化在途',
    title: '追货管家',
    desc: '高频在途位置追踪与电子围栏，货物全程可视化透明交付',
    path: '/products?category=追货',
  },
]

// 第 3 大板块：左侧三圆点纵向切换舞台（IoT服务 / 交易服务 / 产业物流数字化，1:1 对齐 00:19-00:34）
const DEFAULT_STAGES: HomeShowcaseStage[] = [
  {
    key: 'iot',
    title: 'IoT服务',
    description:
      '基于物联网感知技术，打造软硬一体化的智能管车服务，全面打通车辆、司机、货物的全链路数字化管理，助力车队安全降本、高效履约。',
    morePath: '/products',
    mode: 'RADAR',
    alertLabel: '车距过近',
    alertBaseValue: 153,
    alertUnit: 'm',
    subItems: [
      { prefix: 'G', title: '安全管家', path: '/products?category=安全' },
      { prefix: 'G', title: '冷链管家', path: '/products?category=冷链' },
      { prefix: 'G', title: '流向管家', path: '/products?category=流向' },
      { prefix: 'G', title: '追货管家', path: '/products?category=追货' },
    ],
  },
  {
    key: 'trade',
    title: '交易服务',
    description:
      '以物联网与大数据技术重构物流交易环节，围绕能源补给、运力结算、数字货舱与物联保险，让每一笔车队交易透明、合规、省心。',
    morePath: '/solutions',
    mode: 'PAYMENT',
    paymentTitle: '扫码加油',
    paymentSubtitle: '订单支付',
    paymentAmount: '¥202',
    subItems: [
      { prefix: 'G', title: '财运通', path: '/solutions?category=交易' },
      { prefix: 'G', title: '数字货舱', path: '/products' },
      { prefix: 'G', title: '物联保险', path: '/solutions' },
      { prefix: 'G', title: '数字能源', path: '/solutions?category=能源' },
    ],
  },
  {
    key: 'industry',
    title: '产业物流数字化',
    description:
      '基于物联网与大数据技术，重塑传统行业物流运输模式，打造贯穿全链条端到端的数字化解决方案，助力行业伙伴获得持续增长。',
    morePath: '/solutions',
    mode: 'SOLUTIONS',
    subItems: [
      {
        title: '履约可视',
        path: '/solutions/1',
        callouts: [
          { label: 'RFID读头', x: 24, y: 18 },
          { label: 'RFID标签', x: 74, y: 31 },
          { label: 'RFID标签', x: 64, y: 46 },
          { label: '挂车专用主机', x: 26, y: 74 },
        ],
      },
      {
        title: '货运安全',
        path: '/solutions/2',
        callouts: [
          { label: 'AI双目防碰撞', x: 26, y: 22 },
          { label: 'DMS疲劳监测', x: 72, y: 28 },
          { label: '盲区生命探测', x: 66, y: 54 },
          { label: '7×24安全托管', x: 28, y: 72 },
        ],
      },
      {
        title: '运力降本',
        path: '/solutions/3',
        callouts: [
          { label: '智能配载算法', x: 25, y: 20 },
          { label: '数字加注防滴漏', x: 72, y: 32 },
          { label: 'ETC路径寻优', x: 65, y: 52 },
          { label: '自动对账结算', x: 28, y: 74 },
        ],
      },
      {
        title: '新能源减碳',
        path: '/solutions/1',
        callouts: [
          { label: '智能充换电调度', x: 24, y: 20 },
          { label: '电池SOH热管理', x: 73, y: 30 },
          { label: '单车碳足迹台账', x: 66, y: 52 },
          { label: '绿电干线组网', x: 27, y: 74 },
        ],
      },
    ],
  },
]

const showcaseStages = computed<HomeShowcaseStage[]>(() => {
  if (data.value?.showcaseStages && data.value.showcaseStages.length > 0) {
    return data.value.showcaseStages
  }
  return DEFAULT_STAGES
})

const currentStage = computed(
  () => showcaseStages.value[activeStageIdx.value] ?? showcaseStages.value[0],
)

const currentSolutionSub = computed(() => {
  const subs = currentStage.value?.subItems ?? []
  return subs[activeSolutionTab.value] ?? subs[0]
})

function handleAction(link: HomeLink | null | undefined, sourcePosition = 'HOME_HERO') {
  if (!link) return
  if (link.type === 'CONSULT') {
    openConsult({
      sourcePosition,
      contentType: link.contentType as 'PRODUCT' | 'SOLUTION' | 'CASE' | undefined,
      contentId: link.contentId,
      title: '预约产品展示',
    })
    return
  }
  if (link.url) {
    if (link.url.startsWith('http://') || link.url.startsWith('https://')) {
      window.open(link.url, '_blank', 'noopener,noreferrer')
    } else {
      router.push(link.url)
    }
  }
}

let wheelCooldown = false
function onStageWheel(e: WheelEvent) {
  if (wheelCooldown) return
  if (Math.abs(e.deltaY) < 28) return
  if (e.deltaY > 0 && activeStageIdx.value < showcaseStages.value.length - 1) {
    e.preventDefault()
    activeStageIdx.value += 1
    wheelCooldown = true
    window.setTimeout(() => {
      wheelCooldown = false
    }, 650)
  } else if (e.deltaY < 0 && activeStageIdx.value > 0) {
    e.preventDefault()
    activeStageIdx.value -= 1
    wheelCooldown = true
    window.setTimeout(() => {
      wheelCooldown = false
    }, 650)
  }
}

async function load() {
  try {
    const [homeRes, siteRes] = await Promise.all([
      getHome().catch(() => null),
      getSite().catch(() => null),
    ])
    if (homeRes) data.value = homeRes
    if (siteRes?.siteName) siteName.value = siteRes.siteName
  } finally {
    startHeroRotation()
  }
}

onMounted(() => {
  load()
  metricTimer = window.setInterval(() => {
    activeMetricIdx.value = (activeMetricIdx.value + 1) % 8
  }, 2800)
  const distances = [113, 119, 138, 153, 160]
  let dIdx = 3
  radarTimer = window.setInterval(() => {
    dIdx = (dIdx + 1) % distances.length
    radarDistance.value = distances[dIdx]
  }, 1100)
  paymentTimer = window.setInterval(() => {
    paymentStep.value = paymentStep.value === 'qr' ? 'receipt' : 'qr'
  }, 2600)
})

onBeforeUnmount(() => {
  if (heroTimer) clearInterval(heroTimer)
  if (metricTimer) clearInterval(metricTimer)
  if (radarTimer) clearInterval(radarTimer)
  if (paymentTimer) clearInterval(paymentTimer)
})
</script>

<template>
  <div class="g7-home">
    <!-- ==================== 1. 首屏全屏 3 屏轮播（1:1 还原 首页.mp4 00:00-00:08） ==================== -->
    <section
      class="g7-hero"
      :class="[
        `g7-hero--${currentHero.theme || 'dark'}`,
        `g7-hero--slide-${heroIndex % 3}`,
      ]"
    >
      <!-- 背景层：支持后台上传的视频/图片，或默认高精度还原的 3 屏视觉舞台 -->
      <div class="g7-hero__bg-layer">
        <video
          v-if="currentHero.desktopImage?.type === 'VIDEO'"
          :key="currentHero.desktopImage.url"
          :src="currentHero.desktopImage.url"
          class="g7-hero__media-el"
          autoplay
          muted
          loop
          playsinline
        ></video>
        <img
          v-else-if="currentHero.desktopImage?.url"
          :src="currentHero.desktopImage.url"
          :alt="currentHero.desktopImage.alt || currentHero.title"
          class="g7-hero__media-el"
        />

        <!-- 默认第 1 屏视觉：紫宝盒暗黑光束与 3D 硬件魔盒 -->
        <div v-else-if="heroIndex % 3 === 0" class="hero-stage-1" aria-hidden="true">
          <div class="hero-stage-1__watermark">{{ currentHero.watermark || currentHero.title }}</div>
          <div class="hero-stage-1__beam"></div>
          <div class="hero-stage-1__cube">
            <div class="cube-top">
              <span class="cube-g">G7</span>
            </div>
            <div class="cube-front"></div>
            <div class="cube-glow"></div>
          </div>
        </div>

        <!-- 默认第 2 屏视觉：晴空白皮书与新能源重卡编队 -->
        <div v-else-if="heroIndex % 3 === 1" class="hero-stage-2" aria-hidden="true">
          <div class="hero-stage-2__clouds"></div>
          <div class="hero-stage-2__visual">
            <div class="whitepaper-book">
              <span class="whitepaper-book__tag">G7易流 · 2026</span>
              <strong>新能源车队<br />管理白皮书</strong>
            </div>
            <div class="ev-truck-silhouette"></div>
          </div>
        </div>

        <!-- 默认第 3 屏视觉：戈壁公路俯瞰与紫色领航重卡 -->
        <div v-else class="hero-stage-3" aria-hidden="true">
          <div class="desert-highway">
            <div class="desert-road">
              <div class="desert-truck">
                <span class="desert-truck__cab"></span>
                <span class="desert-truck__trailer">G7</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 前景文案与按钮（左对齐，随轮播切换平滑上浮） -->
      <div class="container container--wide g7-hero__inner">
        <Transition name="hero-fade" mode="out-in">
          <div :key="currentHero.key || heroIndex" class="g7-hero__text-box">
            <h1
              class="g7-hero__title"
              :style="{ color: currentHero.titleColor || (currentHero.theme === 'light' ? undefined : '#ffffff') }"
            >
              {{ currentHero.title }}
            </h1>
            <p
              v-if="currentHero.subtitle"
              class="g7-hero__sub"
              :style="{ color: currentHero.subtitleColor || (currentHero.theme === 'light' ? '#334155' : 'rgba(255, 255, 255, 0.92)') }"
            >
              {{ currentHero.subtitle }}
            </p>
            <p
              v-if="currentHero.kicker"
              class="g7-hero__kicker"
              :style="{ color: currentHero.kickerColor }"
            >
              {{ currentHero.kicker }}
            </p>
            <div v-if="currentHero.badgeText" class="g7-hero__badge">{{ currentHero.badgeText }}</div>

            <div v-if="currentHero.buttonText" class="g7-hero__actions">
              <button
                type="button"
                class="g7-hero__btn"
                :style="{
                  backgroundColor: currentHero.buttonBgColor,
                  color: currentHero.buttonTextColor
                }"
                @click="handleAction(currentHero.link, 'HOME_HERO')"
              >
                {{ currentHero.buttonText }}
              </button>
            </div>
          </div>
        </Transition>

        <!-- 左下角横向进度指示条（已按需求去除播放暂停按钮） -->
        <div v-if="heroes.length > 1" class="g7-hero__bars" role="tablist" aria-label="首屏轮播切换">
          <button
            v-for="(h, idx) in heroes"
            :key="h.key || idx"
            type="button"
            role="tab"
            class="g7-hero__bar"
            :class="{ 'is-active': idx === heroIndex }"
            :aria-selected="idx === heroIndex"
            :aria-label="`切换到第 ${idx + 1} 屏`"
            @click="selectHero(idx)"
          >
            <span class="g7-hero__bar-fill"></span>
          </button>
        </div>
      </div>
    </section>

    <!-- ==================== 2. 产品展示：8 项核心指标 + 声波圆环与 11 大产品节点跳转 + 核心产品快捷矩阵 ==================== -->
    <section id="metrics" class="g7-bigdata">
      <div class="container container--wide">
        <h2 v-reveal class="g7-bigdata__title">
          <em v-if="data?.metrics?.brandPrefix" class="g7-bigdata__g">{{ data.metrics.brandPrefix }}</em>
          <span>{{ data?.metrics?.title || '产品展示' }}</span>
        </h2>
        <p v-if="data?.metrics?.subtitle" class="g7-bigdata__sub-lead">{{ data.metrics.subtitle }}</p>

        <!-- 2 行 × 4 列 = 8 项核心指标，右上角上标 +，轮流/悬停紫色高亮 -->
        <ul class="g7-metrics-grid">
          <li
            v-for="(item, mIdx) in metricsList"
            :key="item.key || mIdx"
            v-reveal="mIdx * 60"
            class="g7-metric-cell"
            :class="{ 'is-highlighted': activeMetricIdx === mIdx }"
            @mouseenter="activeMetricIdx = mIdx"
          >
            <div class="g7-metric-cell__num">
              <span>{{ item.displayValue }}</span>
              <sup v-if="item.unit">{{ item.unit }}</sup>
            </div>
            <p class="g7-metric-cell__label">{{ item.label }}</p>
          </li>
        </ul>

        <!-- 底部声波雷达同心圆环 + 透视车体/产品图 + 11 条 90 度折线引线标签（点击直达对应产品详细界面） -->
        <div v-reveal="180" class="g7-diagram-stage">
          <!-- 地面扩散点阵声波环 -->
          <div class="g7-soundwaves" aria-hidden="true">
            <span class="wave-ring wave-ring--1"></span>
            <span class="wave-ring wave-ring--2"></span>
            <span class="wave-ring wave-ring--3"></span>
            <span class="wave-ring wave-ring--4"></span>
          </div>

          <!-- 居中可交互 SVG 透视车体与 90度折线引线标注（点击跳转到详细产品界面） -->
          <svg
            viewBox="0 0 1120 340"
            class="g7-diagram-svg"
            role="img"
            aria-label="全场景智能感知硬件与产品数据节点示意图"
          >
            <defs>
              <linearGradient id="truckBodyGrad" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" stop-color="#f8fafc" />
                <stop offset="100%" stop-color="#e2e8f0" />
              </linearGradient>
              <linearGradient id="cabGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stop-color="#ffffff" />
                <stop offset="100%" stop-color="#cbd5e1" />
              </linearGradient>
            </defs>

            <!-- 地面椭圆波纹线 -->
            <ellipse cx="640" cy="292" rx="440" ry="34" fill="none" stroke="#c7d2fe" stroke-width="1" stroke-dasharray="3 4" />
            <ellipse cx="640" cy="292" rx="340" ry="24" fill="none" stroke="#a5b4fc" stroke-width="1" stroke-dasharray="2 3" />
            <ellipse cx="640" cy="292" rx="230" ry="15" fill="none" stroke="#818cf8" stroke-width="1" stroke-dasharray="2 2" />

            <!-- 车头与挂车高精矢量建模 -->
            <g class="truck-model">
              <!-- 货厢主体 -->
              <rect x="615" y="140" width="245" height="106" rx="3" fill="url(#truckBodyGrad)" stroke="#cbd5e1" stroke-width="1.5" />
              <text x="735" y="205" text-anchor="middle" fill="#4c2ce6" font-size="32" font-weight="900" font-style="italic">G</text>
              <!-- 车头驾驶室 -->
              <path d="M510 246 L522 162 Q530 142 560 140 L608 140 L608 246 Z" fill="url(#cabGrad)" stroke="#94a3b8" stroke-width="1.5" />
              <!-- 挡风玻璃 -->
              <path d="M518 196 L527 166 Q532 158 548 158 L565 158 L565 196 Z" fill="#1e293b" />
              <!-- 底盘与护栏 -->
              <rect x="515" y="246" width="340" height="14" rx="2" fill="#334155" />
              <!-- 车轮组 -->
              <g v-for="wx in [548, 608, 748, 784, 820]" :key="wx">
                <circle :cx="wx" cy="264" r="17" fill="#1e293b" />
                <circle :cx="wx" cy="264" r="9" fill="#cbd5e1" />
                <circle :cx="wx" cy="264" r="4" fill="#475569" />
              </g>
              <!-- 前方智能加注机立柱 -->
              <rect x="642" y="182" width="38" height="92" rx="2" fill="#e2e8f0" stroke="#94a3b8" />
              <rect x="642" y="182" width="38" height="16" fill="#4c2ce6" />
              <text x="654" y="194" fill="#fff" font-size="10" font-weight="900" font-style="italic">G</text>
            </g>

            <!-- 11 条 90 度折线引线 + 机身菱形闪烁节点 ◈ + 文本标签（支持点击跳转到对应产品详情界面） -->
            <g
              v-for="(hs, hIdx) in hotspotsList"
              :key="hs.label + hIdx"
              class="hotspot-node"
              role="button"
              tabindex="0"
              :aria-label="`查看产品详情：${hs.label}`"
              @click="goToProduct(hs.path || '/products')"
              @keydown.enter="goToProduct(hs.path || '/products')"
            >
              <!-- 90度折线：从机身 (x, y) 垂直/水平折向 (labelX, labelY) -->
              <polyline
                :points="
                  hs.labelY < 130
                    ? `${hs.x},${hs.y} ${hs.x},${hs.labelY} ${hs.labelX + (hs.align === 'right' ? -10 : 18)},${hs.labelY}`
                    : `${hs.x},${hs.y} ${hs.labelX},${hs.y} ${hs.labelX},${hs.labelY}`
                "
                class="hotspot-line"
                fill="none"
              />
              <!-- 机身菱形锚点 ◈ -->
              <polygon
                :points="`${hs.x},${hs.y - 5} ${hs.x + 5},${hs.y} ${hs.x},${hs.y + 5} ${hs.x - 5},${hs.y}`"
                class="hotspot-diamond"
              />
              <!-- 标签文字与跳转箭头 -->
              <text
                :x="hs.align === 'right' ? hs.labelX - 6 : hs.labelX"
                :y="hs.labelY - 6"
                :text-anchor="hs.align === 'right' ? 'end' : 'start'"
                class="hotspot-label-text"
              >
                {{ hs.label }}
                <tspan class="hotspot-arrow"> →</tspan>
              </text>
            </g>
          </svg>
        </div>

        <!-- 核心产品展示快捷矩阵：支持直接点击跳转详细界面 -->
        <div v-reveal="240" class="g7-products-quick-grid">
          <div
            v-for="p in featuredProductShortcuts"
            :key="p.title"
            class="product-shortcut-card"
            @click="goToProduct(p.path)"
          >
            <div class="product-shortcut-card__tag">{{ p.tag }}</div>
            <h3 class="product-shortcut-card__title">{{ p.title }}</h3>
            <p class="product-shortcut-card__desc">{{ p.desc }}</p>
            <div class="product-shortcut-card__footer">
              <span class="product-shortcut-card__link">查看产品详情 →</span>
            </div>
          </div>
        </div>

        <div v-reveal="280" class="g7-products-more-action">
          <RouterLink to="/products" class="g7-products-more-btn">
            浏览全部产品与解决方案
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M5 12h14M12 5l7 7-7 7" />
            </svg>
          </RouterLink>
        </div>
      </div>
    </section>

    <!-- ==================== 3. 左侧三圆点纵向切换舞台：IoT服务 / 交易服务 / 产业物流数字化（1:1 还原 00:19-00:34） ==================== -->
    <section
      id="services"
      class="g7-showcase"
      @wheel="onStageWheel"
    >
      <div class="container container--wide g7-showcase__inner">
        <!-- 最左侧 3 个纵向圆点指示器 -->
        <div class="g7-showcase__dots" role="tablist" aria-label="核心业务板块切换">
          <button
            v-for="(st, sIdx) in showcaseStages"
            :key="st.key"
            type="button"
            role="tab"
            class="stage-dot"
            :class="{ 'is-active': sIdx === activeStageIdx }"
            :aria-selected="sIdx === activeStageIdx"
            :title="st.title"
            @click="activeStageIdx = sIdx"
          >
            <span class="stage-dot__core"></span>
          </button>
        </div>

        <!-- 左侧文案与子项切换区 -->
        <Transition name="stage-fade" mode="out-in">
          <div :key="currentStage.key" class="g7-showcase__left">
            <h2 class="g7-showcase__title">{{ currentStage.title }}</h2>
            <p class="g7-showcase__desc">{{ currentStage.description }}</p>

            <div v-if="currentStage.mode !== 'SOLUTIONS'" class="g7-showcase__more-wrap">
              <RouterLink :to="currentStage.morePath || '/products'" class="g7-outline-btn">
                了解更多
              </RouterLink>
            </div>

            <!-- 模式 A/B (IoT服务 / 交易服务)：底部 2×2 带斜体 G 前缀产品矩阵 -->
            <div
              v-if="currentStage.mode !== 'SOLUTIONS'"
              class="g7-sub-matrix"
            >
              <RouterLink
                v-for="sub in currentStage.subItems"
                :key="sub.title"
                :to="sub.path || '/products'"
                class="g7-sub-matrix__item"
              >
                <em class="g7-sub-matrix__prefix">{{ sub.prefix || 'G' }}</em>
                <span>{{ sub.title }}</span>
              </RouterLink>
            </div>

            <!-- 模式 C (产业物流数字化)：底部 4 个纵向可点击切换条（履约可视 / 货运安全 / 运力降本 / 新能源减碳） -->
            <div v-else class="g7-sol-tabs">
              <button
                v-for="(sub, tIdx) in currentStage.subItems"
                :key="sub.title"
                type="button"
                class="g7-sol-tab"
                :class="{ 'is-active': activeSolutionTab === tIdx }"
                @mouseenter="activeSolutionTab = tIdx"
                @click="activeSolutionTab = tIdx"
              >
                {{ sub.title }}
              </button>
            </div>
          </div>
        </Transition>

        <!-- 右侧动态交互立体场景区（1:1 还原雷达测距跳数、扫码加油小票、集装箱正面吊 RFID 标签弹出） -->
        <Transition name="stage-fade" mode="out-in">
          <div :key="`${currentStage.key}-${activeSolutionTab}`" class="g7-showcase__right">
            <!-- 场景 1：IoT服务（俯视车辆 + 紫蓝雷达扇面 + 车距过近动态跳字预警卡） -->
            <div v-if="currentStage.mode === 'RADAR'" class="scene-radar">
              <div class="scene-radar__beam scene-radar__beam--front"></div>
              <div class="scene-radar__beam scene-radar__beam--side-l"></div>
              <div class="scene-radar__beam scene-radar__beam--side-r"></div>

              <!-- 悬浮实时跳字预警气泡（1:1 对齐 home_21.png：车距过近 153m ⚠️） -->
              <div class="radar-alert-card">
                <div class="radar-alert-card__text">
                  <span class="radar-alert-card__lbl">{{ currentStage.alertLabel || '车距过近' }}</span>
                  <strong class="radar-alert-card__val">
                    {{ radarDistance }}{{ currentStage.alertUnit || 'm' }}
                  </strong>
                </div>
                <span class="radar-alert-card__icon" aria-hidden="true">⚠️</span>
              </div>

              <!-- 俯视重卡建模 -->
              <div class="topdown-truck">
                <div class="topdown-truck__cab"></div>
                <div class="topdown-truck__trailer">
                  <span>G</span>
                </div>
              </div>
            </div>

            <!-- 场景 2：交易服务（加油/充电桩场景 + 扫码加油 ¥202 账单小票动态弹出，1:1 对齐 home_25.png） -->
            <div v-else-if="currentStage.mode === 'PAYMENT'" class="scene-payment">
              <div class="payment-popup" :class="`is-${paymentStep}`">
                <div class="payment-popup__head">
                  <span class="payment-popup__dot"></span>
                  <span>{{ currentStage.paymentTitle || '扫码加油' }}</span>
                </div>
                <div class="payment-popup__body">
                  <span class="payment-popup__sub">{{ currentStage.paymentSubtitle || '订单支付' }}</span>
                  <strong class="payment-popup__amt">{{ currentStage.paymentAmount || '¥202' }}</strong>
                </div>
              </div>

              <div class="station-isotruck">
                <div class="station-isotruck__pumps">
                  <span class="pump-box">G</span>
                  <span class="pump-box">G</span>
                </div>
                <div class="station-isotruck__vehicle">
                  <span class="iso-trailer">G</span>
                  <span class="iso-cab"></span>
                </div>
              </div>
            </div>

            <!-- 场景 3：产业物流数字化（等轴测集装箱正面吊装 + 4 个带引线胶囊标签弹出，1:1 对齐 home_32.png） -->
            <div v-else class="scene-solutions">
              <div class="iso-road-lines" aria-hidden="true"></div>

              <!-- 集装箱堆场与正面吊装机 -->
              <div class="iso-yard">
                <div class="iso-containers">
                  <div class="iso-box">G</div>
                  <div class="iso-box">G</div>
                  <div class="iso-box">G</div>
                </div>
                <div class="iso-crane">
                  <div class="iso-crane__arm"></div>
                  <div class="iso-crane__body"></div>
                </div>
                <div class="iso-truck-load">
                  <div class="iso-truck-load__box">G</div>
                  <div class="iso-truck-load__cab"></div>
                </div>
              </div>

              <!-- 4 个动态弹出的引线胶囊标签（RFID读头 / RFID标签 / 挂车专用主机） -->
              <div
                v-for="(cItem, cIdx) in currentSolutionSub?.callouts"
                :key="`${cItem.label}-${cIdx}`"
                class="iso-callout"
                :style="{ left: `${cItem.x}%`, top: `${cItem.y}%`, animationDelay: `${cIdx * 90}ms` }"
              >
                <span class="iso-callout__pill">{{ cItem.label }}</span>
                <span class="iso-callout__line"></span>
              </div>
            </div>
          </div>
        </Transition>
      </div>
    </section>
  </div>
</template>

<style scoped>
.g7-home {
  background: #ffffff;
  color: #1f2329;
}

/* ==================== 1. 首屏 3 屏轮播 ==================== */
.g7-hero {
  position: relative;
  height: calc(100dvh - var(--header-height, 74px));
  min-height: 580px;
  max-height: 860px;
  display: flex;
  align-items: center;
  overflow: hidden;
  background: #0b0c10;
  color: #ffffff;
}

.g7-hero--light {
  background: linear-gradient(180deg, #b8d8f8 0%, #e2effc 52%, #f4f7fb 100%);
  color: #1f2329;
}

.g7-hero__bg-layer {
  position: absolute;
  inset: 0;
  z-index: 1;
}

.g7-hero__media-el {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 第 1 屏：紫宝盒暗黑舞台 */
.hero-stage-1 {
  position: relative;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 68% 54%, #2e1a68 0%, #121124 45%, #090a0f 100%);
}

.hero-stage-1__watermark {
  position: absolute;
  right: 8%;
  top: 14%;
  font-size: clamp(110px, 15vw, 210px);
  font-weight: 900;
  color: rgba(255, 255, 255, 0.05);
  letter-spacing: 0.06em;
  filter: blur(2px);
  user-select: none;
}

.hero-stage-1__beam {
  position: absolute;
  right: 12%;
  bottom: 8%;
  width: 520px;
  height: 220px;
  background: radial-gradient(ellipse at center, rgba(124, 58, 237, 0.48) 0%, rgba(56, 189, 248, 0.18) 45%, transparent 72%);
  transform: rotate(-12deg);
  filter: blur(12px);
}

.hero-stage-1__cube {
  position: absolute;
  right: 20%;
  top: 40%;
  width: 240px;
  height: 170px;
  transform: perspective(800px) rotateX(24deg) rotateY(-28deg);
}

.cube-top {
  width: 100%;
  height: 110px;
  border-radius: 18px;
  background: linear-gradient(135deg, #2e1065 0%, #4c1d95 50%, #1e1b4b 100%);
  border: 2px solid rgba(167, 139, 250, 0.65);
  box-shadow: 0 20px 50px rgba(124, 58, 237, 0.55), inset 0 0 24px rgba(167, 139, 250, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
}

.cube-g {
  font-size: 40px;
  font-weight: 900;
  font-style: italic;
  color: #e9d5ff;
  text-shadow: 0 0 18px #a855f7;
}

/* 第 2 屏：晴空白皮书舞台 */
.hero-stage-2 {
  position: relative;
  width: 100%;
  height: 100%;
  background: linear-gradient(165deg, #9ec9f5 0%, #d8ebff 48%, #eef4fa 100%);
}

.hero-stage-2__visual {
  position: absolute;
  right: 10%;
  bottom: 14%;
  display: flex;
  align-items: flex-end;
  gap: 28px;
}

.whitepaper-book {
  width: 220px;
  height: 290px;
  border-radius: 8px;
  background: linear-gradient(145deg, #ffffff 0%, #f1f5f9 100%);
  box-shadow: -20px 24px 50px rgba(15, 23, 42, 0.18);
  padding: 28px 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  transform: rotate(-6deg);
  border-left: 8px solid #0284c7;
}

.whitepaper-book__tag {
  font-size: 12px;
  font-weight: 700;
  color: #0284c7;
}

.whitepaper-book strong {
  font-size: 24px;
  line-height: 1.35;
  color: #0f172a;
}

/* 第 3 屏：戈壁公路俯瞰舞台 */
.hero-stage-3 {
  position: relative;
  width: 100%;
  height: 100%;
  background:
    radial-gradient(circle at 25% 60%, rgba(45, 30, 20, 0.55) 0%, transparent 55%),
    linear-gradient(115deg, #9a6b4c 0%, #b58360 42%, #7c5338 78%, #3d281a 100%);
}

.desert-highway {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.desert-road {
  position: absolute;
  left: 48%;
  top: -15%;
  width: 150px;
  height: 135%;
  background: #33363d;
  border-left: 6px solid #d6b899;
  border-right: 6px solid #d6b899;
  transform: rotate(-24deg);
  box-shadow: 0 0 60px rgba(0, 0, 0, 0.45);
}

.desert-road::after {
  content: '';
  position: absolute;
  left: 50%;
  top: 0;
  bottom: 0;
  width: 2px;
  background: rgba(245, 158, 11, 0.55);
}

.desert-truck {
  position: absolute;
  left: 22px;
  top: 46%;
  width: 44px;
  height: 150px;
  display: flex;
  flex-direction: column;
  filter: drop-shadow(12px 16px 14px rgba(0, 0, 0, 0.6));
}

.desert-truck__cab {
  height: 28px;
  background: #e2e8f0;
  border-radius: 6px 6px 2px 2px;
}

.desert-truck__trailer {
  flex: 1;
  background: linear-gradient(180deg, #312e81 0%, #4c1d95 100%);
  border-radius: 2px;
  color: rgba(255, 255, 255, 0.85);
  font-size: 13px;
  font-weight: 900;
  font-style: italic;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 12px;
}

.g7-hero__inner {
  position: relative;
  z-index: 3;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.g7-hero__text-box {
  max-width: 680px;
  padding-left: 4%;
}

.g7-hero__title {
  font-size: clamp(36px, 4.4vw, 56px);
  font-weight: 700;
  line-height: 1.18;
  margin: 0;
  letter-spacing: -0.02em;
}

.g7-hero--dark .g7-hero__title {
  color: #ffffff;
}

.g7-hero--light .g7-hero__title {
  background: linear-gradient(90deg, #0284c7 0%, #2563eb 55%, #0d9488 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.g7-hero__sub {
  margin: 18px 0 0;
  font-size: clamp(16px, 1.5vw, 20px);
  color: rgba(255, 255, 255, 0.88);
  line-height: 1.65;
}

.g7-hero--light .g7-hero__sub {
  color: #334155;
}

.g7-hero__kicker {
  margin: 14px 0 0;
  font-size: 13.5px;
  color: #475569;
}

.g7-hero__badge {
  margin-top: 22px;
  font-size: 30px;
  font-weight: 800;
  color: #ffffff;
  text-shadow: 0 4px 16px rgba(2, 132, 199, 0.28);
}

.g7-hero__actions {
  margin-top: 34px;
}

.g7-hero__btn {
  min-width: 128px;
  height: 42px;
  padding: 0 28px;
  border: none;
  border-radius: 2px;
  background: #e5e7eb;
  color: #1f2329;
  font-size: 13.5px;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.g7-hero__btn:hover {
  background: #ffffff;
  color: #5b4be8;
}

/* 左下角 3 根横向进度指示条 */
.g7-hero__bars {
  position: absolute;
  left: calc(4% + 24px);
  bottom: 48px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.g7-hero__bar {
  width: 44px;
  height: 3px;
  border: none;
  padding: 0;
  background: rgba(255, 255, 255, 0.32);
  cursor: pointer;
  overflow: hidden;
  position: relative;
}

.g7-hero--light .g7-hero__bar {
  background: rgba(15, 23, 42, 0.2);
}

.g7-hero__bar-fill {
  display: block;
  height: 100%;
  width: 0%;
  background: #ffffff;
}

.g7-hero--light .g7-hero__bar-fill {
  background: #0284c7;
}

.g7-hero__bar.is-active .g7-hero__bar-fill {
  width: 100%;
  animation: bar-progress 6s linear forwards;
}

@keyframes bar-progress {
  from {
    width: 0%;
  }
  to {
    width: 100%;
  }
}

.hero-fade-enter-active,
.hero-fade-leave-active {
  transition: opacity 0.35s ease, transform 0.35s ease;
}

.hero-fade-enter-from,
.hero-fade-leave-to {
  opacity: 0;
  transform: translateY(14px);
}

/* ==================== 2. G 大数据 ==================== */
.g7-bigdata {
  padding: 96px 0 64px;
  background: #ffffff;
  overflow: hidden;
}

.g7-bigdata__title {
  text-align: center;
  font-size: 36px;
  font-weight: 800;
  color: #1f2329;
  margin: 0 0 56px;
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 8px;
}

.g7-bigdata__g {
  font-style: italic;
  font-weight: 900;
  font-size: 40px;
}

.g7-metrics-grid {
  max-width: 980px;
  margin: 0 auto 36px;
  padding: 0;
  list-style: none;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 44px 32px;
}

.g7-metric-cell {
  cursor: pointer;
  transition: color 0.25s ease;
}

.g7-metric-cell__num {
  font-size: 32px;
  font-weight: 700;
  color: #2b2f38;
  line-height: 1.1;
  transition: color 0.25s ease;
}

.g7-metric-cell__num sup {
  font-size: 16px;
  font-weight: 700;
  margin-left: 2px;
  top: -0.6em;
}

.g7-metric-cell__label {
  margin: 10px 0 0;
  font-size: 13.5px;
  color: #6e7380;
  transition: color 0.25s ease;
}

.g7-metric-cell.is-highlighted .g7-metric-cell__num,
.g7-metric-cell.is-highlighted .g7-metric-cell__label {
  color: #5b4be8;
}

.g7-diagram-stage {
  position: relative;
  max-width: 1120px;
  margin: 0 auto;
}

.g7-diagram-svg {
  width: 100%;
  height: auto;
  display: block;
}

/* 11 个车身硬件/产品节点交互样式 */
.hotspot-node {
  cursor: pointer;
  transition: all 0.25s ease;
  outline: none;
}

.hotspot-line {
  stroke: #94a3b8;
  stroke-width: 1;
  transition: stroke 0.25s ease, stroke-width 0.25s ease;
}

.hotspot-diamond {
  fill: #ffffff;
  stroke: #4c2ce6;
  stroke-width: 1.8;
  transition: all 0.25s ease;
}

.hotspot-label-text {
  fill: #334155;
  font-size: 12.5px;
  font-weight: 500;
  transition: all 0.25s ease;
  user-select: none;
}

.hotspot-arrow {
  fill: #4c2ce6;
  opacity: 0;
  transition: all 0.25s ease;
  font-weight: 700;
}

.hotspot-node:hover .hotspot-line,
.hotspot-node:focus-visible .hotspot-line {
  stroke: #4c2ce6;
  stroke-width: 1.8;
}

.hotspot-node:hover .hotspot-diamond,
.hotspot-node:focus-visible .hotspot-diamond {
  fill: #4c2ce6;
  stroke: #7c3aed;
  filter: drop-shadow(0 0 6px rgba(76, 44, 230, 0.6));
}

.hotspot-node:hover .hotspot-label-text,
.hotspot-node:focus-visible .hotspot-label-text {
  fill: #4c2ce6;
  font-weight: 700;
}

.hotspot-node:hover .hotspot-arrow,
.hotspot-node:focus-visible .hotspot-arrow {
  opacity: 1;
}

/* 核心产品展示快捷矩阵 */
.g7-products-quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-top: 48px;
}

.product-shortcut-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px 20px;
  box-shadow: 0 4px 18px rgba(15, 23, 42, 0.04);
  border: 1px solid rgba(226, 232, 240, 0.8);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  display: flex;
  flex-direction: column;
}

.product-shortcut-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 28px rgba(76, 44, 230, 0.12);
  border-color: rgba(91, 75, 232, 0.3);
}

.product-shortcut-card__tag {
  display: inline-block;
  align-self: flex-start;
  font-size: 11px;
  font-weight: 600;
  color: #4c2ce6;
  background: #f0edff;
  padding: 2px 8px;
  border-radius: 4px;
  margin-bottom: 12px;
}

.product-shortcut-card__title {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px;
}

.product-shortcut-card__desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
  margin: 0 0 16px;
  flex: 1;
}

.product-shortcut-card__footer {
  margin-top: auto;
}

.product-shortcut-card__link {
  font-size: 13px;
  font-weight: 600;
  color: #4c2ce6;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: gap 0.2s ease;
}

.product-shortcut-card:hover .product-shortcut-card__link {
  gap: 8px;
}

.g7-products-more-action {
  text-align: center;
  margin-top: 36px;
}

.g7-products-more-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  border-radius: 999px;
  background: #ffffff;
  color: #4c2ce6;
  font-weight: 600;
  font-size: 14px;
  border: 1.5px solid #4c2ce6;
  text-decoration: none;
  transition: all 0.25s ease;
  box-shadow: 0 2px 8px rgba(76, 44, 230, 0.08);
}

.g7-products-more-btn:hover {
  background: #4c2ce6;
  color: #ffffff;
  box-shadow: 0 6px 20px rgba(76, 44, 230, 0.25);
  transform: translateY(-2px);
}

@media (max-width: 900px) {
  .g7-products-quick-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 580px) {
  .g7-products-quick-grid {
    grid-template-columns: 1fr;
  }
}

/* ==================== 3. 左侧三圆点纵向切换舞台 ==================== */
.g7-showcase {
  padding: 96px 0 110px;
  background: linear-gradient(180deg, #f5f7fb 0%, #eef1f7 100%);
  min-height: 640px;
  display: flex;
  align-items: center;
}

.g7-showcase__inner {
  max-width: 1160px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 36px 400px 1fr;
  gap: 36px;
  align-items: center;
}

.g7-showcase__dots {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 22px;
}

.stage-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 1px solid transparent;
  background: none;
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.stage-dot__core {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #cbd5e1;
  transition: all 0.2s ease;
}

.stage-dot.is-active {
  border-color: rgba(91, 75, 232, 0.35);
  background: rgba(91, 75, 232, 0.1);
}

.stage-dot.is-active .stage-dot__core {
  width: 8px;
  height: 8px;
  background: #5b4be8;
}

.g7-showcase__title {
  font-size: 38px;
  font-weight: 700;
  color: #1f2329;
  margin: 0 0 20px;
}

.g7-showcase__desc {
  font-size: 14px;
  line-height: 1.9;
  color: #6e7380;
  margin: 0 0 28px;
}

.g7-outline-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  padding: 0 32px;
  border: 1px solid #1f2329;
  color: #1f2329;
  font-size: 13.5px;
  text-decoration: none;
  transition: all 0.2s ease;
}

.g7-outline-btn:hover {
  background: #1f2329;
  color: #ffffff;
}

.g7-sub-matrix {
  margin-top: 56px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px 32px;
}

.g7-sub-matrix__item {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: #2b2f38;
  text-decoration: none;
  transition: color 0.2s ease;
}

.g7-sub-matrix__item:hover {
  color: #5b4be8;
}

.g7-sub-matrix__prefix {
  font-style: italic;
  font-weight: 900;
  color: #4c2ce6;
}

/* 产业物流数字化 4 个纵向切换条 */
.g7-sol-tabs {
  margin-top: 36px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  border-left: 1px solid #d0d5dd;
  padding-left: 20px;
}

.g7-sol-tab {
  position: relative;
  border: none;
  background: none;
  padding: 4px 0;
  text-align: left;
  font-size: 15px;
  color: #6e7380;
  font-family: inherit;
  cursor: pointer;
  transition: color 0.2s ease, font-weight 0.2s ease;
}

.g7-sol-tab.is-active {
  color: #1f2329;
  font-weight: 700;
}

.g7-sol-tab.is-active::before {
  content: '';
  position: absolute;
  left: -21px;
  top: 4px;
  bottom: 4px;
  width: 2px;
  background: #5b4be8;
}

/* 右侧立体舞台 */
.g7-showcase__right {
  position: relative;
  height: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 场景1：IoT雷达 */
.scene-radar {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.scene-radar__beam {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.scene-radar__beam--front {
  width: 340px;
  height: 220px;
  left: 12%;
  background: radial-gradient(ellipse at right center, rgba(99, 102, 241, 0.35) 0%, rgba(56, 189, 248, 0.12) 55%, transparent 75%);
  clip-path: polygon(100% 50%, 0 0, 0 100%);
}

.radar-alert-card {
  position: absolute;
  top: 22%;
  right: 16%;
  z-index: 4;
  padding: 12px 18px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  box-shadow: 0 14px 32px rgba(15, 23, 42, 0.1);
  display: flex;
  align-items: center;
  gap: 14px;
}

.radar-alert-card__text {
  display: flex;
  flex-direction: column;
}

.radar-alert-card__lbl {
  font-size: 12px;
  color: #64748b;
}

.radar-alert-card__val {
  font-size: 20px;
  font-weight: 800;
  color: #1e293b;
  font-variant-numeric: tabular-nums;
}

.radar-alert-card__icon {
  font-size: 22px;
}

.topdown-truck {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 4px;
  transform: rotate(-16deg);
  filter: drop-shadow(0 24px 32px rgba(15, 23, 42, 0.16));
}

.topdown-truck__cab {
  width: 58px;
  height: 70px;
  border-radius: 14px 6px 6px 14px;
  background: #ffffff;
  border: 2px solid #cbd5e1;
}

.topdown-truck__trailer {
  width: 220px;
  height: 76px;
  border-radius: 4px;
  background: linear-gradient(90deg, #ffffff 0%, #f1f5f9 100%);
  border: 2px solid #cbd5e1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 900;
  font-style: italic;
  color: #4c2ce6;
}

/* 场景2：交易服务扫码加油 */
.scene-payment {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.payment-popup {
  position: absolute;
  top: 16%;
  right: 24%;
  z-index: 4;
  min-width: 170px;
  padding: 14px 18px;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 18px 40px rgba(79, 70, 229, 0.14);
  border: 1px solid #e0e7ff;
  transition: transform 0.3s ease;
}

.payment-popup__head {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #4f46e5;
  font-weight: 600;
  margin-bottom: 6px;
}

.payment-popup__dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #4f46e5;
}

.payment-popup__body {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
}

.payment-popup__sub {
  font-size: 12px;
  color: #64748b;
}

.payment-popup__amt {
  font-size: 22px;
  font-weight: 800;
  color: #1e293b;
}

.station-isotruck {
  display: flex;
  align-items: flex-end;
  gap: 24px;
  transform: rotate(-10deg);
}

.station-isotruck__pumps {
  display: flex;
  gap: 10px;
}

.pump-box {
  width: 44px;
  height: 76px;
  border-radius: 6px;
  background: #ffffff;
  border: 2px solid #94a3b8;
  border-top: 12px solid #4c2ce6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  font-style: italic;
  color: #4c2ce6;
}

.station-isotruck__vehicle {
  display: flex;
  align-items: flex-end;
  gap: 4px;
}

.iso-trailer {
  width: 190px;
  height: 86px;
  background: #ffffff;
  border: 2px solid #cbd5e1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 900;
  font-style: italic;
  color: #4c2ce6;
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.12);
}

.iso-cab {
  width: 54px;
  height: 68px;
  background: #e2e8f0;
  border: 2px solid #94a3b8;
  border-radius: 4px 12px 4px 4px;
}

/* 场景3：产业物流数字化正面吊与 RFID 标签弹出 */
.scene-solutions {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.iso-yard {
  position: relative;
  width: 420px;
  height: 260px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.iso-containers {
  position: absolute;
  right: 24px;
  top: 28px;
  display: flex;
   gap: 8px;
  transform: skewY(18deg);
}

.iso-box {
  width: 68px;
  height: 54px;
  background: #e2e8f0;
  border: 1.5px solid #cbd5e1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  font-style: italic;
  color: #4c2ce6;
}

.iso-crane__arm {
  width: 140px;
  height: 14px;
  background: #1e293b;
  transform: rotate(-28deg);
  border-radius: 3px;
}

.iso-truck-load {
  position: absolute;
  bottom: 28px;
  right: 48px;
  display: flex;
  align-items: flex-end;
  gap: 4px;
  transform: skewY(18deg);
}

.iso-truck-load__box {
  width: 120px;
  height: 62px;
  background: #ffffff;
  border: 2px solid #94a3b8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 900;
  font-style: italic;
  color: #4c2ce6;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.14);
}

.iso-truck-load__cab {
  width: 44px;
  height: 52px;
  background: #334155;
  border-radius: 4px;
}

.iso-callout {
  position: absolute;
  z-index: 5;
  animation: callout-pop 0.38s cubic-bezier(0.22, 1, 0.36, 1) backwards;
}

@keyframes callout-pop {
  from {
    opacity: 0;
    transform: translateY(10px) scale(0.92);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.iso-callout__pill {
  display: inline-block;
  padding: 6px 16px;
  border-radius: 4px;
  border: 1px solid #93c5fd;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(6px);
  color: #1e293b;
  font-size: 12.5px;
  font-weight: 500;
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.1);
}

.stage-fade-enter-active,
.stage-fade-leave-active {
  transition: opacity 0.26s ease, transform 0.26s ease;
}

.stage-fade-enter-from,
.stage-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

@media (max-width: 960px) {
  .g7-metrics-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .g7-showcase__inner {
    grid-template-columns: 1fr;
  }
  .g7-showcase__dots {
    flex-direction: row;
  }
}
</style>
