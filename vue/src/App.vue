<script setup lang="ts">
/**
 * Корневой компонент приложения.
 * Содержит верхнюю навигационную панель с логотипом и ссылками,
 * а также `<RouterView />` для отображения страниц-потомков.
 */
import { RouterView, RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from './module/auth/store'
import { storeToRefs } from 'pinia'

const router = useRouter()
const authStore = useAuthStore()
const { isAuthenticated, user } = storeToRefs(authStore)

async function handleLogout() {
  await authStore.logout()
  await router.push('/login')
}
</script>

<template>
  <div class="app">
    <nav class="nav">
      <RouterLink to="/" class="nav__logo">Kanban</RouterLink>
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
  font-size: 1.25rem;
  text-decoration: none;
  color: var(--color-primary);
}
.nav__links {
  display: flex;
  gap: 1rem;
}
.nav__links a {
  text-decoration: none;
  color: var(--color-text-secondary);
}
.main {
  flex: 1;
  padding: 1.5rem;
}
</style>
