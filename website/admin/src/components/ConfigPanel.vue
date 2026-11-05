<script setup lang="ts">
import { computed } from 'vue'
import { useAuth } from '@/stores/auth'

defineProps<{
  title: string
  status?: string
  draftVersion?: number
  publishedVersion?: number | null
  hasUnpublishedChanges?: boolean
  loading?: boolean
  saving?: boolean
  publishing?: boolean
}>()

const emit = defineEmits<{ save: []; publish: [] }>()
const { current } = useAuth()
const isAdmin = computed(() => current.value?.role === 'ADMIN')
</script>

<template>
  <div v-loading="loading" class="shell">
    <el-card shadow="never" class="main">
      <template #header>
        <div class="head">
          <span>{{ title }}</span>
          <slot name="header-actions" />
        </div>
      </template>
      <slot />
    </el-card>

    <aside class="side">
      <el-card shadow="never">
        <template #header>状态与版本</template>
        <div class="status">
          <p class="row">
            <span>发布状态</span>
            <el-tag size="small" :type="status === 'PUBLISHED' ? 'success' : 'info'">
              {{ status === 'PUBLISHED' ? '已发布' : '未发布' }}
            </el-tag>
          </p>
          <p class="row"><span>草稿版本</span><span>v{{ draftVersion ?? '-' }}</span></p>
          <p class="row"><span>线上版本</span><span>v{{ publishedVersion ?? '-' }}</span></p>
          <p v-if="hasUnpublishedChanges" class="warn">● 有未发布的草稿修改</p>
        </div>

        <div class="actions">
          <el-button type="primary" :loading="saving" @click="emit('save')">保存草稿</el-button>
          <el-button
            v-if="isAdmin"
            type="success"
            :loading="publishing"
            @click="emit('publish')"
          >
            发布上线
          </el-button>
          <p v-else class="role-hint">当前为编辑角色，保存草稿后请由管理员审核发布</p>
        </div>
      </el-card>

      <el-card v-if="$slots.tips" shadow="never" class="tips">
        <template #header>操作说明</template>
        <slot name="tips" />
      </el-card>
    </aside>
  </div>
</template>

<style scoped>
.shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 16px;
  align-items: start;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-weight: 600;
}

.side {
  position: sticky;
  top: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status .row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  padding: 6px 0;
  color: var(--a-text-muted);
}

.warn {
  margin-top: 8px;
  font-size: 12px;
  font-weight: 500;
  color: #b26a00;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 16px;
}

.actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

.role-hint {
  font-size: 12px;
  color: var(--a-text-muted);
  line-height: 1.5;
  margin-top: 4px;
}

.tips {
  font-size: 12px;
  color: var(--a-text-muted);
  line-height: 1.8;
}

@media (max-width: 1100px) {
  .shell {
    grid-template-columns: 1fr;
  }
}
</style>
