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

export function offlineContent(id: string, expectedLockVersion: number) {
  return request<void>(`/api/admin/contents/${id}/offline`, {
    method: 'POST',
    body: { expectedLockVersion, expectedDraftVersion: 1 },
  })
}

export function deleteContent(id: string, expectedLockVersion: number) {
  return request<void>(`/api/admin/contents/${id}?expectedLockVersion=${expectedLockVersion}`, {
    method: 'DELETE',
  })
}

export function contentReferences(id: string) {
  return get<Array<Record<string, string>>>(`/api/admin/contents/${id}/references`)
}

// ---- 分类 ----

export function listCategories(params: { categoryType: string; keyword?: string; page: number; pageSize: number }) {
  const query = new URLSearchParams({
    categoryType: params.categoryType,
    page: String(params.page),
    pageSize: String(params.pageSize),
  })
  if (params.keyword) query.set('keyword', params.keyword)
  return get<{ items: CategoryView[]; total: number }>(`/api/admin/categories?${query.toString()}`)
}

export function createCategory(payload: { categoryType: string; name: string; sortOrder: number }) {
  return request<string>('/api/admin/categories', { method: 'POST', body: payload })
}

export function updateCategory(id: string, payload: { expectedLockVersion: number; name: string; sortOrder: number }) {
  return request<void>(`/api/admin/categories/${id}/draft`, { method: 'PUT', body: payload })
}

export function publishCategory(id: string, expectedLockVersion: number, expectedDraftVersion: number) {
  return request<void>(`/api/admin/categories/${id}/publish`, {
    method: 'POST',
    body: { expectedLockVersion, expectedDraftVersion },
  })
}

export function offlineCategory(id: string, expectedLockVersion: number) {
  return request<void>(`/api/admin/categories/${id}/offline`, {
    method: 'POST',
    body: { expectedLockVersion, expectedDraftVersion: 1 },
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
  type?: 'IMAGE' | 'VIDEO'
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
  if (params.referenced) query.set('referenced', 'true')
  return get<MediaPage>(`/api/admin/media?${query.toString()}`)
}

export function getMedia(id: string) {
  return get<MediaItem>(`/api/admin/media/${id}`)
}

export function deleteMedia(id: string) {
  return request<void>(`/api/admin/media/${id}`, { method: 'DELETE' })
}

export function mediaReferences(id: string) {
  return get<Array<Record<string, string>>>(`/api/admin/media/${id}/references`)
}
