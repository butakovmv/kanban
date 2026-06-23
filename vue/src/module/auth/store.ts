import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import * as authApi from './api'
import { setAccessToken } from '../../fetch'

const STORAGE_KEY_TOKEN = 'kanban_access_token'
const STORAGE_KEY_REFRESH = 'kanban_refresh_token'
const STORAGE_KEY_USER = 'kanban_user'

/**
 * Pinia-хранилище состояния аутентификации.
 * Управляет access/refresh токенами, информацией о пользователе
 * и действиями login/register/refresh/logout.
 * Токены сохраняются в localStorage и восстанавливаются при перезагрузке.
 */
export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(null)
  const refreshToken = ref<string | null>(null)
  const user = ref<authApi.AuthUser | null>(null)
  const error = ref<string | null>(null)
  const loading = ref(false)

  const isAuthenticated = computed(() => accessToken.value !== null)

  function persistSession(token: string, refresh: string, userData: authApi.AuthUser) {
    try {
      localStorage.setItem(STORAGE_KEY_TOKEN, token)
      localStorage.setItem(STORAGE_KEY_REFRESH, refresh)
      localStorage.setItem(STORAGE_KEY_USER, JSON.stringify(userData))
    } catch {
      // localStorage может быть недоступен (SSR, отключен)
    }
  }

  function clearPersistedSession() {
    try {
      localStorage.removeItem(STORAGE_KEY_TOKEN)
      localStorage.removeItem(STORAGE_KEY_REFRESH)
      localStorage.removeItem(STORAGE_KEY_USER)
    } catch {
      // localStorage может быть недоступен
    }
  }

  function restoreSession(): boolean {
    try {
      const token = localStorage.getItem(STORAGE_KEY_TOKEN)
      const refresh = localStorage.getItem(STORAGE_KEY_REFRESH)
      const raw = localStorage.getItem(STORAGE_KEY_USER)
      if (token && refresh && raw) {
        accessToken.value = token
        refreshToken.value = refresh
        user.value = JSON.parse(raw) as authApi.AuthUser
        setAccessToken(token)
        return true
      }
    } catch {
      clearPersistedSession()
    }
    return false
  }

  /**
   * Сохраняет токены и пользователя в состояние хранилища.
   */
  function setSession(response: authApi.AuthResponse): void {
    accessToken.value = response.accessToken
    refreshToken.value = response.refreshToken
    user.value = response.user
    setAccessToken(response.accessToken)
    persistSession(response.accessToken, response.refreshToken, response.user)
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
    clearPersistedSession()
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
      await authApi.register(request)
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Registration failed'
      console.error('Auth store error:', e)
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
      console.error('Auth store error:', e)
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
      console.error('Auth store error: No refresh token')
      return false
    }
    loading.value = true
    error.value = null
    try {
      const tokens = await authApi.refresh({ refreshToken: refreshToken.value })
      accessToken.value = tokens.accessToken
      refreshToken.value = tokens.refreshToken
      setAccessToken(tokens.accessToken)
      if (user.value !== null) {
        persistSession(tokens.accessToken, tokens.refreshToken, user.value)
      }
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Token refresh failed'
      console.error('Auth store error:', e)
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
    const token = refreshToken.value
    clearSession()
    if (token != null) {
      try {
        await authApi.logout({ refreshToken: token })
      } catch {
        // Ошибка logout игнорируется — сессия уже очищена.
      }
    }
  }

  /* Восстанавливаем сессию из localStorage при создании хранилища */
  restoreSession()

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
