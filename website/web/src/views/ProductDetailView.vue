<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import ArticleBody from '@shared/ArticleBody.vue'
import { getProduct } from '@/api/public'
import { openConsult } from '@/composables/consult'
import { usePageMeta } from '@/composables/usePageMeta'
import { ApiError } from '@/api/http'
import type { MediaRef, ProductDetail } from '@/data/types'

const route = useRoute()
const data = ref<ProductDetail | null>(null)
const loading = ref(true)
const failed = ref(false)

const heroMedia = computed<MediaRef | null>(() => data.value?.hero ?? data.value?.cover ?? null)

usePageMeta(() => ({
  title: data.value ? `${data.value.title}｜产品中心` : '产品详情',
  description: data.value?.summary ?? '产品详情',
  noindex: !loading.value && !data.value,
}))

function isVideo(media: MediaRef | null): boolean {
  return media?.type === 'VIDEO'
}

async function load() {
  loading.value = true
  failed.value = false
  try {
    data.value = await getProduct(String(route.params.id))
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
  <div v-if="loading" class="container state" aria-busy="true">
    <div class="skeleton"></div>
  </div>

  <div v-else-if="failed">
    <PageHeader
      title="产品加载失败"
      :crumbs="[{ label: '首页', path: '/' }, { label: '产品中心', path: '/products' }, { label: '加载失败' }]"
    />
    <section class="section">
      <div class="container">
        <EmptyState title="内容加载失败" description="可能是网络或服务异常，请稍后重试。">
          <button type="button" class="btn btn--ghost" @click="load">重新加载</button>
        </EmptyState>
      </div>
    </section>
  </div>

  <div v-else-if="!data">
    <PageHeader title="产品不存在" :crumbs="[{ label: '首页', path: '/' }, { label: '产品', path: '/products' }, { label: '未找到' }]" />
    <section class="section">
      <div class="container">
        <EmptyState title="该产品可能已下线或链接有误" description="可以查看全部产品，或直接联系我们。">
          <RouterLink to="/products" class="btn btn--ghost">返回产品列表</RouterLink>
        </EmptyState>
      </div>
    </section>
  </div>

  <div v-else>
    <PageHeader
      :title="data.title"
      :description="data.summary"
      eyebrow="PRODUCT"
      :crumbs="[{ label: '首页', path: '/' }, { label: '产品', path: '/products' }, { label: data.title }]"
    />

    <section class="section product">
      <div class="container">
        <div v-if="heroMedia" v-reveal class="product__hero media-frame">
          <video
            v-if="isVideo(heroMedia)"
            :src="heroMedia.url"
            autoplay
            muted
            loop
            playsinline
            preload="metadata"
          ></video>
          <img
            v-else
            :src="heroMedia.url"
            :alt="heroMedia.alt || data.title"
            :width="heroMedia.width"
            :height="heroMedia.height"
          />
          <span v-if="isVideo(heroMedia)" class="product__video-tag">产品视频</span>
        </div>

        <div class="product__layout">
          <div class="product__main">
            <section v-if="data.features?.length" class="feature-grid" aria-labelledby="feature-title">
              <h2 id="feature-title" class="block-title">核心能力</h2>
              <ul class="feature-grid__list">
                <li
                  v-for="(item, index) in data.features"
                  :key="item.title"
                  v-reveal="index * 70"
                  class="feature-cell"
                >
                  <span class="feature-cell__num">{{ String(index + 1).padStart(2, '0') }}</span>
                  <h3 class="feature-cell__title">{{ item.title }}</h3>
                  <p v-if="item.description" class="feature-cell__desc">{{ item.description }}</p>
                </li>
              </ul>
            </section>

            <ArticleBody :body-html="data.bodyHtml">
              <section v-if="data.relatedCases?.length" class="related" aria-labelledby="related-cases">
                <h2 id="related-cases" class="block-title">相关案例</h2>
                <ul class="related__list">
                  <li v-for="item in data.relatedCases ?? []" :key="item.id">
                    <RouterLink :to="`/cases/${item.id}`" class="related__link">
                      <span v-if="item.industry" class="tag">{{ item.industry }}</span>
                      <span class="related__name">{{ item.title }}</span>
                      <span class="related__arrow" aria-hidden="true">→</span>
                    </RouterLink>
                  </li>
                </ul>
              </section>
            </ArticleBody>
          </div>

          <aside class="product__aside" aria-label="咨询">
            <div class="aside-card">
              <p class="aside-card__kicker">TALK TO AN ENGINEER</p>
              <p class="aside-card__title">需要针对你的场景评估？</p>
              <p class="aside-card__desc">留下联系方式，我们会先了解设备类型与现有系统，再给建议。</p>
              <button
                type="button"
                class="btn btn--primary aside-card__btn"
                @click="openConsult({ sourcePosition: 'PRODUCT_DETAIL', contentType: 'PRODUCT', contentId: data.id })"
              >
                预约演示 <span aria-hidden="true">→</span>
              </button>
              <p class="aside-card__note">不会转交第三方，也不会用于电话推销。</p>
            </div>

            <div v-if="data.categoryName" class="aside-meta">
              <p class="aside-meta__label">所属分类</p>
              <p class="aside-meta__value">{{ data.categoryName }}</p>
            </div>
          </aside>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.product__hero {
  aspect-ratio: 16 / 7;
  border-radius: var(--radius-lg);
  margin-bottom: 80px;
}

.product__hero img,
.product__hero video {
  height: 100%;
  object-fit: cover;
}

.product__video-tag {
  position: absolute;
  left: 24px;
  bottom: 20px;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(8, 8, 12, 0.6);
  color: #fff;
  font-size: 12px;
  letter-spacing: 0.08em;
  backdrop-filter: blur(8px);
}

.product__layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 72px;
  align-items: start;
}

.block-title {
  font-size: 26px;
  letter-spacing: -0.02em;
  margin-bottom: 24px;
}

.feature-grid {
  margin-bottom: 64px;
}

.feature-grid__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  border-top: 1px solid var(--c-border);
  border-left: 1px solid var(--c-border);
}

.feature-cell {
  padding: 28px;
  border-right: 1px solid var(--c-border);
  border-bottom: 1px solid var(--c-border);
  transition: background-color 0.3s ease;
}

.feature-cell:hover {
  background: var(--c-surface);
}

.feature-cell__num {
  font-size: 12px;
  letter-spacing: 0.12em;
  color: var(--c-brand);
}

.feature-cell__title {
  margin-top: 14px;
  font-size: 19px;
  letter-spacing: -0.02em;
}

.feature-cell__desc {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.8;
  color: var(--c-text-muted);
}

.related {
  margin-top: 64px;
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
  gap: 12px;
  padding: 16px 18px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  background: #fff;
  transition: border-color 0.2s ease, transform 0.3s var(--ease-out);
}

.related__link:hover {
  border-color: var(--c-brand);
  transform: translateX(4px);
}

.related__name {
  font-size: 15px;
}

.related__arrow {
  margin-left: auto;
  color: var(--c-brand);
}

.product__aside {
  position: sticky;
  top: calc(var(--header-height) + 28px);
  display: flex;
  flex-direction: column;
  gap: 16px;
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

.aside-card__note {
  margin-top: 14px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.38);
}

.aside-meta {
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  padding: 18px;
}

.aside-meta__label {
  font-size: 11px;
  letter-spacing: 0.06em;
  color: var(--c-text-subtle);
}

.aside-meta__value {
  margin-top: 6px;
  font-size: 14px;
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
  .product__hero {
    aspect-ratio: 16 / 10;
    margin-bottom: 48px;
  }
  .product__layout {
    grid-template-columns: 1fr;
    gap: 40px;
  }
  .product__aside {
    position: static;
  }
}

@media (max-width: 720px) {
  .feature-grid__list {
    grid-template-columns: 1fr;
  }
  .feature-cell {
    padding: 22px;
  }
}
</style>
