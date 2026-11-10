<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import ConfigPanel from '@/components/ConfigPanel.vue'
import LinkEditor, { type LinkValue } from '@/components/LinkEditor.vue'
import { useConfig } from '@/composables/useConfig'

interface NavNode {
  key?: string
  label: string
  enabled: boolean
  sortOrder: number
  link: LinkValue | null
  children?: NavNode[]
}

interface MegaSubItem {
  label: string
  path: string
  tag?: string
  isVideo?: boolean
  videoUrl?: string
  videoTitle?: string
}

interface MegaColumn {
  title: string
  items: MegaSubItem[]
}

interface NavigationDraft {
  items: NavNode[]
  megaColumns?: MegaColumn[]
  megaCta?: { text: string; linkText: string }
}

const activeTab = ref<'main' | 'mega'>('main')

const { view, loading, saving, publishing, loadError, load, save, publish } =
  useConfig<NavigationDraft & Record<string, unknown>>('NAVIGATION')

const form = reactive<{
  items: NavNode[]
  megaColumns: MegaColumn[]
  megaCta: { text: string; linkText: string }
}>({
  items: [],
  megaColumns: [],
  megaCta: { text: '需要专业场景建议？', linkText: '联系技术专家' },
})

const DEFAULT_MEGA_COLUMNS: MegaColumn[] = [
  {
    title: 'AI产品',
    items: [{ label: '紫宝盒', path: '/products/1', tag: '首款', isVideo: false }],
  },
  {
    title: 'IoT服务',
    items: [
      { label: '安全管家', path: '/products?category=安全', isVideo: false },
      { label: '冷链管家', path: '/products?category=冷链', isVideo: false },
      { label: '流向管家', path: '/products?category=流向', isVideo: false },
      { label: '追货管家', path: '/products?category=追货', isVideo: false },
    ],
  },
  {
    title: '交易服务',
    items: [
      { label: '财运通', path: '/solutions?category=交易', isVideo: false },
      { label: '数字能源', path: '/solutions?category=能源', isVideo: false },
    ],
  },
  {
    title: '解决方案',
    items: [
      { label: '大宗物流数字化', path: '/solutions/1', isVideo: false },
      { label: '连锁零售数字化', path: '/solutions/2', isVideo: false },
      { label: '饲料防偷盗 AI 治理', path: '/solutions/3', isVideo: false },
    ],
  },
  {
    title: '产品演示',
    items: [
      {
        label: '产品操作视频',
        path: '',
        isVideo: true,
        videoTitle: '紫宝盒 | 物流行业首款全场景AI工具产品演示',
        videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
      },
    ],
  },
]

function normalize(node: Partial<NavNode>): NavNode {
  return {
    key: node.key,
    label: node.label ?? '',
    enabled: node.enabled ?? true,
    sortOrder: node.sortOrder ?? 100,
    link: (node.link as LinkValue | null) ?? { type: 'NONE' },
    children: (node.children ?? []).map((child) => ({
      key: child.key,
      label: child.label ?? '',
      enabled: child.enabled ?? true,
      sortOrder: child.sortOrder ?? 100,
      link: (child.link as LinkValue | null) ?? { type: 'PAGE', pageCode: 'HOME' },
    })),
  }
}

function fill() {
  const draft = view.value?.draft
  form.items = (draft?.items ?? []).map((item) => normalize(item))
  if (form.items.length === 0) {
    form.items = [
      normalize({ label: '首页', link: { type: 'PAGE', pageCode: 'HOME' }, sortOrder: 10 }),
      normalize({ label: '产品与方案', link: { type: 'PAGE', pageCode: 'PRODUCTS' }, sortOrder: 20 }),
      normalize({ label: '客户案例', link: { type: 'PAGE', pageCode: 'CASES' }, sortOrder: 30 }),
      normalize({ label: '新闻动态', link: { type: 'PAGE', pageCode: 'NEWS' }, sortOrder: 40 }),
      normalize({ label: '关于我们', link: { type: 'PAGE', pageCode: 'ABOUT' }, sortOrder: 50 }),
    ]
  }
  form.megaColumns = (draft?.megaColumns ?? []).map((col) => ({
    title: col.title ?? '',
    items: (col.items ?? []).map((sub) => ({
      label: sub.label ?? '',
      path: sub.path ?? '',
      tag: sub.tag ?? '',
      isVideo: Boolean(sub.isVideo),
      videoUrl: sub.videoUrl ?? '',
      videoTitle: sub.videoTitle ?? '',
    })),
  }))
  form.megaCta = {
    text: draft?.megaCta?.text ?? '需要专业场景建议？',
    linkText: draft?.megaCta?.linkText ?? '联系技术专家',
  }
}

function applyDefaultMega() {
  form.megaColumns = JSON.parse(JSON.stringify(DEFAULT_MEGA_COLUMNS))
  form.megaCta = { text: '需要专业场景建议？', linkText: '联系技术专家' }
  ElMessage.success('已载入 5 列业务全景菜单预设，保存草稿后生效')
}

function addTop() {
  if (form.items.length >= 8) {
    ElMessage.warning('顶级导航最多 8 项')
    return
  }
  form.items.push(
    normalize({ label: '', link: { type: 'PAGE', pageCode: 'HOME' }, sortOrder: (form.items.length + 1) * 10 }),
  )
}

function addChild(item: NavNode) {
  item.children = item.children ?? []
  if (item.children.length >= 10) {
    ElMessage.warning('子菜单最多 10 项')
    return
  }
  item.children.push(
    normalize({ label: '', link: { type: 'PAGE', pageCode: 'HOME' }, sortOrder: (item.children.length + 1) * 10 }),
  )
}

function move<T extends { sortOrder: number }>(list: T[], index: number, delta: number) {
  const target = index + delta
  if (target < 0 || target >= list.length) return
  const current = list[index].sortOrder
  list[index].sortOrder = list[target].sortOrder
  list[target].sortOrder = current
  const [item] = list.splice(index, 1)
  list.splice(target, 0, item)
}

function addMegaCol() {
  if (form.megaColumns.length >= 8) {
    ElMessage.warning('全景大菜单最多 8 列')
    return
  }
  form.megaColumns.push({
    title: '新业务列',
    items: [{ label: '示例产品', path: '/products', tag: '', isVideo: false }],
  })
}

function addMegaItem(col: MegaColumn) {
  if (col.items.length >= 15) {
    ElMessage.warning('每列最多 15 个项目')
    return
  }
  col.items.push({ label: '', path: '/products', tag: '', isVideo: false })
}

function validateBeforeSave(): boolean {
  for (let i = 0; i < form.items.length; i++) {
    const item = form.items[i]
    if (!item.label.trim()) {
      ElMessage.warning(`第 ${i + 1} 个顶级导航名称不能为空`)
      return false
    }
    for (let j = 0; j < (item.children?.length ?? 0); j++) {
      const child = item.children![j]
      if (!child.label.trim()) {
        ElMessage.warning(`导航「${item.label}」的第 ${j + 1} 个子项名称不能为空`)
        return false
      }
      if (!child.link || child.link.type === 'NONE') {
        ElMessage.warning(`子菜单「${child.label}」必须配置有效跳转链接`)
        return false
      }
    }
  }
  for (let c = 0; c < form.megaColumns.length; c++) {
    const col = form.megaColumns[c]
    if (!col.title.trim()) {
      ElMessage.warning(`全景菜单第 ${c + 1} 列标题不能为空`)
      return false
    }
    for (let k = 0; k < col.items.length; k++) {
      if (!col.items[k].label.trim()) {
        ElMessage.warning(`全景菜单「${col.title}」中第 ${k + 1} 项名称不能为空`)
        return false
      }
    }
  }
  return true
}

async function onSave(): Promise<boolean> {
  if (!validateBeforeSave()) return false
  const payload: NavigationDraft & Record<string, unknown> = {
    items: form.items,
    megaColumns: form.megaColumns,
    megaCta: form.megaCta,
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
    title="导航与全景大菜单管理"
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

    <el-tabs v-model="activeTab">
      <!-- Tab 1: 常规两级导航树 -->
      <el-tab-pane label="1. 顶部主导航菜单 (items)" name="main">
        <el-alert
          type="info"
          :closable="false"
          class="err"
          title="最多两级，顶层最多 8 项、每项最多 10 个子项。有子项的父节点可以设为「无链接」；叶子节点必须有有效链接才能发布。"
        />

        <div v-for="(item, index) in form.items" :key="item.key ?? `top-${index}`" class="node">
          <div class="node__head">
            <el-switch v-model="item.enabled" active-text="显示" />
            <el-input v-model="item.label" maxlength="20" placeholder="导航名称" style="width: 160px" />
            <span class="node__order">排序 {{ item.sortOrder }}</span>
            <div class="node__actions">
              <el-button link :disabled="index === 0" @click="move(form.items, index, -1)">上移</el-button>
              <el-button link :disabled="index === form.items.length - 1" @click="move(form.items, index, 1)">下移</el-button>
              <el-button link @click="addChild(item)">添加子项</el-button>
              <el-button link type="danger" @click="form.items.splice(index, 1)">删除</el-button>
            </div>
          </div>

          <div class="node__link">
            <span class="label">链接</span>
            <LinkEditor v-model="item.link" :allow-none="true" :allow-consult="true" />
            <span v-if="item.children?.length" class="hint">已有子项时，这里可设为「无链接」，前台作为展开父节点</span>
          </div>

          <div v-if="item.children?.length" class="children">
            <div v-for="(child, childIndex) in item.children" :key="child.key ?? `child-${index}-${childIndex}`" class="child">
              <el-switch v-model="child.enabled" />
              <el-input v-model="child.label" maxlength="20" placeholder="子项名称" style="width: 150px" />
              <LinkEditor v-model="child.link" :allow-none="false" :allow-consult="true" />
              <el-button link :disabled="childIndex === 0" @click="move(item.children!, childIndex, -1)">上移</el-button>
              <el-button link :disabled="childIndex === (item.children?.length ?? 0) - 1" @click="move(item.children!, childIndex, 1)">下移</el-button>
              <el-button link type="danger" @click="item.children!.splice(childIndex, 1)">删除</el-button>
            </div>
          </div>
        </div>

        <el-button class="add" @click="addTop">+ 添加顶级导航项</el-button>
      </el-tab-pane>

      <!-- Tab 2: 5列业务全景下拉大菜单 (megaColumns + megaCta) -->
      <el-tab-pane label="2. 五列业务全景下拉菜单 (megaColumns)" name="mega">
        <div class="mega-toolbar">
          <p class="hint">
            当前台导航项名称包含「产品」或「方案」且展开时，将自动渲染此五列业务全景大菜单（支持角标与视频弹窗演示）。
          </p>
          <el-button size="small" type="primary" plain @click="applyDefaultMega">
            ✨ 一键载入默认五列全景模板
          </el-button>
        </div>

        <div v-for="(col, cIdx) in form.megaColumns" :key="`mega-col-${cIdx}`" class="node">
          <div class="node__head">
            <strong>第 {{ cIdx + 1 }} 列</strong>
            <el-input v-model="col.title" maxlength="30" placeholder="列标题（如：AI产品 / IoT服务）" style="width: 220px" />
            <div class="node__actions">
              <el-button link @click="addMegaItem(col)">+ 添加子项</el-button>
              <el-button link type="danger" @click="form.megaColumns.splice(cIdx, 1)">删除列</el-button>
            </div>
          </div>

          <div class="children">
            <div v-for="(sub, sIdx) in col.items" :key="`mega-sub-${cIdx}-${sIdx}`" class="child">
              <el-input v-model="sub.label" maxlength="40" placeholder="项目名称" style="width: 150px" />
              <el-input v-model="sub.tag" maxlength="10" placeholder="角标(如:首款)" style="width: 110px" />
              <el-checkbox v-model="sub.isVideo">视频演示弹窗</el-checkbox>
              <template v-if="sub.isVideo">
                <el-input v-model="sub.videoTitle" maxlength="100" placeholder="视频弹窗标题" style="width: 190px" />
                <el-input v-model="sub.videoUrl" maxlength="500" placeholder="视频 MP4 地址" style="width: 220px" />
              </template>
              <template v-else>
                <el-input v-model="sub.path" maxlength="255" placeholder="站内路径（如 /products/1）" style="width: 240px" />
              </template>
              <el-button link type="danger" @click="col.items.splice(sIdx, 1)">删除</el-button>
            </div>
          </div>
        </div>

        <el-button class="add" @click="addMegaCol">+ 添加全景菜单列</el-button>

        <el-divider content-position="left">全景菜单底部引导条</el-divider>
        <el-form label-width="120px">
          <el-form-item label="左侧提示文案">
            <el-input v-model="form.megaCta.text" maxlength="100" style="width: 320px" />
          </el-form-item>
          <el-form-item label="右侧按钮文字">
            <el-input v-model="form.megaCta.linkText" maxlength="50" style="width: 220px" />
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <template #tips>
      <p>页脚链接在「全站设置」的页脚分组里维护，不在导航中重复配置。</p>
      <p>目标内容下线后，前台会自动隐藏该导航项；父节点没有有效子项时整块隐藏。</p>
    </template>
  </ConfigPanel>
</template>

<style scoped>
.err {
  margin-bottom: 12px;
}

.mega-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.node {
  border: 1px solid var(--a-border);
  border-radius: 8px;
  padding: 14px;
  margin-bottom: 12px;
  background: #fbfcfe;
}

.node__head {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.node__order {
  font-size: 12px;
  color: var(--a-text-muted);
}

.node__actions {
  margin-left: auto;
}

.node__link {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.label {
  font-size: 13px;
  color: var(--a-text-muted);
  width: 40px;
}

.hint {
  font-size: 12px;
  color: var(--a-text-muted);
  margin: 0;
}

.children {
  margin-top: 12px;
  padding-left: 14px;
  border-left: 2px solid var(--a-border);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.child {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.add {
  margin-top: 4px;
}
</style>
