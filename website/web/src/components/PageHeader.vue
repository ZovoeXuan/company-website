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
  <div class="page-head">
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
.page-head {
  background: linear-gradient(180deg, #f4f8fc 0%, #ffffff 100%);
  border-bottom: 1px solid var(--c-border);
  padding: 32px 0 48px;
}

.crumbs {
  margin-bottom: 20px;
}

.crumbs__list {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  list-style: none;
  margin: 0;
  padding: 0;
  font-size: 13px;
  color: var(--c-text-muted);
}

.crumbs__link:hover {
  color: var(--c-brand);
}

.crumbs__sep {
  color: var(--c-text-subtle);
}

.page-head__title {
  font-size: 36px;
  line-height: 1.3;
  font-weight: 500;
  letter-spacing: -0.01em;
}

.page-head__desc {
  margin-top: 12px;
  font-size: 16px;
  color: var(--c-text-muted);
  max-width: 720px;
  line-height: 1.8;
}

@media (max-width: 700px) {
  .page-head {
    padding: 24px 0 36px;
  }
  .page-head__title {
    font-size: 26px;
  }
  .page-head__desc {
    font-size: 15px;
  }
}
</style>
