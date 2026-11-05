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

interface QrCodeConfig {
  label: string
  mediaId?: string
  imageUrl?: string
}

interface SiteDraft {
  siteName: string
  slogan: string
  copyrightText: string
  icpText: string
  icpUrl: string
  policeText: string
  policeUrl: string
  logoMediaId?: number | null
  logoAlt: string
  faviconMediaId?: number | null
  defaultSeo: { title: string; description: string }
  footerGroups: FooterGroup[]
  qrCodes?: QrCodeConfig[]
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
  qrCodes: QrCodeConfig[]
}

const values = ref<SiteForm>({
  siteName: '',
  slogan: '',
  copyrightText: '',
  icpText: '',
  icpUrl: '',
  policeText: '',
  policeUrl: '',
  logoMediaId: '',
  logoAlt: '',
  faviconMediaId: '',
  seoTitle: '',
  seoDescription: '',
  footerGroups: [],
  qrCodes: [
    { label: '公众号', mediaId: '', imageUrl: '' },
    { label: '视频号', mediaId: '', imageUrl: '' },
  ],
})

const pickerOpen = ref(false)
const pickerTarget = ref<'logo' | 'favicon' | 'qr0' | 'qr1'>('logo')

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
  if (Array.isArray(draft.qrCodes) && draft.qrCodes.length > 0) {
    values.value.qrCodes = draft.qrCodes.map((q) => ({
      label: q.label ?? '公众号',
      mediaId: q.mediaId ? String(q.mediaId) : '',
      imageUrl: q.imageUrl ?? (q.mediaId ? `/api/public/media/${q.mediaId}` : ''),
    }))
  }
}

function buildDraft(): SiteDraft & Record<string, unknown> {
  const base = { ...(view.value?.draft ?? {}) } as Record<string, unknown>
  delete base.logoMediaId
  delete base.faviconMediaId
  return {
    ...base,
    siteName: values.value.siteName.trim(),
    slogan: values.value.slogan.trim(),
    copyrightText: values.value.copyrightText.trim(),
    icpText: values.value.icpText.trim(),
    icpUrl: values.value.icpUrl.trim(),
    policeText: values.value.policeText.trim(),
    policeUrl: values.value.policeUrl.trim(),
    logoMediaId: values.value.logoMediaId ? Number(values.value.logoMediaId) : null,
    logoAlt: values.value.logoAlt.trim(),
    faviconMediaId: values.value.faviconMediaId ? Number(values.value.faviconMediaId) : null,
    defaultSeo: {
      title: values.value.seoTitle.trim(),
      description: values.value.seoDescription.trim(),
    },
    footerGroups: values.value.footerGroups.map((group) => ({
      title: group.title.trim(),
      links: group.links.map((link, idx) => ({
        ...link,
        label: link.label.trim(),
        sortOrder: (idx + 1) * 10,
      })),
    })),
    qrCodes: values.value.qrCodes.map((q) => ({
      label: q.label.trim() || '公众号',
      mediaId: q.mediaId || undefined,
      imageUrl: q.mediaId ? `/api/public/media/${q.mediaId}` : q.imageUrl || undefined,
    })),
  } as SiteDraft & Record<string, unknown>
}

function validateBeforeSave(): boolean {
  if (!values.value.siteName.trim()) {
    ElMessage.warning('站点名称不能为空')
    return false
  }
  const hasIcpText = Boolean(values.value.icpText.trim())
  const hasIcpUrl = Boolean(values.value.icpUrl.trim())
  if (hasIcpText !== hasIcpUrl) {
    ElMessage.warning('ICP 备案号与备案链接需成对填写（或同时留空）')
    return false
  }
  const hasPoliceText = Boolean(values.value.policeText.trim())
  const hasPoliceUrl = Boolean(values.value.policeUrl.trim())
  if (hasPoliceText !== hasPoliceUrl) {
    ElMessage.warning('公安备案号与备案链接需成对填写（或同时留空）')
    return false
  }
  for (let i = 0; i < values.value.footerGroups.length; i++) {
    const g = values.value.footerGroups[i]
    if (!g.title.trim()) {
      ElMessage.warning(`页脚第 ${i + 1} 组标题不能为空`)
      return false
    }
    for (let j = 0; j < g.links.length; j++) {
      if (!g.links[j].label.trim()) {
        ElMessage.warning(`页脚分组「${g.title}」第 ${j + 1} 个链接名称不能为空`)
        return false
      }
    }
  }
  return true
}

async function onSave() {
  if (!validateBeforeSave()) return
  if (await save(buildDraft())) fill()
}

async function onPublish() {
  if (!validateBeforeSave()) return
  if (await publish()) fill()
}

function addGroup() {
  if (values.value.footerGroups.length >= 4) {
    ElMessage.warning('页脚分组最多 4 组')
    return
  }
  values.value.footerGroups.push({ title: '', links: [emptyLink()] })
}

function moveGroup(idx: number, delta: number) {
  const target = idx + delta
  if (target < 0 || target >= values.value.footerGroups.length) return
  const [item] = values.value.footerGroups.splice(idx, 1)
  values.value.footerGroups.splice(target, 0, item)
}

function addLink(group: FooterGroup) {
  if (group.links.length >= 10) {
    ElMessage.warning('每组最多 10 个链接')
    return
  }
  group.links.push({ ...emptyLink(), sortOrder: (group.links.length + 1) * 10 })
}

function moveLink(group: FooterGroup, idx: number, delta: number) {
  const target = idx + delta
  if (target < 0 || target >= group.links.length) return
  const [item] = group.links.splice(idx, 1)
  group.links.splice(target, 0, item)
}

function openPicker(target: 'logo' | 'favicon' | 'qr0' | 'qr1') {
  pickerTarget.value = target
  pickerOpen.value = true
}

function onPick(media: MediaItem) {
  if (pickerTarget.value === 'logo') {
    values.value.logoMediaId = String(media.id)
  } else if (pickerTarget.value === 'favicon') {
    values.value.faviconMediaId = String(media.id)
  } else if (pickerTarget.value === 'qr0') {
    values.value.qrCodes[0].mediaId = String(media.id)
    values.value.qrCodes[0].imageUrl = `/api/public/media/${media.id}`
  } else if (pickerTarget.value === 'qr1') {
    values.value.qrCodes[1].mediaId = String(media.id)
    values.value.qrCodes[1].imageUrl = `/api/public/media/${media.id}`
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

      <el-divider content-position="left">默认 SEO 与搜索结果实时预览</el-divider>
      <p class="hint">页面没有单独 SEO 时的兜底值，发布时必填。</p>
      <el-form-item label="默认标题">
        <el-input v-model="values.seoTitle" maxlength="80" show-word-limit />
      </el-form-item>
      <el-form-item label="默认描述">
        <el-input v-model="values.seoDescription" type="textarea" :rows="2" maxlength="200" show-word-limit />
      </el-form-item>

      <!-- Google SERP & 社交分享卡片实时预览 -->
      <div class="serp-preview-wrap">
        <div class="serp-card">
          <span class="serp-card__badge">Google / 百度搜索结果预览 (SERP)</span>
          <p class="serp-card__url">https://www.company-domain.com › 首页</p>
          <h4 class="serp-card__title">
            {{ values.seoTitle || values.siteName || '企业官方网站' }} - {{ values.siteName || '品牌名称' }}
          </h4>
          <p class="serp-card__desc">
            {{ values.seoDescription || values.slogan || '请在上方填写默认 SEO 描述文案，建议控制在 80～160 字以内，提升自然搜索点击率。' }}
          </p>
        </div>
      </div>

      <el-divider content-position="left">页脚备案与关注二维码</el-divider>
      <el-form-item label="版权文案">
        <el-input v-model="values.copyrightText" maxlength="120" placeholder="© 2026 示例科技有限公司" />
      </el-form-item>
      <el-form-item label="ICP 备案">
        <el-input v-model="values.icpText" maxlength="80" placeholder="京ICP备00000000号" style="width: 260px" />
        <el-input v-model="values.icpUrl" maxlength="500" placeholder="https://beian.miit.gov.cn/" style="width: 300px; margin-left: 8px" />
      </el-form-item>
      <el-form-item label="公安备案">
        <el-input v-model="values.policeText" maxlength="80" placeholder="选填（需与链接成对填写）" style="width: 260px" />
        <el-input v-model="values.policeUrl" maxlength="500" placeholder="https://" style="width: 300px; margin-left: 8px" />
      </el-form-item>
      <el-form-item label="页脚双二维码">
        <div class="qr-config-row">
          <div v-for="(qr, qIdx) in values.qrCodes" :key="qIdx" class="qr-config-box">
            <img v-if="qr.mediaId" :src="`/api/admin/media/${qr.mediaId}/file`" :alt="qr.label" />
            <span v-else class="media__empty">默认矢量图</span>
            <el-input v-model="qr.label" maxlength="12" style="width: 96px" placeholder="标签" />
            <el-button size="small" @click="openPicker(qIdx === 0 ? 'qr0' : 'qr1')">选二维码</el-button>
            <el-button v-if="qr.mediaId" size="small" link type="danger" @click="qr.mediaId = ''; qr.imageUrl = ''">
              重置
            </el-button>
          </div>
        </div>
      </el-form-item>
    </el-form>

    <el-divider content-position="left">页脚分组（支持上下排序）</el-divider>
    <p class="hint">最多 4 组，每组最多 10 个链接。空组不会出现在前台。</p>

    <div v-for="(group, groupIndex) in values.footerGroups" :key="`group-${groupIndex}`" class="group">
      <div class="group__head">
        <el-input v-model="group.title" maxlength="20" placeholder="分组标题" style="width: 180px" />
        <el-button link :disabled="groupIndex === 0" @click="moveGroup(groupIndex, -1)">↑ 上移分组</el-button>
        <el-button link :disabled="groupIndex === values.footerGroups.length - 1" @click="moveGroup(groupIndex, 1)">
          ↓ 下移分组
        </el-button>
        <el-button link type="primary" @click="addLink(group)">+ 添加链接</el-button>
        <el-button link type="danger" @click="values.footerGroups.splice(groupIndex, 1)">删除分组</el-button>
      </div>
      <div v-for="(link, linkIndex) in group.links" :key="link.key ?? `link-${groupIndex}-${linkIndex}`" class="group__row">
        <el-switch v-model="link.enabled" />
        <el-input v-model="link.label" maxlength="20" placeholder="链接名称" style="width: 140px" />
        <LinkEditor v-model="link.link" :allow-none="false" :allow-consult="true" />
        <el-button link :disabled="linkIndex === 0" @click="moveLink(group, linkIndex, -1)">上移</el-button>
        <el-button link :disabled="linkIndex === group.links.length - 1" @click="moveLink(group, linkIndex, 1)">下移</el-button>
        <el-button link type="danger" @click="group.links.splice(linkIndex, 1)">删除</el-button>
      </div>
    </div>
    <el-button class="add" @click="addGroup">+ 添加页脚分组</el-button>

    <MediaPicker v-model="pickerOpen" :selected-id="null" @select="onPick" />

    <template #tips>
      <p>电话、邮箱、地址在「联系我们」页维护，这里不重复填写。</p>
      <p>备案号与备案链接需成对填写，只填一个会在保存或发布时被拦截提示。</p>
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

.serp-preview-wrap {
  margin: 0 0 20px 110px;
}

.serp-card {
  max-width: 620px;
  background: #ffffff;
  border: 1px solid var(--a-border);
  border-radius: 10px;
  padding: 14px 18px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
}

.serp-card__badge {
  display: inline-block;
  font-size: 11px;
  color: #5b4be8;
  background: #eeebff;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 600;
  margin-bottom: 6px;
}

.serp-card__url {
  margin: 0;
  font-size: 12px;
  color: #202124;
}

.serp-card__title {
  margin: 4px 0;
  font-size: 18px;
  font-weight: 500;
  color: #1a0dab;
}

.serp-card__desc {
  margin: 0;
  font-size: 13px;
  color: #4d5156;
  line-height: 1.55;
}

.qr-config-row {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.qr-config-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border: 1px solid var(--a-border);
  border-radius: 8px;
  background: #fafbfe;
}

.qr-config-box img {
  width: 48px;
  height: 48px;
  object-fit: contain;
  border-radius: 4px;
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
  flex-wrap: wrap;
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
