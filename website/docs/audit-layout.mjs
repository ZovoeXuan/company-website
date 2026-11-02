import fs from 'node:fs'

/**
 * 布局审计：在指定视口下找出横向溢出元素，并输出页面尺寸。
 * 用法：node audit-layout.mjs <url> [width]
 */
const BASE = 'http://127.0.0.1:9222'
const url = process.argv[2]
const width = Number(process.argv[3] || 390)

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
await send('Emulation.setDeviceMetricsOverride', {
  width,
  height: 900,
  deviceScaleFactor: 1,
  mobile: width < 768,
})
await send('Page.navigate', { url })
await sleep(3000)

const report = await evaluate(`(() => {
  const vw = window.innerWidth
  const offenders = []
  document.querySelectorAll('body *').forEach((el) => {
    const r = el.getBoundingClientRect()
    if (r.width === 0 && r.height === 0) return
    if (r.right > vw + 1 || r.left < -1) {
      const style = getComputedStyle(el)
      if (style.position === 'fixed') return
      offenders.push({
        tag: el.tagName.toLowerCase(),
        cls: (el.className && String(el.className).slice(0, 60)) || '',
        left: Math.round(r.left),
        right: Math.round(r.right),
        width: Math.round(r.width),
      })
    }
  })
  return {
    viewport: vw,
    scrollWidth: document.documentElement.scrollWidth,
    bodyScrollWidth: document.body.scrollWidth,
    offenders: offenders.slice(0, 15),
  }
})()`)

console.log(JSON.stringify(report, null, 2))

const shot = await send('Page.captureScreenshot', { format: 'png', captureBeyondViewport: false })
if (shot.result?.data) {
  const out = process.argv[4] || null
  if (out) fs.writeFileSync(out, Buffer.from(shot.result.data, 'base64'))
}
ws.close()
