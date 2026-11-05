<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuth } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const { current, logout } = useAuth()

const isCollapse = ref(false)
const isAdmin = computed(() => current.value?.role === 'ADMIN')

const breadcrumbs = computed(() => {
  const metaTitle = String(route.meta.title ?? '管理后台')
  const path = route.path

  if (path === '/dashboard') return [{ label: '工作台' }]
  if (path.startsWith('/settings')) {
    return [{ label: '站点管理' }, { label: metaTitle }]
  }
  if (path === '/home') {
    return [{ label: '首页管理' }]
  }
  if (path.startsWith('/contents/categories')) {
    return [{ label: '内容管理' }, { label: '内容分类' }]
  }
  if (path.startsWith('/contents')) {
    const isEdit = path.includes('/edit')
    const isCreate = path.includes('/create') || path.includes('/new')
    const baseListPath = '/' + path.split('/').filter(Boolean).slice(0, 2).join('/')
    const listTitle = String(route.meta.title ?? '内容').replace(/^(新建|编辑)/, '')
    if (isCreate) return [{ label: '内容管理' }, { label: `${listTitle}管理`, to: baseListPath }, { label: '新建' }]
    if (isEdit) return [{ label: '内容管理' }, { label: `${listTitle}管理`, to: baseListPath }, { label: '编辑' }]
    return [{ label: '内容管理' }, { label: metaTitle }]
  }
  if (path.startsWith('/pages')) {
    return [{ label: '单页管理' }, { label: metaTitle }]
  }
  if (path === '/media') {
    return [{ label: '素材管理' }]
  }
  if (path === '/leads') {
    return [{ label: '咨询管理' }]
  }
  if (path.startsWith('/system')) {
    return [{ label: '系统管理' }, { label: metaTitle }]
  }
  return [{ label: metaTitle }]
})

function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

function handleResize() {
  if (window.innerWidth < 1024) {
    isCollapse.value = true
  }
}

function openVisualStudio() {
  router.push('/preview/home')
}

function openLiveSite() {
  window.open('/', '_blank', 'noopener')
}

async function handleLogout() {
  try {
    await logout()
  } finally {
    await router.replace('/login')
  }
}

onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<template>
  <el-container class="shell">
    <el-aside :width="isCollapse ? '64px' : '224px'" class="aside">
      <div class="brand">
        <span class="logo">G</span>
        <span v-show="!isCollapse" class="brand-name">官网管理后台</span>
      </div>

      <el-menu
        :default-active="route.path"
        :collapse="isCollapse"
        router
        class="menu"
        :collapse-transition="false"
      >
        <el-menu-item index="/dashboard">
          <el-icon>
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="7" height="7"></rect>
              <rect x="14" y="3" width="7" height="7"></rect>
              <rect x="14" y="14" width="7" height="7"></rect>
              <rect x="3" y="14" width="7" height="7"></rect>
            </svg>
          </el-icon>
          <template #title>工作台</template>
        </el-menu-item>

        <el-menu-item index="/preview/home">
          <el-icon>
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 20h9"></path>
              <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path>
            </svg>
          </el-icon>
          <template #title>实景预演编辑台</template>
        </el-menu-item>

        <el-sub-menu index="site">
          <template #title>
            <el-icon>
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="2" y1="12" x2="22" y2="12"></line>
                <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path>
              </svg>
            </el-icon>
            <span>站点管理</span>
          </template>
          <el-menu-item index="/settings/site">全站设置</el-menu-item>
          <el-menu-item index="/settings/navigation">导航与页脚</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/home">
          <el-icon>
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
              <polyline points="9 22 9 12 15 12 15 22"></polyline>
            </svg>
          </el-icon>
          <template #title>首页管理</template>
        </el-menu-item>

        <el-sub-menu index="contents">
          <template #title>
            <el-icon>
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                <polyline points="14 2 14 8 20 8"></polyline>
                <line x1="16" y1="13" x2="8" y2="13"></line>
                <line x1="16" y1="17" x2="8" y2="17"></line>
                <polyline points="10 9 9 9 8 9"></polyline>
              </svg>
            </el-icon>
            <span>内容管理</span>
          </template>
          <el-menu-item index="/contents/products">产品</el-menu-item>
          <el-menu-item index="/contents/solutions">解决方案</el-menu-item>
          <el-menu-item index="/contents/cases">客户案例</el-menu-item>
          <el-menu-item index="/contents/news">新闻</el-menu-item>
          <el-menu-item index="/contents/customers">客户标识</el-menu-item>
          <el-menu-item index="/contents/categories">内容分类</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="pages">
          <template #title>
            <el-icon>
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
                <line x1="8" y1="21" x2="16" y2="21"></line>
                <line x1="12" y1="17" x2="12" y2="21"></line>
              </svg>
            </el-icon>
            <span>单页管理</span>
          </template>
          <el-menu-item index="/pages/about">关于我们</el-menu-item>
          <el-menu-item index="/pages/contact">联系我们</el-menu-item>
          <el-menu-item index="/pages/privacy">隐私说明</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/media">
          <el-icon>
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
              <circle cx="8.5" cy="8.5" r="1.5"></circle>
              <polyline points="21 15 16 10 5 21"></polyline>
            </svg>
          </el-icon>
          <template #title>素材管理</template>
        </el-menu-item>

        <el-menu-item v-if="isAdmin" index="/leads">
          <el-icon>
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
            </svg>
          </el-icon>
          <template #title>咨询管理</template>
        </el-menu-item>

        <el-sub-menu v-if="isAdmin" index="system">
          <template #title>
            <el-icon>
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="3"></circle>
                <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
              </svg>
            </el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/users">账号管理</el-menu-item>
          <el-menu-item index="/system/audit-logs">操作日志</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container class="content-container">
      <el-header height="60px" class="header">
        <div class="header-left">
          <button
            type="button"
            class="collapse-btn"
            :title="isCollapse ? '展开菜单' : '收起菜单'"
            @click="toggleCollapse"
          >
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="3" y1="12" x2="21" y2="12"></line>
              <line x1="3" y1="6" x2="21" y2="6"></line>
              <line x1="3" y1="18" x2="21" y2="18"></line>
            </svg>
          </button>

          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item v-for="(b, idx) in breadcrumbs" :key="b.label" :to="b.to">
              <span :class="{ 'is-active': idx === breadcrumbs.length - 1 }">{{ b.label }}</span>
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <button
            type="button"
            class="btn-studio"
            title="进入所见即所得的全站实景交互预演编辑台"
            @click="openVisualStudio"
          >
            <span>🎨 实景预演交互编辑</span>
          </button>

          <button type="button" class="btn-live-site" title="在新标签页中查看官网前台效果" @click="openLiveSite">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path>
              <polyline points="15 3 21 3 21 9"></polyline>
              <line x1="10" y1="14" x2="21" y2="3"></line>
            </svg>
            <span>查看前台官网</span>
          </button>

          <div class="user-profile">
            <div class="user-avatar" :class="{ 'user-avatar--admin': isAdmin }">
              {{ (current?.displayName || current?.username || 'U').slice(0, 1).toUpperCase() }}
            </div>
            <div class="user-info">
              <span class="user-name">{{ current?.displayName || current?.username }}</span>
              <span class="user-role-badge" :class="isAdmin ? 'badge--admin' : 'badge--editor'">
                {{ isAdmin ? '系统管理员' : '内容编辑' }}
              </span>
            </div>
          </div>

          <div class="header-divider"></div>

          <RouterLink to="/profile/password" class="icon-btn" title="修改密码">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
            </svg>
          </RouterLink>

          <button type="button" class="icon-btn icon-btn--logout" title="退出登录" @click="handleLogout">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
              <polyline points="16 17 21 12 16 7"></polyline>
              <line x1="21" y1="12" x2="9" y2="12"></line>
            </svg>
          </button>
        </div>
      </el-header>

      <el-main class="main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.shell {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: var(--a-bg, #f5f7fa);
}

.aside {
  background: #ffffff;
  border-right: 1px solid var(--a-border);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  transition: width 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 2px 0 8px rgba(16, 16, 20, 0.02);
  z-index: 10;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 60px;
  padding: 0 18px;
  border-bottom: 1px solid var(--a-border);
  flex: none;
}

.logo {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: linear-gradient(135deg, #7a6bff, #5b4be8);
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 700;
  box-shadow: 0 4px 14px rgba(91, 75, 232, 0.3);
  flex: none;
}

.brand-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--a-text);
  white-space: nowrap;
}

.menu {
  border-right: none;
  padding: 8px 4px;
  flex: 1;
}

.content-container {
  min-width: 0;
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  border-bottom: 1px solid var(--a-border);
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(16, 16, 20, 0.02);
  flex: none;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  border: none;
  background: transparent;
  color: var(--a-text-muted);
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.breadcrumb {
  font-size: 13.5px;
}

.breadcrumb .is-active {
  color: var(--a-text);
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.btn-studio {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 6px;
  border: none;
  background: linear-gradient(135deg, #6d5ce8 0%, #5b4be8 100%);
  color: #ffffff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(91, 75, 232, 0.28);
  transition: all 0.2s ease;
}

.btn-studio:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(91, 75, 232, 0.4);
}

.btn-live-site {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 6px;
  border: 1px solid rgba(91, 75, 232, 0.25);
  background: rgba(91, 75, 232, 0.06);
  color: var(--a-brand, #5b4be8);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-live-site:hover {
  background: var(--a-brand, #5b4be8);
  color: #ffffff;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 8px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #3b82f6;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.user-avatar--admin {
  background: linear-gradient(135deg, #ef4444, #dc2626);
}

.user-info {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}

.user-name {
  font-size: 13px;
  font-weight: 600;
}

.user-role-badge {
  font-size: 11px;
  border-radius: 3px;
  padding: 1px 4px;
  font-weight: 500;
}

.badge--admin {
  color: #dc2626;
  background: #fef2f2;
}

.badge--editor {
  color: #2563eb;
  background: #eff6ff;
}

.header-divider {
  width: 1px;
  height: 20px;
  background: var(--a-border);
}

.icon-btn {
  border: none;
  background: transparent;
  color: var(--a-text-muted);
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-btn--logout:hover {
  background: #fef2f2;
  color: #ef4444;
}

.main {
  padding: 20px 24px 32px;
  overflow-y: auto;
  flex: 1;
}
</style>
