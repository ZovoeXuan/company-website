<script setup lang="ts">
import { onMounted, reactive } from 'vue'
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

async function onSave() {
  const draft = {
    ...(view.value?.draft ?? {}),
    title: form.title,
    description: form.description,
    successText: form.successText,
    formEnabled: form.formEnabled,
    phone: form.phone,
    email: form.email,
    address: form.address,
    hours: form.hours,
  } as ContactDraft & Record<string, unknown>
  if (await save(draft)) fill()
}

async function onPublish() {
  if (await publish()) fill()
}

onMounted(async () => {
  await load()
  fill()
})
</script>

<template>
  <ConfigPanel
    title="联系我们"
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

    <el-form label-width="110px">
      <el-form-item label="页面标题">
        <el-input v-model="form.title" maxlength="50" />
      </el-form-item>
      <el-form-item label="页面描述">
        <el-input v-model="form.description" type="textarea" :rows="3" maxlength="300" show-word-limit />
      </el-form-item>

      <el-divider content-position="left">联系方式</el-divider>
      <p class="hint">全站页头、页脚与联系页共用这一处，改一次即可全站生效。</p>
      <el-form-item label="联系电话">
        <el-input v-model="form.phone" maxlength="40" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" maxlength="100" />
      </el-form-item>
      <el-form-item label="地址">
        <el-input v-model="form.address" maxlength="120" />
      </el-form-item>
      <el-form-item label="服务时间">
        <el-input v-model="form.hours" maxlength="60" placeholder="例如：工作日 9:00–18:00" />
      </el-form-item>

      <el-divider content-position="left">咨询表单</el-divider>
      <el-form-item label="启用表单">
        <el-switch v-model="form.formEnabled" />
        <span class="hint-inline">关闭后前台只显示联系方式，不展示表单，首页咨询按钮也会退到联系页。</span>
      </el-form-item>
      <el-form-item label="提交成功提示">
        <el-input v-model="form.successText" maxlength="200" placeholder="我们会在一个工作日内联系你" />
      </el-form-item>
    </el-form>

    <template #tips>
      <p>发布时联系电话与邮箱至少填写一项。</p>
      <p>表单提交接口在阶段 6 接入，当前前台只做前端校验并明确提示未发送。</p>
    </template>
  </ConfigPanel>
</template>

<style scoped>
.err {
  margin-bottom: 16px;
}

.hint {
  font-size: 12px;
  color: var(--a-text-muted);
  margin: 0 0 12px 110px;
}

.hint-inline {
  margin-left: 12px;
  font-size: 12px;
  color: var(--a-text-muted);
}
</style>
