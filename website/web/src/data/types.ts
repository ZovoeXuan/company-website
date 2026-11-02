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
  path: string
}

export interface SiteConfig {
  siteName: string
  slogan: string
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
    links: NavItem[]
  }
}

export interface Metric {
  value: string
  label: string
}

export interface ServiceGroup {
  key: string
  title: string
  summary: string
  contentId: string | null
}

export interface HomeView {
  hero: {
    eyebrow: string
    title: string
    subtitle: string
    primary: NavItem
    secondary: NavItem
    media: MediaRef | null
  }
  metrics: Metric[]
  services: ServiceGroup[]
  featuredCases: CaseSummary[]
  customers: Array<{ id: string; name: string }>
  latestNews: NewsSummary[]
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
  capabilities: string[]
  relatedCases: CaseSummary[]
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
  relatedProducts: ProductSummary[]
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
  relatedProducts: ProductSummary[]
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
