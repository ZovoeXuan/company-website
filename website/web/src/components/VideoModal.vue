<script setup lang="ts">
import { nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { useVideoModal } from '@/composables/videoModal'

const { state, closeVideo } = useVideoModal()
const videoRef = ref<HTMLVideoElement | null>(null)

function onKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape' && state.open) {
    closeVideo()
  }
}

watch(
  () => state.open,
  async (isOpen) => {
    if (isOpen) {
      document.addEventListener('keydown', onKeydown)
      document.body.style.overflow = 'hidden'
      await nextTick()
      videoRef.value?.play().catch(() => {})
    } else {
      document.removeEventListener('keydown', onKeydown)
      document.body.style.overflow = ''
      if (videoRef.value) {
        videoRef.value.pause()
      }
    }
  },
)

onBeforeUnmount(() => {
  document.removeEventListener('keydown', onKeydown)
  document.body.style.overflow = ''
})
</script>

<template>
  <Teleport to="body">
    <div v-if="state.open" class="video-overlay" @click.self="closeVideo">
      <div class="video-dialog" role="dialog" aria-modal="true" :aria-label="state.title || '视频播放'">
        <div class="video-dialog__head">
          <span class="video-dialog__title">{{ state.title || '视频介绍' }}</span>
          <button type="button" class="video-dialog__close" aria-label="关闭视频" @click="closeVideo">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="video-dialog__body">
          <video
            ref="videoRef"
            :src="state.url"
            controls
            autoplay
            playsinline
            class="video-dialog__player"
          ></video>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.video-overlay {
  position: fixed;
  inset: 0;
  z-index: 100;
  background: rgba(10, 12, 20, 0.85);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.video-dialog {
  width: min(920px, 100%);
  background: #111319;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.12);
  display: flex;
  flex-direction: column;
}

.video-dialog__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: #181b24;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.video-dialog__title {
  color: #f1f3f9;
  font-size: 14px;
  font-weight: 500;
}

.video-dialog__close {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.65);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px;
  border-radius: 6px;
  transition: color 0.2s ease, background-color 0.2s ease;
}

.video-dialog__close:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

.video-dialog__body {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  background: #000;
}

.video-dialog__player {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: contain;
}
</style>
