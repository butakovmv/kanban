<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from './store'
import { storeToRefs } from 'pinia'
import { getTariff } from './api'
import type { TariffInfo } from './api'

const router = useRouter()
const authStore = useAuthStore()
const { user } = storeToRefs(authStore)

const tariff = ref<TariffInfo | null>(null)
const tariffError = ref(false)

onMounted(async () => {
  try {
    tariff.value = await getTariff()
  } catch {
    tariffError.value = true
  }
})

async function handleLogout() {
  await authStore.logout()
  await router.push('/login')
}
</script>

<template>
  <div class="profile">
    <div class="profile__card">
      <h1>Profile</h1>

      <div v-if="user" class="profile__info">
        <div class="profile__field">
          <span class="profile__label">Email</span>
          <span class="profile__value">{{ user.email }}</span>
        </div>
        <div class="profile__field">
          <span class="profile__label">Name</span>
          <span class="profile__value">{{ user.displayName }}</span>
        </div>
        <div class="profile__field">
          <span class="profile__label">ID</span>
          <span class="profile__value profile__value--mono">{{ user.id }}</span>
        </div>
      </div>

      <p v-else class="profile__empty">Not logged in</p>

      <div v-if="tariff" class="profile__tariff">
        <h2>Tariff</h2>
        <div class="profile__field">
          <span class="profile__label">Plan</span>
          <span class="profile__value">{{ tariff.name }}</span>
        </div>
        <div class="profile__tariff-limits">
          <div class="profile__field">
            <span class="profile__label">Max Projects</span>
            <span class="profile__value">{{ tariff.maxProjects }}</span>
          </div>
          <div class="profile__field">
            <span class="profile__label">Boards / Project</span>
            <span class="profile__value">{{ tariff.maxBoardsPerProject }}</span>
          </div>
          <div class="profile__field">
            <span class="profile__label">Tasks / Board</span>
            <span class="profile__value">{{ tariff.maxTasksPerBoard }}</span>
          </div>
          <div class="profile__field">
            <span class="profile__label">Max File Size</span>
            <span class="profile__value">{{ tariff.maxFileSizeMb }} MB</span>
          </div>
          <div class="profile__field">
            <span class="profile__label">Storage Limit</span>
            <span class="profile__value">{{ tariff.maxStorageMb }} MB</span>
          </div>
        </div>
      </div>

      <p v-else-if="tariffError" class="profile__empty">Tariff info unavailable</p>

      <button class="profile__logout-btn" @click="handleLogout">Logout</button>
    </div>
  </div>
</template>

<style scoped>
.profile {
  display: flex;
  justify-content: center;
  padding-top: 4rem;
}
.profile__card {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  width: 100%;
  max-width: 28rem;
  padding: 2rem;
  background: var(--color-surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}
.profile__info {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.profile__field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}
.profile__label {
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--color-text-secondary);
}
.profile__value {
  font-size: 1rem;
  color: var(--color-text);
}
.profile__value--mono {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 0.875rem;
}
.profile__empty {
  color: var(--color-text-secondary);
  font-style: italic;
}
.profile__logout-btn {
  align-self: flex-start;
  padding: 0.5rem 1rem;
  background: var(--color-danger);
  color: #fff;
  border: none;
  border-radius: var(--radius);
  font-weight: 600;
}
.profile__logout-btn:hover {
  opacity: 0.9;
}
</style>
