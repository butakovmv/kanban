<script setup lang="ts">
/**
 * Страница деталей задачи.
 * Отображает полную информацию о задаче, позволяет редактировать
 * заголовок/описание/срок/исполнителя, архивировать и удалять задачу.
 * Содержит секции комментариев и прикреплённых файлов.
 */
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useTaskStore } from './store'
import { useAuthStore } from '../auth/store'
import { useProjectStore } from '../project/store'
import CommentSystem from './CommentSystem.vue'
import FileUpload from './FileUpload.vue'
import { useUserStore } from '../user/store'
import * as boardApi from '../board/api'

const route = useRoute()
const router = useRouter()
const taskStore = useTaskStore()
const authStore = useAuthStore()
const userStore = useUserStore()
const projectStore = useProjectStore()
const { currentTask, comments, files, loading, error } = storeToRefs(taskStore)
const { user: currentUser } = storeToRefs(authStore)

const editing = ref(false)
const draftTitle = ref('')
const draftDescription = ref('')
const draftAssigneeId = ref('')
const draftDueDate = ref('')
const draftPriority = ref('')
const showDeleteConfirm = ref(false)
const memberOptions = ref<{ id: string; name: string }[]>([])
const newLabelText = ref('')
const columns = ref<boardApi.Column[]>([])

const taskId = computed(() => {
  const id = route.params['id']
  return Array.isArray(id) ? id[0] : id
})

const columnName = computed(() => {
  if (!currentTask.value) return ''
  const col = columns.value.find(c => c.id === currentTask.value!.columnId)
  return col?.name ?? currentTask.value!.columnId
})

const formattedDeadline = computed(() => {
  if (!currentTask.value?.dueDate) return ''
  const date = new Date(currentTask.value.dueDate)
  if (Number.isNaN(date.getTime())) return currentTask.value.dueDate
  return date.toLocaleDateString('ru-RU', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
})

async function loadMembers(pid: string) {
  await Promise.all([
    projectStore.loadProject(pid),
    projectStore.loadProjectMembers(pid),
  ])
  const ids: string[] = []
  const owner = projectStore.currentProject
  if (owner) {
    ids.push(owner.ownerId)
  }
  for (const m of projectStore.projectMembers) {
    ids.push(m.userId)
  }
  if (ids.length > 0) {
    await userStore.ensureUsers(ids)
  }
  const seen = new Set<string>()
  const opts: { id: string; name: string }[] = []
  if (owner && !seen.has(owner.ownerId)) {
    seen.add(owner.ownerId)
    opts.push({ id: owner.ownerId, name: userStore.getDisplayName(owner.ownerId) || `${owner.ownerId} (owner)` })
  }
  for (const m of projectStore.projectMembers) {
    if (!seen.has(m.userId)) {
      seen.add(m.userId)
      opts.push({ id: m.userId, name: m.displayName })
    }
  }
  memberOptions.value = opts
}

async function resolveProjectAndLoadMembers() {
  const qp = route.query['projectId']
  if (qp && !Array.isArray(qp)) {
    await loadMembers(qp)
    return
  }
  const projectId = currentTask.value?.projectId
  if (!projectId) return
  await loadMembers(projectId)
}

async function loadColumns(projectId: string) {
  try {
    const view = await boardApi.getBoardByProjectId(projectId)
    columns.value = view.columns
  } catch {
    columns.value = []
  }
}

async function load() {
  if (taskId.value === undefined) {
    return
  }
  editing.value = false
  showDeleteConfirm.value = false
  const ok = await taskStore.loadTask(taskId.value)
  if (ok) {
    await Promise.all([
      taskStore.loadComments(taskId.value),
      taskStore.loadFiles(taskId.value),
      currentTask.value?.projectId ? loadColumns(currentTask.value.projectId) : Promise.resolve(),
    ])
    await resolveProjectAndLoadMembers()
  }
}

onMounted(load)
watch(taskId, load)

function backToBoard() {
  router.back()
}

function toDateInputValue(iso: string | null): string {
  if (!iso) return ''
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) return iso
  return date.toISOString().slice(0, 10)
}

function toIsoInstant(dateStr: string): string {
  if (!dateStr) return ''
  if (dateStr.includes('T')) return dateStr
  return `${dateStr}T00:00:00Z`
}

function startEdit() {
  if (currentTask.value === null) {
    return
  }
  draftTitle.value = currentTask.value.title
  draftDescription.value = currentTask.value.description ?? ''
  draftAssigneeId.value = currentTask.value.assigneeId ?? ''
  draftDueDate.value = toDateInputValue(currentTask.value.dueDate)
  draftPriority.value = currentTask.value.priority ?? ''
  editing.value = true
}

function cancelEdit() {
  editing.value = false
}

async function commitEdit() {
  if (taskId.value === undefined) {
    return
  }
  const title = draftTitle.value.trim()
  if (title === '') {
    return
  }
  const description = draftDescription.value.trim()
  const assigneeId = draftAssigneeId.value.trim()
  const dueDate = toIsoInstant(draftDueDate.value.trim())
  const priority = draftPriority.value.trim()
  await taskStore.updateTask(taskId.value, {
    title,
    description: description === '' ? null : description,
    assigneeId: assigneeId === '' ? null : assigneeId,
    dueDate: dueDate === '' ? null : dueDate,
    priority: priority === '' ? null : priority,
    userId: currentUser.value?.id,
  })
  editing.value = false
}

async function archive() {
  if (taskId.value === undefined) {
    return
  }
  await taskStore.archiveTask(taskId.value)
}

function askDelete() {
  showDeleteConfirm.value = true
}

function cancelDelete() {
  showDeleteConfirm.value = false
}

async function confirmDelete() {
  if (taskId.value === undefined) {
    return
  }
  const projectId = currentTask.value?.projectId
  const ok = await taskStore.deleteTask(taskId.value)
  if (ok && projectId !== undefined) {
    await router.push(`/projects/${projectId}/board`)
  }
}

function isAssigneeMe(): boolean {
  if (currentTask.value === null) {
    return false
  }
  return (
    authStore.user !== null && currentTask.value.assigneeId === authStore.user.id
  )
}

async function addLabel() {
  const label = newLabelText.value.trim()
  if (!label || taskId.value === undefined) return
  newLabelText.value = ''
  await taskStore.addLabel(taskId.value, label)
}

async function removeLabel(label: string) {
  if (taskId.value === undefined) return
  await taskStore.removeLabel(taskId.value, label)
}

const assigneeDisplayName = computed(() => {
  if (!currentTask.value?.assigneeId) return ''
  return userStore.getDisplayName(currentTask.value.assigneeId) ?? ''
})

watch(() => currentTask.value?.assigneeId, (id) => {
  if (id) {
    userStore.ensureUsers([id])
  }
}, { immediate: true })
</script>

<template>
  <div class="task-detail">
    <header class="task-detail__header">
      <button type="button" class="task-detail__back" @click="backToBoard">&larr; Back</button>
      <h1>Task</h1>
    </header>

    <div v-if="error" class="task-detail__error">{{ error }}</div>

    <div v-if="loading && currentTask === null" class="task-detail__loading">Loading...</div>

    <div v-else-if="currentTask === null" class="task-detail__not-found">Task not found.</div>

    <article v-else class="task-detail__card">
      <div class="task-detail__row task-detail__row--title">
        <div v-if="!editing" class="task-detail__title-row">
          <h2 class="task-detail__title">{{ currentTask.title }}</h2>
          <button
            v-if="!editing"
            type="button"
            class="task-detail__action"
            @click="startEdit"
          >
            Edit
          </button>
        </div>
        <form
          v-else
          class="task-detail__edit-form"
          @submit.prevent="commitEdit"
        >
          <label>
            <span>Title</span>
            <input
              v-model="draftTitle"
              type="text"
              maxlength="200"
              required
              class="task-detail__input"
            />
          </label>
          <label>
            <span>Description</span>
            <textarea
              v-model="draftDescription"
              class="task-detail__textarea"
              rows="5"
              maxlength="2000"
            />
          </label>
          <label>
            <span>Assignee</span>
            <div class="task-detail__assignee-row">
              <select v-model="draftAssigneeId" class="task-detail__input">
                <option value="">—</option>
                <option v-for="opt in memberOptions" :key="opt.id" :value="opt.id">
                  {{ opt.name }}
                </option>
              </select>
              <button
                type="button"
                class="task-detail__action"
                :disabled="authStore.user === null"
                @click="draftAssigneeId = authStore.user!.id"
              >
                Assign to me
              </button>
            </div>
          </label>
          <label>
            <span>Due date</span>
            <input
              v-model="draftDueDate"
              type="date"
              class="task-detail__input"
            />
          </label>
          <label>
            <span>Priority</span>
            <select v-model="draftPriority" class="task-detail__input">
              <option value="">—</option>
              <option value="low">Low</option>
              <option value="medium">Medium</option>
              <option value="high">High</option>
              <option value="critical">Critical</option>
            </select>
          </label>
          <div class="task-detail__form-actions">
            <button type="submit" class="task-detail__action task-detail__action--primary">
              Save
            </button>
            <button type="button" class="task-detail__action" @click="cancelEdit">Cancel</button>
          </div>
        </form>
      </div>

      <dl v-if="!editing" class="task-detail__meta">
        <div v-if="currentTask.description" class="task-detail__field">
          <dt>Description</dt>
          <dd class="task-detail__description">{{ currentTask.description }}</dd>
        </div>
        <div v-if="formattedDeadline" class="task-detail__field">
          <dt>Deadline</dt>
          <dd>{{ formattedDeadline }}</dd>
        </div>
        <div class="task-detail__field">
          <dt>Priority</dt>
          <dd>
            <span v-if="currentTask.priority" class="task-detail__priority" :class="`task-detail__priority--${currentTask.priority}`" :title="currentTask.priority.charAt(0).toUpperCase() + currentTask.priority.slice(1)" />
            <span :style="{ marginLeft: currentTask.priority ? '0.4rem' : '0' }">{{ currentTask.priority ? currentTask.priority.charAt(0).toUpperCase() + currentTask.priority.slice(1) : '—' }}</span>
          </dd>
        </div>
        <div class="task-detail__field">
          <dt>Column</dt>
          <dd>{{ columnName }}</dd>
        </div>
        <div class="task-detail__field">
          <dt>Assignee</dt>
          <dd>
            <template v-if="currentTask.assigneeId">
              <span v-if="isAssigneeMe()" class="task-detail__you">{{ authStore.user!.displayName }}</span>
              <span v-else>{{ assigneeDisplayName || currentTask.assigneeId }}</span>
              <span v-if="isAssigneeMe()"> (you)</span>
            </template>
            <span v-else>—</span>
          </dd>
        </div>
        <div class="task-detail__field">
          <dt>Labels</dt>
          <dd>
            <div class="task-detail__labels">
              <span v-for="label in currentTask.labels" :key="label" class="task-detail__label">
                {{ label }}
                <button type="button" class="task-detail__label-remove" aria-label="Remove label" @click="removeLabel(label)">&times;</button>
              </span>
              <span v-if="currentTask.labels.length === 0">—</span>
            </div>
            <form class="task-detail__add-label" @submit.prevent="addLabel">
              <input v-model="newLabelText" type="text" placeholder="Add label" maxlength="100" class="task-detail__label-input" />
              <button type="submit" class="task-detail__action" :disabled="!newLabelText.trim()">Add</button>
            </form>
          </dd>
        </div>
        <div class="task-detail__field">
          <dt>Status</dt>
          <dd>{{ currentTask.archived ? 'Archived' : 'Active' }}</dd>
        </div>
        <div class="task-detail__field">
          <dt>Created</dt>
          <dd>{{ currentTask.createdAt }}</dd>
        </div>
        <div class="task-detail__field">
          <dt>Updated</dt>
          <dd>{{ currentTask.updatedAt }}</dd>
        </div>
      </dl>

      <div v-if="!editing" class="task-detail__danger">
        <button
          type="button"
          class="task-detail__action"
          :disabled="currentTask.archived"
          @click="archive"
        >
          Archive
        </button>
        <button
          v-if="!showDeleteConfirm"
          type="button"
          class="task-detail__action task-detail__action--danger"
          @click="askDelete"
        >
          Delete
        </button>
        <div v-else class="task-detail__confirm">
          <span>Delete this task?</span>
          <button
            type="button"
            class="task-detail__action task-detail__action--danger"
            @click="confirmDelete"
          >
            Yes
          </button>
          <button type="button" class="task-detail__action" @click="cancelDelete">No</button>
        </div>
      </div>

      <hr class="task-detail__divider" />

      <CommentSystem :task-id="currentTask.id" :comments="comments" />
      <hr class="task-detail__divider" />
      <FileUpload :task-id="currentTask.id" :files="files" />
    </article>
  </div>
</template>

<style scoped>
.task-detail {
  max-width: 48rem;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.task-detail__header {
  display: flex;
  align-items: center;
  gap: 1rem;
}
.task-detail__header h1 {
  font-size: 1.5rem;
}
.task-detail__back {
  background: transparent;
  border: 1px solid var(--color-border);
  color: var(--color-text-secondary);
  padding: 0.4rem 0.75rem;
  border-radius: var(--radius);
  font-size: 0.875rem;
}
.task-detail__back:hover {
  color: var(--color-primary);
}
.task-detail__error {
  padding: 0.75rem 1rem;
  background: var(--color-danger);
  color: #fff;
  border-radius: var(--radius);
}
.task-detail__loading,
.task-detail__not-found {
  padding: 2rem;
  text-align: center;
  color: var(--color-text-secondary);
  background: var(--color-surface);
  border-radius: var(--radius);
}
.task-detail__card {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 1.5rem;
  background: var(--color-surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}
.task-detail__row--title {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.task-detail__title-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  justify-content: space-between;
}
.task-detail__title {
  font-size: 1.25rem;
  font-weight: 600;
  flex: 1;
}
.task-detail__action {
  padding: 0.4rem 0.75rem;
  font-size: 0.85rem;
  background: var(--color-background);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
}
.task-detail__action:hover {
  background: var(--color-surface);
}
.task-detail__action--primary {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}
.task-detail__action--primary:hover {
  background: var(--color-primary-hover);
}
.task-detail__action--danger {
  color: var(--color-danger);
  border-color: var(--color-danger);
}
.task-detail__action--danger:hover {
  background: var(--color-danger);
  color: #fff;
}
.task-detail__action:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.task-detail__meta {
  display: grid;
  gap: 0.5rem;
  margin: 0;
}
.task-detail__field {
  display: grid;
  grid-template-columns: 9rem 1fr;
  gap: 0.5rem;
  font-size: 0.875rem;
}
.task-detail__field dt {
  color: var(--color-text-secondary);
  font-weight: 500;
}
.task-detail__field dd {
  margin: 0;
  word-break: break-word;
}
.task-detail__description {
  white-space: pre-wrap;
}
.task-detail__you {
  font-size: 0.75rem;
  color: var(--color-primary);
}
.task-detail__priority {
  display: inline-block;
  width: 0.6rem;
  height: 0.6rem;
  border-radius: 50%;
  vertical-align: middle;
}
.task-detail__priority--low {
  background: #9e9e9e;
}
.task-detail__priority--medium {
  background: #4caf50;
}
.task-detail__priority--high {
  background: #ff9800;
}
.task-detail__priority--critical {
  background: #f44336;
}
.task-detail__edit-form {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.task-detail__edit-form label {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.75rem;
  color: var(--color-text-secondary);
}
.task-detail__input,
.task-detail__textarea {
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  background: var(--color-background);
  color: var(--color-text);
  font-family: inherit;
  font-size: 0.875rem;
  resize: vertical;
}
.task-detail__assignee-row {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}
.task-detail__assignee-row input {
  flex: 1;
}
.task-detail__form-actions {
  display: flex;
  gap: 0.5rem;
}
.task-detail__danger {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}
.task-detail__confirm {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  color: var(--color-text-secondary);
}
.task-detail__labels {
  display: flex;
  flex-wrap: wrap;
  gap: 0.3rem;
  align-items: center;
}
.task-detail__label {
  display: inline-flex;
  align-items: center;
  gap: 0.2rem;
  padding: 0.15rem 0.5rem;
  font-size: 0.75rem;
  border-radius: var(--radius);
  background: var(--color-primary);
  color: #fff;
}
.task-detail__label-remove {
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.7);
  font-size: 1rem;
  line-height: 1;
  padding: 0;
  cursor: pointer;
}
.task-detail__label-remove:hover {
  color: #fff;
}
.task-detail__add-label {
  display: flex;
  gap: 0.3rem;
  margin-top: 0.3rem;
}
.task-detail__label-input {
  flex: 1;
  padding: 0.25rem 0.5rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  font-size: 0.8rem;
  background: var(--color-background);
  color: var(--color-text);
  min-width: 6rem;
}
.task-detail__divider {
  border: 0;
  border-top: 1px solid var(--color-border);
  margin: 0.25rem 0;
}
</style>
