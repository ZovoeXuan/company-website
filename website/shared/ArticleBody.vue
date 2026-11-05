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
const currentUrl = computed(() => (typeof window !== 'undefined' ? window.location.href : ''))

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
              <p class="cms-poster__url">{{ currentUrl }}</p>
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
