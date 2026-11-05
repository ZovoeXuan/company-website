<script setup lang="ts">
import { onMounted, reactive } from 'vue'
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

interface AboutDraft {
  intro: string
  mission: string
  milestones: Milestone[]
  qualifications: Qualification[]
}

const { view, loading, saving, publishing, loadError, load, save, publish } =
  useConfig<AboutDraft & Record<string, unknown>>('ABOUT')

const form = reactive<AboutDraft>({ intro: '', mission: '', milestones: [], qualifications: [] })

function fill() {
  const draft = view.value?.draft
  form.intro = draft?.intro ?? ''
  form.mission = draft?.mission ?? ''
  form.milestones = (draft?.milestones ?? []).map((item) => ({ ...item }))
  form.qualifications = (draft?.qualifications ?? []).map((item) => ({ ...item }))
}

function addMilestone() {
  if (form.milestones.length >= 20) {
    ElMessage.warning('发展历程最多 20 条')
    return
  }
  form.milestones.push({ year: String(new Date().getFullYear()), title: '', description: '' })
}

function addQualification() {
  if (form.qualifications.length >= 20) {
    ElMessage.warning('资质认证最多 20 条')
    return
  }
  form.qualifications.push({ name: '', description: '' })
}

function moveItem<T>(list: T[], index: number, delta: number) {
  const target = index + delta
  if (target < 0 || target >= list.length) return
  const [item] = list.splice(index, 1)
  list.splice(target, 0, item)
}

function validateBeforeSave(): boolean {
  for (let i = 0; i < form.milestones.length; i++) {
    const m = form.milestones[i]
    if (!/^\d{4}$/.test((m.year ?? '').trim())) {
      ElMessage.warning(`发展历程第 ${i + 1} 条年份需为 4 位数字（例如 2026）`)
      return false
    }
    if (!m.title.trim()) {
      ElMessage.warning(`发展历程第 ${i + 1} 条标题不能为空`)
      return false
    }
  }
  for (let i = 0; i < form.qualifications.length; i++) {
    if (!form.qualifications[i].name.trim()) {
      ElMessage.warning(`资质与认证第 ${i + 1} 条名称不能为空`)
      return false
    }
  }
  return true
}

async function onSave() {
  if (!validateBeforeSave()) return
  const draft = {
    ...(view.value?.draft ?? {}),
    intro: form.intro,
    mission: form.mission,
    milestones: form.milestones,
    qualifications: form.qualifications,
  } as AboutDraft & Record<string, unknown>
  if (await save(draft)) fill()
}

async function onPublish() {
  if (!validateBeforeSave()) return
  if (await publish()) fill()
}

onMounted(async () => {
  await load()
  fill()
})
</script>

<template>
  <ConfigPanel
    title="关于我们"
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

    <el-form label-width="100px">
      <el-form-item label="公司简介">
        <el-input v-model="form.intro" type="textarea" :rows="5" maxlength="2000" show-word-limit />
      </el-form-item>
      <el-form-item label="企业使命">
        <el-input v-model="form.mission" type="textarea" :rows="3" maxlength="2000" show-word-limit />
      </el-form-item>
    </el-form>

    <el-divider content-position="left">发展历程（里程碑时间轴）</el-divider>
    <div v-for="(item, index) in form.milestones" :key="`ms-${index}`" class="row">
      <el-input v-model="item.year" maxlength="4" placeholder="年份(如2026)" style="width: 110px" />
      <el-input v-model="item.title" maxlength="50" placeholder="里程碑事件标题" style="width: 220px" />
      <el-input v-model="item.description" maxlength="300" placeholder="详细说明（选填）" style="flex: 1" />
      <el-button link :disabled="index === 0" @click="moveItem(form.milestones, index, -1)">↑ 上移</el-button>
      <el-button link :disabled="index === form.milestones.length - 1" @click="moveItem(form.milestones, index, 1)">
        ↓ 下移
      </el-button>
      <el-button link type="danger" @click="form.milestones.splice(index, 1)">删除</el-button>
    </div>
    <el-button @click="addMilestone">+ 添加里程碑</el-button>

    <el-divider content-position="left">资质与荣誉认证</el-divider>
    <div v-for="(item, index) in form.qualifications" :key="`ql-${index}`" class="row">
      <el-input v-model="item.name" maxlength="60" placeholder="资质/荣誉名称" style="width: 260px" />
      <el-input v-model="item.description" maxlength="300" placeholder="认证机构或说明" style="flex: 1" />
      <el-button link :disabled="index === 0" @click="moveItem(form.qualifications, index, -1)">↑ 上移</el-button>
      <el-button link :disabled="index === form.qualifications.length - 1" @click="moveItem(form.qualifications, index, 1)">
        ↓ 下移
      </el-button>
      <el-button link type="danger" @click="form.qualifications.splice(index, 1)">删除</el-button>
    </div>
    <el-button @click="addQualification">+ 添加资质认证</el-button>

    <template #tips>
      <p>发展历程年份需为 4 位数字（如 2024），前台「关于我们」页支持按年份筛选时间轴节点。</p>
    </template>
  </ConfigPanel>
</template>

<style scoped>
.err {
  margin-bottom: 12px;
}

.row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}
</style>
