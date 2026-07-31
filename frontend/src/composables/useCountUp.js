import { ref, watch, onBeforeUnmount } from 'vue'

const easeOutCubic = (t) => 1 - Math.pow(1 - t, 3)

export function useCountUp(source, options = {}) {
  const { duration = 800, decimals = 0 } = options
  const display = ref(0)
  let rafId = null
  let startTime = 0
  let fromValue = 0
  let toValue = 0

  const factor = Math.pow(10, decimals)

  const tick = (now) => {
    const elapsed = now - startTime
    const progress = Math.min(elapsed / duration, 1)
    const eased = easeOutCubic(progress)
    const current = fromValue + (toValue - fromValue) * eased
    display.value = Math.round(current * factor) / factor
    if (progress < 1) {
      rafId = requestAnimationFrame(tick)
    } else {
      rafId = null
    }
  }

  const animateTo = (next) => {
    if (typeof next !== 'number' || Number.isNaN(next)) return
    if (rafId) cancelAnimationFrame(rafId)
    fromValue = display.value
    toValue = next
    startTime = performance.now()
    rafId = requestAnimationFrame(tick)
  }

  const stop = watch(
    () => (typeof source === 'function' ? source() : source.value),
    (next) => animateTo(Number(next) || 0),
    { immediate: true }
  )

  onBeforeUnmount(() => {
    if (rafId) cancelAnimationFrame(rafId)
    stop()
  })

  return display
}
