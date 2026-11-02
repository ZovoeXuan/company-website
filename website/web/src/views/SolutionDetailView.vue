<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getSolution } from '@/api/public'
import { usePageMeta } from '@/composables/usePageMeta'
import type { SolutionDetail } from '@/data/types'

const route = useRoute()
const data = ref<SolutionDetail | null>(null)
const loading = ref(true)

usePageMeta(() => ({
  title: data.value ? `${data.value.title}｜解决方案` : '方案详情',
  description: data.value?.summary ?? '行业解决方案详情',
  noindex: !loading.value && !data.value,
}))

async function load() {
  loading.value = true
  data.value = await getSolution(String(route.params.id))
  loading.value = false
}

watch(() => route.params.id, load)
onMounted(load)
</script>

<template>
  <div v-if="loading" class="container state" aria-busy="true"><div class="skeleton"></div></div>

  <div v-else-if="!data">
    <PageHeader title="方案不存在" :crumbs="[{ label: '首页', path: '/' }, { label: '解决方案', path: '/solutions' }]" />
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
      :crumbs="[{ label: '首页', path: '/' }, { label: '解决方案', path: '/solutions' }, { label: data.industry }]"
    />

    <section class="section">
      <div class="container layout">
        <article>
          <figure v-if="data.cover" class="cover">
            <img :src="data.cover.url" :alt="data.cover.alt" :width="data.cover.width" :height="data.cover.height" />
          </figure>

          <div class="block">
            <h2 class="block__title">面临的挑战</h2>
            <p class="block__text">{{ data.challenge }}</p>
          </div>

          <div class="block">
            <h2 class="block__title">做法</h2>
            <ol class="steps">
              <li v-for="(step, index) in data.approach" :key="step" class="steps__item">
                <span class="steps__num" aria-hidden="true">{{ index + 1 }}</span>
                <span>{{ step }}</span>
              </li>
            </ol>
          </div>

          <div class="block">
            <h2 class="block__title">结果</h2>
            <dl class="outcome">
              <div v-for="item in data.outcome" :key="item.label" class="outcome__item">
                <dt>{{ item.label }}</dt>
                <dd>{{ item.value }}</dd>
              </div>
            </dl>
          </div>

          <section v-if="data.relatedProducts?.length" class="related" aria-labelledby="related-products">
            <h2 id="related-products" class="block__title">用到的产品</h2>
            <ul class="related__list">
              <li v-for="item in data.relatedProducts ?? []" :key="item.id">
                <RouterLink :to="`/products/${item.id}`" class="related__link">
                  <span>{{ item.title }}</span>
                  <span class="related__desc">{{ item.summary }}</span>
                </RouterLink>
              </li>
            </ul>
          </section>
        </article>

        <aside class="aside">
          <div class="aside__card">
            <p class="aside__title">你的场景和这个类似？</p>
            <p class="aside__desc">带上设备类型与现有系统情况，我们会判断是否适用，而不是先卖方案。</p>
            <RouterLink to="/contact" class="btn btn--primary aside__btn">聊聊你的场景</RouterLink>
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
  margin-bottom: 36px;
}

.cover img {
  width: 100%;
  border-radius: var(--radius);
  border: 1px solid var(--c-border);
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
  align-items: flex-start;
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

.outcome {
  display: flex;
  flex-wrap: wrap;
  gap: 40px;
  margin: 0;
}

.outcome__item dt {
  font-size: 13px;
  color: var(--c-text-muted);
}

.outcome__item dd {
  margin: 4px 0 0;
  font-size: 26px;
  font-weight: 500;
  color: var(--c-brand);
}

.related {
  margin-top: 48px;
  padding-top: 28px;
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
  flex-direction: column;
  gap: 4px;
  padding: 16px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
}

.related__link:hover {
  border-color: var(--c-brand);
}

.related__desc {
  font-size: 13px;
  color: var(--c-text-muted);
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
  .outcome {
    gap: 28px;
  }
}
</style>
