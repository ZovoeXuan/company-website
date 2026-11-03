<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { get } from '@/api/http'

interface DashboardData {
  publishedContents: number
  draftContents: number
  contentsByType: Record<string, number>
  pendingLeads?: number
  userCount?: number
}

const data = ref<DashboardData | null>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    data.value = await get<DashboardData>('/api/admin/dashboard')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '工作台加载失败')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div v-loading="loading" class="dashboard">
    <div class="metrics">
      <div class="metric">
        <p class="label">已发布内容</p>
        <p class="value">{{ data?.publishedContents ?? 0 }}</p>
      </div>
      <div class="metric">
        <p class="label">未发布草稿</p>
        <p class="value">{{ data?.draftContents ?? 0 }}</p>
      </div>
      <div v-if="data?.pendingLeads !== undefined" class="metric">
        <p class="label">待处理咨询</p>
        <p class="value">{{ data.pendingLeads }}</p>
      </div>
      <div v-if="data?.userCount !== undefined" class="metric">
        <p class="label">后台账号</p>
        <p class="value">{{ data.userCount }}</p>
      </div>
    </div>

    <el-card shadow="never" class="card">
      <template #header>内容分布</template>
      <el-empty v-if="!data || Object.keys(data.contentsByType).length === 0" description="暂无内容，阶段 3 开始录入产品" />
      <el-table v-else :data="Object.entries(data.contentsByType).map(([type, count]) => ({ type, count }))">
        <el-table-column prop="type" label="类型" />
        <el-table-column prop="count" label="数量" width="120" />
      </el-table>
    </el-card>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="当前进度"
      description="内容、站点配置与首页编排已完成；咨询管理已接入官网表单，可查看并按状态处理线索。"
      class="hint"
    />
  </div>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
}

.metric {
  background: #fff;
  border: 1px solid var(--a-border);
  border-radius: 10px;
  padding: 16px;
}

.label {
  font-size: 12px;
  color: var(--a-text-muted);
  margin-bottom: 6px;
}

.value {
  font-size: 26px;
  font-weight: 500;
}

.card {
  border-radius: 10px;
}

.hint {
  margin-top: 4px;
}
</style>
