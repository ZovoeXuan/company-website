<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import ConfigPanel from '@/components/ConfigPanel.vue'
import MediaPicker from '@/components/MediaPicker.vue'
import { useConfig } from '@/composables/useConfig'

interface SiteDraft {
  siteName: string
  slogan: string
  copyright: string
  icp: string
  logoMediaId?: number
  contact: { phone: string; email: string; address: string; hours: string }
}

const { view, loading, saving, publishing, loadError, load, save, publish } = useConfig<SiteDraft & Record<string, unknown>>('SITE')

const form = reactive({
  siteName: '',
  slogan: '',
  copyright: '',
  icp: '',
  logoMediaId: '' as string,
  phone: '',
  email: '',
  address: '',
  hours: '',
})
const pickerOpen = ref(false)

function fill() {
  const draft = view.value?.draft
  if (!draft) return
  form.siteName = draft.siteName ?? ''
  form.slogan = draft.slogan ?? ''
  form.copyright = draft.copyright ?? ''
  form.icp = draft.icp ?? ''
  form.logoMediaId = draft.logoMediaId ? String(draft.logoMediaId) : ''
  form.phone = draft.contact?.phone ?? ''
  form.email = draft.contact?.email ?? ''
  form.address = draft.contact?.address ?? ''
  form.hours = draft.contact?.hours ?? ''
}

function buildDraft(): SiteDraft & Record<string, unknown> {
  const base = { ...(view.value?.draft ?? {}) } as Record<string, unknown>
  return {
    ...base,
    siteName: form.siteName.trim(),
    slogan: form.slogan.trim(),
    copyright: form.copyright.trim(),
    icp: form.icp.trim(),
    logoMediaId: form.logoMediaId ? Number(form.logoMediaId) : undefined,
    contact: {
      phone: form.phone.trim(),
      email: form.email.trim(),
      address: form.address.trim(),
      hours: form.hours.trim(),
    },
  } as SiteDraft & Record<string, unknown>
}

async function onSave() {
  if (!form.siteName.trim()) {
    ElMessage.warning('站点名称不能为空')
    return
  }
  if (await save(buildDraft())) fill()
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
    title="全站设置"
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
      <el-form-item label="站点名称" required>
        <el-input v-model="form.siteName" maxlength="50" show-word-limit />
      </el-form-item>
      <el-form-item label="标语">
        <el-input v-model="form.slogan" maxlength="100" show-word-limit placeholder="显示在页脚，一句话说明业务" />
      </el-form-item>
      <el-form-item label="站点标识">
        <div class="logo">
          <img v-if="form.logoMediaId" :src="`/api/admin/media/${form.logoMediaId}/file`" alt="站点标识" />
          <span v-else class="logo__empty">未选择</span>
          <el-button size="small" @click="pickerOpen = true">选择图片</el-button>
          <el-button v-if="form.logoMediaId" size="small" link type="danger" @click="form.logoMediaId = ''">清除</el-button>
        </div>
      </el-form-item>

      <el-divider content-position="left">联系方式</el-divider>
      <p class="hint">这里维护的联系方式会同时用于页头和联系页，只需改一处。</p>
      <el-form-item label="电话">
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

      <el-divider content-position="left">页脚</el-divider>
      <el-form-item label="版权信息">
        <el-input v-model="form.copyright" maxlength="120" placeholder="© 2026 示例科技有限公司" />
      </el-form-item>
      <el-form-item label="备案号">
        <el-input v-model="form.icp" maxlength="60" />
      </el-form-item>
    </el-form>

    <MediaPicker v-model="pickerOpen" :selected-id="form.logoMediaId" @select="(m) => { form.logoMediaId = m.id; pickerOpen = false }" />
  </ConfigPanel>
</template>

<style scoped>
.err {
  margin-bottom: 16px;
}

.hint {
  font-size: 12px;
  color: var(--a-text-muted);
  margin: 0 0 12px 100px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo img,
.logo__empty {
  width: 72px;
  height: 48px;
  border: 1px solid var(--a-border);
  border-radius: 6px;
  object-fit: contain;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: var(--a-text-muted);
  background: #f6f8fa;
}
</style>
