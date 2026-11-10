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

const activeTab = ref('basic')

const { view, loading, saving, publishing, loadError, load, save, publish } =
  useConfig<Record<string, any>>('SITE')

const values = ref({
  siteName: '示例科技',
  slogan: '以物联网技术和软件服务改变公路货运',
  copyrightText: '© 2026 示例科技有限公司',
  icpText: '京ICP备08004136号',
  icpUrl: 'https://beian.miit.gov.cn/',
  policeText: '',
  policeUrl: '',
  logoMediaId: '',
  logoAlt: '',
  faviconMediaId: '',
  seoTitle: '示例科技 - 领先的物联网与数字化软件服务公司',
  seoDescription: '连接货运全流程并以数据和AI驱动运营，助力企业降本增效。',
  footerGroups: [] as FooterGroup[],
  qrCodes: [
    { label: '公众号', mediaId: '', imageUrl: '' },
    { label: '视频号', mediaId: '', imageUrl: '' },
  ] as QrCodeConfig[],
  contactEmails: [
    { email: 'connect@example.com', desc: '媒体联系&数据合作' },
    { email: 'jubao@example.com', desc: '集团投诉与举报' },
  ] as ContactEmailConfig[],
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
})

const pickerOpen = ref(false)
const pickerTarget = ref<'logo' | 'favicon' | 'qr0' | 'qr1'>('logo')

function emptyLink(): FooterLink {
  return { label: '', enabled: true, sortOrder: 100, link: { type: 'PAGE', pageCode: 'PRODUCTS' } }
}

function fill() {
  const draft = view.value?.draft
  if (!draft) return
  values.value.siteName = (draft.siteName && !draft.siteName.includes('G7') && !draft.siteName.includes('易流')) ? draft.siteName : '示例科技'
  values.value.slogan = draft.slogan ?? ''
  values.value.copyrightText = (draft.copyrightText && !draft.copyrightText.includes('G7') && !draft.copyrightText.includes('E6')) ? draft.copyrightText : '© 2026 示例科技有限公司'
  values.value.icpText = draft.icpText ?? ''
  values.value.icpUrl = draft.icpUrl ?? ''
  values.value.policeText = draft.policeText ?? ''
  values.value.policeUrl = draft.policeUrl ?? ''
  values.value.logoMediaId = draft.logoMediaId != null ? String(draft.logoMediaId) : ''
  values.value.logoAlt = draft.logoAlt ?? ''
  values.value.faviconMediaId = draft.faviconMediaId != null ? String(draft.faviconMediaId) : ''
  values.value.seoTitle = (draft.defaultSeo?.title && !draft.defaultSeo.title.includes('G7')) ? draft.defaultSeo.title : '示例科技 - 领先的物联网与数字化软件服务公司'
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

  if (Array.isArray(draft.contactEmails) && draft.contactEmails.length > 0) {
    values.value.contactEmails = draft.contactEmails.map((e: any) => ({
      email: (e.email ?? '').replace(/@g7\.com\.cn/g, '@example.com'),
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

function openPicker(target: 'logo' | 'favicon' | 'qr0' | 'qr1') {
  pickerTarget.value = target
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

    </el-tabs>

    <MediaPicker v-model="pickerOpen" kind="IMAGE" @select="onPicked" />
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
