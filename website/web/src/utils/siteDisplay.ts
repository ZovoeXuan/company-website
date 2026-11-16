import type { ContactEmailItem } from '@/data/types'

/**
 * 识别示例占位电话或邮箱（避免将演示占位号渲染为真实可拨打热线）
 */
export function isPlaceholderContact(value?: string | null): boolean {
  if (!value) return true
  const raw = value.trim()
  if (!raw) return true
  const normalized = raw.toLowerCase()
  if (normalized.endsWith('@example.com') || normalized.includes('example.')) return true
  const digits = raw.replace(/[^\d]/g, '')
  if (digits === '4000000000' || digits === '01000000000' || digits === '13800000000') return true
  return false
}

export function resolvePublicPhone(
  contactPhone?: string | null,
  fallbackPhone?: string | null,
): string {
  if (!isPlaceholderContact(contactPhone)) return contactPhone!.trim()
  if (!isPlaceholderContact(fallbackPhone)) return fallbackPhone!.trim()
  return ''
}

export function resolvePublicContactEmails(
  emails?: ContactEmailItem[] | null,
  fallbackEmail?: string | null,
): ContactEmailItem[] {
  const valid = (emails ?? []).filter(
    (item) => item && item.email && !isPlaceholderContact(item.email),
  )
  if (valid.length > 0) return valid
  if (!isPlaceholderContact(fallbackEmail)) {
    return [{ email: fallbackEmail!.trim(), desc: '商务合作' }]
  }
  return []
}
