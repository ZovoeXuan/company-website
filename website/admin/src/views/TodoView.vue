<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => String(route.meta.title ?? '模块'))
const phase = computed(() => String(route.meta.phase ?? '后续阶段'))
</script>

<template>
  <el-card shadow="never" class="card">
    <template #header>
      <div class="head">
        <span class="title">{{ title }}</span>
        <el-tag size="small" type="info">{{ phase }}接入</el-tag>
      </div>
    </template>

    <p class="desc">
      页面骨架已就绪，业务表单、列表与接口联调将在 {{ phase }} 完成。此处不放置假数据，避免把占位误认为可交付功能。
    </p>

    <el-divider />

    <div class="checklist">
      <p class="checklist-title">该模块完成时需要具备</p>
      <ul>
        <li>数据库字段与 DTO 校验</li>
        <li>Service 业务规则与事务边界</li>
        <li>后台列表、编辑表单与权限控制</li>
        <li>公开接口与官网展示</li>
        <li>接口文档与测试记录</li>
      </ul>
    </div>
  </el-card>
</template>

<style scoped>
.card {
  border-radius: 10px;
}

.head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title {
  font-size: 15px;
  font-weight: 500;
}

.desc {
  font-size: 13px;
  color: var(--a-text-muted);
  line-height: 1.8;
}

.checklist-title {
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 8px;
}

.checklist ul {
  margin: 0;
  padding-left: 18px;
  font-size: 13px;
  color: var(--a-text-muted);
  line-height: 1.9;
}
</style>
