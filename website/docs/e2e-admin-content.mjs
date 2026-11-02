import fs from 'node:fs'

/**
 * 后台内容编辑页端到端验证：登录 → 列表 → 编辑 → 改标题 → 保存草稿 → 回读接口。
 * 用法：CMS_E2E_USER=admin CMS_E2E_PASSWORD=... node e2e-admin-content.mjs
 */
const BASE = 'http://127.0.0.1:9222'
const ORIGIN = 'http://127.0.0.1:5174'
const username = process.env.CMS_E2E_USER
const password = process.env.CMS_E2E_PASSWORD

if (!username || !password) {
  console.error('缺少 CMS_E2E_USER / CMS_E2E_PASSWORD')
  process.exit(2)
}

const target = await (await fetch(`${BASE}/json/new?${encodeURIComponent(`${ORIGIN}/admin/login`)}`, { method: 'PUT' })).json()
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
  if (res.result?.exceptionDetails) {
    return `ERROR: ${res.result.exceptionDetails.text}`
  }
  return res.result?.result?.value
}

await send('Page.enable')
await send('Runtime.enable')
await send('Emulation.setDeviceMetricsOverride', { width: 1440, height: 900, deviceScaleFactor: 1, mobile: false })
await sleep(2000)

// 登录
await evaluate(`(() => {
  const setValue = (el, value) => {
    const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set
    setter.call(el, value)
    el.dispatchEvent(new Event('input', { bubbles: true }))
  }
  const inputs = document.querySelectorAll('input')
  setValue(inputs[0], ${JSON.stringify(username)})
  setValue(inputs[1], ${JSON.stringify(password)})
  document.querySelector('button[type=submit]').click()
  return 'submitted'
})()`)
await sleep(2500)
console.log('登录后:', await evaluate('location.pathname'))

// 列表页
await send('Page.navigate', { url: `${ORIGIN}/admin/contents/products` })
await sleep(2500)
const listText = await evaluate("document.body.innerText.replace(/\\n+/g, ' | ').slice(0, 400)")
console.log('列表页文本:', listText)

// 编辑页
await send('Page.navigate', { url: `${ORIGIN}/admin/contents/products/1/edit` })
await sleep(2500)
const before = await evaluate("document.querySelector('input')?.value || ''")
console.log('标题输入框初值:', before)

// 改标题并保存
const title = `设备管理平台 V3 界面保存 ${new Date().toISOString().slice(11, 19)}`
await evaluate(`(() => {
  const input = document.querySelector('input')
  const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set
  setter.call(input, ${JSON.stringify(title)})
  input.dispatchEvent(new Event('input', { bubbles: true }))
  const btn = Array.from(document.querySelectorAll('button')).find((b) => b.textContent?.includes('保存草稿'))
  if (!btn) return 'no-button'
  btn.click()
  return 'clicked'
})()`)
await sleep(2000)

const after = await evaluate(`fetch('/api/admin/contents/1', { credentials: 'include' })
  .then((r) => r.json())
  .then((body) => body.data?.draft?.title ?? 'no-title')`)
console.log('接口回读草稿标题:', after)
console.log('保存成功:', after === title)

const shot = await send('Page.captureScreenshot', { format: 'png' })
if (shot.result?.data) {
  fs.writeFileSync('admin-editor.png', Buffer.from(shot.result.data, 'base64'))
  console.log('截图已保存 admin-editor.png')
}
ws.close()
