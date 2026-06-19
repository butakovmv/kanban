<script setup lang="ts">
/**
 * Модальное окно создания задачи.
 * Содержит поля «Заголовок» и «Описание», а также кнопки Submit/Cancel.
 * При подтверждении эмитит событие `submit` с собранными данными.
 */
import { ref, watch } from 'vue'
import type { CreateTaskRequest } from './api'

const props = defineProps<{
  boardId: string
  columnId: string
  loading?: boolean
}>()

const emit = defineEmits<{
  submit: [request: CreateTaskRequest]
  cancel: []
}>()

const title = ref('')
const description = ref('')

watch(
  () => props.columnId,
  () => {
    title.value = ''
    description.value = ''
  },
)

function onSubmit() {
  const trimmed = title.value.trim()
  if (trimmed === '') {
    return
  }
  const request: CreateTaskRequest = {
    boardId: props.boardId,
    columnId: props.columnId,
    title: trimmed,
  }
  if (description.value.trim() !== '') {
    request.description = description.value
  }
  emit('submit', request)
}

function onCancel() {
  emit('cancel')
}
</script>

<template>
  <div class="modal" role="dialog" aria-modal="true" @click.self="onCancel">
    <div class="modal__panel">
      <header class="modal__header">
        <h2 class="modal__title">New task</h2>
        <button
          type="button"
          class="modal__close"
          aria-label="Close"
          @click="onCancel"
        >
          ×
        </button>
      </header>
      <form class="modal__form" @submit.prevent="onSubmit">
        <label class="modal__field">
          <span>Title</span>
          <input
            v-model="title"
            type="text"
            class="modal__input"
            maxlength="200"
            required
            placeholder="What needs to be done?"
          />
        </label>
        <label class="modal__field">
          <span>Description</span>
          <textarea
            v-model="description"
            class="modal__textarea"
            rows="4"
            maxlength="2000"
            placeholder="Add details (optional)"
          />
        </label>
        <div class="modal__actions">
          <button type="button" class="modal__button modal__button--secondary" @click="onCancel">
            Cancel
          </button>
          <button
            type="submit"
            class="modal__button modal__button--primary"
            :disabled="loading || title.trim() === ''"
          >
            Create
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.modal__panel {
  width: 100%;
  max-width: 28rem;
  background: var(--color-surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  padding: 1.5rem;
}
.modal__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}
.modal__title {
  font-size: 1.125rem;
  font-weight: 600;
}
.modal__close {
  background: transparent;
  border: none;
  font-size: 1.5rem;
  line-height: 1;
  color: var(--color-text-secondary);
}
.modal__form {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.modal__field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.875rem;
  color: var(--color-text-secondary);
}
.modal__input,
.modal__textarea {
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  background: var(--color-background);
  color: var(--color-text);
  font-family: inherit;
  font-size: 0.875rem;
  resize: vertical;
}
.modal__actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 0.5rem;
}
.modal__button {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: var(--radius);
  font-weight: 600;
  font-size: 0.875rem;
}
.modal__button--primary {
  background: var(--color-primary);
  color: #fff;
}
.modal__button--primary:hover {
  background: var(--color-primary-hover);
}
.modal__button--primary:disabled {
  background: var(--color-text-secondary);
  cursor: not-allowed;
}
.modal__button--secondary {
  background: var(--color-background);
  color: var(--color-text);
  border: 1px solid var(--color-border);
}
</style>
