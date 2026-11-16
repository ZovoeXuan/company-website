<script setup lang="ts">
import { onMounted, ref } from 'vue'
import ArticleBody from '../../../shared/ArticleBody.vue'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getPrivacy } from '@/api/public'
import { usePageMeta } from '@/composables/usePageMeta'
import { useRealtimeSync } from '@/composables/useRealtimeSync'
import type { PrivacyPage } from '@/data/types'

const data = ref<PrivacyPage | null>(null)
const loading = ref(true)
const failed = ref(false)

usePageMeta(() => ({
  title: '隐私说明',
  description: '说明本网站收集的信息范围、用途、保存期限与您的权利。',
}))

async function loadPrivacy() {
  loading.value = true
  failed.value = false
  try {
    data.value = await getPrivacy()
  } catch {
    failed.value = true
  } finally {
    loading.value = false
  }
}

useRealtimeSync(['siteConfigVersion'], () => {
  void loadPrivacy()
})

onMounted(loadPrivacy)
</script>

<template>
  <div>
    <PageHeader
      title="隐私说明"
      eyebrow="LEGAL"
      :crumbs="[{ label: '首页', path: '/' }, { label: '隐私说明' }]"
    />

    <div v-if="loading" class="container state" aria-busy="true">
      <div class="skeleton"></div>
    </div>

    <div v-else-if="failed" class="container state">
      <EmptyState title="内容加载失败" description="请稍后刷新页面重试。">
        <button type="button" class="btn btn--ghost" @click="loadPrivacy">重新加载</button>
      </EmptyState>
    </div>

    <section v-else-if="data && data.bodyHtml" class="section">
      <div class="container narrow">
        <p v-if="data.updatedAt" class="updated">
          最近更新：<time :datetime="data.updatedAt">{{ data.updatedAt }}</time>
        </p>
        <ArticleBody :html="data.bodyHtml" title="隐私说明" :show-toc="false" />
      </div>
    </section>

    <div v-else class="container state">
      <EmptyState title="暂无隐私政策正文" description="管理员尚未发布隐私政策说明。" />
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
