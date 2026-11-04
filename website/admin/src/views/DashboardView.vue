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
          <span class="metric-card__label">未发布/草稿内容</span>
          <span class="metric-card__badge metric-card__badge--draft">待发布</span>
        </div>
        <p class="metric-card__value">{{ data?.draftContents ?? 0 }}</p>
        <p class="metric-card__tip">可登录预览，尚未对公众展示</p>
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

    <!-- 常用快捷入口 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-head">
          <span>快捷操作</span>
          <span class="card-head__desc">常用高频内容维护入口</span>
        </div>
      </template>
      <div class="quick-grid">
        <button type="button" class="quick-btn" @click="router.push('/contents/products/new')">
          <span class="quick-btn__icon">📦</span>
          <span class="quick-btn__title">录入产品</span>
          <span class="quick-btn__sub">新建工业硬件/软件能力</span>
        </button>
        <button type="button" class="quick-btn" @click="router.push('/contents/solutions/new')">
          <span class="quick-btn__icon">💡</span>
          <span class="quick-btn__title">新增解决方案</span>
          <span class="quick-btn__sub">发布垂直行业实施路径</span>
        </button>
        <button type="button" class="quick-btn" @click="router.push('/contents/cases/new')">
          <span class="quick-btn__icon">🏆</span>
          <span class="quick-btn__title">添加客户案例</span>
          <span class="quick-btn__sub">沉淀降本增效成果标杆</span>
        </button>
        <button type="button" class="quick-btn" @click="router.push('/contents/news/new')">
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
          <span class="quick-btn__sub">维护 SEO、备案号与 Logo</span>
        </button>
      </div>
    </el-card>

    <!-- 内容分布与状态 -->
    <div class="content-row">
      <el-card shadow="never" class="section-card content-card">
        <template #header>
          <div class="card-head">
            <span>内容分布统计</span>
            <el-button link type="primary" @click="load">刷新</el-button>
          </div>
        </template>
        <el-table :data="contentList" border stripe>
          <el-table-column label="内容类型" min-width="160">
            <template #default="{ row }">
              <span style="margin-right: 8px;">{{ row.icon }}</span>
              <strong>{{ row.label }}</strong>
              <span class="type-code">({{ row.type }})</span>
            </template>
          </el-table-column>
          <el-table-column prop="count" label="条目数量" width="120" align="center">
            <template #default="{ row }">
              <el-tag size="small" type="info">{{ row.count }} 篇</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template #default="{ row }">
              <el-button link type="primary" @click="router.push(row.path)">管理列表</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 双快照发布规范提示 -->
      <el-card shadow="never" class="section-card guide-card">
        <template #header>
          <div class="card-head">
            <span>CMS 双快照规范说明</span>
          </div>
        </template>
        <div class="guide-steps">
          <div class="guide-step">
            <div class="guide-step__num">1</div>
            <div class="guide-step__body">
              <strong>草稿隔离保护</strong>
              <p>录入与修改先落入 draft_json，草稿版本自增，不影响前台正在访问的已发布版本。</p>
            </div>
          </div>
          <div class="guide-step">
            <div class="guide-step__num">2</div>
            <div class="guide-step__body">
              <strong>登录态真实预览</strong>
              <p>在后台点击“预览”可查看草稿完整排版效果，免去盲目发布风险。</p>
            </div>
          </div>
          <div class="guide-step">
            <div class="guide-step__num">3</div>
            <div class="guide-step__body">
              <strong>原子发布上线</strong>
              <p>管理员执行发布，快照原子同步至 published_json，前台官网 5 秒内实时回显。</p>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.welcome-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  background: #ffffff;
  padding: 24px 28px;
  border-radius: 12px;
  border: 1px solid var(--a-border);
  box-shadow: 0 2px 8px rgba(16, 16, 20, 0.02);
}

.welcome-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--a-text);
  margin: 0;
}

.welcome-sub {
  margin: 6px 0 0;
  font-size: 13.5px;
  color: var(--a-text-muted);
}

.welcome-actions {
  display: flex;
  gap: 10px;
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
  padding: 22px 24px;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 2px 8px rgba(16, 16, 20, 0.02);
}

.metric-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(16, 16, 20, 0.06);
  border-color: rgba(91, 75, 232, 0.3);
}

.metric-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.metric-card__label {
  font-size: 13px;
  color: var(--a-text-muted);
  font-weight: 500;
}

.metric-card__badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(16, 16, 20, 0.06);
  color: var(--a-text-muted);
  font-weight: 600;
}

.metric-card__badge--draft {
  background: #eff6ff;
  color: #2563eb;
}

.metric-card__badge--leads {
  background: #fef2f2;
  color: #dc2626;
}

.metric-card__value {
  margin: 0;
  font-size: 34px;
  font-weight: 800;
  color: var(--a-text);
  letter-spacing: -0.02em;
  line-height: 1.1;
}

.metric-card__tip {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--a-text-muted);
}

.metric-card--alert .metric-card__value {
  color: #dc2626;
}

.section-card {
  border-radius: 12px;
  border: 1px solid var(--a-border);
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  font-size: 15px;
}

.card-head__desc {
  font-size: 12px;
  color: var(--a-text-muted);
  font-weight: normal;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 14px;
}

.quick-btn {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 16px 18px;
  background: #fafafc;
  border: 1px solid var(--a-border);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
}

.quick-btn:hover {
  background: #ffffff;
  border-color: var(--a-brand, #5b4be8);
  box-shadow: 0 4px 16px rgba(91, 75, 232, 0.1);
  transform: translateY(-2px);
}

.quick-btn__icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.quick-btn__title {
  font-size: 14px;
  font-weight: 600;
  color: var(--a-text);
}

.quick-btn__sub {
  margin-top: 4px;
  font-size: 12px;
  color: var(--a-text-muted);
}

.content-row {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 20px;
}

@media (max-width: 1024px) {
  .content-row {
    grid-template-columns: 1fr;
  }
}

.type-code {
  font-size: 12px;
  color: var(--a-text-muted);
  margin-left: 4px;
}

.guide-steps {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.guide-step {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.guide-step__num {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: var(--a-brand, #5b4be8);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 13px;
  flex: none;
}

.guide-step__body strong {
  display: block;
  font-size: 14px;
  margin-bottom: 4px;
}

.guide-step__body p {
  margin: 0;
  font-size: 12.5px;
  color: var(--a-text-muted);
  line-height: 1.6;
}
</style>
