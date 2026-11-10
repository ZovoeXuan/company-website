<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import ConfigPanel from '@/components/ConfigPanel.vue'
import { useConfig } from '@/composables/useConfig'

interface PrivacyDraft {
  updatedAt: string
  bodyHtml: string
}

const { view, loading, saving, publishing, loadError, load, save, publish } =
  useConfig<PrivacyDraft & Record<string, unknown>>('PRIVACY')

const form = reactive<PrivacyDraft>({ updatedAt: '', bodyHtml: '' })

function fill() {
  const draft = view.value?.draft
  form.updatedAt = draft?.updatedAt ?? ''
  form.bodyHtml = draft?.bodyHtml ?? ''
  if (!form.updatedAt) {
    form.updatedAt = new Date().toISOString().slice(0, 10)
  }
}

async function onSave(): Promise<boolean> {
  const draft = {
    ...(view.value?.draft ?? {}),
    updatedAt: form.updatedAt,
    bodyHtml: form.bodyHtml,
  } as PrivacyDraft & Record<string, unknown>
  const ok = await save(draft)
  if (ok) fill()
  return ok
}

async function onPublish() {
  const saved = await onSave()
  if (saved === false) return
  if (await publish()) fill()
}

onMounted(async () => {
  await load()
  fill()
})
</script>

<template>
  <ConfigPanel
    title="隐私说明"
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

    <el-form label-width="100px">
      <el-form-item label="更新日期">
        <el-date-picker v-model="form.updatedAt" type="date" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item label="正文">
        <el-input
          v-model="form.bodyHtml"
          type="textarea"
          :rows="14"
          placeholder="支持基础 HTML，保存时会按白名单清洗，脚本与事件属性会被移除"
        />
      </el-form-item>
    </el-form>

    <template #tips>
      <p>正文在保存时经过白名单清洗：只保留排版类标签，`script`、`iframe`、`on*` 事件与 `javascript:` 链接会被移除。</p>
      <p>正式上线前需由法务确认条款内容。</p>
    </template>
  </ConfigPanel>
</template>

<style scoped>
.err {
  margin-bottom: 16px;
}
</style>
