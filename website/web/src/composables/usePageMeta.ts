import { watchEffect } from 'vue'
import { useRoute } from 'vue-router'

/**
 * 每页的 SEO 元信息。正文页需要标题与描述唯一，避免全站同一描述。
 * ORIGIN 上线前替换为真实域名，canonical 与 OG 依赖它。
 */
const SITE_NAME = '示例科技'
const ORIGIN = 'https://www.example.com'

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

export function usePageMeta(getter: () => PageMeta) {
  const route = useRoute()

  watchEffect(() => {
    const meta = getter()
    const fullTitle = meta.title ? `${meta.title}｜${SITE_NAME}` : SITE_NAME
    const url = `${ORIGIN}${route.path}`

    document.title = fullTitle
    upsertMeta('name', 'description', meta.description)
    upsertMeta('name', 'robots', meta.noindex ? 'noindex, nofollow' : 'index, follow')
    upsertLink('canonical', url)

    upsertMeta('property', 'og:type', 'website')
    upsertMeta('property', 'og:site_name', SITE_NAME)
    upsertMeta('property', 'og:title', fullTitle)
    upsertMeta('property', 'og:description', meta.description)
    upsertMeta('property', 'og:url', url)
  })
}
