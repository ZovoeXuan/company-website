<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ConfigPanel from '@/components/ConfigPanel.vue'
import ContentPicker from '@/components/ContentPicker.vue'
import LinkEditor, { type LinkValue } from '@/components/LinkEditor.vue'
import MediaPicker from '@/components/MediaPicker.vue'
import { useConfig } from '@/composables/useConfig'
import type { MediaItem } from '@/api/content'

interface HeroItem {
  key?: string
  enabled: boolean
  sortOrder: number
  title: string
  subtitle: string
  desktopMediaId: number | null
  mobileMediaId: number | null
  imageAlt: string
  buttonText: string
  link: LinkValue | null
}

interface MetricItem {
  key?: string
  sortOrder: number
  displayValue: string
  unit: string
  label: string
  description: string
}

interface ServiceGroup {
  key?: string
  sortOrder: number
  title: string
  description: string
  imageMediaId: number | null
  contentType: 'PRODUCT' | 'SOLUTION'
  contentIds: number[]
  moreLink: LinkValue | null
}

interface HomeDraft {
  sectionOrder?: string[]
  hero: { items: HeroItem[] }
  metrics: { enabled: boolean; title: string; subtitle: string; items: MetricItem[] }
  services: { enabled: boolean; title: string; subtitle: string; groups: ServiceGroup[] }
  cases: { enabled: boolean; title: string; subtitle: string; contentIds: number[] }
  customers: { enabled: boolean; title: string; subtitle: string; contentIds: number[] }
  news: {
    enabled: boolean
    title: string
    subtitle: string
    mode: 'SELECTED' | 'LATEST'
    limit: number
    contentIds: number[]
  }
  consultation: {
    title: string
    subtitle: string
    buttonText: string
    link: LinkValue | null
  }
}

const SECTION_LABELS: Record<string, { name: string; desc: string }> = {
  metrics: { name: '企业关键指标 (metrics)', desc: '四列大数字规模与实力背书' },
  services: { name: '产品与解决方案分组 (services)', desc: '左右交错核心产品与方案推荐卡' },
  cases: { name: '标杆客户案例 (cases)', desc: '行业头部客户实践与成果数据' },
  customers: { name: '合作客户标识墙 (customers)', desc: '合作伙伴与客户 Logo 墙/跑马灯' },
  news: { name: '最新动态与洞察 (news)', desc: '手选或自动按最新日期展示企业动态' },
  consultation: { name: '底部咨询与 ROI 行动区 (consultation)', desc: '全站转化收口与专家预约引导' },
}

const DEFAULT_ORDER = ['metrics', 'services', 'cases', 'customers', 'news', 'consultation']

const router = useRouter()
const activeTab = ref('order')
const pickerOpen = ref(false)
const pickerKind = ref<'IMAGE' | 'VIDEO' | 'ALL'>('ALL')
let pickerCallback: ((media: MediaItem) => void) | null = null

const { view, loading, saving, publishing, loadError, load, save, publish } =
  useConfig<HomeDraft & Record<string, unknown>>('HOME')

const form = reactive<HomeDraft>({
  sectionOrder: [...DEFAULT_ORDER],
  hero: { items: [] },
  metrics: { enabled: true, title: '数字生产力规模背书', subtitle: '', items: [] },
  services: { enabled: true, title: '核心产品与行业方案', subtitle: '', groups: [] },
  cases: { enabled: true, title: '行业领军企业的共同选择', subtitle: '', contentIds: [] },
  customers: { enabled: true, title: '生态伙伴与标杆客户', subtitle: '', contentIds: [] },
  news: { enabled: true, title: '最新动态与前沿洞察', subtitle: '', mode: 'LATEST', limit: 3, contentIds: [] },
  consultation: {
    title: '准备好开启企业数字化生产力跃迁了吗？',
    subtitle: '留下您的业务痛点，我们的行业架构师将在 1 个工作日内为您提供定制方案与演示。',
    buttonText: '立即预约咨询',
    link: { type: 'CONSULT' },
  },
})

function fill() {
  const draft = view.value?.draft
  if (!draft) return

  const rawOrder = Array.isArray(draft.sectionOrder) ? draft.sectionOrder : []
  const mergedOrder = rawOrder.filter((k) => DEFAULT_ORDER.includes(k))
  for (const defKey of DEFAULT_ORDER) {
    if (!mergedOrder.includes(defKey)) mergedOrder.push(defKey)
  }
  form.sectionOrder = mergedOrder

  form.hero.items = (draft.hero?.items ?? []).map((item, idx) => ({
    key: item.key,
    enabled: item.enabled ?? true,
    sortOrder: item.sortOrder ?? (idx + 1) * 10,
    title: item.title ?? '',
    subtitle: item.subtitle ?? '',
    desktopMediaId: item.desktopMediaId != null ? Number(item.desktopMediaId) : null,
    mobileMediaId: item.mobileMediaId != null ? Number(item.mobileMediaId) : null,
    imageAlt: item.imageAlt ?? '',
    buttonText: item.buttonText ?? '了解更多',
    link: (item.link as LinkValue | null) ?? { type: 'CONSULT' },
  }))

  form.metrics = {
    enabled: draft.metrics?.enabled ?? true,
    title: draft.metrics?.title ?? '企业实力与服务规模',
    subtitle: draft.metrics?.subtitle ?? '',
    items: (draft.metrics?.items ?? []).map((m, idx) => ({
      key: m.key,
      sortOrder: m.sortOrder ?? (idx + 1) * 10,
      displayValue: m.displayValue ?? '',
      unit: m.unit ?? '',
      label: m.label ?? '',
      description: m.description ?? '',
    })),
  }

  form.services = {
    enabled: draft.services?.enabled ?? true,
    title: draft.services?.title ?? '核心产品与行业方案',
    subtitle: draft.services?.subtitle ?? '',
    groups: (draft.services?.groups ?? []).map((g, idx) => ({
      key: g.key,
      sortOrder: g.sortOrder ?? (idx + 1) * 10,
      title: g.title ?? '',
      description: g.description ?? '',
      imageMediaId: g.imageMediaId != null ? Number(g.imageMediaId) : null,
      contentType: g.contentType === 'SOLUTION' ? 'SOLUTION' : 'PRODUCT',
      contentIds: (g.contentIds ?? []).map((id) => Number(id)),
      moreLink: (g.moreLink as LinkValue | null) ?? { type: 'PAGE', pageCode: 'PRODUCTS' },
    })),
  }

  form.cases = {
    enabled: draft.cases?.enabled ?? true,
    title: draft.cases?.title ?? '客户案例',
    subtitle: draft.cases?.subtitle ?? '',
    contentIds: (draft.cases?.contentIds ?? []).map((id) => Number(id)),
  }

  form.customers = {
    enabled: draft.customers?.enabled ?? true,
    title: draft.customers?.title ?? '合作客户',
    subtitle: draft.customers?.subtitle ?? '',
    contentIds: (draft.customers?.contentIds ?? []).map((id) => Number(id)),
  }

  form.news = {
    enabled: draft.news?.enabled ?? true,
    title: draft.news?.title ?? '最新动态',
    subtitle: draft.news?.subtitle ?? '',
    mode: draft.news?.mode === 'LATEST' ? 'LATEST' : 'SELECTED',
    limit: draft.news?.limit ?? 3,
    contentIds: (draft.news?.contentIds ?? []).map((id) => Number(id)),
  }

  form.consultation = {
    title: draft.consultation?.title ?? '了解适合您的数字化服务',
    subtitle: draft.consultation?.subtitle ?? '',
    buttonText: draft.consultation?.buttonText ?? '立即咨询',
    link: (draft.consultation?.link as LinkValue | null) ?? { type: 'CONSULT' },
  }
}

function moveSection(idx: number, delta: number) {
  if (!form.sectionOrder) return
  const target = idx + delta
  if (target < 0 || target >= form.sectionOrder.length) return
  const [moved] = form.sectionOrder.splice(idx, 1)
  form.sectionOrder.splice(target, 0, moved)
}

function isSectionEnabled(secKey: string): boolean {
  if (secKey === 'consultation') return true
  const block = (form as Record<string, any>)[secKey]
  return Boolean(block?.enabled)
}

function toggleSectionEnabled(secKey: string, val: boolean) {
  if (secKey === 'consultation') return
  const block = (form as Record<string, any>)[secKey]
  if (block) block.enabled = val
}

function openMedia(kind: 'IMAGE' | 'VIDEO' | 'ALL', cb: (m: MediaItem) => void) {
  pickerKind.value = kind
  pickerCallback = cb
  pickerOpen.value = true
}

function onMediaPicked(media: MediaItem) {
  if (pickerCallback) {
    pickerCallback(media)
    pickerCallback = null
  }
  pickerOpen.value = false
}

function addHero() {
  if (form.hero.items.length >= 5) {
    ElMessage.warning('首屏轮播最多 5 条')
    return
  }
  form.hero.items.push({
    enabled: true,
    sortOrder: (form.hero.items.length + 1) * 10,
    title: '让物联网与 AI 成为企业增长引擎',
    subtitle: '全场景实时感知、智能风控与数字化协同平台',
    desktopMediaId: null,
    mobileMediaId: null,
    imageAlt: '',
    buttonText: '预约演示',
    link: { type: 'CONSULT' },
  })
}

function addMetric() {
  if (form.metrics.items.length >= 8) {
    ElMessage.warning('关键指标最多 8 条')
    return
  }
  form.metrics.items.push({
    sortOrder: (form.metrics.items.length + 1) * 10,
    displayValue: '99.9',
    unit: '%',
    label: '平台可用率',
    description: '金融级高可用云原生架构保障',
  })
}

function addServiceGroup() {
  if (form.services.groups.length >= 3) {
    ElMessage.warning('服务分组最多 3 组')
    return
  }
  form.services.groups.push({
    sortOrder: (form.services.groups.length + 1) * 10,
    title: '智能硬件与 SaaS 平台',
    description: '软硬一体全栈自研，开箱即用赋能业务现场。',
    imageMediaId: null,
    contentType: 'PRODUCT',
    contentIds: [],
    moreLink: { type: 'PAGE', pageCode: 'PRODUCTS' },
  })
}

function applyPresetMetrics() {
  form.metrics.enabled = true
  form.metrics.title = '值得信赖的行业数字化基座'
  form.metrics.subtitle = '用真实在线运行规模验证每一份交付承诺'
  form.metrics.items = [
    { sortOrder: 10, displayValue: '320', unit: '万+', label: '在线物联网终端', description: '7×24h 毫秒级状态感知与采集' },
    { sortOrder: 20, displayValue: '7000', unit: '+', label: '头部标杆企业客户', description: '覆盖能源、冷链、危化、快消制造' },
    { sortOrder: 30, displayValue: '68', unit: '%', label: '高危事故率平均下降', description: 'AI 双目防碰撞与主动安全托管' },
    { sortOrder: 40, displayValue: '99.95', unit: '%', label: '云平台服务可用性', description: '多活容灾与国密级数据安全认证' },
  ]
  ElMessage.success('已填入行业标杆指标模板，保存草稿后生效')
}

async function onSave() {
  const payload = JSON.parse(JSON.stringify(form)) as HomeDraft & Record<string, unknown>
  if (await save(payload)) fill()
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
    title="首页管理"
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
    <div class="studio-banner">
      <div>
        <strong class="studio-banner__title">🎨 想要更直观地边看边改？试试「全站实景交互预演工作台」</strong>
        <p class="studio-banner__desc">
          支持在真实官网画布上直接拖拽调整模块顺序、双击修改文字、点击替换图片/视频、绑定链接，修改结果与当前表单 100% 双向实时互通。
        </p>
      </div>
      <el-button type="primary" @click="router.push('/preview/home')">
        进入实景交互预演台 →
      </el-button>
    </div>

    <el-alert v-if="loadError" type="error" :closable="false" :title="loadError" class="err" />

    <el-tabs v-model="activeTab" class="home-tabs">
      <!-- Tab 0: 模块摆放顺序与总览 -->
      <el-tab-pane label="0. 模块摆放顺序与开关" name="order">
        <p class="tab-hint">
          首屏 Hero 固定置顶展示；下方 6 大业务模块可自由上下移动调整在前台首页的展示顺序，也可一键开启或隐藏。
        </p>
        <div class="order-list">
          <div v-for="(secKey, idx) in form.sectionOrder" :key="secKey" class="order-item">
            <span class="order-item__num">0{{ idx + 1 }}</span>
            <div class="order-item__info">
              <strong>{{ SECTION_LABELS[secKey]?.name || secKey }}</strong>
              <span>{{ SECTION_LABELS[secKey]?.desc }}</span>
            </div>
            <el-switch
              v-if="secKey !== 'consultation'"
              :model-value="isSectionEnabled(secKey)"
              active-text="启用"
              inactive-text="隐藏"
              @update:model-value="(v: boolean) => toggleSectionEnabled(secKey, v)"
            />
            <el-tag v-else size="small" type="info">常驻收口区</el-tag>
            <div class="order-item__btns">
              <el-button size="small" :disabled="idx === 0" @click="moveSection(idx, -1)">↑ 上移</el-button>
              <el-button
                size="small"
                :disabled="idx === (form.sectionOrder?.length ?? 0) - 1"
                @click="moveSection(idx, 1)"
              >
                ↓ 下移
              </el-button>
              <el-button size="small" type="primary" link @click="activeTab = secKey">编辑内容</el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- Tab 1: 首屏 Hero -->
      <el-tab-pane label="1. 首屏轮播 (Hero)" name="hero">
        <p class="tab-hint">最多 5 条首屏，支持桌面大图/视频与移动端竖图自适应。正式发布至少需 1 条有效首屏。</p>
        <div v-for="(slide, idx) in form.hero.items" :key="slide.key ?? `hero-${idx}`" class="block-card">
          <div class="block-card__head">
            <strong>首屏 #{{ idx + 1 }}</strong>
            <el-switch v-model="slide.enabled" active-text="启用" />
            <el-button link type="danger" @click="form.hero.items.splice(idx, 1)">删除</el-button>
          </div>
          <el-form label-width="110px">
            <el-form-item label="主标题" required>
              <el-input v-model="slide.title" maxlength="40" show-word-limit placeholder="最多 40 字" />
            </el-form-item>
            <el-form-item label="副标题">
              <el-input v-model="slide.subtitle" type="textarea" :rows="2" maxlength="100" show-word-limit />
            </el-form-item>
            <el-form-item label="桌面图片/视频" required>
              <div class="media-pick-row">
                <el-tag v-if="slide.desktopMediaId" type="success">素材 ID: #{{ slide.desktopMediaId }}</el-tag>
                <span v-else class="muted">未选择</span>
                <el-button size="small" @click="openMedia('ALL', (m) => (slide.desktopMediaId = Number(m.id)))">
                  选择素材
                </el-button>
                <el-button v-if="slide.desktopMediaId" size="small" link type="danger" @click="slide.desktopMediaId = null">
                  清除
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="手机端配图">
              <div class="media-pick-row">
                <el-tag v-if="slide.mobileMediaId" type="info">素材 ID: #{{ slide.mobileMediaId }}</el-tag>
                <span v-else class="muted">可选，未选时自动回退桌面素材</span>
                <el-button size="small" @click="openMedia('IMAGE', (m) => (slide.mobileMediaId = Number(m.id)))">
                  选择图片
                </el-button>
                <el-button v-if="slide.mobileMediaId" size="small" link type="danger" @click="slide.mobileMediaId = null">
                  清除
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="主按钮文案">
              <el-input v-model="slide.buttonText" maxlength="12" style="width: 220px" placeholder="例如：预约演示" />
            </el-form-item>
            <el-form-item label="按钮跳转动作">
              <LinkEditor v-model="slide.link" :allow-consult="true" />
            </el-form-item>
          </el-form>
        </div>
        <el-button @click="addHero">+ 添加首屏轮播项</el-button>
      </el-tab-pane>

      <!-- Tab 2: 企业指标 -->
      <el-tab-pane label="2. 企业指标 (Metrics)" name="metrics">
        <div class="tab-toolbar">
          <el-switch v-model="form.metrics.enabled" active-text="在前台启用该模块" />
          <el-button size="small" type="primary" plain @click="applyPresetMetrics">
            ✨ 一键填入行业标杆指标模板
          </el-button>
        </div>
        <el-form label-width="110px">
          <el-form-item label="区块标题">
            <el-input v-model="form.metrics.title" maxlength="40" show-word-limit />
          </el-form-item>
          <el-form-item label="区块副标题">
            <el-input v-model="form.metrics.subtitle" maxlength="200" show-word-limit />
          </el-form-item>
        </el-form>
        <div v-for="(m, idx) in form.metrics.items" :key="m.key ?? `m-${idx}`" class="metric-row">
          <el-input v-model="m.displayValue" maxlength="16" placeholder="数值(如 320)" style="width: 130px" />
          <el-input v-model="m.unit" maxlength="8" placeholder="单位(万+)" style="width: 90px" />
          <el-input v-model="m.label" maxlength="20" placeholder="指标名称" style="width: 180px" />
          <el-input v-model="m.description" maxlength="80" placeholder="补充说明（选填）" style="flex: 1" />
          <el-button link type="danger" @click="form.metrics.items.splice(idx, 1)">删除</el-button>
        </div>
        <el-button @click="addMetric">+ 添加企业指标</el-button>
      </el-tab-pane>

      <!-- Tab 3: 产品与解决方案分组 -->
      <el-tab-pane label="3. 产品与服务 (Services)" name="services">
        <div class="tab-toolbar">
          <el-switch v-model="form.services.enabled" active-text="在前台启用该模块" />
        </div>
        <el-form label-width="110px">
          <el-form-item label="模块主标题">
            <el-input v-model="form.services.title" maxlength="40" show-word-limit />
          </el-form-item>
          <el-form-item label="模块副标题">
            <el-input v-model="form.services.subtitle" maxlength="200" show-word-limit />
          </el-form-item>
        </el-form>

        <div v-for="(grp, gIdx) in form.services.groups" :key="grp.key ?? `grp-${gIdx}`" class="block-card">
          <div class="block-card__head">
            <strong>服务分组 #{{ gIdx + 1 }}</strong>
            <el-button link type="danger" @click="form.services.groups.splice(gIdx, 1)">删除分组</el-button>
          </div>
          <el-form label-width="110px">
            <el-form-item label="分组标题" required>
              <el-input v-model="grp.title" maxlength="30" show-word-limit />
            </el-form-item>
            <el-form-item label="分组说明">
              <el-input v-model="grp.description" type="textarea" :rows="2" maxlength="200" show-word-limit />
            </el-form-item>

            <el-form-item label="关联类型">
              <el-radio-group v-model="grp.contentType" @change="grp.contentIds = []">
                <el-radio value="PRODUCT">产品 (PRODUCT)</el-radio>
                <el-radio value="SOLUTION">解决方案 (SOLUTION)</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="选择推荐内容">
              <ContentPicker v-model="grp.contentIds" :content-type="grp.contentType" :max="6" />
            </el-form-item>
            <el-form-item label="更多链接">
              <LinkEditor v-model="grp.moreLink" :allow-consult="true" />
            </el-form-item>
          </el-form>
        </div>
        <el-button @click="addServiceGroup">+ 添加服务分组（最多 3 组）</el-button>
      </el-tab-pane>

      <!-- Tab 4: 客户案例 -->
      <el-tab-pane label="4. 客户案例 (Cases)" name="cases">
        <div class="tab-toolbar">
          <el-switch v-model="form.cases.enabled" active-text="在前台启用该模块" />
        </div>
        <el-form label-width="110px">
          <el-form-item label="模块标题">
            <el-input v-model="form.cases.title" maxlength="40" show-word-limit />
          </el-form-item>
          <el-form-item label="模块副标题">
            <el-input v-model="form.cases.subtitle" maxlength="200" show-word-limit />
          </el-form-item>
          <el-form-item label="精选案例">
            <ContentPicker v-model="form.cases.contentIds" content-type="CASE" :max="6" />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- Tab 5: 合作客户标识 -->
      <el-tab-pane label="5. 客户标识墙 (Customers)" name="customers">
        <div class="tab-toolbar">
          <el-switch v-model="form.customers.enabled" active-text="在前台启用该模块" />
        </div>
        <el-form label-width="110px">
          <el-form-item label="模块标题">
            <el-input v-model="form.customers.title" maxlength="40" show-word-limit />
          </el-form-item>
          <el-form-item label="模块副标题">
            <el-input v-model="form.customers.subtitle" maxlength="200" show-word-limit />
          </el-form-item>
          <el-form-item label="选择客户标识">
            <ContentPicker v-model="form.customers.contentIds" content-type="CUSTOMER" :max="30" />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- Tab 6: 新闻动态 -->
      <el-tab-pane label="6. 新闻动态 (News)" name="news">
        <div class="tab-toolbar">
          <el-switch v-model="form.news.enabled" active-text="在前台启用该模块" />
        </div>
        <el-form label-width="110px">
          <el-form-item label="模块标题">
            <el-input v-model="form.news.title" maxlength="40" show-word-limit />
          </el-form-item>
          <el-form-item label="展示模式">
            <el-radio-group v-model="form.news.mode">
              <el-radio value="LATEST">按最新发布日期自动取前 N 条 (LATEST)</el-radio>
              <el-radio value="SELECTED">人工手选指定新闻 (SELECTED)</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="展示条数">
            <el-input-number v-model="form.news.limit" :min="1" :max="6" />
          </el-form-item>
          <el-form-item v-if="form.news.mode === 'SELECTED'" label="手选新闻">
            <ContentPicker v-model="form.news.contentIds" content-type="NEWS" :max="6" />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- Tab 7: 底部咨询区 -->
      <el-tab-pane label="7. 底部咨询区 (CTA)" name="consultation">
        <el-form label-width="110px">
          <el-form-item label="主标题" required>
            <el-input v-model="form.consultation.title" maxlength="40" show-word-limit />
          </el-form-item>
          <el-form-item label="副标题说明">
            <el-input v-model="form.consultation.subtitle" type="textarea" :rows="2" maxlength="200" show-word-limit />
          </el-form-item>
          <el-form-item label="按钮文字" required>
            <el-input v-model="form.consultation.buttonText" maxlength="12" style="width: 220px" />
          </el-form-item>
          <el-form-item label="按钮动作">
            <LinkEditor v-model="form.consultation.link" :allow-consult="true" :allow-none="false" />
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <MediaPicker v-model="pickerOpen" :kind="pickerKind" @select="onMediaPicked" />

    <template #tips>
      <p>首页模块摆放顺序与各区块内容均已与「实景交互预演工作台 (/preview/home)」双向实时打通。</p>
      <p>关联的产品、方案、案例、新闻与客户标识自动读取目标内容的最新线上发布版本，无需在首页重复录入标题和封面。</p>
    </template>
  </ConfigPanel>
</template>

<style scoped>
.studio-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  margin-bottom: 18px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f5f3ff 0%, #ede9fe 100%);
  border: 1px solid #c4b5fd;
  flex-wrap: wrap;
}

.studio-banner__title {
  font-size: 14.5px;
  color: #3730a3;
}

.studio-banner__desc {
  margin: 4px 0 0;
  font-size: 12.5px;
  color: #4f46e5;
}

.err {
  margin-bottom: 12px;
}

.tab-hint {
  font-size: 12.5px;
  color: var(--a-text-muted);
  margin: 0 0 14px;
}

.tab-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 18px;
  border: 1px solid var(--a-border);
  border-radius: 10px;
  background: #fafbfe;
}

.order-item__num {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: #eeebff;
  color: #5b4be8;
  font-weight: 700;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.order-item__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.order-item__info strong {
  font-size: 14px;
}

.order-item__info span {
  font-size: 12px;
  color: var(--a-text-muted);
}

.order-item__btns {
  display: flex;
  align-items: center;
  gap: 8px;
}

.block-card {
  border: 1px solid var(--a-border);
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 14px;
  background: #fafbfe;
}

.block-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.media-pick-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.muted {
  font-size: 12px;
  color: var(--a-text-muted);
}

.metric-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}
</style>
