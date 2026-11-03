<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ApiError, get } from '@/api/http'

interface PreviewMedia {
  url: string
  width?: number
  height?: number
  alt?: string
}

interface HomeCard {
  id: string
  title: string
  summary: string
  cover: PreviewMedia | null
  url: string
  customerName?: string
  industry?: string
  outcomes?: Array<{ displayValue: string; label: string }>
}

interface HomeHero {
  key: string
  title: string
  subtitle: string
  desktopImage: PreviewMedia
  buttonText: string
}

interface HomeData {
  version: number | null
  publishedAt: string | null
  hero: { items: HomeHero[] }
  metrics: { items: Array<{ displayValue: string; unit: string; label: string; description: string }> } | null
  services: {
    title: string
    groups: Array<{
      title: string
      description: string
      image: PreviewMedia | null
      items: HomeCard[]
    }>
  } | null
  cases: { title: string; items: HomeCard[] } | null
  customers: { items: Array<{ id: string; name: string; logo: PreviewMedia }> } | null
  news: { title: string; items: Array<{ id: string; title: string; summary: string; displayTime: string }> } | null
  consultation: { title: string; subtitle: string; buttonText: string } | null
}

const router = useRouter()
const data = ref<HomeData | null>(null)
const loading = ref(true)
const error = ref('')
const heroIndex = ref(0)

const heroes = computed(() => data.value?.hero?.items ?? [])
const currentHero = computed(() => heroes.value[heroIndex.value] ?? null)

async function load() {
  loading.value = true
  error.value = ''
  try {
    data.value = await get<HomeData>('/api/admin/preview/home')
    heroIndex.value = 0
  } catch (err) {
    data.value = null
    error.value = err instanceof ApiError ? err.message : '首页预览加载失败'
  } finally {
    loading.value = false
  }
}

function backToEdit() {
  router.push('/home')
}

onMounted(load)
</script>

<template>
  <div class="preview">
    <header class="bar">
      <span class="bar__badge">草稿预览</span>
      <p class="bar__text">
        首屏与各区块取自首页草稿；关联的产品、案例、新闻使用各自的线上版本。咨询按钮在预览中不可用。
      </p>
      <div class="bar__actions">
        <el-button size="small" @click="load">刷新</el-button>
        <el-button size="small" type="primary" @click="backToEdit">返回编辑</el-button>
      </div>
    </header>

    <div v-if="loading" class="state">加载中…</div>
    <div v-else-if="error" class="state state--error">{{ error }}</div>

    <div v-else-if="data" class="site">
      <section v-if="currentHero" class="hero">
        <img class="hero__bg" :src="currentHero.desktopImage.url" :alt="currentHero.desktopImage.alt || ''" />
        <div class="hero__mask"></div>
        <div class="hero__inner">
          <h1 class="hero__title">{{ currentHero.title }}</h1>
          <p v-if="currentHero.subtitle" class="hero__sub">{{ currentHero.subtitle }}</p>
          <button
            v-if="currentHero.buttonText"
            type="button"
            class="hero__btn"
            disabled
            title="草稿预览中不可点击"
          >
            {{ currentHero.buttonText }}
          </button>
          <div v-if="heroes.length > 1" class="hero__switch">
            <button
              v-for="(item, index) in heroes"
              :key="item.key"
              type="button"
              class="hero__dot"
              :class="{ 'is-active': index === heroIndex }"
              :aria-label="`切换到第 ${index + 1} 屏`"
              @click="heroIndex = index"
            ></button>
          </div>
        </div>
      </section>

      <section v-if="data.metrics?.items.length" class="block">
        <div class="wrap">
          <ul class="metrics">
            <li v-for="item in data.metrics.items" :key="item.label" class="metrics__item">
              <p class="metrics__value">{{ item.displayValue }}<span class="metrics__unit">{{ item.unit }}</span></p>
              <p class="metrics__label">{{ item.label }}</p>
              <p v-if="item.description" class="metrics__desc">{{ item.description }}</p>
            </li>
          </ul>
        </div>
      </section>

      <section v-if="data.services?.groups.length" class="block">
        <div class="wrap">
          <h2 class="block__title">{{ data.services.title || '核心产品' }}</h2>
          <div v-for="group in data.services.groups" :key="group.title" class="group">
            <div class="group__head">
              <h3 class="group__title">{{ group.title }}</h3>
              <p v-if="group.description" class="group__desc">{{ group.description }}</p>
            </div>
            <ul class="cards">
              <li v-for="item in group.items" :key="item.id" class="card">
                <div class="card__media">
                  <img v-if="item.cover" :src="item.cover.url" :alt="item.cover.alt || item.title" />
                </div>
                <p class="card__title">{{ item.title }}</p>
                <p v-if="item.summary" class="card__desc">{{ item.summary }}</p>
              </li>
            </ul>
          </div>
        </div>
      </section>

      <section v-if="data.cases?.items.length" class="block block--alt">
        <div class="wrap">
          <h2 class="block__title">{{ data.cases.title || '客户案例' }}</h2>
          <ul class="cards">
            <li v-for="item in data.cases.items" :key="item.id" class="card">
              <div class="card__media">
                <img v-if="item.cover" :src="item.cover.url" :alt="item.cover.alt || item.title" />
              </div>
              <p v-if="item.industry" class="card__tag">{{ item.industry }}</p>
              <p class="card__title">{{ item.title }}</p>
              <dl v-if="item.outcomes?.length" class="outcomes">
                <div v-for="outcome in item.outcomes" :key="outcome.label" class="outcomes__item">
                  <dt>{{ outcome.label }}</dt>
                  <dd>{{ outcome.displayValue }}</dd>
                </div>
              </dl>
            </li>
          </ul>
        </div>
      </section>

      <section v-if="data.customers?.items.length" class="block">
        <div class="wrap">
          <h2 class="block__title">合作客户</h2>
          <ul class="logos">
            <li v-for="item in data.customers.items" :key="item.id" class="logos__item">
              <img :src="item.logo.url" :alt="item.name" />
            </li>
          </ul>
        </div>
      </section>

      <section v-if="data.news?.items.length" class="block block--alt">
        <div class="wrap">
          <h2 class="block__title">{{ data.news.title || '最新动态' }}</h2>
          <ul class="news">
            <li v-for="item in data.news.items" :key="item.id" class="news__item">
              <span class="news__date">{{ item.displayTime }}</span>
              <span class="news__title">{{ item.title }}</span>
            </li>
          </ul>
        </div>
      </section>

      <section v-if="data.consultation" class="cta">
        <div class="wrap">
          <h2 class="cta__title">{{ data.consultation.title }}</h2>
          <p v-if="data.consultation.subtitle" class="cta__sub">{{ data.consultation.subtitle }}</p>
          <button type="button" class="cta__btn" disabled title="草稿预览中不可点击">
            {{ data.consultation.buttonText || '立即咨询' }}
          </button>
        </div>
      </section>

      <footer class="foot">
        <div class="wrap">预览版本：首页草稿 v{{ data.version ?? '—' }}，正式版本由管理员发布后生效。</div>
      </footer>
    </div>
  </div>
</template>

<style scoped>
.preview {
  min-height: 100vh;
  background: #fff;
}

.bar {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  padding: 10px 20px;
  background: #1f2937;
  color: #fff;
}

.bar__badge {
  padding: 3px 10px;
  border-radius: 999px;
  background: #f59e0b;
  color: #1f2937;
  font-size: 12px;
  font-weight: 600;
}

.bar__text {
  margin: 0;
  font-size: 13px;
  color: #d1d5db;
  flex: 1;
  min-width: 240px;
}

.bar__actions {
  display: flex;
  gap: 8px;
}

.state {
  padding: 80px 24px;
  text-align: center;
  color: #6b7280;
}

.state--error {
  color: #b91c1c;
}

.wrap {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.hero {
  position: relative;
  min-height: 520px;
  display: flex;
  align-items: center;
  overflow: hidden;
  background: #0b1a2b;
}

.hero__bg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero__mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(6, 18, 32, 0.92) 0%, rgba(6, 18, 32, 0.72) 45%, rgba(6, 18, 32, 0.2) 100%);
}

.hero__inner {
  position: relative;
  max-width: 1200px;
  margin: 0 auto;
  padding: 96px 24px 64px;
  width: 100%;
}

.hero__title {
  max-width: 620px;
  font-size: 48px;
  line-height: 1.25;
  font-weight: 600;
  color: #fff;
  white-space: pre-line;
}

.hero__sub {
  margin: 18px 0 0;
  max-width: 520px;
  font-size: 17px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.82);
}

.hero__btn {
  margin-top: 30px;
  padding: 12px 28px;
  border: 1px solid rgba(255, 255, 255, 0.55);
  border-radius: 4px;
  background: transparent;
  color: rgba(255, 255, 255, 0.75);
  font-size: 15px;
  cursor: not-allowed;
}

.hero__switch {
  display: flex;
  gap: 8px;
  margin-top: 32px;
}

.hero__dot {
  width: 28px;
  height: 4px;
  padding: 0;
  border: none;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.35);
  cursor: pointer;
}

.hero__dot.is-active {
  background: #fff;
}

.block {
  padding: 72px 0;
}

.block--alt {
  background: #f6f8fb;
}

.block__title {
  margin: 0 0 32px;
  font-size: 32px;
  font-weight: 600;
  color: #111827;
}

.metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  list-style: none;
  margin: 0;
  padding: 0;
}

.metrics__item {
  padding: 0 28px;
  border-left: 1px solid #e5e7eb;
}

.metrics__item:first-child {
  padding-left: 0;
  border-left: none;
}

.metrics__value {
  margin: 0;
  font-size: 42px;
  font-weight: 600;
  color: #111827;
  letter-spacing: -0.02em;
}

.metrics__unit {
  margin-left: 4px;
  font-size: 22px;
}

.metrics__label {
  margin: 10px 0 0;
  font-size: 14px;
  color: #4b5563;
}

.metrics__desc {
  margin: 4px 0 0;
  font-size: 12px;
  color: #9ca3af;
}

.group + .group {
  margin-top: 48px;
}

.group__head {
  margin-bottom: 20px;
}

.group__title {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #111827;
}

.group__desc {
  margin: 8px 0 0;
  font-size: 14px;
  color: #6b7280;
}

.cards {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 28px;
  list-style: none;
  margin: 0;
  padding: 0;
}

.card__media {
  aspect-ratio: 16 / 9;
  border-radius: 4px;
  background: #eef1f5;
  overflow: hidden;
}

.card__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.card__tag {
  display: inline-block;
  margin: 14px 0 0;
  padding: 2px 8px;
  border-radius: 3px;
  background: #eef2f7;
  font-size: 12px;
  color: #4b5563;
}

.card__title {
  margin: 12px 0 0;
  font-size: 19px;
  font-weight: 600;
  line-height: 1.5;
  color: #111827;
}

.card__desc {
  margin: 8px 0 0;
  font-size: 14px;
  line-height: 1.8;
  color: #6b7280;
}

.outcomes {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  margin: 16px 0 0;
}

.outcomes dt {
  font-size: 12px;
  color: #6b7280;
}

.outcomes dd {
  margin: 2px 0 0;
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

.logos {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 1px;
  list-style: none;
  margin: 0;
  padding: 0;
  background: #e5e7eb;
}

.logos__item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 96px;
  background: #fff;
}

.logos__item img {
  max-width: 130px;
  max-height: 44px;
  object-fit: contain;
  filter: grayscale(1);
  opacity: 0.8;
}

.news {
  list-style: none;
  margin: 0;
  padding: 0;
}

.news__item {
  display: flex;
  gap: 40px;
  padding: 24px 0;
  border-top: 1px solid #e5e7eb;
}

.news__date {
  flex: 0 0 108px;
  font-size: 14px;
  color: #9ca3af;
}

.news__title {
  font-size: 17px;
  color: #111827;
}

.cta {
  padding: 80px 0;
  background: #0b1a2b;
}

.cta__title {
  margin: 0;
  font-size: 32px;
  font-weight: 600;
  color: #fff;
}

.cta__sub {
  margin: 12px 0 0;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.75);
}

.cta__btn {
  margin-top: 28px;
  padding: 12px 28px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 4px;
  background: transparent;
  color: rgba(255, 255, 255, 0.6);
  font-size: 15px;
  cursor: not-allowed;
}

.foot {
  padding: 20px 0;
  background: #0b1a2b;
  border-top: 1px solid rgba(255, 255, 255, 0.12);
  font-size: 12px;
  color: rgba(255, 255, 255, 0.45);
}

@media (max-width: 980px) {
  .metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 24px 0;
  }
  .cards {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .logos {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
  .hero__title {
    font-size: 36px;
  }
}

@media (max-width: 700px) {
  .cards {
    grid-template-columns: 1fr;
  }
  .logos {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .hero__title {
    font-size: 28px;
  }
  .block {
    padding: 48px 0;
  }
  .block__title {
    font-size: 24px;
  }
  .news__item {
    flex-direction: column;
    gap: 6px;
  }
}
</style>
