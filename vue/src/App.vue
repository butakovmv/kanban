<script setup lang="ts">
import { RouterView, RouterLink } from 'vue-router'
</script>

<template>
  <div class="app">
    <nav class="nav">
      <RouterLink to="/" class="nav__logo">Kanban</RouterLink>
      <div class="nav__links">
        <RouterLink to="/login">Login</RouterLink>
        <RouterLink to="/register">Register</RouterLink>
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
