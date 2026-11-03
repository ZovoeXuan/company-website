<script setup lang="ts">
/**
 * 详情正文的共享渲染组件。
 *
 * 官网详情页与后台草稿预览都使用它，保证「预览看到的就是发布后的样子」，
 * 避免维护两套排版模板。样式自带、类名以 cms- 前缀隔离，不依赖任何一方的
 * 主题变量，因此在官网与后台两种配色下都能正确显示。
 */
withDefaults(
  defineProps<{
    cover?: { url: string; alt?: string; width?: number; height?: number } | null
    capabilities?: string[]
    bodyHtml?: string
    /** 正文为空时是否隐藏（预览空草稿时避免出现一块空白） */
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
      <img
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

    <div v-if="bodyHtml" class="cms-prose" v-html="bodyHtml"></div>
    <p v-else-if="!hideEmptyBody" class="cms-article__empty">尚未填写正文</p>

    <slot />
  </article>
</template>

<style>
.cms-article {
  min-width: 0;
}

.cms-article__cover {
  margin: 0 0 28px;
}

.cms-article__cover img {
  width: 100%;
  /* 必须 height:auto，否则 HTML 的 height 属性会覆盖 aspect-ratio，封面被拉成竖图 */
  height: auto;
  aspect-ratio: 16 / 9;
  object-fit: cover;
  border-radius: 4px;
  background: #eef1f5;
  display: block;
}

.cms-article__caps {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0;
  list-style: none;
  margin: 0 0 32px;
  padding: 0;
  font-size: 14px;
  color: #5b6472;
}

.cms-article__cap {
  position: relative;
  padding: 0 16px;
}

.cms-article__cap:first-child {
  padding-left: 0;
}

.cms-article__cap + .cms-article__cap::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  width: 1px;
  height: 12px;
  margin-top: -6px;
  background: #d5dbe3;
}

.cms-article__empty {
  color: #8b949e;
  font-size: 14px;
}

.cms-prose {
  font-size: 16px;
  line-height: 1.95;
  color: #2b3140;
  overflow-wrap: anywhere;
}

.cms-prose > * + * {
  margin-top: 18px;
}

.cms-prose h2 {
  font-size: 24px;
  font-weight: 500;
  line-height: 1.45;
  margin-top: 44px;
  color: #1a2433;
}

.cms-prose h3 {
  font-size: 19px;
  font-weight: 500;
  margin-top: 32px;
  color: #1a2433;
}

.cms-prose ul,
.cms-prose ol {
  padding-left: 1.4em;
}

.cms-prose li + li {
  margin-top: 8px;
}

.cms-prose a {
  color: #2f6bf0;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.cms-prose img {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
}

.cms-prose table {
  width: 100%;
  border-collapse: collapse;
  display: block;
  overflow-x: auto;
}

.cms-prose th,
.cms-prose td {
  border: 1px solid #e2e7ee;
  padding: 8px 12px;
  text-align: left;
}

.cms-prose blockquote {
  margin: 0;
  padding-left: 16px;
  border-left: 3px solid #d5dbe3;
  color: #4a5361;
}
</style>
