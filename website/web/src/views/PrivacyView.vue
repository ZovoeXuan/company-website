<script setup lang="ts">
import { onMounted, ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getPrivacy } from '@/api/public'
import { usePageMeta } from '@/composables/usePageMeta'
import type { PrivacyPage } from '@/data/types'

const data = ref<PrivacyPage | null>(null)
const failed = ref(false)

usePageMeta(() => ({
  title: '隐私说明',
  description: '说明本网站收集的信息范围、用途、保存期限与您的权利。',
}))

onMounted(async () => {
  try {
    data.value = await getPrivacy()
  } catch {
    failed.value = true
  }
})
</script>

<template>
  <div>
    <PageHeader
      title="隐私说明"
      :crumbs="[{ label: '首页', path: '/' }, { label: '隐私说明' }]"
    />

    <section v-if="data" class="section">
      <div class="container narrow">
        <p class="updated">最近更新：<time :datetime="data.updatedAt">{{ data.updatedAt }}</time></p>
        <div class="prose" v-html="data.bodyHtml"></div>
      </div>
    </section>

    <div v-else-if="failed" class="container state">
      <EmptyState title="内容加载失败" description="请稍后刷新页面重试。" />
    </div>
  </div>
</template>

<style scoped>
.narrow {
  max-width: 820px;
}

.updated {
  font-size: 13px;
  color: var(--c-text-subtle);
  margin-bottom: 24px;
}

.state {
  padding: 72px 24px;
}
</style>
