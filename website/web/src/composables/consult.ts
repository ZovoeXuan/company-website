import { reactive, ref } from 'vue'
import { getContactPage, getSite } from '@/api/public'
import type { SiteConfig } from '@/data/types'

const siteCache = ref<SiteConfig | null>(null)
export function useSiteConfigCache() {
  return { siteCache, loadConsultContext }
}


export interface ConsultOpenOptions {
  sourcePosition: string
  contentType?: 'PRODUCT' | 'SOLUTION' | 'CASE'
  contentId?: string | number
  title?: string
  initialDemand?: string
}

const dialog = reactive({
  open: false,
  sourcePosition: 'NAVIGATION',
  contentType: undefined as 'PRODUCT' | 'SOLUTION' | 'CASE' | undefined,
  contentId: undefined as number | undefined,
  title: '',
  initialDemand: '',
})

/** 小七 AI 助手右侧抽屉全局状态 */
const xiaoqi = reactive({
  open: false,
  initialQuestion: '',
})

export function openConsult(options: ConsultOpenOptions) {
  dialog.sourcePosition = options.sourcePosition
  dialog.contentType = options.contentType
  dialog.contentId = options.contentId != null ? Number(options.contentId) : undefined
  dialog.title = options.title ?? ''
  dialog.initialDemand = options.initialDemand ?? ''
  dialog.open = true
}

export function closeConsult() {
  dialog.open = false
}

export function useConsultDialog() {
  return { dialog, openConsult, closeConsult }
}

export function openXiaoQi(question = '') {
  xiaoqi.initialQuestion = question
  xiaoqi.open = true
}

export function closeXiaoQi() {
  xiaoqi.open = false
}

export function useXiaoQiDrawer() {
  return { xiaoqi, openXiaoQi, closeXiaoQi }
}

export interface ConsultContext {
  formEnabled: boolean
  privacyVersion: number | null
  successText: string
  phone: string
  email: string
}

/** 打开表单时实时拉取当前发布态，不把 formEnabled 缓存在 localStorage。 */
export async function loadConsultContext(): Promise<ConsultContext> {
  const [site, contact] = await Promise.all([
    getSite(),
    getContactPage().catch(() => null),
  ])
  if (site) {
    siteCache.value = site as any
  }
  return {
    formEnabled: Boolean(site.formEnabled ?? contact?.formEnabled ?? true),
    privacyVersion: site.privacyVersion ?? 1,
    successText: contact?.successText || '预约成功！我们的产品专家将在 1 个工作日内与您联系安排产品演示。',
    phone: contact?.phone || site.contact?.phone || '',
    email: contact?.email || site.contact?.email || '',
  }
}
