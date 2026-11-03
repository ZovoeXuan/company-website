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
      eyebrow="NEWSROOM"
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
          <li v-for="(item, index) in result.items" :key="item.id" v-reveal="index * 60" class="item">
            <RouterLink :to="`/news/${item.id}`" class="item__link">
              <div v-if="item.cover" class="item__media media-frame">
                <img
                  :src="item.cover.url"
                  :alt="item.cover.alt"
                  :width="item.cover.width"
                  :height="item.cover.height"
                  loading="lazy"
                />
              </div>
              <div class="item__body">
                <div class="item__meta">
                  <time :datetime="item.displayTime">{{ item.displayTime }}</time>
                  <span v-if="item.categoryName" class="tag">{{ item.categoryName }}</span>
                </div>
                <h2 class="item__title">{{ item.title }}</h2>
                <p class="item__desc">{{ item.summary }}</p>
                <span class="item__more">阅读全文 <span aria-hidden="true">→</span></span>
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
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 36px;
  align-items: center;
  padding: 30px 8px;
  transition: background-color 0.3s ease, padding 0.3s var(--ease-out);
}

.item__link:hover {
  background: var(--c-surface);
  padding-left: 24px;
  padding-right: 24px;
}

.item__media {
  aspect-ratio: 16 / 10;
  border-radius: var(--radius-sm);
}

.item__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item__meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: var(--c-text-subtle);
  font-variant-numeric: tabular-nums;
}

.item__title {
  margin: 12px 0 8px;
  font-size: clamp(20px, 2.2vw, 26px);
  font-weight: 600;
  line-height: 1.4;
  letter-spacing: -0.025em;
  transition: color 0.2s ease;
}

.item__link:hover .item__title {
  color: var(--c-brand);
}

.item__desc {
  font-size: 14px;
  color: var(--c-text-muted);
  line-height: 1.8;
  max-width: 620px;
}

.item__more {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 14px;
  font-size: 13px;
  color: var(--c-brand);
}

.item__more span {
  transition: transform 0.3s var(--ease-out);
}

.item__link:hover .item__more span {
  transform: translateX(4px);
}

.skeleton {
  height: 150px;
  border-radius: var(--radius-sm);
  background: var(--c-surface);
  margin-bottom: 12px;
}

@media (max-width: 860px) {
  .item__link {
    grid-template-columns: 1fr;
    gap: 18px;
    padding: 24px 4px;
  }
  .item__media {
    aspect-ratio: 16 / 8;
  }
  .item__title {
    font-size: 18px;
  }
}
</style>
