<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { listAuditLog, type AuditEntry } from './api'
import { useUserStore } from '../user/store'
import ProjectLayout from '../../component/ProjectLayout.vue'

const route = useRoute()
const userStore = useUserStore()

const items = ref<AuditEntry[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)

const totalPages = computed(() => Math.ceil(total.value / size.value))

const projectId = computed(() => {
  const id = route.params['id']
  return Array.isArray(id) ? id[0] : id
})

const actionLabels: Record<string, string> = {
  'user.registered': 'User registered',
  'project.created': 'Project created',
  'project.updated': 'Project updated',
  'project.member.invited': 'Member invited',
  'project.member.removed': 'Member removed',
  'task.created': 'Task created',
  'task.moved': 'Task moved',
  'task.archived': 'Task archived',
  'task.deleted': 'Task deleted',
  'board.created': 'Board created',
  'board.updated': 'Board updated',
  'board.archived': 'Board archived',
  'document.created': 'Document created',
  'document.updated': 'Document updated',
  'document.deleted': 'Document deleted',
}

function formatDate(dateStr: string): string {
  try {
    const d = new Date(dateStr)
    if (isNaN(d.getTime())) return dateStr
    return d.toLocaleString('ru-RU', { dateStyle: 'short', timeStyle: 'short' })
  } catch {
    return dateStr
  }
}

function actionLabel(action: string): string {
  return actionLabels[action] ?? action
}

async function load() {
  if (!projectId.value) return
  loading.value = true
  try {
    const data = await listAuditLog(projectId.value, page.value, size.value)
    items.value = data.items
    total.value = data.total
    const ids = new Set(data.items.map((i) => i.userId))
    if (ids.size > 0) {
      await userStore.ensureUsers(Array.from(ids))
    }
  } catch {
    items.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function goToPage(p: number) {
  page.value = p
  load()
}

onMounted(load)
watch(projectId, () => {
  page.value = 1
  load()
})
</script>

<template>
  <ProjectLayout :project-id="projectId">
    <div class="audit-page">
      <h1 class="audit-page__title">Audit Log</h1>

      <div v-if="loading" class="audit-page__loading">Loading...</div>

      <div v-else-if="items.length === 0" class="audit-page__empty">No audit entries yet.</div>

      <table v-else class="audit-page__table">
        <thead>
          <tr>
            <th>Action</th>
            <th>User</th>
            <th>Details</th>
            <th>Date</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="entry in items" :key="entry.id">
            <td>{{ actionLabel(entry.action) }}</td>
            <td>{{ userStore.getDisplayName(entry.userId) || '—' }}</td>
            <td>{{ entry.details ?? '—' }}</td>
            <td>{{ formatDate(entry.createdAt) }}</td>
          </tr>
        </tbody>
      </table>

      <div v-if="totalPages > 1" class="audit-page__pagination">
        <button :disabled="page <= 1" @click="goToPage(page - 1)">&laquo; Prev</button>
        <span class="audit-page__page-info">Page {{ page }} of {{ totalPages }}</span>
        <button :disabled="page >= totalPages" @click="goToPage(page + 1)">Next &raquo;</button>
      </div>
    </div>
  </ProjectLayout>
</template>

<style scoped>
.audit-page {
}
.audit-page__title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 1rem;
}
.audit-page__loading,
.audit-page__empty {
  color: var(--color-text-secondary);
  font-size: 0.875rem;
}
.audit-page__table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.8125rem;
}
.audit-page__table th,
.audit-page__table td {
  text-align: left;
  padding: 0.5rem 0.75rem;
  border-bottom: 1px solid var(--color-border);
}
.audit-page__table th {
  font-weight: 600;
  color: var(--color-text-secondary);
}
.audit-page__pagination {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 1rem;
}
.audit-page__pagination button {
  padding: 0.25rem 0.75rem;
  font-size: 0.8125rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  background: var(--color-surface);
  color: var(--color-text);
  cursor: pointer;
}
.audit-page__pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.audit-page__page-info {
  font-size: 0.8125rem;
  color: var(--color-text-secondary);
}
</style>
