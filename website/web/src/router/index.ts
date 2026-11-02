import { createRouter, createWebHistory } from 'vue-router'
import SiteLayout from '@/layouts/SiteLayout.vue'
import HomeView from '@/views/HomeView.vue'
import ProductListView from '@/views/ProductListView.vue'
import ProductDetailView from '@/views/ProductDetailView.vue'
import SolutionListView from '@/views/SolutionListView.vue'
import SolutionDetailView from '@/views/SolutionDetailView.vue'
import CaseListView from '@/views/CaseListView.vue'
import CaseDetailView from '@/views/CaseDetailView.vue'
import NewsListView from '@/views/NewsListView.vue'
import NewsDetailView from '@/views/NewsDetailView.vue'
import AboutView from '@/views/AboutView.vue'
import ContactView from '@/views/ContactView.vue'
import PrivacyView from '@/views/PrivacyView.vue'
import NotFoundView from '@/views/NotFoundView.vue'

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior(to, _from, savedPosition) {
    if (savedPosition) return savedPosition
    if (to.hash) return { el: to.hash, behavior: 'smooth' }
    return { top: 0 }
  },
  routes: [
    {
      path: '/',
      component: SiteLayout,
      children: [
        { path: '', name: 'home', component: HomeView },
        { path: 'products', name: 'products', component: ProductListView },
        { path: 'products/:id', name: 'product-detail', component: ProductDetailView },
        { path: 'solutions', name: 'solutions', component: SolutionListView },
        { path: 'solutions/:id', name: 'solution-detail', component: SolutionDetailView },
        { path: 'cases', name: 'cases', component: CaseListView },
        { path: 'cases/:id', name: 'case-detail', component: CaseDetailView },
        { path: 'news', name: 'news', component: NewsListView },
        { path: 'news/:id', name: 'news-detail', component: NewsDetailView },
        { path: 'about', name: 'about', component: AboutView },
        { path: 'contact', name: 'contact', component: ContactView },
        { path: 'privacy', name: 'privacy', component: PrivacyView },
        { path: ':pathMatch(.*)*', name: 'not-found', component: NotFoundView },
      ],
    },
  ],
})

export default router
