<script setup lang="ts">
defineProps<{ title: string; subtitle: string }>()
</script>

<template>
  <section class="placeholder">
    <div class="container">
      <p class="tag">页面骨架 · 阶段 1</p>
      <h1>{{ title }}</h1>
      <p class="desc">{{ subtitle }}</p>
      <div class="empty">
        <p class="empty-title">该页面的数据接口尚未接入</p>
        <p class="empty-desc">
          内容将来自 <code>/api/public</code> 的发布快照。后端接口与后台编辑页完成后，此处由真实内容替换。
        </p>
      </div>
    </div>
  </section>
</template>

<style scoped>
.placeholder {
  padding: 56px 0 24px;
}

.tag {
  display: inline-block;
  font-size: 12px;
  color: var(--c-brand);
  background: var(--c-brand-soft);
  border-radius: 20px;
  padding: 3px 10px;
  margin-bottom: 14px;
}

h1 {
  font-size: 28px;
  font-weight: 500;
  margin-bottom: 10px;
}

.desc {
  color: var(--c-text-muted);
  font-size: 14px;
  line-height: 1.7;
  max-width: 640px;
}

.empty {
  margin-top: 32px;
  border: 1px dashed var(--c-border);
  border-radius: var(--radius);
  padding: 28px;
  background: var(--c-surface);
}

.empty-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 6px;
}

.empty-desc {
  font-size: 13px;
  color: var(--c-text-muted);
  line-height: 1.7;
}

code {
  background: #fff;
  border: 1px solid var(--c-border);
  border-radius: 4px;
  padding: 1px 5px;
  font-size: 12px;
}
</style>
