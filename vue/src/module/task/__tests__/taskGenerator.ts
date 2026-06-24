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

function uid(): string {
  return Math.random().toString(36).slice(2, 10)
}

/**
 * Генератор тестовых данных для task-модуля.
 * Создаёт случайные сущности для тестов api.ts и store.ts.
 */
export const taskGenerator = {
  task(overrides: Partial<Task> = {}): Task {
    const id = `task-${uid()}`
    return {
      id,
      projectId: `project-${uid()}`,
      columnId: `column-${uid()}`,
      title: `Task ${uid()}`,
      description: 'A test task description',
      assigneeId: null,
      position: 0,
      dueDate: null,
      priority: null,
      archived: false,
      createdAt: new Date('2025-01-01T00:00:00Z').toISOString(),
      updatedAt: new Date('2025-01-02T00:00:00Z').toISOString(),
      ...overrides,
    }
  },

  tasks(count: number, columnId?: string, projectId?: string): Task[] {
    return Array.from({ length: count }, (_, i) =>
      this.task({ position: i, columnId, projectId }),
    )
  },

  rawTask(overrides: Record<string, unknown> = {}): Record<string, unknown> {
    const id = `task-${uid()}`
    return {
      id,
      project_id: `project-${uid()}`,
      column_id: `column-${uid()}`,
      title: `Task ${uid()}`,
      description: 'A test task description',
      assignee_id: null,
      position: 0,
      due_date: null,
      priority: null,
      archived: false,
      created_at: '2025-01-01T00:00:00.000Z',
      updated_at: '2025-01-02T00:00:00.000Z',
      ...overrides,
    }
  },

  rawTasks(count: number, columnId?: string, projectId?: string): Record<string, unknown>[] {
    return Array.from({ length: count }, (_, i) =>
      this.rawTask({ position: i, column_id: columnId, project_id: projectId }),
    )
  },

  createRequest(overrides: Partial<CreateTaskRequest> = {}): CreateTaskRequest {
    return {
      projectId: `project-${Math.random().toString(36).slice(2, 8)}`,
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
    const id = `comment-${uid()}`
    return {
      id,
      taskId: `task-${uid()}`,
      authorId: `user-${uid()}`,
      text: `Comment ${uid()}`,
      createdAt: new Date('2025-01-01T00:00:00Z').toISOString(),
      updatedAt: new Date('2025-01-02T00:00:00Z').toISOString(),
      ...overrides,
    }
  },

  comments(count: number, taskId?: string): Comment[] {
    return Array.from({ length: count }, () => this.comment({ taskId }))
  },

  rawComment(overrides: Record<string, unknown> = {}): Record<string, unknown> {
    const id = `comment-${uid()}`
    return {
      id,
      task_id: `task-${uid()}`,
      author_id: `user-${uid()}`,
      text: `Comment ${uid()}`,
      created_at: '2025-01-01T00:00:00.000Z',
      updated_at: '2025-01-02T00:00:00.000Z',
      ...overrides,
    }
  },

  rawComments(count: number, taskId?: string): Record<string, unknown>[] {
    return Array.from({ length: count }, () => this.rawComment({ task_id: taskId }))
  },

  createCommentRequest(overrides: Partial<CreateCommentRequest> = {}): CreateCommentRequest {
    return {
      authorId: `user-${uid()}`,
      text: `Comment ${uid()}`,
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
    const id = `file-${uid()}`
    return {
      id,
      taskId: `task-${uid()}`,
      fileName: `file-${uid()}.txt`,
      contentType: 'text/plain',
      sizeBytes: 1024,
      storageKey: `storage/${id}`,
      uploadedBy: `user-${uid()}`,
      uploadedAt: new Date('2025-01-01T00:00:00Z').toISOString(),
      ...overrides,
    }
  },

  files(count: number, taskId?: string): FileAttachment[] {
    return Array.from({ length: count }, () => this.file({ taskId }))
  },

  rawFile(overrides: Record<string, unknown> = {}): Record<string, unknown> {
    const id = `file-${uid()}`
    return {
      id,
      task_id: `task-${uid()}`,
      file_name: `file-${uid()}.txt`,
      content_type: 'text/plain',
      size_bytes: 1024,
      storage_key: `storage/${id}`,
      uploaded_by: `user-${uid()}`,
      uploaded_at: '2025-01-01T00:00:00.000Z',
      ...overrides,
    }
  },

  rawFiles(count: number, taskId?: string): Record<string, unknown>[] {
    return Array.from({ length: count }, () => this.rawFile({ task_id: taskId }))
  },
}
