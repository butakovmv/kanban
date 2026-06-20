import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useBoardStore } from '../../board/store'
import { useTaskStore } from '../../task/store'
import { sseService } from '../sseService'

vi.mock('../../board/api', async () => {
  const actual = await vi.importActual('../../board/api')
  return { ...actual, getBoard: vi.fn(), createBoard: vi.fn(), updateBoard: vi.fn(), deleteBoard: vi.fn(), reorderColumns: vi.fn() }
})

vi.mock('../../task/api', async () => {
  const actual = await vi.importActual('../../task/api')
  return { ...actual, listTasks: vi.fn(), getTask: vi.fn(), createTask: vi.fn(), updateTask: vi.fn(), moveTask: vi.fn(), archiveTask: vi.fn(), deleteTask: vi.fn(), listComments: vi.fn(), createComment: vi.fn(), updateComment: vi.fn(), deleteComment: vi.fn(), listFiles: vi.fn(), deleteFile: vi.fn() }
})

interface MockEventSource {
  url: string | URL
  close: ReturnType<typeof vi.fn>
  addEventListener: ReturnType<typeof vi.fn>
  removeEventListener: ReturnType<typeof vi.fn>
  onerror: ((event: Event) => void) | null
  onopen: ((event: Event) => void) | null
  readyState: number
}

let mockEventSource: MockEventSource | null = null

function createMockEventSource(this: void, url: string | URL): MockEventSource {
  const instance: MockEventSource = {
    url,
    close: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    onerror: null,
    onopen: null,
    readyState: 1,
  }
  mockEventSource = instance
  return instance
}

function getListener(type: string): (event: MessageEvent) => void {
  const calls = vi.mocked(mockEventSource!.addEventListener).mock.calls
  const found = calls.find(([t]) => t === type)
  if (!found) throw new Error(`No listener for type ${type}`)
  return found[1] as (event: MessageEvent) => void
}

function emitSse(type: string, data: Record<string, unknown>) {
  const listener = getListener(type)
  listener(new MessageEvent(type, { data: JSON.stringify(data), lastEventId: '1' }))
}

let onMountedCb: (() => void) | null = null
let onUnmountedCb: (() => void) | null = null

vi.mock('vue', async () => {
  const actual = await vi.importActual<typeof import('vue')>('vue')
  return {
    ...actual,
    onMounted: (fn: () => void) => { onMountedCb = fn },
    onUnmounted: (fn: () => void) => { onUnmountedCb = fn },
  }
})

describe('useRealtime', () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    vi.useFakeTimers()
    mockEventSource = null
    onMountedCb = null
    onUnmountedCb = null
    globalThis.EventSource = vi.fn().mockImplementation(createMockEventSource as any) as unknown as typeof EventSource
  })

  afterEach(() => {
    vi.useRealTimers()
    sseService.disconnect()
    delete (globalThis as Record<string, unknown>)['EventSource']
  })

  it('connects to SSE on mount', async () => {
    const { useRealtime } = await import('../useRealtime')
    useRealtime('b-1')
    onMountedCb?.()

    expect(globalThis.EventSource).toHaveBeenCalled()
    expect(mockEventSource).not.toBeNull()
  })

  it('disconnects SSE on unmount', async () => {
    const { useRealtime } = await import('../useRealtime')
    useRealtime('b-1')
    onMountedCb?.()
    onUnmountedCb?.()

    expect(mockEventSource!.close).toHaveBeenCalled()
  })

  it('handles task_moved event by calling taskStore.handleTaskMoved', async () => {
    const taskStore = useTaskStore()
    const spy = vi.spyOn(taskStore, 'handleTaskMoved')

    const { useRealtime } = await import('../useRealtime')
    useRealtime('b-1')
    onMountedCb?.()

    emitSse('task_moved', { task_id: 't-1', column_id: 'c-2' })

    expect(spy).toHaveBeenCalledWith('t-1', 'c-2')
  })

  it('handles task_deleted event by calling taskStore.deleteTaskFromList', async () => {
    const taskStore = useTaskStore()
    const spy = vi.spyOn(taskStore, 'deleteTaskFromList')

    const { useRealtime } = await import('../useRealtime')
    useRealtime('b-1')
    onMountedCb?.()

    emitSse('task_deleted', { task_id: 't-1' })

    expect(spy).toHaveBeenCalledWith('t-1')
  })

  it('handles task_archived event by calling taskStore.handleTaskArchived', async () => {
    const taskStore = useTaskStore()
    const spy = vi.spyOn(taskStore, 'handleTaskArchived')

    const { useRealtime } = await import('../useRealtime')
    useRealtime('b-1')
    onMountedCb?.()

    emitSse('task_archived', { task_id: 't-1' })

    expect(spy).toHaveBeenCalledWith('t-1')
  })

  it('handles task_updated event by calling taskStore.handleTaskUpdated', async () => {
    const taskStore = useTaskStore()
    const spy = vi.spyOn(taskStore, 'handleTaskUpdated')

    const { useRealtime } = await import('../useRealtime')
    useRealtime('b-1')
    onMountedCb?.()

    emitSse('task_updated', { task_id: 't-1' })

    expect(spy).toHaveBeenCalledWith('t-1')
  })

  it('handles task_created event by calling taskStore.scheduleRefresh for current board', async () => {
    const taskStore = useTaskStore()
    const boardStore = useBoardStore()
    const spy = vi.spyOn(taskStore, 'scheduleRefresh')
    boardStore.currentBoard = { id: 'b-1', projectId: 'p-1', name: 'Test', position: 0, createdAt: '' }

    const { useRealtime } = await import('../useRealtime')
    useRealtime('b-1')
    onMountedCb?.()

    emitSse('task_created', { task_id: 't-new', board_id: 'b-1' })

    expect(spy).toHaveBeenCalled()
  })

  it('ignores task_created for other boards', async () => {
    const taskStore = useTaskStore()
    const boardStore = useBoardStore()
    const spy = vi.spyOn(taskStore, 'scheduleRefresh')
    boardStore.currentBoard = { id: 'b-1', projectId: 'p-1', name: 'Test', position: 0, createdAt: '' }

    const { useRealtime } = await import('../useRealtime')
    useRealtime('b-1')
    onMountedCb?.()

    emitSse('task_created', { task_id: 't-new', board_id: 'b-2' })

    expect(spy).not.toHaveBeenCalled()
  })
})
