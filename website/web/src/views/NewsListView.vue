<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getNews } from '@/api/public'
import { usePageMeta } from '@/composables/usePageMeta'
import type { NewsSummary, PageResult } from '@/data/types'

const page = ref(1)
const result = ref<PageResult<NewsSummary> | null>(null)
const loading = ref(true)
const failed = ref(false)

usePageMeta(() => ({
  title: '新闻中心',
  description: '产品版本、公司动态与技术文章，按发布时间倒序排列。',
}))

async function load() {
  loading.value = true
  failed.value = false
  try {
    result.value = await getNews({ page: page.value, pageSize: 8 })
  } catch {
    failed.value = true
  } finally {
    loading.value = false
  }
}

watch(page, load)
onMounted(load)
</script>

<template>
  <div>
    <PageHeader
      title="新闻中心"
      description="产品更新、公司动态与技术文章。技术类内容只写我们实际验证过的结论。"
      :crumbs="[{ label: '首页', path: '/' }, { label: '新闻' }]"
    />

    <section class="section">
      <div class="container">
        <div v-if="loading" aria-busy="true">
          <div v-for="n in 3" :key="n" class="skeleton"></div>
        </div>

        <EmptyState v-else-if="failed" title="新闻加载失败" description="请稍后刷新页面重试。" />

        <EmptyState v-else-if="!result || result.items.length === 0" title="暂无新闻" description="稍后再来看看。" />

        <ul v-else class="list">
          <li v-for="item in result.items" :key="item.id" class="item">
            <RouterLink :to="`/news/${item.id}`" class="item__link">
              <img
                v-if="item.cover"
                :src="item.cover.url"
                :alt="item.cover.alt"
                :width="item.cover.width"
                :height="item.cover.height"
                loading="lazy"
                class="item__cover"
              />
              <div class="item__body">
                <div class="item__meta">
                  <time :datetime="item.displayTime">{{ item.displayTime }}</time>
                  <span v-if="item.categoryName" class="tag">{{ item.categoryName }}</span>
                </div>
                <h2 class="item__title">{{ item.title }}</h2>
                <p class="item__desc">{{ item.summary }}</p>
              </div>
            </RouterLink>
          </li>
        </ul>

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
.list {
  list-style: none;
  margin: 0;
  padding: 0;
  border-top: 1px solid var(--c-border);
}

.item {
  border-bottom: 1px solid var(--c-border);
}

.item__link {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 24px;
  padding: 24px 0;
}

.item__cover {
  width: 100%;
  aspect-ratio: 16 / 9;
  object-fit: cover;
  border-radius: var(--radius-sm);
  border: 1px solid var(--c-border);
}

.item__meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: var(--c-text-subtle);
  font-variant-numeric: tabular-nums;
}

.item__title {
  margin: 8px 0 6px;
  font-size: 19px;
  font-weight: 500;
  line-height: 1.5;
}

.item__link:hover .item__title {
  color: var(--c-brand);
}

.item__desc {
  font-size: 14px;
  color: var(--c-text-muted);
  line-height: 1.8;
}

.skeleton {
  height: 120px;
  border-radius: var(--radius-sm);
  background: var(--c-surface);
  margin-bottom: 12px;
}

@media (max-width: 760px) {
  .item__link {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  .item__cover {
    max-width: 100%;
  }
  .item__title {
    font-size: 17px;
  }
}
</style>
