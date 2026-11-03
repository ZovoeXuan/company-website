<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ApiError, get } from '@/api/http'
import ArticleBody from '@shared/ArticleBody.vue'

interface PreviewMedia {
  url: string
  width?: number
  height?: number
  alt?: string
}

interface PreviewRelated {
  id: string
  contentType: string
  title: string
}

interface TitledItem {
  title: string
  description?: string
}

interface PreviewData {
  id: string
  contentType: string
  status: string
  title: string
  summary: string
  bodyHtml: string
  cover: PreviewMedia | null
  hero: PreviewMedia | null
  logo: PreviewMedia | null
  categoryName: string | null
  features?: TitledItem[]
  capabilities?: string[]
  outcomes?: Array<{ displayValue: string; label: string }>
  pains?: TitledItem[]
  benefits?: TitledItem[]
  quoteText?: string
  quoteAuthor?: string
  customerName?: string
  industry?: string
  displayTime?: string
  author?: string
  related: PreviewRelated[]
}

const route = useRoute()
const router = useRouter()

const data = ref<PreviewData | null>(null)
const loading = ref(true)
const error = ref('')

const typeLabels: Record<string, string> = {
  PRODUCT: '产品',
  SOLUTION: '解决方案',
  CASE: '客户案例',
  NEWS: '新闻',
  CUSTOMER: '客户标识',
}

const relatedLabels: Record<string, string> = {
  PRODUCT: '产品',
  SOLUTION: '方案',
  CASE: '案例',
  NEWS: '新闻',
  CUSTOMER: '客户',
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    data.value = await get<PreviewData>(`/api/admin/preview/contents/${route.params.id}`)
  } catch (err) {
    data.value = null
    error.value = err instanceof ApiError ? err.message : '预览加载失败'
  } finally {
    loading.value = false
  }
}

function backToEdit() {
  router.push(`/contents/${route.params.slug}/${route.params.id}/edit`)
}

onMounted(load)
watch(() => route.params.id, load)
</script>

<template>
  <div class="preview">
    <header class="bar">
      <span class="bar__badge">草稿预览</span>
      <p class="bar__text">
        当前显示未发布草稿；关联内容使用各自的线上版本。咨询按钮在预览中不可用。
      </p>
      <div class="bar__actions">
        <el-button size="small" @click="load">刷新</el-button>
        <el-button size="small" type="primary" @click="backToEdit">返回编辑</el-button>
      </div>
    </header>

    <div v-if="loading" class="state">加载中…</div>
    <div v-else-if="error" class="state state--error">{{ error }}</div>

    <div v-else-if="data" class="canvas">
      <article class="post">
        <div class="post__meta">
          <span>{{ typeLabels[data.contentType] ?? data.contentType }}</span>
          <span v-if="data.categoryName">· {{ data.categoryName }}</span>
          <span v-if="data.industry">· {{ data.industry }}</span>
          <span v-if="data.displayTime">· {{ data.displayTime }}</span>
          <span v-if="data.customerName">· {{ data.customerName }}</span>
          <span class="post__status">· {{ data.status === 'PUBLISHED' ? '已发布内容，当前显示草稿' : '尚未发布' }}</span>
        </div>

        <h1 class="post__title">{{ data.title || '（未填写标题）' }}</h1>
        <p v-if="data.summary" class="post__summary">{{ data.summary }}</p>

        <!-- 客户标识只有 logo，没有正文 -->
        <div v-if="data.contentType === 'CUSTOMER'" class="post__logo">
          <img v-if="data.logo" :src="data.logo.url" :alt="data.title" />
          <p v-else class="post__placeholder">尚未选择 Logo</p>
        </div>

        <template v-else>
          <ArticleBody
            :cover="data.cover"
            :capabilities="(data.capabilities ?? data.features ?? []).map((item) => typeof item === 'string' ? item : item.title)"
            :body-html="data.bodyHtml"
            :hide-empty-body="false"
          >
            <section v-if="data.pains?.length" class="block">
              <h2 class="block__title">面临的挑战</h2>
              <ul class="block__list">
                <li v-for="item in data.pains" :key="item.title">
                  <strong>{{ item.title }}</strong>
                  <span v-if="item.description">：{{ item.description }}</span>
                </li>
              </ul>
            </section>

            <section v-if="data.benefits?.length" class="block">
              <h2 class="block__title">带来的改变</h2>
              <ul class="block__list">
                <li v-for="item in data.benefits" :key="item.title">
                  <strong>{{ item.title }}</strong>
                  <span v-if="item.description">：{{ item.description }}</span>
                </li>
              </ul>
            </section>

            <dl v-if="data.outcomes?.length" class="metrics">
              <div v-for="item in data.outcomes" :key="item.label" class="metrics__item">
                <dt class="metrics__label">{{ item.label }}</dt>
                <dd class="metrics__value">{{ item.displayValue }}</dd>
              </div>
            </dl>

            <blockquote v-if="data.quoteText" class="quote">
              <p>{{ data.quoteText }}</p>
              <footer v-if="data.quoteAuthor">—— {{ data.quoteAuthor }}</footer>
            </blockquote>

            <section v-if="data.related.length" class="block">
              <h2 class="block__title">关联内容（各取线上版本）</h2>
              <ul class="block__list">
                <li v-for="item in data.related" :key="item.id">
                  <span class="tag">{{ relatedLabels[item.contentType] ?? item.contentType }}</span>
                  {{ item.title }}
                </li>
              </ul>
            </section>

            <!-- 与官网详情页位置一致，但预览中禁用，避免测试咨询进入正式线索 -->
            <div class="cta">
              <button type="button" class="cta__btn" disabled title="草稿预览中不可点击">预约演示</button>
              <p class="cta__note">发布后在官网详情页可正常提交咨询。</p>
            </div>
          </ArticleBody>
        </template>
      </article>
    </div>
  </div>
</template>

<style scoped>
.preview {
  min-height: 100vh;
  background: #f5f7fa;
}

.bar {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  padding: 10px 20px;
  background: #1f2937;
  color: #fff;
}

.bar__badge {
  padding: 3px 10px;
  border-radius: 999px;
  background: #f59e0b;
  color: #1f2937;
  font-size: 12px;
  font-weight: 600;
}

.bar__text {
  margin: 0;
  font-size: 13px;
  color: #d1d5db;
  flex: 1;
  min-width: 240px;
}

.bar__actions {
  display: flex;
  gap: 8px;
}

.state {
  padding: 80px 24px;
  text-align: center;
  color: #6b7280;
}

.state--error {
  color: #b91c1c;
}

.canvas {
  padding: 40px 24px 80px;
}

.post {
  max-width: 780px;
  margin: 0 auto;
  padding: 40px 44px 56px;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 2px 12px rgba(16, 30, 48, 0.06);
}

.post__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
}

.post__status {
  color: #b45309;
}

.post__title {
  margin: 12px 0 0;
  font-size: 30px;
  line-height: 1.4;
  font-weight: 600;
  color: #111827;
  overflow-wrap: anywhere;
}

.post__summary {
  margin: 14px 0 28px;
  font-size: 16px;
  line-height: 1.8;
  color: #4b5563;
}

.post__logo {
  margin-top: 28px;
}

.post__logo img {
  max-width: 240px;
  max-height: 120px;
  object-fit: contain;
}

.post__placeholder {
  color: #9ca3af;
  font-size: 14px;
}

.block {
  margin-top: 44px;
}

.block__title {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 14px;
}

.block__list {
  margin: 0;
  padding-left: 1.3em;
  font-size: 15px;
  line-height: 1.9;
  color: #374151;
}

.metrics {
  display: flex;
  flex-wrap: wrap;
  gap: 32px;
  margin: 36px 0 0;
  padding: 20px 0;
  border-top: 1px solid #e5e7eb;
  border-bottom: 1px solid #e5e7eb;
}

.metrics__item {
  min-width: 120px;
}

.metrics__label {
  font-size: 13px;
  color: #6b7280;
}

.metrics__value {
  margin: 4px 0 0;
  font-size: 28px;
  font-weight: 600;
  color: #111827;
}

.quote {
  margin: 36px 0 0;
  padding-left: 18px;
  border-left: 3px solid #d1d5db;
  font-size: 15px;
  line-height: 1.9;
  color: #374151;
}

.quote p {
  margin: 0;
}

.quote footer {
  margin-top: 8px;
  font-size: 13px;
  color: #6b7280;
}

.tag {
  display: inline-block;
  margin-right: 6px;
  padding: 1px 8px;
  border-radius: 3px;
  background: #eef2f7;
  font-size: 12px;
  color: #4b5563;
}

.cta {
  margin-top: 48px;
  padding-top: 28px;
  border-top: 1px solid #e5e7eb;
}

.cta__btn {
  padding: 10px 22px;
  border-radius: 4px;
  border: 1px solid #d1d5db;
  background: #f3f4f6;
  color: #9ca3af;
  font-size: 14px;
  cursor: not-allowed;
}

.cta__note {
  margin: 8px 0 0;
  font-size: 12px;
  color: #9ca3af;
}

@media (max-width: 700px) {
  .canvas {
    padding: 20px 12px 48px;
  }
  .post {
    padding: 24px 18px 40px;
  }
  .post__title {
    font-size: 24px;
  }
}
</style>
