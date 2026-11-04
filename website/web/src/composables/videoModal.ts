import { reactive } from 'vue'

export interface VideoModalState {
  open: boolean
  title: string
  url: string
}

const state = reactive<VideoModalState>({
  open: false,
  title: '',
  url: '',
})

export function openVideo(params: { title: string; url: string }) {
  state.title = params.title
  state.url = params.url
  state.open = true
}

export function closeVideo() {
  state.open = false
  state.url = ''
  state.title = ''
}

export function useVideoModal() {
  return {
    state,
    openVideo,
    closeVideo,
  }
}
