import { ref } from 'vue'

const THEME_KEY = 'kanban_theme'

const prefersDark = window.matchMedia('(prefers-color-scheme: dark)')

export function useTheme() {
  const isDark = ref(false)

  function applyTheme() {
    if (isDark.value) {
      document.documentElement.style.colorScheme = 'dark'
    } else {
      document.documentElement.style.colorScheme = 'light'
    }
  }

  function toggleTheme() {
    isDark.value = !isDark.value
    localStorage.setItem(THEME_KEY, isDark.value ? 'dark' : 'light')
    applyTheme()
  }

  function initTheme() {
    const saved = localStorage.getItem(THEME_KEY)
    if (saved) {
      isDark.value = saved === 'dark'
    } else {
      isDark.value = prefersDark.matches
    }
    applyTheme()
  }

  initTheme()

  prefersDark.addEventListener('change', (e) => {
    if (!localStorage.getItem(THEME_KEY)) {
      isDark.value = e.matches
      applyTheme()
    }
  })

  return {
    isDark,
    toggleTheme,
    initTheme,
  }
}
