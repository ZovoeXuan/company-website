<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ArticleBody from '../../../shared/ArticleBody.vue'
import { get } from '@/api/http'
import { getConfig, publishConfig, type ConfigView } from '@/api/site'
import { publishContent } from '@/api/content'
import { useAuth } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const { current } = useAuth()
const isAdmin = computed(() => current.value?.role === 'ADMIN')

const isContentPreview = computed(() => Boolean(route.params.id))
const contentId = computed(() => String(route.params.id || ''))
const contentSlug = computed(() => String(route.params.slug || 'products'))

const loading = ref(false)
const publishing = ref(false)
const viewport = ref<'desktop' | 'tablet' | 'mobile'>('desktop')
const activeModule = ref<'home' | 'site' | 'navigation' | 'about' | 'contact' | 'privacy'>('home')

const homePreview = ref<Record<string, any> | null>(null)
const contentPreview = ref<Record<string, any> | null>(null)
const configs = ref<Record<string, ConfigView<any>>>({})

const CONFIG_KEYS = ['SITE', 'NAVIGATION', 'HOME', 'ABOUT', 'CONTACT', 'PRIVACY'] as const

interface HealthIssue {
  level: 'warning' | 'info'
  module: string
  message: string
  actionPath: string
}

const healthIssues = computed<HealthIssue[]>(() => {
  const list: HealthIssue[] = []
  const siteCfg = configs.value.SITE?.draft
  const navCfg = configs.value.NAVIGATION?.draft
  const homeCfg = configs.value.HOME?.draft
  const contactCfg = configs.value.CONTACT?.draft

  if (!siteCfg?.siteName?.trim()) {
    list.push({
      level: 'warning',
      module: '全站设置 (SITE)',
      message: '尚未配置站点名称 (siteName)',
      actionPath: '/settings/site',
    })
  }
  if (!siteCfg?.defaultSeo?.title?.trim()) {
    list.push({
      level: 'info',
      module: '全站设置 (SITE)',
      message: '建议补充默认 SEO 标题',
      actionPath: '/settings/site',
    })
  }
  if (!Array.isArray(navCfg?.items) || navCfg.items.length === 0) {
    list.push({
      level: 'warning',
      module: '顶部导航 (NAVIGATION)',
      message: '尚未配置顶部主导航菜单项',
      actionPath: '/settings/navigation',
    })
  }
  if (!Array.isArray(homeCfg?.hero?.items) || homeCfg.hero.items.length === 0) {
    list.push({
      level: 'warning',
      module: '首页管理 (HOME)',
      message: '首页尚未配置首屏轮播焦点图',
      actionPath: '/home?tab=hero',
    })
  }
  if (contactCfg?.formEnabled && !contactCfg?.phone && !siteCfg?.contact?.phone) {
    list.push({
      level: 'info',
      module: '联系我们 (CONTACT)',
      message: '已开启留资表单，建议同时配置官方咨询热线',
      actionPath: '/pages/contact',
    })
  }
  for (const key of CONFIG_KEYS) {
    const cfg = configs.value[key]
    if (cfg?.hasUnpublishedChanges) {
      list.push({
        level: 'info',
        module: `${key} 配置`,
        message: `草稿 v${cfg.draftVersion} 有尚未发布到线上的修改`,
        actionPath: getEditorPathForConfig(key),
      })
    }
  }
  return list
})

function getEditorPathForConfig(key: string): string {
  const map: Record<string, string> = {
    SITE: '/settings/site',
    NAVIGATION: '/settings/navigation',
    HOME: '/home',
    ABOUT: '/pages/about',
    CONTACT: '/pages/contact',
    PRIVACY: '/pages/privacy',
  }
  return map[key] || '/dashboard'
}

async function loadAll() {
  loading.value = true
  try {
    if (isContentPreview.value) {
      contentPreview.value = await get<Record<string, any>>(
        `/api/admin/preview/contents/${contentId.value}`,
      )
    } else {
      const [homeData, ...cfgList] = await Promise.all([
        get<Record<string, any>>('/api/admin/preview/home').catch(() => null),
        ...CONFIG_KEYS.map((k) => getConfig(k).catch(() => null)),
      ])
      homePreview.value = homeData
      const nextMap: Record<string, ConfigView<any>> = {}
      CONFIG_KEYS.forEach((k, idx) => {
        if (cfgList[idx]) nextMap[k] = cfgList[idx]!
      })
      configs.value = nextMap
    }
  } catch (err: any) {
    ElMessage.error(err?.message || '预览数据加载失败')
  } finally {
    loading.value = false
  }
}

async function handlePublishCurrentContent() {
  const meta = contentPreview.value?.content
  if (!meta) return
  publishing.value = true
  try {
    await publishContent(String(meta.id), meta.lockVersion, meta.draftVersion)
    ElMessage.success('内容已正式发布上线')
    await loadAll()
  } catch (err: any) {
    ElMessage.error(err?.message || '发布失败')
  } finally {
    publishing.value = false
  }
}

async function handlePublishConfigKey(key: string) {
  const cfg = configs.value[key]
  if (!cfg) return
  publishing.value = true
  try {
    await publishConfig(key as Parameters<typeof publishConfig>[0], cfg.lockVersion, cfg.draftVersion)
    ElMessage.success(`${key} 配置已发布上线`)
    await loadAll()
  } catch (err: any) {
    ElMessage.error(err?.message || '发布失败')
  } finally {
    publishing.value = false
  }
}

watch(() => route.fullPath, loadAll)
onMounted(loadAll)
</script>

<template>
  <div v-loading="loading" class="preview-workbench">
    <!-- 顶部实景预览与发布体检控制栏 -->
    <header class="pw-topbar">
      <div class="pw-topbar__left">
        <el-button link @click="router.push('/dashboard')">← 返回控制台</el-button>
        <span class="pw-topbar__divider">|</span>
        <strong class="pw-topbar__title">
          {{ isContentPreview ? `内容草稿实景预览 #${contentId}` : '全站实景预览与发布体检工作台' }}
        </strong>
        <el-tag v-if="!isContentPreview" size="small" type="success">单渲染源真实数据契约</el-tag>
      </div>

      <div class="pw-topbar__center">
        <el-radio-group v-model="viewport" size="small">
          <el-radio-button value="desktop">桌面端 (100%)</el-radio-button>
          <el-radio-button value="tablet">平板端 (820px)</el-radio-button>
          <el-radio-button value="mobile">移动端 (390px)</el-radio-button>
        </el-radio-group>
      </div>

      <div class="pw-topbar__right">
        <el-button size="small" @click="loadAll">刷新预览</el-button>
        <template v-if="isContentPreview && contentPreview?.content">
          <el-button
            size="small"
            type="primary"
            plain
            @click="router.push(`/contents/${contentSlug}/${contentId}/edit`)"
          >
            返回编辑器
          </el-button>
          <el-button
            v-if="isAdmin"
            size="small"
            type="success"
            :loading="publishing"
            @click="handlePublishCurrentContent"
          >
            立即发布该内容
          </el-button>
        </template>
        <template v-else>
          <a href="/" target="_blank" class="pw-ext-link">新窗口打开线上官网 ↗</a>
        </template>
      </div>
    </header>

    <!-- 模式 A：单篇内容详情草稿实景预览 -->
    <main v-if="isContentPreview" class="pw-stage-wrap">
      <div class="pw-canvas" :class="`is-${viewport}`">
        <template v-if="contentPreview">
          <div class="pw-content-banner">
            <div class="pw-content-banner__meta">
              <el-tag size="small" effect="dark">{{ contentPreview.content?.contentType }}</el-tag>
              <span v-if="contentPreview.categoryName">{{ contentPreview.categoryName }}</span>
              <span>草稿版本 v{{ contentPreview.content?.draftVersion }}</span>
            </div>
            <h1 class="pw-content-banner__title">{{ contentPreview.title || '未命名标题' }}</h1>
            <p v-if="contentPreview.summary" class="pw-content-banner__sub">
              {{ contentPreview.summary }}
            </p>
          </div>

          <div class="pw-content-body">
            <!-- 产品结构化预览 -->
            <div v-if="contentPreview.features?.length" class="pw-struct-sec">
              <h2>核心能力与特性模块</h2>
              <div class="pw-grid-3">
                <div v-for="(f, idx) in contentPreview.features" :key="idx" class="pw-card">
                  <span v-if="f.tag" class="pw-badge">{{ f.tag }}</span>
                  <h3>{{ f.title }}</h3>
                  <p>{{ f.description }}</p>
                </div>
              </div>
            </div>

            <div v-if="contentPreview.effectMetrics?.length" class="pw-struct-sec">
              <h2>量化效果指标</h2>
              <div class="pw-grid-4">
                <div v-for="(m, idx) in contentPreview.effectMetrics" :key="idx" class="pw-metric">
                  <strong>{{ m.displayValue }}</strong>
                  <span>{{ m.label }}</span>
                </div>
              </div>
            </div>

            <!-- 方案结构化预览 -->
            <div v-if="contentPreview.pains?.length" class="pw-struct-sec">
              <h2>业务痛点</h2>
              <div class="pw-grid-2">
                <div v-for="(p, idx) in contentPreview.pains" :key="idx" class="pw-card">
                  <h3>{{ p.title }}</h3>
                  <p>{{ p.description }}</p>
                </div>
              </div>
            </div>

            <div v-if="contentPreview.benefits?.length" class="pw-struct-sec">
              <h2>方案核心价值</h2>
              <div class="pw-grid-2">
                <div v-for="(b, idx) in contentPreview.benefits" :key="idx" class="pw-card">
                  <h3>{{ b.title }}</h3>
                  <p>{{ b.description }}</p>
                </div>
              </div>
            </div>

            <!-- 案例结构化预览 -->
            <div v-if="contentPreview.outcomes?.length" class="pw-struct-sec">
              <h2>关键量化成果</h2>
              <div class="pw-grid-4">
                <div v-for="(o, idx) in contentPreview.outcomes" :key="idx" class="pw-metric">
                  <strong>{{ o.displayValue }}</strong>
                  <span>{{ o.label }}</span>
                </div>
              </div>
            </div>

            <blockquote v-if="contentPreview.quoteText" class="pw-quote">
              <p>“{{ contentPreview.quoteText }}”</p>
              <footer v-if="contentPreview.quoteAuthor">—— {{ contentPreview.quoteAuthor }}</footer>
            </blockquote>

            <!-- 富文本正文（复用共享 ArticleBody 组件） -->
            <div class="pw-struct-sec">
              <h2>正文详情排版预览</h2>
              <ArticleBody :html="contentPreview.bodyHtml" :title="contentPreview.title" />
            </div>
          </div>
        </template>
      </div>
    </main>

    <!-- 模式 B：全站配置实景预览与发布体检工作台 -->
    <div v-else class="pw-main-layout">
      <!-- 左侧：模块切换与发布体检侧栏 -->
      <aside class="pw-sidebar">
        <div class="pw-sidebar__section">
          <h3 class="pw-sidebar__heading">预览模块切换</h3>
          <div class="pw-nav-list">
            <button
              type="button"
              class="pw-nav-btn"
              :class="{ 'is-active': activeModule === 'home' }"
              @click="activeModule = 'home'"
            >
              <span>首页草稿聚合预览 (HOME)</span>
              <el-tag v-if="configs.HOME?.hasUnpublishedChanges" size="small" type="warning">待发布</el-tag>
            </button>
            <button
              type="button"
              class="pw-nav-btn"
              :class="{ 'is-active': activeModule === 'site' }"
              @click="activeModule = 'site'"
            >
              <span>全站与页脚配置 (SITE)</span>
              <el-tag v-if="configs.SITE?.hasUnpublishedChanges" size="small" type="warning">待发布</el-tag>
            </button>
            <button
              type="button"
              class="pw-nav-btn"
              :class="{ 'is-active': activeModule === 'navigation' }"
              @click="activeModule = 'navigation'"
            >
              <span>顶栏与全景菜单 (NAVIGATION)</span>
              <el-tag v-if="configs.NAVIGATION?.hasUnpublishedChanges" size="small" type="warning">待发布</el-tag>
            </button>
            <button
              type="button"
              class="pw-nav-btn"
              :class="{ 'is-active': activeModule === 'about' }"
              @click="activeModule = 'about'"
            >
              <span>关于我们单页 (ABOUT)</span>
              <el-tag v-if="configs.ABOUT?.hasUnpublishedChanges" size="small" type="warning">待发布</el-tag>
            </button>
            <button
              type="button"
              class="pw-nav-btn"
              :class="{ 'is-active': activeModule === 'contact' }"
              @click="activeModule = 'contact'"
            >
              <span>联系我们与留资 (CONTACT)</span>
              <el-tag v-if="configs.CONTACT?.hasUnpublishedChanges" size="small" type="warning">待发布</el-tag>
            </button>
            <button
              type="button"
              class="pw-nav-btn"
              :class="{ 'is-active': activeModule === 'privacy' }"
              @click="activeModule = 'privacy'"
            >
              <span>隐私说明单页 (PRIVACY)</span>
              <el-tag v-if="configs.PRIVACY?.hasUnpublishedChanges" size="small" type="warning">待发布</el-tag>
            </button>
          </div>
        </div>

        <div class="pw-sidebar__section">
          <h3 class="pw-sidebar__heading">全站发布前健康巡检 ({{ healthIssues.length }})</h3>
          <div v-if="healthIssues.length === 0" class="pw-health-ok">
            ✓ 所有核心配置均已就绪且全部同步发布
          </div>
          <ul v-else class="pw-health-list">
            <li v-for="(issue, idx) in healthIssues" :key="idx" class="pw-health-item">
              <div class="pw-health-item__top">
                <el-tag size="small" :type="issue.level === 'warning' ? 'danger' : 'warning'">
                  {{ issue.module }}
                </el-tag>
                <el-button link type="primary" size="small" @click="router.push(issue.actionPath)">
                  去处理 →
                </el-button>
              </div>
              <p class="pw-health-item__msg">{{ issue.message }}</p>
            </li>
          </ul>
        </div>
      </aside>

      <!-- 右侧：真实草稿数据画布预览 -->
      <main class="pw-stage-wrap">
        <div class="pw-canvas" :class="`is-${viewport}`">
          <!-- 1. 首页草稿聚合预览 -->
          <template v-if="activeModule === 'home'">
            <div class="pw-module-bar">
              <div>
                <strong>首页草稿聚合视图 (/api/admin/preview/home)</strong>
                <span class="pw-module-bar__sub">
                  草稿 v{{ configs.HOME?.draftVersion ?? 1 }} · 线上 v{{ configs.HOME?.publishedVersion ?? '未发布' }}
                </span>
              </div>
              <div class="pw-module-bar__actions">
                <el-button size="small" @click="router.push('/home')">编辑首页配置</el-button>
                <el-button
                  v-if="isAdmin && configs.HOME?.hasUnpublishedChanges"
                  size="small"
                  type="success"
                  :loading="publishing"
                  @click="handlePublishConfigKey('HOME')"
                >
                  发布首页配置
                </el-button>
              </div>
            </div>

            <div v-if="homePreview" class="pw-home-body">
              <!-- 首屏轮播 -->
              <section class="pw-hero">
                <template v-if="homePreview.hero?.items?.length">
                  <div v-for="(slide, sIdx) in homePreview.hero.items" :key="sIdx" class="pw-hero-slide">
                    <span v-if="slide.kicker || slide.badgeText" class="pw-badge">{{ slide.kicker || slide.badgeText }}</span>
                    <h2>{{ slide.title || '未命名首屏标题' }}</h2>
                    <p>{{ slide.subtitle }}</p>
                    <div class="pw-hero-slide__foot">
                      <el-tag size="small">{{ slide.buttonText || '了解详情' }}</el-tag>
                      <el-button size="small" link type="primary" @click="router.push('/home?tab=hero')">
                        修改此首屏 →
                      </el-button>
                    </div>
                  </div>
                </template>
                <div v-else class="pw-empty-block">
                  <p>尚未配置首页首屏焦点图</p>
                  <el-button size="small" type="primary" @click="router.push('/home?tab=hero')">去配置首屏</el-button>
                </div>
              </section>

              <!-- 指标带 -->
              <section v-if="homePreview.metrics?.items?.length" class="pw-struct-sec">
                <div class="pw-sec-head">
                  <h2>{{ homePreview.metrics.title || '核心运营指标' }}</h2>
                  <el-button size="small" link type="primary" @click="router.push('/home')">编辑指标</el-button>
                </div>
                <div class="pw-grid-4">
                  <div v-for="(m, mIdx) in homePreview.metrics.items" :key="mIdx" class="pw-metric">
                    <strong>{{ m.displayValue }}{{ m.unit || '' }}</strong>
                    <span>{{ m.label }}</span>
                  </div>
                </div>
              </section>

              <!-- 服务分组 -->
              <section class="pw-struct-sec">
                <div class="pw-sec-head">
                  <h2>{{ homePreview.services?.title || '核心产品与解决方案' }}</h2>
                  <el-button size="small" link type="primary" @click="router.push('/home?tab=services')">
                    编辑服务分组 →
                  </el-button>
                </div>
                <div v-if="homePreview.services?.groups?.length" class="pw-grid-2">
                  <div v-for="(grp, gIdx) in homePreview.services.groups" :key="gIdx" class="pw-card">
                    <span v-if="grp.tag" class="pw-badge">{{ grp.tag }}</span>
                    <h3>{{ grp.title }}</h3>
                    <p>{{ grp.description }}</p>
                    <div v-if="grp.items?.length" class="pw-subitems">
                      <span v-for="it in grp.items" :key="it.id" class="pw-subtag">{{ it.title }}</span>
                    </div>
                  </div>
                </div>
                <p v-else class="pw-muted">尚未配置产品与方案分组</p>
              </section>

              <!-- 案例与新闻 -->
              <section class="pw-struct-sec">
                <div class="pw-sec-head">
                  <h2>{{ homePreview.cases?.title || '精选客户案例' }} ({{ homePreview.cases?.items?.length || 0 }})</h2>
                  <el-button size="small" link type="primary" @click="router.push('/home?tab=cases')">
                    编辑精选案例 →
                  </el-button>
                </div>
                <div v-if="homePreview.cases?.items?.length" class="pw-grid-3">
                  <div v-for="c in homePreview.cases.items" :key="c.id" class="pw-card">
                    <h3>{{ c.title }}</h3>
                    <p>{{ c.summary }}</p>
                  </div>
                </div>
                <p v-else class="pw-muted">当前无已关联案例</p>
              </section>
            </div>
          </template>

          <!-- 2. 全站设置预览 -->
          <template v-else-if="activeModule === 'site'">
            <div class="pw-module-bar">
              <strong>全站品牌、SEO 与页脚配置草稿 (SITE)</strong>
              <div class="pw-module-bar__actions">
                <el-button size="small" @click="router.push('/settings/site')">编辑全站设置</el-button>
                <el-button
                  v-if="isAdmin && configs.SITE?.hasUnpublishedChanges"
                  size="small"
                  type="success"
                  :loading="publishing"
                  @click="handlePublishConfigKey('SITE')"
                >
                  发布全站设置
                </el-button>
              </div>
            </div>
            <div class="pw-content-body">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="站点名称">{{ configs.SITE?.draft?.siteName || '未配置' }}</el-descriptions-item>
                <el-descriptions-item label="品牌标语">{{ configs.SITE?.draft?.slogan || '未配置' }}</el-descriptions-item>
                <el-descriptions-item label="默认 SEO 标题">{{ configs.SITE?.draft?.defaultSeo?.title || '未配置' }}</el-descriptions-item>
                <el-descriptions-item label="版权信息">{{ configs.SITE?.draft?.copyrightText || '未配置' }}</el-descriptions-item>
                <el-descriptions-item label="ICP 备案号">{{ configs.SITE?.draft?.icpText || '未配置' }}</el-descriptions-item>
                <el-descriptions-item label="页脚分组数">{{ configs.SITE?.draft?.footerGroups?.length || 0 }} 组</el-descriptions-item>
              </el-descriptions>
            </div>
          </template>

          <!-- 3. 顶栏导航预览 -->
          <template v-else-if="activeModule === 'navigation'">
            <div class="pw-module-bar">
              <strong>顶栏主导航与全景下拉菜单草稿 (NAVIGATION)</strong>
              <div class="pw-module-bar__actions">
                <el-button size="small" @click="router.push('/settings/navigation')">编辑导航配置</el-button>
                <el-button
                  v-if="isAdmin && configs.NAVIGATION?.hasUnpublishedChanges"
                  size="small"
                  type="success"
                  :loading="publishing"
                  @click="handlePublishConfigKey('NAVIGATION')"
                >
                  发布导航配置
                </el-button>
              </div>
            </div>
            <div class="pw-content-body">
              <h3>主导航项 ({{ configs.NAVIGATION?.draft?.items?.length || 0 }})</h3>
              <div class="pw-grid-3" style="margin-top: 12px;">
                <div v-for="(item, idx) in configs.NAVIGATION?.draft?.items || []" :key="idx" class="pw-card">
                  <h3>{{ item.label }}</h3>
                  <p>状态：{{ item.enabled ? '显示' : '隐藏' }} · 子项：{{ item.children?.length || 0 }} 个</p>
                </div>
              </div>
            </div>
          </template>

          <!-- 4. 关于我们预览 -->
          <template v-else-if="activeModule === 'about'">
            <div class="pw-module-bar">
              <strong>关于我们单页草稿 (ABOUT)</strong>
              <div class="pw-module-bar__actions">
                <el-button size="small" @click="router.push('/pages/about')">编辑关于我们</el-button>
                <el-button
                  v-if="isAdmin && configs.ABOUT?.hasUnpublishedChanges"
                  size="small"
                  type="success"
                  :loading="publishing"
                  @click="handlePublishConfigKey('ABOUT')"
                >
                  发布关于我们
                </el-button>
              </div>
            </div>
            <div class="pw-content-body">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="首屏标题">{{ configs.ABOUT?.draft?.heroTitle || '未配置' }}</el-descriptions-item>
                <el-descriptions-item label="企业简介标题">{{ configs.ABOUT?.draft?.introTitle || '关于我们' }}</el-descriptions-item>
                <el-descriptions-item label="管理团队人数">{{ configs.ABOUT?.draft?.teamMembers?.length || 0 }} 人</el-descriptions-item>
                <el-descriptions-item label="发展历程节点">{{ configs.ABOUT?.draft?.milestones?.length || 0 }} 条</el-descriptions-item>
                <el-descriptions-item label="共享资料数">{{ configs.ABOUT?.draft?.resources?.length || 0 }} 项</el-descriptions-item>
                <el-descriptions-item label="服务网点数">{{ configs.ABOUT?.draft?.offices?.length || 0 }} 个</el-descriptions-item>
              </el-descriptions>
            </div>
          </template>

          <!-- 5. 联系我们预览 -->
          <template v-else-if="activeModule === 'contact'">
            <div class="pw-module-bar">
              <strong>联系我们与短信验证码留资草稿 (CONTACT)</strong>
              <div class="pw-module-bar__actions">
                <el-button size="small" @click="router.push('/pages/contact')">编辑联系我们</el-button>
                <el-button
                  v-if="isAdmin && configs.CONTACT?.hasUnpublishedChanges"
                  size="small"
                  type="success"
                  :loading="publishing"
                  @click="handlePublishConfigKey('CONTACT')"
                >
                  发布联系我们
                </el-button>
              </div>
            </div>
            <div class="pw-content-body">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="公司全称">{{ configs.CONTACT?.draft?.companyName || '未配置' }}</el-descriptions-item>
                <el-descriptions-item label="咨询热线">{{ configs.CONTACT?.draft?.phone || '未配置' }}</el-descriptions-item>
                <el-descriptions-item label="商务邮箱">{{ configs.CONTACT?.draft?.email || '未配置' }}</el-descriptions-item>
                <el-descriptions-item label="在线留资通道">
                  <el-tag :type="configs.CONTACT?.draft?.formEnabled ? 'success' : 'info'" size="small">
                    {{ configs.CONTACT?.draft?.formEnabled ? '已开启' : '已关闭' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="手机短信验证码核验">
                  <el-tag :type="configs.CONTACT?.draft?.smsVerifyEnabled !== false ? 'success' : 'warning'" size="small">
                    {{ configs.CONTACT?.draft?.smsVerifyEnabled !== false ? '强制核验' : '已关闭' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="总部地址">{{ configs.CONTACT?.draft?.address || '未配置' }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </template>

          <!-- 6. 隐私说明预览 -->
          <template v-else-if="activeModule === 'privacy'">
            <div class="pw-module-bar">
              <strong>隐私说明单页草稿 (PRIVACY)</strong>
              <div class="pw-module-bar__actions">
                <el-button size="small" @click="router.push('/pages/privacy')">编辑隐私说明</el-button>
                <el-button
                  v-if="isAdmin && configs.PRIVACY?.hasUnpublishedChanges"
                  size="small"
                  type="success"
                  :loading="publishing"
                  @click="handlePublishConfigKey('PRIVACY')"
                >
                  发布隐私说明
                </el-button>
              </div>
            </div>
            <div class="pw-content-body">
              <ArticleBody :html="configs.PRIVACY?.draft?.bodyHtml" title="隐私说明" :show-toc="false" />
            </div>
          </template>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
.preview-workbench {
  min-height: 100vh;
  background: #f1f5f9;
  display: flex;
  flex-direction: column;
}

.pw-topbar {
  height: 56px;
  padding: 0 20px;
  background: #0f172a;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  position: sticky;
  top: 0;
  z-index: 40;
}

.pw-topbar__left,
.pw-topbar__right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pw-topbar__divider {
  color: rgba(255, 255, 255, 0.25);
}

.pw-topbar__title {
  font-size: 14px;
}

.pw-ext-link {
  color: #93c5fd;
  font-size: 13px;
  text-decoration: none;
}

.pw-ext-link:hover {
  text-decoration: underline;
}

.pw-main-layout {
  flex: 1;
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  min-height: calc(100vh - 56px);
}

.pw-sidebar {
  background: #ffffff;
  border-right: 1px solid #e2e8f0;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.pw-sidebar__heading {
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 12px;
}

.pw-nav-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pw-nav-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #ffffff;
  font-size: 13px;
  font-weight: 500;
  color: #334155;
  cursor: pointer;
  text-align: left;
}

.pw-nav-btn:hover {
  border-color: #93c5fd;
  background: #f8fafc;
}

.pw-nav-btn.is-active {
  border-color: #2563eb;
  background: #eff6ff;
  color: #1d4ed8;
  font-weight: 600;
}

.pw-health-ok {
  padding: 12px;
  background: #ecfdf5;
  border: 1px solid #a7f3d0;
  border-radius: 8px;
  color: #047857;
  font-size: 13px;
}

.pw-health-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pw-health-item {
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #f8fafc;
}

.pw-health-item__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.pw-health-item__msg {
  margin: 0;
  font-size: 12.5px;
  color: #475569;
}

.pw-stage-wrap {
  padding: 24px;
  overflow-y: auto;
  display: flex;
  justify-content: center;
}

.pw-canvas {
  width: 100%;
  max-width: 1200px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.05);
  transition: max-width 0.25s ease;
}

.pw-canvas.is-tablet {
  max-width: 820px;
}

.pw-canvas.is-mobile {
  max-width: 390px;
}

.pw-module-bar {
  padding: 16px 24px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.pw-module-bar__sub {
  margin-left: 10px;
  font-size: 12px;
  color: #64748b;
}

.pw-module-bar__actions {
  display: flex;
  gap: 8px;
}

.pw-content-banner {
  padding: 48px 36px;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  color: #ffffff;
}

.pw-content-banner__meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: #94a3b8;
  margin-bottom: 14px;
}

.pw-content-banner__title {
  font-size: 28px;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 12px;
}

.pw-content-banner__sub {
  font-size: 15px;
  color: #cbd5e1;
  margin: 0;
}

.pw-content-body,
.pw-home-body {
  padding: 32px 36px;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.pw-hero {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.pw-hero-slide {
  padding: 28px;
  border-radius: 10px;
  background: #0f172a;
  color: #ffffff;
}

.pw-hero-slide h2 {
  color: #ffffff;
  margin: 8px 0;
  font-size: 22px;
}

.pw-hero-slide p {
  color: #cbd5e1;
  font-size: 14px;
  margin: 0 0 16px;
}

.pw-hero-slide__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pw-struct-sec h2 {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 16px;
  color: #0f172a;
}

.pw-sec-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.pw-sec-head h2 {
  margin: 0;
}

.pw-grid-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.pw-grid-3 {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.pw-grid-4 {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.pw-card {
  padding: 18px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #f8fafc;
}

.pw-card h3 {
  font-size: 15px;
  margin: 0 0 8px;
  color: #0f172a;
}

.pw-card p {
  font-size: 13px;
  color: #475569;
  margin: 0;
  line-height: 1.6;
}

.pw-badge {
  display: inline-block;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
  background: #eff6ff;
  color: #2563eb;
  margin-bottom: 6px;
}

.pw-metric {
  padding: 16px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  text-align: center;
}

.pw-metric strong {
  display: block;
  font-size: 24px;
  color: #2563eb;
}

.pw-metric span {
  font-size: 12.5px;
  color: #64748b;
}

.pw-quote {
  margin: 0;
  padding: 20px 24px;
  border-left: 4px solid #2563eb;
  background: #f8fafc;
  border-radius: 0 8px 8px 0;
}

.pw-subitems {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.pw-subtag {
  font-size: 12px;
  padding: 2px 8px;
  background: #ffffff;
  border: 1px solid #cbd5e1;
  border-radius: 4px;
}

.pw-empty-block {
  padding: 36px;
  text-align: center;
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
  color: #64748b;
}

.pw-muted {
  color: #94a3b8;
  font-size: 13px;
}

@media (max-width: 960px) {
  .pw-main-layout {
    grid-template-columns: 1fr;
  }
  .pw-grid-2,
  .pw-grid-3,
  .pw-grid-4 {
    grid-template-columns: 1fr;
  }
}
</style>
