<script setup lang="ts">
import { RouterLink } from 'vue-router'

defineProps<{
  projectId: string
  boards: { id: string; name: string; position: number }[]
}>()
</script>

<template>
  <div class="project-layout">
    <aside class="project-layout__sidebar">
      <nav class="project-layout__nav">
        <div class="project-layout__nav-group">
          <RouterLink
            v-for="board in boards"
            :key="board.id"
            :to="{ name: 'board', params: { id: board.id } }"
            class="project-layout__nav-link"
          >
            {{ board.name }}
          </RouterLink>
        </div>
        <div class="project-layout__nav-divider" />
        <RouterLink
          :to="{ name: 'project-documents', params: { id: projectId } }"
          class="project-layout__nav-link"
        >
          Documents
        </RouterLink>
        <RouterLink
          :to="{ name: 'project-reports', params: { id: projectId } }"
          class="project-layout__nav-link"
        >
          Reports
        </RouterLink>
        <RouterLink
          :to="{ name: 'project-settings', params: { id: projectId } }"
          class="project-layout__nav-link"
        >
          Settings
        </RouterLink>
      </nav>
    </aside>
    <main class="project-layout__content">
      <slot />
    </main>
  </div>
</template>

<style scoped>
.project-layout {
  display: grid;
  grid-template-columns: 12rem 1fr;
  gap: 1.5rem;
}
.project-layout__sidebar {
  background: var(--color-surface);
  border-radius: var(--radius);
  padding: 1rem;
  box-shadow: var(--shadow);
  height: fit-content;
}
.project-layout__nav {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}
.project-layout__nav-group {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}
.project-layout__nav-divider {
  height: 1px;
  background: var(--color-border);
  margin: 0.5rem 0;
}
.project-layout__nav-link {
  padding: 0.5rem 1rem;
  background: var(--color-background);
  border-radius: var(--radius);
  text-decoration: none;
  color: var(--color-text);
  font-weight: 500;
  text-align: center;
  transition: background 0.1s ease;
}
.project-layout__nav-link:hover {
  background: var(--color-primary);
  color: #fff;
}
.project-layout__nav-link.router-link-active {
  background: var(--color-primary);
  color: #fff;
}
.project-layout__content {
  min-width: 0;
}
</style>
