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
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 56px;
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
  border: 1px solid transparent;
  background: transparent;
  border-radius: 999px;
  font-size: 14px;
  color: var(--c-text-muted);
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.pager__btn:hover:not(:disabled) {
  background: var(--c-surface);
  color: var(--c-text);
}

.pager__btn:disabled {
  color: var(--c-text-subtle);
  cursor: not-allowed;
}

.pager__btn.is-current {
  background: var(--c-text);
  color: #fff;
}

.pager__total {
  margin-left: 12px;
  font-size: 12px;
  color: var(--c-text-subtle);
}
</style>
