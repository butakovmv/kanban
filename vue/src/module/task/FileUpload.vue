<script setup lang="ts">
/**
 * Компонент прикреплённых файлов.
 * Содержит область drag-and-drop (фронтенд-заглушка),
 * список уже загруженных файлов со ссылками на скачивание
 * и кнопками удаления.
 */
import { ref } from 'vue'
import type { FileAttachment } from './api'
import { useTaskStore } from './store'
import { getFileDownloadUrl } from './api'

defineProps<{
  taskId: string
  files: FileAttachment[]
}>()

const taskStore = useTaskStore()
const isDragging = ref(false)
const stagingName = ref<string | null>(null)

function formatSize(bytes: number): string {
  if (bytes < 1024) {
    return `${bytes} B`
  }
  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(1)} KB`
  }
  if (bytes < 1024 * 1024 * 1024) {
    return `${(bytes / 1024 / 1024).toFixed(1)} MB`
  }
  return `${(bytes / 1024 / 1024 / 1024).toFixed(1)} GB`
}

function formatTime(value: string): string {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return date.toISOString().replace('T', ' ').slice(0, 16)
}

function onDragOver(event: DragEvent) {
  event.preventDefault()
  isDragging.value = true
}

function onDragLeave() {
  isDragging.value = false
}

function onDrop(event: DragEvent) {
  event.preventDefault()
  isDragging.value = false
  const files = event.dataTransfer?.files
  if (files === undefined || files.length === 0) {
    return
  }
  const file = files[0]
  if (file !== undefined) {
    stagingName.value = file.name
  }
}

function onFileSelect(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (file !== undefined) {
    stagingName.value = file.name
  }
  target.value = ''
}

function clearStaging() {
  stagingName.value = null
}

function downloadUrl(id: string): string {
  return getFileDownloadUrl(id)
}

async function removeFile(file: FileAttachment) {
  await taskStore.deleteFile(file.id)
}
</script>

<template>
  <section class="files">
    <h3 class="files__title">Files</h3>

    <div
      class="files__drop"
      :class="{ 'files__drop--active': isDragging }"
      @dragover="onDragOver"
      @dragleave="onDragLeave"
      @drop="onDrop"
    >
      <p class="files__drop-text">
        Drag a file here or
        <label class="files__browse">
          browse
          <input
            type="file"
            class="files__input"
            @change="onFileSelect"
          />
        </label>
      </p>
      <p v-if="stagingName !== null" class="files__staging">
        Selected: <strong>{{ stagingName }}</strong>
        <button type="button" class="files__clear" @click="clearStaging">×</button>
      </p>
      <p class="files__hint">Real upload to MinIO is not yet implemented.</p>
    </div>

    <ul v-if="files.length > 0" class="files__list">
      <li v-for="file in files" :key="file.id" class="files__item">
        <div class="files__info">
          <a
            class="files__name"
            :href="downloadUrl(file.id)"
            :download="file.fileName"
          >
            {{ file.fileName }}
          </a>
          <div class="files__meta">
            {{ formatSize(file.sizeBytes) }} · {{ file.contentType }} ·
            {{ formatTime(file.uploadedAt) }}
          </div>
        </div>
        <button
          type="button"
          class="files__remove"
          aria-label="Delete file"
          @click="removeFile(file)"
        >
          ×
        </button>
      </li>
    </ul>
    <div v-else class="files__empty">No files attached.</div>
  </section>
</template>

<style scoped>
.files {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.files__title {
  font-size: 1rem;
  font-weight: 600;
}
.files__drop {
  padding: 1.25rem;
  border: 2px dashed var(--color-border);
  border-radius: var(--radius);
  text-align: center;
  background: var(--color-background);
  transition: border-color 0.15s, background 0.15s;
}
.files__drop--active {
  border-color: var(--color-primary);
  background: var(--color-surface);
}
.files__drop-text {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
}
.files__browse {
  color: var(--color-primary);
  cursor: pointer;
  text-decoration: underline;
}
.files__input {
  display: none;
}
.files__staging {
  margin-top: 0.5rem;
  font-size: 0.8rem;
}
.files__clear {
  margin-left: 0.5rem;
  background: transparent;
  border: none;
  color: var(--color-text-secondary);
  font-size: 1rem;
  cursor: pointer;
}
.files__hint {
  margin-top: 0.25rem;
  font-size: 0.7rem;
  color: var(--color-text-secondary);
}
.files__list {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}
.files__item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
}
.files__info {
  flex: 1;
  min-width: 0;
}
.files__name {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-primary);
  word-break: break-all;
}
.files__meta {
  font-size: 0.7rem;
  color: var(--color-text-secondary);
}
.files__remove {
  background: transparent;
  border: 1px solid var(--color-danger);
  color: var(--color-danger);
  border-radius: var(--radius);
  width: 1.5rem;
  height: 1.5rem;
  line-height: 1;
  font-size: 0.9rem;
  padding: 0;
}
.files__remove:hover {
  background: var(--color-danger);
  color: #fff;
}
.files__empty {
  padding: 0.75rem;
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  text-align: center;
  border: 1px dashed var(--color-border);
  border-radius: var(--radius);
}
</style>
