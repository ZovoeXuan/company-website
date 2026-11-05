# 公司官网 (`ZovoeXuan/company-website`) 全栈代码审计、Bug 修复与视觉交互升级指南

本文档基于 GitHub 仓库 `ZovoeXuan/company-website` 的全量源码、《公司官网后台及前后端联动开发文档》与 G7 易流官网参考录屏编制，涵盖 **三端代码深度诊断清单**、**新增/升级交互模块说明** 以及 **核心修复文件的完整源码**。

## 一、三端源码深度审计与已修复 Bug 清单

| 端别 | 严重度 | 文件路径 | 问题复现与根因分析 | 修复与升级方案 |
| :--- | :--- | :--- | :--- | :--- |
| **共享组件** | **P0** | `website/shared/ArticleBody.vue` | 模板正文容器使用 `class="cms-article__prose"`，而样式表全部針對 `.cms-prose` 编写选择器，导致产品、方案、案例、新闻详情页及后台草稿预览的富文本排版（`h2/h3/table/blockquote/img`）完全失效。 | 统一绑定 `class="cms-article__prose cms-prose"`，新增视频封面（`cover.type === 'VIDEO'`）渲染支持，升级表格表头、引用块与标题下划线排版。 |
| **官网前台** | **P1** | `website/web/src/components/ConsultDialog.vue` | 点击顶部「数字物流专家 小七」仅弹出普通表单，缺少参考视频（04:27–04:43）中的 AI 专家问答引导与高频问题预设。 | 升级为 **「数字物流专家 小七」智能交互抽屉**，内置 4 大行业高频问答折叠卡，点击问题即展示专业解答并自动将对应诉求带入下方预约咨询表单。 |
| **官网前台** | **P1** | `website/web/src/components/SiteFooter.vue` | 硬编码了 `connect@g7.com.cn`、地址及底部 `© 2026 G7E6.com.cn`，忽略了后台 `SiteConfig` 的 `contact.phone`、`email`、`address`、`policeText` 等动态字段；且缺少 `ui-redesign.md` 要求的大字号服务热线。 | 全面对接 `config.contact.*` 与 `config.footer.*` 动态配置，新增 24px 高亮全国服务热线、公安备案展示及品牌 Logo 图像渲染。 |
| **官网前台** | **P1** | `website/web/src/composables/usePageMeta.ts` & `consult.ts` | `usePageMeta.ts` 硬编码了 `SITE_NAME = '示例科技'` 与 `ORIGIN = 'https://www.example.com'`；且 `SiteLayout.vue` 与 `consult.ts` 重复请求 `/api/public/site-config`。 | 在 `consult.ts` 中建立响应式 `siteCache` 单例缓存，`usePageMeta.ts` 动态读取 `siteCache.siteName`、`defaultSeo` 与 `faviconUrl`，并使用 `window.location.origin` 生成 Canonical URL。 |
| **管理后台** | **P0** | `website/admin/src/views/DashboardView.vue` & `AdminLayout.vue` | 工作台 4 个快捷新建按钮路径写为 `/contents/*/new`，而 `router/index.ts` 定义为 `/contents/*/create`，点击后因路由未命中被通配符静默重定向回 `/dashboard`。 | 将快捷入口路径全部修正为 `/contents/{slug}/create`，并新增「首页草稿预览」快捷入口。 |
| **管理后台** | **P0** | `website/admin/src/views/UsersView.vue` | 存在 4 处阻断级 Bug：<br>1. 调用 `request()` 时手动 `JSON.stringify(payload)`，而 `http.ts` 内部再次 `JSON.stringify`，导致双重序列化报 400；<br>2. 编辑账号传 `lockVersion` 而后端 `UpdateUserRequest` 要求 `@NotNull expectedLockVersion`；<br>3. 新建账号多传了 `CreateUserRequest` 不存在的 `enabled` 字段且未強制要求初始密码；<br>4. 重置密码未传任何请求体，而后端 `ResetPasswordRequest` 要求 `expectedLockVersion` 与 `newPassword`。 | 1. 移除手动 `JSON.stringify`；<br>2. 对齐 `expectedLockVersion` 字段名；<br>3. 严格对齐 `CreateUserRequest` 字段与 12 位密码校验；<br>4. 重置密码改为 `ElMessageBox.prompt` 输入新密码并携带 `expectedLockVersion` 提交。 |
| **管理后台** | **P1** | `website/admin/src/components/ConfigPanel.vue` & `CategoryView.vue` | 开发文档 4.1 规定 `EDITOR` 编辑角色不可执行发布、下线与删除，但前端仍对 `EDITOR` 显示这些按钮；且 `CategoryView.vue` 在分类下线后（`status === 'OFFLINE'` 且 `hasUnpublishedChanges === false`）隐藏了发布按钮，导致无法重新发布。 | 接入 `useAuth()` 判断 `isAdmin`，对 `EDITOR` 隐藏发布/下线/删除按钮并展示角色提示；对 `OFFLINE` 状态的分类显示「重新发布」按钮。 |
| **管理后台** | **P1** | `website/admin/src/api/http.ts` | 当服务端 Session 超时返回 `401` 或 CSRF 令牌旋转失效返回 `403` 时，`http.ts` 未清空模块级 `csrfToken` 缓存，导致后续请求持续失败。 | 收到 `401/403` 时自动调用 `clearCsrf()`，并在非登录页收到 `401` 时自动携带 `redirect` 跳回登录页。 |
| **Java 后端** | **P1** | `website/server/.../dashboard/DashboardService.java` | `draftContents` 仅统计 `publication_status = 'UNPUBLISHED'`，漏掉了已发布但有未发布草稿修改（`draft_version <> published_version`）的内容。 | 修正 SQL 为 `published_version IS NULL OR draft_version <> published_version`，与开发文档 5.1 节 `hasUnpublishedChanges` 口径严格一致。 |

## 二、如何使用一键升级脚本

1. 下载随附的 `apply_website_upgrades.py` 脚本，放入你本地的 `D:\中转站\公司官网\` 根目录（即 `website` 文件夹所在目录）。
2. 在终端运行：
   ```bash
   python apply_website_upgrades.py
   ```
3. 脚本会自动将原有同名文件备份至 `.upgrade_backup_时间戳/` 目录，并将全部修复与升级后的文件写入对应路径。

## 三、核心升级文件完整源码对照

### `website/shared/ArticleBody.vue`

```vue
<script setup lang="ts">
/**
 * 详情正文的共享渲染组件。
 *
 * 官网详情页与后台草稿预览都使用它，保证「预览看到的就是发布后的样子」，
 * 避免维护两套排版模板。样式自带、类名以 cms- 前缀隔离，不依赖任何一方
 * 主题变量，因此在官网与后台两种配色下都能正确显示。
 */
withDefaults(
  defineProps<{
    cover?: {
      url: string
      alt?: string
      width?: number
      height?: number
      type?: 'IMAGE' | 'VIDEO'
    } | null
    capabilities?: string[]
    bodyHtml?: string
    /** 正文为空时是否隐藏（预览草稿时避免出现一块空白） */
    hideEmptyBody?: boolean
  }>(),
  {
    cover: null,
    capabilities: () => [],
    bodyHtml: '',
    hideEmptyBody: true,
  },
)
</script>

<template>
  <article class="cms-article">
    <figure v-if="cover" class="cms-article__cover">
      <video
        v-if="cover.type === 'VIDEO'"
        :src="cover.url"
        controls
        playsinline
        preload="metadata"
        class="cms-article__video"
      ></video>
      <img
        v-else
        :src="cover.url"
        :alt="cover.alt || ''"
        :width="cover.width"
        :height="cover.height"
        loading="lazy"
      />
    </figure>

    <ul v-if="capabilities.length" class="cms-article__caps" aria-label="核心能力">
      <li v-for="cap in capabilities" :key="cap" class="cms-article__cap">{{ cap }}</li>
    </ul>

    <div v-if="bodyHtml" class="cms-article__prose cms-prose" v-html="bodyHtml"></div>
    <p v-else-if="!hideEmptyBody" class="cms-article__empty">尚未填写正文</p>

    <slot />
  </article>
</template>

<style>
.cms-article {
  min-width: 0;
}

.cms-article__cover {
  margin: 0 0 32px;
}

.cms-article__cover img,
.cms-article__video {
  width: 100%;
  height: auto;
  aspect-ratio: 16 / 9;
  object-fit: cover;
  border-radius: 20px;
  background: #0f1117;
  display: block;
  box-shadow: 0 16px 40px rgba(10, 12, 20, 0.08);
}

.cms-article__caps {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  list-style: none;
  margin: 0 0 32px;
  padding: 0;
  font-size: 13px;
  color: #4636d6;
}

.cms-article__cap {
  padding: 6px 14px;
  border-radius: 999px;
  background: #eeebff;
  font-weight: 500;
  position: static;
}

.cms-article__cap + .cms-article__cap::before {
  display: none;
}

.cms-article__empty {
  color: #8b949e;
  font-size: 14px;
}

.cms-prose,
.cms-article__prose {
  font-size: 16px;
  line-height: 1.95;
  color: #2b3140;
  overflow-wrap: anywhere;
}

.cms-prose > * + *,
.cms-article__prose > * + * {
  margin-top: 18px;
}

.cms-prose h2,
.cms-article__prose h2 {
  font-size: 26px;
  font-weight: 600;
  line-height: 1.4;
  margin-top: 48px;
  margin-bottom: 16px;
  color: #101014;
  letter-spacing: -0.02em;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(16, 16, 20, 0.08);
}

.cms-prose h3,
.cms-article__prose h3 {
  font-size: 20px;
  font-weight: 600;
  margin-top: 34px;
  margin-bottom: 12px;
  color: #101014;
}

.cms-prose p,
.cms-article__prose p {
  margin: 16px 0;
}

.cms-prose ul,
.cms-prose ol,
.cms-article__prose ul,
.cms-article__prose ol {
  padding-left: 1.5em;
  margin: 18px 0;
}

.cms-prose li + li,
.cms-article__prose li + li {
  margin-top: 8px;
}

.cms-prose a,
.cms-article__prose a {
  color: #5b4be8;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.cms-prose img,
.cms-article__prose img {
  max-width: 100%;
  height: auto;
  border-radius: 16px;
  margin: 24px 0;
  display: block;
}

.cms-prose table,
.cms-article__prose table {
  width: 100%;
  border-collapse: collapse;
  display: block;
  overflow-x: auto;
  margin: 24px 0;
}

.cms-prose th,
.cms-prose td,
.cms-article__prose th,
.cms-article__prose td {
  border: 1px solid #e2e7ee;
  padding: 10px 14px;
  text-align: left;
}

.cms-prose th,
.cms-article__prose th {
  background: #f5f7fa;
  font-weight: 600;
  color: #101014;
}

.cms-prose blockquote,
.cms-article__prose blockquote {
  margin: 24px 0;
  padding: 14px 20px;
  border-left: 4px solid #5b4be8;
  background: #f8f8fc;
  border-radius: 0 12px 12px 0;
  color: #4a5361;
}
</style>
```

### `website/web/src/composables/consult.ts`

```typescript
import { reactive, ref } from 'vue'
import { getContactPage, getSite } from '@/api/public'
import type { ContactPage, SiteConfig } from '@/data/types'

export interface ConsultContext {
  site: SiteConfig | null
  page: ContactPage | null
  formEnabled: boolean
  privacyVersion: number | null
  successText: string
}

export interface ConsultTarget {
  sourcePosition: string
  contentType?: string
  contentId?: string
  presetQuestion?: string
}

const siteCache = ref<SiteConfig | null>(null)
let pending: Promise<ConsultContext> | null = null

export function loadConsultContext(forceRefresh = false): Promise<ConsultContext> {
  if (!pending || forceRefresh) {
    pending = Promise.all([
      getSite().catch(() => null),
      getContactPage().catch(() => null),
    ]).then(([site, page]) => {
      if (site) siteCache.value = site
      return {
        site,
        page,
        formEnabled: Boolean(site?.formEnabled ?? page?.formEnabled),
        privacyVersion: site?.privacyVersion ?? null,
        successText: page?.successText || '已收到您的咨询，我们的专家顾问将在 1 个工作日内与您联系。',
      }
    })
  }
  return pending
}

export function useSiteConfigCache() {
  return { siteCache, loadConsultContext }
}

const dialog = reactive<{
  open: boolean
  target: ConsultTarget
}>({
  open: false,
  target: { sourcePosition: 'OTHER_CTA' },
})

export function openConsult(target: ConsultTarget) {
  dialog.target = { ...target }
  dialog.open = true
}

export function closeConsult() {
  dialog.open = false
}

export function useConsultDialog() {
  return { dialog, openConsult, closeConsult }
}
```

### `website/web/src/composables/usePageMeta.ts`

```typescript
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
```

### `website/web/src/components/ConsultDialog.vue`

```vue
<script setup lang="ts">
import { nextTick, onBeforeUnmount, ref, watch } from 'vue'
import ConsultForm from '@/components/ConsultForm.vue'
import { useConsultDialog } from '@/composables/consult'

const { dialog, closeConsult } = useConsultDialog()
const panel = ref<HTMLElement | null>(null)
const selectedFaq = ref<number | null>(null)
const presetDemand = ref('')

interface FaqItem {
  q: string
  a: string
  demandPrefill: string
}

const faqs: FaqItem[] = [
  {
    q: '什么是「紫宝盒」全场景 AI 工具？',
    a: '紫宝盒是面向公路货运与车队管理打造的全场景 AI 智能终端与算法平台，覆盖 50+ 高危驾驶与装卸作业场景，兼具高准确率风险识别与车载智能语音交互。',
    demandPrefill: '我希望了解「紫宝盒」全场景 AI 工具的硬件参数、算法覆盖场景及试用方案。',
  },
  {
    q: '在利润与运力承压下，如何管好车辆、司机与能耗成本？',
    a: '通过 IoT 安全管家 + 数字能源 + 智能管车一体化平台，打通「定位、油耗/电耗、安全风控、自动结算」数据闭环，帮助车队平均降低事故率 51%、节省管理成本。',
    demandPrefill: '我们车队希望了解如何通过数字化平台降低车辆能耗、事故率与日常运营成本。',
  },
  {
    q: '新能源车队管理有哪些核心痛点与解决方案？',
    a: '针对新能源重卡续航焦虑、充电调度难与电池寿命监控问题，我们提供智能补能网络规划、能耗精细化核算与电池健康实时监测方案。',
    demandPrefill: '我们正在运营或筹建新能源车队，希望咨询新能源车队精细化运营与补能管理方案。',
  },
  {
    q: '冷链与大宗物流如何实现全流程透明化追货与防偷盗？',
    a: '依托高精温湿度传感器、电子锁与载重/视频融合 AI 算法，实现全程不脱温预警、异常停留报警与水泥/饲料等大宗散货防偷盗治理。',
    demandPrefill: '希望了解冷链全程温控 / 大宗散货防偷盗 AI 治理方案及成功案例。',
  },
]

function selectFaq(index: number) {
  selectedFaq.value = selectedFaq.value === index ? null : index
  if (selectedFaq.value !== null) {
    presetDemand.value = faqs[index].demandPrefill
  }
}

function onKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape' && dialog.open) {
    closeConsult()
  }
}

watch(
  () => dialog.open,
  async (open) => {
    if (open) {
      presetDemand.value = dialog.target.presetQuestion || ''
      document.addEventListener('keydown', onKeydown)
      document.body.style.overflow = 'hidden'
      await nextTick()
      panel.value?.focus()
    } else {
      document.removeEventListener('keydown', onKeydown)
      document.body.style.overflow = ''
    }
  },
)

onBeforeUnmount(() => {
  document.removeEventListener('keydown', onKeydown)
  document.body.style.overflow = ''
})
</script>

<template>
  <Teleport to="body">
    <Transition name="drawer">
      <div
        v-if="dialog.open"
        class="overlay"
        role="dialog"
        aria-modal="true"
        aria-labelledby="consult-dialog-title"
        @click.self="closeConsult"
      >
        <div ref="panel" class="panel" tabindex="-1">
          <!-- 顶部数字专家助手卡片（对齐参考视频 04:27 小七助手抽屉） -->
          <div class="panel__head">
            <div class="panel__assistant">
              <div class="assistant-avatar" aria-hidden="true">
                <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="11" width="18" height="10" rx="2"></rect>
                  <circle cx="12" cy="5" r="2"></circle>
                  <path d="M12 7v4"></path>
                  <line x1="8" y1="16" x2="8.01" y2="16"></line>
                  <line x1="16" y1="16" x2="16.01" y2="16"></line>
                </svg>
                <span class="assistant-status"></span>
              </div>
              <div>
                <h2 id="consult-dialog-title" class="panel__title">您好，我是数字物流专家 小七</h2>
                <p class="panel__sub">7×24h 实时解答产品方案，为您预约专属行业顾问演示</p>
              </div>
            </div>
            <button type="button" class="panel__close" aria-label="关闭咨询窗口" @click="closeConsult">
              ✕
            </button>
          </div>

          <!-- 猜你想问快捷问答区（对齐参考视频右侧智能问答交互） -->
          <div class="faq-section">
            <div class="faq-section__header">
              <span class="faq-section__badge">💡 猜你想问</span>
              <span class="faq-section__hint">点击问题查看解答并自动带入咨询需求</span>
            </div>
            <ul class="faq-list">
              <li v-for="(item, idx) in faqs" :key="item.q" class="faq-item">
                <button
                  type="button"
                  class="faq-item__q"
                  :class="{ 'is-active': selectedFaq === idx }"
                  @click="selectFaq(idx)"
                >
                  <span>{{ item.q }}</span>
                  <span class="faq-item__arrow" aria-hidden="true">{{ selectedFaq === idx ? '−' : '+' }}</span>
                </button>
                <div v-if="selectedFaq === idx" class="faq-item__a">
                  <p>{{ item.a }}</p>
                </div>
              </li>
            </ul>
          </div>

          <div class="form-divider">
            <span>填写预约信息 · 专属顾问 1 对 1 演示</span>
          </div>

          <ConsultForm
            :source-position="dialog.target.sourcePosition"
            :content-type="dialog.target.contentType"
            :content-id="dialog.target.contentId"
            :initial-demand="presetDemand"
          />
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  z-index: 80;
  background: rgba(8, 8, 12, 0.55);
  backdrop-filter: blur(6px);
  display: flex;
  justify-content: flex-end;
}

.panel {
  width: min(500px, 100vw);
  height: 100dvh;
  overflow-y: auto;
  background: #ffffff;
  padding: 24px 28px 36px;
  box-shadow: -20px 0 60px rgba(8, 8, 12, 0.22);
  display: flex;
  flex-direction: column;
  outline: none;
}

.panel__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 16px;
  border-radius: 16px;
  background: linear-gradient(135deg, #f5f3ff 0%, #eef2ff 100%);
  border: 1px solid rgba(91, 75, 232, 0.14);
  margin-bottom: 18px;
}

.panel__assistant {
  display: flex;
  align-items: center;
  gap: 12px;
}

.assistant-avatar {
  position: relative;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6d5ce8 0%, #4636d6 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 18px rgba(91, 75, 232, 0.35);
  flex: none;
}

.assistant-status {
  position: absolute;
  right: 1px;
  bottom: 1px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #10b981;
  border: 2px solid #ffffff;
}

.panel__title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #101014;
}

.panel__sub {
  margin: 4px 0 0;
  font-size: 12px;
  color: #5f6470;
}

.panel__close {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  background: rgba(255, 255, 255, 0.8);
  color: #5f6470;
  font-size: 14px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.panel__close:hover {
  background: #ffffff;
  color: #101014;
}

.faq-section {
  background: #f8f9fc;
  border: 1px solid rgba(16, 16, 20, 0.07);
  border-radius: 14px;
  padding: 14px 16px;
  margin-bottom: 20px;
}

.faq-section__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.faq-section__badge {
  font-size: 12.5px;
  font-weight: 600;
  color: #4636d6;
}

.faq-section__hint {
  font-size: 11.5px;
  color: #8b90a0;
}

.faq-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.faq-item__q {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 9px 12px;
  border-radius: 8px;
  border: 1px solid rgba(16, 16, 20, 0.06);
  background: #ffffff;
  color: #101014;
  font-size: 13px;
  font-weight: 500;
  text-align: left;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s ease;
}

.faq-item__q:hover,
.faq-item__q.is-active {
  border-color: rgba(91, 75, 232, 0.4);
  color: #5b4be8;
  background: #fcfbff;
}

.faq-item__arrow {
  font-weight: 700;
  color: #5b4be8;
}

.faq-item__a {
  padding: 10px 12px;
  font-size: 12.5px;
  line-height: 1.65;
  color: #5f6470;
  background: rgba(91, 75, 232, 0.04);
  border-radius: 0 0 8px 8px;
  margin-top: -2px;
}

.faq-item__a p {
  margin: 0;
}

.form-divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  font-size: 12px;
  font-weight: 600;
  color: #5f6470;
}

.form-divider::before,
.form-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: rgba(16, 16, 20, 0.08);
}

.drawer-enter-active,
.drawer-leave-active {
  transition: opacity 0.25s ease;
}

.drawer-enter-active .panel,
.drawer-leave-active .panel {
  transition: transform 0.3s cubic-bezier(0.22, 1, 0.36, 1);
}

.drawer-enter-from,
.drawer-leave-to {
  opacity: 0;
}

.drawer-enter-from .panel,
.drawer-leave-to .panel {
  transform: translateX(100%);
}
</style>
```

### `website/web/src/components/SiteFooter.vue`

```vue
<script setup lang="ts">
import { computed } from 'vue'
import { openConsult } from '@/composables/consult'
import type { NavItem, SiteConfig } from '@/data/types'

const props = defineProps<{ config: SiteConfig }>()

const defaultFooterGroups: Array<{ title: string; links: NavItem[] }> = [
  {
    title: 'IoT服务',
    links: [
      { label: '安全管家', path: '/products' },
      { label: '冷链管家', path: '/products' },
      { label: '流向管家', path: '/products' },
      { label: '追货管家', path: '/products' },
      { label: '开放平台', path: '/products' },
    ],
  },
  {
    title: '交易服务',
    links: [
      { label: '财运通', path: '/solutions' },
      { label: '数字能源', path: '/solutions' },
      { label: '共享挂车', path: '/solutions' },
    ],
  },
  {
    title: '解决方案',
    links: [
      { label: '大宗物流数字化', path: '/solutions' },
      { label: '连锁零售数字化', path: '/solutions' },
      { label: '饲料防偷盗 AI 治理', path: '/solutions' },
    ],
  },
  {
    title: '关于我们',
    links: [
      { label: '企业简介', path: '/about' },
      { label: '发展历程', path: '/about' },
      { label: '新闻中心', path: '/news' },
      { label: '联系我们', path: '/contact' },
    ],
  },
]

const groups = computed(() => {
  const configured = props.config.footer?.groups ?? []
  return configured.length > 0 ? configured : defaultFooterGroups
})

const contactPhone = computed(() => props.config.contact?.phone || '400-888-6688')
const contactEmail = computed(() => props.config.contact?.email || 'connect@g7.com.cn')
const contactAddress = computed(
  () => props.config.contact?.address || '北京市海淀区中关村南大街1号 / 深圳湾科技生态园',
)
const contactHours = computed(() => props.config.contact?.hours || '周一至周日 09:00 - 18:00')
const copyrightText = computed(
  () => props.config.footer?.copyright || `© ${new Date().getFullYear()} ${props.config.siteName} 版权所有`,
)
</script>

<template>
  <footer class="footer on-dark">
    <!-- 页脚上方醒目 CTA 横幅 -->
    <div class="footer-cta-band">
      <div class="container container--wide footer-cta-band__inner">
        <div>
          <h2 class="footer-cta-band__title">以物联网技术和软件服务改变公路货运</h2>
          <p class="footer-cta-band__sub">立即预约行业专家，获取针对您车队与业务场景的定制化降本增效方案</p>
        </div>
        <button
          type="button"
          class="btn footer-cta-band__btn"
          @click="openConsult({ sourcePosition: 'FOOTER' })"
        >
          立即咨询 →
        </button>
      </div>
    </div>

    <!-- 多列企业导航与联系矩阵 -->
    <div class="container container--wide footer__body">
      <!-- 品牌标识列 -->
      <div class="footer__col footer__col--brand">
        <RouterLink to="/" class="footer__logo" :aria-label="`${config.siteName} 首页`">
          <img v-if="config.logoUrl" :src="config.logoUrl" :alt="config.logoAlt || config.siteName" class="footer__logo-img" />
          <template v-else>
            <span class="footer__mark" aria-hidden="true">{{ config.siteName?.slice(0, 1) || 'G' }}</span>
            <span class="footer__name">{{ config.siteName }}</span>
          </template>
        </RouterLink>
        <p class="footer__motto">{{ config.slogan || '领先的物联网与数字化软件服务商' }}</p>
      </div>

      <!-- 业务与方案列 -->
      <nav v-for="group in groups" :key="group.title" class="footer__col" :aria-label="group.title">
        <p class="footer__title">{{ group.title }}</p>
        <ul class="footer__list">
          <li v-for="item in group.links" :key="item.label">
            <button
              v-if="item.linkType === 'CONSULT'"
              type="button"
              class="footer__link footer__link--button"
              @click="openConsult({ sourcePosition: 'FOOTER' })"
            >
              {{ item.label }}
            </button>
            <a
              v-else-if="item.url"
              :href="item.url"
              class="footer__link"
              :target="item.newWindow ? '_blank' : undefined"
              :rel="item.newWindow ? 'noopener noreferrer' : undefined"
            >
              {{ item.label }}
            </a>
            <RouterLink v-else-if="item.path" :to="item.path" class="footer__link">
              {{ item.label }}
            </RouterLink>
            <span v-else class="footer__link">{{ item.label }}</span>
          </li>
        </ul>
      </nav>

      <!-- 联系我们列（突出大字号服务热线，对齐 ui-redesign.md 规范） -->
      <div class="footer__col footer__col--contact">
        <p class="footer__title">联系我们</p>
        <div class="footer__hotline">
          <span class="footer__contact-label">全国服务热线 ({{ contactHours }})</span>
          <a :href="`tel:${contactPhone}`" class="footer__phone">{{ contactPhone }}</a>
        </div>
        <ul class="footer__list">
          <li class="footer__contact-item">
            <span class="footer__contact-label">商务合作与媒体联系</span>
            <a :href="`mailto:${contactEmail}`" class="footer__link">{{ contactEmail }}</a>
          </li>
          <li class="footer__contact-address">
            {{ contactAddress }}
          </li>
        </ul>
      </div>

      <!-- 关注我们列 -->
      <div class="footer__col footer__col--qr">
        <p class="footer__title">关注我们</p>
        <div class="qr-group">
          <div class="qr-item">
            <div class="qr-item__box" aria-hidden="true">
              <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="1.5">
                <rect x="3" y="3" width="7" height="7"></rect>
                <rect x="14" y="3" width="7" height="7"></rect>
                <rect x="3" y="14" width="7" height="7"></rect>
                <rect x="14" y="14" width="3" height="3"></rect>
                <rect x="18" y="18" width="3" height="3"></rect>
              </svg>
            </div>
            <span class="qr-item__label">官方公众号</span>
          </div>
          <div class="qr-item">
            <div class="qr-item__box" aria-hidden="true">
              <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="1.5">
                <circle cx="12" cy="12" r="9"></circle>
                <polygon points="10 8 16 12 10 16 10 8" fill="currentColor"></polygon>
              </svg>
            </div>
            <span class="qr-item__label">官方视频号</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部版权与备案条 -->
    <div class="container container--wide footer__bottom">
      <div class="footer__bottom-left">
        <a
          v-if="config.footer?.icp && config.footer?.icpUrl"
          :href="config.footer.icpUrl"
          class="footer__legal"
          target="_blank"
          rel="noopener noreferrer"
        >
          {{ config.footer.icp }}
        </a>
        <span v-else-if="config.footer?.icp" class="footer__legal">{{ config.footer.icp }}</span>

        <template v-if="config.footer?.policeText">
          <span class="footer__divider">|</span>
          <a
            v-if="config.footer.policeUrl"
            :href="config.footer.policeUrl"
            class="footer__legal"
            target="_blank"
            rel="noopener noreferrer"
          >
            {{ config.footer.policeText }}
          </a>
          <span v-else class="footer__legal">{{ config.footer.policeText }}</span>
        </template>

        <span class="footer__divider">|</span>
        <RouterLink to="/privacy" class="footer__legal">隐私说明</RouterLink>
        <span class="footer__divider">|</span>
        <RouterLink to="/contact" class="footer__legal">联系我们</RouterLink>
      </div>
      <div class="footer__bottom-right">
        <span>{{ copyrightText }}</span>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  background: #090a0f;
  color: #a0a4b8;
  margin-top: 0;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.footer-cta-band {
  background: linear-gradient(90deg, #11131f 0%, #191636 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  padding: 44px 0;
}

.footer-cta-band__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

@media (max-width: 768px) {
  .footer-cta-band__inner {
    flex-direction: column;
    text-align: center;
  }
}

.footer-cta-band__title {
  margin: 0;
  font-size: clamp(22px, 2.6vw, 30px);
  font-weight: 700;
  color: #ffffff;
  letter-spacing: -0.02em;
}

.footer-cta-band__sub {
  margin: 8px 0 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.65);
}

.footer-cta-band__btn {
  background: #5b4be8;
  color: #ffffff;
  border: none;
  font-weight: 600;
  font-size: 15px;
  padding: 13px 32px;
  border-radius: 999px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.25s ease;
  box-shadow: 0 8px 24px rgba(91, 75, 232, 0.35);
}

.footer-cta-band__btn:hover {
  background: #4636d6;
  transform: translateY(-2px);
}

.footer__body {
  display: grid;
  grid-template-columns: 160px repeat(4, minmax(0, 1fr)) 1.5fr 130px;
  gap: 32px;
  padding: 64px 0 54px;
}

@media (max-width: 1100px) {
  .footer__body {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 680px) {
  .footer__body {
    grid-template-columns: 1fr;
    gap: 28px;
  }
}

.footer__col--brand {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.footer__logo {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: #fff;
  text-decoration: none;
}

.footer__logo-img {
  max-height: 34px;
  max-width: 150px;
  object-fit: contain;
}

.footer__mark {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  background: linear-gradient(135deg, #7a6bff 0%, #5b4be8 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 17px;
  color: #fff;
}

.footer__name {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.footer__motto {
  font-size: 12.5px;
  color: rgba(255, 255, 255, 0.5);
  margin: 0;
  line-height: 1.6;
}

.footer__title {
  margin: 0 0 18px;
  font-size: 14px;
  font-weight: 600;
  color: #ffffff;
  letter-spacing: 0.04em;
}

.footer__hotline {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 14px;
}

.footer__phone {
  font-size: 24px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: -0.01em;
  text-decoration: none;
  transition: color 0.2s ease;
}

.footer__phone:hover {
  color: #8b7df0;
}

.footer__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.footer__link {
  font-size: 13px;
  color: #8c90a4;
  text-decoration: none;
  background: none;
  border: none;
  padding: 0;
  font-family: inherit;
  cursor: pointer;
  text-align: left;
  transition: color 0.2s ease;
}

.footer__link:hover {
  color: #ffffff;
}

.footer__contact-item {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.footer__contact-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.42);
}

.footer__contact-address {
  font-size: 12px;
  line-height: 1.6;
  color: #717588;
  margin-top: 6px;
}

.qr-group {
  display: flex;
  gap: 16px;
}

.qr-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.qr-item__box {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  background: #181b24;
  border: 1px solid rgba(255, 255, 255, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.75);
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.qr-item:hover .qr-item__box {
  border-color: #5b4be8;
  transform: translateY(-2px);
}

.qr-item__label {
  font-size: 11px;
  color: #8c90a4;
}

.footer__bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 24px 0;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  font-size: 12px;
  color: #686c7e;
  flex-wrap: wrap;
}

.footer__bottom-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.footer__legal {
  color: inherit;
  text-decoration: none;
  transition: color 0.2s ease;
}

.footer__legal:hover {
  color: #ffffff;
}

.footer__divider {
  opacity: 0.3;
}
</style>
```

### `website/admin/src/api/http.ts`

```typescript
const BASE = import.meta.env.VITE_API_BASE ?? ''

export interface FieldErrorItem {
  field: string
  message: string
}

export class ApiError extends Error {
  status: number
  code: string
  requestId?: string
  fieldErrors?: FieldErrorItem[]

  constructor(status: number, code: string, message: string, requestId?: string, fieldErrors?: FieldErrorItem[]) {
    super(message)
    this.status = status
    this.code = code
    this.requestId = requestId
    this.fieldErrors = fieldErrors
  }
}

let csrfToken: string | null = null

export async function ensureCsrf(): Promise<string> {
  if (csrfToken) return csrfToken
  const response = await fetch(`${BASE}/api/admin/auth/csrf`, {
    credentials: 'include',
    cache: 'no-store',
    headers: { Accept: 'application/json' },
  })
  const body = await response.json().catch(() => null)
  if (!response.ok || body?.code !== 'OK') {
    throw new ApiError(response.status, body?.code ?? 'CSRF_UNAVAILABLE', '无法获取请求令牌')
  }
  csrfToken = body.data?.token ?? null
  if (!csrfToken) throw new ApiError(500, 'CSRF_UNAVAILABLE', '请求令牌为空')
  return csrfToken
}

export function clearCsrf() {
  csrfToken = null
}

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  body?: unknown
  form?: Record<string, string>
}

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const method = options.method ?? 'GET'
  const headers: Record<string, string> = { Accept: 'application/json' }
  let payload: BodyInit | undefined

  if (options.form) {
    headers['Content-Type'] = 'application/x-www-form-urlencoded'
    payload = new URLSearchParams(options.form).toString()
  } else if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json'
    payload = typeof options.body === 'string' ? options.body : JSON.stringify(options.body)
  }

  if (method !== 'GET') {
    headers['X-CSRF-TOKEN'] = await ensureCsrf()
  }

  const response = await fetch(`${BASE}${path}`, {
    method,
    headers,
    body: payload,
    credentials: 'include',
    cache: 'no-store',
  })

  if (response.status === 401 || response.status === 403) {
    clearCsrf()
  }

  return handle<T>(response, path)
}

/** multipart 上传：浏览器自动设置 boundary，不能手动指定 Content-Type。 */
export async function upload<T>(path: string, formData: FormData): Promise<T> {
  const response = await fetch(`${BASE}${path}`, {
    method: 'POST',
    headers: { Accept: 'application/json', 'X-CSRF-TOKEN': await ensureCsrf() },
    body: formData,
    credentials: 'include',
    cache: 'no-store',
  })
  if (response.status === 401 || response.status === 403) {
    clearCsrf()
  }
  return handle<T>(response, path)
}

async function handle<T>(response: Response, path = ''): Promise<T> {
  const text = await response.text()
  let body: any = null
  try {
    body = text ? JSON.parse(text) : null
  } catch {
    throw new ApiError(response.status, 'BAD_RESPONSE', '服务返回了无法解析的内容')
  }

  if (!response.ok || body?.code !== 'OK') {
    if (
      response.status === 401 &&
      typeof window !== 'undefined' &&
      !path.includes('/api/admin/auth/me') &&
      !path.includes('/api/admin/auth/login') &&
      !window.location.pathname.endsWith('/login')
    ) {
      const currentPath = window.location.pathname.replace(/^\/admin/, '') + window.location.search
      window.location.href = `/admin/login?redirect=${encodeURIComponent(currentPath)}`
    }
    throw new ApiError(
      response.status,
      body?.code ?? 'UNKNOWN',
      body?.message ?? `请求失败 (${response.status})`,
      body?.requestId,
      body?.fieldErrors,
    )
  }

  return body.data as T
}

export function get<T>(path: string): Promise<T> {
  return request<T>(path, { method: 'GET' })
}

export function post<T>(path: string, body?: unknown): Promise<T> {
  return request<T>(path, { method: 'POST', body })
}

export function put<T>(path: string, body?: unknown): Promise<T> {
  return request<T>(path, { method: 'PUT', body })
}

export function del<T>(path: string): Promise<T> {
  return request<T>(path, { method: 'DELETE' })
}
```

### `website/admin/src/components/ConfigPanel.vue`

```vue
<script setup lang="ts">
import { computed } from 'vue'
import { useAuth } from '@/stores/auth'

defineProps<{
  title: string
  status?: string
  draftVersion?: number
  publishedVersion?: number | null
  hasUnpublishedChanges?: boolean
  loading?: boolean
  saving?: boolean
  publishing?: boolean
}>()

const emit = defineEmits<{ save: []; publish: [] }>()
const { current } = useAuth()
const isAdmin = computed(() => current.value?.role === 'ADMIN')
</script>

<template>
  <div v-loading="loading" class="shell">
    <el-card shadow="never" class="main">
      <template #header>
        <div class="head">
          <span>{{ title }}</span>
          <slot name="header-actions" />
        </div>
      </template>
      <slot />
    </el-card>

    <aside class="side">
      <el-card shadow="never">
        <template #header>状态与版本</template>
        <div class="status">
          <p class="row">
            <span>发布状态</span>
            <el-tag size="small" :type="status === 'PUBLISHED' ? 'success' : 'info'">
              {{ status === 'PUBLISHED' ? '已发布' : '未发布' }}
            </el-tag>
          </p>
          <p class="row"><span>草稿版本</span><span>v{{ draftVersion ?? '-' }}</span></p>
          <p class="row"><span>线上版本</span><span>v{{ publishedVersion ?? '-' }}</span></p>
          <p v-if="hasUnpublishedChanges" class="warn">● 有未发布的草稿修改</p>
        </div>

        <div class="actions">
          <el-button type="primary" :loading="saving" @click="emit('save')">保存草稿</el-button>
          <el-button
            v-if="isAdmin"
            type="success"
            :loading="publishing"
            @click="emit('publish')"
          >
            发布上线
          </el-button>
          <p v-else class="role-hint">当前为编辑角色，保存草稿后请由管理员审核发布</p>
        </div>
      </el-card>

      <el-card v-if="$slots.tips" shadow="never" class="tips">
        <template #header>操作说明</template>
        <slot name="tips" />
      </el-card>
    </aside>
  </div>
</template>

<style scoped>
.shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 16px;
  align-items: start;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-weight: 600;
}

.side {
  position: sticky;
  top: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status .row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  padding: 6px 0;
  color: var(--a-text-muted);
}

.warn {
  margin-top: 8px;
  font-size: 12px;
  font-weight: 500;
  color: #b26a00;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 16px;
}

.actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

.role-hint {
  font-size: 12px;
  color: var(--a-text-muted);
  line-height: 1.5;
  margin-top: 4px;
}

.tips {
  font-size: 12px;
  color: var(--a-text-muted);
  line-height: 1.8;
}

@media (max-width: 1100px) {
  .shell {
    grid-template-columns: 1fr;
  }
}
</style>
```

### `website/admin/src/views/DashboardView.vue`

```vue
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { get } from '@/api/http'
import { useAuth } from '@/stores/auth'

interface DashboardData {
  publishedContents: number
  draftContents: number
  contentsByType: Record<string, number>
  pendingLeads?: number
  userCount?: number
}

const router = useRouter()
const { current } = useAuth()
const isAdmin = computed(() => current.value?.role === 'ADMIN')

const data = ref<DashboardData | null>(null)
const loading = ref(true)

const typeLabels: Record<string, { label: string; path: string; icon: string }> = {
  PRODUCT: { label: '产品中心', path: '/contents/products', icon: '📦' },
  SOLUTION: { label: '解决方案', path: '/contents/solutions', icon: '💡' },
  CASE: { label: '客户案例', path: '/contents/cases', icon: '🏆' },
  NEWS: { label: '新闻动态', path: '/contents/news', icon: '📰' },
  CUSTOMER: { label: '客户标识', path: '/contents/customers', icon: '🤝' },
}

const contentList = computed(() => {
  if (!data.value?.contentsByType) return []
  return Object.entries(data.value.contentsByType).map(([type, count]) => {
    const meta = typeLabels[type] || { label: type, path: '/contents/products', icon: '📄' }
    return {
      type,
      label: meta.label,
      count,
      path: meta.path,
      icon: meta.icon,
    }
  })
})

async function load() {
  loading.value = true
  try {
    data.value = await get<DashboardData>('/api/admin/dashboard')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '工作台加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="dashboard">
    <!-- 欢迎栏 -->
    <div class="welcome-bar">
      <div>
        <h2 class="welcome-title">您好，{{ current?.displayName || current?.username }}！</h2>
        <p class="welcome-sub">
          欢迎使用官网内容管理控制台。您当前以【{{ isAdmin ? '系统管理员' : '内容编辑' }}】身份登录。
        </p>
      </div>
      <div class="welcome-actions">
        <el-button type="primary" @click="router.push('/home')">编排首页</el-button>
        <el-button @click="router.push('/preview/home')">首页草稿预览</el-button>
        <el-button v-if="isAdmin" type="warning" @click="router.push('/leads')">查看咨询线索</el-button>
      </div>
    </div>

    <!-- 核心指标卡片 -->
    <div class="metrics-grid">
      <div class="metric-card metric-card--published" @click="router.push('/contents/products')">
        <div class="metric-card__header">
          <span class="metric-card__label">已发布线上内容</span>
          <span class="metric-card__badge">生效中</span>
        </div>
        <p class="metric-card__value">{{ data?.publishedContents ?? 0 }}</p>
        <p class="metric-card__tip">实时向官网前端公开接口输出</p>
      </div>

      <div class="metric-card metric-card--draft" @click="router.push('/contents/products')">
        <div class="metric-card__header">
          <span class="metric-card__label">待发布/草稿变更</span>
          <span class="metric-card__badge metric-card__badge--draft">待发布</span>
        </div>
        <p class="metric-card__value">{{ data?.draftContents ?? 0 }}</p>
        <p class="metric-card__tip">包含未发布草稿及有未发布修改的内容</p>
      </div>

      <div
        v-if="isAdmin"
        class="metric-card metric-card--leads"
        :class="{ 'metric-card--alert': (data?.pendingLeads ?? 0) > 0 }"
        @click="router.push('/leads')"
      >
        <div class="metric-card__header">
          <span class="metric-card__label">待处理客户咨询</span>
          <span class="metric-card__badge metric-card__badge--leads">NEW</span>
        </div>
        <p class="metric-card__value">{{ data?.pendingLeads ?? 0 }}</p>
        <p class="metric-card__tip">需在 4 小时工作时间内跟进与回复</p>
      </div>

      <div v-if="isAdmin" class="metric-card" @click="router.push('/system/users')">
        <div class="metric-card__header">
          <span class="metric-card__label">后台系统账号</span>
          <span class="metric-card__badge">安全受控</span>
        </div>
        <p class="metric-card__value">{{ data?.userCount ?? 0 }}</p>
        <p class="metric-card__tip">管理员与编辑双角色权限控制</p>
      </div>
    </div>

    <!-- 常用快捷入口（已修复 /create 路由匹配） -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-head">
          <span>快捷操作</span>
          <span class="card-head__desc">常用高频内容维护入口</span>
        </div>
      </template>
      <div class="quick-grid">
        <button type="button" class="quick-btn" @click="router.push('/contents/products/create')">
          <span class="quick-btn__icon">📦</span>
          <span class="quick-btn__title">录入产品</span>
          <span class="quick-btn__sub">新建工业硬件/软件能力</span>
        </button>
        <button type="button" class="quick-btn" @click="router.push('/contents/solutions/create')">
          <span class="quick-btn__icon">💡</span>
          <span class="quick-btn__title">新增解决方案</span>
          <span class="quick-btn__sub">发布垂直行业实施路径</span>
        </button>
        <button type="button" class="quick-btn" @click="router.push('/contents/cases/create')">
          <span class="quick-btn__icon">🏆</span>
          <span class="quick-btn__title">添加客户案例</span>
          <span class="quick-btn__sub">沉淀降本增效成果标杆</span>
        </button>
        <button type="button" class="quick-btn" @click="router.push('/contents/news/create')">
          <span class="quick-btn__icon">📰</span>
          <span class="quick-btn__title">发布新闻</span>
          <span class="quick-btn__sub">企业合并/签约/展会动态</span>
        </button>
        <button type="button" class="quick-btn" @click="router.push('/media')">
          <span class="quick-btn__icon">🖼️</span>
          <span class="quick-btn__title">素材库</span>
          <span class="quick-btn__sub">上传图片与视频媒体文件</span>
        </button>
        <button type="button" class="quick-btn" @click="router.push('/settings/site')">
          <span class="quick-btn__icon">⚙️</span>
          <span class="quick-btn__title">全站设置</span>
          <span class="quick-btn__sub">品牌 Logo、SEO 与备案信息</span>
        </button>
      </div>
    </el-card>

    <!-- 内容模块分布 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-head">
          <span>内容模块概览</span>
          <span class="card-head__desc">点击对应模块进入列表维护</span>
        </div>
      </template>
      <div class="type-grid">
        <div
          v-for="item in contentList"
          :key="item.type"
          class="type-item"
          @click="router.push(item.path)"
        >
          <span class="type-item__icon">{{ item.icon }}</span>
          <div class="type-item__info">
            <span class="type-item__label">{{ item.label }}</span>
            <span class="type-item__count">共 {{ item.count }} 条记录</span>
          </div>
          <span class="type-item__arrow">→</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.welcome-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 22px 26px;
  border-radius: 12px;
  background: linear-gradient(135deg, #ffffff 0%, #f5f3ff 100%);
  border: 1px solid rgba(91, 75, 232, 0.14);
  flex-wrap: wrap;
}

.welcome-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--a-text);
  margin: 0 0 6px;
}

.welcome-sub {
  font-size: 13px;
  color: var(--a-text-muted);
  margin: 0;
}

.welcome-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.metric-card {
  background: #ffffff;
  border: 1px solid var(--a-border);
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 28px rgba(16, 16, 20, 0.06);
  border-color: rgba(91, 75, 232, 0.35);
}

.metric-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.metric-card__label {
  font-size: 13px;
  font-weight: 600;
  color: var(--a-text-muted);
}

.metric-card__badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 999px;
  background: #ecfdf5;
  color: #059669;
  font-weight: 600;
}

.metric-card__badge--draft {
  background: #fffbeb;
  color: #d97706;
}

.metric-card__badge--leads {
  background: #fef2f2;
  color: #dc2626;
}

.metric-card__value {
  font-size: 32px;
  font-weight: 700;
  color: var(--a-text);
  margin: 0 0 6px;
}

.metric-card__tip {
  font-size: 12px;
  color: var(--a-text-muted);
  margin: 0;
}

.section-card {
  border-radius: 12px;
}

.card-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  font-weight: 600;
}

.card-head__desc {
  font-size: 12px;
  font-weight: 400;
  color: var(--a-text-muted);
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.quick-btn {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  padding: 16px;
  border-radius: 10px;
  border: 1px solid var(--a-border);
  background: #fafbfc;
  cursor: pointer;
  text-align: left;
  font-family: inherit;
  transition: all 0.2s ease;
}

.quick-btn:hover {
  background: #f5f3ff;
  border-color: var(--a-brand);
}

.quick-btn__icon {
  font-size: 22px;
}

.quick-btn__title {
  font-size: 14px;
  font-weight: 600;
  color: var(--a-text);
}

.quick-btn__sub {
  font-size: 12px;
  color: var(--a-text-muted);
}

.type-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

.type-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid var(--a-border);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.type-item:hover {
  border-color: var(--a-brand);
  background: #faf9ff;
}

.type-item__icon {
  font-size: 22px;
}

.type-item__info {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.type-item__label {
  font-size: 14px;
  font-weight: 600;
}

.type-item__count {
  font-size: 12px;
  color: var(--a-text-muted);
}

.type-item__arrow {
  color: var(--a-brand);
  font-weight: 600;
}
</style>
```

### `website/admin/src/views/UsersView.vue`

```vue
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { ApiError, get, request } from '@/api/http'
import { useAuth } from '@/stores/auth'

interface UserRow {
  id: string
  username: string
  displayName: string
  role: 'ADMIN' | 'EDITOR'
  enabled: boolean
  lockVersion: number
  lastLoginAt?: string
}

const { current } = useAuth()
const loading = ref(false)
const rows = ref<UserRow[]>([])
const total = ref(0)
const query = reactive({ keyword: '', page: 1, pageSize: 10 })

const formVisible = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const formRef = ref<FormInstance>()
const submitting = ref(false)
const editing = reactive({
  id: '',
  lockVersion: 0,
  username: '',
  displayName: '',
  roleCode: 'EDITOR' as 'ADMIN' | 'EDITOR',
  enabled: true,
  initialPassword: '',
})

const original = reactive({ enabled: true, roleCode: 'EDITOR' as 'ADMIN' | 'EDITOR' })

const rules: FormRules = {
  username: [
    { required: true, message: '请输入登录账号', trigger: 'blur' },
    {
      pattern: /^[A-Za-z0-9_-]{4,50}$/,
      message: '账号需为 4~50 位英文字母、数字、下划线或连字符',
      trigger: 'blur',
    },
  ],
  displayName: [{ required: true, message: '请输入显示名称', trigger: 'blur' }],
  initialPassword: [
    {
      validator: (_rule, value, callback) => {
        if (formMode.value === 'create') {
          if (!value || String(value).length < 12) {
            callback(new Error('新建账号初始密码至少 12 位'))
            return
          }
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
}

function formatTime(value?: string | null) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

async function load() {
  loading.value = true
  try {
    const data = await get<{ items: UserRow[]; total: number }>(
      `/api/admin/users?page=${query.page}&pageSize=${query.pageSize}&keyword=${encodeURIComponent(query.keyword)}`,
    )
    rows.value = data.items
    total.value = data.total
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '账号列表加载失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  formMode.value = 'create'
  Object.assign(editing, {
    id: '',
    lockVersion: 0,
    username: '',
    displayName: '',
    roleCode: 'EDITOR',
    enabled: true,
    initialPassword: '',
  })
  Object.assign(original, { enabled: true, roleCode: 'EDITOR' })
  formVisible.value = true
}

function openEdit(row: UserRow) {
  formMode.value = 'edit'
  Object.assign(editing, {
    id: row.id,
    lockVersion: row.lockVersion,
    username: row.username,
    displayName: row.displayName,
    roleCode: row.role,
    enabled: row.enabled,
    initialPassword: '',
  })
  Object.assign(original, { enabled: row.enabled, roleCode: row.role })
  formVisible.value = true
}

async function confirmSensitiveChange() {
  if (formMode.value !== 'edit') return true
  if (original.enabled && !editing.enabled) {
    try {
      await ElMessageBox.confirm(
        `禁用后「${editing.displayName || editing.username}」将立即退出登录，且无法再进入后台。确认禁用？`,
        '禁用账号确认',
        { type: 'warning', confirmButtonText: '确认禁用', cancelButtonText: '取消' },
      )
    } catch {
      return false
    }
  }
  if (original.roleCode === 'ADMIN' && editing.roleCode === 'EDITOR') {
    try {
      await ElMessageBox.confirm(
        `降级后「${editing.displayName || editing.username}」将失去发布、下线、删除与咨询处理权限。确认降级？`,
        '角色变更确认',
        { type: 'warning', confirmButtonText: '确认降级', cancelButtonText: '取消' },
      )
    } catch {
      return false
    }
  }
  return true
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (!(await confirmSensitiveChange())) return
    submitting.value = true
    try {
      if (formMode.value === 'create') {
        await request('/api/admin/users', {
          method: 'POST',
          body: {
            username: editing.username.trim(),
            displayName: editing.displayName.trim(),
            roleCode: editing.roleCode,
            initialPassword: editing.initialPassword,
          },
        })
        ElMessage.success('账号新建成功')
      } else {
        await request(`/api/admin/users/${editing.id}`, {
          method: 'PUT',
          body: {
            expectedLockVersion: editing.lockVersion,
            displayName: editing.displayName.trim(),
            roleCode: editing.roleCode,
            enabled: editing.enabled,
          },
        })
        ElMessage.success('账号更新成功')
      }
      formVisible.value = false
      await load()
    } catch (error) {
      ElMessage.error(error instanceof ApiError ? error.message : '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

async function resetPassword(row: UserRow) {
  try {
    const { value: newPassword } = await ElMessageBox.prompt(
      `请为「${row.displayName || row.username}」输入新密码（建议至少 12 位），重置后该账号所有在线会话将立即失效：`,
      '重置密码',
      {
        inputType: 'password',
        inputPlaceholder: '请输入至少 12 位新密码',
        inputValidator: (val) => {
          if (!val || val.length < 6) return '密码长度至少 6 位（建议 12 位以上）'
          if (val.length > 128) return '密码长度不能超过 128 位'
          return true
        },
        confirmButtonText: '确认重置',
        cancelButtonText: '取消',
      },
    )
    await request(`/api/admin/users/${row.id}/reset-password`, {
      method: 'POST',
      body: {
        expectedLockVersion: row.lockVersion,
        newPassword,
      },
    })
    ElMessage.success('密码已重置，该账号原有会话已全部撤销')
    await load()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error instanceof ApiError ? error.message : '重置失败')
    }
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div class="head">
          <span>后台账号管理</span>
          <div class="actions">
            <el-input
              v-model="query.keyword"
              placeholder="搜索账号或显示名称"
              clearable
              style="width: 220px"
              @keyup.enter="query.page = 1; load()"
            />
            <el-button @click="query.page = 1; load()">查询</el-button>
            <el-button type="primary" @click="openCreate">新建账号</el-button>
          </div>
        </div>
      </template>

      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="账号不提供物理删除以保留操作审计关联；禁用、降级或重置密码会立即撤销该账号所有登录会话。"
        class="tip"
      />

      <el-table v-loading="loading" :data="rows" border>
        <el-table-column prop="username" label="登录账号" min-width="140" />
        <el-table-column prop="displayName" label="显示名称" min-width="140" />
        <el-table-column label="角色" width="130">
          <template #default="{ row }">
            <el-tag size="small" :type="row.role === 'ADMIN' ? 'danger' : 'primary'">
              {{ row.role === 'ADMIN' ? 'ADMIN 管理员' : 'EDITOR 编辑' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? '启用' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最后登录时间" width="170">
          <template #default="{ row }">{{ formatTime(row.lastLoginAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="resetPassword(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pager"
        layout="total, prev, pager, next"
        :total="total"
        :page-size="query.pageSize"
        :current-page="query.page"
        @current-change="(p: number) => { query.page = p; load() }"
      />
    </el-card>

    <el-dialog v-model="formVisible" :title="formMode === 'create' ? '新建后台账号' : '编辑后台账号'" width="460px">
      <el-form ref="formRef" :model="editing" :rules="rules" label-width="96px">
        <el-form-item label="登录账号" prop="username">
          <el-input
            v-model="editing.username"
            :disabled="formMode === 'edit'"
            maxlength="50"
            placeholder="4~50 位字母、数字、_ 或 -"
          />
        </el-form-item>
        <el-form-item label="显示名称" prop="displayName">
          <el-input v-model="editing.displayName" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="角色权限" required>
          <el-select v-model="editing.roleCode" style="width: 100%">
            <el-option label="EDITOR 内容编辑（保存草稿、上传素材、预览）" value="EDITOR" />
            <el-option label="ADMIN 系统管理员（发布、下线、管理线索与账号）" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="formMode === 'create'" label="初始密码" prop="initialPassword" required>
          <el-input
            v-model="editing.initialPassword"
            type="password"
            show-password
            placeholder="建议至少 12 位，最长 128 位"
          />
        </el-form-item>
        <el-form-item v-if="formMode === 'edit'" label="启用状态">
          <el-switch v-model="editing.enabled" :disabled="editing.id === current?.id" />
          <span v-if="editing.id === current?.id" class="self-hint">不能禁用当前登录账号自身</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.actions {
  display: flex;
  gap: 8px;
}

.tip {
  margin-bottom: 14px;
}

.self-hint {
  margin-left: 10px;
  font-size: 12px;
  color: var(--a-text-muted);
}

.pager {
  margin-top: 14px;
  justify-content: flex-end;
}
</style>
```

### `website/admin/src/views/CategoryView.vue`

```vue
<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ApiError } from '@/api/http'
import {
  createCategory,
  deleteCategory,
  listCategories,
  offlineCategory,
  publishCategory,
  saveCategory,
  type CategoryView,
} from '@/api/content'
import { useAuth } from '@/stores/auth'

const { current } = useAuth()
const isAdmin = computed(() => current.value?.role === 'ADMIN')

const types = [
  { value: 'PRODUCT', label: '产品分类' },
  { value: 'SOLUTION', label: '解决方案分类' },
  { value: 'CASE', label: '客户案例分类' },
  { value: 'NEWS', label: '新闻分类' },
]

const loading = ref(false)
const rows = ref<CategoryView[]>([])
const total = ref(0)
const query = reactive({ type: 'PRODUCT', page: 1, pageSize: 20 })

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const saving = ref(false)
const form = reactive({ id: '', lockVersion: 0, name: '', sortOrder: 100 })

async function load() {
  loading.value = true
  try {
    const data = await listCategories({
      categoryType: query.type,
      page: query.page,
      pageSize: query.pageSize,
    })
    rows.value = data.items
    total.value = data.total
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '分类加载失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  dialogMode.value = 'create'
  Object.assign(form, { id: '', lockVersion: 0, name: '', sortOrder: 100 })
  dialogVisible.value = true
}

function openEdit(row: CategoryView) {
  dialogMode.value = 'edit'
  Object.assign(form, { id: row.id, lockVersion: row.lockVersion, name: row.name, sortOrder: row.sortOrder })
  dialogVisible.value = true
}

async function submit() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  saving.value = true
  try {
    if (dialogMode.value === 'create') {
      await createCategory(query.type, form.name.trim(), form.sortOrder)
      ElMessage.success('已创建分类草稿')
    } else {
      await saveCategory(form.id, form.name.trim(), form.sortOrder, form.lockVersion)
      ElMessage.success('分类草稿已保存')
    }
    dialogVisible.value = false
    await load()
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '保存失败')
  } finally {
    saving.value = false
  }
}

async function publish(row: CategoryView) {
  try {
    await ElMessageBox.confirm(`发布分类「${row.name}」后，前台筛选会立即出现该分类。`, '确认发布', {
      confirmButtonText: '发布',
      cancelButtonText: '取消',
    })
    await publishCategory(row.id, row.lockVersion, row.draftVersion)
    ElMessage.success('已发布')
    await load()
  } catch (error) {
    if (error instanceof ApiError) ElMessage.error(error.message)
  }
}

async function offline(row: CategoryView) {
  try {
    await ElMessageBox.confirm('该分类下仍有已发布内容时无法下线。确认下线？', '确认下线', {
      confirmButtonText: '下线',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await offlineCategory(row.id, row.lockVersion)
    ElMessage.success('已下线')
    await load()
  } catch (error) {
    if (error instanceof ApiError) ElMessage.error(error.message)
  }
}

async function remove(row: CategoryView) {
  try {
    await ElMessageBox.confirm('删除后不可恢复；仍被内容引用时会被拒绝。确认删除？', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteCategory(row.id, row.lockVersion)
    ElMessage.success('已删除')
    await load()
  } catch (error) {
    if (error instanceof ApiError) ElMessage.error(error.message)
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div class="head">
          <span>内容分类</span>
          <div class="actions">
            <el-select v-model="query.type" style="width: 150px" @change="query.page = 1; load()">
              <el-option v-for="item in types" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-button @click="load">刷新</el-button>
            <el-button type="primary" @click="openCreate">新建分类</el-button>
          </div>
        </div>
      </template>

      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="分类是单层的，管理员发布后才会出现在前台筛选里。"
        class="tip"
      />

      <el-table v-loading="loading" :data="rows" border>
        <el-table-column prop="name" label="草稿名称" min-width="180">
          <template #default="{ row }">
            <div class="name-cell">
              <span>{{ row.name }}</span>
              <el-tag v-if="row.hasUnpublishedChanges" size="small" type="warning">有未发布修改</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 'PUBLISHED' ? 'success' : row.status === 'OFFLINE' ? 'warning' : 'info'">
              {{ row.status === 'PUBLISHED' ? '已发布' : row.status === 'OFFLINE' ? '已下线' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="线上名称" width="160">
          <template #default="{ row }">{{ row.publishedName || '-' }}</template>
        </el-table-column>
        <el-table-column label="排序" width="90">
          <template #default="{ row }">{{ row.publishedSortOrder ?? row.sortOrder }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <template v-if="isAdmin">
              <el-button
                v-if="row.hasUnpublishedChanges || row.status === 'OFFLINE'"
                link
                type="success"
                @click="publish(row)"
              >
                {{ row.status === 'OFFLINE' ? '重新发布' : '发布' }}
              </el-button>
              <el-button v-if="row.status === 'PUBLISHED'" link type="warning" @click="offline(row)">下线</el-button>
              <el-button link type="danger" @click="remove(row)">删除</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pager"
        layout="total, prev, pager, next"
        :total="total"
        :page-size="query.pageSize"
        :current-page="query.page"
        @current-change="(p: number) => { query.page = p; load() }"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新建分类' : '编辑分类'" width="420px">
      <el-form label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存草稿</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.actions {
  display: flex;
  gap: 8px;
}

.tip {
  margin-bottom: 14px;
}

.name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pager {
  margin-top: 14px;
  justify-content: flex-end;
}
</style>
```

### `website/server/src/main/java/com/company/cms/dashboard/DashboardService.java`

```java
package com.company.cms.dashboard;

import com.company.cms.system.RoleCode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作台数据。按角色裁剪：编辑只能看到内容相关统计，不接触客户线索与账号。
 */
@Service
public class DashboardService {

    private final JdbcTemplate jdbc;

    public DashboardService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public DashboardResponse load(RoleCode role) {
        Map<String, Long> byType = new LinkedHashMap<>();
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT content_type, COUNT(*) AS total
                FROM cms_content
                WHERE deleted = 0
                GROUP BY content_type
                ORDER BY content_type
                """);
        for (Map<String, Object> row : rows) {
            byType.put(String.valueOf(row.get("content_type")),
                    ((Number) row.get("total")).longValue());
        }

        long published = count("""
                SELECT COUNT(*) FROM cms_content
                WHERE deleted = 0 AND publication_status = 'PUBLISHED'
                """);
        // 包含未发布草稿及已发布但存在未发布修改的内容（对齐文档 5.1 hasUnpublishedChanges 口径）
        long drafts = count("""
                SELECT COUNT(*) FROM cms_content
                WHERE deleted = 0
                  AND (published_version IS NULL OR draft_version <> published_version)
                """);

        if (role != RoleCode.ADMIN) {
            return new DashboardResponse(published, drafts, byType, null, null);
        }

        long pendingLeads = count("SELECT COUNT(*) FROM cms_lead WHERE status = 'NEW'");
        long userCount = count("SELECT COUNT(*) FROM sys_user WHERE enabled = 1");
        return new DashboardResponse(published, drafts, byType, pendingLeads, userCount);
    }

    private long count(String sql) {
        Long value = jdbc.queryForObject(sql, Long.class);
        return value == null ? 0L : value;
    }
}
```

### `website/web/src/components/ConsultForm.vue`

```vue
<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ApiError } from '@/api/http'
import { submitLead } from '@/api/public'
import { loadConsultContext } from '@/composables/consult'

const props = defineProps<{
  sourcePosition: string
  contentType?: string
  contentId?: string
  initialDemand?: string
}>()

const route = useRoute()
const uid = 'consult-' + Math.random().toString(36).slice(2, 8)

const context = reactive({
  loaded: false,
  formEnabled: true,
  privacyVersion: 1 as number | null,
  successText: '',
})

const form = reactive({
  name: '',
  companyName: '',
  phone: '',
  demand: props.initialDemand || '',
  consent: false,
})

watch(
  () => props.initialDemand,
  (val) => {
    if (val) {
      form.demand = val
    }
  },
)

const errors = reactive<Record<string, string>>({})
const submitting = ref(false)
const attempted = ref(false)
const succeeded = ref(false)
const receiptNo = ref('')
const submitError = ref('')
const idempotencyKey = ref(newKey())

function newKey(): string {
  return globalThis.crypto?.randomUUID
    ? globalThis.crypto.randomUUID()
    : 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
        const r = (Math.random() * 16) | 0
        const v = c === 'x' ? r : (r & 0x3) | 0x8
        return v.toString(16)
      })
}

onMounted(async () => {
  try {
    const data = await loadConsultContext()
    context.loaded = true
    context.formEnabled = data.formEnabled
    context.privacyVersion = data.privacyVersion ?? 1
    context.successText = data.successText
  } catch {
    context.loaded = true
    context.formEnabled = true
  }
})

function validate(): boolean {
  Object.keys(errors).forEach((k) => delete errors[k])

  if (!form.name.trim()) {
    errors.name = '请填写您的姓名'
  }
  if (!form.companyName.trim()) {
    errors.companyName = '请填写公司名称'
  }
  if (!form.phone.trim()) {
    errors.phone = '请填写联系电话'
  } else if (!/^1[3-9]\d{9}$/.test(form.phone.trim())) {
    errors.phone = '请输入有效的 11 位中国大陆手机号'
  }
  if (!form.demand.trim()) {
    errors.demand = '请简要描述您的咨询需求'
  }
  if (!form.consent) {
    errors.consent = '请阅读并勾选同意《隐私说明》'
  }

  return Object.keys(errors).length === 0
}

watch(
  () => [form.name, form.companyName, form.phone, form.demand, form.consent],
  () => {
    if (attempted.value) validate()
  },
)

function resetForNext() {
  form.name = ''
  form.companyName = ''
  form.phone = ''
  form.demand = ''
  form.consent = false
  attempted.value = false
  succeeded.value = false
  receiptNo.value = ''
  idempotencyKey.value = newKey()
}

async function handleSubmit() {
  attempted.value = true
  submitError.value = ''
  if (!validate()) return

  submitting.value = true
  try {
    const res = await submitLead(
      {
        name: form.name.trim(),
        phone: form.phone.trim(),
        companyName: form.companyName.trim(),
        demand: form.demand.trim(),
        sourcePath: route.path || '/',
        sourcePosition: props.sourcePosition || 'OTHER_CTA',
        contentType: props.contentType,
        contentId: props.contentId ? Number(props.contentId) : undefined,
        consent: true,
        consentVersion: context.privacyVersion ?? 1,
      },
      idempotencyKey.value,
    )
    succeeded.value = true
    receiptNo.value = res.receiptNo
  } catch (e) {
    if (e instanceof ApiError) {
      submitError.value = e.message
    } else {
      submitError.value = '网络异常，提交未成功（您的输入已保留，可直接重试）'
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="consult-form-wrap">
    <div v-if="succeeded" class="success-state" role="status">
      <div class="success-icon" aria-hidden="true">✓</div>
      <h3 class="success-title">咨询提交成功</h3>
      <p class="success-desc">{{ context.successText || '已收到您的咨询，我们的专家顾问将尽快与您联系。' }}</p>
      <p v-if="receiptNo" class="receipt-tag">受理回执单号：<strong>{{ receiptNo }}</strong></p>
      <button type="button" class="reset-btn" @click="resetForNext">继续提交新咨询</button>
    </div>

    <div v-else-if="context.loaded && !context.formEnabled" class="closed-state">
      <p>在线咨询通道暂未开启，请直接通过联系我们页面的电话或邮箱与我们联系。</p>
      <RouterLink to="/contact" class="reset-btn">前往联系我们 →</RouterLink>
    </div>

    <form v-else class="consult-form" novalidate @submit.prevent="handleSubmit">
      <div class="form-row">
        <div class="form-item" :class="{ 'has-error': errors.name }">
          <label :for="`${uid}-name`" class="form-label">姓名 <span>*</span></label>
          <input
            :id="`${uid}-name`"
            v-model="form.name"
            type="text"
            maxlength="50"
            class="form-input"
            placeholder="您的称呼"
            :aria-invalid="Boolean(errors.name)"
          />
          <span v-if="errors.name" class="error-msg" role="alert">{{ errors.name }}</span>
        </div>

        <div class="form-item" :class="{ 'has-error': errors.phone }">
          <label :for="`${uid}-phone`" class="form-label">手机号码 <span>*</span></label>
          <input
            :id="`${uid}-phone`"
            v-model="form.phone"
            type="tel"
            maxlength="11"
            class="form-input"
            placeholder="11 位手机号"
            :aria-invalid="Boolean(errors.phone)"
          />
          <span v-if="errors.phone" class="error-msg" role="alert">{{ errors.phone }}</span>
        </div>
      </div>

      <div class="form-item" :class="{ 'has-error': errors.companyName }">
        <label :for="`${uid}-company`" class="form-label">公司名称 <span>*</span></label>
        <input
          :id="`${uid}-company`"
          v-model="form.companyName"
          type="text"
          maxlength="100"
          class="form-input"
          placeholder="请输入贵公司全称或简称"
          :aria-invalid="Boolean(errors.companyName)"
        />
        <span v-if="errors.companyName" class="error-msg" role="alert">{{ errors.companyName }}</span>
      </div>

      <div class="form-item" :class="{ 'has-error': errors.demand }">
        <label :for="`${uid}-demand`" class="form-label">咨询需求 <span>*</span></label>
        <textarea
          :id="`${uid}-demand`"
          v-model="form.demand"
          rows="3"
          maxlength="2000"
          class="form-textarea"
          placeholder="请描述您的车队规模、业务场景或希望了解的产品方案..."
          :aria-invalid="Boolean(errors.demand)"
        ></textarea>
        <span v-if="errors.demand" class="error-msg" role="alert">{{ errors.demand }}</span>
      </div>

      <div class="form-item" :class="{ 'has-error': errors.consent }">
        <label class="checkbox-label">
          <input v-model="form.consent" type="checkbox" class="form-checkbox" />
          <span>
            我已阅读并同意
            <RouterLink to="/privacy" target="_blank">《隐私说明》</RouterLink>
          </span>
        </label>
        <span v-if="errors.consent" class="error-msg" role="alert">{{ errors.consent }}</span>
      </div>

      <div v-if="submitError" class="submit-error" role="alert">{{ submitError }}</div>

      <button type="submit" class="submit-btn" :disabled="submitting">
        {{ submitting ? '正在加密提交...' : '立即预约专家演示' }}
      </button>
    </form>
  </div>
</template>

<style scoped>
.consult-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

@media (max-width: 520px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: #101014;
}

.form-label span {
  color: #ef4444;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid rgba(16, 16, 20, 0.16);
  border-radius: 10px;
  font-size: 14px;
  color: #101014;
  background: #ffffff;
  font-family: inherit;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #5b4be8;
  box-shadow: 0 0 0 3px #eeebff;
}

.has-error .form-input,
.has-error .form-textarea {
  border-color: #ef4444;
}

.error-msg {
  font-size: 12px;
  color: #ef4444;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12.5px;
  color: #5f6470;
  cursor: pointer;
}

.checkbox-label a {
  color: #5b4be8;
  text-decoration: underline;
}

.form-checkbox {
  width: 15px;
  height: 15px;
  accent-color: #5b4be8;
}

.submit-error {
  padding: 10px 12px;
  border-radius: 8px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #991b1b;
  font-size: 12.5px;
}

.submit-btn {
  height: 44px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #6d5ce8 0%, #5b4be8 100%);
  color: #ffffff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 8px 20px rgba(91, 75, 232, 0.3);
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
}

.submit-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.success-state,
.closed-state {
  text-align: center;
  padding: 28px 16px;
  background: #f8f9fc;
  border-radius: 14px;
}

.success-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #ecfdf5;
  color: #10b981;
  font-size: 24px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}

.success-title {
  margin: 0 0 8px;
  font-size: 17px;
  color: #101014;
}

.success-desc {
  margin: 0 0 14px;
  font-size: 13.5px;
  color: #5f6470;
}

.receipt-tag {
  font-size: 12.5px;
  color: #5f6470;
  background: #ffffff;
  padding: 6px 14px;
  border-radius: 999px;
  display: inline-block;
  margin-bottom: 16px;
  border: 1px solid rgba(16, 16, 20, 0.08);
}

.reset-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 18px;
  border-radius: 999px;
  border: 1px solid #5b4be8;
  color: #5b4be8;
  background: #ffffff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}
</style>
```

## 四、新增「全站实景交互预演与全局可视化编辑台 (Visual Live Studio)」详解

### 1. 设计目标与双向同步机制
- **所见即所得交互编辑（取代原只读 `/preview/home`）**：工作人员在后台左侧菜单或顶部点击 **「🎨 实景预演交互编辑」** 即可进入全站可视化画布。
- **跨四大模块一次性联动同步**：画布同时加载并绑定 `SITE`（全站设置/Logo/页脚）、`NAVIGATION`（顶部主导航 + 5 列全景下拉大菜单 `megaColumns`）、`HOME`（首屏轮播、企业指标、产品与解决方案分组、客户案例、客户标识墙、新闻动态、底部咨询区及模块上下摆放顺序 `sectionOrder`）与 `CONTACT`（全国服务热线、邮箱、地址、服务时间、全站咨询表单开关）。
- **传统表单 ↔ 可视化预演自由切换**：点击顶部 **「💾 保存全局草稿」** 后，所有改动按各自 `lockVersion` 同步写入后端 `/api/admin/configs/{key}/draft`。此时回到后台任意传统表单页（如「全站设置」「导航」「首页管理」「联系我们」），所有数据均已实时同步，工作人员可自由选择「画布直观编排」或「传统表单精细维护」。

### 2. 核心交互与新增实用能力一览
- **模块自由摆放与显隐控制**：支持在左侧 **「全局模块与摆放顺序树」** 或画布每个模块右上角的悬浮工具条中一键 `⬆️ 上移模块` / `⬇️ 下移模块`、`👁️ 开启/关闭模块`。
- **画布级文字、图片/视频、链接与关联内容直改**：
  - **文字直改**：画布上的站点名、导航名、首屏主副标题、指标数字/单位/标签、分组标题/说明、页脚热线/邮箱/地址/备案号均可直接点击输入。
  - **图片与视频即点即换**：点击 Logo、首屏背景或服务分组配图，直接弹出 `MediaPicker` 选择或上传图片/MP4/WebM 视频，画布即时渲染播放。
  - **跳转链接可视化绑定**：点击任意按钮、导航项或页脚链接旁的 `🔗` 标识，即弹出 `LinkEditor`，可绑定站内页面、具体产品/方案/案例/新闻详情、外部 URL、首页锚点或唤起咨询弹窗。
  - **关联内容卡片可视化挑选**：在产品服务分组、客户案例、客户标识墙、手选新闻区直接内嵌 `ContentPicker`，支持搜索、勾选与排序已发布内容并实时渲染卡片预览。
- **实用增强工具箱**：
  - **多视口响应式实时切换**：顶部一键切换 `🖥️ 桌面 (1440px)`、`📟 平板 (768px)`、`📱 手机 (390px)`，手机视口下自动预览 `mobileMediaId` 手机端首屏素材。
  - **30 步撤销 / 重做栈 (`Ctrl+Z` / `Ctrl+Y`)**：支持随时回退或重做画布上的任意结构、顺序或文案修改。
  - **三态对比切换 (`✏️ 交互编辑` / `👁️ 纯净预演` / `🌐 线上已发布版`)**：一键隐藏编辑辅助框体验真实访客视角，或切换查看当前线上已发布快照进行对比。
  - **🛡️ 发布前智能合规体检抽屉**：实时检测空标题、未选首屏背景图、未配对的 ICP 备案号/链接等阻塞问题，点击问题条目即可自动滚动定位至对应画布模块。

### `website/admin/src/views/PreviewHomeView.vue`

```vue
<script setup lang="ts">
/**
 * 全站实景交互预演与全局可视化编辑台 (Visual Live Studio)
 *
 * 核心能力：
 * 1. 全局跨模块双向同步：同时加载并可视化编辑 SITE（全站设置/页脚）、NAVIGATION（顶部导航与5列全景菜单）、
 *    HOME（首屏、指标、服务分组、案例、客户墙、新闻、咨询区及模块摆放顺序 sectionOrder）、CONTACT（热线/邮箱/地址/表单开关）。
 * 2. 画布级所见即所得直改：文字直接点击修改、图片/视频点击唤起 MediaPicker 即时替换、按钮/导航链接点击唤起 LinkEditor、
 *    关联产品/方案/案例/客户/新闻点击唤起 ContentPicker。
 * 3. 模块自由摆放与增删：支持在画布悬浮条或左侧大纲树中上下移动模块顺序、开关模块显隐、增删改查各模块子条目。
 * 4. 实用增强工具：桌面(100%)/平板(768px)/手机(390px)多视口实时切换、30步 Undo/Redo 撤销重做栈、
 *    「交互编辑态 / 纯净预演态 / 线上已发布版」三态切换、发布前智能合规体检抽屉、一键全局保存草稿与一键全局发布。
 */
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import ContentPicker from '@/components/ContentPicker.vue'
import LinkEditor, { type LinkValue } from '@/components/LinkEditor.vue'
import MediaPicker from '@/components/MediaPicker.vue'
import { getMedia, listContents, type ContentView, type MediaItem } from '@/api/content'
import { ApiError } from '@/api/http'
import { getConfig, publishConfig, saveConfigDraft, type ConfigKey, type ConfigView } from '@/api/site'
import { useAuth } from '@/stores/auth'

const router = useRouter()
const { current } = useAuth()
const isAdmin = computed(() => current.value?.role === 'ADMIN')

// ==================== 类型定义 ====================
interface FooterLink {
  key?: string
  label: string
  enabled: boolean
  sortOrder: number
  link: LinkValue | null
}
interface FooterGroup {
  title: string
  links: FooterLink[]
}
interface SiteDraft {
  siteName: string
  slogan: string
  copyrightText: string
  icpText: string
  icpUrl: string
  policeText: string
  policeUrl: string
  logoMediaId?: number
  logoAlt: string
  faviconMediaId?: number
  defaultSeo: { title: string; description: string }
  footerGroups: FooterGroup[]
}

interface MegaSubItem {
  label: string
  path: string
  tag?: string
  isVideo?: boolean
  videoUrl?: string
  videoTitle?: string
}
interface MegaColumn {
  title: string
  items: MegaSubItem[]
}
interface NavNode {
  key?: string
  label: string
  enabled: boolean
  sortOrder: number
  link: LinkValue | null
  children?: NavNode[]
}
interface NavigationDraft {
  items: NavNode[]
  megaColumns?: MegaColumn[]
  megaCta?: { text: string; linkText: string }
}

interface HeroItem {
  key?: string
  enabled: boolean
  sortOrder: number
  title: string
  subtitle: string
  desktopMediaId?: number
  mobileMediaId?: number
  imageAlt: string
  buttonText: string
  link: LinkValue | null
}
interface MetricItem {
  key?: string
  sortOrder: number
  displayValue: string
  unit: string
  label: string
  description: string
}
interface ServiceGroup {
  key?: string
  sortOrder: number
  title: string
  description: string
  imageMediaId?: number
  contentType: string
  contentIds: number[]
  moreLink: LinkValue | null
}
interface BlockShape {
  enabled: boolean
  title: string
  subtitle: string
}
export type HomeSectionKey = 'metrics' | 'services' | 'cases' | 'customers' | 'news' | 'consultation'
interface HomeDraft {
  sectionOrder: HomeSectionKey[]
  hero: { items: HeroItem[] }
  metrics: BlockShape & { items: MetricItem[] }
  services: BlockShape & { groups: ServiceGroup[] }
  cases: BlockShape & { contentIds: number[] }
  customers: BlockShape & { contentIds: number[] }
  news: BlockShape & { mode: string; limit: number; contentIds: number[] }
  consultation: { title: string; subtitle: string; buttonText: string; link: LinkValue | null }
}

interface ContactDraft {
  title: string
  description: string
  successText: string
  formEnabled: boolean
  phone: string
  email: string
  address: string
  hours: string
}

const DEFAULT_SECTION_ORDER: HomeSectionKey[] = [
  'metrics',
  'services',
  'cases',
  'customers',
  'news',
  'consultation',
]

const SECTION_META: Record<HomeSectionKey, { label: string; icon: string; formPath: string }> = {
  metrics: { label: '企业实力指标', icon: '📊', formPath: '/home' },
  services: { label: '产品与解决方案', icon: '📦', formPath: '/home' },
  cases: { label: '标杆客户案例', icon: '🏆', formPath: '/home' },
  customers: { label: '客户标识 Logo 墙', icon: '🤝', formPath: '/home' },
  news: { label: '新闻动态资讯', icon: '📰', formPath: '/home' },
  consultation: { label: '底部咨询行动区', icon: '💬', formPath: '/home' },
}

// ==================== 全局状态与配置视图 ====================
const loading = ref(true)
const savingAll = ref(false)
const publishingAll = ref(false)

const studioMode = ref<'edit' | 'preview' | 'published'>('edit')
const viewport = ref<'desktop' | 'tablet' | 'mobile'>('desktop')
const showLeftOutline = ref(true)
const showRightInspector = ref(true)
const showDisabledBlocks = ref(true)
const healthDrawerOpen = ref(false)

const heroIndex = ref(0)
const activeServiceGroup = ref(0)
const openMegaPreview = ref(false)
const selectedSection = ref<'header' | 'hero' | HomeSectionKey | 'footer'>('hero')

const configViews = reactive<{
  SITE: ConfigView<SiteDraft> | null
  NAVIGATION: ConfigView<NavigationDraft> | null
  HOME: ConfigView<HomeDraft> | null
  CONTACT: ConfigView<ContactDraft> | null
}>({
  SITE: null,
  NAVIGATION: null,
  HOME: null,
  CONTACT: null,
})

// 实时可编辑草稿副本
const drafts = reactive<{
  SITE: SiteDraft
  NAVIGATION: NavigationDraft
  HOME: HomeDraft
  CONTACT: ContactDraft
}>({
  SITE: {
    siteName: 'G7易流',
    slogan: '领先的物联网与数字化软件服务商',
    copyrightText: '© 2026 示例科技有限公司',
    icpText: '',
    icpUrl: '',
    policeText: '',
    policeUrl: '',
    logoMediaId: undefined,
    logoAlt: '',
    faviconMediaId: undefined,
    defaultSeo: { title: '企业官网', description: '面向企业的物联网与数字化平台' },
    footerGroups: [],
  },
  NAVIGATION: {
    items: [],
    megaColumns: [],
    megaCta: { text: '需要专业场景建议？', linkText: '联系技术专家' },
  },
  HOME: {
    sectionOrder: [...DEFAULT_SECTION_ORDER],
    hero: { items: [] },
    metrics: { enabled: true, title: '企业实力', subtitle: '', items: [] },
    services: { enabled: true, title: '产品与解决方案', subtitle: '', groups: [] },
    cases: { enabled: true, title: '客户案例', subtitle: '', contentIds: [] },
    customers: { enabled: true, title: '合作客户', subtitle: '', contentIds: [] },
    news: { enabled: true, title: '新闻动态', subtitle: '', mode: 'SELECTED', limit: 3, contentIds: [] },
    consultation: { title: '了解适合您的服务', subtitle: '', buttonText: '立即咨询', link: { type: 'CONSULT' } },
  },
  CONTACT: {
    title: '联系我们',
    description: '',
    successText: '已收到您的咨询，我们会尽快与您联系。',
    formEnabled: true,
    phone: '400-888-6688',
    email: 'connect@example.com',
    address: '北京市海淀区中关村南大街1号',
    hours: '周一至周日 09:00 - 18:00',
  },
})

// 初始快照基线（用于判断哪个模块被修改过）
const baselineJson = reactive<Record<ConfigKey, string>>({
  SITE: '',
  NAVIGATION: '',
  HOME: '',
  ABOUT: '',
  CONTACT: '',
  PRIVACY: '',
})

const dirtyKeys = computed<ConfigKey[]>(() => {
  const keys: ConfigKey[] = []
  ;(['SITE', 'NAVIGATION', 'HOME', 'CONTACT'] as ConfigKey[]).forEach((k) => {
    const currentStr = JSON.stringify(drafts[k as 'SITE' | 'NAVIGATION' | 'HOME' | 'CONTACT'])
    if (baselineJson[k] && currentStr !== baselineJson[k]) {
      keys.push(k)
    }
  })
  return keys
})

const unpublishedKeys = computed<ConfigKey[]>(() => {
  const keys: ConfigKey[] = []
  ;(['SITE', 'NAVIGATION', 'HOME', 'CONTACT'] as const).forEach((k) => {
    if (configViews[k]?.hasUnpublishedChanges || dirtyKeys.value.includes(k)) {
      keys.push(k)
    }
  })
  return keys
})

// ==================== Undo / Redo 历史栈 ====================
const historyStack = ref<string[]>([])
const historyPointer = ref(-1)
let suppressHistory = false

function recordSnapshot(label = '') {
  if (suppressHistory) return
  const snap = JSON.stringify(drafts)
  if (historyPointer.value >= 0 && historyStack.value[historyPointer.value] === snap) return
  historyStack.value = historyStack.value.slice(0, historyPointer.value + 1)
  historyStack.value.push(snap)
  if (historyStack.value.length > 30) {
    historyStack.value.shift()
  }
  historyPointer.value = historyStack.value.length - 1
  if (label) {
    // 轻量提示
  }
}

const canUndo = computed(() => historyPointer.value > 0)
const canRedo = computed(() => historyPointer.value < historyStack.value.length - 1)

function undo() {
  if (!canUndo.value) return
  suppressHistory = true
  historyPointer.value -= 1
  const parsed = JSON.parse(historyStack.value[historyPointer.value])
  Object.assign(drafts.SITE, parsed.SITE)
  Object.assign(drafts.NAVIGATION, parsed.NAVIGATION)
  Object.assign(drafts.HOME, parsed.HOME)
  Object.assign(drafts.CONTACT, parsed.CONTACT)
  suppressHistory = false
  ElMessage.info('已撤销上一步修改')
}

function redo() {
  if (!canRedo.value) return
  suppressHistory = true
  historyPointer.value += 1
  const parsed = JSON.parse(historyStack.value[historyPointer.value])
  Object.assign(drafts.SITE, parsed.SITE)
  Object.assign(drafts.NAVIGATION, parsed.NAVIGATION)
  Object.assign(drafts.HOME, parsed.HOME)
  Object.assign(drafts.CONTACT, parsed.CONTACT)
  suppressHistory = false
  ElMessage.info('已重做修改')
}

// ==================== 内容与媒体缓存 ====================
const contentCatalog = reactive<Record<string, ContentView>>({})
const latestNewsList = ref<ContentView[]>([])
const mediaCache = reactive<Record<number, MediaItem>>({})

async function loadCatalogAndMedia() {
  try {
    const types = ['PRODUCT', 'SOLUTION', 'CASE', 'NEWS', 'CUSTOMER']
    await Promise.all(
      types.map(async (t) => {
        const res = await listContents({
          contentType: t,
          publicationStatus: 'PUBLISHED',
          page: 1,
          pageSize: 50,
        })
        for (const item of res.items) {
          contentCatalog[String(item.id)] = item
        }
        if (t === 'NEWS') {
          latestNewsList.value = res.items
        }
      }),
    )
  } catch {
    // 忽略单个列表失败
  }
  await syncMediaCache()
}

async function syncMediaCache() {
  const ids = new Set<number>()
  if (drafts.SITE.logoMediaId) ids.add(Number(drafts.SITE.logoMediaId))
  if (drafts.SITE.faviconMediaId) ids.add(Number(drafts.SITE.faviconMediaId))
  for (const h of drafts.HOME.hero.items) {
    if (h.desktopMediaId) ids.add(Number(h.desktopMediaId))
    if (h.mobileMediaId) ids.add(Number(h.mobileMediaId))
  }
  for (const g of drafts.HOME.services.groups) {
    if (g.imageMediaId) ids.add(Number(g.imageMediaId))
  }
  for (const c of Object.values(contentCatalog)) {
    if (c.coverMediaId) ids.add(Number(c.coverMediaId))
  }
  await Promise.all(
    Array.from(ids)
      .filter((id) => id > 0 && !mediaCache[id])
      .map(async (id) => {
        try {
          mediaCache[id] = await getMedia(id)
        } catch {
          // 忽略已删除素材
        }
      }),
  )
}

function mediaUrl(id?: number | string | null): string {
  if (!id) return ''
  return `/api/admin/media/${id}/file`
}

function isVideoMedia(id?: number | string | null): boolean {
  if (!id) return false
  return mediaCache[Number(id)]?.mediaType === 'VIDEO'
}

// ==================== 数据加载与归一化 ====================
function normalizeSectionOrder(raw?: unknown): HomeSectionKey[] {
  const result: HomeSectionKey[] = []
  if (Array.isArray(raw)) {
    for (const item of raw) {
      const k = String(item) as HomeSectionKey
      if (DEFAULT_SECTION_ORDER.includes(k) && !result.includes(k)) {
        result.push(k)
      }
    }
  }
  for (const k of DEFAULT_SECTION_ORDER) {
    if (!result.includes(k)) result.push(k)
  }
  return result
}

function applySourceData(usePublished = false) {
  const siteSrc = (usePublished ? configViews.SITE?.published : configViews.SITE?.draft) as Partial<SiteDraft> | null
  if (siteSrc) {
    drafts.SITE = {
      siteName: siteSrc.siteName ?? 'G7易流',
      slogan: siteSrc.slogan ?? '领先的物联网与数字化软件服务商',
      copyrightText: siteSrc.copyrightText ?? '© 2026 示例科技有限公司',
      icpText: siteSrc.icpText ?? '',
      icpUrl: siteSrc.icpUrl ?? '',
      policeText: siteSrc.policeText ?? '',
      policeUrl: siteSrc.policeUrl ?? '',
      logoMediaId: siteSrc.logoMediaId ? Number(siteSrc.logoMediaId) : undefined,
      logoAlt: siteSrc.logoAlt ?? '',
      faviconMediaId: siteSrc.faviconMediaId ? Number(siteSrc.faviconMediaId) : undefined,
      defaultSeo: {
        title: siteSrc.defaultSeo?.title ?? '',
        description: siteSrc.defaultSeo?.description ?? '',
      },
      footerGroups: (siteSrc.footerGroups ?? []).map((g) => ({
        title: g.title ?? '',
        links: (g.links ?? []).map((l) => ({
          key: l.key,
          label: l.label ?? '',
          enabled: l.enabled ?? true,
          sortOrder: l.sortOrder ?? 100,
          link: l.link ?? { type: 'PAGE', pageCode: 'HOME' },
        })),
      })),
    }
  }

  const navSrc = (usePublished ? configViews.NAVIGATION?.published : configViews.NAVIGATION?.draft) as Partial<NavigationDraft> | null
  if (navSrc) {
    drafts.NAVIGATION = {
      items: (navSrc.items ?? []).map((n) => ({
        key: n.key,
        label: n.label ?? '',
        enabled: n.enabled ?? true,
        sortOrder: n.sortOrder ?? 100,
        link: n.link ?? { type: 'NONE' },
        children: (n.children ?? []).map((c) => ({
          key: c.key,
          label: c.label ?? '',
          enabled: c.enabled ?? true,
          sortOrder: c.sortOrder ?? 100,
          link: c.link ?? { type: 'PAGE', pageCode: 'HOME' },
        })),
      })),
      megaColumns: (navSrc.megaColumns ?? []).map((col) => ({
        title: col.title ?? '',
        items: (col.items ?? []).map((sub) => ({
          label: sub.label ?? '',
          path: sub.path ?? '/products',
          tag: sub.tag ?? '',
          isVideo: Boolean(sub.isVideo),
          videoUrl: sub.videoUrl ?? '',
          videoTitle: sub.videoTitle ?? '',
        })),
      })),
      megaCta: {
        text: navSrc.megaCta?.text ?? '需要专业场景建议？',
        linkText: navSrc.megaCta?.linkText ?? '联系技术专家',
      },
    }
    if (drafts.NAVIGATION.items.length === 0) {
      drafts.NAVIGATION.items = [
        { label: '首页', enabled: true, sortOrder: 10, link: { type: 'PAGE', pageCode: 'HOME' }, children: [] },
        { label: '产品及解决方案', enabled: true, sortOrder: 20, link: { type: 'PAGE', pageCode: 'PRODUCTS' }, children: [] },
        { label: '客户案例', enabled: true, sortOrder: 30, link: { type: 'PAGE', pageCode: 'CASES' }, children: [] },
        { label: '新闻中心', enabled: true, sortOrder: 40, link: { type: 'PAGE', pageCode: 'NEWS' }, children: [] },
        { label: '关于我们', enabled: true, sortOrder: 50, link: { type: 'PAGE', pageCode: 'ABOUT' }, children: [] },
      ]
    }
  }

  const homeSrc = (usePublished ? configViews.HOME?.published : configViews.HOME?.draft) as Partial<HomeDraft> | null
  if (homeSrc) {
    drafts.HOME = {
      sectionOrder: normalizeSectionOrder((homeSrc as Record<string, unknown>).sectionOrder),
      hero: {
        items: (homeSrc.hero?.items ?? []).map((h) => ({
          key: h.key,
          enabled: h.enabled ?? true,
          sortOrder: h.sortOrder ?? 100,
          title: h.title ?? '',
          subtitle: h.subtitle ?? '',
          desktopMediaId: h.desktopMediaId ? Number(h.desktopMediaId) : undefined,
          mobileMediaId: h.mobileMediaId ? Number(h.mobileMediaId) : undefined,
          imageAlt: h.imageAlt ?? '',
          buttonText: h.buttonText ?? '',
          link: h.link ?? { type: 'NONE' },
        })),
      },
      metrics: {
        enabled: homeSrc.metrics?.enabled ?? true,
        title: homeSrc.metrics?.title ?? '企业实力',
        subtitle: homeSrc.metrics?.subtitle ?? '',
        items: (homeSrc.metrics?.items ?? []).map((m) => ({
          key: m.key,
          sortOrder: m.sortOrder ?? 100,
          displayValue: m.displayValue ?? '',
          unit: m.unit ?? '',
          label: m.label ?? '',
          description: m.description ?? '',
        })),
      },
      services: {
        enabled: homeSrc.services?.enabled ?? true,
        title: homeSrc.services?.title ?? '产品与解决方案',
        subtitle: homeSrc.services?.subtitle ?? '',
        groups: (homeSrc.services?.groups ?? []).map((g) => ({
          key: g.key,
          sortOrder: g.sortOrder ?? 100,
          title: g.title ?? '',
          description: g.description ?? '',
          imageMediaId: g.imageMediaId ? Number(g.imageMediaId) : undefined,
          contentType: g.contentType ?? 'PRODUCT',
          contentIds: (g.contentIds ?? []).map(Number),
          moreLink: g.moreLink ?? { type: 'PAGE', pageCode: 'PRODUCTS' },
        })),
      },
      cases: {
        enabled: homeSrc.cases?.enabled ?? true,
        title: homeSrc.cases?.title ?? '客户案例',
        subtitle: homeSrc.cases?.subtitle ?? '',
        contentIds: (homeSrc.cases?.contentIds ?? []).map(Number),
      },
      customers: {
        enabled: homeSrc.customers?.enabled ?? true,
        title: homeSrc.customers?.title ?? '合作客户',
        subtitle: homeSrc.customers?.subtitle ?? '',
        contentIds: (homeSrc.customers?.contentIds ?? []).map(Number),
      },
      news: {
        enabled: homeSrc.news?.enabled ?? true,
        title: homeSrc.news?.title ?? '新闻动态',
        subtitle: homeSrc.news?.subtitle ?? '',
        mode: homeSrc.news?.mode ?? 'SELECTED',
        limit: homeSrc.news?.limit ?? 3,
        contentIds: (homeSrc.news?.contentIds ?? []).map(Number),
      },
      consultation: {
        title: homeSrc.consultation?.title ?? '了解适合您的服务',
        subtitle: homeSrc.consultation?.subtitle ?? '',
        buttonText: homeSrc.consultation?.buttonText ?? '立即咨询',
        link: homeSrc.consultation?.link ?? { type: 'CONSULT' },
      },
    }
  }

  const contactSrc = (usePublished ? configViews.CONTACT?.published : configViews.CONTACT?.draft) as Partial<ContactDraft> | null
  if (contactSrc) {
    drafts.CONTACT = {
      title: contactSrc.title || '联系我们',
      description: contactSrc.description ?? '',
      successText: contactSrc.successText ?? '已收到您的咨询，我们会尽快与您联系。',
      formEnabled: contactSrc.formEnabled ?? true,
      phone: contactSrc.phone ?? '',
      email: contactSrc.email ?? '',
      address: contactSrc.address ?? '',
      hours: contactSrc.hours ?? '',
    }
  }
}

async function loadAll() {
  loading.value = true
  try {
    const [site, nav, home, contact] = await Promise.all([
      getConfig<SiteDraft>('SITE'),
      getConfig<NavigationDraft>('NAVIGATION'),
      getConfig<HomeDraft>('HOME'),
      getConfig<ContactDraft>('CONTACT'),
    ])
    configViews.SITE = site
    configViews.NAVIGATION = nav
    configViews.HOME = home
    configViews.CONTACT = contact

    applySourceData(false)

    baselineJson.SITE = JSON.stringify(drafts.SITE)
    baselineJson.NAVIGATION = JSON.stringify(drafts.NAVIGATION)
    baselineJson.HOME = JSON.stringify(drafts.HOME)
    baselineJson.CONTACT = JSON.stringify(drafts.CONTACT)

    historyStack.value = [JSON.stringify(drafts)]
    historyPointer.value = 0

    await loadCatalogAndMedia()
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '加载全局配置失败')
  } finally {
    loading.value = false
  }
}

function switchStudioMode(mode: 'edit' | 'preview' | 'published') {
  if (mode === 'published') {
    applySourceData(true)
  } else if (studioMode.value === 'published') {
    applySourceData(false)
  }
  studioMode.value = mode
}

// ==================== 全局保存与一键发布 ====================
async function saveAllDrafts(silent = false): Promise<boolean> {
  const targets: Array<'SITE' | 'NAVIGATION' | 'HOME' | 'CONTACT'> =
    dirtyKeys.value.length > 0
      ? (dirtyKeys.value as Array<'SITE' | 'NAVIGATION' | 'HOME' | 'CONTACT'>)
      : ['HOME']

  savingAll.value = true
  try {
    for (const key of targets) {
      const view = configViews[key]
      if (!view) continue
      const updated = await saveConfigDraft(
        key,
        JSON.parse(JSON.stringify(drafts[key])),
        view.lockVersion,
      )
      ;(configViews as Record<string, unknown>)[key] = updated
      baselineJson[key] = JSON.stringify(drafts[key])
    }
    if (!silent) {
      ElMessage.success(`已同步保存 ${targets.join(' / ')} 草稿至后台各管理模块！`)
    }
    return true
  } catch (error) {
    if (error instanceof ApiError) {
      const detail = error.fieldErrors?.map((f) => `${f.field}: ${f.message}`).join('；')
      ElMessage.error(detail ? `${error.message}（${detail}）` : error.message)
    } else {
      ElMessage.error('保存草稿失败，请检查输入项')
    }
    return false
  } finally {
    savingAll.value = false
  }
}

async function saveAndPublishAll() {
  if (!isAdmin.value) {
    ElMessage.warning('当前为编辑角色，仅管理员可执行发布')
    return
  }
  if (healthIssues.value.some((i) => i.level === 'error')) {
    healthDrawerOpen.value = true
    ElMessage.warning('存在阻塞发布的必填项缺失，请先在右侧体检面板修正')
    return
  }
  try {
    await ElMessageBox.confirm(
      `即将把当前可视化编排的全局改动（${unpublishedKeys.value.join('、') || 'HOME'}）保存并正式发布到线上官网，确认继续？`,
      '全局保存并发布上线',
      { type: 'warning', confirmButtonText: '立即全局发布', cancelButtonText: '取消' },
    )
  } catch {
    return
  }

  publishingAll.value = true
  try {
    if (dirtyKeys.value.length > 0) {
      const ok = await saveAllDrafts(true)
      if (!ok) return
    }
    const pubTargets: Array<'SITE' | 'NAVIGATION' | 'HOME' | 'CONTACT'> = [
      'SITE',
      'NAVIGATION',
      'CONTACT',
      'HOME',
    ]
    for (const key of pubTargets) {
      const v = configViews[key]
      if (v && v.hasUnpublishedChanges) {
        await publishConfig(key, v.lockVersion, v.draftVersion)
      }
    }
    ElMessage.success('🎉 全局配置已成功发布上线！官网前台已实时更新')
    await loadAll()
  } catch (error) {
    if (error instanceof ApiError) {
      const detail = error.fieldErrors?.map((f) => `${f.field}: ${f.message}`).join('；')
      ElMessage.error(detail ? `${error.message}（${detail}）` : error.message)
    } else {
      ElMessage.error('发布失败')
    }
  } finally {
    publishingAll.value = false
  }
}

// ==================== 模块排序、增删与快捷操作 ====================
function moveSection(key: HomeSectionKey, delta: number) {
  const list = drafts.HOME.sectionOrder
  const idx = list.indexOf(key)
  const target = idx + delta
  if (idx < 0 || target < 0 || target >= list.length) return
  const [item] = list.splice(idx, 1)
  list.splice(target, 0, item)
  recordSnapshot('调整模块顺序')
}

function moveItem<T extends { sortOrder?: number }>(list: T[], index: number, delta: number) {
  const target = index + delta
  if (target < 0 || target >= list.length) return
  if (list[index].sortOrder !== undefined && list[target].sortOrder !== undefined) {
    const tmp = list[index].sortOrder
    list[index].sortOrder = list[target].sortOrder
    list[target].sortOrder = tmp
  }
  const [item] = list.splice(index, 1)
  list.splice(target, 0, item)
  recordSnapshot('调整条目顺序')
}

const currentHero = computed<HeroItem | null>(() => {
  return drafts.HOME.hero.items[heroIndex.value] ?? drafts.HOME.hero.items[0] ?? null
})

function addHeroSlide() {
  if (drafts.HOME.hero.items.length >= 5) {
    ElMessage.warning('首屏轮播最多支持 5 条')
    return
  }
  const firstMediaId = drafts.HOME.hero.items[0]?.desktopMediaId
  drafts.HOME.hero.items.push({
    enabled: true,
    sortOrder: (drafts.HOME.hero.items.length + 1) * 10,
    title: '紫宝盒 · 物流行业全场景 AI 工具',
    subtitle: '聪明看见，有效沟通，覆盖 50+ 高危驾驶与作业场景',
    desktopMediaId: firstMediaId,
    mobileMediaId: undefined,
    imageAlt: '全场景AI工具',
    buttonText: '了解详情',
    link: { type: 'PAGE', pageCode: 'PRODUCTS' },
  })
  heroIndex.value = drafts.HOME.hero.items.length - 1
  selectedSection.value = 'hero'
  recordSnapshot('新增首屏海报')
}

function removeHeroSlide(idx: number) {
  drafts.HOME.hero.items.splice(idx, 1)
  if (heroIndex.value >= drafts.HOME.hero.items.length) {
    heroIndex.value = Math.max(0, drafts.HOME.hero.items.length - 1)
  }
  recordSnapshot('删除首屏海报')
}

function addMetricItem() {
  if (drafts.HOME.metrics.items.length >= 8) {
    ElMessage.warning('企业指标最多配置 8 项')
    return
  }
  drafts.HOME.metrics.items.push({
    sortOrder: (drafts.HOME.metrics.items.length + 1) * 10,
    displayValue: '800W',
    unit: '+',
    label: 'IoT 设备连接',
    description: '实时在线追踪与风控守护',
  })
  selectedSection.value = 'metrics'
  recordSnapshot('新增企业指标')
}

function addServiceGroup() {
  if (drafts.HOME.services.groups.length >= 3) {
    ElMessage.warning('产品服务分组最多 3 组')
    return
  }
  const firstProduct = Object.values(contentCatalog).find((c) => c.contentType === 'PRODUCT')
  drafts.HOME.services.groups.push({
    sortOrder: (drafts.HOME.services.groups.length + 1) * 10,
    title: '新业务服务分组',
    description: '在此输入该业务分组的核心能力描述与适用场景说明。',
    contentType: 'PRODUCT',
    contentIds: firstProduct ? [Number(firstProduct.id)] : [],
    moreLink: { type: 'PAGE', pageCode: 'PRODUCTS' },
  })
  activeServiceGroup.value = drafts.HOME.services.groups.length - 1
  selectedSection.value = 'services'
  recordSnapshot('新增服务分组')
}

function addTopNav() {
  if (drafts.NAVIGATION.items.length >= 8) {
    ElMessage.warning('顶层导航最多 8 项')
    return
  }
  drafts.NAVIGATION.items.push({
    label: `新栏目${drafts.NAVIGATION.items.length + 1}`,
    enabled: true,
    sortOrder: (drafts.NAVIGATION.items.length + 1) * 10,
    link: { type: 'PAGE', pageCode: 'ABOUT' },
    children: [],
  })
  selectedSection.value = 'header'
  recordSnapshot('新增顶部导航')
}

function addFooterGroup() {
  if (drafts.SITE.footerGroups.length >= 4) {
    ElMessage.warning('页脚分组最多 4 组')
    return
  }
  drafts.SITE.footerGroups.push({
    title: `栏目分组${drafts.SITE.footerGroups.length + 1}`,
    links: [
      { label: '产品中心', enabled: true, sortOrder: 10, link: { type: 'PAGE', pageCode: 'PRODUCTS' } },
      { label: '联系我们', enabled: true, sortOrder: 20, link: { type: 'PAGE', pageCode: 'CONTACT' } },
    ],
  })
  selectedSection.value = 'footer'
  recordSnapshot('新增页脚分组')
}

// ==================== 媒体选择器弹窗绑定 ====================
type MediaPickContext =
  | { target: 'siteLogo' }
  | { target: 'heroDesktop'; index: number }
  | { target: 'heroMobile'; index: number }
  | { target: 'serviceGroup'; index: number }

const mediaPickerOpen = ref(false)
const mediaPickCtx = ref<MediaPickContext | null>(null)
const mediaPickerKind = computed<'IMAGE' | 'VIDEO' | 'ALL'>(() => {
  if (!mediaPickCtx.value) return 'ALL'
  if (mediaPickCtx.value.target === 'siteLogo' || mediaPickCtx.value.target === 'serviceGroup') {
    return 'IMAGE'
  }
  return 'ALL'
})

function triggerMediaPick(ctx: MediaPickContext) {
  mediaPickCtx.value = ctx
  mediaPickerOpen.value = true
}

function onMediaPicked(media: MediaItem) {
  mediaCache[Number(media.id)] = media
  const ctx = mediaPickCtx.value
  if (!ctx) return
  if (ctx.target === 'siteLogo') {
    drafts.SITE.logoMediaId = Number(media.id)
  } else if (ctx.target === 'heroDesktop') {
    drafts.HOME.hero.items[ctx.index].desktopMediaId = Number(media.id)
  } else if (ctx.target === 'heroMobile') {
    drafts.HOME.hero.items[ctx.index].mobileMediaId = Number(media.id)
  } else if (ctx.target === 'serviceGroup') {
    drafts.HOME.services.groups[ctx.index].imageMediaId = Number(media.id)
  }
  mediaPickerOpen.value = false
  recordSnapshot('更换图片/视频素材')
}

// ==================== 链接编辑器弹窗绑定 ====================
const linkDialogOpen = ref(false)
const linkDialogTitle = ref('编辑跳转链接')
const linkDialogAllowConsult = ref(false)
const linkDialogAllowNone = ref(true)
const tempLink = ref<LinkValue | null>(null)
let applyLinkCallback: ((val: LinkValue | null) => void) | null = null

function openLinkModal(options: {
  title: string
  current: LinkValue | null
  allowConsult?: boolean
  allowNone?: boolean
  onConfirm: (val: LinkValue | null) => void
}) {
  linkDialogTitle.value = options.title
  tempLink.value = options.current ? JSON.parse(JSON.stringify(options.current)) : { type: 'PAGE', pageCode: 'HOME' }
  linkDialogAllowConsult.value = Boolean(options.allowConsult)
  linkDialogAllowNone.value = options.allowNone ?? true
  applyLinkCallback = options.onConfirm
  linkDialogOpen.value = true
}

function confirmLinkModal() {
  if (applyLinkCallback) {
    applyLinkCallback(tempLink.value ? JSON.parse(JSON.stringify(tempLink.value)) : null)
    recordSnapshot('修改跳转链接')
  }
  linkDialogOpen.value = false
}

function describeLink(link?: LinkValue | null): string {
  if (!link || link.type === 'NONE') return '无链接'
  if (link.type === 'PAGE') return `页面:${link.pageCode || 'HOME'}`
  if (link.type === 'CONTENT') return `${link.contentType}#${link.contentId || '?'}`
  if (link.type === 'EXTERNAL') return `外链:${link.url || ''}`
  if (link.type === 'ANCHOR') return `锚点:#${link.anchorCode || ''}`
  if (link.type === 'CONSULT') return '唤起咨询弹窗'
  return link.type
}

// ==================== 内容卡片解析辅助 ====================
function resolveCards(ids: number[]): ContentView[] {
  return ids.map((id) => contentCatalog[String(id)]).filter((c): c is ContentView => Boolean(c))
}

const resolvedNews = computed<ContentView[]>(() => {
  const limit = drafts.HOME.news.limit || 3
  if (drafts.HOME.news.mode === 'LATEST') {
    return latestNewsList.value.slice(0, limit)
  }
  return resolveCards(drafts.HOME.news.contentIds).slice(0, limit)
})

// ==================== 发布前智能合规体检 ====================
interface HealthIssue {
  level: 'error' | 'warning'
  module: string
  message: string
  section: 'header' | 'hero' | HomeSectionKey | 'footer'
}

const healthIssues = computed<HealthIssue[]>(() => {
  const list: HealthIssue[] = []
  if (!drafts.SITE.siteName.trim()) {
    list.push({ level: 'error', module: '全站设置', message: '站点名称不能为空', section: 'header' })
  }
  if (!drafts.SITE.copyrightText.trim()) {
    list.push({ level: 'error', module: '全站设置', message: '页脚版权文案不能为空', section: 'footer' })
  }
  if (Boolean(drafts.SITE.icpText.trim()) !== Boolean(drafts.SITE.icpUrl.trim())) {
    list.push({ level: 'error', module: '全站设置', message: 'ICP 备案号与备案链接需成对填写', section: 'footer' })
  }
  const enabledHeroes = drafts.HOME.hero.items.filter((h) => h.enabled)
  if (enabledHeroes.length === 0) {
    list.push({ level: 'error', module: '首页首屏', message: '至少需要启用 1 条首屏轮播', section: 'hero' })
  }
  enabledHeroes.forEach((h, idx) => {
    if (!h.title.trim()) {
      list.push({ level: 'error', module: '首页首屏', message: `第 ${idx + 1} 条首屏缺少标题`, section: 'hero' })
    }
    if (!h.desktopMediaId) {
      list.push({ level: 'error', module: '首页首屏', message: `第 ${idx + 1} 条首屏未选择桌面图片或视频`, section: 'hero' })
    }
  })
  drafts.HOME.services.groups.forEach((g, idx) => {
    if (!g.title.trim()) {
      list.push({ level: 'error', module: '产品与服务', message: `分组 ${idx + 1} 缺少标题`, section: 'services' })
    }
    if (!g.contentIds || g.contentIds.length === 0) {
      list.push({ level: 'warning', module: '产品与服务', message: `分组「${g.title || idx + 1}」尚未选择关联内容`, section: 'services' })
    }
  })
  if (!drafts.CONTACT.phone.trim() && !drafts.CONTACT.email.trim()) {
    list.push({ level: 'error', module: '联系我们', message: '联系电话与邮箱至少需填写一项', section: 'footer' })
  }
  return list
})

function focusSection(sec: 'header' | 'hero' | HomeSectionKey | 'footer') {
  selectedSection.value = sec
  document.getElementById(`studio-sec-${sec}`)?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

// 键盘快捷键 Ctrl+Z / Ctrl+Y / Ctrl+S
function handleKeydown(e: KeyboardEvent) {
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'z') {
    if (e.shiftKey) {
      e.preventDefault()
      redo()
    } else {
      e.preventDefault()
      undo()
    }
  } else if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'y') {
    e.preventDefault()
    redo()
  } else if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 's') {
    e.preventDefault()
    saveAllDrafts()
  }
}

onMounted(() => {
  loadAll()
  window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <div v-loading="loading" class="studio">
    <!-- ==================== 顶部全局实景工作台控制栏 ==================== -->
    <header class="studio-bar">
      <div class="studio-bar__left">
        <button type="button" class="studio-back" @click="router.push('/home')">
          ← 返回传统后台
        </button>
        <div class="studio-title">
          <span class="studio-badge">🎨 实景交互编辑台</span>
          <span class="studio-sub">所见即所得 · 全局修改自动同步至后台各板块</span>
        </div>

        <!-- 脏数据模块指示器 -->
        <div class="dirty-pills">
          <span
            v-for="k in (['SITE', 'NAVIGATION', 'HOME', 'CONTACT'] as const)"
            :key="k"
            class="dirty-pill"
            :class="{
              'is-dirty': dirtyKeys.includes(k),
              'is-unpub': configViews[k]?.hasUnpublishedChanges,
            }"
            :title="dirtyKeys.includes(k) ? '有未保存的画布修改' : configViews[k]?.hasUnpublishedChanges ? '草稿已存，待发布上线' : '已与线上同步'"
          >
            {{ k }}
            <i v-if="dirtyKeys.includes(k)" class="dot dot--dirty"></i>
            <i v-else-if="configViews[k]?.hasUnpublishedChanges" class="dot dot--unpub"></i>
          </span>
        </div>
      </div>

      <!-- 中部：模式切换 + 视口切换 + 撤销重做 -->
      <div class="studio-bar__center">
        <div class="seg-group" role="group" aria-label="编辑模式切换">
          <button
            type="button"
            class="seg-btn"
            :class="{ 'is-active': studioMode === 'edit' }"
            @click="switchStudioMode('edit')"
          >
            ✏️ 交互编辑
          </button>
          <button
            type="button"
            class="seg-btn"
            :class="{ 'is-active': studioMode === 'preview' }"
            @click="switchStudioMode('preview')"
          >
            👁️ 纯净预演
          </button>
          <button
            type="button"
            class="seg-btn"
            :class="{ 'is-active': studioMode === 'published' }"
            @click="switchStudioMode('published')"
          >
            🌐 线上已发布版
          </button>
        </div>

        <div class="seg-group" role="group" aria-label="响应式视口切换">
          <button
            type="button"
            class="seg-btn"
            :class="{ 'is-active': viewport === 'desktop' }"
            title="桌面端视口 (100%)"
            @click="viewport = 'desktop'"
          >
            🖥️ 桌面
          </button>
          <button
            type="button"
            class="seg-btn"
            :class="{ 'is-active': viewport === 'tablet' }"
            title="平板视口 (768px)"
            @click="viewport = 'tablet'"
          >
            📟 平板
          </button>
          <button
            type="button"
            class="seg-btn"
            :class="{ 'is-active': viewport === 'mobile' }"
            title="手机端视口 (390px)"
            @click="viewport = 'mobile'"
          >
            📱 手机
          </button>
        </div>

        <div class="seg-group">
          <button type="button" class="seg-btn" :disabled="!canUndo" title="撤销 (Ctrl+Z)" @click="undo">
            ↩ 撤销
          </button>
          <button type="button" class="seg-btn" :disabled="!canRedo" title="重做 (Ctrl+Y)" @click="redo">
            ↪ 重做
          </button>
        </div>
      </div>

      <!-- 右侧：体检、全局保存草稿、全局发布 -->
      <div class="studio-bar__right">
        <button
          type="button"
          class="health-btn"
          :class="{ 'has-error': healthIssues.some((i) => i.level === 'error') }"
          @click="healthDrawerOpen = true"
        >
          🛡️ 体检 ({{ healthIssues.length }})
        </button>
        <el-button
          type="primary"
          size="small"
          :loading="savingAll"
          :disabled="studioMode === 'published'"
          @click="saveAllDrafts(false)"
        >
          💾 保存全局草稿 {{ dirtyKeys.length ? `(${dirtyKeys.length})` : '' }}
        </el-button>
        <el-button
          v-if="isAdmin"
          type="success"
          size="small"
          :loading="publishingAll"
          :disabled="studioMode === 'published'"
          @click="saveAndPublishAll"
        >
          🚀 一键全局发布上线
        </el-button>
      </div>
    </header>

    <!-- ==================== 主体三栏工作区 ==================== -->
    <div class="studio-body">
      <!-- 左侧：全局图层与模块摆放排序大纲树 -->
      <aside v-if="showLeftOutline && studioMode === 'edit'" class="studio-outline">
        <div class="outline-head">
          <span>🗂️ 全局模块与摆放顺序</span>
          <button type="button" class="icon-close" @click="showLeftOutline = false">✕</button>
        </div>
        <p class="outline-tip">点击模块定位画布，使用 ↑↓ 自由调整首页模块上下摆放位置</p>

        <div class="outline-list">
          <!-- 固定顶栏 -->
          <div
            class="outline-item"
            :class="{ 'is-selected': selectedSection === 'header' }"
            @click="focusSection('header')"
          >
            <span class="outline-item__icon">🌐</span>
            <span class="outline-item__label">顶部品牌与导航栏</span>
            <el-tag size="small" type="info">SITE/NAV</el-tag>
          </div>

          <!-- 固定首屏 -->
          <div
            class="outline-item"
            :class="{ 'is-selected': selectedSection === 'hero' }"
            @click="focusSection('hero')"
          >
            <span class="outline-item__icon">🎬</span>
            <span class="outline-item__label">首屏海报/视频 ({{ drafts.HOME.hero.items.length }})</span>
            <el-tag size="small" type="primary">必显</el-tag>
          </div>

          <!-- 可自由排序的 6 大模块 -->
          <div
            v-for="(secKey, sIdx) in drafts.HOME.sectionOrder"
            :key="secKey"
            class="outline-item"
            :class="{
              'is-selected': selectedSection === secKey,
              'is-disabled': secKey !== 'consultation' && !drafts.HOME[secKey].enabled,
            }"
            @click="focusSection(secKey)"
          >
            <span class="outline-item__icon">{{ SECTION_META[secKey].icon }}</span>
            <span class="outline-item__label">{{ SECTION_META[secKey].label }}</span>
            <div class="outline-item__actions" @click.stop>
              <el-switch
                v-if="secKey !== 'consultation'"
                v-model="drafts.HOME[secKey].enabled"
                size="small"
                @change="recordSnapshot('切换模块显隐')"
              />
              <button
                type="button"
                class="mini-btn"
                :disabled="sIdx === 0"
                title="上移模块"
                @click="moveSection(secKey, -1)"
              >
                ↑
              </button>
              <button
                type="button"
                class="mini-btn"
                :disabled="sIdx === drafts.HOME.sectionOrder.length - 1"
                title="下移模块"
                @click="moveSection(secKey, 1)"
              >
                ↓
              </button>
            </div>
          </div>

          <!-- 固定页脚 -->
          <div
            class="outline-item"
            :class="{ 'is-selected': selectedSection === 'footer' }"
            @click="focusSection('footer')"
          >
            <span class="outline-item__icon">📞</span>
            <span class="outline-item__label">页脚与全站联系方式</span>
            <el-tag size="small" type="info">SITE/CONTACT</el-tag>
          </div>
        </div>

        <div class="outline-foot">
          <label class="outline-check">
            <input v-model="showDisabledBlocks" type="checkbox" />
            <span>在编辑态显示已关闭模块（半透明）</span>
          </label>
        </div>
      </aside>

      <!-- 折叠态展开悬浮钮 -->
      <button
        v-if="!showLeftOutline && studioMode === 'edit'"
        type="button"
        class="drawer-toggle drawer-toggle--left"
        @click="showLeftOutline = true"
      >
        🗂️ 模块树
      </button>

      <!-- ==================== 中央：可交互实景画布 ==================== -->
      <main class="studio-canvas-wrap">
        <div class="studio-canvas" :class="[`viewport--${viewport}`, `mode--${studioMode}`]">
          <!-- 1. 顶部品牌与导航实景交互区 (SITE + NAVIGATION) -->
          <header
            id="studio-sec-header"
            class="canvas-header"
            :class="{ 'is-focused': studioMode === 'edit' && selectedSection === 'header' }"
            @click="selectedSection = 'header'"
          >
            <div v-if="studioMode === 'edit'" class="sec-floating-bar">
              <span class="sec-tag">🌐 顶部导航与品牌 (同步至「全站设置」&「导航」)</span>
              <button type="button" class="tb-btn" @click.stop="triggerMediaPick({ target: 'siteLogo' })">
                🖼️ 换 Logo
              </button>
              <button type="button" class="tb-btn" @click.stop="addTopNav">
                ➕ 添加导航项
              </button>
              <button
                type="button"
                class="tb-btn"
                @click.stop="openMegaPreview = !openMegaPreview"
              >
                {{ openMegaPreview ? '收起全景下拉菜单' : '📂 编辑5列全景菜单' }}
              </button>
              <button type="button" class="tb-btn tb-btn--ghost" @click.stop="router.push('/settings/navigation')">
                去传统导航页 ↗
              </button>
            </div>

            <div class="canvas-header__inner">
              <!-- 品牌 Logo 与站点名称直改 -->
              <div class="canvas-brand">
                <div
                  class="canvas-brand__logo-wrap"
                  :title="studioMode === 'edit' ? '点击更换站点 Logo' : ''"
                  @click.stop="studioMode === 'edit' && triggerMediaPick({ target: 'siteLogo' })"
                >
                  <img
                    v-if="drafts.SITE.logoMediaId"
                    :src="mediaUrl(drafts.SITE.logoMediaId)"
                    :alt="drafts.SITE.logoAlt || drafts.SITE.siteName"
                    class="canvas-brand__logo"
                  />
                  <span v-else class="canvas-brand__mark">
                    {{ drafts.SITE.siteName.slice(0, 1) || 'G' }}
                  </span>
                </div>
                <input
                  v-if="studioMode === 'edit'"
                  v-model="drafts.SITE.siteName"
                  class="inline-input inline-input--brand"
                  maxlength="60"
                  placeholder="站点名称"
                  @change="recordSnapshot('修改站点名称')"
                />
                <span v-else class="canvas-brand__name">{{ drafts.SITE.siteName }}</span>
              </div>

              <!-- 导航列表直改 -->
              <nav class="canvas-nav">
                <div
                  v-for="(navItem, nIdx) in drafts.NAVIGATION.items"
                  :key="navItem.key ?? `nav-${nIdx}`"
                  class="canvas-nav__item"
                  :class="{ 'is-disabled': !navItem.enabled }"
                >
                  <template v-if="studioMode === 'edit'">
                    <input
                      v-model="navItem.label"
                      class="inline-input inline-input--nav"
                      maxlength="20"
                      placeholder="导航名"
                      @change="recordSnapshot('修改导航名称')"
                    />
                    <div class="nav-item-pop">
                      <button
                        type="button"
                        title="绑定跳转链接"
                        @click.stop="
                          openLinkModal({
                            title: `导航「${navItem.label}」跳转目标`,
                            current: navItem.link,
                            allowNone: true,
                            allowConsult: true,
                            onConfirm: (val) => (navItem.link = val),
                          })
                        "
                      >
                        🔗 {{ describeLink(navItem.link) }}
                      </button>
                      <button
                        type="button"
                        :disabled="nIdx === 0"
                        title="左移"
                        @click.stop="moveItem(drafts.NAVIGATION.items, nIdx, -1)"
                      >
                        ←
                      </button>
                      <button
                        type="button"
                        :disabled="nIdx === drafts.NAVIGATION.items.length - 1"
                        title="右移"
                        @click.stop="moveItem(drafts.NAVIGATION.items, nIdx, 1)"
                      >
                        →
                      </button>
                      <button
                        type="button"
                        class="danger"
                        title="删除该导航"
                        @click.stop="drafts.NAVIGATION.items.splice(nIdx, 1); recordSnapshot('删除导航项')"
                      >
                        ✕
                      </button>
                    </div>
                  </template>
                  <span v-else class="canvas-nav__text">{{ navItem.label }}</span>
                </div>
              </nav>

              <div class="canvas-header__cta">
                <span class="assistant-pill">🤖 数字物流专家 小七</span>
                <span class="cta-pill">产品试用</span>
              </div>
            </div>

            <!-- 5 列全景下拉菜单可视化编辑弹层 -->
            <div v-if="openMegaPreview && studioMode === 'edit'" class="mega-editor" @click.stop>
              <div class="mega-editor__head">
                <strong>📂 产品与解决方案 · 5 列全景下拉菜单可视化编辑 (同步至 NAVIGATION.megaColumns)</strong>
                <div class="mega-editor__actions">
                  <el-button
                    size="small"
                    @click="
                      drafts.NAVIGATION.megaColumns = drafts.NAVIGATION.megaColumns || [];
                      drafts.NAVIGATION.megaColumns.push({
                        title: '新菜单列',
                        items: [{ label: '新菜单项', path: '/products', tag: '' }],
                      });
                      recordSnapshot('新增全景菜单列')
                    "
                  >
                    + 新增菜单列
                  </el-button>
                  <el-button size="small" @click="openMegaPreview = false">收起</el-button>
                </div>
              </div>
              <div class="mega-editor__grid">
                <div
                  v-for="(col, cIdx) in drafts.NAVIGATION.megaColumns"
                  :key="`mcol-${cIdx}`"
                  class="mega-col-box"
                >
                  <div class="mega-col-box__head">
                    <input v-model="col.title" class="inline-input" placeholder="列标题" />
                    <button
                      type="button"
                      class="mini-del"
                      @click="drafts.NAVIGATION.megaColumns?.splice(cIdx, 1)"
                    >
                      删除列
                    </button>
                  </div>
                  <div v-for="(sub, sIdx) in col.items" :key="`msub-${sIdx}`" class="mega-sub-row">
                    <input v-model="sub.label" class="inline-input" placeholder="名称" style="width: 90px" />
                    <input v-model="sub.path" class="inline-input" placeholder="/products/1" style="flex: 1" />
                    <input v-model="sub.tag" class="inline-input" placeholder="角标(如:首款)" style="width: 64px" />
                    <button type="button" class="mini-del" @click="col.items.splice(sIdx, 1)">✕</button>
                  </div>
                  <button
                    type="button"
                    class="mega-add-sub"
                    @click="col.items.push({ label: '新子项', path: '/products', tag: '' })"
                  >
                    + 添加子项
                  </button>
                </div>
              </div>
            </div>
          </header>

          <!-- 2. 首屏海报/视频轮播实景交互区 (HOME.hero) -->
          <section
            id="studio-sec-hero"
            class="canvas-hero"
            :class="{ 'is-focused': studioMode === 'edit' && selectedSection === 'hero' }"
            @click="selectedSection = 'hero'"
          >
            <!-- 首屏悬浮工具条 -->
            <div v-if="studioMode === 'edit'" class="sec-floating-bar">
              <span class="sec-tag">🎬 首屏海报/视频 (共 {{ drafts.HOME.hero.items.length }}/5 屏)</span>
              <button
                v-if="currentHero"
                type="button"
                class="tb-btn"
                @click.stop="triggerMediaPick({ target: 'heroDesktop', index: heroIndex })"
              >
                🖼️ 换桌面背景(图/视频)
              </button>
              <button
                v-if="currentHero"
                type="button"
                class="tb-btn"
                @click.stop="triggerMediaPick({ target: 'heroMobile', index: heroIndex })"
              >
                📱 换手机背景
              </button>
              <button type="button" class="tb-btn" @click.stop="addHeroSlide">
                ➕ 新增一屏
              </button>
              <button
                v-if="drafts.HOME.hero.items.length > 1"
                type="button"
                class="tb-btn"
                :disabled="heroIndex === 0"
                @click.stop="moveItem(drafts.HOME.hero.items, heroIndex, -1); heroIndex = Math.max(0, heroIndex - 1)"
              >
                ⬅ 前移本屏
              </button>
              <button
                v-if="drafts.HOME.hero.items.length > 1"
                type="button"
                class="tb-btn"
                :disabled="heroIndex === drafts.HOME.hero.items.length - 1"
                @click.stop="moveItem(drafts.HOME.hero.items, heroIndex, 1); heroIndex = Math.min(drafts.HOME.hero.items.length - 1, heroIndex + 1)"
              >
                ➡ 后移本屏
              </button>
              <button
                v-if="currentHero && drafts.HOME.hero.items.length > 1"
                type="button"
                class="tb-btn tb-btn--danger"
                @click.stop="removeHeroSlide(heroIndex)"
              >
                🗑️ 删除本屏
              </button>
            </div>

            <template v-if="currentHero">
              <!-- 背景媒体（自动按桌面/手机视口切换并支持 MP4/WebM 视频） -->
              <div class="canvas-hero__media">
                <template v-if="viewport === 'mobile' && currentHero.mobileMediaId">
                  <video
                    v-if="isVideoMedia(currentHero.mobileMediaId)"
                    :key="`mvid-${currentHero.mobileMediaId}`"
                    :src="mediaUrl(currentHero.mobileMediaId)"
                    autoplay
                    muted
                    loop
                    playsinline
                  ></video>
                  <img v-else :src="mediaUrl(currentHero.mobileMediaId)" :alt="currentHero.imageAlt || currentHero.title" />
                </template>
                <template v-else-if="currentHero.desktopMediaId">
                  <video
                    v-if="isVideoMedia(currentHero.desktopMediaId)"
                    :key="`dvid-${currentHero.desktopMediaId}`"
                    :src="mediaUrl(currentHero.desktopMediaId)"
                    autoplay
                    muted
                    loop
                    playsinline
                  ></video>
                  <img v-else :src="mediaUrl(currentHero.desktopMediaId)" :alt="currentHero.imageAlt || currentHero.title" />
                </template>
                <div v-else class="canvas-hero__empty-media">
                  <p>尚未选择首屏背景图片或视频</p>
                  <el-button
                    v-if="studioMode === 'edit'"
                    type="primary"
                    @click.stop="triggerMediaPick({ target: 'heroDesktop', index: heroIndex })"
                  >
                    点击选择首屏图片 / 视频
                  </el-button>
                </div>
                <div class="canvas-hero__mask"></div>
              </div>

              <!-- 首屏前景文案直改区 -->
              <div class="canvas-hero__content">
                <p class="canvas-hero__kicker">INDUSTRIAL IOT · ENERGY INTELLIGENCE</p>
                <template v-if="studioMode === 'edit'">
                  <textarea
                    v-model="currentHero.title"
                    class="inline-textarea inline-textarea--hero-title"
                    rows="2"
                    maxlength="40"
                    placeholder="在此直接输入首屏大标题（支持换行）"
                    @change="recordSnapshot('修改首屏标题')"
                  ></textarea>
                  <textarea
                    v-model="currentHero.subtitle"
                    class="inline-textarea inline-textarea--hero-sub"
                    rows="2"
                    maxlength="100"
                    placeholder="在此输入首屏副标题说明..."
                    @change="recordSnapshot('修改首屏副标题')"
                  ></textarea>
                  <div class="canvas-hero__btn-edit">
                    <input
                      v-model="currentHero.buttonText"
                      class="inline-input inline-input--hero-btn"
                      maxlength="12"
                      placeholder="按钮文字(如:了解详情)"
                      @change="recordSnapshot('修改首屏按钮文字')"
                    />
                    <button
                      type="button"
                      class="link-badge-btn"
                      @click.stop="
                        openLinkModal({
                          title: '首屏主按钮跳转目标',
                          current: currentHero.link,
                          allowNone: true,
                          allowConsult: true,
                          onConfirm: (val) => {
                            if (currentHero) currentHero.link = val
                          },
                        })
                      "
                    >
                      🔗 {{ describeLink(currentHero.link) }}
                    </button>
                  </div>
                </template>
                <template v-else>
                  <h1 class="canvas-hero__title">{{ currentHero.title }}</h1>
                  <p v-if="currentHero.subtitle" class="canvas-hero__sub">{{ currentHero.subtitle }}</p>
                  <span v-if="currentHero.buttonText" class="canvas-hero__cta">
                    {{ currentHero.buttonText }} →
                  </span>
                </template>

                <!-- 底部轮播切换条 -->
                <div class="canvas-hero__dots">
                  <button
                    v-for="(hItem, hIdx) in drafts.HOME.hero.items"
                    :key="hItem.key ?? `dot-${hIdx}`"
                    type="button"
                    class="hero-dot"
                    :class="{ 'is-active': hIdx === heroIndex, 'is-off': !hItem.enabled }"
                    @click.stop="heroIndex = hIdx"
                  >
                    <span class="hero-dot__num">0{{ hIdx + 1 }}</span>
                    <span class="hero-dot__name">{{ hItem.title || '未命名首屏' }}</span>
                  </button>
                </div>
              </div>
            </template>
          </section>

          <!-- ==================== 3. 动态可自由排序的 6 大业务模块 ==================== -->
          <div class="canvas-sections">
            <template v-for="(secKey, orderIdx) in drafts.HOME.sectionOrder" :key="secKey">
              <!-- (A) 企业指标模块 metrics -->
              <section
                v-if="secKey === 'metrics' && (drafts.HOME.metrics.enabled || (studioMode === 'edit' && showDisabledBlocks))"
                id="studio-sec-metrics"
                class="canvas-block"
                :class="{
                  'is-focused': studioMode === 'edit' && selectedSection === 'metrics',
                  'is-disabled-block': !drafts.HOME.metrics.enabled,
                }"
                @click="selectedSection = 'metrics'"
              >
                <div v-if="studioMode === 'edit'" class="sec-floating-bar">
                  <span class="sec-tag">📊 企业指标 (排序 #{{ orderIdx + 1 }})</span>
                  <el-switch v-model="drafts.HOME.metrics.enabled" size="small" active-text="显示" />
                  <button type="button" class="tb-btn" :disabled="orderIdx === 0" @click.stop="moveSection('metrics', -1)">
                    ⬆️ 上移模块
                  </button>
                  <button
                    type="button"
                    class="tb-btn"
                    :disabled="orderIdx === drafts.HOME.sectionOrder.length - 1"
                    @click.stop="moveSection('metrics', 1)"
                  >
                    ⬇️ 下移模块
                  </button>
                  <button type="button" class="tb-btn" @click.stop="addMetricItem">
                    ➕ 添加指标
                  </button>
                </div>

                <div class="canvas-wrap">
                  <div class="sec-header-center">
                    <template v-if="studioMode === 'edit'">
                      <input
                        v-model="drafts.HOME.metrics.title"
                        class="inline-input inline-input--sec-title"
                        maxlength="40"
                        placeholder="模块标题（如：大数据）"
                      />
                      <input
                        v-model="drafts.HOME.metrics.subtitle"
                        class="inline-input inline-input--sec-sub"
                        maxlength="200"
                        placeholder="可选副标题说明..."
                      />
                    </template>
                    <template v-else>
                      <h2 class="sec-title">{{ drafts.HOME.metrics.title || '企业实力' }}</h2>
                      <p v-if="drafts.HOME.metrics.subtitle" class="sec-sub">{{ drafts.HOME.metrics.subtitle }}</p>
                    </template>
                  </div>

                  <div class="metrics-grid">
                    <div
                      v-for="(mItem, mIdx) in drafts.HOME.metrics.items"
                      :key="mItem.key ?? `m-${mIdx}`"
                      class="metric-box"
                    >
                      <template v-if="studioMode === 'edit'">
                        <div class="metric-box__num-row">
                          <input
                            v-model="mItem.displayValue"
                            class="inline-input inline-input--metric-val"
                            maxlength="16"
                            placeholder="800W"
                          />
                          <input
                            v-model="mItem.unit"
                            class="inline-input inline-input--metric-unit"
                            maxlength="8"
                            placeholder="+"
                          />
                        </div>
                        <input
                          v-model="mItem.label"
                          class="inline-input inline-input--metric-label"
                          maxlength="20"
                          placeholder="指标名称"
                        />
                        <input
                          v-model="mItem.description"
                          class="inline-input inline-input--metric-desc"
                          maxlength="80"
                          placeholder="补充说明(可选)"
                        />
                        <div class="item-mini-bar">
                          <button type="button" :disabled="mIdx === 0" @click.stop="moveItem(drafts.HOME.metrics.items, mIdx, -1)">←</button>
                          <button type="button" :disabled="mIdx === drafts.HOME.metrics.items.length - 1" @click.stop="moveItem(drafts.HOME.metrics.items, mIdx, 1)">→</button>
                          <button type="button" class="danger" @click.stop="drafts.HOME.metrics.items.splice(mIdx, 1)">删除</button>
                        </div>
                      </template>
                      <template v-else>
                        <p class="metric-box__val">
                          {{ mItem.displayValue }}<span>{{ mItem.unit }}</span>
                        </p>
                        <p class="metric-box__lbl">{{ mItem.label }}</p>
                        <p v-if="mItem.description" class="metric-box__dsc">{{ mItem.description }}</p>
                      </template>
                    </div>
                  </div>
                </div>
              </section>

              <!-- (B) 产品与解决方案模块 services -->
              <section
                v-if="secKey === 'services' && (drafts.HOME.services.enabled || (studioMode === 'edit' && showDisabledBlocks))"
                id="studio-sec-services"
                class="canvas-block canvas-block--alt"
                :class="{
                  'is-focused': studioMode === 'edit' && selectedSection === 'services',
                  'is-disabled-block': !drafts.HOME.services.enabled,
                }"
                @click="selectedSection = 'services'"
              >
                <div v-if="studioMode === 'edit'" class="sec-floating-bar">
                  <span class="sec-tag">📦 产品与解决方案 (排序 #{{ orderIdx + 1 }})</span>
                  <el-switch v-model="drafts.HOME.services.enabled" size="small" active-text="显示" />
                  <button type="button" class="tb-btn" :disabled="orderIdx === 0" @click.stop="moveSection('services', -1)">
                    ⬆️ 上移模块
                  </button>
                  <button
                    type="button"
                    class="tb-btn"
                    :disabled="orderIdx === drafts.HOME.sectionOrder.length - 1"
                    @click.stop="moveSection('services', 1)"
                  >
                    ⬇️ 下移模块
                  </button>
                  <button type="button" class="tb-btn" @click.stop="addServiceGroup">
                    ➕ 新增服务分组
                  </button>
                </div>

                <div class="canvas-wrap">
                  <div class="sec-header-center">
                    <template v-if="studioMode === 'edit'">
                      <input
                        v-model="drafts.HOME.services.title"
                        class="inline-input inline-input--sec-title"
                        maxlength="40"
                        placeholder="模块标题（如：产品与解决方案）"
                      />
                      <input
                        v-model="drafts.HOME.services.subtitle"
                        class="inline-input inline-input--sec-sub"
                        maxlength="200"
                        placeholder="模块副标题..."
                      />
                    </template>
                    <template v-else>
                      <h2 class="sec-title">{{ drafts.HOME.services.title }}</h2>
                      <p v-if="drafts.HOME.services.subtitle" class="sec-sub">{{ drafts.HOME.services.subtitle }}</p>
                    </template>
                  </div>

                  <div
                    v-for="(grp, gIdx) in drafts.HOME.services.groups"
                    :key="grp.key ?? `grp-${gIdx}`"
                    class="service-group-stage"
                  >
                    <div class="service-group-stage__info">
                      <template v-if="studioMode === 'edit'">
                        <div class="grp-toolbar">
                          <el-tag size="small">分组 0{{ gIdx + 1 }}</el-tag>
                          <el-select
                            v-model="grp.contentType"
                            size="small"
                            style="width: 110px"
                            @change="grp.contentIds = []"
                          >
                            <el-option label="关联产品" value="PRODUCT" />
                            <el-option label="关联方案" value="SOLUTION" />
                          </el-select>
                          <button type="button" class="mini-btn" :disabled="gIdx === 0" @click.stop="moveItem(drafts.HOME.services.groups, gIdx, -1)">↑</button>
                          <button type="button" class="mini-btn" :disabled="gIdx === drafts.HOME.services.groups.length - 1" @click.stop="moveItem(drafts.HOME.services.groups, gIdx, 1)">↓</button>
                          <button type="button" class="mini-del" @click.stop="drafts.HOME.services.groups.splice(gIdx, 1)">删除分组</button>
                        </div>
                        <input
                          v-model="grp.title"
                          class="inline-input inline-input--grp-title"
                          maxlength="30"
                          placeholder="分组名称（如：IoT 服务）"
                        />
                        <textarea
                          v-model="grp.description"
                          class="inline-textarea"
                          rows="2"
                          maxlength="200"
                          placeholder="分组业务场景描述..."
                        ></textarea>
                        <div class="grp-picker-wrap" @click.stop>
                          <p class="grp-picker-label">已关联的线上卡片（可上下调整顺序或增删）：</p>
                          <ContentPicker v-model="grp.contentIds" :content-type="grp.contentType" :max="6" />
                        </div>
                        <div class="grp-link-row">
                          <button
                            type="button"
                            class="link-badge-btn"
                            @click.stop="
                              openLinkModal({
                                title: `分组「${grp.title}」更多跳转链接`,
                                current: grp.moreLink,
                                allowNone: true,
                                onConfirm: (val) => (grp.moreLink = val),
                              })
                            "
                          >
                            🔗 更多链接: {{ describeLink(grp.moreLink) }}
                          </button>
                        </div>
                      </template>
                      <template v-else>
                        <h3 class="grp-title">{{ grp.title }}</h3>
                        <p class="grp-desc">{{ grp.description }}</p>
                      </template>

                      <!-- 关联内容卡片实时渲染 -->
                      <div class="cards-grid">
                        <div v-for="card in resolveCards(grp.contentIds)" :key="card.id" class="live-card">
                          <img
                            v-if="card.coverMediaId"
                            :src="mediaUrl(card.coverMediaId)"
                            :alt="card.title"
                            class="live-card__img"
                          />
                          <div class="live-card__body">
                            <h4>{{ card.title }}</h4>
                            <span class="live-card__go">查看详情 →</span>
                          </div>
                        </div>
                      </div>
                    </div>

                    <!-- 分组右侧视觉配图 -->
                    <div
                      class="service-group-stage__media"
                      :title="studioMode === 'edit' ? '点击更换该分组配图' : ''"
                      @click.stop="studioMode === 'edit' && triggerMediaPick({ target: 'serviceGroup', index: gIdx })"
                    >
                      <img v-if="grp.imageMediaId" :src="mediaUrl(grp.imageMediaId)" :alt="grp.title" />
                      <div v-else class="media-placeholder">
                        <span>🖼️ {{ studioMode === 'edit' ? '点击上传/选择分组配图' : '暂无配图' }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </section>

              <!-- (C) 客户案例模块 cases -->
              <section
                v-if="secKey === 'cases' && (drafts.HOME.cases.enabled || (studioMode === 'edit' && showDisabledBlocks))"
                id="studio-sec-cases"
                class="canvas-block"
                :class="{
                  'is-focused': studioMode === 'edit' && selectedSection === 'cases',
                  'is-disabled-block': !drafts.HOME.cases.enabled,
                }"
                @click="selectedSection = 'cases'"
              >
                <div v-if="studioMode === 'edit'" class="sec-floating-bar">
                  <span class="sec-tag">🏆 客户案例 (排序 #{{ orderIdx + 1 }})</span>
                  <el-switch v-model="drafts.HOME.cases.enabled" size="small" active-text="显示" />
                  <button type="button" class="tb-btn" :disabled="orderIdx === 0" @click.stop="moveSection('cases', -1)">
                    ⬆️ 上移模块
                  </button>
                  <button
                    type="button"
                    class="tb-btn"
                    :disabled="orderIdx === drafts.HOME.sectionOrder.length - 1"
                    @click.stop="moveSection('cases', 1)"
                  >
                    ⬇️ 下移模块
                  </button>
                  <button type="button" class="tb-btn tb-btn--ghost" @click.stop="router.push('/contents/cases')">
                    管理案例库 ↗
                  </button>
                </div>

                <div class="canvas-wrap">
                  <div class="sec-header-center">
                    <template v-if="studioMode === 'edit'">
                      <input
                        v-model="drafts.HOME.cases.title"
                        class="inline-input inline-input--sec-title"
                        maxlength="40"
                        placeholder="模块标题（如：携手行业标杆客户）"
                      />
                      <input
                        v-model="drafts.HOME.cases.subtitle"
                        class="inline-input inline-input--sec-sub"
                        maxlength="200"
                        placeholder="模块副标题..."
                      />
                    </template>
                    <template v-else>
                      <h2 class="sec-title">{{ drafts.HOME.cases.title }}</h2>
                      <p v-if="drafts.HOME.cases.subtitle" class="sec-sub">{{ drafts.HOME.cases.subtitle }}</p>
                    </template>
                  </div>

                  <div v-if="studioMode === 'edit'" class="inline-picker-panel" @click.stop>
                    <ContentPicker v-model="drafts.HOME.cases.contentIds" content-type="CASE" :max="6" />
                  </div>

                  <div class="cards-grid cards-grid--3">
                    <div v-for="cItem in resolveCards(drafts.HOME.cases.contentIds)" :key="cItem.id" class="live-card">
                      <img
                        v-if="cItem.coverMediaId"
                        :src="mediaUrl(cItem.coverMediaId)"
                        :alt="cItem.title"
                        class="live-card__img"
                      />
                      <div class="live-card__body">
                        <span v-if="cItem.categoryName" class="live-card__tag">{{ cItem.categoryName }}</span>
                        <h4>{{ cItem.title }}</h4>
                      </div>
                    </div>
                  </div>
                </div>
              </section>

              <!-- (D) 客户标识墙模块 customers -->
              <section
                v-if="secKey === 'customers' && (drafts.HOME.customers.enabled || (studioMode === 'edit' && showDisabledBlocks))"
                id="studio-sec-customers"
                class="canvas-block canvas-block--alt"
                :class="{
                  'is-focused': studioMode === 'edit' && selectedSection === 'customers',
                  'is-disabled-block': !drafts.HOME.customers.enabled,
                }"
                @click="selectedSection = 'customers'"
              >
                <div v-if="studioMode === 'edit'" class="sec-floating-bar">
                  <span class="sec-tag">🤝 客户标识墙 (排序 #{{ orderIdx + 1 }})</span>
                  <el-switch v-model="drafts.HOME.customers.enabled" size="small" active-text="显示" />
                  <button type="button" class="tb-btn" :disabled="orderIdx === 0" @click.stop="moveSection('customers', -1)">
                    ⬆️ 上移模块
                  </button>
                  <button
                    type="button"
                    class="tb-btn"
                    :disabled="orderIdx === drafts.HOME.sectionOrder.length - 1"
                    @click.stop="moveSection('customers', 1)"
                  >
                    ⬇️ 下移模块
                  </button>
                  <button type="button" class="tb-btn tb-btn--ghost" @click.stop="router.push('/contents/customers')">
                    管理客户标识库 ↗
                  </button>
                </div>

                <div class="canvas-wrap">
                  <div class="sec-header-center">
                    <template v-if="studioMode === 'edit'">
                      <input
                        v-model="drafts.HOME.customers.title"
                        class="inline-input inline-input--sec-title"
                        maxlength="40"
                        placeholder="模块标题（如：合作客户）"
                      />
                    </template>
                    <template v-else>
                      <h2 class="sec-title">{{ drafts.HOME.customers.title || '合作客户' }}</h2>
                    </template>
                  </div>

                  <div v-if="studioMode === 'edit'" class="inline-picker-panel" @click.stop>
                    <ContentPicker v-model="drafts.HOME.customers.contentIds" content-type="CUSTOMER" :max="30" />
                  </div>

                  <div class="logo-wall">
                    <div
                      v-for="cust in resolveCards(drafts.HOME.customers.contentIds)"
                      :key="cust.id"
                      class="logo-wall__item"
                    >
                      <img v-if="cust.coverMediaId" :src="mediaUrl(cust.coverMediaId)" :alt="cust.title" />
                      <span>{{ cust.title }}</span>
                    </div>
                  </div>
                </div>
              </section>

              <!-- (E) 新闻动态模块 news -->
              <section
                v-if="secKey === 'news' && (drafts.HOME.news.enabled || (studioMode === 'edit' && showDisabledBlocks))"
                id="studio-sec-news"
                class="canvas-block"
                :class="{
                  'is-focused': studioMode === 'edit' && selectedSection === 'news',
                  'is-disabled-block': !drafts.HOME.news.enabled,
                }"
                @click="selectedSection = 'news'"
              >
                <div v-if="studioMode === 'edit'" class="sec-floating-bar">
                  <span class="sec-tag">📰 新闻动态 (排序 #{{ orderIdx + 1 }})</span>
                  <el-switch v-model="drafts.HOME.news.enabled" size="small" active-text="显示" />
                  <el-radio-group v-model="drafts.HOME.news.mode" size="small" @click.stop>
                    <el-radio-button value="SELECTED">手选推荐</el-radio-button>
                    <el-radio-button value="LATEST">最新时间倒序</el-radio-button>
                  </el-radio-group>
                  <el-input-number
                    v-model="drafts.HOME.news.limit"
                    size="small"
                    :min="1"
                    :max="6"
                    style="width: 96px"
                    @click.stop
                  />
                  <button type="button" class="tb-btn" :disabled="orderIdx === 0" @click.stop="moveSection('news', -1)">
                    ⬆️ 上移
                  </button>
                  <button
                    type="button"
                    class="tb-btn"
                    :disabled="orderIdx === drafts.HOME.sectionOrder.length - 1"
                    @click.stop="moveSection('news', 1)"
                  >
                    ⬇️ 下移
                  </button>
                </div>

                <div class="canvas-wrap">
                  <div class="sec-header-center">
                    <template v-if="studioMode === 'edit'">
                      <input
                        v-model="drafts.HOME.news.title"
                        class="inline-input inline-input--sec-title"
                        maxlength="40"
                        placeholder="模块标题（如：最新动态）"
                      />
                    </template>
                    <template v-else>
                      <h2 class="sec-title">{{ drafts.HOME.news.title || '新闻动态' }}</h2>
                    </template>
                  </div>

                  <div
                    v-if="studioMode === 'edit' && drafts.HOME.news.mode === 'SELECTED'"
                    class="inline-picker-panel"
                    @click.stop
                  >
                    <ContentPicker v-model="drafts.HOME.news.contentIds" content-type="NEWS" :max="6" />
                  </div>

                  <div class="news-list-preview">
                    <div v-for="nItem in resolvedNews" :key="nItem.id" class="news-row-preview">
                      <span class="news-row-preview__date">{{ nItem.displayTime?.slice(0, 10) || '最新' }}</span>
                      <strong class="news-row-preview__title">{{ nItem.title }}</strong>
                      <span class="news-row-preview__arrow">阅读全文 →</span>
                    </div>
                  </div>
                </div>
              </section>

              <!-- (F) 底部咨询行动区 consultation -->
              <section
                v-if="secKey === 'consultation'"
                id="studio-sec-consultation"
                class="canvas-cta"
                :class="{ 'is-focused': studioMode === 'edit' && selectedSection === 'consultation' }"
                @click="selectedSection = 'consultation'"
              >
                <div v-if="studioMode === 'edit'" class="sec-floating-bar">
                  <span class="sec-tag">💬 底部咨询区 (排序 #{{ orderIdx + 1 }})</span>
                  <button type="button" class="tb-btn" :disabled="orderIdx === 0" @click.stop="moveSection('consultation', -1)">
                    ⬆️ 上移模块
                  </button>
                  <button
                    type="button"
                    class="tb-btn"
                    :disabled="orderIdx === drafts.HOME.sectionOrder.length - 1"
                    @click.stop="moveSection('consultation', 1)"
                  >
                    ⬇️ 下移模块
                  </button>
                </div>

                <div class="canvas-wrap canvas-cta__inner">
                  <template v-if="studioMode === 'edit'">
                    <input
                      v-model="drafts.HOME.consultation.title"
                      class="inline-input inline-input--cta-title"
                      maxlength="40"
                      placeholder="行动区主标题"
                    />
                    <input
                      v-model="drafts.HOME.consultation.subtitle"
                      class="inline-input inline-input--cta-sub"
                      maxlength="200"
                      placeholder="行动区副标题说明"
                    />
                    <div class="canvas-hero__btn-edit">
                      <input
                        v-model="drafts.HOME.consultation.buttonText"
                        class="inline-input inline-input--hero-btn"
                        maxlength="12"
                        placeholder="立即咨询"
                      />
                      <button
                        type="button"
                        class="link-badge-btn"
                        @click.stop="
                          openLinkModal({
                            title: '底部咨询区按钮动作',
                            current: drafts.HOME.consultation.link,
                            allowNone: false,
                            allowConsult: true,
                            onConfirm: (val) => (drafts.HOME.consultation.link = val),
                          })
                        "
                      >
                        🔗 {{ describeLink(drafts.HOME.consultation.link) }}
                      </button>
                    </div>
                  </template>
                  <template v-else>
                    <h2>{{ drafts.HOME.consultation.title }}</h2>
                    <p v-if="drafts.HOME.consultation.subtitle">{{ drafts.HOME.consultation.subtitle }}</p>
                    <span class="canvas-hero__cta">{{ drafts.HOME.consultation.buttonText || '立即咨询' }}</span>
                  </template>
                </div>
              </section>
            </template>
          </div>

          <!-- ==================== 4. 底部页脚与全站联系方式实景交互区 (SITE + CONTACT) ==================== -->
          <footer
            id="studio-sec-footer"
            class="canvas-footer"
            :class="{ 'is-focused': studioMode === 'edit' && selectedSection === 'footer' }"
            @click="selectedSection = 'footer'"
          >
            <div v-if="studioMode === 'edit'" class="sec-floating-bar">
              <span class="sec-tag">📞 页脚分组与全站联系方式 (同步至「全站设置」&「联系我们」)</span>
              <el-switch v-model="drafts.CONTACT.formEnabled" size="small" active-text="开启全站咨询表单" />
              <button type="button" class="tb-btn" @click.stop="addFooterGroup">
                ➕ 添加页脚分组
              </button>
              <button type="button" class="tb-btn tb-btn--ghost" @click.stop="router.push('/pages/contact')">
                去联系我们页 ↗
              </button>
            </div>

            <div class="canvas-wrap canvas-footer__grid">
              <!-- 品牌与标语 -->
              <div class="footer-col">
                <strong>{{ drafts.SITE.siteName }}</strong>
                <textarea
                  v-if="studioMode === 'edit'"
                  v-model="drafts.SITE.slogan"
                  class="inline-textarea inline-textarea--dark"
                  rows="2"
                  maxlength="100"
                  placeholder="一句话品牌标语..."
                ></textarea>
                <p v-else>{{ drafts.SITE.slogan }}</p>
              </div>

              <!-- 页脚分组链接直改 -->
              <div
                v-for="(fGrp, fgIdx) in drafts.SITE.footerGroups"
                :key="`fg-${fgIdx}`"
                class="footer-col"
              >
                <template v-if="studioMode === 'edit'">
                  <div class="fg-head">
                    <input v-model="fGrp.title" class="inline-input inline-input--dark" maxlength="20" placeholder="分组名" />
                    <button type="button" class="mini-del" @click.stop="drafts.SITE.footerGroups.splice(fgIdx, 1)">✕</button>
                  </div>
                  <div v-for="(fLnk, flIdx) in fGrp.links" :key="fLnk.key ?? `fl-${flIdx}`" class="fg-link-row">
                    <input v-model="fLnk.label" class="inline-input inline-input--dark" maxlength="20" placeholder="链接名" />
                    <button
                      type="button"
                      class="mini-btn"
                      title="修改链接"
                      @click.stop="
                        openLinkModal({
                          title: `页脚链接「${fLnk.label}」`,
                          current: fLnk.link,
                          allowNone: false,
                          allowConsult: true,
                          onConfirm: (val) => (fLnk.link = val),
                        })
                      "
                    >
                      🔗
                    </button>
                    <button type="button" class="mini-del" @click.stop="fGrp.links.splice(flIdx, 1)">✕</button>
                  </div>
                  <button
                    type="button"
                    class="mega-add-sub"
                    @click.stop="
                      fGrp.links.push({
                        label: '新链接',
                        enabled: true,
                        sortOrder: (fGrp.links.length + 1) * 10,
                        link: { type: 'PAGE', pageCode: 'ABOUT' },
                      })
                    "
                  >
                    + 添加链接
                  </button>
                </template>
                <template v-else>
                  <h4>{{ fGrp.title }}</h4>
                  <ul>
                    <li v-for="(fLnk, flIdx) in fGrp.links" :key="`flv-${flIdx}`">{{ fLnk.label }}</li>
                  </ul>
                </template>
              </div>

              <!-- 联系我们直改列 (同步至 CONTACT) -->
              <div class="footer-col footer-col--contact">
                <h4>联系我们 (全站共用)</h4>
                <template v-if="studioMode === 'edit'">
                  <label class="dark-field">
                    <span>全国热线电话</span>
                    <input v-model="drafts.CONTACT.phone" class="inline-input inline-input--dark-lg" placeholder="400-888-6688" />
                  </label>
                  <label class="dark-field">
                    <span>服务时间</span>
                    <input v-model="drafts.CONTACT.hours" class="inline-input inline-input--dark" placeholder="周一至周日 9:00-18:00" />
                  </label>
                  <label class="dark-field">
                    <span>商务邮箱</span>
                    <input v-model="drafts.CONTACT.email" class="inline-input inline-input--dark" placeholder="connect@company.com" />
                  </label>
                  <label class="dark-field">
                    <span>公司地址</span>
                    <input v-model="drafts.CONTACT.address" class="inline-input inline-input--dark" placeholder="详细办公地址" />
                  </label>
                </template>
                <template v-else>
                  <p class="footer-phone">{{ drafts.CONTACT.phone }}</p>
                  <p>{{ drafts.CONTACT.hours }}</p>
                  <p>{{ drafts.CONTACT.email }}</p>
                  <p>{{ drafts.CONTACT.address }}</p>
                </template>
              </div>
            </div>

            <!-- 版权与备案直改条 -->
            <div class="canvas-wrap canvas-footer__bottom">
              <template v-if="studioMode === 'edit'">
                <input v-model="drafts.SITE.copyrightText" class="inline-input inline-input--dark" style="width: 260px" placeholder="版权文案(必填)" />
                <input v-model="drafts.SITE.icpText" class="inline-input inline-input--dark" style="width: 180px" placeholder="ICP备案号" />
                <input v-model="drafts.SITE.icpUrl" class="inline-input inline-input--dark" style="width: 220px" placeholder="https://beian.miit.gov.cn/" />
              </template>
              <template v-else>
                <span>{{ drafts.SITE.copyrightText }}</span>
                <span v-if="drafts.SITE.icpText">{{ drafts.SITE.icpText }}</span>
              </template>
            </div>
          </footer>
        </div>
      </main>
    </div>

    <!-- ==================== 媒体选择器弹窗 ==================== -->
    <MediaPicker
      v-model="mediaPickerOpen"
      :selected-id="null"
      :kind="mediaPickerKind"
      @select="onMediaPicked"
    />

    <!-- ==================== 链接绑定器弹窗 ==================== -->
    <el-dialog v-model="linkDialogOpen" :title="linkDialogTitle" width="620px">
      <p style="margin-bottom: 12px; font-size: 13px; color: #5f6470">
        选择点击该按钮或菜单后的目标动作（支持站内页面、具体内容详情、外部链接、首页锚点或打开咨询弹窗）：
      </p>
      <LinkEditor
        v-model="tempLink"
        :allow-none="linkDialogAllowNone"
        :allow-consult="linkDialogAllowConsult"
      />
      <template #footer>
        <el-button @click="linkDialogOpen = false">取消</el-button>
        <el-button type="primary" @click="confirmLinkModal">确定绑定</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 发布前智能体检抽屉 ==================== -->
    <el-drawer v-model="healthDrawerOpen" title="🛡️ 全局发布前智能合规体检" size="400px">
      <el-alert
        v-if="healthIssues.length === 0"
        type="success"
        :closable="false"
        show-icon
        title="所有模块均已通过合规检查，可以随时安全发布！"
      />
      <div v-else class="health-list">
        <div
          v-for="(iss, idx) in healthIssues"
          :key="idx"
          class="health-item"
          :class="`is-${iss.level}`"
          @click="healthDrawerOpen = false; focusSection(iss.section)"
        >
          <div class="health-item__head">
            <el-tag size="small" :type="iss.level === 'error' ? 'danger' : 'warning'">
              {{ iss.level === 'error' ? '阻塞项' : '建议优化' }}
            </el-tag>
            <strong>{{ iss.module }}</strong>
          </div>
          <p class="health-item__msg">{{ iss.message }}</p>
          <span class="health-item__go">点击定位至画布该模块 →</span>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.studio {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: #0f1117;
  color: #101014;
}

/* 顶部全局控制栏 */
.studio-bar {
  height: 58px;
  background: #161923;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding: 0 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #ffffff;
  flex: none;
  z-index: 50;
}

.studio-bar__left,
.studio-bar__center,
.studio-bar__right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.studio-back {
  padding: 6px 12px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: rgba(255, 255, 255, 0.06);
  color: #ffffff;
  font-size: 12.5px;
  cursor: pointer;
}

.studio-back:hover {
  background: rgba(255, 255, 255, 0.14);
}

.studio-title {
  display: flex;
  flex-direction: column;
}

.studio-badge {
  font-size: 13.5px;
  font-weight: 700;
  color: #ffffff;
}

.studio-sub {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.55);
}

.dirty-pills {
  display: flex;
  gap: 6px;
  margin-left: 6px;
}

.dirty-pill {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 11px;
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.65);
}

.dirty-pill.is-dirty {
  background: rgba(245, 158, 11, 0.2);
  color: #fcd34d;
  border: 1px solid rgba(245, 158, 11, 0.45);
}

.dirty-pill.is-unpub {
  color: #a5b4fc;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.dot--dirty {
  background: #f59e0b;
}

.dot--unpub {
  background: #818cf8;
}

.seg-group {
  display: inline-flex;
  background: rgba(255, 255, 255, 0.07);
  padding: 3px;
  border-radius: 8px;
  gap: 2px;
}

.seg-btn {
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.72);
  font-size: 12px;
  padding: 5px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.seg-btn:hover:not(:disabled) {
  color: #ffffff;
}

.seg-btn.is-active {
  background: #5b4be8;
  color: #ffffff;
  font-weight: 600;
}

.seg-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.health-btn {
  padding: 5px 12px;
  border-radius: 6px;
  border: 1px solid rgba(16, 185, 129, 0.4);
  background: rgba(16, 185, 129, 0.12);
  color: #6ee7b7;
  font-size: 12px;
  cursor: pointer;
}

.health-btn.has-error {
  border-color: rgba(239, 68, 68, 0.5);
  background: rgba(239, 68, 68, 0.18);
  color: #fca5a5;
}

/* 主体布局 */
.studio-body {
  flex: 1;
  display: flex;
  min-height: 0;
  position: relative;
}

.studio-outline {
  width: 280px;
  background: #1b1f2c;
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  color: #ffffff;
  display: flex;
  flex-direction: column;
  flex: none;
}

.outline-head {
  padding: 14px 16px 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13.5px;
  font-weight: 700;
}

.outline-tip {
  padding: 0 16px 12px;
  font-size: 11.5px;
  color: rgba(255, 255, 255, 0.5);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.outline-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.outline-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 10px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid transparent;
  cursor: pointer;
  font-size: 12.5px;
  transition: all 0.15s ease;
}

.outline-item:hover {
  background: rgba(255, 255, 255, 0.08);
}

.outline-item.is-selected {
  border-color: #7c6df2;
  background: rgba(91, 75, 232, 0.22);
}

.outline-item.is-disabled {
  opacity: 0.5;
}

.outline-item__label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.outline-item__actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.outline-foot {
  padding: 12px 14px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.outline-check {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.drawer-toggle--left {
  position: absolute;
  left: 12px;
  top: 12px;
  z-index: 30;
  padding: 8px 12px;
  border-radius: 8px;
  background: #5b4be8;
  color: #fff;
  border: none;
  font-size: 12px;
  cursor: pointer;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
}

/* 中央画布容器 */
.studio-canvas-wrap {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  justify-content: center;
  background: #0d0f14;
}

.studio-canvas {
  width: 100%;
  background: #ffffff;
  border-radius: 12px;
  overflow-x: hidden;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.45);
  transition: max-width 0.3s ease;
  height: fit-content;
}

.studio-canvas.viewport--desktop {
  max-width: 1440px;
}

.studio-canvas.viewport--tablet {
  max-width: 768px;
}

.studio-canvas.viewport--mobile {
  max-width: 390px;
}

/* 区块悬浮工具栏 */
.sec-floating-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding: 8px 14px;
  background: rgba(22, 25, 35, 0.94);
  backdrop-filter: blur(8px);
  color: #ffffff;
  font-size: 12px;
  border-bottom: 1px solid rgba(91, 75, 232, 0.4);
}

.sec-tag {
  font-weight: 700;
  color: #c4b5fd;
  margin-right: 6px;
}

.tb-btn {
  padding: 4px 10px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.08);
  color: #ffffff;
  font-size: 12px;
  cursor: pointer;
}

.tb-btn:hover:not(:disabled) {
  background: #5b4be8;
  border-color: #5b4be8;
}

.tb-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.tb-btn--danger:hover {
  background: #dc2626;
  border-color: #dc2626;
}

.tb-btn--ghost {
  margin-left: auto;
  color: #a5b4fc;
}

/* 顶部导航实景 */
.canvas-header {
  position: relative;
  background: #ffffff;
  border-bottom: 1px solid rgba(16, 16, 20, 0.08);
}

.canvas-header.is-focused,
.canvas-hero.is-focused,
.canvas-block.is-focused,
.canvas-cta.is-focused,
.canvas-footer.is-focused {
  outline: 2px solid #5b4be8;
  outline-offset: -2px;
}

.canvas-header__inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 14px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}

.canvas-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.canvas-brand__logo-wrap {
  cursor: pointer;
}

.canvas-brand__logo {
  max-height: 34px;
  max-width: 140px;
  object-fit: contain;
}

.canvas-brand__mark {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  background: #5b4be8;
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.canvas-nav {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.canvas-nav__item {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 4px 6px;
  border-radius: 6px;
  background: #f8f9fc;
}

.nav-item-pop {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-item-pop button {
  font-size: 11px;
  padding: 2px 5px;
  border-radius: 4px;
  border: 1px solid #e2e8f0;
  background: #fff;
  cursor: pointer;
}

.nav-item-pop button.danger {
  color: #dc2626;
}

.canvas-header__cta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.assistant-pill {
  font-size: 12px;
  padding: 5px 12px;
  border-radius: 999px;
  background: #eeebff;
  color: #4636d6;
  font-weight: 600;
}

.cta-pill {
  font-size: 12.5px;
  padding: 6px 14px;
  border-radius: 6px;
  background: #101014;
  color: #ffffff;
  font-weight: 600;
}

/* 5列大菜单可视化编辑区 */
.mega-editor {
  background: #f8f9fc;
  border-top: 1px solid #e2e8f0;
  padding: 16px 24px;
}

.mega-editor__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 13px;
}

.mega-editor__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

.mega-col-box {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 10px;
}

.mega-col-box__head {
  display: flex;
  justify-content: space-between;
  gap: 6px;
  margin-bottom: 8px;
}

.mega-sub-row {
  display: flex;
  gap: 4px;
  margin-bottom: 6px;
}

.mega-add-sub {
  width: 100%;
  padding: 5px;
  border: 1px dashed #cbd5e1;
  border-radius: 6px;
  background: transparent;
  font-size: 12px;
  color: #5b4be8;
  cursor: pointer;
}

/* 首屏实景 */
.canvas-hero {
  position: relative;
  min-height: 520px;
  background: #08080c;
  color: #ffffff;
  display: flex;
  flex-direction: column;
}

.canvas-hero__media {
  position: relative;
  min-height: 480px;
  flex: 1;
}

.canvas-hero__media img,
.canvas-hero__media video {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.canvas-hero__empty-media {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: rgba(255, 255, 255, 0.65);
}

.canvas-hero__mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(8, 8, 12, 0.88) 0%, rgba(8, 8, 12, 0.45) 55%, rgba(8, 8, 12, 0.2) 100%);
  pointer-events: none;
}

.canvas-hero__content {
  position: relative;
  z-index: 5;
  max-width: 1200px;
  width: 100%;
  margin: -420px auto 0;
  padding: 40px 28px 48px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.canvas-hero__kicker {
  font-size: 12px;
  letter-spacing: 0.16em;
  color: #a5b4fc;
  font-weight: 600;
}

.canvas-hero__title {
  font-size: clamp(32px, 4.5vw, 54px);
  font-weight: 700;
  line-height: 1.15;
  white-space: pre-line;
}

.canvas-hero__sub {
  font-size: 17px;
  color: rgba(255, 255, 255, 0.8);
  max-width: 600px;
}

.canvas-hero__btn-edit {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.link-badge-btn {
  padding: 8px 14px;
  border-radius: 8px;
  border: 1px solid rgba(91, 75, 232, 0.5);
  background: rgba(91, 75, 232, 0.2);
  color: #e0e7ff;
  font-size: 12.5px;
  cursor: pointer;
}

.canvas-hero__dots {
  display: flex;
  gap: 10px;
  margin-top: 28px;
  flex-wrap: wrap;
}

.hero-dot {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.75);
  font-size: 12px;
  cursor: pointer;
}

.hero-dot.is-active {
  background: #5b4be8;
  border-color: #5b4be8;
  color: #ffffff;
  font-weight: 600;
}

/* 通用内联编辑输入框 */
.inline-input,
.inline-textarea {
  font-family: inherit;
  border: 1px dashed rgba(91, 75, 232, 0.45);
  border-radius: 6px;
  padding: 4px 8px;
  background: rgba(255, 255, 255, 0.92);
  color: #101014;
  transition: all 0.15s ease;
}

.inline-input:focus,
.inline-textarea:focus {
  outline: none;
  border-style: solid;
  border-color: #5b4be8;
  box-shadow: 0 0 0 3px rgba(91, 75, 232, 0.18);
}

.inline-input--brand {
  font-size: 16px;
  font-weight: 700;
  width: 120px;
}

.inline-input--nav {
  font-size: 13.5px;
  font-weight: 600;
  width: 105px;
}

.inline-textarea--hero-title {
  font-size: 36px;
  font-weight: 700;
  color: #ffffff;
  background: rgba(16, 16, 24, 0.65);
  border-color: rgba(255, 255, 255, 0.35);
  max-width: 680px;
}

.inline-textarea--hero-sub {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  background: rgba(16, 16, 24, 0.55);
  border-color: rgba(255, 255, 255, 0.25);
  max-width: 640px;
}

.inline-input--hero-btn {
  font-size: 14px;
  font-weight: 600;
  width: 150px;
}

/* 业务模块通用画布样式 */
.canvas-block {
  padding: 0 0 64px;
  background: #ffffff;
}

.canvas-block--alt {
  background: #f5f7fa;
}

.is-disabled-block {
  opacity: 0.48;
}

.canvas-wrap {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px 0;
}

.sec-header-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  margin-bottom: 32px;
  text-align: center;
}

.inline-input--sec-title {
  font-size: 28px;
  font-weight: 700;
  text-align: center;
  width: min(420px, 100%);
}

.inline-input--sec-sub {
  font-size: 14px;
  color: #5f6470;
  text-align: center;
  width: min(560px, 100%);
}

/* 指标网格 */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 20px;
}

.metric-box {
  padding: 20px;
  border-radius: 14px;
  border: 1px solid rgba(16, 16, 20, 0.08);
  background: #ffffff;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.metric-box__num-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.inline-input--metric-val {
  font-size: 30px;
  font-weight: 700;
  color: #5b4be8;
  width: 130px;
}

.inline-input--metric-unit {
  font-size: 18px;
  font-weight: 700;
  width: 52px;
}

.metric-box__val {
  font-size: 40px;
  font-weight: 700;
  color: #101014;
}

.metric-box__val span {
  font-size: 22px;
  color: #5b4be8;
  margin-left: 3px;
}

.item-mini-bar {
  display: flex;
  gap: 6px;
  margin-top: 4px;
}

.item-mini-bar button,
.mini-btn,
.mini-del {
  font-size: 11px;
  padding: 2px 7px;
  border-radius: 4px;
  border: 1px solid #cbd5e1;
  background: #fff;
  color: #334155;
  cursor: pointer;
}

.mini-del,
.item-mini-bar button.danger {
  color: #dc2626;
  border-color: #fecaca;
}

/* 服务分组舞台 */
.service-group-stage {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 28px;
  padding: 24px;
  border-radius: 18px;
  background: #ffffff;
  border: 1px solid rgba(16, 16, 20, 0.08);
  margin-bottom: 24px;
}

@media (max-width: 860px) {
  .service-group-stage {
    grid-template-columns: 1fr;
  }
}

.service-group-stage__info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.grp-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.inline-input--grp-title {
  font-size: 22px;
  font-weight: 700;
}

.grp-picker-wrap {
  background: #f8f9fc;
  padding: 12px;
  border-radius: 10px;
}

.grp-picker-label {
  font-size: 12px;
  color: #5f6470;
  margin-bottom: 8px;
}

.service-group-stage__media {
  border-radius: 14px;
  overflow: hidden;
  background: #eef2f6;
  min-height: 240px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.service-group-stage__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.media-placeholder {
  color: #64748b;
  font-size: 13px;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 14px;
  margin-top: 8px;
}

.cards-grid--3 {
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
}

.live-card {
  border-radius: 12px;
  border: 1px solid rgba(16, 16, 20, 0.08);
  background: #ffffff;
  overflow: hidden;
}

.live-card__img {
  width: 100%;
  aspect-ratio: 16 / 9;
  object-fit: cover;
}

.live-card__body {
  padding: 12px;
}

.live-card__tag {
  font-size: 11px;
  color: #5b4be8;
  background: #eeebff;
  padding: 2px 8px;
  border-radius: 999px;
}

.live-card__body h4 {
  margin: 6px 0;
  font-size: 15px;
}

.live-card__go {
  font-size: 12px;
  color: #5b4be8;
}

.inline-picker-panel {
  max-width: 680px;
  margin: 0 auto 24px;
  padding: 14px;
  background: #f8f9fc;
  border-radius: 12px;
  border: 1px dashed rgba(91, 75, 232, 0.35);
}

.logo-wall {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 12px;
}

.logo-wall__item {
  height: 84px;
  border-radius: 10px;
  background: #ffffff;
  border: 1px solid rgba(16, 16, 20, 0.07);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10px;
  gap: 6px;
  font-size: 12px;
}

.logo-wall__item img {
  max-height: 36px;
  max-width: 110px;
  object-fit: contain;
}

.news-list-preview {
  display: flex;
  flex-direction: column;
}

.news-row-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 16px 0;
  border-bottom: 1px solid rgba(16, 16, 20, 0.08);
}

.news-row-preview__date {
  font-size: 13px;
  color: #8b90a0;
}

.news-row-preview__title {
  flex: 1;
  font-size: 16px;
}

.news-row-preview__arrow {
  font-size: 13px;
  color: #5b4be8;
}

/* 底部咨询区与页脚 */
.canvas-cta {
  background: linear-gradient(135deg, #10121d 0%, #1c1738 100%);
  color: #ffffff;
  padding-bottom: 56px;
}

.canvas-cta__inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  text-align: center;
}

.inline-input--cta-title {
  font-size: 28px;
  font-weight: 700;
  text-align: center;
  width: min(480px, 100%);
}

.inline-input--cta-sub {
  font-size: 14px;
  text-align: center;
  width: min(540px, 100%);
}

.canvas-footer {
  background: #090a0f;
  color: #a0a4b8;
  padding-bottom: 36px;
}

.canvas-footer__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 24px;
}

.footer-col {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.footer-col h4,
.footer-col strong {
  color: #ffffff;
  font-size: 15px;
}

.fg-head,
.fg-link-row {
  display: flex;
  align-items: center;
  gap: 4px;
}

.inline-input--dark,
.inline-textarea--dark {
  background: rgba(255, 255, 255, 0.08);
  color: #ffffff;
  border-color: rgba(255, 255, 255, 0.22);
  width: 100%;
}

.inline-input--dark-lg {
  background: rgba(255, 255, 255, 0.1);
  color: #ffffff;
  font-size: 18px;
  font-weight: 700;
}

.dark-field {
  display: flex;
  flex-direction: column;
  gap: 3px;
  font-size: 11px;
}

.footer-phone {
  font-size: 22px;
  font-weight: 700;
  color: #ffffff;
}

.canvas-footer__bottom {
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  font-size: 12px;
}

.health-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.health-item {
  padding: 12px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  cursor: pointer;
}

.health-item.is-error {
  background: #fef2f2;
  border-color: #fecaca;
}

.health-item.is-warning {
  background: #fffbeb;
  border-color: #fde68a;
}

.health-item__head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.health-item__msg {
  font-size: 13px;
  margin-bottom: 6px;
}

.health-item__go {
  font-size: 12px;
  color: #5b4be8;
  font-weight: 600;
}
</style>
```

### `website/admin/src/layouts/AdminLayout.vue`

```vue
<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuth } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const { current, logout } = useAuth()

const isCollapse = ref(false)
const isAdmin = computed(() => current.value?.role === 'ADMIN')

const breadcrumbs = computed(() => {
  const metaTitle = String(route.meta.title ?? '管理后台')
  const path = route.path

  if (path === '/dashboard') return [{ label: '工作台' }]
  if (path.startsWith('/settings')) {
    return [{ label: '站点管理' }, { label: metaTitle }]
  }
  if (path === '/home') {
    return [{ label: '首页管理' }]
  }
  if (path.startsWith('/contents/categories')) {
    return [{ label: '内容管理' }, { label: '内容分类' }]
  }
  if (path.startsWith('/contents')) {
    const isEdit = path.includes('/edit')
    const isCreate = path.includes('/create') || path.includes('/new')
    const baseListPath = '/' + path.split('/').filter(Boolean).slice(0, 2).join('/')
    const listTitle = String(route.meta.title ?? '内容').replace(/^(新建|编辑)/, '')
    if (isCreate) return [{ label: '内容管理' }, { label: `${listTitle}管理`, to: baseListPath }, { label: '新建' }]
    if (isEdit) return [{ label: '内容管理' }, { label: `${listTitle}管理`, to: baseListPath }, { label: '编辑' }]
    return [{ label: '内容管理' }, { label: metaTitle }]
  }
  if (path.startsWith('/pages')) {
    return [{ label: '单页管理' }, { label: metaTitle }]
  }
  if (path === '/media') {
    return [{ label: '素材管理' }]
  }
  if (path === '/leads') {
    return [{ label: '咨询管理' }]
  }
  if (path.startsWith('/system')) {
    return [{ label: '系统管理' }, { label: metaTitle }]
  }
  return [{ label: metaTitle }]
})

function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

function handleResize() {
  if (window.innerWidth < 1024) {
    isCollapse.value = true
  }
}

function openVisualStudio() {
  router.push('/preview/home')
}

function openLiveSite() {
  window.open('/', '_blank', 'noopener')
}

async function handleLogout() {
  try {
    await logout()
  } finally {
    await router.replace('/login')
  }
}

onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<template>
  <el-container class="shell">
    <el-aside :width="isCollapse ? '64px' : '224px'" class="aside">
      <div class="brand">
        <span class="logo">G</span>
        <span v-show="!isCollapse" class="brand-name">官网管理后台</span>
      </div>

      <el-menu
        :default-active="route.path"
        :collapse="isCollapse"
        router
        class="menu"
        :collapse-transition="false"
      >
        <el-menu-item index="/dashboard">
          <el-icon>
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="7" height="7"></rect>
              <rect x="14" y="3" width="7" height="7"></rect>
              <rect x="14" y="14" width="7" height="7"></rect>
              <rect x="3" y="14" width="7" height="7"></rect>
            </svg>
          </el-icon>
          <template #title>工作台</template>
        </el-menu-item>

        <el-menu-item index="/preview/home">
          <el-icon>
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 20h9"></path>
              <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path>
            </svg>
          </el-icon>
          <template #title>实景预演编辑台</template>
        </el-menu-item>

        <el-sub-menu index="site">
          <template #title>
            <el-icon>
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="2" y1="12" x2="22" y2="12"></line>
                <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path>
              </svg>
            </el-icon>
            <span>站点管理</span>
          </template>
          <el-menu-item index="/settings/site">全站设置</el-menu-item>
          <el-menu-item index="/settings/navigation">导航与页脚</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/home">
          <el-icon>
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
              <polyline points="9 22 9 12 15 12 15 22"></polyline>
            </svg>
          </el-icon>
          <template #title>首页管理</template>
        </el-menu-item>

        <el-sub-menu index="contents">
          <template #title>
            <el-icon>
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                <polyline points="14 2 14 8 20 8"></polyline>
                <line x1="16" y1="13" x2="8" y2="13"></line>
                <line x1="16" y1="17" x2="8" y2="17"></line>
                <polyline points="10 9 9 9 8 9"></polyline>
              </svg>
            </el-icon>
            <span>内容管理</span>
          </template>
          <el-menu-item index="/contents/products">产品</el-menu-item>
          <el-menu-item index="/contents/solutions">解决方案</el-menu-item>
          <el-menu-item index="/contents/cases">客户案例</el-menu-item>
          <el-menu-item index="/contents/news">新闻</el-menu-item>
          <el-menu-item index="/contents/customers">客户标识</el-menu-item>
          <el-menu-item index="/contents/categories">内容分类</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="pages">
          <template #title>
            <el-icon>
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
                <line x1="8" y1="21" x2="16" y2="21"></line>
                <line x1="12" y1="17" x2="12" y2="21"></line>
              </svg>
            </el-icon>
            <span>单页管理</span>
          </template>
          <el-menu-item index="/pages/about">关于我们</el-menu-item>
          <el-menu-item index="/pages/contact">联系我们</el-menu-item>
          <el-menu-item index="/pages/privacy">隐私说明</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/media">
          <el-icon>
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
              <circle cx="8.5" cy="8.5" r="1.5"></circle>
              <polyline points="21 15 16 10 5 21"></polyline>
            </svg>
          </el-icon>
          <template #title>素材管理</template>
        </el-menu-item>

        <el-menu-item v-if="isAdmin" index="/leads">
          <el-icon>
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
            </svg>
          </el-icon>
          <template #title>咨询管理</template>
        </el-menu-item>

        <el-sub-menu v-if="isAdmin" index="system">
          <template #title>
            <el-icon>
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="3"></circle>
                <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
              </svg>
            </el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/users">账号管理</el-menu-item>
          <el-menu-item index="/system/audit-logs">操作日志</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container class="content-container">
      <el-header height="60px" class="header">
        <div class="header-left">
          <button
            type="button"
            class="collapse-btn"
            :title="isCollapse ? '展开菜单' : '收起菜单'"
            @click="toggleCollapse"
          >
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="3" y1="12" x2="21" y2="12"></line>
              <line x1="3" y1="6" x2="21" y2="6"></line>
              <line x1="3" y1="18" x2="21" y2="18"></line>
            </svg>
          </button>

          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item v-for="(b, idx) in breadcrumbs" :key="b.label" :to="b.to">
              <span :class="{ 'is-active': idx === breadcrumbs.length - 1 }">{{ b.label }}</span>
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <button
            type="button"
            class="btn-studio"
            title="进入所见即所得的全站实景交互预演编辑台"
            @click="openVisualStudio"
          >
            <span>🎨 实景预演交互编辑</span>
          </button>

          <button type="button" class="btn-live-site" title="在新标签页中查看官网前台效果" @click="openLiveSite">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path>
              <polyline points="15 3 21 3 21 9"></polyline>
              <line x1="10" y1="14" x2="21" y2="3"></line>
            </svg>
            <span>查看前台官网</span>
          </button>

          <div class="user-profile">
            <div class="user-avatar" :class="{ 'user-avatar--admin': isAdmin }">
              {{ (current?.displayName || current?.username || 'U').slice(0, 1).toUpperCase() }}
            </div>
            <div class="user-info">
              <span class="user-name">{{ current?.displayName || current?.username }}</span>
              <span class="user-role-badge" :class="isAdmin ? 'badge--admin' : 'badge--editor'">
                {{ isAdmin ? '系统管理员' : '内容编辑' }}
              </span>
            </div>
          </div>

          <div class="header-divider"></div>

          <RouterLink to="/profile/password" class="icon-btn" title="修改密码">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
            </svg>
          </RouterLink>

          <button type="button" class="icon-btn icon-btn--logout" title="退出登录" @click="handleLogout">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
              <polyline points="16 17 21 12 16 7"></polyline>
              <line x1="21" y1="12" x2="9" y2="12"></line>
            </svg>
          </button>
        </div>
      </el-header>

      <el-main class="main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.shell {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: var(--a-bg, #f5f7fa);
}

.aside {
  background: #ffffff;
  border-right: 1px solid var(--a-border);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  transition: width 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 2px 0 8px rgba(16, 16, 20, 0.02);
  z-index: 10;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 60px;
  padding: 0 18px;
  border-bottom: 1px solid var(--a-border);
  flex: none;
}

.logo {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: linear-gradient(135deg, #7a6bff, #5b4be8);
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 700;
  box-shadow: 0 4px 14px rgba(91, 75, 232, 0.3);
  flex: none;
}

.brand-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--a-text);
  white-space: nowrap;
}

.menu {
  border-right: none;
  padding: 8px 4px;
  flex: 1;
}

.content-container {
  min-width: 0;
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  border-bottom: 1px solid var(--a-border);
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(16, 16, 20, 0.02);
  flex: none;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  border: none;
  background: transparent;
  color: var(--a-text-muted);
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.breadcrumb {
  font-size: 13.5px;
}

.breadcrumb .is-active {
  color: var(--a-text);
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.btn-studio {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 6px;
  border: none;
  background: linear-gradient(135deg, #6d5ce8 0%, #5b4be8 100%);
  color: #ffffff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(91, 75, 232, 0.28);
  transition: all 0.2s ease;
}

.btn-studio:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(91, 75, 232, 0.4);
}

.btn-live-site {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 6px;
  border: 1px solid rgba(91, 75, 232, 0.25);
  background: rgba(91, 75, 232, 0.06);
  color: var(--a-brand, #5b4be8);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-live-site:hover {
  background: var(--a-brand, #5b4be8);
  color: #ffffff;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 8px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #3b82f6;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.user-avatar--admin {
  background: linear-gradient(135deg, #ef4444, #dc2626);
}

.user-info {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}

.user-name {
  font-size: 13px;
  font-weight: 600;
}

.user-role-badge {
  font-size: 11px;
  border-radius: 3px;
  padding: 1px 4px;
  font-weight: 500;
}

.badge--admin {
  color: #dc2626;
  background: #fef2f2;
}

.badge--editor {
  color: #2563eb;
  background: #eff6ff;
}

.header-divider {
  width: 1px;
  height: 20px;
  background: var(--a-border);
}

.icon-btn {
  border: none;
  background: transparent;
  color: var(--a-text-muted);
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-btn--logout:hover {
  background: #fef2f2;
  color: #ef4444;
}

.main {
  padding: 20px 24px 32px;
  overflow-y: auto;
  flex: 1;
}
</style>
```

### `website/server/src/main/java/com/company/cms/site/SiteConfigValidator.java` (新增 `sectionOrder` 模块顺序持久化支持)

```java
package com.company.cms.site;

import com.company.cms.common.ApiException;
import com.company.cms.common.ErrorCode;
import com.company.cms.common.FieldErrorItem;
import com.company.cms.common.HtmlSanitizer;
import com.company.cms.content.ContentRepository;
import com.company.cms.content.ContentRow;
import com.company.cms.media.MediaService;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 配置草稿校验与归一化（文档 6.1～6.4）。
 *
 * 保存草稿只校验结构、长度、数组上限、资源存在与链接安全；必填与「目标已发布」
 * 放到发布校验，保证运营可以分多次填完。
 */
@Component
public class SiteConfigValidator {

    private static final Set<String> PAGE_CODES = Set.of(
            "HOME", "PRODUCTS", "SOLUTIONS", "CASES", "NEWS", "ABOUT", "CONTACT", "PRIVACY");

    private final ObjectMapper objectMapper;
    private final HtmlSanitizer htmlSanitizer;
    private final LinkResolver links;
    private final MediaService mediaService;
    private final ContentRepository contents;

    public SiteConfigValidator(ObjectMapper objectMapper, HtmlSanitizer htmlSanitizer,
                                LinkResolver links, MediaService mediaService,
                                ContentRepository contents) {
        this.objectMapper = objectMapper;
        this.htmlSanitizer = htmlSanitizer;
        this.links = links;
        this.mediaService = mediaService;
        this.contents = contents;
    }

    public JsonNode validate(SiteConfigKey key, JsonNode draft) {
        if (draft == null || !draft.isObject()) {
            throw ApiException.validation("draft", "配置内容必须是 JSON 对象");
        }
        List<FieldErrorItem> errors = new ArrayList<>();
        ObjectNode normalized = switch (key) {
            case SITE -> site(draft, errors);
            case NAVIGATION -> navigation(draft, errors);
            case ABOUT -> about(draft, errors);
            case CONTACT -> contact(draft, errors);
            case PRIVACY -> privacy(draft, errors);
            case HOME -> home(draft, errors);
        };
        if (!errors.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "请检查配置内容", errors);
        }
        return normalized;
    }

    // -------------------------------------------------- SITE

    private ObjectNode site(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("siteName", limit(text(draft, "siteName"), 60, "siteName", "站点名称", errors));
        node.put("slogan", limit(text(draft, "slogan"), 100, "slogan", "标语", errors));
        // 旧字段 copyright 仍写入，公开接口取 copyrightText 优先
        String copyright = limit(text(draft, "copyrightText"), 120, "copyrightText", "版权文案", errors);
        if (copyright.isEmpty()) {
            copyright = limit(text(draft, "copyright"), 120, "copyrightText", "版权文案", errors);
        }
        node.put("copyrightText", copyright);
        node.put("icpText", limit(text(draft, "icpText"), 80, "icpText", "备案号", errors));
        String icpUrl = externalOrEmpty(draft, "icpUrl", "icpUrl", "备案链接", errors);
        node.put("icpUrl", icpUrl);
        node.put("policeText", limit(text(draft, "policeText"), 80, "policeText", "公安备案号", errors));
        node.put("policeUrl", externalOrEmpty(draft, "policeUrl", "policeUrl", "公安备案链接", errors));

        Long logoId = mediaId(draft.get("logoMediaId"), "logoMediaId", "Logo", errors);
        if (logoId != null) {
            node.put("logoMediaId", logoId);
        }
        node.put("logoAlt", limit(text(draft, "logoAlt"), 120, "logoAlt", "Logo 替代文字", errors));
        Long faviconId = mediaId(draft.get("faviconMediaId"), "faviconMediaId", "站点图标", errors);
        if (faviconId != null) {
            node.put("faviconMediaId", faviconId);
        }

        JsonNode seo = draft.get("defaultSeo");
        ObjectNode seoNode = objectMapper.createObjectNode();
        seoNode.put("title", limit(text(seo, "title"), 80, "defaultSeo.title", "默认标题", errors));
        seoNode.put("description", limit(text(seo, "description"), 200, "defaultSeo.description", "默认描述", errors));
        node.set("defaultSeo", seoNode);

        ArrayNode groups = objectMapper.createArrayNode();
        JsonNode footerGroups = draft.get("footerGroups");
        if (footerGroups != null && footerGroups.isArray()) {
            if (footerGroups.size() > 4) {
                errors.add(new FieldErrorItem("footerGroups", "页脚分组最多 4 组"));
            }
            for (int i = 0; i < Math.min(footerGroups.size(), 4); i++) {
                JsonNode group = footerGroups.get(i);
                String path = "footerGroups[" + i + "]";
                ObjectNode item = objectMapper.createObjectNode();
                String title = limit(text(group, "title"), 20, path + ".title", "分组标题", errors);
                if (title.isEmpty()) {
                    errors.add(new FieldErrorItem(path + ".title", "请填写分组标题"));
                }
                item.put("title", title);
                item.set("links", navLinks(group.get("links"), path + ".links", 10, errors));
                groups.add(item);
            }
        }
        node.set("footerGroups", groups);

        // 联系方式统一在 CONTACT 维护，这里不再接受重复录入
        return node;
    }

    // -------------------------------------------------- NAVIGATION

    private ObjectNode navigation(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        JsonNode items = draft.get("items");
        ArrayNode result = objectMapper.createArrayNode();
        if (items == null || !items.isArray() || items.isEmpty()) {
            errors.add(new FieldErrorItem("items", "请至少配置一项导航"));
        } else {
            if (items.size() > 8) {
                errors.add(new FieldErrorItem("items", "顶级导航最多 8 项"));
            }
            Set<String> labels = new HashSet<>();
            Set<String> keys = new HashSet<>();
            for (int i = 0; i < Math.min(items.size(), 8); i++) {
                JsonNode item = items.get(i);
                String path = "items[" + i + "]";
                if (item == null || !item.isObject()) {
                    errors.add(new FieldErrorItem(path, "格式不正确"));
                    continue;
                }
                ObjectNode nav = objectMapper.createObjectNode();
                String key = stableKey(item, "nav", "key-" + i, keys, path, errors);
                nav.put("key", key);
                String label = limit(text(item, "label"), 20, path + ".label", "导航名称", errors);
                if (label.isEmpty()) {
                    errors.add(new FieldErrorItem(path + ".label", "请填写导航名称"));
                }
                if (!labels.add(label)) {
                    errors.add(new FieldErrorItem(path + ".label", "同级导航名称不能重复"));
                }
                nav.put("label", label);
                nav.put("enabled", bool(item, "enabled", true));
                nav.put("sortOrder", sortOrder(item, path, errors));
                nav.set("link", linkOrNull(item.get("link"), path + ".link", "导航链接", errors));

                JsonNode children = item.get("children");
                ArrayNode childNodes = objectMapper.createArrayNode();
                if (children != null && children.isArray() && !children.isEmpty()) {
                    if (children.size() > 10) {
                        errors.add(new FieldErrorItem(path + ".children", "子菜单最多 10 项"));
                    }
                    Set<String> childLabels = new HashSet<>();
                    Set<String> childKeys = new HashSet<>();
                    for (int j = 0; j < Math.min(children.size(), 10); j++) {
                        JsonNode child = children.get(j);
                        String childPath = path + ".children[" + j + "]";
                        if (child == null || !child.isObject()) {
                            errors.add(new FieldErrorItem(childPath, "格式不正确"));
                            continue;
                        }
                        ObjectNode childNode = objectMapper.createObjectNode();
                        childNode.put("key", stableKey(child, "nav", "key-c" + i + "-" + j,
                                childKeys, childPath, errors));
                        String childLabel = limit(text(child, "label"), 20,
                                childPath + ".label", "子菜单名称", errors);
                        if (childLabel.isEmpty()) {
                            errors.add(new FieldErrorItem(childPath + ".label", "请填写子菜单名称"));
                        }
                        if (!childLabels.add(childLabel)) {
                            errors.add(new FieldErrorItem(childPath + ".label", "同级导航名称不能重复"));
                        }
                        childNode.put("label", childLabel);
                        childNode.put("enabled", bool(child, "enabled", true));
                        childNode.put("sortOrder", sortOrder(child, childPath, errors));
                        childNode.set("link", linkOrNull(child.get("link"),
                                childPath + ".link", "子菜单链接", errors));
                        childNodes.add(childNode);
                    }
                }
                nav.set("children", childNodes);
                result.add(nav);
            }
        }
        node.set("items", result);

        // 产品/解决方案 5列全景下拉大菜单配置
        ArrayNode megaResult = objectMapper.createArrayNode();
        JsonNode mega = draft.get("megaColumns");
        if (mega != null && mega.isArray()) {
            if (mega.size() > 8) {
                errors.add(new FieldErrorItem("megaColumns", "大菜单列数最多 8 列"));
            }
            for (int i = 0; i < Math.min(mega.size(), 8); i++) {
                JsonNode col = mega.get(i);
                String path = "megaColumns[" + i + "]";
                if (col == null || !col.isObject()) continue;
                ObjectNode colNode = objectMapper.createObjectNode();
                String colTitle = limit(text(col, "title"), 30, path + ".title", "大菜单列标题", errors);
                colNode.put("title", colTitle);

                ArrayNode colItems = objectMapper.createArrayNode();
                JsonNode subItems = col.get("items");
                if (subItems != null && subItems.isArray()) {
                    if (subItems.size() > 15) {
                        errors.add(new FieldErrorItem(path + ".items", "每列最多 15 个项目"));
                    }
                    for (int j = 0; j < Math.min(subItems.size(), 15); j++) {
                        JsonNode sub = subItems.get(j);
                        String subPath = path + ".items[" + j + "]";
                        if (sub == null || !sub.isObject()) continue;
                        ObjectNode subNode = objectMapper.createObjectNode();
                        String label = limit(text(sub, "label"), 40, subPath + ".label", "项目名称", errors);
                        if (label.isEmpty()) {
                            errors.add(new FieldErrorItem(subPath + ".label", "请填写项目名称"));
                        }
                        subNode.put("label", label);
                        subNode.put("path", limit(text(sub, "path"), 255, subPath + ".path", "链接路径", errors));
                        subNode.put("tag", limit(text(sub, "tag"), 10, subPath + ".tag", "角标文案", errors));
                        subNode.put("isVideo", bool(sub, "isVideo", false));
                        subNode.put("videoUrl", limit(text(sub, "videoUrl"), 500, subPath + ".videoUrl", "视频链接", errors));
                        subNode.put("videoTitle", limit(text(sub, "videoTitle"), 100, subPath + ".videoTitle", "视频标题", errors));
                        colItems.add(subNode);
                    }
                }
                colNode.set("items", colItems);
                megaResult.add(colNode);
            }
        }
        node.set("megaColumns", megaResult);

        // 底部 CTA 引导条
        JsonNode cta = draft.get("megaCta");
        if (cta != null && cta.isObject()) {
            ObjectNode ctaNode = objectMapper.createObjectNode();
            ctaNode.put("text", limit(text(cta, "text"), 100, "megaCta.text", "底部提示语", errors));
            ctaNode.put("linkText", limit(text(cta, "linkText"), 50, "megaCta.linkText", "底部链接文字", errors));
            node.set("megaCta", ctaNode);
        }

        return node;
    }

    /** 页脚链接沿用导航项结构，但不允许二级。 */
    private ArrayNode navLinks(JsonNode source, String field, int max, List<FieldErrorItem> errors) {
        ArrayNode result = objectMapper.createArrayNode();
        if (source == null || !source.isArray()) {
            return result;
        }
        if (source.size() > max) {
            errors.add(new FieldErrorItem(field, "链接最多 " + max + " 项"));
        }
        Set<String> keys = new HashSet<>();
        for (int i = 0; i < Math.min(source.size(), max); i++) {
            JsonNode item = source.get(i);
            String path = field + "[" + i + "]";
            if (item == null || !item.isObject()) {
                errors.add(new FieldErrorItem(path, "格式不正确"));
                continue;
            }
            ObjectNode node = objectMapper.createObjectNode();
            node.put("key", stableKey(item, "lnk", "key-l" + i, keys, path, errors));
            String label = limit(text(item, "label"), 20, path + ".label", "链接名称", errors);
            if (label.isEmpty()) {
                errors.add(new FieldErrorItem(path + ".label", "请填写链接名称"));
            }
            node.put("label", label);
            node.put("enabled", bool(item, "enabled", true));
            node.put("sortOrder", sortOrder(item, path, errors));
            node.set("link", linkOrNull(item.get("link"), path + ".link", "链接", errors));
            result.add(node);
        }
        return result;
    }

    // -------------------------------------------------- ABOUT

    private ObjectNode about(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("intro", limit(text(draft, "intro"), 2000, "intro", "公司简介", errors));
        node.put("mission", limit(text(draft, "mission"), 2000, "mission", "使命", errors));

        ArrayNode milestones = objectMapper.createArrayNode();
        JsonNode source = draft.get("milestones");
        if (source != null && source.isArray()) {
            if (source.size() > 20) {
                errors.add(new FieldErrorItem("milestones", "发展历程最多 20 条"));
            }
            for (int i = 0; i < Math.min(source.size(), 20); i++) {
                JsonNode item = source.get(i);
                String path = "milestones[" + i + "]";
                String year = text(item, "year");
                if (!year.matches("^\\d{4}$")) {
                    errors.add(new FieldErrorItem(path + ".year", "年份需为 4 位数字"));
                }
                ObjectNode milestone = objectMapper.createObjectNode();
                milestone.put("year", year);
                milestone.put("title", limit(text(item, "title"), 50, path + ".title", "标题", errors));
                milestone.put("description",
                        limit(text(item, "description"), 300, path + ".description", "说明", errors));
                milestones.add(milestone);
            }
        }
        node.set("milestones", milestones);

        ArrayNode qualifications = objectMapper.createArrayNode();
        JsonNode quals = draft.get("qualifications");
        if (quals != null && quals.isArray()) {
            if (quals.size() > 20) {
                errors.add(new FieldErrorItem("qualifications", "资质最多 20 条"));
            }
            for (int i = 0; i < Math.min(quals.size(), 20); i++) {
                JsonNode item = quals.get(i);
                String path = "qualifications[" + i + "]";
                ObjectNode qual = objectMapper.createObjectNode();
                qual.put("name", limit(text(item, "name"), 60, path + ".name", "名称", errors));
                qual.put("description",
                        limit(text(item, "description"), 300, path + ".description", "说明", errors));
                qualifications.add(qual);
            }
        }
        node.set("qualifications", qualifications);
        return node;
    }

    // -------------------------------------------------- CONTACT

    private ObjectNode contact(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        String title = limit(text(draft, "title"), 50, "title", "页面标题", errors);
        node.put("title", title.isEmpty() ? "联系我们" : title);
        node.put("description", limit(text(draft, "description"), 300, "description", "页面描述", errors));
        node.put("successText", limit(text(draft, "successText"), 200, "successText", "提交成功提示", errors));
        node.put("formEnabled", bool(draft, "formEnabled", false));
        node.put("phone", limit(text(draft, "phone"), 40, "phone", "联系电话", errors));
        String email = limit(text(draft, "email"), 100, "email", "邮箱", errors);
        if (!email.isEmpty() && !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            errors.add(new FieldErrorItem("email", "邮箱格式不正确"));
        }
        node.put("email", email);
        node.put("address", limit(text(draft, "address"), 120, "address", "地址", errors));
        node.put("hours", limit(text(draft, "hours"), 60, "hours", "服务时间", errors));
        return node;
    }

    // -------------------------------------------------- PRIVACY

    private ObjectNode privacy(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        String updatedAt = text(draft, "updatedAt");
        try {
            node.put("updatedAt", LocalDate.parse(updatedAt).toString());
        } catch (DateTimeParseException ex) {
            errors.add(new FieldErrorItem("updatedAt", "更新日期格式需为 yyyy-MM-dd"));
            node.put("updatedAt", LocalDate.now().toString());
        }
        String body = htmlSanitizer.sanitize(text(draft, "bodyHtml"));
        if (body.isBlank()) {
            errors.add(new FieldErrorItem("bodyHtml", "隐私说明正文不能为空"));
        }
        node.put("bodyHtml", body);
        return node;
    }

    // -------------------------------------------------- HOME

    private static final List<String> DEFAULT_HOME_SECTIONS = List.of(
            "metrics", "services", "cases", "customers", "news", "consultation");

    private ObjectNode home(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        node.set("sectionOrder", sectionOrder(draft.get("sectionOrder")));
        node.set("hero", hero(draft.get("hero"), errors));
        node.set("metrics", metrics(draft.get("metrics"), errors));
        node.set("services", services(draft.get("services"), errors));
        node.set("cases", caseBlock(draft.get("cases"), errors));
        node.set("customers", customers(draft.get("customers"), errors));
        node.set("news", news(draft.get("news"), errors));
        node.set("consultation", consultation(draft.get("consultation"), errors));
        return node;
    }

    private ArrayNode sectionOrder(JsonNode source) {
        ArrayNode arr = objectMapper.createArrayNode();
        Set<String> added = new HashSet<>();
        if (source != null && source.isArray()) {
            for (JsonNode item : source) {
                String key = item != null && item.isString() ? item.asString().trim() : "";
                if (DEFAULT_HOME_SECTIONS.contains(key) && added.add(key)) {
                    arr.add(key);
                }
            }
        }
        for (String defKey : DEFAULT_HOME_SECTIONS) {
            if (added.add(defKey)) {
                arr.add(defKey);
            }
        }
        return arr;
    }

    private ObjectNode hero(JsonNode source, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        ArrayNode items = objectMapper.createArrayNode();
        JsonNode list = source == null ? null : source.get("items");
        if (list != null && list.isArray()) {
            if (list.size() > 5) {
                errors.add(new FieldErrorItem("hero.items", "首屏最多 5 条"));
            }
            Set<String> keys = new HashSet<>();
            for (int i = 0; i < Math.min(list.size(), 5); i++) {
                JsonNode item = list.get(i);
                String path = "hero.items[" + i + "]";
                if (item == null || !item.isObject()) {
                    errors.add(new FieldErrorItem(path, "格式不正确"));
                    continue;
                }
                ObjectNode heroItem = objectMapper.createObjectNode();
                heroItem.put("key", stableKey(item, "hero", "key-h" + i, keys, path, errors));
                heroItem.put("enabled", bool(item, "enabled", true));
                heroItem.put("sortOrder", sortOrder(item, path, errors));
                heroItem.put("title", limit(text(item, "title"), 40, path + ".title", "首屏标题", errors));
                heroItem.put("subtitle",
                        limit(text(item, "subtitle"), 100, path + ".subtitle", "首屏副标题", errors));
                Long desktop = mediaId(item.get("desktopMediaId"), path + ".desktopMediaId", "桌面图片", errors);
                if (desktop != null) {
                    heroItem.put("desktopMediaId", desktop);
                }
                Long mobile = mediaId(item.get("mobileMediaId"), path + ".mobileMediaId", "手机图片", errors);
                if (mobile != null) {
                    heroItem.put("mobileMediaId", mobile);
                }
                heroItem.put("imageAlt", limit(text(item, "imageAlt"), 120, path + ".imageAlt", "图片替代文字", errors));
                String buttonText = limit(text(item, "buttonText"), 12, path + ".buttonText", "按钮文案", errors);
                ObjectNode link = linkOrNull(item.get("link"), path + ".link", "按钮链接", errors);
                if (!buttonText.isEmpty() && link == null) {
                    errors.add(new FieldErrorItem(path + ".link", "填写按钮文案时必须配置链接"));
                }
                if (buttonText.isEmpty() && link != null && !"NONE".equals(text(link, "type"))) {
                    errors.add(new FieldErrorItem(path + ".buttonText", "配置链接时必须填写按钮文案"));
                }
                heroItem.put("buttonText", buttonText);
                heroItem.set("link", link);
                items.add(heroItem);
            }
        }
        node.set("items", items);
        return node;
    }

    private ObjectNode metrics(JsonNode source, List<FieldErrorItem> errors) {
        ObjectNode node = block(source, "metrics", "企业指标", errors);
        ArrayNode items = objectMapper.createArrayNode();
        JsonNode list = source == null ? null : source.get("items");
        if (list != null && list.isArray()) {
            if (list.size() > 8) {
                errors.add(new FieldErrorItem("metrics.items", "指标最多 8 条"));
            }
            Set<String> keys = new HashSet<>();
            for (int i = 0; i < Math.min(list.size(), 8); i++) {
                JsonNode item = list.get(i);
                String path = "metrics.items[" + i + "]";
                if (item == null || !item.isObject()) {
                    errors.add(new FieldErrorItem(path, "格式不正确"));
                    continue;
                }
                ObjectNode metric = objectMapper.createObjectNode();
                metric.put("key", stableKey(item, "metric", "key-m" + i, keys, path, errors));
                metric.put("sortOrder", sortOrder(item, path, errors));
                metric.put("displayValue",
                        limit(text(item, "displayValue"), 16, path + ".displayValue", "展示值", errors));
                metric.put("unit", limit(text(item, "unit"), 8, path + ".unit", "单位", errors));
                metric.put("label", limit(text(item, "label"), 20, path + ".label", "指标名称", errors));
                metric.put("description",
                        limit(text(item, "description"), 80, path + ".description", "指标说明", errors));
                items.add(metric);
            }
        }
        node.set("items", items);
        return node;
    }

    private ObjectNode services(JsonNode source, List<FieldErrorItem> errors) {
        ObjectNode node = block(source, "services", "产品服务", errors);
        ArrayNode groups = objectMapper.createArrayNode();
        JsonNode list = source == null ? null : source.get("groups");
        if (list != null && list.isArray()) {
            if (list.size() > 3) {
                errors.add(new FieldErrorItem("services.groups", "服务分组最多 3 组"));
            }
            Set<String> keys = new HashSet<>();
            for (int i = 0; i < Math.min(list.size(), 3); i++) {
                JsonNode group = list.get(i);
                String path = "services.groups[" + i + "]";
                if (group == null || !group.isObject()) {
                    errors.add(new FieldErrorItem(path, "格式不正确"));
                    continue;
                }
                ObjectNode item = objectMapper.createObjectNode();
                item.put("key", stableKey(group, "group", "key-g" + i, keys, path, errors));
                item.put("sortOrder", sortOrder(group, path, errors));
                item.put("title", limit(text(group, "title"), 30, path + ".title", "分组标题", errors));
                item.put("description",
                        limit(text(group, "description"), 200, path + ".description", "分组说明", errors));
                Long image = mediaId(group.get("imageMediaId"), path + ".imageMediaId", "分组配图", errors);
                if (image != null) {
                    item.put("imageMediaId", image);
                }
                String contentType = text(group, "contentType");
                if (!Set.of("PRODUCT", "SOLUTION").contains(contentType)) {
                    errors.add(new FieldErrorItem(path + ".contentType", "分组内容类型只能是产品或解决方案"));
                }
                item.put("contentType", contentType);
                ArrayNode ids = idArray(group.get("contentIds"), path + ".contentIds", 6, errors);
                if (ids.isEmpty()) {
                    errors.add(new FieldErrorItem(path + ".contentIds", "请选择 1 到 6 个内容"));
                }
                item.set("contentIds", ids);
                item.set("moreLink", linkOrNull(group.get("moreLink"), path + ".moreLink", "更多链接", errors));
                groups.add(item);
            }
        }
        node.set("groups", groups);
        return node;
    }

    private ObjectNode caseBlock(JsonNode source, List<FieldErrorItem> errors) {
        ObjectNode node = block(source, "cases", "客户案例", errors);
        node.set("contentIds", idArray(source == null ? null : source.get("contentIds"),
                "cases.contentIds", 6, errors));
        return node;
    }

    private ObjectNode customers(JsonNode source, List<FieldErrorItem> errors) {
        ObjectNode node = block(source, "customers", "客户标识", errors);
        node.set("contentIds", idArray(source == null ? null : source.get("contentIds"),
                "customers.contentIds", 30, errors));
        return node;
    }

    private ObjectNode news(JsonNode source, List<FieldErrorItem> errors) {
        ObjectNode node = block(source, "news", "新闻动态", errors);
        String mode = text(source, "mode");
        if (mode.isEmpty()) {
            mode = "SELECTED";
        }
        if (!Set.of("SELECTED", "LATEST").contains(mode)) {
            errors.add(new FieldErrorItem("news.mode", "新闻模式只能是 SELECTED 或 LATEST"));
            mode = "SELECTED";
        }
        node.put("mode", mode);
        int limit = intValue(source, "limit", 3);
        if (limit < 1 || limit > 6) {
            errors.add(new FieldErrorItem("news.limit", "条数需在 1 到 6 之间"));
            limit = Math.max(1, Math.min(6, limit));
        }
        node.put("limit", limit);
        node.set("contentIds", idArray(source == null ? null : source.get("contentIds"),
                "news.contentIds", 6, errors));
        return node;
    }

    private ObjectNode consultation(JsonNode source, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("title", limit(text(source, "title"), 40, "consultation.title", "咨询区标题", errors));
        node.put("subtitle",
                limit(text(source, "subtitle"), 200, "consultation.subtitle", "咨询区副标题", errors));
        node.put("buttonText",
                limit(text(source, "buttonText"), 12, "consultation.buttonText", "按钮文案", errors));
        node.set("link", linkOrNull(source == null ? null : source.get("link"),
                "consultation.link", "咨询动作", errors));
        return node;
    }

    /** 区块公共字段：enabled、title、subtitle。 */
    private ObjectNode block(JsonNode source, String field, String label, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("enabled", bool(source, "enabled", true));
        node.put("title", limit(text(source, "title"), 40, field + ".title", label + "标题", errors));
        node.put("subtitle", limit(text(source, "subtitle"), 200, field + ".subtitle", label + "副标题", errors));
        return node;
    }

    // -------------------------------------------------- 发布校验

    /**
     * 发布前的完整校验：必填、目标存在且已发布、类型匹配。
     * 与保存校验分开，避免运营填一半就被拦住。
     */
    public void validateForPublish(SiteConfigKey key, JsonNode draft) {
        List<FieldErrorItem> errors = new ArrayList<>();
        switch (key) {
            case HOME -> publishHome(draft, errors);
            case NAVIGATION -> publishNavigation(draft, errors);
            case SITE -> publishSite(draft, errors);
            case CONTACT -> publishContact(draft, errors);
            default -> {
            }
        }
        if (!errors.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "发布前请先补全以下内容", errors);
        }
    }

    private void publishSite(JsonNode draft, List<FieldErrorItem> errors) {
        if (text(draft, "siteName").isBlank()) {
            errors.add(new FieldErrorItem("siteName", "站点名称不能为空"));
        }
        if (text(draft, "copyrightText").isBlank()) {
            errors.add(new FieldErrorItem("copyrightText", "版权文案不能为空"));
        }
        JsonNode seo = draft.get("defaultSeo");
        if (text(seo, "title").isBlank()) {
            errors.add(new FieldErrorItem("defaultSeo.title", "默认标题不能为空"));
        }
        if (text(seo, "description").isBlank()) {
            errors.add(new FieldErrorItem("defaultSeo.description", "默认描述不能为空"));
        }
        String icpText = text(draft, "icpText");
        String icpUrl = text(draft, "icpUrl");
        if (!icpText.isEmpty() && icpUrl.isEmpty()) {
            errors.add(new FieldErrorItem("icpUrl", "填写备案号时需同时填写备案链接"));
        }
        if (icpText.isEmpty() && !icpUrl.isEmpty()) {
            errors.add(new FieldErrorItem("icpText", "填写备案链接时需同时填写备案号"));
        }
    }

    private void publishContact(JsonNode draft, List<FieldErrorItem> errors) {
        if (text(draft, "phone").isBlank() && text(draft, "email").isBlank()) {
            errors.add(new FieldErrorItem("phone", "联系电话与邮箱至少填写一项"));
        }
    }

    private void publishNavigation(JsonNode draft, List<FieldErrorItem> errors) {
        JsonNode items = draft.get("items");
        if (items == null || !items.isArray() || items.isEmpty()) {
            errors.add(new FieldErrorItem("items", "请至少配置一项导航"));
            return;
        }
        java.util.Map<String, Boolean> rendered = new java.util.HashMap<>();
        for (int i = 0; i < items.size(); i++) {
            JsonNode item = items.get(i);
            String path = "items[" + i + "]";
            boolean enabled = item.get("enabled") == null || item.get("enabled").asBoolean(true);
            if (!enabled) {
                continue;
            }
            JsonNode children = item.get("children");
            boolean hasChild = false;
            if (children != null && children.isArray()) {
                for (int j = 0; j < children.size(); j++) {
                    JsonNode child = children.get(j);
                    boolean childEnabled = child.get("enabled") == null || child.get("enabled").asBoolean(true);
                    if (!childEnabled) {
                        continue;
                    }
                    if (!links.publishable(child.get("link"), null)) {
                        errors.add(new FieldErrorItem(path + ".children[" + j + "].link",
                                "子菜单目标不存在或未发布"));
                    } else {
                        hasChild = true;
                    }
                }
            }
            String type = text(item.get("link"), "type");
            if (!hasChild) {
                if (!links.publishable(item.get("link"), null) || "NONE".equals(type)) {
                    errors.add(new FieldErrorItem(path + ".link", "导航项必须有有效链接或有效子菜单"));
                }
            }
            rendered.put(path, hasChild || !"NONE".equals(type));
        }
        if (rendered.values().stream().noneMatch(Boolean::booleanValue)) {
            errors.add(new FieldErrorItem("items", "没有可渲染的导航项"));
        }
    }

    private void publishHome(JsonNode draft, List<FieldErrorItem> errors) {
        JsonNode hero = draft.get("hero");
        JsonNode items = hero == null ? null : hero.get("items");
        int validHero = 0;
        if (items != null && items.isArray()) {
            for (int i = 0; i < items.size(); i++) {
                JsonNode item = items.get(i);
                boolean enabled = item.get("enabled") == null || item.get("enabled").asBoolean(true);
                if (!enabled) {
                    continue;
                }
                String path = "hero.items[" + i + "]";
                if (text(item, "title").isBlank()) {
                    errors.add(new FieldErrorItem(path + ".title", "首屏标题不能为空"));
                }
                if (item.get("desktopMediaId") == null) {
                    errors.add(new FieldErrorItem(path + ".desktopMediaId", "启用中的首屏必须有桌面图片"));
                } else if (!mediaExists(item.get("desktopMediaId").asLong())) {
                    errors.add(new FieldErrorItem(path + ".desktopMediaId", "桌面图片不存在"));
                }
                if (!links.publishable(item.get("link"), null)) {
                    errors.add(new FieldErrorItem(path + ".link", "按钮目标不存在或未发布"));
                }
                validHero++;
            }
        }
        if (validHero == 0) {
            errors.add(new FieldErrorItem("hero.items", "正式上线至少需要 1 条有效首屏"));
        }

        // metrics 无引用
        JsonNode services = draft.get("services");
        if (services != null && services.get("groups") != null && services.get("groups").isArray()) {
            for (int i = 0; i < services.get("groups").size(); i++) {
                JsonNode group = services.get("groups").get(i);
                String path = "services.groups[" + i + "]";
                if (text(group, "title").isBlank()) {
                    errors.add(new FieldErrorItem(path + ".title", "分组标题不能为空"));
                }
                String contentType = text(group, "contentType");
                JsonNode ids = group.get("contentIds");
                if (ids == null || ids.isEmpty()) {
                    errors.add(new FieldErrorItem(path + ".contentIds", "分组至少选择 1 个内容"));
                } else {
                    for (int j = 0; j < ids.size(); j++) {
                        long id = ids.get(j).asLong();
                        var row = contents.findPublishedById(id);
                        if (row.isEmpty() || !row.get().contentType().name().equals(contentType)) {
                            errors.add(new FieldErrorItem(path + ".contentIds[" + j + "]",
                                    "内容不存在、未发布或类型不匹配"));
                        }
                    }
                }
                if (!links.publishable(group.get("moreLink"), null)) {
                    errors.add(new FieldErrorItem(path + ".moreLink", "更多链接目标不存在或未发布"));
                }
            }
        }

        publishIdBlock(draft, "cases", "CASE", 6, errors);
        publishIdBlock(draft, "customers", "CUSTOMER", 30, errors);
        JsonNode news = draft.get("news");
        if (news != null && "SELECTED".equals(text(news, "mode"))) {
            publishIdBlock(draft, "news", "NEWS", 6, errors);
        }

        JsonNode consultation = draft.get("consultation");
        if (consultation != null) {
            if (text(consultation, "title").isBlank()) {
                errors.add(new FieldErrorItem("consultation.title", "咨询区标题不能为空"));
            }
            if (text(consultation, "buttonText").isBlank()) {
                errors.add(new FieldErrorItem("consultation.buttonText", "咨询按钮文案不能为空"));
            }
            links.publishable(consultation.get("link"), null);
            String type = text(consultation.get("link"), "type");
        }
    }

    private void publishIdBlock(JsonNode draft, String key, String typeName, int max,
                                List<FieldErrorItem> errors) {
        JsonNode block = draft.get(key);
        if (block == null) {
            return;
        }
        JsonNode ids = block.get("contentIds");
        if (ids != null && ids.isArray()) {
            for (int i = 0; i < ids.size(); i++) {
                long id = ids.get(i).asLong();
                var row = contents.findPublishedById(id);
                if (row.isEmpty() || !row.get().contentType().name().equals(typeName)) {
                    errors.add(new FieldErrorItem(key + ".contentIds[" + i + "]", "内容不存在、未发布或类型不匹配"));
                }
            }
        }
    }

    // -------------------------------------------------- 校验辅助工具

    private ObjectNode linkOrNull(JsonNode linkNode, String path, String label, List<FieldErrorItem> errors) {
        if (linkNode == null || linkNode.isNull() || !linkNode.isObject()) {
            return null;
        }
        String error = links.validateDraft(linkNode);
        if (error != null) {
            errors.add(new FieldErrorItem(path, label + error));
            return null;
        }
        return (ObjectNode) linkNode;
    }

    private Long mediaId(JsonNode node, String path, String label, List<FieldErrorItem> errors) {
        if (node == null || node.isNull()) {
            return null;
        }
        Long id = node.isNumber() ? node.asLong() : parseLong(node.asString());
        if (id == null || id <= 0) {
            errors.add(new FieldErrorItem(path, label + " ID 不合法"));
            return null;
        }
        if (!mediaExists(id)) {
            errors.add(new FieldErrorItem(path, label + "不存在，请重新选择"));
            return null;
        }
        return id;
    }

    private boolean mediaExists(long id) {
        try {
            mediaService.require(id);
            return true;
        } catch (ApiException ex) {
            return false;
        }
    }

    private ArrayNode idArray(JsonNode source, String path, int max, List<FieldErrorItem> errors) {
        ArrayNode result = objectMapper.createArrayNode();
        if (source == null || !source.isArray()) {
            return result;
        }
        if (source.size() > max) {
            errors.add(new FieldErrorItem(path, "最多选择 " + max + " 项"));
        }
        Set<Long> seen = new HashSet<>();
        for (int i = 0; i < Math.min(source.size(), max); i++) {
            JsonNode node = source.get(i);
            Long id = node.isNumber() ? node.asLong() : parseLong(node.asString());
            if (id == null || id <= 0) {
                errors.add(new FieldErrorItem(path + "[" + i + "]", "内容 ID 不合法"));
                continue;
            }
            if (!seen.add(id)) {
                errors.add(new FieldErrorItem(path + "[" + i + "]", "同一内容不能重复选择"));
                continue;
            }
            result.add(id);
        }
        return result;
    }

    private Long parseLong(String raw) {
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String stableKey(JsonNode item, String prefix, String fallback,
                            Set<String> used, String path, List<FieldErrorItem> errors) {
        String key = text(item, "key");
        if (key.isEmpty() || !key.matches("^[a-z0-9-]{4,40}$") || used.contains(key)) {
            key = fallback + "-" + Integer.toHexString(ThreadLocalRandom.current().nextInt(0x10000, 0xfffff));
            while (used.contains(key)) {
                key = prefix + "-" + Integer.toHexString(ThreadLocalRandom.current().nextInt(0x10000, 0xfffff));
            }
        }
        used.add(key);
        return key;
    }

    private int sortOrder(JsonNode item, String path, List<FieldErrorItem> errors) {
        int value = intValue(item, "sortOrder", 100);
        if (value < 0 || value > 9999) {
            errors.add(new FieldErrorItem(path + ".sortOrder", "排序需在 0 到 9999 之间"));
            return 100;
        }
        return value;
    }

    private int intValue(JsonNode node, String field, int fallback) {
        if (node == null) {
            return fallback;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return fallback;
        }
        return value.isNumber() ? value.asInt() : fallback;
    }

    private boolean bool(JsonNode node, String field, boolean fallback) {
        if (node == null) {
            return fallback;
        }
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? fallback : value.asBoolean(fallback);
    }

    private String externalOrEmpty(JsonNode node, String field, String path, String label,
                                    List<FieldErrorItem> errors) {
        String url = text(node, field).trim();
        if (url.isEmpty()) {
            return "";
        }
        String lower = url.toLowerCase();
        if (!lower.startsWith("http://") && !lower.startsWith("https://") || url.length() > 500) {
            errors.add(new FieldErrorItem(path, label + "必须是 http/https 完整地址"));
            return "";
        }
        return url;
    }

    private String text(JsonNode node, String field) {
        if (node == null) {
            return "";
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return "";
        }
        return value.isString() ? value.asString().trim() : value.asString();
    }

    private String limit(String value, int max, String path, String label, List<FieldErrorItem> errors) {
        if (value.length() > max) {
            errors.add(new FieldErrorItem(path, label + "不能超过 " + max + " 字"));
        }
        return value;
    }

}
```

### `website/server/src/main/java/com/company/cms/site/HomeAggregateService.java` (新增 `sectionOrder` 输出支持)

```java
package com.company.cms.site;

import com.company.cms.common.ApiException;
import com.company.cms.content.ContentRepository;
import com.company.cms.content.ContentRow;
import com.company.cms.media.MediaRepository;
import com.company.cms.media.MediaRow;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 首页公开聚合（文档 6.4 / 10.4）。
 *
 * 所有关联内容与图片一次性批量读取，避免按卡片逐条查库；
 * 失效引用在输出阶段过滤，不能生成死链接。
 */
@Service
public class HomeAggregateService {

    private final SiteConfigRepository configs;
    private final ContentRepository contents;
    private final MediaRepository mediaRepository;
    private final LinkResolver links;

    public HomeAggregateService(SiteConfigRepository configs, ContentRepository contents,
                                MediaRepository mediaRepository, LinkResolver links) {
        this.configs = configs;
        this.contents = contents;
        this.mediaRepository = mediaRepository;
        this.links = links;
    }

    public Map<String, Object> publicHome() {
        SiteConfigRow row = configs.find(SiteConfigKey.HOME)
                .orElseThrow(() -> ApiException.notFound("首页尚未初始化"));
        if (!row.status().isPublished() || row.published() == null) {
            throw ApiException.notFound("首页尚未发布");
        }
        return build(row.published(), row.publishedVersion(), row.publishedAt());
    }

    /**
     * 草稿预览：首屏与各区块获取当前草稿，关联内容仍只取各自已发布版本（文档 5.4）。
     * 与公开接口共用同一段聚合逻辑，避免预览和线上出现两套结构。
     */
    public Map<String, Object> previewHome() {
        SiteConfigRow row = configs.find(SiteConfigKey.HOME)
                .orElseThrow(() -> ApiException.notFound("首页尚未初始化"));
        JsonNode draft = row.draft();
        if (draft == null || draft.isEmpty()) {
            throw ApiException.notFound("首页草稿尚未填写");
        }
        return build(draft, row.draftVersion(), row.publishedAt());
    }

    private Map<String, Object> build(JsonNode cfg, Integer version, LocalDateTime publishedAt) {
        boolean formEnabled = formEnabled();

        // ---- 第一批：关联内容（一次 IN 查询）
        Set<Long> contentIds = new LinkedHashSet<>();
        collectIds(cfg.get("services"), "groups", contentIds);
        collectIds(cfg.get("cases"), null, contentIds);
        collectIds(cfg.get("customers"), null, contentIds);
        collectIds(cfg.get("news"), null, contentIds);
        Map<Long, ContentRow> contentMap = new HashMap<>(contents.findPublishedByIds(contentIds).stream()
                .collect(Collectors.toMap(ContentRow::id, Function.identity())));
        JsonNode newsBlock = cfg.get("news");
        if (newsBlock != null && enabled(newsBlock) && "LATEST".equals(text(newsBlock, "mode"))) {
            int limit = newsBlock.get("limit") != null ? Math.max(1, Math.min(6, newsBlock.get("limit").asInt())) : 3;
            for (ContentRow r : contents.findLatestPublishedNews(limit)) {
                contentMap.put(r.id(), r);
            }
        }

        // ---- 第二批：媒体（首屏、分组配图、卡片封面）
        Set<Long> mediaIds = new LinkedHashSet<>();
        JsonNode heroItems = cfg.get("hero") == null ? null : cfg.get("hero").get("items");
        if (heroItems != null && heroItems.isArray()) {
            for (JsonNode item : heroItems) {
                addIfNumber(mediaIds, item.get("desktopMediaId"));
                addIfNumber(mediaIds, item.get("mobileMediaId"));
            }
        }
        JsonNode groups = cfg.get("services") == null ? null : cfg.get("services").get("groups");
        if (groups != null && groups.isArray()) {
            for (JsonNode group : groups) {
                addIfNumber(mediaIds, group.get("imageMediaId"));
            }
        }
        for (ContentRow content : contentMap.values()) {
            JsonNode json = content.published();
            if (json != null) {
                addIfNumber(mediaIds, json.get("coverMediaId"));
                addIfNumber(mediaIds, json.get("logoMediaId"));
            }
        }
        Map<Long, MediaRow> mediaMap = mediaRepository.findByIds(mediaIds).stream()
                .collect(Collectors.toMap(MediaRow::id, Function.identity()));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("version", version);
        data.put("publishedAt", publishedAt == null ? null
                : publishedAt.atOffset(ZoneOffset.UTC).toString());

        // ---- 各区块先组装，再据此判断锚点是否可用
        Map<String, Object> hero = hero(cfg.get("hero"), mediaMap);
        Map<String, Object> metrics = metrics(cfg.get("metrics"));
        Map<String, Object> services = services(cfg.get("services"), contentMap, mediaMap, formEnabled);
        Map<String, Object> cases = cases(cfg.get("cases"), contentMap, mediaMap);
        Map<String, Object> customers = customers(cfg.get("customers"), contentMap, mediaMap);
        Map<String, Object> news = news(cfg.get("news"), contentMap, mediaMap);

        Set<String> anchors = new HashSet<>();
        if (metrics != null) {
            anchors.add("metrics");
        }
        if (services != null) {
            anchors.add("services");
        }
        if (cases != null) {
            anchors.add("cases");
        }
        if (customers != null) {
            anchors.add("customers");
        }
        if (news != null) {
            anchors.add("news");
        }
        anchors.add("consultation");

        // 首屏与咨询动作依赖锚点集合，放在最后解析
        enrichHeroLinks(hero, formEnabled, anchors);
        List<String> order = new ArrayList<>();
        JsonNode orderNode = cfg.get("sectionOrder");
        if (orderNode != null && orderNode.isArray()) {
            for (JsonNode n : orderNode) {
                if (n != null && n.isString() && !order.contains(n.asString())) {
                    order.add(n.asString());
                }
            }
        }
        for (String defKey : List.of("metrics", "services", "cases", "customers", "news", "consultation")) {
            if (!order.contains(defKey)) {
                order.add(defKey);
            }
        }
        data.put("sectionOrder", order);
        data.put("hero", hero);
        data.put("metrics", metrics);
        data.put("services", services);
        data.put("cases", cases);
        data.put("customers", customers);
        data.put("news", news);
        data.put("consultation", consultation(cfg.get("consultation"), formEnabled, anchors));
        return data;
    }

    private void collectIds(JsonNode block, String arrayField, Set<Long> target) {
        if (block == null || !block.isObject()) {
            return;
        }
        if (arrayField == null) {
            addIds(block.get("contentIds"), target);
            return;
        }
        JsonNode list = block.get(arrayField);
        if (list != null && list.isArray()) {
            for (JsonNode item : list) {
                addIds(item.get("contentIds"), target);
            }
        }
    }

    private void addIds(JsonNode array, Set<Long> target) {
        if (array == null || !array.isArray()) {
            return;
        }
        for (JsonNode node : array) {
            if (node != null && node.isNumber()) {
                target.add(node.asLong());
            }
        }
    }

    private void addIfNumber(Set<Long> target, JsonNode node) {
        if (node != null && node.isNumber()) {
            target.add(node.asLong());
        }
    }

    // -------------------------------------------------- hero

    private Map<String, Object> hero(JsonNode block, Map<Long, MediaRow> mediaMap) {
        List<Map<String, Object>> items = new ArrayList<>();
        JsonNode list = block == null ? null : block.get("items");
        if (list != null && list.isArray()) {
            for (JsonNode item : list) {
                boolean enabled = item.get("enabled") == null || item.get("enabled").asBoolean(true);
                if (!enabled) {
                    continue;
                }
                String title = text(item, "title");
                Map<String, Object> desktop = media(item.get("desktopMediaId"), mediaMap,
                        text(item, "imageAlt").isBlank() ? title : text(item, "imageAlt"));
                if (title.isBlank() || desktop == null) {
                    continue;
                }
                Map<String, Object> view = new LinkedHashMap<>();
                view.put("key", text(item, "key"));
                view.put("title", title);
                view.put("subtitle", text(item, "subtitle"));
                view.put("desktopImage", desktop);
                view.put("mobileImage", media(item.get("mobileMediaId"), mediaMap, text(item, "imageAlt")));
                view.put("buttonText", text(item, "buttonText"));
                view.put("_link", item.get("link"));
                items.add(view);
            }
        }
        sortByOrder(block == null ? null : block.get("items"), items);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", items);
        return result;
    }

    /** 首屏排序按 sortOrder，再按 key 字典序稳定。 */
    private void sortByOrder(JsonNode source, List<Map<String, Object>> items) {
        if (source == null || !source.isArray() || items.size() < 2) {
            return;
        }
        Map<String, Integer> orders = new HashMap<>();
        for (JsonNode item : source) {
            orders.put(text(item, "key"), item.get("sortOrder") == null ? 100 : item.get("sortOrder").asInt());
        }
        items.sort((a, b) -> {
            int left = orders.getOrDefault(String.valueOf(a.get("key")), 100);
            int right = orders.getOrDefault(String.valueOf(b.get("key")), 100);
            if (left != right) {
                return Integer.compare(left, right);
            }
            return String.valueOf(a.get("key")).compareTo(String.valueOf(b.get("key")));
        });
    }

    private void enrichHeroLinks(Map<String, Object> hero, boolean formEnabled, Set<String> anchors) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) hero.get("items");
        for (Map<String, Object> item : items) {
            Object raw = item.remove("_link");
            Map<String, Object> action = links.resolve((JsonNode) raw, null, formEnabled, anchors);
            item.put("link", action);
            if (action == null) {
                item.put("buttonText", "");
            }
            // 稳定字段顺序：把 link 移到 buttonText 之后即可，Map 顺序不影响 JSON 语义
            item.putIfAbsent("link", null);
        }
    }

    // -------------------------------------------------- metrics

    private Map<String, Object> metrics(JsonNode block) {
        if (!enabled(block)) {
            return null;
        }
        List<Map<String, Object>> items = new ArrayList<>();
        JsonNode list = block == null ? null : block.get("items");
        if (list != null && list.isArray()) {
            for (JsonNode item : list) {
                String value = text(item, "displayValue");
                String label = text(item, "label");
                if (value.isBlank() || label.isBlank()) {
                    continue;
                }
                Map<String, Object> view = new LinkedHashMap<>();
                view.put("key", text(item, "key"));
                view.put("displayValue", value);
                view.put("unit", text(item, "unit"));
                view.put("label", label);
                view.put("description", text(item, "description"));
                items.add(view);
            }
        }
        if (items.isEmpty()) {
            return null;
        }
        sortByOrder(list, items);
        Map<String, Object> result = blockView(block);
        result.put("items", items);
        return result;
    }

    // -------------------------------------------------- services

    private Map<String, Object> services(JsonNode block, Map<Long, ContentRow> contentMap,
                                          Map<Long, MediaRow> mediaMap, boolean formEnabled) {
        if (!enabled(block)) {
            return null;
        }
        List<Map<String, Object>> groups = new ArrayList<>();
        JsonNode list = block == null ? null : block.get("groups");
        if (list != null && list.isArray()) {
            for (JsonNode group : list) {
                String contentType = text(group, "contentType");
                List<Map<String, Object>> cards = new ArrayList<>();
                JsonNode ids = group.get("contentIds");
                if (ids != null && ids.isArray()) {
                    for (JsonNode idNode : ids) {
                        ContentRow content = contentMap.get(idNode.asLong());
                        if (content == null || !content.contentType().name().equals(contentType)) {
                            continue;
                        }
                        cards.add(card(content, mediaMap));
                    }
                }
                if (cards.isEmpty()) {
                    continue;
                }
                Map<String, Object> view = new LinkedHashMap<>();
                view.put("key", text(group, "key"));
                view.put("title", text(group, "title"));
                view.put("description", text(group, "description"));
                view.put("image", media(group.get("imageMediaId"), mediaMap, text(group, "title")));
                view.put("moreLink", links.resolve(group.get("moreLink"), null, formEnabled, Set.of()));
                view.put("items", cards);
                groups.add(view);
            }
        }
        if (groups.isEmpty()) {
            return null;
        }
        sortByOrder(list, groups);
        Map<String, Object> result = blockView(block);
        result.put("groups", groups);
        return result;
    }

    private Map<String, Object> card(ContentRow content, Map<Long, MediaRow> mediaMap) {
        JsonNode json = content.published();
        Map<String, Object> card = new LinkedHashMap<>();
        String id = String.valueOf(content.id());
        card.put("id", id);
        card.put("contentType", content.contentType().name());
        card.put("title", text(json, "title"));
        card.put("summary", text(json, "summary"));
        card.put("cover", cover(json, mediaMap));
        card.put("url", contentUrl(content));
        return card;
    }

    private String contentUrl(ContentRow content) {
        String base = switch (content.contentType()) {
            case PRODUCT -> "/products/";
            case SOLUTION -> "/solutions/";
            case CASE -> "/cases/";
            case NEWS -> "/news/";
            default -> null;
        };
        return base == null ? null : base + content.id();
    }

    // -------------------------------------------------- cases

    private Map<String, Object> cases(JsonNode block, Map<Long, ContentRow> contentMap,
                                      Map<Long, MediaRow> mediaMap) {
        if (!enabled(block)) {
            return null;
        }
        List<Map<String, Object>> items = new ArrayList<>();
        JsonNode ids = block == null ? null : block.get("contentIds");
        if (ids != null && ids.isArray()) {
            for (JsonNode idNode : ids) {
                ContentRow content = contentMap.get(idNode.asLong());
                if (content == null || content.contentType() != com.company.cms.content.ContentType.CASE) {
                    continue;
                }
                JsonNode json = content.published();
                Map<String, Object> view = card(content, mediaMap);
                view.put("customerName", text(json, "customerName"));
                view.put("industry", text(json, "industry"));
                JsonNode outcomes = json == null ? null : json.get("outcomes");
                view.put("outcomes", outcomes == null || outcomes.isNull() ? List.of() : outcomes);
                items.add(view);
            }
        }
        if (items.isEmpty()) {
            return null;
        }
        Map<String, Object> result = blockView(block);
        result.put("items", items);
        result.put("moreUrl", "/cases");
        return result;
    }

    // -------------------------------------------------- customers

    private Map<String, Object> customers(JsonNode block, Map<Long, ContentRow> contentMap,
                                          Map<Long, MediaRow> mediaMap) {
        if (!enabled(block)) {
            return null;
        }
        List<Map<String, Object>> items = new ArrayList<>();
        JsonNode ids = block == null ? null : block.get("contentIds");
        if (ids != null && ids.isArray()) {
            for (JsonNode idNode : ids) {
                ContentRow content = contentMap.get(idNode.asLong());
                if (content == null || content.contentType() != com.company.cms.content.ContentType.CUSTOMER) {
                    continue;
                }
                JsonNode json = content.published();
                Map<String, Object> logo = media(json == null ? null : json.get("logoMediaId"), mediaMap,
                        text(json, "logoAlt").isBlank() ? text(json, "title") : text(json, "logoAlt"));
                if (logo == null) {
                    continue;
                }
                Map<String, Object> view = new LinkedHashMap<>();
                view.put("id", String.valueOf(content.id()));
                view.put("name", text(json, "title"));
                view.put("logo", logo);
                String website = text(json, "websiteUrl");
                view.put("url", website.isBlank() ? null : website);
                items.add(view);
            }
        }
        if (items.isEmpty()) {
            return null;
        }
        Map<String, Object> result = blockView(block);
        result.put("items", items);
        return result;
    }

    // -------------------------------------------------- news

    private Map<String, Object> news(JsonNode block, Map<Long, ContentRow> contentMap, Map<Long, MediaRow> mediaMap) {
        if (!enabled(block)) {
            return null;
        }
        String mode = text(block, "mode").isEmpty() ? "SELECTED" : text(block, "mode");
        int limit = block != null && block.get("limit") != null ? Math.max(1, Math.min(6, block.get("limit").asInt())) : 3;
        List<Map<String, Object>> items = new ArrayList<>();
        if ("LATEST".equals(mode)) {
            for (ContentRow content : contents.findLatestPublishedNews(limit)) {
                items.add(newsItem(content, mediaMap));
            }
        } else {
            JsonNode ids = block == null ? null : block.get("contentIds");
            if (ids != null && ids.isArray()) {
                for (JsonNode idNode : ids) {
                    if (items.size() >= limit) {
                        break;
                    }
                    ContentRow content = contentMap.get(idNode.asLong());
                    if (content == null || content.contentType() != com.company.cms.content.ContentType.NEWS) {
                        continue;
                    }
                    items.add(newsItem(content, mediaMap));
                }
            }
        }
        if (items.isEmpty()) {
            return null;
        }
        Map<String, Object> result = blockView(block);
        result.put("mode", mode);
        result.put("items", items);
        result.put("moreUrl", "/news");
        return result;
    }

    private Map<String, Object> newsItem(ContentRow content, Map<Long, MediaRow> mediaMap) {
        JsonNode json = content.published();
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", String.valueOf(content.id()));
        item.put("title", text(json, "title"));
        item.put("summary", text(json, "summary"));
        item.put("displayTime", text(json, "displayTime"));
        item.put("url", "/news/" + content.id());
        item.put("cover", cover(json, mediaMap));
        return item;
    }

    // -------------------------------------------------- consultation

    private Map<String, Object> consultation(JsonNode block, boolean formEnabled, Set<String> anchors) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("title", text(block, "title"));
        view.put("subtitle", text(block, "subtitle"));
        String buttonText = text(block, "buttonText");
        Map<String, Object> action = links.resolve(block == null ? null : block.get("link"),
                null, formEnabled, anchors);
        if (action == null) {
            // 表单关闭或目标失效时退到联系页，不留空按钮
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("type", "PAGE");
            fallback.put("url", "/contact");
            action = fallback;
            if (buttonText.isBlank()) {
                buttonText = "联系我们";
            }
        }
        view.put("buttonText", buttonText);
        view.put("link", action);
        return view;
    }

    // -------------------------------------------------- 工具

    private Map<String, Object> blockView(JsonNode block) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("title", text(block, "title"));
        view.put("subtitle", text(block, "subtitle"));
        return view;
    }

    private boolean enabled(JsonNode block) {
        return block != null && (block.get("enabled") == null || block.get("enabled").asBoolean(true));
    }

    private Map<String, Object> cover(JsonNode json, Map<Long, MediaRow> mediaMap) {
        JsonNode idNode = json == null ? null : json.get("coverMediaId");
        String alt = text(json, "coverAlt");
        if (alt.isBlank()) {
            alt = text(json, "title");
        }
        return media(idNode, mediaMap, alt);
    }

    private Map<String, Object> media(JsonNode idNode, Map<Long, MediaRow> mediaMap, String alt) {
        if (idNode == null || !idNode.isNumber()) {
            return null;
        }
        MediaRow media = mediaMap.get(idNode.asLong());
        if (media == null) {
            return null;
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", String.valueOf(media.id()));
        view.put("url", "/api/public/media/" + media.id());
        view.put("alt", alt == null ? "" : alt);
        view.put("width", media.width());
        view.put("height", media.height());
        view.put("type", media.mediaType());
        view.put("durationMs", media.durationMs());
        return view;
    }

    private boolean formEnabled() {
        return configs.find(SiteConfigKey.CONTACT)
                .map(SiteConfigRow::published)
                .map(json -> json.get("formEnabled") != null && json.get("formEnabled").asBoolean(false))
                .orElse(false);
    }

    private String text(JsonNode node, String field) {
        if (node == null) {
            return "";
        }
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asString().trim();
    }

}
```
