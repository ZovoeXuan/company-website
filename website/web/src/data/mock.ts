/**
 * 示例内容。正式内容由后台「内容管理」维护，这里仅用于前端开发与演示，
 * 上线前必须由真实素材替换。所有名称、数据均为占位，不代表真实业绩。
 */
import type {
  AboutPage,
  CaseDetail,
  HomeView,
  MediaRef,
  NewsDetail,
  PrivacyPage,
  ProductDetail,
  SiteConfig,
  SolutionDetail,
} from './types'

const wide = (alt: string): MediaRef => ({
  id: 'mock-wide',
  url: '/media/placeholder-16x9.svg',
  alt,
  width: 800,
  height: 450,
})

const square = (alt: string): MediaRef => ({
  id: 'mock-square',
  url: '/media/placeholder-4x3.svg',
  alt,
  width: 600,
  height: 450,
})

export const site: SiteConfig = {
  siteName: '示例科技',
  slogan: '让设备运维从被动响应变成提前预判',
  nav: [
    { label: '首页', path: '/' },
    { label: '产品', path: '/products' },
    { label: '解决方案', path: '/solutions' },
    { label: '客户案例', path: '/cases' },
    { label: '新闻', path: '/news' },
    { label: '关于我们', path: '/about' },
    { label: '联系我们', path: '/contact' },
  ],
  contact: {
    phone: '010-0000 0000',
    email: 'contact@example.com',
    address: '北京市示例区示例路 1 号',
    hours: '工作日 9:00–18:00',
  },
  footer: {
    copyright: '© 2026 示例科技有限公司',
    icp: '京ICP备00000000号',
    links: [
      { label: '隐私说明', path: '/privacy' },
      { label: '联系我们', path: '/contact' },
      { label: '关于我们', path: '/about' },
    ],
  },
}

export const home: HomeView = {
  hero: {
    eyebrow: '工业设备数字化',
    title: '让设备运维从被动响应\n变成提前预判',
    subtitle: '从设备接入、状态监测到运维决策，一套系统覆盖全流程，帮助制造企业降低非计划停机。',
    primary: { label: '预约演示', path: '/contact' },
    secondary: { label: '查看产品', path: '/products' },
    media: wide('设备管理平台界面示意'),
  },
  metrics: [
    { value: '320+', label: '服务企业' },
    { value: '68', label: '覆盖城市' },
    { value: '12,000+', label: '接入设备' },
    { value: '4h', label: '平均响应' },
  ],
  services: [
    { key: 'svc-1', title: '设备管理平台', summary: '统一接入、台账与远程参数下发', contentId: 'p-1' },
    { key: 'svc-2', title: '预测性维护', summary: '基于运行数据提前发现异常', contentId: 'p-2' },
    { key: 'svc-3', title: '能耗监测', summary: '分项计量与用能优化建议', contentId: 'p-3' },
  ],
  featuredCases: [],
  customers: [
    { id: 'c-1', name: '汽车零部件' },
    { id: 'c-2', name: '城市水务' },
    { id: 'c-3', name: '电子制造' },
    { id: 'c-4', name: '化工园区' },
    { id: 'c-5', name: '装备制造' },
    { id: 'c-6', name: '食品加工' },
  ],
  latestNews: [],
}

export const productCategories = [
  { id: 'pc-1', name: '设备管理' },
  { id: 'pc-2', name: '能源管理' },
  { id: 'pc-3', name: '数据应用' },
  { id: 'pc-4', name: '硬件与网关' },
]

export const products: ProductDetail[] = [
  {
    id: 'p-1',
    title: '设备管理平台',
    summary: '面向多产线场景的统一设备接入与台账管理，支持远程参数下发与告警联动。',
    categoryId: 'pc-1',
    categoryName: '设备管理',
    cover: square('设备管理平台界面截图'),
    bodyHtml:
      '<h2>解决什么问题</h2><p>产线设备来自不同厂商、协议各异，台账散落在表格里，故障只能靠电话上报。平台把设备身份、运行状态、维修记录收敛到一处。</p><h2>主要能力</h2><ul><li>支持 Modbus、OPC UA、MQTT 等常见协议接入</li><li>设备台账与二维码巡检，扫码即可查看历史工单</li><li>远程参数下发与固件升级，操作留痕可追溯</li></ul><h2>适用场景</h2><p>多工厂、多产线的制造企业，设备数量在 100 台以上、跨协议混布的环境。</p>',
    capabilities: ['多协议接入', '台账与巡检', '远程下发', '告警联动'],
    relatedCases: [],
  },
  {
    id: 'p-2',
    title: '预测性维护',
    summary: '基于振动、温度与电流数据建立设备健康模型，在故障发生前给出检修建议。',
    categoryId: 'pc-1',
    categoryName: '设备管理',
    cover: square('预测性维护看板截图'),
    bodyHtml:
      '<h2>解决什么问题</h2><p>定期检修要么过度维护、要么漏检。关键设备一旦非计划停机，损失按小时计算。</p><h2>主要能力</h2><ul><li>关键部件健康度评分与趋势预测</li><li>异常自动生成检修建议与备件清单</li><li>与工单系统联动，检修结果回流模型</li></ul>',
    capabilities: ['健康度评分', '趋势预测', '检修建议'],
    relatedCases: [],
  },
  {
    id: 'p-3',
    title: '能耗监测系统',
    summary: '分项计量电、水、气、汽，定位高耗能环节并给出优化建议。',
    categoryId: 'pc-2',
    categoryName: '能源管理',
    cover: square('能耗监测界面截图'),
    bodyHtml:
      '<h2>解决什么问题</h2><p>能耗账单只有一个总数，无法定位到产线、班组或单台设备，节能改造缺少依据。</p><h2>主要能力</h2><ul><li>分项、分时计量与同比环比分析</li><li>单位产品能耗核算</li><li>异常用能推送与工单派发</li></ul>',
    capabilities: ['分项计量', '能耗核算', '异常推送'],
    relatedCases: [],
  },
  {
    id: 'p-4',
    title: '生产可视化看板',
    summary: '把产量、节拍、良率与设备状态汇总到大屏，车间与管理层看到同一份数据。',
    categoryId: 'pc-3',
    categoryName: '数据应用',
    cover: square('生产可视化大屏示意'),
    bodyHtml:
      '<h2>解决什么问题</h2><p>班前会靠口头汇报，数据滞后一天，异常发现时已经过去一个班次。</p><h2>主要能力</h2><ul><li>产线实时状态与节拍分析</li><li>良率、OEE 与停机原因帕累托</li><li>支持大屏、桌面与移动端自适应</li></ul>',
    capabilities: ['实时看板', 'OEE 分析', '多端适配'],
    relatedCases: [],
  },
  {
    id: 'p-5',
    title: '远程运维服务',
    summary: '由服务团队接管设备日常巡检与告警处置，企业按年订阅。',
    categoryId: 'pc-1',
    categoryName: '设备管理',
    cover: square('远程运维工作台截图'),
    bodyHtml:
      '<h2>服务内容</h2><p>平台接入后，由运维团队负责 7×24 小时告警值守、远程诊断与月度健康报告，企业无需自建监控班组。</p><ul><li>告警分级与值班响应</li><li>远程诊断与处置建议</li><li>月度设备健康报告</li></ul>',
    capabilities: ['7×24 值守', '远程诊断', '健康报告'],
    relatedCases: [],
  },
  {
    id: 'p-6',
    title: '边缘数据网关',
    summary: '工业现场数据采集与协议转换，断网本地缓存，恢复后自动补传。',
    categoryId: 'pc-4',
    categoryName: '硬件与网关',
    cover: square('边缘网关设备示意'),
    bodyHtml:
      '<h2>产品说明</h2><p>支持多路串口与网口，内置常见工业协议驱动，数据先落地再上传，网络波动不影响完整性。</p><ul><li>宽温设计，导轨安装</li><li>本地缓存 7 天，断点续传</li><li>支持远程配置与固件升级</li></ul>',
    capabilities: ['多协议转换', '断点续传', '远程配置'],
    relatedCases: [],
  },
]

export const solutions: SolutionDetail[] = [
  {
    id: 's-1',
    title: '汽车零部件产线数字化',
    summary: '面向多品种小批量产线，打通设备状态、质量数据与工单，降低非计划停机。',
    industry: '汽车零部件',
    cover: wide('汽车零部件产线示意'),
    challenge:
      '产线设备品牌混杂，换型频繁，停机原因靠人工记录，质量追溯需要翻查纸质单据。',
    approach: [
      '统一采集层：网关接入冲压、焊装、装配三类设备',
      '状态与工艺参数关联，换型参数一键下发',
      '质量数据与工单绑定，实现单件追溯',
    ],
    outcome: [
      { label: '非计划停机', value: '-37%' },
      { label: '换型时间', value: '-25%' },
      { label: '追溯耗时', value: '从 2 小时到 5 分钟' },
    ],
    relatedProducts: [],
  },
  {
    id: 's-2',
    title: '城市水务管网监测',
    summary: '对管网压力、流量与水质做连续监测，定位漏损区段并辅助调度。',
    industry: '城市水务',
    cover: wide('水务管网监测示意'),
    challenge: '管网埋地、点位分散，漏损依赖人工排查，夜间最小流量异常难以归因。',
    approach: [
      '分区计量（DMA）划分与远传表计接入',
      '夜间最小流量分析定位异常区段',
      '压力调度与爆管告警联动',
    ],
    outcome: [
      { label: '漏损率', value: '降至 8.2%' },
      { label: '查漏效率', value: '+60%' },
      { label: '告警响应', value: '15 分钟内' },
    ],
    relatedProducts: [],
  },
  {
    id: 's-3',
    title: '园区综合能源管理',
    summary: '电水气汽分项计量，结合产线负荷做需量控制与峰谷优化。',
    industry: '园区与公共设施',
    cover: wide('园区能源管理示意'),
    challenge: '园区内多家企业共用配电，费用分摊缺少计量依据，需量超限产生罚款。',
    approach: [
      '分户分项计量改造',
      '需量预测与负荷削峰策略',
      '费用分摊账单自动生成',
    ],
    outcome: [
      { label: '综合能耗', value: '-9%' },
      { label: '需量罚款', value: '清零' },
      { label: '账单核对', value: '自动化' },
    ],
    relatedProducts: [],
  },
]

export const cases: CaseDetail[] = [
  {
    id: 'c-1',
    title: '某汽车零部件集团：非计划停机下降 37%',
    industry: '汽车零部件',
    summary: '三条冲压线与两条焊装线接入平台后，停机原因首次实现可量化归因。',
    results: [
      { label: '非计划停机', value: '-37%' },
      { label: '平均修复时间', value: '-28%' },
    ],
    cover: square('汽车零部件车间'),
    client: '某汽车零部件集团（应客户要求隐去名称）',
    background:
      '集团下属三个工厂，设备来自五个厂商，停机记录依赖班组手工填写，月度分析只能看到总时长，无法定位到具体设备与原因。',
    actions: [
      '为 128 台关键设备加装采集网关，统一状态与工艺参数口径',
      '按设备类型建立停机原因字典，告警自动关联工单',
      '每周输出设备可用率与 top10 停机原因，纳入班组考核',
    ],
    quote: { text: '现在讨论停机，大家看的是同一份数据，不再各说各话。', author: '设备部负责人' },
    relatedProducts: [],
  },
  {
    id: 'c-2',
    title: '某城市水务集团：漏损率降至 8.2%',
    industry: '城市水务',
    summary: '通过分区计量与夜间最小流量分析，一年内定位 43 处隐蔽漏点。',
    results: [
      { label: '漏损率', value: '8.2%' },
      { label: '定位漏点', value: '43 处' },
    ],
    cover: square('水务调度中心'),
    client: '某城市水务集团（应客户要求隐去名称）',
    background: '管网总长超过 1800 公里，漏损率长期在 13% 左右，查漏主要靠市民报修与人工夜巡。',
    actions: [
      '划分 62 个 DMA 分区，加装远传流量与压力计',
      '建立夜间最小流量基线，异常分区自动预警',
      '查漏队伍按预警优先级派单，结果回填闭环',
    ],
    quote: null,
    relatedProducts: [],
  },
  {
    id: 'c-3',
    title: '某电子制造企业：OEE 提升 12 个百分点',
    industry: '电子制造',
    summary: '把设备状态与工单节拍对齐后，瓶颈工序第一次被准确识别。',
    results: [
      { label: 'OEE', value: '+12pt' },
      { label: '换线时间', value: '-18%' },
    ],
    cover: square('电子制造产线'),
    client: '某电子制造企业（应客户要求隐去名称）',
    background: 'SMT 产线节拍数据与工单系统各自独立，OEE 靠人工估算，改善方向不明确。',
    actions: [
      '采集贴片机、回流焊与 AOI 的运行与停机信号',
      '工单与设备状态按时间轴对齐，计算真实 OEE',
      '识别瓶颈工序后调整排产规则',
    ],
    quote: null,
    relatedProducts: [],
  },
  {
    id: 'c-4',
    title: '某化工园区：综合能耗下降 9%',
    industry: '化工园区',
    summary: '分项计量落地后，蒸汽与循环水系统的浪费点被逐一暴露。',
    results: [
      { label: '综合能耗', value: '-9%' },
      { label: '需量罚款', value: '清零' },
    ],
    cover: square('化工园区'),
    client: '某化工园区管委会（应客户要求隐去名称）',
    background: '园区内 12 家企业共用配电与蒸汽管网，费用按面积分摊，节能缺乏动力。',
    actions: [
      '分户分项计量改造，数据按企业归集',
      '需量预测与错峰生产建议',
      '月度能耗账单自动生成并公示',
    ],
    quote: null,
    relatedProducts: [],
  },
]

export const news: NewsDetail[] = [
  {
    id: 'n-1',
    title: '设备管理平台发布 3.2 版本：支持批量远程下发',
    summary: '新版本增加批量参数下发与回滚能力，单次可覆盖 200 台设备。',
    displayTime: '2026-10-18',
    categoryName: '产品动态',
    cover: wide('版本发布示意'),
    bodyHtml:
      '<p>3.2 版本重点解决批量运维场景。此前参数下发只能逐台操作，产线换型时需要两人配合半小时以上。</p><h2>主要变化</h2><ul><li>支持按设备分组批量下发，单次上限 200 台</li><li>下发失败自动回滚到上一版本参数</li><li>操作记录可在审计日志中查询</li></ul><p>升级不影响存量设备，已在测试环境完成 500 台规模验证。</p>',
    prev: null,
    next: { id: 'n-2', title: '工业园区能耗管理实践分享会将于 11 月举行' },
  },
  {
    id: 'n-2',
    title: '工业园区能耗管理实践分享会将于 11 月举行',
    summary: '围绕分项计量、需量控制与费用分摊三个话题，邀请园区运营方分享经验。',
    displayTime: '2026-09-26',
    categoryName: '公司动态',
    cover: wide('活动海报示意'),
    bodyHtml:
      '<p>本次分享会面向园区运营与能源管理负责人，内容以实际项目为主，不设产品推销环节。</p><h2>议题</h2><ul><li>分项计量改造的常见坑与预算范围</li><li>需量控制策略如何与生产排程配合</li><li>多租户场景下的费用分摊规则设计</li></ul>',
    prev: { id: 'n-1', title: '设备管理平台发布 3.2 版本：支持批量远程下发' },
    next: { id: 'n-3', title: '如何评估一条产线是否需要做预测性维护' },
  },
  {
    id: 'n-3',
    title: '如何评估一条产线是否需要做预测性维护',
    summary: '从设备价值、故障后果与数据基础三个维度给出判断标准。',
    displayTime: '2026-08-30',
    categoryName: '技术文章',
    cover: wide('技术文章配图'),
    bodyHtml:
      '<p>预测性维护不是所有设备都值得做。投入产出比取决于三个条件。</p><h2>判断标准</h2><ul><li>设备价值与停机损失：单次停机损失低于监测成本时不适合</li><li>故障是否有前兆：突发性故障难以预测</li><li>数据基础：是否有可用的振动、温度或电流信号</li></ul><p>满足两条以上再考虑试点，先用三个月数据验证模型有效性。</p>',
    prev: { id: 'n-2', title: '工业园区能耗管理实践分享会将于 11 月举行' },
    next: { id: 'n-4', title: '边缘网关断网续传的实测数据' },
  },
  {
    id: 'n-4',
    title: '边缘网关断网续传的实测数据',
    summary: '在弱网环境下连续测试 72 小时，数据完整率 99.97%。',
    displayTime: '2026-07-22',
    categoryName: '技术文章',
    cover: wide('测试环境示意'),
    bodyHtml:
      '<p>测试模拟工厂常见的网络抖动：每小时随机断网 2 至 15 分钟，网关本地缓存后自动补传。</p><h2>结果</h2><ul><li>72 小时共采集 1,296,000 条记录</li><li>完整上传 1,295,610 条，完整率 99.97%</li><li>丢失集中在断电场景，非断网场景</li></ul>',
    prev: { id: 'n-3', title: '如何评估一条产线是否需要做预测性维护' },
    next: { id: 'n-5', title: '公司通过 ISO 27001 信息安全管理体系认证' },
  },
  {
    id: 'n-5',
    title: '公司通过 ISO 27001 信息安全管理体系认证',
    summary: '认证范围覆盖平台研发、运维与客户数据管理。',
    displayTime: '2026-06-15',
    categoryName: '公司动态',
    cover: wide('认证证书示意'),
    bodyHtml:
      '<p>公司于 2026 年 6 月通过 ISO/IEC 27001 认证，认证范围包括平台研发、云上运维与客户数据管理流程。</p><p>相关信息可在认证机构公开查询系统中核验。</p>',
    prev: { id: 'n-4', title: '边缘网关断网续传的实测数据' },
    next: { id: 'n-6', title: '设备台账整理的五个常见问题' },
  },
  {
    id: 'n-6',
    title: '设备台账整理的五个常见问题',
    summary: '从编码规则到责任归属，梳理台账项目启动阶段最容易返工的环节。',
    displayTime: '2026-05-09',
    categoryName: '技术文章',
    cover: wide('台账整理示意'),
    bodyHtml:
      '<p>台账是设备数字化的地基，返工成本极高。以下是项目中最常见的五个问题。</p><ul><li>编码规则没有预留扩展位</li><li>同一设备在不同工厂有多个名称</li><li>位置信息只写到车间，无法定位到工位</li><li>责任人与使用人混为一谈</li><li>历史维修记录没有迁移方案</li></ul>',
    prev: { id: 'n-5', title: '公司通过 ISO 27001 信息安全管理体系认证' },
    next: null,
  },
]

export const about: AboutPage = {
  intro:
    '示例科技成立于 2014 年，为制造企业与公共设施提供设备数字化与能源管理服务。我们不做大而全的平台，只解决设备状态看不见、停机原因说不清、能耗账单拆不开这三件事。',
  mission:
    '我们相信，工业现场的大部分浪费来自信息不对称：设备在做什么、为什么停、能耗花在哪里，往往没有人能同时回答。把这三件事变成可查的数据，改善才有依据。',
  milestones: [
    { year: '2014', title: '公司成立', description: '从设备巡检工具起步，服务第一批本地制造企业。' },
    { year: '2017', title: '平台化', description: '统一接入层上线，支持多协议设备接入。' },
    { year: '2020', title: '进入能源管理', description: '推出分项计量与能耗分析模块。' },
    { year: '2023', title: '预测性维护', description: '与高校联合研发设备健康度模型并落地。' },
    { year: '2026', title: '服务规模', description: '累计服务 320 余家企业，接入设备超过 12,000 台。' },
  ],
  qualifications: [
    { name: '高新技术企业', description: '2021 年认定，证书在有效期内。' },
    { name: 'ISO 27001', description: '信息安全管理体系认证，覆盖研发与运维。' },
    { name: '软件著作权', description: '设备管理、能耗分析等 18 项登记。' },
  ],
}

export const privacy: PrivacyPage = {
  updatedAt: '2026-10-01',
  bodyHtml:
    '<p>本页说明我们在您使用本网站与咨询服务时收集的信息范围、用途与保存期限。正式发布前，本内容需由公司法务确认。</p><h2>我们收集什么</h2><ul><li>您主动提交的姓名、公司、联系电话与需求描述</li><li>访问日志中的页面路径与时间，用于排查故障</li></ul><h2>用途</h2><p>仅用于回复您的咨询与改进网站体验，不用于广告投放，不向第三方出售。</p><h2>保存期限</h2><p>咨询记录保存 24 个月，到期后由管理员按流程清理。</p><h2>您的权利</h2><p>您可以要求查询、更正或删除已提交的信息，请联系 contact@example.com。</p>',
}

// 关联关系在数组定义后回填，避免循环引用
home.featuredCases = [cases[0], cases[2]]
home.latestNews = news.slice(0, 3)

products[0].relatedCases = [cases[0], cases[2]]
products[1].relatedCases = [cases[0]]
solutions[0].relatedProducts = [products[0], products[1], products[5]]
solutions[1].relatedProducts = [products[3], products[5]]
solutions[2].relatedProducts = [products[2], products[3]]
cases[0].relatedProducts = [products[0], products[1]]
cases[1].relatedProducts = [products[2]]
cases[2].relatedProducts = [products[3]]
cases[3].relatedProducts = [products[2], products[3]]
