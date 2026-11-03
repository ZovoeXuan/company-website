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
      :crumbs="[{ label: '首页', path: '/' }, { label: '联系我们' }]"
    />

    <section class="section">
      <div class="container layout">
        <div class="info">
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
        </div>

        <div class="form-wrap">
          <h2 class="form__title">在线咨询</h2>
          <ConsultForm source-position="CONTACT" :success-text="page?.successText" />
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.layout {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 64px;
  align-items: start;
}

.info__title {
  font-size: 20px;
  font-weight: 500;
  margin-bottom: 20px;
}

.info__list {
  margin: 0;
}

.info__row {
  display: grid;
  grid-template-columns: 64px minmax(0, 1fr);
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid var(--c-border);
  font-size: 15px;
}

.info__row dt {
  color: var(--c-text-muted);
  font-size: 14px;
}

.info__link {
  color: var(--c-brand);
}

.info__link:hover {
  text-decoration: underline;
}

.form-wrap {
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  padding: 32px;
}

.form__title {
  font-size: 20px;
  font-weight: 500;
  margin-bottom: 24px;
}

@media (max-width: 980px) {
  .layout {
    grid-template-columns: 1fr;
    gap: 32px;
  }
  .form-wrap {
    padding: 24px;
  }
}
</style>
