<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useProjectStore } from './store'
import { useBoards } from '../../composables/useBoards'
import ProjectLayout from '../../component/ProjectLayout.vue'

const route = useRoute()
const projectStore = useProjectStore()
const { currentProject, loading, error } = storeToRefs(projectStore)

const projectId = computed(() => {
  const id = route.params['id']
  return Array.isArray(id) ? id[0] : id
})

const { boards, loadBoards } = useBoards(() => projectId.value ?? undefined)

async function load() {
  if (projectId.value === undefined) return
  await projectStore.loadProject(projectId.value)
  await loadBoards()
}

onMounted(load)
watch(projectId, load)
</script>

<template>
  <div class="project-view">
    <div v-if="loading && currentProject === null" class="project-view__loading">
      Loading...
    </div>

    <ProjectLayout v-else-if="currentProject !== null && projectId" :project-id="projectId" :boards="boards">
      <div v-if="boards.length === 0" class="project-view__empty">
        No boards yet. Create the first one from project settings.
      </div>
      <div v-else class="project-view__board-list">
        <RouterLink
          v-for="board in boards"
          :key="board.id"
          :to="{ name: 'board', params: { id: board.id } }"
          class="project-view__board-card"
        >
          <span class="project-view__board-name">{{ board.name }}</span>
          <span class="project-view__board-meta">{{ board.position + 1 }}</span>
        </RouterLink>
      </div>
    </ProjectLayout>

    <div v-else class="project-view__not-found">Project not found.</div>
  </div>
</template>

<style scoped>
.project-view {
  max-width: 64rem;
  margin: 0 auto;
}
.project-view__loading,
.project-view__empty,
.project-view__not-found {
  padding: 2rem;
  text-align: center;
  color: var(--color-text-secondary);
  background: var(--color-surface);
  border-radius: var(--radius);
}
.project-view__board-list {
  display: grid;
  gap: 0.75rem;
}
.project-view__board-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.25rem;
  background: var(--color-surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  text-decoration: none;
  color: inherit;
  transition: transform 0.1s ease;
}
.project-view__board-card:hover {
  transform: translateY(-1px);
}
.project-view__board-name {
  font-weight: 600;
}
.project-view__board-meta {
  color: var(--color-text-secondary);
  font-size: 0.75rem;
}
</style>
