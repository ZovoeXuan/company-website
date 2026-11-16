<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ArticleBody from '../../../shared/ArticleBody.vue'
import EmptyState from '@/components/EmptyState.vue'
import { ApiError } from '@/api/http'
import { getProduct } from '@/api/public'
import { openConsult } from '@/composables/consult'
import { usePageMeta } from '@/composables/usePageMeta'
import { vReveal } from '@/directives/reveal'
import type {
  OutcomeMetric,
  ProductDetail,
  ProductRoleTab,
  ProductTerminalTool,
  TitledItem,
} from '@/data/types'

const route = useRoute()
const router = useRouter()
const id = computed(() => String(route.params.id || ''))

const data = ref<ProductDetail | null>(null)
const loading = ref(true)
const notFound = ref(false)
const error = ref('')
const activeRoleIdx = ref(0)

usePageMeta(() => ({
  title: data.value?.seo?.title || data.value?.title || '产品详情',
  description: data.value?.seo?.description || data.value?.summary || '',
  noindex: !loading.value && !data.value,
}))

const heroMedia = computed(() => data.value?.hero || data.value?.cover || null)

const hardwareList = computed<TitledItem[]>(() => {
  if (data.value?.hardwareModules && data.value.hardwareModules.length > 0) {
    return data.value.hardwareModules
  }
  if (data.value?.features && data.value.features.length > 0) {
    return data.value.features
  }
  return []
})

const effectMetrics = computed<OutcomeMetric[]>(() => {
  if (data.value?.effectMetrics && data.value.effectMetrics.length > 0) {
    return data.value.effectMetrics
  }
  return []
})

const roleTabs = computed<ProductRoleTab[]>(() => {
  if (data.value?.roleTabs && data.value.roleTabs.length > 0) {
    return data.value.roleTabs
  }
  return []
})

const currentRoleTab = computed(() => roleTabs.value[activeRoleIdx.value] ?? roleTabs.value[0])

const terminalTools = computed<ProductTerminalTool[]>(() => {
  if (data.value?.terminalTools && data.value.terminalTools.length > 0) {
    return data.value.terminalTools
  }
  return []
})

function triggerConsult() {
  openConsult({
    sourcePosition: 'PRODUCT_DETAIL',
    contentType: 'PRODUCT',
    contentId: id.value,
    title: '预约产品演示与报价',
    initialDemand: data.value?.title ? `咨询产品：${data.value.title}` : '',
  })
}

async function load() {
  if (!id.value) return
  loading.value = true
  notFound.value = false
  error.value = ''
  activeRoleIdx.value = 0
  try {
    data.value = await getProduct(id.value)
  } catch (err) {
    data.value = null
    if (err instanceof ApiError && (err.status === 404 || err.status === 400)) {
      notFound.value = true
    } else {
      error.value = err instanceof ApiError ? err.message : '产品详情加载失败，请稍后重试'
    }
  } finally {
    loading.value = false
  }
}

watch(id, load)
onMounted(load)
</script>

<template>
  <div class="pdetail-page">
    <div v-if="loading" class="container state-box">
      <div class="skeleton"></div>
    </div>

    <div v-else-if="notFound" class="container state-box">
      <EmptyState
        title="未找到该产品"
        description="该产品可能尚未发布、已下线或链接地址有误。"
      >
        <button type="button" class="btn btn--primary" @click="router.push('/products')">
          返回产品列表
        </button>
      </EmptyState>
    </div>

    <div v-else-if="error" class="container state-box">
      <EmptyState title="加载失败" :description="error">
        <button type="button" class="btn btn--primary" @click="load">重试</button>
      </EmptyState>
    </div>

    <template v-else-if="data">
      <!-- 1. 首屏：产品名 + 价值摘要 + 立即咨询 + 首屏视觉 -->
      <section class="pdetail-hero">
        <div class="container pdetail-hero__grid" :class="{ 'is-single': !heroMedia }">
          <div v-reveal class="pdetail-hero__text">
            <span v-if="data.categoryName" class="pdetail-hero__cat">{{ data.categoryName }}</span>
            <h1 class="pdetail-hero__title">{{ data.title }}</h1>
            <p v-if="data.summary" class="pdetail-hero__sub">{{ data.summary }}</p>
            <div class="pdetail-hero__actions">
              <button type="button" class="btn btn--primary" @click="triggerConsult">
                立即咨询
              </button>
              <RouterLink to="/products" class="pdetail-back-link">返回产品列表</RouterLink>
            </div>
          </div>

          <div v-if="heroMedia" v-reveal="80" class="pdetail-hero__visual">
            <video
              v-if="heroMedia.type === 'VIDEO'"
              :src="heroMedia.url"
              class="pdetail-hero__img"
              autoplay
              muted
              loop
              playsinline
            ></video>
            <img v-else :src="heroMedia.url" :alt="heroMedia.alt || data.title" class="pdetail-hero__img" />
          </div>
        </div>
      </section>

      <!-- 2. 核心能力模块（仅当该产品真实配置了 features / hardwareModules 时展示） -->
      <section v-if="hardwareList.length > 0" class="pdetail-sec">
        <div class="container">
          <h2 v-reveal class="pdetail-sec__title">核心能力与功能模块</h2>
          <div class="pdetail-hw-grid">
            <article
              v-for="(item, idx) in hardwareList"
              :key="item.key || item.title || idx"
              v-reveal="idx * 50"
              class="pdetail-hw-card"
            >
              <img
                v-if="item.iconMediaId"
                :src="`/api/public/media/${item.iconMediaId}`"
                :alt="item.title"
                class="pdetail-hw-card__icon"
              />
              <span v-if="item.tag" class="pdetail-hw-card__tag">{{ item.tag }}</span>
              <h3 class="pdetail-hw-card__title">{{ item.title }}</h3>
              <p v-if="item.description" class="pdetail-hw-card__desc">{{ item.description }}</p>
            </article>
          </div>
        </div>
      </section>

      <!-- 3. 量化效果指标带（仅当配置了 effectMetrics 时展示） -->
      <section v-if="effectMetrics.length > 0" class="pdetail-metrics-band">
        <div class="container">
          <h2 v-reveal class="pdetail-sec__title pdetail-sec__title--light">量化应用成效</h2>
          <div class="pdetail-metrics-grid">
            <div
              v-for="(m, mIdx) in effectMetrics"
              :key="m.label + mIdx"
              v-reveal="mIdx * 50"
              class="pdetail-metric-item"
            >
              <strong class="pdetail-metric-item__val">{{ m.displayValue }}</strong>
              <span class="pdetail-metric-item__lbl">{{ m.label }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 4. 按角色价值 Tab 区（仅当配置了 roleTabs 时展示） -->
      <section v-if="roleTabs.length > 0" class="pdetail-sec pdetail-sec--alt">
        <div class="container">
          <h2 v-reveal class="pdetail-sec__title">多角色协同赋能</h2>

          <div v-reveal="60" class="pdetail-role-tabs" role="tablist">
            <button
              v-for="(rt, rIdx) in roleTabs"
              :key="rt.role"
              type="button"
              role="tab"
              class="pdetail-role-tab"
              :class="{ 'is-active': activeRoleIdx === rIdx }"
              :aria-selected="activeRoleIdx === rIdx"
              @click="activeRoleIdx = rIdx"
            >
              {{ rt.role }}
            </button>
          </div>

          <div v-if="currentRoleTab" v-reveal="90" class="pdetail-role-panel">
            <h3 class="pdetail-role-panel__headline">{{ currentRoleTab.headline }}</h3>
            <ul class="pdetail-role-panel__list">
              <li v-for="(b, bIdx) in currentRoleTab.bullets" :key="bIdx">{{ b }}</li>
            </ul>
          </div>
        </div>
      </section>

      <!-- 5. 终端与协同工具矩阵（仅当配置了 terminalTools 时展示） -->
      <section v-if="terminalTools.length > 0" class="pdetail-sec">
        <div class="container">
          <h2 v-reveal class="pdetail-sec__title">多端协同支持</h2>
          <div class="pdetail-hw-grid">
            <div
              v-for="(tool, tIdx) in terminalTools"
              :key="tool.name + tIdx"
              v-reveal="tIdx * 50"
              class="pdetail-tool-card"
            >
              <span v-if="tool.badge" class="pdetail-tool-card__badge">{{ tool.badge }}</span>
              <h3 class="pdetail-tool-card__title">{{ tool.name }}</h3>
              <p class="pdetail-tool-card__desc">{{ tool.description }}</p>
            </div>
          </div>
        </div>
      </section>

      <!-- 6. 详情富文本正文 (bodyHtml) -->
      <section v-if="data.bodyHtml" class="pdetail-sec pdetail-sec--prose">
        <div class="container">
          <ArticleBody :html="data.bodyHtml" :title="data.title" />
        </div>
      </section>

      <!-- 7. 关联解决方案 (relatedSolutions) -->
      <section v-if="data.relatedSolutions?.length" class="pdetail-sec pdetail-sec--alt">
        <div class="container">
          <h2 v-reveal class="pdetail-sec__title">适用行业解决方案</h2>
          <div class="pdetail-hw-grid">
            <RouterLink
              v-for="sol in data.relatedSolutions"
              :key="sol.id"
              :to="`/solutions/${sol.id}`"
              class="pdetail-case-card"
            >
              <div v-if="sol.cover" class="pdetail-case-card__media">
                <img :src="sol.cover.url" :alt="sol.cover.alt || sol.title" loading="lazy" />
              </div>
              <div class="pdetail-case-card__body">
                <span v-if="sol.industry || sol.categoryName" class="pdetail-case-card__cust">
                  {{ sol.industry || sol.categoryName }}
                </span>
                <h3 class="pdetail-case-card__title">{{ sol.title }}</h3>
                <p v-if="sol.summary" class="pdetail-case-card__sum">{{ sol.summary }}</p>
              </div>
            </RouterLink>
          </div>
        </div>
      </section>

      <!-- 8. 关联标杆案例（relatedCases，展示成果数字 outcomes） -->
      <section v-if="data.relatedCases?.length" class="pdetail-sec">
        <div class="container">
          <h2 v-reveal class="pdetail-sec__title">相关客户实践案例</h2>
          <div class="pdetail-hw-grid">
            <RouterLink
              v-for="(c, cIdx) in data.relatedCases"
              :key="c.id"
              v-reveal="cIdx * 60"
              :to="`/cases/${c.id}`"
              class="pdetail-case-card"
            >
              <div v-if="c.cover" class="pdetail-case-card__media">
                <img :src="c.cover.url" :alt="c.cover.alt || c.title" loading="lazy" />
              </div>
              <div class="pdetail-case-card__body">
                <span v-if="c.customerName" class="pdetail-case-card__cust">{{ c.customerName }}</span>
                <h3 class="pdetail-case-card__title">{{ c.title }}</h3>
                <p v-if="c.summary" class="pdetail-case-card__sum">{{ c.summary }}</p>
                <dl v-if="c.outcomes?.length" class="pdetail-case-card__outcomes">
                  <div v-for="o in c.outcomes.slice(0, 2)" :key="o.label">
                    <dt>{{ o.label }}</dt>
                    <dd>{{ o.displayValue }}</dd>
                  </div>
                </dl>
              </div>
            </RouterLink>
          </div>
        </div>
      </section>

      <!-- 9. 底部咨询转化行动区 -->
      <section class="pdetail-bottom-cta">
        <div class="container pdetail-bottom-cta__inner">
          <h2 class="pdetail-bottom-cta__title">预约「{{ data.title }}」产品演示与技术交流</h2>
          <button type="button" class="btn btn--primary" @click="triggerConsult">
            立即咨询
          </button>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.pdetail-page {
  background: #ffffff;
  color: #0f172a;
}

.state-box {
  padding: 80px 0;
}

.pdetail-hero {
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 65%, #1d4ed8 100%);
  color: #ffffff;
  padding: 112px 0 72px;
}

.pdetail-hero__grid {
  max-width: 1160px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  gap: 48px;
  align-items: center;
}

.pdetail-hero__grid.is-single {
  grid-template-columns: 1fr;
}

.pdetail-hero__cat {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 6px;
  background: rgba(37, 99, 235, 0.25);
  color: #93c5fd;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 14px;
}

.pdetail-hero__title {
  font-size: clamp(30px, 3.8vw, 44px);
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 16px;
}

.pdetail-hero__sub {
  font-size: 16px;
  line-height: 1.75;
  color: #cbd5e1;
  margin: 0 0 28px;
}

.pdetail-hero__actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.pdetail-back-link {
  color: #cbd5e1;
  font-size: 14px;
  text-decoration: none;
}

.pdetail-back-link:hover {
  color: #ffffff;
}

.pdetail-hero__img {
  width: 100%;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.14);
}

.pdetail-sec {
  padding: 72px 0;
}

.pdetail-sec--alt {
  background: #f8fafc;
}

.pdetail-sec__title {
  text-align: center;
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 40px;
}

.pdetail-sec__title--light {
  color: #ffffff;
}

.pdetail-hw-grid {
  max-width: 1120px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 24px;
}

.pdetail-hw-card,
.pdetail-tool-card {
  padding: 28px 24px;
  border-radius: 10px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
}

.pdetail-hw-card__icon {
  width: 40px;
  height: 40px;
  object-fit: contain;
  margin-bottom: 14px;
}

.pdetail-hw-card__tag,
.pdetail-tool-card__badge {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  color: var(--c-brand);
  background: var(--c-brand-soft);
  padding: 3px 10px;
  border-radius: 4px;
  margin-bottom: 12px;
}

.pdetail-hw-card__title,
.pdetail-tool-card__title {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 10px;
}

.pdetail-hw-card__desc,
.pdetail-tool-card__desc {
  font-size: 14px;
  line-height: 1.75;
  color: #475569;
  margin: 0;
}

.pdetail-metrics-band {
  padding: 64px 0;
  background: #0f172a;
  color: #ffffff;
}

.pdetail-metrics-grid {
  max-width: 1080px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 24px;
  text-align: center;
}

.pdetail-metric-item__val {
  display: block;
  font-size: 36px;
  font-weight: 700;
  color: #60a5fa;
  margin-bottom: 6px;
}

.pdetail-metric-item__lbl {
  font-size: 14px;
  color: #cbd5e1;
}

.pdetail-role-tabs {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 28px;
}

.pdetail-role-tab {
  padding: 9px 24px;
  border-radius: 6px;
  border: 1px solid #cbd5e1;
  background: #ffffff;
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  cursor: pointer;
}

.pdetail-role-tab.is-active {
  background: var(--c-brand);
  border-color: var(--c-brand);
  color: #ffffff;
}

.pdetail-role-panel {
  max-width: 820px;
  margin: 0 auto;
  background: #ffffff;
  padding: 32px 36px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}

.pdetail-role-panel__headline {
  font-size: 19px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 16px;
}

.pdetail-role-panel__list {
  margin: 0;
  padding-left: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  font-size: 14.5px;
  line-height: 1.75;
  color: #475569;
}

.pdetail-case-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
  text-decoration: none;
  color: inherit;
  display: flex;
  flex-direction: column;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.pdetail-case-card:hover {
  border-color: #93c5fd;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.pdetail-case-card__media {
  aspect-ratio: 16 / 10;
  overflow: hidden;
}

.pdetail-case-card__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pdetail-case-card__body {
  padding: 20px;
}

.pdetail-case-card__cust {
  font-size: 12px;
  font-weight: 600;
  color: var(--c-brand);
}

.pdetail-case-card__title {
  font-size: 16px;
  margin: 6px 0 8px;
}

.pdetail-case-card__sum {
  font-size: 13.5px;
  color: #64748b;
  margin: 0;
}

.pdetail-case-card__outcomes {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  gap: 16px;
}

.pdetail-case-card__outcomes dt {
  font-size: 11px;
  color: #64748b;
}

.pdetail-case-card__outcomes dd {
  margin: 2px 0 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--c-brand);
}

.pdetail-bottom-cta {
  padding: 64px 0;
  background: #0f172a;
  color: #ffffff;
  text-align: center;
}

.pdetail-bottom-cta__inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.pdetail-bottom-cta__title {
  font-size: 24px;
  color: #ffffff;
  margin: 0;
}

@media (max-width: 960px) {
  .pdetail-hero__grid,
  .pdetail-hw-grid {
    grid-template-columns: 1fr;
  }
}
</style>
