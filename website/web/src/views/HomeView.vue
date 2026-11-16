<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import ConsultForm from '@/components/ConsultForm.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getHome, getSite } from '@/api/public'
import { openConsult } from '@/composables/consult'
import { usePageMeta } from '@/composables/usePageMeta'
import { useRealtimeSync } from '@/composables/useRealtimeSync'
import { vReveal } from '@/directives/reveal'
import type {
  HomeCard,
  HomeCustomer,
  HomeHeroItem,
  HomeLink,
  HomeMetric,
  HomeNewsItem,
  HomeServiceGroup,
  HomeView,
} from '@/data/types'

const router = useRouter()
const data = ref<HomeView | null>(null)
const siteName = ref('企业官网')
const loading = ref(true)
const loadFailed = ref(false)
const activeServiceIdx = ref(0)
const activeSlideIdx = ref(0)
let slideTimer: ReturnType<typeof setInterval> | null = null

const heroSlides = computed<HomeHeroItem[]>(() => {
  const items = data.value?.hero?.items
  if (!Array.isArray(items)) return []
  return items.filter((item) => item && (item.title || item.subtitle || item.desktopImage))
})

const currentSlide = computed<HomeHeroItem | null>(
  () => heroSlides.value[activeSlideIdx.value] ?? heroSlides.value[0] ?? null,
)

usePageMeta(() => ({
  title: currentSlide.value?.title || '首页',
  description: currentSlide.value?.subtitle || '',
}))

const DEFAULT_ORDER = [
  'hero',
  'metrics',
  'services',
  'cases',
  'customers',
  'news',
  'consultation',
]

const sectionOrder = computed<string[]>(() => {
  const order = data.value?.sectionOrder
  return Array.isArray(order) && order.length > 0 ? order : DEFAULT_ORDER
})

const metricsBlock = computed(() => data.value?.metrics ?? null)
const metricsList = computed<HomeMetric[]>(() => {
  const list = metricsBlock.value?.items
  if (!Array.isArray(list)) return []
  return list.filter((m) => m && m.displayValue && m.label)
})

const servicesBlock = computed(() => data.value?.services ?? null)
const serviceGroups = computed<HomeServiceGroup[]>(() => {
  const groups = servicesBlock.value?.groups
  if (!Array.isArray(groups)) return []
  return groups.filter((g) => g && g.title)
})

const currentService = computed<HomeServiceGroup | null>(
  () => serviceGroups.value[activeServiceIdx.value] ?? serviceGroups.value[0] ?? null,
)

const casesBlock = computed(() => data.value?.cases ?? null)
const casesList = computed<HomeCard[]>(() => {
  const list = casesBlock.value?.items
  return Array.isArray(list) ? list : []
})

const customersBlock = computed(() => data.value?.customers ?? null)
const customersList = computed<HomeCustomer[]>(() => {
  const list = customersBlock.value?.items
  if (!Array.isArray(list)) return []
  return list.filter((c) => c && (c.name || c.logo?.url))
})

const newsBlock = computed(() => data.value?.news ?? null)
const newsList = computed<HomeNewsItem[]>(() => {
  const list = newsBlock.value?.items
  return Array.isArray(list) ? list : []
})

const consultationConfig = computed(() => data.value?.consultation ?? null)

const hasAnyPublishedSection = computed(() => {
  return (
    heroSlides.value.length > 0 ||
    metricsList.value.length > 0 ||
    serviceGroups.value.length > 0 ||
    casesList.value.length > 0 ||
    customersList.value.length > 0 ||
    newsList.value.length > 0
  )
})

function resolveLinkUrl(link?: HomeLink | null): string {
  if (!link) return ''
  if (link.url) return link.url
  if (link.type === 'CONTENT' && link.contentType && link.contentId) {
    const map: Record<string, string> = {
      PRODUCT: '/products/',
      SOLUTION: '/solutions/',
      CASE: '/cases/',
      NEWS: '/news/',
    }
    return `${map[link.contentType] || '/products/'}${link.contentId}`
  }
  return ''
}

function handleHeroCta(slide: HomeHeroItem | null) {
  const targetUrl = resolveLinkUrl(slide?.link)
  if (targetUrl) {
    if (/^https?:\/\//i.test(targetUrl) || slide?.link?.newWindow) {
      window.open(targetUrl, '_blank', 'noopener,noreferrer')
    } else {
      router.push(targetUrl)
    }
    return
  }
  openConsult({
    sourcePosition: 'HOME',
    title: slide?.buttonText || '预约方案咨询',
  })
}

function setupSlideRotation() {
  if (slideTimer) {
    clearInterval(slideTimer)
    slideTimer = null
  }
  if (heroSlides.value.length > 1) {
    slideTimer = setInterval(() => {
      activeSlideIdx.value = (activeSlideIdx.value + 1) % heroSlides.value.length
    }, 6500)
  }
}

async function loadHomeData() {
  loading.value = true
  loadFailed.value = false
  try {
    const [homeRes, siteRes] = await Promise.all([
      getHome(),
      getSite().catch(() => null),
    ])
    data.value = homeRes
    if (siteRes?.siteName) siteName.value = siteRes.siteName
    setupSlideRotation()
  } catch {
    loadFailed.value = true
  } finally {
    loading.value = false
  }
}

useRealtimeSync(['homeVersion', 'siteConfigVersion', 'contentVersion'], () => {
  void loadHomeData()
})

onMounted(loadHomeData)

onBeforeUnmount(() => {
  if (slideTimer) clearInterval(slideTimer)
})
</script>

<template>
  <div class="home-page">
    <div v-if="loading" class="container state" aria-busy="true">
      <div class="skeleton"></div>
    </div>

    <div v-else-if="loadFailed" class="container state">
      <EmptyState title="首页数据加载失败" description="请检查网络连接或稍后刷新重试。">
        <button type="button" class="btn btn--primary" @click="loadHomeData">重新加载</button>
      </EmptyState>
    </div>

    <div v-else-if="!hasAnyPublishedSection" class="home-unconfigured">
      <div class="container">
        <EmptyState
          title="首页内容尚未发布"
          description="管理员可在后台「首页模块管理」配置首屏焦点图、核心能力、标杆案例与指标并点击发布。"
        >
          <RouterLink to="/products" class="btn btn--primary">浏览产品中心</RouterLink>
        </EmptyState>
      </div>
    </div>

    <template v-else>
      <template v-for="secKey in sectionOrder" :key="secKey">
        <!-- 1. 首屏 Hero -->
        <section
          v-if="secKey === 'hero' && currentSlide"
          class="home-hero"
        >
          <div v-if="currentSlide.desktopImage" class="home-hero__bg" aria-hidden="true">
            <video
              v-if="currentSlide.desktopImage.type === 'VIDEO'"
              :src="currentSlide.desktopImage.url"
              class="home-hero__media"
              autoplay
              muted
              loop
              playsinline
            ></video>
            <img
              v-else
              :src="currentSlide.desktopImage.url"
              :alt="currentSlide.desktopImage.alt || currentSlide.title"
              class="home-hero__media"
            />
            <div class="home-hero__overlay"></div>
          </div>

          <div class="container home-hero__inner">
            <div class="home-hero__content" v-reveal>
              <p v-if="currentSlide.kicker || currentSlide.badgeText" class="home-hero__eyebrow">
                {{ currentSlide.kicker || currentSlide.badgeText }}
              </p>
              <h1 v-if="currentSlide.title" class="home-hero__title">{{ currentSlide.title }}</h1>
              <p v-if="currentSlide.subtitle" class="home-hero__subtitle">{{ currentSlide.subtitle }}</p>

              <div class="home-hero__actions">
                <button type="button" class="btn btn--primary home-hero__btn" @click="handleHeroCta(currentSlide)">
                  {{ currentSlide.buttonText || '预约方案咨询' }}
                </button>
                <RouterLink to="/products" class="btn btn--outline-light home-hero__btn">
                  浏览产品体系
                </RouterLink>
              </div>
            </div>

            <!-- 多张轮播指示器 -->
            <div v-if="heroSlides.length > 1" class="home-hero__dots" role="tablist" aria-label="首屏轮播">
              <button
                v-for="(slide, idx) in heroSlides"
                :key="slide.key || idx"
                type="button"
                class="home-hero__dot"
                :class="{ 'is-active': activeSlideIdx === idx }"
                :aria-label="slide.title || `第 ${idx + 1} 屏`"
                @click="activeSlideIdx = idx"
              ></button>
            </div>
          </div>
        </section>

        <!-- 2. 关键量化指标带 (Metrics) -->
        <section
          v-else-if="secKey === 'metrics' && metricsBlock?.enabled !== false && metricsList.length > 0"
          class="home-metrics"
        >
          <div class="container">
            <div class="home-metrics__grid">
              <div
                v-for="(metric, idx) in metricsList"
                :key="metric.key || metric.label + idx"
                v-reveal="idx * 50"
                class="home-metric-card"
              >
                <div class="home-metric-card__value">
                  <span>{{ metric.displayValue }}</span>
                  <small v-if="metric.unit && !String(metric.displayValue || '').includes(metric.unit)">
                    {{ metric.unit }}
                  </small>
                </div>
                <p class="home-metric-card__label">{{ metric.label }}</p>
                <p v-if="metric.description" class="home-metric-card__note">{{ metric.description }}</p>
              </div>
            </div>
          </div>
        </section>

        <!-- 3. 核心产品与解决方案亮点 (Services) -->
        <section
          v-else-if="secKey === 'services' && servicesBlock?.enabled !== false && serviceGroups.length > 0"
          class="section home-services"
        >
          <div class="container">
            <div class="section-head">
              <div>
                <p class="kicker">CAPABILITIES</p>
                <h2 class="section-title">{{ servicesBlock?.title || '核心产品与解决方案' }}</h2>
                <p v-if="servicesBlock?.subtitle" class="section-sub">{{ servicesBlock.subtitle }}</p>
              </div>
              <RouterLink to="/products" class="link-more">查看全部产品</RouterLink>
            </div>

            <div class="home-services__tabs" role="tablist">
              <button
                v-for="(srv, idx) in serviceGroups"
                :key="srv.key || srv.title"
                type="button"
                role="tab"
                class="home-services__tab"
                :class="{ 'is-active': activeServiceIdx === idx }"
                :aria-selected="activeServiceIdx === idx"
                @click="activeServiceIdx = idx"
              >
                {{ srv.title }}
              </button>
            </div>

            <div v-if="currentService" class="home-service-panel" v-reveal>
              <div class="home-service-panel__text">
                <span v-if="currentService.tag" class="tag">{{ currentService.tag }}</span>
                <h3 class="home-service-panel__title">{{ currentService.title }}</h3>
                <p class="home-service-panel__desc">{{ currentService.description }}</p>

                <ul v-if="currentService.highlights?.length" class="home-service-panel__sublist">
                  <li v-for="hl in currentService.highlights" :key="hl">{{ hl }}</li>
                </ul>

                <div v-if="currentService.items?.length" class="home-service-panel__cards">
                  <RouterLink
                    v-for="subCard in currentService.items"
                    :key="subCard.id"
                    :to="subCard.url || `/products/${subCard.id}`"
                    class="home-service-subcard"
                  >
                    <strong>{{ subCard.title }}</strong>
                    <span v-if="subCard.summary">{{ subCard.summary }}</span>
                  </RouterLink>
                </div>

                <div class="home-service-panel__actions">
                  <RouterLink
                    v-if="resolveLinkUrl(currentService.moreLink)"
                    :to="resolveLinkUrl(currentService.moreLink)"
                    class="btn btn--primary"
                  >
                    了解详情
                  </RouterLink>
                  <button
                    type="button"
                    class="btn btn--ghost"
                    @click="openConsult({ sourcePosition: 'HOME', title: `咨询：${currentService.title}` })"
                  >
                    预约演示
                  </button>
                </div>
              </div>

              <div v-if="currentService.image?.url" class="home-service-panel__media">
                <img
                  :src="currentService.image.url"
                  :alt="currentService.image.alt || currentService.title"
                  loading="lazy"
                />
              </div>
            </div>
          </div>
        </section>

        <!-- 4. 标杆客户案例 (Cases) -->
        <section
          v-else-if="secKey === 'cases' && casesBlock?.enabled !== false && casesList.length > 0"
          class="section section--surface home-cases"
        >
          <div class="container">
            <div class="section-head">
              <div>
                <p class="kicker">CUSTOMER STORIES</p>
                <h2 class="section-title">{{ casesBlock?.title || '标杆客户实践案例' }}</h2>
                <p v-if="casesBlock?.subtitle" class="section-sub">{{ casesBlock.subtitle }}</p>
              </div>
              <RouterLink :to="casesBlock?.moreUrl || '/cases'" class="link-more">全部客户案例</RouterLink>
            </div>

            <div class="grid grid--3">
              <RouterLink
                v-for="(item, idx) in casesList"
                :key="item.id"
                :to="item.url || `/cases/${item.id}`"
                v-reveal="idx * 60"
                class="card home-case-card"
              >
                <div v-if="item.cover?.url" class="home-case-card__media media-frame">
                  <img :src="item.cover.url" :alt="item.cover.alt || item.title" loading="lazy" />
                </div>
                <div class="card__body">
                  <div class="home-case-card__meta">
                    <span v-if="item.industry" class="tag">{{ item.industry }}</span>
                    <span v-if="item.customerName" class="home-case-card__customer">
                      {{ item.customerName }}
                    </span>
                  </div>
                  <h3 class="card__title">{{ item.title }}</h3>
                  <p v-if="item.summary" class="card__desc">{{ item.summary }}</p>

                  <dl v-if="item.outcomes?.length" class="home-case-card__outcomes">
                    <div v-for="o in item.outcomes.slice(0, 2)" :key="o.label">
                      <dt>{{ o.label }}</dt>
                      <dd>{{ o.displayValue }}</dd>
                    </div>
                  </dl>
                </div>
              </RouterLink>
            </div>
          </div>
        </section>

        <!-- 5. 合作客户标识墙 (Customers) -->
        <section
          v-else-if="secKey === 'customers' && customersBlock?.enabled !== false && customersList.length > 0"
          class="section section--tight home-customers"
        >
          <div class="container">
            <div class="section-head">
              <div>
                <p class="kicker">TRUSTED BY</p>
                <h2 class="section-title">{{ customersBlock?.title || '合作伙伴与服务客户' }}</h2>
                <p v-if="customersBlock?.subtitle" class="section-sub">{{ customersBlock.subtitle }}</p>
              </div>
            </div>
            <div class="home-customers__grid">
              <div
                v-for="(cust, idx) in customersList"
                :key="cust.id || cust.name + idx"
                class="home-customer-item"
              >
                <img
                  v-if="cust.logo?.url"
                  :src="cust.logo.url"
                  :alt="cust.logo.alt || cust.name"
                  class="home-customer-item__img"
                  loading="lazy"
                />
                <span v-else class="home-customer-item__name">{{ cust.name }}</span>
              </div>
            </div>
          </div>
        </section>

        <!-- 6. 最新动态与洞察 (News) -->
        <section
          v-else-if="secKey === 'news' && newsBlock?.enabled !== false && newsList.length > 0"
          class="section home-news"
        >
          <div class="container">
            <div class="section-head">
              <div>
                <p class="kicker">NEWS & INSIGHTS</p>
                <h2 class="section-title">{{ newsBlock?.title || '最新动态与技术洞察' }}</h2>
                <p v-if="newsBlock?.subtitle" class="section-sub">{{ newsBlock.subtitle }}</p>
              </div>
              <RouterLink :to="newsBlock?.moreUrl || '/news'" class="link-more">更多资讯</RouterLink>
            </div>

            <div class="grid grid--3">
              <RouterLink
                v-for="(newsItem, idx) in newsList.slice(0, 3)"
                :key="newsItem.id"
                :to="newsItem.url || `/news/${newsItem.id}`"
                v-reveal="idx * 60"
                class="card home-news-card"
              >
                <div v-if="newsItem.cover?.url" class="home-news-card__media media-frame">
                  <img
                    :src="newsItem.cover.url"
                    :alt="newsItem.cover.alt || newsItem.title"
                    loading="lazy"
                  />
                </div>
                <div class="card__body">
                  <div class="home-news-card__meta">
                    <time v-if="newsItem.displayTime" :datetime="newsItem.displayTime">
                      {{ newsItem.displayTime }}
                    </time>
                  </div>
                  <h3 class="card__title">{{ newsItem.title }}</h3>
                  <p v-if="newsItem.summary" class="card__desc">{{ newsItem.summary }}</p>
                </div>
              </RouterLink>
            </div>
          </div>
        </section>

        <!-- 7. 底部留资转化区 (Consultation) -->
        <section
          v-else-if="secKey === 'consultation' && consultationConfig"
          class="section section--surface home-consult"
        >
          <div class="container home-consult__grid">
            <div class="home-consult__info" v-reveal>
              <p class="kicker">GET IN TOUCH</p>
              <h2 class="section-title">{{ consultationConfig.title || '预约专属方案沟通' }}</h2>
              <p v-if="consultationConfig.subtitle" class="section-sub">
                {{ consultationConfig.subtitle }}
              </p>
              <p v-else class="section-sub">
                提交您的业务场景与联系方式，我们的行业顾问将为您提供针对性的架构建议与演示服务。
              </p>
            </div>
            <div class="home-consult__form-card" v-reveal="80">
              <ConsultForm source-position="HOME" />
            </div>
          </div>
        </section>
      </template>
    </template>
  </div>
</template>

<style scoped>
.home-unconfigured {
  padding: 120px 0 80px;
}

.home-hero {
  position: relative;
  min-height: 540px;
  padding: 140px 0 96px;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 65%, #1d4ed8 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  overflow: hidden;
}

.home-hero__bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.home-hero__media {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.home-hero__overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(15, 23, 42, 0.88) 0%, rgba(15, 23, 42, 0.55) 100%);
}

.home-hero__inner {
  position: relative;
  z-index: 1;
}

.home-hero__content {
  max-width: 720px;
}

.home-hero__eyebrow {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #93c5fd;
  margin-bottom: 14px;
}

.home-hero__title {
  font-size: clamp(32px, 4.4vw, 52px);
  font-weight: 700;
  line-height: 1.15;
  color: #ffffff;
  margin-bottom: 20px;
}

.home-hero__subtitle {
  font-size: 17px;
  line-height: 1.75;
  color: #cbd5e1;
  margin-bottom: 32px;
}

.home-hero__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
}

.home-hero__btn {
  min-width: 148px;
}

.home-hero__dots {
  display: flex;
  gap: 8px;
  margin-top: 40px;
}

.home-hero__dot {
  width: 28px;
  height: 4px;
  border: none;
  border-radius: 2px;
  background: rgba(255, 255, 255, 0.3);
  cursor: pointer;
  padding: 0;
}

.home-hero__dot.is-active {
  background: #ffffff;
}

/* 指标带 */
.home-metrics {
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  padding: 36px 0;
}

.home-metrics__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 24px;
}

.home-metric-card {
  padding: 12px 16px;
  border-left: 3px solid var(--c-brand);
}

.home-metric-card__value {
  font-size: clamp(26px, 3vw, 36px);
  font-weight: 700;
  color: #0f172a;
  line-height: 1.2;
}

.home-metric-card__value small {
  font-size: 15px;
  font-weight: 600;
  color: var(--c-brand);
  margin-left: 4px;
}

.home-metric-card__label {
  margin-top: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #475569;
}

.home-metric-card__note {
  margin-top: 2px;
  font-size: 12px;
  color: #94a3b8;
}

/* 核心产品与服务 */
.home-services__tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 28px;
}

.home-services__tab {
  padding: 10px 20px;
  border-radius: 6px;
  border: 1px solid #cbd5e1;
  background: #ffffff;
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  cursor: pointer;
  transition: all 0.18s ease;
}

.home-services__tab:hover {
  border-color: var(--c-brand);
  color: var(--c-brand);
}

.home-services__tab.is-active {
  background: var(--c-brand);
  border-color: var(--c-brand);
  color: #ffffff;
}

.home-service-panel {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 40px;
  align-items: center;
  padding: 36px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
}

.home-service-panel__title {
  font-size: 24px;
  font-weight: 700;
  margin: 12px 0;
}

.home-service-panel__desc {
  font-size: 15px;
  color: #475569;
  line-height: 1.8;
  margin-bottom: 20px;
}

.home-service-panel__sublist {
  margin: 0 0 20px;
  padding-left: 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 14px;
  color: #334155;
}

.home-service-panel__cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
  margin-bottom: 24px;
}

.home-service-subcard {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px 14px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 13px;
}

.home-service-subcard:hover {
  border-color: var(--c-brand);
}

.home-service-subcard strong {
  color: #0f172a;
  font-size: 14px;
}

.home-service-subcard span {
  color: #64748b;
}

.home-service-panel__actions {
  display: flex;
  gap: 12px;
}

.home-service-panel__media img {
  width: 100%;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

/* 案例卡片 */
.home-case-card__media,
.home-news-card__media {
  aspect-ratio: 16 / 10;
  border-radius: 0;
}

.home-case-card__meta,
.home-news-card__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 10px;
  font-size: 12px;
  color: #64748b;
}

.home-case-card__customer {
  font-weight: 600;
  color: #334155;
}

.home-case-card__outcomes {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  gap: 20px;
}

.home-case-card__outcomes dt {
  font-size: 12px;
  color: #64748b;
}

.home-case-card__outcomes dd {
  margin: 2px 0 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--c-brand);
}

/* 客户墙 */
.home-customers__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
}

.home-customer-item {
  height: 72px;
  padding: 12px 18px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.home-customer-item__img {
  max-height: 40px;
  max-width: 120px;
  object-fit: contain;
}

.home-customer-item__name {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
}

/* 底部咨询 */
.home-consult__grid {
  display: grid;
  grid-template-columns: 1fr 460px;
  gap: 56px;
  align-items: start;
}

.home-consult__form-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
}

@media (max-width: 960px) {
  .home-service-panel,
  .home-consult__grid {
    grid-template-columns: 1fr;
  }
}
</style>
