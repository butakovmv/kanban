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
import { useRealtime } from '../realtime/useRealtime'
import { getBoard } from '../board/api'
import CommentSystem from './CommentSystem.vue'
import FileUpload from './FileUpload.vue'
import { useUserStore } from '../user/store'

const route = useRoute()
const router = useRouter()
const taskStore = useTaskStore()
const authStore = useAuthStore()
const userStore = useUserStore()
const projectStore = useProjectStore()
const { currentTask, comments, files, loading, error } = storeToRefs(taskStore)

const editing = ref(false)
const draftTitle = ref('')
const draftDescription = ref('')
const draftAssigneeId = ref('')
const draftDueDate = ref('')
const showDeleteConfirm = ref(false)
const memberOptions = ref<{ id: string; name: string }[]>([])

const taskId = computed(() => {
  const id = route.params['id']
  return Array.isArray(id) ? id[0] : id
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
  const boardId = currentTask.value?.boardId
  if (!boardId) return
  try {
    const boardView = await getBoard(boardId)
    await loadMembers(boardView.board.projectId)
  } catch {
    // fallback: no project context
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
    ])
    await resolveProjectAndLoadMembers()
  }
}

onMounted(load)
watch(taskId, load)

const taskBoardId = computed(() => currentTask.value?.boardId)
useRealtime(taskBoardId)

function backToBoard() {
  router.back()
}

function startEdit() {
  if (currentTask.value === null) {
    return
  }
  draftTitle.value = currentTask.value.title
  draftDescription.value = currentTask.value.description ?? ''
  draftAssigneeId.value = currentTask.value.assigneeId ?? ''
  draftDueDate.value = currentTask.value.dueDate ?? ''
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
  const dueDate = draftDueDate.value.trim()
  await taskStore.updateTask(taskId.value, {
    title,
    description: description === '' ? null : description,
    assigneeId: assigneeId === '' ? null : assigneeId,
    dueDate: dueDate === '' ? null : dueDate,
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
  const boardId = currentTask.value?.boardId
  const ok = await taskStore.deleteTask(taskId.value)
  if (ok && boardId !== undefined) {
    await router.push(`/boards/${boardId}`)
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
        <div v-if="currentTask.dueDate" class="task-detail__field">
          <dt>Due date</dt>
          <dd>{{ currentTask.dueDate }}</dd>
        </div>
        <div class="task-detail__field">
          <dt>Column</dt>
          <dd>{{ currentTask.columnId }}</dd>
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
.task-detail__divider {
  border: 0;
  border-top: 1px solid var(--color-border);
  margin: 0.25rem 0;
}
</style>
