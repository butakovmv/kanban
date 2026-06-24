<script setup lang="ts">
/**
 * Компонент списка комментариев с формой добавления.
 * Отображает автора, текст, время создания. Позволяет редактировать
 * и удалять собственные комментарии.
 */
import { ref, watch } from 'vue'
import type { Comment } from './api'
import { useTaskStore } from './store'
import { useAuthStore } from '../auth/store'
import { useUserStore } from '../user/store'

const props = defineProps<{
  taskId: string
  comments: Comment[]
}>()

const taskStore = useTaskStore()
const authStore = useAuthStore()
const userStore = useUserStore()
const newText = ref('')
const editingId = ref<string | null>(null)
const editDraft = ref('')

const authorNames = ref<Record<string, string>>({})

watch(
  () => props.taskId,
  () => {
    newText.value = ''
    editingId.value = null
    editDraft.value = ''
  },
)

watch(
  () => props.comments,
  (comments) => {
    const ids = comments.map((c) => c.authorId)
    if (ids.length > 0) {
      userStore.ensureUsers(ids)
    }
  },
  { immediate: true },
)

function currentUserId(): string | null {
  return authStore.user?.id ?? null
}

function isOwn(comment: Comment): boolean {
  const userId = currentUserId()
  return userId !== null && comment.authorId === userId
}

function formatTime(value: string): string {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return date.toISOString().replace('T', ' ').slice(0, 16)
}

async function submit() {
  const text = newText.value.trim()
  if (text === '' || !authStore.user) {
    return
  }
  const created = await taskStore.createComment(props.taskId, {
    authorId: authStore.user.id,
    text,
  })
  if (created !== null) {
    newText.value = ''
  }
}

function startEdit(comment: Comment) {
  editingId.value = comment.id
  editDraft.value = comment.text
}

async function commitEdit() {
  if (editingId.value === null) {
    return
  }
  const text = editDraft.value.trim()
  if (text === '') {
    return
  }
  const updated = await taskStore.updateComment(editingId.value, { text })
  if (updated !== null) {
    editingId.value = null
    editDraft.value = ''
  }
}

function cancelEdit() {
  editingId.value = null
  editDraft.value = ''
}

async function remove(comment: Comment) {
  await taskStore.deleteComment(comment.id)
}
</script>

<template>
  <section class="comments">
    <h3 class="comments__title">Comments</h3>
    <ul v-if="comments.length > 0" class="comments__list">
      <li v-for="comment in comments" :key="comment.id" class="comments__item">
        <div class="comments__meta">
          <span class="comments__author">{{ userStore.getDisplayName(comment.authorId) || comment.authorId }}</span>
          <span class="comments__time">{{ formatTime(comment.createdAt) }}</span>
        </div>
        <p v-if="editingId !== comment.id" class="comments__text">{{ comment.text }}</p>
        <form
          v-else
          class="comments__edit-form"
          @submit.prevent="commitEdit"
        >
          <textarea
            v-model="editDraft"
            class="comments__edit-input"
            rows="3"
            maxlength="2000"
            required
          />
          <div class="comments__edit-actions">
            <button type="submit" class="comments__action comments__action--primary">Save</button>
            <button type="button" class="comments__action" @click="cancelEdit">Cancel</button>
          </div>
        </form>
        <div v-if="isOwn(comment) && editingId !== comment.id" class="comments__actions">
          <button type="button" class="comments__action" @click="startEdit(comment)">Edit</button>
          <button
            type="button"
            class="comments__action comments__action--danger"
            @click="remove(comment)"
          >
            Delete
          </button>
        </div>
      </li>
    </ul>
    <div v-else class="comments__empty">No comments yet.</div>

    <form class="comments__form" @submit.prevent="submit">
      <label>
        <span class="comments__label">Add a comment</span>
        <textarea
          v-model="newText"
          class="comments__input"
          rows="3"
          maxlength="2000"
          required
          placeholder="Write a comment..."
        />
      </label>
      <button
        type="submit"
        class="comments__action comments__action--primary"
        :disabled="newText.trim() === ''"
      >
        Post
      </button>
    </form>
  </section>
</template>

<style scoped>
.comments {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.comments__title {
  font-size: 1rem;
  font-weight: 600;
}
.comments__list {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.comments__item {
  padding: 0.5rem 0.75rem;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
}
.comments__meta {
  display: flex;
  gap: 0.5rem;
  font-size: 0.75rem;
  color: var(--color-text-secondary);
  margin-bottom: 0.25rem;
}
.comments__author {
  font-weight: 600;
}
.comments__text {
  font-size: 0.875rem;
  white-space: pre-wrap;
}
.comments__empty {
  padding: 0.75rem;
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  text-align: center;
  border: 1px dashed var(--color-border);
  border-radius: var(--radius);
}
.comments__form {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.comments__form label {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}
.comments__label {
  font-size: 0.75rem;
  color: var(--color-text-secondary);
}
.comments__input,
.comments__edit-input {
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  background: var(--color-surface);
  color: var(--color-text);
  font-size: 0.875rem;
  font-family: inherit;
  resize: vertical;
}
.comments__actions,
.comments__edit-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.25rem;
}
.comments__action {
  padding: 0.25rem 0.6rem;
  font-size: 0.75rem;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
}
.comments__action--primary {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}
.comments__action--primary:disabled {
  background: var(--color-text-secondary);
  border-color: var(--color-text-secondary);
  cursor: not-allowed;
}
.comments__action--danger {
  color: var(--color-danger);
  border-color: var(--color-danger);
}
.comments__action--danger:hover {
  background: var(--color-danger);
  color: #fff;
}
</style>
