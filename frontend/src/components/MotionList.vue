<template>
  <component :is="tag" class="motion-list">
    <div
      v-for="(item, i) in items"
      :key="itemKey(item, i)"
      v-motion
      :initial="{ opacity: 0, y: 12 }"
      :visibleOnce="{
        opacity: 1,
        y: 0,
        transition: { duration: 260, delay: i * stagger, ease: [0.19, 1, 0.22, 1] }
      }"
    >
      <slot :item="item" :index="i" />
    </div>
  </component>
</template>

<script setup>
const props = defineProps({
  items: { type: Array, required: true },
  tag: { type: String, default: 'div' },
  stagger: { type: Number, default: 60 },
  keyField: { type: String, default: '' }
})

const itemKey = (item, i) => {
  if (props.keyField && item && item[props.keyField] !== undefined) return item[props.keyField]
  if (item && item.id !== undefined) return item.id
  return i
}
</script>

<style lang="scss" scoped>
.motion-list {
  display: contents;
}
</style>
