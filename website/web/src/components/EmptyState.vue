<script setup lang="ts">
defineProps<{ title: string; description?: string }>()
</script>

<template>
  <div class="empty">
    <p class="empty__title">{{ title }}</p>
    <p v-if="description" class="empty__desc">{{ description }}</p>
    <slot />
  </div>
</template>

<style scoped>
.empty {
  border: 1px dashed var(--c-border-strong);
  border-radius: var(--radius);
  background: var(--c-surface);
  padding: 48px 24px;
  text-align: center;
}

.empty__title {
  font-size: 16px;
  font-weight: 500;
}

.empty__desc {
  margin-top: 8px;
  font-size: 14px;
  color: var(--c-text-muted);
}
</style>
