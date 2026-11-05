<script setup lang="ts">
import { computed } from 'vue'
import { openConsult } from '@/composables/consult'
import type { NavItem, SiteConfig } from '@/data/types'

const props = defineProps<{ config: SiteConfig }>()

const defaultFooterGroups: Array<{ title: string; links: NavItem[] }> = [
  {
    title: 'IoT服务',
    links: [
      { label: '安全管家', path: '/products' },
      { label: '冷链管家', path: '/products' },
      { label: '流向管家', path: '/products' },
      { label: '追货管家', path: '/products' },
      { label: '开放平台', path: '/products' },
    ],
  },
  {
    title: '交易服务',
    links: [
      { label: '财运通', path: '/solutions' },
      { label: '数字能源', path: '/solutions' },
      { label: '共享挂车', path: '/solutions' },
    ],
  },
  {
    title: '解决方案',
    links: [
      { label: '大宗物流数字化', path: '/solutions' },
      { label: '连锁零售数字化', path: '/solutions' },
      { label: '饲料防偷盗 AI 治理', path: '/solutions' },
    ],
  },
  {
    title: '关于我们',
    links: [
      { label: '企业简介', path: '/about' },
      { label: '发展历程', path: '/about' },
      { label: '新闻中心', path: '/news' },
      { label: '联系我们', path: '/contact' },
    ],
  },
]

const groups = computed(() => {
  const configured = props.config.footer?.groups ?? []
  return configured.length > 0 ? configured : defaultFooterGroups
})

const contactPhone = computed(() => props.config.contact?.phone || '400-888-6688')
const contactEmail = computed(() => props.config.contact?.email || 'connect@g7.com.cn')
const contactAddress = computed(
  () => props.config.contact?.address || '北京市海淀区中关村南大街1号 / 深圳湾科技生态园',
)
const contactHours = computed(() => props.config.contact?.hours || '周一至周日 09:00 - 18:00')
const copyrightText = computed(
  () => props.config.footer?.copyright || `© ${new Date().getFullYear()} ${props.config.siteName} 版权所有`,
)
</script>

<template>
  <footer class="footer on-dark">
    <!-- 页脚上方醒目 CTA 横幅 -->
    <div class="footer-cta-band">
      <div class="container container--wide footer-cta-band__inner">
        <div>
          <h2 class="footer-cta-band__title">以物联网技术和软件服务改变公路货运</h2>
          <p class="footer-cta-band__sub">立即预约行业专家，获取针对您车队与业务场景的定制化降本增效方案</p>
        </div>
        <button
          type="button"
          class="btn footer-cta-band__btn"
          @click="openConsult({ sourcePosition: 'FOOTER' })"
        >
          立即咨询 →
        </button>
      </div>
    </div>

    <!-- 多列企业导航与联系矩阵 -->
    <div class="container container--wide footer__body">
      <!-- 品牌标识列 -->
      <div class="footer__col footer__col--brand">
        <RouterLink to="/" class="footer__logo" :aria-label="`${config.siteName} 首页`">
          <img v-if="config.logoUrl" :src="config.logoUrl" :alt="config.logoAlt || config.siteName" class="footer__logo-img" />
          <template v-else>
            <span class="footer__mark" aria-hidden="true">{{ config.siteName?.slice(0, 1) || 'G' }}</span>
            <span class="footer__name">{{ config.siteName }}</span>
          </template>
        </RouterLink>
        <p class="footer__motto">{{ config.slogan || '领先的物联网与数字化软件服务商' }}</p>
      </div>

      <!-- 业务与方案列 -->
      <nav v-for="group in groups" :key="group.title" class="footer__col" :aria-label="group.title">
        <p class="footer__title">{{ group.title }}</p>
        <ul class="footer__list">
          <li v-for="item in group.links" :key="item.label">
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
            <RouterLink v-else-if="item.path" :to="item.path" class="footer__link">
              {{ item.label }}
            </RouterLink>
            <span v-else class="footer__link">{{ item.label }}</span>
          </li>
        </ul>
      </nav>

      <!-- 联系我们列（突出大字号服务热线，对齐 ui-redesign.md 规范） -->
      <div class="footer__col footer__col--contact">
        <p class="footer__title">联系我们</p>
        <div class="footer__hotline">
          <span class="footer__contact-label">全国服务热线 ({{ contactHours }})</span>
          <a :href="`tel:${contactPhone}`" class="footer__phone">{{ contactPhone }}</a>
        </div>
        <ul class="footer__list">
          <li class="footer__contact-item">
            <span class="footer__contact-label">商务合作与媒体联系</span>
            <a :href="`mailto:${contactEmail}`" class="footer__link">{{ contactEmail }}</a>
          </li>
          <li class="footer__contact-address">
            {{ contactAddress }}
          </li>
        </ul>
      </div>

      <!-- 关注我们列 -->
      <div class="footer__col footer__col--qr">
        <p class="footer__title">关注我们</p>
        <div class="qr-group">
          <div class="qr-item">
            <div class="qr-item__box" aria-hidden="true">
              <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="1.5">
                <rect x="3" y="3" width="7" height="7"></rect>
                <rect x="14" y="3" width="7" height="7"></rect>
                <rect x="3" y="14" width="7" height="7"></rect>
                <rect x="14" y="14" width="3" height="3"></rect>
                <rect x="18" y="18" width="3" height="3"></rect>
              </svg>
            </div>
            <span class="qr-item__label">官方公众号</span>
          </div>
          <div class="qr-item">
            <div class="qr-item__box" aria-hidden="true">
              <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="1.5">
                <circle cx="12" cy="12" r="9"></circle>
                <polygon points="10 8 16 12 10 16 10 8" fill="currentColor"></polygon>
              </svg>
            </div>
            <span class="qr-item__label">官方视频号</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部版权与备案条 -->
    <div class="container container--wide footer__bottom">
      <div class="footer__bottom-left">
        <a
          v-if="config.footer?.icp && config.footer?.icpUrl"
          :href="config.footer.icpUrl"
          class="footer__legal"
          target="_blank"
          rel="noopener noreferrer"
        >
          {{ config.footer.icp }}
        </a>
        <span v-else-if="config.footer?.icp" class="footer__legal">{{ config.footer.icp }}</span>

        <template v-if="config.footer?.policeText">
          <span class="footer__divider">|</span>
          <a
            v-if="config.footer.policeUrl"
            :href="config.footer.policeUrl"
            class="footer__legal"
            target="_blank"
            rel="noopener noreferrer"
          >
            {{ config.footer.policeText }}
          </a>
          <span v-else class="footer__legal">{{ config.footer.policeText }}</span>
        </template>

        <span class="footer__divider">|</span>
        <RouterLink to="/privacy" class="footer__legal">隐私说明</RouterLink>
        <span class="footer__divider">|</span>
        <RouterLink to="/contact" class="footer__legal">联系我们</RouterLink>
      </div>
      <div class="footer__bottom-right">
        <span>{{ copyrightText }}</span>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  background: #090a0f;
  color: #a0a4b8;
  margin-top: 0;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.footer-cta-band {
  background: linear-gradient(90deg, #11131f 0%, #191636 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  padding: 44px 0;
}

.footer-cta-band__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

@media (max-width: 768px) {
  .footer-cta-band__inner {
    flex-direction: column;
    text-align: center;
  }
}

.footer-cta-band__title {
  margin: 0;
  font-size: clamp(22px, 2.6vw, 30px);
  font-weight: 700;
  color: #ffffff;
  letter-spacing: -0.02em;
}

.footer-cta-band__sub {
  margin: 8px 0 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.65);
}

.footer-cta-band__btn {
  background: #5b4be8;
  color: #ffffff;
  border: none;
  font-weight: 600;
  font-size: 15px;
  padding: 13px 32px;
  border-radius: 999px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.25s ease;
  box-shadow: 0 8px 24px rgba(91, 75, 232, 0.35);
}

.footer-cta-band__btn:hover {
  background: #4636d6;
  transform: translateY(-2px);
}

.footer__body {
  display: grid;
  grid-template-columns: 160px repeat(4, minmax(0, 1fr)) 1.5fr 130px;
  gap: 32px;
  padding: 64px 0 54px;
}

@media (max-width: 1100px) {
  .footer__body {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 680px) {
  .footer__body {
    grid-template-columns: 1fr;
    gap: 28px;
  }
}

.footer__col--brand {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.footer__logo {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: #fff;
  text-decoration: none;
}

.footer__logo-img {
  max-height: 34px;
  max-width: 150px;
  object-fit: contain;
}

.footer__mark {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  background: linear-gradient(135deg, #7a6bff 0%, #5b4be8 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 17px;
  color: #fff;
}

.footer__name {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.footer__motto {
  font-size: 12.5px;
  color: rgba(255, 255, 255, 0.5);
  margin: 0;
  line-height: 1.6;
}

.footer__title {
  margin: 0 0 18px;
  font-size: 14px;
  font-weight: 600;
  color: #ffffff;
  letter-spacing: 0.04em;
}

.footer__hotline {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 14px;
}

.footer__phone {
  font-size: 24px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: -0.01em;
  text-decoration: none;
  transition: color 0.2s ease;
}

.footer__phone:hover {
  color: #8b7df0;
}

.footer__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.footer__link {
  font-size: 13px;
  color: #8c90a4;
  text-decoration: none;
  background: none;
  border: none;
  padding: 0;
  font-family: inherit;
  cursor: pointer;
  text-align: left;
  transition: color 0.2s ease;
}

.footer__link:hover {
  color: #ffffff;
}

.footer__contact-item {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.footer__contact-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.42);
}

.footer__contact-address {
  font-size: 12px;
  line-height: 1.6;
  color: #717588;
  margin-top: 6px;
}

.qr-group {
  display: flex;
  gap: 16px;
}

.qr-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.qr-item__box {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  background: #181b24;
  border: 1px solid rgba(255, 255, 255, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.75);
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.qr-item:hover .qr-item__box {
  border-color: #5b4be8;
  transform: translateY(-2px);
}

.qr-item__label {
  font-size: 11px;
  color: #8c90a4;
}

.footer__bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 24px 0;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  font-size: 12px;
  color: #686c7e;
  flex-wrap: wrap;
}

.footer__bottom-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.footer__legal {
  color: inherit;
  text-decoration: none;
  transition: color 0.2s ease;
}

.footer__legal:hover {
  color: #ffffff;
}

.footer__divider {
  opacity: 0.3;
}
</style>
