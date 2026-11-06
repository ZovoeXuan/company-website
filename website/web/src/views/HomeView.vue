<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ApiError } from '@/api/http'
import { getHome } from '@/api/public'
import { openConsult } from '@/composables/consult'
import { usePageMeta } from '@/composables/usePageMeta'
import { vReveal } from '@/directives/reveal'
import EmptyState from '@/components/EmptyState.vue'
import type { HomeLink, HomeView } from '@/data/types'

const router = useRouter()
const data = ref<HomeView | null>(null)
const loading = ref(true)
const error = ref('')
const heroIndex = ref(0)
let heroTimer: number | null = null

usePageMeta(() => ({
  title: '首页',
  description: '致力于以数字化与智能技术助力企业高效运营与持续增长。',
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

function startHeroRotation() {
  stopHeroRotation()
  if (heroes.value.length <= 1) return
  heroTimer = window.setInterval(() => {
    heroIndex.value = (heroIndex.value + 1) % heroes.value.length
  }, 6500)
}

function stopHeroRotation() {
  if (heroTimer) {
    clearInterval(heroTimer)
    heroTimer = null
  }
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
    startHeroRotation()
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
onBeforeUnmount(stopHeroRotation)
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
      <!-- 1. 首屏 Hero（干净大气：去除顶部胶囊与观看90秒按钮、去除底部 01/02 文字条，滑入缓慢浮现） -->
      <section v-if="currentHero" class="hero">
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
          <h1 v-reveal class="hero__title">{{ currentHero.title }}</h1>
          <p v-if="currentHero.subtitle" v-reveal="100" class="hero__subtitle">
            {{ currentHero.subtitle }}
          </p>

          <div v-if="currentHero.buttonText && currentHero.link" v-reveal="180" class="hero__actions">
            <button
              type="button"
              class="btn btn--outline-light hero__btn"
              @click="handleAction(currentHero.link, 'HOME_HERO')"
            >
              <span>{{ currentHero.buttonText }}</span>
              <span aria-hidden="true">→</span>
            </button>
          </div>
        </div>
      </section>

      <!-- 按后台编排的 sectionOrder 动态渲染各大区块，每块与内部卡片均配流动式 v-reveal -->
      <template v-for="secKey in orderedSections" :key="secKey">
        <!-- A. 企业关键指标 (metrics) -->
        <section
          v-if="secKey === 'metrics' && data.metrics?.items?.length"
          id="metrics"
          class="section section--tight metrics-section"
        >
          <div class="container container--wide">
            <div v-if="data.metrics.title || data.metrics.subtitle" v-reveal class="section-head">
              <div>
                <h2 v-if="data.metrics.title" class="section-title">{{ data.metrics.title }}</h2>
                <p v-if="data.metrics.subtitle" class="section-sub">{{ data.metrics.subtitle }}</p>
              </div>
            </div>

            <ul class="metrics-grid">
              <li
                v-for="(item, mIdx) in data.metrics.items"
                :key="item.key"
                v-reveal="mIdx * 90"
                class="metric-item"
              >
                <div class="metric-item__val-row">
                  <span class="metric-item__value">{{ item.displayValue }}</span>
                  <span v-if="item.unit" class="metric-item__unit">{{ item.unit }}</span>
                </div>
                <p class="metric-item__label">{{ item.label }}</p>
                <p v-if="item.description" class="metric-item__desc">{{ item.description }}</p>
              </li>
            </ul>
          </div>
        </section>

        <!-- B. 核心产品与解决方案 (services) — 回归清爽经典结构：去除分组旁边丑陋的大配图框，直接展示分组标题与三列卡片 -->
        <section
          v-else-if="secKey === 'services' && data.services?.groups?.length"
          id="services"
          class="section services-section"
        >
          <div class="container container--wide">
            <div v-reveal class="section-head">
              <div>
                <span class="kicker">PRODUCTS & SOLUTIONS</span>
                <h2 class="section-title">{{ data.services.title || '核心产品与解决方案' }}</h2>
                <p v-if="data.services.subtitle" class="section-sub">{{ data.services.subtitle }}</p>
              </div>
              <RouterLink to="/products" class="link-more section-head__link">查看全部</RouterLink>
            </div>

            <div class="service-groups">
              <div
                v-for="(group, gIdx) in data.services.groups"
                :key="group.key"
                v-reveal="gIdx * 80"
                class="service-group"
              >
                <div class="service-group__header">
                  <div>
                    <span class="service-group__index">0{{ gIdx + 1 }}</span>
                    <h3 class="service-group__title">{{ group.title }}</h3>
                    <p v-if="group.description" class="service-group__desc">{{ group.description }}</p>
                  </div>
                  <button
                    v-if="group.moreLink"
                    type="button"
                    class="btn btn--pill service-group__more"
                    @click="handleAction(group.moreLink, 'OTHER_CTA')"
                  >
                    了解更多 →
                  </button>
                </div>

                <div class="grid grid--3 service-group__cards">
                  <RouterLink
                    v-for="(card, cIdx) in group.items"
                    :key="card.id"
                    v-reveal="cIdx * 90"
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
                      <h4 class="service-card__title">{{ card.title }}</h4>
                      <p v-if="card.summary" class="service-card__summary">{{ card.summary }}</p>
                      <span class="service-card__link">了解详情 →</span>
                    </div>
                  </RouterLink>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- C. 客户案例 (cases) -->
        <section
          v-else-if="secKey === 'cases' && data.cases?.items?.length"
          id="cases"
          class="section section--surface cases-section"
        >
          <div class="container container--wide">
            <div v-reveal class="section-head">
              <div>
                <span class="kicker">CUSTOMER CASES</span>
                <h2 class="section-title">{{ data.cases.title || '客户案例' }}</h2>
                <p v-if="data.cases.subtitle" class="section-sub">{{ data.cases.subtitle }}</p>
              </div>
              <RouterLink :to="data.cases.moreUrl || '/cases'" class="link-more section-head__link">
                查看全部案例
              </RouterLink>
            </div>

            <div class="grid grid--3">
              <RouterLink
                v-for="(item, idx) in data.cases.items"
                :key="item.id"
                v-reveal="idx * 90"
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

        <!-- D. 合作客户标识 (customers) -->
        <section
          v-else-if="secKey === 'customers' && data.customers?.items?.length"
          id="customers"
          class="section section--tight customers-section"
        >
          <div class="container container--wide">
            <div v-reveal class="section-head">
              <div>
                <span class="kicker">CUSTOMERS</span>
                <h2 class="section-title">{{ data.customers.title || '合作客户' }}</h2>
                <p v-if="data.customers.subtitle" class="section-sub">{{ data.customers.subtitle }}</p>
              </div>
            </div>

            <ul class="logo-grid">
              <li
                v-for="(cust, idx) in data.customers.items"
                :key="cust.id"
                v-reveal="(idx % 6) * 60"
                class="logo-grid__item"
              >
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

        <!-- E. 最新动态 (news) -->
        <section
          v-else-if="secKey === 'news' && data.news?.items?.length"
          id="news"
          class="section section--surface news-section"
        >
          <div class="container container--wide">
            <div v-reveal class="section-head">
              <div>
                <span class="kicker">NEWS CENTER</span>
                <h2 class="section-title">{{ data.news.title || '最新动态' }}</h2>
                <p v-if="data.news.subtitle" class="section-sub">{{ data.news.subtitle }}</p>
              </div>
              <RouterLink :to="data.news.moreUrl || '/news'" class="link-more section-head__link">
                查看更多
              </RouterLink>
            </div>

            <div class="grid grid--3">
              <RouterLink
                v-for="(item, idx) in data.news.items"
                :key="item.id"
                v-reveal="idx * 90"
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

        <!-- F. 底部咨询行动区 (consultation) — 已彻底移除 ROI 测算卡 -->
        <section
          v-else-if="secKey === 'consultation' && data.consultation"
          id="consultation"
          class="section section--dark consult-cta-section"
        >
          <div class="container container--wide">
            <div v-reveal class="consult-cta-box">
              <div>
                <h2 class="consult-cta-box__title">{{ data.consultation.title || '与我们取得联系' }}</h2>
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
                  {{ data.consultation.buttonText || '立即咨询' }}
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
  min-height: clamp(540px, 76vh, 720px);
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
    rgba(6, 8, 16, 0.88) 0%,
    rgba(6, 8, 16, 0.68) 48%,
    rgba(6, 8, 16, 0.22) 100%
  );
}

.hero__content {
  position: relative;
  z-index: 2;
  padding-top: 88px;
  padding-bottom: 72px;
}

.hero__title {
  max-width: 720px;
  font-size: clamp(36px, 4.8vw, 60px);
  line-height: 1.15;
  font-weight: 700;
  color: #ffffff;
  white-space: pre-line;
  letter-spacing: -0.03em;
}

.hero__subtitle {
  margin-top: 20px;
  max-width: 560px;
  font-size: clamp(16px, 1.5vw, 18.5px);
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.82);
}

.hero__actions {
  margin-top: 34px;
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
  border-left: 1px solid var(--c-border-strong);
}

.metric-item:first-child {
  padding-left: 0;
  border-left: none;
}

.metric-item__val-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.metric-item__value {
  font-size: clamp(38px, 4.2vw, 52px);
  font-weight: 700;
  letter-spacing: -0.03em;
  line-height: 1.05;
  color: var(--c-text);
}

.metric-item__unit {
  font-size: 22px;
  font-weight: 600;
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

/* 核心产品与方案分组（清爽标题行 + 三列卡片，无右侧多余配图框） */
.service-groups {
  display: flex;
  flex-direction: column;
  gap: 64px;
}

.service-group {
  padding-top: 32px;
  border-top: 1px solid var(--c-border);
}

.service-group:first-child {
  padding-top: 0;
  border-top: none;
}

.service-group__header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 28px;
  flex-wrap: wrap;
}

.service-group__index {
  font-size: 12px;
  font-weight: 700;
  color: var(--c-brand);
  letter-spacing: 0.14em;
}

.service-group__title {
  margin-top: 6px;
  font-size: clamp(22px, 2.6vw, 30px);
}

.service-group__desc {
  margin-top: 8px;
  max-width: 640px;
  font-size: 15px;
  color: var(--c-text-muted);
  line-height: 1.75;
}

.service-card {
  background: #ffffff;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.3s var(--ease-out), box-shadow 0.3s var(--ease-out), border-color 0.25s ease;
}

.service-card:hover {
  transform: translateY(-6px);
  border-color: rgba(91, 75, 232, 0.32);
  box-shadow: 0 20px 44px rgba(10, 12, 24, 0.08);
}

.service-card__media {
  aspect-ratio: 16 / 9;
  border-radius: 0;
}

.service-card__body {
  padding: 24px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.service-card__title {
  font-size: 19px;
  font-weight: 600;
}

.service-card__summary {
  margin-top: 10px;
  font-size: 14px;
  color: var(--c-text-muted);
  line-height: 1.75;
  flex: 1;
}

.service-card__link {
  margin-top: 18px;
  font-size: 13.5px;
  font-weight: 500;
  color: var(--c-brand);
}

/* 客户案例 */
.case-card {
  background: #ffffff;
  border-radius: var(--radius);
  border: 1px solid var(--c-border);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.3s var(--ease-out), box-shadow 0.3s var(--ease-out);
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
  background: rgba(8, 8, 12, 0.72);
  backdrop-filter: blur(6px);
  color: #ffffff;
  font-size: 12px;
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
  font-weight: 700;
  color: var(--c-brand);
}

.case-outcomes__lbl {
  font-size: 12px;
  color: var(--c-text-muted);
}

/* 客户墙 */
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
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.logo-grid__link:hover {
  border-color: var(--c-brand);
  transform: translateY(-2px);
}

.logo-grid__link img {
  max-width: 136px;
  max-height: 44px;
  object-fit: contain;
  filter: grayscale(1);
  opacity: 0.82;
  transition: filter 0.25s ease, opacity 0.25s ease;
}

.logo-grid__link:hover img {
  filter: none;
  opacity: 1;
}

/* 新闻卡片 */
.news-card {
  background: #ffffff;
  border-radius: var(--radius);
  border: 1px solid var(--c-border);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.28s var(--ease-out), box-shadow 0.28s var(--ease-out);
}

.news-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 18px 40px rgba(10, 12, 24, 0.08);
}

.news-card__media {
  aspect-ratio: 16 / 10;
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

/* 底部咨询区 */
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
</style>
