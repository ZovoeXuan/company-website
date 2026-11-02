import { get, request } from './http'

export type ConfigKey = 'SITE' | 'NAVIGATION' | 'HOME' | 'ABOUT' | 'CONTACT' | 'PRIVACY'

export interface ConfigView<T = Record<string, unknown>> {
  key: ConfigKey
  draft: T
  published: T | null
  status: 'UNPUBLISHED' | 'PUBLISHED'
  draftVersion: number
  publishedVersion: number | null
  lockVersion: number
  hasUnpublishedChanges: boolean
  publishedAt: string | null
}

export function getConfig<T>(key: ConfigKey) {
  return get<ConfigView<T>>(`/api/admin/configs/${key}`)
}

export function saveConfigDraft<T>(key: ConfigKey, draft: T, expectedLockVersion: number) {
  return request<ConfigView<T>>(`/api/admin/configs/${key}/draft`, {
    method: 'PUT',
    body: { expectedLockVersion, draft },
  })
}

export function publishConfig(key: ConfigKey, expectedLockVersion: number, expectedDraftVersion: number) {
  return request<void>(`/api/admin/configs/${key}/publish`, {
    method: 'POST',
    body: { expectedLockVersion, expectedDraftVersion },
  })
}
