<template>
  <div class="empty-illustration">
    <div class="empty-illustration__art" :class="`empty-illustration__art--${variant}`">
      <EmptyChat v-if="variant === 'chat'" />
      <EmptyNews v-else-if="variant === 'news'" />
      <EmptyLogs v-else-if="variant === 'logs'" />
      <EmptyUsers v-else-if="variant === 'users'" />
      <EmptyInbox v-else />
    </div>
    <h3 v-if="title" class="empty-illustration__title">{{ title }}</h3>
    <p v-if="description" class="empty-illustration__desc">{{ description }}</p>
    <div v-if="$slots.action" class="empty-illustration__action"><slot name="action" /></div>
  </div>
</template>

<script setup>
import EmptyChat from './svg/EmptyChat.vue'
import EmptyNews from './svg/EmptyNews.vue'
import EmptyLogs from './svg/EmptyLogs.vue'
import EmptyUsers from './svg/EmptyUsers.vue'
import EmptyInbox from './svg/EmptyInbox.vue'

defineProps({
  variant: {
    type: String,
    default: 'inbox',
    validator: (v) => ['chat', 'news', 'logs', 'users', 'inbox'].includes(v)
  },
  title: { type: String, default: '暂无数据' },
  description: { type: String, default: '' }
})
</script>

<style lang="scss" scoped>
.empty-illustration {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 40px 20px;
  color: var(--text-muted);
  text-align: center;
}

.empty-illustration__art {
  width: 160px;
  height: 160px;
  animation: floatY 4s ease-in-out infinite;
}

.empty-illustration__art :deep(svg) {
  width: 100%;
  height: 100%;
}

@keyframes floatY {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.empty-illustration__title {
  margin: 8px 0 0;
  color: var(--text-primary);
  font-size: 1rem;
  font-weight: 700;
}

.empty-illustration__desc {
  margin: 0;
  max-width: 340px;
  color: var(--text-muted);
  font-size: 0.88rem;
  line-height: 1.6;
}

.empty-illustration__action {
  margin-top: 8px;
}
</style>
