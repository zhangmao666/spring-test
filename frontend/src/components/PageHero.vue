<template>
  <section
    class="page-hero"
    :class="[toneClass]"
    v-motion
    :initial="{ opacity: 0, y: 12 }"
    :enter="{ opacity: 1, y: 0, transition: { duration: 280, ease: [0.19, 1, 0.22, 1] } }"
  >
    <div class="page-hero__glow" aria-hidden="true"></div>
    <div class="page-hero__glow page-hero__glow--alt" aria-hidden="true"></div>

    <div class="page-hero__main">
      <div v-if="icon || eyebrow" class="page-hero__eyebrow">
        <span v-if="icon" class="page-hero__icon"><component :is="icon" /></span>
        <span v-if="eyebrow">{{ eyebrow }}</span>
      </div>
      <h1 class="page-hero__title">{{ title }}</h1>
      <p v-if="subtitle" class="page-hero__subtitle">{{ subtitle }}</p>
      <div v-if="$slots.extra" class="page-hero__extra"><slot name="extra" /></div>
    </div>

    <div v-if="$slots.actions" class="page-hero__actions"><slot name="actions" /></div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  eyebrow: { type: String, default: '' },
  icon: { type: [Object, Function], default: null },
  tone: {
    type: String,
    default: 'primary',
    validator: (v) => ['primary', 'ai', 'news', 'tools', 'system'].includes(v)
  }
})

const toneClass = computed(() => (props.tone === 'primary' ? '' : `tone-${props.tone}`))
</script>

<style lang="scss" scoped>
.page-hero {
  position: relative;
  overflow: hidden;
  padding: 22px 26px;
  border-radius: 24px;
  background:
    linear-gradient(135deg, var(--accent-soft), transparent 65%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(248, 250, 252, 0.9));
  border: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-sm);
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}

.page-hero__glow {
  position: absolute;
  top: -80px;
  right: -40px;
  width: 260px;
  height: 260px;
  border-radius: 50%;
  background: radial-gradient(circle, var(--accent-soft), transparent 68%);
  pointer-events: none;
  z-index: 0;
  animation: pageHeroFloat 12s ease-in-out infinite;
}

.page-hero__glow--alt {
  top: auto;
  right: auto;
  bottom: -100px;
  left: 30%;
  width: 200px;
  height: 200px;
  animation-duration: 16s;
  animation-direction: reverse;
  background: radial-gradient(circle, var(--accent-border), transparent 72%);
  opacity: 0.6;
}

@keyframes pageHeroFloat {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-14px, 10px); }
}

.page-hero__main {
  position: relative;
  z-index: 1;
  min-width: 0;
  flex: 1;
}

.page-hero__eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 999px;
  background: var(--accent-soft);
  color: var(--accent-strong);
  font-size: 0.74rem;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.page-hero__icon {
  display: inline-flex;
  font-size: 0.9rem;
}

.page-hero__title {
  margin: 12px 0 0;
  color: var(--text-primary);
  font-size: clamp(1.6rem, 2.4vw, 2rem);
  font-weight: 800;
  line-height: 1.15;
  background: var(--accent-gradient);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.page-hero__subtitle {
  margin: 10px 0 0;
  max-width: 720px;
  color: var(--text-muted);
  font-size: 0.95rem;
  line-height: 1.7;
}

.page-hero__extra {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.page-hero__actions {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
</style>
