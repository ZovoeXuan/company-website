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
  <footer class="footer on-dark">
    <div class="container container--wide footer__top">
      <div class="footer__brand">
        <RouterLink to="/" class="footer__logo" :aria-label="`${config.siteName} 首页`">
          <img v-if="config.logoUrl" :src="config.logoUrl" :alt="config.logoAlt || config.siteName" />
          <template v-else>
            <span class="footer__mark" aria-hidden="true">{{ config.siteName.slice(0, 1) }}</span>
            <span class="footer__name">{{ config.siteName }}</span>
          </template>
        </RouterLink>
        <p v-if="config.slogan" class="footer__slogan">{{ config.slogan }}</p>
        <button type="button" class="footer__cta" @click="openConsult({ sourcePosition: 'FOOTER' })">
          预约一次场景演示 <span aria-hidden="true">→</span>
        </button>
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

      <div class="footer__col footer__col--contact">
        <p class="footer__title">联系我们</p>
        <ul class="footer__list">
          <li v-if="config.contact.phone">
            <a class="footer__link footer__link--strong" :href="telHref(config.contact.phone)">
              {{ config.contact.phone }}
            </a>
          </li>
          <li v-if="config.contact.email">
            <a class="footer__link" :href="`mailto:${config.contact.email}`">{{ config.contact.email }}</a>
          </li>
          <li v-if="config.contact.address" class="footer__address">{{ config.contact.address }}</li>
          <li v-if="config.contact.hours">{{ config.contact.hours }}</li>
        </ul>
      </div>
    </div>

    <div class="container container--wide footer__bottom">
      <span>{{ config.footer.copyright }}</span>
      <a
        v-if="config.footer.icp && config.footer.icpUrl"
        :href="config.footer.icpUrl"
        class="footer__legal"
        target="_blank"
        rel="noopener noreferrer"
      >
        {{ config.footer.icp }}
      </a>
      <span v-else-if="config.footer.icp">{{ config.footer.icp }}</span>
      <a
        v-if="config.footer.policeText && config.footer.policeUrl"
        :href="config.footer.policeUrl"
        class="footer__legal"
        target="_blank"
        rel="noopener noreferrer"
      >
        {{ config.footer.policeText }}
      </a>
      <span v-else-if="config.footer.policeText">{{ config.footer.policeText }}</span>
      <RouterLink to="/privacy" class="footer__legal">隐私说明</RouterLink>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  background: var(--c-navy);
  color: var(--c-on-dark);
  margin-top: 0;
}

.footer__top {
  display: grid;
  grid-template-columns: 1.6fr repeat(2, minmax(0, 1fr)) 1.2fr;
  gap: 56px;
  padding-top: 76px;
  padding-bottom: 64px;
}

.footer__brand {
  max-width: 340px;
}

.footer__logo {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.footer__logo img {
  max-height: 34px;
  max-width: 170px;
  object-fit: contain;
}

.footer__mark {
  width: 32px;
  height: 32px;
  border-radius: 9px;
  background: linear-gradient(135deg, #7a6bff, #5b4be8);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.footer__name {
  font-size: 17px;
  font-weight: 600;
}

.footer__slogan {
  margin-top: 18px;
  font-size: 14px;
  line-height: 1.8;
  color: var(--c-on-dark-muted);
}

.footer__cta {
  margin-top: 28px;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 0;
  border: none;
  background: none;
  color: #fff;
  font-family: inherit;
  font-size: 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.35);
  padding-bottom: 4px;
  cursor: pointer;
  transition: color 0.2s ease, border-color 0.2s ease;
}

.footer__cta span {
  transition: transform 0.3s var(--ease-out);
}

.footer__cta:hover {
  color: #b9b1ff;
  border-color: #b9b1ff;
}

.footer__cta:hover span {
  transform: translateX(4px);
}

.footer__title {
  font-size: 13px;
  letter-spacing: 0.08em;
  color: rgba(255, 255, 255, 0.45);
  margin-bottom: 20px;
}

.footer__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  font-size: 14px;
  color: var(--c-on-dark-muted);
}

.footer__link {
  color: var(--c-on-dark-muted);
  transition: color 0.2s ease;
}

.footer__link:hover {
  color: #fff;
}

.footer__link--strong {
  font-size: 20px;
  color: #fff;
  letter-spacing: -0.01em;
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

.footer__address {
  line-height: 1.8;
}

.footer__bottom {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  padding-top: 22px;
  padding-bottom: 30px;
  border-top: 1px solid var(--c-on-dark-line);
  font-size: 12px;
  color: rgba(255, 255, 255, 0.38);
}

.footer__legal {
  color: rgba(255, 255, 255, 0.38);
}

.footer__legal:hover {
  color: #fff;
}

@media (max-width: 1080px) {
  .footer__top {
    grid-template-columns: 1.4fr repeat(2, minmax(0, 1fr));
    gap: 40px;
  }
  .footer__col--contact {
    grid-column: 1 / -1;
  }
}

@media (max-width: 860px) {
  .footer__top {
    grid-template-columns: 1fr 1fr;
    padding-top: 52px;
    padding-bottom: 44px;
    gap: 32px;
  }
  .footer__brand {
    grid-column: 1 / -1;
    max-width: none;
  }
  .footer__col--contact {
    grid-column: 1 / -1;
  }
}
</style>
