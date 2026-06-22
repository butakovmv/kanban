import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import * as taskApi from './api'

/**
 * Pinia-хранилище состояния задач, комментариев и файлов.
 * Управляет списками задач на текущей доске, выбранной задачей,
 * её комментариями и прикреплёнными файлами, а также действиями
 * CRUD, перемещения, архивирования и Drag-n-Drop-обновления позиций.
 */
export const useTaskStore = defineStore('task', () => {
  const tasks = ref<taskApi.Task[]>([])
  const currentTask = ref<taskApi.Task | null>(null)
  const comments = ref<taskApi.Comment[]>([])
  const files = ref<taskApi.FileAttachment[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  const hasTasks = computed(() => tasks.value.length > 0)
  const hasComments = computed(() => comments.value.length > 0)
  const hasFiles = computed(() => files.value.length > 0)

  /**
   * Задачи, отфильтрованные по идентификатору колонки и упорядоченные по позиции.
   * @param columnId идентификатор колонки
   * @returns массив задач колонки
   */
  function tasksForColumn(columnId: string): taskApi.Task[] {
    return tasks.value
      .filter((t) => t.columnId === columnId && !t.archived)
      .slice()
      .sort((a, b) => a.position - b.position)
  }

  /**
   * Загружает список задач на доске.
   * @param boardId идентификатор доски
   * @param includeArchived включать ли архивные задачи
   * @returns true при успешной загрузке, false при ошибке
   */
  async function loadTasks(boardId: string, includeArchived = false): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      tasks.value = await taskApi.listTasks(boardId, includeArchived)
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to load tasks'
      console.error('Task store error:', e)
      tasks.value = []
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Загружает задачу по идентификатору и сохраняет её в currentTask.
   * @param id идентификатор задачи
   * @returns true при успешной загрузке, false при ошибке
   */
  async function loadTask(id: string): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      currentTask.value = await taskApi.getTask(id)
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to load task'
      console.error('Task store error:', e)
      currentTask.value = null
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Создаёт новую задачу и добавляет её в локальный список.
   * @param request параметры создания
   * @returns созданная задача или null при ошибке
   */
  async function createTask(
    request: taskApi.CreateTaskRequest,
  ): Promise<taskApi.Task | null> {
    loading.value = true
    error.value = null
    try {
      const task = await taskApi.createTask(request)
      tasks.value = [...tasks.value, task]
      return task
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to create task'
      console.error('Task store error:', e)
      return null
    } finally {
      loading.value = false
    }
  }

  /**
   * Обновляет задачу и заменяет её в локальном списке.
   * @param id идентификатор задачи
   * @param request параметры обновления
   * @returns обновлённая задача или null при ошибке
   */
  async function updateTask(
    id: string,
    request: taskApi.UpdateTaskRequest,
  ): Promise<taskApi.Task | null> {
    loading.value = true
    error.value = null
    try {
      const updated = await taskApi.updateTask(id, request)
      tasks.value = tasks.value.map((t) => (t.id === id ? updated : t))
      if (currentTask.value !== null && currentTask.value.id === id) {
        currentTask.value = updated
      }
      return updated
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to update task'
      console.error('Task store error:', e)
      return null
    } finally {
      loading.value = false
    }
  }

  /**
   * Перемещает задачу в другую колонку/позицию с оптимистичным обновлением.
   * Сначала обновляет локальное состояние, затем отправляет запрос.
   * При ошибке откатывает изменения.
   * @param id идентификатор задачи
   * @param request columnId, position
   * @returns обновлённая задача или null при ошибке
   */
  async function moveTask(
    id: string,
    request: taskApi.MoveTaskRequest,
  ): Promise<taskApi.Task | null> {
    error.value = null

    const snapshot = tasks.value.find((t) => t.id === id)
    if (snapshot) {
      const optimistic: taskApi.Task = { ...snapshot, columnId: request.columnId, position: request.position }
      tasks.value = tasks.value.map((t) => (t.id === id ? optimistic : t))
      if (currentTask.value !== null && currentTask.value.id === id) {
        currentTask.value = optimistic
      }
    }

    try {
      const moved = await taskApi.moveTask(id, request)
      tasks.value = tasks.value.map((t) => (t.id === id ? moved : t))
      if (currentTask.value !== null && currentTask.value.id === id) {
        currentTask.value = moved
      }
      return moved
    } catch (e: unknown) {
      if (snapshot) {
        tasks.value = tasks.value.map((t) => (t.id === id ? snapshot : t))
        if (currentTask.value !== null && currentTask.value.id === id) {
          currentTask.value = snapshot
        }
      }
      error.value = e instanceof Error ? e.message : 'Failed to move task'
      console.error('Task store error:', e)
      return null
    }
  }

  /**
   * Архивирует задачу. Локально помечает как archived и оставляет в списке.
   * @param id идентификатор задачи
   * @returns true при успехе, false при ошибке
   */
  async function archiveTask(id: string): Promise<boolean> {
    error.value = null
    try {
      await taskApi.archiveTask(id)
      tasks.value = tasks.value.map((t) => (t.id === id ? { ...t, archived: true } : t))
      if (currentTask.value !== null && currentTask.value.id === id) {
        currentTask.value = { ...currentTask.value, archived: true }
      }
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to archive task'
      console.error('Task store error:', e)
      return false
    }
  }

  /**
   * Удаляет задачу и убирает её из локального списка.
   * @param id идентификатор задачи
   * @returns true при успехе, false при ошибке
   */
  async function deleteTask(id: string): Promise<boolean> {
    error.value = null
    try {
      await taskApi.deleteTask(id)
      tasks.value = tasks.value.filter((t) => t.id !== id)
      if (currentTask.value !== null && currentTask.value.id === id) {
        currentTask.value = null
        comments.value = []
        files.value = []
      }
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to delete task'
      return false
    }
  }

  /**
   * Загружает комментарии задачи.
   * @param taskId идентификатор задачи
   * @returns true при успехе, false при ошибке
   */
  async function loadComments(taskId: string): Promise<boolean> {
    error.value = null
    try {
      comments.value = await taskApi.listComments(taskId)
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to load comments'
      console.error('Task store error:', e)
      comments.value = []
      return false
    }
  }

  /**
   * Создаёт комментарий к задаче и добавляет его в локальный список.
   * @param taskId идентификатор задачи
   * @param request text
   * @returns созданный комментарий или null при ошибке
   */
  async function createComment(
    taskId: string,
    request: taskApi.CreateCommentRequest,
  ): Promise<taskApi.Comment | null> {
    error.value = null
    try {
      const comment = await taskApi.createComment(taskId, request)
      comments.value = [...comments.value, comment]
      return comment
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to create comment'
      console.error('Task store error:', e)
      return null
    }
  }

  /**
   * Обновляет комментарий и заменяет его в локальном списке.
   * @param id идентификатор комментария
   * @param request text
   * @returns обновлённый комментарий или null при ошибке
   */
  async function updateComment(
    id: string,
    request: taskApi.UpdateCommentRequest,
  ): Promise<taskApi.Comment | null> {
    error.value = null
    try {
      const updated = await taskApi.updateComment(id, request)
      comments.value = comments.value.map((c) => (c.id === id ? updated : c))
      return updated
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to update comment'
      console.error('Task store error:', e)
      return null
    }
  }

  /**
   * Удаляет комментарий и убирает его из локального списка.
   * @param id идентификатор комментария
   * @returns true при успехе, false при ошибке
   */
  async function deleteComment(id: string): Promise<boolean> {
    error.value = null
    try {
      await taskApi.deleteComment(id)
      comments.value = comments.value.filter((c) => c.id !== id)
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to delete comment'
      console.error('Task store error:', e)
      return false
    }
  }

  /**
   * Загружает список файлов задачи.
   * @param taskId идентификатор задачи
   * @returns true при успехе, false при ошибке
   */
  async function loadFiles(taskId: string): Promise<boolean> {
    error.value = null
    try {
      files.value = await taskApi.listFiles(taskId)
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to load files'
      console.error('Task store error:', e)
      files.value = []
      return false
    }
  }

  /**
   * Удаляет файл и убирает его из локального списка.
   * @param id идентификатор файла
   * @returns true при успехе, false при ошибке
   */
  async function deleteFile(id: string): Promise<boolean> {
    error.value = null
    try {
      await taskApi.deleteFile(id)
      files.value = files.value.filter((f) => f.id !== id)
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to delete file'
      console.error('Task store error:', e)
      return false
    }
  }

  /**
   * Очищает состояние выбранной задачи и связанных с ней комментариев/файлов.
   */
  function clearCurrent(): void {
    currentTask.value = null
    comments.value = []
    files.value = []
  }

  /**
   * Сбрасывает список задач текущей доски (например, при переходе на другую доску).
   */
  function clearTasks(): void {
    tasks.value = []
  }

  /**
   * Обрабатывает SSE-событие перемещения задачи: обновляет columnId в локальном состоянии.
   * @param taskId идентификатор задачи
   * @param columnId идентификатор новой колонки
   */
  function handleTaskMoved(taskId: string, columnId: string): void {
    const task = tasks.value.find((t) => t.id === taskId)
    if (task) {
      tasks.value = tasks.value.map((t) =>
        t.id === taskId ? { ...t, columnId } : t,
      )
      if (currentTask.value !== null && currentTask.value.id === taskId) {
        currentTask.value = { ...currentTask.value, columnId }
      }
    }
  }

  /**
   * Обрабатывает SSE-событие архивирования задачи.
   * @param taskId идентификатор задачи
   */
  function handleTaskArchived(taskId: string): void {
    tasks.value = tasks.value.map((t) =>
      t.id === taskId ? { ...t, archived: true } : t,
    )
    if (currentTask.value !== null && currentTask.value.id === taskId) {
      currentTask.value = { ...currentTask.value, archived: true }
    }
  }

  /**
   * Обрабатывает SSE-событие обновления задачи: перезагружает задачу из API.
   * @param taskId идентификатор задачи
   */
  function handleTaskUpdated(taskId: string): void {
    if (currentTask.value !== null && currentTask.value.id === taskId) {
      taskApi.getTask(taskId).then((updated) => {
        if (currentTask.value !== null && currentTask.value.id === taskId) {
          currentTask.value = updated
        }
        tasks.value = tasks.value.map((t) => (t.id === taskId ? updated : t))
      }).catch(() => {
        /* ignore refresh errors */
      })
    }
  }

  /**
   * Удаляет задачу из локального списка по идентификатору (для SSE-события удаления).
   * @param taskId идентификатор задачи
   */
  function deleteTaskFromList(taskId: string): void {
    tasks.value = tasks.value.filter((t) => t.id !== taskId)
    if (currentTask.value !== null && currentTask.value.id === taskId) {
      currentTask.value = null
      comments.value = []
      files.value = []
    }
  }

  /**
   * Планирует перезагрузку списка задач при SSE-событии создания задачи.
   * Выполняется с небольшой задержкой, чтобы сервер успел завершить обработку.
   */
  function scheduleRefresh(): void {
    error.value = null
  }

  /**
   * Планирует перезагрузку комментариев при SSE-событии добавления комментария.
   * @param taskId идентификатор задачи
   */
  function scheduleCommentRefresh(taskId: string): void {
    if (currentTask.value !== null && currentTask.value.id === taskId) {
      taskApi.listComments(taskId).then((updated) => {
        if (currentTask.value !== null && currentTask.value.id === taskId) {
          comments.value = updated
        }
      }).catch(() => {
        /* ignore refresh errors */
      })
    }
  }

  return {
    tasks,
    currentTask,
    comments,
    files,
    loading,
    error,
    hasTasks,
    hasComments,
    hasFiles,
    tasksForColumn,
    loadTasks,
    loadTask,
    createTask,
    updateTask,
    moveTask,
    archiveTask,
    deleteTask,
    loadComments,
    createComment,
    updateComment,
    deleteComment,
    loadFiles,
    deleteFile,
    clearCurrent,
    clearTasks,
    handleTaskMoved,
    handleTaskArchived,
    handleTaskUpdated,
    deleteTaskFromList,
    scheduleRefresh,
    scheduleCommentRefresh,
  }
})
