<script setup lang="ts">
/**
 * Модальное окно загрузки документа в проект.
 * Содержит поля «Заголовок», «Описание» и область drag-and-drop для файла.
 * При подтверждении эмитит событие `submit` с base64-кодированным содержимым
 * файла и собранными метаданными.
 */
import { ref } from 'vue'
import type { CreateDocumentRequest } from './api'

const props = defineProps<{
  projectId: string
  uploadedBy: string
  loading?: boolean
}>()

const emit = defineEmits<{
  submit: [request: CreateDocumentRequest]
  cancel: []
}>()

const title = ref('')
const description = ref('')
const file = ref<File | null>(null)
const isDragging = ref(false)
const base64 = ref<string | null>(null)
const readError = ref<string | null>(null)

/**
 * Преобразует файл в base64-строку через FileReader API.
 * @param target файл для чтения
 * @returns Promise с base64-строкой
 */
function readFileAsBase64(target: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const result = reader.result
      if (typeof result === 'string') {
        const commaIndex = result.indexOf(',')
        resolve(commaIndex >= 0 ? result.slice(commaIndex + 1) : result)
      } else {
        reject(new Error('Unexpected reader result type'))
      }
    }
    reader.onerror = () => reject(reader.error ?? new Error('FileReader error'))
    reader.readAsDataURL(target)
  })
}

/**
 * Обрабатывает выбранный или перетащенный файл: сохраняет ссылку
 * и асинхронно вычисляет его base64-представление.
 * @param target выбранный файл
 */
async function applyFile(target: File): Promise<void> {
  file.value = target
  readError.value = null
  base64.value = null
  try {
    base64.value = await readFileAsBase64(target)
  } catch (e: unknown) {
    readError.value = e instanceof Error ? e.message : 'Failed to read file'
    file.value = null
  }
}

function onDragOver(event: DragEvent) {
  event.preventDefault()
  isDragging.value = true
}

function onDragLeave() {
  isDragging.value = false
}

async function onDrop(event: DragEvent) {
  event.preventDefault()
  isDragging.value = false
  const dropped = event.dataTransfer?.files?.[0]
  if (dropped !== undefined) {
    await applyFile(dropped)
  }
}

async function onFileSelect(event: Event) {
  const target = event.target as HTMLInputElement
  const picked = target.files?.[0]
  if (picked !== undefined) {
    await applyFile(picked)
  }
  target.value = ''
}

function clearFile() {
  file.value = null
  base64.value = null
  readError.value = null
}

function onSubmit() {
  if (file.value === null || base64.value === null) {
    return
  }
  const trimmedTitle = title.value.trim()
  if (trimmedTitle === '') {
    return
  }
  const request: CreateDocumentRequest = {
    projectId: props.projectId,
    title: trimmedTitle,
    fileName: file.value.name,
    contentType: file.value.type === '' ? 'application/octet-stream' : file.value.type,
    contentBase64: base64.value,
    uploadedBy: props.uploadedBy,
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
  <div class="upload" role="dialog" aria-modal="true" @click.self="onCancel">
    <div class="upload__panel">
      <header class="upload__header">
        <h2 class="upload__title">Upload document</h2>
        <button
          type="button"
          class="upload__close"
          aria-label="Close"
          @click="onCancel"
        >
          ×
        </button>
      </header>
      <form class="upload__form" @submit.prevent="onSubmit">
        <label class="upload__field">
          <span>Title</span>
          <input
            v-model="title"
            type="text"
            class="upload__input"
            maxlength="200"
            required
            placeholder="Document title"
          />
        </label>
        <label class="upload__field">
          <span>Description</span>
          <textarea
            v-model="description"
            class="upload__textarea"
            rows="3"
            maxlength="2000"
            placeholder="Optional description"
          />
        </label>
        <div
          class="upload__drop"
          :class="{ 'upload__drop--active': isDragging }"
          @dragover="onDragOver"
          @dragleave="onDragLeave"
          @drop="onDrop"
        >
          <p class="upload__drop-text">
            Drag a file here or
            <label class="upload__browse">
              browse
              <input
                type="file"
                class="upload__file-input"
                @change="onFileSelect"
              />
            </label>
          </p>
          <p v-if="file !== null" class="upload__staging">
            Selected: <strong>{{ file.name }}</strong>
            ({{ Math.round(file.size / 1024) }} KB)
            <button type="button" class="upload__clear" @click="clearFile">×</button>
          </p>
          <p v-if="readError !== null" class="upload__error">{{ readError }}</p>
        </div>
        <div class="upload__actions">
          <button type="button" class="upload__button upload__button--secondary" @click="onCancel">
            Cancel
          </button>
          <button
            type="submit"
            class="upload__button upload__button--primary"
            :disabled="
              loading || file === null || base64 === null || title.trim() === ''
            "
          >
            Upload
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.upload {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.upload__panel {
  width: 100%;
  max-width: 32rem;
  background: var(--color-surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  padding: 1.5rem;
}
.upload__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}
.upload__title {
  font-size: 1.125rem;
  font-weight: 600;
}
.upload__close {
  background: transparent;
  border: none;
  font-size: 1.5rem;
  line-height: 1;
  color: var(--color-text-secondary);
  cursor: pointer;
}
.upload__form {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.upload__field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.875rem;
  color: var(--color-text-secondary);
}
.upload__input,
.upload__textarea {
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  background: var(--color-background);
  color: var(--color-text);
  font-family: inherit;
  font-size: 0.875rem;
  resize: vertical;
}
.upload__drop {
  padding: 1.25rem;
  border: 2px dashed var(--color-border);
  border-radius: var(--radius);
  text-align: center;
  background: var(--color-background);
  transition: border-color 0.15s, background 0.15s;
}
.upload__drop--active {
  border-color: var(--color-primary);
  background: var(--color-surface);
}
.upload__drop-text {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
}
.upload__browse {
  color: var(--color-primary);
  cursor: pointer;
  text-decoration: underline;
}
.upload__file-input {
  display: none;
}
.upload__staging {
  margin-top: 0.5rem;
  font-size: 0.8rem;
  color: var(--color-text);
}
.upload__clear {
  margin-left: 0.5rem;
  background: transparent;
  border: none;
  color: var(--color-text-secondary);
  font-size: 1rem;
  cursor: pointer;
}
.upload__error {
  margin-top: 0.5rem;
  font-size: 0.75rem;
  color: var(--color-danger);
}
.upload__actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 0.5rem;
}
.upload__button {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: var(--radius);
  font-weight: 600;
  font-size: 0.875rem;
  cursor: pointer;
}
.upload__button--primary {
  background: var(--color-primary);
  color: #fff;
}
.upload__button--primary:hover {
  background: var(--color-primary-hover);
}
.upload__button--primary:disabled {
  background: var(--color-text-secondary);
  cursor: not-allowed;
}
.upload__button--secondary {
  background: var(--color-background);
  color: var(--color-text);
  border: 1px solid var(--color-border);
}
</style>
