<script setup lang="ts">
import { onMounted, reactive } from 'vue'
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
  form.milestones.push({ year: String(new Date().getFullYear()), title: '', description: '' })
}

function addQualification() {
  form.qualifications.push({ name: '', description: '' })
}

async function onSave() {
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

    <el-form label-position="top">
      <el-form-item label="公司简介">
        <el-input v-model="form.intro" type="textarea" :rows="4" maxlength="2000" show-word-limit />
      </el-form-item>
      <el-form-item label="使命 / 我们为什么做这件事">
        <el-input v-model="form.mission" type="textarea" :rows="4" maxlength="2000" show-word-limit />
      </el-form-item>
    </el-form>

    <el-divider content-position="left">发展历程</el-divider>
    <div class="rows">
      <div v-for="(item, index) in form.milestones" :key="`m-${index}`" class="row">
        <el-input v-model="item.year" placeholder="2026" maxlength="4" style="width: 90px" />
        <el-input v-model="item.title" placeholder="里程碑标题" maxlength="50" style="width: 180px" />
        <el-input v-model="item.description" placeholder="一句话说明" maxlength="300" style="flex: 1; min-width: 220px" />
        <el-button link type="danger" @click="form.milestones.splice(index, 1)">删除</el-button>
      </div>
    </div>
    <el-button class="add" @click="addMilestone">添加里程碑</el-button>

    <el-divider content-position="left">资质与认证</el-divider>
    <div class="rows">
      <div v-for="(item, index) in form.qualifications" :key="`q-${index}`" class="row">
        <el-input v-model="item.name" placeholder="名称" maxlength="60" style="width: 200px" />
        <el-input v-model="item.description" placeholder="说明" maxlength="300" style="flex: 1; min-width: 240px" />
        <el-button link type="danger" @click="form.qualifications.splice(index, 1)">删除</el-button>
      </div>
    </div>
    <el-button class="add" @click="addQualification">添加资质</el-button>
  </ConfigPanel>
</template>

<style scoped>
.err {
  margin-bottom: 16px;
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

.add {
  margin-top: 12px;
}
</style>
