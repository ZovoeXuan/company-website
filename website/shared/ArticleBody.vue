<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = withDefaults(
  defineProps<{
    html?: string | null
    showToc?: boolean
    title?: string
  }>(),
  {
    html: '',
    showToc: true,
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
const lightboxUrl = ref('')
const lightboxAlt = ref('')

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
        btn.textContent = '已复制'
        setTimeout(() => {
          btn.textContent = '复制代码'
        }, 1800)
      } catch {
        btn.textContent = '复制失败'
      }
    })
    pre.appendChild(btn)
  })

  // 3. 为正文受控图片绑定点击放大预览
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
    <div class="cms-article-layout" :class="{ 'has-toc': showToc && tocItems.length >= 2 }">
      <div class="cms-article">
        <!-- eslint-disable-next-line vue/no-v-html -->
        <div
          v-if="html"
          ref="articleRef"
          class="cms-article__prose cms-prose"
          v-html="html"
        ></div>
        <p v-else class="cms-empty">暂无正文内容</p>
      </div>

      <!-- 自动提取的右侧悬浮目录 (TOC) -->
      <aside v-if="showToc && tocItems.length >= 2" class="cms-toc" aria-label="文章目录">
        <div class="cms-toc__card">
          <div class="cms-toc__head">
            <span class="cms-toc__title">本文目录</span>
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
    </Teleport>
  </div>
</template>

<style scoped>
.cms-article-shell {
  position: relative;
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

.cms-prose,
.cms-article__prose {
  font-size: 16px;
  line-height: 1.85;
  color: #1e293b;
  word-break: break-word;
  overflow-wrap: anywhere;
}

.cms-prose :deep(> * + *),
.cms-article__prose :deep(> * + *) {
  margin-top: 1.15em;
}

.cms-prose :deep(h2),
.cms-article__prose :deep(h2) {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.35;
  margin-top: 2em;
  margin-bottom: 0.7em;
  padding-bottom: 0.4em;
  border-bottom: 1px solid #e2e8f0;
  color: #0f172a;
  letter-spacing: -0.015em;
  scroll-margin-top: 96px;
}

.cms-prose :deep(h3),
.cms-article__prose :deep(h3) {
  font-size: 18px;
  font-weight: 600;
  line-height: 1.45;
  margin-top: 1.6em;
  margin-bottom: 0.5em;
  color: #0f172a;
  scroll-margin-top: 96px;
}

.cms-prose :deep(p),
.cms-article__prose :deep(p) {
  margin: 0 0 1.1em;
  color: #334155;
}

.cms-prose :deep(ul),
.cms-prose :deep(ol),
.cms-article__prose :deep(ul),
.cms-article__prose :deep(ol) {
  margin: 0.9em 0 1.2em;
  padding-left: 1.5em;
}

.cms-prose :deep(li + li),
.cms-article__prose :deep(li + li) {
  margin-top: 0.4em;
}

.cms-prose :deep(blockquote),
.cms-article__prose :deep(blockquote) {
  margin: 1.5em 0;
  padding: 14px 20px;
  border-left: 4px solid #2563eb;
  background: #f8fafc;
  border-radius: 0 8px 8px 0;
  color: #1e293b;
  font-style: normal;
}

.cms-prose :deep(img),
.cms-article__prose :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 1.5em auto;
  display: block;
  border: 1px solid #e2e8f0;
}

.cms-prose :deep(pre),
.cms-article__prose :deep(pre) {
  background: #0f172a;
  color: #e2e8f0;
  padding: 16px 18px;
  border-radius: 8px;
  overflow-x: auto;
  font-size: 13.5px;
  line-height: 1.65;
  margin: 1.4em 0;
}

.cms-prose :deep(.cms-code-copy),
.cms-article__prose :deep(.cms-code-copy) {
  position: absolute;
  top: 10px;
  right: 12px;
  padding: 4px 10px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.08);
  color: #cbd5e1;
  font-size: 12px;
  cursor: pointer;
}

.cms-prose :deep(.cms-code-copy:hover),
.cms-article__prose :deep(.cms-code-copy:hover) {
  background: #2563eb;
  color: #ffffff;
  border-color: #2563eb;
}

.cms-prose :deep(table),
.cms-article__prose :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1.5em 0;
  font-size: 14px;
  display: block;
  overflow-x: auto;
}

.cms-prose :deep(th),
.cms-prose :deep(td),
.cms-article__prose :deep(th),
.cms-article__prose :deep(td) {
  border: 1px solid #e2e8f0;
  padding: 10px 14px;
  text-align: left;
}

.cms-prose :deep(th),
.cms-article__prose :deep(th) {
  background: #f8fafc;
  font-weight: 600;
}

.cms-empty {
  color: #64748b;
  font-size: 14px;
  padding: 20px 0;
}

.cms-toc {
  position: sticky;
  top: 96px;
}

.cms-toc__card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 16px;
}

.cms-toc__head {
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e2e8f0;
}

.cms-toc__title {
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
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
  color: #475569;
  cursor: pointer;
}

.cms-toc__link:hover {
  color: #0f172a;
  background: #f1f5f9;
}

.cms-toc__item.is-active .cms-toc__link {
  color: #2563eb;
  font-weight: 600;
  background: #eff6ff;
}

.cms-lightbox {
  position: fixed;
  inset: 0;
  z-index: 120;
  background: rgba(15, 23, 42, 0.85);
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
  border: 1px solid rgba(255, 255, 255, 0.25);
  background: rgba(255, 255, 255, 0.12);
  color: #ffffff;
  font-size: 18px;
  cursor: pointer;
}

.cms-lightbox__img {
  max-width: min(1100px, 92vw);
  max-height: 82vh;
  border-radius: 8px;
  object-fit: contain;
}

.cms-lightbox__caption {
  margin-top: 12px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
}
</style>
