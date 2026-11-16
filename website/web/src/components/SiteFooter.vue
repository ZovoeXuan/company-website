<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getSite } from '@/api/public'
import { useRealtimeSync } from '@/composables/useRealtimeSync'
import {
  isPlaceholderContact,
  resolvePublicContactEmails,
  resolvePublicPhone,
} from '@/utils/siteDisplay'
import type { ContactEmailItem, NavItem, SiteConfig } from '@/data/types'

const site = ref<SiteConfig | null>(null)

const footerGroups = computed(() => {
  const groups = site.value?.footer?.groups
  if (!Array.isArray(groups)) return []
  return groups.filter((g) => g && g.title && Array.isArray(g.links) && g.links.length > 0)
})

const contactEmails = computed<ContactEmailItem[]>(() =>
  resolvePublicContactEmails(site.value?.contactEmails, site.value?.contact?.email),
)

const servicePhone = computed(() =>
  resolvePublicPhone(site.value?.contact?.phone, null),
)

const qrList = computed(() => {
  const qrs = site.value?.footer?.qrCodes
  if (Array.isArray(qrs) && qrs.length > 0) {
    return qrs.filter((item) => item && item.imageUrl)
  }
  if (site.value?.contact?.qrImageUrl) {
    return [
      {
        label: site.value.contact.qrLabel || '官方二维码',
        imageUrl: site.value.contact.qrImageUrl,
      },
    ]
  }
  return []
})

function isExternal(link: NavItem) {
  const targetUrl = link.url || link.path || ''
  return link.linkType === 'URL' || /^https?:\/\//i.test(targetUrl)
}

async function reloadFooterConfig() {
  const siteData = await getSite().catch(() => null)
  if (siteData) site.value = siteData
}

useRealtimeSync(['siteConfigVersion'], () => {
  void reloadFooterConfig()
})

onMounted(reloadFooterConfig)
</script>

<template>
  <footer class="site-footer" role="contentinfo">
    <div class="site-footer__container">
      <div class="site-footer__main">
        <!-- 左侧：品牌信息、服务热线、二维码与联系邮箱 -->
        <div class="site-footer__contact">
          <RouterLink to="/" class="footer-brand">
            <img
              v-if="site?.logoUrl"
              :src="site.logoUrl"
              :alt="site.logoAlt || site.siteName || '企业官网'"
              class="footer-brand__img"
            />
            <span v-else class="footer-brand__text">{{ site?.siteName || '企业官网' }}</span>
          </RouterLink>

          <p v-if="site?.slogan" class="footer-desc">
            {{ site.slogan }}
          </p>

          <div v-if="servicePhone" class="footer-hotline">
            <span class="footer-hotline__label">咨询热线</span>
            <a :href="`tel:${servicePhone.replace(/[\s-]/g, '')}`" class="footer-hotline__number">
              {{ servicePhone }}
            </a>
          </div>

          <div v-if="qrList.length > 0" class="footer-qrs">
            <div v-for="qr in qrList" :key="qr.label" class="footer-qr">
              <div class="footer-qr__box">
                <img :src="qr.imageUrl || ''" :alt="qr.label" class="footer-qr__img" />
              </div>
              <span class="footer-qr__label">{{ qr.label }}</span>
            </div>
          </div>

          <ul v-if="contactEmails.length > 0" class="footer-emails">
            <li v-for="(item, idx) in contactEmails" :key="idx">
              <a :href="`mailto:${item.email}`" class="footer-emails__addr">{{ item.email }}</a>
              <span v-if="item.desc" class="footer-emails__desc">（{{ item.desc }}）</span>
            </li>
          </ul>

          <p
            v-if="site?.contact?.address && !isPlaceholderContact(site.contact.address)"
            class="footer-address"
          >
            地址：{{ site.contact.address }}
          </p>
        </div>

        <!-- 右侧：后台配置的页脚导航分组（未配置时不伪造分组） -->
        <div v-if="footerGroups.length > 0" class="site-footer__columns">
          <div v-for="group in footerGroups" :key="group.title" class="footer-col">
            <h3 class="footer-col__title">{{ group.title }}</h3>
            <ul class="footer-col__list">
              <li v-for="link in group.links" :key="link.label + link.path">
                <a
                  v-if="isExternal(link)"
                  :href="link.url || link.path"
                  :target="link.newWindow ? '_blank' : undefined"
                  :rel="link.newWindow ? 'noopener noreferrer' : undefined"
                  class="footer-col__link"
                >
                  {{ link.label }}
                </a>
                <RouterLink v-else :to="link.path" class="footer-col__link">
                  {{ link.label }}
                </RouterLink>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <!-- 底部版权与备案栏 -->
      <div class="site-footer__bottom">
        <div class="site-footer__legal">
          <span>
            {{
              site?.footer?.copyright ||
              `© ${new Date().getFullYear()} ${site?.siteName || ''} 保留所有权利`
            }}
          </span>
          <a
            v-if="site?.footer?.icp"
            :href="site.footer.icpUrl || 'https://beian.miit.gov.cn'"
            target="_blank"
            rel="noopener noreferrer"
            class="footer-legal-link"
          >
            {{ site.footer.icp }}
          </a>
          <a
            v-if="site?.footer?.policeText"
            :href="site.footer.policeUrl || 'https://www.beian.gov.cn'"
            target="_blank"
            rel="noopener noreferrer"
            class="footer-legal-link"
          >
            {{ site.footer.policeText }}
          </a>
          <RouterLink to="/privacy" class="footer-legal-link">隐私说明</RouterLink>
        </div>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.site-footer {
  background: #0f172a;
  color: #cbd5e1;
  padding: 64px 0 36px;
  font-size: 13px;
}

.site-footer__container {
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 32px;
}

.site-footer__main {
  display: grid;
  grid-template-columns: 340px minmax(0, 1fr);
  gap: 56px;
  padding-bottom: 44px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.footer-brand {
  display: inline-flex;
  align-items: center;
  margin-bottom: 14px;
}

.footer-brand__img {
  height: 30px;
  width: auto;
}

.footer-brand__text {
  font-size: 18px;
  font-weight: 700;
  color: #ffffff;
}

.footer-desc {
  font-size: 13px;
  line-height: 1.7;
  color: #94a3b8;
  margin: 0 0 18px;
}

.footer-hotline {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 20px;
}

.footer-hotline__label {
  font-size: 12px;
  color: #94a3b8;
}

.footer-hotline__number {
  font-size: 22px;
  font-weight: 700;
  color: #ffffff;
  text-decoration: none;
  font-variant-numeric: tabular-nums;
}

.footer-qrs {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.footer-qr {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.footer-qr__box {
  width: 84px;
  height: 84px;
  background: #ffffff;
  padding: 5px;
  border-radius: 6px;
}

.footer-qr__img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.footer-qr__label {
  font-size: 12px;
  color: #94a3b8;
}

.footer-emails {
  list-style: none;
  margin: 0 0 12px;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 12.5px;
}

.footer-emails__addr {
  color: #e2e8f0;
  text-decoration: none;
}

.footer-emails__addr:hover {
  color: #ffffff;
  text-decoration: underline;
}

.footer-emails__desc {
  color: #94a3b8;
}

.footer-address {
  font-size: 12.5px;
  color: #94a3b8;
  line-height: 1.6;
}

.site-footer__columns {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 28px;
}

.footer-col__title {
  font-size: 14px;
  font-weight: 600;
  color: #ffffff;
  margin: 0 0 14px;
}

.footer-col__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.footer-col__link {
  color: #94a3b8;
  text-decoration: none;
  font-size: 13px;
  transition: color 0.16s ease;
}

.footer-col__link:hover {
  color: #ffffff;
}

.site-footer__bottom {
  padding-top: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 12px;
  color: #64748b;
}

.site-footer__legal {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 18px;
}

.footer-legal-link {
  color: #94a3b8;
  text-decoration: none;
}

.footer-legal-link:hover {
  color: #ffffff;
}

@media (max-width: 960px) {
  .site-footer__main {
    grid-template-columns: 1fr;
    gap: 36px;
  }
}
</style>
