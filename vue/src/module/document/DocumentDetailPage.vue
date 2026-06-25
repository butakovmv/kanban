<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useDocumentStore } from './store'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import ProjectLayout from '../../component/ProjectLayout.vue'

const route = useRoute()
const router = useRouter()
const store = useDocumentStore()
const { currentDocument, loading, error } = storeToRefs(store)

const editing = ref(false)
const draftPath = ref('')
const draftTitle = ref('')
const draftContent = ref('')
const draftDescription = ref('')

const docId = computed(() => {
  const id = route.params['docId']
  return Array.isArray(id) ? id[0] : id
})

const projectId = computed(() => {
  const id = route.params['id']
  return Array.isArray(id) ? id[0] : id
})

async function load() {
  if (docId.value === undefined) return
  editing.value = false
  await store.loadDocument(docId.value)
}

onMounted(load)
watch(docId, load)

function back() {
  void router.push(`/projects/${projectId.value}/documents`)
}

function startEdit() {
  if (currentDocument.value === null) return
  draftPath.value = currentDocument.value.path
  draftTitle.value = currentDocument.value.title
  draftContent.value = currentDocument.value.content
  draftDescription.value = currentDocument.value.description ?? ''
  editing.value = true
}

function cancelEdit() {
  editing.value = false
}

async function saveEdit() {
  if (docId.value === undefined || currentDocument.value === null) return
  const updated = await store.updateDocument(docId.value, {
    path: draftPath.value,
    title: draftTitle.value,
    content: draftContent.value,
    description: draftDescription.value || null,
  })
  if (updated !== null) editing.value = false
}

const renderedContent = computed(() => {
  if (!currentDocument.value?.content) return ''
  const raw = marked.parse(currentDocument.value.content, { async: false }) as string
  return DOMPurify.sanitize(raw)
})
</script>

<template>
  <ProjectLayout v-if="projectId" :project-id="projectId">
    <div class="document-detail">
      <header class="document-detail__header">
        <button class="document-detail__back" @click="back">&larr; Back to documents</button>
        <div v-if="!editing" class="document-detail__header-actions">
          <button class="document-detail__edit-btn" @click="startEdit">Edit</button>
        </div>
        <div v-else class="document-detail__header-actions">
          <button class="document-detail__save-btn" :disabled="loading" @click="saveEdit">Save</button>
          <button class="document-detail__cancel-btn" @click="cancelEdit">Cancel</button>
        </div>
      </header>

      <div v-if="loading && !currentDocument" class="document-detail__loading">Loading...</div>
      <div v-else-if="error" class="document-detail__error">{{ error }}</div>

      <template v-if="currentDocument">
        <div v-if="!editing" class="document-detail__view">
          <h1 class="document-detail__title">{{ currentDocument.title }}</h1>
          <div class="document-detail__meta">
            <span class="document-detail__path">{{ currentDocument.path }}</span>
            <span class="document-detail__updated">Updated: {{ currentDocument.updatedAt }}</span>
            <span v-if="currentDocument.description" class="document-detail__desc">{{ currentDocument.description }}</span>
          </div>
          <div class="document-detail__content" v-html="renderedContent" />
        </div>

        <div v-else class="document-detail__edit">
          <label class="document-detail__field">
            Title
            <input v-model="draftTitle" type="text" required maxlength="200" />
          </label>
          <label class="document-detail__field">
            Path
            <input v-model="draftPath" type="text" required />
          </label>
          <label class="document-detail__field">
            Description
            <textarea v-model="draftDescription" rows="2" />
          </label>
          <label class="document-detail__field document-detail__field--content">
            Content (Markdown)
            <textarea v-model="draftContent" rows="20" class="document-detail__content-input" />
          </label>
        </div>
      </template>
    </div>
  </ProjectLayout>
</template>

<style scoped>
.document-detail {
  max-width: 64rem;
  margin: 0 auto;
}
.document-detail__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
  gap: 1rem;
}
.document-detail__back {
  background: none;
  border: none;
  color: var(--color-primary);
  cursor: pointer;
  font-size: 0.9rem;
  padding: 0;
}
.document-detail__back:hover {
  text-decoration: underline;
}
.document-detail__header-actions {
  display: flex;
  gap: 0.5rem;
}
.document-detail__edit-btn,
.document-detail__save-btn,
.document-detail__cancel-btn {
  padding: 0.4rem 0.8rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}
.document-detail__edit-btn {
  background: var(--color-primary);
  color: #fff;
  border: none;
}
.document-detail__save-btn {
  background: var(--color-primary);
  color: #fff;
  border: none;
}
.document-detail__save-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.document-detail__cancel-btn {
  background: transparent;
  border: 1px solid var(--color-border);
  color: var(--color-text);
}
.document-detail__loading {
  padding: 2rem;
  text-align: center;
  color: var(--color-text-secondary);
}
.document-detail__error {
  padding: 1rem;
  background: var(--color-danger-bg, #fdd);
  color: var(--color-danger, #c00);
  border-radius: 4px;
}
.document-detail__view {
  padding: 0 0.5rem;
}
.document-detail__title {
  font-size: 1.5rem;
  margin: 0 0 0.5rem;
}
.document-detail__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  font-size: 0.85rem;
  color: var(--color-text-secondary);
  margin-bottom: 1.5rem;
}
.document-detail__path {
  font-family: monospace;
}
.document-detail__desc {
  font-style: italic;
}
.document-detail__content {
  line-height: 1.7;
  font-size: 1rem;
  max-height: 70vh;
  overflow-y: auto;
}
.document-detail__content :deep(h1),
.document-detail__content :deep(h2),
.document-detail__content :deep(h3) {
  margin: 1.5rem 0 0.75rem;
}
.document-detail__content :deep(p) {
  margin: 0 0 1rem;
}
.document-detail__content :deep(ul),
.document-detail__content :deep(ol) {
  margin: 0 0 1rem;
  padding-left: 2rem;
}
.document-detail__content :deep(pre) {
  background: var(--color-surface);
  padding: 1rem;
  border-radius: 4px;
  overflow-x: auto;
  border: 1px solid var(--color-border);
}
.document-detail__content :deep(code) {
  font-family: monospace;
  font-size: 0.9em;
}
.document-detail__content :deep(:not(pre) > code) {
  background: var(--color-surface);
  padding: 0.15em 0.4em;
  border-radius: 3px;
  border: 1px solid var(--color-border);
}
.document-detail__content :deep(blockquote) {
  border-left: 3px solid var(--color-primary);
  margin: 0 0 1rem;
  padding: 0.5rem 1rem;
  color: var(--color-text-secondary);
}
.document-detail__content :deep(img) {
  max-width: 100%;
  height: auto;
}
.document-detail__edit {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 0 0.5rem;
}
.document-detail__field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.9rem;
  color: var(--color-text-secondary);
}
.document-detail__field input,
.document-detail__field textarea {
  padding: 0.4rem 0.6rem;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  background: var(--color-bg);
  color: var(--color-text);
  font-size: 0.95rem;
}
.document-detail__field--content textarea {
  font-family: monospace;
  line-height: 1.5;
  resize: vertical;
}
</style>
