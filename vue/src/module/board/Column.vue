<script setup lang="ts">
/**
 * Компонент колонки на доске.
 * Отображает название колонки, позицию и список задач в виде карточек.
 * Поддерживает добавление новой задачи, drag-n-drop приём и базовые операции.
 */
import { computed } from 'vue'
import type { Column } from '../board/api'
import type { Task } from '../task/api'
import TaskCard from '../task/TaskCard.vue'

const props = defineProps<{
  column: Column
  tasks: Task[]
  allColumns: Column[]
  boardId: string
  dragOver?: boolean
}>()

const emit = defineEmits<{
  'add-task': [columnId: string]
  'open-task': [taskId: string]
  'move-task': [payload: { taskId: string; columnId: string }]
  drop: [columnId: string]
}>()

const isDropTarget = computed(() => props.dragOver === true)

function onAddClick() {
  emit('add-task', props.column.id)
}

function onDragEnter(event: DragEvent) {
  event.preventDefault()
}

function onDragOver(event: DragEvent) {
  event.preventDefault()
  if (event.dataTransfer !== null) {
    event.dataTransfer.dropEffect = 'move'
  }
}

function onDrop(event: DragEvent) {
  event.preventDefault()
  const taskId = event.dataTransfer?.getData('text/plain')
  if (taskId !== undefined && taskId !== '') {
    emit('move-task', { taskId, columnId: props.column.id })
  }
  emit('drop', props.column.id)
}

const taskCount = computed(() => props.tasks.length)
</script>

<template>
  <div
    class="column"
    :class="{ 'column--drop': isDropTarget }"
    @dragenter="onDragEnter"
    @dragover="onDragOver"
    @drop="onDrop"
  >
    <header class="column__header">
      <h3 class="column__name">{{ column.name }}</h3>
      <span class="column__count">{{ taskCount }}</span>
    </header>
    <div class="column__body">
      <TaskCard
        v-for="task in tasks"
        :key="task.id"
        :task="task"
        :columns="allColumns"
        @open="(id: string) => emit('open-task', id)"
      />
      <div v-if="taskCount === 0" class="column__placeholder">No tasks</div>
    </div>
    <button type="button" class="column__add" @click="onAddClick">+ Add task</button>
  </div>
</template>

<style scoped>
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
  margin: 0.5rem 0.75rem 0.75rem;
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
</style>
