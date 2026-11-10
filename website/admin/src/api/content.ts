import { get, request } from './http'

export type PublicationStatus = 'UNPUBLISHED' | 'PUBLISHED' | 'OFFLINE'

export interface ContentView {
  id: string
  contentType: string
  title: string
  status: PublicationStatus
  draftVersion: number
  publishedVersion: number | null
  lockVersion: number
  hasUnpublishedChanges: boolean
  categoryId: string | null
  categoryName: string | null
  sortOrder: number
  displayTime: string | null
  publishedAt: string | null
  coverMediaId: string | null
  updatedAt: string | null
}

export interface ContentDetail {
  content: ContentView
  draft: Record<string, unknown>
  published: Record<string, unknown> | null
}

export interface CategoryView {
  id: string
  categoryType: string
  name: string
  publishedName: string | null
  status: PublicationStatus
  draftVersion: number
  publishedVersion: number | null
  lockVersion: number
  sortOrder: number
  publishedSortOrder: number | null
  hasUnpublishedChanges: boolean
}

export interface MediaItem {
  id: string
  originalName: string
  mimeType: string
  mediaType: 'IMAGE' | 'VIDEO'
  width: number
  height: number
  durationMs: number | null
  byteSize: number
  url: string
  createdAt: string | null
  referenced: boolean
}

export interface MediaPage {
  items: MediaItem[]
  page: number
  pageSize: number
  total: number
}

// ---- 内容 ----

export function listContents(params: {
  contentType: string
  keyword?: string
  categoryId?: string
  publicationStatus?: string
  hasUnpublishedChanges?: boolean
  page: number
  pageSize: number
}) {
  const query = new URLSearchParams({
    contentType: params.contentType,
    page: String(params.page),
    pageSize: String(params.pageSize),
  })
  if (params.keyword) query.set('keyword', params.keyword)
  if (params.categoryId) query.set('categoryId', params.categoryId)
  if (params.publicationStatus) query.set('publicationStatus', params.publicationStatus)
  if (params.hasUnpublishedChanges) query.set('hasUnpublishedChanges', 'true')
  return get<{ items: ContentView[]; total: number }>(`/api/admin/contents?${query.toString()}`)
}

export function getContent(id: string) {
  return get<ContentDetail>(`/api/admin/contents/${id}`)
}

export function createContent(contentType: string, draft: Record<string, unknown>) {
  return request<ContentDetail>('/api/admin/contents', {
    method: 'POST',
    body: { contentType, draft },
  })
}

export function saveDraft(id: string, draft: Record<string, unknown>, expectedLockVersion: number) {
  return request<ContentDetail>(`/api/admin/contents/${id}/draft`, {
    method: 'PUT',
    body: { expectedLockVersion, draft },
  })
}

export function publishContent(id: string, expectedLockVersion: number, expectedDraftVersion: number) {
  return request<void>(`/api/admin/contents/${id}/publish`, {
    method: 'POST',
    body: { expectedLockVersion, expectedDraftVersion },
  })
}

export function offlineContent(id: string, expectedLockVersion: number, expectedDraftVersion = 1) {
  return request<void>(`/api/admin/contents/${id}/offline`, {
    method: 'POST',
    body: { expectedLockVersion, expectedDraftVersion },
  })
}

export function deleteContent(id: string, expectedLockVersion: number) {
  return request<void>(`/api/admin/contents/${id}?expectedLockVersion=${expectedLockVersion}`, {
    method: 'DELETE',
  })
}

/** 一键克隆内容为新草稿副本 */
export async function cloneContent(id: string) {
  const detail = await getContent(id)
  const clonedDraft: Record<string, unknown> = {
    ...detail.draft,
    title: `${String(detail.draft.title || detail.content.title || '未命名')} (副本)`,
  }
  return createContent(detail.content.contentType, clonedDraft)
}

// ---- 分类 ----

export interface CategoryPage {
  items: CategoryView[]
  page: number
  pageSize: number
  total: number
}

export function listCategories(params?: string | { categoryType?: string; keyword?: string; page?: number; pageSize?: number }) {
  if (typeof params === 'string') {
    return get<CategoryPage>(`/api/admin/categories?categoryType=${encodeURIComponent(params)}&pageSize=50`)
  }
  const query = new URLSearchParams()
  if (params?.categoryType) query.set('categoryType', params.categoryType)
  if (params?.keyword) query.set('keyword', params.keyword)
  if (params?.page) query.set('page', String(params.page))
  if (params?.pageSize) query.set('pageSize', String(params.pageSize))
  return get<CategoryPage>(`/api/admin/categories?${query.toString()}`)
}

export function createCategory(
  arg1: string | { categoryType: string; name: string; sortOrder?: number },
  name?: string,
  sortOrder = 100,
) {
  const body =
    typeof arg1 === 'string'
      ? { categoryType: arg1, name: name ?? '', sortOrder }
      : arg1
  return request<string>('/api/admin/categories', {
    method: 'POST',
    body,
  })
}

export function updateCategory(id: string, body: { expectedLockVersion: number; name: string; sortOrder?: number }) {
  return request<void>(`/api/admin/categories/${id}/draft`, {
    method: 'PUT',
    body,
  })
}

export function saveCategory(id: string, name: string, sortOrder: number, expectedLockVersion: number) {
  return updateCategory(id, { expectedLockVersion, name, sortOrder })
}

export function publishCategory(id: string, expectedLockVersion: number, expectedDraftVersion: number) {
  return request<void>(`/api/admin/categories/${id}/publish`, {
    method: 'POST',
    body: { expectedLockVersion, expectedDraftVersion },
  })
}

export function offlineCategory(id: string, expectedLockVersion: number, expectedDraftVersion = 1) {
  return request<void>(`/api/admin/categories/${id}/offline`, {
    method: 'POST',
    body: { expectedLockVersion, expectedDraftVersion },
  })
}

export function deleteCategory(id: string, expectedLockVersion: number) {
  return request<void>(`/api/admin/categories/${id}?expectedLockVersion=${expectedLockVersion}`, {
    method: 'DELETE',
  })
}

// ---- 素材 ----

export function listMedia(params: {
  keyword?: string
  type?: '' | 'IMAGE' | 'VIDEO'
  referenced?: boolean
  page: number
  pageSize: number
}) {
  const query = new URLSearchParams({
    page: String(params.page),
    pageSize: String(params.pageSize),
  })
  if (params.keyword) query.set('keyword', params.keyword)
  if (params.type) query.set('type', params.type)
  if (params.referenced !== undefined) query.set('referenced', String(params.referenced))
  return get<MediaPage>(`/api/admin/media?${query.toString()}`)
}

export function getMedia(id: string | number) {
  return get<MediaItem>(`/api/admin/media/${id}`)
}

export function deleteMedia(id: string | number) {
  return request<void>(`/api/admin/media/${id}`, { method: 'DELETE' })
}

export function mediaReferences(id: string | number) {
  return get<Array<Record<string, string>>>(`/api/admin/media/${id}/references`)
}
