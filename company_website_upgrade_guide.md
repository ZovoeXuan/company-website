# 企业官网与后台管理系统 (`ZovoeXuan/company-website`) 全栈大整合升级手册

> **整合范围**：本升级包已将前期深度源码审计修复、**全站实景交互预演与全局可视化编辑台 (Visual Live Studio)** 以及 **全套 18 项前后端进阶视觉与生产力特性** 全部整合进 **1 个一键升级脚本 (`apply_website_upgrades.py`)**，共覆盖 **37 个前后端核心源码文件**。

## 一、一键部署与 GitHub 提交步骤

1. 下载本会话交付的 **`apply_website_upgrades.py`**，放入您的本地项目根目录（即包含 `website/` 的目录，如 `D:\中转站\公司官网\`）。
2. 在该目录下打开终端（PowerShell 或 CMD），执行：
   ```bash
   python apply_website_upgrades.py
   ```
   *脚本会自动将原有同名文件备份至 `.upgrade_backup_<时间戳>/` 文件夹，并将全部 37 个升级后的前后端文件写入对应路径。*
3. 本地验证与一键推送到 GitHub：
   ```bash
   git status
   git add .
   git commit -m "feat: 整合全站实景交互预演工作台与18项前后端视觉及CMS升级"
   git push origin master
   ```

## 二、本次全栈大整合覆盖的 37 个文件与核心升级清单

| 序号 | 模块端 | 文件路径 | 核心修复与新增功能摘要 |
| :--- | :--- | :--- | :--- |
| 1 | 管理后台 | `website/admin/src/api/content.ts` | 完整升级实现（共 232 行） |
| 2 | 管理后台 | `website/admin/src/api/http.ts` | 完整升级实现（共 145 行） |
| 3 | 管理后台 | `website/admin/src/components/ConfigPanel.vue` | 完整升级实现（共 139 行） |
| 4 | 管理后台 | `website/admin/src/components/LinkEditor.vue` | 完整升级实现（共 226 行） |
| 5 | 管理后台 | `website/admin/src/components/MediaPicker.vue` | 完整升级实现（共 330 行） |
| 6 | 管理后台 | `website/admin/src/layouts/AdminLayout.vue` | 完整升级实现（共 718 行） |
| 7 | 管理后台 | `website/admin/src/views/AboutPageView.vue` | 完整升级实现（共 173 行） |
| 8 | 管理后台 | `website/admin/src/views/AuditLogsView.vue` | 完整升级实现（共 249 行） |
| 9 | 管理后台 | `website/admin/src/views/CategoryView.vue` | 完整升级实现（共 256 行） |
| 10 | 管理后台 | `website/admin/src/views/ContactPageView.vue` | 完整升级实现（共 146 行） |
| 11 | 管理后台 | `website/admin/src/views/ContentEditorView.vue` | 完整升级实现（共 725 行） |
| 12 | 管理后台 | `website/admin/src/views/ContentListView.vue` | 完整升级实现（共 447 行） |
| 13 | 管理后台 | `website/admin/src/views/DashboardView.vue` | 完整升级实现（共 398 行） |
| 14 | 管理后台 | `website/admin/src/views/HomeManageView.vue` | 完整升级实现（共 709 行） |
| 15 | 管理后台 | `website/admin/src/views/LeadListView.vue` | 完整升级实现（共 514 行） |
| 16 | 管理后台 | `website/admin/src/views/LoginView.vue` | 完整升级实现（共 253 行） |
| 17 | 管理后台 | `website/admin/src/views/MediaView.vue` | 完整升级实现（共 417 行） |
| 18 | 管理后台 | `website/admin/src/views/NavigationView.vue` | 完整升级实现（共 448 行） |
| 19 | 管理后台 | `website/admin/src/views/PreviewHomeView.vue` | 完整升级实现（共 3420 行） |
| 20 | 管理后台 | `website/admin/src/views/SiteSettingsView.vue` | 完整升级实现（共 515 行） |
| 21 | 管理后台 | `website/admin/src/views/UsersView.vue` | 完整升级实现（共 356 行） |
| 22 | Java 后端 | `website/server/src/main/java/com/company/cms/dashboard/DashboardService.java` | 完整升级实现（共 62 行） |
| 23 | Java 后端 | `website/server/src/main/java/com/company/cms/site/HomeAggregateService.java` | 完整升级实现（共 575 行） |
| 24 | Java 后端 | `website/server/src/main/java/com/company/cms/site/SiteConfigValidator.java` | 完整升级实现（共 961 行） |
| 25 | 共享组件 | `website/shared/ArticleBody.vue` | 完整升级实现（共 717 行） |
| 26 | 官网前台 | `website/web/src/components/ConsultDialog.vue` | 完整升级实现（共 367 行） |
| 27 | 官网前台 | `website/web/src/components/ConsultForm.vue` | 完整升级实现（共 421 行） |
| 28 | 官网前台 | `website/web/src/components/FloatingActions.vue` | 完整升级实现（共 311 行） |
| 29 | 官网前台 | `website/web/src/components/RoiCalculatorModal.vue` | 完整升级实现（共 531 行） |
| 30 | 官网前台 | `website/web/src/components/SiteFooter.vue` | 完整升级实现（共 479 行） |
| 31 | 官网前台 | `website/web/src/components/SiteHeader.vue` | 完整升级实现（共 1501 行） |
| 32 | 官网前台 | `website/web/src/composables/consult.ts` | 完整升级实现（共 78 行） |
| 33 | 官网前台 | `website/web/src/composables/usePageMeta.ts` | 完整升级实现（共 70 行） |
| 34 | 官网前台 | `website/web/src/data/types.ts` | 完整升级实现（共 285 行） |
| 35 | 官网前台 | `website/web/src/layouts/SiteLayout.vue` | 完整升级实现（共 106 行） |
| 36 | 官网前台 | `website/web/src/views/AboutView.vue` | 完整升级实现（共 387 行） |
| 37 | 官网前台 | `website/web/src/views/HomeView.vue` | 完整升级实现（共 1341 行） |

## 三、核心整合亮点速览

### 1. 官网前台 (`website/web` & `website/shared`)
- **全站 `⌘K / Ctrl+K` 聚光灯即时搜索 (`SiteHeader.vue`)**：支持快捷键或顶部搜索胶囊一键唤醒，并行检索已发布的产品、解决方案、客户案例与新闻动态，支持键盘上下键选择与快捷功能直达。
- **五列业务全景大菜单动态化与中英双语切换 (`SiteHeader.vue`)**：优先消费后台 `NAVIGATION.megaColumns` 与 `megaCta` 配置（未配置时平滑回退默认五列菜单），支持角标与演示视频弹窗，并提供 `中 / EN` 导航双语切换。
- **互动式「企业数字化 ROI 降本增效测算器」(`RoiCalculatorModal.vue` & `HomeView.vue`)**：支持五大行业场景切换与车队/设备规模滑块实时测算年化节省成本、安全率提升与预计回本周期，点击按钮可将测算参数一键带入「小七」咨询表单。
- **旗舰级首页微交互与模块动态编排 (`HomeView.vue`)**：
  - 完整支持后台 Visual Live Studio 保存的 `sectionOrder` 自定义模块顺序；
  - 首屏视频/图片轮播支持自动播放进度条与悬停暂停；
  - 企业关键指标 (`metrics`) 进入视口时触发平滑 ease-out 数字递增动画；
  - 客户案例 (`cases`) 支持行业胶囊快速筛选；
  - 合作客户标识墙 (`customers`) 支持「无限无缝跑马灯」与「矩阵网格」双视图切换。
- **关于我们交互式时间轴 (`AboutView.vue`)**：支持按年份胶囊筛选发展历程节点、使命愿景 Bento 双卡与资质荣誉检索墙。
- **详情页深度阅读体验 (`ArticleBody.vue`)**：修复 `.cms-prose` 样式不生效 Bug，新增顶部阅读进度条、自动提取 H2/H3 悬浮目录 (TOC)、代码块一键复制、正文图片点击高清灯箱放大及一键生成文章分享海报卡。
- **右下角磁吸悬浮动作坞 (`FloatingActions.vue`)**：集成小七 AI 顾问唤醒、ROI 测算器入口、全国热线/邮箱悬浮卡及带阅读进度环的平滑回到顶部按钮。

### 2. 管理后台 CMS (`website/admin`)
- **全站实景交互预演与全局可视化编辑台 (`PreviewHomeView.vue`)**：跨 `SITE`、`NAVIGATION`、`HOME`、`CONTACT` 四大配置双向同步，支持画布模块上下移动排序、显隐开关、文字直改、点击换图/换视频、链接绑定、30 步 Undo/Redo、桌面/平板/手机三视口切换、草稿/线上对比与发布前合规体检。
- **全局 `⌘K / Ctrl+K` 快捷命令面板 (`AdminLayout.vue`)**：任意页面按下快捷键即可模糊搜索并一键跳转到任意管理模块、新建内容或启动实景预演台。
- **首页管理与全景导航配置 (`HomeManageView.vue` & `NavigationView.vue`)**：首页管理新增 `sectionOrder` 模块顺序与显隐管理及「一键填入行业标杆指标模板」；导航管理新增「五列业务全景下拉菜单 (`megaColumns`)」可视化编辑页签。
- **全站设置 Google SERP 预览与页脚双二维码 (`SiteSettingsView.vue`)**：修复清空 Logo/Favicon 失效问题，增加 ICP/公安备案成对校验、页脚分组/链接上下排序、页脚双二维码选图及 Google/百度搜索结果实时预览卡片。
- **分屏富文本内容编辑器 (`ContentEditorView.vue`)**：内置快捷排版工具栏（H2/H3/粗体/引用/列表/表格/插入受控 `data-media-id` 素材库图片）与左右分屏 `ArticleBody` 实时预览。
- **内容批量操作与一键克隆 (`ContentListView.vue` & `api/content.ts`)**：支持表格多选批量发布、批量下线以及单条内容「一键克隆副本」。
- **咨询线索看板 (Kanban) 与 SLA 超时预警 (`LeadListView.vue`)**：支持「表格视图 / 看板视图」一键切换、超过 24 小时未处理 `NEW` 线索自动标红预警、点击详情触发审计解密完整电话及导出当前页 CSV。
- **素材库拖拽批量上传与引用追踪抽屉 (`MediaView.vue` & `MediaPicker.vue`)**：支持拖拽批量上传图片/视频、查看素材被哪些草稿或发布内容引用（防误删保护）以及素材选择器完整分页浏览。

### 3. Java 后端服务 (`website/server`)
- **`SiteConfigValidator.java` & `HomeAggregateService.java`**：新增 `HOME.sectionOrder` 模块自定义顺序与 `SITE.qrCodes` 页脚双二维码的校验、归一化持久化与公开/预览聚合输出。
- **`DashboardService.java`**：修复草稿统计口径，将已发布但含未发布草稿修改（`draft_version > published_version`）的内容一并纳入待发布统计。

## 四、全量升级文件完整源码参考

### `website/shared/ArticleBody.vue`

```vue
<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = withDefaults(
  defineProps<{
    html?: string | null
    showToc?: boolean
    showProgress?: boolean
    showTools?: boolean
    title?: string
  }>(),
  {
    html: '',
    showToc: true,
    showProgress: true,
    showTools: true,
    title: '',
  },
)

interface TocItem {
  id: string
  text: string
  level: 2 | 3
}

const articleRef = ref<HTMLElement | null>(null)
const tocItems = ref<TocItem[]>([])
const activeId = ref('')
const readingProgress = ref(0)
const lightboxUrl = ref('')
const lightboxAlt = ref('')
const copiedLink = ref(false)
const sharePosterOpen = ref(false)

const estimatedMinutes = computed(() => {
  const plain = (props.html ?? '').replace(/<[^>]+>/g, '').trim()
  if (!plain) return 1
  return Math.max(1, Math.ceil(plain.length / 400))
})

function enhanceContent() {
  const root = articleRef.value
  if (!root) return

  // 1. 提取 H2 / H3 生成锚点目录
  const headings = Array.from(root.querySelectorAll('h2, h3')) as HTMLElement[]
  const list: TocItem[] = []
  headings.forEach((el, idx) => {
    const text = (el.textContent ?? '').trim()
    if (!text) return
    const id = el.id || `cms-heading-${idx}`
    el.id = id
    list.push({
      id,
      text,
      level: el.tagName.toLowerCase() === 'h3' ? 3 : 2,
    })
  })
  tocItems.value = list
  if (list.length > 0 && !activeId.value) {
    activeId.value = list[0].id
  }

  // 2. 为代码块注入一键复制按钮
  const pres = Array.from(root.querySelectorAll('pre')) as HTMLElement[]
  pres.forEach((pre) => {
    if (pre.dataset.enhanced === '1') return
    pre.dataset.enhanced = '1'
    pre.style.position = 'relative'
    const btn = document.createElement('button')
    btn.type = 'button'
    btn.className = 'cms-code-copy'
    btn.textContent = '复制代码'
    btn.addEventListener('click', async () => {
      const codeText = pre.querySelector('code')?.textContent ?? pre.textContent ?? ''
      try {
        await navigator.clipboard.writeText(codeText.replace(/复制代码$/, '').trim())
        btn.textContent = '✓ 已复制'
        setTimeout(() => {
          btn.textContent = '复制代码'
        }, 1800)
      } catch {
        btn.textContent = '复制失败'
      }
    })
    pre.appendChild(btn)
  })

  // 3. 为正文图片绑定点击放大预览
  const imgs = Array.from(root.querySelectorAll('img')) as HTMLImageElement[]
  imgs.forEach((img) => {
    if (img.dataset.enhanced === '1') return
    img.dataset.enhanced = '1'
    img.style.cursor = 'zoom-in'
    img.addEventListener('click', () => {
      lightboxUrl.value = img.src
      lightboxAlt.value = img.alt || props.title || '正文配图预览'
    })
  })
}

function onScroll() {
  const doc = document.documentElement
  const scrollTop = window.scrollY || doc.scrollTop
  const scrollHeight = doc.scrollHeight - doc.clientHeight
  readingProgress.value = scrollHeight > 0 ? Math.min(100, Math.max(0, Math.round((scrollTop / scrollHeight) * 100))) : 0

  if (tocItems.value.length > 0) {
    let current = tocItems.value[0].id
    for (const item of tocItems.value) {
      const el = document.getElementById(item.id)
      if (el) {
        const rect = el.getBoundingClientRect()
        if (rect.top <= 140) {
          current = item.id
        }
      }
    }
    activeId.value = current
  }
}

function scrollToHeading(id: string) {
  const el = document.getElementById(id)
  if (!el) return
  activeId.value = id
  const y = el.getBoundingClientRect().top + window.scrollY - 96
  window.scrollTo({ top: y, behavior: 'smooth' })
}

async function copyPageLink() {
  try {
    await navigator.clipboard.writeText(window.location.href)
    copiedLink.value = true
    setTimeout(() => {
      copiedLink.value = false
    }, 2000)
  } catch {
    // ignore
  }
}

watch(
  () => props.html,
  async () => {
    await nextTick()
    enhanceContent()
  },
)

onMounted(async () => {
  await nextTick()
  enhanceContent()
  window.addEventListener('scroll', onScroll, { passive: true })
  onScroll()
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<template>
  <div class="cms-article-shell">
    <!-- 顶部阅读进度条 -->
    <div v-if="showProgress && html" class="cms-reading-bar" aria-hidden="true">
      <div class="cms-reading-bar__fill" :style="{ width: `${readingProgress}%` }"></div>
    </div>

    <div class="cms-article-layout" :class="{ 'has-toc': showToc && tocItems.length >= 2 }">
      <!-- 主正文区（同时保留 cms-article__prose 与 cms-prose 类名，100% 兼容所有调用方） -->
      <div class="cms-article">
        <div v-if="html && showTools" class="cms-article__meta-bar">
          <span class="cms-article__read-time">⏱ 预计阅读 {{ estimatedMinutes }} 分钟</span>
          <span class="cms-article__progress-txt">阅读进度 {{ readingProgress }}%</span>
        </div>

        <!-- eslint-disable-next-line vue/no-v-html -->
        <div
          v-if="html"
          ref="articleRef"
          class="cms-article__prose cms-prose"
          v-html="html"
        ></div>
        <p v-else class="cms-empty">暂无正文内容</p>

        <!-- 底部互动与分享工具条 -->
        <div v-if="html && showTools" class="cms-article__footer-bar">
          <div class="cms-article__footer-left">
            <span class="cms-article__badge">数字化洞察与技术实践</span>
          </div>
          <div class="cms-article__footer-actions">
            <button type="button" class="cms-tool-btn" @click="copyPageLink">
              {{ copiedLink ? '✓ 已复制本文链接' : '🔗 复制文章链接' }}
            </button>
            <button type="button" class="cms-tool-btn cms-tool-btn--primary" @click="sharePosterOpen = true">
              ✨ 生成分享卡片
            </button>
          </div>
        </div>
      </div>

      <!-- 自动提取的右侧悬浮目录 (TOC) -->
      <aside v-if="showToc && tocItems.length >= 2" class="cms-toc" aria-label="文章目录">
        <div class="cms-toc__card">
          <div class="cms-toc__head">
            <span class="cms-toc__title">本文目录</span>
            <span class="cms-toc__pct">{{ readingProgress }}%</span>
          </div>
          <ul class="cms-toc__list">
            <li
              v-for="item in tocItems"
              :key="item.id"
              class="cms-toc__item"
              :class="[`cms-toc__item--lv${item.level}`, { 'is-active': activeId === item.id }]"
            >
              <button type="button" class="cms-toc__link" @click="scrollToHeading(item.id)">
                {{ item.text }}
              </button>
            </li>
          </ul>
        </div>
      </aside>
    </div>

    <!-- 正文图片高清放大灯箱 -->
    <Teleport to="body">
      <div v-if="lightboxUrl" class="cms-lightbox" @click="lightboxUrl = ''">
        <button type="button" class="cms-lightbox__close" aria-label="关闭预览" @click="lightboxUrl = ''">✕</button>
        <img :src="lightboxUrl" :alt="lightboxAlt" class="cms-lightbox__img" @click.stop />
        <p v-if="lightboxAlt" class="cms-lightbox__caption">{{ lightboxAlt }}</p>
      </div>

      <!-- 文章金句分享海报卡弹窗 -->
      <div v-if="sharePosterOpen" class="cms-lightbox" @click.self="sharePosterOpen = false">
        <div class="cms-poster">
          <div class="cms-poster__head">
            <span class="cms-poster__kicker">ENTERPRISE INSIGHT · 数字化深度洞察</span>
            <button type="button" class="cms-poster__close" @click="sharePosterOpen = false">✕</button>
          </div>
          <h3 class="cms-poster__title">{{ title || '企业数字化转型与智能物联网实践指南' }}</h3>
          <p class="cms-poster__excerpt">
            {{ (html || '').replace(/<[^>]+>/g, '').slice(0, 110) }}...
          </p>
          <div class="cms-poster__foot">
            <div>
              <p class="cms-poster__brand">官方技术洞察专栏</p>
              <p class="cms-poster__url">{{ typeof window !== 'undefined' ? window.location.href : '' }}</p>
            </div>
            <button type="button" class="cms-tool-btn cms-tool-btn--primary" @click="copyPageLink">
              {{ copiedLink ? '✓ 链接已复制' : '复制分享链接' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.cms-article-shell {
  position: relative;
}

.cms-reading-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  z-index: 90;
  background: transparent;
  pointer-events: none;
}

.cms-reading-bar__fill {
  height: 100%;
  background: linear-gradient(90deg, #5b4be8 0%, #8b5cf6 50%, #38bdf8 100%);
  transition: width 0.15s ease-out;
}

.cms-article-layout {
  display: block;
}

.cms-article-layout.has-toc {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 240px;
  gap: 44px;
  align-items: start;
}

@media (max-width: 1024px) {
  .cms-article-layout.has-toc {
    grid-template-columns: 1fr;
  }
  .cms-toc {
    display: none;
  }
}

.cms-article__meta-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  margin-bottom: 24px;
  background: #f8f9fc;
  border: 1px solid rgba(16, 16, 20, 0.06);
  border-radius: 10px;
  font-size: 13px;
  color: #5f6470;
}

.cms-article__progress-txt {
  font-weight: 600;
  color: #5b4be8;
}

.cms-prose,
.cms-article__prose {
  font-size: 16.5px;
  line-height: 1.9;
  color: #1f2329;
  word-break: break-word;
  overflow-wrap: anywhere;
}

.cms-prose :deep(> * + *),
.cms-article__prose :deep(> * + *) {
  margin-top: 1.2em;
}

.cms-prose :deep(h2),
.cms-article__prose :deep(h2) {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.35;
  margin-top: 2.2em;
  margin-bottom: 0.75em;
  padding-bottom: 0.45em;
  border-bottom: 1px solid rgba(16, 16, 20, 0.08);
  color: #101014;
  letter-spacing: -0.02em;
  scroll-margin-top: 96px;
}

.cms-prose :deep(h3),
.cms-article__prose :deep(h3) {
  font-size: 19px;
  font-weight: 600;
  line-height: 1.45;
  margin-top: 1.8em;
  margin-bottom: 0.5em;
  color: #101014;
  scroll-margin-top: 96px;
}

.cms-prose :deep(p),
.cms-article__prose :deep(p) {
  margin: 0 0 1.15em;
  color: #2b2f38;
}

.cms-prose :deep(ul),
.cms-prose :deep(ol),
.cms-article__prose :deep(ul),
.cms-article__prose :deep(ol) {
  margin: 1em 0 1.25em;
  padding-left: 1.5em;
}

.cms-prose :deep(li + li),
.cms-article__prose :deep(li + li) {
  margin-top: 0.45em;
}

.cms-prose :deep(blockquote),
.cms-article__prose :deep(blockquote) {
  margin: 1.6em 0;
  padding: 16px 22px;
  border-left: 4px solid #5b4be8;
  background: #f5f3ff;
  border-radius: 0 12px 12px 0;
  color: #3730a3;
  font-style: normal;
}

.cms-prose :deep(img),
.cms-article__prose :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 14px;
  margin: 1.6em auto;
  display: block;
  box-shadow: 0 12px 32px rgba(10, 12, 20, 0.08);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.cms-prose :deep(img:hover),
.cms-article__prose :deep(img:hover) {
  transform: translateY(-2px);
  box-shadow: 0 16px 40px rgba(91, 75, 232, 0.16);
}

.cms-prose :deep(pre),
.cms-article__prose :deep(pre) {
  background: #0f172a;
  color: #e2e8f0;
  padding: 18px 20px;
  border-radius: 12px;
  overflow-x: auto;
  font-size: 13.5px;
  line-height: 1.65;
  margin: 1.5em 0;
}

.cms-prose :deep(.cms-code-copy),
.cms-article__prose :deep(.cms-code-copy) {
  position: absolute;
  top: 10px;
  right: 12px;
  padding: 4px 10px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: rgba(255, 255, 255, 0.08);
  color: #cbd5e1;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cms-prose :deep(.cms-code-copy:hover),
.cms-article__prose :deep(.cms-code-copy:hover) {
  background: #5b4be8;
  color: #ffffff;
  border-color: #5b4be8;
}

.cms-prose :deep(table),
.cms-article__prose :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1.6em 0;
  font-size: 14.5px;
  display: block;
  overflow-x: auto;
}

.cms-prose :deep(th),
.cms-prose :deep(td),
.cms-article__prose :deep(th),
.cms-article__prose :deep(td) {
  border: 1px solid rgba(16, 16, 20, 0.1);
  padding: 10px 14px;
  text-align: left;
}

.cms-prose :deep(th),
.cms-article__prose :deep(th) {
  background: #f5f7fa;
  font-weight: 600;
}

.cms-empty {
  color: #8b90a0;
  font-size: 14px;
  padding: 24px 0;
}

.cms-article__footer-bar {
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid rgba(16, 16, 20, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.cms-article__badge {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 999px;
  background: #eeebff;
  color: #5b4be8;
  font-weight: 600;
}

.cms-article__footer-actions {
  display: flex;
  gap: 10px;
}

.cms-tool-btn {
  padding: 7px 14px;
  border-radius: 8px;
  border: 1px solid rgba(16, 16, 20, 0.14);
  background: #ffffff;
  color: #101014;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cms-tool-btn:hover {
  border-color: #5b4be8;
  color: #5b4be8;
}

.cms-tool-btn--primary {
  background: #5b4be8;
  border-color: #5b4be8;
  color: #ffffff;
}

.cms-tool-btn--primary:hover {
  background: #4636d6;
  color: #ffffff;
}

/* TOC 悬浮目录 */
.cms-toc {
  position: sticky;
  top: 96px;
}

.cms-toc__card {
  background: #fafbfe;
  border: 1px solid rgba(16, 16, 20, 0.08);
  border-radius: 14px;
  padding: 16px 18px;
}

.cms-toc__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(16, 16, 20, 0.06);
}

.cms-toc__title {
  font-size: 13px;
  font-weight: 700;
  color: #101014;
  letter-spacing: 0.02em;
}

.cms-toc__pct {
  font-size: 12px;
  font-weight: 600;
  color: #5b4be8;
}

.cms-toc__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-height: 60vh;
  overflow-y: auto;
}

.cms-toc__item--lv3 {
  padding-left: 12px;
}

.cms-toc__link {
  width: 100%;
  text-align: left;
  border: none;
  background: transparent;
  padding: 6px 8px;
  border-radius: 6px;
  font-size: 13px;
  line-height: 1.45;
  color: #5f6470;
  cursor: pointer;
  transition: all 0.18s ease;
}

.cms-toc__link:hover {
  color: #101014;
  background: rgba(91, 75, 232, 0.06);
}

.cms-toc__item.is-active .cms-toc__link {
  color: #5b4be8;
  font-weight: 600;
  background: #eeebff;
}

/* 灯箱与分享卡 */
.cms-lightbox {
  position: fixed;
  inset: 0;
  z-index: 120;
  background: rgba(8, 8, 12, 0.82);
  backdrop-filter: blur(8px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.cms-lightbox__close {
  position: absolute;
  top: 24px;
  right: 28px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.1);
  color: #ffffff;
  font-size: 18px;
  cursor: pointer;
}

.cms-lightbox__img {
  max-width: min(1100px, 92vw);
  max-height: 82vh;
  border-radius: 12px;
  object-fit: contain;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.45);
}

.cms-lightbox__caption {
  margin-top: 14px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.cms-poster {
  width: min(520px, 94vw);
  background: linear-gradient(155deg, #0d101b 0%, #171829 60%, #241d4e 100%);
  color: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 22px;
  padding: 28px 30px;
  box-shadow: 0 28px 80px rgba(0, 0, 0, 0.5);
}

.cms-poster__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.cms-poster__kicker {
  font-size: 11px;
  letter-spacing: 0.14em;
  color: #a5b4fc;
  font-weight: 600;
}

.cms-poster__close {
  border: none;
  background: rgba(255, 255, 255, 0.1);
  color: #ffffff;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  cursor: pointer;
}

.cms-poster__title {
  font-size: 22px;
  font-weight: 700;
  color: #ffffff;
  line-height: 1.4;
  margin: 0 0 14px;
}

.cms-poster__excerpt {
  font-size: 14px;
  line-height: 1.75;
  color: rgba(255, 255, 255, 0.74);
  margin: 0 0 24px;
}

.cms-poster__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.12);
}

.cms-poster__brand {
  font-size: 13px;
  font-weight: 600;
  color: #ffffff;
  margin: 0;
}

.cms-poster__url {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.52);
  margin: 4px 0 0;
  max-width: 280px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
```

### `website/web/src/composables/consult.ts`

```typescript
import { reactive } from 'vue'
import { getContactPage, getSite } from '@/api/public'

export interface ConsultOpenOptions {
  sourcePosition: string
  contentType?: 'PRODUCT' | 'SOLUTION' | 'CASE'
  contentId?: string | number
  title?: string
  initialDemand?: string
}

const dialog = reactive({
  open: false,
  sourcePosition: 'NAVIGATION',
  contentType: undefined as 'PRODUCT' | 'SOLUTION' | 'CASE' | undefined,
  contentId: undefined as number | undefined,
  title: '',
  initialDemand: '',
})

const roiModal = reactive({
  open: false,
  presetIndustry: 'logistics',
})

export function openConsult(options: ConsultOpenOptions) {
  dialog.sourcePosition = options.sourcePosition
  dialog.contentType = options.contentType
  dialog.contentId = options.contentId != null ? Number(options.contentId) : undefined
  dialog.title = options.title ?? ''
  dialog.initialDemand = options.initialDemand ?? ''
  dialog.open = true
}

export function closeConsult() {
  dialog.open = false
}

export function useConsultDialog() {
  return { dialog, openConsult, closeConsult }
}

export function openRoiCalculator(industry = 'logistics') {
  roiModal.presetIndustry = industry
  roiModal.open = true
}

export function closeRoiCalculator() {
  roiModal.open = false
}

export function useRoiModal() {
  return { roiModal, openRoiCalculator, closeRoiCalculator }
}

export interface ConsultContext {
  formEnabled: boolean
  privacyVersion: number | null
  successText: string
  phone: string
  email: string
}

/** 打开表单时实时拉取当前发布态，不把 formEnabled 缓存在 localStorage。 */
export async function loadConsultContext(): Promise<ConsultContext> {
  const [site, contact] = await Promise.all([
    getSite(),
    getContactPage().catch(() => null),
  ])
  return {
    formEnabled: Boolean(site.formEnabled ?? contact?.formEnabled),
    privacyVersion: site.privacyVersion ?? null,
    successText: contact?.successText || '感谢您的咨询，我们的专家顾问将在 1 个工作日内与您联系。',
    phone: contact?.phone || site.contact?.phone || '',
    email: contact?.email || site.contact?.email || '',
  }
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
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuth } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const { current, logout } = useAuth()

const isCollapse = ref(false)
const isAdmin = computed(() => current.value?.role === 'ADMIN')

// ==================== 全局 ⌘K / Ctrl+K 命令面板 ====================
interface CmdAction {
  id: string
  title: string
  category: string
  path: string
  keywords: string
  adminOnly?: boolean
}

const ALL_COMMANDS: CmdAction[] = [
  { id: 'studio', title: '🎨 打开全站实景交互预演工作台 (Visual Live Studio)', category: '核心推荐', path: '/preview/home', keywords: 'preview studio 预演 可视化 画布 首页' },
  { id: 'dash', title: '📊 工作台概览 (Dashboard)', category: '导航跳转', path: '/dashboard', keywords: 'dashboard 工作台 统计' },
  { id: 'home', title: '🏠 首页模块编排管理 (Home)', category: '站点配置', path: '/home', keywords: 'home 首页 轮播 指标' },
  { id: 'site', title: '⚙️ 全站设置与 SEO / 页脚备案 (Site)', category: '站点配置', path: '/settings/site', keywords: 'site seo logo icp 全站 页脚 二维码' },
  { id: 'nav', title: '🧭 顶部导航与五列全景菜单 (Navigation)', category: '站点配置', path: '/settings/navigation', keywords: 'nav mega 导航 菜单 全景' },
  { id: 'prod-list', title: '📦 产品管理列表', category: '内容中心', path: '/contents/products', keywords: 'products 产品 列表' },
  { id: 'prod-new', title: '➕ 新建产品 (Create Product)', category: '快捷新建', path: '/contents/products/create', keywords: 'create new product 新建产品' },
  { id: 'sol-list', title: '💡 解决方案管理列表', category: '内容中心', path: '/contents/solutions', keywords: 'solutions 解决方案 列表' },
  { id: 'sol-new', title: '➕ 新建解决方案 (Create Solution)', category: '快捷新建', path: '/contents/solutions/create', keywords: 'create new solution 新建方案' },
  { id: 'case-list', title: '🏆 客户案例管理列表', category: '内容中心', path: '/contents/cases', keywords: 'cases 客户案例 列表' },
  { id: 'case-new', title: '➕ 新建客户案例 (Create Case)', category: '快捷新建', path: '/contents/cases/create', keywords: 'create new case 新建案例' },
  { id: 'news-list', title: '📰 新闻动态管理列表', category: '内容中心', path: '/contents/news', keywords: 'news 新闻 动态 列表' },
  { id: 'news-new', title: '➕ 发布新闻动态 (Create News)', category: '快捷新建', path: '/contents/news/create', keywords: 'create new news 新建新闻' },
  { id: 'cust-list', title: '🤝 合作客户标识墙管理', category: '内容中心', path: '/contents/customers', keywords: 'customers 客户标识 logo' },
  { id: 'cat', title: '🏷️ 内容分类管理', category: '内容中心', path: '/contents/categories', keywords: 'categories 分类' },
  { id: 'media', title: '🖼️ 素材库管理（图片与视频）', category: '资源与单页', path: '/media', keywords: 'media 素材 图片 视频 上传' },
  { id: 'about', title: '🏢 关于我们单页配置', category: '资源与单页', path: '/pages/about', keywords: 'about 关于我们 里程碑 资质' },
  { id: 'contact', title: '📞 联系我们与咨询开关配置', category: '资源与单页', path: '/pages/contact', keywords: 'contact 联系我们 电话 邮箱 表单开关' },
  { id: 'privacy', title: '🛡️ 隐私说明正文配置', category: '资源与单页', path: '/pages/privacy', keywords: 'privacy 隐私说明 协议' },
  { id: 'leads', title: '📬 客户咨询线索看板与跟进 (Leads)', category: '运营与安全', path: '/leads', keywords: 'leads 咨询 线索 客户 看板', adminOnly: true },
  { id: 'users', title: '👥 后台系统账号与角色管理', category: '运营与安全', path: '/system/users', keywords: 'users 账号 管理员 编辑 密码', adminOnly: true },
  { id: 'audit', title: '📜 系统操作审计日志', category: '运营与安全', path: '/system/audit-logs', keywords: 'audit logs 审计 日志', adminOnly: true },
  { id: 'pwd', title: '🔑 修改个人登录密码', category: '个人中心', path: '/profile/password', keywords: 'password 修改密码' },
]

const cmdOpen = ref(false)
const cmdQuery = ref('')
const cmdActiveIdx = ref(0)
const cmdInputRef = ref<HTMLInputElement | null>(null)

const filteredCommands = computed(() => {
  const q = cmdQuery.value.trim().toLowerCase()
  return ALL_COMMANDS.filter((item) => {
    if (item.adminOnly && !isAdmin.value) return false
    if (!q) return true
    return (
      item.title.toLowerCase().includes(q) ||
      item.category.toLowerCase().includes(q) ||
      item.keywords.toLowerCase().includes(q)
    )
  })
})

function openCommandPalette() {
  cmdOpen.value = true
  cmdQuery.value = ''
  cmdActiveIdx.value = 0
  window.setTimeout(() => {
    cmdInputRef.value?.focus()
  }, 50)
}

function runCommand(cmd: CmdAction) {
  cmdOpen.value = false
  router.push(cmd.path)
}

function handleKeydown(e: KeyboardEvent) {
  if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
    e.preventDefault()
    if (cmdOpen.value) cmdOpen.value = false
    else openCommandPalette()
    return
  }
  if (!cmdOpen.value) return
  if (e.key === 'Escape') {
    cmdOpen.value = false
  } else if (e.key === 'ArrowDown' && filteredCommands.value.length) {
    e.preventDefault()
    cmdActiveIdx.value = (cmdActiveIdx.value + 1) % filteredCommands.value.length
  } else if (e.key === 'ArrowUp' && filteredCommands.value.length) {
    e.preventDefault()
    cmdActiveIdx.value = (cmdActiveIdx.value - 1 + filteredCommands.value.length) % filteredCommands.value.length
  } else if (e.key === 'Enter' && filteredCommands.value[cmdActiveIdx.value]) {
    e.preventDefault()
    runCommand(filteredCommands.value[cmdActiveIdx.value])
  }
}

const breadcrumbs = computed(() => {
  const items: Array<{ title: string; path?: string }> = [{ title: '首页', path: '/dashboard' }]
  if (route.path === '/dashboard') return items

  const path = route.path
  if (path.startsWith('/settings') || path === '/home') {
    items.push({ title: '站点管理' })
  } else if (path.startsWith('/contents')) {
    items.push({ title: '内容管理' })
    const isEdit = path.includes('/edit')
    const isCreate = path.includes('/create') || path.includes('/new')
    if (isEdit || isCreate) {
      const parts = path.split('/')
      const type = parts[2]
      const typeMap: Record<string, string> = {
        products: '产品',
        solutions: '解决方案',
        cases: '客户案例',
        news: '新闻',
        customers: '客户标识',
      }
      if (type && typeMap[type]) {
        items.push({ title: typeMap[type], path: `/contents/${type}` })
      }
    }
  } else if (path.startsWith('/pages')) {
    items.push({ title: '单页管理' })
  } else if (path.startsWith('/system')) {
    items.push({ title: '系统管理' })
  }

  if (route.meta.title) {
    items.push({ title: String(route.meta.title) })
  }
  return items
})

function handleResize() {
  isCollapse.value = window.innerWidth < 1024
}

onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('keydown', handleKeydown)
})

async function onLogout() {
  await logout()
  await router.replace('/login')
}
</script>

<template>
  <el-container class="shell">
    <el-aside :width="isCollapse ? '64px' : '240px'" class="aside">
      <div class="brand" :class="{ 'is-collapsed': isCollapse }">
        <span class="brand__logo">G</span>
        <div v-show="!isCollapse" class="brand__text">
          <div class="brand__title">官网管理后台</div>
          <div class="brand__sub">CMS Console</div>
        </div>
      </div>

      <el-scrollbar class="menu-scroll">
        <el-menu
          :default-active="route.path"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
          class="menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon>
              <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                <rect x="3" y="3" width="7" height="7"></rect>
                <rect x="14" y="3" width="7" height="7"></rect>
                <rect x="14" y="14" width="7" height="7"></rect>
                <rect x="3" y="14" width="7" height="7"></rect>
              </svg>
            </el-icon>
            <template #title>工作台</template>
          </el-menu-item>

          <el-menu-item index="/preview/home" class="menu-item--studio">
            <el-icon>
              <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                <path d="M12 20h9"></path>
                <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path>
              </svg>
            </el-icon>
            <template #title>🎨 实景预演交互编辑</template>
          </el-menu-item>

          <el-sub-menu index="site">
            <template #title>
              <el-icon>
                <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                  <circle cx="12" cy="12" r="3"></circle>
                  <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
                </svg>
              </el-icon>
              <span>站点管理</span>
            </template>
            <el-menu-item index="/settings/site">全站设置</el-menu-item>
            <el-menu-item index="/settings/navigation">导航与全景菜单</el-menu-item>
            <el-menu-item index="/home">首页管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="contents">
            <template #title>
              <el-icon>
                <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                  <polyline points="14 2 14 8 20 8"></polyline>
                  <line x1="16" y1="13" x2="8" y2="13"></line>
                  <line x1="16" y1="17" x2="8" y2="17"></line>
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
                <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                  <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                  <line x1="3" y1="9" x2="21" y2="9"></line>
                  <line x1="9" y1="21" x2="9" y2="9"></line>
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
              <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                <circle cx="8.5" cy="8.5" r="1.5"></circle>
                <polyline points="21 15 16 10 5 21"></polyline>
              </svg>
            </el-icon>
            <template #title>素材管理</template>
          </el-menu-item>

          <el-menu-item v-if="isAdmin" index="/leads">
            <el-icon>
              <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
              </svg>
            </el-icon>
            <template #title>咨询管理</template>
          </el-menu-item>

          <el-sub-menu v-if="isAdmin" index="system">
            <template #title>
              <el-icon>
                <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                  <circle cx="9" cy="7" r="4"></circle>
                </svg>
              </el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/system/users">账号管理</el-menu-item>
            <el-menu-item index="/system/audit-logs">操作日志</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container class="main-container">
      <el-header class="header" height="60px">
        <div class="header__left">
          <button class="collapse-btn" :title="isCollapse ? '展开菜单' : '收起菜单'" @click="isCollapse = !isCollapse">
            <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" stroke-width="2" fill="none">
              <line x1="3" y1="12" x2="21" y2="12"></line>
              <line x1="3" y1="6" x2="21" y2="6"></line>
              <line x1="3" y1="18" x2="21" y2="18"></line>
            </svg>
          </button>

          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item
              v-for="(item, index) in breadcrumbs"
              :key="index"
              :to="item.path ? { path: item.path } : undefined"
            >
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header__right">
          <!-- ⌘K 快捷指令入口 -->
          <button type="button" class="cmd-btn" title="全局快捷命令面板 (Ctrl+K / ⌘K)" @click="openCommandPalette">
            <span>🔍 快捷指令</span>
            <kbd>⌘K</kbd>
          </button>

          <button type="button" class="studio-launch-btn" @click="router.push('/preview/home')">
            🎨 实景交互预演台
          </button>

          <a href="/" target="_blank" class="site-link" title="在新窗口打开前台官网">
            <span>访问官网</span>
          </a>

          <div class="divider"></div>

          <div class="user-profile">
            <div class="avatar">{{ (current?.displayName || current?.username || 'A').charAt(0).toUpperCase() }}</div>
            <div class="user-info">
              <span class="user-name">{{ current?.displayName || current?.username }}</span>
              <span class="user-role" :class="current?.role === 'ADMIN' ? 'role-admin' : 'role-editor'">
                {{ current?.role === 'ADMIN' ? '管理员' : '编辑' }}
              </span>
            </div>
          </div>

          <div class="header-actions">
            <button class="icon-btn" title="修改密码" @click="router.push('/profile/password')">
              <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
            </button>
            <button class="icon-btn icon-btn--logout" title="退出登录" @click="onLogout">
              <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                <polyline points="16 17 21 12 16 7"></polyline>
                <line x1="21" y1="12" x2="9" y2="12"></line>
              </svg>
            </button>
          </div>
        </div>
      </el-header>

      <el-main class="main">
        <RouterView />
      </el-main>
    </el-container>

    <!-- 全局 ⌘K / Ctrl+K 命令面板 -->
    <el-dialog
      v-model="cmdOpen"
      title="全局快捷指令面板 (Command Palette)"
      width="580px"
      top="10vh"
      append-to-body
    >
      <div class="cmd-box">
        <input
          ref="cmdInputRef"
          v-model="cmdQuery"
          type="text"
          class="cmd-input"
          placeholder="输入关键词快速跳转页面或执行动作（如：新建产品 / 预演 / 线索 / SEO）..."
        />
        <ul class="cmd-list">
          <li
            v-for="(cmd, idx) in filteredCommands"
            :key="cmd.id"
            class="cmd-item"
            :class="{ 'is-active': idx === cmdActiveIdx }"
            @mouseenter="cmdActiveIdx = idx"
            @click="runCommand(cmd)"
          >
            <span class="cmd-item__cat">{{ cmd.category }}</span>
            <span class="cmd-item__title">{{ cmd.title }}</span>
            <span class="cmd-item__go">↵ 直达</span>
          </li>
          <li v-if="filteredCommands.length === 0" class="cmd-empty">没有匹配的指令</li>
        </ul>
      </div>
    </el-dialog>
  </el-container>
</template>

<style scoped>
.shell {
  min-height: 100vh;
}

.aside {
  background: var(--a-sidebar, #0d101b);
  color: var(--a-sidebar-text, #c9d1d9);
  display: flex;
  flex-direction: column;
  transition: width 0.25s ease;
  overflow: hidden;
}

.brand {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.brand.is-collapsed {
  justify-content: center;
  padding: 0;
}

.brand__logo {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--a-brand, #5b4be8);
  color: #ffffff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 16px;
  flex: none;
}

.brand__title {
  font-size: 14px;
  font-weight: 700;
  color: #ffffff;
  line-height: 1.2;
}

.brand__sub {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.48);
}

.menu-scroll {
  flex: 1;
}

.menu {
  border-right: none;
  background: transparent;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: rgba(255, 255, 255, 0.72);
  --el-menu-hover-bg-color: rgba(255, 255, 255, 0.06);
  --el-menu-active-color: #ffffff;
}

.menu-item--studio {
  margin: 6px 10px;
  border-radius: 8px;
  background: linear-gradient(135deg, rgba(124, 58, 237, 0.28) 0%, rgba(79, 70, 229, 0.28) 100%) !important;
  border: 1px solid rgba(167, 139, 250, 0.35);
  color: #e0e7ff !important;
  font-weight: 600;
}

.main-container {
  background: var(--a-bg, #f4f6f9);
  display: flex;
  flex-direction: column;
}

.header {
  background: #ffffff;
  border-bottom: 1px solid var(--a-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header__left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.collapse-btn {
  border: none;
  background: transparent;
  color: var(--a-text-muted);
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  display: flex;
}

.collapse-btn:hover {
  background: #f3f4f6;
}

.header__right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cmd-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 5px 12px;
  border-radius: 8px;
  border: 1px solid var(--a-border);
  background: #f8f9fc;
  color: var(--a-text-muted);
  font-size: 12.5px;
  cursor: pointer;
}

.cmd-btn:hover {
  border-color: #5b4be8;
  color: #5b4be8;
}

.cmd-btn kbd {
  font-size: 11px;
  padding: 1px 5px;
  background: #eef1f6;
  border-radius: 4px;
}

.studio-launch-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 8px;
  border: none;
  background: linear-gradient(135deg, #7c3aed 0%, #5b4be8 100%);
  color: #ffffff;
  font-size: 12.5px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(91, 75, 232, 0.25);
}

.site-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: var(--a-text-muted);
  text-decoration: none;
  padding: 6px 10px;
  border-radius: 6px;
}

.site-link:hover {
  color: var(--a-brand, #5b4be8);
  background: #f5f3ff;
}

.divider {
  width: 1px;
  height: 20px;
  background: var(--a-border);
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #eeebff;
  color: #5b4be8;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 13px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.user-name,
.username {
  font-size: 13.5px;
  font-weight: 600;
  color: var(--a-text);
}

.user-role {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 600;
}

.role-admin {
  background: #eeebff;
  color: #5b4be8;
}

.role-editor {
  background: #e0f2fe;
  color: #0284c7;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
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

/* 命令面板样式 */
.cmd-input {
  width: 100%;
  height: 42px;
  padding: 0 14px;
  border-radius: 10px;
  border: 1px solid var(--a-border);
  font-size: 14px;
  outline: none;
  margin-bottom: 12px;
}

.cmd-input:focus {
  border-color: #5b4be8;
}

.cmd-list {
  list-style: none;
  margin: 0;
  padding: 0;
  max-height: 340px;
  overflow-y: auto;
}

.cmd-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
}

.cmd-item.is-active {
  background: #f5f3ff;
}

.cmd-item__cat {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: #eef1f6;
  color: #5f6470;
  font-weight: 600;
}

.cmd-item__title {
  flex: 1;
  font-size: 13.5px;
  font-weight: 500;
}

.cmd-item__go {
  font-size: 12px;
  color: #5b4be8;
}

.cmd-empty {
  padding: 24px;
  text-align: center;
  color: var(--a-text-muted);
}
</style>
```

### `website/server/src/main/java/com/company/cms/site/SiteConfigValidator.java`

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

        ArrayNode qrCodes = objectMapper.createArrayNode();
        JsonNode rawQrs = draft.get("qrCodes");
        if (rawQrs != null && rawQrs.isArray()) {
            for (int i = 0; i < Math.min(rawQrs.size(), 4); i++) {
                JsonNode q = rawQrs.get(i);
                if (q == null || !q.isObject()) continue;
                ObjectNode qNode = objectMapper.createObjectNode();
                qNode.put("label", limit(text(q, "label"), 20, "qrCodes[" + i + "].label", "二维码名称", errors));
                Long qMediaId = mediaId(q.get("mediaId"), "qrCodes[" + i + "].mediaId", "二维码图片", errors);
                if (qMediaId != null) {
                    qNode.put("mediaId", qMediaId);
                    qNode.put("imageUrl", "/api/public/media/" + qMediaId);
                } else {
                    String imgUrl = text(q, "imageUrl");
                    if (!imgUrl.isEmpty()) qNode.put("imageUrl", imgUrl);
                }
                qrCodes.add(qNode);
            }
        }
        node.set("qrCodes", qrCodes);

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

### `website/server/src/main/java/com/company/cms/site/HomeAggregateService.java`

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

### `website/web/src/data/types.ts`

```typescript
/**
 * 与后端公开 DTO 对齐的展示类型。
 * 后端接口就绪后，把 api/public.ts 里的 mock 换成 apiGet 即可，页面不需要改。
 */

export interface MediaRef {
  id: string
  url: string
  alt: string
  width: number
  height: number
  type?: 'IMAGE' | 'VIDEO'
  durationMs?: number | null
}

export interface PageResult<T> {
  items: T[]
  page: number
  pageSize: number
  total: number
}

export interface NavItem {
  label: string
  /** 站内路径；纯外链项与咨询动作此处为空字符串 */
  path: string
  url?: string | null
  newWindow?: boolean
  /** PAGE / CONTENT / EXTERNAL / CONSULT / ANCHOR */
  linkType?: string | null
  children?: NavItem[]
}

export interface MegaSubItem {
  label: string
  path?: string
  tag?: string
  isVideo?: boolean
  videoUrl?: string
  videoTitle?: string
}

export interface MegaColumn {
  title: string
  items: MegaSubItem[]
}

export interface QrCodeItem {
  label: string
  imageUrl?: string | null
}

export interface SiteConfig {
  siteName: string
  slogan: string
  logoUrl?: string | null
  logoAlt?: string
  faviconUrl?: string | null
  defaultSeo?: { title: string; description: string }
  nav: NavItem[]
  megaColumns?: MegaColumn[]
  megaCta?: { text: string; linkText: string } | null
  contact: {
    phone: string
    email: string
    address: string
    hours: string
  }
  footer: {
    copyright: string
    icp: string
    icpUrl?: string
    policeText?: string
    policeUrl?: string
    groups: Array<{ title: string; links: NavItem[] }>
    links: NavItem[]
    qrCodes?: QrCodeItem[]
  }
  formEnabled?: boolean
  privacyVersion?: number | null
}

export interface HomeLink {
  type: string
  url?: string
  newWindow?: boolean
  contentType?: string
  contentId?: string
}

export interface HomeBlock {
  title: string
  subtitle: string
}

export interface HomeHeroItem {
  key: string
  title: string
  subtitle: string
  desktopImage: MediaRef | null
  mobileImage: MediaRef | null
  buttonText: string
  link: HomeLink | null
}

export interface HomeMetric {
  key: string
  displayValue: string
  unit: string
  label: string
  description: string
}

export interface HomeCard {
  id: string
  contentType?: string
  title: string
  summary: string
  cover: MediaRef | null
  url: string
  customerName?: string
  industry?: string
  outcomes?: Array<{ displayValue: string; label: string }>
}

export interface HomeServiceGroup {
  key: string
  title: string
  description: string
  image: MediaRef | null
  moreLink: HomeLink | null
  items: HomeCard[]
}

export interface HomeCustomer {
  id: string
  name: string
  logo: MediaRef
  url: string | null
}

export interface HomeNewsItem {
  id: string
  title: string
  summary: string
  displayTime: string
  url: string
  cover?: MediaRef | null
}

export interface HomeView {
  version: number | null
  publishedAt: string | null
  sectionOrder?: string[]
  hero: { items: HomeHeroItem[] }
  metrics: (HomeBlock & { items: HomeMetric[] }) | null
  services: (HomeBlock & { groups: HomeServiceGroup[] }) | null
  cases: (HomeBlock & { items: HomeCard[]; moreUrl: string }) | null
  customers: (HomeBlock & { items: HomeCustomer[] }) | null
  news: (HomeBlock & { mode: string; items: HomeNewsItem[]; moreUrl: string }) | null
  consultation: { title: string; subtitle: string; buttonText: string; link: HomeLink }
}

export interface Category {
  id: string
  name: string
}

export interface SeoMeta {
  title?: string
  description?: string
}

export interface TitledItem {
  title: string
  description: string
  icon?: MediaRef | null
}

export interface OutcomeMetric {
  displayValue: string
  label: string
}

/** 列表与首页卡片共用的摘要结构，字段与后端公开 DTO 一致 */
export interface ContentSummary {
  id: string
  title: string
  summary: string
  cover: MediaRef | null
  coverAlt?: string
  categoryId: string | null
  categoryName: string | null
  industry?: string
  customerName?: string
  outcomes?: OutcomeMetric[]
  displayTime?: string
}

export interface ProductSummary extends ContentSummary {}

export interface ProductDetail extends ProductSummary {
  bodyHtml?: string
  seo?: SeoMeta
  hero?: MediaRef | null
  features?: TitledItem[]
  relatedCases?: CaseSummary[]
}

export interface SolutionSummary extends ContentSummary {}

export interface SolutionDetail extends SolutionSummary {
  bodyHtml?: string
  seo?: SeoMeta
  hero?: MediaRef | null
  pains?: TitledItem[]
  benefits?: TitledItem[]
  relatedProducts?: ProductSummary[]
}

export interface CaseSummary extends ContentSummary {}

export interface CaseDetail extends CaseSummary {
  bodyHtml?: string
  seo?: SeoMeta
  hero?: MediaRef | null
  quoteText?: string
  quoteAuthor?: string
  relatedProducts?: ProductSummary[]
  relatedSolutions?: SolutionSummary[]
}

export interface NewsSummary extends ContentSummary {}

export interface NewsDetail extends NewsSummary {
  bodyHtml?: string
  seo?: SeoMeta
  author?: string
  sourceName?: string
  sourceUrl?: string
  prev?: { id: string; title: string } | null
  next?: { id: string; title: string } | null
}

export interface AboutPage {
  intro: string
  mission: string
  milestones: Array<{ year: string; title: string; description: string }>
  qualifications: Array<{ name: string; description: string }>
}

export interface PrivacyPage {
  updatedAt: string
  bodyHtml: string
}

export interface ContactPage {
  title: string
  description: string
  successText: string
  formEnabled: boolean
  phone?: string
  email?: string
  address?: string
  hours?: string
}

export interface LeadSubmitPayload {
  name: string
  phone: string
  companyName: string
  demand: string
  sourcePath: string
  sourcePosition: string
  contentType?: string
  contentId?: number
  consent: boolean
  consentVersion: number
}

export interface LeadSubmitResult {
  accepted: boolean
  receiptNo: string
}
```

### `website/web/src/components/RoiCalculatorModal.vue`

```vue
<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import { closeRoiCalculator, openConsult, useRoiModal } from '@/composables/consult'

const { roiModal } = useRoiModal()

interface IndustryPreset {
  key: string
  label: string
  unitLabel: string
  defaultUnits: number
  defaultMonthlyCost: number
  fuelSavingRate: number
  safetyReductionRate: number
  efficiencyGainRate: number
  highlight: string
}

const INDUSTRIES: IndustryPreset[] = [
  {
    key: 'logistics',
    label: '大宗能源与干线物流',
    unitLabel: '自有及协同运力规模（辆）',
    defaultUnits: 120,
    defaultMonthlyCost: 18000,
    fuelSavingRate: 0.075,
    safetyReductionRate: 68,
    efficiencyGainRate: 45,
    highlight: 'AI 视频防偷油 + 主动安全风控 + 智能排线调度',
  },
  {
    key: 'coldchain',
    label: '医药与生鲜冷链物流',
    unitLabel: '冷链温控车及冷库节点（台/个）',
    defaultUnits: 80,
    defaultMonthlyCost: 22000,
    fuelSavingRate: 0.088,
    safetyReductionRate: 74,
    efficiencyGainRate: 52,
    highlight: '全链路温湿度毫秒级预警 + GSP 合规自动留档 + 货损率骤降',
  },
  {
    key: 'hazmat',
    label: '危化品与特种运输',
    unitLabel: '受控危化运输车辆（辆）',
    defaultUnits: 60,
    defaultMonthlyCost: 26000,
    fuelSavingRate: 0.082,
    safetyReductionRate: 82,
    efficiencyGainRate: 40,
    highlight: 'DMS 疲劳干预 + 电子围栏合规联锁 + 7×24h 安全管家托管',
  },
  {
    key: 'retail',
    label: '快消与连锁零售城配',
    unitLabel: '城配车辆与门店配送线路（条/辆）',
    defaultUnits: 150,
    defaultMonthlyCost: 12000,
    fuelSavingRate: 0.092,
    safetyReductionRate: 60,
    efficiencyGainRate: 58,
    highlight: 'AI 智能拼单排线 + 门店准点率看板 + 装载率动态优化',
  },
  {
    key: 'manufacturing',
    label: '制造厂区与供应链协同',
    unitLabel: '厂内物流设备与协同承运商节点（个）',
    defaultUnits: 100,
    defaultMonthlyCost: 15000,
    fuelSavingRate: 0.07,
    safetyReductionRate: 65,
    efficiencyGainRate: 48,
    highlight: '月台智能预约排队 + 在途可视化追踪 + 自动对账结算',
  },
]

const state = reactive({
  industryKey: 'logistics',
  units: 120,
  monthlyCostPerUnit: 18000,
  dispatchStaff: 8,
})

const currentIndustry = computed(
  () => INDUSTRIES.find((item) => item.key === state.industryKey) ?? INDUSTRIES[0],
)

function selectIndustry(item: IndustryPreset) {
  state.industryKey = item.key
  state.units = item.defaultUnits
  state.monthlyCostPerUnit = item.defaultMonthlyCost
}

watch(
  () => roiModal.presetIndustry,
  (val) => {
    const found = INDUSTRIES.find((i) => i.key === val)
    if (found) selectIndustry(found)
  },
)

// 计算年度节省金额（万元）
const annualDirectSavingWan = computed(() => {
  const raw = state.units * state.monthlyCostPerUnit * 12 * currentIndustry.value.fuelSavingRate
  return Math.max(5, Math.round((raw / 10000) * 10) / 10)
})

const annualLaborSavingWan = computed(() => {
  const raw = state.dispatchStaff * 9500 * 12 * (currentIndustry.value.efficiencyGainRate / 100) * 0.65
  return Math.max(2, Math.round((raw / 10000) * 10) / 10)
})

const totalAnnualSavingWan = computed(() => {
  return (annualDirectSavingWan.value + annualLaborSavingWan.value).toFixed(1)
})

const paybackMonths = computed(() => {
  const baseMonths = 4.2 - Math.min(2.0, state.units / 400)
  return Math.max(1.8, Math.round(baseMonths * 10) / 10)
})

function requestReport() {
  const summary = `【ROI 效益测算报告预约】行业场景：${currentIndustry.value.label}；资产/运力规模：${state.units}；单节点月均成本：¥${state.monthlyCostPerUnit}；调度团队：${state.dispatchStaff}人。系统测算预计年化综合降本约 ${totalAnnualSavingWan.value} 万元/年（预计回本周期 ${paybackMonths.value} 个月）。希望获取完整 PDF 测算报告与专家落地方案。`
  closeRoiCalculator()
  openConsult({
    sourcePosition: 'OTHER_CTA',
    title: '获取专属 ROI 测算报告与落地方案',
    initialDemand: summary,
  })
}
</script>

<template>
  <Teleport to="body">
    <div v-if="roiModal.open" class="roi-overlay" @click.self="closeRoiCalculator">
      <div class="roi-dialog" role="dialog" aria-modal="true" aria-labelledby="roi-title">
        <div class="roi-dialog__head">
          <div>
            <span class="roi-kicker">INTERACTIVE ROI SIMULATOR · 数字化效益精算模型</span>
            <h3 id="roi-title" class="roi-title">企业数字化与 AI 调度降本增效测算器</h3>
          </div>
          <button type="button" class="roi-close" aria-label="关闭测算器" @click="closeRoiCalculator">✕</button>
        </div>

        <!-- 行业切换 -->
        <div class="roi-tabs">
          <button
            v-for="item in INDUSTRIES"
            :key="item.key"
            type="button"
            class="roi-tab"
            :class="{ 'is-active': state.industryKey === item.key }"
            @click="selectIndustry(item)"
          >
            {{ item.label }}
          </button>
        </div>

        <div class="roi-body">
          <!-- 左侧参数输入 -->
          <div class="roi-inputs">
            <div class="roi-field">
              <div class="roi-field__head">
                <label>{{ currentIndustry.unitLabel }}</label>
                <span class="roi-field__val">{{ state.units }}</span>
              </div>
              <input v-model.number="state.units" type="range" min="10" max="2000" step="10" class="roi-slider" />
              <div class="roi-field__scale">
                <span>10</span>
                <span>500</span>
                <span>1000</span>
                <span>2000+</span>
              </div>
            </div>

            <div class="roi-field">
              <div class="roi-field__head">
                <label>单节点月均能耗/货损/运营成本（元）</label>
                <span class="roi-field__val">¥ {{ state.monthlyCostPerUnit.toLocaleString() }}</span>
              </div>
              <input
                v-model.number="state.monthlyCostPerUnit"
                type="range"
                min="3000"
                max="50000"
                step="1000"
                class="roi-slider"
              />
              <div class="roi-field__scale">
                <span>¥3,000</span>
                <span>¥15,000</span>
                <span>¥30,000</span>
                <span>¥50,000</span>
              </div>
            </div>

            <div class="roi-field">
              <div class="roi-field__head">
                <label>安全与调度运营人员规模（人）</label>
                <span class="roi-field__val">{{ state.dispatchStaff }} 人</span>
              </div>
              <input v-model.number="state.dispatchStaff" type="range" min="1" max="100" step="1" class="roi-slider" />
              <div class="roi-field__scale">
                <span>1 人</span>
                <span>25 人</span>
                <span>50 人</span>
                <span>100 人</span>
              </div>
            </div>

            <div class="roi-highlight">
              <span class="roi-highlight__tag">核心赋能路径</span>
              <span>{{ currentIndustry.highlight }}</span>
            </div>
          </div>

          <!-- 右侧实时测算看板 -->
          <div class="roi-result">
            <div class="roi-result__hero">
              <span class="roi-result__label">预计年化综合节省成本</span>
              <div class="roi-result__number">
                <span class="roi-result__currency">¥</span>
                <strong>{{ totalAnnualSavingWan }}</strong>
                <span class="roi-result__unit">万元 / 年</span>
              </div>
              <p class="roi-result__breakdown">
                含能耗与货损直接节降约 <b>{{ annualDirectSavingWan }} 万</b> + 调度与合规人效释放约
                <b>{{ annualLaborSavingWan }} 万</b>
              </p>
            </div>

            <div class="roi-metrics-grid">
              <div class="roi-metric-box">
                <span class="roi-metric-box__val">{{ currentIndustry.safetyReductionRate }}% ↓</span>
                <span class="roi-metric-box__lbl">高危风险与事故率下降</span>
              </div>
              <div class="roi-metric-box">
                <span class="roi-metric-box__val">{{ currentIndustry.efficiencyGainRate }}% ↑</span>
                <span class="roi-metric-box__lbl">智能排线与调度人效提升</span>
              </div>
              <div class="roi-metric-box">
                <span class="roi-metric-box__val">{{ paybackMonths }} 个月</span>
                <span class="roi-metric-box__lbl">预计数字化投资回本周期</span>
              </div>
            </div>

            <button type="button" class="roi-cta-btn" @click="requestReport">
              <span>获取完整 PDF 测算报告与专家定制方案</span>
              <span aria-hidden="true">→</span>
            </button>
            <p class="roi-disclaimer">
              * 基于同行业标杆客户历史交付中位数建模测算，点击上方按钮可一键将测算参数同步给顾问专家。
            </p>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.roi-overlay {
  position: fixed;
  inset: 0;
  z-index: 85;
  background: rgba(8, 10, 18, 0.72);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.roi-dialog {
  width: min(920px, 96vw);
  max-height: 92dvh;
  overflow-y: auto;
  background: #ffffff;
  border-radius: 24px;
  padding: 32px 36px;
  box-shadow: 0 32px 80px rgba(8, 10, 20, 0.35);
}

.roi-dialog__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.roi-kicker {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
  color: #5b4be8;
}

.roi-title {
  margin: 6px 0 0;
  font-size: 24px;
  font-weight: 700;
  color: #101014;
}

.roi-close {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid rgba(16, 16, 20, 0.12);
  background: #f5f7fa;
  color: #5f6470;
  font-size: 16px;
  cursor: pointer;
}

.roi-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 26px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(16, 16, 20, 0.08);
}

.roi-tab {
  padding: 8px 16px;
  border-radius: 999px;
  border: 1px solid rgba(16, 16, 20, 0.12);
  background: #ffffff;
  color: #5f6470;
  font-size: 13.5px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.roi-tab.is-active {
  background: #5b4be8;
  border-color: #5b4be8;
  color: #ffffff;
  box-shadow: 0 6px 18px rgba(91, 75, 232, 0.25);
}

.roi-body {
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  gap: 32px;
  align-items: stretch;
}

@media (max-width: 820px) {
  .roi-body {
    grid-template-columns: 1fr;
  }
  .roi-dialog {
    padding: 22px 20px;
  }
}

.roi-inputs {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.roi-field__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #101014;
}

.roi-field__val {
  color: #5b4be8;
  font-size: 16px;
  font-weight: 700;
}

.roi-slider {
  width: 100%;
  accent-color: #5b4be8;
  cursor: pointer;
}

.roi-field__scale {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #8b90a0;
  margin-top: 4px;
}

.roi-highlight {
  margin-top: auto;
  padding: 12px 14px;
  border-radius: 12px;
  background: #f5f3ff;
  border: 1px solid rgba(91, 75, 232, 0.18);
  font-size: 13px;
  color: #3730a3;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.roi-highlight__tag {
  padding: 2px 8px;
  border-radius: 6px;
  background: #5b4be8;
  color: #ffffff;
  font-size: 11px;
  font-weight: 600;
}

.roi-result {
  background: linear-gradient(155deg, #0b0d16 0%, #151728 60%, #231c4c 100%);
  color: #ffffff;
  border-radius: 18px;
  padding: 26px 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.roi-result__label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.72);
}

.roi-result__number {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin: 8px 0;
}

.roi-result__currency {
  font-size: 24px;
  font-weight: 700;
  color: #a5b4fc;
}

.roi-result__number strong {
  font-size: 46px;
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1;
  color: #ffffff;
}

.roi-result__unit {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.8);
}

.roi-result__breakdown {
  font-size: 12.5px;
  color: rgba(255, 255, 255, 0.68);
  line-height: 1.6;
  margin: 0 0 18px;
}

.roi-result__breakdown b {
  color: #c4b5fd;
}

.roi-metrics-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 20px;
}

.roi-metric-box {
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 12px 10px;
  text-align: center;
}

.roi-metric-box__val {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: #38bdf8;
}

.roi-metric-box__lbl {
  display: block;
  margin-top: 4px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.68);
}

.roi-cta-btn {
  width: 100%;
  min-height: 48px;
  border-radius: 10px;
  border: none;
  background: linear-gradient(135deg, #6366f1 0%, #5b4be8 100%);
  color: #ffffff;
  font-size: 14.5px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  box-shadow: 0 10px 24px rgba(91, 75, 232, 0.4);
}

.roi-cta-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 14px 28px rgba(91, 75, 232, 0.55);
}

.roi-disclaimer {
  margin: 10px 0 0;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.45);
  line-height: 1.5;
}
</style>
```

### `website/web/src/components/SiteHeader.vue`

```vue
<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCases, getNews, getProducts, getSolutions } from '@/api/public'
import { openConsult, openRoiCalculator } from '@/composables/consult'
import { openVideo } from '@/composables/videoModal'
import type { MegaColumn, MegaSubItem, NavItem } from '@/data/types'

const props = defineProps<{
  siteName: string
  nav: NavItem[]
  logoUrl?: string | null
  logoAlt?: string
  megaColumns?: MegaColumn[]
  megaCta?: { text: string; linkText: string } | null
}>()

const route = useRoute()
const router = useRouter()
const open = ref(false)
const openMenu = ref<string | null>(null)
const openMobile = ref<string | null>(null)
const scrolled = ref(false)
const lang = ref<'zh' | 'en'>('zh')

// ==================== 全站 ⌘K / Ctrl+K 聚光灯搜索 ====================
interface SearchHit {
  id: string
  type: 'PRODUCT' | 'SOLUTION' | 'CASE' | 'NEWS'
  typeLabel: string
  title: string
  summary: string
  path: string
}

const searchOpen = ref(false)
const searchKeyword = ref('')
const searchLoading = ref(false)
const searchResults = ref<SearchHit[]>([])
const activeHitIndex = ref(0)
const searchInputRef = ref<HTMLInputElement | null>(null)
let searchTimer: number | null = null

const enLabelMap: Record<string, string> = {
  首页: 'Home',
  产品: 'Products',
  产品中心: 'Products',
  产品与方案: 'Products & Solutions',
  解决方案: 'Solutions',
  客户案例: 'Cases',
  标杆案例: 'Cases',
  新闻: 'News',
  新闻动态: 'News & Insights',
  洞察专栏: 'Insights',
  关于我们: 'About Us',
  联系我们: 'Contact',
}

function displayNavLabel(label: string) {
  if (lang.value === 'zh') return label
  return enLabelMap[label] || label
}

function toggleLang() {
  lang.value = lang.value === 'zh' ? 'en' : 'zh'
  document.documentElement.lang = lang.value === 'en' ? 'en' : 'zh-CN'
}

function openSearchModal() {
  searchOpen.value = true
  activeHitIndex.value = 0
  window.setTimeout(() => {
    searchInputRef.value?.focus()
  }, 60)
  if (!searchResults.value.length && !searchKeyword.value.trim()) {
    runSpotlightSearch('')
  }
}

function closeSearchModal() {
  searchOpen.value = false
}

async function runSpotlightSearch(kw: string) {
  searchLoading.value = true
  try {
    const q = kw.trim() || undefined
    const [prods, sols, cases, news] = await Promise.all([
      getProducts({ page: 1, pageSize: 4, keyword: q }).catch(() => ({ items: [] })),
      getSolutions({ page: 1, pageSize: 4, keyword: q }).catch(() => ({ items: [] })),
      getCases({ page: 1, pageSize: 4, keyword: q }).catch(() => ({ items: [] })),
      getNews({ page: 1, pageSize: 4, keyword: q }).catch(() => ({ items: [] })),
    ])
    const hits: SearchHit[] = []
    for (const p of prods.items) {
      hits.push({
        id: `P-${p.id}`,
        type: 'PRODUCT',
        typeLabel: '产品',
        title: p.title,
        summary: p.summary || p.categoryName || '核心产品',
        path: `/products/${p.id}`,
      })
    }
    for (const s of sols.items) {
      hits.push({
        id: `S-${s.id}`,
        type: 'SOLUTION',
        typeLabel: '解决方案',
        title: s.title,
        summary: s.summary || s.industry || '行业解决方案',
        path: `/solutions/${s.id}`,
      })
    }
    for (const c of cases.items) {
      hits.push({
        id: `C-${c.id}`,
        type: 'CASE',
        typeLabel: '客户案例',
        title: c.title,
        summary: c.customerName ? `${c.customerName} · ${c.summary}` : c.summary,
        path: `/cases/${c.id}`,
      })
    }
    for (const n of news.items) {
      hits.push({
        id: `N-${n.id}`,
        type: 'NEWS',
        typeLabel: '新闻动态',
        title: n.title,
        summary: n.displayTime || n.summary,
        path: `/news/${n.id}`,
      })
    }
    searchResults.value = hits
    activeHitIndex.value = 0
  } finally {
    searchLoading.value = false
  }
}

watch(searchKeyword, (val) => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = window.setTimeout(() => {
    runSpotlightSearch(val)
  }, 200)
})

function goHit(hit: SearchHit) {
  searchOpen.value = false
  router.push(hit.path)
}

function close() {
  open.value = false
  openMobile.value = null
  openMenu.value = null
}

function onScroll() {
  scrolled.value = window.scrollY > 24
}

function onKeydown(event: KeyboardEvent) {
  if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 'k') {
    event.preventDefault()
    if (searchOpen.value) {
      closeSearchModal()
    } else {
      openSearchModal()
    }
    return
  }
  if (searchOpen.value) {
    if (event.key === 'Escape') {
      closeSearchModal()
      return
    }
    if (event.key === 'ArrowDown' && searchResults.value.length) {
      event.preventDefault()
      activeHitIndex.value = (activeHitIndex.value + 1) % searchResults.value.length
      return
    }
    if (event.key === 'ArrowUp' && searchResults.value.length) {
      event.preventDefault()
      activeHitIndex.value = (activeHitIndex.value - 1 + searchResults.value.length) % searchResults.value.length
      return
    }
    if (event.key === 'Enter' && searchResults.value[activeHitIndex.value]) {
      event.preventDefault()
      goHit(searchResults.value[activeHitIndex.value])
      return
    }
  }
  if (event.key !== 'Escape') return
  if (openMenu.value) {
    openMenu.value = null
    return
  }
  if (open.value) close()
}

let leaveTimer: number | null = null

function handleMouseEnter(item: NavItem) {
  if (!item.children?.length) {
    openMenu.value = null
    return
  }
  if (leaveTimer) {
    clearTimeout(leaveTimer)
    leaveTimer = null
  }
  openMenu.value = item.label
}

function handleMouseLeave() {
  if (leaveTimer) clearTimeout(leaveTimer)
  leaveTimer = window.setTimeout(() => {
    openMenu.value = null
  }, 180)
}

function toggleMenu(item: NavItem) {
  openMenu.value = openMenu.value === item.label ? null : item.label
}

const defaultMegaColumns: MegaColumn[] = [
  {
    title: 'AI产品',
    items: [{ label: '紫宝盒', path: '/products/1', tag: '首款', isVideo: false }],
  },
  {
    title: 'IoT服务',
    items: [
      { label: '安全管家', path: '/products?category=安全', isVideo: false },
      { label: '冷链管家', path: '/products?category=冷链', isVideo: false },
      { label: '流向管家', path: '/products?category=流向', isVideo: false },
      { label: '追货管家', path: '/products?category=追货', isVideo: false },
    ],
  },
  {
    title: '交易服务',
    items: [
      { label: '财运通', path: '/solutions?category=交易', isVideo: false },
      { label: '数字能源', path: '/solutions?category=能源', isVideo: false },
    ],
  },
  {
    title: '解决方案',
    items: [
      { label: '大宗物流数字化', path: '/solutions/1', isVideo: false },
      { label: '连锁零售数字化', path: '/solutions/2', isVideo: false },
      { label: '饲料防偷盗 AI 治理', path: '/solutions/3', isVideo: false },
    ],
  },
  {
    title: '产品演示',
    items: [{ label: '产品操作视频', path: '', isVideo: true }],
  },
]

const resolvedMegaColumns = computed<MegaColumn[]>(() => {
  if (props.megaColumns && props.megaColumns.length > 0) {
    return props.megaColumns
  }
  return defaultMegaColumns
})

const resolvedMegaCta = computed(() => ({
  text: props.megaCta?.text || '需要专业场景建议？',
  linkText: props.megaCta?.linkText || '联系技术专家',
}))

function handleMegaAction(subItem: MegaSubItem) {
  openMenu.value = null
  if (subItem.isVideo) {
    openVideo({
      title: subItem.videoTitle || '紫宝盒 | 物流行业首款全场景AI工具产品演示',
      url: subItem.videoUrl || 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
    })
  }
}

function consult(position = 'NAVIGATION') {
  openMenu.value = null
  openMobile.value = null
  openConsult({ sourcePosition: position })
}

function isActive(item: NavItem): boolean {
  if (!item.path) return false
  if (item.path === '/') return route.path === '/'
  return route.path === item.path || route.path.startsWith(`${item.path}/`)
}

onMounted(() => {
  document.addEventListener('keydown', onKeydown)
  window.addEventListener('scroll', onScroll, { passive: true })
  onScroll()
})
onBeforeUnmount(() => {
  document.removeEventListener('keydown', onKeydown)
  window.removeEventListener('scroll', onScroll)
})
</script>

<template>
  <header class="header" :class="{ 'is-scrolled': scrolled }">
    <div class="header__inner container container--wide">
      <RouterLink to="/" class="brand" :aria-label="`${props.siteName} 首页`">
        <img v-if="logoUrl" :src="logoUrl" :alt="logoAlt || siteName" class="brand__logo" />
        <template v-else>
          <span class="brand__mark" aria-hidden="true">
            <span>{{ siteName.slice(0, 1) }}</span>
          </span>
          <span class="brand__name">{{ siteName }}</span>
        </template>
      </RouterLink>

      <nav class="nav" aria-label="主导航">
        <ul class="nav__list">
          <li
            v-for="item in nav"
            :key="item.label"
            class="nav__item"
            @mouseenter="handleMouseEnter(item)"
            @mouseleave="handleMouseLeave"
          >
            <template v-if="item.children?.length">
              <button
                type="button"
                class="nav__link nav__button"
                :class="{ 'is-active': isActive(item), 'is-open': openMenu === item.label }"
                :aria-expanded="openMenu === item.label"
                @click="toggleMenu(item)"
              >
                {{ displayNavLabel(item.label) }}
                <span class="caret" aria-hidden="true"></span>
              </button>
              <Transition name="menu">
                <div
                  v-if="openMenu === item.label"
                  class="mega"
                  :class="{ 'mega--columns': item.label.includes('产品') || item.label.includes('方案') }"
                >
                  <!-- 对齐视频 5 列业务全景下拉菜单（优先使用后台配置的 megaColumns） -->
                  <template v-if="item.label.includes('产品') || item.label.includes('方案')">
                    <div class="mega-cols">
                      <div v-for="col in resolvedMegaColumns" :key="col.title" class="mega-col">
                        <div class="mega-col__title">{{ col.title }}</div>
                        <ul class="mega-col__list">
                          <li v-for="sub in col.items" :key="sub.label">
                            <button
                              v-if="sub.isVideo"
                              type="button"
                              class="mega__link mega__link--video"
                              @click="handleMegaAction(sub)"
                            >
                              <span class="mega__bullet" aria-hidden="true"></span>
                              <span class="mega__text">{{ sub.label }}</span>
                              <span class="mega__video-badge">▶ 演示</span>
                            </button>
                            <RouterLink
                              v-else-if="sub.path"
                              :to="sub.path"
                              class="mega__link"
                              @click="openMenu = null"
                            >
                              <span class="mega__bullet" aria-hidden="true"></span>
                              <span class="mega__text">{{ sub.label }}</span>
                              <span v-if="sub.tag" class="mega__item-tag">{{ sub.tag }}</span>
                              <span class="mega__arrow" aria-hidden="true">→</span>
                            </RouterLink>
                          </li>
                        </ul>
                      </div>
                    </div>
                  </template>

                  <!-- 常规多子级下拉菜单 -->
                  <template v-else>
                    <div class="mega__head">
                      <span class="mega__kicker">{{ item.label }} · 业务全景</span>
                      <span class="mega__note">选择方向，查看对应内容</span>
                    </div>
                    <ul class="mega__list">
                      <li v-for="child in item.children" :key="child.label">
                        <button
                          v-if="child.linkType === 'CONSULT'"
                          type="button"
                          class="mega__link"
                          @click="consult()"
                        >
                          <span class="mega__bullet" aria-hidden="true"></span>
                          <span class="mega__text">{{ displayNavLabel(child.label) }}</span>
                          <span class="mega__arrow" aria-hidden="true">→</span>
                        </button>
                        <a
                          v-else-if="child.url"
                          :href="child.url"
                          class="mega__link"
                          :target="child.newWindow ? '_blank' : undefined"
                          :rel="child.newWindow ? 'noopener noreferrer' : undefined"
                          @click="openMenu = null"
                        >
                          <span class="mega__bullet" aria-hidden="true"></span>
                          <span class="mega__text">{{ displayNavLabel(child.label) }}</span>
                          <span class="mega__arrow" aria-hidden="true">→</span>
                        </a>
                        <RouterLink v-else :to="child.path" class="mega__link" @click="openMenu = null">
                          <span class="mega__bullet" aria-hidden="true"></span>
                          <span class="mega__text">{{ displayNavLabel(child.label) }}</span>
                          <span class="mega__arrow" aria-hidden="true">→</span>
                        </RouterLink>
                      </li>
                    </ul>
                  </template>

                  <div class="mega__foot">
                    <span>{{ resolvedMegaCta.text }}</span>
                    <div class="mega__foot-actions">
                      <button
                        type="button"
                        class="mega__foot-cta mega__foot-cta--roi"
                        @click="openMenu = null; openRoiCalculator()"
                      >
                        🧮 测算降本 ROI
                      </button>
                      <button type="button" class="mega__foot-cta" @click="consult('NAVIGATION')">
                        {{ resolvedMegaCta.linkText }} <span aria-hidden="true">→</span>
                      </button>
                    </div>
                  </div>
                </div>
              </Transition>
            </template>
            <button
              v-else-if="item.linkType === 'CONSULT'"
              type="button"
              class="nav__link"
              @click="consult()"
            >
              {{ displayNavLabel(item.label) }}
            </button>
            <a
              v-else-if="item.url"
              :href="item.url"
              class="nav__link"
              :target="item.newWindow ? '_blank' : undefined"
              :rel="item.newWindow ? 'noopener noreferrer' : undefined"
            >
              {{ displayNavLabel(item.label) }}
            </a>
            <RouterLink
              v-else
              :to="item.path"
              class="nav__link"
              :class="{ 'is-active': isActive(item) }"
            >
              {{ displayNavLabel(item.label) }}
            </RouterLink>
          </li>
        </ul>
      </nav>

      <div class="actions">
        <!-- ⌘K 聚光灯搜索入口 -->
        <button
          type="button"
          class="spotlight-trigger"
          title="全站搜索 (Ctrl+K / ⌘K)"
          @click="openSearchModal"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
            <circle cx="11" cy="11" r="8"></circle>
            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
          </svg>
          <span class="spotlight-trigger__text">{{ lang === 'en' ? 'Search' : '搜索' }}</span>
          <kbd class="spotlight-trigger__kbd">⌘K</kbd>
        </button>

        <!-- 中英双语切换 -->
        <button type="button" class="lang-btn" title="切换语言 / Switch Language" @click="toggleLang">
          {{ lang === 'zh' ? 'EN' : '中文' }}
        </button>

        <button
          type="button"
          class="actions__assistant"
          title="点击与数字物流专家在线交流"
          @click="consult('NAVIGATION')"
        >
          <span class="assistant__avatar">
            <svg class="assistant__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <rect x="3" y="11" width="18" height="10" rx="2"></rect>
              <circle cx="12" cy="5" r="2"></circle>
              <path d="M12 7v4"></path>
              <line x1="8" y1="16" x2="8.01" y2="16"></line>
              <line x1="16" y1="16" x2="16.01" y2="16"></line>
            </svg>
          </span>
          <span class="assistant__meta">
            <span class="assistant__sub">{{ lang === 'en' ? 'AI Expert' : '您好，我是您的' }}</span>
            <span class="assistant__name">{{ lang === 'en' ? 'Ask XiaoQi' : '数字物流专家 小七' }}</span>
          </span>
        </button>
        <button type="button" class="actions__cta" @click="consult('OTHER_CTA')">
          {{ lang === 'en' ? 'Book Demo' : '产品试用' }}
        </button>
        <button
          type="button"
          class="toggle"
          :aria-expanded="open"
          aria-controls="mobile-nav"
          @click="open = !open"
        >
          <span class="visually-hidden">{{ open ? '关闭菜单' : '打开菜单' }}</span>
          <span class="toggle__line" :class="{ 'is-open': open }"></span>
        </button>
      </div>
    </div>

    <Transition name="mobile">
      <nav v-if="open" id="mobile-nav" class="mobile" aria-label="移动端导航">
        <div class="mobile__top-tools">
          <button type="button" class="mobile__search-btn" @click="close(); openSearchModal()">
            🔍 搜索产品、方案、案例或动态...
          </button>
          <button type="button" class="lang-btn" @click="toggleLang">
            {{ lang === 'zh' ? 'English' : '中文' }}
          </button>
        </div>
        <ul class="mobile__list">
          <li v-for="item in nav" :key="item.label">
            <template v-if="item.children?.length">
              <button
                type="button"
                class="mobile__link mobile__button"
                :aria-expanded="openMobile === item.label"
                @click="openMobile = openMobile === item.label ? null : item.label"
              >
                {{ displayNavLabel(item.label) }}
                <span class="caret" aria-hidden="true"></span>
              </button>
              <ul v-if="openMobile === item.label" class="mobile__sub">
                <li v-for="child in item.children" :key="child.label">
                  <button
                    v-if="child.linkType === 'CONSULT'"
                    type="button"
                    class="mobile__sublink mobile__sublink--button"
                    @click="consult()"
                  >
                    {{ displayNavLabel(child.label) }}
                  </button>
                  <a
                    v-else-if="child.url"
                    :href="child.url"
                    class="mobile__sublink"
                    :target="child.newWindow ? '_blank' : undefined"
                    :rel="child.newWindow ? 'noopener noreferrer' : undefined"
                    @click="close"
                  >
                    {{ displayNavLabel(child.label) }}
                  </a>
                  <RouterLink v-else :to="child.path" class="mobile__sublink" @click="close">
                    {{ displayNavLabel(child.label) }}
                  </RouterLink>
                </li>
              </ul>
            </template>
            <button
              v-else-if="item.linkType === 'CONSULT'"
              type="button"
              class="mobile__link"
              @click="consult()"
            >
              {{ displayNavLabel(item.label) }}
            </button>
            <a
              v-else-if="item.url"
              :href="item.url"
              class="mobile__link"
              :target="item.newWindow ? '_blank' : undefined"
              :rel="item.newWindow ? 'noopener noreferrer' : undefined"
              @click="close"
            >
              {{ displayNavLabel(item.label) }}
            </a>
            <RouterLink
              v-else
              :to="item.path"
              class="mobile__link"
              :class="{ 'is-active': isActive(item) }"
              @click="close"
            >
              {{ displayNavLabel(item.label) }}
            </RouterLink>
          </li>
        </ul>
        <button type="button" class="btn btn--primary mobile__cta" @click="consult('OTHER_CTA')">
          预约演示
        </button>
      </nav>
    </Transition>

    <!-- 全站 ⌘K / Ctrl+K 聚光灯搜索弹窗 -->
    <Teleport to="body">
      <div v-if="searchOpen" class="spotlight-mask" @click.self="closeSearchModal">
        <div class="spotlight-panel" role="dialog" aria-modal="true" aria-label="全站搜索">
          <div class="spotlight-bar">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
            <input
              ref="searchInputRef"
              v-model="searchKeyword"
              type="text"
              class="spotlight-input"
              placeholder="输入关键词即时检索：产品、解决方案、标杆案例、新闻动态..."
            />
            <button v-if="searchKeyword" type="button" class="spotlight-clear" @click="searchKeyword = ''">
              清空
            </button>
            <button type="button" class="spotlight-esc" @click="closeSearchModal">ESC</button>
          </div>

          <div class="spotlight-quick">
            <span class="spotlight-quick__lbl">快捷直达：</span>
            <button type="button" class="spotlight-chip" @click="closeSearchModal(); openRoiCalculator()">
              🧮 ROI 降本增效测算器
            </button>
            <button type="button" class="spotlight-chip" @click="closeSearchModal(); consult('NAVIGATION')">
              🤖 唤醒数字专家小七
            </button>
            <button type="button" class="spotlight-chip" @click="closeSearchModal(); router.push('/products')">
              📦 全部产品
            </button>
            <button type="button" class="spotlight-chip" @click="closeSearchModal(); router.push('/solutions')">
              💡 行业方案
            </button>
            <button type="button" class="spotlight-chip" @click="closeSearchModal(); router.push('/cases')">
              🏆 客户案例
            </button>
          </div>

          <div class="spotlight-body">
            <div v-if="searchLoading" class="spotlight-state">正在检索全站已发布内容...</div>
            <div v-else-if="searchResults.length === 0" class="spotlight-state">
              未找到与「{{ searchKeyword }}」匹配的内容，可直接唤起小七专家为您解答。
            </div>
            <ul v-else class="spotlight-list">
              <li
                v-for="(hit, idx) in searchResults"
                :key="hit.id"
                class="spotlight-item"
                :class="{ 'is-active': idx === activeHitIndex }"
                @mouseenter="activeHitIndex = idx"
                @click="goHit(hit)"
              >
                <span class="spotlight-item__badge" :class="`badge--${hit.type.toLowerCase()}`">
                  {{ hit.typeLabel }}
                </span>
                <div class="spotlight-item__main">
                  <p class="spotlight-item__title">{{ hit.title }}</p>
                  <p v-if="hit.summary" class="spotlight-item__sub">{{ hit.summary }}</p>
                </div>
                <span class="spotlight-item__enter">↵ 跳转</span>
              </li>
            </ul>
          </div>

          <div class="spotlight-foot">
            <span>↑↓ 键切换结果 · Enter 跳转详情 · ESC 关闭</span>
            <span>仅检索线上已发布内容</span>
          </div>
        </div>
      </div>
    </Teleport>
  </header>
</template>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 40;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: saturate(180%) blur(14px);
  border-bottom: 1px solid transparent;
  transition: background-color 0.3s ease, border-color 0.3s ease, box-shadow 0.3s ease;
}

.header.is-scrolled {
  background: rgba(255, 255, 255, 0.98);
  border-bottom-color: var(--c-border);
  box-shadow: 0 8px 32px rgba(10, 12, 20, 0.05);
}

.header__inner {
  display: flex;
  align-items: center;
  gap: 32px;
  height: var(--header-height);
  transition: height 0.3s var(--ease-out);
}

.header.is-scrolled .header__inner {
  height: 68px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: none;
}

.brand__logo {
  max-height: 34px;
  max-width: 168px;
  object-fit: contain;
}

.brand__mark {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: linear-gradient(135deg, #7a6bff 0%, #5b4be8 60%, #3a2ac4 100%);
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 600;
  box-shadow: 0 6px 18px rgba(91, 75, 232, 0.28);
}

.brand__name {
  font-size: 17px;
  font-weight: 600;
  letter-spacing: -0.02em;
}

.nav {
  flex: 1;
  min-width: 0;
}

.nav__list {
  display: flex;
  gap: 28px;
  list-style: none;
  margin: 0;
  padding: 0;
}

.nav__item {
  position: relative;
}

.nav__link {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 0;
  font-size: 15px;
  color: var(--c-text-muted);
  background: none;
  border: none;
  cursor: pointer;
  font-family: inherit;
  transition: color 0.2s ease;
}

.nav__link::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 2px;
  height: 2px;
  background: var(--c-brand);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.3s var(--ease-out);
}

.nav__link:hover,
.nav__link.is-active {
  color: var(--c-text);
}

.nav__link.is-active::after {
  transform: scaleX(1);
}

.caret {
  width: 6px;
  height: 6px;
  border-right: 1.5px solid currentColor;
  border-bottom: 1.5px solid currentColor;
  transform: rotate(45deg) translateY(-2px);
  transition: transform 0.25s var(--ease-out);
}

.nav__button.is-open .caret {
  transform: rotate(225deg) translateY(-2px);
}

.mega {
  position: absolute;
  top: calc(100% + 10px);
  left: -24px;
  width: 520px;
  padding: 22px 24px 18px;
  background: #fff;
  border: 1px solid var(--c-border);
  border-radius: 18px;
  box-shadow: 0 28px 64px rgba(10, 12, 20, 0.14);
  z-index: 30;
}

.mega__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--c-border);
  margin-bottom: 8px;
}

.mega__kicker {
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--c-brand);
  font-weight: 600;
}

.mega__note {
  font-size: 12px;
  color: var(--c-text-subtle);
}

.mega__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2px 18px;
}

.mega__link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  padding: 11px 10px;
  border: none;
  background: none;
  border-radius: 8px;
  font-family: inherit;
  font-size: 14px;
  color: var(--c-text);
  text-align: left;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.mega__link:hover {
  background: var(--c-brand-soft);
  color: var(--c-brand);
}

.mega--columns {
  width: min(980px, 92vw);
  left: 50%;
  transform: translateX(-40%);
}

.mega-cols {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 20px;
  padding: 10px 0 16px;
}

.mega-col__title {
  font-size: 13px;
  font-weight: 700;
  color: var(--c-text-muted);
  letter-spacing: 0.04em;
  padding-bottom: 12px;
  margin-bottom: 8px;
  border-bottom: 1px solid var(--c-border);
}

.mega-col__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.mega-col__list .mega__link {
  padding: 8px 6px;
  font-size: 13.5px;
}

.mega__video-badge {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(91, 75, 232, 0.1);
  color: var(--c-brand);
  font-weight: 500;
}

.mega__item-tag {
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 3px;
  background: var(--c-accent, #ff4d4f);
  color: #fff;
  font-weight: 600;
  margin-right: auto;
  margin-left: 6px;
}

.mega__bullet {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--c-brand, #5b4be8);
  opacity: 0.4;
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.mega__link:hover .mega__bullet {
  opacity: 1;
  transform: scale(1.3);
}

.mega__text {
  flex: 1;
}

.mega__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid var(--c-border);
  font-size: 13px;
  color: var(--c-text-muted);
}

.mega__foot-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.mega__foot-cta {
  border: none;
  background: none;
  padding: 0;
  font-family: inherit;
  font-size: 13px;
  font-weight: 500;
  color: var(--c-brand);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: transform 0.2s ease;
}

.mega__foot-cta--roi {
  padding: 4px 10px;
  border-radius: 6px;
  background: #eeebff;
}

.mega__foot-cta:hover {
  transform: translateX(3px);
}

.mega__arrow {
  opacity: 0;
  transform: translateX(-6px);
  transition: opacity 0.2s ease, transform 0.25s var(--ease-out);
}

.mega__link:hover .mega__arrow {
  opacity: 1;
  transform: none;
}

.menu-enter-active,
.menu-leave-active {
  transition: opacity 0.2s ease, transform 0.25s var(--ease-out);
}

.menu-enter-from,
.menu-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: none;
}

.spotlight-trigger {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  padding: 0 10px;
  border-radius: 999px;
  border: 1px solid var(--c-border-strong);
  background: #f8f9fc;
  color: var(--c-text-muted);
  font-size: 12.5px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.spotlight-trigger:hover {
  border-color: var(--c-brand);
  color: var(--c-brand);
  background: #ffffff;
}

.spotlight-trigger__kbd {
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 4px;
  background: rgba(16, 16, 20, 0.06);
  font-family: inherit;
}

.lang-btn {
  height: 32px;
  padding: 0 10px;
  border-radius: 6px;
  border: 1px solid var(--c-border);
  background: transparent;
  font-size: 12.5px;
  font-weight: 600;
  color: var(--c-text-muted);
  cursor: pointer;
  transition: all 0.2s ease;
}

.lang-btn:hover {
  color: var(--c-brand);
  border-color: var(--c-brand);
}

.actions__assistant {
  display: flex;
  align-items: center;
  gap: 10px;
  background: none;
  border: none;
  padding: 4px 10px;
  border-radius: 999px;
  cursor: pointer;
  font-family: inherit;
  transition: background-color 0.2s ease;
}

.actions__assistant:hover {
  background: var(--c-surface);
}

.assistant__avatar {
  position: relative;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #7c3aed, #4f46e5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.35);
  flex: none;
}

.assistant__icon {
  width: 17px;
  height: 17px;
}

.assistant__meta {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  text-align: left;
  line-height: 1.25;
}

.assistant__sub {
  font-size: 11px;
  color: var(--c-text-subtle);
}

.assistant__name {
  font-size: 12px;
  font-weight: 600;
  color: var(--c-brand);
}

@media (max-width: 1120px) {
  .actions__assistant {
    display: none;
  }
}

.actions__cta {
  min-height: 40px;
  padding: 0 18px;
  border: none;
  border-radius: 6px;
  background: var(--c-text);
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: background-color 0.2s ease, transform 0.2s var(--ease-out);
}

.actions__cta:hover {
  background: var(--c-brand);
  transform: translateY(-1px);
}

.toggle {
  display: none;
  width: 40px;
  height: 40px;
  align-items: center;
  justify-content: center;
  background: #fff;
  border: 1px solid var(--c-border-strong);
  border-radius: 10px;
  cursor: pointer;
}

.toggle__line,
.toggle__line::before,
.toggle__line::after {
  display: block;
  width: 18px;
  height: 1.5px;
  background: var(--c-text);
  transition: transform 0.25s var(--ease-out), opacity 0.2s ease;
}

.toggle__line {
  position: relative;
}

.toggle__line::before,
.toggle__line::after {
  content: '';
  position: absolute;
  left: 0;
}

.toggle__line::before {
  top: -6px;
}

.toggle__line::after {
  top: 6px;
}

.toggle__line.is-open {
  background: transparent;
}

.toggle__line.is-open::before {
  top: 0;
  transform: rotate(45deg);
}

.toggle__line.is-open::after {
  top: 0;
  transform: rotate(-45deg);
}

.mobile {
  display: none;
  border-top: 1px solid var(--c-border);
  background: #fff;
  padding: 12px 20px 24px;
  max-height: calc(100dvh - var(--header-height));
  overflow-y: auto;
}

.mobile__top-tools {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.mobile__search-btn {
  flex: 1;
  height: 36px;
  padding: 0 12px;
  border-radius: 8px;
  border: 1px solid var(--c-border-strong);
  background: #f8f9fc;
  color: var(--c-text-muted);
  font-size: 13px;
  text-align: left;
}

.mobile__list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.mobile__link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 16px 4px;
  font-size: 17px;
  color: var(--c-text);
  border: none;
  border-bottom: 1px solid var(--c-border);
  background: none;
  font-family: inherit;
  text-align: left;
  cursor: pointer;
}

.mobile__link.is-active {
  color: var(--c-brand);
  font-weight: 600;
}

.mobile__sub {
  list-style: none;
  margin: 0;
  padding: 6px 0 10px 14px;
}

.mobile__sublink {
  display: block;
  padding: 11px 4px;
  font-size: 14px;
  color: var(--c-text-muted);
}

.mobile__sublink--button {
  width: 100%;
  border: none;
  background: none;
  text-align: left;
  font-family: inherit;
  cursor: pointer;
}

.mobile__cta {
  margin-top: 18px;
  width: 100%;
}

/* 聚光灯搜索弹窗样式 */
.spotlight-mask {
  position: fixed;
  inset: 0;
  z-index: 100;
  background: rgba(10, 12, 22, 0.58);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 10vh 16px 24px;
}

.spotlight-panel {
  width: min(680px, 96vw);
  background: #ffffff;
  border-radius: 18px;
  border: 1px solid rgba(16, 16, 20, 0.1);
  box-shadow: 0 28px 80px rgba(8, 10, 22, 0.28);
  overflow: hidden;
}

.spotlight-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--c-border);
  color: var(--c-brand);
}

.spotlight-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15.5px;
  color: var(--c-text);
  font-family: inherit;
}

.spotlight-clear {
  font-size: 12px;
  color: var(--c-text-muted);
  cursor: pointer;
}

.spotlight-esc {
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 6px;
  background: #f1f3f7;
  color: #5f6470;
  font-weight: 600;
}

.spotlight-quick {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding: 10px 20px;
  background: #fafbfe;
  border-bottom: 1px solid var(--c-border);
}

.spotlight-quick__lbl {
  font-size: 12px;
  color: var(--c-text-subtle);
}

.spotlight-chip {
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid rgba(91, 75, 232, 0.18);
  background: #ffffff;
  color: #4b5563;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.18s ease;
}

.spotlight-chip:hover {
  background: #5b4be8;
  color: #ffffff;
  border-color: #5b4be8;
}

.spotlight-body {
  max-height: 380px;
  overflow-y: auto;
  padding: 10px 12px;
}

.spotlight-state {
  padding: 36px 20px;
  text-align: center;
  font-size: 14px;
  color: var(--c-text-muted);
}

.spotlight-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.spotlight-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.spotlight-item.is-active {
  background: #f5f3ff;
}

.spotlight-item__badge {
  font-size: 11px;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: 6px;
  flex: none;
}

.badge--product {
  background: #eeebff;
  color: #5b4be8;
}

.badge--solution {
  background: #e0f2fe;
  color: #0284c7;
}

.badge--case {
  background: #fef3c7;
  color: #d97706;
}

.badge--news {
  background: #dcfce7;
  color: #15803d;
}

.spotlight-item__main {
  flex: 1;
  min-width: 0;
}

.spotlight-item__title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--c-text);
}

.spotlight-item__sub {
  margin: 2px 0 0;
  font-size: 12px;
  color: var(--c-text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.spotlight-item__enter {
  font-size: 12px;
  color: var(--c-brand);
  font-weight: 500;
}

.spotlight-foot {
  display: flex;
  justify-content: space-between;
  padding: 10px 20px;
  background: #f8f9fc;
  border-top: 1px solid var(--c-border);
  font-size: 11.5px;
  color: var(--c-text-subtle);
}

@media (max-width: 900px) {
  .nav,
  .spotlight-trigger,
  .lang-btn {
    display: none;
  }

  .toggle {
    display: inline-flex;
  }

  .mobile {
    display: block;
  }

  .actions {
    margin-left: auto;
    gap: 12px;
  }
}

@media (max-width: 520px) {
  .actions__cta {
    display: none;
  }
}
</style>
```

### `website/web/src/components/FloatingActions.vue`

```vue
<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { openConsult, openRoiCalculator } from '@/composables/consult'
import type { SiteConfig } from '@/data/types'

const props = defineProps<{
  site?: SiteConfig | null
}>()

const showPhoneCard = ref(false)
const scrollPercent = ref(0)
const showTopBtn = ref(false)

const phone = computed(() => props.site?.contact?.phone || '400-888-0000')
const email = computed(() => props.site?.contact?.email || 'connect@g7.com.cn')
const hours = computed(() => props.site?.contact?.hours || '周一至周日 08:30 - 20:30')

const ringDashoffset = computed(() => {
  const circumference = 2 * Math.PI * 18
  return circumference - (scrollPercent.value / 100) * circumference
})

function onScroll() {
  const doc = document.documentElement
  const top = window.scrollY || doc.scrollTop
  const height = doc.scrollHeight - doc.clientHeight
  showTopBtn.value = top > 260
  scrollPercent.value = height > 0 ? Math.min(100, Math.max(0, Math.round((top / height) * 100))) : 0
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true })
  onScroll()
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<template>
  <aside class="float-dock" aria-label="快捷工具栏">
    <!-- 1. 小七智能顾问磁吸入口 -->
    <button
      type="button"
      class="dock-btn dock-btn--ai"
      title="唤醒数字物流专家小七"
      @click="openConsult({ sourcePosition: 'FLOATING' })"
    >
      <span class="dock-btn__pulse" aria-hidden="true"></span>
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <rect x="3" y="11" width="18" height="10" rx="2"></rect>
        <circle cx="12" cy="5" r="2"></circle>
        <path d="M12 7v4"></path>
        <line x1="8" y1="16" x2="8.01" y2="16"></line>
        <line x1="16" y1="16" x2="16.01" y2="16"></line>
      </svg>
      <span class="dock-btn__label">小七顾问</span>
    </button>

    <!-- 2. ROI 降本增效测算器 -->
    <button
      type="button"
      class="dock-btn"
      title="测算您的企业数字化 ROI 收益"
      @click="openRoiCalculator()"
    >
      <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <rect x="4" y="2" width="16" height="20" rx="2"></rect>
        <line x1="8" y1="6" x2="16" y2="6"></line>
        <line x1="8" y1="10" x2="10" y2="10"></line>
        <line x1="14" y1="10" x2="16" y2="10"></line>
        <line x1="8" y1="14" x2="10" y2="14"></line>
        <line x1="14" y1="14" x2="16" y2="14"></line>
        <line x1="8" y1="18" x2="16" y2="18"></line>
      </svg>
      <span class="dock-btn__label">ROI测算</span>
    </button>

    <!-- 3. 热线电话与邮箱气泡 -->
    <div
      class="dock-popover-wrap"
      @mouseenter="showPhoneCard = true"
      @mouseleave="showPhoneCard = false"
    >
      <button
        type="button"
        class="dock-btn"
        aria-label="查看联系电话"
        @click="showPhoneCard = !showPhoneCard"
      >
        <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"></path>
        </svg>
        <span class="dock-btn__label">热线咨询</span>
      </button>

      <Transition name="pop">
        <div v-if="showPhoneCard" class="dock-card">
          <p class="dock-card__title">全国专家服务热线</p>
          <a :href="`tel:${phone}`" class="dock-card__phone">{{ phone }}</a>
          <p class="dock-card__meta">服务时间：{{ hours }}</p>
          <p class="dock-card__meta">商务合作：{{ email }}</p>
          <button
            type="button"
            class="dock-card__cta"
            @click="showPhoneCard = false; openConsult({ sourcePosition: 'FLOATING' })"
          >
            预约专家回电 →
          </button>
        </div>
      </Transition>
    </div>

    <!-- 4. 带阅读进度环的回到顶部按钮 -->
    <Transition name="pop">
      <button
        v-if="showTopBtn"
        type="button"
        class="dock-btn dock-btn--top"
        :title="`回到顶部 (已浏览 ${scrollPercent}%)`"
        @click="scrollToTop"
      >
        <svg class="progress-ring" width="42" height="42" viewBox="0 0 42 42">
          <circle class="progress-ring__bg" cx="21" cy="21" r="18" fill="none" stroke-width="2.5" />
          <circle
            class="progress-ring__bar"
            cx="21"
            cy="21"
            r="18"
            fill="none"
            stroke-width="2.5"
            stroke-dasharray="113.1"
            :stroke-dashoffset="ringDashoffset"
          />
        </svg>
        <span class="dock-btn__top-arrow">↑</span>
      </button>
    </Transition>
  </aside>
</template>

<style scoped>
.float-dock {
  position: fixed;
  right: 20px;
  bottom: 28px;
  z-index: 45;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.dock-btn {
  position: relative;
  width: 56px;
  height: 56px;
  border-radius: 16px;
  border: 1px solid rgba(16, 16, 20, 0.1);
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(12px);
  color: #101014;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  cursor: pointer;
  box-shadow: 0 10px 28px rgba(10, 12, 20, 0.1);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease, color 0.2s ease;
}

.dock-btn:hover {
  transform: translateY(-2px);
  border-color: #5b4be8;
  color: #5b4be8;
  box-shadow: 0 14px 32px rgba(91, 75, 232, 0.2);
}

.dock-btn--ai {
  background: linear-gradient(145deg, #6366f1 0%, #5b4be8 60%, #4338ca 100%);
  color: #ffffff;
  border-color: transparent;
  box-shadow: 0 12px 28px rgba(91, 75, 232, 0.38);
}

.dock-btn--ai:hover {
  color: #ffffff;
  box-shadow: 0 16px 36px rgba(91, 75, 232, 0.52);
}

.dock-btn__pulse {
  position: absolute;
  top: 7px;
  right: 7px;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #34d399;
  box-shadow: 0 0 0 3px rgba(52, 211, 153, 0.3);
}

.dock-btn__label {
  font-size: 10.5px;
  font-weight: 600;
  line-height: 1;
}

.dock-popover-wrap {
  position: relative;
}

.dock-card {
  position: absolute;
  right: calc(100% + 12px);
  bottom: 0;
  width: 248px;
  background: #ffffff;
  border: 1px solid rgba(16, 16, 20, 0.1);
  border-radius: 16px;
  padding: 18px;
  box-shadow: 0 20px 48px rgba(10, 12, 20, 0.16);
}

.dock-card__title {
  font-size: 12px;
  color: #8b90a0;
  margin: 0 0 4px;
}

.dock-card__phone {
  display: block;
  font-size: 20px;
  font-weight: 800;
  color: #5b4be8;
  margin-bottom: 8px;
}

.dock-card__meta {
  font-size: 12px;
  color: #5f6470;
  margin: 3px 0;
}

.dock-card__cta {
  margin-top: 12px;
  width: 100%;
  padding: 8px 12px;
  border-radius: 8px;
  border: none;
  background: #eeebff;
  color: #5b4be8;
  font-size: 12.5px;
  font-weight: 600;
  cursor: pointer;
}

.dock-btn--top {
  padding: 0;
}

.progress-ring {
  transform: rotate(-90deg);
}

.progress-ring__bg {
  stroke: rgba(16, 16, 20, 0.08);
}

.progress-ring__bar {
  stroke: #5b4be8;
  stroke-linecap: round;
  transition: stroke-dashoffset 0.15s ease;
}

.dock-btn__top-arrow {
  position: absolute;
  font-size: 15px;
  font-weight: 700;
}

.pop-enter-active,
.pop-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.pop-enter-from,
.pop-leave-to {
  opacity: 0;
  transform: translateY(6px);
}

@media (max-width: 640px) {
  .float-dock {
    right: 12px;
    bottom: 18px;
  }
  .dock-btn {
    width: 48px;
    height: 48px;
  }
  .dock-btn__label {
    font-size: 9.5px;
  }
}
</style>
```

### `website/web/src/layouts/SiteLayout.vue`

```vue
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import ConsultDialog from '@/components/ConsultDialog.vue'
import FloatingActions from '@/components/FloatingActions.vue'
import VideoModal from '@/components/VideoModal.vue'
import RoiCalculatorModal from '@/components/RoiCalculatorModal.vue'
import { ApiError } from '@/api/http'
import { getSite } from '@/api/public'
import type { SiteConfig } from '@/data/types'

const route = useRoute()
const config = ref<SiteConfig | null>(null)
const notice = ref('')

async function loadSite() {
  notice.value = ''
  try {
    config.value = await getSite()
  } catch (error) {
    notice.value =
      error instanceof ApiError && error.code === 'NOT_FOUND'
        ? '站点内容尚未发布，请管理员在后台完成「全站设置」并发布。'
        : '站点信息加载失败，请稍后刷新重试。'
  }
}

onMounted(loadSite)
</script>

<template>
  <div class="site">
    <a class="skip-link" href="#main">跳到主要内容</a>

    <SiteHeader
      v-if="config"
      :site-name="config.siteName"
      :nav="config.nav"
      :logo-url="config.logoUrl"
      :logo-alt="config.logoAlt"
      :mega-columns="config.megaColumns"
      :mega-cta="config.megaCta"
    />

    <main id="main" class="site__main" tabindex="-1">
      <div v-if="notice" class="container notice" role="status">
        <span>{{ notice }}</span>
        <button type="button" class="notice__retry" @click="loadSite">重试</button>
      </div>
      <RouterView v-slot="{ Component }">
        <Transition name="page" mode="out-in">
          <component :is="Component" :key="route.fullPath" />
        </Transition>
      </RouterView>
    </main>

    <SiteFooter v-if="config" :config="config" />

    <FloatingActions :site="config" />

    <ConsultDialog />
    <VideoModal />
    <RoiCalculatorModal />
  </div>
</template>

<style scoped>
.site {
  display: flex;
  flex-direction: column;
  min-height: 100dvh;
}

.site__main {
  flex: 1;
  outline: none;
}

.notice {
  margin-top: 16px;
  padding: 12px 16px;
  border: 1px solid #f0e0bd;
  background: #fdf6e3;
  border-radius: 8px;
  color: #8a6d1f;
  font-size: 14px;
  line-height: 1.7;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.notice__retry {
  padding: 4px 12px;
  border-radius: 6px;
  border: 1px solid #d8be82;
  background: #fff;
  color: #8a6d1f;
  font-size: 12.5px;
  cursor: pointer;
}
</style>
```

### `website/web/src/views/HomeView.vue`

```vue
<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ApiError } from '@/api/http'
import { getHome } from '@/api/public'
import { openConsult, openRoiCalculator } from '@/composables/consult'
import { openVideo } from '@/composables/videoModal'
import { usePageMeta } from '@/composables/usePageMeta'
import { vReveal } from '@/directives/reveal'
import EmptyState from '@/components/EmptyState.vue'
import type { HomeLink, HomeView } from '@/data/types'

const router = useRouter()
const data = ref<HomeView | null>(null)
const loading = ref(true)
const error = ref('')

// 首屏轮播与自动播放控制
const heroIndex = ref(0)
const heroPaused = ref(false)
const heroProgress = ref(0)
let heroTicker: number | null = null

// KPI 递增动画显示缓存
const animatedMetrics = reactive<Record<string, string>>({})
const metricsSectionRef = ref<HTMLElement | null>(null)
let metricsAnimated = false
let metricsObserver: IntersectionObserver | null = null

// 案例行业快速筛选与客户墙视图模式
const activeCaseIndustry = ref('ALL')
const logoViewMode = ref<'marquee' | 'grid'>('marquee')

// 首页内嵌快速 ROI 估算滑块
const quickUnits = ref(150)
const quickSavingWan = computed(() => Math.round(quickUnits.value * 1.65 * 10) / 10)

usePageMeta(() => ({
  title: '首页',
  description: '面向未来的物联网与 AI 数字生产力平台，助力行业领军企业实现降本增效与安全运营。',
}))

const DEFAULT_SECTION_ORDER = ['metrics', 'services', 'cases', 'customers', 'news', 'consultation']

const orderedSections = computed<string[]>(() => {
  const custom = data.value?.sectionOrder
  if (!custom || !Array.isArray(custom) || custom.length === 0) {
    return DEFAULT_SECTION_ORDER
  }
  const merged = [...custom.filter((k) => DEFAULT_SECTION_ORDER.includes(k))]
  for (const defKey of DEFAULT_SECTION_ORDER) {
    if (!merged.includes(defKey)) merged.push(defKey)
  }
  return merged
})

const heroes = computed(() => data.value?.hero?.items ?? [])
const currentHero = computed(() => heroes.value[heroIndex.value] ?? null)

const caseIndustries = computed(() => {
  const items = data.value?.cases?.items ?? []
  const set = new Set<string>()
  for (const item of items) {
    if (item.industry) set.add(item.industry)
  }
  return ['ALL', ...Array.from(set)]
})

const filteredCases = computed(() => {
  const items = data.value?.cases?.items ?? []
  if (activeCaseIndustry.value === 'ALL') return items
  return items.filter((item) => item.industry === activeCaseIndustry.value)
})

function startHeroAutoPlay() {
  stopHeroAutoPlay()
  heroTicker = window.setInterval(() => {
    if (heroPaused.value || heroes.value.length <= 1) return
    heroProgress.value += 2
    if (heroProgress.value >= 100) {
      heroProgress.value = 0
      heroIndex.value = (heroIndex.value + 1) % heroes.value.length
    }
  }, 120)
}

function stopHeroAutoPlay() {
  if (heroTicker) {
    clearInterval(heroTicker)
    heroTicker = null
  }
}

function selectHero(idx: number) {
  heroIndex.value = idx
  heroProgress.value = 0
}

function runMetricsCountUp() {
  if (metricsAnimated || !data.value?.metrics?.items?.length) return
  metricsAnimated = true
  for (const item of data.value.metrics.items) {
    const raw = item.displayValue.trim()
    const match = raw.match(/^([0-9]+(?:\.[0-9]+)?)(.*)$/)
    if (!match) {
      animatedMetrics[item.key] = raw
      continue
    }
    const targetNum = parseFloat(match[1])
    const suffix = match[2] || ''
    const decimals = match[1].includes('.') ? match[1].split('.')[1].length : 0
    const duration = 1300
    const start = performance.now()
    const step = (now: number) => {
      const progress = Math.min(1, (now - start) / duration)
      const eased = 1 - Math.pow(1 - progress, 3)
      const current = (targetNum * eased).toFixed(decimals)
      animatedMetrics[item.key] = `${current}${suffix}`
      if (progress < 1) {
        requestAnimationFrame(step)
      } else {
        animatedMetrics[item.key] = raw
      }
    }
    requestAnimationFrame(step)
  }
}

function observeMetrics() {
  if (typeof IntersectionObserver === 'undefined') {
    runMetricsCountUp()
    return
  }
  metricsObserver?.disconnect()
  metricsObserver = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting) {
          runMetricsCountUp()
          metricsObserver?.disconnect()
        }
      }
    },
    { threshold: 0.2 },
  )
  window.setTimeout(() => {
    const el = document.getElementById('metrics')
    if (el) metricsObserver?.observe(el)
    else runMetricsCountUp()
  }, 120)
}

function handleAction(link: HomeLink | null | undefined, sourcePosition = 'HOME_HERO') {
  if (!link) return
  if (link.type === 'CONSULT') {
    openConsult({
      sourcePosition,
      contentType: link.contentType as 'PRODUCT' | 'SOLUTION' | 'CASE' | undefined,
      contentId: link.contentId,
    })
    return
  }
  if (link.type === 'ANCHOR' && link.url) {
    const targetId = link.url.replace(/^#/, '')
    const el = document.getElementById(targetId)
    if (el) {
      el.scrollIntoView({ behavior: 'smooth' })
      return
    }
  }
  if (link.url) {
    if (link.url.startsWith('http://') || link.url.startsWith('https://')) {
      if (link.newWindow) {
        window.open(link.url, '_blank', 'noopener,noreferrer')
      } else {
        window.location.href = link.url
      }
    } else {
      router.push(link.url)
    }
  }
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    data.value = await getHome()
    heroIndex.value = 0
    heroProgress.value = 0
    metricsAnimated = false
    startHeroAutoPlay()
    observeMetrics()
  } catch (err) {
    data.value = null
    error.value =
      err instanceof ApiError && err.code === 'NOT_FOUND'
        ? '首页内容尚未发布，请管理员在后台「首页管理」或「实景交互预演台」完成配置并发布。'
        : err instanceof ApiError
        ? err.message
        : '首页数据加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

onMounted(load)
onBeforeUnmount(() => {
  stopHeroAutoPlay()
  metricsObserver?.disconnect()
})
</script>

<template>
  <div class="home-page">
    <div v-if="loading" class="container state">
      <div class="skeleton"></div>
    </div>

    <div v-else-if="error" class="container state">
      <EmptyState title="首页暂不可用" :description="error">
        <button type="button" class="btn btn--primary" @click="load">重新加载</button>
      </EmptyState>
    </div>

    <template v-else-if="data">
      <!-- 1. 首屏 Hero（固定置顶，支持视频背景、双端自适应与自动轮播进度条） -->
      <section
        v-if="currentHero"
        class="hero"
        @mouseenter="heroPaused = true"
        @mouseleave="heroPaused = false"
      >
        <div class="hero__media">
          <video
            v-if="currentHero.desktopImage?.type === 'VIDEO'"
            :key="currentHero.desktopImage.url"
            :src="currentHero.desktopImage.url"
            class="hero__bg"
            autoplay
            muted
            loop
            playsinline
          ></video>
          <picture v-else-if="currentHero.desktopImage">
            <source
              v-if="currentHero.mobileImage?.url"
              media="(max-width: 768px)"
              :srcset="currentHero.mobileImage.url"
            />
            <img
              :src="currentHero.desktopImage.url"
              :alt="currentHero.desktopImage.alt || currentHero.title"
              class="hero__bg"
              fetchpriority="high"
            />
          </picture>
          <div class="hero__overlay"></div>
        </div>

        <div class="container container--wide hero__content">
          <div class="hero__badge-row">
            <span class="hero__pill">AI + IoT 生产力平台</span>
            <button
              type="button"
              class="hero__video-trigger"
              @click="
                openVideo({
                  title: '企业级 AI 与物联网全场景数字平台演示',
                  url: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
                })
              "
            >
              ▶ 观看 90 秒全景演示
            </button>
          </div>

          <h1 class="hero__title">{{ currentHero.title }}</h1>
          <p v-if="currentHero.subtitle" class="hero__subtitle">{{ currentHero.subtitle }}</p>

          <div class="hero__actions">
            <button
              v-if="currentHero.buttonText && currentHero.link"
              type="button"
              class="btn btn--accent hero__primary-btn"
              @click="handleAction(currentHero.link, 'HOME_HERO')"
            >
              <span>{{ currentHero.buttonText }}</span>
              <span aria-hidden="true">→</span>
            </button>
            <button
              type="button"
              class="btn btn--outline-light"
              @click="openRoiCalculator()"
            >
              🧮 测算您的降本 ROI
            </button>
          </div>

          <!-- 多屏轮播指示器与进度条 -->
          <div v-if="heroes.length > 1" class="hero__Controls">
            <button
              v-for="(slide, idx) in heroes"
              :key="slide.key"
              type="button"
              class="hero__indicator"
              :class="{ 'is-active': idx === heroIndex }"
              :aria-label="`切换到第 ${idx + 1} 屏：${slide.title}`"
              @click="selectHero(idx)"
            >
              <span class="hero__indicator-num">0{{ idx + 1 }}</span>
              <span class="hero__indicator-label">{{ slide.title }}</span>
              <span class="hero__indicator-bar">
                <span
                  class="hero__indicator-fill"
                  :style="{ width: idx === heroIndex ? `${heroProgress}%` : '0%' }"
                ></span>
              </span>
            </button>
          </div>
        </div>
      </section>

      <!-- 按后台 Visual Live Studio 编排的 sectionOrder 动态渲染各大区块 -->
      <template v-for="secKey in orderedSections" :key="secKey">
        <!-- A. 企业关键指标 (metrics) — 带数字滚动递增动画 -->
        <section
          v-if="secKey === 'metrics' && data.metrics?.items?.length"
          id="metrics"
          ref="metricsSectionRef"
          v-reveal
          class="section section--tight metrics-section"
        >
          <div class="container container--wide">
            <div v-if="data.metrics.title || data.metrics.subtitle" class="section-head">
              <div>
                <span class="kicker">SCALE & IMPACT</span>
                <h2 v-if="data.metrics.title" class="section-title">{{ data.metrics.title }}</h2>
                <p v-if="data.metrics.subtitle" class="section-sub">{{ data.metrics.subtitle }}</p>
              </div>
            </div>

            <ul class="metrics-grid">
              <li v-for="item in data.metrics.items" :key="item.key" class="metric-item">
                <div class="metric-item__val-row">
                  <span class="metric-item__value">
                    {{ animatedMetrics[item.key] ?? item.displayValue }}
                  </span>
                  <span v-if="item.unit" class="metric-item__unit">{{ item.unit }}</span>
                </div>
                <p class="metric-item__label">{{ item.label }}</p>
                <p v-if="item.description" class="metric-item__desc">{{ item.description }}</p>
              </li>
            </ul>
          </div>
        </section>

        <!-- B. 核心产品与解决方案分组 (services) -->
        <section
          v-else-if="secKey === 'services' && data.services?.groups?.length"
          id="services"
          v-reveal
          class="section services-section"
        >
          <div class="container container--wide">
            <div class="section-head">
              <div>
                <span class="kicker">PRODUCTS & SOLUTIONS</span>
                <h2 class="section-title">{{ data.services.title || '核心产品与行业方案' }}</h2>
                <p v-if="data.services.subtitle" class="section-sub">{{ data.services.subtitle }}</p>
              </div>
              <RouterLink to="/products" class="link-more section-head__link">浏览全部产品与方案</RouterLink>
            </div>

            <div class="service-groups">
              <div
                v-for="(group, gIdx) in data.services.groups"
                :key="group.key"
                class="service-group"
                :class="{ 'is-reversed': gIdx % 2 === 1 }"
              >
                <div class="service-group__lead">
                  <div class="service-group__meta">
                    <span class="service-group__index">0{{ gIdx + 1 }}</span>
                    <h3 class="service-group__title">{{ group.title }}</h3>
                    <p v-if="group.description" class="service-group__desc">{{ group.description }}</p>
                    <button
                      v-if="group.moreLink"
                      type="button"
                      class="link-more service-group__more"
                      @click="handleAction(group.moreLink, 'OTHER_CTA')"
                    >
                      了解该领域详情
                    </button>
                  </div>
                  <div v-if="group.image" class="service-group__banner media-frame">
                    <video
                      v-if="group.image.type === 'VIDEO'"
                      :src="group.image.url"
                      autoplay
                      muted
                      loop
                      playsinline
                    ></video>
                    <img v-else :src="group.image.url" :alt="group.image.alt || group.title" loading="lazy" />
                  </div>
                </div>

                <div class="grid grid--3 service-group__cards">
                  <RouterLink
                    v-for="card in group.items"
                    :key="card.id"
                    :to="card.url || `/products/${card.id}`"
                    class="service-card"
                  >
                    <div v-if="card.cover" class="service-card__media media-frame">
                      <video
                        v-if="card.cover.type === 'VIDEO'"
                        :src="card.cover.url"
                        autoplay
                        muted
                        loop
                        playsinline
                      ></video>
                      <img v-else :src="card.cover.url" :alt="card.cover.alt || card.title" loading="lazy" />
                    </div>
                    <div class="service-card__body">
                      <div class="service-card__top">
                        <span class="tag">{{ card.contentType === 'SOLUTION' ? '解决方案' : '核心产品' }}</span>
                        <span class="service-card__arrow" aria-hidden="true">↗</span>
                      </div>
                      <h4 class="service-card__title">{{ card.title }}</h4>
                      <p v-if="card.summary" class="service-card__summary">{{ card.summary }}</p>
                    </div>
                  </RouterLink>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- C. 标杆客户案例 (cases) — 支持行业快筛与量化成果高亮 -->
        <section
          v-else-if="secKey === 'cases' && data.cases?.items?.length"
          id="cases"
          v-reveal
          class="section section--surface cases-section"
        >
          <div class="container container--wide">
            <div class="section-head">
              <div>
                <span class="kicker">CUSTOMER SUCCESS</span>
                <h2 class="section-title">{{ data.cases.title || '行业领军企业的共同选择' }}</h2>
                <p v-if="data.cases.subtitle" class="section-sub">{{ data.cases.subtitle }}</p>
              </div>
              <RouterLink :to="data.cases.moreUrl || '/cases'" class="link-more section-head__link">
                查看全部标杆案例
              </RouterLink>
            </div>

            <!-- 行业快速筛选胶囊 -->
            <div v-if="caseIndustries.length > 2" class="industry-pills">
              <button
                v-for="ind in caseIndustries"
                :key="ind"
                type="button"
                class="industry-pill"
                :class="{ 'is-active': activeCaseIndustry === ind }"
                @click="activeCaseIndustry = ind"
              >
                {{ ind === 'ALL' ? '全部行业' : ind }}
              </button>
            </div>

            <div class="grid grid--3">
              <RouterLink
                v-for="item in filteredCases"
                :key="item.id"
                :to="item.url || `/cases/${item.id}`"
                class="case-card"
              >
                <div class="case-card__media media-frame">
                  <img
                    v-if="item.cover"
                    :src="item.cover.url"
                    :alt="item.cover.alt || item.title"
                    loading="lazy"
                  />
                  <span v-if="item.industry" class="case-card__badge">{{ item.industry }}</span>
                </div>
                <div class="case-card__body">
                  <p v-if="item.customerName" class="case-card__customer">{{ item.customerName }}</p>
                  <h3 class="case-card__title">{{ item.title }}</h3>
                  <p v-if="item.summary" class="case-card__summary">{{ item.summary }}</p>

                  <dl v-if="item.outcomes?.length" class="case-outcomes">
                    <div v-for="out in item.outcomes" :key="out.label" class="case-outcomes__item">
                      <dd class="case-outcomes__val">{{ out.displayValue }}</dd>
                      <dt class="case-outcomes__lbl">{{ out.label }}</dt>
                    </div>
                  </dl>
                </div>
              </RouterLink>
            </div>
          </div>
        </section>

        <!-- D. 合作客户标识墙 (customers) — 支持无限无缝跑马灯与矩阵网格切换 -->
        <section
          v-else-if="secKey === 'customers' && data.customers?.items?.length"
          id="customers"
          v-reveal
          class="section section--tight customers-section"
        >
          <div class="container container--wide">
            <div class="section-head">
              <div>
                <span class="kicker">TRUSTED BY INDUSTRY LEADERS</span>
                <h2 class="section-title">{{ data.customers.title || '生态伙伴与标杆客户' }}</h2>
                <p v-if="data.customers.subtitle" class="section-sub">{{ data.customers.subtitle }}</p>
              </div>
              <div class="logo-mode-switch">
                <button
                  type="button"
                  class="mode-btn"
                  :class="{ 'is-active': logoViewMode === 'marquee' }"
                  @click="logoViewMode = 'marquee'"
                >
                  动态流展示
                </button>
                <button
                  type="button"
                  class="mode-btn"
                  :class="{ 'is-active': logoViewMode === 'grid' }"
                  @click="logoViewMode = 'grid'"
                >
                  矩阵展示
                </button>
              </div>
            </div>

            <!-- 无限无缝跑马灯模式 -->
            <div v-if="logoViewMode === 'marquee' && data.customers.items.length >= 4" class="logo-marquee">
              <div class="logo-marquee__track">
                <template v-for="loop in 2" :key="loop">
                  <component
                    :is="cust.url ? 'a' : 'div'"
                    v-for="cust in data.customers.items"
                    :key="`${loop}-${cust.id}`"
                    :href="cust.url || undefined"
                    :target="cust.url ? '_blank' : undefined"
                    :rel="cust.url ? 'noopener noreferrer' : undefined"
                    class="logo-tile"
                    :title="cust.name"
                  >
                    <img :src="cust.logo.url" :alt="cust.logo.alt || cust.name" loading="lazy" />
                    <span class="logo-tile__name">{{ cust.name }}</span>
                  </component>
                </template>
              </div>
            </div>

            <!-- 经典矩阵模式 -->
            <ul v-else class="logo-grid">
              <li v-for="cust in data.customers.items" :key="cust.id" class="logo-grid__item">
                <a
                  v-if="cust.url"
                  :href="cust.url"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="logo-grid__link"
                >
                  <img :src="cust.logo.url" :alt="cust.logo.alt || cust.name" loading="lazy" />
                </a>
                <div v-else class="logo-grid__link">
                  <img :src="cust.logo.url" :alt="cust.logo.alt || cust.name" loading="lazy" />
                </div>
              </li>
            </ul>
          </div>
        </section>

        <!-- E. 最新动态与洞察 (news) -->
        <section
          v-else-if="secKey === 'news' && data.news?.items?.length"
          id="news"
          v-reveal
          class="section section--surface news-section"
        >
          <div class="container container--wide">
            <div class="section-head">
              <div>
                <span class="kicker">NEWS & INSIGHTS</span>
                <h2 class="section-title">{{ data.news.title || '最新动态与前沿洞察' }}</h2>
                <p v-if="data.news.subtitle" class="section-sub">{{ data.news.subtitle }}</p>
              </div>
              <RouterLink :to="data.news.moreUrl || '/news'" class="link-more section-head__link">
                浏览全部新闻动态
              </RouterLink>
            </div>

            <div class="grid grid--3">
              <RouterLink
                v-for="item in data.news.items"
                :key="item.id"
                :to="item.url || `/news/${item.id}`"
                class="news-card"
              >
                <div v-if="item.cover" class="news-card__media media-frame">
                  <img :src="item.cover.url" :alt="item.cover.alt || item.title" loading="lazy" />
                </div>
                <div class="news-card__body">
                  <time class="news-card__date">{{ item.displayTime }}</time>
                  <h3 class="news-card__title">{{ item.title }}</h3>
                  <p v-if="item.summary" class="news-card__summary">{{ item.summary }}</p>
                  <span class="link-more news-card__more">阅读全文</span>
                </div>
              </RouterLink>
            </div>
          </div>
        </section>

        <!-- F. 互动式 ROI 快捷测算横幅 + 底部咨询行动区 (consultation) -->
        <section
          v-else-if="secKey === 'consultation' && data.consultation"
          id="consultation"
          v-reveal
          class="section section--dark consult-cta-section"
        >
          <div class="container container--wide">
            <!-- 互动式 ROI 快速精算卡 -->
            <div class="roi-inline-card">
              <div class="roi-inline-card__left">
                <span class="eyebrow">ROI QUICK ESTIMATOR · 降本增效快速精算</span>
                <h3 class="roi-inline-card__title">滑动评估您的车队/设备规模，即刻查看预计年化收益</h3>
                <div class="roi-inline-card__slider-row">
                  <span>当前运营规模：<strong>{{ quickUnits }} 台/节点</strong></span>
                  <input
                    v-model.number="quickUnits"
                    type="range"
                    min="20"
                    max="1500"
                    step="10"
                    class="roi-inline-slider"
                  />
                </div>
              </div>
              <div class="roi-inline-card__right">
                <span class="roi-inline-card__lbl">预计年化综合节降约</span>
                <strong class="roi-inline-card__val">¥ {{ quickSavingWan }} 万/年</strong>
                <button type="button" class="btn btn--accent" @click="openRoiCalculator()">
                  打开完整多维测算器 →
                </button>
              </div>
            </div>

            <!-- 底部主咨询区 -->
            <div class="consult-cta-box">
              <div>
                <h2 class="consult-cta-box__title">{{ data.consultation.title || '开启企业数字化生产力跃迁' }}</h2>
                <p v-if="data.consultation.subtitle" class="consult-cta-box__sub">
                  {{ data.consultation.subtitle }}
                </p>
              </div>
              <div class="consult-cta-box__actions">
                <button
                  type="button"
                  class="btn btn--accent consult-cta-box__btn"
                  @click="handleAction(data.consultation.link, 'HOME_BOTTOM')"
                >
                  {{ data.consultation.buttonText || '立即预约专家咨询' }}
                </button>
              </div>
            </div>
          </div>
        </section>
      </template>
    </template>
  </div>
</template>

<style scoped>
.hero {
  position: relative;
  min-height: clamp(560px, 78vh, 760px);
  display: flex;
  align-items: center;
  background: #08080c;
  color: #ffffff;
  overflow: hidden;
}

.hero__media {
  position: absolute;
  inset: 0;
}

.hero__bg {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero__overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    95deg,
    rgba(6, 8, 16, 0.92) 0%,
    rgba(6, 8, 16, 0.74) 48%,
    rgba(6, 8, 16, 0.25) 100%
  );
}

.hero__content {
  position: relative;
  z-index: 2;
  padding-top: 88px;
  padding-bottom: 64px;
}

.hero__badge-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}

.hero__pill {
  padding: 5px 14px;
  border-radius: 999px;
  background: rgba(91, 75, 232, 0.28);
  border: 1px solid rgba(139, 92, 246, 0.5);
  font-size: 12.5px;
  font-weight: 600;
  letter-spacing: 0.06em;
  color: #ddd6fe;
}

.hero__video-trigger {
  padding: 5px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #ffffff;
  font-size: 12.5px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.hero__video-trigger:hover {
  background: rgba(255, 255, 255, 0.2);
}

.hero__title {
  max-width: 720px;
  font-size: clamp(36px, 5vw, 64px);
  line-height: 1.12;
  font-weight: 700;
  color: #ffffff;
  white-space: pre-line;
  letter-spacing: -0.03em;
}

.hero__subtitle {
  margin-top: 20px;
  max-width: 580px;
  font-size: clamp(16px, 1.6vw, 19px);
  line-height: 1.75;
  color: rgba(255, 255, 255, 0.8);
}

.hero__actions {
  margin-top: 36px;
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.hero__Controls {
  margin-top: 56px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 240px));
  gap: 16px;
}

.hero__indicator {
  text-align: left;
  padding: 10px 0;
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.55);
  cursor: pointer;
  transition: color 0.2s ease;
}

.hero__indicator.is-active {
  color: #ffffff;
}

.hero__indicator-num {
  font-size: 11px;
  font-weight: 700;
  margin-right: 8px;
  color: #a5b4fc;
}

.hero__indicator-label {
  font-size: 13px;
  font-weight: 500;
}

.hero__indicator-bar {
  display: block;
  margin-top: 8px;
  height: 2px;
  background: rgba(255, 255, 255, 0.18);
  border-radius: 999px;
  overflow: hidden;
}

.hero__indicator-fill {
  display: block;
  height: 100%;
  background: #8b5cf6;
  transition: width 0.12s linear;
}

/* 企业关键指标 */
.metrics-section {
  border-bottom: 1px solid var(--c-border);
}

.metrics-grid {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 32px;
}

.metric-item {
  padding-left: 24px;
  border-left: 2px solid rgba(91, 75, 232, 0.22);
}

.metric-item__val-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.metric-item__value {
  font-size: clamp(38px, 4.2vw, 54px);
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1.05;
  color: var(--c-text);
}

.metric-item__unit {
  font-size: 22px;
  font-weight: 700;
  color: var(--c-brand);
}

.metric-item__label {
  margin-top: 10px;
  font-size: 15px;
  font-weight: 600;
  color: var(--c-text);
}

.metric-item__desc {
  margin-top: 4px;
  font-size: 13px;
  color: var(--c-text-muted);
}

/* 产品与服务分组 */
.service-groups {
  display: flex;
  flex-direction: column;
  gap: 72px;
}

.service-group__lead {
  display: grid;
  grid-template-columns: 1fr 1.1fr;
  gap: 40px;
  align-items: center;
  margin-bottom: 32px;
}

.service-group.is-reversed .service-group__lead {
  direction: rtl;
}

.service-group.is-reversed .service-group__meta {
  direction: ltr;
}

.service-group__index {
  font-size: 12px;
  font-weight: 700;
  color: var(--c-brand);
  letter-spacing: 0.14em;
}

.service-group__title {
  margin-top: 8px;
  font-size: clamp(24px, 3vw, 34px);
}

.service-group__desc {
  margin-top: 12px;
  font-size: 15.5px;
  color: var(--c-text-muted);
  line-height: 1.8;
}

.service-group__more {
  margin-top: 18px;
}

.service-group__banner {
  aspect-ratio: 16 / 9;
  max-height: 320px;
}

.service-card {
  background: #ffffff;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.28s var(--ease-out), box-shadow 0.28s var(--ease-out), border-color 0.28s ease;
}

.service-card:hover {
  transform: translateY(-6px);
  border-color: rgba(91, 75, 232, 0.35);
  box-shadow: 0 20px 44px rgba(10, 12, 24, 0.08);
}

.service-card__media {
  aspect-ratio: 16 / 9;
  border-radius: 0;
}

.service-card__body {
  padding: 22px 24px 26px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.service-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.service-card__arrow {
  color: var(--c-text-subtle);
  transition: transform 0.2s ease, color 0.2s ease;
}

.service-card:hover .service-card__arrow {
  transform: translate(3px, -3px);
  color: var(--c-brand);
}

.service-card__title {
  font-size: 19px;
  font-weight: 600;
}

.service-card__summary {
  margin-top: 8px;
  font-size: 14px;
  color: var(--c-text-muted);
  line-height: 1.75;
}

/* 客户案例 */
.industry-pills {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 32px;
}

.industry-pill {
  padding: 7px 18px;
  border-radius: 999px;
  border: 1px solid var(--c-border-strong);
  background: #ffffff;
  font-size: 13.5px;
  color: var(--c-text-muted);
  cursor: pointer;
  transition: all 0.2s ease;
}

.industry-pill.is-active {
  background: var(--c-brand);
  border-color: var(--c-brand);
  color: #ffffff;
}

.case-card {
  background: #ffffff;
  border-radius: var(--radius);
  border: 1px solid var(--c-border);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.28s var(--ease-out), box-shadow 0.28s var(--ease-out);
}

.case-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 22px 48px rgba(10, 12, 24, 0.09);
}

.case-card__media {
  aspect-ratio: 16 / 10;
  border-radius: 0;
}

.case-card__badge {
  position: absolute;
  top: 14px;
  left: 14px;
  padding: 4px 12px;
  border-radius: 999px;
  background: rgba(8, 8, 12, 0.75);
  backdrop-filter: blur(6px);
  color: #ffffff;
  font-size: 12px;
  font-weight: 500;
}

.case-card__body {
  padding: 24px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.case-card__customer {
  font-size: 12.5px;
  font-weight: 600;
  color: var(--c-brand);
  margin-bottom: 6px;
}

.case-card__title {
  font-size: 19px;
  line-height: 1.45;
}

.case-card__summary {
  margin-top: 8px;
  font-size: 14px;
  color: var(--c-text-muted);
  line-height: 1.75;
}

.case-outcomes {
  margin: auto 0 0;
  padding-top: 18px;
  border-top: 1px solid var(--c-border);
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.case-outcomes__val {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: var(--c-brand);
}

.case-outcomes__lbl {
  font-size: 12px;
  color: var(--c-text-muted);
}

/* 客户墙与跑马灯 */
.logo-mode-switch {
  display: flex;
  gap: 6px;
  background: var(--c-surface);
  padding: 4px;
  border-radius: 10px;
}

.mode-btn {
  padding: 6px 12px;
  border-radius: 7px;
  font-size: 12.5px;
  color: var(--c-text-muted);
  cursor: pointer;
}

.mode-btn.is-active {
  background: #ffffff;
  color: var(--c-text);
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.logo-marquee {
  overflow: hidden;
  padding: 12px 0;
  mask-image: linear-gradient(90deg, transparent 0%, #000 8%, #000 92%, transparent 100%);
}

.logo-marquee__track {
  display: flex;
  gap: 20px;
  width: max-content;
  animation: marquee-scroll 32s linear infinite;
}

.logo-marquee:hover .logo-marquee__track {
  animation-play-state: paused;
}

@keyframes marquee-scroll {
  from {
    transform: translateX(0);
  }
  to {
    transform: translateX(-50%);
  }
}

.logo-tile {
  width: 200px;
  height: 92px;
  border-radius: 14px;
  border: 1px solid var(--c-border);
  background: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 16px;
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.logo-tile:hover {
  border-color: var(--c-brand);
  transform: translateY(-2px);
}

.logo-tile img {
  max-width: 130px;
  max-height: 40px;
  object-fit: contain;
}

.logo-tile__name {
  font-size: 11.5px;
  color: var(--c-text-subtle);
}

.logo-grid {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.logo-grid__link {
  height: 92px;
  border: 1px solid var(--c-border);
  border-radius: 14px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.logo-grid__link img {
  max-width: 136px;
  max-height: 44px;
  object-fit: contain;
}

/* 新闻卡片 */
.news-card {
  background: #ffffff;
  border-radius: var(--radius);
  border: 1px solid var(--c-border);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.25s var(--ease-out), box-shadow 0.25s var(--ease-out);
}

.news-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 18px 40px rgba(10, 12, 24, 0.08);
}

.news-card__media {
  aspect-ratio: 16 / 9;
  border-radius: 0;
}

.news-card__body {
  padding: 22px 24px 26px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.news-card__date {
  font-size: 12.5px;
  color: var(--c-text-subtle);
}

.news-card__title {
  margin-top: 8px;
  font-size: 18px;
  line-height: 1.5;
}

.news-card__summary {
  margin-top: 8px;
  font-size: 14px;
  color: var(--c-text-muted);
  line-height: 1.75;
  flex: 1;
}

.news-card__more {
  margin-top: 16px;
}

/* 底部 ROI 快捷测算横幅 + 咨询区 */
.roi-inline-card {
  background: linear-gradient(135deg, rgba(91, 75, 232, 0.22) 0%, rgba(30, 27, 75, 0.65) 100%);
  border: 1px solid rgba(165, 180, 252, 0.24);
  border-radius: 22px;
  padding: 32px 36px;
  margin-bottom: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
  flex-wrap: wrap;
}

.roi-inline-card__left {
  flex: 1;
  min-width: 280px;
}

.roi-inline-card__title {
  font-size: 22px;
  color: #ffffff;
  margin-bottom: 16px;
}

.roi-inline-card__slider-row {
  display: flex;
  align-items: center;
  gap: 18px;
  flex-wrap: wrap;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
}

.roi-inline-slider {
  width: min(320px, 100%);
  accent-color: #8b5cf6;
}

.roi-inline-card__right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.roi-inline-card__lbl {
  font-size: 12.5px;
  color: rgba(255, 255, 255, 0.7);
}

.roi-inline-card__val {
  font-size: 36px;
  font-weight: 800;
  color: #38bdf8;
  line-height: 1.1;
}

.consult-cta-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
  flex-wrap: wrap;
}

.consult-cta-box__title {
  font-size: clamp(28px, 3.6vw, 44px);
  color: #ffffff;
}

.consult-cta-box__sub {
  margin-top: 12px;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.74);
  max-width: 600px;
}

@media (max-width: 860px) {
  .service-group__lead {
    grid-template-columns: 1fr;
  }
  .roi-inline-card__right {
    align-items: flex-start;
  }
}
</style>
```

### `website/web/src/views/AboutView.vue`

```vue
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ApiError } from '@/api/http'
import { getAbout } from '@/api/public'
import { openConsult } from '@/composables/consult'
import { usePageMeta } from '@/composables/usePageMeta'
import { vReveal } from '@/directives/reveal'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import type { AboutPage } from '@/data/types'

const data = ref<AboutPage | null>(null)
const loading = ref(true)
const error = ref('')
const activeYear = ref<string>('ALL')
const qualFilter = ref('')

usePageMeta(() => ({
  title: '关于我们',
  description: data.value?.intro?.slice(0, 120) || '了解企业发展历程、技术实力与行业资质认证。',
}))

const milestoneYears = computed(() => {
  const list = data.value?.milestones ?? []
  const years = Array.from(new Set(list.map((m) => m.year).filter(Boolean)))
  return ['ALL', ...years]
})

const filteredMilestones = computed(() => {
  const list = data.value?.milestones ?? []
  if (activeYear.value === 'ALL') return list
  return list.filter((m) => m.year === activeYear.value)
})

const filteredQualifications = computed(() => {
  const list = data.value?.qualifications ?? []
  const kw = qualFilter.value.trim().toLowerCase()
  if (!kw) return list
  return list.filter(
    (q) => q.name.toLowerCase().includes(kw) || (q.description ?? '').toLowerCase().includes(kw),
  )
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    data.value = await getAbout()
  } catch (err) {
    data.value = null
    error.value =
      err instanceof ApiError && err.code === 'NOT_FOUND'
        ? '「关于我们」页面尚未发布，请管理员在后台发布后再访问。'
        : err instanceof ApiError
        ? err.message
        : '页面加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="about-page">
    <PageHeader
      kicker="ABOUT US · 关于我们"
      title="以智能物联网与 AI 重塑产业生产力"
      subtitle="连接物理世界与数字智能，持续为行业领军客户创造可衡量的安全与经营价值。"
    />

    <div v-if="loading" class="container state">
      <div class="skeleton"></div>
    </div>

    <div v-else-if="error" class="container state">
      <EmptyState title="内容暂不可用" :description="error">
        <button type="button" class="btn btn--primary" @click="load">重试</button>
      </EmptyState>
    </div>

    <template v-else-if="data">
      <!-- 1. 公司简介与企业使命 Bento 双卡 -->
      <section v-if="data.intro || data.mission" v-reveal class="section">
        <div class="container">
          <div class="bento-grid">
            <div v-if="data.intro" class="bento-card bento-card--intro">
              <span class="kicker">COMPANY PROFILE · 企业简介</span>
              <h2 class="bento-card__title">深耕产业数字化与智能连接</h2>
              <p class="bento-card__text">{{ data.intro }}</p>
            </div>
            <div v-if="data.mission" class="bento-card bento-card--mission">
              <span class="eyebrow">MISSION & VISION · 使命与愿景</span>
              <h2 class="bento-card__title">用数字技术驱动每一份安全与效率</h2>
              <p class="bento-card__text">{{ data.mission }}</p>
              <div class="bento-card__cta">
                <button
                  type="button"
                  class="btn btn--outline-light"
                  @click="openConsult({ sourcePosition: 'OTHER_CTA', title: '了解企业合作与生态伙伴计划' })"
                >
                  洽谈战略合作 →
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 2. 交互式企业发展历程时间轴 (Milestone Timeline) -->
      <section v-if="data.milestones?.length" v-reveal class="section section--surface">
        <div class="container">
          <div class="section-head">
            <div>
              <span class="kicker">MILESTONES · 发展历程</span>
              <h2 class="section-title">一路向前的关键足迹</h2>
            </div>

            <div v-if="milestoneYears.length > 2" class="year-pills">
              <button
                v-for="yr in milestoneYears"
                :key="yr"
                type="button"
                class="year-pill"
                :class="{ 'is-active': activeYear === yr }"
                @click="activeYear = yr"
              >
                {{ yr === 'ALL' ? '全部年份' : `${yr} 年` }}
              </button>
            </div>
          </div>

          <div class="timeline">
            <div
              v-for="(item, idx) in filteredMilestones"
              :key="`${item.year}-${idx}`"
              class="timeline__node"
            >
              <div class="timeline__year-col">
                <span class="timeline__year">{{ item.year }}</span>
                <span class="timeline__dot" aria-hidden="true"></span>
              </div>
              <div class="timeline__card">
                <h3 class="timeline__title">{{ item.title }}</h3>
                <p v-if="item.description" class="timeline__desc">{{ item.description }}</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 3. 权威资质与技术认证画廊 (Qualifications) -->
      <section v-if="data.qualifications?.length" v-reveal class="section">
        <div class="container">
          <div class="section-head">
            <div>
              <span class="kicker">QUALIFICATIONS & HONORS · 资质与荣誉</span>
              <h2 class="section-title">国家级权威认证与技术合规保障</h2>
            </div>
            <input
              v-if="data.qualifications.length > 4"
              v-model="qualFilter"
              type="search"
              class="qual-search"
              placeholder="搜索资质或认证名称..."
            />
          </div>

          <div class="grid grid--3">
            <div v-for="(q, idx) in filteredQualifications" :key="`${q.name}-${idx}`" class="qual-card">
              <div class="qual-card__icon" aria-hidden="true">🏅</div>
              <div>
                <h3 class="qual-card__title">{{ q.name }}</h3>
                <p v-if="q.description" class="qual-card__desc">{{ q.description }}</p>
              </div>
            </div>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.bento-grid {
  display: grid;
  grid-template-columns: 1.25fr 1fr;
  gap: 28px;
}

@media (max-width: 860px) {
  .bento-grid {
    grid-template-columns: 1fr;
  }
}

.bento-card {
  border-radius: var(--radius-lg);
  padding: 40px 36px;
  display: flex;
  flex-direction: column;
}

.bento-card--intro {
  background: var(--c-surface);
  border: 1px solid var(--c-border);
}

.bento-card--mission {
  background: linear-gradient(155deg, #0b0d16 0%, #191736 60%, #2d2168 100%);
  color: #ffffff;
}

.bento-card__title {
  font-size: 26px;
  margin-bottom: 16px;
}

.bento-card--mission .bento-card__title {
  color: #ffffff;
}

.bento-card__text {
  font-size: 16px;
  line-height: 1.9;
  white-space: pre-line;
  color: var(--c-text-muted);
}

.bento-card--mission .bento-card__text {
  color: rgba(255, 255, 255, 0.8);
}

.bento-card__cta {
  margin-top: auto;
  padding-top: 24px;
}

/* 时间轴 */
.year-pills {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.year-pill {
  padding: 6px 14px;
  border-radius: 999px;
  border: 1px solid var(--c-border-strong);
  background: #ffffff;
  font-size: 13px;
  color: var(--c-text-muted);
  cursor: pointer;
}

.year-pill.is-active {
  background: var(--c-brand);
  border-color: var(--c-brand);
  color: #ffffff;
}

.timeline {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.timeline__node {
  display: grid;
  grid-template-columns: 140px 1fr;
  gap: 24px;
  align-items: stretch;
}

@media (max-width: 640px) {
  .timeline__node {
    grid-template-columns: 1fr;
    gap: 8px;
  }
}

.timeline__year-col {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  position: relative;
}

.timeline__year {
  font-size: 24px;
  font-weight: 800;
  color: var(--c-brand);
  letter-spacing: -0.02em;
}

.timeline__dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--c-brand);
  box-shadow: 0 0 0 4px #eeebff;
}

.timeline__card {
  background: #ffffff;
  border: 1px solid var(--c-border);
  border-radius: 16px;
  padding: 22px 26px;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.timeline__card:hover {
  transform: translateX(4px);
  border-color: rgba(91, 75, 232, 0.35);
  box-shadow: 0 12px 32px rgba(10, 12, 20, 0.06);
}

.timeline__title {
  font-size: 18px;
  font-weight: 600;
}

.timeline__desc {
  margin-top: 8px;
  font-size: 14.5px;
  color: var(--c-text-muted);
  line-height: 1.75;
}

/* 资质卡片 */
.qual-search {
  height: 38px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid var(--c-border-strong);
  font-size: 13.5px;
  outline: none;
}

.qual-search:focus {
  border-color: var(--c-brand);
}

.qual-card {
  background: #ffffff;
  border: 1px solid var(--c-border);
  border-radius: 16px;
  padding: 22px 24px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  transition: transform 0.2s ease, border-color 0.2s ease;
}

.qual-card:hover {
  transform: translateY(-3px);
  border-color: var(--c-brand);
}

.qual-card__icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: #eeebff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex: none;
}

.qual-card__title {
  font-size: 16px;
  font-weight: 600;
}

.qual-card__desc {
  margin-top: 6px;
  font-size: 13.5px;
  color: var(--c-text-muted);
  line-height: 1.65;
}
</style>
```

### `website/admin/src/api/content.ts`

```typescript
import { get, request } from './http'

export type PublicationStatus = 'UNPUBLISHED' | 'PUBLISHED' | 'OFFLINE'

export interface ContentView {
  id: string
  contentType: string
  title: string
  status: PublicationStatus
  draftVersion: number
  publishedVersion: number | null
  lockVersion: number
  hasUnpublishedChanges: boolean
  categoryId: string | null
  categoryName: string | null
  sortOrder: number
  displayTime: string | null
  publishedAt: string | null
  coverMediaId: string | null
  updatedAt: string | null
}

export interface ContentDetail {
  content: ContentView
  draft: Record<string, unknown>
  published: Record<string, unknown> | null
}

export interface CategoryView {
  id: string
  categoryType: string
  name: string
  publishedName: string | null
  status: PublicationStatus
  draftVersion: number
  publishedVersion: number | null
  lockVersion: number
  sortOrder: number
  publishedSortOrder: number | null
  hasUnpublishedChanges: boolean
}

export interface MediaItem {
  id: string
  originalName: string
  mimeType: string
  mediaType: 'IMAGE' | 'VIDEO'
  width: number
  height: number
  durationMs: number | null
  byteSize: number
  url: string
  createdAt: string | null
  referenced: boolean
}

export interface MediaPage {
  items: MediaItem[]
  page: number
  pageSize: number
  total: number
}

// ---- 内容 ----

export function listContents(params: {
  contentType: string
  keyword?: string
  categoryId?: string
  publicationStatus?: string
  hasUnpublishedChanges?: boolean
  page: number
  pageSize: number
}) {
  const query = new URLSearchParams({
    contentType: params.contentType,
    page: String(params.page),
    pageSize: String(params.pageSize),
  })
  if (params.keyword) query.set('keyword', params.keyword)
  if (params.categoryId) query.set('categoryId', params.categoryId)
  if (params.publicationStatus) query.set('publicationStatus', params.publicationStatus)
  if (params.hasUnpublishedChanges) query.set('hasUnpublishedChanges', 'true')
  return get<{ items: ContentView[]; total: number }>(`/api/admin/contents?${query.toString()}`)
}

export function getContent(id: string) {
  return get<ContentDetail>(`/api/admin/contents/${id}`)
}

export function createContent(contentType: string, draft: Record<string, unknown>) {
  return request<ContentDetail>('/api/admin/contents', {
    method: 'POST',
    body: { contentType, draft },
  })
}

export function saveDraft(id: string, draft: Record<string, unknown>, expectedLockVersion: number) {
  return request<ContentDetail>(`/api/admin/contents/${id}/draft`, {
    method: 'PUT',
    body: { expectedLockVersion, draft },
  })
}

export function publishContent(id: string, expectedLockVersion: number, expectedDraftVersion: number) {
  return request<void>(`/api/admin/contents/${id}/publish`, {
    method: 'POST',
    body: { expectedLockVersion, expectedDraftVersion },
  })
}

export function offlineContent(id: string, expectedLockVersion: number, expectedDraftVersion = 1) {
  return request<void>(`/api/admin/contents/${id}/offline`, {
    method: 'POST',
    body: { expectedLockVersion, expectedDraftVersion },
  })
}

export function deleteContent(id: string, expectedLockVersion: number) {
  return request<void>(`/api/admin/contents/${id}?expectedLockVersion=${expectedLockVersion}`, {
    method: 'DELETE',
  })
}

/** 一键克隆内容为新草稿副本 */
export async function cloneContent(id: string) {
  const detail = await getContent(id)
  const clonedDraft: Record<string, unknown> = {
    ...detail.draft,
    title: `${String(detail.draft.title || detail.content.title || '未命名')} (副本)`,
  }
  return createContent(detail.content.contentType, clonedDraft)
}

// ---- 分类 ----

export interface CategoryPage {
  items: CategoryView[]
  page: number
  pageSize: number
  total: number
}

export function listCategories(params?: string | { categoryType?: string; keyword?: string; page?: number; pageSize?: number }) {
  if (typeof params === 'string') {
    return get<CategoryPage>(`/api/admin/categories?categoryType=${encodeURIComponent(params)}&pageSize=100`)
  }
  const query = new URLSearchParams()
  if (params?.categoryType) query.set('categoryType', params.categoryType)
  if (params?.keyword) query.set('keyword', params.keyword)
  if (params?.page) query.set('page', String(params.page))
  if (params?.pageSize) query.set('pageSize', String(params.pageSize))
  return get<CategoryPage>(`/api/admin/categories?${query.toString()}`)
}

export function createCategory(
  arg1: string | { categoryType: string; name: string; sortOrder?: number },
  name?: string,
  sortOrder = 100,
) {
  const body =
    typeof arg1 === 'string'
      ? { categoryType: arg1, name: name ?? '', sortOrder }
      : arg1
  return request<string>('/api/admin/categories', {
    method: 'POST',
    body,
  })
}

export function updateCategory(id: string, body: { expectedLockVersion: number; name: string; sortOrder?: number }) {
  return request<void>(`/api/admin/categories/${id}/draft`, {
    method: 'PUT',
    body,
  })
}

export function saveCategory(id: string, name: string, sortOrder: number, expectedLockVersion: number) {
  return updateCategory(id, { expectedLockVersion, name, sortOrder })
}

export function publishCategory(id: string, expectedLockVersion: number, expectedDraftVersion: number) {
  return request<void>(`/api/admin/categories/${id}/publish`, {
    method: 'POST',
    body: { expectedLockVersion, expectedDraftVersion },
  })
}

export function offlineCategory(id: string, expectedLockVersion: number, expectedDraftVersion = 1) {
  return request<void>(`/api/admin/categories/${id}/offline`, {
    method: 'POST',
    body: { expectedLockVersion, expectedDraftVersion },
  })
}

export function deleteCategory(id: string, expectedLockVersion: number) {
  return request<void>(`/api/admin/categories/${id}?expectedLockVersion=${expectedLockVersion}`, {
    method: 'DELETE',
  })
}

// ---- 素材 ----

export function listMedia(params: {
  keyword?: string
  type?: '' | 'IMAGE' | 'VIDEO'
  referenced?: boolean
  page: number
  pageSize: number
}) {
  const query = new URLSearchParams({
    page: String(params.page),
    pageSize: String(params.pageSize),
  })
  if (params.keyword) query.set('keyword', params.keyword)
  if (params.type) query.set('type', params.type)
  if (params.referenced !== undefined) query.set('referenced', String(params.referenced))
  return get<MediaPage>(`/api/admin/media?${query.toString()}`)
}

export function getMedia(id: string | number) {
  return get<MediaItem>(`/api/admin/media/${id}`)
}

export function deleteMedia(id: string | number) {
  return request<void>(`/api/admin/media/${id}`, { method: 'DELETE' })
}

export function mediaReferences(id: string | number) {
  return get<Array<Record<string, string>>>(`/api/admin/media/${id}/references`)
}
```

### `website/admin/src/components/MediaPicker.vue`

```vue
<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ApiError, upload } from '@/api/http'
import { listMedia, type MediaItem } from '@/api/content'

const props = defineProps<{
  modelValue: boolean
  selectedId?: string | null
  kind?: 'IMAGE' | 'VIDEO' | 'ALL'
}>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  select: [media: MediaItem]
}>()

const loading = ref(false)
const items = ref<MediaItem[]>([])
const keyword = ref('')
const page = ref(1)
const pageSize = 24
const total = ref(0)
const subFilter = ref<'' | 'IMAGE' | 'VIDEO'>('')
const uploading = ref(false)
const dragOver = ref(false)
const fileInput = ref<HTMLInputElement>()

const filterType = computed<'' | 'IMAGE' | 'VIDEO' | undefined>(() => {
  if (props.kind === 'VIDEO') return 'VIDEO'
  if (props.kind === 'IMAGE') return 'IMAGE'
  return subFilter.value || undefined
})

const accept = computed(() => {
  if (props.kind === 'VIDEO') return 'video/mp4,video/webm'
  if (props.kind === 'IMAGE') return 'image/png,image/jpeg,image/webp'
  return 'image/png,image/jpeg,image/webp,video/mp4,video/webm'
})

const dialogTitle = computed(() =>
  props.kind === 'VIDEO' ? '选择视频' : props.kind === 'ALL' ? '选择素材（图片/视频）' : '选择图片',
)
const uploadLabel = computed(() =>
  props.kind === 'VIDEO' ? '上传新视频' : props.kind === 'ALL' ? '上传新素材' : '上传新图片',
)
const hint = computed(() =>
  props.kind === 'VIDEO'
    ? '支持 MP4、WebM 视频，单个不超过 50 MB。视频将静音循环播放。'
    : props.kind === 'ALL'
    ? '支持直接拖拽文件至此上传：JPG、PNG、WebP 图片（≤5 MB）与 MP4、WebM 视频（≤50 MB）。'
    : '支持直接拖拽图片至此上传：JPG、PNG、WebP，单张不超过 5 MB。缩略图为后台鉴权地址，公开地址在发布后生效。',
)

async function load() {
  loading.value = true
  try {
    const data = await listMedia({
      keyword: keyword.value.trim() || undefined,
      type: filterType.value,
      page: page.value,
      pageSize,
    })
    items.value = data.items
    total.value = data.total
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '加载素材失败')
  } finally {
    loading.value = false
  }
}

watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      keyword.value = ''
      page.value = 1
      subFilter.value = ''
      load()
    }
  },
)

async function uploadSingleFile(file: File) {
  uploading.value = true
  try {
    const form = new FormData()
    form.append('file', file)
    const media = await upload<MediaItem>('/api/admin/media', form)
    ElMessage.success('上传成功')
    page.value = 1
    await load()
    emit('select', media)
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '上传失败')
  } finally {
    uploading.value = false
  }
}

async function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  await uploadSingleFile(file)
  input.value = ''
}

async function onDrop(event: DragEvent) {
  dragOver.value = false
  const file = event.dataTransfer?.files?.[0]
  if (!file) return
  await uploadSingleFile(file)
}

function choose(media: MediaItem) {
  emit('select', media)
}

function close() {
  emit('update:modelValue', false)
}

function thumbUrl(media: MediaItem) {
  return `/api/admin/media/${media.id}/file`
}

function formatSize(bytes: number) {
  if (!bytes) return ''
  if (bytes < 1024 * 1024) return `${Math.round(bytes / 1024)}KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)}MB`
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    :title="dialogTitle"
    width="860px"
    top="5vh"
    @update:model-value="(v: boolean) => emit('update:modelValue', v)"
  >
    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="按原始文件名搜索..."
        clearable
        style="width: 240px"
        @keyup.enter="page = 1; load()"
      />
      <el-select
        v-if="kind === 'ALL'"
        v-model="subFilter"
        style="width: 120px"
        placeholder="全部类型"
        @change="page = 1; load()"
      >
        <el-option label="全部类型" value="" />
        <el-option label="仅图片" value="IMAGE" />
        <el-option label="仅视频" value="VIDEO" />
      </el-select>
      <el-button @click="page = 1; load()">查询</el-button>
      <div class="spacer"></div>
      <input ref="fileInput" type="file" :accept="accept" hidden @change="onFileChange" />
      <el-button type="primary" :loading="uploading" @click="fileInput?.click()">
        + {{ uploadLabel }}
      </el-button>
    </div>

    <div
      class="dropzone"
      :class="{ 'is-dragover': dragOver }"
      @dragover.prevent="dragOver = true"
      @dragleave.prevent="dragOver = false"
      @drop.prevent="onDrop"
    >
      <span>{{ hint }}</span>
    </div>

    <div v-loading="loading" class="grid">
      <button
        v-for="media in items"
        :key="media.id"
        type="button"
        class="item"
        :class="{ 'is-selected': String(media.id) === String(selectedId) }"
        :title="`${media.originalName} (${formatSize(media.byteSize)})`"
        @click="choose(media)"
      >
        <video
          v-if="media.mediaType === 'VIDEO'"
          :src="thumbUrl(media)"
          class="item__media"
          muted
          playsinline
          preload="metadata"
        ></video>
        <img v-else :src="thumbUrl(media)" :alt="media.originalName" class="item__media" loading="lazy" />
        <span class="item__name">{{ media.originalName }}</span>
        <span class="item__meta">
          {{ media.mediaType === 'VIDEO' ? '▶ 视频' : `${media.width}×${media.height}` }}
        </span>
      </button>
      <el-empty v-if="!loading && items.length === 0" description="还没有匹配的素材，可点击右上角或拖拽文件上传" />
    </div>

    <div class="pager">
      <span class="pager__total">共 {{ total }} 个素材</span>
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="load"
      />
    </div>

    <template #footer>
      <el-button @click="close">取消</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.spacer {
  flex: 1;
}

.dropzone {
  margin: 12px 0 14px;
  padding: 10px 14px;
  border-radius: 8px;
  border: 1px dashed var(--a-border);
  background: #fafbfe;
  font-size: 12px;
  color: var(--a-text-muted);
  transition: all 0.2s ease;
}

.dropzone.is-dragover {
  border-color: var(--el-color-primary);
  background: #eef5fd;
  color: var(--el-color-primary);
}

.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  max-height: 420px;
  overflow-y: auto;
  min-height: 160px;
}

.item {
  position: relative;
  border: 2px solid var(--a-border);
  border-radius: 8px;
  padding: 0;
  background: #0f172a;
  cursor: pointer;
  overflow: hidden;
  aspect-ratio: 16 / 10;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item:hover {
  border-color: var(--el-color-primary);
}

.item.is-selected {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 0 2px var(--el-color-primary-light-8);
}

.item__media {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.item__name {
  position: absolute;
  left: 6px;
  top: 6px;
  max-width: 75%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
}

.item__meta {
  position: absolute;
  right: 6px;
  bottom: 6px;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.65);
  color: #fff;
}

.pager {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pager__total {
  font-size: 12px;
  color: var(--a-text-muted);
}
</style>
```

### `website/admin/src/components/LinkEditor.vue`

```vue
<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { listContents, type ContentView } from '@/api/content'

export interface LinkValue {
  type: string
  pageCode?: string
  contentType?: string
  contentId?: string
  url?: string
  newWindow?: boolean
  anchorCode?: string
}

const props = withDefaults(
  defineProps<{
    modelValue: LinkValue | null
    allowNone?: boolean
    allowConsult?: boolean
    placeholder?: string
  }>(),
  { allowNone: true, allowConsult: false, placeholder: '选择链接' },
)

const emit = defineEmits<{ 'update:modelValue': [value: LinkValue | null] }>()

const PAGES = [
  { value: 'HOME', label: '首页' },
  { value: 'PRODUCTS', label: '产品列表' },
  { value: 'SOLUTIONS', label: '解决方案列表' },
  { value: 'CASES', label: '客户案例列表' },
  { value: 'NEWS', label: '新闻列表' },
  { value: 'ABOUT', label: '关于我们' },
  { value: 'CONTACT', label: '联系我们' },
  { value: 'PRIVACY', label: '隐私说明' },
]

const ANCHORS = [
  { value: 'metrics', label: '企业指标' },
  { value: 'services', label: '产品与解决方案' },
  { value: 'cases', label: '客户案例' },
  { value: 'customers', label: '客户标识' },
  { value: 'news', label: '新闻动态' },
  { value: 'consultation', label: '咨询区' },
]

const CONTENT_TYPES = computed(() => [
  { value: 'PRODUCT', label: '产品' },
  { value: 'SOLUTION', label: '解决方案' },
  { value: 'CASE', label: '客户案例' },
  { value: 'NEWS', label: '新闻' },
])

const CONSULT_CONTENT_TYPES = [
  { value: '', label: '通用咨询（不限内容）' },
  { value: 'PRODUCT', label: '关联具体产品' },
  { value: 'SOLUTION', label: '关联解决方案' },
  { value: 'CASE', label: '关联客户案例' },
]

const typeOptions = computed(() => {
  const base = [
    { value: 'PAGE', label: '站内页面' },
    { value: 'CONTENT', label: '内容详情' },
    { value: 'EXTERNAL', label: '外部链接' },
    { value: 'ANCHOR', label: '首页锚点' },
  ]
  if (props.allowConsult) base.push({ value: 'CONSULT', label: '咨询动作' })
  if (props.allowNone) base.unshift({ value: 'NONE', label: '无链接' })
  return base
})

const type = computed({
  get: () => props.modelValue?.type ?? (props.allowNone ? 'NONE' : 'PAGE'),
  set: (value: string) => {
    if (value === 'NONE') {
      update({ type: 'NONE' })
    } else if (value === 'PAGE') {
      update({ type: 'PAGE', pageCode: props.modelValue?.pageCode ?? 'HOME' })
    } else if (value === 'CONTENT') {
      update({ type: 'CONTENT', contentType: CONTENT_TYPES.value[0].value, contentId: '' })
    } else if (value === 'EXTERNAL') {
      update({ type: 'EXTERNAL', url: props.modelValue?.url ?? '', newWindow: false })
    } else if (value === 'ANCHOR') {
      update({ type: 'ANCHOR', anchorCode: 'services' })
    } else if (value === 'CONSULT') {
      update({ type: 'CONSULT' })
    }
  },
})

const contents = ref<ContentView[]>([])
const loading = ref(false)

async function loadContents(contentType: string) {
  if (!contentType) {
    contents.value = []
    return
  }
  loading.value = true
  try {
    const data = await listContents({ contentType, publicationStatus: 'PUBLISHED', page: 1, pageSize: 50 })
    contents.value = data.items
  } catch {
    contents.value = []
  } finally {
    loading.value = false
  }
}

watch(
  () => [props.modelValue?.type, props.modelValue?.contentType],
  ([currentType, contentType]) => {
    if (currentType === 'CONTENT' || currentType === 'CONSULT') {
      if (contentType) loadContents(String(contentType))
      else contents.value = []
    }
  },
  { immediate: true },
)

function update(next: LinkValue | null) {
  emit('update:modelValue', next)
}

function patch(partial: Partial<LinkValue>) {
  update({ ...(props.modelValue ?? { type: 'NONE' }), ...partial } as LinkValue)
}
</script>

<template>
  <div class="link-editor">
    <el-select :model-value="type" style="width: 130px" @update:model-value="type = $event">
      <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
    </el-select>

    <template v-if="type === 'PAGE'">
      <el-select
        :model-value="modelValue?.pageCode"
        placeholder="选择页面"
        style="width: 180px"
        @update:model-value="(v: string) => patch({ pageCode: v })"
      >
        <el-option v-for="item in PAGES" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
    </template>

    <template v-else-if="type === 'CONTENT'">
      <el-select
        :model-value="modelValue?.contentType"
        style="width: 120px"
        @update:model-value="(v: string) => patch({ contentType: v, contentId: '' })"
      >
        <el-option v-for="item in CONTENT_TYPES" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select
        :model-value="modelValue?.contentId != null ? String(modelValue.contentId) : ''"
        :loading="loading"
        filterable
        placeholder="选择已发布内容"
        style="width: 220px"
        @update:model-value="(v: string) => patch({ contentId: v })"
      >
        <el-option v-for="item in contents" :key="item.id" :label="item.title" :value="String(item.id)" />
      </el-select>
    </template>

    <template v-else-if="type === 'EXTERNAL'">
      <el-input
        :model-value="modelValue?.url"
        placeholder="https://..."
        style="width: 240px"
        @update:model-value="(v: string) => patch({ url: v })"
      />
      <el-checkbox
        :model-value="Boolean(modelValue?.newWindow)"
        @update:model-value="(v: boolean) => patch({ newWindow: v })"
      >
        新窗口
      </el-checkbox>
    </template>

    <template v-else-if="type === 'ANCHOR'">
      <el-select
        :model-value="modelValue?.anchorCode"
        placeholder="选择锚点"
        style="width: 160px"
        @update:model-value="(v: string) => patch({ anchorCode: v })"
      >
        <el-option v-for="item in ANCHORS" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
    </template>

    <template v-else-if="type === 'CONSULT'">
      <el-select
        :model-value="modelValue?.contentType ?? ''"
        placeholder="关联咨询来源（可选）"
        style="width: 170px"
        @update:model-value="(v: string) => patch({ contentType: v || undefined, contentId: undefined })"
      >
        <el-option v-for="item in CONSULT_CONTENT_TYPES" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select
        v-if="modelValue?.contentType"
        :model-value="modelValue?.contentId != null ? String(modelValue.contentId) : ''"
        :loading="loading"
        filterable
        placeholder="选择关联的已发布内容"
        style="width: 210px"
        @update:model-value="(v: string) => patch({ contentId: v || undefined })"
      >
        <el-option v-for="item in contents" :key="item.id" :label="item.title" :value="String(item.id)" />
      </el-select>
    </template>
  </div>
</template>

<style scoped>
.link-editor {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
```

### `website/admin/src/views/HomeManageView.vue`

```vue
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ConfigPanel from '@/components/ConfigPanel.vue'
import ContentPicker from '@/components/ContentPicker.vue'
import LinkEditor, { type LinkValue } from '@/components/LinkEditor.vue'
import MediaPicker from '@/components/MediaPicker.vue'
import { useConfig } from '@/composables/useConfig'
import type { MediaItem } from '@/api/content'

interface HeroItem {
  key?: string
  enabled: boolean
  sortOrder: number
  title: string
  subtitle: string
  desktopMediaId: number | null
  mobileMediaId: number | null
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
  imageMediaId: number | null
  contentType: 'PRODUCT' | 'SOLUTION'
  contentIds: number[]
  moreLink: LinkValue | null
}

interface HomeDraft {
  sectionOrder?: string[]
  hero: { items: HeroItem[] }
  metrics: { enabled: boolean; title: string; subtitle: string; items: MetricItem[] }
  services: { enabled: boolean; title: string; subtitle: string; groups: ServiceGroup[] }
  cases: { enabled: boolean; title: string; subtitle: string; contentIds: number[] }
  customers: { enabled: boolean; title: string; subtitle: string; contentIds: number[] }
  news: {
    enabled: boolean
    title: string
    subtitle: string
    mode: 'SELECTED' | 'LATEST'
    limit: number
    contentIds: number[]
  }
  consultation: {
    title: string
    subtitle: string
    buttonText: string
    link: LinkValue | null
  }
}

const SECTION_LABELS: Record<string, { name: string; desc: string }> = {
  metrics: { name: '企业关键指标 (metrics)', desc: '四列大数字规模与实力背书' },
  services: { name: '产品与解决方案分组 (services)', desc: '左右交错核心产品与方案推荐卡' },
  cases: { name: '标杆客户案例 (cases)', desc: '行业头部客户实践与成果数据' },
  customers: { name: '合作客户标识墙 (customers)', desc: '合作伙伴与客户 Logo 墙/跑马灯' },
  news: { name: '最新动态与洞察 (news)', desc: '手选或自动按最新日期展示企业动态' },
  consultation: { name: '底部咨询与 ROI 行动区 (consultation)', desc: '全站转化收口与专家预约引导' },
}

const DEFAULT_ORDER = ['metrics', 'services', 'cases', 'customers', 'news', 'consultation']

const router = useRouter()
const activeTab = ref('order')
const pickerOpen = ref(false)
const pickerKind = ref<'IMAGE' | 'VIDEO' | 'ALL'>('ALL')
let pickerCallback: ((media: MediaItem) => void) | null = null

const { view, loading, saving, publishing, loadError, load, save, publish } =
  useConfig<HomeDraft & Record<string, unknown>>('HOME')

const form = reactive<HomeDraft>({
  sectionOrder: [...DEFAULT_ORDER],
  hero: { items: [] },
  metrics: { enabled: true, title: '数字生产力规模背书', subtitle: '', items: [] },
  services: { enabled: true, title: '核心产品与行业方案', subtitle: '', groups: [] },
  cases: { enabled: true, title: '行业领军企业的共同选择', subtitle: '', contentIds: [] },
  customers: { enabled: true, title: '生态伙伴与标杆客户', subtitle: '', contentIds: [] },
  news: { enabled: true, title: '最新动态与前沿洞察', subtitle: '', mode: 'LATEST', limit: 3, contentIds: [] },
  consultation: {
    title: '准备好开启企业数字化生产力跃迁了吗？',
    subtitle: '留下您的业务痛点，我们的行业架构师将在 1 个工作日内为您提供定制方案与演示。',
    buttonText: '立即预约咨询',
    link: { type: 'CONSULT' },
  },
})

function fill() {
  const draft = view.value?.draft
  if (!draft) return

  const rawOrder = Array.isArray(draft.sectionOrder) ? draft.sectionOrder : []
  const mergedOrder = rawOrder.filter((k) => DEFAULT_ORDER.includes(k))
  for (const defKey of DEFAULT_ORDER) {
    if (!mergedOrder.includes(defKey)) mergedOrder.push(defKey)
  }
  form.sectionOrder = mergedOrder

  form.hero.items = (draft.hero?.items ?? []).map((item, idx) => ({
    key: item.key,
    enabled: item.enabled ?? true,
    sortOrder: item.sortOrder ?? (idx + 1) * 10,
    title: item.title ?? '',
    subtitle: item.subtitle ?? '',
    desktopMediaId: item.desktopMediaId != null ? Number(item.desktopMediaId) : null,
    mobileMediaId: item.mobileMediaId != null ? Number(item.mobileMediaId) : null,
    imageAlt: item.imageAlt ?? '',
    buttonText: item.buttonText ?? '了解更多',
    link: (item.link as LinkValue | null) ?? { type: 'CONSULT' },
  }))

  form.metrics = {
    enabled: draft.metrics?.enabled ?? true,
    title: draft.metrics?.title ?? '企业实力与服务规模',
    subtitle: draft.metrics?.subtitle ?? '',
    items: (draft.metrics?.items ?? []).map((m, idx) => ({
      key: m.key,
      sortOrder: m.sortOrder ?? (idx + 1) * 10,
      displayValue: m.displayValue ?? '',
      unit: m.unit ?? '',
      label: m.label ?? '',
      description: m.description ?? '',
    })),
  }

  form.services = {
    enabled: draft.services?.enabled ?? true,
    title: draft.services?.title ?? '核心产品与行业方案',
    subtitle: draft.services?.subtitle ?? '',
    groups: (draft.services?.groups ?? []).map((g, idx) => ({
      key: g.key,
      sortOrder: g.sortOrder ?? (idx + 1) * 10,
      title: g.title ?? '',
      description: g.description ?? '',
      imageMediaId: g.imageMediaId != null ? Number(g.imageMediaId) : null,
      contentType: g.contentType === 'SOLUTION' ? 'SOLUTION' : 'PRODUCT',
      contentIds: (g.contentIds ?? []).map((id) => Number(id)),
      moreLink: (g.moreLink as LinkValue | null) ?? { type: 'PAGE', pageCode: 'PRODUCTS' },
    })),
  }

  form.cases = {
    enabled: draft.cases?.enabled ?? true,
    title: draft.cases?.title ?? '客户案例',
    subtitle: draft.cases?.subtitle ?? '',
    contentIds: (draft.cases?.contentIds ?? []).map((id) => Number(id)),
  }

  form.customers = {
    enabled: draft.customers?.enabled ?? true,
    title: draft.customers?.title ?? '合作客户',
    subtitle: draft.customers?.subtitle ?? '',
    contentIds: (draft.customers?.contentIds ?? []).map((id) => Number(id)),
  }

  form.news = {
    enabled: draft.news?.enabled ?? true,
    title: draft.news?.title ?? '最新动态',
    subtitle: draft.news?.subtitle ?? '',
    mode: draft.news?.mode === 'LATEST' ? 'LATEST' : 'SELECTED',
    limit: draft.news?.limit ?? 3,
    contentIds: (draft.news?.contentIds ?? []).map((id) => Number(id)),
  }

  form.consultation = {
    title: draft.consultation?.title ?? '了解适合您的数字化服务',
    subtitle: draft.consultation?.subtitle ?? '',
    buttonText: draft.consultation?.buttonText ?? '立即咨询',
    link: (draft.consultation?.link as LinkValue | null) ?? { type: 'CONSULT' },
  }
}

function moveSection(idx: number, delta: number) {
  if (!form.sectionOrder) return
  const target = idx + delta
  if (target < 0 || target >= form.sectionOrder.length) return
  const [moved] = form.sectionOrder.splice(idx, 1)
  form.sectionOrder.splice(target, 0, moved)
}

function isSectionEnabled(secKey: string): boolean {
  if (secKey === 'consultation') return true
  const block = (form as Record<string, any>)[secKey]
  return Boolean(block?.enabled)
}

function toggleSectionEnabled(secKey: string, val: boolean) {
  if (secKey === 'consultation') return
  const block = (form as Record<string, any>)[secKey]
  if (block) block.enabled = val
}

function openMedia(kind: 'IMAGE' | 'VIDEO' | 'ALL', cb: (m: MediaItem) => void) {
  pickerKind.value = kind
  pickerCallback = cb
  pickerOpen.value = true
}

function onMediaPicked(media: MediaItem) {
  if (pickerCallback) {
    pickerCallback(media)
    pickerCallback = null
  }
  pickerOpen.value = false
}

function addHero() {
  if (form.hero.items.length >= 5) {
    ElMessage.warning('首屏轮播最多 5 条')
    return
  }
  form.hero.items.push({
    enabled: true,
    sortOrder: (form.hero.items.length + 1) * 10,
    title: '让物联网与 AI 成为企业增长引擎',
    subtitle: '全场景实时感知、智能风控与数字化协同平台',
    desktopMediaId: null,
    mobileMediaId: null,
    imageAlt: '',
    buttonText: '预约演示',
    link: { type: 'CONSULT' },
  })
}

function addMetric() {
  if (form.metrics.items.length >= 8) {
    ElMessage.warning('关键指标最多 8 条')
    return
  }
  form.metrics.items.push({
    sortOrder: (form.metrics.items.length + 1) * 10,
    displayValue: '99.9',
    unit: '%',
    label: '平台可用率',
    description: '金融级高可用云原生架构保障',
  })
}

function addServiceGroup() {
  if (form.services.groups.length >= 3) {
    ElMessage.warning('服务分组最多 3 组')
    return
  }
  form.services.groups.push({
    sortOrder: (form.services.groups.length + 1) * 10,
    title: '智能硬件与 SaaS 平台',
    description: '软硬一体全栈自研，开箱即用赋能业务现场。',
    imageMediaId: null,
    contentType: 'PRODUCT',
    contentIds: [],
    moreLink: { type: 'PAGE', pageCode: 'PRODUCTS' },
  })
}

function applyPresetMetrics() {
  form.metrics.enabled = true
  form.metrics.title = '值得信赖的行业数字化基座'
  form.metrics.subtitle = '用真实在线运行规模验证每一份交付承诺'
  form.metrics.items = [
    { sortOrder: 10, displayValue: '320', unit: '万+', label: '在线物联网终端', description: '7×24h 毫秒级状态感知与采集' },
    { sortOrder: 20, displayValue: '7000', unit: '+', label: '头部标杆企业客户', description: '覆盖能源、冷链、危化、快消制造' },
    { sortOrder: 30, displayValue: '68', unit: '%', label: '高危事故率平均下降', description: 'AI 双目防碰撞与主动安全托管' },
    { sortOrder: 40, displayValue: '99.95', unit: '%', label: '云平台服务可用性', description: '多活容灾与国密级数据安全认证' },
  ]
  ElMessage.success('已填入行业标杆指标模板，保存草稿后生效')
}

async function onSave() {
  const payload = JSON.parse(JSON.stringify(form)) as HomeDraft & Record<string, unknown>
  if (await save(payload)) fill()
}

async function onPublish() {
  if (await publish()) fill()
}

onMounted(async () => {
  await load()
  fill()
})
</script>

<template>
  <ConfigPanel
    title="首页管理"
    :status="view?.status"
    :draft-version="view?.draftVersion"
    :published-version="view?.publishedVersion"
    :has-unpublished-changes="view?.hasUnpublishedChanges"
    :loading="loading"
    :saving="saving"
    :publishing="publishing"
    @save="onSave"
    @publish="onPublish"
  >
    <div class="studio-banner">
      <div>
        <strong class="studio-banner__title">🎨 想要更直观地边看边改？试试「全站实景交互预演工作台」</strong>
        <p class="studio-banner__desc">
          支持在真实官网画布上直接拖拽调整模块顺序、双击修改文字、点击替换图片/视频、绑定链接，修改结果与当前表单 100% 双向实时互通。
        </p>
      </div>
      <el-button type="primary" @click="router.push('/preview/home')">
        进入实景交互预演台 →
      </el-button>
    </div>

    <el-alert v-if="loadError" type="error" :closable="false" :title="loadError" class="err" />

    <el-tabs v-model="activeTab" class="home-tabs">
      <!-- Tab 0: 模块摆放顺序与总览 -->
      <el-tab-pane label="0. 模块摆放顺序与开关" name="order">
        <p class="tab-hint">
          首屏 Hero 固定置顶展示；下方 6 大业务模块可自由上下移动调整在前台首页的展示顺序，也可一键开启或隐藏。
        </p>
        <div class="order-list">
          <div v-for="(secKey, idx) in form.sectionOrder" :key="secKey" class="order-item">
            <span class="order-item__num">0{{ idx + 1 }}</span>
            <div class="order-item__info">
              <strong>{{ SECTION_LABELS[secKey]?.name || secKey }}</strong>
              <span>{{ SECTION_LABELS[secKey]?.desc }}</span>
            </div>
            <el-switch
              v-if="secKey !== 'consultation'"
              :model-value="isSectionEnabled(secKey)"
              active-text="启用"
              inactive-text="隐藏"
              @update:model-value="(v: boolean) => toggleSectionEnabled(secKey, v)"
            />
            <el-tag v-else size="small" type="info">常驻收口区</el-tag>
            <div class="order-item__btns">
              <el-button size="small" :disabled="idx === 0" @click="moveSection(idx, -1)">↑ 上移</el-button>
              <el-button
                size="small"
                :disabled="idx === (form.sectionOrder?.length ?? 0) - 1"
                @click="moveSection(idx, 1)"
              >
                ↓ 下移
              </el-button>
              <el-button size="small" type="primary" link @click="activeTab = secKey">编辑内容</el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- Tab 1: 首屏 Hero -->
      <el-tab-pane label="1. 首屏轮播 (Hero)" name="hero">
        <p class="tab-hint">最多 5 条首屏，支持桌面大图/视频与移动端竖图自适应。正式发布至少需 1 条有效首屏。</p>
        <div v-for="(slide, idx) in form.hero.items" :key="slide.key ?? `hero-${idx}`" class="block-card">
          <div class="block-card__head">
            <strong>首屏 #{{ idx + 1 }}</strong>
            <el-switch v-model="slide.enabled" active-text="启用" />
            <el-button link type="danger" @click="form.hero.items.splice(idx, 1)">删除</el-button>
          </div>
          <el-form label-width="110px">
            <el-form-item label="主标题" required>
              <el-input v-model="slide.title" maxlength="40" show-word-limit placeholder="最多 40 字" />
            </el-form-item>
            <el-form-item label="副标题">
              <el-input v-model="slide.subtitle" type="textarea" :rows="2" maxlength="100" show-word-limit />
            </el-form-item>
            <el-form-item label="桌面图片/视频" required>
              <div class="media-pick-row">
                <el-tag v-if="slide.desktopMediaId" type="success">素材 ID: #{{ slide.desktopMediaId }}</el-tag>
                <span v-else class="muted">未选择</span>
                <el-button size="small" @click="openMedia('ALL', (m) => (slide.desktopMediaId = Number(m.id)))">
                  选择素材
                </el-button>
                <el-button v-if="slide.desktopMediaId" size="small" link type="danger" @click="slide.desktopMediaId = null">
                  清除
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="手机端配图">
              <div class="media-pick-row">
                <el-tag v-if="slide.mobileMediaId" type="info">素材 ID: #{{ slide.mobileMediaId }}</el-tag>
                <span v-else class="muted">可选，未选时自动回退桌面素材</span>
                <el-button size="small" @click="openMedia('IMAGE', (m) => (slide.mobileMediaId = Number(m.id)))">
                  选择图片
                </el-button>
                <el-button v-if="slide.mobileMediaId" size="small" link type="danger" @click="slide.mobileMediaId = null">
                  清除
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="主按钮文案">
              <el-input v-model="slide.buttonText" maxlength="12" style="width: 220px" placeholder="例如：预约演示" />
            </el-form-item>
            <el-form-item label="按钮跳转动作">
              <LinkEditor v-model="slide.link" :allow-consult="true" />
            </el-form-item>
          </el-form>
        </div>
        <el-button @click="addHero">+ 添加首屏轮播项</el-button>
      </el-tab-pane>

      <!-- Tab 2: 企业指标 -->
      <el-tab-pane label="2. 企业指标 (Metrics)" name="metrics">
        <div class="tab-toolbar">
          <el-switch v-model="form.metrics.enabled" active-text="在前台启用该模块" />
          <el-button size="small" type="primary" plain @click="applyPresetMetrics">
            ✨ 一键填入行业标杆指标模板
          </el-button>
        </div>
        <el-form label-width="110px">
          <el-form-item label="区块标题">
            <el-input v-model="form.metrics.title" maxlength="40" show-word-limit />
          </el-form-item>
          <el-form-item label="区块副标题">
            <el-input v-model="form.metrics.subtitle" maxlength="200" show-word-limit />
          </el-form-item>
        </el-form>
        <div v-for="(m, idx) in form.metrics.items" :key="m.key ?? `m-${idx}`" class="metric-row">
          <el-input v-model="m.displayValue" maxlength="16" placeholder="数值(如 320)" style="width: 130px" />
          <el-input v-model="m.unit" maxlength="8" placeholder="单位(万+)" style="width: 90px" />
          <el-input v-model="m.label" maxlength="20" placeholder="指标名称" style="width: 180px" />
          <el-input v-model="m.description" maxlength="80" placeholder="补充说明（选填）" style="flex: 1" />
          <el-button link type="danger" @click="form.metrics.items.splice(idx, 1)">删除</el-button>
        </div>
        <el-button @click="addMetric">+ 添加企业指标</el-button>
      </el-tab-pane>

      <!-- Tab 3: 产品与解决方案分组 -->
      <el-tab-pane label="3. 产品与服务 (Services)" name="services">
        <div class="tab-toolbar">
          <el-switch v-model="form.services.enabled" active-text="在前台启用该模块" />
        </div>
        <el-form label-width="110px">
          <el-form-item label="模块主标题">
            <el-input v-model="form.services.title" maxlength="40" show-word-limit />
          </el-form-item>
          <el-form-item label="模块副标题">
            <el-input v-model="form.services.subtitle" maxlength="200" show-word-limit />
          </el-form-item>
        </el-form>

        <div v-for="(grp, gIdx) in form.services.groups" :key="grp.key ?? `grp-${gIdx}`" class="block-card">
          <div class="block-card__head">
            <strong>服务分组 #{{ gIdx + 1 }}</strong>
            <el-button link type="danger" @click="form.services.groups.splice(gIdx, 1)">删除分组</el-button>
          </div>
          <el-form label-width="110px">
            <el-form-item label="分组标题" required>
              <el-input v-model="grp.title" maxlength="30" show-word-limit />
            </el-form-item>
            <el-form-item label="分组说明">
              <el-input v-model="grp.description" type="textarea" :rows="2" maxlength="200" show-word-limit />
            </el-form-item>
            <el-form-item label="分组配图">
              <div class="media-pick-row">
                <el-tag v-if="grp.imageMediaId" type="success">素材 ID: #{{ grp.imageMediaId }}</el-tag>
                <span v-else class="muted">可选</span>
                <el-button size="small" @click="openMedia('ALL', (m) => (grp.imageMediaId = Number(m.id)))">
                  选择素材
                </el-button>
                <el-button v-if="grp.imageMediaId" size="small" link type="danger" @click="grp.imageMediaId = null">
                  清除
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="关联类型">
              <el-radio-group v-model="grp.contentType" @change="grp.contentIds = []">
                <el-radio value="PRODUCT">产品 (PRODUCT)</el-radio>
                <el-radio value="SOLUTION">解决方案 (SOLUTION)</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="选择推荐内容">
              <ContentPicker v-model="grp.contentIds" :content-type="grp.contentType" :max="6" />
            </el-form-item>
            <el-form-item label="更多链接">
              <LinkEditor v-model="grp.moreLink" :allow-consult="true" />
            </el-form-item>
          </el-form>
        </div>
        <el-button @click="addServiceGroup">+ 添加服务分组（最多 3 组）</el-button>
      </el-tab-pane>

      <!-- Tab 4: 客户案例 -->
      <el-tab-pane label="4. 客户案例 (Cases)" name="cases">
        <div class="tab-toolbar">
          <el-switch v-model="form.cases.enabled" active-text="在前台启用该模块" />
        </div>
        <el-form label-width="110px">
          <el-form-item label="模块标题">
            <el-input v-model="form.cases.title" maxlength="40" show-word-limit />
          </el-form-item>
          <el-form-item label="模块副标题">
            <el-input v-model="form.cases.subtitle" maxlength="200" show-word-limit />
          </el-form-item>
          <el-form-item label="精选案例">
            <ContentPicker v-model="form.cases.contentIds" content-type="CASE" :max="6" />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- Tab 5: 合作客户标识 -->
      <el-tab-pane label="5. 客户标识墙 (Customers)" name="customers">
        <div class="tab-toolbar">
          <el-switch v-model="form.customers.enabled" active-text="在前台启用该模块" />
        </div>
        <el-form label-width="110px">
          <el-form-item label="模块标题">
            <el-input v-model="form.customers.title" maxlength="40" show-word-limit />
          </el-form-item>
          <el-form-item label="模块副标题">
            <el-input v-model="form.customers.subtitle" maxlength="200" show-word-limit />
          </el-form-item>
          <el-form-item label="选择客户标识">
            <ContentPicker v-model="form.customers.contentIds" content-type="CUSTOMER" :max="30" />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- Tab 6: 新闻动态 -->
      <el-tab-pane label="6. 新闻动态 (News)" name="news">
        <div class="tab-toolbar">
          <el-switch v-model="form.news.enabled" active-text="在前台启用该模块" />
        </div>
        <el-form label-width="110px">
          <el-form-item label="模块标题">
            <el-input v-model="form.news.title" maxlength="40" show-word-limit />
          </el-form-item>
          <el-form-item label="展示模式">
            <el-radio-group v-model="form.news.mode">
              <el-radio value="LATEST">按最新发布日期自动取前 N 条 (LATEST)</el-radio>
              <el-radio value="SELECTED">人工手选指定新闻 (SELECTED)</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="展示条数">
            <el-input-number v-model="form.news.limit" :min="1" :max="6" />
          </el-form-item>
          <el-form-item v-if="form.news.mode === 'SELECTED'" label="手选新闻">
            <ContentPicker v-model="form.news.contentIds" content-type="NEWS" :max="6" />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- Tab 7: 底部咨询区 -->
      <el-tab-pane label="7. 底部咨询区 (CTA)" name="consultation">
        <el-form label-width="110px">
          <el-form-item label="主标题" required>
            <el-input v-model="form.consultation.title" maxlength="40" show-word-limit />
          </el-form-item>
          <el-form-item label="副标题说明">
            <el-input v-model="form.consultation.subtitle" type="textarea" :rows="2" maxlength="200" show-word-limit />
          </el-form-item>
          <el-form-item label="按钮文字" required>
            <el-input v-model="form.consultation.buttonText" maxlength="12" style="width: 220px" />
          </el-form-item>
          <el-form-item label="按钮动作">
            <LinkEditor v-model="form.consultation.link" :allow-consult="true" :allow-none="false" />
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <MediaPicker v-model="pickerOpen" :kind="pickerKind" @select="onMediaPicked" />

    <template #tips>
      <p>首页模块摆放顺序与各区块内容均已与「实景交互预演工作台 (/preview/home)」双向实时打通。</p>
      <p>关联的产品、方案、案例、新闻与客户标识自动读取目标内容的最新线上发布版本，无需在首页重复录入标题和封面。</p>
    </template>
  </ConfigPanel>
</template>

<style scoped>
.studio-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  margin-bottom: 18px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f5f3ff 0%, #ede9fe 100%);
  border: 1px solid #c4b5fd;
  flex-wrap: wrap;
}

.studio-banner__title {
  font-size: 14.5px;
  color: #3730a3;
}

.studio-banner__desc {
  margin: 4px 0 0;
  font-size: 12.5px;
  color: #4f46e5;
}

.err {
  margin-bottom: 12px;
}

.tab-hint {
  font-size: 12.5px;
  color: var(--a-text-muted);
  margin: 0 0 14px;
}

.tab-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 18px;
  border: 1px solid var(--a-border);
  border-radius: 10px;
  background: #fafbfe;
}

.order-item__num {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: #eeebff;
  color: #5b4be8;
  font-weight: 700;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.order-item__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.order-item__info strong {
  font-size: 14px;
}

.order-item__info span {
  font-size: 12px;
  color: var(--a-text-muted);
}

.order-item__btns {
  display: flex;
  align-items: center;
  gap: 8px;
}

.block-card {
  border: 1px solid var(--a-border);
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 14px;
  background: #fafbfe;
}

.block-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.media-pick-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.muted {
  font-size: 12px;
  color: var(--a-text-muted);
}

.metric-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}
</style>
```

### `website/admin/src/views/NavigationView.vue`

```vue
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import ConfigPanel from '@/components/ConfigPanel.vue'
import LinkEditor, { type LinkValue } from '@/components/LinkEditor.vue'
import { useConfig } from '@/composables/useConfig'

interface NavNode {
  key?: string
  label: string
  enabled: boolean
  sortOrder: number
  link: LinkValue | null
  children?: NavNode[]
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

interface NavigationDraft {
  items: NavNode[]
  megaColumns?: MegaColumn[]
  megaCta?: { text: string; linkText: string }
}

const activeTab = ref<'main' | 'mega'>('main')

const { view, loading, saving, publishing, loadError, load, save, publish } =
  useConfig<NavigationDraft & Record<string, unknown>>('NAVIGATION')

const form = reactive<{
  items: NavNode[]
  megaColumns: MegaColumn[]
  megaCta: { text: string; linkText: string }
}>({
  items: [],
  megaColumns: [],
  megaCta: { text: '需要专业场景建议？', linkText: '联系技术专家' },
})

const DEFAULT_MEGA_COLUMNS: MegaColumn[] = [
  {
    title: 'AI产品',
    items: [{ label: '紫宝盒', path: '/products/1', tag: '首款', isVideo: false }],
  },
  {
    title: 'IoT服务',
    items: [
      { label: '安全管家', path: '/products?category=安全', isVideo: false },
      { label: '冷链管家', path: '/products?category=冷链', isVideo: false },
      { label: '流向管家', path: '/products?category=流向', isVideo: false },
      { label: '追货管家', path: '/products?category=追货', isVideo: false },
    ],
  },
  {
    title: '交易服务',
    items: [
      { label: '财运通', path: '/solutions?category=交易', isVideo: false },
      { label: '数字能源', path: '/solutions?category=能源', isVideo: false },
    ],
  },
  {
    title: '解决方案',
    items: [
      { label: '大宗物流数字化', path: '/solutions/1', isVideo: false },
      { label: '连锁零售数字化', path: '/solutions/2', isVideo: false },
      { label: '饲料防偷盗 AI 治理', path: '/solutions/3', isVideo: false },
    ],
  },
  {
    title: '产品演示',
    items: [
      {
        label: '产品操作视频',
        path: '',
        isVideo: true,
        videoTitle: '紫宝盒 | 物流行业首款全场景AI工具产品演示',
        videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
      },
    ],
  },
]

function normalize(node: Partial<NavNode>): NavNode {
  return {
    key: node.key,
    label: node.label ?? '',
    enabled: node.enabled ?? true,
    sortOrder: node.sortOrder ?? 100,
    link: (node.link as LinkValue | null) ?? { type: 'NONE' },
    children: (node.children ?? []).map((child) => ({
      key: child.key,
      label: child.label ?? '',
      enabled: child.enabled ?? true,
      sortOrder: child.sortOrder ?? 100,
      link: (child.link as LinkValue | null) ?? { type: 'PAGE', pageCode: 'HOME' },
    })),
  }
}

function fill() {
  const draft = view.value?.draft
  form.items = (draft?.items ?? []).map((item) => normalize(item))
  if (form.items.length === 0) {
    form.items = [
      normalize({ label: '首页', link: { type: 'PAGE', pageCode: 'HOME' }, sortOrder: 10 }),
      normalize({ label: '产品与方案', link: { type: 'PAGE', pageCode: 'PRODUCTS' }, sortOrder: 20 }),
      normalize({ label: '客户案例', link: { type: 'PAGE', pageCode: 'CASES' }, sortOrder: 30 }),
      normalize({ label: '新闻动态', link: { type: 'PAGE', pageCode: 'NEWS' }, sortOrder: 40 }),
      normalize({ label: '关于我们', link: { type: 'PAGE', pageCode: 'ABOUT' }, sortOrder: 50 }),
    ]
  }
  form.megaColumns = (draft?.megaColumns ?? []).map((col) => ({
    title: col.title ?? '',
    items: (col.items ?? []).map((sub) => ({
      label: sub.label ?? '',
      path: sub.path ?? '',
      tag: sub.tag ?? '',
      isVideo: Boolean(sub.isVideo),
      videoUrl: sub.videoUrl ?? '',
      videoTitle: sub.videoTitle ?? '',
    })),
  }))
  form.megaCta = {
    text: draft?.megaCta?.text ?? '需要专业场景建议？',
    linkText: draft?.megaCta?.linkText ?? '联系技术专家',
  }
}

function applyDefaultMega() {
  form.megaColumns = JSON.parse(JSON.stringify(DEFAULT_MEGA_COLUMNS))
  form.megaCta = { text: '需要专业场景建议？', linkText: '联系技术专家' }
  ElMessage.success('已载入 5 列业务全景菜单预设，保存草稿后生效')
}

function addTop() {
  if (form.items.length >= 8) {
    ElMessage.warning('顶级导航最多 8 项')
    return
  }
  form.items.push(
    normalize({ label: '', link: { type: 'PAGE', pageCode: 'HOME' }, sortOrder: (form.items.length + 1) * 10 }),
  )
}

function addChild(item: NavNode) {
  item.children = item.children ?? []
  if (item.children.length >= 10) {
    ElMessage.warning('子菜单最多 10 项')
    return
  }
  item.children.push(
    normalize({ label: '', link: { type: 'PAGE', pageCode: 'HOME' }, sortOrder: (item.children.length + 1) * 10 }),
  )
}

function move<T extends { sortOrder: number }>(list: T[], index: number, delta: number) {
  const target = index + delta
  if (target < 0 || target >= list.length) return
  const current = list[index].sortOrder
  list[index].sortOrder = list[target].sortOrder
  list[target].sortOrder = current
  const [item] = list.splice(index, 1)
  list.splice(target, 0, item)
}

function addMegaCol() {
  if (form.megaColumns.length >= 8) {
    ElMessage.warning('全景大菜单最多 8 列')
    return
  }
  form.megaColumns.push({
    title: '新业务列',
    items: [{ label: '示例产品', path: '/products', tag: '', isVideo: false }],
  })
}

function addMegaItem(col: MegaColumn) {
  if (col.items.length >= 15) {
    ElMessage.warning('每列最多 15 个项目')
    return
  }
  col.items.push({ label: '', path: '/products', tag: '', isVideo: false })
}

function validateBeforeSave(): boolean {
  for (let i = 0; i < form.items.length; i++) {
    const item = form.items[i]
    if (!item.label.trim()) {
      ElMessage.warning(`第 ${i + 1} 个顶级导航名称不能为空`)
      return false
    }
    for (let j = 0; j < (item.children?.length ?? 0); j++) {
      const child = item.children![j]
      if (!child.label.trim()) {
        ElMessage.warning(`导航「${item.label}」的第 ${j + 1} 个子项名称不能为空`)
        return false
      }
      if (!child.link || child.link.type === 'NONE') {
        ElMessage.warning(`子菜单「${child.label}」必须配置有效跳转链接`)
        return false
      }
    }
  }
  for (let c = 0; c < form.megaColumns.length; c++) {
    const col = form.megaColumns[c]
    if (!col.title.trim()) {
      ElMessage.warning(`全景菜单第 ${c + 1} 列标题不能为空`)
      return false
    }
    for (let k = 0; k < col.items.length; k++) {
      if (!col.items[k].label.trim()) {
        ElMessage.warning(`全景菜单「${col.title}」中第 ${k + 1} 项名称不能为空`)
        return false
      }
    }
  }
  return true
}

async function onSave() {
  if (!validateBeforeSave()) return
  const payload: NavigationDraft & Record<string, unknown> = {
    items: form.items,
    megaColumns: form.megaColumns,
    megaCta: form.megaCta,
  }
  if (await save(payload)) fill()
}

async function onPublish() {
  if (await publish()) fill()
}

onMounted(async () => {
  await load()
  fill()
})
</script>

<template>
  <ConfigPanel
    title="导航与全景大菜单管理"
    :status="view?.status"
    :draft-version="view?.draftVersion"
    :published-version="view?.publishedVersion"
    :has-unpublished-changes="view?.hasUnpublishedChanges"
    :loading="loading"
    :saving="saving"
    :publishing="publishing"
    @save="onSave"
    @publish="onPublish"
  >
    <el-alert v-if="loadError" type="error" :closable="false" :title="loadError" class="err" />

    <el-tabs v-model="activeTab">
      <!-- Tab 1: 常规两级导航树 -->
      <el-tab-pane label="1. 顶部主导航菜单 (items)" name="main">
        <el-alert
          type="info"
          :closable="false"
          class="err"
          title="最多两级，顶层最多 8 项、每项最多 10 个子项。有子项的父节点可以设为「无链接」；叶子节点必须有有效链接才能发布。"
        />

        <div v-for="(item, index) in form.items" :key="item.key ?? `top-${index}`" class="node">
          <div class="node__head">
            <el-switch v-model="item.enabled" active-text="显示" />
            <el-input v-model="item.label" maxlength="20" placeholder="导航名称" style="width: 160px" />
            <span class="node__order">排序 {{ item.sortOrder }}</span>
            <div class="node__actions">
              <el-button link :disabled="index === 0" @click="move(form.items, index, -1)">上移</el-button>
              <el-button link :disabled="index === form.items.length - 1" @click="move(form.items, index, 1)">下移</el-button>
              <el-button link @click="addChild(item)">添加子项</el-button>
              <el-button link type="danger" @click="form.items.splice(index, 1)">删除</el-button>
            </div>
          </div>

          <div class="node__link">
            <span class="label">链接</span>
            <LinkEditor v-model="item.link" :allow-none="true" :allow-consult="true" />
            <span v-if="item.children?.length" class="hint">已有子项时，这里可设为「无链接」，前台作为展开父节点</span>
          </div>

          <div v-if="item.children?.length" class="children">
            <div v-for="(child, childIndex) in item.children" :key="child.key ?? `child-${index}-${childIndex}`" class="child">
              <el-switch v-model="child.enabled" />
              <el-input v-model="child.label" maxlength="20" placeholder="子项名称" style="width: 150px" />
              <LinkEditor v-model="child.link" :allow-none="false" :allow-consult="true" />
              <el-button link :disabled="childIndex === 0" @click="move(item.children!, childIndex, -1)">上移</el-button>
              <el-button link :disabled="childIndex === (item.children?.length ?? 0) - 1" @click="move(item.children!, childIndex, 1)">下移</el-button>
              <el-button link type="danger" @click="item.children!.splice(childIndex, 1)">删除</el-button>
            </div>
          </div>
        </div>

        <el-button class="add" @click="addTop">+ 添加顶级导航项</el-button>
      </el-tab-pane>

      <!-- Tab 2: 5列业务全景下拉大菜单 (megaColumns + megaCta) -->
      <el-tab-pane label="2. 五列业务全景下拉菜单 (megaColumns)" name="mega">
        <div class="mega-toolbar">
          <p class="hint">
            当前台导航项名称包含「产品」或「方案」且展开时，将自动渲染此五列业务全景大菜单（支持角标与视频弹窗演示）。
          </p>
          <el-button size="small" type="primary" plain @click="applyDefaultMega">
            ✨ 一键载入默认五列全景模板
          </el-button>
        </div>

        <div v-for="(col, cIdx) in form.megaColumns" :key="`mega-col-${cIdx}`" class="node">
          <div class="node__head">
            <strong>第 {{ cIdx + 1 }} 列</strong>
            <el-input v-model="col.title" maxlength="30" placeholder="列标题（如：AI产品 / IoT服务）" style="width: 220px" />
            <div class="node__actions">
              <el-button link @click="addMegaItem(col)">+ 添加子项</el-button>
              <el-button link type="danger" @click="form.megaColumns.splice(cIdx, 1)">删除列</el-button>
            </div>
          </div>

          <div class="children">
            <div v-for="(sub, sIdx) in col.items" :key="`mega-sub-${cIdx}-${sIdx}`" class="child">
              <el-input v-model="sub.label" maxlength="40" placeholder="项目名称" style="width: 150px" />
              <el-input v-model="sub.tag" maxlength="10" placeholder="角标(如:首款)" style="width: 110px" />
              <el-checkbox v-model="sub.isVideo">视频演示弹窗</el-checkbox>
              <template v-if="sub.isVideo">
                <el-input v-model="sub.videoTitle" maxlength="100" placeholder="视频弹窗标题" style="width: 190px" />
                <el-input v-model="sub.videoUrl" maxlength="500" placeholder="视频 MP4 地址" style="width: 220px" />
              </template>
              <template v-else>
                <el-input v-model="sub.path" maxlength="255" placeholder="站内路径（如 /products/1）" style="width: 240px" />
              </template>
              <el-button link type="danger" @click="col.items.splice(sIdx, 1)">删除</el-button>
            </div>
          </div>
        </div>

        <el-button class="add" @click="addMegaCol">+ 添加全景菜单列</el-button>

        <el-divider content-position="left">全景菜单底部引导条 (megaCta)</el-divider>
        <el-form label-width="110px">
          <el-form-item label="左侧提示文案">
            <el-input v-model="form.megaCta.text" maxlength="100" style="width: 320px" />
          </el-form-item>
          <el-form-item label="右侧按钮文字">
            <el-input v-model="form.megaCta.linkText" maxlength="50" style="width: 220px" />
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <template #tips>
      <p>页脚链接在「全站设置」的页脚分组里维护，不在导航中重复配置。</p>
      <p>目标内容下线后，前台会自动隐藏该导航项；父节点没有有效子项时整块隐藏。</p>
    </template>
  </ConfigPanel>
</template>

<style scoped>
.err {
  margin-bottom: 12px;
}

.mega-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.node {
  border: 1px solid var(--a-border);
  border-radius: 8px;
  padding: 14px;
  margin-bottom: 12px;
  background: #fbfcfe;
}

.node__head {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.node__order {
  font-size: 12px;
  color: var(--a-text-muted);
}

.node__actions {
  margin-left: auto;
}

.node__link {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.label {
  font-size: 13px;
  color: var(--a-text-muted);
  width: 40px;
}

.hint {
  font-size: 12px;
  color: var(--a-text-muted);
  margin: 0;
}

.children {
  margin-top: 12px;
  padding-left: 14px;
  border-left: 2px solid var(--a-border);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.child {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.add {
  margin-top: 4px;
}
</style>
```

### `website/admin/src/views/SiteSettingsView.vue`

```vue
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import ConfigPanel from '@/components/ConfigPanel.vue'
import LinkEditor, { type LinkValue } from '@/components/LinkEditor.vue'
import MediaPicker from '@/components/MediaPicker.vue'
import { useConfig } from '@/composables/useConfig'
import type { MediaItem } from '@/api/content'

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

interface QrCodeConfig {
  label: string
  mediaId?: string
  imageUrl?: string
}

interface SiteDraft {
  siteName: string
  slogan: string
  copyrightText: string
  icpText: string
  icpUrl: string
  policeText: string
  policeUrl: string
  logoMediaId?: number | null
  logoAlt: string
  faviconMediaId?: number | null
  defaultSeo: { title: string; description: string }
  footerGroups: FooterGroup[]
  qrCodes?: QrCodeConfig[]
}

const { view, loading, saving, publishing, loadError, load, save, publish } =
  useConfig<SiteDraft & Record<string, unknown>>('SITE')

interface SiteForm {
  siteName: string
  slogan: string
  copyrightText: string
  icpText: string
  icpUrl: string
  policeText: string
  policeUrl: string
  logoMediaId: string
  logoAlt: string
  faviconMediaId: string
  seoTitle: string
  seoDescription: string
  footerGroups: FooterGroup[]
  qrCodes: QrCodeConfig[]
}

const values = ref<SiteForm>({
  siteName: '',
  slogan: '',
  copyrightText: '',
  icpText: '',
  icpUrl: '',
  policeText: '',
  policeUrl: '',
  logoMediaId: '',
  logoAlt: '',
  faviconMediaId: '',
  seoTitle: '',
  seoDescription: '',
  footerGroups: [],
  qrCodes: [
    { label: '公众号', mediaId: '', imageUrl: '' },
    { label: '视频号', mediaId: '', imageUrl: '' },
  ],
})

const pickerOpen = ref(false)
const pickerTarget = ref<'logo' | 'favicon' | 'qr0' | 'qr1'>('logo')

function emptyLink(): FooterLink {
  return { label: '', enabled: true, sortOrder: 100, link: { type: 'PAGE', pageCode: 'CONTACT' } }
}

function fill() {
  const draft = view.value?.draft
  if (!draft) return
  values.value.siteName = draft.siteName ?? ''
  values.value.slogan = draft.slogan ?? ''
  values.value.copyrightText = draft.copyrightText ?? ''
  values.value.icpText = draft.icpText ?? ''
  values.value.icpUrl = draft.icpUrl ?? ''
  values.value.policeText = draft.policeText ?? ''
  values.value.policeUrl = draft.policeUrl ?? ''
  values.value.logoMediaId = draft.logoMediaId ? String(draft.logoMediaId) : ''
  values.value.logoAlt = draft.logoAlt ?? ''
  values.value.faviconMediaId = draft.faviconMediaId ? String(draft.faviconMediaId) : ''
  values.value.seoTitle = draft.defaultSeo?.title ?? ''
  values.value.seoDescription = draft.defaultSeo?.description ?? ''
  values.value.footerGroups = (draft.footerGroups ?? []).map((group) => ({
    title: group.title ?? '',
    links: (group.links ?? []).map((link) => ({
      key: link.key,
      label: link.label ?? '',
      enabled: link.enabled ?? true,
      sortOrder: link.sortOrder ?? 100,
      link: (link.link as LinkValue | null) ?? { type: 'PAGE', pageCode: 'CONTACT' },
    })),
  }))
  if (values.value.footerGroups.length === 0) {
    values.value.footerGroups = [{ title: '快速入口', links: [emptyLink()] }]
  }
  if (Array.isArray(draft.qrCodes) && draft.qrCodes.length > 0) {
    values.value.qrCodes = draft.qrCodes.map((q) => ({
      label: q.label ?? '公众号',
      mediaId: q.mediaId ? String(q.mediaId) : '',
      imageUrl: q.imageUrl ?? (q.mediaId ? `/api/public/media/${q.mediaId}` : ''),
    }))
  }
}

function buildDraft(): SiteDraft & Record<string, unknown> {
  const base = { ...(view.value?.draft ?? {}) } as Record<string, unknown>
  delete base.logoMediaId
  delete base.faviconMediaId
  return {
    ...base,
    siteName: values.value.siteName.trim(),
    slogan: values.value.slogan.trim(),
    copyrightText: values.value.copyrightText.trim(),
    icpText: values.value.icpText.trim(),
    icpUrl: values.value.icpUrl.trim(),
    policeText: values.value.policeText.trim(),
    policeUrl: values.value.policeUrl.trim(),
    logoMediaId: values.value.logoMediaId ? Number(values.value.logoMediaId) : null,
    logoAlt: values.value.logoAlt.trim(),
    faviconMediaId: values.value.faviconMediaId ? Number(values.value.faviconMediaId) : null,
    defaultSeo: {
      title: values.value.seoTitle.trim(),
      description: values.value.seoDescription.trim(),
    },
    footerGroups: values.value.footerGroups.map((group) => ({
      title: group.title.trim(),
      links: group.links.map((link, idx) => ({
        ...link,
        label: link.label.trim(),
        sortOrder: (idx + 1) * 10,
      })),
    })),
    qrCodes: values.value.qrCodes.map((q) => ({
      label: q.label.trim() || '公众号',
      mediaId: q.mediaId || undefined,
      imageUrl: q.mediaId ? `/api/public/media/${q.mediaId}` : q.imageUrl || undefined,
    })),
  } as SiteDraft & Record<string, unknown>
}

function validateBeforeSave(): boolean {
  if (!values.value.siteName.trim()) {
    ElMessage.warning('站点名称不能为空')
    return false
  }
  const hasIcpText = Boolean(values.value.icpText.trim())
  const hasIcpUrl = Boolean(values.value.icpUrl.trim())
  if (hasIcpText !== hasIcpUrl) {
    ElMessage.warning('ICP 备案号与备案链接需成对填写（或同时留空）')
    return false
  }
  const hasPoliceText = Boolean(values.value.policeText.trim())
  const hasPoliceUrl = Boolean(values.value.policeUrl.trim())
  if (hasPoliceText !== hasPoliceUrl) {
    ElMessage.warning('公安备案号与备案链接需成对填写（或同时留空）')
    return false
  }
  for (let i = 0; i < values.value.footerGroups.length; i++) {
    const g = values.value.footerGroups[i]
    if (!g.title.trim()) {
      ElMessage.warning(`页脚第 ${i + 1} 组标题不能为空`)
      return false
    }
    for (let j = 0; j < g.links.length; j++) {
      if (!g.links[j].label.trim()) {
        ElMessage.warning(`页脚分组「${g.title}」第 ${j + 1} 个链接名称不能为空`)
        return false
      }
    }
  }
  return true
}

async function onSave() {
  if (!validateBeforeSave()) return
  if (await save(buildDraft())) fill()
}

async function onPublish() {
  if (!validateBeforeSave()) return
  if (await publish()) fill()
}

function addGroup() {
  if (values.value.footerGroups.length >= 4) {
    ElMessage.warning('页脚分组最多 4 组')
    return
  }
  values.value.footerGroups.push({ title: '', links: [emptyLink()] })
}

function moveGroup(idx: number, delta: number) {
  const target = idx + delta
  if (target < 0 || target >= values.value.footerGroups.length) return
  const [item] = values.value.footerGroups.splice(idx, 1)
  values.value.footerGroups.splice(target, 0, item)
}

function addLink(group: FooterGroup) {
  if (group.links.length >= 10) {
    ElMessage.warning('每组最多 10 个链接')
    return
  }
  group.links.push({ ...emptyLink(), sortOrder: (group.links.length + 1) * 10 })
}

function moveLink(group: FooterGroup, idx: number, delta: number) {
  const target = idx + delta
  if (target < 0 || target >= group.links.length) return
  const [item] = group.links.splice(idx, 1)
  group.links.splice(target, 0, item)
}

function openPicker(target: 'logo' | 'favicon' | 'qr0' | 'qr1') {
  pickerTarget.value = target
  pickerOpen.value = true
}

function onPick(media: MediaItem) {
  if (pickerTarget.value === 'logo') {
    values.value.logoMediaId = String(media.id)
  } else if (pickerTarget.value === 'favicon') {
    values.value.faviconMediaId = String(media.id)
  } else if (pickerTarget.value === 'qr0') {
    values.value.qrCodes[0].mediaId = String(media.id)
    values.value.qrCodes[0].imageUrl = `/api/public/media/${media.id}`
  } else if (pickerTarget.value === 'qr1') {
    values.value.qrCodes[1].mediaId = String(media.id)
    values.value.qrCodes[1].imageUrl = `/api/public/media/${media.id}`
  }
  pickerOpen.value = false
}

onMounted(async () => {
  await load()
  fill()
})
</script>

<template>
  <ConfigPanel
    title="全站设置"
    :status="view?.status"
    :draft-version="view?.draftVersion"
    :published-version="view?.publishedVersion"
    :has-unpublished-changes="view?.hasUnpublishedChanges"
    :loading="loading"
    :saving="saving"
    :publishing="publishing"
    @save="onSave"
    @publish="onPublish"
  >
    <el-alert v-if="loadError" type="error" :closable="false" :title="loadError" class="err" />

    <el-form label-width="110px">
      <el-form-item label="站点名称" required>
        <el-input v-model="values.siteName" maxlength="60" show-word-limit />
      </el-form-item>
      <el-form-item label="标语">
        <el-input v-model="values.slogan" maxlength="100" show-word-limit placeholder="显示在页脚，一句话说明业务" />
      </el-form-item>
      <el-form-item label="Logo">
        <div class="media">
          <img v-if="values.logoMediaId" :src="`/api/admin/media/${values.logoMediaId}/file`" alt="站点 Logo" />
          <span v-else class="media__empty">未选择</span>
          <el-button size="small" @click="openPicker('logo')">选择图片</el-button>
          <el-button v-if="values.logoMediaId" size="small" link type="danger" @click="values.logoMediaId = ''">清除</el-button>
        </div>
      </el-form-item>
      <el-form-item label="Logo 替代文字">
        <el-input v-model="values.logoAlt" maxlength="120" placeholder="不填时使用站点名称" />
      </el-form-item>
      <el-form-item label="站点图标">
        <div class="media">
          <img v-if="values.faviconMediaId" :src="`/api/admin/media/${values.faviconMediaId}/file`" alt="站点图标" />
          <span v-else class="media__empty">可选，建议 PNG 小于 200KB</span>
          <el-button size="small" @click="openPicker('favicon')">选择图片</el-button>
          <el-button v-if="values.faviconMediaId" size="small" link type="danger" @click="values.faviconMediaId = ''">清除</el-button>
        </div>
      </el-form-item>

      <el-divider content-position="left">默认 SEO 与搜索结果实时预览</el-divider>
      <p class="hint">页面没有单独 SEO 时的兜底值，发布时必填。</p>
      <el-form-item label="默认标题">
        <el-input v-model="values.seoTitle" maxlength="80" show-word-limit />
      </el-form-item>
      <el-form-item label="默认描述">
        <el-input v-model="values.seoDescription" type="textarea" :rows="2" maxlength="200" show-word-limit />
      </el-form-item>

      <!-- Google SERP & 社交分享卡片实时预览 -->
      <div class="serp-preview-wrap">
        <div class="serp-card">
          <span class="serp-card__badge">Google / 百度搜索结果预览 (SERP)</span>
          <p class="serp-card__url">https://www.company-domain.com › 首页</p>
          <h4 class="serp-card__title">
            {{ values.seoTitle || values.siteName || '企业官方网站' }} - {{ values.siteName || '品牌名称' }}
          </h4>
          <p class="serp-card__desc">
            {{ values.seoDescription || values.slogan || '请在上方填写默认 SEO 描述文案，建议控制在 80～160 字以内，提升自然搜索点击率。' }}
          </p>
        </div>
      </div>

      <el-divider content-position="left">页脚备案与关注二维码</el-divider>
      <el-form-item label="版权文案">
        <el-input v-model="values.copyrightText" maxlength="120" placeholder="© 2026 示例科技有限公司" />
      </el-form-item>
      <el-form-item label="ICP 备案">
        <el-input v-model="values.icpText" maxlength="80" placeholder="京ICP备00000000号" style="width: 260px" />
        <el-input v-model="values.icpUrl" maxlength="500" placeholder="https://beian.miit.gov.cn/" style="width: 300px; margin-left: 8px" />
      </el-form-item>
      <el-form-item label="公安备案">
        <el-input v-model="values.policeText" maxlength="80" placeholder="选填（需与链接成对填写）" style="width: 260px" />
        <el-input v-model="values.policeUrl" maxlength="500" placeholder="https://" style="width: 300px; margin-left: 8px" />
      </el-form-item>
      <el-form-item label="页脚双二维码">
        <div class="qr-config-row">
          <div v-for="(qr, qIdx) in values.qrCodes" :key="qIdx" class="qr-config-box">
            <img v-if="qr.mediaId" :src="`/api/admin/media/${qr.mediaId}/file`" :alt="qr.label" />
            <span v-else class="media__empty">默认矢量图</span>
            <el-input v-model="qr.label" maxlength="12" style="width: 96px" placeholder="标签" />
            <el-button size="small" @click="openPicker(qIdx === 0 ? 'qr0' : 'qr1')">选二维码</el-button>
            <el-button v-if="qr.mediaId" size="small" link type="danger" @click="qr.mediaId = ''; qr.imageUrl = ''">
              重置
            </el-button>
          </div>
        </div>
      </el-form-item>
    </el-form>

    <el-divider content-position="left">页脚分组（支持上下排序）</el-divider>
    <p class="hint">最多 4 组，每组最多 10 个链接。空组不会出现在前台。</p>

    <div v-for="(group, groupIndex) in values.footerGroups" :key="`group-${groupIndex}`" class="group">
      <div class="group__head">
        <el-input v-model="group.title" maxlength="20" placeholder="分组标题" style="width: 180px" />
        <el-button link :disabled="groupIndex === 0" @click="moveGroup(groupIndex, -1)">↑ 上移分组</el-button>
        <el-button link :disabled="groupIndex === values.footerGroups.length - 1" @click="moveGroup(groupIndex, 1)">
          ↓ 下移分组
        </el-button>
        <el-button link type="primary" @click="addLink(group)">+ 添加链接</el-button>
        <el-button link type="danger" @click="values.footerGroups.splice(groupIndex, 1)">删除分组</el-button>
      </div>
      <div v-for="(link, linkIndex) in group.links" :key="link.key ?? `link-${groupIndex}-${linkIndex}`" class="group__row">
        <el-switch v-model="link.enabled" />
        <el-input v-model="link.label" maxlength="20" placeholder="链接名称" style="width: 140px" />
        <LinkEditor v-model="link.link" :allow-none="false" :allow-consult="true" />
        <el-button link :disabled="linkIndex === 0" @click="moveLink(group, linkIndex, -1)">上移</el-button>
        <el-button link :disabled="linkIndex === group.links.length - 1" @click="moveLink(group, linkIndex, 1)">下移</el-button>
        <el-button link type="danger" @click="group.links.splice(linkIndex, 1)">删除</el-button>
      </div>
    </div>
    <el-button class="add" @click="addGroup">+ 添加页脚分组</el-button>

    <MediaPicker v-model="pickerOpen" :selected-id="null" @select="onPick" />

    <template #tips>
      <p>电话、邮箱、地址在「联系我们」页维护，这里不重复填写。</p>
      <p>备案号与备案链接需成对填写，只填一个会在保存或发布时被拦截提示。</p>
    </template>
  </ConfigPanel>
</template>

<style scoped>
.err {
  margin-bottom: 12px;
}

.hint {
  font-size: 12px;
  color: var(--a-text-muted);
  margin: 0 0 12px 110px;
}

.media {
  display: flex;
  align-items: center;
  gap: 10px;
}

.media img,
.media__empty {
  width: 72px;
  height: 48px;
  border: 1px solid var(--a-border);
  border-radius: 6px;
  object-fit: contain;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: var(--a-text-muted);
  background: #f6f8fa;
}

.serp-preview-wrap {
  margin: 0 0 20px 110px;
}

.serp-card {
  max-width: 620px;
  background: #ffffff;
  border: 1px solid var(--a-border);
  border-radius: 10px;
  padding: 14px 18px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
}

.serp-card__badge {
  display: inline-block;
  font-size: 11px;
  color: #5b4be8;
  background: #eeebff;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 600;
  margin-bottom: 6px;
}

.serp-card__url {
  margin: 0;
  font-size: 12px;
  color: #202124;
}

.serp-card__title {
  margin: 4px 0;
  font-size: 18px;
  font-weight: 500;
  color: #1a0dab;
}

.serp-card__desc {
  margin: 0;
  font-size: 13px;
  color: #4d5156;
  line-height: 1.55;
}

.qr-config-row {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.qr-config-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border: 1px solid var(--a-border);
  border-radius: 8px;
  background: #fafbfe;
}

.qr-config-box img {
  width: 48px;
  height: 48px;
  object-fit: contain;
  border-radius: 4px;
}

.group {
  border: 1px solid var(--a-border);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
}

.group__head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.group__row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding: 4px 0;
}

.add {
  margin-top: 4px;
}
</style>
```

### `website/admin/src/views/AboutPageView.vue`

```vue
<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import ConfigPanel from '@/components/ConfigPanel.vue'
import { useConfig } from '@/composables/useConfig'

interface Milestone {
  year: string
  title: string
  description: string
}

interface Qualification {
  name: string
  description: string
}

interface AboutDraft {
  intro: string
  mission: string
  milestones: Milestone[]
  qualifications: Qualification[]
}

const { view, loading, saving, publishing, loadError, load, save, publish } =
  useConfig<AboutDraft & Record<string, unknown>>('ABOUT')

const form = reactive<AboutDraft>({ intro: '', mission: '', milestones: [], qualifications: [] })

function fill() {
  const draft = view.value?.draft
  form.intro = draft?.intro ?? ''
  form.mission = draft?.mission ?? ''
  form.milestones = (draft?.milestones ?? []).map((item) => ({ ...item }))
  form.qualifications = (draft?.qualifications ?? []).map((item) => ({ ...item }))
}

function addMilestone() {
  if (form.milestones.length >= 20) {
    ElMessage.warning('发展历程最多 20 条')
    return
  }
  form.milestones.push({ year: String(new Date().getFullYear()), title: '', description: '' })
}

function addQualification() {
  if (form.qualifications.length >= 20) {
    ElMessage.warning('资质认证最多 20 条')
    return
  }
  form.qualifications.push({ name: '', description: '' })
}

function moveItem<T>(list: T[], index: number, delta: number) {
  const target = index + delta
  if (target < 0 || target >= list.length) return
  const [item] = list.splice(index, 1)
  list.splice(target, 0, item)
}

function validateBeforeSave(): boolean {
  for (let i = 0; i < form.milestones.length; i++) {
    const m = form.milestones[i]
    if (!/^\d{4}$/.test((m.year ?? '').trim())) {
      ElMessage.warning(`发展历程第 ${i + 1} 条年份需为 4 位数字（例如 2026）`)
      return false
    }
    if (!m.title.trim()) {
      ElMessage.warning(`发展历程第 ${i + 1} 条标题不能为空`)
      return false
    }
  }
  for (let i = 0; i < form.qualifications.length; i++) {
    if (!form.qualifications[i].name.trim()) {
      ElMessage.warning(`资质与认证第 ${i + 1} 条名称不能为空`)
      return false
    }
  }
  return true
}

async function onSave() {
  if (!validateBeforeSave()) return
  const draft = {
    ...(view.value?.draft ?? {}),
    intro: form.intro,
    mission: form.mission,
    milestones: form.milestones,
    qualifications: form.qualifications,
  } as AboutDraft & Record<string, unknown>
  if (await save(draft)) fill()
}

async function onPublish() {
  if (!validateBeforeSave()) return
  if (await publish()) fill()
}

onMounted(async () => {
  await load()
  fill()
})
</script>

<template>
  <ConfigPanel
    title="关于我们"
    :status="view?.status"
    :draft-version="view?.draftVersion"
    :published-version="view?.publishedVersion"
    :has-unpublished-changes="view?.hasUnpublishedChanges"
    :loading="loading"
    :saving="saving"
    :publishing="publishing"
    @save="onSave"
    @publish="onPublish"
  >
    <el-alert v-if="loadError" type="error" :closable="false" :title="loadError" class="err" />

    <el-form label-width="100px">
      <el-form-item label="公司简介">
        <el-input v-model="form.intro" type="textarea" :rows="5" maxlength="2000" show-word-limit />
      </el-form-item>
      <el-form-item label="企业使命">
        <el-input v-model="form.mission" type="textarea" :rows="3" maxlength="2000" show-word-limit />
      </el-form-item>
    </el-form>

    <el-divider content-position="left">发展历程（里程碑时间轴）</el-divider>
    <div v-for="(item, index) in form.milestones" :key="`ms-${index}`" class="row">
      <el-input v-model="item.year" maxlength="4" placeholder="年份(如2026)" style="width: 110px" />
      <el-input v-model="item.title" maxlength="50" placeholder="里程碑事件标题" style="width: 220px" />
      <el-input v-model="item.description" maxlength="300" placeholder="详细说明（选填）" style="flex: 1" />
      <el-button link :disabled="index === 0" @click="moveItem(form.milestones, index, -1)">↑ 上移</el-button>
      <el-button link :disabled="index === form.milestones.length - 1" @click="moveItem(form.milestones, index, 1)">
        ↓ 下移
      </el-button>
      <el-button link type="danger" @click="form.milestones.splice(index, 1)">删除</el-button>
    </div>
    <el-button @click="addMilestone">+ 添加里程碑</el-button>

    <el-divider content-position="left">资质与荣誉认证</el-divider>
    <div v-for="(item, index) in form.qualifications" :key="`ql-${index}`" class="row">
      <el-input v-model="item.name" maxlength="60" placeholder="资质/荣誉名称" style="width: 260px" />
      <el-input v-model="item.description" maxlength="300" placeholder="认证机构或说明" style="flex: 1" />
      <el-button link :disabled="index === 0" @click="moveItem(form.qualifications, index, -1)">↑ 上移</el-button>
      <el-button link :disabled="index === form.qualifications.length - 1" @click="moveItem(form.qualifications, index, 1)">
        ↓ 下移
      </el-button>
      <el-button link type="danger" @click="form.qualifications.splice(index, 1)">删除</el-button>
    </div>
    <el-button @click="addQualification">+ 添加资质认证</el-button>

    <template #tips>
      <p>发展历程年份需为 4 位数字（如 2024），前台「关于我们」页支持按年份筛选时间轴节点。</p>
    </template>
  </ConfigPanel>
</template>

<style scoped>
.err {
  margin-bottom: 12px;
}

.row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}
</style>
```

### `website/admin/src/views/ContactPageView.vue`

```vue
<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import ConfigPanel from '@/components/ConfigPanel.vue'
import { useConfig } from '@/composables/useConfig'

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

const { view, loading, saving, publishing, loadError, load, save, publish } =
  useConfig<ContactDraft & Record<string, unknown>>('CONTACT')

const form = reactive<ContactDraft>({
  title: '联系我们',
  description: '',
  successText: '',
  formEnabled: false,
  phone: '',
  email: '',
  address: '',
  hours: '',
})

function fill() {
  const draft = view.value?.draft
  form.title = draft?.title || '联系我们'
  form.description = draft?.description ?? ''
  form.successText = draft?.successText ?? ''
  form.formEnabled = draft?.formEnabled ?? false
  form.phone = draft?.phone ?? ''
  form.email = draft?.email ?? ''
  form.address = draft?.address ?? ''
  form.hours = draft?.hours ?? ''
}

function validate(): boolean {
  if (!form.phone.trim() && !form.email.trim()) {
    ElMessage.warning('联系电话与邮箱请至少填写一项')
    return false
  }
  if (form.email.trim() && !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(form.email.trim())) {
    ElMessage.warning('邮箱格式不正确')
    return false
  }
  return true
}

async function onSave() {
  if (!validate()) return
  const draft = {
    ...(view.value?.draft ?? {}),
    title: form.title.trim() || '联系我们',
    description: form.description.trim(),
    successText: form.successText.trim(),
    formEnabled: form.formEnabled,
    phone: form.phone.trim(),
    email: form.email.trim(),
    address: form.address.trim(),
    hours: form.hours.trim(),
  } as ContactDraft & Record<string, unknown>
  if (await save(draft)) fill()
}

async function onPublish() {
  if (!validate()) return
  if (await publish()) fill()
}

onMounted(async () => {
  await load()
  fill()
})
</script>

<template>
  <ConfigPanel
    title="联系我们与全站咨询开关"
    :status="view?.status"
    :draft-version="view?.draftVersion"
    :published-version="view?.publishedVersion"
    :has-unpublished-changes="view?.hasUnpublishedChanges"
    :loading="loading"
    :saving="saving"
    :publishing="publishing"
    @save="onSave"
    @publish="onPublish"
  >
    <el-alert v-if="loadError" type="error" :closable="false" :title="loadError" class="err" />

    <el-form label-width="120px">
      <el-form-item label="页面标题">
        <el-input v-model="form.title" maxlength="50" show-word-limit />
      </el-form-item>
      <el-form-item label="页面导语说明">
        <el-input v-model="form.description" type="textarea" :rows="2" maxlength="300" show-word-limit />
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="form.phone" maxlength="40" placeholder="例如：400-888-0000" style="width: 280px" />
      </el-form-item>
      <el-form-item label="联系邮箱">
        <el-input v-model="form.email" maxlength="100" placeholder="例如：connect@company.com" style="width: 280px" />
      </el-form-item>
      <el-form-item label="办公地址">
        <el-input v-model="form.address" maxlength="120" show-word-limit />
      </el-form-item>
      <el-form-item label="服务时间">
        <el-input v-model="form.hours" maxlength="60" placeholder="例如：周一至周日 08:30 - 20:30" style="width: 320px" />
      </el-form-item>

      <el-divider content-position="left">在线咨询表单控制</el-divider>
      <el-form-item label="开启在线咨询">
        <el-switch v-model="form.formEnabled" active-text="允许访客在线提交咨询线索" inactive-text="仅展示联系方式" />
      </el-form-item>
      <el-form-item label="提交成功回执语">
        <el-input
          v-model="form.successText"
          type="textarea"
          :rows="2"
          maxlength="200"
          show-word-limit
          placeholder="感谢您的信任！我们的行业专家将在 1 个工作日内与您联系。"
        />
      </el-form-item>
    </el-form>

    <template #tips>
      <p>此处维护的电话、邮箱、地址与服务时间将同步应用于官网「联系我们」页、全站页脚及右下角磁吸悬浮坞。</p>
      <p>开启在线咨询前，请确保「隐私说明」页面已有正式发布版本，访客提交的手机号将在服务端以 AES-256-GCM 加密存储。</p>
    </template>
  </ConfigPanel>
</template>

<style scoped>
.err {
  margin-bottom: 12px;
}
</style>
```

### `website/admin/src/views/AuditLogsView.vue`

```vue
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ApiError, get } from '@/api/http'

interface AuditRow {
  id: string
  actorName?: string
  action: string
  targetType?: string
  targetId?: string
  result: 'SUCCESS' | 'FAILURE'
  summary: string
  createdAt: string
}

const QUICK_ACTIONS = [
  { label: '全部操作', value: '' },
  { label: '登录认证', value: 'LOGIN' },
  { label: '配置发布', value: 'CONFIG_PUBLISH' },
  { label: '内容发布', value: 'CONTENT_PUBLISH' },
  { label: '内容下线', value: 'CONTENT_OFFLINE' },
  { label: '线索跟进', value: 'LEAD' },
  { label: '账号变更', value: 'USER' },
]

const loading = ref(false)
const rows = ref<AuditRow[]>([])
const total = ref(0)
const query = reactive({
  action: '',
  range: [] as string[],
  page: 1,
  pageSize: 20,
})

function formatTime(value: string | null) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function selectQuickAction(act: string) {
  query.action = act
  query.page = 1
  load()
}

function resetQuery() {
  query.action = ''
  query.range = []
  query.page = 1
  load()
}

function exportCurrentCsv() {
  if (!rows.value.length) {
    ElMessage.warning('当前页暂无日志可导出')
    return
  }
  const headers = ['ID', '时间', '操作人', '动作', '目标类型', '目标ID', '结果', '摘要']
  const csvRows = [
    headers.join(','),
    ...rows.value.map((r) =>
      [
        r.id,
        formatTime(r.createdAt),
        r.actorName || '-',
        r.action,
        r.targetType || '-',
        r.targetId || '-',
        r.result,
        `"${(r.summary || '').replace(/"/g, '""')}"`,
      ].join(','),
    ),
  ]
  const blob = new Blob(['\uFEFF' + csvRows.join('\n')], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `cms-audit-logs-p${query.page}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('已导出当前页审计日志 CSV')
}

async function load() {
  loading.value = true
  try {
    const params = new URLSearchParams({
      page: String(query.page),
      pageSize: String(query.pageSize),
    })
    if (query.action.trim()) params.set('action', query.action.trim())
    if (query.range.length === 2) {
      params.set('from', query.range[0])
      params.set('to', query.range[1])
    }
    const data = await get<{ items: AuditRow[]; total: number }>(
      `/api/admin/audit-logs?${params.toString()}`,
    )
    rows.value = data.items
    total.value = data.total
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '日志加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <div class="head">
        <div>
          <span class="head__title">操作审计日志</span>
          <span class="head__sub">记录全站登录、账号变更、发布下线与线索查阅痕迹（只读不可篡改）</span>
        </div>
        <div class="filters">
          <el-input
            v-model="query.action"
            placeholder="动作代码（如 LOGIN / PUBLISH）"
            clearable
            style="width: 210px"
            @keyup.enter="query.page = 1; load()"
          />
          <el-date-picker
            v-model="query.range"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 250px"
          />
          <el-button type="primary" @click="query.page = 1; load()">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button plain @click="exportCurrentCsv">导出当前页 CSV</el-button>
        </div>
      </div>
    </template>

    <div class="quick-bar">
      <button
        v-for="item in QUICK_ACTIONS"
        :key="item.label"
        type="button"
        class="quick-chip"
        :class="{ 'is-active': query.action === item.value }"
        @click="selectQuickAction(item.value)"
      >
        {{ item.label }}
      </button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column label="时间" width="175">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column prop="actorName" label="操作人" width="130">
        <template #default="{ row }">{{ row.actorName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="action" label="动作" width="170" />
      <el-table-column label="对象" width="160">
        <template #default="{ row }">
          {{ row.targetType ? `${row.targetType}${row.targetId ? '#' + row.targetId : ''}` : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="结果" width="95" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="row.result === 'SUCCESS' ? 'success' : 'danger'">
            {{ row.result }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="summary" label="摘要说明" min-width="240" />
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
</template>

<style scoped>
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.head__title {
  font-weight: 600;
  font-size: 16px;
}

.head__sub {
  margin-left: 10px;
  font-size: 12px;
  color: var(--a-text-muted);
}

.filters {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.quick-bar {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.quick-chip {
  padding: 5px 12px;
  border-radius: 999px;
  border: 1px solid var(--a-border);
  background: #fafbfe;
  font-size: 12px;
  color: var(--a-text-muted);
  cursor: pointer;
}

.quick-chip.is-active {
  background: #5b4be8;
  border-color: #5b4be8;
  color: #ffffff;
}

.pager {
  margin-top: 14px;
  justify-content: flex-end;
}
</style>
```

### `website/admin/src/views/LoginView.vue`

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ApiError } from '@/api/http'
import { useAuth } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const { login } = useAuth()

const form = ref({ username: '', password: '' })
const showPassword = ref(false)
const submitting = ref(false)
const errorText = ref('')

async function submit() {
  if (submitting.value) return
  errorText.value = ''
  if (!form.value.username.trim() || !form.value.password) {
    errorText.value = '请输入账号和密码'
    return
  }
  submitting.value = true
  try {
    await login(form.value.username.trim(), form.value.password)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard'
    await router.replace(redirect)
  } catch (error) {
    const apiError = error instanceof ApiError ? error : null
    errorText.value = apiError?.message ?? '登录失败，请稍后重试'
    if (apiError?.requestId) {
      ElMessage.error(`${errorText.value} (${apiError.requestId})`)
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="login">
    <div class="login__glow" aria-hidden="true"></div>
    <div class="panel">
      <div class="brand">
        <span class="logo">G</span>
        <div>
          <p class="brand-name">官网内容管理与可视化中台</p>
          <p class="brand-sub">Enterprise CMS & Visual Live Studio</p>
        </div>
      </div>

      <h1>欢迎登录管理后台</h1>

      <form class="form" @submit.prevent="submit">
        <label class="field">
          <span>管理员 / 编辑账号</span>
          <input v-model="form.username" autocomplete="username" placeholder="请输入登录账号" />
        </label>
        <label class="field">
          <span>安全密码</span>
          <div class="pwd-wrap">
            <input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              autocomplete="current-password"
              placeholder="请输入登录密码"
            />
            <button type="button" class="pwd-toggle" @click="showPassword = !showPassword">
              {{ showPassword ? '隐藏' : '显示' }}
            </button>
          </div>
        </label>

        <p v-if="errorText" class="error">{{ errorText }}</p>

        <button type="submit" class="submit" :disabled="submitting">
          {{ submitting ? '安全验证登录中...' : '立即登录控制台' }}
        </button>
      </form>

      <p class="hint">
        账号由系统管理员统一分配（ADMIN 管理员 / EDITOR 内容编辑）。连续输错密码将触发防暴力破解保护。
      </p>
    </div>
  </div>
</template>

<style scoped>
.login {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at 20% 20%, #1d1848 0%, #0b0d17 65%);
  padding: 24px;
  overflow: hidden;
}

.login__glow {
  position: absolute;
  width: 520px;
  height: 520px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(91, 75, 232, 0.32) 0%, transparent 70%);
  top: -120px;
  right: -80px;
  pointer-events: none;
}

.panel {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 410px;
  background: rgba(255, 255, 255, 0.98);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 18px;
  padding: 34px 32px;
  box-shadow: 0 28px 72px rgba(0, 0, 0, 0.45);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 26px;
}

.logo {
  width: 40px;
  height: 40px;
  border-radius: 11px;
  background: linear-gradient(135deg, #7c3aed 0%, #5b4be8 100%);
  color: #ffffff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 800;
  box-shadow: 0 8px 20px rgba(91, 75, 232, 0.35);
}

.brand-name {
  font-size: 15px;
  font-weight: 700;
  color: #101014;
  margin: 0;
}

.brand-sub {
  font-size: 12px;
  color: #6b7280;
  margin: 2px 0 0;
}

h1 {
  font-size: 20px;
  font-weight: 700;
  color: #101014;
  margin: 0 0 20px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.field input {
  height: 42px;
  width: 100%;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 0 14px;
  font-size: 14px;
  color: #111827;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.field input:focus {
  border-color: #5b4be8;
  box-shadow: 0 0 0 3px rgba(91, 75, 232, 0.14);
}

.pwd-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.pwd-toggle {
  position: absolute;
  right: 10px;
  border: none;
  background: none;
  font-size: 12px;
  color: #5b4be8;
  cursor: pointer;
}

.error {
  font-size: 13px;
  color: #dc2626;
  background: #fef2f2;
  padding: 8px 12px;
  border-radius: 8px;
  margin: 0;
}

.submit {
  height: 44px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #6366f1 0%, #5b4be8 100%);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 6px;
  box-shadow: 0 10px 24px rgba(91, 75, 232, 0.3);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.submit:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 14px 28px rgba(91, 75, 232, 0.42);
}

.submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.hint {
  margin-top: 22px;
  font-size: 12px;
  color: #6b7280;
  line-height: 1.6;
}
</style>
```

### `website/admin/src/views/ContentListView.vue`

```vue
<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ApiError } from '@/api/http'
import {
  cloneContent,
  deleteContent,
  listCategories,
  listContents,
  offlineContent,
  publishContent,
  type CategoryView,
  type ContentView,
} from '@/api/content'
import { useAuth } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const { current } = useAuth()
const isAdmin = computed(() => current.value?.role === 'ADMIN')

const contentType = computed(() => String(route.meta.contentType || 'PRODUCT'))
const slug = computed(() => String(route.meta.slug || 'products'))
const pageTitle = computed(() => String(route.meta.title || '内容管理'))
const hasCategory = computed(() => contentType.value !== 'CUSTOMER')

const loading = ref(false)
const batchRunning = ref(false)
const rows = ref<ContentView[]>([])
const total = ref(0)
const categories = ref<CategoryView[]>([])
const selectedRows = ref<ContentView[]>([])

const query = reactive({
  keyword: '',
  categoryId: '',
  publicationStatus: '',
  hasUnpublishedChanges: false,
  page: 1,
  pageSize: 20,
})

async function loadCategories() {
  if (!hasCategory.value) {
    categories.value = []
    return
  }
  try {
    const res = await listCategories(contentType.value)
    categories.value = res.items
  } catch {
    categories.value = []
  }
}

async function load() {
  loading.value = true
  try {
    const data = await listContents({
      contentType: contentType.value,
      keyword: query.keyword.trim() || undefined,
      categoryId: query.categoryId || undefined,
      publicationStatus: query.publicationStatus || undefined,
      hasUnpublishedChanges: query.hasUnpublishedChanges || undefined,
      page: query.page,
      pageSize: query.pageSize,
    })
    rows.value = data.items
    total.value = data.total
    selectedRows.value = []
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '内容列表加载失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.keyword = ''
  query.categoryId = ''
  query.publicationStatus = ''
  query.hasUnpublishedChanges = false
  query.page = 1
  load()
}

function onSelectionChange(val: ContentView[]) {
  selectedRows.value = val
}

function goCreate() {
  router.push(`/contents/${slug.value}/create`)
}

function goEdit(row: ContentView) {
  router.push(`/contents/${slug.value}/${row.id}/edit`)
}

function goPreview(row: ContentView) {
  router.push(`/preview/${slug.value}/${row.id}`)
}

async function handleClone(row: ContentView) {
  try {
    const created = await cloneContent(row.id)
    ElMessage.success(`已克隆生成新草稿：#${created.content.id}`)
    await load()
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '克隆失败')
  }
}

async function handlePublish(row: ContentView) {
  try {
    await ElMessageBox.confirm(`确认将「${row.title}」(v${row.draftVersion}) 正式发布到线上官网？`, '确认发布', {
      confirmButtonText: '立即发布',
      cancelButtonText: '取消',
    })
    await publishContent(row.id, row.lockVersion, row.draftVersion)
    ElMessage.success('发布成功')
    await load()
  } catch (error) {
    if (error instanceof ApiError) ElMessage.error(error.message)
  }
}

async function handleOffline(row: ContentView) {
  try {
    await ElMessageBox.confirm(`下线后「${row.title}」将从公开列表中移除，确认下线？`, '确认下线', {
      type: 'warning',
      confirmButtonText: '确认下线',
      cancelButtonText: '取消',
    })
    await offlineContent(row.id, row.lockVersion, row.draftVersion)
    ElMessage.success('已下线')
    await load()
  } catch (error) {
    if (error instanceof ApiError) ElMessage.error(error.message)
  }
}

async function handleDelete(row: ContentView) {
  try {
    await ElMessageBox.confirm(`删除后不可恢复（已被首页或其他内容引用的条目无法删除），确认删除「${row.title}」？`, '确认删除', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deleteContent(row.id, row.lockVersion)
    ElMessage.success('已删除')
    await load()
  } catch (error) {
    if (error instanceof ApiError) ElMessage.error(error.message)
  }
}

async function batchPublish() {
  if (!selectedRows.value.length) return
  try {
    await ElMessageBox.confirm(`确认批量发布已勾选的 ${selectedRows.value.length} 条内容？`, '批量发布', {
      confirmButtonText: '批量发布',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  batchRunning.value = true
  let ok = 0
  let fail = 0
  for (const row of selectedRows.value) {
    try {
      await publishContent(row.id, row.lockVersion, row.draftVersion)
      ok++
    } catch {
      fail++
    }
  }
  batchRunning.value = false
  ElMessage.success(`批量发布完成：成功 ${ok} 条${fail ? `，失败 ${fail} 条（请检查必填项）` : ''}`)
  await load()
}

async function batchOffline() {
  const pubRows = selectedRows.value.filter((r) => r.status === 'PUBLISHED')
  if (!pubRows.length) {
    ElMessage.warning('所选条目中没有处于「已发布」状态的内容')
    return
  }
  try {
    await ElMessageBox.confirm(`确认批量下线已勾选的 ${pubRows.length} 条已发布内容？`, '批量下线', {
      type: 'warning',
      confirmButtonText: '批量下线',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  batchRunning.value = true
  let ok = 0
  for (const row of pubRows) {
    try {
      await offlineContent(row.id, row.lockVersion, row.draftVersion)
      ok++
    } catch {
      // ignore single failure
    }
  }
  batchRunning.value = false
  ElMessage.success(`已批量下线 ${ok} 条内容`)
  await load()
}

function formatTime(value: string | null) {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  return d.toLocaleString('zh-CN', { hour12: false })
}

watch(
  () => route.path,
  () => {
    query.keyword = ''
    query.categoryId = ''
    query.publicationStatus = ''
    query.hasUnpublishedChanges = false
    query.page = 1
    loadCategories()
    load()
  },
)

onMounted(() => {
  loadCategories()
  load()
})
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <div class="head">
        <div>
          <span class="head__title">{{ pageTitle }}</span>
          <span class="head__sub">草稿修改与线上发布快照双版本隔离保护</span>
        </div>
        <div class="actions">
          <el-button @click="load">刷新</el-button>
          <el-button type="primary" @click="goCreate">+ 新建内容</el-button>
        </div>
      </div>
    </template>

    <!-- 筛选栏 -->
    <div class="filters">
      <el-input
        v-model="query.keyword"
        placeholder="按标题搜索..."
        clearable
        style="width: 220px"
        @keyup.enter="query.page = 1; load()"
      />
      <el-select
        v-if="hasCategory"
        v-model="query.categoryId"
        placeholder="全部分类"
        clearable
        style="width: 160px"
        @change="query.page = 1; load()"
      >
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-select
        v-model="query.publicationStatus"
        placeholder="全部状态"
        clearable
        style="width: 140px"
        @change="query.page = 1; load()"
      >
        <el-option label="草稿 (UNPUBLISHED)" value="UNPUBLISHED" />
        <el-option label="已发布 (PUBLISHED)" value="PUBLISHED" />
        <el-option label="已下线 (OFFLINE)" value="OFFLINE" />
      </el-select>
      <el-checkbox v-model="query.hasUnpublishedChanges" @change="query.page = 1; load()">
        仅看有未发布修改
      </el-checkbox>
      <el-button type="primary" plain @click="query.page = 1; load()">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <!-- 批量操作浮动条 -->
    <div v-if="selectedRows.length > 0 && isAdmin" class="batch-bar">
      <span>已选择 <strong>{{ selectedRows.length }}</strong> 项</span>
      <div class="batch-bar__btns">
        <el-button size="small" type="success" :loading="batchRunning" @click="batchPublish">
          批量发布选中项
        </el-button>
        <el-button size="small" type="warning" :loading="batchRunning" @click="batchOffline">
          批量下线选中项
        </el-button>
      </div>
    </div>

    <el-table
      v-loading="loading"
      :data="rows"
      border
      stripe
      @selection-change="onSelectionChange"
    >
      <el-table-column v-if="isAdmin" type="selection" width="46" />
      <el-table-column prop="id" label="ID" width="76" />
      <el-table-column label="标题" min-width="240">
        <template #default="{ row }">
          <div class="title-cell">
            <span class="title-cell__text">{{ row.title }}</span>
            <el-tag v-if="row.hasUnpublishedChanges" size="small" type="warning">有未发布修改</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column v-if="hasCategory" label="分类" width="130">
        <template #default="{ row }">{{ row.categoryName || '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag
            size="small"
            :type="row.status === 'PUBLISHED' ? 'success' : row.status === 'OFFLINE' ? 'warning' : 'info'"
          >
            {{ row.status === 'PUBLISHED' ? '已发布' : row.status === 'OFFLINE' ? '已下线' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="版本 (草稿/线上)" width="130" align="center">
        <template #default="{ row }">
          v{{ row.draftVersion }} / {{ row.publishedVersion ? `v${row.publishedVersion}` : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="85" align="center" />
      <el-table-column label="最后更新" width="168">
        <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="290" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="goEdit(row)">编辑</el-button>
          <el-button v-if="contentType !== 'CUSTOMER'" link type="info" @click="goPreview(row)">预览</el-button>
          <el-button link @click="handleClone(row)">克隆副本</el-button>
          <template v-if="isAdmin">
            <el-button
              v-if="row.hasUnpublishedChanges || row.status !== 'PUBLISHED'"
              link
              type="success"
              @click="handlePublish(row)"
            >
              发布
            </el-button>
            <el-button v-if="row.status === 'PUBLISHED'" link type="warning" @click="handleOffline(row)">
              下线
            </el-button>
            <el-button v-if="row.status !== 'PUBLISHED'" link type="danger" @click="handleDelete(row)">
              删除
            </el-button>
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
</template>

<style scoped>
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.head__title {
  font-size: 16px;
  font-weight: 600;
}

.head__sub {
  margin-left: 10px;
  font-size: 12px;
  color: var(--a-text-muted);
}

.actions {
  display: flex;
  gap: 8px;
}

.filters {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.batch-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  margin-bottom: 12px;
  background: #f5f3ff;
  border: 1px solid #c4b5fd;
  border-radius: 8px;
  font-size: 13px;
  color: #3730a3;
}

.batch-bar__btns {
  display: flex;
  gap: 8px;
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.title-cell__text {
  font-weight: 600;
}

.pager {
  margin-top: 14px;
  justify-content: flex-end;
}
</style>
```

### `website/admin/src/views/LeadListView.vue`

```vue
<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ApiError } from '@/api/http'
import {
  getLead,
  listLeads,
  updateLeadStatus,
  type LeadDetail,
  type LeadListItem,
  type LeadStatus,
} from '@/api/leads'

const STATUS_COLUMNS: Array<{ key: LeadStatus; label: string; tag: 'danger' | 'warning' | 'success' | 'info' }> = [
  { key: 'NEW', label: '待跟进 (NEW)', tag: 'danger' },
  { key: 'CONTACTED', label: '已联系跟进 (CONTACTED)', tag: 'warning' },
  { key: 'CLOSED', label: '已闭环成单 (CLOSED)', tag: 'success' },
  { key: 'INVALID', label: '无效线索 (INVALID)', tag: 'info' },
]

const viewMode = ref<'table' | 'kanban'>('table')
const loading = ref(false)
const rows = ref<LeadListItem[]>([])
const total = ref(0)

const query = reactive({
  status: '',
  keyword: '',
  range: [] as string[],
  page: 1,
  pageSize: 20,
})

const detailOpen = ref(false)
const detailLoading = ref(false)
const currentLead = ref<LeadDetail | null>(null)
const followForm = reactive<{ status: LeadStatus; note: string }>({
  status: 'CONTACTED',
  note: '',
})
const savingFollow = ref(false)

const kanbanGroups = computed(() => {
  const map: Record<LeadStatus, LeadListItem[]> = {
    NEW: [],
    CONTACTED: [],
    CLOSED: [],
    INVALID: [],
  }
  for (const r of rows.value) {
    if (map[r.status]) map[r.status].push(r)
  }
  return map
})

function isOverdueSla(row: LeadListItem): boolean {
  if (row.status !== 'NEW' || !row.createdAt) return false
  const created = new Date(row.createdAt).getTime()
  if (Number.isNaN(created)) return false
  return Date.now() - created > 24 * 3600 * 1000
}

function formatTime(value: string | null | undefined) {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  return d.toLocaleString('zh-CN', { hour12: false })
}

async function load() {
  loading.value = true
  try {
    const res = await listLeads({
      status: query.status || undefined,
      keyword: query.keyword.trim() || undefined,
      from: query.range[0] || undefined,
      to: query.range[1] || undefined,
      page: query.page,
      pageSize: query.pageSize,
    })
    rows.value = res.items
    total.value = res.total
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '咨询线索加载失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.status = ''
  query.keyword = ''
  query.range = []
  query.page = 1
  load()
}

async function openLeadDetail(row: LeadListItem) {
  detailOpen.value = true
  detailLoading.value = true
  try {
    const detail = await getLead(row.id)
    currentLead.value = detail
    followForm.status = detail.status === 'NEW' ? 'CONTACTED' : detail.status
    followForm.note = detail.lastNote ?? ''
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '详情加载失败')
    detailOpen.value = false
  } finally {
    detailLoading.value = false
  }
}

async function saveFollowUp() {
  if (!currentLead.value) return
  if (
    (currentLead.value.status === 'CLOSED' || currentLead.value.status === 'INVALID') &&
    followForm.status === 'CONTACTED' &&
    !followForm.note.trim()
  ) {
    ElMessage.warning('将已关闭或无效线索重新激活为跟进中时，必须填写跟进备注说明原因')
    return
  }
  savingFollow.value = true
  try {
    await updateLeadStatus(currentLead.value.id, {
      status: followForm.status,
      note: followForm.note.trim(),
      expectedLockVersion: currentLead.value.lockVersion,
    })
    ElMessage.success('线索状态与跟进记录已更新')
    detailOpen.value = false
    await load()
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '保存失败')
  } finally {
    savingFollow.value = false
  }
}

function exportLeadsCsv() {
  if (!rows.value.length) {
    ElMessage.warning('当前列表没有可导出的线索')
    return
  }
  const headers = ['单号', '姓名', '公司名称', '脱敏电话', '来源位置', '关联内容', '状态', '处理人', '提交时间']
  const lines = [
    headers.join(','),
    ...rows.value.map((r) =>
      [
        r.receiptNo,
        `"${(r.name || '').replace(/"/g, '""')}"`,
        `"${(r.companyName || '').replace(/"/g, '""')}"`,
        r.phoneMasked,
        r.sourcePosition,
        `"${(r.contentTitle || '-').replace(/"/g, '""')}"`,
        r.status,
        r.handledByName || '-',
        formatTime(r.createdAt),
      ].join(','),
    ),
  ]
  const blob = new Blob(['\uFEFF' + lines.join('\n')], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `leads-p${query.page}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('已导出当前页脱敏线索列表 CSV')
}

onMounted(load)
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <div class="head">
        <div>
          <span class="head__title">客户咨询线索管理</span>
          <span class="head__sub">列表默认展示末四位脱敏手机号；点击「查看并跟进」触发审计并解密完整号码</span>
        </div>
        <div class="head__actions">
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button value="table">📋 表格视图</el-radio-button>
            <el-radio-button value="kanban">🗂️ 看板视图 (Kanban)</el-radio-button>
          </el-radio-group>
          <el-button size="small" plain @click="exportLeadsCsv">导出当前页 CSV</el-button>
          <el-button size="small" @click="load">刷新</el-button>
        </div>
      </div>
    </template>

    <div class="filters">
      <el-select
        v-model="query.status"
        placeholder="全部状态"
        clearable
        style="width: 160px"
        @change="query.page = 1; load()"
      >
        <el-option v-for="col in STATUS_COLUMNS" :key="col.key" :label="col.label" :value="col.key" />
      </el-select>
      <el-input
        v-model="query.keyword"
        placeholder="搜索姓名 / 公司名称..."
        clearable
        style="width: 220px"
        @keyup.enter="query.page = 1; load()"
      />
      <el-date-picker
        v-model="query.range"
        type="daterange"
        value-format="YYYY-MM-DD"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        style="width: 250px"
      />
      <el-button type="primary" @click="query.page = 1; load()">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <!-- 模式 A：表格视图 -->
    <el-table v-if="viewMode === 'table'" v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="receiptNo" label="回执单号" width="135" />
      <el-table-column label="客户姓名" width="140">
        <template #default="{ row }">
          <strong>{{ row.name }}</strong>
          <el-tag v-if="isOverdueSla(row)" size="small" type="danger" style="margin-left: 6px">
            超时未跟进
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="companyName" label="公司名称" min-width="180" />
      <el-table-column prop="phoneMasked" label="联系电话(脱敏)" width="135" />
      <el-table-column label="来源位置 / 关联内容" min-width="180">
        <template #default="{ row }">
          <span>{{ row.sourcePosition }}</span>
          <span v-if="row.contentTitle" class="sub-txt"> · {{ row.contentTitle }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag
            size="small"
            :type="
              row.status === 'NEW'
                ? 'danger'
                : row.status === 'CONTACTED'
                ? 'warning'
                : row.status === 'CLOSED'
                ? 'success'
                : 'info'
            "
          >
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="handledByName" label="跟进人" width="110">
        <template #default="{ row }">{{ row.handledByName || '-' }}</template>
      </el-table-column>
      <el-table-column label="提交时间" width="168">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="130" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openLeadDetail(row)">查看并跟进</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 模式 B：Kanban 看板视图 -->
    <div v-else v-loading="loading" class="kanban">
      <div v-for="col in STATUS_COLUMNS" :key="col.key" class="kanban__col">
        <div class="kanban__head">
          <strong>{{ col.label }}</strong>
          <el-tag size="small" :type="col.tag">{{ kanbanGroups[col.key].length }}</el-tag>
        </div>
        <div class="kanban__list">
          <div
            v-for="item in kanbanGroups[col.key]"
            :key="item.id"
            class="kanban-card"
            :class="{ 'kanban-card--overdue': isOverdueSla(item) }"
            @click="openLeadDetail(item)"
          >
            <div class="kanban-card__top">
              <strong>{{ item.name }}</strong>
              <span class="kanban-card__phone">{{ item.phoneMasked }}</span>
            </div>
            <p class="kanban-card__company">{{ item.companyName }}</p>
            <div class="kanban-card__foot">
              <span>{{ item.sourcePosition }}</span>
              <span>{{ formatTime(item.createdAt) }}</span>
            </div>
          </div>
          <p v-if="kanbanGroups[col.key].length === 0" class="kanban__empty">暂无该状态线索</p>
        </div>
      </div>
    </div>

    <el-pagination
      class="pager"
      layout="total, prev, pager, next"
      :total="total"
      :page-size="query.pageSize"
      :current-page="query.page"
      @current-change="(p: number) => { query.page = p; load() }"
    />

    <!-- 线索详情与状态流转弹窗 -->
    <el-dialog v-model="detailOpen" title="客户咨询详情与跟进记录" width="620px">
      <div v-loading="detailLoading">
        <el-descriptions v-if="currentLead" :column="2" border>
          <el-descriptions-item label="回执单号">{{ currentLead.receiptNo }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag size="small">{{ currentLead.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="客户姓名">{{ currentLead.name }}</el-descriptions-item>
          <el-descriptions-item label="完整联系电话">
            <strong class="phone-full">{{ currentLead.phone }}</strong>
          </el-descriptions-item>
          <el-descriptions-item label="公司名称" :span="2">{{ currentLead.companyName }}</el-descriptions-item>
          <el-descriptions-item label="来源路径">{{ currentLead.sourcePath }}</el-descriptions-item>
          <el-descriptions-item label="来源位置">{{ currentLead.sourcePosition }}</el-descriptions-item>
          <el-descriptions-item label="关联内容" :span="2">
            {{ currentLead.contentTitle ? `${currentLead.contentType} #${currentLead.contentId} - ${currentLead.contentTitle}` : '无特定关联' }}
          </el-descriptions-item>
          <el-descriptions-item label="隐私协议版本">v{{ currentLead.consentVersion }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatTime(currentLead.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="咨询需求描述" :span="2">
            <div class="demand-box">{{ currentLead.demand || '-' }}</div>
          </el-descriptions-item>
        </el-descriptions>

        <el-form v-if="currentLead" label-position="top" class="follow-form">
          <el-form-item label="流转状态">
            <el-radio-group v-model="followForm.status">
              <el-radio value="CONTACTED">已联系跟进 (CONTACTED)</el-radio>
              <el-radio value="CLOSED">已闭环完成 (CLOSED)</el-radio>
              <el-radio value="INVALID">标记无效 (INVALID)</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="内部跟进备注 (最多 2000 字)">
            <el-input
              v-model="followForm.note"
              type="textarea"
              :rows="3"
              maxlength="2000"
              show-word-limit
              placeholder="记录沟通情况、客户预算或下一步安排..."
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="detailOpen = false">关闭</el-button>
        <el-button type="primary" :loading="savingFollow" @click="saveFollowUp">保存跟进结果</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.head__title {
  font-size: 16px;
  font-weight: 600;
}

.head__sub {
  margin-left: 10px;
  font-size: 12px;
  color: var(--a-text-muted);
}

.head__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filters {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.sub-txt {
  color: var(--a-text-muted);
  font-size: 12px;
}

/* 看板视图 */
.kanban {
  display: grid;
  grid-template-columns: repeat(4, minmax(220px, 1fr));
  gap: 14px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.kanban__col {
  background: #f6f8fb;
  border: 1px solid var(--a-border);
  border-radius: 10px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  min-height: 360px;
}

.kanban__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 13.5px;
}

.kanban__list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.kanban-card {
  background: #ffffff;
  border: 1px solid var(--a-border);
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.kanban-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.06);
}

.kanban-card--overdue {
  border-left: 3px solid #ef4444;
}

.kanban-card__top {
  display: flex;
  justify-content: space-between;
  font-size: 13.5px;
}

.kanban-card__phone {
  font-size: 12px;
  color: #5b4be8;
  font-family: monospace;
}

.kanban-card__company {
  margin: 6px 0;
  font-size: 12.5px;
  color: var(--a-text-muted);
}

.kanban-card__foot {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #9ca3af;
}

.kanban__empty {
  text-align: center;
  font-size: 12px;
  color: var(--a-text-muted);
  padding: 32px 0;
}

.phone-full {
  color: #5b4be8;
  font-size: 15px;
  font-family: monospace;
}

.demand-box {
  white-space: pre-wrap;
  word-break: break-word;
  background: #f8f9fc;
  padding: 10px 12px;
  border-radius: 6px;
  line-height: 1.6;
}

.follow-form {
  margin-top: 16px;
}

.pager {
  margin-top: 14px;
  justify-content: flex-end;
}
</style>
```

### `website/admin/src/views/MediaView.vue`

```vue
<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ApiError, upload } from '@/api/http'
import { deleteMedia, listMedia, mediaReferences, type MediaItem } from '@/api/content'
import { useAuth } from '@/stores/auth'

const { current } = useAuth()
const isAdmin = computed(() => current.value?.role === 'ADMIN')

const loading = ref(false)
const uploading = ref(false)
const dragOver = ref(false)
const items = ref<MediaItem[]>([])
const total = ref(0)
const fileInput = ref<HTMLInputElement>()

const query = reactive({
  keyword: '',
  type: '' as '' | 'IMAGE' | 'VIDEO',
  referencedFilter: 'ALL' as 'ALL' | 'YES' | 'NO',
  page: 1,
  pageSize: 20,
})

const refDrawerOpen = ref(false)
const refLoading = ref(false)
const currentMedia = ref<MediaItem | null>(null)
const refList = ref<Array<Record<string, string>>>([])

async function load() {
  loading.value = true
  try {
    const res = await listMedia({
      keyword: query.keyword.trim() || undefined,
      type: query.type || undefined,
      referenced:
        query.referencedFilter === 'YES'
          ? true
          : query.referencedFilter === 'NO'
          ? false
          : undefined,
      page: query.page,
      pageSize: query.pageSize,
    })
    items.value = res.items
    total.value = res.total
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '素材库加载失败')
  } finally {
    loading.value = false
  }
}

async function handleFiles(fileList: FileList | null | undefined) {
  if (!fileList || fileList.length === 0) return
  uploading.value = true
  let ok = 0
  for (const file of Array.from(fileList)) {
    try {
      const form = new FormData()
      form.append('file', file)
      await upload<MediaItem>('/api/admin/media', form)
      ok++
    } catch (error) {
      ElMessage.error(error instanceof ApiError ? `${file.name}: ${error.message}` : `${file.name} 上传失败`)
    }
  }
  uploading.value = false
  if (ok > 0) {
    ElMessage.success(`成功上传 ${ok} 个素材文件`)
    query.page = 1
    await load()
  }
}

async function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  await handleFiles(input.files)
  input.value = ''
}

async function onDrop(event: DragEvent) {
  dragOver.value = false
  await handleFiles(event.dataTransfer?.files)
}

async function openRefs(item: MediaItem) {
  currentMedia.value = item
  refDrawerOpen.value = true
  refLoading.value = true
  try {
    refList.value = await mediaReferences(item.id)
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '引用查询失败')
    refList.value = []
  } finally {
    refLoading.value = false
  }
}

async function copyMediaId(item: MediaItem) {
  try {
    await navigator.clipboard.writeText(String(item.id))
    ElMessage.success(`已复制素材 ID: #${item.id}`)
  } catch {
    // ignore
  }
}

async function handleDelete(item: MediaItem) {
  if (item.referenced) {
    ElMessage.warning('该素材当前正被草稿或已发布内容引用，请先解除引用后再删除')
    return
  }
  try {
    await ElMessageBox.confirm(`确认删除无引用素材「${item.originalName}」(#${item.id})？`, '确认删除', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deleteMedia(item.id)
    ElMessage.success('已删除')
    await load()
  } catch (error) {
    if (error instanceof ApiError) ElMessage.error(error.message)
  }
}

function formatSize(bytes: number) {
  if (!bytes) return '0 KB'
  if (bytes < 1024 * 1024) return `${Math.max(1, Math.round(bytes / 1024))} KB`
  return `${(bytes / (1024 * 1024)).toFixed(2)} MB`
}

onMounted(load)
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <div class="head">
        <div>
          <span class="head__title">素材资源库</span>
          <span class="head__sub">受控存储图片与视频，支持查看被哪些配置或文章引用，防止误删线上图片</span>
        </div>
        <div class="actions">
          <input
            ref="fileInput"
            type="file"
            multiple
            accept="image/png,image/jpeg,image/webp,video/mp4,video/webm"
            hidden
            @change="onFileChange"
          />
          <el-button @click="load">刷新</el-button>
          <el-button type="primary" :loading="uploading" @click="fileInput?.click()">
            + 上传图片 / 视频
          </el-button>
        </div>
      </div>
    </template>

    <!-- 拖拽上传热区 -->
    <div
      class="drop-banner"
      :class="{ 'is-dragover': dragOver }"
      @dragover.prevent="dragOver = true"
      @dragleave.prevent="dragOver = false"
      @drop.prevent="onDrop"
    >
      <span>📤 将 JPG / PNG / WebP 图片（≤5MB）或 MP4 / WebM 视频（≤50MB）直接拖拽到此处即可批量上传</span>
    </div>

    <div class="filters">
      <el-input
        v-model="query.keyword"
        placeholder="搜索原始文件名..."
        clearable
        style="width: 220px"
        @keyup.enter="query.page = 1; load()"
      />
      <el-select v-model="query.type" style="width: 130px" @change="query.page = 1; load()">
        <el-option label="全部类型" value="" />
        <el-option label="图片 (IMAGE)" value="IMAGE" />
        <el-option label="视频 (VIDEO)" value="VIDEO" />
      </el-select>
      <el-select v-model="query.referencedFilter" style="width: 150px" @change="query.page = 1; load()">
        <el-option label="全部引用状态" value="ALL" />
        <el-option label="已被引用" value="YES" />
        <el-option label="未被引用（可清理）" value="NO" />
      </el-select>
      <el-button type="primary" plain @click="query.page = 1; load()">查询</el-button>
    </div>

    <div v-loading="loading" class="media-grid">
      <div v-for="item in items" :key="item.id" class="media-card">
        <div class="media-card__thumb">
          <video
            v-if="item.mediaType === 'VIDEO'"
            :src="`/api/admin/media/${item.id}/file`"
            muted
            playsinline
            preload="metadata"
          ></video>
          <img v-else :src="`/api/admin/media/${item.id}/file`" :alt="item.originalName" loading="lazy" />
          <span class="media-card__id">#{{ item.id }}</span>
          <el-tag
            size="small"
            :type="item.referenced ? 'success' : 'info'"
            class="media-card__ref-tag"
          >
            {{ item.referenced ? '使用中' : '未引用' }}
          </el-tag>
        </div>

        <div class="media-card__body">
          <p class="media-card__name" :title="item.originalName">{{ item.originalName }}</p>
          <p class="media-card__meta">
            {{ item.mediaType === 'VIDEO' ? '视频' : `${item.width}×${item.height}` }} · {{ formatSize(item.byteSize) }}
          </p>
          <div class="media-card__actions">
            <el-button link size="small" type="primary" @click="openRefs(item)">引用详情</el-button>
            <el-button link size="small" @click="copyMediaId(item)">复制ID</el-button>
            <el-button
              v-if="isAdmin"
              link
              size="small"
              type="danger"
              :disabled="item.referenced"
              @click="handleDelete(item)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && items.length === 0" description="暂无符合条件的素材" />
    </div>

    <el-pagination
      class="pager"
      layout="total, prev, pager, next"
      :total="total"
      :page-size="query.pageSize"
      :current-page="query.page"
      @current-change="(p: number) => { query.page = p; load() }"
    />

    <!-- 引用关系抽屉 -->
    <el-drawer
      v-model="refDrawerOpen"
      :title="`素材引用追踪：${currentMedia?.originalName || ''} (#${currentMedia?.id || ''})`"
      size="460px"
    >
      <div v-loading="refLoading">
        <p class="drawer-tip">
          只要素材存在任何草稿（DRAFT）或线上发布（PUBLISHED）引用，系统将自动锁死删除操作，保障官网不出现裂图。
        </p>
        <el-table v-if="refList.length" :data="refList" border stripe size="small">
          <el-table-column prop="sourceKind" label="来源类型" width="100" />
          <el-table-column prop="sourceId" label="来源标识" width="100" />
          <el-table-column prop="stage" label="阶段" width="95">
            <template #default="{ row }">
              <el-tag size="small" :type="row.stage === 'PUBLISHED' ? 'success' : 'warning'">
                {{ row.stage }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="fieldPath" label="引用字段路径" min-width="130" />
        </el-table>
        <el-empty v-else description="当前没有任何配置或内容引用该素材，管理员可安全删除" />
      </div>
    </el-drawer>
  </el-card>
</template>

<style scoped>
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.head__title {
  font-size: 16px;
  font-weight: 600;
}

.head__sub {
  margin-left: 10px;
  font-size: 12px;
  color: var(--a-text-muted);
}

.actions {
  display: flex;
  gap: 8px;
}

.drop-banner {
  margin-bottom: 14px;
  padding: 12px 16px;
  border-radius: 10px;
  border: 1px dashed #a5b4fc;
  background: #f5f3ff;
  color: #4338ca;
  font-size: 13px;
  text-align: center;
  transition: all 0.2s ease;
}

.drop-banner.is-dragover {
  background: #ede9fe;
  border-color: #5b4be8;
  transform: scale(1.005);
}

.filters {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  min-height: 200px;
}

.media-card {
  border: 1px solid var(--a-border);
  border-radius: 10px;
  overflow: hidden;
  background: #ffffff;
  display: flex;
  flex-direction: column;
}

.media-card__thumb {
  position: relative;
  aspect-ratio: 16 / 10;
  background: #0f172a;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.media-card__thumb img,
.media-card__thumb video {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.media-card__id {
  position: absolute;
  left: 8px;
  top: 8px;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.65);
  color: #ffffff;
}

.media-card__ref-tag {
  position: absolute;
  right: 8px;
  top: 8px;
}

.media-card__body {
  padding: 10px 12px;
}

.media-card__name {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.media-card__meta {
  margin: 4px 0 8px;
  font-size: 11.5px;
  color: var(--a-text-muted);
}

.media-card__actions {
  display: flex;
  justify-content: space-between;
  padding-top: 8px;
  border-top: 1px solid var(--a-border);
}

.drawer-tip {
  font-size: 12.5px;
  color: var(--a-text-muted);
  line-height: 1.6;
  margin: 0 0 14px;
}

.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
```

### `website/admin/src/views/ContentEditorView.vue`

```vue
<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import ArticleBody from '../../../shared/ArticleBody.vue'
import ContentPicker from '@/components/ContentPicker.vue'
import MediaPicker from '@/components/MediaPicker.vue'
import { ApiError } from '@/api/http'
import {
  createContent,
  getContent,
  listCategories,
  offlineContent,
  publishContent,
  saveDraft,
  type CategoryView,
  type ContentView,
  type MediaItem,
} from '@/api/content'
import { useAuth } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const { current } = useAuth()
const isAdmin = computed(() => current.value?.role === 'ADMIN')

const contentType = computed(() => String(route.meta.contentType || 'PRODUCT'))
const slug = computed(() => String(route.meta.slug || 'products'))
const id = computed(() => (typeof route.params.id === 'string' ? route.params.id : ''))
const isEdit = computed(() => Boolean(id.value))

const loading = ref(false)
const saving = ref(false)
const publishing = ref(false)
const meta = ref<ContentView | null>(null)
const rawDraftBackup = ref<Record<string, unknown>>({})
const categories = ref<CategoryView[]>([])

// 编辑器模式：纯源码 / 左右分屏实时预览
const editorMode = ref<'split' | 'code'>('split')
const bodyTextareaRef = ref<HTMLTextAreaElement | null>(null)

// 素材选择器
const pickerOpen = ref(false)
const pickerKind = ref<'IMAGE' | 'VIDEO' | 'ALL'>('IMAGE')
let pickerCallback: ((media: MediaItem) => void) | null = null

const form = reactive({
  title: '',
  summary: '',
  coverMediaId: null as number | null,
  coverAlt: '',
  heroMediaId: null as number | null,
  logoMediaId: null as number | null,
  logoAlt: '',
  websiteUrl: '',
  categoryId: '',
  sortOrder: 100,
  industry: '',
  customerName: '',
  quoteText: '',
  quoteAuthor: '',
  author: '',
  displayTime: new Date().toISOString().slice(0, 16).replace('T', ' '),
  sourceName: '',
  sourceUrl: '',
  bodyHtml: '',
  seoTitle: '',
  seoDescription: '',
  features: [] as Array<{ key?: string; title: string; description: string; iconMediaId?: number | null }>,
  pains: [] as Array<{ key?: string; title: string; description: string }>,
  benefits: [] as Array<{ key?: string; title: string; description: string }>,
  outcomes: [] as Array<{ key?: string; displayValue: string; label: string }>,
  relatedCaseIds: [] as number[],
  relatedProductIds: [] as number[],
  relatedSolutionIds: [] as number[],
})

/** 将正文中的 data-media-id="123" 转换为后台鉴权图片预览地址，供右侧实时预览使用 */
const previewHtml = computed(() => {
  if (!form.bodyHtml) return ''
  return form.bodyHtml.replace(
    /<img([^>]*?)data-media-id=["'](\d+)["']([^>]*?)>/gi,
    '<img$1data-media-id="$2" src="/api/admin/media/$2/file"$3>',
  )
})

function openMedia(kind: 'IMAGE' | 'VIDEO' | 'ALL', cb: (m: MediaItem) => void) {
  pickerKind.value = kind
  pickerCallback = cb
  pickerOpen.value = true
}

function onMediaPicked(media: MediaItem) {
  if (pickerCallback) {
    pickerCallback(media)
    pickerCallback = null
  }
  pickerOpen.value = false
}

function insertHtmlSnippet(snippet: string) {
  const el = bodyTextareaRef.value
  if (!el) {
    form.bodyHtml = (form.bodyHtml ? form.bodyHtml + '\n' : '') + snippet
    return
  }
  const start = el.selectionStart ?? form.bodyHtml.length
  const end = el.selectionEnd ?? form.bodyHtml.length
  const selected = form.bodyHtml.slice(start, end)
  const finalSnippet = selected ? snippet.replace('在此输入内容', selected) : snippet
  form.bodyHtml = form.bodyHtml.slice(0, start) + finalSnippet + form.bodyHtml.slice(end)
}

function insertBodyImage() {
  openMedia('IMAGE', (media) => {
    const tag = `<p><img data-media-id="${media.id}" alt="${media.originalName.replace(/"/g, '')}" /></p>`
    insertHtmlSnippet(tag)
    ElMessage.success(`已插入受控素材图片 #${media.id}`)
  })
}

async function loadCategories() {
  if (contentType.value === 'CUSTOMER') return
  try {
    const res = await listCategories(contentType.value)
    categories.value = res.items
  } catch {
    categories.value = []
  }
}

function fillFromDraft(draft: Record<string, any>) {
  rawDraftBackup.value = { ...draft }
  form.title = draft.title ?? ''
  form.summary = draft.summary ?? ''
  form.coverMediaId = draft.coverMediaId != null ? Number(draft.coverMediaId) : null
  form.coverAlt = draft.coverAlt ?? ''
  form.heroMediaId = draft.heroMediaId != null ? Number(draft.heroMediaId) : null
  form.logoMediaId = draft.logoMediaId != null ? Number(draft.logoMediaId) : null
  form.logoAlt = draft.logoAlt ?? ''
  form.websiteUrl = draft.websiteUrl ?? ''
  form.categoryId = draft.categoryId != null ? String(draft.categoryId) : ''
  form.sortOrder = draft.sortOrder ?? 100
  form.industry = draft.industry ?? ''
  form.customerName = draft.customerName ?? ''
  form.quoteText = draft.quoteText ?? ''
  form.quoteAuthor = draft.quoteAuthor ?? ''
  form.author = draft.author ?? ''
  form.displayTime = draft.displayTime ?? new Date().toISOString().slice(0, 16).replace('T', ' ')
  form.sourceName = draft.sourceName ?? ''
  form.sourceUrl = draft.sourceUrl ?? ''
  form.bodyHtml = draft.bodyHtml ?? ''
  form.seoTitle = draft.seo?.title ?? ''
  form.seoDescription = draft.seo?.description ?? ''
  form.features = Array.isArray(draft.features) ? draft.features.map((f: any) => ({ ...f })) : []
  form.pains = Array.isArray(draft.pains) ? draft.pains.map((p: any) => ({ ...p })) : []
  form.benefits = Array.isArray(draft.benefits) ? draft.benefits.map((b: any) => ({ ...b })) : []
  form.outcomes = Array.isArray(draft.outcomes) ? draft.outcomes.map((o: any) => ({ ...o })) : []
  form.relatedCaseIds = Array.isArray(draft.relatedCaseIds) ? draft.relatedCaseIds.map(Number) : []
  form.relatedProductIds = Array.isArray(draft.relatedProductIds) ? draft.relatedProductIds.map(Number) : []
  form.relatedSolutionIds = Array.isArray(draft.relatedSolutionIds) ? draft.relatedSolutionIds.map(Number) : []
}

function buildDraftPayload(): Record<string, unknown> {
  if (contentType.value === 'CUSTOMER') {
    return {
      ...rawDraftBackup.value,
      title: form.title.trim(),
      logoMediaId: form.logoMediaId,
      logoAlt: form.logoAlt.trim() || form.title.trim(),
      websiteUrl: form.websiteUrl.trim(),
      sortOrder: form.sortOrder,
    }
  }

  const base: Record<string, unknown> = {
    ...rawDraftBackup.value,
    title: form.title.trim(),
    summary: form.summary.trim(),
    coverMediaId: form.coverMediaId,
    coverAlt: form.coverAlt.trim() || form.title.trim(),
    categoryId: form.categoryId ? Number(form.categoryId) : null,
    sortOrder: form.sortOrder,
    bodyHtml: form.bodyHtml,
    seo: {
      title: form.seoTitle.trim(),
      description: form.seoDescription.trim(),
    },
  }

  if (contentType.value === 'PRODUCT') {
    base.heroMediaId = form.heroMediaId
    base.features = form.features
    base.relatedCaseIds = form.relatedCaseIds
  } else if (contentType.value === 'SOLUTION') {
    base.industry = form.industry.trim()
    base.heroMediaId = form.heroMediaId
    base.pains = form.pains
    base.benefits = form.benefits
    base.relatedProductIds = form.relatedProductIds
  } else if (contentType.value === 'CASE') {
    base.customerName = form.customerName.trim()
    base.industry = form.industry.trim()
    base.outcomes = form.outcomes
    base.quoteText = form.quoteText.trim()
    base.quoteAuthor = form.quoteAuthor.trim()
    base.relatedProductIds = form.relatedProductIds
    base.relatedSolutionIds = form.relatedSolutionIds
  } else if (contentType.value === 'NEWS') {
    base.author = form.author.trim()
    base.displayTime = form.displayTime.trim()
    base.sourceName = form.sourceName.trim()
    base.sourceUrl = form.sourceUrl.trim()
  }

  return base
}

async function load() {
  if (!isEdit.value) return
  loading.value = true
  try {
    const detail = await getContent(id.value)
    meta.value = detail.content
    fillFromDraft(detail.draft)
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '内容加载失败')
  } finally {
    loading.value = false
  }
}

async function onSaveDraft() {
  if (!form.title.trim()) {
    ElMessage.warning('请先填写标题')
    return false
  }
  saving.value = true
  try {
    const payload = buildDraftPayload()
    if (isEdit.value && meta.value) {
      const updated = await saveDraft(id.value, payload, meta.value.lockVersion)
      meta.value = updated.content
      fillFromDraft(updated.draft)
      ElMessage.success(`草稿已保存 (v${updated.content.draftVersion})`)
    } else {
      const created = await createContent(contentType.value, payload)
      meta.value = created.content
      ElMessage.success('草稿已创建')
      await router.replace(`/contents/${slug.value}/${created.content.id}/edit`)
    }
    return true
  } catch (error) {
    if (error instanceof ApiError) {
      const fields = error.fieldErrors?.map((f) => `${f.field}: ${f.message}`).join('; ')
      ElMessage.error(fields ? `${error.message} (${fields})` : error.message)
    } else {
      ElMessage.error('保存草稿失败')
    }
    return false
  } finally {
    saving.value = false
  }
}

async function onPublish() {
  const saved = await onSaveDraft()
  if (!saved || !meta.value) return
  try {
    await ElMessageBox.confirm('发布后前台立即更新为当前版本，确认发布？', '确认发布', {
      confirmButtonText: '立即发布',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  publishing.value = true
  try {
    await publishContent(meta.value.id, meta.value.lockVersion, meta.value.draftVersion)
    ElMessage.success('已正式发布上线')
    await load()
  } catch (error) {
    if (error instanceof ApiError) {
      const fields = error.fieldErrors?.map((f) => `${f.field}: ${f.message}`).join('; ')
      ElMessage.error(fields ? `${error.message} (${fields})` : error.message)
    } else {
      ElMessage.error('发布失败')
    }
  } finally {
    publishing.value = false
  }
}

async function onOffline() {
  if (!meta.value) return
  try {
    await ElMessageBox.confirm('确认下线该内容？', '确认下线', { type: 'warning' })
    await offlineContent(meta.value.id, meta.value.lockVersion, meta.value.draftVersion)
    ElMessage.success('已下线')
    await load()
  } catch (error) {
    if (error instanceof ApiError) ElMessage.error(error.message)
  }
}

function goBack() {
  router.push(`/contents/${slug.value}`)
}

function goPreview() {
  if (!meta.value) return
  router.push(`/preview/${slug.value}/${meta.value.id}`)
}

onMounted(() => {
  loadCategories()
  load()
})
</script>

<template>
  <div v-loading="loading" class="editor-page">
    <!-- 顶部粘性状态与动作栏 -->
    <div class="top-bar">
      <div class="top-bar__left">
        <el-button link @click="goBack">← 返回列表</el-button>
        <strong class="top-bar__title">{{ isEdit ? `编辑 #${id}` : '新建内容' }}</strong>
        <template v-if="meta">
          <el-tag size="small" :type="meta.status === 'PUBLISHED' ? 'success' : 'info'">
            {{ meta.status === 'PUBLISHED' ? `线上 v${meta.publishedVersion}` : meta.status === 'OFFLINE' ? '已下线' : '草稿未发布' }}
          </el-tag>
          <el-tag size="small" type="info">草稿 v{{ meta.draftVersion }}</el-tag>
          <el-tag v-if="meta.hasUnpublishedChanges" size="small" type="warning">有未发布修改</el-tag>
        </template>
      </div>
      <div class="top-bar__actions">
        <el-button v-if="isEdit && contentType !== 'CUSTOMER'" @click="goPreview">预览草稿</el-button>
        <el-button type="primary" :loading="saving" @click="onSaveDraft">保存草稿</el-button>
        <el-button v-if="isAdmin" type="success" :loading="publishing" @click="onPublish">
          保存并发布
        </el-button>
        <el-button
          v-if="isAdmin && meta?.status === 'PUBLISHED'"
          type="warning"
          plain
          @click="onOffline"
        >
          下线
        </el-button>
      </div>
    </div>

    <!-- 客户标识 CUSTOMER 专属极简表单 -->
    <el-card v-if="contentType === 'CUSTOMER'" shadow="never" class="main-card">
      <el-form label-width="110px">
        <el-form-item label="客户名称" required>
          <el-input v-model="form.title" maxlength="60" show-word-limit />
        </el-form-item>
        <el-form-item label="客户 Logo" required>
          <div class="media-pick">
            <img v-if="form.logoMediaId" :src="`/api/admin/media/${form.logoMediaId}/file`" alt="Logo" />
            <span v-else class="media-pick__empty">未选择</span>
            <el-button size="small" @click="openMedia('IMAGE', (m) => (form.logoMediaId = Number(m.id)))">
              选择 Logo 图片
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="客户官网外链">
          <el-input v-model="form.websiteUrl" maxlength="500" placeholder="可选，https://..." />
        </el-form-item>
        <el-form-item label="排序权重">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 产品 / 方案 / 案例 / 新闻 完整编辑器 -->
    <template v-else>
      <el-card shadow="never" class="main-card">
        <el-form label-width="110px">
          <el-form-item label="标题" required>
            <el-input v-model="form.title" maxlength="120" show-word-limit placeholder="请输入标题" />
          </el-form-item>

          <div class="form-grid-2">
            <el-form-item label="所属分类">
              <el-select v-model="form.categoryId" clearable placeholder="选择分类" style="width: 100%">
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="排序权重">
              <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
            </el-form-item>
          </div>

          <el-form-item label="摘要导语">
            <el-input v-model="form.summary" type="textarea" :rows="2" maxlength="300" show-word-limit />
          </el-form-item>

          <div class="form-grid-2">
            <el-form-item label="列表封面图" required>
              <div class="media-pick">
                <img v-if="form.coverMediaId" :src="`/api/admin/media/${form.coverMediaId}/file`" alt="封面" />
                <span v-else class="media-pick__empty">必选</span>
                <el-button size="small" @click="openMedia('IMAGE', (m) => (form.coverMediaId = Number(m.id)))">
                  选择封面
                </el-button>
              </div>
            </el-form-item>
            <el-form-item v-if="contentType !== 'NEWS'" label="详情首屏大图">
              <div class="media-pick">
                <img v-if="form.heroMediaId" :src="`/api/admin/media/${form.heroMediaId}/file`" alt="Hero" />
                <span v-else class="media-pick__empty">可选(默认用封面)</span>
                <el-button size="small" @click="openMedia('ALL', (m) => (form.heroMediaId = Number(m.id)))">
                  选择素材
                </el-button>
                <el-button v-if="form.heroMediaId" size="small" link type="danger" @click="form.heroMediaId = null">
                  清除
                </el-button>
              </div>
            </el-form-item>
          </div>

          <!-- 类型专属结构化字段 -->
          <template v-if="contentType === 'PRODUCT'">
            <el-divider content-position="left">产品核心特性 (features，最多 8 条)</el-divider>
            <div v-for="(feat, fIdx) in form.features" :key="fIdx" class="sub-row">
              <el-input v-model="feat.title" maxlength="30" placeholder="特性标题" style="width: 200px" />
              <el-input v-model="feat.description" maxlength="200" placeholder="特性说明" style="flex: 1" />
              <el-button link type="danger" @click="form.features.splice(fIdx, 1)">删除</el-button>
            </div>
            <el-button size="small" @click="form.features.length < 8 && form.features.push({ title: '', description: '' })">
              + 添加产品特性
            </el-button>

            <el-divider content-position="left">关联标杆案例</el-divider>
            <ContentPicker v-model="form.relatedCaseIds" content-type="CASE" :max="6" />
          </template>

          <template v-else-if="contentType === 'SOLUTION'">
            <el-form-item label="适用行业">
              <el-input v-model="form.industry" maxlength="60" style="width: 280px" placeholder="例如：冷链物流 / 大宗能源" />
            </el-form-item>
            <el-divider content-position="left">行业痛点 (pains，最多 6 条)</el-divider>
            <div v-for="(p, pIdx) in form.pains" :key="pIdx" class="sub-row">
              <el-input v-model="p.title" maxlength="30" placeholder="痛点标题" style="width: 200px" />
              <el-input v-model="p.description" maxlength="200" placeholder="痛点说明" style="flex: 1" />
              <el-button link type="danger" @click="form.pains.splice(pIdx, 1)">删除</el-button>
            </div>
            <el-button size="small" @click="form.pains.length < 6 && form.pains.push({ title: '', description: '' })">
              + 添加行业痛点
            </el-button>

            <el-divider content-position="left">方案核心价值 (benefits，最多 6 条)</el-divider>
            <div v-for="(b, bIdx) in form.benefits" :key="bIdx" class="sub-row">
              <el-input v-model="b.title" maxlength="30" placeholder="价值标题" style="width: 200px" />
              <el-input v-model="b.description" maxlength="200" placeholder="价值说明" style="flex: 1" />
              <el-button link type="danger" @click="form.benefits.splice(bIdx, 1)">删除</el-button>
            </div>
            <el-button size="small" @click="form.benefits.length < 6 && form.benefits.push({ title: '', description: '' })">
              + 添加方案价值
            </el-button>

            <el-divider content-position="left">关联推荐产品</el-divider>
            <ContentPicker v-model="form.relatedProductIds" content-type="PRODUCT" :max="6" />
          </template>

          <template v-else-if="contentType === 'CASE'">
            <div class="form-grid-2">
              <el-form-item label="客户名称" required>
                <el-input v-model="form.customerName" maxlength="60" />
              </el-form-item>
              <el-form-item label="所属行业">
                <el-input v-model="form.industry" maxlength="60" />
              </el-form-item>
            </div>

            <el-divider content-position="left">量化成果指标 (outcomes，最多 4 条)</el-divider>
            <div v-for="(out, oIdx) in form.outcomes" :key="oIdx" class="sub-row">
              <el-input v-model="out.displayValue" maxlength="20" placeholder="数值(如 68%↓)" style="width: 160px" />
              <el-input v-model="out.label" maxlength="40" placeholder="成果说明(如 事故率下降)" style="flex: 1" />
              <el-button link type="danger" @click="form.outcomes.splice(oIdx, 1)">删除</el-button>
            </div>
            <el-button size="small" @click="form.outcomes.length < 4 && form.outcomes.push({ displayValue: '', label: '' })">
              + 添加成果指标
            </el-button>

            <div class="form-grid-2" style="margin-top: 14px">
              <el-form-item label="客户证言">
                <el-input v-model="form.quoteText" maxlength="300" placeholder="选填，与证言人成对填写" />
              </el-form-item>
              <el-form-item label="证言人及职务">
                <el-input v-model="form.quoteAuthor" maxlength="60" />
              </el-form-item>
            </div>
          </template>

          <template v-else-if="contentType === 'NEWS'">
            <div class="form-grid-2">
              <el-form-item label="展示日期" required>
                <el-input v-model="form.displayTime" placeholder="YYYY-MM-DD HH:mm" />
              </el-form-item>
              <el-form-item label="作者">
                <el-input v-model="form.author" maxlength="50" />
              </el-form-item>
            </div>
            <div class="form-grid-2">
              <el-form-item label="来源名称">
                <el-input v-model="form.sourceName" maxlength="100" />
              </el-form-item>
              <el-form-item label="来源外链">
                <el-input v-model="form.sourceUrl" maxlength="500" placeholder="https://..." />
              </el-form-item>
            </div>
          </template>

          <!-- 可视化富文本工具栏 + 左右分屏实时预览 -->
          <el-divider content-position="left">详情正文 (支持快捷排版与实时预览)</el-divider>
          <div class="richtext-toolbar">
            <div class="richtext-toolbar__btns">
              <el-button size="small" @click="insertHtmlSnippet('<h2>在此输入内容</h2>\n')">H2 章节标题</el-button>
              <el-button size="small" @click="insertHtmlSnippet('<h3>在此输入内容</h3>\n')">H3 子标题</el-button>
              <el-button size="small" @click="insertHtmlSnippet('<p><strong>在此输入内容</strong></p>\n')">B 粗体段落</el-button>
              <el-button size="small" @click="insertHtmlSnippet('<blockquote><p>在此输入内容</p></blockquote>\n')">“ 引用高亮</el-button>
              <el-button size="small" @click="insertHtmlSnippet('<ul>\n  <li>要点一</li>\n  <li>要点二</li>\n</ul>\n')">• 列表</el-button>
              <el-button size="small" @click="insertHtmlSnippet('<table><thead><tr><th>指标</th><th>数值</th></tr></thead><tbody><tr><td>效率提升</td><td>45%</td></tr></tbody></table>\n')">
                📊 表格
              </el-button>
              <el-button size="small" type="primary" plain @click="insertBodyImage">
                🖼️ 插入素材库图片 (data-media-id)
              </el-button>
            </div>
            <el-radio-group v-model="editorMode" size="small">
              <el-radio-button value="split">左右分屏实时预览</el-radio-button>
              <el-radio-button value="code">仅编辑源码</el-radio-button>
            </el-radio-group>
          </div>

          <div class="richtext-workbench" :class="{ 'is-split': editorMode === 'split' }">
            <textarea
              ref="bodyTextareaRef"
              v-model="form.bodyHtml"
              class="richtext-source"
              rows="18"
              placeholder="在此输入或通过上方工具栏插入 HTML 正文。正文内图片请点击「插入素材库图片」，自动生成受控的 data-media-id 标签..."
            ></textarea>

            <div v-if="editorMode === 'split'" class="richtext-preview">
              <div class="richtext-preview__head">前台排版实时预览（复用 ArticleBody）</div>
              <div class="richtext-preview__body">
                <ArticleBody :html="previewHtml" :show-toc="false" :show-progress="false" :show-tools="false" />
              </div>
            </div>
          </div>

          <el-divider content-position="left">独立 SEO 设置与搜索预览</el-divider>
          <div class="form-grid-2">
            <el-form-item label="SEO 标题">
              <el-input v-model="form.seoTitle" maxlength="80" placeholder="选填，未填时默认使用内容标题" />
            </el-form-item>
            <el-form-item label="SEO 描述">
              <el-input v-model="form.seoDescription" maxlength="200" placeholder="选填，未填时默认使用摘要" />
            </el-form-item>
          </div>
        </el-form>
      </el-card>
    </template>

    <MediaPicker v-model="pickerOpen" :kind="pickerKind" @select="onMediaPicked" />
  </div>
</template>

<style scoped>
.editor-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.top-bar {
  position: sticky;
  top: 0;
  z-index: 15;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 18px;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(8px);
  border: 1px solid var(--a-border);
  border-radius: 10px;
}

.top-bar__left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.top-bar__title {
  font-size: 15px;
}

.top-bar__actions {
  display: flex;
  gap: 8px;
}

.form-grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

@media (max-width: 900px) {
  .form-grid-2 {
    grid-template-columns: 1fr;
  }
}

.media-pick {
  display: flex;
  align-items: center;
  gap: 10px;
}

.media-pick img,
.media-pick__empty {
  width: 84px;
  height: 52px;
  border-radius: 6px;
  border: 1px solid var(--a-border);
  object-fit: cover;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: var(--a-text-muted);
  background: #f6f8fa;
}

.sub-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.richtext-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  flex-wrap: wrap;
  padding: 10px 12px;
  background: #f8f9fc;
  border: 1px solid var(--a-border);
  border-radius: 8px 8px 0 0;
}

.richtext-toolbar__btns {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.richtext-workbench {
  display: grid;
  grid-template-columns: 1fr;
  border: 1px solid var(--a-border);
  border-top: none;
  border-radius: 0 0 8px 8px;
  overflow: hidden;
}

.richtext-workbench.is-split {
  grid-template-columns: 1fr 1fr;
}

@media (max-width: 960px) {
  .richtext-workbench.is-split {
    grid-template-columns: 1fr;
  }
}

.richtext-source {
  width: 100%;
  border: none;
  padding: 14px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 13.5px;
  line-height: 1.65;
  outline: none;
  resize: vertical;
  background: #0f172a;
  color: #e2e8f0;
}

.richtext-preview {
  background: #ffffff;
  border-left: 1px solid var(--a-border);
  display: flex;
  flex-direction: column;
  max-height: 520px;
}

.richtext-preview__head {
  padding: 8px 14px;
  font-size: 12px;
  font-weight: 600;
  color: #5b4be8;
  background: #f5f3ff;
  border-bottom: 1px solid var(--a-border);
}

.richtext-preview__body {
  padding: 16px 20px;
  overflow-y: auto;
  flex: 1;
}
</style>
```
