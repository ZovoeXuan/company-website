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
.footer {
  border-top: 1px solid var(--c-border);
  background: var(--c-surface);
  margin-top: 96px;
}

.footer__top {
  display: flex;
  flex-wrap: wrap;
  gap: 40px;
  padding-top: 56px;
  padding-bottom: 40px;
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
  padding-top: 20px;
  padding-bottom: 28px;
  border-top: 1px solid var(--c-border);
  font-size: 13px;
  color: var(--c-text-subtle);
}

@media (max-width: 860px) {
  .footer__top {
    gap: 32px;
    padding-top: 40px;
  }
  .footer {
    margin-top: 64px;
  }
}
</style>
