<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getCase } from '@/api/public'
import { usePageMeta } from '@/composables/usePageMeta'
import type { CaseDetail } from '@/data/types'

const route = useRoute()
const data = ref<CaseDetail | null>(null)
const loading = ref(true)

usePageMeta(() => ({
  title: data.value ? `${data.value.title}｜客户案例` : '案例详情',
  description: data.value?.summary ?? '客户案例详情',
  noindex: !loading.value && !data.value,
}))

async function load() {
  loading.value = true
  data.value = await getCase(String(route.params.id))
  loading.value = false
}

watch(() => route.params.id, load)
onMounted(load)
</script>

<template>
  <div v-if="loading" class="container state" aria-busy="true"><div class="skeleton"></div></div>

  <div v-else-if="!data">
    <PageHeader title="案例不存在" :crumbs="[{ label: '首页', path: '/' }, { label: '客户案例', path: '/cases' }]" />
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
      :crumbs="[{ label: '首页', path: '/' }, { label: '客户案例', path: '/cases' }, { label: data.industry }]"
    />

    <section class="section">
      <div class="container layout">
        <article>
          <figure v-if="data.cover" class="cover">
            <img :src="data.cover.url" :alt="data.cover.alt" :width="data.cover.width" :height="data.cover.height" />
          </figure>

          <dl class="results" aria-label="关键结果">
            <div v-for="item in data.results" :key="item.label" class="results__item">
              <dt>{{ item.label }}</dt>
              <dd>{{ item.value }}</dd>
            </div>
          </dl>

          <div class="block">
            <h2 class="block__title">客户与背景</h2>
            <p class="block__text">{{ data.client }}</p>
            <p class="block__text">{{ data.background }}</p>
          </div>

          <div class="block">
            <h2 class="block__title">我们做了什么</h2>
            <ol class="steps">
              <li v-for="(action, index) in data.actions" :key="action" class="steps__item">
                <span class="steps__num" aria-hidden="true">{{ index + 1 }}</span>
                <span>{{ action }}</span>
              </li>
            </ol>
          </div>

          <blockquote v-if="data.quote" class="quote">
            <p class="quote__text">{{ data.quote.text }}</p>
            <footer class="quote__author">—— {{ data.quote.author }}</footer>
          </blockquote>

          <section v-if="data.relatedProducts?.length" class="related" aria-labelledby="related-products">
            <h2 id="related-products" class="block__title">用到的产品</h2>
            <ul class="related__list">
              <li v-for="item in data.relatedProducts ?? []" :key="item.id">
                <RouterLink :to="`/products/${item.id}`" class="related__link">{{ item.title }}</RouterLink>
              </li>
            </ul>
          </section>
        </article>

        <aside class="aside">
          <div class="aside__card">
            <p class="aside__title">想了解同类场景的做法？</p>
            <p class="aside__desc">我们可以在沟通前先给出判断：这个问题是否值得做、大概需要多少投入。</p>
            <RouterLink to="/contact" class="btn btn--primary aside__btn">联系我们</RouterLink>
          </div>
        </aside>
      </div>
    </section>
  </div>
</template>

<style scoped>
.layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 56px;
  align-items: start;
}

.cover {
  margin-bottom: 32px;
}

.cover img {
  width: 100%;
  border-radius: var(--radius);
  border: 1px solid var(--c-border);
}

.results {
  display: flex;
  flex-wrap: wrap;
  gap: 40px;
  margin: 0 0 40px;
  padding: 24px;
  background: var(--c-surface);
  border-radius: var(--radius);
}

.results__item dt {
  font-size: 13px;
  color: var(--c-text-muted);
}

.results__item dd {
  margin: 4px 0 0;
  font-size: 26px;
  font-weight: 500;
  color: var(--c-brand);
}

.block + .block {
  margin-top: 40px;
}

.block__title {
  font-size: 20px;
  font-weight: 500;
  margin-bottom: 14px;
}

.block__text {
  font-size: 15px;
  line-height: 1.9;
  color: #2b3138;
}

.block__text + .block__text {
  margin-top: 12px;
}

.steps {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.steps__item {
  display: flex;
  gap: 12px;
  font-size: 15px;
  line-height: 1.8;
}

.steps__num {
  flex: none;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--c-brand-soft);
  color: var(--c-brand);
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-top: 3px;
}

.quote {
  margin: 40px 0 0;
  padding: 24px;
  border-left: 3px solid var(--c-brand);
  background: var(--c-brand-soft);
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
}

.quote__text {
  font-size: 17px;
  line-height: 1.8;
}

.quote__author {
  margin-top: 12px;
  font-size: 14px;
  color: var(--c-text-muted);
}

.related {
  margin-top: 40px;
  padding-top: 28px;
  border-top: 1px solid var(--c-border);
}

.related__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.related__link {
  display: inline-block;
  padding: 10px 16px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  font-size: 14px;
}

.related__link:hover {
  border-color: var(--c-brand);
  color: var(--c-brand);
}

.aside {
  position: sticky;
  top: calc(var(--header-height) + 24px);
}

.aside__card {
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  background: var(--c-surface);
  padding: 24px;
}

.aside__title {
  font-size: 17px;
  font-weight: 500;
  line-height: 1.5;
}

.aside__desc {
  margin-top: 10px;
  font-size: 14px;
  color: var(--c-text-muted);
}

.aside__btn {
  width: 100%;
  margin-top: 18px;
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
  .layout {
    grid-template-columns: 1fr;
    gap: 32px;
  }
  .aside {
    position: static;
  }
  .results {
    gap: 28px;
  }
}
</style>
