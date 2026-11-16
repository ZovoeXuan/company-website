import { reactive } from 'vue'
import { getContactPage, getHome, getSite } from '@/api/public'
import type { ContentType, LeadSourcePosition } from '@/data/types'

export interface ConsultOptions {
  sourcePosition?: LeadSourcePosition
  contentType?: ContentType
  contentId?: string | number
  title?: string
  initialDemand?: string
}

interface ConsultDialogState {
  open: boolean
  sourcePosition: LeadSourcePosition
  contentType?: ContentType
  contentId?: string
  title?: string
  initialDemand?: string
}

interface ConsultContextState {
  loaded: boolean
  loadError: boolean
  formEnabled: boolean
  smsVerifyEnabled: boolean
  privacyVersion: number
  formTitle: string
  successText: string
}

const dialog = reactive<ConsultDialogState>({
  open: false,
  sourcePosition: 'FLOATING',
})

const context = reactive<ConsultContextState>({
  loaded: false,
  loadError: false,
  formEnabled: false,
  smsVerifyEnabled: true,
  privacyVersion: 1,
  formTitle: '预约产品演示与方案咨询',
  successText: '感谢您的留言，我们的顾问将在 1 个工作日内与您联系。',
})

let loadingPromise: Promise<void> | null = null

export function openConsult(options: ConsultOptions = {}) {
  dialog.sourcePosition = options.sourcePosition ?? 'FLOATING'
  dialog.contentType = options.contentType
  dialog.contentId = options.contentId != null ? String(options.contentId) : undefined
  dialog.title = options.title
  dialog.initialDemand = options.initialDemand
  dialog.open = true
}

export function closeConsult() {
  dialog.open = false
}

/**
 * 加载留资表单上下文：采用 Fail-Closed 策略。
 * 若关键配置接口全部不可用，保持 formEnabled = false，避免绕过隐私协议版本与短信核验契约。
 */
export async function loadConsultContext(): Promise<ConsultContextState> {
  if (context.loaded && !context.loadError) return context
  if (!loadingPromise) {
    loadingPromise = Promise.all([
      getSite().catch(() => null),
      getHome().catch(() => null),
      getContactPage().catch(() => null),
    ])
      .then(([site, home, contact]) => {
        if (!site && !home && !contact) {
          context.loadError = true
          context.formEnabled = false
          return
        }
        context.loadError = false
        context.formEnabled = contact?.formEnabled ?? site?.formEnabled ?? home?.formEnabled ?? false
        context.smsVerifyEnabled =
          contact?.smsVerifyEnabled ?? site?.contact?.smsVerifyEnabled ?? true
        context.privacyVersion = site?.privacyVersion ?? 1
        context.formTitle =
          contact?.formTitle || site?.contact?.formTitle || '预约产品演示与方案咨询'
        context.successText =
          contact?.successText ||
          site?.contact?.successText ||
          context.successText
        context.loaded = true
      })
      .finally(() => {
        loadingPromise = null
      })
  }
  await loadingPromise
  return context
}

export function useConsultDialog() {
  return { dialog, context, openConsult, closeConsult, loadConsultContext }
}
