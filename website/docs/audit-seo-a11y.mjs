import fs from 'node:fs'

/**
 * SEO 与无障碍静态审计：逐路由检查标题、描述、H1 数量、图片 alt、可访问名称、重复 id。
 * 用法：node audit-seo-a11y.mjs
 */
const BASE = 'http://127.0.0.1:9222'
const ORIGIN = process.env.AUDIT_ORIGIN || 'http://127.0.0.1:5173'

const routes = [
  '/', '/products', '/products/p-1', '/solutions', '/solutions/s-1',
  '/cases', '/cases/c-1', '/news', '/news/n-1', '/about', '/contact', '/privacy', '/no-such-page',
]

const target = await (await fetch(`${BASE}/json/new?${encodeURIComponent('about:blank')}`, { method: 'PUT' })).json()
const ws = new WebSocket(target.webSocketDebuggerUrl)

let seq = 0
const pending = new Map()
ws.addEventListener('message', (event) => {
  const msg = JSON.parse(event.data)
  if (msg.id && pending.has(msg.id)) {
    pending.get(msg.id)(msg)
    pending.delete(msg.id)
  }
})
await new Promise((resolve) => ws.addEventListener('open', resolve))

const send = (method, params = {}) => new Promise((resolve) => {
  const id = ++seq
  pending.set(id, resolve)
  ws.send(JSON.stringify({ id, method, params }))
})
const sleep = (ms) => new Promise((r) => setTimeout(r, ms))
const evaluate = async (expression) => {
  const res = await send('Runtime.evaluate', { expression, awaitPromise: true, returnByValue: true })
  return res.result?.result?.value
}

await send('Page.enable')
await send('Emulation.setDeviceMetricsOverride', { width: 1440, height: 900, deviceScaleFactor: 1, mobile: false })

const results = []

for (const route of routes) {
  await send('Page.navigate', { url: `${ORIGIN}${route}` })
  await sleep(1200)

  const info = await evaluate(`(() => {
    const q = (sel) => Array.from(document.querySelectorAll(sel))
    const desc = document.querySelector('meta[name="description"]')
    const canonical = document.querySelector('link[rel="canonical"]')
    const robots = document.querySelector('meta[name="robots"]')
    const ids = q('[id]').map((el) => el.id).filter(Boolean)
    const dupIds = ids.filter((id, i) => ids.indexOf(id) !== i)
    const imgsNoAlt = q('img').filter((img) => !img.hasAttribute('alt') || img.getAttribute('alt').trim() === '').length
    const linksNoName = q('a').filter((a) => (a.textContent || '').trim() === '' && !a.getAttribute('aria-label') && !a.querySelector('img[alt]')).length
    const inputsNoLabel = q('input, textarea, select').filter((el) => {
      if (el.type === 'hidden' || el.type === 'submit') return false
      if (el.getAttribute('aria-label')) return false
      if (el.id && document.querySelector('label[for="' + el.id + '"]')) return false
      return !el.closest('label')
    }).length
    const h1s = q('h1').map((h) => (h.textContent || '').trim())
    return {
      title: document.title,
      titleLen: document.title.length,
      descLen: desc ? desc.content.length : 0,
      hasCanonical: Boolean(canonical),
      robots: robots ? robots.content : null,
      h1Count: h1s.length,
      h1: h1s[0] || '',
      imgs: q('img').length,
      imgsNoAlt,
      linksNoName,
      inputsNoLabel,
      dupIds: Array.from(new Set(dupIds)),
    }
  })()`)

  results.push({ route, ...info })
}

console.log(JSON.stringify(results, null, 2))
fs.writeFileSync(new URL('./audit-report.json', import.meta.url), JSON.stringify(results, null, 2))
ws.close()
