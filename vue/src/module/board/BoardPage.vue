<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useBoardStore } from './store'
import { useTaskStore } from '../task/store'
import { useProjectStore } from '../project/store'
import { useUserStore } from '../user/store'
import { useRealtime } from '../realtime/useRealtime'
import ProjectLayout from '../../component/ProjectLayout.vue'
import Column from './Column.vue'
import TaskCard from '../task/TaskCard.vue'
import CreateTaskModal from '../task/CreateTaskModal.vue'
import type { CreateTaskRequest, Task } from '../task/api'
import type { Column as ColumnType } from './api'

const route = useRoute()
const router = useRouter()
const boardStore = useBoardStore()
const taskStore = useTaskStore()
const projectStore = useProjectStore()
const userStore = useUserStore()
const { currentBoard, columns, loading: boardLoading, error: boardError } = storeToRefs(boardStore)
const { tasks, error: taskError, loading: taskLoading } = storeToRefs(taskStore)

const swimlanesEnabled = ref(false)
const showBacklogAndArchive = ref(true)
const modalColumnId = ref<string | null>(null)
const dragOverColumnId = ref<string | null>(null)
const dragOverArchive = ref(false)
const showFilters = ref(false)
const filterPriority = ref('')
const filterAssignee = ref('')
const filterLabel = ref('')
const filterHasDeadline = ref('')

const ARCHIVE_COL_ID = '__archive__'

const projectId = computed(() => {
  const id = route.params['id']
  return Array.isArray(id) ? id[0] : id
})

const error = computed(() => boardError.value ?? taskError.value)
const loading = computed(() => boardLoading.value || taskLoading.value)

const sortedColumns = computed(() =>
  [...columns.value].sort((a, b) => a.position - b.position),
)

const displayColumns = computed(() => {
  const cols: ColumnType[] = sortedColumns.value.filter(c => c.name !== 'Backlog' || showBacklogAndArchive.value)
  if (showBacklogAndArchive.value) {
    cols.push({ id: ARCHIVE_COL_ID, boardId: '', name: 'Archive', position: 999, wipLimit: null, createdAt: '' })
  }
  return cols
})

const allLabels = computed(() => {
  const set = new Set<string>()
  for (const t of tasks.value) {
    for (const l of t.labels) {
      set.add(l)
    }
  }
  return [...set].sort()
})

const memberOptions = computed(() => {
  const ids: string[] = []
  const owner = projectStore.currentProject
  if (owner) ids.push(owner.ownerId)
  for (const m of projectStore.projectMembers) {
    if (!ids.includes(m.userId)) ids.push(m.userId)
  }
  if (ids.length > 0) userStore.ensureUsers(ids)
  return ids.map(id => ({
    id,
    name: userStore.getDisplayName(id) || id,
  }))
})

const filteredTasks = computed(() => {
  let result = tasks.value
  if (filterPriority.value) {
    result = result.filter(t => t.priority === filterPriority.value)
  }
  if (filterAssignee.value) {
    result = result.filter(t => t.assigneeId === filterAssignee.value)
  }
  if (filterLabel.value) {
    result = result.filter(t => t.labels.includes(filterLabel.value))
  }
  if (filterHasDeadline.value === 'yes') {
    result = result.filter(t => t.dueDate !== null)
  } else if (filterHasDeadline.value === 'no') {
    result = result.filter(t => t.dueDate === null)
  }
  return result
})

function tasksForDisplayColumn(columnId: string): Task[] {
  if (columnId === ARCHIVE_COL_ID) {
    return filteredTasks.value.filter(t => t.archived)
  }
  return filteredTasks.value.filter(t => t.columnId === columnId && !t.archived)
}

async function load() {
  if (projectId.value === undefined) {
    return
  }
  await Promise.all([
    boardStore.loadBoardByProjectId(projectId.value),
    projectStore.loadProject(projectId.value),
    projectStore.loadProjectMembers(projectId.value),
  ])
  if (currentBoard.value !== null) {
    await taskStore.loadTasks(projectId.value)
  }
}

onMounted(load)
watch(projectId, load)

useRealtime(undefined, projectId.value)

function toggleSwimlanes() {
  swimlanesEnabled.value = !swimlanesEnabled.value
}

function toggleBacklogAndArchive() {
  showBacklogAndArchive.value = !showBacklogAndArchive.value
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
  void router.push({ path: `/tasks/${taskId}`, query: { projectId: projectId.value } })
}

async function moveTask(payload: { taskId: string; columnId: string }) {
  await boardStore.optimisticMoveTask(payload.taskId, payload.columnId)
  dragOverColumnId.value = null
  dragOverArchive.value = false
}

function onColumnDrop() {
  dragOverColumnId.value = null
  dragOverArchive.value = false
}

function onArchiveDragOver(event: DragEvent) {
  event.preventDefault()
  if (event.dataTransfer !== null) {
    event.dataTransfer.dropEffect = 'move'
  }
  dragOverArchive.value = true
}

function onArchiveDrop(event: DragEvent) {
  event.preventDefault()
  const taskId = event.dataTransfer?.getData('text/plain')
  if (taskId !== undefined && taskId !== '') {
    void taskStore.archiveTask(taskId)
  }
  dragOverArchive.value = false
}
</script>

<template>
  <ProjectLayout v-if="projectId" :project-id="projectId">
    <div class="board">
      <div class="board__actions">
        <button
          type="button"
          class="board__toggle"
          :class="{ 'board__toggle--on': showBacklogAndArchive }"
          @click="toggleBacklogAndArchive"
        >
          Backlog/Archive: {{ showBacklogAndArchive ? 'on' : 'off' }}
        </button>
        <button
          type="button"
          class="board__swimlanes-toggle"
          :aria-pressed="swimlanesEnabled"
          @click="toggleSwimlanes"
        >
          Swimlanes: {{ swimlanesEnabled ? 'on' : 'off' }}
        </button>
        <button type="button" class="board__add-column">+ Add column</button>
        <button type="button" class="board__filter-toggle" :class="{ 'board__filter-toggle--on': showFilters }" @click="showFilters = !showFilters">
          Filters
        </button>
      </div>

      <div v-if="showFilters" class="board__filters">
        <select v-model="filterPriority" class="board__filter-select">
          <option value="">Any priority</option>
          <option value="low">Low</option>
          <option value="medium">Medium</option>
          <option value="high">High</option>
          <option value="critical">Critical</option>
        </select>
        <select v-model="filterAssignee" class="board__filter-select">
          <option value="">Any assignee</option>
          <option v-for="m in memberOptions" :key="m.id" :value="m.id">{{ m.name }}</option>
        </select>
        <select v-model="filterLabel" class="board__filter-select">
          <option value="">Any label</option>
          <option v-for="l in allLabels" :key="l" :value="l">{{ l }}</option>
        </select>
        <select v-model="filterHasDeadline" class="board__filter-select">
          <option value="">Deadline: any</option>
          <option value="yes">With deadline</option>
          <option value="no">Without deadline</option>
        </select>
      </div>

      <div v-if="error" class="board__error">{{ error }}</div>

      <div v-if="loading && currentBoard === null" class="board__loading">Loading...</div>

      <div v-else-if="currentBoard !== null" class="board__columns">
        <template v-for="column in displayColumns" :key="column.id">
          <Column
            v-if="column.id !== ARCHIVE_COL_ID"
            :column="column"
            :tasks="tasksForDisplayColumn(column.id)"
            :all-columns="columns"
            :board-id="currentBoard.id"
            :drag-over="dragOverColumnId === column.id"
            :show-add="column.name === 'Backlog'"
            @add-task="openAddTask"
            @open-task="openTask"
            @move-task="moveTask"
            @drop="onColumnDrop"
          />
          <div
            v-else-if="column.id === ARCHIVE_COL_ID"
            class="column"
            :class="{ 'column--drop': dragOverArchive }"
            @dragenter.prevent
            @dragover="onArchiveDragOver"
            @drop="onArchiveDrop"
          >
            <header class="column__header">
              <h3 class="column__name">Archive</h3>
              <span class="column__count">{{ tasksForDisplayColumn(ARCHIVE_COL_ID).length }}</span>
            </header>
            <div class="column__body">
              <TaskCard
                v-for="task in tasksForDisplayColumn(ARCHIVE_COL_ID)"
                :key="task.id"
                :task="task"
                :columns="columns"
                @open="openTask"
              />
              <div v-if="tasksForDisplayColumn(ARCHIVE_COL_ID).length === 0" class="column__placeholder">
                No archived tasks
              </div>
            </div>
          </div>
        </template>
        <div v-if="columns.length === 0" class="board__empty">No columns yet.</div>
      </div>

      <div v-else class="board__not-found">Board not found.</div>

      <CreateTaskModal
        v-if="modalColumnId !== null && currentBoard !== null"
        :project-id="projectId"
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
.board__toggle {
  padding: 0.5rem 1rem;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  font-weight: 500;
  font-size: 0.875rem;
}
.board__toggle--on {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}
.board__toggle:hover {
  background: var(--color-background);
}
.board__toggle--on:hover {
  background: var(--color-primary-hover);
}
.board__filter-toggle {
  padding: 0.5rem 1rem;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  font-weight: 500;
  font-size: 0.875rem;
}
.board__filter-toggle--on {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}
.board__filter-toggle:hover {
  background: var(--color-background);
}
.board__filter-toggle--on:hover {
  background: var(--color-primary-hover);
}
.board__filters {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}
.board__filter-select {
  padding: 0.4rem 0.75rem;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  font-size: 0.8rem;
  min-width: 10rem;
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

/* Virtual column styles (backlog / archive) */
.column {
  flex: 0 0 18rem;
  display: flex;
  flex-direction: column;
  background: var(--color-surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  max-height: calc(100vh - 10rem);
  border: 2px solid transparent;
  transition: border-color 0.15s;
}
.column--drop {
  border-color: var(--color-primary);
}
.column__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1rem;
  border-bottom: 1px solid var(--color-border);
}
.column__name {
  font-size: 0.875rem;
  font-weight: 600;
}
.column__count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 1.5rem;
  height: 1.5rem;
  padding: 0 0.4rem;
  background: var(--color-background);
  color: var(--color-text-secondary);
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
}
.column__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 0.75rem;
  overflow-y: auto;
}
.column__placeholder {
  padding: 1rem;
  text-align: center;
  font-size: 0.75rem;
  color: var(--color-text-secondary);
  border: 1px dashed var(--color-border);
  border-radius: var(--radius);
}
.column__add {
  margin: 0 0 0.5rem;
  padding: 0.4rem 0.75rem;
  font-size: 0.75rem;
  background: var(--color-background);
  color: var(--color-text);
  border: 1px dashed var(--color-border);
  border-radius: var(--radius);
}
.column__add:hover {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
  border-style: solid;
}
.column__add--prominent {
  background: var(--color-primary);
  color: #fff;
  border-style: solid;
  border-color: var(--color-primary);
  font-weight: 600;
}
.column__add--prominent:hover {
  background: var(--color-primary-hover);
  border-color: var(--color-primary-hover);
}
</style>
