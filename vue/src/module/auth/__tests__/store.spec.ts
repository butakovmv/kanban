import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '../store'
import * as api from '../api'
import * as fetchModule from '../../../fetch'
import { authGenerator } from './authGenerator'

vi.mock('../api', async () => {
  const actual = await vi.importActual<typeof api>('../api')
  return {
    ...actual,
    register: vi.fn(),
    login: vi.fn(),
    refresh: vi.fn(),
    logout: vi.fn(),
  }
})

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    setAccessTokenSpy(vi.spyOn(fetchModule, 'setAccessToken'))
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  function setAccessTokenSpy(spy: ReturnType<typeof vi.spyOn>) {
    spy.mockClear()
  }

  it('starts with no authenticated state', () => {
    const store = useAuthStore()
    expect(store.isAuthenticated).toBe(false)
    expect(store.user).toBeNull()
    expect(store.accessToken).toBeNull()
    expect(store.refreshToken).toBeNull()
    expect(store.error).toBeNull()
    expect(store.loading).toBe(false)
  })

  describe('login', () => {
    it('stores tokens and user on successful login', async () => {
      const response = authGenerator.authResponse()
      vi.mocked(api.login).mockResolvedValue(response)

      const store = useAuthStore()
      const success = await store.login({ email: 'user@kanban.test', password: 'pwd' })

      expect(success).toBe(true)
      expect(store.isAuthenticated).toBe(true)
      expect(store.accessToken).toBe(response.accessToken)
      expect(store.refreshToken).toBe(response.refreshToken)
      expect(store.user).toEqual(response.user)
      expect(store.error).toBeNull()
      expect(fetchModule.setAccessToken).toHaveBeenCalledWith(response.accessToken)
    })

    it('sets error on failed login', async () => {
      vi.mocked(api.login).mockRejectedValue(new Error('Invalid email or password'))

      const store = useAuthStore()
      const success = await store.login({ email: 'user@kanban.test', password: 'wrong' })

      expect(success).toBe(false)
      expect(store.isAuthenticated).toBe(false)
      expect(store.error).toBe('Invalid email or password')
      expect(store.accessToken).toBeNull()
    })

    it('sets generic error for non-Error rejection', async () => {
      vi.mocked(api.login).mockRejectedValue('plain string')

      const store = useAuthStore()
      const success = await store.login({ email: 'user@kanban.test', password: 'pwd' })

      expect(success).toBe(false)
      expect(store.error).toBe('Login failed')
    })

    it('toggles loading state', async () => {
      const response = authGenerator.authResponse()
      let resolveLogin: (value: api.AuthResponse) => void = () => {}
      vi.mocked(api.login).mockReturnValue(new Promise((resolve) => { resolveLogin = resolve }))

      const store = useAuthStore()
      const promise = store.login({ email: 'user@kanban.test', password: 'pwd' })

      expect(store.loading).toBe(true)

      resolveLogin(response)
      await promise

      expect(store.loading).toBe(false)
    })
  })

  describe('register', () => {
    it('stores tokens and user on successful registration', async () => {
      const response = authGenerator.authResponse()
      vi.mocked(api.register).mockResolvedValue(response)

      const store = useAuthStore()
      const success = await store.register({
        email: 'new@kanban.test',
        password: 'pwd',
        displayName: 'New User',
      })

      expect(success).toBe(true)
      expect(store.isAuthenticated).toBe(true)
      expect(store.user).toEqual(response.user)
    })

    it('sets error on failed registration', async () => {
      vi.mocked(api.register).mockRejectedValue(new Error('Email already registered'))

      const store = useAuthStore()
      const success = await store.register({
        email: 'existing@kanban.test',
        password: 'pwd',
        displayName: 'User',
      })

      expect(success).toBe(false)
      expect(store.error).toBe('Email already registered')
    })
  })

  describe('refresh', () => {
    it('updates tokens on successful refresh', async () => {
      const initialResponse = authGenerator.authResponse()
      const newTokens = authGenerator.tokens()
      vi.mocked(api.login).mockResolvedValue(initialResponse)
      vi.mocked(api.refresh).mockResolvedValue(newTokens)

      const store = useAuthStore()
      await store.login({ email: 'user@kanban.test', password: 'pwd' })

      const success = await store.refresh()

      expect(success).toBe(true)
      expect(store.accessToken).toBe(newTokens.accessToken)
      expect(store.refreshToken).toBe(newTokens.refreshToken)
      expect(fetchModule.setAccessToken).toHaveBeenLastCalledWith(newTokens.accessToken)
    })

    it('fails when no refresh token', async () => {
      const store = useAuthStore()
      const success = await store.refresh()

      expect(success).toBe(false)
      expect(store.error).toBe('No refresh token')
    })

    it('clears session on failed refresh', async () => {
      const initialResponse = authGenerator.authResponse()
      vi.mocked(api.login).mockResolvedValue(initialResponse)
      vi.mocked(api.refresh).mockRejectedValue(new Error('Invalid refresh token'))

      const store = useAuthStore()
      await store.login({ email: 'user@kanban.test', password: 'pwd' })
      const success = await store.refresh()

      expect(success).toBe(false)
      expect(store.error).toBe('Invalid refresh token')
      expect(store.isAuthenticated).toBe(false)
      expect(store.user).toBeNull()
    })
  })

  describe('logout', () => {
    it('calls api.logout and clears session', async () => {
      const initialResponse = authGenerator.authResponse()
      vi.mocked(api.login).mockResolvedValue(initialResponse)
      vi.mocked(api.logout).mockResolvedValue(undefined)

      const store = useAuthStore()
      await store.login({ email: 'user@kanban.test', password: 'pwd' })
      await store.logout()

      expect(api.logout).toHaveBeenCalledWith({ refreshToken: initialResponse.refreshToken })
      expect(store.isAuthenticated).toBe(false)
      expect(store.user).toBeNull()
      expect(fetchModule.setAccessToken).toHaveBeenLastCalledWith(null)
    })

    it('clears session even if api.logout fails', async () => {
      const initialResponse = authGenerator.authResponse()
      vi.mocked(api.login).mockResolvedValue(initialResponse)
      vi.mocked(api.logout).mockRejectedValue(new Error('Network error'))

      const store = useAuthStore()
      await store.login({ email: 'user@kanban.test', password: 'pwd' })
      await store.logout()

      expect(store.isAuthenticated).toBe(false)
      expect(store.user).toBeNull()
    })

    it('does nothing if not authenticated', async () => {
      const store = useAuthStore()
      await store.logout()

      expect(api.logout).not.toHaveBeenCalled()
    })
  })
})
