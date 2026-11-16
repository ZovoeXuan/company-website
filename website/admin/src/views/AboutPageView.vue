<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ConfigPanel from '@/components/ConfigPanel.vue'
import MediaPicker from '@/components/MediaPicker.vue'
import { useConfig } from '@/composables/useConfig'
import { ApiError, upload } from '@/api/http'
import type { MediaItem } from '@/api/content'

interface Milestone {
  year: string
  title: string
  description: string
}

interface Qualification {
  name: string
  description: string
  mediaId?: string | number | null
  imageUrl?: string
}

interface ValueItemForm {
  title: string
  linesText: string
}

interface OfficeItemForm {
  city: string
  phone: string
  address: string
  image?: string
  mapX?: number
  mapY?: number
}

interface TeamMemberForm {
  name: string
  role: string
  avatar?: string
  bioText: string
}

interface ResourceItemForm {
  category: string
  title: string
  logoText: string
  logoColor: string
  url: string
  isVideo: boolean
}

interface StoryItemForm {
  quote: string
  author: string
  company: string
  logoText: string
  videoUrl: string
}

const route = useRoute()
const router = useRouter()
const activeTab = ref('intro')

watch(
  () => route.query.tab,
  (t) => {
    if (t && typeof t === 'string' && ['intro', 'team', 'resources', 'join', 'milestones'].includes(t)) {
      activeTab.value = t
    }
  },
  { immediate: true },
)

function onTabChange(tabPane: any) {
  const name = typeof tabPane === 'string' ? tabPane : tabPane?.props?.name || tabPane?.name
  if (name) {
    router.replace({ query: { ...route.query, tab: name } }).catch(() => {})
  }
}

const avatarFileInput = ref<HTMLInputElement>()
const uploadingMemberIdx = ref<number | null>(null)
const uploadingAvatar = ref(false)

function triggerUploadAvatar(index: number) {
  uploadingMemberIdx.value = index
  avatarFileInput.value?.click()
}

async function onAvatarFileSelected(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  uploadingAvatar.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res = await upload<{ id: number; url?: string }>('/api/admin/media', formData)
    const avatarUrl = res.url || `/api/public/media/${res.id}`
    if (uploadingMemberIdx.value !== null && form.teamMembers[uploadingMemberIdx.value]) {
      form.teamMembers[uploadingMemberIdx.value].avatar = avatarUrl
      ElMessage.success('高管头像上传成功')
    }
  } catch (err: any) {
    ElMessage.error(err instanceof ApiError ? err.message : '头像上传失败，请重试')
  } finally {
    uploadingAvatar.value = false
    uploadingMemberIdx.value = null
    input.value = ''
  }
}

function moveMember(index: number, step: number) {
  const target = index + step
  if (target < 0 || target >= form.teamMembers.length) return
  const item = form.teamMembers.splice(index, 1)[0]
  form.teamMembers.splice(target, 0, item)
}

function addTeamMember() {
  form.teamMembers.push({
    name: '',
    role: '',
    avatar: '',
    bioText: '',
  })
}

const previewMember = ref<TeamMemberForm | null>(null)
const previewModalOpen = computed({
  get: () => previewMember.value !== null,
  set: (v) => {
    if (!v) previewMember.value = null
  },
})

function resolveAvatar(url?: string): string {
  if (!url) return ''
  if (url.startsWith('/team/')) {
    const base = import.meta.env.BASE_URL || '/admin/'
    return `${base.replace(/\/$/, '')}${url}`
  }
  return url
}

const mediaPickerOpen = ref(false)
const mediaPickerKind = ref<'IMAGE' | 'VIDEO' | 'ALL'>('IMAGE')
const mediaPickerTarget = ref<string>('')

function openMediaPicker(target: string, kind: 'IMAGE' | 'VIDEO' | 'ALL' = 'IMAGE') {
  mediaPickerTarget.value = target
  mediaPickerKind.value = kind
  mediaPickerOpen.value = true
}

function onMediaPicked(item: MediaItem) {
  if (mediaPickerTarget.value === 'aboutHeroImg') {
    form.heroBgImage = item.url
  } else if (mediaPickerTarget.value === 'aboutHeroVid') {
    form.heroBgVideo = item.url
  } else if (mediaPickerTarget.value === 'aboutIntroImg') {
    form.introImage = item.url
  } else if (mediaPickerTarget.value.startsWith('qual-img-')) {
    const idx = Number(mediaPickerTarget.value.replace('qual-img-', ''))
    if (form.qualifications[idx]) {
      form.qualifications[idx].imageUrl = item.url
      form.qualifications[idx].mediaId = item.id
    }
  }
  mediaPickerOpen.value = false
}

const { view, loading, saving, publishing, loadError, load, save, publish } =
  useConfig<Record<string, any>>('ABOUT')

const form = reactive({
  heroTitle: '',
  heroSubtitle: '',
  heroEyebrow: '',
  heroBgType: 'default' as 'default' | 'image' | 'video',
  heroBgImage: '',
  heroBgVideo: '',
  showHeroLogoMark: false,
  showIntro: true,
  showIntroImage: false,
  introImageMode: 'none' as 'none' | 'image' | 'radar',
  showCeoLetter: false,
  showQualifications: true,
  showMilestones: true,
  showOffices: true,
  introTitle: '关于我们',
  intro: '',
  introImage: '',
  ceoQuote: '',
  ceoTitle: '',
  ceoSignature: '',
  ceoButtonText: '展开全部',
  ceoExcerptText: '',
  ceoFullText: '',
  teamTitle: '管理团队',
  teamMembers: [] as TeamMemberForm[],
  resourcesTitle: '资料库',
  resources: [] as ResourceItemForm[],
  customerStories: [] as StoryItemForm[],
  mission: '',
  vision: '',
  values: [] as ValueItemForm[],
  joinTitle: '期待您的加入',
  joinButtonText: '查看职位',
  joinUrl: '',
  milestones: [] as Milestone[],
  offices: [] as OfficeItemForm[],
  qualifications: [] as Qualification[],
})

function fill() {
  const draft = view.value?.draft
  if (!draft) return
  form.heroTitle = draft.heroTitle ?? ''
  form.heroSubtitle = draft.heroSubtitle ?? ''
  form.heroEyebrow = draft.heroEyebrow ?? ''
  form.heroBgType = draft.heroBgType ?? 'default'
  form.heroBgImage = draft.heroBgImage ?? ''
  form.heroBgVideo = draft.heroBgVideo ?? ''
  form.showHeroLogoMark = draft.showHeroLogoMark === true
  form.showIntro = draft.showIntro !== false
  const hasImg = Boolean(draft.introImage)
  form.showIntroImage = draft.showIntroImage === true || (draft.showIntroImage === undefined ? hasImg : false)
  form.introImageMode = (draft.introImageMode ?? (hasImg ? 'image' : 'none')) as any
  form.showCeoLetter = draft.showCeoLetter === true || Boolean(draft.ceoLetter?.title)
  form.showQualifications = draft.showQualifications !== false
  form.showMilestones = draft.showMilestones !== false
  form.showOffices = draft.showOffices !== false
  form.introTitle = draft.introTitle ?? '关于我们'
  form.intro = draft.intro ?? ''
  form.introImage = draft.introImage ?? ''

  if (draft.ceoLetter) {
    form.ceoQuote = draft.ceoLetter.leftQuote ?? ''
    form.ceoTitle = draft.ceoLetter.title ?? ''
    form.ceoSignature = draft.ceoLetter.signature ?? ''
    form.ceoButtonText = draft.ceoLetter.buttonText || '展开全部'
    form.ceoExcerptText = Array.isArray(draft.ceoLetter.excerpt)
      ? draft.ceoLetter.excerpt.join('\n')
      : ''
    form.ceoFullText = Array.isArray(draft.ceoLetter.fullParagraphs)
      ? draft.ceoLetter.fullParagraphs.join('\n')
      : ''
  } else {
    form.ceoQuote = ''
    form.ceoTitle = ''
    form.ceoSignature = ''
    form.ceoButtonText = '展开全部'
    form.ceoExcerptText = ''
    form.ceoFullText = ''
  }

  form.teamTitle = draft.teamTitle ?? '管理团队'
  form.teamMembers = Array.isArray(draft.teamMembers)
    ? draft.teamMembers.map((m: any) => ({
        name: m.name ?? '',
        role: m.role ?? '',
        avatar: m.avatar ?? '',
        bioText: Array.isArray(m.bio) ? m.bio.join('\n') : '',
      }))
    : []

  form.resourcesTitle = draft.resourcesTitle ?? '资料库'
  form.resources = Array.isArray(draft.resources)
    ? draft.resources.map((r: any) => ({
        category: r.category ?? '企业资料',
        title: r.title ?? '',
        logoText: r.logoText ?? '',
        logoColor: r.logoColor ?? '#2563eb',
        url: r.url ?? '',
        isVideo: Boolean(r.isVideo),
      }))
    : []

  form.customerStories = Array.isArray(draft.customerStories)
    ? draft.customerStories.map((s: any) => ({
        quote: s.quote ?? '',
        author: s.author ?? '',
        company: s.company ?? '',
        logoText: s.logoText ?? '',
        videoUrl: s.videoUrl ?? '',
      }))
    : []

  form.mission = draft.mission ?? ''
  form.vision = draft.vision ?? ''
  form.joinTitle = draft.joinTitle ?? '期待您的加入'
  form.joinButtonText = draft.joinButtonText ?? '查看职位'
  form.joinUrl = draft.joinUrl ?? ''

  form.values = Array.isArray(draft.values)
    ? draft.values.map((v: any) => ({
        title: v.title ?? '',
        linesText: Array.isArray(v.lines)
          ? v.lines.join('\n')
          : typeof v.lines === 'string'
            ? v.lines
            : '',
      }))
    : []

  form.milestones = Array.isArray(draft.milestones)
    ? draft.milestones.map((item: any) => ({
        year: item.year ?? '',
        title: item.title ?? '',
        description: item.description ?? '',
      }))
    : []

  form.offices = Array.isArray(draft.offices)
    ? draft.offices.map((o: any) => ({ ...o }))
    : []

  form.qualifications = Array.isArray(draft.qualifications)
    ? draft.qualifications.map((item: any) => ({ ...item }))
    : []
}

function addMilestone() {
  if (form.milestones.length >= 20) {
    ElMessage.warning('发展历程最多 20 条')
    return
  }
  form.milestones.push({ year: String(new Date().getFullYear()), title: '', description: '' })
}

function validateBeforeSave(): boolean {
  for (let i = 0; i < form.milestones.length; i++) {
    const m = form.milestones[i]
    if (!/^\d{4}$/.test((m.year ?? '').trim())) {
      ElMessage.warning(`发展历程第 ${i + 1} 条年份需为 4 位数字（例如 2026）`)
      return false
    }
  }
  return true
}

async function onSave(): Promise<boolean> {
  if (!validateBeforeSave()) return false
  const payload: Record<string, any> = {
    ...(view.value?.draft ?? {}),
    heroTitle: form.heroTitle,
    heroSubtitle: form.heroSubtitle,
    heroEyebrow: form.heroEyebrow,
    heroBgType: form.heroBgType,
    heroBgImage: form.heroBgImage,
    heroBgVideo: form.heroBgVideo,
    showHeroLogoMark: form.showHeroLogoMark,
    showIntro: form.showIntro,
    showIntroImage: form.showIntroImage,
    introImageMode: form.introImageMode,
    showCeoLetter: form.showCeoLetter,
    showQualifications: form.showQualifications,
    showMilestones: form.showMilestones,
    showOffices: form.showOffices,
    introTitle: form.introTitle,
    intro: form.intro,
    introImage: form.introImage,
    ceoLetter: {
      leftQuote: form.ceoQuote,
      title: form.ceoTitle,
      signature: form.ceoSignature,
      buttonText: form.ceoButtonText,
      excerpt: form.ceoExcerptText
        .split(/\n+/)
        .map((s) => s.trim())
        .filter(Boolean),
      fullParagraphs: form.ceoFullText
        .split(/\n+/)
        .map((s) => s.trim())
        .filter(Boolean),
    },
    teamTitle: form.teamTitle,
    teamMembers: form.teamMembers
      .filter((m) => m.name.trim())
      .map((m) => ({
        name: m.name.trim(),
        role: m.role.trim(),
        avatar: m.avatar?.trim() || undefined,
        bio: m.bioText
          .split(/\n+/)
          .map((s) => s.trim())
          .filter(Boolean),
      })),
    resourcesTitle: form.resourcesTitle,
    resources: form.resources.filter((r) => r.title.trim()),
    customerStories: form.customerStories.filter((s) => s.quote.trim()),
    mission: form.mission,
    vision: form.vision,
    values: form.values
      .filter((v) => v.title.trim())
      .map((v) => ({
        title: v.title.trim(),
        lines: v.linesText
          .split(/\n+/)
          .map((s) => s.trim())
          .filter(Boolean),
      })),
    joinTitle: form.joinTitle,
    joinButtonText: form.joinButtonText,
    joinUrl: form.joinUrl,
    milestones: form.milestones,
    offices: form.offices.filter((o) => o.city.trim()),
    qualifications: form.qualifications,
  }
  const ok = await save(payload)
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
    title="关于我们（5 大子页签全模块配置）"
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
    <el-alert v-if="loadError" type="error" :closable="false" :title="loadError" style="margin-bottom: 14px" />

    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <!-- Tab 1: 企业简介与 CEO 信件 -->
      <el-tab-pane label="1. 企业简介 & CEO的一封信" name="intro">
        <el-form label-width="140px" class="form">
          <el-divider content-position="left">顶部首屏横幅（支持图片/视频背景可换、自定义标题/副标题/标签）</el-divider>
          <el-form-item label="首屏大标题">
            <el-input v-model="form.heroTitle" maxlength="100" show-word-limit placeholder="例如：以物联网技术和软件服务改变公路货运" />
          </el-form-item>
          <el-form-item label="首屏副标题">
            <el-input v-model="form.heroSubtitle" maxlength="200" show-word-limit placeholder="选填，展示在首屏大标题下方" />
          </el-form-item>
          <el-form-item label="首屏眉标/标签">
            <el-input v-model="form.heroEyebrow" maxlength="50" show-word-limit placeholder="选填，例如：ABOUT US / 关于我们" style="width: 320px" />
          </el-form-item>
          <el-form-item label="首屏背景类型">
            <el-radio-group v-model="form.heroBgType">
              <el-radio-button value="default">默认深空流光</el-radio-button>
              <el-radio-button value="image">自定义背景图片</el-radio-button>
              <el-radio-button value="video">自定义背景视频</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="form.heroBgType === 'image'" label="自定义背景图片">
            <div style="display: flex; gap: 10px; width: 100%; align-items: center">
              <el-input v-model="form.heroBgImage" placeholder="输入图片 URL 或从素材库选择" />
              <el-button @click="openMediaPicker('aboutHeroImg', 'IMAGE')">选择素材</el-button>
              <el-button v-if="form.heroBgImage" link type="danger" @click="form.heroBgImage = ''">清除</el-button>
            </div>
            <div v-if="form.heroBgImage" style="margin-top: 8px">
              <img :src="form.heroBgImage" style="max-height: 90px; border-radius: 4px; border: 1px solid #ddd" />
            </div>
          </el-form-item>
          <el-form-item v-if="form.heroBgType === 'video'" label="自定义背景视频">
            <div style="display: flex; gap: 10px; width: 100%; align-items: center">
              <el-input v-model="form.heroBgVideo" placeholder="输入视频 URL (mp4/webm) 或从素材库选择" />
              <el-button @click="openMediaPicker('aboutHeroVid', 'VIDEO')">选择视频</el-button>
              <el-button v-if="form.heroBgVideo" link type="danger" @click="form.heroBgVideo = ''">清除</el-button>
            </div>
            <div v-if="form.heroBgVideo" style="margin-top: 8px">
              <video :src="form.heroBgVideo" style="max-height: 90px; border-radius: 4px" muted autoplay loop></video>
            </div>
          </el-form-item>
          <el-form-item label="字母Logo水印">
            <el-switch v-model="form.showHeroLogoMark" active-text="显示大写字母水印" inactive-text="关闭水印（纯净标题）" />
          </el-form-item>

          <el-divider content-position="left">企业简介区块配置</el-divider>
          <el-form-item label="启用企业简介模块">
            <el-switch v-model="form.showIntro" active-text="显示企业简介" inactive-text="隐藏此模块" />
          </el-form-item>
          <template v-if="form.showIntro">
            <el-form-item label="简介区块标题">
              <el-input v-model="form.introTitle" maxlength="60" show-word-limit />
            </el-form-item>
            <el-form-item label="企业简介正文">
              <el-input
                v-model="form.intro"
                type="textarea"
                :rows="5"
                maxlength="2000"
                show-word-limit
                placeholder="每段一行，留空时展示标准简介文案"
              />
            </el-form-item>
            <el-form-item label="右侧配图设置">
              <el-switch v-model="form.showIntroImage" active-text="显示配图/动效" inactive-text="不放图片（文字全宽排版）" />
            </el-form-item>
            <template v-if="form.showIntroImage">
              <el-form-item label="配图形式">
                <el-radio-group v-model="form.introImageMode">
                  <el-radio-button value="image">自定义图片</el-radio-button>
                  <el-radio-button value="radar">科技雷达动效</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item v-if="form.introImageMode === 'image'" label="企业简介图片">
                <div style="display: flex; gap: 10px; width: 100%; align-items: center">
                  <el-input v-model="form.introImage" placeholder="输入图片 URL 或从素材库选择" />
                  <el-button @click="openMediaPicker('aboutIntroImg', 'IMAGE')">选择素材</el-button>
                  <el-button v-if="form.introImage" link type="danger" @click="form.introImage = ''">清除</el-button>
                </div>
                <div v-if="form.introImage" style="margin-top: 8px">
                  <img :src="form.introImage" style="max-height: 120px; border-radius: 6px; border: 1px solid #ddd" />
                </div>
              </el-form-item>
            </template>
          </template>

          <el-divider content-position="left">CEO的一封信与全文弹窗配置</el-divider>
          <el-form-item label="启用CEO信件模块">
            <el-switch v-model="form.showCeoLetter" active-text="显示CEO的一封信" inactive-text="隐藏此模块" />
          </el-form-item>
          <template v-if="form.showCeoLetter">
            <el-form-item label="左侧背景卡金句">
              <el-input v-model="form.ceoQuote" type="textarea" :rows="2" maxlength="200" />
            </el-form-item>
            <el-form-item label="信件主标题">
              <el-input v-model="form.ceoTitle" maxlength="80" />
            </el-form-item>
            <el-form-item label="签名文字">
              <el-input v-model="form.ceoSignature" maxlength="30" style="width: 220px" />
            </el-form-item>
            <el-form-item label="卡片摘录(每段1行)">
              <el-input v-model="form.ceoExcerptText" type="textarea" :rows="4" placeholder="每行一段，展示在右侧卡片上" />
            </el-form-item>
            <el-form-item label="弹窗全文(每段1行)">
              <el-input v-model="form.ceoFullText" type="textarea" :rows="6" placeholder="点击「展开全部」弹窗中显示的完整信件段落" />
            </el-form-item>
          </template>
        </el-form>
      </el-tab-pane>

      <!-- Tab 2: 管理团队 -->
      <el-tab-pane label="2. 管理团队 & 详情弹窗" name="team">
        <input
          ref="avatarFileInput"
          type="file"
          accept="image/png,image/jpeg,image/webp,image/svg+xml"
          style="display: none"
          @change="onAvatarFileSelected"
        />

        <el-alert
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 16px"
          title="管理团队配置说明：支持为每位高管上传真实照片、自由调整前后排序、编辑职务与详细履历。修改后点击右上角「保存草稿」并「发布上线」，前台即可实时生效。"
        />

        <el-form label-width="140px" class="form" style="margin-bottom: 16px">
          <el-form-item label="团队页面标题">
            <el-input v-model="form.teamTitle" maxlength="60" style="width: 320px" placeholder="管理团队" />
          </el-form-item>
        </el-form>

        <div class="team-cards-list">
          <div v-for="(tm, tIdx) in form.teamMembers" :key="tIdx" class="team-member-edit-card">
            <div class="team-card-header">
              <div class="team-card-header__title">
                <span class="badge">#{{ tIdx + 1 }}</span>
                <strong>{{ tm.name || '未命名高管' }}</strong>
                <span v-if="tm.role" class="sub-role">{{ tm.role }}</span>
              </div>
              <div class="team-card-header__actions">
                <el-button
                  size="small"
                  :disabled="tIdx === 0"
                  @click="moveMember(tIdx, -1)"
                >
                  上移
                </el-button>
                <el-button
                  size="small"
                  :disabled="tIdx === form.teamMembers.length - 1"
                  @click="moveMember(tIdx, 1)"
                >
                  下移
                </el-button>
                <el-button
                  size="small"
                  type="primary"
                  plain
                  @click="previewMember = tm"
                >
                  预览弹窗效果
                </el-button>
                <el-button
                  size="small"
                  link
                  type="danger"
                  @click="form.teamMembers.splice(tIdx, 1)"
                >
                  删除
                </el-button>
              </div>
            </div>

            <div class="team-card-body">
              <div class="avatar-col">
                <div class="avatar-preview-box" @click="triggerUploadAvatar(tIdx)">
                  <img v-if="tm.avatar" :src="resolveAvatar(tm.avatar)" :alt="tm.name" class="avatar-img" />
                  <div v-else class="avatar-placeholder">
                    <span class="avatar-placeholder__icon">+</span>
                    <span>点击上传照片</span>
                  </div>
                  <div class="avatar-hover-mask">
                    <span>更换照片</span>
                  </div>
                </div>
                <div class="avatar-actions">
                  <el-button
                    size="small"
                    type="primary"
                    :loading="uploadingAvatar && uploadingMemberIdx === tIdx"
                    @click="triggerUploadAvatar(tIdx)"
                  >
                    上传照片
                  </el-button>
                  <el-button
                    v-if="tm.avatar"
                    size="small"
                    link
                    type="danger"
                    @click="tm.avatar = ''"
                  >
                    清除
                  </el-button>
                </div>
                <div class="avatar-hint">建议 4:5 比例竖版人像，JPG/PNG/SVG</div>
              </div>

              <div class="info-col">
                <div class="row-inline">
                  <el-input v-model="tm.name" placeholder="姓名" style="width: 180px">
                    <template #prepend>姓名</template>
                  </el-input>
                  <el-input v-model="tm.role" placeholder="职务（如：首席执行官）" style="flex: 1">
                    <template #prepend>职务</template>
                  </el-input>
                </div>

                <div class="row-inline">
                  <el-input v-model="tm.avatar" placeholder="头像图片 URL（如 /api/public/media/1）" style="width: 100%">
                    <template #prepend>头像URL</template>
                  </el-input>
                </div>

                <div class="bio-box">
                  <div class="bio-label">
                    <span>高管履历与个人介绍（前台点击卡片弹窗展示，每行一段）</span>
                    <el-button link type="primary" size="small" @click="previewMember = tm">查看弹窗效果</el-button>
                  </div>
                  <el-input
                    v-model="tm.bioText"
                    type="textarea"
                    :rows="4"
                    placeholder="请输入高管履历介绍，支持多段落，每行一段..."
                  />
                </div>
              </div>
            </div>
          </div>
        </div>

        <div style="margin: 16px 0 28px">
          <el-button type="primary" plain @click="addTeamMember">
            + 添加管理团队成员
          </el-button>
        </div>

        <el-divider content-position="left">团队专区前台 1:1 实景预演（点击任意卡片可测试履历详情弹窗）</el-divider>
        <div class="team-live-preview">
          <h2 class="preview-title">{{ form.teamTitle || '管理团队' }}</h2>
          <div class="preview-grid">
            <div
              v-for="(tm, idx) in form.teamMembers"
              :key="idx"
              class="preview-card"
              @click="previewMember = tm"
            >
              <div class="preview-card__photo">
                <img v-if="tm.avatar" :src="resolveAvatar(tm.avatar)" :alt="tm.name" />
                <div v-else class="preview-no-img">暂无照片</div>
              </div>
              <div class="preview-card__meta">
                <div class="preview-name">{{ tm.name || '姓名' }}</div>
                <div class="preview-role">{{ tm.role || '职务' }}</div>
              </div>
              <div class="preview-card__tag">点击查看履历</div>
            </div>
          </div>
        </div>

        <!-- 履历弹窗 1:1 预览 Dialog -->
        <el-dialog
          v-model="previewModalOpen"
          :title="`高管履历详情 - ${previewMember?.name || ''}`"
          width="680px"
          destroy-on-close
        >
          <div v-if="previewMember" class="bio-modal-preview">
            <div class="bio-modal-photo">
              <img v-if="previewMember.avatar" :src="resolveAvatar(previewMember.avatar)" :alt="previewMember.name" />
              <div v-else class="preview-no-img">暂无头像</div>
            </div>
            <div class="bio-modal-content">
              <h3 class="bio-modal-name">{{ previewMember.name }}</h3>
              <p class="bio-modal-role">{{ previewMember.role }}</p>
              <div class="bio-modal-body">
                <p
                  v-for="(p, pIdx) in (previewMember.bioText || '').split(/\n+/).filter(Boolean)"
                  :key="pIdx"
                >
                  {{ p }}
                </p>
                <p v-if="!(previewMember.bioText || '').trim()" style="color: #94a3b8; font-style: italic;">
                  暂无履历介绍文案
                </p>
              </div>
            </div>
          </div>
        </el-dialog>
      </el-tab-pane>

      <!-- Tab 3: 资料共享 & 客户成功故事 -->
      <el-tab-pane label="3. 资料库 & 客户成功故事" name="resources">
        <el-alert
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 16px"
          title="资料库与客户故事说明：资料卡片直接展示在官网「关于我们 - 资料共享」子页签；下方的 2×2 客户成功故事视频卡片展示在官网「客户案例」页底部。修改后直接点击右上角「保存草稿」或「发布上线」即可同步更新。"
        />

        <el-form label-width="140px" class="form" style="margin-bottom: 14px">
          <el-form-item label="资料库专区大标题">
            <el-input v-model="form.resourcesTitle" maxlength="60" style="width: 320px" placeholder="资料库" />
          </el-form-item>
        </el-form>

        <h3 class="sec-h">资料共享</h3>
        <div v-for="(res, rIdx) in form.resources" :key="rIdx" class="card-box">
          <div class="row-inline">
            <el-select v-model="res.category" style="width: 130px">
              <el-option label="企业资料" value="企业资料" />
              <el-option label="客户视频" value="客户视频" />
              <el-option label="行业报告" value="行业报告" />
              <el-option label="ESG" value="ESG" />
            </el-select>
            <el-input v-model="res.title" placeholder="资料卡片标题" style="width: 220px" />
            <el-input v-model="res.logoText" placeholder="封面大字或品牌名" style="width: 200px" />
            <el-input v-model="res.url" placeholder="下载链接或视频 URL" style="flex: 1" />
            <el-button link type="danger" @click="form.resources.splice(rIdx, 1)">删除</el-button>
          </div>
        </div>
        <el-button
          size="small"
          @click="
            form.resources.push({
              category: '企业资料',
              title: '',
              logoText: '',
              logoColor: '#4c2ce6',
              url: '',
              isVideo: false,
            })
          "
        >
          + 添加资料卡片
        </el-button>

        <el-divider />
        <h3 class="sec-h">客户案例页底部「客户成功故事」2×2 视频证言卡片</h3>
        <div v-for="(st, sIdx) in form.customerStories" :key="sIdx" class="card-box">
          <div class="row-inline">
            <el-input v-model="st.company" placeholder="公司简称" style="width: 160px" />
            <el-input v-model="st.author" placeholder="受访人姓名与职务" style="width: 280px" />
            <el-input v-model="st.logoText" placeholder="右下角品牌 Logo 文字" style="width: 180px" />
            <el-button link type="danger" @click="form.customerStories.splice(sIdx, 1)">删除</el-button>
          </div>
          <el-input v-model="st.quote" placeholder="客户证言引言文案" style="margin-bottom: 8px" />
          <el-input v-model="st.videoUrl" placeholder="点击播放按钮弹出的视频 URL（MP4）" />
        </div>
        <el-button
          size="small"
          @click="form.customerStories.push({ quote: '', author: '', company: '', logoText: '', videoUrl: '' })"
        >
          + 添加客户成功故事
        </el-button>
      </el-tab-pane>

      <!-- Tab 4: 加入我们 & 使命愿景价值观 -->
      <el-tab-pane label="4. 加入我们 & 使命/愿景/5大价值观" name="join">
        <el-alert
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 16px"
          title="加入我们与企业文化说明：本页配置的招聘海报、企业使命、愿景以及 5 大核心价值观，展示在官网「关于我们 - 加入我们」页面。修改后直接点击右上角「保存草稿」或「发布上线」即刻生效。"
        />
        <el-form label-width="140px" class="form">
          <el-form-item label="招聘横幅标题">
            <el-input v-model="form.joinTitle" maxlength="60" placeholder="期待您的加入" />
          </el-form-item>
          <el-form-item label="招聘按钮文字">
            <el-input v-model="form.joinButtonText" maxlength="20" style="width: 220px" placeholder="查看职位" />
          </el-form-item>
          <el-form-item label="招聘跳转链接">
            <el-input v-model="form.joinUrl" placeholder="选填外部招聘门户 URL，留空则唤起职位投递弹窗" />
          </el-form-item>
          <el-form-item label="企业使命">
            <el-input v-model="form.mission" maxlength="200" />
          </el-form-item>
          <el-form-item label="企业愿景">
            <el-input v-model="form.vision" maxlength="200" />
          </el-form-item>
        </el-form>

        <h3 class="sec-h">核心价值观</h3>
        <div v-for="(val, vIdx) in form.values" :key="vIdx" class="card-box">
          <div class="card-box__head">
            <el-input v-model="val.title" placeholder="价值观名称（如：主人翁精神）" style="width: 240px" />
            <el-button link type="danger" @click="form.values.splice(vIdx, 1)">删除</el-button>
          </div>
          <el-input v-model="val.linesText" type="textarea" :rows="3" placeholder="每行一条诠释说明" />
        </div>
        <el-button size="small" @click="form.values.push({ title: '', linesText: '' })">
          + 添加核心价值观
        </el-button>
      </el-tab-pane>

      <!-- Tab 5: 发展历程 & 荣誉资质 & 全国网点 -->
      <el-tab-pane label="5. 发展历程 & 荣誉资质 & 全国办公网点" name="milestones">
        <el-alert
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 16px"
          title="发展历程、荣誉资质与全国网点说明：可分别开关各模块显隐，修改后点击右上角「保存草稿」或「发布上线」即刻生效。"
        />

        <el-form label-width="150px" class="form" style="margin-bottom: 16px">
          <el-form-item label="启用发展历程模块">
            <el-switch v-model="form.showMilestones" active-text="显示发展历程" inactive-text="隐藏此模块" />
          </el-form-item>
        </el-form>

        <template v-if="form.showMilestones">
          <h3 class="sec-h">发展历程时间轴节点</h3>
          <div v-for="(item, index) in form.milestones" :key="index" class="row-inline">
            <el-input v-model="item.year" maxlength="4" placeholder="年份(2022)" style="width: 110px" />
            <el-input v-model="item.title" maxlength="50" placeholder="节点主事件" style="width: 240px" />
            <el-input v-model="item.description" maxlength="300" placeholder="多条事件可用分号 ； 分隔" style="flex: 1" />
            <el-button link type="danger" @click="form.milestones.splice(index, 1)">删除</el-button>
          </div>
          <el-button size="small" @click="addMilestone">+ 添加发展历程</el-button>
        </template>

        <el-divider />

        <el-form label-width="150px" class="form" style="margin-bottom: 16px">
          <el-form-item label="启用荣誉资质模块">
            <el-switch v-model="form.showQualifications" active-text="显示荣誉与资质" inactive-text="隐藏此模块" />
          </el-form-item>
        </el-form>

        <template v-if="form.showQualifications">
          <h3 class="sec-h">荣誉与资质认证列表</h3>
          <div v-for="(q, qIdx) in form.qualifications" :key="qIdx" class="card-box">
            <div class="row-inline">
              <el-input v-model="q.name" placeholder="荣誉/资质名称（如：高新技术企业）" style="width: 240px" />
              <el-input v-model="q.description" placeholder="描述（如：2021 年认定。）" style="flex: 1" />
              <el-button link type="danger" @click="form.qualifications.splice(qIdx, 1)">删除</el-button>
            </div>
            <div class="row-inline">
              <el-input v-model="q.imageUrl" placeholder="证书/徽章图片 URL（选填）" style="flex: 1" />
              <el-button size="small" @click="openMediaPicker(`qual-img-${qIdx}`, 'IMAGE')">选择素材</el-button>
              <el-button v-if="q.imageUrl" size="small" link type="danger" @click="q.imageUrl = ''">清除</el-button>
            </div>
            <div v-if="q.imageUrl" style="margin-top: 6px">
              <img :src="q.imageUrl" style="max-height: 80px; border-radius: 4px; border: 1px solid #ddd" />
            </div>
          </div>
          <el-button size="small" @click="form.qualifications.push({ name: '', description: '', imageUrl: '' })">
            + 添加荣誉与资质
          </el-button>
        </template>

        <el-divider />

        <el-form label-width="150px" class="form" style="margin-bottom: 16px">
          <el-form-item label="启用全国网点模块">
            <el-switch v-model="form.showOffices" active-text="显示全国网点" inactive-text="隐藏此模块" />
          </el-form-item>
        </el-form>

        <template v-if="form.showOffices">
          <h3 class="sec-h">全国办公网点列表</h3>
          <div v-for="(off, oIdx) in form.offices" :key="oIdx" class="card-box">
            <div class="row-inline">
              <el-input v-model="off.city" placeholder="城市名称（如：北京（总部））" style="width: 170px" />
              <el-input v-model="off.phone" placeholder="联系电话（选填）" style="width: 160px" />
              <el-input v-model="off.address" placeholder="详细办公地址" style="flex: 1" />
              <el-button link type="danger" @click="form.offices.splice(oIdx, 1)">删除</el-button>
            </div>
            <div class="row-inline">
              <el-input v-model="off.image" placeholder="办公大楼实景照片 URL（选填）" style="flex: 1" />
              <span>地图横坐标 X(%)：</span>
              <el-input-number v-model="off.mapX" :min="10" :max="90" size="small" />
              <span>纵坐标 Y(%)：</span>
              <el-input-number v-model="off.mapY" :min="10" :max="90" size="small" />
            </div>
          </div>
          <el-button
            size="small"
            @click="form.offices.push({ city: '', phone: '', address: '', image: '', mapX: 65, mapY: 50 })"
          >
            + 添加城市办公网点
          </el-button>
        </template>
      </el-tab-pane>
    </el-tabs>

    <MediaPicker v-model="mediaPickerOpen" :kind="mediaPickerKind" @select="onMediaPicked" />
  </ConfigPanel>
</template>

<style scoped>
.form {
  max-width: 780px;
}
.form :deep(.el-form-item__label) {
  white-space: nowrap;
}
.sec-h {
  font-size: 14.5px;
  margin: 12px 0;
}
.card-box {
  border: 1px solid var(--a-border);
  border-radius: 10px;
  padding: 14px;
  margin-bottom: 12px;
  background: #fafbfe;
}
.card-box__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.row-inline {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

/* Team Member Edit Cards */
.team-cards-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.team-member-edit-card {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}
.team-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}
.team-card-header__title {
  display: flex;
  align-items: center;
  gap: 10px;
}
.team-card-header__title .badge {
  background: #4f46e5;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  padding: 2px 7px;
  border-radius: 10px;
}
.team-card-header__title .sub-role {
  color: #64748b;
  font-size: 13px;
}
.team-card-header__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.team-card-body {
  display: flex;
  gap: 20px;
  padding: 16px;
}
.avatar-col {
  width: 140px;
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}
.avatar-preview-box {
  width: 120px;
  height: 150px;
  border-radius: 8px;
  background: #f1f5f9;
  border: 1px dashed #cbd5e1;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.avatar-preview-box:hover .avatar-hover-mask {
  opacity: 1;
}
.avatar-hover-mask {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.6);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 500;
  opacity: 0;
  transition: opacity 0.2s;
}
.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.avatar-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #94a3b8;
  font-size: 12px;
  gap: 4px;
}
.avatar-placeholder__icon {
  font-size: 24px;
  line-height: 1;
}
.avatar-actions {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.avatar-hint {
  font-size: 11px;
  color: #94a3b8;
  text-align: center;
  margin-top: 6px;
  line-height: 1.3;
}
.info-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.bio-box {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.bio-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12.5px;
  color: #475569;
}

/* Team Live Preview */
.team-live-preview {
  margin-top: 16px;
  padding: 24px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}
.preview-title {
  text-align: center;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 24px 0;
}
.preview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}
.preview-card {
  background: #ffffff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative;
  border: 1px solid #e2e8f0;
}
.preview-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}
.preview-card__photo {
  width: 100%;
  aspect-ratio: 4 / 4.6;
  background: #1e293b;
  overflow: hidden;
}
.preview-card__photo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.preview-no-img {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 13px;
}
.preview-card__meta {
  padding: 14px;
  text-align: center;
}
.preview-name {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 4px;
}
.preview-role {
  font-size: 12.5px;
  color: #64748b;
}
.preview-card__tag {
  background: #eff6ff;
  color: #3b82f6;
  font-size: 11px;
  text-align: center;
  padding: 5px 0;
  border-top: 1px solid #e2e8f0;
  font-weight: 500;
}

/* Bio Modal Preview */
.bio-modal-preview {
  display: flex;
  gap: 24px;
}
.bio-modal-photo {
  width: 180px;
  height: 225px;
  border-radius: 8px;
  background: #1e293b;
  overflow: hidden;
  flex-shrink: 0;
}
.bio-modal-photo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.bio-modal-content {
  flex: 1;
}
.bio-modal-name {
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 6px 0;
}
.bio-modal-role {
  font-size: 14px;
  color: #4f46e5;
  font-weight: 500;
  margin: 0 0 16px 0;
}
.bio-modal-body p {
  font-size: 13.5px;
  line-height: 1.7;
  color: #334155;
  margin: 0 0 10px 0;
}
</style>
