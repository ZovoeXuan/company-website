<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ApiError } from '@/api/http'
import { getHome } from '@/api/public'
import { openConsult, openRoiCalculator } from '@/composables/consult'
import { openVideo } from '@/composables/videoModal'
import { usePageMeta } from '@/composables/usePageMeta'
import { vReveal } from '@/directives/reveal'
import EmptyState from '@/components/EmptyState.vue'
import type { HomeLink, HomeView } from '@/data/types'

const router = useRouter()
const data = ref<HomeView | null>(null)
const loading = ref(true)
const error = ref('')

// 首屏轮播与自动播放控制
const heroIndex = ref(0)
const heroPaused = ref(false)
const heroProgress = ref(0)
let heroTicker: number | null = null

// KPI 递增动画显示缓存
const animatedMetrics = reactive<Record<string, string>>({})
const metricsSectionRef = ref<HTMLElement | null>(null)
let metricsAnimated = false
let metricsObserver: IntersectionObserver | null = null

// 案例行业快速筛选与客户墙视图模式
const activeCaseIndustry = ref('ALL')
const logoViewMode = ref<'marquee' | 'grid'>('marquee')

// 首页内嵌快速 ROI 估算滑块
const quickUnits = ref(150)
const quickSavingWan = computed(() => Math.round(quickUnits.value * 1.65 * 10) / 10)

usePageMeta(() => ({
  title: '首页',
  description: '面向未来的物联网与 AI 数字生产力平台，助力行业领军企业实现降本增效与安全运营。',
}))

const DEFAULT_SECTION_ORDER = ['metrics', 'services', 'cases', 'customers', 'news', 'consultation']

const orderedSections = computed<string[]>(() => {
  const custom = data.value?.sectionOrder
  if (!custom || !Array.isArray(custom) || custom.length === 0) {
    return DEFAULT_SECTION_ORDER
  }
  const merged = [...custom.filter((k) => DEFAULT_SECTION_ORDER.includes(k))]
  for (const defKey of DEFAULT_SECTION_ORDER) {
    if (!merged.includes(defKey)) merged.push(defKey)
  }
  return merged
})

const heroes = computed(() => data.value?.hero?.items ?? [])
const currentHero = computed(() => heroes.value[heroIndex.value] ?? null)

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
  if (activeCaseIndustry.value === 'ALL') return items
  return items.filter((item) => item.industry === activeCaseIndustry.value)
})

function startHeroAutoPlay() {
  stopHeroAutoPlay()
  heroTicker = window.setInterval(() => {
    if (heroPaused.value || heroes.value.length <= 1) return
    heroProgress.value += 2
    if (heroProgress.value >= 100) {
      heroProgress.value = 0
      heroIndex.value = (heroIndex.value + 1) % heroes.value.length
    }
  }, 120)
}

function stopHeroAutoPlay() {
  if (heroTicker) {
    clearInterval(heroTicker)
    heroTicker = null
  }
}

function selectHero(idx: number) {
  heroIndex.value = idx
  heroProgress.value = 0
}

function runMetricsCountUp() {
  if (metricsAnimated || !data.value?.metrics?.items?.length) return
  metricsAnimated = true
  for (const item of data.value.metrics.items) {
    const raw = item.displayValue.trim()
    const match = raw.match(/^([0-9]+(?:\.[0-9]+)?)(.*)$/)
    if (!match) {
      animatedMetrics[item.key] = raw
      continue
    }
    const targetNum = parseFloat(match[1])
    const suffix = match[2] || ''
    const decimals = match[1].includes('.') ? match[1].split('.')[1].length : 0
    const duration = 1300
    const start = performance.now()
    const step = (now: number) => {
      const progress = Math.min(1, (now - start) / duration)
      const eased = 1 - Math.pow(1 - progress, 3)
      const current = (targetNum * eased).toFixed(decimals)
      animatedMetrics[item.key] = `${current}${suffix}`
      if (progress < 1) {
        requestAnimationFrame(step)
      } else {
        animatedMetrics[item.key] = raw
      }
    }
    requestAnimationFrame(step)
  }
}

function observeMetrics() {
  if (typeof IntersectionObserver === 'undefined') {
    runMetricsCountUp()
    return
  }
  metricsObserver?.disconnect()
  metricsObserver = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting) {
          runMetricsCountUp()
          metricsObserver?.disconnect()
        }
      }
    },
    { threshold: 0.2 },
  )
  window.setTimeout(() => {
    const el = metricsSectionRef.value || document.getElementById('metrics')
    if (el) metricsObserver?.observe(el)
    else runMetricsCountUp()
  }, 120)
}

function handleAction(link: HomeLink | null | undefined, sourcePosition = 'HOME_HERO') {
  if (!link) return
  if (link.type === 'CONSULT') {
    openConsult({
      sourcePosition,
      contentType: link.contentType as 'PRODUCT' | 'SOLUTION' | 'CASE' | undefined,
      contentId: link.contentId,
    })
    return
  }
  if (link.type === 'ANCHOR' && link.url) {
    const targetId = link.url.replace(/^#/, '')
    const el = document.getElementById(targetId)
    if (el) {
      el.scrollIntoView({ behavior: 'smooth' })
      return
    }
  }
  if (link.url) {
    if (link.url.startsWith('http://') || link.url.startsWith('https://')) {
      if (link.newWindow) {
        window.open(link.url, '_blank', 'noopener,noreferrer')
      } else {
        window.location.href = link.url
      }
    } else {
      router.push(link.url)
    }
  }
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    data.value = await getHome()
    heroIndex.value = 0
    heroProgress.value = 0
    metricsAnimated = false
    startHeroAutoPlay()
    observeMetrics()
  } catch (err) {
    data.value = null
    error.value =
      err instanceof ApiError && err.code === 'NOT_FOUND'
        ? '首页内容尚未发布，请管理员在后台「首页管理」或「实景交互预演台」完成配置并发布。'
        : err instanceof ApiError
        ? err.message
        : '首页数据加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

onMounted(load)
onBeforeUnmount(() => {
  stopHeroAutoPlay()
  metricsObserver?.disconnect()
})
</script>

<template>
  <div class="home-page">
    <div v-if="loading" class="container state">
      <div class="skeleton"></div>
    </div>

    <div v-else-if="error" class="container state">
      <EmptyState title="首页暂不可用" :description="error">
        <button type="button" class="btn btn--primary" @click="load">重新加载</button>
      </EmptyState>
    </div>

    <template v-else-if="data">
      <!-- 1. 首屏 Hero（固定置顶，支持视频背景、双端自适应与自动轮播进度条） -->
      <section
        v-if="currentHero"
        class="hero"
        @mouseenter="heroPaused = true"
        @mouseleave="heroPaused = false"
      >
        <div class="hero__media">
          <video
            v-if="currentHero.desktopImage?.type === 'VIDEO'"
            :key="currentHero.desktopImage.url"
            :src="currentHero.desktopImage.url"
            class="hero__bg"
            autoplay
            muted
            loop
            playsinline
          ></video>
          <picture v-else-if="currentHero.desktopImage">
            <source
              v-if="currentHero.mobileImage?.url"
              media="(max-width: 768px)"
              :srcset="currentHero.mobileImage.url"
            />
            <img
              :src="currentHero.desktopImage.url"
              :alt="currentHero.desktopImage.alt || currentHero.title"
              class="hero__bg"
              fetchpriority="high"
            />
          </picture>
          <div class="hero__overlay"></div>
        </div>

        <div class="container container--wide hero__content">
          <div class="hero__badge-row">
            <span class="hero__pill">AI + IoT 生产力平台</span>
            <button
              type="button"
              class="hero__video-trigger"
              @click="
                openVideo({
                  title: '企业级 AI 与物联网全场景数字平台演示',
                  url: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
                })
              "
            >
              ▶ 观看 90 秒全景演示
            </button>
          </div>

          <h1 class="hero__title">{{ currentHero.title }}</h1>
          <p v-if="currentHero.subtitle" class="hero__subtitle">{{ currentHero.subtitle }}</p>

          <div class="hero__actions">
            <button
              v-if="currentHero.buttonText && currentHero.link"
              type="button"
              class="btn btn--accent hero__primary-btn"
              @click="handleAction(currentHero.link, 'HOME_HERO')"
            >
              <span>{{ currentHero.buttonText }}</span>
              <span aria-hidden="true">→</span>
            </button>
            <button
              type="button"
              class="btn btn--outline-light"
              @click="openRoiCalculator()"
            >
              🧮 测算您的降本 ROI
            </button>
          </div>

          <!-- 多屏轮播指示器与进度条 -->
          <div v-if="heroes.length > 1" class="hero__Controls">
            <button
              v-for="(slide, idx) in heroes"
              :key="slide.key"
              type="button"
              class="hero__indicator"
              :class="{ 'is-active': idx === heroIndex }"
              :aria-label="`切换到第 ${idx + 1} 屏：${slide.title}`"
              @click="selectHero(idx)"
            >
              <span class="hero__indicator-num">0{{ idx + 1 }}</span>
              <span class="hero__indicator-label">{{ slide.title }}</span>
              <span class="hero__indicator-bar">
                <span
                  class="hero__indicator-fill"
                  :style="{ width: idx === heroIndex ? `${heroProgress}%` : '0%' }"
                ></span>
              </span>
            </button>
          </div>
        </div>
      </section>

      <!-- 按后台 Visual Live Studio 编排的 sectionOrder 动态渲染各大区块 -->
      <template v-for="secKey in orderedSections" :key="secKey">
        <!-- A. 企业关键指标 (metrics) — 带数字滚动递增动画 -->
        <section
          v-if="secKey === 'metrics' && data.metrics?.items?.length"
          id="metrics"
          ref="metricsSectionRef"
          v-reveal
          class="section section--tight metrics-section"
        >
          <div class="container container--wide">
            <div v-if="data.metrics.title || data.metrics.subtitle" class="section-head">
              <div>
                <span class="kicker">SCALE & IMPACT</span>
                <h2 v-if="data.metrics.title" class="section-title">{{ data.metrics.title }}</h2>
                <p v-if="data.metrics.subtitle" class="section-sub">{{ data.metrics.subtitle }}</p>
              </div>
            </div>

            <ul class="metrics-grid">
              <li v-for="item in data.metrics.items" :key="item.key" class="metric-item">
                <div class="metric-item__val-row">
                  <span class="metric-item__value">
                    {{ animatedMetrics[item.key] ?? item.displayValue }}
                  </span>
                  <span v-if="item.unit" class="metric-item__unit">{{ item.unit }}</span>
                </div>
                <p class="metric-item__label">{{ item.label }}</p>
                <p v-if="item.description" class="metric-item__desc">{{ item.description }}</p>
              </li>
            </ul>
          </div>
        </section>

        <!-- B. 核心产品与解决方案分组 (services) -->
        <section
          v-else-if="secKey === 'services' && data.services?.groups?.length"
          id="services"
          v-reveal
          class="section services-section"
        >
          <div class="container container--wide">
            <div class="section-head">
              <div>
                <span class="kicker">PRODUCTS & SOLUTIONS</span>
                <h2 class="section-title">{{ data.services.title || '核心产品与行业方案' }}</h2>
                <p v-if="data.services.subtitle" class="section-sub">{{ data.services.subtitle }}</p>
              </div>
              <RouterLink to="/products" class="link-more section-head__link">浏览全部产品与方案</RouterLink>
            </div>

            <div class="service-groups">
              <div
                v-for="(group, gIdx) in data.services.groups"
                :key="group.key"
                class="service-group"
                :class="{ 'is-reversed': gIdx % 2 === 1 }"
              >
                <div class="service-group__lead">
                  <div class="service-group__meta">
                    <span class="service-group__index">0{{ gIdx + 1 }}</span>
                    <h3 class="service-group__title">{{ group.title }}</h3>
                    <p v-if="group.description" class="service-group__desc">{{ group.description }}</p>
                    <button
                      v-if="group.moreLink"
                      type="button"
                      class="link-more service-group__more"
                      @click="handleAction(group.moreLink, 'OTHER_CTA')"
                    >
                      了解该领域详情
                    </button>
                  </div>
                  <div v-if="group.image" class="service-group__banner media-frame">
                    <video
                      v-if="group.image.type === 'VIDEO'"
                      :src="group.image.url"
                      autoplay
                      muted
                      loop
                      playsinline
                    ></video>
                    <img v-else :src="group.image.url" :alt="group.image.alt || group.title" loading="lazy" />
                  </div>
                </div>

                <div class="grid grid--3 service-group__cards">
                  <RouterLink
                    v-for="card in group.items"
                    :key="card.id"
                    :to="card.url || `/products/${card.id}`"
                    class="service-card"
                  >
                    <div v-if="card.cover" class="service-card__media media-frame">
                      <video
                        v-if="card.cover.type === 'VIDEO'"
                        :src="card.cover.url"
                        autoplay
                        muted
                        loop
                        playsinline
                      ></video>
                      <img v-else :src="card.cover.url" :alt="card.cover.alt || card.title" loading="lazy" />
                    </div>
                    <div class="service-card__body">
                      <div class="service-card__top">
                        <span class="tag">{{ card.contentType === 'SOLUTION' ? '解决方案' : '核心产品' }}</span>
                        <span class="service-card__arrow" aria-hidden="true">↗</span>
                      </div>
                      <h4 class="service-card__title">{{ card.title }}</h4>
                      <p v-if="card.summary" class="service-card__summary">{{ card.summary }}</p>
                    </div>
                  </RouterLink>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- C. 标杆客户案例 (cases) — 支持行业快筛与量化成果高亮 -->
        <section
          v-else-if="secKey === 'cases' && data.cases?.items?.length"
          id="cases"
          v-reveal
          class="section section--surface cases-section"
        >
          <div class="container container--wide">
            <div class="section-head">
              <div>
                <span class="kicker">CUSTOMER SUCCESS</span>
                <h2 class="section-title">{{ data.cases.title || '行业领军企业的共同选择' }}</h2>
                <p v-if="data.cases.subtitle" class="section-sub">{{ data.cases.subtitle }}</p>
              </div>
              <RouterLink :to="data.cases.moreUrl || '/cases'" class="link-more section-head__link">
                查看全部标杆案例
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
                <div class="case-card__media media-frame">
                  <img
                    v-if="item.cover"
                    :src="item.cover.url"
                    :alt="item.cover.alt || item.title"
                    loading="lazy"
                  />
                  <span v-if="item.industry" class="case-card__badge">{{ item.industry }}</span>
                </div>
                <div class="case-card__body">
                  <p v-if="item.customerName" class="case-card__customer">{{ item.customerName }}</p>
                  <h3 class="case-card__title">{{ item.title }}</h3>
                  <p v-if="item.summary" class="case-card__summary">{{ item.summary }}</p>

                  <dl v-if="item.outcomes?.length" class="case-outcomes">
                    <div v-for="out in item.outcomes" :key="out.label" class="case-outcomes__item">
                      <dd class="case-outcomes__val">{{ out.displayValue }}</dd>
                      <dt class="case-outcomes__lbl">{{ out.label }}</dt>
                    </div>
                  </dl>
                </div>
              </RouterLink>
            </div>
          </div>
        </section>

        <!-- D. 合作客户标识墙 (customers) — 支持无限无缝跑马灯与矩阵网格切换 -->
        <section
          v-else-if="secKey === 'customers' && data.customers?.items?.length"
          id="customers"
          v-reveal
          class="section section--tight customers-section"
        >
          <div class="container container--wide">
            <div class="section-head">
              <div>
                <span class="kicker">TRUSTED BY INDUSTRY LEADERS</span>
                <h2 class="section-title">{{ data.customers.title || '生态伙伴与标杆客户' }}</h2>
                <p v-if="data.customers.subtitle" class="section-sub">{{ data.customers.subtitle }}</p>
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

            <!-- 无限无缝跑马灯模式 -->
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
                    :title="cust.name"
                  >
                    <img :src="cust.logo.url" :alt="cust.logo.alt || cust.name" loading="lazy" />
                    <span class="logo-tile__name">{{ cust.name }}</span>
                  </component>
                </template>
              </div>
            </div>

            <!-- 经典矩阵模式 -->
            <ul v-else class="logo-grid">
              <li v-for="cust in data.customers.items" :key="cust.id" class="logo-grid__item">
                <a
                  v-if="cust.url"
                  :href="cust.url"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="logo-grid__link"
                >
                  <img :src="cust.logo.url" :alt="cust.logo.alt || cust.name" loading="lazy" />
                </a>
                <div v-else class="logo-grid__link">
                  <img :src="cust.logo.url" :alt="cust.logo.alt || cust.name" loading="lazy" />
                </div>
              </li>
            </ul>
          </div>
        </section>

        <!-- E. 最新动态与洞察 (news) -->
        <section
          v-else-if="secKey === 'news' && data.news?.items?.length"
          id="news"
          v-reveal
          class="section section--surface news-section"
        >
          <div class="container container--wide">
            <div class="section-head">
              <div>
                <span class="kicker">NEWS & INSIGHTS</span>
                <h2 class="section-title">{{ data.news.title || '最新动态与前沿洞察' }}</h2>
                <p v-if="data.news.subtitle" class="section-sub">{{ data.news.subtitle }}</p>
              </div>
              <RouterLink :to="data.news.moreUrl || '/news'" class="link-more section-head__link">
                浏览全部新闻动态
              </RouterLink>
            </div>

            <div class="grid grid--3">
              <RouterLink
                v-for="item in data.news.items"
                :key="item.id"
                :to="item.url || `/news/${item.id}`"
                class="news-card"
              >
                <div v-if="item.cover" class="news-card__media media-frame">
                  <img :src="item.cover.url" :alt="item.cover.alt || item.title" loading="lazy" />
                </div>
                <div class="news-card__body">
                  <time class="news-card__date">{{ item.displayTime }}</time>
                  <h3 class="news-card__title">{{ item.title }}</h3>
                  <p v-if="item.summary" class="news-card__summary">{{ item.summary }}</p>
                  <span class="link-more news-card__more">阅读全文</span>
                </div>
              </RouterLink>
            </div>
          </div>
        </section>

        <!-- F. 互动式 ROI 快捷测算横幅 + 底部咨询行动区 (consultation) -->
        <section
          v-else-if="secKey === 'consultation' && data.consultation"
          id="consultation"
          v-reveal
          class="section section--dark consult-cta-section"
        >
          <div class="container container--wide">
            <!-- 互动式 ROI 快速精算卡 -->
            <div class="roi-inline-card">
              <div class="roi-inline-card__left">
                <span class="eyebrow">ROI QUICK ESTIMATOR · 降本增效快速精算</span>
                <h3 class="roi-inline-card__title">滑动评估您的车队/设备规模，即刻查看预计年化收益</h3>
                <div class="roi-inline-card__slider-row">
                  <span>当前运营规模：<strong>{{ quickUnits }} 台/节点</strong></span>
                  <input
                    v-model.number="quickUnits"
                    type="range"
                    min="20"
                    max="1500"
                    step="10"
                    class="roi-inline-slider"
                  />
                </div>
              </div>
              <div class="roi-inline-card__right">
                <span class="roi-inline-card__lbl">预计年化综合节降约</span>
                <strong class="roi-inline-card__val">¥ {{ quickSavingWan }} 万/年</strong>
                <button type="button" class="btn btn--accent" @click="openRoiCalculator()">
                  打开完整多维测算器 →
                </button>
              </div>
            </div>

            <!-- 底部主咨询区 -->
            <div class="consult-cta-box">
              <div>
                <h2 class="consult-cta-box__title">{{ data.consultation.title || '开启企业数字化生产力跃迁' }}</h2>
                <p v-if="data.consultation.subtitle" class="consult-cta-box__sub">
                  {{ data.consultation.subtitle }}
                </p>
              </div>
              <div class="consult-cta-box__actions">
                <button
                  type="button"
                  class="btn btn--accent consult-cta-box__btn"
                  @click="handleAction(data.consultation.link, 'HOME_BOTTOM')"
                >
                  {{ data.consultation.buttonText || '立即预约专家咨询' }}
                </button>
              </div>
            </div>
          </div>
        </section>
      </template>
    </template>
  </div>
</template>

<style scoped>
.hero {
  position: relative;
  min-height: clamp(560px, 78vh, 760px);
  display: flex;
  align-items: center;
  background: #08080c;
  color: #ffffff;
  overflow: hidden;
}

.hero__media {
  position: absolute;
  inset: 0;
}

.hero__bg {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero__overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    95deg,
    rgba(6, 8, 16, 0.92) 0%,
    rgba(6, 8, 16, 0.74) 48%,
    rgba(6, 8, 16, 0.25) 100%
  );
}

.hero__content {
  position: relative;
  z-index: 2;
  padding-top: 88px;
  padding-bottom: 64px;
}

.hero__badge-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}

.hero__pill {
  padding: 5px 14px;
  border-radius: 999px;
  background: rgba(91, 75, 232, 0.28);
  border: 1px solid rgba(139, 92, 246, 0.5);
  font-size: 12.5px;
  font-weight: 600;
  letter-spacing: 0.06em;
  color: #ddd6fe;
}

.hero__video-trigger {
  padding: 5px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #ffffff;
  font-size: 12.5px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.hero__video-trigger:hover {
  background: rgba(255, 255, 255, 0.2);
}

.hero__title {
  max-width: 720px;
  font-size: clamp(36px, 5vw, 64px);
  line-height: 1.12;
  font-weight: 700;
  color: #ffffff;
  white-space: pre-line;
  letter-spacing: -0.03em;
}

.hero__subtitle {
  margin-top: 20px;
  max-width: 580px;
  font-size: clamp(16px, 1.6vw, 19px);
  line-height: 1.75;
  color: rgba(255, 255, 255, 0.8);
}

.hero__actions {
  margin-top: 36px;
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.hero__Controls {
  margin-top: 56px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 240px));
  gap: 16px;
}

.hero__indicator {
  text-align: left;
  padding: 10px 0;
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.55);
  cursor: pointer;
  transition: color 0.2s ease;
}

.hero__indicator.is-active {
  color: #ffffff;
}

.hero__indicator-num {
  font-size: 11px;
  font-weight: 700;
  margin-right: 8px;
  color: #a5b4fc;
}

.hero__indicator-label {
  font-size: 13px;
  font-weight: 500;
}

.hero__indicator-bar {
  display: block;
  margin-top: 8px;
  height: 2px;
  background: rgba(255, 255, 255, 0.18);
  border-radius: 999px;
  overflow: hidden;
}

.hero__indicator-fill {
  display: block;
  height: 100%;
  background: #8b5cf6;
  transition: width 0.12s linear;
}

/* 企业关键指标 */
.metrics-section {
  border-bottom: 1px solid var(--c-border);
}

.metrics-grid {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 32px;
}

.metric-item {
  padding-left: 24px;
  border-left: 2px solid rgba(91, 75, 232, 0.22);
}

.metric-item__val-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.metric-item__value {
  font-size: clamp(38px, 4.2vw, 54px);
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1.05;
  color: var(--c-text);
}

.metric-item__unit {
  font-size: 22px;
  font-weight: 700;
  color: var(--c-brand);
}

.metric-item__label {
  margin-top: 10px;
  font-size: 15px;
  font-weight: 600;
  color: var(--c-text);
}

.metric-item__desc {
  margin-top: 4px;
  font-size: 13px;
  color: var(--c-text-muted);
}

/* 产品与服务分组 */
.service-groups {
  display: flex;
  flex-direction: column;
  gap: 72px;
}

.service-group__lead {
  display: grid;
  grid-template-columns: 1fr 1.1fr;
  gap: 40px;
  align-items: center;
  margin-bottom: 32px;
}

.service-group.is-reversed .service-group__lead {
  direction: rtl;
}

.service-group.is-reversed .service-group__meta {
  direction: ltr;
}

.service-group__index {
  font-size: 12px;
  font-weight: 700;
  color: var(--c-brand);
  letter-spacing: 0.14em;
}

.service-group__title {
  margin-top: 8px;
  font-size: clamp(24px, 3vw, 34px);
}

.service-group__desc {
  margin-top: 12px;
  font-size: 15.5px;
  color: var(--c-text-muted);
  line-height: 1.8;
}

.service-group__more {
  margin-top: 18px;
}

.service-group__banner {
  aspect-ratio: 16 / 9;
  max-height: 320px;
}

.service-card {
  background: #ffffff;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.28s var(--ease-out), box-shadow 0.28s var(--ease-out), border-color 0.28s ease;
}

.service-card:hover {
  transform: translateY(-6px);
  border-color: rgba(91, 75, 232, 0.35);
  box-shadow: 0 20px 44px rgba(10, 12, 24, 0.08);
}

.service-card__media {
  aspect-ratio: 16 / 9;
  border-radius: 0;
}

.service-card__body {
  padding: 22px 24px 26px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.service-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.service-card__arrow {
  color: var(--c-text-subtle);
  transition: transform 0.2s ease, color 0.2s ease;
}

.service-card:hover .service-card__arrow {
  transform: translate(3px, -3px);
  color: var(--c-brand);
}

.service-card__title {
  font-size: 19px;
  font-weight: 600;
}

.service-card__summary {
  margin-top: 8px;
  font-size: 14px;
  color: var(--c-text-muted);
  line-height: 1.75;
}

/* 客户案例 */
.industry-pills {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 32px;
}

.industry-pill {
  padding: 7px 18px;
  border-radius: 999px;
  border: 1px solid var(--c-border-strong);
  background: #ffffff;
  font-size: 13.5px;
  color: var(--c-text-muted);
  cursor: pointer;
  transition: all 0.2s ease;
}

.industry-pill.is-active {
  background: var(--c-brand);
  border-color: var(--c-brand);
  color: #ffffff;
}

.case-card {
  background: #ffffff;
  border-radius: var(--radius);
  border: 1px solid var(--c-border);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.28s var(--ease-out), box-shadow 0.28s var(--ease-out);
}

.case-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 22px 48px rgba(10, 12, 24, 0.09);
}

.case-card__media {
  aspect-ratio: 16 / 10;
  border-radius: 0;
}

.case-card__badge {
  position: absolute;
  top: 14px;
  left: 14px;
  padding: 4px 12px;
  border-radius: 999px;
  background: rgba(8, 8, 12, 0.75);
  backdrop-filter: blur(6px);
  color: #ffffff;
  font-size: 12px;
  font-weight: 500;
}

.case-card__body {
  padding: 24px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.case-card__customer {
  font-size: 12.5px;
  font-weight: 600;
  color: var(--c-brand);
  margin-bottom: 6px;
}

.case-card__title {
  font-size: 19px;
  line-height: 1.45;
}

.case-card__summary {
  margin-top: 8px;
  font-size: 14px;
  color: var(--c-text-muted);
  line-height: 1.75;
}

.case-outcomes {
  margin: auto 0 0;
  padding-top: 18px;
  border-top: 1px solid var(--c-border);
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.case-outcomes__val {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: var(--c-brand);
}

.case-outcomes__lbl {
  font-size: 12px;
  color: var(--c-text-muted);
}

/* 客户墙与跑马灯 */
.logo-mode-switch {
  display: flex;
  gap: 6px;
  background: var(--c-surface);
  padding: 4px;
  border-radius: 10px;
}

.mode-btn {
  padding: 6px 12px;
  border-radius: 7px;
  font-size: 12.5px;
  color: var(--c-text-muted);
  cursor: pointer;
}

.mode-btn.is-active {
  background: #ffffff;
  color: var(--c-text);
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.logo-marquee {
  overflow: hidden;
  padding: 12px 0;
  mask-image: linear-gradient(90deg, transparent 0%, #000 8%, #000 92%, transparent 100%);
}

.logo-marquee__track {
  display: flex;
  gap: 20px;
  width: max-content;
  animation: marquee-scroll 32s linear infinite;
}

.logo-marquee:hover .logo-marquee__track {
  animation-play-state: paused;
}

@keyframes marquee-scroll {
  from {
    transform: translateX(0);
  }
  to {
    transform: translateX(-50%);
  }
}

.logo-tile {
  width: 200px;
  height: 92px;
  border-radius: 14px;
  border: 1px solid var(--c-border);
  background: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 16px;
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.logo-tile:hover {
  border-color: var(--c-brand);
  transform: translateY(-2px);
}

.logo-tile img {
  max-width: 130px;
  max-height: 40px;
  object-fit: contain;
}

.logo-tile__name {
  font-size: 11.5px;
  color: var(--c-text-subtle);
}

.logo-grid {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.logo-grid__link {
  height: 92px;
  border: 1px solid var(--c-border);
  border-radius: 14px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.logo-grid__link img {
  max-width: 136px;
  max-height: 44px;
  object-fit: contain;
}

/* 新闻卡片 */
.news-card {
  background: #ffffff;
  border-radius: var(--radius);
  border: 1px solid var(--c-border);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.25s var(--ease-out), box-shadow 0.25s var(--ease-out);
}

.news-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 18px 40px rgba(10, 12, 24, 0.08);
}

.news-card__media {
  aspect-ratio: 16 / 9;
  border-radius: 0;
}

.news-card__body {
  padding: 22px 24px 26px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.news-card__date {
  font-size: 12.5px;
  color: var(--c-text-subtle);
}

.news-card__title {
  margin-top: 8px;
  font-size: 18px;
  line-height: 1.5;
}

.news-card__summary {
  margin-top: 8px;
  font-size: 14px;
  color: var(--c-text-muted);
  line-height: 1.75;
  flex: 1;
}

.news-card__more {
  margin-top: 16px;
}

/* 底部 ROI 快捷测算横幅 + 咨询区 */
.roi-inline-card {
  background: linear-gradient(135deg, rgba(91, 75, 232, 0.22) 0%, rgba(30, 27, 75, 0.65) 100%);
  border: 1px solid rgba(165, 180, 252, 0.24);
  border-radius: 22px;
  padding: 32px 36px;
  margin-bottom: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
  flex-wrap: wrap;
}

.roi-inline-card__left {
  flex: 1;
  min-width: 280px;
}

.roi-inline-card__title {
  font-size: 22px;
  color: #ffffff;
  margin-bottom: 16px;
}

.roi-inline-card__slider-row {
  display: flex;
  align-items: center;
  gap: 18px;
  flex-wrap: wrap;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
}

.roi-inline-slider {
  width: min(320px, 100%);
  accent-color: #8b5cf6;
}

.roi-inline-card__right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.roi-inline-card__lbl {
  font-size: 12.5px;
  color: rgba(255, 255, 255, 0.7);
}

.roi-inline-card__val {
  font-size: 36px;
  font-weight: 800;
  color: #38bdf8;
  line-height: 1.1;
}

.consult-cta-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
  flex-wrap: wrap;
}

.consult-cta-box__title {
  font-size: clamp(28px, 3.6vw, 44px);
  color: #ffffff;
}

.consult-cta-box__sub {
  margin-top: 12px;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.74);
  max-width: 600px;
}

@media (max-width: 860px) {
  .service-group__lead {
    grid-template-columns: 1fr;
  }
  .roi-inline-card__right {
    align-items: flex-start;
  }
}
</style>
