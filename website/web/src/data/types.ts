// 对应源码路径: website/web/src/data/types.ts
/**
 * 与《公司官网后台及前后端联动开发文档》及《参考站核对.md》对齐的前端展示 DTO 类型定义。
 */

export interface MediaRef {
  id: string
  url: string
  alt: string
  width: number
  height: number
  type?: 'IMAGE' | 'VIDEO'
  durationMs?: number | null
}

export interface PageResult<T> {
  items: T[]
  page: number
  pageSize: number
  total: number
}

export interface NavItem {
  label: string
  path: string
  url?: string | null
  newWindow?: boolean
  linkType?: string | null
  children?: NavItem[]
}

export interface MegaSubItem {
  label: string
  path?: string
  tag?: string
  isVideo?: boolean
  videoUrl?: string
  videoTitle?: string
}

export interface MegaColumn {
  title: string
  items: MegaSubItem[]
}

export interface QrCodeItem {
  label: string
  imageUrl?: string | null
  mediaId?: string | number | null
}

export interface ContactEmailItem {
  email: string
  desc: string
}

export interface XiaoQiFaqPair {
  q: string
  a: string
  thought?: string
}

export interface XiaoQiConfig {
  enabled?: boolean
  name?: string
  roleTitle?: string
  headerLine1?: string
  headerLine2?: string
  welcomeTitle?: string
  welcomeSubtitle?: string
  certBadge?: string
  capabilities?: string[]
  questions?: string[]
  faqPairs?: XiaoQiFaqPair[]
  deepThinkEnabled?: boolean
  deepThinkModel?: string
  disclaimer?: string
  apiUrl?: string
  apiKey?: string
  modelName?: string
  systemPrompt?: string
}

export interface SiteConfig {
  siteName: string
  slogan: string
  logoUrl?: string | null
  logoAlt?: string
  faviconUrl?: string | null
  defaultSeo?: { title: string; description: string }
  nav: NavItem[]
  megaColumns?: MegaColumn[]
  megaCta?: { text: string; linkText: string } | null
  xiaoqiConfig?: XiaoQiConfig | null
  contactEmails?: ContactEmailItem[]
  contact: {
    companyName?: string
    phone: string
    email: string
    address: string
    hours: string
    serviceHours?: string
    mapUrl?: string
    qrImageUrl?: string
    qrLabel?: string
    smsVerifyEnabled?: boolean
  }
  footer: {
    copyright: string
    icp: string
    icpUrl?: string
    policeText?: string
    policeUrl?: string
    groups: Array<{ title: string; links: NavItem[] }>
    links: NavItem[]
    qrCodes?: QrCodeItem[]
  }
  formEnabled?: boolean
  privacyVersion?: number | null
}

export interface HomeLink {
  type: string
  url?: string
  newWindow?: boolean
  contentType?: string
  contentId?: string
}

export interface HomeBlock {
  title: string
  subtitle: string
}

export interface HomeHeroItem {
  key: string
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
  desktopImage: MediaRef | null
  mobileImage: MediaRef | null
  buttonText: string
  link: HomeLink | null
}

export interface HomeMetric {
  key: string
  displayValue: string
  unit: string
  label: string
  description: string
}

export interface HomeHotspotItem {
  label: string
  x: number
  y: number
  labelX: number
  labelY: number
  align?: 'left' | 'right'
  path?: string
  tag?: string
  description?: string
}

export interface HomeShowcaseSubItem {
  title: string
  prefix?: string
  path?: string
  sceneImage?: string
  callouts?: Array<{ label: string; x: number; y: number }>
}

export interface HomeShowcaseStage {
  key: string
  title: string
  description: string
  morePath?: string
  mode: 'RADAR' | 'PAYMENT' | 'SOLUTIONS'
  alertLabel?: string
  alertBaseValue?: number
  alertUnit?: string
  paymentTitle?: string
  paymentSubtitle?: string
  paymentAmount?: string
  subItems: HomeShowcaseSubItem[]
}

export interface HomeCard {
  id: string
  contentType?: string
  title: string
  summary: string
  cover: MediaRef | null
  url: string
  customerName?: string
  industry?: string
  outcomes?: Array<{ displayValue: string; label: string }>
}

export interface HomeServiceGroup {
  key: string
  title: string
  description: string
  image: MediaRef | null
  moreLink: HomeLink | null
  items: HomeCard[]
}

export interface HomeCustomer {
  id: string
  name: string
  logo: MediaRef
  url: string | null
}

export interface HomeNewsItem {
  id: string
  title: string
  summary: string
  displayTime: string
  url: string
  cover?: MediaRef | null
}

export interface HomeView {
  version: number | null
  publishedAt: string | null
  sectionOrder?: string[]
  hero: { items: HomeHeroItem[] }
  metrics: (HomeBlock & {
    brandPrefix?: string
    centerImageUrl?: string
    hotspots?: HomeHotspotItem[]
    items: HomeMetric[]
  }) | null
  showcaseStages?: HomeShowcaseStage[]
  services: (HomeBlock & { groups: HomeServiceGroup[] }) | null
  cases: (HomeBlock & { items: HomeCard[]; moreUrl: string }) | null
  customers: (HomeBlock & { items: HomeCustomer[] }) | null
  news: (HomeBlock & { mode: string; items: HomeNewsItem[]; moreUrl: string }) | null
  consultation: { title: string; subtitle: string; buttonText: string; link: HomeLink }
}

export interface Category {
  id: string
  name: string
  sortOrder?: number
}

export interface SeoMeta {
  title?: string
  description?: string
}

export interface TitledItem {
  key?: string
  title: string
  description: string
  tag?: string
  icon?: MediaRef | null
}

export interface OutcomeMetric {
  key?: string
  displayValue: string
  label: string
}

/** 《参考站核对.md》D1：产品详情按角色拆分价值项（司机端 / 安全员 / 车队长） */
export interface ProductRoleTab {
  role: string
  headline: string
  bullets: string[]
}

/** 《参考站核对.md》D1：产品详情端侧工具展示项（WEB + APP + 微信群） */
export interface ProductTerminalTool {
  name: string
  badge?: string
  description: string
}

export interface ContentSummary {
  id: string
  title: string
  summary: string
  cover: MediaRef | null
  coverAlt?: string
  categoryId: string | null
  categoryName: string | null
  industry?: string
  customerName?: string
  outcomes?: OutcomeMetric[]
  displayTime?: string
}

export interface ProductSummary extends ContentSummary {}

export interface ProductDetail extends ProductSummary {
  bodyHtml?: string
  seo?: SeoMeta
  hero?: MediaRef | null
  features?: TitledItem[]
  /** D1 扩展：硬件/核心感知模块（如 ADAS / DSM / BSD 三段图文） */
  hardwareModules?: TitledItem[]
  /** D1 扩展：算法与量化效果指标（如 事故率下降 17%） */
  effectMetrics?: OutcomeMetric[]
  /** D1 扩展：按角色拆分价值（如 司机端 / 安全员 / 车队长） */
  roleTabs?: ProductRoleTab[]
  /** D1 扩展：端侧工具矩阵（WEB + APP + 微信群） */
  terminalTools?: ProductTerminalTool[]
  relatedCases?: CaseSummary[]
}

export interface SolutionSummary extends ContentSummary {}

export interface SolutionDetail extends SolutionSummary {
  bodyHtml?: string
  seo?: SeoMeta
  hero?: MediaRef | null
  pains?: TitledItem[]
  benefits?: TitledItem[]
  relatedProducts?: ProductSummary[]
}

export interface CaseSummary extends ContentSummary {}

export interface CaseDetail extends CaseSummary {
  bodyHtml?: string
  seo?: SeoMeta
  hero?: MediaRef | null
  quoteText?: string
  quoteAuthor?: string
  relatedProducts?: ProductSummary[]
  relatedSolutions?: SolutionSummary[]
}

export interface NewsSummary extends ContentSummary {}

export interface NewsDetail extends NewsSummary {
  bodyHtml?: string
  seo?: SeoMeta
  author?: string
  sourceName?: string
  sourceUrl?: string
  prev?: { id: string; title: string } | null
  next?: { id: string; title: string } | null
}

export interface AboutValueItem {
  title: string
  lines: string[]
}

export interface AboutOfficeItem {
  city: string
  phone: string
  address: string
  image?: string
  mapX?: number
  mapY?: number
}

export interface AboutCeoLetter {
  leftQuote: string
  title: string
  excerpt: string[]
  signature: string
  buttonText?: string
  fullParagraphs: string[]
  bgImage?: string
}

export interface AboutTeamMember {
  name: string
  role: string
  avatar?: string
  bio: string[]
}

export interface AboutResourceItem {
  category: '企业资料' | '客户视频' | '行业报告' | 'ESG' | string
  title: string
  cover?: string
  logoText?: string
  logoColor?: string
  url?: string
  isVideo?: boolean
}

export interface AboutStoryItem {
  quote: string
  author: string
  company: string
  logoText?: string
  cover?: string
  videoUrl?: string
}

export interface AboutQualificationItem {
  name: string
  description?: string
  mediaId?: string | number | null
  imageUrl?: string | null
}

export interface AboutPage {
  title?: string
  heroTitle?: string
  introTitle?: string
  intro: string
  introImage?: string
  bodyHtml?: string
  ceoLetter?: AboutCeoLetter | null
  teamTitle?: string
  teamMembers?: AboutTeamMember[]
  resourcesTitle?: string
  resources?: AboutResourceItem[]
  customerStories?: AboutStoryItem[]
  mission: string
  vision?: string
  values?: AboutValueItem[]
  milestones: Array<{ year: string; title: string; description: string; sortOrder?: number }>
  qualifications: AboutQualificationItem[]
  joinTitle?: string
  joinButtonText?: string
  joinUrl?: string
  joinImage?: string
  offices?: AboutOfficeItem[]
}

export interface PrivacyPage {
  title?: string
  updatedAt: string
  bodyHtml: string
}

export interface ContactPage {
  title: string
  companyName?: string
  description: string
  formTitle?: string
  successText: string
  formEnabled: boolean
  smsVerifyEnabled?: boolean
  phone?: string
  email?: string
  address?: string
  hours?: string
  serviceHours?: string
  mapUrl?: string
  qrMediaId?: string | number | null
  qrImageUrl?: string | null
  qrLabel?: string
}

export interface LeadSubmitPayload {
  name: string
  phone: string
  companyName: string
  demand: string
  sourcePath: string
  sourcePosition: string
  contentType?: string
  contentId?: number
  consent: boolean
  consentVersion: number
}

export interface LeadSubmitResult {
  accepted: boolean
  receiptNo: string
}
