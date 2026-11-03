import { get, request } from './http'

export type LeadStatus = 'NEW' | 'CONTACTED' | 'CLOSED' | 'INVALID'

export interface LeadListItem {
  id: string
  receiptNo: string
  name: string
  companyName: string
  phoneMasked: string
  sourcePosition: string
  sourcePath: string
  contentType: string | null
  contentId: string | null
  contentTitle: string | null
  status: LeadStatus
  handledByName: string | null
  createdAt: string
}

export interface LeadDetail extends LeadListItem {
  phone: string
  demand: string
  consentVersion: number
  consentAt: string
  lastNote: string | null
  handledAt: string | null
  lockVersion: number
  updatedAt: string
}

export function listLeads(params: {
  status?: string
  keyword?: string
  from?: string
  to?: string
  page: number
  pageSize: number
}) {
  const query = new URLSearchParams({
    page: String(params.page),
    pageSize: String(params.pageSize),
  })
  if (params.status) query.set('status', params.status)
  if (params.keyword) query.set('keyword', params.keyword)
  if (params.from) query.set('from', params.from)
  if (params.to) query.set('to', params.to)
  return get<{ items: LeadListItem[]; page: number; pageSize: number; total: number }>(
    `/api/admin/leads?${query.toString()}`,
  )
}

export function getLead(id: string) {
  return get<LeadDetail>(`/api/admin/leads/${id}`)
}

export function updateLeadStatus(id: string, payload: {
  status: LeadStatus
  note: string
  expectedLockVersion: number
}) {
  return request<void>(`/api/admin/leads/${id}/status`, {
    method: 'POST',
    body: payload,
  })
}
