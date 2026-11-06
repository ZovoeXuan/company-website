<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getNews } from '@/api/public'
import { usePageMeta } from '@/composables/usePageMeta'
import { vReveal } from '@/directives/reveal'
import type { NewsSummary } from '@/data/types'

usePageMeta(() => ({
  title: '新闻中心',
  description: '了解企业最新动态、行业前沿洞察、最佳实践、品牌活动与大数据报告。',
}))

const activeCategory = ref('全部新闻')
const currentPage = ref(1)
const showMoreDropdown = ref(false)
const remoteNews = ref<NewsSummary[]>([])

const MAIN_CATEGORIES = [
  '全部新闻',
  '企业新闻',
  '行业洞察',
  '最佳实践',
  '品牌活动',
  '大数据',
  '社会责任',
]

const EXTRA_TAGS = ['产品发布', '新能源重卡', 'AI安全', '冷链物流', '智慧园区']

interface EnrichedNews {
  id: string
  title: string
  summary: string
  displayTime: string
  category: string
  coverUrl?: string
  themeIdx: number
}

const DEFAULT_NEWS_POOL: EnrichedNews[] = [
  {
    id: '1',
    title: '让你的物流AI开工！2026数字物流大会精彩日程抢先看',
    summary:
      'G7易流数字物流大会，是一年一度属于物流人的思想聚会。每年，我们都把行业里最敢想、最敢干的一群人聚在一起，聊聊技术、算算细账，看看物流的未来到底长什么样。',
    displayTime: '2026/10/16',
    category: '品牌活动',
    themeIdx: 0,
  },
  {
    id: '2',
    title: '不加人、不靠老司机，冷链“最后几分钟”的解法都在这',
    summary:
      '聚焦商超与餐饮冷链城配交付痛点，通过 AI 温控预警与自动签收留痕，让新司机也能跑出准点零货损标杆。',
    displayTime: '2026/09/20',
    category: '最佳实践',
    themeIdx: 1,
  },
  {
    id: '3',
    title: '上了好几百台车，利润却越来越薄？新能源重卡竞争已从“选车”转向“经营车队”',
    summary:
      '深入剖析新能源重卡规模化运营后的电耗管理、充换电排班与资产利用率瓶颈，解锁数据驱动的精细化运营方法论。',
    displayTime: '2026/09/09',
    category: '行业洞察',
    themeIdx: 2,
  },
  {
    id: '4',
    title: '2026上半年冷链物流行业报告发布，释放哪些关键信号？',
    summary:
      '基于平台海量冷链运单与温控感知数据，全面解读 2026 上半年生鲜医药冷链运力流向、运价走势与安全合规新趋势。',
    displayTime: '2026/08/26',
    category: '行业洞察',
    themeIdx: 3,
  },
  {
    id: '5',
    title: '新能源车队经营管理共创会｜TOP级新能源车队的「运营经」都在这',
    summary:
      '汇聚全国大宗与港口干线头部电动重卡车队管理者，现场拆解百公里电耗下降 12% 的实战经验。',
    displayTime: '2026/08/19',
    category: '品牌活动',
    themeIdx: 4,
  },
  {
    id: '6',
    title: '2026第六届猪产业发展大会｜养殖效率卷到极致，下一轮利润可能藏在运输途中',
    summary:
      '针对农牧饲料与活体运输全产业链，通过 AI 防偷盗治理与洗消中心数字化监管筑牢生物安全防线。',
    displayTime: '2026/08/19',
    category: '企业新闻',
    themeIdx: 5,
  },
  {
    id: '7',
    title: '现场有真相 03｜「不怎么开车」的城配司机，会被AI替代么？',
    summary:
      '一线跟车实录：当装卸、核单、找月台占据城配司机一半时间，AI 工具如何帮他们省下每一分钟。',
    displayTime: '2026/08/19',
    category: '最佳实践',
    themeIdx: 6,
  },
  {
    id: '8',
    title: '冷链运输温度达标后，交付真正的战场在哪里？',
    summary:
      '从“不断链”走向“精益交付”，拆解头部连锁餐饮供应链如何借助 IoT 数据打通门店无人化夜配。',
    displayTime: '2026/08/26',
    category: '大数据',
    themeIdx: 7,
  },
  {
    id: '9',
    title: '后厨之外，校园团餐在途食品安全如何保障？',
    summary:
      '依托电子铅封、AI 货舱摄像头与多温区实时监测，实现食材从中央厨房到校园食堂的全程阳光可视。',
    displayTime: '2026/08/19',
    category: '社会责任',
    themeIdx: 8,
  },
]

const allNews = computed<EnrichedNews[]>(() => {
  const mapped: EnrichedNews[] = remoteNews.value.map((n, idx) => ({
    id: String(n.id),
    title: n.title,
    summary: n.summary || '点击阅读完整行业分析与最新动态报道。',
    displayTime: (n.displayTime || '2026/09/20').replace(/-/g, '/').slice(0, 10),
    category: n.categoryName || MAIN_CATEGORIES[(idx % 6) + 1],
    coverUrl: n.cover?.url,
    themeIdx: idx % 9,
  }))
  const merged = [...mapped]
  for (const defItem of DEFAULT_NEWS_POOL) {
    if (!merged.some((m) => m.title === defItem.title)) {
      merged.push(defItem)
    }
  }
  return merged
})

const filteredNews = computed(() => {
  if (activeCategory.value === '全部新闻') return allNews.value
  const matched = allNews.value.filter(
    (n) =>
      n.category.includes(activeCategory.value) ||
      n.title.includes(activeCategory.value),
  )
  return matched.length > 0 ? matched : allNews.value
})

const featuredItem = computed(() => filteredNews.value[0] ?? DEFAULT_NEWS_POOL[0])

const gridNews = computed(() => {
  const rest = filteredNews.value.slice(1)
  const pool = rest.length >= 8 ? rest : [...rest, ...allNews.value]
  const start = ((currentPage.value - 1) * 8) % Math.max(1, pool.length)
  const out: EnrichedNews[] = []
  for (let i = 0; i < 8; i++) {
    out.push(pool[(start + i) % pool.length])
  }
  return out
})

function selectCat(cat: string) {
  activeCategory.value = cat
  currentPage.value = 1
  showMoreDropdown.value = false
}

onMounted(async () => {
  try {
    const res = await getNews({ page: 1, pageSize: 20 })
    if (res?.items?.length) {
      remoteNews.value = res.items
    }
  } catch {
    // 保持默认新闻池
  }
})
</script>

<template>
  <div class="g7-news-page">
    <!-- 1. 顶部数字地球网络粒子 Banner（1:1 还原 新闻中心.mp4 00:00-00:05） -->
    <section class="news-hero">
      <div class="news-hero__globe" aria-hidden="true"></div>
      <div class="container news-hero__inner">
        <h1 v-reveal class="news-hero__title">新闻中心</h1>
      </div>
    </section>

    <!-- 2. 跨压 Banner 下沿的白底分类条（7 个主分类 + 右侧 更多标签 v 下拉框） -->
    <div class="container news-tabs-wrap">
      <div v-reveal="80" class="news-tabs-bar">
        <nav class="news-tabs" aria-label="新闻分类筛选">
          <button
            v-for="cat in MAIN_CATEGORIES"
            :key="cat"
            type="button"
            class="news-tabs__btn"
            :class="{ 'is-active': activeCategory === cat }"
            @click="selectCat(cat)"
          >
            {{ cat }}
          </button>
        </nav>

        <div class="news-more-wrap">
          <button
            type="button"
            class="news-more-btn"
            @click="showMoreDropdown = !showMoreDropdown"
          >
            <span>更多标签</span>
            <span class="news-more-caret" :class="{ 'is-open': showMoreDropdown }"></span>
          </button>
          <ul v-if="showMoreDropdown" class="news-more-menu">
            <li v-for="tag in EXTRA_TAGS" :key="tag">
              <button type="button" @click="selectCat(tag)">{{ tag }}</button>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 3. 置顶大图头条卡片（左文 #f5f7fa + 右图 50/50 分栏，1:1 还原 news_04.png & news_34.png） -->
    <section class="news-featured-sec">
      <div class="container">
        <article v-reveal="120" class="news-featured-card">
          <div class="news-featured-card__left">
            <div class="news-meta-time">
              <span aria-hidden="true">🕒</span>
              <time>{{ featuredItem.displayTime }}</time>
            </div>
            <h2 class="news-featured-card__title">{{ featuredItem.title }}</h2>
            <p class="news-featured-card__summary">{{ featuredItem.summary }}</p>
            <div>
              <RouterLink :to="`/news/${featuredItem.id}`" class="news-outline-btn">
                了解更多
              </RouterLink>
            </div>
          </div>

          <RouterLink :to="`/news/${featuredItem.id}`" class="news-featured-card__right">
            <img
              v-if="featuredItem.coverUrl"
              :src="featuredItem.coverUrl"
              :alt="featuredItem.title"
              class="news-featured-card__img"
            />
            <div v-else class="news-featured-poster" :class="`np-theme--${featuredItem.themeIdx % 4}`">
              <div class="news-featured-poster__badge">G7易流 · {{ featuredItem.category }}</div>
              <strong class="news-featured-poster__headline">{{ featuredItem.title.slice(0, 22) }}</strong>
            </div>
          </RouterLink>
        </article>
      </div>
    </section>

    <!-- 4. 下方 4×2 新闻卡片网格 + 圆形分页器（1:1 还原 news_14.png & news_20.png） -->
    <section class="news-grid-sec">
      <div class="container">
        <div class="news-grid-4x2">
          <RouterLink
            v-for="(item, idx) in gridNews"
            :key="`${item.id}-${idx}`"
            v-reveal="(idx % 4) * 70"
            :to="`/news/${item.id}`"
            class="g7-news-card"
          >
            <div class="g7-news-card__media" :class="`np-theme--${item.themeIdx % 4}`">
              <img
                v-if="item.coverUrl"
                :src="item.coverUrl"
                :alt="item.title"
                class="g7-news-card__img"
                loading="lazy"
              />
              <div v-else class="g7-news-card__fallback">
                <span>{{ item.category }}</span>
                <strong>{{ item.title.slice(0, 16) }}</strong>
              </div>
            </div>

            <div class="g7-news-card__body">
              <h3 class="g7-news-card__title">{{ item.title }}</h3>
              <div class="g7-news-card__foot">
                <span class="news-meta-time">
                  <span aria-hidden="true">🕒</span>
                  <time>{{ item.displayTime }}</time>
                </span>
                <span class="g7-news-card__tag-icon" aria-hidden="true">🏷️</span>
              </div>
            </div>
          </RouterLink>
        </div>

        <!-- 底部圆形分页器：< 1 2 3 4 5 ... 15 > -->
        <nav class="g7-pagination" aria-label="新闻列表分页">
          <button
            type="button"
            class="g7-page-arrow"
            :disabled="currentPage === 1"
            @click="currentPage = Math.max(1, currentPage - 1)"
          >
            ‹
          </button>
          <button
            v-for="p in [1, 2, 3, 4, 5]"
            :key="p"
            type="button"
            class="g7-page-num"
            :class="{ 'is-active': currentPage === p }"
            @click="currentPage = p"
          >
            {{ p }}
          </button>
          <span class="g7-page-ellipsis">...</span>
          <button
            type="button"
            class="g7-page-num"
            :class="{ 'is-active': currentPage === 15 }"
            @click="currentPage = 15"
          >
            15
          </button>
          <button
            type="button"
            class="g7-page-arrow"
            :disabled="currentPage === 15"
            @click="currentPage = Math.min(15, currentPage + 1)"
          >
            ›
          </button>
        </nav>
      </div>
    </section>
  </div>
</template>

<style scoped>
.g7-news-page {
  background: #ffffff;
  color: #1f2329;
}

/* 1. 顶部地球网络粒子 Banner */
.news-hero {
  position: relative;
  height: 350px;
  background: radial-gradient(ellipse at 50% 100%, #172554 0%, #090d16 70%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.news-hero__globe {
  position: absolute;
  width: 680px;
  height: 680px;
  bottom: -420px;
  left: 50%;
  transform: translateX(-50%);
  border-radius: 50%;
  border: 1px dashed rgba(96, 165, 250, 0.35);
  box-shadow:
    0 0 80px rgba(59, 130, 246, 0.35),
    inset 0 0 60px rgba(56, 189, 248, 0.25);
  background-image: radial-gradient(rgba(147, 197, 253, 0.45) 1.5px, transparent 1.5px);
  background-size: 18px 18px;
}

.news-hero__inner {
  position: relative;
  z-index: 2;
  text-align: center;
  padding-bottom: 24px;
}

.news-hero__title {
  font-size: clamp(32px, 3.8vw, 42px);
  font-weight: 700;
  color: #ffffff;
  margin: 0;
  letter-spacing: 0.04em;
}

/* 2. 悬浮白底分类栏 */
.news-tabs-wrap {
  position: relative;
  z-index: 10;
  margin-top: -41px;
}

.news-tabs-bar {
  max-width: 1080px;
  height: 82px;
  margin: 0 auto;
  background: #ffffff;
  box-shadow: 0 12px 36px rgba(16, 24, 40, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  gap: 24px;
}

.news-tabs {
  display: flex;
  align-items: center;
  gap: 38px;
  height: 100%;
}

.news-tabs__btn {
  position: relative;
  height: 100%;
  border: none;
  background: none;
  padding: 0 2px;
  font-size: 14.5px;
  color: #2b2f38;
  font-family: inherit;
  cursor: pointer;
  transition: color 0.2s ease;
}

.news-tabs__btn:hover,
.news-tabs__btn.is-active {
  color: #5b4be8;
  font-weight: 600;
}

.news-tabs__btn.is-active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 16px;
  height: 3px;
  background: #5b4be8;
  border-radius: 2px;
}

.news-more-wrap {
  position: relative;
}

.news-more-btn {
  height: 34px;
  padding: 0 16px;
  border: 1px solid #d0d5dd;
  background: #ffffff;
  color: #5f6470;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.news-more-caret {
  width: 6px;
  height: 6px;
  border-right: 1.5px solid currentColor;
  border-bottom: 1.5px solid currentColor;
  transform: rotate(45deg) translateY(-2px);
  transition: transform 0.2s ease;
}

.news-more-caret.is-open {
  transform: rotate(225deg) translateY(-2px);
}

.news-more-menu {
  position: absolute;
  right: 0;
  top: calc(100% + 6px);
  width: 140px;
  background: #ffffff;
  box-shadow: 0 12px 28px rgba(16, 24, 40, 0.12);
  border: 1px solid #eceef2;
  list-style: none;
  margin: 0;
  padding: 6px 0;
  z-index: 20;
}

.news-more-menu button {
  width: 100%;
  padding: 8px 16px;
  border: none;
  background: none;
  text-align: left;
  font-size: 13px;
  color: #2b2f38;
  cursor: pointer;
}

.news-more-menu button:hover {
  background: #f5f7fb;
  color: #5b4be8;
}

/* 3. 置顶 50/50 头条大图卡 */
.news-featured-sec {
  padding: 44px 0 36px;
}

.news-featured-card {
  max-width: 1080px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: 336px;
  background: #f5f7fa;
  overflow: hidden;
}

.news-featured-card__left {
  padding: 44px 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.news-meta-time {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
  color: #98a2b3;
}

.news-featured-card__title {
  margin: 14px 0 16px;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.45;
  color: #1f2329;
}

.news-featured-card__summary {
  margin: 0 0 28px;
  font-size: 13.5px;
  line-height: 1.85;
  color: #6e7380;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-outline-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 38px;
  padding: 0 30px;
  border: 1px solid #1f2329;
  color: #1f2329;
  font-size: 13px;
  text-decoration: none;
  transition: all 0.2s ease;
}

.news-outline-btn:hover {
  background: #1f2329;
  color: #ffffff;
}

.news-featured-card__right {
  position: relative;
  overflow: hidden;
  text-decoration: none;
}

.news-featured-card__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.news-featured-poster {
  width: 100%;
  height: 100%;
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: #ffffff;
}

.np-theme--0 {
  background: linear-gradient(135deg, #311042 0%, #4c1d95 52%, #0f172a 100%);
}

.np-theme--1 {
  background: linear-gradient(135deg, #1e293b 0%, #334155 55%, #0f172a 100%);
}

.np-theme--2 {
  background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 55%, #0f172a 100%);
}

.np-theme--3 {
  background: linear-gradient(135deg, #0c4a6e 0%, #0284c7 55%, #082f49 100%);
}

.news-featured-poster__badge {
  font-size: 13px;
  font-weight: 700;
  opacity: 0.85;
}

.news-featured-poster__headline {
  font-size: 28px;
  line-height: 1.35;
}

/* 4. 4×2 新闻小卡片网格 */
.news-grid-sec {
  padding: 20px 0 96px;
  background: #f8f9fc;
}

.news-grid-4x2 {
  max-width: 1080px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.g7-news-card {
  background: #ffffff;
  display: flex;
  flex-direction: column;
  text-decoration: none;
  color: inherit;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.g7-news-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 16px 36px rgba(16, 24, 40, 0.08);
}

.g7-news-card__media {
  aspect-ratio: 16 / 10;
  overflow: hidden;
  position: relative;
}

.g7-news-card__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.g7-news-card__fallback {
  width: 100%;
  height: 100%;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: #ffffff;
}

.g7-news-card__fallback span {
  font-size: 11.5px;
  opacity: 0.8;
}

.g7-news-card__fallback strong {
  font-size: 16px;
  line-height: 1.4;
}

.g7-news-card__body {
  padding: 20px 18px 18px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 136px;
}

.g7-news-card__title {
  font-size: 14px;
  font-weight: 500;
  line-height: 1.65;
  color: #2b2f38;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.g7-news-card__foot {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.g7-news-card__tag-icon {
  font-size: 11px;
  opacity: 0.35;
}

.g7-pagination {
  margin-top: 52px;
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
}

.g7-page-num:hover:not(.is-active) {
  background: #7a7f8a;
  color: #ffffff;
}

.g7-page-num.is-active {
  background: #0a0b0e;
  color: #ffffff;
}

.g7-page-ellipsis {
  color: #98a2b3;
  font-size: 13px;
}

@media (max-width: 980px) {
  .news-featured-card {
    grid-template-columns: 1fr;
  }
  .news-grid-4x2 {
    grid-template-columns: repeat(2, 1fr);
  }
  .news-tabs-bar {
    padding: 0 16px;
    overflow-x: auto;
  }
}
</style>
