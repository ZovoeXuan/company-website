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
