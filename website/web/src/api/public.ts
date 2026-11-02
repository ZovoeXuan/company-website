/**
 * 公开内容数据源。
 *
 * 当前 USE_MOCK = true：使用 data/mock.ts 的示例数据，便于前端独立开发。
 * 后端 /api/public/* 就绪后，把 USE_MOCK 改为 false 并实现下方注释中的请求即可，
 * 页面与组件不需要修改（返回结构已按公开 DTO 对齐）。
 */
import { apiGet } from './http'
import * as mock from '@/data/mock'
import type {
  AboutPage,
  CaseDetail,
  CaseSummary,
  HomeView,
  NewsDetail,
  NewsSummary,
  PageResult,
  PrivacyPage,
  ProductDetail,
  ProductSummary,
  SiteConfig,
  SolutionDetail,
  SolutionSummary,
} from '@/data/types'

const USE_MOCK = true
const MOCK_LATENCY = 60

function delay<T>(value: T): Promise<T> {
  return new Promise((resolve) => setTimeout(() => resolve(value), MOCK_LATENCY))
}

function paginate<T>(items: T[], page: number, pageSize: number): PageResult<T> {
  const start = (page - 1) * pageSize
  return {
    items: items.slice(start, start + pageSize),
    page,
    pageSize,
    total: items.length,
  }
}

export function getSite(): Promise<SiteConfig> {
  if (!USE_MOCK) return apiGet<SiteConfig>('/api/public/site-config')
  return delay(mock.site)
}

export function getHome(): Promise<HomeView> {
  if (!USE_MOCK) return apiGet<HomeView>('/api/public/home')
  return delay(mock.home)
}

export function getProductCategories(): Promise<Array<{ id: string; name: string }>> {
  if (!USE_MOCK) return apiGet('/api/public/categories?type=PRODUCT')
  return delay(mock.productCategories)
}

export function getProducts(params: {
  page?: number
  pageSize?: number
  categoryId?: string | null
  keyword?: string
}): Promise<PageResult<ProductSummary>> {
  const page = params.page ?? 1
  const pageSize = params.pageSize ?? 9
  if (!USE_MOCK) {
    const query = new URLSearchParams({ page: String(page), pageSize: String(pageSize) })
    if (params.categoryId) query.set('categoryId', params.categoryId)
    if (params.keyword) query.set('keyword', params.keyword)
    return apiGet(`/api/public/products?${query.toString()}`)
  }
  const keyword = params.keyword?.trim()
  const filtered = mock.products.filter((item) => {
    const matchCategory = !params.categoryId || item.categoryId === params.categoryId
    const matchKeyword = !keyword || item.title.includes(keyword) || item.summary.includes(keyword)
    return matchCategory && matchKeyword
  })
  return delay(paginate(filtered, page, pageSize))
}

export function getProduct(id: string): Promise<ProductDetail | null> {
  if (!USE_MOCK) return apiGet<ProductDetail | null>(`/api/public/products/${id}`)
  return delay(mock.products.find((item) => item.id === id) ?? null)
}

export function getSolutions(params: {
  page?: number
  pageSize?: number
  industry?: string | null
}): Promise<PageResult<SolutionSummary>> {
  const page = params.page ?? 1
  const pageSize = params.pageSize ?? 6
  if (!USE_MOCK) {
    const query = new URLSearchParams({ page: String(page), pageSize: String(pageSize) })
    if (params.industry) query.set('industry', params.industry)
    return apiGet(`/api/public/solutions?${query.toString()}`)
  }
  const filtered = mock.solutions.filter((item) => !params.industry || item.industry === params.industry)
  return delay(paginate(filtered, page, pageSize))
}

export function getSolution(id: string): Promise<SolutionDetail | null> {
  if (!USE_MOCK) return apiGet<SolutionDetail | null>(`/api/public/solutions/${id}`)
  return delay(mock.solutions.find((item) => item.id === id) ?? null)
}

export function getCases(params: {
  page?: number
  pageSize?: number
  industry?: string | null
}): Promise<PageResult<CaseSummary>> {
  const page = params.page ?? 1
  const pageSize = params.pageSize ?? 8
  if (!USE_MOCK) {
    const query = new URLSearchParams({ page: String(page), pageSize: String(pageSize) })
    if (params.industry) query.set('industry', params.industry)
    return apiGet(`/api/public/cases?${query.toString()}`)
  }
  const filtered = mock.cases.filter((item) => !params.industry || item.industry === params.industry)
  return delay(paginate(filtered, page, pageSize))
}

export function getCase(id: string): Promise<CaseDetail | null> {
  if (!USE_MOCK) return apiGet<CaseDetail | null>(`/api/public/cases/${id}`)
  return delay(mock.cases.find((item) => item.id === id) ?? null)
}

export function getNews(params: { page?: number; pageSize?: number }): Promise<PageResult<NewsSummary>> {
  const page = params.page ?? 1
  const pageSize = params.pageSize ?? 8
  if (!USE_MOCK) return apiGet(`/api/public/news?page=${page}&pageSize=${pageSize}`)
  return delay(paginate(mock.news, page, pageSize))
}

export function getNewsDetail(id: string): Promise<NewsDetail | null> {
  if (!USE_MOCK) return apiGet<NewsDetail | null>(`/api/public/news/${id}`)
  return delay(mock.news.find((item) => item.id === id) ?? null)
}

export function getAbout(): Promise<AboutPage> {
  if (!USE_MOCK) return apiGet<AboutPage>('/api/public/pages/ABOUT')
  return delay(mock.about)
}

export function getPrivacy(): Promise<PrivacyPage> {
  if (!USE_MOCK) return apiGet<PrivacyPage>('/api/public/pages/PRIVACY')
  return delay(mock.privacy)
}
