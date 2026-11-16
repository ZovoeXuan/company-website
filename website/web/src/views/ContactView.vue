<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import ConsultForm from '@/components/ConsultForm.vue'
import { getAbout, getContactPage, getSite } from '@/api/public'
import { usePageMeta } from '@/composables/usePageMeta'
import { useRealtimeSync } from '@/composables/useRealtimeSync'
import { isPlaceholderContact, resolvePublicContactEmails } from '@/utils/siteDisplay'
import type { AboutPage, ContactPage, SiteConfig } from '@/data/types'

const site = ref<SiteConfig | null>(null)
const page = ref<ContactPage | null>(null)
const about = ref<AboutPage | null>(null)
const activeCity = ref('')

usePageMeta(() => ({
  title: page.value?.title || '联系我们',
  description: page.value?.description || '全国服务网络、各地分部与在线咨询。',
}))

interface BranchOffice {
  id: string
  city: string
  name: string
  phone: string
  address: string
  photo: string
  mapX: number
  mapY: number
}

const branches = computed<BranchOffice[]>(() => {
  if (about.value?.showOffices === false) return []
  const offices = about.value?.offices
  if (!Array.isArray(offices)) return []
  return offices.map((off, idx) => ({
    id: `${off.city || 'office'}-${idx}`,
    city: off.city || '',
    name: off.city || '',
    phone: isPlaceholderContact(off.phone) ? '' : (off.phone || '').trim(),
    address: off.address || '',
    photo: off.image || '',
    mapX: typeof off.mapX === 'number' ? off.mapX : 66,
    mapY: typeof off.mapY === 'number' ? off.mapY : 48,
  }))
})

const currentBranch = computed(() => {
  return branches.value.find((b) => b.id === activeCity.value) || branches.value[0] || null
})

const servicePhone = computed(() => {
  const candidate = page.value?.phone || site.value?.contact?.phone
  return isPlaceholderContact(candidate) ? '' : candidate!.trim()
})

const businessEmail = computed(() => {
  const candidate = page.value?.email || site.value?.contact?.email
  return isPlaceholderContact(candidate) ? '' : candidate!.trim()
})

const extraEmails = computed(() =>
  resolvePublicContactEmails(site.value?.contactEmails, null),
)

async function loadContact() {
  const [siteConfig, contactPage, aboutPage] = await Promise.all([
    getSite().catch(() => null),
    getContactPage().catch(() => null),
    getAbout().catch(() => null),
  ])
  site.value = siteConfig
  page.value = contactPage
  about.value = aboutPage
  if (branches.value.length > 0 && !branches.value.some((b) => b.id === activeCity.value)) {
    activeCity.value = branches.value[0].id
  }
}

useRealtimeSync(['siteConfigVersion', 'aboutVersion'], () => {
  void loadContact()
})

onMounted(loadContact)
</script>

<template>
  <div>
    <PageHeader
      :title="page?.title || '联系我们'"
      :description="page?.description || '全国本地化技术支持与专属客户成功团队，随时响应您的业务诉求。'"
      eyebrow="CONTACT"
      tone="light"
      centered
      :crumbs="[{ label: '首页', path: '/' }, { label: '联系我们' }]"
    />

    <!-- 全国分支网点交互视窗：单源读取 ABOUT.offices，未配置或关闭时不显示 -->
    <section v-if="branches.length > 0 && currentBranch" class="section section--surface branch-section">
      <div class="container">
        <div class="section-head">
          <p class="kicker">NATIONAL NETWORK</p>
          <h2 class="section-title">全国服务分支网络</h2>
          <p class="section-sub">本地化技术支持与专属客户服务团队</p>
        </div>

        <div class="branch-layout">
          <!-- 左侧：分支机构卡片滚动列表 -->
          <div class="branch-list">
            <article
              v-for="b in branches"
              :key="b.id"
              class="branch-card"
              :class="{ 'is-active': currentBranch.id === b.id }"
              tabindex="0"
              role="button"
              :aria-pressed="currentBranch.id === b.id"
              @click="activeCity = b.id"
              @keydown.enter="activeCity = b.id"
            >
              <div class="branch-card__head">
                <h3 class="branch-card__city">{{ b.city }}</h3>
                <span class="branch-card__tag">{{ b.name }}</span>
              </div>
              <p v-if="b.phone" class="branch-card__phone">
                <span class="branch-card__label">电话：</span>
                <a :href="`tel:${b.phone.replace(/[\s-]/g, '')}`" @click.stop>{{ b.phone }}</a>
              </p>
              <p v-if="b.address" class="branch-card__address">
                <span class="branch-card__label">地址：</span>
                {{ b.address }}
              </p>
            </article>
          </div>

          <!-- 右侧：办公空间实景与中国地图网点可视化 -->
          <div class="branch-preview" v-reveal="100">
            <div v-if="currentBranch.photo" class="branch-preview__visual">
              <img :src="currentBranch.photo" :alt="currentBranch.name" class="branch-preview__img" />
              <div class="branch-preview__caption">
                <p class="branch-preview__city-title">{{ currentBranch.name }}</p>
                <p v-if="currentBranch.address" class="branch-preview__city-addr">{{ currentBranch.address }}</p>
              </div>
            </div>

            <!-- 互动地图打点视窗 -->
            <div class="branch-map">
              <div class="branch-map__inner">
                <div
                  v-for="b in branches"
                  :key="b.id"
                  class="map-pin"
                  :class="{ 'is-active': currentBranch.id === b.id }"
                  :style="{
                    left: `${b.mapX}%`,
                    top: `${b.mapY}%`
                  }"
                  @click="activeCity = b.id"
                >
                  <span class="map-pin__dot"></span>
                  <span class="map-pin__label">{{ b.city }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 在线咨询表单与直连热线 -->
    <section class="section">
      <div class="container layout">
        <div class="info" v-reveal>
          <p class="kicker">DIRECT LINE</p>
          <h2 class="info__title">直接联系</h2>
          <dl class="info__list">
            <div v-if="page?.companyName || site?.contact?.companyName" class="info__row">
              <dt>公司名称</dt>
              <dd>{{ page?.companyName || site?.contact?.companyName }}</dd>
            </div>
            <div v-if="servicePhone" class="info__row">
              <dt>服务热线</dt>
              <dd>
                <a class="info__link info__link--phone" :href="`tel:${servicePhone.replace(/[\s-]/g, '')}`">
                  {{ servicePhone }}
                </a>
              </dd>
            </div>
            <div v-if="businessEmail" class="info__row">
              <dt>商务合作</dt>
              <dd><a class="info__link" :href="`mailto:${businessEmail}`">{{ businessEmail }}</a></dd>
            </div>
            <div v-if="page?.address || site?.contact?.address" class="info__row">
              <dt>总部地址</dt>
              <dd>
                <span>{{ page?.address || site?.contact?.address }}</span>
                <a
                  v-if="page?.mapUrl && /^https?:\/\//i.test(page.mapUrl)"
                  :href="page.mapUrl"
                  target="_blank"
                  rel="noopener noreferrer"
                  style="margin-left: 10px; font-size: 13px; color: var(--c-brand);"
                >查看地图 →</a>
              </dd>
            </div>
            <template v-if="extraEmails.length > 0">
              <div v-for="(item, idx) in extraEmails" :key="idx" class="info__row">
                <dt>{{ item.desc || '联系邮箱' }}</dt>
                <dd><a class="info__link" :href="`mailto:${item.email}`">{{ item.email }}</a></dd>
              </div>
            </template>
            <div v-else-if="!businessEmail" class="info__row">
              <dt>官方邮箱</dt>
              <dd>暂未公开</dd>
            </div>
            <div v-if="page?.hours || site?.contact.hours" class="info__row">
              <dt>服务时间</dt>
              <dd>{{ page?.hours || site?.contact.hours }}</dd>
            </div>
          </dl>
        </div>

        <div class="form-wrap" v-reveal="120">
          <div class="form-head">
            <p class="kicker">START A CONVERSATION</p>
            <h2 class="form__title">{{ page?.formTitle || '预约专业方案沟通' }}</h2>
            <p class="form__desc">留下联系方式与业务痛点，资深行业顾问将在 4 小时内与您接洽。</p>
          </div>
          <div v-if="!page || page.formEnabled === false" class="form-closed-notice">
            <p>在线咨询通道暂未开启，如有业务咨询请直接拨打上方服务热线或发送邮件。</p>
          </div>
          <ConsultForm v-else source-position="CONTACT" :success-text="page?.successText" />
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.branch-section {
  padding-bottom: 72px;
}

.branch-layout {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 40px;
  align-items: start;
}

@media (max-width: 992px) {
  .branch-layout {
    grid-template-columns: 1fr;
  }
}

.branch-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  max-height: 520px;
  overflow-y: auto;
  padding-right: 8px;
}

.branch-card {
  padding: 20px 24px;
  background: #ffffff;
  border: 1px solid var(--c-border);
  border-radius: 12px;
  cursor: pointer;
  outline: none;
  transition: all 0.25s var(--ease-out);
}

.branch-card:hover {
  border-color: var(--c-border-strong);
  transform: translateX(4px);
}

.branch-card.is-active {
  border-color: var(--c-brand, #5b4be8);
  box-shadow: 0 10px 28px rgba(91, 75, 232, 0.12);
  background: #ffffff;
}

.branch-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.branch-card__city {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--c-text);
}

.branch-card.is-active .branch-card__city {
  color: var(--c-brand, #5b4be8);
}

.branch-card__tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  background: var(--c-surface);
  color: var(--c-text-muted);
}

.branch-card.is-active .branch-card__tag {
  background: rgba(91, 75, 232, 0.1);
  color: var(--c-brand, #5b4be8);
}

.branch-card__phone,
.branch-card__address {
  margin: 6px 0 0;
  font-size: 13.5px;
  color: var(--c-text-muted);
  line-height: 1.5;
}

.branch-card__label {
  color: var(--c-text-subtle);
}

.branch-card__phone a {
  color: var(--c-text);
  font-weight: 600;
}

.branch-card__phone a:hover {
  color: var(--c-brand);
}

.branch-preview {
  display: flex;
  flex-direction: column;
  gap: 20px;
  position: sticky;
  top: 100px;
}

.branch-preview__visual {
  position: relative;
  border-radius: 14px;
  overflow: hidden;
  aspect-ratio: 16 / 9;
  background: #111;
  box-shadow: 0 14px 36px rgba(10, 12, 20, 0.1);
}

.branch-preview__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.branch-preview__caption {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 20px 24px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.85), transparent);
  color: #fff;
}

.branch-preview__city-title {
  font-size: 17px;
  font-weight: 700;
  margin: 0 0 4px;
}

.branch-preview__city-addr {
  font-size: 12px;
  opacity: 0.85;
  margin: 0;
}

.branch-map {
  background: #ffffff;
  border: 1px solid var(--c-border);
  border-radius: 14px;
  height: 180px;
  position: relative;
  overflow: hidden;
}

.branch-map__inner {
  position: relative;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 50% 50%, rgba(91, 75, 232, 0.05), transparent 70%);
}

.map-pin {
  position: absolute;
  transform: translate(-50%, -50%);
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.map-pin:hover {
  transform: translate(-50%, -50%) scale(1.15);
}

.map-pin__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--c-text-muted);
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.8);
  transition: all 0.2s ease;
}

.map-pin.is-active .map-pin__dot {
  width: 12px;
  height: 12px;
  background: var(--c-brand, #5b4be8);
  box-shadow: 0 0 0 4px rgba(91, 75, 232, 0.25);
}

.map-pin__label {
  font-size: 11px;
  font-weight: 600;
  color: var(--c-text-muted);
}

.map-pin.is-active .map-pin__label {
  color: var(--c-brand, #5b4be8);
}

/* 直连与表单 */
.layout {
  display: grid;
  grid-template-columns: 0.9fr 1.1fr;
  gap: 80px;
  align-items: start;
}

@media (max-width: 900px) {
  .layout {
    grid-template-columns: 1fr;
    gap: 48px;
  }
}

.info__title {
  font-size: clamp(26px, 3vw, 36px);
  letter-spacing: -0.03em;
  margin-bottom: 28px;
}

.info__list {
  margin: 0;
  border-top: 1px solid var(--c-border);
}

.info__row {
  display: grid;
  grid-template-columns: 90px minmax(0, 1fr);
  gap: 16px;
  padding: 18px 0;
  border-bottom: 1px solid var(--c-border);
  font-size: 15px;
}

.info__row dt {
  color: var(--c-text-subtle);
  font-size: 13px;
  padding-top: 2px;
}

.info__link {
  color: var(--c-text);
  font-weight: 500;
}

.info__link--phone {
  font-size: 20px;
  font-weight: 700;
  color: var(--c-brand, #5b4be8);
}

.info__link:hover {
  color: var(--c-brand);
}

.form-head {
  margin-bottom: 28px;
}

.form__title {
  font-size: 28px;
  margin: 4px 0 8px;
}

.form__desc {
  font-size: 14px;
  color: var(--c-text-subtle);
}

.form-closed-notice {
  padding: 32px 24px;
  background: var(--c-surface, #f8fafc);
  border: 1px dashed var(--c-border, #e2e8f0);
  border-radius: 8px;
  text-align: center;
  color: var(--c-text-subtle);
  font-size: 14px;
}
</style>
