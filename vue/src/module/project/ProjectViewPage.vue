<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useProjectStore } from './store'
import * as boardApi from '../board/api'

const route = useRoute()
const router = useRouter()
const projectStore = useProjectStore()
const { currentProject, loading, error } = storeToRefs(projectStore)

const boards = ref<boardApi.Board[]>([])
const boardsLoading = ref(false)
const boardsError = ref<string | null>(null)

const projectId = computed(() => {
  const id = route.params['id']
  return Array.isArray(id) ? id[0] : id
})

async function load() {
  if (projectId.value === undefined) return
  await projectStore.loadProject(projectId.value)
  await loadBoards()
}

async function loadBoards() {
  if (projectId.value === undefined) return
  boardsLoading.value = true
  boardsError.value = null
  try {
    boards.value = await boardApi.listBoardsByProjectId(projectId.value)
  } catch (e: unknown) {
    boardsError.value = e instanceof Error ? e.message : 'Failed to load boards'
    boards.value = []
  } finally {
    boardsLoading.value = false
  }
}

onMounted(load)
watch(projectId, load)
</script>

<template>
  <div class="project-view">
    <header class="project-view__header">
      <RouterLink :to="{ name: 'projects' }" class="project-view__back">&larr; Projects</RouterLink>
    </header>

    <div v-if="loading && currentProject === null" class="project-view__loading">
      Loading...
    </div>

    <template v-else-if="currentProject !== null">
      <div class="project-view__info">
        <h1>{{ currentProject.name }}</h1>
        <p v-if="currentProject.description" class="project-view__description">
          {{ currentProject.description }}
        </p>
      </div>

      <nav class="project-view__nav">
        <RouterLink
          :to="{ name: 'project-documents', params: { id: projectId } }"
          class="project-view__nav-link"
        >
          Documents
        </RouterLink>
        <RouterLink
          :to="{ name: 'project-reports', params: { id: projectId } }"
          class="project-view__nav-link"
        >
          Reports
        </RouterLink>
        <RouterLink
          :to="{ name: 'project-settings', params: { id: projectId } }"
          class="project-view__nav-link"
        >
          Settings
        </RouterLink>
      </nav>

      <section class="project-view__boards">
        <h2>Boards</h2>

        <div v-if="boardsError" class="project-view__error">{{ boardsError }}</div>

        <div v-if="boardsLoading && boards.length === 0" class="project-view__loading">
          Loading boards...
        </div>

        <div v-else-if="boards.length === 0" class="project-view__empty">
          No boards yet.
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
      </section>
    </template>

    <div v-else class="project-view__not-found">Project not found.</div>
  </div>
</template>

<style scoped>
.project-view {
  max-width: 64rem;
  margin: 0 auto;
}
.project-view__header {
  margin-bottom: 1rem;
}
.project-view__back {
  color: var(--color-primary);
  text-decoration: none;
  font-size: 0.875rem;
}
.project-view__back:hover {
  text-decoration: underline;
}
.project-view__info {
  margin-bottom: 1.5rem;
}
.project-view__info h1 {
  font-size: 1.5rem;
  margin-bottom: 0.25rem;
}
.project-view__description {
  color: var(--color-text-secondary);
  font-size: 0.875rem;
}
.project-view__nav {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}
.project-view__nav-link {
  padding: 0.5rem 1rem;
  background: var(--color-surface);
  border-radius: var(--radius);
  text-decoration: none;
  color: var(--color-text);
  font-weight: 500;
  box-shadow: var(--shadow);
  transition: background 0.1s ease;
}
.project-view__nav-link:hover {
  background: var(--color-primary);
  color: #fff;
}
.project-view__boards h2 {
  font-size: 1.125rem;
  margin-bottom: 0.75rem;
}
.project-view__error {
  margin-bottom: 1rem;
  padding: 0.75rem 1rem;
  background: var(--color-danger);
  color: #fff;
  border-radius: var(--radius);
}
.project-view__loading,
.project-view__empty {
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
.project-view__not-found {
  padding: 2rem;
  text-align: center;
  color: var(--color-text-secondary);
}
</style>
