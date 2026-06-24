import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as api from '../api'
import * as fetchModule from '../../../fetch'
import { taskGenerator } from './taskGenerator'

vi.mock('../../../fetch', async () => {
  const actual = await vi.importActual<typeof fetchModule>('../../../fetch')
  return {
    ...actual,
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    del: vi.fn(),
    patch: vi.fn(),
  }
})

describe('task api', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('listTasks', () => {
    it('sends GET to /projects/{projectId}/tasks with default include_archived=false', async () => {
      const rawTasks = taskGenerator.rawTasks(2, 'c-1', 'p-1')
      vi.mocked(fetchModule.get).mockResolvedValue({ tasks: rawTasks })

      const result = await api.listTasks('p-1')

      expect(fetchModule.get).toHaveBeenCalledWith('/projects/p-1/tasks?include_archived=false')
      expect(result).toHaveLength(2)
      expect(result[0].projectId).toBe('p-1')
      expect(result[0].columnId).toBe('c-1')
      expect(result[0].position).toBe(0)
      expect(result[1].position).toBe(1)
    })

    it('sends include_archived=true when requested', async () => {
      vi.mocked(fetchModule.get).mockResolvedValue({ tasks: [] })

      await api.listTasks('p-1', true)

      expect(fetchModule.get).toHaveBeenCalledWith('/projects/p-1/tasks?include_archived=true')
    })
  })

  describe('getTask', () => {
    it('sends GET to /tasks/{id} and returns the task', async () => {
      const raw = taskGenerator.rawTask({ id: 't-1', title: 'Hello' })
      vi.mocked(fetchModule.get).mockResolvedValue(raw)

      const result = await api.getTask('t-1')

      expect(fetchModule.get).toHaveBeenCalledWith('/tasks/t-1')
      expect(result.id).toBe('t-1')
      expect(result.title).toBe('Hello')
    })
  })

  describe('createTask', () => {
    it('sends POST to /tasks with snake_case body and returns the task', async () => {
      const raw = taskGenerator.rawTask({ id: 't-1', title: 'New' })
      vi.mocked(fetchModule.post).mockResolvedValue(raw)

      const result = await api.createTask({
        projectId: 'p-1',
        columnId: 'c-1',
        title: 'New',
        description: 'Desc',
        assigneeId: 'u-1',
        dueDate: '2025-12-31',
      })

      expect(fetchModule.post).toHaveBeenCalledWith('/tasks', {
        project_id: 'p-1',
        column_id: 'c-1',
        title: 'New',
        description: 'Desc',
        assignee_id: 'u-1',
        due_date: '2025-12-31',
      })
      expect(result.id).toBe('t-1')
      expect(result.title).toBe('New')
    })

    it('omits optional fields when not provided', async () => {
      vi.mocked(fetchModule.post).mockResolvedValue(taskGenerator.rawTask())

      await api.createTask({ projectId: 'p-1', columnId: 'c-1', title: 'X' })

      const body = vi.mocked(fetchModule.post).mock.calls[0]?.[1] as Record<string, unknown>
      expect(body).not.toHaveProperty('description')
      expect(body).not.toHaveProperty('assignee_id')
      expect(body).not.toHaveProperty('due_date')
    })
  })

  describe('updateTask', () => {
    it('sends PUT to /tasks/{id} with provided fields only', async () => {
      const raw = taskGenerator.rawTask({ title: 'Updated' })
      vi.mocked(fetchModule.put).mockResolvedValue(raw)

      const result = await api.updateTask('t-1', { title: 'Updated' })

      expect(fetchModule.put).toHaveBeenCalledWith('/tasks/t-1', { title: 'Updated' })
      expect(result.title).toBe('Updated')
    })

    it('supports nullable description via null', async () => {
      vi.mocked(fetchModule.put).mockResolvedValue(taskGenerator.rawTask())

      await api.updateTask('t-1', { description: null })

      expect(fetchModule.put).toHaveBeenCalledWith('/tasks/t-1', { description: null })
    })
  })

  describe('moveTask', () => {
    it('sends PATCH to /tasks/{id}/move with column_id and position', async () => {
      const raw = taskGenerator.rawTask({ column_id: 'c-2', position: 3 })
      vi.mocked(fetchModule.patch).mockResolvedValue(raw)

      const result = await api.moveTask('t-1', { columnId: 'c-2', position: 3 })

      expect(fetchModule.patch).toHaveBeenCalledWith('/tasks/t-1/move', {
        column_id: 'c-2',
        position: 3,
      })
      expect(result.columnId).toBe('c-2')
      expect(result.position).toBe(3)
    })
  })

  describe('archiveTask', () => {
    it('sends POST to /tasks/{id}/archive', async () => {
      vi.mocked(fetchModule.post).mockResolvedValue(undefined)

      await api.archiveTask('t-1')

      expect(fetchModule.post).toHaveBeenCalledWith('/tasks/t-1/archive', {})
    })
  })

  describe('deleteTask', () => {
    it('sends DELETE to /tasks/{id}', async () => {
      vi.mocked(fetchModule.del).mockResolvedValue(undefined)

      await api.deleteTask('t-1')

      expect(fetchModule.del).toHaveBeenCalledWith('/tasks/t-1')
    })
  })

  describe('listComments', () => {
    it('sends GET to /tasks/{taskId}/comments', async () => {
      const raw = taskGenerator.rawComments(2, 't-1')
      vi.mocked(fetchModule.get).mockResolvedValue({ comments: raw })

      const result = await api.listComments('t-1')

      expect(fetchModule.get).toHaveBeenCalledWith('/tasks/t-1/comments')
      expect(result).toHaveLength(2)
      expect(result[0].taskId).toBe('t-1')
    })
  })

  describe('createComment', () => {
    it('sends POST to /tasks/{taskId}/comments with text', async () => {
      const raw = taskGenerator.rawComment({ text: 'Hi' })
      vi.mocked(fetchModule.post).mockResolvedValue(raw)

      const result = await api.createComment('t-1', { authorId: 'u-1', text: 'Hi' })

      expect(fetchModule.post).toHaveBeenCalledWith('/tasks/t-1/comments', {
        author_id: 'u-1',
        text: 'Hi',
      })
      expect(result.text).toBe('Hi')
    })
  })

  describe('updateComment', () => {
    it('sends PUT to /comments/{id} with text', async () => {
      const raw = taskGenerator.rawComment({ text: 'New' })
      vi.mocked(fetchModule.put).mockResolvedValue(raw)

      const result = await api.updateComment('cm-1', { text: 'New' })

      expect(fetchModule.put).toHaveBeenCalledWith('/comments/cm-1', { text: 'New' })
      expect(result.text).toBe('New')
    })
  })

  describe('deleteComment', () => {
    it('sends DELETE to /comments/{id}', async () => {
      vi.mocked(fetchModule.del).mockResolvedValue(undefined)

      await api.deleteComment('cm-1')

      expect(fetchModule.del).toHaveBeenCalledWith('/comments/cm-1')
    })
  })

  describe('listFiles', () => {
    it('sends GET to /tasks/{taskId}/files', async () => {
      const raw = taskGenerator.rawFiles(2, 't-1')
      vi.mocked(fetchModule.get).mockResolvedValue({ files: raw })

      const result = await api.listFiles('t-1')

      expect(fetchModule.get).toHaveBeenCalledWith('/tasks/t-1/files')
      expect(result).toHaveLength(2)
      expect(result[0].taskId).toBe('t-1')
    })
  })

  describe('deleteFile', () => {
    it('sends DELETE to /files/{id}', async () => {
      vi.mocked(fetchModule.del).mockResolvedValue(undefined)

      await api.deleteFile('f-1')

      expect(fetchModule.del).toHaveBeenCalledWith('/files/f-1')
    })
  })

  describe('getFileDownloadUrl', () => {
    it('returns a relative URL with the API base', () => {
      expect(api.getFileDownloadUrl('f-1')).toBe('/api/v1/files/f-1/download')
    })

    it('encodes special characters in the id', () => {
      expect(api.getFileDownloadUrl('a/b c')).toBe('/api/v1/files/a%2Fb%20c/download')
    })
  })
})
