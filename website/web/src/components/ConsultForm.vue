<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { sendLeadSmsCode, submitLead } from '@/api/public'
import { ApiError } from '@/api/http'
import { useConsultDialog } from '@/composables/consult'
import type { ContentType, LeadSourcePosition } from '@/data/types'

const props = withDefaults(
  defineProps<{
    sourcePosition?: LeadSourcePosition
    contentType?: ContentType
    contentId?: string
    successText?: string
    initialDemand?: string
  }>(),
  {
    sourcePosition: 'HOME',
    contentType: undefined,
    contentId: undefined,
    successText: undefined,
    initialDemand: undefined,
  },
)

const { context, loadConsultContext } = useConsultDialog()

const form = reactive({
  name: '',
  company: '',
  phone: '',
  smsCode: '',
  demand: props.initialDemand || '',
  privacyAgreed: false,
  website: '', // 蜜罐字段
})

watch(
  () => props.initialDemand,
  (val) => {
    if (val) form.demand = val
  },
)

const errors = reactive<Record<string, string>>({})
const loadingContext = ref(true)
const submitting = ref(false)
const sendingSms = ref(false)
const submitted = ref(false)
const receiptNo = ref('')
let idempotencyKey = typeof crypto !== 'undefined' && crypto.randomUUID ? crypto.randomUUID() : `lead-${Date.now()}`
const serverError = ref('')
const smsFeedback = ref('')

// 60 秒短信验证码倒计时
const smsCountdown = ref(0)
let smsTimer: ReturnType<typeof setInterval> | null = null

const isSmsEnabled = computed(() => context.smsVerifyEnabled !== false)

onMounted(async () => {
  loadingContext.value = true
  try {
    await loadConsultContext()
  } finally {
    loadingContext.value = false
  }
})

onBeforeUnmount(() => {
  if (smsTimer) clearInterval(smsTimer)
})

const phoneRegex = /^(\+?86[-\s]?)?1[3-9]\d{9}$/

async function handleSendSms() {
  for (const key of Object.keys(errors)) delete errors[key]
  serverError.value = ''
  smsFeedback.value = ''

  if (!phoneRegex.test(form.phone.trim())) {
    errors.phone = '请先输入有效的 11 位手机号码'
    return
  }
  if (smsCountdown.value > 0 || sendingSms.value) return

  sendingSms.value = true
  try {
    const res = await sendLeadSmsCode(form.phone.trim())
    smsCountdown.value = res.cooldownSeconds || 60
    if (res.devCode) {
      smsFeedback.value = `验证码已发送（开发环境测试码：${res.devCode}）`
    } else {
      smsFeedback.value = '验证码已发送至您的手机，5 分钟内有效'
    }
    if (smsTimer) clearInterval(smsTimer)
    smsTimer = setInterval(() => {
      smsCountdown.value -= 1
      if (smsCountdown.value <= 0 && smsTimer) {
        clearInterval(smsTimer)
        smsTimer = null
      }
    }, 1000)
  } catch (err) {
    if (err instanceof ApiError) {
      errors.smsCode = err.message
    } else {
      errors.smsCode = '验证码发送失败，请稍后重试'
    }
  } finally {
    sendingSms.value = false
  }
}

function validate(): boolean {
  for (const key of Object.keys(errors)) delete errors[key]
  if (form.name.trim().length < 2 || form.name.trim().length > 40) {
    errors.name = '请填写 2-40 字的真实姓名'
  }
  if (!phoneRegex.test(form.phone.trim())) {
    errors.phone = '请填写有效的 11 位手机号码'
  }
  if (isSmsEnabled.value && !/^\d{4,6}$/.test(form.smsCode.trim())) {
    errors.smsCode = '请输入收到的短信验证码'
  }
  if (!form.company.trim() || form.company.trim().length > 100) {
    errors.company = '请填写公司名称'
  }
  if (!form.privacyAgreed) {
    errors.privacyAgreed = '提交前需阅读并同意《隐私说明》'
  }
  return Object.keys(errors).length === 0
}

async function onSubmit() {
  serverError.value = ''
  if (!validate()) return
  submitting.value = true
  try {
    const res = await submitLead(
      {
        name: form.name.trim(),
        phone: form.phone.trim(),
        smsCode: isSmsEnabled.value ? form.smsCode.trim() : undefined,
        companyName: form.company.trim(),
        demand: form.demand.trim() || '预约产品演示与方案咨询',
        sourcePath: window.location.pathname,
        sourcePosition: props.sourcePosition,
        contentType: props.contentType,
        contentId: props.contentId ? Number(props.contentId) : undefined,
        consent: form.privacyAgreed,
        consentVersion: context.privacyVersion || 1,
        hp: form.website,
      },
      idempotencyKey,
    )
    receiptNo.value = res.receiptNo
    submitted.value = true
  } catch (err) {
    if (err instanceof ApiError) {
      serverError.value = err.message
    } else {
      serverError.value = '提交失败，请检查网络或稍后再试'
    }
  } finally {
    submitting.value = false
  }
}

function reset() {
  form.name = ''
  form.company = ''
  form.phone = ''
  form.smsCode = ''
  form.demand = ''
  form.privacyAgreed = false
  smsFeedback.value = ''
  receiptNo.value = ''
  idempotencyKey = typeof crypto !== 'undefined' && crypto.randomUUID ? crypto.randomUUID() : `lead-${Date.now()}`
  submitted.value = false
}
</script>

<template>
  <div v-if="loadingContext" class="trial-loading" aria-busy="true">
    正在加载咨询通道配置…
  </div>

  <div v-else-if="context.loadError || !context.formEnabled" class="trial-disabled" role="status">
    <p class="trial-disabled__title">在线留资通道暂未开启</p>
    <p class="trial-disabled__desc">
      当前暂不支持在线提交表单，请通过页面公布的官方电话或商务邮箱与我们联系。
    </p>
  </div>

  <div v-else-if="submitted" class="trial-success" role="status" aria-live="polite">
    <div class="trial-success__icon" aria-hidden="true">✓</div>
    <p class="trial-success__title">提交成功</p>
    <p class="trial-success__desc">{{ successText || context.successText }}</p>
    <button type="button" class="trial-submit-btn trial-submit-btn--ghost" @click="reset">
      再次提交其他需求
    </button>
  </div>

  <form v-else class="trial-form" novalidate @submit.prevent="onSubmit">
    <!-- 姓名 -->
    <div class="trial-field">
      <label class="visually-hidden" for="lead-name">您的姓名</label>
      <input
        id="lead-name"
        v-model="form.name"
        type="text"
        autocomplete="name"
        placeholder="您的姓名 *"
        class="trial-input"
        :class="{ 'is-error': errors.name }"
      />
      <p v-if="errors.name" class="trial-err">{{ errors.name }}</p>
    </div>

    <!-- 公司名称 -->
    <div class="trial-field">
      <label class="visually-hidden" for="lead-company">公司名称</label>
      <input
        id="lead-company"
        v-model="form.company"
        type="text"
        autocomplete="organization"
        placeholder="公司名称 *"
        class="trial-input"
        :class="{ 'is-error': errors.company }"
      />
      <p v-if="errors.company" class="trial-err">{{ errors.company }}</p>
    </div>

    <!-- 手机号 -->
    <div class="trial-field">
      <label class="visually-hidden" for="lead-phone">手机号码</label>
      <input
        id="lead-phone"
        v-model="form.phone"
        type="tel"
        inputmode="tel"
        autocomplete="tel"
        placeholder="手机号码 *"
        class="trial-input"
        :class="{ 'is-error': errors.phone }"
      />
      <p v-if="errors.phone" class="trial-err">{{ errors.phone }}</p>
    </div>

    <!-- 手机短信验证码（当开启短信核验时显示，对接真实后端接口） -->
    <div v-if="isSmsEnabled" class="trial-field">
      <label class="visually-hidden" for="lead-sms">短信验证码</label>
      <div class="trial-sms-row" :class="{ 'is-error': errors.smsCode }">
        <input
          id="lead-sms"
          v-model="form.smsCode"
          type="text"
          inputmode="numeric"
          maxlength="6"
          placeholder="请输入 6 位短信验证码 *"
          class="trial-sms-input"
        />
        <button
          type="button"
          class="trial-sms-btn"
          :disabled="smsCountdown > 0 || sendingSms"
          @click="handleSendSms"
        >
          {{
            sendingSms
              ? '发送中…'
              : smsCountdown > 0
                ? `${smsCountdown}s 后重发`
                : '获取验证码'
          }}
        </button>
      </div>
      <p v-if="smsFeedback && !errors.smsCode" class="trial-feedback">{{ smsFeedback }}</p>
      <p v-if="errors.smsCode" class="trial-err">{{ errors.smsCode }}</p>
    </div>

    <!-- 选填业务诉求 -->
    <div class="trial-field">
      <label class="visually-hidden" for="lead-demand">业务需求说明（选填）</label>
      <input
        id="lead-demand"
        v-model="form.demand"
        type="text"
        maxlength="500"
        placeholder="您关注的业务场景或产品需求（选填）"
        class="trial-input"
      />
    </div>

    <!-- 蜜罐字段 -->
    <div class="visually-hidden" aria-hidden="true">
      <label for="lead-website">Website</label>
      <input id="lead-website" v-model="form.website" type="text" tabindex="-1" autocomplete="off" />
    </div>

    <!-- 隐私协议勾选 -->
    <div class="trial-field trial-field--consent">
      <label class="trial-checkbox">
        <input v-model="form.privacyAgreed" type="checkbox" />
        <span>
          我已阅读并同意
          <RouterLink to="/privacy" target="_blank" class="trial-privacy-link">《隐私说明》</RouterLink>
        </span>
      </label>
      <p v-if="errors.privacyAgreed" class="trial-err">{{ errors.privacyAgreed }}</p>
    </div>

    <p v-if="serverError" class="trial-err trial-err--server" role="alert">{{ serverError }}</p>

    <button type="submit" class="trial-submit-btn" :disabled="submitting">
      {{ submitting ? '正在提交…' : '立即提交' }}
    </button>
  </form>
</template>

<style scoped>
.trial-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.trial-field {
  display: flex;
  flex-direction: column;
}

.trial-input {
  width: 100%;
  height: 44px;
  padding: 0 14px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  background: #ffffff;
  font-size: 14px;
  color: #0f172a;
  font-family: inherit;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}

.trial-input::placeholder {
  color: #94a3b8;
}

.trial-input:focus {
  outline: none;
  border-color: var(--c-brand, #2563eb);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.trial-input.is-error {
  border-color: #dc2626;
}

.trial-sms-row {
  display: flex;
  align-items: center;
  height: 44px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  background: #ffffff;
  overflow: hidden;
  transition: border-color 0.18s ease;
}

.trial-sms-row:focus-within {
  border-color: var(--c-brand, #2563eb);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.trial-sms-row.is-error {
  border-color: #dc2626;
}

.trial-sms-input {
  flex: 1;
  height: 100%;
  border: none;
  padding: 0 14px;
  font-size: 14px;
  color: #0f172a;
  background: transparent;
  font-family: inherit;
}

.trial-sms-input::placeholder {
  color: #94a3b8;
}

.trial-sms-input:focus {
  outline: none;
}

.trial-sms-btn {
  height: 28px;
  padding: 0 14px;
  border: none;
  border-left: 1px solid #e2e8f0;
  background: transparent;
  color: var(--c-brand, #2563eb);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
}

.trial-sms-btn:hover:not(:disabled) {
  color: var(--c-brand-dark, #1d4ed8);
}

.trial-sms-btn:disabled {
  color: #94a3b8;
  cursor: not-allowed;
}

.trial-feedback {
  margin: 5px 0 0;
  font-size: 12px;
  color: #059669;
}

.trial-field--consent {
  margin-top: 2px;
}

.trial-checkbox {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #475569;
  cursor: pointer;
}

.trial-checkbox input {
  width: 15px;
  height: 15px;
  accent-color: var(--c-brand, #2563eb);
}

.trial-privacy-link {
  color: var(--c-brand, #2563eb);
  text-decoration: none;
}

.trial-privacy-link:hover {
  text-decoration: underline;
}

.trial-submit-btn {
  margin-top: 6px;
  width: 100%;
  height: 44px;
  border: none;
  border-radius: 6px;
  background: var(--c-brand, #2563eb);
  color: #ffffff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.18s ease;
}

.trial-submit-btn:hover:not(:disabled) {
  background: var(--c-brand-dark, #1d4ed8);
}

.trial-submit-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.trial-submit-btn--ghost {
  background: transparent;
  border: 1px solid #cbd5e1;
  color: #0f172a;
  margin-top: 16px;
}

.trial-err {
  margin: 4px 0 0;
  font-size: 12px;
  color: #dc2626;
}

.trial-err--server {
  text-align: center;
}

.trial-loading {
  padding: 28px 16px;
  text-align: center;
  font-size: 14px;
  color: #64748b;
}

.trial-disabled {
  padding: 24px 20px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  text-align: center;
}

.trial-disabled__title {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 6px;
}

.trial-disabled__desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
}

.trial-success {
  text-align: center;
  padding: 28px 16px;
}

.trial-success__icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #dcfce7;
  color: #16a34a;
  font-size: 22px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}

.trial-success__title {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
}

.trial-success__desc {
  margin: 8px 0 0;
  font-size: 13.5px;
  color: #475569;
  line-height: 1.65;
}
</style>
