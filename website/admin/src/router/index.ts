import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'
import LoginView from '@/views/LoginView.vue'
import DashboardView from '@/views/DashboardView.vue'
import UsersView from '@/views/UsersView.vue'
import AuditLogsView from '@/views/AuditLogsView.vue'
import PasswordView from '@/views/PasswordView.vue'
import TodoView from '@/views/TodoView.vue'
import { useAuth } from '@/stores/auth'

const page = (title: string, phase: string) => ({
  component: TodoView,
  meta: { title, phase },
})

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/login', name: 'login', component: LoginView, meta: { public: true } },
    {
      path: '/',
      component: AdminLayout,
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', component: DashboardView, meta: { title: '工作台' } },
        { path: 'settings/site', ...page('全站设置', '阶段 5') },
        { path: 'settings/navigation', ...page('导航与页脚链接', '阶段 5') },
        { path: 'home', ...page('首页管理', '阶段 5') },
        { path: 'contents/products', ...page('产品管理', '阶段 3') },
        { path: 'contents/solutions', ...page('解决方案管理', '阶段 4') },
        { path: 'contents/cases', ...page('客户案例管理', '阶段 4') },
        { path: 'contents/news', ...page('新闻管理', '阶段 4') },
        { path: 'contents/customers', ...page('客户标识管理', '阶段 4') },
        { path: 'contents/categories', ...page('内容分类', '阶段 3') },
        { path: 'pages/about', ...page('关于我们', '阶段 5') },
        { path: 'pages/contact', ...page('联系我们', '阶段 5') },
        { path: 'pages/privacy', ...page('隐私说明', '阶段 5') },
        { path: 'media', ...page('素材管理', '阶段 3') },
        { path: 'leads', ...page('咨询管理', '阶段 6') },
        { path: 'system/users', component: UsersView, meta: { title: '账号管理', role: 'ADMIN' } },
        { path: 'system/audit-logs', component: AuditLogsView, meta: { title: '操作日志', role: 'ADMIN' } },
        { path: 'profile/password', component: PasswordView, meta: { title: '修改密码' } },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
  ],
})

/**
 * 未登录先去服务端确认一次会话，避免刷新页面被误判为未登录。
 */
router.beforeEach(async (to) => {
  if (to.meta.public) {
    return true
  }
  const { current, loaded, fetchMe } = useAuth()
  if (!loaded.value) {
    await fetchMe()
  }
  if (!current.value) {
    return { path: '/login', query: to.fullPath === '/' ? undefined : { redirect: to.fullPath } }
  }
  if (to.meta.role === 'ADMIN' && current.value.role !== 'ADMIN') {
    return { path: '/dashboard' }
  }
  return true
})

export default router
