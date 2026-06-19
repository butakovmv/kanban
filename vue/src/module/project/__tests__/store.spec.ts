import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useProjectStore } from '../store'
import * as api from '../api'
import { projectGenerator } from './projectGenerator'

vi.mock('../api', async () => {
  const actual = await vi.importActual<typeof api>('../api')
  return {
    ...actual,
    listProjects: vi.fn(),
    getProject: vi.fn(),
    createProject: vi.fn(),
    updateProject: vi.fn(),
    deleteProject: vi.fn(),
  }
})

describe('project store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('starts with empty state', () => {
    const store = useProjectStore()
    expect(store.projects).toEqual([])
    expect(store.currentProject).toBeNull()
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
    expect(store.hasProjects).toBe(false)
  })

  describe('loadProjects', () => {
    it('populates projects on success', async () => {
      const projects = projectGenerator.projects(3)
      vi.mocked(api.listProjects).mockResolvedValue(projects)

      const store = useProjectStore()
      const success = await store.loadProjects('owner-1')

      expect(success).toBe(true)
      expect(store.projects).toEqual(projects)
      expect(store.hasProjects).toBe(true)
      expect(api.listProjects).toHaveBeenCalledWith('owner-1')
    })

    it('sets error and clears projects on failure', async () => {
      vi.mocked(api.listProjects).mockRejectedValue(new Error('Network error'))

      const store = useProjectStore()
      const success = await store.loadProjects('owner-1')

      expect(success).toBe(false)
      expect(store.error).toBe('Network error')
      expect(store.projects).toEqual([])
      expect(store.hasProjects).toBe(false)
    })

    it('uses generic error message for non-Error rejection', async () => {
      vi.mocked(api.listProjects).mockRejectedValue('plain string')

      const store = useProjectStore()
      const success = await store.loadProjects('owner-1')

      expect(success).toBe(false)
      expect(store.error).toBe('Failed to load projects')
    })

    it('toggles loading state during load', async () => {
      let resolveList: (value: api.Project[]) => void = () => {}
      vi.mocked(api.listProjects).mockReturnValue(
        new Promise((resolve) => {
          resolveList = resolve
        }),
      )

      const store = useProjectStore()
      const promise = store.loadProjects('owner-1')

      expect(store.loading).toBe(true)

      resolveList([])
      await promise

      expect(store.loading).toBe(false)
    })
  })

  describe('loadProject', () => {
    it('populates currentProject on success', async () => {
      const project = projectGenerator.project({ id: 'p-1' })
      vi.mocked(api.getProject).mockResolvedValue(project)

      const store = useProjectStore()
      const success = await store.loadProject('p-1')

      expect(success).toBe(true)
      expect(store.currentProject).toEqual(project)
    })

    it('sets error and clears currentProject on failure', async () => {
      vi.mocked(api.getProject).mockRejectedValue(new Error('Not found'))

      const store = useProjectStore()
      const success = await store.loadProject('p-1')

      expect(success).toBe(false)
      expect(store.error).toBe('Not found')
      expect(store.currentProject).toBeNull()
    })
  })

  describe('createProject', () => {
    it('appends the new project and returns it on success', async () => {
      const existing = projectGenerator.project({ id: 'p-1' })
      const created = projectGenerator.project({ id: 'p-2' })
      vi.mocked(api.createProject).mockResolvedValue(created)

      const store = useProjectStore()
      store.projects = [existing]

      const request = projectGenerator.createRequest()
      const result = await store.createProject(request)

      expect(result).toEqual(created)
      expect(store.projects).toEqual([existing, created])
    })

    it('sets error and returns null on failure', async () => {
      vi.mocked(api.createProject).mockRejectedValue(new Error('Validation failed'))

      const store = useProjectStore()
      const result = await store.createProject(projectGenerator.createRequest())

      expect(result).toBeNull()
      expect(store.error).toBe('Validation failed')
    })
  })

  describe('updateProject', () => {
    it('replaces the project in the list and returns true on success', async () => {
      const original = projectGenerator.project({ id: 'p-1', name: 'Old' })
      const updated = projectGenerator.project({ id: 'p-1', name: 'New' })
      const other = projectGenerator.project({ id: 'p-2' })
      vi.mocked(api.updateProject).mockResolvedValue(updated)

      const store = useProjectStore()
      store.projects = [original, other]

      const success = await store.updateProject('p-1', { name: 'New' })

      expect(success).toBe(true)
      expect(store.projects).toEqual([updated, other])
    })

    it('updates currentProject when it matches the updated id', async () => {
      const updated = projectGenerator.project({ id: 'p-1', name: 'Renamed' })
      vi.mocked(api.updateProject).mockResolvedValue(updated)

      const store = useProjectStore()
      store.currentProject = projectGenerator.project({ id: 'p-1', name: 'Old' })

      await store.updateProject('p-1', { name: 'Renamed' })

      expect(store.currentProject).toEqual(updated)
    })

    it('does not touch currentProject when ids differ', async () => {
      const updated = projectGenerator.project({ id: 'p-1' })
      const current = projectGenerator.project({ id: 'p-2' })
      vi.mocked(api.updateProject).mockResolvedValue(updated)

      const store = useProjectStore()
      store.currentProject = current

      await store.updateProject('p-1', { name: 'Whatever' })

      expect(store.currentProject).toEqual(current)
    })

    it('sets error and returns false on failure', async () => {
      vi.mocked(api.updateProject).mockRejectedValue(new Error('Server error'))

      const store = useProjectStore()
      const success = await store.updateProject('p-1', { name: 'X' })

      expect(success).toBe(false)
      expect(store.error).toBe('Server error')
    })
  })

  describe('deleteProject', () => {
    it('removes the project from the list and returns true on success', async () => {
      const project = projectGenerator.project({ id: 'p-1' })
      const other = projectGenerator.project({ id: 'p-2' })
      vi.mocked(api.deleteProject).mockResolvedValue(undefined)

      const store = useProjectStore()
      store.projects = [project, other]

      const success = await store.deleteProject('p-1')

      expect(success).toBe(true)
      expect(store.projects).toEqual([other])
    })

    it('clears currentProject when matching id is deleted', async () => {
      vi.mocked(api.deleteProject).mockResolvedValue(undefined)

      const store = useProjectStore()
      store.currentProject = projectGenerator.project({ id: 'p-1' })

      await store.deleteProject('p-1')

      expect(store.currentProject).toBeNull()
    })

    it('sets error and returns false on failure', async () => {
      vi.mocked(api.deleteProject).mockRejectedValue(new Error('Forbidden'))

      const store = useProjectStore()
      const success = await store.deleteProject('p-1')

      expect(success).toBe(false)
      expect(store.error).toBe('Forbidden')
    })
  })

  describe('clearCurrent', () => {
    it('resets currentProject to null', () => {
      const store = useProjectStore()
      store.currentProject = projectGenerator.project()

      store.clearCurrent()

      expect(store.currentProject).toBeNull()
    })
  })
})
