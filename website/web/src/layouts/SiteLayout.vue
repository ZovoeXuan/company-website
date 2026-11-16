<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import ConsultDialog from '@/components/ConsultDialog.vue'
import VideoModal from '@/components/VideoModal.vue'
import { ApiError } from '@/api/http'
import { getCachedSiteStrict } from '@/composables/usePageHero'
import { useRealtimeSync } from '@/composables/useRealtimeSync'
import type { SiteConfig } from '@/data/types'

const route = useRoute()
const config = ref<SiteConfig | null>(null)
const notice = ref('')

async function loadSite(force = false) {
  notice.value = ''
  try {
    config.value = await getCachedSiteStrict(force)
  } catch (error) {
    notice.value =
      error instanceof ApiError && error.code === 'NOT_FOUND'
        ? '站点内容尚未发布，请管理员在后台完成「全站设置」并发布。'
        : '站点信息加载失败，请稍后刷新重试。'
  }
}

useRealtimeSync(['siteConfigVersion'], () => {
  void loadSite(true)
})

onMounted(() => {
  void loadSite(false)
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
      :mega-columns="config.megaColumns"
      :mega-cta="config.megaCta"
    />

    <main id="main" class="site__main" tabindex="-1">
      <div v-if="notice" class="container notice" role="status">
        <span>{{ notice }}</span>
        <button type="button" class="notice__retry" @click="loadSite(true)">重试</button>
      </div>
      <RouterView v-slot="{ Component }">
        <Transition name="page" mode="out-in">
          <component :is="Component" :key="route.path" />
        </Transition>
      </RouterView>
    </main>

    <SiteFooter v-if="config" :config="config" />

    <!-- 居中预约产品展示弹窗 + 视频播放弹窗 -->
    <ConsultDialog />
    <VideoModal />
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.notice__retry {
  padding: 4px 12px;
  border-radius: 6px;
  border: 1px solid #d8be82;
  background: #fff;
  color: #8a6d1f;
  font-size: 12.5px;
  cursor: pointer;
}
</style>
