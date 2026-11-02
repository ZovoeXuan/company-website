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
  client?: string
  industry?: string
  results?: ResultMetric[]
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

export interface ProductSummary {
  id: string
  title: string
  summary: string
  categoryId: string | null
  categoryName: string | null
  cover: MediaRef | null
}

export interface ProductDetail extends ProductSummary {
  bodyHtml: string
  capabilities?: string[]
  relatedCases?: CaseSummary[]
}

export interface SolutionSummary {
  id: string
  title: string
  summary: string
  industry: string
  cover: MediaRef | null
}

export interface SolutionDetail extends SolutionSummary {
  challenge: string
  approach: string[]
  outcome: Metric[]
  relatedProducts?: ProductSummary[]
}

export interface Metric {
  value: string
  label: string
}

export interface ResultMetric {
  label: string
  value: string
}

export interface CaseSummary {
  id: string
  title: string
  industry: string
  summary: string
  results: ResultMetric[]
  cover: MediaRef | null
}

export interface CaseDetail extends CaseSummary {
  client: string
  background: string
  actions: string[]
  quote: { text: string; author: string } | null
  relatedProducts?: ProductSummary[]
}

export interface NewsSummary {
  id: string
  title: string
  summary: string
  displayTime: string
  categoryName: string | null
  cover: MediaRef | null
}

export interface NewsDetail extends NewsSummary {
  bodyHtml: string
  prev: { id: string; title: string } | null
  next: { id: string; title: string } | null
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
