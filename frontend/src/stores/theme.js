import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const THEMES = {
  minimal: {
    key: 'minimal',
    name: '极简主题',
    icon: '✨'
  },
  porcelain: {
    key: 'porcelain',
    name: '青花瓷',
    icon: '🏺'
  },
  anime: {
    key: 'anime',
    name: '清新樱花',
    icon: '🌸'
  },
  cyberpunk: {
    key: 'cyberpunk',
    name: '现代悦动',
    icon: '⚡'
  },
  liuyifei: {
    key: 'liuyifei',
    name: '雅致丁香',
    icon: '🌿'
  },
  zen: {
    key: 'zen',
    name: '极简禅意',
    icon: '🧘'
  },
  wealth: {
    key: 'wealth',
    name: '香槟财富',
    icon: '🥂'
  }
}

export const useThemeStore = defineStore('theme', () => {
  const currentTheme = ref(localStorage.getItem('app-theme') || 'minimal')

  function setTheme(themeKey) {
    if (THEMES[themeKey]) {
      currentTheme.value = themeKey
      localStorage.setItem('app-theme', themeKey)
      applyTheme(themeKey)
    }
  }

  function applyTheme(themeKey) {
    document.documentElement.setAttribute('data-theme', themeKey)
  }

  function initTheme() {
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
