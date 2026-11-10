<!-- 对应源码路径: website/admin/src/views/PreviewHomeView.vue -->
<script setup lang="ts">
/**
 * 全站多页面实景交互工作台 (Full-Site Visual Live Studio)
 *
 * 核心能力：
 * 1. 全站 8 大页面无缝实景切换与画布内点击穿透：
 *    - 🏠 首页 (HOME)：Hero 自由增删与自定义标题/按钮颜色、极简横条切换器、核心产品与方案左右灵动交错排布 (layout/tag/highlights)、案例、客户墙、新闻
 *    - 📦 产品中心 (PRODUCTS)：悬浮分类栏画布直改 + 产品卡片网格快编 + 点击进入「产品详情 7 段式结构化画布」直改
 *    - 🧩 解决方案 (SOLUTIONS)：分类栏画布直改 + 方案卡片网格 + 点击进入「方案详情（痛点/价值/正文/关联产品）画布」直改
 *    - 🏆 客户案例 (CASES)：行业分类栏直改 + 4×2 联名水印案例卡片 + 底部 2×2「客户成功故事」视频证言直改 + 「案例详情画布」直改
 *    - 📰 新闻中心 (NEWS)：分类栏直改 + 50/50 置顶头条大图卡 + 4×2 新闻网格 + 点击进入「新闻正文画布」直改
 *    - 🏢 关于我们 (ABOUT)：5 大子页签（企业简介 & CEO信全文弹窗 / 管理团队 & 高管履历弹窗 / 媒体中心 / 资料共享4分类 / 加入我们 & 5大价值观）+ 7城地图与荣誉资质全画布直改
 *    - 📞 联系我们 (CONTACT)：联系方式画布直改 + 右侧「预约产品展示」表单实时预览与开关（formEnabled / smsVerifyEnabled）
 *    - 🛡️ 隐私说明 (PRIVACY)：标题、更新日期与隐私正文所见即所得直改
 * 2. 跨页面全局脏数据追踪、30 步 Undo/Redo 历史栈、桌面/平板/手机三视口切换、一键全局保存草稿与按依赖顺序一键全局发布上线。
 */
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { LinkValue } from '@/components/LinkEditor.vue'
import MediaPicker from '@/components/MediaPicker.vue'
import {
  createCategory,
  createContent,
  getContent,
  listCategories,
  listContents,
  publishCategory,
  publishContent,
  saveCategory,
  saveDraft as saveContentDraft,
  type CategoryView,
  type ContentView,
  type MediaItem,
} from '@/api/content'
import { ApiError } from '@/api/http'
import { getConfig, publishConfig, saveConfigDraft, type ConfigKey, type ConfigView } from '@/api/site'
import { useAuth } from '@/stores/auth'

const route = useRoute()
const { current } = useAuth()
const isAdmin = computed(() => current.value?.role === 'ADMIN')
const activePageInfo = computed(() => STUDIO_PAGES.find((p) => p.key === activePage.value))
const hasHealthError = computed(() => healthIssues.value.some((i) => i.level === 'error'))
const activeProductRole = computed(() => activeContentDraft.value.roleTabs?.[activeProductRoleIdx.value] ?? null)
const currentContentType = computed(() => PAGE_TO_CONTENT_TYPE[activePage.value] ?? 'PRODUCT')
const currentCategoryType = computed(() => PAGE_TO_CATEGORY_TYPE[activePage.value] ?? 'PRODUCT')
const currentContents = computed(() => contentsMap[currentContentType.value] || [])
const currentCategories = computed(() => categoriesMap[currentCategoryType.value] || [])
const aboutMediaNews = computed(() => (contentsMap.NEWS || []).slice(0, 6))
const emptyTeamMember = { name: '', role: '', avatar: '', bio: [] as string[] }
const activeTeamMember = computed(() => {
  if (activeTeamIdx.value === null) return emptyTeamMember
  return drafts.value.ABOUT.teamMembers[activeTeamIdx.value] || emptyTeamMember
})

// ==================== 全站页面枚举与类型定义 ====================
export type StudioPageKey =
  | 'HOME'
  | 'PRODUCTS'
  | 'SOLUTIONS'
  | 'CASES'
  | 'NEWS'
  | 'ABOUT'
  | 'CONTACT'
  | 'PRIVACY'

const STUDIO_PAGES: Array<{ key: StudioPageKey; label: string; icon: string; badge?: string }> = [
  { key: 'HOME', label: '首页', icon: '🏠', badge: 'HOME' },
  { key: 'PRODUCTS', label: '产品中心', icon: '📦', badge: '列表+7段详情' },
  { key: 'SOLUTIONS', label: '解决方案', icon: '🧩', badge: '列表+详情' },
  { key: 'CASES', label: '客户案例', icon: '🏆', badge: '列表+故事+详情' },
  { key: 'NEWS', label: '新闻中心', icon: '📰', badge: '头条+列表+正文' },
  { key: 'ABOUT', label: '关于我们', icon: '🏢', badge: '5大子页签' },
  { key: 'CONTACT', label: '联系我们', icon: '📞', badge: 'CONTACT' },
  { key: 'PRIVACY', label: '隐私说明', icon: '🛡️', badge: 'PRIVACY' },
]

interface FooterLink {
  key?: string
  label: string
  enabled: boolean
  sortOrder: number
  link: LinkValue | null
}
interface FooterGroup {
  title: string
  links: FooterLink[]
}
interface QrCodeConfig {
  label: string
  mediaId?: number
  imageUrl?: string
}
interface ContactEmailConfig {
  email: string
  desc: string
}
interface SiteDraft {
  siteName: string
  slogan: string
  copyrightText: string
  icpText: string
  icpUrl: string
  policeText: string
  policeUrl: string
  logoMediaId?: number
  logoAlt: string
  faviconMediaId?: number
  defaultSeo: { title: string; description: string }
  footerGroups: FooterGroup[]
  qrCodes: QrCodeConfig[]
  contactEmails: ContactEmailConfig[]
}

interface MegaSubItem {
  label: string
  path: string
  tag?: string
  desc?: string
  link?: LinkValue | null
  isVideo?: boolean
  videoUrl?: string
  videoTitle?: string
}
interface MegaColumn {
  title: string
  items: MegaSubItem[]
}
interface NavNode {
  key?: string
  label: string
  enabled: boolean
  sortOrder: number
  link: LinkValue | null
  children?: NavNode[]
}
interface NavigationDraft {
  items: NavNode[]
  megaColumns: MegaColumn[]
  megaCta?: { text: string; linkText: string }
}

interface HeroItem {
  key?: string
  enabled: boolean
  sortOrder: number
  title: string
  subtitle: string
  kicker?: string
  videoUrl?: string
  desktopMediaId?: number
  mobileMediaId?: number
  imageAlt: string
  buttonText: string
  link: LinkValue | null
  titleColor?: string
  subtitleColor?: string
  kickerColor?: string
  buttonBgColor?: string
  buttonTextColor?: string
}
interface MetricItem {
  key?: string
  sortOrder: number
  displayValue: string
  unit: string
  label: string
  description: string
}
interface ServiceGroup {
  key?: string
  sortOrder: number
  title: string
  tag?: string
  highlights?: string[]
  highlightsText?: string
  description: string
  layout?: 'auto' | 'left' | 'right'
  imageMediaId?: number
  contentType: string
  contentIds: number[]
  moreLink: LinkValue | null
}
interface BlockShape {
  enabled: boolean
  title: string
  subtitle: string
}
export type HomeSectionKey = 'services' | 'cases' | 'customers' | 'news'
interface HomeDraft {
  sectionOrder: HomeSectionKey[]
  hero: { items: HeroItem[] }
  metrics: BlockShape & { items: MetricItem[] }
  services: BlockShape & { groups: ServiceGroup[] }
  cases: BlockShape & { contentIds: number[] }
  customers: BlockShape & { contentIds: number[] }
  news: BlockShape & { mode: string; limit: number; contentIds: number[] }
  consultation: { title: string; subtitle: string; buttonText: string; link: LinkValue | null }
}

interface AboutDraft {
  heroTitle: string
  introTitle: string
  intro: string
  introImage: string
  ceoLetter: {
    leftQuote: string
    title: string
    signature: string
    buttonText: string
    excerpt: string[]
    fullParagraphs: string[]
  }
  teamTitle: string
  teamMembers: Array<{ name: string; role: string; avatar?: string; bio: string[] }>
  resourcesTitle: string
  resources: Array<{
    category: string
    title: string
    logoText: string
    logoColor: string
    url: string
    isVideo?: boolean
  }>
  customerStories: Array<{
    quote: string
    author: string
    company: string
    logoText: string
    videoUrl: string
  }>
  mission: string
  vision: string
  values: Array<{ title: string; lines: string[] }>
  joinTitle: string
  joinButtonText: string
  joinUrl: string
  milestones: Array<{ year: string; title: string; description: string }>
  offices: Array<{
    city: string
    phone: string
    address: string
    image?: string
    mapX?: number
    mapY?: number
  }>
  qualifications: Array<{ name: string; description: string; mediaId?: number; imageUrl?: string }>
}

interface ContactDraft {
  title: string
  companyName: string
  description: string
  formTitle: string
  successText: string
  formEnabled: boolean
  smsVerifyEnabled: boolean
  phone: string
  email: string
  address: string
  hours: string
  mapUrl: string
}

interface PrivacyDraft {
  title: string
  updatedAt: string
  bodyHtml: string
}

const DEFAULT_SECTION_ORDER: HomeSectionKey[] = ['services', 'cases', 'customers', 'news']

const SECTION_META: Record<HomeSectionKey, { label: string; icon: string; formPath: string }> = {
  services: { label: '产品与解决方案分组', icon: '📦', formPath: '/home' },
  cases: { label: '标杆客户案例', icon: '🏆', formPath: '/home' },
  customers: { label: '客户标识 Logo 墙', icon: '🤝', formPath: '/home' },
  news: { label: '新闻动态资讯', icon: '📰', formPath: '/home' },
}

// ==================== 全局状态与多页面导航状态 ====================
const loading = ref(true)
const savingAll = ref(false)
const publishingAll = ref(false)

const activePage = ref<StudioPageKey>('HOME')
const studioMode = ref<'edit' | 'preview' | 'published'>('edit')
const viewport = ref<'desktop' | 'tablet' | 'mobile'>('desktop')
const showLeftOutline = ref(true)
const showDisabledBlocks = ref(true)
const healthDrawerOpen = ref(false)

// 首页内部状态
const heroIndex = ref(0)
const openMegaPreview = ref(false)
const selectedSection = ref<string>('hero')

// 关于我们内部状态（5 大子页签 + 2 个弹窗 + 时间轴/地图索引）
const aboutSubTab = ref<'intro' | 'team' | 'media' | 'resources' | 'join'>('intro')
const ceoModalOpen = ref(false)
const activeTeamIdx = ref<number | null>(null)
const aboutMilestoneIdx = ref(0)
const aboutOfficeIdx = ref(0)
const aboutResourceCat = ref('企业资料')

// 列表页与详情页内嵌实景编辑状态（PRODUCTS / SOLUTIONS / CASES / NEWS）
const activeCategoryFilter = ref<string>('ALL')
const activeContentId = ref<string>('')
const contentDetailLoading = ref(false)
const activeContentMeta = ref<ContentView | null>(null)
const activeProductRoleIdx = ref(0)
const activeContentDraft = ref<{
  title: string
  summary: string
  coverMediaId: number | null
  coverAlt: string
  heroMediaId: number | null
  categoryId: string
  sortOrder: number
  industry: string
  customerName: string
  quoteText: string
  quoteAuthor: string
  author: string
  displayTime: string
  sourceName: string
  sourceUrl: string
  bodyHtml: string
  seoTitle: string
  seoDescription: string
  features: Array<{ key?: string; tag?: string; title: string; description: string }>
  effectMetrics: Array<{ displayValue: string; label: string }>
  roleTabs: Array<{ role: string; headline: string; bullets: string[] }>
  terminalTools: Array<{ name: string; badge: string; description: string }>
  pains: Array<{ key?: string; title: string; description: string }>
  benefits: Array<{ key?: string; title: string; description: string }>
  outcomes: Array<{ key?: string; displayValue: string; label: string }>
  relatedCaseIds: number[]
  relatedProductIds: number[]
  relatedSolutionIds: number[]
}>({
  title: '',
  summary: '',
  coverMediaId: null,
  coverAlt: '',
  heroMediaId: null,
  categoryId: '',
  sortOrder: 100,
  industry: '',
  customerName: '',
  quoteText: '',
  quoteAuthor: '',
  author: '',
  displayTime: new Date().toISOString().slice(0, 16).replace('T', ' '),
  sourceName: '',
  sourceUrl: '',
  bodyHtml: '',
  seoTitle: '',
  seoDescription: '',
  features: [],
  effectMetrics: [],
  roleTabs: [],
  terminalTools: [],
  pains: [],
  benefits: [],
  outcomes: [],
  relatedCaseIds: [],
  relatedProductIds: [],
  relatedSolutionIds: [],
})
const activeContentBaseline = ref('')
const isActiveContentDirty = computed(() => {
  if (!activeContentId.value || !activeContentBaseline.value) return false
  return JSON.stringify(activeContentDraft.value) !== activeContentBaseline.value
})

// 六大单例配置视图
const configViews = reactive<{
  SITE: ConfigView<SiteDraft> | null
  NAVIGATION: ConfigView<NavigationDraft> | null
  HOME: ConfigView<HomeDraft> | null
  ABOUT: ConfigView<AboutDraft> | null
  CONTACT: ConfigView<ContactDraft> | null
  PRIVACY: ConfigView<PrivacyDraft> | null
}>({
  SITE: null,
  NAVIGATION: null,
  HOME: null,
  ABOUT: null,
  CONTACT: null,
  PRIVACY: null,
})

// 六大单例可编辑草稿副本
const drafts = ref<{
  SITE: SiteDraft
  NAVIGATION: NavigationDraft
  HOME: HomeDraft
  ABOUT: AboutDraft
  CONTACT: ContactDraft
  PRIVACY: PrivacyDraft
}>({
  SITE: {
    siteName: '公司官网',
    slogan: '领先的物联网与数字化软件服务商',
    copyrightText: '© 2026 示例科技有限公司',
    icpText: '京ICP备08004136号',
    icpUrl: 'https://beian.miit.gov.cn/',
    policeText: '',
    policeUrl: '',
    logoMediaId: undefined,
    logoAlt: '',
    faviconMediaId: undefined,
    defaultSeo: { title: '企业官网', description: '面向企业的物联网与数字化平台' },
    footerGroups: [],
    qrCodes: [
      { label: '公众号', imageUrl: '' },
      { label: '视频号', imageUrl: '' },
    ],
    contactEmails: [
      { email: 'connect@g7.com.cn', desc: '媒体联系&数据合作' },
      { email: 'jiancha@g7.com.cn', desc: '集团投诉与举报' },
    ],
  },
  NAVIGATION: {
    items: [],
    megaColumns: [],
    megaCta: { text: '需要专业场景建议？', linkText: '联系技术专家' },
  },
  HOME: {
    sectionOrder: [...DEFAULT_SECTION_ORDER],
    hero: { items: [] },
    metrics: { enabled: false, title: '企业实力', subtitle: '', items: [] },
    services: { enabled: true, title: '核心产品与行业方案', subtitle: '', groups: [] },
    cases: { enabled: true, title: '客户案例', subtitle: '', contentIds: [] },
    customers: { enabled: true, title: '合作客户', subtitle: '', contentIds: [] },
    news: { enabled: true, title: '新闻动态', subtitle: '', mode: 'LATEST', limit: 3, contentIds: [] },
    consultation: { title: '了解适合您的服务', subtitle: '', buttonText: '立即咨询', link: { type: 'CONSULT' } },
  },
  ABOUT: {
    heroTitle: '以物联网技术和软件服务改变公路货运',
    introTitle: '关于我们',
    intro: '我们是一家领先的产业物联网科技和软件服务公司，连接业务全流程并以数据和AI驱动运营，助力企业降本增效。',
    introImage: '',
    ceoLetter: {
      leftQuote: '“我谨代表全体员工，向在这个不凡时代中努力前行的经营者致以崇高的敬意！”',
      title: '从微不足道开始改变世界',
      signature: '创始人',
      buttonText: '展开全部',
      excerpt: [
        '十多年前，当我们刚开始走进业务一线的时候，很少有人相信，一个个小小的智能终端，有一天会彻底改变整个行业的运行方式。',
        '正是从每一次毫秒级的感知、每一次防碰撞预警、每一份数据的精准计量这些看似“微不足道”的小事开始，我们陪伴客户迈入数字化精细运营的新时代。',
      ],
      fullParagraphs: [
        '我谨代表全体员工，向在这个不凡时代中努力前行的行业经营者致以崇高的敬意！',
        '十多年前，当我们刚开始走进业务一线的时候，很少有人相信，一个个小小的智能终端，有一天会彻底改变整个行业的运行方式。',
        '正是从每一次毫秒级的感知、每一次防碰撞预警、每一份数据的精准计量这些看似“微不足道”的小事开始，我们陪伴客户迈入数字化精细运营的新时代。',
        '时代在变，技术在变，但我们存在的唯一理由始终未变——用科技创新和服务升级产业生产力，做客户最值得信赖的长期伙伴。',
      ],
    },
    teamTitle: '管理团队',
    teamMembers: [
      {
        name: '翟学魂',
        role: '首席执行官 (CEO)',
        avatar: '',
        bio: ['现任公司首席执行官，产业物联网和数字化领域的开拓者。', '毕业于清华大学，致力于推进产业的智能化与绿色化转型。'],
      },
      {
        name: '张景涛',
        role: '总裁',
        avatar: '',
        bio: ['全面负责公司产品研发、行业解决方案及客户成功体系建设。'],
      },
      {
        name: '郝淑霞',
        role: '高级副总裁',
        avatar: '',
        bio: ['负责组织发展、人才体系与大客户战略生态协同。'],
      },
      {
        name: '张杰龙',
        role: '首席财务官 (CFO)',
        avatar: '',
        bio: ['负责集团财务战略、资本市场运作及战略投资并购。'],
      },
    ],
    resourcesTitle: '资料库',
    resources: [
      { category: '企业资料', title: '企业全栈产品手册', logoText: '全栈产品宣传册', logoColor: '#4c2ce6', url: '' },
      { category: '企业资料', title: '安全管家白皮书', logoText: '主动安全托管指南', logoColor: '#2563eb', url: '' },
      { category: '客户视频', title: '标杆客户证言视频', logoText: '双汇物流 × 数字化实践', logoColor: '#dc2626', url: '', isVideo: true },
      { category: '行业报告', title: '2026新能源车队管理白皮书', logoText: '新能源车队运营报告', logoColor: '#0284c7', url: '' },
      { category: 'ESG', title: '年度可持续发展报告', logoText: 'ESG 绿色低碳实践', logoColor: '#059669', url: '' },
    ],
    customerStories: [
      {
        quote: '“通过数字化平台合作，对公司各项核心经营指标都有显著提升。”',
        author: '张太喜  双汇物流董事长',
        company: '双汇物流',
        logoText: 'SHUANGHUI 双汇物流',
        videoUrl: '',
      },
      {
        quote: '“统一多元的数据，加上精准的算法，帮助我们完善了管理闭环。”',
        author: '何镭  天佑物流执行总监',
        company: '天佑物流',
        logoText: 'tianyou天佑',
        videoUrl: '',
      },
      {
        quote: '“「数字化」之后，每一个往返我们都能实时算清利润账。”',
        author: '赵春波  滨拓物流总经理',
        company: '滨拓物流',
        logoText: '滨拓物流',
        videoUrl: '',
      },
      {
        quote: '通过IoT技术重构大宗运输环节，让干线运输与装卸解耦，助力绿色低碳转型。',
        author: '华电能源包头公司',
        company: '中国华电 CHD',
        logoText: '中国华电 CHD',
        videoUrl: '',
      },
    ],
    mission: '以科技创新和服务升级产业生产力，让产业参与者共享美好改变',
    vision: '产业最受尊重的SaaS公司',
    values: [
      { title: '客户成功', lines: ['我们存在的唯一目的就是为客户带来价值', '以可信赖的产品和有温度的服务，做客户的长期朋友'] },
      { title: '极限', lines: ['目标远大，不甘平庸，不断挑战认知边界', '把事做透，用工匠精神实现卓越成果'] },
      { title: '信任', lines: ['以身作则，说到做到', '敢于信任，从我做起'] },
      { title: '坚韧', lines: ['坚持长期主义积极面对挑战和不确定性', '坚定目标，锲而不舍，迎难而上，确保结果'] },
      { title: '主人翁精神', lines: ['从全局出发，长期利益最大化', '主动担当，站在造钟者的角度思考并解决问题'] },
    ],
    joinTitle: '与我们路上见',
    joinButtonText: '查看职位',
    joinUrl: '',
    milestones: [
      { year: '2018', title: '发布智能安全托管服务', description: '连接平台终端突破 80 万台；完成战略融资' },
      { year: '2019', title: '平台连接数突破 140 万', description: '面向多行业提供综合性数字化解决方案' },
      { year: '2022', title: '平台连接数突破 200 万', description: '宣布完成新一轮 2 亿美元融资；量产交付智能装备' },
      { year: '2010', title: '启动 SaaS 服务', description: '提供基于北斗与 GPS 的智能管理 SaaS 服务' },
      { year: '2014', title: '设立七大区域运营中心', description: '推出智能结算服务体系，服务超万家企业' },
    ],
    offices: [
      { city: '北京（总部）', phone: '010-62667878', address: '北京市海淀区建材城中路27号金隅智造工场N7号楼2层', mapX: 68, mapY: 41 },
      { city: '深圳', phone: '0755-26944168', address: '深圳市南山区滨海大道3369号有线信息传输大厦9层', mapX: 68, mapY: 77 },
      { city: '成都', phone: '028-83358692', address: '成都市天府新区兴隆湖畔路西段99号天府新经济产业园D区', mapX: 53, mapY: 63 },
      { city: '广州', phone: '', address: '广州市黄埔区科学大道237号A1栋1106室', mapX: 66, mapY: 74 },
      { city: '上海', phone: '021-51083899', address: '上海市闵行区申长路988弄虹桥万科中心T2号楼6层', mapX: 76, mapY: 57 },
      { city: '西安', phone: '', address: '西安市雁塔区丈八一路1号汇鑫IBC-D座8楼', mapX: 59, mapY: 52 },
      { city: '天津', phone: '', address: '天津市东丽区东二道空港商务园东区E6', mapX: 71, mapY: 45 },
    ],
    qualifications: [],
  },
  CONTACT: {
    title: '联系我们',
    companyName: '北京汇通天下物联科技有限公司',
    description: '感谢您的关注，我们的专家顾问随时为您提供产品演示与方案咨询。',
    formTitle: '预约产品展示',
    successText: '已收到您的咨询，我们会尽快与您联系。',
    formEnabled: true,
    smsVerifyEnabled: true,
    phone: '400-888-6688',
    email: 'connect@g7.com.cn',
    address: '北京市海淀区建材城中路27号金隅智造工场N7号楼2层',
    hours: '周一至周日 09:00 - 18:00',
    mapUrl: '',
  },
  PRIVACY: {
    title: '隐私说明',
    updatedAt: new Date().toISOString().slice(0, 10),
    bodyHtml: '<h2>一、我们如何收集和使用您的信息</h2><p>当您在官网提交预约咨询表单时，我们会收集您的姓名、联系电话、公司名称及咨询需求，仅用于安排产品顾问与您联系提供服务。</p>',
  },
})

const ALL_CONFIG_KEYS: ConfigKey[] = ['SITE', 'NAVIGATION', 'HOME', 'ABOUT', 'CONTACT', 'PRIVACY']

const baselineJson = reactive<Record<ConfigKey, string>>({
  SITE: '',
  NAVIGATION: '',
  HOME: '',
  ABOUT: '',
  CONTACT: '',
  PRIVACY: '',
})

const dirtyConfigKeys = computed<ConfigKey[]>(() => {
  return ALL_CONFIG_KEYS.filter((k) => {
    const cur = JSON.stringify(drafts.value[k])
    return Boolean(baselineJson[k] && cur !== baselineJson[k])
  })
})

const unpublishedConfigKeys = computed<ConfigKey[]>(() => {
  return ALL_CONFIG_KEYS.filter(
    (k) => configViews[k]?.hasUnpublishedChanges || dirtyConfigKeys.value.includes(k),
  )
})

const totalDirtyCount = computed(
  () => dirtyConfigKeys.value.length + (isActiveContentDirty.value ? 1 : 0),
)

export type CategoryType = 'PRODUCT' | 'SOLUTION' | 'CASE' | 'NEWS'
export type ContentType = 'PRODUCT' | 'SOLUTION' | 'CASE' | 'NEWS' | 'CUSTOMER'

// 30 步撤销 / 重做历史栈（全站 6 大单例草稿）
const historyStack = ref<string[]>([])
const historyPointer = ref(-1)
let isApplyingHistory = false
let historyTimer: ReturnType<typeof setTimeout> | null = null

function pushHistorySnapshot() {
  if (isApplyingHistory || loading.value) return
  const snap = JSON.stringify(drafts)
  if (historyPointer.value >= 0 && historyStack.value[historyPointer.value] === snap) return
  historyStack.value = historyStack.value.slice(0, historyPointer.value + 1)
  historyStack.value.push(snap)
  if (historyStack.value.length > 30) {
    historyStack.value.shift()
  } else {
    historyPointer.value++
  }
}

watch(
  drafts,
  () => {
    if (isApplyingHistory || loading.value) return
    if (historyTimer) clearTimeout(historyTimer)
    historyTimer = setTimeout(() => {
      pushHistorySnapshot()
    }, 450)
  },
  { deep: true },
)

const canUndo = computed(() => historyPointer.value > 0)
const canRedo = computed(() => historyPointer.value < historyStack.value.length - 1)

function undo() {
  if (!canUndo.value) return
  isApplyingHistory = true
  historyPointer.value--
  const snap = JSON.parse(historyStack.value[historyPointer.value])
  Object.assign(drafts.value.SITE, snap.SITE)
  Object.assign(drafts.value.NAVIGATION, snap.NAVIGATION)
  Object.assign(drafts.value.HOME, snap.HOME)
  Object.assign(drafts.value.ABOUT, snap.ABOUT)
  Object.assign(drafts.value.CONTACT, snap.CONTACT)
  Object.assign(drafts.value.PRIVACY, snap.PRIVACY)
  setTimeout(() => {
    isApplyingHistory = false
  }, 50)
}

function redo() {
  if (!canRedo.value) return
  isApplyingHistory = true
  historyPointer.value++
  const snap = JSON.parse(historyStack.value[historyPointer.value])
  Object.assign(drafts.value.SITE, snap.SITE)
  Object.assign(drafts.value.NAVIGATION, snap.NAVIGATION)
  Object.assign(drafts.value.HOME, snap.HOME)
  Object.assign(drafts.value.ABOUT, snap.ABOUT)
  Object.assign(drafts.value.CONTACT, snap.CONTACT)
  Object.assign(drafts.value.PRIVACY, snap.PRIVACY)
  setTimeout(() => {
    isApplyingHistory = false
  }, 50)
}

function onKeyDown(e: KeyboardEvent) {
  const isMod = e.ctrlKey || e.metaKey
  if (!isMod) return
  if (e.key.toLowerCase() === 'z' && !e.shiftKey) {
    e.preventDefault()
    undo()
  } else if ((e.key.toLowerCase() === 'z' && e.shiftKey) || e.key.toLowerCase() === 'y') {
    e.preventDefault()
    redo()
  } else if (e.key.toLowerCase() === 's') {
    e.preventDefault()
    saveAllDrafts()
  }
}

// 通用素材选择器状态
const pickerOpen = ref(false)
const pickerKind = ref<'IMAGE' | 'VIDEO' | 'ALL'>('IMAGE')
let pickerCallback: ((media: MediaItem) => void) | null = null

function openMediaPicker(kind: 'IMAGE' | 'VIDEO' | 'ALL', cb: (m: MediaItem) => void) {
  pickerKind.value = kind
  pickerCallback = cb
  pickerOpen.value = true
}

function onMediaPicked(media: MediaItem) {
  mediaCache[Number(media.id)] = media
  if (pickerCallback) {
    pickerCallback(media)
    pickerCallback = null
  }
  pickerOpen.value = false
}

// 媒体资源与内容实体字典
const mediaCache = reactive<Record<number, MediaItem>>({})
const categoriesMap = reactive<Record<CategoryType, CategoryView[]>>({
  PRODUCT: [],
  SOLUTION: [],
  CASE: [],
  NEWS: [],
})
const contentsMap = reactive<Record<ContentType, ContentView[]>>({
  PRODUCT: [],
  SOLUTION: [],
  CASE: [],
  NEWS: [],
  CUSTOMER: [],
})
const contentDetailsCache = reactive<Record<string, Record<string, any>>>({})
const activeContentRawBackup = ref<Record<string, unknown>>({})

function clone<T>(val: T): T {
  return JSON.parse(JSON.stringify(val ?? {}))
}

function normalizeSectionOrder(raw?: unknown): HomeSectionKey[] {
  const list = Array.isArray(raw) ? (raw as HomeSectionKey[]) : []
  const valid = list.filter((k) => DEFAULT_SECTION_ORDER.includes(k))
  for (const key of DEFAULT_SECTION_ORDER) {
    if (!valid.includes(key)) valid.push(key)
  }
  return valid
}

function normalizeSite(d?: Partial<SiteDraft> | null): SiteDraft {
  const c = clone(d || {})
  return {
    siteName: c.siteName ?? 'G7易流',
    slogan: c.slogan ?? '领先的物联网与数字化软件服务商',
    copyrightText: c.copyrightText ?? '© 2026 示例科技有限公司',
    icpText: c.icpText ?? '京ICP备08004136号',
    icpUrl: c.icpUrl ?? 'https://beian.miit.gov.cn/',
    policeText: c.policeText ?? '京公网安备11010802020592号',
    policeUrl: c.policeUrl ?? '',
    logoMediaId: c.logoMediaId ? Number(c.logoMediaId) : undefined,
    logoAlt: c.logoAlt ?? '',
    faviconMediaId: c.faviconMediaId ? Number(c.faviconMediaId) : undefined,
    defaultSeo: {
      title: c.defaultSeo?.title ?? '企业官网',
      description: c.defaultSeo?.description ?? '面向企业的物联网与数字化平台',
    },
    footerGroups: (c.footerGroups ?? []).map((g) => ({
      title: g.title ?? '',
      links: (g.links ?? []).map((l) => ({
        key: l.key,
        label: l.label ?? '',
        enabled: l.enabled ?? true,
        sortOrder: l.sortOrder ?? 100,
        link: l.link ?? { type: 'PAGE', pageCode: 'HOME' },
      })),
    })),
    qrCodes:
      Array.isArray(c.qrCodes) && c.qrCodes.length
        ? c.qrCodes
        : [
            { label: '公众号', imageUrl: '' },
            { label: '视频号', imageUrl: '' },
          ],
    contactEmails:
      Array.isArray(c.contactEmails) && c.contactEmails.length
        ? c.contactEmails
        : [
            { email: 'connect@g7.com.cn', desc: '媒体联系&数据合作' },
            { email: 'jiancha@g7.com.cn', desc: '集团投诉与举报' },
          ],
  }
}

function normalizeNav(d?: Partial<NavigationDraft> | null): NavigationDraft {
  const c = clone(d || {})
  const items = (c.items ?? []).map((n) => ({
    key: n.key,
    label: n.label ?? '',
    enabled: n.enabled ?? true,
    sortOrder: n.sortOrder ?? 100,
    link: n.link ?? { type: 'PAGE', pageCode: 'HOME' },
    children: (n.children ?? []).map((ch) => ({
      key: ch.key,
      label: ch.label ?? '',
      enabled: ch.enabled ?? true,
      sortOrder: ch.sortOrder ?? 100,
      link: ch.link ?? { type: 'PAGE', pageCode: 'HOME' },
    })),
  }))
  return {
    items: items.length
      ? items
      : [
          { label: '首页', enabled: true, sortOrder: 10, link: { type: 'PAGE', pageCode: 'HOME' }, children: [] },
          { label: '产品及解决方案', enabled: true, sortOrder: 20, link: { type: 'PAGE', pageCode: 'PRODUCTS' }, children: [] },
          { label: '客户案例', enabled: true, sortOrder: 30, link: { type: 'PAGE', pageCode: 'CASES' }, children: [] },
          { label: '新闻中心', enabled: true, sortOrder: 40, link: { type: 'PAGE', pageCode: 'NEWS' }, children: [] },
          { label: '关于我们', enabled: true, sortOrder: 50, link: { type: 'PAGE', pageCode: 'ABOUT' }, children: [] },
        ],
    megaColumns: (c.megaColumns ?? []).map((col) => ({
      title: col.title ?? '',
      items: (col.items ?? []).map((sub) => ({
        label: sub.label ?? '',
        path: sub.path ?? '/products',
        tag: sub.tag ?? '',
        isVideo: Boolean(sub.isVideo),
        videoUrl: sub.videoUrl ?? '',
        videoTitle: sub.videoTitle ?? '',
      })),
    })),
    megaCta: {
      text: c.megaCta?.text ?? '需要专业场景建议？',
      linkText: c.megaCta?.linkText ?? '联系技术专家',
    },
  }
}

function normalizeHome(d?: Partial<HomeDraft> | null): HomeDraft {
  const c = clone(d || {})
  return {
    sectionOrder: normalizeSectionOrder((c as Record<string, unknown>).sectionOrder),
    hero: {
      items:
        Array.isArray(c.hero?.items) && c.hero!.items.length > 0
          ? c.hero!.items.map((h) => ({
              key: h.key,
              enabled: h.enabled ?? true,
              sortOrder: h.sortOrder ?? 100,
              title: h.title ?? '',
              subtitle: h.subtitle ?? '',
              desktopMediaId: h.desktopMediaId ? Number(h.desktopMediaId) : undefined,
              mobileMediaId: h.mobileMediaId ? Number(h.mobileMediaId) : undefined,
              imageAlt: h.imageAlt ?? '',
              buttonText: h.buttonText ?? '了解更多',
              link: h.link ?? { type: 'PAGE', pageCode: 'PRODUCTS' },
              titleColor: h.titleColor ?? '',
              subtitleColor: h.subtitleColor ?? '',
              kickerColor: h.kickerColor ?? '',
              buttonBgColor: h.buttonBgColor ?? '',
              buttonTextColor: h.buttonTextColor ?? '',
            }))
          : [
              {
                enabled: true,
                sortOrder: 100,
                title: 'AI与物联网驱动企业效率变革',
                subtitle: '连接核心业务流程，实时掌控经营关键指标',
                imageAlt: '焦点图',
                buttonText: '探索解决方案',
                link: { type: 'PAGE', pageCode: 'SOLUTIONS' },
                titleColor: '',
                subtitleColor: '',
                kickerColor: '',
                buttonBgColor: '',
                buttonTextColor: '',
              },
            ],
    },
    metrics: {
      enabled: c.metrics?.enabled ?? false,
      title: c.metrics?.title ?? '企业实力',
      subtitle: c.metrics?.subtitle ?? '',
      items: Array.isArray(c.metrics?.items) ? c.metrics!.items : [],
    },
    services: {
      enabled: c.services?.enabled ?? true,
      title: c.services?.title ?? '核心产品与行业方案',
      subtitle: c.services?.subtitle ?? '',
      groups: (c.services?.groups ?? []).map((g) => {
        const hlArr = Array.isArray(g.highlights) ? g.highlights : []
        return {
          key: g.key,
          sortOrder: g.sortOrder ?? 100,
          title: g.title ?? '',
          tag: g.tag ?? '',
          highlights: hlArr,
          highlightsText:
            typeof g.highlightsText === 'string' ? g.highlightsText : hlArr.join(' / '),
          description: g.description ?? '',
          layout: g.layout || 'auto',
          imageMediaId: g.imageMediaId ? Number(g.imageMediaId) : undefined,
          contentType: g.contentType ?? 'PRODUCT',
          contentIds: (g.contentIds ?? []).map(Number),
          moreLink: g.moreLink ?? { type: 'PAGE', pageCode: 'PRODUCTS' },
        }
      }),
    },
    cases: {
      enabled: c.cases?.enabled ?? true,
      title: c.cases?.title ?? '客户案例',
      subtitle: c.cases?.subtitle ?? '',
      contentIds: (c.cases?.contentIds ?? []).map(Number),
    },
    customers: {
      enabled: c.customers?.enabled ?? true,
      title: c.customers?.title ?? '合作客户',
      subtitle: c.customers?.subtitle ?? '',
      contentIds: (c.customers?.contentIds ?? []).map(Number),
    },
    news: {
      enabled: c.news?.enabled ?? true,
      title: c.news?.title ?? '新闻动态',
      subtitle: c.news?.subtitle ?? '',
      mode: c.news?.mode ?? 'LATEST',
      limit: c.news?.limit ?? 3,
      contentIds: (c.news?.contentIds ?? []).map(Number),
    },
    consultation: {
      title: c.consultation?.title ?? '了解适合您的服务',
      subtitle: c.consultation?.subtitle ?? '',
      buttonText: c.consultation?.buttonText ?? '立即咨询',
      link: c.consultation?.link ?? { type: 'CONSULT' },
    },
  }
}

function normalizeAbout(d?: Partial<AboutDraft> | null): AboutDraft {
  const c = clone(d || {})
  return {
    heroTitle: c.heroTitle || '以物联网技术和软件服务改变公路货运',
    introTitle: c.introTitle || '关于我们',
    intro:
      c.intro ||
      '我们是一家领先的产业物联网科技和软件服务公司，连接业务全流程并以数据和AI驱动运营，助力企业降本增效。',
    introImage: c.introImage || '',
    ceoLetter: {
      leftQuote:
        c.ceoLetter?.leftQuote ||
        '“我谨代表全体员工，向在这个不凡时代中努力前行的经营者致以崇高的敬意！”',
      title: c.ceoLetter?.title || '从微不足道开始改变世界',
      signature: c.ceoLetter?.signature || '创始人',
      buttonText: c.ceoLetter?.buttonText || '展开全部',
      excerpt:
        Array.isArray(c.ceoLetter?.excerpt) && c.ceoLetter!.excerpt.length
          ? c.ceoLetter!.excerpt
          : clone(drafts.value.ABOUT.ceoLetter.excerpt),
      fullParagraphs:
        Array.isArray(c.ceoLetter?.fullParagraphs) && c.ceoLetter!.fullParagraphs.length
          ? c.ceoLetter!.fullParagraphs
          : clone(drafts.value.ABOUT.ceoLetter.fullParagraphs),
    },
    teamTitle: c.teamTitle || '管理团队',
    teamMembers:
      Array.isArray(c.teamMembers) && c.teamMembers.length
        ? c.teamMembers
        : clone(drafts.value.ABOUT.teamMembers),
    resourcesTitle: c.resourcesTitle || '资料库',
    resources:
      Array.isArray(c.resources) && c.resources.length
        ? c.resources
        : clone(drafts.value.ABOUT.resources),
    customerStories:
      Array.isArray(c.customerStories) && c.customerStories.length
        ? c.customerStories
        : clone(drafts.value.ABOUT.customerStories),
    mission: c.mission || '以科技创新和服务升级产业生产力，让产业参与者共享美好改变',
    vision: c.vision || '产业最受尊重的SaaS公司',
    values:
      Array.isArray(c.values) && c.values.length ? c.values : clone(drafts.value.ABOUT.values),
    joinTitle: c.joinTitle || '与我们路上见',
    joinButtonText: c.joinButtonText || '查看职位',
    joinUrl: c.joinUrl || '',
    milestones:
      Array.isArray(c.milestones) && c.milestones.length
        ? c.milestones
        : clone(drafts.value.ABOUT.milestones),
    offices:
      Array.isArray(c.offices) && c.offices.length ? c.offices : clone(drafts.value.ABOUT.offices),
    qualifications: Array.isArray(c.qualifications) ? c.qualifications : [],
  }
}

function normalizeContact(d?: Partial<ContactDraft> | null): ContactDraft {
  const c = clone(d || {})
  return {
    title: c.title || '联系我们',
    companyName: c.companyName || '北京汇通天下物联科技有限公司',
    description: c.description || '感谢您的关注，我们的专家顾问随时为您提供产品演示与方案咨询。',
    formTitle: c.formTitle || '预约产品展示',
    successText: c.successText || '已收到您的咨询，我们会尽快与您联系。',
    formEnabled: c.formEnabled ?? true,
    smsVerifyEnabled: c.smsVerifyEnabled ?? true,
    phone: c.phone || '400-888-6688',
    email: c.email || 'connect@g7.com.cn',
    address: c.address || '北京市海淀区建材城中路27号金隅智造工场N7号楼2层',
    hours: c.hours || '周一至周日 09:00 - 18:00',
    mapUrl: c.mapUrl || '',
  }
}

function normalizePrivacy(d?: Partial<PrivacyDraft> | null): PrivacyDraft {
  const c = clone(d || {})
  return {
    title: c.title || '隐私说明',
    updatedAt: c.updatedAt || new Date().toISOString().slice(0, 10),
    bodyHtml:
      c.bodyHtml ||
      '<h2>一、我们如何收集和使用您的信息</h2><p>当您在官网提交预约咨询表单时，我们会收集您的姓名、联系电话、公司名称及咨询需求，仅用于安排产品顾问与您联系提供服务。</p>',
  }
}

// ==================== 全站数据加载 ====================
async function loadAll() {
  loading.value = true
  try {
    const [
      siteRes,
      navRes,
      homeRes,
      aboutRes,
      contactRes,
      privacyRes,
      prodCats,
      solCats,
      caseCats,
      newsCats,
      prods,
      sols,
      cases,
      news,
      custs,
    ] = await Promise.all([
      getConfig<SiteDraft>('SITE'),
      getConfig<NavigationDraft>('NAVIGATION'),
      getConfig<HomeDraft>('HOME'),
      getConfig<AboutDraft>('ABOUT'),
      getConfig<ContactDraft>('CONTACT'),
      getConfig<PrivacyDraft>('PRIVACY'),
      listCategories('PRODUCT'),
      listCategories('SOLUTION'),
      listCategories('CASE'),
      listCategories('NEWS'),
      listContents({ contentType: 'PRODUCT', page: 1, pageSize: 100 }),
      listContents({ contentType: 'SOLUTION', page: 1, pageSize: 100 }),
      listContents({ contentType: 'CASE', page: 1, pageSize: 100 }),
      listContents({ contentType: 'NEWS', page: 1, pageSize: 100 }),
      listContents({ contentType: 'CUSTOMER', page: 1, pageSize: 100 }),
    ])

    configViews.SITE = siteRes
    configViews.NAVIGATION = navRes
    configViews.HOME = homeRes
    configViews.ABOUT = aboutRes
    configViews.CONTACT = contactRes
    configViews.PRIVACY = privacyRes

    drafts.value.SITE = normalizeSite(siteRes.draft)
    drafts.value.NAVIGATION = normalizeNav(navRes.draft)
    drafts.value.HOME = normalizeHome(homeRes.draft)
    drafts.value.ABOUT = normalizeAbout(aboutRes.draft)
    drafts.value.CONTACT = normalizeContact(contactRes.draft)
    drafts.value.PRIVACY = normalizePrivacy(privacyRes.draft)

    for (const k of ALL_CONFIG_KEYS) {
      baselineJson[k] = JSON.stringify(drafts.value[k])
    }

    categoriesMap.PRODUCT = prodCats.items || []
    categoriesMap.SOLUTION = solCats.items || []
    categoriesMap.CASE = caseCats.items || []
    categoriesMap.NEWS = newsCats.items || []

    contentsMap.PRODUCT = prods.items || []
    contentsMap.SOLUTION = sols.items || []
    contentsMap.CASE = cases.items || []
    contentsMap.NEWS = news.items || []
    contentsMap.CUSTOMER = custs.items || []

    historyStack.value = [JSON.stringify(drafts)]
    historyPointer.value = 0

    await syncPageFromRoute()
  } catch (err) {
    ElMessage.error(err instanceof ApiError ? err.message : '加载全站实景工作台数据失败')
  } finally {
    loading.value = false
  }
}

const SLUG_TO_PAGE: Record<string, StudioPageKey> = {
  home: 'HOME',
  products: 'PRODUCTS',
  product: 'PRODUCTS',
  solutions: 'SOLUTIONS',
  solution: 'SOLUTIONS',
  cases: 'CASES',
  case: 'CASES',
  news: 'NEWS',
  about: 'ABOUT',
  contact: 'CONTACT',
  privacy: 'PRIVACY',
}

const PAGE_TO_CONTENT_TYPE: Partial<Record<StudioPageKey, ContentType>> = {
  PRODUCTS: 'PRODUCT',
  SOLUTIONS: 'SOLUTION',
  CASES: 'CASE',
  NEWS: 'NEWS',
}

const PAGE_TO_CATEGORY_TYPE: Partial<Record<StudioPageKey, CategoryType>> = {
  PRODUCTS: 'PRODUCT',
  SOLUTIONS: 'SOLUTION',
  CASES: 'CASE',
  NEWS: 'NEWS',
}

async function syncPageFromRoute() {
  const paramSlug = String(
    route.params.slug || route.params.page || route.query.page || '',
  ).toLowerCase()
  const paramId = String(route.params.id || route.query.id || '')
  if (paramSlug && SLUG_TO_PAGE[paramSlug]) {
    activePage.value = SLUG_TO_PAGE[paramSlug]
  }
  if (paramId && PAGE_TO_CONTENT_TYPE[activePage.value]) {
    await openContentInStudio(PAGE_TO_CONTENT_TYPE[activePage.value]!, paramId)
  }
}

function switchStudioPage(page: StudioPageKey, keepDetail = false) {
  activePage.value = page
  activeCategoryFilter.value = 'ALL'
  if (!keepDetail) {
    activeContentId.value = ''
    activeContentMeta.value = null
    activeContentBaseline.value = ''
  }
  openMegaPreview.value = false
}

// ==================== 内容详情内嵌实景编辑（产品/方案/案例/新闻） ====================
async function openContentInStudio(type: ContentType, id: string | number) {
  const pageMap: Record<ContentType, StudioPageKey> = {
    PRODUCT: 'PRODUCTS',
    SOLUTION: 'SOLUTIONS',
    CASE: 'CASES',
    NEWS: 'NEWS',
    CUSTOMER: 'HOME',
  }
  activePage.value = pageMap[type] || 'HOME'
  if (type === 'CUSTOMER') return

  contentDetailLoading.value = true
  try {
    const detail = await getContent(String(id))
    const view = detail.content
    activeContentId.value = String(view.id)
    activeContentMeta.value = view
    activeProductRoleIdx.value = 0

    const d = (
      studioMode.value === 'published' && detail.published ? detail.published : detail.draft || {}
    ) as Record<string, any>
    activeContentRawBackup.value = { ...d }
    contentDetailsCache[String(view.id)] = d

    activeContentDraft.value.title = String(d.title ?? view.title ?? '')
    activeContentDraft.value.summary = String(d.summary ?? '')
    activeContentDraft.value.coverMediaId = d.coverMediaId != null ? Number(d.coverMediaId) : null
    activeContentDraft.value.coverAlt = String(d.coverAlt ?? '')
    activeContentDraft.value.heroMediaId = d.heroMediaId != null ? Number(d.heroMediaId) : null
    activeContentDraft.value.categoryId =
      d.categoryId != null ? String(d.categoryId) : view.categoryId ? String(view.categoryId) : ''
    activeContentDraft.value.sortOrder = Number(d.sortOrder ?? view.sortOrder ?? 100)
    activeContentDraft.value.industry = String(d.industry ?? '')
    activeContentDraft.value.customerName = String(d.customerName ?? '')
    activeContentDraft.value.quoteText = String(d.quoteText ?? '')
    activeContentDraft.value.quoteAuthor = String(d.quoteAuthor ?? '')
    activeContentDraft.value.author = String(d.author ?? '')
    activeContentDraft.value.displayTime = String(
      d.displayTime ?? view.displayTime ?? new Date().toISOString().slice(0, 16).replace('T', ' '),
    )
    activeContentDraft.value.sourceName = String(d.sourceName ?? '')
    activeContentDraft.value.sourceUrl = String(d.sourceUrl ?? '')
    activeContentDraft.value.bodyHtml = String(d.bodyHtml ?? '')
    activeContentDraft.value.seoTitle = String(d.seo?.title ?? d.seoTitle ?? '')
    activeContentDraft.value.seoDescription = String(d.seo?.description ?? d.seoDescription ?? '')

    activeContentDraft.value.features =
      Array.isArray(d.features) && d.features.length
        ? clone(d.features)
        : [
            { tag: '全天候感知', title: '智能算法实时识别风险', description: '7×24 小时自动监测关键节点，异常事件毫秒级预警。' },
            { tag: '闭环协同', title: '多端联动自动派单处置', description: '打通总部看板与一线移动端，形成完整管理闭环。' },
            { tag: '数据洞察', title: '经营指标可视化分析', description: '多维度核算单车与线路效益，辅助管理层精准决策。' },
          ]
    activeContentDraft.value.effectMetrics =
      Array.isArray(d.effectMetrics) && d.effectMetrics.length
        ? clone(d.effectMetrics)
        : [
            { displayValue: '75%', label: '高风险行为发生率下降' },
            { displayValue: '30%', label: '运营管理效率提升' },
            { displayValue: '99.2%', label: '全链路数据准确率' },
          ]
    activeContentDraft.value.roleTabs =
      Array.isArray(d.roleTabs) && d.roleTabs.length
        ? clone(
            d.roleTabs.map((r: any) => ({
              role: String(r.role ?? '角色'),
              headline: String(r.headline ?? ''),
              bullets: Array.isArray(r.bullets) ? r.bullets.map(String) : [],
            })),
          )
        : [
            { role: '企业老板', headline: '全局掌控经营利润与安全底线', bullets: ['核心经营大屏实时汇总', '异常成本波动自动归因预警', '行业对标分析辅助战略决策'] },
            { role: '安全/运营总监', headline: '标准化流程降低一线管控成本', bullets: ['高风险事件智能分级督办', '全流程履约节点可视化追踪', '考核报表一键自动生成'] },
            { role: '车队长/调度', headline: '移动化工具提升日常调度效率', bullets: ['车辆与人员状态实时在线', '异常情况手机端秒级接收处置'] },
            { role: '一线司机', headline: '贴心语音提醒保障行车安全', bullets: ['实时路况与风险语音伴驾', '电子回单免纸质交接更省心'] },
          ]
    activeContentDraft.value.terminalTools =
      Array.isArray(d.terminalTools) && d.terminalTools.length
        ? clone(d.terminalTools)
        : [
            { name: '智能车载终端', badge: 'AI 边缘计算', description: '高清双摄+北斗高精度定位，全天候采集行车数据' },
            { name: '移动管家小程序', badge: '轻量协同', description: '随时随地查看车辆位置、接收预警并完成闭环处理' },
            { name: '企业管理工作台', badge: 'SaaS 看板', description: '多维数据报表与智能规则引擎，支持个性化配置' },
          ]
    activeContentDraft.value.pains = Array.isArray(d.pains) ? clone(d.pains) : []
    activeContentDraft.value.benefits = Array.isArray(d.benefits) ? clone(d.benefits) : []
    activeContentDraft.value.outcomes = Array.isArray(d.outcomes) ? clone(d.outcomes) : []
    activeContentDraft.value.relatedCaseIds = Array.isArray(d.relatedCaseIds)
      ? d.relatedCaseIds.map(Number)
      : []
    activeContentDraft.value.relatedProductIds = Array.isArray(d.relatedProductIds)
      ? d.relatedProductIds.map(Number)
      : []
    activeContentDraft.value.relatedSolutionIds = Array.isArray(d.relatedSolutionIds)
      ? d.relatedSolutionIds.map(Number)
      : []

    activeContentBaseline.value = JSON.stringify(activeContentDraft.value)
  } catch (err) {
    ElMessage.error(err instanceof ApiError ? err.message : '加载内容详情失败')
  } finally {
    contentDetailLoading.value = false
  }
}

function closeContentDetail() {
  activeContentId.value = ''
  activeContentMeta.value = null
  activeContentBaseline.value = ''
}

function buildContentPayload(type: ContentType): Record<string, unknown> {
  const base: Record<string, unknown> = {
    ...activeContentRawBackup.value,
    title: activeContentDraft.value.title.trim() || '未命名内容',
    summary: activeContentDraft.value.summary.trim(),
    coverMediaId: activeContentDraft.value.coverMediaId,
    coverAlt: activeContentDraft.value.coverAlt.trim() || activeContentDraft.value.title.trim(),
    categoryId: activeContentDraft.value.categoryId ? Number(activeContentDraft.value.categoryId) : null,
    sortOrder: Number(activeContentDraft.value.sortOrder || 100),
    bodyHtml: activeContentDraft.value.bodyHtml,
    seo: {
      title: activeContentDraft.value.seoTitle.trim(),
      description: activeContentDraft.value.seoDescription.trim(),
    },
  }
  if (type === 'PRODUCT') {
    base.heroMediaId = activeContentDraft.value.heroMediaId || activeContentDraft.value.coverMediaId
    base.features = activeContentDraft.value.features
    base.hardwareModules = activeContentDraft.value.features
    base.effectMetrics = activeContentDraft.value.effectMetrics
    base.roleTabs = activeContentDraft.value.roleTabs
    base.terminalTools = activeContentDraft.value.terminalTools
    base.relatedSolutionIds = activeContentDraft.value.relatedSolutionIds
    base.relatedCaseIds = activeContentDraft.value.relatedCaseIds
  } else if (type === 'SOLUTION') {
    base.heroMediaId = activeContentDraft.value.heroMediaId || activeContentDraft.value.coverMediaId
    base.industry = activeContentDraft.value.industry.trim()
    base.pains = activeContentDraft.value.pains
    base.benefits = activeContentDraft.value.benefits
    base.relatedProductIds = activeContentDraft.value.relatedProductIds
    base.relatedCaseIds = activeContentDraft.value.relatedCaseIds
  } else if (type === 'CASE') {
    base.industry = activeContentDraft.value.industry.trim()
    base.customerName = activeContentDraft.value.customerName.trim()
    base.quoteText = activeContentDraft.value.quoteText.trim()
    base.quoteAuthor = activeContentDraft.value.quoteAuthor.trim()
    base.outcomes = activeContentDraft.value.outcomes
    base.relatedProductIds = activeContentDraft.value.relatedProductIds
    base.relatedSolutionIds = activeContentDraft.value.relatedSolutionIds
  } else if (type === 'NEWS') {
    base.author = activeContentDraft.value.author.trim()
    base.displayTime = activeContentDraft.value.displayTime.trim()
    base.sourceName = activeContentDraft.value.sourceName.trim()
    base.sourceUrl = activeContentDraft.value.sourceUrl.trim()
  }
  return base
}

async function saveActiveContentDraft(silent = false): Promise<boolean> {
  if (!activeContentId.value || !activeContentMeta.value) return false
  const type = activeContentMeta.value.contentType as ContentType
  if (
    type === 'CASE' &&
    Boolean(activeContentDraft.value.quoteText.trim()) !== Boolean(activeContentDraft.value.quoteAuthor.trim())
  ) {
    ElMessage.warning('客户证言（quoteText）与证言人（quoteAuthor）需成对填写')
    return false
  }
  try {
    const payload = buildContentPayload(type)
    const updated = await saveContentDraft(
      activeContentId.value,
      payload,
      activeContentMeta.value.lockVersion,
    )
    activeContentMeta.value = updated.content
    activeContentRawBackup.value = { ...updated.draft }
    contentDetailsCache[String(updated.content.id)] = updated.draft
    activeContentBaseline.value = JSON.stringify(activeContentDraft.value)

    const list = contentsMap[type] || []
    const idx = list.findIndex((x) => String(x.id) === String(updated.content.id))
    if (idx >= 0) list[idx] = updated.content
    if (!silent) {
      ElMessage.success(`已保存「${activeContentDraft.value.title}」内容草稿`)
    }
    return true
  } catch (err) {
    ElMessage.error(err instanceof ApiError ? err.message : '保存详情草稿失败')
    return false
  }
}

async function publishActiveContentDraft() {
  if (!activeContentId.value || !activeContentMeta.value) return
  const saved = await saveActiveContentDraft(true)
  if (!saved || !activeContentMeta.value) return
  try {
    const type = activeContentMeta.value.contentType as ContentType
    if (activeContentDraft.value.categoryId && PAGE_TO_CATEGORY_TYPE[activePage.value]) {
      const catType = PAGE_TO_CATEGORY_TYPE[activePage.value]!
      const cat = categoriesMap[catType].find(
        (c) => String(c.id) === String(activeContentDraft.value.categoryId),
      )
      if (cat && (cat.status !== 'PUBLISHED' || cat.hasUnpublishedChanges)) {
        await publishCategory(cat.id, cat.lockVersion, cat.draftVersion)
      }
    }
    await publishContent(
      activeContentMeta.value.id,
      activeContentMeta.value.lockVersion,
      activeContentMeta.value.draftVersion,
    )
    const refreshed = await getContent(activeContentMeta.value.id)
    activeContentMeta.value = refreshed.content
    const list = contentsMap[type] || []
    const idx = list.findIndex((x) => String(x.id) === String(refreshed.content.id))
    if (idx >= 0) list[idx] = refreshed.content
    ElMessage.success(`「${activeContentDraft.value.title}」已正式发布上线`)
  } catch (err) {
    ElMessage.error(err instanceof ApiError ? err.message : '发布内容失败')
  }
}

async function createNewContentInStudio(type: ContentType) {
  const labelMap: Record<ContentType, string> = {
    PRODUCT: '新产品',
    SOLUTION: '新解决方案',
    CASE: '新客户案例',
    NEWS: '新新闻资讯',
    CUSTOMER: '新合作客户',
  }
  try {
    const { value: title } = await ElMessageBox.prompt(
      `请输入${labelMap[type]}标题（创建后将直接在画布中打开实景编辑）：`,
      `新建${labelMap[type]}`,
      {
        confirmButtonText: '创建并编辑',
        cancelButtonText: '取消',
        inputPlaceholder: '例如：智能安全管家 3.0',
        inputValidator: (v) => (v && v.trim() ? true : '标题不能为空'),
      },
    )
    const catType = type === 'CUSTOMER' ? null : (type as CategoryType)
    const defaultCatId =
      catType && categoriesMap[catType]?.length ? Number(categoriesMap[catType][0].id) : null
    const draftPayload: Record<string, unknown> = {
      title: title.trim(),
      summary: '点击此处直接在画布中输入摘要描述...',
      categoryId: defaultCatId,
      sortOrder: 100,
      bodyHtml: '<p>在此输入正文详细介绍内容...</p>',
    }
    if (type === 'CASE') {
      draftPayload.customerName = title.trim()
      draftPayload.industry = '智慧物流'
      draftPayload.outcomes = [{ displayValue: '35%', label: '运营效率提升' }]
    }
    if (type === 'NEWS') {
      draftPayload.displayTime = new Date().toISOString().slice(0, 16).replace('T', ' ')
      draftPayload.author = '官方资讯'
    }
    const created = await createContent(type, draftPayload)
    contentsMap[type].unshift(created.content)
    contentDetailsCache[String(created.content.id)] = created.draft
    ElMessage.success(`已创建「${title.trim()}」`)
    if (type !== 'CUSTOMER') {
      await openContentInStudio(type, created.content.id)
    }
  } catch {
    // 用户取消
  }
}

// ==================== 分类栏原位新建 / 改名 / 发布 ====================
async function addCategoryInline(catType: CategoryType) {
  try {
    const { value: name } = await ElMessageBox.prompt(
      '请输入新分类名称（创建后自动保存并发布上线）：',
      '新建分类标签',
      {
        confirmButtonText: '立即创建并发布',
        cancelButtonText: '取消',
        inputValidator: (v) => (v && v.trim() ? true : '分类名称不能为空'),
      },
    )
    await createCategory(catType, name.trim(), 100)
    const refreshed = await listCategories(catType)
    categoriesMap[catType] = refreshed.items || []
    const newest = categoriesMap[catType].find((c) => c.name === name.trim())
    if (newest && newest.status !== 'PUBLISHED') {
      await publishCategory(newest.id, newest.lockVersion, newest.draftVersion)
      const again = await listCategories(catType)
      categoriesMap[catType] = again.items || []
    }
    ElMessage.success(`分类「${name.trim()}」已创建并发布`)
  } catch {
    // 用户取消
  }
}

async function renameCategoryInline(catType: CategoryType, cat: CategoryView) {
  try {
    const { value: name } = await ElMessageBox.prompt('修改分类标签显示名称：', '重命名分类', {
      confirmButtonText: '保存并发布',
      cancelButtonText: '取消',
      inputValue: cat.name,
      inputValidator: (v) => (v && v.trim() ? true : '名称不能为空'),
    })
    await saveCategory(cat.id, name.trim(), cat.sortOrder ?? 100, cat.lockVersion)
    const afterSave = await listCategories(catType)
    const updatedCat = (afterSave.items || []).find((c) => String(c.id) === String(cat.id))
    if (updatedCat) {
      await publishCategory(updatedCat.id, updatedCat.lockVersion, updatedCat.draftVersion)
    }
    const finalRes = await listCategories(catType)
    categoriesMap[catType] = finalRes.items || []
    ElMessage.success(`分类已更新为「${name.trim()}」`)
  } catch {
    // 用户取消
  }
}

// ==================== 媒体与内容辅助方法 ====================
function resolveMediaUrl(mediaId?: number | string | null): string {
  if (!mediaId) return ''
  return `/api/admin/media/${mediaId}/file`
}

function getContentItem(type: ContentType, id: number | string): ContentView | undefined {
  return (contentsMap[type] || []).find((x) => String(x.id) === String(id))
}

function getContentData(item?: ContentView): Record<string, any> {
  if (!item) return {}
  const cached = contentDetailsCache[String(item.id)] || {}
  return {
    ...cached,
    id: item.id,
    title: cached.title ?? item.title,
    summary: cached.summary ?? '',
    categoryId: cached.categoryId ?? item.categoryId,
    categoryName: item.categoryName,
    displayTime: cached.displayTime ?? item.displayTime,
    coverMediaId: cached.coverMediaId ?? item.coverMediaId,
    industry: cached.industry ?? item.categoryName ?? '标杆案例',
    customerName: cached.customerName ?? item.title,
  }
}

function getCategoryName(catType: CategoryType, catId?: number | string | null): string {
  if (!catId) return '默认分类'
  const found = (categoriesMap[catType] || []).find((c) => String(c.id) === String(catId))
  return found?.name || '默认分类'
}

// 当前视图数据源（编辑/预览态使用 drafts，已发布对比态使用 published）
const activeSite = computed<SiteDraft>(() => {
  if (studioMode.value === 'published' && configViews.SITE?.published) {
    return normalizeSite(configViews.SITE.published)
  }
  return drafts.value.SITE
})

const activeNav = computed<NavigationDraft>(() => {
  if (studioMode.value === 'published' && configViews.NAVIGATION?.published) {
    return normalizeNav(configViews.NAVIGATION.published)
  }
  return drafts.value.NAVIGATION
})

const activeHome = computed<HomeDraft>(() => {
  if (studioMode.value === 'published' && configViews.HOME?.published) {
    return normalizeHome(configViews.HOME.published)
  }
  return drafts.value.HOME
})

const activeAbout = computed<AboutDraft>(() => {
  if (studioMode.value === 'published' && configViews.ABOUT?.published) {
    return normalizeAbout(configViews.ABOUT.published)
  }
  return drafts.value.ABOUT
})

const activeContact = computed<ContactDraft>(() => {
  if (studioMode.value === 'published' && configViews.CONTACT?.published) {
    return normalizeContact(configViews.CONTACT.published)
  }
  return drafts.value.CONTACT
})

const activePrivacy = computed<PrivacyDraft>(() => {
  if (studioMode.value === 'published' && configViews.PRIVACY?.published) {
    return normalizePrivacy(configViews.PRIVACY.published)
  }
  return drafts.value.PRIVACY
})

const currentHero = computed<HeroItem | null>(() => {
  const list = activeHome.value.hero.items
  if (!list.length) return null
  const idx = Math.min(heroIndex.value, list.length - 1)
  return list[idx] || list[0]
})

const resolvedHomeNews = computed<ContentView[]>(() => {
  const cfg = activeHome.value.news
  if ((cfg.mode === 'MANUAL' || cfg.mode === 'SELECTED') && cfg.contentIds?.length) {
    return cfg.contentIds
      .map((id) => getContentItem('NEWS', id))
      .filter((x): x is ContentView => Boolean(x))
  }
  return (contentsMap.NEWS || []).slice(0, cfg.limit || 3)
})

function getFilteredContents(type: ContentType): ContentView[] {
  const list = contentsMap[type] || []
  if (activeCategoryFilter.value === 'ALL') return list
  return list.filter((it) => String(it.categoryId || '') === String(activeCategoryFilter.value))
}

const pinnedHeadlineNews = computed<ContentView | null>(() => {
  const list = getFilteredContents('NEWS')
  return list.length ? list[0] : null
})

const gridNewsItems = computed<ContentView[]>(() => {
  const list = getFilteredContents('NEWS')
  return list.length > 1 ? list.slice(1) : []
})

// ==================== 首页及各页面区块增删排序操作 ====================
function moveSection(index: number, delta: number) {
  const list = drafts.value.HOME.sectionOrder
  const target = index + delta
  if (target < 0 || target >= list.length) return
  const [item] = list.splice(index, 1)
  list.splice(target, 0, item)
}

function addHeroSlide() {
  drafts.value.HOME.hero.items.push({
    enabled: true,
    sortOrder: (drafts.value.HOME.hero.items.length + 1) * 10,
    title: '新轮播主标题',
    subtitle: '点击文字即可直接在画布中编辑副标题描述',
    imageAlt: '轮播背景',
    titleColor: '',
    subtitleColor: '',
    kickerColor: '',
    buttonBgColor: '',
    buttonTextColor: '',
    buttonText: '立即了解',
    link: { type: 'PAGE', pageCode: 'PRODUCTS' },
  })
  heroIndex.value = drafts.value.HOME.hero.items.length - 1
}

function removeHeroSlide(idx: number) {
  if (drafts.value.HOME.hero.items.length <= 1) {
    ElMessage.warning('至少保留一张焦点图')
    return
  }
  drafts.value.HOME.hero.items.splice(idx, 1)
  if (heroIndex.value >= drafts.value.HOME.hero.items.length) {
    heroIndex.value = drafts.value.HOME.hero.items.length - 1
  }
}

function addServiceGroup() {
  drafts.value.HOME.services.groups.push({
    sortOrder: (drafts.value.HOME.services.groups.length + 1) * 10,
    title: '新业务分组',
    tag: 'AI + IoT 核心场景',
    description: '在此输入该业务分组的核心场景描述',
    highlights: ['安全可控', '降本增效'],
    highlightsText: '安全可控 / 降本增效',
    layout: 'auto',
    contentType: 'PRODUCT',
    contentIds: [],
    moreLink: { type: 'PAGE', pageCode: 'PRODUCTS' },
  })
}

// 导航链接在画布内的实景跳转
function navigateLinkInStudio(link?: LinkValue | null) {
  if (!link) return
  const rawPage = String((link as any).pageCode || (link as any).pageKey || '').toUpperCase()
  if ((link.type === 'PAGE' || link.type === 'INTERNAL_PAGE') && rawPage) {
    if (['HOME', 'PRODUCTS', 'SOLUTIONS', 'CASES', 'NEWS', 'ABOUT', 'CONTACT', 'PRIVACY'].includes(rawPage)) {
      switchStudioPage(rawPage as StudioPageKey)
      return
    }
  }
  if ((link.type === 'CONTENT' || link.type === 'CONTENT_DETAIL') && link.contentType && link.contentId) {
    openContentInStudio(link.contentType as ContentType, link.contentId)
    return
  }
  if (link.type === 'CONSULT') {
    switchStudioPage('CONTACT')
  }
}

// ==================== 全站发布前健康巡检 ====================
interface HealthIssue {
  level: 'error' | 'warning'
  module: string
  page: StudioPageKey
  message: string
}

const healthIssues = computed<HealthIssue[]>(() => {
  const issues: HealthIssue[] = []
  if (!drafts.value.SITE.siteName?.trim()) {
    issues.push({ level: 'error', module: '站点配置', page: 'HOME', message: '站点名称不能为空' })
  }
  drafts.value.HOME.hero.items.forEach((h, i) => {
    if (!h.title?.trim()) {
      issues.push({
        level: 'error',
        module: `首页焦点图 #${i + 1}`,
        page: 'HOME',
        message: '焦点图主标题为空',
      })
    }
  })
  drafts.value.HOME.services.groups.forEach((g, i) => {
    for (const cid of g.contentIds || []) {
      const item = getContentItem((g.contentType as ContentType) || 'PRODUCT', cid)
      if (!item) {
        issues.push({
          level: 'error',
          module: `首页分组「${g.title || i + 1}」`,
          page: 'HOME',
          message: `引用的内容 #${cid} 不存在`,
        })
      } else if (item.status !== 'PUBLISHED') {
        issues.push({
          level: 'warning',
          module: `首页分组「${g.title || i + 1}」`,
          page: 'HOME',
          message: `引用的「${item.title || cid}」尚未发布，一键发布时将自动同步发布`,
        })
      }
    }
  })
  if (!drafts.value.ABOUT.intro?.trim()) {
    issues.push({ level: 'warning', module: '关于我们', page: 'ABOUT', message: '企业简介正文为空' })
  }
  if (!drafts.value.CONTACT.phone?.trim() && !drafts.value.CONTACT.email?.trim()) {
    issues.push({ level: 'warning', module: '联系我们', page: 'CONTACT', message: '联系电话与邮箱均为空' })
  }
  return issues
})

// ==================== 一键保存草稿 & 一键全站发布 ====================
function buildSavePayload(key: ConfigKey) {
  if (key === 'HOME') {
    const h = clone(drafts.value.HOME)
    h.services.groups = (h.services.groups || []).map((g: ServiceGroup) => {
      const highlights = String(g.highlightsText || '')
        .split(/[\/,，\n]+/)
        .map((s) => s.trim())
        .filter(Boolean)
      return { ...g, highlights }
    })
    return h
  }
  return clone(drafts.value[key])
}

async function saveAllDrafts(silent = false): Promise<boolean> {
  if (totalDirtyCount.value === 0 && !silent) {
    ElMessage.info('当前没有未保存的草稿改动')
    return true
  }
  savingAll.value = true
  try {
    for (const key of dirtyConfigKeys.value) {
      const view = configViews[key]
      if (!view) continue
      const payload = buildSavePayload(key)
      const updated = await saveConfigDraft(key, payload, view.lockVersion)
      ;(configViews as Record<string, unknown>)[key] = updated
      baselineJson[key] = JSON.stringify(drafts.value[key])
    }
    if (isActiveContentDirty.value) {
      const ok = await saveActiveContentDraft(true)
      if (!ok) return false
    }
    if (!silent) {
      ElMessage.success('全站草稿修改已同步保存！')
    }
    return true
  } catch (err) {
    ElMessage.error(err instanceof ApiError ? err.message : '保存草稿失败')
    return false
  } finally {
    savingAll.value = false
  }
}

async function saveAndPublishAll() {
  if (!isAdmin.value) {
    ElMessage.warning('当前为编辑角色，仅管理员可执行发布')
    return
  }
  const blockingErrors = healthIssues.value.filter((i) => i.level === 'error')
  if (blockingErrors.length > 0) {
    healthDrawerOpen.value = true
    ElMessage.error(`发现 ${blockingErrors.length} 项阻断性问题，请先修复后再发布`)
    return
  }

  publishingAll.value = true
  try {
    // 1. 先保存所有脏配置草稿与当前正在编辑的内容详情
    const ok = await saveAllDrafts(true)
    if (!ok) return

    if (activeContentId.value && activeContentMeta.value && activeContentMeta.value.hasUnpublishedChanges) {
      await publishContent(
        activeContentMeta.value.id,
        activeContentMeta.value.lockVersion,
        activeContentMeta.value.draftVersion,
      )
    }

    // 2. 自动发布首页引用的未发布内容项（避免后端引用校验拦截）
    for (const g of drafts.value.HOME.services.groups) {
      const cType = (g.contentType as ContentType) || 'PRODUCT'
      for (const cid of g.contentIds || []) {
        const item = getContentItem(cType, cid)
        if (item && (item.status !== 'PUBLISHED' || item.hasUnpublishedChanges)) {
          await publishContent(item.id, item.lockVersion, item.draftVersion)
        }
      }
    }
    for (const cid of drafts.value.HOME.cases.contentIds || []) {
      const item = getContentItem('CASE', cid)
      if (item && (item.status !== 'PUBLISHED' || item.hasUnpublishedChanges)) {
        await publishContent(item.id, item.lockVersion, item.draftVersion)
      }
    }
    for (const cid of drafts.value.HOME.customers.contentIds || []) {
      const item = getContentItem('CUSTOMER', cid)
      if (item && (item.status !== 'PUBLISHED' || item.hasUnpublishedChanges)) {
        await publishContent(item.id, item.lockVersion, item.draftVersion)
      }
    }

    // 3. 按依赖顺序发布所有有未发布改动的全局配置
    const toPublish: ConfigKey[] = ['SITE', 'NAVIGATION', 'ABOUT', 'CONTACT', 'PRIVACY', 'HOME']
    for (const key of toPublish) {
      const v = configViews[key]
      if (v && v.hasUnpublishedChanges) {
        const res = await publishConfig(key, v.lockVersion, v.draftVersion)
        ;(configViews as Record<string, unknown>)[key] = res
      }
    }

    ElMessage.success('🎉 全站配置与内容已同步发布上线！')
    await loadAll()
  } catch (err) {
    ElMessage.error(err instanceof ApiError ? err.message : '发布失败，请检查关联项状态')
  } finally {
    publishingAll.value = false
  }
}

onMounted(() => {
  loadAll()
  window.addEventListener('keydown', onKeyDown)
  void openMediaPicker
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', onKeyDown)
})
</script>

<template>
  <div class="live-studio" v-loading="loading">
    <!-- ==================== 顶部第一层：全局主控工具栏 ==================== -->
    <header class="studio-topbar">
      <div class="studio-topbar__left">
        <div class="studio-brand">
          <span class="studio-brand__dot"></span>
          <span class="studio-brand__title">全站实景交互工作台</span>
        </div>

        <div class="studio-mode-switch">
          <button
            type="button"
            class="mode-btn"
            :class="{ active: studioMode === 'edit' }"
            @click="studioMode = 'edit'"
          >
            ✏️ 实景编辑
          </button>
          <button
            type="button"
            class="mode-btn"
            :class="{ active: studioMode === 'preview' }"
            @click="studioMode = 'preview'"
          >
            👁️ 净版预览
          </button>
          <button
            type="button"
            class="mode-btn"
            :class="{ active: studioMode === 'published' }"
            @click="studioMode = 'published'"
          >
            🌐 线上已发布对比
          </button>
        </div>

        <div class="studio-viewport-switch">
          <button
            type="button"
            class="vp-btn"
            :class="{ active: viewport === 'desktop' }"
            @click="viewport = 'desktop'"
            title="桌面端 (100%)"
          >
            🖥️ 桌面
          </button>
          <button
            type="button"
            class="vp-btn"
            :class="{ active: viewport === 'tablet' }"
            @click="viewport = 'tablet'"
            title="平板端 (820px)"
          >
            📱 平板
          </button>
          <button
            type="button"
            class="vp-btn"
            :class="{ active: viewport === 'mobile' }"
            @click="viewport = 'mobile'"
            title="手机端 (414px)"
          >
            📲 手机
          </button>
        </div>

        <div class="studio-history-btns" v-if="studioMode === 'edit'">
          <el-tooltip content="撤销上一步修改 (Ctrl+Z)" placement="bottom">
            <el-button size="small" :disabled="!canUndo" @click="undo">↩ 撤销</el-button>
          </el-tooltip>
          <el-tooltip content="重做修改 (Ctrl+Y)" placement="bottom">
            <el-button size="small" :disabled="!canRedo" @click="redo">↪ 重做</el-button>
          </el-tooltip>
          <el-button
            size="small"
            :type="showLeftOutline ? 'primary' : 'default'"
            plain
            @click="showLeftOutline = !showLeftOutline"
          >
            {{ showLeftOutline ? '◀ 收起大纲' : '▶ 展开大纲' }}
          </el-button>
        </div>
      </div>

      <div class="studio-topbar__right">
        <div class="dirty-indicators">
          <el-tag
            v-if="totalDirtyCount > 0"
            type="warning"
            effect="dark"
            size="small"
            round
          >
            ● {{ totalDirtyCount }} 项未保存草稿
          </el-tag>
          <el-tag
            v-else-if="unpublishedConfigKeys.length > 0"
            type="primary"
            effect="light"
            size="small"
            round
          >
            {{ unpublishedConfigKeys.length }} 个模块待发布上线
          </el-tag>
          <el-tag v-else type="success" effect="light" size="small" round>
            ✓ 全站与线上一致
          </el-tag>
        </div>

        <el-badge
          :value="healthIssues.length"
          :hidden="healthIssues.length === 0"
          :type="hasHealthError ? 'danger' : 'warning'"
        >
          <el-button size="small" @click="healthDrawerOpen = true">🩺 发布巡检</el-button>
        </el-badge>

        <el-button
          size="small"
          type="primary"
          plain
          :loading="savingAll"
          :disabled="totalDirtyCount === 0 || studioMode === 'published'"
          @click="saveAllDrafts"
        >
          💾 保存全站草稿
        </el-button>

        <el-button
          size="small"
          type="primary"
          :loading="publishingAll"
          :disabled="studioMode === 'published'"
          @click="saveAndPublishAll"
        >
          🚀 一键全站发布上线
        </el-button>
      </div>
    </header>

    <!-- ==================== 顶部第二层：全站 8 大页面实景切换栏 ==================== -->
    <nav class="studio-pagebar">
      <div class="studio-pagebar__tabs">
        <button
          v-for="p in STUDIO_PAGES"
          :key="p.key"
          type="button"
          class="page-tab-btn"
          :class="{ active: activePage === p.key }"
          @click="switchStudioPage(p.key)"
        >
          <span class="page-tab-btn__icon">{{ p.icon }}</span>
          <span class="page-tab-btn__label">{{ p.label }}</span>
          <span
            v-if="
              (p.key === 'HOME' && dirtyConfigKeys.includes('HOME')) ||
              (p.key === 'ABOUT' && dirtyConfigKeys.includes('ABOUT')) ||
              (p.key === 'CONTACT' && dirtyConfigKeys.includes('CONTACT')) ||
              (p.key === 'PRIVACY' && dirtyConfigKeys.includes('PRIVACY'))
            "
            class="page-tab-btn__dirty"
            title="有未保存修改"
          ></span>
        </button>
      </div>

      <div class="studio-pagebar__context">
        <template v-if="activeContentId && activeContentMeta">
          <span class="detail-crumb-badge">
            📄 正在编辑详情：<strong>{{ activeContentDraft.title || '未命名' }}</strong>
            <el-tag
              size="small"
              :type="activeContentMeta.status === 'PUBLISHED' && !activeContentMeta.hasUnpublishedChanges && !isActiveContentDirty ? 'success' : 'warning'"
              style="margin-left: 6px"
            >
              {{
                isActiveContentDirty
                  ? '详情未保存'
                  : activeContentMeta.status === 'PUBLISHED' && !activeContentMeta.hasUnpublishedChanges
                    ? '已发布'
                    : '草稿待发布'
              }}
            </el-tag>
          </span>
          <el-button size="small" type="primary" plain @click="saveActiveContentDraft()">
            保存此详情
          </el-button>
          <el-button size="small" type="success" @click="publishActiveContentDraft()">
            发布此详情
          </el-button>
          <el-button size="small" @click="closeContentDetail">← 返回列表画布</el-button>
        </template>
        <template v-else>
          <span class="studio-pagebar__tip">
            💡 提示：点击画布任意带虚线框的文字即可直接输入修改；点击卡片或顶部菜单可直接在画布内穿透跳转。
          </span>
        </template>
      </div>
    </nav>

    <!-- ==================== 主体工作区：左侧动态大纲 + 中央全站实景画布 ==================== -->
    <div class="studio-workspace">
      <!-- 左侧动态结构大纲树（随当前页面自动切换） -->
      <aside v-if="showLeftOutline && studioMode === 'edit'" class="studio-outline">
        <!-- 1. 首页大纲 -->
        <template v-if="activePage === 'HOME'">
          <div class="outline-header">
            <span>🏠 首页模块与顺序</span>
            <el-checkbox v-model="showDisabledBlocks" size="small">显示已隐藏</el-checkbox>
          </div>
          <div class="outline-list">
            <div
              class="outline-item"
              :class="{ active: selectedSection === 'header' }"
              @click="selectedSection = 'header'"
            >
              <span class="outline-item__icon">🧭</span>
              <span class="outline-item__label">顶部导航与下拉大菜单</span>
              <el-tag size="small" type="info">全局</el-tag>
            </div>
            <div
              class="outline-item"
              :class="{ active: selectedSection === 'hero' }"
              @click="selectedSection = 'hero'"
            >
              <span class="outline-item__icon">🎞️</span>
              <span class="outline-item__label">Hero 焦点轮播 ({{ drafts.HOME.hero.items.length }}屏)</span>
            </div>

            <div class="outline-divider">可拖拽/上下调整模块顺序</div>

            <div
              v-for="(secKey, idx) in drafts.HOME.sectionOrder"
              :key="secKey"
              class="outline-item"
              :class="{
                active: selectedSection === secKey,
                'is-disabled': !drafts.HOME[secKey].enabled,
              }"
              @click="selectedSection = secKey"
            >
              <span class="outline-item__icon">{{ SECTION_META[secKey].icon }}</span>
              <span class="outline-item__label">{{ SECTION_META[secKey].label }}</span>
              <div class="outline-item__actions" @click.stop>
                <el-switch v-model="drafts.HOME[secKey].enabled" size="small" />
                <button
                  type="button"
                  class="mini-arrow"
                  :disabled="idx === 0"
                  @click="moveSection(idx, -1)"
                  title="上移模块"
                >
                  ↑
                </button>
                <button
                  type="button"
                  class="mini-arrow"
                  :disabled="idx === drafts.HOME.sectionOrder.length - 1"
                  @click="moveSection(idx, 1)"
                  title="下移模块"
                >
                  ↓
                </button>
              </div>
            </div>

            <div
              class="outline-item"
              :class="{ active: selectedSection === 'footer' }"
              @click="selectedSection = 'footer'"
            >
              <span class="outline-item__icon">⚓</span>
              <span class="outline-item__label">底部深色页脚与版权</span>
              <el-tag size="small" type="info">全局</el-tag>
            </div>
          </div>
        </template>

        <!-- 2. 产品 / 方案 / 案例 / 新闻 列表与详情大纲 -->
        <template
          v-else-if="
            activePage === 'PRODUCTS' ||
            activePage === 'SOLUTIONS' ||
            activePage === 'CASES' ||
            activePage === 'NEWS'
          "
        >
          <div class="outline-header">
            <span>
              {{ activePageInfo?.icon }}
              {{ activePageInfo?.label }}管理
            </span>
            <el-button
              size="small"
              type="primary"
              link
              @click="createNewContentInStudio(currentContentType)"
            >
              + 新建
            </el-button>
          </div>

          <div class="outline-list">
            <div class="outline-divider">
              <span>分类筛选栏</span>
              <el-button
                size="small"
                type="primary"
                link
                @click="addCategoryInline(currentCategoryType)"
              >
                + 新增分类
              </el-button>
            </div>
            <div
              class="outline-item"
              :class="{ active: activeCategoryFilter === 'ALL' && !activeContentId }"
              @click="
                closeContentDetail();
                activeCategoryFilter = 'ALL'
              "
            >
              <span class="outline-item__icon">📂</span>
              <span class="outline-item__label">全部 ({{ currentContents.length }})</span>
            </div>
            <div
              v-for="cat in currentCategories"
              :key="cat.id"
              class="outline-item"
              :class="{ active: String(activeCategoryFilter) === String(cat.id) && !activeContentId }"
              @click="
                closeContentDetail();
                activeCategoryFilter = String(cat.id)
              "
            >
              <span class="outline-item__icon">🏷️</span>
              <span class="outline-item__label">{{ cat.name }}</span>
              <div class="outline-item__actions" @click.stop>
                <button
                  type="button"
                  class="mini-arrow"
                  title="修改分类名"
                  @click="renameCategoryInline(currentCategoryType, cat)"
                >
                  ✏️
                </button>
              </div>
            </div>

            <div class="outline-divider">
              <span>点击条目进入详情实景编辑</span>
            </div>
            <div
              v-for="item in getFilteredContents(currentContentType)"
              :key="item.id"
              class="outline-item"
              :class="{ active: String(activeContentId) === String(item.id) }"
              @click="openContentInStudio(currentContentType, item.id)"
            >
              <span
                class="status-dot"
                :class="item.status === 'PUBLISHED' && !item.hasUnpublishedChanges ? 'is-pub' : 'is-draft'"
              ></span>
              <span class="outline-item__label">{{ item.title || '未命名' }}</span>
            </div>
          </div>
        </template>

        <!-- 3. 关于我们 5 大子页签与弹窗大纲 -->
        <template v-else-if="activePage === 'ABOUT'">
          <div class="outline-header">
            <span>🏢 关于我们 · 5大子页签</span>
          </div>
          <div class="outline-list">
            <div
              class="outline-item"
              :class="{ active: aboutSubTab === 'intro' }"
              @click="aboutSubTab = 'intro'"
            >
              <span class="outline-item__icon">📖</span>
              <span class="outline-item__label">1. 企业简介 & CEO信 & 地图</span>
            </div>
            <div
              class="outline-item"
              :class="{ active: aboutSubTab === 'team' }"
              @click="aboutSubTab = 'team'"
            >
              <span class="outline-item__icon">👔</span>
              <span class="outline-item__label">2. 管理团队 ({{ drafts.ABOUT.teamMembers.length }}人)</span>
            </div>
            <div
              class="outline-item"
              :class="{ active: aboutSubTab === 'media' }"
              @click="aboutSubTab = 'media'"
            >
              <span class="outline-item__icon">🎬</span>
              <span class="outline-item__label">3. 媒体中心</span>
            </div>
            <div
              class="outline-item"
              :class="{ active: aboutSubTab === 'resources' }"
              @click="aboutSubTab = 'resources'"
            >
              <span class="outline-item__icon">📚</span>
              <span class="outline-item__label">4. 资料共享 ({{ drafts.ABOUT.resources.length }}份)</span>
            </div>
            <div
              class="outline-item"
              :class="{ active: aboutSubTab === 'join' }"
              @click="aboutSubTab = 'join'"
            >
              <span class="outline-item__icon">🚀</span>
              <span class="outline-item__label">5. 加入我们 & 使命价值观</span>
            </div>

            <div class="outline-divider">快捷弹窗与子项操作</div>
            <div class="outline-item" @click="ceoModalOpen = true">
              <span class="outline-item__icon">✉️</span>
              <span class="outline-item__label">打开 CEO 致信全文弹窗编辑</span>
            </div>
            <div
              class="outline-item"
              @click="
                aboutSubTab = 'team';
                drafts.ABOUT.teamMembers.push({
                  name: '新高管姓名',
                  role: '职位头衔',
                  avatar: '',
                  bio: ['在此输入高管履历介绍...'],
                })
              "
            >
              <span class="outline-item__icon">➕</span>
              <span class="outline-item__label">新增管理团队成员</span>
            </div>
            <div
              class="outline-item"
              @click="
                aboutSubTab = 'intro';
                drafts.ABOUT.milestones.push({
                  year: '2026',
                  title: '新里程碑节点',
                  description: '在此输入里程碑事件描述',
                });
                aboutMilestoneIdx = drafts.ABOUT.milestones.length - 1
              "
            >
              <span class="outline-item__icon">➕</span>
              <span class="outline-item__label">新增发展历程年份节点</span>
            </div>
            <div
              class="outline-item"
              @click="
                aboutSubTab = 'intro';
                drafts.ABOUT.offices.push({
                  city: '新城市',
                  phone: '010-88888888',
                  address: '请输入详细办公地址',
                  mapX: 65,
                  mapY: 55,
                });
                aboutOfficeIdx = drafts.ABOUT.offices.length - 1
              "
            >
              <span class="outline-item__icon">📍</span>
              <span class="outline-item__label">新增全国办公网点城市</span>
            </div>
          </div>
        </template>

        <!-- 4. 联系我们 / 隐私说明大纲 -->
        <template v-else>
          <div class="outline-header">
            <span>{{ activePage === 'CONTACT' ? '📞 联系我们设置' : '🛡️ 隐私说明设置' }}</span>
          </div>
          <div class="outline-list" v-if="activePage === 'CONTACT'">
            <div class="outline-item">
              <span class="outline-item__label">启用右侧预约表单</span>
              <el-switch v-model="drafts.CONTACT.formEnabled" size="small" />
            </div>
            <div class="outline-item">
              <span class="outline-item__label">启用短信验证码行</span>
              <el-switch v-model="drafts.CONTACT.smsVerifyEnabled" size="small" />
            </div>
          </div>
          <div class="outline-list" v-else>
            <div class="outline-item">
              <span class="outline-item__label">支持在画布中直接编写 HTML 隐私条款并实时预览排版</span>
            </div>
          </div>
        </template>
      </aside>

      <!-- ==================== 中央全站实景交互画布 ==================== -->
      <main class="studio-canvas-wrap">
        <div class="studio-canvas" :class="[`vp-${viewport}`, `mode-${studioMode}`]">
          <!-- ==================== 全局公共顶栏：Header + 产品与解决方案下拉大菜单 ==================== -->
          <header
            class="canvas-header editable-zone"
            :class="{ 'is-selected': selectedSection === 'header' && studioMode === 'edit' }"
            @click="selectedSection = 'header'"
          >
            <div v-if="studioMode === 'edit'" class="zone-toolbar">
              <span class="zone-toolbar__tag">全局顶栏 & 导航 (SITE / NAVIGATION)</span>
              <el-popover placement="bottom-start" :width="520" trigger="click">
                <template #reference>
                  <button type="button" class="zone-btn" @click.stop>⚙️ 配置顶栏与下拉菜单</button>
                </template>
                <div class="popover-form">
                  <h4>站点标识与顶栏导航配置</h4>
                  <el-form label-position="top" size="small">
                    <el-row :gutter="10">
                      <el-col :span="12">
                        <el-form-item label="站点名称">
                          <el-input v-model="drafts.SITE.siteName" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="品牌口号">
                          <el-input v-model="drafts.SITE.slogan" />
                        </el-form-item>
                      </el-col>
                    </el-row>
                    <el-form-item label="Logo 媒体资源">
                      <el-select
                        v-model="drafts.SITE.logoMediaId"
                        clearable
                        placeholder="留空则显示品牌文字"
                        style="width: 100%"
                      >
                        <el-option
                          v-for="m in Object.values(mediaCache)"
                          :key="m.id"
                          :label="m.originalName || `#${m.id}`"
                          :value="Number(m.id)"
                        />
                      </el-select>
                    </el-form-item>
                    <div class="popover-subtitle">
                      <span>主导航菜单项</span>
                      <el-button
                        size="small"
                        type="primary"
                        link
                        @click="drafts.NAVIGATION.items.push({ label: '新菜单', enabled: true, sortOrder: (drafts.NAVIGATION.items.length + 1) * 10, link: { type: 'PAGE', pageCode: 'HOME' } })"
                      >
                        + 添加菜单
                      </el-button>
                    </div>
                    <div
                      v-for="(navIt, nIdx) in drafts.NAVIGATION.items"
                      :key="nIdx"
                      class="mini-row"
                    >
                      <el-input v-model="navIt.label" placeholder="菜单名" style="width: 120px" />
                      <el-select
                        v-if="navIt.link"
                        v-model="navIt.link.pageCode"
                        placeholder="目标页面"
                        style="width: 140px"
                      >
                        <el-option label="首页 (HOME)" value="HOME" />
                        <el-option label="产品与方案 (PRODUCTS)" value="PRODUCTS" />
                        <el-option label="解决方案 (SOLUTIONS)" value="SOLUTIONS" />
                        <el-option label="客户案例 (CASES)" value="CASES" />
                        <el-option label="新闻中心 (NEWS)" value="NEWS" />
                        <el-option label="关于我们 (ABOUT)" value="ABOUT" />
                        <el-option label="联系我们 (CONTACT)" value="CONTACT" />
                      </el-select>
                      <el-switch v-model="navIt.enabled" />
                      <el-button
                        type="danger"
                        link
                        @click="drafts.NAVIGATION.items.splice(nIdx, 1)"
                      >
                        删除
                      </el-button>
                    </div>
                  </el-form>
                </div>
              </el-popover>
              <button
                type="button"
                class="zone-btn"
                @click.stop="openMegaPreview = !openMegaPreview"
              >
                {{ openMegaPreview ? '🔼 收起下拉大菜单' : '🔽 展开编辑「产品与解决方案」下拉菜单' }}
              </button>
            </div>

            <div class="canvas-header__inner">
              <div class="canvas-logo" @click.stop="switchStudioPage('HOME')">
                <img
                  v-if="resolveMediaUrl(activeSite.logoMediaId)"
                  :src="resolveMediaUrl(activeSite.logoMediaId)"
                  :alt="activeSite.logoAlt || activeSite.siteName"
                  class="canvas-logo__img"
                />
                <span
                  v-else
                  class="canvas-logo__text"
                  :contenteditable="studioMode === 'edit'"
                  @blur="
                    drafts.SITE.siteName =
                      ($event.target as HTMLElement).innerText.trim() || drafts.SITE.siteName
                  "
                  @click.stop
                >
                  {{ activeSite.siteName }}
                </span>
              </div>

              <nav class="canvas-nav">
                <template v-for="(item, idx) in activeNav.items" :key="idx">
                  <div
                    v-if="item.enabled"
                    class="canvas-nav__item"
                    :class="{
                      active:
                        item.link?.pageCode &&
                        activePage === item.link.pageCode.toUpperCase(),
                    }"
                    @click.stop="navigateLinkInStudio(item.link)"
                  >
                    <span
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        drafts.NAVIGATION.items[idx].label =
                          ($event.target as HTMLElement).innerText.trim() ||
                          drafts.NAVIGATION.items[idx].label
                      "
                      @click.stop="
                        studioMode !== 'edit' ? navigateLinkInStudio(item.link) : undefined
                      "
                    >
                      {{ item.label }}
                    </span>
                    <button
                      type="button"
                      class="canvas-nav__jump"
                      title="在画布中切换到此页面"
                      @click.stop="navigateLinkInStudio(item.link)"
                    >
                      ↗
                    </button>
                  </div>
                </template>
              </nav>

              <div class="canvas-header__actions">
                <span class="canvas-lang">English</span>
                <button
                  type="button"
                  class="canvas-trial-btn"
                  @click.stop="switchStudioPage('CONTACT')"
                >
                  产品试用
                </button>
              </div>
            </div>

            <!-- 下拉 MegaMenu 实景编辑区 -->
            <div v-if="openMegaPreview" class="canvas-mega" @click.stop>
              <div class="canvas-mega__grid">
                <div
                  v-for="(col, cIdx) in drafts.NAVIGATION.megaColumns"
                  :key="cIdx"
                  class="canvas-mega__col"
                >
                  <div class="canvas-mega__col-head">
                    <input
                      v-model="col.title"
                      class="inline-clean-input strong"
                      placeholder="分栏标题（如 AI产品）"
                    />
                    <el-button
                      v-if="studioMode === 'edit'"
                      type="danger"
                      link
                      size="small"
                      @click="drafts.NAVIGATION.megaColumns.splice(cIdx, 1)"
                    >
                      删栏
                    </el-button>
                  </div>
                  <div class="canvas-mega__links">
                    <div
                      v-for="(lnk, lIdx) in col.items"
                      :key="lIdx"
                      class="canvas-mega__link-row"
                    >
                      <input
                        v-model="lnk.label"
                        class="inline-clean-input"
                        placeholder="子项名称"
                      />
                      <button
                        type="button"
                        class="mini-jump-btn"
                        title="在画布中打开目标"
                        @click="navigateLinkInStudio(lnk.link)"
                      >
                        跳转
                      </button>
                      <button
                        v-if="studioMode === 'edit'"
                        type="button"
                        class="mini-del-btn"
                        @click="col.items.splice(lIdx, 1)"
                      >
                        ×
                      </button>
                    </div>
                    <el-button
                      v-if="studioMode === 'edit'"
                      size="small"
                      type="primary"
                      link
                      @click="
                        col.items.push({ label: '新子项', path: '/products', desc: '', link: { type: 'PAGE', pageCode: 'PRODUCTS' } })
                      "
                    >
                      + 添加子项
                    </el-button>
                  </div>
                </div>
                <div v-if="studioMode === 'edit'" class="canvas-mega__add-col">
                  <el-button
                    size="small"
                    type="primary"
                    plain
                    @click="
                      drafts.NAVIGATION.megaColumns.push({ title: '新下拉分栏', items: [{ label: '示例产品', path: '/products', desc: '', link: { type: 'PAGE', pageCode: 'PRODUCTS' } }] })
                    "
                  >
                    + 新增下拉分栏
                  </el-button>
                </div>
              </div>
            </div>
          </header>

          <!-- =========================================================================
               页面 1：首页 (activePage === 'HOME') —— 严格对齐最新 GitHub main 分支
               ========================================================================= -->
          <template v-if="activePage === 'HOME'">
            <!-- Hero 焦点轮播区 -->
            <section
              class="canvas-hero editable-zone"
              :class="{ 'is-selected': selectedSection === 'hero' && studioMode === 'edit' }"
              :style="
                currentHero && resolveMediaUrl(currentHero.desktopMediaId)
                  ? {
                      backgroundImage: `linear-gradient(90deg, rgba(11,15,25,0.82) 0%, rgba(11,15,25,0.45) 100%), url(${resolveMediaUrl(currentHero.desktopMediaId)})`,
                    }
                  : undefined
              "
              @click="selectedSection = 'hero'"
            >
              <div v-if="studioMode === 'edit'" class="zone-toolbar">
                <span class="zone-toolbar__tag">
                  首页 Hero 焦点图 ({{ heroIndex + 1 }} / {{ drafts.HOME.hero.items.length }})
                </span>
                <button type="button" class="zone-btn" @click.stop="addHeroSlide">
                  + 新增轮播屏
                </button>
                <button
                  v-if="activeHome.hero.items.length > 1"
                  type="button"
                  class="zone-btn danger"
                  @click.stop="removeHeroSlide(heroIndex)"
                >
                  🗑️ 删除当前屏
                </button>
                <el-popover
                  v-if="currentHero"
                  placement="bottom-start"
                  :width="440"
                  trigger="click"
                >
                  <template #reference>
                    <button type="button" class="zone-btn" @click.stop>
                      🎨 背景图 / 视频 / 颜色设置
                    </button>
                  </template>
                  <div class="popover-form" v-if="currentHero">
                    <h4>当前焦点屏样式与跳转配置</h4>
                    <el-form label-position="top" size="small">
                      <el-form-item label="背景图媒体">
                        <el-select
                          v-model="drafts.HOME.hero.items[heroIndex].desktopMediaId"
                          clearable
                          placeholder="选择背景大图"
                          style="width: 100%"
                        >
                          <el-option
                            v-for="m in Object.values(mediaCache)"
                            :key="m.id"
                            :label="m.originalName || `#${m.id}`"
                            :value="Number(m.id)"
                          />
                        </el-select>
                      </el-form-item>
                      <el-form-item label="背景视频 URL（可选 MP4）">
                        <el-input
                          v-model="drafts.HOME.hero.items[heroIndex].videoUrl"
                          placeholder="https://.../hero.mp4"
                        />
                      </el-form-item>
                      <el-row :gutter="10">
                        <el-col :span="8">
                          <el-form-item label="眉标颜色">
                            <el-color-picker v-model="drafts.HOME.hero.items[heroIndex].kickerColor" />
                          </el-form-item>
                        </el-col>
                        <el-col :span="8">
                          <el-form-item label="主标题颜色">
                            <el-color-picker v-model="drafts.HOME.hero.items[heroIndex].titleColor" />
                          </el-form-item>
                        </el-col>
                        <el-col :span="8">
                          <el-form-item label="副标题颜色">
                            <el-color-picker v-model="drafts.HOME.hero.items[heroIndex].subtitleColor" />
                          </el-form-item>
                        </el-col>
                      </el-row>
                      <el-row :gutter="10">
                        <el-col :span="12">
                          <el-form-item label="按钮背景色">
                            <el-color-picker v-model="drafts.HOME.hero.items[heroIndex].buttonBgColor" />
                          </el-form-item>
                        </el-col>
                        <el-col :span="12">
                          <el-form-item label="按钮文字色">
                            <el-color-picker v-model="drafts.HOME.hero.items[heroIndex].buttonTextColor" />
                          </el-form-item>
                        </el-col>
                      </el-row>
                    </el-form>
                  </div>
                </el-popover>
              </div>

              <div v-if="currentHero" class="canvas-hero__content">
                <p
                  class="canvas-hero__kicker"
                  :style="currentHero.kickerColor ? { color: currentHero.kickerColor } : undefined"
                  :contenteditable="studioMode === 'edit'"
                  @blur="
                    drafts.HOME.hero.items[heroIndex].kicker =
                      ($event.target as HTMLElement).innerText.trim()
                  "
                >
                  {{ currentHero.kicker || '数智化核心能力 · 驱动产业升级' }}
                </p>
                <h1
                  class="canvas-hero__title"
                  :style="currentHero.titleColor ? { color: currentHero.titleColor } : undefined"
                  :contenteditable="studioMode === 'edit'"
                  @blur="
                    drafts.HOME.hero.items[heroIndex].title =
                      ($event.target as HTMLElement).innerText.trim()
                  "
                >
                  {{ currentHero.title }}
                </h1>
                <p
                  class="canvas-hero__subtitle"
                  :style="currentHero.subtitleColor ? { color: currentHero.subtitleColor } : undefined"
                  :contenteditable="studioMode === 'edit'"
                  @blur="
                    drafts.HOME.hero.items[heroIndex].subtitle =
                      ($event.target as HTMLElement).innerText.trim()
                  "
                >
                  {{ currentHero.subtitle }}
                </p>
                <div class="canvas-hero__actions">
                  <button
                    type="button"
                    class="canvas-hero__btn"
                    :style="{
                      backgroundColor: currentHero.buttonBgColor || '#4c2ce6',
                      color: currentHero.buttonTextColor || '#ffffff',
                    }"
                    @click.stop="
                      studioMode !== 'edit' ? navigateLinkInStudio(currentHero.link) : undefined
                    "
                  >
                    <span
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        drafts.HOME.hero.items[heroIndex].buttonText =
                          ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      {{ currentHero.buttonText || '了解更多' }}
                    </span>
                  </button>
                </div>
              </div>

              <div v-if="activeHome.hero.items.length > 1" class="canvas-hero__bars">
                <button
                  v-for="(_, idx) in activeHome.hero.items"
                  :key="idx"
                  type="button"
                  class="canvas-hero__bar"
                  :class="{ active: heroIndex === idx }"
                  @click.stop="heroIndex = idx"
                ></button>
              </div>
            </section>

            <!-- 首页可排序四大业务模块：services / cases / customers / news -->
            <template v-for="(secKey, sIdx) in activeHome.sectionOrder" :key="secKey">
              <!-- 1. 核心产品与行业解决方案分组 (services) -->
              <section
                v-if="
                  secKey === 'services' &&
                  (activeHome.services.enabled || (studioMode === 'edit' && showDisabledBlocks))
                "
                class="canvas-section editable-zone"
                :class="{
                  'is-selected': selectedSection === 'services' && studioMode === 'edit',
                  'is-hidden-block': !activeHome.services.enabled,
                }"
                @click="selectedSection = 'services'"
              >
                <div v-if="studioMode === 'edit'" class="zone-toolbar">
                  <span class="zone-toolbar__tag">产品与解决方案分组 (HOME.services)</span>
                  <button type="button" class="zone-btn" @click.stop="addServiceGroup">
                    + 新增业务分组
                  </button>
                  <button
                    type="button"
                    class="zone-btn"
                    :disabled="sIdx === 0"
                    @click.stop="moveSection(sIdx, -1)"
                  >
                    ↑ 上移
                  </button>
                  <button
                    type="button"
                    class="zone-btn"
                    :disabled="sIdx === activeHome.sectionOrder.length - 1"
                    @click.stop="moveSection(sIdx, 1)"
                  >
                    ↓ 下移
                  </button>
                </div>

                <div class="canvas-section__head">
                  <h2
                    class="canvas-section__title"
                    :contenteditable="studioMode === 'edit'"
                    @blur="
                      drafts.HOME.services.title =
                        ($event.target as HTMLElement).innerText.trim()
                    "
                  >
                    {{ activeHome.services.title }}
                  </h2>
                  <p
                    class="canvas-section__subtitle"
                    :contenteditable="studioMode === 'edit'"
                    @blur="
                      drafts.HOME.services.subtitle =
                        ($event.target as HTMLElement).innerText.trim()
                    "
                  >
                    {{ activeHome.services.subtitle || '围绕核心业务场景构建软硬一体数字化能力' }}
                  </p>
                </div>

                <div class="service-groups-wrap">
                  <div
                    v-for="(grp, gIdx) in activeHome.services.groups"
                    :key="gIdx"
                    class="service-group-stage"
                    :class="{
                      'is-reversed':
                        grp.layout === 'right' || (grp.layout !== 'left' && gIdx % 2 === 1),
                    }"
                  >
                    <div class="service-group-stage__info">
                      <span
                        class="service-group-stage__tag"
                        :contenteditable="studioMode === 'edit'"
                        @blur="
                          drafts.HOME.services.groups[gIdx].tag =
                            ($event.target as HTMLElement).innerText.trim()
                        "
                      >
                        {{ grp.tag || `0${gIdx + 1} · 核心业务场景` }}
                      </span>
                      <h3
                        class="service-group-stage__title"
                        :contenteditable="studioMode === 'edit'"
                        @blur="
                          drafts.HOME.services.groups[gIdx].title =
                            ($event.target as HTMLElement).innerText.trim()
                        "
                      >
                        {{ grp.title }}
                      </h3>
                      <p
                        class="service-group-stage__desc"
                        :contenteditable="studioMode === 'edit'"
                        @blur="
                          drafts.HOME.services.groups[gIdx].description =
                            ($event.target as HTMLElement).innerText.trim()
                        "
                      >
                        {{ grp.description }}
                      </p>
                      <div v-if="studioMode === 'edit'" class="service-group-stage__hl-edit">
                        <el-input
                          v-model="drafts.HOME.services.groups[gIdx].highlightsText"
                          size="small"
                          placeholder="亮点标签（用 / 分隔，如：7×24安全托管 / 毫秒级预警）"
                        />
                      </div>
                      <div v-else-if="grp.highlights?.length" class="service-group-stage__pills">
                        <span v-for="(hl, hIdx) in grp.highlights" :key="hIdx" class="hl-pill">
                          {{ hl }}
                        </span>
                      </div>

                      <div v-if="studioMode === 'edit'" class="service-group-stage__controls">
                        <el-select
                          v-model="drafts.HOME.services.groups[gIdx].layout"
                          size="small"
                          style="width: 110px"
                        >
                          <el-option label="自动交替" value="auto" />
                          <el-option label="左文右卡" value="left" />
                          <el-option label="右文左卡" value="right" />
                        </el-select>
                        <el-select
                          v-model="drafts.HOME.services.groups[gIdx].contentType"
                          size="small"
                          style="width: 110px"
                        >
                          <el-option label="关联产品" value="PRODUCT" />
                          <el-option label="关联方案" value="SOLUTION" />
                        </el-select>
                        <el-select
                          v-model="drafts.HOME.services.groups[gIdx].contentIds"
                          multiple
                          collapse-tags
                          size="small"
                          placeholder="选择展示卡片"
                          style="width: 210px"
                        >
                          <el-option
                            v-for="c in contentsMap[(grp.contentType as ContentType) || 'PRODUCT']"
                            :key="c.id"
                            :label="c.title || `#${c.id}`"
                            :value="Number(c.id)"
                          />
                        </el-select>
                        <el-button
                          type="danger"
                          link
                          size="small"
                          @click="drafts.HOME.services.groups.splice(gIdx, 1)"
                        >
                          删除分组
                        </el-button>
                      </div>
                    </div>

                    <div class="service-group-stage__cards">
                      <div
                        v-for="cid in grp.contentIds"
                        :key="cid"
                        class="canvas-card"
                        @click.stop="
                          openContentInStudio(
                            (grp.contentType as ContentType) || 'PRODUCT',
                            cid,
                          )
                        "
                      >
                        <div class="canvas-card__body">
                          <h4>
                            {{
                              getContentData(
                                getContentItem(
                                  (grp.contentType as ContentType) || 'PRODUCT',
                                  cid,
                                ),
                              ).title || `内容 #${cid}`
                            }}
                          </h4>
                          <p>
                            {{
                              getContentData(
                                getContentItem(
                                  (grp.contentType as ContentType) || 'PRODUCT',
                                  cid,
                                ),
                              ).summary || '点击卡片直接进入该详情画布编辑'
                            }}
                          </p>
                          <span class="canvas-card__link">
                            {{ studioMode === 'edit' ? '✏️ 点击进入详情画布编辑 →' : '了解详情 →' }}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </section>

              <!-- 2. 标杆客户案例 (cases) -->
              <section
                v-if="
                  secKey === 'cases' &&
                  (activeHome.cases.enabled || (studioMode === 'edit' && showDisabledBlocks))
                "
                class="canvas-section canvas-section--gray editable-zone"
                :class="{
                  'is-selected': selectedSection === 'cases' && studioMode === 'edit',
                  'is-hidden-block': !activeHome.cases.enabled,
                }"
                @click="selectedSection = 'cases'"
              >
                <div v-if="studioMode === 'edit'" class="zone-toolbar">
                  <span class="zone-toolbar__tag">首页标杆客户案例 (HOME.cases)</span>
                  <el-select
                    v-model="drafts.HOME.cases.contentIds"
                    multiple
                    collapse-tags
                    size="small"
                    placeholder="勾选首页展示的案例"
                    style="width: 240px"
                  >
                    <el-option
                      v-for="c in contentsMap.CASE"
                      :key="c.id"
                      :label="c.title || `#${c.id}`"
                      :value="Number(c.id)"
                    />
                  </el-select>
                  <button
                    type="button"
                    class="zone-btn"
                    @click.stop="switchStudioPage('CASES')"
                  >
                    🏆 前往客户案例完整页 →
                  </button>
                </div>

                <div class="canvas-section__head">
                  <h2
                    class="canvas-section__title"
                    :contenteditable="studioMode === 'edit'"
                    @blur="
                      drafts.HOME.cases.title = ($event.target as HTMLElement).innerText.trim()
                    "
                  >
                    {{ activeHome.cases.title }}
                  </h2>
                  <p
                    class="canvas-section__subtitle"
                    :contenteditable="studioMode === 'edit'"
                    @blur="
                      drafts.HOME.cases.subtitle =
                        ($event.target as HTMLElement).innerText.trim()
                    "
                  >
                    {{ activeHome.cases.subtitle || '与行业领军企业携手共建数字化生产力' }}
                  </p>
                </div>

                <div class="canvas-grid-3">
                  <div
                    v-for="cid in activeHome.cases.contentIds"
                    :key="cid"
                    class="canvas-case-card"
                    @click.stop="openContentInStudio('CASE', cid)"
                  >
                    <div class="canvas-case-card__tag">
                      {{ getContentData(getContentItem('CASE', cid)).industry || '标杆案例' }}
                    </div>
                    <h3>{{ getContentData(getContentItem('CASE', cid)).title || `案例 #${cid}` }}</h3>
                    <p>{{ getContentData(getContentItem('CASE', cid)).summary }}</p>
                    <span class="canvas-card__link">
                      {{ studioMode === 'edit' ? '✏️ 点击在画布中编辑此案例 →' : '阅读案例 →' }}
                    </span>
                  </div>
                </div>
              </section>

              <!-- 3. 合作客户标识墙 (customers) -->
              <section
                v-if="
                  secKey === 'customers' &&
                  (activeHome.customers.enabled || (studioMode === 'edit' && showDisabledBlocks))
                "
                class="canvas-section editable-zone"
                :class="{
                  'is-selected': selectedSection === 'customers' && studioMode === 'edit',
                  'is-hidden-block': !activeHome.customers.enabled,
                }"
                @click="selectedSection = 'customers'"
              >
                <div v-if="studioMode === 'edit'" class="zone-toolbar">
                  <span class="zone-toolbar__tag">合作客户标识墙 (HOME.customers)</span>
                  <el-select
                    v-model="drafts.HOME.customers.contentIds"
                    multiple
                    collapse-tags
                    size="small"
                    placeholder="选择展示的客户 Logo"
                    style="width: 240px"
                  >
                    <el-option
                      v-for="c in contentsMap.CUSTOMER"
                      :key="c.id"
                      :label="c.title || `#${c.id}`"
                      :value="Number(c.id)"
                    />
                  </el-select>
                  <button
                    type="button"
                    class="zone-btn"
                    @click.stop="createNewContentInStudio('CUSTOMER')"
                  >
                    + 新增合作客户
                  </button>
                </div>

                <div class="canvas-section__head">
                  <h2
                    class="canvas-section__title"
                    :contenteditable="studioMode === 'edit'"
                    @blur="
                      drafts.HOME.customers.title =
                        ($event.target as HTMLElement).innerText.trim()
                    "
                  >
                    {{ activeHome.customers.title }}
                  </h2>
                </div>

                <div class="customer-logo-wall">
                  <div
                    v-for="cid in activeHome.customers.contentIds"
                    :key="cid"
                    class="customer-logo-pill"
                  >
                    <span>
                      {{ getContentData(getContentItem('CUSTOMER', cid)).title || `#${cid}` }}
                    </span>
                  </div>
                </div>
              </section>

              <!-- 4. 新闻动态资讯 (news) -->
              <section
                v-if="
                  secKey === 'news' &&
                  (activeHome.news.enabled || (studioMode === 'edit' && showDisabledBlocks))
                "
                class="canvas-section canvas-section--gray editable-zone"
                :class="{
                  'is-selected': selectedSection === 'news' && studioMode === 'edit',
                  'is-hidden-block': !activeHome.news.enabled,
                }"
                @click="selectedSection = 'news'"
              >
                <div v-if="studioMode === 'edit'" class="zone-toolbar">
                  <span class="zone-toolbar__tag">首页新闻动态 (HOME.news)</span>
                  <el-radio-group v-model="drafts.HOME.news.mode" size="small">
                    <el-radio-button value="LATEST">自动最新</el-radio-button>
                    <el-radio-button value="MANUAL">手动精选</el-radio-button>
                  </el-radio-group>
                  <button
                    type="button"
                    class="zone-btn"
                    @click.stop="switchStudioPage('NEWS')"
                  >
                    📰 前往新闻中心完整页 →
                  </button>
                </div>

                <div class="canvas-section__head">
                  <h2
                    class="canvas-section__title"
                    :contenteditable="studioMode === 'edit'"
                    @blur="
                      drafts.HOME.news.title = ($event.target as HTMLElement).innerText.trim()
                    "
                  >
                    {{ activeHome.news.title }}
                  </h2>
                </div>

                <div class="canvas-grid-3">
                  <div
                    v-for="nItem in resolvedHomeNews"
                    :key="nItem.id"
                    class="canvas-news-card"
                    @click.stop="openContentInStudio('NEWS', nItem.id)"
                  >
                    <div class="canvas-news-card__meta">
                      {{ getContentData(nItem).displayTime || '最新资讯' }}
                    </div>
                    <h3>{{ getContentData(nItem).title }}</h3>
                    <p>{{ getContentData(nItem).summary }}</p>
                    <span class="canvas-card__link">
                      {{ studioMode === 'edit' ? '✏️ 点击在画布中编辑此新闻 →' : '阅读全文 →' }}
                    </span>
                  </div>
                </div>
              </section>
            </template>
          </template>

          <!-- =========================================================================
               页面 2 / 3 / 4 / 5：产品中心 / 解决方案 / 客户案例 / 新闻中心（列表态 + 详情态）
               ========================================================================= -->
          <template
            v-else-if="
              activePage === 'PRODUCTS' ||
              activePage === 'SOLUTIONS' ||
              activePage === 'CASES' ||
              activePage === 'NEWS'
            "
          >
            <!-- A. 列表画布视图（当未打开具体某篇详情时） -->
            <template v-if="!activeContentId">
              <section class="subpage-hero">
                <div class="subpage-hero__inner">
                  <span class="subpage-hero__kicker">
                    {{ activePageInfo?.icon }}
                    {{ activePageInfo?.label }} · 实景画布
                  </span>
                  <h1>{{ activePageInfo?.label }}</h1>
                  <p>
                    点击下方分类标签可切换或直接重命名分类；点击任意内容卡片即可在画布内进入完整详情页实景编辑。
                  </p>
                </div>
              </section>

              <!-- 分类胶囊筛选与原位管理栏 -->
              <section class="category-bar-section">
                <div class="category-pills">
                  <button
                    type="button"
                    class="cat-pill"
                    :class="{ active: activeCategoryFilter === 'ALL' }"
                    @click="activeCategoryFilter = 'ALL'"
                  >
                    全部 ({{ currentContents.length }})
                  </button>
                  <div
                    v-for="cat in currentCategories"
                    :key="cat.id"
                    class="cat-pill-wrap"
                  >
                    <button
                      type="button"
                      class="cat-pill"
                      :class="{ active: String(activeCategoryFilter) === String(cat.id) }"
                      @click="activeCategoryFilter = String(cat.id)"
                    >
                      {{ cat.name }}
                    </button>
                    <button
                      v-if="studioMode === 'edit'"
                      type="button"
                      class="cat-pill-edit"
                      title="重命名并发布此分类"
                      @click.stop="renameCategoryInline(currentCategoryType, cat)"
                    >
                      ✏️
                    </button>
                  </div>
                  <button
                    v-if="studioMode === 'edit'"
                    type="button"
                    class="cat-pill cat-pill--add"
                    @click="addCategoryInline(currentCategoryType)"
                  >
                    + 新建分类标签
                  </button>
                  <button
                    v-if="studioMode === 'edit'"
                    type="button"
                    class="cat-pill cat-pill--primary"
                    @click="createNewContentInStudio(currentContentType)"
                  >
                    + 新建{{ activePageInfo?.label }}条目
                  </button>
                </div>
              </section>

              <!-- 新闻中心专属：顶部置顶大头条卡片（还原《新闻中心.mp4》） -->
              <section
                v-if="activePage === 'NEWS' && pinnedHeadlineNews"
                class="news-pinned-section"
              >
                <div
                  class="news-pinned-card"
                  @click="openContentInStudio('NEWS', pinnedHeadlineNews.id)"
                >
                  <div class="news-pinned-card__info">
                    <div class="news-pinned-card__badges">
                      <span class="badge-top">置顶头条</span>
                      <span class="badge-cat">
                        {{
                          getCategoryName(
                            'NEWS',
                            getContentData(pinnedHeadlineNews).categoryId,
                          )
                        }}
                      </span>
                      <span class="badge-date">
                        {{ getContentData(pinnedHeadlineNews).displayTime }}
                      </span>
                    </div>
                    <h2>{{ getContentData(pinnedHeadlineNews).title }}</h2>
                    <p>{{ getContentData(pinnedHeadlineNews).summary }}</p>
                    <span class="canvas-card__link">
                      {{ studioMode === 'edit' ? '✏️ 点击进入头条新闻正文实景编辑 →' : '阅读全文 →' }}
                    </span>
                  </div>
                  <div class="news-pinned-card__cover">
                    <img
                      v-if="resolveMediaUrl(getContentData(pinnedHeadlineNews).coverMediaId)"
                      :src="resolveMediaUrl(getContentData(pinnedHeadlineNews).coverMediaId)"
                      alt="头条配图"
                    />
                    <div v-else class="cover-placeholder">📰 头条新闻配图</div>
                  </div>
                </div>
              </section>

              <!-- 列表卡片网格 -->
              <section class="canvas-section">
                <div class="canvas-grid-3">
                  <div
                    v-for="item in activePage === 'NEWS'
                      ? gridNewsItems
                      : getFilteredContents(currentContentType)"
                    :key="item.id"
                    class="canvas-list-card"
                    @click="openContentInStudio(currentContentType, item.id)"
                  >
                    <div class="canvas-list-card__top">
                      <span class="canvas-list-card__cat">
                        {{
                          getCategoryName(
                            currentCategoryType,
                            getContentData(item).categoryId,
                          )
                        }}
                      </span>
                      <el-tag
                        size="small"
                        :type="
                          item.status === 'PUBLISHED' && !item.hasUnpublishedChanges
                            ? 'success'
                            : 'warning'
                        "
                      >
                        {{
                          item.status === 'PUBLISHED' && !item.hasUnpublishedChanges
                            ? '已发布'
                            : '草稿'
                        }}
                      </el-tag>
                    </div>
                    <h3>{{ getContentData(item).title || '未命名内容' }}</h3>
                    <p>{{ getContentData(item).summary || '点击进入详情画布编辑摘要与正文...' }}</p>
                    <div class="canvas-list-card__foot">
                      <span v-if="activePage === 'CASES' && getContentData(item).customerName">
                        🏢 {{ getContentData(item).customerName }}
                      </span>
                      <span v-else-if="activePage === 'NEWS'">
                        🕒 {{ getContentData(item).displayTime }}
                      </span>
                      <span class="canvas-card__link">
                        {{ studioMode === 'edit' ? '✏️ 实景编辑详情 →' : '查看详情 →' }}
                      </span>
                    </div>
                  </div>

                  <!-- 原位新建卡片 -->
                  <div
                    v-if="studioMode === 'edit'"
                    class="canvas-list-card canvas-list-card--add"
                    @click="createNewContentInStudio(currentContentType)"
                  >
                    <div class="add-card-inner">
                      <span class="add-card-icon">＋</span>
                      <strong>
                        新建{{ activePageInfo?.label }}
                      </strong>
                      <span>创建后直接在画布中实景编辑详情</span>
                    </div>
                  </div>
                </div>
              </section>

              <!-- 客户案例页专属：底部「客户成功故事」高管证言模块（还原《客户案例.mp4》） -->
              <section
                v-if="activePage === 'CASES'"
                class="canvas-section canvas-section--gray editable-zone"
              >
                <div v-if="studioMode === 'edit'" class="zone-toolbar">
                  <span class="zone-toolbar__tag">
                    客户案例页 · 客户成功故事证言 (ABOUT.customerStories)
                  </span>
                  <button
                    type="button"
                    class="zone-btn"
                    @click="
                      drafts.ABOUT.customerStories.push({
                        quote: '“在此输入客户高管证言金句...”',
                        author: '高管姓名与职务',
                        company: '客户企业名称',
                        logoText: '企业 LOGO 标识',
                        videoUrl: '',
                      })
                    "
                  >
                    + 新增客户成功故事
                  </button>
                </div>

                <div class="canvas-section__head">
                  <h2 class="canvas-section__title">客户成功故事</h2>
                  <p class="canvas-section__subtitle">
                    听听行业领军企业掌舵人如何评价数字化转型带来的经营改变
                  </p>
                </div>

                <div class="canvas-grid-2">
                  <div
                    v-for="(st, stIdx) in activeAbout.customerStories"
                    :key="stIdx"
                    class="story-quote-card"
                  >
                    <blockquote
                      class="story-quote-card__text"
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        drafts.ABOUT.customerStories[stIdx].quote =
                          ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      {{ st.quote }}
                    </blockquote>
                    <div class="story-quote-card__footer">
                      <div>
                        <strong
                          :contenteditable="studioMode === 'edit'"
                          @blur="
                            drafts.ABOUT.customerStories[stIdx].author =
                              ($event.target as HTMLElement).innerText.trim()
                          "
                        >
                          {{ st.author }}
                        </strong>
                        <span
                          class="story-quote-card__company"
                          :contenteditable="studioMode === 'edit'"
                          @blur="
                            drafts.ABOUT.customerStories[stIdx].logoText =
                              ($event.target as HTMLElement).innerText.trim()
                          "
                        >
                          {{ st.logoText || st.company }}
                        </span>
                      </div>
                      <el-button
                        v-if="studioMode === 'edit'"
                        type="danger"
                        link
                        size="small"
                        @click="drafts.ABOUT.customerStories.splice(stIdx, 1)"
                      >
                        删除
                      </el-button>
                    </div>
                  </div>
                </div>
              </section>
            </template>

            <!-- B. 详情页内嵌实景编辑视图（当点击某张产品/方案/案例/新闻卡片进入详情时） -->
            <template v-else>
              <div class="detail-studio-canvas" v-loading="contentDetailLoading">
                <!-- 详情页顶部实景 Hero 横幅 -->
                <section class="detail-hero">
                  <div class="detail-hero__bar">
                    <el-button size="small" @click="closeContentDetail">
                      ← 返回{{ activePageInfo?.label }}列表
                    </el-button>
                    <div v-if="studioMode === 'edit'" class="detail-hero__meta-controls">
                      <span>所属分类：</span>
                      <el-select
                        v-model="activeContentDraft.categoryId"
                        size="small"
                        placeholder="选择分类"
                        style="width: 150px"
                      >
                        <el-option
                          v-for="cat in currentCategories"
                          :key="cat.id"
                          :label="cat.name"
                          :value="String(cat.id)"
                        />
                      </el-select>
                      <span>封面/头图：</span>
                      <el-select
                        v-model="activeContentDraft.coverMediaId"
                        clearable
                        size="small"
                        placeholder="选择配图"
                        style="width: 160px"
                      >
                        <el-option
                          v-for="m in Object.values(mediaCache)"
                          :key="m.id"
                          :label="m.originalName || `#${m.id}`"
                          :value="Number(m.id)"
                        />
                      </el-select>
                      <el-button size="small" type="primary" @click="saveActiveContentDraft()">
                        💾 保存详情草稿
                      </el-button>
                      <el-button size="small" type="success" @click="publishActiveContentDraft()">
                        🚀 发布此内容
                      </el-button>
                    </div>
                  </div>

                  <div class="detail-hero__body">
                    <h1
                      class="detail-hero__title"
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        activeContentDraft.title =
                          ($event.target as HTMLElement).innerText.trim() ||
                          activeContentDraft.title
                      "
                    >
                      {{ activeContentDraft.title }}
                    </h1>
                    <p
                      class="detail-hero__summary"
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        activeContentDraft.summary =
                          ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      {{ activeContentDraft.summary || '点击此处直接输入一句话核心摘要...' }}
                    </p>
                  </div>
                </section>

                <!-- 1) 产品详情 7 段式实景画布 (activePage === 'PRODUCTS') -->
                <template v-if="activePage === 'PRODUCTS'">
                  <!-- 段 2：三大核心产品特性卡 -->
                  <section class="canvas-section">
                    <div class="canvas-section__head-inline">
                      <h2>核心产品特性</h2>
                      <el-button
                        v-if="studioMode === 'edit'"
                        size="small"
                        type="primary"
                        plain
                        @click="
                          activeContentDraft.features.push({
                            tag: '新特性',
                            title: '输入特性标题',
                            description: '输入特性详细描述',
                          })
                        "
                      >
                        + 添加特性卡
                      </el-button>
                    </div>
                    <div class="canvas-grid-3">
                      <div
                        v-for="(ft, fIdx) in activeContentDraft.features"
                        :key="fIdx"
                        class="canvas-card"
                      >
                        <span
                          class="hl-pill"
                          :contenteditable="studioMode === 'edit'"
                          @blur="
                            activeContentDraft.features[fIdx].tag =
                              ($event.target as HTMLElement).innerText.trim()
                          "
                        >
                          {{ ft.tag || '核心能力' }}
                        </span>
                        <h3
                          :contenteditable="studioMode === 'edit'"
                          @blur="
                            activeContentDraft.features[fIdx].title =
                              ($event.target as HTMLElement).innerText.trim()
                          "
                        >
                          {{ ft.title }}
                        </h3>
                        <p
                          :contenteditable="studioMode === 'edit'"
                          @blur="
                            activeContentDraft.features[fIdx].description =
                              ($event.target as HTMLElement).innerText.trim()
                          "
                        >
                          {{ ft.description }}
                        </p>
                        <el-button
                          v-if="studioMode === 'edit'"
                          type="danger"
                          link
                          size="small"
                          @click="activeContentDraft.features.splice(fIdx, 1)"
                        >
                          删除
                        </el-button>
                      </div>
                    </div>
                  </section>

                  <!-- 段 3：应用效果量化指标卡 -->
                  <section class="canvas-section canvas-section--gray">
                    <div class="canvas-section__head-inline">
                      <h2>应用效果指标</h2>
                      <el-button
                        v-if="studioMode === 'edit'"
                        size="small"
                        type="primary"
                        plain
                        @click="
                          activeContentDraft.effectMetrics.push({
                            displayValue: '50%',
                            label: '关键业务指标提升',
                          })
                        "
                      >
                        + 添加效果指标
                      </el-button>
                    </div>
                    <div class="canvas-grid-3">
                      <div
                        v-for="(em, eIdx) in activeContentDraft.effectMetrics"
                        :key="eIdx"
                        class="metric-stat-card"
                      >
                        <div
                          class="metric-stat-card__val"
                          :contenteditable="studioMode === 'edit'"
                          @blur="
                            activeContentDraft.effectMetrics[eIdx].displayValue =
                              ($event.target as HTMLElement).innerText.trim()
                          "
                        >
                          {{ em.displayValue }}
                        </div>
                        <div
                          class="metric-stat-card__label"
                          :contenteditable="studioMode === 'edit'"
                          @blur="
                            activeContentDraft.effectMetrics[eIdx].label =
                              ($event.target as HTMLElement).innerText.trim()
                          "
                        >
                          {{ em.label }}
                        </div>
                        <el-button
                          v-if="studioMode === 'edit'"
                          type="danger"
                          link
                          size="small"
                          @click="activeContentDraft.effectMetrics.splice(eIdx, 1)"
                        >
                          删除
                        </el-button>
                      </div>
                    </div>
                  </section>

                  <!-- 段 4：典型角色使用场景页签 -->
                  <section class="canvas-section">
                    <div class="canvas-section__head-inline">
                      <h2>典型角色使用场景</h2>
                      <el-button
                        v-if="studioMode === 'edit'"
                        size="small"
                        type="primary"
                        plain
                        @click="
                          activeContentDraft.roleTabs.push({
                            role: '新角色',
                            headline: '输入该角色的核心赋能价值',
                            bullets: ['价值要点 1', '价值要点 2'],
                          });
                          activeProductRoleIdx = activeContentDraft.roleTabs.length - 1
                        "
                      >
                        + 添加角色页签
                      </el-button>
                    </div>

                    <div class="role-tabs-bar">
                      <button
                        v-for="(rt, rIdx) in activeContentDraft.roleTabs"
                        :key="rIdx"
                        type="button"
                        class="cat-pill"
                        :class="{ active: activeProductRoleIdx === rIdx }"
                        @click="activeProductRoleIdx = rIdx"
                      >
                        <span
                          :contenteditable="studioMode === 'edit'"
                          @blur="
                            activeContentDraft.roleTabs[rIdx].role =
                              ($event.target as HTMLElement).innerText.trim()
                          "
                        >
                          {{ rt.role }}
                        </span>
                      </button>
                    </div>

                    <div
                      v-if="activeProductRole"
                      class="role-tab-panel"
                    >
                      <h3
                        :contenteditable="studioMode === 'edit'"
                        @blur="
                          activeContentDraft.roleTabs[activeProductRoleIdx].headline =
                            ($event.target as HTMLElement).innerText.trim()
                        "
                      >
                        {{ activeContentDraft.roleTabs[activeProductRoleIdx].headline }}
                      </h3>
                      <ul class="role-bullet-list">
                        <li
                          v-for="(bText, bIdx) in activeContentDraft.roleTabs[activeProductRoleIdx]
                            .bullets"
                          :key="bIdx"
                        >
                          <span
                            :contenteditable="studioMode === 'edit'"
                            @blur="
                              activeContentDraft.roleTabs[activeProductRoleIdx].bullets[bIdx] =
                                ($event.target as HTMLElement).innerText.trim()
                            "
                          >
                            {{ bText }}
                          </span>
                          <el-button
                            v-if="studioMode === 'edit'"
                            type="danger"
                            link
                            size="small"
                            @click="
                              activeContentDraft.roleTabs[activeProductRoleIdx].bullets.splice(
                                bIdx,
                                1,
                              )
                            "
                          >
                            ×
                          </el-button>
                        </li>
                      </ul>
                      <el-button
                        v-if="studioMode === 'edit'"
                        size="small"
                        type="primary"
                        link
                        @click="
                          activeContentDraft.roleTabs[activeProductRoleIdx].bullets.push(
                            '新场景价值要点',
                          )
                        "
                      >
                        + 添加要点
                      </el-button>
                    </div>
                  </section>

                  <!-- 段 5：终端与工具组合卡 -->
                  <section class="canvas-section canvas-section--gray">
                    <div class="canvas-section__head-inline">
                      <h2>智能终端与配套工具</h2>
                      <el-button
                        v-if="studioMode === 'edit'"
                        size="small"
                        type="primary"
                        plain
                        @click="
                          activeContentDraft.terminalTools.push({
                            name: '新智能终端',
                            badge: 'IoT 硬件',
                            description: '输入终端功能说明',
                          })
                        "
                      >
                        + 添加终端卡片
                      </el-button>
                    </div>
                    <div class="canvas-grid-3">
                      <div
                        v-for="(tt, tIdx) in activeContentDraft.terminalTools"
                        :key="tIdx"
                        class="canvas-card"
                      >
                        <span
                          class="hl-pill"
                          :contenteditable="studioMode === 'edit'"
                          @blur="
                            activeContentDraft.terminalTools[tIdx].badge =
                              ($event.target as HTMLElement).innerText.trim()
                          "
                        >
                          {{ tt.badge }}
                        </span>
                        <h3
                          :contenteditable="studioMode === 'edit'"
                          @blur="
                            activeContentDraft.terminalTools[tIdx].name =
                              ($event.target as HTMLElement).innerText.trim()
                          "
                        >
                          {{ tt.name }}
                        </h3>
                        <p
                          :contenteditable="studioMode === 'edit'"
                          @blur="
                            activeContentDraft.terminalTools[tIdx].description =
                              ($event.target as HTMLElement).innerText.trim()
                          "
                        >
                          {{ tt.description }}
                        </p>
                      </div>
                    </div>
                  </section>
                </template>

                <!-- 2) 解决方案详情专属：行业痛点 & 方案价值 (activePage === 'SOLUTIONS') -->
                <template v-else-if="activePage === 'SOLUTIONS'">
                  <section class="canvas-section">
                    <div class="canvas-section__head-inline">
                      <h2>核心行业痛点</h2>
                      <el-button
                        v-if="studioMode === 'edit'"
                        size="small"
                        type="primary"
                        plain
                        @click="
                          activeContentDraft.pains.push({
                            title: '新行业痛点',
                            description: '在此输入客户面临的典型痛点描述',
                          })
                        "
                      >
                        + 添加痛点
                      </el-button>
                    </div>
                    <div class="canvas-grid-3">
                      <div
                        v-for="(pn, pIdx) in activeContentDraft.pains"
                        :key="pIdx"
                        class="canvas-card"
                      >
                        <h3
                          :contenteditable="studioMode === 'edit'"
                          @blur="
                            activeContentDraft.pains[pIdx].title =
                              ($event.target as HTMLElement).innerText.trim()
                          "
                        >
                          {{ pn.title }}
                        </h3>
                        <p
                          :contenteditable="studioMode === 'edit'"
                          @blur="
                            activeContentDraft.pains[pIdx].description =
                              ($event.target as HTMLElement).innerText.trim()
                          "
                        >
                          {{ pn.description }}
                        </p>
                      </div>
                    </div>
                  </section>

                  <section class="canvas-section canvas-section--gray">
                    <div class="canvas-section__head-inline">
                      <h2>方案核心收益</h2>
                      <el-button
                        v-if="studioMode === 'edit'"
                        size="small"
                        type="primary"
                        plain
                        @click="
                          activeContentDraft.benefits.push({
                            title: '新方案收益',
                            description: '在此输入方案带来的核心价值',
                          })
                        "
                      >
                        + 添加收益
                      </el-button>
                    </div>
                    <div class="canvas-grid-3">
                      <div
                        v-for="(bf, bIdx) in activeContentDraft.benefits"
                        :key="bIdx"
                        class="canvas-card"
                      >
                        <h3
                          :contenteditable="studioMode === 'edit'"
                          @blur="
                            activeContentDraft.benefits[bIdx].title =
                              ($event.target as HTMLElement).innerText.trim()
                          "
                        >
                          {{ bf.title }}
                        </h3>
                        <p
                          :contenteditable="studioMode === 'edit'"
                          @blur="
                            activeContentDraft.benefits[bIdx].description =
                              ($event.target as HTMLElement).innerText.trim()
                          "
                        >
                          {{ bf.description }}
                        </p>
                      </div>
                    </div>
                  </section>
                </template>

                <!-- 3) 客户案例详情专属：客户信息、高管证言、量化成效 (activePage === 'CASES') -->
                <template v-else-if="activePage === 'CASES'">
                  <section class="canvas-section">
                    <div class="case-meta-banner">
                      <div>
                        <span class="meta-label">所属行业：</span>
                        <strong
                          :contenteditable="studioMode === 'edit'"
                          @blur="
                            activeContentDraft.industry =
                              ($event.target as HTMLElement).innerText.trim()
                          "
                        >
                          {{ activeContentDraft.industry || '智慧物流' }}
                        </strong>
                      </div>
                      <div>
                        <span class="meta-label">客户名称：</span>
                        <strong
                          :contenteditable="studioMode === 'edit'"
                          @blur="
                            activeContentDraft.customerName =
                              ($event.target as HTMLElement).innerText.trim()
                          "
                        >
                          {{ activeContentDraft.customerName || '标杆客户' }}
                        </strong>
                      </div>
                    </div>

                    <div class="story-quote-card" style="margin-top: 20px">
                      <blockquote
                        class="story-quote-card__text"
                        :contenteditable="studioMode === 'edit'"
                        @blur="
                          activeContentDraft.quoteText =
                            ($event.target as HTMLElement).innerText.trim()
                        "
                      >
                        {{ activeContentDraft.quoteText || '“在此直接输入客户高管证言引语...”' }}
                      </blockquote>
                      <div class="story-quote-card__footer">
                        <strong
                          :contenteditable="studioMode === 'edit'"
                          @blur="
                            activeContentDraft.quoteAuthor =
                              ($event.target as HTMLElement).innerText.trim()
                          "
                        >
                          — {{ activeContentDraft.quoteAuthor || '客户高管姓名与职务' }}
                        </strong>
                      </div>
                    </div>
                  </section>
                </template>

                <!-- 4) 新闻详情专属：作者、时间、来源栏 (activePage === 'NEWS') -->
                <template v-else-if="activePage === 'NEWS'">
                  <section class="canvas-section" style="padding-bottom: 0">
                    <div class="case-meta-banner">
                      <div>
                        <span class="meta-label">发布时间：</span>
                        <strong
                          :contenteditable="studioMode === 'edit'"
                          @blur="
                            activeContentDraft.displayTime =
                              ($event.target as HTMLElement).innerText.trim()
                          "
                        >
                          {{ activeContentDraft.displayTime }}
                        </strong>
                      </div>
                      <div>
                        <span class="meta-label">作者/来源：</span>
                        <strong
                          :contenteditable="studioMode === 'edit'"
                          @blur="
                            activeContentDraft.author =
                              ($event.target as HTMLElement).innerText.trim()
                          "
                        >
                          {{ activeContentDraft.author || '官方发布' }}
                        </strong>
                      </div>
                    </div>
                  </section>
                </template>

                <!-- 通用富文本正文实景编辑与预览区 -->
                <section class="canvas-section">
                  <div class="canvas-section__head-inline">
                    <h2>详情图文正文 (bodyHtml)</h2>
                  </div>
                  <div v-if="studioMode === 'edit'" class="rich-body-split">
                    <el-input
                      v-model="activeContentDraft.bodyHtml"
                      type="textarea"
                      :rows="8"
                      placeholder="支持直接输入 HTML 或段落文本，右侧/下方实时渲染前台视觉效果"
                    />
                    <div class="rich-body-preview" v-html="activeContentDraft.bodyHtml"></div>
                  </div>
                  <div v-else class="rich-body-preview" v-html="activeContentDraft.bodyHtml"></div>
                </section>
              </div>
            </template>
          </template>

          <!-- =========================================================================
               页面 6：关于我们 (activePage === 'ABOUT') —— 完整 5 大子页签 + 双弹窗 + 7城地图
               ========================================================================= -->
          <template v-else-if="activePage === 'ABOUT'">
            <section class="about-hero editable-zone">
              <div class="about-hero__inner">
                <h1
                  :contenteditable="studioMode === 'edit'"
                  @blur="
                    drafts.ABOUT.heroTitle =
                      ($event.target as HTMLElement).innerText.trim() ||
                      drafts.ABOUT.heroTitle
                  "
                >
                  {{ activeAbout.heroTitle }}
                </h1>
              </div>

              <!-- 悬浮白卡 5 大子页签导航 -->
              <div class="about-subtabs">
                <button
                  type="button"
                  class="about-subtab-btn"
                  :class="{ active: aboutSubTab === 'intro' }"
                  @click="aboutSubTab = 'intro'"
                >
                  企业简介
                </button>
                <button
                  type="button"
                  class="about-subtab-btn"
                  :class="{ active: aboutSubTab === 'team' }"
                  @click="aboutSubTab = 'team'"
                >
                  管理团队
                </button>
                <button
                  type="button"
                  class="about-subtab-btn"
                  :class="{ active: aboutSubTab === 'media' }"
                  @click="aboutSubTab = 'media'"
                >
                  媒体中心
                </button>
                <button
                  type="button"
                  class="about-subtab-btn"
                  :class="{ active: aboutSubTab === 'resources' }"
                  @click="aboutSubTab = 'resources'"
                >
                  资料共享
                </button>
                <button
                  type="button"
                  class="about-subtab-btn"
                  :class="{ active: aboutSubTab === 'join' }"
                  @click="aboutSubTab = 'join'"
                >
                  加入我们
                </button>
              </div>
            </section>

            <!-- 子页签 1：企业简介 + CEO 的信 + 发展历程时间轴 + 7 城地图 + 荣誉资质 -->
            <template v-if="aboutSubTab === 'intro'">
              <!-- 企业简介左文右图 -->
              <section class="canvas-section">
                <div class="about-intro-grid">
                  <div class="about-intro-grid__text">
                    <h2
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        drafts.ABOUT.introTitle =
                          ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      {{ activeAbout.introTitle }}
                    </h2>
                    <p
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        drafts.ABOUT.intro = ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      {{ activeAbout.intro }}
                    </p>
                  </div>
                  <div class="about-intro-grid__media">
                    <img
                      v-if="activeAbout.introImage"
                      :src="activeAbout.introImage"
                      alt="企业简介配图"
                    />
                    <div v-else class="cover-placeholder">🏢 企业形象展示大图</div>
                    <el-input
                      v-if="studioMode === 'edit'"
                      v-model="drafts.ABOUT.introImage"
                      size="small"
                      placeholder="输入企业简介配图 URL..."
                      style="margin-top: 8px"
                    />
                  </div>
                </div>
              </section>

              <!-- CEO 的信紫底双栏卡片 + 展开全文弹窗 -->
              <section class="canvas-section" style="padding-top: 0">
                <div class="ceo-letter-card">
                  <div class="ceo-letter-card__left">
                    <blockquote
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        drafts.ABOUT.ceoLetter.leftQuote =
                          ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      {{ activeAbout.ceoLetter.leftQuote }}
                    </blockquote>
                  </div>
                  <div class="ceo-letter-card__right">
                    <h3
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        drafts.ABOUT.ceoLetter.title =
                          ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      {{ activeAbout.ceoLetter.title }}
                    </h3>
                    <p
                      v-for="(ex, exIdx) in activeAbout.ceoLetter.excerpt"
                      :key="exIdx"
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        drafts.ABOUT.ceoLetter.excerpt[exIdx] =
                          ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      {{ ex }}
                    </p>
                    <div class="ceo-letter-card__foot">
                      <span
                        class="ceo-sig"
                        :contenteditable="studioMode === 'edit'"
                        @blur="
                          drafts.ABOUT.ceoLetter.signature =
                            ($event.target as HTMLElement).innerText.trim()
                        "
                      >
                        — {{ activeAbout.ceoLetter.signature }}
                      </span>
                      <button
                        type="button"
                        class="ceo-expand-btn"
                        @click="ceoModalOpen = true"
                      >
                        {{
                          studioMode === 'edit'
                            ? '✏️ 展开全部 / 编辑弹窗全文'
                            : activeAbout.ceoLetter.buttonText || '展开全部'
                        }}
                      </button>
                    </div>
                  </div>
                </div>
              </section>

              <!-- 发展历程交互时间轴 -->
              <section class="canvas-section canvas-section--gray">
                <div class="canvas-section__head-inline">
                  <h2>发展历程</h2>
                  <el-button
                    v-if="studioMode === 'edit'"
                    size="small"
                    type="primary"
                    plain
                    @click="
                      drafts.ABOUT.milestones.push({
                        year: '2026',
                        title: '新发展里程碑',
                        description: '输入该年份关键突破事件',
                      });
                      aboutMilestoneIdx = drafts.ABOUT.milestones.length - 1
                    "
                  >
                    + 新增年份节点
                  </el-button>
                </div>

                <div class="milestone-years-bar">
                  <button
                    v-for="(ms, mIdx) in activeAbout.milestones"
                    :key="mIdx"
                    type="button"
                    class="cat-pill"
                    :class="{ active: aboutMilestoneIdx === mIdx }"
                    @click="aboutMilestoneIdx = mIdx"
                  >
                    <span
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        drafts.ABOUT.milestones[mIdx].year =
                          ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      {{ ms.year }}
                    </span>
                  </button>
                </div>

                <div
                  v-if="activeAbout.milestones[aboutMilestoneIdx]"
                  class="milestone-active-card"
                >
                  <div class="milestone-active-card__year">
                    {{ activeAbout.milestones[aboutMilestoneIdx].year }}
                  </div>
                  <div class="milestone-active-card__body">
                    <h3
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        drafts.ABOUT.milestones[aboutMilestoneIdx].title =
                          ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      {{ activeAbout.milestones[aboutMilestoneIdx].title }}
                    </h3>
                    <p
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        drafts.ABOUT.milestones[aboutMilestoneIdx].description =
                          ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      {{ activeAbout.milestones[aboutMilestoneIdx].description }}
                    </p>
                  </div>
                  <el-button
                    v-if="studioMode === 'edit' && activeAbout.milestones.length > 1"
                    type="danger"
                    link
                    size="small"
                    @click="
                      drafts.ABOUT.milestones.splice(aboutMilestoneIdx, 1);
                      aboutMilestoneIdx = 0
                    "
                  >
                    删除此节点
                  </el-button>
                </div>
              </section>

              <!-- 7 大城市办公网点与地图坐标联动编辑 -->
              <section class="canvas-section">
                <div class="canvas-section__head-inline">
                  <h2>全国分支机构与联系网点</h2>
                  <el-button
                    v-if="studioMode === 'edit'"
                    size="small"
                    type="primary"
                    plain
                    @click="
                      drafts.ABOUT.offices.push({
                        city: '新城市',
                        phone: '010-88888888',
                        address: '请输入详细办公地址',
                        mapX: 65,
                        mapY: 55,
                      });
                      aboutOfficeIdx = drafts.ABOUT.offices.length - 1
                    "
                  >
                    + 新增城市网点
                  </el-button>
                </div>

                <div class="office-city-tabs">
                  <button
                    v-for="(ofc, oIdx) in activeAbout.offices"
                    :key="oIdx"
                    type="button"
                    class="cat-pill"
                    :class="{ active: aboutOfficeIdx === oIdx }"
                    @click="aboutOfficeIdx = oIdx"
                  >
                    <span
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        drafts.ABOUT.offices[oIdx].city =
                          ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      {{ ofc.city }}
                    </span>
                  </button>
                </div>

                <div
                  v-if="activeAbout.offices[aboutOfficeIdx]"
                  class="office-map-stage"
                >
                  <div class="office-map-stage__card">
                    <h3
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        drafts.ABOUT.offices[aboutOfficeIdx].city =
                          ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      📍 {{ activeAbout.offices[aboutOfficeIdx].city }}
                    </h3>
                    <p>
                      <strong>电话：</strong>
                      <span
                        :contenteditable="studioMode === 'edit'"
                        @blur="
                          drafts.ABOUT.offices[aboutOfficeIdx].phone =
                            ($event.target as HTMLElement).innerText.trim()
                        "
                      >
                        {{ activeAbout.offices[aboutOfficeIdx].phone || '暂无直线电话' }}
                      </span>
                    </p>
                    <p>
                      <strong>地址：</strong>
                      <span
                        :contenteditable="studioMode === 'edit'"
                        @blur="
                          drafts.ABOUT.offices[aboutOfficeIdx].address =
                            ($event.target as HTMLElement).innerText.trim()
                        "
                      >
                        {{ activeAbout.offices[aboutOfficeIdx].address }}
                      </span>
                    </p>
                    <div v-if="studioMode === 'edit'" class="office-coord-inputs">
                      <span>地图 X%：</span>
                      <el-input-number
                        v-model="drafts.ABOUT.offices[aboutOfficeIdx].mapX"
                        :min="5"
                        :max="95"
                        size="small"
                      />
                      <span>地图 Y%：</span>
                      <el-input-number
                        v-model="drafts.ABOUT.offices[aboutOfficeIdx].mapY"
                        :min="5"
                        :max="95"
                        size="small"
                      />
                    </div>
                  </div>

                  <div class="office-map-stage__board">
                    <div
                      v-for="(ofc, oIdx) in activeAbout.offices"
                      :key="oIdx"
                      class="map-pin-dot"
                      :class="{ active: aboutOfficeIdx === oIdx }"
                      :style="{
                        left: `${ofc.mapX ?? 68}%`,
                        top: `${ofc.mapY ?? 45}%`,
                      }"
                      @click="aboutOfficeIdx = oIdx"
                    >
                      <span class="map-pin-dot__pulse"></span>
                      <span class="map-pin-dot__label">{{ ofc.city }}</span>
                    </div>
                  </div>
                </div>
              </section>
            </template>

            <!-- 子页签 2：管理团队 + 高管履历弹窗 -->
            <template v-else-if="aboutSubTab === 'team'">
              <section class="canvas-section">
                <div class="canvas-section__head-inline">
                  <h2
                    :contenteditable="studioMode === 'edit'"
                    @blur="
                      drafts.ABOUT.teamTitle =
                        ($event.target as HTMLElement).innerText.trim()
                    "
                  >
                    {{ activeAbout.teamTitle }}
                  </h2>
                  <el-button
                    v-if="studioMode === 'edit'"
                    size="small"
                    type="primary"
                    plain
                    @click="
                      drafts.ABOUT.teamMembers.push({
                        name: '新高管姓名',
                        role: '职位头衔',
                        avatar: '',
                        bio: ['在此输入高管详细履历...'],
                      })
                    "
                  >
                    + 新增高管成员
                  </el-button>
                </div>

                <div class="canvas-grid-4">
                  <div
                    v-for="(tm, tIdx) in activeAbout.teamMembers"
                    :key="tIdx"
                    class="team-member-card"
                    @click="activeTeamIdx = tIdx"
                  >
                    <div class="team-member-card__avatar">
                      <img v-if="tm.avatar" :src="tm.avatar" :alt="tm.name" />
                      <span v-else class="avatar-letter">{{ tm.name.slice(0, 1) }}</span>
                    </div>
                    <h3
                      :contenteditable="studioMode === 'edit'"
                      @click.stop
                      @blur="
                        drafts.ABOUT.teamMembers[tIdx].name =
                          ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      {{ tm.name }}
                    </h3>
                    <p
                      :contenteditable="studioMode === 'edit'"
                      @click.stop
                      @blur="
                        drafts.ABOUT.teamMembers[tIdx].role =
                          ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      {{ tm.role }}
                    </p>
                    <span class="canvas-card__link">
                      {{ studioMode === 'edit' ? '✏️ 点击打开高管履历弹窗编辑 →' : '查看履历 →' }}
                    </span>
                  </div>
                </div>
              </section>
            </template>

            <!-- 子页签 3：媒体中心 -->
            <template v-else-if="aboutSubTab === 'media'">
              <section class="canvas-section">
                <div class="canvas-section__head-inline">
                  <h2>媒体报道与官方动态</h2>
                  <el-button size="small" type="primary" plain @click="switchStudioPage('NEWS')">
                    前往新闻中心管理全部稿件 →
                  </el-button>
                </div>
                <div class="canvas-grid-3">
                  <div
                    v-for="nItem in aboutMediaNews"
                    :key="nItem.id"
                    class="canvas-news-card"
                    @click="openContentInStudio('NEWS', nItem.id)"
                  >
                    <div class="canvas-news-card__meta">
                      {{ getContentData(nItem).displayTime }}
                    </div>
                    <h3>{{ getContentData(nItem).title }}</h3>
                    <p>{{ getContentData(nItem).summary }}</p>
                    <span class="canvas-card__link">✏️ 点击在画布中编辑此稿件 →</span>
                  </div>
                </div>
              </section>
            </template>

            <!-- 子页签 4：资料共享（4 大资料分类切换 + 原位编辑） -->
            <template v-else-if="aboutSubTab === 'resources'">
              <section class="canvas-section">
                <div class="canvas-section__head-inline">
                  <h2
                    :contenteditable="studioMode === 'edit'"
                    @blur="
                      drafts.ABOUT.resourcesTitle =
                        ($event.target as HTMLElement).innerText.trim()
                    "
                  >
                    {{ activeAbout.resourcesTitle }}
                  </h2>
                  <div class="category-pills">
                    <button
                      v-for="cName in ['企业资料', '客户视频', '行业报告', 'ESG']"
                      :key="cName"
                      type="button"
                      class="cat-pill"
                      :class="{ active: aboutResourceCat === cName }"
                      @click="aboutResourceCat = cName"
                    >
                      {{ cName }}
                    </button>
                    <el-button
                      v-if="studioMode === 'edit'"
                      size="small"
                      type="primary"
                      plain
                      @click="
                        drafts.ABOUT.resources.push({
                          category: aboutResourceCat,
                          title: '新资料手册',
                          logoText: '白皮书/手册封面标题',
                          logoColor: '#4c2ce6',
                          url: '',
                        })
                      "
                    >
                      + 新增「{{ aboutResourceCat }}」卡片
                    </el-button>
                  </div>
                </div>

                <div class="canvas-grid-3" style="margin-top: 20px">
                  <template v-for="(resIt, rIdx) in activeAbout.resources" :key="rIdx">
                    <div
                      v-if="resIt.category === aboutResourceCat"
                      class="resource-card"
                    >
                      <div
                        class="resource-card__banner"
                        :style="{ backgroundColor: resIt.logoColor || '#4c2ce6' }"
                      >
                        <span
                          :contenteditable="studioMode === 'edit'"
                          @blur="
                            drafts.ABOUT.resources[rIdx].logoText =
                              ($event.target as HTMLElement).innerText.trim()
                          "
                        >
                          {{ resIt.logoText }}
                        </span>
                      </div>
                      <div class="resource-card__body">
                        <h3
                          :contenteditable="studioMode === 'edit'"
                          @blur="
                            drafts.ABOUT.resources[rIdx].title =
                              ($event.target as HTMLElement).innerText.trim()
                          "
                        >
                          {{ resIt.title }}
                        </h3>
                        <el-input
                          v-if="studioMode === 'edit'"
                          v-model="drafts.ABOUT.resources[rIdx].url"
                          size="small"
                          placeholder="下载或播放链接 URL"
                        />
                        <div class="resource-card__foot">
                          <span>{{ resIt.isVideo ? '🎬 观看视频' : '📥 下载资料' }}</span>
                          <el-button
                            v-if="studioMode === 'edit'"
                            type="danger"
                            link
                            size="small"
                            @click="drafts.ABOUT.resources.splice(rIdx, 1)"
                          >
                            删除
                          </el-button>
                        </div>
                      </div>
                    </div>
                  </template>
                </div>
              </section>
            </template>

            <!-- 子页签 5：加入我们（使命 / 愿景 / 5大价值观列表 + 招聘横幅） -->
            <template v-else-if="aboutSubTab === 'join'">
              <section class="canvas-section">
                <div class="mv-banner-grid">
                  <div class="mv-box">
                    <span class="mv-box__label">企业使命 (MISSION)</span>
                    <h3
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        drafts.ABOUT.mission =
                          ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      {{ activeAbout.mission }}
                    </h3>
                  </div>
                  <div class="mv-box">
                    <span class="mv-box__label">企业愿景 (VISION)</span>
                    <h3
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        drafts.ABOUT.vision =
                          ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      {{ activeAbout.vision }}
                    </h3>
                  </div>
                </div>

                <div class="values-list-wrap" style="margin-top: 32px">
                  <div class="canvas-section__head-inline">
                    <h2>核心价值观</h2>
                    <el-button
                      v-if="studioMode === 'edit'"
                      size="small"
                      type="primary"
                      plain
                      @click="
                        drafts.ABOUT.values.push({
                          title: '新价值观',
                          lines: ['价值观行为准则描述 1'],
                        })
                      "
                    >
                      + 新增价值观条目
                    </el-button>
                  </div>
                  <div
                    v-for="(valIt, vIdx) in activeAbout.values"
                    :key="vIdx"
                    class="value-row-item"
                  >
                    <h4
                      class="value-row-item__title"
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        drafts.ABOUT.values[vIdx].title =
                          ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      {{ valIt.title }}
                    </h4>
                    <div class="value-row-item__lines">
                      <p
                        v-for="(ln, lIdx) in valIt.lines"
                        :key="lIdx"
                        :contenteditable="studioMode === 'edit'"
                        @blur="
                          drafts.ABOUT.values[vIdx].lines[lIdx] =
                            ($event.target as HTMLElement).innerText.trim()
                        "
                      >
                        {{ ln }}
                      </p>
                    </div>
                  </div>
                </div>

                <div class="join-cta-banner" style="margin-top: 32px">
                  <h2
                    :contenteditable="studioMode === 'edit'"
                    @blur="
                      drafts.ABOUT.joinTitle =
                        ($event.target as HTMLElement).innerText.trim()
                    "
                  >
                    {{ activeAbout.joinTitle }}
                  </h2>
                  <button type="button" class="canvas-hero__btn">
                    <span
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        drafts.ABOUT.joinButtonText =
                          ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      {{ activeAbout.joinButtonText || '查看职位' }}
                    </span>
                  </button>
                </div>
              </section>
            </template>

            <!-- 关于我们弹窗 1：CEO 的信全文实景编辑弹窗 -->
            <el-dialog
              v-model="ceoModalOpen"
              title="CEO 致信全文弹窗（实景编辑）"
              width="680px"
              append-to-body
            >
              <div class="ceo-modal-body">
                <h3
                  :contenteditable="studioMode === 'edit'"
                  @blur="
                    drafts.ABOUT.ceoLetter.title =
                      ($event.target as HTMLElement).innerText.trim()
                  "
                >
                  {{ activeAbout.ceoLetter.title }}
                </h3>
                <div
                  v-for="(para, pIdx) in activeAbout.ceoLetter.fullParagraphs"
                  :key="pIdx"
                  class="ceo-modal-para"
                >
                  <p
                    :contenteditable="studioMode === 'edit'"
                    @blur="
                      drafts.ABOUT.ceoLetter.fullParagraphs[pIdx] =
                        ($event.target as HTMLElement).innerText.trim()
                    "
                  >
                    {{ para }}
                  </p>
                  <el-button
                    v-if="studioMode === 'edit'"
                    type="danger"
                    link
                    size="small"
                    @click="drafts.ABOUT.ceoLetter.fullParagraphs.splice(pIdx, 1)"
                  >
                    删除此段
                  </el-button>
                </div>
                <el-button
                  v-if="studioMode === 'edit'"
                  size="small"
                  type="primary"
                  plain
                  @click="drafts.ABOUT.ceoLetter.fullParagraphs.push('在此输入新段落正文...')"
                >
                  + 新增正文段落
                </el-button>
              </div>
            </el-dialog>

            <!-- 关于我们弹窗 2：高管个人履历实景编辑弹窗 -->
            <el-dialog
              :model-value="activeTeamIdx !== null"
              title="管理团队高管履历弹窗（实景编辑）"
              width="580px"
              append-to-body
              @close="activeTeamIdx = null"
            >
              <div
                v-if="activeTeamIdx !== null"
                class="team-modal-body"
              >
                <el-form label-position="top" size="small">
                  <el-row :gutter="12">
                    <el-col :span="12">
                      <el-form-item label="高管姓名">
                        <el-input v-model="activeTeamMember.name" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="职位头衔">
                        <el-input v-model="activeTeamMember.role" />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  <el-form-item label="头像图片 URL">
                    <el-input
                      v-model="activeTeamMember.avatar"
                      placeholder="https://..."
                    />
                  </el-form-item>
                  <div class="popover-subtitle">
                    <span>个人履历介绍段落</span>
                    <el-button
                      size="small"
                      type="primary"
                      link
                      @click="activeTeamMember.bio.push('新履历段落...')"
                    >
                      + 添加段落
                    </el-button>
                  </div>
                  <div
                    v-for="(_, bIdx) in activeTeamMember.bio"
                    :key="bIdx"
                    class="mini-row"
                    style="margin-bottom: 8px"
                  >
                    <el-input
                      v-model="activeTeamMember.bio[bIdx]"
                      type="textarea"
                      :rows="2"
                    />
                    <el-button
                      type="danger"
                      link
                      @click="activeTeamMember.bio.splice(bIdx, 1)"
                    >
                      删除
                    </el-button>
                  </div>
                </el-form>
              </div>
            </el-dialog>
          </template>

          <!-- =========================================================================
               页面 7：联系我们与预约产品展示 (activePage === 'CONTACT')
               ========================================================================= -->
          <template v-else-if="activePage === 'CONTACT'">
            <section class="subpage-hero">
              <div class="subpage-hero__inner">
                <h1
                  :contenteditable="studioMode === 'edit'"
                  @blur="
                    drafts.CONTACT.title =
                      ($event.target as HTMLElement).innerText.trim() || drafts.CONTACT.title
                  "
                >
                  {{ activeContact.title }}
                </h1>
                <p
                  :contenteditable="studioMode === 'edit'"
                  @blur="
                    drafts.CONTACT.description =
                      ($event.target as HTMLElement).innerText.trim()
                  "
                >
                  {{ activeContact.description }}
                </p>
              </div>
            </section>

            <section class="canvas-section">
              <div class="contact-studio-grid">
                <!-- 左侧企业联系信息卡 -->
                <div class="contact-info-card">
                  <h2
                    :contenteditable="studioMode === 'edit'"
                    @blur="
                      drafts.CONTACT.companyName =
                        ($event.target as HTMLElement).innerText.trim()
                    "
                  >
                    {{ activeContact.companyName }}
                  </h2>
                  <div class="contact-field-row">
                    <span class="contact-field-label">📞 咨询热线：</span>
                    <strong
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        drafts.CONTACT.phone =
                          ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      {{ activeContact.phone }}
                    </strong>
                  </div>
                  <div class="contact-field-row">
                    <span class="contact-field-label">✉️ 商务邮箱：</span>
                    <strong
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        drafts.CONTACT.email =
                          ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      {{ activeContact.email }}
                    </strong>
                  </div>
                  <div class="contact-field-row">
                    <span class="contact-field-label">📍 总部地址：</span>
                    <span
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        drafts.CONTACT.address =
                          ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      {{ activeContact.address }}
                    </span>
                  </div>
                  <div class="contact-field-row">
                    <span class="contact-field-label">🕒 服务时间：</span>
                    <span
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        drafts.CONTACT.hours =
                          ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      {{ activeContact.hours }}
                    </span>
                  </div>
                </div>

                <!-- 右侧「预约产品展示」表单实景卡片（1:1 对齐《预约产品展示.png》） -->
                <div v-if="activeContact.formEnabled" class="consult-form-card">
                  <h2
                    :contenteditable="studioMode === 'edit'"
                    @blur="
                      drafts.CONTACT.formTitle =
                        ($event.target as HTMLElement).innerText.trim()
                    "
                  >
                    {{ activeContact.formTitle || '预约产品展示' }}
                  </h2>
                  <p
                    class="consult-form-card__sub"
                    :contenteditable="studioMode === 'edit'"
                    @blur="
                      drafts.CONTACT.successText =
                        ($event.target as HTMLElement).innerText.trim()
                    "
                  >
                    {{ activeContact.successText }}
                  </p>

                  <div class="mock-form-field">
                    <label>姓名（必填）</label>
                    <input type="text" disabled placeholder="您的姓名..." />
                  </div>
                  <div class="mock-form-field">
                    <label>手机（必填）</label>
                    <input type="text" disabled placeholder="您的手机号码..." />
                  </div>
                  <div v-if="activeContact.smsVerifyEnabled" class="mock-form-field">
                    <label>短信验证码（必填）</label>
                    <div class="mock-sms-row">
                      <input type="text" disabled placeholder="您的短信验证码..." />
                      <button type="button" class="mock-sms-btn">点击获取验证码</button>
                    </div>
                  </div>
                  <div class="mock-form-field">
                    <label>公司名称（必填）</label>
                    <input type="text" disabled placeholder="您的公司名称..." />
                  </div>
                  <div class="mock-form-field">
                    <label>如果您有想了解的产品或方案，欢迎给我们留言（必填）</label>
                    <textarea
                      rows="3"
                      disabled
                      placeholder="您可以写下您的需求，我们会在第一时间为您解答..."
                    ></textarea>
                  </div>
                  <button type="button" class="mock-submit-btn">立即预约</button>
                </div>
              </div>
            </section>
          </template>

          <!-- =========================================================================
               页面 8：隐私说明 (activePage === 'PRIVACY')
               ========================================================================= -->
          <template v-else-if="activePage === 'PRIVACY'">
            <section class="subpage-hero">
              <div class="subpage-hero__inner">
                <h1
                  :contenteditable="studioMode === 'edit'"
                  @blur="
                    drafts.PRIVACY.title =
                      ($event.target as HTMLElement).innerText.trim() || drafts.PRIVACY.title
                  "
                >
                  {{ activePrivacy.title }}
                </h1>
                <p>
                  最近更新日期：
                  <span
                    :contenteditable="studioMode === 'edit'"
                    @blur="
                      drafts.PRIVACY.updatedAt =
                        ($event.target as HTMLElement).innerText.trim()
                    "
                  >
                    {{ activePrivacy.updatedAt }}
                  </span>
                </p>
              </div>
            </section>

            <section class="canvas-section">
              <div v-if="studioMode === 'edit'" class="rich-body-split">
                <el-input
                  v-model="drafts.PRIVACY.bodyHtml"
                  type="textarea"
                  :rows="10"
                  placeholder="直接输入或修改隐私说明 HTML 内容"
                />
                <div class="rich-body-preview" v-html="activePrivacy.bodyHtml"></div>
              </div>
              <div v-else class="rich-body-preview" v-html="activePrivacy.bodyHtml"></div>
            </section>
          </template>

          <!-- ==================== 全局公共底栏：深色多栏 Footer ==================== -->
          <footer
            class="canvas-footer editable-zone"
            :class="{ 'is-selected': selectedSection === 'footer' && studioMode === 'edit' }"
            @click="selectedSection = 'footer'"
          >
            <div v-if="studioMode === 'edit'" class="zone-toolbar">
              <span class="zone-toolbar__tag">全局页脚与版权备案 (SITE)</span>
              <button
                type="button"
                class="zone-btn"
                @click.stop="
                  drafts.SITE.footerGroups.push({
                    title: '新页脚分栏',
                    links: [{ label: '链接项', enabled: true, sortOrder: 100, link: { type: 'PAGE', pageCode: 'ABOUT' } }],
                  })
                "
              >
                + 新增页脚栏目
              </button>
            </div>

            <div class="canvas-footer__grid">
              <div class="canvas-footer__brand">
                <h3
                  :contenteditable="studioMode === 'edit'"
                  @blur="
                    drafts.SITE.siteName =
                      ($event.target as HTMLElement).innerText.trim() || drafts.SITE.siteName
                  "
                >
                  {{ activeSite.siteName }}
                </h3>
                <p
                  :contenteditable="studioMode === 'edit'"
                  @blur="
                    drafts.SITE.slogan = ($event.target as HTMLElement).innerText.trim()
                  "
                >
                  {{ activeSite.slogan }}
                </p>
                <div class="footer-emails">
                  <div
                    v-for="(em, eIdx) in activeSite.contactEmails"
                    :key="eIdx"
                    class="footer-email-item"
                  >
                    <strong
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        drafts.SITE.contactEmails[eIdx].email =
                          ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      {{ em.email }}
                    </strong>
                    <span
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        drafts.SITE.contactEmails[eIdx].desc =
                          ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      {{ em.desc }}
                    </span>
                  </div>
                </div>
              </div>

              <div
                v-for="(fg, fgIdx) in activeSite.footerGroups"
                :key="fgIdx"
                class="canvas-footer__col"
              >
                <h4
                  :contenteditable="studioMode === 'edit'"
                  @blur="
                    drafts.SITE.footerGroups[fgIdx].title =
                      ($event.target as HTMLElement).innerText.trim()
                  "
                >
                  {{ fg.title }}
                </h4>
                <ul>
                  <li v-for="(fl, flIdx) in fg.links" :key="flIdx">
                    <span
                      :contenteditable="studioMode === 'edit'"
                      @blur="
                        drafts.SITE.footerGroups[fgIdx].links[flIdx].label =
                          ($event.target as HTMLElement).innerText.trim()
                      "
                    >
                      {{ fl.label }}
                    </span>
                  </li>
                </ul>
              </div>
            </div>

            <div class="canvas-footer__bottom">
              <span
                :contenteditable="studioMode === 'edit'"
                @blur="
                  drafts.SITE.copyrightText =
                    ($event.target as HTMLElement).innerText.trim()
                "
              >
                {{ activeSite.copyrightText }}
              </span>
              <span
                :contenteditable="studioMode === 'edit'"
                @blur="
                  drafts.SITE.icpText = ($event.target as HTMLElement).innerText.trim()
                "
              >
                {{ activeSite.icpText }}
              </span>
              <button
                type="button"
                class="footer-privacy-link"
                @click.stop="switchStudioPage('PRIVACY')"
              >
                隐私说明
              </button>
            </div>
          </footer>
        </div>
      </main>
    </div>

    <!-- ==================== 全站发布前健康巡检抽屉 ==================== -->
    <el-drawer v-model="healthDrawerOpen" title="🩺 全站发布前健康巡检报告" size="420px">
      <div v-if="healthIssues.length === 0" class="health-empty">
        <el-result
          icon="success"
          title="全站配置与引用状态健康"
          sub-title="所有必填项与关联内容均完整，可随时安全发布上线"
        />
      </div>
      <div v-else class="health-list">
        <div
          v-for="(iss, idx) in healthIssues"
          :key="idx"
          class="health-item"
          :class="`is-${iss.level}`"
          @click="
            switchStudioPage(iss.page);
            healthDrawerOpen = false
          "
        >
          <div class="health-item__head">
            <el-tag :type="iss.level === 'error' ? 'danger' : 'warning'" size="small">
              {{ iss.level === 'error' ? '阻断项' : '提示项' }}
            </el-tag>
            <strong>{{ iss.module }}</strong>
          </div>
          <p>{{ iss.message }}</p>
          <span class="health-item__jump">点击定位到该页面 →</span>
        </div>
      </div>
    </el-drawer>
    <!-- 通用受控素材选择器 -->
    <MediaPicker v-model="pickerOpen" :kind="pickerKind" @select="onMediaPicked" />
  </div>
</template>

<style scoped>
.live-studio {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 60px);
  background: #0f172a;
  color: #1e293b;
  overflow: hidden;
}

/* 顶部主工具栏 */
.studio-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  background: #0b1120;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  color: #f8fafc;
  gap: 12px;
  flex-wrap: wrap;
}
.studio-topbar__left,
.studio-topbar__right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.studio-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  font-size: 15px;
  color: #fff;
}
.studio-brand__dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #10b981;
  box-shadow: 0 0 10px #10b981;
}
.studio-mode-switch,
.studio-viewport-switch {
  display: inline-flex;
  background: #1e293b;
  border-radius: 8px;
  padding: 2px;
  gap: 2px;
}
.mode-btn,
.vp-btn {
  border: none;
  background: transparent;
  color: #94a3b8;
  font-size: 12px;
  padding: 5px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.18s;
}
.mode-btn.active,
.vp-btn.active {
  background: #4c2ce6;
  color: #ffffff;
  font-weight: 600;
}

/* 第二层：全站 8 大页面切换栏 */
.studio-pagebar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 16px;
  background: #162033;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  gap: 12px;
  flex-wrap: wrap;
}
.studio-pagebar__tabs {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}
.page-tab-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 13px;
  border-radius: 7px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.04);
  color: #cbd5e1;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.18s;
}
.page-tab-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}
.page-tab-btn.active {
  background: linear-gradient(135deg, #4c2ce6 0%, #6366f1 100%);
  border-color: #6366f1;
  color: #ffffff;
  font-weight: 600;
  box-shadow: 0 3px 10px rgba(76, 44, 230, 0.35);
}
.page-tab-btn__dirty {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #f59e0b;
}
.studio-pagebar__context {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #94a3b8;
  font-size: 12px;
}
.detail-crumb-badge {
  color: #f8fafc;
  font-size: 13px;
}

/* 主体布局：左侧大纲 + 中央画布 */
.studio-workspace {
  flex: 1;
  display: flex;
  overflow: hidden;
}
.studio-outline {
  width: 265px;
  background: #111827;
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  color: #e2e8f0;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  flex-shrink: 0;
}
.outline-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  font-weight: 700;
  font-size: 13px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.07);
}
.outline-list {
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.outline-divider {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 6px 4px;
  font-size: 11px;
  color: #64748b;
  text-transform: uppercase;
}
.outline-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 7px;
  cursor: pointer;
  font-size: 13px;
  color: #cbd5e1;
  transition: background 0.15s;
}
.outline-item:hover {
  background: rgba(255, 255, 255, 0.06);
}
.outline-item.active {
  background: rgba(99, 102, 241, 0.22);
  color: #ffffff;
  border-left: 3px solid #6366f1;
}
.outline-item.is-disabled {
  opacity: 0.5;
}
.outline-item__label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.outline-item__actions {
  display: flex;
  align-items: center;
  gap: 4px;
}
.mini-arrow {
  background: rgba(255, 255, 255, 0.08);
  border: none;
  color: #cbd5e1;
  border-radius: 4px;
  padding: 2px 5px;
  cursor: pointer;
  font-size: 11px;
}
.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.status-dot.is-pub {
  background: #10b981;
}
.status-dot.is-draft {
  background: #f59e0b;
}

/* 中央画布容器 */
.studio-canvas-wrap {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  justify-content: center;
  background: radial-gradient(circle at top, #1e293b 0%, #0f172a 100%);
}
.studio-canvas {
  width: 100%;
  max-width: 1360px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.45);
  overflow: hidden;
  transition: width 0.25s ease;
  align-self: flex-start;
}
.studio-canvas.vp-tablet {
  max-width: 820px;
}
.studio-canvas.vp-mobile {
  max-width: 414px;
}

/* 实景可编辑热区高亮与悬浮工具条 */
.editable-zone {
  position: relative;
}
.mode-edit .editable-zone:hover {
  outline: 2px dashed rgba(76, 44, 230, 0.55);
  outline-offset: -2px;
}
.mode-edit .editable-zone.is-selected {
  outline: 2px solid #4c2ce6;
  outline-offset: -2px;
}
.mode-edit [contenteditable='true'] {
  cursor: text;
  border-radius: 4px;
  transition: background 0.15s, box-shadow 0.15s;
}
.mode-edit [contenteditable='true']:hover {
  background: rgba(76, 44, 230, 0.06);
  box-shadow: 0 0 0 1px rgba(76, 44, 230, 0.35);
}
.mode-edit [contenteditable='true']:focus {
  outline: none;
  background: rgba(76, 44, 230, 0.1);
  box-shadow: 0 0 0 2px #4c2ce6;
}

.zone-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  background: rgba(15, 23, 42, 0.9);
  color: #f8fafc;
  font-size: 12px;
  flex-wrap: wrap;
}
.zone-toolbar__tag {
  font-weight: 700;
  color: #a5b4fc;
  margin-right: 4px;
}
.zone-btn {
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.18);
  color: #ffffff;
  padding: 3px 10px;
  border-radius: 5px;
  font-size: 12px;
  cursor: pointer;
}
.zone-btn:hover {
  background: #4c2ce6;
}
.zone-btn.danger:hover {
  background: #dc2626;
}

/* 画布公共顶栏 */
.canvas-header {
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
}
.canvas-header__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 36px;
}
.canvas-logo {
  display: flex;
  align-items: center;
  cursor: pointer;
}
.canvas-logo__img {
  height: 34px;
}
.canvas-logo__text {
  font-size: 22px;
  font-weight: 800;
  color: #4c2ce6;
}
.canvas-nav {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}
.canvas-nav__item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 15px;
  color: #1e293b;
  cursor: pointer;
  padding: 4px 6px;
  border-radius: 6px;
}
.canvas-nav__item.active {
  color: #4c2ce6;
  font-weight: 700;
}
.canvas-nav__jump {
  border: none;
  background: #eef2ff;
  color: #4c2ce6;
  font-size: 11px;
  border-radius: 4px;
  padding: 1px 4px;
  cursor: pointer;
}
.canvas-header__actions {
  display: flex;
  align-items: center;
  gap: 14px;
}
.canvas-lang {
  font-size: 13px;
  color: #475569;
}
.canvas-trial-btn {
  background: #0b0f19;
  color: #ffffff;
  border: none;
  padding: 8px 18px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}

/* 下拉大菜单编辑区 */
.canvas-mega {
  background: #f1f5f9;
  border-top: 1px solid #e2e8f0;
  padding: 24px 36px;
}
.canvas-mega__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  gap: 20px;
}
.canvas-mega__col {
  background: #ffffff;
  padding: 14px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}
.canvas-mega__col-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 8px;
  margin-bottom: 10px;
}
.canvas-mega__link-row {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 6px;
}
.inline-clean-input {
  width: 100%;
  border: 1px dashed transparent;
  background: transparent;
  font-size: 13px;
  padding: 3px 6px;
  border-radius: 4px;
}
.inline-clean-input:hover,
.inline-clean-input:focus {
  border-color: #6366f1;
  background: #f8fafc;
  outline: none;
}
.inline-clean-input.strong {
  font-weight: 700;
  font-size: 14px;
}
.mini-jump-btn {
  border: none;
  background: #e0e7ff;
  color: #4338ca;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  cursor: pointer;
  flex-shrink: 0;
}
.mini-del-btn {
  border: none;
  background: #fee2e2;
  color: #dc2626;
  font-size: 12px;
  padding: 1px 6px;
  border-radius: 4px;
  cursor: pointer;
}

/* 首页 Hero 焦点图与底部极简短横条切换器 */
.canvas-hero {
  position: relative;
  min-height: 400px;
  background: linear-gradient(135deg, #0b0f19 0%, #1e1b4b 60%, #311042 100%);
  background-size: cover;
  background-position: center;
  color: #ffffff;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.canvas-hero__content {
  padding: 64px 48px 48px;
  max-width: 780px;
}
.canvas-hero__kicker {
  font-size: 14px;
  color: #a5b4fc;
  margin-bottom: 12px;
  letter-spacing: 1px;
}
.canvas-hero__title {
  font-size: 40px;
  font-weight: 800;
  line-height: 1.22;
  margin: 0 0 16px;
}
.canvas-hero__subtitle {
  font-size: 17px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0 0 28px;
  line-height: 1.6;
}
.canvas-hero__btn {
  border: none;
  padding: 12px 28px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 6px;
  cursor: pointer;
  background: #4c2ce6;
  color: #ffffff;
}
.canvas-hero__bars {
  display: flex;
  justify-content: center;
  gap: 10px;
  padding-bottom: 22px;
}
.canvas-hero__bar {
  width: 36px;
  height: 4px;
  border: none;
  border-radius: 2px;
  background: rgba(255, 255, 255, 0.3);
  cursor: pointer;
  transition: all 0.2s;
}
.canvas-hero__bar.active {
  width: 52px;
  background: #ffffff;
}

/* 通用画布区块 */
.canvas-section {
  padding: 52px 44px;
  background: #ffffff;
}
.canvas-section--gray {
  background: #f8fafc;
}
.is-hidden-block {
  opacity: 0.45;
}
.canvas-section__head {
  text-align: center;
  max-width: 680px;
  margin: 0 auto 36px;
}
.canvas-section__head-inline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 12px;
}
.canvas-section__title {
  font-size: 28px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 10px;
}
.canvas-section__subtitle {
  font-size: 15px;
  color: #64748b;
  margin: 0;
}

/* 首页交替式业务分组 (service-group-stage) */
.service-groups-wrap {
  display: flex;
  flex-direction: column;
  gap: 32px;
}
.service-group-stage {
  display: grid;
  grid-template-columns: 1fr 1.25fr;
  gap: 32px;
  align-items: center;
  padding: 28px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}
.service-group-stage.is-reversed {
  direction: rtl;
}
.service-group-stage.is-reversed > * {
  direction: ltr;
}
.service-group-stage__tag {
  display: inline-block;
  font-size: 12px;
  font-weight: 700;
  color: #4c2ce6;
  background: #eef2ff;
  padding: 4px 10px;
  border-radius: 999px;
  margin-bottom: 10px;
}
.service-group-stage__title {
  font-size: 24px;
  font-weight: 800;
  margin: 0 0 10px;
}
.service-group-stage__desc {
  color: #475569;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 14px;
}
.service-group-stage__controls {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 14px;
}
.service-group-stage__cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

/* 通用网格与卡片 */
.canvas-grid-2 {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}
.canvas-grid-3 {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}
.canvas-grid-4 {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.canvas-card,
.canvas-case-card,
.canvas-news-card,
.canvas-list-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 22px;
  cursor: pointer;
  transition: transform 0.18s, box-shadow 0.18s;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.canvas-card:hover,
.canvas-case-card:hover,
.canvas-news-card:hover,
.canvas-list-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.08);
  border-color: #6366f1;
}
.canvas-card__link {
  display: inline-block;
  margin-top: 12px;
  font-size: 13px;
  font-weight: 600;
  color: #4c2ce6;
}
.hl-pill {
  display: inline-block;
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 999px;
  background: #eef2ff;
  color: #4c2ce6;
  font-weight: 600;
  margin-bottom: 8px;
}

/* 合作客户 Logo 墙 */
.customer-logo-wall {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
  gap: 14px;
}
.customer-logo-pill {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  font-weight: 700;
  color: #334155;
  background: #f8fafc;
}

/* 子页面公共头部与分类胶囊栏 */
.subpage-hero {
  background: linear-gradient(135deg, #0f172a 0%, #1e1b4b 100%);
  color: #ffffff;
  padding: 48px 44px;
}
.subpage-hero__kicker {
  font-size: 12px;
  color: #a5b4fc;
  letter-spacing: 1px;
}
.subpage-hero h1 {
  font-size: 32px;
  margin: 8px 0 10px;
}
.subpage-hero p {
  color: #cbd5e1;
  font-size: 14px;
  margin: 0;
}

.category-bar-section {
  padding: 20px 44px 0;
  background: #ffffff;
}
.category-pills {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.cat-pill-wrap {
  display: inline-flex;
  align-items: center;
  background: #f1f5f9;
  border-radius: 999px;
  padding-right: 6px;
}
.cat-pill {
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  color: #334155;
  padding: 7px 16px;
  border-radius: 999px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
}
.cat-pill.active {
  background: #4c2ce6;
  border-color: #4c2ce6;
  color: #ffffff;
  font-weight: 600;
}
.cat-pill-edit {
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 12px;
  padding: 2px 4px;
}
.cat-pill--add {
  border-style: dashed;
  color: #4c2ce6;
}
.cat-pill--primary {
  background: #0f172a;
  color: #ffffff;
}

/* 新闻中心置顶头条大卡 */
.news-pinned-section {
  padding: 24px 44px 0;
}
.news-pinned-card {
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: 28px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 28px;
  cursor: pointer;
}
.news-pinned-card__badges {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  font-size: 12px;
}
.badge-top {
  background: #dc2626;
  color: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 600;
}
.badge-cat {
  color: #4c2ce6;
  font-weight: 600;
}
.cover-placeholder {
  height: 210px;
  border-radius: 10px;
  background: linear-gradient(135deg, #e0e7ff 0%, #ede9fe 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4338ca;
  font-weight: 700;
}

.canvas-list-card__top,
.canvas-list-card__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #64748b;
}
.canvas-list-card__cat {
  color: #4c2ce6;
  font-weight: 600;
}
.canvas-list-card--add {
  border: 2px dashed #cbd5e1;
  background: #f8fafc;
  align-items: center;
  justify-content: center;
  text-align: center;
}
.add-card-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  color: #475569;
}
.add-card-icon {
  font-size: 28px;
  color: #4c2ce6;
}

/* 客户成功故事证言卡 */
.story-quote-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-left: 4px solid #4c2ce6;
  border-radius: 10px;
  padding: 22px;
}
.story-quote-card__text {
  font-size: 16px;
  font-style: italic;
  color: #0f172a;
  margin: 0 0 14px;
  line-height: 1.6;
}
.story-quote-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: #475569;
}
.story-quote-card__company {
  margin-left: 10px;
  color: #4c2ce6;
  font-weight: 700;
}

/* 详情内嵌画布 */
.detail-hero {
  background: linear-gradient(135deg, #0b0f19 0%, #1e1b4b 100%);
  color: #ffffff;
  padding: 24px 44px 44px;
}
.detail-hero__bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 10px;
}
.detail-hero__meta-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  font-size: 12px;
}
.detail-hero__title {
  font-size: 34px;
  margin: 0 0 12px;
}
.detail-hero__summary {
  font-size: 16px;
  color: #cbd5e1;
  margin: 0;
}
.metric-stat-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  border: 1px solid #e2e8f0;
}
.metric-stat-card__val {
  font-size: 36px;
  font-weight: 800;
  color: #4c2ce6;
}
.metric-stat-card__label {
  font-size: 14px;
  color: #475569;
  margin-top: 6px;
}
.role-tabs-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}
.role-tab-panel {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 24px;
}
.role-bullet-list {
  margin: 12px 0;
  padding-left: 20px;
}
.role-bullet-list li {
  margin-bottom: 8px;
}
.case-meta-banner {
  display: flex;
  gap: 32px;
  padding: 16px 22px;
  background: #f8fafc;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}
.rich-body-split {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
.rich-body-preview {
  padding: 20px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  line-height: 1.75;
}

/* 关于我们 5 大子页签与组件 */
.about-hero {
  position: relative;
  background: linear-gradient(135deg, #090d16 0%, #1e1b4b 100%);
  color: #ffffff;
  padding: 56px 44px 68px;
  text-align: center;
}
.about-subtabs {
  position: relative;
  max-width: 760px;
  margin: 28px auto -42px;
  background: #ffffff;
  border-radius: 10px;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.12);
  display: flex;
  justify-content: space-around;
  padding: 6px;
}
.about-subtab-btn {
  border: none;
  background: transparent;
  padding: 12px 20px;
  font-size: 15px;
  color: #334155;
  cursor: pointer;
  border-bottom: 3px solid transparent;
}
.about-subtab-btn.active {
  color: #4c2ce6;
  font-weight: 700;
  border-bottom-color: #4c2ce6;
}
.about-intro-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 32px;
  align-items: center;
  margin-top: 16px;
}
.ceo-letter-card {
  display: grid;
  grid-template-columns: 1fr 1.6fr;
  gap: 28px;
  background: linear-gradient(135deg, #3b1d9e 0%, #4c2ce6 100%);
  color: #ffffff;
  padding: 36px;
  border-radius: 14px;
}
.ceo-letter-card__left blockquote {
  font-size: 20px;
  font-weight: 700;
  line-height: 1.5;
  margin: 0;
}
.ceo-letter-card__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 18px;
}
.ceo-expand-btn {
  background: #ffffff;
  color: #4c2ce6;
  border: none;
  padding: 8px 18px;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
}
.milestone-years-bar,
.office-city-tabs {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}
.milestone-active-card {
  display: flex;
  align-items: center;
  gap: 24px;
  background: #ffffff;
  padding: 24px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}
.milestone-active-card__year {
  font-size: 36px;
  font-weight: 800;
  color: #4c2ce6;
}
.office-map-stage {
  display: grid;
  grid-template-columns: 1fr 1.3fr;
  gap: 24px;
  align-items: stretch;
}
.office-map-stage__card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 24px;
}
.office-coord-inputs {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 14px;
  font-size: 12px;
  flex-wrap: wrap;
}
.office-map-stage__board {
  position: relative;
  min-height: 260px;
  background: radial-gradient(circle at center, #eef2ff 0%, #e0e7ff 100%);
  border-radius: 12px;
  border: 1px solid #c7d2fe;
  overflow: hidden;
}
.map-pin-dot {
  position: absolute;
  transform: translate(-50%, -50%);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}
.map-pin-dot__pulse {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #64748b;
}
.map-pin-dot.active .map-pin-dot__pulse {
  width: 14px;
  height: 14px;
  background: #4c2ce6;
  box-shadow: 0 0 12px #4c2ce6;
}
.map-pin-dot__label {
  font-size: 12px;
  background: rgba(255, 255, 255, 0.9);
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 600;
}

.team-member-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 22px;
  text-align: center;
  cursor: pointer;
}
.team-member-card__avatar {
  width: 76px;
  height: 76px;
  border-radius: 50%;
  margin: 0 auto 12px;
  background: #4c2ce6;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  font-weight: 700;
  overflow: hidden;
}
.team-member-card__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.resource-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  background: #ffffff;
}
.resource-card__banner {
  height: 110px;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  padding: 12px;
  text-align: center;
}
.resource-card__body {
  padding: 16px;
}
.resource-card__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 12px;
  color: #4c2ce6;
}

.mv-banner-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
.mv-box {
  background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%);
  color: #ffffff;
  padding: 28px;
  border-radius: 12px;
}
.mv-box__label {
  font-size: 12px;
  color: #a5b4fc;
}
.value-row-item {
  display: grid;
  grid-template-columns: 180px 1fr;
  gap: 20px;
  padding: 16px 0;
  border-bottom: 1px solid #e2e8f0;
}
.join-cta-banner {
  background: #0f172a;
  color: #ffffff;
  padding: 36px;
  border-radius: 14px;
  text-align: center;
}

/* 联系我们与预约产品展示画布 */
.contact-studio-grid {
  display: grid;
  grid-template-columns: 1fr 1.15fr;
  gap: 32px;
}
.contact-info-card,
.consult-form-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 28px;
}
.contact-field-row {
  margin-top: 14px;
  font-size: 14px;
}
.contact-field-label {
  color: #64748b;
}
.mock-form-field {
  margin-bottom: 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.mock-form-field label {
  font-size: 13px;
  color: #475569;
}
.mock-form-field input,
.mock-form-field textarea {
  border: 1px solid #e2e8f0;
  background: #ffffff;
  padding: 10px 12px;
  border-radius: 6px;
  font-size: 13px;
}
.mock-sms-row {
  display: flex;
  gap: 8px;
}
.mock-sms-row input {
  flex: 1;
}
.mock-sms-btn {
  background: #0b0f19;
  color: #ffffff;
  border: none;
  padding: 0 14px;
  border-radius: 6px;
  font-size: 12px;
}
.mock-submit-btn {
  width: 100%;
  background: #0b0f19;
  color: #ffffff;
  border: none;
  padding: 12px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 6px;
}

/* 画布公共页脚 */
.canvas-footer {
  background: #0b0f19;
  color: #94a3b8;
  padding: 48px 44px 28px;
}
.canvas-footer__grid {
  display: grid;
  grid-template-columns: 1.5fr repeat(auto-fit, minmax(140px, 1fr));
  gap: 28px;
  padding-bottom: 32px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.canvas-footer__brand h3,
.canvas-footer__col h4 {
  color: #ffffff;
  margin-top: 0;
}
.canvas-footer__col ul {
  list-style: none;
  padding: 0;
  margin: 0;
}
.canvas-footer__col li {
  margin-bottom: 8px;
  font-size: 13px;
}
.footer-emails {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 12px;
}
.footer-email-item strong {
  color: #e2e8f0;
  margin-right: 8px;
}
.canvas-footer__bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 20px;
  font-size: 12px;
  flex-wrap: wrap;
  gap: 10px;
}
.footer-privacy-link {
  border: none;
  background: transparent;
  color: #cbd5e1;
  cursor: pointer;
  text-decoration: underline;
}

/* 巡检抽屉 */
.health-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.health-item {
  padding: 14px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  cursor: pointer;
}
.health-item.is-error {
  background: #fef2f2;
  border-color: #fecaca;
}
.health-item.is-warning {
  background: #fffbeb;
  border-color: #fde68a;
}
.health-item__head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.health-item__jump {
  font-size: 12px;
  color: #4c2ce6;
  font-weight: 600;
}
</style>
