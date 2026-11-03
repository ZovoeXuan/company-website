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
      eyebrow="SOLUTIONS"
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
          <article v-for="(item, index) in items" :key="item.id" v-reveal="index * 70" class="solution">
            <RouterLink :to="`/solutions/${item.id}`" class="solution__link">
              <div class="solution__media media-frame">
                <img
                  v-if="item.cover"
                  :src="item.cover.url"
                  :alt="item.cover.alt"
                  :width="item.cover.width"
                  :height="item.cover.height"
                  loading="lazy"
                />
                <div v-else class="solution__placeholder"></div>
              </div>
              <div class="solution__body">
                <div class="solution__meta">
                  <span class="tag">{{ item.industry || item.categoryName }}</span>
                  <span class="solution__index">{{ String(index + 1).padStart(2, '0') }}</span>
                </div>
                <h2 class="solution__title">{{ item.title }}</h2>
                <p class="solution__desc">{{ item.summary }}</p>
                <span class="solution__more">查看方案 <span aria-hidden="true">→</span></span>
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
  gap: 24px;
}

.solution {
  transition: transform 0.4s var(--ease-out);
}

.solution:hover {
  transform: translateY(-4px);
}

.solution__link {
  display: grid;
  grid-template-columns: 420px minmax(0, 1fr);
  gap: 44px;
  align-items: center;
  padding: 20px;
  border-radius: var(--radius-lg);
  transition: background-color 0.3s ease;
}

.solution:hover .solution__link {
  background: var(--c-surface);
}

.solution__media {
  aspect-ratio: 16 / 10;
  border-radius: var(--radius);
}

.solution__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.solution__placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #17171f, #0a0a10);
}

.solution__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.solution__index {
  font-size: 12px;
  letter-spacing: 0.12em;
  color: var(--c-text-subtle);
}

.solution__title {
  margin: 16px 0 10px;
  font-size: clamp(24px, 2.6vw, 34px);
  font-weight: 600;
  line-height: 1.3;
  letter-spacing: -0.03em;
}

.solution__desc {
  font-size: 15px;
  color: var(--c-text-muted);
  line-height: 1.85;
  max-width: 560px;
}

.solution__more {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 20px;
  font-size: 14px;
  color: var(--c-brand);
}

.solution__more span {
  transition: transform 0.3s var(--ease-out);
}

.solution:hover .solution__more span {
  transform: translateX(5px);
}

.skeleton {
  height: 260px;
  border-radius: var(--radius);
  background: var(--c-surface);
}

@media (max-width: 900px) {
  .solution__link {
    grid-template-columns: 1fr;
    gap: 22px;
    padding: 12px;
  }
  .solution__media {
    aspect-ratio: 16 / 9;
  }
  .solution__title {
    font-size: 22px;
  }
}
</style>
