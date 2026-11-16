<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getCases, getCaseCategories } from '@/api/public'
import { usePageMeta } from '@/composables/usePageMeta'
import { usePageHero } from '@/composables/usePageHero'
import { useRealtimeSync } from '@/composables/useRealtimeSync'
import { openVideo } from '@/composables/videoModal'
import type { CaseSummary, Category, PageResult } from '@/data/types'

const route = useRoute()
const router = useRouter()
const { hero: heroConfig, refreshHero } = usePageHero('cases')

const industries = ref<Category[]>([])
const industry = ref<string | null>(
  typeof route.query.category === 'string' && route.query.category ? route.query.category : null,
)
const page = ref(Math.max(1, Number(route.query.page) || 1))
const result = ref<PageResult<CaseSummary> | null>(null)
const loading = ref(true)
const failed = ref(false)
let reqSeq = 0

usePageMeta(() => ({
  title: heroConfig.value?.title || '客户案例',
  description: heroConfig.value?.subtitle || '按行业查看客户实践与量化成果。',
}))

function syncQueryToUrl() {
  const query: Record<string, string> = {}
  if (industry.value) query.category = industry.value
  if (page.value > 1) query.page = String(page.value)
  router.replace({ path: '/cases', query }).catch(() => {})
}

async function load() {
  const currentSeq = ++reqSeq
  loading.value = true
  failed.value = false
  try {
    const res = await getCases({ page: page.value, pageSize: 8, categoryId: industry.value })
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

function selectIndustry(value: string | null) {
  if (industry.value === value) return
  industry.value = value
  page.value = 1
  syncQueryToUrl()
}

function onPageChange(p: number) {
  page.value = p
  syncQueryToUrl()
}

function getVideoUrl(item: CaseSummary): string {
  return ((item as unknown as Record<string, unknown>).videoUrl as string) || ''
}

function playCaseVideo(item: CaseSummary, event: Event) {
  event.preventDefault()
  event.stopPropagation()
  const videoUrl = getVideoUrl(item)
  if (!videoUrl) return
  openVideo({
    title: `${item.customerName || '客户案例'} | ${item.title}`,
    url: videoUrl,
  })
}

watch(
  () => [route.query.category, route.query.page],
  ([qCat, qPage]) => {
    const nextCat = typeof qCat === 'string' && qCat ? qCat : null
    const nextPage = Math.max(1, Number(qPage) || 1)
    if (nextCat !== industry.value || nextPage !== page.value) {
      industry.value = nextCat
      page.value = nextPage
    }
  },
)

watch([industry, page], load)

useRealtimeSync(['siteConfigVersion', 'contentVersion'], async (changed, force) => {
  if (force || changed.includes('siteConfigVersion')) {
    await refreshHero()
  }
  if (force || changed.includes('contentVersion')) {
    industries.value = await getCaseCategories().catch(() => industries.value)
    await load()
  }
})

onMounted(async () => {
  industries.value = await getCaseCategories().catch(() => [])
  await load()
})
</script>

<template>
  <div>
    <PageHeader
      :title="heroConfig?.title || '客户实践案例'"
      :description="heroConfig?.subtitle ?? '呈现真实业务痛点、部署路径与可衡量的运营成效。'"
      :eyebrow="heroConfig?.eyebrow ?? 'CUSTOMER STORIES'"
      :bg-type="heroConfig?.bgType || 'default'"
      :bg-image="heroConfig?.bgImage"
      :bg-video="heroConfig?.bgVideo"
      :overlay-opacity="heroConfig?.overlayOpacity"
      :tone="heroConfig?.bgType === 'image' || heroConfig?.bgType === 'video' ? 'dark' : 'light'"
      centered
      :crumbs="[{ label: '首页', path: '/' }, { label: '客户案例' }]"
    />

    <section class="section">
      <div class="container">
        <div v-if="industries.length > 0" class="filters" role="group" aria-label="按行业筛选">
          <button
            type="button"
            class="filters__btn"
            :class="{ 'is-active': industry === null }"
            :aria-pressed="industry === null"
            @click="selectIndustry(null)"
          >
            全部
          </button>
          <button
            v-for="item in industries"
            :key="item.id"
            type="button"
            class="filters__btn"
            :class="{ 'is-active': industry === item.id }"
            :aria-pressed="industry === item.id"
            @click="selectIndustry(item.id)"
          >
            {{ item.name }}
          </button>
        </div>

        <div v-if="loading" class="grid grid--2" aria-busy="true">
          <div v-for="n in 2" :key="n" class="skeleton"></div>
        </div>

        <EmptyState v-else-if="failed" title="案例加载失败" description="请稍后刷新页面重试。">
          <button type="button" class="btn btn--ghost" @click="load">重新加载</button>
        </EmptyState>

        <EmptyState
          v-else-if="!result || result.items.length === 0"
          title="该行业暂无公开案例"
          description="可以切换其他行业，或联系我们了解相近场景的实践方案。"
        />

        <div v-else class="case-grid">
          <article
            v-for="(item, index) in result.items"
            :key="item.id"
            v-reveal="index * 60"
            class="case-story-card"
          >
            <div class="case-story-card__media">
              <img
                v-if="item.cover"
                :src="item.cover.url"
                :alt="item.cover.alt || item.title"
                loading="lazy"
              />
              <div v-else class="case-story-card__placeholder"></div>
              <button
                v-if="getVideoUrl(item)"
                type="button"
                class="case-story-card__play"
                aria-label="播放案例视频"
                @click="playCaseVideo(item, $event)"
              >
                <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor" aria-hidden="true">
                  <path d="M8 5v14l11-7z" />
                </svg>
              </button>
            </div>

            <div class="case-story-card__content">
              <div>
                <div class="case-story-card__meta">
                  <span v-if="item.industry || item.categoryName" class="tag">
                    {{ item.industry || item.categoryName }}
                  </span>
                  <span v-if="item.customerName" class="case-story-card__customer">
                    {{ item.customerName }}
                  </span>
                </div>
                <h2 class="case-story-card__title">{{ item.title }}</h2>
                <p v-if="item.summary" class="case-story-card__summary">
                  {{ item.summary }}
                </p>
              </div>

              <div>
                <dl v-if="item.outcomes?.length" class="case-story-card__outcomes">
                  <div v-for="o in item.outcomes" :key="o.label" class="case-story-card__outcome">
                    <dt>{{ o.label }}</dt>
                    <dd>{{ o.displayValue }}</dd>
                  </div>
                </dl>

                <RouterLink :to="`/cases/${item.id}`" class="case-story-card__link">
                  查看案例详情 <span aria-hidden="true">→</span>
                </RouterLink>
              </div>
            </div>
          </article>
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
.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 32px;
}

.filters__btn {
  min-height: 36px;
  padding: 0 16px;
  border: 1px solid var(--c-border);
  border-radius: 6px;
  background: transparent;
  font-size: 14px;
  color: var(--c-text-muted);
  cursor: pointer;
  transition: all 0.18s ease;
}

.filters__btn:hover {
  border-color: var(--c-brand);
  color: var(--c-brand);
}

.filters__btn.is-active {
  background: var(--c-brand);
  border-color: var(--c-brand);
  color: #fff;
}

.case-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 24px;
  align-items: stretch;
}

@media (max-width: 900px) {
  .case-grid {
    grid-template-columns: 1fr;
  }
}

.case-story-card {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  background: #ffffff;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  overflow: hidden;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.case-story-card:hover {
  border-color: #93c5fd;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

@media (max-width: 600px) {
  .case-story-card {
    grid-template-columns: 1fr;
  }
}

.case-story-card__media {
  position: relative;
  aspect-ratio: 4 / 3;
  width: 100%;
  height: 100%;
  background: #0f172a;
  overflow: hidden;
}

.case-story-card__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.case-story-card__placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #1e293b, #0f172a);
}

.case-story-card__play {
  position: absolute;
  left: 14px;
  bottom: 14px;
  width: 36px;
  height: 36px;
  border-radius: 6px;
  background: #ffffff;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0f172a;
  cursor: pointer;
}

.case-story-card__content {
  padding: 22px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 16px;
}

.case-story-card__meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.case-story-card__customer {
  font-size: 13px;
  font-weight: 600;
  color: var(--c-text-muted);
}

.case-story-card__title {
  font-size: 18px;
  font-weight: 600;
  line-height: 1.4;
  margin-bottom: 8px;
}

.case-story-card__summary {
  font-size: 14px;
  line-height: 1.7;
  color: var(--c-text-muted);
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.case-story-card__outcomes {
  display: flex;
  gap: 18px;
  margin: 0 0 14px;
  padding: 10px 0;
  border-top: 1px solid var(--c-border);
  border-bottom: 1px solid var(--c-border);
}

.case-story-card__outcome dt {
  font-size: 11px;
  color: var(--c-text-subtle);
}

.case-story-card__outcome dd {
  margin: 2px 0 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--c-brand);
}

.case-story-card__link {
  font-size: 13.5px;
  font-weight: 600;
  color: var(--c-brand);
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
</style>
