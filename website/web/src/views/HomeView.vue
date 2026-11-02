<script setup lang="ts">
const metrics = [
  { value: '—', label: '服务客户' },
  { value: '—', label: '覆盖城市' },
  { value: '—', label: '在线设备' },
  { value: '—', label: '平均响应' },
]

const services = [
  { title: '服务一', desc: '此处由后台「首页管理 · 服务分组」维护，正式内容上线前为占位。' },
  { title: '服务二', desc: '卡片支持选择已发布产品并自定义标题与说明。' },
  { title: '服务三', desc: '模块可整体隐藏，不会留下空白区域。' },
]
</script>

<template>
  <div class="home">
    <section class="hero">
      <div class="container hero-inner">
        <div class="hero-copy">
          <p class="eyebrow">内容由后台维护 · 当前为占位</p>
          <h1>这里放一句话说明公司做什么</h1>
          <p class="hero-sub">
            首屏标题、副标题、两个按钮和背景图均由「首页管理 · 首屏」控制，保存草稿不会影响线上，发布后才生效。
          </p>
          <div class="hero-actions">
            <RouterLink to="/contact" class="btn primary">预约演示</RouterLink>
            <RouterLink to="/products" class="btn ghost">了解产品</RouterLink>
          </div>
        </div>
        <div class="hero-media">
          <span>首屏图 · 桌面 / 手机各一张</span>
        </div>
      </div>
    </section>

    <section class="metrics">
      <div class="container metrics-grid">
        <div v-for="item in metrics" :key="item.label" class="metric">
          <p class="metric-value">{{ item.value }}</p>
          <p class="metric-label">{{ item.label }}</p>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="container">
        <h2 class="section-title">核心服务</h2>
        <div class="cards">
          <article v-for="item in services" :key="item.title" class="card">
            <div class="card-media"></div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.desc }}</p>
          </article>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="container">
        <h2 class="section-title">客户案例</h2>
        <div class="cases">
          <article class="case">
            <div class="case-media"></div>
            <div>
              <h3>客户名称待填</h3>
              <p>成果数据由案例内容维护，例如「停机时间下降 37%」。</p>
            </div>
          </article>
          <article class="case">
            <div class="case-media"></div>
            <div>
              <h3>客户名称待填</h3>
              <p>首页只展示已发布且在后台被选为推荐的案例。</p>
            </div>
          </article>
        </div>
      </div>
    </section>

    <section class="cta-band">
      <div class="container cta-inner">
        <div>
          <h2>把这段文案换成公司的行动号召</h2>
          <p>咨询表单在「联系我们」页面，提交后进入后台咨询管理。</p>
        </div>
        <RouterLink to="/contact" class="btn primary">立即咨询</RouterLink>
      </div>
    </section>
  </div>
</template>

<style scoped>
.hero {
  background: linear-gradient(180deg, #f2f7fd 0%, #ffffff 100%);
  border-bottom: 1px solid var(--c-border);
}

.hero-inner {
  display: flex;
  gap: 40px;
  align-items: center;
  padding-top: 64px;
  padding-bottom: 64px;
}

.hero-copy {
  flex: 1;
  min-width: 0;
}

.eyebrow {
  font-size: 12px;
  color: var(--c-brand);
  margin-bottom: 14px;
}

h1 {
  font-size: 34px;
  line-height: 1.35;
  font-weight: 500;
  margin-bottom: 16px;
}

.hero-sub {
  font-size: 14px;
  color: var(--c-text-muted);
  line-height: 1.8;
  max-width: 520px;
  margin-bottom: 24px;
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  padding: 10px 20px;
  border-radius: 8px;
  border: 1px solid transparent;
}

.btn.primary {
  background: var(--c-brand);
  color: #fff;
}

.btn.ghost {
  border-color: var(--c-border);
  color: var(--c-text);
  background: #fff;
}

.hero-media {
  width: 320px;
  height: 200px;
  flex: none;
  border-radius: 12px;
  border: 1px dashed #b9cfe6;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: var(--c-text-muted);
}

.metrics {
  border-bottom: 1px solid var(--c-border);
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  padding-top: 28px;
  padding-bottom: 28px;
}

.metric-value {
  font-size: 24px;
  font-weight: 500;
}

.metric-label {
  font-size: 12px;
  color: var(--c-text-muted);
  margin-top: 4px;
}

.section {
  padding: 56px 0 8px;
}

.section-title {
  font-size: 20px;
  font-weight: 500;
  margin-bottom: 20px;
}

.cards {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.card {
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  padding: 18px;
}

.card-media {
  height: 96px;
  border-radius: 7px;
  background: var(--c-surface);
  margin-bottom: 14px;
}

.card h3 {
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 6px;
}

.card p {
  font-size: 13px;
  color: var(--c-text-muted);
  line-height: 1.7;
}

.cases {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.case {
  display: flex;
  gap: 14px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  padding: 14px;
}

.case-media {
  width: 92px;
  height: 68px;
  border-radius: 7px;
  background: var(--c-surface);
  flex: none;
}

.case h3 {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 6px;
}

.case p {
  font-size: 13px;
  color: var(--c-text-muted);
  line-height: 1.7;
}

.cta-band {
  margin-top: 64px;
  background: var(--c-surface);
  border-top: 1px solid var(--c-border);
  border-bottom: 1px solid var(--c-border);
  padding: 40px 0;
}

.cta-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
}

.cta-inner h2 {
  font-size: 20px;
  font-weight: 500;
  margin-bottom: 8px;
}

.cta-inner p {
  font-size: 13px;
  color: var(--c-text-muted);
}

@media (max-width: 900px) {
  .hero-inner {
    flex-direction: column;
    align-items: flex-start;
    padding-top: 40px;
    padding-bottom: 40px;
  }
  .hero-media {
    width: 100%;
    height: 160px;
  }
  h1 {
    font-size: 26px;
  }
  .cards {
    grid-template-columns: 1fr;
  }
  .cases {
    grid-template-columns: 1fr;
  }
  .metrics-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
