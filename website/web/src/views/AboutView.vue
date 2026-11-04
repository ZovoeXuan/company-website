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
      title="以数据，重新理解工业现场"
      description="一家把设备数据变成可解释、可行动结论的公司。"
      eyebrow="ABOUT US"
      tone="light"
      centered
      :crumbs="[{ label: '首页', path: '/' }, { label: '关于我们' }]"
    />

    <div v-if="data" class="container about-nav">
      <a href="#intro">公司简介</a>
      <a href="#timeline">发展历程</a>
      <a href="#qualifications">资质与认证</a>
    </div>

    <section v-if="data" id="intro" class="section about-intro">
      <div class="container intro-grid">
        <div v-reveal>
          <p class="kicker">WHO WE ARE</p>
          <h2 class="intro__title">把现场的复杂度，<br />变成界面上的一目了然。</h2>
        </div>
        <div v-reveal="120" class="intro__body">
          <p class="intro__text">{{ data.intro }}</p>
        </div>
      </div>

      <div class="container about-visual" v-reveal>
        <div class="about-visual__inner">
          <p class="about-visual__label">SINCE</p>
          <p class="about-visual__year">2010</p>
          <p class="about-visual__line"></p>
          <p class="about-visual__note">从设备接入，到预测性维护，再到能源与碳管理。</p>
        </div>
      </div>
    </section>

    <section v-if="data" class="section section--dark mission-section">
      <div class="container">
        <p class="kicker">OUR BELIEF</p>
        <h2 class="mission__title">我们为什么做这件事</h2>
        <p class="mission__text">{{ data.mission }}</p>
      </div>
    </section>

    <section v-if="data" id="timeline" class="section timeline-section">
      <div class="container">
        <div class="section-head">
          <div>
            <p class="kicker">MILESTONES</p>
            <h2 class="section-title">发展历程</h2>
          </div>
        </div>
        <ol class="timeline">
          <li v-for="(item, index) in data.milestones" :key="item.year" v-reveal="index * 70" class="timeline__item">
            <span class="timeline__year">{{ item.year }}</span>
            <h3 class="timeline__name">{{ item.title }}</h3>
            <p class="timeline__desc">{{ item.description }}</p>
          </li>
        </ol>
      </div>
    </section>

    <section v-if="data" id="qualifications" class="section section--surface qual-section">
      <div class="container">
        <div class="section-head">
          <div>
            <p class="kicker">CERTIFICATIONS</p>
            <h2 class="section-title">资质与认证</h2>
          </div>
        </div>
        <ul class="quals">
          <li v-for="(item, index) in data.qualifications" :key="item.name" v-reveal="index * 60" class="quals__item">
            <span class="quals__index">{{ String(index + 1).padStart(2, '0') }}</span>
            <p class="quals__name">{{ item.name }}</p>
            <p class="quals__desc">{{ item.description }}</p>
          </li>
        </ul>
      </div>
    </section>

    <div v-if="!data && failed" class="container state">
      <EmptyState title="内容加载失败" description="请稍后刷新页面重试。" />
    </div>
  </div>
</template>

<style scoped>
.about-nav {
  position: relative;
  z-index: 2;
  display: flex;
  gap: 8px;
  margin-top: -28px;
  padding: 10px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 18px 44px rgba(10, 12, 20, 0.08);
  width: fit-content;
}

.about-nav a {
  padding: 10px 18px;
  border-radius: 8px;
  font-size: 13px;
  color: var(--c-text-muted);
  transition: background-color 0.2s ease, color 0.2s ease;
}

.about-nav a:hover {
  background: var(--c-brand-soft);
  color: var(--c-brand);
}

.intro-grid {
  display: grid;
  grid-template-columns: 1fr 1.1fr;
  gap: 72px;
  align-items: start;
}

.intro__title {
  font-size: clamp(30px, 3.6vw, 46px);
  line-height: 1.25;
  letter-spacing: -0.035em;
}

.intro__text {
  font-size: 17px;
  line-height: 1.95;
  color: var(--c-text-muted);
}

.about-visual {
  margin-top: 72px;
}

.about-visual__inner {
  position: relative;
  overflow: hidden;
  border-radius: var(--radius-lg);
  background: radial-gradient(circle at 72% 28%, rgba(91, 75, 232, 0.5), transparent 55%),
    linear-gradient(120deg, #0b0b12, #16161f 55%, #0b0b12);
  padding: 64px;
  color: #fff;
  min-height: 320px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.about-visual__inner::after {
  content: '';
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(255, 255, 255, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.06) 1px, transparent 1px);
  background-size: 72px 72px;
  mask-image: radial-gradient(circle at 70% 30%, #000, transparent 70%);
}

.about-visual__label {
  font-size: 11px;
  letter-spacing: 0.2em;
  color: rgba(255, 255, 255, 0.45);
}

.about-visual__year {
  font-size: clamp(64px, 12vw, 152px);
  line-height: 0.95;
  font-weight: 600;
  letter-spacing: -0.06em;
  margin-top: 12px;
}

.about-visual__line {
  width: 80px;
  height: 2px;
  background: #fff;
  margin: 24px 0 18px;
}

.about-visual__note {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  max-width: 420px;
  line-height: 1.8;
}

.mission-section {
  padding: 128px 0;
}

.mission__title {
  font-size: clamp(26px, 3vw, 38px);
  letter-spacing: -0.03em;
  color: #fff;
  margin-bottom: 28px;
}

.mission__text {
  font-size: clamp(22px, 2.6vw, 34px);
  line-height: 1.6;
  letter-spacing: -0.02em;
  color: rgba(255, 255, 255, 0.72);
  max-width: 980px;
}

.timeline {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1px;
  background: var(--c-border);
  border-radius: var(--radius);
  overflow: hidden;
}

.timeline__item {
  background: #fff;
  padding: 36px 32px;
  transition: background-color 0.3s ease;
}

.timeline__item:hover {
  background: var(--c-mist);
}

.timeline__year {
  font-size: 13px;
  letter-spacing: 0.12em;
  color: var(--c-brand);
  font-variant-numeric: tabular-nums;
}

.timeline__name {
  margin-top: 14px;
  font-size: 22px;
  letter-spacing: -0.02em;
}

.timeline__desc {
  margin-top: 10px;
  font-size: 14px;
  color: var(--c-text-muted);
  line-height: 1.8;
}

.quals {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

.quals__item {
  background: #fff;
  border-radius: var(--radius);
  padding: 30px 26px;
  transition: transform 0.35s var(--ease-out);
}

.quals__item:hover {
  transform: translateY(-4px);
}

.quals__index {
  font-size: 12px;
  letter-spacing: 0.12em;
  color: var(--c-brand);
}

.quals__name {
  margin-top: 16px;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: -0.02em;
}

.quals__desc {
  margin-top: 8px;
  font-size: 13px;
  color: var(--c-text-muted);
  line-height: 1.8;
}

@media (max-width: 900px) {
  .intro-grid {
    grid-template-columns: 1fr;
    gap: 28px;
  }
  .about-visual {
    margin-top: 40px;
  }
  .about-visual__inner {
    padding: 36px 28px;
    min-height: 240px;
  }
  .timeline {
    grid-template-columns: 1fr;
  }
  .quals {
    grid-template-columns: 1fr;
  }
  .mission-section {
    padding: 80px 0;
  }
}
</style>
