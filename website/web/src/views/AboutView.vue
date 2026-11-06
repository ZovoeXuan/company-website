<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { getAbout, getNews, getSite } from '@/api/public'
import { openConsult } from '@/composables/consult'
import { usePageMeta } from '@/composables/usePageMeta'
import { vReveal } from '@/directives/reveal'
import type { AboutOfficeItem, AboutPage, AboutValueItem, NewsSummary } from '@/data/types'

const data = ref<AboutPage | null>(null)
const siteName = ref('G7易流')
const newsList = ref<NewsSummary[]>([])
const activeTab = ref('intro')
const milestoneIndex = ref(1)
const newsStartIndex = ref(0)
const activeOfficeIndex = ref(0)

usePageMeta(() => ({
  title: '关于我们',
  description: data.value?.intro?.slice(0, 120) || '了解企业简介、使命愿景、发展历程、新闻中心与全国办公网点。',
}))

const TABS = [
  { key: 'intro', label: '企业简介', targetId: 'about-intro' },
  { key: 'culture', label: '管理团队', targetId: 'about-culture' },
  { key: 'media', label: '媒体中心', targetId: 'about-media' },
  { key: 'milestones', label: '资料共享', targetId: 'about-milestones' },
  { key: 'join', label: '加入我们', targetId: 'about-join' },
]

const heroTitle = computed(() => data.value?.heroTitle || '以物联网技术和软件服务改变公路货运')

const introTitle = computed(() => data.value?.introTitle || `关于${siteName.value}`)

const introParagraphs = computed(() => {
  const raw = (data.value?.intro || '').trim()
  if (raw) {
    return raw.split(/\n+/).filter(Boolean)
  }
  return [
    `北京汇通天下物联科技有限公司（${siteName.value}）是一家领先的公路货运行业物联网科技和软件服务公司。为货运经营者提供从车队管理、安全管理、货物管理到智能调度、自动结算的一体化操作平台，连接货运全流程并以数据和AI驱动运营，助力货运经营者降本增效。`,
    `${siteName.value}的客户覆盖超80%上游头部货主以及36000+下游中小物流企业，服务网络覆盖全国核心物流节点与产业园区。`,
  ]
})

const missionText = computed(
  () => data.value?.mission || '以科技创新和服务升级物流生产力，让产业参与者共享美好改变',
)

const visionText = computed(() => data.value?.vision || '物流产业最受尊重的SaaS公司')

const DEFAULT_VALUES: AboutValueItem[] = [
  {
    title: '客户成功',
    lines: [
      '我们存在的唯一目的就是为客户带来价值',
      '客户的需求是公司发展、产品创新的源泉:深入场景，洞察客户需求，解决客户痛点问题',
      '以可信赖的产品和有温度的服务，满足甚至超越客户的合理期望，做客户的长期朋友',
    ],
  },
  {
    title: '极限',
    lines: [
      '目标远大，不甘平庸，不断挑战认知边界，把不可能变为可能',
      '敢想、敢试、敢突破，致力于打造有号召力的产品',
      '把事做透，用工匠精神实现卓越成果',
    ],
  },
  {
    title: '信任',
    lines: [
      '以身作则，说到做到',
      '敢于信任，从我做起',
      '主动寻找有共同理想的产业变革者，善于建立信任，打造有格局的朋友圈',
    ],
  },
  {
    title: '坚韧',
    lines: [
      '坚持长期主义积极面对挑战和不确定性，敢于在泥泞中前行',
      '坚定目标，锲而不舍，迎难而上，确保结果',
      '充分发挥主观能动性，多找思路和方法，拒绝借口',
    ],
  },
]

const valuesList = computed<AboutValueItem[]>(() => {
  if (data.value?.values && data.value.values.length > 0) {
    return data.value.values
  }
  return DEFAULT_VALUES
})

interface NormalizedMilestone {
  year: string
  bullets: string[]
}

const DEFAULT_MILESTONES: NormalizedMilestone[] = [
  {
    year: '2018',
    bullets: [
      '发布智能挂车与安全管家托管服务',
      '连接平台货运车辆突破 80 万台',
      '完成由多家国际顶级机构联合领投的战略融资',
    ],
  },
  {
    year: '2019',
    bullets: [
      '平台卡车连接数突破140万',
      '面向煤炭、钢铁、水泥、汽车、快递、快运等多个行业提供综合性数字化解决方案',
      '与中国铝业、普洛斯、丸红等设立合资公司，服务生态扩张',
    ],
  },
  {
    year: '2022',
    bullets: [
      '平台卡车连接数突破 200万',
      '宣布完成新一轮2亿美元融资',
      '嬴彻科技自动驾驶卡车量产交付',
    ],
  },
  {
    year: '2010',
    bullets: [
      '向物流运输公司提供基于北斗和GPS位置信息的车队管理 SaaS 服务',
      '平台连接车辆2000台',
    ],
  },
  {
    year: '2014',
    bullets: [
      '推出智能管车与车队金融结算服务体系',
      '全国设立七大区域运营中心，服务超万家物流企业',
    ],
  },
]

const milestonesList = computed<NormalizedMilestone[]>(() => {
  const raw = data.value?.milestones ?? []
  if (raw.length > 0) {
    return raw.map((m) => {
      const text = [m.title, m.description].filter(Boolean).join('；')
      const bullets = text
        .split(/[\n；;]+/)
        .map((s) => s.trim())
        .filter(Boolean)
      return {
        year: m.year,
        bullets: bullets.length ? bullets : [m.title || '关键发展节点'],
      }
    })
  }
  return DEFAULT_MILESTONES
})

// 顶部显示的 5 个年份窗口（以当前选中项为中心）
const visibleYearWindow = computed(() => {
  const list = milestonesList.value
  const total = list.length
  if (total === 0) return []
  const count = Math.min(5, total)
  const half = Math.floor(count / 2)
  const result: Array<{ index: number; year: string; isCenter: boolean }> = []
  for (let offset = -half; offset <= half; offset++) {
    if (result.length >= count) break
    const idx = (milestoneIndex.value + offset + total) % total
    result.push({
      index: idx,
      year: list[idx].year,
      isCenter: idx === milestoneIndex.value,
    })
  }
  return result
})

// 下方 3 张卡片（左 / 中高亮 / 右）
const visibleMilestoneCards = computed(() => {
  const list = milestonesList.value
  const total = list.length
  if (total === 0) return []
  if (total === 1) return [{ index: 0, item: list[0], isCenter: true }]
  if (total === 2) {
    const nextIdx = (milestoneIndex.value + 1) % total
    return [
      { index: milestoneIndex.value, item: list[milestoneIndex.value], isCenter: true },
      { index: nextIdx, item: list[nextIdx], isCenter: false },
    ]
  }
  const prevIdx = (milestoneIndex.value - 1 + total) % total
  const nextIdx = (milestoneIndex.value + 1) % total
  return [
    { index: prevIdx, item: list[prevIdx], isCenter: false },
    { index: milestoneIndex.value, item: list[milestoneIndex.value], isCenter: true },
    { index: nextIdx, item: list[nextIdx], isCenter: false },
  ]
})

function prevMilestone() {
  const total = milestonesList.value.length
  if (!total) return
  milestoneIndex.value = (milestoneIndex.value - 1 + total) % total
}

function nextMilestone() {
  const total = milestonesList.value.length
  if (!total) return
  milestoneIndex.value = (milestoneIndex.value + 1) % total
}

const DEFAULT_NEWS: NewsSummary[] = [
  {
    id: '1',
    title: 'G7与E6宣布完成合并',
    summary: '双方正式完成战略合并，共同打造公路货运行业领先的物联网与软件服务平台。',
    cover: null,
    categoryId: null,
    categoryName: '企业动态',
    displayTime: '2022/06/07',
  },
  {
    id: '2',
    title: '让你的物流AI开工！2026数字物流大会精彩日程抢先看',
    summary: '聚焦物流全场景 AI 智能体与 IoT 深度融合实践。',
    cover: null,
    categoryId: null,
    categoryName: '行业峰会',
    displayTime: '2026/10/16',
  },
  {
    id: '3',
    title: '走G7「新」路 马上出发！——老翟新能源车队一线探访',
    summary: '深入干线与大宗运输现场，探访新能源重卡车队真实运营效益。',
    cover: null,
    categoryId: null,
    categoryName: '前沿洞察',
    displayTime: '2026/10/13',
  },
  {
    id: '4',
    title: '2026物流小巨人启动——「新能源&AI」双榜单全新招募！',
    summary: '寻找物流产业最具技术领导力与增长韧性的标杆企业。',
    cover: null,
    categoryId: null,
    categoryName: '品牌活动',
    displayTime: '2026/09/23',
  },
]

const resolvedNews = computed<NewsSummary[]>(() => {
  return newsList.value.length > 0 ? newsList.value : DEFAULT_NEWS
})

const visibleNewsCards = computed(() => {
  const list = resolvedNews.value
  if (list.length <= 4) return list
  const out: NewsSummary[] = []
  for (let i = 0; i < 4; i++) {
    out.push(list[(newsStartIndex.value + i) % list.length])
  }
  return out
})

function prevNews() {
  const len = resolvedNews.value.length
  if (len <= 4) return
  newsStartIndex.value = (newsStartIndex.value - 1 + len) % len
}

function nextNews() {
  const len = resolvedNews.value.length
  if (len <= 4) return
  newsStartIndex.value = (newsStartIndex.value + 1) % len
}

const DEFAULT_OFFICES: AboutOfficeItem[] = [
  {
    city: '北京（总部）',
    phone: '010-62667878',
    address: '北京市 海淀区 建材城中路 27 号金隅智造工场 N7 号楼 2 层',
    mapX: 68,
    mapY: 42,
  },
  {
    city: '深圳',
    phone: '0755-26944168',
    address: '深圳市南山区滨海大道 3369 号有线信息传输大厦 9 层',
    mapX: 67,
    mapY: 75,
  },
  {
    city: '成都',
    phone: '028-83358692',
    address: '成都市 天府新区 兴隆湖畔路西段 99 号天府新经济产业园 D 区 B6 栋 28 楼',
    mapX: 54,
    mapY: 62,
  },
  {
    city: '广州',
    phone: '020-38889000',
    address: '广州市天河区珠江新城花城大道 85 号高德置地广场春广场',
    mapX: 66,
    mapY: 71,
  },
]

const officesList = computed<AboutOfficeItem[]>(() => {
  if (data.value?.offices && data.value.offices.length > 0) {
    return data.value.offices
  }
  return DEFAULT_OFFICES
})

const currentOffice = computed(() => officesList.value[activeOfficeIndex.value] ?? officesList.value[0])

function scrollToSection(tabKey: string, targetId: string) {
  activeTab.value = tabKey
  const el = document.getElementById(targetId)
  if (el) {
    const y = el.getBoundingClientRect().top + window.scrollY - 90
    window.scrollTo({ top: y, behavior: 'smooth' })
  }
}

function onScroll() {
  for (const tab of TABS) {
    const el = document.getElementById(tab.targetId)
    if (el) {
      const rect = el.getBoundingClientRect()
      if (rect.top <= 180 && rect.bottom >= 180) {
        activeTab.value = tab.key
        break
      }
    }
  }
}

onMounted(async () => {
  window.addEventListener('scroll', onScroll, { passive: true })
  const [aboutRes, siteRes, newsRes] = await Promise.all([
    getAbout().catch(() => null),
    getSite().catch(() => null),
    getNews({ page: 1, pageSize: 8 }).catch(() => ({ items: [] })),
  ])
  if (aboutRes) {
    data.value = aboutRes
    if (milestonesList.value.length >= 3) {
      milestoneIndex.value = Math.min(2, milestonesList.value.length - 1)
    } else {
      milestoneIndex.value = 0
    }
  }
  if (siteRes?.siteName) {
    siteName.value = siteRes.siteName
  }
  if (newsRes?.items?.length) {
    newsList.value = newsRes.items
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<template>
  <div class="about-g7">
    <!-- 1. 顶部深空流光透视首屏 Banner（1:1 对齐 image_1254cc.jpg） -->
    <section class="about-hero">
      <div class="about-hero__trails" aria-hidden="true"></div>
      <div class="container about-hero__inner">
        <div v-reveal class="about-hero__logo-mark" aria-hidden="true">G</div>
        <h1 v-reveal="100" class="about-hero__title">{{ heroTitle }}</h1>
      </div>
    </section>

    <!-- 2. 悬浮白底五页签导航条（1:1 对齐 image_1254cc.jpg / image_1250ec.jpg） -->
    <div class="container about-tabs-wrap">
      <nav v-reveal="140" class="about-tabs" aria-label="关于我们页内导航">
        <button
          v-for="tab in TABS"
          :key="tab.key"
          type="button"
          class="about-tabs__btn"
          :class="{ 'is-active': activeTab === tab.key }"
          @click="scrollToSection(tab.key, tab.targetId)"
        >
          {{ tab.label }}
        </button>
      </nav>
    </div>

    <!-- 3. 企业简介：左文右图（1:1 对齐 image_1254cc.jpg） -->
    <section id="about-intro" class="about-intro-sec">
      <div class="container about-intro-grid">
        <div v-reveal class="about-intro__text">
          <h2 class="about-sec-title">{{ introTitle }}</h2>
          <p v-for="(para, pIdx) in introParagraphs" :key="pIdx" class="about-intro__para">
            {{ para }}
          </p>
        </div>

        <div v-reveal="140" class="about-intro__visual">
          <div class="about-intro__photo">
            <div class="about-intro__radar" aria-hidden="true"></div>
            <div class="about-intro__truck-scene" aria-hidden="true">
              <span class="scene-tag">AI + IoT 全链路实时感知</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 4. 使命 · 愿景 · 价值观（1:1 对齐 image_1249bf.png & image_1249a8.jpg，每一行随滚动缓慢弹出） -->
    <section id="about-culture" class="about-culture-sec">
      <div class="container">
        <!-- 使命 -->
        <div v-reveal class="culture-row">
          <div class="culture-row__label">使命</div>
          <div class="culture-row__content">
            <h3 class="culture-row__headline">{{ missionText }}</h3>
          </div>
        </div>

        <!-- 愿景 -->
        <div v-reveal="80" class="culture-row">
          <div class="culture-row__label">愿景</div>
          <div class="culture-row__content">
            <h3 class="culture-row__headline">{{ visionText }}</h3>
          </div>
        </div>

        <!-- 价值观 -->
        <div class="culture-row culture-row--values">
          <div v-reveal class="culture-row__label">价值观</div>
          <div class="culture-row__content culture-values-list">
            <div
              v-for="(valItem, vIdx) in valuesList"
              :key="valItem.title"
              v-reveal="vIdx * 90"
              class="value-block"
            >
              <h4 class="value-block__title">{{ valItem.title }}</h4>
              <p v-for="(line, lIdx) in valItem.lines" :key="lIdx" class="value-block__line">
                {{ line }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 5. 发展历程横向时间轴轮播（1:1 对齐 image_1254a8.png） -->
    <section id="about-milestones" class="about-history-sec">
      <div class="container">
        <h2 v-reveal class="about-center-title">发展历程</h2>

        <!-- 顶部年份轴 + 左右切换箭头 -->
        <div v-reveal="100" class="history-axis">
          <button
            type="button"
            class="history-arrow"
            aria-label="上一年份"
            @click="prevMilestone"
          >
            ‹
          </button>

          <div class="history-years">
            <button
              v-for="yr in visibleYearWindow"
              :key="`${yr.index}-${yr.year}`"
              type="button"
              class="history-year-btn"
              :class="{ 'is-center': yr.isCenter }"
              @click="milestoneIndex = yr.index"
            >
              {{ yr.year }}
            </button>
          </div>

          <button
            type="button"
            class="history-arrow"
            aria-label="下一年份"
            @click="nextMilestone"
          >
            ›
          </button>
        </div>

        <!-- 下方三列历程卡片（中间卡片白底悬浮阴影放大 + 紫色圆环靶心节点） -->
        <div v-reveal="160" class="history-stage">
          <div
            v-for="card in visibleMilestoneCards"
            :key="`card-${card.index}`"
            class="history-card"
            :class="{ 'is-center': card.isCenter }"
            @click="milestoneIndex = card.index"
          >
            <div class="history-card__year">{{ card.item.year }}</div>
            <div class="history-card__line">
              <span v-if="card.isCenter" class="history-card__target" aria-hidden="true"></span>
              <span v-else class="history-card__dot" aria-hidden="true"></span>
            </div>
            <ul class="history-card__bullets">
              <li v-for="(b, bIdx) in card.item.bullets" :key="bIdx">{{ b }}</li>
            </ul>
          </div>
        </div>
      </div>
    </section>

    <!-- 6. 媒体中心 / 新闻中心 4 卡横向轮播（1:1 对齐 image_1250ec.jpg） -->
    <section id="about-media" class="about-news-sec">
      <div class="container">
        <div v-reveal class="about-news__head">
          <h2 class="about-sec-title">新闻中心</h2>
          <RouterLink to="/news" class="about-news__more-btn">查看更多</RouterLink>
        </div>

        <div v-reveal="120" class="about-news-slider">
          <button
            type="button"
            class="slider-arrow slider-arrow--left"
            aria-label="上一组新闻"
            @click="prevNews"
          >
            ‹
          </button>

          <div class="about-news-grid">
            <RouterLink
              v-for="(item, nIdx) in visibleNewsCards"
              :key="`${item.id}-${nIdx}`"
              :to="`/news/${item.id}`"
              class="about-news-card"
            >
              <div class="about-news-card__poster" :class="`poster-theme--${nIdx % 4}`">
                <img
                  v-if="item.cover?.url"
                  :src="item.cover.url"
                  :alt="item.cover.alt || item.title"
                  loading="lazy"
                />
                <div v-else class="poster-fallback">
                  <span class="poster-fallback__brand">{{ siteName }}</span>
                  <strong class="poster-fallback__text">{{ item.title.slice(0, 14) }}</strong>
                </div>
              </div>
              <div class="about-news-card__body">
                <h3 class="about-news-card__title">{{ item.title }}</h3>
                <div class="about-news-card__time">
                  <span aria-hidden="true">🕒</span>
                  <span>{{ item.displayTime || '2026/10/16' }}</span>
                </div>
              </div>
            </RouterLink>
          </div>

          <button
            type="button"
            class="slider-arrow slider-arrow--right"
            aria-label="下一组新闻"
            @click="nextNews"
          >
            ›
          </button>
        </div>
      </div>
    </section>

    <!-- 7. 加入我们横幅海报（1:1 对齐 image_1249c3.jpg） -->
    <section id="about-join" class="about-join-sec">
      <div class="container">
        <div v-reveal class="join-banner">
          <div class="join-banner__content">
            <h2 class="join-banner__title">{{ data?.joinTitle || `${siteName}路上见` }}</h2>
            <button
              type="button"
              class="join-banner__btn"
              @click="openConsult({ sourcePosition: 'OTHER_CTA', title: '人才招聘与职位咨询' })"
            >
              {{ data?.joinButtonText || '查看职位' }}
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 8. 联系我们：多城市左侧切换列表 + 右侧点阵地图与实景照片气泡（1:1 对齐 image_1254a5.jpg） -->
    <section id="about-contact" class="about-offices-sec">
      <div class="container">
        <h2 v-reveal class="about-center-title">联系我们</h2>

        <div class="offices-layout">
          <!-- 左侧城市列表 -->
          <div v-reveal="80" class="offices-list">
            <div
              v-for="(off, oIdx) in officesList"
              :key="off.city"
              class="office-item"
              :class="{ 'is-active': activeOfficeIndex === oIdx }"
              @mouseenter="activeOfficeIndex = oIdx"
              @click="activeOfficeIndex = oIdx"
            >
              <h3 class="office-item__city">{{ off.city }}</h3>
              <template v-if="off.phone || off.address">
                <p v-if="off.phone" class="office-item__line">
                  <span>电话：</span>{{ off.phone }}
                </p>
                <p v-if="off.address" class="office-item__line">
                  <span>地址：</span>{{ off.address }}
                </p>
              </template>
            </div>
          </div>

          <!-- 右侧点阵地图与实景园区悬浮气泡 -->
          <div v-reveal="160" class="offices-map">
            <div class="offices-map__canvas">
              <!-- 悬浮园区照片框 -->
              <div class="map-photo-popup">
                <div class="map-photo-popup__img">
                  <div class="building-illustration">
                    <span class="building-illustration__tag">{{ currentOffice?.city }}</span>
                  </div>
                </div>
                <span class="map-photo-popup__arrow" aria-hidden="true"></span>
              </div>

              <!-- 城市坐标点 -->
              <div class="map-pins">
                <button
                  v-for="(off, oIdx) in officesList"
                  :key="off.city"
                  type="button"
                  class="map-pin"
                  :class="{ 'is-active': activeOfficeIndex === oIdx }"
                  :style="{ left: `${off.mapX || 65}%`, top: `${off.mapY || 45 + oIdx * 10}%` }"
                  @click="activeOfficeIndex = oIdx"
                >
                  <span class="map-pin__dot"></span>
                  <span class="map-pin__name">{{ off.city }}</span>
                </button>
                <span class="map-pin map-pin--static" style="left: 70%; top: 47%">
                  <span class="map-pin__dot"></span>
                  <span class="map-pin__name">天津</span>
                </span>
                <span class="map-pin map-pin--static" style="left: 58%; top: 53%">
                  <span class="map-pin__dot"></span>
                  <span class="map-pin__name">西安</span>
                </span>
                <span class="map-pin map-pin--static" style="left: 74%; top: 58%">
                  <span class="map-pin__dot"></span>
                  <span class="map-pin__name">上海</span>
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.about-g7 {
  background: #ffffff;
  color: #1f2329;
}

/* 1. 顶部深空流光 Banner */
.about-hero {
  position: relative;
  height: 400px;
  background: radial-gradient(ellipse at 50% 85%, #2b1d52 0%, #100e20 55%, #07070c 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  overflow: hidden;
}

.about-hero__trails {
  position: absolute;
  inset: 0;
  background:
    repeating-linear-gradient(
      115deg,
      transparent 0px,
      transparent 38px,
      rgba(245, 158, 11, 0.08) 39px,
      transparent 41px
    ),
    repeating-linear-gradient(
      65deg,
      transparent 0px,
      transparent 44px,
      rgba(99, 102, 241, 0.12) 45px,
      transparent 48px
    );
  opacity: 0.85;
}

.about-hero__inner {
  position: relative;
  z-index: 2;
  padding-bottom: 28px;
}

.about-hero__logo-mark {
  font-size: 56px;
  font-weight: 900;
  font-style: italic;
  color: #ffffff;
  line-height: 1;
  margin-bottom: 14px;
  letter-spacing: -0.05em;
}

.about-hero__title {
  font-size: clamp(26px, 3.6vw, 40px);
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 0.02em;
}

/* 2. 悬浮白底五页签导航条 */
.about-tabs-wrap {
  position: relative;
  z-index: 10;
  margin-top: -42px;
}

.about-tabs {
  max-width: 980px;
  height: 82px;
  margin: 0 auto;
  background: #ffffff;
  box-shadow: 0 12px 36px rgba(16, 24, 40, 0.06);
  display: flex;
  align-items: center;
  padding: 0 48px;
  gap: 56px;
}

.about-tabs__btn {
  position: relative;
  height: 100%;
  border: none;
  background: none;
  font-size: 15px;
  color: #2b2f38;
  cursor: pointer;
  padding: 0 4px;
  transition: color 0.2s ease;
}

.about-tabs__btn:hover,
.about-tabs__btn.is-active {
  color: #5b4be8;
  font-weight: 600;
}

.about-tabs__btn.is-active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 14px;
  height: 3px;
  background: #5b4be8;
  border-radius: 2px;
}

/* 3. 企业简介 */
.about-intro-sec {
  padding: 96px 0 80px;
}

.about-intro-grid {
  max-width: 1040px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 64px;
  align-items: center;
}

.about-sec-title {
  font-size: 34px;
  font-weight: 700;
  color: #1f2329;
  margin-bottom: 28px;
}

.about-intro__para {
  font-size: 14px;
  line-height: 2;
  color: #6e7380;
  margin-bottom: 16px;
}

.about-intro__photo {
  position: relative;
  aspect-ratio: 4 / 3;
  background: linear-gradient(140deg, #1b2432 0%, #0f1722 60%, #1e1b4b 100%);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.about-intro__radar {
  width: 240px;
  height: 240px;
  border-radius: 50%;
  background: conic-gradient(
    from 210deg,
    rgba(124, 58, 237, 0.55) 0deg,
    rgba(56, 189, 248, 0.35) 75deg,
    transparent 120deg
  );
  filter: blur(2px);
}

.scene-tag {
  position: absolute;
  bottom: 20px;
  right: 20px;
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.22);
  color: #ffffff;
  font-size: 12.5px;
  border-radius: 4px;
}

/* 4. 使命 / 愿景 / 价值观 */
.about-culture-sec {
  padding: 40px 0 96px;
}

.culture-row {
  max-width: 1040px;
  margin: 0 auto;
  padding: 64px 0;
  border-bottom: 1px solid #eceef2;
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 32px;
  align-items: baseline;
}

.culture-row--values {
  border-bottom: none;
}

.culture-row__label {
  font-size: 20px;
  color: #5f6470;
}

.culture-row__headline {
  font-size: clamp(24px, 2.8vw, 32px);
  font-weight: 700;
  color: #2b2f38;
  line-height: 1.4;
}

.culture-values-list {
  display: flex;
  flex-direction: column;
  gap: 52px;
}

.value-block__title {
  font-size: 26px;
  font-weight: 700;
  color: #2b2f38;
  margin-bottom: 16px;
}

.value-block__line {
  font-size: 14px;
  line-height: 2;
  color: #6e7380;
  margin: 2px 0;
}

/* 5. 发展历程横向时间轴 */
.about-history-sec {
  padding: 88px 0 110px;
  background: #fcfcfd;
}

.about-center-title {
  text-align: center;
  font-size: 36px;
  font-weight: 700;
  color: #2b2f38;
  margin-bottom: 52px;
}

.history-axis {
  max-width: 920px;
  margin: 0 auto 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.history-arrow {
  width: 36px;
  height: 36px;
  border: none;
  background: none;
  font-size: 34px;
  color: #8b90a0;
  cursor: pointer;
  line-height: 1;
}

.history-arrow:hover {
  color: #5b4be8;
}

.history-years {
  flex: 1;
  display: flex;
  align-items: baseline;
  justify-content: space-around;
  padding-bottom: 20px;
  border-bottom: 1px solid #e5e7eb;
}

.history-year-btn {
  border: none;
  background: none;
  font-size: 16px;
  font-weight: 700;
  color: #2b2f38;
  cursor: pointer;
  transition: transform 0.25s ease, font-size 0.25s ease, color 0.25s ease;
}

.history-year-btn.is-center {
  font-size: 28px;
  color: #1f2329;
  transform: translateY(-2px);
}

.history-stage {
  max-width: 1040px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 28px;
  align-items: stretch;
}

.history-card {
  padding: 40px 32px;
  border-radius: 4px;
  background: transparent;
  cursor: pointer;
  transition: all 0.3s ease;
}

.history-card.is-center {
  background: #ffffff;
  box-shadow: 0 20px 50px rgba(16, 24, 40, 0.08);
}

.history-card__year {
  font-size: 28px;
  font-weight: 700;
  color: #3a3f4a;
  margin-bottom: 20px;
}

.history-card__line {
  position: relative;
  height: 20px;
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}

.history-card__line::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  top: 50%;
  border-bottom: 1px dashed #d0d5dd;
}

.history-card__dot {
  position: relative;
  z-index: 2;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #98a2b3;
}

.history-card__target {
  position: relative;
  z-index: 2;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid #5b4be8;
  background: #ffffff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.history-card__target::after {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #5b4be8;
}

.history-card__bullets {
  list-style: disc;
  padding-left: 18px;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  font-size: 13.5px;
  line-height: 1.8;
  color: #5f6470;
}

/* 6. 新闻中心 4 卡横向轮播 */
.about-news-sec {
  padding: 88px 0 96px;
  background: #f5f7fb;
}

.about-news__head {
  max-width: 1080px;
  margin: 0 auto 36px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.about-news__head .about-sec-title {
  margin-bottom: 0;
}

.about-news__more-btn {
  padding: 9px 28px;
  border: 1px solid #1f2329;
  font-size: 13.5px;
  color: #1f2329;
  background: transparent;
  transition: all 0.2s ease;
}

.about-news__more-btn:hover {
  background: #1f2329;
  color: #ffffff;
}

.about-news-slider {
  max-width: 1180px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 16px;
}

.slider-arrow {
  width: 36px;
  height: 36px;
  border: none;
  background: none;
  font-size: 34px;
  color: #98a2b3;
  cursor: pointer;
  flex: none;
}

.slider-arrow:hover {
  color: #5b4be8;
}

.about-news-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.about-news-card {
  background: #ffffff;
  display: flex;
  flex-direction: column;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.about-news-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 16px 36px rgba(16, 24, 40, 0.08);
}

.about-news-card__poster {
  aspect-ratio: 1 / 1;
  overflow: hidden;
  position: relative;
}

.about-news-card__poster img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.poster-fallback {
  width: 100%;
  height: 100%;
  padding: 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: #ffffff;
}

.poster-theme--0 .poster-fallback {
  background: linear-gradient(145deg, #2563eb 0%, #1e3a8a 60%, #0f172a 100%);
}

.poster-theme--1 .poster-fallback {
  background: linear-gradient(145deg, #4f46e5 0%, #311042 65%, #09090f 100%);
}

.poster-theme--2 .poster-fallback {
  background: linear-gradient(145deg, #0284c7 0%, #1e40af 65%, #0f172a 100%);
}

.poster-theme--3 .poster-fallback {
  background: linear-gradient(145deg, #78350f 0%, #271709 65%, #09090b 100%);
}

.poster-fallback__brand {
  font-size: 13px;
  font-weight: 700;
  opacity: 0.85;
}

.poster-fallback__text {
  font-size: 20px;
  line-height: 1.35;
}

.about-news-card__body {
  padding: 20px 18px 22px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 126px;
}

.about-news-card__title {
  font-size: 14.5px;
  font-weight: 500;
  color: #2b2f38;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.about-news-card__time {
  margin-top: 14px;
  font-size: 12px;
  color: #98a2b3;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 7. 加入我们 Banner */
.about-join-sec {
  padding: 80px 0;
}

.join-banner {
  max-width: 1040px;
  margin: 0 auto;
  height: 320px;
  background:
    linear-gradient(90deg, rgba(16, 18, 28, 0.72) 0%, rgba(16, 18, 28, 0.25) 65%),
    radial-gradient(circle at 80% 30%, #4b5563 0%, #1f2937 100%);
  display: flex;
  align-items: center;
  padding: 0 72px;
}

.join-banner__title {
  font-size: 36px;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 24px;
}

.join-banner__btn {
  padding: 10px 32px;
  border: 1px solid rgba(255, 255, 255, 0.85);
  background: transparent;
  color: #ffffff;
  font-size: 13.5px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.join-banner__btn:hover {
  background: #ffffff;
  color: #101014;
}

/* 8. 联系我们：多城市列表 + 点阵地图 */
.about-offices-sec {
  padding: 88px 0 110px;
  background: #f2f4f8;
}

.offices-layout {
  max-width: 1040px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 440px 1fr;
  gap: 56px;
  align-items: center;
}

.offices-list {
  position: relative;
  border-right: 2px solid #e2e6ee;
  padding-right: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.office-item {
  position: relative;
  padding: 18px 24px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.office-item.is-active {
  background: #ffffff;
  box-shadow: 0 10px 28px rgba(16, 24, 40, 0.05);
}

.office-item.is-active::after {
  content: '';
  position: absolute;
  right: -26px;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #4b5563;
}

.office-item__city {
  font-size: 17px;
  font-weight: 700;
  color: #2b2f38;
  margin-bottom: 8px;
}

.office-item__line {
  font-size: 13px;
  line-height: 1.75;
  color: #5f6470;
  margin: 2px 0;
}

.office-item__line span {
  color: #98a2b3;
}

.offices-map__canvas {
  position: relative;
  height: 400px;
  background-image: radial-gradient(#d0d5dd 1.25px, transparent 1.25px);
  background-size: 12px 12px;
  border-radius: 12px;
}

.map-photo-popup {
  position: absolute;
  top: 24px;
  left: 50%;
  transform: translateX(-50%);
  width: 280px;
  height: 156px;
  border: 2px solid #2b2f38;
  border-radius: 6px;
  background: #ffffff;
  overflow: visible;
  box-shadow: 0 16px 36px rgba(16, 24, 40, 0.14);
}

.map-photo-popup__img {
  width: 100%;
  height: 100%;
  overflow: hidden;
  border-radius: 4px;
}

.building-illustration {
  width: 100%;
  height: 100%;
  background: linear-gradient(160deg, #3b82f6 0%, #1d4ed8 45%, #1e293b 100%);
  display: flex;
  align-items: flex-end;
  padding: 14px;
}

.building-illustration__tag {
  padding: 4px 10px;
  border-radius: 4px;
  background: rgba(15, 23, 42, 0.75);
  color: #ffffff;
  font-size: 12px;
  font-weight: 600;
}

.map-photo-popup__arrow {
  position: absolute;
  bottom: -9px;
  left: 62%;
  width: 14px;
  height: 14px;
  background: #2b2f38;
  transform: rotate(45deg);
}

.map-pin {
  position: absolute;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border: none;
  background: none;
  font-size: 12px;
  color: #4b5563;
  cursor: pointer;
  padding: 2px 4px;
}

.map-pin__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  border: 2px solid #98a2b3;
  background: #ffffff;
}

.map-pin.is-active {
  font-weight: 700;
  color: #101014;
}

.map-pin.is-active .map-pin__dot {
  width: 10px;
  height: 10px;
  border-color: #101014;
  background: #ffffff;
}

.map-pin--static {
  pointer-events: none;
  opacity: 0.75;
}

@media (max-width: 920px) {
  .about-tabs {
    padding: 0 20px;
    gap: 20px;
    overflow-x: auto;
  }
  .about-intro-grid,
  .culture-row,
  .history-stage,
  .offices-layout {
    grid-template-columns: 1fr;
  }
  .about-news-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
