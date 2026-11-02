import { apiGet } from './http'

export interface NavItem {
  label: string
  path: string
  external?: boolean
}

export interface SiteConfigView {
  siteName: string
  logoUrl: string | null
  nav: NavItem[]
  footer: {
    copyright: string
    icp: string | null
    links: NavItem[]
  }
  contact: {
    phone: string | null
    email: string | null
    address: string | null
  }
  formEnabled: boolean
  privacyVersion: number | null
}

/**
 * SITE 未发布时后端返回 404，前端据此显示“站点尚未初始化”，
 * 不用写死内容伪装成已上线。
 */
export function fetchSiteConfig(): Promise<SiteConfigView> {
  return apiGet<SiteConfigView>('/api/public/site-config')
}
