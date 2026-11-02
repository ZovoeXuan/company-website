<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import ConfigPanel from '@/components/ConfigPanel.vue'
import { useConfig } from '@/composables/useConfig'

interface LinkItem {
  label: string
  path: string
  visible: boolean
}

interface NavigationDraft {
  items: LinkItem[]
  footerLinks: LinkItem[]
}

const { view, loading, saving, publishing, loadError, load, save, publish } =
  useConfig<NavigationDraft & Record<string, unknown>>('NAVIGATION')

const form = reactive<{ items: LinkItem[]; footerLinks: LinkItem[] }>({ items: [], footerLinks: [] })

function fill() {
  const draft = view.value?.draft
  form.items = (draft?.items ?? []).map((item) => ({ ...item }))
  form.footerLinks = (draft?.footerLinks ?? []).map((item) => ({ ...item }))
  if (form.items.length === 0) {
    form.items = [{ label: '首页', path: '/', visible: true }]
  }
}

function add(list: LinkItem[]) {
  if (list.length >= 12) {
    ElMessage.warning('最多 12 项')
    return
  }
  list.push({ label: '', path: '/', visible: true })
}

function move(list: LinkItem[], index: number, delta: number) {
  const target = index + delta
  if (target < 0 || target >= list.length) return
  const [item] = list.splice(index, 1)
  list.splice(target, 0, item)
}

function remove(list: LinkItem[], index: number) {
  list.splice(index, 1)
}

async function onSave() {
  if (form.items.some((item) => !item.label.trim())) {
    ElMessage.warning('导航名称不能为空')
    return
  }
  const draft = {
    ...(view.value?.draft ?? {}),
    items: form.items.map((item) => ({ ...item, label: item.label.trim(), path: item.path.trim() })),
    footerLinks: form.footerLinks.map((item) => ({ ...item, label: item.label.trim(), path: item.path.trim() })),
  } as NavigationDraft & Record<string, unknown>
  if (await save(draft)) fill()
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
    title="导航与页脚链接"
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

    <h3 class="section-title">主导航</h3>
    <p class="hint">只接受站内路径，必须以 / 开头；关闭「显示」后该项不出现在前台，但保留配置。</p>

    <div class="rows">
      <div v-for="(item, index) in form.items" :key="`nav-${index}`" class="row">
        <el-input v-model="item.label" placeholder="名称" maxlength="12" style="width: 140px" />
        <el-input v-model="item.path" placeholder="/products" maxlength="120" style="width: 240px" />
        <el-switch v-model="item.visible" active-text="显示" />
        <div class="row-actions">
          <el-button link :disabled="index === 0" @click="move(form.items, index, -1)">上移</el-button>
          <el-button link :disabled="index === form.items.length - 1" @click="move(form.items, index, 1)">下移</el-button>
          <el-button link type="danger" @click="remove(form.items, index)">删除</el-button>
        </div>
      </div>
    </div>
    <el-button class="add" @click="add(form.items)">添加导航项</el-button>

    <el-divider content-position="left">页脚链接</el-divider>
    <div class="rows">
      <div v-for="(item, index) in form.footerLinks" :key="`footer-${index}`" class="row">
        <el-input v-model="item.label" placeholder="名称" maxlength="12" style="width: 140px" />
        <el-input v-model="item.path" placeholder="/privacy" maxlength="120" style="width: 240px" />
        <el-switch v-model="item.visible" active-text="显示" />
        <div class="row-actions">
          <el-button link type="danger" @click="remove(form.footerLinks, index)">删除</el-button>
        </div>
      </div>
    </div>
    <el-button class="add" @click="add(form.footerLinks)">添加页脚链接</el-button>
  </ConfigPanel>
</template>

<style scoped>
.err {
  margin-bottom: 16px;
}

.section-title {
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 6px;
}

.hint {
  font-size: 12px;
  color: var(--a-text-muted);
  margin-bottom: 14px;
  line-height: 1.7;
}

.rows {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.row-actions {
  display: flex;
  gap: 2px;
}

.add {
  margin-top: 12px;
}
</style>
