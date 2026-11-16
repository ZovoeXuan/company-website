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

interface ContactEmailConfig {
  email: string
  desc: string
}

interface XiaoQiFaqPair {
  q: string
  thought?: string
  a: string
}

interface XiaoQiConfigForm {
  enabled: boolean
  name: string
  roleTitle: string
  headerLine1: string
  headerLine2: string
  welcomeSubtitle: string
  certBadge: string
  capabilitiesText: string
  questionsText: string
  faqPairs: XiaoQiFaqPair[]
  deepThinkEnabled: boolean
  deepThinkModel: string
  disclaimer: string
  apiUrl: string
  apiKey: string
  modelName: string
  systemPrompt: string
}

interface PageHeroItemForm {
  title: string
  subtitle: string
  eyebrow: string
  bgType: 'default' | 'image' | 'video'
  bgImage: string
  bgVideo: string
  overlayOpacity: number
}

const activeTab = ref('basic')
const activeHeroNames = ref(['news', 'cases', 'products', 'solutions'])

const { view, loading, saving, publishing, loadError, load, save, publish } =
  useConfig<Record<string, any>>('SITE')

const values = ref({
  siteName: '鑫链科技',
  slogan: '以物联网技术和软件服务赋能实体产业数字化转型',
  copyrightText: '© 2026 鑫链科技有限公司',
  icpText: '',
  icpUrl: 'https://beian.miit.gov.cn/',
  policeText: '',
  policeUrl: '',
  logoMediaId: '',
  logoAlt: '',
  faviconMediaId: '',
  seoTitle: '鑫链科技 - 领先的物联网与数字化软件服务公司',
  seoDescription: '连接产业全流程并以数据和AI驱动运营，助力企业降本增效。',
  footerGroups: [] as FooterGroup[],
  qrCodes: [
    { label: '公众号', mediaId: '', imageUrl: '' },
    { label: '视频号', mediaId: '', imageUrl: '' },
  ] as QrCodeConfig[],
  contactEmails: [] as ContactEmailConfig[],
  xiaoqi: {
    enabled: true,
    name: '小鑫',
    roleTitle: '数字物流专家',
    headerLine1: '您好，我是您的',
    headerLine2: '数字物流专家 小鑫',
    welcomeSubtitle: '企业数字化专家',
    certBadge: '专业认证',
    capabilitiesText: '数字化解决方案\n新能源场景分析\n运价查询\n线路智能推荐\n物流实时数据',
    questionsText:
      '新能源物流车运营痛点解决方案？\n如何构建智慧物流园区？\n“三晒两抓-城配版”系统中司机的安全分是如何分类的？\n物流企业如何控制货损率？\n中国“双碳”目标及全球ESG趋势对物流企业提出哪些新要求？\n什么是「紫宝盒」全场景AI工具？\n大宗煤炭与钢铁运输如何实现防偷盗AI治理？\n冷链运输如何做到全程不脱温预警？\n车队如何通过数字能源降低加油与充电成本？\n如何申请产品免费试用或预约专家演示？',
    faqPairs: [
      {
        q: '新能源物流车运营痛点解决方案？',
        thought: '正在检索新能源重卡与城配车队运营数据库，匹配数字能源与智能管车一体化方案...',
        a: '针对新能源物流车运营，我们提供「智能管车 + 数字能源」一体化解决方案：1. 智能补能调度；2. 能耗精细核算；3. 电池健康实时守护。',
      },
      {
        q: '如何构建智慧物流园区？',
        thought: '正在调取智慧园区与履约可视化架构模型...',
        a: '构建智慧物流园区核心在于「场内场外协同 + 全流程无人化感知」：支持预约准入联动、智能月台调度与装卸作业透明化监控。',
      },
    ],
    deepThinkEnabled: true,
    deepThinkModel: 'DeepSeek R1',
    disclaimer: '提醒：AI也可能会犯错误，请考虑核实重要信息',
    apiUrl: '',
    apiKey: '',
    modelName: 'deepseek-reasoner',
    systemPrompt: '你是公司官网的智能专家助手小鑫，请专业、清晰、热情地解答客户关于产品、解决方案与合作试用的问题。',
  } as XiaoQiConfigForm,
  pageHeroes: {
    news: {
      title: '企业新闻与行业洞察',
      subtitle: '关注物流与供应链前沿技术、业务动态与标杆企业数字化实践。',
      eyebrow: 'NEWSROOM',
      bgType: 'default',
      bgImage: '',
      bgVideo: '',
      overlayOpacity: 0.55,
    },
    cases: {
      title: '客户成功故事',
      subtitle: '每个案例都写清了三件事：原来什么问题、做了什么、结果怎么衡量。',
      eyebrow: 'CUSTOMER STORIES',
      bgType: 'default',
      bgImage: '',
      bgVideo: '',
      overlayOpacity: 0.55,
    },
    products: {
      title: '产品中心',
      subtitle: '从设备接入到运维决策，按产线实际情况选择模块，也可以整套落地。',
      eyebrow: 'PRODUCTS',
      bgType: 'default',
      bgImage: '',
      bgVideo: '',
      overlayOpacity: 0.55,
    },
    solutions: {
      title: '解决方案',
      subtitle: '按行业组织的落地方案。先看问题是否相似，再看做法能否复用。',
      eyebrow: 'SOLUTIONS',
      bgType: 'default',
      bgImage: '',
      bgVideo: '',
      overlayOpacity: 0.55,
    },
  } as Record<string, PageHeroItemForm>,
})

const pickerOpen = ref(false)
const pickerTarget = ref<string>('logo')
const pickerKind = ref<'IMAGE' | 'VIDEO' | 'ALL'>('IMAGE')

function emptyLink(): FooterLink {
  return { label: '', enabled: true, sortOrder: 100, link: { type: 'PAGE', pageCode: 'PRODUCTS' } }
}

function fill() {
  const draft = view.value?.draft
  if (!draft) return
  values.value.siteName = (draft.siteName && !draft.siteName.includes('G7') && !draft.siteName.includes('易流')) ? draft.siteName : '鑫链科技'
  values.value.slogan = draft.slogan ?? ''
  values.value.copyrightText = (draft.copyrightText && !draft.copyrightText.includes('G7') && !draft.copyrightText.includes('E6')) ? draft.copyrightText : '© 2026 鑫链科技有限公司'
  values.value.icpText = draft.icpText ?? ''
  values.value.icpUrl = draft.icpUrl ?? ''
  values.value.policeText = draft.policeText ?? ''
  values.value.policeUrl = draft.policeUrl ?? ''
  values.value.logoMediaId = draft.logoMediaId != null ? String(draft.logoMediaId) : ''
  values.value.logoAlt = draft.logoAlt ?? ''
  values.value.faviconMediaId = draft.faviconMediaId != null ? String(draft.faviconMediaId) : ''
  values.value.seoTitle = (draft.defaultSeo?.title && !draft.defaultSeo.title.includes('G7')) ? draft.defaultSeo.title : '鑫链科技 - 领先的物联网与数字化软件服务公司'
  values.value.seoDescription = draft.defaultSeo?.description ?? ''

  values.value.footerGroups = (draft.footerGroups ?? []).map((g: any) => ({
    title: g.title ?? '',
    links: (g.links ?? []).map((l: any) => ({
      key: l.key,
      label: l.label ?? '',
      enabled: l.enabled ?? true,
      sortOrder: l.sortOrder ?? 100,
      link: l.link ?? null,
    })),
  }))

  if (Array.isArray(draft.qrCodes) && draft.qrCodes.length > 0) {
    values.value.qrCodes = draft.qrCodes.map((q: any) => ({
      label: q.label ?? '公众号',
      mediaId: q.mediaId != null ? String(q.mediaId) : '',
      imageUrl: q.imageUrl ?? (q.mediaId ? `/api/public/media/${q.mediaId}` : ''),
    }))
  }

  if (Array.isArray(draft.contactEmails)) {
    values.value.contactEmails = draft.contactEmails
      .filter((e: any) => e?.email && !/@g7\.com\.cn$/i.test(e.email))
      .map((e: any) => ({
        email: e.email ?? '',
        desc: e.desc ?? '',
      }))
  }

  if (draft.xiaoqiConfig && typeof draft.xiaoqiConfig === 'object') {
    const xq = draft.xiaoqiConfig
    values.value.xiaoqi.enabled = xq.enabled ?? true
    values.value.xiaoqi.name = xq.name ?? '小鑫'
    values.value.xiaoqi.roleTitle = xq.roleTitle ?? '数字物流专家'
    values.value.xiaoqi.headerLine1 = xq.headerLine1 ?? '您好，我是您的'
    values.value.xiaoqi.headerLine2 = xq.headerLine2 ?? '数字物流专家 小鑫'
    values.value.xiaoqi.welcomeSubtitle = (xq.welcomeSubtitle && !xq.welcomeSubtitle.includes('G7') && !xq.welcomeSubtitle.includes('易流')) ? xq.welcomeSubtitle : '企业数字化专家'
    values.value.xiaoqi.certBadge = xq.certBadge ?? '专业认证'
    if (Array.isArray(xq.capabilities) && xq.capabilities.length > 0) {
      values.value.xiaoqi.capabilitiesText = xq.capabilities.join('\n')
    }
    if (Array.isArray(xq.questions) && xq.questions.length > 0) {
      values.value.xiaoqi.questionsText = xq.questions.join('\n')
    }
    if (Array.isArray(xq.faqPairs) && xq.faqPairs.length > 0) {
      values.value.xiaoqi.faqPairs = xq.faqPairs
    }
    values.value.xiaoqi.deepThinkEnabled = xq.deepThinkEnabled ?? true
    values.value.xiaoqi.deepThinkModel = xq.deepThinkModel ?? 'DeepSeek R1'
    values.value.xiaoqi.disclaimer = xq.disclaimer ?? '提醒：AI也可能会犯错误，请考虑核实重要信息'
    values.value.xiaoqi.apiUrl = xq.apiUrl ?? ''
    values.value.xiaoqi.apiKey = xq.apiKey ?? ''
    values.value.xiaoqi.modelName = xq.modelName ?? 'deepseek-reasoner'
    values.value.xiaoqi.systemPrompt = xq.systemPrompt ?? ''
  }

  if (draft.pageHeroes && typeof draft.pageHeroes === 'object') {
    for (const key of ['news', 'cases', 'products', 'solutions']) {
      if (draft.pageHeroes[key]) {
        values.value.pageHeroes[key] = {
          title: draft.pageHeroes[key].title ?? values.value.pageHeroes[key].title,
          subtitle: draft.pageHeroes[key].subtitle ?? values.value.pageHeroes[key].subtitle,
          eyebrow: draft.pageHeroes[key].eyebrow ?? values.value.pageHeroes[key].eyebrow,
          bgType: draft.pageHeroes[key].bgType ?? 'default',
          bgImage: draft.pageHeroes[key].bgImage ?? '',
          bgVideo: draft.pageHeroes[key].bgVideo ?? '',
          overlayOpacity: draft.pageHeroes[key].overlayOpacity ?? 0.55,
        }
      }
    }
  }
}

function buildPayload() {
  return {
    siteName: values.value.siteName,
    slogan: values.value.slogan,
    copyrightText: values.value.copyrightText,
    icpText: values.value.icpText,
    icpUrl: values.value.icpUrl,
    policeText: values.value.policeText,
    policeUrl: values.value.policeUrl,
    logoMediaId: values.value.logoMediaId ? Number(values.value.logoMediaId) : null,
    logoAlt: values.value.logoAlt,
    faviconMediaId: values.value.faviconMediaId ? Number(values.value.faviconMediaId) : null,
    defaultSeo: {
      title: values.value.seoTitle,
      description: values.value.seoDescription,
    },
    footerGroups: values.value.footerGroups,
    qrCodes: values.value.qrCodes.map((q) => ({
      label: q.label,
      mediaId: q.mediaId ? Number(q.mediaId) : undefined,
      imageUrl: q.mediaId ? `/api/public/media/${q.mediaId}` : q.imageUrl || undefined,
    })),
    contactEmails: values.value.contactEmails.filter((e) => e.email.trim()),
    pageHeroes: values.value.pageHeroes,
    xiaoqiConfig: {
      enabled: values.value.xiaoqi.enabled,
      name: values.value.xiaoqi.name,
      roleTitle: values.value.xiaoqi.roleTitle,
      headerLine1: values.value.xiaoqi.headerLine1,
      headerLine2: values.value.xiaoqi.headerLine2,
      welcomeSubtitle: values.value.xiaoqi.welcomeSubtitle,
      certBadge: values.value.xiaoqi.certBadge,
      capabilities: values.value.xiaoqi.capabilitiesText
        .split(/\n+/)
        .map((s) => s.trim())
        .filter(Boolean),
      questions: values.value.xiaoqi.questionsText
        .split(/\n+/)
        .map((s) => s.trim())
        .filter(Boolean),
      faqPairs: values.value.xiaoqi.faqPairs.filter((f) => f.q.trim() && f.a.trim()),
      deepThinkEnabled: values.value.xiaoqi.deepThinkEnabled,
      deepThinkModel: values.value.xiaoqi.deepThinkModel,
      disclaimer: values.value.xiaoqi.disclaimer,
      apiUrl: values.value.xiaoqi.apiUrl.trim(),
      apiKey: values.value.xiaoqi.apiKey.trim(),
      modelName: values.value.xiaoqi.modelName.trim(),
      systemPrompt: values.value.xiaoqi.systemPrompt.trim(),
    },
  }
}

function addGroup() {
  if (values.value.footerGroups.length >= 4) {
    ElMessage.warning('页脚分组最多 4 组')
    return
  }
  values.value.footerGroups.push({ title: '新分组', links: [emptyLink()] })
}

function openPicker(target: string, kind: 'IMAGE' | 'VIDEO' | 'ALL' = 'IMAGE') {
  pickerTarget.value = target
  pickerKind.value = kind
  pickerOpen.value = true
}

function onPicked(media: MediaItem) {
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
  } else if (pickerTarget.value.startsWith('hero-img-')) {
    const col = pickerTarget.value.replace('hero-img-', '')
    if (values.value.pageHeroes[col]) {
      values.value.pageHeroes[col].bgImage = media.url || `/api/public/media/${media.id}`
    }
  } else if (pickerTarget.value.startsWith('hero-vid-')) {
    const col = pickerTarget.value.replace('hero-vid-', '')
    if (values.value.pageHeroes[col]) {
      values.value.pageHeroes[col].bgVideo = media.url || `/api/public/media/${media.id}`
    }
  }
  pickerOpen.value = false
}

async function onSave(): Promise<boolean> {
  const ok = await save(buildPayload())
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
    title="全站设置（基础信息、Logo 与页脚配置）"
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

    <el-tabs v-model="activeTab">
      <!-- Tab 1: 基础品牌与备案 -->
      <el-tab-pane label="1. 品牌标识与备案 SEO" name="basic">
        <el-form label-width="140px" class="form">
          <el-form-item label="站点名称" required>
            <el-input v-model="values.siteName" maxlength="60" show-word-limit />
          </el-form-item>
          <el-form-item label="品牌标语">
            <el-input v-model="values.slogan" maxlength="100" show-word-limit />
          </el-form-item>
          <el-form-item label="站点 Logo">
            <div class="pick-row">
              <span class="muted">{{ values.logoMediaId ? `素材 #${values.logoMediaId}` : '默认显示站点名称' }}</span>
              <el-button size="small" @click="openPicker('logo')">选择图片</el-button>
              <el-button v-if="values.logoMediaId" size="small" link type="danger" @click="values.logoMediaId = ''">
                清除
              </el-button>
            </div>
          </el-form-item>
          <el-form-item label="默认 SEO 标题" required>
            <el-input v-model="values.seoTitle" maxlength="80" show-word-limit />
          </el-form-item>
          <el-form-item label="默认 SEO 描述" required>
            <el-input v-model="values.seoDescription" type="textarea" :rows="2" maxlength="200" show-word-limit />
          </el-form-item>
          <el-form-item label="页脚版权文案" required>
            <el-input v-model="values.copyrightText" maxlength="120" show-word-limit placeholder="© 2026 示例科技有限公司" />
          </el-form-item>
          <el-form-item label="ICP 备案号">
            <el-input v-model="values.icpText" maxlength="80" placeholder="京ICP备08004136号" />
          </el-form-item>
          <el-form-item label="ICP 备案链接">
            <el-input v-model="values.icpUrl" maxlength="500" placeholder="https://beian.miit.gov.cn/" />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- Tab 2: 底部 6 栏导航、双二维码与多邮箱说明 -->
      <el-tab-pane label="2. 页脚 6 栏导航、双二维码与多邮箱" name="footer">
        <h3 class="sec-h">页脚「联系我们」多邮箱与职能说明</h3>
        <div v-for="(em, eIdx) in values.contactEmails" :key="eIdx" class="inline-row">
          <el-input v-model="em.email" placeholder="例如：connect@company.com" style="width: 240px" />
          <el-input v-model="em.desc" placeholder="下方灰色小字说明（如：媒体联系&数据合作）" style="width: 260px" />
          <el-button link type="danger" @click="values.contactEmails.splice(eIdx, 1)">删除</el-button>
        </div>
        <el-button size="small" @click="values.contactEmails.push({ email: '', desc: '' })">
          + 添加联系邮箱
        </el-button>

        <el-divider />
        <h3 class="sec-h">页脚「关注我们」上下双二维码配置</h3>
        <div class="qr-grid">
          <div v-for="(qr, idx) in values.qrCodes" :key="idx" class="qr-card">
            <el-form label-width="100px">
              <el-form-item :label="`二维码 #${idx + 1}`">
                <el-input v-model="qr.label" maxlength="16" placeholder="如：公众号 / 视频号" />
              </el-form-item>
              <el-form-item label="二维码图片">
                <div class="pick-row">
                  <img v-if="qr.imageUrl" :src="qr.imageUrl" alt="二维码预览" class="qr-thumb" />
                  <span v-else class="muted">默认显示矢量二维码</span>
                  <el-button size="small" @click="openPicker(idx === 0 ? 'qr0' : 'qr1')">选择图片</el-button>
                  <el-button
                    v-if="qr.imageUrl || qr.mediaId"
                    size="small"
                    link
                    type="danger"
                    @click="qr.mediaId = ''; qr.imageUrl = ''"
                  >
                    清除
                  </el-button>
                </div>
              </el-form-item>
            </el-form>
          </div>
        </div>

        <el-divider />
        <h3 class="sec-h">页脚 4 列链接分组（留空时自动使用标准 IoT服务/交易服务/解决方案/关于我们）</h3>
        <div v-for="(group, gIndex) in values.footerGroups" :key="gIndex" class="group-card">
          <div class="group-head">
            <el-input v-model="group.title" maxlength="20" placeholder="分组标题" style="width: 220px" />
            <el-button link type="danger" @click="values.footerGroups.splice(gIndex, 1)">删除分组</el-button>
          </div>
          <div v-for="(item, lIndex) in group.links" :key="item.key ?? lIndex" class="link-row">
            <el-input v-model="item.label" maxlength="20" placeholder="链接名称" style="width: 160px" />
            <LinkEditor v-model="item.link" />
            <el-button link type="danger" @click="group.links.splice(lIndex, 1)">删除</el-button>
          </div>
          <el-button size="small" @click="group.links.push(emptyLink())">+ 添加链接</el-button>
        </div>
        <el-button @click="addGroup">+ 添加页脚分组（最多 4 组）</el-button>
      </el-tab-pane>

      <!-- Tab 3: 栏目首屏与横幅配置 -->
      <el-tab-pane label="3. 栏目首屏与横幅 (新闻 / 案例 / 产品 / 方案)" name="pageHeroes">
        <el-alert
          type="info"
          :closable="false"
          title="在此统一配置新闻中心、客户案例、产品中心、解决方案 4 大核心栏目的首屏大标题、副标题、标签及背景媒体（支持默认渐变流光、自定义图片、自定义视频循环播放），实时发布官网同步生效。"
          style="margin-bottom: 20px"
        />

        <el-collapse v-model="activeHeroNames">
          <!-- 1. 新闻中心首屏 -->
          <el-collapse-item title="📰 新闻中心首屏横幅 (/news)" name="news">
            <el-form label-width="140px" class="form">
              <el-form-item label="首屏大标题">
                <el-input v-model="values.pageHeroes.news.title" maxlength="60" show-word-limit placeholder="例如：企业新闻与行业洞察" />
              </el-form-item>
              <el-form-item label="首屏描述/副标题">
                <el-input v-model="values.pageHeroes.news.subtitle" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="例如：关注物流与供应链前沿技术、业务动态与标杆企业数字化实践。" />
              </el-form-item>
              <el-form-item label="首屏眉题/英文标签">
                <el-input v-model="values.pageHeroes.news.eyebrow" maxlength="40" placeholder="例如：NEWSROOM" style="width: 280px" />
              </el-form-item>
              <el-form-item label="首屏背景类型">
                <el-radio-group v-model="values.pageHeroes.news.bgType">
                  <el-radio-button value="default">默认光效背景</el-radio-button>
                  <el-radio-button value="image">自定义背景图片</el-radio-button>
                  <el-radio-button value="video">自定义背景视频</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item v-if="values.pageHeroes.news.bgType === 'image'" label="背景图片">
                <div class="pick-row">
                  <el-input v-model="values.pageHeroes.news.bgImage" placeholder="输入图片 URL 或从素材库选择" />
                  <el-button @click="openPicker('hero-img-news', 'IMAGE')">选择素材</el-button>
                  <el-button v-if="values.pageHeroes.news.bgImage" link type="danger" @click="values.pageHeroes.news.bgImage = ''">清除</el-button>
                </div>
                <div v-if="values.pageHeroes.news.bgImage" style="margin-top: 8px">
                  <img :src="values.pageHeroes.news.bgImage" style="max-height: 80px; border-radius: 4px; border: 1px solid #ddd" />
                </div>
              </el-form-item>
              <el-form-item v-if="values.pageHeroes.news.bgType === 'video'" label="背景视频">
                <div class="pick-row">
                  <el-input v-model="values.pageHeroes.news.bgVideo" placeholder="输入视频 URL (mp4/webm) 或从素材库选择" />
                  <el-button @click="openPicker('hero-vid-news', 'VIDEO')">选择视频</el-button>
                  <el-button v-if="values.pageHeroes.news.bgVideo" link type="danger" @click="values.pageHeroes.news.bgVideo = ''">清除</el-button>
                </div>
                <div v-if="values.pageHeroes.news.bgVideo" style="margin-top: 8px">
                  <video :src="values.pageHeroes.news.bgVideo" style="max-height: 90px; border-radius: 4px" muted autoplay loop></video>
                </div>
              </el-form-item>
            </el-form>
          </el-collapse-item>

          <!-- 2. 客户案例首屏 -->
          <el-collapse-item title="💼 客户案例首屏横幅 (/cases)" name="cases">
            <el-form label-width="140px" class="form">
              <el-form-item label="首屏大标题">
                <el-input v-model="values.pageHeroes.cases.title" maxlength="60" show-word-limit placeholder="例如：客户成功故事" />
              </el-form-item>
              <el-form-item label="首屏描述/副标题">
                <el-input v-model="values.pageHeroes.cases.subtitle" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="例如：每个案例都写清了三件事：原来什么问题、做了什么、结果怎么衡量。" />
              </el-form-item>
              <el-form-item label="首屏眉题/英文标签">
                <el-input v-model="values.pageHeroes.cases.eyebrow" maxlength="40" placeholder="例如：CUSTOMER STORIES" style="width: 280px" />
              </el-form-item>
              <el-form-item label="首屏背景类型">
                <el-radio-group v-model="values.pageHeroes.cases.bgType">
                  <el-radio-button value="default">默认光效背景</el-radio-button>
                  <el-radio-button value="image">自定义背景图片</el-radio-button>
                  <el-radio-button value="video">自定义背景视频</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item v-if="values.pageHeroes.cases.bgType === 'image'" label="背景图片">
                <div class="pick-row">
                  <el-input v-model="values.pageHeroes.cases.bgImage" placeholder="输入图片 URL 或从素材库选择" />
                  <el-button @click="openPicker('hero-img-cases', 'IMAGE')">选择素材</el-button>
                  <el-button v-if="values.pageHeroes.cases.bgImage" link type="danger" @click="values.pageHeroes.cases.bgImage = ''">清除</el-button>
                </div>
                <div v-if="values.pageHeroes.cases.bgImage" style="margin-top: 8px">
                  <img :src="values.pageHeroes.cases.bgImage" style="max-height: 80px; border-radius: 4px; border: 1px solid #ddd" />
                </div>
              </el-form-item>
              <el-form-item v-if="values.pageHeroes.cases.bgType === 'video'" label="背景视频">
                <div class="pick-row">
                  <el-input v-model="values.pageHeroes.cases.bgVideo" placeholder="输入视频 URL (mp4/webm) 或从素材库选择" />
                  <el-button @click="openPicker('hero-vid-cases', 'VIDEO')">选择视频</el-button>
                  <el-button v-if="values.pageHeroes.cases.bgVideo" link type="danger" @click="values.pageHeroes.cases.bgVideo = ''">清除</el-button>
                </div>
                <div v-if="values.pageHeroes.cases.bgVideo" style="margin-top: 8px">
                  <video :src="values.pageHeroes.cases.bgVideo" style="max-height: 90px; border-radius: 4px" muted autoplay loop></video>
                </div>
              </el-form-item>
            </el-form>
          </el-collapse-item>

          <!-- 3. 产品中心首屏 -->
          <el-collapse-item title="📦 产品中心首屏横幅 (/products)" name="products">
            <el-form label-width="140px" class="form">
              <el-form-item label="首屏大标题">
                <el-input v-model="values.pageHeroes.products.title" maxlength="60" show-word-limit placeholder="例如：产品中心" />
              </el-form-item>
              <el-form-item label="首屏描述/副标题">
                <el-input v-model="values.pageHeroes.products.subtitle" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="例如：从设备接入到运维决策，按产线实际情况选择模块，也可以整套落地。" />
              </el-form-item>
              <el-form-item label="首屏眉题/英文标签">
                <el-input v-model="values.pageHeroes.products.eyebrow" maxlength="40" placeholder="例如：PRODUCTS" style="width: 280px" />
              </el-form-item>
              <el-form-item label="首屏背景类型">
                <el-radio-group v-model="values.pageHeroes.products.bgType">
                  <el-radio-button value="default">默认光效背景</el-radio-button>
                  <el-radio-button value="image">自定义背景图片</el-radio-button>
                  <el-radio-button value="video">自定义背景视频</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item v-if="values.pageHeroes.products.bgType === 'image'" label="背景图片">
                <div class="pick-row">
                  <el-input v-model="values.pageHeroes.products.bgImage" placeholder="输入图片 URL 或从素材库选择" />
                  <el-button @click="openPicker('hero-img-products', 'IMAGE')">选择素材</el-button>
                  <el-button v-if="values.pageHeroes.products.bgImage" link type="danger" @click="values.pageHeroes.products.bgImage = ''">清除</el-button>
                </div>
                <div v-if="values.pageHeroes.products.bgImage" style="margin-top: 8px">
                  <img :src="values.pageHeroes.products.bgImage" style="max-height: 80px; border-radius: 4px; border: 1px solid #ddd" />
                </div>
              </el-form-item>
              <el-form-item v-if="values.pageHeroes.products.bgType === 'video'" label="背景视频">
                <div class="pick-row">
                  <el-input v-model="values.pageHeroes.products.bgVideo" placeholder="输入视频 URL (mp4/webm) 或从素材库选择" />
                  <el-button @click="openPicker('hero-vid-products', 'VIDEO')">选择视频</el-button>
                  <el-button v-if="values.pageHeroes.products.bgVideo" link type="danger" @click="values.pageHeroes.products.bgVideo = ''">清除</el-button>
                </div>
                <div v-if="values.pageHeroes.products.bgVideo" style="margin-top: 8px">
                  <video :src="values.pageHeroes.products.bgVideo" style="max-height: 90px; border-radius: 4px" muted autoplay loop></video>
                </div>
              </el-form-item>
            </el-form>
          </el-collapse-item>

          <!-- 4. 解决方案首屏 -->
          <el-collapse-item title="💡 解决方案首屏横幅 (/solutions)" name="solutions">
            <el-form label-width="140px" class="form">
              <el-form-item label="首屏大标题">
                <el-input v-model="values.pageHeroes.solutions.title" maxlength="60" show-word-limit placeholder="例如：解决方案" />
              </el-form-item>
              <el-form-item label="首屏描述/副标题">
                <el-input v-model="values.pageHeroes.solutions.subtitle" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="例如：按行业组织的落地方案。先看问题是否相似，再看做法能否复用。" />
              </el-form-item>
              <el-form-item label="首屏眉题/英文标签">
                <el-input v-model="values.pageHeroes.solutions.eyebrow" maxlength="40" placeholder="例如：SOLUTIONS" style="width: 280px" />
              </el-form-item>
              <el-form-item label="首屏背景类型">
                <el-radio-group v-model="values.pageHeroes.solutions.bgType">
                  <el-radio-button value="default">默认光效背景</el-radio-button>
                  <el-radio-button value="image">自定义背景图片</el-radio-button>
                  <el-radio-button value="video">自定义背景视频</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item v-if="values.pageHeroes.solutions.bgType === 'image'" label="背景图片">
                <div class="pick-row">
                  <el-input v-model="values.pageHeroes.solutions.bgImage" placeholder="输入图片 URL 或从素材库选择" />
                  <el-button @click="openPicker('hero-img-solutions', 'IMAGE')">选择素材</el-button>
                  <el-button v-if="values.pageHeroes.solutions.bgImage" link type="danger" @click="values.pageHeroes.solutions.bgImage = ''">清除</el-button>
                </div>
                <div v-if="values.pageHeroes.solutions.bgImage" style="margin-top: 8px">
                  <img :src="values.pageHeroes.solutions.bgImage" style="max-height: 80px; border-radius: 4px; border: 1px solid #ddd" />
                </div>
              </el-form-item>
              <el-form-item v-if="values.pageHeroes.solutions.bgType === 'video'" label="背景视频">
                <div class="pick-row">
                  <el-input v-model="values.pageHeroes.solutions.bgVideo" placeholder="输入视频 URL (mp4/webm) 或从素材库选择" />
                  <el-button @click="openPicker('hero-vid-solutions', 'VIDEO')">选择视频</el-button>
                  <el-button v-if="values.pageHeroes.solutions.bgVideo" link type="danger" @click="values.pageHeroes.solutions.bgVideo = ''">清除</el-button>
                </div>
                <div v-if="values.pageHeroes.solutions.bgVideo" style="margin-top: 8px">
                  <video :src="values.pageHeroes.solutions.bgVideo" style="max-height: 90px; border-radius: 4px" muted autoplay loop></video>
                </div>
              </el-form-item>
            </el-form>
          </el-collapse-item>
        </el-collapse>
      </el-tab-pane>

    </el-tabs>

    <MediaPicker v-model="pickerOpen" :kind="pickerKind" @select="onPicked" />
  </ConfigPanel>
</template>

<style scoped>
.form {
  max-width: 780px;
}
.sec-h {
  font-size: 14.5px;
  margin: 0 0 12px;
}
.inline-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}
.pick-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.muted {
  color: var(--a-text-muted);
  font-size: 12.5px;
}
.qr-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 14px;
  margin-bottom: 12px;
}
.qr-card {
  border: 1px solid var(--a-border);
  border-radius: 10px;
  padding: 14px;
  background: #fafbfe;
}
.qr-thumb {
  width: 48px;
  height: 48px;
  object-fit: contain;
  border: 1px solid var(--a-border);
  border-radius: 6px;
  background: #fff;
}
.group-card {
  border: 1px solid var(--a-border);
  border-radius: 10px;
  padding: 14px;
  margin-bottom: 12px;
  background: #fafbfe;
}
.group-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.link-row {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  margin-bottom: 10px;
  flex-wrap: wrap;
}
</style>
