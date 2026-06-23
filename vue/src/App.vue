<script setup lang="ts">
/**
 * Корневой компонент приложения.
 * Содержит верхнюю навигационную панель с логотипом и ссылками,
 * а также `<RouterView />` для отображения страниц-потомков.
 */
import { computed, onMounted, watch } from 'vue'
import { RouterView, RouterLink, useRouter, useRoute } from 'vue-router'
import { useAuthStore } from './module/auth/store'
import { useProjectStore } from './module/project/store'
import { useTheme } from './composables/useTheme'
import { storeToRefs } from 'pinia'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const projectStore = useProjectStore()
const { isAuthenticated, user } = storeToRefs(authStore)
const { isDark, toggleTheme, initTheme } = useTheme()
initTheme()

const pageLabels: Record<string, string> = {
  'project-board': 'Board',
  'project-documents': 'Documents',
  'project-reports': 'Reports',
  'project-settings': 'Settings',
  'document-detail': 'Documents',
}

const projectIdOnPage = computed(() => {
  const id = route.params['id']
  return Array.isArray(id) ? id[0] : id
})

const currentPageName = computed(() => {
  const name = route.name
  return typeof name === 'string' ? pageLabels[name] : undefined
})

function loadProjectForNav() {
  const id = projectIdOnPage.value
  if (id && currentPageName.value) {
    projectStore.loadProject(id)
  }
}

onMounted(loadProjectForNav)
watch(() => route.fullPath, loadProjectForNav)

async function handleLogout() {
  await authStore.logout()
  await router.push('/login')
}
</script>

<template>
  <div class="app">
    <nav class="nav">
      <RouterLink to="/" class="nav__logo">Kanban</RouterLink>
      <template v-if="projectStore.currentProject && currentPageName">
        <span class="nav__project-name">/ {{ projectStore.currentProject.name }}</span>
        <span class="nav__page-name">/ {{ currentPageName }}</span>
      </template>
      <span class="nav__spacer"></span>
      <div class="nav__links">
        <template v-if="isAuthenticated">
          <RouterLink to="/projects">Projects</RouterLink>
          <RouterLink to="/profile">{{ user?.displayName ?? 'Profile' }}</RouterLink>
          <a href="#" @click.prevent="handleLogout">Logout</a>
        </template>
        <template v-else>
          <RouterLink to="/login">Login</RouterLink>
          <RouterLink to="/register">Register</RouterLink>
        </template>
        <button class="nav__theme" @click="toggleTheme" :title="isDark ? 'Switch to light theme' : 'Switch to dark theme'">
          {{ isDark ? '☀️' : '🌙' }}
        </button>
      </div>
    </nav>
    <main class="main">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.5rem;
  height: 3.5rem;
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
}
.nav__logo {
  font-weight: 700;
  text-decoration: none;
  color: var(--color-primary);
}
.nav__project-name {
  margin-left: 0.5rem;
  font-weight: 600;
  color: var(--color-text);
}
.nav__page-name {
  margin-left: 0.25rem;
  color: var(--color-text-secondary);
}
.nav__spacer {
  flex: 1;
  min-width: 0;
}
.nav__links {
  display: flex;
  align-items: center;
  gap: 1rem;
}
.nav__links a {
  text-decoration: none;
  color: var(--color-text-secondary);
}
.nav__theme {
  padding: 0.25rem 0.5rem;
  background: none;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  font-size: 1rem;
  cursor: pointer;
}
.nav__theme:hover {
  background: var(--color-background);
}
.main {
}
</style>
