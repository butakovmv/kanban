import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { sseService } from '../sseService'
import type { SseEvent } from '../sseService'

interface MockEventSource {
  url: string | URL
  close: ReturnType<typeof vi.fn>
  addEventListener: ReturnType<typeof vi.fn>
  removeEventListener: ReturnType<typeof vi.fn>
  onerror: ((event: Event) => void) | null
  readyState: number
}

let mockEventSource: MockEventSource | null = null

function createMockEventSource(this: void, url: string | URL): MockEventSource {
  const instance = {
    url,
    close: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    onerror: null as ((event: Event) => void) | null,
    readyState: 1,
  }
  mockEventSource = instance
  return instance
}

beforeEach(() => {
  mockEventSource = null
  vi.useFakeTimers()
  globalThis.EventSource = vi.fn().mockImplementation(createMockEventSource as unknown as (...args: unknown[]) => unknown) as unknown as typeof EventSource
})

afterEach(() => {
  vi.useRealTimers()
  delete (globalThis as Record<string, unknown>)['EventSource']
})

describe('sseService', () => {
  beforeEach(() => {
  })

  afterEach(() => {
    sseService.disconnect()
  })

  it('creates EventSource with correct URL', () => {
    sseService.connect('b-1')

    expect(globalThis.EventSource).toHaveBeenCalledWith('/api/v1/events?board_id=b-1')
  })

  it('creates EventSource with project_id param', () => {
    sseService.connect(undefined, 'p-1')

    expect(globalThis.EventSource).toHaveBeenCalledWith('/api/v1/events?project_id=p-1')
  })

  it('creates EventSource without params', () => {
    sseService.connect()

    expect(globalThis.EventSource).toHaveBeenCalledWith('/api/v1/events')
  })

  it('registers addEventListener for known event types', () => {
    sseService.connect('b-1')

    expect(mockEventSource).not.toBeNull()
    const knownTypes = [
      'task_created', 'task_updated', 'task_moved', 'task_deleted', 'task_archived',
      'comment_added', 'comment_updated', 'comment_deleted',
      'board_updated', 'board_archived', 'columns_reordered',
    ]
    for (const type of knownTypes) {
      expect(mockEventSource!.addEventListener).toHaveBeenCalledWith(type, expect.any(Function))
    }
  })

  it('dispatches event to registered handlers', () => {
    const handler = vi.fn()
    sseService.on('task_moved', handler)
    sseService.connect('b-1')

    const eventData = '{"task_id":"t-1","column_id":"c-2"}'
    const messageEvent = new MessageEvent('task_moved', {
      data: eventData,
      lastEventId: '123',
    })

    const addEventListenerCalls = vi.mocked(mockEventSource!.addEventListener).mock.calls
    const taskMovedCall = addEventListenerCalls.find(([type]) => type === 'task_moved')
    expect(taskMovedCall).toBeDefined()
    const listener = taskMovedCall![1] as (event: MessageEvent) => void
    listener(messageEvent)

    expect(handler).toHaveBeenCalledTimes(1)
    const event = handler.mock.calls[0][0] as SseEvent
    expect(event.type).toBe('task_moved')
    expect(event.data).toEqual({ task_id: 't-1', column_id: 'c-2' })
    expect(event.id).toBe('123')
  })

  it('dispatches to wildcard handler for all events', () => {
    const wildcard = vi.fn()
    const specific = vi.fn()
    sseService.on('*', wildcard)
    sseService.on('task_deleted', specific)
    sseService.connect('b-1')

    const messageEvent = new MessageEvent('task_deleted', {
      data: '{"task_id":"t-1"}',
    })

    const addEventListenerCalls = vi.mocked(mockEventSource!.addEventListener).mock.calls
    const taskDeletedCall = addEventListenerCalls.find(([type]) => type === 'task_deleted')
    const listener = taskDeletedCall![1] as (event: MessageEvent) => void
    listener(messageEvent)

    expect(specific).toHaveBeenCalledTimes(1)
    expect(wildcard).toHaveBeenCalledTimes(1)
  })

  it('does not dispatch to handlers after off()', () => {
    const handler = vi.fn()
    sseService.on('task_moved', handler)
    sseService.off('task_moved', handler)
    sseService.connect('b-1')

    const messageEvent = new MessageEvent('task_moved', {
      data: '{"task_id":"t-1"}',
    })

    const addEventListenerCalls = vi.mocked(mockEventSource!.addEventListener).mock.calls
    const taskMovedCall = addEventListenerCalls.find(([type]) => type === 'task_moved')
    const listener = taskMovedCall![1] as (event: MessageEvent) => void
    listener(messageEvent)

    expect(handler).not.toHaveBeenCalled()
  })

  it('reconnects on error after delay', () => {
    sseService.connect('b-1')

    const errorEvent = new Event('error')
    mockEventSource!.onerror!(errorEvent)

    expect(mockEventSource!.close).toHaveBeenCalled()
    vi.advanceTimersByTime(3000)

    expect(globalThis.EventSource).toHaveBeenCalledTimes(2)
  })

  it('disconnect() closes EventSource and clears reconnect timeout', () => {
    sseService.connect('b-1')

    const errorEvent = new Event('error')
    mockEventSource!.onerror!(errorEvent)

    sseService.disconnect()

    vi.advanceTimersByTime(3000)
    expect(globalThis.EventSource).toHaveBeenCalledTimes(1)
  })

  it('handles parse errors gracefully', () => {
    const handler = vi.fn()
    sseService.on('task_moved', handler)
    sseService.connect('b-1')

    const messageEvent = new MessageEvent('task_moved', {
      data: 'invalid json',
    })

    const addEventListenerCalls = vi.mocked(mockEventSource!.addEventListener).mock.calls
    const taskMovedCall = addEventListenerCalls.find(([type]) => type === 'task_moved')
    const listener = taskMovedCall![1] as (event: MessageEvent) => void
    listener(messageEvent)

    expect(handler).not.toHaveBeenCalled()
  })

  it('skips connect when EventSource is not available', () => {
    delete (globalThis as Record<string, unknown>)['EventSource']

    expect(() => sseService.connect('b-1')).not.toThrow()
  })

  it('disconnect() without connect() does not throw', () => {
    expect(() => sseService.disconnect()).not.toThrow()
  })
})
