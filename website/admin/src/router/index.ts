import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'
import LoginView from '@/views/LoginView.vue'
import TodoView from '@/views/TodoView.vue'

const page = (title: string, phase: string) => ({
  component: TodoView,
  meta: { title, phase },
})

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/login', name: 'login', component: LoginView },
    {
      path: '/',
      component: AdminLayout,
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', ...page('工作台', '阶段 2') },
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
        { path: 'system/users', ...page('账号管理', '阶段 2') },
        { path: 'system/audit-logs', ...page('操作日志', '阶段 2') },
        { path: 'profile/password', ...page('修改密码', '阶段 2') },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
  ],
})

export default router
