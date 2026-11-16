import { onBeforeUnmount, ref } from 'vue'
import { getSite } from '@/api/public'
import type { PageHeroConfig, SiteConfig } from '@/data/types'

let cachedSitePromise: Promise<SiteConfig> | null = null
let inFlightForcePromise: Promise<SiteConfig> | null = null
let lastRefreshAt = 0
const siteListeners = new Set<(site: SiteConfig | null) => void>()

export async function getCachedSiteStrict(forceRefresh = false): Promise<SiteConfig> {
  if (forceRefresh) {
    if (inFlightForcePromise) return inFlightForcePromise
    if (Date.now() - lastRefreshAt < 1000 && cachedSitePromise) {
      return cachedSitePromise
    }
    const req = getSite()
      .then((site) => {
        lastRefreshAt = Date.now()
        for (const listener of siteListeners) {
          listener(site)
        }
        return site
      })
      .catch((err) => {
        cachedSitePromise = null
        throw err
      })
      .finally(() => {
        inFlightForcePromise = null
      })
    cachedSitePromise = req
    inFlightForcePromise = req
    return req
  }

  if (!cachedSitePromise) {
    cachedSitePromise = getSite().catch((err) => {
      cachedSitePromise = null
      throw err
    })
  }
  return cachedSitePromise
}

export function getCachedSite(forceRefresh = false): Promise<SiteConfig | null> {
  return getCachedSiteStrict(forceRefresh).catch(() => null)
}

export async function refreshCachedSite(): Promise<SiteConfig | null> {
  return getCachedSite(true)
}

export function usePageHero(pageKey: 'news' | 'cases' | 'products' | 'solutions') {
  const hero = ref<PageHeroConfig | null>(null)

  const applySite = (site: SiteConfig | null) => {
    hero.value = site?.pageHeroes?.[pageKey] ?? null
  }

  siteListeners.add(applySite)
  void getCachedSite().then(applySite)

  onBeforeUnmount(() => {
    siteListeners.delete(applySite)
  })

  return {
    hero,
    refreshHero: async () => {
      const site = await refreshCachedSite()
      applySite(site)
    },
  }
}
