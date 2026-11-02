<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import ConfigPanel from '@/components/ConfigPanel.vue'
import { useConfig } from '@/composables/useConfig'

interface ContactDraft {
  title: string
  description: string
  successText: string
  formEnabled: boolean
}

const { view, loading, saving, publishing, loadError, load, save, publish } =
  useConfig<ContactDraft & Record<string, unknown>>('CONTACT')

const form = reactive<ContactDraft>({ title: '联系我们', description: '', successText: '', formEnabled: false })

function fill() {
  const draft = view.value?.draft
  form.title = draft?.title || '联系我们'
  form.description = draft?.description ?? ''
  form.successText = draft?.successText ?? ''
  form.formEnabled = draft?.formEnabled ?? false
}

async function onSave() {
  const draft = {
    ...(view.value?.draft ?? {}),
    title: form.title,
    description: form.description,
    successText: form.successText,
    formEnabled: form.formEnabled,
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

      <el-divider content-position="left">咨询表单</el-divider>
      <el-form-item label="启用表单">
        <el-switch v-model="form.formEnabled" />
        <span class="hint">关闭后前台显示联系方式，不展示表单。提交接口在阶段 6 接入。</span>
      </el-form-item>
      <el-form-item label="提交成功提示">
        <el-input v-model="form.successText" maxlength="200" placeholder="我们会在一个工作日内联系你" />
      </el-form-item>
    </el-form>

    <template #tips>
      <p>电话、邮箱、地址统一在「全站设置」维护，这里不重复填写，避免两处不一致。</p>
    </template>
  </ConfigPanel>
</template>

<style scoped>
.err {
  margin-bottom: 16px;
}

.hint {
  margin-left: 12px;
  font-size: 12px;
  color: var(--a-text-muted);
}
</style>
