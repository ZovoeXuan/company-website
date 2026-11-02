<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getProduct } from '@/api/public'
import { usePageMeta } from '@/composables/usePageMeta'
import type { ProductDetail } from '@/data/types'

const route = useRoute()
const data = ref<ProductDetail | null>(null)
const loading = ref(true)

usePageMeta(() => ({
  title: data.value ? `${data.value.title}｜产品中心` : '产品详情',
  description: data.value?.summary ?? '产品详情',
  noindex: !loading.value && !data.value,
}))

async function load() {
  loading.value = true
  data.value = await getProduct(String(route.params.id))
  loading.value = false
}

watch(() => route.params.id, load)
onMounted(load)
</script>

<template>
  <div v-if="loading" class="container state" aria-busy="true">
    <div class="skeleton"></div>
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
      :crumbs="[{ label: '首页', path: '/' }, { label: '产品', path: '/products' }, { label: data.title }]"
    />

    <section class="section">
      <div class="container detail">
        <article class="detail__main">
          <figure v-if="data.cover" class="detail__cover">
            <img :src="data.cover.url" :alt="data.cover.alt" :width="data.cover.width" :height="data.cover.height" />
          </figure>

          <ul v-if="data.capabilities.length" class="caps" aria-label="核心能力">
            <li v-for="cap in data.capabilities" :key="cap" class="caps__item">{{ cap }}</li>
          </ul>

          <!-- 正文由后端清洗后下发，前端只负责排版 -->
          <div class="prose" v-html="data.bodyHtml"></div>

          <section v-if="data.relatedCases.length" class="related" aria-labelledby="related-cases">
            <h2 id="related-cases" class="related__title">相关案例</h2>
            <ul class="related__list">
              <li v-for="item in data.relatedCases" :key="item.id">
                <RouterLink :to="`/cases/${item.id}`" class="related__link">
                  <span class="tag">{{ item.industry }}</span>
                  <span class="related__name">{{ item.title }}</span>
                </RouterLink>
              </li>
            </ul>
          </section>
        </article>

        <aside class="detail__aside" aria-label="咨询">
          <div class="aside-card">
            <p class="aside-card__title">需要针对你的场景评估？</p>
            <p class="aside-card__desc">留下联系方式，我们会先了解设备类型与现有系统，再给建议。</p>
            <RouterLink to="/contact" class="btn btn--primary aside-card__btn">预约演示</RouterLink>
            <p class="aside-card__note">不会转交第三方，也不会用于电话推销。</p>
          </div>

          <div v-if="data.categoryName" class="aside-meta">
            <p class="aside-meta__label">所属分类</p>
            <p class="aside-meta__value">{{ data.categoryName }}</p>
          </div>
        </aside>
      </div>
    </section>
  </div>
</template>

<style scoped>
.detail {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 56px;
  align-items: start;
}

.detail__cover {
  margin-bottom: 32px;
}

.detail__cover img {
  width: 100%;
  aspect-ratio: 16 / 9;
  object-fit: cover;
  border-radius: var(--radius);
  border: 1px solid var(--c-border);
}

.caps {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  list-style: none;
  margin: 0 0 28px;
  padding: 0;
}

.caps__item {
  font-size: 13px;
  color: var(--c-brand);
  background: var(--c-brand-soft);
  border-radius: 999px;
  padding: 5px 14px;
}

.related {
  margin-top: 48px;
  padding-top: 28px;
  border-top: 1px solid var(--c-border);
}

.related__title {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 16px;
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
  padding: 14px 16px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  background: #fff;
}

.related__link:hover {
  border-color: var(--c-brand);
}

.related__name {
  font-size: 15px;
}

.detail__aside {
  position: sticky;
  top: calc(var(--header-height) + 24px);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.aside-card {
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  background: var(--c-surface);
  padding: 24px;
}

.aside-card__title {
  font-size: 17px;
  font-weight: 500;
  line-height: 1.5;
}

.aside-card__desc {
  margin-top: 10px;
  font-size: 14px;
  color: var(--c-text-muted);
}

.aside-card__btn {
  width: 100%;
  margin-top: 18px;
}

.aside-card__note {
  margin-top: 12px;
  font-size: 12px;
  color: var(--c-text-subtle);
}

.aside-meta {
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  padding: 16px;
}

.aside-meta__label {
  font-size: 12px;
  color: var(--c-text-subtle);
}

.aside-meta__value {
  margin-top: 4px;
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
  .detail {
    grid-template-columns: 1fr;
    gap: 32px;
  }
  .detail__aside {
    position: static;
  }
}
</style>
