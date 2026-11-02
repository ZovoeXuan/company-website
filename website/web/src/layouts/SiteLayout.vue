<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ApiError } from '@/api/http'
import { fetchSiteConfig, type SiteConfigView } from '@/api/site'

const config = ref<SiteConfigView | null>(null)
const state = ref<'loading' | 'ready' | 'unavailable'>('loading')
const reason = ref('')

const fallbackNav = [
  { label: '首页', path: '/' },
  { label: '产品', path: '/products' },
  { label: '解决方案', path: '/solutions' },
  { label: '客户案例', path: '/cases' },
  { label: '新闻', path: '/news' },
  { label: '关于我们', path: '/about' },
]

onMounted(async () => {
  try {
    config.value = await fetchSiteConfig()
    state.value = 'ready'
  } catch (error) {
    state.value = 'unavailable'
    reason.value = error instanceof ApiError && error.code === 'NOT_FOUND'
      ? '站点内容尚未发布'
      : '暂时无法连接服务'
  }
})

const navItems = () => (config.value?.nav?.length ? config.value.nav : fallbackNav)
</script>

<template>
  <div class="site">
    <header class="header">
      <div class="container header-inner">
        <RouterLink to="/" class="brand">
          <span class="logo">
            <img v-if="config?.logoUrl" :src="config.logoUrl" alt="" />
            <span v-else>示</span>
          </span>
          <span class="brand-name">{{ config?.siteName || '示例科技' }}</span>
        </RouterLink>

        <nav class="nav">
          <RouterLink v-for="item in navItems()" :key="item.path" :to="item.path" class="nav-link">
            {{ item.label }}
          </RouterLink>
        </nav>

        <RouterLink to="/contact" class="cta">咨询我们</RouterLink>
      </div>
    </header>

    <div v-if="state === 'unavailable'" class="notice">
      <div class="container">
        {{ reason }}，页面结构已就绪，待后台完成初始化并发布后展示正式内容。
      </div>
    </div>

    <main class="main">
      <RouterView />
    </main>

    <footer class="footer">
      <div class="container footer-inner">
        <div class="footer-brand">
          <div class="brand-name">{{ config?.siteName || '示例科技' }}</div>
          <p class="footer-desc">此处文案由后台「站点设置」维护，正式上线前替换为公司实际信息。</p>
        </div>
        <div class="footer-col">
          <p class="footer-title">联系方式</p>
          <p>{{ config?.contact?.phone || '电话待配置' }}</p>
          <p>{{ config?.contact?.email || '邮箱待配置' }}</p>
          <p>{{ config?.contact?.address || '地址待配置' }}</p>
        </div>
        <div class="footer-col">
          <p class="footer-title">快速入口</p>
          <RouterLink v-for="item in navItems().slice(1, 5)" :key="item.path" :to="item.path">
            {{ item.label }}
          </RouterLink>
        </div>
      </div>
      <div class="container footer-bottom">
        <span>{{ config?.footer?.copyright || '© 2026 示例科技有限公司' }}</span>
        <span>{{ config?.footer?.icp || '备案号待配置' }}</span>
        <RouterLink to="/privacy">隐私说明</RouterLink>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.site {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.header {
  position: sticky;
  top: 0;
  z-index: 20;
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: saturate(180%) blur(8px);
  border-bottom: 1px solid var(--c-border);
}

.header-inner {
  display: flex;
  align-items: center;
  gap: 24px;
  height: 64px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: none;
}

.logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 7px;
  background: var(--c-brand-soft);
  color: var(--c-brand);
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
}

.logo img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.brand-name {
  font-size: 15px;
  font-weight: 500;
}

.nav {
  display: flex;
  gap: 22px;
  margin-left: 8px;
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.nav-link {
  font-size: 14px;
  color: var(--c-text-muted);
  white-space: nowrap;
  padding: 6px 0;
  border-bottom: 2px solid transparent;
}

.nav-link.router-link-active {
  color: var(--c-text);
  border-bottom-color: var(--c-brand);
}

.cta {
  flex: none;
  font-size: 13px;
  padding: 8px 16px;
  border-radius: 8px;
  border: 1px solid var(--c-brand);
  color: var(--c-brand);
}

.notice {
  background: #fdf6e3;
  border-bottom: 1px solid #f0e0bd;
  color: #8a6d1f;
  font-size: 13px;
  padding: 10px 0;
}

.main {
  flex: 1;
}

.footer {
  border-top: 1px solid var(--c-border);
  background: var(--c-surface);
  margin-top: 64px;
  padding-top: 40px;
}

.footer-inner {
  display: flex;
  gap: 48px;
  flex-wrap: wrap;
  padding-bottom: 28px;
}

.footer-brand {
  flex: 1;
  min-width: 240px;
}

.footer-desc {
  margin-top: 10px;
  font-size: 13px;
  color: var(--c-text-muted);
  line-height: 1.7;
  max-width: 360px;
}

.footer-col {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: var(--c-text-muted);
  min-width: 160px;
}

.footer-title {
  color: var(--c-text);
  font-weight: 500;
  margin-bottom: 4px;
}

.footer-bottom {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  padding: 16px 24px 24px;
  font-size: 12px;
  color: var(--c-text-muted);
  border-top: 1px solid var(--c-border);
}

@media (max-width: 860px) {
  .nav {
    display: none;
  }
  .footer {
    margin-top: 40px;
  }
}
</style>
