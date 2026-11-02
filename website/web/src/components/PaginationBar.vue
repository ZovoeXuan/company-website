<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  page: number
  pageSize: number
  total: number
}>()

const emit = defineEmits<{ change: [page: number] }>()

const pageCount = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))

const pages = computed(() => {
  const count = pageCount.value
  const current = props.page
  const windowed: number[] = []
  const start = Math.max(1, Math.min(current - 2, count - 4))
  const end = Math.min(count, start + 4)
  for (let i = start; i <= end; i += 1) {
    windowed.push(i)
  }
  return windowed
})

function go(target: number) {
  if (target < 1 || target > pageCount.value || target === props.page) return
  emit('change', target)
}
</script>

<template>
  <nav v-if="pageCount > 1" class="pager" aria-label="分页">
    <button type="button" class="pager__btn" :disabled="page <= 1" @click="go(page - 1)">上一页</button>

    <ul class="pager__list">
      <li v-for="num in pages" :key="num">
        <button
          type="button"
          class="pager__btn"
          :class="{ 'is-current': num === page }"
          :aria-current="num === page ? 'page' : undefined"
          @click="go(num)"
        >
          {{ num }}
        </button>
      </li>
    </ul>

    <button type="button" class="pager__btn" :disabled="page >= pageCount" @click="go(page + 1)">下一页</button>
    <span class="pager__total">共 {{ total }} 条</span>
  </nav>
</template>

<style scoped>
.pager {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 40px;
}

.pager__list {
  display: flex;
  gap: 6px;
  list-style: none;
  margin: 0;
  padding: 0;
}

.pager__btn {
  min-width: 40px;
  height: 40px;
  padding: 0 12px;
  border: 1px solid var(--c-border);
  background: #fff;
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--c-text);
  cursor: pointer;
}

.pager__btn:hover:not(:disabled) {
  border-color: var(--c-brand);
  color: var(--c-brand);
}

.pager__btn:disabled {
  color: var(--c-text-subtle);
  cursor: not-allowed;
  background: var(--c-surface);
}

.pager__btn.is-current {
  background: var(--c-brand);
  border-color: var(--c-brand);
  color: #fff;
}

.pager__total {
  margin-left: 8px;
  font-size: 13px;
  color: var(--c-text-muted);
}
</style>
