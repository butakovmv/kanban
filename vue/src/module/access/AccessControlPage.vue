<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useAccessStore } from './store'
import PermissionEditor from './PermissionEditor.vue'

const store = useAccessStore()
const { groups, currentGroup, members, loading, error } = storeToRefs(store)

const showCreateForm = ref(false)
const showEditForm = ref(false)
const newName = ref('')
const newDescription = ref('')
const editName = ref('')
const editDescription = ref('')
const newMemberId = ref('')
const activeTab = ref<'members' | 'permissions'>('members')
const deleteConfirmId = ref<string | null>(null)

onMounted(async () => {
  await store.loadGroups()
})

function selectGroup(group: typeof groups.value[number]) {
  store.loadGroup(group.id)
  store.loadMembers(group.id)
  store.loadGroupPermissions(group.id)
  activeTab.value = 'members'
}

async function handleCreate() {
  const created = await store.createGroup({
    name: newName.value,
    description: newDescription.value === '' ? null : newDescription.value,
  })
  if (created !== null) {
    newName.value = ''
    newDescription.value = ''
    showCreateForm.value = false
  }
}

function startEdit() {
  if (currentGroup.value) {
    editName.value = currentGroup.value.name
    editDescription.value = currentGroup.value.description ?? ''
    showEditForm.value = true
  }
}

async function handleEdit() {
  if (!currentGroup.value) return
  const success = await store.updateGroup(currentGroup.value.id, {
    name: editName.value,
    description: editDescription.value === '' ? null : editDescription.value,
  })
  if (success) {
    showEditForm.value = false
  }
}

function confirmDelete(id: string) {
  deleteConfirmId.value = id
}

async function handleDelete() {
  if (deleteConfirmId.value === null) return
  await store.deleteGroup(deleteConfirmId.value)
  deleteConfirmId.value = null
}

function cancelDelete() {
  deleteConfirmId.value = null
}

async function handleAddMember() {
  if (!currentGroup.value || !newMemberId.value.trim()) return
  await store.addMember(currentGroup.value.id, newMemberId.value.trim())
  newMemberId.value = ''
}

async function handleRemoveMember(userId: string) {
  if (!currentGroup.value) return
  await store.removeMember(currentGroup.value.id, userId)
}
</script>

<template>
  <div class="access-control">
    <header class="access-control__header">
      <h1>Управление доступом</h1>
      <button class="access-control__create-btn" @click="showCreateForm = !showCreateForm">
        {{ showCreateForm ? 'Отмена' : 'Создать группу' }}
      </button>
    </header>

    <div v-if="error" class="access-control__error">{{ error }}</div>

    <form
      v-if="showCreateForm"
      class="access-control__form"
      @submit.prevent="handleCreate"
    >
      <label>
        Название
        <input v-model="newName" type="text" required maxlength="200" />
      </label>
      <label>
        Описание
        <textarea v-model="newDescription" rows="3" maxlength="2000" />
      </label>
      <button type="submit" :disabled="loading">Создать</button>
    </form>

    <div class="access-control__layout">
      <aside class="access-control__sidebar">
        <div v-if="loading && groups.length === 0" class="access-control__loading">
          Загрузка...
        </div>
        <ul v-else class="access-control__group-list">
          <li
            v-for="group in groups"
            :key="group.id"
            class="access-control__group-item"
            :class="{ 'access-control__group-item--active': currentGroup?.id === group.id }"
          >
            <button class="access-control__group-btn" @click="selectGroup(group)">
              <span class="access-control__group-name">{{ group.name }}</span>
              <span class="access-control__group-desc">{{ group.description }}</span>
            </button>
            <button
              class="access-control__group-delete"
              title="Удалить"
              @click.stop="confirmDelete(group.id)"
            >
              &times;
            </button>
          </li>
        </ul>
        <div v-if="groups.length === 0 && !loading" class="access-control__empty">
          Группы не созданы
        </div>
      </aside>

      <main v-if="currentGroup" class="access-control__main">
        <div class="access-control__group-info">
          <h2>{{ currentGroup.name }}</h2>
          <p v-if="currentGroup.description">{{ currentGroup.description }}</p>
          <div class="access-control__group-actions">
            <button @click="startEdit">Редактировать</button>
            <button class="access-control__btn-danger" @click="confirmDelete(currentGroup.id)">
              Удалить
            </button>
          </div>
        </div>

        <form
          v-if="showEditForm"
          class="access-control__form"
          @submit.prevent="handleEdit"
        >
          <label>
            Название
            <input v-model="editName" type="text" required maxlength="200" />
          </label>
          <label>
            Описание
            <textarea v-model="editDescription" rows="3" maxlength="2000" />
          </label>
          <button type="submit" :disabled="loading">Сохранить</button>
          <button type="button" @click="showEditForm = false">Отмена</button>
        </form>

        <div class="access-control__tabs">
          <button
            class="access-control__tab"
            :class="{ 'access-control__tab--active': activeTab === 'members' }"
            @click="activeTab = 'members'"
          >
            Участники
          </button>
          <button
            class="access-control__tab"
            :class="{ 'access-control__tab--active': activeTab === 'permissions' }"
            @click="activeTab = 'permissions'"
          >
            Разрешения
          </button>
        </div>

        <div v-if="activeTab === 'members'" class="access-control__tab-content">
          <div class="access-control__add-member">
            <input
              v-model="newMemberId"
              type="text"
              placeholder="ID пользователя"
            />
            <button :disabled="!newMemberId.trim() || loading" @click="handleAddMember">
              Добавить
            </button>
          </div>
          <ul class="access-control__member-list">
            <li
              v-for="member in members"
              :key="member.userId"
              class="access-control__member-item"
            >
              <span>{{ member.userId }}</span>
              <button @click="handleRemoveMember(member.userId)">Удалить</button>
            </li>
          </ul>
          <div v-if="members.length === 0" class="access-control__empty">
            Нет участников
          </div>
        </div>

        <div v-else class="access-control__tab-content">
          <PermissionEditor :group-id="currentGroup.id" />
        </div>
      </main>

      <div v-else class="access-control__placeholder">
        Выберите группу для управления
      </div>
    </div>

    <div v-if="deleteConfirmId !== null" class="access-control__confirm-overlay">
      <div class="access-control__confirm-dialog">
        <p>Вы уверены, что хотите удалить эту группу?</p>
        <button :disabled="loading" @click="handleDelete">Удалить</button>
        <button @click="cancelDelete">Отмена</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.access-control {
  max-width: 80rem;
  margin: 0 auto;
}
.access-control__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}
.access-control__header h1 {
  font-size: 1.5rem;
}
.access-control__create-btn {
  padding: 0.5rem 1rem;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius);
  font-weight: 600;
}
.access-control__create-btn:hover {
  background: var(--color-primary-hover);
}
.access-control__error {
  margin-bottom: 1rem;
  padding: 0.75rem 1rem;
  background: var(--color-danger);
  color: #fff;
  border-radius: var(--radius);
}
.access-control__form {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding: 1.25rem;
  margin-bottom: 1.5rem;
  background: var(--color-surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}
.access-control__form label {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.875rem;
  color: var(--color-text-secondary);
}
.access-control__form input,
.access-control__form textarea {
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  background: var(--color-background);
  color: var(--color-text);
}
.access-control__form button {
  align-self: flex-start;
  padding: 0.5rem 1rem;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius);
  font-weight: 600;
}
.access-control__layout {
  display: grid;
  grid-template-columns: 20rem 1fr;
  gap: 1.5rem;
}
.access-control__sidebar {
  background: var(--color-surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  overflow: hidden;
}
.access-control__loading,
.access-control__empty,
.access-control__placeholder {
  padding: 2rem;
  text-align: center;
  color: var(--color-text-secondary);
}
.access-control__group-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.access-control__group-item {
  display: flex;
  align-items: center;
  border-bottom: 1px solid var(--color-border);
}
.access-control__group-item--active {
  background: var(--color-primary);
  color: #fff;
}
.access-control__group-item--active .access-control__group-desc {
  color: rgba(255, 255, 255, 0.7);
}
.access-control__group-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 0.75rem 1rem;
  border: none;
  background: none;
  color: inherit;
  cursor: pointer;
  text-align: left;
  font-size: 0.875rem;
}
.access-control__group-name {
  font-weight: 600;
}
.access-control__group-desc {
  font-size: 0.75rem;
  color: var(--color-text-secondary);
  margin-top: 0.125rem;
}
.access-control__group-delete {
  padding: 0.5rem 0.75rem;
  border: none;
  background: none;
  color: var(--color-danger);
  cursor: pointer;
  font-size: 1.25rem;
}
.access-control__main {
  background: var(--color-surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  padding: 1.25rem;
}
.access-control__group-info h2 {
  margin: 0 0 0.25rem;
}
.access-control__group-info p {
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  margin: 0 0 0.75rem;
}
.access-control__group-actions {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}
.access-control__group-actions button {
  padding: 0.375rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  background: var(--color-background);
  cursor: pointer;
}
.access-control__btn-danger {
  color: var(--color-danger);
  border-color: var(--color-danger);
}
.access-control__tabs {
  display: flex;
  gap: 0;
  border-bottom: 2px solid var(--color-border);
  margin-bottom: 1rem;
}
.access-control__tab {
  padding: 0.5rem 1rem;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-text-secondary);
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
}
.access-control__tab--active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
}
.access-control__tab-content {
  min-height: 10rem;
}
.access-control__add-member {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}
.access-control__add-member input {
  flex: 1;
  padding: 0.375rem 0.5rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  background: var(--color-background);
  color: var(--color-text);
}
.access-control__add-member button {
  padding: 0.375rem 0.75rem;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius);
  font-weight: 600;
}
.access-control__member-list {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}
.access-control__member-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.5rem 0.75rem;
  background: var(--color-background);
  border-radius: var(--radius);
  font-size: 0.875rem;
}
.access-control__member-item button {
  padding: 0.25rem 0.5rem;
  border: 1px solid var(--color-danger);
  border-radius: var(--radius);
  background: none;
  color: var(--color-danger);
  cursor: pointer;
  font-size: 0.75rem;
}
.access-control__confirm-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.access-control__confirm-dialog {
  background: var(--color-surface);
  padding: 1.5rem;
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  text-align: center;
}
.access-control__confirm-dialog button {
  margin: 0.5rem;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: var(--radius);
  font-weight: 600;
  cursor: pointer;
}
.access-control__confirm-dialog button:first-of-type {
  background: var(--color-danger);
  color: #fff;
}
.access-control__confirm-dialog button:last-of-type {
  background: var(--color-background);
  border: 1px solid var(--color-border);
}
</style>
