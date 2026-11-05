import { watchEffect } from 'vue'
import { useRoute } from 'vue-router'
import { useSiteConfigCache } from './consult'

const DEFAULT_SITE_NAME = 'G7易流 · 数字物流与工业物联网平台'

export interface PageMeta {
  title: string
  description: string
  noindex?: boolean
}

function upsertMeta(attr: 'name' | 'property', key: string, content: string) {
  let el = document.head.querySelector<HTMLMetaElement>(`meta[${attr}="${key}"]`)
  if (!el) {
    el = document.createElement('meta')
    el.setAttribute(attr, key)
    document.head.appendChild(el)
  }
  el.setAttribute('content', content)
}

function upsertLink(rel: string, href: string) {
  let el = document.head.querySelector<HTMLLinkElement>(`link[rel="${rel}"]`)
  if (!el) {
    el = document.createElement('link')
    el.setAttribute('rel', rel)
    document.head.appendChild(el)
  }
  el.setAttribute('href', href)
}

export function usePageMeta(source: PageMeta | (() => PageMeta)) {
  const route = useRoute()
  const { siteCache } = useSiteConfigCache()

  watchEffect(() => {
    const meta = typeof source === 'function' ? source() : source
    const siteName = siteCache.value?.siteName || DEFAULT_SITE_NAME
    const fallbackTitle = siteCache.value?.defaultSeo?.title || meta.title || siteName
    const fallbackDesc =
      meta.description ||
      siteCache.value?.defaultSeo?.description ||
      '面向物流与制造企业的 IoT 物联网与 SaaS 数字化平台'

    const fullTitle = meta.title
      ? `${meta.title} | ${siteName}`
      : `${fallbackTitle} | ${siteName}`

    const origin =
      import.meta.env.VITE_SITE_ORIGIN ||
      (typeof window !== 'undefined' ? window.location.origin : 'https://www.example.com')
    const canonical = `${origin}${route.path}`

    document.title = fullTitle
    upsertMeta('name', 'description', fallbackDesc)
    upsertMeta('name', 'robots', meta.noindex ? 'noindex, nofollow' : 'index, follow')
    upsertMeta('property', 'og:title', fullTitle)
    upsertMeta('property', 'og:description', fallbackDesc)
    upsertMeta('property', 'og:url', canonical)
    upsertMeta('property', 'og:site_name', siteName)
    upsertMeta('property', 'og:type', 'website')
    upsertLink('canonical', canonical)

    if (siteCache.value?.faviconUrl) {
      upsertLink('icon', siteCache.value.faviconUrl)
    }
  })
}
