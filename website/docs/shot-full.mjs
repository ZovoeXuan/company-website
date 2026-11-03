/**
 * 整页截图：按页面实际高度裁剪，用于视觉走查。
 * 用法：node shot-full.mjs <url> <out.png> [width]
 */
import fs from 'node:fs'

const BASE = 'http://127.0.0.1:9222'
const url = process.argv[2]
const out = process.argv[3]
const width = Number(process.argv[4] || 1440)

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

await send('Page.enable')
await send('Emulation.setDeviceMetricsOverride', {
  width,
  height: 900,
  deviceScaleFactor: 1,
  mobile: width < 768,
})
await send('Page.navigate', { url })
await sleep(3200)

const metrics = await send('Page.getLayoutMetrics')
const height = Math.min(Math.ceil(metrics.result?.cssContentSize?.height ?? 900), 12000)

await send('Emulation.setDeviceMetricsOverride', {
  width,
  height,
  deviceScaleFactor: 1,
  mobile: width < 768,
})
await sleep(600)

const shot = await send('Page.captureScreenshot', {
  format: 'png',
  captureBeyondViewport: true,
  clip: { x: 0, y: 0, width, height, scale: 1 },
})
if (shot.result?.data) {
  fs.writeFileSync(out, Buffer.from(shot.result.data, 'base64'))
  console.log(`saved ${out} ${width}x${height}`)
}
ws.close()
