<script setup lang="ts">
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
  privacyVersion: 1 as number | null,
  successText: '',
})

const form = reactive({
  name: '',
  phone: '',
  smsCode: '',
  companyName: '',
  demand: props.initialDemand || '',
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

function sendSmsCode() {
  errorMsg.value = ''
  smsNotice.value = ''
  if (!form.phone.trim() || !/^1[3-9]\d{9}$/.test(form.phone.trim())) {
    errorMsg.value = '请先输入有效的 11 位手机号码'
    return
  }
  if (countdown.value > 0) return
  countdown.value = 60
  if (!form.smsCode) {
    form.smsCode = '8866'
  }
  smsNotice.value = '验证码已发送（演示验证码：8866）'
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
    context.privacyVersion = data.privacyVersion ?? 1
    context.successText = data.successText
  } catch {
    context.loaded = true
    context.formEnabled = true
  }
})

onBeforeUnmount(() => {
  if (smsTimer) clearInterval(smsTimer)
})

function newKey(): string {
  return globalThis.crypto?.randomUUID
    ? globalThis.crypto.randomUUID()
    : 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
        const r = (Math.random() * 16) | 0
        const v = c === 'x' ? r : (r & 0x3) | 0x8
        return v.toString(16)
      })
}

async function onSubmit() {
  errorMsg.value = ''
  if (!form.name.trim()) {
    errorMsg.value = '请填写您的姓名'
    return
  }
  if (!form.phone.trim() || !/^1[3-9]\d{9}$/.test(form.phone.trim())) {
    errorMsg.value = '请输入有效的 11 位联系电话'
    return
  }
  if (!form.smsCode.trim()) {
    errorMsg.value = '请输入手机验证码'
    return
  }
  if (!form.companyName.trim()) {
    errorMsg.value = '请填写公司名称'
    return
  }

  submitting.value = true
  try {
    const res = await submitLead(
      {
        name: form.name.trim(),
        phone: form.phone.trim(),
        companyName: form.companyName.trim(),
        demand: form.demand.trim() || '预约产品展示',
        sourcePath: route.fullPath || '/',
        sourcePosition: props.sourcePosition || 'NAVIGATION',
        contentType: props.contentType,
        contentId: props.contentId ? Number(props.contentId) : undefined,
        consent: true,
        consentVersion: context.privacyVersion ?? 1,
      },
      newKey(),
    )
    succeeded.value = true
    receiptNo.value = res.receiptNo
  } catch (err) {
    errorMsg.value = err instanceof ApiError ? err.message : '提交失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

function resetAndClose() {
  succeeded.value = false
  form.name = ''
  form.phone = ''
  form.smsCode = ''
  form.companyName = ''
  form.demand = ''
  closeConsult()
}
</script>

<template>
  <div v-if="succeeded" class="trial-success">
    <div class="trial-success__icon">✓</div>
    <h3 class="trial-success__title">预约提交成功</h3>
    <p class="trial-success__desc">
      {{ context.successText || '我们的产品专家将在 1 个工作日内与您联系，为您安排专属产品演示。' }}
    </p>
    <p v-if="receiptNo" class="trial-success__receipt">回执单号：{{ receiptNo }}</p>
    <button type="button" class="trial-submit-btn" @click="resetAndClose">完成</button>
  </div>

  <form v-else class="trial-form" novalidate @submit.prevent="onSubmit">
    <div class="trial-field">
      <input
        v-model="form.name"
        type="text"
        class="trial-input"
        placeholder="姓名"
        maxlength="40"
        autocomplete="name"
      />
    </div>

    <div class="trial-field">
      <input
        v-model="form.phone"
        type="tel"
        class="trial-input"
        placeholder="联系电话"
        maxlength="20"
        autocomplete="tel"
      />
    </div>

    <div class="trial-field trial-field--sms">
      <input
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
      <input
        v-model="form.companyName"
        type="text"
        class="trial-input"
        placeholder="公司名称"
        maxlength="80"
        autocomplete="organization"
      />
    </div>

    <div class="trial-field">
      <textarea
        v-model="form.demand"
        class="trial-textarea"
        rows="3"
        placeholder="需求描述"
        maxlength="500"
      ></textarea>
    </div>

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
  gap: 14px;
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
  height: 46px;
}

.trial-textarea {
  padding: 12px 16px;
  min-height: 84px;
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
  height: 46px;
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
  margin-top: 8px;
  width: 100%;
  height: 46px;
  border: none;
  border-radius: 4px;
  background: #07080a;
  color: #ffffff;
  font-family: inherit;
  font-size: 14.5px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.trial-submit-btn:hover:not(:disabled) {
  background: #5b4be8;
}

.trial-submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.trial-success {
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

.trial-success__desc {
  font-size: 14px;
  color: #5f6470;
  line-height: 1.7;
  margin-bottom: 10px;
}

.trial-success__receipt {
  font-size: 12.5px;
  color: #8b90a0;
  margin-bottom: 18px;
}
</style>
