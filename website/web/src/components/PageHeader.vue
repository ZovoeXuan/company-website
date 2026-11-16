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
    bgType?: 'default' | 'image' | 'video'
    bgImage?: string
    bgVideo?: string
    overlayOpacity?: number
  }>(),
  { tone: 'dark', centered: false, eyebrow: '', bgType: 'default', overlayOpacity: 0.5 },
)
</script>

<template>
  <header
    class="page-head"
    :class="[
      `page-head--${tone}`,
      {
        'is-centered': centered,
        'has-media-bg': (bgType === 'image' && !!bgImage) || (bgType === 'video' && !!bgVideo),
      },
    ]"
    :aria-labelledby="'page-title'"
  >
    <video
      v-if="bgType === 'video' && bgVideo"
      class="page-head__media-video"
      :src="bgVideo"
      autoplay
      muted
      loop
      playsinline
    ></video>
    <div
      v-else-if="bgType === 'image' && bgImage"
      class="page-head__media-image"
      :style="{ backgroundImage: `url(${bgImage})` }"
    ></div>
    <template v-else>
      <div class="page-head__glow" aria-hidden="true"></div>
      <div class="page-head__grid" aria-hidden="true"></div>
    </template>
    <div
      v-if="(bgType === 'image' && !!bgImage) || (bgType === 'video' && !!bgVideo)"
      class="page-head__overlay"
      :style="{ opacity: overlayOpacity ?? 0.55 }"
      aria-hidden="true"
    ></div>

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
  padding: 112px 0 64px;
}

.page-head--dark {
  background: var(--c-navy);
  color: var(--c-on-dark);
}

.page-head--light {
  background: var(--c-mist);
  color: var(--c-text);
  padding: 104px 0 56px;
}

.page-head__glow {
  position: absolute;
  width: 1200px;
  height: 620px;
  left: 8%;
  top: -40%;
  background: radial-gradient(closest-side, rgba(37, 99, 235, 0.28), transparent 72%);
  filter: blur(20px);
  pointer-events: none;
}

.page-head--light .page-head__glow {
  background: radial-gradient(closest-side, rgba(37, 99, 235, 0.1), transparent 72%);
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
  font-size: clamp(30px, 4.2vw, 48px);
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

.page-head__media-video {
  position: absolute;
  top: 50%;
  left: 50%;
  min-width: 100%;
  min-height: 100%;
  width: auto;
  height: auto;
  transform: translate(-50%, -50%);
  object-fit: cover;
  z-index: 0;
  pointer-events: none;
}

.page-head__media-image {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  z-index: 0;
}

.page-head__overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(8, 10, 18, 0.72) 0%, rgba(8, 10, 18, 0.85) 100%);
  z-index: 0;
  pointer-events: none;
}

.has-media-bg {
  color: #ffffff !important;
}

.has-media-bg .page-head__title {
  color: #ffffff !important;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.45);
}

.has-media-bg .page-head__desc {
  color: rgba(255, 255, 255, 0.9) !important;
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.35);
}

.has-media-bg .crumbs__list {
  color: rgba(255, 255, 255, 0.75) !important;
}

.has-media-bg .crumbs__link:hover {
  color: #ffffff !important;
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
