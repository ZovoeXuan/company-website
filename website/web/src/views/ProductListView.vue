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
        />

        <EmptyState
          v-else-if="!result || result.items.length === 0"
          title="该分类下暂无产品"
          description="可以切换其他分类，或直接联系我们了解定制方案。"
        >
          <RouterLink to="/contact" class="btn btn--ghost empty-action">联系我们</RouterLink>
        </EmptyState>

        <div v-else class="grid grid--3">
          <RouterLink
            v-for="(item, index) in result.items"
            :key="item.id"
            :to="`/products/${item.id}`"
            v-reveal="index * 70"
            class="card product"
          >
            <div class="product__media media-frame">
              <img
                v-if="item.cover"
                :src="item.cover.url"
                :alt="item.cover.alt"
                :width="item.cover.width"
                :height="item.cover.height"
                loading="lazy"
              />
              <div v-else class="product__placeholder"></div>
              <span class="product__arrow" aria-hidden="true">→</span>
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
          @change="(p: number) => (page = p)"
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
  min-height: 38px;
  padding: 0 16px;
  border: 1px solid var(--c-border);
  border-radius: 999px;
  background: transparent;
  font-size: 14px;
  color: var(--c-text-muted);
  cursor: pointer;
  transition: background-color 0.2s ease, border-color 0.2s ease, color 0.2s ease;
}

.filters__btn:hover {
  border-color: var(--c-border-strong);
  color: var(--c-text);
}

.filters__btn.is-active {
  background: var(--c-text);
  border-color: var(--c-text);
  color: #fff;
}

.count {
  font-size: 13px;
  color: var(--c-text-subtle);
}

.product {
  display: block;
}

.product__media {
  aspect-ratio: 4 / 3;
  border-radius: var(--radius);
}

.product__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product__placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #17171f, #0a0a10);
}

.product__arrow {
  position: absolute;
  right: 18px;
  top: 18px;
  width: 36px;
  height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  backdrop-filter: blur(6px);
  opacity: 0;
  transform: translate(-6px, 6px);
  transition: opacity 0.3s ease, transform 0.3s var(--ease-out);
}

.product:hover .product__arrow {
  opacity: 1;
  transform: none;
}

.product__title {
  margin-top: 12px;
}

.empty-action {
  margin-top: 20px;
}

.skeleton {
  height: 280px;
  border-radius: var(--radius);
  background: var(--c-surface);
}

@media (max-width: 640px) {
  .list-head {
    align-items: flex-start;
  }
}
</style>
