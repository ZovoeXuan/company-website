import fs from 'node:fs'

/**
 * 验证官网导航二级菜单（开发自测用）。
 * 前置：官网开发服务器已启动，Chrome 带 --remote-debugging-port=9222。
 */
const BASE = 'http://127.0.0.1:9222'
const url = process.env.CMS_URL || 'http://127.0.0.1:5173/'
const out = process.env.CMS_SHOT || 'nav-dropdown.png'

const target = await (await fetch(`${BASE}/json/new?${encodeURIComponent(url)}`, { method: 'PUT' })).json()
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
await send('Runtime.enable')
await send('Emulation.setDeviceMetricsOverride', { width: 1440, height: 900, deviceScaleFactor: 1, mobile: false })
await sleep(2500)

const clicked = await evaluate(`(() => {
  const buttons = [...document.querySelectorAll('nav button')]
  const target = buttons.find((b) => b.textContent.trim().startsWith('产品'))
  if (!target) return 'no-button'
  target.click()
  return 'clicked'
})()`)
console.log('点击结果:', clicked)
await sleep(600)

const sub = await evaluate(`(() => {
  const items = [...document.querySelectorAll('.sub__link')].map((a) => a.textContent.trim() + ' -> ' + a.getAttribute('href'))
  return { count: items.length, items, expanded: document.querySelector('nav button[aria-expanded="true"]')?.textContent.trim() ?? null }
})()`)
console.log('二级菜单:', JSON.stringify(sub))

const shot = await send('Page.captureScreenshot', { format: 'png' })
if (shot.result?.data) {
  fs.writeFileSync(out, Buffer.from(shot.result.data, 'base64'))
  console.log('截图已保存', out)
}
ws.close()
