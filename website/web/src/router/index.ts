import { createRouter, createWebHistory } from 'vue-router'
import SiteLayout from '@/layouts/SiteLayout.vue'
import HomeView from '@/views/HomeView.vue'
import PlaceholderView from '@/views/PlaceholderView.vue'

const placeholder = (title: string, subtitle: string) => ({
  component: PlaceholderView,
  props: { title, subtitle },
})

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior: () => ({ top: 0 }),
  routes: [
    {
      path: '/',
      component: SiteLayout,
      children: [
        { path: '', name: 'home', component: HomeView },
        { path: 'products', name: 'products', ...placeholder('产品', '按分类浏览产品，卡片与分类由后台维护。') },
        { path: 'products/:id', name: 'product-detail', ...placeholder('产品详情', '图文正文、关联案例与咨询入口。') },
        { path: 'solutions', name: 'solutions', ...placeholder('解决方案', '按行业组织的方案列表。') },
        { path: 'solutions/:id', name: 'solution-detail', ...placeholder('方案详情', '方案说明与关联产品。') },
        { path: 'cases', name: 'cases', ...placeholder('客户案例', '按行业筛选，展示客户故事与成果。') },
        { path: 'cases/:id', name: 'case-detail', ...placeholder('案例详情', '客户背景、实施过程与结果数据。') },
        { path: 'news', name: 'news', ...placeholder('新闻中心', '按日期倒序，支持分类与关键字。') },
        { path: 'news/:id', name: 'news-detail', ...placeholder('新闻详情', '正文、来源与上一篇/下一篇。') },
        { path: 'about', name: 'about', ...placeholder('关于我们', '企业介绍、发展历程与资质。') },
        { path: 'contact', name: 'contact', ...placeholder('联系我们', '电话、邮箱、地址与咨询表单。') },
        { path: 'privacy', name: 'privacy', ...placeholder('隐私说明', '当前发布的隐私说明正文。') },
        { path: ':pathMatch(.*)*', name: 'not-found', ...placeholder('页面不存在', '链接可能已失效，可从导航重新进入。') },
      ],
    },
  ],
})

export default router
