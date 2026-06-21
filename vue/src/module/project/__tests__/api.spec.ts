import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as api from '../api'
import * as fetchModule from '../../../fetch'

vi.mock('../../../fetch', async () => {
  const actual = await vi.importActual<typeof fetchModule>('../../../fetch')
  return {
    ...actual,
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    del: vi.fn(),
  }
})

function rawProject(overrides: Record<string, unknown> = {}) {
  return {
    id: 'p-1',
    owner_id: 'owner-1',
    name: 'Test Project',
    description: 'Test Description',
    created_at: '2025-01-01T00:00:00.000Z',
    updated_at: '2025-01-02T00:00:00.000Z',
    ...overrides,
  }
}

function rawProjectList(...projects: Record<string, unknown>[]) {
  return { projects }
}

describe('project api', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('listProjects', () => {
    it('sends GET to /projects with owner_id query param and returns items', async () => {
      const items = [rawProject({ id: 'a' }), rawProject({ id: 'b' }), rawProject({ id: 'c' })]
      vi.mocked(fetchModule.get).mockResolvedValue(rawProjectList(...items))

      const result = await api.listProjects('owner-1')

      expect(fetchModule.get).toHaveBeenCalledWith('/projects?owner_id=owner-1')
      expect(result).toHaveLength(3)
      expect(result[0].id).toBe('a')
      expect(result[0].ownerId).toBe('owner-1')
    })

    it('encodes special characters in owner_id', async () => {
      vi.mocked(fetchModule.get).mockResolvedValue(rawProjectList())

      await api.listProjects('owner with spaces')

      expect(fetchModule.get).toHaveBeenCalledWith(
        '/projects?owner_id=owner%20with%20spaces',
      )
    })
  })

  describe('getProject', () => {
    it('sends GET to /projects/{id} and returns the project', async () => {
      vi.mocked(fetchModule.get).mockResolvedValue(rawProject({ id: 'p-42' }))

      const result = await api.getProject('p-42')

      expect(fetchModule.get).toHaveBeenCalledWith('/projects/p-42')
      expect(result.id).toBe('p-42')
      expect(result.ownerId).toBe('owner-1')
    })

    it('encodes id in the URL', async () => {
      vi.mocked(fetchModule.get).mockResolvedValue(rawProject())

      await api.getProject('id with/slash')

      expect(fetchModule.get).toHaveBeenCalledWith('/projects/id%20with%2Fslash')
    })
  })

  describe('createProject', () => {
    it('sends POST to /projects with snake_case body and returns the created project', async () => {
      vi.mocked(fetchModule.post).mockResolvedValue(
        rawProject({ id: 'new-id', owner_id: 'owner-7', name: 'New project', description: 'Test' }),
      )

      const result = await api.createProject({ ownerId: 'owner-7', name: 'New project', description: 'Test' })

      expect(fetchModule.post).toHaveBeenCalledWith('/projects', {
        owner_id: 'owner-7',
        name: 'New project',
        description: 'Test',
      })
      expect(result.id).toBe('new-id')
      expect(result.ownerId).toBe('owner-7')
      expect(result.name).toBe('New project')
      expect(result.description).toBe('Test')
    })

    it('omits description key when not provided', async () => {
      vi.mocked(fetchModule.post).mockResolvedValue(rawProject())

      await api.createProject({ ownerId: 'owner-1', name: 'P' })

      const callArgs = vi.mocked(fetchModule.post).mock.calls[0]
      expect(callArgs).toBeDefined()
      const body = callArgs![1] as Record<string, unknown>
      expect(body).not.toHaveProperty('description')
      expect(body).toEqual({ owner_id: 'owner-1', name: 'P' })
    })

    it('passes null description explicitly when provided', async () => {
      vi.mocked(fetchModule.post).mockResolvedValue(rawProject())

      await api.createProject({ ownerId: 'owner-1', name: 'P', description: null })

      expect(fetchModule.post).toHaveBeenCalledWith('/projects', {
        owner_id: 'owner-1',
        name: 'P',
        description: null,
      })
    })
  })

  describe('updateProject', () => {
    it('sends PUT to /projects/{id} with only provided fields', async () => {
      vi.mocked(fetchModule.put).mockResolvedValue(rawProject({ id: 'p-1', name: 'Renamed' }))

      const result = await api.updateProject('p-1', { name: 'Renamed' })

      expect(fetchModule.put).toHaveBeenCalledWith('/projects/p-1', { name: 'Renamed' })
      expect(result.name).toBe('Renamed')
    })

    it('sends description key with null when explicitly set', async () => {
      vi.mocked(fetchModule.put).mockResolvedValue(rawProject({ description: null }))

      await api.updateProject('p-1', { description: null })

      expect(fetchModule.put).toHaveBeenCalledWith('/projects/p-1', {
        description: null,
      })
    })

    it('sends empty body when no fields provided', async () => {
      vi.mocked(fetchModule.put).mockResolvedValue(rawProject())

      await api.updateProject('p-1', {})

      expect(fetchModule.put).toHaveBeenCalledWith('/projects/p-1', {})
    })
  })

  describe('deleteProject', () => {
    it('sends DELETE to /projects/{id}', async () => {
      vi.mocked(fetchModule.del).mockResolvedValue(undefined)

      await api.deleteProject('p-99')

      expect(fetchModule.del).toHaveBeenCalledWith('/projects/p-99')
    })
  })
})
