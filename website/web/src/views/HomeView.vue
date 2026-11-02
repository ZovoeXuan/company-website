<script setup lang="ts">
import { onMounted, ref } from 'vue'
import EmptyState from '@/components/EmptyState.vue'
import { getHome } from '@/api/public'
import { usePageMeta } from '@/composables/usePageMeta'
import type { HomeView } from '@/data/types'

const data = ref<HomeView | null>(null)
const failed = ref(false)

usePageMeta(() => ({
  title: '工业设备数字化与能源管理',
  description: '面向制造企业的设备管理、预测性维护与能耗监测平台，帮助降低非计划停机、定位高耗能环节。',
}))

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
    <section class="hero">
      <div class="container hero__inner">
        <div class="hero__copy">
          <p class="eyebrow">{{ data.hero.eyebrow }}</p>
          <h1 class="hero__title">{{ data.hero.title }}</h1>
          <p class="hero__sub">{{ data.hero.subtitle }}</p>
          <div class="hero__actions">
            <RouterLink :to="data.hero.primary.path" class="btn btn--primary">{{ data.hero.primary.label }}</RouterLink>
            <RouterLink :to="data.hero.secondary.path" class="btn btn--ghost">{{ data.hero.secondary.label }}</RouterLink>
          </div>
        </div>

        <figure v-if="data.hero.media" class="hero__media">
          <img
            :src="data.hero.media.url"
            :alt="data.hero.media.alt"
            :width="data.hero.media.width"
            :height="data.hero.media.height"
            fetchpriority="high"
          />
        </figure>
      </div>
    </section>

    <section class="metrics" aria-label="服务规模">
      <div class="container metrics__grid">
        <div v-for="item in data.metrics" :key="item.label" class="metrics__item">
          <p class="metrics__value">{{ item.value }}</p>
          <p class="metrics__label">{{ item.label }}</p>
        </div>
      </div>
    </section>

    <section class="section" aria-labelledby="services-title">
      <div class="container">
        <div class="section-head">
          <div>
            <h2 id="services-title" class="section-title">核心产品</h2>
            <p class="section-sub">从设备接入到运维决策，按需选择，也可以整套落地。</p>
          </div>
          <RouterLink to="/products" class="link-more">查看全部产品 →</RouterLink>
        </div>

        <div class="grid grid--3">
          <RouterLink
            v-for="item in data.services"
            :key="item.key"
            :to="item.contentId ? `/products/${item.contentId}` : '/products'"
            class="card service"
          >
            <div class="card__body">
              <h3 class="card__title">{{ item.title }}</h3>
              <p class="card__desc">{{ item.summary }}</p>
              <span class="service__more">了解详情 →</span>
            </div>
          </RouterLink>
        </div>
      </div>
    </section>

    <section class="section section--surface" aria-labelledby="cases-title">
      <div class="container">
        <div class="section-head">
          <div>
            <h2 id="cases-title" class="section-title">客户案例</h2>
            <p class="section-sub">项目数据来自实际交付，客户名称按协议隐去。</p>
          </div>
          <RouterLink to="/cases" class="link-more">全部案例 →</RouterLink>
        </div>

        <div class="grid grid--2">
          <RouterLink v-for="item in data.featuredCases" :key="item.id" :to="`/cases/${item.id}`" class="case card">
            <div class="case__body">
              <span class="tag">{{ item.industry }}</span>
              <h3 class="case__title">{{ item.title }}</h3>
              <p class="card__desc">{{ item.summary }}</p>
              <dl class="case__results">
                <div v-for="result in item.results" :key="result.label" class="case__result">
                  <dt>{{ result.label }}</dt>
                  <dd>{{ result.value }}</dd>
                </div>
              </dl>
            </div>
          </RouterLink>
        </div>
      </div>
    </section>

    <section class="section" aria-labelledby="customers-title">
      <div class="container">
        <h2 id="customers-title" class="section-title">服务行业</h2>
        <p class="section-sub">已在多个制造与公共设施细分领域完成交付。</p>
        <ul class="customers">
          <li v-for="item in data.customers" :key="item.id" class="customers__item">{{ item.name }}</li>
        </ul>
      </div>
    </section>

    <section class="section section--surface" aria-labelledby="news-title">
      <div class="container">
        <div class="section-head">
          <h2 id="news-title" class="section-title">最新动态</h2>
          <RouterLink to="/news" class="link-more">全部新闻 →</RouterLink>
        </div>

        <ul class="news-list">
          <li v-for="item in data.latestNews" :key="item.id" class="news-item">
            <RouterLink :to="`/news/${item.id}`" class="news-item__link">
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

    <section class="cta">
      <div class="container cta__inner">
        <div>
          <h2 class="cta__title">先看看你的设备数据能说明什么</h2>
          <p class="cta__desc">留下联系方式，我们会用你的场景做一次演示，不推销、不承诺做不到的事。</p>
        </div>
        <RouterLink to="/contact" class="btn btn--primary">预约演示</RouterLink>
      </div>
    </section>
  </div>

  <div v-else-if="failed" class="container state">
    <EmptyState title="内容加载失败" description="请稍后刷新页面重试。" />
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

.metrics__label {
  margin-top: 4px;
  font-size: 14px;
  color: var(--c-text-muted);
}

.service {
  display: block;
}

.service__more {
  display: inline-block;
  margin-top: 16px;
  font-size: 14px;
  color: var(--c-brand);
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
  gap: 12px;
  list-style: none;
  margin: 28px 0 0;
  padding: 0;
}

.customers__item {
  border: 1px solid var(--c-border);
  border-radius: 999px;
  padding: 8px 18px;
  font-size: 14px;
  color: var(--c-text-muted);
  background: #fff;
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
