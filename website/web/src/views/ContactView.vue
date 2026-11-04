<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import ConsultForm from '@/components/ConsultForm.vue'
import { getContactPage, getSite } from '@/api/public'
import { usePageMeta } from '@/composables/usePageMeta'
import type { ContactPage, SiteConfig } from '@/data/types'

const site = ref<SiteConfig | null>(null)
const page = ref<ContactPage | null>(null)
const activeCity = ref('chengdu')

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
  lat: number
  lng: number
}

const branches: BranchOffice[] = [
  {
    id: 'shenzhen',
    city: '深圳',
    name: '深圳总部',
    phone: '0755-26944168',
    address: '深圳市南山区滨海大道3369号有线信息传输大厦9层',
    photo: 'https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?auto=format&fit=crop&w=600&q=80',
    lat: 22.53,
    lng: 113.93,
  },
  {
    id: 'beijing',
    city: '北京',
    name: '北京总部',
    phone: '010-62961800',
    address: '北京市海淀区中关村南大街1号中鼎大厦 / 联想园区N7栋2层',
    photo: 'https://images.unsplash.com/photo-1508873696983-2df5293cb32f?auto=format&fit=crop&w=600&q=80',
    lat: 39.95,
    lng: 116.32,
  },
  {
    id: 'chengdu',
    city: '成都',
    name: '成都分部',
    phone: '028-83358692',
    address: '成都市天府新区兴隆湖畔路西段99号天府新经济产业园D区B6栋28楼',
    photo: 'https://images.unsplash.com/photo-1577495508048-b635879837f1?auto=format&fit=crop&w=600&q=80',
    lat: 30.41,
    lng: 104.09,
  },
  {
    id: 'guangzhou',
    city: '广州',
    name: '广州分部',
    phone: '020-81075656',
    address: '广州市越秀区中山六路238号越秀新都会大厦西座22楼22C04-22C07室',
    photo: 'https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=600&q=80',
    lat: 23.12,
    lng: 113.25,
  },
  {
    id: 'shanghai',
    city: '上海',
    name: '上海分部',
    phone: '021-64058562',
    address: '上海市闵行区虹莘路3999号MT1栋9层918',
    photo: 'https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=600&q=80',
    lat: 31.15,
    lng: 121.38,
  },
  {
    id: 'xian',
    city: '西安',
    name: '西安分部',
    phone: '029-81107733',
    address: '西安市雁塔区天谷七路996号国家数字出版基地D座8楼802、803室',
    photo: 'https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&w=600&q=80',
    lat: 34.21,
    lng: 108.86,
  },
  {
    id: 'tianjin',
    city: '天津',
    name: '天津分部',
    phone: '022-87895668',
    address: '天津市滨海高新区华苑产业区海泰发展六道6号海泰绿色产业基地',
    photo: 'https://images.unsplash.com/photo-1497366811353-6870744d04b2?auto=format&fit=crop&w=600&q=80',
    lat: 39.11,
    lng: 117.11,
  },
]

const currentBranch = computed(() => {
  return branches.find((b) => b.id === activeCity.value) || branches[2]
})

onMounted(async () => {
  const [siteConfig, contactPage] = await Promise.all([
    getSite().catch(() => null),
    getContactPage().catch(() => null),
  ])
  site.value = siteConfig
  page.value = contactPage
})
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

    <!-- 对齐视频：全国分支网点交互视窗 (frame 238s) -->
    <section class="section section--surface branch-section">
      <div class="container">
        <div class="section-head">
          <p class="kicker">NATIONAL NETWORK</p>
          <h2 class="section-title">全国服务分支网络</h2>
          <p class="section-sub">总部设于深圳与北京，在全国核心物流枢纽城市设有直属技术与服务中心</p>
        </div>

        <div class="branch-layout">
          <!-- 左侧：分支机构卡片滚动列表 -->
          <div class="branch-list">
            <article
              v-for="b in branches"
              :key="b.id"
              class="branch-card"
              :class="{ 'is-active': activeCity === b.id }"
              tabindex="0"
              role="button"
              :aria-pressed="activeCity === b.id"
              @click="activeCity = b.id"
              @keydown.enter="activeCity = b.id"
            >
              <div class="branch-card__head">
                <h3 class="branch-card__city">{{ b.city }}</h3>
                <span class="branch-card__tag">{{ b.name }}</span>
              </div>
              <p class="branch-card__phone">
                <span class="branch-card__label">电话：</span>
                <a :href="`tel:${b.phone.replace(/[\s-]/g, '')}`" @click.stop>{{ b.phone }}</a>
              </p>
              <p class="branch-card__address">
                <span class="branch-card__label">地址：</span>
                {{ b.address }}
              </p>
            </article>
          </div>

          <!-- 右侧：办公空间实景与中国地图网点可视化 -->
          <div class="branch-preview" v-reveal="100">
            <div class="branch-preview__visual">
              <img :src="currentBranch.photo" :alt="currentBranch.name" class="branch-preview__img" />
              <div class="branch-preview__caption">
                <p class="branch-preview__city-title">{{ currentBranch.name }}</p>
                <p class="branch-preview__city-addr">{{ currentBranch.address }}</p>
              </div>
            </div>

            <!-- 互动地图打点视窗 -->
            <div class="branch-map">
              <div class="branch-map__inner">
                <div
                  v-for="b in branches"
                  :key="b.id"
                  class="map-pin"
                  :class="{ 'is-active': activeCity === b.id }"
                  :style="{
                    left: `${((b.lng - 100) / 24) * 80 + 10}%`,
                    top: `${((42 - b.lat) / 22) * 80 + 10}%`
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
            <div v-if="site?.contact.phone" class="info__row">
              <dt>服务热线</dt>
              <dd>
                <a class="info__link info__link--phone" :href="`tel:${site.contact.phone.replace(/[\s-]/g, '')}`">
                  {{ site.contact.phone }}
                </a>
              </dd>
            </div>
            <div v-if="site?.contact.email" class="info__row">
              <dt>商务合作</dt>
              <dd><a class="info__link" :href="`mailto:${site.contact.email}`">{{ site.contact.email }}</a></dd>
            </div>
            <div class="info__row">
              <dt>媒体联系</dt>
              <dd><a class="info__link" href="mailto:connect@g7.com.cn">connect@g7.com.cn</a></dd>
            </div>
            <div class="info__row">
              <dt>廉洁举报</dt>
              <dd><a class="info__link" href="mailto:jiancha@g7.com.cn">jiancha@g7.com.cn</a></dd>
            </div>
            <div v-if="site?.contact.hours" class="info__row">
              <dt>服务时间</dt>
              <dd>{{ site.contact.hours }}</dd>
            </div>
          </dl>
        </div>

        <div class="form-wrap" v-reveal="120">
          <div class="form-head">
            <p class="kicker">START A CONVERSATION</p>
            <h2 class="form__title">预约专业方案沟通</h2>
            <p class="form__desc">留下联系方式与业务痛点，资深行业顾问将在 4 小时内与您接洽。</p>
          </div>
          <ConsultForm source-position="CONTACT" :success-text="page?.successText" />
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
  color: var(--c-text-muted);
  margin: 0;
}
</style>
