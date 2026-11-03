<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getProductCategories, getProducts } from '@/api/public'
import { usePageMeta } from '@/composables/usePageMeta'
import type { PageResult, ProductSummary } from '@/data/types'

const categories = ref<Array<{ id: string; name: string }>>([])
const categoryId = ref<string | null>(null)
const page = ref(1)
const result = ref<PageResult<ProductSummary> | null>(null)
const loading = ref(true)
const failed = ref(false)

usePageMeta(() => ({
  title: '产品中心',
  description: '设备管理平台、预测性维护、能耗监测与边缘网关等产品，覆盖设备接入到运维决策全流程。',
}))

async function load() {
  loading.value = true
  failed.value = false
  try {
    result.value = await getProducts({ page: page.value, pageSize: 9, categoryId: categoryId.value })
  } catch {
    failed.value = true
  } finally {
    loading.value = false
  }
}

function selectCategory(id: string | null) {
  if (categoryId.value === id) return
  categoryId.value = id
  page.value = 1
}

watch([categoryId, page], load)

onMounted(async () => {
  categories.value = await getProductCategories()
  await load()
})
</script>

<template>
  <div>
    <PageHeader
      title="产品中心"
      description="从设备接入到运维决策，按产线实际情况选择模块，也可以整套落地。"
      :crumbs="[{ label: '首页', path: '/' }, { label: '产品' }]"
    />

    <section class="section">
      <div class="container">
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

        <div v-if="loading" class="grid grid--3" aria-busy="true">
          <div v-for="n in 3" :key="n" class="skeleton"></div>
        </div>

        <EmptyState
          v-else-if="failed"
          title="产品加载失败"
          description="请稍后刷新页面重试。"
        />

        <EmptyState
          v-else-if="!result || result.items.length === 0"
          title="该分类下暂无产品"
          description="可以切换其他分类，或直接联系我们了解定制方案。"
        >
          <RouterLink to="/contact" class="btn btn--ghost empty-action">联系我们</RouterLink>
        </EmptyState>

        <div v-else class="grid grid--3">
          <RouterLink v-for="item in result.items" :key="item.id" :to="`/products/${item.id}`" class="card product">
            <img
              v-if="item.cover"
              :src="item.cover.url"
              :alt="item.cover.alt"
              :width="item.cover.width"
              :height="item.cover.height"
              loading="lazy"
              class="product__cover"
            />
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
          @change="(p: number) => (page = p)"
        />
      </div>
    </section>
  </div>
</template>

<style scoped>
.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 28px;
  border-bottom: 1px solid var(--c-border);
}

/* 筛选器做成下划线标签，避免一排彩色胶囊按钮抢走视线 */
.filters__btn {
  position: relative;
  min-height: 46px;
  padding: 0;
  border: none;
  background: none;
  font-size: 15px;
  color: var(--c-text-muted);
  cursor: pointer;
  transition: color 0.16s ease;
}

.filters__btn:hover {
  color: var(--c-text);
}

.filters__btn.is-active {
  color: var(--c-text);
  font-weight: 500;
}

.filters__btn.is-active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -1px;
  height: 2px;
  background: var(--c-brand);
}

.count {
  margin: 20px 0 20px;
  font-size: 14px;
  color: var(--c-text-muted);
}

.product {
  display: block;
}

.product__cover {
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
}

.product__title {
  margin-top: 10px;
}

.empty-action {
  margin-top: 20px;
}

.skeleton {
  height: 280px;
  border-radius: var(--radius);
  background: var(--c-surface);
}
</style>
