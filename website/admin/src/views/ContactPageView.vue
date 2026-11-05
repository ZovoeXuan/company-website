<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import ConfigPanel from '@/components/ConfigPanel.vue'
import { useConfig } from '@/composables/useConfig'

interface ContactDraft {
  title: string
  description: string
  successText: string
  formEnabled: boolean
  phone: string
  email: string
  address: string
  hours: string
}

const { view, loading, saving, publishing, loadError, load, save, publish } =
  useConfig<ContactDraft & Record<string, unknown>>('CONTACT')

const form = reactive<ContactDraft>({
  title: '联系我们',
  description: '',
  successText: '',
  formEnabled: false,
  phone: '',
  email: '',
  address: '',
  hours: '',
})

function fill() {
  const draft = view.value?.draft
  form.title = draft?.title || '联系我们'
  form.description = draft?.description ?? ''
  form.successText = draft?.successText ?? ''
  form.formEnabled = draft?.formEnabled ?? false
  form.phone = draft?.phone ?? ''
  form.email = draft?.email ?? ''
  form.address = draft?.address ?? ''
  form.hours = draft?.hours ?? ''
}

function validate(): boolean {
  if (!form.phone.trim() && !form.email.trim()) {
    ElMessage.warning('联系电话与邮箱请至少填写一项')
    return false
  }
  if (form.email.trim() && !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(form.email.trim())) {
    ElMessage.warning('邮箱格式不正确')
    return false
  }
  return true
}

async function onSave() {
  if (!validate()) return
  const draft = {
    ...(view.value?.draft ?? {}),
    title: form.title.trim() || '联系我们',
    description: form.description.trim(),
    successText: form.successText.trim(),
    formEnabled: form.formEnabled,
    phone: form.phone.trim(),
    email: form.email.trim(),
    address: form.address.trim(),
    hours: form.hours.trim(),
  } as ContactDraft & Record<string, unknown>
  if (await save(draft)) fill()
}

async function onPublish() {
  if (!validate()) return
  if (await publish()) fill()
}

onMounted(async () => {
  await load()
  fill()
})
</script>

<template>
  <ConfigPanel
    title="联系我们与全站咨询开关"
    :status="view?.status"
    :draft-version="view?.draftVersion"
    :published-version="view?.publishedVersion"
    :has-unpublished-changes="view?.hasUnpublishedChanges"
    :loading="loading"
    :saving="saving"
    :publishing="publishing"
    @save="onSave"
    @publish="onPublish"
  >
    <el-alert v-if="loadError" type="error" :closable="false" :title="loadError" class="err" />

    <el-form label-width="120px">
      <el-form-item label="页面标题">
        <el-input v-model="form.title" maxlength="50" show-word-limit />
      </el-form-item>
      <el-form-item label="页面导语说明">
        <el-input v-model="form.description" type="textarea" :rows="2" maxlength="300" show-word-limit />
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="form.phone" maxlength="40" placeholder="例如：400-888-0000" style="width: 280px" />
      </el-form-item>
      <el-form-item label="联系邮箱">
        <el-input v-model="form.email" maxlength="100" placeholder="例如：connect@company.com" style="width: 280px" />
      </el-form-item>
      <el-form-item label="办公地址">
        <el-input v-model="form.address" maxlength="120" show-word-limit />
      </el-form-item>
      <el-form-item label="服务时间">
        <el-input v-model="form.hours" maxlength="60" placeholder="例如：周一至周日 08:30 - 20:30" style="width: 320px" />
      </el-form-item>

      <el-divider content-position="left">在线咨询表单控制</el-divider>
      <el-form-item label="开启在线咨询">
        <el-switch v-model="form.formEnabled" active-text="允许访客在线提交咨询线索" inactive-text="仅展示联系方式" />
      </el-form-item>
      <el-form-item label="提交成功回执语">
        <el-input
          v-model="form.successText"
          type="textarea"
          :rows="2"
          maxlength="200"
          show-word-limit
          placeholder="感谢您的信任！我们的行业专家将在 1 个工作日内与您联系。"
        />
      </el-form-item>
    </el-form>

    <template #tips>
      <p>此处维护的电话、邮箱、地址与服务时间将同步应用于官网「联系我们」页、全站页脚及右下角磁吸悬浮坞。</p>
      <p>开启在线咨询前，请确保「隐私说明」页面已有正式发布版本，访客提交的手机号将在服务端以 AES-256-GCM 加密存储。</p>
    </template>
  </ConfigPanel>
</template>

<style scoped>
.err {
  margin-bottom: 12px;
}
</style>
