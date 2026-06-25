<script setup lang="ts">
/**
 * Страница регистрации нового пользователя.
 * Содержит форму с полями name, email и password.
 * После успешной регистрации перенаправляет на `/login`.
 */
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from './store'
import { storeToRefs } from 'pinia'

const router = useRouter()
const authStore = useAuthStore()
const { error, loading } = storeToRefs(authStore)
const email = ref('')
const password = ref('')
const displayName = ref('')

const displayNameError = ref('')

function validateDisplayName(name: string): boolean {
  const trimmed = name.trim()
  if (trimmed.length < 2) {
    displayNameError.value = 'Name must be at least 2 characters'
    return false
  }
  if (trimmed.length > 100) {
    displayNameError.value = 'Name must be at most 100 characters'
    return false
  }
  displayNameError.value = ''
  return true
}

async function handleSubmit() {
  if (!validateDisplayName(displayName.value)) return
  const success = await authStore.register({
    email: email.value,
    password: password.value,
    displayName: displayName.value.trim(),
  })
  if (success) {
    await router.push('/login')
  }
}
</script>

<template>
  <div class="register">
    <form class="register__form" @submit.prevent="handleSubmit">
      <h1>Register</h1>
      <div v-if="error" class="register__error">{{ error }}</div>
      <label>
        Name
        <input v-model="displayName" type="text" required minlength="2" maxlength="100" />
        <span v-if="displayNameError" class="register__error register__error--field">{{ displayNameError }}</span>
      </label>
      <label>
        Email
        <input v-model="email" type="email" required />
      </label>
      <label>
        Password
        <input v-model="password" type="password" required />
      </label>
      <button type="submit" :disabled="loading">Register</button>
      <RouterLink to="/login">Login</RouterLink>
    </form>
  </div>
</template>

<style scoped>
.register {
  display: flex;
  justify-content: center;
  padding-top: 4rem;
}
.register__form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  width: 100%;
  max-width: 24rem;
  padding: 2rem;
  background: var(--color-surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}
.register__error {
  color: var(--color-danger);
  font-size: 0.875rem;
}
label {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.875rem;
  color: var(--color-text-secondary);
}
input {
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  background: var(--color-background);
  color: var(--color-text);
}
button {
  padding: 0.5rem 1rem;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius);
  font-weight: 600;
}
button:hover {
  background: var(--color-primary-hover);
}
button:disabled {
  background: var(--color-text-secondary);
  cursor: not-allowed;
}
</style>
