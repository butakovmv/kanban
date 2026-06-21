import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import * as authApi from './api'
import { setAccessToken } from '../../fetch'

/**
 * Pinia-хранилище состояния аутентификации.
 * Управляет access/refresh токенами, информацией о пользователе
 * и действиями login/register/refresh/logout.
 */
export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(null)
  const refreshToken = ref<string | null>(null)
  const user = ref<authApi.AuthUser | null>(null)
  const error = ref<string | null>(null)
  const loading = ref(false)

  const isAuthenticated = computed(() => accessToken.value !== null)

  /**
   * Сохраняет токены и пользователя в состояние хранилища.
   */
  function setSession(response: authApi.AuthResponse): void {
    accessToken.value = response.accessToken
    refreshToken.value = response.refreshToken
    user.value = response.user
    setAccessToken(response.accessToken)
    error.value = null
  }

  /**
   * Сбрасывает состояние аутентификации (токены и пользователя).
   */
  function clearSession(): void {
    accessToken.value = null
    refreshToken.value = null
    user.value = null
    setAccessToken(null)
  }

  /**
   * Регистрирует нового пользователя.
   * @param request email, password, displayName
   * @returns true при успешной регистрации, false при ошибке
   */
  async function register(request: authApi.RegisterRequest): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      const response = await authApi.register(request)
      setSession(response)
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Registration failed'
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Аутентифицирует пользователя по email и паролю.
   * @param request email, password
   * @returns true при успешном входе, false при ошибке
   */
  async function login(request: authApi.LoginRequest): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      const response = await authApi.login(request)
      setSession(response)
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Login failed'
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Обновляет пару токенов по текущему refresh-токену.
   * @returns true при успешном обновлении, false при ошибке
   */
  async function refresh(): Promise<boolean> {
    if (refreshToken.value === null) {
      error.value = 'No refresh token'
      return false
    }
    loading.value = true
    error.value = null
    try {
      const tokens = await authApi.refresh({ refreshToken: refreshToken.value })
      accessToken.value = tokens.accessToken
      refreshToken.value = tokens.refreshToken
      setAccessToken(tokens.accessToken)
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Token refresh failed'
      clearSession()
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Выход из системы: аннулирует refresh-токен и очищает сессию.
   */
  async function logout(): Promise<void> {
    if (refreshToken.value != null) {
      try {
        await authApi.logout({ refreshToken: refreshToken.value })
      } catch {
        // Ошибка logout игнорируется — клиент всё равно очищает сессию.
      }
    }
    clearSession()
  }

  return {
    accessToken,
    refreshToken,
    user,
    error,
    loading,
    isAuthenticated,
    register,
    login,
    refresh,
    logout,
  }
})
