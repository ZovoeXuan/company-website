import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'
import LoginView from '@/views/LoginView.vue'
import DashboardView from '@/views/DashboardView.vue'
import UsersView from '@/views/UsersView.vue'
import AuditLogsView from '@/views/AuditLogsView.vue'
import PasswordView from '@/views/PasswordView.vue'
import ContentListView from '@/views/ContentListView.vue'
import ContentEditorView from '@/views/ContentEditorView.vue'
import CategoryView from '@/views/CategoryView.vue'
import MediaView from '@/views/MediaView.vue'
import SiteSettingsView from '@/views/SiteSettingsView.vue'
import NavigationView from '@/views/NavigationView.vue'
import AboutPageView from '@/views/AboutPageView.vue'
import ContactPageView from '@/views/ContactPageView.vue'
import PrivacyPageView from '@/views/PrivacyPageView.vue'
import TodoView from '@/views/TodoView.vue'
import { useAuth } from '@/stores/auth'

const page = (title: string, phase: string) => ({
  component: TodoView,
  meta: { title, phase },
})

const contentRoutes = (slug: string, contentType: string, title: string) => [
  {
    path: `contents/${slug}`,
    component: ContentListView,
    meta: { title: `${title}管理`, contentType, slug },
  },
  {
    path: `contents/${slug}/new`,
    component: ContentEditorView,
    meta: { title, contentType, slug },
  },
  {
    path: `contents/${slug}/:id/edit`,
    component: ContentEditorView,
    meta: { title, contentType, slug },
  },
]

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
        { path: 'settings/site', component: SiteSettingsView, meta: { title: '全站设置' } },
        { path: 'settings/navigation', component: NavigationView, meta: { title: '导航与页脚链接' } },
        { path: 'home', ...page('首页管理', '阶段 5-B') },
        ...contentRoutes('products', 'PRODUCT', '产品'),
        ...contentRoutes('solutions', 'SOLUTION', '解决方案'),
        ...contentRoutes('cases', 'CASE', '客户案例'),
        ...contentRoutes('news', 'NEWS', '新闻'),
        ...contentRoutes('customers', 'CUSTOMER', '客户标识'),
        { path: 'contents/categories', component: CategoryView, meta: { title: '内容分类' } },
        { path: 'pages/about', component: AboutPageView, meta: { title: '关于我们' } },
        { path: 'pages/contact', component: ContactPageView, meta: { title: '联系我们' } },
        { path: 'pages/privacy', component: PrivacyPageView, meta: { title: '隐私说明' } },
        { path: 'media', component: MediaView, meta: { title: '素材管理' } },
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
