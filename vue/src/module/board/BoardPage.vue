<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useBoardStore } from './store'
import { useTaskStore } from '../task/store'
import { useRealtime } from '../realtime/useRealtime'
import ProjectLayout from '../../component/ProjectLayout.vue'
import Column from './Column.vue'
import CreateTaskModal from '../task/CreateTaskModal.vue'
import type { CreateTaskRequest } from '../task/api'

const route = useRoute()
const router = useRouter()
const boardStore = useBoardStore()
const taskStore = useTaskStore()
const { currentBoard, columns, loading: boardLoading, error: boardError } = storeToRefs(boardStore)
const { tasks, error: taskError, loading: taskLoading } = storeToRefs(taskStore)

const swimlanesEnabled = ref(false)
const modalColumnId = ref<string | null>(null)
const dragOverColumnId = ref<string | null>(null)

const projectId = computed(() => {
  const id = route.params['id']
  return Array.isArray(id) ? id[0] : id
})

const error = computed(() => boardError.value ?? taskError.value)
const loading = computed(() => boardLoading.value || taskLoading.value)

const tasksForColumn = (columnId: string) => taskStore.tasksForColumn(columnId)

async function load() {
  if (projectId.value === undefined) {
    return
  }
  await boardStore.loadBoardByProjectId(projectId.value)
  if (currentBoard.value !== null) {
    await taskStore.loadTasks(currentBoard.value.id)
  }
}

onMounted(load)
watch(projectId, load)

useRealtime(computed(() => currentBoard.value?.id ?? ''))

function toggleSwimlanes() {
  swimlanesEnabled.value = !swimlanesEnabled.value
}

function openAddTask(columnId: string) {
  modalColumnId.value = columnId
}

function closeModal() {
  modalColumnId.value = null
}

async function handleCreateTask(request: CreateTaskRequest) {
  const created = await taskStore.createTask(request)
  if (created !== null) {
    modalColumnId.value = null
  }
}

function openTask(taskId: string) {
  void router.push(`/tasks/${taskId}`)
}

async function moveTask(payload: { taskId: string; columnId: string }) {
  await boardStore.optimisticMoveTask(payload.taskId, payload.columnId)
  dragOverColumnId.value = null
}

function onColumnDrop() {
  dragOverColumnId.value = null
}
</script>

<template>
  <ProjectLayout v-if="projectId" :project-id="projectId">
    <div class="board">
      <div class="board__actions">
        <button
          type="button"
          class="board__swimlanes-toggle"
          :aria-pressed="swimlanesEnabled"
          @click="toggleSwimlanes"
        >
          Swimlanes: {{ swimlanesEnabled ? 'on' : 'off' }}
        </button>
        <button type="button" class="board__add-column">+ Add column</button>
      </div>

      <div v-if="error" class="board__error">{{ error }}</div>

      <div v-if="loading && currentBoard === null" class="board__loading">Loading...</div>

      <div v-else-if="currentBoard !== null" class="board__columns">
        <Column
          v-for="column in columns"
          :key="column.id"
          :column="column"
          :tasks="tasksForColumn(column.id)"
          :all-columns="columns"
          :board-id="currentBoard.id"
          :drag-over="dragOverColumnId === column.id"
          @add-task="openAddTask"
          @open-task="openTask"
          @move-task="moveTask"
          @drop="onColumnDrop"
        />
        <div v-if="columns.length === 0" class="board__empty">No columns yet.</div>
        <div v-else-if="tasks.length === 0" class="board__empty">No tasks yet.</div>
      </div>

      <div v-else class="board__not-found">Board not found.</div>

      <CreateTaskModal
        v-if="modalColumnId !== null && currentBoard !== null"
        :board-id="currentBoard.id"
        :column-id="modalColumnId"
        :loading="taskLoading"
        @submit="handleCreateTask"
        @cancel="closeModal"
      />
    </div>
  </ProjectLayout>
</template>

<style scoped>
.board {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  height: calc(100vh - 5.5rem);
}
.board__actions {
  display: flex;
  gap: 0.5rem;
}
.board__swimlanes-toggle,
.board__add-column {
  padding: 0.5rem 1rem;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  font-weight: 500;
  font-size: 0.875rem;
}
.board__swimlanes-toggle[aria-pressed='true'] {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}
.board__swimlanes-toggle:hover,
.board__add-column:hover {
  background: var(--color-background);
}
.board__swimlanes-toggle[aria-pressed='true']:hover {
  background: var(--color-primary-hover);
}
.board__add-column {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}
.board__add-column:hover {
  background: var(--color-primary-hover);
}
.board__error {
  padding: 0.75rem 1rem;
  background: var(--color-danger);
  color: #fff;
  border-radius: var(--radius);
}
.board__loading,
.board__not-found,
.board__empty {
  padding: 2rem;
  text-align: center;
  color: var(--color-text-secondary);
  background: var(--color-surface);
  border-radius: var(--radius);
}
.board__columns {
  display: flex;
  gap: 0.75rem;
  flex: 1;
  overflow-x: auto;
  padding-bottom: 0.5rem;
}
</style>
