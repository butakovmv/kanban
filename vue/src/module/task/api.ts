import { get, post, put, del, patch } from '../../fetch'

/**
 * Задача, возвращаемая сервером.
 */
export interface Task {
  id: string
  boardId: string
  columnId: string
  title: string
  description: string | null
  assigneeId: string | null
  position: number
  dueDate: string | null
  archived: boolean
  createdAt: string
  updatedAt: string
}

/**
 * Комментарий к задаче.
 */
export interface Comment {
  id: string
  taskId: string
  authorId: string
  text: string
  createdAt: string
  updatedAt: string
}

/**
 * Файл, прикреплённый к задаче.
 */
export interface FileAttachment {
  id: string
  taskId: string
  fileName: string
  contentType: string
  sizeBytes: number
  storageKey: string
  uploadedBy: string
  uploadedAt: string
}

/**
 * Параметры запроса создания задачи.
 */
export interface CreateTaskRequest {
  boardId: string
  columnId: string
  title: string
  description?: string | null
  assigneeId?: string | null
  dueDate?: string | null
}

/**
 * Параметры запроса обновления задачи.
 */
export interface UpdateTaskRequest {
  title?: string
  description?: string | null
  assigneeId?: string | null
  dueDate?: string | null
}

/**
 * Параметры запроса перемещения задачи между колонками.
 */
export interface MoveTaskRequest {
  columnId: string
  position: number
}

/**
 * Параметры запроса создания комментария.
 */
export interface CreateCommentRequest {
  text: string
}

/**
 * Параметры запроса обновления комментария.
 */
export interface UpdateCommentRequest {
  text: string
}

/**
 * Преобразует snake_case-ответ сервера в camelCase представление задачи.
 */
function toTask(raw: Task): Task {
  return {
    id: raw.id,
    boardId: raw.boardId,
    columnId: raw.columnId,
    title: raw.title,
    description: raw.description,
    assigneeId: raw.assigneeId,
    position: raw.position,
    dueDate: raw.dueDate,
    archived: raw.archived,
    createdAt: raw.createdAt,
    updatedAt: raw.updatedAt,
  }
}

/**
 * Преобразует snake_case-ответ сервера в camelCase представление комментария.
 */
function toComment(raw: Comment): Comment {
  return {
    id: raw.id,
    taskId: raw.taskId,
    authorId: raw.authorId,
    text: raw.text,
    createdAt: raw.createdAt,
    updatedAt: raw.updatedAt,
  }
}

/**
 * Преобразует snake_case-ответ сервера в camelCase представление файла.
 */
function toFile(raw: FileAttachment): FileAttachment {
  return {
    id: raw.id,
    taskId: raw.taskId,
    fileName: raw.fileName,
    contentType: raw.contentType,
    sizeBytes: raw.sizeBytes,
    storageKey: raw.storageKey,
    uploadedBy: raw.uploadedBy,
    uploadedAt: raw.uploadedAt,
  }
}

/**
 * Возвращает список задач на доске.
 * @param boardId идентификатор доски
 * @param includeArchived включать ли архивные задачи (по умолчанию false)
 * @returns массив задач
 */
export function listTasks(boardId: string, includeArchived = false): Promise<Task[]> {
  const query = includeArchived ? '?include_archived=true' : '?include_archived=false'
  return get<Task[]>(`/boards/${encodeURIComponent(boardId)}/tasks${query}`).then((items) =>
    items.map(toTask),
  )
}

/**
 * Возвращает задачу по идентификатору.
 * @param id идентификатор задачи
 */
export function getTask(id: string): Promise<Task> {
  return get<Task>(`/tasks/${encodeURIComponent(id)}`).then(toTask)
}

/**
 * Создаёт новую задачу.
 * @param request boardId, columnId, title, description?, assigneeId?, dueDate?
 * @returns созданная задача
 */
export function createTask(request: CreateTaskRequest): Promise<Task> {
  const body: Record<string, unknown> = {
    board_id: request.boardId,
    column_id: request.columnId,
    title: request.title,
  }
  if (request.description !== undefined) {
    body['description'] = request.description
  }
  if (request.assigneeId !== undefined) {
    body['assignee_id'] = request.assigneeId
  }
  if (request.dueDate !== undefined) {
    body['due_date'] = request.dueDate
  }
  return post<Task>('/tasks', body).then(toTask)
}

/**
 * Обновляет задачу.
 * @param id идентификатор задачи
 * @param request title?, description?, assigneeId?, dueDate?
 * @returns обновлённая задача
 */
export function updateTask(id: string, request: UpdateTaskRequest): Promise<Task> {
  const body: Record<string, unknown> = {}
  if (request.title !== undefined) {
    body['title'] = request.title
  }
  if (request.description !== undefined) {
    body['description'] = request.description
  }
  if (request.assigneeId !== undefined) {
    body['assignee_id'] = request.assigneeId
  }
  if (request.dueDate !== undefined) {
    body['due_date'] = request.dueDate
  }
  return put<Task>(`/tasks/${encodeURIComponent(id)}`, body).then(toTask)
}

/**
 * Перемещает задачу в другую колонку и/или на новую позицию.
 * @param id идентификатор задачи
 * @param request columnId, position
 * @returns перемещённая задача
 */
export function moveTask(id: string, request: MoveTaskRequest): Promise<Task> {
  return patch<Task>(`/tasks/${encodeURIComponent(id)}/move`, {
    column_id: request.columnId,
    position: request.position,
  }).then(toTask)
}

/**
 * Архивирует задачу.
 * @param id идентификатор задачи
 */
export function archiveTask(id: string): Promise<void> {
  return post<void>(`/tasks/${encodeURIComponent(id)}/archive`, {})
}

/**
 * Удаляет задачу.
 * @param id идентификатор задачи
 */
export function deleteTask(id: string): Promise<void> {
  return del<void>(`/tasks/${encodeURIComponent(id)}`)
}

/**
 * Возвращает список комментариев задачи.
 * @param taskId идентификатор задачи
 * @returns массив комментариев
 */
export function listComments(taskId: string): Promise<Comment[]> {
  return get<Comment[]>(`/tasks/${encodeURIComponent(taskId)}/comments`).then((items) =>
    items.map(toComment),
  )
}

/**
 * Создаёт комментарий к задаче.
 * @param taskId идентификатор задачи
 * @param request text
 * @returns созданный комментарий
 */
export function createComment(taskId: string, request: CreateCommentRequest): Promise<Comment> {
  return post<Comment>(`/tasks/${encodeURIComponent(taskId)}/comments`, {
    text: request.text,
  }).then(toComment)
}

/**
 * Обновляет комментарий.
 * @param id идентификатор комментария
 * @param request text
 * @returns обновлённый комментарий
 */
export function updateComment(id: string, request: UpdateCommentRequest): Promise<Comment> {
  return put<Comment>(`/comments/${encodeURIComponent(id)}`, {
    text: request.text,
  }).then(toComment)
}

/**
 * Удаляет комментарий.
 * @param id идентификатор комментария
 */
export function deleteComment(id: string): Promise<void> {
  return del<void>(`/comments/${encodeURIComponent(id)}`)
}

/**
 * Возвращает список файлов задачи.
 * @param taskId идентификатор задачи
 * @returns массив файлов
 */
export function listFiles(taskId: string): Promise<FileAttachment[]> {
  return get<FileAttachment[]>(`/tasks/${encodeURIComponent(taskId)}/files`).then((items) =>
    items.map(toFile),
  )
}

/**
 * Удаляет файл.
 * @param id идентификатор файла
 */
export function deleteFile(id: string): Promise<void> {
  return del<void>(`/files/${encodeURIComponent(id)}`)
}

/**
 * Формирует URL для скачивания файла.
 * Использует тот же base path, что и request в fetch.ts, но возвращает готовый URL.
 * @param id идентификатор файла
 * @returns относительный URL для скачивания
 */
export function getFileDownloadUrl(id: string): string {
  return `/api/v1/files/${encodeURIComponent(id)}/download`
}
