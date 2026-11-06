<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import ConfigPanel from '@/components/ConfigPanel.vue'
import { useConfig } from '@/composables/useConfig'

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

const activeTab = ref('intro')

const { view, loading, saving, publishing, loadError, load, save, publish } =
  useConfig<Record<string, any>>('ABOUT')

const form = reactive({
  heroTitle: '以物联网技术和软件服务改变公路货运',
  introTitle: '关于G7易流',
  intro: '',
  introImage: '',
  ceoQuote: '“我谨代表G7易流全体员工，向在这个不凡时代中努力前行的货运经营者致以崇高的敬意！”',
  ceoTitle: '从微不足道开始改变世界',
  ceoSignature: '翟学魂',
  ceoButtonText: '展开全部',
  ceoExcerptText: '',
  ceoFullText: '',
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
  form.intro = draft.intro ?? ''
  form.introImage = draft.introImage ?? ''

  if (draft.ceoLetter) {
    form.ceoQuote = draft.ceoLetter.leftQuote ?? form.ceoQuote
    form.ceoTitle = draft.ceoLetter.title ?? form.ceoTitle
    form.ceoSignature = draft.ceoLetter.signature ?? form.ceoSignature
    form.ceoButtonText = draft.ceoLetter.buttonText ?? '展开全部'
    form.ceoExcerptText = Array.isArray(draft.ceoLetter.excerpt)
      ? draft.ceoLetter.excerpt.join('\n')
      : ''
    form.ceoFullText = Array.isArray(draft.ceoLetter.fullParagraphs)
      ? draft.ceoLetter.fullParagraphs.join('\n')
      : ''
  }

  form.teamTitle = draft.teamTitle ?? 'G7易流管理团队'
  if (Array.isArray(draft.teamMembers) && draft.teamMembers.length > 0) {
    form.teamMembers = draft.teamMembers.map((m: any) => ({
      name: m.name ?? '',
      role: m.role ?? '',
      avatar: m.avatar ?? '',
      bioText: Array.isArray(m.bio) ? m.bio.join('\n') : '',
    }))
  } else {
    form.teamMembers = [
      {
        name: '翟学魂',
        role: 'G7易流首席执行官',
        avatar: '',
        bioText:
          '翟学魂先生，现任物联网软件服务公司G7易流首席执行官。\n作为中国产业物联网和物流数字化的开拓者，打造了商用车物联网平台。\n翟学魂先生毕业于清华大学物理系，并同期完成清华大学经管学院双学士学位学业。',
      },
      {
        name: '张景涛',
        role: 'G7易流总裁',
        avatar: '',
        bioText: '张景涛先生，现任G7易流总裁，全面负责公司产品研发、行业解决方案及客户成功体系建设。',
      },
      {
        name: '郝淑霞',
        role: 'G7易流高级副总裁',
        avatar: '',
        bioText: '郝淑霞女士，现任G7易流高级副总裁，负责组织发展、人才体系与大客户战略生态协同。',
      },
      {
        name: '张杰龙',
        role: 'G7易流首席财务官',
        avatar: '',
        bioText: '张杰龙先生，现任G7易流首席财务官（CFO），负责集团财务战略、资本市场运作及战略投资并购。',
      },
    ]
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
  }

  if (Array.isArray(draft.customerStories) && draft.customerStories.length > 0) {
    form.customerStories = draft.customerStories.map((s: any) => ({
      quote: s.quote ?? '',
      author: s.author ?? '',
      company: s.company ?? '',
      logoText: s.logoText ?? '',
      videoUrl: s.videoUrl ?? '',
    }))
  }

  form.mission = draft.mission ?? form.mission
  form.vision = draft.vision ?? form.vision
  form.joinTitle = draft.joinTitle ?? 'G7易流路上见'
  form.joinButtonText = draft.joinButtonText ?? '查看职位'
  form.joinUrl = draft.joinUrl ?? ''

  if (Array.isArray(draft.values) && draft.values.length > 0) {
    form.values = draft.values.map((v: any) => ({
      title: v.title ?? '',
      linesText: Array.isArray(v.lines) ? v.lines.join('\n') : '',
    }))
  } else {
    form.values = [
      { title: '客户成功', linesText: '我们存在的唯一目的就是为客户带来价值\n以可信赖的产品和有温度的服务，做客户的长期朋友' },
      { title: '极限', linesText: '目标远大，不甘平庸，不断挑战认知边界\n把事做透，用工匠精神实现卓越成果' },
      { title: '信任', linesText: '以身作则，说到做到\n敢于信任，从我做起' },
      { title: '坚韧', linesText: '坚持长期主义积极面对挑战和不确定性\n坚定目标，锲而不舍，迎难而上，确保结果' },
      { title: '主人翁精神', linesText: '从全局出发，长期利益最大化\n主动担当，站在造钟者的角度思考并解决问题' },
    ]
  }

  form.milestones = (draft.milestones ?? []).map((item: any) => ({ ...item }))
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

async function onSave() {
  if (!validateBeforeSave()) return
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

    <el-tabs v-model="activeTab">
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
        <el-form label-width="120px" class="form">
          <el-form-item label="团队页面标题">
            <el-input v-model="form.teamTitle" maxlength="60" style="width: 320px" />
          </el-form-item>
        </el-form>
        <div v-for="(tm, tIdx) in form.teamMembers" :key="tIdx" class="card-box">
          <div class="card-box__head">
            <strong>高管成员 #{{ tIdx + 1 }}</strong>
            <el-button link type="danger" @click="form.teamMembers.splice(tIdx, 1)">删除</el-button>
          </div>
          <div class="row-inline">
            <el-input v-model="tm.name" placeholder="姓名（如：翟学魂）" style="width: 180px" />
            <el-input v-model="tm.role" placeholder="职务（如：首席执行官）" style="width: 240px" />
            <el-input v-model="tm.avatar" placeholder="头像图片 URL（选填）" style="flex: 1" />
          </div>
          <el-input
            v-model="tm.bioText"
            type="textarea"
            :rows="3"
            placeholder="高管履历介绍（每行一段，点击人物卡片时在左右分栏弹窗中展示）"
          />
        </div>
        <el-button @click="form.teamMembers.push({ name: '', role: '', avatar: '', bioText: '' })">
          + 添加管理团队成员
        </el-button>
      </el-tab-pane>

      <!-- Tab 3: 资料共享 & 客户成功故事 -->
      <el-tab-pane label="3. 资料库 & 客户成功故事" name="resources">
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
</style>
