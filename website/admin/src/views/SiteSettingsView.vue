<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import ConfigPanel from '@/components/ConfigPanel.vue'
import LinkEditor, { type LinkValue } from '@/components/LinkEditor.vue'
import MediaPicker from '@/components/MediaPicker.vue'
import { useConfig } from '@/composables/useConfig'
import type { MediaItem } from '@/api/content'

interface FooterLink {
  key?: string
  label: string
  enabled: boolean
  sortOrder: number
  link: LinkValue | null
}

interface FooterGroup {
  title: string
  links: FooterLink[]
}

interface SiteDraft {
  siteName: string
  slogan: string
  copyrightText: string
  icpText: string
  icpUrl: string
  policeText: string
  policeUrl: string
  logoMediaId?: number
  logoAlt: string
  faviconMediaId?: number
  defaultSeo: { title: string; description: string }
  footerGroups: FooterGroup[]
}

const { view, loading, saving, publishing, loadError, load, save, publish } =
  useConfig<SiteDraft & Record<string, unknown>>('SITE')

interface SiteForm {
  siteName: string
  slogan: string
  copyrightText: string
  icpText: string
  icpUrl: string
  policeText: string
  policeUrl: string
  logoMediaId: string
  logoAlt: string
  faviconMediaId: string
  seoTitle: string
  seoDescription: string
  footerGroups: FooterGroup[]
}

const values = ref<SiteForm>({
  siteName: '',
  slogan: '',
  copyrightText: '',
  icpText: '',
  icpUrl: '',
  policeText: '',
  policeUrl: '',
  logoMediaId: '' as string,
  logoAlt: '',
  faviconMediaId: '' as string,
  seoTitle: '',
  seoDescription: '',
  footerGroups: [] as FooterGroup[],
})

const pickerOpen = ref(false)
const pickerTarget = ref<'logo' | 'favicon'>('logo')

function emptyLink(): FooterLink {
  return { label: '', enabled: true, sortOrder: 100, link: { type: 'PAGE', pageCode: 'CONTACT' } }
}

function fill() {
  const draft = view.value?.draft
  if (!draft) return
  values.value.siteName = draft.siteName ?? ''
  values.value.slogan = draft.slogan ?? ''
  values.value.copyrightText = draft.copyrightText ?? ''
  values.value.icpText = draft.icpText ?? ''
  values.value.icpUrl = draft.icpUrl ?? ''
  values.value.policeText = draft.policeText ?? ''
  values.value.policeUrl = draft.policeUrl ?? ''
  values.value.logoMediaId = draft.logoMediaId ? String(draft.logoMediaId) : ''
  values.value.logoAlt = draft.logoAlt ?? ''
  values.value.faviconMediaId = draft.faviconMediaId ? String(draft.faviconMediaId) : ''
  values.value.seoTitle = draft.defaultSeo?.title ?? ''
  values.value.seoDescription = draft.defaultSeo?.description ?? ''
  values.value.footerGroups = (draft.footerGroups ?? []).map((group) => ({
    title: group.title ?? '',
    links: (group.links ?? []).map((link) => ({
      key: link.key,
      label: link.label ?? '',
      enabled: link.enabled ?? true,
      sortOrder: link.sortOrder ?? 100,
      link: (link.link as LinkValue | null) ?? { type: 'PAGE', pageCode: 'CONTACT' },
    })),
  }))
  if (values.value.footerGroups.length === 0) {
    values.value.footerGroups = [{ title: '快速入口', links: [emptyLink()] }]
  }
}

function buildDraft(): SiteDraft & Record<string, unknown> {
  const base = { ...(view.value?.draft ?? {}) } as Record<string, unknown>
  return {
    ...base,
    siteName: values.value.siteName.trim(),
    slogan: values.value.slogan.trim(),
    copyrightText: values.value.copyrightText.trim(),
    icpText: values.value.icpText.trim(),
    icpUrl: values.value.icpUrl.trim(),
    policeText: values.value.policeText.trim(),
    policeUrl: values.value.policeUrl.trim(),
    logoMediaId: values.value.logoMediaId ? Number(values.value.logoMediaId) : undefined,
    logoAlt: values.value.logoAlt.trim(),
    faviconMediaId: values.value.faviconMediaId ? Number(values.value.faviconMediaId) : undefined,
    defaultSeo: { title: values.value.seoTitle.trim(), description: values.value.seoDescription.trim() },
    footerGroups: values.value.footerGroups.map((group) => ({
      title: group.title.trim(),
      links: group.links.map((link) => ({ ...link, label: link.label.trim() })),
    })),
  } as SiteDraft & Record<string, unknown>
}

async function onSave() {
  if (!values.value.siteName.trim()) {
    ElMessage.warning('站点名称不能为空')
    return
  }
  if (await save(buildDraft())) fill()
}

async function onPublish() {
  if (await publish()) fill()
}

function addGroup() {
  if (values.value.footerGroups.length >= 4) {
    ElMessage.warning('页脚分组最多 4 组')
    return
  }
  values.value.footerGroups.push({ title: '', links: [emptyLink()] })
}

function addLink(group: FooterGroup) {
  if (group.links.length >= 10) {
    ElMessage.warning('每组最多 10 个链接')
    return
  }
  group.links.push({ ...emptyLink(), sortOrder: (group.links.length + 1) * 10 })
}

function openPicker(target: 'logo' | 'favicon') {
  pickerTarget.value = target
  pickerOpen.value = true
}

function onPick(media: MediaItem) {
  if (pickerTarget.value === 'logo') {
    values.value.logoMediaId = String(media.id)
  } else {
    values.value.faviconMediaId = String(media.id)
  }
  pickerOpen.value = false
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

    <el-form label-width="110px">
      <el-form-item label="站点名称" required>
        <el-input v-model="values.siteName" maxlength="60" show-word-limit />
      </el-form-item>
      <el-form-item label="标语">
        <el-input v-model="values.slogan" maxlength="100" show-word-limit placeholder="显示在页脚，一句话说明业务" />
      </el-form-item>
      <el-form-item label="Logo">
        <div class="media">
          <img v-if="values.logoMediaId" :src="`/api/admin/media/${values.logoMediaId}/file`" alt="站点 Logo" />
          <span v-else class="media__empty">未选择</span>
          <el-button size="small" @click="openPicker('logo')">选择图片</el-button>
          <el-button v-if="values.logoMediaId" size="small" link type="danger" @click="values.logoMediaId = ''">清除</el-button>
        </div>
      </el-form-item>
      <el-form-item label="Logo 替代文字">
        <el-input v-model="values.logoAlt" maxlength="120" placeholder="不填时使用站点名称" />
      </el-form-item>
      <el-form-item label="站点图标">
        <div class="media">
          <img v-if="values.faviconMediaId" :src="`/api/admin/media/${values.faviconMediaId}/file`" alt="站点图标" />
          <span v-else class="media__empty">可选，建议 PNG 小于 200KB</span>
          <el-button size="small" @click="openPicker('favicon')">选择图片</el-button>
          <el-button v-if="values.faviconMediaId" size="small" link type="danger" @click="values.faviconMediaId = ''">清除</el-button>
        </div>
      </el-form-item>

      <el-divider content-position="left">默认 SEO</el-divider>
      <p class="hint">页面没有单独 SEO 时的兜底值，发布时必填。</p>
      <el-form-item label="默认标题">
        <el-input v-model="values.seoTitle" maxlength="80" show-word-limit />
      </el-form-item>
      <el-form-item label="默认描述">
        <el-input v-model="values.seoDescription" type="textarea" :rows="2" maxlength="200" show-word-limit />
      </el-form-item>

      <el-divider content-position="left">页脚</el-divider>
      <el-form-item label="版权文案">
        <el-input v-model="values.copyrightText" maxlength="120" placeholder="© 2026 示例科技有限公司" />
      </el-form-item>
      <el-form-item label="ICP 备案">
        <el-input v-model="values.icpText" maxlength="80" placeholder="京ICP备00000000号" style="width: 260px" />
        <el-input v-model="values.icpUrl" maxlength="500" placeholder="https://beian.miit.gov.cn/" style="width: 300px; margin-left: 8px" />
      </el-form-item>
      <el-form-item label="公安备案">
        <el-input v-model="values.policeText" maxlength="80" placeholder="选填" style="width: 260px" />
        <el-input v-model="values.policeUrl" maxlength="500" placeholder="https://" style="width: 300px; margin-left: 8px" />
      </el-form-item>
    </el-form>

    <el-divider content-position="left">页脚分组</el-divider>
    <p class="hint">最多 4 组，每组最多 10 个链接。空组不会出现在前台。</p>

    <div v-for="(group, groupIndex) in values.footerGroups" :key="`group-${groupIndex}`" class="group">
      <div class="group__head">
        <el-input v-model="group.title" maxlength="20" placeholder="分组标题" style="width: 180px" />
        <el-button link @click="addLink(group)">添加链接</el-button>
        <el-button link type="danger" @click="values.footerGroups.splice(groupIndex, 1)">删除分组</el-button>
      </div>
      <div v-for="(link, linkIndex) in group.links" :key="link.key ?? `link-${groupIndex}-${linkIndex}`" class="group__row">
        <el-switch v-model="link.enabled" />
        <el-input v-model="link.label" maxlength="20" placeholder="链接名称" style="width: 140px" />
        <LinkEditor v-model="link.link" :allow-none="false" />
        <el-button link type="danger" @click="group.links.splice(linkIndex, 1)">删除</el-button>
      </div>
    </div>
    <el-button class="add" @click="addGroup">添加分组</el-button>

    <MediaPicker v-model="pickerOpen" :selected-id="null" @select="onPick" />

    <template #tips>
      <p>电话、邮箱、地址在「联系我们」页维护，这里不重复填写。</p>
      <p>备案号与备案链接需成对填写，只填一个会在发布时被拒绝。</p>
    </template>
  </ConfigPanel>
</template>

<style scoped>
.err {
  margin-bottom: 12px;
}

.hint {
  font-size: 12px;
  color: var(--a-text-muted);
  margin: 0 0 12px 110px;
}

.media {
  display: flex;
  align-items: center;
  gap: 10px;
}

.media img,
.media__empty {
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

.group {
  border: 1px solid var(--a-border);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
}

.group__head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.group__row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding: 4px 0;
}

.add {
  margin-top: 4px;
}
</style>
