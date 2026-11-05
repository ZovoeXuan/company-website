import { reactive, ref } from 'vue'
import { getContactPage, getSite } from '@/api/public'
import type { ContactPage, SiteConfig } from '@/data/types'

export interface ConsultContext {
  site: SiteConfig | null
  page: ContactPage | null
  formEnabled: boolean
  privacyVersion: number | null
  successText: string
}

export interface ConsultTarget {
  sourcePosition: string
  contentType?: string
  contentId?: string
  presetQuestion?: string
}

const siteCache = ref<SiteConfig | null>(null)
let pending: Promise<ConsultContext> | null = null

export function loadConsultContext(forceRefresh = false): Promise<ConsultContext> {
  if (!pending || forceRefresh) {
    pending = Promise.all([
      getSite().catch(() => null),
      getContactPage().catch(() => null),
    ]).then(([site, page]) => {
      if (site) siteCache.value = site
      return {
        site,
        page,
        formEnabled: Boolean(site?.formEnabled ?? page?.formEnabled),
        privacyVersion: site?.privacyVersion ?? null,
        successText: page?.successText || '已收到您的咨询，我们的专家顾问将在 1 个工作日内与您联系。',
      }
    })
  }
  return pending
}

export function useSiteConfigCache() {
  return { siteCache, loadConsultContext }
}

const dialog = reactive<{
  open: boolean
  target: ConsultTarget
}>({
  open: false,
  target: { sourcePosition: 'OTHER_CTA' },
})

export function openConsult(target: ConsultTarget) {
  dialog.target = { ...target }
  dialog.open = true
}

export function closeConsult() {
  dialog.open = false
}

export function useConsultDialog() {
  return { dialog, openConsult, closeConsult }
}
