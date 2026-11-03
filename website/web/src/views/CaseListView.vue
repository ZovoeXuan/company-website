<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getCases, getCaseCategories } from '@/api/public'
import { usePageMeta } from '@/composables/usePageMeta'
import type { CaseSummary, Category, PageResult } from '@/data/types'

/** 行业筛选项来自已发布的案例分类，不写死在前端。 */
const industries = ref<Category[]>([])
const industry = ref<string | null>(null)
const page = ref(1)
const result = ref<PageResult<CaseSummary> | null>(null)
const loading = ref(true)
const failed = ref(false)

usePageMeta(() => ({
  title: '客户案例',
  description: '按行业查看设备管理与能源管理项目的实际做法与结果，客户名称按协议隐去。',
}))

async function load() {
  loading.value = true
  failed.value = false
  try {
    result.value = await getCases({ page: page.value, pageSize: 8, categoryId: industry.value })
  } catch {
    failed.value = true
  } finally {
    loading.value = false
  }
}

function selectIndustry(value: string | null) {
  if (industry.value === value) return
  industry.value = value
  page.value = 1
}

watch([industry, page], load)
onMounted(async () => {
  try {
    industries.value = await getCaseCategories()
  } catch {
    industries.value = []
  }
  await load()
})
</script>

<template>
  <div>
    <PageHeader
      title="客户案例"
      description="每个案例都写清了三件事：原来什么问题、做了什么、结果怎么衡量。"
      :crumbs="[{ label: '首页', path: '/' }, { label: '客户案例' }]"
    />

    <section class="section">
      <div class="container">
        <div class="filters" role="group" aria-label="按行业筛选">
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

        <div v-if="loading" class="grid grid--3 list" aria-busy="true">
          <div v-for="n in 2" :key="n" class="skeleton"></div>
        </div>

        <EmptyState v-else-if="failed" title="案例加载失败" description="请稍后刷新页面重试。" />

        <EmptyState
          v-else-if="!result || result.items.length === 0"
          title="该行业暂无公开案例"
          description="可以切换其他行业，或联系我们了解相近场景的做法。"
        />

        <div v-else class="grid grid--3 list">
          <RouterLink
            v-for="(item, index) in result.items"
            :key="item.id"
            :to="`/cases/${item.id}`"
            v-reveal="index * 70"
            class="card case"
          >
            <div class="case__media media-frame">
              <img
                v-if="item.cover"
                :src="item.cover.url"
                :alt="item.cover.alt"
                :width="item.cover.width"
                :height="item.cover.height"
                loading="lazy"
              />
              <div v-else class="case__placeholder"></div>
            </div>
            <div class="case__body">
              <div class="case__meta">
                <span v-if="item.industry" class="tag">{{ item.industry }}</span>
                <span v-if="item.customerName" class="case__customer">{{ item.customerName }}</span>
              </div>
              <h2 class="case__title">{{ item.title }}</h2>
              <p class="card__desc">{{ item.summary }}</p>
              <dl v-if="item.outcomes?.length" class="case__results">
                <div v-for="o in item.outcomes ?? []" :key="o.label" class="case__result">
                  <dt>{{ o.label }}</dt>
                  <dd>{{ o.displayValue }}</dd>
                </div>
              </dl>
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
  gap: 8px;
  margin-bottom: 36px;
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

.list {
  align-items: start;
}

.case {
  display: flex;
  flex-direction: column;
  gap: 20px;
  transition: transform 0.4s var(--ease-out);
}

.case:hover {
  transform: translateY(-6px);
}

.case__media {
  aspect-ratio: 4 / 3;
  border-radius: var(--radius);
}

.case__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.case__placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #17171f, #0a0a10);
}

.case__meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.case__customer {
  font-size: 12px;
  color: var(--c-text-muted);
}

.case__title {
  margin: 12px 0 8px;
  font-size: 20px;
  font-weight: 600;
  line-height: 1.45;
  letter-spacing: -0.02em;
}

.case__results {
  display: flex;
  gap: 26px;
  margin: 18px 0 0;
}

.case__result dt {
  font-size: 11px;
  color: var(--c-text-subtle);
}

.case__result dd {
  margin: 2px 0 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--c-brand);
  letter-spacing: -0.02em;
}

.skeleton {
  height: 280px;
  border-radius: var(--radius);
  background: var(--c-surface);
}
</style>
