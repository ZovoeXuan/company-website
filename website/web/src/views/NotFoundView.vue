<script setup lang="ts">
import { usePageMeta } from '@/composables/usePageMeta'

usePageMeta(() => ({
  title: '页面不存在',
  description: '您访问的页面不存在或已下线。',
  noindex: true,
}))
</script>

<template>
  <section class="notfound on-dark">
    <div class="notfound__glow" aria-hidden="true"></div>
    <div class="container">
      <p class="kicker">404 / NOT FOUND</p>
      <p class="code" aria-hidden="true">404</p>
      <h1 class="title">页面不存在</h1>
      <p class="desc">链接可能已失效，或者内容已下线。可以从下面的入口重新开始。</p>
      <div class="actions">
        <RouterLink to="/" class="btn btn--outline-light">返回首页</RouterLink>
        <RouterLink to="/products" class="btn btn--outline-light">查看产品</RouterLink>
        <RouterLink to="/contact" class="btn btn--outline-light">联系我们</RouterLink>
      </div>
    </div>
  </section>
</template>

<style scoped>
.notfound {
  position: relative;
  overflow: hidden;
  background: var(--c-navy);
  color: #fff;
  padding: 160px 0 140px;
  min-height: 70vh;
}

.notfound__glow {
  position: absolute;
  width: 900px;
  height: 700px;
  left: 10%;
  top: -20%;
  background: radial-gradient(closest-side, rgba(91, 75, 232, 0.4), transparent 70%);
  filter: blur(20px);
}

.container {
  position: relative;
  z-index: 1;
}

.code {
  font-size: clamp(100px, 20vw, 240px);
  line-height: 0.9;
  font-weight: 600;
  letter-spacing: -0.08em;
  color: rgba(255, 255, 255, 0.12);
  margin-top: 8px;
}

.title {
  margin-top: -20px;
  font-size: clamp(30px, 4vw, 44px);
  color: #fff;
  letter-spacing: -0.03em;
}

.desc {
  margin-top: 14px;
  font-size: 15px;
  color: var(--c-on-dark-muted);
  max-width: 520px;
  line-height: 1.8;
}

.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 36px;
}
</style>
