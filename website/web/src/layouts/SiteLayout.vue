<script setup lang="ts">
import { onMounted, ref } from 'vue'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import { getSite } from '@/api/public'
import type { SiteConfig } from '@/data/types'

const config = ref<SiteConfig | null>(null)
const failed = ref(false)

onMounted(async () => {
  try {
    config.value = await getSite()
  } catch {
    failed.value = true
  }
})
</script>

<template>
  <div class="site">
    <a class="skip-link" href="#main">跳到主要内容</a>

    <SiteHeader v-if="config" :site-name="config.siteName" :nav="config.nav" />

    <main id="main" class="site__main" tabindex="-1">
      <div v-if="failed" class="container notice" role="status">
        站点信息加载失败，请稍后刷新重试。
      </div>
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
  padding-top: 16px;
  color: #a33;
  font-size: 14px;
}
</style>
