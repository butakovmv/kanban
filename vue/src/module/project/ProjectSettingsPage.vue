<script setup lang="ts">
/**
 * Страница настроек проекта.
 */
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useProjectStore } from './store'
import ProjectLayout from '../../component/ProjectLayout.vue'

const route = useRoute()
const router = useRouter()
const projectStore = useProjectStore()
const { currentProject, loading, error } = storeToRefs(projectStore)

const editName = ref('')
const editDescription = ref('')
const isDirty = ref(false)

const projectId = computed(() => {
  const id = route.params['id']
  return Array.isArray(id) ? id[0] : id
})

async function load() {
  if (projectId.value === undefined) return
  await projectStore.loadProject(projectId.value)
  if (currentProject.value !== null) {
    editName.value = currentProject.value.name
    editDescription.value = currentProject.value.description ?? ''
    isDirty.value = false
  }
}

onMounted(load)
watch(projectId, load)

function markDirty() {
  isDirty.value = true
}

async function handleSave() {
  if (projectId.value === undefined) {
    return
  }
  const success = await projectStore.updateProject(projectId.value, {
    name: editName.value,
    description: editDescription.value === '' ? null : editDescription.value,
  })
  if (success) {
    isDirty.value = false
  }
}

async function handleDelete() {
  if (projectId.value === undefined) {
    return
  }
  const confirmed = globalThis.confirm(
    'Delete this project? This action cannot be undone and will erase all related data.',
  )
  if (!confirmed) {
    return
  }
  const success = await projectStore.deleteProject(projectId.value)
  if (success) {
    await router.push({ name: 'projects' })
  }
}
</script>

<template>
  <ProjectLayout v-if="projectId" :project-id="projectId">
    <div v-if="loading && currentProject === null" class="project-settings__loading">
      Loading...
    </div>

    <template v-else-if="currentProject !== null">
      <form class="project-settings__form" @submit.prevent="handleSave">
        <div v-if="error" class="project-settings__error">{{ error }}</div>

        <label>
          Name
          <input v-model="editName" type="text" required maxlength="200" @input="markDirty" />
        </label>

        <label>
          Description
          <textarea v-model="editDescription" rows="4" maxlength="2000" @input="markDirty" />
        </label>

        <div class="project-settings__meta">
          <div>ID: {{ currentProject.id }}</div>
          <div>Owner: {{ currentProject.ownerId }}</div>
          <div>Created: {{ currentProject.createdAt }}</div>
          <div>Updated: {{ currentProject.updatedAt }}</div>
        </div>

        <div class="project-settings__actions">
          <button type="submit" :disabled="loading || !isDirty">Save</button>
          <button type="button" class="project-settings__cancel" @click="handleDelete">
            Delete project
          </button>
        </div>
      </form>
    </template>

    <div v-else class="project-settings__not-found">Project not found.</div>
  </ProjectLayout>
</template>

<style scoped>
.project-settings__loading {
  padding: 2rem;
  text-align: center;
  color: var(--color-text-secondary);
}
.project-settings__form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 1rem;
  background: var(--color-surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}
.project-settings__form label {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.875rem;
  color: var(--color-text-secondary);
}
.project-settings__form input,
.project-settings__form textarea {
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  background: var(--color-background);
  color: var(--color-text);
}
.project-settings__meta {
  display: grid;
  gap: 0.25rem;
  padding: 0.75rem 1rem;
  background: var(--color-background);
  border-radius: var(--radius);
  font-size: 0.75rem;
  color: var(--color-text-secondary);
}
.project-settings__actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 0.5rem;
}
.project-settings__actions button {
  padding: 0.5rem 1rem;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius);
  font-weight: 600;
}
.project-settings__actions button:hover {
  background: var(--color-primary-hover);
}
.project-settings__actions button:disabled {
  background: var(--color-text-secondary);
  cursor: not-allowed;
}
.project-settings__cancel {
  background: var(--color-danger) !important;
}
.project-settings__cancel:hover {
  background: var(--color-danger) !important;
}
.project-settings__error {
  padding: 0.75rem 1rem;
  background: var(--color-danger);
  color: #fff;
  border-radius: var(--radius);
  font-size: 0.875rem;
}
.project-settings__not-found {
  padding: 2rem;
  text-align: center;
  color: var(--color-text-secondary);
}
</style>
