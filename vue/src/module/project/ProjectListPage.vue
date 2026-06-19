<script setup lang="ts">
/**
 * Страница списка проектов пользователя.
 * Содержит карточки проектов и форму создания нового проекта.
 * Клик по карточке переходит на страницу настроек проекта.
 */
import { onMounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useProjectStore } from './store'
import { useAuthStore } from '../auth/store'

const projectStore = useProjectStore()
const authStore = useAuthStore()
const { projects, loading, error } = storeToRefs(projectStore)

const showCreateForm = ref(false)
const newName = ref('')
const newDescription = ref('')

const MOCK_OWNER_ID = 'mock-owner-id'

onMounted(async () => {
  const ownerId = authStore.user?.id ?? MOCK_OWNER_ID
  await projectStore.loadProjects(ownerId)
})

async function handleCreate() {
  const ownerId = authStore.user?.id ?? MOCK_OWNER_ID
  const created = await projectStore.createProject({
    ownerId,
    name: newName.value,
    description: newDescription.value === '' ? null : newDescription.value,
  })
  if (created !== null) {
    newName.value = ''
    newDescription.value = ''
    showCreateForm.value = false
  }
}
</script>

<template>
  <div class="project-list">
    <header class="project-list__header">
      <h1>Projects</h1>
      <button class="project-list__create-btn" @click="showCreateForm = !showCreateForm">
        {{ showCreateForm ? 'Cancel' : 'New project' }}
      </button>
    </header>

    <form v-if="showCreateForm" class="project-list__form" @submit.prevent="handleCreate">
      <label>
        Name
        <input v-model="newName" type="text" required maxlength="200" />
      </label>
      <label>
        Description
        <textarea v-model="newDescription" rows="3" maxlength="2000" />
      </label>
      <button type="submit" :disabled="loading">Create</button>
    </form>

    <div v-if="error" class="project-list__error">{{ error }}</div>

    <div v-if="loading && projects.length === 0" class="project-list__loading">Loading...</div>

    <ul v-else-if="projects.length > 0" class="project-list__items">
      <li v-for="project in projects" :key="project.id" class="project-list__item">
        <RouterLink
          :to="{ name: 'project-settings', params: { id: project.id } }"
          class="project-list__link"
        >
          <div class="project-list__name">{{ project.name }}</div>
          <div v-if="project.description" class="project-list__description">
            {{ project.description }}
          </div>
          <div class="project-list__meta">Created: {{ project.createdAt }}</div>
        </RouterLink>
      </li>
    </ul>

    <div v-else class="project-list__empty">No projects yet. Create the first one!</div>
  </div>
</template>

<style scoped>
.project-list {
  max-width: 64rem;
  margin: 0 auto;
}
.project-list__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}
.project-list__header h1 {
  font-size: 1.5rem;
}
.project-list__create-btn {
  padding: 0.5rem 1rem;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius);
  font-weight: 600;
}
.project-list__create-btn:hover {
  background: var(--color-primary-hover);
}
.project-list__form {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding: 1.25rem;
  margin-bottom: 1.5rem;
  background: var(--color-surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}
.project-list__form label {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.875rem;
  color: var(--color-text-secondary);
}
.project-list__form input,
.project-list__form textarea {
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  background: var(--color-background);
  color: var(--color-text);
}
.project-list__form button {
  align-self: flex-start;
  padding: 0.5rem 1rem;
  background: var(--color-success);
  color: #fff;
  border: none;
  border-radius: var(--radius);
  font-weight: 600;
}
.project-list__error {
  margin-bottom: 1rem;
  padding: 0.75rem 1rem;
  background: var(--color-danger);
  color: #fff;
  border-radius: var(--radius);
}
.project-list__loading,
.project-list__empty {
  padding: 2rem;
  text-align: center;
  color: var(--color-text-secondary);
  background: var(--color-surface);
  border-radius: var(--radius);
}
.project-list__items {
  list-style: none;
  display: grid;
  gap: 0.75rem;
  padding: 0;
}
.project-list__item {
  background: var(--color-surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  transition: transform 0.1s ease;
}
.project-list__item:hover {
  transform: translateY(-1px);
}
.project-list__link {
  display: block;
  padding: 1rem 1.25rem;
  text-decoration: none;
  color: inherit;
}
.project-list__name {
  font-weight: 600;
  margin-bottom: 0.25rem;
}
.project-list__description {
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  margin-bottom: 0.5rem;
}
.project-list__meta {
  color: var(--color-text-secondary);
  font-size: 0.75rem;
}
</style>
