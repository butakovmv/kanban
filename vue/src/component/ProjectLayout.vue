<script setup lang="ts">
import { RouterLink, useRoute } from 'vue-router'

defineProps<{
  projectId: string
}>()

const route = useRoute()
</script>

<template>
  <div class="project-layout">
    <aside class="project-layout__sidebar">
      <nav class="project-layout__nav">
        <RouterLink
          :to="{ name: 'project-board', params: { id: projectId } }"
          class="project-layout__nav-link"
          :class="{ 'project-layout__nav-link--active': route.name === 'project-board' }"
          aria-label="Board"
        >
          <svg viewBox="0 0 24 24" class="project-layout__icon" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="3" width="7" height="7" rx="1" />
            <rect x="14" y="3" width="7" height="7" rx="1" />
            <rect x="3" y="14" width="7" height="7" rx="1" />
            <rect x="14" y="14" width="7" height="7" rx="1" />
          </svg>
        </RouterLink>
        <RouterLink
          :to="{ name: 'project-documents', params: { id: projectId } }"
          class="project-layout__nav-link"
          :class="{ 'project-layout__nav-link--active': route.name === 'project-documents' }"
          aria-label="Documents"
        >
          <svg viewBox="0 0 24 24" class="project-layout__icon" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
            <polyline points="14 2 14 8 20 8" />
            <line x1="16" y1="13" x2="8" y2="13" />
            <line x1="16" y1="17" x2="8" y2="17" />
          </svg>
        </RouterLink>
        <RouterLink
          :to="{ name: 'project-reports', params: { id: projectId } }"
          class="project-layout__nav-link"
          :class="{ 'project-layout__nav-link--active': route.name === 'project-reports' }"
          aria-label="Reports"
        >
          <svg viewBox="0 0 24 24" class="project-layout__icon" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="20" x2="18" y2="10" />
            <line x1="12" y1="20" x2="12" y2="4" />
            <line x1="6" y1="20" x2="6" y2="14" />
          </svg>
        </RouterLink>
        <RouterLink
          :to="{ name: 'project-settings', params: { id: projectId } }"
          class="project-layout__nav-link"
          :class="{ 'project-layout__nav-link--active': route.name === 'project-settings' }"
          aria-label="Settings"
        >
          <svg viewBox="0 0 24 24" class="project-layout__icon" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="3" />
            <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" />
          </svg>
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
  display: flex;
  min-height: calc(100vh - 3.5rem);
}
.project-layout__sidebar {
  position: sticky;
  top: 0;
  left: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0.75rem 0.5rem;
  width: 3.5rem;
  min-width: 3.5rem;
  background: var(--color-surface);
  border-right: 1px solid var(--color-border);
}
.project-layout__nav {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
}
.project-layout__nav-link {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.25rem;
  height: 2.25rem;
  border-radius: var(--radius);
  color: var(--color-text-secondary);
  text-decoration: none;
  transition: background 0.1s ease, color 0.1s ease;
}
.project-layout__nav-link:hover {
  background: var(--color-background);
  color: var(--color-text);
}
.project-layout__nav-link--active {
  background: var(--color-primary);
  color: #fff;
}
.project-layout__icon {
  width: 1.25rem;
  height: 1.25rem;
}
.project-layout__content {
  flex: 1;
  min-width: 0;
  padding: 1rem;
}
</style>
