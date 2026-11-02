<script setup lang="ts">
import { onMounted, reactive } from 'vue'
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

interface NavigationDraft {
  items: NavNode[]
}

const { view, loading, saving, publishing, loadError, load, save, publish } =
  useConfig<NavigationDraft & Record<string, unknown>>('NAVIGATION')

const form = reactive<{ items: NavNode[] }>({ items: [] })

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
      normalize({ label: '联系我们', link: { type: 'PAGE', pageCode: 'CONTACT' }, sortOrder: 30 }),
    ]
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

async function onSave() {
  if (form.items.some((item) => !item.label.trim())) {
    ElMessage.warning('导航名称不能为空')
    return
  }
  if (await save({ items: form.items } as NavigationDraft & Record<string, unknown>)) fill()
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
    title="导航"
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
    <el-alert
      type="info"
      :closable="false"
      class="err"
      title="最多两级，顶层最多 8 项、每项最多 10 个子项。有子项的父节点可以不配链接；叶子节点必须有有效链接才能发布。"
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

    <el-button class="add" @click="addTop">添加导航项</el-button>

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
