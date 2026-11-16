<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getSolutions } from '@/api/public'
import { usePageMeta } from '@/composables/usePageMeta'
import { usePageHero } from '@/composables/usePageHero'
import { useRealtimeSync } from '@/composables/useRealtimeSync'
import type { PageResult, SolutionSummary } from '@/data/types'

const route = useRoute()
const router = useRouter()
const { hero: heroConfig, refreshHero } = usePageHero('solutions')

const page = ref(Math.max(1, Number(route.query.page) || 1))
const result = ref<PageResult<SolutionSummary> | null>(null)
const loading = ref(true)
const failed = ref(false)
let reqSeq = 0

usePageMeta(() => ({
  title: heroConfig.value?.title || '解决方案',
  description: heroConfig.value?.subtitle || '面向各行业场景的数字化解决方案。',
}))

function onPageChange(p: number) {
  page.value = p
  const query: Record<string, string> = {}
  if (p > 1) query.page = String(p)
  router.replace({ path: '/solutions', query }).catch(() => {})
}

async function loadSolutions() {
  const currentSeq = ++reqSeq
  loading.value = true
  failed.value = false
  try {
    const res = await getSolutions({ page: page.value, pageSize: 10 })
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

watch(
  () => route.query.page,
  (qPage) => {
    const nextPage = Math.max(1, Number(qPage) || 1)
    if (nextPage !== page.value) page.value = nextPage
  },
)

watch(page, loadSolutions)

useRealtimeSync(['siteConfigVersion', 'contentVersion'], async (changed, force) => {
  if (force || changed.includes('siteConfigVersion')) {
    await refreshHero()
  }
  if (force || changed.includes('contentVersion')) {
    await loadSolutions()
  }
})

onMounted(loadSolutions)
</script>

<template>
  <div>
    <PageHeader
      :title="heroConfig?.title || '解决方案'"
      :description="heroConfig?.subtitle ?? '按行业场景组织的落地方案，涵盖业务痛点、架构路径与可验证成果。'"
      :eyebrow="heroConfig?.eyebrow ?? 'SOLUTIONS'"
      :bg-type="heroConfig?.bgType || 'default'"
      :bg-image="heroConfig?.bgImage"
      :bg-video="heroConfig?.bgVideo"
      :overlay-opacity="heroConfig?.overlayOpacity"
      :tone="heroConfig?.bgType === 'image' || heroConfig?.bgType === 'video' ? 'dark' : 'light'"
      :crumbs="[{ label: '首页', path: '/' }, { label: '解决方案' }]"
    />

    <section class="section">
      <div class="container">
        <div v-if="loading" class="grid grid--2" aria-busy="true">
          <div v-for="n in 2" :key="n" class="skeleton"></div>
        </div>

        <EmptyState v-else-if="failed" title="方案加载失败" description="请稍后刷新页面重试。">
          <button type="button" class="btn btn--ghost" @click="loadSolutions">重新加载</button>
        </EmptyState>

        <EmptyState
          v-else-if="!result || result.items.length === 0"
          title="暂无公开方案"
          description="可以先查看产品中心，或联系我们沟通定制方案。"
        />

        <div v-else class="list">
          <article
            v-for="(item, index) in result.items"
            :key="item.id"
            v-reveal="index * 60"
            class="solution"
          >
            <RouterLink :to="`/solutions/${item.id}`" class="solution__link">
              <div class="solution__media media-frame">
                <img
                  v-if="item.cover"
                  :src="item.cover.url"
                  :alt="item.cover.alt || item.title"
                  loading="lazy"
                />
                <div v-else class="solution__placeholder"></div>
              </div>
              <div class="solution__body">
                <div class="solution__meta">
                  <span v-if="item.industry || item.categoryName" class="tag">
                    {{ item.industry || item.categoryName }}
                  </span>
                </div>
                <h2 class="solution__title">{{ item.title }}</h2>
                <p class="solution__desc">{{ item.summary }}</p>
                <span class="solution__more">查看方案详情 <span aria-hidden="true">→</span></span>
              </div>
            </RouterLink>
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
.list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.solution__link {
  display: grid;
  grid-template-columns: 380px minmax(0, 1fr);
  gap: 36px;
  align-items: center;
  padding: 20px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  background: #ffffff;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.solution__link:hover {
  border-color: #93c5fd;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.solution__media {
  aspect-ratio: 16 / 10;
  border-radius: var(--radius-sm);
}

.solution__placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #1e293b, #0f172a);
}

.solution__title {
  margin: 12px 0 8px;
  font-size: 22px;
  font-weight: 600;
}

.solution__desc {
  font-size: 14.5px;
  color: var(--c-text-muted);
  line-height: 1.75;
}

.solution__more {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 16px;
  font-size: 14px;
  font-weight: 500;
  color: var(--c-brand);
}

@media (max-width: 900px) {
  .solution__link {
    grid-template-columns: 1fr;
    gap: 18px;
  }
}
</style>
