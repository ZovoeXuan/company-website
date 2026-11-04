<script setup lang="ts">
import { computed } from 'vue'
import { openConsult } from '@/composables/consult'
import type { NavItem, SiteConfig } from '@/data/types'

const props = defineProps<{ config: SiteConfig }>()

// 默认全业务对齐视频页脚结构，若后台配置了 footer.groups 则优先渲染后台配置
const defaultFooterGroups: Array<{ title: string; links: NavItem[] }> = [
  {
    title: 'IoT服务',
    links: [
      { label: '安全管家', path: '/products?category=安全' },
      { label: '冷链管家', path: '/products?category=冷链' },
      { label: '流向管家', path: '/products?category=流向' },
      { label: '追货管家', path: '/products?category=追货' },
      { label: '开放平台', path: '/products' },
    ],
  },
  {
    title: '交易服务',
    links: [
      { label: '财运通', path: '/solutions?category=交易' },
      { label: '数字能源', path: '/solutions?category=能源' },
      { label: '共享挂车', path: '/solutions' },
    ],
  },
  {
    title: '解决方案',
    links: [
      { label: '大宗物流数字化', path: '/solutions/1' },
      { label: '连锁零售数字化', path: '/solutions/2' },
      { label: '饲料防偷盗 AI 治理', path: '/solutions/3' },
    ],
  },
  {
    title: '关于我们',
    links: [
      { label: '企业简介', path: '/about' },
      { label: '管理团队', path: '/about' },
      { label: '媒体中心', path: '/about' },
      { label: '加入我们', path: '/about' },
    ],
  },
]

const groups = computed(() => {
  const configured = props.config.footer?.groups ?? []
  return configured.length > 0 ? configured : defaultFooterGroups
})
</script>

<template>
  <footer class="footer on-dark">
    <!-- 对齐视频：页脚上方醒目 CTA 横幅 (frame 120s) -->
    <div class="footer-cta-band">
      <div class="container container--wide footer-cta-band__inner">
        <h2 class="footer-cta-band__title">以物联网技术和软件服务改变公路货运</h2>
        <button
          type="button"
          class="btn footer-cta-band__btn"
          @click="openConsult({ sourcePosition: 'FOOTER' })"
        >
          立即咨询
        </button>
      </div>
    </div>

    <!-- 对齐视频：多列企业导航与联系矩阵 (frame 215s) -->
    <div class="container container--wide footer__body">
      <!-- 品牌标识列 -->
      <div class="footer__col footer__col--brand">
        <RouterLink to="/" class="footer__logo" :aria-label="`${config.siteName} 首页`">
          <span class="footer__mark" aria-hidden="true">G</span>
          <span class="footer__name">{{ config.siteName }}</span>
        </RouterLink>
        <p class="footer__motto">领先的物联网软件服务商</p>
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

      <!-- 联系我们列 -->
      <div class="footer__col footer__col--contact">
        <p class="footer__title">联系我们</p>
        <ul class="footer__list">
          <li class="footer__contact-item">
            <span class="footer__contact-label">媒体联系&数据合作</span>
            <a href="mailto:connect@g7.com.cn" class="footer__link">connect@g7.com.cn</a>
          </li>
          <li class="footer__contact-item">
            <span class="footer__contact-label">集团投诉与举报</span>
            <a href="mailto:jiancha@g7.com.cn" class="footer__link">jiancha@g7.com.cn</a>
          </li>
          <li class="footer__contact-address">
            北京市海淀区中关村南大街1号中鼎大厦 / 深圳湾科技生态园
          </li>
        </ul>
      </div>

      <!-- 关注我们列 (对齐视频二维码) -->
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
            <span class="qr-item__label">公众号</span>
          </div>
          <div class="qr-item">
            <div class="qr-item__box" aria-hidden="true">
              <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="1.5">
                <circle cx="12" cy="12" r="9"></circle>
                <polygon points="10 8 16 12 10 16 10 8" fill="currentColor"></polygon>
              </svg>
            </div>
            <span class="qr-item__label">视频号</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部版权与备案条 -->
    <div class="container container--wide footer__bottom">
      <div class="footer__bottom-left">
        <a href="#" class="footer__legal" @click.prevent>网络货运信息公示</a>
        <span class="footer__divider">|</span>
        <a
          v-if="config.footer.icp && config.footer.icpUrl"
          :href="config.footer.icpUrl"
          class="footer__legal"
          target="_blank"
          rel="noopener noreferrer"
        >
          {{ config.footer.icp }}
        </a>
        <span v-else class="footer__legal">{{ config.footer.icp || '京ICP备08004136号' }}</span>
        <span class="footer__divider">|</span>
        <RouterLink to="/privacy" class="footer__legal">隐私说明</RouterLink>
      </div>
      <div class="footer__bottom-right">
        <span>© 2026 G7E6.com.cn 版权所有</span>
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

/* 页脚上方醒目 CTA 横幅 */
.footer-cta-band {
  background: #11131a;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  padding: 38px 0;
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
  font-size: clamp(20px, 2.5vw, 28px);
  font-weight: 700;
  color: #ffffff;
  letter-spacing: -0.02em;
}

.footer-cta-band__btn {
  background: #ffffff;
  color: #090a0f;
  border: none;
  font-weight: 600;
  font-size: 15px;
  padding: 12px 32px;
  border-radius: 6px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.25s ease;
}

.footer-cta-band__btn:hover {
  background: var(--c-brand, #5b4be8);
  color: #ffffff;
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(91, 75, 232, 0.35);
}

/* 多列结构 */
.footer__body {
  display: grid;
  grid-template-columns: 140px repeat(4, minmax(0, 1fr)) 1.4fr 120px;
  gap: 36px;
  padding: 64px 0 54px;
}

@media (max-width: 1100px) {
  .footer__body {
    grid-template-columns: repeat(3, 1fr);
    gap: 36px;
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
  gap: 8px;
  color: #fff;
  text-decoration: none;
}

.footer__mark {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--c-brand, #5b4be8);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 18px;
  color: #fff;
}

.footer__name {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.footer__motto {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.45);
  margin: 0;
  line-height: 1.5;
}

.footer__title {
  margin: 0 0 18px;
  font-size: 14px;
  font-weight: 600;
  color: #ffffff;
  letter-spacing: 0.04em;
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
  color: rgba(255, 255, 255, 0.4);
}

.footer__contact-address {
  font-size: 12px;
  line-height: 1.6;
  color: #717588;
  margin-top: 6px;
}

/* 二维码卡片 */
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
  width: 52px;
  height: 52px;
  border-radius: 8px;
  background: #181b24;
  border: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.6);
}

.qr-item__label {
  font-size: 11px;
  color: #8c90a4;
}

/* 底部栏 */
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
