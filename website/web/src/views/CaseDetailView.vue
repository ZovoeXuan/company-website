<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getCase } from '@/api/public'
import { openConsult } from '@/composables/consult'
import { usePageMeta } from '@/composables/usePageMeta'
import { ApiError } from '@/api/http'
import type { CaseDetail, MediaRef } from '@/data/types'

const route = useRoute()
const data = ref<CaseDetail | null>(null)
const loading = ref(true)
const failed = ref(false)

const heroMedia = computed<MediaRef | null>(() => data.value?.hero ?? data.value?.cover ?? null)

usePageMeta(() => ({
  title: data.value ? `${data.value.title}｜客户案例` : '案例详情',
  description: data.value?.summary ?? '客户案例详情',
  noindex: !loading.value && !data.value,
}))

function isVideo(media: MediaRef | null): boolean {
  return media?.type === 'VIDEO'
}

async function load() {
  loading.value = true
  failed.value = false
  try {
    data.value = await getCase(String(route.params.id))
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
    <PageHeader title="案例加载失败" :crumbs="[{ label: '首页', path: '/' }, { label: '客户案例', path: '/cases' }, { label: '加载失败' }]" />
    <section class="section">
      <div class="container">
        <EmptyState title="内容加载失败" description="可能是网络或服务异常，请稍后重试。">
          <button type="button" class="btn btn--ghost" @click="load">重新加载</button>
        </EmptyState>
      </div>
    </section>
  </div>

  <div v-else-if="!data">
    <PageHeader title="案例不存在" :crumbs="[{ label: '首页', path: '/' }, { label: '客户案例', path: '/cases' }, { label: '未找到' }]" />
    <section class="section">
      <div class="container">
        <EmptyState title="该案例可能已下线或链接有误">
          <RouterLink to="/cases" class="btn btn--ghost">返回案例列表</RouterLink>
        </EmptyState>
      </div>
    </section>
  </div>

  <div v-else>
    <PageHeader
      :title="data.title"
      :description="data.summary"
      eyebrow="CUSTOMER STORY"
      :crumbs="[
        { label: '首页', path: '/' },
        { label: '客户案例', path: '/cases' },
        { label: data.industry || data.categoryName || '案例详情' },
      ]"
    />

    <section class="section case">
      <div class="container">
        <div v-if="heroMedia" v-reveal class="case__hero media-frame">
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

        <div class="case__facts">
          <div v-if="data.customerName" class="case__fact">
            <p class="case__fact-label">客户</p>
            <p class="case__fact-value">{{ data.customerName }}</p>
          </div>
          <div v-if="data.industry" class="case__fact">
            <p class="case__fact-label">行业</p>
            <p class="case__fact-value">{{ data.industry }}</p>
          </div>
          <div v-if="data.categoryName" class="case__fact">
            <p class="case__fact-label">场景</p>
            <p class="case__fact-value">{{ data.categoryName }}</p>
          </div>
        </div>

        <div class="case__layout">
          <article class="case__main">
            <div class="prose" v-html="data.bodyHtml"></div>

            <dl v-if="data.outcomes?.length" class="case__outcomes" aria-label="关键结果">
              <div v-for="outcome in data.outcomes ?? []" :key="outcome.label" class="case__outcome">
                <dt>{{ outcome.label }}</dt>
                <dd>{{ outcome.displayValue }}</dd>
              </div>
            </dl>

            <blockquote v-if="data.quoteText" class="case__quote">
              <p class="case__quote-text">“{{ data.quoteText }}”</p>
              <footer v-if="data.quoteAuthor" class="case__quote-author">—— {{ data.quoteAuthor }}</footer>
            </blockquote>

            <section v-if="data.relatedProducts?.length" class="related" aria-labelledby="related-products">
              <h2 id="related-products" class="block-title">用到的产品</h2>
              <ul class="related__list">
                <li v-for="item in data.relatedProducts ?? []" :key="item.id">
                  <RouterLink :to="`/products/${item.id}`" class="related__link">
                    <span>{{ item.title }}</span>
                    <span class="related__arrow" aria-hidden="true">→</span>
                  </RouterLink>
                </li>
              </ul>
            </section>

            <section v-if="data.relatedSolutions?.length" class="related" aria-labelledby="related-solutions">
              <h2 id="related-solutions" class="block-title">相关方案</h2>
              <ul class="related__list">
                <li v-for="item in data.relatedSolutions ?? []" :key="item.id">
                  <RouterLink :to="`/solutions/${item.id}`" class="related__link">
                    <span>{{ item.title }}</span>
                    <span class="related__arrow" aria-hidden="true">→</span>
                  </RouterLink>
                </li>
              </ul>
            </section>
          </article>

          <aside class="case__aside" aria-label="咨询">
            <div class="aside-card">
              <p class="aside-card__kicker">SIMILAR SCENARIO?</p>
              <p class="aside-card__title">想了解同类场景的做法？</p>
              <p class="aside-card__desc">我们可以在沟通前先给出判断：这个问题是否值得做、大概需要多少投入。</p>
              <button
                type="button"
                class="btn btn--primary aside-card__btn"
                @click="openConsult({ sourcePosition: 'CASE_DETAIL', contentType: 'CASE', contentId: data.id })"
              >
                联系我们 <span aria-hidden="true">→</span>
              </button>
            </div>
          </aside>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.case__hero {
  aspect-ratio: 16 / 7;
  border-radius: var(--radius-lg);
  margin-bottom: 48px;
}

.case__hero img,
.case__hero video {
  height: 100%;
  object-fit: cover;
}

.case__facts {
  display: flex;
  gap: 64px;
  flex-wrap: wrap;
  padding: 26px 0;
  border-top: 1px solid var(--c-border);
  border-bottom: 1px solid var(--c-border);
  margin-bottom: 64px;
}

.case__fact-label {
  font-size: 11px;
  letter-spacing: 0.08em;
  color: var(--c-text-subtle);
}

.case__fact-value {
  margin-top: 6px;
  font-size: 16px;
  font-weight: 500;
}

.case__layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 72px;
  align-items: start;
}

.case__outcomes {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 1px;
  margin: 48px 0 0;
  background: var(--c-border);
  border-radius: var(--radius);
  overflow: hidden;
}

.case__outcome {
  background: var(--c-mist);
  padding: 28px;
}

.case__outcome dt {
  font-size: 12px;
  color: var(--c-text-muted);
}

.case__outcome dd {
  margin: 8px 0 0;
  font-size: clamp(30px, 3.4vw, 44px);
  font-weight: 600;
  letter-spacing: -0.04em;
  color: var(--c-brand);
  line-height: 1;
}

.case__quote {
  margin: 48px 0 0;
  padding: 36px 32px;
  background: var(--c-navy);
  color: #fff;
  border-radius: var(--radius);
}

.case__quote-text {
  font-size: clamp(22px, 2.4vw, 30px);
  line-height: 1.6;
  font-weight: 500;
  letter-spacing: -0.02em;
}

.case__quote-author {
  margin-top: 20px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.55);
}

.block-title {
  font-size: 22px;
  letter-spacing: -0.02em;
  margin-bottom: 18px;
}

.related {
  margin-top: 56px;
  padding-top: 32px;
  border-top: 1px solid var(--c-border);
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  transition: border-color 0.2s ease, transform 0.3s var(--ease-out);
}

.related__link:hover {
  border-color: var(--c-brand);
  transform: translateX(4px);
}

.related__arrow {
  color: var(--c-brand);
}

.case__aside {
  position: sticky;
  top: calc(var(--header-height) + 28px);
}

.aside-card {
  border-radius: var(--radius);
  background: var(--c-surface);
  padding: 30px 26px;
}

.aside-card__kicker {
  font-size: 10px;
  letter-spacing: 0.16em;
  color: var(--c-text-subtle);
  margin-bottom: 16px;
}

.aside-card__title {
  font-size: 20px;
  font-weight: 600;
  line-height: 1.5;
}

.aside-card__desc {
  margin-top: 12px;
  font-size: 13px;
  line-height: 1.8;
  color: var(--c-text-muted);
}

.aside-card__btn {
  width: 100%;
  margin-top: 22px;
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
  .case__hero {
    aspect-ratio: 16 / 10;
    margin-bottom: 36px;
  }
  .case__layout {
    grid-template-columns: 1fr;
    gap: 40px;
  }
  .case__aside {
    position: static;
  }
  .case__facts {
    gap: 32px;
    margin-bottom: 40px;
  }
}

@media (max-width: 720px) {
  .case__quote {
    padding: 26px 22px;
  }
}
</style>
