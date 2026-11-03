<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getSolution } from '@/api/public'
import { openConsult } from '@/composables/consult'
import { usePageMeta } from '@/composables/usePageMeta'
import { ApiError } from '@/api/http'
import type { MediaRef, SolutionDetail } from '@/data/types'

const route = useRoute()
const data = ref<SolutionDetail | null>(null)
const loading = ref(true)
const failed = ref(false)

const heroMedia = computed<MediaRef | null>(() => data.value?.hero ?? data.value?.cover ?? null)

usePageMeta(() => ({
  title: data.value ? `${data.value.title}｜解决方案` : '方案详情',
  description: data.value?.summary ?? '行业解决方案详情',
  noindex: !loading.value && !data.value,
}))

function isVideo(media: MediaRef | null): boolean {
  return media?.type === 'VIDEO'
}

async function load() {
  loading.value = true
  failed.value = false
  try {
    data.value = await getSolution(String(route.params.id))
  } catch (error) {
    data.value = null
    failed.value = !(error instanceof ApiError && (error.status === 404 || error.status === 400))
  } finally {
    loading.value = false
  }
}

watch(() => route.params.id, load)
onMounted(load)
</script>

<template>
  <div v-if="loading" class="container state" aria-busy="true"><div class="skeleton"></div></div>

  <div v-else-if="failed">
    <PageHeader title="方案加载失败" :crumbs="[{ label: '首页', path: '/' }, { label: '解决方案', path: '/solutions' }, { label: '加载失败' }]" />
    <section class="section">
      <div class="container">
        <EmptyState title="内容加载失败" description="可能是网络或服务异常，请稍后重试。">
          <button type="button" class="btn btn--ghost" @click="load">重新加载</button>
        </EmptyState>
      </div>
    </section>
  </div>

  <div v-else-if="!data">
    <PageHeader title="方案不存在" :crumbs="[{ label: '首页', path: '/' }, { label: '解决方案', path: '/solutions' }, { label: '未找到' }]" />
    <section class="section">
      <div class="container">
        <EmptyState title="该方案可能已下线或链接有误">
          <RouterLink to="/solutions" class="btn btn--ghost">返回方案列表</RouterLink>
        </EmptyState>
      </div>
    </section>
  </div>

  <div v-else>
    <PageHeader
      :title="data.title"
      :description="data.summary"
      eyebrow="SOLUTION"
      :crumbs="[
        { label: '首页', path: '/' },
        { label: '解决方案', path: '/solutions' },
        { label: data.industry || data.categoryName || '方案详情' },
      ]"
    />

    <section class="section solution">
      <div class="container">
        <div v-if="heroMedia" v-reveal class="solution__hero media-frame">
          <video
            v-if="isVideo(heroMedia)"
            :src="heroMedia.url"
            autoplay
            muted
            loop
            playsinline
            preload="metadata"
          ></video>
          <img v-else :src="heroMedia.url" :alt="heroMedia.alt || data.title" loading="lazy" />
        </div>

        <div class="solution__layout">
          <article class="solution__main">
            <section v-if="data.pains?.length" class="block" aria-labelledby="pains-title">
              <p class="kicker">CHALLENGES</p>
              <h2 id="pains-title" class="block-title">业务痛点</h2>
              <ul class="cell-list">
                <li
                  v-for="(item, index) in data.pains"
                  :key="item.title"
                  v-reveal="index * 70"
                  class="cell"
                >
                  <span class="cell__num">{{ String(index + 1).padStart(2, '0') }}</span>
                  <h3 class="cell__title">{{ item.title }}</h3>
                  <p v-if="item.description" class="cell__desc">{{ item.description }}</p>
                </li>
              </ul>
            </section>

            <section class="block">
              <p class="kicker">APPROACH</p>
              <h2 class="block-title">方案说明</h2>
              <div class="prose" v-html="data.bodyHtml"></div>
            </section>

            <section v-if="data.benefits?.length" class="block" aria-labelledby="benefits-title">
              <p class="kicker">OUTCOMES</p>
              <h2 id="benefits-title" class="block-title">价值说明</h2>
              <ul class="cell-list">
                <li
                  v-for="(item, index) in data.benefits"
                  :key="item.title"
                  v-reveal="index * 70"
                  class="cell"
                >
                  <span class="cell__num">{{ String(index + 1).padStart(2, '0') }}</span>
                  <h3 class="cell__title">{{ item.title }}</h3>
                  <p v-if="item.description" class="cell__desc">{{ item.description }}</p>
                </li>
              </ul>
            </section>

            <section v-if="data.relatedProducts?.length" class="block" aria-labelledby="related-products">
              <p class="kicker">RELATED PRODUCTS</p>
              <h2 id="related-products" class="block-title">用到的产品</h2>
              <ul class="related__list">
                <li v-for="item in data.relatedProducts ?? []" :key="item.id">
                  <RouterLink :to="`/products/${item.id}`" class="related__link">
                    <span class="related__name">{{ item.title }}</span>
                    <span class="related__desc">{{ item.summary }}</span>
                    <span class="related__arrow" aria-hidden="true">→</span>
                  </RouterLink>
                </li>
              </ul>
            </section>
          </article>

          <aside class="solution__aside" aria-label="咨询">
            <div class="aside-card">
              <p class="aside-card__kicker">TALK TO AN ENGINEER</p>
              <p class="aside-card__title">你的场景和这个类似？</p>
              <p class="aside-card__desc">带上设备类型与现有系统情况，我们会判断是否适用，而不是先卖方案。</p>
              <button
                type="button"
                class="btn btn--primary aside-card__btn"
                @click="openConsult({ sourcePosition: 'SOLUTION_DETAIL', contentType: 'SOLUTION', contentId: data.id })"
              >
                聊聊你的场景 <span aria-hidden="true">→</span>
              </button>
            </div>
          </aside>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.solution__hero {
  aspect-ratio: 16 / 7;
  border-radius: var(--radius-lg);
  margin-bottom: 80px;
}

.solution__hero img,
.solution__hero video {
  height: 100%;
  object-fit: cover;
}

.solution__layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 72px;
  align-items: start;
}

.block {
  margin-bottom: 64px;
}

.block-title {
  font-size: 26px;
  letter-spacing: -0.02em;
  margin-bottom: 24px;
}

.cell-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  border-top: 1px solid var(--c-border);
  border-left: 1px solid var(--c-border);
}

.cell {
  padding: 28px;
  border-right: 1px solid var(--c-border);
  border-bottom: 1px solid var(--c-border);
  transition: background-color 0.3s ease;
}

.cell:hover {
  background: var(--c-surface);
}

.cell__num {
  font-size: 12px;
  letter-spacing: 0.12em;
  color: var(--c-brand);
}

.cell__title {
  margin-top: 14px;
  font-size: 18px;
  letter-spacing: -0.02em;
}

.cell__desc {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.8;
  color: var(--c-text-muted);
}

.related__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.related__link {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 28px;
  gap: 4px 16px;
  padding: 18px 20px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  transition: border-color 0.2s ease, transform 0.3s var(--ease-out);
}

.related__link:hover {
  border-color: var(--c-brand);
  transform: translateX(4px);
}

.related__name {
  font-size: 16px;
  font-weight: 500;
}

.related__desc {
  grid-column: 1;
  font-size: 13px;
  color: var(--c-text-muted);
}

.related__arrow {
  grid-row: 1 / 3;
  align-self: center;
  color: var(--c-brand);
}

.solution__aside {
  position: sticky;
  top: calc(var(--header-height) + 28px);
}

.aside-card {
  border-radius: var(--radius);
  background: var(--c-navy);
  color: #fff;
  padding: 30px 26px;
}

.aside-card__kicker {
  font-size: 10px;
  letter-spacing: 0.16em;
  color: rgba(255, 255, 255, 0.45);
  margin-bottom: 16px;
}

.aside-card__title {
  font-size: 20px;
  font-weight: 600;
  line-height: 1.5;
  color: #fff;
}

.aside-card__desc {
  margin-top: 12px;
  font-size: 13px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.6);
}

.aside-card__btn {
  width: 100%;
  margin-top: 22px;
  background: #fff;
  color: var(--c-navy);
}

.aside-card__btn:hover {
  background: var(--c-brand);
  color: #fff;
}

.state {
  padding: 72px 24px;
}

.skeleton {
  height: 320px;
  border-radius: var(--radius);
  background: var(--c-surface);
}

@media (max-width: 980px) {
  .solution__hero {
    aspect-ratio: 16 / 10;
    margin-bottom: 48px;
  }
  .solution__layout {
    grid-template-columns: 1fr;
    gap: 40px;
  }
  .solution__aside {
    position: static;
  }
}

@media (max-width: 720px) {
  .cell-list {
    grid-template-columns: 1fr;
  }
  .cell {
    padding: 22px;
  }
}
</style>
