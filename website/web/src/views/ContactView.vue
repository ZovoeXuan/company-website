<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import { getContactPage, getSite } from '@/api/public'
import { usePageMeta } from '@/composables/usePageMeta'
import type { ContactPage, SiteConfig } from '@/data/types'

const site = ref<SiteConfig | null>(null)
const page = ref<ContactPage | null>(null)
const submitted = ref(false)
const errors = reactive<Record<string, string>>({})
const form = reactive({ name: '', company: '', phone: '', demand: '', consent: false })

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

function validate(): boolean {
  Object.keys(errors).forEach((key) => delete errors[key])

  if (!form.name.trim()) errors.name = '请填写您的称呼'
  if (!form.company.trim()) errors.company = '请填写公司名称'
  if (!/^1[3-9]\d{9}$/.test(form.phone.trim())) errors.phone = '请填写 11 位手机号'
  if (form.demand.trim().length < 10) errors.demand = '请简单描述需求，至少 10 个字'
  if (!form.consent) errors.consent = '请先阅读并同意隐私说明'

  return Object.keys(errors).length === 0
}

function submit() {
  submitted.value = false
  if (!validate()) {
    return
  }
  submitted.value = true
}
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
              <dd><a class="info__link" :href="`tel:${site.contact.phone.replace(/[\s-]/g, '')}`">{{ site.contact.phone }}</a></dd>
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

          <p v-if="page && !page.formEnabled" class="form-disabled">
            在线表单暂未启用，请通过左侧电话或邮箱联系我们。
          </p>

          <form v-else class="form" novalidate @submit.prevent="submit">
            <div class="field">
              <label class="field__label" for="name">称呼<span class="req" aria-hidden="true">*</span></label>
              <input
                id="name"
                v-model="form.name"
                type="text"
                autocomplete="name"
                :aria-invalid="Boolean(errors.name)"
                :aria-describedby="errors.name ? 'name-error' : undefined"
              />
              <p v-if="errors.name" id="name-error" class="field__error" role="alert">{{ errors.name }}</p>
            </div>

            <div class="field">
              <label class="field__label" for="company">公司名称<span class="req" aria-hidden="true">*</span></label>
              <input
                id="company"
                v-model="form.company"
                type="text"
                autocomplete="organization"
                :aria-invalid="Boolean(errors.company)"
                :aria-describedby="errors.company ? 'company-error' : undefined"
              />
              <p v-if="errors.company" id="company-error" class="field__error" role="alert">{{ errors.company }}</p>
            </div>

            <div class="field">
              <label class="field__label" for="phone">手机号<span class="req" aria-hidden="true">*</span></label>
              <input
                id="phone"
                v-model="form.phone"
                type="tel"
                inputmode="numeric"
                autocomplete="tel"
                :aria-invalid="Boolean(errors.phone)"
                :aria-describedby="errors.phone ? 'phone-error' : 'phone-hint'"
              />
              <p id="phone-hint" class="field__hint">仅用于回复本次咨询，不用于营销。</p>
              <p v-if="errors.phone" id="phone-error" class="field__error" role="alert">{{ errors.phone }}</p>
            </div>

            <div class="field">
              <label class="field__label" for="demand">需求描述<span class="req" aria-hidden="true">*</span></label>
              <textarea
                id="demand"
                v-model="form.demand"
                rows="4"
                placeholder="例如：三条冲压线，设备来自不同厂商，想解决停机原因说不清的问题"
                :aria-invalid="Boolean(errors.demand)"
                :aria-describedby="errors.demand ? 'demand-error' : undefined"
              ></textarea>
              <p v-if="errors.demand" id="demand-error" class="field__error" role="alert">{{ errors.demand }}</p>
            </div>

            <div class="field field--check">
              <input
                id="consent"
                v-model="form.consent"
                type="checkbox"
                :aria-invalid="Boolean(errors.consent)"
                :aria-describedby="errors.consent ? 'consent-error' : undefined"
              />
              <label for="consent" class="field__label field__label--inline">
                我已阅读并同意<RouterLink to="/privacy" class="field__link">隐私说明</RouterLink>
              </label>
              <p v-if="errors.consent" id="consent-error" class="field__error" role="alert">{{ errors.consent }}</p>
            </div>

            <button type="submit" class="btn btn--primary form__submit">提交咨询</button>

            <p v-if="submitted" class="notice" role="status">
              {{ page?.successText || '演示模式：前端校验已通过，但表单尚未连接服务端（提交接口计划在后端阶段实现），本次未真实发送。' }}
            </p>
          </form>
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

.form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field__label {
  font-size: 14px;
  color: var(--c-text);
}

.field__label--inline {
  display: inline;
  font-size: 14px;
  color: var(--c-text-muted);
}

.req {
  color: #c0392b;
  margin-left: 2px;
}

.field input[type='text'],
.field input[type='tel'],
.field textarea {
  width: 100%;
  border: 1px solid var(--c-border-strong);
  border-radius: var(--radius-sm);
  padding: 11px 14px;
  font-size: 15px;
  font-family: inherit;
  color: var(--c-text);
  background: #fff;
}

.field textarea {
  resize: vertical;
}

.field input:focus,
.field textarea:focus {
  border-color: var(--c-brand);
}

.field input[aria-invalid='true'],
.field textarea[aria-invalid='true'] {
  border-color: #c0392b;
}

.field__hint {
  font-size: 12px;
  color: var(--c-text-subtle);
}

.field__error {
  font-size: 13px;
  color: #c0392b;
}

.field--check {
  flex-direction: row;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.field--check input {
  width: 16px;
  height: 16px;
}

.field--check .field__error {
  flex-basis: 100%;
}

.field__link {
  color: var(--c-brand);
  text-decoration: underline;
}

.form__submit {
  align-self: flex-start;
  min-width: 160px;
}

.form-disabled {
  border: 1px dashed var(--c-border-strong);
  border-radius: var(--radius-sm);
  background: var(--c-surface);
  padding: 20px;
  font-size: 14px;
  color: var(--c-text-muted);
  line-height: 1.8;
}

.notice {
  font-size: 13px;
  line-height: 1.7;
  color: #8a6d1f;
  background: #fdf6e3;
  border: 1px solid #f0e0bd;
  border-radius: var(--radius-sm);
  padding: 12px 14px;
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
