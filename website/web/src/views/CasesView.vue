<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getAbout, getSite } from '@/api/public'
import { openVideo } from '@/composables/videoModal'
import { usePageMeta } from '@/composables/usePageMeta'
import { vReveal } from '@/directives/reveal'
import type { AboutStoryItem, CaseSummary, PageResult } from '@/data/types'

usePageMeta(() => ({
  title: '客户案例',
  description: '携手大宗、食品生鲜医药、快消零售、大物流与危化行业领军企业，共建数字化生产力。',
}))

const siteName = ref('G7易流')
const activeCategory = ref('全部案例')
const currentPage = ref(1)
const remoteCases = ref<CaseSummary[]>([])
const customStories = ref<AboutStoryItem[]>([])

const CATEGORIES = ['全部案例', '大宗', '食品/生鲜/医药', '快消/零售', '大物流', '危化']

interface EnrichedCaseItem {
  id: string
  title: string
  category: string
  partnerBrand: string
  partnerSub?: string
  coverBg: string
  coverUrl?: string
}

const DEFAULT_CASES: EnrichedCaseItem[] = [
  {
    id: '1',
    title: '中铝物流集团 × G7易流：给每辆货车装上智慧大脑，筑牢大宗运输安全防线',
    category: '大宗',
    partnerBrand: '中铝物流',
    partnerSub: 'CHINALCO LOGISTICS',
    coverBg: 'linear-gradient(135deg, #1e293b 0%, #334155 55%, #0f172a 100%)',
  },
  {
    id: '2',
    title: '想念食品：用数字化打通从麦田到餐桌的每一公里安全与时效',
    category: '食品/生鲜/医药',
    partnerBrand: '想念食品',
    partnerSub: 'XIANGNIAN FOOD',
    coverBg: 'linear-gradient(135deg, #3f2e21 0%, #78350f 55%, #1c1917 100%)',
  },
  {
    id: '3',
    title: '津西钢铁 × G7易流：构建厂外物流全流程无人化、透明化协同平台',
    category: '大宗',
    partnerBrand: '津西钢铁',
    partnerSub: 'JINXI IRON & STEEL',
    coverBg: 'linear-gradient(135deg, #1e3a8a 0%, #1d4ed8 55%, #0f172a 100%)',
  },
  {
    id: '4',
    title: '百年凤祥 × G7易流：AI 全程冷链温控守护食品安全“鲜”标准',
    category: '食品/生鲜/医药',
    partnerBrand: '凤祥食品',
    partnerSub: 'FENGXIANG FOOD',
    coverBg: 'linear-gradient(135deg, #064e3b 0%, #047857 55%, #022c22 100%)',
  },
  {
    id: '5',
    title: '华鼎如何一步步找到冷链配送最后两米的答案',
    category: '食品/生鲜/医药',
    partnerBrand: '华鼎冷链科技',
    partnerSub: 'HUADING COLD CHAIN',
    coverBg: 'linear-gradient(135deg, #1e293b 0%, #0f172a 60%, #334155 100%)',
  },
  {
    id: '6',
    title: '物流运输运价内卷，华港集团如何靠安全管理守住生存空间',
    category: '大宗',
    partnerBrand: '华港国际',
    partnerSub: 'HGI LOGISTICS',
    coverBg: 'linear-gradient(135deg, #27272a 0%, #3f3f46 55%, #18181b 100%)',
  },
  {
    id: '7',
    title: '新疆晟祥物流 × G7易流：AI + 数字化赋能新能源重卡车队管理扩张',
    category: '大宗',
    partnerBrand: '晟祥物流',
    partnerSub: 'SHENGXIANG',
    coverBg: 'linear-gradient(135deg, #0c4a6e 0%, #0284c7 55%, #082f49 100%)',
  },
  {
    id: '8',
    title: '年运量700万吨，双汇物流用AI实现「聪明运营」',
    category: '大物流',
    partnerBrand: '双汇物流',
    partnerSub: 'SHUANGHUI LOGISTICS',
    coverBg: 'linear-gradient(135deg, #7f1d1d 0%, #991b1b 55%, #450a0a 100%)',
  },
  {
    id: '9',
    title: '连锁商超城配数字化标杆：实现全国 3000+ 门店准点率提升至 99.2%',
    category: '快消/零售',
    partnerBrand: '头部商超供应链',
    partnerSub: 'RETAIL SUPPLY CHAIN',
    coverBg: 'linear-gradient(135deg, #312e81 0%, #4338ca 55%, #1e1b4b 100%)',
  },
  {
    id: '10',
    title: '危化品运输主动安全与电子运单全闭环：高危风险事件下降 72%',
    category: '危化',
    partnerBrand: '万华化学物流生态',
    partnerSub: 'HAZMAT SAFETY',
    coverBg: 'linear-gradient(135deg, #134e4a 0%, #0f766e 55%, #042f2e 100%)',
  },
]

const allCases = computed<EnrichedCaseItem[]>(() => {
  const mappedRemote: EnrichedCaseItem[] = remoteCases.value.map((c, idx) => ({
    id: String(c.id),
    title: c.title,
    category: c.industry || c.categoryName || '大宗',
    partnerBrand: c.customerName || c.title.split(/[×：:]/)[0].slice(0, 8) || '标杆客户',
    coverUrl: c.cover?.url,
    coverBg: DEFAULT_CASES[idx % DEFAULT_CASES.length].coverBg,
  }))
  const merged = [...mappedRemote]
  for (const defItem of DEFAULT_CASES) {
    if (!merged.some((m) => m.title === defItem.title)) {
      merged.push(defItem)
    }
  }
  return merged
})

const filteredCases = computed(() => {
  if (activeCategory.value === '全部案例') return allCases.value
  return allCases.value.filter(
    (c) =>
      c.category.includes(activeCategory.value) ||
      activeCategory.value.includes(c.category),
  )
})

const pageSize = 8
const totalPages = computed(() => Math.max(6, Math.ceil(filteredCases.value.length / pageSize)))

const pagedCases = computed(() => {
  const list = filteredCases.value
  if (list.length === 0) return []
  const start = ((currentPage.value - 1) * pageSize) % list.length
  const out: EnrichedCaseItem[] = []
  for (let i = 0; i < Math.min(pageSize, list.length); i++) {
    out.push(list[(start + i) % list.length])
  }
  return out
})

function selectCategory(cat: string) {
  activeCategory.value = cat
  currentPage.value = 1
}

function goPage(p: number) {
  if (p < 1 || p > totalPages.value) return
  currentPage.value = p
}

// 底部 2×2 客户成功故事视频证言卡（1:1 还原 case_24.png / case_32.png）
const DEFAULT_STORIES: AboutStoryItem[] = [
  {
    quote: '“与G7易流的合作，对公司各项经营指标都有显著提升。”',
    author: '张太喜  双汇物流董事长',
    company: '双汇物流',
    logoText: 'SHUANGHUI 双汇物流',
    cover: '',
    videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
  },
  {
    quote: '“统一多元的数据，加上G7易流精准的算法，帮助天佑完善了管理闭环。”',
    author: '何镭  天佑物流标准/执行总监',
    company: '天佑物流',
    logoText: 'tianyou天佑',
    cover: '',
    videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
  },
  {
    quote: '“「数字化」之后，一个往返我们就知道利润了。”',
    author: '赵春波  滨拓物流总经理',
    company: '滨拓物流',
    logoText: '滨拓物流',
    cover: '',
    videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
  },
  {
    quote: '通过IoT技术重构煤炭运输环节，让干线运输与装卸解耦，助力包头市煤炭物流绿色低碳转型。',
    author: '华电能源包头公司',
    company: '中国华电 CHD',
    logoText: '中国华电 CHD',
    cover: '',
    videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
  },
]

const storiesList = computed<AboutStoryItem[]>(() => {
  if (customStories.value.length > 0) return customStories.value
  return DEFAULT_STORIES
})

function playStoryVideo(story: AboutStoryItem) {
  openVideo({
    title: `${story.company} · 客户成功故事`,
    url:
      story.videoUrl ||
      'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
  })
}

onMounted(async () => {
  try {
    const [siteRes, aboutRes, casesResp] = await Promise.all([
      getSite().catch(() => null),
      getAbout().catch(() => null),
      fetch('/api/public/cases?page=1&pageSize=24')
        .then((r) => r.json())
        .catch(() => null),
    ])
    if (siteRes?.siteName) siteName.value = siteRes.siteName
    if (aboutRes?.customerStories?.length) {
      customStories.value = aboutRes.customerStories
    }
    const pageData = casesResp?.data as PageResult<CaseSummary> | undefined
    if (pageData?.items?.length) {
      remoteCases.value = pageData.items
    }
  } catch {
    // 保持默认标杆数据
  }
})
</script>

<template>
  <div class="g7-cases-page">
    <!-- 1. 顶部深空立交建筑透视 Banner（1:1 对齐 客户案例.mp4 00:00-00:04） -->
    <section class="cases-hero">
      <div class="cases-hero__grid-bg" aria-hidden="true"></div>
      <div class="container cases-hero__inner">
        <h1 v-reveal class="cases-hero__title">客户案例</h1>
      </div>
    </section>

    <!-- 2. 跨压 Banner 下沿的悬浮白底行业分类条 -->
    <div class="container cases-tabs-wrap">
      <nav v-reveal="80" class="cases-tabs" aria-label="案例行业分类">
        <button
          v-for="cat in CATEGORIES"
          :key="cat"
          type="button"
          class="cases-tabs__btn"
          :class="{ 'is-active': activeCategory === cat }"
          @click="selectCategory(cat)"
        >
          {{ cat }}
        </button>
      </nav>
    </div>

    <!-- 3. 浅灰蓝底色 4×2 案例卡片网格 + 黑色圆圈分页器（1:1 对齐 case_05.png & case_14.png） -->
    <section class="cases-grid-sec">
      <div class="container">
        <div class="cases-grid">
          <RouterLink
            v-for="(item, idx) in pagedCases"
            :key="`${item.id}-${idx}`"
            v-reveal="(idx % 4) * 70"
            :to="`/cases/${item.id}`"
            class="g7-case-card"
          >
            <!-- 顶部 16:10 封面 + 居中白色联名水印 Logo（客户Logo × G7易流） -->
            <div class="g7-case-card__cover" :style="{ background: item.coverBg }">
              <img
                v-if="item.coverUrl"
                :src="item.coverUrl"
                :alt="item.title"
                class="g7-case-card__img"
                loading="lazy"
              />
              <div class="g7-case-card__mask"></div>
              <div class="g7-case-card__cobrand">
                <span class="cobrand__partner">{{ item.partnerBrand }}</span>
                <span class="cobrand__x">×</span>
                <span class="cobrand__self"><em>G7</em>{{ siteName.replace(/^G7/i, '') || '易流' }}</span>
              </div>
            </div>

            <!-- 中部标题 + 底部左侧胶囊与右侧了解更多 > -->
            <div class="g7-case-card__body">
              <h3 class="g7-case-card__title">{{ item.title }}</h3>
              <div class="g7-case-card__foot">
                <span class="g7-case-card__tag">{{ item.category }}</span>
                <span class="g7-case-card__more">了解更多 &gt;</span>
              </div>
            </div>
          </RouterLink>
        </div>

        <!-- 居中圆形分页器：< 1 2 3 4 5 6 > -->
        <nav class="g7-pagination" aria-label="案例列表分页">
          <button
            type="button"
            class="g7-page-arrow"
            :disabled="currentPage === 1"
            aria-label="上一页"
            @click="goPage(currentPage - 1)"
          >
            ‹
          </button>
          <button
            v-for="p in totalPages"
            :key="p"
            type="button"
            class="g7-page-num"
            :class="{ 'is-active': currentPage === p }"
            @click="goPage(p)"
          >
            {{ p }}
          </button>
          <button
            type="button"
            class="g7-page-arrow"
            :disabled="currentPage === totalPages"
            aria-label="下一页"
            @click="goPage(currentPage + 1)"
          >
            ›
          </button>
        </nav>
      </div>
    </section>

    <!-- 4. 客户成功故事 2×2 视频证言卡片（1:1 还原 case_24.png / case_32.png） -->
    <section class="stories-sec">
      <div class="container">
        <h2 v-reveal class="stories-sec__title">客户成功故事</h2>

        <div class="stories-grid">
          <article
            v-for="(st, sIdx) in storiesList"
            :key="st.company + sIdx"
            v-reveal="sIdx * 80"
            class="story-card"
          >
            <!-- 左侧人物/现场剧照 + 左下角白色方形播放按钮 ▶ -->
            <div
              class="story-card__media"
              :class="`story-theme--${sIdx % 4}`"
              @click="playStoryVideo(st)"
            >
              <img v-if="st.cover" :src="st.cover" :alt="st.author" class="story-card__img" />
              <div v-else class="story-card__scene">
                <div class="story-card__avatar-silhouette" :class="`silhouette--${sIdx % 4}`"></div>
              </div>
              <button
                type="button"
                class="story-card__play"
                aria-label="播放客户证言视频"
                @click.stop="playStoryVideo(st)"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                  <polygon points="6 4 20 12 6 20 6 4"></polygon>
                </svg>
              </button>
            </div>

            <!-- 右侧引言 + 姓名职务 + 底部客户品牌 Logo -->
            <div class="story-card__content">
              <div>
                <p class="story-card__quote">{{ st.quote }}</p>
                <p class="story-card__author">{{ st.author }}</p>
              </div>
              <div class="story-card__logo">
                <span class="story-logo-badge" :class="`logo-style--${sIdx % 4}`">
                  {{ st.logoText || st.company }}
                </span>
              </div>
            </div>
          </article>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.g7-cases-page {
  background: #f4f6fa;
  color: #1f2329;
}

/* 1. 顶部 Banner */
.cases-hero {
  position: relative;
  height: 350px;
  background: linear-gradient(135deg, #0d1322 0%, #1b2845 52%, #0f172a 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.cases-hero__grid-bg {
  position: absolute;
  inset: 0;
  background:
    repeating-linear-gradient(
      90deg,
      rgba(255, 255, 255, 0.03) 0px,
      rgba(255, 255, 255, 0.03) 1px,
      transparent 1px,
      transparent 64px
    ),
    radial-gradient(circle at 70% 30%, rgba(56, 189, 248, 0.18) 0%, transparent 55%);
}

.cases-hero__inner {
  position: relative;
  z-index: 2;
  text-align: center;
  padding-bottom: 24px;
}

.cases-hero__title {
  font-size: clamp(32px, 3.8vw, 42px);
  font-weight: 700;
  color: #ffffff;
  margin: 0;
  letter-spacing: 0.04em;
}

/* 2. 悬浮分类导航条 */
.cases-tabs-wrap {
  position: relative;
  z-index: 10;
  margin-top: -41px;
}

.cases-tabs {
  max-width: 1080px;
  height: 82px;
  margin: 0 auto;
  background: #ffffff;
  box-shadow: 0 10px 30px rgba(16, 24, 40, 0.05);
  display: flex;
  align-items: center;
  padding: 0 48px;
  gap: 52px;
}

.cases-tabs__btn {
  position: relative;
  height: 100%;
  border: none;
  background: none;
  padding: 0 4px;
  font-size: 14.5px;
  color: #2b2f38;
  font-family: inherit;
  cursor: pointer;
  transition: color 0.2s ease;
}

.cases-tabs__btn:hover,
.cases-tabs__btn.is-active {
  color: #5b4be8;
  font-weight: 600;
}

.cases-tabs__btn.is-active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 16px;
  height: 3px;
  background: #5b4be8;
  border-radius: 2px;
}

/* 3. 4×2 案例卡片网格 */
.cases-grid-sec {
  padding: 48px 0 72px;
}

.cases-grid {
  max-width: 1080px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.g7-case-card {
  background: #ffffff;
  display: flex;
  flex-direction: column;
  text-decoration: none;
  color: inherit;
  transition: transform 0.28s ease, box-shadow 0.28s ease;
}

.g7-case-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 18px 40px rgba(16, 24, 40, 0.09);
}

.g7-case-card__cover {
  position: relative;
  aspect-ratio: 16 / 10;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.g7-case-card__img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.g7-case-card:hover .g7-case-card__img {
  transform: scale(1.05);
}

.g7-case-card__mask {
  position: absolute;
  inset: 0;
  background: rgba(10, 14, 24, 0.34);
}

.g7-case-card__cobrand {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #ffffff;
  padding: 0 12px;
  text-align: center;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.45);
}

.cobrand__partner {
  font-size: 16px;
  font-weight: 800;
}

.cobrand__x {
  font-size: 13px;
  opacity: 0.85;
}

.cobrand__self {
  font-size: 16px;
  font-weight: 800;
}

.cobrand__self em {
  font-style: italic;
  font-weight: 900;
  margin-right: 1px;
}

.g7-case-card__body {
  padding: 22px 20px 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 156px;
}

.g7-case-card__title {
  font-size: 14.5px;
  font-weight: 500;
  line-height: 1.65;
  color: #2b2f38;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.g7-case-card__foot {
  margin-top: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.g7-case-card__tag {
  padding: 3px 12px;
  border-radius: 999px;
  background: #eff1f5;
  color: #8b90a0;
  font-size: 11.5px;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.g7-case-card__more {
  font-size: 12px;
  color: #8b90a0;
  transition: color 0.2s ease;
}

/* 1:1 还原 case_05.png：卡片悬停时左下角标签变深灰底白字、了解更多变紫色 */
.g7-case-card:hover .g7-case-card__tag {
  background: #5f6470;
  color: #ffffff;
}

.g7-case-card:hover .g7-case-card__more {
  color: #5b4be8;
}

/* 圆形分页器 */
.g7-pagination {
  margin-top: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.g7-page-arrow,
.g7-page-num {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: transparent;
  color: #2b2f38;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.18s ease, color 0.18s ease;
}

.g7-page-num:hover:not(.is-active) {
  background: #7a7f8a;
  color: #ffffff;
}

.g7-page-num.is-active {
  background: #0a0b0e;
  color: #ffffff;
}

.g7-page-arrow:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

/* 4. 客户成功故事 2×2 区域 */
.stories-sec {
  background: #ffffff;
  padding: 84px 0 110px;
}

.stories-sec__title {
  max-width: 1080px;
  margin: 0 auto 40px;
  font-size: 34px;
  font-weight: 700;
  color: #1f2329;
}

.stories-grid {
  max-width: 1080px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 28px;
}

.story-card {
  background: #ffffff;
  box-shadow: 0 12px 36px rgba(16, 24, 40, 0.06);
  display: grid;
  grid-template-columns: 48% 52%;
  min-height: 236px;
  overflow: hidden;
}

.story-card__media {
  position: relative;
  cursor: pointer;
  overflow: hidden;
}

.story-theme--0 {
  background: linear-gradient(145deg, #991b1b 0%, #450a0a 100%);
}

.story-theme--1 {
  background: linear-gradient(145deg, #78350f 0%, #451a03 100%);
}

.story-theme--2 {
  background: linear-gradient(145deg, #334155 0%, #0f172a 100%);
}

.story-theme--3 {
  background: linear-gradient(145deg, #0284c7 0%, #1e3a8a 100%);
}

.story-card__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.story-card__scene {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.story-card__avatar-silhouette {
  width: 120px;
  height: 150px;
  border-radius: 60px 60px 0 0;
  background: rgba(255, 255, 255, 0.16);
}

.story-card__play {
  position: absolute;
  left: 12px;
  bottom: 12px;
  width: 44px;
  height: 44px;
  border: none;
  background: #ffffff;
  color: #0a0b0e;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.22);
  transition: transform 0.2s ease, color 0.2s ease;
}

.story-card__media:hover .story-card__play {
  transform: scale(1.06);
  color: #5b4be8;
}

.story-card__content {
  padding: 28px 28px 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.story-card__quote {
  font-size: 14px;
  line-height: 1.78;
  color: #2b2f38;
  margin: 0 0 14px;
}

.story-card__author {
  font-size: 12.5px;
  color: #98a2b3;
  margin: 0;
}

.story-logo-badge {
  display: inline-block;
  font-size: 13px;
  font-weight: 800;
  color: #dc2626;
}

.logo-style--1 {
  color: #0284c7;
}

.logo-style--2 {
  color: #16a34a;
}

.logo-style--3 {
  color: #1d4ed8;
}

@media (max-width: 980px) {
  .cases-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .stories-grid {
    grid-template-columns: 1fr;
  }
  .cases-tabs {
    padding: 0 20px;
    gap: 24px;
    overflow-x: auto;
  }
}
</style>
