<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getNewsDetail } from '@/api/public'
import { usePageMeta } from '@/composables/usePageMeta'
import type { NewsDetail } from '@/data/types'

const route = useRoute()
const data = ref<NewsDetail | null>(null)
const loading = ref(true)

usePageMeta(() => ({
  title: data.value ? `${data.value.title}｜新闻` : '新闻详情',
  description: data.value?.summary ?? '新闻详情',
  noindex: !loading.value && !data.value,
}))

async function load() {
  loading.value = true
  data.value = await getNewsDetail(String(route.params.id))
  loading.value = false
}

watch(() => route.params.id, load)
onMounted(load)
</script>

<template>
  <div v-if="loading" class="container state" aria-busy="true"><div class="skeleton"></div></div>

  <div v-else-if="!data">
    <PageHeader title="文章不存在" :crumbs="[{ label: '首页', path: '/' }, { label: '新闻', path: '/news' }]" />
    <section class="section">
      <div class="container">
        <EmptyState title="该文章可能已下线或链接有误">
          <RouterLink to="/news" class="btn btn--ghost">返回新闻列表</RouterLink>
        </EmptyState>
      </div>
    </section>
  </div>

  <article v-else class="article">
    <PageHeader
      :title="data.title"
      :crumbs="[{ label: '首页', path: '/' }, { label: '新闻', path: '/news' }, { label: data.categoryName || '正文' }]"
    />

    <div class="container article__body">
      <div class="meta">
        <time :datetime="data.displayTime">{{ data.displayTime }}</time>
        <span v-if="data.categoryName" class="tag">{{ data.categoryName }}</span>
      </div>

      <figure v-if="data.cover" class="cover">
        <img :src="data.cover.url" :alt="data.cover.alt" :width="data.cover.width" :height="data.cover.height" />
      </figure>

      <!-- 正文由后端清洗后下发 -->
      <div class="prose" v-html="data.bodyHtml"></div>

      <nav class="adjacent" aria-label="相邻文章">
        <RouterLink v-if="data.prev" :to="`/news/${data.prev.id}`" class="adjacent__link">
          <span class="adjacent__label">上一篇</span>
          <span class="adjacent__title">{{ data.prev.title }}</span>
        </RouterLink>
        <RouterLink v-if="data.next" :to="`/news/${data.next.id}`" class="adjacent__link adjacent__link--next">
          <span class="adjacent__label">下一篇</span>
          <span class="adjacent__title">{{ data.next.title }}</span>
        </RouterLink>
      </nav>

      <div class="back">
        <RouterLink to="/news" class="link-more">← 返回新闻列表</RouterLink>
      </div>
    </div>
  </article>
</template>

<style scoped>
.article__body {
  max-width: 820px;
  padding-top: 40px;
}

.meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: var(--c-text-subtle);
  font-variant-numeric: tabular-nums;
  margin-bottom: 24px;
}

.cover {
  margin-bottom: 32px;
}

.cover img {
  width: 100%;
  border-radius: var(--radius);
  border: 1px solid var(--c-border);
}

.adjacent {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 48px;
  padding-top: 28px;
  border-top: 1px solid var(--c-border);
}

.adjacent__link {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 16px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
}

.adjacent__link:hover {
  border-color: var(--c-brand);
}

.adjacent__link--next {
  text-align: right;
}

.adjacent__label {
  font-size: 12px;
  color: var(--c-text-subtle);
}

.adjacent__title {
  font-size: 14px;
  line-height: 1.6;
}

.back {
  margin-top: 32px;
}

.state {
  padding: 72px 24px;
}

.skeleton {
  height: 320px;
  border-radius: var(--radius);
  background: var(--c-surface);
}

@media (max-width: 700px) {
  .adjacent {
    grid-template-columns: 1fr;
  }
  .adjacent__link--next {
    text-align: left;
  }
}
</style>
