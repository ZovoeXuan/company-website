<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getProductCategories, getProducts } from '@/api/public'
import { usePageMeta } from '@/composables/usePageMeta'
import { usePageHero } from '@/composables/usePageHero'
import { useRealtimeSync } from '@/composables/useRealtimeSync'
import type { PageResult, ProductSummary } from '@/data/types'

const route = useRoute()
const router = useRouter()
const { hero: heroConfig, refreshHero } = usePageHero('products')

const categories = ref<Array<{ id: string; name: string }>>([])
const categoryId = ref<string | null>(
  typeof route.query.category === 'string' && route.query.category ? route.query.category : null,
)
const page = ref(Math.max(1, Number(route.query.page) || 1))
const result = ref<PageResult<ProductSummary> | null>(null)
const loading = ref(true)
const failed = ref(false)
let reqSeq = 0

usePageMeta(() => ({
  title: heroConfig.value?.title || '产品中心',
  description: heroConfig.value?.subtitle || '浏览已发布的产品体系与技术规格。',
}))

function syncQueryToUrl() {
  const query: Record<string, string> = {}
  if (categoryId.value) query.category = categoryId.value
  if (page.value > 1) query.page = String(page.value)
  router.replace({ path: '/products', query }).catch(() => {})
}

async function load() {
  const currentSeq = ++reqSeq
  loading.value = true
  failed.value = false
  try {
    const res = await getProducts({
      page: page.value,
      pageSize: 9,
      categoryId: categoryId.value,
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

function selectCategory(id: string | null) {
  if (categoryId.value === id) return
  categoryId.value = id
  page.value = 1
  syncQueryToUrl()
}

function onPageChange(p: number) {
  page.value = p
  syncQueryToUrl()
}

watch(
  () => [route.query.category, route.query.page],
  ([qCat, qPage]) => {
    const nextCat = typeof qCat === 'string' && qCat ? qCat : null
    const nextPage = Math.max(1, Number(qPage) || 1)
    if (nextCat !== categoryId.value || nextPage !== page.value) {
      categoryId.value = nextCat
      page.value = nextPage
    }
  },
)

watch([categoryId, page], load)

useRealtimeSync(['siteConfigVersion', 'contentVersion'], async (changed, force) => {
  if (force || changed.includes('siteConfigVersion')) {
    await refreshHero()
  }
  if (force || changed.includes('contentVersion')) {
    categories.value = await getProductCategories().catch(() => categories.value)
    await load()
  }
})

onMounted(async () => {
  categories.value = await getProductCategories().catch(() => [])
  await load()
})
</script>

<template>
  <div>
    <PageHeader
      :title="heroConfig?.title || '产品中心'"
      :description="heroConfig?.subtitle ?? '按业务场景选择核心产品模块，支持灵活组合与全栈部署。'"
      :eyebrow="heroConfig?.eyebrow ?? 'PRODUCTS'"
      :bg-type="heroConfig?.bgType || 'default'"
      :bg-image="heroConfig?.bgImage"
      :bg-video="heroConfig?.bgVideo"
      :overlay-opacity="heroConfig?.overlayOpacity"
      :tone="heroConfig?.bgType === 'image' || heroConfig?.bgType === 'video' ? 'dark' : 'light'"
      centered
      :crumbs="[{ label: '首页', path: '/' }, { label: '产品中心' }]"
    />

    <section class="section">
      <div class="container">
        <div class="list-head">
          <div class="filters" role="group" aria-label="按分类筛选">
            <button
              type="button"
              class="filters__btn"
              :class="{ 'is-active': categoryId === null }"
              :aria-pressed="categoryId === null"
              @click="selectCategory(null)"
            >
              全部
            </button>
            <button
              v-for="item in categories"
              :key="item.id"
              type="button"
              class="filters__btn"
              :class="{ 'is-active': categoryId === item.id }"
              :aria-pressed="categoryId === item.id"
              @click="selectCategory(item.id)"
            >
              {{ item.name }}
            </button>
          </div>
          <p class="count" role="status">
            {{ loading ? '正在加载…' : `共 ${result?.total ?? 0} 个产品` }}
          </p>
        </div>

        <div v-if="loading" class="grid grid--3" aria-busy="true">
          <div v-for="n in 3" :key="n" class="skeleton"></div>
        </div>

        <EmptyState
          v-else-if="failed"
          title="产品加载失败"
          description="请稍后刷新页面重试。"
        >
          <button type="button" class="btn btn--ghost empty-action" @click="load">重新加载</button>
        </EmptyState>

        <EmptyState
          v-else-if="!result || result.items.length === 0"
          title="该分类下暂无已发布产品"
          description="可以切换其他分类，或直接联系我们了解定制方案。"
        >
          <RouterLink to="/contact" class="btn btn--ghost empty-action">联系我们</RouterLink>
        </EmptyState>

        <div v-else class="grid grid--3">
          <RouterLink
            v-for="(item, index) in result.items"
            :key="item.id"
            :to="`/products/${item.id}`"
            v-reveal="index * 60"
            class="card product"
          >
            <div class="product__media media-frame">
              <img
                v-if="item.cover"
                :src="item.cover.url"
                :alt="item.cover.alt || item.title"
                :width="item.cover.width"
                :height="item.cover.height"
                loading="lazy"
              />
              <div v-else class="product__placeholder"></div>
            </div>
            <div class="card__body">
              <span v-if="item.categoryName" class="tag">{{ item.categoryName }}</span>
              <h2 class="card__title product__title">{{ item.title }}</h2>
              <p class="card__desc">{{ item.summary }}</p>
            </div>
          </RouterLink>
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
.list-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
  margin-bottom: 28px;
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
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

.count {
  font-size: 13px;
  color: var(--c-text-subtle);
}

.product__media {
  aspect-ratio: 16 / 10;
  border-radius: 0;
}

.product__placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #1e293b, #0f172a);
}

.product__title {
  margin-top: 10px;
}

.empty-action {
  margin-top: 20px;
}
</style>
