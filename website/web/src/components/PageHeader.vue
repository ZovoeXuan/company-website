<script setup lang="ts">
interface Crumb {
  label: string
  path?: string
}

defineProps<{
  title: string
  description?: string
  crumbs?: Crumb[]
}>()
</script>

<template>
  <div class="page-head on-dark">
    <div class="container">
      <nav v-if="crumbs?.length" class="crumbs" aria-label="面包屑">
        <ol class="crumbs__list">
          <li v-for="(crumb, index) in crumbs" :key="crumb.label">
            <RouterLink v-if="crumb.path" :to="crumb.path" class="crumbs__link">{{ crumb.label }}</RouterLink>
            <span v-else aria-current="page">{{ crumb.label }}</span>
            <span v-if="index < crumbs.length - 1" class="crumbs__sep" aria-hidden="true">/</span>
          </li>
        </ol>
      </nav>

      <h1 class="page-head__title">{{ title }}</h1>
      <p v-if="description" class="page-head__desc">{{ description }}</p>
    </div>
  </div>
</template>

<style scoped>
/* 内页页头：深蓝带 + 极淡的径向光，与首屏形成同一套暗色体系 */
.page-head {
  position: relative;
  overflow: hidden;
  background:
    radial-gradient(1100px 380px at 12% -10%, rgba(47, 107, 240, 0.28), transparent 70%),
    var(--c-navy);
  color: var(--c-on-dark);
  padding: 56px 0 64px;
}

.page-head::after {
  content: '';
  position: absolute;
  inset: auto 0 0 0;
  height: 1px;
  background: var(--c-on-dark-line);
}

.crumbs {
  margin-bottom: 26px;
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

.crumbs__link:hover {
  color: #fff;
}

.crumbs__sep {
  color: rgba(255, 255, 255, 0.35);
}

.page-head__title {
  font-size: 48px;
  line-height: 1.2;
  font-weight: 500;
  letter-spacing: -0.02em;
  color: var(--c-on-dark);
}

.page-head__desc {
  margin-top: 18px;
  font-size: 17px;
  color: var(--c-on-dark-muted);
  max-width: 760px;
  line-height: 1.8;
}

@media (max-width: 700px) {
  .page-head {
    padding: 36px 0 44px;
  }
  .page-head__title {
    font-size: 30px;
  }
  .page-head__desc {
    font-size: 15px;
  }
}
</style>
