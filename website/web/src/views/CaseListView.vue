<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getCases } from '@/api/public'
import { usePageMeta } from '@/composables/usePageMeta'
import type { CaseSummary, PageResult } from '@/data/types'

// 行业筛选项：后端分类接口就绪后改为读取 /api/public/categories?type=CASE
const industries = ['汽车零部件', '城市水务', '电子制造', '化工园区']

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
    result.value = await getCases({ page: page.value, pageSize: 8, industry: industry.value })
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
onMounted(load)
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
            :key="item"
            type="button"
            class="filters__btn"
            :class="{ 'is-active': industry === item }"
            :aria-pressed="industry === item"
            @click="selectIndustry(item)"
          >
            {{ item }}
          </button>
        </div>

        <div v-if="loading" class="grid grid--2 list" aria-busy="true">
          <div v-for="n in 2" :key="n" class="skeleton"></div>
        </div>

        <EmptyState v-else-if="failed" title="案例加载失败" description="请稍后刷新页面重试。" />

        <EmptyState
          v-else-if="!result || result.items.length === 0"
          title="该行业暂无公开案例"
          description="可以切换其他行业，或联系我们了解相近场景的做法。"
        />

        <div v-else class="grid grid--2 list">
          <RouterLink v-for="item in result.items" :key="item.id" :to="`/cases/${item.id}`" class="card case">
            <img
              v-if="item.cover"
              :src="item.cover.url"
              :alt="item.cover.alt"
              :width="item.cover.width"
              :height="item.cover.height"
              loading="lazy"
              class="case__cover"
            />
            <div class="case__body">
              <span class="tag">{{ item.industry }}</span>
              <h2 class="case__title">{{ item.title }}</h2>
              <p class="card__desc">{{ item.summary }}</p>
              <dl class="case__results">
                <div v-for="r in item.results" :key="r.label" class="case__result">
                  <dt>{{ r.label }}</dt>
                  <dd>{{ r.value }}</dd>
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
  gap: 10px;
  margin-bottom: 28px;
}

.filters__btn {
  min-height: 38px;
  padding: 0 16px;
  border: 1px solid var(--c-border);
  background: #fff;
  border-radius: 999px;
  font-size: 14px;
  color: var(--c-text-muted);
  cursor: pointer;
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

.case {
  display: block;
}

.case__cover {
  width: 100%;
  aspect-ratio: 16 / 9;
  object-fit: cover;
}

.case__body {
  padding: 24px;
}

.case__title {
  margin: 12px 0 8px;
  font-size: 19px;
  font-weight: 500;
  line-height: 1.5;
}

.case__results {
  display: flex;
  gap: 28px;
  margin: 18px 0 0;
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

.skeleton {
  height: 280px;
  border-radius: var(--radius);
  background: var(--c-surface);
}
</style>
