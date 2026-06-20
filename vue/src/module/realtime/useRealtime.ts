import { onMounted, onUnmounted, unref, watch } from 'vue'
import type { Ref } from 'vue'
import { sseService } from './sseService'
import { useBoardStore } from '../board/store'
import { useTaskStore } from '../task/store'

function toValue(r: string | Ref<string | undefined> | undefined): string | undefined {
  if (r === undefined) return undefined
  return unref(r) ?? undefined
}

export function useRealtime(boardId?: string | Ref<string | undefined>, projectId?: string) {
  const boardStore = useBoardStore()
  const taskStore = useTaskStore()

  function handleTaskCreated(event: { data: Record<string, unknown> }) {
    const taskId = event.data['task_id'] as string | undefined
    const eBoardId = event.data['board_id'] as string | undefined
    if (taskId && eBoardId && boardStore.currentBoard?.id === eBoardId) {
      taskStore.scheduleRefresh()
    }
  }

  function handleTaskUpdated(event: { data: Record<string, unknown> }) {
    const taskId = event.data['task_id'] as string | undefined
    if (taskId) {
      taskStore.handleTaskUpdated(taskId)
    }
  }

  function handleTaskMoved(event: { data: Record<string, unknown> }) {
    const taskId = event.data['task_id'] as string | undefined
    const columnId = event.data['column_id'] as string | undefined
    if (taskId && columnId) {
      taskStore.handleTaskMoved(taskId, columnId)
    }
  }

  function handleTaskDeleted(event: { data: Record<string, unknown> }) {
    const taskId = event.data['task_id'] as string | undefined
    if (taskId) {
      taskStore.deleteTaskFromList(taskId)
    }
  }

  function handleTaskArchived(event: { data: Record<string, unknown> }) {
    const taskId = event.data['task_id'] as string | undefined
    if (taskId) {
      taskStore.handleTaskArchived(taskId)
    }
  }

  function handleCommentAdded(event: { data: Record<string, unknown> }) {
    const taskId = event.data['task_id'] as string
    if (taskId && taskStore.currentTask?.id === taskId) {
      taskStore.scheduleCommentRefresh(taskId)
    }
  }

  function handleBoardUpdated(event: { data: Record<string, unknown> }) {
    const eBoardId = event.data['board_id'] as string | undefined
    if (eBoardId && boardStore.currentBoard?.id === eBoardId) {
      taskStore.scheduleRefresh()
    }
  }

  function connect() {
    const bid = toValue(boardId)
    sseService.connect(bid, projectId)
    sseService.on('task_created', handleTaskCreated)
    sseService.on('task_updated', handleTaskUpdated)
    sseService.on('task_moved', handleTaskMoved)
    sseService.on('task_deleted', handleTaskDeleted)
    sseService.on('task_archived', handleTaskArchived)
    sseService.on('comment_added', handleCommentAdded)
    sseService.on('board_updated', handleBoardUpdated)
  }

  function disconnect() {
    sseService.off('task_created', handleTaskCreated)
    sseService.off('task_updated', handleTaskUpdated)
    sseService.off('task_moved', handleTaskMoved)
    sseService.off('task_deleted', handleTaskDeleted)
    sseService.off('task_archived', handleTaskArchived)
    sseService.off('comment_added', handleCommentAdded)
    sseService.off('board_updated', handleBoardUpdated)
    sseService.disconnect()
  }

  onMounted(connect)
  onUnmounted(disconnect)

  if (typeof boardId === 'object' && boardId !== null) {
    watch(boardId, () => {
      disconnect()
      connect()
    })
  }
}
