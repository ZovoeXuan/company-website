/**
 * 与后端公开 DTO 对齐的展示类型。
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
  cover?: MediaRef | null
}

export interface HomeView {
  version: number | null
  publishedAt: string | null
  sectionOrder?: string[]
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

export interface AboutPage {
  heroTitle?: string
  introTitle?: string
  intro: string
  introImage?: string
  mission: string
  vision?: string
  values?: AboutValueItem[]
  milestones: Array<{ year: string; title: string; description: string }>
  qualifications: Array<{ name: string; description: string }>
  joinTitle?: string
  joinButtonText?: string
  offices?: AboutOfficeItem[]
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
