<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuth } from '@/stores/auth'
import { getConfig } from '@/api/site'

const route = useRoute()
const router = useRouter()
const { current, logout } = useAuth()

const isCollapse = ref(false)
const isAdmin = computed(() => current.value?.role === 'ADMIN')

// ==================== 动态顶部导航与扩展页面实时同步 ====================
interface NavLinkValue {
  type?: string
  pageCode?: string
  contentType?: string
  contentId?: string
  url?: string
}

interface NavNode {
  key?: string
  label: string
  enabled: boolean
  sortOrder: number
  link?: NavLinkValue | null
  children?: NavNode[]
}

interface NavigationDraft {
  items: NavNode[]
  megaColumns?: any[]
  megaCta?: any
}

const navDraft = ref<NavigationDraft | null>(null)

async function loadNavConfig() {
  try {
    const res = await getConfig<NavigationDraft>('NAVIGATION')
    if (res?.draft) {
      navDraft.value = res.draft
    }
  } catch {
    // 允许离线或降级
  }
}

const BUILTIN_PAGE_CODES = new Set(['HOME', 'PRODUCTS', 'SOLUTIONS', 'CASES', 'NEWS', 'ABOUT', 'CONTACT', 'PRIVACY'])

interface CustomNavItem {
  id: string
  label: string
  enabled: boolean
  targetDesc: string
  url?: string
  isExternal: boolean
}

const customNavItems = computed<CustomNavItem[]>(() => {
  if (!navDraft.value?.items) return []
  const list: CustomNavItem[] = []
  for (const item of navDraft.value.items) {
    if (!item.enabled) continue
    const link = item.link
    let isBuiltin = false
    let targetDesc = ''
    let isExternal = false
    let url = ''

    if (link) {
      if (link.type === 'PAGE' && link.pageCode && BUILTIN_PAGE_CODES.has(link.pageCode)) {
        isBuiltin = true
      } else if (link.type === 'URL') {
        targetDesc = link.url || '外部链接'
        url = link.url || ''
        isExternal = true
      } else if (link.type === 'PAGE') {
        targetDesc = link.pageCode ? `单页: ${link.pageCode}` : ''
      } else if (link.type === 'CONTENT') {
        targetDesc = `${link.contentType} #${link.contentId}`
      } else {
        targetDesc = ''
      }
    }

    if (!isBuiltin && item.label) {
      list.push({
        id: item.key || item.label,
        label: item.label,
        enabled: item.enabled,
        targetDesc,
        url,
        isExternal,
      })
    }
  }
  return list
})

function openExternal(url?: string) {
  if (url) {
    window.open(url, '_blank')
  }
}

// 侧边栏高亮智能追踪（含首页 tab 与分类 type 路由）
const activeMenu = computed(() => {
  const p = route.path
  if (p === '/home') {
    return `/home?tab=${route.query.tab || 'hero'}`
  }
  if (p === '/contents/categories') {
    return route.query.type ? `/contents/categories?type=${route.query.type}` : '/contents/categories?type=PRODUCT'
  }
  if (p.startsWith('/contents/products') && p.includes('/edit')) {
    return '/contents/products'
  }
  if (p.startsWith('/contents/solutions') && p.includes('/edit')) {
    return '/contents/solutions'
  }
  if (p.startsWith('/contents/cases') && p.includes('/edit')) {
    return '/contents/cases'
  }
  if (p.startsWith('/contents/news') && p.includes('/edit')) {
    return '/contents/news'
  }
  return route.fullPath || p
})

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
  { id: 'studio', title: '打开全站实景交互工作台 (Visual Live Studio)', category: '核心推荐', path: '/preview/home', keywords: 'preview studio 预演 可视化 画布 首页 实景' },
  { id: 'dash', title: '工作台概览 (Dashboard)', category: '工作台', path: '/dashboard', keywords: 'dashboard 工作台 统计' },
  // 首页模块独立直达
  { id: 'home-hero', title: '首页: 轮播焦点图 (Hero 轮播深浅主题/配色)', category: '首页管理', path: '/home?tab=hero', keywords: 'home hero 轮播 焦点图 banner 首页' },
  { id: 'home-services', title: '首页: 核心产品与方案分组推荐', category: '首页管理', path: '/home?tab=services', keywords: 'home services 产品 方案 分组 首页' },
  { id: 'home-cases', title: '首页: 标杆客户成功案例精选', category: '首页管理', path: '/home?tab=cases', keywords: 'home cases 案例 首页' },
  { id: 'home-customers', title: '首页: 合作客户标识 Logo 墙', category: '首页管理', path: '/home?tab=customers', keywords: 'home customers 客户标识 首页' },
  { id: 'home-news', title: '首页: 最新动态资讯精选', category: '首页管理', path: '/home?tab=news', keywords: 'home news 新闻 资讯 首页' },
  { id: 'home-order', title: '首页: 模块显示开关与上下排序', category: '首页管理', path: '/home?tab=order', keywords: 'home order 排序 显示 模块 首页' },
  // 各主要页面管理中心
  { id: 'prod-list', title: '产品中心: 全部产品管理列表', category: '产品中心', path: '/contents/products', keywords: 'products 产品 列表' },
  { id: 'prod-new', title: '产品中心: 新增产品 (Create Product)', category: '产品中心', path: '/contents/products/create', keywords: 'create new product 新增产品' },
  { id: 'prod-cat', title: '产品中心: 产品分类体系管理', category: '产品中心', path: '/contents/categories?type=PRODUCT', keywords: 'product category 分类 产品' },
  { id: 'sol-list', title: '解决方案: 全部方案管理列表', category: '解决方案', path: '/contents/solutions', keywords: 'solutions 解决方案 列表' },
  { id: 'sol-new', title: '解决方案: 新增方案 (Create Solution)', category: '解决方案', path: '/contents/solutions/create', keywords: 'create new solution 新增方案' },
  { id: 'sol-cat', title: '解决方案: 方案分类体系管理', category: '解决方案', path: '/contents/categories?type=SOLUTION', keywords: 'solution category 分类 方案' },
  { id: 'case-list', title: '客户案例: 客户案例管理列表', category: '客户案例', path: '/contents/cases', keywords: 'cases 客户案例 列表' },
  { id: 'case-new', title: '客户案例: 新增案例 (Create Case)', category: '客户案例', path: '/contents/cases/create', keywords: 'create new case 新增案例' },
  { id: 'case-cust', title: '客户案例: 合作客户 Logo 标识墙', category: '客户案例', path: '/contents/customers', keywords: 'customers 客户标识 logo' },
  { id: 'case-cat', title: '客户案例: 案例分类体系管理', category: '客户案例', path: '/contents/categories?type=CASE', keywords: 'case category 分类 案例' },
  { id: 'news-list', title: '动态资讯: 新闻动态管理列表', category: '动态资讯', path: '/contents/news', keywords: 'news 新闻 动态 列表' },
  { id: 'news-new', title: '动态资讯: 发布新闻动态 (Create News)', category: '动态资讯', path: '/contents/news/create', keywords: 'create new news 发布动态' },
  { id: 'news-cat', title: '动态资讯: 资讯分类体系管理', category: '动态资讯', path: '/contents/categories?type=NEWS', keywords: 'news category 分类 资讯' },
  // 单页管理
  { id: 'about', title: '关于我们单页配置', category: '单页管理', path: '/pages/about', keywords: 'about 关于我们 里程碑 资质' },
  { id: 'contact', title: '联系我们与咨询开关配置', category: '单页管理', path: '/pages/contact', keywords: 'contact 联系我们 电话 邮箱 表单开关' },
  { id: 'privacy', title: '隐私说明正文配置', category: '单页管理', path: '/pages/privacy', keywords: 'privacy 隐私说明 协议' },
  // 全局配置与运维
  { id: 'nav', title: '顶部导航与五列全景菜单 (Navigation)', category: '全局配置', path: '/settings/navigation', keywords: 'nav mega 导航 菜单 全景' },
  { id: 'site', title: '全站设置与 SEO / 页脚备案 (Site)', category: '全局配置', path: '/settings/site', keywords: 'site seo logo icp 全站 页脚 二维码' },
  { id: 'media', title: '素材库管理（图片与视频）', category: '全局配置', path: '/media', keywords: 'media 素材 图片 视频 上传' },
  { id: 'leads', title: '客户咨询线索看板与跟进 (Leads)', category: '运营与安全', path: '/leads', keywords: 'leads 咨询 线索 客户 看板', adminOnly: true },
  { id: 'users', title: '后台系统账号与角色管理', category: '运营与安全', path: '/system/users', keywords: 'users 账号 管理员 编辑 密码', adminOnly: true },
  { id: 'audit', title: '系统操作审计日志', category: '运营与安全', path: '/system/audit-logs', keywords: 'audit logs 审计 日志', adminOnly: true },
  { id: 'pwd', title: '修改个人登录密码', category: '个人中心', path: '/profile/password', keywords: 'password 修改密码' },
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
  const items: Array<{ title: string; path?: string }> = [{ title: '控制台', path: '/dashboard' }]
  if (route.path === '/dashboard') return items

  const path = route.path
  if (path === '/preview/home') {
    items.push({ title: '实景预演交互编辑' })
  } else if (path === '/home') {
    items.push({ title: '首页模块管理', path: '/home' })
    const tabMap: Record<string, string> = {
      hero: '轮播焦点图 (Hero)',
      services: '核心产品与方案推荐',
      cases: '客户成功案例精选',
      customers: '合作客户标识墙',
      news: '最新动态资讯精选',
      order: '模块显示与排序',
    }
    const tab = (route.query.tab as string) || 'hero'
    if (tabMap[tab]) {
      items.push({ title: tabMap[tab] })
    }
  } else if (path.startsWith('/contents/products')) {
    items.push({ title: '产品中心', path: '/contents/products' })
    if (path.includes('/create')) items.push({ title: '新增产品' })
    else if (path.includes('/edit')) items.push({ title: '编辑产品' })
  } else if (path.startsWith('/contents/solutions')) {
    items.push({ title: '解决方案', path: '/contents/solutions' })
    if (path.includes('/create')) items.push({ title: '新增解决方案' })
    else if (path.includes('/edit')) items.push({ title: '编辑解决方案' })
  } else if (path.startsWith('/contents/cases')) {
    items.push({ title: '客户案例', path: '/contents/cases' })
    if (path.includes('/create')) items.push({ title: '新增客户案例' })
    else if (path.includes('/edit')) items.push({ title: '编辑客户案例' })
  } else if (path.startsWith('/contents/news')) {
    items.push({ title: '动态资讯', path: '/contents/news' })
    if (path.includes('/create')) items.push({ title: '发布新动态' })
    else if (path.includes('/edit')) items.push({ title: '编辑动态' })
  } else if (path === '/contents/customers') {
    items.push({ title: '客户案例', path: '/contents/cases' })
    items.push({ title: '合作客户 Logo 墙' })
  } else if (path === '/contents/categories') {
    const type = route.query.type as string
    const catMap: Record<string, string> = {
      PRODUCT: '产品分类体系',
      SOLUTION: '方案分类体系',
      CASE: '案例分类体系',
      NEWS: '资讯分类体系',
    }
    if (type && catMap[type]) {
      items.push({ title: catMap[type] })
    } else {
      items.push({ title: '分类体系管理' })
    }
  } else if (path === '/pages/about') {
    items.push({ title: '关于我们单页' })
  } else if (path === '/pages/contact') {
    items.push({ title: '联系我们单页' })
  } else if (path === '/pages/privacy') {
    items.push({ title: '隐私说明单页' })
  } else if (path === '/settings/navigation') {
    items.push({ title: '顶栏导航与全景菜单' })
  } else if (path === '/settings/site') {
    items.push({ title: '全站设置与备案' })
  } else if (path === '/media') {
    items.push({ title: '素材管理库' })
  } else if (path === '/leads') {
    items.push({ title: '咨询线索管理' })
  } else if (path === '/system/users') {
    items.push({ title: '系统账号管理' })
  } else if (path === '/system/audit-logs') {
    items.push({ title: '操作审计日志' })
  } else if (path === '/profile/password') {
    items.push({ title: '修改个人密码' })
  } else if (route.meta.title) {
    items.push({ title: String(route.meta.title) })
  }

  return items
})

function handleResize() {
  isCollapse.value = window.innerWidth < 1024
}

function onStorageUpdate(e: StorageEvent) {
  if (e.key === 'admin_data_updated' || e.key === 'g7_last_published') {
    loadNavConfig()
  }
}

function onCustomConfigChanged() {
  loadNavConfig()
}

onMounted(() => {
  handleResize()
  loadNavConfig()
  window.addEventListener('resize', handleResize)
  window.addEventListener('keydown', handleKeydown)
  window.addEventListener('storage', onStorageUpdate)
  window.addEventListener('admin_config_changed', onCustomConfigChanged)
  window.addEventListener('focus', loadNavConfig)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('keydown', handleKeydown)
  window.removeEventListener('storage', onStorageUpdate)
  window.removeEventListener('admin_config_changed', onCustomConfigChanged)
  window.removeEventListener('focus', loadNavConfig)
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
          :default-active="activeMenu"
          :default-openeds="['page-home']"
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
            <template #title>工作台概览</template>
          </el-menu-item>

          <el-menu-item index="/preview/home" class="menu-item--studio">
            <el-icon>
              <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                <path d="M12 20h9"></path>
                <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path>
              </svg>
            </el-icon>
            <template #title>全站实景交互工作台</template>
          </el-menu-item>

          <!-- 页面管理中心：分页面模块化编辑 -->
          <div v-show="!isCollapse" class="menu-category-header">页面管理中心</div>

          <!-- 1. 首页各模块编辑 -->
          <el-sub-menu index="page-home">
            <template #title>
              <el-icon>
                <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                  <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                  <polyline points="9 22 9 12 15 12 15 22"></polyline>
                </svg>
              </el-icon>
              <span>首页模块管理</span>
            </template>
            <el-menu-item index="/home?tab=hero">轮播焦点图 (Hero)</el-menu-item>
            <el-menu-item index="/home?tab=services">核心产品与方案</el-menu-item>
            <el-menu-item index="/home?tab=cases">标杆成功案例</el-menu-item>
            <el-menu-item index="/home?tab=customers">合作客户标识墙</el-menu-item>
            <el-menu-item index="/home?tab=news">最新动态资讯</el-menu-item>
            <el-menu-item index="/home?tab=order">模块显示与排序</el-menu-item>
          </el-sub-menu>

          <!-- 2. 产品中心页 -->
          <el-sub-menu index="page-products">
            <template #title>
              <el-icon>
                <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                  <polygon points="12 2 2 7 12 12 22 7 12 2"></polygon>
                  <polyline points="2 17 12 22 22 17"></polyline>
                  <polyline points="2 12 12 17 22 12"></polyline>
                </svg>
              </el-icon>
              <span>产品中心页</span>
            </template>
            <el-menu-item index="/contents/products">全部产品列表</el-menu-item>
            <el-menu-item index="/contents/products/create">新增产品</el-menu-item>
            <el-menu-item index="/contents/categories?type=PRODUCT">产品分类体系</el-menu-item>
          </el-sub-menu>

          <!-- 3. 解决方案页 -->
          <el-sub-menu index="page-solutions">
            <template #title>
              <el-icon>
                <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                  <line x1="12" y1="1" x2="12" y2="3"></line>
                  <path d="M12 9a4 4 0 0 0-4 4c0 1.5 1 2.5 1.5 3.5.5 1 1 2.5 1 3.5h3c0-1 .5-2.5 1-3.5.5-1 1.5-2 1.5-3.5a4 4 0 0 0-4-4z"></path>
                </svg>
              </el-icon>
              <span>解决方案页</span>
            </template>
            <el-menu-item index="/contents/solutions">全部解决方案列表</el-menu-item>
            <el-menu-item index="/contents/solutions/create">新增解决方案</el-menu-item>
            <el-menu-item index="/contents/categories?type=SOLUTION">方案分类体系</el-menu-item>
          </el-sub-menu>

          <!-- 4. 客户案例页 -->
          <el-sub-menu index="page-cases">
            <template #title>
              <el-icon>
                <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                  <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"></path>
                  <path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"></path>
                  <path d="M4 22h16"></path>
                  <path d="M10 14.66V17c0 .55-.45 1-1 1H7v4h10v-4h-2c-.55 0-1-.45-1-1v-2.34"></path>
                  <path d="M18 4H6v7a6 6 0 0 0 12 0V4z"></path>
                </svg>
              </el-icon>
              <span>客户案例页</span>
            </template>
            <el-menu-item index="/contents/cases">客户案例列表</el-menu-item>
            <el-menu-item index="/contents/cases/create">新增客户案例</el-menu-item>
            <el-menu-item index="/contents/customers">合作客户 Logo 墙</el-menu-item>
            <el-menu-item index="/contents/categories?type=CASE">案例分类体系</el-menu-item>
          </el-sub-menu>

          <!-- 5. 动态资讯页 -->
          <el-sub-menu index="page-news">
            <template #title>
              <el-icon>
                <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                  <path d="M4 22h16a2 2 0 0 0 2-2V4a2 2 0 0 0-2-2H8a2 2 0 0 0-2 2v16a2 2 0 0 1-2 2Zm0 0a2 2 0 0 1-2-2v-9c0-1.1.9-2 2-2h2"></path>
                  <path d="M18 14h-8"></path>
                  <path d="M15 18h-5"></path>
                  <path d="M10 6h8v4h-8V6Z"></path>
                </svg>
              </el-icon>
              <span>动态资讯页</span>
            </template>
            <el-menu-item index="/contents/news">新闻动态列表</el-menu-item>
            <el-menu-item index="/contents/news/create">发布新动态</el-menu-item>
            <el-menu-item index="/contents/categories?type=NEWS">资讯分类体系</el-menu-item>
          </el-sub-menu>

          <!-- 6. 独立单页配置 -->
          <el-menu-item index="/pages/about">
            <el-icon>
              <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
            </el-icon>
            <template #title>关于我们单页</template>
          </el-menu-item>

          <el-menu-item index="/pages/contact">
            <el-icon>
              <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
              </svg>
            </el-icon>
            <template #title>联系我们单页</template>
          </el-menu-item>

          <el-menu-item index="/pages/privacy">
            <el-icon>
              <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
              </svg>
            </el-icon>
            <template #title>隐私说明单页</template>
          </el-menu-item>

          <!-- 7. 动态顶栏扩展菜单 (当导航设置中新建了自定义顶栏菜单时自动显示并实时更新) -->
          <el-sub-menu v-if="customNavItems.length > 0" index="page-custom-nav">
            <template #title>
              <el-icon>
                <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                  <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                </svg>
              </el-icon>
              <span>顶栏扩展页面 ({{ customNavItems.length }})</span>
            </template>
            <el-menu-item
              v-for="cItem in customNavItems"
              :key="cItem.id"
              :index="cItem.url ? '' : '/settings/navigation'"
              @click="cItem.url ? openExternal(cItem.url) : null"
            >
              <span class="custom-nav-label">{{ cItem.label }}</span>
              <span v-if="cItem.targetDesc && cItem.targetDesc !== 'NONE' && cItem.isExternal" class="custom-nav-badge">{{ cItem.targetDesc }}</span>
            </el-menu-item>
            <el-menu-item index="/settings/navigation">
              <span style="color: #a78bfa;">配置顶栏菜单...</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 全局底座与设置 -->
          <div v-show="!isCollapse" class="menu-category-header">全局底座与设置</div>

          <el-menu-item index="/settings/navigation">
            <el-icon>
              <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                <circle cx="12" cy="12" r="10"></circle>
                <polygon points="16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88 16.24 7.76"></polygon>
              </svg>
            </el-icon>
            <template #title>顶栏导航与全景菜单</template>
          </el-menu-item>

          <el-menu-item index="/settings/site">
            <el-icon>
              <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                <circle cx="12" cy="12" r="3"></circle>
                <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
              </svg>
            </el-icon>
            <template #title>全站设置与备案</template>
          </el-menu-item>

          <el-menu-item index="/media">
            <el-icon>
              <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                <circle cx="8.5" cy="8.5" r="1.5"></circle>
                <polyline points="21 15 16 10 5 21"></polyline>
              </svg>
            </el-icon>
            <template #title>素材管理库</template>
          </el-menu-item>

          <el-menu-item v-if="isAdmin" index="/leads">
            <el-icon>
              <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
              </svg>
            </el-icon>
            <template #title>咨询线索管理</template>
          </el-menu-item>

          <!-- 系统运维与安全 -->
          <div v-if="isAdmin && !isCollapse" class="menu-category-header">系统运维与安全</div>

          <el-sub-menu v-if="isAdmin" index="system">
            <template #title>
              <el-icon>
                <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                  <circle cx="9" cy="7" r="4"></circle>
                </svg>
              </el-icon>
              <span>系统账号管理</span>
            </template>
            <el-menu-item index="/system/users">账号与角色列表</el-menu-item>
            <el-menu-item index="/system/audit-logs">操作审计日志</el-menu-item>
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
            <span>快捷指令</span>
            <kbd>⌘K</kbd>
          </button>

          <button type="button" class="studio-launch-btn" @click="router.push('/preview/home')">
            实景交互工作台
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

.menu-category-header {
  padding: 16px 18px 6px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: rgba(255, 255, 255, 0.36);
  user-select: none;
}

.custom-nav-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.custom-nav-badge {
  margin-left: 8px;
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 4px;
  background: rgba(167, 139, 250, 0.22);
  color: #c4b5fd;
  max-width: 90px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
