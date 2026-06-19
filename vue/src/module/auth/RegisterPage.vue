<script setup lang="ts">
/**
 * Страница регистрации нового пользователя.
 * Содержит форму с полями name, email и password.
 * После успешной регистрации перенаправляет на `/login`.
 */
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const email = ref('')
const password = ref('')
const displayName = ref('')
const error = ref('')

async function handleSubmit() {
  error.value = ''
  try {
    // TODO: call auth API
    await router.push('/login')
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Registration failed'
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
        <input v-model="displayName" type="text" required />
      </label>
      <label>
        Email
        <input v-model="email" type="email" required />
      </label>
      <label>
        Password
        <input v-model="password" type="password" required />
      </label>
      <button type="submit">Register</button>
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
</style>
