/**
 * 与后端公开 DTO 对齐的展示类型。
 * 后端接口就绪后，把 api/public.ts 里的 mock 换成 apiGet 即可，页面不需要改。
 */

export interface MediaRef {
  id: string
  url: string
  alt: string
  width: number
  height: number
}

export interface PageResult<T> {
  items: T[]
  page: number
  pageSize: number
  total: number
}

export interface NavItem {
  label: string
  /** 站内路径；纯外链项此处为空字符串，改用 url */
  path: string
  url?: string | null
  newWindow?: boolean
  children?: NavItem[]
}

export interface SiteConfig {
  siteName: string
  slogan: string
  logoUrl?: string | null
  logoAlt?: string
  faviconUrl?: string | null
  defaultSeo?: { title: string; description: string }
  nav: NavItem[]
  contact: {
    phone: string
    email: string
    address: string
    hours: string
  }
  footer: {
    copyright: string
    icp: string
    icpUrl?: string
    policeText?: string
    policeUrl?: string
    groups: Array<{ title: string; links: NavItem[] }>
    links: NavItem[]
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
}

export interface HomeView {
  version: number | null
  publishedAt: string | null
  hero: { items: HomeHeroItem[] }
  metrics: (HomeBlock & { items: HomeMetric[] }) | null
  services: (HomeBlock & { groups: HomeServiceGroup[] }) | null
  cases: (HomeBlock & { items: HomeCard[]; moreUrl: string }) | null
  customers: (HomeBlock & { items: HomeCustomer[] }) | null
  news: (HomeBlock & { mode: string; items: HomeNewsItem[]; moreUrl: string }) | null
  consultation: { title: string; subtitle: string; buttonText: string; link: HomeLink }
}

export interface Category {
  id: string
  name: string
}

export interface SeoMeta {
  title?: string
  description?: string
}

export interface TitledItem {
  title: string
  description: string
  icon?: MediaRef | null
}

export interface OutcomeMetric {
  displayValue: string
  label: string
}

/** 列表与首页卡片共用的摘要结构，字段与后端公开 DTO 一致 */
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

export interface AboutPage {
  intro: string
  mission: string
  milestones: Array<{ year: string; title: string; description: string }>
  qualifications: Array<{ name: string; description: string }>
}

export interface PrivacyPage {
  updatedAt: string
  bodyHtml: string
}

export interface ContactPage {
  title: string
  description: string
  successText: string
  formEnabled: boolean
  phone?: string
  email?: string
  address?: string
  hours?: string
}
