<script setup lang="ts">
import type { SiteConfig } from '@/data/types'

defineProps<{
  site?: SiteConfig | null
}>()
</script>

<template>
  <!-- 1:1 对齐四段参考视频：全站无右下角遮挡浮窗，统一通过顶部「小鑫AI」与「产品试用」触发 -->
  <span class="floating-actions-empty" aria-hidden="true"></span>
</template>

<style scoped>
.floating-actions-empty {
  display: none;
}
</style>
