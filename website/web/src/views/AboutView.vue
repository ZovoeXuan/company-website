<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ApiError } from '@/api/http'
import { getAbout } from '@/api/public'
import { openConsult } from '@/composables/consult'
import { usePageMeta } from '@/composables/usePageMeta'
import { vReveal } from '@/directives/reveal'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import type { AboutPage } from '@/data/types'

const data = ref<AboutPage | null>(null)
const loading = ref(true)
const error = ref('')
const activeYear = ref<string>('ALL')
const qualFilter = ref('')

usePageMeta(() => ({
  title: '关于我们',
  description: data.value?.intro?.slice(0, 120) || '了解企业发展历程、技术实力与行业资质认证。',
}))

const milestoneYears = computed(() => {
  const list = data.value?.milestones ?? []
  const years = Array.from(new Set(list.map((m) => m.year).filter(Boolean)))
  return ['ALL', ...years]
})

const filteredMilestones = computed(() => {
  const list = data.value?.milestones ?? []
  if (activeYear.value === 'ALL') return list
  return list.filter((m) => m.year === activeYear.value)
})

const filteredQualifications = computed(() => {
  const list = data.value?.qualifications ?? []
  const kw = qualFilter.value.trim().toLowerCase()
  if (!kw) return list
  return list.filter(
    (q) => q.name.toLowerCase().includes(kw) || (q.description ?? '').toLowerCase().includes(kw),
  )
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    data.value = await getAbout()
  } catch (err) {
    data.value = null
    error.value =
      err instanceof ApiError && err.code === 'NOT_FOUND'
        ? '「关于我们」页面尚未发布，请管理员在后台发布后再访问。'
        : err instanceof ApiError
        ? err.message
        : '页面加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="about-page">
    <PageHeader
      kicker="ABOUT US · 关于我们"
      title="以智能物联网与 AI 重塑产业生产力"
      subtitle="连接物理世界与数字智能，持续为行业领军客户创造可衡量的安全与经营价值。"
    />

    <div v-if="loading" class="container state">
      <div class="skeleton"></div>
    </div>

    <div v-else-if="error" class="container state">
      <EmptyState title="内容暂不可用" :description="error">
        <button type="button" class="btn btn--primary" @click="load">重试</button>
      </EmptyState>
    </div>

    <template v-else-if="data">
      <!-- 1. 公司简介与企业使命 Bento 双卡 -->
      <section v-if="data.intro || data.mission" v-reveal class="section">
        <div class="container">
          <div class="bento-grid">
            <div v-if="data.intro" class="bento-card bento-card--intro">
              <span class="kicker">COMPANY PROFILE · 企业简介</span>
              <h2 class="bento-card__title">深耕产业数字化与智能连接</h2>
              <p class="bento-card__text">{{ data.intro }}</p>
            </div>
            <div v-if="data.mission" class="bento-card bento-card--mission">
              <span class="eyebrow">MISSION & VISION · 使命与愿景</span>
              <h2 class="bento-card__title">用数字技术驱动每一份安全与效率</h2>
              <p class="bento-card__text">{{ data.mission }}</p>
              <div class="bento-card__cta">
                <button
                  type="button"
                  class="btn btn--outline-light"
                  @click="openConsult({ sourcePosition: 'OTHER_CTA', title: '了解企业合作与生态伙伴计划' })"
                >
                  洽谈战略合作 →
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 2. 交互式企业发展历程时间轴 (Milestone Timeline) -->
      <section v-if="data.milestones?.length" v-reveal class="section section--surface">
        <div class="container">
          <div class="section-head">
            <div>
              <span class="kicker">MILESTONES · 发展历程</span>
              <h2 class="section-title">一路向前的关键足迹</h2>
            </div>

            <div v-if="milestoneYears.length > 2" class="year-pills">
              <button
                v-for="yr in milestoneYears"
                :key="yr"
                type="button"
                class="year-pill"
                :class="{ 'is-active': activeYear === yr }"
                @click="activeYear = yr"
              >
                {{ yr === 'ALL' ? '全部年份' : `${yr} 年` }}
              </button>
            </div>
          </div>

          <div class="timeline">
            <div
              v-for="(item, idx) in filteredMilestones"
              :key="`${item.year}-${idx}`"
              class="timeline__node"
            >
              <div class="timeline__year-col">
                <span class="timeline__year">{{ item.year }}</span>
                <span class="timeline__dot" aria-hidden="true"></span>
              </div>
              <div class="timeline__card">
                <h3 class="timeline__title">{{ item.title }}</h3>
                <p v-if="item.description" class="timeline__desc">{{ item.description }}</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 3. 权威资质与技术认证画廊 (Qualifications) -->
      <section v-if="data.qualifications?.length" v-reveal class="section">
        <div class="container">
          <div class="section-head">
            <div>
              <span class="kicker">QUALIFICATIONS & HONORS · 资质与荣誉</span>
              <h2 class="section-title">国家级权威认证与技术合规保障</h2>
            </div>
            <input
              v-if="data.qualifications.length > 4"
              v-model="qualFilter"
              type="search"
              class="qual-search"
              placeholder="搜索资质或认证名称..."
            />
          </div>

          <div class="grid grid--3">
            <div v-for="(q, idx) in filteredQualifications" :key="`${q.name}-${idx}`" class="qual-card">
              <div class="qual-card__icon" aria-hidden="true">🏅</div>
              <div>
                <h3 class="qual-card__title">{{ q.name }}</h3>
                <p v-if="q.description" class="qual-card__desc">{{ q.description }}</p>
              </div>
            </div>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.bento-grid {
  display: grid;
  grid-template-columns: 1.25fr 1fr;
  gap: 28px;
}

@media (max-width: 860px) {
  .bento-grid {
    grid-template-columns: 1fr;
  }
}

.bento-card {
  border-radius: var(--radius-lg);
  padding: 40px 36px;
  display: flex;
  flex-direction: column;
}

.bento-card--intro {
  background: var(--c-surface);
  border: 1px solid var(--c-border);
}

.bento-card--mission {
  background: linear-gradient(155deg, #0b0d16 0%, #191736 60%, #2d2168 100%);
  color: #ffffff;
}

.bento-card__title {
  font-size: 26px;
  margin-bottom: 16px;
}

.bento-card--mission .bento-card__title {
  color: #ffffff;
}

.bento-card__text {
  font-size: 16px;
  line-height: 1.9;
  white-space: pre-line;
  color: var(--c-text-muted);
}

.bento-card--mission .bento-card__text {
  color: rgba(255, 255, 255, 0.8);
}

.bento-card__cta {
  margin-top: auto;
  padding-top: 24px;
}

/* 时间轴 */
.year-pills {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.year-pill {
  padding: 6px 14px;
  border-radius: 999px;
  border: 1px solid var(--c-border-strong);
  background: #ffffff;
  font-size: 13px;
  color: var(--c-text-muted);
  cursor: pointer;
}

.year-pill.is-active {
  background: var(--c-brand);
  border-color: var(--c-brand);
  color: #ffffff;
}

.timeline {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.timeline__node {
  display: grid;
  grid-template-columns: 140px 1fr;
  gap: 24px;
  align-items: stretch;
}

@media (max-width: 640px) {
  .timeline__node {
    grid-template-columns: 1fr;
    gap: 8px;
  }
}

.timeline__year-col {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  position: relative;
}

.timeline__year {
  font-size: 24px;
  font-weight: 800;
  color: var(--c-brand);
  letter-spacing: -0.02em;
}

.timeline__dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--c-brand);
  box-shadow: 0 0 0 4px #eeebff;
}

.timeline__card {
  background: #ffffff;
  border: 1px solid var(--c-border);
  border-radius: 16px;
  padding: 22px 26px;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.timeline__card:hover {
  transform: translateX(4px);
  border-color: rgba(91, 75, 232, 0.35);
  box-shadow: 0 12px 32px rgba(10, 12, 20, 0.06);
}

.timeline__title {
  font-size: 18px;
  font-weight: 600;
}

.timeline__desc {
  margin-top: 8px;
  font-size: 14.5px;
  color: var(--c-text-muted);
  line-height: 1.75;
}

/* 资质卡片 */
.qual-search {
  height: 38px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid var(--c-border-strong);
  font-size: 13.5px;
  outline: none;
}

.qual-search:focus {
  border-color: var(--c-brand);
}

.qual-card {
  background: #ffffff;
  border: 1px solid var(--c-border);
  border-radius: 16px;
  padding: 22px 24px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  transition: transform 0.2s ease, border-color 0.2s ease;
}

.qual-card:hover {
  transform: translateY(-3px);
  border-color: var(--c-brand);
}

.qual-card__icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: #eeebff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex: none;
}

.qual-card__title {
  font-size: 16px;
  font-weight: 600;
}

.qual-card__desc {
  margin-top: 6px;
  font-size: 13.5px;
  color: var(--c-text-muted);
  line-height: 1.65;
}
</style>
