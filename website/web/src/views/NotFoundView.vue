<script setup lang="ts">
import { usePageMeta } from '@/composables/usePageMeta'

usePageMeta(() => ({
  title: '页面不存在',
  description: '您访问的页面不存在或已下线。',
  noindex: true,
}))
</script>

<template>
  <section class="notfound">
    <div class="container">
      <p class="code">404</p>
      <h1 class="title">页面不存在</h1>
      <p class="desc">链接可能已失效，或者内容已下线。可以从下面的入口重新开始。</p>
      <div class="actions">
        <RouterLink to="/" class="btn btn--primary">返回首页</RouterLink>
        <RouterLink to="/products" class="btn btn--ghost">查看产品</RouterLink>
        <RouterLink to="/contact" class="btn btn--ghost">联系我们</RouterLink>
      </div>
    </div>
  </section>
</template>

<style scoped>
.notfound {
  padding: 96px 0 64px;
}

.code {
  font-size: 64px;
  font-weight: 500;
  color: var(--c-brand);
  line-height: 1;
  letter-spacing: -0.02em;
}

.title {
  margin-top: 16px;
  font-size: 28px;
  font-weight: 500;
}

.desc {
  margin-top: 12px;
  font-size: 15px;
  color: var(--c-text-muted);
  max-width: 520px;
}

.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 28px;
}
</style>
