import type { ContactEmailItem } from '@/data/types'

const PLACEHOLDER_EMAIL_PATTERN = /@(g7\.com\.cn|example\.com)$/i
const PLACEHOLDER_PHONE_PATTERN = /^(010-?0{4}[\s-]?0{4}|400-?0{3}[\s-]?0{4}|0{7,})$/

export function isPlaceholderContact(value?: string | null): boolean {
  if (!value) return true
  const trimmed = value.trim()
  if (!trimmed) return true
  return PLACEHOLDER_EMAIL_PATTERN.test(trimmed) || PLACEHOLDER_PHONE_PATTERN.test(trimmed)
}

export function isPlaceholderIcp(value?: string | null): boolean {
  if (!value) return true
  const trimmed = value.trim()
  return !trimmed || trimmed.includes('08004136') || trimmed.includes('00000000')
}

export function resolvePublicContactEmails(
  configured?: ContactEmailItem[] | null,
  fallbackPrimaryEmail?: string | null,
): ContactEmailItem[] {
  if (Array.isArray(configured) && configured.length > 0) {
    const valid = configured.filter((item) => !isPlaceholderContact(item?.email))
    if (valid.length > 0) return valid
  }
  if (!isPlaceholderContact(fallbackPrimaryEmail)) {
    return [{ email: fallbackPrimaryEmail!.trim(), desc: '商务与媒体联系' }]
  }
  return []
}
