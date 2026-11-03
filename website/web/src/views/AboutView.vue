<script setup lang="ts">
import { onMounted, ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getAbout } from '@/api/public'
import { usePageMeta } from '@/composables/usePageMeta'
import type { AboutPage } from '@/data/types'

const data = ref<AboutPage | null>(null)
const failed = ref(false)

usePageMeta(() => ({
  title: '关于我们',
  description: '了解公司的业务方向、发展历程与资质情况。',
}))

onMounted(async () => {
  try {
    data.value = await getAbout()
  } catch {
    failed.value = true
  }
})
</script>

<template>
  <div>
    <PageHeader
      title="关于我们"
      description="一家把工业现场数据变得可解释的公司。"
      :crumbs="[{ label: '首页', path: '/' }, { label: '关于我们' }]"
    />

    <section v-if="data" class="section">
      <div class="container">
        <div class="intro">
          <p class="intro__text">{{ data.intro }}</p>
        </div>

        <div class="mission">
          <h2 class="mission__title">我们为什么做这件事</h2>
          <p class="mission__text">{{ data.mission }}</p>
        </div>

        <section class="block" aria-labelledby="timeline-title">
          <h2 id="timeline-title" class="block__title">发展历程</h2>
          <ol class="timeline">
            <li v-for="item in data.milestones" :key="item.year" class="timeline__item">
              <span class="timeline__year">{{ item.year }}</span>
              <div class="timeline__body">
                <h3 class="timeline__name">{{ item.title }}</h3>
                <p class="timeline__desc">{{ item.description }}</p>
              </div>
            </li>
          </ol>
        </section>

        <section class="block" aria-labelledby="qualification-title">
          <h2 id="qualification-title" class="block__title">资质与认证</h2>
          <ul class="quals">
            <li v-for="item in data.qualifications" :key="item.name" class="quals__item">
              <p class="quals__name">{{ item.name }}</p>
              <p class="quals__desc">{{ item.description }}</p>
            </li>
          </ul>
        </section>
      </div>
    </section>

    <div v-else-if="failed" class="container state">
      <EmptyState title="内容加载失败" description="请稍后刷新页面重试。" />
    </div>
  </div>
</template>

<style scoped>
.intro__text {
  font-size: 18px;
  line-height: 1.9;
  color: #2b3138;
  max-width: 860px;
}

/* 使命段落：用左侧竖线强调，不用灰底卡片 */
.mission {
  margin-top: 56px;
  padding: 4px 0 4px 28px;
  border-left: 2px solid var(--c-brand);
}

.mission__title {
  font-size: 14px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  font-weight: 500;
  color: var(--c-text-subtle);
  margin-bottom: 14px;
}

.mission__text {
  font-size: 20px;
  line-height: 1.8;
  color: var(--c-text);
  max-width: 860px;
}

.block {
  margin-top: 64px;
}

.block__title {
  font-size: 22px;
  font-weight: 500;
  margin-bottom: 24px;
}

.timeline {
  list-style: none;
  margin: 0;
  padding: 0;
  border-left: 1px solid var(--c-border);
}

.timeline__item {
  position: relative;
  display: grid;
  grid-template-columns: 88px minmax(0, 1fr);
  gap: 20px;
  padding: 0 0 32px 24px;
}

.timeline__item::before {
  content: '';
  position: absolute;
  left: -4.5px;
  top: 8px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--c-brand);
}

.timeline__year {
  font-size: 18px;
  font-weight: 500;
  color: var(--c-brand);
  font-variant-numeric: tabular-nums;
}

.timeline__name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 6px;
}

.timeline__desc {
  font-size: 14px;
  color: var(--c-text-muted);
  line-height: 1.8;
}

.quals {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
  list-style: none;
  margin: 0;
  padding: 0;
}

.quals__item {
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  padding: 24px;
}

.quals__name {
  font-size: 16px;
  font-weight: 500;
}

.quals__desc {
  margin-top: 8px;
  font-size: 14px;
  color: var(--c-text-muted);
  line-height: 1.8;
}

.state {
  padding: 72px 24px;
}

@media (max-width: 860px) {
  .quals {
    grid-template-columns: 1fr;
  }
  .timeline__item {
    grid-template-columns: 64px minmax(0, 1fr);
    gap: 12px;
  }
  .mission {
    padding: 24px;
  }
}
</style>
