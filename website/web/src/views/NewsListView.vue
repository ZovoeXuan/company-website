<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getNews, getNewsCategories } from '@/api/public'
import { usePageMeta } from '@/composables/usePageMeta'
import type { Category, NewsSummary, PageResult } from '@/data/types'

const page = ref(1)
const categories = ref<Category[]>([])
const currentCategory = ref<string | null>(null)
const result = ref<PageResult<NewsSummary> | null>(null)
const loading = ref(true)
const failed = ref(false)

const fallbackCategories = [
  { id: 'corp', name: '企业新闻' },
  { id: 'insight', name: '行业洞察' },
  { id: 'practice', name: '最佳实践' },
  { id: 'brand', name: '品牌活动' },
  { id: 'bigdata', name: '大数据' },
  { id: 'esg', name: '社会责任' },
]

const displayCategories = computed(() => {
  return categories.value.length > 0 ? categories.value : fallbackCategories
})

usePageMeta(() => ({
  title: '新闻中心',
  description: '公司动态、行业洞察与实践文章，对齐参考视频 4 列卡片与头条排版。',
}))

async function loadCategories() {
  try {
    const list = await getNewsCategories()
    if (list && list.length > 0) {
      categories.value = list
    }
  } catch {
    // 降级使用备选分类
  }
}

async function load() {
  loading.value = true
  failed.value = false
  try {
    const cat = currentCategory.value
    const isMock = fallbackCategories.some((c) => c.id === cat)
    result.value = await getNews({
      page: page.value,
      pageSize: 9,
      categoryId: cat && !isMock ? cat : undefined,
    })
  } catch {
    failed.value = true
  } finally {
    loading.value = false
  }
}

function selectCategory(catId: string | null) {
  if (currentCategory.value === catId) return
  currentCategory.value = catId
  page.value = 1
  load()
}

const featuredNews = computed(() => result.value?.items[0] ?? null)
const gridNews = computed(() => (result.value?.items ?? []).slice(1))

watch(page, load)
onMounted(async () => {
  await loadCategories()
  await load()
})
</script>

<template>
  <div>
    <PageHeader
      title="企业新闻与行业洞察"
      description="关注物流与供应链前沿技术、业务动态与标杆企业数字化实践。"
      eyebrow="NEWSROOM"
      tone="light"
      centered
      :crumbs="[{ label: '首页', path: '/' }, { label: '新闻中心' }]"
    />

    <!-- 对齐视频：白色背景分类导航条 -->
    <div class="news-nav-bar">
      <div class="container">
        <div class="news-tabs" role="tablist" aria-label="新闻分类">
          <button
            type="button"
            class="news-tab"
            :class="{ 'is-active': currentCategory === null }"
            :aria-selected="currentCategory === null"
            @click="selectCategory(null)"
          >
            全部新闻
          </button>
          <button
            v-for="cat in displayCategories"
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

        <EmptyState v-else-if="failed" title="新闻加载失败" description="请稍后刷新页面重试。" />

        <EmptyState v-else-if="!result || result.items.length === 0" title="暂无新闻" description="稍后再来看看。" />

        <div v-else class="news-content">
          <!-- 对齐视频：置顶大头条卡片 -->
          <article v-if="featuredNews" v-reveal class="headline-card">
            <div class="headline-card__body">
              <time v-if="featuredNews.displayTime" class="headline-card__time">
                <svg class="news-clock-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <circle cx="12" cy="12" r="10"></circle>
                  <polyline points="12 6 12 12 16 14"></polyline>
                </svg>
                {{ featuredNews.displayTime }}
              </time>
              <h2 class="headline-card__title">{{ featuredNews.title }}</h2>
              <p class="headline-card__desc">{{ featuredNews.summary }}</p>
              <RouterLink :to="`/news/${featuredNews.id}`" class="btn btn--outline-dark headline-card__btn">
                了解更多 <span aria-hidden="true">→</span>
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
                <span class="headline-card__mark">FEATURED NEWS</span>
              </div>
            </div>
          </article>

          <!-- 对齐视频：4 列卡片流 -->
          <div class="news-cards-grid">
            <RouterLink
              v-for="(item, index) in gridNews"
              :key="item.id"
              :to="`/news/${item.id}`"
              v-reveal="index * 60"
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
                <h3 class="news-tile__title">{{ item.title }}</h3>
                <time class="news-tile__time">
                  <svg class="news-clock-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <circle cx="12" cy="12" r="10"></circle>
                    <polyline points="12 6 12 12 16 14"></polyline>
                  </svg>
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
          @change="(p: number) => (page = p)"
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
  top: var(--header-height, 72px);
  z-index: 20;
}

.news-tabs {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 0;
  overflow-x: auto;
}

.news-tab {
  border: none;
  background: transparent;
  padding: 8px 18px;
  border-radius: 999px;
  font-family: inherit;
  font-size: 14px;
  font-weight: 500;
  color: var(--c-text-muted);
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s ease;
}

.news-tab:hover {
  color: var(--c-text);
  background: var(--c-surface);
}

.news-tab.is-active {
  background: var(--c-brand, #5b4be8);
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(91, 75, 232, 0.25);
}

.news-section {
  padding-top: 40px;
}

/* 置顶头条卡片 */
.headline-card {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  background: #ffffff;
  border: 1px solid var(--c-border);
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 48px;
  box-shadow: 0 10px 30px rgba(10, 12, 20, 0.05);
}

@media (max-width: 860px) {
  .headline-card {
    grid-template-columns: 1fr;
  }
}

.headline-card__body {
  padding: 48px 40px;
  background: #fafafc;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.headline-card__time {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--c-text-subtle);
  margin-bottom: 16px;
}

.news-clock-icon {
  width: 14px;
  height: 14px;
}

.headline-card__title {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.35;
  color: var(--c-text);
  margin: 0 0 18px;
  letter-spacing: -0.02em;
}

.headline-card__desc {
  font-size: 15px;
  line-height: 1.65;
  color: var(--c-text-muted);
  margin: 0 0 28px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.headline-card__btn {
  align-self: flex-start;
  padding: 10px 24px;
  border: 1px solid var(--c-text);
  border-radius: 6px;
  color: var(--c-text);
  font-weight: 600;
  font-size: 14px;
  background: transparent;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s ease;
}

.headline-card__btn:hover {
  background: var(--c-brand, #5b4be8);
  border-color: var(--c-brand, #5b4be8);
  color: #fff;
}

.headline-card__media {
  position: relative;
  background: #111;
  min-height: 280px;
}

.headline-card__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.headline-card__placeholder {
  width: 100%;
  height: 100%;
  min-height: 280px;
  background: linear-gradient(135deg, #181a24, #2c254e);
  display: flex;
  align-items: center;
  justify-content: center;
}

.headline-card__mark {
  font-size: 14px;
  letter-spacing: 0.2em;
  color: rgba(255, 255, 255, 0.4);
  font-weight: 700;
}

/* 4 列网格卡片 */
.news-cards-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 24px;
  margin-bottom: 48px;
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
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.3s var(--ease-out), box-shadow 0.3s var(--ease-out), border-color 0.3s ease;
}

.news-tile:hover {
  transform: translateY(-5px);
  box-shadow: 0 16px 36px rgba(10, 12, 20, 0.08);
  border-color: rgba(91, 75, 232, 0.35);
}

.news-tile__media {
  aspect-ratio: 16 / 10;
  width: 100%;
  background: #222;
  overflow: hidden;
}

.news-tile__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.news-tile:hover .news-tile__media img {
  transform: scale(1.05);
}

.news-tile__placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #222533, #15161e);
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
  margin: 0 0 14px;
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
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--c-text-subtle);
}

.news-grid-loading {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.news-skeleton {
  height: 240px;
  border-radius: 12px;
  background: var(--c-surface);
}
</style>
