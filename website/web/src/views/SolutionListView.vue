<script setup lang="ts">
import { onMounted, ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getSolutions } from '@/api/public'
import { usePageMeta } from '@/composables/usePageMeta'
import type { SolutionSummary } from '@/data/types'

const items = ref<SolutionSummary[]>([])
const loading = ref(true)
const failed = ref(false)

usePageMeta(() => ({
  title: '解决方案',
  description: '面向汽车零部件、城市水务、园区能源等场景的数字化方案，包含问题、做法与可验证的结果。',
}))

onMounted(async () => {
  try {
    const result = await getSolutions({ page: 1, pageSize: 12 })
    items.value = result.items
  } catch {
    failed.value = true
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div>
    <PageHeader
      title="解决方案"
      description="按行业组织的落地方案。先看问题是否相似，再看做法能否复用。"
      :crumbs="[{ label: '首页', path: '/' }, { label: '解决方案' }]"
    />

    <section class="section">
      <div class="container">
        <div v-if="loading" class="grid grid--2" aria-busy="true">
          <div v-for="n in 2" :key="n" class="skeleton"></div>
        </div>

        <EmptyState v-else-if="failed" title="方案加载失败" description="请稍后刷新页面重试。" />

        <EmptyState
          v-else-if="items.length === 0"
          title="暂无公开方案"
          description="可以先查看产品，或联系我们了解行业实践。"
        />

        <div v-else class="list">
          <article v-for="item in items" :key="item.id" class="solution">
            <RouterLink :to="`/solutions/${item.id}`" class="solution__link">
              <div class="solution__media">
                <img
                  v-if="item.cover"
                  :src="item.cover.url"
                  :alt="item.cover.alt"
                  :width="item.cover.width"
                  :height="item.cover.height"
                  loading="lazy"
                />
              </div>
              <div class="solution__body">
                <span class="tag">{{ item.industry }}</span>
                <h2 class="solution__title">{{ item.title }}</h2>
                <p class="solution__desc">{{ item.summary }}</p>
                <span class="solution__more">查看方案 →</span>
              </div>
            </RouterLink>
          </article>
        </div>
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

.solution {
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  overflow: hidden;
  transition: border-color 0.15s ease;
}

.solution:hover {
  border-color: var(--c-brand);
}

.solution__link {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 0;
}

.solution__media {
  background: var(--c-surface);
}

.solution__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.solution__body {
  padding: 28px 32px;
}

.solution__title {
  margin: 12px 0 10px;
  font-size: 22px;
  font-weight: 500;
  line-height: 1.45;
}

.solution__desc {
  font-size: 15px;
  color: var(--c-text-muted);
  line-height: 1.8;
}

.solution__more {
  display: inline-block;
  margin-top: 18px;
  font-size: 14px;
  color: var(--c-brand);
}

.skeleton {
  height: 260px;
  border-radius: var(--radius);
  background: var(--c-surface);
}

@media (max-width: 860px) {
  .solution__link {
    grid-template-columns: 1fr;
  }
  .solution__media img {
    aspect-ratio: 16 / 9;
  }
  .solution__body {
    padding: 20px;
  }
  .solution__title {
    font-size: 18px;
  }
}
</style>
