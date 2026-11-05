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

export interface MegaSubItem {
  label: string
  path: string
  tag?: string
  isVideo?: boolean
  videoUrl?: string
  videoTitle?: string
}

export interface MegaColumn {
  title: string
  items: MegaSubItem[]
}

export interface MegaCta {
  text: string
  linkText: string
}

interface NavigationDraft {
  items: NavNode[]
  megaColumns?: MegaColumn[]
  megaCta?: MegaCta
}

const defaultMegaColumns: MegaColumn[] = [
  {
    title: 'AI产品',
    items: [
      { label: '紫宝盒', path: '/products/1', tag: '首款', isVideo: false }
    ]
  },
  {
    title: 'IoT服务',
    items: [
      { label: '安全管家', path: '/products?category=安全', isVideo: false },
      { label: '冷链管家', path: '/products?category=冷链', isVideo: false },
      { label: '流向管家', path: '/products?category=流向', isVideo: false },
      { label: '追货管家', path: '/products?category=追货', isVideo: false }
    ]
  },
  {
    title: '交易服务',
    items: [
      { label: '财运通', path: '/solutions?category=交易', isVideo: false },
      { label: '数字能源', path: '/solutions?category=能源', isVideo: false }
    ]
  },
  {
    title: '解决方案',
    items: [
      { label: '大宗物流数字化', path: '/solutions/1', isVideo: false },
      { label: '连锁零售数字化', path: '/solutions/2', isVideo: false },
      { label: '饲料防偷盗 AI 治理', path: '/solutions/3', isVideo: false }
    ]
  },
  {
    title: '产品演示',
    items: [
      {
        label: '产品操作视频',
        path: '',
        isVideo: true,
        videoTitle: '紫宝盒 | 物流行业首款全场景AI工具产品演示',
        videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4'
      }
    ]
  }
]

const { view, loading, saving, publishing, loadError, load, save, publish } =
  useConfig<NavigationDraft & Record<string, unknown>>('NAVIGATION')

const activeTab = ref('top')

const form = reactive<{
  items: NavNode[]
  megaColumns: MegaColumn[]
  megaCta: MegaCta
}>({
  items: [],
  megaColumns: [],
  megaCta: { text: '需要专业场景建议？', linkText: '联系技术专家' }
})

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
      normalize({ label: '产品', link: { type: 'PAGE', pageCode: 'PRODUCTS' }, sortOrder: 20 }),
      normalize({ label: '解决方案', link: { type: 'PAGE', pageCode: 'SOLUTIONS' }, sortOrder: 30 }),
      normalize({ label: '客户案例', link: { type: 'PAGE', pageCode: 'CASES' }, sortOrder: 40 }),
      normalize({ label: '新闻', link: { type: 'PAGE', pageCode: 'NEWS' }, sortOrder: 50 }),
      normalize({ label: '关于我们', link: { type: 'PAGE', pageCode: 'ABOUT' }, sortOrder: 60 }),
      normalize({ label: '联系我们', link: { type: 'PAGE', pageCode: 'CONTACT' }, sortOrder: 70 }),
    ]
  }

  // 大菜单回填
  if (draft?.megaColumns && draft.megaColumns.length > 0) {
    form.megaColumns = JSON.parse(JSON.stringify(draft.megaColumns))
  } else {
    form.megaColumns = JSON.parse(JSON.stringify(defaultMegaColumns))
  }

  if (draft?.megaCta) {
    form.megaCta = {
      text: draft.megaCta.text ?? '需要专业场景建议？',
      linkText: draft.megaCta.linkText ?? '联系技术专家'
    }
  } else {
    form.megaCta = { text: '需要专业场景建议？', linkText: '联系技术专家' }
  }
}

function addTop() {
  if (form.items.length >= 8) {
    ElMessage.warning('顶级导航最多 8 项')
    return
  }
  form.items.push(normalize({ label: '', link: { type: 'PAGE', pageCode: 'HOME' }, sortOrder: (form.items.length + 1) * 10 }))
}

function addChild(item: NavNode) {
  item.children = item.children ?? []
  if (item.children.length >= 10) {
    ElMessage.warning('子菜单最多 10 项')
    return
  }
  item.children.push(normalize({ label: '', link: { type: 'PAGE', pageCode: 'HOME' }, sortOrder: (item.children.length + 1) * 10 }))
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

// ----------------- 大菜单操作函数 -----------------
function addCol() {
  if (form.megaColumns.length >= 6) {
    ElMessage.warning('大菜单最多 6 列')
    return
  }
  form.megaColumns.push({
    title: '新分类',
    items: [{ label: '新产品', path: '/products', tag: '', isVideo: false }]
  })
}

function removeCol(index: number) {
  form.megaColumns.splice(index, 1)
}

function moveCol(index: number, delta: number) {
  const target = index + delta
  if (target < 0 || target >= form.megaColumns.length) return
  const [col] = form.megaColumns.splice(index, 1)
  form.megaColumns.splice(target, 0, col)
}

function addItem(col: MegaColumn) {
  if (col.items.length >= 12) {
    ElMessage.warning('每列最多 12 个选项')
    return
  }
  col.items.push({
    label: '',
    path: '/products',
    tag: '',
    isVideo: false
  })
}

function moveItem(list: MegaSubItem[], index: number, delta: number) {
  const target = index + delta
  if (target < 0 || target >= list.length) return
  const [item] = list.splice(index, 1)
  list.splice(target, 0, item)
}

function resetToDefaultMega() {
  form.megaColumns = JSON.parse(JSON.stringify(defaultMegaColumns))
  form.megaCta = { text: '需要专业场景建议？', linkText: '联系技术专家' }
  ElMessage.success('已重置为系统默认 5 列全景菜单配置')
}

async function onSave() {
  if (form.items.some((item) => !item.label.trim())) {
    ElMessage.warning('顶级导航名称不能为空')
    return
  }
  for (const col of form.megaColumns) {
    if (!col.title.trim()) {
      ElMessage.warning('大菜单列标题不能为空')
      return
    }
    for (const sub of col.items) {
      if (!sub.label.trim()) {
        ElMessage.warning(`列「${col.title}」中有未填写的选项名称`)
        return
      }
    }
  }
  if (await save({
    items: form.items,
    megaColumns: form.megaColumns,
    megaCta: form.megaCta
  } as NavigationDraft & Record<string, unknown>)) {
    fill()
  }
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
    title="导航与产品下拉菜单"
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

    <el-tabs v-model="activeTab" class="nav-tabs" type="border-card">
      <!-- Tab 1: 顶部主导航 -->
      <el-tab-pane label="顶部主导航设置" name="top">
        <el-alert
          type="info"
          :closable="false"
          class="err"
          title="设置官网顶部主导航条（最多 8 项，每项最多 10 个子项）。点击保存并发布后在前台生效。"
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
            <LinkEditor v-model="item.link" :allow-none="true" />
            <span v-if="item.children?.length" class="hint">已有子项时，这里可设为「无链接」，前台作为展开父节点</span>
          </div>

          <div v-if="item.children?.length" class="children">
            <div v-for="(child, childIndex) in item.children" :key="child.key ?? `child-${index}-${childIndex}`" class="child">
              <el-switch v-model="child.enabled" />
              <el-input v-model="child.label" maxlength="20" placeholder="子项名称" style="width: 150px" />
              <LinkEditor v-model="child.link" :allow-none="false" />
              <el-button link :disabled="childIndex === 0" @click="move(item.children!, childIndex, -1)">上移</el-button>
              <el-button link :disabled="childIndex === (item.children?.length ?? 0) - 1" @click="move(item.children!, childIndex, 1)">下移</el-button>
              <el-button link type="danger" @click="item.children!.splice(childIndex, 1)">删除</el-button>
            </div>
          </div>
        </div>

        <el-button class="add" @click="addTop">+ 添加导航项</el-button>
      </el-tab-pane>

      <!-- Tab 2: 产品与解决方案下拉大菜单 -->
      <el-tab-pane label="产品/方案 5列全景下拉大菜单" name="mega">
        <el-alert
          type="success"
          :closable="false"
          class="err"
          title="这里专门控制鼠标悬停在顶部「产品」或「解决方案」时展开的 5 列业务全景下拉菜单。您可以自由修改每一列的标题、包含的产品/服务选项、红底徽标（如“首款”）、跳转路径以及演示视频。"
        />

        <div class="mega-builder">
          <div v-for="(col, cIndex) in form.megaColumns" :key="cIndex" class="mega-card">
            <div class="mega-card__head">
              <div class="mega-card__title-box">
                <span class="mega-card__col-num">第 {{ cIndex + 1 }} 列</span>
                <el-input v-model="col.title" placeholder="列标题（如：AI产品）" style="width: 130px" />
              </div>
              <div class="mega-card__ops">
                <el-button link size="small" :disabled="cIndex === 0" @click="moveCol(cIndex, -1)">← 左移</el-button>
                <el-button link size="small" :disabled="cIndex === form.megaColumns.length - 1" @click="moveCol(cIndex, 1)">右移 →</el-button>
                <el-button link size="small" type="danger" @click="removeCol(cIndex)">删除列</el-button>
              </div>
            </div>

            <div class="mega-card__items">
              <div v-for="(sub, sIndex) in col.items" :key="sIndex" class="mega-row">
                <div class="mega-row__main">
                  <span class="mega-row__dot">•</span>
                  <el-input v-model="sub.label" placeholder="产品/选项名称" style="width: 120px" />
                  <el-input v-if="!sub.isVideo" v-model="sub.path" placeholder="跳转路径（如 /products/1）" style="width: 150px" />
                  <el-input v-model="sub.tag" placeholder="角标（如首款）" style="width: 80px" />
                  <el-checkbox v-model="sub.isVideo">▶ 演示视频</el-checkbox>
                  <div class="mega-row__actions">
                    <el-button link size="small" :disabled="sIndex === 0" @click="moveItem(col.items, sIndex, -1)">↑</el-button>
                    <el-button link size="small" :disabled="sIndex === col.items.length - 1" @click="moveItem(col.items, sIndex, 1)">↓</el-button>
                    <el-button link size="small" type="danger" @click="col.items.splice(sIndex, 1)">✕</el-button>
                  </div>
                </div>

                <div v-if="sub.isVideo" class="mega-row__video">
                  <el-input v-model="sub.videoTitle" placeholder="视频标题（如：紫宝盒产品操作演示）" style="width: 220px" />
                  <el-input v-model="sub.videoUrl" placeholder="MP4 视频直链（留空使用默认示例视频）" style="flex: 1" />
                </div>
              </div>

              <el-button class="mega-add-sub-btn" size="small" plain @click="addItem(col)">+ 添加菜单选项</el-button>
            </div>
          </div>
        </div>

        <div class="mega-footer-actions">
          <el-button type="primary" plain @click="addCol" :disabled="form.megaColumns.length >= 6">+ 添加新列（最多6列）</el-button>
          <el-button @click="resetToDefaultMega">重置为系统默认 5 列</el-button>
        </div>

        <div class="mega-cta-card">
          <div class="mega-cta-card__title">下拉菜单底部咨询引导横条</div>
          <div class="mega-cta-card__row">
            <span class="label">提示文字：</span>
            <el-input v-model="form.megaCta.text" placeholder="例如：需要专业场景建议？" style="width: 240px" />
            <span class="label" style="margin-left: 16px">按钮文字：</span>
            <el-input v-model="form.megaCta.linkText" placeholder="例如：联系技术专家" style="width: 160px" />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <template #tips>
      <p><b>主导航：</b>叶子节点必须有有效链接才能发布；父节点无有效子项时整块隐藏。</p>
      <p><b>5列大菜单：</b>在此处修改列标题、产品选项、角标或视频链接，保存并发布后，官网前台下拉菜单即刻全网更新！</p>
    </template>
  </ConfigPanel>
</template>

<style scoped>
.err {
  margin-bottom: 12px;
}

.nav-tabs {
  margin-bottom: 16px;
  background: #fff;
  border-radius: 8px;
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
}

.hint {
  font-size: 12px;
  color: var(--a-text-muted);
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

/* 5列大菜单样式 */
.mega-builder {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.mega-card {
  border: 1px solid var(--a-border);
  border-radius: 8px;
  background: #f8fafc;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.mega-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  background: #edf2f7;
  border-bottom: 1px solid var(--a-border);
}

.mega-card__title-box {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mega-card__col-num {
  font-size: 12px;
  font-weight: 600;
  color: #4a5568;
  background: #e2e8f0;
  padding: 2px 6px;
  border-radius: 4px;
}

.mega-card__ops {
  display: flex;
  gap: 4px;
}

.mega-card__items {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
}

.mega-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 8px;
  border-radius: 6px;
  background: #fff;
  border: 1px solid #e2e8f0;
}

.mega-row__main {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.mega-row__dot {
  color: #6366f1;
  font-weight: bold;
}

.mega-row__actions {
  margin-left: auto;
  display: flex;
  gap: 2px;
}

.mega-row__video {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-left: 16px;
}

.mega-add-sub-btn {
  margin-top: 4px;
  align-self: flex-start;
}

.mega-footer-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.mega-cta-card {
  border: 1px dashed var(--a-border);
  border-radius: 8px;
  padding: 14px 18px;
  background: #fbfcfe;
}

.mega-cta-card__title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #334155;
}

.mega-cta-card__row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
