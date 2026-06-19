import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as api from '../api'
import * as fetchModule from '../../../fetch'
import { projectGenerator } from './projectGenerator'

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

describe('project api', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('listProjects', () => {
    it('sends GET to /projects with owner_id query param and returns items', async () => {
      const items = projectGenerator.projects(3)
      vi.mocked(fetchModule.get).mockResolvedValue({ items, total: items.length })

      const result = await api.listProjects('owner-1')

      expect(fetchModule.get).toHaveBeenCalledWith('/projects?owner_id=owner-1')
      expect(result).toEqual(items)
      expect(result).toHaveLength(3)
    })

    it('encodes special characters in owner_id', async () => {
      vi.mocked(fetchModule.get).mockResolvedValue({ items: [], total: 0 })

      await api.listProjects('owner with spaces')

      expect(fetchModule.get).toHaveBeenCalledWith(
        '/projects?owner_id=owner%20with%20spaces',
      )
    })
  })

  describe('getProject', () => {
    it('sends GET to /projects/{id} and returns the project', async () => {
      const project = projectGenerator.project({ id: 'p-42' })
      vi.mocked(fetchModule.get).mockResolvedValue(project)

      const result = await api.getProject('p-42')

      expect(fetchModule.get).toHaveBeenCalledWith('/projects/p-42')
      expect(result).toEqual(project)
    })

    it('encodes id in the URL', async () => {
      const project = projectGenerator.project()
      vi.mocked(fetchModule.get).mockResolvedValue(project)

      await api.getProject('id with/slash')

      expect(fetchModule.get).toHaveBeenCalledWith('/projects/id%20with%2Fslash')
    })
  })

  describe('createProject', () => {
    it('sends POST to /projects with snake_case body and returns the created project', async () => {
      const request = projectGenerator.createRequest({
        ownerId: 'owner-7',
        name: 'New project',
        description: 'Test',
      })
      const created = projectGenerator.project({
        id: 'new-id',
        ownerId: 'owner-7',
        name: 'New project',
        description: 'Test',
      })
      vi.mocked(fetchModule.post).mockResolvedValue(created)

      const result = await api.createProject(request)

      expect(fetchModule.post).toHaveBeenCalledWith('/projects', {
        owner_id: 'owner-7',
        name: 'New project',
        description: 'Test',
      })
      expect(result).toEqual(created)
    })

    it('omits description key when not provided', async () => {
      const request = projectGenerator.createRequest({ description: undefined })
      const created = projectGenerator.project()
      vi.mocked(fetchModule.post).mockResolvedValue(created)

      await api.createProject(request)

      const callArgs = vi.mocked(fetchModule.post).mock.calls[0]
      expect(callArgs).toBeDefined()
      const body = callArgs![1] as Record<string, unknown>
      expect(body).not.toHaveProperty('description')
      expect(body).toEqual({ owner_id: request.ownerId, name: request.name })
    })

    it('passes null description explicitly when provided', async () => {
      const request = projectGenerator.createRequest({ description: null })
      const created = projectGenerator.project()
      vi.mocked(fetchModule.post).mockResolvedValue(created)

      await api.createProject(request)

      expect(fetchModule.post).toHaveBeenCalledWith('/projects', {
        owner_id: request.ownerId,
        name: request.name,
        description: null,
      })
    })
  })

  describe('updateProject', () => {
    it('sends PUT to /projects/{id} with only provided fields', async () => {
      const updated = projectGenerator.project({ name: 'Renamed' })
      vi.mocked(fetchModule.put).mockResolvedValue(updated)

      const result = await api.updateProject('p-1', { name: 'Renamed' })

      expect(fetchModule.put).toHaveBeenCalledWith('/projects/p-1', { name: 'Renamed' })
      expect(result).toEqual(updated)
    })

    it('sends description key with null when explicitly set', async () => {
      const updated = projectGenerator.project({ description: null })
      vi.mocked(fetchModule.put).mockResolvedValue(updated)

      await api.updateProject('p-1', { description: null })

      expect(fetchModule.put).toHaveBeenCalledWith('/projects/p-1', {
        description: null,
      })
    })

    it('sends empty body when no fields provided', async () => {
      const updated = projectGenerator.project()
      vi.mocked(fetchModule.put).mockResolvedValue(updated)

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
