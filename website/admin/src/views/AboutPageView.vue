<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ConfigPanel from '@/components/ConfigPanel.vue'
import { useConfig } from '@/composables/useConfig'
import { ApiError, upload } from '@/api/http'

interface Milestone {
  year: string
  title: string
  description: string
}

interface Qualification {
  name: string
  description: string
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

const DEFAULT_INTRO = `北京汇通天下物联科技有限公司（G7易流）是一家领先的公路货运行业物联网科技和软件服务公司。为货运经营者提供从车队管理、安全管理、货物管理到智能调度、自动结算的一体化操作平台，连接货运全流程并以数据和AI驱动运营，助力货运经营者降本增效。\n\nG7易流的客户覆盖超80%上游头部货主以及35000+下游中小物流企业，服务网络覆盖全国核心物流节点与产业园区。`

const DEFAULT_CEO_QUOTE = '“我谨代表G7易流全体员工，向在这个不凡时代中努力前行的货运经营者致以崇高的敬意！”'
const DEFAULT_CEO_TITLE = '从微不足道开始改变世界'
const DEFAULT_CEO_SIGNATURE = '翟学魂'
const DEFAULT_CEO_EXCERPT = [
  '十多年前，当我们刚开始带着第一代车载终端走进物流园、爬上驾驶室的时候，很少有人相信，一辆大货车上的小小黑盒子，有一天会彻底改变整个中国公路货运的运行方式。',
  '公路货运是国民经济的血脉，千万名卡车司机和数万家货运经营者日夜兼程。然而在过去很长一段时间里，安全风险高、油费过路费核算难、在途货物状态黑盒，一直是压在每一位车队经营者心头的三座大山。',
  '正是从每一次毫秒级的定位、每一次防碰撞预警、每一滴油的精准计量这些看似“微不足道”的小事开始，我们将物联网感知技术与AI算法融入货运现场，陪伴客户一步步走出粗放经验管理，迈入数字化精细运营的新时代。',
].join('\n')
const DEFAULT_CEO_FULL = [
  '我谨代表G7易流全体员工，向在这个不凡时代中努力前行的货运经营者致以崇高的敬意！',
  '十多年前，当我们刚开始带着第一代车载终端走进物流园、爬上驾驶室的时候，很少有人相信，一辆大货车上的小小黑盒子，有一天会彻底改变整个中国公路货运的运行方式。',
  '公路货运是国民经济的血脉，千万名卡车司机和数万家货运经营者日夜兼程。然而在过去很长一段时间里，安全风险高、油费过路费核算难、在途货物状态黑盒，一直是压在每一位车队经营者心头的三座大山。',
  '正是从每一次毫秒级的定位、每一次防碰撞预警、每一滴油的精准计量这些看似“微不足道”的小事开始，我们将物联网感知技术与AI算法融入货运现场，陪伴客户一步步走出粗放经验管理，迈入数字化精细运营的新时代。',
  '今天，超过800万台物联网设备在我们的平台上实时在线，每天产生超12TB的真实运营数据与1亿次车辆事件交互。当大模型与全场景AI工具「紫宝盒」走进车队，数据不再只是冰冷的报表，而是能够主动看见风险、听懂指令、帮车队省钱管事的“数字员工”。',
  '时代在变，技术在变，但我们存在的唯一理由始终未变——用科技创新和服务升级物流生产力，做货运经营者最值得信赖的长期伙伴。让我们继续从微不足道处着手，一起改变世界！',
].join('\n')

const DEFAULT_RESOURCES_LIST: ResourceItemForm[] = [
  { category: '企业资料', title: 'G7易流企业宣传册', logoText: 'G7易流 全栈产品手册', logoColor: '#4c2ce6', url: '', isVideo: false },
  { category: '企业资料', title: '安全管家', logoText: 'G 安全管家 · 主动安全托管', logoColor: '#2563eb', url: '', isVideo: false },
  { category: '企业资料', title: '财运通', logoText: 'G 财运通 · 数字化运力结算', logoColor: '#0284c7', url: '', isVideo: false },
  { category: '企业资料', title: '冷链管家', logoText: 'G 冷链管家 · 全程温控可视', logoColor: '#0d9488', url: '', isVideo: false },
  { category: '客户视频', title: '滨拓物流', logoText: '滨拓物流', logoColor: '#16a34a', url: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', isVideo: true },
  { category: '客户视频', title: '华电能源', logoText: '中国华电 CHD', logoColor: '#0284c7', url: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', isVideo: true },
  { category: '客户视频', title: '双汇物流', logoText: '双汇物流', logoColor: '#dc2626', url: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', isVideo: true },
  { category: '客户视频', title: '天佑物流', logoText: 'tianyou天佑', logoColor: '#d97706', url: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', isVideo: true },
  { category: '行业报告', title: '2026新能源车队管理白皮书', logoText: '新能源车队管理白皮书', logoColor: '#0284c7', url: '', isVideo: false },
  { category: '行业报告', title: '2026上半年冷链物流行业报告', logoText: '中国冷链物流大数据报告', logoColor: '#2563eb', url: '', isVideo: false },
  { category: '行业报告', title: '中国公路货运数字安全蓝皮书', logoText: '公路货运安全AI治理报告', logoColor: '#4f46e5', url: '', isVideo: false },
  { category: '行业报告', title: '大宗物流数字化转型实践指南', logoText: '大宗物流无人化协同白皮书', logoColor: '#334155', url: '', isVideo: false },
  { category: 'ESG', title: 'G7易流环境、社会及管治（ESG）报告', logoText: 'ESG 可持续发展报告', logoColor: '#059669', url: '', isVideo: false },
  { category: 'ESG', title: '绿色物流与电动重卡减碳核算标准', logoText: '物流双碳与绿色运力实践', logoColor: '#10b981', url: '', isVideo: false },
  { category: 'ESG', title: '卡车司机安全守护与公益行动年报', logoText: '千万卡车司机安全守护计划', logoColor: '#0d9488', url: '', isVideo: false },
  { category: 'ESG', title: '数据安全与隐私合规治理白皮书', logoText: '信息安全与隐私保护合规', logoColor: '#0284c7', url: '', isVideo: false },
]

const DEFAULT_STORIES_LIST: StoryItemForm[] = [
  {
    company: '双汇物流',
    author: '张太喜 双汇物流董事长',
    quote: '通过G7易流冷链数字运营体系，冷藏车温控合规率提升至99.8%，全程实时可视。',
    logoText: '双汇物流',
    videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
  },
  {
    company: '滨拓物流',
    author: '王滨 滨拓物流总经理',
    quote: '全场景物联网终端与AI安全管家，让我们车队的百万公里事故率下降超过70%。',
    logoText: '滨拓物流',
    videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
  },
  {
    company: '天佑物流',
    author: '李天佑 董事长',
    quote: '财运通自动结算与智能调度彻底改变了我们的现金流与运力周转效率。',
    logoText: 'tianyou天佑',
    videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
  },
]

const DEFAULT_MILESTONES_LIST: Milestone[] = [
  {
    year: '2018',
    title: '发布智能挂车与安全管家托管服务',
    description: '连接平台货运车辆突破 80 万台；完成由多家国际顶级机构联合领投的战略融资',
  },
  {
    year: '2019',
    title: '平台卡车连接数突破140万',
    description: '面向煤炭、钢铁、水泥、汽车、快运提供综合性数字化方案；与中铝、普洛斯设立合资公司',
  },
  {
    year: '2022',
    title: '平台卡车连接数突破 200万',
    description: '宣布完成新一轮2亿美元融资；嬴彻科技自动驾驶卡车量产交付',
  },
  {
    year: '2010',
    title: '提供基于北斗和GPS车队管理 SaaS 服务',
    description: '平台连接车辆突破 2000 台',
  },
  {
    year: '2014',
    title: '推出智能管车与车队金融结算体系',
    description: '全国设立七大区域运营中心，服务超万家物流企业',
  },
]

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

const DEFAULT_TEAM_MEMBERS: TeamMemberForm[] = [
  {
    name: '翟学魂',
    role: 'G7易流首席执行官',
    avatar: '/team/zhai-xuehun.svg',
    bioText:
      '翟学魂先生，现任物联网软件服务公司G7易流首席执行官。\n作为中国产业物联网和物流数字化的开拓者，打造了商用车物联网平台。翟学魂先生是中国物流行业的著名思想领袖之一，推动了中国物流企业的信息化和数字化进程。\n曾获得“中国物流行业十大风云人物”、“中国物流行业年度人物”、“改革开放40年物流行业企业家代表性人物”等荣誉称号。\n翟学魂先生毕业于清华大学物理系，并同期完成清华大学经管学院双学士学位学业。',
  },
  {
    name: '张景涛',
    role: 'G7易流总裁',
    avatar: '/team/zhang-jingtao.svg',
    bioText:
      '张景涛先生，现任G7易流总裁，全面负责公司产品研发、冷链与大宗行业解决方案及客户成功体系建设。\n深耕供应链与物流科技领域二十余年，推动了软硬一体物联网终端与全场景SaaS平台的深度融合，助力数万家货运经营者实现安全与效率跃升。',
  },
  {
    name: '郝淑霞',
    role: 'G7易流高级副总裁',
    avatar: '/team/hao-shuxia.svg',
    bioText:
      '郝淑霞女士，现任G7易流高级副总裁，负责组织发展、人才体系与大客户战略生态协同。\n长期致力于科技企业的组织创新与数字化领导力建设，打造了覆盖全国核心物流节点的高效服务团队。',
  },
  {
    name: '张杰龙',
    role: 'G7易流首席财务官',
    avatar: '/team/zhang-jielong.svg',
    bioText:
      '张杰龙先生，现任G7易流首席财务官（CFO），负责集团财务战略、资本市场运作及战略投资并购。\n主导完成了公司多轮国际顶级投资机构战略融资及与易流科技的战略合并，推动公司构建稳健可持续的商业增长模型。',
  },
]

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

const { view, loading, saving, publishing, loadError, load, save, publish } =
  useConfig<Record<string, any>>('ABOUT')

const form = reactive({
  heroTitle: '以物联网技术和软件服务改变公路货运',
  introTitle: '关于G7易流',
  intro: '',
  introImage: '',
  ceoQuote: DEFAULT_CEO_QUOTE,
  ceoTitle: DEFAULT_CEO_TITLE,
  ceoSignature: DEFAULT_CEO_SIGNATURE,
  ceoButtonText: '展开全部',
  ceoExcerptText: DEFAULT_CEO_EXCERPT,
  ceoFullText: DEFAULT_CEO_FULL,
  teamTitle: 'G7易流管理团队',
  teamMembers: [] as TeamMemberForm[],
  resourcesTitle: '资料库',
  resources: [] as ResourceItemForm[],
  customerStories: [] as StoryItemForm[],
  mission: '以科技创新和服务升级物流生产力，让产业参与者共享美好改变',
  vision: '物流产业最受尊重的SaaS公司',
  values: [] as ValueItemForm[],
  joinTitle: 'G7易流路上见',
  joinButtonText: '查看职位',
  joinUrl: '',
  milestones: [] as Milestone[],
  offices: [] as OfficeItemForm[],
  qualifications: [] as Qualification[],
})

function fill() {
  const draft = view.value?.draft
  if (!draft) return
  form.heroTitle = draft.heroTitle ?? '以物联网技术和软件服务改变公路货运'
  form.introTitle = draft.introTitle ?? '关于G7易流'
  form.intro = (draft.intro && !draft.intro.includes('示例科技')) ? draft.intro : DEFAULT_INTRO
  form.introImage = draft.introImage ?? ''

  if (draft.ceoLetter) {
    form.ceoQuote = draft.ceoLetter.leftQuote || DEFAULT_CEO_QUOTE
    form.ceoTitle = draft.ceoLetter.title || DEFAULT_CEO_TITLE
    form.ceoSignature = draft.ceoLetter.signature || DEFAULT_CEO_SIGNATURE
    form.ceoButtonText = draft.ceoLetter.buttonText || '展开全部'
    form.ceoExcerptText = Array.isArray(draft.ceoLetter.excerpt) && draft.ceoLetter.excerpt.length > 0
      ? draft.ceoLetter.excerpt.join('\n')
      : DEFAULT_CEO_EXCERPT
    form.ceoFullText = Array.isArray(draft.ceoLetter.fullParagraphs) && draft.ceoLetter.fullParagraphs.length > 0
      ? draft.ceoLetter.fullParagraphs.join('\n')
      : DEFAULT_CEO_FULL
  } else {
    form.ceoQuote = DEFAULT_CEO_QUOTE
    form.ceoTitle = DEFAULT_CEO_TITLE
    form.ceoSignature = DEFAULT_CEO_SIGNATURE
    form.ceoButtonText = '展开全部'
    form.ceoExcerptText = DEFAULT_CEO_EXCERPT
    form.ceoFullText = DEFAULT_CEO_FULL
  }

  form.teamTitle = draft.teamTitle ?? 'G7易流管理团队'
  if (Array.isArray(draft.teamMembers) && draft.teamMembers.length > 0) {
    form.teamMembers = draft.teamMembers.map((m: any, idx: number) => {
      const match = DEFAULT_TEAM_MEMBERS.find((d) => d.name === m.name) || DEFAULT_TEAM_MEMBERS[idx]
      return {
        name: m.name ?? '',
        role: m.role ?? '',
        avatar: m.avatar || match?.avatar || '',
        bioText: Array.isArray(m.bio) && m.bio.length > 0
          ? m.bio.join('\n')
          : (match?.bioText || ''),
      }
    })
  } else {
    form.teamMembers = DEFAULT_TEAM_MEMBERS.map((m) => ({ ...m }))
  }

  form.resourcesTitle = draft.resourcesTitle ?? '资料库'
  if (Array.isArray(draft.resources) && draft.resources.length > 0) {
    form.resources = draft.resources.map((r: any) => ({
      category: r.category ?? '企业资料',
      title: r.title ?? '',
      logoText: r.logoText ?? '',
      logoColor: r.logoColor ?? '#4c2ce6',
      url: r.url ?? '',
      isVideo: Boolean(r.isVideo),
    }))
  } else {
    form.resources = DEFAULT_RESOURCES_LIST.map((r) => ({ ...r }))
  }

  if (Array.isArray(draft.customerStories) && draft.customerStories.length > 0) {
    form.customerStories = draft.customerStories.map((s: any) => ({
      quote: s.quote ?? '',
      author: s.author ?? '',
      company: s.company ?? '',
      logoText: s.logoText ?? '',
      videoUrl: s.videoUrl ?? '',
    }))
  } else {
    form.customerStories = DEFAULT_STORIES_LIST.map((s) => ({ ...s }))
  }

  form.mission = (draft.mission && !draft.mission.includes('设备状态'))
    ? draft.mission
    : '以科技创新和服务升级物流生产力，让产业参与者共享美好改变'
  form.vision = draft.vision ?? '物流产业最受尊重的SaaS公司'
  form.joinTitle = draft.joinTitle ?? 'G7易流路上见'
  form.joinButtonText = draft.joinButtonText ?? '查看职位'
  form.joinUrl = draft.joinUrl ?? ''

  if (Array.isArray(draft.values) && draft.values.length > 0) {
    form.values = draft.values.map((v: any) => ({
      title: v.title ?? '',
      linesText: Array.isArray(v.lines)
        ? v.lines.join('\n')
        : typeof v.lines === 'string'
          ? v.lines
          : '',
    }))
  } else {
    form.values = [
      { title: '客户成功', linesText: '我们存在的唯一目的就是为客户带来价值\n深入场景，洞察客户需求，解决客户痛点问题\n以可信赖的产品和有温度的服务，做客户的长期朋友' },
      { title: '极限', linesText: '目标远大，不甘平庸，不断挑战认知边界，把不可能变为可能\n敢想、敢试、敢突破，致力于打造有号召力的产品\n把事做透，用工匠精神实现卓越成果' },
      { title: '信任', linesText: '以身作则，说到做到\n敢于信任，从我做起\n主动寻找有共同理想的产业变革者，打造有格局的朋友圈' },
      { title: '坚韧', linesText: '坚持长期主义积极面对挑战和不确定性，敢于在泥泞中前行\n坚定目标，锲而不舍，迎难而上，确保结果\n充分发挥主观能动性，多找思路和方法，拒绝借口' },
      { title: '主人翁精神', linesText: '从全局出发，长期利益最大化\n主动担当，站在造钟者的角度思考并解决问题\n持续学习，自我迭代，与公司共同成长' },
    ]
  }

  const isDemoMilestones = Array.isArray(draft.milestones) && draft.milestones.some((m: any) => (m.description || '').includes('设备巡检'))
  if (Array.isArray(draft.milestones) && draft.milestones.length >= 3 && !isDemoMilestones) {
    form.milestones = draft.milestones.map((item: any) => ({ ...item }))
  } else {
    form.milestones = DEFAULT_MILESTONES_LIST.map((item) => ({ ...item }))
  }

  if (Array.isArray(draft.offices) && draft.offices.length > 0) {
    form.offices = draft.offices.map((o: any) => ({ ...o }))
  } else {
    form.offices = [
      { city: '北京（总部）', phone: '010-62667878', address: '北京市 海淀区 建材城中路 27 号金隅智造工场 N7 号楼 2 层', mapX: 68, mapY: 41 },
      { city: '深圳', phone: '0755-26944168', address: '深圳市南山区滨海大道 3369 号有线信息传输大厦 9 层', mapX: 68, mapY: 77 },
      { city: '成都', phone: '028-83358692', address: '成都市 天府新区 兴隆湖畔路西段 99 号天府新经济产业园 D 区 B6 栋 28 楼', mapX: 53, mapY: 63 },
      { city: '广州', phone: '', address: '广州市黄埔区科学大道237号A1栋1106室', mapX: 66, mapY: 74 },
      { city: '上海', phone: '021-51083899', address: '上海市闵行区申长路988弄虹桥万科中心T2号楼6层', mapX: 76, mapY: 57 },
      { city: '西安', phone: '', address: '西安市雁塔区丈八一路1号汇鑫IBC-D座8楼802、803室', mapX: 59, mapY: 52 },
      { city: '天津', phone: '', address: '天津市东丽区东二道空港商务园东区E6-102、202', mapX: 71, mapY: 45 },
    ]
  }
  form.qualifications = (draft.qualifications ?? []).map((item: any) => ({ ...item }))
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
          <el-form-item label="顶部首屏大标题">
            <el-input v-model="form.heroTitle" maxlength="100" show-word-limit />
          </el-form-item>
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

          <el-divider content-position="left">CEO的一封信与全文弹窗配置</el-divider>
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

        <el-form label-width="120px" class="form" style="margin-bottom: 16px">
          <el-form-item label="团队页面标题">
            <el-input v-model="form.teamTitle" maxlength="60" style="width: 320px" placeholder="G7易流管理团队" />
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
                  <el-input v-model="tm.name" placeholder="姓名（如：翟学魂）" style="width: 180px">
                    <template #prepend>姓名</template>
                  </el-input>
                  <el-input v-model="tm.role" placeholder="职务（如：G7易流首席执行官）" style="flex: 1">
                    <template #prepend>职务</template>
                  </el-input>
                </div>

                <div class="row-inline">
                  <el-input v-model="tm.avatar" placeholder="头像图片路径或 URL（如：/team/zhai-xuehun.svg）" style="width: 100%">
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
          <h2 class="preview-title">{{ form.teamTitle || 'G7易流管理团队' }}</h2>
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

        <el-form label-width="120px" class="form" style="margin-bottom: 14px">
          <el-form-item label="资料库专区大标题">
            <el-input v-model="form.resourcesTitle" maxlength="60" style="width: 320px" placeholder="资料库" />
          </el-form-item>
        </el-form>

        <h3 class="sec-h">资料共享（支持 企业资料 / 客户视频 / 行业报告 / ESG 四分类切换）</h3>
        <div v-for="(res, rIdx) in form.resources" :key="rIdx" class="card-box">
          <div class="row-inline">
            <el-select v-model="res.category" style="width: 130px">
              <el-option label="企业资料" value="企业资料" />
              <el-option label="客户视频" value="客户视频" />
              <el-option label="行业报告" value="行业报告" />
              <el-option label="ESG" value="ESG" />
            </el-select>
            <el-input v-model="res.title" placeholder="卡片标题（如：安全管家 / 滨拓物流）" style="width: 220px" />
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
            <el-input v-model="st.author" placeholder="受访人姓名与职务（如：张太喜 双汇物流董事长）" style="width: 280px" />
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
        <el-form label-width="130px" class="form">
          <el-form-item label="招聘横幅标题">
            <el-input v-model="form.joinTitle" maxlength="60" placeholder="G7易流路上见" />
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

        <h3 class="sec-h">核心价值观（完整包含：客户成功、极限、信任、坚韧、主人翁精神）</h3>
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

      <!-- Tab 5: 发展历程 & 7 城联系网点 -->
      <el-tab-pane label="5. 发展历程 & 7 城联系我们地图" name="milestones">
        <el-alert
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 16px"
          title="发展历程与 7 城网点说明：时间轴轮播与 7 城联系地图直接展示在官网「关于我们 - 企业简介」页面下方（亦同步在关于我们底部呈现）。修改后点击右上角「保存草稿」或「发布上线」即刻生效。"
        />
        <h3 class="sec-h">发展历程时间轴节点</h3>
        <div v-for="(item, index) in form.milestones" :key="index" class="row-inline">
          <el-input v-model="item.year" maxlength="4" placeholder="年份(2022)" style="width: 110px" />
          <el-input v-model="item.title" maxlength="50" placeholder="节点主事件" style="width: 240px" />
          <el-input v-model="item.description" maxlength="300" placeholder="多条事件可用分号 ； 分隔" style="flex: 1" />
          <el-button link type="danger" @click="form.milestones.splice(index, 1)">删除</el-button>
        </div>
        <el-button size="small" @click="addMilestone">+ 添加发展历程</el-button>

        <el-divider />
        <h3 class="sec-h">全国办公网点列表（对应左侧内部滚动容器与右侧中国地图坐标点）</h3>
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
      </el-tab-pane>
    </el-tabs>
  </ConfigPanel>
</template>

<style scoped>
.form {
  max-width: 780px;
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
