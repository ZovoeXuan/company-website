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
    <!-- 首屏：整幅大图作背景，文案压在左侧；只渲染当前一条，避免多个 h1 -->
    <section v-if="current" class="hero on-dark">
      <div class="hero__bg" aria-hidden="true">
        <picture v-if="current.desktopImage">
          <source
            v-if="current.mobileImage"
            media="(max-width: 860px)"
            :srcset="current.mobileImage.url"
          />
          <img :src="current.desktopImage.url" alt="" fetchpriority="high" />
        </picture>
      </div>

      <div class="container hero__inner">
        <div class="hero__copy">
          <h1 class="hero__title">{{ current.title }}</h1>
          <p v-if="current.subtitle" class="hero__sub">{{ current.subtitle }}</p>
          <div v-if="current.buttonText && current.link" class="hero__actions">
            <button
              v-if="current.link.type === 'CONSULT'"
              type="button"
              class="btn btn--outline-light"
              @click="openConsult({ sourcePosition: 'HOME_HERO', contentType: current.link.contentType, contentId: current.link.contentId })"
            >
              {{ current.buttonText }}
            </button>
            <a
              v-else-if="isExternal(current.link)"
              :href="hrefOf(current.link) ?? '#'"
              class="btn btn--outline-light"
              :target="current.link?.newWindow ? '_blank' : undefined"
              :rel="current.link?.newWindow ? 'noopener noreferrer' : undefined"
            >
              {{ current.buttonText }}
            </a>
            <RouterLink v-else :to="hrefOf(current.link) ?? '/'" class="btn btn--outline-light">
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

        <div class="grid grid--3">
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
/* 首屏：整幅背景图 + 左侧文案，最小高度保证大图有足够气势 */
.hero {
  position: relative;
  display: flex;
  align-items: flex-end;
  min-height: 620px;
  overflow: hidden;
  background: var(--c-navy);
  color: var(--c-on-dark);
}

.hero__bg {
  position: absolute;
  inset: 0;
}

.hero__bg img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 左深右浅的双层遮罩：保证白色标题在任何图片上都有对比度 */
.hero__bg::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, rgba(6, 15, 34, 0.94) 0%, rgba(6, 15, 34, 0.72) 42%, rgba(6, 15, 34, 0.32) 100%),
    linear-gradient(180deg, rgba(6, 15, 34, 0.45) 0%, rgba(6, 15, 34, 0) 35%);
}

.hero__inner {
  position: relative;
  z-index: 1;
  padding-top: 140px;
  padding-bottom: 88px;
}

.hero__copy {
  max-width: 760px;
}

.hero__title {
  font-size: clamp(34px, 4.2vw, 58px);
  line-height: 1.16;
  font-weight: 500;
  letter-spacing: -0.025em;
  color: var(--c-on-dark);
  white-space: pre-line;
}

.hero__sub {
  margin-top: 24px;
  font-size: 18px;
  color: rgba(255, 255, 255, 0.8);
  max-width: 600px;
  line-height: 1.85;
}

.hero__actions {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  margin-top: 40px;
}

.hero__switch {
  display: flex;
  gap: 10px;
  margin-top: 48px;
}

.hero__dot {
  width: 40px;
  height: 3px;
  padding: 0;
  border: none;
  background: rgba(255, 255, 255, 0.3);
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.hero__dot.is-active {
  background: #fff;
}

.metrics {
  background: #fff;
  border-bottom: 1px solid var(--c-border);
}

.metrics__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0;
  padding-top: 56px;
  padding-bottom: 56px;
}

/* 指标之间用细线分隔，比卡片更接近参考站的排布 */
.metrics__item {
  padding: 0 32px;
  border-left: 1px solid var(--c-border);
}

.metrics__item:first-child {
  padding-left: 0;
  border-left: none;
}

.metrics__value {
  font-size: 44px;
  line-height: 1.1;
  font-weight: 500;
  letter-spacing: -0.03em;
  font-variant-numeric: tabular-nums;
}

.metrics__unit {
  margin-left: 4px;
  font-size: 24px;
}

.metrics__label {
  margin-top: 10px;
  font-size: 14px;
  color: var(--c-text-muted);
}

.metrics__desc {
  margin-top: 6px;
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
  padding: 22px 0 0;
}

.case__title {
  margin: 10px 0 8px;
  font-size: 20px;
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

/* 客户标识：去掉描边盒子，按网格铺开，接近参考站的 logo 墙 */
.customers {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 1px;
  list-style: none;
  margin: 40px 0 0;
  padding: 0;
  background: var(--c-border);
}

.customers__item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 96px;
  padding: 16px 20px;
  background: #fff;
}

.customers__item img {
  max-width: 130px;
  max-height: 44px;
  object-fit: contain;
  filter: grayscale(1);
  opacity: 0.75;
  transition: filter 0.2s ease, opacity 0.2s ease;
}

.customers__item:hover img {
  filter: grayscale(0);
  opacity: 1;
}

.news-list {
  list-style: none;
  margin: 0;
  padding: 0;
  border-top: 1px solid var(--c-border);
}

.news-item + .news-item {
  border-top: 1px solid var(--c-border);
}

.news-item__link {
  display: flex;
  gap: 40px;
  padding: 28px 0;
}

.news-item__link:hover .news-item__date {
  color: var(--c-brand);
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
  background:
    radial-gradient(900px 320px at 15% 0%, rgba(47, 107, 240, 0.3), transparent 65%),
    var(--c-navy);
  color: #fff;
  padding: 88px 0;
}

.cta__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
  flex-wrap: wrap;
}

.cta__title {
  font-size: 34px;
  font-weight: 500;
  line-height: 1.35;
  color: #fff;
}

.cta__desc {
  margin-top: 10px;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.72);
}

.state {
  padding: 96px 24px;
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
  /* 首屏不再左右分栏，改为整幅背景 + 文案，故这里只需压缩高度 */
  .hero {
    min-height: 520px;
  }
  .hero__inner {
    padding-top: 96px;
    padding-bottom: 56px;
  }
  .hero__title {
    font-size: 34px;
  }
  .hero__sub {
    font-size: 16px;
  }
  .metrics__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 0;
    padding-top: 40px;
    padding-bottom: 40px;
  }
  /* 指标改成两列后，第二行起需要一条横线区分 */
  .metrics__item {
    padding: 24px 20px;
  }
  .metrics__item:nth-child(odd) {
    padding-left: 0;
    border-left: none;
  }
  .metrics__item:nth-child(n + 3) {
    border-top: 1px solid var(--c-border);
  }
  .metrics__value {
    font-size: 34px;
  }
  .customers {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 700px) {
  .hero {
    min-height: 460px;
  }
  .hero__title {
    font-size: 28px;
  }
  .hero__actions {
    margin-top: 28px;
  }
  .hero__switch {
    margin-top: 32px;
  }
  .customers {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .news-item__link {
    flex-direction: column;
    gap: 6px;
    padding: 18px 0;
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
