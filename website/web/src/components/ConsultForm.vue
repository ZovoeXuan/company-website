<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { ApiError } from '@/api/http'
import { submitLead } from '@/api/public'
import { loadConsultContext } from '@/composables/consult'

const props = defineProps<{
  sourcePosition: string
  contentType?: string
  contentId?: string
  successText?: string
}>()

const uid = 'consult-' + Math.random().toString(36).slice(2, 8)
const context = reactive({
  loaded: false,
  formEnabled: false,
  privacyVersion: null as number | null,
  successText: '',
  phone: '',
  email: '',
})

const form = reactive({
  name: '',
  companyName: '',
  phone: '',
  demand: '',
  consent: false,
})

const errors = reactive<Record<string, string>>({})
const submitting = ref(false)
const attempted = ref(false)
const succeeded = ref(false)
const receipt = ref('')
const submitError = ref('')
const idempotencyKey = ref(newKey())

function newKey(): string {
  return globalThis.crypto?.randomUUID
    ? globalThis.crypto.randomUUID()
    : 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
        const r = (Math.random() * 16) | 0
        const v = c === 'x' ? r : (r & 0x3) | 0x8
        return v.toString(16)
      })
}

onMounted(async () => {
  try {
    const loaded = await loadConsultContext()
    context.formEnabled = loaded.formEnabled
    context.privacyVersion = loaded.privacyVersion
    context.successText = props.successText || loaded.successText
    context.phone = loaded.site?.contact.phone ?? ''
    context.email = loaded.site?.contact.email ?? ''
  } finally {
    context.loaded = true
  }
})

// 上次提交失败后用户改了内容：这是新的咨询，换一个幂等键，避免与首次载荷冲突
watch(
  () => [form.name, form.companyName, form.phone, form.demand, form.consent],
  () => {
    if (attempted.value && !submitting.value && !succeeded.value) {
      idempotencyKey.value = newKey()
      attempted.value = false
      submitError.value = ''
    }
  },
)

function validate(): boolean {
  Object.keys(errors).forEach((key) => delete errors[key])
  if (!form.name.trim()) errors.name = '请填写您的称呼'
  else if (form.name.trim().length > 50) errors.name = '称呼不能超过 50 字'
  if (!form.companyName.trim()) errors.companyName = '请填写公司名称'
  else if (form.companyName.trim().length > 100) errors.companyName = '公司名称不能超过 100 字'
  if (!/^1[3-9]\d{9}$/.test(form.phone.trim())) errors.phone = '请填写 11 位中国大陆手机号'
  if (!form.demand.trim()) errors.demand = '请简单描述需求'
  else if (form.demand.trim().length > 2000) errors.demand = '需求描述不能超过 2000 字'
  if (!form.consent) errors.consent = '请先阅读并同意隐私说明'
  return Object.keys(errors).length === 0
}

function applyFieldErrors(error: ApiError) {
  for (const item of error.fieldErrors ?? []) {
    if (item.field === 'Idempotency-Key') {
      submitError.value = item.message
    } else {
      errors[item.field] = item.message
    }
  }
}

async function submit() {
  submitError.value = ''
  succeeded.value = false
  if (!validate()) return
  if (!context.privacyVersion) {
    submitError.value = '隐私说明尚未发布，暂时无法提交，请通过电话或邮箱联系我们。'
    return
  }

  submitting.value = true
  attempted.value = true
  try {
    const payload = {
      name: form.name.trim(),
      phone: form.phone.trim(),
      companyName: form.companyName.trim(),
      demand: form.demand.trim(),
      sourcePath: window.location.pathname,
      sourcePosition: props.sourcePosition,
      consent: form.consent,
      consentVersion: context.privacyVersion,
      ...(props.contentType && props.contentId
        ? { contentType: props.contentType, contentId: Number(props.contentId) }
        : {}),
    }
    const result = await submitLead(payload, idempotencyKey.value)
    succeeded.value = true
    receipt.value = result.receiptNo
    form.name = ''
    form.companyName = ''
    form.phone = ''
    form.demand = ''
    form.consent = false
    attempted.value = false
    idempotencyKey.value = newKey()
  } catch (error) {
    if (error instanceof ApiError) {
      applyFieldErrors(error)
      if (error.code === 'IDEMPOTENCY_CONFLICT') {
        idempotencyKey.value = newKey()
        submitError.value = '提交内容与上次不一致，已重置本次提交，请重新点击提交。'
      } else if (!error.fieldErrors?.length) {
        submitError.value = error.message
      }
    } else {
      submitError.value = '网络异常，请保留当前内容并重试。'
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="consult">
    <p v-if="!context.loaded" class="state-line" aria-busy="true">正在加载咨询方式…</p>

    <div v-else-if="!context.formEnabled || !context.privacyVersion" class="disabled">
      <p>在线表单暂未启用，请通过电话或邮箱联系我们。</p>
      <p v-if="context.phone || context.email" class="disabled__contact">
        <a v-if="context.phone" :href="`tel:${context.phone.replace(/[\s-]/g, '')}`">{{ context.phone }}</a>
        <a v-if="context.email" :href="`mailto:${context.email}`">{{ context.email }}</a>
      </p>
    </div>

    <form v-else class="form" novalidate @submit.prevent="submit">
      <div class="field">
        <label class="field__label" :for="`${uid}-name`">称呼<span class="req" aria-hidden="true">*</span></label>
        <input
          :id="`${uid}-name`"
          v-model="form.name"
          type="text"
          autocomplete="name"
          maxlength="50"
          :aria-invalid="Boolean(errors.name)"
          :aria-describedby="errors.name ? `${uid}-name-error` : undefined"
        />
        <p v-if="errors.name" :id="`${uid}-name-error`" class="field__error" role="alert">{{ errors.name }}</p>
      </div>

      <div class="field">
        <label class="field__label" :for="`${uid}-company`">公司名称<span class="req" aria-hidden="true">*</span></label>
        <input
          :id="`${uid}-company`"
          v-model="form.companyName"
          type="text"
          autocomplete="organization"
          maxlength="100"
          :aria-invalid="Boolean(errors.companyName)"
          :aria-describedby="errors.companyName ? `${uid}-company-error` : undefined"
        />
        <p v-if="errors.companyName" :id="`${uid}-company-error`" class="field__error" role="alert">
          {{ errors.companyName }}
        </p>
      </div>

      <div class="field">
        <label class="field__label" :for="`${uid}-phone`">手机号<span class="req" aria-hidden="true">*</span></label>
        <input
          :id="`${uid}-phone`"
          v-model="form.phone"
          type="tel"
          inputmode="numeric"
          autocomplete="tel"
          maxlength="11"
          :aria-invalid="Boolean(errors.phone)"
          :aria-describedby="errors.phone ? `${uid}-phone-error` : `${uid}-phone-hint`"
        />
        <p :id="`${uid}-phone-hint`" class="field__hint">仅用于回复本次咨询，不用于营销。</p>
        <p v-if="errors.phone" :id="`${uid}-phone-error`" class="field__error" role="alert">{{ errors.phone }}</p>
      </div>

      <div class="field">
        <label class="field__label" :for="`${uid}-demand`">需求描述<span class="req" aria-hidden="true">*</span></label>
        <textarea
          :id="`${uid}-demand`"
          v-model="form.demand"
          rows="4"
          maxlength="2000"
          placeholder="例如：三条冲压线，设备来自不同厂商，想解决停机原因说不清的问题"
          :aria-invalid="Boolean(errors.demand)"
          :aria-describedby="errors.demand ? `${uid}-demand-error` : undefined"
        ></textarea>
        <p v-if="errors.demand" :id="`${uid}-demand-error`" class="field__error" role="alert">{{ errors.demand }}</p>
      </div>

      <div class="field field--check">
        <input
          :id="`${uid}-consent`"
          v-model="form.consent"
          type="checkbox"
          :aria-invalid="Boolean(errors.consent)"
          :aria-describedby="errors.consent ? `${uid}-consent-error` : undefined"
        />
        <label :for="`${uid}-consent`" class="field__label field__label--inline">
          我已阅读并同意<RouterLink to="/privacy" class="field__link">隐私说明</RouterLink>
        </label>
        <p v-if="errors.consent" :id="`${uid}-consent-error`" class="field__error" role="alert">
          {{ errors.consent }}
        </p>
      </div>

      <button type="submit" class="btn btn--primary form__submit" :disabled="submitting">
        {{ submitting ? '提交中…' : '提交咨询' }}
      </button>

      <p v-if="submitError" class="error-notice" role="alert">{{ submitError }}</p>

      <div v-if="succeeded" class="success" role="status">
        <p class="success__text">{{ context.successText }}</p>
        <p class="success__receipt">回执号：{{ receipt }}（请留存，便于核对本次咨询）</p>
      </div>
    </form>
  </div>
</template>

<style scoped>
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

.form__submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.error-notice {
  font-size: 13px;
  line-height: 1.7;
  color: #a33124;
  background: #fdf0ee;
  border: 1px solid #f0c8c1;
  border-radius: var(--radius-sm);
  padding: 12px 14px;
}

.success {
  border: 1px solid #cfe6d5;
  background: #f2faf4;
  border-radius: var(--radius-sm);
  padding: 16px;
}

.success__text {
  font-size: 14px;
  line-height: 1.8;
  color: #23633a;
}

.success__receipt {
  margin-top: 8px;
  font-size: 13px;
  color: var(--c-text-muted);
}

.disabled {
  border: 1px dashed var(--c-border-strong);
  border-radius: var(--radius-sm);
  background: var(--c-surface);
  padding: 20px;
  font-size: 14px;
  color: var(--c-text-muted);
  line-height: 1.8;
}

.disabled__contact {
  margin-top: 8px;
  display: flex;
  gap: 16px;
}

.disabled__contact a {
  color: var(--c-brand);
}

.state-line {
  font-size: 14px;
  color: var(--c-text-muted);
}
</style>
