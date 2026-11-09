<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ConfigPanel from '@/components/ConfigPanel.vue'
import LinkEditor, { type LinkValue } from '@/components/LinkEditor.vue'
import MediaPicker from '@/components/MediaPicker.vue'
import { useConfig } from '@/composables/useConfig'
import type { MediaItem } from '@/api/content'

interface HeroItem {
  key?: string
  enabled: boolean
  sortOrder: number
  title: string
  subtitle: string
  theme?: 'dark' | 'light'
  titleColor?: string
  subtitleColor?: string
  kickerColor?: string
  buttonBgColor?: string
  buttonTextColor?: string
  kicker?: string
  badgeText?: string
  watermark?: string
  desktopMediaId: number | null
  mobileMediaId: number | null
  imageAlt: string
  buttonText: string
  link: LinkValue | null
}

interface MetricItem {
  key?: string
  sortOrder: number
  displayValue: string
  unit: string
  label: string
  description: string
}

interface HotspotItem {
  label: string
  path?: string
  x: number
  y: number
  labelX: number
  labelY: number
  align?: 'left' | 'right'
}

interface ShowcaseSubItem {
  title: string
  prefix?: string
  path?: string
  calloutsText?: string
}

interface ShowcaseStageItem {
  key: string
  title: string
  description: string
  morePath: string
  mode: 'RADAR' | 'PAYMENT' | 'SOLUTIONS'
  alertLabel?: string
  alertBaseValue?: number
  alertUnit?: string
  paymentTitle?: string
  paymentSubtitle?: string
  paymentAmount?: string
  subItems: ShowcaseSubItem[]
}

interface ServiceGroup {
  key?: string
  sortOrder: number
  title: string
  description: string
  imageMediaId: number | null
  contentType: 'PRODUCT' | 'SOLUTION'
  contentIds: number[]
  moreLink: LinkValue | null
}

const DEFAULT_ORDER = ['metrics', 'services', 'cases', 'customers', 'news', 'consultation']

const router = useRouter()
const activeTab = ref('hero')
const pickerOpen = ref(false)
const pickerKind = ref<'IMAGE' | 'VIDEO' | 'ALL'>('ALL')
let pickerCallback: ((media: MediaItem) => void) | null = null

const { view, loading, saving, publishing, loadError, load, save, publish } =
  useConfig<Record<string, any>>('HOME')

const form = reactive({
  sectionOrder: [...DEFAULT_ORDER],
  hero: { items: [] as HeroItem[] },
  metrics: {
    enabled: true,
    brandPrefix: 'G',
    title: '产品展示',
    subtitle: '',
    items: [] as MetricItem[],
    hotspots: [] as HotspotItem[],
  },
  showcaseStages: [] as ShowcaseStageItem[],
  services: { enabled: true, title: '核心产品与行业方案', subtitle: '', groups: [] as ServiceGroup[] },
  cases: { enabled: true, title: '客户案例', subtitle: '', contentIds: [] as number[] },
  customers: { enabled: true, title: '合作客户', subtitle: '', contentIds: [] as number[] },
  news: { enabled: true, title: '最新动态', subtitle: '', mode: 'LATEST' as 'SELECTED' | 'LATEST', limit: 3, contentIds: [] as number[] },
  consultation: {
    title: '了解适合您的数字化服务',
    subtitle: '',
    buttonText: '立即咨询',
    link: { type: 'CONSULT' } as LinkValue | null,
  },
})

const DEFAULT_HOTSPOTS: HotspotItem[] = [
  { label: '安全驾驶系统', path: '/products/1', x: 525, y: 175, labelX: 390, labelY: 165, align: 'right' },
  { label: 'AI 车挂匹配', path: '/products?category=IoT', x: 585, y: 195, labelX: 405, labelY: 205, align: 'right' },
  { label: '数字加注', path: '/solutions?category=交易', x: 565, y: 245, labelX: 410, labelY: 245, align: 'right' },
  { label: '油耗管理', path: '/products?category=IoT', x: 590, y: 260, labelX: 590, labelY: 75, align: 'left' },
  { label: 'AI 量方', path: '/products/1', x: 645, y: 170, labelX: 645, labelY: 95, align: 'left' },
  { label: '实时温控', path: '/products?category=冷链', x: 695, y: 160, labelX: 695, labelY: 60, align: 'left' },
  { label: '胎温胎压', path: '/products?category=安全', x: 748, y: 255, labelX: 748, labelY: 82, align: 'left' },
  { label: '轴称重', path: '/products?category=IoT', x: 785, y: 255, labelX: 785, labelY: 98, align: 'left' },
  { label: '货舱摄像头', path: '/products?category=安全', x: 842, y: 155, labelX: 842, labelY: 65, align: 'left' },
  { label: '挂车电子制动系统', path: '/products?category=安全', x: 760, y: 268, labelX: 930, labelY: 220, align: 'left' },
  { label: '倒车盲点检测', path: '/products/1', x: 852, y: 265, labelX: 930, labelY: 265, align: 'left' },
]

function fill() {
  const draft = view.value?.draft
  if (!draft) return

  form.hero.items = (draft.hero?.items ?? []).map((item: any, idx: number) => ({
    key: item.key,
    enabled: item.enabled ?? true,
    sortOrder: item.sortOrder ?? (idx + 1) * 10,
    title: item.title ?? '',
    subtitle: item.subtitle ?? '',
    theme: item.theme === 'light' ? 'light' : 'dark',
    titleColor: item.titleColor ?? '',
    subtitleColor: item.subtitleColor ?? '',
    kickerColor: item.kickerColor ?? '',
    buttonBgColor: item.buttonBgColor ?? '',
    buttonTextColor: item.buttonTextColor ?? '',
    kicker: item.kicker ?? '',
    badgeText: item.badgeText ?? '',
    watermark: item.watermark ?? '',
    desktopMediaId: item.desktopMediaId != null ? Number(item.desktopMediaId) : null,
    mobileMediaId: item.mobileMediaId != null ? Number(item.mobileMediaId) : null,
    imageAlt: item.imageAlt ?? '',
    buttonText: item.buttonText ?? '了解详情',
    link: (item.link as LinkValue | null) ?? { type: 'CONSULT' },
  }))

  if (form.hero.items.length === 0) {
    form.hero.items = [
      {
        enabled: true,
        sortOrder: 10,
        title: '紫宝盒',
        subtitle: '物流行业首款全场景AI工具',
        theme: 'dark',
        watermark: '紫宝盒',
        kicker: '',
        badgeText: '',
        desktopMediaId: null,
        mobileMediaId: null,
        imageAlt: '',
        buttonText: '了解详情',
        link: { type: 'PAGE', pageCode: 'PRODUCTS' },
      },
      {
        enabled: true,
        sortOrder: 20,
        title: '新能源车队管理白皮书',
        subtitle: '迈向数据驱动的「成本、效率、安全」的精细化运营新时代',
        theme: 'light',
        kicker: '徐浪 G7易流安全专家、金牌讲师',
        badgeText: '免费下载',
        watermark: '',
        desktopMediaId: null,
        mobileMediaId: null,
        imageAlt: '',
        buttonText: '了解详情',
        link: { type: 'CONSULT' },
      },
      {
        enabled: true,
        sortOrder: 30,
        title: '以物联网技术和软件服务改变公路货运',
        subtitle: '公路货运行业物联网SaaS公司',
        theme: 'dark',
        kicker: '',
        badgeText: '',
        watermark: '',
        desktopMediaId: null,
        mobileMediaId: null,
        imageAlt: '',
        buttonText: '了解详情',
        link: { type: 'PAGE', pageCode: 'ABOUT' },
      },
    ]
  }

  form.metrics.enabled = draft.metrics?.enabled ?? true
  form.metrics.brandPrefix = draft.metrics?.brandPrefix ?? 'G'
  form.metrics.title = draft.metrics?.title || '产品展示'
  form.metrics.subtitle = draft.metrics?.subtitle ?? ''
  form.metrics.items = (draft.metrics?.items ?? []).map((m: any, idx: number) => ({
    key: m.key,
    sortOrder: m.sortOrder ?? (idx + 1) * 10,
    displayValue: m.displayValue ?? '',
    unit: m.unit ?? '+',
    label: m.label ?? '',
    description: m.description ?? '',
  }))
  if (form.metrics.items.length === 0) {
    form.metrics.items = [
      { sortOrder: 10, displayValue: '800W', unit: '+', label: 'IoT设备连接', description: '' },
      { sortOrder: 20, displayValue: '12000', unit: '+', label: '油站连接', description: '' },
      { sortOrder: 30, displayValue: '18W', unit: '+', label: '安全设备', description: '' },
      { sortOrder: 40, displayValue: '14000', unit: '+', label: '数字货舱', description: '' },
      { sortOrder: 50, displayValue: '35000', unit: '+', label: '货运经营者', description: '' },
      { sortOrder: 60, displayValue: '1.44亿公里', unit: '+', label: '日均跟踪里程', description: '' },
      { sortOrder: 70, displayValue: '1亿', unit: '+', label: '日均车辆事件', description: '' },
      { sortOrder: 80, displayValue: '12T', unit: '+', label: '日均上传数据', description: '' },
    ]
  }

  form.metrics.hotspots =
    Array.isArray(draft.metrics?.hotspots) && draft.metrics.hotspots.length > 0
      ? draft.metrics.hotspots.map((h: any, idx: number) => ({
          ...(DEFAULT_HOTSPOTS[idx] || {}),
          ...h,
          path: h.path || DEFAULT_HOTSPOTS[idx]?.path || '/products',
        }))
      : DEFAULT_HOTSPOTS.map((h) => ({ ...h }))

  if (Array.isArray(draft.showcaseStages) && draft.showcaseStages.length > 0) {
    form.showcaseStages = draft.showcaseStages.map((st: any) => ({
      key: st.key ?? 'iot',
      title: st.title ?? '',
      description: st.description ?? '',
      morePath: st.morePath ?? '/products',
      mode: st.mode ?? 'RADAR',
      alertLabel: st.alertLabel ?? '车距过近',
      alertBaseValue: st.alertBaseValue ?? 153,
      alertUnit: st.alertUnit ?? 'm',
      paymentTitle: st.paymentTitle ?? '扫码加油',
      paymentSubtitle: st.paymentSubtitle ?? '订单支付',
      paymentAmount: st.paymentAmount ?? '¥202',
      subItems: (st.subItems ?? []).map((sub: any) => ({
        title: sub.title ?? '',
        prefix: sub.prefix ?? 'G',
        path: sub.path ?? '/products',
        calloutsText: Array.isArray(sub.callouts)
          ? sub.callouts.map((c: any) => c.label).join(' / ')
          : '',
      })),
    }))
  } else {
    form.showcaseStages = [
      {
        key: 'iot',
        title: 'IoT服务',
        description: '基于物联网感知技术，打造软硬一体化的智能管车服务，全面打通车辆、司机、货物的全链路数字化管理。',
        morePath: '/products',
        mode: 'RADAR',
        alertLabel: '车距过近',
        alertBaseValue: 153,
        alertUnit: 'm',
        subItems: [
          { prefix: 'G', title: '安全管家', path: '/products?category=安全' },
          { prefix: 'G', title: '冷链管家', path: '/products?category=冷链' },
          { prefix: 'G', title: '流向管家', path: '/products?category=流向' },
          { prefix: 'G', title: '追货管家', path: '/products?category=追货' },
        ],
      },
      {
        key: 'trade',
        title: '交易服务',
        description: '以物联网与大数据技术重构物流交易环节，围绕能源补给、运力结算、数字货舱与物联保险。',
        morePath: '/solutions',
        mode: 'PAYMENT',
        paymentTitle: '扫码加油',
        paymentSubtitle: '订单支付',
        paymentAmount: '¥202',
        subItems: [
          { prefix: 'G', title: '财运通', path: '/solutions?category=交易' },
          { prefix: 'G', title: '数字货舱', path: '/products' },
          { prefix: 'G', title: '物联保险', path: '/solutions' },
          { prefix: 'G', title: '数字能源', path: '/solutions?category=能源' },
        ],
      },
      {
        key: 'industry',
        title: '产业物流数字化',
        description: '基于物联网与大数据技术，重塑传统行业物流运输模式，打造贯穿全链条端到端的数字化解决方案。',
        morePath: '/solutions',
        mode: 'SOLUTIONS',
        subItems: [
          { title: '履约可视', path: '/solutions/1', calloutsText: 'RFID读头 / RFID标签 / RFID标签 / 挂车专用主机' },
          { title: '货运安全', path: '/solutions/2', calloutsText: 'AI双目防碰撞 / DMS疲劳监测 / 盲区生命探测 / 7×24安全托管' },
          { title: '运力降本', path: '/solutions/3', calloutsText: '智能配载算法 / 数字加注防滴漏 / ETC路径寻优 / 自动对账结算' },
          { title: '新能源减碳', path: '/solutions/1', calloutsText: '智能充换电调度 / 电池SOH热管理 / 单车碳足迹台账 / 绿电干线组网' },
        ],
      },
    ]
  }

  form.services = {
    enabled: draft.services?.enabled ?? true,
    title: draft.services?.title ?? '核心产品与行业方案',
    subtitle: draft.services?.subtitle ?? '',
    groups: (draft.services?.groups ?? []).map((g: any, idx: number) => ({
      key: g.key,
      sortOrder: g.sortOrder ?? (idx + 1) * 10,
      title: g.title ?? '',
      description: g.description ?? '',
      imageMediaId: g.imageMediaId != null ? Number(g.imageMediaId) : null,
      contentType: g.contentType === 'SOLUTION' ? 'SOLUTION' : 'PRODUCT',
      contentIds: (g.contentIds ?? []).map((id: any) => Number(id)),
      moreLink: (g.moreLink as LinkValue | null) ?? { type: 'PAGE', pageCode: 'PRODUCTS' },
    })),
  }

  form.cases = {
    enabled: draft.cases?.enabled ?? true,
    title: draft.cases?.title ?? '客户案例',
    subtitle: draft.cases?.subtitle ?? '',
    contentIds: (draft.cases?.contentIds ?? []).map((id: any) => Number(id)),
  }

  form.customers = {
    enabled: draft.customers?.enabled ?? true,
    title: draft.customers?.title ?? '合作客户',
    subtitle: draft.customers?.subtitle ?? '',
    contentIds: (draft.customers?.contentIds ?? []).map((id: any) => Number(id)),
  }

  form.news = {
    enabled: draft.news?.enabled ?? true,
    title: draft.news?.title ?? '最新动态',
    subtitle: draft.news?.subtitle ?? '',
    mode: draft.news?.mode === 'LATEST' ? 'LATEST' : 'SELECTED',
    limit: draft.news?.limit ?? 3,
    contentIds: (draft.news?.contentIds ?? []).map((id: any) => Number(id)),
  }

  form.consultation = {
    title: draft.consultation?.title ?? '了解适合您的数字化服务',
    subtitle: draft.consultation?.subtitle ?? '',
    buttonText: draft.consultation?.buttonText ?? '立即咨询',
    link: (draft.consultation?.link as LinkValue | null) ?? { type: 'CONSULT' },
  }
}

function openMedia(kind: 'IMAGE' | 'VIDEO' | 'ALL', cb: (m: MediaItem) => void) {
  pickerKind.value = kind
  pickerCallback = cb
  pickerOpen.value = true
}

function onMediaPicked(media: MediaItem) {
  if (pickerCallback) {
    pickerCallback(media)
    pickerCallback = null
  }
  pickerOpen.value = false
}

function addHero() {
  if (form.hero.items.length >= 5) {
    ElMessage.warning('首屏轮播最多 5 条')
    return
  }
  form.hero.items.push({
    enabled: true,
    sortOrder: (form.hero.items.length + 1) * 10,
    title: '新首屏标题',
    subtitle: '首屏副标题说明',
    theme: 'dark',
    titleColor: '#FFFFFF',
    subtitleColor: 'rgba(255, 255, 255, 0.92)',
    kickerColor: '',
    buttonBgColor: '#FFFFFF',
    buttonTextColor: '#101014',
    kicker: '',
    badgeText: '',
    watermark: '',
    desktopMediaId: null,
    mobileMediaId: null,
    imageAlt: '',
    buttonText: '了解详情',
    link: { type: 'CONSULT' },
  })
}

function addMetric() {
  if (form.metrics.items.length >= 8) {
    ElMessage.warning('关键指标最多 8 条')
    return
  }
  form.metrics.items.push({
    sortOrder: (form.metrics.items.length + 1) * 10,
    displayValue: '100W',
    unit: '+',
    label: '新指标名称',
    description: '',
  })
}

const DEFAULT_CALLOUT_COORDS = [
  { x: 24, y: 18 },
  { x: 74, y: 31 },
  { x: 64, y: 46 },
  { x: 26, y: 74 },
]

async function onSave() {
  const payload = JSON.parse(JSON.stringify(form))
  payload.showcaseStages = form.showcaseStages.map((st) => ({
    ...st,
    subItems: st.subItems.map((sub) => ({
      title: sub.title,
      prefix: sub.prefix,
      path: sub.path,
      callouts: sub.calloutsText
        ? sub.calloutsText
            .split('/')
            .map((s) => s.trim())
            .filter(Boolean)
            .map((label, idx) => ({
              label,
              x: DEFAULT_CALLOUT_COORDS[idx % 4].x,
              y: DEFAULT_CALLOUT_COORDS[idx % 4].y,
            }))
        : undefined,
    })),
  }))
  if (await save(payload)) fill()
}

async function onPublish() {
  if (await publish()) fill()
}

onMounted(async () => {
  await load()
  fill()
})
</script>

<template>
  <ConfigPanel
    title="首页管理（含 3屏深浅主题轮播、G大数据 8指标与折线引线、三圆点纵向切换舞台）"
    :status="view?.status"
    :draft-version="view?.draftVersion"
    :published-version="view?.publishedVersion"
    :has-unpublished-changes="view?.hasUnpublishedChanges"
    :loading="loading"
    :saving="saving"
    :publishing="publishing"
    @save="onSave"
    @publish="onPublish"
  >
    <div class="studio-banner">
      <div>
        <strong class="studio-banner__title">🎨 想要更直观地边看边改？试试「全站实景交互预演工作台」</strong>
        <p class="studio-banner__desc">
          支持在真实官网画布上直接调整模块、修改文字、替换图片/视频、绑定链接，修改结果与当前表单 100% 双向实时互通。
        </p>
      </div>
      <el-button type="primary" @click="router.push('/preview/home')">
        进入实景交互预演台 →
      </el-button>
    </div>

    <el-alert v-if="loadError" type="error" :closable="false" :title="loadError" class="err" />

    <el-tabs v-model="activeTab" class="home-tabs">
      <!-- Tab 1: 首屏 Hero -->
      <el-tab-pane label="1. 首屏 3 屏轮播 (Hero 深/浅主题)" name="hero">
        <p class="tab-hint">
          支持每屏独立选择「深色背景/白色文字」或「浅色背景/渐变文字（如白皮书屏）」，未选图片时自动使用高还原度内置 3D 舞台背景。
        </p>
        <div v-for="(slide, idx) in form.hero.items" :key="slide.key ?? `hero-${idx}`" class="block-card">
          <div class="block-card__head">
            <strong>首屏 #{{ idx + 1 }}</strong>
            <el-switch v-model="slide.enabled" active-text="启用" />
            <el-button link type="danger" @click="form.hero.items.splice(idx, 1)">删除</el-button>
          </div>
          <el-form label-width="120px">
            <el-form-item label="文字与背景主题">
              <el-radio-group v-model="slide.theme">
                <el-radio value="dark">深色背景（白字 + 浅灰实心按钮）</el-radio>
                <el-radio value="light">浅色背景（蓝青渐变大标题 + 讲师署名）</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="文字颜色与样式">
              <div class="color-pickers-row">
                <div class="color-picker-item">
                  <span class="color-label">标题颜色</span>
                  <el-color-picker v-model="slide.titleColor" />
                  <el-input v-model="slide.titleColor" size="small" style="width: 110px" placeholder="#FFFFFF" />
                </div>
                <div class="color-picker-item">
                  <span class="color-label">副标题颜色</span>
                  <el-color-picker v-model="slide.subtitleColor" />
                  <el-input v-model="slide.subtitleColor" size="small" style="width: 130px" placeholder="#E2E8F0" />
                </div>
                <div class="color-picker-item">
                  <span class="color-label">按钮背景</span>
                  <el-color-picker v-model="slide.buttonBgColor" />
                  <el-input v-model="slide.buttonBgColor" size="small" style="width: 110px" placeholder="#FFFFFF" />
                </div>
                <div class="color-picker-item">
                  <span class="color-label">按钮文字</span>
                  <el-color-picker v-model="slide.buttonTextColor" />
                  <el-input v-model="slide.buttonTextColor" size="small" style="width: 110px" placeholder="#101014" />
                </div>
              </div>
            </el-form-item>
            <el-form-item label="主标题" required>
              <el-input v-model="slide.title" maxlength="40" show-word-limit />
            </el-form-item>
            <el-form-item label="副标题">
              <el-input v-model="slide.subtitle" type="textarea" :rows="2" maxlength="100" show-word-limit />
            </el-form-item>
            <el-form-item label="讲师/署名行(选填)">
              <el-input v-model="slide.kicker" maxlength="60" placeholder="例如：徐浪 G7易流安全专家、金牌讲师" />
            </el-form-item>
            <el-form-item label="大字号提示(选填)">
              <el-input v-model="slide.badgeText" maxlength="20" style="width: 220px" placeholder="例如：免费下载" />
            </el-form-item>
            <el-form-item label="背景大字水印">
              <el-input v-model="slide.watermark" maxlength="20" style="width: 220px" placeholder="例如：紫宝盒" />
            </el-form-item>
            <el-form-item label="桌面图片/视频">
              <div class="media-pick-row">
                <el-tag v-if="slide.desktopMediaId" type="success">素材 ID: #{{ slide.desktopMediaId }}</el-tag>
                <span v-else class="muted">未选择时默认渲染内置高还原 3D 视觉舞台</span>
                <el-button size="small" @click="openMedia('ALL', (m) => (slide.desktopMediaId = Number(m.id)))">
                  选择素材
                </el-button>
                <el-button v-if="slide.desktopMediaId" size="small" link type="danger" @click="slide.desktopMediaId = null">
                  清除
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="主按钮文案">
              <el-input v-model="slide.buttonText" maxlength="12" style="width: 220px" />
            </el-form-item>
            <el-form-item label="按钮跳转动作">
              <LinkEditor v-model="slide.link" :allow-consult="true" />
            </el-form-item>
          </el-form>
        </div>
        <el-button @click="addHero">+ 添加首屏轮播项</el-button>
      </el-tab-pane>

      <!-- Tab 2: 产品展示与核心节点 (产品展示 + 11大产品跳转链接) -->
      <el-tab-pane label="2. 产品展示与核心节点 (产品展示 + 11大产品跳转链接)" name="metrics">
        <el-form label-width="120px">
          <el-form-item label="品牌前缀斜体字">
            <el-input v-model="form.metrics.brandPrefix" maxlength="10" style="width: 120px" placeholder="可留空，如 G" />
          </el-form-item>
          <el-form-item label="模块主标题">
            <el-input v-model="form.metrics.title" maxlength="40" style="width: 260px" placeholder="产品展示" />
          </el-form-item>
          <el-form-item label="模块导语说明">
            <el-input v-model="form.metrics.subtitle" maxlength="80" style="width: 420px" placeholder="全场景软硬件一体化产品矩阵，点击节点可直达详情" />
          </el-form-item>
        </el-form>

        <h3 class="sub-h">8 项核心指标（右上角上标 +，轮流紫色高亮）</h3>
        <div v-for="(m, idx) in form.metrics.items" :key="m.key ?? `m-${idx}`" class="metric-row">
          <el-input v-model="m.displayValue" maxlength="16" placeholder="数值(如 800W)" style="width: 140px" />
          <el-input v-model="m.unit" maxlength="8" placeholder="上标(+)" style="width: 80px" />
          <el-input v-model="m.label" maxlength="20" placeholder="指标名称(如 服务企业)" style="width: 220px" />
          <el-button link type="danger" @click="form.metrics.items.splice(idx, 1)">删除</el-button>
        </div>
        <el-button size="small" @click="addMetric">+ 添加指标（建议保持 8 项）</el-button>

        <el-divider />
        <h3 class="sub-h">产品透视架构图：11 个产品折线引线节点（支持修改产品名称与点击跳转详细界面的链接）</h3>
        <p class="tab-hint" style="margin-top: -6px; margin-bottom: 14px">
          访客在前台点击对应引线标签时，将直接跳转到您填写的详细页面（如 <code>/products/1</code> 或 <code>/products?category=安全</code>）。
        </p>
        <div class="hotspot-grid">
          <div v-for="(hs, hIdx) in form.metrics.hotspots" :key="hIdx" class="hotspot-chip">
            <span class="hotspot-chip__idx">#{{ hIdx + 1 }}</span>
            <el-input v-model="hs.label" size="small" maxlength="20" placeholder="产品名称" style="width: 130px" />
            <el-input v-model="hs.path" size="small" maxlength="120" placeholder="跳转路径，如 /products/1" style="flex: 1" />
          </div>
        </div>
      </el-tab-pane>

      <!-- Tab 3: 三圆点纵向切换舞台 -->
      <el-tab-pane label="3. 三圆点纵向切换舞台 (IoT/交易/产业数字化)" name="showcase">
        <div v-for="(st, sIdx) in form.showcaseStages" :key="st.key" class="block-card">
          <div class="block-card__head">
            <strong>第 {{ sIdx + 1 }} 屏舞台：{{ st.title }}</strong>
            <el-tag size="small">{{ st.mode }}</el-tag>
          </div>
          <el-form label-width="130px">
            <el-form-item label="舞台大标题">
              <el-input v-model="st.title" maxlength="30" style="width: 260px" />
            </el-form-item>
            <el-form-item label="舞台描述文案">
              <el-input v-model="st.description" type="textarea" :rows="2" maxlength="200" />
            </el-form-item>
            <el-form-item v-if="st.mode === 'RADAR'" label="右侧雷达预警文案">
              <el-input v-model="st.alertLabel" style="width: 160px" placeholder="车距过近" />
            </el-form-item>
            <el-form-item v-if="st.mode === 'PAYMENT'" label="右侧支付卡片文案">
              <div class="metric-row" style="margin-bottom: 0">
                <el-input v-model="st.paymentTitle" style="width: 140px" placeholder="扫码加油" />
                <el-input v-model="st.paymentSubtitle" style="width: 140px" placeholder="订单支付" />
                <el-input v-model="st.paymentAmount" style="width: 120px" placeholder="¥202" />
              </div>
            </el-form-item>
          </el-form>

          <h4 style="margin: 8px 0; font-size: 13px">左下角 4 个子项 / 方案切换条：</h4>
          <div v-for="(sub, subIdx) in st.subItems" :key="subIdx" class="metric-row">
            <el-input v-if="st.mode !== 'SOLUTIONS'" v-model="sub.prefix" style="width: 70px" placeholder="G" />
            <el-input v-model="sub.title" style="width: 160px" placeholder="子项名称" />
            <el-input v-model="sub.path" style="width: 200px" placeholder="跳转路径 /products" />
            <el-input
              v-if="st.mode === 'SOLUTIONS'"
              v-model="sub.calloutsText"
              style="flex: 1"
              placeholder="右侧弹出的4个标签，用 / 分隔（如：RFID读头 / RFID标签 / RFID标签 / 挂车专用主机）"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <MediaPicker v-model="pickerOpen" :kind="pickerKind" @select="onMediaPicked" />
  </ConfigPanel>
</template>

<style scoped>
.studio-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  margin-bottom: 18px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f5f3ff 0%, #ede9fe 100%);
  border: 1px solid #c4b5fd;
  flex-wrap: wrap;
}
.studio-banner__title {
  font-size: 14.5px;
  color: #3730a3;
}
.studio-banner__desc {
  margin: 4px 0 0;
  font-size: 12.5px;
  color: #4f46e5;
}
.err {
  margin-bottom: 12px;
}
.tab-hint {
  font-size: 12.5px;
  color: var(--a-text-muted);
  margin: 0 0 14px;
}
.sub-h {
  font-size: 14px;
  margin: 12px 0;
}
.block-card {
  border: 1px solid var(--a-border);
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 14px;
  background: #fafbfe;
}
.block-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.media-pick-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.muted {
  font-size: 12px;
  color: var(--a-text-muted);
}
.metric-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}
.hotspot-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 12px;
}
.hotspot-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--a-text-muted);
  background: #f8fafc;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}
.hotspot-chip__idx {
  font-weight: 600;
  color: #5b4be8;
  white-space: nowrap;
}
.color-pickers-row {
  display: flex;
  align-items: center;
  gap: 18px;
  flex-wrap: wrap;
}
.color-picker-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.color-label {
  font-size: 12.5px;
  color: #475569;
  white-space: nowrap;
}
</style>
