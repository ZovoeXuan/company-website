<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import EmptyState from '@/components/EmptyState.vue'
import { getHome } from '@/api/public'
import { openConsult } from '@/composables/consult'
import { usePageMeta } from '@/composables/usePageMeta'
import type { HomeHeroItem, HomeLink, HomeView } from '@/data/types'

const data = ref<HomeView | null>(null)
const failed = ref(false)
const heroIndex = ref(0)

const heroes = computed<HomeHeroItem[]>(() => data.value?.hero.items ?? [])
const current = computed<HomeHeroItem | null>(() => heroes.value[heroIndex.value] ?? null)

usePageMeta(() => ({
  title: '工业设备数字化与能源管理',
  description: '面向制造企业的设备管理、预测性维护与能耗监测平台，帮助降低非计划停机、定位高耗能环节。',
}))

/** 非咨询动作的跳转地址；CONSULT 由弹窗承接，不再伪造链接 */
function hrefOf(link: HomeLink | null): string | null {
  if (!link || link.type === 'CONSULT') return null
  return link.url ?? null
}

function isExternal(link: HomeLink | null): boolean {
  return link?.type === 'EXTERNAL'
}

onMounted(async () => {
  try {
    data.value = await getHome()
  } catch {
    failed.value = true
  }
})
</script>

<template>
  <div v-if="data" class="home">
    <!-- 首屏：只渲染当前一条，避免多个 h1 -->
    <section v-if="current" class="hero">
      <div class="container hero__inner">
        <div class="hero__copy">
          <h1 class="hero__title">{{ current.title }}</h1>
          <p v-if="current.subtitle" class="hero__sub">{{ current.subtitle }}</p>
          <div v-if="current.buttonText && current.link" class="hero__actions">
            <button
              v-if="current.link.type === 'CONSULT'"
              type="button"
              class="btn btn--primary"
              @click="openConsult({ sourcePosition: 'HOME_HERO', contentType: current.link.contentType, contentId: current.link.contentId })"
            >
              {{ current.buttonText }}
            </button>
            <a
              v-else-if="isExternal(current.link)"
              :href="hrefOf(current.link) ?? '#'"
              class="btn btn--primary"
              :target="current.link?.newWindow ? '_blank' : undefined"
              :rel="current.link?.newWindow ? 'noopener noreferrer' : undefined"
            >
              {{ current.buttonText }}
            </a>
            <RouterLink v-else :to="hrefOf(current.link) ?? '/'" class="btn btn--primary">
              {{ current.buttonText }}
            </RouterLink>
          </div>

          <div v-if="heroes.length > 1" class="hero__switch">
            <button
              v-for="(item, index) in heroes"
              :key="item.key"
              type="button"
              class="hero__dot"
              :class="{ 'is-active': index === heroIndex }"
              :aria-label="`切换到第 ${index + 1} 屏：${item.title}`"
              :aria-current="index === heroIndex ? 'true' : undefined"
              @click="heroIndex = index"
            ></button>
          </div>
        </div>

        <figure v-if="current.desktopImage" class="hero__media">
          <picture>
            <source
              v-if="current.mobileImage"
              media="(max-width: 700px)"
              :srcset="current.mobileImage.url"
            />
            <img
              :src="current.desktopImage.url"
              :alt="current.desktopImage.alt || current.title"
              :width="current.desktopImage.width"
              :height="current.desktopImage.height"
              fetchpriority="high"
            />
          </picture>
        </figure>
      </div>
    </section>

    <!-- 企业指标 -->
    <section v-if="data.metrics" id="metrics" class="metrics" aria-labelledby="metrics-title">
      <div class="container">
        <h2 v-if="data.metrics.title" id="metrics-title" class="visually-hidden">{{ data.metrics.title }}</h2>
        <div class="metrics__grid">
          <div v-for="item in data.metrics.items" :key="item.key" class="metrics__item">
            <p class="metrics__value">
              {{ item.displayValue }}<span v-if="item.unit" class="metrics__unit">{{ item.unit }}</span>
            </p>
            <p class="metrics__label">{{ item.label }}</p>
            <p v-if="item.description" class="metrics__desc">{{ item.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 产品服务 -->
    <section v-if="data.services" id="services" class="section" aria-labelledby="services-title">
      <div class="container">
        <div class="section-head">
          <div>
            <h2 id="services-title" class="section-title">{{ data.services.title }}</h2>
            <p v-if="data.services.subtitle" class="section-sub">{{ data.services.subtitle }}</p>
          </div>
        </div>

        <div v-for="group in data.services.groups" :key="group.key" class="group">
          <div class="group__head">
            <h3 class="group__title">{{ group.title }}</h3>
            <p v-if="group.description" class="group__desc">{{ group.description }}</p>
            <a
              v-if="group.moreLink && group.moreLink.type === 'EXTERNAL'"
              :href="group.moreLink.url"
              class="link-more"
              :target="group.moreLink.newWindow ? '_blank' : undefined"
              :rel="group.moreLink.newWindow ? 'noopener noreferrer' : undefined"
            >
              了解更多 →
            </a>
            <RouterLink v-else-if="group.moreLink?.url" :to="group.moreLink.url" class="link-more">
              了解更多 →
            </RouterLink>
          </div>

          <div class="grid grid--3">
            <RouterLink v-for="item in group.items" :key="item.id" :to="item.url" class="card service">
              <img
                v-if="item.cover"
                :src="item.cover.url"
                :alt="item.cover.alt || item.title"
                :width="item.cover.width"
                :height="item.cover.height"
                loading="lazy"
                class="service__cover"
              />
              <div class="card__body">
                <h4 class="card__title">{{ item.title }}</h4>
                <p class="card__desc">{{ item.summary }}</p>
                <span class="service__more">了解详情 →</span>
              </div>
            </RouterLink>
          </div>
        </div>
      </div>
    </section>

    <!-- 客户案例 -->
    <section v-if="data.cases" id="cases" class="section section--surface" aria-labelledby="cases-title">
      <div class="container">
        <div class="section-head">
          <div>
            <h2 id="cases-title" class="section-title">{{ data.cases.title }}</h2>
            <p v-if="data.cases.subtitle" class="section-sub">{{ data.cases.subtitle }}</p>
          </div>
          <RouterLink :to="data.cases.moreUrl" class="link-more">全部案例 →</RouterLink>
        </div>

        <div class="grid grid--2">
          <RouterLink v-for="item in data.cases.items" :key="item.id" :to="item.url" class="case card">
            <img
              v-if="item.cover"
              :src="item.cover.url"
              :alt="item.cover.alt || item.title"
              :width="item.cover.width"
              :height="item.cover.height"
              loading="lazy"
              class="case__cover"
            />
            <div class="case__body">
              <span v-if="item.industry" class="tag">{{ item.industry }}</span>
              <h3 class="case__title">{{ item.title }}</h3>
              <p class="card__desc">{{ item.summary }}</p>
              <dl v-if="item.outcomes?.length" class="case__results">
                <div v-for="outcome in item.outcomes" :key="outcome.label" class="case__result">
                  <dt>{{ outcome.label }}</dt>
                  <dd>{{ outcome.displayValue }}</dd>
                </div>
              </dl>
            </div>
          </RouterLink>
        </div>
      </div>
    </section>

    <!-- 客户标识 -->
    <section v-if="data.customers" id="customers" class="section" aria-labelledby="customers-title">
      <div class="container">
        <h2 id="customers-title" class="section-title">{{ data.customers.title }}</h2>
        <p v-if="data.customers.subtitle" class="section-sub">{{ data.customers.subtitle }}</p>
        <ul class="customers">
          <li v-for="item in data.customers.items" :key="item.id" class="customers__item">
            <a
              v-if="item.url"
              :href="item.url"
              target="_blank"
              rel="noopener noreferrer"
              class="customers__link"
            >
              <img :src="item.logo.url" :alt="item.logo.alt || item.name" loading="lazy" />
            </a>
            <img v-else :src="item.logo.url" :alt="item.logo.alt || item.name" loading="lazy" />
          </li>
        </ul>
      </div>
    </section>

    <!-- 新闻 -->
    <section v-if="data.news" id="news" class="section section--surface" aria-labelledby="news-title">
      <div class="container">
        <div class="section-head">
          <div>
            <h2 id="news-title" class="section-title">{{ data.news.title }}</h2>
            <p v-if="data.news.subtitle" class="section-sub">{{ data.news.subtitle }}</p>
          </div>
          <RouterLink :to="data.news.moreUrl" class="link-more">全部新闻 →</RouterLink>
        </div>

        <ul class="news-list">
          <li v-for="item in data.news.items" :key="item.id" class="news-item">
            <RouterLink :to="item.url" class="news-item__link">
              <time class="news-item__date" :datetime="item.displayTime">{{ item.displayTime }}</time>
              <div>
                <h3 class="news-item__title">{{ item.title }}</h3>
                <p class="news-item__desc">{{ item.summary }}</p>
              </div>
            </RouterLink>
          </li>
        </ul>
      </div>
    </section>

    <!-- 咨询区 -->
    <section id="consultation" class="cta">
      <div class="container cta__inner">
        <div>
          <h2 class="cta__title">{{ data.consultation.title }}</h2>
          <p v-if="data.consultation.subtitle" class="cta__desc">{{ data.consultation.subtitle }}</p>
        </div>
        <button
          v-if="data.consultation.link.type === 'CONSULT'"
          type="button"
          class="btn btn--primary"
          @click="openConsult({ sourcePosition: 'HOME_BOTTOM', contentType: data.consultation.link.contentType, contentId: data.consultation.link.contentId })"
        >
          {{ data.consultation.buttonText }}
        </button>
        <a
          v-else-if="data.consultation.link.type === 'EXTERNAL'"
          :href="data.consultation.link.url"
          class="btn btn--primary"
          :target="data.consultation.link.newWindow ? '_blank' : undefined"
          :rel="data.consultation.link.newWindow ? 'noopener noreferrer' : undefined"
        >
          {{ data.consultation.buttonText }}
        </a>
        <RouterLink v-else :to="hrefOf(data.consultation.link) ?? '/contact'" class="btn btn--primary">
          {{ data.consultation.buttonText }}
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
.hero {
  background: linear-gradient(180deg, #f2f7fd 0%, #ffffff 100%);
  border-bottom: 1px solid var(--c-border);
}

.hero__inner {
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  gap: 56px;
  align-items: center;
  padding-top: 72px;
  padding-bottom: 72px;
}

.hero__title {
  font-size: 42px;
  line-height: 1.32;
  font-weight: 500;
  letter-spacing: -0.015em;
  white-space: pre-line;
}

.hero__sub {
  margin-top: 18px;
  font-size: 16px;
  color: var(--c-text-muted);
  max-width: 520px;
  line-height: 1.85;
}

.hero__actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 30px;
}

.hero__switch {
  display: flex;
  gap: 8px;
  margin-top: 28px;
}

.hero__dot {
  width: 28px;
  height: 4px;
  padding: 0;
  border: none;
  border-radius: 999px;
  background: var(--c-border-strong);
  cursor: pointer;
}

.hero__dot.is-active {
  background: var(--c-brand);
}

.hero__media img {
  width: 100%;
  border-radius: var(--radius);
  border: 1px solid var(--c-border);
}

.metrics {
  border-bottom: 1px solid var(--c-border);
}

.metrics__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
  padding-top: 32px;
  padding-bottom: 32px;
}

.metrics__value {
  font-size: 30px;
  font-weight: 500;
  letter-spacing: -0.01em;
}

.metrics__unit {
  margin-left: 2px;
  font-size: 20px;
}

.metrics__label {
  margin-top: 4px;
  font-size: 14px;
  color: var(--c-text-muted);
}

.metrics__desc {
  margin-top: 4px;
  font-size: 12px;
  color: var(--c-text-subtle);
}

.group + .group {
  margin-top: 48px;
}

.group__head {
  display: flex;
  align-items: baseline;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.group__title {
  font-size: 18px;
  font-weight: 500;
}

.group__desc {
  flex: 1;
  min-width: 200px;
  font-size: 14px;
  color: var(--c-text-muted);
}

.service {
  display: block;
  overflow: hidden;
}

.service__cover {
  width: 100%;
  aspect-ratio: 16 / 9;
  object-fit: cover;
  display: block;
}

.service__more {
  display: inline-block;
  margin-top: 16px;
  font-size: 14px;
  color: var(--c-brand);
}

.case__cover {
  width: 100%;
  aspect-ratio: 16 / 9;
  object-fit: cover;
  display: block;
}

.case__body {
  padding: 24px;
}

.case__title {
  margin: 12px 0 8px;
  font-size: 18px;
  font-weight: 500;
  line-height: 1.5;
}

.case__results {
  display: flex;
  gap: 28px;
  margin: 20px 0 0;
}

.case__result dt {
  font-size: 13px;
  color: var(--c-text-muted);
}

.case__result dd {
  margin: 2px 0 0;
  font-size: 20px;
  font-weight: 500;
  color: var(--c-brand);
}

.customers {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  list-style: none;
  margin: 28px 0 0;
  padding: 0;
}

.customers__item {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 132px;
  height: 64px;
  padding: 10px 18px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  background: #fff;
}

.customers__item img {
  max-width: 120px;
  max-height: 44px;
  object-fit: contain;
}

.news-list {
  list-style: none;
  margin: 0;
  padding: 0;
  background: #fff;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
}

.news-item + .news-item {
  border-top: 1px solid var(--c-border);
}

.news-item__link {
  display: flex;
  gap: 28px;
  padding: 20px 24px;
}

.news-item__link:hover .news-item__title {
  color: var(--c-brand);
}

.news-item__date {
  flex: none;
  width: 96px;
  font-size: 13px;
  color: var(--c-text-subtle);
  font-variant-numeric: tabular-nums;
}

.news-item__title {
  font-size: 16px;
  font-weight: 500;
  line-height: 1.5;
}

.news-item__desc {
  margin-top: 6px;
  font-size: 14px;
  color: var(--c-text-muted);
}

.cta {
  margin-top: 8px;
  background: #0e1c2b;
  color: #fff;
  padding: 56px 0;
}

.cta__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
  flex-wrap: wrap;
}

.cta__title {
  font-size: 26px;
  font-weight: 500;
  line-height: 1.4;
}

.cta__desc {
  margin-top: 10px;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.72);
}

.state {
  padding: 72px 24px;
}

.skeleton {
  height: 200px;
  border-radius: var(--radius);
  background: var(--c-surface);
  margin-bottom: 20px;
}

.skeleton--hero {
  height: 320px;
}

@media (max-width: 980px) {
  .hero__inner {
    grid-template-columns: 1fr;
    gap: 32px;
    padding-top: 48px;
    padding-bottom: 48px;
  }
  .hero__title {
    font-size: 32px;
  }
  .metrics__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 700px) {
  .news-item__link {
    flex-direction: column;
    gap: 6px;
    padding: 18px;
  }
  .news-item__date {
    width: auto;
  }
  .cta {
    padding: 40px 0;
  }
  .cta__title {
    font-size: 22px;
  }
}
</style>
