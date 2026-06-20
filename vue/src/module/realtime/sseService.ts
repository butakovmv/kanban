export interface SseEvent {
  type: string
  data: Record<string, unknown>
  id: string
}

export type EventHandler = (event: SseEvent) => void

const KNOWN_EVENT_TYPES = [
  'task_created',
  'task_updated',
  'task_moved',
  'task_deleted',
  'task_archived',
  'comment_added',
  'comment_updated',
  'comment_deleted',
  'board_updated',
  'board_archived',
  'columns_reordered',
] as const

class SseService {
  private eventSource: EventSource | null = null
  private handlers = new Map<string, Set<EventHandler>>()
  private reconnectTimeout: number | null = null
  private currentBoardId: string | undefined
  private currentProjectId: string | undefined

  connect(boardId?: string, projectId?: string): void {
    this.disconnect()
    this.currentBoardId = boardId
    this.currentProjectId = projectId

    if (typeof EventSource === 'undefined') {
      return
    }

    const params = new URLSearchParams()
    if (boardId) params.set('board_id', boardId)
    if (projectId) params.set('project_id', projectId)
    const query = params.toString()
    const url = `/api/v1/events${query ? '?' + query : ''}`

    this.eventSource = new EventSource(url)

    for (const type of KNOWN_EVENT_TYPES) {
      this.registerEventListener(type)
    }

    this.eventSource.onerror = () => {
      this.disconnect()
      this.reconnectTimeout = window.setTimeout(
        () => this.connect(this.currentBoardId, this.currentProjectId),
        3000,
      )
    }
  }

  disconnect(): void {
    if (this.reconnectTimeout !== null) {
      clearTimeout(this.reconnectTimeout)
      this.reconnectTimeout = null
    }
    if (this.eventSource !== null) {
      this.eventSource.close()
      this.eventSource = null
    }
  }

  on(eventType: string, handler: EventHandler): void {
    if (!this.handlers.has(eventType)) {
      this.handlers.set(eventType, new Set())
    }
    this.handlers.get(eventType)!.add(handler)
  }

  off(eventType: string, handler: EventHandler): void {
    this.handlers.get(eventType)?.delete(handler)
  }

  private registerEventListener(eventType: string): void {
    if (!this.eventSource) return

    const listener = (msg: MessageEvent) => {
      try {
        const parsed = JSON.parse(msg.data) as Record<string, unknown>
        const event: SseEvent = {
          type: eventType,
          data: parsed,
          id: msg.lastEventId || '',
        }
        this.dispatch(event)
      } catch {
        /* ignore parse errors */
      }
    }

    this.eventSource.addEventListener(eventType, listener)
  }

  private dispatch(event: SseEvent): void {
    this.handlers.get(event.type)?.forEach((h) => h(event))
    this.handlers.get('*')?.forEach((h) => h(event))
  }
}

export const sseService = new SseService()
