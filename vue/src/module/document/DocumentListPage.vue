<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useDocumentStore } from './store'
import ProjectLayout from '../../component/ProjectLayout.vue'
import DocumentUpload from './DocumentUpload.vue'
import type { CreateDocumentRequest, Document, UpdateDocumentRequest } from './api'

const route = useRoute()
const router = useRouter()
const documentStore = useDocumentStore()
const { documents, loading, error } = storeToRefs(documentStore)

const showCreate = ref(false)
const editingId = ref<string | null>(null)
const editingDoc = ref<Document | null>(null)

const projectId = computed(() => {
  const id = route.params['id']
  return Array.isArray(id) ? id[0] : id
})

interface FlatNode {
  id: string
  name: string
  depth: number
  isLast: boolean
  isFolder: boolean
  expanded: boolean
  doc?: Document
}

function buildFlatTree(docs: Document[]): FlatNode[] {
  interface MutableNode {
    name: string
    isFolder: boolean
    docList: Document[]
    children: MutableNode[]
    expanded: boolean
  }

  const root: MutableNode[] = []

  for (const doc of docs) {
    const segments = doc.path.split('/').filter(Boolean)
    let currentLevel = root

    for (let i = 0; i < segments.length; i++) {
      const isLast = i === segments.length - 1
      const segName = segments[i]

      const existing = currentLevel.find((n) => n.name === segName)
      if (existing !== undefined) {
        if (isLast) {
          existing.isFolder = false
          existing.docList.push(doc)
        }
        currentLevel = existing.children
      } else {
        const node: MutableNode = {
          name: segName,
          isFolder: !isLast,
          docList: isLast ? [doc] : [],
          children: [],
          expanded: true,
        }
        currentLevel.push(node)
        currentLevel = node.children
      }
    }
  }

  function flattenNodes(nodes: MutableNode[], depth: number): FlatNode[] {
    const result: FlatNode[] = []
    for (const node of nodes) {
      const hasDocs = node.docList.length > 0
      const hasChildren = node.children.length > 0
      if (node.isFolder || hasChildren || hasDocs) {
        result.push({
          id: `folder:${node.name}:${depth}`,
          name: node.name,
          depth,
          isLast: false,
          isFolder: true,
          expanded: node.expanded,
          doc: undefined,
        })
        if (node.expanded) {
          for (const d of node.docList) {
            result.push({
              id: d.id,
              name: node.name,
              depth: depth + 1,
              isLast: false,
              isFolder: false,
              expanded: true,
              doc: d,
            })
          }
          result.push(...flattenNodes(node.children, depth + 1))
        }
      }
    }
    return result
  }

  const flat = flattenNodes(root, 0)
  if (flat.length > 0) flat[flat.length - 1].isLast = true
  return flat
}

const flatTree = computed(() => buildFlatTree(documents.value))

function toggleNode(node: FlatNode) {
  if (node.isFolder) {
    node.expanded = !node.expanded
  }
}

function formatTime(value: string): string {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toISOString().replace('T', ' ').slice(0, 16)
}

async function load() {
  if (projectId.value === undefined) return
  await documentStore.loadDocuments(projectId.value)
}

onMounted(load)
watch(projectId, load)

async function handleCreate(request: CreateDocumentRequest) {
  const created = await documentStore.createDocument(request)
  if (created !== null) showCreate.value = false
}

function handleCreateCancel() {
  showCreate.value = false
}

function cancelEdit() {
  editingId.value = null
  editingDoc.value = null
}

async function saveEdit() {
  if (editingDoc.value === null || editingId.value === null) return
  const doc = editingDoc.value
  const request: UpdateDocumentRequest = {
    path: doc.path,
    title: doc.title,
    description: doc.description,
  }
  const updated = await documentStore.updateDocument(doc.id, request)
  if (updated !== null) cancelEdit()
}

function viewDocument(doc: Document) {
  void router.push(`/projects/${projectId.value}/documents/${doc.id}`)
}

async function handleDelete(doc: Document) {
  const confirmed = globalThis.confirm(`Delete "${doc.title}"? This cannot be undone.`)
  if (!confirmed) return
  await documentStore.deleteDocument(doc.id)
}
</script>

<template>
  <ProjectLayout v-if="projectId" :project-id="projectId">
    <button
      type="button"
      class="document-list__create-btn"
      :disabled="projectId === undefined"
      @click="showCreate = true"
    >
      + New
    </button>

    <div v-if="error" class="document-list__error">{{ error }}</div>

    <div v-if="loading && documents.length === 0" class="document-list__loading">
      Loading...
    </div>

    <div v-else-if="flatTree.length > 0" class="document-list__tree">
      <div
        v-for="node in flatTree"
        :key="node.id"
        class="document-list__row"
        :style="{ paddingLeft: (node.depth * 1.5 + 0.75) + 'rem' }"
      >
        <div
          v-if="node.isFolder"
          class="document-list__folder"
          @click="toggleNode(node)"
        >
          <span class="document-list__folder-icon">{{ node.expanded ? '&#9660;' : '&#9654;' }}</span>
          <span class="document-list__folder-name">{{ node.name }}/</span>
        </div>
        <div v-else-if="node.doc" class="document-list__doc" :class="{ 'document-list__doc--editing': editingId === node.doc.id }" @click="viewDocument(node.doc!)">
          <template v-if="editingId !== node.doc.id">
            <div class="document-list__doc-info">
              <span class="document-list__doc-name">{{ node.doc.title }}</span>
              <span class="document-list__doc-path">{{ node.name }}</span>
              <span class="document-list__doc-updated">{{ formatTime(node.doc.updatedAt) }}</span>
              <span v-if="node.doc.description" class="document-list__doc-desc">{{ node.doc.description }}</span>
            </div>
            <div class="document-list__doc-actions">
              <button type="button" class="document-list__action document-list__action--danger" @click.stop="handleDelete(node.doc!)">Delete</button>
            </div>
          </template>
          <div v-else-if="editingDoc" class="document-list__edit-form">
            <label>Title <input v-model="editingDoc.title" type="text" required maxlength="200" /></label>
            <label>Path <input v-model="editingDoc.path" type="text" required /></label>
            <label>Description <textarea :value="editingDoc.description ?? ''" @input="editingDoc.description = ($event.target as HTMLTextAreaElement).value" rows="2" /></label>
            <div class="document-list__edit-actions">
              <button :disabled="loading" @click="saveEdit">Save</button>
              <button @click="cancelEdit">Cancel</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="document-list__empty">
      No documents yet. Click "New" to create one.
    </div>

    <DocumentUpload
      v-if="showCreate && projectId !== undefined"
      :project-id="projectId"
      :loading="loading"
      @submit="handleCreate"
      @cancel="handleCreateCancel"
    />
  </ProjectLayout>
</template>

<style scoped>
.document-list__create-btn {
  padding: 0.5rem 1rem;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius);
  font-weight: 600;
  cursor: pointer;
}
.document-list__create-btn:hover {
  background: var(--color-primary-hover);
}
.document-list__create-btn:disabled {
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
.document-list__tree {
  background: var(--color-surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  padding: 0.5rem 0;
}
.document-list__row {
  display: flex;
  align-items: center;
}
.document-list__folder {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0;
  cursor: pointer;
  user-select: none;
  font-weight: 600;
  font-size: 0.875rem;
  color: var(--color-text);
  width: 100%;
}
.document-list__folder:hover {
  background: var(--color-background);
}
.document-list__folder-icon {
  font-size: 0.625rem;
  width: 1rem;
  text-align: center;
  flex-shrink: 0;
  color: var(--color-text-secondary);
}
.document-list__folder-name {
  color: var(--color-primary);
}
.document-list__doc {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.5rem 0;
  gap: 1rem;
  width: 100%;
  border-bottom: 1px solid var(--color-border);
  cursor: pointer;
}
.document-list__doc:hover {
  background: var(--color-background);
}
.document-list__row:last-child .document-list__doc {
  border-bottom: none;
}
.document-list__doc--editing {
  background: var(--color-background);
}
.document-list__doc-info {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 0.5rem 1rem;
  flex: 1;
  min-width: 0;
}
.document-list__doc-name {
  font-weight: 600;
  font-size: 0.875rem;
}
.document-list__doc-path {
  font-size: 0.75rem;
  color: var(--color-text-secondary);
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
}
.document-list__doc-updated {
  font-size: 0.75rem;
  color: var(--color-text-secondary);
  white-space: nowrap;
}
.document-list__doc-desc {
  width: 100%;
  font-size: 0.75rem;
  color: var(--color-text-secondary);
}
.document-list__doc-actions {
  display: flex;
  gap: 0.25rem;
  flex-shrink: 0;
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
.document-list__action--danger {
  border-color: var(--color-danger);
  color: var(--color-danger);
}
.document-list__action--danger:hover {
  background: var(--color-danger);
  color: #fff;
}
.document-list__edit-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  width: 100%;
}
.document-list__edit-form label {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.75rem;
  color: var(--color-text-secondary);
  flex: 1;
  min-width: 200px;
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
  align-items: flex-end;
  padding-bottom: 0.25rem;
}
.document-list__edit-actions button {
  padding: 0.4rem 0.75rem;
  border: none;
  border-radius: var(--radius);
  font-size: 0.8125rem;
  font-weight: 600;
  cursor: pointer;
}
.document-list__edit-actions button:first-child {
  background: var(--color-primary);
  color: #fff;
}
.document-list__edit-actions button:last-child {
  background: var(--color-background);
  border: 1px solid var(--color-border);
  color: var(--color-text);
}
</style>
