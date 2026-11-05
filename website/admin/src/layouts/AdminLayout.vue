<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuth } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const { current, logout } = useAuth()

const isCollapse = ref(false)
const isAdmin = computed(() => current.value?.role === 'ADMIN')

// ==================== 全局 ⌘K / Ctrl+K 命令面板 ====================
interface CmdAction {
  id: string
  title: string
  category: string
  path: string
  keywords: string
  adminOnly?: boolean
}

const ALL_COMMANDS: CmdAction[] = [
  { id: 'studio', title: '🎨 打开全站实景交互预演工作台 (Visual Live Studio)', category: '核心推荐', path: '/preview/home', keywords: 'preview studio 预演 可视化 画布 首页' },
  { id: 'dash', title: '📊 工作台概览 (Dashboard)', category: '导航跳转', path: '/dashboard', keywords: 'dashboard 工作台 统计' },
  { id: 'home', title: '🏠 首页模块编排管理 (Home)', category: '站点配置', path: '/home', keywords: 'home 首页 轮播 指标' },
  { id: 'site', title: '⚙️ 全站设置与 SEO / 页脚备案 (Site)', category: '站点配置', path: '/settings/site', keywords: 'site seo logo icp 全站 页脚 二维码' },
  { id: 'nav', title: '🧭 顶部导航与五列全景菜单 (Navigation)', category: '站点配置', path: '/settings/navigation', keywords: 'nav mega 导航 菜单 全景' },
  { id: 'prod-list', title: '📦 产品管理列表', category: '内容中心', path: '/contents/products', keywords: 'products 产品 列表' },
  { id: 'prod-new', title: '➕ 新建产品 (Create Product)', category: '快捷新建', path: '/contents/products/create', keywords: 'create new product 新建产品' },
  { id: 'sol-list', title: '💡 解决方案管理列表', category: '内容中心', path: '/contents/solutions', keywords: 'solutions 解决方案 列表' },
  { id: 'sol-new', title: '➕ 新建解决方案 (Create Solution)', category: '快捷新建', path: '/contents/solutions/create', keywords: 'create new solution 新建方案' },
  { id: 'case-list', title: '🏆 客户案例管理列表', category: '内容中心', path: '/contents/cases', keywords: 'cases 客户案例 列表' },
  { id: 'case-new', title: '➕ 新建客户案例 (Create Case)', category: '快捷新建', path: '/contents/cases/create', keywords: 'create new case 新建案例' },
  { id: 'news-list', title: '📰 新闻动态管理列表', category: '内容中心', path: '/contents/news', keywords: 'news 新闻 动态 列表' },
  { id: 'news-new', title: '➕ 发布新闻动态 (Create News)', category: '快捷新建', path: '/contents/news/create', keywords: 'create new news 新建新闻' },
  { id: 'cust-list', title: '🤝 合作客户标识墙管理', category: '内容中心', path: '/contents/customers', keywords: 'customers 客户标识 logo' },
  { id: 'cat', title: '🏷️ 内容分类管理', category: '内容中心', path: '/contents/categories', keywords: 'categories 分类' },
  { id: 'media', title: '🖼️ 素材库管理（图片与视频）', category: '资源与单页', path: '/media', keywords: 'media 素材 图片 视频 上传' },
  { id: 'about', title: '🏢 关于我们单页配置', category: '资源与单页', path: '/pages/about', keywords: 'about 关于我们 里程碑 资质' },
  { id: 'contact', title: '📞 联系我们与咨询开关配置', category: '资源与单页', path: '/pages/contact', keywords: 'contact 联系我们 电话 邮箱 表单开关' },
  { id: 'privacy', title: '🛡️ 隐私说明正文配置', category: '资源与单页', path: '/pages/privacy', keywords: 'privacy 隐私说明 协议' },
  { id: 'leads', title: '📬 客户咨询线索看板与跟进 (Leads)', category: '运营与安全', path: '/leads', keywords: 'leads 咨询 线索 客户 看板', adminOnly: true },
  { id: 'users', title: '👥 后台系统账号与角色管理', category: '运营与安全', path: '/system/users', keywords: 'users 账号 管理员 编辑 密码', adminOnly: true },
  { id: 'audit', title: '📜 系统操作审计日志', category: '运营与安全', path: '/system/audit-logs', keywords: 'audit logs 审计 日志', adminOnly: true },
  { id: 'pwd', title: '🔑 修改个人登录密码', category: '个人中心', path: '/profile/password', keywords: 'password 修改密码' },
]

const cmdOpen = ref(false)
const cmdQuery = ref('')
const cmdActiveIdx = ref(0)
const cmdInputRef = ref<HTMLInputElement | null>(null)

const filteredCommands = computed(() => {
  const q = cmdQuery.value.trim().toLowerCase()
  return ALL_COMMANDS.filter((item) => {
    if (item.adminOnly && !isAdmin.value) return false
    if (!q) return true
    return (
      item.title.toLowerCase().includes(q) ||
      item.category.toLowerCase().includes(q) ||
      item.keywords.toLowerCase().includes(q)
    )
  })
})

function openCommandPalette() {
  cmdOpen.value = true
  cmdQuery.value = ''
  cmdActiveIdx.value = 0
  window.setTimeout(() => {
    cmdInputRef.value?.focus()
  }, 50)
}

function runCommand(cmd: CmdAction) {
  cmdOpen.value = false
  router.push(cmd.path)
}

function handleKeydown(e: KeyboardEvent) {
  if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
    e.preventDefault()
    if (cmdOpen.value) cmdOpen.value = false
    else openCommandPalette()
    return
  }
  if (!cmdOpen.value) return
  if (e.key === 'Escape') {
    cmdOpen.value = false
  } else if (e.key === 'ArrowDown' && filteredCommands.value.length) {
    e.preventDefault()
    cmdActiveIdx.value = (cmdActiveIdx.value + 1) % filteredCommands.value.length
  } else if (e.key === 'ArrowUp' && filteredCommands.value.length) {
    e.preventDefault()
    cmdActiveIdx.value = (cmdActiveIdx.value - 1 + filteredCommands.value.length) % filteredCommands.value.length
  } else if (e.key === 'Enter' && filteredCommands.value[cmdActiveIdx.value]) {
    e.preventDefault()
    runCommand(filteredCommands.value[cmdActiveIdx.value])
  }
}

const breadcrumbs = computed(() => {
  const items: Array<{ title: string; path?: string }> = [{ title: '首页', path: '/dashboard' }]
  if (route.path === '/dashboard') return items

  const path = route.path
  if (path.startsWith('/settings') || path === '/home') {
    items.push({ title: '站点管理' })
  } else if (path.startsWith('/contents')) {
    items.push({ title: '内容管理' })
    const isEdit = path.includes('/edit')
    const isCreate = path.includes('/create') || path.includes('/new')
    if (isEdit || isCreate) {
      const parts = path.split('/')
      const type = parts[2]
      const typeMap: Record<string, string> = {
        products: '产品',
        solutions: '解决方案',
        cases: '客户案例',
        news: '新闻',
        customers: '客户标识',
      }
      if (type && typeMap[type]) {
        items.push({ title: typeMap[type], path: `/contents/${type}` })
      }
    }
  } else if (path.startsWith('/pages')) {
    items.push({ title: '单页管理' })
  } else if (path.startsWith('/system')) {
    items.push({ title: '系统管理' })
  }

  if (route.meta.title) {
    items.push({ title: String(route.meta.title) })
  }
  return items
})

function handleResize() {
  isCollapse.value = window.innerWidth < 1024
}

onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('keydown', handleKeydown)
})

async function onLogout() {
  await logout()
  await router.replace('/login')
}
</script>

<template>
  <el-container class="shell">
    <el-aside :width="isCollapse ? '64px' : '240px'" class="aside">
      <div class="brand" :class="{ 'is-collapsed': isCollapse }">
        <span class="brand__logo">G</span>
        <div v-show="!isCollapse" class="brand__text">
          <div class="brand__title">官网管理后台</div>
          <div class="brand__sub">CMS Console</div>
        </div>
      </div>

      <el-scrollbar class="menu-scroll">
        <el-menu
          :default-active="route.path"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
          class="menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon>
              <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                <rect x="3" y="3" width="7" height="7"></rect>
                <rect x="14" y="3" width="7" height="7"></rect>
                <rect x="14" y="14" width="7" height="7"></rect>
                <rect x="3" y="14" width="7" height="7"></rect>
              </svg>
            </el-icon>
            <template #title>工作台</template>
          </el-menu-item>

          <el-menu-item index="/preview/home" class="menu-item--studio">
            <el-icon>
              <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                <path d="M12 20h9"></path>
                <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path>
              </svg>
            </el-icon>
            <template #title>🎨 实景预演交互编辑</template>
          </el-menu-item>

          <el-sub-menu index="site">
            <template #title>
              <el-icon>
                <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                  <circle cx="12" cy="12" r="3"></circle>
                  <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
                </svg>
              </el-icon>
              <span>站点管理</span>
            </template>
            <el-menu-item index="/settings/site">全站设置</el-menu-item>
            <el-menu-item index="/settings/navigation">导航与全景菜单</el-menu-item>
            <el-menu-item index="/home">首页管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="contents">
            <template #title>
              <el-icon>
                <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                  <polyline points="14 2 14 8 20 8"></polyline>
                  <line x1="16" y1="13" x2="8" y2="13"></line>
                  <line x1="16" y1="17" x2="8" y2="17"></line>
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
                <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                  <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                  <line x1="3" y1="9" x2="21" y2="9"></line>
                  <line x1="9" y1="21" x2="9" y2="9"></line>
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
              <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                <circle cx="8.5" cy="8.5" r="1.5"></circle>
                <polyline points="21 15 16 10 5 21"></polyline>
              </svg>
            </el-icon>
            <template #title>素材管理</template>
          </el-menu-item>

          <el-menu-item v-if="isAdmin" index="/leads">
            <el-icon>
              <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
              </svg>
            </el-icon>
            <template #title>咨询管理</template>
          </el-menu-item>

          <el-sub-menu v-if="isAdmin" index="system">
            <template #title>
              <el-icon>
                <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                  <circle cx="9" cy="7" r="4"></circle>
                </svg>
              </el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/system/users">账号管理</el-menu-item>
            <el-menu-item index="/system/audit-logs">操作日志</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container class="main-container">
      <el-header class="header" height="60px">
        <div class="header__left">
          <button class="collapse-btn" :title="isCollapse ? '展开菜单' : '收起菜单'" @click="isCollapse = !isCollapse">
            <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" stroke-width="2" fill="none">
              <line x1="3" y1="12" x2="21" y2="12"></line>
              <line x1="3" y1="6" x2="21" y2="6"></line>
              <line x1="3" y1="18" x2="21" y2="18"></line>
            </svg>
          </button>

          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item
              v-for="(item, index) in breadcrumbs"
              :key="index"
              :to="item.path ? { path: item.path } : undefined"
            >
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header__right">
          <!-- ⌘K 快捷指令入口 -->
          <button type="button" class="cmd-btn" title="全局快捷命令面板 (Ctrl+K / ⌘K)" @click="openCommandPalette">
            <span>🔍 快捷指令</span>
            <kbd>⌘K</kbd>
          </button>

          <button type="button" class="studio-launch-btn" @click="router.push('/preview/home')">
            🎨 实景交互预演台
          </button>

          <a href="/" target="_blank" class="site-link" title="在新窗口打开前台官网">
            <span>访问官网</span>
          </a>

          <div class="divider"></div>

          <div class="user-profile">
            <div class="avatar">{{ (current?.displayName || current?.username || 'A').charAt(0).toUpperCase() }}</div>
            <div class="user-info">
              <span class="user-name">{{ current?.displayName || current?.username }}</span>
              <span class="user-role" :class="current?.role === 'ADMIN' ? 'role-admin' : 'role-editor'">
                {{ current?.role === 'ADMIN' ? '管理员' : '编辑' }}
              </span>
            </div>
          </div>

          <div class="header-actions">
            <button class="icon-btn" title="修改密码" @click="router.push('/profile/password')">
              <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
            </button>
            <button class="icon-btn icon-btn--logout" title="退出登录" @click="onLogout">
              <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                <polyline points="16 17 21 12 16 7"></polyline>
                <line x1="21" y1="12" x2="9" y2="12"></line>
              </svg>
            </button>
          </div>
        </div>
      </el-header>

      <el-main class="main">
        <RouterView />
      </el-main>
    </el-container>

    <!-- 全局 ⌘K / Ctrl+K 命令面板 -->
    <el-dialog
      v-model="cmdOpen"
      title="全局快捷指令面板 (Command Palette)"
      width="580px"
      top="10vh"
      append-to-body
    >
      <div class="cmd-box">
        <input
          ref="cmdInputRef"
          v-model="cmdQuery"
          type="text"
          class="cmd-input"
          placeholder="输入关键词快速跳转页面或执行动作（如：新建产品 / 预演 / 线索 / SEO）..."
        />
        <ul class="cmd-list">
          <li
            v-for="(cmd, idx) in filteredCommands"
            :key="cmd.id"
            class="cmd-item"
            :class="{ 'is-active': idx === cmdActiveIdx }"
            @mouseenter="cmdActiveIdx = idx"
            @click="runCommand(cmd)"
          >
            <span class="cmd-item__cat">{{ cmd.category }}</span>
            <span class="cmd-item__title">{{ cmd.title }}</span>
            <span class="cmd-item__go">↵ 直达</span>
          </li>
          <li v-if="filteredCommands.length === 0" class="cmd-empty">没有匹配的指令</li>
        </ul>
      </div>
    </el-dialog>
  </el-container>
</template>

<style scoped>
.shell {
  min-height: 100vh;
}

.aside {
  background: var(--a-sidebar, #0d101b);
  color: var(--a-sidebar-text, #c9d1d9);
  display: flex;
  flex-direction: column;
  transition: width 0.25s ease;
  overflow: hidden;
}

.brand {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.brand.is-collapsed {
  justify-content: center;
  padding: 0;
}

.brand__logo {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--a-brand, #5b4be8);
  color: #ffffff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 16px;
  flex: none;
}

.brand__title {
  font-size: 14px;
  font-weight: 700;
  color: #ffffff;
  line-height: 1.2;
}

.brand__sub {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.48);
}

.menu-scroll {
  flex: 1;
}

.menu {
  border-right: none;
  background: transparent;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: rgba(255, 255, 255, 0.72);
  --el-menu-hover-bg-color: rgba(255, 255, 255, 0.06);
  --el-menu-active-color: #ffffff;
}

.menu-item--studio {
  margin: 6px 10px;
  border-radius: 8px;
  background: linear-gradient(135deg, rgba(124, 58, 237, 0.28) 0%, rgba(79, 70, 229, 0.28) 100%) !important;
  border: 1px solid rgba(167, 139, 250, 0.35);
  color: #e0e7ff !important;
  font-weight: 600;
}

.main-container {
  background: var(--a-bg, #f4f6f9);
  display: flex;
  flex-direction: column;
}

.header {
  background: #ffffff;
  border-bottom: 1px solid var(--a-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header__left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.collapse-btn {
  border: none;
  background: transparent;
  color: var(--a-text-muted);
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  display: flex;
}

.collapse-btn:hover {
  background: #f3f4f6;
}

.header__right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cmd-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 5px 12px;
  border-radius: 8px;
  border: 1px solid var(--a-border);
  background: #f8f9fc;
  color: var(--a-text-muted);
  font-size: 12.5px;
  cursor: pointer;
}

.cmd-btn:hover {
  border-color: #5b4be8;
  color: #5b4be8;
}

.cmd-btn kbd {
  font-size: 11px;
  padding: 1px 5px;
  background: #eef1f6;
  border-radius: 4px;
}

.studio-launch-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 8px;
  border: none;
  background: linear-gradient(135deg, #7c3aed 0%, #5b4be8 100%);
  color: #ffffff;
  font-size: 12.5px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(91, 75, 232, 0.25);
}

.site-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: var(--a-text-muted);
  text-decoration: none;
  padding: 6px 10px;
  border-radius: 6px;
}

.site-link:hover {
  color: var(--a-brand, #5b4be8);
  background: #f5f3ff;
}

.divider {
  width: 1px;
  height: 20px;
  background: var(--a-border);
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #eeebff;
  color: #5b4be8;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 13px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.user-name,
.username {
  font-size: 13.5px;
  font-weight: 600;
  color: var(--a-text);
}

.user-role {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 600;
}

.role-admin {
  background: #eeebff;
  color: #5b4be8;
}

.role-editor {
  background: #e0f2fe;
  color: #0284c7;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
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

/* 命令面板样式 */
.cmd-input {
  width: 100%;
  height: 42px;
  padding: 0 14px;
  border-radius: 10px;
  border: 1px solid var(--a-border);
  font-size: 14px;
  outline: none;
  margin-bottom: 12px;
}

.cmd-input:focus {
  border-color: #5b4be8;
}

.cmd-list {
  list-style: none;
  margin: 0;
  padding: 0;
  max-height: 340px;
  overflow-y: auto;
}

.cmd-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
}

.cmd-item.is-active {
  background: #f5f3ff;
}

.cmd-item__cat {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: #eef1f6;
  color: #5f6470;
  font-weight: 600;
}

.cmd-item__title {
  flex: 1;
  font-size: 13.5px;
  font-weight: 500;
}

.cmd-item__go {
  font-size: 12px;
  color: #5b4be8;
}

.cmd-empty {
  padding: 24px;
  text-align: center;
  color: var(--a-text-muted);
}
</style>
