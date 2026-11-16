<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAbout, getNews, getSite } from '@/api/public'
import { openConsult } from '@/composables/consult'
import { openVideo } from '@/composables/videoModal'
import { usePageMeta } from '@/composables/usePageMeta'
import { useRealtimeSync } from '@/composables/useRealtimeSync'
import { isPlaceholderContact } from '@/utils/siteDisplay'
import { vReveal } from '@/directives/reveal'
import type {
  AboutCeoLetter,
  AboutOfficeItem,
  AboutPage,
  AboutResourceItem,
  AboutTeamMember,
  AboutValueItem,
  NewsSummary,
} from '@/data/types'

const route = useRoute()
const router = useRouter()
const data = ref<AboutPage | null>(null)
const siteName = ref('企业官网')
const newsList = ref<NewsSummary[]>([])
const activeTab = ref<'intro' | 'team' | 'media' | 'resources' | 'join'>('intro')

const ceoModalOpen = ref(false)
const activeTeamMember = ref<AboutTeamMember | null>(null)
const milestoneIndex = ref(0)
const newsStartIndex = ref(0)
const activeOfficeIndex = ref(0)
const activeResourceCat = ref('企业资料')
const resourceStartIndex = ref(0)

usePageMeta(() => ({
  title: '关于我们',
  description:
    data.value?.intro?.slice(0, 120) ||
    '了解企业简介、发展历程、管理团队、媒体中心、资料共享与加入我们。',
}))

const TABS: Array<{ key: 'intro' | 'team' | 'media' | 'resources' | 'join'; label: string }> = [
  { key: 'intro', label: '企业简介' },
  { key: 'team', label: '管理团队' },
  { key: 'media', label: '媒体中心' },
  { key: 'resources', label: '资料共享' },
  { key: 'join', label: '加入我们' },
]

function syncTabFromQuery() {
  const q = String(route.query.tab || '').toLowerCase()
  if (q === 'team' || q === 'culture') activeTab.value = 'team'
  else if (q === 'media') activeTab.value = 'media'
  else if (q === 'resources') activeTab.value = 'resources'
  else if (q === 'join') activeTab.value = 'join'
  else if (q === 'milestones') {
    activeTab.value = 'intro'
    setTimeout(() => {
      document.querySelector('.about-history-sec')?.scrollIntoView({ behavior: 'smooth' })
    }, 200)
  } else {
    activeTab.value = 'intro'
  }
}

watch(
  () => route.query.tab,
  () => {
    syncTabFromQuery()
  },
)

function switchTab(tabKey: 'intro' | 'team' | 'media' | 'resources' | 'join') {
  activeTab.value = tabKey
  router.replace({ path: '/about', query: { tab: tabKey }, hash: '#part' }).catch(() => {})
}

const heroTitle = computed(() => data.value?.heroTitle || `关于${siteName.value}`)
const introTitle = computed(() => data.value?.introTitle || `关于${siteName.value}`)

const introParagraphs = computed(() => {
  const raw = (data.value?.intro || '').trim()
  if (!raw) return []
  return raw.split(/\n+/).filter(Boolean)
})

const introVisualType = computed<'none' | 'image' | 'radar'>(() => {
  if (data.value?.showIntroImage === false) return 'none'
  const mode = data.value?.introImageMode
  if (mode === 'none') return 'none'
  if (mode === 'radar') return 'radar'
  if (mode === 'image' && data.value?.introImage) return 'image'
  if (data.value?.introImage) return 'image'
  return 'none'
})

const hasIntroVisual = computed(() => introVisualType.value !== 'none')

const statsList = computed(() => {
  const raw = data.value?.stats
  if (!Array.isArray(raw)) return []
  return raw.filter((s) => s && (s.displayValue || s.value !== undefined) && s.label)
})

// 1. CEO的一封信：仅当后台真实配置了 title 或正文时展示，绝不回退参考公司高管签名
const ceoLetter = computed<AboutCeoLetter | null>(() => {
  const raw = data.value?.ceoLetter
  if (!raw || (!raw.title && !(Array.isArray(raw.excerpt) && raw.excerpt.length > 0))) {
    return null
  }
  const excerpt = Array.isArray(raw.excerpt) ? raw.excerpt.filter(Boolean) : []
  const fullParagraphs =
    Array.isArray(raw.fullParagraphs) && raw.fullParagraphs.length > 0
      ? raw.fullParagraphs.filter(Boolean)
      : excerpt
  return {
    leftQuote: raw.leftQuote || '',
    title: raw.title || '致合作伙伴的一封信',
    signature: raw.signature || '',
    buttonText: raw.buttonText || '展开全部',
    excerpt,
    fullParagraphs,
  }
})

// 2. 发展历程：仅读取后台真实配置的 milestones
interface NormalizedMilestone {
  year: string
  bullets: string[]
}

const milestonesList = computed<NormalizedMilestone[]>(() => {
  const raw = data.value?.milestones ?? []
  if (!Array.isArray(raw) || raw.length === 0) return []
  return raw
    .filter((m) => m && m.year)
    .map((m) => {
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
})

const visibleYearWindow = computed(() => {
  const list = milestonesList.value
  const total = list.length
  if (total === 0) return []
  const currentIdx = Math.min(milestoneIndex.value, total - 1)
  const count = Math.min(5, total)
  const half = Math.floor(count / 2)
  const result: Array<{ index: number; year: string; isCenter: boolean }> = []
  for (let offset = -half; offset <= half; offset++) {
    if (result.length >= count) break
    const idx = (currentIdx + offset + total) % total
    result.push({
      index: idx,
      year: list[idx].year,
      isCenter: idx === currentIdx,
    })
  }
  return result
})

const visibleMilestoneCards = computed(() => {
  const list = milestonesList.value
  const total = list.length
  if (total === 0) return []
  if (total === 1) {
    return [{ index: 0, item: list[0], isCenter: true }]
  }
  const currentIdx = Math.min(milestoneIndex.value, total - 1)
  const prevIdx = (currentIdx - 1 + total) % total
  const nextIdx = (currentIdx + 1) % total
  return [
    { index: prevIdx, item: list[prevIdx], isCenter: false },
    { index: currentIdx, item: list[currentIdx], isCenter: true },
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

// 3. 联系网点
const officesList = computed<AboutOfficeItem[]>(() => {
  if (data.value?.showOffices === false) return []
  return Array.isArray(data.value?.offices) ? data.value.offices.filter((o) => o && o.city) : []
})

const currentOffice = computed(
  () => officesList.value[activeOfficeIndex.value] ?? officesList.value[0],
)

// 4. 管理团队
const teamList = computed<AboutTeamMember[]>(() => {
  return Array.isArray(data.value?.teamMembers)
    ? data.value.teamMembers.filter((m) => m && m.name)
    : []
})

// 5. 媒体中心与企业故事
const resolvedNews = computed<NewsSummary[]>(() => newsList.value)

const storiesList = computed(() => {
  if (data.value?.showStories === false) return []
  return Array.isArray(data.value?.stories) ? data.value.stories.filter((s) => s && s.title) : []
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

// 6. 资料共享：仅展示后台真实配置的资料，不回退参考公司资料
const allResources = computed<AboutResourceItem[]>(() => {
  const custom = data.value?.resources
  if (!Array.isArray(custom)) return []
  return custom.filter((r) => r && r.title)
})

const resourceCategories = computed<string[]>(() => {
  const cats = new Set<string>()
  for (const r of allResources.value) {
    if (r.category) cats.add(r.category)
  }
  return cats.size > 0 ? Array.from(cats) : ['企业资料', '客户视频', '行业报告', 'ESG']
})

const filteredResources = computed<AboutResourceItem[]>(() => {
  if (allResources.value.length === 0) return []
  const byCat = allResources.value.filter((r) => r.category === activeResourceCat.value)
  return byCat.length > 0 ? byCat : allResources.value
})

const visibleResources = computed(() => {
  const list = filteredResources.value
  if (list.length <= 4) return list
  const out: AboutResourceItem[] = []
  for (let i = 0; i < 4; i++) {
    out.push(list[(resourceStartIndex.value + i) % list.length])
  }
  return out
})

function handleResourceClick(item: AboutResourceItem) {
  if ((item.isVideo || item.category === '客户视频') && item.url) {
    openVideo({
      title: `${item.title}`,
      url: item.url,
    })
    return
  }
  if (item.url && (item.url.startsWith('http') || item.url.startsWith('/'))) {
    window.open(item.url, '_blank', 'noopener,noreferrer')
    return
  }
  openConsult({
    sourcePosition: 'ABOUT_RESOURCE',
    title: '获取企业资料与白皮书',
    initialDemand: `申请获取《${item.title}》资料`,
  })
}

// 7. 加入我们：使命 / 愿景 / 价值观（仅展示真实配置内容）
const missionText = computed(() => (data.value?.mission || '').trim())
const visionText = computed(() => (data.value?.vision || '').trim())

const valuesList = computed<AboutValueItem[]>(() => {
  const raw = data.value?.values
  if (!Array.isArray(raw) || raw.length === 0) return []
  return raw
    .filter((v) => v && v.title)
    .map((v) => ({
      title: v.title,
      lines: Array.isArray(v.lines)
        ? v.lines
        : typeof v.lines === 'string'
          ? (v.lines as string).split('\n').map((s) => s.trim()).filter(Boolean)
          : [],
    }))
})

function handleJoinClick() {
  if (data.value?.joinUrl && data.value.joinUrl.startsWith('http')) {
    window.open(data.value.joinUrl, '_blank', 'noopener,noreferrer')
    return
  }
  openConsult({
    sourcePosition: 'ABOUT_JOIN',
    title: '了解开放职位与投递简历',
    initialDemand: '我希望了解当前开放岗位并沟通投递简历。',
  })
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') {
    ceoModalOpen.value = false
    activeTeamMember.value = null
  }
}

async function reloadAll() {
  const [aboutRes, siteRes, newsRes] = await Promise.all([
    getAbout().catch(() => null),
    getSite().catch(() => null),
    getNews({ page: 1, pageSize: 8 }).catch(() => ({ items: [] })),
  ])
  if (aboutRes) {
    data.value = aboutRes
    if (Array.isArray(aboutRes.resources) && aboutRes.resources.length > 0 && aboutRes.resources[0]?.category) {
      activeResourceCat.value = aboutRes.resources[0].category
    }
  }
  if (siteRes?.siteName) {
    siteName.value = siteRes.siteName
  }
  if (newsRes?.items) {
    newsList.value = newsRes.items
  }
}

useRealtimeSync(['aboutVersion', 'siteConfigVersion', 'contentVersion'], () => {
  void reloadAll()
})

onMounted(async () => {
  syncTabFromQuery()
  document.addEventListener('keydown', onKeydown)
  await reloadAll()
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', onKeydown)
})
</script>

<template>
  <div class="about-g7">
    <!-- 1. 顶部首屏 Banner -->
    <section
      class="about-hero"
      :class="{
        'has-media-bg':
          (data?.heroBgType === 'image' && !!data?.heroBgImage) ||
          (data?.heroBgType === 'video' && !!data?.heroBgVideo),
      }"
    >
      <video
        v-if="data?.heroBgType === 'video' && data?.heroBgVideo"
        class="about-hero__media-video"
        :src="data.heroBgVideo"
        autoplay
        muted
        loop
        playsinline
      ></video>
      <div
        v-else-if="data?.heroBgType === 'image' && data?.heroBgImage"
        class="about-hero__media-image"
        :style="{ backgroundImage: `url(${data.heroBgImage})` }"
      ></div>
      <div v-else class="about-hero__trails" aria-hidden="true"></div>

      <div
        v-if="(data?.heroBgType === 'image' && !!data?.heroBgImage) || (data?.heroBgType === 'video' && !!data?.heroBgVideo)"
        class="about-hero__overlay"
        aria-hidden="true"
      ></div>

      <div class="container about-hero__inner">
        <div v-if="data?.showHeroLogoMark" v-reveal class="about-hero__logo-mark" aria-hidden="true">
          {{ (siteName || 'E').slice(0, 1) }}
        </div>
        <p v-if="data?.heroEyebrow" v-reveal class="about-hero__eyebrow">{{ data.heroEyebrow }}</p>
        <h1 v-reveal="100" class="about-hero__title">{{ heroTitle }}</h1>
        <p v-if="data?.heroSubtitle" v-reveal="140" class="about-hero__subtitle">{{ data.heroSubtitle }}</p>
      </div>
    </section>

    <!-- 2. 悬浮白底五页签导航条 -->
    <div id="part" class="container about-tabs-wrap">
      <nav v-reveal="140" class="about-tabs" aria-label="关于我们五页签导航">
        <button
          v-for="tab in TABS"
          :key="tab.key"
          type="button"
          class="about-tabs__btn"
          :class="{ 'is-active': activeTab === tab.key }"
          @click="switchTab(tab.key)"
        >
          {{ tab.label }}
        </button>
      </nav>
    </div>

    <!-- ==================== 子页面 1：企业简介 (intro) ==================== -->
    <div v-if="activeTab === 'intro'" key="subpage-intro" class="about-subpage">
      <!-- (1) 企业简介 -->
      <section v-if="data?.showIntro !== false && (introParagraphs.length > 0 || hasIntroVisual)" class="about-intro-sec">
        <div class="container about-intro-grid" :class="{ 'no-visual': !hasIntroVisual }">
          <div v-reveal class="about-intro__text">
            <h2 class="about-sec-title">{{ introTitle }}</h2>
            <p v-for="(para, pIdx) in introParagraphs" :key="pIdx" class="about-intro__para">
              {{ para }}
            </p>
          </div>

          <div v-if="hasIntroVisual" v-reveal="120" class="about-intro__visual">
            <div class="about-intro__photo">
              <img v-if="introVisualType === 'image' && data?.introImage" :src="data.introImage" :alt="introTitle" class="about-intro__img" />
              <template v-else-if="introVisualType === 'radar'">
                <div class="about-intro__radar" aria-hidden="true"></div>
                <div class="about-intro__truck-scene" aria-hidden="true">
                  <span class="scene-tag">{{ siteName }} 数字化平台</span>
                </div>
              </template>
            </div>
          </div>
        </div>
      </section>

      <!-- (1.5) 企业规模指标 (stats) -->
      <section v-if="statsList.length > 0" class="about-quals-sec" style="padding-top: 24px; padding-bottom: 48px;">
        <div class="container">
          <div class="quals-grid">
            <div v-for="(st, sIdx) in statsList" :key="st.label + sIdx" class="qual-card">
              <h3 class="qual-card__name" style="font-size: 28px; color: #2563eb; margin-bottom: 6px;">
                {{ st.displayValue || st.value }}{{ st.unit || '' }}
              </h3>
              <p class="qual-card__desc">{{ st.label }}</p>
            </div>
          </div>
        </div>
      </section>

      <!-- (2) CEO的一封信（仅当后台配置并开启时显示） -->
      <section v-if="data?.showCeoLetter !== false && ceoLetter" class="about-ceo-sec">
        <div class="container">
          <h2 v-reveal class="about-center-title">致辞与展望</h2>

          <div v-reveal="100" class="ceo-card">
            <div v-if="ceoLetter.leftQuote" class="ceo-card__left">
              <p class="ceo-card__quote">{{ ceoLetter.leftQuote }}</p>
            </div>

            <div class="ceo-card__right">
              <h3 class="ceo-card__title">{{ ceoLetter.title }}</h3>
              <div class="ceo-card__paras">
                <p v-for="(p, idx) in ceoLetter.excerpt" :key="idx">{{ p }}</p>
              </div>
              <div class="ceo-card__foot">
                <button
                  v-if="ceoLetter.fullParagraphs.length > 0"
                  type="button"
                  class="about-outline-btn"
                  @click="ceoModalOpen = true"
                >
                  {{ ceoLetter.buttonText || '展开全部' }}
                </button>
                <span v-if="ceoLetter.signature" class="ceo-card__sign">{{ ceoLetter.signature }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- (3) 发展历程（仅当后台配置并开启时显示） -->
      <section v-if="data?.showMilestones !== false && milestonesList.length" class="about-history-sec">
        <div class="container">
          <h2 v-reveal class="about-center-title">发展历程</h2>

          <div v-reveal="100" class="history-axis">
            <button type="button" class="history-arrow" aria-label="上一年份" @click="prevMilestone">‹</button>
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
            <button type="button" class="history-arrow" aria-label="下一年份" @click="nextMilestone">›</button>
          </div>

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

      <!-- (3.5) 荣誉资质与权威认证 -->
      <section v-if="data?.showQualifications !== false && data?.qualifications?.length" class="about-quals-sec">
        <div class="container">
          <h2 v-reveal class="about-center-title">荣誉与资质认证</h2>
          <div class="quals-grid">
            <div
              v-for="(qItem, qIdx) in data.qualifications"
              :key="qItem.name + qIdx"
              v-reveal="qIdx * 60"
              class="qual-card"
            >
              <img v-if="qItem.imageUrl" :src="qItem.imageUrl" :alt="qItem.name" class="qual-card__img" />
              <h3 class="qual-card__name">{{ qItem.name }}</h3>
              <p v-if="qItem.description" class="qual-card__desc">{{ qItem.description }}</p>
            </div>
          </div>
        </div>
      </section>

      <!-- (4) 服务分支网络 -->
      <section v-if="data?.showOffices !== false && officesList.length" class="about-offices-sec">
        <div class="container">
          <h2 v-reveal class="about-center-title">联系我们</h2>

          <div class="offices-layout">
            <div v-reveal="80" class="offices-scroll-box">
              <div
                v-for="(off, oIdx) in officesList"
                :key="off.city"
                class="office-item"
                :class="{ 'is-active': activeOfficeIndex === oIdx }"
                @mouseenter="activeOfficeIndex = oIdx"
                @click="activeOfficeIndex = oIdx"
              >
                <h3 class="office-item__city">{{ off.city }}</h3>
                <p v-if="!isPlaceholderContact(off.phone)" class="office-item__line">
                  <span>电话：</span>{{ off.phone }}
                </p>
                <p v-if="off.address" class="office-item__line">
                  <span>地址：</span>{{ off.address }}
                </p>
              </div>
            </div>

            <div v-reveal="140" class="offices-map">
              <div class="offices-map__canvas">
                <div
                  class="map-photo-popup"
                  :style="{
                    left: `${Math.min(76, Math.max(28, currentOffice?.mapX || 66))}%`,
                    top: `${Math.max(6, (currentOffice?.mapY || 48) - 36)}%`,
                  }"
                >
                  <div class="map-photo-popup__img">
                    <img
                      v-if="currentOffice?.image"
                      :src="currentOffice.image"
                      :alt="currentOffice.city"
                    />
                    <div v-else class="building-illustration" :class="`b-theme--${activeOfficeIndex % 4}`">
                      <div class="building-skyline"></div>
                      <span class="building-illustration__tag">{{ currentOffice?.city }}</span>
                    </div>
                  </div>
                  <span class="map-photo-popup__arrow" aria-hidden="true"></span>
                </div>

                <div class="map-pins">
                  <button
                    v-for="(off, oIdx) in officesList"
                    :key="off.city"
                    type="button"
                    class="map-pin"
                    :class="{ 'is-active': activeOfficeIndex === oIdx }"
                    :style="{ left: `${off.mapX || 65}%`, top: `${off.mapY || 42 + oIdx * 6}%` }"
                    @click="activeOfficeIndex = oIdx"
                  >
                    <span class="map-pin__dot"></span>
                    <span class="map-pin__name">{{ off.city.replace(/（.*）/, '') }}</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 若企业简介页签下所有模块均未配置 -->
      <section
        v-if="introParagraphs.length === 0 && !ceoLetter && milestonesList.length === 0 && officesList.length === 0"
        style="padding: 80px 0; text-align: center; color: #64748b;"
      >
        <div class="container">
          <p>企业简介内容正在完善中。</p>
        </div>
      </section>
    </div>

    <!-- ==================== 子页面 2：管理团队 (team) ==================== -->
    <div v-else-if="activeTab === 'team'" key="subpage-team" class="about-subpage">
      <section class="about-team-sec">
        <div class="container">
          <h2 v-reveal class="about-center-title">
            {{ data?.teamTitle || `${siteName}管理团队` }}
          </h2>

          <div v-if="teamList.length > 0" class="team-grid">
            <article
              v-for="(member, mIdx) in teamList"
              :key="member.name"
              v-reveal="mIdx * 80"
              class="team-card"
              @click="activeTeamMember = member"
            >
              <div class="team-card__portrait" :class="`portrait-bg--${mIdx % 4}`">
                <img v-if="member.avatar" :src="member.avatar" :alt="member.name" />
                <div v-else class="portrait-placeholder">
                  <div class="portrait-placeholder__head"></div>
                  <div class="portrait-placeholder__suit"></div>
                </div>
              </div>
              <div class="team-card__info">
                <h3 class="team-card__name">{{ member.name }}</h3>
                <p class="team-card__role">{{ member.role }}</p>
              </div>
            </article>
          </div>
          <p v-else style="text-align: center; color: #64748b; padding: 48px 0">
            管理团队信息暂未公开。
          </p>
        </div>
      </section>
    </div>

    <!-- ==================== 子页面 3：媒体中心 (media) ==================== -->
    <div v-else-if="activeTab === 'media'" key="subpage-media" class="about-subpage">
      <section class="about-news-sec">
        <div class="container">
          <div v-reveal class="about-news__head">
            <h2 class="about-sec-title">新闻中心</h2>
            <RouterLink to="/news" class="about-outline-btn">查看更多</RouterLink>
          </div>

          <div v-if="visibleNewsCards.length > 0" v-reveal="100" class="about-news-slider">
            <button type="button" class="slider-arrow" aria-label="上一组新闻" @click="prevNews">‹</button>

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
                    <span>{{ item.displayTime || '' }}</span>
                  </div>
                </div>
              </RouterLink>
            </div>

            <button type="button" class="slider-arrow" aria-label="下一组新闻" @click="nextNews">›</button>
          </div>
          <p v-else style="text-align: center; color: #64748b; padding: 48px 0">
            暂无已发布新闻资讯。
          </p>

          <!-- 企业故事集 (stories) -->
          <div v-if="storiesList.length > 0" style="margin-top: 56px;">
            <h2 class="about-sec-title" style="margin-bottom: 24px;">{{ data?.storiesTitle || '企业故事' }}</h2>
            <div class="about-news-grid">
              <a
                v-for="(st, sIdx) in storiesList"
                :key="st.title + sIdx"
                :href="st.url || '/news'"
                class="about-news-card"
              >
                <div class="about-news-card__poster" :class="`poster-theme--${sIdx % 4}`">
                  <img v-if="st.cover" :src="st.cover" :alt="st.title" loading="lazy" />
                  <div v-else class="poster-fallback">
                    <span class="poster-fallback__brand">{{ st.tag || siteName }}</span>
                    <strong class="poster-fallback__text">{{ st.title.slice(0, 14) }}</strong>
                  </div>
                </div>
                <div class="about-news-card__body">
                  <h3 class="about-news-card__title">{{ st.title }}</h3>
                  <div v-if="st.date" class="about-news-card__time">
                    <span>{{ st.date }}</span>
                  </div>
                </div>
              </a>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- ==================== 子页面 4：资料共享 / 资料库 (resources) ==================== -->
    <div v-else-if="activeTab === 'resources'" key="subpage-resources" class="about-subpage">
      <section class="about-res-sec">
        <div class="container">
          <h2 v-reveal class="about-center-title">{{ data?.resourcesTitle || '资料库' }}</h2>

          <template v-if="allResources.length > 0">
            <div v-reveal="80" class="res-subtabs">
              <button
                v-for="cat in resourceCategories"
                :key="cat"
                type="button"
                class="res-subtabs__btn"
                :class="{ 'is-active': activeResourceCat === cat }"
                @click="activeResourceCat = cat; resourceStartIndex = 0"
              >
                {{ cat }}
              </button>
            </div>

            <div v-reveal="140" class="about-news-slider">
              <button
                type="button"
                class="slider-arrow"
                aria-label="上一组资料"
                @click="resourceStartIndex = Math.max(0, resourceStartIndex - 1)"
              >
                ‹
              </button>

              <div class="about-news-grid">
                <article
                  v-for="(resItem, rIdx) in visibleResources"
                  :key="`${resItem.title}-${rIdx}`"
                  class="res-card"
                  @click="handleResourceClick(resItem)"
                >
                  <div class="res-card__cover" :class="{ 'is-logo-mode': resItem.category === '客户视频' }">
                    <img v-if="resItem.cover" :src="resItem.cover" :alt="resItem.title" />
                    <div
                      v-else-if="resItem.category === '客户视频'"
                      class="res-card__logo-box"
                    >
                      <strong :style="{ color: resItem.logoColor || '#1e293b' }">
                        {{ resItem.logoText || resItem.title }}
                      </strong>
                    </div>
                    <div
                      v-else
                      class="res-card__brochure"
                      :style="{ background: `linear-gradient(145deg, ${resItem.logoColor || '#2563eb'} 0%, #0f172a 100%)` }"
                    >
                      <span class="res-card__brochure-brand">{{ siteName || '企业资料' }}</span>
                      <strong>{{ resItem.logoText || resItem.title }}</strong>
                    </div>
                  </div>

                  <div class="res-card__foot">
                    <h3 class="res-card__title">{{ resItem.title }}</h3>
                    <button
                      type="button"
                      class="res-card__dl-btn"
                      :title="resItem.category === '客户视频' ? '播放视频' : '下载资料'"
                      @click.stop="handleResourceClick(resItem)"
                    >
                      ↓
                    </button>
                  </div>
                </article>
              </div>

              <button
                type="button"
                class="slider-arrow"
                aria-label="下一组资料"
                @click="resourceStartIndex = resourceStartIndex + 1"
              >
                ›
              </button>
            </div>
          </template>
          <p v-else style="text-align: center; color: #64748b; padding: 48px 0">
            暂无公开共享资料，如需获取产品白皮书或手册请联系商务顾问。
          </p>
        </div>
      </section>
    </div>

    <!-- ==================== 子页面 5：加入我们 (join) ==================== -->
    <div v-else-if="activeTab === 'join'" key="subpage-join" class="about-subpage">
      <section class="about-join-sec">
        <div class="container">
          <div v-reveal class="join-banner">
            <div class="join-banner__content">
              <h2 class="join-banner__title">{{ data?.joinTitle || '期待您的加入' }}</h2>
              <button type="button" class="join-banner__btn" @click="handleJoinClick">
                {{ data?.joinButtonText || '查看职位' }}
              </button>
            </div>
          </div>
        </div>
      </section>

      <section v-if="missionText || visionText || valuesList.length > 0" class="about-culture-sec">
        <div class="container">
          <div v-if="missionText" v-reveal class="culture-row">
            <div class="culture-row__label">使命</div>
            <div class="culture-row__content">
              <h3 class="culture-row__headline">{{ missionText }}</h3>
            </div>
          </div>

          <div v-if="visionText" v-reveal="80" class="culture-row">
            <div class="culture-row__label">愿景</div>
            <div class="culture-row__content">
              <h3 class="culture-row__headline">{{ visionText }}</h3>
            </div>
          </div>

          <div v-if="valuesList.length > 0" class="culture-row culture-row--values">
            <div v-reveal class="culture-row__label">价值观</div>
            <div class="culture-row__content culture-values-list">
              <div
                v-for="(valItem, vIdx) in valuesList"
                :key="valItem.title"
                v-reveal="vIdx * 80"
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
    </div>

    <!-- ==================== 弹窗 A：致辞全文滚动弹窗 ==================== -->
    <Teleport to="body">
      <Transition name="about-modal">
        <div
          v-if="ceoModalOpen && ceoLetter"
          class="about-modal-backdrop"
          role="dialog"
          aria-modal="true"
          @click.self="ceoModalOpen = false"
        >
          <div class="ceo-full-modal">
            <button
              type="button"
              class="about-modal-close"
              aria-label="关闭全文"
              @click="ceoModalOpen = false"
            >
              ⊗
            </button>
            <div class="ceo-full-modal__scroll">
              <h2 class="ceo-full-modal__title">{{ ceoLetter.title }}</h2>
              <p
                v-for="(para, pIdx) in ceoLetter.fullParagraphs"
                :key="pIdx"
                class="ceo-full-modal__para"
              >
                {{ para }}
              </p>
              <div v-if="ceoLetter.signature" class="ceo-full-modal__sign">{{ ceoLetter.signature }}</div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- ==================== 弹窗 B：管理团队详情弹窗 ==================== -->
    <Teleport to="body">
      <Transition name="about-modal">
        <div
          v-if="activeTeamMember"
          class="about-modal-backdrop"
          role="dialog"
          aria-modal="true"
          @click.self="activeTeamMember = null"
        >
          <div class="team-bio-modal">
            <button
              type="button"
              class="about-modal-close"
              aria-label="关闭介绍"
              @click="activeTeamMember = null"
            >
              ⊗
            </button>

            <div class="team-bio-modal__left">
              <img
                v-if="activeTeamMember.avatar"
                :src="activeTeamMember.avatar"
                :alt="activeTeamMember.name"
              />
              <div v-else class="portrait-placeholder portrait-placeholder--lg">
                <div class="portrait-placeholder__head"></div>
                <div class="portrait-placeholder__suit"></div>
              </div>
            </div>

            <div class="team-bio-modal__right">
              <h3 class="team-bio-modal__name">{{ activeTeamMember.name }}</h3>
              <p class="team-bio-modal__role">{{ activeTeamMember.role }}</p>
              <div class="team-bio-modal__divider"></div>
              <div class="team-bio-modal__paras">
                <p v-for="(bPara, bIdx) in activeTeamMember.bio" :key="bIdx">{{ bPara }}</p>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
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
  height: 390px;
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
}

.about-hero__inner {
  position: relative;
  z-index: 2;
  padding-bottom: 28px;
}

.about-hero__media-video {
  position: absolute;
  top: 50%;
  left: 50%;
  min-width: 100%;
  min-height: 100%;
  width: auto;
  height: auto;
  transform: translate(-50%, -50%);
  object-fit: cover;
  z-index: 1;
  pointer-events: none;
}

.about-hero__media-image {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  z-index: 1;
}

.about-hero__overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(8, 10, 20, 0.6) 0%, rgba(8, 10, 20, 0.82) 100%);
  z-index: 1;
  pointer-events: none;
}

.about-hero__eyebrow {
  display: inline-block;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #a5b4fc;
  margin-bottom: 12px;
}

.about-hero__subtitle {
  font-size: clamp(15px, 1.8vw, 18px);
  color: rgba(255, 255, 255, 0.85);
  max-width: 760px;
  margin: 14px auto 0;
  line-height: 1.6;
}

.about-hero__logo-mark {
  font-size: 54px;
  font-weight: 900;
  font-style: italic;
  color: #ffffff;
  line-height: 1;
  margin-bottom: 14px;
}

.about-hero__title {
  font-size: clamp(26px, 3.6vw, 40px);
  font-weight: 700;
  color: #ffffff;
  margin: 0;
}

/* 2. 悬浮白底五页签导航条 */
.about-tabs-wrap {
  position: relative;
  z-index: 10;
  margin-top: -41px;
}

.about-tabs {
  max-width: 1040px;
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
  font-family: inherit;
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

/* 3. 企业简介：左文右图 */
.about-intro-sec {
  padding: 88px 0 64px;
}

.about-intro-grid {
  max-width: 1040px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 64px;
  align-items: center;
}

.about-intro-grid.no-visual {
  grid-template-columns: 1fr;
  max-width: 860px;
}

.about-sec-title {
  font-size: 34px;
  font-weight: 700;
  color: #1f2329;
  margin: 0 0 28px;
}

.about-center-title {
  text-align: center;
  font-size: 36px;
  font-weight: 700;
  color: #1f2329;
  margin: 0 0 52px;
}

.about-intro__para {
  font-size: 14px;
  line-height: 2;
  color: #6e7380;
  margin: 0 0 16px;
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

.about-intro__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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
}

.scene-tag {
  position: absolute;
  bottom: 20px;
  right: 20px;
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(8px);
  color: #ffffff;
  font-size: 12.5px;
}

/* 4. CEO的一封信 */
.about-ceo-sec {
  padding: 40px 0 88px;
}

.ceo-card {
  max-width: 1040px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 38% 62%;
  min-height: 410px;
  box-shadow: 0 16px 44px rgba(16, 24, 40, 0.06);
}

.ceo-card__left {
  background:
    linear-gradient(180deg, rgba(15, 23, 42, 0.35) 0%, rgba(15, 23, 42, 0.85) 100%),
    radial-gradient(circle at 30% 40%, #334155 0%, #0f172a 100%);
  padding: 48px 36px;
  display: flex;
  align-items: flex-end;
  color: #ffffff;
}

.ceo-card__quote {
  font-size: 16px;
  font-weight: 600;
  line-height: 1.85;
  margin: 0;
}

.ceo-card__right {
  position: relative;
  background: linear-gradient(135deg, #f8fafc 0%, #eef2ff 100%);
  padding: 48px 52px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.ceo-card__watermark {
  position: absolute;
  right: 28px;
  bottom: -20px;
  font-size: 220px;
  font-weight: 900;
  font-style: italic;
  color: rgba(99, 102, 241, 0.06);
  pointer-events: none;
}

.ceo-card__title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2329;
  margin: 0 0 20px;
}

.ceo-card__paras p {
  font-size: 13.5px;
  line-height: 1.95;
  color: #5f6470;
  margin: 0 0 14px;
}

.ceo-card__foot {
  margin-top: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.about-outline-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 38px;
  padding: 0 28px;
  border: 1px solid #1f2329;
  background: transparent;
  color: #1f2329;
  font-size: 13px;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.about-outline-btn:hover {
  background: #1f2329;
  color: #ffffff;
}

.ceo-card__sign {
  font-size: 26px;
  font-family: 'STKaiti', 'KaiTi', cursive, serif;
  color: #1f2329;
  letter-spacing: 0.08em;
}

/* 5. 发展历程 */
.about-history-sec {
  padding: 80px 0 100px;
  background: #fcfcfd;
}

.history-axis {
  max-width: 920px;
  margin: 0 auto 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.history-arrow,
.slider-arrow {
  width: 36px;
  height: 36px;
  border: none;
  background: none;
  font-size: 34px;
  color: #8b90a0;
  cursor: pointer;
  line-height: 1;
}

.history-arrow:hover,
.slider-arrow:hover {
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
  transition: all 0.25s ease;
}

.history-year-btn.is-center {
  font-size: 28px;
  color: #1f2329;
}

.history-stage {
  max-width: 1040px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 28px;
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

/* 5.5 荣誉资质认证墙（开发文档 6.11） */
.about-quals-sec {
  padding: 72px 0 88px;
  background: #ffffff;
}

.quals-grid {
  max-width: 1040px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.qual-card {
  padding: 24px 20px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  text-align: center;
  background: #fafbfe;
}

.qual-card__img {
  max-height: 110px;
  max-width: 100%;
  object-fit: contain;
  margin-bottom: 12px;
}

.qual-card__name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2329;
  margin: 0 0 6px;
}

.qual-card__desc {
  font-size: 12.5px;
  color: #6e7380;
  margin: 0;
}

/* 6. 联系我们 7 城内部滚动容器 + 右侧地图 */
.about-offices-sec {
  padding: 84px 0 110px;
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

.offices-scroll-box {
  max-height: 430px;
  overflow-y: auto;
  padding-right: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.offices-scroll-box::-webkit-scrollbar {
  width: 3px;
}

.offices-scroll-box::-webkit-scrollbar-track {
  background: #e2e6ee;
}

.offices-scroll-box::-webkit-scrollbar-thumb {
  background: #4b5563;
}

.office-item {
  padding: 18px 24px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.22s ease;
}

.office-item.is-active {
  background: #ffffff;
  box-shadow: 0 10px 28px rgba(16, 24, 40, 0.05);
}

.office-item__city {
  font-size: 17px;
  font-weight: 700;
  color: #2b2f38;
  margin: 0 0 8px;
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
  height: 410px;
  background-image: radial-gradient(#cbd5e1 1.25px, transparent 1.25px);
  background-size: 12px 12px;
  border-radius: 12px;
}

.map-photo-popup {
  position: absolute;
  transform: translateX(-50%);
  width: 240px;
  height: 136px;
  border: 2px solid #2b2f38;
  border-radius: 4px;
  background: #ffffff;
  box-shadow: 0 16px 36px rgba(16, 24, 40, 0.14);
  z-index: 5;
  transition: left 0.3s ease, top 0.3s ease;
}

.map-photo-popup__img {
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.map-photo-popup__img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.building-illustration {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: flex-end;
  padding: 12px;
  background: linear-gradient(160deg, #38bdf8 0%, #2563eb 52%, #1e293b 100%);
}

.b-theme--1 {
  background: linear-gradient(160deg, #6366f1 0%, #4338ca 52%, #0f172a 100%);
}

.b-theme--2 {
  background: linear-gradient(160deg, #0d9488 0%, #0f766e 52%, #0f172a 100%);
}

.b-theme--3 {
  background: linear-gradient(160deg, #0284c7 0%, #1e40af 52%, #0f172a 100%);
}

.building-illustration__tag {
  padding: 3px 10px;
  border-radius: 3px;
  background: rgba(15, 23, 42, 0.72);
  color: #ffffff;
  font-size: 11.5px;
}

.map-photo-popup__arrow {
  position: absolute;
  bottom: -8px;
  left: 50%;
  width: 12px;
  height: 12px;
  background: #2b2f38;
  transform: translateX(-50%) rotate(45deg);
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
  width: 11px;
  height: 11px;
  border-color: #101014;
}

/* 7. 管理团队 4 列高管网格 */
.about-team-sec {
  padding: 84px 0 110px;
}

.team-grid {
  max-width: 1040px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 24px;
}

.team-card {
  background: #f5f7fa;
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.team-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 18px 40px rgba(16, 24, 40, 0.1);
}

.team-card__portrait {
  aspect-ratio: 4 / 4.6;
  background: #2b2f36;
  overflow: hidden;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.team-card__portrait img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.portrait-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
}

.portrait-placeholder__head {
  width: 68px;
  height: 82px;
  border-radius: 50%;
  background: linear-gradient(180deg, #e2e8f0 0%, #94a3b8 100%);
  margin-bottom: -10px;
  z-index: 2;
}

.portrait-placeholder__suit {
  width: 168px;
  height: 120px;
  border-radius: 72px 72px 0 0;
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
}

.team-card__info {
  padding: 24px 16px 28px;
  text-align: center;
}

.team-card__name {
  font-size: 20px;
  font-weight: 700;
  color: #1f2329;
  margin: 0 0 8px;
}

.team-card__role {
  font-size: 13px;
  color: #8b90a0;
  margin: 0;
}

/* 8. 媒体中心 & 资料共享 */
.about-news-sec,
.about-res-sec {
  padding: 84px 0 110px;
  background: #f5f7fb;
}

.about-news__head {
  max-width: 1040px;
  margin: 0 auto 36px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.about-news__head .about-sec-title {
  margin-bottom: 0;
}

.about-news-slider {
  max-width: 1140px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 16px;
}

.about-news-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.about-news-card,
.res-card {
  background: #ffffff;
  display: flex;
  flex-direction: column;
  text-decoration: none;
  color: inherit;
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.about-news-card:hover,
.res-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 16px 36px rgba(16, 24, 40, 0.08);
}

.about-news-card__poster {
  aspect-ratio: 16 / 10;
  overflow: hidden;
  position: relative;
  background: #0f172a;
}

.about-news-card__poster img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.4s ease;
}

.about-news-card:hover .about-news-card__poster img {
  transform: scale(1.05);
}

.res-card__cover {
  aspect-ratio: 1 / 1;
  overflow: hidden;
  position: relative;
}

.poster-fallback,
.res-card__brochure {
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
  margin: 0;
}

.about-news-card__time {
  margin-top: 14px;
  font-size: 12px;
  color: #98a2b3;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 资料库子标签与卡片 */
.res-subtabs {
  display: flex;
  justify-content: center;
  gap: 48px;
  margin-bottom: 44px;
}

.res-subtabs__btn {
  position: relative;
  border: none;
  background: none;
  padding: 8px 4px 14px;
  font-size: 15px;
  color: #5f6470;
  font-family: inherit;
  cursor: pointer;
}

.res-subtabs__btn.is-active {
  color: #5b4be8;
  font-weight: 600;
}

.res-subtabs__btn.is-active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 3px;
  background: #5b4be8;
}

.res-card__logo-box {
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, #eef2f6 0%, #ffffff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  text-align: center;
  font-size: 22px;
}

.res-card__brochure-brand {
  font-size: 12px;
  font-weight: 700;
  opacity: 0.85;
}

.res-card__foot {
  padding: 18px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.res-card__title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2329;
  margin: 0;
}

.res-card__dl-btn {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 1px solid #2b2f38;
  background: #ffffff;
  color: #2b2f38;
  font-size: 13px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.res-card:hover .res-card__dl-btn {
  background: #5b4be8;
  border-color: #5b4be8;
  color: #ffffff;
}

/* 9. 加入我们 + 使命/愿景/价值观 */
.about-join-sec {
  padding: 72px 0 36px;
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
  margin: 0 0 24px;
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

.about-culture-sec {
  padding: 24px 0 96px;
}

.culture-row {
  max-width: 1040px;
  margin: 0 auto;
  padding: 56px 0;
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
  margin: 0;
}

.culture-values-list {
  display: flex;
  flex-direction: column;
  gap: 48px;
}

.value-block__title {
  font-size: 26px;
  font-weight: 700;
  color: #2b2f38;
  margin: 0 0 14px;
}

.value-block__line {
  font-size: 14px;
  line-height: 2;
  color: #6e7380;
  margin: 2px 0;
}

/* ==================== 弹窗样式（CEO信全文 & 高管详情） ==================== */
.about-modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 95;
  background: rgba(0, 0, 0, 0.56);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.about-modal-close {
  position: absolute;
  top: 20px;
  right: 22px;
  border: none;
  background: none;
  font-size: 24px;
  color: #5f6470;
  cursor: pointer;
  line-height: 1;
}

.about-modal-close:hover {
  color: #101014;
}

.ceo-full-modal {
  position: relative;
  width: 100%;
  max-width: 920px;
  max-height: 82vh;
  background: #ffffff;
  padding: 52px 56px 48px;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.25);
  display: flex;
  flex-direction: column;
}

.ceo-full-modal__scroll {
  overflow-y: auto;
  padding-right: 24px;
}

.ceo-full-modal__title {
  font-size: 30px;
  font-weight: 700;
  color: #1f2329;
  margin: 0 0 28px;
}

.ceo-full-modal__para {
  font-size: 14.5px;
  line-height: 2;
  color: #4b5563;
  margin: 0 0 18px;
}

.ceo-full-modal__sign {
  margin-top: 28px;
  text-align: right;
  font-size: 30px;
  font-family: 'STKaiti', 'KaiTi', cursive, serif;
}

.team-bio-modal {
  position: relative;
  width: 100%;
  max-width: 840px;
  min-height: 410px;
  background: #ffffff;
  display: grid;
  grid-template-columns: 38% 62%;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.28);
  overflow: hidden;
}

.team-bio-modal__left {
  background: #2b2f36;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.team-bio-modal__left img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.team-bio-modal__right {
  padding: 48px 44px;
}

.team-bio-modal__name {
  font-size: 28px;
  font-weight: 700;
  color: #1f2329;
  margin: 0 0 8px;
}

.team-bio-modal__role {
  font-size: 15px;
  color: #5f6470;
  margin: 0;
}

.team-bio-modal__divider {
  margin: 22px 0;
  border-bottom: 1px solid #eceef2;
}

.team-bio-modal__paras p {
  font-size: 13.5px;
  line-height: 1.95;
  color: #5f6470;
  margin: 0 0 12px;
}

@media (max-width: 920px) {
  .about-tabs {
    padding: 0 20px;
    gap: 20px;
    overflow-x: auto;
  }
  .about-intro-grid,
  .ceo-card,
  .culture-row,
  .history-stage,
  .offices-layout,
  .team-bio-modal {
    grid-template-columns: 1fr;
  }
  .team-grid,
  .about-news-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
