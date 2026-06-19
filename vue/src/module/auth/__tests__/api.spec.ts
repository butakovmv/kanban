import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as api from '../api'
import * as fetchModule from '../../../fetch'
import { authGenerator } from './authGenerator'

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
    it('sends POST to /auth/register with snake_case body', async () => {
      const response = authGenerator.authResponse()
      vi.mocked(fetchModule.post).mockResolvedValue(response)

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
      expect(result).toEqual(response)
    })
  })

  describe('login', () => {
    it('sends POST to /auth/login with email and password', async () => {
      const response = authGenerator.authResponse()
      vi.mocked(fetchModule.post).mockResolvedValue(response)

      const result = await api.login({ email: 'user@kanban.test', password: 'pwd' })

      expect(fetchModule.post).toHaveBeenCalledWith('/auth/login', {
        email: 'user@kanban.test',
        password: 'pwd',
      })
      expect(result).toEqual(response)
    })
  })

  describe('refresh', () => {
    it('sends POST to /auth/refresh with snake_case body', async () => {
      const tokens = authGenerator.tokens()
      vi.mocked(fetchModule.post).mockResolvedValue(tokens)

      const result = await api.refresh({ refreshToken: 'old-refresh' })

      expect(fetchModule.post).toHaveBeenCalledWith('/auth/refresh', {
        refresh_token: 'old-refresh',
      })
      expect(result).toEqual(tokens)
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
