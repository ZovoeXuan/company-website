<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getCases, getCaseCategories } from '@/api/public'
import { usePageMeta } from '@/composables/usePageMeta'
import { openVideo } from '@/composables/videoModal'
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

function playCaseVideo(item: CaseSummary, event: Event) {
  event.preventDefault()
  event.stopPropagation()
  openVideo({
    title: `${item.customerName || '客户案例'} | ${item.title}`,
    url: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4'
  })
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
      title="客户成功故事"
      description="每个案例都写清了三件事：原来什么问题、做了什么、结果怎么衡量。"
      eyebrow="CUSTOMER STORIES"
      tone="light"
      centered
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

        <!-- 对齐视频：双列视频案例卡片 + 播放器按钮 + 客户原声引言 -->
        <div v-else class="case-grid">
          <div
            v-for="(item, index) in result.items"
            :key="item.id"
            v-reveal="index * 70"
            class="case-story-card"
          >
            <!-- 左侧：封面与悬浮播放按钮 -->
            <div class="case-story-card__media">
              <img
                v-if="item.cover"
                :src="item.cover.url"
                :alt="item.cover.alt || item.title"
                loading="lazy"
              />
              <div v-else class="case-story-card__placeholder"></div>
              <button
                type="button"
                class="case-story-card__play"
                aria-label="播放客户专访视频"
                @click="playCaseVideo(item, $event)"
              >
                <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor" aria-hidden="true">
                  <path d="M8 5v14l11-7z" />
                </svg>
              </button>
            </div>

            <!-- 右侧：客户证言与成果 -->
            <div class="case-story-card__content">
              <blockquote class="case-story-card__quote">
                “{{ item.summary }}”
              </blockquote>

              <div class="case-story-card__author">
                <p class="case-story-card__customer">{{ item.customerName || item.title }}</p>
                <div class="case-story-card__tags">
                  <span v-if="item.industry || item.categoryName" class="case-story-card__industry">
                    {{ item.industry || item.categoryName }}
                  </span>
                </div>
              </div>

              <dl v-if="item.outcomes?.length" class="case-story-card__outcomes">
                <div v-for="o in item.outcomes" :key="o.label" class="case-story-card__outcome">
                  <dt>{{ o.label }}</dt>
                  <dd>{{ o.displayValue }}</dd>
                </div>
              </dl>

              <RouterLink :to="`/cases/${item.id}`" class="case-story-card__link">
                查看实践详情 <span aria-hidden="true">→</span>
              </RouterLink>
            </div>
          </div>
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
  background: var(--c-brand, #5b4be8);
  border-color: var(--c-brand, #5b4be8);
  color: #fff;
}

.case-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 32px;
  align-items: stretch;
}

@media (max-width: 900px) {
  .case-grid {
    grid-template-columns: 1fr;
  }
}

.case-story-card {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  background: #ffffff;
  border: 1px solid var(--c-border);
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(10, 12, 20, 0.04);
  transition: transform 0.3s var(--ease-out), box-shadow 0.3s var(--ease-out);
}

.case-story-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 36px rgba(10, 12, 20, 0.08);
  border-color: rgba(91, 75, 232, 0.3);
}

@media (max-width: 600px) {
  .case-story-card {
    grid-template-columns: 1fr;
  }
}

.case-story-card__media {
  position: relative;
  aspect-ratio: 4 / 3;
  width: 100%;
  height: 100%;
  background: #111;
  overflow: hidden;
}

.case-story-card__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.case-story-card__placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #1c1e28, #111319);
}

.case-story-card__play {
  position: absolute;
  left: 14px;
  bottom: 14px;
  width: 36px;
  height: 36px;
  border-radius: 6px;
  background: #ffffff;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #111;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
  transition: transform 0.2s ease, background-color 0.2s ease, color 0.2s ease;
}

.case-story-card__play:hover {
  background: var(--c-brand, #5b4be8);
  color: #ffffff;
  transform: scale(1.08);
}

.case-story-card__content {
  padding: 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.case-story-card__quote {
  margin: 0 0 18px;
  font-size: 15px;
  line-height: 1.6;
  color: var(--c-text);
  font-weight: 500;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.case-story-card__author {
  margin-bottom: 16px;
}

.case-story-card__customer {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--c-text-muted);
}

.case-story-card__tags {
  margin-top: 6px;
}

.case-story-card__industry {
  display: inline-block;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  background: var(--c-surface);
  color: var(--c-text-subtle);
  border: 1px solid var(--c-border);
}

.case-story-card__outcomes {
  display: flex;
  gap: 18px;
  margin: 0 0 16px;
  padding: 10px 0;
  border-top: 1px solid var(--c-border);
  border-bottom: 1px solid var(--c-border);
}

.case-story-card__outcome dt {
  font-size: 11px;
  color: var(--c-text-subtle);
}

.case-story-card__outcome dd {
  margin: 2px 0 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--c-brand, #5b4be8);
}

.case-story-card__link {
  font-size: 13px;
  font-weight: 600;
  color: var(--c-brand, #5b4be8);
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: transform 0.2s ease;
}

.case-story-card__link:hover {
  transform: translateX(4px);
}

.skeleton {
  height: 280px;
  border-radius: var(--radius);
  background: var(--c-surface);
}
</style>
