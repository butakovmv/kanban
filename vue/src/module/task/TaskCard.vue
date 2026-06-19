<script setup lang="ts">
/**
 * Компонент карточки задачи.
 * Отображает заголовок, превью описания, дату выполнения.
 * Поддерживает in-place редактирование заголовка,
 * перемещение между колонками, архивирование и удаление.
 * Поддерживает drag-and-drop через HTML5 API.
 */
import { computed, ref } from 'vue'
import type { Task } from './api'
import type { Column } from '../board/api'
import { useTaskStore } from './store'

const props = defineProps<{
  task: Task
  columns: Column[]
  isDragging?: boolean
}>()

const emit = defineEmits<{
  open: [id: string]
  dragstart: [taskId: string]
  dragend: []
}>()

const taskStore = useTaskStore()
const editing = ref(false)
const draftTitle = ref(props.task.title)
const showMoveMenu = ref(false)
const showDeleteConfirm = ref(false)

const descriptionPreview = computed(() => {
  if (props.task.description === null) {
    return ''
  }
  const text = props.task.description.replace(/\s+/g, ' ').trim()
  return text.length > 100 ? `${text.slice(0, 100)}…` : text
})

const formattedDueDate = computed(() => {
  if (props.task.dueDate === null) {
    return ''
  }
  const date = new Date(props.task.dueDate)
  if (Number.isNaN(date.getTime())) {
    return props.task.dueDate
  }
  return date.toISOString().slice(0, 10)
})

function openTask() {
  if (editing.value) {
    return
  }
  emit('open', props.task.id)
}

function startEdit() {
  draftTitle.value = props.task.title
  editing.value = true
}

async function commitEdit() {
  const trimmed = draftTitle.value.trim()
  if (trimmed === '' || trimmed === props.task.title) {
    editing.value = false
    draftTitle.value = props.task.title
    return
  }
  await taskStore.updateTask(props.task.id, { title: trimmed })
  editing.value = false
}

function cancelEdit() {
  editing.value = false
  draftTitle.value = props.task.title
}

function toggleMoveMenu() {
  showMoveMenu.value = !showMoveMenu.value
}

async function moveToColumn(columnId: string) {
  showMoveMenu.value = false
  if (columnId === props.task.columnId) {
    return
  }
  await taskStore.moveTask(props.task.id, { columnId, position: 0 })
}

async function archive() {
  await taskStore.archiveTask(props.task.id)
}

function askDelete() {
  showDeleteConfirm.value = true
}

function cancelDelete() {
  showDeleteConfirm.value = false
}

async function confirmDelete() {
  showDeleteConfirm.value = false
  await taskStore.deleteTask(props.task.id)
}

function onDragStart(event: DragEvent) {
  if (event.dataTransfer !== null) {
    event.dataTransfer.effectAllowed = 'move'
    event.dataTransfer.setData('text/plain', props.task.id)
  }
  emit('dragstart', props.task.id)
}

function onDragEnd() {
  emit('dragend')
}
</script>

<template>
  <div
    class="task-card"
    :class="{ 'task-card--dragging': isDragging }"
    draggable="true"
    @dragstart="onDragStart"
    @dragend="onDragEnd"
    @click="openTask"
  >
    <div v-if="!editing" class="task-card__title-row">
      <div class="task-card__title" @click.stop="openTask">{{ task.title }}</div>
      <button
        type="button"
        class="task-card__edit"
        aria-label="Edit title"
        @click.stop="startEdit"
      >
        ✎
      </button>
    </div>
    <form v-else class="task-card__edit-form" @submit.prevent="commitEdit" @click.stop>
      <input
        v-model="draftTitle"
        type="text"
        class="task-card__edit-input"
        maxlength="200"
        required
        @keydown.esc="cancelEdit"
      />
      <button type="submit" class="task-card__edit-save">Save</button>
      <button type="button" class="task-card__edit-cancel" @click="cancelEdit">Cancel</button>
    </form>

    <div v-if="descriptionPreview" class="task-card__description">{{ descriptionPreview }}</div>

    <div v-if="formattedDueDate" class="task-card__due">Due: {{ formattedDueDate }}</div>

    <div class="task-card__actions" @click.stop>
      <div class="task-card__move">
        <button
          type="button"
          class="task-card__action"
          :aria-expanded="showMoveMenu"
          @click="toggleMoveMenu"
        >
          Move
        </button>
        <ul v-if="showMoveMenu" class="task-card__menu">
          <li
            v-for="column in columns"
            :key="column.id"
            class="task-card__menu-item"
            :class="{ 'task-card__menu-item--active': column.id === task.columnId }"
            @click="moveToColumn(column.id)"
          >
            {{ column.name }}
          </li>
        </ul>
      </div>
      <button type="button" class="task-card__action" @click="archive">Archive</button>
      <button
        v-if="!showDeleteConfirm"
        type="button"
        class="task-card__action task-card__action--danger"
        @click="askDelete"
      >
        Delete
      </button>
      <div v-else class="task-card__confirm">
        <span>Delete?</span>
        <button type="button" class="task-card__action task-card__action--danger" @click="confirmDelete">
          Yes
        </button>
        <button type="button" class="task-card__action" @click="cancelDelete">No</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.task-card {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  padding: 0.5rem 0.75rem;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  cursor: grab;
  position: relative;
}
.task-card:active {
  cursor: grabbing;
}
.task-card--dragging {
  opacity: 0.5;
}
.task-card__title-row {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  justify-content: space-between;
}
.task-card__title {
  font-size: 0.875rem;
  font-weight: 500;
  flex: 1;
}
.task-card__edit {
  background: transparent;
  border: none;
  color: var(--color-text-secondary);
  font-size: 0.85rem;
  padding: 0 0.25rem;
  border-radius: var(--radius);
}
.task-card__edit:hover {
  color: var(--color-primary);
  background: var(--color-surface);
}
.task-card__edit-form {
  display: flex;
  gap: 0.25rem;
  flex-wrap: wrap;
}
.task-card__edit-input {
  flex: 1;
  min-width: 0;
  padding: 0.25rem 0.5rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  font-size: 0.85rem;
  background: var(--color-surface);
  color: var(--color-text);
}
.task-card__edit-save,
.task-card__edit-cancel {
  padding: 0.2rem 0.5rem;
  font-size: 0.75rem;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius);
}
.task-card__edit-cancel {
  background: var(--color-text-secondary);
}
.task-card__description {
  font-size: 0.75rem;
  color: var(--color-text-secondary);
  line-height: 1.3;
}
.task-card__due {
  font-size: 0.7rem;
  color: var(--color-text-secondary);
}
.task-card__actions {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  flex-wrap: wrap;
  position: relative;
}
.task-card__action {
  padding: 0.15rem 0.5rem;
  font-size: 0.7rem;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
}
.task-card__action:hover {
  background: var(--color-background);
}
.task-card__action--danger {
  color: var(--color-danger);
  border-color: var(--color-danger);
}
.task-card__action--danger:hover {
  background: var(--color-danger);
  color: #fff;
}
.task-card__move {
  position: relative;
}
.task-card__menu {
  position: absolute;
  top: 100%;
  left: 0;
  z-index: 10;
  margin-top: 0.25rem;
  list-style: none;
  padding: 0.25rem 0;
  min-width: 8rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}
.task-card__menu-item {
  padding: 0.3rem 0.6rem;
  font-size: 0.75rem;
  cursor: pointer;
}
.task-card__menu-item:hover {
  background: var(--color-background);
}
.task-card__menu-item--active {
  font-weight: 600;
  color: var(--color-primary);
}
.task-card__confirm {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.7rem;
  color: var(--color-text-secondary);
}
</style>
