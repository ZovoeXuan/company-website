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
  HomeLink,
  HomeView,
} from '@/data/types'

const router = useRouter()
const data = ref<HomeView | null>(null)
const siteName = ref('公司官网')
const heroIndex = ref(0)

let heroTimer: number | null = null

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
  if (configured.length > 0) {
    return configured.map((item, idx) => ({
      ...DEFAULT_HEROES[idx % DEFAULT_HEROES.length],
      ...item,
      theme: item.theme || DEFAULT_HEROES[idx % DEFAULT_HEROES.length]?.theme || 'dark',
    }))
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

// ==================== 3. 核心产品与行业方案分组（老版本左右交替灵动排布） ====================
export interface HomeServiceGroupItem {
  key: string
  title: string
  description: string
  tag?: string
  layout?: 'auto' | 'left' | 'right'
  image?: any
  moreLink?: HomeLink | null
  highlights?: string[]
  items: Array<{
    id?: string | number
    title: string
    summary: string
    url?: string
    cover?: { url: string; alt?: string } | null
    categoryName?: string
  }>
}

const DEFAULT_SERVICES: HomeServiceGroupItem[] = [
  {
    key: 'srv-1',
    title: '设备接入',
    description: '从协议解析到边缘缓存，把不同厂商的设备统一接进来。',
    layout: 'left', // 左边文案，右边卡片
    tag: 'IoT硬件与边缘网关',
    highlights: ['工业协议全栈解析', '毫秒级边缘缓存', '多厂商设备即插即用'],
    moreLink: { type: 'PAGE', url: '/products' },
    items: [
      {
        id: '1',
        title: '设备数据采集网关',
        summary: '工业级边缘智能网关，支持 Modbus、OPC-UA、CAN 总线等多协议互通与实时边缘处理。',
        url: '/products/1',
        categoryName: 'IoT服务',
      },
    ],
  },
  {
    key: 'srv-2',
    title: '健康与能源',
    description: '用振动、温度与能耗基线，提前发现设备劣化与能源浪费。',
    layout: 'right', // 右边文案，左边卡片（一个在左一个在右，灵动错落！）
    tag: 'AI能耗与预测性维护',
    highlights: ['振动能耗双基线', '故障劣化提前预警', '综合能效提升 18%+'],
    moreLink: { type: 'PAGE', url: '/products?category=IoT' },
    items: [
      {
        id: '2',
        title: '设备健康管理平台',
        summary: '基于 AI 时序机理模型的设备全生命周期健康预测，杜绝意外停机与高能耗隐患。',
        url: '/products?category=IoT',
        categoryName: '交易与能源',
      },
    ],
  },
  {
    key: 'srv-3',
    title: '智能安全运营',
    description: 'AI 驾驶安全干预与全天候盲区防碰撞，护航每一趟在途运输。',
    layout: 'left', // 左边文案，右边卡片
    tag: '数字安全大脑',
    highlights: ['50+ 高危场景主动干预', '盲区声光防碰撞', '风险事故率降低 90%'],
    moreLink: { type: 'PAGE', url: '/products?category=安全' },
    items: [
      {
        id: '3',
        title: '安全驾驶智能系统',
        summary: '软硬件一体的主动安全防控体系，实时防疲劳防分神，全流程护航在途安全。',
        url: '/products?category=安全',
        categoryName: '安全服务',
      },
    ],
  },
  {
    key: 'srv-4',
    title: '全链路冷链追溯',
    description: '高精度双温区感知与断链秒级告警，保障温控敏感货物全流程品质可溯。',
    layout: 'right', // 右边文案，左边卡片
    tag: '数智冷链温控',
    highlights: ['±0.5℃ 高精温感探头', '开门与断温秒级上报', '医药食品合规认证'],
    moreLink: { type: 'PAGE', url: '/products?category=冷链' },
    items: [
      {
        id: '4',
        title: '智能冷链温控管家',
        summary: '覆盖冷库、冷藏车、冷柜端到端温度全程透明可视与智能溯源闭环。',
        url: '/products?category=冷链',
        categoryName: '冷链服务',
      },
    ],
  },
]

const servicesList = computed<HomeServiceGroupItem[]>(() => {
  if (data.value && (!data.value.services || data.value.services.enabled === false)) {
    return []
  }
  const customGroups = (data.value?.services?.groups ?? []) as unknown as HomeServiceGroupItem[]
  if (customGroups.length > 0) {
    return customGroups.map((g, idx) => {
      const fallback = DEFAULT_SERVICES[idx % DEFAULT_SERVICES.length]
      return {
        ...fallback,
        ...g,
        layout: g.layout || (idx % 2 === 1 ? 'right' : 'left'),
        tag: g.tag || ((g as any).contentType === 'SOLUTION' ? '行业解决方案' : (fallback.tag || '核心产品系列')),
        items: g.items && g.items.length > 0 ? g.items : (fallback.items || []),
        highlights: Array.isArray(g.highlights) && g.highlights.length > 0 ? g.highlights : (fallback.highlights || []),
      }
    })
  }
  if (!data.value) return DEFAULT_SERVICES
  return []
})

function isGroupReversed(group: HomeServiceGroupItem, gIdx: number): boolean {
  if (group.layout === 'right') return true
  if (group.layout === 'left') return false
  // 默认 auto 奇偶交替：偶数 0 在左(left)，奇数 1 在右(right)，一个在左一个在右
  return gIdx % 2 === 1
}

function handleGroupAction(group: HomeServiceGroupItem) {
  if (group.moreLink) {
    handleAction(group.moreLink, 'SERVICES_GROUP')
  } else {
    router.push('/products')
  }
}

function goToCard(card: any) {
  if (card.url) {
    if (card.url.startsWith('http://') || card.url.startsWith('https://')) {
      window.open(card.url, '_blank', 'noopener,noreferrer')
    } else {
      router.push(card.url)
    }
  } else if (card.id) {
    router.push(`/products/${card.id}`)
  } else {
    router.push('/products')
  }
}

// ==================== 4. 案例与客户筛选 ====================
const activeCaseIndustry = ref('ALL')
const caseIndustries = computed(() => {
  const items = data.value?.cases?.items ?? []
  const set = new Set<string>()
  for (const item of items) {
    if (item.industry) set.add(item.industry)
  }
  return ['ALL', ...Array.from(set)]
})

const filteredCases = computed(() => {
  const items = data.value?.cases?.items ?? []
  if (items.length === 0) {
    return [
      {
        id: '1',
        title: '某头部零担快运千万级车队安全数字化转型',
        summary: '接入主动安全预警系统后，重大交通事故发生率同比下降 92%，年化保费节约超 800 万元。',
        industry: '智慧物流',
        cover: null,
        url: '/cases/1',
        outcomes: [
          { displayValue: '92%', label: '高危事故率下降' },
          { displayValue: '800万+', label: '年化保费综合节约' },
        ],
      },
      {
        id: '2',
        title: '大型连锁餐饮集团冷链全温区数智监管实践',
        summary: '全流程冷链断链秒级告警与在途可视化，货物货损率由 1.8% 降至 0.08%，实现全程透明合规。',
        industry: '食品冷链',
        cover: null,
        url: '/cases/2',
        outcomes: [
          { displayValue: '0.08%', label: '在途货损率降至' },
          { displayValue: '100%', label: '食安合规追溯率' },
        ],
      },
      {
        id: '3',
        title: '大宗能源化工运力智慧调度与数字加注结算',
        summary: '数字化油气加注与智能调度无缝联动，单车百公里综合油耗降低 2.3L，结算对账效率提升 10 倍。',
        industry: '大宗能源',
        cover: null,
        url: '/cases/3',
        outcomes: [
          { displayValue: '2.3L', label: '百公里油耗下降' },
          { displayValue: '10倍', label: '财税对账效率提升' },
        ],
      },
    ]
  }
  if (activeCaseIndustry.value === 'ALL') return items
  return items.filter((item) => item.industry === activeCaseIndustry.value)
})

// ==================== 5. 客户标识与跑马灯 ====================
const logoViewMode = ref<'marquee' | 'grid'>('marquee')

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
})

onBeforeUnmount(() => {
  if (heroTimer) clearInterval(heroTimer)
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

    <!-- ==================== 3. 核心产品与行业方案分组 (Services - 一个在左一个在右灵动排布) ==================== -->
    <section
      v-if="servicesList.length > 0"
      id="services"
      v-reveal="100"
      class="section services-section"
    >
      <div class="container container--wide">
        <!-- 模块顶栏 -->
        <div class="services-header">
          <div class="services-header__left">
            <span class="services-kicker">PRODUCTS & SOLUTIONS</span>
            <h2 class="services-title">{{ data?.services?.title || '核心产品与行业方案' }}</h2>
            <p class="services-sub">
              {{ data?.services?.subtitle || '从设备接入到运维决策，覆盖全流程的数字生产力工具。' }}
            </p>
          </div>
          <RouterLink to="/products" class="services-more-link">
            浏览全部产品与方案
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M5 12h14M12 5l7 7-7 7" />
            </svg>
          </RouterLink>
        </div>

        <!-- 左右交错服务分组列表 (一个在左一个在右，灵动错落) -->
        <div class="service-groups">
          <div
            v-for="(group, gIdx) in servicesList"
            :key="group.key || gIdx"
            v-reveal="140 + gIdx * 40"
            class="service-group"
            :class="{ 'is-reversed': isGroupReversed(group, gIdx) }"
          >
            <!-- 灵动背景微光 -->
            <div class="service-group__glow" aria-hidden="true"></div>

            <div class="service-group__lead">
              <!-- 文案与介绍列 (奇数右/偶数左，错落交替) -->
              <div class="service-group__meta">
                <div class="service-group__badge-row">
                  <span class="service-group__index">0{{ gIdx + 1 }}</span>
                  <span class="service-group__pill">{{ group.tag || '核心产品系列' }}</span>
                </div>
                <h3 class="service-group__title">{{ group.title }}</h3>
                <p class="service-group__desc">{{ group.description }}</p>

                <!-- 灵动亮点标签 -->
                <div v-if="group.highlights && group.highlights.length" class="service-group__chips">
                  <span
                    v-for="(chip, cIdx) in group.highlights"
                    :key="cIdx"
                    class="service-chip"
                  >
                    <span class="service-chip__dot"></span>
                    {{ chip }}
                  </span>
                </div>

                <div class="service-group__actions">
                  <button
                    type="button"
                    class="service-group__cta"
                    @click="handleGroupAction(group)"
                  >
                    <span>了解该领域详情</span>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M5 12h14M12 5l7 7-7 7" />
                    </svg>
                  </button>
                </div>
              </div>

              <!-- 卡片展示与多媒体列 (奇数左/偶数右，错落交替) -->
              <div class="service-group__visual">
                <!-- 关联的产品卡片 -->
                <div v-if="group.items && group.items.length > 0" class="service-cards-wrap">
                  <div
                    v-for="card in group.items"
                    :key="card.id || card.title"
                    class="service-card"
                    @click="goToCard(card)"
                  >
                    <div class="service-card__media">
                      <img
                        v-if="card.cover?.url"
                        :src="card.cover.url"
                        :alt="card.cover.alt || card.title"
                        loading="lazy"
                        class="service-card__img"
                      />
                      <div v-else class="service-card__placeholder">
                        <div class="device-graphic-box">
                          <span class="device-graphic-box__cube"></span>
                        </div>
                      </div>
                      <span class="service-card__category">{{ card.categoryName || group.title }}</span>
                    </div>

                    <div class="service-card__body">
                      <h4 class="service-card__title">
                        {{ card.title }}
                        <span class="service-card__arrow">→</span>
                      </h4>
                      <p class="service-card__summary">{{ card.summary }}</p>
                      <div class="service-card__footer">
                        <span class="service-card__link">查看详情 →</span>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 独立配图展示 -->
                <div v-else-if="group.image" class="service-banner-wrap">
                  <video
                    v-if="group.image.type === 'VIDEO'"
                    :src="group.image.url"
                    class="service-banner-media"
                    autoplay
                    muted
                    loop
                    playsinline
                  ></video>
                  <img
                    v-else
                    :src="group.image.url"
                    :alt="group.image.alt || group.title"
                    class="service-banner-media"
                    loading="lazy"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ==================== 4. 标杆客户案例 (Cases) ==================== -->
    <section
      v-if="filteredCases.length > 0"
      id="cases"
      v-reveal="120"
      class="section section--surface cases-section"
    >
      <div class="container container--wide">
        <div class="section-head">
          <div>
            <span class="kicker">CUSTOMER SUCCESS</span>
            <h2 class="section-title">{{ data?.cases?.title || '行业领军企业的共同选择' }}</h2>
            <p v-if="data?.cases?.subtitle" class="section-sub">{{ data.cases.subtitle }}</p>
          </div>
          <RouterLink to="/cases" class="link-more section-head__link">
            查看全部标杆案例 →
          </RouterLink>
        </div>

        <!-- 行业快速筛选胶囊 -->
        <div v-if="caseIndustries.length > 2" class="industry-pills">
          <button
            v-for="ind in caseIndustries"
            :key="ind"
            type="button"
            class="industry-pill"
            :class="{ 'is-active': activeCaseIndustry === ind }"
            @click="activeCaseIndustry = ind"
          >
            {{ ind === 'ALL' ? '全部行业' : ind }}
          </button>
        </div>

        <div class="grid grid--3">
          <RouterLink
            v-for="item in filteredCases"
            :key="item.id"
            :to="item.url || `/cases/${item.id}`"
            class="case-card"
          >
            <div class="case-card__media">
              <img
                v-if="item.cover"
                :src="item.cover.url"
                :alt="item.cover.alt || item.title"
                loading="lazy"
              />
              <div v-else class="case-card__placeholder">
                <span class="case-card__placeholder-tag">{{ item.industry || '标杆客户' }}</span>
              </div>
            </div>
            <div class="case-card__body">
              <span v-if="item.industry" class="case-card__industry">{{ item.industry }}</span>
              <h3 class="case-card__title">{{ item.title }}</h3>
              <p v-if="item.summary" class="case-card__summary">{{ item.summary }}</p>
              <div v-if="item.outcomes?.length" class="case-outcomes">
                <div v-for="(out, oIdx) in item.outcomes.slice(0, 2)" :key="oIdx" class="case-outcomes__item">
                  <p class="case-outcomes__val">{{ out.displayValue }}</p>
                  <span class="case-outcomes__lbl">{{ out.label }}</span>
                </div>
              </div>
            </div>
          </RouterLink>
        </div>
      </div>
    </section>

    <!-- ==================== 5. 合作客户标识 (Customers) ==================== -->
    <section
      v-if="data?.customers?.items?.length"
      id="customers"
      v-reveal="120"
      class="section section--tight customers-section"
    >
      <div class="container container--wide">
        <div class="section-head">
          <div>
            <span class="kicker">TRUSTED BY INDUSTRY LEADERS</span>
            <h2 class="section-title">{{ data?.customers?.title || '生态伙伴与标杆客户' }}</h2>
            <p v-if="data?.customers?.subtitle" class="section-sub">{{ data.customers.subtitle }}</p>
          </div>
          <div class="logo-mode-switch">
            <button
              type="button"
              class="mode-btn"
              :class="{ 'is-active': logoViewMode === 'marquee' }"
              @click="logoViewMode = 'marquee'"
            >
              动态流展示
            </button>
            <button
              type="button"
              class="mode-btn"
              :class="{ 'is-active': logoViewMode === 'grid' }"
              @click="logoViewMode = 'grid'"
            >
              矩阵展示
            </button>
          </div>
        </div>

        <!-- 跑马灯模式 -->
        <div v-if="logoViewMode === 'marquee' && data.customers.items.length >= 4" class="logo-marquee">
          <div class="logo-marquee__track">
            <template v-for="loop in 2" :key="loop">
              <component
                :is="cust.url ? 'a' : 'div'"
                v-for="cust in data.customers.items"
                :key="`${loop}-${cust.id}`"
                :href="cust.url || undefined"
                :target="cust.url ? '_blank' : undefined"
                :rel="cust.url ? 'noopener noreferrer' : undefined"
                class="logo-tile"
              >
                <img :src="cust.logo.url" :alt="cust.logo.alt || cust.name" loading="lazy" />
                <span class="logo-tile__name">{{ cust.name }}</span>
              </component>
            </template>
          </div>
        </div>

        <!-- 网格模式 -->
        <ul v-else class="logo-grid">
          <li v-for="cust in data.customers.items" :key="cust.id" class="logo-grid__item">
            <component
              :is="cust.url ? 'a' : 'div'"
              :href="cust.url || undefined"
              :target="cust.url ? '_blank' : undefined"
              class="logo-grid__link"
            >
              <img :src="cust.logo.url" :alt="cust.logo.alt || cust.name" loading="lazy" />
            </component>
          </li>
        </ul>
      </div>
    </section>

    <!-- ==================== 6. 最新动态 (News) ==================== -->
    <section
      v-if="data?.news?.items?.length"
      id="news"
      v-reveal="120"
      class="section section--surface news-section"
    >
      <div class="container container--wide">
        <div class="section-head">
          <div>
            <span class="kicker">NEWS & INSIGHTS</span>
            <h2 class="section-title">{{ data?.news?.title || '最新动态与前沿洞察' }}</h2>
            <p v-if="data?.news?.subtitle" class="section-sub">{{ data.news.subtitle }}</p>
          </div>
          <RouterLink to="/news" class="link-more section-head__link">
            浏览全部新闻动态 →
          </RouterLink>
        </div>

        <div class="grid grid--3">
          <RouterLink
            v-for="item in data.news.items"
            :key="item.id"
            :to="item.url || `/news/${item.id}`"
            class="news-card"
          >
            <div v-if="item.cover" class="news-card__media">
              <img :src="item.cover.url" :alt="item.cover.alt || item.title" loading="lazy" />
            </div>
            <div class="news-card__body">
              <time class="news-card__date">{{ item.displayTime }}</time>
              <h3 class="news-card__title">{{ item.title }}</h3>
              <p v-if="item.summary" class="news-card__summary">{{ item.summary }}</p>
              <span class="link-more news-card__more">阅读全文 →</span>
            </div>
          </RouterLink>
        </div>
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
  white-space: pre-line;
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
  white-space: pre-line;
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

/* ==================== 3. 核心产品与方案分组 (一个在左一个在右灵动排布) ==================== */
.services-section {
  padding: 100px 0 110px;
  background: #f8fafc;
  position: relative;
  overflow: hidden;
}

.services-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 56px;
  padding-bottom: 24px;
  border-bottom: 1px solid #e2e8f0;
}

.services-kicker {
  display: inline-block;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  color: #4c2ce6;
  margin-bottom: 8px;
}

.services-title {
  font-size: clamp(28px, 3.2vw, 38px);
  font-weight: 700;
  color: #0f172a;
  margin: 0;
  letter-spacing: -0.01em;
}

.services-sub {
  margin: 10px 0 0;
  font-size: 15px;
  color: #64748b;
  max-width: 600px;
  line-height: 1.6;
}

.services-more-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #4c2ce6;
  text-decoration: none;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.services-more-link:hover {
  color: #3b1fc4;
  transform: translateX(4px);
}

.service-groups {
  display: flex;
  flex-direction: column;
  gap: 72px;
}

/* 核心交错排布卡片容器：默认左文右卡，is-reversed 时左卡右文 (一个在左一个在右) */
.service-group {
  position: relative;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  padding: 48px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
  transition: transform 0.3s ease, box-shadow 0.3s ease, border-color 0.3s ease;
  overflow: hidden;
}

.service-group:hover {
  transform: translateY(-3px);
  box-shadow: 0 20px 40px rgba(76, 44, 230, 0.08);
  border-color: #cbd5e1;
}

.service-group__glow {
  position: absolute;
  top: -80px;
  right: -80px;
  width: 260px;
  height: 260px;
  background: radial-gradient(circle, rgba(76, 44, 230, 0.07) 0%, rgba(76, 44, 230, 0) 70%);
  pointer-events: none;
  border-radius: 50%;
  transition: transform 0.5s ease;
}

.service-group:hover .service-group__glow {
  transform: scale(1.3);
}

.service-group__lead {
  display: grid;
  grid-template-columns: 1fr 1.15fr;
  gap: 56px;
  align-items: center;
}

/* 左右灵动交错排布核心样式 */
/* 默认模式：左文右卡 (order: 1 / 2) */
.service-group .service-group__meta {
  order: 1;
}
.service-group .service-group__visual {
  order: 2;
}

/* 反转模式：左卡右文 (order: 2 / 1) */
.service-group.is-reversed .service-group__meta {
  order: 2;
}
.service-group.is-reversed .service-group__visual {
  order: 1;
}

.service-group__badge-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.service-group__index {
  font-size: 20px;
  font-weight: 800;
  color: #4c2ce6;
  opacity: 0.85;
  letter-spacing: -0.02em;
}

.service-group__pill {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: #4c2ce6;
  background: rgba(76, 44, 230, 0.08);
}

.service-group__title {
  font-size: clamp(24px, 2.4vw, 32px);
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 14px;
  letter-spacing: -0.01em;
}

.service-group__desc {
  font-size: 15px;
  line-height: 1.7;
  color: #475569;
  margin: 0 0 24px;
}

.service-group__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 28px;
}

.service-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 6px;
  background: #f1f5f9;
  font-size: 12.5px;
  color: #334155;
  font-weight: 500;
}

.service-chip__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #4c2ce6;
}

.service-group__actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.service-group__cta {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 22px;
  border-radius: 8px;
  background: #0f172a;
  color: #ffffff;
  font-size: 13.5px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.service-group__cta:hover {
  background: #4c2ce6;
  transform: translateY(-2px);
  box-shadow: 0 8px 18px rgba(76, 44, 230, 0.25);
}

/* 视觉区卡片展示 */
.service-cards-wrap {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
}

.service-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s ease;
  display: flex;
  flex-direction: column;
}

.service-card:hover {
  transform: translateY(-4px);
  border-color: #93c5fd;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.08);
}

.service-card__media {
  position: relative;
  width: 100%;
  height: 180px;
  background: #0b0f19;
  overflow: hidden;
}

.service-card__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.service-card:hover .service-card__img {
  transform: scale(1.04);
}

.service-card__placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0b0f19 0%, #1e1b4b 100%);
}

.device-graphic-box {
  width: 64px;
  height: 64px;
  border: 2px dashed rgba(255, 255, 255, 0.3);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.device-graphic-box__cube {
  width: 24px;
  height: 24px;
  background: #4c2ce6;
  border-radius: 4px;
  box-shadow: 0 0 16px rgba(76, 44, 230, 0.6);
}

.service-card__category {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  color: #ffffff;
  background: rgba(15, 23, 42, 0.75);
  backdrop-filter: blur(4px);
}

.service-card__body {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.service-card__title {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.service-card__arrow {
  color: #94a3b8;
  font-weight: 400;
  transition: transform 0.2s ease, color 0.2s ease;
}

.service-card:hover .service-card__arrow {
  color: #4c2ce6;
  transform: translateX(4px);
}

.service-card__summary {
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
  margin: 0 0 16px;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.service-card__footer {
  margin-top: auto;
}

.service-card__link {
  font-size: 12.5px;
  font-weight: 600;
  color: #4c2ce6;
}

.service-banner-wrap {
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
}

.service-banner-media {
  width: 100%;
  height: 320px;
  object-fit: cover;
  display: block;
}

/* ==================== 4. 标杆案例 (Cases) ==================== */
.cases-section {
  padding: 100px 0 110px;
  background: #ffffff;
}

.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 40px;
}

.kicker {
  display: inline-block;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  color: #4c2ce6;
  margin-bottom: 8px;
}

.section-title {
  font-size: clamp(26px, 3vw, 36px);
  font-weight: 700;
  color: #0f172a;
  margin: 0;
}

.section-sub {
  margin: 10px 0 0;
  font-size: 15px;
  color: #64748b;
}

.link-more {
  display: inline-flex;
  align-items: center;
  font-size: 14px;
  font-weight: 600;
  color: #4c2ce6;
  text-decoration: none;
  transition: all 0.2s ease;
}

.link-more:hover {
  transform: translateX(4px);
  color: #3b1fc4;
}

.industry-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 32px;
}

.industry-pill {
  padding: 6px 16px;
  border-radius: 999px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  color: #475569;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.industry-pill.is-active,
.industry-pill:hover {
  background: #4c2ce6;
  color: #ffffff;
  border-color: #4c2ce6;
}

.grid--3 {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 28px;
}

.case-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  overflow: hidden;
  text-decoration: none;
  display: flex;
  flex-direction: column;
  transition: all 0.25s ease;
}

.case-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.08);
  border-color: #cbd5e1;
}

.case-card__media {
  height: 200px;
  position: relative;
  background: #0f172a;
  overflow: hidden;
}

.case-card__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.case-card:hover .case-card__media img {
  transform: scale(1.05);
}

.case-card__placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.case-card__placeholder-tag {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  font-weight: 600;
}

.case-card__body {
  padding: 24px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.case-card__industry {
  font-size: 11.5px;
  font-weight: 700;
  color: #4c2ce6;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin-bottom: 8px;
}

.case-card__title {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 10px;
  line-height: 1.4;
}

.case-card__summary {
  font-size: 13.5px;
  color: #64748b;
  line-height: 1.6;
  margin: 0 0 20px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.case-outcomes {
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px dashed #e2e8f0;
  display: flex;
  gap: 20px;
}

.case-outcomes__item {
  flex: 1;
}

.case-outcomes__val {
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
  margin: 0;
}

.case-outcomes__lbl {
  font-size: 11.5px;
  color: #94a3b8;
}

/* ==================== 5. 生态伙伴 (Customers) ==================== */
.customers-section {
  padding: 80px 0;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
  border-bottom: 1px solid #e2e8f0;
}

.logo-mode-switch {
  display: flex;
  gap: 4px;
  background: #e2e8f0;
  padding: 3px;
  border-radius: 8px;
}

.mode-btn {
  border: none;
  background: none;
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  padding: 5px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.mode-btn.is-active {
  background: #ffffff;
  color: #0f172a;
  box-shadow: 0 2px 4px rgba(15, 23, 42, 0.08);
}

.logo-marquee {
  overflow: hidden;
  width: 100%;
  margin-top: 32px;
  mask-image: linear-gradient(90deg, transparent, #000 10%, #000 90%, transparent);
}

.logo-marquee__track {
  display: flex;
  gap: 32px;
  width: max-content;
  animation: marquee-roll 28s linear infinite;
}

@keyframes marquee-roll {
  from {
    transform: translateX(0);
  }
  to {
    transform: translateX(-50%);
  }
}

.logo-marquee__track:hover {
  animation-play-state: paused;
}

.logo-tile {
  width: 180px;
  height: 80px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px;
  text-decoration: none;
  transition: all 0.2s ease;
}

.logo-tile:hover {
  border-color: #cbd5e1;
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.06);
}

.logo-tile img {
  max-width: 110px;
  max-height: 38px;
  object-fit: contain;
  filter: grayscale(1);
  opacity: 0.75;
  transition: all 0.2s ease;
}

.logo-tile:hover img {
  filter: grayscale(0);
  opacity: 1;
}

.logo-tile__name {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 4px;
}

.logo-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  list-style: none;
  padding: 0;
  margin: 32px 0 0;
}

.logo-grid__item {
  height: 76px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
}

.logo-grid__link {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
}

.logo-grid__link img {
  max-width: 100px;
  max-height: 36px;
  object-fit: contain;
  filter: grayscale(1);
  opacity: 0.7;
}

.logo-grid__link:hover img {
  filter: grayscale(0);
  opacity: 1;
}

/* ==================== 6. 最新动态 (News) ==================== */
.news-section {
  padding: 100px 0 110px;
  background: #ffffff;
}

.news-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  overflow: hidden;
  text-decoration: none;
  display: flex;
  flex-direction: column;
  transition: all 0.25s ease;
}

.news-card:hover {
  transform: translateY(-4px);
  border-color: #cbd5e1;
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.08);
}

.news-card__media {
  height: 180px;
  background: #0f172a;
}

.news-card__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.news-card__body {
  padding: 22px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.news-card__date {
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 8px;
}

.news-card__title {
  font-size: 17px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 10px;
  line-height: 1.45;
}

.news-card__summary {
  font-size: 13.5px;
  color: #64748b;
  line-height: 1.6;
  margin: 0 0 16px;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-card__more {
  margin-top: auto;
  font-size: 13px;
  font-weight: 600;
  color: #4c2ce6;
}

/* 响应式适配 */
@media (max-width: 960px) {
  .service-group__lead {
    grid-template-columns: 1fr;
    gap: 36px;
  }
  .service-group .service-group__meta,
  .service-group.is-reversed .service-group__meta {
    order: 1 !important;
  }
  .service-group .service-group__visual,
  .service-group.is-reversed .service-group__visual {
    order: 2 !important;
  }
  .grid--3 {
    grid-template-columns: repeat(2, 1fr);
  }
  .logo-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 640px) {
  .service-group {
    padding: 24px;
  }
  .grid--3 {
    grid-template-columns: 1fr;
  }
  .logo-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .services-header,
  .section-head {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}

</style>
