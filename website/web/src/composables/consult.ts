import { reactive } from 'vue'
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
}

let pending: Promise<ConsultContext> | null = null

/** 站点配置与联系页只取一次，避免每个详情页重复请求。 */
export function loadConsultContext(): Promise<ConsultContext> {
  if (!pending) {
    pending = Promise.all([
      getSite().catch(() => null),
      getContactPage().catch(() => null),
    ]).then(([site, page]) => {
      const formEnabled = Boolean(site?.formEnabled ?? page?.formEnabled)
      return {
        site,
        page,
        formEnabled,
        privacyVersion: site?.privacyVersion ?? null,
        successText: page?.successText || '已收到你的咨询，我们会尽快联系你。',
      }
    })
  }
  return pending
}

const dialog = reactive<{ open: boolean; target: ConsultTarget }>({
  open: false,
  target: { sourcePosition: 'OTHER_CTA' },
})

export function openConsult(target: ConsultTarget) {
  dialog.target = target
  dialog.open = true
}

export function closeConsult() {
  dialog.open = false
}

export function useConsultDialog() {
  return { dialog, openConsult, closeConsult }
}
