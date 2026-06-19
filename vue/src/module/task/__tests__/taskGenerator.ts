import type {
  Comment,
  CreateCommentRequest,
  CreateTaskRequest,
  FileAttachment,
  MoveTaskRequest,
  Task,
  UpdateCommentRequest,
  UpdateTaskRequest,
} from '../api'

/**
 * Генератор тестовых данных для task-модуля.
 * Создаёт случайные сущности для тестов api.ts и store.ts.
 */
export const taskGenerator = {
  task(overrides: Partial<Task> = {}): Task {
    const id = `task-${Math.random().toString(36).slice(2, 10)}`
    return {
      id,
      boardId: `board-${Math.random().toString(36).slice(2, 8)}`,
      columnId: `column-${Math.random().toString(36).slice(2, 8)}`,
      title: `Task ${Math.random().toString(36).slice(2, 6)}`,
      description: 'A test task description',
      assigneeId: null,
      position: 0,
      dueDate: null,
      archived: false,
      createdAt: new Date('2025-01-01T00:00:00Z').toISOString(),
      updatedAt: new Date('2025-01-02T00:00:00Z').toISOString(),
      ...overrides,
    }
  },

  tasks(count: number, columnId?: string, boardId?: string): Task[] {
    return Array.from({ length: count }, (_, i) =>
      this.task({ position: i, columnId, boardId }),
    )
  },

  createRequest(overrides: Partial<CreateTaskRequest> = {}): CreateTaskRequest {
    return {
      boardId: `board-${Math.random().toString(36).slice(2, 8)}`,
      columnId: `column-${Math.random().toString(36).slice(2, 8)}`,
      title: `New task ${Math.random().toString(36).slice(2, 6)}`,
      ...overrides,
    }
  },

  updateRequest(overrides: Partial<UpdateTaskRequest> = {}): UpdateTaskRequest {
    return {
      title: `Updated ${Math.random().toString(36).slice(2, 6)}`,
      ...overrides,
    }
  },

  moveRequest(overrides: Partial<MoveTaskRequest> = {}): MoveTaskRequest {
    return {
      columnId: `column-${Math.random().toString(36).slice(2, 8)}`,
      position: 0,
      ...overrides,
    }
  },

  comment(overrides: Partial<Comment> = {}): Comment {
    const id = `comment-${Math.random().toString(36).slice(2, 10)}`
    return {
      id,
      taskId: `task-${Math.random().toString(36).slice(2, 8)}`,
      authorId: `user-${Math.random().toString(36).slice(2, 8)}`,
      text: `Comment ${Math.random().toString(36).slice(2, 6)}`,
      createdAt: new Date('2025-01-01T00:00:00Z').toISOString(),
      updatedAt: new Date('2025-01-02T00:00:00Z').toISOString(),
      ...overrides,
    }
  },

  comments(count: number, taskId?: string): Comment[] {
    return Array.from({ length: count }, () => this.comment({ taskId }))
  },

  createCommentRequest(overrides: Partial<CreateCommentRequest> = {}): CreateCommentRequest {
    return {
      text: `Comment ${Math.random().toString(36).slice(2, 6)}`,
      ...overrides,
    }
  },

  updateCommentRequest(
    overrides: Partial<UpdateCommentRequest> = {},
  ): UpdateCommentRequest {
    return {
      text: `Updated ${Math.random().toString(36).slice(2, 6)}`,
      ...overrides,
    }
  },

  file(overrides: Partial<FileAttachment> = {}): FileAttachment {
    const id = `file-${Math.random().toString(36).slice(2, 10)}`
    return {
      id,
      taskId: `task-${Math.random().toString(36).slice(2, 8)}`,
      fileName: `file-${Math.random().toString(36).slice(2, 6)}.txt`,
      contentType: 'text/plain',
      sizeBytes: 1024,
      storageKey: `storage/${id}`,
      uploadedBy: `user-${Math.random().toString(36).slice(2, 8)}`,
      uploadedAt: new Date('2025-01-01T00:00:00Z').toISOString(),
      ...overrides,
    }
  },

  files(count: number, taskId?: string): FileAttachment[] {
    return Array.from({ length: count }, () => this.file({ taskId }))
  },
}
