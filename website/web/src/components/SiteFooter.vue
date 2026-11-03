<script setup lang="ts">
import { computed } from 'vue'
import { openConsult } from '@/composables/consult'
import type { SiteConfig } from '@/data/types'

const props = defineProps<{ config: SiteConfig }>()

/** 页脚分组由后台「全站设置」维护；未配置时回退到主导航前几项，避免空列 */
const groups = computed(() => {
  const configured = props.config.footer?.groups ?? []
  if (configured.length > 0) return configured
  const fallback = props.config.footer?.links ?? []
  if (fallback.length > 0) return [{ title: '快速入口', links: fallback }]
  return [{ title: '快速入口', links: props.config.nav.slice(1, 5) }]
})

function telHref(phone: string) {
  return `tel:${phone.replace(/[\s-]/g, '')}`
}
</script>

<template>
  <footer class="footer">
    <div class="container footer__top">
      <div class="footer__brand">
        <p class="footer__name">
          <img v-if="config.logoUrl" :src="config.logoUrl" :alt="config.logoAlt || config.siteName" class="footer__logo" />
          <span v-else>{{ config.siteName }}</span>
        </p>
        <p v-if="config.slogan" class="footer__slogan">{{ config.slogan }}</p>
      </div>

      <nav v-for="group in groups" :key="group.title" class="footer__col" :aria-label="group.title">
        <p class="footer__title">{{ group.title }}</p>
        <ul class="footer__list">
          <li v-for="item in group.links" :key="item.label + (item.path || item.url || '')">
            <button
              v-if="item.linkType === 'CONSULT'"
              type="button"
              class="footer__link footer__link--button"
              @click="openConsult({ sourcePosition: 'FOOTER' })"
            >
              {{ item.label }}
            </button>
            <a
              v-else-if="item.url"
              :href="item.url"
              class="footer__link"
              :target="item.newWindow ? '_blank' : undefined"
              :rel="item.newWindow ? 'noopener noreferrer' : undefined"
            >
              {{ item.label }}
            </a>
            <RouterLink v-else-if="item.path" :to="item.path" class="footer__link">{{ item.label }}</RouterLink>
            <span v-else class="footer__link">{{ item.label }}</span>
          </li>
        </ul>
      </nav>

      <div class="footer__col">
        <p class="footer__title">联系方式</p>
        <ul class="footer__list">
          <li v-if="config.contact.phone">
            <a class="footer__link" :href="telHref(config.contact.phone)">{{ config.contact.phone }}</a>
          </li>
          <li v-if="config.contact.email">
            <a class="footer__link" :href="`mailto:${config.contact.email}`">{{ config.contact.email }}</a>
          </li>
          <li v-if="config.contact.address">{{ config.contact.address }}</li>
          <li v-if="config.contact.hours">{{ config.contact.hours }}</li>
        </ul>
      </div>
    </div>

    <div class="container footer__bottom">
      <span>{{ config.footer.copyright }}</span>
      <a v-if="config.footer.icp && config.footer.icpUrl" :href="config.footer.icpUrl" class="footer__link" target="_blank" rel="noopener noreferrer">
        {{ config.footer.icp }}
      </a>
      <span v-else-if="config.footer.icp">{{ config.footer.icp }}</span>
      <a v-if="config.footer.policeText && config.footer.policeUrl" :href="config.footer.policeUrl" class="footer__link" target="_blank" rel="noopener noreferrer">
        {{ config.footer.policeText }}
      </a>
      <span v-else-if="config.footer.policeText">{{ config.footer.policeText }}</span>
      <RouterLink to="/privacy" class="footer__link">隐私说明</RouterLink>
    </div>
  </footer>
</template>

<style scoped>
/* 页脚：与导航同一深蓝，形成首尾呼应 */
.footer {
  background: var(--c-navy);
  color: var(--c-on-dark);
  margin-top: 0;
}

.footer__top {
  display: flex;
  flex-wrap: wrap;
  gap: 48px;
  padding-top: 72px;
  padding-bottom: 56px;
}

.footer__brand {
  flex: 1.4 1 260px;
  min-width: 220px;
}

.footer__col {
  flex: 1 1 150px;
  min-width: 140px;
}

.footer__logo {
  max-height: 40px;
  max-width: 180px;
  object-fit: contain;
}

.footer__name {
  font-size: 20px;
  font-weight: 500;
  color: var(--c-on-dark);
}

.footer__slogan {
  margin-top: 12px;
  font-size: 14px;
  color: var(--c-on-dark-muted);
  max-width: 320px;
  line-height: 1.8;
}

.footer__title {
  font-size: 13px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-weight: 500;
  margin-bottom: 16px;
  color: rgba(255, 255, 255, 0.5);
}

.footer__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 11px;
  font-size: 14px;
  color: var(--c-on-dark-muted);
}

.footer__link {
  color: var(--c-on-dark-muted);
}

.footer__link:hover {
  color: #fff;
}

.footer__link--button {
  padding: 0;
  border: none;
  background: none;
  font-family: inherit;
  font-size: inherit;
  text-align: left;
  cursor: pointer;
}

.footer__bottom {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  padding-top: 24px;
  padding-bottom: 32px;
  border-top: 1px solid var(--c-on-dark-line);
  font-size: 13px;
  color: rgba(255, 255, 255, 0.45);
}

.footer__bottom .footer__link {
  color: rgba(255, 255, 255, 0.45);
}

.footer__bottom .footer__link:hover {
  color: #fff;
}

@media (max-width: 860px) {
  .footer__top {
    gap: 36px;
    padding-top: 48px;
    padding-bottom: 36px;
  }
}
</style>
