<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useAccessStore } from './store'

const props = defineProps<{
  groupId: string
}>()

const store = useAccessStore()
const { groupPermissions, loading } = storeToRefs(store)

const selectedResource = ref('project')
const selectedAction = ref('read')
const targetId = ref('')

const resources = ['project', 'board', 'task', 'document', 'group']
const actions = ['read', 'write', 'delete', 'admin']

onMounted(async () => {
  await store.loadGroupPermissions(props.groupId)
})

async function handleGrant() {
  await store.grantPermission(props.groupId, selectedResource.value + ':' + selectedAction.value)
}

async function handleRevoke(permissionId: string) {
  await store.revokePermission(props.groupId, permissionId)
}
</script>

<template>
  <div class="permission-editor">
    <h3>Разрешения группы</h3>

    <div class="permission-editor__grant-form">
      <select v-model="selectedResource">
        <option v-for="r in resources" :key="r" :value="r">{{ r }}</option>
      </select>
      <select v-model="selectedAction">
        <option v-for="a in actions" :key="a" :value="a">{{ a }}</option>
      </select>
      <input
        v-model="targetId"
        type="text"
        placeholder="ID цели (опционально)"
      />
      <button :disabled="loading" @click="handleGrant">Назначить</button>
    </div>

    <table v-if="groupPermissions.length > 0" class="permission-editor__table">
      <thead>
        <tr>
          <th>Ресурс</th>
          <th>Действие</th>
          <th>Цель</th>
          <th />
        </tr>
      </thead>
      <tbody>
        <tr v-for="perm in groupPermissions" :key="perm.id">
          <td>{{ perm.resource }}</td>
          <td>{{ perm.action }}</td>
          <td>{{ perm.targetId ?? '—' }}</td>
          <td>
            <button
              class="permission-editor__revoke-btn"
              :disabled="loading"
              @click="handleRevoke(perm.id)"
            >
              Отозвать
            </button>
          </td>
        </tr>
      </tbody>
    </table>
    <div v-else class="permission-editor__empty">Разрешения не назначены</div>
  </div>
</template>

<style scoped>
.permission-editor__grant-form {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
}
.permission-editor__grant-form select,
.permission-editor__grant-form input {
  padding: 0.375rem 0.5rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  background: var(--color-background);
  color: var(--color-text);
}
.permission-editor__grant-form button {
  padding: 0.375rem 0.75rem;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius);
  font-weight: 600;
}
.permission-editor__table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;
}
.permission-editor__table th,
.permission-editor__table td {
  padding: 0.5rem 0.75rem;
  text-align: left;
  border-bottom: 1px solid var(--color-border);
}
.permission-editor__table th {
  font-weight: 600;
  color: var(--color-text-secondary);
}
.permission-editor__revoke-btn {
  padding: 0.25rem 0.5rem;
  border: 1px solid var(--color-danger);
  border-radius: var(--radius);
  background: none;
  color: var(--color-danger);
  cursor: pointer;
  font-size: 0.75rem;
}
.permission-editor__empty {
  padding: 1.5rem;
  text-align: center;
  color: var(--color-text-secondary);
}
</style>
