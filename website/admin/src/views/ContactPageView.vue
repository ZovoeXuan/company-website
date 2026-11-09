<!-- 对应源码路径: website/admin/src/views/ContactPageView.vue -->
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import ConfigPanel from '@/components/ConfigPanel.vue'
import { useConfig } from '@/composables/useConfig'

interface ContactDraft {
  title: string
  companyName: string
  description: string
  formTitle: string
  successText: string
  formEnabled: boolean
  smsVerifyEnabled: boolean
  phone: string
  email: string
  address: string
  hours: string
  mapUrl: string
}

const { view, loading, saving, publishing, loadError, load, save, publish } =
  useConfig<ContactDraft & Record<string, unknown>>('CONTACT')

const form = ref<ContactDraft>({
  title: '联系我们',
  companyName: '',
  description: '',
  formTitle: '预约产品展示',
  successText: '预约成功！我们的产品专家将在 1 个工作日内与您联系。',
  formEnabled: true,
  smsVerifyEnabled: true,
  phone: '',
  email: '',
  address: '',
  hours: '',
  mapUrl: '',
})

function fill() {
  const draft = view.value?.draft
  form.value.title = draft?.title || '联系我们'
  form.value.companyName = (draft?.companyName as string) ?? ''
  form.value.description = draft?.description ?? ''
  form.value.formTitle = (draft?.formTitle as string) || '预约产品展示'
  form.value.successText = draft?.successText ?? ''
  form.value.formEnabled = draft?.formEnabled ?? true
  form.value.smsVerifyEnabled = (draft?.smsVerifyEnabled as boolean) ?? true
  form.value.phone = draft?.phone ?? ''
  form.value.email = draft?.email ?? ''
  form.value.address = draft?.address ?? ''
  form.value.hours = draft?.hours ?? ''
  form.value.mapUrl = (draft?.mapUrl as string) ?? ''
}

function validate(): boolean {
  if (!form.value.phone.trim() && !form.value.email.trim()) {
    ElMessage.warning('联系电话与邮箱请至少填写一项')
    return false
  }
  if (form.value.email.trim() && !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(form.value.email.trim())) {
    ElMessage.warning('邮箱格式不正确')
    return false
  }
  return true
}

async function onSave() {
  if (!validate()) return
  const draft = {
    ...(view.value?.draft ?? {}),
    title: form.value.title.trim() || '联系我们',
    companyName: form.value.companyName.trim(),
    description: form.value.description.trim(),
    formTitle: form.value.formTitle.trim() || '预约产品展示',
    successText: form.value.successText.trim(),
    formEnabled: form.value.formEnabled,
    smsVerifyEnabled: form.value.smsVerifyEnabled,
    phone: form.value.phone.trim(),
    email: form.value.email.trim(),
    address: form.value.address.trim(),
    hours: form.value.hours.trim(),
    mapUrl: form.value.mapUrl.trim(),
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
    title="联系我们与全站咨询开关（开发文档 6.12）"
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

    <el-form label-width="140px">
      <el-form-item label="页面标题">
        <el-input v-model="form.title" maxlength="50" show-word-limit style="width: 320px" />
      </el-form-item>
      <el-form-item label="公司全称">
        <el-input v-model="form.companyName" maxlength="100" show-word-limit style="width: 420px" />
      </el-form-item>
      <el-form-item label="页面导语说明">
        <el-input v-model="form.description" type="textarea" :rows="2" maxlength="300" show-word-limit />
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="form.phone" maxlength="40" placeholder="支持座机、400 或手机号" style="width: 280px" />
      </el-form-item>
      <el-form-item label="联系邮箱">
        <el-input v-model="form.email" maxlength="100" placeholder="connect@company.com" style="width: 280px" />
      </el-form-item>
      <el-form-item label="办公地址">
        <el-input v-model="form.address" maxlength="200" show-word-limit />
      </el-form-item>
      <el-form-item label="服务时间">
        <el-input v-model="form.hours" maxlength="100" placeholder="周一至周日 08:30 - 20:30" style="width: 320px" />
      </el-form-item>
      <el-form-item label="地图外链 (选填)">
        <el-input v-model="form.mapUrl" maxlength="500" placeholder="https://..." />
      </el-form-item>

      <el-divider content-position="left">在线咨询表单控制（对应开发文档 6.12 & 7.1）</el-divider>
      <el-form-item label="开启在线咨询表单">
        <el-switch
          v-model="form.formEnabled"
          active-text="开启（允许访客提交咨询线索）"
          inactive-text="关闭（前台隐藏表单并引导至联系页，接口返回 503）"
        />
      </el-form-item>
      <el-form-item label="显示手机验证码框">
        <el-switch
          v-model="form.smsVerifyEnabled"
          active-text="显示（还原《预约产品展示.png》样式）"
          inactive-text="隐藏（标准 4 字段模式）"
        />
      </el-form-item>
      <el-form-item label="弹窗表单标题">
        <el-input v-model="form.formTitle" maxlength="40" style="width: 280px" placeholder="预约产品展示" />
      </el-form-item>
      <el-form-item label="提交成功回执语">
        <el-input
          v-model="form.successText"
          type="textarea"
          :rows="2"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
    </el-form>
  </ConfigPanel>
</template>

<style scoped>
.err {
  margin-bottom: 12px;
}
</style>
