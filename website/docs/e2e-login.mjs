import fs from 'node:fs'

/**
 * 端到端登录验证脚本（开发自测用）。
 * 用法：
 *   CMS_E2E_USER=admin CMS_E2E_PASSWORD=你的密码 node e2e-login.mjs
 * 前置：后端已启动、admin 开发服务器已启动（默认 5174），Chrome 带 --remote-debugging-port=9222。
 * 不要把真实密码写进本文件或提交到仓库。
 */
const BASE = 'http://127.0.0.1:9222'
const url = process.env.CMS_E2E_URL || 'http://127.0.0.1:5174/admin/login'
const username = process.env.CMS_E2E_USER
const password = process.env.CMS_E2E_PASSWORD

if (!username || !password) {
  console.error('缺少 CMS_E2E_USER / CMS_E2E_PASSWORD，已跳过')
  process.exit(2)
}

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
await send('Emulation.setDeviceMetricsOverride', {
  width: Number(process.env.CMS_E2E_WIDTH || 1440),
  height: Number(process.env.CMS_E2E_HEIGHT || 1200),
  deviceScaleFactor: 1,
  mobile: false,
})
await sleep(1800)

console.log('--- 登录前 ---')
console.log(await evaluate('document.title'))

const clicked = await evaluate(`(() => {
  const setValue = (el, value) => {
    const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set
    setter.call(el, value)
    el.dispatchEvent(new Event('input', { bubbles: true }))
  }
  const inputs = document.querySelectorAll('input')
  if (inputs.length < 2) return 'no-inputs'
  setValue(inputs[0], ${JSON.stringify(process.env.CMS_E2E_USER)})
  setValue(inputs[1], ${JSON.stringify(process.env.CMS_E2E_PASSWORD)})
  document.querySelector('button[type=submit]').click()
  return 'clicked'
})()`)
console.log('点击结果:', clicked)

await sleep(3000)

const path = process.env.CMS_E2E_PATH
if (path) {
  await send('Page.navigate', { url: path })
  await sleep(3000)
}

console.log('--- 登录后 URL ---')
console.log(await evaluate('location.pathname'))
console.log('--- 页面文本 ---')
console.log((await evaluate('document.body.innerText') || '').slice(0, 500))

const shot = await send('Page.captureScreenshot', { format: 'png' })
if (shot.result?.data) {
  const out = process.env.CMS_E2E_SHOT || 'dashboard.png'
  fs.writeFileSync(out, Buffer.from(shot.result.data, 'base64'))
  console.log('截图已保存', out)
} else {
  console.log('截图失败:', JSON.stringify(shot).slice(0, 300))
}
ws.close()
