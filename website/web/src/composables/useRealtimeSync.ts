import { onBeforeUnmount, onMounted } from 'vue'
import { getPublicVersion } from '@/api/public'
import type { PublicVersionInfo } from '@/data/types'

export type VersionKey = 'siteConfigVersion' | 'homeVersion' | 'aboutVersion' | 'contentVersion'

interface Subscriber {
  keys: VersionKey[]
  onChanged: (changedKeys: VersionKey[], force: boolean) => void
}

const subscribers = new Set<Subscriber>()
let latestVersions: PublicVersionInfo | null = null
let latestEtag: string | null = null
let pollTimer: number | null = null
let backoffDelayMs = 30_000
let inFlightCheck: Promise<void> | null = null
let globalListenersAttached = false

const BASE_POLL_MS = 30_000
const BACKOFF_STEPS_MS = [30_000, 60_000, 120_000]
const SYNC_STORAGE_KEYS = new Set(['site_last_published', 'g7_last_published', 'admin_data_updated'])

function clearPollTimer() {
  if (pollTimer !== null) {
    window.clearTimeout(pollTimer)
    pollTimer = null
  }
}

function scheduleNextPoll() {
  clearPollTimer()
  if (subscribers.size === 0 || document.hidden) return
  pollTimer = window.setTimeout(() => {
    void checkNow()
  }, backoffDelayMs)
}

async function checkNow(forceAll = false): Promise<void> {
  if (subscribers.size === 0) return
  if (inFlightCheck) return inFlightCheck

  inFlightCheck = (async () => {
    try {
      const res = await getPublicVersion(forceAll ? null : latestEtag)
      latestEtag = res.etag
      backoffDelayMs = BASE_POLL_MS

      if (res.status === 304) {
        if (forceAll) {
          for (const sub of subscribers) {
            sub.onChanged([], true)
          }
        }
        return
      }

      const next = res.data
      if (!latestVersions) {
        latestVersions = next
        if (forceAll) {
          for (const sub of subscribers) {
            sub.onChanged([...sub.keys], true)
          }
        }
        return
      }

      const changed: VersionKey[] = []
      const allKeys: VersionKey[] = ['siteConfigVersion', 'homeVersion', 'aboutVersion', 'contentVersion']
      for (const k of allKeys) {
        if (next[k] !== latestVersions[k]) {
          changed.push(k)
        }
      }
      latestVersions = next

      if (changed.length > 0 || forceAll) {
        for (const sub of subscribers) {
          const matched = sub.keys.filter((k) => changed.includes(k))
          if (matched.length > 0 || forceAll) {
            sub.onChanged(matched, forceAll)
          }
        }
      }
    } catch {
      const idx = BACKOFF_STEPS_MS.indexOf(backoffDelayMs)
      backoffDelayMs =
        idx >= 0 && idx < BACKOFF_STEPS_MS.length - 1 ? BACKOFF_STEPS_MS[idx + 1] : BACKOFF_STEPS_MS[BACKOFF_STEPS_MS.length - 1]
    } finally {
      inFlightCheck = null
      scheduleNextPoll()
    }
  })()

  return inFlightCheck
}

function onVisibilityChange() {
  if (document.hidden) {
    clearPollTimer()
  } else {
    void checkNow()
  }
}

function onWindowFocus() {
  if (!document.hidden) {
    void checkNow()
  }
}

function onStorageSync(e: StorageEvent) {
  if (e.key && SYNC_STORAGE_KEYS.has(e.key)) {
    void checkNow(true)
  }
}

function attachGlobalListeners() {
  if (globalListenersAttached) return
  globalListenersAttached = true
  document.addEventListener('visibilitychange', onVisibilityChange)
  window.addEventListener('focus', onWindowFocus)
  window.addEventListener('storage', onStorageSync)
  scheduleNextPoll()
}

function detachGlobalListenersIfEmpty() {
  if (subscribers.size > 0 || !globalListenersAttached) return
  globalListenersAttached = false
  clearPollTimer()
  document.removeEventListener('visibilitychange', onVisibilityChange)
  window.removeEventListener('focus', onWindowFocus)
  window.removeEventListener('storage', onStorageSync)
}

/**
 * 订阅官网发布版本变化：
 * - 基于 `/api/public/version`（ETag/304 + 30/60/120s 退避轮询）
 * - 页面隐藏暂停、切回与聚焦立即补检、兼听同源 storage 事件
 * - 组件卸载时自动清理全部订阅与全局监听器
 */
export function useRealtimeSync(
  keys: VersionKey[],
  onChanged: (changedKeys: VersionKey[], force: boolean) => void,
) {
  const subscriber: Subscriber = { keys, onChanged }

  onMounted(() => {
    subscribers.add(subscriber)
    attachGlobalListeners()
    if (!latestVersions && !document.hidden) {
      void checkNow()
    }
  })

  onBeforeUnmount(() => {
    subscribers.delete(subscriber)
    detachGlobalListenersIfEmpty()
  })
}
