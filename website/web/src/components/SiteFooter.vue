<script setup lang="ts">
import { computed } from 'vue'
import type { ContactEmailItem, NavItem, QrCodeItem, SiteConfig } from '@/data/types'

const props = defineProps<{ config: SiteConfig }>()

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
      { label: '企业简介', path: '/about?tab=intro#part' },
      { label: '管理团队', path: '/about?tab=team#part' },
      { label: '媒体中心', path: '/about?tab=media#part' },
      { label: '加入我们', path: '/about?tab=join#part' },
    ],
  },
]

const groups = computed(() => {
  const configured = props.config.footer?.groups ?? []
  if (configured.length > 0) {
    return configured.map((g) => {
      if (g.title === '关于我们') {
        return {
          title: g.title,
          links: [
            { label: '企业简介', path: '/about?tab=intro#part' },
            { label: '管理团队', path: '/about?tab=team#part' },
            { label: '媒体中心', path: '/about?tab=media#part' },
            { label: '加入我们', path: '/about?tab=join#part' },
          ],
        }
      }
      return g
    })
  }
  return defaultFooterGroups
})

const contactEmails = computed<ContactEmailItem[]>(() => {
  if (props.config.contactEmails && props.config.contactEmails.length > 0) {
    return props.config.contactEmails
  }
  const primary = props.config.contact?.email || 'connect@g7.com.cn'
  return [
    { email: primary, desc: '媒体联系&数据合作' },
    { email: 'jiancha@g7.com.cn', desc: '集团投诉与举报' },
  ]
})

const contactAddress = computed(
  () => props.config.contact?.address || '北京市海淀区建材城中路27号金隅智造工场N7号楼2层',
)

const qrCodes = computed<QrCodeItem[]>(() => {
  const raw = props.config.footer?.qrCodes ?? []
  if (raw.length >= 2) return raw
  if (raw.length === 1) {
    return [raw[0], { label: '视频号', imageUrl: '' }]
  }
  return [
    { label: '公众号', imageUrl: '' },
    { label: '视频号', imageUrl: '' },
  ]
})

const icpText = computed(() => props.config.footer?.icp || '京ICP备08004136号')
const icpUrl = computed(() => props.config.footer?.icpUrl || 'https://beian.miit.gov.cn/')
const copyrightText = computed(() => props.config.footer?.copyright || '© 2026 G7E6.com.cn')

function switchLang(lang: 'zh' | 'en') {
  document.documentElement.lang = lang === 'en' ? 'en' : 'zh-CN'
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<template>
  <!-- 1:1 还原四段视频底部深色 6 栏页脚（双二维码上下垂直叠放、邮箱职能副标题、右侧语言与备案对齐） -->
  <footer class="g7-footer">
    <div class="container container--wide g7-footer__inner">
      <!-- 左侧斜体 G 标识 -->
      <div class="g7-footer__brand">
        <RouterLink to="/" class="g7-footer__mark" aria-label="返回首页">G</RouterLink>
      </div>

      <!-- 4 列导航分组（IoT服务 / 交易服务 / 解决方案 / 关于我们） -->
      <div v-for="grp in groups" :key="grp.title" class="g7-footer__col">
        <h3 class="g7-footer__title">{{ grp.title }}</h3>
        <ul class="g7-footer__list">
          <li v-for="lnk in grp.links" :key="lnk.label">
            <a
              v-if="lnk.url"
              :href="lnk.url"
              class="g7-footer__link"
              :target="lnk.newWindow ? '_blank' : undefined"
              :rel="lnk.newWindow ? 'noopener noreferrer' : undefined"
            >
              {{ lnk.label }}
            </a>
            <RouterLink v-else :to="lnk.path || '/'" class="g7-footer__link">
              {{ lnk.label }}
            </RouterLink>
          </li>
        </ul>
      </div>

      <!-- 第 5 列：联系我们（双邮箱 + 灰色职能说明 + 底部地址） -->
      <div class="g7-footer__col g7-footer__col--contact">
        <h3 class="g7-footer__title">联系我们</h3>
        <div v-for="item in contactEmails" :key="item.email" class="g7-footer__email-block">
          <a :href="`mailto:${item.email}`" class="g7-footer__email">{{ item.email }}</a>
          <span class="g7-footer__email-desc">{{ item.desc }}</span>
        </div>
        <p class="g7-footer__addr">{{ contactAddress }}</p>
      </div>

      <!-- 第 6 列：关注我们（双二维码上下垂直排列：公众号在上、视频号在下） -->
      <div class="g7-footer__col g7-footer__col--qr">
        <h3 class="g7-footer__title">关注我们</h3>
        <div class="g7-footer__qr-stack">
          <div v-for="(qr, qIdx) in qrCodes" :key="`${qr.label}-${qIdx}`" class="g7-footer__qr-item">
            <div class="g7-footer__qr-box">
              <img v-if="qr.imageUrl" :src="qr.imageUrl" :alt="qr.label" />
              <svg
                v-else
                viewBox="0 0 100 100"
                class="g7-footer__qr-svg"
                aria-hidden="true"
              >
                <rect width="100" height="100" fill="#ffffff" />
                <rect x="8" y="8" width="26" height="26" fill="none" stroke="#111" stroke-width="6" />
                <rect x="16" y="16" width="10" height="10" fill="#111" />
                <rect x="66" y="8" width="26" height="26" fill="none" stroke="#111" stroke-width="6" />
                <rect x="74" y="16" width="10" height="10" fill="#111" />
                <rect x="8" y="66" width="26" height="26" fill="none" stroke="#111" stroke-width="6" />
                <rect x="16" y="74" width="10" height="10" fill="#111" />
                <rect x="42" y="12" width="6" height="6" fill="#111" />
                <rect x="52" y="20" width="6" height="12" fill="#111" />
                <rect x="40" y="40" width="20" height="20" rx="3" fill="#4f46e5" />
                <text x="50" y="55" text-anchor="middle" fill="#fff" font-size="13" font-weight="900" font-style="italic">G</text>
                <rect x="14" y="44" width="18" height="6" fill="#111" />
                <rect x="68" y="44" width="18" height="6" fill="#111" />
                <rect x="44" y="68" width="12" height="6" fill="#111" />
                <rect x="64" y="66" width="10" height="10" fill="#111" />
                <rect x="80" y="74" width="12" height="18" fill="#111" />
                <rect x="44" y="82" width="18" height="10" fill="#111" />
              </svg>
            </div>
            <span class="g7-footer__qr-label">{{ qr.label }}</span>
          </div>
        </div>
      </div>

      <!-- 第 7 列：网站语言 + 底部备案版权信息同列对齐 -->
      <div class="g7-footer__col g7-footer__col--lang">
        <div>
          <h3 class="g7-footer__title">网站语言</h3>
          <div class="g7-footer__langs">
            <button type="button" class="g7-footer__lang-btn" @click="switchLang('zh')">中文</button>
            <button type="button" class="g7-footer__lang-btn is-muted" @click="switchLang('en')">English</button>
          </div>
        </div>

        <div class="g7-footer__legal">
          <RouterLink to="/privacy" class="g7-footer__legal-link">网络货运信息公示</RouterLink>
          <a
            :href="icpUrl"
            target="_blank"
            rel="noopener noreferrer"
            class="g7-footer__legal-link"
          >
            {{ icpText }}
          </a>
          <RouterLink to="/privacy" class="g7-footer__legal-link">隐私说明</RouterLink>
          <p class="g7-footer__copy">{{ copyrightText }}</p>
        </div>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.g7-footer {
  background: #060709;
  color: #ffffff;
  padding: 76px 0 68px;
}

.g7-footer__inner {
  max-width: 1180px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 52px repeat(4, minmax(96px, 1fr)) 190px 108px 148px;
  gap: 24px;
  align-items: start;
}

.g7-footer__mark {
  display: inline-block;
  font-size: 20px;
  font-weight: 900;
  font-style: italic;
  color: #ffffff;
  text-decoration: none;
  line-height: 1;
}

.g7-footer__title {
  font-size: 13px;
  font-weight: 400;
  color: #6e7380;
  margin: 0 0 24px;
}

.g7-footer__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.g7-footer__link {
  font-size: 13px;
  color: #d0d5dd;
  text-decoration: none;
  transition: color 0.18s ease;
}

.g7-footer__link:hover {
  color: #ffffff;
}

.g7-footer__email-block {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 22px;
}

.g7-footer__email {
  font-size: 13.5px;
  color: #e5e7eb;
  text-decoration: none;
}

.g7-footer__email:hover {
  color: #ffffff;
}

.g7-footer__email-desc {
  font-size: 12px;
  color: #5f6470;
}

.g7-footer__addr {
  margin: 26px 0 0;
  font-size: 12px;
  line-height: 1.75;
  color: #6e7380;
}

.g7-footer__qr-stack {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.g7-footer__qr-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 84px;
}

.g7-footer__qr-box {
  width: 84px;
  height: 84px;
  background: #ffffff;
  padding: 4px;
  border-radius: 2px;
  overflow: hidden;
}

.g7-footer__qr-box img,
.g7-footer__qr-svg {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}

.g7-footer__qr-label {
  margin-top: 8px;
  font-size: 12px;
  color: #98a2b3;
}

.g7-footer__col--lang {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 245px;
}

.g7-footer__langs {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-start;
}

.g7-footer__lang-btn {
  border: none;
  background: none;
  padding: 0;
  font-size: 13px;
  color: #e5e7eb;
  cursor: pointer;
}

.g7-footer__lang-btn.is-muted {
  color: #5f6470;
}

.g7-footer__lang-btn:hover {
  color: #ffffff;
}

.g7-footer__legal {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 32px;
}

.g7-footer__legal-link {
  font-size: 12.5px;
  color: #d0d5dd;
  text-decoration: none;
}

.g7-footer__legal-link:hover {
  color: #ffffff;
}

.g7-footer__copy {
  margin: 2px 0 0;
  font-size: 12px;
  color: #5f6470;
}

@media (max-width: 1024px) {
  .g7-footer__inner {
    grid-template-columns: repeat(2, 1fr);
    gap: 32px;
  }
  .g7-footer__col--lang {
    min-height: auto;
  }
}
</style>
