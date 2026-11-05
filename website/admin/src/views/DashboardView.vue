<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { get } from '@/api/http'
import { useAuth } from '@/stores/auth'

interface DashboardData {
  publishedContents: number
  draftContents: number
  contentsByType: Record<string, number>
  pendingLeads?: number
  userCount?: number
}

const router = useRouter()
const { current } = useAuth()
const isAdmin = computed(() => current.value?.role === 'ADMIN')

const data = ref<DashboardData | null>(null)
const loading = ref(true)

const typeLabels: Record<string, { label: string; path: string; icon: string }> = {
  PRODUCT: { label: '产品中心', path: '/contents/products', icon: '📦' },
  SOLUTION: { label: '解决方案', path: '/contents/solutions', icon: '💡' },
  CASE: { label: '客户案例', path: '/contents/cases', icon: '🏆' },
  NEWS: { label: '新闻动态', path: '/contents/news', icon: '📰' },
  CUSTOMER: { label: '客户标识', path: '/contents/customers', icon: '🤝' },
}

const contentList = computed(() => {
  if (!data.value?.contentsByType) return []
  return Object.entries(data.value.contentsByType).map(([type, count]) => {
    const meta = typeLabels[type] || { label: type, path: '/contents/products', icon: '📄' }
    return {
      type,
      label: meta.label,
      count,
      path: meta.path,
      icon: meta.icon,
    }
  })
})

async function load() {
  loading.value = true
  try {
    data.value = await get<DashboardData>('/api/admin/dashboard')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '工作台加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="dashboard">
    <!-- 欢迎栏 -->
    <div class="welcome-bar">
      <div>
        <h2 class="welcome-title">您好，{{ current?.displayName || current?.username }}！</h2>
        <p class="welcome-sub">
          欢迎使用官网内容管理控制台。您当前以【{{ isAdmin ? '系统管理员' : '内容编辑' }}】身份登录。
        </p>
      </div>
      <div class="welcome-actions">
        <el-button type="primary" @click="router.push('/home')">编排首页</el-button>
        <el-button @click="router.push('/preview/home')">首页草稿预览</el-button>
        <el-button v-if="isAdmin" type="warning" @click="router.push('/leads')">查看咨询线索</el-button>
      </div>
    </div>

    <!-- 核心指标卡片 -->
    <div class="metrics-grid">
      <div class="metric-card metric-card--published" @click="router.push('/contents/products')">
        <div class="metric-card__header">
          <span class="metric-card__label">已发布线上内容</span>
          <span class="metric-card__badge">生效中</span>
        </div>
        <p class="metric-card__value">{{ data?.publishedContents ?? 0 }}</p>
        <p class="metric-card__tip">实时向官网前端公开接口输出</p>
      </div>

      <div class="metric-card metric-card--draft" @click="router.push('/contents/products')">
        <div class="metric-card__header">
          <span class="metric-card__label">待发布/草稿变更</span>
          <span class="metric-card__badge metric-card__badge--draft">待发布</span>
        </div>
        <p class="metric-card__value">{{ data?.draftContents ?? 0 }}</p>
        <p class="metric-card__tip">包含未发布草稿及有未发布修改的内容</p>
      </div>

      <div
        v-if="isAdmin"
        class="metric-card metric-card--leads"
        :class="{ 'metric-card--alert': (data?.pendingLeads ?? 0) > 0 }"
        @click="router.push('/leads')"
      >
        <div class="metric-card__header">
          <span class="metric-card__label">待处理客户咨询</span>
          <span class="metric-card__badge metric-card__badge--leads">NEW</span>
        </div>
        <p class="metric-card__value">{{ data?.pendingLeads ?? 0 }}</p>
        <p class="metric-card__tip">需在 4 小时工作时间内跟进与回复</p>
      </div>

      <div v-if="isAdmin" class="metric-card" @click="router.push('/system/users')">
        <div class="metric-card__header">
          <span class="metric-card__label">后台系统账号</span>
          <span class="metric-card__badge">安全受控</span>
        </div>
        <p class="metric-card__value">{{ data?.userCount ?? 0 }}</p>
        <p class="metric-card__tip">管理员与编辑双角色权限控制</p>
      </div>
    </div>

    <!-- 常用快捷入口（已修复 /create 路由匹配） -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-head">
          <span>快捷操作</span>
          <span class="card-head__desc">常用高频内容维护入口</span>
        </div>
      </template>
      <div class="quick-grid">
        <button type="button" class="quick-btn" @click="router.push('/contents/products/create')">
          <span class="quick-btn__icon">📦</span>
          <span class="quick-btn__title">录入产品</span>
          <span class="quick-btn__sub">新建工业硬件/软件能力</span>
        </button>
        <button type="button" class="quick-btn" @click="router.push('/contents/solutions/create')">
          <span class="quick-btn__icon">💡</span>
          <span class="quick-btn__title">新增解决方案</span>
          <span class="quick-btn__sub">发布垂直行业实施路径</span>
        </button>
        <button type="button" class="quick-btn" @click="router.push('/contents/cases/create')">
          <span class="quick-btn__icon">🏆</span>
          <span class="quick-btn__title">添加客户案例</span>
          <span class="quick-btn__sub">沉淀降本增效成果标杆</span>
        </button>
        <button type="button" class="quick-btn" @click="router.push('/contents/news/create')">
          <span class="quick-btn__icon">📰</span>
          <span class="quick-btn__title">发布新闻</span>
          <span class="quick-btn__sub">企业合并/签约/展会动态</span>
        </button>
        <button type="button" class="quick-btn" @click="router.push('/media')">
          <span class="quick-btn__icon">🖼️</span>
          <span class="quick-btn__title">素材库</span>
          <span class="quick-btn__sub">上传图片与视频媒体文件</span>
        </button>
        <button type="button" class="quick-btn" @click="router.push('/settings/site')">
          <span class="quick-btn__icon">⚙️</span>
          <span class="quick-btn__title">全站设置</span>
          <span class="quick-btn__sub">品牌 Logo、SEO 与备案信息</span>
        </button>
      </div>
    </el-card>

    <!-- 内容模块分布 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-head">
          <span>内容模块概览</span>
          <span class="card-head__desc">点击对应模块进入列表维护</span>
        </div>
      </template>
      <div class="type-grid">
        <div
          v-for="item in contentList"
          :key="item.type"
          class="type-item"
          @click="router.push(item.path)"
        >
          <span class="type-item__icon">{{ item.icon }}</span>
          <div class="type-item__info">
            <span class="type-item__label">{{ item.label }}</span>
            <span class="type-item__count">共 {{ item.count }} 条记录</span>
          </div>
          <span class="type-item__arrow">→</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.welcome-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 22px 26px;
  border-radius: 12px;
  background: linear-gradient(135deg, #ffffff 0%, #f5f3ff 100%);
  border: 1px solid rgba(91, 75, 232, 0.14);
  flex-wrap: wrap;
}

.welcome-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--a-text);
  margin: 0 0 6px;
}

.welcome-sub {
  font-size: 13px;
  color: var(--a-text-muted);
  margin: 0;
}

.welcome-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.metric-card {
  background: #ffffff;
  border: 1px solid var(--a-border);
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 28px rgba(16, 16, 20, 0.06);
  border-color: rgba(91, 75, 232, 0.35);
}

.metric-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.metric-card__label {
  font-size: 13px;
  font-weight: 600;
  color: var(--a-text-muted);
}

.metric-card__badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 999px;
  background: #ecfdf5;
  color: #059669;
  font-weight: 600;
}

.metric-card__badge--draft {
  background: #fffbeb;
  color: #d97706;
}

.metric-card__badge--leads {
  background: #fef2f2;
  color: #dc2626;
}

.metric-card__value {
  font-size: 32px;
  font-weight: 700;
  color: var(--a-text);
  margin: 0 0 6px;
}

.metric-card__tip {
  font-size: 12px;
  color: var(--a-text-muted);
  margin: 0;
}

.section-card {
  border-radius: 12px;
}

.card-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  font-weight: 600;
}

.card-head__desc {
  font-size: 12px;
  font-weight: 400;
  color: var(--a-text-muted);
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.quick-btn {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  padding: 16px;
  border-radius: 10px;
  border: 1px solid var(--a-border);
  background: #fafbfc;
  cursor: pointer;
  text-align: left;
  font-family: inherit;
  transition: all 0.2s ease;
}

.quick-btn:hover {
  background: #f5f3ff;
  border-color: var(--a-brand);
}

.quick-btn__icon {
  font-size: 22px;
}

.quick-btn__title {
  font-size: 14px;
  font-weight: 600;
  color: var(--a-text);
}

.quick-btn__sub {
  font-size: 12px;
  color: var(--a-text-muted);
}

.type-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

.type-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid var(--a-border);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.type-item:hover {
  border-color: var(--a-brand);
  background: #faf9ff;
}

.type-item__icon {
  font-size: 22px;
}

.type-item__info {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.type-item__label {
  font-size: 14px;
  font-weight: 600;
}

.type-item__count {
  font-size: 12px;
  color: var(--a-text-muted);
}

.type-item__arrow {
  color: var(--a-brand);
  font-weight: 600;
}
</style>
