import type { AuthResponse, AuthTokens, AuthUser } from '../api'

/**
 * Генератор тестовых данных для auth-модуля.
 * Создаёт случайные сущности для тестов api.ts и store.ts.
 */
export const authGenerator = {
  user(): AuthUser {
    return {
      id: `user-${Math.random().toString(36).slice(2, 10)}`,
      email: `user-${Math.random().toString(36).slice(2, 8)}@kanban.test`,
      displayName: `Test User ${Math.random().toString(36).slice(2, 6)}`,
    }
  },

  tokens(): AuthTokens {
    return {
      accessToken: `access-${Math.random().toString(36).slice(2)}`,
      refreshToken: `refresh-${Math.random().toString(36).slice(2)}`,
    }
  },

  authResponse(): AuthResponse {
    return {
      ...this.tokens(),
      user: this.user(),
    }
  },
}
