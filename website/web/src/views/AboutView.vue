<!-- 对应源码路径: website/web/src/views/AboutView.vue -->
<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAbout, getNews, getSite } from '@/api/public'
import { openConsult } from '@/composables/consult'
import { openVideo } from '@/composables/videoModal'
import { usePageMeta } from '@/composables/usePageMeta'
import { vReveal } from '@/directives/reveal'
import type {
  AboutCeoLetter,
  AboutOfficeItem,
  AboutPage,
  AboutResourceItem,
  AboutTeamMember,
  AboutValueItem,
  NewsSummary,
} from '@/data/types'

const route = useRoute()
const router = useRouter()
const data = ref<AboutPage | null>(null)
const siteName = ref('公司官网')
const newsList = ref<NewsSummary[]>([])
const activeTab = ref<'intro' | 'team' | 'media' | 'resources' | 'join'>('intro')

const ceoModalOpen = ref(false)
const activeTeamMember = ref<AboutTeamMember | null>(null)
const milestoneIndex = ref(2)
const newsStartIndex = ref(0)
const activeOfficeIndex = ref(0)
const activeResourceCat = ref('企业资料')
const resourceStartIndex = ref(0)

usePageMeta(() => ({
  title: '关于我们',
  description:
    data.value?.intro?.slice(0, 120) ||
    '了解企业简介、CEO致辞、发展历程、管理团队、媒体中心、资料库与加入我们。',
}))

const TABS: Array<{ key: 'intro' | 'team' | 'media' | 'resources' | 'join'; label: string }> = [
  { key: 'intro', label: '企业简介' },
  { key: 'team', label: '管理团队' },
  { key: 'media', label: '媒体中心' },
  { key: 'resources', label: '资料共享' },
  { key: 'join', label: '加入我们' },
]

function syncTabFromQuery() {
  const q = String(route.query.tab || '').toLowerCase()
  if (q === 'team' || q === 'culture') activeTab.value = 'team'
  else if (q === 'media') activeTab.value = 'media'
  else if (q === 'resources' || q === 'milestones') activeTab.value = 'resources'
  else if (q === 'join') activeTab.value = 'join'
  else activeTab.value = 'intro'
}

watch(
  () => route.query.tab,
  () => {
    syncTabFromQuery()
  },
)

function switchTab(tabKey: 'intro' | 'team' | 'media' | 'resources' | 'join') {
  activeTab.value = tabKey
  router.replace({ path: '/about', query: { tab: tabKey }, hash: '#part' }).catch(() => {})
}

const heroTitle = computed(() => data.value?.heroTitle || '以物联网技术和软件服务改变公路货运')
const introTitle = computed(() => data.value?.introTitle || `关于${siteName.value}`)

const introParagraphs = computed(() => {
  const raw = (data.value?.intro || '').trim()
  if (raw) {
    return raw.split(/\n+/).filter(Boolean)
  }
  return [
    `北京汇通天下物联科技有限公司（${siteName.value}）是一家领先的公路货运行业物联网科技和软件服务公司。为货运经营者提供从车队管理、安全管理、货物管理到智能调度、自动结算的一体化操作平台，连接货运全流程并以数据和AI驱动运营，助力货运经营者降本增效。`,
    `${siteName.value}的客户覆盖超80%上游头部货主以及35000+下游中小物流企业，服务网络覆盖全国核心物流节点与产业园区。`,
  ]
})

// 1. CEO的一封信（1:1 还原 about_06.png & about_08.png）
const DEFAULT_CEO_LETTER: AboutCeoLetter = {
  leftQuote: '“我谨代表G7易流全体员工，向在这个不凡时代中努力前行的货运经营者致以崇高的敬意！”',
  title: '从微不足道开始改变世界',
  excerpt: [
    '十多年前，当我们刚开始带着第一代车载终端走进物流园、爬上驾驶室的时候，很少有人相信，一辆大货车上的小小黑盒子，有一天会彻底改变整个中国公路货运的运行方式。',
    '公路货运是国民经济的血脉，千万名卡车司机和数万家货运经营者日夜兼程。然而在过去很长一段时间里，安全风险高、油费过路费核算难、在途货物状态黑盒，一直是压在每一位车队经营者心头的三座大山。',
    '正是从每一次毫秒级的定位、每一次防碰撞预警、每一滴油的精准计量这些看似“微不足道”的小事开始，我们将物联网感知技术与AI算法融入货运现场，陪伴客户一步步走出粗放经验管理，迈入数字化精细运营的新时代。',
  ],
  signature: '翟学魂',
  buttonText: '展开全部',
  fullParagraphs: [
    '我谨代表G7易流全体员工，向在这个不凡时代中努力前行的货运经营者致以崇高的敬意！',
    '十多年前，当我们刚开始带着第一代车载终端走进物流园、爬上驾驶室的时候，很少有人相信，一辆大货车上的小小黑盒子，有一天会彻底改变整个中国公路货运的运行方式。',
    '公路货运是国民经济的血脉，千万名卡车司机和数万家货运经营者日夜兼程。然而在过去很长一段时间里，安全风险高、油费过路费核算难、在途货物状态黑盒，一直是压在每一位车队经营者心头的三座大山。',
    '正是从每一次毫秒级的定位、每一次防碰撞预警、每一滴油的精准计量这些看似“微不足道”的小事开始，我们将物联网感知技术与AI算法融入货运现场，陪伴客户一步步走出粗放经验管理，迈入数字化精细运营的新时代。',
    '今天，超过800万台物联网设备在我们的平台上实时在线，每天产生超12TB的真实运营数据与1亿次车辆事件交互。当大模型与全场景AI工具「紫宝盒」走进车队，数据不再只是冰冷的报表，而是能够主动看见风险、听懂指令、帮车队省钱管事的“数字员工”。',
    '时代在变，技术在变，但我们存在的唯一理由始终未变——用科技创新和服务升级物流生产力，做货运经营者最值得信赖的长期伙伴。让我们继续从微不足道处着手，一起改变世界！',
  ],
}

const ceoLetter = computed<AboutCeoLetter>(() => {
  if (data.value?.ceoLetter && data.value.ceoLetter.title) {
    return {
      ...DEFAULT_CEO_LETTER,
      ...data.value.ceoLetter,
    }
  }
  return DEFAULT_CEO_LETTER
})

// 2. 发展历程（1:1 还原 about_18.png 循环年份轴 + 3列卡片中间悬浮靶心）
interface NormalizedMilestone {
  year: string
  bullets: string[]
}

const DEFAULT_MILESTONES: NormalizedMilestone[] = [
  {
    year: '2018',
    bullets: [
      '发布智能挂车与安全管家托管服务',
      '连接平台货运车辆突破 80 万台',
      '完成由多家国际顶级机构联合领投的战略融资',
    ],
  },
  {
    year: '2019',
    bullets: [
      '平台卡车连接数突破140万',
      '面向煤炭、钢铁、水泥、汽车、快递、快运等多个行业提供综合性数字化解决方案',
      '与中国铝业、普洛斯、丸红等设立合资公司，服务生态扩张',
    ],
  },
  {
    year: '2022',
    bullets: [
      '平台卡车连接数突破 200万',
      '宣布完成新一轮2亿美元融资',
      '嬴彻科技自动驾驶卡车量产交付',
    ],
  },
  {
    year: '2010',
    bullets: [
      '向物流运输公司提供基于北斗和GPS位置信息的车队管理 SaaS 服务',
      '平台连接车辆2000台',
    ],
  },
  {
    year: '2014',
    bullets: [
      '推出智能管车与车队金融结算服务体系',
      '全国设立七大区域运营中心，服务超万家物流企业',
    ],
  },
]

const milestonesList = computed<NormalizedMilestone[]>(() => {
  const raw = data.value?.milestones ?? []
  if (raw.length >= 3) {
    return raw.map((m) => {
      const text = [m.title, m.description].filter(Boolean).join('；')
      const bullets = text
        .split(/[\n；;]+/)
        .map((s) => s.trim())
        .filter(Boolean)
      return {
        year: m.year,
        bullets: bullets.length ? bullets : [m.title || '关键发展节点'],
      }
    })
  }
  return DEFAULT_MILESTONES
})

const visibleYearWindow = computed(() => {
  const list = milestonesList.value
  const total = list.length
  if (total === 0) return []
  const count = Math.min(5, total)
  const half = Math.floor(count / 2)
  const result: Array<{ index: number; year: string; isCenter: boolean }> = []
  for (let offset = -half; offset <= half; offset++) {
    if (result.length >= count) break
    const idx = (milestoneIndex.value + offset + total) % total
    result.push({
      index: idx,
      year: list[idx].year,
      isCenter: idx === milestoneIndex.value,
    })
  }
  return result
})

const visibleMilestoneCards = computed(() => {
  const list = milestonesList.value
  const total = list.length
  if (total === 0) return []
  const prevIdx = (milestoneIndex.value - 1 + total) % total
  const nextIdx = (milestoneIndex.value + 1) % total
  return [
    { index: prevIdx, item: list[prevIdx], isCenter: false },
    { index: milestoneIndex.value, item: list[milestoneIndex.value], isCenter: true },
    { index: nextIdx, item: list[nextIdx], isCenter: false },
  ]
})

function prevMilestone() {
  const total = milestonesList.value.length
  if (!total) return
  milestoneIndex.value = (milestoneIndex.value - 1 + total) % total
}

function nextMilestone() {
  const total = milestonesList.value.length
  if (!total) return
  milestoneIndex.value = (milestoneIndex.value + 1) % total
}

// 3. 联系我们：左侧 7 城内部滚动容器 + 右侧地图与大楼实景照片切换（1:1 还原 about_28.png & about_35.png）
const DEFAULT_OFFICES: AboutOfficeItem[] = [
  {
    city: '北京（总部）',
    phone: '010-62667878',
    address: '北京市 海淀区 建材城中路 27 号金隅智造工场 N7 号楼 2 层',
    mapX: 68,
    mapY: 41,
  },
  {
    city: '深圳',
    phone: '0755-26944168',
    address: '深圳市南山区滨海大道 3369 号有线信息传输大厦 9 层',
    mapX: 68,
    mapY: 77,
  },
  {
    city: '成都',
    phone: '028-83358692',
    address: '成都市 天府新区 兴隆湖畔路西段 99 号天府新经济产业园 D 区 B6 栋 28 楼',
    mapX: 53,
    mapY: 63,
  },
  {
    city: '广州',
    phone: '',
    address: '广州市黄埔区科学大道237号A1栋1106室',
    mapX: 66,
    mapY: 74,
  },
  {
    city: '上海',
    phone: '021-51083899',
    address: '上海市闵行区申长路988弄虹桥万科中心T2号楼6层',
    mapX: 76,
    mapY: 57,
  },
  {
    city: '西安',
    phone: '',
    address: '西安市雁塔区丈八一路1号汇鑫IBC-D座8楼802、803室',
    mapX: 59,
    mapY: 52,
  },
  {
    city: '天津',
    phone: '',
    address: '天津市东丽区东二道空港商务园东区E6-102、202',
    mapX: 71,
    mapY: 45,
  },
]

const officesList = computed<AboutOfficeItem[]>(() => {
  if (data.value?.offices && data.value.offices.length > 0) {
    return data.value.offices
  }
  return DEFAULT_OFFICES
})

const currentOffice = computed(
  () => officesList.value[activeOfficeIndex.value] ?? officesList.value[0],
)

// 4. 管理团队 4 位高管 + 点击居中左右分栏弹窗（1:1 还原 about_48.png & about_62.png）
const DEFAULT_TEAM: AboutTeamMember[] = [
  {
    name: '翟学魂',
    role: 'G7易流首席执行官',
    bio: [
      '翟学魂先生，现任物联网软件服务公司G7易流首席执行官。',
      '作为中国产业物联网和物流数字化的开拓者，打造了商用车物联网平台。翟学魂先生是中国物流行业的著名思想领袖之一，推动了中国物流企业的信息化和数字化进程。联合发起成立了自动驾驶重卡领域的独角兽企业嬴彻科技，以及电动重卡运营平台G7绿港，致力于积极推进物流产业的智能化和绿色化。',
      '曾获得“中国物流行业十大风云人物”、“中国物流行业年度人物”、“改革开放40年物流行业企业家代表性人物”等荣誉名称。',
      '翟学魂先生毕业于清华大学物理系，并同期完成清华大学经管学院双学士学位学业。',
    ],
  },
  {
    name: '张景涛',
    role: 'G7易流总裁',
    bio: [
      '张景涛先生，现任G7易流总裁，全面负责公司产品研发、冷链与大宗行业解决方案及客户成功体系建设。',
      '深耕供应链与物流科技领域二十余年，推动了软硬一体物联网终端与全场景SaaS平台的深度融合，助力数万家货运经营者实现安全与效率跃升。',
    ],
  },
  {
    name: '郝淑霞',
    role: 'G7易流高级副总裁',
    bio: [
      '郝淑霞女士，现任G7易流高级副总裁，负责组织发展、人才体系与大客户战略生态协同。',
      '长期致力于科技企业的组织创新与数字化领导力建设，打造了覆盖全国核心物流节点的高效服务团队。',
    ],
  },
  {
    name: '张杰龙',
    role: 'G7易流首席财务官',
    bio: [
      '张杰龙先生，现任G7易流首席财务官（CFO），负责集团财务战略、资本市场运作及战略投资并购。',
      '主导完成了公司多轮国际顶级投资机构战略融资及与易流科技的战略合并，推动公司构建稳健可持续的商业增长模型。',
    ],
  },
]

const teamList = computed<AboutTeamMember[]>(() => {
  if (data.value?.teamMembers && data.value.teamMembers.length > 0) {
    return data.value.teamMembers
  }
  return DEFAULT_TEAM
})

// 5. 媒体中心 4 卡横向轮播
const DEFAULT_NEWS: NewsSummary[] = [
  {
    id: '1',
    title: 'G7与E6宣布完成合并',
    summary: '双方正式完成战略合并，共同打造公路货运行业领先的物联网与软件服务平台。',
    cover: null,
    categoryId: null,
    categoryName: '企业动态',
    displayTime: '2022/06/07',
  },
  {
    id: '2',
    title: '让你的物流AI开工！2026数字物流大会精彩日程抢先看',
    summary: '聚焦物流全场景 AI 智能体与 IoT 深度融合实践。',
    cover: null,
    categoryId: null,
    categoryName: '行业峰会',
    displayTime: '2026/10/16',
  },
  {
    id: '3',
    title: '走G7「新」路 马上出发！——老翟新能源车队一线探访',
    summary: '深入干线与大宗运输现场，探访新能源重卡车队真实运营效益。',
    cover: null,
    categoryId: null,
    categoryName: '前沿洞察',
    displayTime: '2026/10/13',
  },
  {
    id: '4',
    title: '2026物流小巨人启动——「新能源&AI」双榜单全新招募！',
    summary: '寻找物流产业最具技术领导力与增长韧性的标杆企业。',
    cover: null,
    categoryId: null,
    categoryName: '品牌活动',
    displayTime: '2026/09/23',
  },
]

const resolvedNews = computed<NewsSummary[]>(() => {
  return newsList.value.length > 0 ? newsList.value : DEFAULT_NEWS
})

const visibleNewsCards = computed(() => {
  const list = resolvedNews.value
  if (list.length <= 4) return list
  const out: NewsSummary[] = []
  for (let i = 0; i < 4; i++) {
    out.push(list[(newsStartIndex.value + i) % list.length])
  }
  return out
})

function prevNews() {
  const len = resolvedNews.value.length
  if (len <= 4) return
  newsStartIndex.value = (newsStartIndex.value - 1 + len) % len
}

function nextNews() {
  const len = resolvedNews.value.length
  if (len <= 4) return
  newsStartIndex.value = (newsStartIndex.value + 1) % len
}

// 6. 资料共享（资料库 4 个子分类切换 + 4 卡轮播 + 右下角圆形下载/播放按钮 ↓，1:1 还原 about_70.png & about_74.png）
const RESOURCE_CATS = ['企业资料', '客户视频', '行业报告', 'ESG']

const DEFAULT_RESOURCES: AboutResourceItem[] = [
  // 企业资料
  { category: '企业资料', title: 'G7易流企业宣传册', logoText: 'G7易流 全栈产品手册', logoColor: '#4c2ce6' },
  { category: '企业资料', title: '安全管家', logoText: 'G 安全管家 · 主动安全托管', logoColor: '#2563eb' },
  { category: '企业资料', title: '财运通', logoText: 'G 财运通 · 数字化运力结算', logoColor: '#0284c7' },
  { category: '企业资料', title: '冷链管家', logoText: 'G 冷链管家 · 全程温控可视', logoColor: '#0d9488' },
  // 客户视频（1:1 还原 about_74.png：滨拓物流 / 中国华电 / 双汇物流 / 天佑物流）
  {
    category: '客户视频',
    title: '滨拓物流',
    logoText: '滨拓物流',
    logoColor: '#16a34a',
    isVideo: true,
    url: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
  },
  {
    category: '客户视频',
    title: '华电能源',
    logoText: '中国华电 CHD',
    logoColor: '#0284c7',
    isVideo: true,
    url: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
  },
  {
    category: '客户视频',
    title: '双汇物流',
    logoText: '双汇物流',
    logoColor: '#dc2626',
    isVideo: true,
    url: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
  },
  {
    category: '客户视频',
    title: '天佑物流',
    logoText: 'tianyou天佑',
    logoColor: '#d97706',
    isVideo: true,
    url: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
  },
  // 行业报告
  { category: '行业报告', title: '2026新能源车队管理白皮书', logoText: '新能源车队管理白皮书', logoColor: '#0284c7' },
  { category: '行业报告', title: '2026上半年冷链物流行业报告', logoText: '中国冷链物流大数据报告', logoColor: '#2563eb' },
  { category: '行业报告', title: '中国公路货运数字安全蓝皮书', logoText: '公路货运安全AI治理报告', logoColor: '#4f46e5' },
  { category: '行业报告', title: '大宗物流数字化转型实践指南', logoText: '大宗物流无人化协同白皮书', logoColor: '#334155' },
  // ESG
  { category: 'ESG', title: 'G7易流环境、社会及管治（ESG）报告', logoText: 'ESG 可持续发展报告', logoColor: '#059669' },
  { category: 'ESG', title: '绿色物流与电动重卡减碳核算标准', logoText: '物流双碳与绿色运力实践', logoColor: '#10b981' },
  { category: 'ESG', title: '卡车司机安全守护与公益行动年报', logoText: '千万卡车司机安全守护计划', logoColor: '#0d9488' },
  { category: 'ESG', title: '数据安全与隐私合规治理白皮书', logoText: '信息安全与隐私保护合规', logoColor: '#0284c7' },
]

const filteredResources = computed<AboutResourceItem[]>(() => {
  const pool =
    data.value?.resources && data.value.resources.length > 0
      ? [...data.value.resources, ...DEFAULT_RESOURCES]
      : DEFAULT_RESOURCES
  const list = pool.filter((r) => r.category === activeResourceCat.value)
  return list.length > 0 ? list : DEFAULT_RESOURCES.slice(0, 4)
})

const visibleResources = computed(() => {
  const list = filteredResources.value
  if (list.length <= 4) return list
  const out: AboutResourceItem[] = []
  for (let i = 0; i < 4; i++) {
    out.push(list[(resourceStartIndex.value + i) % list.length])
  }
  return out
})

function handleResourceClick(item: AboutResourceItem) {
  if (item.isVideo || item.category === '客户视频') {
    openVideo({
      title: `${item.title} · 客户证言视频`,
      url:
        item.url ||
        'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
    })
    return
  }
  if (item.url && item.url.startsWith('http')) {
    window.open(item.url, '_blank', 'noopener,noreferrer')
    return
  }
  openConsult({
    sourcePosition: 'ABOUT_RESOURCE',
    title: '获取企业资料与行业报告',
    initialDemand: `申请获取《${item.title}》完整版 PDF 资料`,
  })
}

// 7. 加入我们：使命 / 愿景 / 5 大价值观（含第5项「主人翁精神」，1:1 还原 about_84.png & 01:20-01:36）
const missionText = computed(
  () => data.value?.mission || '以科技创新和服务升级物流生产力，让产业参与者共享美好改变',
)
const visionText = computed(() => data.value?.vision || '物流产业最受尊重的SaaS公司')

const DEFAULT_VALUES: AboutValueItem[] = [
  {
    title: '客户成功',
    lines: [
      '我们存在的唯一目的就是为客户带来价值',
      '客户的需求是公司发展、产品创新的源泉:深入场景，洞察客户需求，解决客户痛点问题',
      '以可信赖的产品和有温度的服务，满足甚至超越客户的合理期望，做客户的长期朋友',
    ],
  },
  {
    title: '极限',
    lines: [
      '目标远大，不甘平庸，不断挑战认知边界，把不可能变为可能',
      '敢想、敢试、敢突破，致力于打造有号召力的产品',
      '把事做透，用工匠精神实现卓越成果',
    ],
  },
  {
    title: '信任',
    lines: [
      '以身作则，说到做到',
      '敢于信任，从我做起',
      '主动寻找有共同理想的产业变革者，善于建立信任，打造有格局的朋友圈',
    ],
  },
  {
    title: '坚韧',
    lines: [
      '坚持长期主义积极面对挑战和不确定性，敢于在泥泞中前行',
      '坚定目标，锲而不舍，迎难而上，确保结果',
      '充分发挥主观能动性，多找思路和方法，拒绝借口',
    ],
  },
  {
    title: '主人翁精神',
    lines: [
      '从全局出发，长期利益最大化',
      '主动担当，站在造钟者的角度思考并解决问题',
      '持续学习，自我迭代，与公司共同成长',
    ],
  },
]

const valuesList = computed<AboutValueItem[]>(() => {
  if (data.value?.values && data.value.values.length >= 5) {
    return data.value.values
  }
  if (data.value?.values && data.value.values.length > 0) {
    const merged = [...data.value.values]
    if (!merged.some((v) => v.title.includes('主人翁'))) {
      merged.push(DEFAULT_VALUES[4])
    }
    return merged
  }
  return DEFAULT_VALUES
})

function handleJoinClick() {
  if (data.value?.joinUrl && data.value.joinUrl.startsWith('http')) {
    window.open(data.value.joinUrl, '_blank', 'noopener,noreferrer')
    return
  }
  openConsult({
    sourcePosition: 'ABOUT_JOIN',
    title: '查看热招职位与投递简历',
    initialDemand: '我希望了解当前开放的研发/产品/解决方案岗位并投递简历。',
  })
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') {
    ceoModalOpen.value = false
    activeTeamMember.value = null
  }
}

onMounted(async () => {
  syncTabFromQuery()
  document.addEventListener('keydown', onKeydown)
  const [aboutRes, siteRes, newsRes] = await Promise.all([
    getAbout().catch(() => null),
    getSite().catch(() => null),
    getNews({ page: 1, pageSize: 8 }).catch(() => ({ items: [] })),
  ])
  if (aboutRes) {
    data.value = aboutRes
  }
  if (siteRes?.siteName) {
    siteName.value = siteRes.siteName
  }
  if (newsRes?.items?.length) {
    newsList.value = newsRes.items
  }
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', onKeydown)
})
</script>

<template>
  <div class="about-g7">
    <!-- 1. 顶部深空流光透视首屏 Banner（1:1 还原 关于我们.mp4） -->
    <section class="about-hero">
      <div class="about-hero__trails" aria-hidden="true"></div>
      <div class="container about-hero__inner">
        <div v-reveal class="about-hero__logo-mark" aria-hidden="true">G</div>
        <h1 v-reveal="100" class="about-hero__title">{{ heroTitle }}</h1>
      </div>
    </section>

    <!-- 2. 悬浮白底五页签导航条（#part，支持点击独立切换 5 大子页面，并与页脚关于我们深链同步） -->
    <div id="part" class="container about-tabs-wrap">
      <nav v-reveal="140" class="about-tabs" aria-label="关于我们五页签导航">
        <button
          v-for="tab in TABS"
          :key="tab.key"
          type="button"
          class="about-tabs__btn"
          :class="{ 'is-active': activeTab === tab.key }"
          @click="switchTab(tab.key)"
        >
          {{ tab.label }}
        </button>
      </nav>
    </div>

    <!-- ==================== 子页面 1：企业简介 (intro) ==================== -->
    <div v-if="activeTab === 'intro'" key="subpage-intro" class="about-subpage">
      <!-- (1) 关于G7易流：左文右图 -->
      <section class="about-intro-sec">
        <div class="container about-intro-grid">
          <div v-reveal class="about-intro__text">
            <h2 class="about-sec-title">{{ introTitle }}</h2>
            <p v-for="(para, pIdx) in introParagraphs" :key="pIdx" class="about-intro__para">
              {{ para }}
            </p>
          </div>

          <div v-reveal="120" class="about-intro__visual">
            <div class="about-intro__photo">
              <img v-if="data?.introImage" :src="data.introImage" :alt="introTitle" class="about-intro__img" />
              <template v-else>
                <div class="about-intro__radar" aria-hidden="true"></div>
                <div class="about-intro__truck-scene" aria-hidden="true">
                  <span class="scene-tag">AI + IoT 全链路实时感知</span>
                </div>
              </template>
            </div>
          </div>
        </div>
      </section>

      <!-- (2) CEO的一封信（1:1 还原 about_06.png 左暗色公路金句卡 + 右浅灰蓝背景信件摘录 + 展开全部弹窗） -->
      <section class="about-ceo-sec">
        <div class="container">
          <h2 v-reveal class="about-center-title">CEO的一封信</h2>

          <div v-reveal="100" class="ceo-card">
            <div class="ceo-card__left">
              <p class="ceo-card__quote">{{ ceoLetter.leftQuote }}</p>
            </div>

            <div class="ceo-card__right">
              <div class="ceo-card__watermark" aria-hidden="true">G</div>
              <h3 class="ceo-card__title">{{ ceoLetter.title }}</h3>
              <div class="ceo-card__paras">
                <p v-for="(p, idx) in ceoLetter.excerpt" :key="idx">{{ p }}</p>
              </div>
              <div class="ceo-card__foot">
                <button
                  type="button"
                  class="about-outline-btn"
                  @click="ceoModalOpen = true"
                >
                  {{ ceoLetter.buttonText || '展开全部' }}
                </button>
                <span class="ceo-card__sign">{{ ceoLetter.signature }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- (3) 发展历程横向时间轴轮播（1:1 还原 about_18.png） -->
      <section class="about-history-sec">
        <div class="container">
          <h2 v-reveal class="about-center-title">发展历程</h2>

          <div v-reveal="100" class="history-axis">
            <button type="button" class="history-arrow" aria-label="上一年份" @click="prevMilestone">‹</button>
            <div class="history-years">
              <button
                v-for="yr in visibleYearWindow"
                :key="`${yr.index}-${yr.year}`"
                type="button"
                class="history-year-btn"
                :class="{ 'is-center': yr.isCenter }"
                @click="milestoneIndex = yr.index"
              >
                {{ yr.year }}
              </button>
            </div>
            <button type="button" class="history-arrow" aria-label="下一年份" @click="nextMilestone">›</button>
          </div>

          <div v-reveal="160" class="history-stage">
            <div
              v-for="card in visibleMilestoneCards"
              :key="`card-${card.index}`"
              class="history-card"
              :class="{ 'is-center': card.isCenter }"
              @click="milestoneIndex = card.index"
            >
              <div class="history-card__year">{{ card.item.year }}</div>
              <div class="history-card__line">
                <span v-if="card.isCenter" class="history-card__target" aria-hidden="true"></span>
                <span v-else class="history-card__dot" aria-hidden="true"></span>
              </div>
              <ul class="history-card__bullets">
                <li v-for="(b, bIdx) in card.item.bullets" :key="bIdx">{{ b }}</li>
              </ul>
            </div>
          </div>
        </div>
      </section>

      <!-- (3.5) 荣誉资质与权威认证（开发文档 6.11 qualifications[]，有配置时展示，无内容自动隐藏） -->
      <section v-if="data?.qualifications?.length" class="about-quals-sec">
        <div class="container">
          <h2 v-reveal class="about-center-title">荣誉与资质认证</h2>
          <div class="quals-grid">
            <div
              v-for="(qItem, qIdx) in data.qualifications"
              :key="qItem.name + qIdx"
              v-reveal="qIdx * 60"
              class="qual-card"
            >
              <img v-if="qItem.imageUrl" :src="qItem.imageUrl" :alt="qItem.name" class="qual-card__img" />
              <h3 class="qual-card__name">{{ qItem.name }}</h3>
              <p v-if="qItem.description" class="qual-card__desc">{{ qItem.description }}</p>
            </div>
          </div>
        </div>
      </section>

      <!-- (4) 联系我们：左侧 7 城内部滚动容器 + 右侧点阵地图与大楼实景照片气泡（1:1 还原 about_28.png & about_35.png） -->
      <section class="about-offices-sec">
        <div class="container">
          <h2 v-reveal class="about-center-title">联系我们</h2>

          <div class="offices-layout">
            <!-- 左侧带自定义深色滚动条的 7 城滚动列表 -->
            <div v-reveal="80" class="offices-scroll-box">
              <div
                v-for="(off, oIdx) in officesList"
                :key="off.city"
                class="office-item"
                :class="{ 'is-active': activeOfficeIndex === oIdx }"
                @mouseenter="activeOfficeIndex = oIdx"
                @click="activeOfficeIndex = oIdx"
              >
                <h3 class="office-item__city">{{ off.city }}</h3>
                <p class="office-item__line">
                  <span>电话：</span>{{ off.phone || '' }}
                </p>
                <p class="office-item__line">
                  <span>地址：</span>{{ off.address }}
                </p>
              </div>
            </div>

            <!-- 右侧点阵地图 + 对应城市大厦照片缩略框 -->
            <div v-reveal="140" class="offices-map">
              <div class="offices-map__canvas">
                <div
                  class="map-photo-popup"
                  :style="{
                    left: `${Math.min(76, Math.max(28, currentOffice?.mapX || 66))}%`,
                    top: `${Math.max(6, (currentOffice?.mapY || 48) - 36)}%`,
                  }"
                >
                  <div class="map-photo-popup__img">
                    <img
                      v-if="currentOffice?.image"
                      :src="currentOffice.image"
                      :alt="currentOffice.city"
                    />
                    <div v-else class="building-illustration" :class="`b-theme--${activeOfficeIndex % 4}`">
                      <div class="building-skyline"></div>
                      <span class="building-illustration__tag">{{ currentOffice?.city }}办公区实景</span>
                    </div>
                  </div>
                  <span class="map-photo-popup__arrow" aria-hidden="true"></span>
                </div>

                <!-- 7 城坐标圆点 -->
                <div class="map-pins">
                  <button
                    v-for="(off, oIdx) in officesList"
                    :key="off.city"
                    type="button"
                    class="map-pin"
                    :class="{ 'is-active': activeOfficeIndex === oIdx }"
                    :style="{ left: `${off.mapX || 65}%`, top: `${off.mapY || 42 + oIdx * 6}%` }"
                    @click="activeOfficeIndex = oIdx"
                  >
                    <span class="map-pin__dot"></span>
                    <span class="map-pin__name">{{ off.city.replace(/（.*）/, '') }}</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- ==================== 子页面 2：管理团队 (team，1:1 还原 about_62.png & about_48.png) ==================== -->
    <div v-else-if="activeTab === 'team'" key="subpage-team" class="about-subpage">
      <section class="about-team-sec">
        <div class="container">
          <h2 v-reveal class="about-center-title">
            {{ data?.teamTitle || `${siteName}管理团队` }}
          </h2>

          <div class="team-grid">
            <article
              v-for="(member, mIdx) in teamList"
              :key="member.name"
              v-reveal="mIdx * 80"
              class="team-card"
              @click="activeTeamMember = member"
            >
              <div class="team-card__portrait" :class="`portrait-bg--${mIdx % 4}`">
                <img v-if="member.avatar" :src="member.avatar" :alt="member.name" />
                <div v-else class="portrait-placeholder">
                  <div class="portrait-placeholder__head"></div>
                  <div class="portrait-placeholder__suit"></div>
                </div>
              </div>
              <div class="team-card__info">
                <h3 class="team-card__name">{{ member.name }}</h3>
                <p class="team-card__role">{{ member.role }}</p>
              </div>
            </article>
          </div>
        </div>
      </section>
    </div>

    <!-- ==================== 子页面 3：媒体中心 (media，1:1 还原 00:56-01:06) ==================== -->
    <div v-else-if="activeTab === 'media'" key="subpage-media" class="about-subpage">
      <section class="about-news-sec">
        <div class="container">
          <div v-reveal class="about-news__head">
            <h2 class="about-sec-title">新闻中心</h2>
            <RouterLink to="/news" class="about-outline-btn">查看更多</RouterLink>
          </div>

          <div v-reveal="100" class="about-news-slider">
            <button type="button" class="slider-arrow" aria-label="上一组新闻" @click="prevNews">‹</button>

            <div class="about-news-grid">
              <RouterLink
                v-for="(item, nIdx) in visibleNewsCards"
                :key="`${item.id}-${nIdx}`"
                :to="`/news/${item.id}`"
                class="about-news-card"
              >
                <div class="about-news-card__poster" :class="`poster-theme--${nIdx % 4}`">
                  <img
                    v-if="item.cover?.url"
                    :src="item.cover.url"
                    :alt="item.cover.alt || item.title"
                    loading="lazy"
                  />
                  <div v-else class="poster-fallback">
                    <span class="poster-fallback__brand">{{ siteName }}</span>
                    <strong class="poster-fallback__text">{{ item.title.slice(0, 14) }}</strong>
                  </div>
                </div>
                <div class="about-news-card__body">
                  <h3 class="about-news-card__title">{{ item.title }}</h3>
                  <div class="about-news-card__time">
                    <span aria-hidden="true">🕒</span>
                    <span>{{ item.displayTime || '2026/10/16' }}</span>
                  </div>
                </div>
              </RouterLink>
            </div>

            <button type="button" class="slider-arrow" aria-label="下一组新闻" @click="nextNews">›</button>
          </div>
        </div>
      </section>
    </div>

    <!-- ==================== 子页面 4：资料共享 / 资料库 (resources，1:1 还原 about_70.png & about_74.png) ==================== -->
    <div v-else-if="activeTab === 'resources'" key="subpage-resources" class="about-subpage">
      <section class="about-res-sec">
        <div class="container">
          <h2 v-reveal class="about-center-title">{{ data?.resourcesTitle || '资料库' }}</h2>

          <!-- 4 个子分类切换条：企业资料 / 客户视频 / 行业报告 / ESG -->
          <div v-reveal="80" class="res-subtabs">
            <button
              v-for="cat in RESOURCE_CATS"
              :key="cat"
              type="button"
              class="res-subtabs__btn"
              :class="{ 'is-active': activeResourceCat === cat }"
              @click="activeResourceCat = cat; resourceStartIndex = 0"
            >
              {{ cat }}
            </button>
          </div>

          <!-- 4 卡横向轮播 + 右下角圆形下载/播放按钮 ↓ -->
          <div v-reveal="140" class="about-news-slider">
            <button
              type="button"
              class="slider-arrow"
              aria-label="上一组资料"
              @click="resourceStartIndex = Math.max(0, resourceStartIndex - 1)"
            >
              ‹
            </button>

            <div class="about-news-grid">
              <article
                v-for="(resItem, rIdx) in visibleResources"
                :key="`${resItem.title}-${rIdx}`"
                class="res-card"
                @click="handleResourceClick(resItem)"
              >
                <div class="res-card__cover" :class="{ 'is-logo-mode': resItem.category === '客户视频' }">
                  <img v-if="resItem.cover" :src="resItem.cover" :alt="resItem.title" />
                  <div
                    v-else-if="resItem.category === '客户视频'"
                    class="res-card__logo-box"
                  >
                    <strong :style="{ color: resItem.logoColor || '#1e293b' }">
                      {{ resItem.logoText || resItem.title }}
                    </strong>
                  </div>
                  <div
                    v-else
                    class="res-card__brochure"
                    :style="{ background: `linear-gradient(145deg, ${resItem.logoColor || '#4c2ce6'} 0%, #0f172a 100%)` }"
                  >
                    <span class="res-card__brochure-brand">G7易流</span>
                    <strong>{{ resItem.logoText || resItem.title }}</strong>
                  </div>
                </div>

                <div class="res-card__foot">
                  <h3 class="res-card__title">{{ resItem.title }}</h3>
                  <button
                    type="button"
                    class="res-card__dl-btn"
                    :title="resItem.category === '客户视频' ? '播放视频' : '下载资料'"
                    @click.stop="handleResourceClick(resItem)"
                  >
                    ↓
                  </button>
                </div>
              </article>
            </div>

            <button
              type="button"
              class="slider-arrow"
              aria-label="下一组资料"
              @click="resourceStartIndex = resourceStartIndex + 1"
            >
              ›
            </button>
          </div>
        </div>
      </section>
    </div>

    <!-- ==================== 子页面 5：加入我们 (join，1:1 还原 about_84.png + 使命/愿景/5项价值观) ==================== -->
    <div v-else-if="activeTab === 'join'" key="subpage-join" class="about-subpage">
      <!-- 顶部 G7易流路上见 海报横幅 -->
      <section class="about-join-sec">
        <div class="container">
          <div v-reveal class="join-banner">
            <div class="join-banner__content">
              <h2 class="join-banner__title">{{ data?.joinTitle || `${siteName}路上见` }}</h2>
              <button type="button" class="join-banner__btn" @click="handleJoinClick">
                {{ data?.joinButtonText || '查看职位' }}
              </button>
            </div>
          </div>
        </div>
      </section>

      <!-- 下方：使命 · 愿景 · 5大价值观（客户成功 / 极限 / 信任 / 坚韧 / 主人翁精神） -->
      <section class="about-culture-sec">
        <div class="container">
          <div v-reveal class="culture-row">
            <div class="culture-row__label">使命</div>
            <div class="culture-row__content">
              <h3 class="culture-row__headline">{{ missionText }}</h3>
            </div>
          </div>

          <div v-reveal="80" class="culture-row">
            <div class="culture-row__label">愿景</div>
            <div class="culture-row__content">
              <h3 class="culture-row__headline">{{ visionText }}</h3>
            </div>
          </div>

          <div class="culture-row culture-row--values">
            <div v-reveal class="culture-row__label">价值观</div>
            <div class="culture-row__content culture-values-list">
              <div
                v-for="(valItem, vIdx) in valuesList"
                :key="valItem.title"
                v-reveal="vIdx * 80"
                class="value-block"
              >
                <h4 class="value-block__title">{{ valItem.title }}</h4>
                <p v-for="(line, lIdx) in valItem.lines" :key="lIdx" class="value-block__line">
                  {{ line }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- ==================== 弹窗 A：CEO的一封信全文滚动弹窗（1:1 还原 about_08.png） ==================== -->
    <Teleport to="body">
      <Transition name="about-modal">
        <div
          v-if="ceoModalOpen"
          class="about-modal-backdrop"
          role="dialog"
          aria-modal="true"
          @click.self="ceoModalOpen = false"
        >
          <div class="ceo-full-modal">
            <button
              type="button"
              class="about-modal-close"
              aria-label="关闭全文"
              @click="ceoModalOpen = false"
            >
              ⊗
            </button>
            <div class="ceo-full-modal__scroll">
              <h2 class="ceo-full-modal__title">{{ ceoLetter.title }}</h2>
              <p
                v-for="(para, pIdx) in ceoLetter.fullParagraphs"
                :key="pIdx"
                class="ceo-full-modal__para"
              >
                {{ para }}
              </p>
              <div class="ceo-full-modal__sign">{{ ceoLetter.signature }}</div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- ==================== 弹窗 B：管理团队高管详情左右分栏弹窗（1:1 还原 about_48.png） ==================== -->
    <Teleport to="body">
      <Transition name="about-modal">
        <div
          v-if="activeTeamMember"
          class="about-modal-backdrop"
          role="dialog"
          aria-modal="true"
          @click.self="activeTeamMember = null"
        >
          <div class="team-bio-modal">
            <button
              type="button"
              class="about-modal-close"
              aria-label="关闭介绍"
              @click="activeTeamMember = null"
            >
              ⊗
            </button>

            <div class="team-bio-modal__left">
              <img
                v-if="activeTeamMember.avatar"
                :src="activeTeamMember.avatar"
                :alt="activeTeamMember.name"
              />
              <div v-else class="portrait-placeholder portrait-placeholder--lg">
                <div class="portrait-placeholder__head"></div>
                <div class="portrait-placeholder__suit"></div>
              </div>
            </div>

            <div class="team-bio-modal__right">
              <h3 class="team-bio-modal__name">{{ activeTeamMember.name }}</h3>
              <p class="team-bio-modal__role">{{ activeTeamMember.role }}</p>
              <div class="team-bio-modal__divider"></div>
              <div class="team-bio-modal__paras">
                <p v-for="(bPara, bIdx) in activeTeamMember.bio" :key="bIdx">{{ bPara }}</p>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.about-g7 {
  background: #ffffff;
  color: #1f2329;
}

/* 1. 顶部深空流光 Banner */
.about-hero {
  position: relative;
  height: 390px;
  background: radial-gradient(ellipse at 50% 85%, #2b1d52 0%, #100e20 55%, #07070c 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  overflow: hidden;
}

.about-hero__trails {
  position: absolute;
  inset: 0;
  background:
    repeating-linear-gradient(
      115deg,
      transparent 0px,
      transparent 38px,
      rgba(245, 158, 11, 0.08) 39px,
      transparent 41px
    ),
    repeating-linear-gradient(
      65deg,
      transparent 0px,
      transparent 44px,
      rgba(99, 102, 241, 0.12) 45px,
      transparent 48px
    );
}

.about-hero__inner {
  position: relative;
  z-index: 2;
  padding-bottom: 28px;
}

.about-hero__logo-mark {
  font-size: 54px;
  font-weight: 900;
  font-style: italic;
  color: #ffffff;
  line-height: 1;
  margin-bottom: 14px;
}

.about-hero__title {
  font-size: clamp(26px, 3.6vw, 40px);
  font-weight: 700;
  color: #ffffff;
  margin: 0;
}

/* 2. 悬浮白底五页签导航条 */
.about-tabs-wrap {
  position: relative;
  z-index: 10;
  margin-top: -41px;
}

.about-tabs {
  max-width: 1040px;
  height: 82px;
  margin: 0 auto;
  background: #ffffff;
  box-shadow: 0 12px 36px rgba(16, 24, 40, 0.06);
  display: flex;
  align-items: center;
  padding: 0 48px;
  gap: 56px;
}

.about-tabs__btn {
  position: relative;
  height: 100%;
  border: none;
  background: none;
  font-size: 15px;
  color: #2b2f38;
  font-family: inherit;
  cursor: pointer;
  padding: 0 4px;
  transition: color 0.2s ease;
}

.about-tabs__btn:hover,
.about-tabs__btn.is-active {
  color: #5b4be8;
  font-weight: 600;
}

.about-tabs__btn.is-active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 14px;
  height: 3px;
  background: #5b4be8;
  border-radius: 2px;
}

/* 3. 企业简介：左文右图 */
.about-intro-sec {
  padding: 88px 0 64px;
}

.about-intro-grid {
  max-width: 1040px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 64px;
  align-items: center;
}

.about-sec-title {
  font-size: 34px;
  font-weight: 700;
  color: #1f2329;
  margin: 0 0 28px;
}

.about-center-title {
  text-align: center;
  font-size: 36px;
  font-weight: 700;
  color: #1f2329;
  margin: 0 0 52px;
}

.about-intro__para {
  font-size: 14px;
  line-height: 2;
  color: #6e7380;
  margin: 0 0 16px;
}

.about-intro__photo {
  position: relative;
  aspect-ratio: 4 / 3;
  background: linear-gradient(140deg, #1b2432 0%, #0f1722 60%, #1e1b4b 100%);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.about-intro__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.about-intro__radar {
  width: 240px;
  height: 240px;
  border-radius: 50%;
  background: conic-gradient(
    from 210deg,
    rgba(124, 58, 237, 0.55) 0deg,
    rgba(56, 189, 248, 0.35) 75deg,
    transparent 120deg
  );
}

.scene-tag {
  position: absolute;
  bottom: 20px;
  right: 20px;
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(8px);
  color: #ffffff;
  font-size: 12.5px;
}

/* 4. CEO的一封信 */
.about-ceo-sec {
  padding: 40px 0 88px;
}

.ceo-card {
  max-width: 1040px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 38% 62%;
  min-height: 410px;
  box-shadow: 0 16px 44px rgba(16, 24, 40, 0.06);
}

.ceo-card__left {
  background:
    linear-gradient(180deg, rgba(15, 23, 42, 0.35) 0%, rgba(15, 23, 42, 0.85) 100%),
    radial-gradient(circle at 30% 40%, #334155 0%, #0f172a 100%);
  padding: 48px 36px;
  display: flex;
  align-items: flex-end;
  color: #ffffff;
}

.ceo-card__quote {
  font-size: 16px;
  font-weight: 600;
  line-height: 1.85;
  margin: 0;
}

.ceo-card__right {
  position: relative;
  background: linear-gradient(135deg, #f8fafc 0%, #eef2ff 100%);
  padding: 48px 52px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.ceo-card__watermark {
  position: absolute;
  right: 28px;
  bottom: -20px;
  font-size: 220px;
  font-weight: 900;
  font-style: italic;
  color: rgba(99, 102, 241, 0.06);
  pointer-events: none;
}

.ceo-card__title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2329;
  margin: 0 0 20px;
}

.ceo-card__paras p {
  font-size: 13.5px;
  line-height: 1.95;
  color: #5f6470;
  margin: 0 0 14px;
}

.ceo-card__foot {
  margin-top: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.about-outline-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 38px;
  padding: 0 28px;
  border: 1px solid #1f2329;
  background: transparent;
  color: #1f2329;
  font-size: 13px;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.about-outline-btn:hover {
  background: #1f2329;
  color: #ffffff;
}

.ceo-card__sign {
  font-size: 26px;
  font-family: 'STKaiti', 'KaiTi', cursive, serif;
  color: #1f2329;
  letter-spacing: 0.08em;
}

/* 5. 发展历程 */
.about-history-sec {
  padding: 80px 0 100px;
  background: #fcfcfd;
}

.history-axis {
  max-width: 920px;
  margin: 0 auto 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.history-arrow,
.slider-arrow {
  width: 36px;
  height: 36px;
  border: none;
  background: none;
  font-size: 34px;
  color: #8b90a0;
  cursor: pointer;
  line-height: 1;
}

.history-arrow:hover,
.slider-arrow:hover {
  color: #5b4be8;
}

.history-years {
  flex: 1;
  display: flex;
  align-items: baseline;
  justify-content: space-around;
  padding-bottom: 20px;
  border-bottom: 1px solid #e5e7eb;
}

.history-year-btn {
  border: none;
  background: none;
  font-size: 16px;
  font-weight: 700;
  color: #2b2f38;
  cursor: pointer;
  transition: all 0.25s ease;
}

.history-year-btn.is-center {
  font-size: 28px;
  color: #1f2329;
}

.history-stage {
  max-width: 1040px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 28px;
}

.history-card {
  padding: 40px 32px;
  border-radius: 4px;
  background: transparent;
  cursor: pointer;
  transition: all 0.3s ease;
}

.history-card.is-center {
  background: #ffffff;
  box-shadow: 0 20px 50px rgba(16, 24, 40, 0.08);
}

.history-card__year {
  font-size: 28px;
  font-weight: 700;
  color: #3a3f4a;
  margin-bottom: 20px;
}

.history-card__line {
  position: relative;
  height: 20px;
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}

.history-card__line::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  top: 50%;
  border-bottom: 1px dashed #d0d5dd;
}

.history-card__dot {
  position: relative;
  z-index: 2;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #98a2b3;
}

.history-card__target {
  position: relative;
  z-index: 2;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid #5b4be8;
  background: #ffffff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.history-card__target::after {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #5b4be8;
}

.history-card__bullets {
  list-style: disc;
  padding-left: 18px;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  font-size: 13.5px;
  line-height: 1.8;
  color: #5f6470;
}

/* 5.5 荣誉资质认证墙（开发文档 6.11） */
.about-quals-sec {
  padding: 72px 0 88px;
  background: #ffffff;
}

.quals-grid {
  max-width: 1040px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.qual-card {
  padding: 24px 20px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  text-align: center;
  background: #fafbfe;
}

.qual-card__img {
  max-height: 110px;
  max-width: 100%;
  object-fit: contain;
  margin-bottom: 12px;
}

.qual-card__name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2329;
  margin: 0 0 6px;
}

.qual-card__desc {
  font-size: 12.5px;
  color: #6e7380;
  margin: 0;
}

/* 6. 联系我们 7 城内部滚动容器 + 右侧地图 */
.about-offices-sec {
  padding: 84px 0 110px;
  background: #f2f4f8;
}

.offices-layout {
  max-width: 1040px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 440px 1fr;
  gap: 56px;
  align-items: center;
}

.offices-scroll-box {
  max-height: 430px;
  overflow-y: auto;
  padding-right: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.offices-scroll-box::-webkit-scrollbar {
  width: 3px;
}

.offices-scroll-box::-webkit-scrollbar-track {
  background: #e2e6ee;
}

.offices-scroll-box::-webkit-scrollbar-thumb {
  background: #4b5563;
}

.office-item {
  padding: 18px 24px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.22s ease;
}

.office-item.is-active {
  background: #ffffff;
  box-shadow: 0 10px 28px rgba(16, 24, 40, 0.05);
}

.office-item__city {
  font-size: 17px;
  font-weight: 700;
  color: #2b2f38;
  margin: 0 0 8px;
}

.office-item__line {
  font-size: 13px;
  line-height: 1.75;
  color: #5f6470;
  margin: 2px 0;
}

.office-item__line span {
  color: #98a2b3;
}

.offices-map__canvas {
  position: relative;
  height: 410px;
  background-image: radial-gradient(#cbd5e1 1.25px, transparent 1.25px);
  background-size: 12px 12px;
  border-radius: 12px;
}

.map-photo-popup {
  position: absolute;
  transform: translateX(-50%);
  width: 240px;
  height: 136px;
  border: 2px solid #2b2f38;
  border-radius: 4px;
  background: #ffffff;
  box-shadow: 0 16px 36px rgba(16, 24, 40, 0.14);
  z-index: 5;
  transition: left 0.3s ease, top 0.3s ease;
}

.map-photo-popup__img {
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.map-photo-popup__img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.building-illustration {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: flex-end;
  padding: 12px;
  background: linear-gradient(160deg, #38bdf8 0%, #2563eb 52%, #1e293b 100%);
}

.b-theme--1 {
  background: linear-gradient(160deg, #6366f1 0%, #4338ca 52%, #0f172a 100%);
}

.b-theme--2 {
  background: linear-gradient(160deg, #0d9488 0%, #0f766e 52%, #0f172a 100%);
}

.b-theme--3 {
  background: linear-gradient(160deg, #0284c7 0%, #1e40af 52%, #0f172a 100%);
}

.building-illustration__tag {
  padding: 3px 10px;
  border-radius: 3px;
  background: rgba(15, 23, 42, 0.72);
  color: #ffffff;
  font-size: 11.5px;
}

.map-photo-popup__arrow {
  position: absolute;
  bottom: -8px;
  left: 50%;
  width: 12px;
  height: 12px;
  background: #2b2f38;
  transform: translateX(-50%) rotate(45deg);
}

.map-pin {
  position: absolute;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border: none;
  background: none;
  font-size: 12px;
  color: #4b5563;
  cursor: pointer;
  padding: 2px 4px;
}

.map-pin__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  border: 2px solid #98a2b3;
  background: #ffffff;
}

.map-pin.is-active {
  font-weight: 700;
  color: #101014;
}

.map-pin.is-active .map-pin__dot {
  width: 11px;
  height: 11px;
  border-color: #101014;
}

/* 7. 管理团队 4 列高管网格 */
.about-team-sec {
  padding: 84px 0 110px;
}

.team-grid {
  max-width: 1040px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 24px;
}

.team-card {
  background: #f5f7fa;
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.team-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 18px 40px rgba(16, 24, 40, 0.1);
}

.team-card__portrait {
  aspect-ratio: 4 / 4.6;
  background: #2b2f36;
  overflow: hidden;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.team-card__portrait img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.portrait-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
}

.portrait-placeholder__head {
  width: 68px;
  height: 82px;
  border-radius: 50%;
  background: linear-gradient(180deg, #e2e8f0 0%, #94a3b8 100%);
  margin-bottom: -10px;
  z-index: 2;
}

.portrait-placeholder__suit {
  width: 168px;
  height: 120px;
  border-radius: 72px 72px 0 0;
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
}

.team-card__info {
  padding: 24px 16px 28px;
  text-align: center;
}

.team-card__name {
  font-size: 20px;
  font-weight: 700;
  color: #1f2329;
  margin: 0 0 8px;
}

.team-card__role {
  font-size: 13px;
  color: #8b90a0;
  margin: 0;
}

/* 8. 媒体中心 & 资料共享 */
.about-news-sec,
.about-res-sec {
  padding: 84px 0 110px;
  background: #f5f7fb;
}

.about-news__head {
  max-width: 1040px;
  margin: 0 auto 36px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.about-news__head .about-sec-title {
  margin-bottom: 0;
}

.about-news-slider {
  max-width: 1140px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 16px;
}

.about-news-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.about-news-card,
.res-card {
  background: #ffffff;
  display: flex;
  flex-direction: column;
  text-decoration: none;
  color: inherit;
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.about-news-card:hover,
.res-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 16px 36px rgba(16, 24, 40, 0.08);
}

.about-news-card__poster,
.res-card__cover {
  aspect-ratio: 1 / 1;
  overflow: hidden;
  position: relative;
}

.poster-fallback,
.res-card__brochure {
  width: 100%;
  height: 100%;
  padding: 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: #ffffff;
}

.poster-theme--0 .poster-fallback {
  background: linear-gradient(145deg, #2563eb 0%, #1e3a8a 60%, #0f172a 100%);
}

.poster-theme--1 .poster-fallback {
  background: linear-gradient(145deg, #4f46e5 0%, #311042 65%, #09090f 100%);
}

.poster-theme--2 .poster-fallback {
  background: linear-gradient(145deg, #0284c7 0%, #1e40af 65%, #0f172a 100%);
}

.poster-theme--3 .poster-fallback {
  background: linear-gradient(145deg, #78350f 0%, #271709 65%, #09090b 100%);
}

.about-news-card__body {
  padding: 20px 18px 22px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 126px;
}

.about-news-card__title {
  font-size: 14.5px;
  font-weight: 500;
  color: #2b2f38;
  line-height: 1.6;
  margin: 0;
}

.about-news-card__time {
  margin-top: 14px;
  font-size: 12px;
  color: #98a2b3;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 资料库子标签与卡片 */
.res-subtabs {
  display: flex;
  justify-content: center;
  gap: 48px;
  margin-bottom: 44px;
}

.res-subtabs__btn {
  position: relative;
  border: none;
  background: none;
  padding: 8px 4px 14px;
  font-size: 15px;
  color: #5f6470;
  font-family: inherit;
  cursor: pointer;
}

.res-subtabs__btn.is-active {
  color: #5b4be8;
  font-weight: 600;
}

.res-subtabs__btn.is-active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 3px;
  background: #5b4be8;
}

.res-card__logo-box {
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, #eef2f6 0%, #ffffff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  text-align: center;
  font-size: 22px;
}

.res-card__brochure-brand {
  font-size: 12px;
  font-weight: 700;
  opacity: 0.85;
}

.res-card__foot {
  padding: 18px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.res-card__title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2329;
  margin: 0;
}

.res-card__dl-btn {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 1px solid #2b2f38;
  background: #ffffff;
  color: #2b2f38;
  font-size: 13px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.res-card:hover .res-card__dl-btn {
  background: #5b4be8;
  border-color: #5b4be8;
  color: #ffffff;
}

/* 9. 加入我们 + 使命/愿景/价值观 */
.about-join-sec {
  padding: 72px 0 36px;
}

.join-banner {
  max-width: 1040px;
  margin: 0 auto;
  height: 320px;
  background:
    linear-gradient(90deg, rgba(16, 18, 28, 0.72) 0%, rgba(16, 18, 28, 0.25) 65%),
    radial-gradient(circle at 80% 30%, #4b5563 0%, #1f2937 100%);
  display: flex;
  align-items: center;
  padding: 0 72px;
}

.join-banner__title {
  font-size: 36px;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 24px;
}

.join-banner__btn {
  padding: 10px 32px;
  border: 1px solid rgba(255, 255, 255, 0.85);
  background: transparent;
  color: #ffffff;
  font-size: 13.5px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.join-banner__btn:hover {
  background: #ffffff;
  color: #101014;
}

.about-culture-sec {
  padding: 24px 0 96px;
}

.culture-row {
  max-width: 1040px;
  margin: 0 auto;
  padding: 56px 0;
  border-bottom: 1px solid #eceef2;
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 32px;
  align-items: baseline;
}

.culture-row--values {
  border-bottom: none;
}

.culture-row__label {
  font-size: 20px;
  color: #5f6470;
}

.culture-row__headline {
  font-size: clamp(24px, 2.8vw, 32px);
  font-weight: 700;
  color: #2b2f38;
  line-height: 1.4;
  margin: 0;
}

.culture-values-list {
  display: flex;
  flex-direction: column;
  gap: 48px;
}

.value-block__title {
  font-size: 26px;
  font-weight: 700;
  color: #2b2f38;
  margin: 0 0 14px;
}

.value-block__line {
  font-size: 14px;
  line-height: 2;
  color: #6e7380;
  margin: 2px 0;
}

/* ==================== 弹窗样式（CEO信全文 & 高管详情） ==================== */
.about-modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 95;
  background: rgba(0, 0, 0, 0.56);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.about-modal-close {
  position: absolute;
  top: 20px;
  right: 22px;
  border: none;
  background: none;
  font-size: 24px;
  color: #5f6470;
  cursor: pointer;
  line-height: 1;
}

.about-modal-close:hover {
  color: #101014;
}

.ceo-full-modal {
  position: relative;
  width: 100%;
  max-width: 920px;
  max-height: 82vh;
  background: #ffffff;
  padding: 52px 56px 48px;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.25);
  display: flex;
  flex-direction: column;
}

.ceo-full-modal__scroll {
  overflow-y: auto;
  padding-right: 24px;
}

.ceo-full-modal__title {
  font-size: 30px;
  font-weight: 700;
  color: #1f2329;
  margin: 0 0 28px;
}

.ceo-full-modal__para {
  font-size: 14.5px;
  line-height: 2;
  color: #4b5563;
  margin: 0 0 18px;
}

.ceo-full-modal__sign {
  margin-top: 28px;
  text-align: right;
  font-size: 30px;
  font-family: 'STKaiti', 'KaiTi', cursive, serif;
}

.team-bio-modal {
  position: relative;
  width: 100%;
  max-width: 840px;
  min-height: 410px;
  background: #ffffff;
  display: grid;
  grid-template-columns: 38% 62%;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.28);
  overflow: hidden;
}

.team-bio-modal__left {
  background: #2b2f36;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.team-bio-modal__left img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.team-bio-modal__right {
  padding: 48px 44px;
}

.team-bio-modal__name {
  font-size: 28px;
  font-weight: 700;
  color: #1f2329;
  margin: 0 0 8px;
}

.team-bio-modal__role {
  font-size: 15px;
  color: #5f6470;
  margin: 0;
}

.team-bio-modal__divider {
  margin: 22px 0;
  border-bottom: 1px solid #eceef2;
}

.team-bio-modal__paras p {
  font-size: 13.5px;
  line-height: 1.95;
  color: #5f6470;
  margin: 0 0 12px;
}

@media (max-width: 920px) {
  .about-tabs {
    padding: 0 20px;
    gap: 20px;
    overflow-x: auto;
  }
  .about-intro-grid,
  .ceo-card,
  .culture-row,
  .history-stage,
  .offices-layout,
  .team-bio-modal {
    grid-template-columns: 1fr;
  }
  .team-grid,
  .about-news-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
