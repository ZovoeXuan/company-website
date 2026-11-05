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
