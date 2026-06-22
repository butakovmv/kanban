import { ref } from 'vue'

const THEME_KEY = 'kanban_theme'

const isDark = ref(false)

function applyTheme() {
  document.documentElement.classList.remove('light', 'dark')
  document.documentElement.classList.add(isDark.value ? 'dark' : 'light')
}

function toggleTheme() {
  isDark.value = !isDark.value
  localStorage.setItem(THEME_KEY, isDark.value ? 'dark' : 'light')
  applyTheme()
}

function initTheme() {
  const saved = localStorage.getItem(THEME_KEY)
  if (saved === 'dark') {
    isDark.value = true
  } else if (saved === 'light') {
    isDark.value = false
  } else {
    isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches
  }
  applyTheme()
}

export function useTheme() {
  return {
    isDark,
    toggleTheme,
    initTheme,
  }
}
