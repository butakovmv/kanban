<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const email = ref('')
const password = ref('')
const error = ref('')

async function handleSubmit() {
  error.value = ''
  try {
    // TODO: call auth API
    router.push('/board')
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Login failed'
  }
}
</script>

<template>
  <div class="login">
    <form class="login__form" @submit.prevent="handleSubmit">
      <h1>Login</h1>
      <div v-if="error" class="login__error">{{ error }}</div>
      <label>
        Email
        <input v-model="email" type="email" required />
      </label>
      <label>
        Password
        <input v-model="password" type="password" required />
      </label>
      <button type="submit">Login</button>
      <RouterLink to="/register">Register</RouterLink>
    </form>
  </div>
</template>

<style scoped>
.login {
  display: flex;
  justify-content: center;
  padding-top: 4rem;
}
.login__form {
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
.login__error {
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
