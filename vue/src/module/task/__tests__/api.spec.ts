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
    it('sends GET to /boards/{boardId}/tasks with default include_archived=false', async () => {
      const tasks = taskGenerator.tasks(2, 'c-1', 'b-1')
      vi.mocked(fetchModule.get).mockResolvedValue(tasks)

      const result = await api.listTasks('b-1')

      expect(fetchModule.get).toHaveBeenCalledWith('/boards/b-1/tasks?include_archived=false')
      expect(result).toEqual(tasks)
    })

    it('sends include_archived=true when requested', async () => {
      vi.mocked(fetchModule.get).mockResolvedValue([])

      await api.listTasks('b-1', true)

      expect(fetchModule.get).toHaveBeenCalledWith('/boards/b-1/tasks?include_archived=true')
    })
  })

  describe('getTask', () => {
    it('sends GET to /tasks/{id} and returns the task', async () => {
      const task = taskGenerator.task({ id: 't-1', title: 'Hello' })
      vi.mocked(fetchModule.get).mockResolvedValue(task)

      const result = await api.getTask('t-1')

      expect(fetchModule.get).toHaveBeenCalledWith('/tasks/t-1')
      expect(result).toEqual(task)
    })
  })

  describe('createTask', () => {
    it('sends POST to /tasks with snake_case body and returns the task', async () => {
      const created = taskGenerator.task({ id: 't-1', title: 'New' })
      vi.mocked(fetchModule.post).mockResolvedValue(created)

      const result = await api.createTask({
        boardId: 'b-1',
        columnId: 'c-1',
        title: 'New',
        description: 'Desc',
        assigneeId: 'u-1',
        dueDate: '2025-12-31',
      })

      expect(fetchModule.post).toHaveBeenCalledWith('/tasks', {
        board_id: 'b-1',
        column_id: 'c-1',
        title: 'New',
        description: 'Desc',
        assignee_id: 'u-1',
        due_date: '2025-12-31',
      })
      expect(result).toEqual(created)
    })

    it('omits optional fields when not provided', async () => {
      vi.mocked(fetchModule.post).mockResolvedValue(taskGenerator.task())

      await api.createTask({ boardId: 'b-1', columnId: 'c-1', title: 'X' })

      const body = vi.mocked(fetchModule.post).mock.calls[0]?.[1] as Record<string, unknown>
      expect(body).not.toHaveProperty('description')
      expect(body).not.toHaveProperty('assignee_id')
      expect(body).not.toHaveProperty('due_date')
    })
  })

  describe('updateTask', () => {
    it('sends PUT to /tasks/{id} with provided fields only', async () => {
      const updated = taskGenerator.task({ title: 'Updated' })
      vi.mocked(fetchModule.put).mockResolvedValue(updated)

      const result = await api.updateTask('t-1', { title: 'Updated' })

      expect(fetchModule.put).toHaveBeenCalledWith('/tasks/t-1', { title: 'Updated' })
      expect(result).toEqual(updated)
    })

    it('supports nullable description via null', async () => {
      vi.mocked(fetchModule.put).mockResolvedValue(taskGenerator.task())

      await api.updateTask('t-1', { description: null })

      expect(fetchModule.put).toHaveBeenCalledWith('/tasks/t-1', { description: null })
    })
  })

  describe('moveTask', () => {
    it('sends PATCH to /tasks/{id}/move with column_id and position', async () => {
      const moved = taskGenerator.task({ columnId: 'c-2', position: 3 })
      vi.mocked(fetchModule.patch).mockResolvedValue(moved)

      const result = await api.moveTask('t-1', { columnId: 'c-2', position: 3 })

      expect(fetchModule.patch).toHaveBeenCalledWith('/tasks/t-1/move', {
        column_id: 'c-2',
        position: 3,
      })
      expect(result).toEqual(moved)
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
      const comments = taskGenerator.comments(2, 't-1')
      vi.mocked(fetchModule.get).mockResolvedValue(comments)

      const result = await api.listComments('t-1')

      expect(fetchModule.get).toHaveBeenCalledWith('/tasks/t-1/comments')
      expect(result).toEqual(comments)
    })
  })

  describe('createComment', () => {
    it('sends POST to /tasks/{taskId}/comments with text', async () => {
      const comment = taskGenerator.comment({ text: 'Hi' })
      vi.mocked(fetchModule.post).mockResolvedValue(comment)

      const result = await api.createComment('t-1', { text: 'Hi' })

      expect(fetchModule.post).toHaveBeenCalledWith('/tasks/t-1/comments', { text: 'Hi' })
      expect(result).toEqual(comment)
    })
  })

  describe('updateComment', () => {
    it('sends PUT to /comments/{id} with text', async () => {
      const updated = taskGenerator.comment({ text: 'New' })
      vi.mocked(fetchModule.put).mockResolvedValue(updated)

      const result = await api.updateComment('cm-1', { text: 'New' })

      expect(fetchModule.put).toHaveBeenCalledWith('/comments/cm-1', { text: 'New' })
      expect(result).toEqual(updated)
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
      const files = taskGenerator.files(2, 't-1')
      vi.mocked(fetchModule.get).mockResolvedValue(files)

      const result = await api.listFiles('t-1')

      expect(fetchModule.get).toHaveBeenCalledWith('/tasks/t-1/files')
      expect(result).toEqual(files)
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
