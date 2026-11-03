<script setup lang="ts">
import { onMounted, ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import ConsultForm from '@/components/ConsultForm.vue'
import { getContactPage, getSite } from '@/api/public'
import { usePageMeta } from '@/composables/usePageMeta'
import type { ContactPage, SiteConfig } from '@/data/types'

const site = ref<SiteConfig | null>(null)
const page = ref<ContactPage | null>(null)

usePageMeta(() => ({
  title: page.value?.title || '联系我们',
  description: page.value?.description || '电话、邮箱与在线咨询。留下需求，我们会先判断是否适合再做沟通。',
}))

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
      :description="page?.description || '先了解你的场景，再决定是否值得合作。工作时间内通常 4 小时内回复。'"
      eyebrow="CONTACT"
      tone="light"
      centered
      :crumbs="[{ label: '首页', path: '/' }, { label: '联系我们' }]"
    />

    <section class="section">
      <div class="container layout">
        <div class="info" v-reveal>
          <p class="kicker">DIRECT LINE</p>
          <h2 class="info__title">直接联系</h2>
          <dl class="info__list">
            <div v-if="site?.contact.phone" class="info__row">
              <dt>电话</dt>
              <dd>
                <a class="info__link" :href="`tel:${site.contact.phone.replace(/[\s-]/g, '')}`">
                  {{ site.contact.phone }}
                </a>
              </dd>
            </div>
            <div v-if="site?.contact.email" class="info__row">
              <dt>邮箱</dt>
              <dd><a class="info__link" :href="`mailto:${site.contact.email}`">{{ site.contact.email }}</a></dd>
            </div>
            <div v-if="site?.contact.address" class="info__row">
              <dt>地址</dt>
              <dd>{{ site.contact.address }}</dd>
            </div>
            <div v-if="site?.contact.hours" class="info__row">
              <dt>时间</dt>
              <dd>{{ site.contact.hours }}</dd>
            </div>
          </dl>

          <div class="map" aria-hidden="true">
            <div class="map__grid"></div>
            <div class="map__pin"></div>
            <p class="map__label">HQ · 示例科技</p>
          </div>
        </div>

        <div class="form-wrap" v-reveal="120">
          <div class="form-head">
            <p class="kicker">START A CONVERSATION</p>
            <h2 class="form__title">在线咨询</h2>
            <p class="form__desc">留下联系方式与场景，我们会在一个工作日内回复。</p>
          </div>
          <ConsultForm source-position="CONTACT" :success-text="page?.successText" />
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.layout {
  display: grid;
  grid-template-columns: 0.9fr 1.1fr;
  gap: 80px;
  align-items: start;
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
  grid-template-columns: 72px minmax(0, 1fr);
  gap: 16px;
  padding: 20px 0;
  border-bottom: 1px solid var(--c-border);
  font-size: 16px;
}

.info__row dt {
  color: var(--c-text-subtle);
  font-size: 12px;
  letter-spacing: 0.06em;
  padding-top: 3px;
}

.info__link {
  color: var(--c-text);
  font-weight: 500;
}

.info__link:hover {
  color: var(--c-brand);
}

.map {
  position: relative;
  margin-top: 36px;
  height: 230px;
  border-radius: var(--radius);
  overflow: hidden;
  background: radial-gradient(circle at 30% 40%, rgba(91, 75, 232, 0.18), transparent 55%),
    #eef3fa;
}

.map__grid {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(16, 16, 20, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(16, 16, 20, 0.06) 1px, transparent 1px);
  background-size: 40px 40px;
}

.map__pin {
  position: absolute;
  left: 50%;
  top: 46%;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--c-brand);
  transform: translate(-50%, -50%);
  box-shadow: 0 0 0 10px rgba(91, 75, 232, 0.16), 0 0 0 22px rgba(91, 75, 232, 0.08);
}

.map__label {
  position: absolute;
  left: 24px;
  bottom: 20px;
  font-size: 11px;
  letter-spacing: 0.12em;
  color: var(--c-text-muted);
}

.form-wrap {
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 44px;
  box-shadow: 0 24px 64px rgba(10, 12, 20, 0.08);
}

.form-head {
  padding-bottom: 28px;
  margin-bottom: 28px;
  border-bottom: 1px solid var(--c-border);
}

.form__title {
  font-size: clamp(26px, 3vw, 34px);
  letter-spacing: -0.03em;
}

.form__desc {
  margin-top: 10px;
  font-size: 14px;
  color: var(--c-text-muted);
  line-height: 1.8;
}

@media (max-width: 980px) {
  .layout {
    grid-template-columns: 1fr;
    gap: 48px;
  }
  .form-wrap {
    padding: 28px;
  }
  .map {
    height: 180px;
  }
}
</style>
