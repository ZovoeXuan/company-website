<script setup lang="ts">
interface Crumb {
  label: string
  path?: string
}

withDefaults(
  defineProps<{
    title: string
    description?: string
    crumbs?: Crumb[]
    tone?: 'dark' | 'light'
    centered?: boolean
    eyebrow?: string
  }>(),
  { tone: 'dark', centered: false, eyebrow: '' },
)
</script>

<template>
  <header class="page-head" :class="[`page-head--${tone}`, { 'is-centered': centered }]" :aria-labelledby="'page-title'">
    <div class="page-head__glow" aria-hidden="true"></div>
    <div class="page-head__grid" aria-hidden="true"></div>
    <div class="container page-head__inner">
      <nav v-if="crumbs?.length" class="crumbs" aria-label="面包屑">
        <ol class="crumbs__list">
          <li v-for="(crumb, index) in crumbs" :key="crumb.label">
            <RouterLink v-if="crumb.path" :to="crumb.path" class="crumbs__link">{{ crumb.label }}</RouterLink>
            <span v-else aria-current="page">{{ crumb.label }}</span>
            <span v-if="index < crumbs.length - 1" class="crumbs__sep" aria-hidden="true">/</span>
          </li>
        </ol>
      </nav>

      <p v-if="eyebrow" class="kicker page-head__kicker">{{ eyebrow }}</p>
      <h1 id="page-title" class="page-head__title">{{ title }}</h1>
      <p v-if="description" class="page-head__desc">{{ description }}</p>
      <slot />
    </div>
  </header>
</template>

<style scoped>
.page-head {
  position: relative;
  overflow: hidden;
  padding: 128px 0 96px;
}

.page-head--dark {
  background: var(--c-navy);
  color: var(--c-on-dark);
}

.page-head--light {
  background: var(--c-mist);
  color: var(--c-text);
  padding: 112px 0 88px;
}

.page-head__glow {
  position: absolute;
  width: 1200px;
  height: 620px;
  left: 8%;
  top: -40%;
  background: radial-gradient(closest-side, rgba(91, 75, 232, 0.4), transparent 72%);
  filter: blur(20px);
  pointer-events: none;
}

.page-head--light .page-head__glow {
  background: radial-gradient(closest-side, rgba(91, 75, 232, 0.14), transparent 72%);
}

.page-head__grid {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(255, 255, 255, 0.045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.045) 1px, transparent 1px);
  background-size: 96px 96px;
  mask-image: linear-gradient(to bottom, rgba(0, 0, 0, 0.7), transparent 85%);
  pointer-events: none;
}

.page-head--light .page-head__grid {
  background-image: linear-gradient(rgba(16, 16, 20, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(16, 16, 20, 0.05) 1px, transparent 1px);
}

.page-head__inner {
  position: relative;
  z-index: 1;
}

.crumbs {
  margin-bottom: 40px;
}

.crumbs__list {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  list-style: none;
  margin: 0;
  padding: 0;
  font-size: 13px;
  color: var(--c-on-dark-muted);
}

.page-head--light .crumbs__list {
  color: var(--c-text-muted);
}

.crumbs__link:hover {
  color: #fff;
}

.page-head--light .crumbs__link:hover {
  color: var(--c-brand);
}

.crumbs__sep {
  color: rgba(255, 255, 255, 0.3);
}

.page-head--light .crumbs__sep {
  color: var(--c-border-strong);
}

.page-head__kicker {
  margin-bottom: 18px;
}

.page-head__title {
  font-size: clamp(44px, 7vw, 88px);
  line-height: 1.04;
  font-weight: 600;
  letter-spacing: -0.045em;
  color: var(--c-on-dark);
  max-width: 1120px;
}

.page-head--light .page-head__title {
  color: var(--c-text);
}

.page-head__desc {
  margin-top: 26px;
  font-size: 17px;
  color: var(--c-on-dark-muted);
  max-width: 680px;
  line-height: 1.85;
}

.page-head--light .page-head__desc {
  color: var(--c-text-muted);
}

.is-centered {
  text-align: center;
}

.is-centered .crumbs__list {
  justify-content: center;
}

.is-centered .page-head__title,
.is-centered .page-head__desc {
  margin-left: auto;
  margin-right: auto;
}

.is-centered .kicker {
  justify-content: center;
}

@media (max-width: 860px) {
  .page-head {
    padding: 88px 0 64px;
  }
  .page-head--light {
    padding: 80px 0 60px;
  }
  .crumbs {
    margin-bottom: 28px;
  }
  .page-head__title {
    font-size: clamp(34px, 10vw, 54px);
  }
  .page-head__desc {
    margin-top: 18px;
    font-size: 15px;
  }
}
</style>
