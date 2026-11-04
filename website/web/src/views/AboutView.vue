<script setup lang="ts">
import { onMounted, ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getAbout } from '@/api/public'
import { usePageMeta } from '@/composables/usePageMeta'
import type { AboutPage } from '@/data/types'

const data = ref<AboutPage | null>(null)
const failed = ref(false)
const activeTab = ref<'intro' | 'team' | 'media' | 'library' | 'join'>('intro')
const libraryCategory = ref('industry')

usePageMeta(() => ({
  title: '关于我们',
  description: '了解公司的业务方向、管理团队、资料库与发展历程。',
}))

const teamMembers = [
  {
    name: '翟学魂',
    role: 'G7易流首席执行官',
    avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80',
    desc: '深耕物流数字化与产业互联网二十余年，带领团队开创物联网在货运行业的规模化应用。',
  },
  {
    name: '张景涛',
    role: 'G7易流总裁',
    avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=80',
    desc: '拥有丰富的大宗物流与供应链数字化实战经验，主导多项行业领先方案落地。',
  },
  {
    name: '郝淑霞',
    role: 'G7易流高级副总裁',
    avatar: 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=400&q=80',
    desc: '负责企业组织运营与战略协同，持续推动技术产品在全行业的深度赋能。',
  },
  {
    name: '张杰龙',
    role: 'G7易流首席财务官',
    avatar: 'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&w=400&q=80',
    desc: '资深财务与资本市场专家，主持公司战略投资、资本运作与精细化经营闭环。',
  },
]

const libraryItems = [
  {
    title: '新能源车队管理白皮书',
    category: 'industry',
    tag: '行业报告',
    summary: '迈向数据驱动的「成本、效率、安全」的精细化运营新时代。',
    cover: 'https://images.unsplash.com/photo-1519003722824-194d4455a60c?auto=format&fit=crop&w=500&q=80',
  },
  {
    title: '基于大数据的中国公路货运行业运行分析报告',
    category: 'industry',
    tag: '行业报告',
    summary: '权威量化公路物流大盘周转率、在途时效与空驶里程关键指标。',
    cover: 'https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=500&q=80',
  },
  {
    title: '中国公路货运市场研究报告',
    category: 'industry',
    tag: '行业报告',
    summary: '洞察运力供需平衡、油价波动应对与新能源重卡替代演进趋势。',
    cover: 'https://images.unsplash.com/photo-1601584115197-04ecc0da31d7?auto=format&fit=crop&w=500&q=80',
  },
  {
    title: '中国公路货运行业智慧安全白皮书',
    category: 'industry',
    tag: '行业报告',
    summary: '以软硬一体与车联网主动安全算法，将重特大事故率下降 80% 以上。',
    cover: 'https://images.unsplash.com/photo-1508974239320-0a029497e820?auto=format&fit=crop&w=500&q=80',
  },
]

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
      title="以物联网与AI连接每一个生产要素"
      description="让客户的先进落地，让先进的客户成功。"
      eyebrow="ABOUT US"
      tone="light"
      centered
      :crumbs="[{ label: '首页', path: '/' }, { label: '关于我们' }]"
    />

    <!-- 对齐视频 5 大分类选项卡（企业简介、管理团队、媒体中心、资料共享、加入我们） -->
    <div class="about-tab-bar">
      <div class="container">
        <nav class="about-tabs" role="tablist" aria-label="关于我们导航">
          <button
            type="button"
            class="about-tab"
            :class="{ 'is-active': activeTab === 'intro' }"
            :aria-selected="activeTab === 'intro'"
            role="tab"
            @click="activeTab = 'intro'"
          >
            企业简介
          </button>
          <button
            type="button"
            class="about-tab"
            :class="{ 'is-active': activeTab === 'team' }"
            :aria-selected="activeTab === 'team'"
            role="tab"
            @click="activeTab = 'team'"
          >
            管理团队
          </button>
          <button
            type="button"
            class="about-tab"
            :class="{ 'is-active': activeTab === 'library' }"
            :aria-selected="activeTab === 'library'"
            role="tab"
            @click="activeTab = 'library'"
          >
            资料共享
          </button>
          <button
            type="button"
            class="about-tab"
            :class="{ 'is-active': activeTab === 'media' }"
            :aria-selected="activeTab === 'media'"
            role="tab"
            @click="activeTab = 'media'"
          >
            媒体中心
          </button>
          <button
            type="button"
            class="about-tab"
            :class="{ 'is-active': activeTab === 'join' }"
            :aria-selected="activeTab === 'join'"
            role="tab"
            @click="activeTab = 'join'"
          >
            加入我们
          </button>
        </nav>
      </div>
    </div>

    <!-- 1. 企业简介 Tab -->
    <section v-if="activeTab === 'intro'" class="section tab-content">
      <div v-if="data" class="container">
        <div class="intro-grid">
          <div v-reveal>
            <p class="kicker">WHO WE ARE</p>
            <h2 class="intro__title">把现场的复杂度，<br />变成界面上的一目了然。</h2>
          </div>
          <div v-reveal="120" class="intro__body">
            <p class="intro__text">{{ data.intro }}</p>
          </div>
        </div>

        <div class="about-visual" v-reveal>
          <div class="about-visual__inner">
            <p class="about-visual__label">SINCE</p>
            <p class="about-visual__year">2010</p>
            <p class="about-visual__line"></p>
            <p class="about-visual__note">从设备接入，到全场景AI，再到产业物流数字化基础设施。</p>
          </div>
        </div>

        <div class="mission-box" v-reveal>
          <p class="kicker kicker--white">OUR BELIEF</p>
          <h2 class="mission__title">我们为什么做这件事</h2>
          <p class="mission__text">{{ data.mission }}</p>
        </div>

        <!-- 发展历程 -->
        <div id="timeline" class="section-block">
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

        <!-- 资质认证 -->
        <div id="qualifications" class="section-block">
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
      </div>
    </section>

    <!-- 2. 管理团队 Tab (对齐视频 frame 172s) -->
    <section v-else-if="activeTab === 'team'" class="section tab-content">
      <div class="container">
        <div class="section-head section-head--center">
          <h2 class="section-title">管理团队</h2>
          <p class="section-sub">具有丰富技术研发、供应链管理与产业经营经验的领军团队</p>
        </div>

        <div class="team-grid">
          <article v-for="(member, index) in teamMembers" :key="member.name" v-reveal="index * 70" class="team-card">
            <div class="team-card__photo">
              <img :src="member.avatar" :alt="member.name" loading="lazy" />
            </div>
            <div class="team-card__body">
              <h3 class="team-card__name">{{ member.name }}</h3>
              <p class="team-card__role">{{ member.role }}</p>
              <p class="team-card__desc">{{ member.desc }}</p>
            </div>
          </article>
        </div>
      </div>
    </section>

    <!-- 3. 资料共享 Tab (对齐视频 frame 185s) -->
    <section v-else-if="activeTab === 'library'" class="section tab-content">
      <div class="container">
        <div class="section-head">
          <h2 class="section-title">资料库</h2>
          <p class="section-sub">行业白皮书、运行分析报告与产业研究成果</p>
        </div>

        <!-- 子分类过滤 -->
        <div class="sub-filter-bar">
          <button
            type="button"
            class="sub-filter-btn"
            :class="{ 'is-active': libraryCategory === 'corp' }"
            @click="libraryCategory = 'corp'"
          >
            企业资料
          </button>
          <button
            type="button"
            class="sub-filter-btn"
            :class="{ 'is-active': libraryCategory === 'video' }"
            @click="libraryCategory = 'video'"
          >
            客户视频
          </button>
          <button
            type="button"
            class="sub-filter-btn"
            :class="{ 'is-active': libraryCategory === 'industry' }"
            @click="libraryCategory = 'industry'"
          >
            行业报告
          </button>
          <button
            type="button"
            class="sub-filter-btn"
            :class="{ 'is-active': libraryCategory === 'esg' }"
            @click="libraryCategory = 'esg'"
          >
            ESG
          </button>
        </div>

        <!-- 4 列资料卡片 -->
        <div class="library-grid">
          <div v-for="(doc, index) in libraryItems" :key="doc.title" v-reveal="index * 60" class="library-card">
            <div class="library-card__media">
              <img :src="doc.cover" :alt="doc.title" loading="lazy" />
              <span class="library-card__tag">{{ doc.tag }}</span>
            </div>
            <div class="library-card__body">
              <h3 class="library-card__title">{{ doc.title }}</h3>
              <p class="library-card__desc">{{ doc.summary }}</p>
              <a href="#" class="library-card__download" @click.prevent>
                <span>免费下载</span>
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                  <polyline points="7 10 12 15 17 10"></polyline>
                  <line x1="12" y1="15" x2="12" y2="3"></line>
                </svg>
              </a>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 4. 媒体中心 Tab -->
    <section v-else-if="activeTab === 'media'" class="section tab-content">
      <div class="container">
        <div class="section-head">
          <h2 class="section-title">媒体中心</h2>
          <p class="section-sub">品牌物料、VI规范与媒体合作联络渠道</p>
        </div>
        <div class="media-box-grid">
          <div class="media-box" v-reveal>
            <h3>媒体联系与数据合作</h3>
            <p>如需采访预约、行业数据报告引用授权或联合活动合作，欢迎与我们沟通。</p>
            <p class="media-box__email">联系邮箱：<span>connect@g7.com.cn</span></p>
          </div>
          <div class="media-box" v-reveal="80">
            <h3>品牌资产下载</h3>
            <p>提供官方标准色 Logo、品牌使用指南与标准印刷矢量文件下载包。</p>
            <a href="#" class="btn btn--outline-dark" @click.prevent>下载品牌视觉包 (ZIP)</a>
          </div>
        </div>
      </div>
    </section>

    <!-- 5. 加入我们 Tab -->
    <section v-else-if="activeTab === 'join'" class="section tab-content">
      <div class="container">
        <div class="section-head section-head--center">
          <h2 class="section-title">加入我们</h2>
          <p class="section-sub">与优秀的同行者一起，用科技改变万亿级公路物流行业</p>
        </div>
        <div class="join-highlights">
          <div class="join-card" v-reveal>
            <div class="join-card__icon">🚀</div>
            <h3>技术引领</h3>
            <p>亿级高并发实时计算、千万级硬件边缘计算与产业AI大模型真实落地。</p>
          </div>
          <div class="join-card" v-reveal="80">
            <div class="join-card__icon">🌱</div>
            <h3>快速成长</h3>
            <p>导师辅导机制、扁平技术文化与明确的专业/管理双通道成长路径。</p>
          </div>
          <div class="join-card" v-reveal="160">
            <div class="join-card__icon">💼</div>
            <h3>全面福利</h3>
            <p>高标准六险一金、带薪年假、弹性工时、定期体检与暖心节日礼包。</p>
          </div>
        </div>
      </div>
    </section>

    <div v-if="!data && failed" class="container state">
      <EmptyState title="内容加载失败" description="请稍后刷新页面重试。" />
    </div>
  </div>
</template>

<style scoped>
.about-tab-bar {
  background: #ffffff;
  border-bottom: 1px solid var(--c-border);
  position: sticky;
  top: var(--header-height, 72px);
  z-index: 20;
}

.about-tabs {
  display: flex;
  gap: 36px;
  overflow-x: auto;
}

.about-tab {
  padding: 16px 4px;
  background: transparent;
  border: none;
  font-family: inherit;
  font-size: 15px;
  font-weight: 500;
  color: var(--c-text-muted);
  cursor: pointer;
  position: relative;
  transition: color 0.2s ease;
  white-space: nowrap;
}

.about-tab:hover {
  color: var(--c-text);
}

.about-tab.is-active {
  color: var(--c-brand, #5b4be8);
  font-weight: 600;
}

.about-tab.is-active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 3px;
  background: var(--c-brand, #5b4be8);
  border-radius: 3px 3px 0 0;
}

.tab-content {
  padding-top: 50px;
}

/* 企业简介区块 */
.intro-grid {
  display: grid;
  grid-template-columns: 1fr 1.1fr;
  gap: 72px;
  align-items: start;
}

.intro__title {
  font-size: clamp(30px, 3.6vw, 46px);
  line-height: 1.25;
  letter-spacing: -0.03em;
  margin: 12px 0 0;
}

.intro__text {
  font-size: 17px;
  line-height: 1.8;
  color: var(--c-text-muted);
}

.about-visual {
  margin-top: 64px;
  padding: 60px 48px;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, rgba(91, 75, 232, 0.08), rgba(91, 75, 232, 0.02)), #ffffff;
  border: 1px solid var(--c-border);
}

.about-visual__label {
  font-size: 13px;
  letter-spacing: 0.24em;
  color: var(--c-brand);
  font-weight: 600;
}

.about-visual__year {
  font-size: 80px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: -0.04em;
  color: var(--c-text);
  margin: 8px 0;
}

.about-visual__line {
  width: 60px;
  height: 3px;
  background: var(--c-brand);
  margin: 18px 0;
}

.about-visual__note {
  font-size: 16px;
  color: var(--c-text-muted);
}

.mission-box {
  margin-top: 48px;
  padding: 56px 48px;
  border-radius: var(--radius-lg);
  background: #0f1118;
  color: #fff;
}

.mission__title {
  font-size: 28px;
  margin: 8px 0 16px;
  color: #fff;
}

.mission__text {
  font-size: 17px;
  line-height: 1.75;
  color: rgba(255, 255, 255, 0.8);
  max-width: 800px;
}

.section-block {
  margin-top: 80px;
}

.timeline {
  list-style: none;
  margin: 36px 0 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32px;
}

@media (max-width: 900px) {
  .timeline {
    grid-template-columns: repeat(2, 1fr);
  }
}

.timeline__item {
  position: relative;
  padding-top: 24px;
  border-top: 2px solid var(--c-border);
}

.timeline__year {
  font-size: 24px;
  font-weight: 700;
  color: var(--c-brand);
}

.timeline__name {
  font-size: 16px;
  margin: 10px 0 6px;
}

.timeline__desc {
  font-size: 13px;
  color: var(--c-text-muted);
  line-height: 1.6;
}

.quals {
  list-style: none;
  margin: 36px 0 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

@media (max-width: 768px) {
  .quals {
    grid-template-columns: 1fr;
  }
}

.quals__item {
  padding: 28px;
  background: #ffffff;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
}

.quals__index {
  font-size: 14px;
  font-weight: 700;
  color: var(--c-brand);
}

.quals__name {
  font-size: 17px;
  font-weight: 600;
  margin: 12px 0 6px;
}

.quals__desc {
  font-size: 13px;
  color: var(--c-text-muted);
}

/* 管理团队 */
.section-head--center {
  text-align: center;
  margin-bottom: 48px;
}

.team-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 28px;
}

@media (max-width: 960px) {
  .team-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.team-card {
  background: #ffffff;
  border: 1px solid var(--c-border);
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(10, 12, 20, 0.03);
  transition: transform 0.3s var(--ease-out), box-shadow 0.3s var(--ease-out);
}

.team-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 16px 36px rgba(10, 12, 20, 0.08);
}

.team-card__photo {
  aspect-ratio: 4 / 4.5;
  background: #181a24;
}

.team-card__photo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.team-card__body {
  padding: 24px 20px;
}

.team-card__name {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--c-text);
}

.team-card__role {
  margin: 6px 0 14px;
  font-size: 13px;
  color: var(--c-brand, #5b4be8);
  font-weight: 500;
}

.team-card__desc {
  margin: 0;
  font-size: 13px;
  line-height: 1.65;
  color: var(--c-text-muted);
}

/* 资料库 */
.sub-filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 36px;
}

.sub-filter-btn {
  padding: 8px 20px;
  border-radius: 999px;
  border: 1px solid var(--c-border);
  background: #ffffff;
  font-family: inherit;
  font-size: 14px;
  color: var(--c-text-muted);
  cursor: pointer;
  transition: all 0.2s ease;
}

.sub-filter-btn.is-active {
  background: var(--c-brand, #5b4be8);
  border-color: var(--c-brand, #5b4be8);
  color: #fff;
}

.library-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 24px;
}

@media (max-width: 960px) {
  .library-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.library-card {
  background: #ffffff;
  border: 1px solid var(--c-border);
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.library-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 14px 32px rgba(10, 12, 20, 0.08);
}

.library-card__media {
  position: relative;
  aspect-ratio: 16 / 10;
  background: #222;
}

.library-card__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.library-card__tag {
  position: absolute;
  top: 10px;
  left: 10px;
  background: rgba(0, 0, 0, 0.65);
  backdrop-filter: blur(4px);
  color: #fff;
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 4px;
}

.library-card__body {
  padding: 20px 18px;
  display: flex;
  flex-direction: column;
  flex: 1;
  justify-content: space-between;
}

.library-card__title {
  margin: 0 0 10px;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.45;
  color: var(--c-text);
}

.library-card__desc {
  margin: 0 0 18px;
  font-size: 13px;
  color: var(--c-text-muted);
  line-height: 1.6;
}

.library-card__download {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--c-brand, #5b4be8);
}

/* 媒体与招聘 */
.media-box-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
}

.media-box {
  padding: 36px;
  background: #ffffff;
  border: 1px solid var(--c-border);
  border-radius: 14px;
}

.media-box__email span {
  font-weight: 600;
  color: var(--c-brand);
}

.join-highlights {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 28px;
}

.join-card {
  padding: 36px 28px;
  background: #ffffff;
  border: 1px solid var(--c-border);
  border-radius: 14px;
  text-align: center;
}

.join-card__icon {
  font-size: 36px;
  margin-bottom: 16px;
}
</style>
