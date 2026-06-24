<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { searchTasks, type SearchParams, type SearchResult } from './api'

const router = useRouter()

const query = ref('')
const projectId = ref('')
const status = ref('')
const priority = ref('')
const assigneeId = ref('')
const dueDateFrom = ref('')
const dueDateTo = ref('')
const page = ref(1)
const size = ref(10)

const results = ref<SearchResult[]>([])
const total = ref(0)
const loading = ref(false)
const error = ref<string | null>(null)

const totalPages = ref(0)

let debounceTimer: ReturnType<typeof setTimeout> | null = null

function buildParams(): SearchParams {
  return {
    q: query.value,
    ...(projectId.value && { projectId: projectId.value }),
    ...(status.value && { status: status.value }),
    ...(priority.value && { priority: priority.value }),
    ...(assigneeId.value && { assigneeId: assigneeId.value }),
    ...(dueDateFrom.value && { dueDateFrom: dueDateFrom.value }),
    ...(dueDateTo.value && { dueDateTo: dueDateTo.value }),
    page: page.value,
    size: size.value,
  }
}

async function doSearch() {
  if (query.value.trim() === '') {
    results.value = []
    total.value = 0
    totalPages.value = 0
    return
  }
  loading.value = true
  error.value = null
  try {
    const data = await searchTasks(buildParams())
    results.value = data.results
    total.value = data.total
    totalPages.value = Math.ceil(data.total / size.value)
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Search failed'
    results.value = []
    total.value = 0
    totalPages.value = 0
  } finally {
    loading.value = false
  }
}

function onQueryInput() {
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    page.value = 1
    doSearch()
  }, 300)
}

function clearFilters() {
  projectId.value = ''
  status.value = ''
  priority.value = ''
  assigneeId.value = ''
  dueDateFrom.value = ''
  dueDateTo.value = ''
  page.value = 1
  doSearch()
}

function goToPage(p: number) {
  page.value = p
  doSearch()
}

function goToTask(id: string) {
  void router.push({ name: 'task-detail', params: { id } })
}

function highlight(text: string, q: string): string {
  if (!q.trim()) return text
  const escaped = q.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  return text.replace(new RegExp(`(${escaped})`, 'gi'), '<mark>$1</mark>')
}

watch([projectId, status, priority, assigneeId, dueDateFrom, dueDateTo], () => {
  page.value = 1
  doSearch()
})
</script>

<template>
  <div class="search-page">
    <header class="search-page__header">
      <h1>Search Tasks</h1>
    </header>

    <div class="search-page__bar">
      <input
        v-model="query"
        type="text"
        placeholder="Search tasks..."
        class="search-page__input"
        @input="onQueryInput"
      />
      <button class="search-page__clear-btn" @click="clearFilters">Clear filters</button>
    </div>

    <details class="search-page__filters">
      <summary>Filters</summary>
      <div class="search-page__filter-grid">
        <label>
          Project ID
          <input v-model="projectId" type="text" />
        </label>
        <label>
          Status
          <input v-model="status" type="text" />
        </label>
        <label>
          Priority
          <input v-model="priority" type="text" />
        </label>
        <label>
          Assignee ID
          <input v-model="assigneeId" type="text" />
        </label>
        <label>
          Due date from
          <input v-model="dueDateFrom" type="date" />
        </label>
        <label>
          Due date to
          <input v-model="dueDateTo" type="date" />
        </label>
      </div>
    </details>

    <div v-if="error" class="search-page__error">{{ error }}</div>

    <div v-if="loading" class="search-page__loading">Searching...</div>

    <div v-else-if="query && results.length === 0 && !loading" class="search-page__empty">
      No results found.
    </div>

    <div v-else-if="results.length > 0" class="search-page__results">
      <div class="search-page__meta">{{ total }} task(s) found</div>
      <ul class="search-page__list">
        <li
          v-for="result in results"
          :key="result.id"
          class="search-page__card"
          @click="goToTask(result.id)"
        >
          <div class="search-page__card-title" v-html="highlight(result.title, query)" />
          <div
            v-if="result.description"
            class="search-page__card-desc"
            v-html="highlight(result.description, query)"
          />
          <div class="search-page__card-meta">
            <span class="search-page__badge">Status: {{ result.status }}</span>
            <span v-if="result.priority" class="search-page__badge">Priority: {{ result.priority }}</span>
            <span v-if="result.assigneeId" class="search-page__badge">Assignee: {{ result.assigneeId }}</span>
            <span class="search-page__badge">Rank: {{ result.rank.toFixed(2) }}</span>
          </div>
        </li>
      </ul>

      <div v-if="totalPages > 1" class="search-page__pagination">
        <button
          :disabled="page <= 1"
          @click="goToPage(page - 1)"
        >
          &laquo; Prev
        </button>
        <span class="search-page__page-info">Page {{ page }} of {{ totalPages }}</span>
        <button
          :disabled="page >= totalPages"
          @click="goToPage(page + 1)"
        >
          Next &raquo;
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.search-page {
  max-width: 64rem;
  margin: 0 auto;
}
.search-page__header {
  margin-bottom: 1.5rem;
}
.search-page__header h1 {
  font-size: 1.5rem;
}
.search-page__bar {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1rem;
}
.search-page__input {
  flex: 1;
  padding: 0.6rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  background: var(--color-surface);
  color: var(--color-text);
  font-size: 1rem;
}
.search-page__clear-btn {
  padding: 0.5rem 1rem;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
}
.search-page__filters {
  margin-bottom: 1rem;
  background: var(--color-surface);
  border-radius: var(--radius);
  padding: 0.75rem 1rem;
}
.search-page__filters summary {
  cursor: pointer;
  font-weight: 600;
  color: var(--color-text-secondary);
}
.search-page__filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(14rem, 1fr));
  gap: 0.75rem;
  margin-top: 0.75rem;
}
.search-page__filter-grid label {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  font-size: 0.8rem;
  color: var(--color-text-secondary);
}
.search-page__filter-grid input {
  padding: 0.4rem 0.5rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  background: var(--color-background);
  color: var(--color-text);
}
.search-page__error {
  margin-bottom: 1rem;
  padding: 0.75rem 1rem;
  background: var(--color-danger);
  color: #fff;
  border-radius: var(--radius);
}
.search-page__loading,
.search-page__empty {
  padding: 2rem;
  text-align: center;
  color: var(--color-text-secondary);
  background: var(--color-surface);
  border-radius: var(--radius);
}
.search-page__results {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.search-page__meta {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
}
.search-page__list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 0;
}
.search-page__card {
  background: var(--color-surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  padding: 1rem 1.25rem;
  cursor: pointer;
  transition: transform 0.1s ease;
}
.search-page__card:hover {
  transform: translateY(-1px);
}
.search-page__card-title {
  font-weight: 600;
  margin-bottom: 0.25rem;
}
.search-page__card-title :deep(mark) {
  background: #fde68a;
  color: #92400e;
  padding: 0 0.15rem;
  border-radius: 2px;
}
.search-page__card-desc {
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  margin-bottom: 0.5rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.search-page__card-desc :deep(mark) {
  background: #fde68a;
  color: #92400e;
  padding: 0 0.15rem;
  border-radius: 2px;
}
.search-page__card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}
.search-page__badge {
  font-size: 0.75rem;
  padding: 0.2rem 0.5rem;
  background: var(--color-background);
  border-radius: var(--radius);
  color: var(--color-text-secondary);
}
.search-page__pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  margin-top: 1rem;
}
.search-page__pagination button {
  padding: 0.4rem 0.75rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  color: var(--color-text);
}
.search-page__pagination button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.search-page__page-info {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
}
</style>
