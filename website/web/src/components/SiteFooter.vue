<script setup lang="ts">
import { computed } from 'vue'
import type { SiteConfig } from '@/data/types'

const props = defineProps<{ config: SiteConfig }>()

/** 页脚链接由后台「导航与页脚链接」维护；未配置时回退到主导航，避免空列。 */
const quickLinks = computed(() => {
  const links = props.config.footer?.links ?? []
  if (links.length > 0) return links
  return props.config.nav.slice(1, 5)
})
</script>

<template>
  <footer class="footer">
    <div class="container footer__top">
      <div class="footer__brand">
        <p class="footer__name">{{ config.siteName }}</p>
        <p class="footer__slogan">{{ config.slogan }}</p>
      </div>

      <nav class="footer__col" aria-label="页脚导航">
        <p class="footer__title">快速入口</p>
        <ul class="footer__list">
          <li v-for="item in quickLinks" :key="item.path">
            <RouterLink :to="item.path" class="footer__link">{{ item.label }}</RouterLink>
          </li>
        </ul>
      </nav>

      <div class="footer__col">
        <p class="footer__title">联系方式</p>
        <ul class="footer__list">
          <li><a class="footer__link" :href="`tel:${config.contact.phone.replace(/\s/g, '')}`">{{ config.contact.phone }}</a></li>
          <li><a class="footer__link" :href="`mailto:${config.contact.email}`">{{ config.contact.email }}</a></li>
          <li>{{ config.contact.address }}</li>
          <li>{{ config.contact.hours }}</li>
        </ul>
      </div>
    </div>

    <div class="container footer__bottom">
      <span>{{ config.footer.copyright }}</span>
      <span>{{ config.footer.icp }}</span>
      <RouterLink to="/privacy" class="footer__link">隐私说明</RouterLink>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  border-top: 1px solid var(--c-border);
  background: var(--c-surface);
  margin-top: 96px;
}

.footer__top {
  display: grid;
  grid-template-columns: 1.4fr 1fr 1fr;
  gap: 40px;
  padding-top: 56px;
  padding-bottom: 40px;
}

.footer__name {
  font-size: 18px;
  font-weight: 500;
}

.footer__slogan {
  margin-top: 10px;
  font-size: 14px;
  color: var(--c-text-muted);
  max-width: 320px;
  line-height: 1.8;
}

.footer__title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 14px;
}

.footer__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  font-size: 14px;
  color: var(--c-text-muted);
}

.footer__link {
  color: var(--c-text-muted);
}

.footer__link:hover {
  color: var(--c-brand);
  text-decoration: underline;
}

.footer__bottom {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  padding-top: 20px;
  padding-bottom: 28px;
  border-top: 1px solid var(--c-border);
  font-size: 13px;
  color: var(--c-text-subtle);
}

@media (max-width: 860px) {
  .footer__top {
    grid-template-columns: 1fr 1fr;
    gap: 32px;
    padding-top: 40px;
  }
  .footer__brand {
    grid-column: 1 / -1;
  }
  .footer {
    margin-top: 64px;
  }
}
</style>
