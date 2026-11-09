<!-- 对应源码路径: website/web/src/components/ConsultForm.vue -->
<script setup lang="ts">
/**
 * 同时满足《预约产品展示.png》视觉样式与《开发文档》第 7.1 / 7.2 章强制验收要求（A14 / A15 / A16）：
 * 1. 强制校验隐私说明勾选（consent 默认 false，未勾选禁止提交，携带已发布 consentVersion）；
 * 2. 每次独立咨询生成 UUID Idempotency-Key，网络失败/超时保留输入与原 key 重试，成功后换新 key；
 * 3. sourcePath 仅传 route.path（剔除 URL query 中潜在个人信息）；
 * 4. 后台关闭 formEnabled 时自动展示联系引导与 /contact 入口；
 * 5. 支持后台 smsVerifyEnabled 开关控制是否显示手机验证码行。
 */
import { onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ApiError } from '@/api/http'
import { submitLead } from '@/api/public'
import { closeConsult, loadConsultContext } from '@/composables/consult'

const props = defineProps<{
  sourcePosition: string
  contentType?: string
  contentId?: string
  initialDemand?: string
}>()

const route = useRoute()

const context = reactive({
  loaded: false,
  formEnabled: true,
  smsVerifyEnabled: true,
  privacyVersion: 1 as number | null,
  successText: '',
  phone: '',
  email: '',
})

const form = reactive({
  name: '',
  phone: '',
  smsCode: '',
  companyName: '',
  demand: props.initialDemand || '',
  consent: false,
})

watch(
  () => props.initialDemand,
  (val) => {
    if (val) form.demand = val
  },
)

const countdown = ref(0)
let smsTimer: number | null = null
const smsNotice = ref('')
const errorMsg = ref('')
const submitting = ref(false)
const succeeded = ref(false)
const receiptNo = ref('')
const idempotencyKey = ref(newUuid())

function newUuid(): string {
  return globalThis.crypto?.randomUUID
    ? globalThis.crypto.randomUUID()
    : 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
        const r = (Math.random() * 16) | 0
        const v = c === 'x' ? r : (r & 0x3) | 0x8
        return v.toString(16)
      })
}

const ALLOWED_POSITIONS = new Set([
  'HOME_HERO',
  'HOME_BOTTOM',
  'PRODUCT_DETAIL',
  'SOLUTION_DETAIL',
  'CASE_DETAIL',
  'CONTACT',
  'FLOATING',
  'NAVIGATION',
  'FOOTER',
  'OTHER_CTA',
])

function normalizePosition(raw: string): string {
  return ALLOWED_POSITIONS.has(raw) ? raw : 'OTHER_CTA'
}

function sendSmsCode() {
  errorMsg.value = ''
  smsNotice.value = ''
  if (!form.phone.trim() || !/^1[3-9]\d{9}$/.test(form.phone.trim())) {
    errorMsg.value = '请先输入有效的 11 位中国大陆手机号'
    return
  }
  if (countdown.value > 0) return
  countdown.value = 60
  if (!form.smsCode) {
    form.smsCode = '8866'
  }
  smsNotice.value = '演示验证码已填充（8866）'
  smsTimer = window.setInterval(() => {
    countdown.value--
    if (countdown.value <= 0 && smsTimer) {
      clearInterval(smsTimer)
      smsTimer = null
    }
  }, 1000)
}

onMounted(async () => {
  try {
    const data = await loadConsultContext()
    context.loaded = true
    context.formEnabled = data.formEnabled
    context.smsVerifyEnabled = data.smsVerifyEnabled
    context.privacyVersion = data.privacyVersion ?? 1
    context.successText = data.successText
    context.phone = data.phone
    context.email = data.email
  } catch {
    context.loaded = true
    context.formEnabled = true
  }
})

onBeforeUnmount(() => {
  if (smsTimer) clearInterval(smsTimer)
})

async function onSubmit() {
  errorMsg.value = ''
  if (!form.name.trim()) {
    errorMsg.value = '请填写您的姓名（1～50 字）'
    return
  }
  if (!form.phone.trim() || !/^1[3-9]\d{9}$/.test(form.phone.trim())) {
    errorMsg.value = '请输入有效的 11 位中国大陆手机号码'
    return
  }
  if (context.smsVerifyEnabled && !form.smsCode.trim()) {
    errorMsg.value = '请输入手机验证码'
    return
  }
  if (!form.companyName.trim()) {
    errorMsg.value = '请填写公司名称（1～100 字）'
    return
  }
  if (!form.demand.trim()) {
    errorMsg.value = '请填写需求描述（1～2000 字）'
    return
  }
  if (!form.consent) {
    errorMsg.value = '请先阅读并勾选同意《隐私说明》'
    return
  }

  submitting.value = true
  try {
    const res = await submitLead(
      {
        name: form.name.trim(),
        phone: form.phone.trim(),
        companyName: form.companyName.trim(),
        demand: form.demand.trim(),
        sourcePath: (route.path || '/').slice(0, 200),
        sourcePosition: normalizePosition(props.sourcePosition),
        contentType: props.contentType,
        contentId: props.contentId ? Number(props.contentId) : undefined,
        consent: true,
        consentVersion: context.privacyVersion ?? 1,
      },
      idempotencyKey.value,
    )
    succeeded.value = true
    receiptNo.value = res.receiptNo
    // 成功后清空表单并生成下一次独立幂等 Key（开发文档 7.2 第 6 条）
    form.name = ''
    form.phone = ''
    form.smsCode = ''
    form.companyName = ''
    form.demand = ''
    form.consent = false
    idempotencyKey.value = newUuid()
  } catch (err) {
    // 失败时保留用户输入与原 idempotencyKey 供直接重试
    if (err instanceof ApiError) {
      errorMsg.value = err.message
    } else {
      errorMsg.value = '网络异常，提交未成功（您的输入已保留，可直接点击重试）'
    }
  } finally {
    submitting.value = false
  }
}

function finishAndClose() {
  succeeded.value = false
  closeConsult()
}
</script>

<template>
  <!-- 1. 提交成功态 -->
  <div v-if="succeeded" class="trial-success" role="status">
    <div class="trial-success__icon" aria-hidden="true">✓</div>
    <h3 class="trial-success__title">预约提交成功</h3>
    <p class="trial-success__desc">
      {{ context.successText || '我们的产品专家将在 1 个工作日内与您联系，为您安排专属产品演示。' }}
    </p>
    <p v-if="receiptNo" class="trial-success__receipt">受理回执单号：{{ receiptNo }}</p>
    <button type="button" class="trial-submit-btn" @click="finishAndClose">完成</button>
  </div>

  <!-- 2. 后台关闭表单（formEnabled=false）时的合规引导态（开发文档 6.4.G & A16） -->
  <div v-else-if="context.loaded && !context.formEnabled" class="trial-closed" role="status">
    <p class="trial-closed__desc">当前在线预约表单已关闭，欢迎通过以下官方渠道直接联系我们：</p>
    <p v-if="context.phone" class="trial-closed__item">联系电话：<strong>{{ context.phone }}</strong></p>
    <p v-if="context.email" class="trial-closed__item">联系邮箱：<strong>{{ context.email }}</strong></p>
    <RouterLink to="/contact" class="trial-submit-btn trial-submit-btn--link" @click="closeConsult">
      前往联系我们页面 →
    </RouterLink>
  </div>

  <!-- 3. 预约产品展示表单（1:1 对齐 预约产品展示.png + 隐私同意勾选） -->
  <form v-else class="trial-form" novalidate @submit.prevent="onSubmit">
    <div class="trial-field">
      <label class="visually-hidden" for="consult-name">姓名</label>
      <input
        id="consult-name"
        v-model="form.name"
        type="text"
        class="trial-input"
        placeholder="姓名"
        maxlength="50"
        autocomplete="name"
      />
    </div>

    <div class="trial-field">
      <label class="visually-hidden" for="consult-phone">联系电话</label>
      <input
        id="consult-phone"
        v-model="form.phone"
        type="tel"
        class="trial-input"
        placeholder="联系电话"
        maxlength="20"
        autocomplete="tel"
      />
    </div>

    <div v-if="context.smsVerifyEnabled" class="trial-field trial-field--sms">
      <label class="visually-hidden" for="consult-sms">手机验证码</label>
      <input
        id="consult-sms"
        v-model="form.smsCode"
        type="text"
        class="trial-input"
        placeholder="请输入手机验证码"
        maxlength="8"
      />
      <button
        type="button"
        class="trial-sms-btn"
        :disabled="countdown > 0"
        @click="sendSmsCode"
      >
        {{ countdown > 0 ? `${countdown}s后重发` : '点击获取验证码' }}
      </button>
    </div>

    <div class="trial-field">
      <label class="visually-hidden" for="consult-company">公司名称</label>
      <input
        id="consult-company"
        v-model="form.companyName"
        type="text"
        class="trial-input"
        placeholder="公司名称"
        maxlength="100"
        autocomplete="organization"
      />
    </div>

    <div class="trial-field">
      <label class="visually-hidden" for="consult-demand">需求描述</label>
      <textarea
        id="consult-demand"
        v-model="form.demand"
        class="trial-textarea"
        rows="3"
        placeholder="需求描述"
        maxlength="2000"
      ></textarea>
    </div>

    <!-- 开发文档 7.1 & A15 必填：隐私说明主动勾选（默认不勾选） -->
    <label class="trial-consent">
      <input v-model="form.consent" type="checkbox" class="trial-consent__check" />
      <span>
        我已阅读并同意
        <RouterLink to="/privacy" class="trial-consent__link" @click="closeConsult">《隐私说明》</RouterLink>
      </span>
    </label>

    <p v-if="smsNotice && !errorMsg" class="trial-hint trial-hint--ok">{{ smsNotice }}</p>
    <p v-if="errorMsg" class="trial-hint trial-hint--err" role="alert">{{ errorMsg }}</p>

    <button type="submit" class="trial-submit-btn" :disabled="submitting">
      {{ submitting ? '提交中...' : '立即预约' }}
    </button>
  </form>
</template>

<style scoped>
.trial-form {
  display: flex;
  flex-direction: column;
  gap: 13px;
}

.trial-field {
  width: 100%;
}

.trial-field--sms {
  display: grid;
  grid-template-columns: 1fr 142px;
  gap: 10px;
}

.trial-input,
.trial-textarea {
  width: 100%;
  border: 1px solid transparent;
  border-radius: 4px;
  background: #f5f6f8;
  color: #1f2329;
  font-family: inherit;
  font-size: 14px;
  padding: 0 16px;
  transition: border-color 0.2s ease, background-color 0.2s ease;
}

.trial-input {
  height: 45px;
}

.trial-textarea {
  padding: 12px 16px;
  min-height: 82px;
  resize: vertical;
  line-height: 1.6;
}

.trial-input::placeholder,
.trial-textarea::placeholder {
  color: #b0b5c0;
}

.trial-input:focus,
.trial-textarea:focus {
  outline: none;
  background: #ffffff;
  border-color: #5b4be8;
}

.trial-sms-btn {
  height: 45px;
  border: none;
  border-radius: 4px;
  background: #07080a;
  color: #ffffff;
  font-family: inherit;
  font-size: 13.5px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease, opacity 0.2s ease;
}

.trial-sms-btn:hover:not(:disabled) {
  background: #5b4be8;
}

.trial-sms-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.trial-consent {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12.5px;
  color: #5f6470;
  cursor: pointer;
  user-select: none;
  margin-top: 2px;
}

.trial-consent__check {
  width: 15px;
  height: 15px;
  accent-color: #07080a;
  cursor: pointer;
}

.trial-consent__link {
  color: #5b4be8;
  text-decoration: none;
}

.trial-consent__link:hover {
  text-decoration: underline;
}

.trial-hint {
  margin: -2px 0 0;
  font-size: 12.5px;
}

.trial-hint--ok {
  color: #16a34a;
}

.trial-hint--err {
  color: #dc2626;
}

.trial-submit-btn {
  margin-top: 4px;
  width: 100%;
  height: 45px;
  border: none;
  border-radius: 4px;
  background: #07080a;
  color: #ffffff;
  font-family: inherit;
  font-size: 14.5px;
  font-weight: 500;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  transition: background-color 0.2s ease;
}

.trial-submit-btn:hover:not(:disabled) {
  background: #5b4be8;
}

.trial-submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.trial-success,
.trial-closed {
  text-align: center;
  padding: 12px 0 4px;
}

.trial-success__icon {
  width: 52px;
  height: 52px;
  margin: 0 auto 14px;
  border-radius: 50%;
  background: #ede9fe;
  color: #5b4be8;
  font-size: 26px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.trial-success__title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2329;
  margin-bottom: 8px;
}

.trial-success__desc,
.trial-closed__desc {
  font-size: 14px;
  color: #5f6470;
  line-height: 1.7;
  margin-bottom: 12px;
}

.trial-closed__item {
  font-size: 14px;
  color: #1f2329;
  margin: 6px 0;
}

.trial-success__receipt {
  font-size: 12.5px;
  color: #8b90a0;
  margin-bottom: 18px;
}
</style>
