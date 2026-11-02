<script setup lang="ts">
import { onMounted, ref } from 'vue'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import { ApiError } from '@/api/http'
import { getSite } from '@/api/public'
import type { SiteConfig } from '@/data/types'

const config = ref<SiteConfig | null>(null)
const notice = ref('')

onMounted(async () => {
  try {
    config.value = await getSite()
  } catch (error) {
    notice.value = error instanceof ApiError && error.code === 'NOT_FOUND'
      ? '站点内容尚未发布，请管理员在后台完成「全站设置」并发布。'
      : '站点信息加载失败，请稍后刷新重试。'
  }
})
</script>

<template>
  <div class="site">
    <a class="skip-link" href="#main">跳到主要内容</a>

    <SiteHeader
      v-if="config"
      :site-name="config.siteName"
      :nav="config.nav"
      :logo-url="config.logoUrl"
      :logo-alt="config.logoAlt"
    />

    <main id="main" class="site__main" tabindex="-1">
      <div v-if="notice" class="container notice" role="status">{{ notice }}</div>
      <RouterView />
    </main>

    <SiteFooter v-if="config" :config="config" />
  </div>
</template>

<style scoped>
.site {
  display: flex;
  flex-direction: column;
  min-height: 100dvh;
}

.site__main {
  flex: 1;
  outline: none;
}

.notice {
  margin-top: 16px;
  padding: 12px 16px;
  border: 1px solid #f0e0bd;
  background: #fdf6e3;
  border-radius: 8px;
  color: #8a6d1f;
  font-size: 14px;
  line-height: 1.7;
}
</style>
