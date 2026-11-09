/**
 * 公开内容数据源。全部走后端 `/api/public/*`，不再保留示例数据分支：
 * 页面区分 loading / 空结果 / 请求失败，不能把失败静默当空数组。
 */
import { apiGet, apiPost } from './http'
import type {
  AboutPage,
  CaseDetail,
  CaseSummary,
  ContactPage,
  HomeView,
  LeadSubmitPayload,
  LeadSubmitResult,
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

export function getSite(): Promise<SiteConfig> {
  return apiGet<SiteConfig>('/api/public/site-config')
}

export function getHome(): Promise<HomeView> {
  return apiGet<HomeView>('/api/public/home')
}

export function getProductCategories(): Promise<Array<{ id: string; name: string }>> {
  return apiGet('/api/public/categories?type=PRODUCT')
}

export function getProducts(params: {
  page?: number
  pageSize?: number
  categoryId?: string | null
  keyword?: string
}): Promise<PageResult<ProductSummary>> {
  const query = new URLSearchParams({
    page: String(params.page ?? 1),
    pageSize: String(params.pageSize ?? 9),
  })
  if (params.categoryId) query.set('categoryId', params.categoryId)
  if (params.keyword) query.set('keyword', params.keyword)
  return apiGet(`/api/public/products?${query.toString()}`)
}

export function getProduct(id: string): Promise<ProductDetail> {
  return apiGet<ProductDetail>(`/api/public/products/${id}`)
}

export function getSolutionCategories(): Promise<Array<{ id: string; name: string }>> {
  return apiGet('/api/public/categories?type=SOLUTION')
}

export function getSolutions(params: {
  page?: number
  pageSize?: number
  categoryId?: string | null
  keyword?: string
}): Promise<PageResult<SolutionSummary>> {
  const query = new URLSearchParams({
    page: String(params.page ?? 1),
    pageSize: String(params.pageSize ?? 9),
  })
  if (params.categoryId) query.set('categoryId', params.categoryId)
  if (params.keyword) query.set('keyword', params.keyword)
  return apiGet(`/api/public/solutions?${query.toString()}`)
}

export function getSolution(id: string): Promise<SolutionDetail> {
  return apiGet<SolutionDetail>(`/api/public/solutions/${id}`)
}

export function getCaseCategories(): Promise<Array<{ id: string; name: string }>> {
  return apiGet('/api/public/categories?type=CASE')
}

export function getCases(params: {
  page?: number
  pageSize?: number
  categoryId?: string | null
  keyword?: string
}): Promise<PageResult<CaseSummary>> {
  const query = new URLSearchParams({
    page: String(params.page ?? 1),
    pageSize: String(params.pageSize ?? 9),
  })
  if (params.categoryId) query.set('categoryId', params.categoryId)
  if (params.keyword) query.set('keyword', params.keyword)
  return apiGet(`/api/public/cases?${query.toString()}`)
}

export function getCase(id: string): Promise<CaseDetail> {
  return apiGet<CaseDetail>(`/api/public/cases/${id}`)
}

export function getNewsCategories(): Promise<Array<{ id: string; name: string }>> {
  return apiGet('/api/public/categories?type=NEWS')
}

export function getNews(params: {
  page?: number
  pageSize?: number
  categoryId?: string | null
  keyword?: string
}): Promise<PageResult<NewsSummary>> {
  const query = new URLSearchParams({
    page: String(params.page ?? 1),
    pageSize: String(params.pageSize ?? 9),
  })
  if (params.categoryId) query.set('categoryId', params.categoryId)
  if (params.keyword) query.set('keyword', params.keyword)
  return apiGet(`/api/public/news?${query.toString()}`)
}

export function getNewsDetail(id: string): Promise<NewsDetail> {
  return apiGet<NewsDetail>(`/api/public/news/${id}`)
}

export function getAbout(): Promise<AboutPage> {
  return getPage<AboutPage>('ABOUT')
}

export function getPrivacy(): Promise<PrivacyPage> {
  return getPage<PrivacyPage>('PRIVACY')
}

export function getContactPage(): Promise<ContactPage> {
  return getPage<ContactPage>('CONTACT')
}

/** 咨询提交。每次独立咨询使用一个 UUID 幂等键，失败重试沿用同一个键。 */
export function submitLead(
  payload: LeadSubmitPayload,
  idempotencyKey: string,
): Promise<LeadSubmitResult> {
  return apiPost<LeadSubmitResult>('/api/public/leads', payload, {
    'Idempotency-Key': idempotencyKey,
  })
}

/** 单页接口返回 {code, content}，这里统一拆开。 */
/** 小鑫问答：服务端代理调用后台配置的大模型，密钥不经过浏览器。 */
export function askXiaoqi(
  question: string,
  history: Array<{ role: 'user' | 'assistant'; content: string }> = [],
): Promise<{ answer: string }> {
  return apiPost<{ answer: string }>('/api/public/xiaoqi/chat', { question, history })
}

async function getPage<T>(code: string): Promise<T> {
  const response = await apiGet<{ code: string; content: T }>(`/api/public/pages/${code}`)
  return response.content
}
