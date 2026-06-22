import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as api from '../api'
import * as fetchModule from '../../../fetch'

vi.mock('../../../fetch', async () => {
  const actual = await vi.importActual<typeof fetchModule>('../../../fetch')
  return {
    ...actual,
    post: vi.fn(),
  }
})

describe('auth api', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('register', () => {
    it('sends POST to /auth/register with snake_case body and maps response', async () => {
      const rawResponse = {
        access_token: 'access-abc123',
        refresh_token: 'refresh-def456',
        user: { id: 'u1', email: 'new@kanban.test', display_name: 'New User' },
      }
      vi.mocked(fetchModule.post).mockResolvedValue(rawResponse)

      const result =
        await api.register({
          email: 'new@kanban.test',
          password: 'pwd',
          displayName: 'New User',
        })

      expect(fetchModule.post).toHaveBeenCalledWith('/auth/register', {
        email: 'new@kanban.test',
        password: 'pwd',
        display_name: 'New User',
      })
      expect(result).toEqual({
        accessToken: 'access-abc123',
        refreshToken: 'refresh-def456',
        user: { id: 'u1', email: 'new@kanban.test', displayName: 'New User' },
      })
    })
  })

  describe('login', () => {
    it('sends POST to /auth/login and maps response', async () => {
      const rawResponse = {
        access_token: 'access-xyz',
        refresh_token: 'refresh-789',
        user: { id: 'u2', email: 'user@kanban.test', display_name: 'Test User' },
      }
      vi.mocked(fetchModule.post).mockResolvedValue(rawResponse)

      const result = await api.login({ email: 'user@kanban.test', password: 'pwd' })

      expect(fetchModule.post).toHaveBeenCalledWith('/auth/login', {
        email: 'user@kanban.test',
        password: 'pwd',
      })
      expect(result).toEqual({
        accessToken: 'access-xyz',
        refreshToken: 'refresh-789',
        user: { id: 'u2', email: 'user@kanban.test', displayName: 'Test User' },
      })
    })
  })

  describe('refresh', () => {
    it('sends POST to /auth/refresh with snake_case body and maps response', async () => {
      const rawTokens = { access_token: 'access-new', refresh_token: 'refresh-new' }
      vi.mocked(fetchModule.post).mockResolvedValue(rawTokens)

      const result = await api.refresh({ refreshToken: 'old-refresh' })

      expect(fetchModule.post).toHaveBeenCalledWith('/auth/refresh', {
        refresh_token: 'old-refresh',
      })
      expect(result).toEqual({
        accessToken: 'access-new',
        refreshToken: 'refresh-new',
      })
    })
  })

  describe('logout', () => {
    it('sends POST to /auth/logout with snake_case body', async () => {
      vi.mocked(fetchModule.post).mockResolvedValue(undefined)

      await api.logout({ refreshToken: 'token-to-revoke' })

      expect(fetchModule.post).toHaveBeenCalledWith('/auth/logout', {
        refresh_token: 'token-to-revoke',
      })
    })
  })
})
