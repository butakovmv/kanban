import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useTaskStore } from '../store'
import * as api from '../api'
import { taskGenerator } from './taskGenerator'

vi.mock('../api', async () => {
  const actual = await vi.importActual<typeof api>('../api')
  return {
    ...actual,
    listTasks: vi.fn(),
    getTask: vi.fn(),
    createTask: vi.fn(),
    updateTask: vi.fn(),
    moveTask: vi.fn(),
    archiveTask: vi.fn(),
    deleteTask: vi.fn(),
    listComments: vi.fn(),
    createComment: vi.fn(),
    updateComment: vi.fn(),
    deleteComment: vi.fn(),
    listFiles: vi.fn(),
    deleteFile: vi.fn(),
  }
})

describe('task store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('starts with empty state', () => {
    const store = useTaskStore()
    expect(store.tasks).toEqual([])
    expect(store.currentTask).toBeNull()
    expect(store.comments).toEqual([])
    expect(store.files).toEqual([])
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
    expect(store.hasTasks).toBe(false)
    expect(store.hasComments).toBe(false)
    expect(store.hasFiles).toBe(false)
  })

  describe('loadTasks', () => {
    it('populates tasks and returns true on success', async () => {
      const tasks = taskGenerator.tasks(3)
      vi.mocked(api.listTasks).mockResolvedValue(tasks)

      const store = useTaskStore()
      const ok = await store.loadTasks('b-1')

      expect(ok).toBe(true)
      expect(store.tasks).toEqual(tasks)
      expect(store.hasTasks).toBe(true)
      expect(api.listTasks).toHaveBeenCalledWith('b-1', false)
    })

    it('passes includeArchived flag to api', async () => {
      vi.mocked(api.listTasks).mockResolvedValue([])

      const store = useTaskStore()
      await store.loadTasks('b-1', true)

      expect(api.listTasks).toHaveBeenCalledWith('b-1', true)
    })

    it('clears tasks and sets error on failure', async () => {
      vi.mocked(api.listTasks).mockRejectedValue(new Error('boom'))

      const store = useTaskStore()
      const ok = await store.loadTasks('b-1')

      expect(ok).toBe(false)
      expect(store.error).toBe('boom')
      expect(store.tasks).toEqual([])
    })
  })

  describe('loadTask', () => {
    it('populates currentTask on success', async () => {
      const task = taskGenerator.task({ id: 't-1' })
      vi.mocked(api.getTask).mockResolvedValue(task)

      const store = useTaskStore()
      const ok = await store.loadTask('t-1')

      expect(ok).toBe(true)
      expect(store.currentTask).toEqual(task)
    })

    it('returns false and clears currentTask on failure', async () => {
      vi.mocked(api.getTask).mockRejectedValue(new Error('not found'))

      const store = useTaskStore()
      const ok = await store.loadTask('t-1')

      expect(ok).toBe(false)
      expect(store.currentTask).toBeNull()
    })
  })

  describe('createTask', () => {
    it('appends the new task to the list', async () => {
      const created = taskGenerator.task({ id: 't-new' })
      vi.mocked(api.createTask).mockResolvedValue(created)

      const store = useTaskStore()
      const result = await store.createTask({
        boardId: 'b-1',
        columnId: 'c-1',
        title: 'New',
      })

      expect(result).toEqual(created)
      expect(store.tasks).toHaveLength(1)
      expect(store.tasks[0]).toEqual(created)
    })

    it('returns null on failure', async () => {
      vi.mocked(api.createTask).mockRejectedValue(new Error('denied'))

      const store = useTaskStore()
      const result = await store.createTask({ boardId: 'b-1', columnId: 'c-1', title: 'X' })

      expect(result).toBeNull()
      expect(store.error).toBe('denied')
    })
  })

  describe('updateTask', () => {
    it('replaces the task in the list and in currentTask', async () => {
      const original = taskGenerator.task({ id: 't-1', title: 'Old' })
      const updated = taskGenerator.task({ id: 't-1', title: 'New' })
      vi.mocked(api.updateTask).mockResolvedValue(updated)

      const store = useTaskStore()
      store.tasks = [original]
      store.currentTask = original

      const result = await store.updateTask('t-1', { title: 'New' })

      expect(result).toEqual(updated)
      expect(store.tasks[0]).toEqual(updated)
      expect(store.currentTask).toEqual(updated)
    })

    it('returns null on failure', async () => {
      vi.mocked(api.updateTask).mockRejectedValue(new Error('oops'))

      const store = useTaskStore()
      const result = await store.updateTask('t-1', { title: 'X' })

      expect(result).toBeNull()
      expect(store.error).toBe('oops')
    })
  })

  describe('moveTask', () => {
    it('updates the task in the list and currentTask', async () => {
      const original = taskGenerator.task({ id: 't-1', columnId: 'c-1' })
      const moved = taskGenerator.task({ id: 't-1', columnId: 'c-2', position: 1 })
      vi.mocked(api.moveTask).mockResolvedValue(moved)

      const store = useTaskStore()
      store.tasks = [original]
      store.currentTask = original

      const result = await store.moveTask('t-1', { columnId: 'c-2', position: 1 })

      expect(result).toEqual(moved)
      expect(store.tasks[0]).toEqual(moved)
    })

    it('returns null on failure', async () => {
      vi.mocked(api.moveTask).mockRejectedValue(new Error('conflict'))

      const store = useTaskStore()
      store.tasks = [taskGenerator.task({ id: 't-1', columnId: 'c-1' })]
      const result = await store.moveTask('t-1', { columnId: 'c-2', position: 0 })

      expect(result).toBeNull()
      expect(store.error).toBe('conflict')
    })
  })

  describe('archiveTask', () => {
    it('marks the task as archived in state', async () => {
      const task = taskGenerator.task({ id: 't-1', archived: false })
      vi.mocked(api.archiveTask).mockResolvedValue(undefined)

      const store = useTaskStore()
      store.tasks = [task]
      store.currentTask = task

      const ok = await store.archiveTask('t-1')

      expect(ok).toBe(true)
      expect(store.tasks[0]?.archived).toBe(true)
      expect(store.currentTask?.archived).toBe(true)
    })
  })

  describe('deleteTask', () => {
    it('removes task from list and clears currentTask and related data', async () => {
      const task = taskGenerator.task({ id: 't-1' })
      vi.mocked(api.deleteTask).mockResolvedValue(undefined)

      const store = useTaskStore()
      store.tasks = [task]
      store.currentTask = task
      store.comments = [taskGenerator.comment({ taskId: 't-1' })]
      store.files = [taskGenerator.file({ taskId: 't-1' })]

      const ok = await store.deleteTask('t-1')

      expect(ok).toBe(true)
      expect(store.tasks).toEqual([])
      expect(store.currentTask).toBeNull()
      expect(store.comments).toEqual([])
      expect(store.files).toEqual([])
    })

    it('keeps state when ids differ', async () => {
      const task = taskGenerator.task({ id: 't-1' })
      vi.mocked(api.deleteTask).mockResolvedValue(undefined)

      const store = useTaskStore()
      store.tasks = [task]

      const ok = await store.deleteTask('t-2')

      expect(ok).toBe(true)
      expect(store.tasks).toHaveLength(1)
    })
  })

  describe('loadComments / createComment / updateComment / deleteComment', () => {
    it('loadComments populates comments', async () => {
      const comments = taskGenerator.comments(2, 't-1')
      vi.mocked(api.listComments).mockResolvedValue(comments)

      const store = useTaskStore()
      const ok = await store.loadComments('t-1')

      expect(ok).toBe(true)
      expect(store.comments).toEqual(comments)
    })

    it('createComment appends to list', async () => {
      const comment = taskGenerator.comment({ id: 'cm-1' })
      vi.mocked(api.createComment).mockResolvedValue(comment)

      const store = useTaskStore()
      const result = await store.createComment('t-1', { authorId: 'u-1', text: 'Hi' })

      expect(result).toEqual(comment)
      expect(store.comments).toHaveLength(1)
    })

    it('updateComment replaces in list', async () => {
      const original = taskGenerator.comment({ id: 'cm-1', text: 'Old' })
      const updated = taskGenerator.comment({ id: 'cm-1', text: 'New' })
      vi.mocked(api.updateComment).mockResolvedValue(updated)

      const store = useTaskStore()
      store.comments = [original]

      const result = await store.updateComment('cm-1', { text: 'New' })

      expect(result).toEqual(updated)
      expect(store.comments[0]).toEqual(updated)
    })

    it('deleteComment removes from list', async () => {
      const comment = taskGenerator.comment({ id: 'cm-1' })
      vi.mocked(api.deleteComment).mockResolvedValue(undefined)

      const store = useTaskStore()
      store.comments = [comment]

      const ok = await store.deleteComment('cm-1')

      expect(ok).toBe(true)
      expect(store.comments).toEqual([])
    })
  })

  describe('loadFiles / deleteFile', () => {
    it('loadFiles populates files', async () => {
      const files = taskGenerator.files(2, 't-1')
      vi.mocked(api.listFiles).mockResolvedValue(files)

      const store = useTaskStore()
      const ok = await store.loadFiles('t-1')

      expect(ok).toBe(true)
      expect(store.files).toEqual(files)
    })

    it('deleteFile removes from list', async () => {
      const file = taskGenerator.file({ id: 'f-1' })
      vi.mocked(api.deleteFile).mockResolvedValue(undefined)

      const store = useTaskStore()
      store.files = [file]

      const ok = await store.deleteFile('f-1')

      expect(ok).toBe(true)
      expect(store.files).toEqual([])
    })
  })

  describe('tasksForColumn', () => {
    it('returns non-archived tasks for a column sorted by position', () => {
      const store = useTaskStore()
      store.tasks = [
        taskGenerator.task({ id: 't-1', columnId: 'c-1', position: 2, archived: false }),
        taskGenerator.task({ id: 't-2', columnId: 'c-1', position: 0, archived: false }),
        taskGenerator.task({ id: 't-3', columnId: 'c-1', position: 1, archived: true }),
        taskGenerator.task({ id: 't-4', columnId: 'c-2', position: 0, archived: false }),
      ]

      const result = store.tasksForColumn('c-1')

      expect(result.map((t) => t.id)).toEqual(['t-2', 't-1'])
    })

    it('returns an empty array for a column with no tasks', () => {
      const store = useTaskStore()
      store.tasks = [taskGenerator.task({ columnId: 'c-1' })]

      expect(store.tasksForColumn('c-2')).toEqual([])
    })
  })

  describe('moveTask optimistic update', () => {
    it('applies optimistic update immediately before API resolves', async () => {
      const original = taskGenerator.task({ id: 't-1', columnId: 'c-1', position: 0 })
      const moved = taskGenerator.task({ id: 't-1', columnId: 'c-2', position: 2 })
      vi.mocked(api.moveTask).mockResolvedValue(moved)

      const store = useTaskStore()
      store.tasks = [original]
      store.currentTask = original

      const promise = store.moveTask('t-1', { columnId: 'c-2', position: 2 })

      expect(store.tasks[0].columnId).toBe('c-2')
      expect(store.tasks[0].position).toBe(2)
      expect(store.currentTask?.columnId).toBe('c-2')

      await promise
      expect(store.tasks[0]).toEqual(moved)
    })

    it('rolls back optimistic update on API failure', async () => {
      const original = taskGenerator.task({ id: 't-1', columnId: 'c-1', position: 0 })
      vi.mocked(api.moveTask).mockRejectedValue(new Error('conflict'))

      const store = useTaskStore()
      store.tasks = [original]
      store.currentTask = original

      const result = await store.moveTask('t-1', { columnId: 'c-2', position: 2 })

      expect(result).toBeNull()
      expect(store.error).toBe('conflict')
      expect(store.tasks[0].columnId).toBe('c-1')
      expect(store.tasks[0].position).toBe(0)
      expect(store.currentTask?.columnId).toBe('c-1')
    })

    it('still moves task when not in local state (no optimistic update)', async () => {
      const moved = taskGenerator.task({ id: 't-new', columnId: 'c-2' })
      vi.mocked(api.moveTask).mockResolvedValue(moved)

      const store = useTaskStore()
      const result = await store.moveTask('t-new', { columnId: 'c-2', position: 0 })

      expect(result).toEqual(moved)
    })
  })

  describe('handleTaskMoved', () => {
    it('updates columnId for the given task in tasks list', () => {
      const store = useTaskStore()
      store.tasks = [taskGenerator.task({ id: 't-1', columnId: 'c-1' })]

      store.handleTaskMoved('t-1', 'c-2')

      expect(store.tasks[0].columnId).toBe('c-2')
    })

    it('updates currentTask if it matches', () => {
      const store = useTaskStore()
      const task = taskGenerator.task({ id: 't-1', columnId: 'c-1' })
      store.tasks = [task]
      store.currentTask = task

      store.handleTaskMoved('t-1', 'c-3')

      expect(store.currentTask?.columnId).toBe('c-3')
    })

    it('does nothing for unknown task', () => {
      const store = useTaskStore()
      store.tasks = [taskGenerator.task({ id: 't-1', columnId: 'c-1' })]

      store.handleTaskMoved('t-unknown', 'c-2')

      expect(store.tasks[0].columnId).toBe('c-1')
    })
  })

  describe('handleTaskArchived', () => {
    it('marks task as archived in tasks list', () => {
      const store = useTaskStore()
      store.tasks = [taskGenerator.task({ id: 't-1', archived: false })]

      store.handleTaskArchived('t-1')

      expect(store.tasks[0].archived).toBe(true)
    })

    it('updates currentTask if it matches', () => {
      const store = useTaskStore()
      const task = taskGenerator.task({ id: 't-1', archived: false })
      store.tasks = [task]
      store.currentTask = task

      store.handleTaskArchived('t-1')

      expect(store.currentTask?.archived).toBe(true)
    })
  })

  describe('deleteTaskFromList', () => {
    it('removes task from list', () => {
      const store = useTaskStore()
      store.tasks = [taskGenerator.task({ id: 't-1' }), taskGenerator.task({ id: 't-2' })]

      store.deleteTaskFromList('t-1')

      expect(store.tasks).toHaveLength(1)
      expect(store.tasks[0].id).toBe('t-2')
    })

    it('clears currentTask and comments/files when deleted task is current', () => {
      const store = useTaskStore()
      const task = taskGenerator.task({ id: 't-1' })
      store.tasks = [task]
      store.currentTask = task
      store.comments = [taskGenerator.comment({ taskId: 't-1' })]
      store.files = [taskGenerator.file({ taskId: 't-1' })]

      store.deleteTaskFromList('t-1')

      expect(store.tasks).toEqual([])
      expect(store.currentTask).toBeNull()
      expect(store.comments).toEqual([])
      expect(store.files).toEqual([])
    })

    it('does nothing for unknown task', () => {
      const store = useTaskStore()
      store.tasks = [taskGenerator.task({ id: 't-1' })]

      store.deleteTaskFromList('t-unknown')

      expect(store.tasks).toHaveLength(1)
    })
  })

  describe('handleTaskUpdated', () => {
    it('re-fetches task and updates currentTask when taskId matches', async () => {
      const original = taskGenerator.task({ id: 't-1', title: 'Old' })
      const updated = taskGenerator.task({ id: 't-1', title: 'New' })
      vi.mocked(api.getTask).mockResolvedValue(updated)

      const store = useTaskStore()
      store.currentTask = original
      store.tasks = [original]

      store.handleTaskUpdated('t-1')
      await new Promise((r) => setTimeout(r, 0))

      expect(store.currentTask?.title).toBe('New')
      expect(store.tasks[0].title).toBe('New')
    })

    it('does not refetch when taskId does not match currentTask', async () => {
      vi.mocked(api.getTask).mockResolvedValue(taskGenerator.task({ id: 't-1' }))
      const store = useTaskStore()
      store.currentTask = taskGenerator.task({ id: 't-2' })

      store.handleTaskUpdated('t-1')

      expect(api.getTask).not.toHaveBeenCalled()
    })
  })

  describe('scheduleCommentRefresh', () => {
    it('re-fetches comments when taskId matches currentTask', async () => {
      const comments = taskGenerator.comments(2, 't-1')
      vi.mocked(api.listComments).mockResolvedValue(comments)

      const store = useTaskStore()
      store.currentTask = taskGenerator.task({ id: 't-1' })

      store.scheduleCommentRefresh('t-1')
      await new Promise((r) => setTimeout(r, 0))

      expect(store.comments).toHaveLength(2)
    })

    it('does nothing when taskId does not match currentTask', () => {
      const store = useTaskStore()
      store.currentTask = taskGenerator.task({ id: 't-2' })

      store.scheduleCommentRefresh('t-1')

      expect(api.listComments).not.toHaveBeenCalled()
    })
  })

  describe('clearCurrent / clearTasks', () => {
    it('clearCurrent resets currentTask, comments and files', () => {
      const store = useTaskStore()
      store.currentTask = taskGenerator.task({ id: 't-1' })
      store.comments = [taskGenerator.comment()]
      store.files = [taskGenerator.file()]

      store.clearCurrent()

      expect(store.currentTask).toBeNull()
      expect(store.comments).toEqual([])
      expect(store.files).toEqual([])
    })

    it('clearTasks resets the tasks list', () => {
      const store = useTaskStore()
      store.tasks = taskGenerator.tasks(3)

      store.clearTasks()

      expect(store.tasks).toEqual([])
    })
  })
})
