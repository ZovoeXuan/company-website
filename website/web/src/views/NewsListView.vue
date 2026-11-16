<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getNews, getNewsCategories } from '@/api/public'
import { usePageMeta } from '@/composables/usePageMeta'
import { usePageHero } from '@/composables/usePageHero'
import { useRealtimeSync } from '@/composables/useRealtimeSync'
import type { Category, NewsSummary, PageResult } from '@/data/types'

const route = useRoute()
const router = useRouter()
const { hero: heroConfig, refreshHero } = usePageHero('news')

const page = ref(Math.max(1, Number(route.query.page) || 1))
const categories = ref<Category[]>([])
const currentCategory = ref<string | null>(
  typeof route.query.category === 'string' && route.query.category ? route.query.category : null,
)
const result = ref<PageResult<NewsSummary> | null>(null)
const loading = ref(true)
const failed = ref(false)
let reqSeq = 0

usePageMeta(() => ({
  title: heroConfig.value?.title || '新闻中心',
  description: heroConfig.value?.subtitle || '企业新闻、技术动态与行业实践资讯。',
}))

function syncQueryToUrl() {
  const query: Record<string, string> = {}
  if (currentCategory.value) query.category = currentCategory.value
  if (page.value > 1) query.page = String(page.value)
  router.replace({ path: '/news', query }).catch(() => {})
}

async function loadCategories() {
  try {
    categories.value = await getNewsCategories()
  } catch {
    categories.value = []
  }
}

async function load() {
  const currentSeq = ++reqSeq
  loading.value = true
  failed.value = false
  try {
    const res = await getNews({
      page: page.value,
      pageSize: 9,
      categoryId: currentCategory.value || undefined,
    })
    if (currentSeq !== reqSeq) return
    result.value = res
  } catch {
    if (currentSeq !== reqSeq) return
    failed.value = true
  } finally {
    if (currentSeq === reqSeq) {
      loading.value = false
    }
  }
}

function selectCategory(catId: string | null) {
  if (currentCategory.value === catId) return
  currentCategory.value = catId
  page.value = 1
  syncQueryToUrl()
}

function onPageChange(p: number) {
  page.value = p
  syncQueryToUrl()
}

const featuredNews = computed(() => (page.value === 1 ? result.value?.items[0] ?? null : null))
const gridNews = computed(() =>
  page.value === 1 ? (result.value?.items ?? []).slice(1) : (result.value?.items ?? []),
)

watch(
  () => [route.query.category, route.query.page],
  ([qCat, qPage]) => {
    const nextCat = typeof qCat === 'string' && qCat ? qCat : null
    const nextPage = Math.max(1, Number(qPage) || 1)
    if (nextCat !== currentCategory.value || nextPage !== page.value) {
      currentCategory.value = nextCat
      page.value = nextPage
    }
  },
)

watch([currentCategory, page], load)

useRealtimeSync(['siteConfigVersion', 'contentVersion'], async (changed, force) => {
  if (force || changed.includes('siteConfigVersion')) {
    await refreshHero()
  }
  if (force || changed.includes('contentVersion')) {
    await loadCategories()
    await load()
  }
})

onMounted(async () => {
  await loadCategories()
  await load()
})
</script>

<template>
  <div>
    <PageHeader
      :title="heroConfig?.title || '新闻与资讯'"
      :description="heroConfig?.subtitle ?? '了解最新企业动态、产品发布与行业数字化实践。'"
      :eyebrow="heroConfig?.eyebrow ?? 'NEWSROOM'"
      :bg-type="heroConfig?.bgType || 'default'"
      :bg-image="heroConfig?.bgImage"
      :bg-video="heroConfig?.bgVideo"
      :overlay-opacity="heroConfig?.overlayOpacity"
      :tone="heroConfig?.bgType === 'image' || heroConfig?.bgType === 'video' ? 'dark' : 'light'"
      centered
      :crumbs="[{ label: '首页', path: '/' }, { label: '新闻中心' }]"
    />

    <!-- 仅当后台存在真实分类时显示分类筛选条 -->
    <div v-if="categories.length > 0" class="news-nav-bar">
      <div class="container">
        <div class="news-tabs" role="tablist" aria-label="新闻分类">
          <button
            type="button"
            class="news-tab"
            :class="{ 'is-active': currentCategory === null }"
            :aria-selected="currentCategory === null"
            @click="selectCategory(null)"
          >
            全部资讯
          </button>
          <button
            v-for="cat in categories"
            :key="cat.id"
            type="button"
            class="news-tab"
            :class="{ 'is-active': currentCategory === cat.id }"
            :aria-selected="currentCategory === cat.id"
            @click="selectCategory(cat.id)"
          >
            {{ cat.name }}
          </button>
        </div>
      </div>
    </div>

    <section class="section news-section">
      <div class="container">
        <div v-if="loading" class="news-grid-loading" aria-busy="true">
          <div v-for="n in 4" :key="n" class="skeleton news-skeleton"></div>
        </div>

        <EmptyState v-else-if="failed" title="新闻加载失败" description="请稍后刷新页面重试。">
          <button type="button" class="btn btn--ghost" @click="load">重新加载</button>
        </EmptyState>

        <EmptyState v-else-if="!result || result.items.length === 0" title="暂无已发布资讯" description="敬请关注后续动态。" />

        <div v-else class="news-content">
          <!-- 首条置顶卡片 -->
          <article v-if="featuredNews" v-reveal class="headline-card">
            <div class="headline-card__body">
              <div class="headline-card__meta">
                <span v-if="featuredNews.categoryName" class="tag">{{ featuredNews.categoryName }}</span>
                <time v-if="featuredNews.displayTime" class="headline-card__time">
                  {{ featuredNews.displayTime }}
                </time>
              </div>
              <h2 class="headline-card__title">{{ featuredNews.title }}</h2>
              <p class="headline-card__desc">{{ featuredNews.summary }}</p>
              <RouterLink :to="`/news/${featuredNews.id}`" class="btn btn--primary headline-card__btn">
                阅读全文 <span aria-hidden="true">→</span>
              </RouterLink>
            </div>
            <div class="headline-card__media">
              <img
                v-if="featuredNews.cover"
                :src="featuredNews.cover.url"
                :alt="featuredNews.cover.alt || featuredNews.title"
                loading="lazy"
              />
              <div v-else class="headline-card__placeholder">
                <span class="headline-card__mark">NEWS</span>
              </div>
            </div>
          </article>

          <!-- 资讯卡片流 -->
          <div v-if="gridNews.length > 0" class="news-cards-grid">
            <RouterLink
              v-for="(item, index) in gridNews"
              :key="item.id"
              :to="`/news/${item.id}`"
              v-reveal="index * 50"
              class="news-tile"
            >
              <div class="news-tile__media">
                <img
                  v-if="item.cover"
                  :src="item.cover.url"
                  :alt="item.cover.alt || item.title"
                  loading="lazy"
                />
                <div v-else class="news-tile__placeholder">
                  <span>NEWS</span>
                </div>
              </div>
              <div class="news-tile__body">
                <div>
                  <span v-if="item.categoryName" class="tag" style="margin-bottom: 8px;">
                    {{ item.categoryName }}
                  </span>
                  <h3 class="news-tile__title">{{ item.title }}</h3>
                </div>
                <time v-if="item.displayTime" class="news-tile__time">
                  {{ item.displayTime }}
                </time>
              </div>
            </RouterLink>
          </div>
        </div>

        <PaginationBar
          v-if="result && result.total > 0"
          :page="result.page"
          :page-size="result.pageSize"
          :total="result.total"
          @change="onPageChange"
        />
      </div>
    </section>
  </div>
</template>

<style scoped>
.news-nav-bar {
  background: #ffffff;
  border-bottom: 1px solid var(--c-border);
  position: sticky;
  top: var(--header-height, 68px);
  z-index: 20;
}

.news-tabs {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 0;
  overflow-x: auto;
}

.news-tab {
  border: 1px solid transparent;
  background: transparent;
  padding: 7px 16px;
  border-radius: 6px;
  font-family: inherit;
  font-size: 14px;
  font-weight: 500;
  color: var(--c-text-muted);
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.18s ease;
}

.news-tab:hover {
  color: var(--c-brand);
  background: var(--c-surface);
}

.news-tab.is-active {
  background: var(--c-brand);
  color: #ffffff;
}

.news-section {
  padding-top: 40px;
}

.headline-card {
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  background: #ffffff;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  overflow: hidden;
  margin-bottom: 40px;
}

@media (max-width: 860px) {
  .headline-card {
    grid-template-columns: 1fr;
  }
}

.headline-card__body {
  padding: 40px 36px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.headline-card__meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.headline-card__time {
  font-size: 13px;
  color: var(--c-text-subtle);
}

.headline-card__title {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.35;
  color: var(--c-text);
  margin: 0 0 14px;
}

.headline-card__desc {
  font-size: 14.5px;
  line-height: 1.7;
  color: var(--c-text-muted);
  margin: 0 0 24px;
}

.headline-card__btn {
  align-self: flex-start;
}

.headline-card__media {
  position: relative;
  background: #0f172a;
  min-height: 260px;
}

.headline-card__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.headline-card__placeholder {
  width: 100%;
  height: 100%;
  min-height: 260px;
  background: linear-gradient(135deg, #1e293b, #0f172a);
  display: flex;
  align-items: center;
  justify-content: center;
}

.headline-card__mark {
  font-size: 14px;
  letter-spacing: 0.2em;
  color: rgba(255, 255, 255, 0.35);
  font-weight: 700;
}

.news-cards-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

@media (max-width: 1100px) {
  .news-cards-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 600px) {
  .news-cards-grid {
    grid-template-columns: 1fr;
  }
}

.news-tile {
  background: #ffffff;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.news-tile:hover {
  border-color: #93c5fd;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.news-tile__media {
  aspect-ratio: 16 / 10;
  width: 100%;
  background: #0f172a;
  overflow: hidden;
}

.news-tile__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.news-tile__placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #1e293b, #0f172a);
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.3);
  font-size: 13px;
  font-weight: 600;
}

.news-tile__body {
  padding: 18px;
  display: flex;
  flex-direction: column;
  flex: 1;
  justify-content: space-between;
}

.news-tile__title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.45;
  color: var(--c-text);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-tile__time {
  font-size: 12px;
  color: var(--c-text-subtle);
}

.news-grid-loading {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.news-skeleton {
  height: 240px;
  border-radius: var(--radius);
}
</style>
