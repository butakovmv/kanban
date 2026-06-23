<script setup lang="ts">
import { ref } from 'vue'
import type { CreateDocumentRequest } from './api'

const props = defineProps<{
  projectId: string
  loading?: boolean
}>()

const emit = defineEmits<{
  submit: [request: CreateDocumentRequest]
  cancel: []
}>()

const path = ref('')
const title = ref('')
const content = ref('')
const description = ref('')

function onSubmit() {
  if (path.value.trim() === '' || title.value.trim() === '') return
  const request: CreateDocumentRequest = {
    projectId: props.projectId,
    path: path.value.trim(),
    title: title.value.trim(),
    content: content.value,
  }
  if (description.value.trim() !== '') {
    request.description = description.value.trim()
  }
  emit('submit', request)
}

function onCancel() {
  emit('cancel')
}
</script>

<template>
  <div class="create" role="dialog" aria-modal="true" @click.self="onCancel">
    <div class="create__panel">
      <header class="create__header">
        <h2 class="create__title">Create document</h2>
        <button type="button" class="create__close" aria-label="Close" @click="onCancel">&times;</button>
      </header>
      <form class="create__form" @submit.prevent="onSubmit">
        <label class="create__field">
          <span>Path</span>
          <input v-model="path" type="text" class="create__input" required placeholder="e.g. docs/requirements/overview" />
        </label>
        <label class="create__field">
          <span>Title</span>
          <input v-model="title" type="text" class="create__input" required maxlength="200" placeholder="Document title" />
        </label>
        <label class="create__field">
          <span>Content (Markdown)</span>
          <textarea v-model="content" class="create__textarea" rows="12" placeholder="# Document&#10;&#10;Write markdown content here..." />
        </label>
        <label class="create__field">
          <span>Description</span>
          <textarea v-model="description" class="create__textarea" rows="2" maxlength="2000" placeholder="Optional description" />
        </label>
        <div class="create__actions">
          <button type="button" class="create__button create__button--secondary" @click="onCancel">Cancel</button>
          <button type="submit" class="create__button create__button--primary" :disabled="loading || path.trim() === '' || title.trim() === ''">
            Create
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.create {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.create__panel {
  width: 100%;
  max-width: 40rem;
  max-height: 90vh;
  overflow-y: auto;
  background: var(--color-surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  padding: 1.5rem;
}
.create__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}
.create__title {
  font-size: 1.125rem;
  font-weight: 600;
}
.create__close {
  background: transparent;
  border: none;
  font-size: 1.5rem;
  line-height: 1;
  color: var(--color-text-secondary);
  cursor: pointer;
}
.create__form {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.create__field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.875rem;
  color: var(--color-text-secondary);
}
.create__input,
.create__textarea {
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  background: var(--color-background);
  color: var(--color-text);
  font-family: inherit;
  font-size: 0.875rem;
  resize: vertical;
}
.create__textarea {
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  line-height: 1.4;
}
.create__actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 0.5rem;
}
.create__button {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: var(--radius);
  font-weight: 600;
  font-size: 0.875rem;
  cursor: pointer;
}
.create__button--primary {
  background: var(--color-primary);
  color: #fff;
}
.create__button--primary:hover {
  background: var(--color-primary-hover);
}
.create__button--primary:disabled {
  background: var(--color-text-secondary);
  cursor: not-allowed;
}
.create__button--secondary {
  background: var(--color-background);
  color: var(--color-text);
  border: 1px solid var(--color-border);
}
</style>
