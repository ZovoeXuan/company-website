<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ApiError } from '@/api/http'
import { submitLead } from '@/api/public'
import { loadConsultContext } from '@/composables/consult'

const props = defineProps<{
  sourcePosition: string
  contentType?: string
  contentId?: string
  initialDemand?: string
}>()

const route = useRoute()
const uid = 'consult-' + Math.random().toString(36).slice(2, 8)

const context = reactive({
  loaded: false,
  formEnabled: true,
  privacyVersion: 1 as number | null,
  successText: '',
})

const form = reactive({
  name: '',
  companyName: '',
  phone: '',
  demand: props.initialDemand || '',
  consent: false,
})

watch(
  () => props.initialDemand,
  (val) => {
    if (val) {
      form.demand = val
    }
  },
)

const errors = reactive<Record<string, string>>({})
const submitting = ref(false)
const attempted = ref(false)
const succeeded = ref(false)
const receiptNo = ref('')
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
    const data = await loadConsultContext()
    context.loaded = true
    context.formEnabled = data.formEnabled
    context.privacyVersion = data.privacyVersion ?? 1
    context.successText = data.successText
  } catch {
    context.loaded = true
    context.formEnabled = true
  }
})

function validate(): boolean {
  Object.keys(errors).forEach((k) => delete errors[k])

  if (!form.name.trim()) {
    errors.name = '请填写您的姓名'
  }
  if (!form.companyName.trim()) {
    errors.companyName = '请填写公司名称'
  }
  if (!form.phone.trim()) {
    errors.phone = '请填写联系电话'
  } else if (!/^1[3-9]\d{9}$/.test(form.phone.trim())) {
    errors.phone = '请输入有效的 11 位中国大陆手机号'
  }
  if (!form.demand.trim()) {
    errors.demand = '请简要描述您的咨询需求'
  }
  if (!form.consent) {
    errors.consent = '请阅读并勾选同意《隐私说明》'
  }

  return Object.keys(errors).length === 0
}

watch(
  () => [form.name, form.companyName, form.phone, form.demand, form.consent],
  () => {
    if (attempted.value) validate()
  },
)

function resetForNext() {
  form.name = ''
  form.companyName = ''
  form.phone = ''
  form.demand = ''
  form.consent = false
  attempted.value = false
  succeeded.value = false
  receiptNo.value = ''
  idempotencyKey.value = newKey()
}

async function handleSubmit() {
  attempted.value = true
  submitError.value = ''
  if (!validate()) return

  submitting.value = true
  try {
    const res = await submitLead(
      {
        name: form.name.trim(),
        phone: form.phone.trim(),
        companyName: form.companyName.trim(),
        demand: form.demand.trim(),
        sourcePath: route.path || '/',
        sourcePosition: props.sourcePosition || 'OTHER_CTA',
        contentType: props.contentType,
        contentId: props.contentId ? Number(props.contentId) : undefined,
        consent: true,
        consentVersion: context.privacyVersion ?? 1,
      },
      idempotencyKey.value,
    )
    succeeded.value = true
    receiptNo.value = res.receiptNo
  } catch (e) {
    if (e instanceof ApiError) {
      submitError.value = e.message
    } else {
      submitError.value = '网络异常，提交未成功（您的输入已保留，可直接重试）'
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="consult-form-wrap">
    <div v-if="succeeded" class="success-state" role="status">
      <div class="success-icon" aria-hidden="true">✓</div>
      <h3 class="success-title">咨询提交成功</h3>
      <p class="success-desc">{{ context.successText || '已收到您的咨询，我们的专家顾问将尽快与您联系。' }}</p>
      <p v-if="receiptNo" class="receipt-tag">受理回执单号：<strong>{{ receiptNo }}</strong></p>
      <button type="button" class="reset-btn" @click="resetForNext">继续提交新咨询</button>
    </div>

    <div v-else-if="context.loaded && !context.formEnabled" class="closed-state">
      <p>在线咨询通道暂未开启，请直接通过联系我们页面的电话或邮箱与我们联系。</p>
      <RouterLink to="/contact" class="reset-btn">前往联系我们 →</RouterLink>
    </div>

    <form v-else class="consult-form" novalidate @submit.prevent="handleSubmit">
      <div class="form-row">
        <div class="form-item" :class="{ 'has-error': errors.name }">
          <label :for="`${uid}-name`" class="form-label">姓名 <span>*</span></label>
          <input
            :id="`${uid}-name`"
            v-model="form.name"
            type="text"
            maxlength="50"
            class="form-input"
            placeholder="您的称呼"
            :aria-invalid="Boolean(errors.name)"
          />
          <span v-if="errors.name" class="error-msg" role="alert">{{ errors.name }}</span>
        </div>

        <div class="form-item" :class="{ 'has-error': errors.phone }">
          <label :for="`${uid}-phone`" class="form-label">手机号码 <span>*</span></label>
          <input
            :id="`${uid}-phone`"
            v-model="form.phone"
            type="tel"
            maxlength="11"
            class="form-input"
            placeholder="11 位手机号"
            :aria-invalid="Boolean(errors.phone)"
          />
          <span v-if="errors.phone" class="error-msg" role="alert">{{ errors.phone }}</span>
        </div>
      </div>

      <div class="form-item" :class="{ 'has-error': errors.companyName }">
        <label :for="`${uid}-company`" class="form-label">公司名称 <span>*</span></label>
        <input
          :id="`${uid}-company`"
          v-model="form.companyName"
          type="text"
          maxlength="100"
          class="form-input"
          placeholder="请输入贵公司全称或简称"
          :aria-invalid="Boolean(errors.companyName)"
        />
        <span v-if="errors.companyName" class="error-msg" role="alert">{{ errors.companyName }}</span>
      </div>

      <div class="form-item" :class="{ 'has-error': errors.demand }">
        <label :for="`${uid}-demand`" class="form-label">咨询需求 <span>*</span></label>
        <textarea
          :id="`${uid}-demand`"
          v-model="form.demand"
          rows="3"
          maxlength="2000"
          class="form-textarea"
          placeholder="请描述您的车队规模、业务场景或希望了解的产品方案..."
          :aria-invalid="Boolean(errors.demand)"
        ></textarea>
        <span v-if="errors.demand" class="error-msg" role="alert">{{ errors.demand }}</span>
      </div>

      <div class="form-item" :class="{ 'has-error': errors.consent }">
        <label class="checkbox-label">
          <input v-model="form.consent" type="checkbox" class="form-checkbox" />
          <span>
            我已阅读并同意
            <RouterLink to="/privacy" target="_blank">《隐私说明》</RouterLink>
          </span>
        </label>
        <span v-if="errors.consent" class="error-msg" role="alert">{{ errors.consent }}</span>
      </div>

      <div v-if="submitError" class="submit-error" role="alert">{{ submitError }}</div>

      <button type="submit" class="submit-btn" :disabled="submitting">
        {{ submitting ? '正在加密提交...' : '立即预约专家演示' }}
      </button>
    </form>
  </div>
</template>

<style scoped>
.consult-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

@media (max-width: 520px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: #101014;
}

.form-label span {
  color: #ef4444;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid rgba(16, 16, 20, 0.16);
  border-radius: 10px;
  font-size: 14px;
  color: #101014;
  background: #ffffff;
  font-family: inherit;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #5b4be8;
  box-shadow: 0 0 0 3px #eeebff;
}

.has-error .form-input,
.has-error .form-textarea {
  border-color: #ef4444;
}

.error-msg {
  font-size: 12px;
  color: #ef4444;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12.5px;
  color: #5f6470;
  cursor: pointer;
}

.checkbox-label a {
  color: #5b4be8;
  text-decoration: underline;
}

.form-checkbox {
  width: 15px;
  height: 15px;
  accent-color: #5b4be8;
}

.submit-error {
  padding: 10px 12px;
  border-radius: 8px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #991b1b;
  font-size: 12.5px;
}

.submit-btn {
  height: 44px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #6d5ce8 0%, #5b4be8 100%);
  color: #ffffff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 8px 20px rgba(91, 75, 232, 0.3);
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
}

.submit-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.success-state,
.closed-state {
  text-align: center;
  padding: 28px 16px;
  background: #f8f9fc;
  border-radius: 14px;
}

.success-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #ecfdf5;
  color: #10b981;
  font-size: 24px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}

.success-title {
  margin: 0 0 8px;
  font-size: 17px;
  color: #101014;
}

.success-desc {
  margin: 0 0 14px;
  font-size: 13.5px;
  color: #5f6470;
}

.receipt-tag {
  font-size: 12.5px;
  color: #5f6470;
  background: #ffffff;
  padding: 6px 14px;
  border-radius: 999px;
  display: inline-block;
  margin-bottom: 16px;
  border: 1px solid rgba(16, 16, 20, 0.08);
}

.reset-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 18px;
  border-radius: 999px;
  border: 1px solid #5b4be8;
  color: #5b4be8;
  background: #ffffff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}
</style>
