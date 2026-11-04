<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import EmptyState from '@/components/EmptyState.vue'
import { getHome } from '@/api/public'
import { openConsult } from '@/composables/consult'
import { openVideo } from '@/composables/videoModal'
import { usePageMeta } from '@/composables/usePageMeta'
import type { HomeCard, HomeHeroItem, HomeLink, HomeView, MediaRef } from '@/data/types'

const data = ref<HomeView | null>(null)
const failed = ref(false)
const heroIndex = ref(0)
const activeGroup = ref(0)

let autoSlideTimer: number | null = null

function startAutoSlide() {
  stopAutoSlide()
  if (heroes.value.length <= 1) return
  autoSlideTimer = window.setInterval(() => {
    heroIndex.value = (heroIndex.value + 1) % heroes.value.length
  }, 6000)
}

function stopAutoSlide() {
  if (autoSlideTimer) {
    clearInterval(autoSlideTimer)
    autoSlideTimer = null
  }
}

function selectHero(index: number) {
  heroIndex.value = index
  startAutoSlide()
}

const heroes = computed<HomeHeroItem[]>(() => data.value?.hero.items ?? [])
const current = computed<HomeHeroItem | null>(() => heroes.value[heroIndex.value] ?? null)

usePageMeta(() => ({
  title: '工业设备数字化与能源管理',
  description: '面向制造企业的设备管理、预测性维护与能耗监测平台，帮助降低非计划停机、定位高耗能环节。',
}))

/** 非咨询动作的跳转地址；CONSULT 由弹窗承接 */
function hrefOf(link: HomeLink | null): string | null {
  if (!link || link.type === 'CONSULT') return null
  return link.url ?? null
}

function isExternal(link: HomeLink | null): boolean {
  return link?.type === 'EXTERNAL'
}

function isVideo(media: MediaRef | null | undefined): boolean {
  return media?.type === 'VIDEO'
}

function openHeroConsult() {
  if (!current.value) return
  openConsult({
    sourcePosition: 'HOME_HERO',
    contentType: current.value.link?.contentType,
    contentId: current.value.link?.contentId,
  })
}

function playSpotlightVideo() {
  openVideo({
    title: '唐山双赢物流 | G7易流数字化运营实践专访',
    url: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4'
  })
}

function openBottomConsult() {
  openConsult({
    sourcePosition: 'HOME_BOTTOM',
    contentType: data.value?.consultation.link.contentType,
    contentId: data.value?.consultation.link.contentId,
  })
}

function caseCover(item: HomeCard): string | null {
  return item.cover?.url ?? null
}

function pad(index: number): string {
  return String(index + 1).padStart(2, '0')
}

onMounted(async () => {
  try {
    data.value = await getHome()
    startAutoSlide()
  } catch {
    failed.value = true
  }
})

onBeforeUnmount(() => {
  stopAutoSlide()
})
</script>

<template>
  <div v-if="data" class="home">
    <!-- 首屏：深色电影感画面 + 巨幅标题 -->
    <section v-if="current" :key="current.key" class="hero on-dark">
      <div class="hero__media">
        <video
          v-if="isVideo(current.desktopImage)"
          :src="current.desktopImage?.url"
          :poster="current.mobileImage?.url"
          autoplay
          muted
          loop
          playsinline
          preload="metadata"
        ></video>
        <picture v-else-if="current.desktopImage">
          <source v-if="current.mobileImage" media="(max-width: 700px)" :srcset="current.mobileImage.url" />
          <img
            :src="current.desktopImage.url"
            :alt="current.desktopImage.alt || current.title"
            :width="current.desktopImage.width"
            :height="current.desktopImage.height"
            fetchpriority="high"
          />
        </picture>
        <div class="hero__veil"></div>
        <div class="hero__glow"></div>
      </div>

      <div class="container container--wide hero__inner">
        <div class="hero__copy">
          <p class="kicker hero__kicker">INDUSTRIAL IOT · ENERGY INTELLIGENCE</p>
          <h1 class="hero__title">{{ current.title }}</h1>
          <p v-if="current.subtitle" class="hero__sub">{{ current.subtitle }}</p>

          <div v-if="current.buttonText && current.link" class="hero__actions">
            <button
              v-if="current.link.type === 'CONSULT'"
              type="button"
              class="btn btn--outline-light hero__btn"
              @click="openHeroConsult"
            >
              {{ current.buttonText }} <span aria-hidden="true">→</span>
            </button>
            <a
              v-else-if="isExternal(current.link)"
              :href="hrefOf(current.link) ?? '#'"
              class="btn btn--outline-light hero__btn"
              :target="current.link?.newWindow ? '_blank' : undefined"
              :rel="current.link?.newWindow ? 'noopener noreferrer' : undefined"
            >
              {{ current.buttonText }} <span aria-hidden="true">→</span>
            </a>
            <RouterLink v-else :to="hrefOf(current.link) ?? '/'" class="btn btn--outline-light hero__btn">
              {{ current.buttonText }} <span aria-hidden="true">→</span>
            </RouterLink>
          </div>
        </div>
      </div>

      <div class="container container--wide hero__foot">
        <div v-if="heroes.length > 1" class="hero__switch" role="tablist" aria-label="首屏轮播">
          <button
            v-for="(item, index) in heroes"
            :key="item.key"
            type="button"
            class="hero__dot"
            :class="{ 'is-active': index === heroIndex }"
            :aria-label="`切换到第 ${index + 1} 屏：${item.title}`"
            :aria-selected="index === heroIndex"
            role="tab"
            @click="selectHero(index)"
          >
            <span class="hero__dot-bar">
              <span v-if="index === heroIndex" :key="`fill-${heroIndex}`" class="hero__dot-fill"></span>
            </span>
            <span class="hero__dot-label">{{ pad(index) }}</span>
          </button>
        </div>
        <span class="hero__count">{{ pad(heroIndex) }} / {{ pad(heroes.length - 1) }}</span>
      </div>
    </section>

    <!-- 企业指标 -->
    <section v-if="data.metrics" class="metrics" aria-labelledby="metrics-title">
      <div class="container">
        <div class="metrics__head">
          <div class="metrics__brand-title">
            <span class="metrics__brand-g" aria-hidden="true">G</span>
            <h2 id="metrics-title" class="metrics__title">{{ data.metrics.title || '大数据' }}</h2>
          </div>
          <p class="metrics__sub">全场景物联网设备连接与全国干线货运网络实时数据沉淀</p>
        </div>
        <div class="metrics__grid">
          <div
            v-for="(item, index) in data.metrics.items"
            :key="item.key"
            v-reveal="index * 90"
            class="metric"
          >
            <p class="metric__value">
              {{ item.displayValue }}<span v-if="item.unit" class="metric__unit">{{ item.unit }}</span>
            </p>
            <p class="metric__label">{{ item.label }}</p>
            <p v-if="item.description" class="metric__desc">{{ item.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 产品与能力：对齐视频标签切换与左右交错结构 -->
    <section v-if="data.services" id="services" class="section services" aria-labelledby="services-title">
      <div class="container">
        <div class="section-head section-head--services">
          <div>
            <p class="kicker">PRODUCTS & CAPABILITIES</p>
            <h2 id="services-title" class="section-title">{{ data.services.title }}</h2>
            <p v-if="data.services.subtitle" class="section-sub">{{ data.services.subtitle }}</p>
          </div>

          <!-- 分组切换标签 (对齐视频左侧切换效果) -->
          <div v-if="data.services.groups.length > 1" class="service-tabs" role="tablist" aria-label="服务分类">
            <button
              v-for="(group, gIdx) in data.services.groups"
              :key="group.key"
              type="button"
              class="service-tab"
              :class="{ 'is-active': activeGroup === gIdx }"
              :aria-selected="activeGroup === gIdx"
              role="tab"
              @click="activeGroup = gIdx"
            >
              <span class="service-tab__dot" aria-hidden="true"></span>
              <span>{{ group.title }}</span>
            </button>
          </div>
        </div>

        <div class="features">
          <article
            v-for="(group, index) in data.services.groups"
            v-show="data.services.groups.length <= 1 || activeGroup === index"
            :key="group.key"
            class="feature"
            :class="{ 'feature--reverse': index % 2 === 1 }"
          >
            <div v-reveal class="feature__media media-frame">
              <img
                v-if="group.image || group.items[0]?.cover"
                :src="(group.image || group.items[0]?.cover)?.url"
                :alt="(group.image || group.items[0]?.cover)?.alt || group.title"
                loading="lazy"
              />
              <div v-else class="feature__placeholder"></div>
              <span class="feature__badge">{{ pad(index) }}</span>
            </div>

            <div v-reveal="120" class="feature__body">
              <p class="feature__eyebrow">{{ group.title }}</p>
              <h3 class="feature__title">{{ group.description || group.title }}</h3>
              <ul class="feature__list">
                <li v-for="item in group.items" :key="item.id">
                  <RouterLink :to="item.url" class="feature__item">
                    <span>{{ item.title }}</span>
                    <span class="feature__arrow" aria-hidden="true">→</span>
                  </RouterLink>
                  <p class="feature__desc">{{ item.summary }}</p>
                </li>
              </ul>
              <RouterLink
                v-if="group.moreLink?.url"
                :to="group.moreLink.url"
                class="btn btn--pill feature__more"
              >
                查看全部 <span aria-hidden="true">→</span>
              </RouterLink>
            </div>
          </article>
        </div>
      </div>
    </section>

    <!-- 客户案例 -->
    <section v-if="data.cases" id="cases" class="section section--mist cases" aria-labelledby="cases-title">
      <div class="container">
        <div class="section-head">
          <div>
            <p class="kicker">CUSTOMER STORIES</p>
            <h2 id="cases-title" class="section-title">{{ data.cases.title }}</h2>
            <p v-if="data.cases.subtitle" class="section-sub">{{ data.cases.subtitle }}</p>
          </div>
          <RouterLink :to="data.cases.moreUrl" class="link-more">全部案例</RouterLink>
        </div>

        <div class="grid grid--3">
          <RouterLink
            v-for="(item, index) in data.cases.items"
            :key="item.id"
            :to="item.url"
            v-reveal="index * 90"
            class="case-card"
          >
            <div class="case-card__media media-frame">
              <img v-if="caseCover(item)" :src="caseCover(item) || ''" :alt="item.cover?.alt || item.title" loading="lazy" />
              <div v-else class="case-card__placeholder"></div>
            </div>
            <div class="case-card__body">
              <div class="case-card__meta">
                <span v-if="item.industry" class="tag">{{ item.industry }}</span>
                <span v-if="item.customerName" class="case-card__customer">{{ item.customerName }}</span>
              </div>
              <h3 class="case-card__title">{{ item.title }}</h3>
              <dl v-if="item.outcomes?.length" class="case-card__outcomes">
                <div v-for="outcome in item.outcomes" :key="outcome.label" class="case-card__outcome">
                  <dt>{{ outcome.label }}</dt>
                  <dd>{{ outcome.displayValue }}</dd>
                </div>
              </dl>
              <p class="case-card__desc">{{ item.summary }}</p>
            </div>
          </RouterLink>
        </div>

        <!-- 客户引言聚焦与专访视频 (对齐参考视频 frame 120s) -->
        <div class="case-spotlight" v-reveal="120">
          <div class="case-spotlight__inner">
            <div class="case-spotlight__content">
              <span class="case-spotlight__mark" aria-hidden="true">“</span>
              <blockquote class="case-spotlight__text">
                管车、管货、管账，无论是业务还是财务，都带来了极大的便利，同时也提升了企业内部的精细化运营。
              </blockquote>
              <div class="case-spotlight__author">
                <p class="case-spotlight__name">唐山双赢物流</p>
                <p class="case-spotlight__role">数字化运营项目负责人</p>
              </div>
              <div class="case-spotlight__action">
                <button type="button" class="btn btn--outline-dark case-spotlight__btn" @click="openHeroConsult">
                  了解更多 <span aria-hidden="true">→</span>
                </button>
              </div>
            </div>

            <!-- 右侧：车队现场缩略图与居中播放按钮 -->
            <div class="case-spotlight__media">
              <img
                src="https://images.unsplash.com/photo-1601584115197-04ecc0da31d7?auto=format&fit=crop&w=700&q=80"
                alt="唐山双赢物流车队运营现场"
                class="case-spotlight__img"
              />
              <button
                type="button"
                class="case-spotlight__play"
                aria-label="播放唐山双赢物流案例视频"
                @click="playSpotlightVideo"
              >
                <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor" aria-hidden="true">
                  <path d="M8 5v14l11-7z" />
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 客户标识 -->
    <section v-if="data.customers" id="customers" class="section customers" aria-labelledby="customers-title">
      <div class="container">
        <div class="customers__head">
          <p class="kicker">TRUSTED BY</p>
          <h2 id="customers-title" class="section-title">{{ data.customers.title }}</h2>
          <p v-if="data.customers.subtitle" class="section-sub">{{ data.customers.subtitle }}</p>
        </div>
        <ul class="customers__grid">
          <li v-for="(item, index) in data.customers.items" :key="item.id" v-reveal="index * 60" class="customers__item">
            <a v-if="item.url" :href="item.url" target="_blank" rel="noopener noreferrer" class="customers__link">
              <img :src="item.logo.url" :alt="item.logo.alt || item.name" loading="lazy" />
            </a>
            <img v-else :src="item.logo.url" :alt="item.logo.alt || item.name" loading="lazy" />
          </li>
        </ul>
      </div>
    </section>

    <!-- 最新动态 -->
    <section v-if="data.news" id="news" class="section section--surface news" aria-labelledby="news-title">
      <div class="container">
        <div class="section-head">
          <div>
            <p class="kicker">NEWSROOM</p>
            <h2 id="news-title" class="section-title">{{ data.news.title }}</h2>
            <p v-if="data.news.subtitle" class="section-sub">{{ data.news.subtitle }}</p>
          </div>
          <RouterLink :to="data.news.moreUrl" class="link-more">全部动态</RouterLink>
        </div>

        <div class="news-grid">
          <RouterLink
            v-for="(item, index) in data.news.items"
            :key="item.id"
            :to="item.url"
            v-reveal="index * 70"
            class="news-card"
          >
            <div class="news-card__media media-frame">
              <img
                v-if="item.cover"
                :src="item.cover.url"
                :alt="item.cover.alt || item.title"
                loading="lazy"
              />
              <div v-else class="news-card__placeholder">
                <span class="news-card__icon" aria-hidden="true">NEWS</span>
              </div>
            </div>
            <div class="news-card__body">
              <time class="news-card__date" :datetime="item.displayTime">
                <svg class="news-card__clock" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                  <circle cx="12" cy="12" r="10"></circle>
                  <polyline points="12 6 12 12 16 14"></polyline>
                </svg>
                {{ item.displayTime }}
              </time>
              <h3 class="news-card__title">{{ item.title }}</h3>
              <p class="news-card__desc">{{ item.summary }}</p>
              <span class="news-card__more">
                阅读更多 <span aria-hidden="true">→</span>
              </span>
            </div>
          </RouterLink>
        </div>
      </div>
    </section>

    <!-- 咨询区 -->
    <section id="consultation" class="cta on-dark">
      <div class="cta__glow" aria-hidden="true"></div>
      <div class="container cta__inner">
        <div>
          <p class="kicker">START A CONVERSATION</p>
          <h2 class="cta__title">{{ data.consultation.title }}</h2>
          <p v-if="data.consultation.subtitle" class="cta__desc">{{ data.consultation.subtitle }}</p>
        </div>
        <button
          v-if="data.consultation.link.type === 'CONSULT'"
          type="button"
          class="btn btn--accent cta__btn"
          @click="openBottomConsult"
        >
          {{ data.consultation.buttonText }} <span aria-hidden="true">→</span>
        </button>
        <a
          v-else-if="data.consultation.link.type === 'EXTERNAL'"
          :href="data.consultation.link.url"
          class="btn btn--accent cta__btn"
          :target="data.consultation.link.newWindow ? '_blank' : undefined"
          :rel="data.consultation.link.newWindow ? 'noopener noreferrer' : undefined"
        >
          {{ data.consultation.buttonText }} <span aria-hidden="true">→</span>
        </a>
        <RouterLink v-else :to="hrefOf(data.consultation.link) ?? '/contact'" class="btn btn--accent cta__btn">
          {{ data.consultation.buttonText }} <span aria-hidden="true">→</span>
        </RouterLink>
      </div>
    </section>
  </div>

  <div v-else-if="failed" class="container state">
    <EmptyState title="内容加载失败" description="请稍后刷新页面重试。首页尚未发布时也会显示该提示。" />
  </div>

  <div v-else class="container state" aria-busy="true">
    <div class="skeleton skeleton--hero"></div>
    <div class="skeleton"></div>
  </div>
</template>

<style scoped>
/* ---------------- hero ---------------- */
.hero {
  position: relative;
  min-height: min(880px, calc(100dvh - var(--header-height)));
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  overflow: hidden;
  background: var(--c-navy);
  color: #fff;
}

.hero__media {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.hero__media img,
.hero__media video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.95;
  animation: heroMedia 1.6s var(--ease-out) both;
}

@keyframes heroMedia {
  from {
    transform: scale(1.06);
    opacity: 0.4;
  }
  to {
    transform: scale(1);
    opacity: 0.95;
  }
}

.hero__veil {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(6, 6, 10, 0.94) 0%, rgba(6, 6, 10, 0.7) 42%, rgba(6, 6, 10, 0.25) 74%, transparent 100%),
    linear-gradient(180deg, rgba(6, 6, 10, 0.45) 0%, transparent 35%, rgba(6, 6, 10, 0.8) 100%);
}

.hero__glow {
  position: absolute;
  width: 900px;
  height: 900px;
  right: -8%;
  bottom: -40%;
  background: radial-gradient(closest-side, rgba(91, 75, 232, 0.32), transparent 70%);
  filter: blur(30px);
}

.hero__inner {
  position: relative;
  z-index: 1;
  padding-top: 180px;
  padding-bottom: 120px;
}

.hero__copy {
  max-width: 720px;
}

.hero__kicker {
  color: rgba(255, 255, 255, 0.55);
  animation: heroUp 0.9s var(--ease-out) 0.15s both;
}

.hero__title {
  font-size: clamp(48px, 7.6vw, 104px);
  line-height: 1.02;
  font-weight: 600;
  letter-spacing: -0.05em;
  color: #fff;
  white-space: pre-line;
  animation: heroUp 1s var(--ease-out) 0.25s both;
}

.hero__sub {
  margin-top: 28px;
  max-width: 520px;
  font-size: 17px;
  line-height: 1.85;
  color: rgba(255, 255, 255, 0.66);
  animation: heroUp 1s var(--ease-out) 0.4s both;
}

.hero__actions {
  display: flex;
  gap: 14px;
  margin-top: 40px;
  animation: heroUp 1s var(--ease-out) 0.55s both;
}

.hero__btn {
  min-height: 54px;
  padding: 0 28px;
  border-radius: 4px;
  background: #fff;
  border-color: #fff;
  color: var(--c-navy);
}

.hero__btn:hover {
  background: var(--c-brand);
  border-color: var(--c-brand);
  color: #fff;
}

@keyframes heroUp {
  from {
    opacity: 0;
    transform: translateY(26px);
  }
  to {
    opacity: 1;
    transform: none;
  }
}

.hero__foot {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding-bottom: 36px;
  gap: 24px;
}

.hero__switch {
  display: flex;
  gap: 8px;
}

.hero__dot {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 86px;
  padding: 0;
  border: none;
  background: none;
  cursor: pointer;
  text-align: left;
}

.hero__dot-bar {
  display: block;
  width: 76px;
  height: 3px;
  border-radius: 2px;
  background: rgba(255, 255, 255, 0.24);
  position: relative;
  overflow: hidden;
  transition: background-color 0.3s ease;
}

.hero__dot.is-active .hero__dot-bar {
  background: rgba(255, 255, 255, 0.4);
}

.hero__dot-fill {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  background: #ffffff;
  width: 100%;
  animation: heroBarFill 6s linear forwards;
}

@keyframes heroBarFill {
  from { width: 0; }
  to { width: 100%; }
}

.section-head--services {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
}

.service-tabs {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.service-tab {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  border-radius: 999px;
  border: 1px solid var(--c-border);
  background: #ffffff;
  color: var(--c-text-muted);
  font-family: inherit;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s var(--ease-out);
}

.service-tab:hover {
  color: var(--c-text);
  border-color: var(--c-border-strong);
}

.service-tab.is-active {
  background: var(--c-brand);
  border-color: var(--c-brand);
  color: #ffffff;
  box-shadow: 0 4px 14px rgba(91, 75, 232, 0.3);
}

.service-tab__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
  opacity: 0.5;
}

.service-tab.is-active .service-tab__dot {
  opacity: 1;
  background: #ffffff;
}

.hero__dot-label {
  font-size: 11px;
  letter-spacing: 0.12em;
  color: rgba(255, 255, 255, 0.4);
}

.hero__dot.is-active .hero__dot-label {
  color: #fff;
}

.hero__count {
  font-size: 11px;
  letter-spacing: 0.16em;
  color: rgba(255, 255, 255, 0.42);
}

/* ---------------- metrics ---------------- */
.metrics {
  padding: 88px 0 76px;
  border-bottom: 1px solid var(--c-border);
}

.metrics__head {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 40px;
}

.metrics__title {
  font-size: 30px;
  letter-spacing: -0.02em;
}

.metrics__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 24px;
  border-top: 1px solid var(--c-border);
  padding-top: 36px;
}

.metric__value {
  font-size: clamp(34px, 3.6vw, 54px);
  font-weight: 600;
  letter-spacing: -0.04em;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}

.metric__unit {
  font-size: 0.5em;
  font-weight: 500;
  margin-left: 2px;
  color: var(--c-brand);
}

.metric__label {
  margin-top: 14px;
  font-size: 14px;
  color: var(--c-text-muted);
}

.metric__desc {
  margin-top: 6px;
  font-size: 12px;
  color: var(--c-text-subtle);
}

/* ---------------- features ---------------- */
.features {
  display: flex;
  flex-direction: column;
  gap: 96px;
}

.feature {
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  gap: 80px;
  align-items: center;
}

.feature--reverse .feature__media {
  order: 2;
}

.feature__media {
  position: relative;
  aspect-ratio: 4 / 3;
  border-radius: var(--radius-lg);
}

.feature__media img {
  height: 100%;
  object-fit: cover;
}

.feature__placeholder,
.case-card__placeholder {
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 70% 30%, rgba(91, 75, 232, 0.35), transparent 60%),
    linear-gradient(135deg, #16161e, #0a0a10);
}

.feature__badge {
  position: absolute;
  left: 24px;
  top: 24px;
  font-size: 12px;
  letter-spacing: 0.14em;
  color: rgba(255, 255, 255, 0.62);
}

.feature__eyebrow {
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--c-brand);
  margin-bottom: 16px;
}

.feature__title {
  font-size: clamp(28px, 3vw, 40px);
  line-height: 1.25;
  letter-spacing: -0.03em;
  font-weight: 600;
}

.feature__list {
  list-style: none;
  margin: 30px 0 0;
  padding: 0;
  border-top: 1px solid var(--c-border);
}

.feature__list > li {
  padding: 18px 0;
  border-bottom: 1px solid var(--c-border);
}

.feature__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  font-size: 17px;
  font-weight: 500;
}

.feature__arrow {
  color: var(--c-brand);
  transition: transform 0.3s var(--ease-out);
}

.feature__item:hover .feature__arrow {
  transform: translateX(5px);
}

.feature__desc {
  margin-top: 6px;
  font-size: 13px;
  color: var(--c-text-muted);
  line-height: 1.75;
}

.feature__more {
  margin-top: 28px;
}

/* ---------------- cases ---------------- */
.case-card {
  display: flex;
  flex-direction: column;
  gap: 22px;
  transition: transform 0.4s var(--ease-out);
}

.case-card:hover {
  transform: translateY(-6px);
}

.case-card__media {
  aspect-ratio: 4 / 3;
  border-radius: var(--radius);
}

.case-card__meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.case-card__customer {
  font-size: 12px;
  color: var(--c-text-muted);
}

.case-card__title {
  margin-top: 12px;
  font-size: 20px;
  line-height: 1.45;
  letter-spacing: -0.02em;
}

.case-card__outcomes {
  display: flex;
  gap: 24px;
  margin: 16px 0 0;
}

.case-card__outcome dt {
  font-size: 11px;
  color: var(--c-text-subtle);
}

.case-card__outcome dd {
  margin: 2px 0 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--c-brand);
  letter-spacing: -0.02em;
}

.case-card__desc {
  margin-top: 12px;
  font-size: 13px;
  color: var(--c-text-muted);
  line-height: 1.8;
}

/* ---------------- customers ---------------- */
.customers {
  padding-top: 104px;
}

.customers__head {
  max-width: 640px;
  margin-bottom: 48px;
}

.customers__grid {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  border-top: 1px solid var(--c-border);
  border-left: 1px solid var(--c-border);
}

.customers__item {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 148px;
  padding: 24px;
  border-right: 1px solid var(--c-border);
  border-bottom: 1px solid var(--c-border);
  transition: background-color 0.3s ease;
}

.customers__item:hover {
  background: var(--c-surface);
}

.customers__item img {
  max-height: 72px;
  width: auto;
  max-width: 82%;
  object-fit: contain;
}

.metrics__brand-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.metrics__brand-g {
  font-size: 32px;
  font-weight: 800;
  color: var(--c-text);
  font-family: inherit;
  line-height: 1;
}

.metrics__sub {
  margin: 8px 0 0;
  font-size: 15px;
  color: var(--c-text-muted);
}

/* ---------------- case-spotlight ---------------- */
.case-spotlight {
  margin-top: 64px;
  background: #ffffff;
  border: 1px solid var(--c-border);
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 16px 44px rgba(10, 12, 20, 0.06);
}

.case-spotlight__inner {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  align-items: stretch;
}

@media (max-width: 860px) {
  .case-spotlight__inner {
    grid-template-columns: 1fr;
  }
}

.case-spotlight__content {
  padding: 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
}

.case-spotlight__media {
  position: relative;
  background: #111;
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.case-spotlight__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.case-spotlight__play {
  position: absolute;
  width: 56px;
  height: 56px;
  border-radius: 8px;
  background: #ffffff;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #111111;
  cursor: pointer;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
  transition: transform 0.2s ease, background-color 0.2s ease, color 0.2s ease;
}

.case-spotlight__play:hover {
  background: var(--c-brand, #5b4be8);
  color: #ffffff;
  transform: scale(1.1);
}

.case-spotlight__quote {
  position: relative;
  padding-left: 36px;
}

.case-spotlight__mark {
  position: absolute;
  left: 0;
  top: -14px;
  font-size: 54px;
  font-family: Georgia, serif;
  color: var(--c-brand);
  line-height: 1;
  opacity: 0.6;
}

.case-spotlight__text {
  font-size: 18px;
  line-height: 1.7;
  font-weight: 500;
  color: var(--c-text);
  margin: 0;
}

.case-spotlight__author {
  margin-top: 16px;
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.case-spotlight__name {
  font-size: 14px;
  font-weight: 600;
  color: var(--c-text);
}

.case-spotlight__role {
  font-size: 13px;
  color: var(--c-text-muted);
}

.case-spotlight__btn {
  white-space: nowrap;
}

/* ---------------- news ---------------- */
.news-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 24px;
}

.news-card {
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  overflow: hidden;
  transition: transform 0.35s var(--ease-out), box-shadow 0.35s ease, border-color 0.35s ease;
}

.news-card:hover {
  transform: translateY(-6px);
  border-color: rgba(91, 75, 232, 0.28);
  box-shadow: 0 16px 36px rgba(10, 12, 20, 0.08);
}

.news-card__media {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 10;
  overflow: hidden;
  background: var(--c-surface);
}

.news-card__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s var(--ease-out);
}

.news-card:hover .news-card__media img {
  transform: scale(1.06);
}

.news-card__placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #181924, #0d0e14);
}

.news-card__icon {
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.16em;
  color: rgba(255, 255, 255, 0.36);
}

.news-card__body {
  padding: 22px 20px 24px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.news-card__date {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--c-text-subtle);
  font-variant-numeric: tabular-nums;
  margin-bottom: 12px;
}

.news-card__clock {
  width: 13px;
  height: 13px;
  opacity: 0.7;
}

.news-card__title {
  font-size: 17px;
  font-weight: 600;
  line-height: 1.45;
  letter-spacing: -0.02em;
  color: var(--c-text);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.9em;
}

.news-card__desc {
  margin-top: 10px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--c-text-muted);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.news-card__more {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 18px;
  font-size: 13px;
  font-weight: 500;
  color: var(--c-brand);
  transition: transform 0.25s var(--ease-out);
}

.news-card:hover .news-card__more {
  transform: translateX(4px);
}

/* ---------------- CTA ---------------- */
.cta {
  position: relative;
  overflow: hidden;
  background: var(--c-navy);
  padding: 112px 0;
}

.cta__glow {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 1100px;
  height: 600px;
  transform: translate(-50%, -50%);
  background: radial-gradient(closest-side, rgba(91, 75, 232, 0.35), transparent 70%);
  filter: blur(20px);
}

.cta__inner {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 48px;
}

.cta__title {
  font-size: clamp(34px, 5vw, 64px);
  line-height: 1.1;
  letter-spacing: -0.04em;
  color: #fff;
  max-width: 780px;
}

.cta__desc {
  margin-top: 18px;
  font-size: 16px;
  color: var(--c-on-dark-muted);
  max-width: 560px;
}

.cta__btn {
  flex: none;
  min-height: 56px;
  padding: 0 30px;
  border-radius: 4px;
}

.skeleton--hero {
  height: 420px;
  margin-bottom: 24px;
}

/* ---------------- responsive ---------------- */
@media (max-width: 1080px) {
  .metrics__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    row-gap: 36px;
  }
  .feature {
    gap: 48px;
  }
  .customers__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 860px) {
  .hero {
    min-height: 760px;
  }
  .hero__inner {
    padding-top: 140px;
    padding-bottom: 96px;
  }
  .hero__title {
    font-size: clamp(42px, 12vw, 64px);
  }
  .hero__veil {
    background: linear-gradient(180deg, rgba(6, 6, 10, 0.55) 0%, rgba(6, 6, 10, 0.8) 65%, rgba(6, 6, 10, 0.96) 100%);
  }
  .hero__sub {
    font-size: 15px;
  }
  .metrics {
    padding: 64px 0 56px;
  }
  .metrics__head {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
    margin-bottom: 28px;
  }
  .metric__value {
    font-size: 34px;
  }
  .features {
    gap: 64px;
  }
  .feature {
    grid-template-columns: 1fr;
    gap: 28px;
  }
  .feature--reverse .feature__media {
    order: 0;
  }
  .feature__media {
    aspect-ratio: 16 / 10;
  }
  .case-card {
    gap: 18px;
  }
  .customers__grid {
    grid-template-columns: 1fr 1fr;
  }
  .customers__item {
    min-height: 110px;
  }
  .news-item__link {
    grid-template-columns: 1fr 28px;
    gap: 8px 20px;
    padding: 22px 4px;
  }
  .news-item__date {
    grid-column: 1 / -1;
  }
  .cta {
    padding: 72px 0;
  }
  .cta__inner {
    flex-direction: column;
    align-items: flex-start;
    gap: 28px;
  }
}
</style>
