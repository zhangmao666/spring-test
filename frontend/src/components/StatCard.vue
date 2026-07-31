<template>
  <div
    class="stat-card"
    :class="[toneClass, { 'stat-card--interactive': interactive }]"
    v-motion
    :initial="{ opacity: 0, y: 12 }"
    :visibleOnce="{ opacity: 1, y: 0, transition: { duration: 320, delay: 60 + delay } }"
  >
    <div class="stat-card__head">
      <div class="stat-card__label-row">
        <span v-if="icon" class="stat-card__icon"><component :is="icon" /></span>
        <span class="stat-card__label">{{ label }}</span>
      </div>
      <span v-if="delta !== null && delta !== undefined" class="stat-card__delta" :class="deltaClass">
        <el-icon v-if="deltaIcon"><component :is="deltaIcon" /></el-icon>
        {{ formattedDelta }}
      </span>
    </div>

    <div class="stat-card__body">
      <div class="stat-card__value">
        <span class="stat-card__value-num">{{ formattedValue }}</span>
        <span v-if="unit" class="stat-card__unit">{{ unit }}</span>
      </div>
      <div v-if="$slots.chart" class="stat-card__chart"><slot name="chart" /></div>
    </div>

    <p v-if="hint" class="stat-card__hint">{{ hint }}</p>
  </div>
</template>

<script setup>
import { computed, toRef } from 'vue'
import { ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import { useCountUp } from '@/composables/useCountUp'

const props = defineProps({
  label: { type: String, required: true },
  value: { type: [Number, String], default: 0 },
  delta: { type: Number, default: null },
  unit: { type: String, default: '' },
  hint: { type: String, default: '' },
  icon: { type: [Object, Function], default: null },
  tone: { type: String, default: 'primary' },
  format: { type: String, default: 'number' },
  decimals: { type: Number, default: 0 },
  delay: { type: Number, default: 0 },
  animate: { type: Boolean, default: true },
  interactive: { type: Boolean, default: false }
})

const toneClass = computed(() => (props.tone === 'primary' ? '' : `tone-${props.tone}`))

const numericValue = computed(() => {
  const n = Number(props.value)
  return Number.isFinite(n) ? n : 0
})

const displayNumber = props.animate
  ? useCountUp(numericValue, { duration: 900, decimals: props.decimals })
  : numericValue

const formattedValue = computed(() => {
  if (typeof props.value === 'string' && !props.animate) return props.value
  const n = displayNumber.value
  if (props.format === 'percent') return `${n.toFixed(props.decimals)}%`
  if (props.format === 'duration') {
    const m = Math.floor(n / 60)
    const s = Math.floor(n % 60)
    return `${m}:${String(s).padStart(2, '0')}`
  }
  return n.toLocaleString('zh-CN', { maximumFractionDigits: props.decimals })
})

const deltaClass = computed(() => {
  if (props.delta > 0) return 'stat-card__delta--up'
  if (props.delta < 0) return 'stat-card__delta--down'
  return 'stat-card__delta--flat'
})

const deltaIcon = computed(() => {
  if (props.delta > 0) return ArrowUp
  if (props.delta < 0) return ArrowDown
  return null
})

const formattedDelta = computed(() => {
  if (props.delta === null || props.delta === undefined) return ''
  return `${Math.abs(props.delta)}%`
})
</script>

<style lang="scss" scoped>
.stat-card {
  position: relative;
  overflow: hidden;
  padding: 18px 20px;
  border-radius: 18px;
  border: 1px solid var(--border-subtle);
  background:
    linear-gradient(160deg, var(--accent-soft) 0%, transparent 55%),
    var(--surface-base);
  box-shadow: var(--shadow-sm);
  transition: transform 180ms ease, box-shadow 180ms ease, border-color 180ms ease;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-card::after {
  content: '';
  position: absolute;
  top: -60px;
  right: -60px;
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: radial-gradient(circle, var(--accent-soft), transparent 72%);
  pointer-events: none;
}

.stat-card--interactive {
  cursor: pointer;
}

.stat-card--interactive:hover,
.stat-card:hover {
  transform: translateY(-3px);
  border-color: var(--accent-border);
  box-shadow: var(--shadow-strong);
}

.stat-card__head {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.stat-card__label-row {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--text-muted);
  font-size: 0.86rem;
  font-weight: 600;
}

.stat-card__icon {
  display: inline-grid;
  place-items: center;
  width: 30px;
  height: 30px;
  border-radius: 10px;
  background: var(--accent-soft);
  color: var(--accent-strong);
  font-size: 1rem;
}

.stat-card__delta {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 8px;
  border-radius: 999px;
  font-size: 0.72rem;
  font-weight: 700;
  background: var(--surface-muted);
  color: var(--text-muted);
}

.stat-card__delta--up {
  background: rgba(16, 185, 129, 0.12);
  color: #047857;
}

.stat-card__delta--down {
  background: rgba(220, 38, 38, 0.12);
  color: #b91c1c;
}

.stat-card__body {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
}

.stat-card__value {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
  color: var(--text-primary);
  font-weight: 800;
  line-height: 1;
}

.stat-card__value-num {
  font-size: clamp(1.6rem, 2.2vw, 1.9rem);
  background: var(--accent-gradient);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stat-card__unit {
  color: var(--text-muted);
  font-size: 0.85rem;
  font-weight: 600;
}

.stat-card__chart {
  flex-shrink: 0;
  width: 96px;
  height: 32px;
}

.stat-card__hint {
  position: relative;
  z-index: 1;
  margin: 0;
  color: var(--text-muted);
  font-size: 0.8rem;
  line-height: 1.4;
}
</style>
