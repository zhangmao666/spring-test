import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const THEMES = {
  professional: {
    key: 'professional',
    name: '专业现代',
    icon: 'system'
  }
}

const DEFAULT_THEME = 'professional'

export const useThemeStore = defineStore('theme', () => {
  const currentTheme = ref(localStorage.getItem('app-theme') || DEFAULT_THEME)

  function applyTheme(themeKey) {
    document.documentElement.setAttribute('data-theme', themeKey || DEFAULT_THEME)
  }

  function setTheme(themeKey) {
    if (!THEMES[themeKey]) {
      return
    }

    currentTheme.value = themeKey
    localStorage.setItem('app-theme', themeKey)
    applyTheme(themeKey)
  }

  function initTheme() {
    if (!THEMES[currentTheme.value]) {
      currentTheme.value = DEFAULT_THEME
      localStorage.setItem('app-theme', DEFAULT_THEME)
    }

    applyTheme(currentTheme.value)
  }

  watch(currentTheme, (newTheme) => {
    applyTheme(newTheme)
  })

  return {
    currentTheme,
    setTheme,
    initTheme
  }
})
