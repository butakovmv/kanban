<script setup lang="ts">
/**
 * Страница списка документов проекта.
 */
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useDocumentStore } from './store'
import { useAuthStore } from '../auth/store'
import { useBoards } from '../../composables/useBoards'
import ProjectLayout from '../../component/ProjectLayout.vue'
import DocumentUpload from './DocumentUpload.vue'
import type { CreateDocumentRequest, Document, UpdateDocumentRequest } from './api'

const route = useRoute()
const documentStore = useDocumentStore()
const authStore = useAuthStore()
const { documents, loading, error } = storeToRefs(documentStore)

const showUpload = ref(false)
const editingId = ref<string | null>(null)
const editTitle = ref('')
const editDescription = ref('')
const MOCK_OWNER_ID = 'mock-owner-id'

const projectId = computed(() => {
  const id = route.params['id']
  return Array.isArray(id) ? id[0] : id
})

const { boards, loadBoards } = useBoards(() => projectId.value)

async function load() {
  if (projectId.value === undefined) return
  await documentStore.loadDocuments(projectId.value)
  await loadBoards()
}

onMounted(load)
watch(projectId, load)

function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / 1024 / 1024).toFixed(1)} MB`
  return `${(bytes / 1024 / 1024 / 1024).toFixed(1)} GB`
}

function formatTime(value: string): string {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toISOString().replace('T', ' ').slice(0, 16)
}

async function handleDownload(doc: Document) {
  const url = await documentStore.getDownloadUrl(doc.id)
  if (url === null) return
  window.open(url, '_blank', 'noopener,noreferrer')
}

function startEdit(doc: Document) {
  editingId.value = doc.id
  editTitle.value = doc.title
  editDescription.value = doc.description ?? ''
}

function cancelEdit() {
  editingId.value = null
  editTitle.value = ''
  editDescription.value = ''
}

async function saveEdit(doc: Document) {
  const request: UpdateDocumentRequest = { title: editTitle.value }
  if (editDescription.value === '') request.description = null
  else request.description = editDescription.value
  const updated = await documentStore.updateDocument(doc.id, request)
  if (updated !== null) cancelEdit()
}

async function handleDelete(doc: Document) {
  const confirmed = globalThis.confirm(`Delete "${doc.title}"? This cannot be undone.`)
  if (!confirmed) return
  await documentStore.deleteDocument(doc.id)
}

async function handleUpload(request: CreateDocumentRequest) {
  const created = await documentStore.createDocument(request)
  if (created !== null) showUpload.value = false
}

function handleUploadCancel() {
  showUpload.value = false
}
</script>

<template>
  <div class="document-list">
    <header class="document-list__header">
      <h1>Documents</h1>
      <button
        type="button"
        class="document-list__upload-btn"
        :disabled="projectId === undefined"
        @click="showUpload = true"
      >
        Upload
      </button>
    </header>

    <ProjectLayout v-if="projectId" :project-id="projectId" :boards="boards">
      <div v-if="error" class="document-list__error">{{ error }}</div>

      <div v-if="loading && documents.length === 0" class="document-list__loading">
        Loading...
      </div>

      <table v-else-if="documents.length > 0" class="document-list__table">
        <thead>
          <tr>
            <th>Title</th>
            <th>File</th>
            <th>Size</th>
            <th>Version</th>
            <th>Updated</th>
            <th class="document-list__actions-col">Actions</th>
          </tr>
        </thead>
        <tbody>
          <template v-for="doc in documents" :key="doc.id">
            <tr v-if="editingId !== doc.id" class="document-list__row">
              <td>
                <button
                  type="button"
                  class="document-list__title-link"
                  :title="`Download ${doc.fileName}`"
                  @click="handleDownload(doc)"
                >
                  {{ doc.title }}
                </button>
                <div v-if="doc.description" class="document-list__description">
                  {{ doc.description }}
                </div>
              </td>
              <td class="document-list__file">{{ doc.fileName }}</td>
              <td class="document-list__size">{{ formatSize(doc.sizeBytes) }}</td>
              <td class="document-list__version">v{{ doc.version }}</td>
              <td class="document-list__updated">{{ formatTime(doc.updatedAt) }}</td>
              <td class="document-list__actions">
                <button type="button" class="document-list__action" :disabled="loading" @click="handleDownload(doc)">
                  Download
                </button>
                <button type="button" class="document-list__action" :disabled="loading" @click="startEdit(doc)">
                  Edit
                </button>
                <button type="button" class="document-list__action document-list__action--danger" :disabled="loading" @click="handleDelete(doc)">
                  Delete
                </button>
              </td>
            </tr>
            <tr v-else class="document-list__edit-row">
              <td colspan="6">
                <form class="document-list__edit-form" @submit.prevent="saveEdit(doc)">
                  <label>
                    Title
                    <input v-model="editTitle" type="text" required maxlength="200" />
                  </label>
                  <label>
                    Description
                    <textarea v-model="editDescription" rows="2" maxlength="2000" />
                  </label>
                  <div class="document-list__edit-actions">
                    <button type="submit" :disabled="loading">Save</button>
                    <button type="button" @click="cancelEdit">Cancel</button>
                  </div>
                </form>
              </td>
            </tr>
          </template>
        </tbody>
      </table>

      <div v-else class="document-list__empty">
        No documents yet. Click "Upload" to add one.
      </div>
    </ProjectLayout>

    <DocumentUpload
      v-if="showUpload && projectId !== undefined"
      :project-id="projectId"
      :uploaded-by="authStore.user?.id ?? MOCK_OWNER_ID"
      :loading="loading"
      @submit="handleUpload"
      @cancel="handleUploadCancel"
    />
  </div>
</template>

<style scoped>
.document-list {
  max-width: 64rem;
  margin: 0 auto;
}
.document-list__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
  gap: 1rem;
}
.document-list__back {
  background: transparent;
  border: none;
  color: var(--color-text-secondary);
  cursor: pointer;
  font-size: 0.875rem;
  padding: 0.25rem 0.5rem;
}
.document-list__back:hover {
  color: var(--color-primary);
}
.document-list__header h1 {
  font-size: 1.5rem;
  flex: 1;
}
.document-list__upload-btn {
  padding: 0.5rem 1rem;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius);
  font-weight: 600;
  cursor: pointer;
}
.document-list__upload-btn:hover {
  background: var(--color-primary-hover);
}
.document-list__upload-btn:disabled {
  background: var(--color-text-secondary);
  cursor: not-allowed;
}
.document-list__error {
  margin-bottom: 1rem;
  padding: 0.75rem 1rem;
  background: var(--color-danger);
  color: #fff;
  border-radius: var(--radius);
}
.document-list__loading,
.document-list__empty {
  padding: 2rem;
  text-align: center;
  color: var(--color-text-secondary);
  background: var(--color-surface);
  border-radius: var(--radius);
}
.document-list__table {
  width: 100%;
  border-collapse: collapse;
  background: var(--color-surface);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow);
}
.document-list__table th,
.document-list__table td {
  padding: 0.75rem 1rem;
  text-align: left;
  font-size: 0.875rem;
  border-bottom: 1px solid var(--color-border);
  vertical-align: middle;
}
.document-list__table th {
  background: var(--color-background);
  color: var(--color-text-secondary);
  font-weight: 600;
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.document-list__table tbody tr:last-child td {
  border-bottom: none;
}
.document-list__title-link {
  background: transparent;
  border: none;
  color: var(--color-primary);
  font-weight: 600;
  padding: 0;
  cursor: pointer;
  text-align: left;
}
.document-list__title-link:hover {
  text-decoration: underline;
}
.document-list__description {
  margin-top: 0.25rem;
  font-size: 0.75rem;
  color: var(--color-text-secondary);
}
.document-list__file,
.document-list__size,
.document-list__version,
.document-list__updated {
  color: var(--color-text-secondary);
  font-size: 0.8125rem;
}
.document-list__updated {
  white-space: nowrap;
}
.document-list__actions-col {
  text-align: right;
}
.document-list__actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.25rem;
  flex-wrap: wrap;
}
.document-list__action {
  padding: 0.25rem 0.5rem;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  color: var(--color-text);
  font-size: 0.75rem;
  cursor: pointer;
}
.document-list__action:hover {
  background: var(--color-surface);
}
.document-list__action:disabled {
  color: var(--color-text-secondary);
  cursor: not-allowed;
}
.document-list__action--danger {
  border-color: var(--color-danger);
  color: var(--color-danger);
}
.document-list__action--danger:hover {
  background: var(--color-danger);
  color: #fff;
}
.document-list__edit-row td {
  background: var(--color-background);
}
.document-list__edit-form {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.document-list__edit-form label {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.75rem;
  color: var(--color-text-secondary);
}
.document-list__edit-form input,
.document-list__edit-form textarea {
  padding: 0.4rem 0.6rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  background: var(--color-surface);
  color: var(--color-text);
  font-size: 0.875rem;
  font-family: inherit;
  resize: vertical;
}
.document-list__edit-actions {
  display: flex;
  gap: 0.5rem;
}
.document-list__edit-actions button {
  padding: 0.4rem 0.75rem;
  border: none;
  border-radius: var(--radius);
  font-size: 0.8125rem;
  font-weight: 600;
  cursor: pointer;
}
.document-list__edit-actions button[type='submit'] {
  background: var(--color-primary);
  color: #fff;
}
.document-list__edit-actions button[type='button'] {
  background: var(--color-background);
  border: 1px solid var(--color-border);
  color: var(--color-text);
}
.document-list__edit-actions button:disabled {
  background: var(--color-text-secondary);
  cursor: not-allowed;
}
</style>
