<template>
  <el-dropdown trigger="click" @command="handleThemeChange">
    <div class="theme-trigger">
      <span class="theme-icon">{{ currentThemeInfo.icon }}</span>
      <span class="theme-name">{{ currentThemeInfo.name }}</span>
      <el-icon class="arrow"><ArrowDown /></el-icon>
    </div>
    <template #dropdown>
      <el-dropdown-menu class="theme-dropdown">
        <el-dropdown-item
          v-for="theme in themeList"
          :key="theme.key"
          :command="theme.key"
          :class="{ 'is-active': theme.key === currentTheme }"
        >
          <span class="theme-option-icon">{{ theme.icon }}</span>
          <span class="theme-option-name">{{ theme.name }}</span>
          <el-icon v-if="theme.key === currentTheme" class="check-icon"><Check /></el-icon>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { computed } from 'vue'
import { useThemeStore, THEMES } from '@/stores/theme'

const themeStore = useThemeStore()

const currentTheme = computed(() => themeStore.currentTheme)

const currentThemeInfo = computed(() => {
  return THEMES[currentTheme.value] || THEMES.minimal
})

const themeList = computed(() => Object.values(THEMES))

const handleThemeChange = (themeKey) => {
  themeStore.setTheme(themeKey)
}
</script>

<style lang="scss" scoped>
.theme-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
  background: var(--primary-bg);
  border: 1px solid var(--border-color);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  &:hover {
    background: var(--primary-bg);
    border-color: var(--primary-color);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-1px);
  }

  .theme-icon {
    font-size: 18px;
    filter: drop-shadow(0 1px 2px rgba(0,0,0,0.1));
  }

  .theme-name {
    font-size: 13px;
    font-weight: 600;
    color: var(--text-primary);
    letter-spacing: 0.3px;
  }

  .arrow {
    font-size: 12px;
    color: var(--text-muted);
    transition: transform 0.2s ease;
  }
  
  &:hover .arrow {
    transform: rotate(180deg);
  }
}

.theme-dropdown {
  min-width: 180px;

  :deep(.el-dropdown-menu__item) {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 18px;
    transition: all 0.2s ease;

    &.is-active {
      background: var(--primary-bg);
      color: var(--primary-color);
      font-weight: 600;
    }

    .theme-option-icon {
      font-size: 20px;
      filter: drop-shadow(0 2px 3px rgba(0,0,0,0.15));
    }

    .theme-option-name {
      flex: 1;
      font-size: 14px;
    }

    .check-icon {
      color: var(--primary-color);
      font-size: 16px;
    }
  }
}
</style>
