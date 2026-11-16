import type { Directive } from 'vue'

let observer: IntersectionObserver | null = null

function getObserver(): IntersectionObserver | null {
  if (typeof IntersectionObserver === 'undefined') return null
  if (!observer) {
    observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            entry.target.classList.add('is-revealed')
            observer?.unobserve(entry.target)
          }
        }
      },
      { threshold: 0.05, rootMargin: '0px 0px -2% 0px' },
    )
  }
  return observer
}

/**
 * v-reveal：渐进式入场动效指令。
 * 首屏内或无 IntersectionObserver 环境默认直接可见，绝不隐藏首屏内容。
 */
export const vReveal: Directive<HTMLElement, number | undefined> = {
  mounted(el, binding) {
    if (
      typeof window === 'undefined' ||
      typeof IntersectionObserver === 'undefined' ||
      window.matchMedia('(prefers-reduced-motion: reduce)').matches
    ) {
      el.classList.add('is-revealed')
      return
    }
    const rect = el.getBoundingClientRect()
    if (rect.top < window.innerHeight * 0.95) {
      el.classList.add('is-revealed')
      return
    }
    el.setAttribute('data-reveal', '')
    if (binding.value) {
      el.style.setProperty('--reveal-delay', `${Math.min(binding.value, 240)}ms`)
    }
    getObserver()?.observe(el)
  },
  unmounted(el) {
    observer?.unobserve(el)
  },
}
